// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

public class TranslationDesignerProperties {

  public static String getCorrespondingString(String key) {
    String value = key;

    // Properties
    if (key.equals("AboutScreen")) {
      value = MESSAGES.AboutScreenProperties();
    } else if (key.equals("AboveRangeEventEnabled")) {
      value = MESSAGES.AboveRangeEventEnabledProperties();
    } else if (key.equals("Action")) {
      value = MESSAGES.ActionProperties();
    } else if (key.equals("ActivityClass")) {
      value = MESSAGES.ActivityClassProperties();
    } else if (key.equals("ActivityPackage")) {
      value = MESSAGES.ActivityPackageProperties();
    } else if (key.equals("AlignHorizontal")) {
      value = MESSAGES.AlignHorizontalProperties();
    } else if (key.equals("AlignVertical")) {
      value = MESSAGES.AlignVerticalProperties();
    } else if (key.equals("AllowCookies")) {
      value = MESSAGES.AllowCookiesProperties();
    } else if (key.equals("ApiKey")) {
      value = MESSAGES.ApiKeyProperties();
    } else if (key.equals("BackgroundColor")) {
      value = MESSAGES.BackgroundColorProperties();
    } else if (key.equals("BackgroundImage")) {
      value = MESSAGES.BackgroundImageProperties();
    } else if (key.equals("BelowRangeEventEnabled")) {
      value = MESSAGES.BelowRangeEventEnabledProperties();
    } else if (key.equals("BluetoothClient")) {
      value = MESSAGES.BluetoothClientProperties();
    } else if (key.equals("BottomOfRange")) {
      value = MESSAGES.BottomOfRangeProperties();
    } else if (key.equals("CalibrateStrideLength")) {
      value = MESSAGES.CalibrateStrideLengthProperties();
    } else if (key.equals("CharacterEncoding")) {
      value = MESSAGES.CharacterEncodingProperties();
    } else if (key.equals("Checked")) {
      value = MESSAGES.CheckedProperties();
    } else if (key.equals("CloseScreenAnimation")) {
      value = MESSAGES.CloseScreenAnimationProperties();
    } else if (key.equals("ColorChangedEventEnabled")) {
      value = MESSAGES.ColorChangedEventEnabledProperties();
    } else if (key.equals("Columns")) {
      value = MESSAGES.ColumnsProperties();
    } else if (key.equals("ConsumerKey")) {
      value = MESSAGES.ConsumerKeyProperties();
    } else if (key.equals("ConsumerSecret")) {
      value = MESSAGES.ConsumerSecretProperties();
    } else if (key.equals("Country")) {
      value = MESSAGES.CountryProperties();
    } else if (key.equals("DataType")) {
      value = MESSAGES.DataTypeProperties();
    } else if (key.equals("DataUri")) {
      value = MESSAGES.DataUriProperties();
    } else if (key.equals("DelimiterByte")) {
      value = MESSAGES.DelimiterByteProperties();
    } else if (key.equals("DetectColor")) {
      value = MESSAGES.DetectColorProperties();
    } else if (key.equals("DistanceInterval")) {
      value = MESSAGES.DistanceIntervalProperties();
    } else if (key.equals("DriveMotors")) {
      value = MESSAGES.DriveMotorsProperties();
    } else if (key.equals("ElementsFromString")) {
      value = MESSAGES.ElementsFromStringProperties();
    } else if (key.equals("Enabled")) {
      value = MESSAGES.EnabledProperties();
    } else if (key.equals("ExtraKey")) {
      value = MESSAGES.ExtraKeyProperties();
    } else if (key.equals("ExtraValue")) {
      value = MESSAGES.ExtraValueProperties();
    } else if (key.equals("FollowLinks")) {
      value = MESSAGES.FollowLinksProperties();
    } else if (key.equals("FontBold")) {
      value = MESSAGES.FontBoldProperties();
    } else if (key.equals("FontItalic")) {
      value = MESSAGES.FontItalicProperties();
    } else if (key.equals("FontSize")) {
      value = MESSAGES.FontSizeProperties();
    } else if (key.equals("FontTypeface")) {
      value = MESSAGES.FontTypefaceProperties();
    } else if (key.equals("GameId")) {
      value = MESSAGES.GameIdProperties();
    } else if (key.equals("GenerateColor")) {
      value = MESSAGES.GenerateColorProperties();
    } else if (key.equals("GenerateLight")) {
      value = MESSAGES.GenerateLightProperties();
    } else if (key.equals("GoogleVoiceEnabled")) {
      value = MESSAGES.GoogleVoiceEnabledProperties();
    } else if (key.equals("HasMargins")) {
      value = MESSAGES.HasMarginsProperties();
    } else if (key.equals("Heading")) {
      value = MESSAGES.HeadingProperties();
    } else if (key.equals("HighByteFirst")) {
      value = MESSAGES.HighByteFirstProperties();
    } else if (key.equals("Hint")) {
      value = MESSAGES.HintProperties();
    } else if (key.equals("HomeUrl")) {
      value = MESSAGES.HomeUrlProperties();
    } else if (key.equals("Icon")) {
      value = MESSAGES.IconProperties();
    } else if (key.equals("Image")) {
      value = MESSAGES.ImageProperties();
    } else if (key.equals("Interval")) {
      value = MESSAGES.IntervalProperties();
    } else if (key.equals("IsLooping")) {
      value = MESSAGES.IsLoopingProperties();
    } else if (key.equals("Language")) {
      value = MESSAGES.LanguageProperties();
    } else if (key.equals("LineWidth")) {
      value = MESSAGES.LineWidthProperties();
    } else if (key.equals("Message")) {
      value = MESSAGES.MessageProperties();
    } else if (key.equals("MinimumInterval")) {
      value = MESSAGES.MinimumIntervalProperties();
    } else if (key.equals("MultiLine")) {
      value = MESSAGES.MultiLineProperties();
    } else if (key.equals("NumbersOnly")) {
      value = MESSAGES.NumbersOnlyProperties();
    } else if (key.equals("OpenScreenAnimation")) {
      value = MESSAGES.OpenScreenAnimationProperties();
    } else if (key.equals("PaintColor")) {
      value = MESSAGES.PaintColorProperties();
    } else if (key.equals("PhoneNumber")) {
      value = MESSAGES.PhoneNumberProperties();
    } else if (key.equals("Picture")) {
      value = MESSAGES.PictureProperties();
    } else if (key.equals("PressedEventEnabled")) {
      value = MESSAGES.PressedEventEnabledProperties();
    } else if (key.equals("PromptforPermission")) {
      value = MESSAGES.PromptforPermissionProperties();
    } else if (key.equals("Query")) {
      value = MESSAGES.QueryProperties();
    } else if (key.equals("Radius")) {
      value = MESSAGES.RadiusProperties();
    } else if (key.equals("ReadMode")) {
      value = MESSAGES.ReadModeProperties();
    } else if (key.equals("ReceivingEnabled")) {
      value = MESSAGES.ReceivingEnabledProperties();
    } else if (key.equals("ReleasedEventEnabled")) {
      value = MESSAGES.ReleasedEventEnabledProperties();
    } else if (key.equals("ResponseFileName")) {
      value = MESSAGES.ResponseFileNameProperties();
    } else if (key.equals("ResultName")) {
      value = MESSAGES.ResultNameProperties();
    } else if (key.equals("Rotates")) {
      value = MESSAGES.RotatesProperties();
    } else if (key.equals("Rows")) {
      value = MESSAGES.RowsProperties();
    } else if (key.equals("SaveResponse")) {
      value = MESSAGES.SaveResponseProperties();
    } else if (key.equals("SensorPort")) {
      value = MESSAGES.SensorPortProperties();
    } else if (key.equals("ScreenOrientation")) {
      value = MESSAGES.ScreenOrientationProperties();
    } else if (key.equals("Scrollable")) {
      value = MESSAGES.ScrollableProperties();
    } else if (key.equals("Secure")) {
      value = MESSAGES.SecureProperties();
    } else if (key.equals("Selection")) {
      value = MESSAGES.SelectionProperties();
    } else if (key.equals("ServiceURL")) {
      value = MESSAGES.ServiceURLProperties();
    } else if (key.equals("Shape")) {
      value = MESSAGES.ShapeProperties();
    } else if (key.equals("ShowFeedback")) {
      value = MESSAGES.ShowFeedbackProperties();
    } else if (key.equals("show tables")) {
      value = MESSAGES.ShowTablesProperties();
    } else if (key.equals("Source")) {
      value = MESSAGES.SourceProperties();
    } else if (key.equals("Speed")) {
      value = MESSAGES.SpeedProperties();
    } else if (key.equals("StopBeforeDisconnect")) {
      value = MESSAGES.StopBeforeDisconnectProperties();
    } else if (key.equals("StopDetectionTimeout")) {
      value = MESSAGES.StopDetectionTimeoutProperties();
    } else if (key.equals("StrideLength")) {
      value = MESSAGES.StrideLengthProperties();
    } else if (key.equals("Text")) {
      value = MESSAGES.TextProperties();
    } else if (key.equals("TextAlignment")) {
      value = MESSAGES.TextAlignmentProperties();
    } else if (key.equals("TextColor")) {
      value = MESSAGES.TextColorProperties();
    } else if (key.equals("TimeInterval")) {
      value = MESSAGES.TimeIntervalProperties();
    } else if (key.equals("TimerAlwaysFires")) {
      value = MESSAGES.TimerAlwaysFiresProperties();
    } else if (key.equals("TimerEnabled")) {
      value = MESSAGES.TimerEnabledProperties();
    } else if (key.equals("TimerInterval")) {
      value = MESSAGES.TimerIntervalProperties();
    } else if (key.equals("Title")) {
      value = MESSAGES.TitleProperties();
    } else if (key.equals("TopOfRange")) {
      value = MESSAGES.TopOfRangeProperties();
    } else if (key.equals("Url")) {
      value = MESSAGES.UrlProperties();
    } else if (key.equals("UseFront")) {
      value = MESSAGES.UseFrontProperties();
    } else if (key.equals("UseGPS")) {
      value = MESSAGES.UseGPSProperties();
    } else if (key.equals("UsesLocation")) {
      value = MESSAGES.UsesLocationProperties();
    } else if (key.equals("UsesLocationVisible")) {
      value = MESSAGES.UsesLocationVisibleProperties();
    } else if (key.equals("VersionCode")) {
      value = MESSAGES.VersionCodeProperties();
    } else if (key.equals("VersionName")) {
      value = MESSAGES.VersionNameProperties();
    } else if (key.equals("showing")) {
      value = MESSAGES.VisibilityShowingProperties();
    } else if (key.equals("hidden")) {
      value = MESSAGES.VisibilityHiddenProperties();
    } else if (key.equals("Visible")) {
      value = MESSAGES.VisibleProperties();
    } else if (key.equals("Volume")) {
      value = MESSAGES.VolumeProperties();
    } else if (key.equals("WheelDiameter")) {
      value = MESSAGES.WheelDiameterProperties();
    } else if (key.equals("WithinRangeEventEnabled")) {
      value = MESSAGES.WithinRangeEventEnabledProperties();
    } else if (key.equals("X")) {
      value = MESSAGES.XProperties();
    } else if (key.equals("Y")) {
      value = MESSAGES.YProperties();
    } else if (key.equals("Z")) {
      value = MESSAGES.ZProperties();
    } else if (key.equals("ShowFilterBar")) {
      value = MESSAGES.ShowFilterBarProperties();
    } else if (key.equals("NotifierLength")) {
      value = MESSAGES.NotifierLengthProperties();
    } else if (key.equals("Loop")) {
      value = MESSAGES.LoopProperties();
    } else if (key.equals("Pitch")) {
      value = MESSAGES.PitchProperties();
    } else if (key.equals("SpeechRate")) {
      value = MESSAGES.SpeechRateProperties();
    } else if (key.equals("Sensitivity")) {
      value = MESSAGES.SensitivityProperties();
    } else if (key.equals("TwitPic_API_Key")) {
      value = MESSAGES.TwitPic_API_KeyProperties();
    } else if (key.equals("Prompt")) {
      value = MESSAGES.PromptProperties();
    } else if (key.equals("ColorLeft")) {
      value = MESSAGES.ColorLeftProperties();
    } else if (key.equals("ColorRight")) {
      value = MESSAGES.ColorRightProperties();
    } else if (key.equals("MaxValue")) {
      value = MESSAGES.MaxValueProperties();
    } else if (key.equals("MinValue")) {
      value = MESSAGES.MinValueProperties();
    } else if (key.equals("ThumbPosition")) {
      value = MESSAGES.ThumbPositionProperties();
    } else if (key.equals("UseFront")) {
      value = MESSAGES.UseFrontProperties();
    } else if (key.equals("Day")) {
      value = MESSAGES.DayProperties();
    } else if (key.equals("Month")) {
      value = MESSAGES.MonthProperties();
    } else if (key.equals("MonthInText")) {
      value = MESSAGES.MonthInTextProperties();
    } else if (key.equals("Year")) {
      value = MESSAGES.YearProperties();
    } else if (key.equals("AboutScreen")) {
      value = MESSAGES.AboutScreenProperties();
    } else if (key.equals("CloseScreenAnimation")) {
      value = MESSAGES.CloseScreenAnimationProperties();
    } else if (key.equals("OpenScreenAnimation")) {
      value = MESSAGES.OpenScreenAnimationProperties();
    } else if (key.equals("LastMessage")) {
      value = MESSAGES.LastMessageProperties();
    } else if (key.equals("ReadMode")) {
      value = MESSAGES.ReadModeProperties();
    } else if (key.equals("TextToWrite")) {
      value = MESSAGES.TextToWriteProperties();
    } else if (key.equals("WriteType")) {
      value = MESSAGES.WriteTypeProperties();
    } else if (key.equals("CalibrateStrideLength")) {
      value = MESSAGES.CalibrateStrideLengthProperties();
    } else if (key.equals("Distance")) {
      value = MESSAGES.DistanceProperties();
    } else if (key.equals("ElapsedTime")) {
      value = MESSAGES.ElapsedTimeProperties();
    } else if (key.equals("Moving")) {
      value = MESSAGES.MovingProperties();
    } else if (key.equals("StopDetectionTimeout")) {
      value = MESSAGES.StopDetectionTimeoutProperties();
    } else if (key.equals("StrideLength")) {
      value = MESSAGES.StrideLengthProperties();
    } else if (key.equals("UseGPS")) {
      value = MESSAGES.UseGPSProperties();
    } else if (key.equals("Hour")) {
      value = MESSAGES.HourProperties();
    } else if (key.equals("Minute")) {
      value = MESSAGES.MinuteProperties();
    }
    return value;
  }
}
