package de.bund.bfr.knime.fsklab.nodes;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.util.FileUtil;
import org.rosuda.REngine.REXPMismatchException;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;
import de.bund.bfr.knime.fsklab.r.client.LibRegistry;
import de.bund.bfr.knime.fsklab.r.client.RController;
import de.bund.bfr.knime.fsklab.r.client.ScriptExecutor;
import de.bund.bfr.knime.fsklab.r.client.IRController.RException;

public class RScriptHandler extends ScriptHandler {

  
  public RScriptHandler(RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings) {
    this.internalSettings = internalSettings;
    this.nodeSettings = nodeSettings;
  }
  public RScriptHandler() {
    this.internalSettings = new RunnerNodeInternalSettings();;
    this.nodeSettings = new RunnerNodeSettings();
  }
  
  RController controller;
  @Override
  void simpleOperation() {
    // TODO Auto-generated method stub

  }

  @Override
  void setController(AutoCloseable controller) {
    // TODO Auto-generated method stub
    this.controller = (RController)controller;
    
    
  }

  @Override
  FskPortObject runSnippet(final AutoCloseable controller, final FskPortObject fskObj,
      final FskSimulation simulation, final ExecutionMonitor exec) throws Exception {
    this.controller = (RController)controller;
    final ScriptExecutor executor = new ScriptExecutor(this.controller);

    // Sets up working directory with resource files. This directory needs to be deleted.
    exec.setProgress(0.05, "Add resource files");
    {
      String workingDirectoryString = fskObj.getWorkingDirectory();
      if (!workingDirectoryString.isEmpty()) {
        Path workingDirectory =
            FileUtil.getFileFromURL(FileUtil.toURL(workingDirectoryString)).toPath();
        this.controller.setWorkingDirectory(workingDirectory);
      }
    }

    // START RUNNING MODEL
    exec.setProgress(0.1, "Setting up output capturing");
    executor.setupOutputCapturing(exec);

    // Install needed libraries
    if (!fskObj.packages.isEmpty()) {
      try {
        // Install missing libraries
        LibRegistry libReg = LibRegistry.instance();
        List<String> missingLibs = fskObj.packages.stream().filter(lib -> !libReg.isInstalled(lib))
            .collect(Collectors.toList());

        if (!missingLibs.isEmpty()) {
          libReg.installLibs(missingLibs, exec, LOGGER);
        }
      } catch (RException | REXPMismatchException e) {
        LOGGER.error(e.getMessage());
      }
    }

    exec.setProgress(0.71, "Add paths to libraries");
    this.controller.addPackagePath(LibRegistry.instance().getInstallationPath());

    exec.setProgress(0.72, "Set parameter values");
    LOGGER.info(" Running with '" + simulation.getName() + "' simulation!");
    String paramScript = NodeUtils.buildParameterScript(simulation);
    executor.execute(paramScript, exec);

    exec.setProgress(0.75, "Run models script");
    executor.executeIgnoreResult(fskObj.model, exec);

    exec.setProgress(0.9, "Run visualization script");
    try {
      NodeUtils.plot(internalSettings.imageFile, fskObj.viz, nodeSettings.width,
          nodeSettings.height, nodeSettings.pointSize, nodeSettings.res, executor, exec);

      // Save path of generated plot
      fskObj.setPlot(internalSettings.imageFile.getAbsolutePath());
    } catch (final RException exception) {
      LOGGER.warn("Visualization script failed", exception);
    }

    exec.setProgress(0.96, "Restore library paths");
    this.controller.restorePackagePath();

    exec.setProgress(0.98, "Collecting captured output");
    executor.finishOutputCapturing(exec);

    // END RUNNING MODEL

    // Save workspace
    if (fskObj.workspace == null) {
      fskObj.workspace = FileUtil.createTempFile("workspace", ".R").toPath();
    }
    this.controller.saveWorkspace(fskObj.workspace, exec);
    this.executor = executor;
    // process the return value of error capturing and update error and
    // output views accordingly
    /*if (!executor.getStdOut().isEmpty()) {
      setExternalOutput(getLinkedListFromOutput(executor.getStdOut()));
    }

    if (!executor.getStdErr().isEmpty()) {
      final LinkedList<String> output = getLinkedListFromOutput(executor.getStdErr());
      setExternalErrorOutput(output);
      
      for (final String line : output) {
        if (line.startsWith(ScriptExecutor.ERROR_PREFIX)) {
          throw new RException(line, null);
        }
      }
    }
  
*/    // cleanup temporary variables of output capturing and consoleLikeCommand stuff
    exec.setProgress(0.99, "Cleaning up");
    executor.cleanup(exec);
    return fskObj;
    
  }
  private ScriptExecutor executor;
  public LinkedList<String> getExternalOutput(){
    if (!executor.getStdOut().isEmpty()) {
      return Arrays.stream(executor.getStdOut().split("\\r?\\n")).collect(Collectors.toCollection(LinkedList::new));

    }
    return null;
  }
  public LinkedList<String> getExternalErrorOutput() {
    if (!executor.getStdErr().isEmpty()) {
      return Arrays.stream(executor.getStdErr().split("\\r?\\n")).collect(Collectors.toCollection(LinkedList::new));

    }
    return null;
  }
  
}
