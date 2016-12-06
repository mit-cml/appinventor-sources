//
//  ActivityStarter.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class ActivityStarter: NonvisibleComponent {
  private var _action: String = ""
  private var _dataUri: String = ""
  private var _dataType: String = ""
  private var _activityPackage: String = ""
  private var _activityClass: String = ""
  private var _extraKey: String = ""
  private var _extraValue: String = ""
  private var _resultName: String = ""
  private var _result: String = ""

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }

  // MARK: ActivityStarter Properties
  public var Action: String {
    get {
      return _action
    }
    set(action) {
      _action = action
    }
  }

  public var ActivityClass: String {
    get {
      return _activityClass
    }
    set(activityClass) {
      _activityClass = activityClass
    }
  }

  public var ActivityPackage: String {
    get {
      return _activityPackage
    }
    set(activityPackage) {
      _activityPackage = activityPackage
    }
  }

  public var DataType: String {
    get {
      return _dataType
    }
    set(dataType) {
      _dataType = dataType
    }
  }

  public var DataUri: String {
    get {
      return _dataUri
    }
    set(dataUri) {
      _dataUri = dataUri
    }
  }

  public var ExtraKey: String {
    get {
      return _extraKey
    }
    set(extraKey) {
      _extraKey = extraKey
    }
  }

  public var ExtraValue: String {
    get {
      return _extraValue
    }
    set(extraValue) {
      _extraValue = extraValue
    }
  }

  public var ResultName: String {
    get {
      return _resultName
    }
    set(resultName) {
      _resultName = resultName
    }
  }

  // MARK: ActivityStarter Methods
  public func ResolveActivity() -> Bool {
    if _action == "android.intent.activity.VIEW" {
      if let url = URL(string: _dataUri) {
        if UIApplication.shared.canOpenURL(url) {
          return true
        }
      }
    }
    return false
  }

  public func StartActivity() {
    if self._action == "" {
      _form?.dispatchErrorOccurredEvent(self, "StartActivity",
                                        ErrorMessages.ERROR_ACTIVITY_STARTER_NO_ACTION_INFO.code,
                                        ErrorMessages.ERROR_ACTIVITY_STARTER_NO_ACTION_INFO.message)
    } else if self.ResolveActivity() {
      if let url = URL(string: _dataUri) {
        UIApplication.shared.openURL(url)
      }
    } else {
      _form?.dispatchErrorOccurredEvent(self, "StartActivity",
                                        ErrorMessages.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY.code,
                                        ErrorMessages.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY.message)
    }
  }

  // MARK: ActivityStarter Events
  public func ActivityCanceled() {
    EventDispatcher.dispatchEvent(of: self, called: "ActivityCanceled")
  }

  public func ActivityError(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ActivityError", arguments: message as NSString)
  }

  public func AfterActivity(_ result: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterActivity", arguments: result as NSString)
  }
}
