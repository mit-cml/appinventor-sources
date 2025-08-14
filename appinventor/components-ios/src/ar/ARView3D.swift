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
  
  public static var SHARED_GROUND_LEVEL: Float = -1.2
   
   // Update your existing GROUND_LEVEL to use the shared value
   public var GROUND_LEVEL: Float {
       get { return ARView3D.SHARED_GROUND_LEVEL }
       set { ARView3D.SHARED_GROUND_LEVEL = newValue }
   }
  
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
  
  private var _invisibleFloor: ModelEntity?
  private var _floorAnchor: AnchorEntity?
  private var _hasSetGroundLevel: Bool = false
  
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
  
  private var _currentDraggedObject: ARNodeBase? = nil
  
  struct CollisionGroups {
      static let arObjects: CollisionGroup = CollisionGroup(rawValue: 1 << 0)
      static let environment: CollisionGroup = CollisionGroup(rawValue: 1 << 1)
  }
  
  
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
    _arView.environment.sceneUnderstanding.options = [.occlusion]
    _arView.environment.sceneUnderstanding.options.insert(.physics)
    _arView.environment.sceneUnderstanding.options.insert(.collision)
    _arView.environment.sceneUnderstanding.options.remove(.occlusion)
    
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
    
    ensureFloorExists()
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
    
    
    setupCollisionDetection()
    
    if _sessionRunning {
      ResetTracking()
    }
  }
  
  // Add this method to ARView3D class
  private func setupCollisionGroups(for node: ARNodeBase) {
      guard let shapes = node._modelEntity.collision?.shapes else {
          print("⚠️ No collision shapes found for \(node.Name) - enable physics first")
          return
      }
      
      node._modelEntity.collision = CollisionComponent(
          shapes: shapes,
          filter: CollisionFilter(
              group: CollisionGroups.arObjects,
              mask: [CollisionGroups.arObjects, CollisionGroups.environment]
          )
      )
      
      print("✅ Collision groups set up for \(node.Name)")
  }
  
  
   @objc func removeInvisibleFloor() {
      if let floorAnchor = _floorAnchor {
          _arView.scene.removeAnchor(floorAnchor)
          _floorAnchor = nil
      }
      _invisibleFloor = nil
      print("🏠 Invisible floor removed")
  }
      
  @objc func adjustFloorHeight(_ newHeight: Float) {
      guard let floor = _invisibleFloor else {
          print("🏠 No invisible floor to adjust")
          return
      }
      
      floor.transform.translation.y = newHeight
      _floorAnchor?.transform.translation.y = newHeight
      print("🏠 Floor height adjusted to: \(newHeight)m")
  }
  
  @objc var hasInvisibleFloor: Bool {
      return _invisibleFloor != nil
  }
  
  @objc func ensureFloorExists() {
      if !hasInvisibleFloor {
          createInvisibleFloor()
      }
  }
  
  private func validateAllObjectPositions() {
      let groundLevel: Float = Float(GROUND_LEVEL)
      
      for (node, _) in _nodeToAnchorDict {
          let currentPos = node._modelEntity.transform.translation
          
          // Simple calculation: object bottom should be above ground
          let bounds = node._modelEntity.visualBounds(relativeTo: nil)
          let objectBottom = currentPos.y - (bounds.max.y - bounds.min.y) / 2.0 * node.Scale
          
          if objectBottom < groundLevel {
              // Move object up so its bottom is at ground level
              let correction = groundLevel - objectBottom + 0.01  // 2cm buffer
              let newPos = SIMD3<Float>(currentPos.x, currentPos.y + correction, currentPos.z)
              node._modelEntity.transform.translation = newPos
              print("🏠 Moved \(node.Name) above floor")
          }
      }
  }

  
  private func setupCollisionDetection() {
      // Cancel existing observer
      collisionBeganObserver?.cancel()
      
      // Simple collision observer - only gets AR object collisions due to collision groups
      collisionBeganObserver = _arView.scene.subscribe(
        to: CollisionEvents.Began.self,
        on: nil
      ) { [weak self] event in
        guard let self = self else { return }
        
        let entityA = event.entityA
        let entityB = event.entityB
        
        print("🔥 AR Object collision: \(entityA.name) ↔ \(entityB.name)")
        
        // Find the nodes and notify them of the collision
        if let nodeA = self.findNodeForEntity(entityA as? ModelEntity),
           let nodeB = self.findNodeForEntity(entityB as? ModelEntity) {
          
          // Both nodes handle the collision (color flash, behavior-specific effects, etc.)
          nodeA.ObjectCollidedWithObject(nodeB)
          nodeB.ObjectCollidedWithObject(nodeA)
          
          print("🔥 Notified nodes of collision: \(nodeA.Name) ↔ \(nodeB.Name)")
        } else {
          print("⚠️ Could not find nodes for collision entities")
      }
      }
      
      print("✅ Simplified collision observer set up")
      debugCollisionSetup()
    }
    
    private func debugCollisionSetup() {
      print("=== Collision Debug Info ===")
      print("Scene understanding options: \(_arView.environment.sceneUnderstanding.options)")
      print("Collision observer exists: \(collisionBeganObserver != nil)")
      print("Number of nodes with physics: \(_nodeToAnchorDict.keys.filter { $0._modelEntity.physicsBody != nil }.count)")
      print("Total nodes: \(_nodeToAnchorDict.count)")
    }
    
    // ✅ KEEP - Essential for connecting entities to nodes
    private func findNodeForEntity(_ entity: ModelEntity?) -> ARNodeBase? {
      guard let entity = entity else { return nil }
      
      for (node, anchor) in _nodeToAnchorDict {
        if node._modelEntity == entity || anchor.children.contains(entity) {
          return node
        }
      }
      return nil
    }
    
    // ✅ OPTIONAL: Keep these if you want to dispatch events to your app level
    @objc open func NodesCollided(_ nodeA: ARNodeBase, _ nodeB: ARNodeBase) {
      EventDispatcher.dispatchEvent(of: self, called: "NodesCollided",
                                    arguments: nodeA as AnyObject, nodeB as AnyObject)
    }
  // ✅ Add cleanup
  deinit {
      collisionBeganObserver?.cancel()
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
    
    ensureFloorExists()
    
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

              if !_hasSetGroundLevel &&
               planeAnchor.alignment == .horizontal &&
               planeAnchor.transform.translation.y < 0.1 {
                let detectedRealFloorLevel = planeAnchor.transform.translation.y
               if #available(iOS 16.0, *) {
                    let planeSize = planeAnchor.planeExtent.width * planeAnchor.planeExtent.height
                    
                    // Only use large, confident planes (at least 1 square meter)
                    if planeSize > 1.0 {
                      print("🏠 FIRST TIME: Setting ground level to detected floor: \(detectedRealFloorLevel)m")
                      let invisibleFloorLevel = detectedRealFloorLevel + 0.3
                      // ✅ Update ground level reference
                      ARView3D.SHARED_GROUND_LEVEL = invisibleFloorLevel
                      
                      // ✅ RECREATE invisible floor at correct position
                      createInvisibleFloor(at: invisibleFloorLevel)
                    }
                }
            }
          } else if let imageAnchor = anchor as? ARImageAnchor {
              guard let name = imageAnchor.referenceImage.name else { return }
              let imageMarker = _imageMarkers[name]
          } else if let geoAnchor = anchor as? ARGeoAnchor {
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
          } else if let geoAnchor = anchor as? ARGeoAnchor {
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
    
    private func ensureAboveFloor(_ position: SIMD3<Float>, for node: ARNodeBase) -> SIMD3<Float> {
        let groundLevel: Float = Float(GROUND_LEVEL)
        
        // Get object bounds to calculate its radius/height
        let bounds = node._modelEntity.visualBounds(relativeTo: nil)
        let objectRadius = max((bounds.max.y - bounds.min.y) / 2.0, 0.025) // Minimum 2.5cm radius
        
        // Calculate minimum Y position (object bottom should be at ground level)
        let minY = groundLevel + objectRadius + 0.05  // Extra 5cm buffer for safety
        
        let safeY = max(position.y, minY)
        
        if position.y != safeY {
            print("🏠 Adjusted object placement from Y=\(position.y) to Y=\(safeY) to stay above floor")
        }
        
        return SIMD3<Float>(position.x, safeY, position.z)
    }
    
    private func setupLocation(x: Float, y: Float, z: Float, latitude: Double, longitude: Double, altitude: Double, node: ARNodeBase, hasGeoCoordinates: Bool) {
        
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
                        // Close anchor: Use the EXACT tap position - let physics drop it
                        let xMeters: Float = UnitHelper.centimetersToMeters(x)
                        let yMeters: Float = UnitHelper.centimetersToMeters(y)
                        let zMeters: Float = UnitHelper.centimetersToMeters(z)
                        
             
                        let groundLevel = Float(GROUND_LEVEL)
                        let safeY = max(yMeters, groundLevel + 0.15) // At least 1cm above ground
     
                        node.setPosition(x: xMeters, y: safeY, z: zMeters)
                        
                        node._worldOffset = SIMD3<Float>(x: xMeters, y: yMeters, z: zMeters)
                        node._creatorSessionStart = anchorLocation
                        print("saved world coords for offset \(String(describing: node._worldOffset))")
                    }
                    return
                }
            } else {
                print("setting up location error: Invalid Coordinates", ErrorMessage.ERROR_INVALID_COORDINATES.code)
            }
        }
        
        // For non-geo coordinates, use exact position and let physics handle floor collision
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
      
      
      optimizeFloorSetup()
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)
      
      node.Initialize()  // order is important as we need to set geoanchor first b/c init overrides it - or fix that
      
      //which collisions to pay attention to
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
          self.setupCollisionGroups(for: node)
      }
      
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
          
        var SCREEN_THRESHOLD: CGFloat = 30.0
          // Skip if too far in screen space
          if screenDistance > SCREEN_THRESHOLD { continue }  // 100 pixel max
          
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
        
        
        var worldThreshold: Float = 0.1  // Default 10cm

        // Use actual sphere radius if it's a sphere
        if let sphereNode = node as? SphereNode {
          let sphereRadiusInCM = sphereNode.RadiusInCentimeters
          let sphereRadiusInMeters = sphereRadiusInCM / 100.0  // Simple conversion
        
            worldThreshold = sphereRadiusInMeters * 1.5  // 50% buffer around actual sphere
        }

        let normalizedWorldDistance = min(worldDistance / worldThreshold, 1.0)  // Normalize to 0-1
          
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

  
    func screenDragToWorldDirection(_ screenVector: CGPoint) -> SIMD3<Float> {
      let cameraTransform = _arView.cameraTransform
      
      let rightVector = simd_normalize(SIMD3<Float>(
          cameraTransform.matrix.columns.0.x,
          cameraTransform.matrix.columns.0.y,
          cameraTransform.matrix.columns.0.z
      ))
      
      // ✅ Choose forward vs up vector based on camera angle
      let cameraUpComponent = cameraTransform.matrix.columns.1.y  // How "up" is the camera?
      
      if abs(cameraUpComponent) > 0.7 {  // Camera mostly level
          // Use up vector for lifting
          let upVector = simd_normalize(SIMD3<Float>(
            cameraTransform.matrix.columns.1.x,
              cameraTransform.matrix.columns.1.y,
              cameraTransform.matrix.columns.1.z
          ))
          
          return rightVector * Float(screenVector.x) * 0.001 +
                 upVector * Float(-screenVector.y) * 0.001
          
      } else {  // Camera angled down (looking at floor)
          // Use forward vector for ground movement
          let forwardVector = simd_normalize(SIMD3<Float>(
              -cameraTransform.matrix.columns.2.x,
              0,  // Keep on ground plane
              -cameraTransform.matrix.columns.2.z
          ))
          
          return rightVector * Float(screenVector.x) * 0.001 +
                 forwardVector * Float(-screenVector.y) * 0.001
      }
      

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
    
    _detectedPlanesDict.removeAll()
    
    _imageMarkers.removeAll()
    
    _lights.keys.forEach {
      _arView.scene.removeAnchor($0)
    }
    
    _nodeToAnchorDict = [:]
    _lights = [:]
    _detectedPlanesDict = [:]
    _imageMarkers = [:]
    
    createInvisibleFloor()
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


@available(iOS 14.0, *)
extension ARView3D {
    
    // STEP 1: Replace your initializeGestureRecognizers method with this
    private func initializeGestureRecognizers() {
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap))
        _arView.addGestureRecognizer(tapGesture)
        
        // ✅ NEW: Enhanced pan gesture with camera-aware finger following + rolling + flicking
        if #available(iOS 16.0, *) {
            let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePanComplete))
            panGesture.maximumNumberOfTouches = 1
            _arView.addGestureRecognizer(panGesture)
        } else {
            let panGesture = UIPanGestureRecognizer(target: self, action: #selector(handlePanComplete))
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
    
  @objc private func handlePanComplete(_ gesture: UIPanGestureRecognizer) {
    guard let node = _currentDraggedObject ?? findClosestNode(tapLocation: gesture.location(in: _arView)) else { return }
      
      if gesture.state == .began {
          _currentDraggedObject = node
      }
      
      let fingerLocation = gesture.location(in: _arView)
      let fingerVelocity = gesture.velocity(in: _arView)
      let groundProjection = projectFingerToGround(fingerLocation: fingerLocation)
      let camera3DProjection = projectFingerWithCameraTransform(fingerLocation: fingerLocation, currentBall: node)
      
      node.handleAdvancedGestureUpdate(
          fingerLocation: fingerLocation,
          fingerVelocity: fingerVelocity,
          groundProjection: groundProjection,
          camera3DProjection: camera3DProjection,
          gesturePhase: gesture.state
      )
      
      if gesture.state == .ended || gesture.state == .cancelled {
        _currentDraggedObject = nil
      }
  }
    // STEP 3: Camera-aware positioning methods
    private func positionBallUnderFinger(fingerLocation: CGPoint, currentBall: ARNodeBase) -> SIMD3<Float>? {
        
      // Method 1: Try ARKit raycast first (most accurate)
      if let raycastResult = performCameraAwareRaycast(from: fingerLocation) {
          return constrainPositionForBall(raycastResult, ball: currentBall)
      }
      
      // Method 2: Fallback to camera-space projection
      return projectFingerWithCameraTransform(fingerLocation: fingerLocation, currentBall: currentBall)
    }
  
  private func constrainPositionForBall(_ position: SIMD3<Float>, ball: ARNodeBase) -> SIMD3<Float> {
    let groundLevel: Float = Float(GROUND_LEVEL)
    let ballRadius = (ball as? SphereNode)?.RadiusInCentimeters ?? 0.05
    let scaledRadius = ballRadius * ball.Scale
    let minY = groundLevel + scaledRadius + 0.02
    let maxPickupHeight = groundLevel + 3.0
      
    return SIMD3<Float>(
        position.x,
        max(minY, min(position.y, maxPickupHeight)),
        position.z
    )
  }
    
    private func performCameraAwareRaycast(from fingerLocation: CGPoint) -> SIMD3<Float>? {
      let raycastTargets: [ARRaycastQuery.Target] = [
          .existingPlaneGeometry,
          .existingPlaneInfinite,
          .estimatedPlane
      ]
      
      for target in raycastTargets {
          let results = _arView.raycast(from: fingerLocation, allowing: target, alignment: .any)
          if let result = results.first {
              let hitPoint = SIMD3<Float>(
                  result.worldTransform.columns.3.x,
                  result.worldTransform.columns.3.y,
                  result.worldTransform.columns.3.z
              )
              return hitPoint
          }
      }
      return nil
    }
    
    public func projectFingerWithCameraTransform(fingerLocation: CGPoint, currentBall: ARNodeBase) -> SIMD3<Float>? {
        let cameraTransform = _arView.cameraTransform
        let cameraPosition = SIMD3<Float>(cameraTransform.translation)
        let currentBallPosition = currentBall._modelEntity.transform.translation
        
        let ballDistance = simd_distance(cameraPosition, currentBallPosition)
        let targetDistance = max(ballDistance, 0.5)
        
        let normalizedX = (fingerLocation.x / _arView.bounds.width) * 2.0 - 1.0
        let normalizedY = -((fingerLocation.y / _arView.bounds.height) * 2.0 - 1.0)
        
        let fov = Float.pi / 3.0
        let aspectRatio = Float(_arView.bounds.width / _arView.bounds.height)
        
        let rayDirectionCamera = SIMD3<Float>(
            Float(normalizedX) * tan(fov / 2.0) * aspectRatio,
            Float(normalizedY) * tan(fov / 2.0),
            -1.0
        )
        
        let rayDirectionWorld = simd_normalize(cameraTransform.rotation.act(rayDirectionCamera))
        let worldPosition = cameraPosition + rayDirectionWorld * targetDistance
        
        return worldPosition
    }
    
    public func projectFingerToGround(fingerLocation: CGPoint) -> SIMD3<Float>? {
        let cameraTransform = _arView.cameraTransform
        let cameraPosition = SIMD3<Float>(cameraTransform.translation)
        
        let normalizedX = (fingerLocation.x / _arView.bounds.width) * 2.0 - 1.0
        let normalizedY = -((fingerLocation.y / _arView.bounds.height) * 2.0 - 1.0)
        
        let fov = Float.pi / 3.0
        let aspectRatio = Float(_arView.bounds.width / _arView.bounds.height)
        
        let rayDirectionCamera = SIMD3<Float>(
            Float(normalizedX) * tan(fov / 2.0) * aspectRatio,
            Float(normalizedY) * tan(fov / 2.0),
            -1.0
        )
        
        let rayDirectionWorld = cameraTransform.rotation.act(rayDirectionCamera)
        let groundLevel: Float = Float(GROUND_LEVEL)
        let rayToGround = groundLevel - cameraPosition.y
        
        guard abs(rayDirectionWorld.y) > 0.001 else { return nil }
        
        let t = rayToGround / rayDirectionWorld.y
        guard t > 0 else { return nil }
        
        let groundIntersection = cameraPosition + rayDirectionWorld * t
        return groundIntersection
    }
    



}


// MARK: - Enhanced Floor Management System for ARView3D
// Replace your existing floor methods with these enhanced versions

@available(iOS 14.0, *)
extension ARView3D {
    
  private func optimizeFloorSetup() {
      if let realFloorLevel = findMostAccurateFloorLevel() {
          let oldLevel = Float(GROUND_LEVEL)
          
          // Update references
          ARView3D.SHARED_GROUND_LEVEL = realFloorLevel
          
          // Move invisible floor
          _invisibleFloor?.transform.translation.y = realFloorLevel
          _floorAnchor?.transform.translation.y = realFloorLevel
          
          print("🏠 Floor reference updated: \(oldLevel) → \(realFloorLevel)")
          print("🏠 Objects will settle via physics")
          
          // NO automatic object repositioning
      }
  }
    
    // STEP 2: Find the most accurate floor level from multiple sources
    private func findMostAccurateFloorLevel() -> Float? {
        var floorCandidates: [(Float, String, Float)] = []  // (Y level, source, confidence)
        
        // Check detected planes (high confidence)
        for (anchor, _) in _detectedPlanesDict {
            if let planeAnchor = anchor as? ARPlaneAnchor,
               planeAnchor.alignment == .horizontal {
                let planeY = planeAnchor.transform.translation.y
              
              if #available(iOS 16.0, *) {
                let planeSize = planeAnchor.planeExtent.width * planeAnchor.planeExtent.height
                let confidence = min(planeSize * 10.0, 100.0)  // Larger planes = higher confidence
                
                if planeY < 0.1 { // Below camera level = likely floor
                    floorCandidates.append((planeY, "DetectedPlane", confidence))
                    print("📏 Detected plane floor candidate: Y=\(planeY), confidence=\(confidence)")
                }
              } else {
                // Fallback on earlier versions
              }
                
            }
        }
        
        // Check scene understanding meshes (medium confidence)
        for anchor in _arView.scene.anchors {
            for child in anchor.children {
                let entityType = String(describing: type(of: child))
                if entityType.contains("RKSceneUnderstanding") || entityType.contains("MeshEntity") {
                    let meshY = child.transform.translation.y
                    
                    if meshY < 0.1 { // Below camera = likely floor
                        floorCandidates.append((meshY, "SceneUnderstanding", 50.0))
                        print("📏 Scene understanding floor candidate: Y=\(meshY)")
                    }
                }
            }
        }
        
        // Return the most confident floor level
        if let bestCandidate = floorCandidates.max(by: { $0.2 < $1.2 }) {
            print("✅ Best floor candidate: Y=\(bestCandidate.0) from \(bestCandidate.1) (confidence: \(bestCandidate.2))")
            return bestCandidate.0
        }
        
        return nil
    }
  
  private func moveInvisibleFloorToRealFloor(realFloorLevel: Float) {
      let currentLevel = Float(GROUND_LEVEL)
      
      // ✅ ONLY CHANGE: Stop if floor hasn't moved
      if abs(realFloorLevel - currentLevel) < 0.05 { return }
      
      // Keep ALL your existing code exactly as it was
      guard let invisibleFloor = _invisibleFloor else {
          print("❌ No invisible floor to move")
          return
      }
      
      let oldLevel = invisibleFloor.transform.translation.y
      let newLevel = realFloorLevel
      
      ARView3D.SHARED_GROUND_LEVEL = newLevel
      invisibleFloor.transform.translation.y = newLevel
      _floorAnchor?.transform.translation.y = newLevel
      
      print("🔄 Moved invisible floor: \(oldLevel)m → \(newLevel)m")
      
      // Keep your original adjustAllObjectsToNewFloorLevel code
      adjustAllObjectsToNewFloorLevel(oldLevel: oldLevel, newLevel: newLevel)
  }
  

    private func adjustAllObjectsToNewFloorLevel(oldLevel: Float, newLevel: Float) {
        let levelChange = newLevel - oldLevel
        
        for (node, _) in _nodeToAnchorDict {
            let currentPos = node._modelEntity.transform.translation
            
            // Only adjust objects that were sitting on the old floor
            let bounds = node._modelEntity.visualBounds(relativeTo: nil)
            let objectBottom = currentPos.y - (bounds.max.y - bounds.min.y) / 2.0 * node.Scale
            
            // If object was sitting on old floor (within 10cm), move it to new floor
            if abs(objectBottom - oldLevel) < 0.1 {
                let newY = currentPos.y + levelChange
                node._modelEntity.transform.translation.y = newY
                print("📦 Adjusted \(node.Name): Y=\(currentPos.y) → Y=\(newY)")
            }
        }
    }
    
  @objc func createInvisibleFloor(at height: Float = SHARED_GROUND_LEVEL) {
      print("🏠 Creating simple invisible floor at: \(height)m")
      
      // Remove existing floor
      removeInvisibleFloor()
      
      // Create large invisible floor
      let floorSize: Float = 200.0  // 200m x 200m
      let floorMesh = MeshResource.generatePlane(width: floorSize, depth: floorSize)
      
      // Invisible material
      var invisibleMaterial = SimpleMaterial()
      if #available(iOS 15.0, *) {
          invisibleMaterial.color = .init(tint: .clear)
      }
      
      // Create floor entity
      _invisibleFloor = ModelEntity(mesh: floorMesh, materials: [invisibleMaterial])
      _invisibleFloor?.name = "InvisibleFloor"
      
      // Position at specified height
      _invisibleFloor?.transform.translation = SIMD3<Float>(0, height, 0)
      let floorThickness: Float = 0.1  // 10cm
      let floorShape = ShapeResource.generateBox(
          width: floorSize,
          height: floorThickness,  // Thicker
          depth: floorSize
      )
      
      // Position collision box so TOP surface is at the floor height
    let collisionY = height - (floorThickness/2) + 0.5
    print("Collision Y: \(collisionY)m")
    _invisibleFloor?.collision = CollisionComponent(
        shapes: [floorShape],
        filter: CollisionFilter(
          group: CollisionGroups.environment,  // ✅ "I am environment"
          mask: [CollisionGroups.arObjects]    // ✅ "I can hit AR objects"
        )
    )
      _invisibleFloor?.position.y = collisionY  // Lower the collision box
      
      // Static physics body
      _invisibleFloor?.physicsBody = PhysicsBodyComponent(
          massProperties: PhysicsMassProperties(mass: 1000.0),
          material: PhysicsMaterialResource.generate(
              staticFriction: 0.6,
              dynamicFriction: 0.4,
              restitution: 0.3
          ),
          mode: .static
      )
      
      // Create anchor and add to scene
      _floorAnchor = AnchorEntity(world: SIMD3<Float>(0, height, 0))
      _floorAnchor?.addChild(_invisibleFloor!)
      _arView.scene.addAnchor(_floorAnchor!)
      
    print("✅ Simple invisible floor created at: \(collisionY)m")
  }
    
    @objc func debugFloorSystem() {
        print("=== ENHANCED FLOOR SYSTEM DEBUG ===")
        print("Current GROUND_LEVEL: \(GROUND_LEVEL)")
        print("Has invisible floor: \(hasInvisibleFloor)")
        
        if let floor = _invisibleFloor {
            print("Invisible floor position: \(floor.transform.translation)")
            print("Invisible floor collision: \(floor.collision != nil)")
            print("Invisible floor physics: \(floor.physicsBody != nil)")
        }
        
        print("Detected planes: \(_detectedPlanesDict.count)")
        for (anchor, _) in _detectedPlanesDict {
            if let planeAnchor = anchor as? ARPlaneAnchor,
               planeAnchor.alignment == .horizontal {
                let planeY = planeAnchor.transform.translation.y
              if #available(iOS 16.0, *) {
                let size = planeAnchor.planeExtent.width * planeAnchor.planeExtent.height
                print("  Floor plane: Y=\(planeY), size=\(size)m²")
              }
                
            }
        }
        
        print("Object positions:")
        for (node, _) in _nodeToAnchorDict {
            if let sphere = node as? SphereNode {
                let pos = sphere._modelEntity.transform.translation
                let radius = sphere.RadiusInCentimeters * sphere.Scale
                let bottom = pos.y - radius
                print("  \(sphere.Name): center=Y\(pos.y), bottom=Y\(bottom)")
                
                if bottom < Float(GROUND_LEVEL) - 0.05 {
                    print("    ⚠️ This sphere might be below floor!")
                }
            }
        }
        print("================================")
    }
}


   
