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
package de.bund.bfr.knime.fsklab.v2_0.joiner;

import java.io.IOException;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.bfr.knime.fsklab.FskPlugin;
import de.bund.bfr.knime.fsklab.v2_0.JoinRelation;

class JoinerViewValue extends JSONViewContent {

  // Configuration keys
  private static final String CFG_MODEL_METADATA = "ModelMetaData";
  private static final String CFG_MODELS_DATA = "JoinerModelsData";
  private static final String CFG_JOINER_RELATION = "joinRelation";
  private static final String CFG_JSON_REPRESENTATION = "JSONRepresentation";
  private static final String CFG_MODELSCRIPT_TREE = "ModelScriptTree";
  private static final String CFG_ORIGINAL_VISUALIZATION_SCRIPT = "originalVisualizationScript";

  private final int pseudoIdentifier = (new Random()).nextInt();
  private final ObjectMapper MAPPER = FskPlugin.getDefault().MAPPER104;

  public String modelMetaData;
  public JoinerModelsData joinerModelsData = new JoinerModelsData();
  public JoinRelation[] joinRelations;
  public String jsonRepresentation;
  public String svgRepresentation;
  public String modelScriptTree;
  public String visualizationScript;
  public String different;

  public JoinerViewValue() {
    visualizationScript = "";
  }

  @Override
  public void saveToNodeSettings(NodeSettingsWO settings) {

    // Add joinRelations as string
    try {
      String relationsAsString = MAPPER.writeValueAsString(joinRelations);
      settings.addString(CFG_JOINER_RELATION, relationsAsString);

      String joinerModelsDataAsString = MAPPER.writeValueAsString(joinerModelsData);
      settings.addString(CFG_MODELS_DATA, joinerModelsDataAsString);
    } catch (JsonProcessingException err) {
      // do nothing
    }

    settings.addString(CFG_ORIGINAL_VISUALIZATION_SCRIPT, visualizationScript);
    settings.addString(CFG_JSON_REPRESENTATION, jsonRepresentation);
    settings.addString(CFG_MODELSCRIPT_TREE, modelScriptTree);
    settings.addString(CFG_MODEL_METADATA, modelMetaData);

  }

  @Override
  public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {

    // Read relations as string
    if (settings.containsKey(CFG_JOINER_RELATION)) {
      String relationsAsString = settings.getString(CFG_JOINER_RELATION);
      if (relationsAsString != null) {
        try {
          joinRelations = MAPPER.readValue(relationsAsString, JoinRelation[].class);
          joinerModelsData =
              MAPPER.readValue(settings.getString(CFG_MODELS_DATA), JoinerModelsData.class);
        } catch (IOException err) {
          // do nothing
        }
      }
    }

    jsonRepresentation = settings.getString(CFG_JSON_REPRESENTATION);
    modelScriptTree = settings.getString(CFG_MODELSCRIPT_TREE);
    modelMetaData = settings.getString(CFG_MODEL_METADATA);
    visualizationScript = settings.getString(CFG_ORIGINAL_VISUALIZATION_SCRIPT);

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

  /**
   * Utility check for checking quickly if this view value is empty. A view value with all its
   * mandatory properties not set is considered empty.
   */
  public boolean isEmpty() {
    return StringUtils.isEmpty(modelMetaData) && StringUtils.isBlank(joinerModelsData.firstModelName) ;
  }
}
