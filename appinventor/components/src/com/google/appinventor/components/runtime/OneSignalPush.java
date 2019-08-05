// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
//source from https://github.com/AppyBuilder/AppyBuilder-Source/blob/f92177c299886fb1f3968a1befd14d77a60dc4ab/appinventor/components/src/com/google/appinventor/components/runtime/OneSignalPush.java

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.onesignal.*;
import org.json.JSONObject;

@DesignerComponent(category = ComponentCategory.EXPERIMENTAL,
        description = "change this xxxxxxxxxxxxxxxxxxxxxxxx Non-visible component that provides push notification using the OneSignal service. Please refer to the <a href=\"http://onesignal.com/\">OneSignal</a> for more information.",
        iconName = "images/onesignal.png", nonVisible = true, version = 1)
@SimpleObject
@UsesLibraries(libraries = "google-play-services.jar,onesignal.jar")
@UsesPermissions(permissionNames = "com.google.android.c2dm.permission.RECEIVE, android.permission.WAKE_LOCK, android.permission.VIBRATE, android.permission.ACCESS_NETWORK_STATE, android.permission.RECEIVE_BOOT_COMPLETED, com.sec.android.provider.badge.permission.READ, com.sec.android.provider.badge.permission.WRITE, com.htc.launcher.permission.READ_SETTINGS, com.htc.launcher.permission.UPDATE_SHORTCUT, com.sonyericsson.home.permission.BROADCAST_BADGE, com.sonymobile.home.permission.PROVIDER_INSERT_BADGE, com.anddoes.launcher.permission.UPDATE_COUNT, com.majeur.launcher.permission.UPDATE_BADGE, com.huawei.android.launcher.permission.CHANGE_BADGE, com.huawei.android.launcher.permission.READ_SETTINGS, com.huawei.android.launcher.permission.WRITE_SETTINGS")
public class OneSignalPush extends AndroidNonvisibleComponent
        implements Component
{

    private final ComponentContainer container;
    private boolean subscriptionEnabled = true;
    private boolean vibrateEnabled = true;
    private boolean soundEnabled = true;
    private String playerId = "";

    // https://github.com/OneSignal/OneSignal-Android-SDK/blob/master/Examples/Eclipse/OneSignalExample/src/com/onesignal/example/ExampleApplication.java
    // https://documentation.onesignal.com/v3.0/docs/android-sdk-setup
    public OneSignalPush(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        // Logging set to help debug issues, remove before releasing your app.
        // THIS SHOULD BE SET TO NONE or REMOVED. Otherwise, you'll get errors like:
        // http://community.appybuilder.com/t/how-to-fix-onesignal-push-notification-warning/4002
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE);

        OneSignal.startInit(container.$context())
                .autoPromptLocation(false) // default call promptLocation later
                .setNotificationReceivedHandler(new ExampleNotificationReceivedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();
        SubscriptionEnabled(subscriptionEnabled);
        VibrateEnabled(vibrateEnabled);
        SoundEnabled(soundEnabled);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "OneSignal App ID")
    @SimpleProperty(userVisible = true)
    public void AppId(String appId) {
        //Allowing user to programmatically setup onesignal app id - sdk api: https://documentation.onesignal.com/docs/android-native-sdk
        OneSignal.init(container.$context(), null, appId, new NotificationOpenHandler(), new ExampleNotificationReceivedHandler());
        // OneSignal.init(context, null, oneSignalAppId, NotificationOpenedHandler, NotificationReceivedHandler)
    }

    private class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            //BackgroundImageLayout backgroundImageLayout = notification.payload.backgroundImageLayout;
            String rawPayload = notification.payload.rawPayload;

            String customKey;

            Log.d("OneSignalPush", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.d("OneSignalPush", "customkey set with value: " + customKey);
            }
        }
    }
    // https://stackoverflow.com/questions/41863680/onesignal-android-notificationopenedhandler-start-activity
    class NotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult osNotificationOpenResult) {
            OSNotificationAction.ActionType actionType = osNotificationOpenResult.action.type;
            JSONObject data = osNotificationOpenResult.notification.payload.additionalData;
            String customKey;
            Log.d("OneSignalPush", "NotificationID received: " + data);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null) {
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
                } else {
                    Log.i("OneSignalExample", "No data");
                }
            }
        }
        // This fires when a notification is opened by tapping on it.

    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Opt users in or out of receiving notifications. Default is true")
    public void SubscriptionEnabled(boolean enabled) {
        this.subscriptionEnabled = enabled;
        OneSignal.setSubscription(enabled);
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Reports if notification is enabled or not")
    public boolean SubscriptionEnabled() {
        return subscriptionEnabled;
    }


    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Enables / disables push notification vibration. Default is true")
    public void VibrateEnabled(boolean enabled) {
        if (vibrateEnabled == enabled) return;

        this.vibrateEnabled = enabled;
        OneSignal.enableVibrate(enabled);
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Reports if notification vibration is enabled or not")
    public boolean VibrateEnabled() {
        return vibrateEnabled;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Enables / disables push notification sound. Default is true")
    public void SoundEnabled(boolean enabled) {
        if (soundEnabled == enabled) return;

        this.soundEnabled = enabled;
        OneSignal.enableSound(enabled);
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Reports if notification sound is enabled or not")
    public boolean SoundEnabled() {
        return soundEnabled;
    }


    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns OneSignal Player / User ID. If not set, returns empty string")
    public final String PlayerId() {
        OSPermissionSubscriptionState state = OneSignal.getPermissionSubscriptionState();

        if (state.getSubscriptionStatus().getUserId() != null) {
            this.playerId = state.getSubscriptionStatus().getUserId();
        }

        return this.playerId;
    }
}
