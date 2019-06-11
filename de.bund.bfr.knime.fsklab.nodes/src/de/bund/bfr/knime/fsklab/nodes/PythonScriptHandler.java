package de.bund.bfr.knime.fsklab.nodes;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;

public class PythonScriptHandler extends ScriptHandler {
  String std_out = "";
  String std_err = "";
  // controller that communicates with Python Installation
  PythonKernel controller;

  @Override
  public void setController(ExecutionContext exec) throws Exception {
    // automatically receive information about used Python Version (2.7 or 3.x)
    PythonKernelOptions m_kernelOptions = new PythonKernelOptions();
    controller = new PythonKernel(m_kernelOptions);
   
  }

  @Override
  void convertToKnimeDataTable(FskPortObject fskObj, ExecutionContext exec) throws Exception{

  }

  @Override
  public String[] runScript(String script, ExecutionContext exec, Boolean showErrors)
      throws Exception {
    String[] output = controller.execute(script.replaceAll("<-", "="));
    if(!output[0].isEmpty())
      std_out += output[0] + "\n";
    if(!output[1].isEmpty())
      std_err += output[1] + "\n";
    return output;
  }

  @Override
  void installLibs(FskPortObject fskObj, ExecutionContext exec, NodeLogger LOGGER)
      throws Exception {
    // TODO Install neccessary packages
  }

  @Override
  String buildParameterScript(FskSimulation simulation) {
    String paramScript = NodeUtils.buildParameterScript(simulation);
    paramScript = paramScript.replaceAll("<-", "=");
    return paramScript;
  }

  @Override
  void plotToImageFile(RunnerNodeInternalSettings internalSettings, RunnerNodeSettings nodeSettings,
      FskPortObject fskObj, ExecutionContext exec) throws Exception {
    String plot_setup = "import matplotlib.pyplot as fsk_eclipse_plt\n" + "matplotlib.use('Agg')";
    String[] output = controller.execute(plot_setup);
    if(!output[0].isEmpty())
      std_out += output[0] + "\n";
    if(!output[1].isEmpty())
      std_err += output[1] + "\n";
   // Get image path (with proper slashes)
    final String path =
        FilenameUtils.separatorsToUnix(internalSettings.imageFile.getAbsolutePath());
    // Gets values
    
    output = controller.execute(fskObj.viz);
    if(!output[0].isEmpty())
      std_out += output[0] + "\n";
    if(!output[1].isEmpty())
      std_err += output[1] + "\n";

    String pngCommand = "fsk_eclipse_fig = fsk_eclipse_plt.figure(1)\n"+
                        "fsk_eclipse_fig.savefig('" + path + "')\n"+
                         "fsk_eclipse_plt.clf()";
    output = controller.execute(pngCommand);
    if(!output[0].isEmpty())
      std_out += output[0] + "\n";
    if(!output[1].isEmpty())
      std_err += output[1] + "\n";
  }

  @Override
  void restoreDefaultLibrary() throws Exception {
    // TODO remove previously installed packages and restore the library
    // to its default state
  }

  @Override
  void saveWorkspace(FskPortObject fskObj, ExecutionContext exec) throws Exception {
    fskObj.workspace = FileUtil.createTempFile("workspace", ".py").toPath();
  }

  @Override
  public String getStdOut() {
    
    return std_out;
  }

  @Override
  public String getStdErr() {
    return std_err;
  }

  @Override
  public void cleanup(ExecutionContext exec) throws Exception {
    
    std_out = "";
    std_err = "";
  }

  @Override
  void setWorkingDirectory(Path workingDirectory, ExecutionContext exec) throws Exception {
    String cmd = "import os\n";
    String directory = workingDirectory.toString().replaceAll("\\\\", "/");
    
    cmd += "os.chdir('" + directory + "')";
    
    runScript(cmd,exec,false);
    }

  @Override
  void setupOutputCapturing(ExecutionContext exec) throws Exception {
    // can be left empty
  }

  @Override
  void finishOutputCapturing(ExecutionContext exec) throws Exception {
    // can be left empty
  }

  @Override
  public String getPackageVersionCommand(String pkg_name) {
    String command = pkg_name + ".__version__";
    return command;
  }

  @Override
  public String getPackageVersionCommand(List<String> pkg_names) {
    // TODO: find a way to get a list of all available packages
    String command = "";
    return command;
  }

  @Override
  public String getFileExtension() {
    return "py";
  }

  @Override
  public void close() throws Exception {
    controller.close();
    
  }
}