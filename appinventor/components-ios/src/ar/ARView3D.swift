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

  public static var SHARED_GROUND_LEVEL: Float = -1.0
  public static var VERTICAL_OFFSET: Float = 0.02
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
  private var _enableOcclusion: Bool = false
  private var _showWorldOrigin: Bool = false
  private var _showFeaturePoints: Bool = false
  private var _showWireframes: Bool = false
  private var _showPhysics: Bool = false
  private var _showBoundingBoxes: Bool = false
  private var _showGeometry: Bool = false
  private var _showLightLocations: Bool = false
  private var _showLightAreas: Bool = false
  fileprivate var trackingNode: ARNode? = nil
  fileprivate var _sessionRunning: Bool = false
  fileprivate var _rotation: Float = 0.0
  fileprivate var _containsModelNodes: Bool = false
  fileprivate var _lightingEstimationEnabled: Bool = false
  
  private var _lastLightingUpdate: TimeInterval = 0
  private let _lightingUpdateInterval: TimeInterval = 0.1
  
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
  private var wallCollisionCache: [UUID: ModelEntity] = [:]
  private var lastWallCleanup: Date = Date()
  
  enum State {
    case none,
         colliding,
         released,
         idle
    
  }
  var currentState: State = .none

  private var _currentDraggedObject: ARNodeBase? = nil
  
  private var _placementIndicator: AnchorEntity?
  private var _lastPlacementPosition: SIMD3<Float>?

  
  struct CollisionGroups {
    static let arObjects: CollisionGroup = CollisionGroup(rawValue: 1 << 0)
    static let environment: CollisionGroup = CollisionGroup(rawValue: 1 << 1)
    static let manualWalls: CollisionGroup = CollisionGroup(rawValue: 1 << 2)
  }
  
  enum HitTestResult {
       case node(ARNodeBase, SIMD3<Float>)           // Node + world position
       case detectedPlane(DetectedPlane, SIMD3<Float>) // Detected plane + world position
       case invisibleFloor(SIMD3<Float>)             // Invisible floor + world position
       case empty(SIMD3<Float>)                                  // Nothing found
       
       var worldPosition: SIMD3<Float>? {
           switch self {
           case .node(_, let pos), .detectedPlane(_, let pos), .invisibleFloor(let pos):
               return pos
           case .empty:
               return nil
           }
       }
       
       var isNode: Bool {
           if case .node = self { return true }
           return false
       }
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
    _arView.environment.sceneUnderstanding.options = [.physics]
    _arView.translatesAutoresizingMaskIntoConstraints = false


    super.init(parent)
    _arView.session.delegate = self
    setupLocationManager()
    initializeGestureRecognizers()
    setupLifecycleObservers()
    

    _trackingSet = true
    _trackingType = .worldTracking
    _planeDetectionSet = true
    _planeDetection = .horizontal
    _lightingEstimationSet = true
    _lightingEstimationEnabled = false
    

    setupConfiguration()
    setupSceneUnderstanding()
    
    parent.add(self)
    Height = kARViewPreferredHeight
    Width = kARViewPreferredWidth
    
    _showWireframes = false
    _showWorldOrigin = false
    _showFeaturePoints = false
    
    ensureFloorExists()
    
    print("🏁 ARView3D initialization complete")
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
  
   open var CurrentAnchors: [ARNodeBase: AnchorEntity] {
    get {
      return _nodeToAnchorDict
    }
  }
  
  @objc open var CurrentConfig: ARConfiguration {
    get {
      return _configuration
    }
  }
  
  @objc open var IsSessionRunning: Bool {
    get {
      return _sessionRunning
    }
  }
  
  @objc open var InvisibleFloorIsNil: Bool {
    get {
      return _invisibleFloor == nil
    }
  }
  
  @objc open var EnableOcclusion: Bool {
    get {
      return _enableOcclusion
    }
    set(enableOcclusion) {
      if enableOcclusion {
          _arView.environment.sceneUnderstanding.options.insert(.occlusion)
      } else {
          _arView.environment.sceneUnderstanding.options.remove(.occlusion)
      }
    _enableOcclusion = enableOcclusion
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
        _arView.debugOptions.insert(.showSceneUnderstanding)
      } else {
        _arView.debugOptions.remove(.showSceneUnderstanding)
      }
      _showWireframes = showWireframes
    }
  }
  
  @objc open var ShowAnchorGeometry: Bool {
    get {
      return _showGeometry
    }
    set(showAnchorGeometry) {
      if showAnchorGeometry {
        _arView.debugOptions.insert(.showAnchorGeometry)
      } else {
        _arView.debugOptions.remove(.showAnchorGeometry)
      }
      _showGeometry = showAnchorGeometry
    }
  }
  
  @objc open var ShowBoundingBoxes: Bool {
    get {
      return _showBoundingBoxes
    }
    set(showBoundingBoxes) {
      if showBoundingBoxes {
        _arView.debugOptions.insert(.showPhysics) //showPhysics?
      } else {
        _arView.debugOptions.remove(.showPhysics)
      }
      _showBoundingBoxes = showBoundingBoxes
    }
  }
  
  @objc open var ShowPhysics: Bool {
    get {
      return _showPhysics
    }
    set(showPhysics) {
      if showPhysics {
        _arView.debugOptions.insert(.showPhysics)
      } else {
        _arView.debugOptions.remove(.showPhysics)
      }
      _showPhysics = showPhysics
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
  
  
  private func setupSceneUnderstanding() {
      print("🎯 Setting up scene understanding...")
      
      // Start with base options
      var options: ARView.Environment.SceneUnderstanding.Options = [.physics]
      
      // Add occlusion if enabled
      if _enableOcclusion {
          options.insert(.occlusion)
          print("  ✅ Occlusion enabled")
      }
      
      // Add collision if needed
      options.insert(.collision)
      
      _arView.environment.sceneUnderstanding.options = options
      
      print("  Final scene understanding options: \(options)")
      
      // Verify configuration has scene reconstruction
      if let config = _arView.session.configuration as? ARWorldTrackingConfiguration {
          print("  Scene reconstruction in config: \(String(describing: config.sceneReconstruction))")
      }
  }
  
  @available(iOS 14.0, *)
  private func setupConfiguration() {
    print("SETUP CONFIGURATION")
    guard _trackingSet && _planeDetectionSet else {
      //self._container?.form?.dispatchErrorOccurredEvent(self, "AR Tracking", ErrorMessage.ERROR_AR_TRACKING_NOT_SUPPORTED.code, "AR tracking not supported on this device")
      return }
    
   
    // Check geo tracking support only when needed
    if _trackingType == .geoTracking && !ARGeoTrackingConfiguration.isSupported {
      self._container?.form?.dispatchErrorOccurredEvent(self, "Geotracking", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code, "Geo tracking not supported on this device")
      return
    }
    
    if _trackingType == .worldTracking && !ARWorldTrackingConfiguration.isSupported {
      self._container?.form?.dispatchErrorOccurredEvent(self, "World tracking", ErrorMessage.ERROR_WORLD_TRACKING_NOT_SUPPORTED.code, "World tracking not supported on this device")
      return
    }
    
    switch _trackingType {
    case .worldTracking:
      let worldTrackingConfiguration = ARWorldTrackingConfiguration()
      
      // Check if scene reconstruction is supported on this device
      if ARWorldTrackingConfiguration.supportsSceneReconstruction(.mesh) {
        worldTrackingConfiguration.sceneReconstruction = .mesh
          print("CONFIG: Mesh reconstruction enabled")
      } else if ARWorldTrackingConfiguration.supportsSceneReconstruction(.meshWithClassification) {
          worldTrackingConfiguration.sceneReconstruction = .meshWithClassification
          print("CONFIG: Mesh with classification enabled")
      } else {
        print("CONFIG: Scene reconstruction not supported on this device")
        worldTrackingConfiguration.planeDetection = [.horizontal, .vertical]
      }
      
      if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
        worldTrackingConfiguration.frameSemantics.insert(.sceneDepth)
      }
     
      worldTrackingConfiguration.maximumNumberOfTrackedImages = 4
      worldTrackingConfiguration.detectionImages = getReferenceImages()

     // worldTrackingConfiguration.initialWorldMap = _initialWorldMap
      
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
      //ResetTracking()
    }
    
  
  }
  
  // MARK: - Debug Options Management

  public func reapplyDebugOptions() {
    print("🔧 Reapplying debug options...")
    print("🔧 Reapplying DEBUG OPTIONS...")
    print("🔧 Current flags - Wireframes: \(_showWireframes), Origin: \(_showWorldOrigin), Features: \(_showFeaturePoints)")
    
    // Clear all debug options first
    _arView.debugOptions = []
    
    for delay in [5.0, 15.0] {
      DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
        
        
        // Small delay to ensure session is fully started
        //DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
        
        var options: ARView.DebugOptions = []
       
        if self._showWireframes {
          options.insert(.showSceneUnderstanding)
          print("🔧 Adding .showSceneUnderstanding")

          print("🔧 Scene understanding toggled and re-enabled")
        }
        
        if self._showWorldOrigin {
          options.insert(.showWorldOrigin)
          print("🔧 Adding .showWorldOrigin")
        }
        
        if self._showFeaturePoints {
          options.insert(.showFeaturePoints)
          print("🔧 Adding .showFeaturePoints")
        }
        
        if self._showPhysics {
          options.insert(.showPhysics)
          print("🔧 Adding .showPhysics")
        }
        
        if self._showBoundingBoxes {
          options.insert(.showAnchorOrigins)
          print("🔧 Adding .showAnchorOrigins")
        }
        
        if self._showGeometry {
          options.insert(.showAnchorGeometry)
          print("🔧 Adding .showAnchorOrigins")
        }
        
        if self.ShowStatistics {
          options.insert(.showStatistics)
          print("🔧 Adding .showStatistics")
        }
        self.findCorrectDebugValues()
        self._arView.debugOptions = options
        
        print("🔧 Final debug options: \(self._arView.debugOptions)")
        print("🔧 Scene understanding enabled in config: \((self._configuration as? ARWorldTrackingConfiguration)?.sceneReconstruction != [])")
      }
    }
  }
  
  @objc func findCorrectDebugValues() {
      print("🔍 === DEBUG OPTION RAW VALUES ===")
      
      // Test all possible raw values
      for i in 0...256 {
        let option = ARView.DebugOptions(rawValue: Int(UInt(i)))
          if option.rawValue > 0 {
              var name = "Unknown"
              
              if option == .showPhysics { name = "showPhysics" }
              else if option == .showStatistics { name = "showStatistics" }
              else if option == .showSceneUnderstanding { name = "showSceneUnderstanding" }
              else if option == .showWorldOrigin { name = "showWorldOrigin" }
              else if option == .showFeaturePoints { name = "showFeaturePoints" }
              else if option == .showAnchorOrigins { name = "showAnchorOrigins" }
              else if option == .showAnchorGeometry { name = "showAnchorGeometry" }
              
              if name != "Unknown" {
                  print("🔍 \(name) = rawValue: \(option.rawValue)")
              }
          }
      }
      
      print("🔍 ================================")
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
              mask: [CollisionGroups.arObjects, CollisionGroups.manualWalls]
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
    _hasSetGroundLevel = false
    print("🏠 Invisible floor removed")
  }
  
  @objc var hasInvisibleFloor: Bool {
    return _invisibleFloor != nil
  }
  
  @objc func ensureFloorExists() {
    if !hasInvisibleFloor {
        createInvisibleFloor()
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
      
      // Find the nodes and notify them of the collision
      if let nodeA = self.findNodeForEntity(entityA as? ModelEntity),
         let nodeB = self.findNodeForEntity(entityB as? ModelEntity) {
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
            nodeA.ObjectCollidedWithObject(nodeB)
            nodeB.ObjectCollidedWithObject(nodeA)
            
            // Notify app level
            self?.NodesCollided(nodeA, nodeB)
        }
          
          
          
        print("🔥 Notified nodes of collision: \(nodeA.Name) ↔ \(nodeB.Name)")
      }
        
      if entityA.name == "InvisibleFloor" || entityB.name == "InvisibleFloor" {
          let nodeEntity = entityA.name == "InvisibleFloor" ? entityB : entityA
          print("💥 FLOOR COLLISION with \(nodeEntity.name) at \(nodeEntity.transform.translation)")
      }
    }
      
    //print("✅ Simplified collision observer set up")
    //debugCollisionSetup()
  }
    
  private func debugCollisionSetup() {
    print("=== CONFIG: Collision Debug Info ===")
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
    
   
  @objc open func NodesCollided(_ nodeA: ARNodeBase, _ nodeB: ARNodeBase) {
    EventDispatcher.dispatchEvent(of: self, called: "NodesCollided",
                                  arguments: nodeA as AnyObject, nodeB as AnyObject)
  }

  deinit {
    NotificationCenter.default.removeObserver(self)
    collisionBeganObserver?.cancel()
    _arView.session.pause()
    _arView.scene.anchors.removeAll()
    _arView.session.delegate = nil
  }


  private func getReferenceImages() -> Set<ARReferenceImage> {
    return Set(_imageMarkers.values.compactMap{ $0._referenceImage })
  }
  
  // MARK: Functions
  
  @objc open func StartTracking() {
      print("▶️ Starting AR tracking")
      startTrackingWithOptions(startOptions)
  }

  @objc open func startTrackingWithOptions(_ options: ARSession.RunOptions = []) {
    if _sessionRunning && options.isEmpty {
        print("⚠️ Session already running, skipping start")
        return
    }
    print("▶️ STARTTRACKING WITH OPTIONS: \(options)")
    startOptions = []
    _arView.session.run(_configuration, options: startOptions)
    _sessionRunning = true
     //, .resetSceneReconstruction]
    

    // ✅ Only recreate floor if anchors were removed
    if options.contains(.removeExistingAnchors) || _invisibleFloor == nil {
        ensureFloorExists()
    }
    
    updateGroundLevel(newGroundLevel: GROUND_LEVEL)
    
    
    // ✅ Re-enable WebViews if needed
    if _reenableWebViewNodes {
      for node in _nodeToAnchorDict.keys {
          if let webViewNode = node as? ARWebView {
              webViewNode.isUserInteractionEnabled = true
          }
      }
      _reenableWebViewNodes = false
    }
    
    if _requiresAddNodes {
      for (node, anchorEntity) in _nodeToAnchorDict {
        node.EnablePhysics(node.EnablePhysics)
        
        if node.IsGeoAnchored {
            if let geoAnchor = node.getGeoAnchor() {
                _arView.session.add(anchor: geoAnchor)
            }
        } else {
            if !node.IsFollowingImageMarker {
                _arView.scene.addAnchor(anchorEntity)
            }
            
            if node._fromPropertyPosition != nil {
              let position = node._fromPropertyPosition.split(separator: ",")
                .prefix(3)
                .map { Float(String($0)) ?? 0.0 }
              
              node._modelEntity.transform.translation = SIMD3<Float>(
                position[0],
                position[1],
                position[2]
              )
            }
            if !node._fromPropertyRotation.isEmpty {
              let eulerDegrees = node._fromPropertyRotation.split(separator: ",")
                  .prefix(3)
                  .map { Float(String($0)) ?? 0.0 }
              
              let xRadians = eulerDegrees[0] * .pi / 180.0
              let yRadians = eulerDegrees[1] * .pi / 180.0
              let zRadians = eulerDegrees[2] * .pi / 180.0
              
              node._modelEntity.transform.rotation = simd_quatf(
                  angle: yRadians, axis: [0, 1, 0]
              ) * simd_quatf(
                  angle: xRadians, axis: [1, 0, 0]
              ) * simd_quatf(
                  angle: zRadians, axis: [0, 0, 1]
              )
            }
          }
        }
          
        for (anchorEntity, light) in _lights {
            _arView.scene.addAnchor(anchorEntity)
        }
        
        _requiresAddNodes = false
      }
      
    // ✅ Reapply debug options
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
      self.setupSceneUnderstanding()
        self.reapplyDebugOptions()
    }
    
    print("▶️ AR session started successfully")
  }
  
  @objc open func PauseTracking() {
    print("⏸️ Pausing AR tracking")
    pauseTracking(true)
  }

  private func pauseTracking(_ disableWebViewInteraction: Bool = false) {

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
    print("⏸️ AR session paused")
  }
  
  @objc open func ResetTracking() {
    print("RESET TRACKING")
    
    let wasRunning = _sessionRunning
    
    // Stop session
    _arView.session.pause()
    _sessionRunning = false
    
    // Clear all AR state
    _hasSetGroundLevel = false
    removeInvisibleFloor()
    _detectedPlanesDict.removeAll()
    
    for (node, _) in _nodeToAnchorDict {
        node.EnablePhysics(false)
    }
    
    // Pause and prepare for reset
    pauseTracking(!wasRunning)
    setupConfiguration()
    startOptions = [] //.resetTracking] //, .resetSceneReconstruction]
    
    // Short delay to ensure clean reset

      
    if wasRunning {
      print("🔄 Restarting with clean slate...")
      self.startTrackingWithOptions(self.startOptions)
      
      // Re-enable physics after restart
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
          for (node, _) in self._nodeToAnchorDict {
              node.EnablePhysics(node.EnablePhysics)
          }
      }
    }
    
    print("🔄 Complete reset finished")
    
  }
  
  private func setupLifecycleObservers() {
    NotificationCenter.default.addObserver(
        self,
        selector: #selector(onAppDidBecomeActive),
        name: UIApplication.didBecomeActiveNotification,
        object: nil
    )
    print("🔔 Lifecycle observers registered")
  }


  @objc func onAppDidBecomeActive() {
      print("📱 ===== APP BECAME ACTIVE =====")
      
      // ✅ If we manually paused, restart now
      if !_sessionRunning {
          print("📱 Session not running, restarting...")
          startTrackingWithOptions([])
          return
      }
        
      // ✅ If session claims to be running but has no frame, force restart
      if _arView.session.currentFrame == nil {
          print("📱 Session running but no frame - force restarting...")
          _sessionRunning = false
          startTrackingWithOptions([])
      }
  }
  
  /*@objc open func ResetDetectedItems() {
    let _shouldRestartSession = _sessionRunning
    
    _hasSetGroundLevel = false
    removeInvisibleFloor()
    
    pauseTracking(!_shouldRestartSession)
    if _shouldRestartSession {
      startTrackingWithOptions([.removeExistingAnchors])
    } else if startOptions.isEmpty {
      startOptions = [.removeExistingAnchors]
    }
  }*/
  
  @objc func verifyFloorState() {
    print("🏠 Floor State Check:")
    print("  - Has floor: \(hasInvisibleFloor)")
    print("  - Ground level: \(GROUND_LEVEL)")
    print("  - Has set ground level: \(_hasSetGroundLevel)")
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
  
  private func CreateBoxNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let boxNode = BoxNode(self) as BoxNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      boxNode as BoxNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
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
  
  private func CreateVideoNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let videoNode = VideoNode(self) as VideoNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      videoNode as ARNodeBase, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
  }
  
  private func CreateWebViewNodeFromYail(_ yailNodeObj: YailDictionary) -> ARNodeBase? {
    let webviewNode = WebViewNode(self) as WebViewNode
    let yailNodeObj: YailDictionary = yailNodeObj
    let result = ARNodeUtilities.parseYailToNode(
      webviewNode as WebViewNode, yailNodeObj as YailDictionary, _arView.session, sessionStartLocation: sessionStartLocation
    )
    return result
  }
  
  
  func performHitTest(at screenPoint: CGPoint) -> HitTestResult {

    if let node = findClosestNode(tapLocation: screenPoint) {
      //print("hit a node \(node.Name) at \(screenPoint)")
      return .node(node, node._modelEntity.transform.translation)
    }
    let raycastResults = _arView.raycast(from: screenPoint, allowing: .existingPlaneGeometry, alignment: .any)
    if let bestResult = getHighestSurfaceRaycast(rayCastResults: raycastResults) {
      let worldPosition = SIMD3<Float>(
        bestResult.worldTransform.columns.3.x,
        bestResult.worldTransform.columns.3.y,
        bestResult.worldTransform.columns.3.z
    )
          
    if let planeAnchor = bestResult.anchor as? ARPlaneAnchor,
      let detected_Plane = _detectedPlanesDict[planeAnchor] {
        print("best detected plane at \(worldPosition)")
          return .detectedPlane(detected_Plane, worldPosition)
      }
      return .invisibleFloor(worldPosition)
    }
  
    let estimatedPlanes = _arView.raycast(from: screenPoint, allowing: .estimatedPlane, alignment: .any)
    if let result = estimatedPlanes.first {
      let worldPosition = SIMD3<Float>(
          result.worldTransform.columns.3.x,
          result.worldTransform.columns.3.y,
          result.worldTransform.columns.3.z
      )
      print("using estimated plane at \(worldPosition)")
      // CSB this should be a plane object.. why not detected plane?
      return .invisibleFloor(worldPosition)
    }
  
    return .empty(SIMD3<Float>(0, 0, 0))

  }
  
  private func getHighestSurfaceRaycast(rayCastResults: [ARRaycastResult]) -> ARRaycastResult? {
  
    // Sort by Y position (highest first) to prefer elevated surfaces
    let sortedResults = rayCastResults.sorted { result1, result2 in
        let y1 = result1.worldTransform.columns.3.y
        let y2 = result2.worldTransform.columns.3.y
        return y1 > y2 // Prefer higher surfaces
    }
    
    if let bestResult = sortedResults.first {
        return bestResult
    }
    
    return nil
  }
            
  @objc func handleTap(_ sender: UITapGestureRecognizer) {
    let tapLocation = sender.location(in: _arView)
    let hitResult = performHitTest(at: tapLocation)
    
    switch hitResult {
      case .node(let node, let position):
        NodeClick(node)
        node.Click()
        
        // Also dispatch tap at location with geo coordinates if available
        if let geoData = worldToGPS(position) {
            TapAtLocation(position.x, position.y, position.z,
                  geoData.coordinate.latitude, geoData.coordinate.longitude, geoData.altitude,
                  true, true) // isANodeAtPoint = true
        } else {
          TapAtLocation(position.x, position.y, position.z, 0.0, 0.0, 0.0, false, true)
          TapAtPoint(position.x, position.y, position.z, false)
        }
        
      case .detectedPlane(let plane, let position):
        // Handle detected plane tap
        ClickOnDetectedPlaneAt(plane, position.x, position.y, position.z, false)
        
        // Also dispatch general tap
        if let geoData = worldToGPS(position) {
            TapAtLocation(position.x, position.y, position.z,
                  geoData.coordinate.latitude, geoData.coordinate.longitude, geoData.altitude,
                  true, false)
        } else {
          TapAtLocation(position.x, position.y, position.z, 0.0, 0.0, 0.0, false, false)
          TapAtPoint(position.x, position.y, position.z, false)
        }
          
      case .invisibleFloor(let position):
        // Handle floor/surface tap
        if let geoData = worldToGPS(position) {
            TapAtLocation(position.x, position.y, position.z,
                         geoData.coordinate.latitude, geoData.coordinate.longitude, geoData.altitude,
                         true, false)
        } else {
          TapAtLocation(position.x, position.y, position.z, 0.0, 0.0, 0.0, false, false)
          TapAtPoint(position.x, position.y, position.z, false)
          //ClickOnDetectedPlaneAt(plane, position.x, position.y, position.z, false)
        }
          
      case .empty (let position):
        print("Tap hit nothing, but floating at position: \(position)")
        TapAtLocation(position.x, position.y, position.z, 0.0, 0.0, 0.0, false, false)
        TapAtPoint(position.x, position.y, position.z, false)
      }
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
    
    ensureFloorExists()
    if node.IsGeoAnchored {
      addGeoAnchoredNode(node)
    } else {
      var anchorEntity = node.Anchor
      if (anchorEntity == nil){
        anchorEntity = node.createAnchor()
      }
      let nodeId = ObjectIdentifier(node)
      print("adding non-geo node")
      _nodeToAnchorDict[node] = anchorEntity
      _containsModelNodes = node is ModelNode ? true : _containsModelNodes
      
      if _sessionRunning {
        _arView.scene.addAnchor(anchorEntity!)
        
        if !node._fromPropertyPosition.isEmpty {
          let position = node._fromPropertyPosition.split(separator: ",")
            .prefix(3)
            .map { Float(String($0)) ?? 0.0 }
          
          node._modelEntity.transform.translation = SIMD3<Float>(
            position[0],
            position[1],
            position[2]
          )
        }
        
        if !node._fromPropertyRotation.isEmpty {
          let eulerDegrees = node._fromPropertyRotation.split(separator: ",")
            .prefix(3)
            .map { Float(String($0)) ?? 0.0 }
          
          // Convert degrees to radians
          let xRadians = eulerDegrees[0] * .pi / 180.0
          let yRadians = eulerDegrees[1] * .pi / 180.0
          let zRadians = eulerDegrees[2] * .pi / 180.0
          
          // Create quaternion from Euler angles (ZYX order - standard)
          node._modelEntity.transform.rotation = simd_quatf(
            angle: yRadians, axis: [0, 1, 0]  // Y rotation (yaw)
          ) * simd_quatf(
            angle: xRadians, axis: [1, 0, 0]  // X rotation (pitch)
          ) * simd_quatf(
            angle: zRadians, axis: [0, 0, 1]  // Z rotation (roll)
          )
        }
        
        if (hasInvisibleFloor){
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
  
  func updateGroundLevel(newGroundLevel: Float) {
      
      // Check all existing nodes
    for node in _nodeToAnchorDict.keys {
      let currentY = node._modelEntity.transform.translation.y
      let bounds = node._modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.y - bounds.min.y)/2
      let bottomY = currentY - halfHeight
      
      if bottomY < newGroundLevel + 0.005 {
        let correctedY = newGroundLevel + halfHeight + 0.02
          node._modelEntity.transform.translation.y = correctedY
          print("⬆️ LIFTED \(node.Name) from \(currentY) to \(correctedY)")
      }
      }
  }
  
  
  // MARK: ARSession Delegate Methods
  public func session(_ session: ARSession, didAdd anchors: [ARAnchor]) {
    
    for anchor in anchors {
      if let planeAnchor = anchor as? ARPlaneAnchor {
        let detectedPlane = DetectedPlane(anchor: planeAnchor, container: self)
        _detectedPlanesDict[anchor] = detectedPlane
        PlaneDetected(detectedPlane)
      
        
      // for the floor
        if !_hasSetGroundLevel &&
         planeAnchor.alignment == .horizontal &&
            planeAnchor.transform.translation.y < 0.1 {
          let detectedRealFloorLevel = planeAnchor.transform.translation.y
          
          // optimize if we can, whether plane is large
          if #available(iOS 16.0, *) {
            let planeSize = planeAnchor.planeExtent.width * planeAnchor.planeExtent.height
            
            // Only use large, confident planes (at least 1 square meter)
            if planeAnchor.classification == .floor && planeSize > 1.0 {
              
              let invisibleFloorLevel = detectedRealFloorLevel + ARView3D.VERTICAL_OFFSET
              print("🏠 FIRST TIME: Setting ground level to detected floor: \(invisibleFloorLevel)")
              
              GROUND_LEVEL = invisibleFloorLevel
              
              // ✅ RECREATE invisible floor at correct position
              removeInvisibleFloor()
              createInvisibleFloor(at: invisibleFloorLevel)
              _hasSetGroundLevel = true
            }
          } else {
            let invisibleFloorLevel = detectedRealFloorLevel + ARView3D.VERTICAL_OFFSET
            print("🏠 FIRST TIME: Setting ground level to detected floor: \(invisibleFloorLevel)")
            GROUND_LEVEL = invisibleFloorLevel
            removeInvisibleFloor()
            createInvisibleFloor(at: invisibleFloorLevel)
            _hasSetGroundLevel = true
          }
        } else {
          if #available(iOS 16.0, *) {
            if planeAnchor.alignment == .vertical {
              //createOptimizedWallCollision(planeAnchor) //CSB keep ?
            }
          }
        }
      } else if let imageAnchor = anchor as? ARImageAnchor {
          guard let name = imageAnchor.referenceImage.name else { return }
          
      } else if let geoAnchor = anchor as? ARGeoAnchor {
          handleGeoAnchorAdded(geoAnchor)
      }
    }
  }

  //TODO csb not sure if this is truly necessary
  private func cleanupDistantWalls() {
      guard let camera = _arView.session.currentFrame?.camera else { return }
      let cameraPos = camera.transform.translation
      
      var wallsToRemove: [UUID] = []
      
      for (id, wallEntity) in wallCollisionCache {
          let wallPos = wallEntity.transform.translation
          let distance = simd_distance(cameraPos, wallPos)
          
          // Remove walls more than 10m away
          if distance > 10.0 {
              wallsToRemove.append(id)
              wallEntity.parent?.removeFromParent()
          }
      }
      
      for id in wallsToRemove {
          wallCollisionCache.removeValue(forKey: id)
      }
      
      lastWallCleanup = Date()
      print("Cleaned up \(wallsToRemove.count) distant walls")
  }

  //TODO csb not sure if this is truly necessary
  @available(iOS 16.0, *)
  private func createOptimizedWallCollision(_ planeAnchor: ARPlaneAnchor) {
      // Clean up old walls periodically
      if Date().timeIntervalSince(lastWallCleanup) > 5.0 {
          cleanupDistantWalls()
      }
      
      // Check if we already have a wall for this area
      if wallCollisionCache[planeAnchor.identifier] != nil {
          return // Already exists
      }
      
      // Only create walls for significant surfaces
      let wallArea = planeAnchor.planeExtent.width * planeAnchor.planeExtent.height
      guard wallArea > 1.0 else { return }
      
      // Use minimal collision geometry - just the essential boundary
      let boundaryThickness: Float = 0.02 // Very thin
      let wallShape = ShapeResource.generateBox(
          width: planeAnchor.planeExtent.width,
          height: max(planeAnchor.planeExtent.height, 2.0),
          depth: boundaryThickness
      )
      
      let wallEntity = ModelEntity()
      wallEntity.name = "OptimizedWall_\(planeAnchor.identifier)"
      
      // Optimized collision with perfect physics properties
      wallEntity.collision = CollisionComponent(
          shapes: [wallShape],
          filter: CollisionFilter(
              group: CollisionGroups.manualWalls,
              mask: [CollisionGroups.arObjects]
          )
      )
      
      // Perfect wall physics - no energy loss, predictable bounces
      wallEntity.physicsBody = PhysicsBodyComponent(
          massProperties: PhysicsMassProperties(mass: 1000.0),
          material: PhysicsMaterialResource.generate(
              staticFriction: 0.0,      // No friction for clean bounces
              dynamicFriction: 0.0,
              restitution: 0.95         // Near-perfect bounce
          ),
          mode: .static
      )
      
      wallEntity.transform.translation = planeAnchor.transform.translation
      wallEntity.transform.rotation = simd_quatf(planeAnchor.transform)
      
      let anchor = AnchorEntity(world: planeAnchor.transform)
      anchor.addChild(wallEntity)
      _arView.scene.addAnchor(anchor)
      
      wallCollisionCache[planeAnchor.identifier] = wallEntity
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

      autoreleasepool {
          // Extract data immediately - don't store frame reference
          let timestamp = frame.timestamp
          
          // Handle lighting estimation
          if _lightingEstimationEnabled {
              guard timestamp - _lastLightingUpdate >= _lightingUpdateInterval else { return }
              _lastLightingUpdate = timestamp
              
              if let lightingEstimate = frame.lightEstimate {
                  let ambientIntensity = Float(lightingEstimate.ambientIntensity)
                  let ambientColorTemperature = Float(lightingEstimate.ambientColorTemperature)
                  
                  // ✅ Don't capture frame in async block
                  DispatchQueue.main.async { [weak self] in
                      self?.LightingEstimateUpdated(ambientIntensity, ambientColorTemperature)
                  }
              }
          }
          
          // Frame is released here when autoreleasepool exits
      }
  }
  
  public func session(_ session: ARSession, didFailWithError error: Error) {
       print("AR Session failed: \(error.localizedDescription)")
       
       DispatchQueue.main.async {
         print("AR Session failed. Restarting...")
         self.clearView()
       }
   }
  
  //ARSessionObserver
  public func sessionWasInterrupted(_ session: ARSession) {
      print("⚠️ ===== AR SESSION INTERRUPTED =====")
      print("⚠️ Reason: phone call, backgrounding, control center, etc.")
      _sessionRunning = false
      // Don't call pause() here - ARKit already paused it
  }
      
  public func sessionInterruptionEnded(_ session: ARSession) {
      print("✅ ===== AR SESSION INTERRUPTION ENDED =====")
      
      // ✅ Don't restart the session - ARKit resumes automatically
      // Just mark that we're running again
      _sessionRunning = true
      
      // ✅ Optionally verify the session has our configuration
      if _arView.session.configuration == nil {
          print("⚠️ Configuration lost, reapplying...")
          _arView.session.run(_configuration, options: [])
      }
      
      ensureFloorExists()
      
      if _reenableWebViewNodes {
          for node in _nodeToAnchorDict.keys {
              if let webViewNode = node as? ARWebView {
                  webViewNode.isUserInteractionEnabled = true
              }
          }
          _reenableWebViewNodes = false
      }
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
          self.reapplyDebugOptions()
      }
      print("✅ Camera resumed successfully!")
  }
  
  
  private func handleGeoAnchorAdded(_ geoAnchor: ARGeoAnchor) {
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
      node.Name = "PlaneNode"
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
      node.Name = "CapsuleNode"
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      
      node.setPosition(x: xMeters, y: yMeters, z: zMeters)
      return node
    }
    
    
    private func setupLocation(x: Float, y: Float, z: Float, latitude: Double, longitude: Double, altitude: Double, node: ARNodeBase, hasGeoCoordinates: Bool) {
      print("SETUP LOCATION")
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
              
              
              let groundLevel = GROUND_LEVEL
              let bounds = node._modelEntity.visualBounds(relativeTo: nil as Entity?)
              let halfHeight = (bounds.max.y - bounds.min.y)/2

              let safeY = max(
                  yMeters + halfHeight + 0.001,  // detected surface + radius + clearance
                  groundLevel + ARView3D.VERTICAL_OFFSET + halfHeight  // fallback floor position
              )
              
              //node.setPosition(x: xMeters, y: safeY, z: zMeters)
              node._modelEntity.setPosition(SIMD3<Float>(xMeters, safeY, zMeters), relativeTo: nil)
              print("create node at y  \(yMeters) and safe is \(safeY)")
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
      
      let groundLevel = GROUND_LEVEL
      let safeY = max(yMeters, groundLevel + ARView3D.VERTICAL_OFFSET) // At least 1cm above ground
      
      node.setPosition(x: xMeters, y: safeY, z: zMeters)
      print("set up anchor and setting position \(xMeters) \(yMeters) \(zMeters)")
    }
    
    @objc open func CreateBoxNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> BoxNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateBoxNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = BoxNode(self)
      node.Name = "GeoBoxNode"
      node.Initialize()
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)

      return node
    }
    
    @objc open func CreateCapsuleNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> CapsuleNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateCapsuleNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = CapsuleNode(self)
      node.Name = "GeoCapsuleNode"
      node.Initialize()
      
      setupLocation(x: x, y: y, z: z, latitude: lat, longitude: lng, altitude: altitude, node: node, hasGeoCoordinates: hasGeoCoordinates)

      return node
    }
    

    @objc open func CreateModelNode(_ x: Float, _ y: Float, _ z: Float, _ modelObjString: String) -> ModelNode {
      let node:ModelNode = ModelNode(self)
      node.Name = "ModelNode"
      node.Initialize()
      node.Model = modelObjString
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
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
    
    @objc open func CreateVideoNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> VideoNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreateVideoNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = VideoNode(self)
      node.Name = "GeoVideoNode"
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
      node.Name = "CreatedWebNode"
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
      print("LOADING stored scene \(dictionaries)")
      
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
          case "box", "geoboxnode":
            loadNode = self.CreateBoxNodeFromYail(nodeDict)
          case "sphere", "geospherenode":
            loadNode = self.CreateSphereNodeFromYail(nodeDict)
          case "model", "geomodelnode":
            loadNode = self.CreateModelNodeFromYail(nodeDict)
          case "text", "geotextnode":
            loadNode = self.CreateTextNodeFromYail(nodeDict)
          case "video", "geovideonode":
            loadNode = self.CreateVideoNodeFromYail(nodeDict)
          case "webview", "geowebviewnode":
            loadNode = self.CreateWebViewNodeFromYail(nodeDict)
          default:
            // currently not storing or handling modelNode..
            loadNode = nil
          }
          
          if let node = loadNode {
            addNode(node)
            newNodes.append(node)
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
      // Save world map
      _arView.session.getCurrentWorldMap { worldMap, error in
        /*  guard let worldMap = worldMap else {
              print("Error getting world map: \(error?.localizedDescription ?? "unknown")")
              return
          }
          
          // Save worldMap to disk
          do {
              let data = try NSKeyedArchiver.archivedData(
                  withRootObject: worldMap,
                  requiringSecureCoding: true
              )
              try data.write(to: worldMapURL)
          } catch {
              print("Error saving world map: \(error)")
          }*/
      }
      return dictionaries
    }
    
    
    
    public func worldToGPS(_ worldPoint: SIMD3<Float>) -> (coordinate: CLLocationCoordinate2D, altitude: Double)? {
      guard let sessionStart = sessionStartLocation else { return nil }
      
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "worldToGPS", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
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
          if nodeScreenPos == nil { continue }
          let screenDistance = sqrt(
            pow(tapLocation.x - nodeScreenPos!.x, 2) +
            pow(tapLocation.y - nodeScreenPos!.y, 2)
          )
          
        let SCREEN_THRESHOLD: CGFloat = 50.0
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

        let bounds = node._modelEntity.visualBounds(relativeTo: nil as Entity?)
        let currentSize = bounds.max - bounds.min
        worldThreshold = currentSize.max()

        let normalizedWorldDistance = min(worldDistance / worldThreshold, 1.0)  // Normalize to 0-1
          
        let combinedScore = (screenWeight * Float(normalizedScreenDistance)) +
                             (worldWeight * normalizedWorldDistance)
          
        if combinedScore < bestScore {
            bestScore = combinedScore
            bestNode = node
        }
      }
      
      if let node = bestNode {
          //print("Selected node \(node.Name) with combined score: \(bestScore)")
          return node
      }
      
      return nil
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
    return position.y > GROUND_LEVEL
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
      print("Ended rotating node: \(_rotation)")
      _rotation = 0.0
      
    default:
      break
    }
  }
  
  
  public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer,
                                shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
    
    // ✅ Allow pinch and rotation to work together
    let isPinchAndRotation = (gestureRecognizer is UIPinchGestureRecognizer && otherGestureRecognizer is UIRotationGestureRecognizer) ||
    (gestureRecognizer is UIRotationGestureRecognizer && otherGestureRecognizer is UIPinchGestureRecognizer)
    
    if isPinchAndRotation {
      return true
    }
    
    // ✅ Don't allow pan with pinch/rotation to avoid conflicts
    let isPanWithOther = (gestureRecognizer is UIPanGestureRecognizer &&
                          (otherGestureRecognizer is UIPinchGestureRecognizer || otherGestureRecognizer is UIRotationGestureRecognizer)) ||
    ((gestureRecognizer is UIPinchGestureRecognizer || gestureRecognizer is UIRotationGestureRecognizer) &&
     otherGestureRecognizer is UIPanGestureRecognizer)
    
    return !isPanWithOther
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
  @objc public func onPause() {
    if _sessionRunning {
      PauseTracking()
    }
  }
  
  @objc public func onResume() {
    print("RESUMING view")
    ResetTracking()
  }
  
  @objc public func onDelete() {
    print("DELETEING/REFRESHING view")
    clearView()
    DispatchQueue.main.asyncAfter(deadline: .now() + 3.5) {
            print("CLEARING view in onDelete")
            
        }
  }
  
  @objc public func onDestroy() {
    clearView()
  }
  
  private func clearView() {
    print("CLEARING view")
    _nodeToAnchorDict.keys.forEach {
      $0.stopFollowing()
      $0.removeFromAnchor()
    }
    
    _arView.session.pause()
    _sessionRunning = false
    
    // ✅ Force release any retained frames
    autoreleasepool {
        // This ensures any frames in the current pool are released
        _ = _arView.session.currentFrame  // Access and release
    }
    
    // ✅ Remove all anchors
    _arView.scene.anchors.removeAll()
    
    // ✅ Clear the session completely
    _arView.session.delegate = nil
    _detectedPlanesDict.removeAll()


    _imageMarkers.removeAll()
    
    _lights.keys.forEach {
      _arView.scene.removeAnchor($0)
    }
    
    _nodeToAnchorDict = [:]
    _lights = [:]
    _detectedPlanesDict = [:]
    _imageMarkers = [:]
    
    _arView.session.delegate = nil
    locationManager!.delegate = nil
    _planeDetection = .none
    
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
  
  
  public func addPlacementIndicator(_ indicator: AnchorEntity) {
    _arView.scene.addAnchor(indicator)
  }
  
  public func removePlacementIndicator(_ indicator: AnchorEntity) {
    _arView.scene.removeAnchor(indicator)
  }
  
  // MARK: - Preview Surface Handling (called from ARView3D during drag)
  
  
  
  /// Hides the placement preview indicator
  public func hidePlacementPreview() {
    if let indicator = _placementIndicator {
      _lastPlacementPosition = _placementIndicator?.position
      removePlacementIndicator(indicator)
      _placementIndicator = nil
    }
  }
  
  private func getCurrentCameraVectors() -> CameraVectors? {
      autoreleasepool {
          guard let frame = _arView.session.currentFrame else { return nil }
          
          let transform = frame.camera.transform
          
          let right = simd_normalize(SIMD3<Float>(
              transform.columns.0.x, 0, transform.columns.0.z
          ))
          
          let forward = simd_normalize(SIMD3<Float>(
              -transform.columns.2.x, 0, -transform.columns.2.z
          ))
          
          return CameraVectors(right: right, forward: forward)
      }
  }
  
  // Define a simple struct to hold camera vectors
  struct CameraVectors {
    let right: SIMD3<Float>
    let forward: SIMD3<Float>
  }
  
  
  @objc private func handlePanComplete(_ gesture: UIPanGestureRecognizer) {
      let targetNode: ARNodeBase?
      let hitResult: HitTestResult = performHitTest(at: gesture.location(in: _arView))
      if gesture.state == .began {
         
        if case .node(let node, _) = hitResult {
            _currentDraggedObject = node
            node.isBeingDragged = true
            targetNode = node
            print("BEGAN dragging: \(node.Name)")
        } else {
            targetNode = nil
        }
          
      } else {
          targetNode = _currentDraggedObject
      }
      
      guard let node = targetNode, node.moveByPan != nil else { return }
      
      let fingerLocation = gesture.location(in: _arView)
      let fingerMovement = gesture.translation(in: _arView)
      let fingerVelocity = gesture.velocity(in: _arView)
      
      let groundProjection = getProjectionForNode(
          node: node,
          fingerLocation: fingerLocation,
          fingerMovement: fingerMovement,
          gesturePhase: gesture.state,
          hitResult: hitResult
      )
      
      let cameraVectors = getCurrentCameraVectors()
      
      node.handleAdvancedGestureUpdate(
          fingerLocation: fingerLocation,
          fingerMovement: fingerMovement,
          fingerVelocity: fingerVelocity,
          groundProjection: groundProjection,
          camera3DProjection: cameraVectors,
          gesturePhase: gesture.state
      )
      
      if gesture.state == .changed {
          gesture.setTranslation(.zero, in: _arView)
      }
      
      if gesture.state == .ended || gesture.state == .cancelled {
          _currentDraggedObject?.isBeingDragged = false
          _currentDraggedObject = nil
          hidePlacementPreview()
      }
  }
 
  func getProjectionForNode(node: ARNodeBase, fingerLocation: CGPoint, fingerMovement: CGPoint, gesturePhase: UIGestureRecognizer.State, hitResult: HitTestResult) -> SIMD3<Float>? {
    
    if node is SphereNode {
      // Spheres use ground projection for rolling behavior
      return projectFingerToGround(fingerLocation: fingerLocation, fingerMovement: fingerMovement)
      
    } else { //if let modelNode = node as? ModelNode {
      return projectFingerTo3DSpace(fingerLocation: fingerLocation, fingerMovement: fingerMovement, hitResult: hitResult)
    }
  }
  
  
  
  // ✅ NEW: Project finger to smooth 3D space for Pokemon GO style dragging
    private func projectFingerTo3DSpace(fingerLocation: CGPoint, fingerMovement: CGPoint, hitResult: HitTestResult) -> SIMD3<Float>? {
    guard let draggedNode = _currentDraggedObject else { return nil }
    
    // Get current position before moving
    let currentPos = draggedNode._modelEntity.transform.translation
    
    // Use camera-compensated incremental movement for smooth dragging
    let newPosition = projectFingerIncrementally(currentPos, fingerMovement: fingerMovement)
    
    // ✅ Allow Y movement but with constraints for better UX
    let constrainedY = max(newPosition.y, Float(GROUND_LEVEL) + 0.5) // Stay above ground
    
    checkAndUpdateSurfacePreview(for: draggedNode, at: newPosition, hitResult: hitResult)
    
    return SIMD3<Float>(
      newPosition.x,
      constrainedY,
      newPosition.z
    )
  }
  
  func getProjectionForNode(node: ARNodeBase, fingerLocation: CGPoint, fingerMovement: CGPoint, gesturePhase: UIGestureRecognizer.State) -> SIMD3<Float>? {
          
    if node is SphereNode {
        return projectFingerToGround(fingerLocation: fingerLocation, fingerMovement: fingerMovement)
    } else {
        let currentPos = node._modelEntity.transform.translation
        let projectedPos = projectFingerIncrementally(currentPos, fingerMovement: fingerMovement)
        
        // During drag, check surfaces using both projected position and hit result
        if gesturePhase == .changed {
            let hitResult = performHitTest(at: fingerLocation)
            checkAndUpdateSurfacePreview(for: node, at: projectedPos, hitResult: hitResult)
        }
        
        return projectedPos
    }
  }
  
  
  private func projectFingerIncrementally(_ currentPos: SIMD3<Float>, fingerMovement: CGPoint) -> SIMD3<Float> {
      guard let frame = _arView.session.currentFrame else {
          let scale: Float = 0.002
          return SIMD3<Float>(
              currentPos.x + Float(fingerMovement.x) * scale,
              currentPos.y,
              currentPos.z + Float(fingerMovement.y) * scale
          )
      }
      
      return autoreleasepool {
          let cameraTransform = frame.camera.transform
          let cameraForward = -SIMD3<Float>(
              cameraTransform.columns.2.x,
              0,
              cameraTransform.columns.2.z
          )
          let cameraYaw = atan2(cameraForward.x, cameraForward.z)
          
          let cameraPosition = SIMD3<Float>(
              cameraTransform.columns.3.x,
              cameraTransform.columns.3.y,
              cameraTransform.columns.3.z
          )
          let distance = simd_distance(currentPos, cameraPosition)
          let scale: Float = 0.004 * max(distance * 0.5, 0.5)
          
          let fingerX = -Float(fingerMovement.x) * scale
          let fingerZ = -Float(fingerMovement.y) * scale
          
          let rotatedX = fingerX * cos(-cameraYaw) - fingerZ * sin(-cameraYaw)
          let rotatedZ = fingerX * sin(-cameraYaw) + fingerZ * cos(-cameraYaw)
          
          return SIMD3<Float>(
              currentPos.x + rotatedX,
              currentPos.y,
              currentPos.z + rotatedZ
          )
      }
  }
  
  // ✅ Keep existing methods but with improvements
  
  private func projectFingerRaycast(fingerLocation: CGPoint, fingerMovement: CGPoint) -> SIMD3<Float>? {
    guard let draggedNode = _currentDraggedObject else { return nil }
    
    // ✅ Try raycast first, but don't require it for smooth interaction
    let raycastResults = _arView.raycast(from: fingerLocation, allowing: .estimatedPlane, alignment: .horizontal)
    
    if let raycastResult = raycastResults.first {
      let hitPoint = SIMD3<Float>(
        raycastResult.worldTransform.columns.3.x,
        raycastResult.worldTransform.columns.3.y + 0.01, // Slight offset above surface
        raycastResult.worldTransform.columns.3.z
      )
      
      print("🎯 Raycast hit plane at: \(hitPoint)")
      return hitPoint
      
    } else {
      // ✅ Always fallback to smooth incremental movement
      let currentPos = draggedNode._modelEntity.transform.translation
      return projectFingerIncrementally(currentPos, fingerMovement: fingerMovement)
    }
  }
  
  func projectFingerToGround(fingerLocation: CGPoint, fingerMovement: CGPoint) -> SIMD3<Float>? {
    guard let draggedNode = _currentDraggedObject else { return nil }
    let currentPos = draggedNode._modelEntity.transform.translation
    
    // ✅ Enhanced ground projection with Y constraint
    var newPosition = projectFingerIncrementally(currentPos, fingerMovement: fingerMovement)
    
    // Keep on or slightly above ground level
    newPosition.y = max(newPosition.y, Float(GROUND_LEVEL))
    
    return newPosition
  }
  
  func checkAndUpdateSurfacePreview(for node: ARNodeBase, at dragPosition: SIMD3<Float>, hitResult: HitTestResult) -> SIMD3<Float>? {
      
    switch hitResult {
      case .node(let targetNode, let hitPosition):
       
        guard targetNode != node else { return nil }
        
        print("🔍 Calculating surface for node: \(targetNode.Name)")
        let topSurface = calculateNodeTopSurface(targetNode)
        print("🔍 Top surface result: \(topSurface)")
        
        let dragBoxBounds = node._modelEntity.visualBounds(relativeTo: nil)
        let dragBoxHalfHeight = (dragBoxBounds.max.y - dragBoxBounds.min.y) / 2
        
        // Position the new box's CENTER at: top of target + half-height of new box
        let stackPosition = SIMD3<Float>(
            hitPosition.x,
            topSurface.y + dragBoxHalfHeight + 0.02,
            hitPosition.z
        )
        if stackPosition.y.isFinite {
            showPlacementPreview(at: stackPosition, isStacking: true)
            return stackPosition
        } else {
            print("🚨 Invalid surface Y calculated, using fallback")
            let fallback = SIMD3<Float>(hitPosition.x, Float(GROUND_LEVEL) + 0.01, hitPosition.z)
            showPlacementPreview(at: fallback, isStacking: false)
            return fallback
        }
          
      case .detectedPlane(_, let hitPosition):
      
        let safeY = max(hitPosition.y + 0.01,Float(GROUND_LEVEL) + 0.01)
        let surface = SIMD3<Float>(hitPosition.x, safeY, hitPosition.z)
        showPlacementPreview(at: surface, isStacking: false)
        return surface
          
      case .invisibleFloor(let hitPosition):
        let surface = SIMD3<Float>(hitPosition.x, Float(GROUND_LEVEL) + 0.01, hitPosition.z)
        showPlacementPreview(at: surface, isStacking: false)
        return surface
          
      case .empty:
        hidePlacementPreview()
        return nil
      }
  }
  
  func findBestSurfaceForPlacement() -> SIMD3<Float>? {
    print("📍 Placement indicator for dragged node \(String(describing: _lastPlacementPosition))")
      return _lastPlacementPosition
  }
  
  private func isStackingSurface(_ surface: SIMD3<Float>, dragPosition: SIMD3<Float>) -> Bool {
      let floorLevel = Float(GROUND_LEVEL)
      return surface.y > floorLevel + 0.1
  }
      
  private func calculateNodeTopSurface(_ node: ARNodeBase) -> SIMD3<Float> {
      let nodePos = node._modelEntity.transform.translation
      let bounds = node._modelEntity.visualBounds(relativeTo: nil)
      
      print("Node \(node.Name):")
      print("  Position: \(nodePos)")
      print("  Bounds min: \(bounds.min)")
      print("  Bounds max: \(bounds.max)")
      
      let topY = max(bounds.max.y, bounds.min.y) + 0.001
      let safeY = max(topY, ARView3D.SHARED_GROUND_LEVEL + 0.01)
  
      print("  Calculated top: \(topY)")
      
      return SIMD3<Float>(nodePos.x, safeY, nodePos.z)
  }
  
  private func showPlacementPreview(at position: SIMD3<Float>, isStacking: Bool) {
    guard #available(iOS 15.0, *) else { return }
    
    hidePlacementPreview()
    
    // Create indicator entity
    let indicatorEntity = ModelEntity()
    let geometry = MeshResource.generatePlane(width: 0.15, depth: 0.15)
    var material = SimpleMaterial()
    
    // Different visual feedback for different surface types
    if isStacking {
      // Stacking on another node - blue indicator
      material.color = .init(tint: .blue.withAlphaComponent(0.7))
      material.baseColor = MaterialColorParameter.color(.blue)
    } else {
      // Placing on plane/floor - green indicator
      material.color = .init(tint: .green.withAlphaComponent(0.7))
      material.baseColor = MaterialColorParameter.color(.green)
    }
    
    let anchor = AnchorEntity(world: position)
    indicatorEntity.model = ModelComponent(mesh: geometry, materials: [material])
    if #available(iOS 18.0, *) {
      material.faceCulling = .none
      anchor.addChild(indicatorEntity)
    } else {
      // iOS 14-17 - create back plane
      guard let mesh = indicatorEntity.model?.mesh,
            let materials = indicatorEntity.model?.materials else { return }
      
      let backPlane = ModelEntity(mesh: mesh, materials: materials)
      backPlane.transform.rotation = simd_quatf(angle: .pi, axis: [0, 1, 0])
      anchor.addChild(indicatorEntity)
      anchor.addChild(backPlane)
    }
    
    
    _arView.scene.addAnchor(anchor)
    _placementIndicator = anchor
    _lastPlacementPosition = position

  }
}

// MARK: - Enhanced Floor Management System for ARView3D
// Replace your existing floor methods with these enhanced versions

@available(iOS 14.0, *)
extension ARView3D {
  
  
  @objc func createInvisibleFloor(at height: Float = SHARED_GROUND_LEVEL) {
    print("Creating simple INVISIBLE floor at: \(height)m")
    
    // Remove existing floor
    removeInvisibleFloor()
    
    // Create large invisible floor
    let floorSize: Float = 200.0
    let floorThickness: Float = 0.02
    
    // Create collision shape - this will be centered on the entity
    let floorShape = ShapeResource.generateBox(
      width: floorSize,
      height: floorThickness,
      depth: floorSize
    )
    
    // Create floor entity
    _invisibleFloor = ModelEntity()
    _invisibleFloor?.name = "InvisibleFloor"
    
    _invisibleFloor?.transform.translation = SIMD3<Float>(0, -floorThickness/2, 0)  // Negative to put top at 0

    _invisibleFloor?.collision = CollisionComponent(
      shapes: [floorShape],
      filter: CollisionFilter(
        group: CollisionGroups.environment,
        mask: [CollisionGroups.arObjects]
      )
    )
    
    _invisibleFloor?.physicsBody = PhysicsBodyComponent(
      massProperties: PhysicsMassProperties(mass: 1000.0),
      material: PhysicsMaterialResource.generate(
        staticFriction: 0.6,
        dynamicFriction: 0.4,
        restitution: 0.5
      ),
      mode: .static
    )
    
    // Create anchor at the specified height
    _floorAnchor = AnchorEntity(world: SIMD3<Float>(0, height, 0))
    _floorAnchor?.addChild(_invisibleFloor!)
    _arView.scene.addAnchor(_floorAnchor!)
    
    updateGroundLevel(newGroundLevel: height)
    
    print("Simple invisible floor created with top at: \(height)m")
  }
  
}
   
