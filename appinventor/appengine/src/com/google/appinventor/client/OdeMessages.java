// Copyright 2008 Google Inc. All Rights Reserved.

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

  @DefaultMessage("Add...")
  @Description("Text on 'Add...' button")
  String addButton();

  @DefaultMessage("Name")
  @Description("Header for name column of project table")
  String projectNameHeader();

  @DefaultMessage("Date Created")
  @Description("Header for date column of project table.")
  String projectDateHeader();

  // Used in DesignToolbar.java

  @DefaultMessage("Save")
  @Description("Label of the button for save")
  String saveButton();

  @DefaultMessage("Save As")
  @Description("Label of the button for save as")
  String saveAsButton();

  @DefaultMessage("Checkpoint")
  @Description("Label of the button for checkpoint")
  String checkpointButton();

  @DefaultMessage("Add Screen")
  @Description("Label of the button for adding a new screen")
  String addFormButton();

  @DefaultMessage("Remove Screen")
  @Description("Label of the button for removing a screen")
  String removeFormButton();

  @DefaultMessage("Deleting this screen will completely remove the screen from your project. " +
      "All components and blocks associated with this screen will be deleted.\n" +
      "There is no undo.\nAre you sure you want to delete {0}?")
  @Description("Confirmation query for removing a screen")
  String reallyDeleteForm(String formName);

  @DefaultMessage("Open the Blocks Editor")
  @Description("Label of the button for opening the blocks editor")
  String openBlocksEditorButton();

  @DefaultMessage("Show Barcode")
  @Description("Label of the cascade item for building a project and showing barcode")
  String showBarcodeButton();

  @DefaultMessage("Download to this Computer")
  @Description("Label of the cascade item for building a project and downloading")
  String downloadToComputerButton();

  @DefaultMessage("Download to Connected Phone")
  @Description("Label of the cascade item for building a project and downloading it to a phone")
  String downloadToPhoneButton();

  @DefaultMessage("Package for Phone")
  @Description("Label of the button leading to build related cascade items")
  String buildButton();

  @DefaultMessage("Packaging...")
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

  // TODO(user): Replace with commented version once we're ready
  @DefaultMessage("App Inventor for Android - Experimental Version")
//  @DefaultMessage("App Inventor for Android")
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

  @DefaultMessage("The server is temporarily unavailable. Please try again later!")
  @Description("Error message if the server becomes completely unavailable.")
  String serverUnavailable();

  @DefaultMessage("The Blocks Editor should close automatically.\n" +
      "Please press OK when the Blocks Editor is closed.")
  @Description("Closing messsage with codeblocks open")
  String onClosingBrowserWithCodeblocksOpen();

  // Used in RpcStatusPopup.java

  @DefaultMessage("Loading...")
  @Description("Message that is shown to indicate that a loading RPC is going on")
  String defaultRpcMessage();

  @DefaultMessage("Saving...")
  @Description("Message that is shown to indicate that a saving RPC is going on")
  String savingRpcMessage();

  @DefaultMessage("Copying...")
  @Description("Message that is shown to indicate that a copying RPC is going on")
  String copyingRpcMessage();

  @DefaultMessage("Deleting...")
  @Description("Message that is shown to indicate that a deleting RPC is going on")
  String deletingRpcMessage();

  @DefaultMessage("Packaging...")
  @Description("Message shown during a building RPC (for Young Android, called 'packaging')")
  String packagingRpcMessage();

  @DefaultMessage("Downloading to phone...")
  @Description("Message shown while downloading application to the phone (during compilation)")
  String downloadingRpcMessage();

  // Used in StatusPanel.java

  @DefaultMessage("Version: {0} Id: {1}")
  @Description("Label showing the Mercurial build id")
  String mercurialBuildId(String version, String id);

  @DefaultMessage("About")
  @Description("Label of the link for About")
  String aboutLink();

  @DefaultMessage("Privacy")
  @Description("Label of the link for Privacy")
  String privacyLink();

  @DefaultMessage("Terms")
  @Description("Label of the link for Terms")
  String termsLink();

  // Used in TopPanel.java

  @DefaultMessage("Report bug")
  @Description("Label of the link for reporting a bug")
  String reportBugLink();

  @DefaultMessage("Sign out")
  @Description("Label of the link for signing out")
  String signOutLink();

  @DefaultMessage("My Projects")
  @Description("Name of My Projects tab")
  String tabNameProjects();

  @DefaultMessage("Design")
  @Description("Name of Design tab")
  String tabNameDesign();

  @DefaultMessage("Learn")
  @Description("Name of Learn tab")
  String tabNameLearn();

  @DefaultMessage("(Debugging)")
  @Description("Name of Debugging tab")
  String tabNameDebugging();

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

  // Used in boxes/ViewerBox.java

  @DefaultMessage("Viewer")
  @Description("Caption for a viewer box.")
  String viewerBoxCaption();

  // Used in editor/EditorManager.java

  @DefaultMessage("Server error: could not save one or more files. Please try again later!")
  @Description("Error message reported when one or more file couldn't be saved to the server.")
  String saveErrorMultipleFiles();

  // Used in editor/simple/SimpleNonVisibleComponentsPanel.java

  @DefaultMessage("Non-visible components")
  @Description("Header for the non-visible components in the designer.")
  String nonVisibleComponentsHeader();

  // Used in editor/simple/SimpleVisibleComponentsPanel.java

  @DefaultMessage("Display Invisible Components in Viewer")
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

  @DefaultMessage("Deleting this component will delete all blocks associated with it in the " +
      "Blocks Editor. Are you sure you want to delete?")
  @Description("Confirmation query for removing a component")
  String reallyDeleteComponent();

  // Used in editor/simple/components/MockButtonBase.java, MockCheckBox.java, MockLabel.java, and
  // MockRadioButton.java

  @DefaultMessage("Text for {0}")
  @Description("Default value for Text property")
  String textPropertyValue(String componentName);

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

  // Used in editor/youngandroid/YaFormEditor.java

  @DefaultMessage("Server error: could not load file. Please try again later!")
  @Description("Error message reported when a source file couldn't be loaded from the server.")
  String loadError();

  @DefaultMessage("Server error: could not save file. Please try again later!")
  @Description("Error message reported when a source file couldn't be saved to the server.")
  String saveError();

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

  // Used in explorer/SourceStructureExplorer.java

  @DefaultMessage("Rename")
  @Description("Label of the button for rename")
  String renameButton();

  // Used in explorer/commands/AddFormCommand.java

  @DefaultMessage("New Form")
  @Description("Title of new form dialog.")
  String newFormTitle();

  @DefaultMessage("Form name:")
  @Description("Label in front of name in new form dialog.")
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

  @DefaultMessage("There are errors in the blocks for this project. Click on the [Open the " +
      "Blocks Editor] button. Then, retry packaging your project with the blocks editor open.")
  @Description("Alert displayed when an error occurs while generating YAIL for a form and " +
      "codeblocks is not open.")
  String errorGeneratingYailPleaseOpenCodeblocks();

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

  // Used in explorer/commands/DownloadToPhoneCommand.java

  @DefaultMessage("Downloading application to the phone.")
  @Description("Message displayed when downloading an Android application to the phone.")
  String downloadingToPhoneMessage();

  @DefaultMessage("Server error: download to phone failed. Please try again later!")
  @Description("Message displayed when downloading an Android application to the phone fails.")
  String downloadToPhoneFailedMessage();

  @DefaultMessage("Application successfully downloaded to phone.")
  @Description("Message displayed after a successful download of an Android application " +
      "to the phone.")
  String downloadToPhoneSucceededMessage();

  // Used in explorer/commands/EnsurePhoneConnectedCommand.java

  @DefaultMessage("The phone is not connected.")
  @Description("Error message displayed when the user wants to download a project to the phone, " +
      "but the phone is not connected.")
  String phoneNotConnected();

  // Used in explorer/commands/ShowBarcodeCommand.java

  @DefaultMessage("Barcode link for {0}")
  @Description("Title of barcode dialog.")
  String barcodeTitle(String projectName);

  @DefaultMessage("Note: this barcode will only work for user {0}. See {1} the FAQ {2} for info " +
      "on how to share your app with others.")
  @Description("Warning in barcode dialog.")
  String barcodeWarning(String userEmail, String aTagStart, String aTagEnd);

  // Used in explorer/project/Project.java

  @DefaultMessage("Server error: could not load project. Please try again later!")
  @Description("Error message reported when a project could not be loaded from the server.")
  String projectLoadError();

  // Used in explorer/project/ProjectManager.java

  @DefaultMessage("Server error: could not retrieve project information. Please try again later!")
  @Description("Error message reported when information about projects could not be retrieved " +
      "from the server.")
  String projectInformationRetrievalError();

  // Used in explorer/youngandroid/ProjectToolbar.java

  @DefaultMessage("New")
  @Description("Label of the button for creating a new project")
  String newButton();

  @DefaultMessage("Download Source")
  @Description("Label of the button for downloading source")
  String downloadSourceButton();

  @DefaultMessage("Upload Source")
  @Description("Label of the button for uploading source")
  String uploadSourceButton();

  @DefaultMessage("Download All Projects")
  @Description("Label of the button to download all projects' source code")
  String downloadAllButton();

  @DefaultMessage("It may take a little while for your projects to be downloaded. " +
      "Please be patient...")
  @Description("Warning that downloading projects will take a while")
  String downloadAllAlert();

  @DefaultMessage("More Actions")
  @Description("Label of the button leading to more cascade items")
  String moreActionsButton();

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

  @DefaultMessage("Upload File...")
  @Description("Caption for file upload wizard.")
  String fileUploadWizardCaption();

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

  @DefaultMessage("A file named {0} already exists in this project. Do you want to overwrite " +
      "the old file?")
  @Description("Confirmation message shown when a file is about to be overwritten.")
  String confirmOverwrite(String filename);

  // Used in wizards/NewProjectWizard.java

  @DefaultMessage("Server error: could not create project. Please try again later!")
  @Description("Error message reported when the project couldn't be created on the server.")
  String createProjectError();

  // Used in wizards/ProjectUploadWizard.java

  @DefaultMessage("Upload Project...")
  @Description("Caption for project upload wizard.")
  String projectUploadWizardCaption();

  @DefaultMessage("Server error: could not upload project. Please try again later!")
  @Description("Error message reported when a project couldn't be uploaded to the server.")
  String projectUploadError();

  @DefaultMessage("The selected project is not a project archive!")
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

  @DefaultMessage("New App Inventor for Android Project...")
  @Description("Caption for the wizard to create a new Young Android project")
  String newYoungAndroidProjectWizardCaption();

  @DefaultMessage("Project name:")
  @Description("Label for the project name input text box")
  String projectNameLabel();

  // Used in youngandroid/CodeblocksManager.java

  @DefaultMessage("Unable to start the Blocks Editor.")
  @Description("Error message displayed when Codeblocks fails to open.")
  String startingCodeblocksFailed();

  @DefaultMessage("Would you like to continue waiting for the Blocks Editor to start?")
  @Description("Message displayed after waiting a long time for the Blocks Editor to start.")
  String continueTryingToConnect();

  @DefaultMessage("The Blocks Editor needs to be open. Click on the " +
      "[Open the Blocks Editor] button.")
  @Description("Error message displayed when an attempt is made to communicate with Codeblocks, " +
      "but the connection is null.")
  String noCodeblocksConnection();

  @DefaultMessage("The Blocks Editor is not responding. Click on the " +
      "[Open the Blocks Editor] button.")
  @Description("Error message displayed when we need to communicate with Codeblocks, " +
      "but it is not responding.")
  String codeblocksConnectionUnresponsive();

  @DefaultMessage("The Blocks Editor was not able to reload the designer properties.")
  @Description("Error message displayed when Codeblocks fails to reload properties.")
  String codeblocksFailedToReloadProperties();

  @DefaultMessage("The Blocks Editor was not able to load the blocks for the screen.")
  @Description("Error message displayed when Codeblocks fails to load a form.")
  String codeblocksFailedToLoadPropertiesAndBlocks();

  @DefaultMessage("The Blocks Editor was not able to package the blocks.")
  @Description("Error message displayed when Codeblocks fails to save blocks.")
  String codeblocksFailedToSaveBlocks();

  @DefaultMessage("Blocks Editor failed to clear.  Please close and reopen Blocks Editor.")
  @Description("Error message reported when attempting to clear codeblocks.")
  String clearCodeblocksError();

  @DefaultMessage("The Blocks Editor was not able to receive the property change.")
  @Description("Error message displayed when Codeblocks fails to sync a property.")
  String codeblocksFailedToSyncProperty();

  @DefaultMessage("The Blocks Editor was not able to add the asset.")
  @Description("Error message displayed when Codeblocks fails to add an asset.")
  String codeblocksFailedToAddAsset();

  @DefaultMessage("The Blocks Editor was not able to install the application.")
  @Description("Error message displayed when Codeblocks fails to install an application.")
  String codeblocksFailedToInstallApplication();

  @DefaultMessage("Blocks Editor failed to determine whether a phone is connected.  Please close" +
      " and reopen Blocks Editor.")
  @Description("Error message reported when attempting to check if phone is connected.")
  String codeblocksIsPhoneConnectedError();

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
}
