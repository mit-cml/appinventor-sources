// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.INTERNET;
import static com.google.appinventor.components.runtime.util.PaintUtil.hexStringToInt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.common.HorizontalAlignment;
import com.google.appinventor.components.common.Permission;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.ScreenAnimation;
import com.google.appinventor.components.common.ScreenOrientation;
import com.google.appinventor.components.common.VerticalAlignment;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.multidex.MultiDex;
import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.FullScreenVideoUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.ScreenDensityUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;


/**
 * Top-level component containing all other components in the program.
 *
 * @internaldoc
 * Component underlying activities and UI apps, not directly accessible to Simple programmers.
 *
 * <p>This is the root container of any Android activity and also the
 *     superclass for Simple/Android UI applications.</p>
 *
 * <p>The main form is always named "Screen1".</p>
 *
 * <p>NOTE WELL: There are many places in the code where the name "Screen1" is
 *     directly referenced. If we ever change App Inventor to support renaming
 *     screens and Screen1 in particular, we need to make sure we find all those
 *     places and make the appropriate code changes.</p>
 *
 */

@DesignerComponent(version = YaVersion.FORM_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Top-level component containing all other components in the program",
    showOnPalette = false)
@SimpleObject
@UsesPermissions({INTERNET})
public class Form extends AppInventorCompatActivity
    implements Component, ComponentContainer, HandlesEventDispatching,
    OnGlobalLayoutListener {

  private static final String LOG_TAG = "Form";

  private static final String RESULT_NAME = "APP_INVENTOR_RESULT";

  private static final String ARGUMENT_NAME = "APP_INVENTOR_START";

  public static final String APPINVENTOR_URL_SCHEME = "appinventor";

  public static final String ASSETS_PREFIX = "file:///android_asset/";

  private static final int DEFAULT_PRIMARY_COLOR_DARK =
      hexStringToInt(ComponentConstants.DEFAULT_PRIMARY_DARK_COLOR);
  private static final int DEFAULT_ACCENT_COLOR =
      hexStringToInt(ComponentConstants.DEFAULT_ACCENT_COLOR);

  private List<Component> allChildren = new ArrayList<>();

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

  protected final Handler androidUIHandler = new Handler();

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
  protected String title = "";

  private String backgroundImagePath = "";
  private Drawable backgroundDrawable;
  private boolean usesDefaultBackground;
  private boolean usesDarkTheme;

  // Layout
  private LinearLayout viewLayout;

  // translates App Inventor alignment codes to Android gravity
  private AlignmentUtil alignmentSetter;

  // The alignment for this component's LinearLayout
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;

  // Represents the transition animation type.
  private ScreenAnimation openAnimType = ScreenAnimation.Default;
  private ScreenAnimation closeAnimType = ScreenAnimation.Default;

  // Syle information
  private int primaryColor = DEFAULT_PRIMARY_COLOR;
  private int primaryColorDark = DEFAULT_PRIMARY_COLOR_DARK;
  private int accentColor = DEFAULT_ACCENT_COLOR;

  private FrameLayout frameLayout;
  private boolean scrollable;

  private boolean highContrast;
  private boolean bigDefaultText;

  private ScaledFrameLayout scaleLayout;
  private static boolean sCompatibilityMode;

  private static boolean showListsAsJson;

  private final Set<String> permissions = new HashSet<String>();

  private FileScope defaultFileScope = FileScope.App;

  // Application lifecycle related fields
  private final HashMap<Integer, ActivityResultListener> activityResultMap = Maps.newHashMap();
  private final Map<Integer, Set<ActivityResultListener>> activityResultMultiMap = Maps.newHashMap();
  private final Set<OnStopListener> onStopListeners = Sets.newHashSet();
  private final Set<OnClearListener> onClearListeners = Sets.newHashSet();
  private final Set<OnNewIntentListener> onNewIntentListeners = Sets.newHashSet();
  private final Set<OnResumeListener> onResumeListeners = Sets.newHashSet();
  private final Set<OnOrientationChangeListener> onOrientationChangeListeners = Sets.newHashSet();
  private final Set<OnPauseListener> onPauseListeners = Sets.newHashSet();
  private final Set<OnDestroyListener> onDestroyListeners = Sets.newHashSet();

  // AppInventor lifecycle: listeners for the Initialize Event
  private final Set<OnInitializeListener> onInitializeListeners = Sets.newHashSet();

  // Listeners for options menu.
  private final Set<OnCreateOptionsMenuListener> onCreateOptionsMenuListeners = Sets.newHashSet();
  private final Set<OnOptionsItemSelectedListener> onOptionsItemSelectedListeners = Sets.newHashSet();

  // Listeners for permission results
  private final HashMap<Integer, PermissionResultHandler> permissionHandlers = Maps.newHashMap();

  private final Random permissionRandom = new Random(); // Used for generating nonces

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

  private boolean actionBarEnabled = false;
  private boolean keyboardShown = false;

  private ProgressDialog progress;
  private static boolean _initialized = false;

  // It should be changed from 100000 to 65535 if the functionality to extend
  // FragmentActivity is added in future.
  public static final int MAX_PERMISSION_NONCE = 100000;



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
  // private ArrayList<PercentStorageRecord> dimChanges = new ArrayList();
  private LinkedHashMap<Integer, PercentStorageRecord> dimChanges = new LinkedHashMap();

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

    // This version is for production apps. See {@link ReplForm#onCreate} for the REPL version,
    // which overrides this method.
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(LOG_TAG, "Uncaught Exception", e);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(Form.this, "<unknown>",
                ErrorMessages.ERROR_UNCAUGHT_EXCEPTION_IN_THREAD, e.toString());
          }
        });
      }
    });

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

    populatePermissions();

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

  /**
   * Builds a set of permissions requested by the app from the package manifest.
   */
  private void populatePermissions() {
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
          PackageManager.GET_PERMISSIONS);
      Collections.addAll(permissions, packageInfo.requestedPermissions);
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception while attempting to learn permissions.", e);
    }
  }

  protected void defaultPropertyValues() {
    if (isRepl()) {
      ActionBar(actionBarEnabled);
    } else {
      ActionBar(themeHelper.hasActionBar());
    }
    Scrollable(false);       // frameLayout is created in Scrollable()
    HighContrast(false);
    BigDefaultText(false);
    Sizing("Responsive");    // Note: Only the Screen1 value is used as this is per-project
    AboutScreen("");
    BackgroundImage("");
    AlignHorizontal(ComponentConstants.GRAVITY_LEFT);
    AlignVertical(ComponentConstants.GRAVITY_TOP);
    Title("");
    ShowStatusBar(true);
    TitleVisible(true);
    ShowListsAsJson(true);  // Note: Only the Screen1 value is used as this is per-project
    ActionBar(false);
    AccentColor(DEFAULT_ACCENT_COLOR);
    PrimaryColor(DEFAULT_PRIMARY_COLOR);
    PrimaryColorDark(DEFAULT_PRIMARY_COLOR_DARK);
    BackgroundColor(Component.COLOR_DEFAULT);
    OpenScreenAnimationAbstract(ScreenAnimation.Default);
    CloseScreenAnimationAbstract(ScreenAnimation.Default);
    DefaultFileScope(FileScope.App);
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
                  if (savedLayout != null) {
                    savedLayout.invalidate();
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
    int totalHeight = scaleLayout.getRootView().getHeight();
    int scaledHeight = scaleLayout.getHeight();
    int heightDiff = totalHeight - scaledHeight;
    // int[] position = new int[2];
    // scaleLayout.getLocationInWindow(position);
    // int contentViewTop = position[1];
    float diffPercent = (float) heightDiff / (float) totalHeight;
    Log.d(LOG_TAG, "onGlobalLayout(): diffPercent = " + diffPercent);

    if(diffPercent < 0.25) {    // 0.25 is kind of arbitrary
      Log.d(LOG_TAG, "keyboard hidden!");
      if (keyboardShown) {
        keyboardShown = false;
        if (sCompatibilityMode) { // Put us back in "Fixed" Mode
          scaleLayout.setScale(compatScalingFactor);
          scaleLayout.invalidate();
        }
      }
    } else {
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
   * that the closing screen animation is applied.
   */
  @Override
  public void onBackPressed() {
    if (!BackPressed()) {
      AnimationUtil.ApplyCloseScreenAnimation(this, closeAnimType);
      super.onBackPressed();
    }
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
      // Many components are interested in this request (e.g., Texting, PhoneCall)
      Set<ActivityResultListener> listeners = activityResultMultiMap.get(requestCode);
      if (listeners != null) {
        for (ActivityResultListener listener : listeners.toArray(new ActivityResultListener[0])) {
          listener.resultReturned(requestCode, resultCode, data);
        }
      }
    }
  }

  // functionName is a string to include in the error message that will be shown
  // if the JSON decoding fails
  private  static Object decodeJSONStringForForm(String jsonString, String functionName) {
    Log.i(LOG_TAG, "decodeJSONStringForForm -- decoding JSON representation:" + jsonString);
    Object valueFromJSON = "";
    try {
      valueFromJSON = JsonUtil.getObjectFromJson(jsonString, true);
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

  /**
   * Register a {@code listener} for the given {@code requestCode}. This is used to simulate
   * broadcast receivers as a workaround for PhoneCall and Texting handlers related to initiating
   * calls/messages.
   *
   * @param listener The object to report activity results to for the given request code
   */
  public void registerForActivityResult(ActivityResultListener listener, int requestCode) {
    Set<ActivityResultListener> listeners = activityResultMultiMap.get(requestCode);
    if (listeners == null) {
      listeners = Sets.newHashSet();
      activityResultMultiMap.put(requestCode, listeners);
    }
    listeners.add(listener);
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

    // Remove any simulated broadcast receivers
    Iterator<Map.Entry<Integer, Set<ActivityResultListener>>> it =
        activityResultMultiMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, Set<ActivityResultListener>> entry = it.next();
      entry.getValue().remove(listener);
      if (entry.getValue().size() == 0) {
        it.remove();
      }
    }
  }

  void ReplayFormOrientation() {
    // We first make a copy of the existing dimChanges list
    // because while we are replaying it, it is being appended to
    Log.d(LOG_TAG, "ReplayFormOrientation()");
    LinkedHashMap<Integer, PercentStorageRecord> temp = (LinkedHashMap<Integer, PercentStorageRecord>) dimChanges.clone();
    dimChanges.clear();         // Empties it out
    // Iterate temp
    for (PercentStorageRecord r : temp.values()) {
      if (r.dim == PercentStorageRecord.Dim.HEIGHT) {
        r.component.Height(r.length);
      } else {
        r.component.Width(r.length);
      }
    }
  }

  private Integer generateHashCode(AndroidViewComponent component, PercentStorageRecord.Dim dim) {
    if (dim == PercentStorageRecord.Dim.HEIGHT) {
      return component.hashCode() * 2 + 1;
    } else {
      return component.hashCode() * 2;
    }
  }

  public void registerPercentLength(AndroidViewComponent component, int length, PercentStorageRecord.Dim dim) {
    PercentStorageRecord r = new PercentStorageRecord(component, length, dim);
    Integer key = generateHashCode(component, dim);
    dimChanges.put(key, r);
  }

  public void unregisterPercentLength(AndroidViewComponent component, PercentStorageRecord.Dim dim) {
    // iterate map, remove all entry match this
    dimChanges.remove(generateHashCode(component, dim));
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

  public void registerForOnOrientationChange(OnOrientationChangeListener component) {
    onOrientationChangeListeners.add(component);
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

  public void registerForOnClear(OnClearListener component) {
    onClearListeners.add(component);
  }

  @Override
  protected void onDestroy() {
    // for debugging and future growth
    Log.i(LOG_TAG, "Form " + formName + " got onDestroy");

    // Unregister events for components in this form.
    EventDispatcher.removeDispatchDelegate(this);

    for (OnDestroyListener onDestroyListener : onDestroyListeners) {
      onDestroyListener.onDestroy();
    }

    // call super method at the end to delegate the destruction of the app to the parent
    super.onDestroy();
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
   * App Inventor component definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.ComponentDescriptorGenerator},
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
   * App Inventor component definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.ComponentDescriptorGenerator},
   * respectively.  The actual implementation appears in {@code runtime.scm}.
   */
  @Override
  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void dispatchGenericEvent(Component component, String eventName,
      boolean notAlreadyHandled, Object[] args) {
    throw new UnsupportedOperationException();
  }

  @SimpleEvent(description = "The Initialize event is run when the Screen starts and is only run "
      + "once per screen.")
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
    for (OnOrientationChangeListener onOrientationChangeListener : onOrientationChangeListeners) {
      onOrientationChangeListener.onOrientationChange();
    }
    EventDispatcher.dispatchEvent(this, "ScreenOrientationChanged");
  }

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

  /**
   * Schedules a run of the PermissionDenied event handler for after the current stack of blocks finishes executing
   * on the UI thread.
   *
   * @param component The component that needs the denied permission.
   * @param functionName The function that triggers the denied permission.
   * @param exception The PermissionDenied exception
   */
  public void dispatchPermissionDeniedEvent(final Component component, final String functionName,
      final PermissionException exception) {
    exception.printStackTrace();
    dispatchPermissionDeniedEvent(component, functionName, exception.getPermissionNeeded());
  }

  /**
   * Schedules a run of the PermissionDenied event handler for after the current stack of blocks finishes executing
   * on the UI thread.
   *
   * @param component The component that needs the denied permission.
   * @param functionName The function that triggers the denied permission.
   * @param permissionName The name of the needed permission.
   */
  public void dispatchPermissionDeniedEvent(final Component component, final String functionName,
      final String permissionName) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PermissionDenied(component, functionName, permissionName);
      }
    });
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

  // This is like dispatchErrorOccurredEvent, except that it defaults to showing
  // a message dialog rather than an alert.   The app writer can override either of these behaviors,
  // but using the event dialog version frees the app writer from the need to explicitly override
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

  // This runtimeFormErrorOccurred can be called from runtime.scm in
  // the case of a runtime error.  The event is always signaled in the
  // active form. It triggers the normal Form error system which fires
  // the ErrorOccurred event. This can be handled by the App Inventor
  // programmer. If it isn't a Notifier (toast) is displayed showing
  // the error.
  public void runtimeFormErrorOccurredEvent(String functionName, int errorNumber, String message) {
    Log.d("FORM_RUNTIME_ERROR", "functionName is " + functionName);
    Log.d("FORM_RUNTIME_ERROR", "errorNumber is " + errorNumber);
    Log.d("FORM_RUNTIME_ERROR", "message is " + message);
    dispatchErrorOccurredEvent((Component) activeForm, functionName, errorNumber, message);
  }

  /**
   * Event to handle when the app user has denied a needed permission.
   *
   * @param component The component that needs the denied permission.
   * @param functionName The property or method of the component that needs the denied permission.
   * @param permissionName The name of the permission that has been denied by the user.
   */
  @SimpleEvent
  public void PermissionDenied(
      Component component,
      String functionName,
      @Options(Permission.class) String permissionName) {
    if (permissionName.startsWith("android.permission.")) {
      // Forward compatibility with iOS so that we don't have to pass around Android-specific names
      permissionName = permissionName.replace("android.permission.", "");
    }
    if (!EventDispatcher.dispatchEvent(this, "PermissionDenied", component, functionName, permissionName)) {
      dispatchErrorOccurredEvent(component, functionName, ErrorMessages.ERROR_PERMISSION_DENIED, permissionName);
    }
  }

  /**
   * Event to handle when the app user has granted a needed permission. This event is only run when permission is
   * granted in response to the {@link #AskForPermission(String)} method.
   *
   * @param permissionName The name of the permission that was granted by the user.
   */
  @SimpleEvent(description = "Event to handle when the app user has granted a needed permission. "
      + "This event is only run when permission is granted in response to the AskForPermission "
      + "method.")
  public void PermissionGranted(@Options(Permission.class) String permissionName) {
    if (permissionName.startsWith("android.permission.")) {
      // Forward compatibility with iOS so that we don't have to pass around Android-specific names
      permissionName = permissionName.replace("android.permission.", "");
    }
    EventDispatcher.dispatchEvent(this, "PermissionGranted", permissionName);
  }

  /**
   * Ask the user to grant access to a sensitive permission, such as `ACCESS_FINE_LOCATION`. This
   * block is typically used as part of a {@link #PermissionDenied(Component, String, String)}
   * event to ask for permission. If the user grants permission, the
   * {@link #PermissionGranted(String)} event will be run. If the user denies permission, the
   * {@link #PermissionDenied(Component, String, String)} event will be run.
   *
   *   **Note:** It is a best practice to only ask for permissions at the time they are needed,
   * which App Inventor components will do when necessary. You should not use `AskForPermission`
   * in your {@link #Initialize()} event unless access to that permission is critical to the
   * behavior of your app and is needed up front, such as location services for a navigation app.
   *
   * @param permissionName The name of the permission to request from the user.
   */
  @SimpleFunction(description = "Ask the user to grant access to a dangerous permission.")
  public void AskForPermission(@Options(Permission.class) String permissionName) {
    if (!permissionName.contains(".")) {
      permissionName = "android.permission." + permissionName;
    }
    askPermission(permissionName, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          PermissionGranted(permission);
        } else {
          PermissionDenied(Form.this, "RequestPermission", permission);
        }
      }
    });
  }


  /**
   * HighContrast property getter method.
   *
   * @return  true if we want high constrast mode
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
          description = "When checked, we will use high contrast mode")
  public boolean HighContrast() {
    return highContrast;
  }

  /**
   * When checked, there will be high contrast mode turned on.
   *
   * @param highContrast  true if the high contrast mode is on
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty
  public void HighContrast(boolean highContrast) {

    //this.scrollable = scrollable;
    this.highContrast=highContrast;
    setHighContrastRecursive(this, highContrast);
    recomputeLayout();
  }

  private static void setHighContrastRecursive(ComponentContainer container, boolean enabled) {
    for (Component child : container.getChildren()) {
      if (child instanceof ComponentContainer) {
        setHighContrastRecursive((ComponentContainer) child, enabled);
      } else if (child instanceof AccessibleComponent) {
        ((AccessibleComponent) child).setHighContrast(enabled);
      }
    }
  }


  /**
   * BigDefaultText property getter method.
   *
   * @return  true if we are in the big text mode
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
          description = "When checked, we will use high contrast mode")
  public boolean BigDefaultText() {
    return bigDefaultText;
  }

  /**
   * When checked, all default size text will be increased in size.
   *
   * @param bigDefaultText  true if the big text mode is on
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty
  public void BigDefaultText(boolean bigDefaultText) {

    //this.scrollable = scrollable;
    this.bigDefaultText=bigDefaultText;
    setBigDefaultTextRecursive(this, bigDefaultText);
    recomputeLayout();
  }


  private static void setBigDefaultTextRecursive(ComponentContainer container, boolean enabled) {
    for (Component child : container.getChildren()) {
      if (child instanceof ComponentContainer) {
        setBigDefaultTextRecursive((ComponentContainer) child, enabled);
      } else if (child instanceof AccessibleComponent) {
        ((AccessibleComponent) child).setLargeFont(enabled);
      }
    }
  }

  /**
   * Asks the user to grant access to a sensitive permission.
   */
  @SuppressWarnings({"RegularMethodName", "unused"})
  public void AskForPermissionAbstract(Permission permission) {
    // Usually when we are upgrading components to use dropdown blocks we make the Abstract function
    // the "true" function. But in this case I think it makes more sense to use the concrete one.
    AskForPermission(permission.toUnderlyingValue());
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
   * When checked, there will be a vertical scrollbar on the screen, and the height of the
   * application can exceed the physical height of the device. When unchecked, the application
   * height is constrained to the height of the device.
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
    boolean needsTitleBar = titleBar != null && titleBar.getParent() == frameWithTitle;
    frameWithTitle.removeAllViews();
    if (needsTitleBar) {
      frameWithTitle.addView(titleBar, new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
      ));
    }

    // Layout
    // ------frameWithTitle------
    // | [======titleBar======] |
    // | ------scaleLayout----- |
    // | | ----frameLayout--- | |
    // | | |                | | |
    // | | ------------------ | |
    // | ---------------------- |
    // --------------------------

    if (scrollable) {
      frameLayout = new ScrollView(this);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Nougat changes how ScrollView handles its content size. Here we force it to fill the viewport
        // in order to preserve the layout of apps developed prior to N that rely on the old behavior.
        ((ScrollView) frameLayout).setFillViewport(true);
      }
    } else {
      frameLayout = new FrameLayout(this);
    }
    frameLayout.addView(viewLayout.getLayoutManager(), new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    setBackground(frameLayout);

    Log.d(LOG_TAG, "About to create a new ScaledFrameLayout");
    scaleLayout = new ScaledFrameLayout(this);
    scaleLayout.addView(frameLayout, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    frameWithTitle.addView(scaleLayout, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
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
          frameWithTitle.requestLayout();
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
  @IsColor
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the `%type%`'s background color as an alpha-red-green-blue
   * integer.  If an {@link #BackgroundImage(String)} has been set, the color
   * change will not be visible until the {@link #BackgroundImage(String)} is removed.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    if (argb == Component.COLOR_DEFAULT) {
      usesDefaultBackground = true;
    } else {
      usesDefaultBackground = false;
      backgroundColor = argb;
    }
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
   * Specifies the path of the `%type%`'s background image. If there is both an `BackgroundImage`
   * and a {@link #BackgroundColor()} specified, only the `BackgroundImage` will be visible.
   *
   * @internaldoc
   * See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path the path of the background image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "The screen background image.")
  public void BackgroundImage(@Asset String path) {
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
   * Specifies the default scope used when components access files. Note that the
   * <a href="/reference/components/storage.html#File" target="_blank">File</a>
   * component has its own property for controlling file scopes.
   *
   * @param scope the desired scope to use by default during file accesses
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FILESCOPE,
      defaultValue = "App")
  @SimpleProperty(category = PropertyCategory.GENERAL, userVisible = false)
  public void DefaultFileScope(FileScope scope) {
    this.defaultFileScope = scope;
  }

  public FileScope DefaultFileScope() {
    return defaultFileScope;
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
    this.title = title;
    if (titleBar != null) {
      titleBar.setText(title);
    }
    setTitle(title);
    updateTitle();
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
   * Information about the screen. It appears when "About this Application" is selected from the
   * system menu. Use it to tell users about your app. In multiple screen apps, each screen has its
   * own `AboutScreen` info.
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
   * The title bar is the top gray bar on the screen. This property reports whether the title bar
   * is visible.
   *
   * @param show boolean
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TitleVisible(boolean show) {
    if (show != showTitle) {
      showTitle = show;
      if (actionBarEnabled) {
        actionBarEnabled = themeHelper.setActionBarVisible(show);
      } else {
        maybeShowTitleBar();
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
   * The status bar is the topmost bar on the screen. This property reports whether the status bar
   * is visible.
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
  public @Options(ScreenOrientation.class) String ScreenOrientation() {
    return ScreenOrientationAbstract().toUnderlyingValue();
  }

  /**
   * Returns the requested screen orientation.
   */
  @SuppressWarnings("RegularMethodName")
  public ScreenOrientation ScreenOrientationAbstract() {
    switch (getRequestedOrientation()) {
      case ActivityInfo.SCREEN_ORIENTATION_BEHIND:
        return ScreenOrientation.Behind;
      case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
        return ScreenOrientation.Landscape;
      case ActivityInfo.SCREEN_ORIENTATION_NOSENSOR:
        return ScreenOrientation.NoSensor;
      case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
        return ScreenOrientation.Portrait;
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR:
        return ScreenOrientation.Sensor;
      case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
        return ScreenOrientation.Unspecified;
      case ActivityInfo.SCREEN_ORIENTATION_USER:
        return ScreenOrientation.User;
      case ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR:
        return ScreenOrientation.FullSensor;
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
        return ScreenOrientation.ReverseLandscape;
      case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
        return ScreenOrientation.ReversePortrait;
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
        return ScreenOrientation.SensorLandscape;
      case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
        return ScreenOrientation.SensorPortrait;
      default:
        return ScreenOrientation.Unspecified;
    }
  }

  /**
   * Sets the requested screen orientation.
   */
  @SuppressWarnings("RegularMethodName")
  public void ScreenOrientationAbstract(ScreenOrientation orientation) {
    int orientationConst = orientation.getOrientationConstant();
    if (orientationConst > 5 && SdkLevel.getLevel() < SdkLevel.LEVEL_GINGERBREAD) {
      dispatchErrorOccurredEvent(this, "ScreenOrientation",
          ErrorMessages.ERROR_INVALID_SCREEN_ORIENTATION, orientation);
      return;
    }
    setRequestedOrientation(orientationConst);
  }

  /**
   * Declares the requested screen orientation, specified as a text value. Commonly used values are
   * `landscape`, `portrait`, `sensor`, `user` and `unspecified`. See the Android developer
   * documentation for the complete list of possible
   * [options](https://developer.android.com/reference/android/R.attr.html#screenOrientation).
   *
   * @param screenOrientation  the screen orientation as a string
   */
  @SuppressLint("SourceLockedOrientationActivity")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ORIENTATION,
      defaultValue = "unspecified", alwaysSend = true)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ScreenOrientation(@Options(ScreenOrientation.class) String screenOrientation) {
    // Make sure screenOrientation is a valid ScreenOrientation.
    ScreenOrientation orientation = ScreenOrientation.fromUnderlyingValue(screenOrientation);
    if (orientation == null) {
      dispatchErrorOccurredEvent(this, "ScreenOrientation",
          ErrorMessages.ERROR_INVALID_SCREEN_ORIENTATION, screenOrientation);
      return;
    }
    ScreenOrientationAbstract(orientation);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false, category = PropertyCategory.APPEARANCE)
  public void ActionBar(boolean enabled) {
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
      // ActionBar is available on SDK 11 or higher
      return;
    }
    if (actionBarEnabled != enabled) {
      setActionBarEnabled(enabled);
      if (enabled) {
        hideTitleBar();
        actionBarEnabled = themeHelper.setActionBarVisible(showTitle);
      } else {
        maybeShowTitleBar();
        actionBarEnabled = themeHelper.setActionBarVisible(false);
      }
      actionBarEnabled = enabled;
    }
  }

  /**
  * Returns a number that encodes how contents of the screen are aligned horizontally.
  * The choices are: 1 = left aligned, 2 = horizontally centered, 3 = right aligned
  */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how contents of the screen are aligned "
        + " horizontally. The choices are: 1 = left aligned, 3 = horizontally centered, "
        + " 2 = right aligned.")
  public @Options(HorizontalAlignment.class) int AlignHorizontal() {
    return horizontalAlignment.toUnderlyingValue();
  }

  /**
   * A number that encodes how contents of the screen are aligned horizontally. The choices are:
   * `1` (left aligned), `3` (horizontally centered), `2` (right aligned).
   *
   * @internaldoc
   * Sets the horizontal alignment for contents of the screen
   *
   * @param alignment the new alignment for the form contents
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignHorizontal(@Options(HorizontalAlignment.class) int alignment) {
    // Make sure the alignment is a valid HorizontalAlignment.
    HorizontalAlignment align = HorizontalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      this.dispatchErrorOccurredEvent(this, "HorizontalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
      return;
    }
    AlignHorizontalAbstract(align);
  }

  /**
   * Returns the current alignment of the screen.
   * @return the current alignment of the screen.
   */
  @SuppressWarnings({"RegularMethodName", "unused"})
  public HorizontalAlignment AlignHorizontalAbstract() {
    return horizontalAlignment;
  }

  /**
   * Sets the horizontal alignment for the contents of the screen.
   * @param alignment the alignment to set the contents of the screen to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AlignHorizontalAbstract(HorizontalAlignment alignment) {
    alignmentSetter.setHorizontalAlignment(alignment);
    horizontalAlignment = alignment;
  }

 /**
  * Returns a number that encodes how contents of the arrangement are aligned vertically.
  * The choices are: 1 = top, 2 = vertically centered, 3 = aligned at the bottom.
  * Vertical alignment has no effect if the screen is scrollable.
  */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how the contents of the arrangement are aligned "
        + "vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = "
        + "aligned at the bottom. Vertical alignment has no effect if the screen is scrollable.")
  public @Options(VerticalAlignment.class) int AlignVertical() {
    return AlignVerticalAbstract().toUnderlyingValue();
  }

  /**
   * A number that encodes how the contents of the arrangement are aligned vertically. The choices
   * are: `1` (aligned at the top), `2` (vertically centered), `3` (aligned at the bottom). Vertical
   * alignment has no effect if the screen is scrollable.
   *
   * @internaldoc
   * Sets the vertical alignment for contents of the screen
   *
   * @param alignment the new vertical alignment of the form contents
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignVertical(@Options(VerticalAlignment.class) int alignment) {
    // Make sure the alignment is a valid VerticalAlignment.
    VerticalAlignment align = VerticalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      this.dispatchErrorOccurredEvent(this, "VerticalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
      return;
    }
    AlignVerticalAbstract(align);
  }

  /**
   * Returns the vertical alignment of the screen's contents.
   * @return the vertical alignment of the screen's contents.
   */
  @SuppressWarnings("RegularMethodName")
  public VerticalAlignment AlignVerticalAbstract() {
    return verticalAlignment;
  }

  /**
   * Sets the vertical alignment of the screen's contents.
   * @param alignment the alignment to set the screen's contents to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AlignVerticalAbstract(VerticalAlignment alignment) {
    alignmentSetter.setVerticalAlignment(alignment);
    verticalAlignment = alignment;
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
  public @Options(ScreenAnimation.class) String OpenScreenAnimation() {
    if (openAnimType != null) {
      return openAnimType.toUnderlyingValue();
    }
    return null;
  }

  /**
   * Sets the animation type for the transition of this form opening.
   *
   * @param animType the type of animation to use for the transition
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION,
      defaultValue = "default")
  @SimpleProperty
  public void OpenScreenAnimation(@Options(ScreenAnimation.class) String animType) {
    // Make sure that "animType" is a valid ScreenAnimation.
    ScreenAnimation anim = ScreenAnimation.fromUnderlyingValue(animType);
    if (anim == null) {
      this.dispatchErrorOccurredEvent(this, "Screen",
          ErrorMessages.ERROR_SCREEN_INVALID_ANIMATION, animType);
      return;
    }
    OpenScreenAnimationAbstract(anim);
  }

  /**
   * Gets the current open screen animation.
   *
   * @return The current open screen animation.
   */
  @SuppressWarnings({"RegularMethodName", "unused"})
  public ScreenAnimation OpenScreenAnimationAbstract() {
    return openAnimType;
  }

  /**
   * Sets the animation type for the transition of this form opening.
   *
   * @param animType the type of animation to use for the transition
   */
  @SuppressWarnings("RegularMethodName")
  public void OpenScreenAnimationAbstract(ScreenAnimation animType) {
    openAnimType = animType;
  }

 /**
  * The animation for closing current screen and returning to the previous screen. Valid options
  * are `default`, `fade`, `zoom`, `slidehorizontal`, `slidevertical`, and `none`.
  *
  * @return open screen animation
  */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The animation for closing current screen and returning "
        + " to the previous screen. Valid options are default, fade, zoom, slidehorizontal, "
        + "slidevertical, and none")
  public @Options(ScreenAnimation.class) String CloseScreenAnimation() {
    if (closeAnimType != null) {
      return CloseScreenAnimationAbstract().toUnderlyingValue();
    }
    return null;
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
  public void CloseScreenAnimation(@Options(ScreenAnimation.class) String animType) {
    // Make sure that "animType" is a valid ScreenAnimation.
    ScreenAnimation anim = ScreenAnimation.fromUnderlyingValue(animType);
    if (anim == null) {
      this.dispatchErrorOccurredEvent(this, "Screen",
          ErrorMessages.ERROR_SCREEN_INVALID_ANIMATION, animType);
      return;
    }
    CloseScreenAnimationAbstract(anim);
  }

  /**
   * Gets the close animation of the screen, as a ScreenAnimation enum.
   *
   * @return The current close screen animation.
   */
  @SuppressWarnings("RegularMethodName")
  public ScreenAnimation CloseScreenAnimationAbstract() {
    return closeAnimType;
  }

  /**
   * Sets the animation type for the transition of this form closing and returning to a form behind
   * it on the activity stack.
   * 
   * @param animType the type of animation to use for the transition.
   */
  @SuppressWarnings("RegularMethodName")
  public void CloseScreenAnimationAbstract(ScreenAnimation animType) {
    closeAnimType = animType;
  }

  /**
   * The image used for your App's display icon should be a square png or jpeg image with dimensions
   * up to 1024x1024 pixels. Larger images may cause compiling or installing the app to fail.
   * The build server will generate images of standard dimensions for Android devices.
   *
   * @param name the name of the application icon
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(userVisible = false, category = PropertyCategory.GENERAL)
  public void Icon(String name) {
    // We don't actually need to do anything.
  }

  /**
   * An integer value which must be incremented each time a new Android Application Package File
   * (APK) is created for the Google Play Store.
   *
   * @param vCode the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
    defaultValue = "1")
  @SimpleProperty(userVisible = false,
    description = "An integer value which must be incremented each time a new Android "
    +  "Application Package File (APK) is created for the Google Play Store.",
    category = PropertyCategory.PUBLISHING)
  public void VersionCode(int vCode) {
    // We don't actually need to do anything.
  }

  /**
   * A string which can be changed to allow Google Play Store users to distinguish between
   * different versions of the App.
   *
   * @param vName the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "1.0")
  @SimpleProperty(userVisible = false,
    description = "A string which can be changed to allow Google Play "
    + "Store users to distinguish between different versions of the App.",
    category = PropertyCategory.PUBLISHING)
  public void VersionName(String vName) {
    // We don't actually need to do anything.
  }

  /**
   * If set to responsive (the default), screen layouts will use the actual resolution of the
   * device. See the [documentation on responsive design](../other/responsiveDesign.html) in App
   * Inventor for more information.
   * If set to fixed, screen layouts will be created for a single fixed-size screen and autoscaled.
   *
   *   **Note:** This property appears on Screen1 only and controls the sizing for all screens in
   * the app.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SIZING,
      defaultValue = "Responsive", alwaysSend = true)
  @SimpleProperty(userVisible = false,
      // This desc won't apprear as a tooltip, since there's no block, but we'll keep it with the source.
      description = "If set to fixed,  screen layouts will be created for a single fixed-size screen and autoscaled. " +
          "If set to responsive, screen layouts will use the actual resolution of the device.  " +
          "See the documentation on responsive design in App Inventor for more information. " +
          "This property appears on Screen1 only and controls the sizing for all screens in the app.",
      category = PropertyCategory.GENERAL)
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
   * ShowListsAsJson Property Setter
   * This only appears in the designer for screen 1
   * @param
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "True", alwaysSend = true)
  @SimpleProperty(category = PropertyCategory.GENERAL, userVisible = false,
  // This description won't appear as a tooltip, since there's no block, but we'll keep it with the source.
    description = "If false, lists will be converted to strings using Lisp "
      + "notation, i.e., as symbols separated by spaces, e.g., (a 1 b2 (c "
      + "d). If true, lists will appear as in Json or Python, e.g.  [\"a\", 1, "
      + "\"b\", 2, [\"c\", \"d\"]].  This property appears only in Screen 1, "
      + "and the value for Screen 1 determines the behavior for all "
      + "screens. The property defaults to \"true\" meaning that the App "
      + "Inventor programmer must explicitly set it to \"false\" if Lisp "
      + "syntax is desired. In older versions of App Inventor, this setting "
      + "defaulted to false. Older projects should not have been affected by "
      + "this default settings update."
    )
  public void ShowListsAsJson(boolean asJson) {
    showListsAsJson = asJson;
  }

  /**
   * If `true`{:.logic.block} (the default), lists will be shown as strings in JSON/Python notation
   * for example [1, "a", true]. If `false`{:.logic.block}, lists will be shown in the LISP
   * notation, for example (1 a true).
   *
   *   **Note:** This property appears only in Screen1 and the value for Screen1 determines the
   * behavior for all screens in the app.
   */
  @SimpleProperty(category = PropertyCategory.GENERAL, userVisible = false)
  public boolean ShowListsAsJson() {
    return showListsAsJson;
  }

  /**
   * This is the display name of the installed application in the phone. If the `AppName` is blank,
   * it will be set to the name of the project when the project is built.
   *
   * @param aName the display name of the installed application in the phone
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(userVisible = false,
      description = "This is the display name of the installed application in the phone." +
          "If the AppName is blank, it will be set to the name of the project when the project is built.",
      category = PropertyCategory.GENERAL)
  public void AppName(String aName) {
    // We don't actually need to do anything.
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_PRIMARY_COLOR)
  @SimpleProperty(userVisible = false, description = "This is the primary color used for " +
      "Material UI elements, such as the ActionBar.", category = PropertyCategory.THEMING)
  public void PrimaryColor(final int color) {
    setPrimaryColor(color);
  }

  /**
   * This is the primary color used as part of the Android theme, including coloring the `%type%`'s
   * title bar.
   */
  @SimpleProperty
  @IsColor
  public int PrimaryColor() {
    return primaryColor;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_PRIMARY_DARK_COLOR)
  @SimpleProperty(userVisible = false, description = "This is the primary color used for darker " +
      "elements in Material UI.", category = PropertyCategory.THEMING)
  public void PrimaryColorDark(int color) {
    primaryColorDark = color;
  }

  /**
   * This is the primary color used when the Theme property is specified to be Dark. It applies to
   * a number of elements, including the `%type%`'s title bar.
   */
  @SimpleProperty()
  @IsColor
  public int PrimaryColorDark() {
    return primaryColorDark;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = ComponentConstants.DEFAULT_ACCENT_COLOR)
  @SimpleProperty(userVisible = false, description = "This is the accent color used for " +
      "highlights and other user interface accents.", category = PropertyCategory.THEMING)
  public void AccentColor(int color) {
    accentColor = color;
  }

  /**
   * This is the accent color used for highlights and other user interface accents in newer
   * versions of Android. Components affected by this property include dialogs created by the
   * {@link Notifier}, the {@link DatePicker}, and others.
   */
  @SimpleProperty
  @IsColor
  public int AccentColor() {
    return accentColor;
  }

  /**
   * Selects the theme for the application. Theme can only be set at compile time and the Companion
   * will approximate changes during live development. Possible options are:
   *
   *   * `Classic`, which is the same as older versions of App Inventor;
   *   * `Device Default`, which gives the same theme as the version of Android running on the
   *     device and uses PrimaryColor for the Action Bar and has light buttons;
   *   * `Black Title Text`, which is the `Device Default` theme but with black title text; and
   *   * `Dark`, which is a dark version of the `Device Default` theme using `PrimaryColorDark` and
   *     having dark grey components.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_THEME,
      defaultValue = ComponentConstants.DEFAULT_THEME)
  @SimpleProperty(userVisible = false, description = "Pick a design theme for your app. Themes change the appearance of an app, " +
     "such as how buttons and text look. The most common themes are: </p> <ul> <li> <code>Classic</code>: " +
     "This theme stays consistent whether you are looking at an Android, iOS, or the screen layout " +
     "in App Inventor’s designer. Choose Classic if you want detailed control of the appearance " +
     "of your app. </li><li> <code>Device Default</code>: This theme makes your app resemble the other " +
     "apps on your device. With the default theme, however, your app won’t look consistent across " +
     "Android, iOS, and App Inventor’s designer. The best way to see the true appearance of your app is to view it using the Companion.</ul>",
      category = PropertyCategory.THEMING)
  public void Theme(String theme) {
    if (usesDefaultBackground) {
      if (theme.equalsIgnoreCase("AppTheme") && !isClassicMode()) {
        backgroundColor = Component.COLOR_BLACK;
      } else {
        backgroundColor = Component.COLOR_WHITE;
      }
      setBackground(frameLayout);
    }
    usesDarkTheme = false;
    if (theme.equals("Classic")) {
      setAppInventorTheme(Theme.CLASSIC);
    } else if (theme.equals("AppTheme.Light.DarkActionBar")) {
      setAppInventorTheme(Theme.DEVICE_DEFAULT);
    } else if (theme.equals("AppTheme.Light")) {
      setAppInventorTheme(Theme.BLACK_TITLE_TEXT);
    } else if (theme.equals("AppTheme")) {
      usesDarkTheme = true;
      setAppInventorTheme(Theme.DARK);
    }
  }

  /**
   * Returns the Screen width in pixels (x-size).
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
   * Returns the Screen height in pixels (y-size).
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
   * A URL which will be opened on the left side panel (which can be toggled once it is open). This
   * is intended for projects that have an in-line tutorial as part of the project. For security
   * reasons, only tutorials hosted on http://appinventor.mit.edu or linked to from our URL
   * shortener (http://appinv.us) may be used here. Other URLs will be silently ignored.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "")
  @SimpleProperty(userVisible = false,
    description = "A URL to use to populate the Tutorial Sidebar while "
    + "editing a project. Used as a teaching aid.",
    category = PropertyCategory.GENERAL)
  public void TutorialURL(String url) {
    // We don't actually do anything This property is stored in the
    // project properties file
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SUBSET_JSON,
    defaultValue = "")
  @SimpleProperty(userVisible = false,
    description = "Choose the set of components you’ll need for your project. A smaller set is " +
                   "good for beginner projects, while experts can use all options to build complex apps. For example, the " +
                   "Beginner Toolkit gives you access to all the features you need for our novice tutorials and curriculum.</p>" +
                   "<p>You can always change your toolkit in Project Properties, so your choice now won’t limit the future possibilities for your app.</p>",
    category = PropertyCategory.GENERAL)
  public void BlocksToolkit(String json) {
    // We don't actually do anything. This property is stored in the
    // project properties file
  }

  /**
   * Gets the name of the underlying platform running the app. Currently, this is the text
   * "Android". Other platforms may be supported in the future.
   *
   * @return The platform running the app
   */
  @SimpleProperty(description = "The platform the app is running on, for example \"Android\" or "
      + "\"iOS\".")
  public String Platform() {
    return "Android";
  }

  /**
   * Gets the version number of the platform running the app. This is typically a dotted version
   * number, such as 10.0. Any value can be returned, however, so you should take care to handle
   * unexpected data. If the platform version is unavailable, the empty text will be returned.
   *
   * @return The version of the platform running the app
   */
  @SimpleProperty(description = "The dotted version number of the platform, such as 4.2.2 or 10.0. "
      + "This is platform specific and there is no guarantee that it has a particular format.")
  public String PlatformVersion() {
    return Build.VERSION.RELEASE;
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
    allChildren.add(component);
  }

  @Override
  public List<? extends Component> getChildren(){
    return allChildren;
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

  private void showAboutApplicationNotification() {
    String title = "About this app";
    String MITtagline = "<p><small><em>Invented with MIT App Inventor<br>appinventor.mit.edu</em></small></p>";
    // Users can hide the taglines by including an HTML open comment <!-- in the about screen message
    String message = aboutScreen + MITtagline;
    message = message.replaceAll("\\n", "<br>"); // Allow for line breaks in the string.
    String buttonText ="Got it";
    Notifier.oneButtonAlert(this, message, title, buttonText);
  }

  // This is called from clear-current-form in runtime.scm.
  public void clear() {
    Log.d(LOG_TAG, "Form " + formName + " clear called");
    viewLayout.getLayoutManager().removeAllViews();
    if (frameLayout != null) {
      frameLayout.removeAllViews();
      frameLayout = null;
    }
    // Set all screen properties to default values.
    defaultPropertyValues();
    onStopListeners.clear();
    onNewIntentListeners.clear();
    onResumeListeners.clear();
    onOrientationChangeListeners.clear();
    onPauseListeners.clear();
    onDestroyListeners.clear();
    onInitializeListeners.clear();
    onCreateOptionsMenuListeners.clear();
    onOptionsItemSelectedListeners.clear();
    screenInitialized = false;
    // Notifiy those who care
    for (OnClearListener onClearListener : onClearListeners) {
      onClearListener.onClear();
    }
    // And reset the list
    onClearListeners.clear();
    System.err.println("Form.clear() About to do moby GC!");
    System.gc();
    dimChanges.clear();
  }

  public FrameLayout getFrameLayout() {
    return frameLayout;
  }

  public void deleteComponent(Object component) {
    if (component instanceof OnStopListener) {
      onStopListeners.remove(component);
    }
    if (component instanceof OnNewIntentListener) {
      onNewIntentListeners.remove(component);
    }
    if (component instanceof OnResumeListener) {
      onResumeListeners.remove(component);
    }
    if (component instanceof OnOrientationChangeListener) {
      onOrientationChangeListeners.remove(component);
    }
    if (component instanceof OnPauseListener) {
      onPauseListeners.remove(component);
    }
    if (component instanceof OnDestroyListener) {
      onDestroyListeners.remove(component);
    }
    if (component instanceof OnInitializeListener) {
      onInitializeListeners.remove(component);
    }
    if (component instanceof OnCreateOptionsMenuListener) {
      onCreateOptionsMenuListeners.remove(component);
    }
    if (component instanceof OnOptionsItemSelectedListener) {
      onOptionsItemSelectedListeners.remove(component);
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

  /**
   * Hide the soft keyboard
   */
  @SimpleFunction(description = "Hide the onscreen soft keyboard.")
  public void HideKeyboard() {
    View view = this.getCurrentFocus();
    if (view == null) {
      view = frameLayout;
    }
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  protected void updateTitle() {
    themeHelper.setTitle(title);
  }

  @Override
  protected void maybeShowTitleBar() {
    if (showTitle) {
      super.maybeShowTitleBar();
    } else {
      super.hideTitleBar();
    }
  }

  public boolean isDarkTheme() {
    return usesDarkTheme;
  }

  // Permission Handling Code

  /**
   * Test whether the permission is denied by the user.
   *
   * @param permission The name of the permission to test.
   * @return true if the permission has been denied, otherwise false.
   */
  public boolean isDeniedPermission(String permission) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED;
  }

  /**
   * Test whether the permission is denied by the user and throws a PermissionException if it has.
   *
   * @param permission The name of the permission to assert.
   * @throws PermissionException if the permission is denied
   */
  public void assertPermission(String permission) {
    if (isDeniedPermission(permission)) {
      throw new PermissionException(permission);
    }
  }

  /**
   * askPermission -- Request the user to allow what Google claims is
   *                  a "dangerous" permission.
   *
   * Newer versions of Android require explicit user consent for
   * selected permissions, even if they are declared in the Android
   * manifest. This routine permits components (and extensions) to
   * query the user for the required permission. The caller should
   * provide a "PermissionResultHandler" callback to handle the
   * returned result. The caller should *not* continue to perform the
   * operation that requires the new permission directly. Instead the
   * operation needs to be continued in the PermissionResultHandler if
   * the permission is granted (and should do something reasonable if
   * it is not).
   *
   * @param permission        -- The requested Android Permission as a strong
   * @param responseRequestor -- The PermissionResultHandler that
   *                             takes actions based on the user
   *                             provided answer.
   */
  public void askPermission(final String permission, final PermissionResultHandler responseRequestor) {
    final Form form = this;
    if (!isDeniedPermission(permission)) {
      // We already have permission, so no need to ask
      responseRequestor.HandlePermissionResponse(permission, true);
      return;
    }
    androidUIHandler.post(new Runnable() {
        @Override
        public void run() {
          int nonce = permissionRandom.nextInt(MAX_PERMISSION_NONCE);
          Log.d(LOG_TAG, "askPermission: permission = " + permission +
            " requestCode = " + nonce);
          permissionHandlers.put(nonce, responseRequestor);
          ActivityCompat.requestPermissions((Activity)form,
            new String[] {permission}, nonce);
        }
      });
  }

  /**
   * Evaluates the request for bulk permissions and asks the user for any ungranted permissions.
   *
   * @param request the request to evaluate
   */
  public void askPermission(final BulkPermissionRequest request) {
    final List<String> permissionsToAsk = request.getPermissions();
    Iterator<String> it = permissionsToAsk.iterator();
    while (it.hasNext()) {
      if (!isDeniedPermission(it.next())) {
        it.remove();
      }
    }
    if (permissionsToAsk.size() == 0) {
      // We already have all the necessary permissions
      request.onGranted();
    }  else {
      // Make sure we ask for permissions on the UI thread
      androidUIHandler.post(new Runnable() {
        @Override
        public void run() {
          final Iterator<String> it = permissionsToAsk.iterator();
          final PermissionResultHandler handler = new PermissionResultHandler() {
            final List<String> deniedPermissions = new ArrayList<String>();

            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
              if (!granted) {
                deniedPermissions.add(permission);
              }
              if (it.hasNext()) {
                askPermission(it.next(), this);
              } else {
                if (deniedPermissions.size() == 0) {
                  request.onGranted();
                } else {
                  request.onDenied(deniedPermissions.toArray(new String[] {}));
                }
              }
            }
          };
          askPermission(it.next(), handler);
        }
      });
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
    String permissions[], int[] grantResults) {
    PermissionResultHandler responder = permissionHandlers.get(requestCode);
    if (responder == null) {
      // Hmm. Shouldn't happen
      Log.e(LOG_TAG, "Received permission response which we cannot match.");
      return;
    }
    if (grantResults.length > 0) {
      if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        responder.HandlePermissionResponse(permissions[0], true);
      } else {
        responder.HandlePermissionResponse(permissions[0], false);
      }
    } else {
      Log.d(LOG_TAG, "onRequestPermissionsResult: grantResults.length = " + grantResults.length +
        " requestCode = " + requestCode);
    }
    permissionHandlers.remove(requestCode);
  }

  /**
   * Tests whether the app declares the given permission.
   *
   * @param permissionName The name of the permission to test.
   * @see android.Manifest.permission
   * @return True if the permission is declared in the manifest, otherwise false.
   */
  @SuppressWarnings("WeakerAccess")  // May be used by extensions
  public boolean doesAppDeclarePermission(String permissionName) {
    return permissions.contains(permissionName);
  }

  /**
   * Gets the path to an asset.
   *
   * @param asset The filename of an application asset
   * @return A file: URI to the asset
   */
  public String getAssetPath(String asset) {
    return ASSETS_PREFIX + asset;
  }

  public String getCachePath(String cache) {
    return "file://" + new java.io.File(getCacheDir(), cache).getAbsolutePath();
  }

  public String getDefaultPath(String name) {
    return FileUtil.resolveFileName(this, name, defaultFileScope);
  }

  /**
   * Gets the path to an app-private data file identified by {@code fileName}.
   *
   * @param fileName the file name
   * @return an absolute file: URI to the app-private file name
   * @see ReplForm#getPrivatePath(String)
   */
  public String getPrivatePath(String fileName) {
    return "file://" + new java.io.File(getFilesDir(), fileName).getAbsolutePath();
  }

  /**
   * Opens an application asset.
   *
   * @param asset The filename of an application asset
   * @return An open InputStream to the asset
   * @throws IOException if the asset cannot be opened, e.g., if it is not bundled in the app
   */
  @SuppressWarnings({"WeakerAccess"})  // May be called by extensions
  public InputStream openAsset(String asset) throws IOException {
    return openAssetInternal(getAssetPath(asset));
  }

  /**
   * Determines a WebView compatible, REPL-sensitive path for an asset provided by a given
   * extension.
   *
   * @param component The extension that is requesting an asset
   * @param asset The asset filename
   * @return A string containing the path to the asset
   * @throws FileNotFoundException if the asset cannot be located
   */
  public String getAssetPathForExtension(Component component, String asset) throws FileNotFoundException {
    String extPkgName = component.getClass().getPackage().getName();
    return ASSETS_PREFIX + extPkgName + "/" + asset;
  }

  /**
   * Opens an asset for reading as an InputStream. If the asset cannot be found, an IOException will
   * be raised.
   *
   * @param component The extension that is requesting an asset
   * @param asset The asset filename
   * @return A new input stream for the requested asset. The caller is responsible for closing the
   * stream to prevent resource leaking.
   * @throws IOException if the asset is not found or cannot be read
   */
  @SuppressWarnings("unused")  // May be called by extensions
  public InputStream openAssetForExtension(Component component, String asset) throws IOException {
    return openAssetInternal(getAssetPathForExtension(component, asset));
  }

  @SuppressWarnings("WeakerAccess")  // Visible for testing
  @VisibleForTesting
  InputStream openAssetInternal(String path) throws IOException {
    if (path.startsWith(ASSETS_PREFIX)) {
      final AssetManager am = getAssets();
      return am.open(path.substring(ASSETS_PREFIX.length()));
    } else if (path.startsWith("file:")) {
      return FileUtil.openFile(this, URI.create(path));
    } else {
      return FileUtil.openFile(this, path);
    }
  }
}
