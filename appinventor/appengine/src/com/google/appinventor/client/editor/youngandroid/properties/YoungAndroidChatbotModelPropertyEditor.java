// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.utils.Promise.reject;
import static com.google.appinventor.client.utils.Promise.rejectWithReason;
import static com.google.appinventor.client.utils.Promise.resolve;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Promise.RejectCallback;
import com.google.appinventor.client.utils.Promise.ResolveCallback;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import com.google.appinventor.common.version.AppInventorFeatures;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Property editor for chatbot model property
 *
 */
public class YoungAndroidChatbotModelPropertyEditor extends ChoicePropertyEditor {

  private static final Promise<Choice[]> choicePromise = new Promise<Choice[]>((resolve, reject) -> {
      String url = AppInventorFeatures.chatBotHost() + "model_list/v1";
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

      try {
        String choiceString = getChatProxyInfo("model");
        if (choiceString != null && !choiceString.isEmpty()) {
          String[] choiceNames = choiceString.split(",");
          Choice[] values = new
            Choice[choiceNames.length];
          for (int i = 0; i < choiceNames.length; i++)
            values[i] = new Choice(choiceNames[i],
              choiceNames[i]);
          resolve.apply(values);
          return;
        }
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
              String choiceString = getChatProxyInfo("model");
              JSONObject choiceJson = JSONParser.parse(choiceString).isObject();
              Iterator<String> keys = choiceJson.keySet().iterator();
              Choice[] values = new
                Choice[choiceJson.size() + 1];

              ///////////////////////////////////////////////////////////////////////
              // Note that we add the default choice at the end of the list. This  //
              // is because the default evaluates to the empty string, and some    //
              // provider/model pairs also evaluate to the empty string, which is  //
              // to say the "default" should be used in that case. When the value  //
              // of the pull-down is the empty string (or actually, when there are //
              // two entries that evaluate to the same value, then the label of    //
              // the last one is shown in the pull-down.  This way, the last one   //
              // will be "default" for the empty string. Sorry if this is          //
              // confusing!                                                        //
              ///////////////////////////////////////////////////////////////////////

              int i = 0;
              while (keys.hasNext()) {
                String modelDisplayName = keys.next();
                String modelValue = choiceJson.get(modelDisplayName).isString().stringValue();
                values[i] = new Choice(modelDisplayName, modelValue);
                i++;
              }
              values[i] = new Choice(MESSAGES.defaultText(), "");
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

  public YoungAndroidChatbotModelPropertyEditor() {
    super(choicePromise);
  }

  // Note: top.chatproxinfo[item] is a json object. But we stringify it
  // here so we don't have to deal with passing a JSON object from Javascript
  // back to the java world. Ok, so I'm lazy.
  private static native String getChatProxyInfo(String item) /*-{
    if (top.chatproxyinfo) {
      return JSON.stringify(top.chatproxyinfo[item]);
    }
  }-*/;

  private static native void setChatProxInfo(String info) /*-{
    top.chatproxyinfo = JSON.parse(info);
  }-*/;

}
