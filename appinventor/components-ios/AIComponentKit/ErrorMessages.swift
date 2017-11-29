// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

/* Creating New Errors:
 * `case ERROR_NAME = code`
 * (code is now the error's rawValue)
 */
public enum ErrorMessage: Int, Error {
  // ActivityStarter Errors
  case ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY = 601
  case ERROR_ACTIVITY_STARTER_NO_ACTION_INFO = 602
  
  // Media Errors
  case ERROR_UNABLE_TO_PREPARE_MEDIA = 702
  
  // ContactPicker and PhoneNumberPicker Errors
  // "Error message for when contact data cannot be used on the device."
  case ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER = 1107
  
  // Web Errors
  case ERROR_WEB_UNSUPPORTED_ENCODING = 1102
  case ERROR_WEB_MALFORMED_URL = 1109
  case ERROR_WEB_REQUEST_HEADER_NOT_LIST = 1110
  case ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS = 1111
  
  // AccelerometerSensor Errors
  case ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY = 1901
  
  // File Errors
  case ERROR_CANNOT_WRITE_TO_FILE = 2104
  
  // iOS Specific Errors
  case ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED = 100001
  
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
    // ActivityStarter Errors
    case .ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY:
      return "No corresponding activity was found."
      
    case .ERROR_ACTIVITY_STARTER_NO_ACTION_INFO:
      return "No Action information in ActivityStarter was found."
      
    // Media Errors
    case .ERROR_UNABLE_TO_PREPARE_MEDIA:
      return "Unable to prepare %s."
      
    // ContactPicker and PhoneNumberPicker Errors
    case .ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER:
      return "The software used in this app cannot extract contacts from this type of phone."
      
    // Web Errors
    case .ERROR_WEB_UNSUPPORTED_ENCODING:
      return "The encoding %s is not supported."
    case .ERROR_WEB_MALFORMED_URL:
      return "The given URL was not valid."
    case .ERROR_WEB_REQUEST_HEADER_NOT_LIST:
      return "The specified request headers are not valid: element %s is not a list"
    case .ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS:
      return "The specified request headers are not valid: element %s does not contain two elements"
      
    // AccelerometerSensor Errors
    case .ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY:
      return "The value -- %s -- provided for AccelerometerSensor's sensitivity was bad. The only legal values are 1, 2, or 3."
      
    // File Errors
    case .ERROR_CANNOT_WRITE_TO_FILE:
      return "Cannot write to file %s"
      
    // iOS Specific Errors
    case .ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED:
      return "Installing packages from URLs is not supported on iOS"
    }
  }
}
