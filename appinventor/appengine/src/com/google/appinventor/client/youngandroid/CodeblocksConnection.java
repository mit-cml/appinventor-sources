// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.youngandroid;

import com.google.appinventor.client.jsonp.JsonpConnection;
import com.google.appinventor.common.youngandroid.YaHttpServerConstants;
import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

import java.util.HashMap;
import java.util.Map;

/**
 * CodeblocksConnection provides methods for communicating from the ODE
 * client to a codeblocks JavaWebStart instance. Connections are managed
 * by the CodeblocksManager.
 * @author sharon@google.com (Sharon Perl)
 *
 */

public class CodeblocksConnection extends JsonpConnection {

  /**
   * Creates a CodeblocksConnection object with the given connection information,
   * JSON parser, and escape function.
   *
   * @param connInfo the JSONP connection information
   * @param jsonParser the JSON parser
   * @param escapeQueryParameterFunction the escape function
   */
  public CodeblocksConnection(JsonpConnectionInfo connInfo, JSONParser jsonParser,
      Function<String, String> escapeQueryParameterFunction) {
    super(connInfo, jsonParser, escapeQueryParameterFunction);
  }

  /**
   * Sends a JSONP request to codeblocks to sync the form properties and then
   * generate new YAIL in order to show any problems (like empty sockets) to the user.
   */
  public void generateYail() {
    Map<String, Object> parameters = createNewParameterMap();
    sendJsonpRequestAndPoll(YaHttpServerConstants.GENERATE_YAIL, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(null));
  }

  /**
   * Sends a JSONP request to codeblocks to tell it to reload properties.
   */
  public void reloadProperties(AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    sendJsonpRequestAndPoll(YaHttpServerConstants.RELOAD_PROPERTIES, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  /**
   * Sends a JSONP request to codeblocks to tell it to load properties and blocks.
   */
  public void loadPropertiesAndBlocks(String formPropertiesPath, String assetsPath,
      String projectName, AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    parameters.put(YaHttpServerConstants.FORM_PROPERTIES_PATH, formPropertiesPath);
    parameters.put(YaHttpServerConstants.ASSET_PATH, assetsPath);
    parameters.put(YaHttpServerConstants.PROJECT_NAME, projectName);
    sendJsonpRequestAndPoll(YaHttpServerConstants.LOAD_FORM, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  /**
   * Sends a JSONP request to codeblocks to tell it clear its workspace.
   */
  public void clearCodeblocks(AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    sendJsonpRequestAndPoll(YaHttpServerConstants.CLEAR_CODEBLOCKS, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  /**
   * Sends a JSONP request to codeblocks to tell it to save its
   * current workspaces
   */
  public void saveCodeblocksSource(AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    sendJsonpRequestAndPoll(YaHttpServerConstants.SAVE_CODEBLOCKS_SOURCE, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  /**
   * Sync a single component property with codeblocks
   * @param componentName the name of the component
   * @param componentType the type of the component
   * @param propertyName the name of the property
   * @param propertyValue the value of the property
   * @param callback a callback to pass along to the connection
   */
  public void syncProperty(String componentName, String componentType,
                           String propertyName, String propertyValue,
                           AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    parameters.put(YaHttpServerConstants.COMPONENT_NAME, componentName);
    parameters.put(YaHttpServerConstants.COMPONENT_TYPE, componentType);
    parameters.put(YaHttpServerConstants.PROPERTY_NAME, propertyName);
    parameters.put(YaHttpServerConstants.PROPERTY_VALUE, propertyValue);
    sendJsonpRequestAndPoll(YaHttpServerConstants.SYNC_PROPERTY, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  public void addAsset(String assetFilePath, AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    parameters.put(YaHttpServerConstants.ASSET_PATH, assetFilePath);
    sendJsonpRequestAndPoll(YaHttpServerConstants.ADD_ASSET, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  public void installApplication(String apkFilePath, String appName, String packageName,
      AsyncCallback<Void> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    parameters.put(YaHttpServerConstants.APK_FILE_PATH, apkFilePath);
    parameters.put(YaHttpServerConstants.APP_NAME, appName);
    parameters.put(YaHttpServerConstants.PACKAGE_NAME, packageName);
    sendJsonpRequestAndPoll(YaHttpServerConstants.INSTALL_APPLICATION, parameters,
        booleanResponseDecoder, createBooleanCallbackWrapper(callback));
  }

  public void isPhoneConnected(AsyncCallback<Boolean> callback) {
    Map<String, Object> parameters = createNewParameterMap();
    sendJsonpRequestAndPoll(YaHttpServerConstants.IS_PHONE_CONNECTED, parameters,
        booleanResponseDecoder, callback);
  }

  private static HashMap<String, Object> createNewParameterMap() {
    HashMap<String, Object> newMap = new HashMap<String, Object>();
    return newMap;
  }

  /**
   * Creates an AsyncCallback<Boolean> that wraps the given callback.
   * If onSuccess is called with false, it will call onFailure.
   *
   * @param callback an optional callback to receive success or failure
   */
  private static AsyncCallback<Boolean> createBooleanCallbackWrapper(
      final AsyncCallback<Void> voidCallback) {
    return new AsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean okay) {
        if (okay) {
          if (voidCallback != null) {
            voidCallback.onSuccess(null);
          }
        } else {
          // TODO(lizlooney,sharon) - make codeblocks indicate failure in a way that triggers
          // onFailure below with an appropriate Throwable so we know what happened.
          if (voidCallback != null) {
            voidCallback.onFailure(
                new InvocationException("Received false response from blocks editor"));
          }
        }
      }
      @Override
      public void onFailure(Throwable caught) {
        if (voidCallback != null) {
          voidCallback.onFailure(caught);
        }
      }
    };
  }
}
