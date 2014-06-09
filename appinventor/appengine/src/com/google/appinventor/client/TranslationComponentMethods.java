package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationComponentMethods {

  public static class MethodPair {

    private String key;
    private String value;
    private String description;

    public MethodPair(String key, String value) {
      this(key, value, "");
    }

    public MethodPair(String key, String value, String description) {
      this.key = key;
      this.value = value;
      this.description = description;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return "{ \"key\": \"" + key + "\", \"value\": \"" + value + "\", \"description\": \"" + description + "\"}";
    }
  }

  public static String languageSetting = "zh_TW";
  public static Map<String, MethodPair> myMap = createMap(languageSetting);

  public static enum Language {
    zh_TW, en_US
  }

  public static void updateMap(String language) {
    if (!languageSetting.equals(language)) {
      languageSetting = language;
      switch (Language.valueOf(language)) {
        case zh_TW:
          myMap = chineseMap();
          break;
        case en_US:
          myMap = englishMap();
          break;
        default:
          myMap = englishMap();
          break;
      }
    }
  }

  public static Map<String, MethodPair> createMap(String language) {
    Map<String, MethodPair> map;
    switch (Language.valueOf(language)) {
      case zh_TW:
        map = chineseMap();
        break;
      case en_US:
        map = englishMap();
        break;
      default:
        map = englishMap();
        break;
    }
    return map;
  }

  public static String getName(String key) {
    if (!myMap.containsKey(key)) {
      OdeLog.log("Method map does not contain key " + key);
      return key;
    }
    return myMap.get(key).getKey();
  }
  
  public static String getMapJSON(String eventName) {
    if (myMap.containsKey(eventName)) {
      return myMap.get(eventName).toString();
    }
    return new MethodPair(eventName, eventName).toString();
  }
  
  /**
   * Get JSON representation of the translation map
   * 
   * @param events
   *          A list of event names
   */
  public static String getMapJSON(List<String> events) {
    OdeLog.log("Current language setting: " + languageSetting);
    StringBuilder sb = new StringBuilder();

    sb.append('[');
    String separator = "";
    for (String eventName : events) {
      sb.append(separator);
      sb.append(getMapJSON(eventName));
      separator = ",\n";
    }

    sb.append(']');
    System.err.println(sb.toString());
    OdeLog.log("Event JSON: " + sb.toString());
    return sb.toString();
  }

  /**
   * Get an English translation map.
   * 
   * The output map has the following format: 
   *    Map = [{eventName1: MethodPair1}, ...]
   * 
   * @return map
   */
  public static HashMap<String, MethodPair> englishMap() { 
    // TODO: Some methods may have different descriptions depending on which components
    // they are associated to; need to change the event name to be component type specific
    HashMap<String, MethodPair> map = new HashMap<String, MethodPair>();
    
    // Methods
    map.put("ResolveActivity", new MethodPair("ResolveActivity", "ResolveActivity"));
    map.put("StartActivity", new MethodPair("StartActivity", "StartActivity"));
    map.put("Bounce", new MethodPair("Bounce", "Bounce"));
    map.put("CollidingWith", new MethodPair("CollidingWith", "CollidingWith"));
    map.put("MoveIntoBounds", new MethodPair("MoveIntoBounds", "MoveIntoBounds"));
    map.put("MoveTo", new MethodPair("MoveTo", "MoveTo"));
    map.put("PointInDirection", new MethodPair("PointInDirection", "PointInDirection"));
    map.put("PointTowards", new MethodPair("PointTowards", "PointTowards"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "BytesAvailableToReceive"));
    map.put("Connect", new MethodPair("Connect", "Connect"));
    map.put("ConnectWithUUID", new MethodPair("ConnectWithUUID", "ConnectWithUUID"));

    map.put("Disconnect", new MethodPair("Disconnect", "Disconnect"));
    map.put("IsDevicePaired", new MethodPair("IsDevicePaired", "IsDevicePaired"));
    map.put("ReceiveSigned1ByteNumber", new MethodPair("ReceiveSigned1ByteNumber", "ReceiveSigned1ByteNumber"));
    map.put("ReceiveSigned2ByteNumber", new MethodPair("ReceiveSigned2ByteNumber", "ReceiveSigned2ByteNumber"));
    map.put("ReceiveSigned4ByteNumber", new MethodPair("ReceiveSigned4ByteNumber", "ReceiveSigned4ByteNumber"));
    map.put("ReceiveSignedBytes", new MethodPair("ReceiveSignedBytes", "ReceiveSignedBytes"));
    map.put("ReceiveText", new MethodPair("ReceiveText", "ReceiveText"));
    map.put("ReceiveUnsigned1ByteNumber", new MethodPair("ReceiveUnsigned1ByteNumber", "ReceiveUnsigned1ByteNumber"));
    map.put("ReceiveUnsigned2ByteNumber", new MethodPair("ReceiveUnsigned2ByteNumber", "ReceiveUnsigned2ByteNumber"));
    map.put("ReceiveUnsigned4ByteNumber", new MethodPair("ReceiveUnsigned4ByteNumber", "ReceiveUnsigned4ByteNumber"));
    map.put("ReceiveUnsignedBytes", new MethodPair("ReceiveUnsignedBytes", "ReceiveUnsignedBytes"));
    map.put("Send1ByteNumber", new MethodPair("Send1ByteNumber", "Send1ByteNumber"));
    map.put("Send2ByteNumber", new MethodPair("Send2ByteNumber", "Send2ByteNumber"));
    map.put("Send4ByteNumber", new MethodPair("Send4ByteNumber", "Send4ByteNumber"));
    map.put("SendBytes", new MethodPair("SendBytes", "SendBytes"));
    map.put("SendText", new MethodPair("SendText", "SendText"));
    map.put("AcceptConnection", new MethodPair("AcceptConnection", "AcceptConnection"));
    map.put("AcceptConnectionWithUUID", new MethodPair("AcceptConnectionWithUUID", "AcceptConnectionWithUUID"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "BytesAvailableToReceive"));
    map.put("StopAccepting", new MethodPair("StopAccepting", "StopAccepting"));
   
    map.put("RecordVideo", new MethodPair("RecordVideo", "RecordVideo"));
    map.put("TakePicture", new MethodPair("TakePicture", "TakePicture"));
    map.put("Clear", new MethodPair("Clear", "Clear"));
    map.put("DrawCircle", new MethodPair("DrawCircle", "DrawCircle"));
    map.put("DrawLine", new MethodPair("DrawLine", "DrawLine"));
    map.put("DrawPoint", new MethodPair("DrawPoint", "DrawPoint"));
    map.put("DrawText", new MethodPair("DrawText", "DrawText"));
    map.put("DrawTextAtAngle", new MethodPair("DrawTextAtAngle", "DrawTextAtAngle"));
    map.put("GetBackgroundPixelColor", new MethodPair("GetBackgroundPixelColor", "GetBackgroundPixelColor"));
    map.put("GetPixelColor", new MethodPair("GetPixelColor", "GetPixelColor"));
    map.put("Save", new MethodPair("Save", "Save"));
    map.put("SaveAs", new MethodPair("SaveAs", "SaveAs"));
    
    map.put("SetBackgroundPixelColor", new MethodPair("SetBackgroundPixelColor", "SetBackgroundPixelColor"));
    map.put("AddDays", new MethodPair("AddDays", "AddDays"));
    map.put("AddHours", new MethodPair("AddHours", "AddHours"));
    map.put("AddMinutes", new MethodPair("AddMinutes", "AddMinutes"));
    map.put("AddMonths", new MethodPair("AddMonths", "AddMonths"));
    map.put("AddSeconds", new MethodPair("AddSeconds", "AddSeconds"));
    map.put("AddWeeks", new MethodPair("AddWeeks", "AddWeeks"));
    map.put("AddYears", new MethodPair("AddYears", "AddYears"));
    map.put("DayOfMonth", new MethodPair("DayOfMonth", "DayOfMonth"));
    map.put("Duration", new MethodPair("Duration", "Duration"));
    map.put("FormatDate", new MethodPair("FormatDate", "FormatDate"));
    map.put("FormatDateTime", new MethodPair("FormatDateTime", "FormatDateTime"));
    
    map.put("FormatTime", new MethodPair("FormatTime", "FormatTime"));
    map.put("GetMillis", new MethodPair("GetMillis", "GetMillis"));
    map.put("Hour", new MethodPair("Hour", "Hour"));
    map.put("MakeInstant", new MethodPair("MakeInstant", "MakeInstant"));
    map.put("MakeInstantFromMillis", new MethodPair("MakeInstantFromMillis", "MakeInstantFromMillis"));
    map.put("Minute", new MethodPair("Minute", "Minute"));
    map.put("Month", new MethodPair("Month", "Month"));
    map.put("MonthName", new MethodPair("MonthName", "MonthName"));
    map.put("Now", new MethodPair("Now", "Now"));
    map.put("Second", new MethodPair("Second", "Second"));
    map.put("SystemTime", new MethodPair("SystemTime", "SystemTime"));
    map.put("Weekday", new MethodPair("Weekday", "Weekday"));
    map.put("WeekdayName", new MethodPair("WeekdayName", "WeekdayName"));
    map.put("Year", new MethodPair("Year", "Year"));
    map.put("Open", new MethodPair("Open", "Open"));
    map.put("CloseScreenAnimation", new MethodPair("CloseScreenAnimation", "CloseScreenAnimation"));
    map.put("OpenScreenAnimation", new MethodPair("OpenScreenAnimation", "OpenScreenAnimation"));
    
    map.put("DoQuery", new MethodPair("DoQuery", "DoQuery"));
    map.put("ForgetLogin", new MethodPair("ForgetLogin", "ForgetLogin"));
    map.put("SendQuery", new MethodPair("SendQuery", "SendQuery"));
    map.put("GetInstanceLists", new MethodPair("GetInstanceLists", "GetInstanceLists"));
    map.put("GetMessages", new MethodPair("GetMessages", "GetMessages"));
    map.put("Invite", new MethodPair("Invite", "Invite"));
    map.put("LeaveInstance", new MethodPair("LeaveInstance", "LeaveInstance"));
    map.put("MakeNewInstance", new MethodPair("MakeNewInstance", "MakeNewInstance"));
    map.put("SendMessage", new MethodPair("SendMessage", "SendMessage"));
    map.put("ServerCommand", new MethodPair("ServerCommand", "ServerCommand"));
    map.put("SetInstance", new MethodPair("SetInstance", "SetInstance"));
    map.put("SetLeader", new MethodPair("SetLeader", "SetLeader"));
    map.put("Bounce", new MethodPair("Bounce", "Bounce"));
    map.put("CollidingWith", new MethodPair("CollidingWith", "CollidingWith"));
    map.put("MoveIntoBounds", new MethodPair("MoveIntoBounds", "MoveIntoBounds"));
    map.put("MoveTo", new MethodPair("MoveTo", "MoveTo"));
    map.put("PointInDirection", new MethodPair("PointInDirection", "PointInDirection"));
   
    map.put("PointTowards", new MethodPair("PointTowards", "PointTowards"));
    map.put("LatitudeFromAddress", new MethodPair("LatitudeFromAddress", "LatitudeFromAddress"));
    map.put("LongitudeFromAddress", new MethodPair("LongitudeFromAddress", "LongitudeFromAddress"));
    map.put("LogError", new MethodPair("LogError", "LogError"));
    map.put("LogInfo", new MethodPair("LogInfo", "LogInfo"));
    map.put("LogWarning", new MethodPair("LogWarning", "LogWarning"));
    map.put("ShowAlert", new MethodPair("ShowAlert", "ShowAlert"));
    map.put("ShowChooseDialog", new MethodPair("ShowChooseDialog", "ShowChooseDialog"));
    map.put("ShowMessageDialog", new MethodPair("ShowMessageDialog", "ShowMessageDialog"));
    map.put("ShowTextDialog", new MethodPair("ShowTextDialog", "ShowTextDialog"));
    map.put("GetColor", new MethodPair("GetColor", "GetColor"));
    map.put("GetLightLevel", new MethodPair("GetLightLevel", "GetLightLevel"));
    map.put("DeleteFile", new MethodPair("DeleteFile", "DeleteFile"));
    map.put("DownloadFile", new MethodPair("DownloadFile", "DownloadFile"));
    map.put("GetBatteryLevel", new MethodPair("GetBatteryLevel", "GetBatteryLevel"));
    map.put("GetBrickName", new MethodPair("GetBrickName", "GetBrickName"));
    map.put("GetCurrentProgramName", new MethodPair("GetCurrentProgramName", "GetCurrentProgramName"));
    map.put("GetFirmwareVersion", new MethodPair("GetFirmwareVersion", "GetFirmwareVersion"));
    map.put("GetInputValues", new MethodPair("GetInputValues", "GetInputValues"));
    map.put("GetOutputState", new MethodPair("GetOutputState", "GetOutputState"));
    map.put("KeepAlive", new MethodPair("KeepAlive", "KeepAlive"));
   
    map.put("ListFiles", new MethodPair("ListFiles", "ListFiles"));
    map.put("LsGetStatus", new MethodPair("LsGetStatus", "LsGetStatus"));
    map.put("LsRead", new MethodPair("LsRead", "LsRead"));
    map.put("LsWrite", new MethodPair("LsWrite", "LsWrite"));
    map.put("MessageRead", new MethodPair("MessageRead", "MessageRead"));
    map.put("MessageWrite", new MethodPair("MessageWrite", "MessageWrite"));
    map.put("PlaySoundFile", new MethodPair("PlaySoundFile", "PlaySoundFile"));
    map.put("PlayTone", new MethodPair("PlayTone", "PlayTone"));
    map.put("ResetInputScaledValue", new MethodPair("ResetInputScaledValue", "ResetInputScaledValue"));
    map.put("ResetMotorPosition", new MethodPair("ResetMotorPosition", "ResetMotorPosition"));
    map.put("SetBrickName", new MethodPair("SetBrickName", "SetBrickName"));
    map.put("SetInputMode", new MethodPair("SetInputMode", "SetInputMode"));
    map.put("SetOutputState", new MethodPair("SetOutputState", "SetOutputState"));
    
    map.put("StartProgram", new MethodPair("StartProgram", "StartProgram"));
    map.put("StopProgram", new MethodPair("StopProgram", "StopProgram"));
    map.put("StopSoundPlayback", new MethodPair("StopSoundPlayback", "StopSoundPlayback"));
    map.put("LsWrite", new MethodPair("LsWrite", "LsWrite"));
    map.put("MoveBackward", new MethodPair("MoveBackward", "MoveBackward"));
    map.put("MoveBackwardIndefinitely", new MethodPair("MoveBackwardIndefinitely", "MoveBackwardIndefinitely"));
    map.put("MoveForward", new MethodPair("MoveForward", "MoveForward"));
    map.put("MoveForwardIndefinitely", new MethodPair("MoveForwardIndefinitely", "MoveForwardIndefinitely"));
    map.put("Stop", new MethodPair("Stop", "Stop"));
    map.put("TurnClockwiseIndefinitely", new MethodPair("TurnClockwiseIndefinitely", "TurnClockwiseIndefinitely"));
    map.put("TurnCounterClockwiseIndefinitely", new MethodPair("TurnCounterClockwiseIndefinitely", "TurnCounterClockwiseIndefinitely"));
    map.put("GetSoundLevel", new MethodPair("GetSoundLevel", "GetSoundLevel"));
    map.put("IsPressed", new MethodPair("IsPressed", "IsPressed"));
    
    map.put("GetDistance", new MethodPair("GetDistance", "GetDistance"));
    map.put("Pause", new MethodPair("Pause", "Pause"));
    map.put("Reset", new MethodPair("Reset", "Reset"));
    map.put("Resume", new MethodPair("Resume", "Resume"));
    map.put("Start", new MethodPair("Start", "Start"));
    map.put("MakePhoneCall", new MethodPair("MakePhoneCall", "MakePhoneCall"));
    map.put("GetWifiIpAddress", new MethodPair("GetWifiIpAddress", "GetWifiIpAddress"));
    map.put("isConnected", new MethodPair("isConnected", "isConnected"));
    map.put("setHmacSeedReturnCode", new MethodPair("setHmacSeedReturnCode", "setHmacSeedReturnCode"));
    map.put("startHTTPD", new MethodPair("startHTTPD", "startHTTPD")); 
    map.put("Vibrate", new MethodPair("Vibrate", "Vibrate"));
    map.put("GetText", new MethodPair("GetText", "GetText"));
    map.put("HideKeyboard", new MethodPair("HideKeyboard", "HideKeyboard"));
    
    map.put("Speak", new MethodPair("Speak", "Speak"));
    map.put("SendMessage", new MethodPair("SendMessage", "SendMessage"));
    map.put("GetValue", new MethodPair("GetValue", "GetValue"));
    map.put("StoreValue", new MethodPair("StoreValue", "StoreValue"));
    map.put("Authorize", new MethodPair("Authorize", "Authorize"));
    map.put("CheckAuthorized", new MethodPair("CheckAuthorized", "CheckAuthorized"));
    map.put("DeAuthorize", new MethodPair("DeAuthorize", "DeAuthorize"));
    map.put("DirectMessage", new MethodPair("DirectMessage", "DirectMessage"));
    map.put("Follow", new MethodPair("Follow", "Follow"));
    map.put("RequestDirectMessages", new MethodPair("RequestDirectMessages", "RequestDirectMessages")); 
    map.put("RequestFollowers", new MethodPair("RequestFollowers", "RequestFollowers"));
    map.put("RequestFriendTimeline", new MethodPair("RequestFriendTimeline", "RequestFriendTimeline"));
    map.put("RequestMentions", new MethodPair("RequestMentions", "RequestMentions"));
    map.put("SearchTwitter", new MethodPair("SearchTwitter", "SearchTwitter"));
    map.put("SetStatus", new MethodPair("SetStatus", "SetStatus"));
    map.put("StopFollowing", new MethodPair("StopFollowing", "StopFollowing")); 
    map.put("GetDuration", new MethodPair("GetDuration", "GetDuration"));
    map.put("SeekTo", new MethodPair("SeekTo", "SeekTo"));
    map.put("DoScan", new MethodPair("DoScan", "DoScan"));
     
    map.put("RequestBallot", new MethodPair("RequestBallot", "RequestBallot"));
    map.put("SendBallot", new MethodPair("SendBallot", "SendBallot"));
    map.put("BuildPostData", new MethodPair("BuildPostData", "BuildPostData"));
    map.put("ClearCookies", new MethodPair("ClearCookies", "ClearCookies")); 
    map.put("Get", new MethodPair("Get", "Get"));
    map.put("HtmlTextDecode", new MethodPair("HtmlTextDecode", "HtmlTextDecode"));
    map.put("JsonTextDecode", new MethodPair("JsonTextDecode", "JsonTextDecode"));
    map.put("PostFile", new MethodPair("PostFile", "PostFile"));
    map.put("PostText", new MethodPair("PostText", "PostText"));
    map.put("PostTextWithEncoding", new MethodPair("PostTextWithEncoding", "PostTextWithEncoding")); 
    map.put("UriEncode", new MethodPair("UriEncode", "UriEncode"));
    map.put("CanGoBack", new MethodPair("CanGoBack", "CanGoBack"));
    map.put("CanGoForward", new MethodPair("CanGoForward", "CanGoForward"));
    map.put("ClearLocations", new MethodPair("ClearLocations", "ClearLocations")); 
    map.put("GoBack", new MethodPair("GoBack", "GoBack"));
    map.put("GoForward", new MethodPair("GoForward", "GoForward"));
    map.put("GoHome", new MethodPair("GoHome", "GoHome"));
    map.put("GoToUrl", new MethodPair("GoToUrl", "GoToUrl"));
    map.put("AppendToFile", new MethodPair("AppendToFile", "AppendToFile"));
    map.put("Delete", new MethodPair("Delete", "Delete"));
    map.put("ReadFrom", new MethodPair("ReadFrom", "ReadFrom"));
    map.put("SaveFile", new MethodPair("SaveFile", "SaveFile"));
    map.put("doFault", new MethodPair("doFault", "doFault"));
    map.put("getVersionName", new MethodPair("getVersionName", "getVersionName"));
    map.put("installURL", new MethodPair("installURL", "installURL"));
    map.put("isDirect", new MethodPair("isDirect", "isDirect"));
    map.put("setAssetsLoaded", new MethodPair("setAssetsLoaded", "setAssetsLoaded"));
    map.put("shutdown", new MethodPair("shutdown", "shutdown"));
    map.put("ShareFile", new MethodPair("ShareFile", "ShareFile"));
    map.put("ShareFileWithMessage", new MethodPair("ShareFileWithMessage", "ShareFileWithMessage"));
    map.put("ShareMessage", new MethodPair("ShareMessage", "ShareMessage"));
    map.put("Play", new MethodPair("Play", "Play"));
    map.put("DisplayDropdown", new MethodPair("DisplayDropdown", "DisplayDropdown"));
    map.put("ClearAll", new MethodPair("ClearAll", "ClearAll"));
    map.put("ClearTag", new MethodPair("ClearTag", "ClearTag"));
    map.put("GetTags", new MethodPair("GetTags", "GetTags"));
    map.put("Tweet", new MethodPair("Tweet", "Tweet"));
    map.put("TweetWithImage", new MethodPair("TweetWithImage", "TweetWithImage"));
    map.put("BuildRequestData", new MethodPair("BuildRequestData", "BuildRequestData"));
    map.put("PutFile", new MethodPair("PutFile", "PutFile"));
    map.put("PutText", new MethodPair("PutText", "PutText"));
    map.put("PutTextWithEncoding", new MethodPair("PutTextWithEncoding", "PutTextWithEncoding"));
    map.put("RequestTranslation", new MethodPair("RequestTranslation", "RequestTranslation"));
    
    return map;
  }
  
  /**
   * Get a Chinese translation map.
   * 
   * The output map has the following format: 
   *    Map = {propertyKey1: {propertyKey1, propertyValue1}, ...}
   * 
   * @return map
   */
  public static Map<String, MethodPair> chineseMap() {
    // TODO: Some methods may have different descriptions depending on which components
    // they are associated to; need to change the event name to be component type specific
    
    HashMap<String, MethodPair> map = new HashMap<String, MethodPair>();
    
    // Methods
    map.put("ResolveActivity", new MethodPair("ResolveActivity", "解决活动"));
    map.put("StartActivity", new MethodPair("StartActivity", "启动活动"));
    map.put("Bounce", new MethodPair("Bounce", "弹跳"));
    map.put("CollidingWith", new MethodPair("CollidingWith", "碰撞"));
    map.put("MoveIntoBounds", new MethodPair("MoveIntoBounds", "进入界"));
    map.put("MoveTo", new MethodPair("MoveTo", "移动到"));
    map.put("PointInDirection", new MethodPair("PointInDirection", "点方向"));
    map.put("PointTowards", new MethodPair("PointTowards", "点迎"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "字节可用来接收"));
    map.put("Connect", new MethodPair("Connect", "接"));
    map.put("ConnectWithUUID", new MethodPair("ConnectWithUUID", "连接UUID"));

    map.put("Disconnect", new MethodPair("Disconnect", "断开"));
    map.put("IsDevicePaired", new MethodPair("IsDevicePaired", "是设备配对"));
    map.put("ReceiveSigned1ByteNumber", new MethodPair("ReceiveSigned1ByteNumber", "收签名1字节数"));
    map.put("ReceiveSigned2ByteNumber", new MethodPair("ReceiveSigned2ByteNumber", "收签名2字节数"));
    map.put("ReceiveSigned4ByteNumber", new MethodPair("ReceiveSigned4ByteNumber", "收签名4字节数"));
    map.put("ReceiveSignedBytes", new MethodPair("ReceiveSignedBytes", "接收符号字节"));
    map.put("ReceiveText", new MethodPair("ReceiveText", "接收文本"));
    map.put("ReceiveUnsigned1ByteNumber", new MethodPair("ReceiveUnsigned1ByteNumber", "接收1字节无符号数"));
    map.put("ReceiveUnsigned2ByteNumber", new MethodPair("ReceiveUnsigned2ByteNumber", "接收2字节无符号数"));
    map.put("ReceiveUnsigned4ByteNumber", new MethodPair("ReceiveUnsigned4ByteNumber", "接收4字节无符号数"));
    map.put("ReceiveUnsignedBytes", new MethodPair("ReceiveUnsignedBytes", "接收无符号字节"));
    map.put("Send1ByteNumber", new MethodPair("Send1ByteNumber", "发送1字节数"));
    map.put("Send2ByteNumber", new MethodPair("Send2ByteNumber", "发送2字节数"));
    map.put("Send4ByteNumber", new MethodPair("Send4ByteNumber", "发送4字节数"));
    map.put("SendBytes", new MethodPair("SendBytes", "发送字节"));
    map.put("SendText", new MethodPair("SendText", "发送文本"));
    map.put("AcceptConnection", new MethodPair("AcceptConnection", "接受连线"));
    map.put("AcceptConnectionWithUUID", new MethodPair("AcceptConnectionWithUUID", "接受与UUID"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "字节可用来接收"));
    map.put("StopAccepting", new MethodPair("StopAccepting", "停止接受"));
   
    map.put("RecordVideo", new MethodPair("RecordVideo", "录製视频"));
    map.put("TakePicture", new MethodPair("TakePicture", "拍照"));
    map.put("Clear", new MethodPair("Clear", "清除"));
    map.put("DrawCircle", new MethodPair("DrawCircle", "画圆"));
    map.put("DrawLine", new MethodPair("DrawLine", "画线"));
    map.put("DrawPoint", new MethodPair("DrawPoint", "画点"));
    map.put("DrawText", new MethodPair("DrawText", "绘製文本"));
    map.put("DrawTextAtAngle", new MethodPair("DrawTextAtAngle", "绘製文本纠结"));
    map.put("GetBackgroundPixelColor", new MethodPair("GetBackgroundPixelColor", "背景像素的颜色"));
    map.put("GetPixelColor", new MethodPair("GetPixelColor", "像素颜色"));
    map.put("Save", new MethodPair("Save", "节省"));
    map.put("SaveAs", new MethodPair("SaveAs", "另存为"));
    
    map.put("SetBackgroundPixelColor", new MethodPair("SetBackgroundPixelColor", "设置背景像素的颜色"));
    map.put("AddDays", new MethodPair("AddDays", "添加天"));
    map.put("AddHours", new MethodPair("AddHours", "添加营业时间"));
    map.put("AddMinutes", new MethodPair("AddMinutes", "添加纪要"));
    map.put("AddMonths", new MethodPair("AddMonths", "添加个月"));
    map.put("AddSeconds", new MethodPair("AddSeconds", "添加秒"));
    map.put("AddWeeks", new MethodPair("AddWeeks", "週"));
    map.put("AddYears", new MethodPair("AddYears", "新增年"));
    map.put("DayOfMonth", new MethodPair("DayOfMonth", "月日"));
    map.put("Duration", new MethodPair("Duration", "为期"));
    map.put("FormatDate", new MethodPair("FormatDate", "格式日期"));
    map.put("FormatDateTime", new MethodPair("FormatDateTime", "格式日期时间"));
    
    map.put("FormatTime", new MethodPair("FormatTime", "格式化时间"));
    map.put("GetMillis", new MethodPair("GetMillis", "获取米利斯"));
    map.put("Hour", new MethodPair("Hour", "小时"));
    map.put("MakeInstant", new MethodPair("MakeInstant", "请即时"));
    map.put("MakeInstantFromMillis", new MethodPair("MakeInstantFromMillis", "即时从米利斯"));
    map.put("Minute", new MethodPair("Minute", "分钟"));
    map.put("Month", new MethodPair("Month", "月"));
    map.put("MonthName", new MethodPair("MonthName", "本月名称"));
    map.put("Now", new MethodPair("Now", "现在"));
    map.put("Second", new MethodPair("Second", "秒"));
    map.put("SystemTime", new MethodPair("SystemTime", "系统时间"));
    map.put("Weekday", new MethodPair("Weekday", "平日"));
    map.put("WeekdayName", new MethodPair("WeekdayName", "平日名称"));
    map.put("Year", new MethodPair("Year", "年"));
    map.put("Open", new MethodPair("Open", "打开"));
    map.put("CloseScreenAnimation", new MethodPair("CloseScreenAnimation", "关闭屏幕动画"));
    map.put("OpenScreenAnimation", new MethodPair("OpenScreenAnimation", "打开屏幕动画"));
    
    map.put("DoQuery", new MethodPair("DoQuery", "做查询"));
    map.put("ForgetLogin", new MethodPair("ForgetLogin", "忘记登入"));
    map.put("SendQuery", new MethodPair("SendQuery", "发送查询"));
    map.put("GetInstanceLists", new MethodPair("GetInstanceLists", "获取实例列表"));
    map.put("GetMessages", new MethodPair("GetMessages", "获取消息"));
    map.put("Invite", new MethodPair("Invite", "邀请"));
    map.put("LeaveInstance", new MethodPair("LeaveInstance", "离开实例"));
    map.put("MakeNewInstance", new MethodPair("MakeNewInstance", "新实例"));
    map.put("SendMessage", new MethodPair("SendMessage", "送信"));
    map.put("ServerCommand", new MethodPair("ServerCommand", "server命令"));
    map.put("SetInstance", new MethodPair("SetInstance", "设置实例"));
    map.put("SetLeader", new MethodPair("SetLeader", "集指挥"));
   
    map.put("PointTowards", new MethodPair("PointTowards", "点迎"));
    map.put("LatitudeFromAddress", new MethodPair("LatitudeFromAddress", "纬度从地址"));
    map.put("LongitudeFromAddress", new MethodPair("LongitudeFromAddress", "经度从地址"));
    map.put("LogError", new MethodPair("LogError", "登录错误"));
    map.put("LogInfo", new MethodPair("LogInfo", "登录信息"));
    map.put("LogWarning", new MethodPair("LogWarning", "日志警告"));
    map.put("ShowAlert", new MethodPair("ShowAlert", "显示警报"));
    map.put("ShowChooseDialog", new MethodPair("ShowChooseDialog", "显示选择对话框"));
    map.put("ShowMessageDialog", new MethodPair("ShowMessageDialog", "显示消息对话框"));
    map.put("ShowTextDialog", new MethodPair("ShowTextDialog", "显示文本对话框"));
    map.put("GetColor", new MethodPair("GetColor", "获取颜色"));
    map.put("GetLightLevel", new MethodPair("GetLightLevel", "光水平"));
    map.put("DeleteFile", new MethodPair("DeleteFile", "删除文件"));
    map.put("DownloadFile", new MethodPair("DownloadFile", "下载文件"));
    map.put("GetBatteryLevel", new MethodPair("GetBatteryLevel", "电池电量"));
    map.put("GetBrickName", new MethodPair("GetBrickName", "砖名字"));
    map.put("GetCurrentProgramName", new MethodPair("GetCurrentProgramName", "获取当前程序名称"));
    map.put("GetFirmwareVersion", new MethodPair("GetFirmwareVersion", "固件版本"));
    map.put("GetInputValues", new MethodPair("GetInputValues", "输入值"));
    map.put("GetOutputState", new MethodPair("GetOutputState", "输出状态"));
    map.put("KeepAlive", new MethodPair("KeepAlive", "永葆"));
   
    map.put("ListFiles", new MethodPair("ListFiles", "列表文件"));
    map.put("LsGetStatus", new MethodPair("LsGetStatus", "LS获取状态"));
    map.put("LsRead", new MethodPair("LsRead", "LS阅读"));
    map.put("LsWrite", new MethodPair("LsWrite", "LS写"));
    map.put("MessageRead", new MethodPair("MessageRead", "读消息"));
    map.put("MessageWrite", new MethodPair("MessageWrite", "签写留言"));
    map.put("PlaySoundFile", new MethodPair("PlaySoundFile", "播放声音文件"));
    map.put("PlayTone", new MethodPair("PlayTone", "播放音"));
    map.put("ResetInputScaledValue", new MethodPair("ResetInputScaledValue", "复位输入换算值"));
    map.put("ResetMotorPosition", new MethodPair("ResetMotorPosition", "复位电机位置"));
    map.put("SetBrickName", new MethodPair("SetBrickName", "设置砖名字"));
    map.put("SetInputMode", new MethodPair("SetInputMode", "设置输入模式"));
    map.put("SetOutputState", new MethodPair("SetOutputState", "设置输出状态"));
    
    map.put("StartProgram", new MethodPair("StartProgram", "启动程序"));
    map.put("StopProgram", new MethodPair("StopProgram", "停止计划"));
    map.put("StopSoundPlayback", new MethodPair("StopSoundPlayback", "停止声音播放"));
    map.put("LsWrite", new MethodPair("LsWrite", "LS写"));
    map.put("MoveBackward", new MethodPair("MoveBackward", "后移"));
    map.put("MoveBackwardIndefinitely", new MethodPair("MoveBackwardIndefinitely", "向后移动无限期"));
    map.put("MoveForward", new MethodPair("MoveForward", "正向移动"));
    map.put("MoveForwardIndefinitely", new MethodPair("MoveForwardIndefinitely", "无限期结转"));
    map.put("Stop", new MethodPair("Stop", "停止"));
    map.put("TurnClockwiseIndefinitely", new MethodPair("TurnClockwiseIndefinitely", "顺时针旋转无限期"));
    map.put("TurnCounterClockwiseIndefinitely", new MethodPair("TurnCounterClockwiseIndefinitely", "逆时针顺时针无限期"));
    map.put("GetSoundLevel", new MethodPair("GetSoundLevel", "声压级"));
    map.put("IsPressed", new MethodPair("IsPressed", "按下"));
    
    map.put("GetDistance", new MethodPair("GetDistance", "获取距离"));
    map.put("Pause", new MethodPair("Pause", "暂停"));
    map.put("Reset", new MethodPair("Reset", "复位"));
    map.put("Resume", new MethodPair("Resume", "恢复"));
    map.put("Start", new MethodPair("Start", "开始"));
    map.put("MakePhoneCall", new MethodPair("MakePhoneCall", "电话呼叫"));
    map.put("GetWifiIpAddress", new MethodPair("GetWifiIpAddress", "WIFI IP地址"));
    map.put("isConnected", new MethodPair("isConnected", "连接"));
    map.put("setHmacSeedReturnCode", new MethodPair("setHmacSeedReturnCode", "设置HMAC种子返回代码"));
    map.put("startHTTPD", new MethodPair("startHTTPD", "启动HTTPD")); 
    map.put("Vibrate", new MethodPair("Vibrate", "颤动"));
    map.put("GetText", new MethodPair("GetText", "获取文本"));
    map.put("HideKeyboard", new MethodPair("HideKeyboard", "隐藏键盘"));
    
    map.put("Speak", new MethodPair("Speak", "说话"));
    map.put("SendMessage", new MethodPair("SendMessage", "送信"));
    map.put("GetValue", new MethodPair("GetValue", "获得值"));
    map.put("StoreValue", new MethodPair("StoreValue", "储值"));
    map.put("Authorize", new MethodPair("Authorize", "授权"));
    map.put("CheckAuthorized", new MethodPair("CheckAuthorized", "检查授权"));
    map.put("DeAuthorize", new MethodPair("DeAuthorize", "取消授权"));
    map.put("DirectMessage", new MethodPair("DirectMessage", "直接留言"));
    map.put("Follow", new MethodPair("Follow", "遵循"));
    map.put("RequestDirectMessages", new MethodPair("RequestDirectMessages", "要求直接信息")); 
    map.put("RequestFollowers", new MethodPair("RequestFollowers", "请求关注"));
    map.put("RequestFriendTimeline", new MethodPair("RequestFriendTimeline", "请求朋友时间轴"));
    map.put("RequestMentions", new MethodPair("RequestMentions", "请求说起"));
    map.put("SearchTwitter", new MethodPair("SearchTwitter", "搜索Twitter"));
    map.put("SetStatus", new MethodPair("SetStatus", "设置状态"));
    map.put("StopFollowing", new MethodPair("StopFollowing", "停止继")); 
    map.put("GetDuration", new MethodPair("GetDuration", "获取时间"));
    map.put("SeekTo", new MethodPair("SeekTo", "寻求"));
    map.put("DoScan", new MethodPair("DoScan", "做扫描"));
     
    map.put("RequestBallot", new MethodPair("RequestBallot", "索取选票"));
    map.put("SendBallot", new MethodPair("SendBallot", "发送选票"));
    map.put("BuildPostData", new MethodPair("BuildPostData", "构建POST数据"));
    map.put("ClearCookies", new MethodPair("ClearCookies", "清除Cookies")); 
    map.put("Get", new MethodPair("Get", "得到"));
    map.put("HtmlTextDecode", new MethodPair("HtmlTextDecode", "HTML文本解码"));
    map.put("JsonTextDecode", new MethodPair("JsonTextDecode", "JSON文本解码"));
    map.put("PostFile", new MethodPair("PostFile", "发布文件"));
    map.put("PostText", new MethodPair("PostText", "发布文本"));
    map.put("PostTextWithEncoding", new MethodPair("PostTextWithEncoding", "发布带编码的文本")); 
    map.put("UriEncode", new MethodPair("UriEncode", "URI编码"));
    map.put("CanGoBack", new MethodPair("CanGoBack", "可以回去"));
    map.put("CanGoForward", new MethodPair("CanGoForward", "可以前进"));
    map.put("ClearLocations", new MethodPair("ClearLocations", "清除位置")); 
    map.put("GoBack", new MethodPair("GoBack", "返回"));
    map.put("GoForward", new MethodPair("GoForward", "前进"));
    map.put("GoHome", new MethodPair("GoHome", "返回首页"));
    map.put("GoToUrl", new MethodPair("GoToUrl", "转到URL"));
    
    map.put("AppendToFile", new MethodPair("AppendToFile", "加进文件"));
    map.put("Delete", new MethodPair("Delete", "删除"));
    map.put("ReadFrom", new MethodPair("ReadFrom", "读处"));
    map.put("SaveFile", new MethodPair("SaveFile", "保存文件"));
    map.put("doFault", new MethodPair("doFault", "做错"));
    map.put("getVersionName", new MethodPair("getVersionName", "得到版本名称"));
    map.put("installURL", new MethodPair("installURL", "安装URL"));
    map.put("isDirect", new MethodPair("isDirect", "是直接"));
    map.put("setAssetsLoaded", new MethodPair("setAssetsLoaded", "设置资产装了"));
    map.put("shutdown", new MethodPair("shutdown", "关闭"));
    map.put("ShareFile", new MethodPair("ShareFile", "分享文件"));
    map.put("ShareFileWithMessage", new MethodPair("ShareFileWithMessage", "分享文件加信息"));
    map.put("ShareMessage", new MethodPair("ShareMessage", "分享信息"));
    map.put("Play", new MethodPair("Play", "玩"));
    map.put("DisplayDropdown", new MethodPair("DisplayDropdown", "显示下啦是列表"));
    map.put("ClearAll", new MethodPair("ClearAll", "清除所有"));
    map.put("ClearTag", new MethodPair("ClearTag", "清除标签"));
    map.put("GetTags", new MethodPair("GetTags", "得到标签"));
    map.put("Tweet", new MethodPair("Tweet", "鸟叫"));
    map.put("TweetWithImage", new MethodPair("TweetWithImage", "带照片的鸟"));
    map.put("BuildRequestData", new MethodPair("BuildRequestData", "建筑请求材料"));
    map.put("PutFile", new MethodPair("PutFile", "放文件"));
    map.put("PutText", new MethodPair("PutText", "放文字"));
    map.put("PutTextWithEncoding", new MethodPair("PutTextWithEncoding", "放文字带编码"));
    map.put("RequestTranslation", new MethodPair("RequestTranslation", "请求翻译"));
    return map;
  }
}