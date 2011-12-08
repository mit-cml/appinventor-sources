// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblockutil.CDeviceSelector;
import openblocks.codeblockutil.PhoneCommIndicator;
import openblocks.renderable.RenderableBlock;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

/**
 * Handles communication between the blocks editor and the phone.
 * @author halabelson@google.com (Hal Abelson)
 */
public class PhoneCommManager {

  private static final boolean DEBUG = true;

  // The port for communicating with the phone for the ReplCommController
  // Note: This should reference the same definition as in ReplCommController.
  public static final int REPL_COMMUNICATION_PORT = 9997;

  // procedure (macro) defined in runtime.scm for transforming ReplCommController input
  // before evaluation
  private static final String RUNTIME_REPL_COMMUNICATION_INPUT_WRAPPER = "process-repl-input";

  private AndroidController androidController;

  // This separates components of the message that is returned from the phone.
  // used to indicate the block ID in REPL interactions with the phone
  // This is used in a regexp match, so  be wary of changing it to include
  // regexp metacharacters.
  // You can change these punctuation marks for Repl messages, although I can't
  // imagine why. But don't even think about using "&" or "\" in any of them!
  // public for testing
  public static final String REPL_OPEN_BRACKET = "<<";
  public static final String REPL_BLOCK_ID_INDICATOR = ":";
  public static final String REPL_RETURN_TAG_ENDER = "@@";
  public static final String REPL_RESULT_INDICATOR = "==";
  public static final String REPL_CLOSE_BRACKET = ">>";
  public static final String REPL_SUCCESS = "Success";
  public static final String REPL_FAILURE = "Failure";
  private static final String REPL_CONFIRMATION = "Confirmation";
  public static final String REPL_PROJECT_LOADING = "Project loading";

  public static final String REPL_ESCAPE = "&";
  public static final String REPL_ENCODED_ESCAPE = REPL_ESCAPE + "0";
  public static final String REPL_ENCODED_OPEN_BRACKET = REPL_ESCAPE + "1";
  public static final String REPL_ENCODED_CLOSE_BRACKET = REPL_ESCAPE + "2";

  private static final String YAIL_NEWLINE = "(newline)";
  private static final String LOCALHOST = "127.0.0.1";

  // This message to runtime.scm establishes the strings used to punctuate
  // messages it sends back here. Check setup-repl-environment carefully to
  // ensure parameters match. Here's how it should start:
  // (define (setup-repl-environment
  //    open-bracket
  //    block-id-indicator
  //    return-tag-ender
  //    success
  //    failure
  //    result-indicator
  //    close-bracket
  //    encoding-map)
  //The list of pairs at the end represent a mapping
  // for encoding responses; the escape string must be at the end.
  // See also postProcessREPLResponse.
  //@VisibleForTesting
  private static final String REPL_STARTUP_STR =
          "(begin (require com.google.youngandroid.runtime) " +
          " (setup-repl-environment" +
          " \"" + REPL_OPEN_BRACKET + "\"" +
          " \"" + REPL_BLOCK_ID_INDICATOR + "\"" +
          " \"" + REPL_RETURN_TAG_ENDER + "\"" +
          " \"" + REPL_SUCCESS + "\"" +
          " \"" + REPL_FAILURE + "\"" +
          " \"" + REPL_RESULT_INDICATOR + "\"" +
          " \"" + REPL_CLOSE_BRACKET + "\"" +
         " \'((\"" + REPL_CLOSE_BRACKET + "\" \"" + REPL_ENCODED_CLOSE_BRACKET + "\")" +
             "(\"" + REPL_OPEN_BRACKET  + "\" \"" + REPL_ENCODED_OPEN_BRACKET  + "\")" +
             "(\""  + REPL_ESCAPE + "\" \"" + REPL_ENCODED_ESCAPE + "\"))))";

  private static final String REPL_COMM_ERROR_MSG =
    "<p>App Inventor is having trouble communicating with the device.\n"
    + "If you've connected a phone, unplug and replug the phone and try again.</p>\n"
    + "<p>If you've started an emulator, you may need to wait longer for\n"
    + "it to boot up.  Or just try to connect again.</p>\n"
    + "<p>If App Inventor still can't connect, you should close and reopen the Blocks\n"
    + "Editor and try again.</p>\n"
    + "<p>If you are using Windows, and the phone has not previously connected to this\n"
    + "computer, then you might need to install a driver for the\n"
    + "phone, as described in the App Inventor setup documentation.</p>\n";

  private PhoneCommIndicator commIndicator;  // Controls the communication status icon & button
  private CDeviceSelector deviceSelector;

  // Assets - directory path must match the path in
  // com.google.appinventor.components.runtime.util.MediaUtil
  private static final String SDCARD_ASSET_DIR = "/sdcard/AppInventor/assets/";
  private volatile ConcurrentMap<String, String> downloadedProjectAssets;

  /*
   * Communication with the phone is performed in a separate thread. phoneSynchronizer
   * provides synchronous and asynchronous methods to queue calls for that
   * thread. All calls on psReplController should be via code running
   * in the phoneSynchronizer thread.
   */
  private final PhoneSynchronizer phoneSynchronizer = new PhoneSynchronizer();
  private volatile DeviceReplCommController psReplController = null;
  private boolean aiCommandSetupOkay = false;

  // These variables help determine the state of the repl comm button and
  // phone status indicator. They may be read and written from multiple threads
  // so they are protected by statusLock.
  private Object statusLock = new Object();
  private volatile boolean sendingToPhone = false;
  private volatile boolean waitingForProjectLoad = false;
  private volatile boolean connectedToPhone = false; // true iff repl running on phone

  public PhoneCommManager() {
    downloadedProjectAssets = new ConcurrentHashMap<String, String>();
  }

  // @VisibleForTesting
  protected void setReplController(DeviceReplCommController replController) {
    psReplController = replController;
  }

  /*
   * Call this before calling initializeReplComm
   */
  public void setAndroidController(AndroidController androidController) {
    this.androidController = androidController;
  }

  /**
   * Set up the "Connect to Device" to start the REPL
   */
  public void initializeReplComm(final CDeviceSelector deviceSelector,
      final PhoneCommIndicator commIndicator) {
    this.commIndicator = commIndicator;
    this.deviceSelector = deviceSelector;
    if (psReplController == null) {
      if (androidController == null) {
        System.out.println("androidController unexpectedly null. Programming error?");
        return;
      }
      WorkspaceControllerHolder.get().getAIDir().checkSetup();
      initReplController();
    }

    deviceSelector.addCallback(new CDeviceSelector.DeviceSelectedCallback() {
      @Override
      public void onDeviceSelected(String device) {
        String prevDevice = psReplController.getSelectedDevice();
        if (prevDevice != null && connectedToPhone()) {
          if (!device.equals(prevDevice)) {
            if (!FeedbackReporter.getConfirmation(
                "The Blocks Editor is currently connected to device " + prevDevice + ".\n"
                + "At most one device can be connected at a time. Do you want to switch to "
                + "device " + device + "?")) {
              return;
            }
          } else {
            if (FeedbackReporter.getConfirmation(
                "The Blocks Editor is already connected to device " + prevDevice + ". \n"
                + "Do you want to restart the app on the device?")) {
              reinitPhoneApp();
            }
            return;
          }
        }
        setConnectedToPhone(false);
        System.out.println("Selecting device " + device);
        if (!psReplController.selectDevice(device)) {
          if (DEBUG) {
            System.out.println("Selected device is no longer attached");
          }
          FeedbackReporter.showErrorMessage(
              "It appears that device " + device + " is no longer available.");
          return;
        }
        if (DEBUG) {
          System.out.println("Creating the REPL controller");
        }
        replControllerCreateAndSendAsync(YAIL_NEWLINE, REPL_CONFIRMATION, new Long(0), false);
      }});
  }

  private void initReplController() {
    setReplController(new DeviceReplCommController(
        LOCALHOST,
        REPL_COMMUNICATION_PORT,
        androidController,
        new DeviceReplCommController.PostProcessor() {
          public void postProcess(String received) {
            setConnectedToPhone(true);
            postProcessREPLResponse(received, System.out);
          }
          public void onFailure(Throwable e) {
            if (connectedToPhone()) {
              setConnectedToPhone(false);
              showReplCommError(e);
              try {
                phoneSynchronizer.sendNow(new Runnable() {
                  @Override
                  public void run() {
                    psResetReplController();
                  }
                });
              } catch (PhoneCommunicationException p) {
                p.printStackTrace();
              }
            } else {
              // We don't show the user the error because this codepath
              // is triggered by them requesting to restart the
              // application with the "reconnect" button.
              System.out.println("Communication failure with phone. Not reporting it to user.");
              e.printStackTrace();
            }
          }
          public void onDisconnect(String device) {
            if (connectedToDevice(device)) {
              if (DEBUG) {
                System.out.println("Disconnecting currently selected device");
              }
              setConnectedToPhone(false);
              setWaitingForProjectLoad(false);
            }
            deviceSelector.removeDevice(device);
          }
          public void onConnect(String device) {
            String deviceString = device;
            deviceSelector.addDevice(deviceString);
          }
        }));
    androidController.setDeviceListener(psReplController);
  }

  private void reinitPhoneApp() {
    if (DEBUG) {
      System.out.println("Re-initializing the phone app");
      // new RuntimeException("Stack info").printStackTrace();
    }
    try {
      phoneSynchronizer.sendEventually(new Runnable() {
        @Override
        public void run() {
          try {
            psReplControllerRestart(true /*send defns*/, true /*restart app*/);
          } catch (IOException e) {
            System.out.println("Error trying to reinit phone app: ");
            e.printStackTrace();
          } catch (ExternalStorageException e) {
            FeedbackReporter.showErrorMessage(e.getMessage());
            System.out.println("Error trying to reinit phone app: ");
            e.printStackTrace();
          } catch (CodeblocksException e) {
            System.out.println("Error trying to reinit phone app: ");
            e.printStackTrace();
          } catch (NoProjectException e) {
            // There is no current project.
            // Just call psResetReplController to kill the starter app.
            psResetReplController();
          } finally {
            updateStatusIndicators();
          }
        }});
    } catch (PhoneCommunicationException e) {
      e.printStackTrace();
    } finally {
      updateStatusIndicators();
    }
  }

  /**
   * Install an application on the phone.
   * Wait synchronously for the result; (safe since this is called directly
   * from a server handler).
   * @param localApkPath the path of the apk on the local machine
   * @param appName the name of the application
   */
  public boolean installApplication(final String localApkPath, final String appName,
      final String packageName) {
    Callable<Boolean> installApplicationCallable = new Callable<Boolean>() {
      @Override
      public Boolean call() {
        try {
          setSendingToPhone(true);
          androidController.androidSyncAndInstallSpecificApplication(localApkPath, appName,
              packageName);
          return true;
        } catch (AndroidControllerException e) {
          FeedbackReporter.showErrorMessage("Couldn't install the app on the phone: \n" +
              e.getMessage());
          e.printStackTrace();
          return false;
        } catch (ExternalStorageException e) {
          FeedbackReporter.showErrorMessage(e.getMessage());
          return false;
        } finally {
          setSendingToPhone(false);
        }
      }
    };

    try {
      return phoneSynchronizer.sendNow(installApplicationCallable);
    } catch (PhoneCommunicationException e) {
      e.printStackTrace();
      updateStatusIndicators();
      return false;
    }
  }

  public boolean connectedToPhone() {
    synchronized(statusLock) {
      return connectedToPhone;
    }
  }

  public boolean connectedToDevice(String device) {
    String currentDevice = androidController.getSelectedDevice();
    if (currentDevice == null || !device.equals(currentDevice)) {
      return false;
    }
    synchronized(statusLock) {
      return connectedToPhone;
    }
  }

  /**
   * Restarts and reinitializes the app on the phone, if we're connected, to
   * prepare for loading in new project definitions.
   *
   * @param clearAssets whether to clear assets
   * @param resetPhone whether to reset the phone app
   */
  public void prepareForNewProject(boolean clearAssets, boolean resetPhone) {
    if (DEBUG) {
      System.out.println("Preparing phone for new project");
    }
    if (clearAssets) {
      downloadedProjectAssets.clear();
    }

    if (resetPhone) {
      /* cancel outstanding tasks waiting for phone, if any. */
      /* TODO(kerr): cancellation needs to be debugged.  disable for now
       * try {
       *  phoneSynchronizer.cancelAllAndWait();
       * } catch (PhoneCommunicationException e) {
       *  System.out.println(e.getMessage());
       *  e.printStackTrace();
       * }
       */
      if (!connectedToPhone()) {
        return;
      }
      reinitPhoneApp();
    }
  }

  /**
   * Calls replControllerCreateAndSend and does not wait for the
   * communication to the phone to finish.
   *
   * replControllerCreateAndSend sends a data string to the REPL, creating
   * a new instance of a working REPL first if necessary.
   *
   * @param data The data string to be sent to the REPL.
   * just sent back to the REPL so we can use this information in processing the response
   * @param purpose a string documenting the purpose of this call
   * @param blockID the block id of the block that this call originates from.  Use 0 if there is
   *        no block (or no single block).
   * @param loading if true, won't try to send project definitions to the phone
   *        if a restart of the app is required. Useful when this is called
   *        to send the project definitions to the phone (so we don't try to
   *        do it again, which will deadlock)
   */
  public void replControllerCreateAndSendAsync(final String data, final String purpose,
                                               final Long blockID, final boolean loading) {
    replControllerCreateAndSend(data, purpose, blockID, loading, true);
  }

  /* See replControllerCreateAndSendAsync for documentation
   */
  private void replControllerCreateAndSend(final String data, final String purpose,
      final Long blockID, final boolean loading, boolean asynchronous) {
    if (psReplController == null) {
      return;
    }

    Runnable replControllerCreateAndSendRunnable = new Runnable() {
      @Override
      public void run() {
        if (DEBUG) {
          System.out.println("********* Actually invoking CreateAndSend");
        }

        // If we might have a working controller, try the send directly.
        if (connectedToPhone()) {
          try {
            psReplControllerSend(data, purpose + REPL_BLOCK_ID_INDICATOR + blockID.toString());
            return;
          } catch (IOException e) {
            if (DEBUG) {
              System.out.println("^^^^^^^^ regular send failed, will try kick: " + e.getMessage());
            }
            setConnectedToPhone(false);
          }
        }

        // The direct send failed, try to start or restart the connection as appropriate.
        try {
          psReplControllerRestart(!loading /* don't send defns to phone if already loading */,
              loading /*restart the app if we're loading*/);
          psReplControllerSend(data, purpose + REPL_BLOCK_ID_INDICATOR + blockID.toString());
          return;
        } catch (IOException e) {
          setConnectedToPhone(false);
          showReplCommError(e);
        } catch (ExternalStorageException e) {
          setConnectedToPhone(false);
          FeedbackReporter.showErrorMessage(e.getMessage());
        } catch (CodeblocksException e) {
          setConnectedToPhone(false);
          FeedbackReporter.showErrorMessage(e.getMessage());
        } catch (NoProjectException e) {
          // There is no current project.
          setConnectedToPhone(false);
          FeedbackReporter.showInfoMessage(FeedbackReporter.NO_PROJECT_MESSAGE);
        }
        // if we get here there was an error and we need to reset
        psResetReplController();
      }
    };

    try {
      if (asynchronous) {
        phoneSynchronizer.sendEventually(replControllerCreateAndSendRunnable);
      } else {
        phoneSynchronizer.sendNow(replControllerCreateAndSendRunnable);
      }
    } catch (PhoneCommunicationException e) {
      e.printStackTrace();
    }
  }

  private void setSendingToPhone(boolean sending) {
    synchronized(statusLock) {
      if (sendingToPhone == sending) return;
      sendingToPhone = sending;
    }
    updateStatusIndicators();
  }

  private void setWaitingForProjectLoad(boolean waiting) {
    synchronized(statusLock) {
      if (waitingForProjectLoad == waiting) return;
      waitingForProjectLoad =  waiting;
    }
    updateStatusIndicators();
  }

  /*
   * Reinitializes the REPL state on the phone. If restartApp is true then
   * the phone app will be forced to restart. If sendProjectDefinitions
   * is true we'll send the project definitions to the phone.
   * (Assumes caller will fix state of wc.replCommButton and commIndicator)
   * REQUIRES: is called from the phone synchronizer queue
   */
  private void psReplControllerRestart(boolean sendProjectDefinitions, boolean restartApp)
      throws IOException, CodeblocksException, NoProjectException, ExternalStorageException {
    psResetReplController();
    /* send this command to the REPL to initialize the evaluation
     * environment on the phone.
     * TODO(halabelson): setup-repl-environment should cause the phone
     * to return an ACK. The procedure in runtime.scm does this, but
     * it's ignored here because post-process-repl response doesn't recognize
     * the ACK as a value to be displayed.
     */
    if (DEBUG) {
      System.out.println("Restarting repl controller (" + sendProjectDefinitions + ")");
    }
    // get the project definitions string, which we will send to the phone
    // immediately after the initialization string
    String projectDefinitions = "";
    if (sendProjectDefinitions) {
      try {
        projectDefinitions = WorkspaceControllerHolder.get().getProjectDefinitionsForRepl();
      } catch (YailGenerationException e) {
        if (DEBUG) {
          System.out.println("Got YailGenerationException when trying to get project definitions");
        }
        throw new CodeblocksException("Problem getting project definitions: "
            + e.getMessage());
      } catch (IOException e) {
        if (DEBUG) {
          System.out.println("Got IOException when trying to get project definitions");
        }
        throw new CodeblocksException("Problem getting project definitions from server: "
            + e.getMessage());
      }
    }
    // Note that these sends can trigger an IOException
    try {
      setSendingToPhone(true);
      psReplController.sendInitial(REPL_STARTUP_STR, restartApp);
    } finally {
      setSendingToPhone(false);
    }
    if (sendProjectDefinitions) {
      // make sure all the assets we need are on the phone before sending
      // the definitions.
      if (DEBUG) {
        System.out.println("Sending project startup definitions");
      }
      psPushAssetsToPhone();
      psReplControllerSend(projectDefinitions, REPL_PROJECT_LOADING + REPL_BLOCK_ID_INDICATOR
          + "0");
    }
    setConnectedToPhone(true);
  }

  // REQUIRES: is called from the phone synchronizer queue
  // expects that replController is in a good state
  private void psReplControllerSend(String data, String returnTag) throws IOException {
    String wrapped = "(" + RUNTIME_REPL_COMMUNICATION_INPUT_WRAPPER + " "
        + data + " \"" + returnTag + "\")";
    /* Kawa's REPL behaves nastily when you give it an input that has linebreaks
     * in it.  It seems to echo those back and spits out a prompt for each new
     * line.So we avoid the multiple prompts by replacing all whitespace with spaces.
     * This will mess up if we want to get results that really do have whitespace
     * in them: We'll see them as spaces.  Can we live with that as a documented
     * misfeature?
     */
    String noWhitespace = wrapped.replaceAll("\\s", " ");
    if (DEBUG) {
      System.out.println("Sending to phone: " + noWhitespace);
    }
    // Note that this send can trigger an IOException
    boolean projectLoading = false;
    try {
      setSendingToPhone(true);
      if (returnTag.startsWith(REPL_PROJECT_LOADING)) {
        setWaitingForProjectLoad(true);
        projectLoading = true;
      }
      psReplController.send(noWhitespace);
    } catch (IOException e) {
      if (projectLoading) {
        setWaitingForProjectLoad(false);
      }
      throw e;
    } finally {
      setSendingToPhone(false);
    }
  }

  /**
   * Adds an asset to the phone synchronously, and returns the result.
   * @param assetPath  the path of the asset on the server
   * @param localAssetPath the path of the asset on the local machine
   */
  public boolean addAsset(final String assetPath, final String localAssetPath) {
    Callable<Boolean> addAssetCallable = new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return psDoAddAsset(assetPath, localAssetPath);
      }
    };
    try {
      return phoneSynchronizer.sendNow(addAssetCallable);
    } catch (PhoneCommunicationException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Schedules an asset to be added to the phone and returns without awaiting
   * the result.
   * @param assetPath  the path of the asset on the server
   * @param localAssetPath the path of the asset on the local machine
   */
  public void addAssetAsync(final String assetPath, final String localAssetPath) {
    Runnable addAssetRunnable = new Runnable() {
      @Override
      public void run() {
        psDoAddAsset(assetPath, localAssetPath);
      }
    };
    try {
      phoneSynchronizer.sendEventually(addAssetRunnable);
    } catch (PhoneCommunicationException e) {
      e.printStackTrace();
    }
  }

  // REQUIRES: is called from the phoneSynchronizer queue
  private boolean psDoAddAsset(String assetPath, String localAssetPath) {
    downloadedProjectAssets.put(assetPath, localAssetPath);
    if (DEBUG) {
      System.out.println("Adding asset " + assetPath);
    }
    final String assetName = assetNameFromAssetPath(assetPath);
    return psPushAssetToPhone(assetName, localAssetPath, false);
  }

  // REQUIRES: is called from the phoneSynchronizer queue
  private boolean psPushAssetToPhone(String assetName, String localAssetPath,
      boolean ignoreConnected) {
    if (ignoreConnected || connectedToPhone()) {
      final String remoteAccessPath = SDCARD_ASSET_DIR + assetName;
      try {
        setSendingToPhone(true);
        if (DEBUG) {
          System.out.println("Trying to push asset " + localAssetPath +
              " to phone at: " + remoteAccessPath);
        }
        androidController.pushFileToDevice(localAssetPath, remoteAccessPath);
        if (DEBUG) {
          System.out.println("Asset pushed to phone at: " + remoteAccessPath);
        }
        return true;
      } catch (AndroidControllerException e) {
        e.printStackTrace();
        FeedbackReporter.showErrorMessage("Error sending media file to the phone: \n" +
            e.getMessage());
        return false;
      } catch (ExternalStorageException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
        return false;
      } finally {
        setSendingToPhone(false);
      }
    } else {
      return true;
    }
  }

  // REQUIRES: run from phone synchronizer queue.
  private void psPushAssetsToPhone() {
    if (DEBUG) {
      System.out.println("Pushing assets to phone...");
    }
    for (ConcurrentMap.Entry<String, String> entry : downloadedProjectAssets.entrySet()) {
      String assetPath = entry.getKey();
      String localAssetPath = entry.getValue();
      final String assetName = assetNameFromAssetPath(assetPath);
      psPushAssetToPhone(assetName, localAssetPath, true /*ignore connect status*/);
    }
    if (DEBUG) {
      System.out.println("...finished pushing assets to phone.");
    }
  }

  private static String assetNameFromAssetPath(String assetPath) {
    if (assetPath.length() == 0) {
      return assetPath;
    }

    int pos = assetPath.lastIndexOf("/");
    if (pos == -1) {
      return assetPath;
    } else {
      return assetPath.substring(pos + 1);
    }
  }

  /* Show the response from the phone, both in the console and in a balloon
   * on the block in the responseTag. It can be either a computed result or an error.
   * TODO(user): Maybe revive special REPL response window?
   * Note that a block id of 0 is accepted and ignored.
   *
   * The phone has a limit to the size of strings it sends back, and some values
   * may be too big. Therefore we need to accumulate strings before passing them on.
   * leftOver holds characters that have arrived from the phone that don't have a
   * complete response yet. Anything preceding a REPL_OPEN_BRACKET is garbage, but we'll
   * report it later.
   */
  private StringBuilder leftOver = new StringBuilder();
  private static final Pattern grossResponseP = Pattern.compile("(?s)(.*?)" + REPL_OPEN_BRACKET +
                                                                "(.*?)" + REPL_CLOSE_BRACKET);
  private static final Pattern fineResponseP = Pattern.compile(
      "(?s)([\\w ]*)" + REPL_BLOCK_ID_INDICATOR +
      "([\\d]*)" + REPL_RETURN_TAG_ENDER +
      "([\\w ]*)" + REPL_RESULT_INDICATOR +
      "(.*)");

  /* The phone/computer protocol
   * At the lowest level there is a USB connection.
   * Above that, there is a tcp connection.
   * Above that, a telnet connection on phone accepts strings
   * Above that, a Kawa interpreter on the phone accepts accepts S-expressions
   * to be evaluated.
   * Most expression are of the form
   * (process-repl-input expression return-tag)
   *
   * This causes the expression to be evaluated to <value> and a string of the
   * form
   *      <<  return-tag @@ outcome == value >>
   * is sent to the computer where outcome is "Success" or "Failure" depending
   * on whether expression returns normally or throws an exception.
   * Return tags should be of the form
   *
   *            purpose : blockID
   *
   *  where purpose gives an idea of what the call was for, and blockID is the
   *  ID of a codeblocks block to which the outcome and value should be sent.
   *
   * These patterns below define comprehensible responses.
   * Responses have the form
   *         << purpose:blockID @@ (Success: value | Failure: message) >>
   * Purposes can be words with spaces. BlockID's can be
   * integers. Values can be anything not containing REPL_OPEN_BRACKET or
   * REPL_CLOSE_BRACKET. The "?" in grossResponseP achieves this final constraint.
   * The phone must encode values coming back so that they can't confuse this
   * parser. runtime.scm replaces "\" with REPL_ENCODED_BACK_SLASH, then
   * replaces REPL_CLOSE_BRACKET with REPL_ENCODED_CLOSE_BRACKET and REPL_OPEN_BRACKET
   * with REPL_ENCODED_OPEN_BRACKET.
   * The code in parseResponse reverses the process.
   */

  /**
   * This method does the breaking up of messages between brackets and
   * passes the innards to parseAndSendResponse to distribute to blocks
   * @returns the number of responses detected
   */
  //@VisibleForTesting
  int postProcessREPLResponse(String received, Appendable error) {
    if (DEBUG) {
      System.out.println("Received from phone: \"" + received + "\"");
    }
    int responsesFound = 0;  // Just for sanity testing
    leftOver.append(received);
    String candidate = leftOver.toString();
    while (true) {
      Matcher completeResponse = grossResponseP.matcher(candidate);
      if (!completeResponse.find()) {
        leftOver.setLength(0);
        leftOver.append(candidate);
        return responsesFound;
      }
      responsesFound++;
      candidate = candidate.substring(completeResponse.end());
      checkNoise(completeResponse.group(1), error);
      parseAndSendResponse(completeResponse.group(2), error);
    }
  }

  private void parseAndSendResponse(String grossResponse, Appendable error)  {
    Matcher match1 = fineResponseP.matcher(grossResponse);
    if (!match1.matches()) {
      try {
        error.append("Garbled response: " + grossResponse);
      } catch (IOException e) {
        // To mollify compiler
      }
    } else {
      PhoneResponse pr =  new PhoneResponse(match1.group(1),
          Long.parseLong(match1.group(2).equals("")
                  ? "0"
                  : match1.group(2)),
                  match1.group(3).equals(REPL_SUCCESS),
                  match1.group(4)
                     .replace(REPL_ENCODED_CLOSE_BRACKET, REPL_CLOSE_BRACKET)
                     .replace(REPL_ENCODED_OPEN_BRACKET, REPL_OPEN_BRACKET)
                     .replace(REPL_ENCODED_ESCAPE, REPL_ESCAPE));
      RenderableBlock rb = RenderableBlock.getRenderableBlock(pr.blockID);
      if (rb != null) {
        rb.showReplResult(pr.purpose, pr.success, pr.message);
        if (DEBUG) {
          System.out.println("Sent to block: " + pr.toString());
        }
      } else if (!pr.success) {  // It's a non-attributable error
        FeedbackReporter.showErrorMessage(pr.message, pr.purpose);
      }
      // see if purpose was project loading and enable repl comm button if so
      if (pr.purpose.equals(REPL_PROJECT_LOADING)) {
        setWaitingForProjectLoad(false);
      }
    }
  }


  void checkNoise(String message, Appendable error) {
    if (!message.matches("(\\n|\\s|#\\|kawa:[\\d]+\\|#)*")) {
      try {
        error.append("Ignored \"" + message + "\"\n");
      } catch (IOException e) {
        // IOException can't occur, but we have to mollify the compiler.
      }
    }
  }

  public class PhoneResponse {
    String purpose;
    long blockID;
    boolean success;
    String message;

    PhoneResponse(String purpose, long blockID, boolean success, String message) {
      this.purpose = purpose;
      this.blockID = blockID;
      this.success = success;
      this.message = message;
    }

    @Override
    public String toString() {
      return purpose +
             REPL_BLOCK_ID_INDICATOR +
             new Long(blockID).toString() +
             REPL_RETURN_TAG_ENDER +
             (success ? REPL_SUCCESS : REPL_FAILURE) +
             REPL_RESULT_INDICATOR +
             message;
    }
  }

  // REQUIRES: is called from the phone synchronizer queue
  /**
   * Reset any existing REPL Controller
   */
  private void psResetReplController() {
    if (!(psReplController == null)) {
      // do some finalization here?
      psReplController.reset();
    }
  }

  private void showReplCommError(Throwable e) {
    //TODO(user) get a character version of usb symbol or insert gif into
    // this message somehow.
    FeedbackReporter.showErrorMessage(REPL_COMM_ERROR_MSG
       + "<p>Detailed error: " + e.getMessage() + "</p>",
    "Trouble connecting to device");
    if (DEBUG) {
      System.out.println ("Communication with REPL failed: " + e);
    }
  }

  private void setConnectedToPhone(boolean newSetting) {
    synchronized(statusLock) {
      if (newSetting == connectedToPhone) return;
      connectedToPhone = newSetting;
      if (DEBUG && SwingUtilities.isEventDispatchThread()) {
        System.out.println("setConnectedToPhone: in eventDispatchThread");
      }
    }
    updateStatusIndicators();
  }

  /**
   * The status indicators give the user an indication of block editor to phone
   * connectivity. The repl comm button has a label that might display
   * one of four possible values ("Connect to phone", "Restart phone app",
   * "Communicating", and "Can't connect"). The button can be enabled,
   * temporarily disabled, or permanently disabled. The 4th label value is only
   * shown when the button is permanently disabled. The comm indicator shows one
   * of three icons, corresponding to "connected" (green), "communicating"
   * (yellow) and "disconnected" (gray). The state of the indicators is set
   * according to the following state variables:
   *
   *    - deviceConnected - true iff the first device to get plugged in is still
   *      plugged in
   *    - sendingToPhone - true while we are in the process of sending commands
   *      or data to the phone
   *    - waitingForProjectLoad - true if we have sent project definitions
   *      to the repl on the phone and are waiting for an acknowledgement that
   *      the project is loaded on the phone
   *    - connectedToPhone - true if we believe that there is a REPL running
   *      on the phone that we can communicate with
   *
   * It is the job of any code that changes the state variables to call
   * updateStatusIndicators to reflect the change in the UI.
   *
   * All of the state variables are read with statusLock held. The update
   * code (including getting the lock) runs on the UI thread.
   */
  public void updateStatusIndicators() {
    // this runs on the UI thread, to avoid conflicting UI updates
    final class statusUpdater implements Runnable {
      @Override
      public void run() {
        boolean phoneCommBusy;
        boolean myConnectedToPhone;
        synchronized(statusLock) {
          phoneCommBusy = waitingForProjectLoad || sendingToPhone;
          myConnectedToPhone = connectedToPhone;
        }
        // comm indicator
        if (phoneCommBusy) { // communicating with phone
          commIndicator.setState(PhoneCommIndicator.IndicatorState.COMMUNICATING);
        } else {
          if (myConnectedToPhone) {  // not communicating with phone and repl app is running
            commIndicator.setState(PhoneCommIndicator.IndicatorState.CONNECTED);
          } else { // not communicating and repl app not running
            commIndicator.setState(PhoneCommIndicator.IndicatorState.DISCONNECTED);
          }
        }
        // connect menu enabled/disabled
        // Note: The device selector menu is created with the initial text
        // "Loading a Project".  This changes to "Connect to Device" when the
        //  menu is first activated.
        if (!phoneCommBusy
            && WorkspaceControllerHolder.get().haveProject()) {
          deviceSelector.setEnabled(true);
        } else {
          deviceSelector.setEnabled(false);
        }
        // connected device
        if (myConnectedToPhone) {
          deviceSelector.setCurrentDevice(psReplController.getSelectedDevice());
        } else {
          deviceSelector.setCurrentDevice(null);
        }
      }
    }
    if (commIndicator == null) {
      // only expect this to be true during testing
      return;
    }
    if (SwingUtilities.isEventDispatchThread()) {
      new statusUpdater().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new statusUpdater());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
}
