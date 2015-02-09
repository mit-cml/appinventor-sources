// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.output.OdeLog;

import java.util.HashMap;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public class TranslationComponentMethods {

  public static String languageSetting;
  public static Map<String, String> myMap = map();

  public static enum Language {
    zh_CN, en_US
  }

  public static String getName(String key) {
    if (!myMap.containsKey(key)) {
      OdeLog.log("Method map does not contain key " + key);
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
    // TODO: Some methods may have different descriptions depending on which components
    // they are associated to; need to change the event name to be component type specific
    HashMap<String, String> map = new HashMap<String, String>();

    // Methods
    map.put("ResolveActivity", MESSAGES.ResolveActivityMethods());
    map.put("StartActivity", MESSAGES.StartActivityMethods());
    map.put("Bounce", MESSAGES.BounceMethods());
    map.put("CollidingWith", MESSAGES.CollidingWithMethods());
    map.put("MoveIntoBounds", MESSAGES.MoveIntoBoundsMethods());
    map.put("MoveTo", MESSAGES.MoveToMethods());
    map.put("PointInDirection", MESSAGES.PointInDirectionMethods());
    map.put("PointTowards", MESSAGES.PointTowardsMethods());
    map.put("BytesAvailableToReceive", MESSAGES.BytesAvailableToReceiveMethods());
    map.put("Connect", MESSAGES.ConnectMethods());
    map.put("ConnectWithUUID", MESSAGES.ConnectWithUUIDMethods());
    map.put("Disconnect", MESSAGES.DisconnectMethods());
    map.put("IsDevicePaired", MESSAGES.IsDevicePairedMethods());
    map.put("ReceiveSigned1ByteNumber", MESSAGES.ReceiveSigned1ByteNumberMethods());
    map.put("ReceiveSigned2ByteNumber", MESSAGES.ReceiveSigned2ByteNumberMethods());
    map.put("ReceiveSigned4ByteNumber", MESSAGES.ReceiveSigned4ByteNumberMethods());
    map.put("ReceiveSignedBytes", MESSAGES.ReceiveSignedBytesMethods());
    map.put("ReceiveText", MESSAGES.ReceiveTextMethods());
    map.put("ReceiveUnsigned1ByteNumber", MESSAGES.ReceiveUnsigned1ByteNumberMethods());
    map.put("ReceiveUnsigned2ByteNumber", MESSAGES.ReceiveUnsigned2ByteNumberMethods());
    map.put("ReceiveUnsigned4ByteNumber", MESSAGES.ReceiveUnsigned4ByteNumberMethods());
    map.put("ReceiveUnsignedBytes", MESSAGES.ReceiveUnsignedBytesMethods());
    map.put("Send1ByteNumber", MESSAGES.Send1ByteNumberMethods());
    map.put("Send2ByteNumber", MESSAGES.Send2ByteNumberMethods());
    map.put("Send4ByteNumber", MESSAGES.Send4ByteNumberMethods());
    map.put("SendBytes", MESSAGES.SendBytesMethods());
    map.put("SendText", MESSAGES.SendTextMethods());
    map.put("AcceptConnection", MESSAGES.AcceptConnectionMethods());
    map.put("AcceptConnectionWithUUID", MESSAGES.AcceptConnectionWithUUIDMethods());
    map.put("BytesAvailableToReceive", MESSAGES.BytesAvailableToReceiveMethods());
    map.put("StopAccepting", MESSAGES.StopAcceptingMethods());
    map.put("RecordVideo", MESSAGES.RecordVideoMethods());
    map.put("TakePicture", MESSAGES.TakePictureMethods());
    map.put("Clear", MESSAGES.ClearMethods());
    map.put("DrawCircle", MESSAGES.DrawCircleMethods());
    map.put("DrawLine", MESSAGES.DrawLineMethods());
    map.put("DrawPoint", MESSAGES.DrawPointMethods());
    map.put("DrawText", MESSAGES.DrawTextMethods());
    map.put("DrawTextAtAngle", MESSAGES.DrawTextAtAngleMethods());
    map.put("GetBackgroundPixelColor", MESSAGES.GetBackgroundPixelColorMethods());
    map.put("GetPixelColor", MESSAGES.GetPixelColorMethods());
    map.put("Save", MESSAGES.SaveMethods());
    map.put("SaveAs", MESSAGES.SaveAsMethods());
    map.put("SetBackgroundPixelColor", MESSAGES.SetBackgroundPixelColorMethods());
    map.put("AddDays", MESSAGES.AddDaysMethods());
    map.put("AddHours", MESSAGES.AddHoursMethods());
    map.put("AddMinutes", MESSAGES.AddMinutesMethods());
    map.put("AddMonths", MESSAGES.AddMonthsMethods());
    map.put("AddSeconds", MESSAGES.AddSecondsMethods());
    map.put("AddWeeks", MESSAGES.AddWeeksMethods());
    map.put("AddYears", MESSAGES.AddYearsMethods());
    map.put("DayOfMonth", MESSAGES.DayOfMonthMethods());
    map.put("Duration", MESSAGES.DurationMethods());
    map.put("FormatDate", MESSAGES.FormatDateMethods());
    map.put("FormatDateTime", MESSAGES.FormatDateTimeMethods());
    map.put("FormatTime", MESSAGES.FormatTimeMethods());
    map.put("GetMillis", MESSAGES.GetMillisMethods());
    map.put("Hour", MESSAGES.HourMethods());
    map.put("MakeInstant", MESSAGES.MakeInstantMethods());
    map.put("MakeInstantFromMillis", MESSAGES.MakeInstantFromMillisMethods());
    map.put("Minute", MESSAGES.MinuteMethods());
    map.put("Month", MESSAGES.MonthMethods());
    map.put("MonthName", MESSAGES.MonthNameMethods());
    map.put("Now", MESSAGES.NowMethods());
    map.put("Second", MESSAGES.SecondMethods());
    map.put("SystemTime", MESSAGES.SystemTimeMethods());
    map.put("Weekday", MESSAGES.WeekdayMethods());
    map.put("WeekdayName", MESSAGES.WeekdayNameMethods());
    map.put("Year", MESSAGES.YearMethods());
    map.put("Open", MESSAGES.OpenMethods());
    map.put("CloseScreenAnimation", MESSAGES.CloseScreenAnimationMethods());
    map.put("OpenScreenAnimation", MESSAGES.OpenScreenAnimationMethods());
    map.put("DoQuery", MESSAGES.DoQueryMethods());
    map.put("ForgetLogin", MESSAGES.ForgetLoginMethods());
    map.put("SendQuery", MESSAGES.SendQueryMethods());
    map.put("GetInstanceLists", MESSAGES.GetInstanceListsMethods());
    map.put("GetMessages", MESSAGES.GetMessagesMethods());
    map.put("Invite", MESSAGES.InviteMethods());
    map.put("LeaveInstance", MESSAGES.LeaveInstanceMethods());
    map.put("MakeNewInstance", MESSAGES.MakeNewInstanceMethods());
    map.put("SendMessage", MESSAGES.SendMessageMethods());
    map.put("ServerCommand", MESSAGES.ServerCommandMethods());
    map.put("SetInstance", MESSAGES.SetInstanceMethods());
    map.put("SetLeader", MESSAGES.SetLeaderMethods());
    map.put("Bounce", MESSAGES.BounceMethods());
    map.put("CollidingWith", MESSAGES.CollidingWithMethods());
    map.put("MoveIntoBounds", MESSAGES.MoveIntoBoundsMethods());
    map.put("MoveTo", MESSAGES.MoveToMethods());
    map.put("PointInDirection", MESSAGES.PointInDirectionMethods());
    map.put("PointTowards", MESSAGES.PointTowardsMethods());
    map.put("LatitudeFromAddress", MESSAGES.LatitudeFromAddressMethods());
    map.put("LongitudeFromAddress", MESSAGES.LongitudeFromAddressMethods());
    map.put("LogError", MESSAGES.LogErrorMethods());
    map.put("LogInfo", MESSAGES.LogInfoMethods());
    map.put("LogWarning", MESSAGES.LogWarningMethods());
    map.put("ShowAlert", MESSAGES.ShowAlertMethods());
    map.put("ShowChooseDialog", MESSAGES.ShowChooseDialogMethods());
    map.put("ShowMessageDialog", MESSAGES.ShowMessageDialogMethods());
    map.put("ShowTextDialog", MESSAGES.ShowTextDialogMethods());
    map.put("GetColor", MESSAGES.GetColorMethods());
    map.put("GetLightLevel", MESSAGES.GetLightLevelMethods());
    map.put("DeleteFile", MESSAGES.DeleteFileMethods());
    map.put("DownloadFile", MESSAGES.DownloadFileMethods());
    map.put("GetBatteryLevel", MESSAGES.GetBatteryLevelMethods());
    map.put("GetBrickName", MESSAGES.GetBrickNameMethods());
    map.put("GetCurrentProgramName", MESSAGES.GetCurrentProgramNameMethods());
    map.put("GetFirmwareVersion", MESSAGES.GetFirmwareVersionMethods());
    map.put("GetInputValues", MESSAGES.GetInputValuesMethods());
    map.put("GetOutputState", MESSAGES.GetOutputStateMethods());
    map.put("KeepAlive", MESSAGES.KeepAliveMethods());
    map.put("ListFiles", MESSAGES.ListFilesMethods());
    map.put("LsGetStatus", MESSAGES.LsGetStatusMethods());
    map.put("LsRead", MESSAGES.LsReadMethods());
    map.put("LsWrite", MESSAGES.LsWriteMethods());
    map.put("MessageRead", MESSAGES.MessageReadMethods());
    map.put("MessageWrite", MESSAGES.MessageWriteMethods());
    map.put("PlaySoundFile", MESSAGES.PlaySoundFileMethods());
    map.put("PlayTone", MESSAGES.PlayToneMethods());
    map.put("ResetInputScaledValue", MESSAGES.ResetInputScaledValueMethods());
    map.put("ResetMotorPosition", MESSAGES.ResetMotorPositionMethods());
    map.put("SetBrickName", MESSAGES.SetBrickNameMethods());
    map.put("SetInputMode", MESSAGES.SetInputModeMethods());
    map.put("SetOutputState", MESSAGES.SetOutputStateMethods());
    map.put("StartProgram", MESSAGES.StartProgramMethods());
    map.put("StopProgram", MESSAGES.StopProgramMethods());
    map.put("StopSoundPlayback", MESSAGES.StopSoundPlaybackMethods());
    map.put("LsWrite", MESSAGES.LsWriteMethods());
    map.put("MoveBackward", MESSAGES.MoveBackwardMethods());
    map.put("MoveBackwardIndefinitely", MESSAGES.MoveBackwardIndefinitelyMethods());
    map.put("MoveForward", MESSAGES.MoveForwardMethods());
    map.put("MoveForwardIndefinitely", MESSAGES.MoveForwardIndefinitelyMethods());
    map.put("Stop", MESSAGES.StopMethods());
    map.put("TurnClockwiseIndefinitely", MESSAGES.TurnClockwiseIndefinitelyMethods());
    map.put("TurnCounterClockwiseIndefinitely", MESSAGES.TurnCounterClockwiseIndefinitelyMethods());
    map.put("GetSoundLevel", MESSAGES.GetSoundLevelMethods());
    map.put("IsPressed", MESSAGES.IsPressedMethods());
    map.put("GetDistance", MESSAGES.GetDistanceMethods());
    map.put("Pause", MESSAGES.PauseMethods());
    map.put("Reset", MESSAGES.ResetMethods());
    map.put("Resume", MESSAGES.ResumeMethods());
    map.put("Start", MESSAGES.StartMethods());
    map.put("MakePhoneCall", MESSAGES.MakePhoneCallMethods());
    map.put("GetWifiIpAddress", MESSAGES.GetWifiIpAddressMethods());
    map.put("isConnected", MESSAGES.isConnectedMethods());
    map.put("setHmacSeedReturnCode", MESSAGES.setHmacSeedReturnCodeMethods());
    map.put("startHTTPD", MESSAGES.startHTTPDMethods());
    map.put("Vibrate", MESSAGES.VibrateMethods());
    map.put("GetText", MESSAGES.GetTextMethods());
    map.put("HideKeyboard", MESSAGES.HideKeyboardMethods());
    map.put("Speak", MESSAGES.SpeakMethods());
    map.put("SendMessage", MESSAGES.SendMessageMethods());
    map.put("GetValue", MESSAGES.GetValueMethods());
    map.put("StoreValue", MESSAGES.StoreValueMethods());
    map.put("Authorize", MESSAGES.AuthorizeMethods());
    map.put("CheckAuthorized", MESSAGES.CheckAuthorizedMethods());
    map.put("DeAuthorize", MESSAGES.DeAuthorizeMethods());
    map.put("DirectMessage", MESSAGES.DirectMessageMethods());
    map.put("Follow", MESSAGES.FollowMethods());
    map.put("RequestDirectMessages", MESSAGES.RequestDirectMessagesMethods());
    map.put("RequestFollowers", MESSAGES.RequestFollowersMethods());
    map.put("RequestFriendTimeline", MESSAGES.RequestFriendTimelineMethods());
    map.put("RequestMentions", MESSAGES.RequestMentionsMethods());
    map.put("SearchTwitter", MESSAGES.SearchTwitterMethods());
    map.put("SetStatus", MESSAGES.SetStatusMethods());
    map.put("StopFollowing", MESSAGES.StopFollowingMethods());
    map.put("GetDuration", MESSAGES.GetDurationMethods());
    map.put("SeekTo", MESSAGES.SeekToMethods());
    map.put("DoScan", MESSAGES.DoScanMethods());
    map.put("RequestBallot", MESSAGES.RequestBallotMethods());
    map.put("SendBallot", MESSAGES.SendBallotMethods());
    map.put("BuildPostData", MESSAGES.BuildPostDataMethods());
    map.put("ClearCookies", MESSAGES.ClearCookiesMethods());
    map.put("Get", MESSAGES.GetMethods());
    map.put("RequestFocus", MESSAGES.RequestFocusMethods());
    map.put("HtmlTextDecode", MESSAGES.HtmlTextDecodeMethods());
    map.put("JsonTextDecode", MESSAGES.JsonTextDecodeMethods());
    map.put("XmlTextDecode", MESSAGES.XmlTextDecodeMethods());
    map.put("PostFile", MESSAGES.PostFileMethods());
    map.put("PostText", MESSAGES.PostTextMethods());
    map.put("PostTextWithEncoding", MESSAGES.PostTextWithEncodingMethods());
    map.put("UriEncode", MESSAGES.UriEncodeMethods());
    map.put("CanGoBack", MESSAGES.CanGoBackMethods());
    map.put("CanGoForward", MESSAGES.CanGoForwardMethods());
    map.put("ClearLocations", MESSAGES.ClearLocationsMethods());
    map.put("ClearCaches", MESSAGES.ClearCachesMethods());
    map.put("GoBack", MESSAGES.GoBackMethods());
    map.put("GoForward", MESSAGES.GoForwardMethods());
    map.put("GoHome", MESSAGES.GoHomeMethods());
    map.put("GoToUrl", MESSAGES.GoToUrlMethods());
    map.put("AppendToFile", MESSAGES.AppendToFileMethods());
    map.put("Delete", MESSAGES.DeleteMethods());
    map.put("ReadFrom", MESSAGES.ReadFromMethods());
    map.put("SaveFile", MESSAGES.SaveFileMethods());
    map.put("doFault", MESSAGES.doFaultMethods());
    map.put("getVersionName", MESSAGES.getVersionNameMethods());
    map.put("installURL", MESSAGES.installURLMethods());
    map.put("isDirect", MESSAGES.isDirectMethods());
    map.put("setAssetsLoaded", MESSAGES.setAssetsLoadedMethods());
    map.put("shutdown", MESSAGES.shutdownMethods());
    map.put("ShareFile", MESSAGES.ShareFileMethods());
    map.put("ShareFileWithMessage", MESSAGES.ShareFileWithMessageMethods());
    map.put("ShareMessage", MESSAGES.ShareMessageMethods());
    map.put("Play", MESSAGES.PlayMethods());
    map.put("DisplayDropdown", MESSAGES.DisplayDropdownMethods());
    map.put("ClearAll", MESSAGES.ClearAllMethods());
    map.put("ClearTag", MESSAGES.ClearTagMethods());
    map.put("GetTags", MESSAGES.GetTagsMethods());
    map.put("Tweet", MESSAGES.TweetMethods());
    map.put("TweetWithImage", MESSAGES.TweetWithImageMethods());
    map.put("BuildRequestData", MESSAGES.BuildRequestDataMethods());
    map.put("PutFile", MESSAGES.PutFileMethods());
    map.put("PutText", MESSAGES.PutTextMethods());
    map.put("PutTextWithEncoding", MESSAGES.PutTextWithEncodingMethods());
    map.put("RequestTranslation", MESSAGES.RequestTranslationMethods());
    map.put("InsertRow", MESSAGES.InsertRowMethods());
    map.put("GetRows", MESSAGES.GetRowsMethods());
    map.put("GetRowsWithConditions", MESSAGES.GetRowsWithConditionsMethods());

    return map;
  }
}