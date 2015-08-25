// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import java.util.HashMap;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public class TranslationComponentParams {

  public static String languageSetting;
  public static Map<String, String> myMap = map();

  public static enum Language {
    zh_CN, en_US
  }

  public static String getName(String key) {
    if (!myMap.containsKey(key)) {
      OdeLog.log("Param does not contain key " + key);
      return key;
    }
    return myMap.get(key);
  }

  /**
   * Get a translation map.
   *
   * The output map has the following format:
   * Map = [{eventName1: String1}, ...]
   *
   * @return map
   */
  public static HashMap<String, String> map() {
    HashMap<String, String> map = new HashMap<String, String>();

    // Paramaters
    map.put("xAccel", MESSAGES.xAccelParams());
    map.put("yAccel", MESSAGES.yAccelParams());
    map.put("zAccel", MESSAGES.zAccelParams());
    map.put("result", MESSAGES.resultParams());
    map.put("other", MESSAGES.otherParams());
    map.put("component", MESSAGES.componentParams());
    map.put("startX", MESSAGES.startXParams());
    map.put("startY", MESSAGES.startYParams());
    map.put("prevX", MESSAGES.prevXParams());
    map.put("prevY", MESSAGES.prevYParams());
    map.put("currentX", MESSAGES.currentXParams());
    map.put("currentY", MESSAGES.currentYParams());
    map.put("edge", MESSAGES.edgeParams());
    map.put("speed", MESSAGES.speedParams());
    map.put("heading", MESSAGES.headingParams());
    map.put("xvel", MESSAGES.xvelParams());
    map.put("yvel", MESSAGES.yvelParams());
    map.put("target", MESSAGES.targetParams());
    map.put("address", MESSAGES.addressParams());
    map.put("uuid", MESSAGES.uuidParams());
    map.put("numberOfBytes", MESSAGES.numberOfBytesParams());
    map.put("number", MESSAGES.numberParams());
    map.put("list", MESSAGES.listParams());
    map.put("text", MESSAGES.textParams());
    map.put("clip", MESSAGES.clipParams());
    map.put("image", MESSAGES.imageParams());
    map.put("draggedSprite", MESSAGES.draggedSpriteParams());
    map.put("draggedAnySprite", MESSAGES.draggedAnySpriteParams());
    map.put("flungSprite", MESSAGES.flungSpriteParams());
    map.put("touchedSprite", MESSAGES.touchedSpriteParams());
    map.put("touchedAnySprite", MESSAGES.touchedAnySpriteParams());
    map.put("x", MESSAGES.xParams());
    map.put("y", MESSAGES.yParams());
    map.put("centerX", MESSAGES.centerXParams());
    map.put("centerY", MESSAGES.centerYParams());
    map.put("r", MESSAGES.rParams());
    map.put("radius", MESSAGES.radiusParams());
    map.put("x1", MESSAGES.x1Params());
    map.put("x2", MESSAGES.x2Params());
    map.put("y1", MESSAGES.y1Params());
    map.put("y2", MESSAGES.y2Params());
    map.put("angle", MESSAGES.angleParams());
    map.put("fileName", MESSAGES.fileNameParams());
    map.put("color", MESSAGES.colorParams());
    map.put("instant", MESSAGES.instantParams());
    map.put("days", MESSAGES.daysParams());
    map.put("hours", MESSAGES.hoursParams());
    map.put("minutes", MESSAGES.minutesParams());
    map.put("months", MESSAGES.monthsParams());
    map.put("seconds", MESSAGES.secondsParams());
    map.put("weeks", MESSAGES.weeksParams());
    map.put("years", MESSAGES.yearsParams());
    map.put("InstantInTime", MESSAGES.InstantInTimeParams());
    map.put("from", MESSAGES.fromParams());
    map.put("millis", MESSAGES.millisParams());
    map.put("functionName", MESSAGES.functionNameParams());
    map.put("errorNumber", MESSAGES.errorNumberParams());
    map.put("message", MESSAGES.messageParams());
    map.put("otherScreenName", MESSAGES.otherScreenNameParams());
    map.put("animType", MESSAGES.animTypeParams());
    map.put("sender", MESSAGES.senderParams());
    map.put("contents", MESSAGES.contentsParams());
    map.put("instanceId", MESSAGES.instanceIdParams());
    map.put("playerId", MESSAGES.playerIdParams());
    map.put("command", MESSAGES.commandParams());
    map.put("arguments", MESSAGES.argumentsParams());
    map.put("response", MESSAGES.responseParams());
    map.put("emailAddress", MESSAGES.emailAddressParams());
    map.put("type", MESSAGES.typeParams());
    map.put("count", MESSAGES.countParams());
    map.put("makePublic", MESSAGES.makePublicParams());
    map.put("recipients", MESSAGES.recipientsParams());
    map.put("arguments", MESSAGES.argumentsParams());
    map.put("playerEmail", MESSAGES.playerEmailParams());
    map.put("latitude", MESSAGES.latitudeParams());
    map.put("longitude", MESSAGES.longitudeParams());
    map.put("altitude", MESSAGES.altitudeParams());
    map.put("provider", MESSAGES.providerParams());
    map.put("status", MESSAGES.statusParams());
    map.put("locationName", MESSAGES.locationNameParams());
    map.put("choice", MESSAGES.choiceParams());
    map.put("response", MESSAGES.responseParams());
    map.put("notice", MESSAGES.noticeParams());
    map.put("title", MESSAGES.titleParams());
    map.put("buttonText", MESSAGES.buttonTextParams());
    map.put("cancelable", MESSAGES.cancelableParams());
    map.put("button1Text", MESSAGES.button1TextParams());
    map.put("button2Text", MESSAGES.button2TextParams());
    map.put("source", MESSAGES.sourceParams());
    map.put("destination", MESSAGES.destinationParams());
    map.put("sensorPortLetter", MESSAGES.sensorPortLetterParams());
    map.put("rxDataLength", MESSAGES.rxDataLengthParams());
    map.put("wildcard", MESSAGES.wildcardParams());
    map.put("motorPortLetter", MESSAGES.motorPortLetterParams());
    map.put("mailbox", MESSAGES.mailboxParams());
    map.put("durationMs", MESSAGES.durationMsParams());
    map.put("relative", MESSAGES.relativeParams());
    map.put("sensorType", MESSAGES.sensorTypeParams());
    map.put("sensorMode", MESSAGES.sensorModeParams());
    map.put("power", MESSAGES.powerParams());
    map.put("mode", MESSAGES.modeParams());
    map.put("regulationMode", MESSAGES.regulationModeParams());
    map.put("turnRatio", MESSAGES.turnRatioParams());
    map.put("runState", MESSAGES.runStateParams());
    map.put("tachoLimit", MESSAGES.tachoLimitParams());
    map.put("programName", MESSAGES.programNameParams());
    map.put("distance", MESSAGES.distanceParams());
    map.put("azimuth", MESSAGES.azimuthParams());
    map.put("pitch", MESSAGES.pitchParams());
    map.put("roll", MESSAGES.rollParams());
    map.put("simpleSteps", MESSAGES.simpleStepsParams());
    map.put("walkSteps", MESSAGES.walkStepsParams());
    map.put("seed", MESSAGES.seedParams());
    map.put("millisecs", MESSAGES.millisecsParams());
    map.put("sound", MESSAGES.soundParams());
    map.put("messageText", MESSAGES.messageTextParams());
    map.put("tag", MESSAGES.tagParams());
    map.put("valueToStore", MESSAGES.valueToStoreParams());
    map.put("tagFromWebDB", MESSAGES.tagFromWebDBParams());
    map.put("valueFromWebDB", MESSAGES.valueFromWebDBParams());
    map.put("followers2", MESSAGES.followers2Params());
    map.put("timeline", MESSAGES.timelineParams());
    map.put("mentions", MESSAGES.mentionsParams());
    map.put("searchResults", MESSAGES.searchResultsParams());
    map.put("user", MESSAGES.userParams());
    map.put("url", MESSAGES.urlParams());
    map.put("responseCode", MESSAGES.responseCodeParams());
    map.put("responseType", MESSAGES.responseTypeParams());
    map.put("responseContent", MESSAGES.responseContentParams());
    map.put("htmlText", MESSAGES.htmlTextParams());
    map.put("jsonText", MESSAGES.jsonTextParams());
    map.put("path", MESSAGES.pathParams());
    map.put("encoding", MESSAGES.encodingParams());
    map.put("name", MESSAGES.nameParams());
    map.put("serviceName", MESSAGES.serviceNameParams());
    map.put("milliseconds", MESSAGES.millisecondsParams());
    map.put("messages", MESSAGES.messagesParams());
    map.put("start", MESSAGES.startParams());
    map.put("end", MESSAGES.endParams());
    map.put("frequencyHz", MESSAGES.frequencyHzParams());
    map.put("secure", MESSAGES.secureParams());
    map.put("file", MESSAGES.fileParams());
    map.put("thumbPosition", MESSAGES.thumbPositionParams());
    map.put("selection", MESSAGES.selectionParams());
    map.put("valueIfTagNotThere", MESSAGES.valueIfTagNotThereParams());
    map.put("query", MESSAGES.queryParams());
    map.put("ImagePath", MESSAGES.ImagePathParams());
    map.put("ms", MESSAGES.msParams());
    map.put("translation", MESSAGES.translationParams());
    map.put("languageToTranslateTo", MESSAGES.languageToTranslateToParams());
    map.put("textToTranslate", MESSAGES.textToTranslateParams());
    map.put("uri", MESSAGES.uriParams());
    return map;
  }
}