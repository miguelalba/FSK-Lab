package de.bund.bfr.knime.fsklab.nodes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.knime.core.node.ExecutionContext;
import de.bund.bfr.knime.fsklab.v1_9.FskPortObject;
import de.bund.bfr.metadata.swagger.Parameter;
import de.bund.bfr.metadata.swagger.Parameter.ClassificationEnum;
import metadata.SwaggerUtil;

public class RHDFHandler extends HDFHandler {

  public RHDFHandler(ScriptHandler scriptHandler, ExecutionContext exec) {
    super(scriptHandler, exec);
  }
  
  @Override
  protected void importHDFLibraries() throws Exception {
    try {
      scriptHandler.runScript("library(hdf5r)", exec, true);
    } catch (Exception e) {
      scriptHandler.runScript("install.packages('hdf5r', type='source', dependencies=TRUE)", exec, true);
      scriptHandler.runScript("library(hdf5r)", exec, true);
    }
  }

  @Override
  public void saveInputParametersToHDF(FskPortObject fskObj) throws Exception {
    StringBuilder script = new StringBuilder();
    
    script.append("file.h5 <- H5File$new('" + HDF_FILE_NAME + "', mode='w')\n");
    script.append("file.h5$create_group('output')\n");
    script.append(compileListOfParameters(fskObj, Parameter.ClassificationEnum.INPUT ));
    script.append("file.h5$close_all()\n");
    
    // remove h5 variable from workspace
    script.append("rm(file.h5)\n");
    
    

    scriptHandler.runScript(script.toString(), exec, false);

  }

  @Override
  public void saveOutputParametersToHDF(FskPortObject fskObj) throws Exception {
    
    StringBuilder script = new StringBuilder();
    
    script.append("file.h5 <- H5File$new('" + HDF_FILE_NAME + "', mode='a')\n");
    script.append("file.h5$create_group('output')\n");
    script.append(compileListOfParameters(fskObj, Parameter.ClassificationEnum.OUTPUT ));
    script.append("file.h5$close_all()\n");
    
    // remove h5 variable from workspace
    script.append("rm(file.h5)\n");
    
    

    scriptHandler.runScript(script.toString(), exec, false);


  }

  @Override
  protected
  String compileListOfParameters(FskPortObject fskObj, ClassificationEnum classification) {
    StringBuilder script = new StringBuilder();
    List<Parameter> paras = SwaggerUtil.getParameter(fskObj.modelMetadata);
    for (Parameter p : paras) {
      if (p.getClassification() == classification) {
        
        script.append("file.h5[['output/" + p.getId() + "']] <- " + p.getId() + "\n");
        
      }
    }
    return script.toString();  
  }
  
  @Override
  public void loadInputParametersFromHDF(FskPortObject fskObj) throws Exception {
    
    StringBuilder script = new StringBuilder();
    script.append("file.h5 <- H5File$new('" + HDF_FILE_NAME + "', mode='r')\n");
        
    File file = getResource("data/loadHDF.r");
    script.append(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    
    scriptHandler.runScript(script.toString(), exec, false);
  }

}