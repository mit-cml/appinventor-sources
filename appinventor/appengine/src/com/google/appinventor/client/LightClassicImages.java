// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Tree.Resources;

/**
 * Image bundle containing all client images.
 *
 * Note: Images extends Tree.Resources rather than ClientBundle so that
 * the Images can be used with the com.google.gwt.user.client.ui.Tree class.
 *
 */
public interface LightClassicImages extends Images {

  /**
   * Codi the Bee for the No Projects Dialog
   * {@link Ode#createWelcomeDialog(boolean)}
   */
  @Source("com/google/appinventor/images/classicImages/codi_vert.png")
  ImageResource codiVert();

  /**
   * App Inventor Logo
   * {@link Ode#createWelcomeDialog(boolean)}
   */
  @Source("com/google/appinventor/images/classicImages/appinvlogo-32.png")
  ImageResource appInventorLogo();

  /**
   * Close button image for the box widget
   */
  @Source("com/google/appinventor/images/classicImages/boxClose.png")
  ImageResource boxClose();

  /**
   * Menu button image for the box widget
   */
  @Source("com/google/appinventor/images/classicImages/boxMenu.png")
  ImageResource boxMenu();

  /**
   * Minimize button image for the box widget
   */
  @Source("com/google/appinventor/images/classicImages/boxMinimize.png")
  ImageResource boxMinimize();

  /**
   * Restore button image for the box widget
   */
  @Source("com/google/appinventor/images/classicImages/boxRestore.png")
  ImageResource boxRestore();

  /**
   * Close button image for the tab widget
   */
  @Source("com/google/appinventor/images/classicImages/close.png")
  ImageResource close();

  /**
   * Phone status bar for Android Holo shown above the form in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/phonebar.png")
  ImageResource phonebar();

  /**
   * Phone status bar for the Android Material form in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/phonebarAndroidMaterial.png")
  ImageResource phonebarAndroidMaterial();

  /**
   * Phone status bar for iPhone containing white left side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPhoneLeftWhiteFB.png")
  ImageResource phonebariPhoneLeftWhite();

  /**
   * Phone status bar for iPhone containing white right side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPhoneRightWhite.png")
  ImageResource phonebariPhoneRightWhite();

  /**
   * Phone status bar for iPhone containing black left side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPhoneLeftBlack.png")
  ImageResource phonebariPhoneLeftBlack();

  /**
   * Phone status bar for iPhone containing black right side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPhoneRightBlack.png")
  ImageResource phonebariPhoneRightBlack();

  /**
   * Phone status bar for iPad containing black left side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPadBlackFB.png")
  ImageResource phonebariPadLeftBlack();

  /**
   * Phone status bar for iPad containing black right side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPadRightBlack.png")
  ImageResource phonebariPadRightBlack();

  /**
   * Phone status bar for iPad containing white left side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPadLeftWhiteFB.png")
  ImageResource phonebariPadLeftWhite();

  /**
   * Phone status bar for iPad containing white right side icons in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iPadRightWhite.png")
  ImageResource phonebariPadRightWhite();

  /**
   * Phone status bar containing white book icon for the iOS form in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iOSBookmarkWhite.png")
  ImageResource bookIconWhite();

  /**
   * Phone status bar containing black book icon for the iOS form in the visual designer
   */
  @Source("com/google/appinventor/images/classicImages/iOSBookmarkBlack.png")
  ImageResource bookIconBlack();

  /**
   * Spinning/wait graphic to indicate long-running operations.
   */
  @Source("com/google/appinventor/images/classicImages/spin_16.gif")
  ImageResource waitingIcon();

  /**
   * Designer palette item: question mark for more component information
   */
  @Source("com/google/appinventor/images/classicImages/help.png")
  ImageResource help();

  /**
   * Designer palette item: nearfield component
   */
  @Source("com/google/appinventor/images/classicImages/nearfield.png")
  ImageResource nearfield();

   /**
    * Designer palette item: accelerometersensor component
    */
   @Source("com/google/appinventor/images/classicImages/accelerometersensor.png")
   ImageResource accelerometersensor();

   /**
    * Designer palette item: lightsensor component
    * <p>
    * Source: https://feathericons.com/
    */
   @Source("com/google/appinventor/images/classicImages/lightsensor.png")
   ImageResource lightsensor();

   /**
    * Designer palette item: barometer component
    * <p>
    * Source: Ellen Spertus, released into public domain
    */
   @Source("com/google/appinventor/images/classicImages/barometer.png")
   ImageResource barometer();

   /**
    * Designer palette item: thermometer component
    * <p>
    * Source: Ellen Spertus, released into public domain
    */
   @Source("com/google/appinventor/images/classicImages/thermometer.png")
   ImageResource thermometer();

  /**
    * Designer palette item: hygrometer component
    * <p>
    * Source: Ellen Spertus, released into public domain
    */
   @Source("com/google/appinventor/images/classicImages/hygrometer.png")
   ImageResource hygrometer();

  /**
   * Designer palette item: barcode scanner component
   */
  @Source("com/google/appinventor/images/classicImages/barcodeScanner.png")
  ImageResource barcodeScanner();

  /**
   * Designer palette item: button component
   */
  @Source("com/google/appinventor/images/classicImages/button.png")
  ImageResource button();

  /**
   * Designer palette item: camera declaration
   */
  @Source("com/google/appinventor/images/classicImages/camera.png")
  ImageResource camera();

  /**
   * Designer palette item: camcorder declaration
   */
  @Source("com/google/appinventor/images/classicImages/camcorder.png")
  ImageResource camcorder();

  /**
   * Designer palette item: canvas component
   */
  @Source("com/google/appinventor/images/classicImages/canvas.png")
  ImageResource canvas();

  /**
   * Designer palette item: checkbox component
   */
  @Source("com/google/appinventor/images/classicImages/checkbox.png")
  ImageResource checkbox();

  /**
   * Designer palette item: checkbox component
   */
  @Source("com/google/appinventor/images/classicImages/switch.png")
  ImageResource toggleswitch();

  /**
   * Designer palette item: DatePicker Component
   */
  @Source("com/google/appinventor/images/classicImages/datePicker.png")
  ImageResource datePickerComponent();

  /**
   * Designer palette item: Delete Component
   */
  @Source("com/google/appinventor/images/classicImages/delete.png")
  ImageResource deleteComponent();

  /**
   * Designer palette item: Extension Component
   */
  @Source("com/google/appinventor/images/classicImages/extension.png")
  ImageResource extension();

  /**
   * Designer palette item: form component
   */
  @Source("com/google/appinventor/images/classicImages/form.png")
  ImageResource form();

  /**
   * Designer palette item: horizontal arrangement component
   */
  @Source("com/google/appinventor/images/classicImages/horizontal.png")
  ImageResource horizontal();

  /**
   * Designer palette item: image component
   * Also used for image file icon for project explorer
   */
  @Source("com/google/appinventor/images/classicImages/image.png")
  ImageResource image();

  /**
   * Designer palette item: label component
   */
  @Source("com/google/appinventor/images/classicImages/label.png")
  ImageResource label();

  /**
   * Designer palette item: listbox component
   */
  @Source("com/google/appinventor/images/classicImages/listbox.png")
  ImageResource listbox();

  /**
   * Designer palette item: orientationsensor component
   */
  @Source("com/google/appinventor/images/classicImages/orientationsensor.png")
  ImageResource orientationsensor();

  /**
   * Designer palette item: player component
   */
  @Source("com/google/appinventor/images/classicImages/player.png")
  ImageResource player();

  /**
   * Designer palette item: sound recorder component
   */
  @Source("com/google/appinventor/images/classicImages/soundRecorder.png")
  ImageResource soundRecorder();

  /**
   * Designer palette item: VideoPlayer component
   */
  @Source("com/google/appinventor/images/classicImages/videoPlayer.png")
  ImageResource videoplayer();

  /**
   * Designer palette item: progressbar component
   */
  @Source("com/google/appinventor/images/classicImages/progressbar.png")
  ImageResource progressbar();

  /**
   * Designer palette item: radiobutton component
   */
  @Source("com/google/appinventor/images/classicImages/radiobutton.png")
  ImageResource radiobutton();

  /**
   * Designer palette item: textbox component
   */
  @Source("com/google/appinventor/images/classicImages/textbox.png")
  ImageResource textbox();

  /**
   * Designer palette item: PasswordTextBox component.
   */
  @Source("com/google/appinventor/images/classicImages/passwordtextbox.png")
  ImageResource passwordtextbox();

  /**
   * Designer palette item: clock component
   */
  @Source("com/google/appinventor/images/classicImages/clock.png")
  ImageResource clock();

  /**
   * Designer palette item: SoundEffect component
   */
  @Source("com/google/appinventor/images/classicImages/soundEffect.png")
  ImageResource soundeffect();

  /**
   * Designer palette item: ContactPicker component
   */
  @Source("com/google/appinventor/images/classicImages/contactPicker.png")
  ImageResource contactpicker();

  /**
   * Designer palette item: PhoneNumberPicker component
   */
  @Source("com/google/appinventor/images/classicImages/phoneNumberPicker.png")
  ImageResource phonenumberpicker();

  /**
   * Designer palette item: ImagePicker component
   */
  @Source("com/google/appinventor/images/classicImages/imagePicker.png")
  ImageResource imagepicker();

  /**
   * Designer palette item: ListPicker component
   */
  @Source("com/google/appinventor/images/classicImages/listPicker.png")
  ImageResource listpicker();

  /**
   * Designer palette item: ListView component
   */
  @Source("com/google/appinventor/images/classicImages/listView.png")
  ImageResource listview();

  /**
 * Designer palette item: ListView component
 */

 @Source("com/google/appinventor/images/classicImages/recyclerView.png")
 ImageResource recyclerview();

  /**
   * Designer palette item: PhoneCall component
   */
  @Source("com/google/appinventor/images/classicImages/phoneCall.png")
  ImageResource phonecall();

  /**
   * Designer palette item: ActivityStarter component
   */
  @Source("com/google/appinventor/images/classicImages/activityStarter.png")
  ImageResource activitystarter();

  /**
   * Designer palette item: EmailPicker component
   */
  @Source("com/google/appinventor/images/classicImages/emailPicker.png")
  ImageResource emailpicker();

  /**
   * Designer palette item: Texting component
   */
  @Source("com/google/appinventor/images/classicImages/texting.png")
  ImageResource texting();

  /**
   * Designer palette item: GameClient component
   */
  @Source("com/google/appinventor/images/classicImages/gameClient.png")
  ImageResource gameclient();

  /**
   * Designer pallete item: Spreadsheet component
   */
  @Source("com/google/appinventor/images/classicImages/spreadsheet.png")
  ImageResource spreadsheet();

  /**
   * Designer palette item: Sprite
   */
  @Source("com/google/appinventor/images/classicImages/imageSprite.png")
  ImageResource imageSprite();

  /**
   * Designer palette item: Ball
   */
  @Source("com/google/appinventor/images/classicImages/ball.png")
  ImageResource ball();

  /**
   * Designer palette item: Slider
   */
  @Source("com/google/appinventor/images/classicImages/slider.png")
  ImageResource slider();

  /**
   * Designer palette item: Notifier
   */
  @Source("com/google/appinventor/images/classicImages/notifier.png")
  ImageResource notifier();

  /**
   * Designer palette item: LocationSensor
   */
  @Source("com/google/appinventor/images/classicImages/locationSensor.png")
  ImageResource locationSensor();

  /**
   * Designer palette item: SpeechRecognizer component
   */
  @Source("com/google/appinventor/images/classicImages/speechRecognizer.png")
  ImageResource speechRecognizer();

  /**
   * Designer palette item: table arrangement component
   */
  @Source("com/google/appinventor/images/classicImages/table.png")
  ImageResource table();

  /**
   * Designer palette item: Twitter Component
   */
  @Source("com/google/appinventor/images/classicImages/twitter.png")
  ImageResource twitterComponent();

  /**
   * Designer palette item: TimePicker Component
   */
  @Source("com/google/appinventor/images/classicImages/timePicker.png")
  ImageResource timePickerComponent();

  /**
   * Designer palette item: TinyDB Component
   */
  @Source("com/google/appinventor/images/classicImages/tinyDB.png")
  ImageResource tinyDB();

  /**
   * Designer palette item: File Component
   */
  @Source("com/google/appinventor/images/classicImages/file.png")
  ImageResource file();

  /**
   * Designer palette item: TinyWebDB Component
   */
  @Source("com/google/appinventor/images/classicImages/tinyWebDB.png")
  ImageResource tinyWebDB();

  /**
   * Designer palette item: FirebaseDB Component
   */
  @Source("com/google/appinventor/images/classicImages/firebaseDB.png")
  ImageResource firebaseDB();

  /**
   * Designer palette item: TextToSpeech component
   */
  @Source("com/google/appinventor/images/classicImages/textToSpeech.png")
  ImageResource textToSpeech();

  /**
   * Designer palette item: vertical arrangement component
   */
  @Source("com/google/appinventor/images/classicImages/vertical.png")
  ImageResource vertical();

  /**
   * Designer palette item: VotingComponent
   */
  @Source("com/google/appinventor/images/classicImages/voting.png")
  ImageResource voting();

  /**
   * Designer palette item: ImageBot.
   */
  @Source("com/google/appinventor/images/classicImages/paintpalette.png")
  ImageResource paintPalette();

  /**
   * Designer palette item: Pedometer Component
   */
  @Source("com/google/appinventor/images/classicImages/pedometer.png")
  ImageResource pedometerComponent();

  /**
   * Designer pallete item: PhoneStatus Component
   */
  @Source("com/google/appinventor/images/classicImages/phoneip.png")
  ImageResource phonestatusComponent();

  /**
   * Designer palette item: Lego Mindstorms NXT components
   */
  @Source("com/google/appinventor/images/classicImages/legoMindstormsNxt.png")
  ImageResource legoMindstormsNxt();

  /**
   * Designer palette item: Lego Mindstorms EV3 components
   */
  @Source("com/google/appinventor/images/classicImages/legoMindstormsEv3.png")
  ImageResource legoMindstormsEv3();

  /**
   * Designer palette item: Bluetooth components
   */
  @Source("com/google/appinventor/images/classicImages/bluetooth.png")
  ImageResource bluetooth();

  /**
   * Designer palette item: FusiontablesControl component
   */
  @Source("com/google/appinventor/images/classicImages/fusiontables.png")
  ImageResource fusiontables();

  /**
   * Designer palette item: WebViewer component
   */
  @Source("com/google/appinventor/images/classicImages/webviewer.png")
  ImageResource webviewer();

  /**
   * Designer item: WebViewer component in designer
   */
  // The image here is public domain and comes from
  // www.pdclipart.org/displayimage.php/?pid=1047
  @Source("com/google/appinventor/images/classicImages/webviewerbig.png")
  ImageResource webviewerbig();

  /**
   * Designer palette item: Web component
   */
  @Source("com/google/appinventor/images/classicImages/web.png")
  ImageResource web();

  /**
   * Designer palette item: GyroscopeSensor component
   */
  @Source("com/google/appinventor/images/classicImages/gyroscopesensor.png")
  ImageResource gyroscopesensor();

  /**
   * Built in drawer item: control
   */
  @Source("com/google/appinventor/images/classicImages/control.png")
  ImageResource control();

  /**
   * Built in drawer item: logic
   */
  @Source("com/google/appinventor/images/classicImages/logic.png")
  ImageResource logic();

  /**
   * Built in drawer item: math
   */
  @Source("com/google/appinventor/images/classicImages/math.png")
  ImageResource math();

  /**
   * Built in drawer item: text
   */
  @Source("com/google/appinventor/images/classicImages/text.png")
  ImageResource text();

  /**
   * Built in drawer item: lists
   */
  @Source("com/google/appinventor/images/classicImages/lists.png")
  ImageResource lists();

  /**
   * Built in drawer item: dictionaries
   */
  @Source("com/google/appinventor/images/classicImages/dictionaries.png")
  ImageResource dictionaries();

  /**
   * Built in drawer item: colors
   */
  @Source("com/google/appinventor/images/classicImages/colors.png")
  ImageResource colors();

  /**
   * Built in drawer item: variables
   */
  @Source("com/google/appinventor/images/classicImages/variables.png")
  ImageResource variables();

  /**
   * Built in drawer item: procedures
   */
  @Source("com/google/appinventor/images/classicImages/procedures.png")
  ImageResource procedures();
  /**
    * Designer palette item: MediaStore
    */
  @Source("com/google/appinventor/images/classicImages/mediastore.png")
  ImageResource mediastore();

  /**
   * Designer palette item: Sharing Component
   */
  @Source("com/google/appinventor/images/classicImages/sharing.png")
  ImageResource sharingComponent();

  /**
    * Designer palette item: Spinner
    */
  @Source("com/google/appinventor/images/classicImages/spinner.png")
  ImageResource spinner();

  /**
   * Designer palette item: Translator Component
   */
  @Source("com/google/appinventor/images/classicImages/translator.png")
  ImageResource translator();

  /**
   * Designer pallette item: ChatBot Component
   */
  @Source("com/google/appinventor/images/classicImages/chatbot.png")
  ImageResource chatbot();

  /**
    * Designer palette item: YandexTranslate
    */
  @Source("com/google/appinventor/images/classicImages/yandex.png")
  ImageResource yandex();

  /**
   * Designer palette item: proximitysensor component
   */
  @Source("com/google/appinventor/images/classicImages/proximitysensor.png")
  ImageResource proximitysensor();

  /**
   * Designer palette item: cloudDB component
   */
  @Source("com/google/appinventor/images/classicImages/cloudDB.png")
  ImageResource cloudDB();

  /**
   * Designer palette item: Map
   */
  @Source("com/google/appinventor/images/classicImages/map.png")
  ImageResource map();

  /**
   * Designer palette item: Marker
   */
  @Source("com/google/appinventor/images/classicImages/marker.png")
  ImageResource marker();

  /**
   * Designer palette item: Circle
   */
  @Source("com/google/appinventor/images/classicImages/circle.png")
  ImageResource circle();

  /**
   * Designer palette item: FeatureCollection
   */
  @Source("com/google/appinventor/images/classicImages/featurecollection.png")
  ImageResource featurecollection();

  /**
   * Designer palette item: LineString
   */
  @Source("com/google/appinventor/images/classicImages/linestring.png")
  ImageResource linestring();

  /**
   * Designer palette item: Polygon
   */
  @Source("com/google/appinventor/images/classicImages/polygon.png")
  ImageResource polygon();

  /**
   * Designer palette item: Rectangle
   */
  @Source("com/google/appinventor/images/classicImages/rectangle.png")
  ImageResource rectangle();

  /**
   * Codi Logo
   */
   @Source("com/google/appinventor/images/classicImages/logo.png")
   ImageResource logo();

  /**
   * Designer palette item: Arduino component
   */
  @Source("com/google/appinventor/images/classicImages/arduino.png")
  ImageResource arduino();
  
  /**
   * Media icon: image
   */
  @Source("com/google/appinventor/images/classicImages/mediaIcon_img.png")
  ImageResource mediaIconImg();

  /**
   * Media icon: audio
   */
  @Source("com/google/appinventor/images/classicImages/mediaIcon_audio.png")
  ImageResource mediaIconAudio();

  /**
   * Media icon: video
   */
  @Source("com/google/appinventor/images/classicImages/mediaIcon_video.png")
  ImageResource mediaIconVideo();
  
  /**
   * Designer palette item: Magnetic Field Sensor component
   */
  @Source("com/google/appinventor/images/classicImages/magneticSensor.png")
  ImageResource magneticSensor();

  /**
   * Designer palette item:
   */
  @Source("com/google/appinventor/images/classicImages/navigation.png")
  ImageResource navigationComponent();

  /**
  * Wilson Logo 
  */
  @Source("com/google/appinventor/images/classicImages/wilson.png")
  ImageResource wilson();

  /**
  * Talk to Me Logo
  */
  @Source("com/google/appinventor/images/classicImages/talkToMeLogo.png")
  ImageResource talkToMeLogo();

  /**
  * YR Media Logo
  */
  @Source("com/google/appinventor/images/classicImages/YRLogo.png")
  ImageResource YRLogo();

  /**
   * Download app icon
   */
  @Source("com/google/appinventor/images/classicImages/get-app.png")
  ImageResource GetApp();

  /**
   * Designer palette item: Chart.
   */
  @Source("com/google/appinventor/images/classicImages/chart.png")
  ImageResource chart();

  /**
   * Designer palette item: ChartData2D.
   */
  @Source("com/google/appinventor/images/classicImages/chartData.png")
  ImageResource chartData2D();

  /**
   * Designer palette item: Regression.
   */
  @Source("com/google/appinventor/images/classicImages/regression.png")
  ImageResource regression();

  /**
   * Designer palette item: AnomalyDetection.
   */
  @Source("com/google/appinventor/images/classicImages/anomaly.png")
  ImageResource anomalyDetection();

  /**
   * Designer palette item: DataFile.
   */
  @Source("com/google/appinventor/images/classicImages/dataFile.png")
  ImageResource dataFile();
}
