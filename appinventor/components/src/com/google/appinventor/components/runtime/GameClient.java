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
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.GameInstance;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.PlayerListDelta;
import com.google.appinventor.components.runtime.util.WebServiceUtil;
import com.google.appinventor.components.runtime.util.YailList;

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
 * GameClient provides a way for AppInventor applications to
 * communicate with online game servers. This allows users to create
 * games that are coordinated and managed in the cloud.
 *
 * Most communication is done by sending keyed messages back and
 * forth between the client and the server in the form of YailLists.
 * The server and game client can then switch on the keys and perform
 * more complex operations on the data. In addition, game servers can
 * implement a library of server commands that can perform complex
 * functions on the server and send back responses that are converted
 * into YailLists and sent back to the component. For more
 * information about server commands, consult the game server code
 * at http://code.google.com/p/app-inventor-for-android/
 *
 * Games instances are uniquely determined by a game id and an
 * instance id. In general, each App Inventor program should have
 * its own game id. Then, when running different instances of that
 * program, new instance ides should be used. Players are
 * represented uniquely by the email address registered to their
 * phones.
 *
 * All call functions perform POSTs to a web server. Upon successful
 * completion of these POST requests, FunctionCompleted will be
 * triggered with the function name as an argument. If the post
 * fails, WebServiceError will trigger with the function name and the
 * error message as arguments. These calls allow for application
 * creators to deal with web service failures and keep track of the
 * success or failure of their operations. The only exception to this
 * is when the return value from the server has the incorrect game id
 * or instance id. In this case, the response is completely ignored
 * and neither of these events will trigger.
 *
 *
 */
@DesignerComponent(version = YaVersion.GAMECLIENT_COMPONENT_VERSION,
    description = "Provides a way for applications to communicate with online game servers",
    category = ComponentCategory.INTERNAL, // moved to internal until fully tested
    nonVisible = true,
    iconName = "images/gameClient.png")
@SimpleObject
@UsesPermissions(
    permissionNames = "android.permission.INTERNET, " +
                "com.google.android.googleapps.permission.GOOGLE_AUTH")
public class GameClient extends AndroidNonvisibleComponent
    implements Component, OnResumeListener, OnStopListener {

  private static final String LOG_TAG = "GameClient";

  // Parameter keys
  private static final String GAME_ID_KEY = "gid";
  private static final String INSTANCE_ID_KEY = "iid";
  private static final String PLAYER_ID_KEY = "pid";
  private static final String INVITEE_KEY = "inv";
  private static final String LEADER_KEY = "leader";
  private static final String COUNT_KEY = "count";
  private static final String TYPE_KEY = "type";
  private static final String INSTANCE_PUBLIC_KEY = "makepublic";
  private static final String MESSAGE_RECIPIENTS_KEY = "mrec";
  private static final String MESSAGE_CONTENT_KEY = "contents";
  private static final String MESSAGE_TIME_KEY = "mtime";
  private static final String MESSAGE_SENDER_KEY = "msender";
  private static final String COMMAND_TYPE_KEY = "command";
  private static final String COMMAND_ARGUMENTS_KEY = "args";
  private static final String SERVER_RETURN_VALUE_KEY = "response";
  private static final String MESSAGES_LIST_KEY = "messages";
  private static final String ERROR_RESPONSE_KEY = "e";
  private static final String PUBLIC_LIST_KEY = "public";
  private static final String JOINED_LIST_KEY = "joined";
  private static final String INVITED_LIST_KEY = "invited";
  private static final String PLAYERS_LIST_KEY = "players";

  // Command keys
  private static final String GET_INSTANCE_LISTS_COMMAND = "getinstancelists";
  private static final String GET_MESSAGES_COMMAND = "messages";
  private static final String INVITE_COMMAND = "invite";
  private static final String JOIN_INSTANCE_COMMAND = "joininstance";
  private static final String LEAVE_INSTANCE_COMMAND = "leaveinstance";
  private static final String NEW_INSTANCE_COMMAND = "newinstance";
  private static final String NEW_MESSAGE_COMMAND = "newmessage";
  private static final String SERVER_COMMAND = "servercommand";
  private static final String SET_LEADER_COMMAND = "setleader";

  // URL for accessing the game server
  private String serviceUrl;
  private String gameId;
  private GameInstance instance;
  private Handler androidUIHandler;
  private Activity activityContext;

  private String userEmailAddress = "";

  // Game instances in the current GameId that this player has joined
  private List<String> joinedInstances;
  // Game instances to which this player has been invited
  private List<String> invitedInstances;
  // Game instances which have been made public.
  private List<String> publicInstances;

  /**
   * Creates a new GameClient component.
   *
   * @param container the Form that this component is contained in.
   */
  public GameClient(ComponentContainer container) {
    super(container.$form());
    // Note that although this is creating a new Handler there is
    // only one UI thread in an Android app and posting to this
    // handler queues up a Runnable for execution on that thread.
    androidUIHandler = new Handler();
    activityContext = container.$context();
    form.registerForOnResume(this);
    form.registerForOnStop(this);
    gameId = "";
    instance = new GameInstance("");
    joinedInstances = Lists.newArrayList();
    invitedInstances = Lists.newArrayList();
    publicInstances = Lists.newArrayList();
    serviceUrl = "http://appinvgameserver.appspot.com";

    // This needs to be done in a separate thread since it uses
    // a blocking service to complete and will cause the UI to hang
    // if it happens in the constructor.
    /*
     * Remove this code until we fix LoginServiceUtil to work in later
     * versions of the android SDK.
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        userEmailAddress = LoginServiceUtil.getPhoneEmailAddress(activityContext);
        if (!userEmailAddress.equals("")) {
          UserEmailAddressSet(userEmailAddress);
        }
      }
    });
    */
  }


  //----------------------------------------------------------------
  // Properties

  /**
   * Returns a string indicating the game name for this application.
   * The same game ID can have one or more game instances.
   */
  @SimpleProperty(
      description = "The game name for this application. " +
      "The same game ID can have one or more game instances.",
      category = PropertyCategory.BEHAVIOR)
  public String GameId() {
    return gameId;
  }

  /**
   * Specifies a string indicating the family of the current game
   * instance.  The same game ID can have one or more game instance
   * IDs.
   */
  // Only exposed in the designer to enforce that each GameClient
  // instance should be made for a single GameId.
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  public void GameId(String id) {
    this.gameId = id;
  }

  /**
   * Returns the game instance id.  Taken together, the game ID and
   * the instance ID uniquely identify the game.
   */
  @SimpleProperty(
      description = "The game instance id.  Taken together," +
      "the game ID and the instance ID uniquely identify the game.",
      category = PropertyCategory.BEHAVIOR)
  public String InstanceId() {
    return instance.getInstanceId();
  }

  /**
   * Returns the set of game instances to which this player has been
   * invited but has not yet joined.  To ensure current values are
   * returned, first invoke {@link #GetInstanceLists}.
   */
  @SimpleProperty(
      description = "The set of game instances to which this player has been " +
                    "invited but has not yet joined.  To ensure current values are " +
                    "returned, first invoke GetInstanceLists.",
      category = PropertyCategory.BEHAVIOR)
  public List<String> InvitedInstances() {
    return invitedInstances;
  }

  /**
   * Returns the set of game instances in which this player is
   * participating.  To ensure current values are returned, first
   * invoke {@link #GetInstanceLists}.
   */
  @SimpleProperty(
      description = "The set of game instances in which this player is " +
      "participating.  To ensure current values are returned, first " +
      "invoke GetInstanceLists.",
      category = PropertyCategory.BEHAVIOR)
  public List<String> JoinedInstances() {
    return joinedInstances;
  }

  /**
   * Returns the game's leader. At any time, each game instance has
   * only one leader, but the leader may change with time.
   * Initially, the leader is the game instance creator. Application
   * writers determine special properties of the leader. The leader
   * value is updated each time a successful communication is made
   * with the server.
   */
  @SimpleProperty(
      description = "The game's leader. At any time, each game instance has " +
      "only one leader, but the leader may change with time.  " +
      "Initially, the leader is the game instance creator. Application " +
      "writers determine special properties of the leader. The leader " +
      "value is updated each time a successful communication is made " +
      "with the server.",
      category = PropertyCategory.BEHAVIOR)
  public String Leader() {
    return instance.getLeader();
  }

  /**
   * Returns the current set of players for this game instance. Each
   * player is designated by an email address, which is a string. The
   * list of players is updated each time a successful communication
   * is made with the game server.
   */
  @SimpleProperty(
      description = "The current set of players for this game instance. Each " +
      "player is designated by an email address, which is a string. The " +
      "list of players is updated each time a successful communication " +
      "is made with the game server.",
      category = PropertyCategory.BEHAVIOR)
  public List<String> Players() {
    return instance.getPlayers();
  }

  /**
   * Returns the set of game instances that have been marked public.
   * To ensure current values are returned, first
   * invoke {@link #GetInstanceLists}.
   */
  @SimpleProperty(
      description = "The set of game instances that have been marked public. " +
      "To ensure current values are returned, first " +
      "invoke {@link #GetInstanceLists}. ",
      category = PropertyCategory.BEHAVIOR)
  public List<String> PublicInstances() {
    return publicInstances;
  }

  /**
   * The URL of the game server.
   */
  @SimpleProperty(
      description = "The URL of the game server.",
      category = PropertyCategory.BEHAVIOR)
  public String ServiceUrl() {
    return serviceUrl;
  }

  /**
   * Set the URL of the game server.
   *
   * @param url The URL (include initial http://).
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "http://appinvgameserver.appspot.com")
  public void ServiceURL(String url){
    if (url.endsWith("/")) {
      this.serviceUrl = url.substring(0, url.length() - 1);
    } else {
      this.serviceUrl = url;
    }
  }

  /**
   * Returns the registered email address that is being used as the
   * player id for this game client.
   */
  @SimpleProperty(
      description = "The email address that is being used as the " +
                    "player id for this game client.   At present, users " +
                    "must set this manually in oder to join a game.  But " +
                    "this property will change in the future so that is set " +
      "automatically, and users will not be able to change it.",
      category = PropertyCategory.BEHAVIOR)

  public String UserEmailAddress() {
    if (userEmailAddress.equals("")) {
      Info("User email address is empty.");
    }
    return userEmailAddress;
  }

  /**
   * Changes the player of this game by changing the email address
   * used to communicate with the server.
   *
   * This should only be used during development. Games should not
   * allow players to set their own email address.
   *
   * @param emailAddress The email address to set the current player
   * id to.
   */
  @SimpleProperty
  public void UserEmailAddress(String emailAddress) {
    userEmailAddress = emailAddress;
    UserEmailAddressSet(emailAddress);
  }

  //----------------------------------------------------------------
  // Event Handlers

  /**
   * Indicates that a server request from a function call has
   * completed. This can be used to control a polling loop or
   * otherwise respond to server request completions.
   *
   * @param functionName The name of the App Inventor function that
   * finished.
   */
  @SimpleEvent(description = "Indicates that a function call completed.")
  public void FunctionCompleted(final String functionName) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "Request completed: " + functionName);
        EventDispatcher.dispatchEvent(GameClient.this, "FunctionCompleted", functionName);
      }});
  }

  /**
   * Ensures that the GameId was set by the game creator.
   */
  public void Initialize() {
    Log.d(LOG_TAG, "Initialize");
    if (gameId.equals("")) {
      throw new YailRuntimeError("Game Id must not be empty.", "GameClient Configuration Error.");
    }
  }

  /**
   * Indicates that a GetMessages call received a message. This could
   * be invoked multiple times for a single call to GetMessages.
   *
   * @param type The type of the message received.
   * @param contents The message's contents. Consists of a list
   * nested to arbitrary depth that includes string, boolean and
   * number values.
   */
  @SimpleEvent(description = "Indicates that a new message has " +
                "been received.")
  public void GotMessage(final String type, final String sender, final List<Object> contents) {
    Log.d(LOG_TAG, "Got message of type " + type);
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "GotMessage", type, sender, contents);
      }});
  }

  /**
   * Indicates that InstanceId has changed due to the creation of a
   * new instance or setting the InstanceId.
   *
   * @param instanceId The id of the instance the player is now in.
   */
  @SimpleEvent(description = "Indicates that the InstanceId " +
                "property has changed as a result of calling " +
                "MakeNewInstance or SetInstance.")
  public void InstanceIdChanged(final String instanceId) {
    Log.d(LOG_TAG, "Instance id changed to " + instanceId);
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "InstanceIdChanged", instanceId);
      }});
  }

  /**
   * Indicates a user has been invited to this game instance by
   * another player.
   *
   * @param instanceId The id of the new game instance.
   */
  @SimpleEvent(
      description = "Indicates that a user has been invited to " +
                "this game instance.")
  public void Invited(final String instanceId) {
    Log.d(LOG_TAG, "Player invited to " + instanceId);
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "Invited", instanceId);
      }});
  }

  /**
   * Indicates this game instance has a new leader. This could happen
   * in response to a call to SetLeader or by the side effects of a
   * server command performed by any player in the game.
   *
   * Since the current leader is sent back with every server
   * response, NewLeader can trigger after making any server call.
   *
   * @param playerId The email address of the new leader.
   */
  @SimpleEvent(description = "Indicates that this game has a new " +
                "leader as specified through SetLeader")
  public void NewLeader(final String playerId) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "Leader change to " + playerId);
        EventDispatcher.dispatchEvent(GameClient.this, "NewLeader", playerId);
      }});
  }

  /**
   * Indicates this game instance was created as specified via
   * MakeNewInstance. The creating player is automatically the leader
   * of the instance and the InstanceId property has already been set
   * to this new instance.
   *
   * @param instanceId The id of the newly created game instance.
   */
  @SimpleEvent(description = "Indicates that a new instance was " +
                "successfully created after calling MakeNewInstance.")
  public void NewInstanceMade(final String instanceId) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "New instance made: " + instanceId);
        EventDispatcher.dispatchEvent(GameClient.this, "NewInstanceMade", instanceId);
      }});
  }

  /**
   * Indicates that a player has joined this game instance.
   *
   * @param playerId The email address of the new player.
   */
  @SimpleEvent(description = "Indicates that a new player has " +
                "joined this game instance.")
  public void PlayerJoined(final String playerId) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        if (!playerId.equals(UserEmailAddress())) {
          Log.d(LOG_TAG, "Player joined: " + playerId);
          EventDispatcher.dispatchEvent(GameClient.this, "PlayerJoined", playerId);
        }
      }});
  }

  /**
   * Indicates that a player has left this game instance.
   *
   * @param playerId The email address of the player that left.
   */
  @SimpleEvent(description = "Indicates that a player has left " +
                "this game instance.")
  public void PlayerLeft(final String playerId) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "Player left: " + playerId);
        EventDispatcher.dispatchEvent(GameClient.this, "PlayerLeft", playerId);
      }});
  }

  /**
   * Indicates that an attempt to complete a server command failed on
   * the server.
   * @param command The command requested.
   * @param arguments The arguments sent to the command.
   */
  @SimpleEvent(
      description = "Indicates that a server command failed.")
  public void ServerCommandFailure(final String command, final YailList arguments) {
    androidUIHandler.post(new Runnable() {
      public void run() {
        Log.d(LOG_TAG, "Server command failed: " + command);
        EventDispatcher.dispatchEvent(GameClient.this, "ServerCommandFailure", command, arguments);
      }});
  }

  /**
   * Indicates that a ServerCommand completed.
   *
   * @param command The key for the command that resulted in this
   * response.
   * @param response The server response. This consists of a list
   * nested to arbitrary depth that includes string, boolean and
   * number values.
   */
  @SimpleEvent(description = "Indicates that a server command " +
                "returned successfully.")
  public void ServerCommandSuccess(final String command, final List<Object> response) {
    Log.d(LOG_TAG, command + " server command returned.");
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this,
            "ServerCommandSuccess", command, response);
      }});
  }

  /**
   * Indicates that the user email address property has been
   * successfully set. This event should be used to initialize
   * any web service functions.
   *
   * This separate event was required because the email address was
   * unable to be first fetched from the the UI thread without
   * causing programs to hang. GameClient will now start fetching
   * the user email address in its constructor and trigger this event
   * when it finishes.
   */
  @SimpleEvent(description = "Indicates that the user email " +
                "address has been set.")
  public void UserEmailAddressSet(final String emailAddress) {
    Log.d(LOG_TAG, "Email address set.");
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "UserEmailAddressSet", emailAddress);
      }});
  }

  //----------------------------------------------------------------
  // Message events

  /**
   * Indicates that something has occurred which the player should be
   * somehow informed of.
   *
   * @param message the message.
   */
  @SimpleEvent(description = "Indicates that something has " +
                "occurred which the player should know about.")
  public void Info(final String message) {
    Log.d(LOG_TAG, "Info: " + message);
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "Info", message);
      }});

  }

  /**
   * Indicates that the attempt to communicate with the web service
   * resulted in an error.
   *
   * @param functionName The name of the function call that caused this
   * error.
   * @param message the error message
   */
  @SimpleEvent(description = "Indicates that an error occurred " +
                "while communicating with the web server.")
  public void WebServiceError(final String functionName, final String message) {
    Log.e(LOG_TAG, "WebServiceError: " + message);
    androidUIHandler.post(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(GameClient.this, "WebServiceError", functionName, message);
      }});
  }

  //----------------------------------------------------------------
  // Functions

  /**
   * Updates the current InstancesJoined and InstancesInvited lists.
   *
   * If the player has been invited to new instances an Invited
   * event will be raised for each new instance.
   */
  @SimpleFunction(description = "Updates the InstancesJoined and " +
                "InstancesInvited lists. This procedure can be called " +
                "before setting the InstanceId.")
  public void GetInstanceLists() {
    AsynchUtil.runAsynchronously(new Runnable() {
        public void run() { postGetInstanceLists(); }});
  }

  private void postGetInstanceLists() {
    AsyncCallbackPair<JSONObject> readMessagesCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        processInstanceLists(response);
        FunctionCompleted("GetInstanceLists");
      }
      public void onFailure(final String message) {
        WebServiceError("GetInstanceLists", "Failed to get up to date instance lists.");
      }
    };

    postCommandToGameServer(GET_INSTANCE_LISTS_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress())),
        readMessagesCallback);
  }

  private void processInstanceLists(JSONObject instanceLists){
    try {
      joinedInstances = JsonUtil.getStringListFromJsonArray(instanceLists.
          getJSONArray(JOINED_LIST_KEY));

      publicInstances = JsonUtil.getStringListFromJsonArray(instanceLists.
          getJSONArray(PUBLIC_LIST_KEY));

      List<String> receivedInstancesInvited = JsonUtil.getStringListFromJsonArray(instanceLists.
          getJSONArray(INVITED_LIST_KEY));

      if (!receivedInstancesInvited.equals(InvitedInstances())) {
        List<String> oldList = invitedInstances;
        invitedInstances = receivedInstancesInvited;
        List<String> newInvites = new ArrayList<String>(receivedInstancesInvited);
        newInvites.removeAll(oldList);

        for (final String instanceInvited : newInvites) {
          Invited(instanceInvited);
        }
      }

    } catch (JSONException e) {
      Log.w(LOG_TAG, e);
      Info("Instance lists failed to parse.");
    }
  }

  /**
   * Retrieves messages of the specified type.
   *
   * Requests that only messages which have not been seen during
   * the current session are returned. Messages will be processed
   * in chronological order with the oldest first, however, only
   * the count newest messages will be retrieved. This means that
   * one could "miss out" on some messages if they request less than
   * the number of messages created since the last request for
   * that message type.
   *
   * Setting type to the empty string will fetch all message types.
   * Even though those message types were not specifically requested,
   * their most recent message time will be updated. This keeps
   * players from receiving the same message again if they later
   * request the specific message type.
   *
   * Note that the message receive times are not updated until after
   * the messages are actually received. Thus, if multiple message
   * requests are made before the previous ones return, they could
   * send stale time values and thus receive the same messages more
   * than once. To avoid this, application creators should wait for
   * the get messages function to return before calling it again.
   *
   * @param type The type of message to retrieve. If the empty string
   * is used as the message type then all message types will be
   * requested.
   * @param count The maximum number of messages to retrieve. This
   * should be an integer from 1 to 1000.
   */
  @SimpleFunction(
      description = "Retrieves messages of the specified type.")
  public void GetMessages(final String type, final int count) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postGetMessages(type, count); }});
  }

  private void postGetMessages(final String requestedType, final int count) {
    AsyncCallbackPair<JSONObject> myCallback = new AsyncCallbackPair<JSONObject>() {
      public void onSuccess(final JSONObject result) {
        try {
          int count = result.getInt(COUNT_KEY);
          JSONArray messages = result.getJSONArray(MESSAGES_LIST_KEY);
          for (int i = 0; i < count; i++) {
            JSONObject message = messages.getJSONObject(i);
            String type = message.getString(TYPE_KEY);
            String sender = message.getString(MESSAGE_SENDER_KEY);
            String time = message.getString(MESSAGE_TIME_KEY);
            List<Object> contents = JsonUtil.getListFromJsonArray(message.
                getJSONArray(MESSAGE_CONTENT_KEY));
            // Assumes that the server is going to return messages in
            // chronological order.
            if (requestedType.equals("")) {
              instance.putMessageTime(requestedType, time);
            }
            instance.putMessageTime(type, time);
            GotMessage(type, sender, contents);
          }
        } catch (JSONException e) {
          Log.w(LOG_TAG, e);
          Info("Failed to parse messages response.");
        }
        FunctionCompleted("GetMessages");
      }

      public void onFailure(String message) {
        WebServiceError("GetMessages", message);
      }
    };

    if (InstanceId().equals("")) {
      Info("You must join an instance before attempting to fetch messages.");
      return;
    }

    postCommandToGameServer(GET_MESSAGES_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(COUNT_KEY, Integer.toString(count)),
            new BasicNameValuePair(MESSAGE_TIME_KEY, instance.getMessageTime(requestedType)),
            new BasicNameValuePair(TYPE_KEY, requestedType)),
        myCallback);
  }

  /**
   * Invites a player to this game instance.
   *
   * Players implicitly accept invitations when they join games by
   * setting the instance id in their GameClient.
   *
   * Invitations remain active as long as the game instance exists.
   *
   * @param playerEmail a string containing the email address of the
   * player to become leader. The email should be in one of the
   * following formats:<br>"Name O. Person
   * &ltname.o.person@gmail.com&gt"<br>"name.o.person@gmail.com".
   */
  @SimpleFunction(
      description = "Invites a player to this game instance.")
  public void Invite(final String playerEmail) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postInvite(playerEmail); }});
  }

  private void postInvite(final String inviteeEmail) {
    AsyncCallbackPair<JSONObject> inviteCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        try {
          String invitedPlayer = response.getString(INVITEE_KEY);

          if (invitedPlayer.equals("")) {
            Info(invitedPlayer + " was already invited.");
          } else {
            Info("Successfully invited " + invitedPlayer + ".");
          }
        } catch (JSONException e) {
          Log.w(LOG_TAG, e);
          Info("Failed to parse invite player response.");
        }
        FunctionCompleted("Invite");
      }
      public void onFailure(final String message) {
        WebServiceError("Invite", message);
      }
    };

    if (InstanceId().equals("")) {
      Info("You must have joined an instance before you can invite new players.");
      return;
    }

    postCommandToGameServer(INVITE_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(INVITEE_KEY, inviteeEmail)),
        inviteCallback);
  }

  /**
   * Requests to leave the current instance. If the player is the
   * current leader, the lead will be passed to another player.
   *
   * If there are no other players left in the instance after the
   * current player leaves, the instance will become unjoinable.
   *
   * Upon successful completion of this command, the instance
   * lists will be updated and InstanceId will be set back to the
   * empty string.
   *
   * Note that while this call does clear the leader and player
   * lists, no NewLeader or PlayerLeft events are raised.
   */
  @SimpleFunction(description = "Leaves the current instance.")
  public void LeaveInstance() {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        postLeaveInstance();
      }
    });
  }

  private void postLeaveInstance() {
    AsyncCallbackPair<JSONObject> setInstanceCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        SetInstance("");
        processInstanceLists(response);
        FunctionCompleted("LeaveInstance");
      }
      public void onFailure(final String message) {
        WebServiceError("LeaveInstance", message);
      }
    };

    postCommandToGameServer(LEAVE_INSTANCE_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress())),
            setInstanceCallback);
  }

  /**
   * Creates a new game instance.  The instance has a unique
   * instanceId, and the leader is the player who created it. The
   * player that creates the game automatically joins it without
   * being sent an invitation.
   *
   * The actual instance id could differ from the instanceId
   * specified because the game server will enforce uniqueness. The
   * actual instanceId will be provided to AppInventor when a
   * NewInstanceMade event triggers upon successful completion of
   * this server request.
   *
   * @param instanceId A string to use as for the instance
   * id. If no other instance exists with this id, the new instance
   * will have this id. However, since the id must be unique, if
   * another instance exists with the same one, then a number
   * will be appended to the end of this prefix.
   * @param makePublic A boolean indicating whether or not the
   * instance should be publicly viewable and able to be joined by
   * anyone.
   */
  @SimpleFunction(description = "Asks the server to create a new " +
                "instance of this game.")
  public void MakeNewInstance(final String instanceId, final boolean makePublic) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postMakeNewInstance(instanceId, makePublic); }});
  }

  private void postMakeNewInstance(final String requestedInstanceId, final Boolean makePublic) {
    AsyncCallbackPair<JSONObject> makeNewGameCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        processInstanceLists(response);
        NewInstanceMade(InstanceId());
        FunctionCompleted("MakeNewInstance");
      }
      public void onFailure(final String message) {
        WebServiceError("MakeNewInstance", message);
      }
    };

    postCommandToGameServer(NEW_INSTANCE_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, requestedInstanceId),
            new BasicNameValuePair(INSTANCE_PUBLIC_KEY, makePublic.toString())),
            makeNewGameCallback, true);
  }

  /**
   * Creates a new message and sends it to the stated recipients.
   *
   * @param type A "key" for the message. This identifies the type of
   * message so that when other players receive the message they know
   * how to properly handle it.
   * @param recipients If set to an empty list, the server will send
   * this message with a blank set of recipients, meaning that all
   * players in the instance are able to retrieve it. To limit the
   * message receipt to a single person or a group of people,
   * recipients should be a list of the email addresses of the people
   * meant to receive the message. Each email should be in one of the
   * following formats:<br>
   * "Name O. Person &ltname.o.person@gmail.com&gt"<br>
   * "name.o.person@gmail.com"
   * @param contents the contents of the message. This can be any
   * AppInventor data value.
   */
  @SimpleFunction(description = "Sends a keyed message to all " +
                "recipients in the recipients list. The message will " +
                "consist of the contents list.")
  public void SendMessage(final String type, final YailList recipients, final YailList contents) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postNewMessage(type, recipients, contents); }});
  }

  private void postNewMessage(final String type, YailList recipients, YailList contents){
    AsyncCallbackPair<JSONObject> myCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        FunctionCompleted("SendMessage");
      }
      public void onFailure(final String message) {
        WebServiceError("SendMessage", message);
      }
    };

    if (InstanceId().equals("")) {
      Info("You must have joined an instance before you can send messages.");
      return;
    }

    postCommandToGameServer(NEW_MESSAGE_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(TYPE_KEY, type),
            new BasicNameValuePair(MESSAGE_RECIPIENTS_KEY, recipients.toJSONString()),
            new BasicNameValuePair(MESSAGE_CONTENT_KEY, contents.toJSONString()),
            new BasicNameValuePair(MESSAGE_TIME_KEY, instance.getMessageTime(type))),
        myCallback);
  }

  /**
   * Submits a command to the game server. Server commands are
   * custom actions that are performed on the server. The arguments
   * required and return value of a server command depend on its
   * implementation.
   *
   * For more information about server commands, consult the game
   * server code at:
   * http://code.google.com/p/app-inventor-for-android/
   *
   * @param command The name of the server command.
   * @param arguments The arguments to pass to the server to specify
   * how to execute the command.
   */
  @SimpleFunction(description = "Sends the specified command to " +
                "the game server.")
  public void ServerCommand(final String command, final YailList arguments) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postServerCommand(command, arguments); }});
  }

  private void postServerCommand(final String command, final YailList arguments){
    AsyncCallbackPair<JSONObject> myCallback = new AsyncCallbackPair<JSONObject>() {
      public void onSuccess(final JSONObject result) {
        try {
          ServerCommandSuccess(command, JsonUtil.getListFromJsonArray(result.
              getJSONArray(MESSAGE_CONTENT_KEY)));
        } catch (JSONException e) {
          Log.w(LOG_TAG, e);
          Info("Server command response failed to parse.");
        }
        FunctionCompleted("ServerCommand");
      }

      public void onFailure(String message) {
        ServerCommandFailure(command, arguments);
        WebServiceError("ServerCommand", message);
      }
    };

    Log.d(LOG_TAG, "Going to post " + command + " with args " + arguments);
    postCommandToGameServer(SERVER_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(COMMAND_TYPE_KEY, command),
            new BasicNameValuePair(COMMAND_ARGUMENTS_KEY, arguments.toJSONString())),
        myCallback);
  }

  /**
   * Specifies the game instance id.  Taken together, the game ID and
   * the instance ID uniquely identify the game.
   *
   * @param instanceId the name of the game instance to join.
   */
  @SimpleFunction(description = "Sets InstanceId and joins the " +
                "specified instance.")
  public void SetInstance(final String instanceId) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        if (instanceId.equals("")) {
          Log.d(LOG_TAG, "Instance id set to empty string.");
          if (!InstanceId().equals("")) {
            instance = new GameInstance("");
            InstanceIdChanged("");
            FunctionCompleted("SetInstance");
          }
        } else {
          postSetInstance(instanceId);
        }
      }
    });
  }

  private void postSetInstance(String instanceId) {
    AsyncCallbackPair<JSONObject> setInstanceCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        processInstanceLists(response);
        FunctionCompleted("SetInstance");
      }
      public void onFailure(final String message) {
        WebServiceError("SetInstance", message);
      }
    };

    postCommandToGameServer(JOIN_INSTANCE_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, instanceId),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress())),
            setInstanceCallback, true);
  }

  /**
   * Specifies the game's leader. At any time, each game instance
   * has only one leader, but the leader may change over time.
   * Initially, the leader is the game instance creator. Application
   * inventors determine special properties of the leader.
   *
   * The leader can only be set by the current leader of the game.
   *
   * @param playerEmail a string containing the email address of the
   * player to become leader. The email should be in one of the
   * following formats:
   * <br>"Name O. Person &ltname.o.person@gmail.com&gt"
   * <br>"name.o.person@gmail.com".
   */
  @SimpleFunction(description = "Tells the server to set the " +
                "leader to playerId. Only the current leader may " +
                "successfully set a new leader.")
  public void SetLeader(final String playerEmail) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() { postSetLeader(playerEmail); }});
  }

  private void postSetLeader(final String newLeader) {
    AsyncCallbackPair<JSONObject> setLeaderCallback = new AsyncCallbackPair<JSONObject>(){
      public void onSuccess(final JSONObject response) {
        FunctionCompleted("SetLeader");
      }
      public void onFailure(final String message) {
        WebServiceError("SetLeader", message);
      }
    };

    if (InstanceId().equals("")) {
      Info("You must join an instance before attempting to set a leader.");
      return;
    }

    postCommandToGameServer(SET_LEADER_COMMAND,
        Lists.<NameValuePair>newArrayList(
            new BasicNameValuePair(GAME_ID_KEY, GameId()),
            new BasicNameValuePair(INSTANCE_ID_KEY, InstanceId()),
            new BasicNameValuePair(PLAYER_ID_KEY, UserEmailAddress()),
            new BasicNameValuePair(LEADER_KEY, newLeader)),
            setLeaderCallback);
  }

  //----------------------------------------------------------------
  // Activity Lifecycle Management

  /**
   * Called automatically by the operating system.
   *
   * Currently does nothing.
   */
  public void onResume() {
    Log.d(LOG_TAG, "Activity Resumed.");
  }

  /**
   * Called automatically by the operating system.
   *
   * Currently does nothing.
   */
  public void onStop() {
    Log.d(LOG_TAG, "Activity Stopped.");
  }

  //----------------------------------------------------------------
  // Utility Methods

  private void postCommandToGameServer(final String commandName,
      List<NameValuePair> params, final AsyncCallbackPair<JSONObject> callback) {
    postCommandToGameServer(commandName, params, callback, false);
  }

  private void postCommandToGameServer(final String commandName,
      final List<NameValuePair> params, final AsyncCallbackPair<JSONObject> callback,
      final boolean allowInstanceIdChange) {
    AsyncCallbackPair<JSONObject> thisCallback = new AsyncCallbackPair<JSONObject>() {
      public void onSuccess(JSONObject responseObject) {
        Log.d(LOG_TAG, "Received response for " + commandName + ": " + responseObject.toString());

        try {
          if (responseObject.getBoolean(ERROR_RESPONSE_KEY)) {
            callback.onFailure(responseObject.getString(SERVER_RETURN_VALUE_KEY));
          } else {
            String responseGameId = responseObject.getString(GAME_ID_KEY);
            if (!responseGameId.equals(GameId())) {
              Info("Incorrect game id in response: + " + responseGameId + ".");
              return;
            }
            String responseInstanceId = responseObject.getString(INSTANCE_ID_KEY);
            if (responseInstanceId.equals("")) {
              callback.onSuccess(responseObject.getJSONObject(SERVER_RETURN_VALUE_KEY));
              return;
            }

            if (responseInstanceId.equals(InstanceId())) {
              updateInstanceInfo(responseObject);
            } else {
              if (allowInstanceIdChange || InstanceId().equals("")) {
                instance = new GameInstance(responseInstanceId);
                updateInstanceInfo(responseObject);
                InstanceIdChanged(responseInstanceId);
              } else {
                Info("Ignored server response to " + commandName + " for incorrect instance " +
                    responseInstanceId + ".");
                return;
              }
            }
            callback.onSuccess(responseObject.getJSONObject(SERVER_RETURN_VALUE_KEY));
          }
        } catch (JSONException e) {
          Log.w(LOG_TAG, e);
          callback.onFailure("Failed to parse JSON response to command " + commandName);
        }
      }
      public void onFailure(String failureMessage) {
        Log.d(LOG_TAG, "Posting to server failed for " + commandName + " with arguments " +
            params + "\n Failure message: " +  failureMessage);
        callback.onFailure(failureMessage);
      }
    };

    WebServiceUtil.getInstance().postCommandReturningObject(ServiceUrl(), commandName, params,
        thisCallback);
  }

  private void updateInstanceInfo(JSONObject responseObject) throws JSONException {
    boolean newLeader = false;
    String leader = responseObject.getString(LEADER_KEY);
    List<String> receivedPlayers = JsonUtil.getStringListFromJsonArray(responseObject.
        getJSONArray(PLAYERS_LIST_KEY));

    if (!Leader().equals(leader)) {
      instance.setLeader(leader);
      newLeader = true;
    }

    PlayerListDelta playersDelta = instance.setPlayers(receivedPlayers);
    if (playersDelta != PlayerListDelta.NO_CHANGE) {
      for (final String player : playersDelta.getPlayersRemoved()) {
        PlayerLeft(player);
      }
      for (final String player : playersDelta.getPlayersAdded()) {
        PlayerJoined(player);
      }
    }

    if (newLeader) {
      NewLeader(Leader());
    }
  }
}
