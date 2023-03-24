// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc public protocol LifecycleDelegate {
  @objc optional func onResume()
  @objc optional func onPause()
  @objc optional func onDelete()
  @objc optional func onDestroy()
}
