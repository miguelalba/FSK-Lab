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
/**
 * Simple Factory that creates a concrete instance of abstract type ScriptHandler.
 *  
 * 
 * @author Thomas Schueler
 *
 */
public class ScriptHandlerFactory {
  
  /**
   * 
   * @param script_type The language with which the a script is written
   * @return instance of a ScriptHandler subclass
   */
  public static ScriptHandler createHandler(String script_type) {

    // R is default if script_type is emtpy:
    if(script_type == null)
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("r"))
      return new RScriptHandler();
    if(script_type.toLowerCase().startsWith("py"))
      return new PythonScriptHandler();
    
 
    return new RScriptHandler();
  }
}
