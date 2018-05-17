// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

func synchronized(_ lock: NSLock, _ code: () -> Void) {
  lock.lock()
  defer { lock.unlock() }
  code()
}

func synchronized(_ lock: NSLock, _ code: () throws -> Void) throws {
  lock.lock()
  defer { lock.unlock() }
  try code()
}
