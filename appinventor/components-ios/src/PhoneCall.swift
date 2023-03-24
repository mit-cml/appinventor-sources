// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import CoreTelephony

private enum PhoneCallState {
  case disconnected
  case dialing
  case ringing
  case connectedIncoming
  case connectedOutgoing
  case connectedUnknown
}

private let kPhoneCallStatusMissed: Int32 = 1
private let kPhoneCallStatusIncomingEnded: Int32 = 2
private let kPhoneCallStatusOutgoingEnded: Int32 = 3

open class PhoneCall: NonvisibleComponent {
  fileprivate var _phoneNumber = ""
  fileprivate let _callCenter: CTCallCenter
  fileprivate var _status = PhoneCallState.disconnected

  public override init(_ parent: ComponentContainer) {
    _callCenter = CTCallCenter()
    super.init(parent)
    _callCenter.callEventHandler = {(call: CTCall) in
      if call.callState == CTCallStateIncoming {
        self._status = .ringing
        self.PhoneCallStarted(1, call.callID)
      } else if call.callState == CTCallStateDialing {
        self._status = .dialing
        self.PhoneCallStarted(2, call.callID)
      } else if call.callState == CTCallStateConnected {
        if self._status == .dialing {
          self._status = .connectedOutgoing
        } else if self._status == .ringing {
          self._status = .connectedIncoming
          self.IncomingCallAnswered(call.callID)
        } else if self._status == .disconnected {
          self._status = .connectedUnknown
        }
      } else if call.callState == CTCallStateDisconnected {
        var callStatus: Int32 = 0
        if self._status == .ringing {
          callStatus = 1
        } else if self._status == .connectedIncoming {
          callStatus = 2
        } else if self._status == .connectedOutgoing {
          callStatus = 3
        }
        self.PhoneCallEnded(callStatus, call.callID)
      }
    }
  }

  // MARK: PhoneCall Properties
  @objc open var PhoneNumber: String {
    get {
      return _phoneNumber
    }
    set(number) {
      _phoneNumber = number
    }
  }

  // MARK: PhoneCall Methods
  @objc open func MakePhoneCall() {
    let cleanNumber = _phoneNumber.components(separatedBy: CharacterSet(charactersIn: "0123456789+-()").inverted).joined()
    if let telurl = URL(string: "tel:" + cleanNumber) {
      if #available(iOS 10.0, *) {
        UIApplication.shared.open(telurl, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: { (success: Bool) in
          self.PhoneCallStarted(1, self._phoneNumber)
        })
      } else {
        UIApplication.shared.openURL(telurl)
      }
    }
  }

  @objc open func MakePhoneCallDirect() {
    MakePhoneCall()
    if (_form?.isRepl ?? false) {
      RetValManager.shared().sendError("MakePhoneCallDirect is not supported. Use MakePhoneCall instead.")
    }
  }

  // MARK: PhoneCall Events
  @objc open func IncomingCallAnswered(_ phoneNumber: String) {
    EventDispatcher.dispatchEvent(of: self, called: "IncomingCallAnswered", arguments: phoneNumber as NSString)
  }

  @objc open func PhoneCallEnded(_ status: Int32, _ phoneNumber: String) {
    EventDispatcher.dispatchEvent(of: self, called: "PhoneCallEndded", arguments: NSNumber(value: status), phoneNumber as NSString)
  }

  @objc open func PhoneCallStarted(_ status: Int32, _ phoneNumber: String) {
    EventDispatcher.dispatchEvent(of: self, called: "PhoneCallStarted", arguments: NSNumber(value: status), phoneNumber as NSString)
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToUIApplicationOpenExternalURLOptionsKeyDictionary(_ input: [String: Any]) -> [UIApplication.OpenExternalURLOptionsKey: Any] {
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (UIApplication.OpenExternalURLOptionsKey(rawValue: key), value)})
}
