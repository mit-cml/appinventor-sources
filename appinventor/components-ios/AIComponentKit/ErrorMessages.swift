// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public struct ErrorMessage {
  public let code: Int32
  public let message: String
  public init(code: Int32, message: String) {
    self.code = code
    self.message = message
  }
}

public final class ErrorMessages {
  public static let ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY = ErrorMessage(code: 601, message: "No corresponding activity was found.")
  public static let ERROR_ACTIVITY_STARTER_NO_ACTION_INFO = ErrorMessage(code: 602, message: "No Action information in ActivityStarter was found.")

  public static let ERROR_UNABLE_TO_PREPARE_MEDIA = ErrorMessage(code: 702, message: "Unable to prepare %s.")

  public static let ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER = ErrorMessage(code: 1107, message: NSLocalizedString("The software used in this app cannot extract contacts from this type of phone.", comment: "Error message for when contact data cannot be used on the device."))
  public static let ERROR_WEB_REQUEST_HEADER_NOT_LIST = ErrorMessage(code: 1110, message: "The specified request headers are not valid: element %s is not a list")
  public static let ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS = ErrorMessage(code: 1111, message: "The specified request headers are not valid: element %s does not contain two elements")

  public static let ERROR_BAD_VALUE_FOR_ACCELEROMETER_SENSITIVITY = ErrorMessage(code: 1901, message: "The value -- %s -- provided for AccelerometerSensor's sensitivity was bad. The only legal values are 1, 2, or 3.")

  public static let ERROR_CANNOT_WRITE_TO_FILE = ErrorMessage(code: 2104, message: NSLocalizedString("Cannot write to file %s", comment: "Error message for when a file cannot be written."))

  public static let ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED = ErrorMessage(code: 3300, message: "Installing packages from URLs is not supported on iOS")

}
