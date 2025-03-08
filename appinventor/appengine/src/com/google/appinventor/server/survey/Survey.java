// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * This modules supports sending the user to an external server to complete a survey.
 * When someone logs in we call the "check" function to see if they need to fill out the
 * survey. Check sends a request to the survey server which returns whether or not the
 * user needs to fill out the survey. The check function here then either returns null
 * if no survey is pending, or returns the URL that they user should be sent to.
 *
 * We are called out of "getSystemConfig" and we return the URL there. If present, the
 * client side (Ode) will then redirect the user to the survey.
 *
 * We use an encrypted protocol buffer to communicate with the survey server. It returns
 * its answer as a JSON object. There is a shared key that is used to create and verify
 * the token.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

package com.google.appinventor.server.survey;

import com.google.appinventor.server.flags.Flag;

import org.keyczar.Crypter;
import org.keyczar.util.Base64Coder;
import org.keyczar.exceptions.KeyczarException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Survey {
  private static Flag<String> surveyServer = Flag.createFlag("survey.server", "");
  private static Flag<String> keyFile = Flag.createFlag("survey.keyfile", "WEB-INF/surveykey");
  private static Flag<String> returnUrl = Flag.createFlag("survey.returnurl", "");
  private static Crypter crypter;

  private static final Logger LOG =
    Logger.getLogger(Survey.class.getName());

  static {
    try {
      crypter = new Crypter(keyFile.get());
    } catch (KeyczarException e) {
      e.printStackTrace();
    }
  }

  private Survey() {
  }

  public static synchronized String makeSurveyToken(String username) {
    try {
      SurveyProto.surveytoken newToken = SurveyProto.surveytoken.newBuilder()
        .setCommand(SurveyProto.surveytoken.CommandType.DOSURVEY)
        .setLoginname(username)
        .setReturnurl(returnUrl.get()).build();
      return Base64Coder.encode(crypter.encrypt(newToken.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Check to see if this user should take the survey. We use the username/email
   * instead of the internal userId because the same user may be using more then
   * one instance of MIT App Inventor and we do not need them to fill out the survey
   * more then once. For example ai2.appinventor.mit.edu vs. code.appinventor.mit.edu
   *
   * @param username The person username/email
   * @returns The URL to redirect to (with a query string) or null if not survey is needed
   */
  public static String check(String username) {
    try {
      if (surveyServer.get().isEmpty()) { // No configured survey server
        LOG.severe("surveyServer is empty");
        return null;
      }
      String token = makeSurveyToken(username);
      URL surveyUrl = new URL(surveyServer.get() + "/check?token=" + token);
      HttpURLConnection connection = (HttpURLConnection) surveyUrl.openConnection();
      connection.setConnectTimeout(5000); // 5 second timeout
      int responseCode = 0;
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        return null;            // Maybe we should log something
      }
      String rawResponse = readContent(connection.getInputStream());

      JSONObject data = new org.json.JSONObject(rawResponse);
      String status = data.getString("status");
      connection.disconnect();
      if (status.equals("DOIT")) {
        return (surveyServer.get() + "/?token=" + token);
      } else {
        return null;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  /*
   * Reads the UTF-8 content from the given input stream.
   */
  private static String readContent(InputStream stream) throws IOException {
    if (stream != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      try {
        return CharStreams.toString(reader);
      } finally {
        reader.close();
      }
    }
    return null;
  }
}

