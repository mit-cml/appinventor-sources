// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import LocalAuthentication

@objc
open class FingerPrint: NonvisibleComponent {
  private var context = LAContext()
  private var _useDialog = true
  private var _lightTheme = false
  private var _dialogTitle = "Authentication"
  private var _dialogHelpText = ""
  
  @objc var HasFingerPrintScanner: Bool {
    return true
  }
  
  @objc var HasFingersAdded: Bool {
    return context.biometryType == .touchID || context.biometryType == .faceID
  }
  
  @objc var DialogHelpText: String {
    get {
      return _dialogHelpText
    }
    set {
      _dialogHelpText = newValue
    }
  }
  
  @objc var DialogTitle: String {
    get {
      return _dialogTitle
    }
    set {
      _dialogTitle = newValue
    }
  }
  
  @objc var LightTheme: Bool {
    get {
      return _lightTheme
    }
    set {
      _lightTheme = newValue
    }
  }
  
  @objc var UseDialog: Bool {
    get {
      return _useDialog
    }
    set {
      _useDialog = newValue
    }
  }
  
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }
  
  @objc public func OnAuthenticationError() {
    print("Authentication Error")
    EventDispatcher.dispatchEvent(of: self, called: "Authentication Error")
  }
  @objc public func OnAuthenticationFailed() {
    print("Authentication Failed")
    EventDispatcher.dispatchEvent(of: self, called: "Authentication Failed")
  }
  @objc public func OnAuthenticationHelp(errorMessage : String) {
    print("Authentication error: \(errorMessage)")
    EventDispatcher.dispatchEvent(of: self, called: "Authentication error: \(errorMessage)")
  }
  
  @objc public func OnAuthenticationSucceeded() {
    print("Authentication succeeded")
    EventDispatcher.dispatchEvent(of: self, called: "Authentication succeeded")
  }
  
  @objc func Authenticate() {
    guard HasFingerPrintScanner else {
      return
    }
    
    var policy: LAPolicy = .deviceOwnerAuthentication
    if _useDialog {
      policy = .deviceOwnerAuthenticationWithBiometrics
    }
    context = LAContext()
    
    context.evaluatePolicy(policy, localizedReason: _dialogTitle, reply: { [weak self] (success, error) in
      guard let self = self else {
        return
      }
      
      DispatchQueue.main.async {
        if success {
          self.OnAuthenticationSucceeded()
        } else {
          if let error = error as NSError? {
            let errorCode = error.code
            
            switch errorCode {
            case LAError.authenticationFailed.rawValue:
              self.OnAuthenticationFailed()
            case LAError.userCancel.rawValue:
              print("Authentication cancelled by user")
              break
            case LAError.userFallback.rawValue:
              print("User chose to use fallback authentication")
              break
            case LAError.systemCancel.rawValue:
              print("Authentication cancelled by system")
              break
            default:
              let errorMessage = error.localizedDescription
              self.OnAuthenticationHelp(errorMessage: errorMessage)
            }
            
            self.OnAuthenticationError()
          }
        }
      }
    })
  }
  
  @objc func cancelScan() {
    context.invalidate()
  }
}

