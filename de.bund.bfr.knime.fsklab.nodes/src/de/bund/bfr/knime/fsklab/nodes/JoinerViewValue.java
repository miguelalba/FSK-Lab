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

import java.io.IOException;
import java.util.Random;
import org.apache.commons.lang3.StringEscapeUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.bfr.knime.fsklab.FskPlugin;
import de.bund.bfr.knime.fsklab.JoinRelation;

class JoinerViewValue extends JSONViewContent {

  private static final NodeLogger LOGGER = NodeLogger.getLogger(JoinerViewValue.class);

  // Configuration keys
  private static final String CFG_ORIGINAL_MODEL_SCRIPT = "originalModelScript";
  private static final String CFG_ORIGINAL_VISUALIZATION_SCRIPT = "originalVisualizationScript";
  private static final String CFG_ORIGINAL_MODEL_SCRIPT2 = "originalModelScript2";
  private static final String CFG_ORIGINAL_VISUALIZATION_SCRIPT2 = "originalVisualizationScript2";
  private static final String CFG_MODEL_METADATA = "ModelMetaData";
  private static final String CFG_JOINER_RELATION = "joinRelation";
  private static final String CFG_JSON_REPRESENTATION = "JSONRepresentation";
  private static final String CFG_MODELSCRIPT_TREE = "ModelScriptTree";
  private static final String CFG_FIRST_MODEL_NAME = "firstModelName";
  private static final String CFG_SECOND_MODEL_NAME = "secondModelName";

  private final int pseudoIdentifier = (new Random()).nextInt();
  private final ObjectMapper MAPPER = FskPlugin.getDefault().MAPPER104;

  public String firstModelScript;
  public String secondModelScript;
  public String firstModelViz;
  public String secondModelViz;
  public String modelMetaData;
  public JoinRelation[] joinRelations;
  public String jsonRepresentation;
  public String svgRepresentation;
  public String modelScriptTree;
  public String firstModelName;
  public String secondModelName;
  public String different;
  public String modelType;

  @Override
  public void saveToNodeSettings(NodeSettingsWO settings) {
    settings.addString(CFG_FIRST_MODEL_NAME, firstModelName);
    settings.addString(CFG_SECOND_MODEL_NAME, secondModelName);
    settings.addString(CFG_ORIGINAL_MODEL_SCRIPT, firstModelScript);
    settings.addString(CFG_ORIGINAL_VISUALIZATION_SCRIPT, firstModelViz);
    settings.addString(CFG_ORIGINAL_MODEL_SCRIPT2, secondModelScript);
    settings.addString(CFG_ORIGINAL_VISUALIZATION_SCRIPT2, secondModelViz);

    // Add joinRelations as string
    if (joinRelations != null) {
      try {
        String relationsAsString = MAPPER.writeValueAsString(joinRelations);
        settings.addString(CFG_JOINER_RELATION, relationsAsString);
      } catch (JsonProcessingException err) {
        // do nothing
      }
    }

    settings.addString(CFG_JSON_REPRESENTATION, jsonRepresentation);
    settings.addString(CFG_MODELSCRIPT_TREE, modelScriptTree);
    if (modelMetaData != null) {
      saveSettings(settings, CFG_MODEL_METADATA, modelMetaData);
    }
  }

  @Override
  public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
    firstModelName = settings.getString(CFG_FIRST_MODEL_NAME);
    secondModelName = settings.getString(CFG_SECOND_MODEL_NAME);
    firstModelScript = settings.getString(CFG_ORIGINAL_MODEL_SCRIPT);
    firstModelViz = settings.getString(CFG_ORIGINAL_VISUALIZATION_SCRIPT);
    secondModelScript = settings.getString(CFG_ORIGINAL_MODEL_SCRIPT2);
    secondModelViz = settings.getString(CFG_ORIGINAL_VISUALIZATION_SCRIPT2);
    
    // Read relations as string
    String relationsAsString = settings.getString(CFG_JOINER_RELATION);
    if (relationsAsString != null) {
      try {
        joinRelations = MAPPER.readValue(relationsAsString, JoinRelation[].class);
      } catch (IOException err) {
        // do nothing
      }
    }
    
    jsonRepresentation = settings.getString(CFG_JSON_REPRESENTATION);
    modelScriptTree = settings.getString(CFG_MODELSCRIPT_TREE);
    // load meta data
    if (settings.containsKey(CFG_MODEL_METADATA)) {
      modelMetaData = getEObject(settings, CFG_MODEL_METADATA);
    }
  }

  private static void saveSettings(final NodeSettingsWO settings, final String key,
      final String eObject) {

    try {
      ObjectMapper objectMapper = FskPlugin.getDefault().OBJECT_MAPPER;
      String jsonStr = objectMapper.writeValueAsString(eObject);
      settings.addString(key, jsonStr);
    } catch (JsonProcessingException exception) {
      LOGGER.warn("Error saving " + key);
    }
  }

  private static String getEObject(NodeSettingsRO settings, String key)
      throws InvalidSettingsException {
    String jsonStr = settings.getString(key);
    jsonStr = StringEscapeUtils.unescapeJson(jsonStr);
    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
    return jsonStr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return false;
  }

  @Override
  public int hashCode() {
    return pseudoIdentifier;
  }
}
