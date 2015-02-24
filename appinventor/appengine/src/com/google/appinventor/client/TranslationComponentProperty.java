// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.HashMap;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public class TranslationComponentProperty {

  public static String languageSetting;
  public static Map<String, String> myMap = map();

  public static enum Language {
    zh_CN, en_US
  }

  public static String getName(String key) {
    String value = myMap.get(key);
    if (key == null) {
      // This will help implementors debug if the forget to add an entry
      // when defining a new property
      return "**Missing key in TranslationComponentProperty**";
    } else {
      return value;
    }
  }

  /**
   * Get a translation map.
   *
   * The output map has the following format: Map = {propertyKey1:
   * {propertyKey1, propertyValue1}, ...}
   *
   * @return map
   */
  public static HashMap<String, String> map() {
    HashMap<String, String> map = new HashMap<String, String>();
    // Properties
    map.put("DirectMessages", MESSAGES.DirectMessagesProperties());
    map.put("ContactName", MESSAGES.ContactNameProperties());
    map.put("CurrentAddress", MESSAGES.CurrentAddressProperties());
    map.put("CurrentPageTitle", MESSAGES.CurrentPageTitleProperties());
    map.put("CurrentUrl", MESSAGES.CurrentUrlProperties());
    map.put("Accuracy", MESSAGES.AccuracyProperties());
    map.put("AddressesAndNames", MESSAGES.AddressesAndNamesProperties());
    map.put("Altitude", MESSAGES.AltitudeProperties());
    map.put("Angle", MESSAGES.AngleProperties());
    map.put("Animation", MESSAGES.AnimationProperties());
    map.put("Available", MESSAGES.AvailableProperties());
    map.put("AvailableProviders", MESSAGES.AvailableProvidersProperties());
    map.put("Azimuth", MESSAGES.AzimuthProperties());
    map.put("BallotOptions", MESSAGES.BallotOptionsProperties());
    map.put("BallotQuestion", MESSAGES.BallotQuestionProperties());
    map.put("EmailAddress", MESSAGES.EmailAddressProperties());
    map.put("EmailAddressList", MESSAGES.EmailAddressListProperties());
    map.put("Elements", MESSAGES.ElementsProperties());
    map.put("Followers", MESSAGES.FollowersProperties());
    map.put("FriendTimeline", MESSAGES.FriendTimelineProperties());
    map.put("FullScreen", MESSAGES.FullScreenProperties());
    map.put("HasAccuracy", MESSAGES.HasAccuracyProperties());
    map.put("HasAltitude", MESSAGES.HasAltitudeProperties());
    map.put("HasLongitudeLatitude", MESSAGES.HasLongitudeLatitudeProperties());
    map.put("Height", MESSAGES.HeightProperties());
    map.put("InstanceId", MESSAGES.InstanceIdProperties());
    map.put("InvitedInstances", MESSAGES.InvitedInstancesProperties());
    map.put("IsAccepting", MESSAGES.IsAcceptingProperties());
    map.put("IsConnected", MESSAGES.IsConnectedProperties());
    map.put("IsPlaying", MESSAGES.IsPlayingProperties());
    map.put("JoinedInstances", MESSAGES.JoinedInstancesProperties());
    map.put("Latitude", MESSAGES.LatitudeProperties());
    map.put("Leader", MESSAGES.LeaderProperties());
    map.put("Longitude", MESSAGES.LongitudeProperties());
    map.put("Magnitude", MESSAGES.MagnitudeProperties());
    map.put("Mentions", MESSAGES.MentionsProperties());
    map.put("ProviderLocked", MESSAGES.ProviderLockedProperties());
    map.put("ProviderName", MESSAGES.ProviderNameProperties());
    map.put("PublicInstances", MESSAGES.PublicInstancesProperties());
    map.put("PlayOnlyInForeground", MESSAGES.PlayOnlyInForegroundProperties());
    map.put("Players", MESSAGES.PlayersProperties());
    map.put("RequestHeaders", MESSAGES.RequestHeadersProperties());
    map.put("Result", MESSAGES.ResultProperties());
    map.put("UseExternalScanner", MESSAGES.UseExternalScannerProperties());
    map.put("ResultType", MESSAGES.ResultTypeProperties());
    map.put("ResultUri", MESSAGES.ResultUriProperties());
    map.put("Roll", MESSAGES.RollProperties());
    map.put("SearchResults", MESSAGES.SearchResultsProperties());
    map.put("ServiceUrl", MESSAGES.ServiceUrlProperties());
    map.put("SelectionIndex", MESSAGES.SelectionIndexProperties());
    map.put("UserChoice", MESSAGES.UserChoiceProperties());
    map.put("UserEmailAddress", MESSAGES.UserEmailAddressProperties());
    map.put("UserId", MESSAGES.UserIdProperties());
    map.put("Username", MESSAGES.UsernameProperties());
    map.put("XAccel", MESSAGES.XAccelProperties());
    map.put("YAccel", MESSAGES.YAccelProperties());
    map.put("ZAccel", MESSAGES.ZAccelProperties());
    map.put("Width", MESSAGES.WidthProperties());
    map.put("WebViewString", MESSAGES.WebViewStringProperties());
    map.put("AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("Action", MESSAGES.ActionProperties());
    map.put("ActivityClass", MESSAGES.ActivityClassProperties());
    map.put("ActivityPackage", MESSAGES.ActivityPackageProperties());
    map.put("AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("AllowCookies", MESSAGES.AllowCookiesProperties());
    map.put("ApiKey", MESSAGES.ApiKeyProperties());
    map.put("BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("BackgroundImage", MESSAGES.BackgroundImageProperties());
    map.put("BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("CharacterEncoding", MESSAGES.CharacterEncodingProperties());
    map.put("Checked", MESSAGES.CheckedProperties());
    map.put("ColorChangedEventEnabled", MESSAGES.ColorChangedEventEnabledProperties());
    map.put("Country", MESSAGES.CountryProperties());
    map.put("ConsumerKey", MESSAGES.ConsumerKeyProperties());
    map.put("ConsumerSecret", MESSAGES.ConsumerSecretProperties());
    map.put("DataType", MESSAGES.DataTypeProperties());
    map.put("DataUri", MESSAGES.DataUriProperties());
    map.put("DelimiterByte", MESSAGES.DelimiterByteProperties());
    map.put("DetectColor", MESSAGES.DetectColorProperties());
    map.put("DistanceInterval", MESSAGES.DistanceIntervalProperties());
    map.put("ElementsFromString", MESSAGES.ElementsFromStringProperties());
    map.put("Enabled", MESSAGES.EnabledProperties());
    map.put("ExtraKey", MESSAGES.ExtraKeyProperties());
    map.put("ExtraValue", MESSAGES.ExtraValueProperties());
    map.put("FollowLinks", MESSAGES.FollowLinksProperties());
    map.put("FontSize", MESSAGES.FontSizeProperties());
    map.put("GameId", MESSAGES.GameIdProperties());
    map.put("GenerateColor", MESSAGES.GenerateColorProperties());
    map.put("GenerateLight", MESSAGES.GenerateLightProperties());
    map.put("GoogleVoiceEnabled", MESSAGES.GoogleVoiceEnabledProperties());
    map.put("Heading", MESSAGES.HeadingProperties());
    map.put("HighByteFirst", MESSAGES.HighByteFirstProperties());
    map.put("Hint", MESSAGES.HintProperties());
    map.put("HomeUrl", MESSAGES.HomeUrlProperties());
    map.put("IgnoreSslErrors", MESSAGES.IgnoreSslErrorsProperties());
    map.put("Image", MESSAGES.ImageProperties());
    map.put("Interval", MESSAGES.IntervalProperties());
    map.put("Language", MESSAGES.LanguageProperties());
    map.put("LineWidth", MESSAGES.LineWidthProperties());
    map.put("IsLooping", MESSAGES.IsLoopingProperties());
    map.put("KeyFile", MESSAGES.KeyFileProperties());
    map.put("Message", MESSAGES.MessageProperties());
    map.put("MinimumInterval", MESSAGES.MinimumIntervalProperties());
    map.put("MultiLine", MESSAGES.MultiLineProperties());
    map.put("NumbersOnly", MESSAGES.NumbersOnlyProperties());
    map.put("PaintColor", MESSAGES.PaintColorProperties());
    map.put("PhoneNumber", MESSAGES.PhoneNumberProperties());
    map.put("PhoneNumberList", MESSAGES.PhoneNumberListProperties());
    map.put("Picture", MESSAGES.PictureProperties());
    map.put("Pitch", MESSAGES.PitchProperties());
    map.put("PressedEventEnabled", MESSAGES.PressedEventEnabledProperties());
    map.put("PromptforPermission", MESSAGES.PromptforPermissionProperties());
    map.put("Query", MESSAGES.QueryProperties());
    map.put("Radius", MESSAGES.RadiusProperties());
    map.put("ReceivingEnabled", MESSAGES.ReceivingEnabledProperties());
    map.put("ReleasedEventEnabled", MESSAGES.ReleasedEventEnabledProperties());
    map.put("ResponseFileName", MESSAGES.ResponseFileNameProperties());
    map.put("ResultName", MESSAGES.ResultNameProperties());
    map.put("Rotates", MESSAGES.RotatesProperties());
    map.put("SaveResponse", MESSAGES.SaveResponseProperties());
    map.put("SavedRecording", MESSAGES.SavedRecordingProperties());
    map.put("ScalePictureToFit", MESSAGES.ScalePictureToFitProperties());
    map.put("ScreenOrientation", MESSAGES.ScreenOrientationProperties());
    map.put("Scrollable", MESSAGES.ScrollableProperties());
    map.put("Secure", MESSAGES.SecureProperties());
    map.put("ServiceURL", MESSAGES.ServiceURLProperties());
    map.put("ServiceAccountEmail", MESSAGES.ServiceAccountEmailProperties());
    map.put("Selection", MESSAGES.SelectionProperties());
    map.put("Source", MESSAGES.SourceProperties());
    map.put("Speed", MESSAGES.SpeedProperties());
    map.put("StopBeforeDisconnect", MESSAGES.StopBeforeDisconnectProperties());
    map.put("Text", MESSAGES.TextProperties());
    map.put("TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("TextColor", MESSAGES.TextColorProperties());
    map.put("TimeInterval", MESSAGES.TimeIntervalProperties());
    map.put("TimerAlwaysFires", MESSAGES.TimerAlwaysFiresProperties());
    map.put("TimerEnabled", MESSAGES.TimerEnabledProperties());
    map.put("TimerInterval", MESSAGES.TimerIntervalProperties());
    map.put("Title", MESSAGES.TitleProperties());
    map.put("TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("Url", MESSAGES.UrlProperties());
    map.put("UseServiceAuthentication", MESSAGES.UseServiceAuthenticationProperties());
    map.put("Visible", MESSAGES.VisibleProperties());
    map.put("Volume", MESSAGES.VolumeProperties());
    map.put("WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());
    map.put("X", MESSAGES.XProperties());
    map.put("Y", MESSAGES.YProperties());
    map.put("Z", MESSAGES.ZProperties());
    map.put("ShowFilterBar", MESSAGES.ShowFilterBarProperties());
    map.put("TextSize", MESSAGES.TextSizeProperties());
    map.put("NotifierLength", MESSAGES.NotifierLengthProperties());
    map.put("Loop", MESSAGES.LoopProperties());
    map.put("Pitch", MESSAGES.PitchProperties());
    map.put("SpeechRate", MESSAGES.SpeechRateProperties());
    map.put("Sensitivity", MESSAGES.SensitivityProperties());
    map.put("TwitPic_API_Key", MESSAGES.TwitPic_API_KeyProperties());
    map.put("Prompt", MESSAGES.PromptProperties());
    map.put("ColorLeft", MESSAGES.ColorLeftProperties());
    map.put("ColorRight", MESSAGES.ColorRightProperties());
    map.put("MaxValue", MESSAGES.MaxValueProperties());
    map.put("MinValue", MESSAGES.MinValueProperties());
    map.put("ThumbPosition", MESSAGES.ThumbPositionProperties());
    map.put("ThumbEnabled", MESSAGES.ThumbEnabled());
    map.put("FontBold", MESSAGES.FontBoldProperties());
    map.put("FontItalic", MESSAGES.FontItalicProperties());
    map.put("ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("UseFront", MESSAGES.UseFrontProperties());
    map.put("Day", MESSAGES.DayProperties());
    map.put("Month", MESSAGES.MonthProperties());
    map.put("MonthInText", MESSAGES.MonthInTextProperties());
    map.put("Year", MESSAGES.YearProperties());
    map.put("AboutScreen", MESSAGES.AboutScreenProperties());
    map.put("CloseScreenAnimation", MESSAGES.CloseScreenAnimationProperties());
    map.put("OpenScreenAnimation", MESSAGES.OpenScreenAnimationProperties());
    map.put("LastMessage", MESSAGES.LastMessageProperties());
    map.put("ReadMode", MESSAGES.ReadModeProperties());
    map.put("TextToWrite", MESSAGES.TextToWriteProperties());
    map.put("WriteType", MESSAGES.WriteTypeProperties());
    map.put("CalibrateStrideLength", MESSAGES.CalibrateStrideLengthProperties());
    map.put("Distance", MESSAGES.DistanceProperties());
    map.put("ElapsedTime", MESSAGES.ElapsedTimeProperties());
    map.put("Moving", MESSAGES.MovingProperties());
    map.put("StopDetectionTimeout", MESSAGES.StopDetectionTimeoutProperties());
    map.put("StrideLength", MESSAGES.StrideLengthProperties());
    map.put("UseGPS", MESSAGES.UseGPSProperties());
    map.put("Hour", MESSAGES.HourProperties());
    map.put("Minute", MESSAGES.MinuteProperties());
    map.put("HasMargins", MESSAGES.HasMarginsProperties());

    // =========== ProximitySensor
    map.put("MaximumRange", MESSAGES.MaximumRangeProperties());
    map.put("KeepRunningWhenOnPause", MESSAGES.KeepRunningWhenOnPauseProperties());
    map.put("ProximityChanged", MESSAGES.ProximityChangedPropertiesProperties());

    // ========== ListPicker
    map.put("ItemTextColor", MESSAGES.ItemTextColorProperties());
    map.put("ItemBackgroundColor", MESSAGES.ItemBackgroundColorProperties());

    return map;
  }
}
