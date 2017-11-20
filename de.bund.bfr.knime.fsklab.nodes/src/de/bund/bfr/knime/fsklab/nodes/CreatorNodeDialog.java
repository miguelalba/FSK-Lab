package de.bund.bfr.knime.fsklab.nodes;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.util.SimpleFileFilter;

import de.bund.bfr.knime.fsklab.nodes.ui.UIUtils;
import de.bund.bfr.swing.FilePanel;
import de.bund.bfr.swing.UI;

public class CreatorNodeDialog extends NodeDialogPane {

	private final FilePanel modelScriptChooser;
	private final FilePanel paramScriptChooser;
	private final FilePanel vizScriptChooser;
	private final FilePanel metaDataChooser;

	private final CreatorNodeSettings settings = new CreatorNodeSettings();

	public CreatorNodeDialog() {

		modelScriptChooser = new FilePanel("Model script", FilePanel.OPEN_DIALOG, 50);
		modelScriptChooser.setToolTipText("Script that calculates the values of the model (Mandatory)");
		modelScriptChooser.setAcceptAllFiles(false);
		modelScriptChooser.addFileFilter(".r", "R file (*.r)");

		paramScriptChooser = new FilePanel("Parameters script", FilePanel.OPEN_DIALOG, 50);
		paramScriptChooser.setToolTipText("Script with the parameter values of the model (Optional).");
		paramScriptChooser.setAcceptAllFiles(false);
		paramScriptChooser.addFileFilter(".r", "R file (*.r)");

		vizScriptChooser = new FilePanel("Visualization script", FilePanel.OPEN_DIALOG, 50);
		vizScriptChooser.setToolTipText("Script with a number of commands to create plots or charts "
				+ "using the simulation results (Optional).");
		vizScriptChooser.setAcceptAllFiles(false);
		vizScriptChooser.addFileFilter(".r", "R file (*.r)");

		metaDataChooser = new FilePanel("XLSX spreadsheet", FilePanel.OPEN_DIALOG, 50);
		metaDataChooser.setToolTipText("XLSX file with model metadata");
		metaDataChooser.setAcceptAllFiles(false);
		metaDataChooser.addFileFilter(".xlsx", "XSLX spreadsheet (*.xslx)");

		JPanel gridPanel = new JPanel(new GridLayout(4, 1, 5, 5));
		gridPanel.add(modelScriptChooser);
		gridPanel.add(paramScriptChooser);
		gridPanel.add(vizScriptChooser);
		gridPanel.add(metaDataChooser);

		JPanel northPanel = UI.createNorthPanel(gridPanel);

		addTab("Options", northPanel);

		// TODO: Test tab
//		addTab("Files", createFilePanel());
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.load(settings);

			modelScriptChooser.setFileName(this.settings.modelScript);
			paramScriptChooser.setFileName(this.settings.parameterScript);
			vizScriptChooser.setFileName(this.settings.visualizationScript);
			metaDataChooser.setFileName(this.settings.spreadsheet);
		} catch (InvalidSettingsException exception) {
			throw new NotConfigurableException(exception.getMessage(), exception);
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {

		this.settings.modelScript = modelScriptChooser.getFileName();
		this.settings.parameterScript = paramScriptChooser.getFileName();
		this.settings.visualizationScript = vizScriptChooser.getFileName();
		this.settings.spreadsheet = metaDataChooser.getFileName();

		this.settings.save(settings);
	}

	JPanel createFilePanel() {
		DefaultListModel<File> listModel = new DefaultListModel<>();
		listModel.addElement(KNIMEConstants.getKNIMETempPath().toFile());

		JList<File> list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);

		final JButton addButton = UIUtils.createAddButton();
		final JButton removeButton = UIUtils.createRemoveButton();
		final JPanel buttonsPanel = UI.createHorizontalPanel(addButton, removeButton);

		addButton.addActionListener(event -> {
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.addChoosableFileFilter(new SimpleFileFilter("txt", "Plain text file"));
			fc.addChoosableFileFilter(new SimpleFileFilter("rdata", "R workspace file"));
			fc.setAcceptAllFileFilterUsed(false);  // do not use the AccepptAll FileFilter

			final int returnVal = fc.showOpenDialog(getPanel());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (!listModel.contains(fc.getSelectedFile())) {
					listModel.addElement(fc.getSelectedFile());
				}
			}
		});

		removeButton.addActionListener(event -> {
			final int selectedIndex = list.getSelectedIndex();
			if (selectedIndex != -1) {
				listModel.remove(selectedIndex);
			}
		});

		final JPanel northPanel = UI.createNorthPanel(new JScrollPane(list));
		northPanel.add(UI.createCenterPanel(buttonsPanel), BorderLayout.SOUTH);

		return northPanel;
	}
}
