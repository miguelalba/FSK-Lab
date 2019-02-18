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

  
   
   public RScriptHandler() {
     fileExtention = "r";
   }
   
   public void setController( ExecutionContext exec) throws Exception {
    
    this.controller = new RController();
    
    this.executor = new ScriptExecutor((RController)controller);
    
  }
  
  

  public void setWorkingDirectory(Path workingDirectory) throws Exception{
      
    ((RController)controller).setWorkingDirectory(workingDirectory);
    
   }
  
  @Override
  public String[] runScript(String script,
      ExecutionContext exec,
      Boolean showErrors) throws Exception{
  
    
    
    if(showErrors) {
      REXP c = executor.execute(script, exec);
      String[] execResult = c.asStrings();
      return execResult;
    }else {
      executor.executeIgnoreResult(script, exec);
    }
    return null;
    
  }
  
  
  
  @Override
  public void installLibs(final FskPortObject fskObj,
      ExecutionContext exec,
      NodeLogger LOGGER)throws Exception {


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
    ((RController)controller).addPackagePath(LibRegistry.instance().getInstallationPath());

  }
  @Override
  public String buildParameterScript(final FskSimulation simulation) {
    
    return NodeUtils.buildParameterScript(simulation);
    
  }
  @Override
  public void plotToImageFile(final RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings,
      final FskPortObject fskObj,
      ExecutionContext exec)throws Exception {
    
    
    NodeUtils.plot(internalSettings.imageFile, fskObj.viz, nodeSettings.width,
        nodeSettings.height, nodeSettings.pointSize, nodeSettings.res, executor, exec);
   
  }
  @Override
  public void saveWorkspace(final FskPortObject fskObj,ExecutionContext exec)throws Exception {
    
    executor.finishOutputCapturing(exec);
    if (fskObj.workspace == null) {
           
      fskObj.workspace = FileUtil.createTempFile("workspace", ".R").toPath();
    }
    ((RController)controller).saveWorkspace(fskObj.workspace, exec);
  }
  
  @Override
  public void addScriptToArchive() {
    // TODO Auto-generated method stub
    
  }

  @Override
  FskPortObject runSnippet(AutoCloseable controller, FskPortObject fskObj, FskSimulation simulation,
      ExecutionMonitor exec) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }



  @Override
  public void restoreDefaultLibrary()throws Exception {
    ((RController)controller).restorePackagePath();
    
  }

  @Override
  public String getStdOut() {

    return executor.getStdOut();
  }

  @Override
  public String getStdErr() {
    return executor.getStdErr();
    
  }
  public void cleanup(ExecutionContext exec)throws Exception {
    executor.cleanup(exec);
  }


  @Override
  public void setupOutputCapturing(ExecutionContext exec) throws Exception {
    executor.setupOutputCapturing(exec);
    
  }


  @Override
  public void finishOutputCapturing(ExecutionContext exec) throws Exception {
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

 
}
