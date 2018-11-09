package de.bund.bfr.knime.fsklab.nodes;

import java.util.LinkedList;
import org.knime.core.node.ExecutionMonitor;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions;
import de.bund.bfr.knime.fsklab.FskPortObject;
import de.bund.bfr.knime.fsklab.FskSimulation;

public class PythonScriptHandler extends ScriptHandler {

  
  @Override
  void simpleOperation() throws Exception {

  }

  @Override
  void setController(AutoCloseable controller) {
    // TODO Auto-generated method stub
    
  }


  @Override
  FskPortObject runSnippet(AutoCloseable controller, FskPortObject fskObj, FskSimulation simulation,
      ExecutionMonitor exec) throws Exception {
    
    return fskObj;
  }

  @Override
  LinkedList<String> getExternalOutput() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  LinkedList<String> getExternalErrorOutput() {
    // TODO Auto-generated method stub
    return null;
  }

}
