package com.google.appinventor.client.explorer.dialogs;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslations;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface NoProjectDialogBoxMessages extends Messages, ComponentTranslations {

  @DefaultMessage("Welcome to")
  @Description("Welcome text preceding the App Inventor logo")
  String welcome();

  @DefaultMessage("Get started with some tutorials")
  @Description("The header text instructing the new user to get started.")
  String getStarted();

  @DefaultMessage("Close")
  @Description("Text of the button to close the dialog box")
  String closeButton();

  @DefaultMessage("Go to Tutorial")
  @Description("Text of the button to open a tutorial")
  String startTutorialButton();

  @DefaultMessage("HelloPurr is a simple app that you can build in a very short time. You create a button that has a picture of a cat on it, and then program the button so that when it is clicked a \"meow\" sound plays.")
  @Description("Description of the Hello Purr app")
  String helloPurrDescription();

  @DefaultMessage("Hello Purr")
  @Description("Title of the Hello Purr app")
  String helloPurrTitle();

  @DefaultMessage("Start a blank project")
  @Description("Text of the button to start a new project")
  String startProjectButton();


  @DefaultMessage("Text to Speech is surprisingly fun. Find out for yourself with this starter app that talks.")
  @Description("Description of the Talk to Me app")
  String talkToMeDescription();

  @DefaultMessage("Talk to Me")
  @Description("Title of the Talk to Me app")
  String talkToMeTitle();

  @DefaultMessage("Quickly translate English to Spanish (and other languages too\\!) You''re challenged with creating an app that could act as an aid for immigrant parents who need a little extra help in English-speaking situations. Inspired by YR Media story What It''s Like to be a Translator")
  @Description("Translate App Description")
  String translateAppDescription();

  @DefaultMessage("Translate App")
  @Description("Title of the Translate App")
  String translateAppTitle();

}
