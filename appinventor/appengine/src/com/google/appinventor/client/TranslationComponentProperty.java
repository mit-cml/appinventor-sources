package com.google.appinventor.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationComponentProperty {

  public static String languageSetting = "zh_TW";
  public static Map<String, String> myMap = createMap(languageSetting);

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

  public static Map<String, String> createMap(String language) {
    Map<String, String> map;
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
    return myMap.get(key);
  }

  /**
   * The output is a sequence of component descriptions enclosed in square
   * brackets and separated by commas.
   * 
   * Each component description has the following format: 
   *  { "name": "COMPONENT-TYPE-NAME", 
   *    "blockProperties": 
   *      [ { "key": "PROPERTY-NAME",
   *          "value": "PROPERTY-VALUE"},* 
   *      ] 
   *  }
   */
  public static String getMapJSON(String typeName, List<String> propertyKeys) {
    StringBuilder sb = new StringBuilder();
    sb.append("{ \"name\": \"");
    sb.append(typeName);

    sb.append("\",\n  \"blockProperties\": [");
    String separator = "";
    for (String key : propertyKeys) {
      if (myMap.containsKey(key)) {
        sb.append(separator);
        separator = ",\n    ";
        sb.append("{ \"key\": \"");
        sb.append(key);
        sb.append("\", \"value\": \"");
        sb.append(myMap.get(key));
        sb.append("\"}");
      }
    }
    sb.append("]}\n");
    return sb.toString();
  }

  /**
   * Get JSON representation of the translation map
   * 
   * @param components
   *          A dictionary of {typeName: propertyList}
   */
  public static String getMapJSON(Map<String, List<String>> components) {
    StringBuilder sb = new StringBuilder();

    sb.append('[');
    String separator = "";

    for (String componentTypeName : components.keySet()) {
      sb.append(separator);
      sb.append(getMapJSON(componentTypeName, components.get(componentTypeName)));
      separator = ",\n";
    }

    sb.append(']');
    System.err.println(sb.toString());
    return sb.toString();
  }

  /**
   * Get an English translation map.
   * 
   * The output map has the following format: Map = {propertyKey1:
   * {propertyKey1, propertyValue1}, ...}
   * 
   * @return map
   */
  public static HashMap<String, String> englishMap() {
    HashMap<String, String> map = new HashMap<String, String>();
    // Properties
    map.put("AboveRangeEventEnabled", "AboveRangeEventEnabled");
    map.put("Action", "Action");
    map.put("ActivityClass", "ActivityClass");
    map.put("ActivityPackage", "ActivityPackage");
    map.put("Accuracy", "Accuracy");
    map.put("AddressesAndNames", "AddressesAndNames");
    map.put("AlignHorizontal", "AlignHorizontal");
    map.put("AlignVertical", "AlignVertical");
    map.put("AllowCookies", "AllowCookies");
    map.put("Altitude", "Altitude");
    map.put("Angle", "Angle");
    map.put("Animation", "Animation");
    map.put("ApiKey", "ApiKey");
    map.put("Available", "Available");
    map.put("AvailableProviders", "AvailableProviders");
    map.put("Azimuth", "Azimuth");
    map.put("BackgroundColor", "BackgroundColor");
    map.put("BackgroundImage", "BackgroundImage");
    map.put("BallotOptions", "BallotOptions");
    map.put("BallotQuestion", "BallotQuestion");
    map.put("BelowRangeEventEnabled", "BelowRangeEventEnabled");
    map.put("BottomOfRange", "BottomOfRange");
    map.put("CharacterEncoding", "CharacterEncoding");
    map.put("Checked", "Checked");
    map.put("ColorChangedEventEnabled", "ColorChangedEventEnabled");
    map.put("Country", "Country");
    map.put("ConsumerKey", "ConsumerKey");
    map.put("ConsumerSecret", "ConsumerSecret");
    map.put("ContactName", "ContactName");
    map.put("CurrentAddress", "CurrentAddress");
    map.put("CurrentPageTitle", "CurrentPageTitle");
    map.put("CurrentUrl", "CurrentUrl");
    map.put("DataType", "DataType");
    map.put("DataUri", "DataUri");
    map.put("DelimiterByte", "DelimiterByte");
    map.put("DetectColor", "DetectColor");
    map.put("DirectMessages", "DirectMessages");
    map.put("DistanceInterval", "DistanceInterval");
    map.put("Elements", "Elements");
    map.put("ElementsFromString", "ElementsFromString");
    map.put("EmailAddress", "EmailAddress");
    map.put("Enabled", "Enabled");
    map.put("ExtraKey", "ExtraKey");
    map.put("ExtraValue", "ExtraValue");
    map.put("Followers", "Followers");
    map.put("FollowLinks", "FollowLinks");
    map.put("FontSize", "FontSize");
    map.put("FriendTimeline", "FriendTimeline");
    map.put("FullScreen", "FullScreen");
    map.put("GameId", "GameId");
    map.put("GenerateColor", "GenerateColor");
    map.put("GenerateLight", "GenerateLight");
    map.put("GoogleVoiceEnabled", "GoogleVoiceEnabled");
    map.put("HasAccuracy", "HasAccuracy");
    map.put("HasAltitude", "HasAltitude");
    map.put("HasLongitudeLatitude", "HasLongitudeLatitude");
    map.put("Heading", "Heading");
    map.put("Height", "Height");
    map.put("HighByteFirst", "HighByteFirst");
    map.put("Hint", "Hint");
    map.put("HomeUrl", "HomeUrl");
    map.put("Image", "Image");
    map.put("InstanceId", "InstanceId");
    map.put("Interval", "Interval");
    map.put("InvitedInstances", "InvitedInstances");
    map.put("IsAccepting", "IsAccepting");
    map.put("IsConnected", "IsConnected");
    map.put("IsLooping", "IsLooping");
    map.put("IsPlaying", "IsPlaying");
    map.put("JoinedInstances", "JoinedInstances");
    map.put("Language", "Language");
    map.put("Latitude", "Latitude");
    map.put("Leader", "Leader");
    map.put("LineWidth", "LineWidth");
    map.put("Longitude", "Longitude");
    map.put("Magnitude", "Magnitude");
    map.put("Mentions", "Mentions");
    map.put("Message", "Message");
    map.put("MinimumInterval", "MinimumInterval");
    map.put("MultiLine", "MultiLine");
    map.put("NumbersOnly", "NumbersOnly");
    map.put("PaintColor", "PaintColor");
    map.put("PhoneNumber", "PhoneNumber");
    map.put("Picture", "Picture");
    map.put("Pitch", "Pitch");
    map.put("Players", "Players");
    map.put("PressedEventEnabled", "PressedEventEnabled");
    map.put("PromptforPermission", "PromptforPermission");
    map.put("ProviderLocked", "ProviderLocked");
    map.put("ProviderName", "ProviderName");
    map.put("PublicInstances", "PublicInstances");
    map.put("Query", "Query");
    map.put("Radius", "Radius");
    map.put("ReceivingEnabled", "ReceivingEnabled");
    map.put("ReleasedEventEnabled", "ReleasedEventEnabled");
    map.put("RequestHeaders", "RequestHeaders");
    map.put("ResponseFileName", "ResponseFileName");
    map.put("Result", "Result");
    map.put("ResultName", "ResultName");
    map.put("ResultType", "ResultType");
    map.put("ResultUri", "ResultUri");
    map.put("Roll", "Roll");
    map.put("Rotates", "Rotates");
    map.put("SaveResponse", "SaveResponse");
    map.put("ScreenOrientation", "ScreenOrientation");
    map.put("Scrollable", "Scrollable");
    map.put("SearchResults", "SearchResults");
    map.put("Secure", "Secure");
    map.put("ServiceUrl", "ServiceUrl");
    map.put("ServiceURL", "ServiceURL");
    map.put("Selection", "Selection");
    map.put("SelectionIndex", "SelectionIndex");
    map.put("Source", "Source");
    map.put("Speed", "Speed");
    map.put("StopBeforeDisconnect", "StopBeforeDisconnect");
    map.put("Text", "Text");
    map.put("TextColor", "TextColor");
    map.put("TimeInterval", "TimeInterval");
    map.put("TimerAlwaysFires", "TimerAlwaysFires");
    map.put("TimerEnabled", "TimerEnabled");
    map.put("TimerInterval", "TimerInterval");
    map.put("Title", "Title");
    map.put("TopOfRange", "TopOfRange");
    map.put("Url", "Url");
    map.put("UserChoice", "UserChoice");
    map.put("UserEmailAddress", "UserEmailAddress");
    map.put("UserId", "UserId");
    map.put("Username", "Username");
    map.put("Visible", "Visible");
    map.put("Volume", "Volume");
    map.put("WithinRangeEventEnabled", "WithinRangeEventEnabled");
    map.put("X", "X");
    map.put("XAccel", "XAccel");
    map.put("Y", "Y");
    map.put("YAccel", "YAccel");
    map.put("Z", "Z");
    map.put("ZAccel", "ZAccel");
    map.put("Width", "Width");
    return map;
  }

  /**
   * Get a Chinese translation map.
   * 
   * The output map has the following format: Map = {propertyKey1:
   * {propertyKey1, propertyValue1}, ...}
   * 
   * @return map
   */
  public static HashMap<String, String> chineseMap() {
    HashMap<String, String> map = new HashMap<String, String>();

    map.put("AboveRangeEventEnabled", "上述範圍事件已啟用");
    map.put("Action", "行動");
    map.put("ActivityClass", "活動類");
    map.put("ActivityPackage", "活動套餐");
    map.put("Accuracy", "準確性");
    map.put("AddressesAndNames", "地址和名稱");
    map.put("AlignHorizontal", "水平對齊");
    map.put("AlignVertical", "垂直對齊");
    map.put("AllowCookies", "允許Cookies");
    map.put("Altitude", "海拔");
    map.put("Angle", "角");
    map.put("Animation", "動畫");
    map.put("ApiKey", "API密鑰");
    map.put("Available", "可用的");
    map.put("AvailableProviders", "可用提供商");
    map.put("Azimuth", "方位角");
    map.put("BackgroundColor", "背景顏色");
    map.put("BackgroundImage", "背景圖片");
    map.put("BallotOptions", "選票選項");
    map.put("BallotQuestion", "選票問題");
    map.put("BelowRangeEventEnabled", "下面範圍事件啟用");
    map.put("BottomOfRange", "範圍的底部");
    map.put("CharacterEncoding", "字符編碼");
    map.put("Checked", "經過");
    map.put("ColorChangedEventEnabled", "顏色改變事件已啟用");
    map.put("ConsumerKey", "消費者的關鍵");
    map.put("ConsumerSecret", "消費者揭秘");
    map.put("ContactName", "站點名稱");
    map.put("Country", "國家");
    map.put("CurrentAddress", "當前地址");
    map.put("CurrentPageTitle", "當前頁標題");
    map.put("CurrentUrl", "當前URL");
    map.put("DataType", "數據類型");
    map.put("DataUri", "數據烏里");
    map.put("DelimiterByte", "分隔字節");
    map.put("DetectColor", "檢測顏色");
    map.put("DirectMessages", "直接消息");
    map.put("DistanceInterval", "距離間隔");
    map.put("Elements", "分子");
    map.put("ElementsFromString", "從字符串的元素");
    map.put("EmailAddress", "電子郵件地址");
    map.put("Enabled", "啟用");
    map.put("ExtraKey", "額外的關鍵");
    map.put("ExtraValue", "額外的價值");
    map.put("Followers", "追隨者");
    map.put("FollowLinks", "按照鏈接");
    map.put("FontSize", "字體大小");
    map.put("FriendTimeline", "好友時間軸");
    map.put("FullScreen", "全屏");
    map.put("GameId", "遊戲ID");
    map.put("GenerateColor", "生成顏色");
    map.put("GenerateLight", "產生光");
    map.put("GoogleVoiceEnabled", "谷歌語音啟用");
    map.put("HasAccuracy", "具有精度");
    map.put("HasAltitude", "有海拔");
    map.put("HasLongitudeLatitude", "有經度緯度");
    map.put("Heading", "標題");
    map.put("Height", "高度");
    map.put("HighByteFirst", "首先是高字節");
    map.put("Hint", "暗示");
    map.put("HomeUrl", "首頁網址");
    map.put("Image", "首頁網址");
    map.put("InstanceId", "實例ID");
    map.put("Interval", "間隔");
    map.put("InvitedInstances", "邀請實例");
    map.put("IsAccepting", "正在接受");
    map.put("IsConnected", "連接");
    map.put("IsLooping", "是循環");
    map.put("IsPlaying", "正在播放");
    map.put("JoinedInstances", "註冊實例");
    map.put("Language", "語");
    map.put("Latitude", "緯度");
    map.put("Leader", "領導者");
    map.put("LineWidth", "線寬");
    map.put("Longitude", "經度");
    map.put("Magnitude", "大小");
    map.put("Mentions", "提到");
    map.put("Message", "信息");
    map.put("MinimumInterval", "最小間隔");
    map.put("MultiLine", "多行");
    map.put("NumbersOnly", "僅數字");
    map.put("PaintColor", "塗料顏色");
    map.put("PhoneNumber", "電話號碼");
    map.put("Picture", "圖片");
    map.put("Pitch", "瀝青");
    map.put("Players", "玩家");
    map.put("PressedEventEnabled", "按下事件已啟用");
    map.put("PromptforPermission", "提示權限");
    map.put("ProviderLocked", "供應商鎖定");
    map.put("ProviderName", "供應商名稱");
    map.put("PublicInstances", "公共實例");
    map.put("Query", "詢問");
    map.put("Radius", "半徑");
    map.put("ReceivingEnabled", "接收啟用");
    map.put("ReleasedEventEnabled", "發布事件已啟用");
    map.put("RequestHeaders", "請求頭");
    map.put("ResponseFileName", "響應文件名");
    map.put("Result", "導致");
    map.put("ResultName", "結果名稱");
    map.put("ResultType", "結果類型");
    map.put("ResultUri", "結果URI");
    map.put("Roll", "滾");
    map.put("Rotates", "旋轉");
    map.put("SaveResponse", "保存響應");
    map.put("ScreenOrientation", "屏幕方向");
    map.put("Scrollable", "可滾動");
    map.put("SearchResults", "搜索結果");
    map.put("Secure", "安全");
    map.put("Selection", "選擇");
    map.put("SelectionIndex", "選擇指數");
    map.put("ServiceUrl", "服務URL");
    map.put("ServiceURL", "服務URL");
    map.put("Source", "源");
    map.put("Speed", "速度");
    map.put("StopBeforeDisconnect", "斷開前停止");
    map.put("Text", "文本");
    map.put("TextColor", "文字顏色");
    map.put("TimeInterval", "時間間隔");
    map.put("TimerAlwaysFires", "定時器始終閃光");
    map.put("TimerEnabled", "啟用定時器");
    map.put("TimerInterval", "定時器的時間間隔");
    map.put("Title", "標題");
    map.put("TopOfRange", "頂部的範圍");
    map.put("Url", "網址");
    map.put("UserChoice", "用戶選擇");
    map.put("UserEmailAddress", "用戶電子郵件地址");
    map.put("UserId", "用戶ID");
    map.put("Username", "用戶名");
    map.put("Visible", "可見");
    map.put("Volume", "量");
    map.put("WithinRangeEventEnabled", "範圍內事件啟用");
    map.put("X", "X");
    map.put("XAccel", "XAccel");
    map.put("Y", "Y");
    map.put("YAccel", "YAccel");
    map.put("Z", "Z");
    map.put("ZAccel", "ZAccel");
    map.put("Width", "寬度");

    return map;
  }
}