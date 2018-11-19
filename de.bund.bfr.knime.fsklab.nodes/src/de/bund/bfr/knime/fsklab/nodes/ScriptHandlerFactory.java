package de.bund.bfr.knime.fsklab.nodes;

public class ScriptHandlerFactory {
  
  
  public static ScriptHandler createHandler(String script_type) {
    if(script_type == null)
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("r"))
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("py"))
      return new PythonScriptHandler();
    return new RScriptHandler();
  }
}
