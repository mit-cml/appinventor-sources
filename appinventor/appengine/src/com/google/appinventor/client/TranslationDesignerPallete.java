// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import static com.google.appinventor.client.Ode.MESSAGES;

public class TranslationDesignerPallete {

  public static String getCorrespondingString(String key) {
    String value = key;

    // Palette components name
    if (key.equals("User Interface")) {
      OdeLog.wlog("key exists!");
      value = MESSAGES.UIComponentPallette();
    } else if (key.equals("Layout")) {
      value = MESSAGES.layoutComponentPallette();
    } else if (key.equals("Media")) {
      value = MESSAGES.mediaComponentPallette();
    } else if (key.equals("Drawing and Animation")) {
      value = MESSAGES.drawanimationComponentPallette();
    } else if (key.equals("Social")) {
      value = MESSAGES.socialComponentPallette();
    } else if (key.equals("Sensors")) {
      value = MESSAGES.sensorsComponentPallette();
    } else if (key.equals("Social")) {
      value = MESSAGES.socialComponentPallette();
    } else if (key.equals("Storage")) {
      value = MESSAGES.storageComponentPallette();
    } else if (key.equals("Connectivity")) {
      value = MESSAGES.connectivityComponentPallette();
    } else if (key.equals("LEGO\u00AE MINDSTORMS\u00AE")) {
      value = MESSAGES.legoComponentPallette();
    } else if (key.equals("Experimental")) {
      value = MESSAGES.experimentalComponentPallette();

//     } else if (key.equals("Not ready for prime time")) {
//       value = MESSAGES.notReadyForPrimeTimeComponentPallette();
//     } else if (key.equals("Old stuff")) {
//       value = MESSAGES.oldStuffComponentPallette();

      // Basic
    } else if (key.equals("Button")) {
      value = MESSAGES.buttonComponentPallette();
    } else if (key.equals("Canvas")) {
      value = MESSAGES.canvasComponentPallette();
    } else if (key.equals("CheckBox")) {
      value = MESSAGES.checkBoxComponentPallette();
    } else if (key.equals("Clock")) {
      value = MESSAGES.clockComponentPallette();
    } else if (key.equals("DatePicker")) {
      value = MESSAGES.datePickerComponentPallette();
    } else if (key.equals("Form")) {
      value = MESSAGES.FormComponentPallette();
    } else if (key.equals("Image")) {
      value = MESSAGES.imageComponentPallette();
    } else if (key.equals("Label")) {
      value = MESSAGES.labelComponentPallette();
    } else if (key.equals("ListPicker")) {
      value = MESSAGES.listPickerComponentPallette();
    } else if (key.equals("ListView")) {
      value = MESSAGES.listViewComponentPallette();
    } else if (key.equals("PasswordTextBox")) {
      value = MESSAGES.passwordTextBoxComponentPallette();
    } else if (key.equals("Slider")) {
      value = MESSAGES.sliderComponentPallette();
    } else if (key.equals("Spinner")) {
      value = MESSAGES.spinnerComponentPallette();
    } else if (key.equals("TextBox")) {
      value = MESSAGES.textBoxComponentPallette();
    } else if (key.equals("TimePicker")) {
      value = MESSAGES.timePickerComponentPallette();
    } else if (key.equals("TinyDB")) {
      value = MESSAGES.tinyDBComponentPallette();
      // Media
    } else if (key.equals("Camcorder")) {
      value = MESSAGES.camcorderComponentPallette();
    } else if (key.equals("Camera")) {
      value = MESSAGES.cameraComponentPallette();
    } else if (key.equals("ImagePicker")) {
      value = MESSAGES.imagePickerComponentPallette();
    } else if (key.equals("Player")) {
      value = MESSAGES.playerComponentPallette();
    } else if (key.equals("Sound")) {
      value = MESSAGES.soundComponentPallette();
    } else if (key.equals("VideoPlayer")) {
      value = MESSAGES.videoPlayerComponentPallette();
    } else if (key.equals("YandexTranslate")) {
      value = MESSAGES.yandexTranslateComponentPallette();
      // Animation
    } else if (key.equals("Ball")) {
      value = MESSAGES.ballComponentPallette();
    } else if (key.equals("ImageSprite")) {
      value = MESSAGES.imageSpriteComponentPallette();
      // Social
    } else if (key.equals("ContactPicker")) {
      value = MESSAGES.contactPickerComponentPallette();
    } else if (key.equals("EmailPicker")) {
      value = MESSAGES.emailPickerComponentPallette();
    } else if (key.equals("PhoneCall")) {
      value = MESSAGES.phoneCallComponentPallette();
    } else if (key.equals("PhoneNumberPicker")) {
      value = MESSAGES.phoneNumberPickerComponentPallette();
    } else if (key.equals("Sharing")) {
      value = MESSAGES.sharingComponentPallette();
    } else if (key.equals("Texting")) {
      value = MESSAGES.textingComponentPallette();
    } else if (key.equals("Twitter")) {
      value = MESSAGES.twitterComponentPallette();
      // Sensor
    } else if (key.equals("AccelerometerSensor")) {
      value = MESSAGES.accelerometerSensorComponentPallette();
    } else if (key.equals("LocationSensor")) {
      value = MESSAGES.locationSensorComponentPallette();
    } else if (key.equals("NearField")) {
      value = MESSAGES.nearFieldComponentPallette();
    } else if (key.equals("OrientationSensor")) {
      value = MESSAGES.orientationSensorComponentPallette();
      // Screen Arrangement
    } else if (key.equals("HorizontalArrangement")) {
      value = MESSAGES.horizontalArrangementComponentPallette();
    } else if (key.equals("TableArrangement")) {
      value = MESSAGES.tableArrangementComponentPallette();
    } else if (key.equals("VerticalArrangement")) {
      value = MESSAGES.verticalArrangementComponentPallette();
      // Lego Mindstorms
    } else if (key.equals("NxtColorSensor")) {
      value = MESSAGES.nxtColorSensorComponentPallette();
    } else if (key.equals("NxtDirectCommands")) {
      value = MESSAGES.nxtDirectCommandsComponentPallette();
    } else if (key.equals("NxtDrive")) {
      value = MESSAGES.nxtDriveComponentPallette();
    } else if (key.equals("NxtLightSensor")) {
      value = MESSAGES.nxtLightSensorComponentPallette();
    } else if (key.equals("NxtSoundSensor")) {
      value = MESSAGES.nxtSoundSensorComponentPallette();
    } else if (key.equals("NxtTouchSensor")) {
      value = MESSAGES.nxtTouchSensorComponentPallette();
    } else if (key.equals("NxtUltrasonicSensor")) {
      value = MESSAGES.nxtUltrasonicSensorComponentPallette();
      // Other stuff
    } else if (key.equals("ActivityStarter")) {
      value = MESSAGES.activityStarterComponentPallette();
    } else if (key.equals("BarcodeScanner")) {
      value = MESSAGES.barcodeScannerComponentPallette();
    } else if (key.equals("BluetoothClient")) {
      value = MESSAGES.bluetoothClientComponentPallette();
    } else if (key.equals("BluetoothServer")) {
      value = MESSAGES.bluetoothServerComponentPallette();
    } else if (key.equals("Notifier")) {
      value = MESSAGES.notifierComponentPallette();
    } else if (key.equals("ProximitySensor")) {
      value = MESSAGES.proximitySensorComponentPallette();
    } else if (key.equals("SpeechRecognizer")) {
      value = MESSAGES.speechRecognizerComponentPallette();
    } else if (key.equals("TextToSpeech")) {
      value = MESSAGES.textToSpeechComponentPallette();
    } else if (key.equals("TinyWebDB")) {
      value = MESSAGES.tinyWebDBComponentPallette();
    } else if (key.equals("Web")) {
      value = MESSAGES.webComponentPallette();
      // Not ready for prime time
    } else if (key.equals("File")) {
      value = MESSAGES.fileComponentPallette();
    } else if (key.equals("FusiontablesControl")) {
      value = MESSAGES.fusiontablesControlComponentPallette();
    } else if (key.equals("GameClient")) {
      value = MESSAGES.gameClientComponentPallette();
    } else if (key.equals("SoundRecorder")) {
      value = MESSAGES.soundRecorderComponentPallette();
    } else if (key.equals("Voting")) {
      value = MESSAGES.votingComponentPallette();
    } else if (key.equals("WebViewer")) {
      value = MESSAGES.webViewerComponentPallette();

      //help strings

    } else if (key.equals("AccelerometerSensor-helpString")) {
      value = MESSAGES.AccelerometerSensorHelpStringComponentPallette();
    } else if (key.equals("ActivityStarter-helpString")) {
      value = MESSAGES.ActivityStarterHelpStringComponentPallette();
    } else if (key.equals("Ball-helpString")) {
      value = MESSAGES.BallHelpStringComponentPallette();
    } else if (key.equals("BarcodeScanner-helpString")) {
      value = MESSAGES.BarcodeScannerHelpStringComponentPallette();
    } else if (key.equals("BluetoothClient-helpString")) {
      value = MESSAGES.BluetoothClientHelpStringComponentPallette();
    } else if (key.equals("BluetoothServer-helpString")) {
      value = MESSAGES.BluetoothServerHelpStringComponentPallette();
    } else if (key.equals("Button-helpString")) {
      value = MESSAGES.ButtonHelpStringComponentPallette();
    } else if (key.equals("Camcorder-helpString")) {
      value = MESSAGES.CamcorderHelpStringComponentPallette();
    } else if (key.equals("Camera-helpString")) {
      value = MESSAGES.CameraHelpStringComponentPallette();
    } else if (key.equals("Canvas-helpString")) {
      value = MESSAGES.CanvasHelpStringComponentPallette();
    } else if (key.equals("CheckBox-helpString")) {
      value = MESSAGES.CheckBoxHelpStringComponentPallette();
    } else if (key.equals("Clock-helpString")) {
      value = MESSAGES.ClockHelpStringComponentPallette();
    } else if (key.equals("ContactPicker-helpString")) {
      value = MESSAGES.ContactPickerHelpStringComponentPallette();
    } else if (key.equals("DatePicker-helpString")) {
      value = MESSAGES.DatePickerHelpStringComponentPallette();
    } else if (key.equals("EmailPicker-helpString")) {
      value = MESSAGES.EmailPickerHelpStringComponentPallette();
    } else if (key.equals("File-helpString")) {
      value = MESSAGES.FileHelpStringComponentPallette();
    } else if (key.equals("Form-helpString")) {
      value = MESSAGES.FormHelpStringComponentPallette();
    } else if (key.equals("FusiontablesControl-helpString")) {
      value = MESSAGES.FusiontablesControlHelpStringComponentPallette();
    } else if (key.equals("GameClient-helpString")) {
      value = MESSAGES.GameClientHelpStringComponentPallette();
    } else if (key.equals("HorizontalArrangement-helpString")) {
      value = MESSAGES.HorizontalArrangementHelpStringComponentPallette();
    } else if (key.equals("Image-helpString")) {
      value = MESSAGES.ImageHelpStringComponentPallette();
    } else if (key.equals("ImagePicker-helpString")) {
      value = MESSAGES.ImagePickerHelpStringComponentPallette();
    } else if (key.equals("ImageSprite-helpString")) {
      value = MESSAGES.ImageSpriteHelpStringComponentPallette();
    } else if (key.equals("Label-helpString")) {
      value = MESSAGES.LabelHelpStringComponentPallette();
    } else if (key.equals("ListPicker-helpString")) {
      value = MESSAGES.ListPickerHelpStringComponentPallette();
    } else if (key.equals("ListView-helpString")) {
      value = MESSAGES.ListViewHelpStringComponentPallette();
    } else if (key.equals("LocationSensor-helpString")) {
      value = MESSAGES.LocationSensorHelpStringComponentPallette();
    } else if (key.equals("NearField-helpString")) {
      value = MESSAGES.NearFieldHelpStringComponentPallette();
    } else if (key.equals("Notifier-helpString")) {
      value = MESSAGES.NotifierHelpStringComponentPallette();
    } else if (key.equals("ProximitySensor-helpString")) {
      value = MESSAGES.ProximitySensorHelpStringComponentPallette();
    } else if (key.equals("NxtColorSensor-helpString")) {
      value = MESSAGES.NxtColorSensorHelpStringComponentPallette();
    } else if (key.equals("NxtDirectCommands-helpString")) {
      value = MESSAGES.NxtDirectCommandsHelpStringComponentPallette();
    } else if (key.equals("NxtDrive-helpString")) {
      value = MESSAGES.NxtDriveHelpStringComponentPallette();
    } else if (key.equals("NxtLightSensor-helpString")) {
      value = MESSAGES.NxtLightSensorHelpStringComponentPallette();
    } else if (key.equals("NxtSoundSensor-helpString")) {
      value = MESSAGES.NxtSoundSensorHelpStringComponentPallette();
    } else if (key.equals("NxtTouchSensor-helpString")) {
      value = MESSAGES.NxtTouchSensorHelpStringComponentPallette();
    } else if (key.equals("NxtUltrasonicSensor-helpString")) {
      value = MESSAGES.NxtUltrasonicSensorHelpStringComponentPallette();
    } else if (key.equals("OrientationSensor-helpString")) {
      value = MESSAGES.OrientationSensorHelpStringComponentPallette();
    } else if (key.equals("PasswordTextBox-helpString")) {
      value = MESSAGES.PasswordTextBoxHelpStringComponentPallette();
    } else if (key.equals("Pedometer-helpString")) {
      value = MESSAGES.PedometerHelpStringComponentPallette();
    } else if (key.equals("PhoneCall-helpString")) {
      value = MESSAGES.PhoneCallHelpStringComponentPallette();
    } else if (key.equals("PhoneNumberPicker-helpString")) {
      value = MESSAGES.PhoneNumberPickerHelpStringComponentPallette();
    } else if (key.equals("PhoneStatus-helpString")) {
      value = MESSAGES.PhoneStatusHelpStringComponentPallette();
    } else if (key.equals("Player-helpString")) {
      value = MESSAGES.PlayerHelpStringComponentPallette();
    } else if (key.equals("Sharing-helpString")) {
      value = MESSAGES.SharingHelpStringComponentPallette();
    } else if (key.equals("Slider-helpString")) {
      value = MESSAGES.SliderHelpStringComponentPallette();
    } else if (key.equals("Sound-helpString")) {
      value = MESSAGES.SoundHelpStringComponentPallette();
    } else if (key.equals("SoundRecorder-helpString")) {
      value = MESSAGES.SoundRecorderHelpStringComponentPallette();
    } else if (key.equals("SpeechRecognizer-helpString")) {
      value = MESSAGES.SpeechRecognizerHelpStringComponentPallette();
    } else if (key.equals("Spinner-helpString")) {
      value = MESSAGES.SpinnerHelpStringComponentPallette();
    } else if (key.equals("TableArrangement-helpString")) {
      value = MESSAGES.TableArrangementHelpStringComponentPallette();
    } else if (key.equals("TextBox-helpString")) {
      value = MESSAGES.TextBoxHelpStringComponentPallette();
    } else if (key.equals("TextToSpeech-helpString")) {
      value = MESSAGES.TextToSpeechHelpStringComponentPallette();
    } else if (key.equals("Texting-helpString")) {
      value = MESSAGES.TextingHelpStringComponentPallette();
    } else if (key.equals("TimePicker-helpString")) {
      value = MESSAGES.TimePickerHelpStringComponentPallette();
    } else if (key.equals("TinyDB-helpString")) {
      value = MESSAGES.TinyDBHelpStringComponentPallette();
    } else if (key.equals("TinyWebDB-helpString")) {
      value = MESSAGES.TinyWebDBHelpStringComponentPallette();
    } else if (key.equals("Twitter-helpString")) {
      value = MESSAGES.TwitterHelpStringComponentPallette();
    } else if (key.equals("VerticalArrangement-helpString")) {
      value = MESSAGES.VerticalArrangementHelpStringComponentPallette();
    } else if (key.equals("VideoPlayer-helpString")) {
      value = MESSAGES.VideoPlayerHelpStringComponentPallette();
    } else if (key.equals("Voting-helpString")) {
      value = MESSAGES.VotingHelpStringComponentPallette();
    } else if (key.equals("Web-helpString")) {
      value = MESSAGES.WebHelpStringComponentPallette();
    } else if (key.equals("WebViewer-helpString")) {
      value = MESSAGES.WebViewerHelpStringComponentPallette();
    } else if (key.equals("YandexTranslate-helpString")) {
      value = MESSAGES.YandexTranslateHelpStringComponentPallette();
    }
    return value;
  }
}
