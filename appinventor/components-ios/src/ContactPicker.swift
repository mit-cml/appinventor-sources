// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import ContactsUI

open class ContactPicker: Picker, AbstractMethodsForPicker, CNContactPickerDelegate {
  fileprivate var _contact: CNContact? = nil

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.titleLabel?.text = NSLocalizedString("ContactPicker", comment: "The class name of the contact picker.") + "1"
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
  }

  // MARK: Properties
  @objc open var ContactName: String {
    get {
      if let contact = _contact {
        return CNContactFormatter.string(from: contact, style: .fullName) ?? ""
      } else {
        return ""
      }
    }
  }

  @objc open var ContactUri: String {
    get {
      return _contact?.identifier ?? ""
    }
  }

  @objc open var EmailAddress: String {
    get {
      if let contact = _contact {
        if contact.emailAddresses.count > 0 {
          return contact.emailAddresses[0].value as String
        }
      }
      return ""
    }
  }

  @objc open var EmailAddressList: [String] {
    get {
      var result = [String]()
      if let contact = _contact {
        for email in contact.emailAddresses {
          result.append(email.value as String)
        }
      }
      return result
    }
  }

  @objc open var PhoneNumber: String {
    get {
      if let contact = _contact {
        if contact.phoneNumbers.count > 0 {
          return contact.phoneNumbers[0].value.stringValue
        }
      }
      return ""
    }
  }

  @objc open var PhoneNumberList: [String] {
    get {
      var result = [String]()
      if let contact = _contact {
        for phone in contact.phoneNumbers {
          result.append(phone.value.stringValue)
        }
      }
      return result
    }
  }

  @objc open var Picture: String {
    get {
      if let contact = _contact {
        if contact.imageDataAvailable, let imageData = contact.imageData {
          var url: URL!
          do {
            url = try URL(fileURLWithPath: "contact.png", relativeTo: FileManager.default.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: true))
            try imageData.write(to: url)
            return url.absoluteString
          } catch {
            _container?.form?.dispatchErrorOccurredEvent(self, "Picture",
                ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE.code, url?.absoluteString ?? "<unknown>")
          }
        }
      }
      return ""
    }
  }

  // MARK: Methods
  @objc open func ViewContact(_ uri: String) {
    if let contact = _contact {
      let abUrl = "addressbook://\(contact.identifier)"
      if let url = URL(string: abUrl) {
        if #available(iOS 10.0, *) {
          UIApplication.shared.open(url, options: [UIApplication.OpenExternalURLOptionsKey : Any]()) { (success: Bool) in
            if !success {
              self.reportViewContactError()
            }
          }
        } else {
          if !UIApplication.shared.openURL(url) {
            reportViewContactError()
          }
        }
      }
    }
  }

  fileprivate func reportViewContactError() {
    _container?.form?.dispatchErrorOccurredEvent(self, "ViewContact",
        ErrorMessage.ERROR_PHONE_UNSUPPORTED_CONTACT_PICKER.code)
  }

  // MARK: CNContactPickerDelegate Implementation
  open func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
    _contact = nil
  }

  open func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {
    _contact = contact
    AfterPicking()
  }

  // MARK: AbstractMethodsForPicker Implementation
  @objc open func open() {
    let picker = CNContactPickerViewController()
    picker.delegate = self
    _container?.form?.present(picker, animated: true)
  }
}

