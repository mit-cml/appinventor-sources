// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import ARKit
import GLKit

@available(iOS 14.0, *)
open class ImageMarker: NSObject, ARImageMarker {

  weak var _container: ARImageMarkerContainer?
  open var _referenceImage: ARReferenceImage? = nil
  public var _attachedNodes: [ARNodeBase] = []
  var _imagePath: String = ""
  var _anchorEntity: AnchorEntity? = nil
  var _physicalWidth: Float = 0
  var _name: String = "<unknown>"
  var _lastPushTime = Date()
  open var _isTracking = false
  
  var _initialized: Bool = false
  var _detecting: Bool = false
  var _widthSet: Bool = false
  var _imageSet: Bool = false
  
  // Override the protocol extension to provide actual storage
  open var Anchor: AnchorEntity? {
    get {
      if let anchor = _anchorEntity {
        return anchor
      }
      
      //_anchorEntity = createAnchor()
      return _anchorEntity
    }
    set(a) {
      _anchorEntity = a
      
    }
  }
  
  
  @objc init(_ container: ARImageMarkerContainer) {
    _container = container
    super.init()
    setupReferenceImage()
  }
  
  @objc open var Name: String {
    get {
      return _name
    }
    set(name) {
      if !_initialized  {
        guard _container?.markerNameIsAvailable(name) ?? true else {
          return
        }
        _referenceImage?.name = name
        _name = name
        
      } else if _referenceImage?.name != name, let oldName = _referenceImage?.name {
        guard _container?.updateMarker(self, for: oldName, with: name) ?? false else {
          return
        }
        _referenceImage?.name = name
        _name = name
      }
    }
  }
  
  @objc open var Image: String {
    get {
      return _imagePath
    }
    set(path) {
      _imagePath = path
      
      guard let _ = AssetManager.shared.imageFromPath(path: _imagePath)?.cgImage else {
        if !path.isEmpty {
          _container?.form?.dispatchErrorOccurredEvent(self, "Image", ErrorMessage.ERROR_MEDIA_IMAGE_FILE_FORMAT.code)
        }
        return
      }
      
      if !_initialized  {
        _imageSet = !_imagePath.isEmpty
      }
      
      setupReferenceImage()
    }
  }
  
  @objc open var PhysicalWidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_physicalWidth)
    }
    set(width) {
      _physicalWidth = UnitHelper.centimetersToMeters(abs(width))
      
      if !_initialized {
        _widthSet = _physicalWidth > 0
      }
      
      setupReferenceImage()
    }
  }
  
  @objc open var PhysicalHeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_referenceImage?.physicalSize.height ?? 0)
    }
  }
  
  @objc open var AttachedNodes: [ARNodeBase] {
    get {
      return _attachedNodes
    }
  }
  
  private func setupReferenceImage() {
    _initialized = _widthSet && _imageSet
    
    guard _initialized else { return }
    
    guard let image = AssetManager.shared.imageFromPath(path: _imagePath)?.cgImage else {
      return
    }
    
    _referenceImage = ARReferenceImage(image, orientation: .up, physicalWidth: CGFloat(_physicalWidth))
    _referenceImage?.name = _name
    
    if _detecting {
      _container?.updateMarker(self, for: _name, with: _name)
    } else {
      _container?.addMarker(self)
      _detecting = true
    }
  }
  
  open func attach(_ node: ARNodeBase) {
    // Remove from previous parent
    node._modelEntity.removeFromParent()
    _attachedNodes.append(node)
    
    // Attach to our anchor entity if it exists
    if let anchorEntity = _anchorEntity {
      anchorEntity.addChild(node._modelEntity)
    }
  }
  
  open func removeNode(_ node: ARNodeBase) {
    if let index = _attachedNodes.firstIndex(of: node) {
      _attachedNodes.remove(at: index)
      node._modelEntity.removeFromParent()
    }
  }
  
  open func pushUpdate(_ position: SIMD3<Float>, _ angles: SIMD3<Float>) {
    let elapsed = Date().timeIntervalSince(_lastPushTime)
    /**
     * As the worldmap gets updated, if the image is constantly in the frame, the position is constantly
     * going through small micro changes.  Therefore, we only push a position changed at most every
     * 50 milliseconds.
     */
    if elapsed > 0.050 {
      _lastPushTime = Date()
      PositionChanged(
        UnitHelper.metersToCentimeters(position.x),
        UnitHelper.metersToCentimeters(position.y),
        UnitHelper.metersToCentimeters(position.z)
      )
      RotationChanged(
        GLKMathRadiansToDegrees(angles.x),
        GLKMathRadiansToDegrees(angles.y),
        GLKMathRadiansToDegrees(angles.z)
      )
    }
  }
  
  // MARK: Events
  @objc open func FirstDetected(_ anchor: AnyObject) {
    // Create anchor entity from ARKit anchor
    _anchorEntity = anchor as! AnchorEntity
    
    _isTracking = true
    _lastPushTime = Date()
    AppearedInView()
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FirstDetected")
    }
  }
  
  @objc open func PositionChanged(_ x: Float, _ y: Float, _ z: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "PositionChanged", arguments: x as NSNumber, y as NSNumber, z as NSNumber)
    }
  }
  
  @objc open func RotationChanged(_ x: Float, _ y: Float, _ z: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "RotationChanged", arguments: x as NSNumber, y as NSNumber, z as NSNumber)
    }
  }
  
  @objc public func NoLongerInView() {
    _isTracking = false
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "NoLongerInView")
    }
  }
  
  @objc public func AppearedInView() {
    _isTracking = true
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "AppearedInView")
    }
  }
  
  @objc public func Reset() {
    _isTracking = false
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Reset")
    }
  }
  
  public func removeAllNodes() {
    for node in _attachedNodes {
      node.stopFollowing()
    }
    
    _attachedNodes = []
  }
  
  @objc open var Visible: Bool {
    get { return _anchorEntity!.isEnabled }
    set(visible) { _anchorEntity!.isEnabled = visible }
  }
}

@available(iOS 14.0, *)
extension ImageMarker: LifecycleDelegate {
  @objc public func onResume() {}
  
  @objc public func onPause() {}
  
  @objc public func onDelete() {
    removeAllNodes()
    _container?.removeMarker(self)
    _container = nil
  }
  
  @objc public func onDestroy() {
    removeAllNodes()
    _container?.removeMarker(self)
    _container = nil
  }
}

@available(iOS 14.0, *)
extension ImageMarker: VisibleComponent {
  public var Width: Int32 {
    get {
      return 0
    }
    set {}
  }
  
  public var Height: Int32 {
    get {
      return 0
    }
    set {}
  }
  
  public var dispatchDelegate: HandlesEventDispatching? {
    get {
      return _container!.form?.dispatchDelegate
    }
  }
  
  public func copy(with zone: NSZone? = nil) -> Any { return (Any).self }
  public func setWidthPercent(_ toPercent: Int32) {}
  public func setHeightPercent(_ toPercent: Int32) {}
}
