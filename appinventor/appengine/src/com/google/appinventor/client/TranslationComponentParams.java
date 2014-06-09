package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationComponentParams {

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
    if (!myMap.containsKey(key)) {
      OdeLog.log("Param does not contain key " + key);
      return key;
    }
    return myMap.get(key);
  }
  
  public static String getMapJSON(String paramName) {
    return "{ \"key\": \"" + paramName + "\", \"value\": \"" + getName(paramName) + "\"}";
  }
  
  /**
   * Get JSON representation of the translation map
   * 
   * @param params
   *          A list of parameter names
   */
  public static String getMapJSON(List<String> params) {
    OdeLog.log("Current language setting: " + languageSetting);
    StringBuilder sb = new StringBuilder();

    sb.append('[');
    String separator = "";
    for (String paramName : params) {
      sb.append(separator);
      sb.append(getMapJSON(paramName));
      separator = ",\n";
    }

    sb.append(']');
    System.err.println(sb.toString());
    OdeLog.log("Param JSON: " + sb.toString());
    return sb.toString();
  }

  /**
   * Get an English translation map.
   * 
   * The output map has the following format: 
   *    Map = [{eventName1: String1}, ...]
   * 
   * @return map
   */
  public static HashMap<String, String> englishMap() { 
    HashMap<String, String> map = new HashMap<String, String>();
    
    // Paramaters
    map.put("xAccel", "xAccel");
    map.put("yAccel", "yAccel");
    map.put("zAccel", "zAccel");
    map.put("result", "result");
    map.put("other", "other");
    map.put("component", "component");
    map.put("startX", "startX");
    map.put("startY", "startY");
    map.put("prevX", "prevX");
    map.put("prevY", "prevY");

    map.put("currentX", "currentX");
    map.put("currentY", "currentY");
    map.put("edge", "edge");
    map.put("speed", "speed");
    map.put("heading", "heading");
    map.put("xvel", "xvel");
    map.put("yvel", "yvel");
    map.put("target", "target");
    
    map.put("address", "address");
    map.put("uuid", "uuid");
    map.put("numberOfBytes", "numberOfBytes");
    map.put("number", "number");
    map.put("list", "list");
    map.put("text", "text");
    map.put("clip", "clip");
    map.put("image", "image");
    map.put("draggedSprite", "draggedSprite");
    map.put("flungSprite", "flungSprite");
    map.put("touchedSprite", "touchedSprite");
    
    map.put("x", "x");
    map.put("y", "y");
    map.put("r", "r");
    map.put("x1", "x1");
    map.put("x2", "x2");
    map.put("y1", "y1");
    map.put("y2", "y2");
    map.put("angle", "angle");
    map.put("fileName", "fileName");
    map.put("color", "color");
    map.put("instant", "instant");
    map.put("days", "days");
    map.put("hours", "hours");
    map.put("minutes", "minutes");
    map.put("months", "months");
    map.put("seconds", "seconds");
   
    map.put("weeks", "weeks");
    map.put("years", "years");
    map.put("InstantInTime", "InstantInTime");
    map.put("from", "from");
    map.put("millis", "millis");
    map.put("functionName", "functionName");
    map.put("errorNumber", "errorNumber");
    map.put("message", "message");
    map.put("otherScreenName", "otherScreenName");
    map.put("animType", "animType");
    map.put("sender", "sender");
    map.put("contents", "contents");
    map.put("instanceId", "instanceId");
    map.put("playerId", "playerId");
    map.put("command", "command");
    map.put("arguments", "arguments");
    map.put("response", "response");
    map.put("emailAddress", "emailAddress");
    map.put("type", "type");
    map.put("count", "count");
    
    map.put("makePublic", "makePublic");
    map.put("recipients", "recipients");
    map.put("arguments", "arguments");
    map.put("playerEmail", "playerEmail");
    map.put("latitude", "latitude");
    map.put("longitude", "longitude");
    map.put("altitude", "altitude");
    map.put("provider", "provider");
    map.put("status", "status");
    map.put("locationName", "locationName");
    map.put("choice", "choice");
    map.put("response", "response");
    map.put("notice", "notice");
    map.put("title", "title");
    map.put("buttonText", "buttonText");
    map.put("cancelable", "cancelable");

    map.put("button1Text", "button1Text");
    map.put("button2Text", "button2Text");
    map.put("source", "source");
    map.put("destination", "destination");
    map.put("sensorPortLetter", "sensorPortLetter");
    map.put("rxDataLength", "rxDataLength");
    map.put("wildcard", "wildcard");
    map.put("motorPortLetter", "motorPortLetter");
    map.put("mailbox", "mailbox");
    map.put("durationMs", "durationMs");
    map.put("relative", "relative");
    map.put("sensorType", "sensorType");
    map.put("sensorMode", "sensorMode");
    map.put("power", "power");
    map.put("mode", "mode");
    map.put("regulationMode", "regulationMode");
    map.put("turnRatio", "turnRatio");
    map.put("runState", "runState");
    
    map.put("tachoLimit", "tachoLimit");
    map.put("programName", "programName");
    map.put("distance", "distance");
    map.put("azimuth", "azimuth");
    map.put("pitch", "pitch");
    map.put("roll", "roll");
    map.put("simpleSteps", "simpleSteps");
    map.put("walkSteps", "walkSteps");
    map.put("seed", "seed");
    map.put("millisecs", "millisecs");
    map.put("sound", "sound");
    map.put("messageText", "messageText");
    map.put("tag", "tag");
    map.put("valueToStore", "valueToStore");
    map.put("tagFromWebDB", "tagFromWebDB");
    map.put("valueFromWebDB", "valueFromWebDB");
    map.put("followers2", "followers2");
    map.put("timeline", "timeline");
    
    map.put("mentions", "mentions");
    map.put("searchResults", "searchResults");
    map.put("user", "user");
    map.put("url", "url");
    map.put("responseCode", "responseCode");
    map.put("responseType", "responseType");
    map.put("responseContent", "responseContent");
    map.put("htmlText", "htmlText");
    map.put("jsonText", "jsonText");
    map.put("path", "path");
    map.put("encoding", "encoding");
    map.put("name", "name");
    map.put("serviceName", "serviceName");
    map.put("milliseconds", "milliseconds");
    map.put("messages", "messages");
    map.put("start", "start");
    map.put("end", "end");    
    map.put("frequencyHz", "frequencyHz");
    map.put("secure", "secure");  
    map.put("file", "file");
    map.put("thumbPosition", "thumbPosition");
    map.put("selection", "selection");
    map.put("valueIfTagNotThere", "valueIfTagNotThere");
    map.put("query", "query");
    map.put("ImagePath", "ImagePath");
    map.put("ms", "ms");
    map.put("translation", "translation");
    map.put("languageToTranslateTo", "languageToTranslateTo");
    map.put("textToTranslate", "textToTranslate");
    
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
  public static Map<String, String> chineseMap() {
    HashMap<String, String> map = new HashMap<String, String>();
    
    // Parameter map
    map.put("xAccel", "X加速");
    map.put("yAccel", "Y加速");
    map.put("zAccel", "Z加速");
    map.put("result", "导致");
    map.put("other", "其他");
    map.put("component", "元件");
    map.put("startX", "启动X");
    map.put("startY", "启动Y");
    map.put("prevX", "以前的X");
    map.put("prevY", "以前的Y");

    map.put("currentX", "当前的X");
    map.put("currentY", "当前的Y");
    map.put("edge", "边缘");
    map.put("speed", "速度");
    map.put("heading", "标题");
    map.put("xvel", "X速度");
    map.put("yvel", "Y速度");
    map.put("target", "目标");
    
    map.put("address", "地址");
    map.put("uuid", "UUID");
    map.put("numberOfBytes", "字节数");
    map.put("number", "数");
    map.put("list", "表");
    map.put("text", "文本");
    map.put("clip", "夹");
    map.put("image", "图像");
    map.put("draggedSprite", "拖雪碧");
    map.put("flungSprite", "偏远雪碧");
    map.put("touchedSprite", "感动雪碧");
    
    map.put("x", "x");
    map.put("y", "y");
    map.put("r", "r");
    map.put("x1", "x1");
    map.put("x2", "x2");
    map.put("y1", "y1");
    map.put("y2", "y2");
    map.put("angle", "角");
    map.put("fileName", "文件名称");
    map.put("color", "颜色");
    map.put("instant", "瞬间");
    map.put("days", "天");
    map.put("hours", "小时");
    map.put("minutes", "分钟");
    map.put("months", "个月");
    map.put("seconds", "秒");
   
    map.put("weeks", "週");
    map.put("years", "岁月");
    map.put("InstantInTime", "瞬间时间");
    map.put("from", "从");
    map.put("millis", "米利斯");
    map.put("functionName", "功能名称");
    map.put("errorNumber", "错误号");
    map.put("message", "信息");
    map.put("otherScreenName", "其他屏幕名称");
    map.put("animType", "动画类型");
    map.put("sender", "寄件人");
    map.put("contents", "内容");
    map.put("instanceId", "实例ID");
    map.put("playerId", "玩家ID");
    map.put("command", "命令");
    map.put("arguments", "参数");
    map.put("response", "响应");
    map.put("emailAddress", "电子邮件地址");
    map.put("type", "类型");
    map.put("count", "算");
    
    map.put("makePublic", "公开");
    map.put("recipients", "受助人");
    map.put("playerEmail", "player电邮");
    map.put("latitude", "纬度");
    map.put("longitude", "经度");
    map.put("altitude", "海拔");
    map.put("provider", "提供者");
    map.put("status", "状态");
    map.put("locationName", "所在地名称");
    map.put("choice", "精选");
    map.put("response", "响应");
    map.put("notice", "注意");
    map.put("title", "标题");
    map.put("buttonText", "按钮上的文字");
    map.put("cancelable", "撤销");

    map.put("button1Text", "按钮1文本");
    map.put("button2Text", "按钮2文本");
    map.put("source", "源");
    map.put("destination", "目的地");
    map.put("sensorPortLetter", "传感器港信");
    map.put("rxDataLength", "RX数据长度");
    map.put("wildcard", "通配符");
    map.put("motorPortLetter", "马达港信");
    map.put("mailbox", "邮箱");
    map.put("durationMs", "持续时间毫秒");
    map.put("relative", "相对的");
    map.put("sensorType", "传感器类型");
    map.put("sensorMode", "传感器模式");
    map.put("power", "功率");
    map.put("mode", "模式");
    map.put("regulationMode", "规制模式");
    map.put("turnRatio", "转比例");
    map.put("runState", "运行状态");
    
    map.put("tachoLimit", "测速限制");
    map.put("programName", "程序名称");
    map.put("distance", "距离");
    map.put("azimuth", "方位角");
    map.put("pitch", "沥青");
    map.put("roll", "滚");
    map.put("simpleSteps", "简单的步骤");
    map.put("walkSteps", "走路步骤");
    map.put("seed", "种子");
    map.put("millisecs", "毫秒数");
    map.put("sound", "声音");
    map.put("messageText", "消息文本");
    map.put("tag", "标籤");
    map.put("valueToStore", "值存储");
    map.put("tagFromWebDB", "从WebDB的标籤");
    map.put("valueFromWebDB", "从WebDB值");
    map.put("followers2", "追随者2");
    map.put("timeline", "时间表");
    
    map.put("mentions", "提到");
    map.put("searchResults", "搜索结果");
    map.put("user", "用户");
    map.put("url", "网址");
    map.put("responseCode", "响应代码");
    map.put("responseType", "响应类型");
    map.put("responseContent", "回应内容");
    map.put("htmlText", "HTML文本");
    map.put("jsonText", "JSON文本");
    map.put("path", "路径");
    map.put("encoding", "编码");
    map.put("name", "名");
    map.put("serviceName", "服务名称");
    map.put("milliseconds", "毫秒");
    map.put("messages", "讯息");
    
    map.put("start", "开始");
    map.put("end", "结束");    
    map.put("frequencyHz", "频率Hz");
    map.put("secure", "安全");  
    map.put("file", "文件");
    map.put("thumbPosition", "大拇指位置");
    map.put("selection", "选择");
    map.put("valueIfTagNotThere", "价值如果标签不在");
    map.put("query", "搜索请求");
    map.put("ImagePath", "照片路线");
    map.put("ms", "毫秒");
    map.put("translation", "翻译");
    map.put("languageToTranslateTo", "翻译为的语言");
    map.put("textToTranslate", "翻译的文字");

    return map;
  }
}