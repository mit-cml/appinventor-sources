// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit

@available(iOS 10.0, *)
open class ARLightBase: NSObject, ARLight {
  weak var _container: ARLightContainer?
  private var _node: SCNNode = SCNNode()
  public var _light: SCNLight

  @objc public init(_ container: ARLightContainer, type: SCNLight.LightType) {
    _container = container
    _light = SCNLight()
    _light.type = type
    _light.color = UIColor.white
    _light.shadowMode = .deferred
    _node.light = _light
    super.init()
    DispatchQueue.main.async {
      self._container?.addLight(self)
    }
  }

  @objc open var Name: String {
    get {
      return _light.name ?? ""
    }
    set(name) {
      _light.name = name
      _node.name = name
    }
  }
  
  @objc open var `Type`: String {
    get {
      return String(describing: type(of: self))
    }
  }

  @objc open var Color: Int32 {
    get {
      if let color = _light.color as? UIColor {
        return colorToArgb(color)
      }
      return Int32(bitPattern: AIComponentKit.Color.none.rawValue)
    }
    set(color) {
      _light.color = argbToColor(color)
    }

  }

  @objc open var Temperature: Float {
    get {
      return Float(_light.temperature)
    }
    set(temp) {
      let validTemp = min(max(0, temp), 40000)
      _light.temperature = CGFloat(validTemp)
    }
  }

  @objc open var Intensity: Float {
    get {
      return Float(_light.intensity)
    }
    set(intensity) {
      _light.intensity = CGFloat(intensity)
    }
  }

  @objc open var Visible: Bool {
    get {
      return !_node.isHidden
    }
    set(visible) {
      _node.isHidden = !visible
    }
  }

  public func getNode() -> SCNNode {
    return _node
  }
}


@available(iOS 10.0, *)
extension ARLightBase: VisibleComponent {
  @objc open var Width: Int32 {
    get {
      return 0
    }
    set {}
  }

  @objc open var Height: Int32 {
    get {
      return 0
    }
    set {}
  }

  @objc open var dispatchDelegate: HandlesEventDispatching? {
    get {
      return _container!.form!.dispatchDelegate
    }
  }

  public func copy(with zone: NSZone? = nil) -> Any { return (Any).self }
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
}

@available(iOS 10.0, *)
extension ARLightBase: LifecycleDelegate {
  @objc public func onResume() {}

  @objc public func onPause() {}

  @objc public func onDelete() {
    _container?.removeLight(self)
    _container = nil
  }

  @objc public func onDestroy() {
    _container?.removeLight(self)
    _container = nil
  }
}
