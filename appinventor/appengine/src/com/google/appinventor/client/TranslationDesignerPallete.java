package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;
import static com.google.appinventor.client.Ode.MESSAGES;


public class TranslationDesignerPallete {

  public static String getCorrespondingString(String key) {
    String value = key;

    // XXX
    OdeLog.wlog("getCorrespondingString: key = " + key);
//    return key;

     // Palette components name
     if (key.equals("User Interface")) {
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
     } else if (key.equals("Image")) {
       value = MESSAGES.imageComponentPallette();
     } else if (key.equals("Label")) {
       value = MESSAGES.labelComponentPallette();
     } else if (key.equals("ListPicker")) {
       value = MESSAGES.listPickerComponentPallette();
     } else if (key.equals("PasswordTextBox")) {
       value = MESSAGES.passwordTextBoxComponentPallette();
     } else if (key.equals("TextBox")) {
       value = MESSAGES.textBoxComponentPallette();
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
     } else if (key.equals("Texting")) {
       value = MESSAGES.textingComponentPallette();
     } else if (key.equals("Twitter")) {
       value = MESSAGES.twitterComponentPallette();
       // Sensor
     } else if (key.equals("AccelerometerSensor")) {
       value = MESSAGES.accelerometerSensorComponentPallette();
     } else if (key.equals("LocationSensor")) {
       value = MESSAGES.locationSensorComponentPallette();
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
     } else if (key.equals("SpeechRecognizer")) {
       value = MESSAGES.speechRecognizerComponentPallette();
     } else if (key.equals("TextToSpeech")) {
       value = MESSAGES.textToSpeechComponentPallette();
     } else if (key.equals("TinyWebDB")) {
       value = MESSAGES.tinyWebDBComponentPallette();
     } else if (key.equals("Web")) {
       value = MESSAGES.webComponentPallette();
       // Not ready for prime time
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
     }
     return value;
  }
}
