// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.WebServiceUtil;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Voting component communicates with a Web service to retrieve a ballot
 * and send back users' votes.
 *
 * <p>The application should call the method <code>RequestBallot</code>, usually
 * in the <code>Initialize</code> event handler, in order to get the ballot
 * question and options from the Web service (specified by the
 * <code>ServiceURL</code> property).  Depending on the response from the
 * Web service, the system will raise one of the following three events:
 * <ol>
 * <li> <code>GotBallot</code>, indicating that the ballot question and options
 *      were retrieved and the properties <code>BallotQuestion</code> and
 *      <code>BallotOptions</code> have been set.</li>
 * <li> <code>NoOpenPoll</code>, indicating that no ballot question is
 *      available.</li>
 * <li> <code>WebServiceError</code>, indicating that the service did not
 *      provide a legal response and providing an error messages.</li>
 * </ol></p>
 *
 * <p>After getting the ballot, the application should allow the user to make
 * a choice from among <code>BallotOptions</code> and set the property
 * <code>UserChoice</code> to that choice.  The application should also set
 * <code>UserId</code> to specify which user is voting.</p>
 *
 * <p>Once the application has set <code>UserChoice</code> and
 * <code>UserId</code>, the application can call <code>SendBallot</code> to
 * send this information to the Web service.  If the service successfully
 * receives the vote, the event <code>GotBallotConfirmation</code> will be
 * raised.  Otherwise, the event <code>WebServiceError</code> will be raised
 * with the appropriate error message.</p>
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(version = YaVersion.VOTING_COMPONENT_VERSION,
    designerHelpDescription = "<p>The Voting component enables users to vote " +
    "on a question by communicating with a Web service to retrieve a ballot " +
    "and later sending back users' votes.</p>",
    category = ComponentCategory.INTERNAL, // moved to Internal until fully tested
    nonVisible = true,
    iconName = "images/voting.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")

public class Voting extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "Voting";
  private static final String REQUESTBALLOT_COMMAND = "requestballot";
  private static final String SENDBALLOT_COMMAND = "sendballot";
  private static final String IS_POLLING_PARAMETER = "isPolling";
  private static final String ID_REQUESTED_PARAMETER = "idRequested";
  private static final String BALLOT_QUESTION_PARAMETER = "question";
  private static final String BALLOT_OPTIONS_PARAMETER = "options";
  private static final String USER_CHOICE_PARAMETER = "userchoice";
  private static final String USER_ID_PARAMETER = "userid";

  private Handler androidUIHandler;
  private ComponentContainer theContainer;
  private Activity activityContext;

  private String userId;
  private String serviceURL;
  private String ballotQuestion;
  private String ballotOptionsString;

  // The choices that a vote selects among
  private ArrayList<String> ballotOptions;

  // TODO(halabelson): idRequested isn't used in this version, but we'll keep it for the future
  private Boolean idRequested;
  private String userChoice;
  private Boolean isPolling;

  public Voting(ComponentContainer container){
    super(container.$form());
    serviceURL = "http://androvote.appspot.com";
    userId = "";
    isPolling = false;
    idRequested = false;
    ballotQuestion = "";
    ballotOptions = new ArrayList<String>();
    userChoice = "";

    androidUIHandler = new Handler();
    theContainer = container;
    activityContext = container.$context();

    // We set the initial value of serviceURL to be the
    // demo Web service
    serviceURL = "http://androvote.appspot.com";
  }

  /**
   * The URL of the Voting Service
   */
  @SimpleProperty(
      description = "The URL of the Voting service",
      category = PropertyCategory.BEHAVIOR)
  public String ServiceURL() {
    return serviceURL;
  }

  /**
   * Set the URL of the Voting Service
   *
   * @param serviceURL the URL (includes initial http:, but no trailing slash)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "http://androvote.appspot.com")
  @SimpleProperty
  public void ServiceURL(String serviceURL) {
    this.serviceURL = serviceURL;
  }

  /**
   * The question to be voted on.
   */
  @SimpleProperty(
      description = "The question to be voted on.",
      category = PropertyCategory.BEHAVIOR)
  public String BallotQuestion() {
    return ballotQuestion;
  }

  /**
   * The list of choices to vote.
   */
  @SimpleProperty(
      description = "The list of ballot options.",
      category = PropertyCategory.BEHAVIOR)
  public List<String> BallotOptions(){
    return ballotOptions;
  }


  // This should not be settable by the user
  // @SimpleProperty
  // public void BallotOptions(String ballotOptions){
  //   this.ballotOptions = ballotOptions;
  // }

  /**
   * An Id that is sent to the Web server along with the vote.
   */
  @SimpleProperty(
      description = "A text identifying the voter that is sent to the Voting " +
                    "server along with the vote.  This must be set before " +
                    "<code>SendBallot</code> is called.",
      category = PropertyCategory.BEHAVIOR)
  public String UserId() {
    return userId;
  }

  /**
   * Set an Id to be sent to the Web server along with the vote.
   *
   * @param userId the string to use as the Id
   */
  @SimpleProperty
  public void UserId(String userId){
    this.userId = userId;
  }

  /**
   * The choice to select when sending the vote.
   */
  @SimpleProperty(
      description = "The ballot choice to send to the server, which must be " +
                    "set before <code>SendBallot</code> is called.  " +
                    "This must be one of <code>BallotOptions</code>.",
      category = PropertyCategory.BEHAVIOR)
  public String UserChoice() {
    return userChoice;
  }

  /**
   * Set the choice to select when sending the vote.
   *
   * @param userChoice the choice to select.  Must be one of the BallotOptions
   */
  @SimpleProperty
  public void UserChoice(String userChoice){
    this.userChoice = userChoice;
  }

  /**
   * Returns the registered email address, as a string, for this
   * device's user.
   */
  @SimpleProperty(
      description = "The email address associated with this device. This property has been " +
      "deprecated and always returns the empty text value.",
      category = PropertyCategory.BEHAVIOR)
  public String UserEmailAddress() {
    // The UserEmailAddress has not been supported since before the Gingerbread release, so we
    // suspect that nobody is relying on it, and are therefore deprecating it. If it happens that
    // it needs to be added back, the way to get an email address, is to force the user to select
    // an account. This has to be done asynchronously (not here on the UI thread), generally when
    // the application starts. However, that would mean that the application would always ask the
    // user to select an account at startup, even if the application never actually accesses this
    // property, which would possibly be alarming to the user of a Voting application.
    return "";
  }

  /* RequestBallot will talk to the Web service and retrieve the ballot of
   * the current open poll. Depending on the service response, two events
   * might be triggered: NoOpenPoll or GotBallot.
   * When a ballot is received, the JSON response looks like this:
   *            {"isPolling" : "true",
   *            "idRequested" : "true",
   *            "question" : "What are you?",
   *            "options": [ "I'm a PC", "I'm a Mac" ] }
   */

  /**
   * Send a request ballot command to the Voting server.
   */
  @SimpleFunction(
      description =
      "Send a request for a ballot to the Web service specified " +
      "by the property <code>ServiceURL</code>.  When the " +
      "completes, one of the following events will be raised: " +
      "<code>GotBallot</code>, <code>NoOpenPoll</code>, or " +
      "<code>WebServiceError</code>.")
  public void RequestBallot() {
    final Runnable call = new Runnable() {
      public void run() { postRequestBallot(); }};
      AsynchUtil.runAsynchronously(call);
  }

  private void postRequestBallot(){
    AsyncCallbackPair<JSONObject> myCallback = new AsyncCallbackPair<JSONObject>() {
      public void onSuccess(JSONObject result) {
        if (result == null) {
          // Signal a Web error event to indicate that there was no response
          // to this request for a ballot.
          androidUIHandler.post(new Runnable() {
            public void run() {
              WebServiceError("The Web server did not respond to your request for a ballot");
            }
          });
          return;
        } else {
          try {
            Log.i(LOG_TAG, "postRequestBallot: ballot retrieved " + result);
            // The Web service is designed to return the JSON encoded object
            // This has to be a legal JSON encoding.  For example, true and false
            // should not be quoted if we're using getBoolean.  A bad encoding will
            // throw a JSON exception.
            isPolling = result.getBoolean(IS_POLLING_PARAMETER);
            if (isPolling){
              //populate parameter's value directly from reading JSONObject
              idRequested = result.getBoolean(ID_REQUESTED_PARAMETER);
              ballotQuestion = result.getString(BALLOT_QUESTION_PARAMETER);
              ballotOptionsString = result.getString(BALLOT_OPTIONS_PARAMETER);
              ballotOptions  = JSONArrayToArrayList(new JSONArray(ballotOptionsString));
              androidUIHandler.post(new Runnable() {
                public void run() {
                  GotBallot();
                }
              });
            } else {
              androidUIHandler.post(new Runnable() {
                public void run() {
                  NoOpenPoll();
                }
              });
            }
          } catch (JSONException e) {
            // Signal a Web error event to indicate the the server
            // returned a garbled value.  From the user's perspective,
            // there may be no practical difference between this and
            // the "no response" error above, but application writers
            // can create handlers to use these events as they choose.
            // Note that server errors that create malformed JSON
            // responses will sometimes be caught here.
            androidUIHandler.post(new Runnable() {
              public void run() {
                WebServiceError("The Web server returned a garbled object");
              }
            });
            return;
          }
        }
      }
      public void onFailure(final String message) {
        Log.w(LOG_TAG, "postRequestBallot Failure " + message);
          androidUIHandler.post(new Runnable() {
            public void run() {
              WebServiceError(message);
            }
          });
          return;
      }
    };

    WebServiceUtil.getInstance().postCommandReturningObject(
        serviceURL,
        REQUESTBALLOT_COMMAND,
        null,
        myCallback);
    return;
  }

  private ArrayList<String> JSONArrayToArrayList(JSONArray ja) throws JSONException {
    ArrayList<String> a = new ArrayList<String>();
    for (int i = 0; i < ja.length(); i++) {
      a.add(ja.getString(i));
    }
    return a;
    }


  /**
   * Event indicating that a ballot was received from the Web service.
   */
  @SimpleEvent(
      description =
      "Event indicating that a ballot was retrieved from the Web " +
      "service and that the properties <code>BallotQuestion</code> and " +
      "<code>BallotOptions</code> have been set.  This is always preceded " +
      "by a call to the method <code>RequestBallot</code>.")
  public void GotBallot() {
    EventDispatcher.dispatchEvent(this, "GotBallot");
  }

  /**
   * Event indicating that the service has no open poll.
   */
  @SimpleEvent
  public void NoOpenPoll() {
    EventDispatcher.dispatchEvent(this, "NoOpenPoll");
  }

  /**
   * Send a ballot to the Web Voting server.  The userId and the choice are
   * specified by the UserId and UserChoice properties.
   */
  @SimpleFunction(
      description =
      "Send a completed ballot to the Web service.  This should " +
      "not be called until the properties <code>UserId</code> " +
      "and <code>UserChoice</code> have been set by the application.")
  public void SendBallot() {
    final Runnable call = new Runnable() {
      public void run() { postSendBallot(userChoice, userId); }};
      AsynchUtil.runAsynchronously(call);
  }

  private void postSendBallot(String userChoice, String userId){
    AsyncCallbackPair<String> myCallback = new AsyncCallbackPair<String>(){
      // the Web service will send back a confirmation message, but
      // the component ignores it and notes only that anything at
      // all was sent back.  We can improve this later.
      public void onSuccess(String response) {
        androidUIHandler.post(new Runnable() {
          public void run() {
            GotBallotConfirmation();
          }
        });
      }
      public void onFailure(final String message) {
        Log.w(LOG_TAG, "postSendBallot Failure " + message);
          androidUIHandler.post(new Runnable() {
            public void run() {
              WebServiceError(message);
            }
          });
          return;
      }
    };

    WebServiceUtil.getInstance().postCommand(serviceURL,
        SENDBALLOT_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(USER_CHOICE_PARAMETER, userChoice),
            new BasicNameValuePair(USER_ID_PARAMETER, userId)),
            myCallback);

  }

  /**
   * Event confirming that the Voting service received the ballot.
   */
  @SimpleEvent
  public void GotBallotConfirmation() {
    EventDispatcher.dispatchEvent(this, "GotBallotConfirmation");
  }

  //-----------------------------------------------------------------------------
  /**
   * Event indicating that the communication with the Web service resulted in
   * an error.
   *
   * @param message the error message
   */
  @SimpleEvent
  public void WebServiceError(String message) {
    // Invoke the application's "WebServiceError" event handler
    EventDispatcher.dispatchEvent(this, "WebServiceError", message);
  }
}
