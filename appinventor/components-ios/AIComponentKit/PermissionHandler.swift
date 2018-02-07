// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Speech
import Contacts

/**
 A set of possible permissions that can be requested
 */
public enum Permission: String {
  /**
   * Camera permissions
   */
  case camera
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
open class PermissionHandler {
  /**
   * Tests whether the user has granted camera/microphone/speech permissions to the app
   * @param status the specific status being requested
   * @returns true if the user has explicitly granted permissions, false if the user has explicitly denied permissions, or nil if permissions are uncertain
   */
  open static func HasPermission(for status: Permission) -> Bool? {
    switch status {
    case .camera:
      return HasCameraPermission()
    case .microphone:
      return HasMicrophonePermission()
    case .speech:
      return HasSpeechRecognitionPermission()
    }
  }

  /**
   * Tests whether the user has provided camera permissions
   * @returns true if the user has granted camera permissions, false if the user has denied permissions, and nil if the status is unknown
   */
  fileprivate static func HasCameraPermission() -> Bool? {
    switch AVCaptureDevice.authorizationStatus(forMediaType: AVMediaTypeVideo) {
    case .authorized:
      return true
    case .restricted, .denied:
      return false
    case .notDetermined:
      return nil
    }
  }

  /**
   * Tests whether the user has provided microphone permissions
   * @returns true if the user has granted microphone permissions, nil if the status is unknown, and false otherwise
   */
  fileprivate static func HasMicrophonePermission() -> Bool? {
    switch AVAudioSession.sharedInstance().recordPermission() {
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
   * Requests audio, camera, or speech recognition permissions for a component
   * @param permission the specific permission (camera, microphone, speech) being requested
   * @param completionHandler code from the component to be executed after the request finishes
   * @param allowed whether the requested permission was granted
   * @param changed whether the status changed for the requested permission
   */
  open static func RequestPermission(for permission: Permission, with completionHandler: ((_ allowed: Bool, _ changed: Bool) -> ())? = nil){
    switch permission {
    case .camera:
      RequestCameraPermission(with: completionHandler)
    case .microphone:
      RequestMicrophonePermission(with: completionHandler)
    case .speech:
      RequestSpeechRecognitionPermission(with: completionHandler)
    }
  }

  /**
   * Requests camera permission
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestCameraPermission(with completionHandler: ((Bool, Bool) -> ())? = nil){
    let cameraAuthorized = HasCameraPermission()
    AVCaptureDevice.requestAccess(forMediaType: AVMediaTypeVideo) { allowed in
      if let handler = completionHandler{
        let permissionChanged = cameraAuthorized == nil || cameraAuthorized != allowed
        handler(allowed, permissionChanged)
      }
    }
  }

  /**
   * Requests microphone permission
   * @param completionHandler the code that will be called after execution
   */
  fileprivate static func RequestMicrophonePermission(with completionHandler: ((Bool, Bool) -> ())? = nil){
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
  fileprivate static func RequestSpeechRecognitionPermission(with completionHandler: ((Bool, Bool) -> ())? = nil) {
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
