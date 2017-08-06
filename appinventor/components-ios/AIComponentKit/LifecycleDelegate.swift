// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public protocol LifecycleDelegate {
  func onResume()
  func onPause()
  func onDelete()
  func onDestroy()
}
