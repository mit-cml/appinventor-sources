// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class BoxNode: ARNodeBase, ARBox {
  private var _width: Float = 0.05 // stored in meters
  private var _height: Float = 0.05 // stored in meters
  private var _length: Float = 0.05 // stored in meters
  private var _cornerRadius: Float = 0.0 // stored in meters
  

  private var _lastFingerPosition: SIMD3<Float>? = nil
  
  
  @objc init(_ container: ARNodeContainer) {
    // Create initial box mesh
    let mesh = MeshResource.generateBox(width: 0.05, height: 0.05, depth: 0.05)
    super.init(container: container, mesh: mesh)
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }


  private func updateBoxMesh() {
    // Generate new box mesh with current dimensions
    let mesh = MeshResource.generateBox(
      width: _width,
      height: _height,
      depth: _length,
      cornerRadius: _cornerRadius
    )
    
    // Preserve existing materials when updating mesh
    let existingMaterials = _modelEntity.model?.materials ?? []
    _modelEntity.model = ModelComponent(
      mesh: mesh,
      materials: existingMaterials.isEmpty ? [SimpleMaterial()] : existingMaterials
    )
  }

  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_width)
    }
    set(width) {
      _width = UnitHelper.centimetersToMeters(abs(width))
      updateBoxMesh()
    }
  }

  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_height)
    }
    set(height) {
      _height = UnitHelper.centimetersToMeters(abs(height))
      updateBoxMesh()
    }
  }

  @objc open var LengthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_length)
    }
    set(length) {
      _length = UnitHelper.centimetersToMeters(abs(length))
      updateBoxMesh()
    }
  }
  

  @objc open var CornerRadius: Float {
    get {
      return UnitHelper.metersToCentimeters(_cornerRadius)
    }
    set(radius) {
      _cornerRadius = UnitHelper.centimetersToMeters(max(0, radius))
      updateBoxMesh()
    }
  }

  override open func startDrag() {
    let originalPosition = _modelEntity.transform.translation

    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.mode = .kinematic
      _modelEntity.physicsBody = physicsBody
    }
    print("🎯 box Drag started at position: \(originalPosition)")
  }
  
  override open func endDrag(releaseVelocity: CGPoint, camera3DProjection: Any) {
    guard _isBeingDragged else { return }
    
    // Switch back to dynamic mode
    if var physicsBody = _modelEntity.physicsBody {
      physicsBody.mode = .dynamic
      _modelEntity.physicsBody = physicsBody
    }
    
    _isBeingDragged = false
    // Let base class handle surface placement
    super.endDrag(releaseVelocity: releaseVelocity, camera3DProjection: camera3DProjection)
  }
}
