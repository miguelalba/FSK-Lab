package de.bund.bfr.knime.fsklab.nodes;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.knime.python2.kernel.PythonKernel;
import de.bund.bfr.fskml.FSKML;
import de.bund.bfr.knime.fsklab.CombinedFskPortObject;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;
import de.bund.bfr.knime.fsklab.JoinRelation;
import de.bund.bfr.knime.fsklab.r.client.RController;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import metadata.Parameter;
import metadata.ParameterClassification;

public abstract class ScriptHandler {
  
  
  AutoCloseable controller;
  String fileExtention = "";
  
  public abstract void setWorkingDirectory(Path workingDirectory)throws Exception;
  public abstract void setupOutputCapturing(ExecutionContext exec) throws Exception;
  public abstract void finishOutputCapturing(ExecutionContext exec) throws Exception;
  public abstract void setController(ExecutionContext exec) throws Exception;
  public abstract String[] runScript(String script,
      ExecutionContext exec,
      Boolean showErrors)throws Exception;
  
  
  public abstract void installLibs(final FskPortObject fskObj,ExecutionContext exec,NodeLogger LOGGER)throws Exception;
  
  public abstract String buildParameterScript(final FskSimulation simulation);

  public abstract void plotToImageFile(final RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings,
      final FskPortObject fskObj,  
      ExecutionContext exec) throws Exception;
      

  public abstract void restoreDefaultLibrary()throws Exception;
  
  public abstract void saveWorkspace(final FskPortObject fskObj,ExecutionContext exec)throws Exception;
  public abstract String getStdOut();
  public abstract String getStdErr();
  public abstract void addScriptToArchive();
  
  public abstract void cleanup(ExecutionContext exec)throws Exception;
  public abstract String getPackageVersionCommand(String pkg_name);
  public abstract String getPackageVersionCommand(List<String> pkg_names);
  
  
//  //protected static final NodeLogger LOGGER = NodeLogger.getLogger("Fskx Runner Node Model");
//  
//  
//  protected RunnerNodeInternalSettings internalSettings;
//
//  protected RunnerNodeSettings nodeSettings;
//  
//  
//  
//  //template methods
//  public final FskPortObject runFskPortObject(FskPortObject fskObj, ExecutionContext exec,
//      AutoCloseable controller) throws Exception{
//    LOGGER.info("Running Model: " + fskObj);
//    if (fskObj instanceof CombinedFskPortObject) {
//      CombinedFskPortObject comFskObj = (CombinedFskPortObject) fskObj;
//      List<JoinRelation> joinRelations = comFskObj.getJoinerRelation();
//      FskPortObject firstFskObj = comFskObj.getFirstFskPortObject();
//      if (firstFskObj instanceof CombinedFskPortObject) {
//        firstFskObj = runFskPortObject(firstFskObj, exec, controller);
//      }
//      FskPortObject secondFskObj = comFskObj.getSecondFskPortObject();
//      // apply join command for complex join
//      if (secondFskObj instanceof CombinedFskPortObject) {
//        FskPortObject embedFSKObject = getEmbedFSKObject((CombinedFskPortObject) secondFskObj);
//        if (joinRelations != null) {
//          List<Parameter> alternativeParams = firstFskObj.modelMath.getParameter().stream()
//              .filter(p -> p.getParameterID().endsWith(JoinerNodeModel.suffix))
//              .collect(Collectors.toList());
//          for (Parameter param : alternativeParams) {
//            // if (!(param.getParameterClassification().equals(ParameterClassification.INPUT)
//            // || param.getParameterClassification().equals(ParameterClassification.CONSTANT))) {
//            String alternativeId = param.getParameterID();
//            String oldId = param.getParameterID().substring(0,
//                param.getParameterID().indexOf(JoinerNodeModel.suffix));
////!!            controller.eval(alternativeId + " <- " + oldId, false);
//            
//            // controller.eval("rm(" + oldId + ")", false);
//            // }
//          }
//          for (JoinRelation joinRelation : joinRelations) {
//            for (metadata.Parameter sourceParameter : firstFskObj.modelMath.getParameter()) {
//
//              if (joinRelation.getSourceParam().getParameterID()
//                  .equals(sourceParameter.getParameterID())) {
//                // override the value of the target parameter with the value generated by the
//                // command
//                for (FskSimulation sim : embedFSKObject.simulations) {
//                  final String embedParametername = joinRelation.getTargetParam().getParameterID();
//                  List<Parameter> params = embedFSKObject.modelMath.getParameter().stream()
//                      .filter(p -> embedParametername.startsWith(p.getParameterID()))
//                      .collect(Collectors.toList());
//
//                  sim.getParameters().put(params.get(0).getParameterID(),
//                      joinRelation.getCommand());
//                }
//
//              }
//            }
//          }
//        }
//
//        secondFskObj = runFskPortObject(secondFskObj, exec, controller);
//      }
//
//
//      LOGGER.info(" recieving '" + firstFskObj.selectedSimulationIndex
//          + "' as the selected simulation index!");
//
//      // get the index of the selected simulation saved by the JavaScript FSK Simulation
//      // Configurator the default value is 0 which is the the default simulation
//      ExecutionContext context = exec.createSubExecutionContext(1.0);
//
//      FskSimulation fskSimulation =
//          firstFskObj.simulations.get(firstFskObj.selectedSimulationIndex);
//      // recreate the INPUT or CONSTANT parameters which cause parameterId conflicts
//      List<Parameter> alternativeParams = firstFskObj.modelMath.getParameter().stream()
//          .filter(p -> p.getParameterID().endsWith(JoinerNodeModel.suffix))
//          .collect(Collectors.toList());
//      for (Parameter param : alternativeParams) {
//        if (param.getParameterClassification().equals(ParameterClassification.INPUT)
//            || param.getParameterClassification().equals(ParameterClassification.CONSTANT)) {
//          // cut out the old Parameter ID
//          String oldId = param.getParameterID().substring(0,
//              param.getParameterID().indexOf(JoinerNodeModel.suffix));
//          // make the old parameter available for the Model script
////!!          controller.eval(oldId + " <- " + param.getParameterValue(), false);
//        }
//      }
//
//      // make a map of file name and its last modification date to observe any changes which
//      // means file overwriting or generating new one
//      String wd1 = firstFskObj.getWorkingDirectory();
//      String wd2 = secondFskObj.getWorkingDirectory();
//
//      Map<String, Long> fileModifacationMap = new HashMap<>();
//      if (!wd1.isEmpty() && !wd2.isEmpty() && !wd1.equals(wd2)) {
//        try (Stream<Path> paths =
//            Files.walk(FileUtil.getFileFromURL(FileUtil.toURL(wd1)).toPath())) {
//          paths.filter(Files::isRegularFile).forEach(currentFile -> {
//            fileModifacationMap.put(currentFile.toFile().getName(),
//                currentFile.toFile().lastModified());
//          });
//        }
//      }
//
//      // run the first model!
//      LOGGER.info("Running Snippet of first Model: " + firstFskObj);
//      firstFskObj = runSnippet(controller, firstFskObj, fskSimulation, context);
//
//      // move the generated files to the working
//      // directory of the second model
//      if (!wd1.isEmpty() && !wd2.isEmpty() && !wd1.equals(wd2)) {
//        Path targetDirectory = FileUtil.getFileFromURL(FileUtil.toURL(wd2)).toPath();
//        try (Stream<Path> paths =
//            Files.walk(FileUtil.getFileFromURL(FileUtil.toURL(wd1)).toPath())) {
//          paths.filter(Files::isRegularFile).forEach(currentFile -> {
//            // move new and modified files
//            Long fileLastModified = fileModifacationMap.get(currentFile.toFile().getName());
//            if (fileLastModified == null
//                || currentFile.toFile().lastModified() != fileLastModified) {
//              try {
//                FileUtils.copyFileToDirectory(currentFile.toFile(), targetDirectory.toFile());
//              } catch (IOException e) {
//                e.printStackTrace();
//              }
//            }
//          });
//        }
//      }
//
//
//      // assign the value of parameters which are causing parameterId conflicts to alternative
//      // Parameter which is (maybe) used later in the joining
//
//      for (Parameter param : alternativeParams) {
//        // if (!(param.getParameterClassification().equals(ParameterClassification.INPUT)
//        // || param.getParameterClassification().equals(ParameterClassification.CONSTANT))) {
//        String alternativeId = param.getParameterID();
//        String oldId = param.getParameterID().substring(0,
//            param.getParameterID().indexOf(JoinerNodeModel.suffix));
////!!       controller.eval(alternativeId + " <- " + oldId, false);
//        // controller.eval("rm(" + oldId + ")", false);
//        // }
//      }
//
//      // apply join command
//      if (joinRelations != null) {
//
//        for (JoinRelation joinRelation : joinRelations) {
//          for (metadata.Parameter sourceParameter : firstFskObj.modelMath.getParameter()) {
//
//            if (joinRelation.getSourceParam().getParameterID()
//                .equals(sourceParameter.getParameterID())) {
//              // override the value of the target parameter with the value generated by the
//              // command
//              for (FskSimulation sim : secondFskObj.simulations) {
//                sim.getParameters().put(joinRelation.getTargetParam().getParameterID(),
//                    joinRelation.getCommand());
//              }
//            }
//          }
//        }
//      }
//
//      // get the index of the selected simulation saved by the JavaScript FSK Simulation
//      // Configurater the default value is 0 which is the the default simulation
//      FskSimulation secondfskSimulation =
//          secondFskObj.simulations.get(secondFskObj.selectedSimulationIndex);
//      LOGGER.info("Running Snippet of second Model: " + secondFskObj);
//      secondFskObj = runSnippet(controller, secondFskObj, secondfskSimulation, context);
//      fskObj.workspace = secondFskObj.workspace;
//
//      return comFskObj;
//    } else {
//      LOGGER.info(
//          " recieving '" + fskObj.selectedSimulationIndex + "' as the selected simulation index!");
//
//
//      // get the index of the selected simulation saved by the JavaScript FSK Simulation
//      // Configurator the default value is 0 which is the the default simulation
//      FskSimulation fskSimulation = fskObj.simulations.get(fskObj.selectedSimulationIndex);
//
//      ExecutionContext context = exec.createSubExecutionContext(1.0);
//
//      fskObj = runSnippet(controller, fskObj, fskSimulation, context);
//
//      return fskObj;
//    }
//      
//    //return runSnippet();
//  }
  
//  public final void plot(RunnerNodeInternalSettings internalSettings,
//      RunnerNodeSettings nodeSettings) {}
//  
  //abstact methods for concrete subclasses
//  abstract void simpleOperation()throws Exception;

  abstract FskPortObject runSnippet(final AutoCloseable controller, final FskPortObject fskObj,
      final FskSimulation simulation, final ExecutionMonitor exec)throws Exception;
  

  //hooks
  void hook() {}
  
  public String getFileExtention() {
    
    
    return fileExtention;
  }
  
  
  //static methods
  public static ScriptHandler createHandler(String script_type) {
 
    if(script_type == null)
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("r"))
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("py"))
      return new PythonScriptHandler();
    return new RScriptHandler();
  }

  public static ArchiveEntry addRScript(final CombineArchive archive, final String script,
      final String filename) throws IOException, URISyntaxException {

    final File file = File.createTempFile("temp","");
    FileUtils.writeStringToFile(file, script, "UTF-8");
//  TODO: automate in some way
    final ArchiveEntry entry = archive.addEntry(file, filename, FSKML.getURIS(1, 0, 12).get("r"));
    file.delete();

    return entry;
  }
  
}







