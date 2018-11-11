package de.bund.bfr.knime.fsklab.nodes;

import java.nio.file.Path;
import java.util.LinkedList;
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
  public void runScript(String script, ExecutionContext exec, Boolean showErrors) throws Exception {
    ((PythonKernel)controller).execute(script, exec);
    
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
    
    ((PythonKernel)controller).execute(plot_setup, exec);


    // Get image path (with proper slashes)
    final String path = FilenameUtils.separatorsToUnix(internalSettings.imageFile.getAbsolutePath());

    // Gets values
    String pngCommand = "fig.savefig('" + path + "')";
    //if(fskObj.viz.contains(".show()"))
    

    ((PythonKernel)controller).execute(fskObj.viz,exec);
    ((PythonKernel)controller).execute(pngCommand,exec);
    
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getStdErr() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addScriptToArchive() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void cleanup(ExecutionContext exec) throws Exception {
    ((PythonKernel)controller).close();
    
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
    // TODO Auto-generated method stub
    
  }

}
