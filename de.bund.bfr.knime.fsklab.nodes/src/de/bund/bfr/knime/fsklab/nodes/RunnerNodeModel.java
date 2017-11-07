/*
 ***************************************************************************************************
 * Copyright (c) 2017 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 *************************************************************************************************
 */
package de.bund.bfr.knime.fsklab.nodes;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.knime.base.node.util.exttool.ExtToolOutputNodeModel;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.knime.core.util.FileUtil;

import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskPortObjectSpec;
import de.bund.bfr.knime.fsklab.nodes.controller.ConsoleLikeRExecutor;
import de.bund.bfr.knime.fsklab.nodes.controller.LibRegistry;
import de.bund.bfr.knime.fsklab.nodes.controller.IRController.RException;
import de.bund.bfr.knime.fsklab.nodes.controller.RController;
import de.bund.bfr.knime.fsklab.rakip.ModelMath;
import de.bund.bfr.knime.fsklab.rakip.Parameter;

public class RunnerNodeModel extends ExtToolOutputNodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger("Fskx Runner Node Model");

	/** Output spec for an FSK object. */
	private static final FskPortObjectSpec FSK_SPEC = FskPortObjectSpec.INSTANCE;

	/** Output spec for a PNG image. */
	private static final ImagePortObjectSpec PNG_SPEC = new ImagePortObjectSpec(PNGImageContent.TYPE);

	private final RunnerNodeInternalSettings internalSettings = new RunnerNodeInternalSettings();

	private RunnerNodeSettings nodeSettings = new RunnerNodeSettings();

	// Input and output port types
	private static final PortType[] IN_TYPES = { FskPortObject.TYPE, BufferedDataTable.TYPE_OPTIONAL };
	private static final PortType[] OUT_TYPES = { FskPortObject.TYPE, ImagePortObject.TYPE_OPTIONAL };

	public RunnerNodeModel() {
		super(IN_TYPES, OUT_TYPES);
	}

	// --- internal settings methods ---

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		internalSettings.loadInternals(nodeInternDir);
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		internalSettings.saveInternals(nodeInternDir);
	}

	@Override
	protected void reset() {
		internalSettings.reset();
	}

	// --- node settings methods ---

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		nodeSettings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		nodeSettings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		nodeSettings.loadValidatedSettingsFrom(settings);
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { FSK_SPEC, PNG_SPEC };
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {

		FskPortObject fskObj = (FskPortObject) inData[0];

		final ModelMath modelMath = fskObj.genericModel.modelMath;

		final List<Parameter> indepVars;
		if (modelMath == null) {
			indepVars = Collections.emptyList();
		} else {
			indepVars = modelMath.parameter.stream().filter(it -> it.classification == Parameter.Classification.input)
					.collect(Collectors.toList());
		}

		if (!indepVars.isEmpty()) {
			try {
				fskObj.param = loadParameterScript(indepVars);
			} catch (IllegalArgumentException exception) {
				LOGGER.warn(exception.getMessage());
			}
		}

		try (RController controller = new RController()) {
			fskObj = runSnippet(controller, fskObj, exec.createSubExecutionContext(1.0));
		}

		try (FileInputStream fis = new FileInputStream(internalSettings.imageFile)) {
			final PNGImageContent content = new PNGImageContent(fis);
			internalSettings.plot = content.getImage();
			ImagePortObject imgObj = new ImagePortObject(content, PNG_SPEC);
			return new PortObject[] { fskObj, imgObj };
		} catch (IOException e) {
			LOGGER.warn("There is no image created");
			return new PortObject[] { fskObj };
		}
	}

	/**
	 * Generate a parameter script with parameters names and values from the model
	 * metadata.
	 * 
	 * @param params
	 *            non-empty list of input parameters.
	 * @throw IllegalArgumentException if a parameter is not valid
	 */
	private static String loadParameterScript(final List<Parameter> params) {

		String script = "";
		for (final Parameter param : params) {
			final String paramName = param.name;
			final String paramValue = param.value;

			if (StringUtils.isAnyEmpty(paramName, paramValue))
				throw new IllegalArgumentException("Parameter from metadata is not valid: " + param);

			script += paramName + " <- " + paramValue + "\n";
		}

		return script;
	}

	private FskPortObject runSnippet(final RController controller, final FskPortObject fskObj,
			final ExecutionMonitor exec) throws Exception {

		final ConsoleLikeRExecutor executor = new ConsoleLikeRExecutor(controller);

		// START RUNNING MODEL
		exec.setMessage("Setting up output capturing");
		executor.setupOutputCapturing(exec);

		exec.setMessage("Add paths to libraries");
		LibRegistry libRegistry = LibRegistry.instance();
		String cmd = ".libPaths(c('" + libRegistry.getInstallationPath().toString().replace("\\", "/")
				+ "', .libPaths()))";
		final String[] newPaths = executor.execute(cmd, exec).asStrings();

		// If parameters are defined in metadata used the values from there, otherwise
		// stick to the parameters script
		exec.setMessage("Run parameters from metadata or script");
		if (fskObj.genericModel.modelMath != null && !fskObj.genericModel.modelMath.parameter.isEmpty()) {
			for (final Parameter p : fskObj.genericModel.modelMath.parameter) {
				if (p.classification.equals(Parameter.Classification.input)) {
					if (p.dataType.equals("Integer")) {
						controller.assign(p.name, Integer.parseInt(p.value));
					} else if (p.dataType.equals("Double")) {
						controller.assign(p.name, Double.parseDouble(p.value));
					}
				}
			}
		} else {
			executor.executeIgnoreResult(fskObj.param, exec);
		}
		
		exec.setMessage("Run models script");
		executor.executeIgnoreResult(fskObj.model, exec);
		
		exec.setMessage("Run visualization script");
		try {
			NodeUtils.plot(internalSettings.imageFile, fskObj.viz, nodeSettings, executor, exec);
		} catch (final RException exception) {
			LOGGER.warn("Visualization script failed", exception);
		}
		
		// Restore .libPaths() to the original library path which happens to be in the
		// last position
		exec.setMessage("Restore library paths");
		executor.executeIgnoreResult(".libPaths()[" + newPaths.length + "]", exec);

		exec.setMessage("Collecting captured output");
		executor.finishOutputCapturing(exec);
		
		// END RUNNING MODEL

		// Save workspace
		if (fskObj.workspace == null) {
			fskObj.workspace = FileUtil.createTempFile("workspace", ".R");
		}
		controller.saveWorkspace(fskObj.workspace, exec);

		// process the return value of error capturing and update error and
		// output views accordingly
		if (!executor.getStdOut().isEmpty()) {
			setExternalOutput(getLinkedListFromOutput(executor.getStdOut()));
		}

		if (!executor.getStdErr().isEmpty()) {
			final LinkedList<String> output = getLinkedListFromOutput(executor.getStdErr());
			setExternalErrorOutput(output);

			for (final String line : output) {
				if (line.startsWith(ConsoleLikeRExecutor.ERROR_PREFIX)) {
					throw new RException("Error in R code: \"" + line + "\"", null);
				}
			}
		}

		// cleanup temporary variables of output capturing and consoleLikeCommand stuff
		exec.setMessage("Cleaning up");
		executor.cleanup(exec);

		return fskObj;
	}

	private static final LinkedList<String> getLinkedListFromOutput(final String output) {
		return Arrays.stream(output.split("\\r?\\n")).collect(Collectors.toCollection(LinkedList::new));
	}

	Image getResultImage() {
		return internalSettings.plot;
	}
}
