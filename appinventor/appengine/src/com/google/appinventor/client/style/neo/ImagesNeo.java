// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.gwt.resources.client.ImageResource;

public interface ImagesNeo extends Images {
   /*
   * These are from Google's Material Icon set https://fonts.google.com/icons
   * */

  /**
   * Designer palette item: nearfield component
   */
  @Override
  @Source("images/nearfield.png")
  ImageResource nearfield();

  /**
   * Designer palette item: accelerometersensor component
   */
  @Override
  @Source("images/accelerometersensor.png")
  ImageResource accelerometersensor();

  /**
   * Designer palette item: lightsensor component
   * <p>
   * Source: https://feathericons.com/
   */
  @Override
  @Source("images/lightsensor.png")
  ImageResource lightsensor();

  /**
   * Designer palette item: barometer component
   * <p>
   * Source: Ellen Spertus, released into public domain
   */
  @Override
  @Source("images/barometer.png")
  ImageResource barometer();

  /**
   * Designer palette item: thermometer component
   * <p>
   * Source: Ellen Spertus, released into public domain
   */
  @Override
  @Source("images/thermometer.png")
  ImageResource thermometer();

  /**
   * Designer palette item: hygrometer component
   * <p>
   * Source: Ellen Spertus, released into public domain
   */
  @Override
  @Source("images/hygrometer.png")
  ImageResource hygrometer();

  /**
   * Designer palette item: barcode scanner component
   */
  @Override
  @Source("images/barcodeScanner.png")
  ImageResource barcodeScanner();

  /**
   * Designer palette item: button component
   * Material icon: buttons_alt
   */
  @Override
  @Source("images/button.png")
  ImageResource button();

  /**
   * Designer palette item: camera declaration
   */
  @Override
  @Source("images/camera.png")
  ImageResource camera();

  /**
   * Designer palette item: camcorder declaration
   */
  @Override
  @Source("images/camcorder.png")
  ImageResource camcorder();

  /**
   * Designer palette item: canvas component
   */
  @Override
  @Source("images/canvas.png")
  ImageResource canvas();

  /**
   * Designer palette item: checkbox component
   */
  @Override
  @Source("images/checkbox.png")
  ImageResource checkbox();

  /**
   * Designer palette item: checkbox component
   */
  @Override
  @Source("images/switch.png")
  ImageResource toggleswitch();

  /**
   * Designer palette item: DatePicker Component
   */
  @Override
  @Source("images/datePicker.png")
  ImageResource datePickerComponent();

  /**
   * Designer palette item: form component
   */
  @Source("com/google/appinventor/images/form.png")
  ImageResource form();

  /**
   * Designer palette item: horizontal arrangement component
   */
  @Override
  @Source("images/horizontal.png")
  ImageResource horizontal();

  /**
   * Designer palette item: image component
   * Also used for image file icon for project explorer
   */
  @Override
  @Source("images/image.png")
  ImageResource image();

  /**
   * Designer palette item: label component
   */
  @Override
  @Source("images/label.png")
  ImageResource label();

  /**
   * Designer palette item: listbox component
   */
  @Override
  @Source("images/listbox.png")
  ImageResource listbox();

  /**
   * Designer palette item: orientationsensor component
   */
  @Override
  @Source("images/orientationsensor.png")
  ImageResource orientationsensor();

  /**
   * Designer palette item: player component
   */
  @Override
  @Source("images/player.png")
  ImageResource player();

  /**
   * Designer palette item: sound recorder component
   */
  @Override
  @Source("images/soundRecorder.png")
  ImageResource soundRecorder();

  /**
   * Designer palette item: VideoPlayer component
   */
  @Override
  @Source("images/videoPlayer.png")
  ImageResource videoplayer();

  /**
   * Designer palette item: progressbar component
   */
  @Override
  @Source("images/progressbar.png")
  ImageResource progressbar();

  /**
   * Designer palette item: radiobutton component
   */
  @Override
  @Source("images/radiobutton.png")
  ImageResource radiobutton();

  /**
   * Designer palette item: textbox component
   */
  @Override
  @Source("images/textbox.png")
  ImageResource textbox();

  /**
   * Designer palette item: PasswordTextBox component.
   */
  @Override
  @Source("images/passwordtextbox.png")
  ImageResource passwordtextbox();

  /**
   * Designer palette item: clock component
   */
  @Override
  @Source("images/clock.png")
  ImageResource clock();

  /**
   * Designer palette item: SoundEffect component
   */
  @Override
  @Source("images/soundEffect.png")
  ImageResource soundeffect();

  /**
   * Designer palette item: ContactPicker component
   */
  @Override
  @Source("images/contactPicker.png")
  ImageResource contactpicker();

  /**
   * Designer palette item: PhoneNumberPicker component
   */
  @Override
  @Source("images/phoneNumberPicker.png")
  ImageResource phonenumberpicker();

  /**
   * Designer palette item: ImagePicker component
   */
  @Override
  @Source("images/imagePicker.png")
  ImageResource imagepicker();

  /**
   * Designer palette item: ListPicker component
   */
  @Override
  @Source("images/listPicker.png")
  ImageResource listpicker();

  /**
   * Designer palette item: ListView component
   */
  @Override
  @Source("images/listView.png")
  ImageResource listview();

  /**
   * Designer palette item: ListView component
   */

  @Override
  @Source("images/recyclerView.png")
  ImageResource recyclerview();

  /**
   * Designer palette item: PhoneCall component
   */
  @Override
  @Source("images/phoneCall.png")
  ImageResource phonecall();

  /**
   * Designer palette item: ActivityStarter component
   */
  @Override
  @Source("images/activityStarter.png")
  ImageResource activitystarter();

  /**
   * Designer palette item: EmailPicker component
   */
  @Override
  @Source("images/emailPicker.png")
  ImageResource emailpicker();

  /**
   * Designer palette item: Texting component
   */
  @Override
  @Source("images/texting.png")
  ImageResource texting();

  /**
   * Designer pallete item: Spreadsheet component
   */
  @Override
  @Source("images/spreadsheet.png")
  ImageResource spreadsheet();

  /**
   * Designer palette item: Sprite
   */
  @Override
  @Source("images/imageSprite.png")
  ImageResource imageSprite();

  /**
   * Designer palette item: Ball
   */
  @Override
  @Source("images/ball.png")
  ImageResource ball();

  /**
   * Designer palette item: Slider
   */
  @Override
  @Source("images/slider.png")
  ImageResource slider();

  /**
   * Designer palette item: Notifier
   */
  @Override
  @Source("images/notifier.png")
  ImageResource notifier();

  /**
   * Designer palette item: LocationSensor
   */
  @Override
  @Source("images/locationSensor.png")
  ImageResource locationSensor();

  /**
   * Designer palette item: SpeechRecognizer component
   */
  @Override
  @Source("images/speechRecognizer.png")
  ImageResource speechRecognizer();

  /**
   * Designer palette item: table arrangement component
   */
  @Override
  @Source("images/table.png")
  ImageResource table();

  /**
   * Designer palette item: Twitter Component
   */
  @Override
  @Source("images/twitter.png")
  ImageResource twitterComponent();

  /**
   * Designer palette item: TimePicker Component
   */
  @Override
  @Source("images/timePicker.png")
  ImageResource timePickerComponent();

  /**
   * Designer palette item: TinyDB Component
   */
  @Override
  @Source("images/tinyDB.png")
  ImageResource tinyDB();

  /**
   * Designer palette item: File Component
   */
  @Override
  @Source("images/file.png")
  ImageResource file();

  /**
   * Designer palette item: TinyWebDB Component
   */
  @Override
  @Source("images/tinyWebDB.png")
  ImageResource tinyWebDB();

  /**
   * Designer palette item: FirebaseDB Component
   */
  @Override
  @Source("images/firebaseDB.png")
  ImageResource firebaseDB();

  /**
   * Designer palette item: TextToSpeech component
   */
  @Override
  @Source("images/textToSpeech.png")
  ImageResource textToSpeech();

  /**
   * Designer palette item: vertical arrangement component
   */
  @Override
  @Source("images/vertical.png")
  ImageResource vertical();

  /**
   * Designer palette item: ImageBot.
   * Material icon: palette
   */
  @Override
  @Source("images/paintpalette.png")
  ImageResource paintPalette();

  /**
   * Designer palette item: Pedometer Component
   */
  @Override
  @Source("images/pedometer.png")
  ImageResource pedometerComponent();

  /**
   * Designer palette item: Lego Mindstorms NXT components
   */
  @Override
  @Source("images/legoMindstormsNxt.png")
  ImageResource legoMindstormsNxt();

  /**
   * Designer palette item: Lego Mindstorms EV3 components
   */
  @Override
  @Source("images/legoMindstormsEv3.png")
  ImageResource legoMindstormsEv3();

  /**
   * Designer palette item: Bluetooth components
   */
  @Override
  @Source("images/bluetooth.png")
  ImageResource bluetooth();

  /**
   * Designer palette item: WebViewer component
   */
  @Override
  @Source("images/webviewer.png")
  ImageResource webviewer();

  /**
   * Designer palette item: Web component
   */
  @Override
  @Source("images/web.png")
  ImageResource web();

  /**
   * Designer palette item: GyroscopeSensor component
   */
  @Override
  @Source("images/gyroscopesensor.png")
  ImageResource gyroscopesensor();

  /**
   * Designer palette item: Sharing Component
   */
  @Override
  @Source("images/sharing.png")
  ImageResource sharingComponent();

  /**
   * Designer palette item: Spinner
   */
  @Override
  @Source("images/spinner.png")
  ImageResource spinner();

  /**
   * Designer palette item: Translator Component
   */
  @Override
  @Source("images/translator.png")
  ImageResource translator();

  /**
   * Designer pallette item: ChatBot Component
   * Material icon: forum
   */
  @Override
  @Source("images/chatbot.png")
  ImageResource chatbot();

  /**
   * Designer palette item: proximitysensor component
   */
  @Override
  @Source("images/proximitysensor.png")
  ImageResource proximitysensor();

  /**
   * Designer palette item: cloudDB component
   */
  @Override
  @Source("images/cloudDB.png")
  ImageResource cloudDB();

  /**
   * Designer palette item: Map
   */
  @Override
  @Source("images/map.png")
  ImageResource map();

  /**
   * Designer palette item: Marker
   */
  @Override
  @Source("images/marker.png")
  ImageResource marker();

  /**
   * Designer palette item: Circle
   */
  @Override
  @Source("images/circle.png")
  ImageResource circle();

  /**
   * Designer palette item: FeatureCollection
   */
  @Override
  @Source("images/featurecollection.png")
  ImageResource featurecollection();

  /**
   * Designer palette item: LineString
   */
  @Override
  @Source("images/linestring.png")
  ImageResource linestring();

  /**
   * Designer palette item: Polygon
   */
  @Override
  @Source("images/polygon.png")
  ImageResource polygon();

  /**
   * Designer palette item: Rectangle
   */
  @Override
  @Source("images/rectangle.png")
  ImageResource rectangle();

  /**
   * Designer palette item: Arduino component
   */
  @Override
  @Source("images/arduino.png")
  ImageResource arduino();

  /**
   * Designer palette item: Magnetic Field Sensor component
   */
  @Override
  @Source("images/magneticSensor.png")
  ImageResource magneticSensor();

  /**
   * Designer palette item:
   */
  @Override
  @Source("images/navigation.png")
  ImageResource navigationComponent();

  /**
   * Download app icon
   */
  @Override
  @Source("images/get-app.png")
  ImageResource GetApp();

  /**
   * Designer palette item: Chart.
   */
  @Override
  @Source("images/chart.png")
  ImageResource chart();

  /**
   * Designer palette item: ChartData2D.
   */
  @Override
  @Source("images/chartData.png")
  ImageResource chartData2D();

  /**
   * Designer palette item: Regression.
   * Material icon: query_stats
   */
  @Override
  @Source("images/regression.png")
  ImageResource regression();

  /**
   * Designer palette item: AnomalyDetection.
   * Material icon: error
   */
  @Override
  @Source("images/anomaly.png")
  ImageResource anomalyDetection();

  /**
   * Designer palette item: DataFile.
   */
  @Override
  @Source("images/dataFile.png")
  ImageResource dataFile();

  /**
   * Designer palette item: progressbar circular component
   * Material icon: progress_activity
   */
  @Source("images/circularProgress.png")
  ImageResource circularProgress();

  /**
   * Designer palette item: progressbar circular component
   * Material icon: keyboard_double_arrow_right
   */
  @Source("images/linearProgress.png")
  ImageResource linearProgress();

  /**
   * Designer palette item: Trendline.
   * Material icon: trending_up
   */
  @Source("images/trendline.png")
  ImageResource trendline();
}
