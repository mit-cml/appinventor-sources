// Copyright 2009 Google Inc. All Rights Reserved.

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
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component for accessing Twitter.
 *
 * @author sharon@google.com (Sharon Perl) - added OAuth support
 */
@DesignerComponent(version = YaVersion.TWITTER_COMPONENT_VERSION,
    description = "<p>A non-visible component that enables communication " +
    "with <a href=\"http://www.twitter.com\" target=\"_blank\">Twitter</a>. " +
    "Methods are included to enabling searching (<code>SearchTwitter</code>) " +
    "or logging into (<code>Authorize</code>) Twitter.  Once a user has authorized " +
    "their Twitter account (and the authorization has been confirmed successful by the " +
    "<code>IsAuthorized</code> event), many more operations are available:<ul>" +
    "<li> Setting the status of the logged-in user (<code>SetStatus</code>)" +
    "     </li>" +
    "<li> Directing a message to a specific user " +
    "     (<code>DirectMessage</code>)</li> " +
    "<li> Receiving the most recent messages directed to the logged-in user " +
    "     (<code>RequestDirectMessages</code>)</li> " +
    "<li> Following a specific user (<code>Follow</code>)</li>" +
    "<li> Ceasing to follow a specific user (<code>StopFollowing</code>)</li>" +
    "<li> Getting a list of users following the logged-in user " +
    "     (<code>RequestFollowers</code>)</li> " +
    "<li> Getting the most recent messages of users followed by the " +
    "     logged-in user (<code>RequestFriendTimeline</code>)</li> " +
    "<li> Getting the most recent mentions of the logged-in user " +
    "     (<code>RequestMentions</code>)</li></ul></p> " +
    "<p>You must obtain a Comsumer Key and Consumer Secret for Twitter authorization " +
    " specific to your app from http://twitter.com/oauth_clients/new </p>",
    category = ComponentCategory.SOCIAL,
    nonVisible = true,
    iconName = "images/twitter.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class Twitter extends AndroidNonvisibleComponent
    implements ActivityResultListener, Component {
  private static final String ACCESS_TOKEN_TAG = "TwitterOauthAccessToken";
  private static final String ACCESS_SECRET_TAG = "TwitterOauthAccessSecret";
  private static final String MAX_CHARACTERS = "160";
  private static final String URL_HOST = "twitter";
  private static final String CALLBACK_URL = Form.APPINVENTOR_URL_SCHEME + "://"
    + URL_HOST;
  private static final String WEBVIEW_ACTIVITY_CLASS = WebViewActivity.class.getName();

  // lock protects fields twitter, requestToken, accessToken, userId,
  // sharedPreferences
  private final Object lock = new Object();
  private volatile twitter4j.Twitter twitter;
  private volatile RequestToken requestToken;
  private volatile AccessToken accessToken;
  private volatile String userName = "";
  private final SharedPreferences sharedPreferences;

  // twitterLock synchronizes uses of any/all twitter4j.Twitter objects
  // in this class. As far as I can tell, these objects are not thread-safe
  private final Object twitterLock = new Object();

  // the following fields should only be accessed from the UI thread
  private volatile String consumerKey = "";
  private volatile String consumerSecret = "";
  private final List<String> mentions;
  private final List<String> followers;
  private final List<List<String>> timeline;
  private final List<String> directMessages;
  private final List<String> searchResults;

  // the following final fields are not synchronized
  private final int requestCode;
  private final ComponentContainer container;
  private final Handler handler;

  // TODO(sharon): twitter4j apparently has an asynchronous interface (AsynchTwitter).
  // We should consider whether it has any advantages over AsynchUtil.

  /**
   *  The maximum number of mentions returned by the following methods:
   *
   * <table>
   *    <tr>
   *        <td>component</td>
   *        <td>twitter4j library</td>
   *        <td>twitter API</td>
   *     </tr>
   *     <tr>
   *         <td>RequestMentions</td>
   *         <td>getMentions</td>
   *         <td>statuses/mentions</td>
   *     </tr>
   *     <tr>
   *         <td>RequestDirectMessages</td>
   *         <td>getDirectMessages</td>
   *         <td>direct_messages</td>
   *     </tr>
   * </table>
   */
  private static final String MAX_MENTIONS_RETURNED = "20";

  public Twitter(ComponentContainer container) {
    super(container.$form());
    this.container = container;
    handler = new Handler();

    mentions = new ArrayList<String>();
    followers = new ArrayList<String>();
    timeline = new ArrayList<List<String>>();
    directMessages = new ArrayList<String>();
    searchResults = new ArrayList<String>();

    sharedPreferences = container.$context().getSharedPreferences("Twitter",
        Context.MODE_PRIVATE);
    accessToken = retrieveAccessToken();

    requestCode = form.registerForActivityResult(this);
 }

  /**
   * Logs in to Twitter with a username and password.
   */
  @SimpleFunction(userVisible = false,
      description = "Twitter's API no longer supports login via username and " +
      "password. Use the Authorize call instead.")
  public void Login(String username, String password) {
    form.dispatchErrorOccurredEvent(this, "Login",
        ErrorMessages.ERROR_TWITTER_UNSUPPORTED_LOGIN_FUNCTION);
  }

  /**
   * Indicates when the login has been successful.
   */
  @SimpleEvent(description =
               "This event is raised after the program calls " +
               "<code>Authorize</code> if the authorization was successful.  " +
               "It is also called after a call to <code>CheckAuthorized</code> " +
               "if we already have a valid access token. " +
               "After this event has been raised, any other method for this " +
               "component can be called.")
  public void IsAuthorized() {
    EventDispatcher.dispatchEvent(this, "IsAuthorized");
  }

  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "The user name of the authorized user. Empty if " +
      "there is no authorized user.")
  public String Username() {
    synchronized (lock) {
      return userName;
    }
  }

  /**
   * ConsumerKey property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String ConsumerKey() {
    return consumerKey;
  }

  /**
   * ConsumerKey property setter method: sets the consumer key to be used
   * when authorizing with Twitter via OAuth.
   *
   * @param consumerKey the key for use in Twitter OAuth
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void ConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * ConsumerSecret property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String ConsumerSecret() {
    return consumerSecret;
  }

  /**
   * ConsumerSecret property setter method: sets the consumer secret to be used
   * when authorizing with Twitter via OAuth.
   *
   * @param consumerSecret the secret for use in Twitter OAuth
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void ConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * Authenticate to Twitter using OAuth
   */
  @SimpleFunction(
      description = "Redirects user to login to Twitter via the Web browser using " +
                    "the OAuth protocol if we don't already have authorization.")
  public void Authorize() {
    if (consumerKey.length() == 0 || consumerSecret.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "Authorize",
          ErrorMessages.ERROR_TWITTER_BLANK_CONSUMER_KEY_OR_SECRET);
      return;
    }
    final String myConsumerKey = consumerKey;
    final String myConsumerSecret = consumerSecret;
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      if (twitter == null) {
        twitter = new twitter4j.Twitter();
      }
      myTwitter = twitter;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        if (checkAccessToken(myConsumerKey, myConsumerSecret)) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              IsAuthorized();
            }
          });
          return;
        }
        try {
          // potentially time-consuming calls
          RequestToken newRequestToken;
          synchronized (twitterLock) {
            myTwitter.setOAuthConsumer(myConsumerKey, myConsumerSecret);
            newRequestToken = myTwitter.getOAuthRequestToken(CALLBACK_URL);
          }
          String authURL = newRequestToken.getAuthorizationURL();
          synchronized (lock) {
            requestToken = newRequestToken;  // request token will be needed to get access token
          }
          Intent browserIntent = new Intent(Intent.ACTION_MAIN, Uri.parse(authURL));
          browserIntent.setClassName(container.$context(), WEBVIEW_ACTIVITY_CLASS);
          container.$context().startActivityForResult(browserIntent, requestCode);
        } catch (TwitterException e) {
          Log.i("Twitter", "Got exception: " + e.getMessage());
          e.printStackTrace();
          form.dispatchErrorOccurredEvent(Twitter.this, "Authorize",
              ErrorMessages.ERROR_TWITTER_EXCEPTION, e.getMessage());
          DeAuthorize();  // clean up
        }
      }
    });
  }

  /**
   * Check whether we already have a valid Twitter access token
   */
  @SimpleFunction(
      description = "Checks whether we already have access, and if so, causes "
          + "IsAuthorized event handler to be called.")
  public void CheckAuthorized() {
    final String myConsumerKey = consumerKey;
    final String myConsumerSecret = consumerSecret;
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        if (checkAccessToken(myConsumerKey, myConsumerSecret)) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              IsAuthorized();
            }
          });
        }
      }
    });
  }

  /*
   * Get result from starting WebView activity to authorize access
   */
  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    Log.i("Twitter", "Got result " + resultCode);
    if (data != null) {
      Uri uri = data.getData();
      if (uri != null) {
        Log.i("Twitter", "Intent URI: " + uri.toString());
        final String oauthVerifier = uri.getQueryParameter("oauth_verifier");
        final RequestToken myRequestToken;
        final twitter4j.Twitter myTwitter;
        synchronized (lock) {
          myRequestToken = requestToken;
          myTwitter = twitter;
        }
        if (myTwitter == null) {
          Log.e("Twitter", "twitter field is unexpectedly null");
          new RuntimeException().printStackTrace();
          form.dispatchErrorOccurredEvent(this, "Authorize",
              ErrorMessages.ERROR_TWITTER_UNABLE_TO_GET_ACCESS_TOKEN,
              "internal error: can't access Twitter library");
        }
        if (myRequestToken != null && oauthVerifier != null && oauthVerifier.length() != 0) {
          AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
              try {
                AccessToken resultAccessToken;
                synchronized (twitterLock) {
                  resultAccessToken = myTwitter.getOAuthAccessToken(myRequestToken,
                      oauthVerifier);
                }
                synchronized (lock) {
                  accessToken = resultAccessToken;
                  userName = accessToken.getScreenName();
                  saveAccessToken(resultAccessToken);
                }
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    IsAuthorized();
                  }
                });
              } catch (TwitterException e) {
                Log.e("Twitter", "Got exception: " + e.getMessage());
                e.printStackTrace();
                form.dispatchErrorOccurredEvent(Twitter.this, "Authorize",
                    ErrorMessages.ERROR_TWITTER_UNABLE_TO_GET_ACCESS_TOKEN, e.getMessage());
                DeAuthorize();  // clean up
              }
            }
          });
        } else {
          form.dispatchErrorOccurredEvent(this, "Authorize",
              ErrorMessages.ERROR_TWITTER_AUTHORIZATION_FAILED);
          DeAuthorize();  // clean up
        }
      } else {
        Log.e("Twitter", "uri retured from WebView activity was unexpectedly null");
      }
    } else {
      Log.e("Twitter", "intent retured from WebView activity was unexpectedly null");
    }
  }

  // Call with twitterLock held
  private void saveAccessToken(AccessToken accessToken) {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    if (accessToken == null) {
      sharedPrefsEditor.remove(ACCESS_TOKEN_TAG);
      sharedPrefsEditor.remove(ACCESS_SECRET_TAG);
    } else {
      sharedPrefsEditor.putString(ACCESS_TOKEN_TAG, accessToken.getToken());
      sharedPrefsEditor.putString(ACCESS_SECRET_TAG, accessToken.getTokenSecret());
    }
    sharedPrefsEditor.commit();
  }

  // call with twitterLock held
  private AccessToken retrieveAccessToken() {
    String token = sharedPreferences.getString(ACCESS_TOKEN_TAG, "");
    String secret = sharedPreferences.getString(ACCESS_SECRET_TAG, "");
    if (token.length() == 0 || secret.length() == 0) {
      return null;
    }
    return new AccessToken(token, secret);
  }

  /**
   * Remove authentication for this app instance
   */
  @SimpleFunction(
      description = "Removes Twitter authorization from this running app instance")
  public void DeAuthorize() {
    final twitter4j.Twitter oldTwitter;
    synchronized (lock) {
      requestToken = null;
      accessToken = null;
      userName = "";
      oldTwitter = twitter;
      twitter = null;  // setting twitter to null gives us a quick check
                       // that we don't have an authorized version around.
      saveAccessToken(accessToken);
    }
    // clear the access token from the old twitter instance, just in case
    // someone stashed it away.
    if (oldTwitter != null) {
      synchronized (twitterLock) {
        oldTwitter.setOAuthAccessToken(null);
      }
    }
  }

  /**
   * Sets the status of the currently logged in user.
   */
  @SimpleFunction(
      description = "This updates the logged-in user's status to the " +
      "specified Text, which will be trimmed if it exceeds " +
      MAX_CHARACTERS + " characters. " +
      "<p><u>Requirements</u>: This should only be called after the " +
      "<code>IsAuthorized</code> event has been raised, indicating that the " +
      "user has successfully logged in to Twitter.</p>")
  public void SetStatus(final String status) {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myTwitter = twitter;
    }
    if (myTwitter == null) {
      form.dispatchErrorOccurredEvent(this, "SetStatus",
          ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, "Need to login?");
      return;
    }
    // TODO(sharon): note that if the user calls DeAuthorize immediately after
    // SetStatus it is possible that the DeAuthorize call can slip in
    // and invalidate the authorization credentials for myTwitter, causing
    // the call below to fail. If we want to prevent this we could consider
    // using an ExecutorService object to serialize calls to Twitter.
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          synchronized (twitterLock) {
            myTwitter.updateStatus(status);
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "SetStatus",
              ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Gets the most recent messages where your username is mentioned.
   */
  @SimpleFunction(
      description = "Requests the " + MAX_MENTIONS_RETURNED + " most " +
      "recent mentions of the logged-in user.  When the mentions have been " +
      "retrieved, the system will raise the <code>MentionsReceived</code> " +
      "event and set the <code>Mentions</code> property to the list of " +
      "mentions." +
      "<p><u>Requirements</u>: This should only be called after the " +
      "<code>IsAuthorized</code> event has been raised, indicating that the " +
      "user has successfully logged in to Twitter.</p>")
  public void RequestMentions() {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myTwitter = twitter;
    }
    if (myTwitter == null) {
      form.dispatchErrorOccurredEvent(this, "RequestMentions",
          ErrorMessages.ERROR_TWITTER_REQUEST_MENTIONS_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Status> replies = Collections.emptyList();
      public void run() {
        try {
          synchronized (twitterLock) {
            replies = myTwitter.getMentions();
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestMentions",
              ErrorMessages.ERROR_TWITTER_REQUEST_MENTIONS_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              mentions.clear();
              for (Status status : replies) {
                mentions.add(status.getUser().getScreenName() + " " + status.getText());
              }
              MentionsReceived(mentions);
            }
          });
        }
      }
    });
  }

  /**
   * Indicates when all the mentions requested through
   * {@link #RequestMentions()} have been received.
   */
  @SimpleEvent(
      description =
      "This event is raised when the mentions of the logged-in user " +
      "requested through <code>RequestMentions</code> have been retrieved.  " +
      "A list of the mentions can then be found in the <code>mentions</code> " +
      "parameter or the <code>Mentions</code> property.")
  public void MentionsReceived(final List<String> mentions) {
    EventDispatcher.dispatchEvent(this, "MentionsReceived", mentions);
  }

  @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "This property contains a list of mentions of the " +
        "logged-in user.  Initially, the list is empty.  To set it, the " +
        "program must: <ol> " +
        "<li> Call the <code>Authorize</code> method.</li> " +
        "<li> Wait for the <code>IsAuthorized</code> event.</li> " +
        "<li> Call the <code>RequestMentions</code> method.</li> " +
        "<li> Wait for the <code>MentionsReceived</code> event.</li></ol>\n" +
        "The value of this property will then be set to the list of mentions " +
        "(and will maintain its value until any subsequent calls to " +
        "<code>RequestMentions</code>).")
  public List<String> Mentions() {
    return mentions;
  }

  /**
   * Gets who is following you.
   */
  @SimpleFunction
  public void RequestFollowers() {
    final String myUserId;
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myUserId = userName;
      myTwitter = twitter;
    }
    if (myTwitter == null || myUserId.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "RequestFollowers",
          ErrorMessages.ERROR_TWITTER_REQUEST_FOLLOWERS_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<User> friends = Collections.emptyList();

      public void run() {
        try {
          synchronized (twitterLock) {
            friends = myTwitter.getFollowersStatuses(myUserId, new Paging());
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestFollowers",
              ErrorMessages.ERROR_TWITTER_REQUEST_FOLLOWERS_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              followers.clear();
              for (User user : friends) {
                followers.add(user.getName());
              }
              FollowersReceived(followers);
            }
          });
        }
      }
    });
  }

  /**
   * Indicates when all of your followers requested through
   * {@link #RequestFollowers()} have been received.
   */
  @SimpleEvent(
      description = "This event is raised when all of the followers of the " +
      "logged-in user requested through <code>RequestFollowers</code> have " +
      "been retrieved. A list of the followers can then be found in the " +
      "<code>followers</code> parameter or the <code>Followers</code> " +
      "property.")
  public void FollowersReceived(final List<String> followers) {
    EventDispatcher.dispatchEvent(this, "FollowersReceived", followers);
  }

  @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "This property contains a list of the followers of the " +
        "logged-in user.  Initially, the list is empty.  To set it, the " +
        "program must: <ol> " +
        "<li> Call the <code>Authorize</code> method.</li> " +
        "<li> Wait for the <code>IsAuthorized</code> event.</li> " +
        "<li> Call the <code>RequestFollowers</code> method.</li> " +
        "<li> Wait for the <code>FollowersReceived</code> event.</li></ol>\n" +
        "The value of this property will then be set to the list of " +
        "followers (and maintain its value until any subsequent call to " +
        "<code>RequestFollowers</code>).")
  public List<String> Followers() {
    return followers;
  }

  /**
   * Gets the most recent messages sent directly to you.
   */
  @SimpleFunction(
      description = "Requests the " + MAX_MENTIONS_RETURNED + " most " +
      "recent direct messages sent to the logged-in user.  When the " +
      "messages have been retrieved, the system will raise the " +
      "<code>DirectMessagesReceived</code> event and set the " +
      "<code>DirectMessages</code> property to the list of messages." +
      "<p><u>Requirements</u>: This should only be called after the " +
      "<code>IsAuthorized</code> event has been raised, indicating that the " +
      "user has successfully logged in to Twitter.</p>")
  public void RequestDirectMessages() {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myTwitter = twitter;
    }
    if (myTwitter == null) {
      form.dispatchErrorOccurredEvent(this, "RequestDirectMessages",
          ErrorMessages.ERROR_TWITTER_REQUEST_DIRECT_MESSAGES_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<DirectMessage> messages = Collections.emptyList();

      @Override
      public void run() {
        try {
          synchronized (twitterLock) {
            messages = myTwitter.getDirectMessages();
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestDirectMessages",
              ErrorMessages.ERROR_TWITTER_REQUEST_DIRECT_MESSAGES_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            @Override
            public void run() {
              directMessages.clear();
              for (DirectMessage message : messages) {
                directMessages.add(message.getSenderScreenName() + " " + message.getText());
              }
              DirectMessagesReceived(directMessages);
            }
          });
        }
      }

    });
  }

  /**
   * Indicates when all the direct messages requested through
   * {@link #RequestDirectMessages()} have been received.
   */
  @SimpleEvent(
      description = "This event is raised when the recent messages " +
      "requested through <code>RequestDirectMessages</code> have " +
      "been retrieved. A list of the messages can then be found in the " +
      "<code>messages</code> parameter or the <code>Messages</code> " +
      "property.")
  public void DirectMessagesReceived(final List<String> messages) {
    EventDispatcher.dispatchEvent(this, "DirectMessagesReceived", messages);
  }

  @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "This property contains a list of the most recent " +
        "messages mentioning the logged-in user.  Initially, the list is " +
        "empty.  To set it, the program must: <ol> " +
        "<li> Call the <code>Authorize</code> method.</li> " +
        "<li> Wait for the <code>Authorized</code> event.</li> " +
        "<li> Call the <code>RequestDirectMessages</code> method.</li> " +
        "<li> Wait for the <code>DirectMessagesReceived</code> event.</li>" +
        "</ol>\n" +
        "The value of this property will then be set to the list of direct " +
        "messages retrieved (and maintain that value until any subsequent " +
        "call to <code>RequestDirectMessages</code>).")
  public List<String> DirectMessages() {
    return directMessages;
  }

  /**
   * Sends a direct message to a specified username.
   */
  @SimpleFunction(
      description = "This sends a direct (private) message to the specified " +
      "user.  The message will be trimmed if it exceeds " +  MAX_CHARACTERS +
      "characters. " +
      "<p><u>Requirements</u>: This should only be called after the " +
      "<code>IsAuthorized</code> event has been raised, indicating that the " +
      "user has successfully logged in to Twitter.</p>")
  public void DirectMessage(final String user, final String message) {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      if (twitter == null) {
        form.dispatchErrorOccurredEvent(this, "DirectMessage",
            ErrorMessages.ERROR_TWITTER_DIRECT_MESSAGE_FAILED, "Need to login?");
        return;
      }
      myTwitter = twitter;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          synchronized (twitterLock) {
            myTwitter.sendDirectMessage(user, message);
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "DirectMessage",
              ErrorMessages.ERROR_TWITTER_DIRECT_MESSAGE_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Starts following a user.
   */
  @SimpleFunction
  public void Follow(final String user) {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      if (twitter == null) {
        form.dispatchErrorOccurredEvent(this, "Follow",
            ErrorMessages.ERROR_TWITTER_FOLLOW_FAILED, "Need to login?");
        return;
      }
      myTwitter = twitter;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          synchronized (twitterLock) {
            myTwitter.enableNotification(user);
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "Follow",
              ErrorMessages.ERROR_TWITTER_FOLLOW_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Stops following a user.
   */
  @SimpleFunction
  public void StopFollowing(final String user) {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myTwitter = twitter;
    }
    if (myTwitter == null) {
      form.dispatchErrorOccurredEvent(this, "StopFollowing",
          ErrorMessages.ERROR_TWITTER_STOP_FOLLOWING_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          synchronized (twitterLock) {
            myTwitter.disableNotification(user);
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "StopFollowing",
              ErrorMessages.ERROR_TWITTER_STOP_FOLLOWING_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Gets the most recent 20 messages of usernames that you follow.
   */
  @SimpleFunction
  public void RequestFriendTimeline() {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      myTwitter = twitter;
    }
    if (myTwitter == null) {
      form.dispatchErrorOccurredEvent(this, "RequestFriendTimeline",
          ErrorMessages.ERROR_TWITTER_REQUEST_FRIEND_TIMELINE_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Status> messages = Collections.emptyList();

      public void run() {
        try {
          messages = myTwitter.getFriendsTimeline();
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestFriendTimeline",
              ErrorMessages.ERROR_TWITTER_REQUEST_FRIEND_TIMELINE_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              timeline.clear();
              for (Status message : messages) {
                List<String> status = new ArrayList<String>();
                status.add(message.getUser().getName());
                status.add(message.getText());
                timeline.add(status);
              }
              FriendTimelineReceived(timeline);
            }
          });
        }
      }
    });
  }

  /**
   * Indicates when the friend timeline requested through
   * {@link #RequestFriendTimeline()} has been received.
   */
  @SimpleEvent(
      description = "This event is raised when the messages " +
      "requested through <code>RequestFriendTimeline</code> have " +
      "been retrieved. The <code>timeline</code> parameter and the " +
      "<code>Timeline</code> property will contain a list of lists, where " +
      "each sub-list contains a status update of the form (username message)")
  public void FriendTimelineReceived(final List<List<String>> timeline) {
    EventDispatcher.dispatchEvent(this, "FriendTimelineReceived", timeline);
  }

  @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "This property contains the 20 most recent messages of " +
        "users being followed.  Initially, the list is empty.  To set it, " +
        "the program must: <ol> " +
        "<li> Call the <code>Authorize</code> method.</li> " +
        "<li> Wait for the <code>IsAuthorized</code> event.</li> " +
        "<li> Specify users to follow with one or more calls to the " +
        "<code>Follow</code> method.</li> " +
        "<li> Call the <code>RequestFriendTimeline</code> method.</li> " +
        "<li> Wait for the <code>FriendTimelineReceived</code> event.</li> " +
        "</ol>\n" +
        "The value of this property will then be set to the list of messages " +
        "(and maintain its value until any subsequent call to " +
        "<code>RequestFriendTimeline</code>.")
  public List<List<String>> FriendTimeline() {
    return timeline;
  }

  /**
   * Search for tweets or labels
   */
  @SimpleFunction
  public void SearchTwitter(final String query) {
    final twitter4j.Twitter myTwitter;
    synchronized (lock) {
      if (twitter == null) {
        // We don't need to login so open an anonymous Twitter just for this
        // operation
        myTwitter = new twitter4j.Twitter();
      } else {
        myTwitter = twitter;
      }
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Tweet> tweets = Collections.emptyList();

      public void run() {
        try {
          synchronized (twitterLock) {
            tweets = myTwitter.search(new Query(query)).getTweets();
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "SearchTwitter",
              ErrorMessages.ERROR_TWITTER_SEARCH_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              searchResults.clear();
              for (Tweet tweet : tweets) {
                searchResults.add(tweet.getFromUser() + " " + tweet.getText());
              }
              SearchSuccessful(searchResults);
            }
          });
        }
      }
    });
  }

  /**
   * Indicates when the search requested through {@link #SearchTwitter(String)}
   * has completed.
   */
  @SimpleEvent(
      description = "This event is raised when the results of the search " +
      "requested through <code>SearchSuccessful</code> have " +
      "been retrieved. A list of the results can then be found in the " +
      "<code>results</code> parameter or the <code>Results</code> " +
      "property.")
  public void SearchSuccessful(final List<String> searchResults) {
    EventDispatcher.dispatchEvent(this, "SearchSuccessful", searchResults);
  }

  @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "This property, which is initially empty, is set to a " +
        "list of search results after the program: <ol>" +
        "<li>Calls the <code>SearchTwitter</code> method.</li> " +
        "<li>Waits for the <code>SearchSuccessful</code> event.</li></ol>\n" +
        "The value of the property will then be the same as the parameter to " +
        "<code>SearchSuccessful</code>.  Note that it is not necessary to " +
        "call the <code>Authorize</code> method before calling " +
        "<code>SearchTwitter</code>.")
  public List<String> SearchResults() {
    return searchResults;
  }

  /**
   * Check whether accessToken is valid. This call can take a while
   * because it makes a request to Twitter, so it should be called from a
   * non-UI thread.
   * @return true if accessToken is valid, false otherwise.
   */
  private boolean checkAccessToken(String myConsumerKey, String myConsumerSecret) {
    twitter4j.Twitter myTwitter;
    AccessToken myAccessToken;
    synchronized (lock) {
      if (accessToken == null) {
        return false;
      }
      myAccessToken = accessToken;
      if (twitter == null) {
        twitter = new twitter4j.Twitter();
      }
      myTwitter = twitter;
    }
    try {
      User user;
      synchronized (twitterLock) {
        myTwitter.setOAuthConsumer(myConsumerKey, myConsumerSecret);
        myTwitter.setOAuthAccessToken(myAccessToken);
        user = myTwitter.verifyCredentials();
      }
      synchronized (lock) {
        userName = user.getScreenName();
      }
      Log.i("Twitter", "Saved accessToken is valid. UserId is " + userName);
      return true;  // already have access
    } catch (TwitterException e) {
      synchronized (lock) {
        accessToken = null;  // clear invalid token
        userName = "";
        saveAccessToken(accessToken);
      }
      Log.i("Twitter", "Saved access token is not valid---clearing it in shared prefs");
      return false;
    }

  }
}
