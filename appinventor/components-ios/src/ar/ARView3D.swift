// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import ARKit
import UIKit
import os.log
import Combine

@available(iOS 14.0, *)
open class ARView3D: ViewComponent, ARSessionDelegate, ARNodeContainer, CLLocationManagerDelegate, EventSource {
  
  
  public func getView() -> ARView3D {
    return self
  }
  
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
  
  // Updated data structures for RealityKit
  fileprivate var _nodeToAnchorDict: [ARNodeBase : AnchorEntity] = [:]
  fileprivate var _detectedPlanesDict: [ARAnchor: DetectedPlane] = [:]
  fileprivate var _imageMarkers: [String : ImageMarker] = [:]
  fileprivate var _lights: [AnchorEntity: ARLightBase] = [:]
  
  final var _arView: ARView
  private var _trackingType: ARTrackingType = .worldTracking
  private var _configuration: ARConfiguration = ARWorldTrackingConfiguration()
  private var _planeDetection: ARPlaneDetectionType = .horizontal
  private var _showWorldOrigin: Bool = false
  private var _showFeaturePoints: Bool = false
  private var _showWireframes: Bool = false
  private var _showBoundingBoxes: Bool = false
  private var _showLightLocations: Bool = false
  private var _showLightAreas: Bool = false
  fileprivate var trackingNode: ARNode? = nil
  fileprivate var _sessionRunning: Bool = false
  fileprivate var _rotation: Float = 0.0
  fileprivate var _containsModelNodes: Bool = false
  fileprivate var _lightingEstimationEnabled: Bool = false
  
  // Needed for configuration setting and ensuring initialization
  fileprivate var _trackingSet: Bool = false
  private var _planeDetectionSet: Bool = false
  private var _lightingEstimationSet: Bool = false
  
  // Needed that way nodes are only added when the session is running and nodes are disabled when session stopped
  private var _requiresAddNodes = false
  private var startOptions: ARSession.RunOptions = []
  private var _reenableWebViewNodes: Bool = false
  
  private var locationManager: CLLocationManager?
  private var deviceLocation: CLLocation?
  private var sessionStartLocation: CLLocation?
  private var sessionStartTime: Date?
  
  internal var collisionBeganObserver: Cancellable!
  
  enum State {
    case none,
         colliding,
         released,
         idle
    
  }
  var currentState: State = .none
  
  
  // Cache for 3D text geometries representing the classification values
  private var modelsForClassification: [ARMeshClassification: ModelEntity] = [:]
  
  private func setupLocationManager() {
    locationManager = CLLocationManager()
    locationManager?.delegate = self
    locationManager?.desiredAccuracy = kCLLocationAccuracyBest
    locationManager?.requestWhenInUseAuthorization()
    locationManager?.startUpdatingLocation()
  }
  
  public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let location = locations.last else { return }
    
    if sessionStartLocation == nil && _sessionRunning {
      
      sessionStartLocation = location
      sessionStartTime = Date()
      print("AR session anchored to GPS: \(location.coordinate.latitude), \(location.coordinate.longitude), altitude: \(location.altitude)")
    }
  }
  
  public override init(_ parent: ComponentContainer) {
    _arView = ARView()
    _arView.environment.sceneUnderstanding.options = .occlusion
    _arView.environment.sceneUnderstanding.options.insert(.occlusion)
    
    _arView.translatesAutoresizingMaskIntoConstraints = false
    
    
    super.init(parent)
    _arView.session.delegate = self
    setupLocationManager()
    initializeGestureRecognizers()
    
    TrackingType = 1
    PlaneDetectionType = 2 // .horizontal
    LightingEstimation = false
    parent.add(self)
    Height = kARViewPreferredHeight
    Width = kARViewPreferredWidth
    

  }
  

  private func initializeGestureRecognizers() {
    let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap))
    _arView.addGestureRecognizer(tapGesture)
    
    if #available(iOS 16.0, *) {
      let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePan))
      panGesture.maximumNumberOfTouches = 1
      _arView.addGestureRecognizer(panGesture)
    } else {
      let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePanSimple))
      panGesture.maximumNumberOfTouches = 1
      _arView.addGestureRecognizer(panGesture)
    }
    
    let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(handleLongPress))
    longPressGesture.minimumPressDuration = 0.75
    _arView.addGestureRecognizer(longPressGesture)
    
    tapGesture.require(toFail: longPressGesture)
    
    let pinchGesture = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch))
    _arView.addGestureRecognizer(pinchGesture)
    pinchGesture.delegate = self
    
    let rotationGesture = UIRotationGestureRecognizer(target: self, action: #selector(handleRotation))
    _arView.addGestureRecognizer(rotationGesture)
    rotationGesture.delegate = self
  }
  
  // MARK: Properties
  
  @objc open override var view: UIView {
    get {
      return _arView
    }
  }
  
  @objc open var Nodes: [ARNode] {
    get {
      return Array(_nodeToAnchorDict.keys)
    }
  }
  
  @objc open var TrackingType: Int32 {
    get {
      return _trackingType.rawValue
    }
    set(type) {
      _trackingSet = true
      
      guard 1...4 ~= type else {
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
      return _arView.debugOptions.contains(.showStatistics)
    }
    set(showStatistics) {
      if showStatistics {
        _arView.debugOptions.insert(.showStatistics)
      } else {
        _arView.debugOptions.remove(.showStatistics)
      }
    }
  }
  
  @objc open var ShowWorldOrigin: Bool {
    get {
      return _showWorldOrigin
    }
    set(showOrigin) {
      if showOrigin {
        _arView.debugOptions.insert(.showWorldOrigin)
      } else {
        _arView.debugOptions.remove(.showWorldOrigin)
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
        _arView.debugOptions.insert(.showAnchorGeometry)
      } else {
        _arView.debugOptions.remove(.showAnchorGeometry)
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
        _arView.debugOptions.insert(.showAnchorOrigins)
      } else {
        _arView.debugOptions.remove(.showAnchorOrigins)
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
        _arView.debugOptions.insert(.showFeaturePoints)
      } else {
        _arView.debugOptions.remove(.showFeaturePoints)
      }
      _showFeaturePoints = showFeaturePoints
    }
  }
  
  @available(iOS 14.0, *)
  private func setupConfiguration() {
    guard _trackingSet && _planeDetectionSet && _lightingEstimationSet else { return }
    
    // Check geo tracking support only when needed
    if _trackingType == .geoTracking && !ARGeoTrackingConfiguration.isSupported {
      self._container?.form?.dispatchErrorOccurredEvent(self, "Geotracking", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code, "Geo tracking not supported on this device")
      return
    }
    
    switch _trackingType {
    case .worldTracking:
      let worldTrackingConfiguration = ARWorldTrackingConfiguration()
      
      // Enable scene reconstruction for occlusion and physics
      worldTrackingConfiguration.sceneReconstruction = .mesh
      worldTrackingConfiguration.maximumNumberOfTrackedImages = 4
      worldTrackingConfiguration.detectionImages = getReferenceImages()
      
      // Configure plane detection
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
      
    case .geoTracking:
      let geoTrackingConfiguration = ARGeoTrackingConfiguration()
      
      // ARGeoTrackingConfiguration does NOT support sceneReconstruction!
      // This means limited occlusion capabilities with geo tracking
      geoTrackingConfiguration.maximumNumberOfTrackedImages = 4
      geoTrackingConfiguration.detectionImages = getReferenceImages()
      
      // Configure plane detection (still available)
      switch _planeDetection {
      case .horizontal:
        geoTrackingConfiguration.planeDetection = .horizontal
      case .vertical:
        geoTrackingConfiguration.planeDetection = .vertical
      case .both:
        geoTrackingConfiguration.planeDetection = [.horizontal, .vertical]
      case .none:
        break
      }
      
      _configuration = geoTrackingConfiguration
      
    case .orientationTracking:
      _configuration = AROrientationTrackingConfiguration()
      
    case .imageTracking:
      let imageTrackingConfiguration = ARImageTrackingConfiguration()
      imageTrackingConfiguration.maximumNumberOfTrackedImages = 4
      imageTrackingConfiguration.trackingImages = getReferenceImages()
      _configuration = imageTrackingConfiguration
    }
    
    // Enable lighting estimation
    _configuration.isLightEstimationEnabled = _lightingEstimationEnabled
    
    // Configure scene understanding options
    setupSceneUnderstanding()
    
    // Setup collision detection
    setupCollisionDetection()
    
    if _sessionRunning {
      ResetTracking()
    }
  }
  
  private func setupSceneUnderstanding() {
    // Configure scene understanding based on tracking type
    _arView.environment.sceneUnderstanding.options = []
    
    if _trackingType == .worldTracking { // TODO expose these options to user
      // Full scene understanding available with world tracking
      _arView.environment.sceneUnderstanding.options.insert(.occlusion)
      _arView.environment.sceneUnderstanding.options.insert(.physics)
      _arView.environment.sceneUnderstanding.options.insert(.collision)
      print("Scene understanding enabled: occlusion, physics, collision")
      
      
      _arView.debugOptions.insert(.showSceneUnderstanding)
    } else if _trackingType == .geoTracking {
      // Limited scene understanding with geo tracking
      _arView.environment.sceneUnderstanding.options.insert(.occlusion)
      // Note: physics and collision may be limited without sceneReconstruction
      
      print("Scene understanding enabled: occlusion only (geo tracking)")
    } else {
      print("Scene understanding disabled for tracking type: \(_trackingType)")
    }
  }
  
  private func setupCollisionDetection() {
    // Cancel existing observer
    collisionBeganObserver?.cancel()
    
    // Create new observer - fix the syntax
    collisionBeganObserver = _arView.scene.subscribe(
      to: CollisionEvents.Began.self,
      on: nil  // Use nil instead of self for global subscription
    ) { event in
      print("🔥 COLLISION DETECTED!")
      print("Entity A: \(event.entityA.name)")
      print("Entity B: \(event.entityB.name)")
      
      self.forwardCollisionToNodes(event)
    }
    
    print("Collision observer set up successfully")
    
    debugCollisionSetup()
  }
  
  private func debugCollisionSetup() {
    print("=== Collision Debug Info ===")
    print("Scene understanding options: \(_arView.environment.sceneUnderstanding.options)")
    print("Collision observer exists: \(collisionBeganObserver != nil)")
    print("Number of nodes with physics: \(_nodeToAnchorDict.keys.filter { $0._modelEntity.physicsBody != nil }.count)")
    print("Total nodes: \(_nodeToAnchorDict.count)")
  }
  

  private func CollisionDetectedBegin(_ event: CollisionEvents.Began){
    let entityA = event.entityA
    let entityB = event.entityB
    
    print("Collision detected between: \(entityA.name) and \(entityB.name)")
    
    // Check if collision involves scene understanding mesh
    if String(describing: type(of: entityA)).contains("RKSceneUnderstanding") ||
        String(describing: type(of: entityB)).contains("RKSceneUnderstanding") {
      print("Collision with scene understanding mesh")
      
      // Determine which entity is your AR object
      let arEntity = String(describing: type(of: entityA)).contains("RKSceneUnderstanding") ? entityB : entityA
      
      // Handle collision with real world
      ObjectCollidedWithScene(arEntity)
    } else {
      // Collision between two AR objects
      ObjectCollidedWithObject(entityA, entityB)
    }
  }
  
  
  
  @objc open func ObjectCollidedWithScene(_ entity: AnyObject) {
    let arEntity = entity as! ModelEntity
    print("AR object \(arEntity.name) collided with real world")
    
    // Find the corresponding ARNode
    if let modelEntity = arEntity as? ModelEntity,
       let node = findNodeForEntity(modelEntity) {
      // Dispatch collision event
      EventDispatcher.dispatchEvent(of: self, called: "ObjectCollidedWithScene", arguments: node as AnyObject)
    }
  }
  
  @objc open func ObjectCollidedWithObject(_ entity1: AnyObject, _ entity2: AnyObject) {
    let entityA = entity1 as! ModelEntity
    let entityB = entity2 as! ModelEntity
    print("Collision between AR objects: \(entityA.name) and \(entityB.name)")
    
    // Handle AR object to AR object collision
    if let nodeA = findNodeForEntity(entityA as? ModelEntity),
       let nodeB = findNodeForEntity(entityB as? ModelEntity) {
      EventDispatcher.dispatchEvent(of: self, called: "ObjectsCollided",
                                    arguments: nodeA as AnyObject, nodeB as AnyObject)
    }
  }
  
  private func findNodeForEntity(_ entity: ModelEntity?) -> ARNodeBase? {
    guard let entity = entity else { return nil }
    
    for (node, anchor) in _nodeToAnchorDict {
      if node._modelEntity == entity || anchor.children.contains(entity) {
        return node
      }
    }
    return nil
  }
  
  private func getReferenceImages() -> Set<ARReferenceImage> {
    return Set(_imageMarkers.values.compactMap{ $0._referenceImage })
  }
  
  // MARK: Functions
  
  @objc open func StartTracking() {
    startTracking(startOptions)
  }
  
  @objc open func startTracking(_ options: ARSession.RunOptions = []) {
    _arView.session.run(_configuration, options: options)
    _sessionRunning = true
    startOptions = []
    
    if _requiresAddNodes {
      for (node, anchorEntity) in _nodeToAnchorDict {
        if node.IsGeoAnchored {
          // Handle geo anchors
          if let geoAnchor = node.getGeoAnchor() {
            _arView.session.add(anchor: geoAnchor)
            // AnchorEntity will be created in delegate callback
          }
        } else {
          
          if _reenableWebViewNodes, let webViewNode = node as? ARWebView {
            webViewNode.isUserInteractionEnabled = true
          }
          
          
          
          if !node.IsFollowingImageMarker {
            _arView.scene.addAnchor(anchorEntity)
          }
          
          
          if node._fromPropertyPosition != nil {
            let position = node._fromPropertyPosition.split(separator: ",")
              .prefix(3)
              .map { Float(String($0)) ?? 0.0 }
            
            node._modelEntity.transform.translation = SIMD3<Float>(
              UnitHelper.centimetersToMeters(position[0]),
              UnitHelper.centimetersToMeters(position[1]),
              UnitHelper.centimetersToMeters(position[2])
            )
          }
        }
      }
      
      for (anchorEntity, light) in _lights {
        _arView.scene.addAnchor(anchorEntity)
      }
      
      _requiresAddNodes = false
      _reenableWebViewNodes = false
    } else if _reenableWebViewNodes {
      for node in _nodeToAnchorDict.keys {
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
    _arView.session.pause()
    trackingNode = nil
    
    if disableWebViewInteraction {
      for node in _nodeToAnchorDict.keys {
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
  
  
  
  // These methods would need to be implemented based on your ARNode creation logic
  private func CreateCapsuleNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    
    let capNode = CapsuleNode(self) as CapsuleNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      capNode as CapsuleNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    
    
    return result
  }
  
  private func CreateBoxNodeFromYail(_ nodeDict: YailDictionary) -> ARNodeBase? {
    // Implementation depends on your ARNode structure
    return nil
  }
  
  private func CreateSphereNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let sphereNode = SphereNode(self) as SphereNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      sphereNode as SphereNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
  }
  
  private func CreateModelNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let modelnode = ModelNode(self) as ModelNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      modelnode as ModelNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
  }
  
  private func CreateTextNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let textnode = TextNode(self) as TextNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      textnode as TextNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
  }
  
  private func CreateVideoNodeFromYail(_ nodeDict: YailDictionary) -> ARNodeBase? {
    // Implementation depends on your ARNode structure
    return nil
  }
  
  private func CreateWebViewNodeFromYail(_ nodeDict: YailDictionary) -> ARNodeBase? {
    // Implementation depends on your ARNode structure
    return nil
  }
  
  private func addGeoAnchoredNode(_ node: ARNodeBase) {
    guard let geoAnchor = node.getGeoAnchor() else {
      print("No geo anchor found on node")
      return
    }
    
    _containsModelNodes = node is ModelNode ? true : _containsModelNodes
    
    if _sessionRunning {
      // Add geo anchor to session first
      _arView.session.add(anchor: geoAnchor)
      
      // Store node with placeholder - actual AnchorEntity created in delegate
      _nodeToAnchorDict[node] = nil
      
      print("Added geo anchor to session: \(geoAnchor.coordinate)")
    } else {
      // Store for later when session starts
      _nodeToAnchorDict[node] = nil
      _requiresAddNodes = true
    }
  }
  
  // called after LoadScene and during node init
  @objc open func addNode(_ node: ARNodeBase) {
    if node.IsGeoAnchored {
      print("✅ Taking GEO ANCHOR branch - calling addGeoAnchoredNode")
      addGeoAnchoredNode(node)
    } else {
      var anchorEntity = node.Anchor
      if (anchorEntity == nil){
        anchorEntity = node.createAnchor()
      }
      let nodeId = ObjectIdentifier(node)
      print("adding the node")
      _nodeToAnchorDict[node] = anchorEntity
      _containsModelNodes = node is ModelNode ? true : _containsModelNodes
      
      if _sessionRunning {
        _arView.scene.addAnchor(anchorEntity!)
        
        print("adding anchor and setting position \(anchorEntity?.position.x) \(anchorEntity?.position.y) \(anchorEntity?.position.z)")
        if !node._fromPropertyPosition.isEmpty {
          let position = node._fromPropertyPosition.split(separator: ",")
            .prefix(3)
            .map { Float(String($0)) ?? 0.0 }
          
          node._modelEntity.transform.translation = SIMD3<Float>(
            UnitHelper.centimetersToMeters(position[0]),
            UnitHelper.centimetersToMeters(position[1]),
            UnitHelper.centimetersToMeters(position[2])
          )
          node.EnablePhysics(node.EnablePhysics)
        }
      } else {
        _requiresAddNodes = true
      }
    }
  }
  
  @objc open func removeNode(_ node: ARNodeBase) {
    guard let anchorEntity = _nodeToAnchorDict[node] else { return }
    
    _arView.scene.removeAnchor(anchorEntity)
    _nodeToAnchorDict.removeValue(forKey: node)
    node.removeFromAnchor()
  }
  
  @objc open func getARView() -> ARView3D {
    return self
  }
  
  @objc open func HideAllNodes() {
    for node in _nodeToAnchorDict.keys {
      node._modelEntity.isEnabled = false
    }
  }
  
  open func sessionShouldAttemptRelocalization(_ session: ARSession) -> Bool {
    return true
  }
  
  // MARK: ARSession Delegate Methods
  
  public func session(_ session: ARSession, didAdd anchors: [ARAnchor]) {
    
    for anchor in anchors {
      
      if let planeAnchor = anchor as? ARPlaneAnchor {
        let detectedPlane = DetectedPlane(anchor: planeAnchor, container: self)
        _detectedPlanesDict[anchor] = detectedPlane
        PlaneDetected(detectedPlane)
      } else if let imageAnchor = anchor as? ARImageAnchor {
        guard let name = imageAnchor.referenceImage.name else { return }
        let imageMarker = _imageMarkers[name]
        //imageMarker?.FirstDetected(anchor)
      } else if let geoAnchor = anchor as? ARGeoAnchor {
        // Handle geo anchor being tracked
        handleGeoAnchorAdded(geoAnchor)
      }
    }
  }
  
  public func session(_ session: ARSession, didUpdate anchors: [ARAnchor]) {
    
    for anchor in anchors {
      
      if let planeAnchor = anchor as? ARPlaneAnchor, let updatedPlane = _detectedPlanesDict[anchor] {
        updatedPlane.updateFor(anchor: planeAnchor)
        DetectedPlaneUpdated(updatedPlane)
      } else if let imageAnchor = anchor as? ARImageAnchor {
        guard let name = imageAnchor.referenceImage.name, let imageMarker = _imageMarkers[name] else { return }
        if !imageAnchor.isTracked {
          imageMarker.NoLongerInView()
        } else if !imageMarker._isTracking {
          imageMarker.AppearedInView()
        }
        let position = SIMD3<Float>(imageAnchor.transform.columns.3.x, imageAnchor.transform.columns.3.y, imageAnchor.transform.columns.3.z)
        let rotation = imageAnchor.transform.eulerAngles
        imageMarker.pushUpdate(position, rotation)
      }else if let geoAnchor = anchor as? ARGeoAnchor {
        // Handle geo anchor being tracked
        handleGeoAnchorAdded(geoAnchor)
      }
    }
  }
  
  public func session(_ session: ARSession, didRemove anchors: [ARAnchor]) {
    for anchor in anchors {
      if let planeAnchor = anchor as? ARPlaneAnchor, let removedPlane = _detectedPlanesDict[anchor] {
        _detectedPlanesDict.removeValue(forKey: anchor)
        removedPlane.removed()
        DetectedPlaneRemoved(removedPlane)
      } else if let imageAnchor = anchor as? ARImageAnchor {
        guard let name = imageAnchor.referenceImage.name, let imageMarker = _imageMarkers[name] else { return }
        imageMarker.Reset()
      }
    }
  }
  
  public func session(_ session: ARSession, didUpdate frame: ARFrame) {
    guard _lightingEstimationEnabled, let lightingEstimate = frame.lightEstimate else { return }
    
    LightingEstimateUpdated(Float(lightingEstimate.ambientIntensity), Float(lightingEstimate.ambientColorTemperature))
  }
  
  private func handleGeoAnchorAdded(_ geoAnchor: ARGeoAnchor) {
    print("🔥 handleGeoAnchorAdded called!")
    print("📍 Geo anchor coordinate: \(geoAnchor.coordinate)")
    print("🌍 Geo anchor transform: \(geoAnchor.transform)")
    
    // Calculate distance from session start
    if let sessionStart = sessionStartLocation {
      let distance = CLLocation(latitude: geoAnchor.coordinate.latitude, longitude: geoAnchor.coordinate.longitude)
        .distance(from: sessionStart)
      print("📏 Distance from session start: \(distance) meters")
    }
    
    // Find the node that corresponds to this geo anchor
    for (node, _) in _nodeToAnchorDict {
      if let nodeGeoAnchor = node.getGeoAnchor(),
         nodeGeoAnchor.identifier == geoAnchor.identifier {
        
        print("✅ Found matching node: \(node.Name)")
        
        let anchorEntity = AnchorEntity(world: geoAnchor.transform)
        anchorEntity.addChild(node._modelEntity)
        _arView.scene.addAnchor(anchorEntity)
        
        // Update our tracking
        _nodeToAnchorDict[node] = anchorEntity
        node._anchorEntity = anchorEntity
        
        if let currentSessionStart = sessionStartLocation,
           let creatorSessionStart = node._creatorSessionStart,
           let worldOffset = node._worldOffset {
          
          // Check if current user is close to where anchor was created
          let creatorLocation = CLLocation(latitude: creatorSessionStart.coordinate.latitude, longitude: creatorSessionStart.coordinate.longitude)
          let currentDistance = currentSessionStart.distance(from: creatorLocation)
          
          if currentDistance < 5.0 {
            // User is very close to original creation location - use precise world coordinates
            if let creatorStartWorldPos = self.gpsToWorld(creatorSessionStart.coordinate, creatorSessionStart.altitude) {
              let precisePosition = creatorStartWorldPos + worldOffset
              node.setPosition(x: precisePosition.x, y: precisePosition.y, z: precisePosition.z)
              return
            }
          }
          
          
          print("🎯 Geo anchor now tracked and displayed at \(geoAnchor.coordinate)")
          print("📍 AnchorEntity position: \(anchorEntity.position)")
          break
        }
      }
    }
  }
    // MARK: Events
    @objc open func NodeClick(_ node: ARNode) {
      EventDispatcher.dispatchEvent(of: self, called: "NodeClick", arguments: node as AnyObject)
    }
    
    @objc open func TapAtPoint(_ x: Float, _ y: Float, _ z: Float, _ isANodeAtPoint: Bool) {
      let xCm: Float = UnitHelper.metersToCentimeters(x)
      let yCm: Float = UnitHelper.metersToCentimeters(y)
      let zCm: Float = UnitHelper.metersToCentimeters(z)
      print("tapped at \(xCm), \(yCm), \(zCm)")
      EventDispatcher.dispatchEvent(of: self, called: "TapAtPoint", arguments: xCm as NSNumber, yCm as NSNumber, zCm as NSNumber, isANodeAtPoint as NSNumber)
    }
    
    @objc open func TapAtLocation(_ x: Float, _ y: Float, _ z: Float,
                                  _ lat: Double, _ lng: Double, _ alt: Double,
                                  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) {
      
      // Convert world coordinates to centimeters for compatibility
      let xCm = UnitHelper.metersToCentimeters(x)
      let yCm = UnitHelper.metersToCentimeters(y)
      let zCm = UnitHelper.metersToCentimeters(z)
      
      if hasGeoCoordinates {
        // Dispatch with both world and geo coordinates
        EventDispatcher.dispatchEvent(of: self, called: "TapAtLocation",
                                      arguments: xCm as NSNumber, yCm as NSNumber, zCm as NSNumber,
                                      lat as NSNumber, lng as NSNumber, alt as NSNumber,
                                      true as NSNumber, isANodeAtPoint as NSNumber)
      } else {
        // Dispatch with only world coordinates
        EventDispatcher.dispatchEvent(of: self, called: "TapAtLocation",
                                      arguments: xCm as NSNumber, yCm as NSNumber, zCm as NSNumber,
                                      0.0 as NSNumber, 0.0 as NSNumber, 0.0 as NSNumber,
                                      false as NSNumber, isANodeAtPoint as NSNumber)
      }
    }
    
    
    @objc open func NodeLongClick(_ node: ARNode) {
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
  @available(iOS 14.0, *)
  extension ARView3D {
    @objc open func CreateBoxNode(_ x: Float, _ y: Float, _ z: Float) -> BoxNode {
      let node = BoxNode(self)
      node.Name = "CreatedBoxNode"
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    @objc open func CreateSphereNode(_ x: Float, _ y: Float, _ z: Float) -> SphereNode {
      let node = SphereNode(self)
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    
    
    
    
    
    
    @objc open func CreatePlaneNode(_ x: Float, _ y: Float, _ z: Float) -> PlaneNode {
      let node = PlaneNode(self)
      node.Name = "CreatedPlaneNode"
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    /*
     @objc open func CreateCylinderNode(_ x: Float, _ y: Float, _ z: Float) -> CylinderNode {
     let node = CylinderNode(self)
     node.Name = "CreatedCylinderNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     
     @objc open func CreateConeNode(_ x: Float, _ y: Float, _ z: Float) -> ConeNode {
     let node = ConeNode(self)
     node.Name = "CreatedConeNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     */
    @objc open func CreateCapsuleNode(_ x: Float, _ y: Float, _ z: Float) -> CapsuleNode {
      let node = CapsuleNode(self)
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    private func setupLocation(x: Float, y: Float, z: Float, latitude: Double, longitude: Double, altitude: Double,node: ARNodeBase, hasGeoCoordinates: Bool) {
      
      
      
      
      // Create geo anchor if we can
      if hasGeoCoordinates {
        let coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        if CLLocationCoordinate2DIsValid(coordinate) {
          let geoAnchor = ARGeoAnchor(coordinate: coordinate, altitude: altitude)
          node.setGeoAnchor(geoAnchor)
          print("setup node's geoanchor \(String(describing: geoAnchor))")
          

          // Check distance from session start
          if let sessionStart = sessionStartLocation {
            let anchorLocation = CLLocation(latitude: latitude, longitude: longitude)
            let distance = sessionStart.distance(from: anchorLocation)
            
            if distance < 10.0 {
              // Close anchor: Store world coordinates as precision backup
              let xMeters: Float = UnitHelper.centimetersToMeters(x)
              let yMeters: Float = UnitHelper.centimetersToMeters(y)
              let zMeters: Float = UnitHelper.centimetersToMeters(z)
              node.setPosition(x: xMeters, y: yMeters, z: zMeters)
              node._worldOffset = SIMD3<Float>(x: xMeters, y: yMeters, z: zMeters)
              node._creatorSessionStart = anchorLocation
              print("saved world coords for offset \(String(describing: node._worldOffset))\(String(describing: node._creatorSessionStart))")
            }
            // Far anchors: GPS only (precision less critical)
            return
          }
        }else {
          print("setting up location error: Invalid Coordinates", ErrorMessage.ERROR_INVALID_COORDINATES.code)
        }
        
      }
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      print("set up anchor and setting position \(xMeters) \(yMeters) \(zMeters)")
      
    }
    
    @objc open func CreateCapsuleNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> CapsuleNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateCapsuleNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = CapsuleNode(self)
      node.Name = "GeoCapsuleNode"
      
      
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      
      node.Initialize()  // order is important as we need to set geoanchor first b/c init overrides it - or fix that
      
      print("✅ Created node: \(node.Name)")
      print("🔍 IsGeoAnchored: \(node.IsGeoAnchored)")
      print("🌍 Has geo anchor: \(node.getGeoAnchor() != nil)")
      if let geoAnchor = node.getGeoAnchor() {
        print("📍 Geo anchor coordinates: \(geoAnchor.coordinate)")
      }
      return node
    }
    
    @objc open func CreateModelNode(_ x: Float, _ y: Float, _ z: Float, _ modelObjString: String) -> ModelNode {
      let node:ModelNode = ModelNode(self)
      node.Name = "modelNode"
      //print("adding anchor and setting position \(anchorEntity?.position.x) \(anchorEntity?.position.y) \(anchorEntity?.position.z)")
      node.Initialize()
      node.Model = modelObjString
      
      let xMeters: Float = 1.0 //UnitHelper.centimetersToMeters(x)
      let yMeters: Float = 0.0 //UnitHelper.centimetersToMeters(y)
      let zMeters: Float = 1.0 //UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    @objc open func CreateModelNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool, _ modelObjString: String) -> ModelNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreatModelNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node:ModelNode = ModelNode(self)
      node.Name = "GeoModelNode"
      node.Model = modelObjString
      node.Initialize()  // order is important as we need to set geoanchor first b/c init overrides it - or fix that
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      return node
    }
    
    @objc open func CreateSphereNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> SphereNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateSphereNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = SphereNode(self)
      node.Name = "GeoSphereNode"
      node.Initialize()
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      
      return node
    }
    
    @objc open func CreateTextNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> TextNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateTextNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = TextNode(self)
      node.Name = "GeoTextNode"
      node.Initialize()
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      
      return node
    }
    
    
    
    /*
     @objc open func CreateTubeNode(_ x: Float, _ y: Float, _ z: Float) -> TubeNode {
     let node = TubeNode(self)
     node.Name = "CreatedTubeNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     
     @objc open func CreateTorusNode(_ x: Float, _ y: Float, _ z: Float) -> TorusNode {
     let node = TorusNode(self)
     node.Name = "CreatedTorusNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     
     @objc open func CreatePyramidNode(_ x: Float, _ y: Float, _ z: Float) -> PyramidNode {
     let node = PyramidNode(self)
     node.Name = "CreatedPyramidNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     
     
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     
     @objc open func CreateVideoNode(_ x: Float, _ y: Float, _ z: Float) -> VideoNode {
     let node = VideoNode(self)
     node.Name = "CreatedVideoNode"
     node.Initialize()
     
     let xMeters: Float = UnitHelper.centimetersToMeters(x)
     let yMeters: Float = UnitHelper.centimetersToMeters(y)
     let zMeters: Float = UnitHelper.centimetersToMeters(z)
     node.setPosition(x: xMeters, y: yMeters, z: zMeters)
     return node
     }
     */
    
    @objc open func CreateTextNode(_ x: Float, _ y: Float, _ z: Float) -> TextNode {
      let node = TextNode(self)
      node.Name = "CreatedTextNode"
      node.Initialize()
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    @objc open func CreateWebViewNode(_ x: Float, _ y: Float, _ z: Float) -> WebViewNode {
      let node = WebViewNode(self)
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    @objc open func CreateWebViewNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ latitude: Double, _ longitude: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> WebViewNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateWebViewNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = WebViewNode(self)
      node.Name = "GeoWebViewNode"
      node.Initialize()
      
      setupLocation(x: x, y: y, z: z, latitude: latitude, longitude: longitude, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      
      return node
    }
    
    @objc open func LoadScene(_ dictionaries: [AnyObject]) -> [AnyObject] {
      print("loading stored scene \(dictionaries)")
      
      var loadNode: ARNodeBase?
      var newNodes: [ARNode] = []
      
      guard !dictionaries.isEmpty else {
        return []
      }
      
      
      for obj in dictionaries {
        if obj is YailDictionary{
          
          let nodeDict = obj as! YailDictionary
          
          
          guard let type = nodeDict["type"] as? String else {
            print("loadscene missing type (\type)")
            continue
          }
          
          switch type.lowercased() {
          case "capsule", "geocapsulenode":
            loadNode = self.CreateCapsuleNodeFromYail(nodeDict)
          case "box":
            loadNode = self.CreateBoxNodeFromYail(nodeDict)
          case "sphere", "geospherenode":
            loadNode = self.CreateSphereNodeFromYail(nodeDict)
          case "video":
            loadNode = self.CreateVideoNodeFromYail(nodeDict)
          case "webview":
            loadNode = self.CreateWebViewNodeFromYail(nodeDict)
          case "model", "geomodelnode":
            loadNode = self.CreateModelNodeFromYail(nodeDict)
          case "text", "geotextnode":
            loadNode = self.CreateModelNodeFromYail(nodeDict)
          default:
            // currently not storing or handling modelNode..
            loadNode = nil
          }
          
          if let node = loadNode {
            addNode(node)
            newNodes.append(node)
            print("loaded (\node)")
          }
        }
        
      }
      
      print("loadscene new nodes are \(newNodes)")
      return newNodes
    }
    
    //objc signature expects only primitives or object
    @objc open func SaveScene(_ newNodes: [AnyObject]) -> [YailDictionary] {
      var dictionaries: [YailDictionary] = []
      // a list of arnodes
      for node in newNodes { // swift thinks newnodes is nsarray
        
        guard let arNode = node as? ARNode else { continue }
        
        let nodeDict = arNode.ARNodeToYail()
        dictionaries.append(nodeDict)
        
      }
      print("returning dictionaries")
      return dictionaries
    }
    
    
    
    public func worldToGPS(_ worldPoint: SIMD3<Float>) -> (coordinate: CLLocationCoordinate2D, altitude: Double)? {
      guard let sessionStart = sessionStartLocation else { return nil }
      
      // Calculate offset from session origin
      let deltaX = Double(worldPoint.x)  // East/West
      let deltaY = Double(worldPoint.y)  // Up/Down (altitude)
      let deltaZ = Double(worldPoint.z)  // North/South
      
      // Convert horizontal meters to degrees
      let earthRadius = 6371000.0  // meters
      let latOffset = deltaZ / earthRadius * 180.0 / .pi
      let lonOffset = deltaX / (earthRadius * cos(sessionStart.coordinate.latitude * .pi / 180.0)) * 180.0 / .pi
      
      // Calculate new coordinates
      let newLatitude = sessionStart.coordinate.latitude + latOffset
      let newLongitude = sessionStart.coordinate.longitude + lonOffset
      let newAltitude = sessionStart.altitude + deltaY  // Add Y offset to starting altitude
      
      return (
        coordinate: CLLocationCoordinate2D(latitude: newLatitude, longitude: newLongitude),
        altitude: newAltitude
      )
    }
    
    public func gpsToWorld(_ coordinate: CLLocationCoordinate2D, _ altitude: Double) -> SIMD3<Float>? {
      guard let sessionStart = sessionStartLocation else { return nil }
      
      // Calculate differences in degrees
      let latDiff = coordinate.latitude - sessionStart.coordinate.latitude
      let lonDiff = coordinate.longitude - sessionStart.coordinate.longitude
      let altDiff = altitude - sessionStart.altitude
      
      // Convert degrees to meters
      let earthRadius = 6371000.0  // meters
      let deltaZ = Float(latDiff * earthRadius * .pi / 180.0)  // North/South
      let deltaX = Float(lonDiff * earthRadius * cos(sessionStart.coordinate.latitude * .pi / 180.0) * .pi / 180.0)  // East/West
      let deltaY = Float(altDiff)  // Up/Down
      
      return SIMD3<Float>(deltaX, deltaY, deltaZ)
    }
  }
  


  
  
// MARK: Functions Handling Gestures
@available(iOS 14.0, *)
extension ARView3D: UIGestureRecognizerDelegate {
  
  func findClosestNode(tapLocation: CGPoint) -> ARNodeBase? {
      
      var bestNode: ARNodeBase?
      var bestScore: Float = Float.greatestFiniteMagnitude
      
      // Get fresh world hit point
      let worldHitPoint: SIMD3<Float>?
      if let result = _arView.raycast(from: tapLocation, allowing: .estimatedPlane, alignment: .any).first {
          worldHitPoint = SIMD3<Float>(
              result.worldTransform.columns.3.x,
              result.worldTransform.columns.3.y,
              result.worldTransform.columns.3.z
          )
      } else {
          worldHitPoint = nil
      }
      
      for (node, _) in _nodeToAnchorDict {
          // Screen space check (always works, accounts for camera movement)
          let nodeScreenPos = _arView.project(node._modelEntity.position)
          let screenDistance = sqrt(
            pow(tapLocation.x - nodeScreenPos!.x, 2) +
            pow(tapLocation.y - nodeScreenPos!.y, 2)
          )
          
          // Skip if too far in screen space
          if screenDistance > 100.0 { continue }  // 100 pixel max
          
          // World space check (more accurate for close objects)
          var worldDistance: Float = Float.greatestFiniteMagnitude
          if let hitPoint = worldHitPoint {
              let nodePosition = node._modelEntity.transform.translation
              worldDistance = simd_distance(nodePosition, hitPoint)
          }
          
          // Combine both distances with weighting
          let screenWeight: Float = 0.3
          let worldWeight: Float = 0.7
          
          let normalizedScreenDistance = screenDistance / 100.0  // Normalize to 0-1
          let normalizedWorldDistance = min(worldDistance / 0.5, 1.0)  // Normalize to 0-1
          
        let combinedScore = (screenWeight * Float(normalizedScreenDistance)) +
                             (worldWeight * normalizedWorldDistance)
          
          if combinedScore < bestScore {
              bestScore = combinedScore
              bestNode = node
          }
      }
      
      if let node = bestNode {
          print("Selected node \(node.Name) with combined score: \(bestScore)")
          return node
      }
      
      return nil
  }
  
  @objc func handleTap(_ sender: UITapGestureRecognizer) {
    let tapLocation = sender.location(in: _arView)
    var isNodeAtPoint = false
    
    // Check if we hit a node entity
    if let nodeEntity = findClosestNode(tapLocation:tapLocation) {
      NodeClick(nodeEntity)
      nodeEntity.Click()
      isNodeAtPoint = true
    }
    
    // Perform raycast for world interaction
    if let result = _arView.raycast(from: tapLocation, allowing: .estimatedPlane, alignment: .any).first {
      let hitPoint = SIMD3<Float>(
        result.worldTransform.columns.3.x,
        result.worldTransform.columns.3.y,
        result.worldTransform.columns.3.z
      )
      
      if let geoData = worldToGPS(hitPoint){
        print("Converted to GPS: \(geoData)")
        self.TapAtLocation(hitPoint.x, hitPoint.y, hitPoint.z,
                           geoData.coordinate.latitude, geoData.coordinate.longitude, geoData.altitude,
                           true, isNodeAtPoint)
        
      } else {
        print("❌ Failed to convert to GPS - sessionStartLocation: \(sessionStartLocation)")
        
        self.TapAtLocation(hitPoint.x, hitPoint.y, hitPoint.z,
                           0.0,0.0,0.0,
                           false, isNodeAtPoint)
        
      }
    }
  }
  
  
  
  // Helper method to find node for entity
  private func findNodeForEntity(_ entity: ModelEntity) -> ARNodeBase? {
    for (node, anchor) in _nodeToAnchorDict {
      if node._modelEntity == entity || anchor.children.contains(entity) {
        return node
      }
    }
    return nil
  }
  
  @objc fileprivate func handlePanSimple(sender: UIPanGestureRecognizer) {
    guard _sessionRunning else { return }
    let tapLocation = sender.location(in: _arView)
    
    switch sender.state {
    case .began:
      if let entity = _arView.entity(at: tapLocation) as? ModelEntity,
         let node = findNodeForEntity(entity) {
        guard trackingNode == nil else {
          trackingNode = nil
          return
        }
        trackingNode = node
      }
    case .changed:
      guard let node = trackingNode else { return }
      let translation = sender.translation(in: _arView)
      node.moveByPan(x: Float(translation.x / 10000.0), y: Float(-translation.y / 10000.0))
    case .ended:
      trackingNode = nil
    default:
      trackingNode = nil
    }
  }
  
  
  private func isPositionValid(_ position: SIMD3<Float>) -> Bool {
    // Basic collision check - make sure we're not going below floor
    return position.y > -1.5
  }

  private func forwardCollisionToNodes(_ event: CollisionEvents.Began) {
      let entityA = event.entityA
      let entityB = event.entityB
      
      print("🔥 Collision between: \(entityA.name) and \(entityB.name)")
      
      // Evaluate each entity to determine what it is
      let entityAInfo = evaluateEntity(entityA)
      let entityBInfo = evaluateEntity(entityB)
      
      print("🔥 EntityA: \(entityAInfo.description)")
      print("🔥 EntityB: \(entityBInfo.description)")
      
      // Handle collision based on what each entity is
      handleCollisionBetween(entityAInfo, and: entityBInfo, event: event)
  }
  
  private func evaluateEntity(_ entity: Entity) -> EntityInfo {
      // First, check if it's one of our AR nodes
      if let node = findNodeForEntity(entity as? ModelEntity) {
          return EntityInfo(type: .arNode, entity: entity, node: node)
      }
      
      // If not an AR node, classify as scene element
      let sceneType = classifySceneEntity(entity)
      return EntityInfo(type: .sceneElement, entity: entity, sceneType: sceneType)
  }
      
    private func classifySceneEntity(_ entity: Entity) -> SceneEntityType {
        let entityType = String(describing: type(of: entity))
        let entityName = entity.name
        let position = entity.transform.translation
        
        print("🔍 Classifying entity: \(entityName), type: \(entityType), position: \(position)")
        
        // Check for RealityKit scene understanding
        if entityType.contains("RKSceneUnderstanding") {
            return classifySceneUnderstandingEntity(entity, position: position)
        }
        
        // Check for your invisible floor
        if entityName == "InvisibleFloor" {
            return .floor
        }
        
        // Check for detected planes
        if entityName.contains("DetectedPlane") {
            return classifyDetectedPlane(entity)
        }
        
        // Check for mesh entities
        if entityType.contains("MeshEntity") {
            return classifyMeshEntity(entity, position: position)
        }
        
        // Fallback classification based on position and bounds
        return classifyByPosition(entity, position: position)
    }
      
    private func classifySceneUnderstandingEntity(_ entity: Entity, position: SIMD3<Float>) -> SceneEntityType {
        // For RealityKit scene understanding, we can often get more info
        if position.y < -1.0 {
            return .floor
        } else if position.y > 2.0 {
            return .ceiling
        } else {
            // Could be wall or furniture - check bounds if available
            return .wall
        }
    }
      
    private func classifyDetectedPlane(_ entity: Entity) -> SceneEntityType {
        // Most detected planes are floors, but we could check orientation
        // if we had access to the ARPlaneAnchor
        return .floor
    }
      
    private func classifyMeshEntity(_ entity: Entity, position: SIMD3<Float>) -> SceneEntityType {
        // Check bounds and position for mesh entities
        if position.y < -0.5 {
            return .floor
        } else if position.y > 2.0 {
            return .ceiling
        } else {
            // Could be wall or furniture
            return .wall
        }
    }
      
    private func classifyByPosition(_ entity: Entity, position: SIMD3<Float>) -> SceneEntityType {
        if position.y < -0.5 {
            return .floor
        } else if position.y > 2.5 {
            return .ceiling
        } else {
            return .wall
        }
    }
      
    private func handleCollisionBetween(_ entityAInfo: EntityInfo, and entityBInfo: EntityInfo, event: CollisionEvents.Began) {
        
        switch (entityAInfo.type, entityBInfo.type) {
        case (.arNode, .arNode):
            // Node-to-Node collision
            handleNodeToNodeCollision(
                nodeA: entityAInfo.node!,
                nodeB: entityBInfo.node!,
                event: event
            )
            
        case (.arNode, .sceneElement):
            // Node hit scene element
            handleNodeToSceneCollision(
                node: entityAInfo.node!,
                sceneEntity: entityBInfo.entity,
                sceneType: entityBInfo.sceneType!,
                event: event
            )
            
        case (.sceneElement, .arNode):
            // Scene element hit node (same as node hit scene element)
            handleNodeToSceneCollision(
                node: entityBInfo.node!,
                sceneEntity: entityAInfo.entity,
                sceneType: entityAInfo.sceneType!,
                event: event
            )
            
        case (.sceneElement, .sceneElement):
            // Scene-only collision (no AR nodes involved)
            print("🔥 Scene-only collision: \(entityAInfo.sceneType!) vs \(entityBInfo.sceneType!)")
        }
    }
      
      private func handleNodeToNodeCollision(nodeA: ARNodeBase, nodeB: ARNodeBase, event: CollisionEvents.Began) {
          print("🔥 Node-to-Node collision: \(nodeA.Name) <-> \(nodeB.Name)")
          
          // Both nodes handle the collision
          nodeA.handleNodeCollision(with: nodeB, event: event)
          nodeB.handleNodeCollision(with: nodeA, event: event)
      }
      
      private func handleNodeToSceneCollision(node: ARNodeBase, sceneEntity: Entity, sceneType: SceneEntityType, event: CollisionEvents.Began) {
          print("🔥 Node-to-Scene collision: \(node.Name) hit \(sceneType)")
          
          // Only the node handles the collision
        node.handleSceneCollision(sceneEntity: sceneEntity, sceneType: sceneType, event: event)
      }
      
      // MARK: - Supporting Types
      
      struct EntityInfo {
          let type: EntityType
          let entity: Entity
          let node: ARNodeBase?
          let sceneType: SceneEntityType?
          
          init(type: EntityType, entity: Entity, node: ARNodeBase? = nil, sceneType: SceneEntityType? = nil) {
              self.type = type
              self.entity = entity
              self.node = node
              self.sceneType = sceneType
          }
          
          var description: String {
              switch type {
              case .arNode:
                  return "ARNode(\(node?.Name ?? "unknown"))"
              case .sceneElement:
                  return "SceneElement(\(sceneType?.rawValue ?? "unknown"))"
              }
          }
      }
      
      enum EntityType {
          case arNode
          case sceneElement
      }
      
      enum SceneEntityType: String {
          case floor = "floor"
          case wall = "wall"
          case ceiling = "ceiling"
          case furniture = "furniture"
          case unknown = "unknown"
      }
  
  

      func screenDragToWorldDirection(_ screenVector: CGPoint) -> SIMD3<Float> {
          let cameraTransform = _arView.cameraTransform
          
        let rightVector = SIMD3<Float>(cameraTransform.matrix.columns.0.x, 0, cameraTransform.matrix.columns.0.z)
        let forwardVector = SIMD3<Float>(-cameraTransform.matrix.columns.2.x, 0, -cameraTransform.matrix.columns.2.z)
          
          let normalizedRight = simd_normalize(rightVector)
          let normalizedForward = simd_normalize(forwardVector)
          
          let worldDirection = normalizedRight * Float(screenVector.x) * 0.001 +
                              normalizedForward * Float(-screenVector.y) * 0.001
          
          return worldDirection
      }
      
      func screenVelocityToWorldDirection(_ screenVelocity: CGPoint) -> SIMD3<Float> {
          let cameraTransform = _arView.cameraTransform
          
        let rightVector = SIMD3<Float>(cameraTransform.matrix.columns.0.x, 0, cameraTransform.matrix.columns.0.z)
        let forwardVector = SIMD3<Float>(-cameraTransform.matrix.columns.2.x, 0, -cameraTransform.matrix.columns.2.z)
          
          let normalizedRight = simd_normalize(rightVector)
          let normalizedForward = simd_normalize(forwardVector)
          
          return normalizedRight * Float(screenVelocity.x) + normalizedForward * Float(-screenVelocity.y)
      }

    // Simple, clean drag handler
    @objc fileprivate func handlePan(_ gesture: UIPanGestureRecognizer) {
        guard _sessionRunning else { return }
        
        let location = gesture.location(in: _arView)
        
        switch gesture.state {
        case .began:
            // Find node and let it handle drag start
            if let draggedNode = findClosestNode(tapLocation: location) {
                draggedNode.startDrag()
            }
            
        case .changed:
            // Let the dragged node handle the update
            if let draggedNode = findDraggedNode(at: location) {
              let worldDirection = screenDragToWorldDirection(calculateDragVector(gesture))

              draggedNode.updateDrag(dragVector: calculateDragVector(gesture), velocity: gesture.velocity(in: _arView), worldDirection: worldDirection)
            }
            
        case .ended, .cancelled, .failed:
            // Let the dragged node handle the release
            if let draggedNode = findDraggedNode(at: location) {
              let worldDirection = screenDragToWorldDirection(calculateDragVector(gesture))
              draggedNode.endDrag(releaseVelocity: gesture.velocity(in: _arView), worldDirection: worldDirection)
            }
            
        default:
            break
        }
    }
    
    private func findDraggedNode(at location: CGPoint) -> ARNodeBase? {
        // Find the node currently being dragged
        for (node, _) in _nodeToAnchorDict {
            if node.isBeingDragged {
                return node
            }
        }
        return nil
    }
    
    private func calculateDragVector(_ gesture: UIPanGestureRecognizer) -> CGPoint {
        let currentLocation = gesture.location(in: _arView)
        let translation = gesture.translation(in: _arView)
        return CGPoint(x: translation.x, y: translation.y)
    }
  

  
  @available(iOS 14.0, *)
  private func isNodeAtLocation(_ location: CGPoint, in arView: ARView) -> Bool {
      // Use your proven findClosestNode approach
    if let closestNode = findClosestNode(tapLocation: location) {
      return true
    }
    return false
  }

  @objc fileprivate func handleLongPress(sender: UILongPressGestureRecognizer) {
    guard _sessionRunning else { return }
    
    if sender.state == .ended {
      let tapLocation = sender.location(in: _arView)
      var isNodeAtPoint = false
      
      // Check for node at press location
      if let entity = _arView.entity(at: tapLocation) as? ModelEntity,
         let node = findNodeForEntity(entity) {
        NodeLongClick(node)
        node.LongClick()
        isNodeAtPoint = true
      }
      
      // Check for detected plane at location
      if let result = _arView.raycast(from: tapLocation, allowing: .estimatedPlane, alignment: .any).first {
        // Check if this corresponds to a detected plane
        for (anchor, detectedPlane) in _detectedPlanesDict {
          if let planeAnchor = anchor as? ARPlaneAnchor {
            // Simple distance check to see if the tap is on this plane
            let planeCenter = planeAnchor.transform.translation
            let hitPoint = result.worldTransform.translation
            let distance = simd_distance(planeCenter, hitPoint)
            
            if distance < 0.5 { // 50cm threshold
              LongClickOnDetectedPlaneAt(detectedPlane, hitPoint.x, hitPoint.y, hitPoint.z, isNodeAtPoint)
              break
            }
          }
        }
        
        // Always dispatch the general long press event
        let position = result.worldTransform.translation
        LongPressAtPoint(
          UnitHelper.metersToCentimeters(position.x),
          UnitHelper.metersToCentimeters(position.y),
          UnitHelper.metersToCentimeters(position.z),
          isNodeAtPoint
        )
      }
    }
  }
  
  @objc fileprivate func handlePinch(sender: UIPinchGestureRecognizer) {
    guard _sessionRunning else { return }
    
    switch sender.state {
      case .changed:
          let tapLocation = sender.location(in: _arView)
          
          let nodeEntity = findClosestNode(tapLocation:  tapLocation )
              if nodeEntity != nil {
                print("Found closest node for scaling: \(nodeEntity!.Name) at distance:")
                nodeEntity!.scaleByPinch(scalar: Float(sender.scale))
              } else {
                  print("No nodes close to tap location")
              }
          sender.scale = 1
      default:
          break
      }
}
   
    
  @objc fileprivate func handleRotation(sender: UIRotationGestureRecognizer) {
      guard _sessionRunning else { return }
      
      switch sender.state {
      case .began:
          let tapLocation = sender.location(in: _arView)
          
          if let closestNode = findClosestNode(tapLocation: tapLocation) {
              trackingNode = closestNode
              // Store the starting rotation
              let euler = closestNode.quaternionToEulerAngles(closestNode._modelEntity.transform.rotation)
              _rotation = euler.y
              print("Started rotating node: \(closestNode.Name)")
          }
          
      case .changed:
          if let node = trackingNode {
              // Add the gesture rotation to the starting rotation
              let newRotation = _rotation + Float(sender.rotation)
              node.rotateByGesture(radians: newRotation)
          }
          
      case .ended, .cancelled, .failed:
          trackingNode = nil
          _rotation = 0.0
          
      default:
          break
      }
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
@available(iOS 14.0, *)
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


@available(iOS 14.0, *)
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
@available(iOS 14.0, *)
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
      // RealityKit doesn't have the same debug options for lights
      // You might need to implement custom visualization
      _showLightLocations = showLightLocations
    }
  }
  
  @objc open var ShowLightAreas: Bool {
    get {
      return _showLightAreas
    }
    set(showLightAreas) {
      // RealityKit doesn't have the same debug options for lights
      // You might need to implement custom visualization
      _showLightAreas = showLightAreas
    }
  }
  
  @objc open func HideAllLights() {
    for (anchorEntity, light) in _lights {
      anchorEntity.isEnabled = false
    }
  }
  
  public func addLight(_ light: ARLightBase) {
    // Create anchor for light
    let lightAnchor = AnchorEntity(world: SIMD3<Float>(0, 0, 0))
    
    // Add light entity to anchor (you'll need to implement light entity creation)
    // This depends on how you're handling lights in RealityKit
    
    _lights[lightAnchor] = light
    
    if _sessionRunning {
      _arView.scene.addAnchor(lightAnchor)
    } else {
      _requiresAddNodes = true
    }
  }
  
  public func removeLight(_ light: ARLightBase) {
    // Find and remove the light's anchor
    for (anchorEntity, lightBase) in _lights {
      if lightBase === light {
        _arView.scene.removeAnchor(anchorEntity)
        _lights.removeValue(forKey: anchorEntity)
        break
      }
    }
  }
  
  @objc open func LightingEstimateUpdated(_ ambientIntensity: Float, _ ambientTemperature: Float) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "LightingEstimateUpdated", arguments: ambientIntensity as NSNumber, ambientTemperature as NSNumber)
    }
  }
}

// MARK: LifeCycleDelegate
@available(iOS 14.0, *)
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
    _nodeToAnchorDict.keys.forEach {
      $0.stopFollowing()
      $0.removeFromAnchor()
    }
    
    _lights.keys.forEach {
      _arView.scene.removeAnchor($0)
    }
    
    _nodeToAnchorDict = [:]
    _lights = [:]
    _detectedPlanesDict = [:]
    _imageMarkers = [:]
  }
}

// MARK: - Extensions for compatibility

extension matrix_float4x4 {
  var translation: SIMD3<Float> {
    return SIMD3<Float>(columns.3.x, columns.3.y, columns.3.z)
  }
  
  var eulerAngles: SIMD3<Float> {
    // Extract Euler angles from rotation matrix
    let sy = sqrt(self[0][0] * self[0][0] + self[1][0] * self[1][0])
    let singular = sy < 1e-6
    
    let x: Float
    let y: Float
    let z: Float
    
    if !singular {
      x = atan2(self[2][1], self[2][2])
      y = atan2(-self[2][0], sy)
      z = atan2(self[1][0], self[0][0])
    } else {
      x = atan2(-self[1][2], self[1][1])
      y = atan2(-self[2][0], sy)
      z = 0
    }
    
    return SIMD3<Float>(x, y, z)
  }
}
