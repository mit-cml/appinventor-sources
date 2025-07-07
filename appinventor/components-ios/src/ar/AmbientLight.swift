// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit

@available(iOS 14.0, *)
open class AmbientLight: ARLightBase, ARAmbientLight {
  
  @objc init(_ container: ARLightContainer) {
    super.init(container, type: .ambient)
    setupAmbientLight()
  }
  
  private func setupAmbientLight() {
    // RealityKit doesn't have a specific ambient light component
    // Instead, we can use ImageBasedLightComponent or modify the scene's lighting
    // For now, we'll simulate ambient light behavior
    
    // Create a very dim point light that simulates ambient lighting
    var lightComponent = PointLightComponent()
    lightComponent.color = .white
    lightComponent.intensity = 100 // Lower intensity for ambient effect
    lightComponent.attenuationRadius = 1000 // Very large radius for ambient effect
    
    _modelEntity.components.set(lightComponent)
  }
  
  override open var Intensity: Float {
    get {
      return _modelEntity.components[PointLightComponent.self]?.intensity ?? 100
    }
    set(intensity) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      lightComponent.intensity = intensity
      _modelEntity.components.set(lightComponent)
    }
  }
  
  override open var Color: Int32 {
    get {
      let color = _modelEntity.components[PointLightComponent.self]?.color ?? .white
      return Int32(color.hashValue)
    }
    set(color) {
      guard var lightComponent = _modelEntity.components[PointLightComponent.self] else { return }
      //lightComponent.color = UIColor(argbToColor(color)).cgColor
      _modelEntity.components.set(lightComponent)
    }
  }
}
