//
//  PhoneStatus.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import CoreFoundation

public class PhoneStatus : NonvisibleComponent {
  // MARK: PhoneStatus Methods
  public class func GetWifiIpAddress() -> String {
    return NetworkUtils.getIPAddress()
  }

  public class func isConnected() -> Bool {
    return NetworkUtils.getIPAddress() != "error"
  }

  public func setHmacSeedReturnCode(_ seed: String) -> String {
    AppInvHTTPD.setHmacKey(seed)
    return seed.sha1
  }

  public func isDirect() -> Bool {
    // iOS Companion only runs via Wifi
    return false
  }

  public func startHTTPD(_ secure: Bool) {
    ReplForm.topform?.startHTTPD(secure: secure)
  }

  public func setAssetsLoaded() {
    if _form is ReplForm {
      (_form as! ReplForm).setAssetsLoaded()
    }
  }

  public class func doFault() throws {
    throw NSError(domain: "AIComponentKit", code: -1)
  }

  public func getVersionName() -> String {
    let info = Bundle.main.infoDictionary
    if let versionString = info?["CFBundleShortVersionString"] as? String {
      return versionString
    } else {
      return ""
    }
  }

  public func installUrl(_ url: String) {
    // not implemented for iOS
    _form?.dispatchErrorOccurredEvent(self, "installUrl", ErrorMessages.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.code, ErrorMessages.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.message)
  }

  public func shutdown() {
    exit(0)
  }

  // MARK: PhoneStatus Events
  public func OnSettings() {
    EventDispatcher.dispatchEvent(of: self, called: "OnSettings")
  }
}
