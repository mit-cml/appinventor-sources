package com.google.appinventor.client;
import java.util.HashMap;
import java.util.Map;
import static com.google.appinventor.client.Ode.MESSAGES;
public class ComponentsTranslation {
  public static Map<String, String> myMap = map();

  private static String getName(String key) {
    String value = myMap.get(key);
    if (key == null) {
      return "**Missing key in ComponentsTranslations**";
    } else {
      return value;
    }
  }

  public static String getPropertyName(String key) {
    String value = getName("PROPERTY-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getPropertyDescription(String key) {
    String value = getName("PROPDESC-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getMethodName(String key) {
    String value = getName("METHOD-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getEventName(String key) {
    String value = getName("EVENT-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getComponentName(String key) {
    String value = getName("COMPONENT-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getCategoryName(String key) {
    String value = getName("CATEGORY-" + key);
    if(value == null) return key;
    return value;
  }

  public static String getComponentHelpString(String key) {
    String value = getName(key + "-helpString");
    if(value == null) return key;
    return value;
  }
  public static HashMap<String, String> map() {
    HashMap<String, String> map = new HashMap<String, String>();


/* Component: AccelerometerSensor */

    map.put("COMPONENT-AccelerometerSensor", MESSAGES.accelerometerSensorComponentPallette());

    map.put("AccelerometerSensor-helpString", MESSAGES.AccelerometerSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-LegacyMode", MESSAGES.LegacyModeProperties());
    map.put("PROPERTY-MinimumInterval", MESSAGES.MinimumIntervalProperties());
    map.put("PROPERTY-Sensitivity", MESSAGES.SensitivityProperties());
    map.put("PROPERTY-XAccel", MESSAGES.XAccelProperties());
    map.put("PROPERTY-YAccel", MESSAGES.YAccelProperties());
    map.put("PROPERTY-ZAccel", MESSAGES.ZAccelProperties());


/* Events */

    map.put("EVENT-AccelerationChanged", MESSAGES.AccelerationChangedEvents());
    map.put("EVENT-Shaking", MESSAGES.ShakingEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-xAccel", MESSAGES.xAccelParams());
    map.put("PARAM-yAccel", MESSAGES.yAccelParams());
    map.put("PARAM-zAccel", MESSAGES.zAccelParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ActivityStarter */

    map.put("COMPONENT-ActivityStarter", MESSAGES.activityStarterComponentPallette());

    map.put("ActivityStarter-helpString", MESSAGES.ActivityStarterHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Action", MESSAGES.ActionProperties());
    map.put("PROPERTY-ActivityClass", MESSAGES.ActivityClassProperties());
    map.put("PROPERTY-ActivityPackage", MESSAGES.ActivityPackageProperties());
    map.put("PROPERTY-DataType", MESSAGES.DataTypeProperties());
    map.put("PROPERTY-DataUri", MESSAGES.DataUriProperties());
    map.put("PROPERTY-ExtraKey", MESSAGES.ExtraKeyProperties());
    map.put("PROPERTY-ExtraValue", MESSAGES.ExtraValueProperties());
    map.put("PROPERTY-Extras", MESSAGES.ExtrasProperties());
    map.put("PROPERTY-Result", MESSAGES.ResultProperties());
    map.put("PROPERTY-ResultName", MESSAGES.ResultNameProperties());
    map.put("PROPERTY-ResultType", MESSAGES.ResultTypeProperties());
    map.put("PROPERTY-ResultUri", MESSAGES.ResultUriProperties());


/* Events */

    map.put("EVENT-ActivityCanceled", MESSAGES.ActivityCanceledEvents());
    map.put("EVENT-AfterActivity", MESSAGES.AfterActivityEvents());


/* Methods */

    map.put("METHOD-ResolveActivity", MESSAGES.ResolveActivityMethods());
    map.put("METHOD-StartActivity", MESSAGES.StartActivityMethods());


/* Parameters */

    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ball */

    map.put("COMPONENT-Ball", MESSAGES.ballComponentPallette());

    map.put("Ball-helpString", MESSAGES.BallHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-Heading", MESSAGES.HeadingProperties());
    map.put("PROPERTY-Interval", MESSAGES.IntervalProperties());
    map.put("PROPERTY-PaintColor", MESSAGES.PaintColorProperties());
    map.put("PROPERTY-Radius", MESSAGES.RadiusProperties());
    map.put("PROPERTY-Speed", MESSAGES.SpeedProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-X", MESSAGES.XProperties());
    map.put("PROPERTY-Y", MESSAGES.YProperties());
    map.put("PROPERTY-Z", MESSAGES.ZProperties());


/* Events */

    map.put("EVENT-CollidedWith", MESSAGES.CollidedWithEvents());
    map.put("EVENT-Dragged", MESSAGES.DraggedEvents());
    map.put("EVENT-EdgeReached", MESSAGES.EdgeReachedEvents());
    map.put("EVENT-Flung", MESSAGES.FlungEvents());
    map.put("EVENT-NoLongerCollidingWith", MESSAGES.NoLongerCollidingWithEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());
    map.put("EVENT-Touched", MESSAGES.TouchedEvents());


/* Methods */

    map.put("METHOD-Bounce", MESSAGES.BounceMethods());
    map.put("METHOD-CollidingWith", MESSAGES.CollidingWithMethods());
    map.put("METHOD-MoveIntoBounds", MESSAGES.MoveIntoBoundsMethods());
    map.put("METHOD-MoveTo", MESSAGES.MoveToMethods());
    map.put("METHOD-PointInDirection", MESSAGES.PointInDirectionMethods());
    map.put("METHOD-PointTowards", MESSAGES.PointTowardsMethods());


/* Parameters */

    map.put("PARAM-other", MESSAGES.otherParams());
    map.put("PARAM-startX", MESSAGES.startXParams());
    map.put("PARAM-startY", MESSAGES.startYParams());
    map.put("PARAM-prevX", MESSAGES.prevXParams());
    map.put("PARAM-prevY", MESSAGES.prevYParams());
    map.put("PARAM-currentX", MESSAGES.currentXParams());
    map.put("PARAM-currentY", MESSAGES.currentYParams());
    map.put("PARAM-edge", MESSAGES.edgeParams());
    map.put("PARAM-x", MESSAGES.xParams());
    map.put("PARAM-y", MESSAGES.yParams());
    map.put("PARAM-speed", MESSAGES.speedParams());
    map.put("PARAM-heading", MESSAGES.headingParams());
    map.put("PARAM-xvel", MESSAGES.xvelParams());
    map.put("PARAM-yvel", MESSAGES.yvelParams());
    map.put("PARAM-target", MESSAGES.targetParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: BarcodeScanner */

    map.put("COMPONENT-BarcodeScanner", MESSAGES.barcodeScannerComponentPallette());

    map.put("BarcodeScanner-helpString", MESSAGES.BarcodeScannerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Result", MESSAGES.ResultProperties());
    map.put("PROPERTY-UseExternalScanner", MESSAGES.UseExternalScannerProperties());


/* Events */

    map.put("EVENT-AfterScan", MESSAGES.AfterScanEvents());


/* Methods */

    map.put("METHOD-DoScan", MESSAGES.DoScanMethods());


/* Parameters */

    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: BluetoothClient */

    map.put("COMPONENT-BluetoothClient", MESSAGES.bluetoothClientComponentPallette());

    map.put("BluetoothClient-helpString", MESSAGES.BluetoothClientHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AddressesAndNames", MESSAGES.AddressesAndNamesProperties());
    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-CharacterEncoding", MESSAGES.CharacterEncodingProperties());
    map.put("PROPERTY-DelimiterByte", MESSAGES.DelimiterByteProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-HighByteFirst", MESSAGES.HighByteFirstProperties());
    map.put("PROPERTY-IsConnected", MESSAGES.IsConnectedProperties());
    map.put("PROPERTY-Secure", MESSAGES.SecureProperties());


/* Events */



/* Methods */

    map.put("METHOD-BytesAvailableToReceive", MESSAGES.BytesAvailableToReceiveMethods());
    map.put("METHOD-Connect", MESSAGES.ConnectMethods());
    map.put("METHOD-ConnectWithUUID", MESSAGES.ConnectWithUUIDMethods());
    map.put("METHOD-Disconnect", MESSAGES.DisconnectMethods());
    map.put("METHOD-IsDevicePaired", MESSAGES.IsDevicePairedMethods());
    map.put("METHOD-ReceiveSigned1ByteNumber", MESSAGES.ReceiveSigned1ByteNumberMethods());
    map.put("METHOD-ReceiveSigned2ByteNumber", MESSAGES.ReceiveSigned2ByteNumberMethods());
    map.put("METHOD-ReceiveSigned4ByteNumber", MESSAGES.ReceiveSigned4ByteNumberMethods());
    map.put("METHOD-ReceiveSignedBytes", MESSAGES.ReceiveSignedBytesMethods());
    map.put("METHOD-ReceiveText", MESSAGES.ReceiveTextMethods());
    map.put("METHOD-ReceiveUnsigned1ByteNumber", MESSAGES.ReceiveUnsigned1ByteNumberMethods());
    map.put("METHOD-ReceiveUnsigned2ByteNumber", MESSAGES.ReceiveUnsigned2ByteNumberMethods());
    map.put("METHOD-ReceiveUnsigned4ByteNumber", MESSAGES.ReceiveUnsigned4ByteNumberMethods());
    map.put("METHOD-ReceiveUnsignedBytes", MESSAGES.ReceiveUnsignedBytesMethods());
    map.put("METHOD-Send1ByteNumber", MESSAGES.Send1ByteNumberMethods());
    map.put("METHOD-Send2ByteNumber", MESSAGES.Send2ByteNumberMethods());
    map.put("METHOD-Send4ByteNumber", MESSAGES.Send4ByteNumberMethods());
    map.put("METHOD-SendBytes", MESSAGES.SendBytesMethods());
    map.put("METHOD-SendText", MESSAGES.SendTextMethods());


/* Parameters */

    map.put("PARAM-address", MESSAGES.addressParams());
    map.put("PARAM-uuid", MESSAGES.uuidParams());
    map.put("PARAM-numberOfBytes", MESSAGES.numberOfBytesParams());
    map.put("PARAM-number", MESSAGES.numberParams());
    map.put("PARAM-list", MESSAGES.listParams());
    map.put("PARAM-text", MESSAGES.textParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: BluetoothServer */

    map.put("COMPONENT-BluetoothServer", MESSAGES.bluetoothServerComponentPallette());

    map.put("BluetoothServer-helpString", MESSAGES.BluetoothServerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-CharacterEncoding", MESSAGES.CharacterEncodingProperties());
    map.put("PROPERTY-DelimiterByte", MESSAGES.DelimiterByteProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-HighByteFirst", MESSAGES.HighByteFirstProperties());
    map.put("PROPERTY-IsAccepting", MESSAGES.IsAcceptingProperties());
    map.put("PROPERTY-IsConnected", MESSAGES.IsConnectedProperties());
    map.put("PROPERTY-Secure", MESSAGES.SecureProperties());


/* Events */

    map.put("EVENT-ConnectionAccepted", MESSAGES.ConnectionAcceptedEvents());


/* Methods */

    map.put("METHOD-AcceptConnection", MESSAGES.AcceptConnectionMethods());
    map.put("METHOD-AcceptConnectionWithUUID", MESSAGES.AcceptConnectionWithUUIDMethods());
    map.put("METHOD-BytesAvailableToReceive", MESSAGES.BytesAvailableToReceiveMethods());
    map.put("METHOD-Disconnect", MESSAGES.DisconnectMethods());
    map.put("METHOD-ReceiveSigned1ByteNumber", MESSAGES.ReceiveSigned1ByteNumberMethods());
    map.put("METHOD-ReceiveSigned2ByteNumber", MESSAGES.ReceiveSigned2ByteNumberMethods());
    map.put("METHOD-ReceiveSigned4ByteNumber", MESSAGES.ReceiveSigned4ByteNumberMethods());
    map.put("METHOD-ReceiveSignedBytes", MESSAGES.ReceiveSignedBytesMethods());
    map.put("METHOD-ReceiveText", MESSAGES.ReceiveTextMethods());
    map.put("METHOD-ReceiveUnsigned1ByteNumber", MESSAGES.ReceiveUnsigned1ByteNumberMethods());
    map.put("METHOD-ReceiveUnsigned2ByteNumber", MESSAGES.ReceiveUnsigned2ByteNumberMethods());
    map.put("METHOD-ReceiveUnsigned4ByteNumber", MESSAGES.ReceiveUnsigned4ByteNumberMethods());
    map.put("METHOD-ReceiveUnsignedBytes", MESSAGES.ReceiveUnsignedBytesMethods());
    map.put("METHOD-Send1ByteNumber", MESSAGES.Send1ByteNumberMethods());
    map.put("METHOD-Send2ByteNumber", MESSAGES.Send2ByteNumberMethods());
    map.put("METHOD-Send4ByteNumber", MESSAGES.Send4ByteNumberMethods());
    map.put("METHOD-SendBytes", MESSAGES.SendBytesMethods());
    map.put("METHOD-SendText", MESSAGES.SendTextMethods());
    map.put("METHOD-StopAccepting", MESSAGES.StopAcceptingMethods());


/* Parameters */

    map.put("PARAM-serviceName", MESSAGES.serviceNameParams());
    map.put("PARAM-uuid", MESSAGES.uuidParams());
    map.put("PARAM-numberOfBytes", MESSAGES.numberOfBytesParams());
    map.put("PARAM-number", MESSAGES.numberParams());
    map.put("PARAM-list", MESSAGES.listParams());
    map.put("PARAM-text", MESSAGES.textParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Button */

    map.put("COMPONENT-Button", MESSAGES.buttonComponentPallette());

    map.put("Button-helpString", MESSAGES.ButtonHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Camcorder */

    map.put("COMPONENT-Camcorder", MESSAGES.camcorderComponentPallette());

    map.put("Camcorder-helpString", MESSAGES.CamcorderHelpStringComponentPallette());



/* Properties */



/* Events */

    map.put("EVENT-AfterRecording", MESSAGES.AfterRecordingEvents());


/* Methods */

    map.put("METHOD-RecordVideo", MESSAGES.RecordVideoMethods());


/* Parameters */

    map.put("PARAM-clip", MESSAGES.clipParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Camera */

    map.put("COMPONENT-Camera", MESSAGES.cameraComponentPallette());

    map.put("Camera-helpString", MESSAGES.CameraHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-UseFront", MESSAGES.UseFrontProperties());


/* Events */

    map.put("EVENT-AfterPicture", MESSAGES.AfterPictureEvents());


/* Methods */

    map.put("METHOD-TakePicture", MESSAGES.TakePictureMethods());


/* Parameters */

    map.put("PARAM-image", MESSAGES.imageParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Canvas */

    map.put("COMPONENT-Canvas", MESSAGES.canvasComponentPallette());

    map.put("Canvas-helpString", MESSAGES.CanvasHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-BackgroundImage", MESSAGES.BackgroundImageProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-LineWidth", MESSAGES.LineWidthProperties());
    map.put("PROPERTY-PaintColor", MESSAGES.PaintColorProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Dragged", MESSAGES.DraggedEvents());
    map.put("EVENT-Flung", MESSAGES.FlungEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());
    map.put("EVENT-Touched", MESSAGES.TouchedEvents());


/* Methods */

    map.put("METHOD-Clear", MESSAGES.ClearMethods());
    map.put("METHOD-DrawArc", MESSAGES.DrawArcMethods());
    map.put("METHOD-DrawCircle", MESSAGES.DrawCircleMethods());
    map.put("METHOD-DrawLine", MESSAGES.DrawLineMethods());
    map.put("METHOD-DrawPoint", MESSAGES.DrawPointMethods());
    map.put("METHOD-DrawShape", MESSAGES.DrawShapeMethods());
    map.put("METHOD-DrawText", MESSAGES.DrawTextMethods());
    map.put("METHOD-DrawTextAtAngle", MESSAGES.DrawTextAtAngleMethods());
    map.put("METHOD-GetBackgroundPixelColor", MESSAGES.GetBackgroundPixelColorMethods());
    map.put("METHOD-GetPixelColor", MESSAGES.GetPixelColorMethods());
    map.put("METHOD-Save", MESSAGES.SaveMethods());
    map.put("METHOD-SaveAs", MESSAGES.SaveAsMethods());
    map.put("METHOD-SetBackgroundPixelColor", MESSAGES.SetBackgroundPixelColorMethods());


/* Parameters */

    map.put("PARAM-startX", MESSAGES.startXParams());
    map.put("PARAM-startY", MESSAGES.startYParams());
    map.put("PARAM-prevX", MESSAGES.prevXParams());
    map.put("PARAM-prevY", MESSAGES.prevYParams());
    map.put("PARAM-currentX", MESSAGES.currentXParams());
    map.put("PARAM-currentY", MESSAGES.currentYParams());
    map.put("PARAM-draggedAnySprite", MESSAGES.draggedAnySpriteParams());
    map.put("PARAM-x", MESSAGES.xParams());
    map.put("PARAM-y", MESSAGES.yParams());
    map.put("PARAM-speed", MESSAGES.speedParams());
    map.put("PARAM-heading", MESSAGES.headingParams());
    map.put("PARAM-xvel", MESSAGES.xvelParams());
    map.put("PARAM-yvel", MESSAGES.yvelParams());
    map.put("PARAM-flungSprite", MESSAGES.flungSpriteParams());
    map.put("PARAM-touchedAnySprite", MESSAGES.touchedAnySpriteParams());
    map.put("PARAM-left", MESSAGES.leftParams());
    map.put("PARAM-top", MESSAGES.topParams());
    map.put("PARAM-right", MESSAGES.rightParams());
    map.put("PARAM-bottom", MESSAGES.bottomParams());
    map.put("PARAM-startAngle", MESSAGES.startAngleParams());
    map.put("PARAM-sweepAngle", MESSAGES.sweepAngleParams());
    map.put("PARAM-useCenter", MESSAGES.useCenterParams());
    map.put("PARAM-fill", MESSAGES.fillParams());
    map.put("PARAM-centerX", MESSAGES.centerXParams());
    map.put("PARAM-centerY", MESSAGES.centerYParams());
    map.put("PARAM-radius", MESSAGES.radiusParams());
    map.put("PARAM-x1", MESSAGES.x1Params());
    map.put("PARAM-y1", MESSAGES.y1Params());
    map.put("PARAM-x2", MESSAGES.x2Params());
    map.put("PARAM-y2", MESSAGES.y2Params());
    map.put("PARAM-pointList", MESSAGES.pointListParams());
    map.put("PARAM-text", MESSAGES.textParams());
    map.put("PARAM-angle", MESSAGES.angleParams());
    map.put("PARAM-fileName", MESSAGES.fileNameParams());
    map.put("PARAM-color", MESSAGES.colorParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: CheckBox */

    map.put("COMPONENT-CheckBox", MESSAGES.checkBoxComponentPallette());

    map.put("CheckBox-helpString", MESSAGES.CheckBoxHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Checked", MESSAGES.CheckedProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Changed", MESSAGES.ChangedEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Circle */

    map.put("COMPONENT-Circle", MESSAGES.circleComponentPallette());

    map.put("Circle-helpString", MESSAGES.CircleHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Description", MESSAGES.DescriptionProperties());
    map.put("PROPERTY-Draggable", MESSAGES.DraggableProperties());
    map.put("PROPERTY-EnableInfobox", MESSAGES.EnableInfoboxProperties());
    map.put("PROPERTY-FillColor", MESSAGES.FillColorProperties());
    map.put("PROPERTY-Latitude", MESSAGES.LatitudeProperties());
    map.put("PROPERTY-Longitude", MESSAGES.LongitudeProperties());
    map.put("PROPERTY-Radius", MESSAGES.RadiusProperties());
    map.put("PROPERTY-StrokeColor", MESSAGES.StrokeColorProperties());
    map.put("PROPERTY-StrokeWidth", MESSAGES.StrokeWidthProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Type", MESSAGES.TypeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-Drag", MESSAGES.DragEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-StartDrag", MESSAGES.StartDragEvents());
    map.put("EVENT-StopDrag", MESSAGES.StopDragEvents());


/* Methods */

    map.put("METHOD-DistanceToFeature", MESSAGES.DistanceToFeatureMethods());
    map.put("METHOD-DistanceToPoint", MESSAGES.DistanceToPointMethods());
    map.put("METHOD-HideInfobox", MESSAGES.HideInfoboxMethods());
    map.put("METHOD-SetLocation", MESSAGES.SetLocationMethods());
    map.put("METHOD-ShowInfobox", MESSAGES.ShowInfoboxMethods());


/* Parameters */

    map.put("PARAM-mapFeature", MESSAGES.mapFeatureParams());
    map.put("PARAM-centroids", MESSAGES.centroidsParams());
    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-centroid", MESSAGES.centroidParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Clock */

    map.put("COMPONENT-Clock", MESSAGES.clockComponentPallette());

    map.put("Clock-helpString", MESSAGES.ClockHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-TimerAlwaysFires", MESSAGES.TimerAlwaysFiresProperties());
    map.put("PROPERTY-TimerEnabled", MESSAGES.TimerEnabledProperties());
    map.put("PROPERTY-TimerInterval", MESSAGES.TimerIntervalProperties());


/* Events */

    map.put("EVENT-Timer", MESSAGES.TimerEvents());


/* Methods */

    map.put("METHOD-AddDays", MESSAGES.AddDaysMethods());
    map.put("METHOD-AddDuration", MESSAGES.AddDurationMethods());
    map.put("METHOD-AddHours", MESSAGES.AddHoursMethods());
    map.put("METHOD-AddMinutes", MESSAGES.AddMinutesMethods());
    map.put("METHOD-AddMonths", MESSAGES.AddMonthsMethods());
    map.put("METHOD-AddSeconds", MESSAGES.AddSecondsMethods());
    map.put("METHOD-AddWeeks", MESSAGES.AddWeeksMethods());
    map.put("METHOD-AddYears", MESSAGES.AddYearsMethods());
    map.put("METHOD-DayOfMonth", MESSAGES.DayOfMonthMethods());
    map.put("METHOD-Duration", MESSAGES.DurationMethods());
    map.put("METHOD-DurationToDays", MESSAGES.DurationToDaysMethods());
    map.put("METHOD-DurationToHours", MESSAGES.DurationToHoursMethods());
    map.put("METHOD-DurationToMinutes", MESSAGES.DurationToMinutesMethods());
    map.put("METHOD-DurationToSeconds", MESSAGES.DurationToSecondsMethods());
    map.put("METHOD-DurationToWeeks", MESSAGES.DurationToWeeksMethods());
    map.put("METHOD-FormatDate", MESSAGES.FormatDateMethods());
    map.put("METHOD-FormatDateTime", MESSAGES.FormatDateTimeMethods());
    map.put("METHOD-FormatTime", MESSAGES.FormatTimeMethods());
    map.put("METHOD-GetMillis", MESSAGES.GetMillisMethods());
    map.put("METHOD-Hour", MESSAGES.HourMethods());
    map.put("METHOD-MakeDate", MESSAGES.MakeDateMethods());
    map.put("METHOD-MakeInstant", MESSAGES.MakeInstantMethods());
    map.put("METHOD-MakeInstantFromMillis", MESSAGES.MakeInstantFromMillisMethods());
    map.put("METHOD-MakeInstantFromParts", MESSAGES.MakeInstantFromPartsMethods());
    map.put("METHOD-MakeTime", MESSAGES.MakeTimeMethods());
    map.put("METHOD-Minute", MESSAGES.MinuteMethods());
    map.put("METHOD-Month", MESSAGES.MonthMethods());
    map.put("METHOD-MonthName", MESSAGES.MonthNameMethods());
    map.put("METHOD-Now", MESSAGES.NowMethods());
    map.put("METHOD-Second", MESSAGES.SecondMethods());
    map.put("METHOD-SystemTime", MESSAGES.SystemTimeMethods());
    map.put("METHOD-Weekday", MESSAGES.WeekdayMethods());
    map.put("METHOD-WeekdayName", MESSAGES.WeekdayNameMethods());
    map.put("METHOD-Year", MESSAGES.YearMethods());


/* Parameters */

    map.put("PARAM-instant", MESSAGES.instantParams());
    map.put("PARAM-quantity", MESSAGES.quantityParams());
    map.put("PARAM-start", MESSAGES.startParams());
    map.put("PARAM-end", MESSAGES.endParams());
    map.put("PARAM-duration", MESSAGES.durationParams());
    map.put("PARAM-pattern", MESSAGES.patternParams());
    map.put("PARAM-year", MESSAGES.yearParams());
    map.put("PARAM-month", MESSAGES.monthParams());
    map.put("PARAM-day", MESSAGES.dayParams());
    map.put("PARAM-from", MESSAGES.fromParams());
    map.put("PARAM-millis", MESSAGES.millisParams());
    map.put("PARAM-hour", MESSAGES.hourParams());
    map.put("PARAM-minute", MESSAGES.minuteParams());
    map.put("PARAM-second", MESSAGES.secondParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: CloudDB */

    map.put("COMPONENT-CloudDB", MESSAGES.cloudDBComponentPallette());

    map.put("CloudDB-helpString", MESSAGES.CloudDBHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-DefaultRedisServer", MESSAGES.DefaultRedisServerProperties());
    map.put("PROPERTY-ProjectID", MESSAGES.ProjectIDProperties());
    map.put("PROPERTY-RedisPort", MESSAGES.RedisPortProperties());
    map.put("PROPERTY-RedisServer", MESSAGES.RedisServerProperties());
    map.put("PROPERTY-Token", MESSAGES.TokenProperties());
    map.put("PROPERTY-UseSSL", MESSAGES.UseSSLProperties());


/* Events */

    map.put("EVENT-CloudDBError", MESSAGES.CloudDBErrorEvents());
    map.put("EVENT-DataChanged", MESSAGES.DataChangedEvents());
    map.put("EVENT-FirstRemoved", MESSAGES.FirstRemovedEvents());
    map.put("EVENT-GotValue", MESSAGES.GotValueEvents());
    map.put("EVENT-TagList", MESSAGES.TagListEvents());


/* Methods */

    map.put("METHOD-AppendValueToList", MESSAGES.AppendValueToListMethods());
    map.put("METHOD-ClearTag", MESSAGES.ClearTagMethods());
    map.put("METHOD-CloudConnected", MESSAGES.CloudConnectedMethods());
    map.put("METHOD-GetTagList", MESSAGES.GetTagListMethods());
    map.put("METHOD-GetValue", MESSAGES.GetValueMethods());
    map.put("METHOD-RemoveFirstFromList", MESSAGES.RemoveFirstFromListMethods());
    map.put("METHOD-StoreValue", MESSAGES.StoreValueMethods());


/* Parameters */

    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-tag", MESSAGES.tagParams());
    map.put("PARAM-value", MESSAGES.valueParams());
    map.put("PARAM-itemToAdd", MESSAGES.itemToAddParams());
    map.put("PARAM-valueIfTagNotThere", MESSAGES.valueIfTagNotThereParams());
    map.put("PARAM-valueToStore", MESSAGES.valueToStoreParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ContactPicker */

    map.put("COMPONENT-ContactPicker", MESSAGES.contactPickerComponentPallette());

    map.put("ContactPicker-helpString", MESSAGES.ContactPickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-ContactName", MESSAGES.ContactNameProperties());
    map.put("PROPERTY-ContactUri", MESSAGES.ContactUriProperties());
    map.put("PROPERTY-EmailAddress", MESSAGES.EmailAddressProperties());
    map.put("PROPERTY-EmailAddressList", MESSAGES.EmailAddressListProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-PhoneNumber", MESSAGES.PhoneNumberProperties());
    map.put("PROPERTY-PhoneNumberList", MESSAGES.PhoneNumberListProperties());
    map.put("PROPERTY-Picture", MESSAGES.PictureProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterPicking", MESSAGES.AfterPickingEvents());
    map.put("EVENT-BeforePicking", MESSAGES.BeforePickingEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-Open", MESSAGES.OpenMethods());
    map.put("METHOD-ViewContact", MESSAGES.ViewContactMethods());


/* Parameters */

    map.put("PARAM-uri", MESSAGES.uriParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: DatePicker */

    map.put("COMPONENT-DatePicker", MESSAGES.datePickerComponentPallette());

    map.put("DatePicker-helpString", MESSAGES.DatePickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Day", MESSAGES.DayProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Instant", MESSAGES.InstantProperties());
    map.put("PROPERTY-Month", MESSAGES.MonthProperties());
    map.put("PROPERTY-MonthInText", MESSAGES.MonthInTextProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());
    map.put("PROPERTY-Year", MESSAGES.YearProperties());


/* Events */

    map.put("EVENT-AfterDateSet", MESSAGES.AfterDateSetEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-LaunchPicker", MESSAGES.LaunchPickerMethods());
    map.put("METHOD-SetDateToDisplay", MESSAGES.SetDateToDisplayMethods());
    map.put("METHOD-SetDateToDisplayFromInstant", MESSAGES.SetDateToDisplayFromInstantMethods());


/* Parameters */

    map.put("PARAM-year", MESSAGES.yearParams());
    map.put("PARAM-month", MESSAGES.monthParams());
    map.put("PARAM-day", MESSAGES.dayParams());
    map.put("PARAM-instant", MESSAGES.instantParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: EmailPicker */

    map.put("COMPONENT-EmailPicker", MESSAGES.emailPickerComponentPallette());

    map.put("EmailPicker-helpString", MESSAGES.EmailPickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Hint", MESSAGES.HintProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());


/* Methods */

    map.put("METHOD-RequestFocus", MESSAGES.RequestFocusMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3ColorSensor */

    map.put("COMPONENT-Ev3ColorSensor", MESSAGES.ev3ColorSensorComponentPallette());

    map.put("Ev3ColorSensor-helpString", MESSAGES.Ev3ColorSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-ColorChangedEventEnabled", MESSAGES.ColorChangedEventEnabledProperties());
    map.put("PROPERTY-Mode", MESSAGES.ModeProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-ColorChanged", MESSAGES.ColorChangedEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetColorCode", MESSAGES.GetColorCodeMethods());
    map.put("METHOD-GetColorName", MESSAGES.GetColorNameMethods());
    map.put("METHOD-GetLightLevel", MESSAGES.GetLightLevelMethods());
    map.put("METHOD-SetAmbientMode", MESSAGES.SetAmbientModeMethods());
    map.put("METHOD-SetColorMode", MESSAGES.SetColorModeMethods());
    map.put("METHOD-SetReflectedMode", MESSAGES.SetReflectedModeMethods());


/* Parameters */

    map.put("PARAM-colorCode", MESSAGES.colorCodeParams());
    map.put("PARAM-colorName", MESSAGES.colorNameParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3Commands */

    map.put("COMPONENT-Ev3Commands", MESSAGES.ev3CommandsComponentPallette());

    map.put("Ev3Commands-helpString", MESSAGES.Ev3CommandsHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());


/* Events */



/* Methods */

    map.put("METHOD-GetBatteryCurrent", MESSAGES.GetBatteryCurrentMethods());
    map.put("METHOD-GetBatteryVoltage", MESSAGES.GetBatteryVoltageMethods());
    map.put("METHOD-GetFirmwareBuild", MESSAGES.GetFirmwareBuildMethods());
    map.put("METHOD-GetFirmwareVersion", MESSAGES.GetFirmwareVersionMethods());
    map.put("METHOD-GetHardwareVersion", MESSAGES.GetHardwareVersionMethods());
    map.put("METHOD-GetOSBuild", MESSAGES.GetOSBuildMethods());
    map.put("METHOD-GetOSVersion", MESSAGES.GetOSVersionMethods());
    map.put("METHOD-KeepAlive", MESSAGES.KeepAliveMethods());


/* Parameters */

    map.put("PARAM-minutes", MESSAGES.minutesParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3GyroSensor */

    map.put("COMPONENT-Ev3GyroSensor", MESSAGES.ev3GyroSensorComponentPallette());

    map.put("Ev3GyroSensor-helpString", MESSAGES.Ev3GyroSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-Mode", MESSAGES.ModeProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-SensorValueChangedEventEnabled", MESSAGES.SensorValueChangedEventEnabledProperties());


/* Events */

    map.put("EVENT-SensorValueChanged", MESSAGES.SensorValueChangedEvents());


/* Methods */

    map.put("METHOD-GetSensorValue", MESSAGES.GetSensorValueMethods());
    map.put("METHOD-SetAngleMode", MESSAGES.SetAngleModeMethods());
    map.put("METHOD-SetRateMode", MESSAGES.SetRateModeMethods());


/* Parameters */

    map.put("PARAM-sensorValue", MESSAGES.sensorValueParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3Motors */

    map.put("COMPONENT-Ev3Motors", MESSAGES.ev3MotorsComponentPallette());

    map.put("Ev3Motors-helpString", MESSAGES.Ev3MotorsHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-EnableSpeedRegulation", MESSAGES.EnableSpeedRegulationProperties());
    map.put("PROPERTY-MotorPorts", MESSAGES.MotorPortsProperties());
    map.put("PROPERTY-ReverseDirection", MESSAGES.ReverseDirectionProperties());
    map.put("PROPERTY-StopBeforeDisconnect", MESSAGES.StopBeforeDisconnectProperties());
    map.put("PROPERTY-TachoCountChangedEventEnabled", MESSAGES.TachoCountChangedEventEnabledProperties());
    map.put("PROPERTY-WheelDiameter", MESSAGES.WheelDiameterProperties());


/* Events */

    map.put("EVENT-TachoCountChanged", MESSAGES.TachoCountChangedEvents());


/* Methods */

    map.put("METHOD-GetTachoCount", MESSAGES.GetTachoCountMethods());
    map.put("METHOD-ResetTachoCount", MESSAGES.ResetTachoCountMethods());
    map.put("METHOD-RotateInDistance", MESSAGES.RotateInDistanceMethods());
    map.put("METHOD-RotateInDuration", MESSAGES.RotateInDurationMethods());
    map.put("METHOD-RotateInTachoCounts", MESSAGES.RotateInTachoCountsMethods());
    map.put("METHOD-RotateIndefinitely", MESSAGES.RotateIndefinitelyMethods());
    map.put("METHOD-RotateSyncInDistance", MESSAGES.RotateSyncInDistanceMethods());
    map.put("METHOD-RotateSyncInDuration", MESSAGES.RotateSyncInDurationMethods());
    map.put("METHOD-RotateSyncInTachoCounts", MESSAGES.RotateSyncInTachoCountsMethods());
    map.put("METHOD-RotateSyncIndefinitely", MESSAGES.RotateSyncIndefinitelyMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());
    map.put("METHOD-ToggleDirection", MESSAGES.ToggleDirectionMethods());


/* Parameters */

    map.put("PARAM-tachoCount", MESSAGES.tachoCountParams());
    map.put("PARAM-power", MESSAGES.powerParams());
    map.put("PARAM-distance", MESSAGES.distanceParams());
    map.put("PARAM-useBrake", MESSAGES.useBrakeParams());
    map.put("PARAM-milliseconds", MESSAGES.millisecondsParams());
    map.put("PARAM-tachoCounts", MESSAGES.tachoCountsParams());
    map.put("PARAM-turnRatio", MESSAGES.turnRatioParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3Sound */

    map.put("COMPONENT-Ev3Sound", MESSAGES.ev3SoundComponentPallette());

    map.put("Ev3Sound-helpString", MESSAGES.Ev3SoundHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());


/* Events */



/* Methods */

    map.put("METHOD-PlayTone", MESSAGES.PlayToneMethods());
    map.put("METHOD-StopSound", MESSAGES.StopSoundMethods());


/* Parameters */

    map.put("PARAM-volume", MESSAGES.volumeParams());
    map.put("PARAM-frequency", MESSAGES.frequencyParams());
    map.put("PARAM-milliseconds", MESSAGES.millisecondsParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3TouchSensor */

    map.put("COMPONENT-Ev3TouchSensor", MESSAGES.ev3TouchSensorComponentPallette());

    map.put("Ev3TouchSensor-helpString", MESSAGES.Ev3TouchSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-PressedEventEnabled", MESSAGES.PressedEventEnabledProperties());
    map.put("PROPERTY-ReleasedEventEnabled", MESSAGES.ReleasedEventEnabledProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());


/* Events */

    map.put("EVENT-Pressed", MESSAGES.PressedEvents());
    map.put("EVENT-Released", MESSAGES.ReleasedEvents());


/* Methods */

    map.put("METHOD-IsPressed", MESSAGES.IsPressedMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3UI */

    map.put("COMPONENT-Ev3UI", MESSAGES.ev3UIComponentPallette());

    map.put("Ev3UI-helpString", MESSAGES.Ev3UIHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());


/* Events */



/* Methods */

    map.put("METHOD-DrawCircle", MESSAGES.DrawCircleMethods());
    map.put("METHOD-DrawIcon", MESSAGES.DrawIconMethods());
    map.put("METHOD-DrawLine", MESSAGES.DrawLineMethods());
    map.put("METHOD-DrawPoint", MESSAGES.DrawPointMethods());
    map.put("METHOD-DrawRect", MESSAGES.DrawRectMethods());
    map.put("METHOD-FillScreen", MESSAGES.FillScreenMethods());


/* Parameters */

    map.put("PARAM-color", MESSAGES.colorParams());
    map.put("PARAM-x", MESSAGES.xParams());
    map.put("PARAM-y", MESSAGES.yParams());
    map.put("PARAM-radius", MESSAGES.radiusParams());
    map.put("PARAM-fill", MESSAGES.fillParams());
    map.put("PARAM-type", MESSAGES.typeParams());
    map.put("PARAM-no", MESSAGES.noParams());
    map.put("PARAM-x1", MESSAGES.x1Params());
    map.put("PARAM-y1", MESSAGES.y1Params());
    map.put("PARAM-x2", MESSAGES.x2Params());
    map.put("PARAM-y2", MESSAGES.y2Params());
    map.put("PARAM-width", MESSAGES.widthParams());
    map.put("PARAM-height", MESSAGES.heightParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Ev3UltrasonicSensor */

    map.put("COMPONENT-Ev3UltrasonicSensor", MESSAGES.ev3UltrasonicSensorComponentPallette());

    map.put("Ev3UltrasonicSensor-helpString", MESSAGES.Ev3UltrasonicSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-Unit", MESSAGES.UnitProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetDistance", MESSAGES.GetDistanceMethods());
    map.put("METHOD-SetCmUnit", MESSAGES.SetCmUnitMethods());
    map.put("METHOD-SetInchUnit", MESSAGES.SetInchUnitMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: FeatureCollection */

    map.put("COMPONENT-FeatureCollection", MESSAGES.featureCollectionComponentPallette());

    map.put("FeatureCollection-helpString", MESSAGES.FeatureCollectionHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Features", MESSAGES.FeaturesProperties());
    map.put("PROPERTY-FeaturesFromGeoJSON", MESSAGES.FeaturesFromGeoJSONProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Source", MESSAGES.SourceProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-FeatureClick", MESSAGES.FeatureClickEvents());
    map.put("EVENT-FeatureDrag", MESSAGES.FeatureDragEvents());
    map.put("EVENT-FeatureLongClick", MESSAGES.FeatureLongClickEvents());
    map.put("EVENT-FeatureStartDrag", MESSAGES.FeatureStartDragEvents());
    map.put("EVENT-FeatureStopDrag", MESSAGES.FeatureStopDragEvents());
    map.put("EVENT-GotFeatures", MESSAGES.GotFeaturesEvents());
    map.put("EVENT-LoadError", MESSAGES.LoadErrorEvents());


/* Methods */

    map.put("METHOD-FeatureFromDescription", MESSAGES.FeatureFromDescriptionMethods());
    map.put("METHOD-LoadFromURL", MESSAGES.LoadFromURLMethods());


/* Parameters */

    map.put("PARAM-feature", MESSAGES.featureParams());
    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-features", MESSAGES.featuresParams());
    map.put("PARAM-responseCode", MESSAGES.responseCodeParams());
    map.put("PARAM-errorMessage", MESSAGES.errorMessageParams());
    map.put("PARAM-description", MESSAGES.descriptionParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: File */

    map.put("COMPONENT-File", MESSAGES.fileComponentPallette());

    map.put("File-helpString", MESSAGES.FileHelpStringComponentPallette());



/* Properties */



/* Events */

    map.put("EVENT-AfterFileSaved", MESSAGES.AfterFileSavedEvents());
    map.put("EVENT-GotText", MESSAGES.GotTextEvents());


/* Methods */

    map.put("METHOD-AppendToFile", MESSAGES.AppendToFileMethods());
    map.put("METHOD-Delete", MESSAGES.DeleteMethods());
    map.put("METHOD-ReadFrom", MESSAGES.ReadFromMethods());
    map.put("METHOD-SaveFile", MESSAGES.SaveFileMethods());


/* Parameters */

    map.put("PARAM-fileName", MESSAGES.fileNameParams());
    map.put("PARAM-text", MESSAGES.textParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: FirebaseDB */

    map.put("COMPONENT-FirebaseDB", MESSAGES.firebaseDBComponentPallette());

    map.put("FirebaseDB-helpString", MESSAGES.FirebaseDBHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-DefaultURL", MESSAGES.DefaultURLProperties());
    map.put("PROPERTY-DeveloperBucket", MESSAGES.DeveloperBucketProperties());
    map.put("PROPERTY-FirebaseToken", MESSAGES.FirebaseTokenProperties());
    map.put("PROPERTY-FirebaseURL", MESSAGES.FirebaseURLProperties());
    map.put("PROPERTY-Persist", MESSAGES.PersistProperties());
    map.put("PROPERTY-ProjectBucket", MESSAGES.ProjectBucketProperties());


/* Events */

    map.put("EVENT-DataChanged", MESSAGES.DataChangedEvents());
    map.put("EVENT-FirebaseError", MESSAGES.FirebaseErrorEvents());
    map.put("EVENT-FirstRemoved", MESSAGES.FirstRemovedEvents());
    map.put("EVENT-GotValue", MESSAGES.GotValueEvents());
    map.put("EVENT-TagList", MESSAGES.TagListEvents());


/* Methods */

    map.put("METHOD-AppendValue", MESSAGES.AppendValueMethods());
    map.put("METHOD-ClearTag", MESSAGES.ClearTagMethods());
    map.put("METHOD-GetTagList", MESSAGES.GetTagListMethods());
    map.put("METHOD-GetValue", MESSAGES.GetValueMethods());
    map.put("METHOD-RemoveFirst", MESSAGES.RemoveFirstMethods());
    map.put("METHOD-StoreValue", MESSAGES.StoreValueMethods());
    map.put("METHOD-Unauthenticate", MESSAGES.UnauthenticateMethods());


/* Parameters */

    map.put("PARAM-tag", MESSAGES.tagParams());
    map.put("PARAM-value", MESSAGES.valueParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-valueToAdd", MESSAGES.valueToAddParams());
    map.put("PARAM-valueIfTagNotThere", MESSAGES.valueIfTagNotThereParams());
    map.put("PARAM-valueToStore", MESSAGES.valueToStoreParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Form */

    map.put("COMPONENT-Form", MESSAGES.formComponentPallette());

    map.put("Form-helpString", MESSAGES.FormHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboutScreen", MESSAGES.AboutScreenProperties());
    map.put("PROPERTY-AccentColor", MESSAGES.AccentColorProperties());
    map.put("PROPERTY-ActionBar", MESSAGES.ActionBarProperties());
    map.put("PROPERTY-AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("PROPERTY-AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("PROPERTY-AppName", MESSAGES.AppNameProperties());
    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-BackgroundImage", MESSAGES.BackgroundImageProperties());
    map.put("PROPERTY-BlocksToolkit", MESSAGES.BlocksToolkitProperties());
    map.put("PROPERTY-CloseScreenAnimation", MESSAGES.CloseScreenAnimationProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-Icon", MESSAGES.IconProperties());
    map.put("PROPERTY-OpenScreenAnimation", MESSAGES.OpenScreenAnimationProperties());
    map.put("PROPERTY-PrimaryColor", MESSAGES.PrimaryColorProperties());
    map.put("PROPERTY-PrimaryColorDark", MESSAGES.PrimaryColorDarkProperties());
    map.put("PROPERTY-ScreenOrientation", MESSAGES.ScreenOrientationProperties());
    map.put("PROPERTY-Scrollable", MESSAGES.ScrollableProperties());
    map.put("PROPERTY-ShowListsAsJson", MESSAGES.ShowListsAsJsonProperties());
    map.put("PROPERTY-ShowStatusBar", MESSAGES.ShowStatusBarProperties());
    map.put("PROPERTY-Sizing", MESSAGES.SizingProperties());
    map.put("PROPERTY-Theme", MESSAGES.ThemeProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-TitleVisible", MESSAGES.TitleVisibleProperties());
    map.put("PROPERTY-TutorialURL", MESSAGES.TutorialURLProperties());
    map.put("PROPERTY-VersionCode", MESSAGES.VersionCodeProperties());
    map.put("PROPERTY-VersionName", MESSAGES.VersionNameProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());


/* Events */

    map.put("EVENT-BackPressed", MESSAGES.BackPressedEvents());
    map.put("EVENT-ErrorOccurred", MESSAGES.ErrorOccurredEvents());
    map.put("EVENT-Initialize", MESSAGES.InitializeEvents());
    map.put("EVENT-OtherScreenClosed", MESSAGES.OtherScreenClosedEvents());
    map.put("EVENT-PermissionDenied", MESSAGES.PermissionDeniedEvents());
    map.put("EVENT-PermissionGranted", MESSAGES.PermissionGrantedEvents());
    map.put("EVENT-ScreenOrientationChanged", MESSAGES.ScreenOrientationChangedEvents());


/* Methods */

    map.put("METHOD-AskForPermission", MESSAGES.AskForPermissionMethods());
    map.put("METHOD-HideKeyboard", MESSAGES.HideKeyboardMethods());


/* Parameters */

    map.put("PARAM-component", MESSAGES.componentParams());
    map.put("PARAM-functionName", MESSAGES.functionNameParams());
    map.put("PARAM-errorNumber", MESSAGES.errorNumberParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-otherScreenName", MESSAGES.otherScreenNameParams());
    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-permissionName", MESSAGES.permissionNameParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: FusiontablesControl */

    map.put("COMPONENT-FusiontablesControl", MESSAGES.fusiontablesControlComponentPallette());

    map.put("FusiontablesControl-helpString", MESSAGES.FusiontablesControlHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-ApiKey", MESSAGES.ApiKeyProperties());
    map.put("PROPERTY-KeyFile", MESSAGES.KeyFileProperties());
    map.put("PROPERTY-LoadingDialogMessage", MESSAGES.LoadingDialogMessageProperties());
    map.put("PROPERTY-Query", MESSAGES.QueryProperties());
    map.put("PROPERTY-ServiceAccountEmail", MESSAGES.ServiceAccountEmailProperties());
    map.put("PROPERTY-ShowLoadingDialog", MESSAGES.ShowLoadingDialogProperties());
    map.put("PROPERTY-UseServiceAuthentication", MESSAGES.UseServiceAuthenticationProperties());


/* Events */

    map.put("EVENT-GotResult", MESSAGES.GotResultEvents());


/* Methods */

    map.put("METHOD-DoQuery", MESSAGES.DoQueryMethods());
    map.put("METHOD-ForgetLogin", MESSAGES.ForgetLoginMethods());
    map.put("METHOD-GetRows", MESSAGES.GetRowsMethods());
    map.put("METHOD-GetRowsWithConditions", MESSAGES.GetRowsWithConditionsMethods());
    map.put("METHOD-InsertRow", MESSAGES.InsertRowMethods());
    map.put("METHOD-SendQuery", MESSAGES.SendQueryMethods());


/* Parameters */

    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-tableId", MESSAGES.tableIdParams());
    map.put("PARAM-columns", MESSAGES.columnsParams());
    map.put("PARAM-conditions", MESSAGES.conditionsParams());
    map.put("PARAM-values", MESSAGES.valuesParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: GameClient */

    map.put("COMPONENT-GameClient", MESSAGES.gameClientComponentPallette());

    map.put("GameClient-helpString", MESSAGES.GameClientHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-GameId", MESSAGES.GameIdProperties());
    map.put("PROPERTY-InstanceId", MESSAGES.InstanceIdProperties());
    map.put("PROPERTY-InvitedInstances", MESSAGES.InvitedInstancesProperties());
    map.put("PROPERTY-JoinedInstances", MESSAGES.JoinedInstancesProperties());
    map.put("PROPERTY-Leader", MESSAGES.LeaderProperties());
    map.put("PROPERTY-Players", MESSAGES.PlayersProperties());
    map.put("PROPERTY-PublicInstances", MESSAGES.PublicInstancesProperties());
    map.put("PROPERTY-ServiceURL", MESSAGES.ServiceURLProperties());
    map.put("PROPERTY-ServiceUrl", MESSAGES.ServiceUrlProperties());
    map.put("PROPERTY-UserEmailAddress", MESSAGES.UserEmailAddressProperties());


/* Events */

    map.put("EVENT-FunctionCompleted", MESSAGES.FunctionCompletedEvents());
    map.put("EVENT-GotMessage", MESSAGES.GotMessageEvents());
    map.put("EVENT-Info", MESSAGES.InfoEvents());
    map.put("EVENT-InstanceIdChanged", MESSAGES.InstanceIdChangedEvents());
    map.put("EVENT-Invited", MESSAGES.InvitedEvents());
    map.put("EVENT-NewInstanceMade", MESSAGES.NewInstanceMadeEvents());
    map.put("EVENT-NewLeader", MESSAGES.NewLeaderEvents());
    map.put("EVENT-PlayerJoined", MESSAGES.PlayerJoinedEvents());
    map.put("EVENT-PlayerLeft", MESSAGES.PlayerLeftEvents());
    map.put("EVENT-ServerCommandFailure", MESSAGES.ServerCommandFailureEvents());
    map.put("EVENT-ServerCommandSuccess", MESSAGES.ServerCommandSuccessEvents());
    map.put("EVENT-UserEmailAddressSet", MESSAGES.UserEmailAddressSetEvents());
    map.put("EVENT-WebServiceError", MESSAGES.WebServiceErrorEvents());


/* Methods */

    map.put("METHOD-GetInstanceLists", MESSAGES.GetInstanceListsMethods());
    map.put("METHOD-GetMessages", MESSAGES.GetMessagesMethods());
    map.put("METHOD-Invite", MESSAGES.InviteMethods());
    map.put("METHOD-LeaveInstance", MESSAGES.LeaveInstanceMethods());
    map.put("METHOD-MakeNewInstance", MESSAGES.MakeNewInstanceMethods());
    map.put("METHOD-SendMessage", MESSAGES.SendMessageMethods());
    map.put("METHOD-ServerCommand", MESSAGES.ServerCommandMethods());
    map.put("METHOD-SetInstance", MESSAGES.SetInstanceMethods());
    map.put("METHOD-SetLeader", MESSAGES.SetLeaderMethods());


/* Parameters */

    map.put("PARAM-functionName", MESSAGES.functionNameParams());
    map.put("PARAM-type", MESSAGES.typeParams());
    map.put("PARAM-sender", MESSAGES.senderParams());
    map.put("PARAM-contents", MESSAGES.contentsParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-instanceId", MESSAGES.instanceIdParams());
    map.put("PARAM-playerId", MESSAGES.playerIdParams());
    map.put("PARAM-command", MESSAGES.commandParams());
    map.put("PARAM-arguments", MESSAGES.argumentsParams());
    map.put("PARAM-response", MESSAGES.responseParams());
    map.put("PARAM-emailAddress", MESSAGES.emailAddressParams());
    map.put("PARAM-count", MESSAGES.countParams());
    map.put("PARAM-playerEmail", MESSAGES.playerEmailParams());
    map.put("PARAM-makePublic", MESSAGES.makePublicParams());
    map.put("PARAM-recipients", MESSAGES.recipientsParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: GyroscopeSensor */

    map.put("COMPONENT-GyroscopeSensor", MESSAGES.gyroscopeSensorComponentPallette());

    map.put("GyroscopeSensor-helpString", MESSAGES.GyroscopeSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-XAngularVelocity", MESSAGES.XAngularVelocityProperties());
    map.put("PROPERTY-YAngularVelocity", MESSAGES.YAngularVelocityProperties());
    map.put("PROPERTY-ZAngularVelocity", MESSAGES.ZAngularVelocityProperties());


/* Events */

    map.put("EVENT-GyroscopeChanged", MESSAGES.GyroscopeChangedEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-xAngularVelocity", MESSAGES.xAngularVelocityParams());
    map.put("PARAM-yAngularVelocity", MESSAGES.yAngularVelocityParams());
    map.put("PARAM-zAngularVelocity", MESSAGES.zAngularVelocityParams());
    map.put("PARAM-timestamp", MESSAGES.timestampParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: HorizontalArrangement */

    map.put("COMPONENT-HorizontalArrangement", MESSAGES.horizontalArrangementComponentPallette());

    map.put("HorizontalArrangement-helpString", MESSAGES.HorizontalArrangementHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("PROPERTY-AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: HorizontalScrollArrangement */

    map.put("COMPONENT-HorizontalScrollArrangement", MESSAGES.horizontalScrollArrangementComponentPallette());

    map.put("HorizontalScrollArrangement-helpString", MESSAGES.HorizontalScrollArrangementHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("PROPERTY-AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Image */

    map.put("COMPONENT-Image", MESSAGES.imageComponentPallette());

    map.put("Image-helpString", MESSAGES.ImageHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Animation", MESSAGES.AnimationProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Picture", MESSAGES.PictureProperties());
    map.put("PROPERTY-RotationAngle", MESSAGES.RotationAngleProperties());
    map.put("PROPERTY-ScalePictureToFit", MESSAGES.ScalePictureToFitProperties());
    map.put("PROPERTY-Scaling", MESSAGES.ScalingProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ImagePicker */

    map.put("COMPONENT-ImagePicker", MESSAGES.imagePickerComponentPallette());

    map.put("ImagePicker-helpString", MESSAGES.ImagePickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Selection", MESSAGES.SelectionProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterPicking", MESSAGES.AfterPickingEvents());
    map.put("EVENT-BeforePicking", MESSAGES.BeforePickingEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-Open", MESSAGES.OpenMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ImageSprite */

    map.put("COMPONENT-ImageSprite", MESSAGES.imageSpriteComponentPallette());

    map.put("ImageSprite-helpString", MESSAGES.ImageSpriteHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-Heading", MESSAGES.HeadingProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-Interval", MESSAGES.IntervalProperties());
    map.put("PROPERTY-Picture", MESSAGES.PictureProperties());
    map.put("PROPERTY-Rotates", MESSAGES.RotatesProperties());
    map.put("PROPERTY-Speed", MESSAGES.SpeedProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-X", MESSAGES.XProperties());
    map.put("PROPERTY-Y", MESSAGES.YProperties());
    map.put("PROPERTY-Z", MESSAGES.ZProperties());


/* Events */

    map.put("EVENT-CollidedWith", MESSAGES.CollidedWithEvents());
    map.put("EVENT-Dragged", MESSAGES.DraggedEvents());
    map.put("EVENT-EdgeReached", MESSAGES.EdgeReachedEvents());
    map.put("EVENT-Flung", MESSAGES.FlungEvents());
    map.put("EVENT-NoLongerCollidingWith", MESSAGES.NoLongerCollidingWithEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());
    map.put("EVENT-Touched", MESSAGES.TouchedEvents());


/* Methods */

    map.put("METHOD-Bounce", MESSAGES.BounceMethods());
    map.put("METHOD-CollidingWith", MESSAGES.CollidingWithMethods());
    map.put("METHOD-MoveIntoBounds", MESSAGES.MoveIntoBoundsMethods());
    map.put("METHOD-MoveTo", MESSAGES.MoveToMethods());
    map.put("METHOD-PointInDirection", MESSAGES.PointInDirectionMethods());
    map.put("METHOD-PointTowards", MESSAGES.PointTowardsMethods());


/* Parameters */

    map.put("PARAM-other", MESSAGES.otherParams());
    map.put("PARAM-startX", MESSAGES.startXParams());
    map.put("PARAM-startY", MESSAGES.startYParams());
    map.put("PARAM-prevX", MESSAGES.prevXParams());
    map.put("PARAM-prevY", MESSAGES.prevYParams());
    map.put("PARAM-currentX", MESSAGES.currentXParams());
    map.put("PARAM-currentY", MESSAGES.currentYParams());
    map.put("PARAM-edge", MESSAGES.edgeParams());
    map.put("PARAM-x", MESSAGES.xParams());
    map.put("PARAM-y", MESSAGES.yParams());
    map.put("PARAM-speed", MESSAGES.speedParams());
    map.put("PARAM-heading", MESSAGES.headingParams());
    map.put("PARAM-xvel", MESSAGES.xvelParams());
    map.put("PARAM-yvel", MESSAGES.yvelParams());
    map.put("PARAM-target", MESSAGES.targetParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Label */

    map.put("COMPONENT-Label", MESSAGES.labelComponentPallette());

    map.put("Label-helpString", MESSAGES.LabelHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-HTMLFormat", MESSAGES.HTMLFormatProperties());
    map.put("PROPERTY-HasMargins", MESSAGES.HasMarginsProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: LineString */

    map.put("COMPONENT-LineString", MESSAGES.lineStringComponentPallette());

    map.put("LineString-helpString", MESSAGES.LineStringHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Description", MESSAGES.DescriptionProperties());
    map.put("PROPERTY-Draggable", MESSAGES.DraggableProperties());
    map.put("PROPERTY-EnableInfobox", MESSAGES.EnableInfoboxProperties());
    map.put("PROPERTY-Points", MESSAGES.PointsProperties());
    map.put("PROPERTY-PointsFromString", MESSAGES.PointsFromStringProperties());
    map.put("PROPERTY-StrokeColor", MESSAGES.StrokeColorProperties());
    map.put("PROPERTY-StrokeWidth", MESSAGES.StrokeWidthProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Type", MESSAGES.TypeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-Drag", MESSAGES.DragEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-StartDrag", MESSAGES.StartDragEvents());
    map.put("EVENT-StopDrag", MESSAGES.StopDragEvents());


/* Methods */

    map.put("METHOD-DistanceToFeature", MESSAGES.DistanceToFeatureMethods());
    map.put("METHOD-DistanceToPoint", MESSAGES.DistanceToPointMethods());
    map.put("METHOD-HideInfobox", MESSAGES.HideInfoboxMethods());
    map.put("METHOD-ShowInfobox", MESSAGES.ShowInfoboxMethods());


/* Parameters */

    map.put("PARAM-mapFeature", MESSAGES.mapFeatureParams());
    map.put("PARAM-centroids", MESSAGES.centroidsParams());
    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-centroid", MESSAGES.centroidParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ListPicker */

    map.put("COMPONENT-ListPicker", MESSAGES.listPickerComponentPallette());

    map.put("ListPicker-helpString", MESSAGES.ListPickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Elements", MESSAGES.ElementsProperties());
    map.put("PROPERTY-ElementsFromString", MESSAGES.ElementsFromStringProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-ItemBackgroundColor", MESSAGES.ItemBackgroundColorProperties());
    map.put("PROPERTY-ItemTextColor", MESSAGES.ItemTextColorProperties());
    map.put("PROPERTY-Selection", MESSAGES.SelectionProperties());
    map.put("PROPERTY-SelectionIndex", MESSAGES.SelectionIndexProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-ShowFilterBar", MESSAGES.ShowFilterBarProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterPicking", MESSAGES.AfterPickingEvents());
    map.put("EVENT-BeforePicking", MESSAGES.BeforePickingEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-Open", MESSAGES.OpenMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ListView */

    map.put("COMPONENT-ListView", MESSAGES.listViewComponentPallette());

    map.put("ListView-helpString", MESSAGES.ListViewHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Elements", MESSAGES.ElementsProperties());
    map.put("PROPERTY-ElementsFromString", MESSAGES.ElementsFromStringProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Selection", MESSAGES.SelectionProperties());
    map.put("PROPERTY-SelectionColor", MESSAGES.SelectionColorProperties());
    map.put("PROPERTY-SelectionIndex", MESSAGES.SelectionIndexProperties());
    map.put("PROPERTY-ShowFilterBar", MESSAGES.ShowFilterBarProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-TextSize", MESSAGES.TextSizeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterPicking", MESSAGES.AfterPickingEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: LocationSensor */

    map.put("COMPONENT-LocationSensor", MESSAGES.locationSensorComponentPallette());

    map.put("LocationSensor-helpString", MESSAGES.LocationSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Accuracy", MESSAGES.AccuracyProperties());
    map.put("PROPERTY-Altitude", MESSAGES.AltitudeProperties());
    map.put("PROPERTY-AvailableProviders", MESSAGES.AvailableProvidersProperties());
    map.put("PROPERTY-CurrentAddress", MESSAGES.CurrentAddressProperties());
    map.put("PROPERTY-DistanceInterval", MESSAGES.DistanceIntervalProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-HasAccuracy", MESSAGES.HasAccuracyProperties());
    map.put("PROPERTY-HasAltitude", MESSAGES.HasAltitudeProperties());
    map.put("PROPERTY-HasLongitudeLatitude", MESSAGES.HasLongitudeLatitudeProperties());
    map.put("PROPERTY-Latitude", MESSAGES.LatitudeProperties());
    map.put("PROPERTY-Longitude", MESSAGES.LongitudeProperties());
    map.put("PROPERTY-ProviderLocked", MESSAGES.ProviderLockedProperties());
    map.put("PROPERTY-ProviderName", MESSAGES.ProviderNameProperties());
    map.put("PROPERTY-TimeInterval", MESSAGES.TimeIntervalProperties());


/* Events */

    map.put("EVENT-LocationChanged", MESSAGES.LocationChangedEvents());
    map.put("EVENT-StatusChanged", MESSAGES.StatusChangedEvents());


/* Methods */

    map.put("METHOD-LatitudeFromAddress", MESSAGES.LatitudeFromAddressMethods());
    map.put("METHOD-LongitudeFromAddress", MESSAGES.LongitudeFromAddressMethods());


/* Parameters */

    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-altitude", MESSAGES.altitudeParams());
    map.put("PARAM-speed", MESSAGES.speedParams());
    map.put("PARAM-provider", MESSAGES.providerParams());
    map.put("PARAM-status", MESSAGES.statusParams());
    map.put("PARAM-locationName", MESSAGES.locationNameParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Map */

    map.put("COMPONENT-Map", MESSAGES.mapComponentPallette());

    map.put("Map-helpString", MESSAGES.MapHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BoundingBox", MESSAGES.BoundingBoxProperties());
    map.put("PROPERTY-CenterFromString", MESSAGES.CenterFromStringProperties());
    map.put("PROPERTY-EnablePan", MESSAGES.EnablePanProperties());
    map.put("PROPERTY-EnableRotation", MESSAGES.EnableRotationProperties());
    map.put("PROPERTY-EnableZoom", MESSAGES.EnableZoomProperties());
    map.put("PROPERTY-Features", MESSAGES.FeaturesProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Latitude", MESSAGES.LatitudeProperties());
    map.put("PROPERTY-LocationSensor", MESSAGES.LocationSensorProperties());
    map.put("PROPERTY-Longitude", MESSAGES.LongitudeProperties());
    map.put("PROPERTY-MapType", MESSAGES.MapTypeProperties());
    map.put("PROPERTY-Rotation", MESSAGES.RotationProperties());
    map.put("PROPERTY-ScaleUnits", MESSAGES.ScaleUnitsProperties());
    map.put("PROPERTY-ShowCompass", MESSAGES.ShowCompassProperties());
    map.put("PROPERTY-ShowScale", MESSAGES.ShowScaleProperties());
    map.put("PROPERTY-ShowUser", MESSAGES.ShowUserProperties());
    map.put("PROPERTY-ShowZoom", MESSAGES.ShowZoomProperties());
    map.put("PROPERTY-UserLatitude", MESSAGES.UserLatitudeProperties());
    map.put("PROPERTY-UserLongitude", MESSAGES.UserLongitudeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());
    map.put("PROPERTY-ZoomLevel", MESSAGES.ZoomLevelProperties());


/* Events */

    map.put("EVENT-BoundsChange", MESSAGES.BoundsChangeEvents());
    map.put("EVENT-DoubleTapAtPoint", MESSAGES.DoubleTapAtPointEvents());
    map.put("EVENT-FeatureClick", MESSAGES.FeatureClickEvents());
    map.put("EVENT-FeatureDrag", MESSAGES.FeatureDragEvents());
    map.put("EVENT-FeatureLongClick", MESSAGES.FeatureLongClickEvents());
    map.put("EVENT-FeatureStartDrag", MESSAGES.FeatureStartDragEvents());
    map.put("EVENT-FeatureStopDrag", MESSAGES.FeatureStopDragEvents());
    map.put("EVENT-GotFeatures", MESSAGES.GotFeaturesEvents());
    map.put("EVENT-InvalidPoint", MESSAGES.InvalidPointEvents());
    map.put("EVENT-LoadError", MESSAGES.LoadErrorEvents());
    map.put("EVENT-LongPressAtPoint", MESSAGES.LongPressAtPointEvents());
    map.put("EVENT-Ready", MESSAGES.ReadyEvents());
    map.put("EVENT-TapAtPoint", MESSAGES.TapAtPointEvents());
    map.put("EVENT-ZoomChange", MESSAGES.ZoomChangeEvents());


/* Methods */

    map.put("METHOD-CreateMarker", MESSAGES.CreateMarkerMethods());
    map.put("METHOD-FeatureFromDescription", MESSAGES.FeatureFromDescriptionMethods());
    map.put("METHOD-LoadFromURL", MESSAGES.LoadFromURLMethods());
    map.put("METHOD-PanTo", MESSAGES.PanToMethods());
    map.put("METHOD-Save", MESSAGES.SaveMethods());


/* Parameters */

    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-feature", MESSAGES.featureParams());
    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-features", MESSAGES.featuresParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-responseCode", MESSAGES.responseCodeParams());
    map.put("PARAM-errorMessage", MESSAGES.errorMessageParams());
    map.put("PARAM-description", MESSAGES.descriptionParams());
    map.put("PARAM-zoom", MESSAGES.zoomParams());
    map.put("PARAM-path", MESSAGES.pathParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Marker */

    map.put("COMPONENT-Marker", MESSAGES.markerComponentPallette());

    map.put("Marker-helpString", MESSAGES.MarkerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AnchorHorizontal", MESSAGES.AnchorHorizontalProperties());
    map.put("PROPERTY-AnchorVertical", MESSAGES.AnchorVerticalProperties());
    map.put("PROPERTY-Description", MESSAGES.DescriptionProperties());
    map.put("PROPERTY-Draggable", MESSAGES.DraggableProperties());
    map.put("PROPERTY-EnableInfobox", MESSAGES.EnableInfoboxProperties());
    map.put("PROPERTY-FillColor", MESSAGES.FillColorProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-ImageAsset", MESSAGES.ImageAssetProperties());
    map.put("PROPERTY-Latitude", MESSAGES.LatitudeProperties());
    map.put("PROPERTY-Longitude", MESSAGES.LongitudeProperties());
    map.put("PROPERTY-StrokeColor", MESSAGES.StrokeColorProperties());
    map.put("PROPERTY-StrokeWidth", MESSAGES.StrokeWidthProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Type", MESSAGES.TypeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-Drag", MESSAGES.DragEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-StartDrag", MESSAGES.StartDragEvents());
    map.put("EVENT-StopDrag", MESSAGES.StopDragEvents());


/* Methods */

    map.put("METHOD-BearingToFeature", MESSAGES.BearingToFeatureMethods());
    map.put("METHOD-BearingToPoint", MESSAGES.BearingToPointMethods());
    map.put("METHOD-DistanceToFeature", MESSAGES.DistanceToFeatureMethods());
    map.put("METHOD-DistanceToPoint", MESSAGES.DistanceToPointMethods());
    map.put("METHOD-HideInfobox", MESSAGES.HideInfoboxMethods());
    map.put("METHOD-SetLocation", MESSAGES.SetLocationMethods());
    map.put("METHOD-ShowInfobox", MESSAGES.ShowInfoboxMethods());


/* Parameters */

    map.put("PARAM-mapFeature", MESSAGES.mapFeatureParams());
    map.put("PARAM-centroids", MESSAGES.centroidsParams());
    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: MediaStore */

    map.put("COMPONENT-MediaStore", MESSAGES.mediaStoreComponentPallette());

    map.put("MediaStore-helpString", MESSAGES.MediaStoreHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-ServiceURL", MESSAGES.ServiceURLProperties());


/* Events */

    map.put("EVENT-MediaStored", MESSAGES.MediaStoredEvents());
    map.put("EVENT-WebServiceError", MESSAGES.WebServiceErrorEvents());


/* Methods */

    map.put("METHOD-PostMedia", MESSAGES.PostMediaMethods());


/* Parameters */

    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-mediafile", MESSAGES.mediafileParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NearField */

    map.put("COMPONENT-NearField", MESSAGES.nearFieldComponentPallette());

    map.put("NearField-helpString", MESSAGES.NearFieldHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-LastMessage", MESSAGES.LastMessageProperties());
    map.put("PROPERTY-ReadMode", MESSAGES.ReadModeProperties());
    map.put("PROPERTY-TextToWrite", MESSAGES.TextToWriteProperties());
    map.put("PROPERTY-WriteType", MESSAGES.WriteTypeProperties());


/* Events */

    map.put("EVENT-TagRead", MESSAGES.TagReadEvents());
    map.put("EVENT-TagWritten", MESSAGES.TagWrittenEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Notifier */

    map.put("COMPONENT-Notifier", MESSAGES.notifierComponentPallette());

    map.put("Notifier-helpString", MESSAGES.NotifierHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-NotifierLength", MESSAGES.NotifierLengthProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());


/* Events */

    map.put("EVENT-AfterChoosing", MESSAGES.AfterChoosingEvents());
    map.put("EVENT-AfterTextInput", MESSAGES.AfterTextInputEvents());
    map.put("EVENT-ChoosingCanceled", MESSAGES.ChoosingCanceledEvents());
    map.put("EVENT-TextInputCanceled", MESSAGES.TextInputCanceledEvents());


/* Methods */

    map.put("METHOD-DismissProgressDialog", MESSAGES.DismissProgressDialogMethods());
    map.put("METHOD-LogError", MESSAGES.LogErrorMethods());
    map.put("METHOD-LogInfo", MESSAGES.LogInfoMethods());
    map.put("METHOD-LogWarning", MESSAGES.LogWarningMethods());
    map.put("METHOD-ShowAlert", MESSAGES.ShowAlertMethods());
    map.put("METHOD-ShowChooseDialog", MESSAGES.ShowChooseDialogMethods());
    map.put("METHOD-ShowMessageDialog", MESSAGES.ShowMessageDialogMethods());
    map.put("METHOD-ShowPasswordDialog", MESSAGES.ShowPasswordDialogMethods());
    map.put("METHOD-ShowProgressDialog", MESSAGES.ShowProgressDialogMethods());
    map.put("METHOD-ShowTextDialog", MESSAGES.ShowTextDialogMethods());


/* Parameters */

    map.put("PARAM-choice", MESSAGES.choiceParams());
    map.put("PARAM-response", MESSAGES.responseParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-notice", MESSAGES.noticeParams());
    map.put("PARAM-title", MESSAGES.titleParams());
    map.put("PARAM-button1Text", MESSAGES.button1TextParams());
    map.put("PARAM-button2Text", MESSAGES.button2TextParams());
    map.put("PARAM-cancelable", MESSAGES.cancelableParams());
    map.put("PARAM-buttonText", MESSAGES.buttonTextParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtColorSensor */

    map.put("COMPONENT-NxtColorSensor", MESSAGES.nxtColorSensorComponentPallette());

    map.put("NxtColorSensor-helpString", MESSAGES.NxtColorSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-ColorChangedEventEnabled", MESSAGES.ColorChangedEventEnabledProperties());
    map.put("PROPERTY-DetectColor", MESSAGES.DetectColorProperties());
    map.put("PROPERTY-GenerateColor", MESSAGES.GenerateColorProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-ColorChanged", MESSAGES.ColorChangedEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetColor", MESSAGES.GetColorMethods());
    map.put("METHOD-GetLightLevel", MESSAGES.GetLightLevelMethods());


/* Parameters */

    map.put("PARAM-color", MESSAGES.colorParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtDirectCommands */

    map.put("COMPONENT-NxtDirectCommands", MESSAGES.nxtDirectCommandsComponentPallette());

    map.put("NxtDirectCommands-helpString", MESSAGES.NxtDirectCommandsHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());


/* Events */



/* Methods */

    map.put("METHOD-DeleteFile", MESSAGES.DeleteFileMethods());
    map.put("METHOD-DownloadFile", MESSAGES.DownloadFileMethods());
    map.put("METHOD-GetBatteryLevel", MESSAGES.GetBatteryLevelMethods());
    map.put("METHOD-GetBrickName", MESSAGES.GetBrickNameMethods());
    map.put("METHOD-GetCurrentProgramName", MESSAGES.GetCurrentProgramNameMethods());
    map.put("METHOD-GetFirmwareVersion", MESSAGES.GetFirmwareVersionMethods());
    map.put("METHOD-GetInputValues", MESSAGES.GetInputValuesMethods());
    map.put("METHOD-GetOutputState", MESSAGES.GetOutputStateMethods());
    map.put("METHOD-KeepAlive", MESSAGES.KeepAliveMethods());
    map.put("METHOD-ListFiles", MESSAGES.ListFilesMethods());
    map.put("METHOD-LsGetStatus", MESSAGES.LsGetStatusMethods());
    map.put("METHOD-LsRead", MESSAGES.LsReadMethods());
    map.put("METHOD-LsWrite", MESSAGES.LsWriteMethods());
    map.put("METHOD-MessageRead", MESSAGES.MessageReadMethods());
    map.put("METHOD-MessageWrite", MESSAGES.MessageWriteMethods());
    map.put("METHOD-PlaySoundFile", MESSAGES.PlaySoundFileMethods());
    map.put("METHOD-PlayTone", MESSAGES.PlayToneMethods());
    map.put("METHOD-ResetInputScaledValue", MESSAGES.ResetInputScaledValueMethods());
    map.put("METHOD-ResetMotorPosition", MESSAGES.ResetMotorPositionMethods());
    map.put("METHOD-SetBrickName", MESSAGES.SetBrickNameMethods());
    map.put("METHOD-SetInputMode", MESSAGES.SetInputModeMethods());
    map.put("METHOD-SetOutputState", MESSAGES.SetOutputStateMethods());
    map.put("METHOD-StartProgram", MESSAGES.StartProgramMethods());
    map.put("METHOD-StopProgram", MESSAGES.StopProgramMethods());
    map.put("METHOD-StopSoundPlayback", MESSAGES.StopSoundPlaybackMethods());


/* Parameters */

    map.put("PARAM-fileName", MESSAGES.fileNameParams());
    map.put("PARAM-source", MESSAGES.sourceParams());
    map.put("PARAM-destination", MESSAGES.destinationParams());
    map.put("PARAM-sensorPortLetter", MESSAGES.sensorPortLetterParams());
    map.put("PARAM-motorPortLetter", MESSAGES.motorPortLetterParams());
    map.put("PARAM-wildcard", MESSAGES.wildcardParams());
    map.put("PARAM-list", MESSAGES.listParams());
    map.put("PARAM-rxDataLength", MESSAGES.rxDataLengthParams());
    map.put("PARAM-mailbox", MESSAGES.mailboxParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-frequencyHz", MESSAGES.frequencyHzParams());
    map.put("PARAM-durationMs", MESSAGES.durationMsParams());
    map.put("PARAM-relative", MESSAGES.relativeParams());
    map.put("PARAM-name", MESSAGES.nameParams());
    map.put("PARAM-sensorType", MESSAGES.sensorTypeParams());
    map.put("PARAM-sensorMode", MESSAGES.sensorModeParams());
    map.put("PARAM-power", MESSAGES.powerParams());
    map.put("PARAM-mode", MESSAGES.modeParams());
    map.put("PARAM-regulationMode", MESSAGES.regulationModeParams());
    map.put("PARAM-turnRatio", MESSAGES.turnRatioParams());
    map.put("PARAM-runState", MESSAGES.runStateParams());
    map.put("PARAM-tachoLimit", MESSAGES.tachoLimitParams());
    map.put("PARAM-programName", MESSAGES.programNameParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtDrive */

    map.put("COMPONENT-NxtDrive", MESSAGES.nxtDriveComponentPallette());

    map.put("NxtDrive-helpString", MESSAGES.NxtDriveHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-DriveMotors", MESSAGES.DriveMotorsProperties());
    map.put("PROPERTY-StopBeforeDisconnect", MESSAGES.StopBeforeDisconnectProperties());
    map.put("PROPERTY-WheelDiameter", MESSAGES.WheelDiameterProperties());


/* Events */



/* Methods */

    map.put("METHOD-MoveBackward", MESSAGES.MoveBackwardMethods());
    map.put("METHOD-MoveBackwardIndefinitely", MESSAGES.MoveBackwardIndefinitelyMethods());
    map.put("METHOD-MoveForward", MESSAGES.MoveForwardMethods());
    map.put("METHOD-MoveForwardIndefinitely", MESSAGES.MoveForwardIndefinitelyMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());
    map.put("METHOD-TurnClockwiseIndefinitely", MESSAGES.TurnClockwiseIndefinitelyMethods());
    map.put("METHOD-TurnCounterClockwiseIndefinitely", MESSAGES.TurnCounterClockwiseIndefinitelyMethods());


/* Parameters */

    map.put("PARAM-power", MESSAGES.powerParams());
    map.put("PARAM-distance", MESSAGES.distanceParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtLightSensor */

    map.put("COMPONENT-NxtLightSensor", MESSAGES.nxtLightSensorComponentPallette());

    map.put("NxtLightSensor-helpString", MESSAGES.NxtLightSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-GenerateLight", MESSAGES.GenerateLightProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetLightLevel", MESSAGES.GetLightLevelMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtSoundSensor */

    map.put("COMPONENT-NxtSoundSensor", MESSAGES.nxtSoundSensorComponentPallette());

    map.put("NxtSoundSensor-helpString", MESSAGES.NxtSoundSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetSoundLevel", MESSAGES.GetSoundLevelMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtTouchSensor */

    map.put("COMPONENT-NxtTouchSensor", MESSAGES.nxtTouchSensorComponentPallette());

    map.put("NxtTouchSensor-helpString", MESSAGES.NxtTouchSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-PressedEventEnabled", MESSAGES.PressedEventEnabledProperties());
    map.put("PROPERTY-ReleasedEventEnabled", MESSAGES.ReleasedEventEnabledProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());


/* Events */

    map.put("EVENT-Pressed", MESSAGES.PressedEvents());
    map.put("EVENT-Released", MESSAGES.ReleasedEvents());


/* Methods */

    map.put("METHOD-IsPressed", MESSAGES.IsPressedMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: NxtUltrasonicSensor */

    map.put("COMPONENT-NxtUltrasonicSensor", MESSAGES.nxtUltrasonicSensorComponentPallette());

    map.put("NxtUltrasonicSensor-helpString", MESSAGES.NxtUltrasonicSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledProperties());
    map.put("PROPERTY-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledProperties());
    map.put("PROPERTY-BluetoothClient", MESSAGES.BluetoothClientProperties());
    map.put("PROPERTY-BottomOfRange", MESSAGES.BottomOfRangeProperties());
    map.put("PROPERTY-SensorPort", MESSAGES.SensorPortProperties());
    map.put("PROPERTY-TopOfRange", MESSAGES.TopOfRangeProperties());
    map.put("PROPERTY-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledProperties());


/* Events */

    map.put("EVENT-AboveRange", MESSAGES.AboveRangeEvents());
    map.put("EVENT-BelowRange", MESSAGES.BelowRangeEvents());
    map.put("EVENT-WithinRange", MESSAGES.WithinRangeEvents());


/* Methods */

    map.put("METHOD-GetDistance", MESSAGES.GetDistanceMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: OrientationSensor */

    map.put("COMPONENT-OrientationSensor", MESSAGES.orientationSensorComponentPallette());

    map.put("OrientationSensor-helpString", MESSAGES.OrientationSensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Angle", MESSAGES.AngleProperties());
    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-Azimuth", MESSAGES.AzimuthProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-Magnitude", MESSAGES.MagnitudeProperties());
    map.put("PROPERTY-Pitch", MESSAGES.PitchProperties());
    map.put("PROPERTY-Roll", MESSAGES.RollProperties());


/* Events */

    map.put("EVENT-OrientationChanged", MESSAGES.OrientationChangedEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-azimuth", MESSAGES.azimuthParams());
    map.put("PARAM-pitch", MESSAGES.pitchParams());
    map.put("PARAM-roll", MESSAGES.rollParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: PasswordTextBox */

    map.put("COMPONENT-PasswordTextBox", MESSAGES.passwordTextBoxComponentPallette());

    map.put("PasswordTextBox-helpString", MESSAGES.PasswordTextBoxHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Hint", MESSAGES.HintProperties());
    map.put("PROPERTY-PasswordVisible", MESSAGES.PasswordVisibleProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());


/* Methods */

    map.put("METHOD-RequestFocus", MESSAGES.RequestFocusMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Pedometer */

    map.put("COMPONENT-Pedometer", MESSAGES.pedometerComponentPallette());

    map.put("Pedometer-helpString", MESSAGES.PedometerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-CalibrateStrideLength", MESSAGES.CalibrateStrideLengthProperties());
    map.put("PROPERTY-Distance", MESSAGES.DistanceProperties());
    map.put("PROPERTY-ElapsedTime", MESSAGES.ElapsedTimeProperties());
    map.put("PROPERTY-Moving", MESSAGES.MovingProperties());
    map.put("PROPERTY-SimpleSteps", MESSAGES.SimpleStepsProperties());
    map.put("PROPERTY-StopDetectionTimeout", MESSAGES.StopDetectionTimeoutProperties());
    map.put("PROPERTY-StrideLength", MESSAGES.StrideLengthProperties());
    map.put("PROPERTY-UseGPS", MESSAGES.UseGPSProperties());
    map.put("PROPERTY-WalkSteps", MESSAGES.WalkStepsProperties());


/* Events */

    map.put("EVENT-CalibrationFailed", MESSAGES.CalibrationFailedEvents());
    map.put("EVENT-GPSAvailable", MESSAGES.GPSAvailableEvents());
    map.put("EVENT-GPSLost", MESSAGES.GPSLostEvents());
    map.put("EVENT-SimpleStep", MESSAGES.SimpleStepEvents());
    map.put("EVENT-StartedMoving", MESSAGES.StartedMovingEvents());
    map.put("EVENT-StoppedMoving", MESSAGES.StoppedMovingEvents());
    map.put("EVENT-WalkStep", MESSAGES.WalkStepEvents());


/* Methods */

    map.put("METHOD-Pause", MESSAGES.PauseMethods());
    map.put("METHOD-Reset", MESSAGES.ResetMethods());
    map.put("METHOD-Resume", MESSAGES.ResumeMethods());
    map.put("METHOD-Save", MESSAGES.SaveMethods());
    map.put("METHOD-Start", MESSAGES.StartMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());


/* Parameters */

    map.put("PARAM-simpleSteps", MESSAGES.simpleStepsParams());
    map.put("PARAM-distance", MESSAGES.distanceParams());
    map.put("PARAM-walkSteps", MESSAGES.walkStepsParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: PhoneCall */

    map.put("COMPONENT-PhoneCall", MESSAGES.phoneCallComponentPallette());

    map.put("PhoneCall-helpString", MESSAGES.PhoneCallHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-PhoneNumber", MESSAGES.PhoneNumberProperties());


/* Events */

    map.put("EVENT-IncomingCallAnswered", MESSAGES.IncomingCallAnsweredEvents());
    map.put("EVENT-PhoneCallEnded", MESSAGES.PhoneCallEndedEvents());
    map.put("EVENT-PhoneCallStarted", MESSAGES.PhoneCallStartedEvents());


/* Methods */

    map.put("METHOD-MakePhoneCall", MESSAGES.MakePhoneCallMethods());
    map.put("METHOD-MakePhoneCallDirect", MESSAGES.MakePhoneCallDirectMethods());


/* Parameters */

    map.put("PARAM-phoneNumber", MESSAGES.phoneNumberParams());
    map.put("PARAM-status", MESSAGES.statusParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: PhoneNumberPicker */

    map.put("COMPONENT-PhoneNumberPicker", MESSAGES.phoneNumberPickerComponentPallette());

    map.put("PhoneNumberPicker-helpString", MESSAGES.PhoneNumberPickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-ContactName", MESSAGES.ContactNameProperties());
    map.put("PROPERTY-ContactUri", MESSAGES.ContactUriProperties());
    map.put("PROPERTY-EmailAddress", MESSAGES.EmailAddressProperties());
    map.put("PROPERTY-EmailAddressList", MESSAGES.EmailAddressListProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-PhoneNumber", MESSAGES.PhoneNumberProperties());
    map.put("PROPERTY-PhoneNumberList", MESSAGES.PhoneNumberListProperties());
    map.put("PROPERTY-Picture", MESSAGES.PictureProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterPicking", MESSAGES.AfterPickingEvents());
    map.put("EVENT-BeforePicking", MESSAGES.BeforePickingEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-Open", MESSAGES.OpenMethods());
    map.put("METHOD-ViewContact", MESSAGES.ViewContactMethods());


/* Parameters */

    map.put("PARAM-uri", MESSAGES.uriParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: PhoneStatus */

    map.put("COMPONENT-PhoneStatus", MESSAGES.phoneStatusComponentPallette());

    map.put("PhoneStatus-helpString", MESSAGES.PhoneStatusHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-WebRTC", MESSAGES.WebRTCProperties());


/* Events */

    map.put("EVENT-OnSettings", MESSAGES.OnSettingsEvents());


/* Methods */

    map.put("METHOD-GetInstaller", MESSAGES.GetInstallerMethods());
    map.put("METHOD-GetVersionName", MESSAGES.GetVersionNameMethods());
    map.put("METHOD-GetWifiIpAddress", MESSAGES.GetWifiIpAddressMethods());
    map.put("METHOD-InstallationId", MESSAGES.InstallationIdMethods());
    map.put("METHOD-SdkLevel", MESSAGES.SdkLevelMethods());
    map.put("METHOD-doFault", MESSAGES.doFaultMethods());
    map.put("METHOD-installURL", MESSAGES.installURLMethods());
    map.put("METHOD-isConnected", MESSAGES.isConnectedMethods());
    map.put("METHOD-isDirect", MESSAGES.isDirectMethods());
    map.put("METHOD-setAssetsLoaded", MESSAGES.setAssetsLoadedMethods());
    map.put("METHOD-setHmacSeedReturnCode", MESSAGES.setHmacSeedReturnCodeMethods());
    map.put("METHOD-shutdown", MESSAGES.shutdownMethods());
    map.put("METHOD-startHTTPD", MESSAGES.startHTTPDMethods());
    map.put("METHOD-startWebRTC", MESSAGES.startWebRTCMethods());


/* Parameters */

    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-seed", MESSAGES.seedParams());
    map.put("PARAM-rendezvousServer", MESSAGES.rendezvousServerParams());
    map.put("PARAM-secure", MESSAGES.secureParams());
    map.put("PARAM-iceServers", MESSAGES.iceServersParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Player */

    map.put("COMPONENT-Player", MESSAGES.playerComponentPallette());

    map.put("Player-helpString", MESSAGES.PlayerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-IsPlaying", MESSAGES.IsPlayingProperties());
    map.put("PROPERTY-Loop", MESSAGES.LoopProperties());
    map.put("PROPERTY-PlayOnlyInForeground", MESSAGES.PlayOnlyInForegroundProperties());
    map.put("PROPERTY-Source", MESSAGES.SourceProperties());
    map.put("PROPERTY-Volume", MESSAGES.VolumeProperties());


/* Events */

    map.put("EVENT-Completed", MESSAGES.CompletedEvents());
    map.put("EVENT-OtherPlayerStarted", MESSAGES.OtherPlayerStartedEvents());


/* Methods */

    map.put("METHOD-Pause", MESSAGES.PauseMethods());
    map.put("METHOD-Start", MESSAGES.StartMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());
    map.put("METHOD-Vibrate", MESSAGES.VibrateMethods());


/* Parameters */

    map.put("PARAM-milliseconds", MESSAGES.millisecondsParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Polygon */

    map.put("COMPONENT-Polygon", MESSAGES.polygonComponentPallette());

    map.put("Polygon-helpString", MESSAGES.PolygonHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Description", MESSAGES.DescriptionProperties());
    map.put("PROPERTY-Draggable", MESSAGES.DraggableProperties());
    map.put("PROPERTY-EnableInfobox", MESSAGES.EnableInfoboxProperties());
    map.put("PROPERTY-FillColor", MESSAGES.FillColorProperties());
    map.put("PROPERTY-HolePoints", MESSAGES.HolePointsProperties());
    map.put("PROPERTY-HolePointsFromString", MESSAGES.HolePointsFromStringProperties());
    map.put("PROPERTY-Points", MESSAGES.PointsProperties());
    map.put("PROPERTY-PointsFromString", MESSAGES.PointsFromStringProperties());
    map.put("PROPERTY-StrokeColor", MESSAGES.StrokeColorProperties());
    map.put("PROPERTY-StrokeWidth", MESSAGES.StrokeWidthProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Type", MESSAGES.TypeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-Drag", MESSAGES.DragEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-StartDrag", MESSAGES.StartDragEvents());
    map.put("EVENT-StopDrag", MESSAGES.StopDragEvents());


/* Methods */

    map.put("METHOD-Centroid", MESSAGES.CentroidMethods());
    map.put("METHOD-DistanceToFeature", MESSAGES.DistanceToFeatureMethods());
    map.put("METHOD-DistanceToPoint", MESSAGES.DistanceToPointMethods());
    map.put("METHOD-HideInfobox", MESSAGES.HideInfoboxMethods());
    map.put("METHOD-ShowInfobox", MESSAGES.ShowInfoboxMethods());


/* Parameters */

    map.put("PARAM-mapFeature", MESSAGES.mapFeatureParams());
    map.put("PARAM-centroids", MESSAGES.centroidsParams());
    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-centroid", MESSAGES.centroidParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: ProximitySensor */

    map.put("COMPONENT-ProximitySensor", MESSAGES.proximitySensorComponentPallette());

    map.put("ProximitySensor-helpString", MESSAGES.ProximitySensorHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Available", MESSAGES.AvailableProperties());
    map.put("PROPERTY-Distance", MESSAGES.DistanceProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-KeepRunningWhenOnPause", MESSAGES.KeepRunningWhenOnPauseProperties());
    map.put("PROPERTY-MaximumRange", MESSAGES.MaximumRangeProperties());


/* Events */

    map.put("EVENT-ProximityChanged", MESSAGES.ProximityChangedEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-distance", MESSAGES.distanceParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Rectangle */

    map.put("COMPONENT-Rectangle", MESSAGES.rectangleComponentPallette());

    map.put("Rectangle-helpString", MESSAGES.RectangleHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Description", MESSAGES.DescriptionProperties());
    map.put("PROPERTY-Draggable", MESSAGES.DraggableProperties());
    map.put("PROPERTY-EastLongitude", MESSAGES.EastLongitudeProperties());
    map.put("PROPERTY-EnableInfobox", MESSAGES.EnableInfoboxProperties());
    map.put("PROPERTY-FillColor", MESSAGES.FillColorProperties());
    map.put("PROPERTY-NorthLatitude", MESSAGES.NorthLatitudeProperties());
    map.put("PROPERTY-SouthLatitude", MESSAGES.SouthLatitudeProperties());
    map.put("PROPERTY-StrokeColor", MESSAGES.StrokeColorProperties());
    map.put("PROPERTY-StrokeWidth", MESSAGES.StrokeWidthProperties());
    map.put("PROPERTY-Title", MESSAGES.TitleProperties());
    map.put("PROPERTY-Type", MESSAGES.TypeProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-WestLongitude", MESSAGES.WestLongitudeProperties());


/* Events */

    map.put("EVENT-Click", MESSAGES.ClickEvents());
    map.put("EVENT-Drag", MESSAGES.DragEvents());
    map.put("EVENT-LongClick", MESSAGES.LongClickEvents());
    map.put("EVENT-StartDrag", MESSAGES.StartDragEvents());
    map.put("EVENT-StopDrag", MESSAGES.StopDragEvents());


/* Methods */

    map.put("METHOD-Bounds", MESSAGES.BoundsMethods());
    map.put("METHOD-Center", MESSAGES.CenterMethods());
    map.put("METHOD-DistanceToFeature", MESSAGES.DistanceToFeatureMethods());
    map.put("METHOD-DistanceToPoint", MESSAGES.DistanceToPointMethods());
    map.put("METHOD-HideInfobox", MESSAGES.HideInfoboxMethods());
    map.put("METHOD-SetCenter", MESSAGES.SetCenterMethods());
    map.put("METHOD-ShowInfobox", MESSAGES.ShowInfoboxMethods());


/* Parameters */

    map.put("PARAM-mapFeature", MESSAGES.mapFeatureParams());
    map.put("PARAM-centroids", MESSAGES.centroidsParams());
    map.put("PARAM-latitude", MESSAGES.latitudeParams());
    map.put("PARAM-longitude", MESSAGES.longitudeParams());
    map.put("PARAM-centroid", MESSAGES.centroidParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Sharing */

    map.put("COMPONENT-Sharing", MESSAGES.sharingComponentPallette());

    map.put("Sharing-helpString", MESSAGES.SharingHelpStringComponentPallette());



/* Properties */



/* Events */



/* Methods */

    map.put("METHOD-ShareFile", MESSAGES.ShareFileMethods());
    map.put("METHOD-ShareFileWithMessage", MESSAGES.ShareFileWithMessageMethods());
    map.put("METHOD-ShareMessage", MESSAGES.ShareMessageMethods());


/* Parameters */

    map.put("PARAM-file", MESSAGES.fileParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Slider */

    map.put("COMPONENT-Slider", MESSAGES.sliderComponentPallette());

    map.put("Slider-helpString", MESSAGES.SliderHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-ColorLeft", MESSAGES.ColorLeftProperties());
    map.put("PROPERTY-ColorRight", MESSAGES.ColorRightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-MaxValue", MESSAGES.MaxValueProperties());
    map.put("PROPERTY-MinValue", MESSAGES.MinValueProperties());
    map.put("PROPERTY-ThumbEnabled", MESSAGES.ThumbEnabledProperties());
    map.put("PROPERTY-ThumbPosition", MESSAGES.ThumbPositionProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-PositionChanged", MESSAGES.PositionChangedEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-thumbPosition", MESSAGES.thumbPositionParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Sound */

    map.put("COMPONENT-Sound", MESSAGES.soundComponentPallette());

    map.put("Sound-helpString", MESSAGES.SoundHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-MinimumInterval", MESSAGES.MinimumIntervalProperties());
    map.put("PROPERTY-Source", MESSAGES.SourceProperties());


/* Events */



/* Methods */

    map.put("METHOD-Pause", MESSAGES.PauseMethods());
    map.put("METHOD-Play", MESSAGES.PlayMethods());
    map.put("METHOD-Resume", MESSAGES.ResumeMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());
    map.put("METHOD-Vibrate", MESSAGES.VibrateMethods());


/* Parameters */

    map.put("PARAM-millisecs", MESSAGES.millisecsParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: SoundRecorder */

    map.put("COMPONENT-SoundRecorder", MESSAGES.soundRecorderComponentPallette());

    map.put("SoundRecorder-helpString", MESSAGES.SoundRecorderHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-SavedRecording", MESSAGES.SavedRecordingProperties());


/* Events */

    map.put("EVENT-AfterSoundRecorded", MESSAGES.AfterSoundRecordedEvents());
    map.put("EVENT-StartedRecording", MESSAGES.StartedRecordingEvents());
    map.put("EVENT-StoppedRecording", MESSAGES.StoppedRecordingEvents());


/* Methods */

    map.put("METHOD-Start", MESSAGES.StartMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());


/* Parameters */

    map.put("PARAM-sound", MESSAGES.soundParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: SpeechRecognizer */

    map.put("COMPONENT-SpeechRecognizer", MESSAGES.speechRecognizerComponentPallette());

    map.put("SpeechRecognizer-helpString", MESSAGES.SpeechRecognizerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Result", MESSAGES.ResultProperties());
    map.put("PROPERTY-UseLegacy", MESSAGES.UseLegacyProperties());


/* Events */

    map.put("EVENT-AfterGettingText", MESSAGES.AfterGettingTextEvents());
    map.put("EVENT-BeforeGettingText", MESSAGES.BeforeGettingTextEvents());


/* Methods */

    map.put("METHOD-GetText", MESSAGES.GetTextMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());


/* Parameters */

    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-partial", MESSAGES.partialParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Spinner */

    map.put("COMPONENT-Spinner", MESSAGES.spinnerComponentPallette());

    map.put("Spinner-helpString", MESSAGES.SpinnerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Elements", MESSAGES.ElementsProperties());
    map.put("PROPERTY-ElementsFromString", MESSAGES.ElementsFromStringProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Prompt", MESSAGES.PromptProperties());
    map.put("PROPERTY-Selection", MESSAGES.SelectionProperties());
    map.put("PROPERTY-SelectionIndex", MESSAGES.SelectionIndexProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterSelecting", MESSAGES.AfterSelectingEvents());


/* Methods */

    map.put("METHOD-DisplayDropdown", MESSAGES.DisplayDropdownMethods());


/* Parameters */

    map.put("PARAM-selection", MESSAGES.selectionParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Switch */

    map.put("COMPONENT-Switch", MESSAGES.switchComponentPallette());

    map.put("Switch-helpString", MESSAGES.SwitchHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-On", MESSAGES.OnProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-ThumbColorActive", MESSAGES.ThumbColorActiveProperties());
    map.put("PROPERTY-ThumbColorInactive", MESSAGES.ThumbColorInactiveProperties());
    map.put("PROPERTY-TrackColorActive", MESSAGES.TrackColorActiveProperties());
    map.put("PROPERTY-TrackColorInactive", MESSAGES.TrackColorInactiveProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Changed", MESSAGES.ChangedEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());


/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TableArrangement */

    map.put("COMPONENT-TableArrangement", MESSAGES.tableArrangementComponentPallette());

    map.put("TableArrangement-helpString", MESSAGES.TableArrangementHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Columns", MESSAGES.ColumnsProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Rows", MESSAGES.RowsProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TextBox */

    map.put("COMPONENT-TextBox", MESSAGES.textBoxComponentPallette());

    map.put("TextBox-helpString", MESSAGES.TextBoxHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Hint", MESSAGES.HintProperties());
    map.put("PROPERTY-MultiLine", MESSAGES.MultiLineProperties());
    map.put("PROPERTY-NumbersOnly", MESSAGES.NumbersOnlyProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());


/* Methods */

    map.put("METHOD-HideKeyboard", MESSAGES.HideKeyboardMethods());
    map.put("METHOD-RequestFocus", MESSAGES.RequestFocusMethods());


/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TextToSpeech */

    map.put("COMPONENT-TextToSpeech", MESSAGES.textToSpeechComponentPallette());

    map.put("TextToSpeech-helpString", MESSAGES.TextToSpeechHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AvailableCountries", MESSAGES.AvailableCountriesProperties());
    map.put("PROPERTY-AvailableLanguages", MESSAGES.AvailableLanguagesProperties());
    map.put("PROPERTY-Country", MESSAGES.CountryProperties());
    map.put("PROPERTY-Language", MESSAGES.LanguageProperties());
    map.put("PROPERTY-Pitch", MESSAGES.PitchProperties());
    map.put("PROPERTY-Result", MESSAGES.ResultProperties());
    map.put("PROPERTY-SpeechRate", MESSAGES.SpeechRateProperties());


/* Events */

    map.put("EVENT-AfterSpeaking", MESSAGES.AfterSpeakingEvents());
    map.put("EVENT-BeforeSpeaking", MESSAGES.BeforeSpeakingEvents());


/* Methods */

    map.put("METHOD-Speak", MESSAGES.SpeakMethods());


/* Parameters */

    map.put("PARAM-result", MESSAGES.resultParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Texting */

    map.put("COMPONENT-Texting", MESSAGES.textingComponentPallette());

    map.put("Texting-helpString", MESSAGES.TextingHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-GoogleVoiceEnabled", MESSAGES.GoogleVoiceEnabledProperties());
    map.put("PROPERTY-Message", MESSAGES.MessageProperties());
    map.put("PROPERTY-PhoneNumber", MESSAGES.PhoneNumberProperties());
    map.put("PROPERTY-ReceivingEnabled", MESSAGES.ReceivingEnabledProperties());


/* Events */

    map.put("EVENT-MessageReceived", MESSAGES.MessageReceivedEvents());


/* Methods */

    map.put("METHOD-SendMessage", MESSAGES.SendMessageMethods());
    map.put("METHOD-SendMessageDirect", MESSAGES.SendMessageDirectMethods());


/* Parameters */

    map.put("PARAM-number", MESSAGES.numberParams());
    map.put("PARAM-messageText", MESSAGES.messageTextParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TimePicker */

    map.put("COMPONENT-TimePicker", MESSAGES.timePickerComponentPallette());

    map.put("TimePicker-helpString", MESSAGES.TimePickerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Enabled", MESSAGES.EnabledProperties());
    map.put("PROPERTY-FontBold", MESSAGES.FontBoldProperties());
    map.put("PROPERTY-FontItalic", MESSAGES.FontItalicProperties());
    map.put("PROPERTY-FontSize", MESSAGES.FontSizeProperties());
    map.put("PROPERTY-FontTypeface", MESSAGES.FontTypefaceProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Hour", MESSAGES.HourProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Instant", MESSAGES.InstantProperties());
    map.put("PROPERTY-Minute", MESSAGES.MinuteProperties());
    map.put("PROPERTY-Shape", MESSAGES.ShapeProperties());
    map.put("PROPERTY-ShowFeedback", MESSAGES.ShowFeedbackProperties());
    map.put("PROPERTY-Text", MESSAGES.TextProperties());
    map.put("PROPERTY-TextAlignment", MESSAGES.TextAlignmentProperties());
    map.put("PROPERTY-TextColor", MESSAGES.TextColorProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-AfterTimeSet", MESSAGES.AfterTimeSetEvents());
    map.put("EVENT-GotFocus", MESSAGES.GotFocusEvents());
    map.put("EVENT-LostFocus", MESSAGES.LostFocusEvents());
    map.put("EVENT-TouchDown", MESSAGES.TouchDownEvents());
    map.put("EVENT-TouchUp", MESSAGES.TouchUpEvents());


/* Methods */

    map.put("METHOD-LaunchPicker", MESSAGES.LaunchPickerMethods());
    map.put("METHOD-SetTimeToDisplay", MESSAGES.SetTimeToDisplayMethods());
    map.put("METHOD-SetTimeToDisplayFromInstant", MESSAGES.SetTimeToDisplayFromInstantMethods());


/* Parameters */

    map.put("PARAM-hour", MESSAGES.hourParams());
    map.put("PARAM-minute", MESSAGES.minuteParams());
    map.put("PARAM-instant", MESSAGES.instantParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TinyDB */

    map.put("COMPONENT-TinyDB", MESSAGES.tinyDBComponentPallette());

    map.put("TinyDB-helpString", MESSAGES.TinyDBHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-Namespace", MESSAGES.NamespaceProperties());


/* Events */



/* Methods */

    map.put("METHOD-ClearAll", MESSAGES.ClearAllMethods());
    map.put("METHOD-ClearTag", MESSAGES.ClearTagMethods());
    map.put("METHOD-GetTags", MESSAGES.GetTagsMethods());
    map.put("METHOD-GetValue", MESSAGES.GetValueMethods());
    map.put("METHOD-StoreValue", MESSAGES.StoreValueMethods());


/* Parameters */

    map.put("PARAM-tag", MESSAGES.tagParams());
    map.put("PARAM-valueIfTagNotThere", MESSAGES.valueIfTagNotThereParams());
    map.put("PARAM-valueToStore", MESSAGES.valueToStoreParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: TinyWebDB */

    map.put("COMPONENT-TinyWebDB", MESSAGES.tinyWebDBComponentPallette());

    map.put("TinyWebDB-helpString", MESSAGES.TinyWebDBHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-ServiceURL", MESSAGES.ServiceURLProperties());


/* Events */

    map.put("EVENT-GotValue", MESSAGES.GotValueEvents());
    map.put("EVENT-ValueStored", MESSAGES.ValueStoredEvents());
    map.put("EVENT-WebServiceError", MESSAGES.WebServiceErrorEvents());


/* Methods */

    map.put("METHOD-GetValue", MESSAGES.GetValueMethods());
    map.put("METHOD-StoreValue", MESSAGES.StoreValueMethods());


/* Parameters */

    map.put("PARAM-tagFromWebDB", MESSAGES.tagFromWebDBParams());
    map.put("PARAM-valueFromWebDB", MESSAGES.valueFromWebDBParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-tag", MESSAGES.tagParams());
    map.put("PARAM-valueToStore", MESSAGES.valueToStoreParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Twitter */

    map.put("COMPONENT-Twitter", MESSAGES.twitterComponentPallette());

    map.put("Twitter-helpString", MESSAGES.TwitterHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-ConsumerKey", MESSAGES.ConsumerKeyProperties());
    map.put("PROPERTY-ConsumerSecret", MESSAGES.ConsumerSecretProperties());
    map.put("PROPERTY-DirectMessages", MESSAGES.DirectMessagesProperties());
    map.put("PROPERTY-Followers", MESSAGES.FollowersProperties());
    map.put("PROPERTY-FriendTimeline", MESSAGES.FriendTimelineProperties());
    map.put("PROPERTY-Mentions", MESSAGES.MentionsProperties());
    map.put("PROPERTY-SearchResults", MESSAGES.SearchResultsProperties());
    map.put("PROPERTY-TwitPic_API_Key", MESSAGES.TwitPic_API_KeyProperties());
    map.put("PROPERTY-Username", MESSAGES.UsernameProperties());


/* Events */

    map.put("EVENT-DirectMessagesReceived", MESSAGES.DirectMessagesReceivedEvents());
    map.put("EVENT-FollowersReceived", MESSAGES.FollowersReceivedEvents());
    map.put("EVENT-FriendTimelineReceived", MESSAGES.FriendTimelineReceivedEvents());
    map.put("EVENT-IsAuthorized", MESSAGES.IsAuthorizedEvents());
    map.put("EVENT-MentionsReceived", MESSAGES.MentionsReceivedEvents());
    map.put("EVENT-SearchSuccessful", MESSAGES.SearchSuccessfulEvents());


/* Methods */

    map.put("METHOD-Authorize", MESSAGES.AuthorizeMethods());
    map.put("METHOD-CheckAuthorized", MESSAGES.CheckAuthorizedMethods());
    map.put("METHOD-DeAuthorize", MESSAGES.DeAuthorizeMethods());
    map.put("METHOD-DirectMessage", MESSAGES.DirectMessageMethods());
    map.put("METHOD-Follow", MESSAGES.FollowMethods());
    map.put("METHOD-RequestDirectMessages", MESSAGES.RequestDirectMessagesMethods());
    map.put("METHOD-RequestFollowers", MESSAGES.RequestFollowersMethods());
    map.put("METHOD-RequestFriendTimeline", MESSAGES.RequestFriendTimelineMethods());
    map.put("METHOD-RequestMentions", MESSAGES.RequestMentionsMethods());
    map.put("METHOD-SearchTwitter", MESSAGES.SearchTwitterMethods());
    map.put("METHOD-StopFollowing", MESSAGES.StopFollowingMethods());
    map.put("METHOD-Tweet", MESSAGES.TweetMethods());
    map.put("METHOD-TweetWithImage", MESSAGES.TweetWithImageMethods());


/* Parameters */

    map.put("PARAM-messages", MESSAGES.messagesParams());
    map.put("PARAM-followers2", MESSAGES.followers2Params());
    map.put("PARAM-timeline", MESSAGES.timelineParams());
    map.put("PARAM-mentions", MESSAGES.mentionsParams());
    map.put("PARAM-searchResults", MESSAGES.searchResultsParams());
    map.put("PARAM-user", MESSAGES.userParams());
    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-query", MESSAGES.queryParams());
    map.put("PARAM-status", MESSAGES.statusParams());
    map.put("PARAM-imagePath", MESSAGES.imagePathParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: VerticalArrangement */

    map.put("COMPONENT-VerticalArrangement", MESSAGES.verticalArrangementComponentPallette());

    map.put("VerticalArrangement-helpString", MESSAGES.VerticalArrangementHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("PROPERTY-AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: VerticalScrollArrangement */

    map.put("COMPONENT-VerticalScrollArrangement", MESSAGES.verticalScrollArrangementComponentPallette());

    map.put("VerticalScrollArrangement-helpString", MESSAGES.VerticalScrollArrangementHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AlignHorizontal", MESSAGES.AlignHorizontalProperties());
    map.put("PROPERTY-AlignVertical", MESSAGES.AlignVerticalProperties());
    map.put("PROPERTY-BackgroundColor", MESSAGES.BackgroundColorProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Image", MESSAGES.ImageProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */



/* Methods */



/* Parameters */

    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: VideoPlayer */

    map.put("COMPONENT-VideoPlayer", MESSAGES.videoPlayerComponentPallette());

    map.put("VideoPlayer-helpString", MESSAGES.VideoPlayerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-FullScreen", MESSAGES.FullScreenProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-Source", MESSAGES.SourceProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-Volume", MESSAGES.VolumeProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-Completed", MESSAGES.CompletedEvents());


/* Methods */

    map.put("METHOD-GetDuration", MESSAGES.GetDurationMethods());
    map.put("METHOD-Pause", MESSAGES.PauseMethods());
    map.put("METHOD-SeekTo", MESSAGES.SeekToMethods());
    map.put("METHOD-Start", MESSAGES.StartMethods());
    map.put("METHOD-Stop", MESSAGES.StopMethods());


/* Parameters */

    map.put("PARAM-ms", MESSAGES.msParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Voting */

    map.put("COMPONENT-Voting", MESSAGES.votingComponentPallette());

    map.put("Voting-helpString", MESSAGES.VotingHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-BallotOptions", MESSAGES.BallotOptionsProperties());
    map.put("PROPERTY-BallotQuestion", MESSAGES.BallotQuestionProperties());
    map.put("PROPERTY-ServiceURL", MESSAGES.ServiceURLProperties());
    map.put("PROPERTY-UserChoice", MESSAGES.UserChoiceProperties());
    map.put("PROPERTY-UserEmailAddress", MESSAGES.UserEmailAddressProperties());
    map.put("PROPERTY-UserId", MESSAGES.UserIdProperties());


/* Events */

    map.put("EVENT-GotBallot", MESSAGES.GotBallotEvents());
    map.put("EVENT-GotBallotConfirmation", MESSAGES.GotBallotConfirmationEvents());
    map.put("EVENT-NoOpenPoll", MESSAGES.NoOpenPollEvents());
    map.put("EVENT-WebServiceError", MESSAGES.WebServiceErrorEvents());


/* Methods */

    map.put("METHOD-RequestBallot", MESSAGES.RequestBallotMethods());
    map.put("METHOD-SendBallot", MESSAGES.SendBallotMethods());


/* Parameters */

    map.put("PARAM-message", MESSAGES.messageParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: Web */

    map.put("COMPONENT-Web", MESSAGES.webComponentPallette());

    map.put("Web-helpString", MESSAGES.WebHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-AllowCookies", MESSAGES.AllowCookiesProperties());
    map.put("PROPERTY-RequestHeaders", MESSAGES.RequestHeadersProperties());
    map.put("PROPERTY-ResponseFileName", MESSAGES.ResponseFileNameProperties());
    map.put("PROPERTY-SaveResponse", MESSAGES.SaveResponseProperties());
    map.put("PROPERTY-Url", MESSAGES.UrlProperties());


/* Events */

    map.put("EVENT-GotFile", MESSAGES.GotFileEvents());
    map.put("EVENT-GotText", MESSAGES.GotTextEvents());


/* Methods */

    map.put("METHOD-BuildRequestData", MESSAGES.BuildRequestDataMethods());
    map.put("METHOD-ClearCookies", MESSAGES.ClearCookiesMethods());
    map.put("METHOD-Delete", MESSAGES.DeleteMethods());
    map.put("METHOD-Get", MESSAGES.GetMethods());
    map.put("METHOD-HtmlTextDecode", MESSAGES.HtmlTextDecodeMethods());
    map.put("METHOD-JsonTextDecode", MESSAGES.JsonTextDecodeMethods());
    map.put("METHOD-PostFile", MESSAGES.PostFileMethods());
    map.put("METHOD-PostText", MESSAGES.PostTextMethods());
    map.put("METHOD-PostTextWithEncoding", MESSAGES.PostTextWithEncodingMethods());
    map.put("METHOD-PutFile", MESSAGES.PutFileMethods());
    map.put("METHOD-PutText", MESSAGES.PutTextMethods());
    map.put("METHOD-PutTextWithEncoding", MESSAGES.PutTextWithEncodingMethods());
    map.put("METHOD-UriDecode", MESSAGES.UriDecodeMethods());
    map.put("METHOD-UriEncode", MESSAGES.UriEncodeMethods());
    map.put("METHOD-XMLTextDecode", MESSAGES.XMLTextDecodeMethods());


/* Parameters */

    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-responseCode", MESSAGES.responseCodeParams());
    map.put("PARAM-responseType", MESSAGES.responseTypeParams());
    map.put("PARAM-fileName", MESSAGES.fileNameParams());
    map.put("PARAM-responseContent", MESSAGES.responseContentParams());
    map.put("PARAM-list", MESSAGES.listParams());
    map.put("PARAM-htmlText", MESSAGES.htmlTextParams());
    map.put("PARAM-jsonText", MESSAGES.jsonTextParams());
    map.put("PARAM-path", MESSAGES.pathParams());
    map.put("PARAM-text", MESSAGES.textParams());
    map.put("PARAM-encoding", MESSAGES.encodingParams());
    map.put("PARAM-XmlText", MESSAGES.xmlTextParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: WebViewer */

    map.put("COMPONENT-WebViewer", MESSAGES.webViewerComponentPallette());

    map.put("WebViewer-helpString", MESSAGES.WebViewerHelpStringComponentPallette());



/* Properties */

    map.put("PROPERTY-CurrentPageTitle", MESSAGES.CurrentPageTitleProperties());
    map.put("PROPERTY-CurrentUrl", MESSAGES.CurrentUrlProperties());
    map.put("PROPERTY-FollowLinks", MESSAGES.FollowLinksProperties());
    map.put("PROPERTY-Height", MESSAGES.HeightProperties());
    map.put("PROPERTY-HeightPercent", MESSAGES.HeightPercentProperties());
    map.put("PROPERTY-HomeUrl", MESSAGES.HomeUrlProperties());
    map.put("PROPERTY-IgnoreSslErrors", MESSAGES.IgnoreSslErrorsProperties());
    map.put("PROPERTY-PromptforPermission", MESSAGES.PromptforPermissionProperties());
    map.put("PROPERTY-UsesLocation", MESSAGES.UsesLocationProperties());
    map.put("PROPERTY-Visible", MESSAGES.VisibleProperties());
    map.put("PROPERTY-WebViewString", MESSAGES.WebViewStringProperties());
    map.put("PROPERTY-Width", MESSAGES.WidthProperties());
    map.put("PROPERTY-WidthPercent", MESSAGES.WidthPercentProperties());


/* Events */

    map.put("EVENT-WebViewStringChange", MESSAGES.WebViewStringChangeEvents());


/* Methods */

    map.put("METHOD-CanGoBack", MESSAGES.CanGoBackMethods());
    map.put("METHOD-CanGoForward", MESSAGES.CanGoForwardMethods());
    map.put("METHOD-ClearCaches", MESSAGES.ClearCachesMethods());
    map.put("METHOD-ClearLocations", MESSAGES.ClearLocationsMethods());
    map.put("METHOD-GoBack", MESSAGES.GoBackMethods());
    map.put("METHOD-GoForward", MESSAGES.GoForwardMethods());
    map.put("METHOD-GoHome", MESSAGES.GoHomeMethods());
    map.put("METHOD-GoToUrl", MESSAGES.GoToUrlMethods());


/* Parameters */

    map.put("PARAM-value", MESSAGES.valueParams());
    map.put("PARAM-url", MESSAGES.urlParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


/* Component: YandexTranslate */

    map.put("COMPONENT-YandexTranslate", MESSAGES.yandexTranslateComponentPallette());

    map.put("YandexTranslate-helpString", MESSAGES.YandexTranslateHelpStringComponentPallette());



/* Properties */



/* Events */

    map.put("EVENT-GotTranslation", MESSAGES.GotTranslationEvents());


/* Methods */

    map.put("METHOD-RequestTranslation", MESSAGES.RequestTranslationMethods());


/* Parameters */

    map.put("PARAM-responseCode", MESSAGES.responseCodeParams());
    map.put("PARAM-translation", MESSAGES.translationParams());
    map.put("PARAM-languageToTranslateTo", MESSAGES.languageToTranslateToParams());
    map.put("PARAM-textToTranslate", MESSAGES.textToTranslateParams());
    map.put("PARAM-notAlreadyHandled", MESSAGES.notAlreadyHandledParams());


    /* Descriptions */

    map.put("PROPDESC-AboutScreen", MESSAGES.AboutScreenPropertyDescriptions());
    map.put("PROPDESC-AboveRangeEventEnabled", MESSAGES.AboveRangeEventEnabledPropertyDescriptions());
    map.put("PROPDESC-AccentColor", MESSAGES.AccentColorPropertyDescriptions());
    map.put("PROPDESC-Accuracy", MESSAGES.AccuracyPropertyDescriptions());
    map.put("PROPDESC-Action", MESSAGES.ActionPropertyDescriptions());
    map.put("PROPDESC-ActionBar", MESSAGES.ActionBarPropertyDescriptions());
    map.put("PROPDESC-ActivityClass", MESSAGES.ActivityClassPropertyDescriptions());
    map.put("PROPDESC-ActivityPackage", MESSAGES.ActivityPackagePropertyDescriptions());
    map.put("PROPDESC-AddressesAndNames", MESSAGES.AddressesAndNamesPropertyDescriptions());
    map.put("PROPDESC-AlignHorizontal", MESSAGES.AlignHorizontalPropertyDescriptions());
    map.put("PROPDESC-AlignVertical", MESSAGES.AlignVerticalPropertyDescriptions());
    map.put("PROPDESC-AllowCookies", MESSAGES.AllowCookiesPropertyDescriptions());
    map.put("PROPDESC-Altitude", MESSAGES.AltitudePropertyDescriptions());
    map.put("PROPDESC-AnchorHorizontal", MESSAGES.AnchorHorizontalPropertyDescriptions());
    map.put("PROPDESC-AnchorVertical", MESSAGES.AnchorVerticalPropertyDescriptions());
    map.put("PROPDESC-Angle", MESSAGES.AnglePropertyDescriptions());
    map.put("PROPDESC-Animation", MESSAGES.AnimationPropertyDescriptions());
    map.put("PROPDESC-ApiKey", MESSAGES.ApiKeyPropertyDescriptions());
    map.put("PROPDESC-AppName", MESSAGES.AppNamePropertyDescriptions());
    map.put("PROPDESC-Available", MESSAGES.AvailablePropertyDescriptions());
    map.put("PROPDESC-AvailableCountries", MESSAGES.AvailableCountriesPropertyDescriptions());
    map.put("PROPDESC-AvailableLanguages", MESSAGES.AvailableLanguagesPropertyDescriptions());
    map.put("PROPDESC-AvailableProviders", MESSAGES.AvailableProvidersPropertyDescriptions());
    map.put("PROPDESC-Azimuth", MESSAGES.AzimuthPropertyDescriptions());
    map.put("PROPDESC-BackgroundColor", MESSAGES.BackgroundColorPropertyDescriptions());
    map.put("PROPDESC-BackgroundImage", MESSAGES.BackgroundImagePropertyDescriptions());
    map.put("PROPDESC-BallotOptions", MESSAGES.BallotOptionsPropertyDescriptions());
    map.put("PROPDESC-BallotQuestion", MESSAGES.BallotQuestionPropertyDescriptions());
    map.put("PROPDESC-BelowRangeEventEnabled", MESSAGES.BelowRangeEventEnabledPropertyDescriptions());
    map.put("PROPDESC-BlocksToolkit", MESSAGES.BlocksToolkitPropertyDescriptions());
    map.put("PROPDESC-BluetoothClient", MESSAGES.BluetoothClientPropertyDescriptions());
    map.put("PROPDESC-BottomOfRange", MESSAGES.BottomOfRangePropertyDescriptions());
    map.put("PROPDESC-BoundingBox", MESSAGES.BoundingBoxPropertyDescriptions());
    map.put("PROPDESC-CalibrateStrideLength", MESSAGES.CalibrateStrideLengthPropertyDescriptions());
    map.put("PROPDESC-CenterFromString", MESSAGES.CenterFromStringPropertyDescriptions());
    map.put("PROPDESC-CharacterEncoding", MESSAGES.CharacterEncodingPropertyDescriptions());
    map.put("PROPDESC-Checked", MESSAGES.CheckedPropertyDescriptions());
    map.put("PROPDESC-CloseScreenAnimation", MESSAGES.CloseScreenAnimationPropertyDescriptions());
    map.put("PROPDESC-ColorChangedEventEnabled", MESSAGES.ColorChangedEventEnabledPropertyDescriptions());
    map.put("PROPDESC-ColorLeft", MESSAGES.ColorLeftPropertyDescriptions());
    map.put("PROPDESC-ColorRight", MESSAGES.ColorRightPropertyDescriptions());
    map.put("PROPDESC-Columns", MESSAGES.ColumnsPropertyDescriptions());
    map.put("PROPDESC-ConsumerKey", MESSAGES.ConsumerKeyPropertyDescriptions());
    map.put("PROPDESC-ConsumerSecret", MESSAGES.ConsumerSecretPropertyDescriptions());
    map.put("PROPDESC-ContactName", MESSAGES.ContactNamePropertyDescriptions());
    map.put("PROPDESC-ContactUri", MESSAGES.ContactUriPropertyDescriptions());
    map.put("PROPDESC-Country", MESSAGES.CountryPropertyDescriptions());
    map.put("PROPDESC-CurrentAddress", MESSAGES.CurrentAddressPropertyDescriptions());
    map.put("PROPDESC-CurrentPageTitle", MESSAGES.CurrentPageTitlePropertyDescriptions());
    map.put("PROPDESC-CurrentUrl", MESSAGES.CurrentUrlPropertyDescriptions());
    map.put("PROPDESC-DataType", MESSAGES.DataTypePropertyDescriptions());
    map.put("PROPDESC-DataUri", MESSAGES.DataUriPropertyDescriptions());
    map.put("PROPDESC-Day", MESSAGES.DayPropertyDescriptions());
    map.put("PROPDESC-DefaultRedisServer", MESSAGES.DefaultRedisServerPropertyDescriptions());
    map.put("PROPDESC-DefaultURL", MESSAGES.DefaultURLPropertyDescriptions());
    map.put("PROPDESC-DelimiterByte", MESSAGES.DelimiterBytePropertyDescriptions());
    map.put("PROPDESC-Description", MESSAGES.DescriptionPropertyDescriptions());
    map.put("PROPDESC-DetectColor", MESSAGES.DetectColorPropertyDescriptions());
    map.put("PROPDESC-DeveloperBucket", MESSAGES.DeveloperBucketPropertyDescriptions());
    map.put("PROPDESC-DirectMessages", MESSAGES.DirectMessagesPropertyDescriptions());
    map.put("PROPDESC-Distance", MESSAGES.DistancePropertyDescriptions());
    map.put("PROPDESC-DistanceInterval", MESSAGES.DistanceIntervalPropertyDescriptions());
    map.put("PROPDESC-Draggable", MESSAGES.DraggablePropertyDescriptions());
    map.put("PROPDESC-DriveMotors", MESSAGES.DriveMotorsPropertyDescriptions());
    map.put("PROPDESC-EastLongitude", MESSAGES.EastLongitudePropertyDescriptions());
    map.put("PROPDESC-ElapsedTime", MESSAGES.ElapsedTimePropertyDescriptions());
    map.put("PROPDESC-Elements", MESSAGES.ElementsPropertyDescriptions());
    map.put("PROPDESC-ElementsFromString", MESSAGES.ElementsFromStringPropertyDescriptions());
    map.put("PROPDESC-EmailAddress", MESSAGES.EmailAddressPropertyDescriptions());
    map.put("PROPDESC-EmailAddressList", MESSAGES.EmailAddressListPropertyDescriptions());
    map.put("PROPDESC-EnableInfobox", MESSAGES.EnableInfoboxPropertyDescriptions());
    map.put("PROPDESC-EnablePan", MESSAGES.EnablePanPropertyDescriptions());
    map.put("PROPDESC-EnableRotation", MESSAGES.EnableRotationPropertyDescriptions());
    map.put("PROPDESC-EnableSpeedRegulation", MESSAGES.EnableSpeedRegulationPropertyDescriptions());
    map.put("PROPDESC-EnableZoom", MESSAGES.EnableZoomPropertyDescriptions());
    map.put("PROPDESC-Enabled", MESSAGES.EnabledPropertyDescriptions());
    map.put("PROPDESC-ExtraKey", MESSAGES.ExtraKeyPropertyDescriptions());
    map.put("PROPDESC-ExtraValue", MESSAGES.ExtraValuePropertyDescriptions());
    map.put("PROPDESC-Extras", MESSAGES.ExtrasPropertyDescriptions());
    map.put("PROPDESC-Features", MESSAGES.FeaturesPropertyDescriptions());
    map.put("PROPDESC-FeaturesFromGeoJSON", MESSAGES.FeaturesFromGeoJSONPropertyDescriptions());
    map.put("PROPDESC-FillColor", MESSAGES.FillColorPropertyDescriptions());
    map.put("PROPDESC-FirebaseToken", MESSAGES.FirebaseTokenPropertyDescriptions());
    map.put("PROPDESC-FirebaseURL", MESSAGES.FirebaseURLPropertyDescriptions());
    map.put("PROPDESC-FollowLinks", MESSAGES.FollowLinksPropertyDescriptions());
    map.put("PROPDESC-Followers", MESSAGES.FollowersPropertyDescriptions());
    map.put("PROPDESC-FontBold", MESSAGES.FontBoldPropertyDescriptions());
    map.put("PROPDESC-FontItalic", MESSAGES.FontItalicPropertyDescriptions());
    map.put("PROPDESC-FontSize", MESSAGES.FontSizePropertyDescriptions());
    map.put("PROPDESC-FontTypeface", MESSAGES.FontTypefacePropertyDescriptions());
    map.put("PROPDESC-FriendTimeline", MESSAGES.FriendTimelinePropertyDescriptions());
    map.put("PROPDESC-FullScreen", MESSAGES.FullScreenPropertyDescriptions());
    map.put("PROPDESC-GameId", MESSAGES.GameIdPropertyDescriptions());
    map.put("PROPDESC-GenerateColor", MESSAGES.GenerateColorPropertyDescriptions());
    map.put("PROPDESC-GenerateLight", MESSAGES.GenerateLightPropertyDescriptions());
    map.put("PROPDESC-GoogleVoiceEnabled", MESSAGES.GoogleVoiceEnabledPropertyDescriptions());
    map.put("PROPDESC-HTMLFormat", MESSAGES.HTMLFormatPropertyDescriptions());
    map.put("PROPDESC-HasAccuracy", MESSAGES.HasAccuracyPropertyDescriptions());
    map.put("PROPDESC-HasAltitude", MESSAGES.HasAltitudePropertyDescriptions());
    map.put("PROPDESC-HasLongitudeLatitude", MESSAGES.HasLongitudeLatitudePropertyDescriptions());
    map.put("PROPDESC-HasMargins", MESSAGES.HasMarginsPropertyDescriptions());
    map.put("PROPDESC-Heading", MESSAGES.HeadingPropertyDescriptions());
    map.put("PROPDESC-Height", MESSAGES.HeightPropertyDescriptions());
    map.put("PROPDESC-HeightPercent", MESSAGES.HeightPercentPropertyDescriptions());
    map.put("PROPDESC-HighByteFirst", MESSAGES.HighByteFirstPropertyDescriptions());
    map.put("PROPDESC-Hint", MESSAGES.HintPropertyDescriptions());
    map.put("PROPDESC-HolePoints", MESSAGES.HolePointsPropertyDescriptions());
    map.put("PROPDESC-HolePointsFromString", MESSAGES.HolePointsFromStringPropertyDescriptions());
    map.put("PROPDESC-HomeUrl", MESSAGES.HomeUrlPropertyDescriptions());
    map.put("PROPDESC-Hour", MESSAGES.HourPropertyDescriptions());
    map.put("PROPDESC-Icon", MESSAGES.IconPropertyDescriptions());
    map.put("PROPDESC-IgnoreSslErrors", MESSAGES.IgnoreSslErrorsPropertyDescriptions());
    map.put("PROPDESC-Image", MESSAGES.ImagePropertyDescriptions());
    map.put("PROPDESC-ImageAsset", MESSAGES.ImageAssetPropertyDescriptions());
    map.put("PROPDESC-InstanceId", MESSAGES.InstanceIdPropertyDescriptions());
    map.put("PROPDESC-Instant", MESSAGES.InstantPropertyDescriptions());
    map.put("PROPDESC-Interval", MESSAGES.IntervalPropertyDescriptions());
    map.put("PROPDESC-InvitedInstances", MESSAGES.InvitedInstancesPropertyDescriptions());
    map.put("PROPDESC-IsAccepting", MESSAGES.IsAcceptingPropertyDescriptions());
    map.put("PROPDESC-IsConnected", MESSAGES.IsConnectedPropertyDescriptions());
    map.put("PROPDESC-IsPlaying", MESSAGES.IsPlayingPropertyDescriptions());
    map.put("PROPDESC-ItemBackgroundColor", MESSAGES.ItemBackgroundColorPropertyDescriptions());
    map.put("PROPDESC-ItemTextColor", MESSAGES.ItemTextColorPropertyDescriptions());
    map.put("PROPDESC-JoinedInstances", MESSAGES.JoinedInstancesPropertyDescriptions());
    map.put("PROPDESC-KeepRunningWhenOnPause", MESSAGES.KeepRunningWhenOnPausePropertyDescriptions());
    map.put("PROPDESC-KeyFile", MESSAGES.KeyFilePropertyDescriptions());
    map.put("PROPDESC-Language", MESSAGES.LanguagePropertyDescriptions());
    map.put("PROPDESC-LastMessage", MESSAGES.LastMessagePropertyDescriptions());
    map.put("PROPDESC-Latitude", MESSAGES.LatitudePropertyDescriptions());
    map.put("PROPDESC-Leader", MESSAGES.LeaderPropertyDescriptions());
    map.put("PROPDESC-LegacyMode", MESSAGES.LegacyModePropertyDescriptions());
    map.put("PROPDESC-LineWidth", MESSAGES.LineWidthPropertyDescriptions());
    map.put("PROPDESC-LoadingDialogMessage", MESSAGES.LoadingDialogMessagePropertyDescriptions());
    map.put("PROPDESC-LocationSensor", MESSAGES.LocationSensorPropertyDescriptions());
    map.put("PROPDESC-Longitude", MESSAGES.LongitudePropertyDescriptions());
    map.put("PROPDESC-Loop", MESSAGES.LoopPropertyDescriptions());
    map.put("PROPDESC-Magnitude", MESSAGES.MagnitudePropertyDescriptions());
    map.put("PROPDESC-MapType", MESSAGES.MapTypePropertyDescriptions());
    map.put("PROPDESC-MaxValue", MESSAGES.MaxValuePropertyDescriptions());
    map.put("PROPDESC-MaximumRange", MESSAGES.MaximumRangePropertyDescriptions());
    map.put("PROPDESC-Mentions", MESSAGES.MentionsPropertyDescriptions());
    map.put("PROPDESC-Message", MESSAGES.MessagePropertyDescriptions());
    map.put("PROPDESC-MinValue", MESSAGES.MinValuePropertyDescriptions());
    map.put("PROPDESC-MinimumInterval", MESSAGES.MinimumIntervalPropertyDescriptions());
    map.put("PROPDESC-Minute", MESSAGES.MinutePropertyDescriptions());
    map.put("PROPDESC-Mode", MESSAGES.ModePropertyDescriptions());
    map.put("PROPDESC-Month", MESSAGES.MonthPropertyDescriptions());
    map.put("PROPDESC-MonthInText", MESSAGES.MonthInTextPropertyDescriptions());
    map.put("PROPDESC-MotorPorts", MESSAGES.MotorPortsPropertyDescriptions());
    map.put("PROPDESC-Moving", MESSAGES.MovingPropertyDescriptions());
    map.put("PROPDESC-MultiLine", MESSAGES.MultiLinePropertyDescriptions());
    map.put("PROPDESC-Namespace", MESSAGES.NamespacePropertyDescriptions());
    map.put("PROPDESC-NorthLatitude", MESSAGES.NorthLatitudePropertyDescriptions());
    map.put("PROPDESC-NotifierLength", MESSAGES.NotifierLengthPropertyDescriptions());
    map.put("PROPDESC-NumbersOnly", MESSAGES.NumbersOnlyPropertyDescriptions());
    map.put("PROPDESC-On", MESSAGES.OnPropertyDescriptions());
    map.put("PROPDESC-OpenScreenAnimation", MESSAGES.OpenScreenAnimationPropertyDescriptions());
    map.put("PROPDESC-PaintColor", MESSAGES.PaintColorPropertyDescriptions());
    map.put("PROPDESC-PasswordVisible", MESSAGES.PasswordVisiblePropertyDescriptions());
    map.put("PROPDESC-Persist", MESSAGES.PersistPropertyDescriptions());
    map.put("PROPDESC-PhoneNumber", MESSAGES.PhoneNumberPropertyDescriptions());
    map.put("PROPDESC-PhoneNumberList", MESSAGES.PhoneNumberListPropertyDescriptions());
    map.put("PROPDESC-Picture", MESSAGES.PicturePropertyDescriptions());
    map.put("PROPDESC-Pitch", MESSAGES.PitchPropertyDescriptions());
    map.put("PROPDESC-PlayOnlyInForeground", MESSAGES.PlayOnlyInForegroundPropertyDescriptions());
    map.put("PROPDESC-Players", MESSAGES.PlayersPropertyDescriptions());
    map.put("PROPDESC-Points", MESSAGES.PointsPropertyDescriptions());
    map.put("PROPDESC-PointsFromString", MESSAGES.PointsFromStringPropertyDescriptions());
    map.put("PROPDESC-PressedEventEnabled", MESSAGES.PressedEventEnabledPropertyDescriptions());
    map.put("PROPDESC-PrimaryColor", MESSAGES.PrimaryColorPropertyDescriptions());
    map.put("PROPDESC-PrimaryColorDark", MESSAGES.PrimaryColorDarkPropertyDescriptions());
    map.put("PROPDESC-ProjectBucket", MESSAGES.ProjectBucketPropertyDescriptions());
    map.put("PROPDESC-ProjectID", MESSAGES.ProjectIDPropertyDescriptions());
    map.put("PROPDESC-Prompt", MESSAGES.PromptPropertyDescriptions());
    map.put("PROPDESC-PromptforPermission", MESSAGES.PromptforPermissionPropertyDescriptions());
    map.put("PROPDESC-ProviderLocked", MESSAGES.ProviderLockedPropertyDescriptions());
    map.put("PROPDESC-ProviderName", MESSAGES.ProviderNamePropertyDescriptions());
    map.put("PROPDESC-PublicInstances", MESSAGES.PublicInstancesPropertyDescriptions());
    map.put("PROPDESC-Query", MESSAGES.QueryPropertyDescriptions());
    map.put("PROPDESC-Radius", MESSAGES.RadiusPropertyDescriptions());
    map.put("PROPDESC-ReadMode", MESSAGES.ReadModePropertyDescriptions());
    map.put("PROPDESC-ReceivingEnabled", MESSAGES.ReceivingEnabledPropertyDescriptions());
    map.put("PROPDESC-RedisPort", MESSAGES.RedisPortPropertyDescriptions());
    map.put("PROPDESC-RedisServer", MESSAGES.RedisServerPropertyDescriptions());
    map.put("PROPDESC-ReleasedEventEnabled", MESSAGES.ReleasedEventEnabledPropertyDescriptions());
    map.put("PROPDESC-RequestHeaders", MESSAGES.RequestHeadersPropertyDescriptions());
    map.put("PROPDESC-ResponseFileName", MESSAGES.ResponseFileNamePropertyDescriptions());
    map.put("PROPDESC-Result", MESSAGES.ResultPropertyDescriptions());
    map.put("PROPDESC-ResultName", MESSAGES.ResultNamePropertyDescriptions());
    map.put("PROPDESC-ResultType", MESSAGES.ResultTypePropertyDescriptions());
    map.put("PROPDESC-ResultUri", MESSAGES.ResultUriPropertyDescriptions());
    map.put("PROPDESC-ReverseDirection", MESSAGES.ReverseDirectionPropertyDescriptions());
    map.put("PROPDESC-Roll", MESSAGES.RollPropertyDescriptions());
    map.put("PROPDESC-Rotates", MESSAGES.RotatesPropertyDescriptions());
    map.put("PROPDESC-Rotation", MESSAGES.RotationPropertyDescriptions());
    map.put("PROPDESC-RotationAngle", MESSAGES.RotationAnglePropertyDescriptions());
    map.put("PROPDESC-Rows", MESSAGES.RowsPropertyDescriptions());
    map.put("PROPDESC-SaveResponse", MESSAGES.SaveResponsePropertyDescriptions());
    map.put("PROPDESC-SavedRecording", MESSAGES.SavedRecordingPropertyDescriptions());
    map.put("PROPDESC-ScalePictureToFit", MESSAGES.ScalePictureToFitPropertyDescriptions());
    map.put("PROPDESC-ScaleUnits", MESSAGES.ScaleUnitsPropertyDescriptions());
    map.put("PROPDESC-Scaling", MESSAGES.ScalingPropertyDescriptions());
    map.put("PROPDESC-ScreenOrientation", MESSAGES.ScreenOrientationPropertyDescriptions());
    map.put("PROPDESC-Scrollable", MESSAGES.ScrollablePropertyDescriptions());
    map.put("PROPDESC-SearchResults", MESSAGES.SearchResultsPropertyDescriptions());
    map.put("PROPDESC-Secure", MESSAGES.SecurePropertyDescriptions());
    map.put("PROPDESC-Selection", MESSAGES.SelectionPropertyDescriptions());
    map.put("PROPDESC-SelectionColor", MESSAGES.SelectionColorPropertyDescriptions());
    map.put("PROPDESC-SelectionIndex", MESSAGES.SelectionIndexPropertyDescriptions());
    map.put("PROPDESC-Sensitivity", MESSAGES.SensitivityPropertyDescriptions());
    map.put("PROPDESC-SensorPort", MESSAGES.SensorPortPropertyDescriptions());
    map.put("PROPDESC-SensorValueChangedEventEnabled", MESSAGES.SensorValueChangedEventEnabledPropertyDescriptions());
    map.put("PROPDESC-ServiceAccountEmail", MESSAGES.ServiceAccountEmailPropertyDescriptions());
    map.put("PROPDESC-ServiceURL", MESSAGES.ServiceURLPropertyDescriptions());
    map.put("PROPDESC-ServiceUrl", MESSAGES.ServiceUrlPropertyDescriptions());
    map.put("PROPDESC-Shape", MESSAGES.ShapePropertyDescriptions());
    map.put("PROPDESC-ShowCompass", MESSAGES.ShowCompassPropertyDescriptions());
    map.put("PROPDESC-ShowFeedback", MESSAGES.ShowFeedbackPropertyDescriptions());
    map.put("PROPDESC-ShowFilterBar", MESSAGES.ShowFilterBarPropertyDescriptions());
    map.put("PROPDESC-ShowListsAsJson", MESSAGES.ShowListsAsJsonPropertyDescriptions());
    map.put("PROPDESC-ShowLoadingDialog", MESSAGES.ShowLoadingDialogPropertyDescriptions());
    map.put("PROPDESC-ShowScale", MESSAGES.ShowScalePropertyDescriptions());
    map.put("PROPDESC-ShowStatusBar", MESSAGES.ShowStatusBarPropertyDescriptions());
    map.put("PROPDESC-ShowUser", MESSAGES.ShowUserPropertyDescriptions());
    map.put("PROPDESC-ShowZoom", MESSAGES.ShowZoomPropertyDescriptions());
    map.put("PROPDESC-SimpleSteps", MESSAGES.SimpleStepsPropertyDescriptions());
    map.put("PROPDESC-Sizing", MESSAGES.SizingPropertyDescriptions());
    map.put("PROPDESC-Source", MESSAGES.SourcePropertyDescriptions());
    map.put("PROPDESC-SouthLatitude", MESSAGES.SouthLatitudePropertyDescriptions());
    map.put("PROPDESC-SpeechRate", MESSAGES.SpeechRatePropertyDescriptions());
    map.put("PROPDESC-Speed", MESSAGES.SpeedPropertyDescriptions());
    map.put("PROPDESC-StopBeforeDisconnect", MESSAGES.StopBeforeDisconnectPropertyDescriptions());
    map.put("PROPDESC-StopDetectionTimeout", MESSAGES.StopDetectionTimeoutPropertyDescriptions());
    map.put("PROPDESC-StrideLength", MESSAGES.StrideLengthPropertyDescriptions());
    map.put("PROPDESC-StrokeColor", MESSAGES.StrokeColorPropertyDescriptions());
    map.put("PROPDESC-StrokeWidth", MESSAGES.StrokeWidthPropertyDescriptions());
    map.put("PROPDESC-TachoCountChangedEventEnabled", MESSAGES.TachoCountChangedEventEnabledPropertyDescriptions());
    map.put("PROPDESC-Text", MESSAGES.TextPropertyDescriptions());
    map.put("PROPDESC-TextAlignment", MESSAGES.TextAlignmentPropertyDescriptions());
    map.put("PROPDESC-TextColor", MESSAGES.TextColorPropertyDescriptions());
    map.put("PROPDESC-TextSize", MESSAGES.TextSizePropertyDescriptions());
    map.put("PROPDESC-TextToWrite", MESSAGES.TextToWritePropertyDescriptions());
    map.put("PROPDESC-Theme", MESSAGES.ThemePropertyDescriptions());
    map.put("PROPDESC-ThumbColorActive", MESSAGES.ThumbColorActivePropertyDescriptions());
    map.put("PROPDESC-ThumbColorInactive", MESSAGES.ThumbColorInactivePropertyDescriptions());
    map.put("PROPDESC-ThumbEnabled", MESSAGES.ThumbEnabledPropertyDescriptions());
    map.put("PROPDESC-ThumbPosition", MESSAGES.ThumbPositionPropertyDescriptions());
    map.put("PROPDESC-TimeInterval", MESSAGES.TimeIntervalPropertyDescriptions());
    map.put("PROPDESC-TimerAlwaysFires", MESSAGES.TimerAlwaysFiresPropertyDescriptions());
    map.put("PROPDESC-TimerEnabled", MESSAGES.TimerEnabledPropertyDescriptions());
    map.put("PROPDESC-TimerInterval", MESSAGES.TimerIntervalPropertyDescriptions());
    map.put("PROPDESC-Title", MESSAGES.TitlePropertyDescriptions());
    map.put("PROPDESC-TitleVisible", MESSAGES.TitleVisiblePropertyDescriptions());
    map.put("PROPDESC-Token", MESSAGES.TokenPropertyDescriptions());
    map.put("PROPDESC-TopOfRange", MESSAGES.TopOfRangePropertyDescriptions());
    map.put("PROPDESC-TrackColorActive", MESSAGES.TrackColorActivePropertyDescriptions());
    map.put("PROPDESC-TrackColorInactive", MESSAGES.TrackColorInactivePropertyDescriptions());
    map.put("PROPDESC-TutorialURL", MESSAGES.TutorialURLPropertyDescriptions());
    map.put("PROPDESC-TwitPic_API_Key", MESSAGES.TwitPic_API_KeyPropertyDescriptions());
    map.put("PROPDESC-Type", MESSAGES.TypePropertyDescriptions());
    map.put("PROPDESC-Unit", MESSAGES.UnitPropertyDescriptions());
    map.put("PROPDESC-Url", MESSAGES.UrlPropertyDescriptions());
    map.put("PROPDESC-UseExternalScanner", MESSAGES.UseExternalScannerPropertyDescriptions());
    map.put("PROPDESC-UseFront", MESSAGES.UseFrontPropertyDescriptions());
    map.put("PROPDESC-UseGPS", MESSAGES.UseGPSPropertyDescriptions());
    map.put("PROPDESC-UseLegacy", MESSAGES.UseLegacyPropertyDescriptions());
    map.put("PROPDESC-UseSSL", MESSAGES.UseSSLPropertyDescriptions());
    map.put("PROPDESC-UseServiceAuthentication", MESSAGES.UseServiceAuthenticationPropertyDescriptions());
    map.put("PROPDESC-UserChoice", MESSAGES.UserChoicePropertyDescriptions());
    map.put("PROPDESC-UserEmailAddress", MESSAGES.UserEmailAddressPropertyDescriptions());
    map.put("PROPDESC-UserId", MESSAGES.UserIdPropertyDescriptions());
    map.put("PROPDESC-UserLatitude", MESSAGES.UserLatitudePropertyDescriptions());
    map.put("PROPDESC-UserLongitude", MESSAGES.UserLongitudePropertyDescriptions());
    map.put("PROPDESC-Username", MESSAGES.UsernamePropertyDescriptions());
    map.put("PROPDESC-UsesLocation", MESSAGES.UsesLocationPropertyDescriptions());
    map.put("PROPDESC-VersionCode", MESSAGES.VersionCodePropertyDescriptions());
    map.put("PROPDESC-VersionName", MESSAGES.VersionNamePropertyDescriptions());
    map.put("PROPDESC-Visible", MESSAGES.VisiblePropertyDescriptions());
    map.put("PROPDESC-Volume", MESSAGES.VolumePropertyDescriptions());
    map.put("PROPDESC-WalkSteps", MESSAGES.WalkStepsPropertyDescriptions());
    map.put("PROPDESC-WebRTC", MESSAGES.WebRTCPropertyDescriptions());
    map.put("PROPDESC-WebViewString", MESSAGES.WebViewStringPropertyDescriptions());
    map.put("PROPDESC-WestLongitude", MESSAGES.WestLongitudePropertyDescriptions());
    map.put("PROPDESC-WheelDiameter", MESSAGES.WheelDiameterPropertyDescriptions());
    map.put("PROPDESC-Width", MESSAGES.WidthPropertyDescriptions());
    map.put("PROPDESC-WidthPercent", MESSAGES.WidthPercentPropertyDescriptions());
    map.put("PROPDESC-WithinRangeEventEnabled", MESSAGES.WithinRangeEventEnabledPropertyDescriptions());
    map.put("PROPDESC-WriteType", MESSAGES.WriteTypePropertyDescriptions());
    map.put("PROPDESC-X", MESSAGES.XPropertyDescriptions());
    map.put("PROPDESC-XAccel", MESSAGES.XAccelPropertyDescriptions());
    map.put("PROPDESC-XAngularVelocity", MESSAGES.XAngularVelocityPropertyDescriptions());
    map.put("PROPDESC-Y", MESSAGES.YPropertyDescriptions());
    map.put("PROPDESC-YAccel", MESSAGES.YAccelPropertyDescriptions());
    map.put("PROPDESC-YAngularVelocity", MESSAGES.YAngularVelocityPropertyDescriptions());
    map.put("PROPDESC-Year", MESSAGES.YearPropertyDescriptions());
    map.put("PROPDESC-Z", MESSAGES.ZPropertyDescriptions());
    map.put("PROPDESC-ZAccel", MESSAGES.ZAccelPropertyDescriptions());
    map.put("PROPDESC-ZAngularVelocity", MESSAGES.ZAngularVelocityPropertyDescriptions());
    map.put("PROPDESC-ZoomLevel", MESSAGES.ZoomLevelPropertyDescriptions());
    map.put("EVENTDESC-AboveRange", MESSAGES.AboveRangeEventDescriptions());
    map.put("EVENTDESC-AccelerationChanged", MESSAGES.AccelerationChangedEventDescriptions());
    map.put("EVENTDESC-ActivityCanceled", MESSAGES.ActivityCanceledEventDescriptions());
    map.put("EVENTDESC-AfterActivity", MESSAGES.AfterActivityEventDescriptions());
    map.put("EVENTDESC-AfterChoosing", MESSAGES.AfterChoosingEventDescriptions());
    map.put("EVENTDESC-AfterDateSet", MESSAGES.AfterDateSetEventDescriptions());
    map.put("EVENTDESC-AfterFileSaved", MESSAGES.AfterFileSavedEventDescriptions());
    map.put("EVENTDESC-AfterGettingText", MESSAGES.AfterGettingTextEventDescriptions());
    map.put("EVENTDESC-AfterPicking", MESSAGES.AfterPickingEventDescriptions());
    map.put("EVENTDESC-AfterPicture", MESSAGES.AfterPictureEventDescriptions());
    map.put("EVENTDESC-AfterRecording", MESSAGES.AfterRecordingEventDescriptions());
    map.put("EVENTDESC-AfterScan", MESSAGES.AfterScanEventDescriptions());
    map.put("EVENTDESC-AfterSelecting", MESSAGES.AfterSelectingEventDescriptions());
    map.put("EVENTDESC-AfterSoundRecorded", MESSAGES.AfterSoundRecordedEventDescriptions());
    map.put("EVENTDESC-AfterSpeaking", MESSAGES.AfterSpeakingEventDescriptions());
    map.put("EVENTDESC-AfterTextInput", MESSAGES.AfterTextInputEventDescriptions());
    map.put("EVENTDESC-AfterTimeSet", MESSAGES.AfterTimeSetEventDescriptions());
    map.put("EVENTDESC-BackPressed", MESSAGES.BackPressedEventDescriptions());
    map.put("EVENTDESC-BeforeGettingText", MESSAGES.BeforeGettingTextEventDescriptions());
    map.put("EVENTDESC-BeforePicking", MESSAGES.BeforePickingEventDescriptions());
    map.put("EVENTDESC-BeforeSpeaking", MESSAGES.BeforeSpeakingEventDescriptions());
    map.put("EVENTDESC-BelowRange", MESSAGES.BelowRangeEventDescriptions());
    map.put("EVENTDESC-BoundsChange", MESSAGES.BoundsChangeEventDescriptions());
    map.put("EVENTDESC-CalibrationFailed", MESSAGES.CalibrationFailedEventDescriptions());
    map.put("EVENTDESC-Changed", MESSAGES.ChangedEventDescriptions());
    map.put("EVENTDESC-ChoosingCanceled", MESSAGES.ChoosingCanceledEventDescriptions());
    map.put("EVENTDESC-Click", MESSAGES.ClickEventDescriptions());
    map.put("EVENTDESC-CloudDBError", MESSAGES.CloudDBErrorEventDescriptions());
    map.put("EVENTDESC-CollidedWith", MESSAGES.CollidedWithEventDescriptions());
    map.put("EVENTDESC-ColorChanged", MESSAGES.ColorChangedEventDescriptions());
    map.put("EVENTDESC-Completed", MESSAGES.CompletedEventDescriptions());
    map.put("EVENTDESC-ConnectionAccepted", MESSAGES.ConnectionAcceptedEventDescriptions());
    map.put("EVENTDESC-DataChanged", MESSAGES.DataChangedEventDescriptions());
    map.put("EVENTDESC-DirectMessagesReceived", MESSAGES.DirectMessagesReceivedEventDescriptions());
    map.put("EVENTDESC-DoubleTapAtPoint", MESSAGES.DoubleTapAtPointEventDescriptions());
    map.put("EVENTDESC-Drag", MESSAGES.DragEventDescriptions());
    map.put("EVENTDESC-Dragged", MESSAGES.DraggedEventDescriptions());
    map.put("EVENTDESC-EdgeReached", MESSAGES.EdgeReachedEventDescriptions());
    map.put("EVENTDESC-ErrorOccurred", MESSAGES.ErrorOccurredEventDescriptions());
    map.put("EVENTDESC-FeatureClick", MESSAGES.FeatureClickEventDescriptions());
    map.put("EVENTDESC-FeatureDrag", MESSAGES.FeatureDragEventDescriptions());
    map.put("EVENTDESC-FeatureLongClick", MESSAGES.FeatureLongClickEventDescriptions());
    map.put("EVENTDESC-FeatureStartDrag", MESSAGES.FeatureStartDragEventDescriptions());
    map.put("EVENTDESC-FeatureStopDrag", MESSAGES.FeatureStopDragEventDescriptions());
    map.put("EVENTDESC-FirebaseError", MESSAGES.FirebaseErrorEventDescriptions());
    map.put("EVENTDESC-FirstRemoved", MESSAGES.FirstRemovedEventDescriptions());
    map.put("EVENTDESC-Flung", MESSAGES.FlungEventDescriptions());
    map.put("EVENTDESC-FollowersReceived", MESSAGES.FollowersReceivedEventDescriptions());
    map.put("EVENTDESC-FriendTimelineReceived", MESSAGES.FriendTimelineReceivedEventDescriptions());
    map.put("EVENTDESC-FunctionCompleted", MESSAGES.FunctionCompletedEventDescriptions());
    map.put("EVENTDESC-GPSAvailable", MESSAGES.GPSAvailableEventDescriptions());
    map.put("EVENTDESC-GPSLost", MESSAGES.GPSLostEventDescriptions());
    map.put("EVENTDESC-GotBallot", MESSAGES.GotBallotEventDescriptions());
    map.put("EVENTDESC-GotBallotConfirmation", MESSAGES.GotBallotConfirmationEventDescriptions());
    map.put("EVENTDESC-GotFeatures", MESSAGES.GotFeaturesEventDescriptions());
    map.put("EVENTDESC-GotFile", MESSAGES.GotFileEventDescriptions());
    map.put("EVENTDESC-GotFocus", MESSAGES.GotFocusEventDescriptions());
    map.put("EVENTDESC-GotMessage", MESSAGES.GotMessageEventDescriptions());
    map.put("EVENTDESC-GotResult", MESSAGES.GotResultEventDescriptions());
    map.put("EVENTDESC-GotText", MESSAGES.GotTextEventDescriptions());
    map.put("EVENTDESC-GotTranslation", MESSAGES.GotTranslationEventDescriptions());
    map.put("EVENTDESC-GotValue", MESSAGES.GotValueEventDescriptions());
    map.put("EVENTDESC-GyroscopeChanged", MESSAGES.GyroscopeChangedEventDescriptions());
    map.put("EVENTDESC-IncomingCallAnswered", MESSAGES.IncomingCallAnsweredEventDescriptions());
    map.put("EVENTDESC-Info", MESSAGES.InfoEventDescriptions());
    map.put("EVENTDESC-Initialize", MESSAGES.InitializeEventDescriptions());
    map.put("EVENTDESC-InstanceIdChanged", MESSAGES.InstanceIdChangedEventDescriptions());
    map.put("EVENTDESC-InvalidPoint", MESSAGES.InvalidPointEventDescriptions());
    map.put("EVENTDESC-Invited", MESSAGES.InvitedEventDescriptions());
    map.put("EVENTDESC-IsAuthorized", MESSAGES.IsAuthorizedEventDescriptions());
    map.put("EVENTDESC-LoadError", MESSAGES.LoadErrorEventDescriptions());
    map.put("EVENTDESC-LocationChanged", MESSAGES.LocationChangedEventDescriptions());
    map.put("EVENTDESC-LongClick", MESSAGES.LongClickEventDescriptions());
    map.put("EVENTDESC-LongPressAtPoint", MESSAGES.LongPressAtPointEventDescriptions());
    map.put("EVENTDESC-LostFocus", MESSAGES.LostFocusEventDescriptions());
    map.put("EVENTDESC-MediaStored", MESSAGES.MediaStoredEventDescriptions());
    map.put("EVENTDESC-MentionsReceived", MESSAGES.MentionsReceivedEventDescriptions());
    map.put("EVENTDESC-MessageReceived", MESSAGES.MessageReceivedEventDescriptions());
    map.put("EVENTDESC-NewInstanceMade", MESSAGES.NewInstanceMadeEventDescriptions());
    map.put("EVENTDESC-NewLeader", MESSAGES.NewLeaderEventDescriptions());
    map.put("EVENTDESC-NoLongerCollidingWith", MESSAGES.NoLongerCollidingWithEventDescriptions());
    map.put("EVENTDESC-NoOpenPoll", MESSAGES.NoOpenPollEventDescriptions());
    map.put("EVENTDESC-OnSettings", MESSAGES.OnSettingsEventDescriptions());
    map.put("EVENTDESC-OrientationChanged", MESSAGES.OrientationChangedEventDescriptions());
    map.put("EVENTDESC-OtherPlayerStarted", MESSAGES.OtherPlayerStartedEventDescriptions());
    map.put("EVENTDESC-OtherScreenClosed", MESSAGES.OtherScreenClosedEventDescriptions());
    map.put("EVENTDESC-PermissionDenied", MESSAGES.PermissionDeniedEventDescriptions());
    map.put("EVENTDESC-PermissionGranted", MESSAGES.PermissionGrantedEventDescriptions());
    map.put("EVENTDESC-PhoneCallEnded", MESSAGES.PhoneCallEndedEventDescriptions());
    map.put("EVENTDESC-PhoneCallStarted", MESSAGES.PhoneCallStartedEventDescriptions());
    map.put("EVENTDESC-PlayerJoined", MESSAGES.PlayerJoinedEventDescriptions());
    map.put("EVENTDESC-PlayerLeft", MESSAGES.PlayerLeftEventDescriptions());
    map.put("EVENTDESC-PositionChanged", MESSAGES.PositionChangedEventDescriptions());
    map.put("EVENTDESC-Pressed", MESSAGES.PressedEventDescriptions());
    map.put("EVENTDESC-ProximityChanged", MESSAGES.ProximityChangedEventDescriptions());
    map.put("EVENTDESC-Ready", MESSAGES.ReadyEventDescriptions());
    map.put("EVENTDESC-Released", MESSAGES.ReleasedEventDescriptions());
    map.put("EVENTDESC-ScreenOrientationChanged", MESSAGES.ScreenOrientationChangedEventDescriptions());
    map.put("EVENTDESC-SearchSuccessful", MESSAGES.SearchSuccessfulEventDescriptions());
    map.put("EVENTDESC-SensorValueChanged", MESSAGES.SensorValueChangedEventDescriptions());
    map.put("EVENTDESC-ServerCommandFailure", MESSAGES.ServerCommandFailureEventDescriptions());
    map.put("EVENTDESC-ServerCommandSuccess", MESSAGES.ServerCommandSuccessEventDescriptions());
    map.put("EVENTDESC-Shaking", MESSAGES.ShakingEventDescriptions());
    map.put("EVENTDESC-SimpleStep", MESSAGES.SimpleStepEventDescriptions());
    map.put("EVENTDESC-StartDrag", MESSAGES.StartDragEventDescriptions());
    map.put("EVENTDESC-StartedMoving", MESSAGES.StartedMovingEventDescriptions());
    map.put("EVENTDESC-StartedRecording", MESSAGES.StartedRecordingEventDescriptions());
    map.put("EVENTDESC-StatusChanged", MESSAGES.StatusChangedEventDescriptions());
    map.put("EVENTDESC-StopDrag", MESSAGES.StopDragEventDescriptions());
    map.put("EVENTDESC-StoppedMoving", MESSAGES.StoppedMovingEventDescriptions());
    map.put("EVENTDESC-StoppedRecording", MESSAGES.StoppedRecordingEventDescriptions());
    map.put("EVENTDESC-TachoCountChanged", MESSAGES.TachoCountChangedEventDescriptions());
    map.put("EVENTDESC-TagList", MESSAGES.TagListEventDescriptions());
    map.put("EVENTDESC-TagRead", MESSAGES.TagReadEventDescriptions());
    map.put("EVENTDESC-TagWritten", MESSAGES.TagWrittenEventDescriptions());
    map.put("EVENTDESC-TapAtPoint", MESSAGES.TapAtPointEventDescriptions());
    map.put("EVENTDESC-TextInputCanceled", MESSAGES.TextInputCanceledEventDescriptions());
    map.put("EVENTDESC-Timer", MESSAGES.TimerEventDescriptions());
    map.put("EVENTDESC-TouchDown", MESSAGES.TouchDownEventDescriptions());
    map.put("EVENTDESC-TouchUp", MESSAGES.TouchUpEventDescriptions());
    map.put("EVENTDESC-Touched", MESSAGES.TouchedEventDescriptions());
    map.put("EVENTDESC-UserEmailAddressSet", MESSAGES.UserEmailAddressSetEventDescriptions());
    map.put("EVENTDESC-ValueStored", MESSAGES.ValueStoredEventDescriptions());
    map.put("EVENTDESC-WalkStep", MESSAGES.WalkStepEventDescriptions());
    map.put("EVENTDESC-WebServiceError", MESSAGES.WebServiceErrorEventDescriptions());
    map.put("EVENTDESC-WebViewStringChange", MESSAGES.WebViewStringChangeEventDescriptions());
    map.put("EVENTDESC-WithinRange", MESSAGES.WithinRangeEventDescriptions());
    map.put("EVENTDESC-ZoomChange", MESSAGES.ZoomChangeEventDescriptions());
    map.put("METHDESC-AcceptConnection", MESSAGES.AcceptConnectionMethodDescriptions());
    map.put("METHDESC-AcceptConnectionWithUUID", MESSAGES.AcceptConnectionWithUUIDMethodDescriptions());
    map.put("METHDESC-AddDays", MESSAGES.AddDaysMethodDescriptions());
    map.put("METHDESC-AddDuration", MESSAGES.AddDurationMethodDescriptions());
    map.put("METHDESC-AddHours", MESSAGES.AddHoursMethodDescriptions());
    map.put("METHDESC-AddMinutes", MESSAGES.AddMinutesMethodDescriptions());
    map.put("METHDESC-AddMonths", MESSAGES.AddMonthsMethodDescriptions());
    map.put("METHDESC-AddSeconds", MESSAGES.AddSecondsMethodDescriptions());
    map.put("METHDESC-AddWeeks", MESSAGES.AddWeeksMethodDescriptions());
    map.put("METHDESC-AddYears", MESSAGES.AddYearsMethodDescriptions());
    map.put("METHDESC-AppendToFile", MESSAGES.AppendToFileMethodDescriptions());
    map.put("METHDESC-AppendValue", MESSAGES.AppendValueMethodDescriptions());
    map.put("METHDESC-AppendValueToList", MESSAGES.AppendValueToListMethodDescriptions());
    map.put("METHDESC-AskForPermission", MESSAGES.AskForPermissionMethodDescriptions());
    map.put("METHDESC-Authorize", MESSAGES.AuthorizeMethodDescriptions());
    map.put("METHDESC-BearingToFeature", MESSAGES.BearingToFeatureMethodDescriptions());
    map.put("METHDESC-BearingToPoint", MESSAGES.BearingToPointMethodDescriptions());
    map.put("METHDESC-Bounce", MESSAGES.BounceMethodDescriptions());
    map.put("METHDESC-Bounds", MESSAGES.BoundsMethodDescriptions());
    map.put("METHDESC-BuildRequestData", MESSAGES.BuildRequestDataMethodDescriptions());
    map.put("METHDESC-BytesAvailableToReceive", MESSAGES.BytesAvailableToReceiveMethodDescriptions());
    map.put("METHDESC-CanGoBack", MESSAGES.CanGoBackMethodDescriptions());
    map.put("METHDESC-CanGoForward", MESSAGES.CanGoForwardMethodDescriptions());
    map.put("METHDESC-Center", MESSAGES.CenterMethodDescriptions());
    map.put("METHDESC-Centroid", MESSAGES.CentroidMethodDescriptions());
    map.put("METHDESC-CheckAuthorized", MESSAGES.CheckAuthorizedMethodDescriptions());
    map.put("METHDESC-Clear", MESSAGES.ClearMethodDescriptions());
    map.put("METHDESC-ClearAll", MESSAGES.ClearAllMethodDescriptions());
    map.put("METHDESC-ClearCaches", MESSAGES.ClearCachesMethodDescriptions());
    map.put("METHDESC-ClearCookies", MESSAGES.ClearCookiesMethodDescriptions());
    map.put("METHDESC-ClearLocations", MESSAGES.ClearLocationsMethodDescriptions());
    map.put("METHDESC-ClearTag", MESSAGES.ClearTagMethodDescriptions());
    map.put("METHDESC-CloudConnected", MESSAGES.CloudConnectedMethodDescriptions());
    map.put("METHDESC-CollidingWith", MESSAGES.CollidingWithMethodDescriptions());
    map.put("METHDESC-Connect", MESSAGES.ConnectMethodDescriptions());
    map.put("METHDESC-ConnectWithUUID", MESSAGES.ConnectWithUUIDMethodDescriptions());
    map.put("METHDESC-CreateMarker", MESSAGES.CreateMarkerMethodDescriptions());
    map.put("METHDESC-DayOfMonth", MESSAGES.DayOfMonthMethodDescriptions());
    map.put("METHDESC-DeAuthorize", MESSAGES.DeAuthorizeMethodDescriptions());
    map.put("METHDESC-Delete", MESSAGES.DeleteMethodDescriptions());
    map.put("METHDESC-DeleteFile", MESSAGES.DeleteFileMethodDescriptions());
    map.put("METHDESC-DirectMessage", MESSAGES.DirectMessageMethodDescriptions());
    map.put("METHDESC-Disconnect", MESSAGES.DisconnectMethodDescriptions());
    map.put("METHDESC-DismissProgressDialog", MESSAGES.DismissProgressDialogMethodDescriptions());
    map.put("METHDESC-DisplayDropdown", MESSAGES.DisplayDropdownMethodDescriptions());
    map.put("METHDESC-DistanceToFeature", MESSAGES.DistanceToFeatureMethodDescriptions());
    map.put("METHDESC-DistanceToPoint", MESSAGES.DistanceToPointMethodDescriptions());
    map.put("METHDESC-DoQuery", MESSAGES.DoQueryMethodDescriptions());
    map.put("METHDESC-DoScan", MESSAGES.DoScanMethodDescriptions());
    map.put("METHDESC-DownloadFile", MESSAGES.DownloadFileMethodDescriptions());
    map.put("METHDESC-DrawArc", MESSAGES.DrawArcMethodDescriptions());
    map.put("METHDESC-DrawCircle", MESSAGES.DrawCircleMethodDescriptions());
    map.put("METHDESC-DrawIcon", MESSAGES.DrawIconMethodDescriptions());
    map.put("METHDESC-DrawLine", MESSAGES.DrawLineMethodDescriptions());
    map.put("METHDESC-DrawPoint", MESSAGES.DrawPointMethodDescriptions());
    map.put("METHDESC-DrawRect", MESSAGES.DrawRectMethodDescriptions());
    map.put("METHDESC-DrawShape", MESSAGES.DrawShapeMethodDescriptions());
    map.put("METHDESC-DrawText", MESSAGES.DrawTextMethodDescriptions());
    map.put("METHDESC-DrawTextAtAngle", MESSAGES.DrawTextAtAngleMethodDescriptions());
    map.put("METHDESC-Duration", MESSAGES.DurationMethodDescriptions());
    map.put("METHDESC-DurationToDays", MESSAGES.DurationToDaysMethodDescriptions());
    map.put("METHDESC-DurationToHours", MESSAGES.DurationToHoursMethodDescriptions());
    map.put("METHDESC-DurationToMinutes", MESSAGES.DurationToMinutesMethodDescriptions());
    map.put("METHDESC-DurationToSeconds", MESSAGES.DurationToSecondsMethodDescriptions());
    map.put("METHDESC-DurationToWeeks", MESSAGES.DurationToWeeksMethodDescriptions());
    map.put("METHDESC-FeatureFromDescription", MESSAGES.FeatureFromDescriptionMethodDescriptions());
    map.put("METHDESC-FillScreen", MESSAGES.FillScreenMethodDescriptions());
    map.put("METHDESC-Follow", MESSAGES.FollowMethodDescriptions());
    map.put("METHDESC-ForgetLogin", MESSAGES.ForgetLoginMethodDescriptions());
    map.put("METHDESC-FormatDate", MESSAGES.FormatDateMethodDescriptions());
    map.put("METHDESC-FormatDateTime", MESSAGES.FormatDateTimeMethodDescriptions());
    map.put("METHDESC-FormatTime", MESSAGES.FormatTimeMethodDescriptions());
    map.put("METHDESC-Get", MESSAGES.GetMethodDescriptions());
    map.put("METHDESC-GetBackgroundPixelColor", MESSAGES.GetBackgroundPixelColorMethodDescriptions());
    map.put("METHDESC-GetBatteryCurrent", MESSAGES.GetBatteryCurrentMethodDescriptions());
    map.put("METHDESC-GetBatteryLevel", MESSAGES.GetBatteryLevelMethodDescriptions());
    map.put("METHDESC-GetBatteryVoltage", MESSAGES.GetBatteryVoltageMethodDescriptions());
    map.put("METHDESC-GetBrickName", MESSAGES.GetBrickNameMethodDescriptions());
    map.put("METHDESC-GetColor", MESSAGES.GetColorMethodDescriptions());
    map.put("METHDESC-GetColorCode", MESSAGES.GetColorCodeMethodDescriptions());
    map.put("METHDESC-GetColorName", MESSAGES.GetColorNameMethodDescriptions());
    map.put("METHDESC-GetCurrentProgramName", MESSAGES.GetCurrentProgramNameMethodDescriptions());
    map.put("METHDESC-GetDistance", MESSAGES.GetDistanceMethodDescriptions());
    map.put("METHDESC-GetDuration", MESSAGES.GetDurationMethodDescriptions());
    map.put("METHDESC-GetFirmwareBuild", MESSAGES.GetFirmwareBuildMethodDescriptions());
    map.put("METHDESC-GetFirmwareVersion", MESSAGES.GetFirmwareVersionMethodDescriptions());
    map.put("METHDESC-GetHardwareVersion", MESSAGES.GetHardwareVersionMethodDescriptions());
    map.put("METHDESC-GetInputValues", MESSAGES.GetInputValuesMethodDescriptions());
    map.put("METHDESC-GetInstaller", MESSAGES.GetInstallerMethodDescriptions());
    map.put("METHDESC-GetInstanceLists", MESSAGES.GetInstanceListsMethodDescriptions());
    map.put("METHDESC-GetLightLevel", MESSAGES.GetLightLevelMethodDescriptions());
    map.put("METHDESC-GetMessages", MESSAGES.GetMessagesMethodDescriptions());
    map.put("METHDESC-GetMillis", MESSAGES.GetMillisMethodDescriptions());
    map.put("METHDESC-GetOSBuild", MESSAGES.GetOSBuildMethodDescriptions());
    map.put("METHDESC-GetOSVersion", MESSAGES.GetOSVersionMethodDescriptions());
    map.put("METHDESC-GetOutputState", MESSAGES.GetOutputStateMethodDescriptions());
    map.put("METHDESC-GetPixelColor", MESSAGES.GetPixelColorMethodDescriptions());
    map.put("METHDESC-GetRows", MESSAGES.GetRowsMethodDescriptions());
    map.put("METHDESC-GetRowsWithConditions", MESSAGES.GetRowsWithConditionsMethodDescriptions());
    map.put("METHDESC-GetSensorValue", MESSAGES.GetSensorValueMethodDescriptions());
    map.put("METHDESC-GetSoundLevel", MESSAGES.GetSoundLevelMethodDescriptions());
    map.put("METHDESC-GetTachoCount", MESSAGES.GetTachoCountMethodDescriptions());
    map.put("METHDESC-GetTagList", MESSAGES.GetTagListMethodDescriptions());
    map.put("METHDESC-GetTags", MESSAGES.GetTagsMethodDescriptions());
    map.put("METHDESC-GetText", MESSAGES.GetTextMethodDescriptions());
    map.put("METHDESC-GetValue", MESSAGES.GetValueMethodDescriptions());
    map.put("METHDESC-GetVersionName", MESSAGES.GetVersionNameMethodDescriptions());
    map.put("METHDESC-GetWifiIpAddress", MESSAGES.GetWifiIpAddressMethodDescriptions());
    map.put("METHDESC-GoBack", MESSAGES.GoBackMethodDescriptions());
    map.put("METHDESC-GoForward", MESSAGES.GoForwardMethodDescriptions());
    map.put("METHDESC-GoHome", MESSAGES.GoHomeMethodDescriptions());
    map.put("METHDESC-GoToUrl", MESSAGES.GoToUrlMethodDescriptions());
    map.put("METHDESC-HideInfobox", MESSAGES.HideInfoboxMethodDescriptions());
    map.put("METHDESC-HideKeyboard", MESSAGES.HideKeyboardMethodDescriptions());
    map.put("METHDESC-Hour", MESSAGES.HourMethodDescriptions());
    map.put("METHDESC-HtmlTextDecode", MESSAGES.HtmlTextDecodeMethodDescriptions());
    map.put("METHDESC-InsertRow", MESSAGES.InsertRowMethodDescriptions());
    map.put("METHDESC-InstallationId", MESSAGES.InstallationIdMethodDescriptions());
    map.put("METHDESC-Invite", MESSAGES.InviteMethodDescriptions());
    map.put("METHDESC-IsDevicePaired", MESSAGES.IsDevicePairedMethodDescriptions());
    map.put("METHDESC-IsPressed", MESSAGES.IsPressedMethodDescriptions());
    map.put("METHDESC-JsonTextDecode", MESSAGES.JsonTextDecodeMethodDescriptions());
    map.put("METHDESC-KeepAlive", MESSAGES.KeepAliveMethodDescriptions());
    map.put("METHDESC-LatitudeFromAddress", MESSAGES.LatitudeFromAddressMethodDescriptions());
    map.put("METHDESC-LaunchPicker", MESSAGES.LaunchPickerMethodDescriptions());
    map.put("METHDESC-LeaveInstance", MESSAGES.LeaveInstanceMethodDescriptions());
    map.put("METHDESC-ListFiles", MESSAGES.ListFilesMethodDescriptions());
    map.put("METHDESC-LoadFromURL", MESSAGES.LoadFromURLMethodDescriptions());
    map.put("METHDESC-LogError", MESSAGES.LogErrorMethodDescriptions());
    map.put("METHDESC-LogInfo", MESSAGES.LogInfoMethodDescriptions());
    map.put("METHDESC-LogWarning", MESSAGES.LogWarningMethodDescriptions());
    map.put("METHDESC-LongitudeFromAddress", MESSAGES.LongitudeFromAddressMethodDescriptions());
    map.put("METHDESC-LsGetStatus", MESSAGES.LsGetStatusMethodDescriptions());
    map.put("METHDESC-LsRead", MESSAGES.LsReadMethodDescriptions());
    map.put("METHDESC-LsWrite", MESSAGES.LsWriteMethodDescriptions());
    map.put("METHDESC-MakeDate", MESSAGES.MakeDateMethodDescriptions());
    map.put("METHDESC-MakeInstant", MESSAGES.MakeInstantMethodDescriptions());
    map.put("METHDESC-MakeInstantFromMillis", MESSAGES.MakeInstantFromMillisMethodDescriptions());
    map.put("METHDESC-MakeInstantFromParts", MESSAGES.MakeInstantFromPartsMethodDescriptions());
    map.put("METHDESC-MakeNewInstance", MESSAGES.MakeNewInstanceMethodDescriptions());
    map.put("METHDESC-MakePhoneCall", MESSAGES.MakePhoneCallMethodDescriptions());
    map.put("METHDESC-MakePhoneCallDirect", MESSAGES.MakePhoneCallDirectMethodDescriptions());
    map.put("METHDESC-MakeTime", MESSAGES.MakeTimeMethodDescriptions());
    map.put("METHDESC-MessageRead", MESSAGES.MessageReadMethodDescriptions());
    map.put("METHDESC-MessageWrite", MESSAGES.MessageWriteMethodDescriptions());
    map.put("METHDESC-Minute", MESSAGES.MinuteMethodDescriptions());
    map.put("METHDESC-Month", MESSAGES.MonthMethodDescriptions());
    map.put("METHDESC-MonthName", MESSAGES.MonthNameMethodDescriptions());
    map.put("METHDESC-MoveBackward", MESSAGES.MoveBackwardMethodDescriptions());
    map.put("METHDESC-MoveBackwardIndefinitely", MESSAGES.MoveBackwardIndefinitelyMethodDescriptions());
    map.put("METHDESC-MoveForward", MESSAGES.MoveForwardMethodDescriptions());
    map.put("METHDESC-MoveForwardIndefinitely", MESSAGES.MoveForwardIndefinitelyMethodDescriptions());
    map.put("METHDESC-MoveIntoBounds", MESSAGES.MoveIntoBoundsMethodDescriptions());
    map.put("METHDESC-MoveTo", MESSAGES.MoveToMethodDescriptions());
    map.put("METHDESC-Now", MESSAGES.NowMethodDescriptions());
    map.put("METHDESC-Open", MESSAGES.OpenMethodDescriptions());
    map.put("METHDESC-PanTo", MESSAGES.PanToMethodDescriptions());
    map.put("METHDESC-Pause", MESSAGES.PauseMethodDescriptions());
    map.put("METHDESC-Play", MESSAGES.PlayMethodDescriptions());
    map.put("METHDESC-PlaySoundFile", MESSAGES.PlaySoundFileMethodDescriptions());
    map.put("METHDESC-PlayTone", MESSAGES.PlayToneMethodDescriptions());
    map.put("METHDESC-PointInDirection", MESSAGES.PointInDirectionMethodDescriptions());
    map.put("METHDESC-PointTowards", MESSAGES.PointTowardsMethodDescriptions());
    map.put("METHDESC-PostFile", MESSAGES.PostFileMethodDescriptions());
    map.put("METHDESC-PostMedia", MESSAGES.PostMediaMethodDescriptions());
    map.put("METHDESC-PostText", MESSAGES.PostTextMethodDescriptions());
    map.put("METHDESC-PostTextWithEncoding", MESSAGES.PostTextWithEncodingMethodDescriptions());
    map.put("METHDESC-PutFile", MESSAGES.PutFileMethodDescriptions());
    map.put("METHDESC-PutText", MESSAGES.PutTextMethodDescriptions());
    map.put("METHDESC-PutTextWithEncoding", MESSAGES.PutTextWithEncodingMethodDescriptions());
    map.put("METHDESC-ReadFrom", MESSAGES.ReadFromMethodDescriptions());
    map.put("METHDESC-ReceiveSigned1ByteNumber", MESSAGES.ReceiveSigned1ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveSigned2ByteNumber", MESSAGES.ReceiveSigned2ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveSigned4ByteNumber", MESSAGES.ReceiveSigned4ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveSignedBytes", MESSAGES.ReceiveSignedBytesMethodDescriptions());
    map.put("METHDESC-ReceiveText", MESSAGES.ReceiveTextMethodDescriptions());
    map.put("METHDESC-ReceiveUnsigned1ByteNumber", MESSAGES.ReceiveUnsigned1ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveUnsigned2ByteNumber", MESSAGES.ReceiveUnsigned2ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveUnsigned4ByteNumber", MESSAGES.ReceiveUnsigned4ByteNumberMethodDescriptions());
    map.put("METHDESC-ReceiveUnsignedBytes", MESSAGES.ReceiveUnsignedBytesMethodDescriptions());
    map.put("METHDESC-RecordVideo", MESSAGES.RecordVideoMethodDescriptions());
    map.put("METHDESC-RemoveFirst", MESSAGES.RemoveFirstMethodDescriptions());
    map.put("METHDESC-RemoveFirstFromList", MESSAGES.RemoveFirstFromListMethodDescriptions());
    map.put("METHDESC-RequestBallot", MESSAGES.RequestBallotMethodDescriptions());
    map.put("METHDESC-RequestDirectMessages", MESSAGES.RequestDirectMessagesMethodDescriptions());
    map.put("METHDESC-RequestFocus", MESSAGES.RequestFocusMethodDescriptions());
    map.put("METHDESC-RequestFollowers", MESSAGES.RequestFollowersMethodDescriptions());
    map.put("METHDESC-RequestFriendTimeline", MESSAGES.RequestFriendTimelineMethodDescriptions());
    map.put("METHDESC-RequestMentions", MESSAGES.RequestMentionsMethodDescriptions());
    map.put("METHDESC-RequestTranslation", MESSAGES.RequestTranslationMethodDescriptions());
    map.put("METHDESC-Reset", MESSAGES.ResetMethodDescriptions());
    map.put("METHDESC-ResetInputScaledValue", MESSAGES.ResetInputScaledValueMethodDescriptions());
    map.put("METHDESC-ResetMotorPosition", MESSAGES.ResetMotorPositionMethodDescriptions());
    map.put("METHDESC-ResetTachoCount", MESSAGES.ResetTachoCountMethodDescriptions());
    map.put("METHDESC-ResolveActivity", MESSAGES.ResolveActivityMethodDescriptions());
    map.put("METHDESC-Resume", MESSAGES.ResumeMethodDescriptions());
    map.put("METHDESC-RotateInDistance", MESSAGES.RotateInDistanceMethodDescriptions());
    map.put("METHDESC-RotateInDuration", MESSAGES.RotateInDurationMethodDescriptions());
    map.put("METHDESC-RotateInTachoCounts", MESSAGES.RotateInTachoCountsMethodDescriptions());
    map.put("METHDESC-RotateIndefinitely", MESSAGES.RotateIndefinitelyMethodDescriptions());
    map.put("METHDESC-RotateSyncInDistance", MESSAGES.RotateSyncInDistanceMethodDescriptions());
    map.put("METHDESC-RotateSyncInDuration", MESSAGES.RotateSyncInDurationMethodDescriptions());
    map.put("METHDESC-RotateSyncInTachoCounts", MESSAGES.RotateSyncInTachoCountsMethodDescriptions());
    map.put("METHDESC-RotateSyncIndefinitely", MESSAGES.RotateSyncIndefinitelyMethodDescriptions());
    map.put("METHDESC-Save", MESSAGES.SaveMethodDescriptions());
    map.put("METHDESC-SaveAs", MESSAGES.SaveAsMethodDescriptions());
    map.put("METHDESC-SaveFile", MESSAGES.SaveFileMethodDescriptions());
    map.put("METHDESC-SdkLevel", MESSAGES.SdkLevelMethodDescriptions());
    map.put("METHDESC-SearchTwitter", MESSAGES.SearchTwitterMethodDescriptions());
    map.put("METHDESC-Second", MESSAGES.SecondMethodDescriptions());
    map.put("METHDESC-SeekTo", MESSAGES.SeekToMethodDescriptions());
    map.put("METHDESC-Send1ByteNumber", MESSAGES.Send1ByteNumberMethodDescriptions());
    map.put("METHDESC-Send2ByteNumber", MESSAGES.Send2ByteNumberMethodDescriptions());
    map.put("METHDESC-Send4ByteNumber", MESSAGES.Send4ByteNumberMethodDescriptions());
    map.put("METHDESC-SendBallot", MESSAGES.SendBallotMethodDescriptions());
    map.put("METHDESC-SendBytes", MESSAGES.SendBytesMethodDescriptions());
    map.put("METHDESC-SendMessage", MESSAGES.SendMessageMethodDescriptions());
    map.put("METHDESC-SendMessageDirect", MESSAGES.SendMessageDirectMethodDescriptions());
    map.put("METHDESC-SendQuery", MESSAGES.SendQueryMethodDescriptions());
    map.put("METHDESC-SendText", MESSAGES.SendTextMethodDescriptions());
    map.put("METHDESC-ServerCommand", MESSAGES.ServerCommandMethodDescriptions());
    map.put("METHDESC-SetAmbientMode", MESSAGES.SetAmbientModeMethodDescriptions());
    map.put("METHDESC-SetAngleMode", MESSAGES.SetAngleModeMethodDescriptions());
    map.put("METHDESC-SetBackgroundPixelColor", MESSAGES.SetBackgroundPixelColorMethodDescriptions());
    map.put("METHDESC-SetBrickName", MESSAGES.SetBrickNameMethodDescriptions());
    map.put("METHDESC-SetCenter", MESSAGES.SetCenterMethodDescriptions());
    map.put("METHDESC-SetCmUnit", MESSAGES.SetCmUnitMethodDescriptions());
    map.put("METHDESC-SetColorMode", MESSAGES.SetColorModeMethodDescriptions());
    map.put("METHDESC-SetDateToDisplay", MESSAGES.SetDateToDisplayMethodDescriptions());
    map.put("METHDESC-SetDateToDisplayFromInstant", MESSAGES.SetDateToDisplayFromInstantMethodDescriptions());
    map.put("METHDESC-SetInchUnit", MESSAGES.SetInchUnitMethodDescriptions());
    map.put("METHDESC-SetInputMode", MESSAGES.SetInputModeMethodDescriptions());
    map.put("METHDESC-SetInstance", MESSAGES.SetInstanceMethodDescriptions());
    map.put("METHDESC-SetLeader", MESSAGES.SetLeaderMethodDescriptions());
    map.put("METHDESC-SetLocation", MESSAGES.SetLocationMethodDescriptions());
    map.put("METHDESC-SetOutputState", MESSAGES.SetOutputStateMethodDescriptions());
    map.put("METHDESC-SetRateMode", MESSAGES.SetRateModeMethodDescriptions());
    map.put("METHDESC-SetReflectedMode", MESSAGES.SetReflectedModeMethodDescriptions());
    map.put("METHDESC-SetTimeToDisplay", MESSAGES.SetTimeToDisplayMethodDescriptions());
    map.put("METHDESC-SetTimeToDisplayFromInstant", MESSAGES.SetTimeToDisplayFromInstantMethodDescriptions());
    map.put("METHDESC-ShareFile", MESSAGES.ShareFileMethodDescriptions());
    map.put("METHDESC-ShareFileWithMessage", MESSAGES.ShareFileWithMessageMethodDescriptions());
    map.put("METHDESC-ShareMessage", MESSAGES.ShareMessageMethodDescriptions());
    map.put("METHDESC-ShowAlert", MESSAGES.ShowAlertMethodDescriptions());
    map.put("METHDESC-ShowChooseDialog", MESSAGES.ShowChooseDialogMethodDescriptions());
    map.put("METHDESC-ShowInfobox", MESSAGES.ShowInfoboxMethodDescriptions());
    map.put("METHDESC-ShowMessageDialog", MESSAGES.ShowMessageDialogMethodDescriptions());
    map.put("METHDESC-ShowPasswordDialog", MESSAGES.ShowPasswordDialogMethodDescriptions());
    map.put("METHDESC-ShowProgressDialog", MESSAGES.ShowProgressDialogMethodDescriptions());
    map.put("METHDESC-ShowTextDialog", MESSAGES.ShowTextDialogMethodDescriptions());
    map.put("METHDESC-Speak", MESSAGES.SpeakMethodDescriptions());
    map.put("METHDESC-Start", MESSAGES.StartMethodDescriptions());
    map.put("METHDESC-StartActivity", MESSAGES.StartActivityMethodDescriptions());
    map.put("METHDESC-StartProgram", MESSAGES.StartProgramMethodDescriptions());
    map.put("METHDESC-Stop", MESSAGES.StopMethodDescriptions());
    map.put("METHDESC-StopAccepting", MESSAGES.StopAcceptingMethodDescriptions());
    map.put("METHDESC-StopFollowing", MESSAGES.StopFollowingMethodDescriptions());
    map.put("METHDESC-StopProgram", MESSAGES.StopProgramMethodDescriptions());
    map.put("METHDESC-StopSound", MESSAGES.StopSoundMethodDescriptions());
    map.put("METHDESC-StopSoundPlayback", MESSAGES.StopSoundPlaybackMethodDescriptions());
    map.put("METHDESC-StoreValue", MESSAGES.StoreValueMethodDescriptions());
    map.put("METHDESC-SystemTime", MESSAGES.SystemTimeMethodDescriptions());
    map.put("METHDESC-TakePicture", MESSAGES.TakePictureMethodDescriptions());
    map.put("METHDESC-ToggleDirection", MESSAGES.ToggleDirectionMethodDescriptions());
    map.put("METHDESC-TurnClockwiseIndefinitely", MESSAGES.TurnClockwiseIndefinitelyMethodDescriptions());
    map.put("METHDESC-TurnCounterClockwiseIndefinitely", MESSAGES.TurnCounterClockwiseIndefinitelyMethodDescriptions());
    map.put("METHDESC-Tweet", MESSAGES.TweetMethodDescriptions());
    map.put("METHDESC-TweetWithImage", MESSAGES.TweetWithImageMethodDescriptions());
    map.put("METHDESC-Unauthenticate", MESSAGES.UnauthenticateMethodDescriptions());
    map.put("METHDESC-UriDecode", MESSAGES.UriDecodeMethodDescriptions());
    map.put("METHDESC-UriEncode", MESSAGES.UriEncodeMethodDescriptions());
    map.put("METHDESC-Vibrate", MESSAGES.VibrateMethodDescriptions());
    map.put("METHDESC-ViewContact", MESSAGES.ViewContactMethodDescriptions());
    map.put("METHDESC-Weekday", MESSAGES.WeekdayMethodDescriptions());
    map.put("METHDESC-WeekdayName", MESSAGES.WeekdayNameMethodDescriptions());
    map.put("METHDESC-XMLTextDecode", MESSAGES.XMLTextDecodeMethodDescriptions());
    map.put("METHDESC-Year", MESSAGES.YearMethodDescriptions());
    map.put("METHDESC-doFault", MESSAGES.doFaultMethodDescriptions());
    map.put("METHDESC-installURL", MESSAGES.installURLMethodDescriptions());
    map.put("METHDESC-isConnected", MESSAGES.isConnectedMethodDescriptions());
    map.put("METHDESC-isDirect", MESSAGES.isDirectMethodDescriptions());
    map.put("METHDESC-setAssetsLoaded", MESSAGES.setAssetsLoadedMethodDescriptions());
    map.put("METHDESC-setHmacSeedReturnCode", MESSAGES.setHmacSeedReturnCodeMethodDescriptions());
    map.put("METHDESC-shutdown", MESSAGES.shutdownMethodDescriptions());
    map.put("METHDESC-startHTTPD", MESSAGES.startHTTPDMethodDescriptions());
    map.put("METHDESC-startWebRTC", MESSAGES.startWebRTCMethodDescriptions());


    /* Categories */

    map.put("CATEGORY-Connectivity", MESSAGES.connectivityComponentPallette());
    map.put("CATEGORY-Drawing and Animation", MESSAGES.drawingAndAnimationComponentPallette());
    map.put("CATEGORY-Experimental", MESSAGES.experimentalComponentPallette());
    map.put("CATEGORY-For internal use only", MESSAGES.forInternalUseOnlyComponentPallette());
    map.put("CATEGORY-LEGO® MINDSTORMS®", MESSAGES.legoMindstormsComponentPallette());
    map.put("CATEGORY-Layout", MESSAGES.layoutComponentPallette());
    map.put("CATEGORY-Maps", MESSAGES.mapsComponentPallette());
    map.put("CATEGORY-Media", MESSAGES.mediaComponentPallette());
    map.put("CATEGORY-Sensors", MESSAGES.sensorsComponentPallette());
    map.put("CATEGORY-Social", MESSAGES.socialComponentPallette());
    map.put("CATEGORY-Storage", MESSAGES.storageComponentPallette());
    map.put("CATEGORY-User Interface", MESSAGES.userInterfaceComponentPallette());
  return map;
  }
}
