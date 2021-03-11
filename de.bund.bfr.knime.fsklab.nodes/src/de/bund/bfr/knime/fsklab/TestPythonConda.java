package de.bund.bfr.knime.fsklab;

import java.util.Optional;
import javax.swing.event.ChangeEvent;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatus;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatusListener;
import org.knime.python2.config.CondaEnvironmentsConfig;

public class TestPythonConda {
  private String packet;
  
  // True if receiver should wait
  // False if sender should wait
  private boolean transfer = true;
  /**
   * Initialized when the {@link #switchToStartingOrRetryingState() create button is clicked}.
   */
  private volatile CondaEnvironmentCreationStatus m_status;

  /**
   * Initialized by {@link #registerExternalHooks()}.
   */
  private CondaEnvironmentCreationStatusListener m_statusChangeListener;

  private volatile boolean m_environmentCreationTerminated = false;
  final CondaEnvironmentsConfig condaEnvironmentsConfig = new CondaEnvironmentsConfig();
  private FskCondaEnvironmentCreationObserver m_environmentCreator;


  /**
   * May be followed by finished, canceled, or failed state.
   */
  private void switchToStartingOrRetryingState() {
    // If retrying.
    try {
      m_environmentCreationTerminated = false;

      //unregisterExternalHooks();


      String environmentName = "fsk_py3_env1";

      m_environmentCreator = new FskCondaEnvironmentCreationObserver(PythonVersion.PYTHON3,
          condaEnvironmentsConfig.getCondaDirectoryPath());
      m_status = new CondaEnvironmentCreationStatus();

      registerExternalHooks();

      Optional<String> pathToEnvFile =
          Optional.of("C:/Users/thsch/OneDrive/Dokumente/CodeAndScripts/fsk_py3_env1.yml");
      m_environmentCreator.startEnvironmentCreation(environmentName, m_status, pathToEnvFile);
   
    }finally {
      unregisterExternalHooks();
  }
       


  }

  /**
   * May be followed by starting state ("retry") or no state (terminal state).
   */
  private void switchToFailedState() {
    m_environmentCreationTerminated = true;
  }

  /**
   * Terminal state.
   */
  private void switchToFinishedState() {
    m_environmentCreationTerminated = true;
  }

  /**
   * Terminal state.
   */
  private void switchToCanceledState() {
    m_environmentCreationTerminated = true;
  }

  private void registerExternalHooks() {
    
    m_status.getProgress().addChangeListener(this::updateProgress);
    m_status.getErrorLog().addChangeListener(this::updateErrorLog);
    m_statusChangeListener = new CondaEnvironmentCreationStatusListener() {

      @Override
      public void condaEnvironmentCreationStarting(final CondaEnvironmentCreationStatus status) {
        // no-op
      }

      @Override
      public void condaEnvironmentCreationFinished(final CondaEnvironmentCreationStatus status,
          final String createdEnvironmentName) {
        if (status == m_status) {
          switchToFinishedState();
        }
      }

      @Override
      public void condaEnvironmentCreationCanceled(final CondaEnvironmentCreationStatus status) {
        if (status == m_status) {
          switchToCanceledState();
        }
      }

      @Override
      public void condaEnvironmentCreationFailed(final CondaEnvironmentCreationStatus status,
          final String errorMessage) {
        if (status == m_status) {
          switchToFailedState();
        }
      }
    };
    // Prepend to close dialog before installation tests on the preference page are triggered.
    m_environmentCreator.addEnvironmentCreationStatusListener(m_statusChangeListener, true);
  }


  private void unregisterExternalHooks() {
    if (m_status != null) {
      
      m_status.getProgress().removeChangeListener(this::updateProgress);
      m_status.getErrorLog().removeChangeListener(this::updateErrorLog);
    }
    m_environmentCreator.removeEnvironmentCreationStatusListener(m_statusChangeListener);
  }
  
  
  private void updateProgress(@SuppressWarnings("unused") final ChangeEvent e) {
    final int progress = m_status.getProgress().getIntValue();
    System.out.println("update " + progress);
    
}

private void updateErrorLog(@SuppressWarnings("unused") final ChangeEvent e) {
   
}
  public static void main(String[] args) {
    TestPythonConda test = new TestPythonConda();
    test.switchToStartingOrRetryingState();
    System.out.println("done");
  }

}
