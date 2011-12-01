// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.youngandroid;

import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.jsonp.ConnectivityListener;
import com.google.appinventor.client.jsonp.JsonpConnection;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.utils.Urls;
import com.google.appinventor.common.youngandroid.YaHttpServerConstants;
import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

import java.util.HashMap;
import java.util.Map;

/**
 * CodeblocksManager manages the client's connection to the Codeblocks JavaWebStart
 * process, and provides methods for communicating from the client to the
 * YaHttpServer (the HTTP server running with codeblocks)
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class CodeblocksManager implements ConnectivityListener {
  // Maintain only one Codeblocks process at a time.
  private CodeblocksConnection conn;
  private boolean unresponsiveConnection;

  private boolean startingCodeblocks;
  private boolean startCanceled;

  private final Map<Long, String> projectPaths = new HashMap<Long, String>();
  private YoungAndroidFormNode currentFormNode;
  private String currentProjectPath;

  private static final int CODEBLOCKS_CONNECTION_RETRY_DELAY_MILLIS = 5000;
  private static final long MAX_TIME_TO_CONNECT_MILLIS = 300000;

  /**
   * Returns the singleton codeblocks manager instance.
   *
   * @return  codeblocks manager instance
   */
  public static CodeblocksManager getCodeblocksManager() {
    return CodeblocksManagerInstanceHolder.INSTANCE;
  }

  private static class CodeblocksManagerInstanceHolder {
    private CodeblocksManagerInstanceHolder() {} // not to be instantiated
    private static final CodeblocksManager INSTANCE = new CodeblocksManager();
  }

  private CodeblocksManager() {
  }

  /**
   * Returns true if we can start codeblocks, false otherwise.
   */
  public boolean canStartCodeblocks() {
    // We can start codeblocks if there is a current project, we aren't already connected to
    // codeblocks, and we aren't already trying to start codeblocks.
    return Ode.getInstance().getCurrentYoungAndroidProjectId() != 0
        && (conn == null || unresponsiveConnection)
        && !startingCodeblocks;
  }

  /**
   * Startup codeblocks. If codeblocks is already running, this method does
   * nothing.
   *
   * <p/>The work to start codeblocks and create a connection is done
   * asynchronously, and it may not have finished by the time the call returns.
   *
   * <p/>When codeblocks is up and we are connected, we will tell it to load the
   * source file corresponding to the current project/file at that time.
   */
  public void startCodeblocks() {
    if (canStartCodeblocks()) {
      if (unresponsiveConnection) {
        // We had a connection to codeblocks, but it is unresponsive.
        // Tell it to close (it might already be closed), and then abandon the
        // connection before starting up a new codeblocks instance.
        conn.removeConnectivityListener(this);
        conn.quit();
        conn = null;
        unresponsiveConnection = false;
      }

      startingCodeblocks = true;
      startCanceled = false;
      updateCodeblocksButton();

      // Before we download the jnlp file, clear the previously saved JSONP connection info.
      Ode.getInstance().getLaunchService().clearJsonpConnectionInfo(
          YaHttpServerConstants.CODEBLOCKS_INFO_FILE_PREFIX, new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          if (startCanceled) {
            startingCodeblocks = false;
            updateCodeblocksButton();
          } else {
            downloadJnlpAndMakeConnection();
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          ErrorReporter.reportError(MESSAGES.startingCodeblocksFailed());
          startingCodeblocks = false;
          updateCodeblocksButton();
        }
      });
    }
  }

  private void downloadJnlpAndMakeConnection() {
    // fire up codeblocks via JavaWebStart. The download command goes from
    // the client to the ODE Server, which responds with the JNLP file
    // that causes the browser to start codeblocks via JavaWebStart.
    // We append a hash of the userid to the file name because we need the
    // jnlp file to be user-specific, while still be cacheable.
    String userId = Ode.getInstance().getUser().getUserId();
    Downloader.getInstance().download(ServerLayout.WEBSTART_JNLP_SERVLET_BASE +
        ServerLayout.WEBSTART_JNLP_PURPOSE_CODEBLOCKS + "/" +
        userId.hashCode());

    final long[] timeToStopTryingToConnect = {
        System.currentTimeMillis() + MAX_TIME_TO_CONNECT_MILLIS};

    // Get the connection info for the codeblocks HTTP server.
    Timer timer = new Timer() {
      @Override
      public void run() {
        Ode.getInstance().getLaunchService().retrieveJsonpConnectionInfo(
            YaHttpServerConstants.CODEBLOCKS_INFO_FILE_PREFIX,
            new AsyncCallback<JsonpConnectionInfo>() {
          @Override
          public void onSuccess(JsonpConnectionInfo connInfo) {
            if (connInfo == null) {
              // The connection info has not been sent to the ODE server yet.
              boolean continueConnecting = true;
              if (System.currentTimeMillis() > timeToStopTryingToConnect[0]) {
                continueConnecting = askIfUserWantsToContinueTryingToConnect();
                if (continueConnecting) {
                  timeToStopTryingToConnect[0] =
                      System.currentTimeMillis() + MAX_TIME_TO_CONNECT_MILLIS;
                }
              }
              // Try again, unless the user has canceled.
              if (startCanceled || !continueConnecting) {
                startingCodeblocks = false;
                updateCodeblocksButton();
              } else {
                schedule(CODEBLOCKS_CONNECTION_RETRY_DELAY_MILLIS); // schedule the timer
              }
            } else {
              conn = new CodeblocksConnection(connInfo, new ClientJsonParser(),
                  Urls.getEscapeQueryParameterFunction());
              conn.addConnectivityListener(CodeblocksManager.this);

              startingCodeblocks = false;
              updateCodeblocksButton();

              // The user may have switched projects/forms while codeblocks was starting.
              // We want the current project/form now.
              YaFormEditor formEditor = Ode.getInstance().getCurrentYoungAndroidFormEditor();
              if (formEditor != null) {
                YoungAndroidFormNode formNode = formEditor.getFormNode();
                loadPropertiesAndBlocks(formNode, null);
              }
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            ErrorReporter.reportError(MESSAGES.startingCodeblocksFailed());
            startingCodeblocks = false;
            updateCodeblocksButton();
          }
        });
      }
    };

    // Schedule the timer for 1 second to give some time for the download to occur.
    timer.schedule(CODEBLOCKS_CONNECTION_RETRY_DELAY_MILLIS);
  }

  private boolean askIfUserWantsToContinueTryingToConnect() {
    return Window.confirm(MESSAGES.continueTryingToConnect());
  }

  /**
   * Returns true if we can cancel codeblocks, false otherwise.
   */
  public boolean canCancelCodeblocks() {
    return startingCodeblocks && !startCanceled;
  }

  /**
   * Requests that we try to cancel the codeblocks launch.
   *
   * <p/>If the JNLP file for codeblocks has already been downloaded,
   * we may not be able to cancel.
   */
  public void cancelCodeblocks() {
    startCanceled = true;
    updateCodeblocksButton();
  }

  /*
   * Given a project id, retrieve and store the project path.

   * @param projectId the project id
   * @param callback a callback to call when the project path has been retrieved.
   */
  private void getProjectPath(final long projectId, final AsyncCallback<Void> callback) {

    // Get the project path from the ODE server.
    Ode.getInstance().getLaunchService().getWebStartProjectPath(projectId,
        new OdeAsyncCallback<String>() {
      @Override
      public void onSuccess(String projectPath) {
        /*
         * Keep the project path for later use. We'd prefer not to get it multiple times both
         * because it involves calls to the server and because we can get a different result each
         * time (since the path is encrypted).
         */
        projectPaths.put(projectId, projectPath);
        callback.onSuccess(null);
      }

      @Override
      public void onFailure(Throwable caught) {
        // Calling super.onFailure will cause the error to be reported.
        super.onFailure(caught);
        callback.onFailure(caught);
      }
    });
  }

  /*
   * Checks whether codeblocks is open and responsive, reports errors if
   * appropriate.
   * If this method returns true, the caller can proceed with sending a request
   * to codeblocks.
   * If this method returns false, the given callback will have been called and
   * the caller does not need to do so.
   *
   * @param codeblocksMustBeOpen true if codeblocks must be open
   * @param callback an optional callback to receive success or failure
   * @Return true if codeblocks is open and responsive, false otherwise
   */
  private boolean checkConnection(boolean codeblocksMustBeOpen, AsyncCallback<Void> callback) {
    if (conn == null) {
      if (codeblocksMustBeOpen) {
        // If codeblocks is not open, tell the user to open it.
        ErrorReporter.reportError(MESSAGES.noCodeblocksConnection());
        if (callback != null) {
          callback.onFailure(new InvocationException(MESSAGES.noCodeblocksConnection()));
        }
      } else {
        // It's ok if codeblocks is not open.
        if (callback != null) {
          callback.onSuccess(null);
        }
      }
      return false;

    } else if (unresponsiveConnection) {
      // Codeblocks was open, but has been closed or is unresponsive.
      if (codeblocksMustBeOpen) {
        // If codeblocks has been closed or is unresponsive, report that.
        ErrorReporter.reportError(MESSAGES.codeblocksConnectionUnresponsive());
        if (callback != null) {
          callback.onFailure(new InvocationException(MESSAGES.codeblocksConnectionUnresponsive()));
        }
      } else {
        // It's ok if codeblocks has been closed or is unresponsive.
        if (callback != null) {
          callback.onSuccess(null);
        }
      }
      return false;
    }

    // Codeblocks is open and responsive.
    return true;
  }

  /**
   * Tell Codeblocks to reload the properties from the ODE server.
   *
   * @param callback an optional callback to pass along to the connection
   */
  public void reloadProperties(AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      OdeLog.log("Telling Codeblocks to reload the properties");
      conn.reloadProperties(
          createErrorReportingCallback(callback, MESSAGES.codeblocksFailedToReloadProperties()));
    }
  }

  /**
   * Tell Codeblocks to load the properties and blocks from the ODE server.
   *
   * @param formNode the YoungAndroidFormNode
   * @param callback an optional callback to pass along to the connection
   */
  public void loadPropertiesAndBlocks(final YoungAndroidFormNode formNode,
      final AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      long projectId = formNode.getProjectId();
      final String projectPath = projectPaths.get(projectId);
      if (projectPath == null) {
        // We don't have the project path for the project.
        // Get the project path from the ODE server before telling codeblocks to load.
        getProjectPath(projectId, new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            loadPropertiesAndBlocks(formNode, callback);
          }
          @Override
          public void onFailure(Throwable caught) {
            // The error has already been reported in getProjectPath.
            if (callback != null) {
              callback.onFailure(caught);
            }
          }
        });

      } else {
        String formPath = projectPath + "/" + formNode.getFileId();
        String assetsPath = projectPath + "/" + YaHttpServerConstants.ASSETS_ZIPFILE;
        String projectName = Ode.getInstance().getProjectManager().getProject(projectId).
            getProjectName();
        OdeLog.log("Telling Codeblocks to load form:\n" + formPath);
        final AsyncCallback<Void> errorReportingCallback = createErrorReportingCallback(callback,
            MESSAGES.codeblocksFailedToLoadPropertiesAndBlocks());
        conn.loadPropertiesAndBlocks(formPath, assetsPath, projectName, new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            currentFormNode = formNode;
            currentProjectPath = projectPath;
            errorReportingCallback.onSuccess(null);
          }
          @Override
          public void onFailure(Throwable caught) {
            errorReportingCallback.onFailure(caught);
          }
        });
      }
    }
  }

  /**
   * Tell Codeblocks to generate its YAIL in order to show problems (like empty sockets) to the
   * user.
   */
  public void generateYail() {
    if (checkConnection(true, null)) {
      OdeLog.log("Telling Codeblocks to generate YAIL");
      conn.generateYail();
    }
  }

  /**
   * Tell Codeblocks to save codeblocks source.
   *
   * @param callback an optional callback to pass along to the connection
   */
  public void saveCodeblocksSource(AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      OdeLog.log("Telling Codeblocks to save codeblocks source");
      conn.saveCodeblocksSource(
          createErrorReportingCallback(callback, MESSAGES.codeblocksFailedToSaveBlocks()));
    }
  }

  /**
   * Tell Codeblocks to clear the workspace.
   *
   * @param callback an optional callback to pass along to the connection
   */
  public void clearCodeblocks(AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      currentFormNode = null;
      OdeLog.log("Clearing blocks editor.");
      conn.clearCodeblocks(
          createErrorReportingCallback(callback, MESSAGES.clearCodeblocksError()));
    }
  }

  /**
   * Returns true if Codeblocks is open.
   */
  public boolean isCodeblocksOpen() {
    return conn != null;
  }

  /**
   * Returns true if Codeblocks is open and responsive.
   */
  public boolean isCodeblocksOpenAndResponsive() {
    return conn != null && !unresponsiveConnection;
  }

  /**
   * Returns the form node that is currently loaded in Codeblocks.
   */
  public YoungAndroidFormNode getCurrentFormNode() {
    return currentFormNode;
  }

  /**
   * Terminates codeblocks if it is running. Called from the
   * {@link com.google.appinventor.client.explorer.youngandroid.ProjectList}
   * when no projects are selected and when ODE is closing.
   */
  public void terminateCodeblocks() {
    if (conn != null) {
      conn.removeConnectivityListener(this);
      conn.quit();
      conn = null;
      unresponsiveConnection = false;
      updateCodeblocksButton();
    }
  }

  /**
   * Sync a single component property with codeblocks.
   *
   * @param componentName the name of the component
   * @param componentType the type of the component
   * @param propertyName the name of the property
   * @param propertyValue the value of the property
   * @param callback an optional callback to pass along to the connection
   */
  public void syncProperty(String componentName, String componentType,
      String propertyName, String propertyValue, AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      OdeLog.log("Syncing property " + componentName + "." + propertyName);
      conn.syncProperty(componentName, componentType, propertyName, propertyValue,
          createErrorReportingCallback(callback, MESSAGES.codeblocksFailedToSyncProperty()));
    }
  }

  /**
   * Tell codeblocks about a newly added asset in a project.
   *
   * @param assetFileId the file id of the asset
   * @param callback an optional callback to pass along to the connection
   *
   */
  public void addAsset(String assetFileId, AsyncCallback<Void> callback) {
    if (checkConnection(false, callback)) {
      String assetPath = currentProjectPath + "/" + assetFileId;
      OdeLog.log("Notifying codeblocks of asset: " + assetPath);
      conn.addAsset(assetPath,
          createErrorReportingCallback(callback, MESSAGES.codeblocksFailedToAddAsset()));
    }
  }

  /**
   * Tell codeblocks to install an application.
   *
   * @param apkFileId the file id of the apk
   * @param appName the application name
   * @param packageName the package name
   * @param callback an optional callback to pass along to the connection
   */
  public void installApplication(String apkFileId, String appName, String packageName,
      AsyncCallback<Void> callback) {
    if (checkConnection(true, callback)) {
      String apkPath = currentProjectPath + "/" + apkFileId;
      OdeLog.log("Telling Codeblocks to install application: " + appName + " with URL " + apkPath);
      conn.installApplication(apkPath, appName, packageName,
          createErrorReportingCallback(callback, MESSAGES.codeblocksFailedToInstallApplication()));
    }
  }

  /**
   * Ask codeblocks whether a phone is connected.
   *
   * @param callback a callback to pass along to the connection
   */
  public void isPhoneConnected(final AsyncCallback<Boolean> callback) {
    AsyncCallback<Void> voidCallback = new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        // This will never be called by checkConnection because we pass true to indicate that
        // codeblocks must be open.
      }
      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }
    };
    if (checkConnection(true, voidCallback)) {
      OdeLog.log("Checking if phone is connected.");
      conn.isPhoneConnected(createErrorReportingBooleanCallback(callback,
          MESSAGES.codeblocksIsPhoneConnectedError()));
    }
  }

  private void updateCodeblocksButton() {
    DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
    // If we are trying to start codeblocks, switch the open codeblocks button to a cancel button.
    // If codeblocks is open, change the button label to say that.
    designToolbar.updateCodeblocksButtonLabel(startingCodeblocks);
    // Enable/disable the button as appropriate.
    designToolbar.updateCodeblocksButton();
  }

  @Override
  public void onConnectivityStatusChange(JsonpConnection connection, boolean status) {
    if (!status) {
      // Codeblocks may have been terminated.
      OdeLog.wlog("Codeblocks is not responding.");
      unresponsiveConnection = true;
    } else {
      // Codeblocks is still alive!
      OdeLog.log("Codeblocks is still alive.");
      unresponsiveConnection = false;
    }
    updateCodeblocksButton();
  }

  /*
   * Creates an AsyncCallback<Void> that wraps the given optional callback.
   * If failure occurs, an appropriate error message will be reported prior to
   * calling onFailure.
   *
   * @param callback an optional callback to receive success or failure
   */
  private AsyncCallback<Void> createErrorReportingCallback(final AsyncCallback<Void> callback,
      final String errorMessage) {
    return new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        if (callback != null) {
          callback.onSuccess(null);
        }
      }
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(errorMessage);
        if (callback != null) {
          callback.onFailure(caught);
        }
      }
    };
  }

  /*
   * Creates an AsyncCallback<Boolean> that wraps the given optional callback.
   * If failure occurs, an appropriate error message will be reported prior to
   * calling onFailure.
   *
   * @param callback an optional callback to receive success or failure
   */
  private AsyncCallback<Boolean> createErrorReportingBooleanCallback(
      final AsyncCallback<Boolean> callback, final String errorMessage) {
    return new AsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean result) {
        if (callback != null) {
          callback.onSuccess(result);
        }
      }
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(errorMessage);
        if (callback != null) {
          callback.onFailure(caught);
        }
      }
    };
  }
}
