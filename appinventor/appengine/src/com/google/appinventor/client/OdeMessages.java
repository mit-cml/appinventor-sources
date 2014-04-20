// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n strings for {@link Ode}.
 *
 */
public interface OdeMessages extends Messages {
  // Used in multiple files

  @DefaultMessage("Cancel")
  @Description("Text on 'Cancel' button.")
  String cancelButton();

  @DefaultMessage("OK")
  @Description("Text on 'OK' button.")
  String okButton();

  @DefaultMessage("Dismiss")
  @Description("Text on 'Dismiss' button.")
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
  @Description("Text on 'Delete' button")
  String deleteButton();

  @DefaultMessage("Delete Project")
  @Description("Text on 'Delete Project' button")
  String deleteProjectButton();

  @DefaultMessage("Show Warnings")
  @Description("Text on Toggle Warning Button")
  String showWarnings();

  @DefaultMessage("Hide Warnings")
  @Description("Text on Toggle Warning Button")
  String hideWarnings();

  @DefaultMessage("Upload File ...")
  @Description("Text on 'Add...' button")
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

  @DefaultMessage("Checkpoint ...")
  @Description("Label of the button for checkpoint")
  String checkpointButton();

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

  @DefaultMessage("App ( provide QR code for .apk )")
  @Description("Label of item for building a project and show barcode")
  String showBarcodeButton();

  @DefaultMessage("App ( save .apk to my computer )")
  @Description("Label of item for building a project and downloading")
  String downloadToComputerButton();

  @DefaultMessage("Generate YAIL")
  @Description("Label of the cascade item for generating YAIL for a project")
  String generateYailButton();

  @DefaultMessage("Build")
  @Description("Label of the button leading to build related cascade items")
  String buildButton();

  @DefaultMessage("Packaging ...")
  @Description("Label of the button leading to build related cascade items, when building")
  String isBuildingButton();

  @DefaultMessage("Opening the Blocks Editor... (click to cancel)")
  @Description("Label of the button for canceling the blocks editor launch")
  String cancelBlocksEditorButton();

  @DefaultMessage("Blocks Editor is open")
  @Description("Label of the button for opening the blocks editor when the it is already open")
  String blocksEditorIsOpenButton();

  // Used in MotdFetcher.java

  @DefaultMessage("Failed to contact server to get the MOTD.")
  @Description("Message displayed when cannot get a MOTD from the server.")
  String getMotdFailed();

  // Used in Ode.java

  @DefaultMessage("MIT App Inventor 2")
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

  @DefaultMessage("An internal error has occurred. Click 'ok' for more information.")
  @Description("Confirm alert after an internal error")
  String internalErrorClickOkDebuggingView();

  @DefaultMessage("The server is temporarily unavailable. Please try again later!")
  @Description("Error message if the server becomes completely unavailable.")
  String serverUnavailable();

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

  @DefaultMessage("Help")
  @Description("Label for the Help menu")
  String helpLink();

  @DefaultMessage("About")
  @Description("Label of the link for About")
  String aboutLink();

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

  @DefaultMessage("Save project")
  @Description("Name of Save menuitem")
  String saveMenuItem();

  @DefaultMessage("Save project as ...")
  @Description("Name of Save as ... menuitem")
  String saveAsMenuItem();

  @DefaultMessage("Import project (.aia) from my computer ...")
  @Description("Name of Import Project menuitem")
  String importProjectButton();

  @DefaultMessage("Export selected project (.aia) to my computer")
  @Description("Name of Export Project menuitem")
  String exportProjectButton();

  @DefaultMessage("Export all projects")
  @Description("Name of Export all Project menuitem")
  String exportAllProjectsButton();

  @DefaultMessage("Sign out")
  @Description("Label of the link for signing out")
  String signOutLink();

  @DefaultMessage("My Projects")
  @Description("Name of My Projects tab")
  String tabNameProjects();

  @DefaultMessage("Report an Issue")
  @Description("Link for Report an Issue form")
  String feedbackLink();

  @DefaultMessage("Gallery")
  @Description("Link for Gallery")
  String galleryLink();

  @DefaultMessage("Design")
  @Description("Name of Design tab")
  String tabNameDesign();

  @DefaultMessage("Library")
  @Description("Name of Library link")
  String libraryLink();

  @DefaultMessage("Get Started")
  @Description("Name of Getting Started link")
  String getStartedLink();

  @DefaultMessage("Tutorials")
  @Description("Name of Tutorials link")
  String tutorialsLink();

  @DefaultMessage("Troubleshooting")
  @Description("Name of Troubleshooting link")
  String troubleshootingLink();

  @DefaultMessage("Forums")
  @Description("Name of Forums link")
  String forumsLink();

  @DefaultMessage("Guide")
  @Description("Name of Guide link")
  String guideLink();

  @DefaultMessage("(Debugging)")
  @Description("Name of Debugging tab")
  String tabNameDebugging();

  @DefaultMessage("Please choose a project to open or create a new project.")
  @Description("Message shown when there is no current file editor to switch to")
  String chooseProject();

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

  @DefaultMessage("Projects")
  @Description("Caption for project list box.")
  String projectListBoxCaption();

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

  @DefaultMessage("Component name cannot be any of the following: CsvUtil, Double, Float, " +
      "Integer, JavaCollection, JavaIterator, KawaEnvironment, Long, Short, SimpleForm, String, " +
      "Pattern, YailList, YailNumberToString, YailRuntimeError")
  @Description("Error shown when a new component name is a variable name already used in the" +
      "Yail code")
  String badComponentNameError();

  @DefaultMessage("Deleting this component will delete all blocks associated with it in the " +
      "Blocks Editor. Are you sure you want to delete?")
  @Description("Confirmation query for removing a component")
  String reallyDeleteComponent();

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
  @Description("Text for horizontal alignemt choice 'Right'")
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

  @DefaultMessage("pixels")
  @Description("Caption for pixels label")
  String pixelsCaption();

  @DefaultMessage("{0} pixels")
  @Description("Summary for custom length in pixels")
  String pixelsSummary(String pixels);

  @DefaultMessage("The value must be a number greater than or equal to 0")
  @Description("Error shown after validation of custom length field failed.")
  String nonnumericInputError();

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

  @DefaultMessage("New Screen")
  @Description("Title of new Screen dialog.")
  String newFormTitle();

  @DefaultMessage("Screen name:")
  @Description("Label in front of name in new screen dialog.")
  String formNameLabel();

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

  @DefaultMessage("Project")
  @Description("Label for the Project Button")
  String projectButton();

  @DefaultMessage("Start new project ...")
  @Description("Label of the menu item for creating a new project")
  String newMenuItemButton();

  @DefaultMessage("Delete project")
  @Description("Label of the menu item for deleting a project")
  String deleteMenuItemButton();

  @DefaultMessage("New Project")
  @Description("Label of the button for creating a new project")
    String newButton();

  @DefaultMessage("Export keystore")
  @Description("Label of the button for export keystore")
  String downloadKeystoreButton();

  @DefaultMessage("Import keystore")
  @Description("Label of the button for import keystore")
  String uploadKeystoreButton();

  @DefaultMessage("Delete keystore")
  @Description("Label of the button for delete keystore")
  String deleteKeystoreButton();

  @DefaultMessage("It may take a little while for your projects to be downloaded. " +
      "Please be patient...")
  @Description("Warning that downloading projects will take a while")
  String downloadAllAlert();

  @DefaultMessage("More Actions")
  @Description("Label of the button leading to more cascade items")
  String moreActionsButton();

  @DefaultMessage("Download User Source")
  @Description("Label of the button for admins to download a user's project source")
  String downloadUserSourceButton();

  @DefaultMessage("Switch To Debug Panel")
  @Description("Label of the button for admins to switch to the debug panel without an explicit error")
  String switchToDebugButton();

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

  @DefaultMessage("Admin")
  @Description("Label of the button leading to admin functionality")
  String adminButton();

  @DefaultMessage("Please select a project to delete")
  @Description("Error message displayed when no project is selected")
  String noProjectSelectedForDelete();

  @DefaultMessage("Are you really sure you want to delete this project: {0}")
  @Description("Confirmation message for selecting a single project and clicking delete")
  String confirmDeleteSingleProject(String projectName);

  @DefaultMessage("Are you really sure you want to delete these projects: {0}")
  @Description("Confirmation message for selecting multiple projects and clicking delete")
  String confirmDeleteManyProjects(String projectNames);

  @DefaultMessage("Server error: could not delete project. Please try again later!")
  @Description("Error message reported when deleting a project failed on the server.")
  String deleteProjectError();

  @DefaultMessage("One project must be selected")
  @Description("Error message displayed when no or many projects are selected")
  String wrongNumberProjectsSelected();

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
      "clicking above on \"Learn\", then \"Troubleshooting\", and then \"Keystores and Signing " +
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
      "clicking above on \"Learn\", then \"Troubleshooting\", and then \"Keystores and Signing " +
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

  @DefaultMessage("File names can contain only unaccented letters, numbers, and the characters " +
                  "\"-\", \"_\", \".\", \"!\", \"~\", \"*\", \"(\", and \")\"")
  @Description("Error message when file name contains characters that would require URL encoding.")
  String malformedFilename();

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

  @DefaultMessage("Please select a file to upload.")
  @Description("Error message reported when a file was not selected.")
  String noFileSelected();

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

  // Used in wizards/ProjectUploadWizard.java

  @DefaultMessage("Import Project...")
  @Description("Caption for project upload wizard.")
  String projectUploadWizardCaption();

  @DefaultMessage("Server error: could not upload project. Please try again later!")
  @Description("Error message reported when a project couldn't be uploaded to the server.")
  String projectUploadError();

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

  @DefaultMessage("AI Companion")
  @Description("Message providing details about starting the wireless connection.")
  String wirelessButton();

  @DefaultMessage("Emulator")
  @Description("Message providing details about starting the emulator connection.")
  String emulatorButton();

  @DefaultMessage("USB")
  @Description("Message providing details about starting a USB connection.")
  String usbButton();

  @DefaultMessage("Reset Connection")
  @Description("Reset all connections.")
  String resetConnections();

  @DefaultMessage("Hard Reset")
  @Description("Hard Reset the Emulator.")
  String hardResetConnections();

  // This error message is displayed as HTML
  @DefaultMessage("App Inventor is unable to compile this project.  " +
  "<br /> The compiler error output was <br /> {0}.")
  @Description("Compilation error, with error message.")
  String unableToCompile(String errorMesssage);

}
