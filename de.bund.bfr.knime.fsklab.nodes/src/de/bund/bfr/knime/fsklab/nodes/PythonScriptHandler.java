package de.bund.bfr.knime.fsklab.nodes;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
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
  
  public PythonScriptHandler() {
    fileExtention = "py";
  }

  @Override
  FskPortObject runSnippet(AutoCloseable controller, FskPortObject fskObj, FskSimulation simulation,
      ExecutionMonitor exec) throws Exception {
    
    return fskObj;
  }

  

  @Override
  public void setController(ExecutionContext exec) throws Exception {
    
    PythonKernelOptions m_kernelOptions = new PythonKernelOptions();
    
    controller = new PythonKernel(m_kernelOptions);
    
  }

  @Override
  public String[] runScript(String script, ExecutionContext exec, Boolean showErrors) throws Exception {
    
    String[] output =((PythonKernel)controller).execute(script.replaceAll("<-", "="), exec);
    std_out += output[0] + "\n";
    std_err += output[1] + "\n";
    return output;
    
  }

  @Override
  public void installLibs(FskPortObject fskObj, ExecutionContext exec, NodeLogger LOGGER)
      throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String buildParameterScript(FskSimulation simulation) {
    String paramScript = NodeUtils.buildParameterScript(simulation);
    paramScript = paramScript.replace("<-","=");
    return paramScript;
  }

  @Override
  public void plotToImageFile(RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings, FskPortObject fskObj, ExecutionContext exec)
      throws Exception {
    
    String plot_setup = "import matplotlib\n" + 
        "matplotlib.use('Agg')";
    
    String [] output = ((PythonKernel)controller).execute(plot_setup, exec);
    std_out += output[0] + "\n";
    std_err += output[1] + "\n";


    // Get image path (with proper slashes)
    final String path = FilenameUtils.separatorsToUnix(internalSettings.imageFile.getAbsolutePath());

    // Gets values
    String pngCommand = "fig.savefig('" + path + "')";
    //if(fskObj.viz.contains(".show()"))
    

    output = ((PythonKernel)controller).execute(fskObj.viz,exec);
    std_out += output[0] + "\n";
    std_err += output[1] + "\n";
    output = ((PythonKernel)controller).execute(pngCommand,exec);
    std_out += output[0] + "\n";
    std_err += output[1] + "\n";
    
    
  }

  @Override
  public void restoreDefaultLibrary() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveWorkspace(FskPortObject fskObj, ExecutionContext exec) throws Exception {
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
  public void addScriptToArchive() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void cleanup(ExecutionContext exec) throws Exception {
    ((PythonKernel)controller).close();
    std_out = "";
    std_err = "";
  }




  @Override
  public void setWorkingDirectory(Path workingDirectory) throws Exception {
    // TODO Auto-generated method stub
    
  }




  @Override
  public void setupOutputCapturing(ExecutionContext exec) throws Exception {
    // TODO Auto-generated method stub
    
  }




  @Override
  public void finishOutputCapturing(ExecutionContext exec) throws Exception {
 
    
  }

  @Override
  public String getPackageVersionCommand(String pkg_name) {
    
    String command = pkg_name + ".__version__";

    return command;
  }

  @Override
  public String getPackageVersionCommand(List<String> pkg_names) {
    String command ="";
       
    return command;
  }



}
