package de.bund.bfr.knime.fsklab.nodes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.ExecutionContext;
import org.osgi.framework.Bundle;
import de.bund.bfr.knime.fsklab.v1_9.FskPortObject;
import de.bund.bfr.knime.fsklab.v1_9.JoinRelationAdvanced;
import de.bund.bfr.metadata.swagger.Parameter;
import metadata.SwaggerUtil;

/**
 * 
 * @author Thomas Schüler
 *
 * HDF5 is used to store model parameters and their values in a
 * standardized way in order to move parameters across
 * different script languages.
 * The HDFHandler provides methods for each supported script 
 * language to store parameters (input & ouput) in a hdf file.
 *
 */
public abstract class JsonHandler {

  // the hdf file where all model parameters are stored
  public static final String JSON_FILE_NAME = "parameters.json";
  protected static final String JSON_PARAMETERS_NAME = "fsk_parameters";

  protected ScriptHandler scriptHandler;
  protected ExecutionContext exec;
  
  protected JsonHandler(ScriptHandler scriptHandler, ExecutionContext exec) {
    this.scriptHandler = scriptHandler;
    this.exec = exec;
    
    try {
      importLibraries();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  // TODO: conversion command? maybe create a new Class instead of using a map
  public void applyJoinRelation(FskPortObject fskObj,
      List<JoinRelationAdvanced> joinRelationList,
      String suffix) throws Exception {
    
      if(joinRelationList != null ) {
        for (JoinRelationAdvanced joinRelation: joinRelationList) {
          String targetParameter = joinRelation.getTargetParam();
          FskPortObject sourceModel = joinRelation.getModel();
          String sourceParameter = joinRelation.getSourceParam();
          
          for (Parameter param : SwaggerUtil.getParameter(fskObj.modelMetadata)) {
            if (targetParameter.equals(param.getId() + suffix)) {
              String resourcePath = sourceModel.getGeneratedResourcesDirectory().get().getAbsolutePath()
                  .replaceAll("\\\\", "/") + "/";
              String jsonPath = resourcePath + JSON_FILE_NAME;
              loadParametersIntoWorkspace(jsonPath, sourceParameter, param.getId());
              
           // if target parameter is of type FILE, add path to generatedResources to sourceParam
              // This should be safe since source and target parameter must have the same type (if File)
              if(param.getDataType().equals(Parameter.DataTypeEnum.FILE)) {
                addPathToFileParameter(param.getId(), resourcePath);
              }
              String rep = joinRelation.replaceCommand(param.getId());
              applyJoinCommand(param.getId(), joinRelation.replaceCommand(param.getId()));
            }
          }
        }
      }
  }
  
  protected abstract void applyJoinCommand(String parameter, String command) throws Exception;
  
  /**
   * Import the required hdf5 library into workspace using the runscript() metod
   * of the scriptHandler. Installs package if no library is available (R).
   * 
   * @throws Exception if an error occurs running the script.
   */
  protected abstract void importLibraries() throws Exception;
  
  /**
   * Method to save input parameters in the hdf files. This needs
   * to be called before the execution of the model script and after
   * execution of the parameter script.
   * 
   * @param FSKPortObject fsk object containing the parameter names
   * @throws Exception if an error occurs running the script.
   */
  public abstract void saveInputParameters(FskPortObject fskObj)
      throws Exception;
  /**
   * Method to save output parameters in the hdf files. This needs
   * to be called after the execution of the model script.
   * 
   * @param FSKPortObject fsk object containing the parameter names
   * @throws Exception if an error occurs running the script.
   */
  public abstract void saveOutputParameters(FskPortObject fskObj)
      throws Exception;

  /**
   * Method to load input parameters into the workspace of the currently
   * active (to be executed) model.
   * 
   * Work in progress.
   * 
   * @param FSKPortObject fsk object containing the parameter names
   * @throws Exception if an error occurs running the script.
   */
  public abstract void loadParametersIntoWorkspace(String parameterJson, 
      String sourceParam, String targetParam)
      throws Exception;
  
  
  /**
   * @return a string containing script code that adds variables (parameters)
   * to the hdf file in the corresponding script-language.
   * 
   * @param fskObj fsk object containing the parameter names
   * @param classification Parameter.ClassificationEnum either OUTPUT or INPUT 
   */
  protected abstract String compileListOfParameters(FskPortObject fskObj,
      Parameter.ClassificationEnum classification);
 
  /**
   * Creates and returns an HDFHandler instance of the correct language
   * (Python or R).
   * 
   * @param ScriptHandler language appropriate scripthandler 
   * @param ExecutionContext the context of the KNIME workflow
   * @throws Exception if an error occurs running the script.
   */

  public static JsonHandler createHandler(ScriptHandler scriptHandler, ExecutionContext exec)
      throws Exception {

    final JsonHandler handler;

    if (scriptHandler instanceof PythonScriptHandler) {
      handler = new PythonJsonHandler(scriptHandler, exec);
    } else return new RJsonHandler(scriptHandler, exec);
    
    return handler;
  }
  
  /**
   *  If a FILE parameter is overwritten with another value (file) because of joining,
   *  the new file will be located in the generatedResource folder of the other model.
   *  This method concatenates the path to the parameter value.
   *  
   *  Assume, inFile <- "myFile.csv" will be overwritten with "fileOfModel1.csv", then:
   *    inFile <- "fileOfModel1.csv"
   *  will become
   *    inFile <- "/path/to/generatedResourceOfModel1/fileOfModel1.csv"
   *    
   *  
   * @param parameter
   * @param path
   * @throws Exception
   */
  protected abstract void addPathToFileParameter(String parameter, String path) 
      throws Exception;
  
  /**
   * 
   * @param path to the appropriate script file
   * @return appropriate R or Python script file (in /data) needed to load parameters
   *  from a hdf file into the current workspace
   * @throws IOException
   * @throws URISyntaxException
   */
  protected File getResource(final String path) throws IOException, URISyntaxException {
    Bundle bundle = Platform.getBundle("de.bund.bfr.knime.fsklab.nodes");
    URL fileURL = bundle.getEntry(path);
    URL resolvedFileURL = FileLocator.toFileURL(fileURL);
    URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
    return new File(resolvedURI);
  }
  
}
