/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

// Modified for App Inventor by Liz Looney
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.*;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Utility {

  public static final String AUTOCONFIGURE_FILENAME = "AutoConfigured";
  public static final String CONFIG_FILES_DIR = Environment.getExternalStorageDirectory() + "/FIRST/";
  public static final String DEFAULT_ROBOT_CONFIG = "robot_config";
  public static final String FILE_EXT = ".xml";
  public static final String DEFAULT_ROBOT_CONFIG_FILENAME = DEFAULT_ROBOT_CONFIG + FILE_EXT;
  public static final String NO_FILE = "No current file!"; //todo remove
  public static final String UNSAVED = "Unsaved"; //todo remove

  private Activity activity;
  private SharedPreferences preferences;
  private static int count = 1;
  private WriteXMLFileHandler writer;
  private String output;

  public Utility(Activity activity){
    this.activity = activity;
    preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    writer = new WriteXMLFileHandler(activity);

  }

  /**
   * Builds the Folder on the sdcard that holds all of the configuration files
   * if it doesn't exist. If this fails, a complainToast will pop up.
   */
  public void createConfigFolder(){
    File robotDir = new File(CONFIG_FILES_DIR);
    boolean createdDir = true;

    if (!robotDir.exists()){
      createdDir = robotDir.mkdir();
    }

    if (!createdDir){
      RobotLog.e("Can't create the Robot Config Files directory!");
      complainToast("Can't create the Robot Config Files directory!", activity);
    }
  }

  /**
   * Gets the list of files from the Configuration File directory, and populates the global list
   * used by the fileSpinner
   */
  public ArrayList<String> getXMLFiles(){

    File robotDir = new File(CONFIG_FILES_DIR);
    File[] configFiles = robotDir.listFiles();
    if (configFiles == null) {
      complainToast("The directory is empty: " + CONFIG_FILES_DIR, activity);
      RobotLog.e("robotConfigFiles directory is empty");
      return new ArrayList<String>();
    }
    ArrayList<String> fileList = new ArrayList<String>();

    for (File f: configFiles){
      if (f.isFile()){
        String name = f.getName();
        String nameNoExt = name.replaceFirst("[.][^.]+$", "");
        fileList.add(nameNoExt);
      }
    }
    return fileList;
  }

  /**
   * Actually calls the writer to write the current list of devices out to an XML file. The Write XML
   * Handler checks for duplicate names, and will throw an error if any are found. In that case,
   * a helpful complainToast pops up with the duplicate names, and the file is not saved or written.
   *
   * @return - true if there were duplicate names, false if there were not.
   */
  public boolean writeXML(Map<SerialNumber, ControllerConfiguration> deviceControllers){
    ArrayList<ControllerConfiguration> deviceList = new ArrayList<ControllerConfiguration>();
    deviceList.addAll(deviceControllers.values());
    try {
      output = writer.writeXml(deviceList);
    } catch (RuntimeException e){
      if (e.getMessage().contains("Duplicate name")){
        complainToast("Found " + e.getMessage(), activity);
        RobotLog.e("Found " + e.getMessage());
        return true;
      }
    }
    return false;
  }

  public void writeToFile(String filename) throws RobotCoreException, IOException{
    //writer.writeToFile(output, CONFIG_FILES_DIR, AUTOCONFIGURE_FILENAME+FILE_EXT);
    writer.writeToFile(output, CONFIG_FILES_DIR, filename);
  }

  public String getOutput(){
    return output;
  }

  //*********************** error communication ***********************//

  /**
   * Displays a red message with white text  in the center of the screen.
   * @param msg - the message to be displayed context - the context to display it.
   */
  public void complainToast(String msg, Context context){
    Toast complainToast =  Toast.makeText(context, msg, Toast.LENGTH_SHORT);
    complainToast.setGravity(Gravity.CENTER, 0, 0);
    // makes it a nice red color, but squares the corners for some reason :(
    //complainToast.getView().setBackgroundColor(Color.RED);
    TextView message = (TextView) complainToast.getView().findViewById(android.R.id.message);
    message.setTextColor(Color.WHITE);
    message.setTextSize(18);
    complainToast.show();
  }


  //****************************** create empty devices ***************************************************** //

  /**
   * Builds the list of MotorControllers, ServoControllers, and LegacyModuleControllers
   * filled with empty motors, servos, and modules for display. The "entries" HashMap
   * is populated by the scan button that scan for connected USB devices.
   */
  public void createLists(Set<Map.Entry<SerialNumber, DeviceManager.DeviceType>> entries, Map<SerialNumber, ControllerConfiguration> deviceControllers){
    for(Map.Entry<SerialNumber, DeviceManager.DeviceType> entry : entries){
      DeviceManager.DeviceType enumVal = entry.getValue();
      switch(enumVal){
        case MODERN_ROBOTICS_USB_DC_MOTOR_CONTROLLER:
          ArrayList<DeviceConfiguration> motors = createMotorList();
          deviceControllers.put(entry.getKey(), new MotorControllerConfiguration("Motor Controller " + count, motors, entry.getKey()));
          count++;
          break;
        case MODERN_ROBOTICS_USB_SERVO_CONTROLLER:
          ArrayList<DeviceConfiguration> servos = createServoList();
          deviceControllers.put(entry.getKey(), new ServoControllerConfiguration("Servo Controller " + count, servos, entry.getKey()));
          count++;
          break;
        case MODERN_ROBOTICS_USB_LEGACY_MODULE:
          //Uncomment this line to get some devices in your list, for testing.
          //ArrayList<DeviceConfiguration> legacies = createLegacyModuleTEST();
          ArrayList<DeviceConfiguration> legacies = createLegacyModuleList();
          deviceControllers.put(entry.getKey(),new LegacyModuleControllerConfiguration("Legacy Module " + count, legacies, entry.getKey()));
          count++;
        default:
          break;
      }
    }
  }

  /**
   * Creates a list of MotorConfiguration objects.
   * @return motors a list of MotorConfiguration objects.
   */
  public ArrayList<DeviceConfiguration> createMotorList(){
    ArrayList<DeviceConfiguration> motors = new ArrayList<DeviceConfiguration>();
    MotorConfiguration motor1 = new MotorConfiguration(1);
    motors.add(motor1);
    MotorConfiguration motor2 = new MotorConfiguration(2);
    motors.add(motor2);
    return motors;
  }

  /**
   * Creates a list of ServoConfiguration objects.
   * @return servos a list of ServoConfiguration objects.
   */
  public ArrayList<DeviceConfiguration> createServoList(){
    ArrayList<DeviceConfiguration> servos = new ArrayList<DeviceConfiguration>();

    for (int i = 1; i <= 6; i++){
      ServoConfiguration servo = new ServoConfiguration(i);
      servos.add(servo);
    }
    return servos;
  }

  /**
   * Creates a list of empty DeviceConfiguration objects.
   * @return modules a list of DeviceConfiguration objects.
   */
  public ArrayList<DeviceConfiguration> createLegacyModuleList(){
    ArrayList<DeviceConfiguration> modules = new ArrayList<DeviceConfiguration>();

    for (int i = 0; i < 6; i++){
      DeviceConfiguration module = new DeviceConfiguration(i);
      modules.add(module);
    }
    return modules;
  }

  public void updateHeader(String default_name,
      /*AI int pref_hardware_config_filename_id */ String pref_hardware_config_filename_key,
      /*AI int fileTextView */ TextView activeFile,
      /*AI int header_id */ LinearLayout header){
    String fullFilename = preferences.getString(
        /*AI activity.getString( */pref_hardware_config_filename_key /*AI ) */, default_name);
    String activeFilename = fullFilename.replaceFirst("[.][^.]+$", "");
    //AI TextView activeFile = (TextView) activity.findViewById(fileTextView);
    activeFile.setText(activeFilename);

    if (activeFilename.equalsIgnoreCase(NO_FILE)) {
      // from the Qualcomm logo
      int color = Color.parseColor("#5DBCD2");
      changeBackground(color, /*AI header_id */ header);
    } else if (activeFilename.toLowerCase().contains(UNSAVED.toLowerCase())){
      changeBackground(Color.YELLOW, /*AI header_id */ header);
    } else {
      // lightest part of the Qualcomm logo
      int color = Color.parseColor("#309EA4");
      changeBackground(color, /*AI header_id */ header);
    }
  }

  //takes in a file name and updates the filename stored in the preferences.
  public void saveToPreferences(String filename,
      /*AI int pref_hardware_config_filename_id */ String pref_hardware_config_filename_key){
    filename = filename.replaceFirst("[.][^.]+$", "");
    SharedPreferences.Editor edit = preferences.edit();
    edit.putString(/*AI activity.getString( */ pref_hardware_config_filename_key /*AI ) */, filename);
    edit.apply();
  }

  public void changeBackground(int color, /*AI int header_id */ LinearLayout header){
    //AI LinearLayout header = (LinearLayout) activity.findViewById(header_id);
    header.setBackgroundColor(color);
  }

  public String getFilenameFromPrefs(
      /*AI int pref_hardware_config_filename_id */ String pref_hardware_config_filename_key,
      String default_name){
    return preferences.getString(/*AI activity.getString( */ pref_hardware_config_filename_key /*AI ) */, default_name);
  }

  public void resetCount(){
    count = 1;
  }

  /*AI
  public void setOrangeText(String msg0, String msg1, int info_id, int layout_id, int orange0, int orange1){
    LinearLayout layout = (LinearLayout) activity.findViewById(info_id);
    layout.setVisibility(View.VISIBLE);
    layout.removeAllViews();
    activity.getLayoutInflater().inflate(layout_id, layout, true);
    TextView text0 = (TextView) layout.findViewById(orange0);
    TextView text1 = (TextView) layout.findViewById(orange1);
    text1.setGravity(Gravity.LEFT);

    text0.setText(msg0);
    text1.setText(msg1);
  }
  */

  //********************** Alert Dialog helpers ***********************//

  public void confirmSave(){
    Toast confirmation = Toast.makeText(activity, "Saved", Toast.LENGTH_SHORT);
    confirmation.setGravity(Gravity.BOTTOM, 0, 50);
    confirmation.show();
  }

  public AlertDialog.Builder buildBuilder(String title, String message){
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(title)
        .setMessage(message);
        //.setView(input);
    return builder;
  }

  public String prepareFilename(String currentFile){
    //String currentFile = preferredFilename;
    if (currentFile.toLowerCase().contains(Utility.UNSAVED.toLowerCase())){
      currentFile = currentFile.substring(7).trim();
    }
    if (currentFile.equalsIgnoreCase(Utility.NO_FILE)){
      currentFile = "";
    }
    return currentFile;
  }

  /************************* TEST CODE ***************************/

  @SuppressWarnings("unused")
  private ArrayList<DeviceConfiguration> createLegacyModuleTEST(){
    ArrayList<DeviceConfiguration> modules = new ArrayList<DeviceConfiguration>();

    // port 0
    DeviceConfiguration light_sensor = new DeviceConfiguration(DeviceConfiguration.ConfigurationType.LIGHT_SENSOR);
    light_sensor.setName("yagami");
    light_sensor.setPort(0);
    modules.add(light_sensor);
    // port 1
    DeviceConfiguration motorController = new MotorControllerConfiguration("motor controller poo", createMotorList(), new SerialNumber("99"));
    motorController.setPort(1);
    modules.add(motorController);
    // port 2
    DeviceConfiguration servoController = new ServoControllerConfiguration("servo controller morgh", createServoList(), new SerialNumber("98"));
    servoController.setPort(2);
    modules.add(servoController);
    // port 3
    DeviceConfiguration compass = new DeviceConfiguration(DeviceConfiguration.ConfigurationType.COMPASS);
    compass.setName("compass name!");
    compass.setPort(3);
    modules.add(compass);
    // port 4
    DeviceConfiguration motorController2 = new MotorControllerConfiguration("motor controller flibbitty flobbitty", createMotorList(), new SerialNumber("97"));
    motorController2.setPort(4);
    modules.add(motorController2);

    // port 5
    DeviceConfiguration disabled_device = new DeviceConfiguration(5);
    //disabled_device.setPort(-1);
    modules.add(disabled_device);

    return modules;
  }
}
