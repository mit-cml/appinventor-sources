// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SceneKit
import ARKit

@available(iOS 11.3, *)
open class ARView3D: ViewComponent, ARSCNViewDelegate, ARNodeContainer, AbstractMethodsForViewComponent {
  public var container: ComponentContainer? {
    return _container
  }

  public func isVisible(component: ViewComponent) -> Bool {
    true
  }
  
  public func setVisible(component: ViewComponent, to visibility: Bool) {
    // TODO: Fix this
  }
  
  public func getChildren() -> [any Component] {
    return []
  }
  
  fileprivate var _nodesDict: [SCNNode : ARNodeBase] = [:]
  fileprivate var _detectedPlanesDict: [SCNNode: DetectedPlane] = [:]
  fileprivate var _imageMarkers: [String : ImageMarker] = [:]
  fileprivate var _lights: [SCNNode: ARLightBase] = [:]

  final var _sceneView: ARSCNView
  private var _trackingType: ARTrackingType = .worldTracking
  private var _configuration: ARConfiguration = ARWorldTrackingConfiguration()
  private var _planeDetection: ARPlaneDetectionType = .none
  private var _showWorldOrigin: Bool = false
  private var _showFeaturePoints: Bool = false
  private var _showWireframes: Bool = false
  private var _showBoundingBoxes: Bool = false
  private var _showLightLocations: Bool = false
  private var _showLightAreas: Bool = false
  fileprivate var trackingNode: ARNodeBase? = nil
  fileprivate var _sessionRunning: Bool = false
  fileprivate var _rotation: Float = 0.0
  fileprivate var _containsModelNodes: Bool = false
  fileprivate var _lightingEstimationEnabled: Bool = false

  /// Needed for configuration setting and ensuring intialization
  fileprivate var _trackingSet: Bool = false
  private var _planeDetectionSet: Bool = false
  private var _lightingEstimationSet: Bool = false
  
  /// Needed that way nodes are only added when the session is running and nodes are disabled when session stopped
  private var _requiresAddNodes = false
  private var startOptions: ARSession.RunOptions = []
  private var _reenableWebViewNodes: Bool = false

  public override init(_ parent: ComponentContainer) {
    _sceneView = ARSCNView()
    super.init(parent)
    setDelegate(self)
    _sceneView.delegate = self
    _sceneView.automaticallyUpdatesLighting = false
    initializeGestureRecognizers()
    TrackingType = 1
    PlaneDetectionType = 1
    LightingEstimation = false
    parent.add(self)
    Height = kARViewPreferredHeight
    Width = kARViewPreferredWidth
  }

  private func initializeGestureRecognizers() {
    let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap))
    _sceneView.addGestureRecognizer(tapGesture)

    let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePan))
    panGesture.maximumNumberOfTouches = 1
    _sceneView.addGestureRecognizer(panGesture)

    let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress))
    longPressGesture.minimumPressDuration = 0.75
    _sceneView.addGestureRecognizer(longPressGesture)

    tapGesture.require(toFail: longPressGesture)

    let pinchGesture = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch))
    _sceneView.addGestureRecognizer(pinchGesture)
    pinchGesture.delegate = self

    let rotationGesture = UIRotationGestureRecognizer(target: self, action: #selector(handleRotation))
    _sceneView.addGestureRecognizer(rotationGesture)
    rotationGesture.delegate = self

  }

  // MARK: Properties

  @objc open override var view: UIView {
    get {
      return _sceneView
    }
  }

  @objc open var Nodes: [ARNode] {
    get {
      return _nodesDict.values.map { $0 }
    }
  }

  @objc open var TrackingType: Int32 {
    get {
      return _trackingType.rawValue
    }
    set(type) {

      _trackingSet = true

      guard 1...3 ~= type else {
        _container?.form?.dispatchErrorOccurredEvent(self, "TrackingType", ErrorMessage.ERROR_INVALID_TRACKING_TYPE.code, type)
        return
      }
      _trackingType = ARTrackingType(rawValue: type)!

      setupConfiguration()
    }
  }

  @objc open var PlaneDetectionType: Int32 {
    get {
      return _planeDetection.rawValue
    }
    set(type) {

      _planeDetectionSet = true

      guard 1...4 ~= type else {
        _container?.form?.dispatchErrorOccurredEvent(self, "PlaneDetectionType", ErrorMessage.ERROR_INVALID_PLANE_DETECTION_TYPE.code, type)
        return
      }

      _planeDetection = ARPlaneDetectionType(rawValue: type)!
      setupConfiguration()
    }
  }

  @objc open var ShowStatistics: Bool {
    get {
      return _sceneView.showsStatistics
    }
    set(showStatistics) {
      _sceneView.showsStatistics = showStatistics
    }
  }

  @objc open var ShowWorldOrigin: Bool {
    get {
      return _showWorldOrigin
    }
    set(showOrigin) {
      if showOrigin {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showWorldOrigin)
      } else {
        _ = _sceneView.debugOptions.remove(ARSCNDebugOptions.showWorldOrigin)
      }
      _showWorldOrigin = showOrigin
    }
  }
  
  @objc open var ShowWireframes: Bool {
    get {
      return _showWireframes
    }
    set(showWireframes) {
      if showWireframes {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showWireframe)
      } else {
        _ = _sceneView.debugOptions.remove(ARSCNDebugOptions.showWireframe)
      }
      _showWireframes = showWireframes
    }
  }
  
  @objc open var ShowBoundingBoxes: Bool {
    get {
      return _showBoundingBoxes
    }
    set(showBoundingBoxes) {
      if showBoundingBoxes {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showBoundingBoxes)
      } else {
        _ = _sceneView.debugOptions.remove(ARSCNDebugOptions.showBoundingBoxes)
      }
      _showBoundingBoxes = showBoundingBoxes
    }
  }

  @objc open var ShowFeaturePoints: Bool {
    get {
      return _showFeaturePoints
    }
    set(showFeaturePoints) {
      if showFeaturePoints {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showFeaturePoints)
      } else {
        _sceneView.debugOptions.remove(ARSCNDebugOptions.showFeaturePoints)
      }
      _showFeaturePoints = showFeaturePoints
    }
  }

  private func setupConfiguration() {
    guard _trackingSet && _planeDetectionSet && _lightingEstimationSet else { return }

    switch _trackingType {
    /**
     * World tracking configuration
     * Requires setting the following
     * - Plane Detection
     * - Detection Images
     */
    case .worldTracking:
      let worldTrackingConfiguration = ARWorldTrackingConfiguration()
      if #available(iOS 12.0, *) { worldTrackingConfiguration.maximumNumberOfTrackedImages = 4 }
      worldTrackingConfiguration.detectionImages = getReferenceImages()

      switch _planeDetection {
      case .horizontal:
        worldTrackingConfiguration.planeDetection = .horizontal
      case .vertical:
        worldTrackingConfiguration.planeDetection = .vertical
      case .both:
        worldTrackingConfiguration.planeDetection = [.horizontal, .vertical]
      case .none:
        break
      }
      
      _configuration = worldTrackingConfiguration

    case .orientationTracking:
      _configuration = AROrientationTrackingConfiguration()

    /**
    * Image tracking configuration
    * Requires setting the following
    * - Detection Images / Tracking Images
    */
    case .imageTracking:
      if #available(iOS 12.0, *) {
        let imageTrackingConfiguration = ARImageTrackingConfiguration()
        imageTrackingConfiguration.maximumNumberOfTrackedImages = 4
        imageTrackingConfiguration.trackingImages = getReferenceImages()
        _configuration = imageTrackingConfiguration
      } else {
        /// Fallback on earlier versions
        let worldTrackingConfiguration = ARWorldTrackingConfiguration()
        worldTrackingConfiguration.detectionImages = getReferenceImages()
        _configuration = worldTrackingConfiguration
      }
    }
    
    _configuration.isLightEstimationEnabled = _lightingEstimationEnabled

    if _sessionRunning {
      ResetTracking()
    }
  }
  
  private func getReferenceImages() -> Set<ARReferenceImage> {
    return Set(_imageMarkers.values.compactMap{ $0._referenceImage })
  }

  // MARK: Functions

  @objc open func StartTracking() {
    startTracking(startOptions)
  }

  @objc open func startTracking(_ options: ARSession.RunOptions = []) {
    _sceneView.session.run(_configuration, options: options)
    _sessionRunning = true
    startOptions = []
    
    if _requiresAddNodes {
      for node in _nodesDict.values {
        if _reenableWebViewNodes, let webViewNode = node as? ARWebView {
          webViewNode.isUserInteractionEnabled = true
        }
        if node._node.parent != _sceneView.scene.rootNode && !node.IsFollowingImageMarker {
          _sceneView.scene.rootNode.addChildNode(node._node)
        }
      }
      
      for light in _lights.values {
        let lightNode = light.getNode()
        if lightNode.parent != _sceneView.scene.rootNode {
          _sceneView.scene.rootNode.addChildNode(lightNode)
        }
      }
      _requiresAddNodes = false
      _reenableWebViewNodes = false
    } else if _reenableWebViewNodes {
      for node in _nodesDict.values {
        if let webViewNode = node as? ARWebView {
          webViewNode.isUserInteractionEnabled = true
        }
      }
      _reenableWebViewNodes = false
    }
  }

  @objc open func PauseTracking() {
    pauseTracking(true)
  }
  
  private func pauseTracking(_ disableWebViewInteraction: Bool = false ) {
    _sessionRunning = false
    _sceneView.session.pause()
    trackingNode = nil
    
    if disableWebViewInteraction {
      for node in _nodesDict.values {
        if let webViewNode = node as? ARWebView {
          webViewNode.isUserInteractionEnabled = false
          _reenableWebViewNodes = true
        }
      }
    }
  }

  @objc open func ResetTracking() {
    let _shouldRestartSession = _sessionRunning
    pauseTracking(!_shouldRestartSession)
    startOptions = [.resetTracking, .removeExistingAnchors]
    if _shouldRestartSession {
      startTracking(startOptions)
    }
  }

  @objc open func ResetDetectedItems() {
    let _shouldRestartSession = _sessionRunning
    pauseTracking(!_shouldRestartSession)
    if _shouldRestartSession {
      startTracking([.removeExistingAnchors])
    } else if startOptions.isEmpty {
      startOptions = [.removeExistingAnchors]
    }
  }

  @objc open func addNode(_ node: ARNodeBase) {
    _nodesDict[node._node] = node
    _containsModelNodes = node is ModelNode ? true : _containsModelNodes
    
    if _sessionRunning {
      _sceneView.scene.rootNode.addChildNode(node._node)
    } else {
      _requiresAddNodes = true
    }
  }

  @objc open func removeNode(_ node: ARNodeBase) {
    _nodesDict.removeValue(forKey: node._node)
    node._node.removeFromParentNode()
  }

  @objc open func getARView() -> ARView3D {
    return self
  }

  @objc open func HideAllNodes() {
    for node in _nodesDict.values {
      node._node.isHidden = true
    }
  }
  
  open func sessionShouldAttemptRelocalization(_ session: ARSession) -> Bool {
    return true
  }

  @objc open func renderer(_ renderer: SCNSceneRenderer, didAdd node: SCNNode, for anchor: ARAnchor) {
    /// Plane Detected
    if let planeAnchor = anchor as? ARPlaneAnchor {
      let detectedPlane = DetectedPlane(anchor: planeAnchor, node: node, container: self)
      _detectedPlanesDict[node] = detectedPlane
      PlaneDetected(detectedPlane)
    }
    /// Image Recognized
    else if let imageAnchor = anchor as? ARImageAnchor {
      guard let name = imageAnchor.referenceImage.name else { return }
      let imageMarker = _imageMarkers[name]
      imageMarker?.FirstDetected(node)
    }
  }

  @objc open func renderer(_ renderer: SCNSceneRenderer, didUpdate node: SCNNode, for anchor: ARAnchor) {
    /// DetectedPlane Updated
    if let planeAnchor = anchor as? ARPlaneAnchor, let updatedPlane = _detectedPlanesDict[node] {
      updatedPlane.updateFor(anchor: planeAnchor)
      DetectedPlaneUpdated(updatedPlane)
    }
    /// Detected ImageMarker Updated
    else if let imageAnchor = anchor as? ARImageAnchor {
      guard let name = imageAnchor.referenceImage.name, let imageMarker = _imageMarkers[name] else { return }
      if !imageAnchor.isTracked {
        imageMarker.NoLongerInView()
      } else if !imageMarker._isTracking {
        imageMarker.AppearedInView()
      }
      imageMarker.pushUpdate(node.position, node.eulerAngles)
    }
  }
  
  open func renderer(_ renderer: SCNSceneRenderer, didRemove node: SCNNode, for anchor: ARAnchor) {
    /**
     * This occurs when planes are expanded and some are removed.
     *
     * Essentially, ARKit might notice that 2+ added planes are colinear and should actually just be one plane.
     * It then removes one of the planes and expands the other.
     *
     */
    if let _ = anchor as? ARPlaneAnchor, let removedPlane = _detectedPlanesDict[node] {
      _detectedPlanesDict.removeValue(forKey: node)
      removedPlane.removed()
      DetectedPlaneRemoved(removedPlane)
    } else if let imageAnchor = anchor as? ARImageAnchor {
      guard let name = imageAnchor.referenceImage.name, let imageMarker = _imageMarkers[name] else { return }
      imageMarker.Reset()
    }
  }
  
  open func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
    guard _lightingEstimationEnabled, let lightingEstimate = _sceneView.session.currentFrame?.lightEstimate else { return }
    
    LightingEstimateUpdated(Float(lightingEstimate.ambientIntensity), Float(lightingEstimate.ambientColorTemperature))
  }

  // MARK: Events
  @objc open func NodeClick(_ node: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "NodeClick", arguments: node as AnyObject)
  }

  @objc open func TapAtPoint(_ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool) {
    let xCm: Float = UnitHelper.metersToCentimeters(x)
    let yCm: Float = UnitHelper.metersToCentimeters(y)
    let zCm: Float = UnitHelper.metersToCentimeters(z)

    EventDispatcher.dispatchEvent(of: self, called: "TapAtPoint", arguments: xCm as NSNumber, yCm as NSNumber, zCm as NSNumber, isANodeAtPoint as NSNumber)
  }

  @objc open func NodeLongClick(_ node: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "NodeLongClick", arguments: node as AnyObject)
  }

  @objc open func LongPressAtPoint(_ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool) {
    let xCm: Float = UnitHelper.metersToCentimeters(x)
    let yCm: Float = UnitHelper.metersToCentimeters(y)
    let zCm: Float = UnitHelper.metersToCentimeters(z)
    
    EventDispatcher.dispatchEvent(of: self, called: "LongPressAtPoint", arguments: xCm as NSNumber, yCm as NSNumber, zCm as NSNumber, isANodeAtPoint as NSNumber)
  }

  open func add(_ component: ViewComponent) {}

  open func setChildWidth(of component: ViewComponent, to width: Int32) {}

  open func setChildHeight(of component: ViewComponent, to height: Int32) {}
}


// MARK: Functions for Node Creation
@available(iOS 11.3, *)
extension ARView3D {
  @objc open func CreateBoxNode(_ x: Float, _ y: Float, _ z: Float) -> BoxNode {
    let node = BoxNode(self)
    node.Name = "CreatedBoxNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateSphereNode(_ x: Float, _ y: Float, _ z: Float) -> SphereNode {
    let node = SphereNode(self)
    node.Name = "CreatedSphereNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreatePlaneNode(_ x: Float, _ y: Float, _ z: Float) -> PlaneNode {
    let node = PlaneNode(self)
    node.Name = "CreatedPlaneNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateCylinderNode(_ x: Float, _ y: Float, _ z: Float) -> CylinderNode {
    let node = CylinderNode(self)
    node.Name = "CreatedCylinderNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateConeNode(_ x: Float, _ y: Float, _ z: Float) -> ConeNode {
    let node = ConeNode(self)
    node.Name = "CreatedConeNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateCapsuleNode(_ x: Float, _ y: Float, _ z: Float) -> CapsuleNode {
    let node = CapsuleNode(self)
    node.Name = "CreatedCapsuleNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateTubeNode(_ x: Float, _ y: Float, _ z: Float) -> TubeNode {
    let node = TubeNode(self)
    node.Name = "CreatedTubeNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateTorusNode(_ x: Float, _ y: Float, _ z: Float) -> TorusNode {
    let node = TorusNode(self)
    node.Name = "CreatedTorusNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreatePyramidNode(_ x: Float, _ y: Float, _ z: Float) -> PyramidNode {
    let node = PyramidNode(self)
    node.Name = "CreatedPyramidNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateTextNode(_ x: Float, _ y: Float, _ z: Float) -> TextNode {
    let node = TextNode(self)
    node.Name = "CreatedTextNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateVideoNode(_ x: Float, _ y: Float, _ z: Float) -> VideoNode {
    let node = VideoNode(self)
    node.Name = "CreatedVideoNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
  
  @objc open func CreateWebViewNode(_ x: Float, _ y: Float, _ z: Float) -> WebViewNode {
    let node = WebViewNode(self)
    node.Name = "CreatedWebViewNode"
    node.syncInitialize()
    
    let xMeters: Float = UnitHelper.centimetersToMeters(x)
    let yMeters: Float = UnitHelper.centimetersToMeters(y)
    let zMeters: Float = UnitHelper.centimetersToMeters(z)
    node.setPosition(x: xMeters, y: yMeters, z: zMeters)
    return node
  }
}


// MARK: Functions Handling Gestures
@available(iOS 11.3, *)
extension ARView3D: UIGestureRecognizerDelegate {
  @objc fileprivate func handleTap(sender: UITapGestureRecognizer) {
    guard _sessionRunning, let areaTapped = sender.view as? SCNView else { return }
    let tappedCoordinates = sender.location(in: areaTapped)
    var isNodeAtPoint = false

    if let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
      if let ARNode = _nodesDict[tappedNode] {
        NodeClick(ARNode)
        ARNode.Click()
        isNodeAtPoint = true
      } else if let parentNode = tappedNode.parent, parentNode != _sceneView.scene.rootNode {
        let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
        
        if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
          NodeClick(ARNode)
          ARNode.Click()
          isNodeAtPoint = true
        }
      }
    }

    if let tappedPlaneAt = _sceneView.onPlaneAt(tappedCoordinates), let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
      if let detectedPlane = _detectedPlanesDict[tappedNode] {
        /// NOTE: this case is essentially never hit
        ClickOnDetectedPlaneAt(detectedPlane, tappedPlaneAt.x, tappedPlaneAt.y, tappedPlaneAt.z, isNodeAtPoint)
      } else if let parentNode = tappedNode.parent, let detectedPlane = _detectedPlanesDict[parentNode] {
        ClickOnDetectedPlaneAt(detectedPlane, tappedPlaneAt.x, tappedPlaneAt.y, tappedPlaneAt.z, isNodeAtPoint)
      }
    }

    if let tappedPoint = _sceneView.pointAt(tappedCoordinates) {
      TapAtPoint(Float(tappedPoint.x), Float(tappedPoint.y), Float(tappedPoint.z), isNodeAtPoint)
    } else {
      /// Enable returning the 2D coordinate in screenspace if ever useable
      /// ie. if overlays are ever allowed in App Inventor
    }
  }

  /**
   * Moves a node by the pan gesture
   */
  @objc fileprivate func handlePan(sender: UIPanGestureRecognizer) {
    guard _sessionRunning, let areaPanned = sender.view as? SCNView else { return }
    let tappedCoordinates = sender.location(in: areaPanned)

    switch sender.state {
    case .began:
      if let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[tappedNode] {
          guard trackingNode == nil else {
            trackingNode = nil
            return
          }
          trackingNode = ARNode
        } else if let parentNode = tappedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            guard trackingNode == nil else {
              trackingNode = nil
              return
            }
            trackingNode = ARNode
          }
        }
      }
    case .changed:
      if let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[tappedNode] {
          guard trackingNode == ARNode else {
            trackingNode = nil
            return
          }
          let translation = sender.translation(in: areaPanned)
          ARNode.moveByPan(x: Float(translation.x / 10000.0), y: Float(-translation.y / 10000.0))
        } else if let parentNode = tappedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            guard trackingNode == ARNode else {
              trackingNode = nil
              return
            }
            let translation = sender.translation(in: areaPanned)
            ARNode.moveByPan(x: Float(translation.x / 10000.0), y: Float(-translation.y / 10000.0))
          }
        }
      }
    case .ended:
      trackingNode = nil
    default:
      trackingNode = nil
    }
  }

  /**
   * Handles long pressing on the view
   * - subsequently handles long pressing on a node, detected plane, and the view iteself (a feature point)
   */
  @objc fileprivate func handleLongPress(sender: UILongPressGestureRecognizer) {
    guard _sessionRunning else { return }
    
    if sender.state == .ended {
      guard let areaPressed = sender.view as? SCNView else { return }
      let tappedCoordinates = sender.location(in: areaPressed)
      var isNodeAtPoint = false

      if let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[tappedNode] {
          NodeLongClick(ARNode)
          ARNode.LongClick()
          isNodeAtPoint = true
        } else if let parentNode = tappedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            NodeLongClick(ARNode)
            ARNode.LongClick()
            isNodeAtPoint = true
          }
        }

      }

      if let tappedPlaneAt = _sceneView.onPlaneAt(tappedCoordinates), let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let detectedPlane = _detectedPlanesDict[tappedNode] {
          /// NOTE: this case is essentially never hit
          LongClickOnDetectedPlaneAt(detectedPlane, tappedPlaneAt.x, tappedPlaneAt.y, tappedPlaneAt.z, isNodeAtPoint)
        } else if let parentNode = tappedNode.parent, let detectedPlane = _detectedPlanesDict[parentNode] {
          LongClickOnDetectedPlaneAt(detectedPlane, tappedPlaneAt.x, tappedPlaneAt.y, tappedPlaneAt.z, isNodeAtPoint)
        }
      }

      if let tappedPoint = _sceneView.pointAt(tappedCoordinates) {
        LongPressAtPoint(Float(tappedPoint.x), Float(tappedPoint.y), Float(tappedPoint.z), isNodeAtPoint)
      } else {
        /// Enable returning the 2D coordinate in screenspace if ever useable
        /// ie. if overlays are ever allowed in App Inventor
      }
    }
  }

  /**
   * Scales a node based on pinching
   */
  @objc fileprivate func handlePinch(sender: UIPinchGestureRecognizer) {
    guard _sessionRunning else { return }
    
    switch sender.state {
    case .changed:
      guard let areaPinched = sender.view as? SCNView else { return }
      let tappedCoordinates = sender.location(in: areaPinched)

      if let tappedNode = _sceneView.nodeAt(tappedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[tappedNode] {
          ARNode.scaleByPinch(scalar: Float(sender.scale))
        } else if let parentNode = tappedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            ARNode.scaleByPinch(scalar: Float(sender.scale))
          }
        }
      }
      sender.scale = 1
    default:
      break
    }
  }

  /**
   * Rotates a node via the Y axis
   */
  @objc fileprivate func handleRotation(sender: UIRotationGestureRecognizer) {
    guard _sessionRunning else { return }
    
    switch sender.state {
    case .began:
      guard let areaTouched = sender.view as? SCNView else { return }
      let touchedCoordinates = sender.location(in: areaTouched)

      if let touchedNode = _sceneView.nodeAt(touchedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[touchedNode] {
          _rotation = ARNode._node.eulerAngles.y
        } else if let parentNode = touchedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            _rotation = ARNode._node.eulerAngles.y
          }
        }
      }
    case .changed:
      guard let areaTouched = sender.view as? SCNView else { return }
      let touchedCoordinates = sender.location(in: areaTouched)

      if let touchedNode = _sceneView.nodeAt(touchedCoordinates, boundingBoxOnly: !_containsModelNodes) {
        if let ARNode = _nodesDict[touchedNode] {
          let rotation = _rotation + Float(sender.rotation)
          ARNode.rotateByGesture(radians: rotation)
        } else if let parentNode = touchedNode.parent, parentNode != _sceneView.scene.rootNode {
          let rootNode = getRootNodeOfTouchedNode(parentNode: parentNode)
          
          if let rootOfTappedNode = rootNode, let ARNode = _nodesDict[rootOfTappedNode] {
            let rotation = _rotation + Float(sender.rotation)
            ARNode.rotateByGesture(radians: rotation)
          }
        }
      }
    case .ended, .failed, .cancelled:
      _rotation = 0.0
    default:
      break
    }
  }
  
  /**
   * When dealing with a reference node (or ModelNode), there may be several nodes in
   * that are child nodes of the reference's root node.  We want to apply any gestures on
   * the root node, that way it affects the child nodes as well.  Therefore, when interacting
   * with one of these nodes, the touch might be registered on one of the child nodes.  We need
   * to go up the node tree in order to get to the root node.
   *
   * The root node will have the scene's root node as its parent.
   */
  private func getRootNodeOfTouchedNode(parentNode: SCNNode?) -> SCNNode? {
    var rootNode: SCNNode? = parentNode
    while let node = rootNode, node.parent != _sceneView.scene.rootNode {
      rootNode = node.parent
    }
    
    return rootNode
  }

  public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer,
                         shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {

      let rotationAndPinch = gestureRecognizer is UIRotationGestureRecognizer &&
        otherGestureRecognizer is UIPinchGestureRecognizer
      let pinchAndRotation = gestureRecognizer is UIPinchGestureRecognizer &&
        otherGestureRecognizer is UIRotationGestureRecognizer

      if rotationAndPinch || pinchAndRotation {
        return true
      }

      return false
  }
}

// MARK: ARImageMarkerContainer
@available(iOS 11.3, *)
extension ARView3D: ARImageMarkerContainer {
  @objc public var ImageMarkers: [ARImageMarker] {
    get {
      return _imageMarkers.values.map { $0 }
    }
  }

  public func addMarker(_ marker: ImageMarker) {
    guard !_imageMarkers.keys.contains(marker.Name) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "addMarker", ErrorMessage.ERROR_IMAGEMARKER_ALREADY_EXISTS_WITH_NAME.code, marker.Name)
      return
    }

    _imageMarkers[marker.Name] = marker
    setupConfiguration()
  }

  public func removeMarker(_ marker: ImageMarker) {
    guard _imageMarkers.keys.contains(marker.Name) else {
      return
    }

    marker.removeAllNodes()
    _imageMarkers.removeValue(forKey: marker.Name)
  }

  public func updateMarker(_ marker: ImageMarker, for oldName: String, with newName: String) -> Bool {
    guard _imageMarkers.keys.contains(oldName) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "updateMarker", ErrorMessage.ERROR_IMAGEMARKER_DOES_NOT_EXIST_WITH_NAME.code, oldName)
      return false
    }

    guard newName != oldName else {
      _imageMarkers[oldName] = marker
      setupConfiguration()
      return true
    }
    
    guard markerNameIsAvailable(newName) else {
      _container?.form?.dispatchErrorOccurredEvent(self, "updateMarker", ErrorMessage.ERROR_IMAGEMARKER_ALREADY_EXISTS_WITH_NAME.code, newName)
      return false
    }

    let marker = _imageMarkers[oldName]
    _imageMarkers.removeValue(forKey: oldName)
    _imageMarkers[newName] = marker
    return true
  }

  public func markerNameIsAvailable(_ name: String) -> Bool {
    return !_imageMarkers.keys.contains(name)
  }
}

@available(iOS 11.3, *)
extension ARView3D: ARDetectedPlaneContainer {
  @objc open var DetectedPlanes: [ARDetectedPlane] {
    get {
      return _detectedPlanesDict.values.map { $0 }
    }
  }
  
  @objc open func ClickOnDetectedPlaneAt(_ detectedPlane: ARDetectedPlane, _ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool) {
    let xCm: Float = UnitHelper.metersToCentimeters(x)
    let yCm: Float = UnitHelper.metersToCentimeters(y)
    let zCm: Float = UnitHelper.metersToCentimeters(z)
    EventDispatcher.dispatchEvent(of: self, called: "ClickOnDetectedPlaneAt", arguments: detectedPlane as AnyObject, xCm as NSNumber, yCm as NSNumber, zCm as NSNumber, isANodeAtPoint as NSNumber)
  }
  
  @objc open func LongClickOnDetectedPlaneAt(_ detectedPlane: ARDetectedPlane, _ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool) {
    let xCm: Float = UnitHelper.metersToCentimeters(x)
    let yCm: Float = UnitHelper.metersToCentimeters(y)
    let zCm: Float = UnitHelper.metersToCentimeters(z)
    EventDispatcher.dispatchEvent(of: self, called: "LongClickOnDetectedPlaneAt", arguments: detectedPlane as AnyObject, xCm as NSNumber, yCm as NSNumber, zCm as NSNumber, isANodeAtPoint as NSNumber)
  }
  
  @objc open func PlaneDetected(_ detectedPlane: ARDetectedPlane) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "PlaneDetected", arguments: detectedPlane as AnyObject)
    }
  }
  
  open func DetectedPlaneUpdated(_ updatedPlane: ARDetectedPlane) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "DetectedPlaneUpdated", arguments: updatedPlane as AnyObject)
    }
  }
  
  open func DetectedPlaneRemoved(_ detectedPlane: ARDetectedPlane) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "DetectedPlaneRemoved", arguments: detectedPlane as AnyObject)
    }
  }
}

// MARK: ARLightContainer
@available(iOS 11.3, *)
extension ARView3D: ARLightContainer {
  @objc open var Lights: [ARLight] {
    get {
      return _lights.values.map { $0 }
    }
  }
  
  @objc open var LightingEstimation: Bool {
    get {
      return _lightingEstimationEnabled
    }
    set(updatesLighting) {
      _lightingEstimationEnabled = updatesLighting
      _lightingEstimationSet = true
      setupConfiguration()
    }
  }
  
  @objc open var ShowLightLocations: Bool {
    get {
      return _showLightLocations
    }
    set(showLightLocations) {
      if showLightLocations {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showLightInfluences)
      } else {
        _sceneView.debugOptions.remove(ARSCNDebugOptions.showLightInfluences)
      }
      _showLightLocations = showLightLocations
    }
  }
  
  @objc open var ShowLightAreas: Bool {
    get {
      return _showLightAreas
    }
    set(showLightAreas) {
      if showLightAreas {
        _sceneView.debugOptions.insert(ARSCNDebugOptions.showLightExtents)
      } else {
        _sceneView.debugOptions.remove(ARSCNDebugOptions.showLightExtents)
      }
      _showLightAreas = showLightAreas
    }
  }
  
  @objc open func HideAllLights() {
    for light in _lights.values {
      light.getNode().isHidden = true
    }
  }
  
  public func addLight(_ light: ARLightBase) {
    let lightNode = light.getNode()
    _lights[lightNode] = light
    
    if _sessionRunning {
      _sceneView.scene.rootNode.addChildNode(lightNode)
    } else {
      _requiresAddNodes = true
    }
  }
  
  public func removeLight(_ light: ARLightBase) {
    let lightNode = light.getNode()
    _lights.removeValue(forKey: lightNode)
    
    lightNode.removeFromParentNode()
  }
  
  @objc open func LightingEstimateUpdated(_ ambientIntensity: Float, _ ambientTemperature: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "LightingEstimateUpdated", arguments: ambientIntensity as NSNumber, ambientTemperature as NSNumber)
    }
  }
}

// MARK: LifeCycleDelegate
@available(iOS 11.3, *)
extension ARView3D: LifecycleDelegate {
  @objc public func onResume() {}
  
  @objc public func onPause() {
    if _sessionRunning {
      PauseTracking()
    }
  }
  
  @objc public func onDelete() {
    clearView()
  }
  
  @objc public func onDestroy() {
    clearView()
  }
  
  private func clearView() {
    _nodesDict.values.forEach {
      $0.stopFollowing()
      $0._node.removeFromParentNode()
    }
    
    _lights.values.forEach {
      $0.getNode().removeFromParentNode()
    }
    
    _nodesDict = [:]
    _lights = [:]
    _detectedPlanesDict = [:]
    _imageMarkers = [:]
  }
}
