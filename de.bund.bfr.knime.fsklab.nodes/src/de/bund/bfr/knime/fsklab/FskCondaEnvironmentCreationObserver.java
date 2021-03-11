package de.bund.bfr.knime.fsklab;

import java.util.Optional;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver;

public class FskCondaEnvironmentCreationObserver extends AbstractCondaEnvironmentCreationObserver {
  /**
   * The created instance is {@link #getIsEnvironmentCreationEnabled() disabled by default}.
   *
   * @param environmentPythonVersion The Python version of the Conda environments created by this instance.
   * @param condaDirectoryPath The Conda directory path. Changes in the model are reflected by this instance.
   */
  public FskCondaEnvironmentCreationObserver(final PythonVersion environmentPythonVersion,
      final SettingsModelString condaDirectoryPath) {
      super(environmentPythonVersion, condaDirectoryPath);
  }

  /**
   * @return The default environment name for the next environment created by this instance. Returns an empty string
   *         in case calling Conda failed.<br>
   *         Note that this method makes no guarantees about the uniqueness of the returned name if invoked in
   *         parallel to an ongoing environment creation process.
   */
  public String getDefaultEnvironmentName() {
      return getDefaultEnvironmentName("");
  }
  

  /**
   * Initiates the a new Conda environment creation process. Only allowed if this instance is
   * {@link #getIsEnvironmentCreationEnabled() enabled}.
   *
   * @param environmentName The name of the environment. Must not already exist in the local Conda installation. May
   *            be {@code null} or empty in which case a unique default name is used.
   * @param status The status object that is will be notified about changes in the state of the initiated creation
   *            process. Can also be used to {@link #cancelEnvironmentCreation(CondaEnvironmentCreationStatus) cancel}
   *            the creation process. A new status object must be used for each new creation process.
   */
  public void startEnvironmentCreation(final String environmentName,
      final CondaEnvironmentCreationStatus status,  Optional<String> pathToEnvFile) {
      super.startEnvironmentCreation(environmentName, status, pathToEnvFile);
  }
}
