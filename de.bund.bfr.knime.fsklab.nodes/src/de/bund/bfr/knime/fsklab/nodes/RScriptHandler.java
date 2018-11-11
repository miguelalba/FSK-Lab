package de.bund.bfr.knime.fsklab.nodes;


import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.rosuda.REngine.REXPMismatchException;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;
import de.bund.bfr.knime.fsklab.r.client.LibRegistry;
import de.bund.bfr.knime.fsklab.r.client.RController;
import de.bund.bfr.knime.fsklab.r.client.ScriptExecutor;
import de.bund.bfr.knime.fsklab.r.client.IRController.RException;

public class RScriptHandler extends ScriptHandler {

   ScriptExecutor executor;
  
  public void setController( ExecutionContext exec) throws Exception {
    
    this.controller = new RController();
    
    this.executor = new ScriptExecutor((RController)controller);
    
  }
  
  
 
 

//  @Override
//  FskPortObject runSnippet(final AutoCloseable controller, final FskPortObject fskObj,
//      final FskSimulation simulation, final ExecutionMonitor exec) throws Exception {
//    this.controller = (RController)controller;
//    final ScriptExecutor executor = new ScriptExecutor(this.controller);
//
//    // Sets up working directory with resource files. This directory needs to be deleted.
//    exec.setProgress(0.05, "Add resource files");
//    {
//      String workingDirectoryString = fskObj.getWorkingDirectory();
//      if (!workingDirectoryString.isEmpty()) {
//        Path workingDirectory =
//            FileUtil.getFileFromURL(FileUtil.toURL(workingDirectoryString)).toPath();
//        this.controller.setWorkingDirectory(workingDirectory);
//      }
//    }
//
//    // START RUNNING MODEL
//    exec.setProgress(0.1, "Setting up output capturing");
//    //TODO: captureErrorOutput
//    executor.setupOutputCapturing(exec);
//    //TODO: optional: libraries need to be installed : abstract installLibraries
//    //
//    // Install needed libraries
//    if (!fskObj.packages.isEmpty()) {
//      try {
//        // Install missing libraries
//        LibRegistry libReg = LibRegistry.instance();
//        List<String> missingLibs = fskObj.packages.stream().filter(lib -> !libReg.isInstalled(lib))
//            .collect(Collectors.toList());
//
//        if (!missingLibs.isEmpty()) {
//          libReg.installLibs(missingLibs, exec, LOGGER);
//        }
//      } catch (RException | REXPMismatchException e) {
//        LOGGER.error(e.getMessage());
//      }
//    }
//
//    //TODO: abstract setPaths (possibly not necessary)
//    exec.setProgress(0.71, "Add paths to libraries");
//    this.controller.addPackagePath(LibRegistry.instance().getInstallationPath());
//
//    exec.setProgress(0.72, "Set parameter values");
//    LOGGER.info(" Running with '" + simulation.getName() + "' simulation!");
//    //TODO: buildParameterScript
//    String paramScript = NodeUtils.buildParameterScript(simulation);
//    //TODO: runScript
//    executor.execute(paramScript, exec);
//  
//    exec.setProgress(0.75, "Run models script");
//    //TODO: runScript
//    executor.executeIgnoreResult(fskObj.model, exec);
//
//    exec.setProgress(0.9, "Run visualization script");
//    //TODO: abstract plot()-- return absolute path to image
//    try {
//      NodeUtils.plot(internalSettings.imageFile, fskObj.viz, nodeSettings.width,
//          nodeSettings.height, nodeSettings.pointSize, nodeSettings.res, executor, exec);
//
//      // Save path of generated plot
//      fskObj.setPlot(internalSettings.imageFile.getAbsolutePath());
//    } catch (final RException exception) {
//      LOGGER.warn("Visualization script failed", exception);
//    }
//
//    exec.setProgress(0.96, "Restore library paths");
//    //TODO:remove lib paths 
//    this.controller.restorePackagePath();
//
//    exec.setProgress(0.98, "Collecting captured output");
//    //TODO: finish capturing
//    executor.finishOutputCapturing(exec);
//
//    // END RUNNING MODEL
//    
//    //TODO: abstract saveworkspace (for R)  
//    // Save workspace
//    if (fskObj.workspace == null) {
//      
//      fskObj.workspace = FileUtil.createTempFile("workspace", ".R").toPath();
//    }
//    this.controller.saveWorkspace(fskObj.workspace, exec);
//    
//    //END TODO
//    
//    this.executor = executor;
//    // process the return value of error capturing and update error and
//    // output views accordingly
//    //TODO: abstract capturelog
///*    if (!executor.getStdOut().isEmpty()) {
//      setExternalOutput(getLinkedListFromOutput(executor.getStdOut()));
//    }
//
//    if (!executor.getStdErr().isEmpty()) {
//      final LinkedList<String> output = getLinkedListFromOutput(executor.getStdErr());
//      setExternalErrorOutput(output);
//      
//      for (final String line : output) {
//        if (line.startsWith(ScriptExecutor.ERROR_PREFIX)) {
//          throw new RException(line, null);
//        }
//      }
//    }
// */ 
//    // cleanup temporary variables of output capturing and consoleLikeCommand stuff
//    exec.setProgress(0.99, "Cleaning up");
//    //TODO: clean up variables that are no longer needed
//    executor.cleanup(exec);
//    return fskObj;
//    
//  }
//  private ScriptExecutor executor;
//  public LinkedList<String> getExternalOutput(){
//    if (!executor.getStdOut().isEmpty()) {
//      return Arrays.stream(executor.getStdOut().split("\\r?\\n")).collect(Collectors.toCollection(LinkedList::new));
//
//    }
//    return null;
//  }
//  public LinkedList<String> getExternalErrorOutput() {
//    if (!executor.getStdErr().isEmpty()) {
//      return Arrays.stream(executor.getStdErr().split("\\r?\\n")).collect(Collectors.toCollection(LinkedList::new));
//
//    }
//    return null;
//  }
  public void setWorkingDirectory(Path workingDirectory) throws Exception{
      
    ((RController)controller).setWorkingDirectory(workingDirectory);
   }
  
  @Override
  public void runScript(String script,
      ExecutionContext exec,
      Boolean showErrors) throws Exception{
  
    
   
    
    if(showErrors) {
      executor.execute(script, exec);
    }else {
      executor.executeIgnoreResult(script, exec);
    }
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
}
