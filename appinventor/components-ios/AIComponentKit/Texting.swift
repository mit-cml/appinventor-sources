//
//  Textign.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import MessageUI

open class Texting: NonvisibleComponent, MFMessageComposeViewControllerDelegate {
  fileprivate var _phoneNumber = ""
  fileprivate var _message = ""

  // MARK: Texting Properties
  open var PhoneNumber: String {
    get {
      return _phoneNumber
    }
    set(phoneNumber) {
      _phoneNumber = phoneNumber
    }
  }

  open var Message: String {
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
  open var GoogleVoiceEnabled: Bool {
    get {
      return false  // Google Voice not supported on iOS
    }
    set(enabled) {
      // Google Voice not supported on iOS
      _form?.view.makeToast("Sorry, your phone's system does not support this option.")
    }
  }

  open var ReceivingEnabled: Bool {
    get {
      return false  // Receiving messages not supported on iOS
    }
    set(enabled) {
      // Receiving messages not supported on iOS
      _form?.view.makeToast("Sorry, your phone's system does not support this option.")
    }
  }

  // MARK: Texting Methods
  open func SendMessage() {
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

  // MARK: Texting Events
  open func MessageReceived(_ number: String, _ messageText: String) {
    EventDispatcher.dispatchEvent(of: self, called: "MessageReceived", arguments: number as NSString, messageText as NSString)
  }

  // MARK: MFMessageViewControllerDelegate implementation
  open func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
    controller.dismiss(animated: true, completion: nil)
  }
}
