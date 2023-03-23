// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class PermissionException : NSException {
  public let permissionNeeded: String

  public init(permission: String) {
    permissionNeeded = permission
    super.init(name: NSExceptionName(rawValue: "PermissionDenied"), reason: nil, userInfo: nil)
  }

  required init?(coder: NSCoder) {
    permissionNeeded = coder.decodeObject() as! String
    super.init(coder: coder)
  }
}
