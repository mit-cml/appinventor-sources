// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.youngandroid;

import com.google.appinventor.blockseditor.jsonp.AsyncJsonpRequestHandler;
import com.google.appinventor.blockseditor.jsonp.HttpServer;
import com.google.appinventor.blockseditor.jsonp.HttpServerEventListener;
import com.google.appinventor.blockseditor.jsonp.Util;
import com.google.appinventor.common.youngandroid.YaHttpServerConstants;

import org.json.JSONException;

import openblocks.yacodeblocks.AndroidController;
import openblocks.yacodeblocks.ExternalController;
import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.IWorkspaceController;
import openblocks.yacodeblocks.LoadException;
import openblocks.yacodeblocks.NoProjectException;
import openblocks.yacodeblocks.SaveException;
import openblocks.yacodeblocks.WorkspaceController;
import openblocks.yacodeblocks.WorkspaceControllerHolder;
import openblocks.yacodeblocks.YailGenerationException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Young Android HTTP Server. Wraps an HTTP server around the codeblocks
 * executable so that it can communicate with the ODE server. This program is
 * executed via JavaWebStart on the client machine.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class YaHttpServerMain {

  /**
   * Interface for translating a string URI into a {@link URLConnection}.
   * Injected into {@link YaCodeblocksController} to allow testing mocks.
   */
  public interface ServerConnection {
    HttpURLConnection getConnection(String path) throws MalformedURLException, IOException;
  }

  // All access to workspaceController blocks state should be locked from
  // workspaceControllerLock. Note that we don't currently use
  // workspaceController itself as a synchronization object because
  // some of its methods that we call are themselves synchronized.
  // TODO(sharon): revisit this if we remove the synchronization on
  // WorkspaceController methods.
  private static final Object workspaceControllerLock = new Object();

  private static final String EMPTY_PROPERTIES_ERROR_MESSAGE =
      "Project was unexepectedly empty and "
          + "failed to load.  Please try reloading Blocks Editor.";

  protected final HttpServer server;
  private YaCodeblocksController yaController;
  private YaCodeblocksAndroidController yaAndroidController = null;

  // This gives access to methods in the workspace controller.
  private final WorkspaceController workspaceController;

  private final ServerConnection conn;

  // Keep track of the codeblocks source path and the yail path of the current form.
  private String codeblocksSourcePath;
  private String yailPath;

  public YaHttpServerMain(final HttpServer server, String baseUrl, String path,
      WorkspaceController wc, ServerConnection conn) {
    this.server = server;
    this.workspaceController = wc;
    this.conn = conn;
    this.yaController = new YaCodeblocksController(conn, baseUrl);

    codeblocksSourcePath = "";
    yailPath = "";

    final String phoneAppUrl = baseUrl + path + YaHttpServerConstants.STARTER_PHONEAPP_APK;
    // handle case where phoneAppUrl is malformed or null
    try {
      String phoneAppFile = downloadPhoneAppFromServer(phoneAppUrl);
      this.yaAndroidController = new YaCodeblocksAndroidController(phoneAppFile);
    } catch (MalformedURLException e) {
      reportDownloadError(e.getMessage());
    } catch (FileNotFoundException e) {
      reportDownloadError(e.getMessage());
    } catch (IOException e) {
      reportDownloadError(e.getMessage());
    }

    final String savePortUrl = baseUrl + path + YaHttpServerConstants.CODEBLOCKS_SAVE_PORT;
    server.addHttpServerEventListener(new HttpServerEventListener() {
      @Override
      public void onPortSelected(HttpServer subject, int port) {
        int secret = savePort(savePortUrl, port);
        server.setSecret(secret);
      }
      @Override
      public void onConnectivityStatusChange(HttpServer subject, boolean status) {
        /*
         * TODO(sharon): in some OS/browser combos this seems to happen
         * frequently even though we haven't really lost connection to the
         * browser. At some point we might want to report these in a status bar,
         * but for now we do nothing.
         */
      }
    });

    addHandlers();
  }

  private void reportDownloadError(String exceptionMessage) {
    FeedbackReporter.showErrorMessage(
        "We could not download the starter application from the server in order to install it " +
        "on the <br/>device. This may prevent the \"Connect to Device\" button from working." +
        "<p>This error can occur if you have tried to start the blocks editor with a previously " +
        "downloaded <br/>\".jnlp\" file.</p>" +
        "<p>Make sure that you start the blocks editor by clicking the \"Open the Blocks " +
        "Editor\" button in <br/>the designer and using the \".jnlp\" file that is downloaded by " +
        "that action. <br/><b>Do not try to start the blocks editor with a previously downloaded " +
        "\".jnlp\" file.</b></p>");
    System.out.println("Failed to get application from server and start controller: " +
        exceptionMessage);
  }

  private void addHandlers() {
    server.setHandler(YaHttpServerConstants.GENERATE_YAIL, new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        return Boolean.toString(generateYail());
      }
    });

    server.setHandler(YaHttpServerConstants.LOAD_FORM, new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        String formPropertiesPath = parameters.get(YaHttpServerConstants.FORM_PROPERTIES_PATH);
        String assetsPath = parameters.get(YaHttpServerConstants.ASSET_PATH);
        String projectName = parameters.get(YaHttpServerConstants.PROJECT_NAME);
        return Boolean.toString(loadForm(formPropertiesPath, assetsPath, projectName));
      }
    });

    server.setHandler(YaHttpServerConstants.RELOAD_PROPERTIES,
        new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        return Boolean.toString(reloadProperties());
      }
    });

    server.setHandler(YaHttpServerConstants.CLEAR_CODEBLOCKS, new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        return Boolean.toString(clearCodeblocks());
      }
    });

    server.setHandler(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE, new AsyncJsonpRequestHandler(
        server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        return Boolean.toString(saveCodeblocksSource());
      }
    });

    server.setHandler(YaHttpServerConstants.SYNC_PROPERTY, new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        String componentName = parameters.get(YaHttpServerConstants.COMPONENT_NAME);
        String componentType = parameters.get(YaHttpServerConstants.COMPONENT_TYPE);
        String propertyName = parameters.get(YaHttpServerConstants.PROPERTY_NAME);
        String propertyValue = parameters.get(YaHttpServerConstants.PROPERTY_VALUE);
        return Boolean.toString(syncProperty(componentName, componentType, propertyName,
            propertyValue));
      }
    });

    server.setHandler(YaHttpServerConstants.ADD_ASSET,
        new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        String assetPath = parameters.get(YaHttpServerConstants.ASSET_PATH);
        return Boolean.toString(addAsset(assetPath));
      }
    });

    server.setHandler(YaHttpServerConstants.INSTALL_APPLICATION,
        new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        String apkFilePath = parameters.get(YaHttpServerConstants.APK_FILE_PATH);
        String appName = parameters.get(YaHttpServerConstants.APP_NAME);
        String packageName = parameters.get(YaHttpServerConstants.PACKAGE_NAME);
        return Boolean.toString(installApplication(apkFilePath, appName, packageName));
      }
    });

    server.setHandler(YaHttpServerConstants.IS_PHONE_CONNECTED,
        new AsyncJsonpRequestHandler(server) {
      @Override
      public String getResponseValueAsync(Map<String, String> parameters) throws Throwable {
        return Boolean.toString(isPhoneConnected());
      }
    });
  }

  /**
   * Downloads the phone application apk from the server and saves it into
   * a temporary file.  phoneAppFile will get set to the location where the
   * apk is placed on the local file system.
   *
   * @param  phoneAppUrl URL on server to tell it to download the phone application apk.
   * @return the location of the temp file containing the apk.
   * @throws MalformedURLException (which we don't expect)
   * @throws FileNotFoundException (which we don't expect)
   * @throws IOException (which we also do not expect, but might happen somehow)
   */
  public static String downloadPhoneAppFromServer(String phoneAppUrl)
      throws FileNotFoundException, IOException, MalformedURLException {
    System.out.println("Trying to download phone app apk");
    String phoneAppFile = Util.downloadFile(phoneAppUrl, "phoneApp.apk");
    System.out.println("Phone app apk saved into temp file: " + phoneAppFile);
    return phoneAppFile;
  }

  /**
   * Returns {@code true} if the call to persist the source succeeds in
   * {@link WorkspaceController}, {@code false} if a runtime exception occurs.
   */
  private boolean saveCodeblocksSource() {
    System.out.println("Got call to save codeblocks source.");

    synchronized (workspaceControllerLock) {
      try {
        workspaceController.persistCodeblocksSourceFile(true /* wait for save to complete */);
        return true;
      } catch (SaveException e) {
        FeedbackReporter.showErrorMessage("Blocks Editor source file failed to save.");
        e.printStackTrace();
        return false;
      }
    }
  }

  private boolean clearCodeblocks() {
    codeblocksSourcePath = "";
    yailPath = "";
    yaController.setFormPropertiesPath("");

    synchronized (workspaceControllerLock) {
      System.out.println("Clearing codeblocks.");
      workspaceController.loadFreshWorkspace("", null);
    }
    return true;
  }

  private boolean reloadProperties() {
    System.out.println("Reloading properties");
    try {
      String formProperties = yaController.getFormPropertiesForProject();
      if (formProperties == null) {
        return false;
      }
      synchronized (workspaceControllerLock) {
        if (formProperties.length() > 0) {
          workspaceController.loadProperties(formProperties);
        } else {
          FeedbackReporter.showSystemErrorMessage(EMPTY_PROPERTIES_ERROR_MESSAGE);
          workspaceController.loadFreshWorkspace("", null);
          return false;
        }
      }
    } catch (LoadException e) {
      System.out.println(e.getMessage());
      return false;
    } catch (NoProjectException e) {
      FeedbackReporter.showErrorMessage(e.getMessage());
      return false;
    } catch (IOException e) {
      FeedbackReporter.showSystemErrorMessage("Failed to reload properties: " +
          e.getMessage());
      return false;
    }
    return true;
  }

  private boolean generateYail() {
    /*
     * TODO(user): Get the qualified project name here so that it can be
     * put in the YAIL without parsing it from the path.
     */
    /* This synchronize prevents a save of the .blk file while yail generation is
     * going on, but it doesn't prevent the user from fiddling with the blocks.
     */
    synchronized (workspaceControllerLock) {
      try {
        // We generate YAIL in order to show problems (like empty sockets) to the user.
        // We don't actually send the YAIL to the server.
        workspaceController.wrapProjectYailForAPK(yailPath);
        return true;
      } catch (YailGenerationException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
        return false;
      } catch (NoProjectException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
        return false;
      } catch (JSONException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
        return false;
      } catch (IOException e) {
        FeedbackReporter.showSystemErrorMessage("Failed in Packaging for Phone" + e.getMessage());
        return false;
      }
    }
  }


  private boolean loadForm(String formPropertiesPath,
      String assetsFilePath, String projectName) {
    System.out.println("==== Loading form, project is " + projectName);
    if (formPropertiesPath.equals("") || !formPropertiesPath.endsWith(".scm")) {
      return false;
    }
    String rootName = formPropertiesPath.substring(0, formPropertiesPath.length() - 4);
    codeblocksSourcePath = rootName + ".blk";
    yailPath = rootName + ".yail";
    yaController.setFormPropertiesPath(formPropertiesPath);

    String codeblocksSource;
    String formProperties;
    Map<String, String> assetFiles;
    try {
      codeblocksSource = yaController.getContentsFromServer(codeblocksSourcePath);
      formProperties = yaController.getContentsFromServer(formPropertiesPath);
      assetFiles = yaController.downloadZipFromServer(assetsFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    try {
      synchronized (workspaceControllerLock) {
        if (formProperties.length() > 0) {
          workspaceController.loadSourceAndProperties(codeblocksSourcePath, codeblocksSource,
              formProperties, assetFiles, projectName);
        } else {
          System.out.println("Both source files are empty. Clearing workspace");
          workspaceController.loadFreshWorkspace("", null);
          FeedbackReporter.showSystemErrorMessage(EMPTY_PROPERTIES_ERROR_MESSAGE);
          return false;
        }
      }
    } catch (LoadException e) {
      System.out.println(e.getMessage());
      return false;
    }
    return true;
  }

  private ExternalController getExternalController() {
    return yaController;
  }

  private AndroidController getAndroidController() {
    return yaAndroidController;
  }

  private int savePort(String savePortUrl, int port) {
    try {
      // Send the port back to the ODE server using the savePortUrl.
      HttpURLConnection httpUrlConn = conn.getConnection(savePortUrl);
      // Write the port as the data for the POST request.
      httpUrlConn.setDoOutput(true);
      httpUrlConn.setRequestMethod("POST");
      httpUrlConn.addRequestProperty("Content-Type", "text/plain; charset=utf-8");
      httpUrlConn.connect();
      OutputStreamWriter writer = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
      writer.write(Integer.toString(port));
      writer.close();
      String encoding = httpUrlConn.getContentEncoding();
      if (encoding == null) {
        encoding = "UTF-8";
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          httpUrlConn.getInputStream(), encoding));
      try {
        String line = reader.readLine();
        int secret = Integer.parseInt(line);
        return secret;
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      if (HttpServer.LOG_TO_SYSTEM_OUT) {
        e.printStackTrace(System.out);
      }
    }
    return 0;
  }

  private boolean syncProperty(String componentName, String componentType, String propertyName,
      String propertyValue) {
    System.out.println("Got call to sync the " + componentName + "." + propertyName + " property.");

    synchronized (workspaceControllerLock) {
      return workspaceController.syncProperty(componentName, componentType, propertyName,
          propertyValue);
    }
  }

  // TODO(sharon): assets downloaded via addAsset will end up in /tmp/<assetname>
  // while those downloaded when the project is downloaded will end up in
  // /tmp/assets/<assetname>. Do we care?
  private boolean addAsset(String assetPath) {
    try {
      String localAssetPath =
          yaController.downloadContentFromServer(assetPath);
      System.out.println("Asset downloaded from server to: " + localAssetPath);
      return workspaceController.getPhoneCommManager().addAsset(assetPath, localAssetPath);
    } catch (IOException e) {
      e.printStackTrace();
      // ignore for now.
      return false;
    }
  }

  private boolean installApplication(String apkFilePath, String appName, String packageName) {
    System.out.println("Got call to install the application: " + appName);
    try {
      String localApkPath = yaController.downloadContentFromServer(apkFilePath);
      System.out.println("Apk downloaded from server to: " + localApkPath);
      return workspaceController.getPhoneCommManager()
          .installApplication(localApkPath, appName, packageName);
    } catch (IOException e) {
      // TODO(kerr) handle this exception
      e.printStackTrace();
      return false;
    }
  }

  private boolean isPhoneConnected() {
    return workspaceController.getPhoneCommManager().connectedToPhone();
  }

  /**
   * Start an HTTP server so the ODE client can talk to us.
   * Command-line arguments:
   * <ol>
   * <li>the base URL of the WebStartFileServlet in the ODE server
   * <li>the path within the WebStartFileServlet to use when sending our port
   *     and when retrieving the phone app apk
   * <li>boolean indicating whether this is a production server
   * </ol>
   *
   * @throws IOException if running the HTTP server throws it
   */
  public static void main(String[] args) throws IOException {
    String baseUrl = args[0];
    String path = args[1];
    HttpServer server = new HttpServer();

    // Give the WorkspaceControllerHolder a factory that will create a WorkspaceController.
    // This ensures that only one workspace controller will be created and that it will be the
    // appropriate implementation: WorkspaceController.
    IWorkspaceController.Factory factory = new IWorkspaceController.Factory() {
      @Override
      public IWorkspaceController create() {
        return new WorkspaceController();
      }
    };
    WorkspaceControllerHolder.setFactory(factory, false);  // not headless
    WorkspaceController workspaceController = (WorkspaceController) WorkspaceControllerHolder.get();

    YaHttpServerMain main =
        new YaHttpServerMain(server, baseUrl, path, workspaceController,
            new ServerConnection() {
              @Override
              public HttpURLConnection getConnection(String path) throws MalformedURLException,
                  IOException {
                URLConnection urlConnection = new URL(path).openConnection();
                if (!(urlConnection instanceof HttpURLConnection)) {
                  throw new MalformedURLException("Not an http URL: " + path);
                } else {
                  return (HttpURLConnection) urlConnection;
                }
              }
            });
    AndroidController androidController = main.getAndroidController();
    workspaceController.startCodeblocks(main.getExternalController(), androidController);
    System.out.flush();

    /*
     * Now, we start up the HTTP server. If any calls come in while the fresh
     * workspace is loading they will get scheduled to run immediately after the
     * workspace is loaded.
     */
    server.runHttpServer();
    if (androidController != null) {
      androidController.androidCleanUpBeforeExit();
    }

    /*
     * When this process is started via JavaWebStart on Linux, it exits without
     * calling System.exit here. However, on the Mac, there are two threads
     * related to JavaWebStart that are not daemon threads: the "Javaws Secure
     * Thread" and the "CacheMemoryCleanUpThread", both in the
     * "javawsSecurityThreadGroup" thread group. These non-daemon threads
     * prevent the process from exiting unless we explicitly call System.exit
     * here.
     */
    try {
      synchronized (workspaceControllerLock) {
        workspaceController.persistCodeblocksSourceFile(true /* wait for save to complete */);
      }
    } catch (SaveException e) {
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }
}
