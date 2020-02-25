// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public protocol LifecycleDelegate {
  @objc optional func onResume()
  @objc optional func onPause()
  @objc optional func onDelete()
  @objc optional func onDestroy()
}
