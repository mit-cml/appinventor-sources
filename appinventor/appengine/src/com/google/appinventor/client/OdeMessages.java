// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n strings for {@link Ode}.
 *
 */
//@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
//@LocalizableResource.DefaultLocale("en")

public interface OdeMessages extends Messages {
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

  @DefaultMessage("Delete Project")
  @Description("Text on \"Delete Project\" button")
  String deleteProjectButton();

  @DefaultMessage("Publish to Gallery")
  @Description("Text on \"Publish to Gallery\" button")
  String publishToGalleryButton();

  @DefaultMessage("Update Gallery App")
  @Description("Text on \"Update Gallery App\" button")
  String updateGalleryAppButton();

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

  @DefaultMessage("Save As")
  @Description("Label of the button for save as")
  String saveAsButton();

  @DefaultMessage("Checkpoint ...")
  @Description("Label of the button for checkpoint")
  String checkpointButton();

  @DefaultMessage("Toggle Tutorial")
  @Description("Label for the Toggle Tutorial Button")
  String toggleTutorialButton();

  @DefaultMessage("Add Screen ...")
  @Description("Label of the button for adding a new screen")
  String addFormButton();

  @DefaultMessage("Remove Screen")
  @Description("Label of the button for removing a screen")
  String removeFormButton();

  @DefaultMessage("Connect")
  @Description("Label of the button for selecting phone connection")
  String connectButton();

  @DefaultMessage("Deleting this screen will completely remove the screen from your project. " +
      "All components and blocks associated with this screen will be deleted.\n" +
      "There is no undo.\nAre you sure you want to delete {0}?")
  @Description("Confirmation query for removing a screen")
  String reallyDeleteForm(String formName);

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

  // Not used anymore it is now dynamically created and translated at compile time depending on what
  //languages are translated and available.

//  @DefaultMessage("English")
//  @Description("Label of the button for switching language to English")
//  String switchLanguageEnglishButton();
//
//  @DefaultMessage("Chinese CN")
//  @Description("Label of the button for switching language to Chinese CN")
//  String switchLanguageChineseCNButton();
//
//  @DefaultMessage("German")
//  @Description("Label of the button for switching language to German")
//  String switchLanguageGermanButton();
//
//  @DefaultMessage("Vietnamese")
//  @Description("Label of the button for switching language to Vietnamese")
//  String switchLanguageVietnameseButton();

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


  // Used in MotdFetcher.java

  @DefaultMessage("Failed to contact server to get the MOTD.")
  @Description("Message displayed when cannot get a MOTD from the server.")
  String getMotdFailed();

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

  @DefaultMessage("There is no Gallery App with the given id.")
  @Description("Error message if the gallery id does not exist")
  String galleryIdNotExist();

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

  @DefaultMessage("Import project (.aia) from my computer ...")
  @Description("Name of Import Project menuitem")
  String importProjectMenuItem();

  @DefaultMessage("Delete project")
  @Description("Name of Delete project menuitem")
  String deleteProjectMenuItem();

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
  @DefaultMessage("Components")
  @Description("Name of Components tab")
  String componentsTabName();

  @DefaultMessage("My components")
  @Description("Name of My components menuitem")
  String myComponentsMenuItem();

  @DefaultMessage("Start new component")
  @Description("Name of Start new component menuitem")
  String startNewComponentMenuItem();

  @DefaultMessage("Import component to project ...")
  @Description("Name of Import component menuitem")
  String importComponentMenuItem();

  @DefaultMessage("Import extension")
  @Description("String shown in the palette to import an extension")
  String importExtensionMenuItem();

  @DefaultMessage("Build component")
  @Description("Name of Build component menuitem")
  String buildComponentMenuItem();

  @DefaultMessage("Upload component (.aix) from my computer ...")
  @Description("Name of Upload component menuitem")
  String uploadComponentMenuItem();

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

  @DefaultMessage("Please select a component to import")
  @Description("Error message reported when no component is selected to import.")
  String noComponentSelectedError();

  @DefaultMessage("Unable to find component \"{0}\" while loading project \"{1}\".")
  @Description("Error message shown when a project references an unknown component.")
  String noComponentFound(String componentName, String projectName);

  @DefaultMessage("Please enter a url")
  @Description("Error message reported when no url is entered.")
  String noUrlError();

  @DefaultMessage("Name")
  @Description("Header for name column of component table")
  String componentNameHeader();

  @DefaultMessage("Version")
  @Description("Header for version column of component table")
  String componentVersionHeader();

  @DefaultMessage("Are you really sure you want to delete the component(s): {0}?")
  @Description("Confirmation message for deleting component(s)")
  String confirmDeleteComponents(String componentNames);

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

  @DefaultMessage("Emulator")
  @Description("Message providing details about starting the emulator connection.")
  String emulatorMenuItem();

  @DefaultMessage("Report an Issue")
  @Description("Link for Report an Issue form")
  String feedbackLink();

  @DefaultMessage("Gallery")
  @Description("Name of Gallery tab")
  String tabNameGallery();

  @DefaultMessage("Moderation")
  @Description("Name of moderation tab")
  String tabNameModeration();

  @DefaultMessage("Profile")
  @Description("Label of the link for private user profile")
  String privateProfileLink();

  @DefaultMessage("My Projects")
  @Description("Name of My Projects tab")
  String tabNameProjects();

  @DefaultMessage("USB")
  @Description("Message providing details about starting a USB connection.")
  String usbMenuItem();

  @DefaultMessage("Reset Connection")
  @Description("Reset all connections.")
  String resetConnectionsMenuItem();

  @DefaultMessage("Hard Reset")
  @Description("Hard Reset the Emulator.")
  String hardResetConnectionsMenuItem();

  //Build
  @DefaultMessage("Build")
  @Description("Label of the button leading to build related cascade items")
  String buildTabName();

  @DefaultMessage("App ( provide QR code for .apk )")
  @Description("Label of item for building a project and show barcode")
  String showBarcodeMenuItem();

  @DefaultMessage("App for Google Play ( provide QR code for .apk )")
  @Description("Label of item for building a project and show barcode")
  String showBarcodeMenuItem2();

  @DefaultMessage("App ( save .apk to my computer )")
  @Description("Label of item for building a project and downloading")
  String downloadToComputerMenuItem();

  @DefaultMessage("App for Google Play ( save .apk to my computer )")
  @Description("Label of item for building a project and downloading")
  String downloadToComputerMenuItem2();

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

  //Tabs
  @DefaultMessage("My Projects")
  @Description("Name of My Projects tab")
  String myProjectsTabName();

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

  //

  @DefaultMessage("Design")
  @Description("Name of Design tab")
  String tabNameDesign();

  @DefaultMessage("(Debugging)")
  @Description("Name of Debugging tab")
  String tabNameDebugging();

  @DefaultMessage("Please choose a project to open or create a new project.")
  @Description("Message shown when there is no current file editor to switch to")
  String chooseProject();

  @DefaultMessage("Emails")
  @Description("Title for user's email inbox")
  String emailInboxTitle();

  @DefaultMessage("Send an Email")
  @Description("Title for moderator send email dialog")
  String emailSendTitle();

  @DefaultMessage("My Components")
  @Description("Caption for component list box.")
  String componentListBoxCaption();

  // Used in boxes/AssetListBox.java

  @DefaultMessage("Media")
  @Description("Caption for asset list box.")
  String assetListBoxCaption();

  // Used in boxes/MessagesOutputBox.java

  @DefaultMessage("Messages")
  @Description("Caption for message output box.")
  String messagesOutputBoxCaption();

  // Used in boxes/MotdBox.java

  @DefaultMessage("Welcome to App Inventor!")
  @Description("Initial caption for MOTD box.")
  String motdBoxCaption();

  // Used in boxes/OdeLogBox.java

  @DefaultMessage("Developer Messages")
  @Description("Caption for ODE log box.")
  String odeLogBoxCaption();

  // Used in boxes/PaletteBox.java

  @DefaultMessage("Palette")
  @Description("Caption for palette box.")
  String paletteBoxCaption();

  // Used in boxes/ProjectListBox.java

  @DefaultMessage("My Projects")
  @Description("Caption for project list box.")
  String projectListBoxCaption();

  // Used in boxes/ProjectListBox.java

  @DefaultMessage("My Studios")
  @Description("Caption for studio list box.")
  String studioListBoxCaption();

  // Used in boxes/ProjectListBox.java

  @DefaultMessage("My Profile")
  @Description("Caption for profile page box.")
  String profilePageBoxCaption();

  // Used in boxes/ModerationPageBox.java

  @DefaultMessage("Reports")
  @Description("Caption for moderation page box.")
  String moderationPageBoxCaption();

  // Used in boxes/GalleryListBox.java

  @DefaultMessage("Gallery")
  @Description("Caption for gallery list box.")
  String galleryListBoxCaption();

  // Used in boxes/GalleryAppBox.java

  @DefaultMessage("Gallery App")
  @Description("Caption for gallery app box.")
  String galleryAppBoxCaption();

  // Used in boxes/UserProfileBox.java

  @DefaultMessage("User Profile")
  @Description("Caption for user profile box.")
  String userProfileBoxCaption();

  // Used in boxes/PropertiesBox.java

  @DefaultMessage("Properties")
  @Description("Caption for properties box.")
  String propertiesBoxCaption();

  // Used in boxes/SourceStructureBox.java

  @DefaultMessage("Components")
  @Description("Caption for source structure box.")
  String sourceStructureBoxCaption();

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

  @DefaultMessage("Check to see Preview on Tablet size.")
  @Description("Checkbox (check) controlling whether to display a preview on Tablet size.")
  String previewTabletSize();

  @DefaultMessage("Un-check to see Preview on Phone size.")
  @Description("Checkbox (un-check) controlling whether to display a preview on Phone size.")
  String previewPhoneSize();

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
      "Pattern, YailList, YailNumberToString, YailRuntimeError, abstract, continue, for, new, " +
      "switch, assert, default, goto, package, synchronized, boolean, do, if, private, this, break, " +
      "double, implements, protected, throw, byte, else, import, public, throws, case, enum, instanceof, " +
      "return, transient, catch, extends, int, short, try, char, final, interface, static, void, class, " +
      "finally, long, strictfp, volatile, const, float, native, super, while")
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

  @DefaultMessage("LegacyMode")
  @Description("")
  String LegacyModeProperties();

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

  @DefaultMessage("Percentage input values should be between 1 and 100")
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

  @DefaultMessage("Delete...")
  @Description("Label for the context menu command that deletes a file")
  String deleteFileCommand();

  @DefaultMessage("Download to my computer")
  @Description("Label for the context menu command that downloads a file")
  String downloadFileCommand();

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

  @DefaultMessage("Barcode link for {0}")
  @Description("Title of barcode dialog.")
  String barcodeTitle(String projectName);

  @DefaultMessage("Note: this barcode is only valid for 2 hours. See {0} the FAQ {1} for info " +
      "on how to share your app with others.")
  @Description("Warning in barcode dialog.")
  String barcodeWarning(String aTagStart, String aTagEnd);

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

  @DefaultMessage("Are you really sure you want to delete this project: {0}")
  @Description("Confirmation message for selecting a single project and clicking delete")
  String confirmDeleteSingleProject(String projectName);

  @DefaultMessage("Are you really sure you want to delete this project: {0}?"+
      " Note that the published copy of this project will be removed from the gallery as well.")
  @Description("Confirmation message for selecting a single project and clicking delete when gallery is on")
  String confirmDeleteSinglePublishedProject(String projectName);

  @DefaultMessage("Are you really sure you want to delete these projects: {0}?")
  @Description("Confirmation message for selecting multiple projects and clicking delete")
  String confirmDeleteManyProjects(String projectNames);

  @DefaultMessage("Are you really sure you want to delete these projects: {0}?"+
      " Note that if any of the projects have been published, the published version in"+
      " the gallery will be removed as well.")
  @Description("Confirmation message for selecting multiple projects and clicking delete when gallery is on")
  String confirmDeleteManyProjectsWithGalleryOn(String projectNames);

  @DefaultMessage("Server error: could not delete project. Please try again later!")
  @Description("Error message reported when deleting a project failed on the server.")
  String deleteProjectError();

  @DefaultMessage("One project must be selected.")
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

  @DefaultMessage("Server error: could not retrieve tutorial apps from gallery")
  @Description("Error message reported when can't get tutorial on server.")
  String galleryTutorialAppError();

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

  // Used in RemixedYoungAndroidProjectWizard.java
  @DefaultMessage("Loading App ...")
  @Description("loading indicator when opening the app")
  String loadingAppIndicatorText();

  // Used in ProfilePage.java
  @DefaultMessage("Edit Profile")
  @Description("Edit Profile Button, only seen by profile owner")
  String buttonEditProfile();

  @DefaultMessage("Update Profile")
  @Description("Update Profile Button, only seen by profile owner")
  String buttonUpdateProfile();

  @DefaultMessage("Edit your profile")
  @Description("label of editing your profile")
  String labelEditYourProfile();

  @DefaultMessage("Your display name")
  @Description("label of your display name")
  String labelYourDisplayName();

  @DefaultMessage("More info link")
  @Description("label of more info link")
  String labelMoreInfoLink();

  @DefaultMessage("App Inventor will send you a notification "
      + "when the apps you have posted are liked or downloaded. "
      + "Below, you can enable/disable this feature and you can "
      + "specify how often you want to be notified")
  @Description("label of email description")
  String labelEmailDescription();

  @DefaultMessage("Get email for every ")
  @Description("label of email frequency prefix")
  String labelEmailFrequencyPrefix();

  @DefaultMessage(" new Likes + Downloads")
  @Description("label of email frequency suffix")
  String labelEmailFrequencySuffix();

  @DefaultMessage("Invalid Email Frequency: Must be an numeric and greater than 0")
  @Description("error message of wrong email frequency")
  String errorEmailFrequency();

  // Used in GalleryList.java
  @DefaultMessage("Search for Apps")
  @Description("Search for Apps Text")
  String gallerySearchForAppsButton();

  @DefaultMessage("More Apps")
  @Description("More Apps Text")
  String galleryMoreApps();

  @DefaultMessage("search for \"")
  @Description("Search Results Prefix")
  String gallerySearchResultsPrefix();

  @DefaultMessage("\" returned ")
  @Description("Search Results Infix")
  String gallerySearchResultsInfix();

  @DefaultMessage(" results")
  @Description("Search Results Suffix")
  String gallerySearchResultsSuffix();

  @DefaultMessage("Server error: gallery cannot be accessed")
  @Description("Error message reported when the gallery cannot be accessed.")
  String galleryError();

  @DefaultMessage("No Results Found")
  @Description("Label feedback for no results found after searching.")
  String noResultsFound();

  // Used in GalleryClient.java

  @DefaultMessage("Server error: gallery settings could not be accessed")
  @Description("Error message reported when unable to extract gallery settings from xml.")
  String gallerySettingsError();

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

  // Used in wizards/youngandroid/RemixedYoungAndroidProjectWizard.java

  @DefaultMessage("Name this App Inventor project")
  @Description("Caption for the wizard to name the opening Young Android project")
  String remixedYoungAndroidProjectWizardCaption();

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

  @DefaultMessage("Project names cannot contain spaces")
  @Description("Error shown when user types space into project name.")
  String whitespaceProjectNameError();

  @DefaultMessage("Project names must begin with a letter")
  @Description("Error shown when user does not type letter as first character in project name.")
  String firstCharProjectNameError();

  @DefaultMessage("Invalid character. Project names can only contain letters, numbers, and underscores")
  @Description("Error shown when user types invalid character into project name.")
  String invalidCharProjectNameError();

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

  @DefaultMessage("Starting asset transfer to companion...")
  @Description("Message to display at the start of an asset transfer before any assets are sent")
  String startingAssetTransfer();

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

  @DefaultMessage("HTMLFormat")
  @Description("")
  String HTMLFormatProperties();

  // This error message is displayed as HTML

  @DefaultMessage("User Interface")
  @Description("")
  String userInterfaceComponentPallette();

  @DefaultMessage("Layout")
  @Description("")
  String layoutComponentPallette();

  @DefaultMessage("Media")
  @Description("")
  String mediaComponentPallette();

  @DefaultMessage("Drawing and Animation")
  @Description("")
  String drawingAndAnimationComponentPallette();

  @DefaultMessage("Maps")
  @Description("")
  String mapsComponentPallette();

  @DefaultMessage("Sensors")
  @Description("")
  String sensorsComponentPallette();

  @DefaultMessage("Social")
  @Description("")
  String socialComponentPallette();

  @DefaultMessage("Storage")
  @Description("")
  String storageComponentPallette();

  @DefaultMessage("For internal use only")
  @Description("")
  String forInternalUseOnlyComponentPallette();

  @DefaultMessage("Form")
  @Description("")
  String formComponentPallette();

  @DefaultMessage("Math")
  @Description("Label on built-in-Math-blocks branch of block selector tree")
  String builtinMathLabel();

  @DefaultMessage("Connectivity")
  @Description("")
  String connectivityComponentPallette();

  @DefaultMessage("LEGO\u00AE MINDSTORMS\u00AE")
  @Description("")
  String legoMindstormsComponentPallette();

  @DefaultMessage("Extension")
  @Description("")
  String extensionComponentPallette();

  @DefaultMessage("External Components")
  @Description("")
  String externalComponentPalette();

  @DefaultMessage("Experimental")
  @Description("")
  String experimentalComponentPallette();

  @DefaultMessage("For internal use only")
  @Description("")
  String internalUseComponentPallette();

  @DefaultMessage("Uninitialized")
  @Description("")
  String uninitializedComponentPallette();

  // UI Pallette
  @DefaultMessage("Button")
  @Description("")
  String buttonComponentPallette();

  @DefaultMessage("Canvas")
  @Description("")
  String canvasComponentPallette();

  @DefaultMessage("CheckBox")
  @Description("")
  String checkBoxComponentPallette();

  @DefaultMessage("Clock")
  @Description("")
  String clockComponentPallette();

  @DefaultMessage("DatePicker")
  @Description("")
  String datePickerComponentPallette();

  @DefaultMessage("Image")
  @Description("")
  String imageComponentPallette();

  @DefaultMessage("Label")
  @Description("")
  String labelComponentPallette();

  @DefaultMessage("ListPicker")
  @Description("")
  String listPickerComponentPallette();

  @DefaultMessage("ListView")
  @Description("")
  String listViewComponentPallette();

  @DefaultMessage("PasswordTextBox")
  @Description("")
  String passwordTextBoxComponentPallette();

  @DefaultMessage("Pedometer")
  @Description("")
  String pedometerComponentPallette();

  @DefaultMessage("Slider")
  @Description("")
  String sliderComponentPallette();

  @DefaultMessage("Spinner")
  @Description("")
  String spinnerComponentPallette();

  @DefaultMessage("TextBox")
  @Description("")
  String textBoxComponentPallette();

  @DefaultMessage("TimePicker")
  @Description("")
  String timePickerComponentPallette();

  @DefaultMessage("TinyDB")
  @Description("")
  String tinyDBComponentPallette();

  // Media Pallette
  @DefaultMessage("Camcorder")
  @Description("")
  String camcorderComponentPallette();

  @DefaultMessage("Camera")
  @Description("")
  String cameraComponentPallette();

  @DefaultMessage("ImagePicker")
  @Description("")
  String imagePickerComponentPallette();

  @DefaultMessage("Player")
  @Description("")
  String playerComponentPallette();

  @DefaultMessage("Sound")
  @Description("")
  String soundComponentPallette();

  @DefaultMessage("VideoPlayer")
  @Description("")
  String videoPlayerComponentPallette();

  @DefaultMessage("YandexTranslate")
  @Description("")
  String yandexTranslateComponentPallette();

  // Animation
  @DefaultMessage("Ball")
  @Description("")
  String ballComponentPallette();

  @DefaultMessage("ImageSprite")
  @Description("")
  String imageSpriteComponentPallette();

  // Social
  @DefaultMessage("ContactPicker")
  @Description("")
  String contactPickerComponentPallette();

  @DefaultMessage("EmailPicker")
  @Description("")
  String emailPickerComponentPallette();

  @DefaultMessage("PhoneCall")
  @Description("")
  String phoneCallComponentPallette();

  @DefaultMessage("PhoneNumberPicker")
  @Description("")
  String phoneNumberPickerComponentPallette();

  @DefaultMessage("PhoneStatus")
  @Description("")
  String phoneStatusComponentPallette();

  @DefaultMessage("Sharing")
  @Description("")
  String sharingComponentPallette();

  @DefaultMessage("Texting")
  @Description("")
  String textingComponentPallette();

  @DefaultMessage("Twitter")
  @Description("")
  String twitterComponentPallette();

  // Sensor
  @DefaultMessage("AccelerometerSensor")
  @Description("")
  String accelerometerSensorComponentPallette();

  @DefaultMessage("BarcodeScanner")
  @Description("")
  String barcodeScannerComponentPallette();

  @DefaultMessage("GyroscopeSensor")
  @Description("")
  String gyroscopeSensorComponentPallette();

  @DefaultMessage("LocationSensor")
  @Description("")
  String locationSensorComponentPallette();

  @DefaultMessage("MediaStore")
  @Description("")
  String mediaStoreComponentPallette();

  @DefaultMessage("NearField")
  @Description("")
  String nearFieldComponentPallette();

  @DefaultMessage("OrientationSensor")
  @Description("")
  String orientationSensorComponentPallette();

  // Screen Arrangement
  @DefaultMessage("HorizontalArrangement")
  @Description("")
  String horizontalArrangementComponentPallette();

  @DefaultMessage("HorizontalScrollArrangement")
  @Description("")
  String horizontalScrollArrangementComponentPallette();

  @DefaultMessage("TableArrangement")
  @Description("")
  String tableArrangementComponentPallette();

  @DefaultMessage("VerticalArrangement")
  @Description("")
  String verticalArrangementComponentPallette();

  @DefaultMessage("VerticalScrollArrangement")
  @Description("")
  String verticalScrollArrangementComponentPallette();

  // Lego Mindstorms NXT
  @DefaultMessage("NxtColorSensor")
  @Description("")
  String nxtColorSensorComponentPallette();

  @DefaultMessage("NxtDirectCommands")
  @Description("")
  String nxtDirectCommandsComponentPallette();

  @DefaultMessage("NxtDrive")
  @Description("")
  String nxtDriveComponentPallette();

  @DefaultMessage("NxtLightSensor")
  @Description("")
  String nxtLightSensorComponentPallette();

  @DefaultMessage("NxtSoundSensor")
  @Description("")
  String nxtSoundSensorComponentPallette();

  @DefaultMessage("NxtTouchSensor")
  @Description("")
  String nxtTouchSensorComponentPallette();

  @DefaultMessage("NxtUltrasonicSensor")
  @Description("")
  String nxtUltrasonicSensorComponentPallette();

  // Lego Mindstorms EV3
  @DefaultMessage("Ev3Commands")
  @Description("")
  String ev3CommandsComponentPallette();

  @DefaultMessage("Ev3UI")
  @Description("")
  String ev3UIComponentPallette();

  @DefaultMessage("Ev3Sound")
  @Description("")
  String ev3SoundComponentPallette();

  @DefaultMessage("Ev3Motors")
  @Description("")
  String ev3MotorsComponentPallette();

  @DefaultMessage("Ev3TouchSensor")
  @Description("")
  String ev3TouchSensorComponentPallette();

  @DefaultMessage("Ev3ColorSensor")
  @Description("")
  String ev3ColorSensorComponentPallette();

  @DefaultMessage("Ev3GyroSensor")
  @Description("")
  String ev3GyroSensorComponentPallette();

  @DefaultMessage("Ev3UltrasonicSensor")
  @Description("")
  String ev3UltrasonicSensorComponentPallette();

  // Storage
  @DefaultMessage("ActivityStarter")
  @Description("")
  String activityStarterComponentPallette();

  @DefaultMessage("BluetoothClient")
  @Description("")
  String bluetoothClientComponentPallette();

  @DefaultMessage("BluetoothServer")
  @Description("")
  String bluetoothServerComponentPallette();

  @DefaultMessage("Notifier")
  @Description("")
  String notifierComponentPallette();

  @DefaultMessage("SpeechRecognizer")
  @Description("")
  String speechRecognizerComponentPallette();

  @DefaultMessage("TextToSpeech")
  @Description("")
  String textToSpeechComponentPallette();

  @DefaultMessage("TinyWebDB")
  @Description("")
  String tinyWebDBComponentPallette();

  @DefaultMessage("Web")
  @Description("")
  String webComponentPallette();

  // Connectivity
  @DefaultMessage("File")
  @Description("")
  String fileComponentPallette();

  @DefaultMessage("FirebaseDB")
  @Description("")
  String firebaseDBComponentPallette();

  @DefaultMessage("FusiontablesControl")
  @Description("")
  String fusiontablesControlComponentPallette();

  @DefaultMessage("GameClient")
  @Description("")
  String gameClientComponentPallette();

  @DefaultMessage("password")
  @Description("")
  String passwordParams();

  @DefaultMessage("SoundRecorder")
  @Description("")
  String soundRecorderComponentPallette();

  @DefaultMessage("Voting")
  @Description("")
  String votingComponentPallette();

  @DefaultMessage("WebViewer")
  @Description("")
  String webViewerComponentPallette();

  // Component Properties
  @DefaultMessage("AboutScreen")
  @Description("")
  String AboutScreenProperties();

  @DefaultMessage("ShowStatusBar")
  @Description("")
  String ShowStatusBarProperties();

  @DefaultMessage("TitleVisible")
  @Description("")
  String TitleVisibleProperties();

  @DefaultMessage("AboveRangeEventEnabled")
  @Description("")
  String AboveRangeEventEnabledProperties();

  @DefaultMessage("Action")
  @Description("")
  String ActionProperties();

  @DefaultMessage("ActivityClass")
  @Description("")
  String ActivityClassProperties();

  @DefaultMessage("ActivityPackage")
  @Description("")
  String ActivityPackageProperties();

  @DefaultMessage("AlignHorizontal")
  @Description("")
  String AlignHorizontalProperties();

  @DefaultMessage("AlignVertical")
  @Description("")
  String AlignVerticalProperties();

  @DefaultMessage("AllowCookies")
  @Description("")
  String AllowCookiesProperties();

  @DefaultMessage("ApiKey")
  @Description("")
  String ApiKeyProperties();

  @DefaultMessage("AppName")
  @Description("")
  String AppNameProperties();

  @DefaultMessage("AvailableCountries")
  @Description("")
  String AvailableCountriesProperties();

  @DefaultMessage("AvailableLanguages")
  @Description("")
  String AvailableLanguagesProperties();

  @DefaultMessage("BackgroundColor")
  @Description("")
  String BackgroundColorProperties();

  @DefaultMessage("BackgroundImage")
  @Description("")
  String BackgroundImageProperties();

  @DefaultMessage("BelowRangeEventEnabled")
  @Description("")
  String BelowRangeEventEnabledProperties();

  @DefaultMessage("BluetoothClient")
  @Description("")
  String BluetoothClientProperties();

  @DefaultMessage("BottomOfRange")
  @Description("")
  String BottomOfRangeProperties();

  @DefaultMessage("CalibrateStrideLength")
  @Description("")
  String CalibrateStrideLengthProperties();

  @DefaultMessage("CharacterEncoding")
  @Description("")
  String CharacterEncodingProperties();

  @DefaultMessage("Checked")
  @Description("")
  String CheckedProperties();

  @DefaultMessage("CloseScreenAnimation")
  @Description("")
  String CloseScreenAnimationProperties();

  @DefaultMessage("ColorChangedEventEnabled")
  @Description("")
  String ColorChangedEventEnabledProperties();

  @DefaultMessage("Columns")
  @Description("")
  String ColumnsProperties();

  @DefaultMessage("ConsumerKey")
  @Description("")
  String ConsumerKeyProperties();

  @DefaultMessage("ConsumerSecret")
  @Description("")
  String ConsumerSecretProperties();

  @DefaultMessage("Country")
  @Description("")
  String CountryProperties();

  @DefaultMessage("DataType")
  @Description("")
  String DataTypeProperties();

  @DefaultMessage("DataUri")
  @Description("")
  String DataUriProperties();

  @DefaultMessage("DelimiterByte")
  @Description("")
  String DelimiterByteProperties();

  @DefaultMessage("DetectColor")
  @Description("")
  String DetectColorProperties();

  @DefaultMessage("DistanceInterval")
  @Description("")
  String DistanceIntervalProperties();

  @DefaultMessage("DriveMotors")
  @Description("")
  String DriveMotorsProperties();

  @DefaultMessage("Enabled")
  @Description("")
  String EnabledProperties();

  @DefaultMessage("ExtraKey")
  @Description("")
  String ExtraKeyProperties();

  @DefaultMessage("ExtraValue")
  @Description("")
  String ExtraValueProperties();

  @DefaultMessage("Extras")
  @Description("")
  String ExtrasProperties();

  @DefaultMessage("FollowLinks")
  @Description("")
  String FollowLinksProperties();

  @DefaultMessage("FontBold")
  @Description("")
  String FontBoldProperties();

  @DefaultMessage("FontItalic")
  @Description("")
  String FontItalicProperties();

  @DefaultMessage("FontSize")
  @Description("")
  String FontSizeProperties();

  @DefaultMessage("FontTypeface")
  @Description("")
  String FontTypefaceProperties();

  @DefaultMessage("GameId")
  @Description("")
  String GameIdProperties();

  @DefaultMessage("GenerateColor")
  @Description("")
  String GenerateColorProperties();

  @DefaultMessage("GenerateLight")
  @Description("")
  String GenerateLightProperties();

  @DefaultMessage("GoogleVoiceEnabled")
  @Description("")
  String GoogleVoiceEnabledProperties();

  @DefaultMessage("HasMargins")
  @Description("")
  String HasMarginsProperties();

  @DefaultMessage("Heading")
  @Description("")
  String HeadingProperties();

  @DefaultMessage("HighByteFirst")
  @Description("")
  String HighByteFirstProperties();

  @DefaultMessage("Hint")
  @Description("")
  String HintProperties();

  @DefaultMessage("HomeUrl")
  @Description("")
  String HomeUrlProperties();

  @DefaultMessage("Icon")
  @Description("")
  String IconProperties();

  @DefaultMessage("Instant")
  @Description("")
  String InstantProperties();

  @DefaultMessage("IgnoreSslErrors")
  @Description("")
  String IgnoreSslErrorsProperties();

  @DefaultMessage("Image")
  @Description("")
  String ImageProperties();

  @DefaultMessage("Interval")
  @Description("")
  String IntervalProperties();

  @DefaultMessage("IsLooping")
  @Description("")
  String IsLoopingProperties();

  @DefaultMessage("KeyFile")
  @Description("")
  String KeyFileProperties();

  @DefaultMessage("Language")
  @Description("")
  String LanguageProperties();

  @DefaultMessage("LineWidth")
  @Description("")
  String LineWidthProperties();

  @DefaultMessage("LoadingDialogMessage")
  @Description("")
  String LoadingDialogMessageProperties();

  @DefaultMessage("Message")
  @Description("")
  String MessageProperties();

  @DefaultMessage("MinimumInterval (ms)")
  @Description("")
  String MinimumIntervalProperties();

  @DefaultMessage("MultiLine")
  @Description("")
  String MultiLineProperties();

  @DefaultMessage("Namespace")
  @Description("")
  String NamespaceProperties();

  @DefaultMessage("NumbersOnly")
  @Description("")
  String NumbersOnlyProperties();

  @DefaultMessage("OpenScreenAnimation")
  @Description("")
  String OpenScreenAnimationProperties();

  @DefaultMessage("PaintColor")
  @Description("")
  String PaintColorProperties();

  @DefaultMessage("PhoneNumber")
  @Description("")
  String PhoneNumberProperties();

  @DefaultMessage("PhoneNumber")
  @Description("")
  String phoneNumberParams();

  @DefaultMessage("PhoneNumberList")
  @Description("")
  String PhoneNumberListProperties();

  @DefaultMessage("Picture")
  @Description("")
  String PictureProperties();

  @DefaultMessage("PressedEventEnabled")
  @Description("")
  String PressedEventEnabledProperties();

  @DefaultMessage("PromptforPermission")
  @Description("")
  String PromptforPermissionProperties();

  @DefaultMessage("Query")
  @Description("")
  String QueryProperties();

  @DefaultMessage("Radius")
  @Description("")
  String RadiusProperties();

  @DefaultMessage("ReadMode")
  @Description("")
  String ReadModeProperties();

  @DefaultMessage("ReceivingEnabled")
  @Description("")
  String ReceivingEnabledProperties();

  @DefaultMessage("ReleasedEventEnabled")
  @Description("")
  String ReleasedEventEnabledProperties();

  @DefaultMessage("ResponseFileName")
  @Description("")
  String ResponseFileNameProperties();

  @DefaultMessage("ResultName")
  @Description("")
  String ResultNameProperties();

  @DefaultMessage("Rows")
  @Description("")
  String RowsProperties();

  @DefaultMessage("SavedRecording")
  @Description("")
  String SavedRecordingProperties();

  @DefaultMessage("SaveResponse")
  @Description("")
  String SaveResponseProperties();

  @DefaultMessage("ScalePictureToFit")
  @Description("")
  String ScalePictureToFitProperties();

  @DefaultMessage("SensorPort")
  @Description("")
  String SensorPortProperties();

  @DefaultMessage("ScreenOrientation")
  @Description("")
  String ScreenOrientationProperties();

  @DefaultMessage("Secure")
  @Description("")
  String SecureProperties();

  @DefaultMessage("ServiceAccountEmail")
  @Description("")
  String ServiceAccountEmailProperties();

  @DefaultMessage("ServiceURL")
  @Description("")
  String ServiceURLProperties();

  @DefaultMessage("ShowLoadingDialog")
  @Description("")
  String ShowLoadingDialogProperties();

  @DefaultMessage("FirebaseURL")
  @Description("")
  String FirebaseURLProperties();

  @DefaultMessage("Persist")
  @Description("")
  String PersistProperties();

  @DefaultMessage("ProjectBucket")
  @Description("")
  String ProjectBucketProperties();

  @DefaultMessage("DeveloperBucket")
  @Description("")
  String DeveloperBucketProperties();

  @DefaultMessage("FirebaseToken")
  @Description("")
  String FirebaseTokenProperties();

  @DefaultMessage("PrivateUserStorage")
  @Description("")
  String PrivateUserStorageProperties();

  @DefaultMessage("Scrollable")
  @Description("")
  String ScrollableProperties();

  @DefaultMessage("Shape")
  @Description("")
  String ShapeProperties();

  @DefaultMessage("ShowFeedback")
  @Description("")
  String ShowFeedbackProperties();

  @DefaultMessage("show tables")
  @Description("")
  String ShowTablesProperties();

  @DefaultMessage("Source")
  @Description("")
  String SourceProperties();

  @DefaultMessage("Speed")
  @Description("")
  String SpeedProperties();

  @DefaultMessage("StopBeforeDisconnect")
  @Description("")
  String StopBeforeDisconnectProperties();

  @DefaultMessage("StopDetectionTimeout")
  @Description("")
  String StopDetectionTimeoutProperties();

  @DefaultMessage("StrideLength")
  @Description("")
  String StrideLengthProperties();

  @DefaultMessage("Text")
  @Description("")
  String TextProperties();

  @DefaultMessage("TextAlignment")
  @Description("")
  String TextAlignmentProperties();

  @DefaultMessage("TextColor")
  @Description("")
  String TextColorProperties();

  @DefaultMessage("TimerAlwaysFires")
  @Description("")
  String TimerAlwaysFiresProperties();

  @DefaultMessage("TimerEnabled")
  @Description("")
  String TimerEnabledProperties();

  @DefaultMessage("TimerInterval")
  @Description("")
  String TimerIntervalProperties();

  @DefaultMessage("Title")
  @Description("")
  String TitleProperties();

  @DefaultMessage("TopOfRange")
  @Description("")
  String TopOfRangeProperties();

  @DefaultMessage("Url")
  @Description("")
  String UrlProperties();

  @DefaultMessage("UseFront")
  @Description("")
  String UseFrontProperties();

  @DefaultMessage("UseGPS")
  @Description("")
  String UseGPSProperties();

  @DefaultMessage("UseServiceAuthentication")
  @Description("")
  String UseServiceAuthenticationProperties();

  @DefaultMessage("UsesLocationVisible")
  @Description("")
  String UsesLocationVisibleProperties();

  @DefaultMessage("VersionCode")
  @Description("")
  String VersionCodeProperties();

  @DefaultMessage("VersionName")
  @Description("")
  String VersionNameProperties();

  @DefaultMessage("TutorialURL")
  @Description("")
  String TutorialURLProperties();

  @DefaultMessage("Sizing")
  @Description("")
  String SizingProperties();

  @DefaultMessage("ShowListsAsJson")
  @Description("")
  String ShowListsAsJsonProperties();

  @DefaultMessage("Visible")
  @Description("")
  String VisibleProperties();

  @DefaultMessage("Volume")
  @Description("")
  String VolumeProperties();

  @DefaultMessage("WheelDiameter")
  @Description("")
  String WheelDiameterProperties();

  @DefaultMessage("WithinRangeEventEnabled")
  @Description("")
  String WithinRangeEventEnabledProperties();

  @DefaultMessage("X")
  @Description("")
  String XProperties();

  @DefaultMessage("Y")
  @Description("")
  String YProperties();

  @DefaultMessage("Z")
  @Description("")
  String ZProperties();

  @DefaultMessage("showing")
  @Description("")
  String VisibilityShowingProperties();

  @DefaultMessage("hidden")
  @Description("")
  String VisibilityHiddenProperties();

  @DefaultMessage("ElementsFromString")
  @Description("")
  String ElementsFromStringProperties();

  @DefaultMessage("Rotates")
  @Description("")
  String RotatesProperties();

  @DefaultMessage("RotationAngle")
  @Description("")
  String RotationAngleProperties();

  @DefaultMessage("Selection")
  @Description("")
  String SelectionProperties();

  @DefaultMessage("TimeInterval")
  @Description("")
  String TimeIntervalProperties();

  @DefaultMessage("UsesLocation")
  @Description("")
  String UsesLocationProperties();

  @DefaultMessage("ShowFilterBar")
  @Description("")
  String ShowFilterBarProperties();

  @DefaultMessage("TextSize")
  @Description("")
  String TextSizeProperties();

  @DefaultMessage("NotifierLength")
  @Description("")
  String NotifierLengthProperties();

  @DefaultMessage("Loop")
  @Description("")
  String LoopProperties();

  @DefaultMessage("Pitch")
  @Description("")
  String PitchProperties();

  @DefaultMessage("SpeechRate")
  @Description("")
  String SpeechRateProperties();

  @DefaultMessage("Sensitivity")
  @Description("")
  String SensitivityProperties();

  @DefaultMessage("TwitPic_API_Key")
  @Description("")
  String TwitPic_API_KeyProperties();

  @DefaultMessage("Prompt")
  @Description("")
  String PromptProperties();

  @DefaultMessage("ColorLeft")
  @Description("")
  String ColorLeftProperties();

  @DefaultMessage("ColorRight")
  @Description("")
  String ColorRightProperties();

  @DefaultMessage("MaxValue")
  @Description("")
  String MaxValueProperties();

  @DefaultMessage("MinValue")
  @Description("")
  String MinValueProperties();

  @DefaultMessage("ThumbPosition")
  @Description("")
  String ThumbPositionProperties();

  @DefaultMessage("ThumbEnabled")
  @Description("")
  String ThumbEnabled();

  @DefaultMessage("Day")
  @Description("")
  String DayProperties();

  @DefaultMessage("Month")
  @Description("")
  String MonthProperties();

  @DefaultMessage("MonthInText")
  @Description("")
  String MonthInTextProperties();

  @DefaultMessage("Year")
  @Description("")
  String YearProperties();

  @DefaultMessage("LastMessage")
  @Description("")
  String LastMessageProperties();

  @DefaultMessage("TextToWrite")
  @Description("")
  String TextToWriteProperties();

  @DefaultMessage("WriteType")
  @Description("")
  String WriteTypeProperties();

  @DefaultMessage("ElapsedTime")
  @Description("")
  String ElapsedTimeProperties();

  @DefaultMessage("SimpleSteps")
  @Description("")
  String SimpleStepsProperties();

  @DefaultMessage("WalkSteps")
  @Description("")
  String WalkStepsProperties();

  @DefaultMessage("Moving")
  @Description("")
  String MovingProperties();

  @DefaultMessage("Hour")
  @Description("")
  String HourProperties();

  @DefaultMessage("Minute")
  @Description("")
  String MinuteProperties();

  @DefaultMessage("Distance")
  @Description("")
  String DistanceProperties();

  @DefaultMessage("DirectMessages")
  @Description("")
  String DirectMessagesProperties();

  @DefaultMessage("ContactName")
  @Description("")
  String ContactNameProperties();

  @DefaultMessage("CurrentAddress")
  @Description("")
  String CurrentAddressProperties();

  @DefaultMessage("CurrentPageTitle")
  @Description("")
  String CurrentPageTitleProperties();

  @DefaultMessage("CurrentUrl")
  @Description("")
  String CurrentUrlProperties();

  @DefaultMessage("Accuracy")
  @Description("")
  String AccuracyProperties();

  @DefaultMessage("AddressesAndNames")
  @Description("")
  String AddressesAndNamesProperties();

  @DefaultMessage("Altitude")
  @Description("")
  String AltitudeProperties();

  @DefaultMessage("Angle")
  @Description("")
  String AngleProperties();

  @DefaultMessage("Animation")
  @Description("")
  String AnimationProperties();

  @DefaultMessage("Available")
  @Description("")
  String AvailableProperties();

  @DefaultMessage("AvailableProviders")
  @Description("")
  String AvailableProvidersProperties();

  @DefaultMessage("Azimuth")
  @Description("")
  String AzimuthProperties();

  @DefaultMessage("BallotOptions")
  @Description("")
  String BallotOptionsProperties();

  @DefaultMessage("BallotQuestion")
  @Description("")
  String BallotQuestionProperties();

  @DefaultMessage("ContactUri")
  @Description("")
  String ContactUriProperties();

  @DefaultMessage("EmailAddress")
  @Description("")
  String EmailAddressProperties();

  @DefaultMessage("EmailAddressList")
  @Description("")
  String EmailAddressListProperties();

  @DefaultMessage("Elements")
  @Description("")
  String ElementsProperties();

  @DefaultMessage("Followers")
  @Description("")
  String FollowersProperties();

  @DefaultMessage("FriendTimeline")
  @Description("")
  String FriendTimelineProperties();

  @DefaultMessage("FullScreen")
  @Description("")
  String FullScreenProperties();

  @DefaultMessage("HasAccuracy")
  @Description("")
  String HasAccuracyProperties();

  @DefaultMessage("HasAltitude")
  @Description("")
  String HasAltitudeProperties();

  @DefaultMessage("HasLongitudeLatitude")
  @Description("")
  String HasLongitudeLatitudeProperties();

  @DefaultMessage("Height")
  @Description("")
  String HeightProperties();

  @DefaultMessage("HeightPercent")
  @Description("")
  String HeightPercentProperties();

  @DefaultMessage("InstanceId")
  @Description("")
  String InstanceIdProperties();

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

  @DefaultMessage("username")
  @Description("")
  String usernameParams();

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

  @DefaultMessage("rate")
  @Description("")
  String rateParams();

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

  @DefaultMessage("InitializeValue")
  @Description("")
  String InitializeValueMethods();

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

  @DefaultMessage("getVersionName")
  @Description("")
  String getVersionNameMethods();

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

  @DefaultMessage("SetPower")
  @Description("")
  String SetPowerMethods();

  @DefaultMessage("StopSound")
  @Description("")
  String StopSoundMethods();

  //Mock Components
  @DefaultMessage("add items...")
  @Description("")
  String MockSpinnerAddItems();

  //help strings
  @DefaultMessage("Non-visible component that can detect shaking and measure acceleration approximately in three dimensions using SI units (m/s<sup>2</sup>).  The components are: <ul>\n<li> <strong>xAccel</strong>: 0 when the phone is at rest on a flat      surface, positive when the phone is tilted to the right (i.e.,      its left side is raised), and negative when the phone is tilted      to the left (i.e., its right size is raised).</li>\n <li> <strong>yAccel</strong>: 0 when the phone is at rest on a flat      surface, positive when its bottom is raised, and negative when      its top is raised. </li>\n <li> <strong>zAccel</strong>: Equal to -9.8 (earth\"s gravity in meters per      second per second when the device is at rest parallel to the ground      with the display facing up,      0 when perpendicular to the ground, and +9.8 when facing down.       The value can also be affected by accelerating it with or against      gravity. </li></ul>")
  @Description("")
  String AccelerometerSensorHelpStringComponentPallette();

  @DefaultMessage("A component that can launch an activity using the <code>StartActivity</code> method.<p>Activities that can be launched include: <ul> \n<li> starting other App Inventor for Android apps </li> \n<li> starting the camera application </li> \n<li> performing web search </li> \n<li> opening a browser to a specified web page</li> \n<li> opening the map application to a specified location</li></ul> \nYou can also launch activities that return text data.  See the documentation on using the Activity Starter for examples.</p>")
  @Description("")
  String ActivityStarterHelpStringComponentPallette();

  @DefaultMessage("<p>A round \"sprite\" that can be placed on a <code>Canvas</code>, where it can react to touches and drags, interact with other sprites (<code>ImageSprite</code>s and other <code>Ball</code>s) and the edge of the Canvas, and move according to its property values.</p><p>For example, to have a <code>Ball</code> move 4 pixels toward the top of a <code>Canvas</code> every 500 milliseconds (half second), you would set the <code>Speed</code> property to 4 [pixels], the <code>Interval</code> property to 500 [milliseconds], the <code>Heading</code> property to 90 [degrees], and the <code>Enabled</code> property to <code>True</code>.  These and its other properties can be changed at any time.</p><p>The difference between a Ball and an <code>ImageSprite</code> is that the latter can get its appearance from an image file, while a Ball\"s appearance can only be changed by varying its <code>PaintColor</code> and <code>Radius</code> properties.</p>")
  @Description("")
  String BallHelpStringComponentPallette();

  @DefaultMessage("Component for using the Barcode Scanner to read a barcode")
  @Description("")
  String BarcodeScannerHelpStringComponentPallette();

  @DefaultMessage("Bluetooth client component")
  @Description("")
  String BluetoothClientHelpStringComponentPallette();

  @DefaultMessage("Bluetooth server component")
  @Description("")
  String BluetoothServerHelpStringComponentPallette();

  @DefaultMessage("Button with the ability to detect clicks.  Many aspects of its appearance can be changed, as well as whether it is clickable (<code>Enabled</code>), can be changed in the Designer or in the Blocks Editor.")
  @Description("")
  String ButtonHelpStringComponentPallette();

  @DefaultMessage("A component to record a video using the device\"s camcorder.After the video is recorded, the name of the file on the phone containing the clip is available as an argument to the AfterRecording event. The file name can be used, for example, to set the source property of a VideoPlayer component.")
  @Description("")
  String CamcorderHelpStringComponentPallette();

  @DefaultMessage("A component to take a picture using the device\"s camera. After the picture is taken, the name of the file on the phone containing the picture is available as an argument to the AfterPicture event. The file name can be used, for example, to set the Picture property of an Image component.")
  @Description("")
  String CameraHelpStringComponentPallette();

  @DefaultMessage("<p>A two-dimensional touch-sensitive rectangular panel on which drawing can be done and sprites can be moved.</p> <p>The <code>BackgroundColor</code>, <code>PaintColor</code>, <code>BackgroundImage</code>, <code>Width</code>, and <code>Height</code> of the Canvas can be set in either the Designer or in the Blocks Editor.  The <code>Width</code> and <code>Height</code> are measured in pixels and must be positive.</p><p>Any location on the Canvas can be specified as a pair of (X, Y) values, where <ul> <li>X is the number of pixels away from the left edge of the Canvas</li><li>Y is the number of pixels away from the top edge of the Canvas</li></ul>.</p> <p>There are events to tell when and where a Canvas has been touched or a <code>Sprite</code> (<code>ImageSprite</code> or <code>Ball</code>) has been dragged.  There are also methods for drawing points, lines, and circles.</p>")
  @Description("")
  String CanvasHelpStringComponentPallette();

  @DefaultMessage("Checkbox that raises an event when the user clicks on it. There are many properties affecting its appearance that can be set in the Designer or Blocks Editor.")
  @Description("")
  String CheckBoxHelpStringComponentPallette();

  @DefaultMessage("Non-visible component that provides the instant in time "
    + "using the internal clock on the phone. It can fire a timer at "
    + "regularly set intervals and perform time calculations, "
    + "manipulations, and conversions.</p> <p>Methods to convert an "
    + "instant to text are also available. Acceptable patterns are "
    + "empty string, MM/dd/YYYY hh:mm:ss a, or MMM d, yyyy "
    + "HH:mm. The empty string will provide the default format, "
    + "which is \"MMM d, yyyy hh:mm:ss a\" for FormatDateTime \"MMM "
    + "d, yyyy\" for FormatDate.  To see all possible format, "
    + "please see <a "
    + "href=\"https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html\" "
    + "target=\"_blank\"> here</a>.")
  @Description("")
  String ClockHelpStringComponentPallette();

  @DefaultMessage("A button that, when clicked on, displays a list of the contacts to choose among. After the user has made a selection, the following properties will be set to information about the chosen contact: <ul>\n<li> <code>ContactName</code>: the contact's name </li>\n <li> <code>EmailAddress</code>: the contact's primary email address </li>\n <li> <code>ContactUri</code>: the contact's URI on the device </li>\n <li> <code>Picture</code>: the name of the file containing the contact's image, which can be used as a <code>Picture</code> property value for the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>\n</p><p>Other properties affect the appearance of the button (<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and whether it can be clicked on (<code>Enabled</code>).\n</p><p>Picking is not supported on all phones.  If it fails, this component will show a notification.  The error behavior can be overridden with the Screen.ErrorOccurred event handler.")
  @Description("")
  String ContactPickerHelpStringComponentPallette();

  @DefaultMessage("<p>A button that, when clicked on, launches a popup dialog to allow the user to select a date.</p>")
  @Description("")
  String DatePickerHelpStringComponentPallette();

  @DefaultMessage("An EmailPicker is a kind of text box.  If the user begins entering the name or email address of a contact, the phone will show a dropdown menu of choices that complete the entry.  If there are many contacts, the dropdown can take several seconds to appear, and can show intermediate results while the matches are being computed. <p>The initial contents of the text box and the contents< after user entry is in the <code>Text</code> property.  If the <code>Text</code> property is initially empty, the contents of the <code>Hint</code> property will be faintly shown in the text box as a hint to the user.</p>\n <p>Other properties affect the appearance of the text box (<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and whether it can be used (<code>Enabled</code>).</p>\n<p>Text boxes like this are usually used with <code>Button</code> components, with the user clicking on the button when text entry is complete.")
  @Description("")
  String EmailPickerHelpStringComponentPallette();

  @DefaultMessage("A component that provides both high- and low-level interfaces to control the motors on LEGO MINDSTORMS EV3.")
  @Description("")
  String Ev3MotorsHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a touch sensor on a LEGO MINDSTORMS EV3 robot.")
  @Description("")
  String Ev3TouchSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a gyro sensor on a LEGO MINDSTORMS EV3 robot.")
  @Description("")
  String Ev3GyroSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a color sensor on a LEGO MINDSTORMS EV3 robot.")
  @Description("")
  String Ev3ColorSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to an ultrasonic sensor on a LEGO MINDSTORMS EV3 robot.")
  @Description("")
  String Ev3UltrasonicSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a low-level interface to a LEGO MINDSTORMS EV3 robot, with functions to send system or direct commands to EV3 robots.")
  @Description("")
  String Ev3CommandsHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a LEGO MINDSTORMS EV3 robot, which provides graphic functionalities.")
  @Description("")
  String Ev3UIHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to sound functionalities on LEGO MINDSTORMS EV3 robot.")
  @Description("")
  String Ev3SoundHelpStringComponentPallette();

  @DefaultMessage("Non-visible component for storing and retrieving files. Use this component to write or read files on your device. The default behaviour is to write files to the private data directory associated with your App. The Companion is special cased to write files to /sdcard/AppInventor/data to facilitate debugging. If the file path starts with a slash (/), then the file is created relative to /sdcard. For example writing a file to /myFile.txt will write the file in /sdcard/myFile.txt.")
  @Description("")
  String FileHelpStringComponentPallette();

  @DefaultMessage("Top-level component containing all other components in the program")
  @Description("")
  String FormHelpStringComponentPallette();

  @DefaultMessage("<p>A non-visible component that communicates with Google Fusion Tables.  Fusion Tables let you store, share, query and visualize data tables; this component lets you query, create, and modify these tables.</p> <p>This component uses the <a href=\"https://developers.google.com/fusiontables/docs/v2/getting_started\" target=\"_blank\">Fusion Tables API V2.0</a>.  <p>Applications using Fusion Tables must authentication to Google\"s servers. There are two ways this can be done. The first way uses an API Key which you the developer obtain (see below). With this approach end-users must also login to access a Fusion Table.  The second approach is to use a Service Account. With this approach you create credentials and a special \"Service Account Email Address\" which you obtain from the <a href=\"https://code.google.com/apis/console/\" target=\"_blank\">Google APIs Console</a>.  You then tell the Fusion Table Control the name of the Service Account Email address and upload the secret key as an asset to your application and set the KeyFile property to point at this file. Finally you check the \"UseServiceAuthentication\" checkbox in the designer.  When using a Service Account, end-users do not need to login to use Fusion Tables, your service account authenticates all access.</p> <p>To get an API key, follow these instructions.</p> <ol> <li>Go to your <a href=\"https://code.google.com/apis/console/\" target=\"_blank\">Google APIs Console</a> and login if necessary.</li> <li>Select the <i>Services</i> item from the menu on the left.</li> <li>Choose the <i>Fusiontables</i> service from the list provided and turn it on.</li> <li>Go back to the main menu and select the <i>API Access</i> item. </li> </ol> <p>Your API Key will be near the bottom of that pane in the section called \"Simple API Access\".  You will have to provide that key as the value for the <i>ApiKey</i> property in your Fusiontables app.</p> <p>Once you have an API key, set the value of the <i>Query</i> property to a valid Fusiontables SQL query and call <i>SendQuery</i> to execute the query.  App Inventor will send the query to the Fusion Tables server and the <i>GotResult</i> block will fire when a result is returned from the server.  Query results will be returned in CSV format, and can be converted to list format using the \"list from csv table\" or \"list from csv row\" blocks.</p> <p>Note that you do not need to worry about UTF-encoding the query.  But you do need to make sure the query follows the syntax described in <a href=\"https://developers.google.com/fusiontables/docs/v2/getting_started\" target=\"_blank\">the reference manual</a>, which means that things like capitalization for names of columns matters, and that single quotes must be used around column names if there are spaces in them.</p>")
  @Description("")
  String FusiontablesControlHelpStringComponentPallette();

  @DefaultMessage("Provides a way for applications to communicate with online game servers")
  @Description("")
  String GameClientHelpStringComponentPallette();

  @DefaultMessage("<p>Non-visible component that can measure angular velocity in three dimensions in units of degrees per second.</p><p>In order to function, the component must have its <code>Enabled</code> property set to True, and the device must have a gyroscope sensor.</p>")
  @Description("")
  String GyroscopeSensorHelpStringComponentPallette();

  @DefaultMessage("<p>A formatting element in which to place components that should be displayed from left to right.  If you wish to have components displayed one over another, use <code>VerticalArrangement</code> instead.</p>")
  @Description("")
  String HorizontalArrangementHelpStringComponentPallette();

  @DefaultMessage("<p>A formatting element in which to place components that should be displayed from left to right.  If you wish to have components displayed one over another, use <code>VerticalArrangement</code> instead.</p><p>This version is scrollable.,")
  @Description("")
  String HorizontalScrollArrangementHelpStringComponentPallette();

  @DefaultMessage("Component for displaying images.  The picture to display, and other aspects of the Image\"s appearance, can be specified in the Designer or in the Blocks Editor.")
  @Description("")
  String ImageHelpStringComponentPallette();

  @DefaultMessage("A special-purpose button. When the user taps an image picker, the device\"s image gallery appears, and the user can choose an image. After an image is picked, it is saved, and the <code>Selected</code> property will be the name of the file where the image is stored. In order to not fill up storage, a maximum of 10 images will be stored.  Picking more images will delete previous images, in order from oldest to newest.")
  @Description("")
  String ImagePickerHelpStringComponentPallette();

  @DefaultMessage("<p>A \"sprite\" that can be placed on a <code>Canvas</code>, where it can react to touches and drags, interact with other sprites (<code>Ball</code>s and other <code>ImageSprite</code>s) and the edge of the Canvas, and move according to its property values.  Its appearance is that of the image specified in its <code>Picture</code> property (unless its <code>Visible</code> property is <code>False</code>.</p> <p>To have an <code>ImageSprite</code> move 10 pixels to the left every 1000 milliseconds (one second), for example, you would set the <code>Speed</code> property to 10 [pixels], the <code>Interval</code> property to 1000 [milliseconds], the <code>Heading</code> property to 180 [degrees], and the <code>Enabled</code> property to <code>True</code>.  A sprite whose <code>Rotates</code> property is <code>True</code> will rotate its image as the sprite\"s <code>Heading</code> changes.  Checking for collisions with a rotated sprite currently checks the sprite\"s unrotated position so that collision checking will be inaccurate for tall narrow or short wide sprites that are rotated.  Any of the sprite properties can be changed at any time under program control.</p> ")
  @Description("")
  String ImageSpriteHelpStringComponentPallette();

  @DefaultMessage("A Label displays a piece of text, which is specified through the <code>Text</code> property.  Other properties, all of which can be set in the Designer or Blocks Editor, control the appearance and placement of the text.")
  @Description("")
  String LabelHelpStringComponentPallette();

  @DefaultMessage("<p>A button that, when clicked on, displays a list of texts for the user to choose among. The texts can be specified through the Designer or Blocks Editor by setting the <code>ElementsFromString</code> property to their string-separated concatenation (for example, <em>choice 1, choice 2, choice 3</em>) or by setting the <code>Elements</code> property to a List in the Blocks editor.</p><p>Setting property ShowFilterBar to true, will make the list searchable.  Other properties affect the appearance of the button (<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and whether it can be clicked on (<code>Enabled</code>).</p>")
  @Description("")
  String ListPickerHelpStringComponentPallette();

  @DefaultMessage("<p>This is a visible component that allows to place a list of text elements in your Screen to display. <br> The list can be set using the ElementsFromString property or using the Elements block in the blocks editor.</p>")
  @Description("")
  String ListViewHelpStringComponentPallette();

  @DefaultMessage("Non-visible component providing location information, including longitude, latitude, altitude (if supported by the device), speed (if supported by the device), and address.  This can also perform \"geocoding\", converting a given address (not necessarily the current one) to a latitude (with the <code>LatitudeFromAddress</code> method) and a longitude (with the <code>LongitudeFromAddress</code> method).</p>\n <p>In order to function, the component must have its <code>Enabled</code> property set to True, and the device must have location sensing enabled through wireless networks or GPS satellites (if outdoors).</p>\nLocation information might not be immediately available when an app starts.  You''ll have to wait a short time for a location provider to be found and used, or wait for the OnLocationChanged event")
  @Description("")
  String LocationSensorHelpStringComponentPallette();

  @DefaultMessage("<p>The MediaStore component communicates with a web service to store media objects. This component has a single method that stores a media object in the services blob store, and returns a pointer to the object via a url.</p>")
  @Description("")
  String MediaStoreHelpStringComponentPallette();

  @DefaultMessage("<p>Non-visible component to provide NFC capabilities.  For now this component supports the reading and writing of text tags only (if supported by the device)</p><p>In order to read and write text tags, the component must have its <code>ReadMode</code> property set to True or False respectively.</p><p><strong>Note:</strong> This component will only work on Screen1 of any App Inventor app.</p>")
  @Description("")
  String NearFieldHelpStringComponentPallette();

  @DefaultMessage("The Notifier component displays alert dialogs, messages, and temporary alerts, and creates Android log entries through the following methods: <ul><li> ShowMessageDialog: displays a message which the user must dismiss by pressing a button.</li><li> ShowChooseDialog: displays a message two buttons to let the user choose one of two responses, for example, yes or no, after which the AfterChoosing event is raised.</li><li> ShowTextDialog: lets the user enter text in response to the message, after which the AfterTextInput event is raised. <li> ShowAlert: displays a temporary  alert that goes away by itself after a short time.</li><li> ShowProgressDialog: displays an alert with a loading spinner that cannot be dismissed by the user. It can only be dismissed by using the DismissProgressDialog block.</li><li> DismissProgressDialog: Dismisses the progress dialog displayed by ShowProgressDialog.</li><li> LogError: logs an error message to the Android log. </li><li> LogInfo: logs an info message to the Android log.</li><li> LogWarning: logs a warning message to the Android log.</li><li>The messages in the dialogs (but not the alert) can be formatted using the following HTML tags:&lt;b&gt;, &lt;big&gt;, &lt;blockquote&gt;, &lt;br&gt;, &lt;cite&gt;, &lt;dfn&gt;, &lt;div&gt;, &lt;em&gt;, &lt;small&gt;, &lt;strong&gt;, &lt;sub&gt;, &lt;sup&gt;, &lt;tt&gt;. &lt;u&gt;</li><li>You can also use the font tag to specify color, for example, &lt;font color=\"blue\"&gt;.  Some of the available color names are aqua, black, blue, fuchsia, green, grey, lime, maroon, navy, olive, purple, red, silver, teal, white, and yellow</li></ul>")
  @Description("")
  String NotifierHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a color sensor on a LEGO MINDSTORMS NXT robot.")
  @Description("")
  String NxtColorSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a low-level interface to a LEGO MINDSTORMS NXT robot, with functions to send NXT Direct Commands.")
  @Description("")
  String NxtDirectCommandsHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a LEGO MINDSTORMS NXT robot, with functions that can move and turn the robot.")
  @Description("")
  String NxtDriveHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a light sensor on a LEGO MINDSTORMS NXT robot.")
  @Description("")
  String NxtLightSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a sound sensor on a LEGO MINDSTORMS NXT robot.")
  @Description("")
  String NxtSoundSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to a touch sensor on a LEGO MINDSTORMS NXT robot.")
  @Description("")
  String NxtTouchSensorHelpStringComponentPallette();

  @DefaultMessage("A component that provides a high-level interface to an ultrasonic sensor on a LEGO MINDSTORMS NXT robot.")
  @Description("")
  String NxtUltrasonicSensorHelpStringComponentPallette();

  @DefaultMessage("<p>Non-visible component providing information about the device\"s physical orientation in three dimensions: <ul> <li> <strong>Roll</strong>: 0 degrees when the device is level, increases to      90 degrees as the device is tilted up on its left side, and      decreases to -90 degrees when the device is tilted up on its right side.      </li> <li> <strong>Pitch</strong>: 0 degrees when the device is level, up to      90 degrees as the device is tilted so its top is pointing down,      up to 180 degrees as it gets turned over.  Similarly, as the device      is tilted so its bottom points down, pitch decreases to -90      degrees, then further decreases to -180 degrees as it gets turned all the way      over.</li> <li> <strong>Azimuth</strong>: 0 degrees when the top of the device is      pointing north, 90 degrees when it is pointing east, 180 degrees      when it is pointing south, 270 degrees when it is pointing west,      etc.</li></ul>     These measurements assume that the device itself is not moving.</p>")
  @Description("")
  String OrientationSensorHelpStringComponentPallette();

  @DefaultMessage("<p>A box for entering passwords.  This is the same as the ordinary <code>TextBox</code> component except this does not display the characters typed by the user.</p><p>The value of the text in the box can be found or set through the <code>Text</code> property. If blank, the <code>Hint</code> property, which appears as faint text in the box, can provide the user with guidance as to what to type.</p> <p>Text boxes are usually used with the <code>Button</code> component, with the user clicking on the button when text entry is complete.</p>")
  @Description("")
  String PasswordTextBoxHelpStringComponentPallette();

  @DefaultMessage("A Component that acts like a Pedometer. It senses motion via the " +
    "Accerleromter and attempts to determine if a step has been " +
    "taken. Using a configurable stride length, it can estimate the " +
    "distance traveled as well. ")
  @Description("")
  String PedometerHelpStringComponentPallette();

  @DefaultMessage("<p>A non-visible component that makes a phone call to the number specified in the <code>PhoneNumber</code> property, which can be set either in the Designer or Blocks Editor. The component has a <code>MakePhoneCall</code> method, enabling the program to launch a phone call.</p><p>Often, this component is used with the <code>ContactPicker</code> component, which lets the user select a contact from the ones stored on the phone and sets the <code>PhoneNumber</code> property to the contact\"s phone number.</p><p>To directly specify the phone number (e.g., 650-555-1212), set the <code>PhoneNumber</code> property to a Text with the specified digits (e.g., \"6505551212\").  Dashes, dots, and parentheses may be included (e.g., \"(650)-555-1212\") but will be ignored; spaces may not be included.</p>")
  @Description("")
  String PhoneCallHelpStringComponentPallette();

  @DefaultMessage("A button that, when clicked on, displays a list of the contacts\" phone numbers to choose among. After the user has made a selection, the following properties will be set to information about the chosen contact: <ul>\n<li> <code>ContactName</code>: the contact\"s name </li>\n <li> <code>PhoneNumber</code>: the contact\"s phone number </li>\n <li> <code>EmailAddress</code>: the contact\"s email address </li> <li> <code>Picture</code>: the name of the file containing the contact\"s image, which can be used as a <code>Picture</code> property value for the <code>Image</code> or <code>ImageSprite</code> component.</li></ul>\n</p><p>Other properties affect the appearance of the button (<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and whether it can be clicked on (<code>Enabled</code>).</p>\n<p>Picking is not supported on all phones.  If it fails, this component will show a notification.  This default error behavior can be overridden with the Screen.ErrorOccurred event handler.")
  @Description("")
  String PhoneNumberPickerHelpStringComponentPallette();

  @DefaultMessage("Component that returns information about the phone.")
  @Description("")
  String PhoneStatusHelpStringComponentPallette();

  @DefaultMessage("Multimedia component that plays audio and controls phone vibration.  The name of a multimedia field is specified in the <code>Source</code> property, which can be set in the Designer or in the Blocks Editor.  The length of time for a vibration is specified in the Blocks Editor in milliseconds (thousandths of a second).\n<p>For supported audio formats, see <a href=\"http://developer.android.com/guide/appendix/media-formats.html\" target=\"_blank\">Android Supported Media Formats</a>.</p>\n<p>This component is best for long sound files, such as songs, while the <code>Sound</code> component is more efficient for short files, such as sound effects.</p>")
  @Description("")
  String PlayerHelpStringComponentPallette();

  @DefaultMessage("Sharing is a non-visible component that enables sharing files and/or messages between your app and other apps installed on a device. The component will display a list of the installed apps that can handle the information provided, and will allow the user to choose one to share the content with, for instance a mail app, a social network app, a texting app, and so on.<br>The file path can be taken directly from other components such as the Camera or the ImagePicker, but can also be specified directly to read from storage. Be aware that different devices treat storage differently, so a few things to try if, for instance, you have a file called arrow.gif in the folder <code>Appinventor/assets</code>, would be: <ul><li><code>\"file:///sdcard/Appinventor/assets/arrow.gif\"</code></li> or <li><code>\"/storage/Appinventor/assets/arrow.gif\"</code></li></ul>")
  @Description("")
  String SharingHelpStringComponentPallette();

  @DefaultMessage("A Slider is a progress bar that adds a draggable thumb. You can touch " +
        "the thumb and drag left or right to set the slider thumb position. " +
        "As the Slider thumb is dragged, it will trigger the PositionChanged event, " +
        "reporting the position of the Slider thumb. The reported position of the " +
        "Slider thumb can be used to dynamically update another component " +
        "attribute, such as the font size of a TextBox or the radius of a Ball.")
  @Description("")
  String SliderHelpStringComponentPallette();

  @DefaultMessage("<p>A multimedia component that plays sound files and optionally vibrates for the number of milliseconds (thousandths of a second) specified in the Blocks Editor.  The name of the sound file to play can be specified either in the Designer or in the Blocks Editor.</p> <p>For supported sound file formats, see <a href=\"http://developer.android.com/guide/appendix/media-formats.html\" target=\"_blank\">Android Supported Media Formats</a>.</p><p>This <code>Sound</code> component is best for short sound files, such as sound effects, while the <code>Player</code> component is more efficient for longer sounds, such as songs.</p>")
  @Description("")
  String SoundHelpStringComponentPallette();

  @DefaultMessage("<p>Multimedia component that records audio.</p>")
  @Description("")
  String SoundRecorderHelpStringComponentPallette();

  @DefaultMessage("Component for using Voice Recognition to convert from speech to text")
  @Description("")
  String SpeechRecognizerHelpStringComponentPallette();

  @DefaultMessage("<p>A spinner component that displays a pop-up with a list of elements. These elements can be set in the Designer or Blocks Editor by setting the<code>ElementsFromString</code> property to a string-separated concatenation (for example, <em>choice 1, choice 2, choice 3</em>) or by setting the <code>Elements</code> property to a List in the Blocks editor.</p>")
  @Description("")
  String SpinnerHelpStringComponentPallette();

  @DefaultMessage("<p>A formatting element in which to place components that should be displayed in tabular form.</p>")
  @Description("")
  String TableArrangementHelpStringComponentPallette();

  @DefaultMessage("<p>A box for the user to enter text.  The initial or user-entered text value is in the <code>Text</code> property.  If blank, the <code>Hint</code> property, which appears as faint text in the box, can provide the user with guidance as to what to type.</p><p>The <code>MultiLine</code> property determines if the text can havemore than one line.  For a single line text box, the keyboard will closeautomatically when the user presses the Done key.  To close the keyboard for multiline text boxes, the app should use  the HideKeyboard method or  rely on the user to press the Back key.</p><p>The <code> NumbersOnly</code> property restricts the keyboard to acceptnumeric input only.</p><p>Other properties affect the appearance of the text box (<code>TextAlignment</code>, <code>BackgroundColor</code>, etc.) and whether it can be used (<code>Enabled</code>).</p><p>Text boxes are usually used with the <code>Button</code> component, with the user clicking on the button when text entry is complete.</p><p>If the text entered by the user should not be displayed, use <code>PasswordTextBox</code> instead.</p>")
  @Description("")
  String TextBoxHelpStringComponentPallette();

  @DefaultMessage("Component for using TextToSpeech to speak a message")
  @Description("")
  String TextToSpeechHelpStringComponentPallette();

  @DefaultMessage("<p>A component that will, when the <code>SendMessage</code> method is called, send the text message specified in the <code>Message</code> property to the phone number specified in the <code>PhoneNumber</code> property.</p> <p>If the <code>ReceivingEnabled</code> property is set to 1 messages will <b>not</b> be received. If <code>ReceivingEnabled</code> is set to 2 messages will be received only when the application is running. Finally if <code>ReceivingEnabled</code> is set to 3, messages will be received when the application is running <b>and</b> when the application is not running they will be queued and a notification displayed to the user.</p> <p>When a message arrives, the <code>MessageReceived</code> event is raised and provides the sending number and message.</p> <p> An app that includes this component will receive messages even when it is in the background (i.e. when it is not visible on the screen) and, moreso, even if the app is not running, so long as it is installed on the phone. If the phone receives a text message when the app is not in the foreground, the phone will show a notification in the notification bar.  Selecting the notification will bring up the app.  As an app developer, you will probably want to give your users the ability to control ReceivingEnabled so that they can make the phone ignore text messages.</p> <p>If the GoogleVoiceEnabled property is true, messages can be sent over Wifi using Google Voice. This option requires that the user have a Google Voice account and that the mobile Voice app is installed on the phone. The Google Voice option works only on phones that support Android 2.0 (Eclair) or higher.</p> <p>To specify the phone number (e.g., 650-555-1212), set the <code>PhoneNumber</code> property to a Text string with the specified digits (e.g., 6505551212).  Dashes, dots, and parentheses may be included (e.g., (650)-555-1212) but will be ignored; spaces may not be included.</p> <p>Another way for an app to specify a phone number would be to include a <code>PhoneNumberPicker</code> component, which lets the users select a phone numbers from the ones stored in the the phone contacts.</p>")
  @Description("")
  String TextingHelpStringComponentPallette();

  @DefaultMessage("<p>A button that, when clicked on, launches  a popup dialog to allow the user to select a time.</p>")
  @Description("")
  String TimePickerHelpStringComponentPallette();

  @DefaultMessage("TinyDB is a non-visible component that stores data for an app. <p> Apps created with App Inventor are initialized each time they run: If an app sets the value of a variable and the user then quits the app, the value of that variable will not be remembered the next time the app is run. In contrast, TinyDB is a <em> persistent </em> data store for the app, that is, the data stored there will be available each time the app is run. An example might be a game that saves the high score and retrieves it each time the game is played. </<p> <p> Data items are strings stored under <em>tags</em> . To store a data item, you specify the tag it should be stored under.  Subsequently, you can retrieve the data that was stored under a given tag. </p><p> There is only one data store per app. Even if you have multiple TinyDB components, they will use the same data store. To get the effect of separate stores, use different keys. Also each app has its own data store. You cannot use TinyDB to pass data between two different apps on the phone, although you <em>can</em> use TinyDb to shares data between the different screens of a multi-screen app. </p> <p>When you are developing apps using the AI Companion, all the apps using that companion will share the same TinyDb.  That sharing will disappear once the apps are packaged.  But, during development, you should be careful to clear the TinyDb each time you start working on a new app.</p>")
  @Description("")
  String TinyDBHelpStringComponentPallette();

  @DefaultMessage("Non-visible component that communicates with a Web service to store and retrieve information.")
  @Description("")
  String TinyWebDBHelpStringComponentPallette();

  @DefaultMessage("A non-visible component that enables communication with <a href=\"http://www.twitter.com\" target=\"_blank\">Twitter</a>. Once a user has logged into their Twitter account (and the authorization has been confirmed successful by the <code>IsAuthorized</code> event), many more operations are available:<ul><li> Searching Twitter for tweets or labels (<code>SearchTwitter</code>)</li>\n<li> Sending a Tweet (<code>Tweet</code>)     </li>\n<li> Sending a Tweet with an Image (<code>TweetWithImage</code>)     </li>\n<li> Directing a message to a specific user      (<code>DirectMessage</code>)</li>\n <li> Receiving the most recent messages directed to the logged-in user      (<code>RequestDirectMessages</code>)</li>\n <li> Following a specific user (<code>Follow</code>)</li>\n<li> Ceasing to follow a specific user (<code>StopFollowing</code>)</li>\n<li> Getting a list of users following the logged-in user      (<code>RequestFollowers</code>)</li>\n <li> Getting the most recent messages of users followed by the      logged-in user (<code>RequestFriendTimeline</code>)</li>\n <li> Getting the most recent mentions of the logged-in user      (<code>RequestMentions</code>)</li></ul></p>\n <p>You must obtain a Comsumer Key and Consumer Secret for Twitter authorization  specific to your app from http://twitter.com/oauth_clients/new")
  @Description("")
  String TwitterHelpStringComponentPallette();

  @DefaultMessage("<p>A formatting element in which to place components that should be displayed one below another.  (The first child component is stored on top, the second beneath it, etc.)  If you wish to have components displayed next to one another, use <code>HorizontalArrangement</code> instead.</p>")
  @Description("")
  String VerticalArrangementHelpStringComponentPallette();

  @DefaultMessage("<p>A formatting element in which to place components that should be displayed one below another.  (The first child component is stored on top, the second beneath it, etc.)  If you wish to have components displayed next to one another, use <code>HorizontalArrangement</code> instead.</p><p> This version is scrollable.</p>")
  @Description("")
  String VerticalScrollArrangementHelpStringComponentPallette();

  @DefaultMessage("A multimedia component capable of playing videos. When the application is run, the VideoPlayer will be displayed as a rectangle on-screen.  If the user touches the rectangle, controls will appear to play/pause, skip ahead, and skip backward within the video.  The application can also control behavior by calling the <code>Start</code>, <code>Pause</code>, and <code>SeekTo</code> methods.  <p>Video files should be in Windows Media Video (.wmv) format, 3GPP (.3gp), or MPEG-4 (.mp4).  For more details about legal formats, see <a href=\"http://developer.android.com/guide/appendix/media-formats.html\" target=\"_blank\">Android Supported Media Formats</a>.</p><p>App Inventor for Android only permits video files under 1 MB and limits the total size of an application to 5 MB, not all of which is available for media (video, audio, and sound) files.  If your media files are too large, you may get errors when packaging or installing your application, in which case you should reduce the number of media files or their sizes.  Most video editing software, such as Windows Movie Maker and Apple iMovie, can help you decrease the size of videos by shortening them or re-encoding the video into a more compact format.</p><p>You can also set the media source to a URL that points to a streaming video, but the URL must point to the video file itself, not to a program that plays the video.")
  @Description("")
  String VideoPlayerHelpStringComponentPallette();

  @DefaultMessage("<p>The Voting component enables users to vote on a question by communicating with a Web service to retrieve a ballot and later sending back users\" votes.</p>")
  @Description("")
  String VotingHelpStringComponentPallette();

  @DefaultMessage("Non-visible component that provides functions for HTTP GET, POST, PUT, and DELETE requests.")
  @Description("")
  String WebHelpStringComponentPallette();

  @DefaultMessage("Component for viewing Web pages.  The Home URL can be specified in the Designer or in the Blocks Editor.  The view can be set to follow links when they are tapped, and users can fill in Web forms. Warning: This is not a full browser.  For example, pressing the phone\"s hardware Back key will exit the app, rather than move back in the browser history.<p />You can use the WebViewer.WebViewString property to communicate between your app and Javascript code running in the Webviewer page. In the app, you get and set WebViewString.  In the WebViewer, you include Javascript that references the window.AppInventor object, using the methoods </em getWebViewString()</em> and <em>setWebViewString(text)</em>.  <p />For example, if the WebViewer opens to a page that contains the Javascript command <br /> <em>document.write(\"The answer is\" + window.AppInventor.getWebViewString());</em> <br />and if you set WebView.WebVewString to \"hello\", then the web page will show </br ><em>The answer is hello</em>.  <br />And if the Web page contains Javascript that executes the command <br /><em>window.AppInventor.setWebViewString(\"hello from Javascript\")</em>, <br />then the value of the WebViewString property will be <br /><em>hello from Javascript</em>. ")
  @Description("")
  String WebViewerHelpStringComponentPallette();

  @DefaultMessage("Use this component to translate words and sentences between different languages. This component needs Internet access, as it will request translations to the Yandex.Translate service. Specify the source and target language in the form source-target using two letter language codes. So\"en-es\" will translate from English to Spanish while \"es-ru\" will translate from Spanish to Russian. If you leave out the source language, the service will attempt to detect the source language. So providing just \"es\" will attempt to detect the source language and translate it to Spanish.<p /> This component is powered by the Yandex translation service.  See http://api.yandex.com/translate/ for more information, including the list of available languages and the meanings of the language codes and status codes. <p />Note: Translation happens asynchronously in the background. When the translation is complete, the \"GotTranslation\" event is triggered.")
  @Description("")
  String YandexTranslateHelpStringComponentPallette();

  @DefaultMessage("A non-visible component allowing you to store data on a Web database powered by Firebase. " +
      "This allows the users of your app to share data with each other. " +
      "By default, data will be stored in App Inventor''s shared Firebase database. " +
      "Otherwise, you can specify the URL for your own Firebase in the \"FirebaseURL\" property. " +
      "Learn more at <a target=\"_blank\" href=\"http://www.firebase.com\">Firebase.com</a>.")
  @Description("")
  String FirebaseDBHelpStringComponentPallette();


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

  @DefaultMessage("简体中文")
  @Description("")
  String SwitchToSimplifiedChinese();

  @DefaultMessage("繁体中文")
  @Description("")
  String SwitchToTraditionalChinese();

  @DefaultMessage("Español")
  @Description("")
  String SwitchToSpanish();

  @DefaultMessage("Français")
  @Description("")
  String SwitchToFrench();

  @DefaultMessage("Italiano")
  @Description("")
  String SwitchToItalian();

  @DefaultMessage("Pусский")
  @Description("")
  String SwitchToRussian();

  @DefaultMessage("한국어")
  @Description("")
  String SwitchToKorean();

  @DefaultMessage("Svenska")
  @Description("")
  String SwitchToSwedish();

  @DefaultMessage("Português do Brasil")
  @Description("")
  String switchToPortugueseBR();

  @DefaultMessage("Português")
  @Description("")
  String switchToPortuguese();

  @DefaultMessage("Nederlands")
  @Description("")
  String switchToDutch();

  @DefaultMessage("Progress Bar")
  @Description("")
  String ProgressBarFor();

  // =========== ProximitySensor
  @DefaultMessage("ProximitySensor")
  @Description("")
  String proximitySensorComponentPallette();

  @DefaultMessage("Non-visible component that can measures the proximity of an object in cm relative to the view screen of a device. This sensor is typically used to determine whether a handset is being held up to a persons ear; i.e. lets you determine how far away an object is from a device. Many devices return the absolute distance, in cm, but some return only near and far values. In this case, the sensor usually reports its maximum range value in the far state and a lesser value in the near state.")
  @Description("")
  String ProximitySensorHelpStringComponentPallette();

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

  @DefaultMessage("AccountName")
  @Description("")
  String AccountNameProperties();

  @DefaultMessage("ProjectID")
  @Description("")
  String ProjectIDProperties();

  @DefaultMessage("CloudDBError")
  @Description("")
  String CloudDBErrorEvents();

  @DefaultMessage("CloudDB")
  @Description("")
  String cloudDBComponentPallette();

  @DefaultMessage("Non-visible component allowing you to store data on a Internet " +
    "connected database server (using Redis software). This allows the users of " +
    "your App to share data with each other. " +
    "By default data will be stored in a server maintained by MIT, however you " +
    "can setup and run your own server. Set the \"RedisServer\" property and " +
    "\"RedisPort\" Property to access your own server.")
  @Description("")
  String CloudDBHelpStringComponentPallette();

  @DefaultMessage("RedisServer")
  @Description("")
  String RedisServerProperties();

  @DefaultMessage("DefaultRedisServer")
  @Description("")
  String DefaultRedisServerProperties();

  @DefaultMessage("RedisPort")
  @Description("")
  String RedisPortProperties();

  @DefaultMessage("Token")
  @Description("")
  String TokenProperties();

  @DefaultMessage("GetValues")
  @Description("")
  String GetValuesMethods();

  @DefaultMessage("itemToAdd")
  @Description("")
  String itemToAddParams();

  @DefaultMessage("UseSSL")
  @Description("")
  String UseSSLProperties();

  @DefaultMessage("CloudConnected")
  @Description("")
  String CloudConnectedMethods();

  @DefaultMessage("PrimaryColor")
  @Description("")
  String PrimaryColorProperties();

  @DefaultMessage("PrimaryColorDark")
  @Description("")
  String PrimaryColorDarkProperties();

  @DefaultMessage("AccentColor")
  @Description("")
  String AccentColorProperties();

  @DefaultMessage("Theme")
  @Description("")
  String ThemeProperties();

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

  @DefaultMessage("<p>A two-dimensional container that renders map tiles in the background and " +
      "allows for multiple Marker elements to identify points on the map. Map tiles are supplied " +
      "by OpenStreetMap contributors and the United States Geological Survey.</p>" +
      "<p>The Map component provides three utilities for manipulating its boundaries within App " +
      "Inventor. First, a locking mechanism is provided to allow the map to be moved relative to " +
      "other components on the Screen. Second, when unlocked, the user can pan the Map to any " +
      "location. At this new location, the &quot;Set Initial Boundary&quot; button can be pressed " +
      "to save the current Map coordinates to its properties. Lastly, if the Map is moved to a " +
      "different location, for example to add Markers off-screen, then the &quot;Reset Map to " +
      "Initial Bounds&quot; button can be used to re-center the Map at the starting location.</p>")
  @Description("")
  String MapHelpStringComponentPallette();

  @DefaultMessage("CenterFromString")
  @Description("")
  String CenterFromStringProperties();

  @DefaultMessage("BoundingBox")
  @Description("A list containing the latitude and longitude of the top-left and bottom-right " +
    "corners of the Map view in the form ((lat, long), (lat, long)).")
  String BoundingBoxProperties();

  @DefaultMessage("MapType")
  @Description("The type of map tile to be displayed")
  String MapTypeProperties();

  @DefaultMessage("ScaleUnits")
  @Description("Display name for the property to adjust the map's scale units")
  String ScaleUnitsProperties();

  @DefaultMessage("ShowCompass")
  @Description("Show a compass control on the Map")
  String ShowCompassProperties();

  @DefaultMessage("ShowScale")
  @Description("Show a scale indicator on the Map")
  String ShowScaleProperties();

  @DefaultMessage("ShowUser")
  @Description("Show a marker on the Map for the user's current location")
  String ShowUserProperties();

  @DefaultMessage("ShowZoom")
  @Description("Show a control for changing the Map zoom")
  String ShowZoomProperties();

  @DefaultMessage("EnableRotation")
  @Description("Allow the user to rotate the map")
  String EnableRotationProperties();

  @DefaultMessage("ZoomLevel")
  @Description("Get or set the zoom level of the Map")
  String ZoomLevelProperties();

  @DefaultMessage("BoundsChange")
  @Description("On change of the Map bounds, do...")
  String BoundsChangeEvents();

  @DefaultMessage("MapReady")
  @Description("On map ready, do...")
  String ReadyEvents();

  @DefaultMessage("ZoomChange")
  @Description("On zoom change, do...")
  String ZoomChangeEvents();

  @DefaultMessage("InvalidPoint")
  @Description("")
  String InvalidPointEvents();

  @DefaultMessage("PanTo")
  @Description("Pan the map to the given latitude, longitude, and zoom")
  String PanToMethods();

  @DefaultMessage("Roads")
  @Description("Road network map type")
  String mapTypeRoads();

  @DefaultMessage("Aerial")
  @Description("Aerial photography map type")
  String mapTypeAerial();

  @DefaultMessage("Terrain")
  @Description("Terrain map type")
  String mapTypeTerrain();

  @DefaultMessage("Metric")
  @Description("Display name for the metric unit system")
  String mapScaleUnitsMetric();

  @DefaultMessage("Imperial")
  @Description("Display name for the imperial unit system")
  String mapScaleUnitsImperial();

  @DefaultMessage("ImageAsset")
  @Description("ImageAsset")
  String ImageAssetProperties();

  @DefaultMessage("Description")
  @Description("Description")
  String DescriptionProperties();

  @DefaultMessage("Draggable")
  @Description("Draggable")
  String DraggableProperties();

  @DefaultMessage("StartDrag")
  @Description("StartDrag")
  String StartDragEvents();

  @DefaultMessage("Drag")
  @Description("Drag")
  String DragEvents();

  @DefaultMessage("StopDrag")
  @Description("StopDrag")
  String StopDragEvents();

  @DefaultMessage("Unexpected map type: {0}")
  @Description("")
  String unknownMapTypeException(String maptype);

  @DefaultMessage("Expected 2 values for CenterFromString but got {0}")
  @Description("")
  String mapCenterWrongNumberArgumentsException(int numArgs);

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

  @DefaultMessage("CreateMarker")
  @Description("CreateMarker")
  String CreateMarkerMethods();

  @DefaultMessage("SetLocation")
  @Description("SetLocation")
  String SetLocationMethods();

  @DefaultMessage("FillColor")
  @Description("FillColor")
  String FillColorProperties();

  @DefaultMessage("StrokeColor")
  @Description("StrokeColor")
  String StrokeColorProperties();

  @DefaultMessage("StrokeWidth")
  @Description("StrokeWidth")
  String StrokeWidthProperties();

  @DefaultMessage("Type")
  @Description("Type")
  String TypeProperties();

  @DefaultMessage("LoadError")
  @Description("")
  String LoadErrorEvents();

  @DefaultMessage("GotFeatures")
  @Description("")
  String GotFeaturesEvents();

  @DefaultMessage("TapAtPoint")
  @Description("")
  String TapAtPointEvents();

  @DefaultMessage("FeatureFromDescription")
  @Description("")
  String FeatureFromDescriptionMethods();

  @DefaultMessage("EnableInfobox")
  @Description("")
  String EnableInfoboxProperties();

  @DefaultMessage("EnableZoom")
  @Description("")
  String EnableZoomProperties();

  @DefaultMessage("HideInfobox")
  @Description("")
  String HideInfoboxMethods();

  @DefaultMessage("ShowInfobox")
  @Description("")
  String ShowInfoboxMethods();

  @DefaultMessage("From URL...")
  @Description("")
  String fromUrlButton();

  @DefaultMessage("Import Media from URL")
  @Description("")
  String urlImportWizardCaption();

  @DefaultMessage("AnchorHorizontal")
  @Description("")
  String AnchorHorizontalProperties();

  @DefaultMessage("AnchorVertical")
  @Description("")
  String AnchorVerticalProperties();

  @DefaultMessage("Features")
  @Description("")
  String FeaturesProperties();

  @DefaultMessage("FeaturesFromGeoJSON")
  @Description("")
  String FeaturesFromGeoJSONProperties();

  @DefaultMessage("Points")
  @Description("")
  String PointsProperties();

  @DefaultMessage("EnablePan")
  @Description("")
  String EnablePanProperties();

  @DefaultMessage("DistanceToFeature")
  @Description("")
  String DistanceToFeatureMethods();

  @DefaultMessage("DistanceToPoint")
  @Description("")
  String DistanceToPointMethods();

  @DefaultMessage("BearingToFeature")
  @Description("")
  String BearingToFeatureMethods();

  @DefaultMessage("BearingToPoint")
  @Description("")
  String BearingToPointMethods();

  @DefaultMessage("HolePoints")
  @Description("")
  String HolePointsProperties();

  @DefaultMessage("Centroid")
  @Description("")
  String CentroidMethods();

  @DefaultMessage("PointsFromString")
  @Description("")
  String PointsFromStringProperties();

  @DefaultMessage("HolePointsFromString")
  @Description("")
  String HolePointsFromStringProperties();

  @DefaultMessage("DoubleTapAtPoint")
  @Description("")
  String DoubleTapAtPointEvents();

  @DefaultMessage("FeatureClick")
  @Description("")
  String FeatureClickEvents();

  @DefaultMessage("FeatureDrag")
  @Description("")
  String FeatureDragEvents();

  @DefaultMessage("FeatureLongClick")
  @Description("")
  String FeatureLongClickEvents();

  @DefaultMessage("FeatureStartDrag")
  @Description("")
  String FeatureStartDragEvents();

  @DefaultMessage("FeatureStopDrag")
  @Description("")
  String FeatureStopDragEvents();

  @DefaultMessage("LongPressAtPoint")
  @Description("")
  String LongPressAtPointEvents();

  @DefaultMessage("Circle")
  @Description("")
  String circleComponentPallette();

  @DefaultMessage("<p>Use the Circle component to draw a circle of a given radius around a " +
      "point. The radius is specified in meters.</p><p>Click and drag the handle on the edge of " +
      "the circle to change its size. Click and drag the center to change its location.</p>")
  @Description("")
  String CircleHelpStringComponentPallette();

  @DefaultMessage("mapFeature")
  @Description("")
  String mapFeatureParams();

  @DefaultMessage("centroids")
  @Description("")
  String centroidsParams();

  @DefaultMessage("centroid")
  @Description("")
  String centroidParams();

  @DefaultMessage("FeatureCollection")
  @Description("")
  String featureCollectionComponentPallette();

  @DefaultMessage("<p>Use a FeatureCollection to show a group of features. Load a GeoJSON file " +
      "as an asset (or using a URL in blocks) into the FeatureCollection to see the data " +
      "contained within.</p>")
  @Description("")
  String FeatureCollectionHelpStringComponentPallette();

  @DefaultMessage("LineString")
  @Description("")
  String lineStringComponentPallette();

  @DefaultMessage("<p>Use the LineString component to draw a sequence of line segments on a " +
      "Map.</p><p>Click and drag on the vertices of the LineString to move it in the designer. " +
      "Click and drag the midpoint of a line segment to split it into smaller pieces.</p>")
  @Description("")
  String LineStringHelpStringComponentPallette();

  @DefaultMessage("Map")
  @Description("")
  String mapComponentPallette();

  @DefaultMessage("feature")
  @Description("")
  String featureParams();

  @DefaultMessage("features")
  @Description("")
  String featuresParams();

  @DefaultMessage("description")
  @Description("")
  String descriptionParams();

  @DefaultMessage("zoom")
  @Description("")
  String zoomParams();

  @DefaultMessage("Marker")
  @Description("")
  String markerComponentPallette();

  @DefaultMessage("<p>Marker is an icon positioned at a point to indicate information on a Map. " +
      "Markers can be used to provide an info window, custom fill and stroke colors, and custom " +
      "images to convey information to the user.")
  @Description("")
  String MarkerHelpStringComponentPallette();

  @DefaultMessage("Polygon")
  @Description("")
  String polygonComponentPallette();

  @DefaultMessage("<p>Use a Polygon to draw arbitrary shapes on a Map.</p><p>Click and drag on " +
      "the handles to move the vertices of the Polygon. Click and drag the handle at the midpoint " +
      "of an edge to split that edge into two parts. Drag the Polygon around to reposition it.</p>")
  @Description("")
  String PolygonHelpStringComponentPallette();

  @DefaultMessage("errorMessage")
  @Description("")
  String errorMessageParams();

  @DefaultMessage("Rectangle")
  @Description("")
  String rectangleComponentPallette();

  @DefaultMessage("<p>Use the Rectangle to draw a rectangle on a Map bounded by north, south, " +
      "east, and west edges.</p><p>Click and drag the handles of the Rectangle to change its " +
      "size. Click and drag the Rectangle to reposition it on the Map.</p>")
  @Description("")
  String RectangleHelpStringComponentPallette();

  @DefaultMessage("EastLongitude")
  @Description("")
  String EastLongitudeProperties();

  @DefaultMessage("NorthLatitude")
  @Description("")
  String NorthLatitudeProperties();

  @DefaultMessage("SouthLatitude")
  @Description("")
  String SouthLatitudeProperties();

  @DefaultMessage("WestLongitude")
  @Description("")
  String WestLongitudeProperties();

  @DefaultMessage("LoadFromURL")
  @Description("")
  String LoadFromURLMethods();

  @DefaultMessage("Custom...")
  @Description("")
  String customEllipsis();

  @DefaultMessage("ActionBar")
  @Description("")
  String ActionBarProperties();

  @DefaultMessage("The given value {0} was not in the expected range [{1}, {2}].")
  @Description("")
  String valueNotInRange(String text, String min, String max);

  @DefaultMessage("The value supplied for {0} was not a valid latitude, longitude pair.")
  @Description("")
  String expectedLatLongPair(String property);

  @DefaultMessage("LocationSensor")
  @Description("")
  String LocationSensorProperties();

  @DefaultMessage("UserLatitude")
  @Description("")
  String UserLatitudeProperties();

  @DefaultMessage("UserLongitude")
  @Description("")
  String UserLongitudeProperties();

  @DefaultMessage("Bounds")
  @Description("")
  String BoundsMethods();

  @DefaultMessage("Center")
  @Description("")
  String CenterMethods();

  @DefaultMessage("SetCenter")
  @Description("")
  String SetCenterMethods();

  @DefaultMessage("Rotation")
  @Description("")
  String RotationProperties();

  @DefaultMessage("WebRTC")     // Note: This is INTERNAL so doesn't need translation
  @Description("")
  String WebRTCProperties();

  @DefaultMessage("GetVersionName")
  @Description("")
  String GetVersionNameMethods();

  @DefaultMessage("SdkLevel")
  @Description("")
  String SdkLevelMethods();

  @DefaultMessage("GetInstaller")
  @Description("")
  String GetInstallerMethods();

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
}
