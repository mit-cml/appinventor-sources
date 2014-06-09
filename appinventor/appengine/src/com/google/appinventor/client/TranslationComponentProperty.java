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
    map.put("ShowFilterBar", "ShowFilterBar");
    map.put("NotifierLength", "NotifierLength");
    map.put("Loop", "Loop");
    map.put("Pitch", "Pitch");
    map.put("SpeechRate", "SpeechRate");
    map.put("Sensitivity", "Sensitivity");
    map.put("TwitPic_API_Key", "TwitPic_API_Key");
    map.put("Prompt", "Prompt");
    map.put("ColorLeft", "ColorLeft");
    map.put("ColorRight", "ColorRight");
    map.put("MaxValue", "MaxValue");
    map.put("MinValue", "MinValue");
    map.put("ThumbPosition", "ThumbPosition");
    map.put("FontBold", "FontBold");
    map.put("FontItalic", "FontItalic");
    map.put("ShowFeedback", "ShowFeedback");
    map.put("WebViewString", "WebViewString");
    map.put("UseFront", "UseFront");
    map.put("Day", "Day");
    map.put("Month", "Month");
    map.put("MonthInText", "MonthInText");
    map.put("Year", "Year");
    map.put("AboutScreen", "AboutScreen");
    map.put("CloseScreenAnimation", "CloseScreenAnimation");
    map.put("OpenScreenAnimation", "OpenScreenAnimation");
    map.put("LastMessage", "LastMessage");
    map.put("ReadMode", "ReadMode");
    map.put("TextToWrite", "TextToWrite");
    map.put("WriteType", "WriteType");
    map.put("CalibrateStrideLength", "CalibrateStrideLength");
    map.put("Distance", "Distance");
    map.put("ElapsedTime", "ElapsedTime");
    map.put("Moving", "Moving");
    map.put("StopDetectionTimeout", "StopDetectionTimeout");
    map.put("StrideLength", "StrideLength");
    map.put("UseGPS", "UseGPS");
    map.put("Hour", "Hour");
    map.put("Minute", "Minute");
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

    map.put("AboveRangeEventEnabled", "上述范围事件已启用");
    map.put("Action", "行动");
    map.put("ActivityClass", "活动类");
    map.put("ActivityPackage", "活动套餐");
    map.put("Accuracy", "准确性");
    map.put("AddressesAndNames", "地址和名称");
    map.put("AlignHorizontal", "水平对齐");
    map.put("AlignVertical", "垂直对齐");
    map.put("AllowCookies", "允许Cookies");
    map.put("Altitude", "海拔");
    map.put("Angle", "角");
    map.put("Animation", "动画");
    map.put("ApiKey", "API密钥");
    map.put("Available", "可用的");
    map.put("AvailableProviders", "可用提供商");
    map.put("Azimuth", "方位角");
    map.put("BackgroundColor", "背景颜色");
    map.put("BackgroundImage", "背景图片");
    map.put("BallotOptions", "选票选项");
    map.put("BallotQuestion", "选票问题");
    map.put("BelowRangeEventEnabled", "下面范围事件启用");
    map.put("BottomOfRange", "范围的底部");
    map.put("CharacterEncoding", "字符编码");
    map.put("Checked", "经过");
    map.put("ColorChangedEventEnabled", "颜色改变事件已启用");
    map.put("ConsumerKey", "消费者的关键");
    map.put("ConsumerSecret", "消费者揭秘");
    map.put("ContactName", "站点名称");
    map.put("Country", "国家");
    map.put("CurrentAddress", "当前地址");
    map.put("CurrentPageTitle", "当前页标题");
    map.put("CurrentUrl", "当前URL");
    map.put("DataType", "数据类型");
    map.put("DataUri", "数据乌里");
    map.put("DelimiterByte", "分隔字节");
    map.put("DetectColor", "检测颜色");
    map.put("DirectMessages", "直接消息");
    map.put("DistanceInterval", "距离间隔");
    map.put("Elements", "分子");
    map.put("ElementsFromString", "从字符串的元素");
    map.put("EmailAddress", "电子邮件地址");
    map.put("Enabled", "启用");
    map.put("ExtraKey", "额外的关键");
    map.put("ExtraValue", "额外的价值");
    map.put("Followers", "追随者");
    map.put("FollowLinks", "按照链接");
    map.put("FontSize", "字体大小");
    map.put("FriendTimeline", "好友时间轴");
    map.put("FullScreen", "全屏");
    map.put("GameId", "游戏ID");
    map.put("GenerateColor", "生成颜色");
    map.put("GenerateLight", "产生光");
    map.put("GoogleVoiceEnabled", "谷歌语音启用");
    map.put("HasAccuracy", "具有精度");
    map.put("HasAltitude", "有海拔");
    map.put("HasLongitudeLatitude", "有经度纬度");
    map.put("Heading", "标题");
    map.put("Height", "高度");
    map.put("HighByteFirst", "首先是高字节");
    map.put("Hint", "暗示");
    map.put("HomeUrl", "首页网址");
    map.put("Image", "首页网址");
    map.put("InstanceId", "实例ID");
    map.put("Interval", "间隔");
    map.put("InvitedInstances", "邀请实例");
    map.put("IsAccepting", "正在接受");
    map.put("IsConnected", "连接");
    map.put("IsLooping", "是循环");
    map.put("IsPlaying", "正在播放");
    map.put("JoinedInstances", "注册实例");
    map.put("Language", "语");
    map.put("Latitude", "纬度");
    map.put("Leader", "领导者");
    map.put("LineWidth", "线宽");
    map.put("Longitude", "经度");
    map.put("Magnitude", "大小");
    map.put("Mentions", "提到");
    map.put("Message", "信息");
    map.put("MinimumInterval", "最小间隔");
    map.put("MultiLine", "多行");
    map.put("NumbersOnly", "仅数字");
    map.put("PaintColor", "涂料颜色");
    map.put("PhoneNumber", "电话号码");
    map.put("Picture", "图片");
    map.put("Pitch", "沥青");
    map.put("Players", "玩家");
    map.put("PressedEventEnabled", "按下事件已启用");
    map.put("PromptforPermission", "提示权限");
    map.put("ProviderLocked", "供应商锁定");
    map.put("ProviderName", "供应商名称");
    map.put("PublicInstances", "公共实例");
    map.put("Query", "询问");
    map.put("Radius", "半径");
    map.put("ReceivingEnabled", "接收启用");
    map.put("ReleasedEventEnabled", "发布事件已启用");
    map.put("RequestHeaders", "请求头");
    map.put("ResponseFileName", "响应文件名");
    map.put("Result", "导致");
    map.put("ResultName", "结果名称");
    map.put("ResultType", "结果类型");
    map.put("ResultUri", "结果URI");
    map.put("Roll", "滚");
    map.put("Rotates", "旋转");
    map.put("SaveResponse", "保存响应");
    map.put("ScreenOrientation", "屏幕方向");
    map.put("Scrollable", "可滚动");
    map.put("SearchResults", "搜索结果");
    map.put("Secure", "安全");
    map.put("Selection", "选择");
    map.put("SelectionIndex", "选择指数");
    map.put("ServiceUrl", "服务URL");
    map.put("ServiceURL", "服务URL");
    map.put("Source", "源");
    map.put("Speed", "速度");
    map.put("StopBeforeDisconnect", "断开前停止");
    map.put("Text", "文本");
    map.put("TextColor", "文字颜色");
    map.put("TimeInterval", "时间间隔");
    map.put("TimerAlwaysFires", "定时器始终闪光");
    map.put("TimerEnabled", "启用定时器");
    map.put("TimerInterval", "定时器的时间间隔");
    map.put("Title", "标题");
    map.put("TopOfRange", "顶部的范围");
    map.put("Url", "网址");
    map.put("UserChoice", "用户选择");
    map.put("UserEmailAddress", "用户电子邮件地址");
    map.put("UserId", "用户ID");
    map.put("Username", "用户名");
    map.put("Visible", "可见");
    map.put("Volume", "量");
    map.put("WithinRangeEventEnabled", "范围内事件启用");
    map.put("X", "X");
    map.put("XAccel", "X加速");
    map.put("Y", "Y");
    map.put("YAccel", "Y加速");
    map.put("Z", "Z");
    map.put("ZAccel", "Z加速");
    map.put("Width", "宽度");
    map.put("ShowFilterBar", "加过滤条");
    map.put("NotifierLength", "通知器长度");
    map.put("Loop", "环");
    map.put("Pitch", "音调");
    map.put("SpeechRate", "语速");
    map.put("Sensitivity", "敏感度");
    map.put("TwitPic_API_Key", "TwitPic_API_Key");
    map.put("Prompt", "提示");
    map.put("ColorLeft", "左方颜色");
    map.put("ColorRight", "右方颜色");
    map.put("MaxValue", "最大数");
    map.put("MinValue", "最小数");
    map.put("ThumbPosition", "大拇指位置");
    map.put("FontBold", "粗体字");
    map.put("FontItalic", "斜体字");
    map.put("ShowFeedback", "显示回馈");
    map.put("WebViewString", "网站试图穿");
    map.put("UseFront", "用前面");
    map.put("Day", "日");
    map.put("Month", "月");
    map.put("MonthInText", "字的月份");
    map.put("Year", "年");
    map.put("AboutScreen", "关于屏幕");
    map.put("CloseScreenAnimation", "关闭屏幕动画");
    map.put("OpenScreenAnimation", "打开屏幕动画");
    map.put("LastMessage", "最后的信");
    map.put("ReadMode", "可读状态");
    map.put("TextToWrite", "字到写");
    map.put("WriteType", "写得");
    map.put("CalibrateStrideLength", "校准步子长度");
    map.put("Distance", "距离");
    map.put("ElapsedTime", "消逝的时间");
    map.put("Moving", "动的");
    map.put("StopDetectionTimeout", "停止检测暂停");
    map.put("StrideLength", "步子长度");
    map.put("UseGPS", "用GPS");
    map.put("Hour", "小时");
    map.put("Minute", "分钟");
    return map;
  }
}