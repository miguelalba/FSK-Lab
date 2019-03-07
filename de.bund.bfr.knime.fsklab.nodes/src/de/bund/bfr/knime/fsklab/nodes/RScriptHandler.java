package de.bund.bfr.knime.fsklab.nodes;


import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;
import de.bund.bfr.knime.fsklab.r.client.LibRegistry;
import de.bund.bfr.knime.fsklab.r.client.RController;
import de.bund.bfr.knime.fsklab.r.client.ScriptExecutor;
import de.bund.bfr.knime.fsklab.r.client.IRController.RException;

public class RScriptHandler extends ScriptHandler {
  ScriptExecutor executor;
  RController controller;

  public void setController(ExecutionContext exec) throws Exception {
    this.controller = new RController();
    this.executor = new ScriptExecutor(controller);
  }

  void setWorkingDirectory(Path workingDirectory, ExecutionContext exec) throws Exception {
    controller.setWorkingDirectory(workingDirectory);
  }

  @Override
  public String[] runScript(String script, ExecutionContext exec, Boolean showErrors)
      throws Exception {
    if (showErrors) {
      REXP c = executor.execute(script, exec);
      String[] execResult = c.asStrings();
      return execResult;
    } else {
      executor.executeIgnoreResult(script, exec);
    }
    return null;
  }

  @Override
  void installLibs(final FskPortObject fskObj, ExecutionContext exec, NodeLogger LOGGER)
      throws Exception {
    // Install needed libraries
    if (!fskObj.packages.isEmpty()) {
      LibRegistry.instance().install(fskObj.packages);
    }

    exec.setProgress(0.71, "Add paths to libraries");
    controller.addPackagePath(LibRegistry.instance().getInstallationPath());
  }

  @Override
  String buildParameterScript(final FskSimulation simulation) {
    return NodeUtils.buildParameterScript(simulation);
  }

  @Override
  void plotToImageFile(final RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings, final FskPortObject fskObj, ExecutionContext exec)
      throws Exception {
    NodeUtils.plot(internalSettings.imageFile, fskObj.viz, nodeSettings.width, nodeSettings.height,
        nodeSettings.pointSize, nodeSettings.res, executor, exec);
  }

  @Override
  void saveWorkspace(final FskPortObject fskObj, ExecutionContext exec) throws Exception {
    if (fskObj.workspace == null) {
      fskObj.workspace = FileUtil.createTempFile("workspace", ".R").toPath();
    }
    controller.saveWorkspace(fskObj.workspace, exec);
  }

  @Override
  void restoreDefaultLibrary() throws Exception {
    controller.restorePackagePath();
  }

  @Override
  public String getStdOut() {
    return executor.getStdOut();
  }

  @Override
  public String getStdErr() {
    return executor.getStdErr();
  }

  public void cleanup(ExecutionContext exec) throws Exception {
    executor.cleanup(exec);
  }

  @Override
  void setupOutputCapturing(ExecutionContext exec) throws Exception {
    executor.setupOutputCapturing(exec);
  }

  @Override
  void finishOutputCapturing(ExecutionContext exec) throws Exception {
    executor.finishOutputCapturing(exec);
  }

  @Override
  public String getPackageVersionCommand(String pkg_name) {
    String command = "packageDescription(\"" + pkg_name + "\")$Version";
    return command;
  }

  @Override
  public String getPackageVersionCommand(List<String> pkg_names) {
    String command =
        "available.packages(contriburl = contrib.url(c(\"https://cloud.r-project.org/\"), \"both\"))[c('"
            + pkg_names.stream().collect(Collectors.joining("','")) + "'),]";
    return command;
  }

  @Override
  public String getFileExtension() {
    return "r";
  }
}
