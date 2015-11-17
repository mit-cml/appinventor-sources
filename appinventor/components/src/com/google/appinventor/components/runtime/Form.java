// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// ***********************************************
// If we're not going to go this route with onDestroy, then at least get rid of the DEBUG flag.

package com.google.appinventor.components.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.multidex.MultiDex;
import com.google.appinventor.components.runtime.multidex.MultiDexApplication;
import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FullScreenVideoUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ScreenDensityUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

/**
 * Component underlying activities and UI apps, not directly accessible to Simple programmers.
 *
 * <p>This is the root container of any Android activity and also the
 * superclass for for Simple/Android UI applications.
 *
 * The main form is always named "Screen1".
 *
 * NOTE WELL: There are many places in the code where the name "Screen1" is
 * directly referenced. If we ever change App Inventor to support renaming
 * screens and Screen1 in particular, we need to make sure we find all those
 * places and make the appropriate code changes.
 *
 */
@DesignerComponent(version = YaVersion.FORM_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT,
    description = "Top-level component containing all other components in the program",
    showOnPalette = false)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_WIFI_STATE,android.permission.ACCESS_NETWORK_STATE")
public class Form extends Activity
  implements Component, ComponentContainer, HandlesEventDispatching,
  OnGlobalLayoutListener {

  private static final String LOG_TAG = "Form";

  private static final String RESULT_NAME = "APP_INVENTOR_RESULT";

  private static final String ARGUMENT_NAME = "APP_INVENTOR_START";

  public static final String APPINVENTOR_URL_SCHEME = "appinventor";

  // Keep track of the current form object.
  // activeForm always holds the Form that is currently handling event dispatching so runtime.scm
  // can lookup symbols in the correct environment.
  // There is at least one case where an event can be fired when the activity is not the foreground
  // activity: if a Clock component's TimerAlwaysFires property is true, the Clock component's
  // Timer event will still fire, even when the activity is no longer in the foreground. For this
  // reason, we cannot assume that the activeForm is the foreground activity.
  protected static Form activeForm;

  private float deviceDensity;
  private float compatScalingFactor;

  // applicationIsBeingClosed is set to true during closeApplication.
  private static boolean applicationIsBeingClosed;

  private final Handler androidUIHandler = new Handler();

  protected String formName;

  private boolean screenInitialized;

  private static final int SWITCH_FORM_REQUEST_CODE = 1;
  private static int nextRequestCode = SWITCH_FORM_REQUEST_CODE + 1;

  // Backing for background color
  private int backgroundColor;

  // Information string the app creator can set.  It will be shown when
  // "about this application" menu item is selected.
  private String aboutScreen;
  private boolean showStatusBar = true;
  private boolean showTitle = true;

  private String backgroundImagePath = "";
  private Drawable backgroundDrawable;

  // Layout
  private LinearLayout viewLayout;

  // translates App Inventor alignment codes to Android gravity
  private AlignmentUtil alignmentSetter;

  // the alignment for this component's LinearLayout
  private int horizontalAlignment;
  private int verticalAlignment;

  // String representing the transition animation type
  private String openAnimType;
  private String closeAnimType;

  private FrameLayout frameLayout;
  private boolean scrollable;

  private ScaledFrameLayout scaleLayout;
  private static boolean sCompatibilityMode;

  // Application lifecycle related fields
  private final HashMap<Integer, ActivityResultListener> activityResultMap = Maps.newHashMap();
  private final Set<OnStopListener> onStopListeners = Sets.newHashSet();
  private final Set<OnNewIntentListener> onNewIntentListeners = Sets.newHashSet();
  private final Set<OnResumeListener> onResumeListeners = Sets.newHashSet();
  private final Set<OnPauseListener> onPauseListeners = Sets.newHashSet();
  private final Set<OnDestroyListener> onDestroyListeners = Sets.newHashSet();

  // AppInventor lifecycle: listeners for the Initialize Event
  private final Set<OnInitializeListener> onInitializeListeners = Sets.newHashSet();

  // Listeners for options menu.
  private final Set<OnCreateOptionsMenuListener> onCreateOptionsMenuListeners = Sets.newHashSet();
  private final Set<OnOptionsItemSelectedListener> onOptionsItemSelectedListeners = Sets.newHashSet();

  // Set to the optional String-valued Extra passed in via an Intent on startup.
  // This is passed directly in the Repl.
  protected String startupValue = "";

  // To control volume of error complaints
  private static long minimumToastWait = 10000000000L; // 10 seconds
  private long lastToastTime = System.nanoTime() - minimumToastWait;

  // In a multiple screen application, when a secondary screen is opened, nextFormName is set to
  // the name of the secondary screen. It is saved so that it can be passed to the OtherScreenClosed
  // event.
  private String nextFormName;

  private FullScreenVideoUtil fullScreenVideoUtil;

  private int formWidth;
  private int formHeight;

  private boolean keyboardShown = false;

  private ProgressDialog progress;
  private static boolean _initialized = false;

  public static class PercentStorageRecord {
    public enum Dim {
      HEIGHT, WIDTH };

    public PercentStorageRecord(AndroidViewComponent component, int length, Dim dim) {
      this.component = component;
      this.length = length;
      this.dim = dim;
    }

    AndroidViewComponent component;
    int length;
    Dim dim;
  }
  private ArrayList<PercentStorageRecord> dimChanges = new ArrayList();

  private static class MultiDexInstaller extends AsyncTask<Form, Void, Boolean> {
    Form ourForm;

    @Override
    protected Boolean doInBackground(Form... form) {
      ourForm = form[0];
      Log.d(LOG_TAG, "Doing Full MultiDex Install");
      MultiDex.install(ourForm, true); // Force installation
      return true;
    }
    @Override
    protected void onPostExecute(Boolean v) {
      ourForm.onCreateFinish();
    }
  }

  @Override
  public void onCreate(Bundle icicle) {
    // Called when the activity is first created
    super.onCreate(icicle);

    // Figure out the name of this form.
    String className = getClass().getName();
    int lastDot = className.lastIndexOf('.');
    formName = className.substring(lastDot + 1);
    Log.d(LOG_TAG, "Form " + formName + " got onCreate");

    activeForm = this;
    Log.i(LOG_TAG, "activeForm is now " + activeForm.formName);

    deviceDensity = this.getResources().getDisplayMetrics().density;
    Log.d(LOG_TAG, "deviceDensity = " + deviceDensity);
    compatScalingFactor = ScreenDensityUtil.computeCompatibleScaling(this);
    Log.i(LOG_TAG, "compatScalingFactor = " + compatScalingFactor);
    viewLayout = new LinearLayout(this, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL);
    alignmentSetter = new AlignmentUtil(viewLayout);

    progress = null;
    if (!_initialized && formName.equals("Screen1")) {
      Log.d(LOG_TAG, "MULTI: _initialized = " + _initialized + " formName = " + formName);
      _initialized = true;
      // Note that we always consult ReplApplication even if we are not the Repl (Companion)
      // this is subtle. When ReplApplication isn't directly used, the "installed" property
      // defaults to ture, which means we can continue. The MultiDexApplication which is
      // used in a non-Companion context will always do the full install
      if (ReplApplication.installed) {
        Log.d(LOG_TAG, "MultiDex already installed.");
        onCreateFinish();
      } else {
        progress = ProgressDialog.show(this, "Please Wait...", "Installation Finishing");
        progress.show();
        new MultiDexInstaller().execute(this);
      }
    } else {
      Log.d(LOG_TAG, "NO MULTI: _initialized = " + _initialized + " formName = " + formName);
      _initialized = true;
      onCreateFinish();
    }
  }

  /*
   * Finish the work of setting up the Screen.
   *
   * onCreate is done in two parts. The first part is done in onCreate
   * and the second part is done here. This division is so that we can
   * asynchronously load classes2.dex if we have to, while displaying
   * a splash screen which explains that installation is finishing.
   * We do this because there can be a significant time spent in
   * DexOpt'ing classes2.dex. Note: If it is already optimized, we
   * don't show the splash screen and call this function
   * immediately. Similarly we call this function immediately on any
   * screen other then Screen1.
   *
   */

  void onCreateFinish() {

    Log.d(LOG_TAG, "onCreateFinish called " + System.currentTimeMillis());
    if (progress != null) {
      progress.dismiss();
    }

    defaultPropertyValues();

    // Get startup text if any before adding components
    Intent startIntent = getIntent();
    if (startIntent != null && startIntent.hasExtra(ARGUMENT_NAME)) {
      startupValue = startIntent.getStringExtra(ARGUMENT_NAME);
    }

    fullScreenVideoUtil = new FullScreenVideoUtil(this, androidUIHandler);

    // Set soft keyboard to not cover the focused UI element, e.g., when you are typing
    // into a textbox near the bottom of the screen.
    WindowManager.LayoutParams params = getWindow().getAttributes();
    int softInputMode = params.softInputMode;
    getWindow().setSoftInputMode(
        softInputMode | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    // Add application components to the form
    $define();

    // Special case for Event.Initialize(): all other initialize events are triggered after
    // completing the constructor. This doesn't work for Android apps though because this method
    // is called after the constructor completes and therefore the Initialize event would run
    // before initialization finishes. Instead the compiler suppresses the invocation of the
    // event and leaves it up to the library implementation.
    Initialize();
  }

  private void defaultPropertyValues() {
    Scrollable(false); // frameLayout is created in Scrollable()
    Sizing("Fixed");
    BackgroundImage("");
    AboutScreen("");
    BackgroundImage("");
    BackgroundColor(Component.COLOR_WHITE);
    AlignHorizontal(ComponentConstants.GRAVITY_LEFT);
    AlignVertical(ComponentConstants.GRAVITY_TOP);
    Title("");
    ShowStatusBar(true);
    TitleVisible(true);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.d(LOG_TAG, "onConfigurationChanged() called");
    final int newOrientation = newConfig.orientation;
    if (newOrientation == Configuration.ORIENTATION_LANDSCAPE ||
        newOrientation == Configuration.ORIENTATION_PORTRAIT) {
      // At this point, the screen has not be resized to match the new orientation.
      // We use Handler.post so that we'll dispatch the ScreenOrientationChanged event after the
      // screen has been resized to match the new orientation.

      androidUIHandler.post(new Runnable() {
        public void run() {
          boolean dispatchEventNow = false;
          if (frameLayout != null) {
            if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
              if (frameLayout.getWidth() >= frameLayout.getHeight()) {
                dispatchEventNow = true;
              }
            } else { // Portrait
              if (frameLayout.getHeight() >= frameLayout.getWidth()) {
                dispatchEventNow = true;
              }
            }
          }
          if (dispatchEventNow) {
            recomputeLayout();
            final FrameLayout savedLayout = frameLayout;
            androidUIHandler.postDelayed(new Runnable() {
                public void run() {
                  if (frameLayout != null) {
                    frameLayout.invalidate();
                  }
                }
              }, 100);          // Redraw the whole screen in 1/10 second
                                // we do this to avoid screen artifacts left
                                // left by the Android runtime.
            ScreenOrientationChanged();
          } else {
            // Try again later.
            androidUIHandler.post(this);
          }
        }
      });
    }
  }

// What's this code?
//
// There is either an App Inventor bug, or Android bug (likely both)
// that results in the contents of the screen being rendered "too
// tall" on some devices when the soft keyboard is toggled from
// displayed to hidden. This results in the bottom of the App being
// cut-off. This only happens when we are in "Fixed" mode where we
// provide a ScaledFrameLayout whose job is to scale the app to fill
// the display of whatever device it is running on ("big phone mode").
//
// The code below is triggered on every major layout change. It
// compares the size of the device window with the height of the
// displayed content. Based on the difference, we can tell if the
// keyboard is open or closed. We detect the transition from open to
// closed and iff we are in "Fixed" mode (sComptabilityMode = true) we
// trigger a recomputation of the entire apps layout after a delay of
// 100ms (which seems to be required, for reasons we don't quite
// understand).
//
// This code is not really a "fix" but more of a "workaround."

  @Override
  public void onGlobalLayout() {
    int heightDiff = scaleLayout.getRootView().getHeight() - scaleLayout.getHeight();
    int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    Log.d(LOG_TAG, "onGlobalLayout(): heightdiff = " + heightDiff + " contentViewTop = " +
      contentViewTop);

    if(heightDiff <= contentViewTop){
      Log.d(LOG_TAG, "keyboard hidden!");
      if (keyboardShown) {
        keyboardShown = false;
        if (sCompatibilityMode) { // Put us back in "Fixed" Mode
          scaleLayout.setScale(compatScalingFactor);
          scaleLayout.invalidate();
        }
      }
    } else {
      int keyboardHeight = heightDiff - contentViewTop;
      Log.d(LOG_TAG, "keyboard shown!");
      keyboardShown = true;
      if (scaleLayout != null) { // Effectively put us in responsive mode
        scaleLayout.setScale(1.0f);
        scaleLayout.invalidate();
      }
    }
  }

  /*
   * Here we override the hardware back button, just to make sure
   * that the closing screen animation is applied. (In API level
   * 5, we can simply override the onBackPressed method rather
   * than bothering with onKeyDown)
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (!BackPressed()) {
        boolean handled = super.onKeyDown(keyCode, event);
        AnimationUtil.ApplyCloseScreenAnimation(this, closeAnimType);
        return handled;
      } else {
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  @SimpleEvent(description = "Device back button pressed.")
  public boolean BackPressed() {
    return EventDispatcher.dispatchEvent(this, "BackPressed");
  }

  // onActivityResult should be triggered in only two cases:
  // (1) The result is for some other component in the app, not this Form itself
  // (2) This page started another page, and that page is closing, and passing
  // its value back as a JSON-encoded string in the intent.

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(LOG_TAG, "Form " + formName + " got onActivityResult, requestCode = " +
        requestCode + ", resultCode = " + resultCode);
    if (requestCode == SWITCH_FORM_REQUEST_CODE) {
      // Assume this is a multiple screen application, and a secondary
      // screen has closed.  Process the result as a JSON-encoded string.
      // This can also happen if the user presses the back button, in which case
      // there's no data.
     String resultString;
      if (data != null && data.hasExtra(RESULT_NAME)) {
        resultString = data.getStringExtra(RESULT_NAME);
      } else {
        resultString = "";
      }
      Object decodedResult = decodeJSONStringForForm(resultString, "other screen closed");
      // nextFormName was set when this screen opened the secondary screen
      OtherScreenClosed(nextFormName, decodedResult);
    } else {
      // Another component (such as a ListPicker, ActivityStarter, etc) is expecting this result.
      ActivityResultListener component = activityResultMap.get(requestCode);
      if (component != null) {
        component.resultReturned(requestCode, resultCode, data);
      }
    }
  }

  // functionName is a string to include in the error message that will be shown
  // if the JSON decoding fails
  private  static Object decodeJSONStringForForm(String jsonString, String functionName) {
    Log.i(LOG_TAG, "decodeJSONStringForForm -- decoding JSON representation:" + jsonString);
    Object valueFromJSON = "";
    try {
      valueFromJSON = JsonUtil.getObjectFromJson(jsonString);
      Log.i(LOG_TAG, "decodeJSONStringForForm -- got decoded JSON:" + valueFromJSON.toString());
    } catch (JSONException e) {
      activeForm.dispatchErrorOccurredEvent(activeForm, functionName,
          // showing the start value here will produce an ugly error on the phone, but it's
          // more useful than not showing the value
          ErrorMessages.ERROR_SCREEN_BAD_VALUE_RECEIVED, jsonString);
    }
    return valueFromJSON;
  }

  public int registerForActivityResult(ActivityResultListener listener) {
    int requestCode = generateNewRequestCode();
    activityResultMap.put(requestCode, listener);
    return requestCode;
  }

  public void unregisterForActivityResult(ActivityResultListener listener) {
    List<Integer> keysToDelete = Lists.newArrayList();
    for (Map.Entry<Integer, ActivityResultListener> mapEntry : activityResultMap.entrySet()) {
      if (listener.equals(mapEntry.getValue())) {
        keysToDelete.add(mapEntry.getKey());
      }
    }
    for (Integer key : keysToDelete) {
      activityResultMap.remove(key);
    }
  }

  void ReplayFormOrientation() {
    // We first make a copy of the existing dimChanges list
    // because while we are replaying it, it is being appended to
    Log.d(LOG_TAG, "ReplayFormOrientation()");
    ArrayList<PercentStorageRecord> temp = (ArrayList<PercentStorageRecord>) dimChanges.clone();
    dimChanges.clear();         // Empties it out
    for (int i = 0; i < temp.size(); i++) {
      // Iterate over the list...
      PercentStorageRecord r = temp.get(i);
      if (r.dim == PercentStorageRecord.Dim.HEIGHT) {
        r.component.Height(r.length);
      } else {
        r.component.Width(r.length);
      }
    }
  }

  public void registerPercentLength(AndroidViewComponent component, int length, PercentStorageRecord.Dim dim) {
    dimChanges.add(new PercentStorageRecord(component, length, dim));
  }

  private static int generateNewRequestCode() {
    return nextRequestCode++;
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.i(LOG_TAG, "Form " + formName + " got onResume");
    activeForm = this;

    // If applicationIsBeingClosed is true, call closeApplication() immediately to continue
    // unwinding through all forms of a multi-screen application.
    if (applicationIsBeingClosed) {
      closeApplication();
      return;
    }

    for (OnResumeListener onResumeListener : onResumeListeners) {
      onResumeListener.onResume();
    }
  }

  public void registerForOnResume(OnResumeListener component) {
    onResumeListeners.add(component);
  }

  /**
   * An app can register to be notified when App Inventor's Initialize
   * block has fired.  They will be called in Initialize().
   *
   * @param component
   */
  public void registerForOnInitialize(OnInitializeListener component) {
    onInitializeListeners.add(component);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.d(LOG_TAG, "Form " + formName + " got onNewIntent " + intent);
    for (OnNewIntentListener onNewIntentListener : onNewIntentListeners) {
      onNewIntentListener.onNewIntent(intent);
    }
  }

  public void registerForOnNewIntent(OnNewIntentListener component) {
    onNewIntentListeners.add(component);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.i(LOG_TAG, "Form " + formName + " got onPause");
    for (OnPauseListener onPauseListener : onPauseListeners) {
      onPauseListener.onPause();
    }
  }

  public void registerForOnPause(OnPauseListener component) {
    onPauseListeners.add(component);
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.i(LOG_TAG, "Form " + formName + " got onStop");
    for (OnStopListener onStopListener : onStopListeners) {
      onStopListener.onStop();
    }
  }

  public void registerForOnStop(OnStopListener component) {
    onStopListeners.add(component);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // for debugging and future growth
    Log.i(LOG_TAG, "Form " + formName + " got onDestroy");

    // Unregister events for components in this form.
    EventDispatcher.removeDispatchDelegate(this);

    for (OnDestroyListener onDestroyListener : onDestroyListeners) {
      onDestroyListener.onDestroy();
    }
  }

  public void registerForOnDestroy(OnDestroyListener component) {
    onDestroyListeners.add(component);
  }

  public void registerForOnCreateOptionsMenu(OnCreateOptionsMenuListener component) {
    onCreateOptionsMenuListeners.add(component);
  }

  public void registerForOnOptionsItemSelected(OnOptionsItemSelectedListener component) {
    onOptionsItemSelectedListeners.add(component);
  }

  public Dialog onCreateDialog(int id) {
    switch(id) {
    case FullScreenVideoUtil.FULLSCREEN_VIDEO_DIALOG_FLAG:
      return fullScreenVideoUtil.createFullScreenVideoDialog();
    default:
      return super.onCreateDialog(id);
    }
  }

  public void onPrepareDialog(int id, Dialog dialog) {
    switch(id) {
    case FullScreenVideoUtil.FULLSCREEN_VIDEO_DIALOG_FLAG:
      fullScreenVideoUtil.prepareFullScreenVideoDialog(dialog);
      break;
    default:
      super.onPrepareDialog(id, dialog);
    }
  }

  /**
   * Compiler-generated method to initialize and add application components to
   * the form.  We just provide an implementation here to artificially make
   * this class concrete so that it is included in the documentation and
   * Codeblocks language definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.LangDefXmlGenerator},
   * respectively.  The actual implementation appears in {@code runtime.scm}.
   */
  protected void $define() {    // This must be declared protected because we are called from Screen1 which subclasses
                                // us and isn't in our package.
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canDispatchEvent(Component component, String eventName) {
    // Events can only be dispatched after the screen initialized event has completed.
    boolean canDispatch = screenInitialized ||
        (component == this && eventName.equals("Initialize"));

    if (canDispatch) {
      // Set activeForm to this before the event is dispatched.
      // runtime.scm will call getActiveForm() when the event handler executes.
      activeForm = this;
    }

    return canDispatch;
  }

  /**
   * A trivial implementation to artificially make this class concrete so
   * that it is included in the documentation and
   * Codeblocks language definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.LangDefXmlGenerator},
   * respectively.  The actual implementation appears in {@code runtime.scm}.
   */
  @Override
  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args) {
    throw new UnsupportedOperationException();
  }


  /**
   * Initialize event handler.
   */
  @SimpleEvent(description = "Screen starting")
  public void Initialize() {
    // Dispatch the Initialize event only after the screen's width and height are no longer zero.
    androidUIHandler.post(new Runnable() {
      public void run() {
        if (frameLayout != null && frameLayout.getWidth() != 0 && frameLayout.getHeight() != 0) {
          EventDispatcher.dispatchEvent(Form.this, "Initialize");
          if (sCompatibilityMode) { // Make sure call to setLayout happens
            Sizing("Fixed");
          } else {
            Sizing("Responsive");
          }
          screenInitialized = true;

          //  Call all apps registered to be notified when Initialize Event is dispatched
          for (OnInitializeListener onInitializeListener : onInitializeListeners) {
            onInitializeListener.onInitialize();
          }
          if (activeForm instanceof ReplForm) { // We are the Companion
            ((ReplForm)activeForm).HandleReturnValues();
          }
        } else {
          // Try again later.
          androidUIHandler.post(this);
        }
      }
    });
  }

  @SimpleEvent(description = "Screen orientation changed")
  public void ScreenOrientationChanged() {
    EventDispatcher.dispatchEvent(this, "ScreenOrientationChanged");
  }

  /**
   * ErrorOccurred event handler.
   */
  @SimpleEvent(
      description = "Event raised when an error occurs. Only some errors will " +
      "raise this condition.  For those errors, the system will show a notification " +
      "by default.  You can use this event handler to prescribe an error " +
      "behavior different than the default.")
  public void ErrorOccurred(Component component, String functionName, int errorNumber,
      String message) {
    String componentType = component.getClass().getName();
    componentType = componentType.substring(componentType.lastIndexOf(".") + 1);
    Log.e(LOG_TAG, "Form " + formName + " ErrorOccurred, errorNumber = " + errorNumber +
        ", componentType = " + componentType + ", functionName = " + functionName +
        ", messages = " + message);
    if ((!(EventDispatcher.dispatchEvent(
        this, "ErrorOccurred", component, functionName, errorNumber, message)))
        && screenInitialized)  {
      // If dispatchEvent returned false, then no user-supplied error handler was run.
      // If in addition, the screen initializer was run, then we assume that the
      // user did not provide an error handler.   In this case, we run a default
      // error handler, namely, showing a notification to the end user of the app.
      // The app writer can override this by providing an error handler.
      new Notifier(this).ShowAlert("Error " + errorNumber + ": " + message);
    }
  }


  public void ErrorOccurredDialog(Component component, String functionName, int errorNumber,
      String message, String title, String buttonText) {
    String componentType = component.getClass().getName();
    componentType = componentType.substring(componentType.lastIndexOf(".") + 1);
    Log.e(LOG_TAG, "Form " + formName + " ErrorOccurred, errorNumber = " + errorNumber +
        ", componentType = " + componentType + ", functionName = " + functionName +
        ", messages = " + message);
    if ((!(EventDispatcher.dispatchEvent(
        this, "ErrorOccurred", component, functionName, errorNumber, message)))
        && screenInitialized)  {
      // If dispatchEvent returned false, then no user-supplied error handler was run.
      // If in addition, the screen initializer was run, then we assume that the
      // user did not provide an error handler.   In this case, we run a default
      // error handler, namely, showing a message dialog to the end user of the app.
      // The app writer can override this by providing an error handler.
      new Notifier(this).ShowMessageDialog("Error " + errorNumber + ": " + message, title, buttonText);
    }
  }


  public void dispatchErrorOccurredEvent(final Component component, final String functionName,
      final int errorNumber, final Object... messageArgs) {
    runOnUiThread(new Runnable() {
      public void run() {
        String message = ErrorMessages.formatMessage(errorNumber, messageArgs);
        ErrorOccurred(component, functionName, errorNumber, message);
      }
    });
  }

  // This is like dispatchErrorOccurred, except that it defaults to showing
  // a message dialog rather than an alert.   The app writer can override either of these behaviors,
  // but using the event dialog version frees the app writer of the need to explicitly override
  // the alert behavior in the case
  // where a message dialog is what's generally needed.
  public void dispatchErrorOccurredEventDialog(final Component component, final String functionName,
      final int errorNumber, final Object... messageArgs) {
    runOnUiThread(new Runnable() {
      public void run() {
        String message = ErrorMessages.formatMessage(errorNumber, messageArgs);
        ErrorOccurredDialog(
            component,
            functionName,
            errorNumber,
            message,
            "Error in " + functionName,
            "Dismiss");
      }
    });
  }



  /**
   * Scrollable property getter method.
   *
   * @return  true if the screen is vertically scrollable
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "When checked, there will be a vertical scrollbar on the "
    + "screen, and the height of the application can exceed the physical "
    + "height of the device. When unchecked, the application height is "
    + "constrained to the height of the device.")
  public boolean Scrollable() {
    return scrollable;
  }

  /**
   * Scrollable property setter method.
   *
   * @param scrollable  true if the screen should be vertically scrollable
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  @SimpleProperty
  public void Scrollable(boolean scrollable) {
    if (this.scrollable == scrollable && frameLayout != null) {
      return;
    }

    this.scrollable = scrollable;
    recomputeLayout();
  }

  private void recomputeLayout() {

    Log.d(LOG_TAG, "recomputeLayout called");
    // Remove our view from the current frameLayout.
    if (frameLayout != null) {
      frameLayout.removeAllViews();
    }

    frameLayout = scrollable ? new ScrollView(this) : new FrameLayout(this);
    frameLayout.addView(viewLayout.getLayoutManager(), new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    setBackground(frameLayout);

    Log.d(LOG_TAG, "About to create a new ScaledFrameLayout");
    scaleLayout = new ScaledFrameLayout(this);
    scaleLayout.addView(frameLayout, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    setContentView(scaleLayout);
    frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
    scaleLayout.requestLayout();
    androidUIHandler.post(new Runnable() {
      public void run() {
        if (frameLayout != null && frameLayout.getWidth() != 0 && frameLayout.getHeight() != 0) {
          if (sCompatibilityMode) { // Make sure call to setLayout happens
            Sizing("Fixed");
          } else {
            Sizing("Responsive");
          }
          ReplayFormOrientation(); // Re-do Form layout because percentage code
                                   // needs to recompute objects sizes etc.
          frameLayout.requestLayout();
        } else {
          // Try again later.
          androidUIHandler.post(this);
        }
      }
    });
  }

  /**
   * BackgroundColor property getter method.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * BackgroundColor property setter method.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    // setBackground(viewLayout.getLayoutManager()); // Doesn't seem necessary anymore
    setBackground(frameLayout);
  }

  /**
   * Returns the path of the background image.
   *
   * @return  the path of the background image
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "The screen background image.")
  public String BackgroundImage() {
    return backgroundImagePath;
  }


  /**
   * Specifies the path of the background image.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path the path of the background image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "The screen background image.")
  public void BackgroundImage(String path) {
    backgroundImagePath = (path == null) ? "" : path;

    try {
      backgroundDrawable = MediaUtil.getBitmapDrawable(this, backgroundImagePath);
    } catch (IOException ioe) {
      Log.e(LOG_TAG, "Unable to load " + backgroundImagePath);
      backgroundDrawable = null;
    }
    setBackground(frameLayout);
  }

  /**
   * Title property getter method.
   *
   * @return  form caption
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The caption for the form, which apears in the title bar")
  public String Title() {
    return getTitle().toString();
  }

  /**
   * Title property setter method: sets a new caption for the form in the
   * form's title bar.
   *
   * @param title  new form caption
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Title(String title) {
    setTitle(title);
  }


  /**
   * AboutScreen property getter method.
   *
   * @return  AboutScreen string
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Information about the screen.  It appears when \"About this Application\" "
      + "is selected from the system menu. Use it to inform people about your app.  In multiple "
      + "screen apps, each screen has its own AboutScreen info.")
  public String AboutScreen() {
    return aboutScreen;
  }

  /**
   * AboutScreen property setter method: sets a new aboutApp string for the form in the
   * form's "About this application" menu.
   *
   * @param aboutScreen content to be displayed in aboutApp
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA,
      defaultValue = "")
  @SimpleProperty
  public void AboutScreen(String aboutScreen) {
    this.aboutScreen = aboutScreen;
  }

  /**
   * TitleVisible property getter method.
   *
   * @return  showTitle boolean
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The title bar is the top gray bar on the screen. This property reports whether the title bar is visible.")
  public boolean TitleVisible() {
    return showTitle;
  }

  /**
   * TitleVisible property setter method.
   *
   * @param show boolean
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TitleVisible(boolean show) {
    if (show != showTitle) {
      View v = (View)findViewById(android.R.id.title).getParent();
      if (v != null) {
        if (show) {
          v.setVisibility(View.VISIBLE);
        } else {
          v.setVisibility(View.GONE);
        }
        showTitle = show;
      }
    }
  }

  /**
   * ShowStatusBar property getter method.
   *
   * @return  showStatusBar boolean
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The status bar is the topmost bar on the screen. This property reports whether the status bar is visible.")
  public boolean ShowStatusBar() {
    return showStatusBar;
  }

  /**
   * ShowStatusBar property setter method.
   *
   * @param show boolean
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShowStatusBar(boolean show) {
    if (show != showStatusBar) {
      if (show) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      } else {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      }
      showStatusBar = show;
    }
  }

  /**
   * The requested screen orientation. Commonly used values are
      unspecified (-1), landscape (0), portrait (1), sensor (4), and user (2).  " +
      "See the Android developer documentation for ActivityInfo.Screen_Orientation for the " +
      "complete list of possible settings.
   *
   * ScreenOrientation property getter method.
   *
   * @return  screen orientation
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The requested screen orientation, specified as a text value.  " +
      "Commonly used values are " +
      "landscape, portrait, sensor, user and unspecified.  " +
      "See the Android developer documentation for ActivityInfo.Screen_Orientation for the " +
      "complete list of possible settings.")
  public String ScreenOrientation() {
    switch (getRequestedOrientation()) {
      case ActivityInfo.SCREEN_ORIENTATION_BEHIND:
        return "behind";
      case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
        return "landscape";
      case ActivityInfo.SCREEN_ORIENTATION_NOSENSOR:
        return "nosensor";
      case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
        return "portrait";
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR:
        return "sensor";
      case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
        return "unspecified";
      case ActivityInfo.SCREEN_ORIENTATION_USER:
        return "user";
      case 10: // ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        return "fullSensor";
      case 8: // ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        return "reverseLandscape";
      case 9: // ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        return "reversePortrait";
      case 6: // ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        return "sensorLandscape";
      case 7: // ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        return "sensorPortrait";
    }

    return "unspecified";
  }

  /**
   * ScreenOrientation property setter method: sets the screen orientation for
   * the form.
   *
   * @param screenOrientation  the screen orientation as a string
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ORIENTATION,
      defaultValue = "unspecified")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ScreenOrientation(String screenOrientation) {
    if (screenOrientation.equalsIgnoreCase("behind")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
    } else if (screenOrientation.equalsIgnoreCase("landscape")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    } else if (screenOrientation.equalsIgnoreCase("nosensor")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    } else if (screenOrientation.equalsIgnoreCase("portrait")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else if (screenOrientation.equalsIgnoreCase("sensor")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    } else if (screenOrientation.equalsIgnoreCase("unspecified")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    } else if (screenOrientation.equalsIgnoreCase("user")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD) {
      if (screenOrientation.equalsIgnoreCase("fullSensor")) {
        setRequestedOrientation(10); // ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
      } else if (screenOrientation.equalsIgnoreCase("reverseLandscape")) {
        setRequestedOrientation(8); // ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
      } else if (screenOrientation.equalsIgnoreCase("reversePortrait")) {
        setRequestedOrientation(9); // ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
      } else if (screenOrientation.equalsIgnoreCase("sensorLandscape")) {
        setRequestedOrientation(6); // ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      } else if (screenOrientation.equalsIgnoreCase("sensorPortrait")) {
        setRequestedOrientation(7); // ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
      } else {
        dispatchErrorOccurredEvent(this, "ScreenOrientation",
            ErrorMessages.ERROR_INVALID_SCREEN_ORIENTATION, screenOrientation);
      }
    } else {
      dispatchErrorOccurredEvent(this, "ScreenOrientation",
          ErrorMessages.ERROR_INVALID_SCREEN_ORIENTATION, screenOrientation);
    }
  }


  // Note(halabelson): This section on centering is duplicated between Form and HVArrangement
  // I did not see a clean way to abstract it.  Someone should have a look.

  // Note(halabelson): The numeric encodings of the alignment specifications are specified
  // in ComponentConstants

  /**
  * Returns a number that encodes how contents of the screen are aligned horizontally.
  * The choices are: 1 = left aligned, 2 = horizontally centered, 3 = right aligned
  */
  @SimpleProperty(
     category = PropertyCategory.APPEARANCE,
     description = "A number that encodes how contents of the screen are aligned " +
         " horizontally. The choices are: 1 = left aligned, 2 = horizontally centered, " +
         " 3 = right aligned.")
 public int AlignHorizontal() {
   return horizontalAlignment;
 }

 /**
  * Sets the horizontal alignment for contents of the screen
  *
  * @param alignment
  */
 @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
     defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
 @SimpleProperty
 public void AlignHorizontal(int alignment) {
   try {
     // notice that the throw will prevent the alignment from being changed
     // if the argument is illegal
     alignmentSetter.setHorizontalAlignment(alignment);
     horizontalAlignment = alignment;
   } catch (IllegalArgumentException e) {
     this.dispatchErrorOccurredEvent(this, "HorizontalAlignment",
         ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
   }
 }

 /**
  * Returns a number that encodes how contents of the arrangement are aligned vertically.
  * The choices are: 1 = top, 2 = vertically centered, 3 = aligned at the bottom.
  * Vertical alignment has no effect if the screen is scrollable.
  */
 @SimpleProperty(
     category = PropertyCategory.APPEARANCE,
     description = "A number that encodes how the contents of the arrangement are aligned " +
     "vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, " +
     "3 = aligned at the bottom. Vertical alignment has no effect if the screen is scrollable.")
 public int AlignVertical() {
   return verticalAlignment;
 }

 /**
  * Sets the vertical alignment for contents of the screen
  *
  * @param alignment
  */
 @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
     defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
 @SimpleProperty
 public void AlignVertical(int alignment) {
   try {
     // notice that the throw will prevent the alignment from being changed
     // if the argument is illegal
     alignmentSetter.setVerticalAlignment(alignment);
     verticalAlignment = alignment;
   } catch (IllegalArgumentException e) {
     this.dispatchErrorOccurredEvent(this, "VerticalAlignment",
         ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
   }
 }

 /**
  * Returns the type of open screen animation (default, fade, zoom, slidehorizontal,
  * slidevertical and none).
  *
  * @return open screen animation
  */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "The animation for switching to another screen. Valid" +
    " options are default, fade, zoom, slidehorizontal, slidevertical, and none"    )
  public String OpenScreenAnimation() {
    return openAnimType;
  }

  /**
   * Sets the animation type for the transition to another screen.
   *
   * @param animType the type of animation to use for the transition
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION,
    defaultValue = "default")
  @SimpleProperty
  public void OpenScreenAnimation(String animType) {
    if ((animType != "default") &&
      (animType != "fade") && (animType != "zoom") && (animType != "slidehorizontal") &&
      (animType != "slidevertical") && (animType != "none")) {
      this.dispatchErrorOccurredEvent(this, "Screen",
        ErrorMessages.ERROR_SCREEN_INVALID_ANIMATION, animType);
      return;
    }
    openAnimType = animType;
  }

 /**
  * Returns the type of close screen animation (default, fade, zoom, slidehorizontal,
  * slidevertical and none).
  *
  * @return open screen animation
  */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "The animation for closing current screen and returning " +
    " to the previous screen. Valid options are default, fade, zoom, slidehorizontal, " +
    "slidevertical, and none")
  public String CloseScreenAnimation() {
    return closeAnimType;
  }

  /**
   * Sets the animation type for the transition of this form closing and returning
   * to a form behind it in the activity stack.
   *
   * @param animType the type of animation to use for the transition
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION,
    defaultValue = "default")
  @SimpleProperty
  public void CloseScreenAnimation(String animType) {
    if ((animType != "default") &&
      (animType != "fade") && (animType != "zoom") && (animType != "slidehorizontal") &&
      (animType != "slidevertical") && (animType != "none")) {
      this.dispatchErrorOccurredEvent(this, "Screen",
        ErrorMessages.ERROR_SCREEN_INVALID_ANIMATION, animType);
      return;
    }
    closeAnimType = animType;
  }

  /*
   * Used by ListPicker, and ActivityStarter to get this Form's current opening transition
   * animation
   */
  public String getOpenAnimType() {
    return openAnimType;
  }

  /**
   * Specifies the name of the application icon.
   *
   * @param name the name of the application icon
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void Icon(String name) {
    // We don't actually need to do anything.
  }

  /**
   * Specifies the Version Code.
   *
   * @param vCode the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
    defaultValue = "1")
  @SimpleProperty(userVisible = false,
    description = "An integer value which must be incremented each time a new Android "
    +  "Application Package File (APK) is created for the Google Play Store.")
  public void VersionCode(int vCode) {
    // We don't actually need to do anything.
  }

  /**
   * Specifies the Version Name.
   *
   * @param vName the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "1.0")
  @SimpleProperty(userVisible = false,
    description = "A string which can be changed to allow Google Play "
    + "Store users to distinguish between different versions of the App.")
  public void VersionName(String vName) {
    // We don't actually need to do anything.
  }

  /**
   * Sizing Property Setter
   *
   * @param
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SIZING,
      defaultValue = "Fixed")
  @SimpleProperty(userVisible = false,
    // This desc won't apprear as a tooltip, since there's no block, but we'll keep it with the source.
    description = "If set to fixed,  screen layouts will be created for a single fixed-size screen and autoscaled. " +
                  "If set to responsive, screen layouts will use the actual resolution of the device.  " +
                  "See the documentation on responsive design in App Inventor for more information. " +
                  "This property appears on Screen1 only and controls the sizing for all screens in the app.")
  public void Sizing(String value) {
    // This is used by the project and build server.
    // We also use it to adjust sizes
    Log.d(LOG_TAG, "Sizing(" + value + ")");
    formWidth = (int)((float) this.getResources().getDisplayMetrics().widthPixels / deviceDensity);
    formHeight = (int)((float) this.getResources().getDisplayMetrics().heightPixels / deviceDensity);
    if (value.equals("Fixed")) {
      sCompatibilityMode = true;
      formWidth /= compatScalingFactor;
      formHeight /= compatScalingFactor;
    } else {
      sCompatibilityMode = false;
    }
    scaleLayout.setScale(sCompatibilityMode ? compatScalingFactor : 1.0f);
    if (frameLayout != null) {
      frameLayout.invalidate();
    }
    Log.d(LOG_TAG, "formWidth = " + formWidth + " formHeight = " + formHeight);
  }

  // public String Sizing() {
  //   if (compatibilityMode) {
  //     return "Fixed";
  //   } else {
  //     return "Responsive";
  //   }
  // }

  /**
   * Specifies the App Name.
   *
   * @param aName the display name of the installed application in the phone
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "")
  @SimpleProperty(userVisible = false,
    description = "This is the display name of the installed application in the phone." +
        "If the AppName is blank, it will be set to the name of the project when the project is built.")
  public void AppName(String aName) {
    // We don't actually need to do anything.
  }

  /**
   * Width property getter method.
   *
   * @return  width property used by the layout
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "Screen width (x-size).")
  public int Width() {
    Log.d(LOG_TAG, "Form.Width = " + formWidth);
    return formWidth;
  }

  /**
   * Height property getter method.
   *
   * @return  height property used by the layout
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "Screen height (y-size).")
  public int Height() {
    Log.d(LOG_TAG, "Form.Height = " + formHeight);
    return formHeight;
  }

  /**
   * Display a new form.
   *
   * @param nextFormName the name of the new form to display
   */
  // This is called from runtime.scm when a "open another screen" block is executed.
  public static void switchForm(String nextFormName) {
    if (activeForm != null) {
      activeForm.startNewForm(nextFormName, null);
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }

  /**
   * Display a new form and pass a startup value to the new form.
   *
   * @param nextFormName the name of the new form to display
   * @param startValue the start value to pass to the new form
   */
  // This is called from runtime.scm when a "open another screen with start value" block is
  // executed.  Note that startNewForm will JSON encode the start value
  public static void switchFormWithStartValue(String nextFormName, Object startValue) {
    Log.i(LOG_TAG, "Open another screen with start value:" + nextFormName);
    if (activeForm != null) {
      activeForm.startNewForm(nextFormName, startValue);
      } else {
        throw new IllegalStateException("activeForm is null");
    }
  }

  // This JSON encodes the startup value
  protected void startNewForm(String nextFormName, Object startupValue) {
    Log.i(LOG_TAG, "startNewForm:" + nextFormName);
    Intent activityIntent = new Intent();
    // Note that the following is dependent on form generated class names being the same as
    // their form names and all forms being in the same package.
    activityIntent.setClassName(this, getPackageName() + "." + nextFormName);
    String functionName = (startupValue == null) ? "open another screen" :
      "open another screen with start value";
    String jValue;
    if (startupValue != null) {
      Log.i(LOG_TAG, "StartNewForm about to JSON encode:" + startupValue);
      jValue = jsonEncodeForForm(startupValue, functionName);
      Log.i(LOG_TAG, "StartNewForm got JSON encoding:" + jValue);
    } else{
      jValue = "";
    }
    activityIntent.putExtra(ARGUMENT_NAME, jValue);
    // Save the nextFormName so that it can be passed to the OtherScreenClosed event in the
    // future.
    this.nextFormName = nextFormName;
    Log.i(LOG_TAG, "about to start new form" + nextFormName);
    try {
      Log.i(LOG_TAG, "startNewForm starting activity:" + activityIntent);
      startActivityForResult(activityIntent, SWITCH_FORM_REQUEST_CODE);
      AnimationUtil.ApplyOpenScreenAnimation(this, openAnimType);
    } catch (ActivityNotFoundException e) {
      dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_SCREEN_NOT_FOUND, nextFormName);
    }
  }

  // functionName is used for including in the error message to be shown
  // if the JSON encoding fails
  protected static String jsonEncodeForForm(Object value, String functionName) {
    String jsonResult = "";
    Log.i(LOG_TAG, "jsonEncodeForForm -- creating JSON representation:" + value.toString());
    try {
      // TODO(hal): check that this is OK for raw strings
      jsonResult = JsonUtil.getJsonRepresentation(value);
      Log.i(LOG_TAG, "jsonEncodeForForm -- got JSON representation:" + jsonResult);
    } catch (JSONException e) {
      activeForm.dispatchErrorOccurredEvent(activeForm, functionName,
          // showing the bad value here will produce an ugly error on the phone, but it's
          // more useful than not showing the value
          ErrorMessages.ERROR_SCREEN_BAD_VALUE_FOR_SENDING, value.toString());
    }
    return jsonResult;
  }

  @SimpleEvent(description = "Event raised when another screen has closed and control has " +
      "returned to this screen.")
  public void OtherScreenClosed(String otherScreenName, Object result) {
    Log.i(LOG_TAG, "Form " + formName + " OtherScreenClosed, otherScreenName = " +
        otherScreenName + ", result = " + result.toString());
    EventDispatcher.dispatchEvent(this, "OtherScreenClosed", otherScreenName, result);
  }


  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return this;
  }

  // ComponentContainer implementation

  @Override
  public Activity $context() {
    return this;
  }

  @Override
  public Form $form() {
    return this;
  }

  @Override
  public void $add(AndroidViewComponent component) {
    viewLayout.add(component);
  }

  public float deviceDensity(){
    return this.deviceDensity;
  }

  public float compatScalingFactor() {
    return this.compatScalingFactor;
  }

  @Override
  public void setChildWidth(final AndroidViewComponent component, int width) {
    int cWidth = Width();
    if (cWidth == 0) {          // We're not really ready yet...
      final int fWidth = width;
      androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            System.err.println("(Form)Width not stable yet... trying again");
            setChildWidth(component, fWidth);
          }
        }, 100);                // Try again in 1/10 of a second
    }
    System.err.println("Form.setChildWidth(): width = " + width + " parent Width = " + cWidth + " child = " + component);
    if (width <= LENGTH_PERCENT_TAG) {
      width = cWidth * (- (width - LENGTH_PERCENT_TAG)) / 100;
//      System.err.println("Form.setChildWidth(): Setting " + component + " lastwidth to " + width);
    }

    component.setLastWidth(width);

    // A form is a vertical layout.
    ViewUtil.setChildWidthForVerticalLayout(component.getView(), width);
  }

  @Override
  public void setChildHeight(final AndroidViewComponent component, int height) {
    int cHeight = Height();
    if (cHeight == 0) {         // Not ready yet...
      final int fHeight = height;
      androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            System.err.println("(Form)Height not stable yet... trying again");
            setChildHeight(component, fHeight);
          }
        }, 100);                // Try again in 1/10 of a second
    }
    if (height <= LENGTH_PERCENT_TAG) {
      height = Height() * (- (height - LENGTH_PERCENT_TAG)) / 100;
    }

    component.setLastHeight(height);

    // A form is a vertical layout.
    ViewUtil.setChildHeightForVerticalLayout(component.getView(), height);
  }

  /*
   * This is called from runtime.scm at the beginning of each event handler.
   * It allows runtime.scm to know which form environment should be used for
   * looking up symbols. The active form is the form that is currently
   * (or was most recently) dispatching an event.
   */
  public static Form getActiveForm() {
    return activeForm;
  }


  /**
   * Returns the string that was passed to this screen when it was opened
   *
   * @return StartupText
   */
  // This is called from runtime.scm when a "get plain start text" block is executed.
  public static String getStartText() {
    if (activeForm != null) {
      return activeForm.startupValue;
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }

  /**
   * Returns the value that was passed to this screen when it was opened
   *
   * @return StartValue
   */
  // TODO(hal): cache this?
  // Note: This is called as a primitive from runtime.scm and it returns an arbitrary Java object.
  // Therefore it must be explicitly sanitized by runtime, unlike methods, which
  // are sanitized via call-component-method.
  public static Object getStartValue() {
    if (activeForm != null) {
      return decodeJSONStringForForm(activeForm.startupValue, "get start value");
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }


  /**
   * Closes the current screen, as opposed to finishApplication, which
   * exits the entire application.
   */
  // This is called from runtime.scm when a "close screen" block is executed.
  public static void finishActivity() {
    if (activeForm != null) {
      activeForm.closeForm(null);
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }

  // This is called from runtime.scm when a "close screen with value" block is executed.
  public static void finishActivityWithResult(Object result) {
    if (activeForm != null) {
      if (activeForm instanceof ReplForm) {
        ((ReplForm)activeForm).setResult(result);
        activeForm.closeForm(null);        // This will call RetValManager.popScreen()
      } else {
        String jString = jsonEncodeForForm(result, "close screen with value");
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_NAME, jString);
        activeForm.closeForm(resultIntent);
      }
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }

  // This is called from runtime.scm when a "close screen with plain text" block is executed.
  public static void finishActivityWithTextResult(String result) {
    if (activeForm != null) {
      Intent resultIntent = new Intent();
      resultIntent.putExtra(RESULT_NAME, result);
      activeForm.closeForm(resultIntent);
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }


  protected void closeForm(Intent resultIntent) {
    if (resultIntent != null) {
      setResult(Activity.RESULT_OK, resultIntent);
    }
    finish();
    AnimationUtil.ApplyCloseScreenAnimation(this, closeAnimType);
  }

  // This is called from runtime.scm when a "close application" block is executed.
  public static void finishApplication() {
    if (activeForm != null) {
      activeForm.closeApplicationFromBlocks();
    } else {
      throw new IllegalStateException("activeForm is null");
    }
  }

  protected void closeApplicationFromBlocks() {
    closeApplication();
  }

  private void closeApplicationFromMenu() {
    closeApplication();
  }

  private void closeApplication() {
    // In a multi-screen application, only Screen1 can successfully call System.exit(0). Here, we
    // set applicationIsBeingClosed to true. If this is not Screen1, when we call finish() below,
    // the previous form's onResume method will be called. In onResume, we check
    // applicationIsBeingClosed and call closeApplication again. The stack of forms will unwind
    // until we get back to Screen1; then we'll call System.exit(0) below.
    applicationIsBeingClosed = true;

    finish();

    if (formName.equals("Screen1")) {
      // I know that this is frowned upon in Android circles but I really think that it's
      // confusing to users if the exit button doesn't really stop everything, including other
      // forms in the app (when we support them), non-UI threads, etc.  We might need to be
      // careful about this is we ever support services that start up on boot (since it might
      // mean that the only way to restart that service) is to reboot but that's a long way off.
      System.exit(0);
    }
  }

  // Configure the system menu to include items to kill the application and to show "about"
  // information

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // This procedure is called only once.  To change the items dynamically
    // we would use onPrepareOptionsMenu.
    super.onCreateOptionsMenu(menu);
    // add the menu items
    // Comment out the next line if we don't want the exit button
    addExitButtonToMenu(menu);
    addAboutInfoToMenu(menu);
    for (OnCreateOptionsMenuListener onCreateOptionsMenuListener : onCreateOptionsMenuListeners) {
      onCreateOptionsMenuListener.onCreateOptionsMenu(menu);
    }
    return true;
  }

  public void addExitButtonToMenu(Menu menu) {
    MenuItem stopApplicationItem = menu.add(Menu.NONE, Menu.NONE, Menu.FIRST,
    "Stop this application")
    .setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem item) {
        showExitApplicationNotification();
        return true;
      }
    });
    stopApplicationItem.setIcon(android.R.drawable.ic_notification_clear_all);
  }

  public void addAboutInfoToMenu(Menu menu) {
    MenuItem aboutAppItem = menu.add(Menu.NONE, Menu.NONE, 2,
    "About this application")
    .setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem item) {
        showAboutApplicationNotification();
        return true;
      }
    });
    aboutAppItem.setIcon(android.R.drawable.sym_def_app_icon);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    for (OnOptionsItemSelectedListener onOptionsItemSelectedListener : onOptionsItemSelectedListeners) {
      if (onOptionsItemSelectedListener.onOptionsItemSelected(item)) {
        return true;
      }
    }
    return false;
  }

  private void showExitApplicationNotification() {
    String title = "Stop application?";
    String message = "Stop this application and exit? You'll need to relaunch " +
        "the application to use it again.";
    String positiveButton = "Stop and exit";
    String negativeButton = "Don't stop";
    // These runnables are passed to twoButtonAlert.  They perform the corresponding actions
    // when the button is pressed.   Here there's nothing to do for "don't stop" and cancel
    Runnable stopApplication = new Runnable() {public void run () {closeApplicationFromMenu();}};
    Runnable doNothing = new Runnable () {public void run() {}};
    Notifier.twoButtonDialog(
        this,
        message,
        title,
        positiveButton,
        negativeButton,
        false, // cancelable is false
        stopApplication,
        doNothing,
        doNothing);
  }

  private String yandexTranslateTagline = "";

  void setYandexTranslateTagline(){
    yandexTranslateTagline = "<p><small>Language translation powered by Yandex.Translate</small></p>";
  }

  private void showAboutApplicationNotification() {
    String title = "About this app";
    String MITtagline = "<p><small><em>Invented with MIT App Inventor<br>appinventor.mit.edu</em></small></p>";
    // Users can hide the taglines by including an HTML open comment <!-- in the about screen message
    String message = aboutScreen + MITtagline + yandexTranslateTagline;
    message = message.replaceAll("\\n", "<br>"); // Allow for line breaks in the string.
    String buttonText ="Got it";
    Notifier.oneButtonAlert(this, message, title, buttonText);
  }

  // This is called from clear-current-form in runtime.scm.
  public void clear() {
    viewLayout.getLayoutManager().removeAllViews();
    frameLayout.removeAllViews();
    frameLayout = null;
    // Set all screen properties to default values.
    defaultPropertyValues();
    onStopListeners.clear();
    onNewIntentListeners.clear();
    onResumeListeners.clear();
    onPauseListeners.clear();
    onDestroyListeners.clear();
    onInitializeListeners.clear();
    onCreateOptionsMenuListeners.clear();
    onOptionsItemSelectedListeners.clear();
    screenInitialized = false;
    System.err.println("Form.clear() About to do moby GC!");
    System.gc();
    dimChanges.clear();
  }

  public void deleteComponent(Object component) {
    if (component instanceof OnStopListener) {
      OnStopListener onStopListener = (OnStopListener) component;
      if (onStopListeners.contains(onStopListener)) {
        onStopListeners.remove(onStopListener);
      }
    }
    if (component instanceof OnNewIntentListener) {
      OnNewIntentListener onNewIntentListener = (OnNewIntentListener) component;
      if (onNewIntentListeners.contains(onNewIntentListener)) {
        onNewIntentListeners.remove(onNewIntentListener);
      }
    }
    if (component instanceof OnResumeListener) {
      OnResumeListener onResumeListener = (OnResumeListener) component;
      if (onResumeListeners.contains(onResumeListener)) {
        onResumeListeners.remove(onResumeListener);
      }
    }
    if (component instanceof OnPauseListener) {
      OnPauseListener onPauseListener = (OnPauseListener) component;
      if (onPauseListeners.contains(onPauseListener)) {
        onPauseListeners.remove(onPauseListener);
      }
    }
    if (component instanceof OnDestroyListener) {
      OnDestroyListener onDestroyListener = (OnDestroyListener) component;
      if (onDestroyListeners.contains(onDestroyListener)) {
        onDestroyListeners.remove(onDestroyListener);
      }
    }
    if (component instanceof OnInitializeListener) {
      OnInitializeListener onInitializeListener = (OnInitializeListener) component;
      if (onInitializeListeners.contains(onInitializeListener)) {
        onInitializeListeners.remove(onInitializeListener);
      }
    }
    if (component instanceof OnCreateOptionsMenuListener) {
      OnCreateOptionsMenuListener onCreateOptionsMenuListener = (OnCreateOptionsMenuListener) component;
      if (onCreateOptionsMenuListeners.contains(onCreateOptionsMenuListener)) {
        onCreateOptionsMenuListeners.remove(onCreateOptionsMenuListener);
      }
    }
    if (component instanceof OnOptionsItemSelectedListener) {
      OnOptionsItemSelectedListener onOptionsItemSelectedListener = (OnOptionsItemSelectedListener) component;
      if (onOptionsItemSelectedListeners.contains(onOptionsItemSelectedListener)) {
        onOptionsItemSelectedListeners.remove(onOptionsItemSelectedListener);
      }
    }
    if (component instanceof Deleteable) {
      ((Deleteable) component).onDelete();
    }
  }

  public void dontGrabTouchEventsForComponent() {
    // The following call results in the Form not grabbing our events and
    // handling dragging on its own, which it wants to do to handle scrolling.
    // Its effect only lasts long as the current set of motion events
    // generated during this touch and drag sequence.  Consequently, if a
    // component wants to handle dragging it needs to call this in the
    // onTouchEvent of its View.
    frameLayout.requestDisallowInterceptTouchEvent(true);
  }


  // This is used by Repl to throttle error messages which can get out of
  // hand, e.g. if triggered by Accelerometer.
  protected boolean toastAllowed() {
    long now = System.nanoTime();
    if (now > lastToastTime + minimumToastWait) {
      lastToastTime = now;
      return true;
    }
    return false;
  }

  // This is used by runtime.scm to call the Initialize of a component.
  public void callInitialize(Object component) throws Throwable {
    Method method;
    try {
      method = component.getClass().getMethod("Initialize", (Class<?>[]) null);
    } catch (SecurityException e) {
      Log.i(LOG_TAG, "Security exception " + e.getMessage());
      return;
    } catch (NoSuchMethodException e) {
      //This is OK.
      return;
    }
    try {
      Log.i(LOG_TAG, "calling Initialize method for Object " + component.toString());
      method.invoke(component, (Object[]) null);
    } catch (InvocationTargetException e){
      Log.i(LOG_TAG, "invoke exception: " + e.getMessage());
      throw e.getTargetException();
    }
  }

  /**
   * Perform some action related to fullscreen video display.
   * @param action
   *          Can be any of the following:
   *          <ul>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_DURATION}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_FULLSCREEN}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_PAUSE}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_PLAY}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_SEEK}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_SOURCE}
   *          </li>
   *          <li>
   *          {@link com.google.appinventor.components.runtime.util.FullScreenVideoUtil#FULLSCREEN_VIDEO_ACTION_STOP}
   *          </li>
   *          </ul>
   * @param source
   *          The VideoPlayer to use in some actions.
   * @param data
   *          Used by the method. This object varies depending on the action.
   * @return Varies depending on what action was passed in.
   */
  public synchronized Bundle fullScreenVideoAction(int action, VideoPlayer source, Object data) {
    return fullScreenVideoUtil.performAction(action, source, data);
  }

  private void setBackground(View bgview) {
    Drawable setDraw = backgroundDrawable;
    if (backgroundImagePath != "" && setDraw != null) {
      setDraw = backgroundDrawable.getConstantState().newDrawable();
      setDraw.setColorFilter((backgroundColor != Component.COLOR_DEFAULT) ? backgroundColor : Component.COLOR_WHITE,
        PorterDuff.Mode.DST_OVER);
    } else {
      setDraw = new ColorDrawable(
        (backgroundColor != Component.COLOR_DEFAULT) ? backgroundColor : Component.COLOR_WHITE);
    }
    ViewUtil.setBackgroundImage(bgview, setDraw);
    bgview.invalidate();
  }

  public static boolean getCompatibilityMode() {
    return sCompatibilityMode;
  }

}
