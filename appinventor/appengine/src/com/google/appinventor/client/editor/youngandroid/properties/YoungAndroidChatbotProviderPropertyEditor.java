// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

// import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.utils.Promise.reject;
import static com.google.appinventor.client.utils.Promise.rejectWithReason;
import static com.google.appinventor.client.utils.Promise.resolve;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Promise.RejectCallback;
import com.google.appinventor.client.utils.Promise.ResolveCallback;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import com.google.appinventor.common.version.AppInventorFeatures;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.gwt.user.client.Window;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Property editor for chatbot provider property
 *
 */
public class YoungAndroidChatbotProviderPropertyEditor extends ChoicePropertyEditor {

  private static final Promise<Choice[]> choicePromise = new Promise<Choice[]>((resolve, reject) -> {
      String url = AppInventorFeatures.chatBotHost() + "model_list/v1";
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

      try {
        Request response = builder.sendRequest(null, new RequestCallback() {
          @Override
          public void onError(Request request, Throwable exception) {
            reject.apply(new Promise.WrappedException(exception));
          }

          @Override
          public void onResponseReceived(Request request, Response response) {
            try {
              int statusCode = response.getStatusCode();
              if (statusCode != Response.SC_OK) {
                reject.apply(new Promise.WrappedException(new IOException("Bad Response Code " + statusCode)));
                return;
              }
              String choiceInfo = response.getText();
              setChatProxInfo(choiceInfo);
              String choiceString = getChatProxyInfo("provider");
              JSONArray providerArray = JSONParser.parse(choiceString).isArray();
              Choice[] values = new Choice[providerArray.size()];
              for (int i = 0; i < providerArray.size(); i++) {
                String value = providerArray.get(i).isString().stringValue();
                values[i] = new Choice(value, value);
              }
              resolve.apply(values);
            } catch (Exception e) {
              reject.apply(new Promise.WrappedException(e));
            }
          }
        });
      } catch (RequestException e) {
        reject.apply(new Promise.WrappedException(e));
      }
    });

  public YoungAndroidChatbotProviderPropertyEditor() {
    super(choicePromise);
  }

  private static native String getChatProxyInfo(String item) /*-{
    if (top.chatproxyinfo) {
      return JSON.stringify(top.chatproxyinfo[item]);
    }
  }-*/;

  private static native void setChatProxInfo(String info) /*-{
    top.chatproxyinfo = JSON.parse(info);
  }-*/;

}
