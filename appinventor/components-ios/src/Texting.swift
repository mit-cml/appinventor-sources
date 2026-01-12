// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MessageUI

open class Texting: NonvisibleComponent, MFMessageComposeViewControllerDelegate {
  fileprivate var _phoneNumber = ""
  fileprivate var _message = ""

  // MARK: Texting Properties
  @objc open var PhoneNumber: String {
    get {
      return _phoneNumber
    }
    set(phoneNumber) {
      _phoneNumber = phoneNumber
    }
  }

  @objc open var Message: String {
    get {
      return _message
    }
    set(message) {
      _message = message
    }
  }

  /**
   * GoogleVoiceEnabled is provided for compatbility with the Android version. Sending a message
   * using Google Voice is not supported on iOS, so this is a no-op.
   */
  @objc open var GoogleVoiceEnabled: Bool {
    get {
      return false  // Google Voice not supported on iOS
    }
    set(enabled) {
      if _form?.isRepl ?? false {
        // Google Voice not supported on iOS
        _form?.dispatchErrorOccurredEvent(self, "GoogleVoiceEnabled",
                                          .ERROR_IOS_GOOGLEVOICE_NOT_SUPPORTED)
      }
    }
  }

  @objc open var ReceivingEnabled: Int32 {
    get {
      return ReceivingState.Off.value  // Receiving messages not supported on iOS
    }
    set(enabled) {
      if enabled == ReceivingState.Off.value {
        return  // No state change since Off is the only valid value on iOS.
      }
      if _form?.isRepl ?? false {
        // Receiving messages not supported on iOS
        _form?.dispatchErrorOccurredEvent(self, "ReceivingEnabled",
                                          .ERROR_IOS_RECEIVING_NOT_SUPPORTED)
      }
    }
  }

  // MARK: Texting Methods
  @objc open func SendMessage() {
    if !MFMessageComposeViewController.canSendText() {
      let alert = UIAlertController(title: "Texting not available", message: "Texting is not supported on this device.", preferredStyle: .alert)
      alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
      _form?.present(alert, animated: true, completion: nil)
      return
    }
    let messageVC = MFMessageComposeViewController()
    messageVC.messageComposeDelegate = self
    messageVC.body = _message
    messageVC.recipients = [_phoneNumber]
    _form?.present(messageVC, animated: true, completion: nil)
  }

  @objc open func SendMessageDirect() {
    SendMessage()
    if _form?.isRepl ?? false {
      RetValManager.shared().sendError("SendMessageDirect is not supported. Use SendMessage instead.")
    }
  }

  // MARK: Texting Events
  @objc open func MessageReceived(_ number: String, _ messageText: String) {
    EventDispatcher.dispatchEvent(of: self, called: "MessageReceived", arguments: number as NSString, messageText as NSString)
  }

  // MARK: MFMessageViewControllerDelegate implementation
  open func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
    controller.dismiss(animated: true, completion: nil)
  }
}
