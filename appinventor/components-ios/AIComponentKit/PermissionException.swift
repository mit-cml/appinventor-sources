// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

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
