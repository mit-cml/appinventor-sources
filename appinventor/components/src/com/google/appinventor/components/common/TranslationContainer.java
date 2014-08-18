package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public class TranslationContainer {
  private Map<String, String> CompTransMap = new HashMap<String, String>();
  public TranslationContainer() {
        
  //Palette components name
    CompTransMap.put("Basic", "基本");
    CompTransMap.put("Media", "媒体");
    CompTransMap.put("Animation", "动画"); 
    CompTransMap.put("Social", "社交的");
    CompTransMap.put("Sensors", "传感器");
    CompTransMap.put("Screen Arrangement", "屏幕布局");
    CompTransMap.put("LEGO\u00AE MINDSTORMS\u00AE", "乐高机器人套件\u00AE");
    CompTransMap.put("Other stuff", "其他东西");
    CompTransMap.put("Not ready for prime time", "测试中的套件");
    CompTransMap.put("Old stuff", "旧东西");
        
    //Basic
    CompTransMap.put("Button","按钮");
    CompTransMap.put("Canvas","画布");
    CompTransMap.put("CheckBox","复选框");
    CompTransMap.put("Clock","时钟");
    CompTransMap.put("Image","图像");
    CompTransMap.put("Label","便签");
    CompTransMap.put("ListPicker","列表选择器");
    CompTransMap.put("PasswordTextBox","密码框");
    CompTransMap.put("TextBox","文本框");
    CompTransMap.put("TinyDB","细小数据库");
        
    //Media
    CompTransMap.put("Camcorder","摄像机");
    CompTransMap.put("Camera","相机");
    CompTransMap.put("ImagePicker","画像选择器");
    CompTransMap.put("Player","播放器");
    CompTransMap.put("Sound","声音");
    CompTransMap.put("VideoPlayer","媒体播放器");
        
        //Animation
    CompTransMap.put("Ball","球");
    CompTransMap.put("ImageSprite","图片精灵");
        
    //Social
    CompTransMap.put("ContactPicker","联系信息选择器");
    CompTransMap.put("EmailPicker","邮件选择器");
    CompTransMap.put("PhoneCall","电话");
    CompTransMap.put("PhoneNumberPicker","电话号码选择器");
    CompTransMap.put("Texting","信息");
    CompTransMap.put("Twitter","Twitter");
        
    //Sensor        
    CompTransMap.put("AccelerometerSensor","加速度传感器");
    CompTransMap.put("LocationSensor","位置传感器");
    CompTransMap.put("OrientationSensor","方向传感器");
        
    //Screen Arrangement
    CompTransMap.put("HorizontalArrangement", "水平排列");
    CompTransMap.put("TableArrangement", "表安排");
    CompTransMap.put("VerticalArrangement", "竖向布置");
        
    //Lego Mindstorms
    CompTransMap.put("NxtColorSensor", "Nxt颜色传感器");
    CompTransMap.put("NxtDirectCommands", "Nxt直接命令");
    CompTransMap.put("NxtDrive", "Nxt驱动");
    CompTransMap.put("NxtLightSensor", "Nxt光传感器");
    CompTransMap.put("NxtSoundSensor", "Nxt声音传感器");
    CompTransMap.put("NxtTouchSensor", "Nxt触摸传感器");
    CompTransMap.put("NxtUltrasonicSensor", "Nxt超声波传感器");
        
    //Other stuff
    CompTransMap.put("ActivityStarter", "活动启动");
    CompTransMap.put("BarcodeScanner", "条码扫描器");
    CompTransMap.put("BluetoothClient", "蓝牙客户");
    CompTransMap.put("BluetoothServer", "蓝牙服务器");
    CompTransMap.put("Notifier", "通告人");
    CompTransMap.put("SpeechRecognizer", "语音识别");
    CompTransMap.put("TextToSpeech", "文本到语音");
    CompTransMap.put("TinyWebDB", "细小网络数据库");
    CompTransMap.put("Web", "网络");
        
    //Not ready for prime time
    CompTransMap.put("FusiontablesControl","Fusiontables控制");
    CompTransMap.put("GameClient","游戏客户端");
    CompTransMap.put("SoundRecorder","声音记录器");
    CompTransMap.put("Voting","投票");
    CompTransMap.put("WebViewer","网页浏览器");       
    };
    
    public String getCorrespondingString(String key) {
      if (CompTransMap.containsKey(key)) {
        return CompTransMap.get(key);
      } else {
        return "Missing name";
      }
    }    
}
