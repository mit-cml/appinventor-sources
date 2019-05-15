// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit
import ARKit

@available(iOS 11.0, *)
public extension ARSCNView {
  func nodeAt(_ point: CGPoint, boundingBoxOnly: Bool) -> SCNNode? {
    let hitTestOptions: [SCNHitTestOption: Any] = [.boundingBoxOnly: boundingBoxOnly]
    let hitTestResults = hitTest(point, options: hitTestOptions)
    
    return hitTestResults.first?.node
  }
  
  func onPlaneAt(_ point: CGPoint) -> simd_float4? {
    let hitTestResults = hitTest(point, types: .existingPlaneUsingExtent)
    return hitTestResults.first?.worldTransform.columns.3
  }
  
  func pointAt(_ point: CGPoint) -> simd_float4? {
    let hitTestResults = hitTest(point, types: .featurePoint)
    return hitTestResults.first?.worldTransform.columns.3
  }
}
