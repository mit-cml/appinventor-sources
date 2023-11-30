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
public interface LightNewImages extends Images {
    /** 
     * Codi the Bee for the No Projects Dialog
     * {@link Ode#createWelcomeDialog(boolean)}
     */
    @Source("com/google/appinventor/images/GSoCimages/codi_vert.png")
    ImageResource codiVert();

    /**
     * App Inventor Logo
     * {@link Ode#createWelcomeDialog(boolean)}
     */
    @Source("com/google/appinventor/images/GSoCimages/appinvlogo-32.png")
    ImageResource appInventorLogo();

    /**
     * Close button image for the box widget
     */
    @Source("com/google/appinventor/images/GSoCimages/boxClose.png")
    ImageResource boxClose();

    /**
     * Menu button image for the box widget
     */
    @Source("com/google/appinventor/images/GSoCimages/boxMenu.png")
    ImageResource boxMenu();

    /**
     * Minimize button image for the box widget
     */
    @Source("com/google/appinventor/images/GSoCimages/boxMinimize.png")
    ImageResource boxMinimize();

    /**
     * Restore button image for the box widget
     */
    @Source("com/google/appinventor/images/GSoCimages/boxRestore.png")
    ImageResource boxRestore();

    /**
     * Close button image for the tab widget
     */
    @Source("com/google/appinventor/images/GSoCimages/close.png")
    ImageResource close();

    /**
     * Phone status bar for Android Holo shown above the form in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/phonebar.png")
    ImageResource phonebar();

    /**
     * Phone status bar for the Android Material form in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/phonebarAndroidMaterial.png")
    ImageResource phonebarAndroidMaterial();

    /**
     * Phone status bar for iPhone containing white left side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPhoneLeftWhiteFB.png")
    ImageResource phonebariPhoneLeftWhite();

    /**
     * Phone status bar for iPhone containing white right side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPhoneRightWhite.png")
    ImageResource phonebariPhoneRightWhite();

    /**
     * Phone status bar for iPhone containing black left side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPhoneLeftBlack.png")
    ImageResource phonebariPhoneLeftBlack();

    /**
     * Phone status bar for iPhone containing black right side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPhoneRightBlack.png")
    ImageResource phonebariPhoneRightBlack();

    /**
     * Phone status bar for iPad containing black left side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPadBlackFB.png")
    ImageResource phonebariPadLeftBlack();

    /**
     * Phone status bar for iPad containing black right side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPadRightBlack.png")
    ImageResource phonebariPadRightBlack();

    /**
     * Phone status bar for iPad containing white left side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPadLeftWhiteFB.png")
    ImageResource phonebariPadLeftWhite();

    /**
     * Phone status bar for iPad containing white right side icons in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iPadRightWhite.png")
    ImageResource phonebariPadRightWhite();

    /**
     * Phone status bar containing white book icon for the iOS form in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iOSBookmarkWhite.png")
    ImageResource bookIconWhite();

    /**
     * Phone status bar containing black book icon for the iOS form in the visual designer
     */
    @Source("com/google/appinventor/images/GSoCimages/iOSBookmarkBlack.png")
    ImageResource bookIconBlack();

    /**
     * Spinning/wait graphic to indicate long-running operations.
     */
    @Source("com/google/appinventor/images/GSoCimages/spin_16.gif")
    ImageResource waitingIcon();

    /**
     * Designer palette item: question mark for more component information
     */
    @Source("com/google/appinventor/images/GSoCimages/help.png")
    ImageResource help();

    /**
     * Designer palette item: nearfield component
     */
    @Source("com/google/appinventor/images/GSoCimages/nearfield.png")
    ImageResource nearfield();

    /**
        * Designer palette item: accelerometersensor component
        */
    @Source("com/google/appinventor/images/GSoCimages/accelerometersensor.png")
    ImageResource accelerometersensor();

    /**
        * Designer palette item: lightsensor component
        * <p>
        * Source: https://feathericons.com/
        */
    @Source("com/google/appinventor/images/GSoCimages/lightsensor.png")
    ImageResource lightsensor();

    /**
        * Designer palette item: barometer component
        * <p>
        * Source: Ellen Spertus, released into public domain
        */
    @Source("com/google/appinventor/images/GSoCimages/barometer.png")
    ImageResource barometer();

    /**
        * Designer palette item: thermometer component
        * <p>
        * Source: Ellen Spertus, released into public domain
        */
    @Source("com/google/appinventor/images/GSoCimages/thermometer.png")
    ImageResource thermometer();

    /**
        * Designer palette item: hygrometer component
        * <p>
        * Source: Ellen Spertus, released into public domain
        */
    @Source("com/google/appinventor/images/GSoCimages/hygrometer.png")
    ImageResource hygrometer();

    /**
     * Designer palette item: barcode scanner component
     */
    @Source("com/google/appinventor/images/GSoCimages/barcodeScanner.png")
    ImageResource barcodeScanner();

    /**
     * Designer palette item: button component
     */
    @Source("com/google/appinventor/images/GSoCimages/button.png")
    ImageResource button();

    /**
     * Designer palette item: camera declaration
     */
    @Source("com/google/appinventor/images/GSoCimages/camera.png")
    ImageResource camera();

    /**
     * Designer palette item: camcorder declaration
     */
    @Source("com/google/appinventor/images/GSoCimages/camcorder.png")
    ImageResource camcorder();

    /**
     * Designer palette item: canvas component
     */
    @Source("com/google/appinventor/images/GSoCimages/canvas.png")
    ImageResource canvas();

    /**
     * Designer palette item: checkbox component
     */
    @Source("com/google/appinventor/images/GSoCimages/checkbox.png")
    ImageResource checkbox();

    /**
     * Designer palette item: checkbox component
     */
    @Source("com/google/appinventor/images/GSoCimages/switch.png")
    ImageResource toggleswitch();

    /**
     * Designer palette item: DatePicker Component
     */
    @Source("com/google/appinventor/images/GSoCimages/datePicker.png")
    ImageResource datePickerComponent();

    /**
     * Designer palette item: Delete Component
     */
    @Source("com/google/appinventor/images/GSoCimages/delete.png")
    ImageResource deleteComponent();

    /**
     * Designer palette item: Extension Component
     */
    @Source("com/google/appinventor/images/GSoCimages/extension.png")
    ImageResource extension();

    /**
     * Designer palette item: form component
     */
    @Source("com/google/appinventor/images/GSoCimages/form.png")
    ImageResource form();

    /**
     * Designer palette item: horizontal arrangement component
     */
    @Source("com/google/appinventor/images/GSoCimages/horizontal.png")
    ImageResource horizontal();

    /**
     * Designer palette item: image component
     * Also used for image file icon for project explorer
     */
    @Source("com/google/appinventor/images/GSoCimages/image.png")
    ImageResource image();

    /**
     * Designer palette item: label component
     */
    @Source("com/google/appinventor/images/GSoCimages/label.png")
    ImageResource label();

    /**
     * Designer palette item: listbox component
     */
    @Source("com/google/appinventor/images/GSoCimages/listbox.png")
    ImageResource listbox();

    /**
     * Designer palette item: orientationsensor component
     */
    @Source("com/google/appinventor/images/GSoCimages/orientationsensor.png")
    ImageResource orientationsensor();

    /**
     * Designer palette item: player component
     */
    @Source("com/google/appinventor/images/GSoCimages/player.png")
    ImageResource player();

    /**
     * Designer palette item: sound recorder component
     */
    @Source("com/google/appinventor/images/GSoCimages/soundRecorder.png")
    ImageResource soundRecorder();

    /**
     * Designer palette item: VideoPlayer component
     */
    @Source("com/google/appinventor/images/GSoCimages/videoPlayer.png")
    ImageResource videoplayer();

    /**
     * Designer palette item: progressbar component
     */
    @Source("com/google/appinventor/images/GSoCimages/progressbar.png")
    ImageResource progressbar();

    /**
     * Designer palette item: radiobutton component
     */
    @Source("com/google/appinventor/images/GSoCimages/radiobutton.png")
    ImageResource radiobutton();

    /**
     * Designer palette item: textbox component
     */
    @Source("com/google/appinventor/images/GSoCimages/textbox.png")
    ImageResource textbox();

    /**
     * Designer palette item: PasswordTextBox component.
     */
    @Source("com/google/appinventor/images/GSoCimages/passwordtextbox.png")
    ImageResource passwordtextbox();

    /**
     * Designer palette item: clock component
     */
    @Source("com/google/appinventor/images/GSoCimages/clock.png")
    ImageResource clock();

    /**
     * Designer palette item: SoundEffect component
     */
    @Source("com/google/appinventor/images/GSoCimages/soundEffect.png")
    ImageResource soundeffect();

    /**
     * Designer palette item: ContactPicker component
     */
    @Source("com/google/appinventor/images/GSoCimages/contactPicker.png")
    ImageResource contactpicker();

    /**
     * Designer palette item: PhoneNumberPicker component
     */
    @Source("com/google/appinventor/images/GSoCimages/phoneNumberPicker.png")
    ImageResource phonenumberpicker();

    /**
     * Designer palette item: ImagePicker component
     */
    @Source("com/google/appinventor/images/GSoCimages/imagePicker.png")
    ImageResource imagepicker();

    /**
     * Designer palette item: ListPicker component
     */
    @Source("com/google/appinventor/images/GSoCimages/listPicker.png")
    ImageResource listpicker();

    /**
     * Designer palette item: ListView component
     */
    @Source("com/google/appinventor/images/GSoCimages/listView.png")
    ImageResource listview();

    /**
     * Designer palette item: ListView component
     */

    @Source("com/google/appinventor/images/GSoCimages/recyclerView.png")
    ImageResource recyclerview();

    /**
     * Designer palette item: PhoneCall component
     */
    @Source("com/google/appinventor/images/GSoCimages/phoneCall.png")
    ImageResource phonecall();

    /**
     * Designer palette item: ActivityStarter component
     */
    @Source("com/google/appinventor/images/GSoCimages/activityStarter.png")
    ImageResource activitystarter();

    /**
     * Designer palette item: EmailPicker component
     */
    @Source("com/google/appinventor/images/GSoCimages/emailPicker.png")
    ImageResource emailpicker();

    /**
     * Designer palette item: Texting component
     */
    @Source("com/google/appinventor/images/GSoCimages/texting.png")
    ImageResource texting();

    /**
     * Designer palette item: GameClient component
     */
    @Source("com/google/appinventor/images/GSoCimages/gameClient.png")
    ImageResource gameclient();

    /**
     * Designer pallete item: Spreadsheet component
     */
    @Source("com/google/appinventor/images/GSoCimages/spreadsheet.png")
    ImageResource spreadsheet();

    /**
     * Designer palette item: Sprite
     */
    @Source("com/google/appinventor/images/GSoCimages/imageSprite.png")
    ImageResource imageSprite();

    /**
     * Designer palette item: Ball
     */
    @Source("com/google/appinventor/images/GSoCimages/ball.png")
    ImageResource ball();

    /**
     * Designer palette item: Slider
     */
    @Source("com/google/appinventor/images/GSoCimages/slider.png")
    ImageResource slider();

    /**
     * Designer palette item: Notifier
     */
    @Source("com/google/appinventor/images/GSoCimages/notifier.png")
    ImageResource notifier();

    /**
     * Designer palette item: LocationSensor
     */
    @Source("com/google/appinventor/images/GSoCimages/locationSensor.png")
    ImageResource locationSensor();

    /**
     * Designer palette item: SpeechRecognizer component
     */
    @Source("com/google/appinventor/images/GSoCimages/speechRecognizer.png")
    ImageResource speechRecognizer();

    /**
     * Designer palette item: table arrangement component
     */
    @Source("com/google/appinventor/images/GSoCimages/table.png")
    ImageResource table();

    /**
     * Designer palette item: Twitter Component
     */
    @Source("com/google/appinventor/images/GSoCimages/twitter.png")
    ImageResource twitterComponent();

    /**
     * Designer palette item: TimePicker Component
     */
    @Source("com/google/appinventor/images/GSoCimages/timePicker.png")
    ImageResource timePickerComponent();

    /**
     * Designer palette item: TinyDB Component
     */
    @Source("com/google/appinventor/images/GSoCimages/tinyDB.png")
    ImageResource tinyDB();

    /**
     * Designer palette item: File Component
     */
    @Source("com/google/appinventor/images/GSoCimages/file.png")
    ImageResource file();

    /**
     * Designer palette item: TinyWebDB Component
     */
    @Source("com/google/appinventor/images/GSoCimages/tinyWebDB.png")
    ImageResource tinyWebDB();

    /**
     * Designer palette item: FirebaseDB Component
     */
    @Source("com/google/appinventor/images/GSoCimages/firebaseDB.png")
    ImageResource firebaseDB();

    /**
     * Designer palette item: TextToSpeech component
     */
    @Source("com/google/appinventor/images/GSoCimages/textToSpeech.png")
    ImageResource textToSpeech();

    /**
     * Designer palette item: vertical arrangement component
     */
    @Source("com/google/appinventor/images/GSoCimages/vertical.png")
    ImageResource vertical();

    /**
     * Designer palette item: VotingComponent
     */
    @Source("com/google/appinventor/images/GSoCimages/voting.png")
    ImageResource voting();

    /**
     * Designer palette item: ImageBot.
     */
    @Source("com/google/appinventor/images/GSoCimages/paintpalette.png")
    ImageResource paintPalette();

    /**
     * Designer palette item: Pedometer Component
     */
    @Source("com/google/appinventor/images/GSoCimages/pedometer.png")
    ImageResource pedometerComponent();

    /**
     * Designer pallete item: PhoneStatus Component
     */
    @Source("com/google/appinventor/images/GSoCimages/phoneip.png")
    ImageResource phonestatusComponent();

    /**
     * Designer palette item: Lego Mindstorms NXT components
     */
    @Source("com/google/appinventor/images/GSoCimages/legoMindstormsNxt.png")
    ImageResource legoMindstormsNxt();

    /**
     * Designer palette item: Lego Mindstorms EV3 components
     */
    @Source("com/google/appinventor/images/GSoCimages/legoMindstormsEv3.png")
    ImageResource legoMindstormsEv3();

    /**
     * Designer palette item: Bluetooth components
     */
    @Source("com/google/appinventor/images/GSoCimages/bluetooth.png")
    ImageResource bluetooth();

    /**
     * Designer palette item: FusiontablesControl component
     */
    @Source("com/google/appinventor/images/GSoCimages/fusiontables.png")
    ImageResource fusiontables();

    /**
     * Designer palette item: WebViewer component
     */
    @Source("com/google/appinventor/images/GSoCimages/webviewer.png")
    ImageResource webviewer();

    /**
     * Designer item: WebViewer component in designer
     */
    // The image here is public domain and comes from
    // www.pdclipart.org/displayimage.php/?pid=1047
    @Source("com/google/appinventor/images/GSoCimages/webviewerbig.png")
    ImageResource webviewerbig();

    /**
     * Designer palette item: Web component
     */
    @Source("com/google/appinventor/images/GSoCimages/web.png")
    ImageResource web();

    /**
     * Designer palette item: GyroscopeSensor component
     */
    @Source("com/google/appinventor/images/GSoCimages/gyroscopesensor.png")
    ImageResource gyroscopesensor();

    /**
     * Built in drawer item: control
     */
    @Source("com/google/appinventor/images/GSoCimages/control.png")
    ImageResource control();

    /**
     * Built in drawer item: logic
     */
    @Source("com/google/appinventor/images/GSoCimages/logic.png")
    ImageResource logic();

    /**
     * Built in drawer item: math
     */
    @Source("com/google/appinventor/images/GSoCimages/math.png")
    ImageResource math();

    /**
     * Built in drawer item: text
     */
    @Source("com/google/appinventor/images/GSoCimages/text.png")
    ImageResource text();

    /**
     * Built in drawer item: lists
     */
    @Source("com/google/appinventor/images/GSoCimages/lists.png")
    ImageResource lists();

    /**
     * Built in drawer item: dictionaries
     */
    @Source("com/google/appinventor/images/GSoCimages/dictionaries.png")
    ImageResource dictionaries();

    /**
     * Built in drawer item: colors
     */
    @Source("com/google/appinventor/images/GSoCimages/colors.png")
    ImageResource colors();

    /**
     * Built in drawer item: variables
     */
    @Source("com/google/appinventor/images/GSoCimages/variables.png")
    ImageResource variables();

    /**
     * Built in drawer item: procedures
     */
    @Source("com/google/appinventor/images/GSoCimages/procedures.png")
    ImageResource procedures();
    /**
        * Designer palette item: MediaStore
        */
    @Source("com/google/appinventor/images/GSoCimages/mediastore.png")
    ImageResource mediastore();

    /**
     * Designer palette item: Sharing Component
     */
    @Source("com/google/appinventor/images/GSoCimages/sharing.png")
    ImageResource sharingComponent();

    /**
        * Designer palette item: Spinner
        */
    @Source("com/google/appinventor/images/GSoCimages/spinner.png")
    ImageResource spinner();

    /**
     * Designer palette item: Translator Component
     */
    @Source("com/google/appinventor/images/GSoCimages/translator.png")
    ImageResource translator();

    /**
     * Designer pallette item: ChatBot Component
     */
    @Source("com/google/appinventor/images/GSoCimages/chatbot.png")
    ImageResource chatbot();

    /**
        * Designer palette item: YandexTranslate
        */
    @Source("com/google/appinventor/images/GSoCimages/yandex.png")
    ImageResource yandex();

    /**
     * Designer palette item: proximitysensor component
     */
    @Source("com/google/appinventor/images/GSoCimages/proximitysensor.png")
    ImageResource proximitysensor();

    /**
     * Designer palette item: cloudDB component
     */
    @Source("com/google/appinventor/images/GSoCimages/cloudDB.png")
    ImageResource cloudDB();

    /**
     * Designer palette item: Map
     */
    @Source("com/google/appinventor/images/GSoCimages/map.png")
    ImageResource map();

    /**
     * Designer palette item: Marker
     */
    @Source("com/google/appinventor/images/GSoCimages/marker.png")
    ImageResource marker();

    /**
     * Designer palette item: Circle
     */
    @Source("com/google/appinventor/images/GSoCimages/circle.png")
    ImageResource circle();

    /**
     * Designer palette item: FeatureCollection
     */
    @Source("com/google/appinventor/images/GSoCimages/featurecollection.png")
    ImageResource featurecollection();

    /**
     * Designer palette item: LineString
     */
    @Source("com/google/appinventor/images/GSoCimages/linestring.png")
    ImageResource linestring();

    /**
     * Designer palette item: Polygon
     */
    @Source("com/google/appinventor/images/GSoCimages/polygon.png")
    ImageResource polygon();

    /**
     * Designer palette item: Rectangle
     */
    @Source("com/google/appinventor/images/GSoCimages/rectangle.png")
    ImageResource rectangle();

    /**
     * Codi Logo
     */
    @Source("com/google/appinventor/images/GSoCimages/logo.png")
    ImageResource logo();

    /**
     * Designer palette item: Arduino component
     */
    @Source("com/google/appinventor/images/GSoCimages/arduino.png")
    ImageResource arduino();
    
    /**
     * Media icon: image
     */
    @Source("com/google/appinventor/images/GSoCimages/mediaIcon_img.png")
    ImageResource mediaIconImg();

    /**
     * Media icon: audio
     */
    @Source("com/google/appinventor/images/GSoCimages/mediaIcon_audio.png")
    ImageResource mediaIconAudio();

    /**
     * Media icon: video
     */
    @Source("com/google/appinventor/images/GSoCimages/mediaIcon_video.png")
    ImageResource mediaIconVideo();
    
    /**
     * Designer palette item: Magnetic Field Sensor component
     */
    @Source("com/google/appinventor/images/GSoCimages/magneticSensor.png")
    ImageResource magneticSensor();

    /**
     * Designer palette item:
     */
    @Source("com/google/appinventor/images/GSoCimages/navigation.png")
    ImageResource navigationComponent();

    /**
     * Wilson Logo 
    */
    @Source("com/google/appinventor/images/GSoCimages/wilson.png")
    ImageResource wilson();

    /**
     * Talk to Me Logo
    */
    @Source("com/google/appinventor/images/GSoCimages/talkToMeLogo.png")
    ImageResource talkToMeLogo();

    /**
     * YR Media Logo
    */
    @Source("com/google/appinventor/images/GSoCimages/YRLogo.png")
    ImageResource YRLogo();

    /**
     * Download app icon
     */
    @Source("com/google/appinventor/images/GSoCimages/get-app.png")
    ImageResource GetApp();

    /**
     * Designer palette item: Chart.
     */
    @Source("com/google/appinventor/images/GSoCimages/chart.png")
    ImageResource chart();

    /**
     * Designer palette item: ChartData2D.
     */
    @Source("com/google/appinventor/images/GSoCimages/chartData.png")
    ImageResource chartData2D();

    /**
     * Designer palette item: Regression.
     */
    @Source("com/google/appinventor/images/GSoCimages/regression.png")
    ImageResource regression();

    /**
     * Designer palette item: AnomalyDetection.
     */
    @Source("com/google/appinventor/images/GSoCimages/anomaly.png")
    ImageResource anomalyDetection();

    /**
     * Designer palette item: DataFile.
     */
    @Source("com/google/appinventor/images/GSoCimages/dataFile.png")
    ImageResource dataFile();
}