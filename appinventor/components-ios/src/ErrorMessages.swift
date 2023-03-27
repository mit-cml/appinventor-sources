// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/* Creating New Errors:
 * `case ERROR_NAME = code`
 * (code is now the error's rawValue)
 */

@objc public enum ErrorMessage: Int, Error {
  // LocationSensor Errors
  case ERROR_LOCATION_SENSOR_LATITUDE_NOT_FOUND = 101
  case ERROR_LOCATION_SENSOR_LONGITUDE_NOT_FOUND = 102
  case ERROR_LOCATION_SENSOR_PERMISSION_DENIED = 103
  case ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR = 104

  // Camera Errors
  case ERROR_CAMERA_NO_IMAGE_RETURNED = 201
  case ERROR_CAMERA_CANNOT_SAVE_FILE = 202

  // ActivityStarter Errors
  case ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY = 601
  case ERROR_ACTIVITY_STARTER_NO_ACTION_INFO = 602

  // Media Errors
  case ERROR_UNABLE_TO_LOAD_MEDIA = 701
  case ERROR_UNABLE_TO_PREPARE_MEDIA = 702
  case ERROR_MEDIA_IMAGE_FILE_FORMAT = 706
  case ERROR_MEDIA_FILE_ERROR = 708

  // SoundRecorder Errors
  case ERROR_SOUND_RECORDER = 801
  case ERROR_SOUND_RECORDER_CANNOT_CREATE = 802
  case ERROR_SOUND_RECORDER_PERMISSION_DENIED = 806

  // Form errors
  case ERROR_PERMISSION_DENIED = 908

  // ContactPicker and PhoneNumberPicker Errors
  // "Error message for when contact data cannot be used on the device."
  case ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER = 1107

  // Canvas Errors
  case ERROR_CANVAS_DRAW_SHAPE_BAD_ARGUMENT = 1004

  // Web Errors
  case ERROR_WEB_UNSUPPORTED_ENCODING = 1102
  case ERROR_WEB_JSON_TEXT_DECODE_FAILED = 1105
  case ERROR_WEB_HTML_TEXT_DECODE_FAILED = 1106
  case ERROR_WEB_MALFORMED_URL = 1109
  case ERROR_WEB_REQUEST_HEADER_NOT_LIST = 1110
  case ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS = 1111
  case ERROR_WEB_BUILD_REQUEST_DATA_NOT_LIST = 1112
  case ERROR_WEB_BUILD_REQUEST_DATA_NOT_TWO_ELEMENTS = 1113
  case ERROR_WEB_UNABLE_TO_DELETE = 1114
  case ERROR_WEB_XML_TEXT_DECODE_FAILED = 1115
  case ERROR_WEB_REQUEST_TIMED_OUT = 1117
  case ERROR_WEB_JSON_TEXT_ENCODE_FAILED = 1118

  // Camcorder Errors
  case ERROR_CAMCORDER_NO_CLIP_RETURNED = 1201
  case ERROR_CAMCORDER_CANNOT_SAVE_FILE = 1202

  // ImagePicker Errors
  case ERROR_CANNOT_COPY_MEDIA = 1602

  // WebViewer Errors
  case ERROR_WEB_VIEWER_UNKNOWN_ERROR = 2502
  case ERROR_WEB_VIEWER_UNSUPPORTED_METHOD = 101000
  case ERROR_WEB_VIEWER_MISSING_FILE = 101001

  // Repl Communication Errors
  case ERROR_REPL_SECURITY_ERROR = 1801

  // AccelerometerSensor Errors
  case ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY = 1901

  // File Errors
  case ERROR_CANNOT_FIND_FILE = 2101
  case ERROR_CANNOT_READ_FILE = 2102
  case ERROR_CANNOT_CREATE_FILE = 2103
  case ERROR_CANNOT_WRITE_TO_FILE = 2104
  case ERROR_CANNOT_DELETE_ASSET = 2105
  case ERROR_CANNOT_WRITE_ASSET = 2106

  // Yandex.Translate errors
  case ERROR_TRANSLATE_NO_KEY_FOUND = 2201
  case ERROR_TRANSLATE_SERVICE_NOT_AVAILABLE = 2202
  case ERROR_TRANSLATE_JSON_RESPONSE = 2203

  // TimePicker Errors
  case ERROR_ILLEGAL_HOUR = 2301
  case ERROR_ILLEGAL_MINUTE = 2302

  // DatePicker Errors
  case ERROR_ILLEGAL_DATE = 2401

  // FusionTables Errors
  case FUSION_TABLES_QUERY_ERROR = 2601
  case FUSION_TABLES_AUTH_ERROR = 2602

  // Form errors that are signalled in runtime.scm
  case ERROR_DIVISION_BY_ZERO = 3200

  // Map Errors
  case ERROR_LINESTRING_TOO_FEW_POINTS = 3401
  case ERROR_LINESTRING_PARSE_ERROR = 3402
  case ERROR_POLYGON_PARSE_ERROR = 3404
  case ERROR_EXCEPTION_DURING_MAP_SAVE = 3412
  case ERROR_INVALID_LATITUDE = 3413
  case ERROR_INVALID_LONGITUDE = 3414
  case ERROR_INVALID_ANCHOR_VERTICAL = 3416
  case ERROR_INVALID_ANCHOR_HORIZONTAL = 3417
  case ERROR_INVALID_MAP_TYPE = 3421

  // Navigation Errors
  case ERROR_INVALID_API_KEY = 4001
  case ERROR_UNABLE_TO_REQUEST_DIRECTIONS = 4002
  case ERROR_ROUTING_SERVICE_ERROR = 4003
  case ERROR_NO_ROUTE_FOUND = 4004

  // CloudDB Errors
  case ERROR_EMPTY_CLOUDDB_PROPERTY = 104001
  case ERROR_CLOUDDB_JSON_MALFORMED = 104002

  // iOS Specific Errors
  case ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED = 100001
  case ERROR_CANNOT_ENCODE_TEXT_AS_UTF8 = 100002

  // iOS Specific SpeechRecognizer Errors
  case ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED = 100100
  case ERROR_IOS_SPEECH_RECOGNITION_UNAVAILABLE = 100101
  case ERROR_IOS_SPEECH_RECOGNITION_PROCESSING_ERROR = 100102

  // iOS Specific GyroscopeSensor Errors
  case ERROR_IOS_GYROSCOPE_SENSOR_DATA_ERROR = 100200

  // iOS Specific OrientationSensor Errors
  case ERROR_IOS_ORIENTATION_SENSOR_DATA_ERROR = 100300
  
  var code: Int32 {
    return Int32(self.rawValue)
  }

  public var _code: Int {
    return self.rawValue
  }

  var localizedDescription: String {
    return self.message
  }

  public var message: String {
    switch self {
    // LocationSensor Errors
    case .ERROR_LOCATION_SENSOR_LATITUDE_NOT_FOUND:
      return "Unable to find latitude from %s."
    case .ERROR_LOCATION_SENSOR_LONGITUDE_NOT_FOUND:
      return "Unable to find longitude from %s."
    case .ERROR_LOCATION_SENSOR_PERMISSION_DENIED:
      return "User denied location services permission."
    case .ERROR_LOCATION_SENSOR_UNEXPECTED_ERROR:
      return "An unexpected error occurred when using location services: %s."

    // Camera errors
    case .ERROR_CAMERA_NO_IMAGE_RETURNED:
      return "The camera did not return an image."
    case .ERROR_CAMERA_CANNOT_SAVE_FILE:
      return "Camera could not save media image"

    // ActivityStarter Errors
    case .ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY:
      return "No corresponding activity was found."
    case .ERROR_ACTIVITY_STARTER_NO_ACTION_INFO:
      return "No Action information in ActivityStarter was found."

    // Media Errors
    case .ERROR_UNABLE_TO_LOAD_MEDIA:
      return "Unable to load media."
    case .ERROR_UNABLE_TO_PREPARE_MEDIA:
      return "Unable to prepare %s."
    case .ERROR_MEDIA_IMAGE_FILE_FORMAT:
      return "Image file name must end in \".jpg\", \".jpeg\", or \".png\"."
    case .ERROR_MEDIA_FILE_ERROR:
      return  "Got file error: %s."

    // SoundRecorder Errors
    case .ERROR_SOUND_RECORDER:
      return "An unexpected error occurred while recording sound."
    case .ERROR_SOUND_RECORDER_CANNOT_CREATE:
      return "Cannot start recording: %s"
    case .ERROR_SOUND_RECORDER_PERMISSION_DENIED:
      return "Permission to record audio was denied."

    // Form Errors
    case .ERROR_PERMISSION_DENIED:
      return "The permission %s has been denied. Please enable it in the Settings app."

    // ContactPicker and PhoneNumberPicker Errors
    case .ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER:
      return "The software used in this app cannot extract contacts from this type of phone."

    // Canvas Errors
    case .ERROR_CANVAS_DRAW_SHAPE_BAD_ARGUMENT:
      return "Canvas cannot draw the shape with a bad point list"

    // Web Errors
    case .ERROR_WEB_UNSUPPORTED_ENCODING:
      return "The encoding %s is not supported."
    case .ERROR_WEB_JSON_TEXT_DECODE_FAILED:
      return "Unable to decode the JSON text: %s"
    case .ERROR_WEB_HTML_TEXT_DECODE_FAILED:
      return "Unable to decode the HTML text: %s"
    case .ERROR_WEB_MALFORMED_URL:
      return "The given URL was not valid."
    case .ERROR_WEB_REQUEST_HEADER_NOT_LIST:
      return "The specified request headers are not valid: element %s is not a list"
    case .ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS:
      return "The specified request headers are not valid: element %s does not contain two elements"
    case .ERROR_WEB_BUILD_REQUEST_DATA_NOT_LIST:
      return "Unable to build request data: element %s is not a list"
    case .ERROR_WEB_BUILD_REQUEST_DATA_NOT_TWO_ELEMENTS:
      return "Unable to build request data: element %s does not contain two elements"
    case .ERROR_WEB_UNABLE_TO_DELETE:
      return "Unable to delete a resource with the specified URL: %@"
    case .ERROR_WEB_XML_TEXT_DECODE_FAILED:
      return "Unable to decode the XML text: %@"
    case .ERROR_WEB_REQUEST_TIMED_OUT:
      return "Took longer than timeout period to receive data from the URL: %s"
    case .ERROR_WEB_JSON_TEXT_ENCODE_FAILED:
      return "Unable to encode as JSON the object %@"

    // Camcorder Errors
    case .ERROR_CAMCORDER_NO_CLIP_RETURNED:
      return "Unable to find clip from camcorder"
    case .ERROR_CAMCORDER_CANNOT_SAVE_FILE:
      return "Cannot save media file"

    // WebViewer Errors
    case .ERROR_WEB_VIEWER_UNKNOWN_ERROR:
      return ""
    case .ERROR_WEB_VIEWER_UNSUPPORTED_METHOD:
      return "The specified method is not supported on iOS"
    case .ERROR_WEB_VIEWER_MISSING_FILE:
      return "Could not locate target file"

    // Repl Communication Errors
    case .ERROR_REPL_SECURITY_ERROR:
      return "Security Error Receiving Blocks from Browser: %@."

    // ImagePicker Errors
    case .ERROR_CANNOT_COPY_MEDIA:
      return "Unable to copy selected media: %s"

    // AccelerometerSensor Errors
    case .ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY:
      return "The value -- %s -- provided for AccelerometerSensor's sensitivity was bad. The only legal values are 1, 2, or 3."

    // TimePicker Errors
    case .ERROR_ILLEGAL_HOUR:
      return "The hour must be set to a value between 0 and 23."
    case .ERROR_ILLEGAL_MINUTE:
      return "The minute must be set to a value between 0 and 59."

    // DatePicker Errors
    case .ERROR_ILLEGAL_DATE:
      return "The date you entered is invalid."

    // Map Errors
    case .ERROR_LINESTRING_TOO_FEW_POINTS:
      return "Need at least 2 points for a LineString. Got only %d."
    case .ERROR_LINESTRING_PARSE_ERROR:
      return "Parse error attempting to create LineString: %s."
    case .ERROR_POLYGON_PARSE_ERROR:
      return "Parse error attempting to create Polygon: %s."
    case .ERROR_EXCEPTION_DURING_MAP_SAVE:
      return "Unable to save Map due to an internal exception: %s."
    case .ERROR_INVALID_LATITUDE:
      return "Latitude %f was not in the expected range [-90, 90]."
    case .ERROR_INVALID_LONGITUDE:
      return "Longitude %f was not in the expected range [-180, 180]."
    case .ERROR_INVALID_ANCHOR_VERTICAL:
      return "Invalid value %d given for AnchorVertical. Valid settings are 1, 2, or 3."
    case .ERROR_INVALID_ANCHOR_HORIZONTAL:
      return "Invalid value %d given for AnchorHorizontal. Valid settings are 1, 2, or 3."
    case .ERROR_INVALID_MAP_TYPE:
      return "The MapType must be 1, 2, or 3"

    // File Errors
    case .ERROR_CANNOT_FIND_FILE:
      return "The file %s could not be found"
    case .ERROR_CANNOT_READ_FILE:
      return "The file %s could not be opened"
    case .ERROR_CANNOT_CREATE_FILE:
      return "The file %s could not be created"
    case .ERROR_CANNOT_WRITE_TO_FILE:
      return "Cannot write to file %s"
    case .ERROR_CANNOT_DELETE_ASSET:
      return "Cannot delete asset file at %s"
    case .ERROR_CANNOT_WRITE_ASSET:
      return "Cannot write asset file at %s"
    case .ERROR_CANNOT_ENCODE_TEXT_AS_UTF8:
      return "Cannot encode text as utf8 for file at %s"

    //Yandex.Translate Errors
    case .ERROR_TRANSLATE_NO_KEY_FOUND:
      return "Missing API key for the Yandex.Translate service."
    case .ERROR_TRANSLATE_SERVICE_NOT_AVAILABLE:
      return "The translation service is not available; Please try again later."
    case .ERROR_TRANSLATE_JSON_RESPONSE:
      return "The response from the Yandex.Translate service cannot be parsed; Please try again later."

    // FusionTables Errors
    case .FUSION_TABLES_QUERY_ERROR:
      return "Fusion tables returned an error. The query was: %s. The response was: %s"
    case .FUSION_TABLES_AUTH_ERROR:
      return "An error occured when attempting to authenticate for Fusion Tables. The error was %s"

    // Form Errors
    case .ERROR_DIVISION_BY_ZERO:
      return "Trying to divide %s by 0.  The result might not be valid."

    // CloudDB Errors
    case .ERROR_EMPTY_CLOUDDB_PROPERTY:
      return "CloudDB %s property cannot be blank"
    case .ERROR_CLOUDDB_JSON_MALFORMED:
      return "The JSON is malformed and cannot be parsed: %s"

    // Navigation Errors
    case .ERROR_INVALID_API_KEY:
      return "No api key provided"
    case .ERROR_UNABLE_TO_REQUEST_DIRECTIONS:
      return "Unable to request directions. Reason: %s"
    case .ERROR_ROUTING_SERVICE_ERROR:
      return "Routing service failed with status %d %s"
    case .ERROR_NO_ROUTE_FOUND:
      return "No route returned by the routing service."

    // iOS Specific Errors
    case .ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED:
      return "Installing packages from URLs is not supported on iOS"

    // iOS Specific SpeechRecognizer Errors
    case .ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED:
      return "Speech recognition is not supported for iOS versions below 10.0"
    case .ERROR_IOS_SPEECH_RECOGNITION_UNAVAILABLE:
      return "Speech recognition encountered an error and is currently unavailable"
    case .ERROR_IOS_SPEECH_RECOGNITION_PROCESSING_ERROR:
      return "An error occured when attempting to process audio"

    case .ERROR_IOS_GYROSCOPE_SENSOR_DATA_ERROR:
      return "The gyroscope is no longer recording data due to the following error %s"

    case .ERROR_IOS_ORIENTATION_SENSOR_DATA_ERROR:
      return "The orientation sensor is no longer recording data due to the following error %s"
    }
  }
}
