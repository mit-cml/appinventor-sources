// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import java.util.HashMap;
import java.util.Map;

public class TranslationContainer {

  private Map<String, String> myMap = new HashMap<String, String>();

  public TranslationContainer() {
    //Palette components name
    myMap.put("Basic", "基本");
    myMap.put("Media", "媒体");
    myMap.put("Animation", "动画");
    myMap.put("Social", "社交的");
    myMap.put("Sensors", "传感器");
    myMap.put("Screen Arrangement", "屏幕布局");
    myMap.put("LEGO\u00AE MINDSTORMS\u00AE", "乐高机器人套件\u00AE");
    myMap.put("Other stuff", "其他东西");
    myMap.put("Not ready for prime time", "测试中的套件");
    myMap.put("Old stuff", "旧东西");

    //Basic
    myMap.put("Button","按钮");
    myMap.put("Canvas","画布");
    myMap.put("CheckBox","复选框");
    myMap.put("Clock","时钟");
    myMap.put("Image","图像");
    myMap.put("Label","便签");
    myMap.put("ListPicker","列表选择器");
    myMap.put("PasswordTextBox","密码框");
    myMap.put("TextBox","文本框");
    myMap.put("TinyDB","细小数据库");

    //Media
    myMap.put("Camcorder","摄像机");
    myMap.put("Camera","相机");
    myMap.put("ImagePicker","画像选择器");
    myMap.put("Player","播放器");
    myMap.put("Sound","声音");
    myMap.put("VideoPlayer","媒体播放器");

    //Animation
    myMap.put("Ball","球");
    myMap.put("ImageSprite","图片精灵");

    //Social
    myMap.put("ContactPicker","联系信息选择器");
    myMap.put("EmailPicker","邮件选择器");
    myMap.put("PhoneCall","电话");
    myMap.put("PhoneNumberPicker","电话号码选择器");
    myMap.put("Texting","信息");
    myMap.put("Twitter","Twitter");

    //Sensor
    myMap.put("AccelerometerSensor","加速度传感器");
    myMap.put("LocationSensor","位置传感器");
    myMap.put("OrientationSensor","方向传感器");

    //Screen Arrangement
    myMap.put("HorizontalArrangement", "水平排列");
    myMap.put("TableArrangement", "表安排");
    myMap.put("VerticalArrangement", "竖向布置");

    //Lego Mindstorms
    myMap.put("NxtColorSensor", "Nxt颜色传感器");
    myMap.put("NxtDirectCommands", "Nxt直接命令");
    myMap.put("NxtDrive", "Nxt驱动");
    myMap.put("NxtLightSensor", "Nxt光传感器");
    myMap.put("NxtSoundSensor", "Nxt声音传感器");
    myMap.put("NxtTouchSensor", "Nxt触摸传感器");
    myMap.put("NxtUltrasonicSensor", "Nxt超声波传感器");

    //Other stuff
    myMap.put("ActivityStarter", "活动启动");
    myMap.put("BarcodeScanner", "条码扫描器");
    myMap.put("BluetoothClient", "蓝牙客户");
    myMap.put("BluetoothServer", "蓝牙服务器");
    myMap.put("Notifier", "通告人");
    myMap.put("SpeechRecognizer", "语音识别");
    myMap.put("TextToSpeech", "文本到语音");
    myMap.put("TinyWebDB", "细小网络数据库");
    myMap.put("Web", "网络");

    //Not ready for prime time
    myMap.put("FusiontablesControl","Fusiontables控制");
    myMap.put("GameClient","游戏客户端");
    myMap.put("SoundRecorder","声音记录器");
    myMap.put("Voting","投票");
    myMap.put("WebViewer","网页浏览器");
  };

  public String getCorrespondingString(String key) {
    if (myMap.containsKey(key)) {
      return myMap.get(key);
    } else {
      return "Missing name";
    }
  }
}
