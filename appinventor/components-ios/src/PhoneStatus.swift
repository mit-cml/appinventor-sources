// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreFoundation

open class PhoneStatus : NonvisibleComponent {
  private var firstSeed: String? = nil
  private var firstHmacSeed = ""

  // MARK: PhoneStatus Methods
  @objc open class func GetWifiIpAddress() -> String {
    return NetworkUtils.getIPAddress()
  }

  @objc open class func isConnected() -> Bool {
    return NetworkUtils.getIPAddress() != "error"
  }

  @objc open func setHmacSeedReturnCode(_ seed: String) -> String {
    guard firstSeed == nil else {
      return firstHmacSeed
    }
    firstSeed = seed
    AppInvHTTPD.setHmacKey(seed)
    firstHmacSeed = seed.sha1
    return firstHmacSeed
  }

  @objc open func isDirect() -> Bool {
    // iOS Companion only runs via Wifi
    return false
  }

  @objc open func startWebRTC(_ rendezvousServer: String, _ iceServers: String) {
    guard WebRTC else {
      return
    }
    guard let firstSeed = self.firstSeed else {
      return
    }
    let manager = WebRTCNativeManager(rendezvousServer, iceServers)
    manager.initiate(_form as! ReplForm, firstSeed)
  }

  @objc open func startHTTPD(_ secure: Bool) {
    ReplForm.topform?.startHTTPD(secure)
  }

  @objc open func setAssetsLoaded() {
    if _form is ReplForm {
      (_form as! ReplForm).setAssetsLoaded()
    }
  }

  @objc open class func doFault() throws {
    throw NSError(domain: "AIComponentKit", code: -1)
  }

  @objc open func getVersionName() -> String {
    let info = Bundle.main.infoDictionary
    if let versionString = info?["CFBundleShortVersionString"] as? String {
      return versionString
    } else {
      return ""
    }
  }

  @objc open func installUrl(_ url: String) {
    // not implemented for iOS
    _form?.dispatchErrorOccurredEvent(self, "installUrl",
        ErrorMessage.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.code,
        ErrorMessage.ERROR_IOS_INSTALLING_URLS_NOT_SUPPORTED.message)
  }

  @objc open func shutdown() {
    exit(0)
  }

  @objc open func SdkLevel() -> String {
    return UIDevice.current.systemVersion
  }

  @objc open func GetVersionName() -> String {
    return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
  }

  @objc open func GetInstaller() -> String {
    #if DEBUG
    return "xcode"
    #else
    if let path = Bundle.main.appStoreReceiptURL?.path {
      return path.contains("sandboxReceipt") ? "testflight" : "appstore"
    } else {
      return "unknown"
    }
    #endif
  }

  @objc open func InstallationId() -> String {
    let id_file = FileUtil.absoluteFileName("ACRA-INSTALLATION", false)
    if let id = try? String(contentsOfFile: id_file) {
      return id
    } else {
      let id = UUID().uuidString
      do {
        try id.write(toFile: id_file, atomically: true, encoding: .utf8)
      } catch {
        print("Unable to create installation ID")
      }
      return id
    }
  }

  // MARK: PhoneStatus Events
  @objc open func OnSettings() {
    EventDispatcher.dispatchEvent(of: self, called: "OnSettings")
  }

  // MARK: PhoneStatus Properties
  @objc open var WebRTC: Bool = false
  
  @objc open var UseProxy: Bool = true
  
  @objc open func setPopup(_ popup : String) {
    AppInvHTTPD.setPopup(popup);
  }
}
