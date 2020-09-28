/*
 ***************************************************************************************************
 * Copyright (c) 2020 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.fsklab.v1_9.fskdbview;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.node.AbstractWizardNodeModel;
import de.bund.bfr.knime.fsklab.v1_9.editor.FSKEditorJSNodeFactory;


/**
 * This is an implementation of the node model of the "FSKDBView" node.
 * 
 * This node visualizes FSK models as a HTML table
 */
public class FSKDBViewNodeModel
    extends AbstractWizardNodeModel<FSKDBViewRepresentation, FSKDBViewValue> {
  // Input and output port types
  private static final PortType[] IN_TYPES = {BufferedDataTable.TYPE_OPTIONAL};
  private static final PortType[] OUT_TYPES = {BufferedDataTable.TYPE};

  private static final String VIEW_NAME = new FSKEditorJSNodeFactory().getInteractiveViewName();

  protected FSKDBViewNodeModel() {
    super(IN_TYPES, OUT_TYPES, VIEW_NAME);
  }

  /**
   * The logger is used to print info/warning/error messages to the KNIME console and to the KNIME
   * log file. Retrieve it via 'NodeLogger.getLogger' providing the class of this node model.
   */
  private static final NodeLogger LOGGER = NodeLogger.getLogger(FSKDBViewNodeModel.class);

  /**
   * The settings key to retrieve and store settings shared between node dialog and node model. In
   * this case, the key for the online repository URL String that should be entered by the user in
   * the dialog.
   */
  private static final String KEY_REPOSITORY_LOCATION = "repository_location";

  /**
   * The default online repository URL String.
   */
  private static final String DEFAULT_REPOSITORY_LOCATION = "https://knime.bfr.berlin/backend/";

  /**
   * The settings model to manage the shared settings. This model will hold the value entered by the
   * user in the dialog and will update once the user changes the value. Furthermore, it provides
   * methods to easily load and save the value to and from the shared settings (see: <br>
   * {@link #loadValidatedSettingsFrom(NodeSettingsRO)}, {@link #saveSettingsTo(NodeSettingsWO)}).
   * <br>
   * Here, we use a SettingsModelString as the online repository URL is a String. Also have a look
   * at the comments in the constructor of the {@link FSKDBViewNodeDialog} as the settings models
   * are also used to create simple dialogs.
   */
  private final SettingsModelString m_repositoryLocationSettings =
      createRepositoryLocationSettingsModel();


  /**
   * A convenience method to create a new settings model used for the online repository URL String.
   * This method will also be used in the {@link FSKDBViewNodeDialog}. The settings model will sync
   * via the above defined key.
   * 
   * @return a new SettingsModelString with the key for the online repository URL String
   */
  static SettingsModelString createRepositoryLocationSettingsModel() {
    return new SettingsModelString(KEY_REPOSITORY_LOCATION, DEFAULT_REPOSITORY_LOCATION);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    m_repositoryLocationSettings.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    m_repositoryLocationSettings.loadSettingsFrom(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    m_repositoryLocationSettings.validateSettings(settings);
  }

  @Override
  public FSKDBViewRepresentation createEmptyViewRepresentation() {
    FSKDBViewRepresentation representation = new FSKDBViewRepresentation();
    return representation;
  }

  @Override
  public FSKDBViewValue createEmptyViewValue() {
    return new FSKDBViewValue();
  }

  @Override
  public FSKDBViewValue getViewValue() {
    FSKDBViewValue val;
    synchronized (getLock()) {
      val = super.getViewValue();
      if (val == null) {
        val = createEmptyViewValue();
        String connectedNodeId = getTableId(0);
        val.setTableID(connectedNodeId);
      }
    }
    return val;
  }

  @Override
  public String getJavascriptObjectID() {
    return "de.bund.bfr.knime.fsklab.v1.9.fskdbview.component";
  }

  @Override
  public boolean isHideInWizard() {
    return false;
  }

  @Override
  public void setHideInWizard(boolean hide) {
  }

  @Override
  public ValidationError validateViewValue(FSKDBViewValue viewContent) {
    return null;
  }

  @Override
  public void saveCurrentValue(NodeSettingsWO content) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    return inSpecs;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObject[] performExecute(PortObject[] inObjects, ExecutionContext exec)
      throws Exception {
    PortObject outport = inObjects[0];
    synchronized (getLock()) {
      FSKDBViewRepresentation representation = getViewRepresentation();
      if (outport == null) {
        // if the optional input port is not provided then
        outport = createEmptyTable(exec);
      } else if (representation.getTable() == null) {

        // construct a BufferedDataTable from the input object.
        BufferedDataTable table = (BufferedDataTable) inObjects[0];
        JSONDataTable jsonTable = JSONDataTable.newBuilder().setDataTable(table).build(exec);
        representation.setTable(jsonTable);

        // set the table ID which will be used in broadcasting and receiving event in the component.
        String connectedNodeId = getTableId(0);
        representation.setTableID(connectedNodeId);

        outport = inObjects[0];
      }
    }
    return new PortObject[] {outport};
  }

  /**
   * A helper method for creating an empty table in the case of empty input port.
   * 
   * @param ExecutionContext exec.
   * @return an empty BufferedDataTable instance.
   */
  private BufferedDataTable createEmptyTable(final ExecutionContext exec) {
    BufferedDataContainer container =
        exec.createDataContainer(new DataTableSpecCreator().createSpec());
    container.close();
    BufferedDataTable out = container.getTable();
    return out;
  }

  @Override
  protected void performReset() {
    // TODO perform reset

  }

  @Override
  protected void useCurrentValueAsDefault() {
    // TODO required to preset implementation.

  }
}

