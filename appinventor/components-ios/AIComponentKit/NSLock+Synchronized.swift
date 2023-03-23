// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
