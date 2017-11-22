//
//  ActivityStarter.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

private let ACTION_VIEW = "android.intent.action.VIEW"

open class ActivityStarter: NonvisibleComponent {
  fileprivate var _action: String = ""
  fileprivate var _dataUri: String = ""
  fileprivate var _dataType: String = ""
  fileprivate var _activityPackage: String = ""
  fileprivate var _activityClass: String = ""
  fileprivate var _extraKey: String = ""
  fileprivate var _extraValue: String = ""
  fileprivate var _resultName: String = ""
  fileprivate var _result: String = ""

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  // MARK: ActivityStarter Properties
  open var Action: String {
    get {
      return _action
    }
    set(action) {
      _action = action
    }
  }

  open var ActivityClass: String {
    get {
      return _activityClass
    }
    set(activityClass) {
      _activityClass = activityClass
    }
  }

  open var ActivityPackage: String {
    get {
      return _activityPackage
    }
    set(activityPackage) {
      _activityPackage = activityPackage
    }
  }

  open var DataType: String {
    get {
      return _dataType
    }
    set(dataType) {
      _dataType = dataType
    }
  }

  open var DataUri: String {
    get {
      return _dataUri
    }
    set(dataUri) {
      _dataUri = dataUri
    }
  }

  open var ExtraKey: String {
    get {
      return _extraKey
    }
    set(extraKey) {
      _extraKey = extraKey
    }
  }

  open var ExtraValue: String {
    get {
      return _extraValue
    }
    set(extraValue) {
      _extraValue = extraValue
    }
  }

  open var ResultName: String {
    get {
      return _resultName
    }
    set(resultName) {
      _resultName = resultName
    }
  }

  // MARK: ActivityStarter Methods
  open func ResolveActivity() -> Bool {
    if _action == ACTION_VIEW, let url = URL(string: _dataUri) {
      if UIApplication.shared.canOpenURL(url) {
          return true
      }
    }
    return false
  }

  open func StartActivity() {
    if self._action == "" {
      _form.dispatchErrorOccurredEvent(self, "StartActivity",
                                       ErrorMessage.ERROR_ACTIVITY_STARTER_NO_ACTION_INFO.code,
                                       ErrorMessage.ERROR_ACTIVITY_STARTER_NO_ACTION_INFO.message)
    } else if self.ResolveActivity() {
      if let url = URL(string: _dataUri) {
        UIApplication.shared.openURL(url)
      }
    } else {
      _form.dispatchErrorOccurredEvent(self, "StartActivity",
                                       ErrorMessage.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY.code,
                                       ErrorMessage.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY.message)
    }
  }

  // MARK: ActivityStarter Events
  open func ActivityCanceled() {
    EventDispatcher.dispatchEvent(of: self, called: "ActivityCanceled")
  }

  open func ActivityError(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ActivityError", arguments: message as NSString)
  }

  open func AfterActivity(_ result: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterActivity", arguments: result as NSString)
  }
}
