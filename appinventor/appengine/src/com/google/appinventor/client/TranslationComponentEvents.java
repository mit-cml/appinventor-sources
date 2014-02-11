package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationComponentEvents {

  public static class EventPair {

    private String key;
    private String value;
    private String description;

    public EventPair(String key, String value) {
      this(key, value, "");
    }

    public EventPair(String key, String value, String description) {
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
  public static Map<String, EventPair> myMap = createMap(languageSetting);

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

  public static Map<String, EventPair> createMap(String language) {
    Map<String, EventPair> map;
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
      OdeLog.log("Map does not contain key " + key);
      return key;
    }
    return myMap.get(key).getKey();
  }
  
  public static String getMapJSON(String eventName) {
    if (myMap.containsKey(eventName)) {
      return myMap.get(eventName).toString();
    }
    OdeLog.log("Event map does not contain key");
    return new EventPair(eventName, eventName).toString();
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
   *    Map = [{eventName1: EventPair1}, ...]
   * 
   * @return map
   */
  public static HashMap<String, EventPair> englishMap() { 
    // TODO: Some events may have different descriptions depending on which components
    // they are associated to; need to change the event name to be component type specific
    HashMap<String, EventPair> map = new HashMap<String, EventPair>();
    
    // Events
    map.put("AccelerationChanged", new EventPair("AccelerationChanged", "AccelerationChanged",
        "Indicates the acceleration changed in the X, Y, and or Z dimensions."));
    map.put("AfterActivity", new EventPair("AfterActivity", "AfterActivity",
        "Event raised after this ActivityStarter returns."));
    map.put("CollidedWith", new EventPair("CollidedWith", "CollidedWith"));
    map.put("Dragged", new EventPair("Dragged", "Dragged"));
    map.put("EdgeReached", new EventPair("EdgeReached", "EdgeReached"));
    map.put("Flung", new EventPair("Flung", "Flung"));
    map.put("NoLongerCollidingWith", new EventPair("NoLongerCollidingWith", "NoLongerCollidingWith",
        "Event indicating that a pair of sprites are no longer colliding."));
    map.put("TouchDown", new EventPair("TouchDown", "TouchDown"));
    map.put("TouchUp", new EventPair("TouchUp", "TouchUp"));
    map.put("Touched", new EventPair("Touched", "Touched"));

    map.put("AfterScan", new EventPair("AfterScan", "AfterScan",
        "Simple event to raise after the scanner activity has returned"));
    map.put("ConnectionAccepted", new EventPair("ConnectionAccepted", "ConnectionAccepted",
        "Indicates that a bluetooth connection has been accepted."));
    map.put("Click", new EventPair("Click", "Click",
        "Indicates a user has clicked on the button."));
    map.put("GotFocus", new EventPair("GotFocus", "GotFocus",
        "GotFocus event handler."));
    map.put("LongClick", new EventPair("LongClick", "LongClick",
        "Indicates a user has long clicked on the button."));
    map.put("LostFocus", new EventPair("LostFocus", "LostFocus",
        "LostFocus event handler."));
    map.put("AfterRecording", new EventPair("AfterRecording", "AfterRecording"));
    map.put("AfterPicture", new EventPair("AfterPicture", "AfterPicture"));
    map.put("Changed", new EventPair("Changed", "Changed",
        "Changed event handler."));
    map.put("Timer", new EventPair("Timer", "Timer", 
        "Timer has gone off."));
    map.put("AfterPicking", new EventPair("AfterPicking", "AfterPicking"));
    map.put("BeforePicking", new EventPair("BeforePicking", "BeforePicking"));
    
    map.put("BackPressed", new EventPair("BackPressed", "BackPressed",
        "Device back button pressed."));
    map.put("ErrorOccurred", new EventPair("ErrorOccurred", "ErrorOccurred"));
    map.put("Initialize", new EventPair("Initialize", "Initialize",
        "Screen starting"));
    map.put("OtherScreenClosed", new EventPair("OtherScreenClosed", "OtherScreenClosed",
        "Event raised when another screen has closed and control has returned to this screen."));
    map.put("ScreenOrientationChanged", new EventPair("ScreenOrientationChanged", "ScreenOrientationChanged",
        "Screen orientation changed"));
    map.put("GotResult", new EventPair("GotResult", "GotResult"));
    
    map.put("FunctionCompleted", new EventPair("FunctionCompleted", "FunctionCompleted",
        "Indicates that a function call completed."));
    map.put("GotMessage", new EventPair("GotMessage", "GotMessage",
        "Indicates that a new message has been received."));
    map.put("Info", new EventPair("Info", "Info",
        "Indicates that something has occurred which the player should know about."));
    map.put("InstanceIdChanged", new EventPair("InstanceIdChanged", "InstanceIdChanged",
        "Indicates that the InstanceId property has changed as a result of calling MakeNewInstance or SetInstance."));
    map.put("Invited", new EventPair("Invited", "Invited",
        "Indicates that a user has been invited to this game instance."));
    map.put("NewInstanceMade", new EventPair("NewInstanceMade", "NewInstanceMade",
        "Indicates that a new instance was successfully created after calling MakeNewInstance."));
    map.put("NewLeader", new EventPair("NewLeader", "NewLeader",
        "Indicates that this game has a new leader as specified through SetLeader"));
    map.put("PlayerJoined", new EventPair("PlayerJoined", "PlayerJoined",
        "Indicates that a new player has joined this game instance."));
    map.put("PlayerLeft", new EventPair("PlayerLeft", "PlayerLeft",
        "Indicates that a player has left this game instance."));
    map.put("ServerCommandFailure", new EventPair("ServerCommandFailure", "ServerCommandFailure",
        "Indicates that a server command failed."));
    map.put("ServerCommandSuccess", new EventPair("ServerCommandSuccess", "ServerCommandSuccess",
        "Indicates that a server command returned successfully."));
    map.put("UserEmailAddressSet", new EventPair("UserEmailAddressSet", "UserEmailAddressSet",
        "Indicates that the user email address has been set."));
    map.put("WebServiceError", new EventPair("WebServiceError", "WebServiceError",
        "Indicates that an error occurred while communicating with the web server."));
    
    map.put("LocationChanged", new EventPair("LocationChanged", "LocationChanged",
        "Indicates that a new location has been detected."));
    map.put("StatusChanged", new EventPair("StatusChanged", "StatusChanged",
        "Indicates that the status of the provider has changed."));
    
    map.put("AfterChoosing", new EventPair("AfterChoosing", "AfterChoosing",
        "Event after the user has made a selection for ShowChooseDialog."));
    map.put("AfterTextInput", new EventPair("AfterTextInput", "AfterTextInput",
        "Event raised after the user has responded to ShowTextDialog."));
    
    map.put("AboveRange", new EventPair("AboveRange", "AboveRange"));
    map.put("BelowRange", new EventPair("BelowRange", "BelowRange"));
    map.put("ColorChanged", new EventPair("ColorChanged", "ColorChanged"));
    map.put("WithinRange", new EventPair("WithinRange", "WithinRange"));
    map.put("Pressed", new EventPair("Pressed", "Pressed"));
    map.put("Released", new EventPair("Released", "Released"));
    map.put("OrientationChanged", new EventPair("OrientationChanged", "OrientationChanged"));
    map.put("CalibrationFailed", new EventPair("CalibrationFailed", "CalibrationFailed"));
    map.put("GPSAvailable", new EventPair("GPSAvailable", "GPSAvailable"));
    map.put("GPSLost", new EventPair("GPSLost", "GPSLost"));
    map.put("SimpleStep", new EventPair("SimpleStep", "SimpleStep"));

    map.put("StartedMoving", new EventPair("StartedMoving", "StartedMoving"));
    map.put("StoppedMoving", new EventPair("StoppedMoving", "StoppedMoving"));
    map.put("WalkStep", new EventPair("WalkStep", "WalkStep"));
    map.put("Completed", new EventPair("Completed", "Completed"));
    map.put("AfterSoundRecorded", new EventPair("AfterSoundRecorded", "AfterSoundRecorded"));
    map.put("StartedRecording", new EventPair("StartedRecording", "StartedRecording"));
    map.put("StoppedRecording", new EventPair("StoppedRecording", "StoppedRecording"));
    map.put("AfterGettingText", new EventPair("AfterGettingText", "AfterGettingText"));
    map.put("BeforeGettingText", new EventPair("BeforeGettingText", "BeforeGettingText"));
    map.put("AfterSpeaking", new EventPair("AfterSpeaking", "AfterSpeaking"));
    map.put("BeforeSpeaking", new EventPair("BeforeSpeaking", "BeforeSpeaking"));
    map.put("MessageReceived", new EventPair("MessageReceived", "MessageReceived"));
    map.put("SendMessage", new EventPair("SendMessage", "SendMessage"));
    
    map.put("GotValue", new EventPair("GotValue", "GotValue"));
    map.put("ValueStored", new EventPair("ValueStored", "ValueStored"));
    map.put("DirectMessagesReceived", new EventPair("DirectMessagesReceived", "DirectMessagesReceived"));
    map.put("FollowersReceived", new EventPair("FollowersReceived", "FollowersReceived"));
    map.put("FriendTimelineReceived", new EventPair("FriendTimelineReceived", "FriendTimelineReceived"));
    map.put("IsAuthorized", new EventPair("IsAuthorized", "IsAuthorized"));
    map.put("MentionsReceived", new EventPair("MentionsReceived", "MentionsReceived"));
    map.put("SearchSuccessful", new EventPair("SearchSuccessful", "SearchSuccessful"));
    map.put("GotBallot", new EventPair("GotBallot", "GotBallot"));
    map.put("GotBallotConfirmation", new EventPair("GotBallotConfirmation", "GotBallotConfirmation"));
    map.put("NoOpenPoll", new EventPair("NoOpenPoll", "NoOpenPoll"));
    map.put("GotFile", new EventPair("GotFile", "GotFile"));
    map.put("GotText", new EventPair("GotText", "GotText"));
    map.put("LongClick", new EventPair("LongClick", "LongClick"));
    map.put("Shaking", new EventPair("Shaking", "Shaking"));
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
  public static Map<String, EventPair> chineseMap() {
    HashMap<String, EventPair> map = new HashMap<String, EventPair>();
    
    map.put("AccelerationChanged", new EventPair("AccelerationChanged", "加速改变"));
    map.put("AfterActivity", new EventPair("AfterActivity", "活动后"));
    map.put("CollidedWith", new EventPair("CollidedWith", "相撞"));
    map.put("Dragged", new EventPair("Dragged", "拖"));
    map.put("EdgeReached", new EventPair("EdgeReached", "达到的边缘"));
    map.put("Flung", new EventPair("Flung", "甩到"));
    map.put("NoLongerCollidingWith", new EventPair("NoLongerCollidingWith", "不再碰撞"));
    map.put("TouchDown", new EventPair("TouchDown", "向下触摸"));
    map.put("TouchUp", new EventPair("TouchUp", "润色"));
    map.put("Touched", new EventPair("Touched", "触摸"));

    map.put("AfterScan", new EventPair("AfterScan", "扫描后"));
    map.put("ConnectionAccepted", new EventPair("ConnectionAccepted", "连接被接受"));
    map.put("Click", new EventPair("Click", "点击"));
    map.put("GotFocus", new EventPair("GotFocus", "获得焦点"));
    map.put("LostFocus", new EventPair("LostFocus", "失去焦点"));
    map.put("AfterRecording", new EventPair("AfterRecording", "后录音"));
    map.put("AfterPicture", new EventPair("AfterPicture", "经过图片"));
    map.put("Changed", new EventPair("Changed", "变"));
    map.put("Timer", new EventPair("Timer", "定时器"));
    map.put("AfterPicking", new EventPair("AfterPicking", "采摘后"));
    map.put("BeforePicking", new EventPair("BeforePicking", "采摘前"));
    
    map.put("BackPressed", new EventPair("BackPressed", "回压"));
    map.put("ErrorOccurred", new EventPair("ErrorOccurred", "发生错误"));
    map.put("Initialize", new EventPair("Initialize", "初始化"));
    map.put("OtherScreenClosed", new EventPair("OtherScreenClosed", "其他屏幕关闭"));
    map.put("ScreenOrientationChanged", new EventPair("ScreenOrientationChanged", "屏幕方向改变"));
    
    map.put("GotResult", new EventPair("GotResult", "获取结果"));
    map.put("FunctionCompleted", new EventPair("FunctionCompleted", "完成功能"));
    map.put("GotMessage", new EventPair("GotMessage", "获取信息"));

    map.put("Info", new EventPair("Info", "信息"));
    map.put("InstanceIdChanged", new EventPair("InstanceIdChanged", "实例ID改变"));
    map.put("Invited", new EventPair("Invited", "邀请"));
    map.put("NewInstanceMade", new EventPair("NewInstanceMade", "新的实例"));
    map.put("NewLeader", new EventPair("NewLeader", "新领导人"));
    map.put("PlayerJoined", new EventPair("PlayerJoined", "球员加入"));
    map.put("PlayerLeft", new EventPair("PlayerLeft", "玩家离开"));
    map.put("ServerCommandFailure", new EventPair("ServerCommandFailure", "服务器命令失败"));
    map.put("ServerCommandSuccess", new EventPair("ServerCommandSuccess", "服务器命令成功"));
    map.put("UserEmailAddressSet", new EventPair("UserEmailAddressSet", "设置用户电子邮件地址"));
    map.put("WebServiceError", new EventPair("WebServiceError", "WebService的错误"));
    map.put("LocationChanged", new EventPair("LocationChanged", "地點已更改"));
    map.put("StatusChanged", new EventPair("StatusChanged", "狀態已改變"));
    
    map.put("AfterChoosing", new EventPair("AfterChoosing", "選擇後"));
    map.put("AfterTextInput", new EventPair("AfterTextInput", "文字輸入後"));
    map.put("AboveRange", new EventPair("AboveRange", "上述範圍"));
    map.put("BelowRange", new EventPair("BelowRange", "下面範圍"));
    map.put("ColorChanged", new EventPair("ColorChanged", "顏色改變"));
    map.put("WithinRange", new EventPair("WithinRange", "範圍內"));
    map.put("Pressed", new EventPair("Pressed", "按下"));
    map.put("Released", new EventPair("Released", "發布"));
    map.put("OrientationChanged", new EventPair("OrientationChanged", "取向改變"));
    map.put("CalibrationFailed", new EventPair("CalibrationFailed", "校準失敗"));
    map.put("GPSAvailable", new EventPair("GPSAvailable", "GPS可用"));
    map.put("GPSLost", new EventPair("GPSLost", "GPS丟失"));
    map.put("SimpleStep", new EventPair("SimpleStep", "簡單的步驟"));
    
    map.put("StartedMoving", new EventPair("StartedMoving", "開始移動"));
    map.put("StoppedMoving", new EventPair("StoppedMoving", "停止移動"));
    map.put("WalkStep", new EventPair("WalkStep", "步行步"));
    map.put("Completed", new EventPair("Completed", "已完成"));
    map.put("AfterSoundRecorded", new EventPair("AfterSoundRecorded", "錄製的聲音後"));
    map.put("StartedRecording", new EventPair("StartedRecording", "開始錄製"));
    map.put("StoppedRecording", new EventPair("StoppedRecording", "停止記錄"));
    map.put("AfterGettingText", new EventPair("AfterGettingText", "起床後文本"));
    map.put("BeforeGettingText", new EventPair("BeforeGettingText", "在獲取文本"));
    map.put("AfterSpeaking", new EventPair("AfterSpeaking", "說到後"));
    map.put("BeforeSpeaking", new EventPair("BeforeSpeaking", "說起前"));
    map.put("MessageReceived", new EventPair("MessageReceived", "收到的消息"));
    map.put("SendMessage", new EventPair("SendMessage", "送信"));
    
    map.put("GotValue", new EventPair("GotValue", "獲得值"));
    map.put("ValueStored", new EventPair("ValueStored", "值存儲"));
    map.put("DirectMessagesReceived", new EventPair("DirectMessagesReceived", "直接收到消息"));
    map.put("FollowersReceived", new EventPair("FollowersReceived", "收到追隨者"));
    map.put("FriendTimelineReceived", new EventPair("FriendTimelineReceived", "朋友時間軸收到的"));
    map.put("IsAuthorized", new EventPair("IsAuthorized", "授權"));
    map.put("MentionsReceived", new EventPair("MentionsReceived", "說起收到"));
    map.put("SearchSuccessful", new EventPair("SearchSuccessful", "搜索成功"));
    map.put("GotBallot", new EventPair("GotBallot", "獲取選票"));
    map.put("GotBallotConfirmation", new EventPair("GotBallotConfirmation", "得到選票的確認"));
    map.put("NoOpenPoll", new EventPair("NoOpenPoll", "沒有開放投票"));
    map.put("GotFile", new EventPair("GotFile", "獲取文件"));
    map.put("GotText", new EventPair("GotText", "獲得文本"));
    map.put("LongClick", new EventPair("LongClick", "長按"));
    map.put("Shaking", new EventPair("Shaking", "發抖"));

    return map;
  }
}