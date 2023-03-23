// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

extension NSDictionary {
  @objc func toString() -> String {
    return (try? getJsonRepresentation(self)) ?? "{unserializable dictionary}"
  }
}
