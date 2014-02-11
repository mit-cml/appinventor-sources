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
    map.put("ResolveActivity", new MethodPair("ResolveActivity", "解決活動"));
    map.put("StartActivity", new MethodPair("StartActivity", "啟動活動"));
    map.put("Bounce", new MethodPair("Bounce", "彈跳"));
    map.put("CollidingWith", new MethodPair("CollidingWith", "碰撞"));
    map.put("MoveIntoBounds", new MethodPair("MoveIntoBounds", "進入界"));
    map.put("MoveTo", new MethodPair("MoveTo", "移動到"));
    map.put("PointInDirection", new MethodPair("PointInDirection", "點方向"));
    map.put("PointTowards", new MethodPair("PointTowards", "點迎"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "字節可用來接收"));
    map.put("Connect", new MethodPair("Connect", "接"));
    map.put("ConnectWithUUID", new MethodPair("ConnectWithUUID", "連接UUID"));

    map.put("Disconnect", new MethodPair("Disconnect", "斷開"));
    map.put("IsDevicePaired", new MethodPair("IsDevicePaired", "是設備配對"));
    map.put("ReceiveSigned1ByteNumber", new MethodPair("ReceiveSigned1ByteNumber", "收簽名1字節數"));
    map.put("ReceiveSigned2ByteNumber", new MethodPair("ReceiveSigned2ByteNumber", "收簽名2字節數"));
    map.put("ReceiveSigned4ByteNumber", new MethodPair("ReceiveSigned4ByteNumber", "收簽名4字節數"));
    map.put("ReceiveSignedBytes", new MethodPair("ReceiveSignedBytes", "接收符號字節"));
    map.put("ReceiveText", new MethodPair("ReceiveText", "接收文本"));
    map.put("ReceiveUnsigned1ByteNumber", new MethodPair("ReceiveUnsigned1ByteNumber", "接收1字節無符號數"));
    map.put("ReceiveUnsigned2ByteNumber", new MethodPair("ReceiveUnsigned2ByteNumber", "接收2字節無符號數"));
    map.put("ReceiveUnsigned4ByteNumber", new MethodPair("ReceiveUnsigned4ByteNumber", "接收4字節無符號數"));
    map.put("ReceiveUnsignedBytes", new MethodPair("ReceiveUnsignedBytes", "接收無符號字節"));
    map.put("Send1ByteNumber", new MethodPair("Send1ByteNumber", "發送1字節數"));
    map.put("Send2ByteNumber", new MethodPair("Send2ByteNumber", "發送2字節數"));
    map.put("Send4ByteNumber", new MethodPair("Send4ByteNumber", "發送4字節數"));
    map.put("SendBytes", new MethodPair("SendBytes", "發送字節"));
    map.put("SendText", new MethodPair("SendText", "發送文本"));
    map.put("AcceptConnection", new MethodPair("AcceptConnection", "接受連線"));
    map.put("AcceptConnectionWithUUID", new MethodPair("AcceptConnectionWithUUID", "接受與UUID"));
    map.put("BytesAvailableToReceive", new MethodPair("BytesAvailableToReceive", "字節可用來接收"));
    map.put("StopAccepting", new MethodPair("StopAccepting", "停止接受"));
   
    map.put("RecordVideo", new MethodPair("RecordVideo", "錄製視頻"));
    map.put("TakePicture", new MethodPair("TakePicture", "拍照"));
    map.put("Clear", new MethodPair("Clear", "清除"));
    map.put("DrawCircle", new MethodPair("DrawCircle", "畫圓"));
    map.put("DrawLine", new MethodPair("DrawLine", "畫線"));
    map.put("DrawPoint", new MethodPair("DrawPoint", "畫點"));
    map.put("DrawText", new MethodPair("DrawText", "繪製文本"));
    map.put("DrawTextAtAngle", new MethodPair("DrawTextAtAngle", "繪製文本糾結"));
    map.put("GetBackgroundPixelColor", new MethodPair("GetBackgroundPixelColor", "背景像素的顏色"));
    map.put("GetPixelColor", new MethodPair("GetPixelColor", "像素顏色"));
    map.put("Save", new MethodPair("Save", "節省"));
    map.put("SaveAs", new MethodPair("SaveAs", "另存為"));
    
    map.put("SetBackgroundPixelColor", new MethodPair("SetBackgroundPixelColor", "設置背景像素的顏色"));
    map.put("AddDays", new MethodPair("AddDays", "添加天"));
    map.put("AddHours", new MethodPair("AddHours", "添加營業時間"));
    map.put("AddMinutes", new MethodPair("AddMinutes", "添加紀要"));
    map.put("AddMonths", new MethodPair("AddMonths", "添加個月"));
    map.put("AddSeconds", new MethodPair("AddSeconds", "添加秒"));
    map.put("AddWeeks", new MethodPair("AddWeeks", "週"));
    map.put("AddYears", new MethodPair("AddYears", "新增年"));
    map.put("DayOfMonth", new MethodPair("DayOfMonth", "月日"));
    map.put("Duration", new MethodPair("Duration", "為期"));
    map.put("FormatDate", new MethodPair("FormatDate", "格式日期"));
    map.put("FormatDateTime", new MethodPair("FormatDateTime", "格式日期時間"));
    
    map.put("FormatTime", new MethodPair("FormatTime", "格式化時間"));
    map.put("GetMillis", new MethodPair("GetMillis", "獲取米利斯"));
    map.put("Hour", new MethodPair("Hour", "小時"));
    map.put("MakeInstant", new MethodPair("MakeInstant", "請即時"));
    map.put("MakeInstantFromMillis", new MethodPair("MakeInstantFromMillis", "即時從米利斯"));
    map.put("Minute", new MethodPair("Minute", "分鐘"));
    map.put("Month", new MethodPair("Month", "月"));
    map.put("MonthName", new MethodPair("MonthName", "本月名稱"));
    map.put("Now", new MethodPair("Now", "現在"));
    map.put("Second", new MethodPair("Second", "秒"));
    map.put("SystemTime", new MethodPair("SystemTime", "系統時間"));
    map.put("Weekday", new MethodPair("Weekday", "平日"));
    map.put("WeekdayName", new MethodPair("WeekdayName", "平日名稱"));
    map.put("Year", new MethodPair("Year", "年"));
    map.put("Open", new MethodPair("Open", "打開"));
    map.put("CloseScreenAnimation", new MethodPair("CloseScreenAnimation", "關閉屏幕動畫"));
    map.put("OpenScreenAnimation", new MethodPair("OpenScreenAnimation", "打開屏幕動畫"));
    
    map.put("DoQuery", new MethodPair("DoQuery", "做查詢"));
    map.put("ForgetLogin", new MethodPair("ForgetLogin", "忘記登入"));
    map.put("SendQuery", new MethodPair("SendQuery", "發送查詢"));
    map.put("GetInstanceLists", new MethodPair("GetInstanceLists", "獲取實例列表"));
    map.put("GetMessages", new MethodPair("GetMessages", "獲取消息"));
    map.put("Invite", new MethodPair("Invite", "邀請"));
    map.put("LeaveInstance", new MethodPair("LeaveInstance", "離開實例"));
    map.put("MakeNewInstance", new MethodPair("MakeNewInstance", "新實例"));
    map.put("SendMessage", new MethodPair("SendMessage", "送信"));
    map.put("ServerCommand", new MethodPair("ServerCommand", "server命令"));
    map.put("SetInstance", new MethodPair("SetInstance", "設置實例"));
    map.put("SetLeader", new MethodPair("SetLeader", "集指揮"));
   
    map.put("PointTowards", new MethodPair("PointTowards", "點迎"));
    map.put("LatitudeFromAddress", new MethodPair("LatitudeFromAddress", "緯度從地址"));
    map.put("LongitudeFromAddress", new MethodPair("LongitudeFromAddress", "經度從地址"));
    map.put("LogError", new MethodPair("LogError", "登錄錯誤"));
    map.put("LogInfo", new MethodPair("LogInfo", "登錄信息"));
    map.put("LogWarning", new MethodPair("LogWarning", "日誌警告"));
    map.put("ShowAlert", new MethodPair("ShowAlert", "顯示警報"));
    map.put("ShowChooseDialog", new MethodPair("ShowChooseDialog", "顯示選擇對話框"));
    map.put("ShowMessageDialog", new MethodPair("ShowMessageDialog", "顯示消息對話框"));
    map.put("ShowTextDialog", new MethodPair("ShowTextDialog", "顯示文本對話框"));
    map.put("GetColor", new MethodPair("GetColor", "獲取顏色"));
    map.put("GetLightLevel", new MethodPair("GetLightLevel", "光水平"));
    map.put("DeleteFile", new MethodPair("DeleteFile", "刪除文件"));
    map.put("DownloadFile", new MethodPair("DownloadFile", "下載文件"));
    map.put("GetBatteryLevel", new MethodPair("GetBatteryLevel", "電池電量"));
    map.put("GetBrickName", new MethodPair("GetBrickName", "磚名字"));
    map.put("GetCurrentProgramName", new MethodPair("GetCurrentProgramName", "獲取當前程序名稱"));
    map.put("GetFirmwareVersion", new MethodPair("GetFirmwareVersion", "固件版本"));
    map.put("GetInputValues", new MethodPair("GetInputValues", "輸入值"));
    map.put("GetOutputState", new MethodPair("GetOutputState", "輸出狀態"));
    map.put("KeepAlive", new MethodPair("KeepAlive", "永葆"));
   
    map.put("ListFiles", new MethodPair("ListFiles", "列表文件"));
    map.put("LsGetStatus", new MethodPair("LsGetStatus", "LS獲取狀態"));
    map.put("LsRead", new MethodPair("LsRead", "LS閱讀"));
    map.put("LsWrite", new MethodPair("LsWrite", "LS寫"));
    map.put("MessageRead", new MethodPair("MessageRead", "讀消息"));
    map.put("MessageWrite", new MethodPair("MessageWrite", "簽寫留言"));
    map.put("PlaySoundFile", new MethodPair("PlaySoundFile", "播放聲音文件"));
    map.put("PlayTone", new MethodPair("PlayTone", "播放音"));
    map.put("ResetInputScaledValue", new MethodPair("ResetInputScaledValue", "復位輸入換算值"));
    map.put("ResetMotorPosition", new MethodPair("ResetMotorPosition", "復位電機位置"));
    map.put("SetBrickName", new MethodPair("SetBrickName", "設置磚名字"));
    map.put("SetInputMode", new MethodPair("SetInputMode", "設置輸入模式"));
    map.put("SetOutputState", new MethodPair("SetOutputState", "設置輸出狀態"));
    
    map.put("StartProgram", new MethodPair("StartProgram", "啟動程序"));
    map.put("StopProgram", new MethodPair("StopProgram", "停止計劃"));
    map.put("StopSoundPlayback", new MethodPair("StopSoundPlayback", "停止聲音播放"));
    map.put("LsWrite", new MethodPair("LsWrite", "LS寫"));
    map.put("MoveBackward", new MethodPair("MoveBackward", "後移"));
    map.put("MoveBackwardIndefinitely", new MethodPair("MoveBackwardIndefinitely", "向後移動無限期"));
    map.put("MoveForward", new MethodPair("MoveForward", "正向移動"));
    map.put("MoveForwardIndefinitely", new MethodPair("MoveForwardIndefinitely", "無限期結轉"));
    map.put("Stop", new MethodPair("Stop", "停止"));
    map.put("TurnClockwiseIndefinitely", new MethodPair("TurnClockwiseIndefinitely", "順時針旋轉無限期"));
    map.put("TurnCounterClockwiseIndefinitely", new MethodPair("TurnCounterClockwiseIndefinitely", "逆時針順時針無限期"));
    map.put("GetSoundLevel", new MethodPair("GetSoundLevel", "聲壓級"));
    map.put("IsPressed", new MethodPair("IsPressed", "按下"));
    
    map.put("GetDistance", new MethodPair("GetDistance", "獲取距離"));
    map.put("Pause", new MethodPair("Pause", "暫停"));
    map.put("Reset", new MethodPair("Reset", "復位"));
    map.put("Resume", new MethodPair("Resume", "恢復"));
    map.put("Start", new MethodPair("Start", "開始"));
    map.put("MakePhoneCall", new MethodPair("MakePhoneCall", "電話呼叫"));
    map.put("GetWifiIpAddress", new MethodPair("GetWifiIpAddress", "WIFI IP地址"));
    map.put("isConnected", new MethodPair("isConnected", "連接"));
    map.put("setHmacSeedReturnCode", new MethodPair("setHmacSeedReturnCode", "設置HMAC種子返回代碼"));
    map.put("startHTTPD", new MethodPair("startHTTPD", "啟動HTTPD")); 
    map.put("Vibrate", new MethodPair("Vibrate", "顫動"));
    map.put("GetText", new MethodPair("GetText", "獲取文本"));
    map.put("HideKeyboard", new MethodPair("HideKeyboard", "隱藏鍵盤"));
    
    map.put("Speak", new MethodPair("Speak", "說話"));
    map.put("SendMessage", new MethodPair("SendMessage", "送信"));
    map.put("GetValue", new MethodPair("GetValue", "獲得值"));
    map.put("StoreValue", new MethodPair("StoreValue", "儲值"));
    map.put("Authorize", new MethodPair("Authorize", "授權"));
    map.put("CheckAuthorized", new MethodPair("CheckAuthorized", "檢查授權"));
    map.put("DeAuthorize", new MethodPair("DeAuthorize", "取消授權"));
    map.put("DirectMessage", new MethodPair("DirectMessage", "直接留言"));
    map.put("Follow", new MethodPair("Follow", "遵循"));
    map.put("RequestDirectMessages", new MethodPair("RequestDirectMessages", "要求直接信息")); 
    map.put("RequestFollowers", new MethodPair("RequestFollowers", "請求關注"));
    map.put("RequestFriendTimeline", new MethodPair("RequestFriendTimeline", "請求朋友時間軸"));
    map.put("RequestMentions", new MethodPair("RequestMentions", "請求說起"));
    map.put("SearchTwitter", new MethodPair("SearchTwitter", "搜索Twitter"));
    map.put("SetStatus", new MethodPair("SetStatus", "設置狀態"));
    map.put("StopFollowing", new MethodPair("StopFollowing", "停止繼")); 
    map.put("GetDuration", new MethodPair("GetDuration", "獲取時間"));
    map.put("SeekTo", new MethodPair("SeekTo", "尋求"));
    map.put("DoScan", new MethodPair("DoScan", "做掃描"));
     
    map.put("RequestBallot", new MethodPair("RequestBallot", "索取選票"));
    map.put("SendBallot", new MethodPair("SendBallot", "發送選票"));
    map.put("BuildPostData", new MethodPair("BuildPostData", "構建POST數據"));
    map.put("ClearCookies", new MethodPair("ClearCookies", "清除Cookies")); 
    map.put("Get", new MethodPair("Get", "得到"));
    map.put("HtmlTextDecode", new MethodPair("HtmlTextDecode", "HTML文本解碼"));
    map.put("JsonTextDecode", new MethodPair("JsonTextDecode", "JSON文本解碼"));
    map.put("PostFile", new MethodPair("PostFile", "發布文件"));
    map.put("PostText", new MethodPair("PostText", "發布文本"));
    map.put("PostTextWithEncoding", new MethodPair("PostTextWithEncoding", "發布帶編碼的文本")); 
    map.put("UriEncode", new MethodPair("UriEncode", "URI編碼"));
    map.put("CanGoBack", new MethodPair("CanGoBack", "可以回去"));
    map.put("CanGoForward", new MethodPair("CanGoForward", "可以前進"));
    map.put("ClearLocations", new MethodPair("ClearLocations", "清除位置")); 
    map.put("GoBack", new MethodPair("GoBack", "返回"));
    map.put("GoForward", new MethodPair("GoForward", "前進"));
    map.put("GoHome", new MethodPair("GoHome", "返回首頁"));
    map.put("GoToUrl", new MethodPair("GoToUrl", "轉到URL"));
    return map;
  }
}