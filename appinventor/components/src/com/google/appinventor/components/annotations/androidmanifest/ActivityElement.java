// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe an <activity> element required by a component so that it
 * can be added to AndroidManifest.xml. <activity> element attributes that are not
 * set explicitly default to "" or {} and are ignored when the element is created
 * in the manifest.
 *
 * Note: Some of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html">
 *              https://developer.android.com/guide/topics/manifest/activity-element.html
 *            </a>}.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActivityElement {

  /**
   * An array containing any intent filters used by this <activity> element.
   *
   * @return  an array containing the <intent-filter> subelements for this
   *          <activity> element
   */
  IntentFilterElement[] intentFilters() default {};

  /**
   * An array containing any meta data used by this <activity> element.
   *
   * @return  an array containing the <meta-data> subelements for this
   *          <activity> element
   */
  MetaDataElement[] metaDataElements() default {};

  /**
   * Specifies the fully qualified class name for the activity
   * launched by the App Inventor component. The name attribute is required
   * in any @ActivityElement annotation and hence has no default value.
   *
   * @return  the activity class name
   */
  String name();

  /**
   * Indicate that the activity can be launched as the embedded child of another
   * activity. Particularly in the case where the child lives in a container such
   * as a Display owned by another activity. For example, activities that are
   * used for Wear custom notifications must declare this so Wear can display
   * the activity in it's context stream, which resides in another process. The
   * default value of this attribute is false.
   *
   * @return  the activity allowEmbedded attribute
   */
  String allowEmbedded() default "";

  /**
   * Whether or not the activity can move from the task that started it to the
   * task it has an affinity for when that task is next brought to the front —
   * "true" if it can move, and "false" if it must remain with the task where
   * it started.
   *
   * If this attribute is not set, the value set by the corresponding
   * allowTaskReparenting attribute of the <application> element applies to the
   * activity. The default value is "false".
   *
   * Normally when an activity is started, it's associated with the task of the
   * activity that started it and it stays there for its entire lifetime. You
   * can use this attribute to force it to be re-parented to the task it has
   * an affinity for when its current task is no longer displayed. Typically,
   * it's used to cause the activities of an application to move to the main
   * task associated with that application.
   *
   * For example, if an e-mail message contains a link to a web page, clicking
   * the link brings up an activity that can display the page. That activity is
   * defined by the browser application, but is launched as part of the e-mail
   * task. If it's re-parented to the browser task, it will be shown when the
   * browser next comes to the front, and will be absent when the e-mail task
   * again comes forward.
   *
   * The affinity of an activity is defined by the {@link #taskAffinity()}
   * attribute. The affinity of a task is determined by reading the affinity
   * of its root activity. Therefore, by definition, a root activity is always
   * in a task with the same affinity. Since activities with "singleTask" or
   * "singleInstance" launch modes can only be at the root of a task,
   * re-parenting is limited to the "standard" and "singleTop" modes.
   * (See also the {@link #launchMode()} attribute.)
   *
   * @return  the activity allowTaskReparenting attribute
   */
  String allowTaskReparenting() default  "";

  /**
   * Whether or not the state of the task that the activity is in will always
   * be maintained by the system — "true" if it will be, and "false" if the
   * system is allowed to reset the task to its initial state in certain
   * situations. The default value is "false". This attribute is meaningful
   * only for the root activity of a task; it's ignored for all other activities.
   * Normally, the system clears a task (removes all activities from the stack
   * above the root activity) in certain situations when the user re-selects
   * that task from the home screen. Typically, this is done if the user hasn't
   * visited the task for a certain amount of time, such as 30 minutes.
   *
   * However, when this attribute is "true", users will always return to the
   * task in its last state, regardless of how they get there. This is useful,
   * for example, in an application like the web browser where there is a lot of
   * state (such as multiple open tabs) that users would not like to lose.
   *
   * @return the activity alwaysRetainTaskState attribute
   */
  String alwaysRetainTaskState() default "";

  /**
   * Whether or not tasks launched by activities with this attribute remains in
   * the overview screen until the last activity in the task is completed. If
   * true, the task is automatically removed from the overview screen. This
   * overrides the caller's use of FLAG_ACTIVITY_RETAIN_IN_RECENTS. It must be a
   * boolean value, either "true" or "false".
   *
   * @return  the activity autoRemoveFromRecents attribute
   */
  String autoRemoveFromRecents() default "";

  /**
   * A drawable resource providing an extended graphical banner for its
   * associated item. Use with the <activity> tag to supply a default banner
   * for a specific activity, or with the <application> tag to supply a banner
   * for all application activities.
   *
   * The system uses the banner to represent an app in the Android TV home
   * screen. Since the banner is displayed only in the home screen, it should
   * only be specified by applications with an activity that handles the
   * CATEGORY_LEANBACK_LAUNCHER intent.
   *
   * This attribute must be set as a reference to a drawable resource containing
   * the image (for example "@drawable/banner"). There is no default banner.
   *
   * @return  the activity banner attribute
   */
  String banner() default "";

  /**
   * Whether or not all activities will be removed from the task, except for the
   * root activity, whenever it is re-launched from the home screen — "true" if
   * the task is always stripped down to its root activity, and "false" if not.
   * The default value is "false". This attribute is meaningful only for activities
   * that start a new task (the root activity); it's ignored for all other activities
   * in the task.
   *
   * When the value is "true", every time users start the task again, they are
   * brought to its root activity regardless of what they were last doing in
   * the task and regardless of whether they used the Back or Home button to
   * leave it. When the value is "false", the task may be cleared of activities
   * in some situations (see the {@link #alwaysRetainTaskState()} attribute),
   * but not always.
   *
   * Suppose, for example, that someone launches activity P from the home screen,
   * and from there goes to activity Q. The user next presses Home, and then returns
   * to activity P. Normally, the user would see activity Q, since that is what they
   * were last doing in P's task. However, if P set this flag to "true", all of the
   * activities on top of it (Q in this case) were removed when the user pressed Home
   * and the task went to the background. So the user sees only P when returning
   * to the task.
   *
   * If this attribute and {@link #allowTaskReparenting()} are both "true", any
   * activities that can be re-parented are moved to the task they share an
   * affinity with; the remaining activities are then dropped, as described above.
   *
   * @return  the activity clearTaskOnLaunch attribute
   */
  String clearTaskOnLaunch() default "";

  /**
   * Lists configuration changes that the activity will handle itself. When a
   * configuration change occurs at runtime, the activity is shut down and
   * restarted by default, but declaring a configuration with this attribute
   * will prevent the activity from being restarted. Instead, the activity
   * remains running and its
   * {@link android.app.Activity#onConfigurationChanged(android.content.res.Configuration)}
   * method is called.
   *
   * Any or all of the following strings are valid values for this attribute.
   * Multiple values are separated by '|' — for example,
   * "locale|navigation|orientation".
   *
   * Accepted Strings:
   *
   * ["mcc", "mnc", "locale", "touchscreen", "keyboard", "keyboardHidden",
   * "navigation", "screenLayout", "fontScale", "uiMode", "orientation",
   * "screenSize", "smallestScreenSize"]
   *
   * For more information on these attribute values, see
   *
   * {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#config">
   *              https://developer.android.com/guide/topics/manifest/activity-element.html#config
   *            </a>}.
   *
   * @return  the activity configChanges attribute
   */
  String configChanges() default "";

  /**
   * Specifies how a new instance of an activity should be added to a task each
   * time it is launched. This attribute permits the user to have multiple
   * documents from the same application appear in the overview screen.
   *
   * This attribute has four values: ["intoExisting" | "always" | "none" | "never"].
   *
   * For more information on these attribute values, see
   *
   * {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#dlmode">
   *              https://developer.android.com/guide/topics/manifest/activity-element.html#dlmode
   *            </a>}.
   *
   * @return  the activity documentLaunchMode attribute
   */
  String documentLaunchMode() default "";

  /**
   * Whether or not the activity can be instantiated by the system — "true"
   * if it can be, and "false" if not. The default value is "true".
   *
   * The <application> element has its own enabled attribute that applies
   * to all application components, including activities. The <application>
   * and <activity> attributes must both be "true" (as they both are by
   * default) for the system to be able to instantiate the activity. If either
   * is "false", it cannot be instantiated.
   *
   * @return  the activity enabled attribute
   */
  String enabled() default "";

  /**
   * Whether or not the task initiated by this activity should be excluded from
   * the list of recently used applications, the overview screen. That is, when
   * this activity is the root activity of a new task, this attribute determines
   * whether the task should not appear in the list of recent apps. Set "true"
   * if the task should be excluded from the list; set "false" if it should be
   * included. The default value is "false".
   *
   * @return  the activity excludeFromRecents attribute
   */
  String excludeFromRecents() default "";

  /**
   * Whether or not the activity can be launched by components of other
   * applications — "true" if it can be, and "false" if not. If "false",
   * the activity can be launched only by components of the same application
   * or applications with the same user ID. For our purposes, those components
   * are other broadcast receivers and activities.
   *
   * The default value depends on whether the activity contains intent filters.
   * The absence of any filters means that the activity can be invoked only by
   * specifying its exact class name. This implies that the activity is intended
   * only for application-internal use (since others would not know the class
   * name). So in this case, the default value is "false". On the other hand,
   * the presence of at least one filter implies that the activity is intended
   * for external use, so the default value is "true".
   *
   * This attribute is not the only way to limit an activity's exposure to other
   * applications. You can also use a permission to limit the external entities that
   * can invoke the activity (see the {@link #permission()} attribute).
   *
   * @return  the activity exported attribute
   */
  String exported() default "";

  /**
   * Whether or not an existing instance of the activity should be shut down
   * (finished) whenever the user again launches its task (chooses the task on
   * the home screen) — "true" if it should be shut down, and "false" if not.
   * The default value is "false".
   *
   * If this attribute and {@link #allowTaskReparenting()} are both "true", this
   * attribute trumps the other. The affinity of the activity is ignored. The
   * activity is not re-parented, but destroyed.
   *
   * @return  the activity finishOnTaskLaunch attribute
   */
  String finishOnTaskLaunch() default "";

  /**
   * Whether or not hardware-accelerated rendering should be enabled for this
   * Activity — "true" if it should be enabled, and "false" if not. The default
   * value is "false".
   *
   * Starting from Android 3.0, a hardware-accelerated OpenGL renderer is
   * available to applications, to improve performance for many common 2D
   * graphics operations. When the hardware-accelerated renderer is enabled,
   * most operations in Canvas, Paint, Xfermode, ColorFilter, Shader, and Camera
   * are accelerated. This results in smoother animations, smoother scrolling,
   * and improved responsiveness overall, even for applications that do not
   * explicitly make use the framework's OpenGL libraries. Because of the
   * increased resources required to enable hardware acceleration, your app will
   * consume more RAM.
   *
   * Note that not all of the OpenGL 2D operations are accelerated. If you enable
   * the hardware-accelerated renderer, test your application to ensure that it
   * can make use of the renderer without errors.
   *
   * @return  the activity hardwareAccelerated attribute
   */
  String hardwareAccelerated() default "";

  /**
   * An icon representing the activity. The icon is displayed to users when a
   * representation of the activity is required on-screen. For example, icons
   * for activities that initiate tasks are displayed in the launcher window.
   * The icon is often accompanied by a label (see the {@link #label()}
   * attribute).
   *
   * This attribute must be set as a reference to a drawable resource containing
   * the image definition. If it is not set, the icon specified for the
   * application as a whole is used instead.
   *
   * The activity's icon — whether set here or by the <application> element — is
   * also the default icon for all the activity's intent filters (see the
   * {@link IntentFilterElement#icon()} attribute).
   *
   * @return  the activity icon attribute
   */
  String icon() default "";

  /**
   * A user-readable label for the activity. The label is displayed on-screen
   * when the activity must be represented to the user. It's often displayed
   * along with the activity icon.
   *
   * If this attribute is not set, the label set for the application as a whole
   * is used instead.
   *
   * The activity's label — whether set here or by the <application> element — is
   * also the default label for all the activity's intent filters (see the
   * {@link IntentFilterElement#label()} attribute).
   *
   * The label should be set as a reference to a string resource, so that it can
   * be localized like other strings in the user interface. However, as a
   * convenience while you're developing the application, it can also be set as a
   * raw string.
   *
   * @return  the activity label attribute
   */
  String label() default "";

  /**
   * An instruction on how the activity should be launched. There are four modes
   * that work in conjunction with activity flags (FLAG_ACTIVITY_* constants) in
   * {@link android.content.Intent} objects to determine what should happen when
   * the activity is called upon to handle an intent. They are:
   *
   * -> "standard"
   * -> "singleTop"
   * -> "singleTask"
   * -> "singleInstance"
   *
   * "standard" is the default mode and is appropriate for most types of activities.
   * "singleTop" is also a common and useful launch mode for many types of activities.
   * The other modes — "singleTask" and "singleInstance" — are not appropriate for
   * most applications, since they result in an interaction model that is likely
   * to be unfamiliar to users and is very different from most other applications.
   *
   * An activity with the "standard" or "singleTop" launch mode can be instantiated
   * multiple times. The instances can belong to any task and can be located
   * anywhere in the activity stack. Typically, they're launched into the task
   * that called {@link android.content.Context#startActivity(android.content.Intent)}
   * (unless the Intent object contains a {@link android.content.Intent#FLAG_ACTIVITY_NEW_TASK}
   * instruction, in which case a different task is chosen — see the
   * {@link #taskAffinity()} attribute).
   *
   * In contrast, "singleTask" and "singleInstance" activities can only begin a
   * task. They are always at the root of the activity stack. Moreover, the device
   * can hold only one instance of the activity at a time — only one such task.
   *
   * The "standard" and "singleTop" modes differ from each other in just one
   * respect: Every time there's a new intent for a "standard" activity, a new
   * instance of the class is created to respond to that intent. Each instance
   * handles a single intent. Similarly, a new instance of a "singleTop" activity
   * may also be created to handle a new intent. However, if the target task
   * already has an existing instance of the activity at the top of its stack,
   * that instance will receive the new intent (in an
   * {@link android.app.Activity#onNewIntent(android.content.Intent)} call); a new
   * instance is not created. In other circumstances — for example, if an existing
   * instance of the "singleTop" activity is in the target task, but not at the
   * top of the stack, or if it's at the top of a stack, but not in the target
   * task — a new instance would be created and pushed on the stack.
   *
   * Similarly, if you navigate up to an activity on the current stack, the
   * behavior is determined by the parent activity's launch mode. If the parent
   * activity has launch mode singleTop (or the up intent contains
   * {@link android.content.Intent#FLAG_ACTIVITY_CLEAR_TOP}), the parent is
   * brought to the top of the stack, and its state is preserved. The navigation
   * intent is received by the parent activity's
   * {@link android.app.Activity#onNewIntent(android.content.Intent)} method. If
   * the parent activity has launch mode standard (and the up intent does not
   * contain {@link android.content.Intent#FLAG_ACTIVITY_CLEAR_TOP}), the current
   * activity and its parent are both popped off the stack, and a new instance of
   * the parent activity is created to receive the navigation intent.
   *
   * The "singleTask" and "singleInstance" modes also differ from each other in
   * only one respect: A "singleTask" activity allows other activities to be
   * part of its task. It's always at the root of its task, but other activities
   * (necessarily "standard" and "singleTop" activities) can be launched into
   * that task. A "singleInstance" activity, on the other hand, permits no other
   * activities to be part of its task. It's the only activity in the task. If it
   * starts another activity, that activity is assigned to a different task — as
   * if {@link android.content.Intent#FLAG_ACTIVITY_NEW_TASK} was in the intent.
   *
   * @return  the activity launchMode attribute
   */
  String launchMode() default "";

  /**
   * The maximum number of tasks rooted at this activity in the overview screen.
   * When this number of entries is reached, the system removes the least-recently
   * used instance from the overview screen. Valid values are 1 through 50 (25
   * on low memory devices); zero is invalid. This must be an integer value,
   * such as 50. The default value is 16.
   *
   * @return  the activity maxRecents attribute
   */
  String maxRecents() default "";

  /**
   * Whether an instance of the activity can be launched into the process of the
   * component that started it — "true" if it can be, and "false" if not. The
   * default value is "false".
   *
   * Normally, a new instance of an activity is launched into the process of the
   * application that defined it, so all instances of the activity run in the same
   * process. However, if this flag is set to "true", instances of the activity
   * can run in multiple processes, allowing the system to create instances wherever
   * they are used (provided permissions allow it), something that is almost never
   * necessary or desirable.
   *
   * @return  the activity multiprocess attribute
   */
  String multiprocess() default "";

  /**
   * Whether or not the activity should be removed from the activity stack and
   * finished (its finish() method called) when the user navigates away from it
   * and it's no longer visible on screen — "true" if it should be finished, and
   * "false" if not. The default value is "false".
   *
   * A value of "true" means that the activity will not leave a historical trace.
   * It will not remain in the activity stack for the task, so the user will not
   * be able to return to it. In this case,
   * {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)}
   * is never called if you start another activity for a result from this activity.
   *
   * This attribute was introduced in API Level 3.
   *
   * @return  the activity noHistory attribute
   */
  String noHistory() default "";

  /**
   * The class name of the logical parent of the activity. The name here must
   * match the class name given to the corresponding {@link ActivityElement#name()}
   * attribute.
   *
   * The system reads this attribute to determine which activity should be
   * started when the user presses the Up button in the action bar. The system
   * can also use this information to synthesize a back stack of activities
   * with TaskStackBuilder.
   *
   * To support API levels 4 - 16, you can also declare the parent activity with
   * a {@link MetaDataElement} that specifies a value for "android.support.PARENT_ACTIVITY".
   *
   * This attribute was introduced in API Level 16.
   *
   * @return  the activity parentActivityName attribute
   */
  String parentActivityName() default "";

  /**
   * The name of a permission that clients must have to launch the activity
   * or otherwise get it to respond to an intent. If a caller of
   * {@link android.content.Context#startActivity(android.content.Intent)}
   * or {@link android.app.Activity#startActivityForResult(android.content.Intent, int)}
   * has not been granted the specified permission, its intent will not be
   * delivered to the activity.
   *
   * If this attribute is not set, the permission set by the <application>
   * element's permission attribute applies to the activity. If neither
   * attribute is set, the activity is not protected by a permission.
   *
   * @return  the activity permission attribute
   */
  String permission() default "";

  /**
   * The name of the process in which the activity should run. Normally, all
   * components of an application run in a default process name created for
   * the application and you do not need to use this attribute. For our purposes,
   * these components are either activities or broadcast receivers. If necessary,
   * you can override the default process name with this attribute, allowing you
   * to spread your app components across multiple processes.
   *
   * If the name assigned to this attribute begins with a colon (':'), a new
   * process, private to the application, is created when it's needed and the
   * activity runs in that process. If the process name begins with a lowercase
   * character, the activity will run in a global process of that name, provided
   * that it has permission to do so. This allows components in different
   * applications to share a process, reducing resource usage.
   *
   * @return  the activity process attribute
   */
  String process() default "";

  /**
   * Whether or not the activity relinquishes its task identifiers to an activity
   * above it in the task stack. A task whose root activity has this attribute
   * set to "true" replaces the base Intent with that of the next activity in the
   * task. If the next activity also has this attribute set to "true" then it
   * will yield the base Intent to any activity that it launches in the same task.
   * This continues for each activity until an activity is encountered which has
   * this attribute set to "false". The default value is "false".
   *
   * This attribute set to "true" also permits the activity's use of the
   * {@link android.app.ActivityManager.TaskDescription} to change labels,
   * colors and icons in the overview screen.
   *
   * @return  the activity relinquishTaskIdentity attribute
   */
  String relinquishTaskIdentity() default "";

  /**
   * Specifies whether the app supports multi-window display. You can set this
   * attribute in either the <activity> or <application> element.
   *
   * If you set this attribute to true, the user can launch the activity in
   * split-screen and freeform modes. If you set the attribute to false, the
   * activity does not support multi-window mode. If this value is false, and
   * the user attempts to launch the activity in multi-window mode, the activity
   * takes over the full screen.
   *
   * If your app targets API level 24 or higher, but you do not specify a value
   * for this attribute, the attribute's value defaults to true.
   *
   * This attribute was added in API level 24.
   *
   * @return  the activity resizableActivity attribute
   */
  String resizableActivity() default "";

  /**
   * The orientation of the activity's display on the device. The system ignores
   * this attribute if the activity is running in multi-window mode.
   *
   * The value can be any one of the following strings:
   *
   * ["unspecified" | "behind" | "landscape" | "portrait" | "reverseLandscape"
   * | "reversePortrait" | "sensorLandscape" | "sensorPortrait" | "userLandscape"
   * | "userPortrait" | "sensor" | "fullSensor" | "nosensor" | "user" | "fullUser" | "locked"]
   *
   * For more information on these attribute values, see
   *
   * {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#screen">
   *              https://developer.android.com/guide/topics/manifest/activity-element.html#screen
   *            </a>}.
   *
   * @return  the activity screenOrientation attribute
   */
  String screenOrientation() default "";

  /**
   * Whether or not the activity can be killed and successfully restarted
   * without having saved its state — "true" if it can be restarted without
   * reference to its previous state, and "false" if its previous state is
   * required. The default value is "false".
   *
   * Normally, before an activity is temporarily shut down to save resources,
   * its {@link android.app.Activity#onSaveInstanceState(android.os.Bundle)}
   * method is called. This method stores the current state of the activity in
   * a Bundle object, which is then passed to
   * {@link android.app.Activity#onCreate(android.os.Bundle)} when the activity is
   * restarted. If this attribute is set to "true", onSaveInstanceState() may not
   * be called and onCreate() will be passed null instead of the Bundle — just as
   * it was when the activity started for the first time.
   *
   * A "true" setting ensures that the activity can be restarted in the absence
   * of retained state. For example, the activity that displays the home screen
   * uses this setting to make sure that it does not get removed if it crashes
   * for some reason.
   *
   * @return  the activity stateNotNeeded attribute
   */
  String stateNotNeeded() default "";

  /**
   * Specifies whether the activity supports Picture-in-Picture display. The system
   * ignores this attribute if {@link #resizableActivity()} is false.
   *
   * This attribute was added in API level 24.
   *
   * @return  the activity supportPictureInPicture attribute
   */
  String supportPictureInPicture() default "";

  /**
   * The task that the activity has an affinity for. Activities with the same
   * affinity conceptually belong to the same task (to the same "application"
   * from the user's perspective). The affinity of a task is determined by the
   * affinity of its root activity.
   *
   * The affinity determines two things — the task that the activity is
   * re-parented to (see the {@link #allowTaskReparenting()} attribute) and the
   * task that will house the activity when it is launched with the
   * {@link android.content.Intent#FLAG_ACTIVITY_NEW_TASK} flag.
   *
   * By default, all activities in an application have the same affinity. You can
   * set this attribute to group them differently, and even place activities defined
   * in different applications within the same task. To specify that the activity
   * does not have an affinity for any task, set it to an empty string.
   *
   * If this attribute is not set, the activity inherits the affinity set for the
   * application. The name of the default affinity for an application is the package name set by
   * the <manifest> element.
   *
   * For details see {@link com.google.appinventor.buildserver.Compiler#writeAndroidManifest(java.io.File,
   *                                                                                         java.util.Set)}.
   *
   * @return  the activity taskAffinity attribute
   */
  String taskAffinity() default "";

  /**
   * A reference to a style resource defining an overall theme for the activity.
   * This automatically sets the activity's context to use this theme
   * (see {@link android.content.Context#setTheme(int)}, and may also cause
   * "starting" animations prior to the activity being launched (to better match
   * what the activity actually looks like).
   *
   * If this attribute is not set, the activity inherits the theme set for the
   * application as a whole — from the <application> element's theme attribute.
   * If that attribute is also not set, the default system theme is used.
   *
   * @return  the activity theme attribute
   */
  String theme() default "";

  /**
   * Extra options for an activity's UI.
   *
   * Must be one of the following values: ["none" | "splitActionBarWhenNarrow"]
   *
   * For more information on these attribute values, see
   *
   * {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#dlmode">
   *              https://developer.android.com/guide/topics/manifest/activity-element.html#dlmode
   *            </a>}.
   *
   * @return  the activity uiOptions attribute
   */
  String uiOptions() default "";

  /**
   * How the main window of the activity interacts with the window containing
   * the on-screen soft keyboard. The setting for this attribute affects two
   * things:
   *
   * -> The state of the soft keyboard — whether it is hidden or visible — when
   *    the activity becomes the focus of user attention.
   *
   * -> The adjustment made to the activity's main window — whether it is resized
   *    smaller to make room for the soft keyboard or whether its contents pan to
   *    make the current focus visible when part of the window is covered by the
   *    soft keyboard.
   *
   * The setting must be one of the values listed below, or a combination of one
   * "state..." value plus one "adjust..." value. Setting multiple values in
   * either group — multiple "state..." values, for example — has undefined
   * results. Individual values are separated by a vertical bar (|).
   *
   * For example: "stateVisible|adjustResize"
   *
   * The value can be any one of the following strings:
   *
   * ["stateUnspecified", "stateUnchanged", "stateHidden", "stateAlwaysHidden",
   * "stateVisible", "stateAlwaysVisible", "adjustUnspecified", "adjustResize",
   * "adjustPan"]
   *
   * For more information on these attribute values, see
   *
   * {@link <a href="https://developer.android.com/guide/topics/manifest/activity-element.html#wsoft">
   *              https://developer.android.com/guide/topics/manifest/activity-element.html#wsoft
   *            </a>}.
   *
   * @return  the activity windowSoftInputMode attribute
   */
  String windowSoftInputMode() default "";

}