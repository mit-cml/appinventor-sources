// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslations;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

/**
 * I18n strings for {@link Ode}.
 *
 */
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface OdeMessages extends Messages, ComponentTranslations {
  // Used in multiple files

  @DefaultMessage("Default")
  @Description("Text for property editors")
  String defaultText();

  @DefaultMessage("Cancel")
  @Description("Text on \"Cancel\" button.")
  String cancelButton();

  @DefaultMessage("OK")
  @Description("Text on \"OK\" button.")
  String okButton();

  @DefaultMessage("Reset")
  @Description("Text on the \"Reset\" button.")
  String resetButton();

  @DefaultMessage("Dismiss")
  @Description("Text on \"Dismiss\" button.")
  String dismissButton();

  @DefaultMessage("Old name:")
  @Description("Label next to the old name in a rename dialog")
  String oldNameLabel();

  @DefaultMessage("New name:")
  @Description("Label next to the new name in a rename dialog")
  String newNameLabel();

  @DefaultMessage("None")
  @Description("Caption for None entry")
  String noneCaption();

  @DefaultMessage("Delete")
  @Description("Text on \"Delete\" button")
  String deleteButton();

  @DefaultMessage("Move To Trash")
  @Description("Text on \"Delete Project\" button")
  String deleteProjectButton();

  @DefaultMessage("Overwrite")
  @Description("Text on \"Overwrite\" button")
  String overwriteButton();

  @DefaultMessage("View Trash")
  @Description("Text on \"Trash\" button")
  String viewTrashButton();

  @DefaultMessage("Trash")
  @Description("Abbreviated alternative trash button text")
  String trashButton();

  @DefaultMessage("My Projects")
  @Description("Text on \"Projects\" button")
  String myProjectsButton();

  @DefaultMessage("Restore")
  @Description("Text on \"Restore\" button")
  String restoreProjectButton();

  @DefaultMessage("Delete Forever")
  @Description("Text on \"Delete From Trash\" button")
  String deleteFromTrashButton();

  @DefaultMessage("Login to Gallery")
  @Description("Text for the \"Login to New Gallery\" Button")
  String loginToGallery();

  @DefaultMessage("Publish to Gallery")
  @Description("Text on \"Publish to Gallery\" button")
  String publishToGalleryButton();

  @DefaultMessage("Update Gallery App")
  @Description("Text on \"Update Gallery App\" button")
  String updateGalleryAppButton();

  @DefaultMessage("Error sending project to the Gallery")
  @Description("Error given when sending fails")
  String GallerySendingError();

  @DefaultMessage("This project contains extensions and cannot be published to gallery.")
  @Description("Error Message for displaying error when user tries to publish the project" + 
    "containing extensions")
  String ProjectContainsExtensions();

  @DefaultMessage("Error Logging Into the Gallery")
  @Description("Error given if login fails for some reason")
  String GalleryLoginError();

  @DefaultMessage("Projects with extensions cannot be published to the gallery.")
  @Description("Error given if project contains extensions")
  String HasExtensionError();

  @DefaultMessage("Show Warnings")
  @Description("Text on Toggle Warning Button")
  String showWarnings();

  @DefaultMessage("Hide Warnings")
  @Description("Text on Toggle Warning Button")
  String hideWarnings();

  @DefaultMessage("Upload File ...")
  @Description("Text on \"Add...\" button")
  String addButton();

  @DefaultMessage("Name")
  @Description("Header for name column of project table")
  String projectNameHeader();

  @DefaultMessage("Date Created")
  @Description("Header for date created column of project table.")
  String projectDateCreatedHeader();

  @DefaultMessage("Date Modified")
  @Description("Header for date modified column of project table.")
  String projectDateModifiedHeader();

  @DefaultMessage("Published")
  @Description("Header for published column of project table.")
  String projectPublishedHeader();

  @DefaultMessage("Save")
  @Description("Label of the button for save")
  String saveButton();

  @DefaultMessage("Export to File ...")
  @Description("Label of the button for save as")
  String saveAsButton();

  @DefaultMessage("Checkpoint ...")
  @Description("Label of the button for checkpoint")
  String checkpointButton();

  @DefaultMessage("Create checkpoint before deleting")
  @Description("Label of the CheckBox for checkpoint")
  String checkBoxButton();

  @DefaultMessage("Toggle Tutorial")
  @Description("Label for the Toggle Tutorial Button")
  String toggleTutorialButton();

  @DefaultMessage("Add Screen ...")
  @Description("Label of the button for adding a new screen")
  String addFormButton();

  @DefaultMessage("Remove Screen")
  @Description("Label of the button for removing a screen")
  String removeFormButton();

  @DefaultMessage("Toggle Console")
  @Description("Label of the button for toggling the console panel")
  String toggleConsoleButton();

  @DefaultMessage("Connect")
  @Description("Label of the button for selecting phone connection")
  String connectButton();

  @DefaultMessage("Deleting this screen will completely remove the screen from your project. " +
      "All components and blocks associated with this screen will be deleted.\n" +
      "There is no undo.\nAre you sure you want to delete {0}?")
  @Description("Confirmation query for removing a screen")
  String reallyDeleteForm(String formName);

  @DefaultMessage("Warning: You are deleting a screen.\nThis operation cannot be undone.\n" +
	  "Please type the name of the screen {0} into the box below to confirm you really want to do this.")
  @Description("Confirmation query for removing a screen")
  String reallyDeleteWarning(String formName);

  @DefaultMessage("Open the Blocks Editor")
  @Description("Label of the button for opening the blocks editor")
  String openBlocksEditorButton();

  @DefaultMessage("Screens ...")
  @Description("Label of the button for switching screens")
  String screensButton();

  @DefaultMessage("Blocks")
  @Description("Label of the button for switching to the blocks editor")
  String switchToBlocksEditorButton();

  @DefaultMessage("Designer")
  @Description("Label of the button for switching to the form editor")
  String switchToFormEditorButton();

  @DefaultMessage("Packaging ...")
  @Description("Label of the button leading to build related cascade items, when building")
  String isBuildingButton();

  @DefaultMessage("Opening the Blocks Editor... (click to cancel)")
  @Description("Label of the button for canceling the blocks editor launch")
  String cancelBlocksEditorButton();

  @DefaultMessage("Blocks Editor is open")
  @Description("Label of the button for opening the blocks editor when the it is already open")
  String blocksEditorIsOpenButton();

  // Switch Language Buttons (Internationalization)
  @DefaultMessage("Language")
  @Description("Label of the button for switching language")
  String switchLanguageButton();

  @DefaultMessage("Delete component")
  @Description("Text on \"Delete component\" button")
  String deleteComponentButton();

  @DefaultMessage("Publish")
  @Description("Text on \"Publish\" button")
  String publishButton();

  //Used in ModerationPage.java
  @DefaultMessage("...")
  @Description("Label of ... ")
  String moderationDotDotDot();

  @DefaultMessage("Report")
  @Description("Label of the report text field on moderation page")
  String moderationReportTextHeader();

  @DefaultMessage("App")
  @Description("Label of the app field on moderation page")
  String moderationAppHeader();

  @DefaultMessage("Reported On")
  @Description("Label of the created date of report field on moderation page")
  String moderationReportDateCreatedHeader();

  @DefaultMessage("App Author")
  @Description("Label of the app author field on moderation page")
  String moderationAppAuthorHeader();

  @DefaultMessage("Reporter")
  @Description("Label of the reporter field on moderation page")
  String moderationReporterHeader();

  // Used in ReportList.java
  @DefaultMessage("Inappropriate App Content: Remove")
  @Description("Label of the Inappropriate App Content Remove on reportlist")
  String inappropriateAppContentRemoveTitle();

  @DefaultMessage("Inappropriate App Content")
  @Description("Label of the Inappropriate App Content on reportlist")
  String inappropriateAppContentTitle();

  @DefaultMessage("Inappropriate User Profile Content")
  @Description("Label of the Inappropriate User Profile Content on reportlist")
  String inappropriateUserProfileContentTitle();

  @DefaultMessage("Choose Template")
  @Description("Label of the Choose Template Label on reportlist")
  String labelChooseTemplate();

  @DefaultMessage("Sent From: ")
  @Description("Label of the Sent From on reportlist")
  String emailSentFrom();

  @DefaultMessage("Sent To: ")
  @Description("Label of the Sent To on reportlist")
  String emailSentTo();

  @DefaultMessage("Send Email")
  @Description("Label of the Send Email of reportlist")
  String buttonSendEmail();

  @DefaultMessage("Deactivate App")
  @Description("Label of the Deactivate App on reportlist")
  String labelDeactivateApp();

  @DefaultMessage("Reactivate App")
  @Description("Label of the Reactivate App on reportlist")
  String labelReactivateApp();

  @DefaultMessage("Deactivate App & Send Email")
  @Description("Label of the Deactivate App & Send Email on reportlist")
  String labelDeactivateAppAndSendEmail();

  @DefaultMessage("Mark As Resolved")
  @Description("Label of the Mark As Resolved of reportlist")
  String labelmarkAsResolved();

  @DefaultMessage("Mark As Unresolved")
  @Description("Label of the Mark As Unresolved of reportlist")
  String labelmarkAsUnresolved();

  @DefaultMessage("Previous Actions")
  @Description("Label of the See All Actions of reportlist")
  String labelSeeAllActions();

  @DefaultMessage("Previous Actions on Report")
  @Description("Title of the Previous Actions Popup of reportlist")
  String titleSeeAllActionsPopup();

  @DefaultMessage("More Reports")
  @Description("text for more reports")
  String galleryMoreReports();

  @DefaultMessage("X")
  @Description("Symbol X")
  String symbolX();

  @DefaultMessage("Cancel")
  @Description("Label of the Cancel Action on reportlist")
  String labelCancel();

  @DefaultMessage("Confirm")
  @Description("Label of the Confirm Action on reportlist")
  String labelConfirm();

  @DefaultMessage("Your app \"{0}\" has been removed from the gallery due to inappropriate content. "
          + "Please review the guidelines at http://ai2.appinventor.mit.edu/about/termsofservice.html. "
          + "If you feel this action has been taken in error, " +
          "you may reply directly to this e-mail for discussion. \n")
  @Description("Label of the Text of Template 1 of reportlist")
  String inappropriateAppContentRemoveEmail(String title);

  @DefaultMessage("Your app \"{0}\" has inappropriate content. "
          + "Please review the guidelines at ..."
          + "and modify your app accordingly. ")
  @Description("Label of the Text of Template 2 of reportlist")
  String inappropriateAppContentEmail(String title);

  @DefaultMessage("Your profile contains inappropriate content. Please modify your profile.\n")
  @Description("Label of the Text of Template 3 of reportlist")
  String inappropriateUserProfileContentEmail();

  @DefaultMessage("see more ...")
  @Description("Label of the Text of seeing more of reportlist")
  String seeMoreLink();

  @DefaultMessage("hide")
  @Description("Label of the Text of hiding of reportlist")
  String hideLink();

  @DefaultMessage("sends an email: ")
  @Description("Label of the Text of sending an email of reportlist")
  String moderationActionSendAnEmail();

  @DefaultMessage("deativates this app with email: ")
  @Description("Label of the Text of deativating this app with email of reportlist")
  String moderationActionDeactivateThisAppWithEmail();

  @DefaultMessage("reactivates this app")
  @Description("Label of the Text of reactivating this app of reportlist")
  String moderationActionReactivateThisApp();

  @DefaultMessage("marks this report as resolved")
  @Description("Label of the Text of marking this report as resolved of reportlist")
  String moderationActionMarkThisReportAsResolved();

  @DefaultMessage("marks this report as unresolved")
  @Description("Label of the Text of marking this report as unresolved of reportlist")
  String moderationActionMarkThisReportAsUnresolved();

  @DefaultMessage("Show resolved reports")
  @Description("Label of the Text of showing resolved reports of reportlist")
  String moderationShowResolvedReports();

  @DefaultMessage("An Email from App Inventor Gallery")
  @Description("Title of the email when moderator sends out an email")
  String moderationSendEmailTitle();

  @DefaultMessage("App Inventor Gallery: App Activated")
  @Description("Title of the email when app was activated")
  String moderationAppReactivatedTitle();

  @DefaultMessage("Your app \"{0}\" has been reactivated.")
  @Description("Body of the email when app was activated")
  String moderationAppReactivateBody(String title);

  @DefaultMessage("App Inventor Gallery: App Deactivated")
  @Description("Title of the email when app was deactivated")
  String moderationAppDeactivatedTitle();

  @DefaultMessage("Fail to send out the email, please try again later")
  @Description("error message when fail to send to user from moderator")
  String moderationErrorFailToSendEmail();

  // Used in Ode.java

  @DefaultMessage("MIT App Inventor")
  @Description("Title for App Inventor")
  String titleYoungAndroid();

  @DefaultMessage("An internal error has occurred. Report a bug?")
  @Description("Confirmation for reporting a bug after an internal error")
  String internalErrorReportBug();

  @DefaultMessage("An internal error has occurred.")
  @Description("Alert after an internal error")
  String internalError();

  @DefaultMessage("An internal error has occurred. Go look in the Debugging view.")
  @Description("Alert after an internal error")
  String internalErrorSeeDebuggingView();

  @DefaultMessage("An internal error has occurred. Click \"ok\" for more information.")
  @Description("Confirm alert after an internal error")
  String internalErrorClickOkDebuggingView();

  @DefaultMessage("The server is temporarily unavailable. Please try again later!")
  @Description("Error message if the server becomes completely unavailable.")
  String serverUnavailable();

  @DefaultMessage("No Project Chosen")
  @Description("Title for Error Dialog when connection is attempted without a project.")
  String noprojectDialogTitle();

  @DefaultMessage("You must first create or select a project before connecting!")
  @Description("Error message for connection attempt without a project selected.")
  String noprojectDuringConnect();

  // Used in RpcStatusPopup.java

  @DefaultMessage("Loading ...")
  @Description("Message that is shown to indicate that a loading RPC is going on")
  String defaultRpcMessage();

  @DefaultMessage("Saving ...")
  @Description("Message that is shown to indicate that a saving RPC is going on")
  String savingRpcMessage();

  @DefaultMessage("Copying ...")
  @Description("Message that is shown to indicate that a copying RPC is going on")
  String copyingRpcMessage();

  @DefaultMessage("Deleting ...")
  @Description("Message that is shown to indicate that a deleting RPC is going on")
  String deletingRpcMessage();

  @DefaultMessage("Packaging ...")
  @Description("Message shown during a building RPC (for Young Android, called 'packaging')")
  String packagingRpcMessage();

  @DefaultMessage("Downloading to phone ...")
  @Description("Message shown while downloading application to the phone (during compilation)")
  String downloadingRpcMessage();

  // Used in StatusPanel.java

  @DefaultMessage("Built: {0}  Version: {1}")
  @Description("Label showing the ant build date and the git version")
  String gitBuildId(String date, String version);

  @DefaultMessage("Privacy")
  @Description("Label of the link for Privacy")
  String privacyLink();

  @DefaultMessage("Terms")
  @Description("Label of the link for Terms")
  String termsLink();

  @DefaultMessage("Privacy Policy and Terms of Use")
  @Description("Label of the link for Privacy and Terms of Use")
  String privacyTermsLink();

  // Used in TopPanel.java

  //Project
  @DefaultMessage("Projects")
  @Description("Name of Projects tab")
  String projectsTabName();

  @DefaultMessage("My projects")
  @Description("Name of My projects menuitem")
  String projectMenuItem();

  @DefaultMessage("Start new project")
  @Description("Label of the menu item for creating a new project")
  String newProjectMenuItem();

  @DefaultMessage("New project")
  @Description("Label of the menu item for creating a new project")
  String newProjectButton();

  @DefaultMessage("Import project (.aia) from my computer ...")
  @Description("Name of Import Project menuitem")
  String importProjectMenuItem();

  @DefaultMessage("New Folder")
  @Description("Label of the menu item for creating a new project folder")
  String newProjectFolderMenuItem();

  @DefaultMessage("Move Project…")
  @Description("Label of the menu item for moving a project to a different folder")
  String moveProjectMenuItem();

  @DefaultMessage("Move…")
  @Description("Label of the toolbar button moving a project to a different folder")
  String moveProjectButton();

  @DefaultMessage("Folder Name")
  @Description("Name of new folder in new folder wizard.")
  String newFolderWizardName();

  @DefaultMessage("Create folder in")
  @Description("Label of folder selector for new folder wizard.")
  String newFolderWizardParent();

  @DefaultMessage("Move to folder")
  @Description("Label of folder selector for move project wizard.")
  String moveProjectWizard();

  @DefaultMessage("Move To Trash")
  @Description("Name of Move To Trash menuitem")
  String trashProjectMenuItem();

  @DefaultMessage("Save project")
  @Description("Name of Save menuitem")
  String saveMenuItem();

  @DefaultMessage("Save project as ...")
  @Description("Name of Save as ... menuitem")
  String saveAsMenuItem();

  @DefaultMessage("Checkpoint")
  @Description("Name of Checkpoint menuitem")
  String checkpointMenuItem();

  @DefaultMessage("Import project (.aia) from a repository ...")
  @Description("Name of Import Template menuitem")
  String importTemplateButton();

  @DefaultMessage("Export selected project (.aia) to my computer")
  @Description("Name of Export Project menuitem")
  String exportProjectMenuItem();

  @DefaultMessage("Export")
  @Description("Export button title")
  String exportButton();

  @DefaultMessage("Export {0} selected projects")
  @Description("Name of Export Selected Projects menuitem")
  String exportSelectedProjectsMenuItem(int numSelectedProjects);

  @DefaultMessage("Export all projects")
  @Description("Name of Export all Project menuitem")
  String exportAllProjectsMenuItem();

  @DefaultMessage("Export keystore")
  @Description("Label of the button for export keystore")
  String downloadKeystoreMenuItem();

  @DefaultMessage("Import keystore")
  @Description("Label of the button for import keystore")
  String uploadKeystoreMenuItem();

  @DefaultMessage("Delete keystore")
  @Description("Label of the button for delete keystore")
  String deleteKeystoreMenuItem();

  //Component
  @DefaultMessage("Import extension")
  @Description("String shown in the palette to import an extension")
  String importExtensionMenuItem();

  @DefaultMessage("Upload Component...")
  @Description("Caption for component upload wizard.")
  String componentUploadWizardCaption();

  @DefaultMessage("Import an extension into project")
  @Description("Caption for component import wizard.")
  String componentImportWizardCaption();

  @DefaultMessage("Rename extension")
  @Description("Caption for component rename wizard.")
  String componentRenameWizardCaption();

  @DefaultMessage("Extension name")
  @Description("Caption for component Name Label in rename wizard.")
  String componentNameLabel();

  @DefaultMessage("Project Properties...")
  @Description("Text On Project Properties Dialog Menuitem")
  String projectPropertiesMenuItem();

  @DefaultMessage("Project Properties")
  @Description("Text On Project Properties Button")
  String projectPropertiesText();

  @DefaultMessage("Import Extension Failed!")
  @Description("Error message reported when the component import failed")
  String componentImportError();

  @DefaultMessage("Extension Import failed due to unknown URL")
  @Description("Error message reported when the component import failed due to unknown url")
  String componentImportUnknownURLError();

  @DefaultMessage("Extension Upgraded : ")
  @Description("Alert message reported when the component import upgraded an already imported extension")
  String componentUpgradedAlert();

  @DefaultMessage("The selected file is not a component file!\n" +
      "Component files are aix files.")
  @Description("Error message reported when the file selected for upload is not a component archive.")
  String notComponentArchiveError();

  @DefaultMessage("Unable to find component \"{0}\" while loading project \"{1}\".")
  @Description("Error message shown when a project references an unknown component.")
  String noComponentFound(String componentName, String projectName);

  @DefaultMessage("Please enter a url")
  @Description("Error message reported when no url is entered.")
  String noUrlError();

  @DefaultMessage("From my computer")
  @Description("")
  String componentImportFromComputer();

  @DefaultMessage("URL")
  @Description("For importing from a URL")
  String componentImportFromURL();

  @DefaultMessage("The component database in the project \"{0}\" is corrupt.")
  @Description("Error message when the component database is not valid.")
  String componentDatabaseCorrupt(String projectName);

  @DefaultMessage("The extension description of \"{0}\" in the project \"{1}\" is corrupt.")
  @Description("Error message when the component descriptors for an extension are not parsable.")
  String extensionDescriptorCorrupt(String extensionName, String projectName);

  @DefaultMessage("The project \"{0}\" contains an invalid extension. App Inventor will attempt to continue.")
  @Description("Error message when an extension descriptor pathname does not have the correct structure.")
  String invalidExtensionInProject(String projectName);

  //Connect
  @DefaultMessage("Connect")
  @Description("Label of the button leading to Connect related cascade items")
  String connectTabName();

  @DefaultMessage("AI Companion")
  @Description("Message providing details about starting the wireless connection.")
  String AICompanionMenuItem();

  @DefaultMessage("Chromebook")
  @Description("Menu item for initiating a connection to the companion running on a Chromebook.")
  String chromebookMenuItem();

  @DefaultMessage("Emulator")
  @Description("Message providing details about starting the emulator connection.")
  String emulatorMenuItem();

  @DefaultMessage("USB")
  @Description("Message providing details about starting a USB connection.")
  String usbMenuItem();

  @DefaultMessage("Reset Connection (Alt + Shift + R)")
  @Description("Reset all connections.")
  String resetConnectionsMenuItem();

  @DefaultMessage("Hard Reset")
  @Description("Hard Reset the Emulator.")
  String hardResetConnectionsMenuItem();

  @DefaultMessage("Refresh Companion Screen (Alt + R)")
  @Description("Refresh the companion screen.")
  String refreshCompanionMenuItem();

  //Build
  @DefaultMessage("Build")
  @Description("Label of the button leading to build related cascade items")
  String buildTabName();

  @DefaultMessage("Android App (.apk)")
  @Description("Label of item for building a project as apk and showing the qr+download dialog")
  String showExportAndroidApk();

  @DefaultMessage("[2] Android App (.apk)")
  @Description("Label of item for building a project as apk and showing the qr+download dialog")
  String showExportAndroidApk2();

  @DefaultMessage("Android App Bundle (.aab)")
  @Description("Label of item for building a project as aab and showing the qr+download dialog")
  String showExportAndroidAab();

  @DefaultMessage("[2] Android App Bundle (.aab)")
  @Description("Label of item for building a project as aab and showing the qr+download dialog")
  String showExportAndroidAab2();

  @DefaultMessage("Generate YAIL")
  @Description("Label of the cascade item for generating YAIL for a project")
  String generateYailMenuItem();

  //Help
  @DefaultMessage("Help")
  @Description("Label for the Help menu")
  String helpTabName();

  @DefaultMessage("About")
  @Description("Label of the link for About")
  String aboutMenuItem();

  @DefaultMessage("Use Companion: {0} or {1}")
  @Description("Label showing the companion version")
  String useCompanion(String version, String alternateVersion);

  @DefaultMessage("Target Android SDK: {0} ({1})")
  @Description("Label showing the target sdk version and the target android version")
  String targetSdkVersion(int sdk, String name);

  @DefaultMessage("Companion Information")
  @Description("Information about the Companion")
  String companionInformation();

  @DefaultMessage("Update the Companion")
  @Description("Menu item to update the Companion to the latest version")
  String companionUpdate();

  @DefaultMessage("You must have a project open to update the Companion")
  @Description("")
  String companionUpdateMustHaveProject();

  @DefaultMessage("Show Splash Screen")
  @Description("Redisplay the Splash Screen")
  String showSplashMenuItem();

  @DefaultMessage("Show Keyboard Shortcuts (Alt + ?)")
  @Description("Display the Shortcuts dialog")
  String showShortcuts();

  @DefaultMessage("Library")
  @Description("Name of Library link")
  String libraryMenuItem();

  @DefaultMessage("Get Started")
  @Description("Name of Getting Started link")
  String getStartedMenuItem();

  @DefaultMessage("Extensions")
  @Description("Extensions link")
  String extensionsMenuItem();

  @DefaultMessage("Tutorials")
  @Description("Name of Tutorials link")
  String tutorialsMenuItem();

  @DefaultMessage("Troubleshooting")
  @Description("Name of Troubleshooting link")
  String troubleshootingMenuItem();

  @DefaultMessage("Forums")
  @Description("Name of Forums link")
  String forumsMenuItem();

  @DefaultMessage("Report an Issue")
  @Description("Link for Report an Issue form")
  String feedbackMenuItem();

  // About Companion dialog

  @DefaultMessage("About The Companion")
  @Description("Title of the About Companion dialog")
  String aboutCompanionDialogTitle();

  @DefaultMessage("Download URL: {0}")
  @Description("Download URL for the Companion")
  String companionDownloadUrl(String url);

  @DefaultMessage("Companion Version {0}")
  @Description("Prefix appearing before the companion version number")
  String companionVersion(String version);

  // Settings

  @DefaultMessage("Settings")
  @Description("User Settings")
  String settingsTabName();

  @DefaultMessage("Enable Project Autoload")
  @Description("Menu item to enable automatic loading of projects when App Inventor is opened.")
  String enableAutoload();

  @DefaultMessage("Disable Project Autoload")
  @Description("Menu item to disable automatic loading of projects when App Inventor is opened.")
  String disableAutoload();

  @DefaultMessage("Enable OpenDyslexic")
  @Description("Switch to enable OpenDyslexic")
  String enableOpenDyslexic();

  @DefaultMessage("Disable OpenDyslexic")
  @Description("Switch to disable OpenDyslexic")
  String disableOpenDyslexic();

  @DefaultMessage("Select Interface Style")
  @Description("Select new or classic style")
  String selectStyle();

  @DefaultMessage("Neo")
  @Description("Switch to enable new layouts")
  String enableNewLayout();

  @DefaultMessage("Classic")
  @Description("Switch to disable new layouts")
  String disableNewLayout();

  @DefaultMessage("Select Theme")
  @Description("Select light or dark theme")
  String selectTheme();

  @DefaultMessage("Light")
  @Description("Switch to enable light theme")
  String lightMode();

  @DefaultMessage("Dark")
  @Description("Switch to enable dark theme")
  String darkMode();

  @DefaultMessage("User Interface Settings")
  @Description("Open wizard for user interface settings")
  String uiSettings();

  //Admin
  @DefaultMessage("Admin")
  @Description("Label of the button leading to admin functionality")
  String adminTabName();

  @DefaultMessage("Download User Source")
  @Description("Label of the button for admins to download a user's project source")
  String downloadUserSourceMenuItem();

  @DefaultMessage("Switch To Debug Panel")
  @Description("Label of the button for admins to switch to the debug panel without an explicit error")
  String switchToDebugMenuItem();

  @DefaultMessage("User Admin")
  @Description("Label of the button for admins to switch to the user admin interface")
  String userAdminMenuItem();

  @DefaultMessage("Advanced")
  @Description("Label of the button leading to admin functionality")
  String advancedMenuItem();


  //Tabs
  @DefaultMessage("My Projects")
  @Description("Name of My Projects tab")
  String myProjectsTabName();

  @DefaultMessage("View Trash")
  @Description("Name of View Trash tab")
  String viewTrashTabName();

  @DefaultMessage("Guide")
  @Description("Name of Guide link")
  String guideTabName();

  @DefaultMessage("Report an Issue")
  @Description("Link for Report an Issue form")
  String feedbackTabName();

  @DefaultMessage("Gallery")
  @Description("Link for Gallery")
  String galleryTabName();

  //User email dropdown
  @DefaultMessage("Sign out")
  @Description("Label of the link for signing out")
  String signOutLink();

  @DefaultMessage("Delete Account")
  @Description("Label of the link to delete your account")
  String deleteAccountLink();

  @DefaultMessage("project deletion failed")
  @Description("the deleteAccount function threw an error")
  String accountDeletionFailed();

  //

  @DefaultMessage("You must first delete all of your projects before you can delete your account")
  @Description("Dialog to show when account deletion is requested but the user has projects")
  String warnHasProjects();

  @DefaultMessage("Design")
  @Description("Name of Design tab")
  String tabNameDesign();

  @DefaultMessage("(Debugging)")
  @Description("Name of Debugging tab")
  String tabNameDebugging();

  @DefaultMessage("Please choose a project to open or create a new project.")
  @Description("Message shown when there is no current file editor to switch to")
  String chooseProject();

  @DefaultMessage("My Components")
  @Description("Caption for component list box.")
  String componentListBoxCaption();

  @DefaultMessage("Emails")
  @Description("Title for user's email inbox")
  String emailInboxTitle();

  @DefaultMessage("Send an Email")
  @Description("Title for moderator send email dialog")
  String emailSendTitle();

  // Used in boxes/AssetListBox.java

  @DefaultMessage("Media")
  @Description("Caption for asset list box.")
  String assetListBoxCaption();

  // Used in boxes/MessagesOutputBox.java

  @DefaultMessage("Messages")
  @Description("Caption for message output box.")
  String messagesOutputBoxCaption();

  // Used in boxes/OdeLogBox.java

  @DefaultMessage("Developer Messages")
  @Description("Caption for ODE log box.")
  String odeLogBoxCaption();

  // Used in boxes/PaletteBox.java

  @DefaultMessage("Palette")
  @Description("Caption for palette box.")
  String paletteBoxCaption();

  // Used in boxes/ProjectListBox.java

  @DefaultMessage("Projects")
  @Description("Caption for project list box.")
  String projectListBoxCaption();

  // Used in boxes/TrashProjectListBox.java

  @DefaultMessage("Trash")
  @Description("Caption for trash list box.")
  String trashprojectlistbox();

  // Used in boxes/PropertiesBox.java

  @DefaultMessage("Properties")
  @Description("Caption for properties box.")
  String propertiesBoxCaption();

  // Used in boxes/SourceStructureBox.java

  @DefaultMessage("Components")
  @Description("Caption for source structure box.")
  String sourceStructureBoxCaption();

  @DefaultMessage("All Components")
  @Description("Caption for source structure box.")
  String sourceStructureBoxCaptionAll();

  @DefaultMessage("Visible Components")
  @Description("Caption for source structure box.")
  String sourceStructureBoxCaptionVisible();

  @DefaultMessage("Non-visible Components")
  @Description("Caption for source structure box.")
  String sourceStructureBoxCaptionNonVisible();

  @DefaultMessage("{0} components selected")
  @Description("Component multi-select caption.")
  String componentsSelected(int componentCount);

  // Used in BlocksToolkit (SubsetJSONPropertyEditor)

  @DefaultMessage("Toolkit")
  @Description("Title for Blocks Toolkit custom editor.")
  String blocksToolkitTitle();

  // Used in boxes/BlockSelectorBox.java

  @DefaultMessage("Blocks")
  @Description("Caption for block selector box.")
  String blockSelectorBoxCaption();

  @DefaultMessage("Built-in")
  @Description("Label on built-in-blocks branch of block selector tree")
  String builtinBlocksLabel();

  @DefaultMessage("Control")
  @Description("Label on built-in-Control-blocks branch of block selector tree")
  String builtinControlLabel();

  @DefaultMessage("Logic")
  @Description("Label on built-in-Logic-blocks branch of block selector tree")
  String builtinLogicLabel();

  @DefaultMessage("Text")
  @Description("Label on built-in-Text-blocks branch of block selector tree")
  String builtinTextLabel();

  @DefaultMessage("Lists")
  @Description("Label on built-in-Lists-blocks branch of block selector tree")
  String builtinListsLabel();

  @DefaultMessage("Dictionaries")
  @Description("Label on built-in-Dictionaries-blocks branch of block selector tree")
  String builtinDictionariesLabel();

  @DefaultMessage("Colors")
  @Description("Label on built-in-Colors-blocks branch of block selector tree")
  String builtinColorsLabel();

  @DefaultMessage("Variables")
  @Description("Label on built-in-Variables-blocks branch of block selector tree")
  String builtinVariablesLabel();

  @DefaultMessage("Procedures")
  @Description("Label on built-in-Procedures-blocks branch of block selector tree")
  String builtinProceduresLabel();

  @DefaultMessage("Any component")
  @Description("Label on any-component branch of block selector tree")
  String anyComponentLabel();

  @DefaultMessage("Any ")
  @Description("None")
  String textAnyComponentLabel();

  // Used in ListView data property editor

  @DefaultMessage("Add Data to the ListView")
  @Description("Title Bar on ListData property editor")
  String listDataAddDataTitle();

  @DefaultMessage("Main Text")
  @Description("Column header for main text on ListData property editor")
  String listDataMainTextHeader();

  @DefaultMessage("Detail Text")
  @Description("Column header for detail text on ListData property editor")
  String listDataDetailTextHeader();

  @DefaultMessage("Image")
  @Description("Column header for image file name on ListData property editor")
  String listDataImageHeader();

  @DefaultMessage("Are you sure you want to exit without saving data?")
  @Description("Confirm cancel from ListData property editor without saving data.")
  String listDataConcelConfirm();


  // Used in boxes/ViewerBox.java

  @DefaultMessage("Viewer")
  @Description("Caption for a viewer box.")
  String viewerBoxCaption();

  // Used in SaveAllEditorsCommand.java

  @DefaultMessage("Saved project at {0}")
  @Description("Message reported when project was saved successfully.")
  String savedProject(String saveTime);

  // Used in editor/EditorManager.java

  @DefaultMessage("Server error: could not save one or more files. Please try again later!")
  @Description("Error message reported when one or more file couldn't be saved to the server.")
  String saveErrorMultipleFiles();

  @DefaultMessage("Error generating Yail for screen {0}: {1}. Please fix and try packaging again.")
  @Description("Error message reported when yail generation fails for a screen")
  String yailGenerationError(String formName, String description);

  // Used in editor/simple/SimpleNonVisibleComponentsPanel.java

  @DefaultMessage("Non-visible components")
  @Description("Header for the non-visible components in the designer.")
  String nonVisibleComponentsHeader();

  // Used in editor/simple/SimpleVisibleComponentsPanel.java

  @DefaultMessage("Display hidden components in Viewer")
  @Description("Checkbox controlling whether to display invisible components in the designer.")
  String showHiddenComponentsCheckbox();

  @DefaultMessage("Display components with visible property set to FALSE")
  @Description("Alternate phrasing of showHiddenComponentsCheckbox.")
  String showInvisiblePropertyComponents();

  @DefaultMessage("Device size")
  @Description("Caption for listbox selecting device preview screen size.")
  String previewDeviceSize();

  @DefaultMessage("Tablet size")
  @Description("Listbox (tablet) controlling whether to display a preview on Tablet size.")
  String previewTabletSize();

  @DefaultMessage("Tablet")
  @Description("Listbox (tablet) controlling whether to display a preview on Tablet size.")
  String previewTablet();

  @DefaultMessage("Phone size")
  @Description("Listbox (phone) controlling whether to display a preview on Phone size.")
  String previewPhoneSize();

  @DefaultMessage("Phone")
  @Description("Listbox (phone) controlling whether to display a preview on Phone size.")
  String previewPhone();

  @DefaultMessage("Monitor size")
  @Description("Listbox (monitor) controlling whether to display a preview on Monitor size.")
  String previewMonitorSize();

  @DefaultMessage("Monitor")
  @Description("Listbox (monitor) controlling whether to display a preview on Monitor size.")
  String previewMonitor();

  @DefaultMessage("Device Operating System")
  @Description("Caption for listbox choosing the OS Theme Style for device preview")
  String previewOperatingSystem();

  @DefaultMessage("Android 3.0-4.4.2 Devices")
  @Description("Listbox (monitor) controlling whether to display Android Holo Theme Style")
  String previewAndroid4Devices();

  @DefaultMessage("Android 5+ Devices")
  @Description("Listbox (monitor) controlling whether to display Android Material Theme Style")
  String previewAndroid5Devices();

  @DefaultMessage("iOS 13")
  @Description("Listbox (monitor) controlling whether to display iOS Theme in Neo layout")
  String previewIOS13();

  @DefaultMessage("Android 3.0-4.x (Holo)")
  @Description("Listbox (monitor) controlling whether to display Android Holo Theme Style")
  String previewAndroidHolo();

  @DefaultMessage("Android 5+ (Material)")
  @Description("Listbox (monitor) controlling whether to display Android Material Theme for Neo Layout")
  String previewAndroidMaterial();

  @DefaultMessage("iOS 13 Devices")
  @Description("Listbox (monitor) controlling whether to display iOS Theme Style")
  String previewIOS();

  // Used in editor/simple/components/MockComponent.java

  @DefaultMessage("Rename Component")
  @Description("Title for the rename component dialog")
  String renameTitle();

  @DefaultMessage("Component names can contain only letters, numbers, and underscores and " +
      "must start with a letter")
  @Description("Error message when component name contains non-alphanumeric characters besides _ " +
      "or does not start with a letter")
  String malformedComponentNameError();

  @DefaultMessage("Duplicate component name!")
  @Description("Error shown when a new component name would be the same as an existing one")
  String duplicateComponentNameError();

  @DefaultMessage("Component instance names cannot be the same as a component type")
  @Description("Error shown when a new component name would be the same as a component type name")
  String sameAsComponentTypeNameError();

  @DefaultMessage("Component names cannot be the same as that of a component instance")
  @Description("Error shown when a new component type would be the same as a component instance name")
  String sameAsComponentInstanceNameError();

  @DefaultMessage("Name cannot be any of the following: CsvUtil, Double, Float, " +
      "Integer, JavaCollection, JavaIterator, KawaEnvironment, Long, Short, SimpleForm, String, " +
      "Pattern, YailDictionary, YailList, YailNumberToString, YailRuntimeError, abstract, continue, for, new, " +
      "switch, assert, default, goto, package, synchronized, boolean, do, if, private, this, break, " +
      "double, implements, protected, throw, byte, else, import, public, throws, case, enum, instanceof, " +
      "return, transient, catch, extends, int, short, try, char, final, interface, static, void, class, " +
      "finally, long, strictfp, volatile, const, float, native, super, while, begin, def, foreach, forrange, JavaStringUtils, quote")
  @Description("Error shown when a new name is a reserved name in Yail or Java code")
  String reservedNameError();

  @DefaultMessage("Deleting this component will delete all blocks associated with it in the " +
      "Blocks Editor. Are you sure you want to delete?")
  @Description("Confirmation query for deleting a component")
  String reallyDeleteComponent();

  @DefaultMessage("Removing this component will delete all components and blocks associated with them in the " +
          "Project. Are you sure you want to delete?")
  @Description("Confirmation query for removing a component")
  String reallyRemoveComponent();

  // Used in editor/simple/components/MockButtonBase.java, MockCheckBox.java, MockLabel.java, and
  // MockRadioButton.java

  @DefaultMessage("Text for {0}")
  @Description("Default value for Text property")
  String textPropertyValue(String componentName);

  // Used in editor/simple/components/MockButtonBase.java, MockHVLayoutBase.java
  @DefaultMessage("System error: Bad value - {0} - for Horizontal Alignment.")
  @Description("Default message for bad value for Horizontal Alignment")
  String badValueForHorizontalAlignment(String componentName);

  @DefaultMessage("System error: Bad value - {0} - for Vertical Alignment.")
  @Description("Default message for bad value for Vartical Alignment")
  String badValueForVerticalAlignment(String componentName);

  // Used in editor/simple/components/MockVisibleComponent.java

  @DefaultMessage("Width")
  @Description("Caption for the width property")
  String widthPropertyCaption();

  @DefaultMessage("Height")
  @Description("Caption for the height property")
  String heightPropertyCaption();

  // Used in editor/simple/components/MockTextBoxBase.java

  @DefaultMessage("Hint for {0}")
  @Description("Default value for Hint property")
  String hintPropertyValue(String componentName);

  // Used in editor/simple/palette/ComponentHelpWidget.java

  @DefaultMessage("Extension Version:")
  @Description("Header for extension version information")
  String externalComponentVersion();

  @DefaultMessage("Date Built:")
  @Description("Header to indicate the date an extension was compiled")
  String dateBuilt();

  @DefaultMessage("More information")
  @Description("Label of the link to a component's reference docs")
  String moreInformation();

  @DefaultMessage("View license")
  @Description("Label of the link to a component's attribution license")
  String viewLicense();

  // Used in editor/youngandroid/YaFormEditor.java and YaBlocksEditor.java

  @DefaultMessage("Server error: could not load file. Please try again later!")
  @Description("Error message reported when a source file couldn't be loaded from the server.")
  String loadError();

  @DefaultMessage("Server error: could not save file. Please try again later!")
  @Description("Error message reported when a source file couldn't be saved to the server.")
  String saveError();

  @DefaultMessage("{0} blocks")
  @Description("Tab name for blocks editor")
  String blocksEditorTabName(String formName);

  // Used in editor/youngandroid/BlocklyPanel.java

  @DefaultMessage("The blocks area did not load properly. Changes to the blocks for screen {0} will not be saved.")
  @Description("Message indicating that blocks changes were not saved")
  String blocksNotSaved(String formName);

  @DefaultMessage("The blocks for screen {0} did not load properly. "
      + "You will not be able to edit using the blocks editor until the problem is corrected.")
  @Description("Message when blocks fail to load properly")
  String blocksLoadFailure(String formName);

  // Used in editor/youngandroid/palette/YoungAndroidPalettePanel.java
  @DefaultMessage("Type / to search components")
  @Description("Text shown in the component palette search box")
  String searchComponents();

  //Used in editor/youngandroid/properties/YoungAndroidAccelerometerSensitivityChoicePropertyEditor.java

  @DefaultMessage("weak")
  @Description("Text for accelerometer sensitivity choice 'weak'")
  String weakAccelerometerSensitivity();

  @DefaultMessage("moderate")
  @Description("Text for accelerometer sensitivity choice 'moderate'")
  String moderateAccelerometerSensitivity();

  @DefaultMessage("strong")
  @Description("Text for accelerometer sensitivity choice 'strong'")
  String strongAccelerometerSensitivity();

  // Used in editor/youngandroid/properties/YoungAndroidSizingChoicePropertyEditor.java

  @DefaultMessage("Fixed")
  @Description("Text for Sizing choice 'fixed' -- scale to fit device screen")
  String fixedSizing();

  @DefaultMessage("Responsive")
  @Description("Text for Sizing choice 'responsive' -- size based on device type")
  String responsiveSizing();

  // Used in editor/youngandroid/properties/YoungAndroidAlignmentChoicePropertyEditor.java

  @DefaultMessage("left")
  @Description("Text for text alignment choice 'left'")
  String leftTextAlignment();

  @DefaultMessage("center")
  @Description("Text for text alignment choice 'center'")
  String centerTextAlignment();

  @DefaultMessage("right")
  @Description("Text for text alignment choice 'right'")
  String rightTextAlignment();

  // Used in
  // editor/youngandroid/properties/YoungAndroidHorizontalAlignmentChoicePropertyEditor.java

  @DefaultMessage("Left")
  @Description("Text for horizontal alignment choice 'Left")
  String horizontalAlignmentChoiceLeft();

  @DefaultMessage("Right")
  @Description("Text for horizontal alignment choice 'Right'")
  String horizontalAlignmentChoiceRight();

  @DefaultMessage("Center")
  @Description("Text for horizontal alignment choice 'Center'")
  String horizontalAlignmentChoiceCenter();

  // Used in
  // editor/youngandroid/properties/YoungAndroidVerticalAlignmentChoicePropertyEditor.java

  @DefaultMessage("Top")
  @Description("Text for vertical alignment choice 'Top'")
  String verticalAlignmentChoiceTop();

  @DefaultMessage("Center")
  @Description("Text for vertical alignment choice 'Center'")
  String verticalAlignmentChoiceCenter();

  @DefaultMessage("Bottom")
  @Description("Text for vertical alignment choice 'Bottom'")
  String verticalAlignmentChoiceBottom();

  // Used in editor/youngandroid/properties/YoungAndroidButtonShapeChoicePropertyEditor.java

  @DefaultMessage("default")
  @Description("Text for button shape choice 'default'")
  String defaultButtonShape();

  @DefaultMessage("rounded")
  @Description("Text for button shape choice 'rounded'")
  String roundedButtonShape();

  @DefaultMessage("rectangular")
  @Description("Text for button shape choice 'rectangular'")
  String rectButtonShape();

  @DefaultMessage("oval")
  @Description("Text for button shape choice 'oval'")
  String ovalButtonShape();

  // Used in editor/youngandroid/properties/YoungAndroidRecyclerViewOrientationPropertyEditor.java

  @DefaultMessage("vertical")
  @Description("Text for recycler view orientation 'vertical'")
  String verticalOrientation();

  @DefaultMessage("horizontal")
  @Description("Text for recycler view orientation 'horizontal'")
  String horisontalOrientation();

  @DefaultMessage("grid")
  @Description("Text for recycler view orientation 'grid'")
  String gridOrientation();

  // Used in editor/youngandroid/properties/YoungAndroidAssetSelectorPropertyEditor.java

  @DefaultMessage("You must select an asset!")
  @Description("Message displayed when OK button is clicked when there is no asset selected.")
  String noAssetSelected();

  // Used in editor/youngandroid/properties/YoungAndroidComponentSelectorPropertyEditor.java

  @DefaultMessage("You must select a component!")
  @Description("Message displayed when OK button is clicked when there is no component selected.")
  String noComponentSelected();

  // Used in editor/youngandroid/properties/YoungAndroidColorChoicePropertyEditor.java

  @DefaultMessage("None")
  @Description("Text for color choice 'None'")
  String noneColor();

  @DefaultMessage("Black")
  @Description("Text for color choice 'Black'")
  String blackColor();

  @DefaultMessage("Blue")
  @Description("Text for color choice 'Blue'")
  String blueColor();

  @DefaultMessage("Cyan")
  @Description("Text for color choice 'Cyan'")
  String cyanColor();

  @DefaultMessage("Default")
  @Description("Text for color choice 'Default'")
  String defaultColor();

  @DefaultMessage("Dark Gray")
  @Description("Text for color choice 'Dark Gray'")
  String darkGrayColor();

  @DefaultMessage("Gray")
  @Description("Text for color choice 'Gray'")
  String grayColor();

  @DefaultMessage("Green")
  @Description("Text for color choice 'Green'")
  String greenColor();

  @DefaultMessage("Light Gray")
  @Description("Text for color choice 'Light Gray'")
  String lightGrayColor();

  @DefaultMessage("Magenta")
  @Description("Text for color choice 'Magenta'")
  String magentaColor();

  @DefaultMessage("Orange")
  @Description("Text for color choice 'Orange'")
  String orangeColor();

  @DefaultMessage("Pink")
  @Description("Text for color choice 'Pink'")
  String pinkColor();

  @DefaultMessage("Red")
  @Description("Text for color choice 'Red'")
  String redColor();

  @DefaultMessage("White")
  @Description("Text for color choice 'White'")
  String whiteColor();

  @DefaultMessage("Yellow")
  @Description("Text for color choice 'Yellow'")
  String yellowColor();

  // Used in editor/youngandroid/properties/YoungAndroidFontTypefaceChoicePropertyEditor.java

  @DefaultMessage("default")
  @Description("Text for font typeface choice 'default '")
  String defaultFontTypeface();

  @DefaultMessage("sans serif")
  @Description("Text for font typeface choice 'sans serif '")
  String sansSerifFontTypeface();

  @DefaultMessage("serif")
  @Description("Text for font typeface choice 'serif '")
  String serifFontTypeface();

  @DefaultMessage("monospace")
  @Description("Text for font typeface choice 'monospace '")
  String monospaceFontTypeface();

  // Used in editor/youngandroid/properties/YoungAndroidLengthPropertyEditor.java

  @DefaultMessage("Automatic")
  @Description("Caption and summary for Automatic choice")
  String automaticCaption();

  @DefaultMessage("Fill parent")
  @Description("Caption and summary for Fill Parent choice")
  String fillParentCaption();

  @DefaultMessage("percent")
  @Description("Caption for percent label")
  String percentCaption();

  @DefaultMessage("pixels")
  @Description("Caption for pixels label")
  String pixelsCaption();

  @DefaultMessage("{0} pixels")
  @Description("Summary for custom length in pixels")
  String pixelsSummary(String pixels);

  @DefaultMessage("{0} percent")
  @Description("Summary for length in percent")
  String percentSummary(String percent);

  @DefaultMessage("The value must be an integer greater than or equal to 0")
  @Description("Error shown after validation of custom length field failed.")
  String nonnumericInputError();

  @DefaultMessage("Percentage input values should be between 0 and 100")
  @Description("Error shown after validation of percentage input fields.")
  String nonvalidPercentValue();

  // Used in editor/youngandroid/properties/YoungAndroidScreenAnimationChoicePropertyEditor.java

  @DefaultMessage("Default")
  @Description("Text for screen animation choice 'Default '")
  String defaultScreenAnimation();

  @DefaultMessage("Fade")
  @Description("Text for screen animation choice 'Fade '")
  String fadeScreenAnimation();

  @DefaultMessage("Zoom")
  @Description("Text for screen animation choice 'Zoom '")
  String zoomScreenAnimation();

  @DefaultMessage("SlideHorizontal")
  @Description("Text for screen animation choice 'SlideHorizontal '")
  String slideHorizontalScreenAnimation();

  @DefaultMessage("SlideVertical")
  @Description("Text for screen animation choice 'SlideVertical '")
  String slideVerticalScreenAnimation();

  @DefaultMessage("None")
  @Description("Text for screen animation choice 'None '")
  String noneScreenAnimation();

  // Used in editor/youngandroid/properties/YoungAndroidScreenOrientationChoicePropertyEditor.java

  @DefaultMessage("Unspecified")
  @Description("Text for screen orientation choice 'Unspecified '")
  String unspecifiedScreenOrientation();

  @DefaultMessage("Portrait")
  @Description("Text for screen orientation choice 'Portrait '")
  String portraitScreenOrientation();

  @DefaultMessage("Landscape")
  @Description("Text for screen orientation choice 'Landscape '")
  String landscapeScreenOrientation();

  @DefaultMessage("Sensor")
  @Description("Text for screen orientation choice 'Sensor '")
  String sensorScreenOrientation();

  @DefaultMessage("User")
  @Description("Text for screen orientation choice 'User '")
  String userScreenOrientation();

  // Used in editor/youngandroid/properties/YoungAndroidToastLengthChoicePropertyEditor.java

  @DefaultMessage("Short")
  @Description("Show toast for a Toast_Short of time")
  String shortToastLength();

  @DefaultMessage("Long")
  @Description("Show toast for a Toast_Long of time")
  String longToastLength();

  // Used in explorer/SourceStructureExplorer.java

  @DefaultMessage("Rename")
  @Description("Label of the button for rename")
  String renameButton();

  // Used in explorer/commands/AddFormCommand.java

  @DefaultMessage("Add")
  @Description("Text on 'Add' button to continue with screen creation.")
  String addScreenButton();

  @DefaultMessage("Do Not Add")
  @Description("Text on 'Dont Add' button to dismiss screen creation.")
  String cancelScreenButton();

  @DefaultMessage("New Screen")
  @Description("Title of new Screen dialog.")
  String newFormTitle();

  @DefaultMessage("Screen name:")
  @Description("Label in front of name in new screen dialog.")
  String formNameLabel();

  @DefaultMessage("WARNING: The number of screens in this app might exceed the limits of App Inventor. " +
                  "Click <a target=\"_blank\" href=\"/reference/other/manyscreens.html\">here</a> for advice about " +
                  "creating apps with many screens. " +
                  "<p>Do you really want to add another screen?</p>")
  @Description("Label to indicate the application has too many screens.")
  String formCountErrorLabel();

  @DefaultMessage("Screen names can contain only letters, numbers, and underscores and must " +
      "start with a letter")
  @Description("Error message when form name contains non-alphanumeric characters besides _")
  String malformedFormNameError();

  @DefaultMessage("Duplicate Screen name!")
  @Description("Error shown when a new form name would be the same as an existing one")
  String duplicateFormNameError();

  @DefaultMessage("Server error: could not add form. Please try again later!")
  @Description("Error message reported when adding a form failed on the server.")
  String addFormError();

  // Used in explorer/commands/BuildCommand.java, and
  // explorer/commands/WaitForBuildResultCommand.java

  @DefaultMessage("Build of {0} requested at {1}.")
  @Description("Message shown in the build output panel when a build is requested.")
  String buildRequestedMessage(String projectName, String time);

  @DefaultMessage("Server error: could not build target. Please try again later!")
  @Description("Error message reported when building a target failed on the server because of a " +
      "network error.")
  String buildError();

  @DefaultMessage("Build failed!")
  @Description("Error message reported when a build failed due to an error in the build pipeline.")
  String buildFailedError();

  @DefaultMessage("Sorry, cannot package projects larger than {0} MB. Yours is "
      + "{1,number,###.##} MB.")
  @Description("Error message reported when a project is too large to build.")
  String buildProjectTooLargeError(int maxSize, double aiaSize);

  @DefaultMessage("The build server is currently busy. Please try again in a few minutes.")
  @Description("Error message reported when the build server is temporarily too busy to accept " +
      "a build request.")
  String buildServerBusyError();

  @DefaultMessage("The build server is not compatible with this version of App Inventor.")
  @Description("Error message reported when the build server is running a different version of " +
      "the App Inventor code.")
  String buildServerDifferentVersion();

  @DefaultMessage("Unable to generate code for {0}.")
  @Description("Message displayed when an error occurs while generating YAIL for a form.")
  String errorGeneratingYail(String formName);

  // Used in explorer/commands/CommandRegistory.java
  @DefaultMessage("Preview...")
  @Description("Label for the context menu command that previews a file")
  String previewFileCommand();

  @DefaultMessage("Close Preview")
  @Description("Text for closing a file preview window")
  String closeFilePreview();

  @DefaultMessage("Delete...")
  @Description("Label for the context menu command that deletes a file")
  String deleteFileCommand();

  @DefaultMessage("Download to my computer")
  @Description("Label for the context menu command that downloads a file")
  String downloadFileCommand();

  // Used in explore/commands/FilePreviewCommand.java
  @DefaultMessage("Unfortunately, a preview for this file is unavailable.")
  @Description("Text for files not compatible with HTML5 elements")
  String filePreviewError();

  @DefaultMessage("Unfortunately, your browser does not support playback of this file.")
  @Description("Text for browsers not compatable with HTML5 elements")
  String filePlaybackError();

  // Used in explorer/commands/CopyYoungAndroidProjectCommand.java

  @DefaultMessage("Checkpoint - {0}")
  @Description("Title of checkpoint dialog.")
  String checkpointTitle(String projectName);

  @DefaultMessage("Save As - {0}")
  @Description("Title of save as dialog.")
  String saveAsTitle(String projectName);

  @DefaultMessage("{0}_checkpoint{1}")
  @Description("Default project name in checkoint dialog")
  String defaultCheckpointProjectName(String projectName, String suffix);

  @DefaultMessage("Previous checkpoints:")
  @Description("Label for previous checkpoints table in checkpoint dialog.")
  String previousCheckpointsLabel();

  @DefaultMessage("{0}_copy")
  @Description("Defaulf project name in save as dialog")
  String defaultSaveAsProjectName(String projectName);

  @DefaultMessage("Checkpoint name:")
  @Description("Label in front of new name in checkpoint dialog.")
  String checkpointNameLabel();

  @DefaultMessage("Server error: could not copy project. Please try again later!")
  @Description("Error message reported when copying a project failed on the server.")
  String copyProjectError();

  // Used in explorer/commands/DeleteFileCommand.java

  @DefaultMessage("Do you really want to delete this file?  It will be removed from " +
      "the App Inventor server.  Also, parts of your application may still refer to the deleted " +
      "file, and you will need to change these.")
  @Description("Confirmation message that will be shown before deleting a file")
  String reallyDeleteFile();

  @DefaultMessage("Server error: could not delete the file. Please try again later!")
  @Description("Error message reported when deleting a file failed on the server.")
  String deleteFileError();

  // Used in explorer/commands/EnsurePhoneConnectedCommand.java

  @DefaultMessage("The phone is not connected.")
  @Description("Error message displayed when the user wants to download a project to the phone, " +
      "but the phone is not connected.")
  String phoneNotConnected();

  // Used in explorer/commands/ShowBarcodeCommand.java

  @DefaultMessage("Android App for {0}")
  @Description("Title of download apk dialog.")
  String downloadApkDialogTitle(String projectName);

  @DefaultMessage("Android App Bundle for {0} (to be used in Google Play Store)")
  @Description("Title of download aab dialog.")
  String downloadAabDialogTitle(String projectName);

  @DefaultMessage("Download .apk now")
  @Description("Download button shown in barcode dialog")
  String barcodeDownloadApk();

  @DefaultMessage("Download .aab now")
  @Description("Download button shown in barcode dialog")
  String barcodeDownloadAab();

  @DefaultMessage("Note: this barcode is only valid for 2 hours. See {0} the FAQ {1} for info " +
      "on how to share your app with others.")
  String barcodeWarning(String aTagStart, String aTagEnd);

  @DefaultMessage("<b>Click the button to download the app, right-click on it to copy a download link, or scan the " +
          "code with a barcode scanner to install.</b><br>" +
          "Note: this link and barcode are only valid for 2 hours. See {0} the FAQ {1} for info on how to share your " +
          "app with others.")
  @Description("Warning in barcode dialog.")
  String barcodeWarning2(String aTagStart, String aTagEnd);

  // Used in explorer/project/Project.java

  @DefaultMessage("Server error: could not load project. Please try again later!")
  @Description("Error message reported when a project could not be loaded from the server.")
  String projectLoadError();

  // Used in explorer/project/ProjectManager.java

  @DefaultMessage("Server error: could not retrieve project information. Please try again later!")
  @Description("Error message reported when information about projects could not be retrieved " +
      "from the server.")
  String projectInformationRetrievalError();

  // Used in explorer/youngandroid/Toolbar.java

  @DefaultMessage("It may take a little while for your projects to be downloaded. " +
      "Please be patient...")
  @Description("Warning that downloading projects will take a while")
  String downloadAllAlert();

  @DefaultMessage("More Actions")
  @Description("Label of the button leading to more cascade items")
  String moreActionsButton();

  @DefaultMessage("Download User Source")
  @Description("Title of the dialog box for downloading a user's project source")
  String downloadUserSourceDialogTitle();

  @DefaultMessage("User id or email (case-sensitive):")
  @Description("Label for the user id input text box")
  String userIdLabel();

  @DefaultMessage("Project id or name:")
  @Description("Label for the project id input text box")
  String projectIdLabel();

  @DefaultMessage("Please only select one project")
  @Description("Error Message for when more then one project (or no projects) " +
    "are selected to send to the new gallery")
  String selectOnlyOneProject();

  @DefaultMessage("Please specify both a user email address or id and a project name or id " +
      "for the project to be downloaded. Ids are numeric and may come from the system " +
      "logs or from browsing the Datastore. If you use an email address, it must match " +
      "exactly the stored email address in the Datastore. Similarly, project names must " +
      "match exactly. Both are case sensitive.")
  @Description("Error message reported when user id or project id is missing")
  String invalidUserIdOrProjectIdError();

  @DefaultMessage("Please select a project to delete")
  @Description("Error message displayed when no project is selected")
  String noProjectSelectedForDelete();

  @DefaultMessage("Please select a project to restore")
  @Description("Error message displayed when no project is selected")
  String noProjectSelectedForRestore();

  @DefaultMessage("Are you really sure you want to delete this project: {0}")
  @Description("Confirmation message for selecting a single project and clicking delete")
  String confirmDeleteSingleProject(String projectName);

  //moving single project to trash
  @DefaultMessage("Are you really sure you want to move this project to trash: {0}")
  @Description("Confirmation message for selecting a single project and clicking trash")
  String confirmMoveToTrashSingleProject(String projectName);

  @DefaultMessage("Are you really sure you want to delete these projects: {0}?")
  @Description("Confirmation message for selecting multiple projects and clicking delete")
  String confirmDeleteManyProjects(String projectNames);

  @DefaultMessage("Are you sure you want to move these items to trash? {0}")
  @Description("Confirmation message for selecting multiple projects and clicking trash")
  String confirmMoveToTrash(String projectNames);
  @DefaultMessage("Are you sure you want to delete these items permanently? {0}")
  @Description("Confirmation message for selecting multiple projects in trash and clicking delete")
  String confirmDeleteForever(String projectNames);

  @DefaultMessage("Projects: {0}")
  @Description("Information on selected projects for delete/trash confirmation")
  String confirmTrashDeleteProjects(String projectNames);

  @DefaultMessage("Folders: {0}")
  @Description("Information on selected folders for delete/trash confirmation")
  String confirmTrashDeleteFolders(String folderNames);

  @DefaultMessage("Server error: could not delete project. Please try again later!")
  @Description("Error message reported when deleting a project failed on the server.")
  String deleteProjectError();

  @DefaultMessage("Server error: could not restore project. Please try again later!")
  @Description("Error message reported when restoring a project failed on the server.")
  String restoreProjectError();

  @DefaultMessage("Server error: could not move project to trash. Please try again later!")
  @Description("Error message reported when moving the project to trash failed on the server.")
  String moveToTrashProjectError();

  @DefaultMessage("One project must be selected")
  @Description("Error message displayed when no or many projects are selected")
  String wrongNumberProjectsSelected();

  @DefaultMessage("Please select only one project to publish or update")
  @Description("Error message displayed when zero or more than one projects are selected")
  String wrongNumberProjectSelectedForPublishOrUpdate();

  @DefaultMessage("Server error: could not download your keystore file.")
  @Description("Error message displayed when a server error occurs during download keystore")
  String downloadKeystoreError();

  @DefaultMessage("There is no keystore file to download.")
  @Description("Error message displayed when no keystore file exists")
  String noKeystoreToDownload();

  @DefaultMessage("Server error: could not upload your keystore file.")
  @Description("Error message displayed when a server error occurs during upload keystore")
  String uploadKeystoreError();

  @DefaultMessage("Do you want to overwrite your keystore file?\n\n" +
      "If you agree, your old keystore file will be completely removed from the App Inventor " +
      "server.\n\n" +
      "If you have published applications to the Google Play Store using the keystore you are " +
      "about to overwrite, you will lose the ability to update your applications.\n\n" +
      "Any projects that you package in the future will be signed using your new keystore file. " +
      "Changing the keystore affects the ability to reinstall previously installed apps. If you " +
      "are not sure that you want to do this, please read the documentation about keystores by " +
      "clicking above on \"Help\", then \"Troubleshooting\", and then \"Keystores and Signing " +
      "of Applications\"\n\n" +
      "There is no undo for overwriting your keystore file.")
  @Description("Confirmation message shown when keystore is about to be overwritten.")
  String confirmOverwriteKeystore();

  @DefaultMessage("Server error: could not delete your keystore file.")
  @Description("Error message reported when a server error occurs during delete keystore")
  String deleteKeystoreError();

  @DefaultMessage("Do you really want to delete your keystore file?\n\n" +
      "If you agree, your old keystore file will be completely removed from the App Inventor " +
      "server. A new, but different, keystore file will be created automatically the next time " +
      "you package a project for the phone.\n\n" +
      "If you have published applications to the Google Play Store using the keystore you are " +
      "about to delete, you will lose the ability to update your applications.\n\n" +
      "Any projects that you package in the future will be signed using your new keystore file. " +
      "Changing the keystore affects the ability to reinstall previously installed apps. If you " +
      "are not sure that you want to do this, please read the documentation about keystores by " +
      "clicking above on \"Help\", then \"Troubleshooting\", and then \"Keystores and Signing " +
      "of Applications\"\n\n" +
      "There is no undo for deleting your keystore file.")
  @Description("Confirmation message for delete keystore")
  String confirmDeleteKeystore();

  // Used in output/OdeLog.java

  @DefaultMessage("Clear")
  @Description("Text on 'Clear' button")
  String clearButton();

  // Used in settings/CommonSettings.java, settings/project/ProjectSettings.java, and
  // settings/user/UserSettings.java

  @DefaultMessage("Server error: could not load settings. Please try again later!")
  @Description("Error message reported when the settings couldn't be loaded from the server.")
  String settingsLoadError();

  @DefaultMessage("Server error: could not save settings. Please try again later!")
  @Description("Error message reported when the settings couldn't be saved to the server.")
  String settingsSaveError();

  // Used in widgets/boxes/Box.java

  @DefaultMessage("Done")
  @Description("Caption for button to finish the box resizing dialog.")
  String done();

  @DefaultMessage("Close")
  @Description("Tool tip text for header icon for closing/removing a minimized box.")
  String hdrClose();

  @DefaultMessage("Shrink")
  @Description("Tool tip text for header icon for minimizing the box.")
  String hdrMinimize();

  @DefaultMessage("Settings")
  @Description("Tool tip text for header icon for context menu of box.")
  String hdrSettings();

  @DefaultMessage("Shrink")
  @Description("Caption for context menu item for minimizing the box.")
  String cmMinimize();

  @DefaultMessage("Expand")
  @Description("Caption for context menu item for restoring a minimized box.")
  String cmRestore();

  @DefaultMessage("Resize...")
  @Description("Caption for context menu item for resizing the box.")
  String cmResize();

  @DefaultMessage("Expand")
  @Description("Tool tip text for header icon for restoring a minimized box.")
  String hdrRestore();

  // Used in widgets/properties/FloatPropertyEditor.java

  @DefaultMessage("{0} is not a legal number")
  @Description("Error shown after validation of float failed.")
  String notAFloat(String nonNumericText);

  // Used in widgets/properties/IntegerPropertyEditor.java

  @DefaultMessage("{0} is not a legal integer")
  @Description("Error shown after validation of integer failed.")
  String notAnInteger(String nonNumericText);

  // Used in widgets/properties/TextPropertyEditor.java

  @DefaultMessage("Malformed input!")
  @Description("Error shown after validation of input text failed.")
  String malformedInputError();

  @DefaultMessage("Beginner")
  @Description("List item in Subset Property Editor")
  String beginnerToolkitButton();

  @DefaultMessage("Intermediate")
  @Description("List item in Subset Property Editor")
  String intermediateToolkitButton();

  @DefaultMessage("Expert")
  @Description("List item in Subset Property Editor")
  String expertToolkitButton();

  @DefaultMessage("All")
  @Description("List item in Subset Property Editor")
  String allButton();

  @DefaultMessage("View And Modify")
  @Description("List item in Subset Property Editor")
  String viewAndModifyButton();

  @DefaultMessage("Match Project")
  @Description("List item in Subset Property Editor")
  String matchProjectButton();

  // Used in wizards/FileUploadWizard.java

  @DefaultMessage("Upload File ...")
  @Description("Caption for file upload wizard.")
  String fileUploadWizardCaption();

  @DefaultMessage("Error: Malformed Filename")
  @Description("Error message when file name contains characters that would require URL encoding.")
  String malformedFilenameTitle();

  @DefaultMessage("File names can contain only unaccented letters, numbers, and the characters " +
      "\"-\", \"_\", \".\", \"!\", \"~\", \"*\", \"(\", and \")\"")
  @Description("Error message when file name contains characters that would require URL encoding.")
  String malformedFilename();

  @DefaultMessage("Error: Bad Filename Size")
  @Description("Error message when filenames are 0 or 101+ characters long")
  String filenameBadSizeTitle();

  @DefaultMessage("File names must be between 1 and 100 characters.")
  @Description("Error message when filenames are 0 or 101+ characters long")
  String filenameBadSize();

  @DefaultMessage("Uploading {0} to the App Inventor server")
  @Description("Message displayed when an asset is uploaded.")
  String fileUploadingMessage(String filename);

  @DefaultMessage("Server error: could not upload file. Please try again later!")
  @Description("Error message reported when a file couldn't be uploaded to the server.")
  String fileUploadError();

  @DefaultMessage("Error: could not upload file because it is too large")
  @Description("Error message reported when a file couldn't be uploaded because of its size.")
  String fileTooLargeError();

  @DefaultMessage("Error: No File Selected")
  @Description("Error message reported when a file was not selected.")
  String noFileSelectedTitle();

  @DefaultMessage("Please select a file to upload.")
  @Description("Error message reported when a file was not selected.")
  String noFileSelected();

  @DefaultMessage("Error: Cannot upload .aia file as media asset")
  @Description("Error message when user tries to upload aia file as media asset")
  String aiaMediaAssetTitle();

  @DefaultMessage("To use this file, click Projects > Import project (.aia) from" +
    " my computer ...")
  @Description("Error message when user tries to upload aia file as media asset")
  String aiaMediaAsset();

  @DefaultMessage("http://appinventor.mit.edu/explore/ai2/share.html")
  @Description("URL for more info on using aia files properly")
  String aiaMediaAssetHelp();

  @DefaultMessage("Request to save {1}" +
      "\n\nA file named {0} already exists in this project." +
      "\nDo you want to remove that old file?" +
      "\nThis will also remove any other files whose " +
      "names conflict with {1}.")
  @Description("Confirmation message shown when conflicting files are about to be deleted.")
  String confirmOverwrite(String newFile, String existingFile);

  // Used in wizards/KeystoreUploadWizard.java

  @DefaultMessage("Upload Keystore...")
  @Description("Caption for keystore upload wizard.")
  String keystoreUploadWizardCaption();

  @DefaultMessage("Server error: could not upload keystore. Please try again later!")
  @Description("Error message reported when the keystore couldn't be uploaded to the server.")
  String keystoreUploadError();

  @DefaultMessage("The selected file is not a keystore!")
  @Description("Error message reported when the file selected for upload is not a keystore.")
  String notKeystoreError();

  // Used in wizards/NewProjectWizard.java

  @DefaultMessage("Server error: could not create project. Please try again later!")
  @Description("Error message reported when the project couldn't be created on the server.")
  String createProjectError();

  // Used in wizards/TemplateUploadWizard.java

  @DefaultMessage("Create a Project from a Template")
  @Description("Caption for template upload wizard.")
  String templateUploadWizardCaption();

  @DefaultMessage("Add a New Template Library Url")
  @Description("Caption for template dialog menu item.")
  String templateUploadNewUrlCaption();

  @DefaultMessage("Input a Url...")
  @Description("Caption for input template url wizard.")
  String inputNewUrlCaption();

  @DefaultMessage("Templates Url: ")
  @Description("Label for template url wizard.")
  String newUrlLabel();
  // Used in wizards/ProjectUploadWizard.java

  @DefaultMessage("Import Project...")
  @Description("Caption for project upload wizard.")
  String projectUploadWizardCaption();

  // Used in wizards/RequestNewProjectNameWizard.java

  @DefaultMessage("Enter a New Name")
  @Description("Label for new project name Textbox.")
  String requestNewProjectNameLabel();

  @DefaultMessage("\nDo you want to continue with {0}")
  @Description("Title for dialog box.")
  String suggestNameTitleCaption(String projectName);

  @DefaultMessage("New Project Name Success")
  @Description("Title for New Project Name DialogBox on Success.")
  String successfulTitleFormat();

  @DefaultMessage("Project Name {0} is invalid")
  @Description("Title for New Project Name DialogBox on Invalid Format Error.")
  String invalidTitleFormatError(String projectName);

  @DefaultMessage("The Name {0} is Reserved")
  @Description("Title for New Project Name DialogBox on Reserved Name Error.")
  String reservedTitleFormatError(String projectName);

  @DefaultMessage("Project Name {0} exists")
  @Description("Title for New Project Name DialogBox on Duplicate Name Error.")
  String duplicateTitleFormatError(String projectName);

  @DefaultMessage("A Project with the same name {0} exists in Trash")
  @Description("Title for New Project Name DialogBox on Duplicate Name Error.")
  String duplicateTitleInTrashFormatError(String projectName);

  // Used in GalleryToolBar.java
  @DefaultMessage("Search")
  @Description("Text for gallery search button")
  String gallerySearch();

  // Used in GalleryPage.java
  @DefaultMessage("")
  @Description("Text for gallery Empty Text")
  String galleryEmptyText();

  @DefaultMessage("Feature")
  @Description("Text for gallery Feature Text")
  String galleryFeaturedText();

  @DefaultMessage("Unfeature")
  @Description("Text for gallery Unfeature Text")
  String galleryUnfeaturedText();

  @DefaultMessage("Tutorial")
  @Description("Text for gallery Tutorial  Text")
  String galleryTutorialText();

  @DefaultMessage("Untutorial")
  @Description("Text for gallery Untutorial Text")
  String galleryUntutorialText();

  @DefaultMessage(" ")
  @Description("Text for gallery Single Space Text")
  String gallerySingleSpaceText();

  @DefaultMessage("By Author")
  @Description("Text for gallery By Author Text")
  String galleryByAuthorText();

  @DefaultMessage("Open the App")
  @Description("Text for gallery page open the app button")
  String galleryOpenText();

  @DefaultMessage("Publish")
  @Description("Text for gallery page publish button")
  String galleryPublishText();

  @DefaultMessage("Update")
  @Description("Text for gallery page update button")
  String galleryUpdateText();

  @DefaultMessage("Remove")
  @Description("Text for gallery page remove button")
  String galleryRemoveText();

  @DefaultMessage("Are you really sure you want to remove this app from gallery?")
  @Description("Text for remove confirm alert")
  String galleryRemoveConfirmText();

  @DefaultMessage("Edit")
  @Description("Text for gallery page edit button")
  String galleryEditText();

  @DefaultMessage("Cancel")
  @Description("Text for gallery page cancel button")
  String galleryCancelText();

  @DefaultMessage("Please submit a screenshot or some other representative image before publishing your app")
  @Description("Error messgage for when submitting galleryapp")
  String galleryNoScreenShotMessage();

  @DefaultMessage("please provide a longer description before publishing your app")
  @Description("Error messgage for when submitting galleryapp")
  String galleryNotEnoughDescriptionMessage();

  @DefaultMessage("By ")
  @Description("Text for gallery app developer prefix text label")
  String galleryByDeveloperPrefixedText();

  @DefaultMessage("Created Date: ")
  @Description("Text for gallery page created date label")
  String galleryCreatedDateLabel();

  @DefaultMessage("Changed Date: ")
  @Description("Text for gallery page changed date label")
  String galleryChangedDateLabel();

  @DefaultMessage("Tutorial / Video: ")
  @Description("Text for gallery page more info link label")
  String galleryMoreInfoLabel();

  @DefaultMessage("Credits: ")
  @Description("Text for gallery page credit label")
  String galleryCreditLabel();

  @DefaultMessage("If this app has a tutorial or video, please enter the URL here.")
  @Description("Text for gallery page more info link hint")
  String galleryMoreInfoHint();

  @DefaultMessage("By submitting an app in the gallery, you are publishing " +
    "it under a <a href=\"https://creativecommons.org/licenses/by/4.0/\" " +
    "target=\"_blank\">Creative Commons Attribution License</a>, and " +
    "affirming that you have the authority to do so.")
  @Description("Reference to the Creative Commons License")
  String galleryCcLicenseRef();

  @DefaultMessage("Are you remixing code from other apps? Credit them here.")
  @Description("Text for gallery page credit hint")
  String galleryCreditHint();

  @DefaultMessage("Please write the description of the app here.")
  @Description("Text for gallery page description hint")
  String galleryDescriptionHint();

  @DefaultMessage("Opening ...")
  @Description("Text for gallery page opening feedback")
  String galleryAppOpening();

  @DefaultMessage("Publishing ...")
  @Description("Text for gallery page publishing feedback.")
  String galleryAppPublishing();

  @DefaultMessage("Updating ...")
  @Description("Text for gallery page updating feedback")
  String galleryAppUpdating();

  @DefaultMessage("Removing ...")
  @Description("Text for gallery page removing feedback")
  String galleryAppRemoving();

  @DefaultMessage("Like")
  @Description("Text for gallery page like button (not liked yet).")
  String galleryAppsLike();

  @DefaultMessage("Unlike")
  @Description("Text for gallery page like button (already liked).")
  String galleryAppsAlreadyLike();

  @DefaultMessage("Apps developed by")
  @Description("Title for the gallery page sidebar that shows list of apps of a specific author.")
  String galleryAppsByAuthorSidebar();

  @DefaultMessage("Created on ")
  @Description("Text prefix for the gallery app shared / created date.")
  String galleryAppCreatedPrefix();

  @DefaultMessage("Changed on ")
  @Description("Text prefix for the gallery app last changed date.")
  String galleryAppChangedPrefix();

  @DefaultMessage("Remixed from: ")
  @Description("Text prefix for the gallery app remixed from label.")
  String galleryRemixedFrom();

  @DefaultMessage("Click to view the remixes of this app!")
  @Description("Text for the gallery app remix children list.")
  String galleryRemixChildren();

  @DefaultMessage("Remixes of ")
  @Description("Title prefix for the gallery page sidebar that shows list of remixed apps.")
  String galleryAppsRemixesSidebar();

  @DefaultMessage("Please explain why you feel this app is disrespectful or inappropriate, or otherwise breaks the <a href=\"http://google.com\">App Inventor Gallery Community Guidelines.</a> " +
      "")
  @Description("Prompt for the gallery app report section.")
  String galleryReportPrompt();

  @DefaultMessage("Copy and share link: ")
  @Description("Prompt for the gallery app share section.")
  String gallerySharePrompt();

  @DefaultMessage("Copy")
  @Description("Button for copying share link to clipboard")
  String galleryCopyButton();

  @DefaultMessage("/?galleryId=")
  @Description("redirect action of galleryId")
  String galleryGalleryIdAction();

  @DefaultMessage("\n\nVisit your app: {0}/?galleryId={1}")
  @Description("gallery app link label")
  String galleryVisitGalleryAppLinkLabel(String host, long galleryId);

  @DefaultMessage("Submit report")
  @Description("Text for the gallery app report button.")
  String galleryReportButton();

  @DefaultMessage("You reported this app. An administrator will process your report shortly.")
  @Description("Prompt for the gallery app report section when user just submitted the report.")
  String galleryReportCompletionPrompt();

  @DefaultMessage("You already reported this app. An administrator will process your report shortly.")
  @Description("Prompt for the gallery app report section if user has already reported.")
  String galleryAlreadyReportedPrompt();

  @DefaultMessage("Server error: could not retrieve comments")
  @Description("Error message reported when can't get gallery app comments on server.")
  String galleryCommentError();

  @DefaultMessage("Server error: could not retrieve num of like")
  @Description("Error message reported when can't get gallery app like on server.")
  String galleryAppLikeError();

  @DefaultMessage("Server error: could not retrieve developer gallery apps from gallery")
  @Description("Error message reported when can't get developer gallery apps on server.")
  String galleryDeveloperAppError();

  @DefaultMessage("Server error: could not retrieve featured apps from gallery")
  @Description("Error message reported when can't get featured on server.")
  String galleryFeaturedAppError();

  @DefaultMessage("Server error: could not complete a search of gallery")
  @Description("Error message reported when can't search on server.")
  String gallerySearchError();

  @DefaultMessage("Server error: could not get recent apps from gallery")
  @Description("Error message reported when can't get recent apps server.")
  String galleryRecentAppsError();

  @DefaultMessage("Server error: could not get most downloaded apps from gallery")
  @Description("Error message reported when can't get most downloaded apps server.")
  String galleryDownloadedAppsError();

  @DefaultMessage("Server error: could not get most liked apps from gallery")
  @Description("Error message reported when can't get most liked apps server.")
  String galleryLikedAppsError();

  @DefaultMessage("Server error: gallery deletion error")
  @Description("Error message reported when the gallery delete breaks")
  String galleryDeleteError();

  @DefaultMessage("Server error: gallery error when setting project gallery id")
  @Description("Error message reported when the gallery trying to set project gallery id")
  String gallerySetProjectIdError();

  @DefaultMessage("Server error: could not upload project. Please try again later!")
  @Description("Error message reported when a project couldn't be uploaded to the server.")
  String projectUploadError();

  @DefaultMessage("Apps with extensions cannot be uploaded to the Gallery")
  @Description("Error to report when an app with an extension is attempted to be added to the Gallery")
  String galleryNoExtensionsPlease();

  @DefaultMessage("The selected project is not a project source file!\n" +
      "Project source files are aia files.")
  @Description("Error message reported when the file selected for upload is not a project archive.")
  String notProjectArchiveError();

  // Used in wizards/Wizard.java

  @DefaultMessage("Back")
  @Description("Text on 'Back' button to go back to the previous page of the wizard.")
  String backButton();

  @DefaultMessage("Next")
  @Description("Text on 'Next' button to proceed to the next page of the wizard.")
  String nextButton();

  // Used in wizards/youngandroid/NewYoungAndroidProjectWizard.java

  @DefaultMessage("Create new App Inventor project")
  @Description("Caption for the wizard to create a new Young Android project")
  String newYoungAndroidProjectWizardCaption();

  @DefaultMessage("Project name:")
  @Description("Label for the project name input text box")
  String projectNameLabel();

  // Used in youngandroid/TextValidators.java

  @DefaultMessage("Project names must start with a letter and can contain only letters, " +
      "numbers, and underscores")
  @Description("Error message when project name does not start with a letter or contains a " +
      "character that is not a letter, number, or underscore.")
  String malformedProjectNameError();

  @DefaultMessage("{0} already exists. You cannot create another project with the same name.")
  @Description("Error shown when a new project name would be the same as an existing one")
  String duplicateProjectNameError(String projectName);

  @DefaultMessage("{0} already exists in your Trash. You cannot create another project with the same name.")
  @Description("Error shown when a new project name would be the same as an existing one")
  String duplicateTrashProjectNameError(String projectName);

  @DefaultMessage("Project names cannot contain spaces")
  @Description("Error shown when user types space into project name.")
  String whitespaceProjectNameError();

  @DefaultMessage("Project names must begin with a letter")
  @Description("Error shown when user does not type letter as first character in project name.")
  String firstCharProjectNameError();

  @DefaultMessage("Invalid character. Project names can only contain letters, numbers, and underscores")
  @Description("Error shown when user types invalid character into project name.")
  String invalidCharProjectNameError();

  @DefaultMessage("The first character of the folder name must be a letter")
  @Description("Error shown when user does not type letter as first character in folder name.")
  String firstCharFolderNameError();

  @DefaultMessage("Invalid character. Folder names can only contain letters, numbers, and underscores")
  @Description("Error shown when user types invalid character into folder name.")
  String invalidCharFolderNameError();

  @DefaultMessage("Folder names cannot contain spaces")
  @Description("Error shown when user types space into folder name.")
  String whitespaceFolderNameError();

  // Used in youngandroid/YoungAndroidFormUpgrader.java

  @DefaultMessage("This project was created with an older version of the App Inventor " +
      "system and was upgraded.\n{0}")
  @Description("Alert message displayed when a project is upgraded")
  String projectWasUpgraded(String details);

  @DefaultMessage("A problem occurred while loading this project. {0}")
  @Description("Alert message displayed when upgrade fails")
  String unexpectedProblem(String details);

  @DefaultMessage("This project was saved with a newer version of the App Inventor system. We " +
      "will attempt to load the project, but there may be compatibility issues.")
  @Description("Alert message displayed when project is newer than system")
  String newerVersionProject();

  @DefaultMessage("This project was saved with an early pre-release version of the App Inventor " +
      "system. We will attempt to load the project, but there may be compatibility issues.")
  @Description("Alert message displayed when upgrading a project without version numbers")
  String veryOldProject();

  @DefaultMessage("The Logger component named {0} was changed to a Notifier component.\n")
  @Description("Message providing details about a project upgrade involving a Logger component")
  String upgradeDetailLoggerReplacedWithNotifier(String name);

  @DefaultMessage("The YandexTranslate component named {0} was changed to use the Translator component.\n")
  @Description("Message providing details about the renaming of YandexTranslate to Translator")
  String yandexTranslateReplacedwithTranslator(String name);

  @DefaultMessage("Unable to load project with {0} version {1} (maximum known version is {2}).")
  @Description("Exception message used when a project contains a newer version component than " +
      "the version known by the system")
  String newerVersionComponentException(String componentType, int srcCompVersion,
      int sysCompVersion);

  @DefaultMessage("No upgrade strategy exists for {0} from version {1} to {2}.")
  @Description("Exception message used when a component was not upgraded")
  String noUpgradeStrategyException(String componentType, int srcCompVersion, int sysCompVersion);

  // Used in client/editor/simple/components/MockHVarrangement.java

  @DefaultMessage("System error: bad alignment property editor for horizontal or vertical arrangement.")
  @Description("System error message for a bad alignment property editor")
  String badAlignmentPropertyEditorForArrangement();

  // Used in
  // editor/youngandroid/properties/YoungAndroidTextReceivingPropertyEditor.java

  @DefaultMessage("Off")
  @Description("Text Messages are not received at any time.")
  String textReceivingChoiceOff();

  @DefaultMessage("Foreground")
  @Description("Text Messages are received only when the App is in the foreground.")
  String textReceivingChoiceForeground();

  @DefaultMessage("Always")
  @Description("Text messages are always received, and a notification is shown if the App is in the background.")
  String textReceivingChoiceAlways();

  @DefaultMessage("0 Starting Up")
  @Description("")
  String startingConnectionDialog();

  @DefaultMessage("Downloading {0} from the App Inventor server...")
  @Description("Message to display when an asset is being downloaded from the server")
  String loadingAsset(String assetPath);

  @DefaultMessage("Sending {0} to companion...")
  @Description("Message to display when sending an asset to the companion")
  String sendingAssetToCompanion(String assetPath);

  // This error message is displayed as HTML
  @DefaultMessage("App Inventor is unable to compile this project.  " +
      "<br /> The compiler error output was <br /> {0}.")
  @Description("Compilation error, with error message.")
  String unableToCompile(String errorMesssage);

  @DefaultMessage("The APK file will be saved in the download folder.")
  @Description("")
  String apkSavedToComputer();

  @DefaultMessage("The APK file will be installed in the phone.")
  @Description("")
  String apkInstalledToPhone();

  @DefaultMessage("Waiting for the barcode.")
  @Description("")
  String waitingForBarcode();

  @DefaultMessage("Preparing application icon")
  @Description("")
  String preparingApplicationIcon();

  @DefaultMessage("Determining permissions")
  @Description("")
  String determiningPermissions();

  @DefaultMessage("Generating application information")
  @Description("")
  String generatingApplicationInformation();

  @DefaultMessage("Compiling part 1")
  @Description("")
  String compilingPart1();

  @DefaultMessage("Compiling part 2 (please wait)")
  @Description("")
  String compilingPart2();

  @DefaultMessage("Preparing final package")
  @Description("")
  String preparingFinalPackage();

  @DefaultMessage("Building APK")
  @Description("")
  String buildingApk();

  @DefaultMessage("Math")
  @Description("Label on built-in-Math-blocks branch of block selector tree")
  String builtinMathLabel();

  @DefaultMessage("Extension")
  @Description("")
  String extensionComponentPallette();

  @DefaultMessage("For internal use only")
  @Description("")
  String internalUseComponentPallette();

  @DefaultMessage("Uninitialized")
  @Description("")
  String uninitializedComponentPallette();

  @DefaultMessage("InvitedInstances")
  @Description("")
  String InvitedInstancesProperties();

  @DefaultMessage("IsAccepting")
  @Description("")
  String IsAcceptingProperties();

  @DefaultMessage("IsConnected")
  @Description("")
  String IsConnectedProperties();

  @DefaultMessage("IsPlaying")
  @Description("")
  String IsPlayingProperties();

  @DefaultMessage("JoinedInstances")
  @Description("")
  String JoinedInstancesProperties();

  @DefaultMessage("Latitude")
  @Description("")
  String LatitudeProperties();

  @DefaultMessage("Leader")
  @Description("")
  String LeaderProperties();

  @DefaultMessage("Longitude")
  @Description("")
  String LongitudeProperties();

  @DefaultMessage("Magnitude")
  @Description("")
  String MagnitudeProperties();

  @DefaultMessage("Mentions")
  @Description("")
  String MentionsProperties();

  @DefaultMessage("PasswordVisible")
  @Description("")
  String PasswordVisibleProperties();

  @DefaultMessage("ProviderLocked")
  @Description("")
  String ProviderLockedProperties();

  @DefaultMessage("ProviderName")
  @Description("")
  String ProviderNameProperties();

  @DefaultMessage("PublicInstances")
  @Description("")
  String PublicInstancesProperties();

  @DefaultMessage("PlayOnlyInForeground")
  @Description("")
  String PlayOnlyInForegroundProperties();

  @DefaultMessage("Players")
  @Description("")
  String PlayersProperties();

  @DefaultMessage("RequestHeaders")
  @Description("")
  String RequestHeadersProperties();

  @DefaultMessage("Result")
  @Description("")
  String ResultProperties();

  @DefaultMessage("UseExternalScanner")
  @Description("")
  String UseExternalScannerProperties();

  @DefaultMessage("ResultType")
  @Description("")
  String ResultTypeProperties();

  @DefaultMessage("ResultUri")
  @Description("")
  String ResultUriProperties();

  @DefaultMessage("Roll")
  @Description("")
  String RollProperties();

  @DefaultMessage("Scaling")
  @Description("A property for scaling images")
  String ScalingProperties();

  @DefaultMessage("SearchResults")
  @Description("")
  String SearchResultsProperties();

  @DefaultMessage("ServiceUrl")
  @Description("")
  String ServiceUrlProperties();

  @DefaultMessage("SelectionIndex")
  @Description("")
  String SelectionIndexProperties();

  @DefaultMessage("uri")
  @Description("")
  String uriParams();

  @DefaultMessage("UserChoice")
  @Description("")
  String UserChoiceProperties();

  @DefaultMessage("UserEmailAddress")
  @Description("")
  String UserEmailAddressProperties();

  @DefaultMessage("UserId")
  @Description("")
  String UserIdProperties();

  @DefaultMessage("Username")
  @Description("")
  String UsernameProperties();

  @DefaultMessage("XAccel")
  @Description("")
  String XAccelProperties();

  @DefaultMessage("XAngularVelocity")
  @Description("")
  String XAngularVelocityProperties();

  @DefaultMessage("YAccel")
  @Description("")
  String YAccelProperties();

  @DefaultMessage("YAngularVelocity")
  @Description("")
  String YAngularVelocityProperties();

  @DefaultMessage("ZAccel")
  @Description("")
  String ZAccelProperties();

  @DefaultMessage("ZAngularVelocity")
  @Description("")
  String ZAngularVelocityProperties();

  @DefaultMessage("Width")
  @Description("")
  String WidthProperties();

  @DefaultMessage("WidthPercent")
  @Description("")
  String WidthPercentProperties();

  @DefaultMessage("WebViewString")
  @Description("")
  String WebViewStringProperties();

  @DefaultMessage("WebViewStringChange")
  @Description("")
  String WebViewStringChangeEvents();

  @DefaultMessage("EnableSpeedRegulation")
  @Description("")
  String EnableSpeedRegulationProperties();

  @DefaultMessage("Mode")
  @Description("")
  String ModeProperties();

  @DefaultMessage("MotorPorts")
  @Description("")
  String MotorPortsProperties();

  @DefaultMessage("ReverseDirection")
  @Description("")
  String ReverseDirectionProperties();

  @DefaultMessage("SensorValueChangedEventEnabled")
  @Description("")
  String SensorValueChangedEventEnabledProperties();

  @DefaultMessage("TachoCountChangedEventEnabled")
  @Description("")
  String TachoCountChangedEventEnabledProperties();

  @DefaultMessage("Unit")
  @Description("")
  String UnitProperties();

  //Params
  @DefaultMessage("xAccel")
  @Description("")
  String xAccelParams();

  @DefaultMessage("yAccel")
  @Description("")
  String yAccelParams();

  @DefaultMessage("zAccel")
  @Description("")
  String zAccelParams();

  @DefaultMessage("result")
  @Description("")
  String resultParams();

  @DefaultMessage("tableId")
  @Description("")
  String tableIdParams();

  @DefaultMessage("columns")
  @Description("")
  String columnsParams();

  @DefaultMessage("conditions")
  @Description("")
  String conditionsParams();

  @DefaultMessage("values")
  @Description("")
  String valuesParams();

  @DefaultMessage("other")
  @Description("")
  String otherParams();

  @DefaultMessage("component")
  @Description("")
  String componentParams();

  @DefaultMessage("notAlreadyHandled")
  @Description("Name of the event parameter that indicates whether an event has already been handled or not.")
  String notAlreadyHandledParams();


  @DefaultMessage("startX")
  @Description("")
  String startXParams();

  @DefaultMessage("startY")
  @Description("")
  String startYParams();

  @DefaultMessage("prevX")
  @Description("")
  String prevXParams();

  @DefaultMessage("prevY")
  @Description("")
  String prevYParams();

  @DefaultMessage("currentX")
  @Description("")
  String currentXParams();

  @DefaultMessage("currentY")
  @Description("")
  String currentYParams();

  @DefaultMessage("edge")
  @Description("")
  String edgeParams();

  @DefaultMessage("speed")
  @Description("")
  String speedParams();

  @DefaultMessage("heading")
  @Description("")
  String headingParams();

  @DefaultMessage("xvel")
  @Description("")
  String xvelParams();

  @DefaultMessage("yvel")
  @Description("")
  String yvelParams();

  @DefaultMessage("target")
  @Description("")
  String targetParams();

  @DefaultMessage("address")
  @Description("")
  String addressParams();

  @DefaultMessage("uuid")
  @Description("")
  String uuidParams();

  @DefaultMessage("numberOfBytes")
  @Description("")
  String numberOfBytesParams();

  @DefaultMessage("number")
  @Description("")
  String numberParams();

  @DefaultMessage("list")
  @Description("")
  String listParams();

  @DefaultMessage("text")
  @Description("")
  String textParams();

  @DefaultMessage("clip")
  @Description("")
  String clipParams();

  @DefaultMessage("image")
  @Description("")
  String imageParams();

  @DefaultMessage("draggedSprite")
  @Description("")
  String draggedSpriteParams();

  @DefaultMessage("draggedAnySprite")
  @Description("")
  String draggedAnySpriteParams();

  @DefaultMessage("flungSprite")
  @Description("")
  String flungSpriteParams();

  @DefaultMessage("touchedSprite")
  @Description("")
  String touchedSpriteParams();

  @DefaultMessage("touchedAnySprite")
  @Description("")
  String touchedAnySpriteParams();

  @DefaultMessage("x")
  @Description("")
  String xParams();

  @DefaultMessage("y")
  @Description("")
  String yParams();

  @DefaultMessage("centerX")
  @Description("")
  String centerXParams();

  @DefaultMessage("centerY")
  @Description("")
  String centerYParams();

  @DefaultMessage("r")
  @Description("")
  String rParams();

  @DefaultMessage("radius")
  @Description("")
  String radiusParams();

  @DefaultMessage("x1")
  @Description("")
  String x1Params();

  @DefaultMessage("x2")
  @Description("")
  String x2Params();

  @DefaultMessage("y1")
  @Description("")
  String y1Params();

  @DefaultMessage("y2")
  @Description("")
  String y2Params();

  @DefaultMessage("angle")
  @Description("")
  String angleParams();

  @DefaultMessage("fileName")
  @Description("")
  String fileNameParams();

  @DefaultMessage("color")
  @Description("")
  String colorParams();

  @DefaultMessage("year")
  @Description("")
  String yearParams();

  @DefaultMessage("month")
  @Description("")
  String monthParams();

  @DefaultMessage("day")
  @Description("")
  String dayParams();

  @DefaultMessage("hour")
  @Description("")
  String hourParams();

  @DefaultMessage("minute")
  @Description("")
  String minuteParams();

  @DefaultMessage("instant")
  @Description("")
  String instantParams();

  @DefaultMessage("days")
  @Description("")
  String daysParams();

  @DefaultMessage("hours")
  @Description("")
  String hoursParams();

  @DefaultMessage("minutes")
  @Description("")
  String minutesParams();

  @DefaultMessage("months")
  @Description("")
  String monthsParams();

  @DefaultMessage("seconds")
  @Description("")
  String secondsParams();

  @DefaultMessage("weeks")
  @Description("")
  String weeksParams();

  @DefaultMessage("quantity")
  @Description("")
  String quantityParams();

  @DefaultMessage("duration")
  @Description("")
  String durationParams();

  @DefaultMessage("pattern")
  @Description("")
  String patternParams();

  @DefaultMessage("years")
  @Description("")
  String yearsParams();

  @DefaultMessage("InstantInTime")
  @Description("")
  String InstantInTimeParams();

  @DefaultMessage("from")
  @Description("")
  String fromParams();

  @DefaultMessage("millis")
  @Description("")
  String millisParams();

  @DefaultMessage("second")
  @Description("")
  String secondParams();

  @DefaultMessage("functionName")
  @Description("")
  String functionNameParams();

  @DefaultMessage("permissionName")
  @Description("The name of the parameter that is used to report the name of a needed permission.")
  String permissionNameParams();

  @DefaultMessage("errorNumber")
  @Description("")
  String errorNumberParams();

  @DefaultMessage("message")
  @Description("")
  String messageParams();

  @DefaultMessage("mediafileParams")
  @Description("")
  String mediafileParams();

  @DefaultMessage("otherScreenName")
  @Description("")
  String otherScreenNameParams();

  @DefaultMessage("animType")
  @Description("")
  String animTypeParams();

  @DefaultMessage("sender")
  @Description("")
  String senderParams();

  @DefaultMessage("contents")
  @Description("")
  String contentsParams();

  @DefaultMessage("instanceId")
  @Description("")
  String instanceIdParams();

  @DefaultMessage("playerId")
  @Description("")
  String playerIdParams();

  @DefaultMessage("command")
  @Description("")
  String commandParams();

  @DefaultMessage("arguments")
  @Description("")
  String argumentsParams();

  @DefaultMessage("response")
  @Description("")
  String responseParams();

  @DefaultMessage("emailAddress")
  @Description("")
  String emailAddressParams();

  @DefaultMessage("type")
  @Description("")
  String typeParams();

  @DefaultMessage("count")
  @Description("")
  String countParams();

  @DefaultMessage("makePublic")
  @Description("")
  String makePublicParams();

  @DefaultMessage("recipients")
  @Description("")
  String recipientsParams();

  @DefaultMessage("playerEmail")
  @Description("")
  String playerEmailParams();

  @DefaultMessage("latitude")
  @Description("")
  String latitudeParams();

  @DefaultMessage("longitude")
  @Description("")
  String longitudeParams();

  @DefaultMessage("altitude")
  @Description("")
  String altitudeParams();

  @DefaultMessage("provider")
  @Description("")
  String providerParams();

  @DefaultMessage("status")
  @Description("")
  String statusParams();

  @DefaultMessage("locationName")
  @Description("")
  String locationNameParams();

  @DefaultMessage("choice")
  @Description("")
  String choiceParams();

  @DefaultMessage("notice")
  @Description("")
  String noticeParams();

  @DefaultMessage("title")
  @Description("")
  String titleParams();

  @DefaultMessage("buttonText")
  @Description("")
  String buttonTextParams();

  @DefaultMessage("cancelable")
  @Description("")
  String cancelableParams();

  @DefaultMessage("button1Text")
  @Description("")
  String button1TextParams();

  @DefaultMessage("button2Text")
  @Description("")
  String button2TextParams();

  @DefaultMessage("source")
  @Description("")
  String sourceParams();

  @DefaultMessage("destination")
  @Description("")
  String destinationParams();

  @DefaultMessage("sensorPortLetter")
  @Description("")
  String sensorPortLetterParams();

  @DefaultMessage("rxDataLength")
  @Description("")
  String rxDataLengthParams();

  @DefaultMessage("wildcard")
  @Description("")
  String wildcardParams();

  @DefaultMessage("motorPortLetter")
  @Description("")
  String motorPortLetterParams();

  @DefaultMessage("mailbox")
  @Description("")
  String mailboxParams();

  @DefaultMessage("durationMs")
  @Description("")
  String durationMsParams();

  @DefaultMessage("relative")
  @Description("")
  String relativeParams();

  @DefaultMessage("sensorType")
  @Description("")
  String sensorTypeParams();

  @DefaultMessage("sensorMode")
  @Description("")
  String sensorModeParams();

  @DefaultMessage("power")
  @Description("")
  String powerParams();

  @DefaultMessage("mode")
  @Description("")
  String modeParams();

  @DefaultMessage("regulationMode")
  @Description("")
  String regulationModeParams();

  @DefaultMessage("turnRatio")
  @Description("")
  String turnRatioParams();

  @DefaultMessage("runState")
  @Description("")
  String runStateParams();

  @DefaultMessage("tachoLimit")
  @Description("")
  String tachoLimitParams();

  @DefaultMessage("programName")
  @Description("")
  String programNameParams();

  @DefaultMessage("distance")
  @Description("")
  String distanceParams();

  @DefaultMessage("azimuth")
  @Description("")
  String azimuthParams();

  @DefaultMessage("pitch")
  @Description("")
  String pitchParams();

  @DefaultMessage("roll")
  @Description("")
  String rollParams();

  @DefaultMessage("simpleSteps")
  @Description("")
  String simpleStepsParams();

  @DefaultMessage("walkSteps")
  @Description("")
  String walkStepsParams();

  @DefaultMessage("seed")
  @Description("")
  String seedParams();

  @DefaultMessage("rendezvousServer")
  @Description("")
  String rendezvousServerParams();

  @DefaultMessage("millisecs")
  @Description("")
  String millisecsParams();

  @DefaultMessage("sound")
  @Description("")
  String soundParams();

  @DefaultMessage("messageText")
  @Description("")
  String messageTextParams();

  @DefaultMessage("tag")
  @Description("")
  String tagParams();

  @DefaultMessage("value")
  @Description("")
  String valueParams();

  @DefaultMessage("valueToStore")
  @Description("")
  String valueToStoreParams();

  @DefaultMessage("valueToAdd")
  @Description("")
  String valueToAddParams();

  @DefaultMessage("tagFromWebDB")
  @Description("")
  String tagFromWebDBParams();

  @DefaultMessage("valueFromWebDB")
  @Description("")
  String valueFromWebDBParams();

  @DefaultMessage("followers2")
  @Description("")
  String followers2Params();

  @DefaultMessage("timeline")
  @Description("")
  String timelineParams();

  @DefaultMessage("mentions")
  @Description("")
  String mentionsParams();

  @DefaultMessage("searchResults")
  @Description("")
  String searchResultsParams();

  @DefaultMessage("user")
  @Description("")
  String userParams();

  @DefaultMessage("url")
  @Description("")
  String urlParams();

  @DefaultMessage("responseCode")
  @Description("")
  String responseCodeParams();

  @DefaultMessage("responseType")
  @Description("")
  String responseTypeParams();

  @DefaultMessage("responseContent")
  @Description("")
  String responseContentParams();

  @DefaultMessage("htmlText")
  @Description("")
  String htmlTextParams();

  @DefaultMessage("jsonObject")
  @Description("")
  String jsonObjectParams();

  @DefaultMessage("jsonText")
  @Description("")
  String jsonTextParams();

  @DefaultMessage("path")
  @Description("")
  String pathParams();

  @DefaultMessage("encoding")
  @Description("")
  String encodingParams();

  @DefaultMessage("xmlText")
  @Description("")
  String xmlTextParams();

  @DefaultMessage("name")
  @Description("")
  String nameParams();

  @DefaultMessage("serviceName")
  @Description("")
  String serviceNameParams();

  @DefaultMessage("milliseconds")
  @Description("")
  String millisecondsParams();

  @DefaultMessage("messages")
  @Description("")
  String messagesParams();

  @DefaultMessage("start")
  @Description("")
  String startParams();

  @DefaultMessage("end")
  @Description("")
  String endParams();

  @DefaultMessage("frequencyHz")
  @Description("")
  String frequencyHzParams();

  @DefaultMessage("secure")
  @Description("")
  String secureParams();

  @DefaultMessage("file")
  @Description("")
  String fileParams();

  @DefaultMessage("thumbPosition")
  @Description("")
  String thumbPositionParams();

  @DefaultMessage("selection")
  @Description("")
  String selectionParams();

  @DefaultMessage("valueIfTagNotThere")
  @Description("")
  String valueIfTagNotThereParams();

  @DefaultMessage("query")
  @Description("")
  String queryParams();

  @DefaultMessage("imagePath")
  @Description("")
  String imagePathParams();

  @DefaultMessage("ms")
  @Description("")
  String msParams();

  @DefaultMessage("translation")
  @Description("")
  String translationParams();

  @DefaultMessage("languageToTranslateTo")
  @Description("")
  String languageToTranslateToParams();

  @DefaultMessage("textToTranslate")
  @Description("")
  String textToTranslateParams();

  @DefaultMessage("xAngularVelocity")
  @Description("")
  String xAngularVelocityParams();

  @DefaultMessage("yAngularVelocity")
  @Description("")
  String yAngularVelocityParams();

  @DefaultMessage("zAngularVelocity")
  @Description("")
  String zAngularVelocityParams();

  @DefaultMessage("timestamp")
  @Description("")
  String timestampParams();

  @DefaultMessage("colorCode")
  @Description("")
  String colorCodeParams();

  @DefaultMessage("colorName")
  @Description("")
  String colorNameParams();

  @DefaultMessage("fill")
  @Description("")
  String fillParams();

  @DefaultMessage("frequency")
  @Description("")
  String frequencyParams();

  @DefaultMessage("height")
  @Description("")
  String heightParams();

  @DefaultMessage("no")
  @Description("")
  String noParams();

  @DefaultMessage("sensorValue")
  @Description("")
  String sensorValueParams();

  @DefaultMessage("tachoCount")
  @Description("")
  String tachoCountParams();

  @DefaultMessage("tachoCounts")
  @Description("")
  String tachoCountsParams();

  @DefaultMessage("useBrake")
  @Description("")
  String useBrakeParams();

  @DefaultMessage("volume")
  @Description("")
  String volumeParams();

  @DefaultMessage("width")
  @Description("")
  String widthParams();

  //Events
  @DefaultMessage("AccelerationChanged")
  @Description("")
  String AccelerationChangedEvents();

  @DefaultMessage("ActivityCanceled")
  @Description("")
  String ActivityCanceledEvents();

  @DefaultMessage("AfterActivity")
  @Description("")
  String AfterActivityEvents();

  @DefaultMessage("CollidedWith")
  @Description("")
  String CollidedWithEvents();

  @DefaultMessage("Dragged")
  @Description("")
  String DraggedEvents();

  @DefaultMessage("EdgeReached")
  @Description("")
  String EdgeReachedEvents();

  @DefaultMessage("Flung")
  @Description("")
  String FlungEvents();

  @DefaultMessage("NoLongerCollidingWith")
  @Description("")
  String NoLongerCollidingWithEvents();

  @DefaultMessage("TouchDown")
  @Description("")
  String TouchDownEvents();

  @DefaultMessage("TouchUp")
  @Description("")
  String TouchUpEvents();

  @DefaultMessage("Touched")
  @Description("")
  String TouchedEvents();

  @DefaultMessage("AfterScan")
  @Description("")
  String AfterScanEvents();

  @DefaultMessage("ConnectionAccepted")
  @Description("")
  String ConnectionAcceptedEvents();

  @DefaultMessage("Click")
  @Description("")
  String ClickEvents();

  @DefaultMessage("GotFocus")
  @Description("")
  String GotFocusEvents();

  @DefaultMessage("LongClick")
  @Description("")
  String LongClickEvents();

  @DefaultMessage("RequestFocus")
  @Description("")
  String RequestFocusMethods();

  @DefaultMessage("LostFocus")
  @Description("")
  String LostFocusEvents();

  @DefaultMessage("ViewContact")
  @Description("")
  String ViewContactMethods();

  @DefaultMessage("AfterRecording")
  @Description("")
  String AfterRecordingEvents();

  @DefaultMessage("AfterPicture")
  @Description("")
  String AfterPictureEvents();

  @DefaultMessage("Changed")
  @Description("")
  String ChangedEvents();

  @DefaultMessage("Timer")
  @Description("")
  String TimerEvents();

  @DefaultMessage("AfterPicking")
  @Description("")
  String AfterPickingEvents();

  @DefaultMessage("BeforePicking")
  @Description("")
  String BeforePickingEvents();

  @DefaultMessage("BackPressed")
  @Description("")
  String BackPressedEvents();

  @DefaultMessage("ErrorOccurred")
  @Description("")
  String ErrorOccurredEvents();

  @DefaultMessage("Initialize")
  @Description("")
  String InitializeEvents();

  @DefaultMessage("OtherScreenClosed")
  @Description("")
  String OtherScreenClosedEvents();

  @DefaultMessage("PermissionDenied")
  @Description("The name of the event handler for when the app is denied a dangerous permission by the user.")
  String PermissionDeniedEvents();

  @DefaultMessage("PermissionGranted")
  @Description("The name of the event handler for when the app is granted a dangerous permission by the user.")
  String PermissionGrantedEvents();

  @DefaultMessage("ScreenOrientationChanged")
  @Description("")
  String ScreenOrientationChangedEvents();

  @DefaultMessage("GotResult")
  @Description("")
  String GotResultEvents();

  @DefaultMessage("FunctionCompleted")
  @Description("")
  String FunctionCompletedEvents();

  @DefaultMessage("GotMessage")
  @Description("")
  String GotMessageEvents();

  @DefaultMessage("Info")
  @Description("")
  String InfoEvents();

  @DefaultMessage("InstanceIdChanged")
  @Description("")
  String InstanceIdChangedEvents();

  @DefaultMessage("Invited")
  @Description("")
  String InvitedEvents();

  @DefaultMessage("NewInstanceMade")
  @Description("")
  String NewInstanceMadeEvents();

  @DefaultMessage("NewLeader")
  @Description("")
  String NewLeaderEvents();

  @DefaultMessage("PlayerJoined")
  @Description("")
  String PlayerJoinedEvents();

  @DefaultMessage("PlayerLeft")
  @Description("")
  String PlayerLeftEvents();

  @DefaultMessage("ServerCommandFailure")
  @Description("")
  String ServerCommandFailureEvents();

  @DefaultMessage("ServerCommandSuccess")
  @Description("")
  String ServerCommandSuccessEvents();

  @DefaultMessage("UserEmailAddressSet")
  @Description("")
  String UserEmailAddressSetEvents();

  @DefaultMessage("WebServiceError")
  @Description("")
  String WebServiceErrorEvents();

  @DefaultMessage("FirebaseError")
  @Description("")
  String FirebaseErrorEvents();

  @DefaultMessage("LocationChanged")
  @Description("")
  String LocationChangedEvents();

  @DefaultMessage("StatusChanged")
  @Description("")
  String StatusChangedEvents();

  @DefaultMessage("AfterChoosing")
  @Description("")
  String AfterChoosingEvents();

  @DefaultMessage("AfterTextInput")
  @Description("")
  String AfterTextInputEvents();

  @DefaultMessage("ChoosingCanceled")
  @Description("")
  String ChoosingCanceledEvents();

  @DefaultMessage("TextInputCanceled")
  @Description("")
  String TextInputCanceledEvents();

  @DefaultMessage("AboveRange")
  @Description("")
  String AboveRangeEvents();

  @DefaultMessage("BelowRange")
  @Description("")
  String BelowRangeEvents();

  @DefaultMessage("ColorChanged")
  @Description("")
  String ColorChangedEvents();

  @DefaultMessage("WithinRange")
  @Description("")
  String WithinRangeEvents();

  @DefaultMessage("Pressed")
  @Description("")
  String PressedEvents();

  @DefaultMessage("Released")
  @Description("")
  String ReleasedEvents();

  @DefaultMessage("OrientationChanged")
  @Description("")
  String OrientationChangedEvents();

  @DefaultMessage("CalibrationFailed")
  @Description("")
  String CalibrationFailedEvents();

  @DefaultMessage("GPSAvailable")
  @Description("")
  String GPSAvailableEvents();

  @DefaultMessage("GPSLost")
  @Description("")
  String GPSLostEvents();

  @DefaultMessage("SimpleStep")
  @Description("")
  String SimpleStepEvents();

  @DefaultMessage("StartedMoving")
  @Description("")
  String StartedMovingEvents();

  @DefaultMessage("StoppedMoving")
  @Description("")
  String StoppedMovingEvents();

  @DefaultMessage("WalkStep")
  @Description("")
  String WalkStepEvents();

  @DefaultMessage("Completed")
  @Description("")
  String CompletedEvents();

  @DefaultMessage("AfterSoundRecorded")
  @Description("")
  String AfterSoundRecordedEvents();

  @DefaultMessage("StartedRecording")
  @Description("")
  String StartedRecordingEvents();

  @DefaultMessage("StoppedRecording")
  @Description("")
  String StoppedRecordingEvents();

  @DefaultMessage("AfterGettingText")
  @Description("")
  String AfterGettingTextEvents();

  @DefaultMessage("BeforeGettingText")
  @Description("")
  String BeforeGettingTextEvents();

  @DefaultMessage("AfterSpeaking")
  @Description("")
  String AfterSpeakingEvents();

  @DefaultMessage("BeforeSpeaking")
  @Description("")
  String BeforeSpeakingEvents();

  @DefaultMessage("MessageReceived")
  @Description("")
  String MessageReceivedEvents();

  @DefaultMessage("SendMessage")
  @Description("")
  String SendMessageEvents();

  @DefaultMessage("GotValue")
  @Description("")
  String GotValueEvents();

  @DefaultMessage("TagList")
  @Description("")
  String TagListEvents();

  @DefaultMessage("ValueStored")
  @Description("")
  String ValueStoredEvents();

  @DefaultMessage("DataChanged")
  @Description("")
  String DataChangedEvents();

  @DefaultMessage("DirectMessagesReceived")
  @Description("")
  String DirectMessagesReceivedEvents();

  @DefaultMessage("FollowersReceived")
  @Description("")
  String FollowersReceivedEvents();

  @DefaultMessage("FriendTimelineReceived")
  @Description("")
  String FriendTimelineReceivedEvents();

  @DefaultMessage("IsAuthorized")
  @Description("")
  String IsAuthorizedEvents();

  @DefaultMessage("MentionsReceived")
  @Description("")
  String MentionsReceivedEvents();

  @DefaultMessage("SearchSuccessful")
  @Description("")
  String SearchSuccessfulEvents();

  @DefaultMessage("GotBallot")
  @Description("")
  String GotBallotEvents();

  @DefaultMessage("GotBallotConfirmation")
  @Description("")
  String GotBallotConfirmationEvents();

  @DefaultMessage("NoOpenPoll")
  @Description("")
  String NoOpenPollEvents();

  @DefaultMessage("GotFile")
  @Description("")
  String GotFileEvents();

  @DefaultMessage("GotText")
  @Description("")
  String GotTextEvents();

  @DefaultMessage("AfterFileSaved")
  @Description("")
  String AfterFileSavedEvents();

  @DefaultMessage("AfterDateSet")
  @Description("")
  String AfterDateSetEvents();

  @DefaultMessage("TagRead")
  @Description("")
  String TagReadEvents();

  @DefaultMessage("TagWritten")
  @Description("")
  String TagWrittenEvents();

  @DefaultMessage("PositionChanged")
  @Description("")
  String PositionChangedEvents();

  @DefaultMessage("AfterSelecting")
  @Description("")
  String AfterSelectingEvents();

  @DefaultMessage("AfterTimeSet")
  @Description("")
  String AfterTimeSetEvents();

  @DefaultMessage("GotTranslation")
  @Description("")
  String GotTranslationEvents();

  @DefaultMessage("Shaking")
  @Description("")
  String ShakingEvents();

  @DefaultMessage("GyroscopeChanged")
  @Description("")
  String GyroscopeChangedEvents();

  @DefaultMessage("TachoCountChanged")
  @Description("")
  String TachoCountChangedEvents();

  @DefaultMessage("SensorValueChanged")
  @Description("")
  String SensorValueChangedEvents();

  //Methods

  @DefaultMessage("DrawShape")
  @Description("")
  String DrawShapeMethods();

  @DefaultMessage("DrawArc")
  @Description("")
  String DrawArcMethods();

  @DefaultMessage("ResolveActivity")
  @Description("")
  String ResolveActivityMethods();

  @DefaultMessage("StartActivity")
  @Description("")
  String StartActivityMethods();

  @DefaultMessage("Connect")
  @Description("")
  String ConnectMethods();

  @DefaultMessage("ConnectWithUUID")
  @Description("")
  String ConnectWithUUIDMethods();

  @DefaultMessage("Disconnect")
  @Description("")
  String DisconnectMethods();

  @DefaultMessage("IsDevicePaired")
  @Description("")
  String IsDevicePairedMethods();

  @DefaultMessage("ReceiveSigned1ByteNumber")
  @Description("")
  String ReceiveSigned1ByteNumberMethods();

  @DefaultMessage("ReceiveSigned2ByteNumber")
  @Description("")
  String ReceiveSigned2ByteNumberMethods();

  @DefaultMessage("ReceiveSigned4ByteNumber")
  @Description("")
  String ReceiveSigned4ByteNumberMethods();

  @DefaultMessage("ReceiveSignedBytes")
  @Description("")
  String ReceiveSignedBytesMethods();

  @DefaultMessage("ReceiveText")
  @Description("")
  String ReceiveTextMethods();

  @DefaultMessage("ReceiveUnsigned1ByteNumber")
  @Description("")
  String ReceiveUnsigned1ByteNumberMethods();

  @DefaultMessage("ReceiveUnsigned2ByteNumber")
  @Description("")
  String ReceiveUnsigned2ByteNumberMethods();

  @DefaultMessage("ReceiveUnsigned4ByteNumber")
  @Description("")
  String ReceiveUnsigned4ByteNumberMethods();

  @DefaultMessage("ReceiveUnsignedBytes")
  @Description("")
  String ReceiveUnsignedBytesMethods();

  @DefaultMessage("Send1ByteNumber")
  @Description("")
  String Send1ByteNumberMethods();

  @DefaultMessage("Send2ByteNumber")
  @Description("")
  String Send2ByteNumberMethods();

  @DefaultMessage("Send4ByteNumber")
  @Description("")
  String Send4ByteNumberMethods();

  @DefaultMessage("SendBytes")
  @Description("")
  String SendBytesMethods();

  @DefaultMessage("SendText")
  @Description("")
  String SendTextMethods();

  @DefaultMessage("AcceptConnection")
  @Description("")
  String AcceptConnectionMethods();

  @DefaultMessage("AcceptConnectionWithUUID")
  @Description("")
  String AcceptConnectionWithUUIDMethods();

  @DefaultMessage("BytesAvailableToReceive")
  @Description("")
  String BytesAvailableToReceiveMethods();

  @DefaultMessage("StopAccepting")
  @Description("")
  String StopAcceptingMethods();

  @DefaultMessage("RecordVideo")
  @Description("")
  String RecordVideoMethods();

  @DefaultMessage("TakePicture")
  @Description("")
  String TakePictureMethods();

  @DefaultMessage("Clear")
  @Description("")
  String ClearMethods();

  @DefaultMessage("DrawCircle")
  @Description("")
  String DrawCircleMethods();

  @DefaultMessage("DrawLine")
  @Description("")
  String DrawLineMethods();

  @DefaultMessage("DrawPoint")
  @Description("")
  String DrawPointMethods();

  @DefaultMessage("DrawText")
  @Description("")
  String DrawTextMethods();

  @DefaultMessage("DrawTextAtAngle")
  @Description("")
  String DrawTextAtAngleMethods();

  @DefaultMessage("GetBackgroundPixelColor")
  @Description("")
  String GetBackgroundPixelColorMethods();

  @DefaultMessage("GetPixelColor")
  @Description("")
  String GetPixelColorMethods();

  @DefaultMessage("Save")
  @Description("")
  String SaveMethods();

  @DefaultMessage("SaveAs")
  @Description("")
  String SaveAsMethods();

  @DefaultMessage("SetBackgroundPixelColor")
  @Description("")
  String SetBackgroundPixelColorMethods();

  @DefaultMessage("AddDuration")
  @Description("")
  String AddDurationMethods();

  @DefaultMessage("AddDays")
  @Description("")
  String AddDaysMethods();

  @DefaultMessage("AddHours")
  @Description("")
  String AddHoursMethods();

  @DefaultMessage("AddMinutes")
  @Description("")
  String AddMinutesMethods();

  @DefaultMessage("AddMonths")
  @Description("")
  String AddMonthsMethods();

  @DefaultMessage("AddSeconds")
  @Description("")
  String AddSecondsMethods();

  @DefaultMessage("AddWeeks")
  @Description("")
  String AddWeeksMethods();

  @DefaultMessage("AddYears")
  @Description("")
  String AddYearsMethods();

  @DefaultMessage("DayOfMonth")
  @Description("")
  String DayOfMonthMethods();

  @DefaultMessage("Duration")
  @Description("")
  String DurationMethods();

  @DefaultMessage("DurationToSeconds")
  @Description("")
  String DurationToSecondsMethods();

  @DefaultMessage("DurationToMinutes")
  @Description("")
  String DurationToMinutesMethods();

  @DefaultMessage("DurationToHours")
  @Description("")
  String DurationToHoursMethods();

  @DefaultMessage("DurationToDays")
  @Description("")
  String DurationToDaysMethods();

  @DefaultMessage("DurationToWeeks")
  @Description("")
  String DurationToWeeksMethods();

  @DefaultMessage("FormatDate")
  @Description("")
  String FormatDateMethods();

  @DefaultMessage("FormatDateTime")
  @Description("")
  String FormatDateTimeMethods();

  @DefaultMessage("FormatTime")
  @Description("")
  String FormatTimeMethods();

  @DefaultMessage("GetMillis")
  @Description("")
  String GetMillisMethods();

  @DefaultMessage("Hour")
  @Description("")
  String HourMethods();

  @DefaultMessage("Instant")
  @Description("")
  String InstantMethods();

  @DefaultMessage("MakeInstant")
  @Description("")
  String MakeInstantMethods();

  @DefaultMessage("MakeInstantFromParts")
  @Description("")
  String MakeInstantFromPartsMethods();

  @DefaultMessage("MakeDate")
  @Description("")
  String MakeDateMethods();

  @DefaultMessage("MakeTime")
  @Description("")
  String MakeTimeMethods();

  @DefaultMessage("secondParams")
  @Description("")
  String secondParamsMethods();

  @DefaultMessage("MakeInstantFromMillis")
  @Description("")
  String MakeInstantFromMillisMethods();

  @DefaultMessage("Minute")
  @Description("")
  String MinuteMethods();

  @DefaultMessage("Month")
  @Description("")
  String MonthMethods();

  @DefaultMessage("MonthName")
  @Description("")
  String MonthNameMethods();

  @DefaultMessage("Now")
  @Description("")
  String NowMethods();

  @DefaultMessage("Second")
  @Description("")
  String SecondMethods();

  @DefaultMessage("SystemTime")
  @Description("")
  String SystemTimeMethods();

  @DefaultMessage("Weekday")
  @Description("")
  String WeekdayMethods();

  @DefaultMessage("WeekdayName")
  @Description("")
  String WeekdayNameMethods();

  @DefaultMessage("Year")
  @Description("")
  String YearMethods();

  @DefaultMessage("Open")
  @Description("")
  String OpenMethods();

  @DefaultMessage("CloseScreenAnimation")
  @Description("")
  String CloseScreenAnimationMethods();

  @DefaultMessage("OpenScreenAnimation")
  @Description("")
  String OpenScreenAnimationMethods();

  @DefaultMessage("DoQuery")
  @Description("")
  String DoQueryMethods();

  @DefaultMessage("ForgetLogin")
  @Description("")
  String ForgetLoginMethods();

  @DefaultMessage("SendQuery")
  @Description("")
  String SendQueryMethods();

  @DefaultMessage("GetInstanceLists")
  @Description("")
  String GetInstanceListsMethods();

  @DefaultMessage("GetMessages")
  @Description("")
  String GetMessagesMethods();

  @DefaultMessage("Invite")
  @Description("")
  String InviteMethods();

  @DefaultMessage("LeaveInstance")
  @Description("")
  String LeaveInstanceMethods();

  @DefaultMessage("MakeNewInstance")
  @Description("")
  String MakeNewInstanceMethods();

  @DefaultMessage("ServerCommand")
  @Description("")
  String ServerCommandMethods();

  @DefaultMessage("SetInstance")
  @Description("")
  String SetInstanceMethods();

  @DefaultMessage("SetLeader")
  @Description("")
  String SetLeaderMethods();

  @DefaultMessage("Bounce")
  @Description("")
  String BounceMethods();

  @DefaultMessage("CollidingWith")
  @Description("")
  String CollidingWithMethods();

  @DefaultMessage("MoveIntoBounds")
  @Description("")
  String MoveIntoBoundsMethods();

  @DefaultMessage("MoveTo")
  @Description("")
  String MoveToMethods();

  @DefaultMessage("PointInDirection")
  @Description("")
  String PointInDirectionMethods();

  @DefaultMessage("PointTowards")
  @Description("")
  String PointTowardsMethods();

  @DefaultMessage("LatitudeFromAddress")
  @Description("")
  String LatitudeFromAddressMethods();

  @DefaultMessage("LongitudeFromAddress")
  @Description("")
  String LongitudeFromAddressMethods();

  @DefaultMessage("LogError")
  @Description("")
  String LogErrorMethods();

  @DefaultMessage("LogInfo")
  @Description("")
  String LogInfoMethods();

  @DefaultMessage("LogWarning")
  @Description("")
  String LogWarningMethods();

  @DefaultMessage("ShowAlert")
  @Description("")
  String ShowAlertMethods();

  @DefaultMessage("ShowChooseDialog")
  @Description("")
  String ShowChooseDialogMethods();

  @DefaultMessage("ShowMessageDialog")
  @Description("")
  String ShowMessageDialogMethods();

  @DefaultMessage("ShowTextDialog")
  @Description("")
  String ShowTextDialogMethods();

  @DefaultMessage("ShowPasswordDialog")
  @Description("")
  String ShowPasswordDialogMethods();

  @DefaultMessage("ShowProgressDialog")
  @Description("")
  String ShowProgressDialogMethods();

  @DefaultMessage("DismissProgressDialog")
  @Description("")
  String DismissProgressDialogMethods();

  @DefaultMessage("GetColor")
  @Description("")
  String GetColorMethods();

  @DefaultMessage("GetLightLevel")
  @Description("")
  String GetLightLevelMethods();

  @DefaultMessage("DeleteFile")
  @Description("")
  String DeleteFileMethods();

  @DefaultMessage("DownloadFile")
  @Description("")
  String DownloadFileMethods();

  @DefaultMessage("GetBatteryLevel")
  @Description("")
  String GetBatteryLevelMethods();

  @DefaultMessage("GetBrickName")
  @Description("")
  String GetBrickNameMethods();

  @DefaultMessage("GetCurrentProgramName")
  @Description("")
  String GetCurrentProgramNameMethods();

  @DefaultMessage("GetFirmwareVersion")
  @Description("")
  String GetFirmwareVersionMethods();

  @DefaultMessage("GetInputValues")
  @Description("")
  String GetInputValuesMethods();

  @DefaultMessage("GetOutputState")
  @Description("")
  String GetOutputStateMethods();

  @DefaultMessage("KeepAlive")
  @Description("")
  String KeepAliveMethods();

  @DefaultMessage("ListFiles")
  @Description("")
  String ListFilesMethods();

  @DefaultMessage("LsGetStatus")
  @Description("")
  String LsGetStatusMethods();

  @DefaultMessage("LsRead")
  @Description("")
  String LsReadMethods();

  @DefaultMessage("MessageRead")
  @Description("")
  String MessageReadMethods();

  @DefaultMessage("MessageWrite")
  @Description("")
  String MessageWriteMethods();

  @DefaultMessage("PlaySoundFile")
  @Description("")
  String PlaySoundFileMethods();

  @DefaultMessage("PlayTone")
  @Description("")
  String PlayToneMethods();

  @DefaultMessage("ResetInputScaledValue")
  @Description("")
  String ResetInputScaledValueMethods();

  @DefaultMessage("ResetMotorPosition")
  @Description("")
  String ResetMotorPositionMethods();

  @DefaultMessage("SetBrickName")
  @Description("")
  String SetBrickNameMethods();

  @DefaultMessage("SetInputMode")
  @Description("")
  String SetInputModeMethods();

  @DefaultMessage("SetOutputState")
  @Description("")
  String SetOutputStateMethods();

  @DefaultMessage("StartProgram")
  @Description("")
  String StartProgramMethods();

  @DefaultMessage("StopProgram")
  @Description("")
  String StopProgramMethods();

  @DefaultMessage("StopSoundPlayback")
  @Description("")
  String StopSoundPlaybackMethods();

  @DefaultMessage("LsWrite")
  @Description("")
  String LsWriteMethods();

  @DefaultMessage("MoveBackward")
  @Description("")
  String MoveBackwardMethods();

  @DefaultMessage("MoveBackwardIndefinitely")
  @Description("")
  String MoveBackwardIndefinitelyMethods();

  @DefaultMessage("MoveForward")
  @Description("")
  String MoveForwardMethods();

  @DefaultMessage("MoveForwardIndefinitely")
  @Description("")
  String MoveForwardIndefinitelyMethods();

  @DefaultMessage("Stop")
  @Description("")
  String StopMethods();

  @DefaultMessage("TurnClockwiseIndefinitely")
  @Description("")
  String TurnClockwiseIndefinitelyMethods();

  @DefaultMessage("TurnCounterClockwiseIndefinitely")
  @Description("")
  String TurnCounterClockwiseIndefinitelyMethods();

  @DefaultMessage("GetSoundLevel")
  @Description("")
  String GetSoundLevelMethods();

  @DefaultMessage("IsPressed")
  @Description("")
  String IsPressedMethods();

  @DefaultMessage("GetDistance")
  @Description("")
  String GetDistanceMethods();

  @DefaultMessage("Pause")
  @Description("")
  String PauseMethods();

  @DefaultMessage("Reset")
  @Description("")
  String ResetMethods();

  @DefaultMessage("Resume")
  @Description("")
  String ResumeMethods();

  @DefaultMessage("Start")
  @Description("")
  String StartMethods();

  @DefaultMessage("MakePhoneCall")
  @Description("")
  String MakePhoneCallMethods();

  @DefaultMessage("MakePhoneCallDirect")
  @Description("")
  String MakePhoneCallDirectMethods();

  @DefaultMessage("GetWifiIpAddress")
  @Description("")
  String GetWifiIpAddressMethods();

  @DefaultMessage("isConnected")
  @Description("")
  String isConnectedMethods();

  @DefaultMessage("setHmacSeedReturnCode")
  @Description("")
  String setHmacSeedReturnCodeMethods();

  @DefaultMessage("startHTTPD")
  @Description("")
  String startHTTPDMethods();

  @DefaultMessage("Vibrate")
  @Description("")
  String VibrateMethods();

  @DefaultMessage("GetText")
  @Description("")
  String GetTextMethods();

  @DefaultMessage("HideKeyboard")
  @Description("")
  String HideKeyboardMethods();

  @DefaultMessage("AskForPermission")
  @Description("")
  String AskForPermissionMethods();

  @DefaultMessage("Speak")
  @Description("")
  String SpeakMethods();

  @DefaultMessage("SendMessage")
  @Description("")
  String SendMessageMethods();

  @DefaultMessage("SendMessageDirect")
  @Description("")
  String SendMessageDirectMethods();

  @DefaultMessage("GetValue")
  @Description("")
  String GetValueMethods();

  @DefaultMessage("StoreValue")
  @Description("")
  String StoreValueMethods();

  @DefaultMessage("GetTagList")
  @Description("")
  String GetTagListMethods();

  @DefaultMessage("AppendValue")
  @Description("")
  String AppendValueMethods();

  @DefaultMessage("RemoveFirst")
  @Description("")
  String RemoveFirstMethods();

  @DefaultMessage("AppendValueToList")
  @Description("")
  String AppendValueToListMethods();

  @DefaultMessage("RemoveFirstFromList")
  @Description("")
  String RemoveFirstFromListMethods();

  @DefaultMessage("FirstRemoved")
  @Description("")
  String FirstRemovedEvents();

  @DefaultMessage("Authorize")
  @Description("")
  String AuthorizeMethods();

  @DefaultMessage("CheckAuthorized")
  @Description("")
  String CheckAuthorizedMethods();

  @DefaultMessage("DeAuthorize")
  @Description("")
  String DeAuthorizeMethods();

  @DefaultMessage("DirectMessage")
  @Description("")
  String DirectMessageMethods();

  @DefaultMessage("Follow")
  @Description("")
  String FollowMethods();

  @DefaultMessage("RequestDirectMessages")
  @Description("")
  String RequestDirectMessagesMethods();

  @DefaultMessage("RequestFollowers")
  @Description("")
  String RequestFollowersMethods();

  @DefaultMessage("RequestFriendTimeline")
  @Description("")
  String RequestFriendTimelineMethods();

  @DefaultMessage("RequestMentions")
  @Description("")
  String RequestMentionsMethods();

  @DefaultMessage("SearchTwitter")
  @Description("")
  String SearchTwitterMethods();

  @DefaultMessage("SetStatus")
  @Description("")
  String SetStatusMethods();

  @DefaultMessage("StopFollowing")
  @Description("")
  String StopFollowingMethods();

  @DefaultMessage("GetDuration")
  @Description("")
  String GetDurationMethods();

  @DefaultMessage("SeekTo")
  @Description("")
  String SeekToMethods();

  @DefaultMessage("DoScan")
  @Description("")
  String DoScanMethods();

  @DefaultMessage("RequestBallot")
  @Description("")
  String RequestBallotMethods();

  @DefaultMessage("SendBallot")
  @Description("")
  String SendBallotMethods();

  @DefaultMessage("BuildPostData")
  @Description("")
  String BuildPostDataMethods();

  @DefaultMessage("ClearCookies")
  @Description("")
  String ClearCookiesMethods();

  @DefaultMessage("Get")
  @Description("")
  String GetMethods();

  @DefaultMessage("HtmlTextDecode")
  @Description("")
  String HtmlTextDecodeMethods();

  @DefaultMessage("JsonTextDecode")
  @Description("")
  String JsonTextDecodeMethods();

  @DefaultMessage("JsonObjectEncode")
  @Description("")
  String JsonObjectEncodeMethods();

  @DefaultMessage("XmlTextDecode")
  @Description("")
  String xmlTextDecodeMethods();

  @DefaultMessage("PostFile")
  @Description("")
  String PostFileMethods();

  @DefaultMessage("PostText")
  @Description("")
  String PostTextMethods();

  @DefaultMessage("PostTextWithEncoding")
  @Description("")
  String PostTextWithEncodingMethods();

  @DefaultMessage("UriEncode")
  @Description("")
  String UriEncodeMethods();

  @DefaultMessage("UriDecode")
  @Description("")
  String UriDecodeMethods();

  @DefaultMessage("CanGoBack")
  @Description("")
  String CanGoBackMethods();

  @DefaultMessage("CanGoForward")
  @Description("")
  String CanGoForwardMethods();

  @DefaultMessage("ClearLocations")
  @Description("")
  String ClearLocationsMethods();

  @DefaultMessage("ClearCaches")
  @Description("")
  String ClearCachesMethods();

  @DefaultMessage("GoBack")
  @Description("")
  String GoBackMethods();

  @DefaultMessage("GoForward")
  @Description("")
  String GoForwardMethods();

  @DefaultMessage("GoHome")
  @Description("")
  String GoHomeMethods();

  @DefaultMessage("GoToUrl")
  @Description("")
  String GoToUrlMethods();

  @DefaultMessage("AppendToFile")
  @Description("")
  String AppendToFileMethods();

  @DefaultMessage("Delete")
  @Description("")
  String DeleteMethods();

  @DefaultMessage("ReadFrom")
  @Description("")
  String ReadFromMethods();

  @DefaultMessage("SaveFile")
  @Description("")
  String SaveFileMethods();

  @DefaultMessage("doFault")
  @Description("")
  String doFaultMethods();

  @DefaultMessage("installURL")
  @Description("")
  String installURLMethods();

  @DefaultMessage("isDirect")
  @Description("")
  String isDirectMethods();

  @DefaultMessage("setAssetsLoaded")
  @Description("")
  String setAssetsLoadedMethods();

  @DefaultMessage("shutdown")
  @Description("")
  String shutdownMethods();

  @DefaultMessage("ShareFile")
  @Description("")
  String ShareFileMethods();

  @DefaultMessage("ShareFileWithMessage")
  @Description("")
  String ShareFileWithMessageMethods();

  @DefaultMessage("ShareMessage")
  @Description("")
  String ShareMessageMethods();

  @DefaultMessage("Play")
  @Description("")
  String PlayMethods();

  @DefaultMessage("DisplayDropdown")
  @Description("")
  String DisplayDropdownMethods();

  @DefaultMessage("ClearAll")
  @Description("")
  String ClearAllMethods();

  @DefaultMessage("ClearTag")
  @Description("")
  String ClearTagMethods();

  @DefaultMessage("GetTags")
  @Description("")
  String GetTagsMethods();

  @DefaultMessage("Tweet")
  @Description("")
  String TweetMethods();

  @DefaultMessage("TweetWithImage")
  @Description("")
  String TweetWithImageMethods();

  @DefaultMessage("BuildRequestData")
  @Description("")
  String BuildRequestDataMethods();

  @DefaultMessage("PutFile")
  @Description("")
  String PutFileMethods();

  @DefaultMessage("PutText")
  @Description("")
  String PutTextMethods();

  @DefaultMessage("PutTextWithEncoding")
  @Description("")
  String PutTextWithEncodingMethods();

  @DefaultMessage("RequestTranslation")
  @Description("")
  String RequestTranslationMethods();

  @DefaultMessage("GetBatteryCurrent")
  @Description("")
  String GetBatteryCurrentMethods();

  @DefaultMessage("GetBatteryVoltage")
  @Description("")
  String GetBatteryVoltageMethods();

  @DefaultMessage("GetHardwareVersion")
  @Description("")
  String GetHardwareVersionMethods();

  @DefaultMessage("SetAngleMode")
  @Description("")
  String SetAngleModeMethods();

  @DefaultMessage("SetRateMode")
  @Description("")
  String SetRateModeMethods();

  @DefaultMessage("SetCmUnit")
  @Description("")
  String SetCmUnitMethods();

  @DefaultMessage("SetInchUnit")
  @Description("")
  String SetInchUnitMethods();

  @DefaultMessage("SetColorMode")
  @Description("")
  String SetColorModeMethods();

  @DefaultMessage("SetReflectedMode")
  @Description("")
  String SetReflectedModeMethods();

  @DefaultMessage("SetAmbientMode")
  @Description("")
  String SetAmbientModeMethods();

  @DefaultMessage("RotateIndefinitely")
  @Description("")
  String RotateIndefinitelyMethods();

  @DefaultMessage("RotateInDistance")
  @Description("")
  String RotateInDistanceMethods();

  @DefaultMessage("RotateInDuration")
  @Description("")
  String RotateInDurationMethods();

  @DefaultMessage("RotateInTachoCounts")
  @Description("")
  String RotateInTachoCountsMethods();

  @DefaultMessage("RotateSyncIndefinitely")
  @Description("")
  String RotateSyncIndefinitelyMethods();

  @DefaultMessage("RotateSyncInDistance")
  @Description("")
  String RotateSyncInDistanceMethods();

  @DefaultMessage("RotateSyncInDuration")
  @Description("")
  String RotateSyncInDurationMethods();

  @DefaultMessage("RotateSyncInTachoCounts")
  @Description("")
  String RotateSyncInTachoCountsMethods();

  @DefaultMessage("ToggleDirection")
  @Description("")
  String ToggleDirectionMethods();

  @DefaultMessage("GetTachoCount")
  @Description("")
  String GetTachoCountMethods();

  @DefaultMessage("ResetTachoCount")
  @Description("")
  String ResetTachoCountMethods();

  @DefaultMessage("GetSensorValue")
  @Description("")
  String GetSensorValueMethods();

  @DefaultMessage("GetColorCode")
  @Description("")
  String GetColorCodeMethods();

  @DefaultMessage("GetColorName")
  @Description("")
  String GetColorNameMethods();

  @DefaultMessage("FillScreen")
  @Description("")
  String FillScreenMethods();

  @DefaultMessage("DrawRect")
  @Description("")
  String DrawRectMethods();

  @DefaultMessage("DrawIcon")
  @Description("")
  String DrawIconMethods();

  @DefaultMessage("GetOSVersion")
  @Description("")
  String GetOSVersionMethods();

  @DefaultMessage("GetOSBuild")
  @Description("")
  String GetOSBuildMethods();

  @DefaultMessage("GetFirmwareBuild")
  @Description("")
  String GetFirmwareBuildMethods();

  @DefaultMessage("StopSound")
  @Description("")
  String StopSoundMethods();

  //Mock Components
  @DefaultMessage("add items...")
  @Description("")
  String MockSpinnerAddItems();

  //Ode.java messages
  @DefaultMessage("Welcome to App Inventor 2!")
  @Description("")
  String createNoProjectsDialogText();

  @DefaultMessage("You do not have any projects in App Inventor 2. " +
      "To learn how to use App Inventor, click the \"Guide\" " +
      "link at the top of the window; or to start your first project, " +
      "click the \"Start New Project\" button at the upper left of the window.")
  @Description("")
  String createNoProjectsDialogMessage1();

  @DefaultMessage("Your Trash List is Empty")
  @Description("")
  String showEmptyTrashMessage();

  @DefaultMessage("Happy Inventing!")
  @Description("")
  String createNoprojectsDialogMessage2();

  @DefaultMessage("Welcome to App Inventor!")
  @Description("")
  String createWelcomeDialogText();

  @DefaultMessage("Continue")
  @Description("")
  String createWelcomeDialogButton();

  @DefaultMessage("Do Not Show Again")
  @Description("")
  String doNotShow();

  @DefaultMessage("<h2>Please fill out a short voluntary survey so that we can learn more about our users and improve MIT App Inventor.</h2>")
  @Description("")
  String showSurveySplashMessage();

  @DefaultMessage("Take Survey Now")
  @Description("")
  String showSurveySplashButtonNow();

  @DefaultMessage("Take Survey Later")
  @Description("")
  String showSurveySplashButtonLater();

  @DefaultMessage("Never Take Survey")
  @Description("")
  String showSurveySplashButtonNever();

  @DefaultMessage("This Session Is Out of Date")
  @Description("")
  String invalidSessionDialogText();

  @DefaultMessage("<p><font color=red>Warning:</font> This session is out of date.</p>" +
                "<p>This App Inventor account has been opened from another location. " +
                "Using a single account from more than one location at the same time " +
                "can damage your projects.</p>" +
                "<p>Choose one of the buttons below to:" +
                "<ul>" +
                "<li>End this session here.</li>" +
                "<li>Make this the current session and make the other sessions out of date.</li>" +
                "<li>Continue with both sessions.</li>" +
                "</ul>" +
                "</p>")
  @Description("")
  String invalidSessionDialogMessage();

  @DefaultMessage("End This Session")
  @Description("")
  String invalidSessionDialogButtonEnd();

  @DefaultMessage("Make this the current session")
  @Description("")
  String invalidSessionDialogButtonCurrent();

  @DefaultMessage("Continue with Both Sessions")
  @Description("")
  String invalidSessionDialogButtonContinue();

  @DefaultMessage("Do you want to continue with multiple sessions?")
  @Description("")
  String bashWarningDialogText();

  @DefaultMessage("<p><font color=red>WARNING:</font> A second App " +
                "Inventor session has been opened for this account. You may choose to " +
                "continue with both sessions, but working with App Inventor from more " +
                "than one session simultaneously can cause blocks to be lost in ways " +
                "that cannot be recovered from the App Inventor server.</p><p>" +
                "We recommend that people not open multiple sessions on the same " +
                "account. But if you do need to work in this way, then you should " +
                "regularly export your project to your local computer, so you will " +
                "have a backup copy independent of the App Inventor server. Use " +
                "\"Export\" from the Projects menu to export the project.</p>")
  @Description("")
  String bashWarningDialogMessage();

  @DefaultMessage("Continue with Multiple Sessions")
  @Description("")
  String bashWarningDialogButtonContinue();

  @DefaultMessage("Do not use multiple Sessions")
  @Description("")
  String bashWarningDialogButtonNo();

  @DefaultMessage("Your Session is Finished")
  @Description("")
  String finalDialogText();

  @DefaultMessage("Project Loading...")
  @Description("")
  String galleryLoadingDialogText();

  @DefaultMessage("Your Account is Disabled")
  @Description("")
  String accountDisabledMessage();

  @DefaultMessage("<p><b>Your Session is now ended, you may close this window</b></p>")
  @Description("")
  String finalDialogMessage();

  @DefaultMessage("Project Read Error")
  @Description("")
  String corruptionDialogText();

  @DefaultMessage("<p><b>We detected errors while reading in your project</b></p>" +
                "<p>To protect your project from damage, we have ended this session. You may close this " +
                "window.</p>")
  @Description("")
  String corruptionDialogMessage();

  @DefaultMessage("Blocks Workspace is Empty")
  @Description("")
  String blocksTruncatedDialogText();

  @DefaultMessage("<p>It appears that <b>" + "%1" +
      "</b> has had all blocks removed.</p><p>" +
      "<ul><li>You can save the empty screen, and then all those blocks will be " +
      "permanently gone from the project.</li>" +
      "<li>Alternatively, you can restore the previously saved version " +
      "of the project.</li></ul></p>")
  @Description("")
  String blocksTruncatedDialogMessage();

  @DefaultMessage("Save the empty screen now.")
  @Description("")
  String blocksTruncatedDialogButtonSave();

  @DefaultMessage("Restore the previous version.")
  @Description("")
  String blocksTruncatedDialogButtonNoSave();

  @DefaultMessage("Please wait " + "%1" + " seconds...")
  @Description("")
  String blocksTruncatedDialogButtonHTML();

  @DefaultMessage("InsertRow")
  @Description("")
  String InsertRowMethods();

  @DefaultMessage("GetRows")
  @Description("")
  String GetRowsMethods();

  @DefaultMessage("GetRowsWithConditions")
  @Description("")
  String GetRowsWithConditionsMethods();

  @DefaultMessage("Progress Bar")
  @Description("")
  String ProgressBarFor();

  @DefaultMessage("MaximumRange")
  @Description("")
  String MaximumRangeProperties();

  @DefaultMessage("KeepRunningWhenOnPause")
  @Description("")
  String KeepRunningWhenOnPauseProperties();

  @DefaultMessage("ProximityChangedProperties")
  @Description("")
  String ProximityChangedPropertiesProperties();

  @DefaultMessage("ProximityChanged")
  @Description("")
  String ProximityChangedMethods();

  @DefaultMessage("MaximumRangeMethods")
  @Description("")
  String MaximumRangeMethods();

  @DefaultMessage(
    "<table border='1' cellpadding='8' cellspacing='0'>" +
    "<thead>" +
    "<tr>" +
    "<th>Action</th>" +
    "<th>Key Combination</th>" +
    "</tr>" +
    "</thead>" +
    "<tbody>" +
    "<tr><td>Focus Component search box</td><td>/</td></tr>" +
    "<tr><td>Focus Components tree</td><td>T</td></tr>" +
    "<tr><td>Focus Viewer</td><td>V</td></tr>" +
    "<tr><td>Focus Properties Panel</td><td>P</td></tr>" +
    "<tr><td>Focus Media Panel</td><td>M</td></tr>" +
    "<tr><td>Switch between Designer and Block editor</td><td>Ctrl + Alt</td></tr>" +
    "<tr><td>Rename Component</td><td>Alt + N</td></tr>" +
    "<tr><td>Delete Component</td><td>Delete/Backspace</td></tr>" +
    "<tr><td>Reset Connection</td><td>Alt + Shift + R</td></tr>" +
    "<tr><td>Refresh Companion Screen</td><td>Alt + R</td></tr>" +
    "<tr><td>Navigate Components in components tree</td><td>↑/↓</td></tr>" +
    "<tr><td>Open this dialog</td><td>Alt + ?</td></tr>" +
    "</tbody>" +
    "</table>")
  @Description("")
  String KeyBoardShortcuts();

  // =========== ListPicker
  @DefaultMessage("ItemTextColor")
  @Description("")
  String ItemTextColorProperties();

  @DefaultMessage("ItemBackgroundColor")
  @Description("")
  String ItemBackgroundColorProperties();

  @DefaultMessage("Error on Fusion Tables query")
  @Description("")
  String FusionTablesStandardErrorMessage();

  @DefaultMessage("WARNING: Google has Deprecated the Fusion Tables Service. " +
    "It will stop working on December 3, 2019 " +
    "<a href=\"https://support.google.com/fusiontables/answer/9185417\" target=\"_blank\"> " +
    "Learn More</a>")
  @Description("")
  String FusionTablesDeprecated();

  @DefaultMessage("WARNING: The library the Twitter component uses, Twitter4J, no longer works as of 2024. " +
  "The component no longer works and has been deprecated.")
  @Description("")
  String TwitterDeprecated();

  @DefaultMessage("SelectionColor")
  @Description("")
  String SelectionColorProperties();

  // Missing translations from 4/8/2015 -- Should sort into appropriate place

  @DefaultMessage("LaunchPicker")
  @Description("")
  String LaunchPickerMethods();

  @DefaultMessage("SetDateToDisplay")
  @Description("")
  String SetDateToDisplayMethods();

  @DefaultMessage("SetDateToDisplayFromInstant")
  @Description("")
  String SetDateToDisplayFromInstantMethods();

  @DefaultMessage("IncomingCallAnswered")
  @Description("")
  String IncomingCallAnsweredEvents();

  @DefaultMessage("PhoneCallEnded")
  @Description("")
  String PhoneCallEndedEvents();

  @DefaultMessage("PhoneCallStarted")
  @Description("")
  String PhoneCallStartedEvents();

  @DefaultMessage("OnSettings")
  @Description("")
  String OnSettingsEvents();

  @DefaultMessage("OtherPlayerStarted")
  @Description("")
  String OtherPlayerStartedEvents();

  @DefaultMessage("ProximityChanged")
  @Description("")
  String ProximityChangedEvents();

  @DefaultMessage("ThumbEnabled")
  @Description("")
  String ThumbEnabledProperties();

  @DefaultMessage("SetTimeToDisplay")
  @Description("")
  String SetTimeToDisplayMethods();

  @DefaultMessage("SetTimeToDisplayFromInstant")
  @Description("")
  String SetTimeToDisplayFromInstantMethods();

  @DefaultMessage("XMLTextDecode")
  @Description("")
  String XMLTextDecodeMethods();

  @DefaultMessage("ExtraKey and ExtraValue are deprecated and will not be supported. " +
      "Please use the new Extras property in Blocks.\n")
  @Description("")
  String extraKeyValueWarning();

  @DefaultMessage("MediaStored")
  @Description("")
  String MediaStoredEvents();

  @DefaultMessage("PostMedia")
  @Description("")
  String PostMediaMethods();

  @DefaultMessage("Scale proportionally")
  @Description("A choice in ScalingChoicePropertyEditor")
  String scaleProportionally();

  @DefaultMessage("Scale to fit")
  @Description("A choice in ScalingChoicePropertyEditor")
  String scaleToFit();

  @DefaultMessage("Unauthenticate")
  @Description("")
  String UnauthenticateMethods();

  @DefaultMessage("Use Default")
  @Description("Used by the MockFirebaseDB to display default checkbox")
  String useDefault();

  @DefaultMessage("DefaultURL")
  @Description("")
  String DefaultURLProperties();

  @DefaultMessage("Warning!")
  @Description("")
  String warningDialogTitle();

  @DefaultMessage("The useFront property has been removed from your Camera Component")
  @Description("")
  String useFrontDeprecated();

  @DefaultMessage("FirebaseDB is an experimental feature " +
    "which may change in the future or break. Packaged Apps built with this component may not " +
    "function into the indefinite future.")
  @Description("")
  String firebaseExperimentalWarning();

  @DefaultMessage("ChatBot is an experimental feature " +
    "which may change in the future. It''s usage may be restricted, " +
    "see <a href=\"https://appinv.us/chatbot\" target=\"_blank\">" +
    "https://appinv.us/chatbot</a> for more information.")
  @Description("")
  String chatBotExperimentalWarning();

  @DefaultMessage("ImageBot is an experimental feature " +
    "which may change in the future. It''s usage may be restricted, " +
    "see <a href=\"https://appinv.us/imagebot\" target=\"_blank\">" +
    "https://appinv.us/imagebot</a> for more information.")
  @Description("")
  String imageBotExperimentalWarning();

  @DefaultMessage("You are in Read Only Mode")
  @Description("")
  String readOnlyMode();

  @DefaultMessage("Either your session has expired, or App Inventor has been upgraded. " +
    "You will need to \"Reload\" your session to continue. Press the \"Reload\" Button " +
    "below.")
  @Description("")
  String sessionDead();

  @DefaultMessage("Reload")
  @Description("")
  String reloadWindow();

  //ThemeChoiceEditor
  @DefaultMessage("Theme")
  @Description("")
  String themeTitle();

  @DefaultMessage("Classic")
  @Description("")
  String classicTheme();

  @DefaultMessage("Device Default")
  @Description("")
  String defaultTheme();

  @DefaultMessage("Black Title Text")
  @Description("")
  String blackTitleTheme();

  @DefaultMessage("Dark")
  @Description("")
  String darkTheme();

  // Maps components

  @DefaultMessage("Roads")
  @Description("Road network map type")
  String mapTypeRoads();

  @DefaultMessage("Aerial")
  @Description("Aerial photography map type")
  String mapTypeAerial();

  @DefaultMessage("Terrain")
  @Description("Terrain map type")
  String mapTypeTerrain();

  @DefaultMessage("Custom")
  @Description("Custom map type")
  String mapTypeCustom();

  @DefaultMessage("CustomUrl")
  @Description("The URL of the custom tile layer to use as the base of the map")
  String mapCustomUrl();

  @DefaultMessage("Metric")
  @Description("Display name for the metric unit system")
  String mapScaleUnitsMetric();

  @DefaultMessage("Imperial")
  @Description("Display name for the imperial unit system")
  String mapScaleUnitsImperial();

  @DefaultMessage("Unexpected map type: {0}")
  @Description("")
  String unknownMapTypeException(String maptype);

  @DefaultMessage("Expected 2 values for CenterFromString but got {0}")
  @Description("")
  String mapCenterWrongNumberArgumentsException(int numArgs);

  @DefaultMessage("CenterFromString")
  @Description("")
  String CenterFromStringProperties();  //TODO: This should not be here. Why is it not auto-generating?

  @DefaultMessage("ZoomLevel must be between 1 and 18")
  @Description("")
  String mapZoomLevelOutOfBoundsException();

  @DefaultMessage("Zoom In")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's zoom in control")
  String mapZoomIn();

  @DefaultMessage("Zoom Out")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's zoom out control")
  String mapZoomOut();

  @DefaultMessage("Lock map movement")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's lock map control")
  String mapLockMovementTooltip();

  @DefaultMessage("Unlock map component")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's unlock map control")
  String mapUnlockMovementTooltip();

  @DefaultMessage("Set initial map to current view")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's set initial map control")
  String mapSetInitialMapTooltip();

  @DefaultMessage("Reset bounding box")
  @Description("Tooltip shown when the user hovers the mouse over the Map component's reset map control")
  String mapResetBoundingBoxTooltip();

  @DefaultMessage("From URL...")
  @Description("")
  String fromUrlButton();

  @DefaultMessage("Import Media from URL")
  @Description("")
  String urlImportWizardCaption();

  @DefaultMessage("Custom...")
  @Description("")
  String customEllipsis();

  @DefaultMessage("The given value {0} was not in the expected range [{1}, {2}].")
  @Description("")
  String valueNotInRange(String text, String min, String max);

  @DefaultMessage("The value supplied for {0} was not a valid latitude, longitude pair.")
  @Description("")
  String expectedLatLongPair(String property);

  @DefaultMessage("The provided URL {0} does not contain placeholders for {1}.") // Can't use {x} here, Java compiler tries to interpret the variable x
  @Description("")
  String customUrlNoPlaceholders(String property, String placeholders);

  @DefaultMessage("The provided URL {0}, when tested, failed authentication (with HTTP status code {1}).")
  @Description("")
  String customUrlBadAuthentication(String property, int statusCode);

  @DefaultMessage("The provided URL {0}, when tested, returned a bad HTTP status code ({1}).")
  @Description("")
  String customUrlBadStatusCode(String property, int statusCode);

  @DefaultMessage("The provided URL {0}, when tested, returned an exception ({1}).")
  @Description("")
  String customUrlException(String property, String e);

  @DefaultMessage("Notice!")
  @Description("Title for the Warning Dialog Box")
  String NoticeTitle();

  @DefaultMessage("Use this option to build apps that that will work back to Android version 2.1 (Eclair)," +
      "<br/>but will not be publishable in the Google Play Store.")
  @Description("Text for the Package non-SDK 26 Warning Dialog Box (HTML)")
  String PackageNotice();

  @DefaultMessage("Use this option to create applications that can be submitted to the Google Play Store." +
      "<br/>These applications will not run on Android versions older than 4.0.")
  @Description("Text for the Package SDK 26 Warning Dialog Box (HTML)")
  String Package26Notice();

  // Navigation component

  @DefaultMessage("Walking")
  @Description("The label used to indicate walking navigation.")
  String WalkingNavMethod();

  @DefaultMessage("Driving")
  @Description("The label used to indicate driving navigation.")
  String DrivingNavMethod();

  @DefaultMessage("Cycling")
  @Description("The label used to indicate cycling navigation.")
  String CyclingNavMethod();

  @DefaultMessage("Wheelchair")
  @Description("The label used to indicate wheelchair navigation.")
  String WheelchairNavMethod();

  // Magnetic Field Sensor Componet
  @DefaultMessage("XStrength")
  @Description("")
  String XstrengthProperties();

  @DefaultMessage("YStrength")
  @Description("")
  String YstrengthProperties();

  @DefaultMessage("ZStrength")
  @Description("")
  String ZstrengthProperties();

  @DefaultMessage("MagneticChanged")
  @Description("")
  String MagneticChangedEvents();

  @DefaultMessage("xStrength")
  @Description("")
  String xStrengthParams();

  @DefaultMessage("yStrength")
  @Description("")
  String yStrengthParams();

  @DefaultMessage("zStrength")
  @Description("")
  String zStrengthParams();

  @DefaultMessage("absoluteStrength")
  @Description("")
  String absoluteStrengthParams();

  @DefaultMessage("AbsoluteStrength")
  @Description("")
  String AbsoluteStrengthProperties();

  @DefaultMessage("<multiple>")
  @Description("String shown when multiple components have different values for a property")
  String multipleValues();

  // Used in editor/youngandroid/properties/YoungAndroidChartTypeChoicePropertyEditor.java

  @DefaultMessage("line")
  @Description("Text for Chart type choice 'line'")
  String lineChartType();

  @DefaultMessage("scatter")
  @Description("Text for Chart type choice 'scatter'")
  String scatterChartType();

  @DefaultMessage("area")
  @Description("Text for Chart type choice 'area'")
  String areaChartType();

  @DefaultMessage("bar")
  @Description("Text for Chart type choice 'bar'")
  String barChartType();

  @DefaultMessage("pie")
  @Description("Text for Chart type choice 'pie'")
  String pieChartType();

  // Used in editor/youngandroid/properties/YoungAndroidChartPointShapeChoicePropertyEditor.java

  @DefaultMessage("circle")
  @Description("Text for Chart point shape choice 'circle'")
  String chartCircleShape();

  @DefaultMessage("square")
  @Description("Text for Chart point shape choice 'square'")
  String chartSquareShape();

  @DefaultMessage("triangle")
  @Description("Text for Chart point shape choice 'triangle'")
  String chartTriangleShape();

  @DefaultMessage("cross")
  @Description("Text for Chart point shape choice 'cross'")
  String chartCrossShape();

  @DefaultMessage("x")
  @Description("Text for Chart point shape choice 'x'")
  String chartXShape();

  // Used in editor/youngandroid/properties/YoungAndroidChartLineTypeChoicePropertyEditor.java

  @DefaultMessage("linear")
  @Description("Text for Chart line type choice 'linear'")
  String lineTypeLinear();

  @DefaultMessage("curved")
  @Description("Text for Chart line type choice 'curved'")
  String lineTypeCurved();

  @DefaultMessage("stepped")
  @Description("Text for Chart line type choice 'stepped'")
  String lineTypeStepped();

  // Used in editor/simple/components/MockDataFile.java

  @DefaultMessage("Reading data from {0} to update the columns of {1}.")
  @Description("Message displayed when data is being parsed for a DataFile component.")
  String dataFileParsingMessage(String filename, String dataFile);

  @DefaultMessage("Empty file specified!")
  @Description("Message displayed when the Source File specified in the DataFile is an empty file.")
  String emptyFileError();

  // Used in editor/youngandroid/properties/YoungAndroidCsvColumnSelectorPropertyEditor.java

  @DefaultMessage("You must select a column!")
  @Description("Message displayed when OK button is clicked when there is no column selected.")
  String noColumnSelected();

  // Used in editor/simple/components Chart Model components
  @DefaultMessage("Invalid Chart Data entry specified.")
  @Description("Message displayed when the user enters an invalid Chart Data entry that cannot "
      + "be parsed.")
  String invalidChartDataEntry();

  @DefaultMessage("reflected")
  @Description("")
  String reflectedValues();

  @DefaultMessage("ambient")
  @Description("")
  String ambientValues();

  @DefaultMessage("color")
  @Description("")
  String colorValues();

  @DefaultMessage("rate")
  @Description("")
  String rateValues();

  @DefaultMessage("angle")
  @Description("")
  String angleValues();

  //these need to be changed/updated
  @DefaultMessage("MainText")
  @Description("Display Text for ListView layout choice having single text.")
  String singleTextLayout();

  @DefaultMessage("MainText, DetailText (Vertical)")
  @Description("Display Text for ListView layout choice having two lines of text.")
  String twoTextLayout();

  @DefaultMessage("MainText, DetailText (Horizontal)")
  @Description("Display Text for ListView layout choice having two lines of text in linear manner.")
  String twoTextLinearLayout();

  @DefaultMessage("Image, MainText")
  @Description("Display Text for ListView layout choice having an image and single line of text.")
  String imageSingleTextLayout();

  @DefaultMessage("Image, MainText, DetailText (Vertical)")
  @Description("Display Text for ListView layout choice having an image and two lines of text.")
  String imageTwoTextLayout();

  @DefaultMessage("Image (Top), MainText, DetailText")
  @Description("Display Text for ListView layout choice having an image on top and two lines of text.")
  String imageTopTwoTextLayout();

  // File Scope choices

  @DefaultMessage("App")
  @Description("")
  String fileScopeApp();

  @DefaultMessage("Asset")
  @Description("")
  String fileScopeAsset();

  @DefaultMessage("Cache")
  @Description("")
  String fileScopeCache();

  @DefaultMessage("Legacy")
  @Description("")
  String fileScopeLegacy();

  @DefaultMessage("Private")
  @Description("")
  String fileScopePrivate();

  @DefaultMessage("Shared")
  @Description("")
  String fileScopeShared();

  @DefaultMessage("General")
  @Description("Text to Display General Project Property Category in Project Property Dialog")
  String projectPropertyGeneralCategoryTitle();

  @DefaultMessage("Theming")
  @Description("Text to Display Theming Project Property Category in Project Property Dialog")
  String projectPropertyThemingCategoryTitle();

  @DefaultMessage("Publishing")
  @Description("Text to Display Publishing Project Property Category in Project Property Dialog")
  String projectPropertyPublishingCategoryTitle();

  // Best Fit Model names

  @DefaultMessage("Linear")
  @Description("")
  String fitModelLinear();

  @DefaultMessage("Quadratic")
  @Description("")
  String fitModelQuadratic();

  @DefaultMessage("Cubic")
  @Description("")
  String fitModelCubic();

  @DefaultMessage("Exponential")
  @Description("")
  String fitModelExponential();

  @DefaultMessage("Logarithmic")
  @Description("")
  String fitModelLogarithmic();

  // Stroke Style names

  @DefaultMessage("Solid")
  @Description("")
  String strokeStyleSolid();

  @DefaultMessage("Dashed")
  @Description("")
  String strokeStyleDashed();

  @DefaultMessage("Dotted")
  @Description("")
  String strokeStyleDotted();

  // Trendline customization

  @DefaultMessage("XIntercept(s)")
  @Description("")
  String XInterceptsProperties();

  @DefaultMessage("Mark Origin")
  @Description("")
  String markOriginButton();
  
  @DefaultMessage("Place the Marker on the Origin")
  @Description("Caption for Mark Origin Wizard")
  String markOriginWizardCaption();

  @DefaultMessage("Set an image before attempting to Mark its Origin")
  @Description("Message to display when Mark Origin is attempted before an image is set")
  String provideImageFirst();

  @DefaultMessage("You can always change your user interface under the Settings menu.")
  @Description("Dialog to introduce users to new UI. Shown once to each user.")
  String selectUIStyle();

  @DefaultMessage("Integer")
  @Description("Set x-axis label values as Integers")
  String labelInteger();

  @DefaultMessage("Decimal")
  @Description("Set x-axis label values as Decimal")
  String labelDecimal();

  @DefaultMessage("Date")
  @Description("Set x-axis label values Type as Date")
  String labelDate();

  @DefaultMessage("Time")
  @Description("Set x-axis label values Type as Time")
  String labelTime();

  @DefaultMessage("Welcome to App Inventor Neo! If you are looking for the classic App Inventor look, you can switch in the User Interface Settings, or <a href=\"\">click here</a>.")
  @Description("Message shown in the info popup when the user first opens the Neo UI.")
  String neoWelcomeMessage();
}
