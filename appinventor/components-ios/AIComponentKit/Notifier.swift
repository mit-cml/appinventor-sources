//
//  Notifier.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import Toast_Swift

open class Notifier: NonvisibleComponent {
  fileprivate var _notifierLength = ToastLength.long.rawValue
  fileprivate var _backgroundColor = Int32(bitPattern: Color.darkGray.rawValue)
  fileprivate var _textColor = Int32(bitPattern: Color.white.rawValue)

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // MARK: Notifier Properties
  open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
    }
  }

  open var NotifierLength: Int32 {
    get {
      return _notifierLength
    }
    set(length) {
      _notifierLength = length
    }
  }

  open var TextColor: Int32 {
    get {
      return _textColor
    }
    set(argb) {
      _textColor = argb
    }
  }

  // MARK: Notifier Methods
  open func DismissProgressDialog() {
    // TODO: implementation
  }

  open func LogError(_ message: String) {
    NSLog("Error: \(message)")
  }

  open func LogInfo(_ message: String) {
    NSLog("Info: \(message)")
  }

  open func LogWarning(_ message: String) {
    NSLog("Warning: \(message)")
  }

  open func ShowAlert(_ notice: String) {
    let duration = TimeInterval(_notifierLength == 1 ? 3.5 : 2.0)
    _form.view.window?.makeToast(notice, duration: duration, position: ToastPosition.center)
  }

  open func ShowChooseDialog(_ message: String, _ title: String, _ button1text: String, _ button2text: String, _ cancelable: Bool) {
    let dialog = UIAlertController(title: title, message: message, preferredStyle: .actionSheet)
    let btn1Action = UIAlertAction(title: button1text, style: .default) { (action: UIAlertAction) in
      self.performSelector(onMainThread: #selector(self.AfterChoosing(_:)), with: button1text, waitUntilDone: false)
    }
    dialog.addAction(btn1Action)
    let btn2Action = UIAlertAction(title: button2text, style: .default) { (action: UIAlertAction) in
      self.performSelector(onMainThread: #selector(self.AfterChoosing(_:)), with: button2text, waitUntilDone: false)
    }
    dialog.addAction(btn2Action)
    if (cancelable) {
      let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { (action: UIAlertAction) in
        self.performSelector(onMainThread: #selector(self.AfterChoosing(_:)), with: "", waitUntilDone: false)
      }
      dialog.addAction(cancelAction)
    }
    _form.present(dialog, animated: true, completion: {})
  }

  open func ShowMessageDialog(_ message: String, _ title: String, _ buttonText: String) {
    let dialog = UIAlertController(title: title, message: message, preferredStyle: .alert)
    let okAction = UIAlertAction(title: buttonText, style: .default) { (action: UIAlertAction) in }
    dialog.addAction(okAction)
    dialog.preferredAction = okAction
    _form.present(dialog, animated: true, completion: {})
  }

  open func ShowProgressDialog(_ message: String, _ title: String) {
    // TODO: implementation
  }

  open func ShowTextDialog(_ message: String, _ title: String, _ cancelable: Bool) {
    let dialog = UIAlertController(title: title, message: message, preferredStyle: .alert)
    dialog.addTextField { (textbox: UITextField) in }
    let okAction = UIAlertAction(title: "OK", style: .default) { (action: UIAlertAction) in
      self.performSelector(onMainThread: #selector(self.AfterTextInput(_:)), with: dialog.textFields?[0].text, waitUntilDone: false)
    }
    dialog.addAction(okAction)
    dialog.preferredAction = okAction
    if cancelable {
      let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { (action: UIAlertAction) in
        self.performSelector(onMainThread: #selector(self.AfterTextInput(_:)), with: "", waitUntilDone: false)
      }
      dialog.addAction(cancelAction)
    }
    _form.present(dialog, animated: true, completion: {})
  }

  // MARK: Notifier Events
  open func AfterChoosing(_ choice: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterChoosing", arguments: choice as NSString)
  }

  open func AfterTextInput(_ response: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterTextInput", arguments: response as NSString)
  }
}
