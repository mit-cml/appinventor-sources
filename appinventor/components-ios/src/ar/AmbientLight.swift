// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@available(iOS 10.0, *)
open class AmbientLight: ARLightBase, ARAmbientLight {
  
  @objc init(_ container: ARLightContainer) {
    super.init(container, type: .ambient)
  }
}
