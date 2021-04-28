// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2021 Massachusetts Institute of Technology, All rights reserved.

import Foundation

extension NSDictionary {
  @objc func toString() -> String {
    return (try? getJsonRepresentation(self)) ?? "{unserializable dictionary}"
  }
}
