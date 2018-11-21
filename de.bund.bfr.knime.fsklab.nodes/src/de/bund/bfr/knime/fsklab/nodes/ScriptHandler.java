/*
 ***************************************************************************************************
 * Copyright (c) 2017 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 *************************************************************************************************
 */
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
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.util.FileUtil;
import org.knime.python2.kernel.PythonKernel;
import de.bund.bfr.fskml.FSKML;
import de.bund.bfr.knime.fsklab.CombinedFskPortObject;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;
import de.bund.bfr.knime.fsklab.JoinRelation;
import de.bund.bfr.knime.fsklab.r.client.RController;
import de.bund.bfr.knime.fsklab.r.client.IRController.RException;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import metadata.Parameter;
import metadata.ParameterClassification;

/**
 * Abstract class defining methods to handle external script code.
 * More specifically, it defines:
 * <ul>
 * <li>setting up the working directory in which the script code is executed </li>
 * <li>setting up and finishing the output-capturing of executed script code </li>
 * <li>setting up the way in which Eclipse and the script interpreter communicate  </li>
 * <li>running a script</li>
 * <li>installing libraries</li>
 * <li>building the parameter script</li>
 * <li>saving an image to a file</li>
 * <li>restoring the library of the interpreter to its default state</li>
 * <li>storing the workspace in a file </li>
 * <li>getting the standard output and standard error output </li>
 * <li>getting the file extension based on the language used in the script files </li>
 * <li>getting the script command to determine the version of a package according to the specific language </li>
 * </ul>
 * Derived classes must overwrite all of the methods, however, certain ones can be left empty based on the language requirements
 *
 * @author Thomas Schueler, Federal Institute for Risk Assessment
 */
public abstract class ScriptHandler {
  
  /**
   * This template method runs a snippet of script code. It does not save the stdOutput or the stdErrOutput.
   * After running this method, "cleanup" has to be called in order to close the communication with the script-interpreter.
   * 
   * 
   * @param fskObj A {@link FSKPortObject} containing the model scripts
   * @param simulation Part of {@link FSKPortObject} which contains the parameter list of the chosen simulation
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @param LOGGER A {@link NodeLogger} that keeps track of the progress during the execution of the node
   * @param internalSettings internal settings to store the image file of the plot
   * @param nodeSettings   settings of the node containing the dimensions of the output image 
   * @throws Exception
   */
  public final void runSnippet(final FskPortObject fskObj,
      final FskSimulation simulation,
      final ExecutionContext exec,
      NodeLogger LOGGER,
      final RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings) throws Exception {



    // Sets up working directory with resource files. This directory needs to be deleted.
    exec.setProgress(0.05, "Add resource files");
    {
      String workingDirectoryString = fskObj.getWorkingDirectory();
      if (!workingDirectoryString.isEmpty()) {
        Path workingDirectory =
            FileUtil.getFileFromURL(FileUtil.toURL(workingDirectoryString)).toPath();
        setWorkingDirectory(workingDirectory);
      }
    }

    // START RUNNING MODEL
    exec.setProgress(0.1, "Setting up output capturing");

    setupOutputCapturing(exec);
    
    // Install needed libraries
    if (!fskObj.packages.isEmpty()) {

      installLibs(fskObj, exec, LOGGER);
    
    }

    exec.setProgress(0.71, "Add paths to libraries");

  
    exec.setProgress(0.72, "Set parameter values");
    LOGGER.info(" Running with '" + simulation.getName() + "' simulation!");
    String paramScript = buildParameterScript(simulation);

    runScript(paramScript, exec, true);

    exec.setProgress(0.75, "Run models script");

    runScript(fskObj.model, exec, false);
    
    exec.setProgress(0.9, "Run visualization script");
    try {

      plotToImageFile(internalSettings, nodeSettings, fskObj, exec);
      // Save path of generated plot
      fskObj.setPlot(internalSettings.imageFile.getAbsolutePath());
    } catch (final RException exception) {
      LOGGER.warn("Visualization script failed", exception);
    }

    exec.setProgress(0.96, "Restore library paths");
    
    restoreDefaultLibrary();

    exec.setProgress(0.98, "Collecting captured output");
    
    finishOutputCapturing(exec);
    saveWorkspace(fskObj, exec);
  }
  
  /**
   * Set the directory in which the interpreter can temporarily save data while executing the script 
   * @param workingDirectory The directory in which the script code is executed
   * @throws Exception if an error occurs accessing the directory
   */
  abstract void setWorkingDirectory(Path workingDirectory)throws Exception;
  /**
   * Needed if the interpreter requires specific code necessary for starting output capturing.
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception 
   */
  abstract void setupOutputCapturing(ExecutionContext exec) throws Exception;
  
  /**
   * Needed if interpreter requires specific code for retrieving captured output 
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception
   */
  abstract void finishOutputCapturing(ExecutionContext exec) throws Exception;
  
  /**
   * Set up the way in which Eclipse and the script interpreter communicate. 
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception
   */
  public abstract void setController(ExecutionContext exec) throws Exception;
  public abstract String[] runScript(String script,
      ExecutionContext exec,
      Boolean showErrors)throws Exception;
  
  /**
   * install library packages necessary for running the scripts 
   * @param fskObj A {@link FSKPortObject} containing the model scripts
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @param LOGGER A {@link NodeLogger} that keeps track of the progress during the execution of the node 
   * @throws Exception
   */
  abstract void installLibs(final FskPortObject fskObj,ExecutionContext exec,NodeLogger LOGGER)throws Exception;
  
  /**
   * 
   * @param simulation Part of {@link FSKPortObject} which contains the parameter list of the chosen simulation
   * @return A script in the concrete language containing a list of parameters and their values (e.g. "x <- 1.0 \n id <- 'model1'")
   */
  abstract String buildParameterScript(final FskSimulation simulation);

  /**
   * 
   * @param internalSettings internal settings to store the image file of the plot
   * @param nodeSettings   settings of the node containing the dimensions of the output image 
   * @param fskObj A {@link FSKPortObject} containing the model scripts
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception
   */
  abstract void plotToImageFile(final RunnerNodeInternalSettings internalSettings,
      RunnerNodeSettings nodeSettings,
      final FskPortObject fskObj,  
      ExecutionContext exec) throws Exception;
      
  /**
   * Restore library trees to the default library.
   * @throws Exception
   */
  abstract void restoreDefaultLibrary()throws Exception;

  /**
   * Save the workspace in the session to a file linked to by a path in the {@link FSKPortObject.workspace}.
   * 
   * @param fskObj A {@link FSKPortObject} containing the model scripts
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception
   */
  abstract void saveWorkspace(final FskPortObject fskObj,ExecutionContext exec)throws Exception;
  
  /**
   * 
   * @return The output generated by the last {@link #runScript(String, ExecutionContext)} call.
   */
  public abstract String getStdOut();

  /**
   * 
   * @return The error output generated by the last {@link #runScript(String, ExecutionContext)} call.
   */
  public abstract String getStdErr();

  /**
   * Cleanup temporary variables, which were created during output capturing and execution
   * @param exec KNIME way of managing storage and information output during the current NodeModels execution
   * @throws Exception
   */
  public abstract void cleanup(ExecutionContext exec)throws Exception;
  
  /**
   * Returns the script code to have the interpreter acquire the version number of a package
   * @param pkg_name The name of the package
   * @return The command have the interpreter return the version of the package  (e.g. in R: "packageDescription(\"RServe")$Version" 
   */
  public abstract String getPackageVersionCommand(String pkg_name);
  
  /**
   * Returns the script code to have the interpreter acquire the version number of all available packages (e.g. in R: available.packages(...))
   * @param pkg_names a list of packages whose version need to be determined
   * @return the command to have the interpreter return a list of available packages
   */
  public abstract String getPackageVersionCommand(List<String> pkg_names);
  
  

  /**
   * 
   * @return The file extension of script files of the specific language (e.g. in R: return value would be "r" )
   */
  public abstract String getFileExtension();

 
  
}
