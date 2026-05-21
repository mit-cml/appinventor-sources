// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation

@objc open class ComponentUtil: NSObject {
  @objc static func filterComponentsOfType(_ env: YailDictionary, _ typename: String) -> [Component] {
    var results: [Component] = []
    guard let simpleName = typename.split("\\.").last else {
      return results
    }
    for component in env.allValues {
      guard let component = component as? Component else {
        continue
      }
      if String(describing: type(of: component)).hasSuffix(simpleName) {
        results.append(component)
      }
    }
    return results
  }
}
