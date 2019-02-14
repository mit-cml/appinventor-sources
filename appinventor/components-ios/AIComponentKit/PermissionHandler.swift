// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Speech
import Contacts
import CoreLocation

/**
 A set of possible permissions that can be requested
 */
public enum Permission: String {
  /**
   * Camera permissions
   */
  case camera
  /**
   * Location permissions
   */
  case location
  /**
   * Microphone permission
   */
  case microphone
  /**
   * Speech recognition permissions
   */
  case speech

}

/**
 Used to handle permissions for components
 */
open class PermissionHandler: NSObject, CLLocationManagerDelegate {
  public typealias ResultBlock = (_ allowed: Bool, _ changed: Bool) -> ()

  /**
   * The shared PermissionHandler object, used for location requests
   */
  fileprivate static let main = PermissionHandler()

  /**
   * An Array of CodeBlocks to be executed when requesting location permissions
   */
  fileprivate static var completion = [ResultBlock]()

  /**
   * A shared queue to ensure access to completion is thread safe
   */
  fileprivate static let codeQueue = DispatchQueue(label: "PermissionQueue", attributes: .concurrent)

  fileprivate static var manager: CLLocationManager? = nil


  /**
   * Tests whether the user has granted camera/microphone/speech permissions to the app
   * @param status the specific status being requested
   * @returns true if the user has explicitly granted permissions, false if the user has explicitly denied permissions, or nil if permissions are uncertain
   */
  public static func HasPermission(for status: Permission) -> Bool? {
    switch status {
    case .camera:
      return HasCameraPermission()
    case .microphone:
      return HasMicrophonePermission()
    case .speech:
      return HasSpeechRecognitionPermission()
    case .location:
      return HasLocationPermissions()
    }
  }

  /**
   * Tests whether the user has provided camera permissions
   * @returns true if the user has granted camera permissions, false if the user has denied permissions, and nil if the status is unknown
   */
  fileprivate static func HasCameraPermission() -> Bool? {
    switch AVCaptureDevice.authorizationStatus(for: AVMediaType(rawValue: convertFromAVMediaType(AVMediaType.video))) {
    case .authorized:
      return true
    case .restricted, .denied:
      return false
    case .notDetermined:
      return nil
    }
  }

  /**
   * Tests whether the user has provided location permissions
   * @returns true if the user has granted location permissions, false if the user has denied permissions, and nil if the status is unknown
   */
  fileprivate static func HasLocationPermissions() -> Bool? {
    if CLLocationManager.locationServicesEnabled() {
      switch CLLocationManager.authorizationStatus() {
      case .authorizedAlways, .authorizedWhenInUse:
        return true
      case .notDetermined:
        return nil
      default:
        return false
      }
    } else {
      return false
    }
  }

  /**
   * Tests whether the user has provided microphone permissions
   * @returns true if the user has granted microphone permissions, nil if the status is unknown, and false otherwise
   */
  fileprivate static func HasMicrophonePermission() -> Bool? {
    switch AVAudioSession.sharedInstance().recordPermission {
    case .granted:
      return true
    case .undetermined:
      return nil
    default:
      return false
    }
  }

  /**
   * Tests whether the user has provided speech recognition permissions
   * requires iOS >= 10.0
   * @returns true if the user has granted camera speech recognition, false if the user has denied permissions or if the user is not on iOS 10.0 or above, and nil if the status is unknown
   */
  fileprivate static func HasSpeechRecognitionPermission() -> Bool? {
    if #available(iOS 10.0, *) {
      switch SFSpeechRecognizer.authorizationStatus() {
      case .authorized:
        return true
      case .denied, .restricted:
        return false
      case .notDetermined:
        return nil
      }
    } else {
      return false
    }
  }

  /**
   * Requests audio, camera, location, or speech recognition permissions for a component
   * @param permission the specific permission (camera, microphone, speech) being requested
   * @param completionHandler code from the component to be executed after the request finishes
   * @param allowed whether the requested permission was granted
   * @param changed whether the status changed for the requested permission
   */
  public static func RequestPermission(for permission: Permission, with completionHandler: ResultBlock? = nil){
    switch permission {
    case .camera:
      RequestCameraPermission(with: completionHandler)
    case .microphone:
      RequestMicrophonePermission(with: completionHandler)
    case .speech:
      RequestSpeechRecognitionPermission(with: completionHandler)
    case .location:
      RequestLocationPermission(with: completionHandler)
    }
  }

  /**
   * Requests camera permission
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestCameraPermission(with completionHandler: ResultBlock?  = nil){
    let cameraAuthorized = HasCameraPermission()
    AVCaptureDevice.requestAccess(for: AVMediaType.video) { allowed in
      if let handler = completionHandler{
        let permissionChanged = cameraAuthorized == nil || cameraAuthorized != allowed
        handler(allowed, permissionChanged)
      }
    }
  }

  /**
   * Requests location permission
   * If the authorization status is known, will immediately call the handler
   * If authorization status is unknown, will append handler to an array of blocks
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestLocationPermission(with completionHandler: ResultBlock? = nil) {
    if let permission = HasLocationPermissions() {
      if let handler = completionHandler {
        handler(permission, false)
      }
    } else {
      PermissionHandler.manager = CLLocationManager()
      PermissionHandler.manager?.delegate = PermissionHandler.main
      PermissionHandler.codeQueue.async(flags: .barrier) {
        if let handler = completionHandler {
          PermissionHandler.completion.append(handler)
        }
      }
      PermissionHandler.manager?.requestWhenInUseAuthorization()
    }
  }

  /**
   * Method for CLLocationManagerDelegate, called when authorization changed
   * Calls all received location permission requests in the order received
   * Upon completion, all blocks are removed
   */
  public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
    if status != .notDetermined {
      PermissionHandler.codeQueue.sync {
        for block in PermissionHandler.completion {
          block(status == .authorizedAlways || status == .authorizedWhenInUse, true)
        }
        PermissionHandler.completion.removeAll()
        PermissionHandler.manager?.delegate = nil
        PermissionHandler.manager = nil
      }
    }
  }

  /**
   * Requests microphone permission
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestMicrophonePermission(with completionHandler: ResultBlock? = nil){
    let microphoneAuthorized = HasMicrophonePermission()
    AVAudioSession.sharedInstance().requestRecordPermission { allowed in
      if let handler = completionHandler {
        let permissionChanged = microphoneAuthorized == nil || microphoneAuthorized != allowed
        handler(allowed, permissionChanged)
      }
    }
  }

  /**
   * Requests speech recognition permission
   * requires iOS >= 10.0
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestSpeechRecognitionPermission(with completionHandler: ResultBlock? = nil) {
    let speechAuthorized = HasSpeechRecognitionPermission()
    if #available(iOS 10.0, *) {
      SFSpeechRecognizer.requestAuthorization { allowed in
        let result = allowed == .authorized
        if let handler = completionHandler {
          let permissionChanged = speechAuthorized == nil || speechAuthorized != result
          handler(result, permissionChanged)
        }
      }
    }
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromAVMediaType(_ input: AVMediaType) -> String {
	return input.rawValue
}
