// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;

import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * Component for accessing Twitter.
 *
 * @author sharon@google.com (Sharon Perl) - added OAuth support
 * @author ajcolter@gmail.com (Aubrey Colter) - added the twitter4j 2.2.6 jars
 * @author josmasflores@gmail.com (Jose Dominguez) - added the twitter4j 3.0.3 jars and fixed auth bug 2413
 * @author edwinhzhang@gmail.com (Edwin Zhang) - added twitter4j-media-support-3.03 jar, status + image upload
 */
@DesignerComponent(version = YaVersion.TWITTER_COMPONENT_VERSION, description = "A non-visible component that enables communication "
    + "with <a href=\"http://www.twitter.com\" target=\"_blank\">Twitter</a>. "
    + "Once a user has logged into their Twitter account (and the authorization has been confirmed successful by the "
    + "<code>IsAuthorized</code> event), many more operations are available:<ul>"
    + "<li> Searching Twitter for tweets or labels (<code>SearchTwitter</code>)</li>\n"
    + "<li> Sending a Tweet (<code>Tweet</code>)"
    + "     </li>\n"
    + "<li> Sending a Tweet with an Image (<code>TweetWithImage</code>)"
    + "     </li>\n"
    + "<li> Directing a message to a specific user "
    + "     (<code>DirectMessage</code>)</li>\n "
    + "<li> Receiving the most recent messages directed to the logged-in user "
    + "     (<code>RequestDirectMessages</code>)</li>\n "
    + "<li> Following a specific user (<code>Follow</code>)</li>\n"
    + "<li> Ceasing to follow a specific user (<code>StopFollowing</code>)</li>\n"
    + "<li> Getting a list of users following the logged-in user "
    + "     (<code>RequestFollowers</code>)</li>\n "
    + "<li> Getting the most recent messages of users followed by the "
    + "     logged-in user (<code>RequestFriendTimeline</code>)</li>\n "
    + "<li> Getting the most recent mentions of the logged-in user "
    + "     (<code>RequestMentions</code>)</li></ul></p>\n "
    + "<p>You must obtain a Consumer Key and Consumer Secret for Twitter authorization "
    + " specific to your app from http://twitter.com/oauth_clients/new",
    category = ComponentCategory.SOCIAL, nonVisible = true, iconName = "images/twitter.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "twitter4j.jar," + "twitter4jmedia.jar")
@UsesActivities(activities = {
    @ActivityElement(name = "com.google.appinventor.components.runtime.WebViewActivity",
                     configChanges = "orientation|keyboardHidden",
                     screenOrientation = "behind",
                     intentFilters = {
                         @IntentFilterElement(actionElements = {
                             @ActionElement(name = "android.intent.action.MAIN")
                         })
    })
})
public final class Twitter extends AndroidNonvisibleComponent implements
    ActivityResultListener, Component {
  private static final String ACCESS_TOKEN_TAG = "TwitterOauthAccessToken";
  private static final String ACCESS_SECRET_TAG = "TwitterOauthAccessSecret";
  private static final String MAX_CHARACTERS = "160";
  private static final String URL_HOST = "twitter";
  private static final String CALLBACK_URL = Form.APPINVENTOR_URL_SCHEME
      + "://" + URL_HOST;
  private static final String WEBVIEW_ACTIVITY_CLASS = WebViewActivity.class
      .getName();

  // the following fields should only be accessed from the UI thread
  private String consumerKey = "";
  private String consumerSecret = "";
  private String TwitPic_API_Key = "";
  private final List<String> mentions;
  private final List<String> followers;
  private final List<List<String>> timeline;
  private final List<String> directMessages;
  private final List<String> searchResults;

  // the following final fields are not synchronized -- twitter4j is thread
  // safe as of 2.2.6
  private twitter4j.Twitter twitter;
  private RequestToken requestToken;
  private AccessToken accessToken;
  private String userName = "";
  private final SharedPreferences sharedPreferences;
  private final int requestCode;
  private final ComponentContainer container;
  private final Handler handler;

  // TODO(sharon): twitter4j apparently has an asynchronous interface
  // (AsynchTwitter).
  // We should consider whether it has any advantages over AsynchUtil.

  /**
   * The maximum number of mentions returned by the following methods:
   *
   * <table>
   * <tr>
   * <td>component</td>
   * <td>twitter4j library</td>
   * <td>twitter API</td>
   * </tr>
   * <tr>
   * <td>RequestMentions</td>
   * <td>getMentions</td>
   * <td>statuses/mentions</td>
   * </tr>
   * <tr>
   * <td>RequestDirectMessages</td>
   * <td>getDirectMessages</td>
   * <td>direct_messages</td>
   * </tr>
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
  // @Deprecated
  // [lyn, 2015/12/30] Removed @Deprecated annotation for this method, which was deprecated in AI1
  // by setting userVisible = false. The @Deprecated annotation should only be used for
  // events/methods/properties deprecated in AI2. The problem with using it for methods deprecated
  // in AI1 is that the names of such methods no longer exist in OdeMessages.java, but the
  // AI2 bad blocks mechanism (which uses the @Deprecated annotation) requires the method names
  // to exist and be translatable so that they can appear in a block marked bad.
  @SimpleFunction(userVisible = false, description = "Twitter's API no longer supports login via username and "
      + "password. Use the Authorize call instead.")
  public void Login(String username, String password) {
    form.dispatchErrorOccurredEvent(this, "Login",
        ErrorMessages.ERROR_TWITTER_UNSUPPORTED_LOGIN_FUNCTION);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "The user name of the authorized user. Empty if "
      + "there is no authorized user.")
  public String Username() {
    return userName;
  }

  /**
   * ConsumerKey property getter method.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String ConsumerKey() {
    return consumerKey;
  }

  /**
   * ConsumerKey property setter method: sets the consumer key to be used when
   * authorizing with Twitter via OAuth.
   *
   * @param consumerKey
   *          the key for use in Twitter OAuth
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "The the consumer key to be used when authorizing with Twitter via OAuth.")
  public void ConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * ConsumerSecret property getter method.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String ConsumerSecret() {
    return consumerSecret;
  }

  /**
   * ConsumerSecret property setter method: sets the consumer secret to be used
   * when authorizing with Twitter via OAuth.
   *
   * @param consumerSecret
   *          the secret for use in Twitter OAuth
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description="The consumer secret to be used when authorizing with Twitter via OAuth")
  public void ConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * TwitPicAPIkey property getter method.
   */
  @Deprecated
  @SimpleProperty( // [lyn 2015/12/30] removed userVisible = false, which is superseded by @Deprecated
      category = PropertyCategory.BEHAVIOR)
  public String TwitPic_API_Key() {
     return TwitPic_API_Key;
  }

  /**
   * TwitPicAPIkey property setter method: sets the TwitPicAPIkey to be used
   * for image uploading with twitter.
   *
   * @param TwitPic_API_Key
   *          the API Key for image uploading, given by TwitPic
   */
  @Deprecated
  // Hide the deprecated property from the Designer
  //@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty( // [lyn 2015/12/30] removed userVisible = false, which is superseded by @Deprecated
      category = PropertyCategory.BEHAVIOR,
      description="The API Key for image uploading, provided by TwitPic.")
  public void TwitPic_API_Key(String TwitPic_API_Key) {
    this.TwitPic_API_Key = TwitPic_API_Key;
  }

  /**
   * Indicates when the login has been successful.
   */
  @SimpleEvent(description = "This event is raised after the program calls "
      + "<code>Authorize</code> if the authorization was successful.  "
      + "It is also called after a call to <code>CheckAuthorized</code> "
      + "if we already have a valid access token. "
      + "After this event has been raised, any other method for this "
      + "component can be called.")
  public void IsAuthorized() {
    EventDispatcher.dispatchEvent(this, "IsAuthorized");
  }

  /**
   * Authenticate to Twitter using OAuth
   */
  @SimpleFunction(description = "Redirects user to login to Twitter via the Web browser using "
      + "the OAuth protocol if we don't already have authorization.")
  public void Authorize() {
    if (consumerKey.length() == 0 || consumerSecret.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "Authorize",
          ErrorMessages.ERROR_TWITTER_BLANK_CONSUMER_KEY_OR_SECRET);
      return;
    }
    if (twitter == null) {
      twitter = new TwitterFactory().getInstance();
    }
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
          return;
        }
        try {
          // potentially time-consuming calls
          RequestToken newRequestToken;
          twitter.setOAuthConsumer(myConsumerKey, myConsumerSecret);
          newRequestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
          String authURL = newRequestToken.getAuthorizationURL();
          requestToken = newRequestToken; // request token will be
          // needed to get access token
          Intent browserIntent = new Intent(Intent.ACTION_MAIN, Uri
              .parse(authURL));
          browserIntent.setClassName(container.$context(),
              WEBVIEW_ACTIVITY_CLASS);
          container.$context().startActivityForResult(browserIntent,
              requestCode);
        } catch (TwitterException e) {
          Log.i("Twitter", "Got exception: " + e.getMessage());
          e.printStackTrace();
          form.dispatchErrorOccurredEvent(Twitter.this, "Authorize",
              ErrorMessages.ERROR_TWITTER_EXCEPTION, e.getMessage());
          DeAuthorize(); // clean up
        } catch (IllegalStateException ise){ //This should never happen cause it should return
          // at the if (checkAccessToken...). We mark as an error but let continue
          Log.e("Twitter", "OAuthConsumer was already set: launch IsAuthorized()");
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

  /**
   * Check whether we already have a valid Twitter access token
   */
  @SimpleFunction(description = "Checks whether we already have access, and if so, causes "
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
        if (twitter == null) {
          Log.e("Twitter", "twitter field is unexpectedly null");
          form.dispatchErrorOccurredEvent(this, "Authorize",
              ErrorMessages.ERROR_TWITTER_UNABLE_TO_GET_ACCESS_TOKEN,
              "internal error: can't access Twitter library");
          new RuntimeException().printStackTrace();
        }
        if (requestToken != null && oauthVerifier != null
            && oauthVerifier.length() != 0) {
          AsynchUtil.runAsynchronously(new Runnable() {
            public void run() {
              try {
                AccessToken resultAccessToken;
                resultAccessToken = twitter.getOAuthAccessToken(requestToken,
                    oauthVerifier);
                accessToken = resultAccessToken;
                userName = accessToken.getScreenName();
                saveAccessToken(resultAccessToken);
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
                    ErrorMessages.ERROR_TWITTER_UNABLE_TO_GET_ACCESS_TOKEN,
                    e.getMessage());
                deAuthorize(); // clean up
              }
            }
          });
        } else {
          form.dispatchErrorOccurredEvent(this, "Authorize",
              ErrorMessages.ERROR_TWITTER_AUTHORIZATION_FAILED);
          deAuthorize(); // clean up
        }
      } else {
        Log.e("Twitter", "uri returned from WebView activity was unexpectedly null");
        deAuthorize(); // clean up so we can call Authorize again
      }
    } else {
      Log.e("Twitter", "intent returned from WebView activity was unexpectedly null");
      deAuthorize(); // clean up so we can call Authorize again
    }
  }

  private void saveAccessToken(AccessToken accessToken) {
    final SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
    if (accessToken == null) {
      sharedPrefsEditor.remove(ACCESS_TOKEN_TAG);
      sharedPrefsEditor.remove(ACCESS_SECRET_TAG);
    } else {
      sharedPrefsEditor.putString(ACCESS_TOKEN_TAG, accessToken.getToken());
      sharedPrefsEditor.putString(ACCESS_SECRET_TAG,
          accessToken.getTokenSecret());
    }
    sharedPrefsEditor.commit();
  }

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
  @SimpleFunction(description = "Removes Twitter authorization from this running app instance")
  public void DeAuthorize() {
    deAuthorize();
  }

  private void deAuthorize() {
    final twitter4j.Twitter oldTwitter;
    requestToken = null;
    accessToken = null;
    userName = "";
    oldTwitter = twitter;
    twitter = null; // setting twitter to null gives us a quick check
    // that we don't have an authorized version around.
    saveAccessToken(accessToken);

    // clear the access token from the old twitter instance, just in case
    // someone stashed it away.
    if (oldTwitter != null) {
      oldTwitter.setOAuthAccessToken(null);
    }
  }

  /**
   * Sends a Tweet of the currently logged in user.
   */
  @SimpleFunction(description = "This sends a tweet as the logged-in user with the "
      + "specified Text, which will be trimmed if it exceeds "
      + MAX_CHARACTERS
      + " characters. "
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>")
  public void Tweet(final String status) {

    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "Tweet",
          ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, "Need to login?");
      return;
    }
    // TODO(sharon): note that if the user calls DeAuthorize immediately
    // after
    // Tweet it is possible that the DeAuthorize call can slip in
    // and invalidate the authorization credentials for myTwitter, causing
    // the call below to fail. If we want to prevent this we could consider
    // using an ExecutorService object to serialize calls to Twitter.
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          twitter.updateStatus(status);
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "Tweet",
              ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Tweet with Image, Uploaded to Twitter
   */
  @SimpleFunction(description = "This sends a tweet as the logged-in user with the "
      + "specified Text and a path to the image to be uploaded, which will be trimmed if it "
      + "exceeds " + MAX_CHARACTERS + " characters. "
      + "If an image is not found or invalid, only the text will be tweeted."
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>" )
  public void TweetWithImage(final String status, final String imagePath) {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "TweetWithImage",
          ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, "Need to login?");
      return;
    }

    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          String cleanImagePath = imagePath;
          // Clean up the file path if necessary
          if (cleanImagePath.startsWith("file://")) {
            cleanImagePath = imagePath.replace("file://", "");
          }
          File imageFilePath = new File(cleanImagePath);
          if (imageFilePath.exists()) {
            StatusUpdate theTweet = new StatusUpdate(status);
            theTweet.setMedia(imageFilePath);
            twitter.updateStatus(theTweet);
          }
          else {
            form.dispatchErrorOccurredEvent(Twitter.this, "TweetWithImage",
                ErrorMessages.ERROR_TWITTER_INVALID_IMAGE_PATH);
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "TweetWithImage",
              ErrorMessages.ERROR_TWITTER_SET_STATUS_FAILED, e.getMessage());
        }
      }
    });

  }

  /**
   * Gets the most recent messages where your username is mentioned.
   */
  @SimpleFunction(description = "Requests the " + MAX_MENTIONS_RETURNED
      + " most "
      + "recent mentions of the logged-in user.  When the mentions have been "
      + "retrieved, the system will raise the <code>MentionsReceived</code> "
      + "event and set the <code>Mentions</code> property to the list of "
      + "mentions."
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>")
  public void RequestMentions() {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "RequestMentions",
          ErrorMessages.ERROR_TWITTER_REQUEST_MENTIONS_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Status> replies = Collections.emptyList();

      public void run() {
        try {
          replies = twitter.getMentionsTimeline();
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestMentions",
              ErrorMessages.ERROR_TWITTER_REQUEST_MENTIONS_FAILED,
              e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              mentions.clear();
              for (Status status : replies) {
                mentions.add(status.getUser().getScreenName() + " "
                    + status.getText());
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
  @SimpleEvent(description = "This event is raised when the mentions of the logged-in user "
      + "requested through <code>RequestMentions</code> have been retrieved.  "
      + "A list of the mentions can then be found in the <code>mentions</code> "
      + "parameter or the <code>Mentions</code> property.")
  public void MentionsReceived(final List<String> mentions) {
    EventDispatcher.dispatchEvent(this, "MentionsReceived", mentions);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "This property contains a list of mentions of the "
      + "logged-in user.  Initially, the list is empty.  To set it, the "
      + "program must: <ol> "
      + "<li> Call the <code>Authorize</code> method.</li> "
      + "<li> Wait for the <code>IsAuthorized</code> event.</li> "
      + "<li> Call the <code>RequestMentions</code> method.</li> "
      + "<li> Wait for the <code>MentionsReceived</code> event.</li></ol>\n"
      + "The value of this property will then be set to the list of mentions "
      + "(and will maintain its value until any subsequent calls to "
      + "<code>RequestMentions</code>).")
  public List<String> Mentions() {
    return mentions;
  }

  /**
   * Gets who is following you.
   */
  @SimpleFunction
  public void RequestFollowers() {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "RequestFollowers",
          ErrorMessages.ERROR_TWITTER_REQUEST_FOLLOWERS_FAILED,
          "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<User> friends = new ArrayList<User>();

      public void run() {
        try {
          IDs followerIDs = twitter.getFollowersIDs(-1);
          for (long id : followerIDs.getIDs()) {
            // convert from the IDs returned to the User
            friends.add(twitter.showUser(id));
          }
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "RequestFollowers",
              ErrorMessages.ERROR_TWITTER_REQUEST_FOLLOWERS_FAILED,
              e.getMessage());
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
  @SimpleEvent(description = "This event is raised when all of the followers of the "
      + "logged-in user requested through <code>RequestFollowers</code> have "
      + "been retrieved. A list of the followers can then be found in the "
      + "<code>followers</code> parameter or the <code>Followers</code> "
      + "property.")
  public void FollowersReceived(final List<String> followers2) {
    EventDispatcher.dispatchEvent(this, "FollowersReceived", followers2);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "This property contains a list of the followers of the "
      + "logged-in user.  Initially, the list is empty.  To set it, the "
      + "program must: <ol> "
      + "<li> Call the <code>Authorize</code> method.</li> "
      + "<li> Wait for the <code>IsAuthorized</code> event.</li> "
      + "<li> Call the <code>RequestFollowers</code> method.</li> "
      + "<li> Wait for the <code>FollowersReceived</code> event.</li></ol>\n"
      + "The value of this property will then be set to the list of "
      + "followers (and maintain its value until any subsequent call to "
      + "<code>RequestFollowers</code>).")
  public List<String> Followers() {
    return followers;
  }

  /**
   * Gets the most recent messages sent directly to you.
   */
  @SimpleFunction(description = "Requests the " + MAX_MENTIONS_RETURNED
      + " most "
      + "recent direct messages sent to the logged-in user.  When the "
      + "messages have been retrieved, the system will raise the "
      + "<code>DirectMessagesReceived</code> event and set the "
      + "<code>DirectMessages</code> property to the list of messages."
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>")
  public void RequestDirectMessages() {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "RequestDirectMessages",
          ErrorMessages.ERROR_TWITTER_REQUEST_DIRECT_MESSAGES_FAILED,
          "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<DirectMessage> messages = Collections.emptyList();

      @Override
      public void run() {
        try {
          messages = twitter.getDirectMessages();
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this,
              "RequestDirectMessages",
              ErrorMessages.ERROR_TWITTER_REQUEST_DIRECT_MESSAGES_FAILED,
              e.getMessage());
        } finally {
          handler.post(new Runnable() {
            @Override
            public void run() {
              directMessages.clear();
              for (DirectMessage message : messages) {
                directMessages.add(message.getSenderScreenName() + " "
                    + message.getText());
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
  @SimpleEvent(description = "This event is raised when the recent messages "
      + "requested through <code>RequestDirectMessages</code> have "
      + "been retrieved. A list of the messages can then be found in the "
      + "<code>messages</code> parameter or the <code>Messages</code> "
      + "property.")
  public void DirectMessagesReceived(final List<String> messages) {
    EventDispatcher.dispatchEvent(this, "DirectMessagesReceived", messages);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "This property contains a list of the most recent "
      + "messages mentioning the logged-in user.  Initially, the list is "
      + "empty.  To set it, the program must: <ol> "
      + "<li> Call the <code>Authorize</code> method.</li> "
      + "<li> Wait for the <code>Authorized</code> event.</li> "
      + "<li> Call the <code>RequestDirectMessages</code> method.</li> "
      + "<li> Wait for the <code>DirectMessagesReceived</code> event.</li>"
      + "</ol>\n"
      + "The value of this property will then be set to the list of direct "
      + "messages retrieved (and maintain that value until any subsequent "
      + "call to <code>RequestDirectMessages</code>).")
  public List<String> DirectMessages() {
    return directMessages;
  }

  /**
   * Sends a direct message to a specified username.
   */
  @SimpleFunction(description = "This sends a direct (private) message to the specified "
      + "user.  The message will be trimmed if it exceeds "
      + MAX_CHARACTERS
      + "characters. "
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>")
  public void DirectMessage(final String user, final String message) {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "DirectMessage",
          ErrorMessages.ERROR_TWITTER_DIRECT_MESSAGE_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          twitter.sendDirectMessage(user, message);
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
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "Follow",
          ErrorMessages.ERROR_TWITTER_FOLLOW_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          twitter.createFriendship(user);
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
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "StopFollowing",
          ErrorMessages.ERROR_TWITTER_STOP_FOLLOWING_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        try {
          twitter.destroyFriendship(user);
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "StopFollowing",
              ErrorMessages.ERROR_TWITTER_STOP_FOLLOWING_FAILED, e.getMessage());
        }
      }
    });
  }

  /**
   * Gets the most recent 20 messages in the user's timeline.
   */
  @SimpleFunction
  public void RequestFriendTimeline() {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "RequestFriendTimeline",
          ErrorMessages.ERROR_TWITTER_REQUEST_FRIEND_TIMELINE_FAILED,
          "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Status> messages = Collections.emptyList();

      public void run() {
        try {
          messages = twitter.getHomeTimeline();
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this,
              "RequestFriendTimeline",
              ErrorMessages.ERROR_TWITTER_REQUEST_FRIEND_TIMELINE_FAILED,
              e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              timeline.clear();
              for (Status message : messages) {
                List<String> status = new ArrayList<String>();
                status.add(message.getUser().getScreenName());
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
  @SimpleEvent(description = "This event is raised when the messages "
      + "requested through <code>RequestFriendTimeline</code> have "
      + "been retrieved. The <code>timeline</code> parameter and the "
      + "<code>Timeline</code> property will contain a list of lists, where "
      + "each sub-list contains a status update of the form (username message)")
  public void FriendTimelineReceived(final List<List<String>> timeline) {
    EventDispatcher.dispatchEvent(this, "FriendTimelineReceived", timeline);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "This property contains the 20 most recent messages of "
      + "users being followed.  Initially, the list is empty.  To set it, "
      + "the program must: <ol> "
      + "<li> Call the <code>Authorize</code> method.</li> "
      + "<li> Wait for the <code>IsAuthorized</code> event.</li> "
      + "<li> Specify users to follow with one or more calls to the "
      + "<code>Follow</code> method.</li> "
      + "<li> Call the <code>RequestFriendTimeline</code> method.</li> "
      + "<li> Wait for the <code>FriendTimelineReceived</code> event.</li> "
      + "</ol>\n"
      + "The value of this property will then be set to the list of messages "
      + "(and maintain its value until any subsequent call to "
      + "<code>RequestFriendTimeline</code>.")
  public List<List<String>> FriendTimeline() {
    return timeline;
  }

  /**
   * Search for tweets or labels
   */
  @SimpleFunction(description = "This searches Twitter for the given String query."
      + "<p><u>Requirements</u>: This should only be called after the "
      + "<code>IsAuthorized</code> event has been raised, indicating that the "
      + "user has successfully logged in to Twitter.</p>")
  public void SearchTwitter(final String query) {
    if (twitter == null || userName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, "SearchTwitter",
          ErrorMessages.ERROR_TWITTER_SEARCH_FAILED, "Need to login?");
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      List<Status> tweets = Collections.emptyList();

      public void run() {
        try {
          tweets = twitter.search(new Query(query)).getTweets();
        } catch (TwitterException e) {
          form.dispatchErrorOccurredEvent(Twitter.this, "SearchTwitter",
              ErrorMessages.ERROR_TWITTER_SEARCH_FAILED, e.getMessage());
        } finally {
          handler.post(new Runnable() {
            public void run() {
              searchResults.clear();
              for (Status tweet : tweets) {
                searchResults.add(tweet.getUser().getName() + " " + tweet.getText());
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
  @SimpleEvent(description = "This event is raised when the results of the search "
      + "requested through <code>SearchSuccessful</code> have "
      + "been retrieved. A list of the results can then be found in the "
      + "<code>results</code> parameter or the <code>Results</code> "
      + "property.")
  public void SearchSuccessful(final List<String> searchResults) {
    EventDispatcher.dispatchEvent(this, "SearchSuccessful", searchResults);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "This property, which is initially empty, is set to a "
      + "list of search results after the program: <ol>"
      + "<li>Calls the <code>SearchTwitter</code> method.</li> "
      + "<li>Waits for the <code>SearchSuccessful</code> event.</li></ol>\n"
      + "The value of the property will then be the same as the parameter to "
      + "<code>SearchSuccessful</code>.  Note that it is not necessary to "
      + "call the <code>Authorize</code> method before calling "
      + "<code>SearchTwitter</code>.")
  public List<String> SearchResults() {
    return searchResults;
  }

  /**
   * Check whether accessToken is stored in preferences. If there is one, set it.
   * If it was already set (for instance calling Authorize twice in a row),
   * it will throw an IllegalStateException that, in this case, can be ignored.
   * @return true if accessToken is valid and set (user authorized), false otherwise.
   */
  private boolean checkAccessToken(String myConsumerKey, String myConsumerSecret) {
    accessToken = retrieveAccessToken();
    if (accessToken == null) {
      return false;
    }
    else {
      if (twitter == null) {
        twitter = new TwitterFactory().getInstance();
      }
      try {
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(accessToken);
      }
      catch (IllegalStateException ies) {
        //ignore: it means that the consumer data was already set
      }
      if (userName.trim().length() == 0) {
        User user;
        try {
          user = twitter.verifyCredentials();
          userName = user.getScreenName();
        } catch (TwitterException e) {// something went wrong (networks or bad credentials <-- DeAuthorize
          deAuthorize();
          return false;
        }
      }
      return true;
    }
  }
}
