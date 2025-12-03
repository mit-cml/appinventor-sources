// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import AVFoundation
import RealityKit
import ARKit
import UIKit
import os.log
import Combine

@available(iOS 14.0, *)
private extension Entity {
  func isDescendant(of ancestor: Entity) -> Bool {
    var cur: Entity? = self
    while let e = cur {
        if e === ancestor { return true }
        cur = e.parent
    }
    return false
  }
}

@available(iOS 14.0, *)
open class ARView3D: ViewComponent, ARSessionDelegate, ARNodeContainer, CLLocationManagerDelegate, EventSource {

  public static var SHARED_GROUND_LEVEL: Float = -1.0
  public static var VERTICAL_OFFSET: Float = 0.002
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
  
  fileprivate var _isStarting: Bool = false
  
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
  
  private var _observersInstalled = false
  private var restartPending = false
  
  private var _currentImageSnapshot = ""
  
  enum State {
    case none,
         colliding,
         released,
         idle
    
  }
  
  /// Simple camera ownership guard to prevent multiple components from using the camera simultaneously.
  enum CameraClient {
      case arkit
      case barcode
      case none
  }

  final class CameraGuard {
      static let shared = CameraGuard()
      private init() {}
      
      private(set) var owner: CameraClient = .none
      
      /// Try to acquire exclusive camera use for a client.
      /// Returns true if successful, false if someone else already owns it.
      func tryAcquire(_ who: CameraClient) -> Bool {
          guard owner == .none && owner != who else {
              print("⚠️ Camera already owned by other \(owner)")
              return false
          }
          guard owner == who else {
            owner = who
            print("🎥 Camera now acquired by \(who)")
            return true
          }
        print("🎥 Camera already owned by arkit: \(who)")
          return true
      }
      
      /// Release the camera for other clients to use.
      func release(_ who: CameraClient) {
          if owner == who {
              owner = .none
              print("📸 Camera released by \(who)")
          }
      }
  }

  var currentState: State = .none

  private var _currentDraggedObject: ARNodeBase? = nil
  
  private var _placementIndicator: AnchorEntity?
  private var _lastPlacementPosition: SIMD3<Float>?

  private var worldMapURL: URL {
      let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
      return docs.appendingPathComponent("ar_worldmap.ardata")
  }
  
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
    locationManager?.requestTemporaryFullAccuracyAuthorization(withPurposeKey: "ARPlacement")
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
    _arView.automaticallyConfigureSession = false
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
    
    parent.add(self)
    Height = kARViewPreferredHeight
    Width = kARViewPreferredWidth
    
    _showWireframes = false
    _showWorldOrigin = false
    _showFeaturePoints = false

    print("🏁 ARView3D init complete")
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
  
  
  @available(iOS 14.0, *)
  private func setupConfiguration() -> ARConfiguration {
    
    // Check geo tracking support only when needed
    if _trackingType == .geoTracking && !ARGeoTrackingConfiguration.isSupported {
      self._container?.form?.dispatchErrorOccurredEvent(self, "Geotracking", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code, "Geo tracking not supported on this device")
     
    }
    
    if _trackingType == .worldTracking && !ARWorldTrackingConfiguration.isSupported {
      self._container?.form?.dispatchErrorOccurredEvent(self, "World tracking", ErrorMessage.ERROR_WORLD_TRACKING_NOT_SUPPORTED.code, "World tracking not supported on this device")

    }
    
    if let existingConfig = _arView.session.configuration as? ARWorldTrackingConfiguration,
       _trackingType == .worldTracking {
      existingConfig.detectionImages = getReferenceImages()
      existingConfig.maximumNumberOfTrackedImages = max(4, getReferenceImages().count)
      print("re-using configuration")
      return existingConfig
    }
    
    switch _trackingType {
      case .worldTracking:
        let worldTrackingConfiguration = ARWorldTrackingConfiguration()
        
        // Check if scene reconstruction is supported on this device
        if ARWorldTrackingConfiguration.supportsSceneReconstruction(.meshWithClassification) {
          worldTrackingConfiguration.sceneReconstruction = .meshWithClassification
          print("CONFIG: Mesh classification enabled")
          EventDispatcher.dispatchEvent(of: self, called: "Mesh with classification enabled")
        } else if ARWorldTrackingConfiguration.supportsSceneReconstruction(.mesh) {
          worldTrackingConfiguration.sceneReconstruction = .mesh
          print("CONFIG: Mesh with reconstruction enabled")
          EventDispatcher.dispatchEvent(of: self, called: "Mesh with reconstruction enabled")
        } else {
          print("CONFIG: Scene reconstruction not supported on this device")
          //worldTrackingConfiguration.planeDetection = [.horizontal, .vertical]
        }
        
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
        
        worldTrackingConfiguration.environmentTexturing = .none //.automatic
      _arView.renderOptions.insert(.disableMotionBlur)
      _arView.renderOptions.insert(.disableGroundingShadows)
        worldTrackingConfiguration.maximumNumberOfTrackedImages = 4
        worldTrackingConfiguration.detectionImages = getReferenceImages()
        _configuration = worldTrackingConfiguration
        
      case .geoTracking:
        let geoTrackingConfiguration = ARGeoTrackingConfiguration()
        geoTrackingConfiguration.maximumNumberOfTrackedImages = 4
      
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
        geoTrackingConfiguration.detectionImages = getReferenceImages()
        _configuration = geoTrackingConfiguration
        
      case .orientationTracking:
        _configuration = AROrientationTrackingConfiguration()
        
      case .imageTracking:
        let imageTrackingConfiguration = ARImageTrackingConfiguration()
        imageTrackingConfiguration.maximumNumberOfTrackedImages = 4
        imageTrackingConfiguration.trackingImages = getReferenceImages()
        _configuration = imageTrackingConfiguration
    }
  
    /* backup occlusion via depth*/
    if _enableOcclusion {
      var semantics: ARConfiguration.FrameSemantics = []
      if ARWorldTrackingConfiguration.supportsFrameSemantics(.sceneDepth) {
        semantics.insert(.sceneDepth)
      }
      if ARWorldTrackingConfiguration.supportsFrameSemantics(.smoothedSceneDepth) {
        semantics.insert(.smoothedSceneDepth)
      }
      _configuration.frameSemantics = semantics
      print("🔧 Frame semantics:", semantics)
    }
    
    
    // Enable lighting estimation
    _configuration.isLightEstimationEnabled = _lightingEstimationEnabled
    
    setupCollisionDetection()
    
    return _configuration
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
  
  @objc open var locationOfInvisibleFloorInCentimeters: Float {
    get {
      if (_invisibleFloor == nil) { return 0.0 }
      return UnitHelper.metersToCentimeters(_invisibleFloor!.transform.translation.y)
    }
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
          
          
          
        //print("🔥 Notified nodes of collision: \(nodeA.Name) ↔ \(nodeB.Name)")
      }
        
      if entityA.name == "InvisibleFloor" || entityB.name == "InvisibleFloor" {
          let nodeEntity = entityA.name == "InvisibleFloor" ? entityB : entityA
          //print("💥 FLOOR COLLISION with \(nodeEntity.name) at \(nodeEntity.transform.translation)")
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


  func requestRestart(_ opts: ARSession.RunOptions = [.resetTracking, .removeExistingAnchors]) {
    guard !restartPending else { return }
    restartPending = true
      DispatchQueue.main.async {
          self.startTrackingWithOptions(opts)
          self.restartPending = false
      }
  }
  
  private func parseNodes() {
    for (node, anchorEntity) in self._nodeToAnchorDict {
      node.EnablePhysics(node.EnablePhysics)
      
      if node.IsGeoAnchored {
        if let geoAnchor = node.getGeoAnchor() {
          self._arView.session.add(anchor: geoAnchor)
        }
      } else {
        if !node.IsFollowingImageMarker {
          self._arView.scene.addAnchor(anchorEntity)
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
  }
  
  
  private var relocalizationTimer: DispatchWorkItem?

  /* returning to same place or not ? */
  private func armRelocalizationWatchdog(enabled: Bool) {
      relocalizationTimer?.cancel()
      guard enabled else { return }

      let task = DispatchWorkItem { [weak self] in
          guard let self = self else { return }
          let frame = self._arView.session.currentFrame
          let status = frame?.worldMappingStatus
          let meshes = frame?.anchors.compactMap { $0 as? ARMeshAnchor }.count ?? 0

          // Not relocalized AND no meshes → fall back
          let notRelocalized = (status == .notAvailable || status == .limited || status == nil)
          if meshes == 0 && notRelocalized {
              self.forceFreshMapping()
          }
      }
      relocalizationTimer = task
      DispatchQueue.main.asyncAfter(deadline: .now() + 5.0, execute: task)
  }

  private func forceFreshMapping() {
      print("⏩ Fallback: dropping initialWorldMap to unblock mesh reconstruction")

      //prevent ARKit from trying to relocalize again
      self.shouldAttemptRelocalization = false
      requestRestart()
  }

  // MARK: - Wireframe Refresh Helpers
  private var meshArrivalToken: Cancellable?

  @objc open func startTrackingWithOptions(_ options: ARSession.RunOptions = [.resetTracking, .removeExistingAnchors]) {
    
    if _isStarting {
        print("⛔️ startTracking re-entry blocked");
        return
    }
    _isStarting = true
    defer { _isStarting = false }
    
    
    if !CameraGuard.shared.tryAcquire(.arkit) {
        print("🚫 seems that Camera in use by another component")
        //return
    }

    print("▶️ STARTTRACKING WITH OPTIONS: \(options)")
    print("ARView instance:", ObjectIdentifier(_arView))
    
    
    DispatchQueue.main.async {

      print("ARView instance inside startrackingoptions async:", ObjectIdentifier(self._arView))
      self._arView.session.pause()
      
      self._arView.session.delegate = self
      self._arView.automaticallyConfigureSession = false
      
      let config = self.setupConfiguration()
      
      /*if self.shouldAttemptRelocalization {
        if let map = self.loadWorldMapFromDisk() {
          print("✅ Restoring with saved world map")
          if let cfg = config as? ARWorldTrackingConfiguration {
            cfg.initialWorldMap = map
          }
        } else {
          print("ℹ️ No saved map found — starting fresh")
        }
      }*/
      
      self._arView.session.run(config, options: options)
      self.armRelocalizationWatchdog(enabled: self.shouldAttemptRelocalization)
      self._sessionRunning = true
      
      print("Mesh mode:", (self._arView.session.configuration as? ARWorldTrackingConfiguration)?.sceneReconstruction as Any)
      print("NOT loading a stored world map right now:")
      print("SU options:", self._arView.environment.sceneUnderstanding.options)
      print("autoConfig:", self._arView.automaticallyConfigureSession)

      var dbg: ARView.DebugOptions = []
      if self._showWireframes { dbg.insert(.showSceneUnderstanding) }
      if self._showWorldOrigin { dbg.insert(.showWorldOrigin) }
      if self._showFeaturePoints { dbg.insert(.showFeaturePoints) }
      if self._showPhysics { dbg.insert(.showPhysics) }
      
      self._arView.debugOptions = dbg
      
      self._arView.environment.sceneUnderstanding.options.insert(.collision)
      if (self._enableOcclusion){
        self._arView.environment.sceneUnderstanding.options.insert(.occlusion)
      }
      
      self.ensureFloorExists()
      
      self.updateGroundLevel(newGroundLevel: self.GROUND_LEVEL)
      
      // ✅ Re-enable WebViews if needed
      if self._reenableWebViewNodes {
        for node in self._nodeToAnchorDict.keys {
          if let webViewNode = node as? ARWebView {
            webViewNode.isUserInteractionEnabled = true
          }
        }
        self._reenableWebViewNodes = false
      }
      
      if self._requiresAddNodes {
        
        self.parseNodes()
        //self.refreshWireframeAndSU()

        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
          let meshes = self?._arView.session.currentFrame?.anchors.compactMap { $0 as? ARMeshAnchor }.count ?? 0
          let status = self?._arView.session.currentFrame?.worldMappingStatus
          print("🧪 startTracking mesh anchors:", meshes, "mapping:", String(describing: status))
        }
        for (anchorEntity, light) in self._lights {
          self._arView.scene.addAnchor(anchorEntity)
        }
        self._requiresAddNodes = false
      }
    }
    
    print("▶️ AR session started successfully")
    verifyFloorState()
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
    print("----RESET TRACKING----")
     
    let wasRunning = _sessionRunning
    
    // Stop session
    _arView.session.pause()
    _sessionRunning = false
    
    _hasSetGroundLevel = false
    removeInvisibleFloor()
    //_detectedPlanesDict.removeAll()
    
    for (node, _) in _nodeToAnchorDict {
        node.EnablePhysics(false)
    }
    
    // Pause and prepare for reset
    pauseTracking(!wasRunning)
      
    if wasRunning {
      print("🔄 Restarting with clean slate...")
      requestRestart([])
      
      // Re-enable physics after restart
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
          for (node, _) in self._nodeToAnchorDict {
              node.EnablePhysics(node.EnablePhysics)
          }
      }
    }
    print("🔄 Complete resetTracking finished")
  }
  
  private func setupLifecycleObservers() {
    guard !_observersInstalled else { return }
    NotificationCenter.default.addObserver(
        self,
        selector: #selector(onAppDidBecomeActive),
        name: UIApplication.didBecomeActiveNotification,
        object: nil
    )
    _observersInstalled = true
    print("🔔 Lifecycle observers registered")
  }


  @objc func onAppDidBecomeActive() {
      print("📱 ===== APP BECAME ACTIVE =====")
      
    if !_sessionRunning || _arView.session.currentFrame == nil {
        requestRestart([.resetTracking, .removeExistingAnchors])
    }
  }
  
  @objc open func ResetDetectedItems() {
    let _shouldRestartSession = _sessionRunning
    
    _hasSetGroundLevel = false
    removeInvisibleFloor()
    
    pauseTracking(!_shouldRestartSession)
    requestRestart([])
  }
  
  @objc func verifyFloorState() {
    print("🏠 Floor State Check:")
    print("  - Has floor: \(hasInvisibleFloor)")
    print("  - Ground level: \(GROUND_LEVEL)")
    print("  - Has set ground level: \(_hasSetGroundLevel)")
  }
  
  private func CreateImageMarkerFromYail(_ d: YailDictionary) -> ImageMarker? {
      guard let type = (d["type"] as? String)?.lowercased(), type == "imagemarker" else { return nil }
      let name = (d["name"] as? String) ?? "<unnamed>"
      var imgPath = (d["image"] as? String) ?? ""
      let wCM = (d["physicalWidthCM"] as? Float) ?? 15
      let vis = (d["visible"] as? Bool) ?? true
    
      print("🔍 Creating marker '\(name)' with image path: '\(imgPath)'")
      
      // ✅ If it's a full file:// URL but doesn't exist, try to find the file by name
      if imgPath.hasPrefix("file://") {
          let existingPath = AssetManager.shared.pathForExistingFileAsset(imgPath)
          
          if existingPath.isEmpty || !FileManager.default.fileExists(atPath: existingPath) {
              print("   ⚠️ Old path invalid, searching for file by name...")
              
              // Extract filename from old path
              if let filename = URL(string: imgPath)?.lastPathComponent {
                  print("   Looking for: \(filename)")
                  
                  // Search in Documents directory
                  let docsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                  let newPath = docsPath.appendingPathComponent(filename).path
                  
                  if FileManager.default.fileExists(atPath: newPath) {
                      print("   ✅ Found file at new location: \(newPath)")
                      imgPath = newPath
                  } else {
                      // Try AssetManager paths
                      let publicPath = AssetManager.shared.pathForPublicAsset(filename)
                      if FileManager.default.fileExists(atPath: publicPath) {
                          print("   ✅ Found file in public assets: \(publicPath)")
                          imgPath = publicPath
                      } else {
                          print("   ❌ Could not find file anywhere")
                      }
                  }
              }
          } else {
              imgPath = existingPath
          }
      }
      
      // Now try to load the image
      if let image = UIImage(contentsOfFile: imgPath) {
          print("   ✅ Image loaded from: \(imgPath)")
      } else {
          print("   ❌ Still cannot load image from: \(imgPath)")
      }
    
      let marker = ImageMarker(self)
      marker.Name = name
      marker.Image = imgPath
      marker.PhysicalWidthInCentimeters = wCM
      marker.Visible = vis
      
      if marker._referenceImage != nil {
          print("✅ Reference image created for '\(name)'")
      } else {
          print("❌ Reference image is NIL for '\(name)'")
      }
      
      return marker
  }

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
      //return .node(node, node._modelEntity.transform.translation)
      let worldPos = node._modelEntity.convert(position: .zero, to: nil)
      return .node(node, worldPos)
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
  
  private var shouldAttemptRelocalization = true
  open func sessionShouldAttemptRelocalization(_ session: ARSession) -> Bool {
      return shouldAttemptRelocalization
  }
  
  func updateGroundLevel(newGroundLevel: Float) {
      
      // Check all existing nodes
    for node in _nodeToAnchorDict.keys {
      let currentY = node._modelEntity.transform.translation.y
      let bounds = node._modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.y - bounds.min.y)/2
      let bottomY = currentY - halfHeight
      
      if bottomY < newGroundLevel + 0.005 {
        let correctedY = newGroundLevel + halfHeight + ARView3D.VERTICAL_OFFSET
          node._modelEntity.transform.translation.y = correctedY
          print("⬆️ LIFTED \(node.Name) from \(currentY) to \(correctedY)")
      }
    }
  }
  
  private var didRefreshForFirstMesh = false
  private var meshAnchorCount = 0
  private var didReassertConfigOnce = false

  private func resetMeshFlags() {
      didRefreshForFirstMesh = false
      meshAnchorCount = 0
      didReassertConfigOnce = false
  }
  
  private func refreshWireframeAndSU() {
    DispatchQueue.main.async {
      var su = self._arView.environment.sceneUnderstanding.options
      su.insert(.collision)
      su.insert(.physics)
      if su.contains(.occlusion) || self._enableOcclusion {
        su.insert(.occlusion)
      } else {
        su.remove(.occlusion)
      }
      
      self._arView.environment.sceneUnderstanding.options = su
      print("🔁 refreshWireframeAndSU")
      var dbg: ARView.DebugOptions = []
      if self._showWireframes { dbg.insert(.showSceneUnderstanding) }
      if self._showWorldOrigin   { dbg.insert(.showWorldOrigin) }
      if self._showFeaturePoints { dbg.insert(.showFeaturePoints) }
      if self._showPhysics       { dbg.insert(.showPhysics) }
      self._arView.debugOptions = dbg

      let hasCollision = su.contains(.collision)
      let hasOcclusion = su.contains(.occlusion)
      let hasPhysics   = su.contains(.physics)
      print("🔁 SU:", su.rawValue, "DBG:", dbg.rawValue,
            "=> collision=\(hasCollision) occlusion=\(hasOcclusion) physics=\(hasPhysics)")
    }
  }
  
  func detachToWorldIfNeeded(for marker: ImageMarker) {
      for node in marker._attachedNodes {
          // Cache LOCAL transform on first detach
        
        if node._nodeLocalTransform == nil {
              let localTransform = node._modelEntity.transformMatrix(relativeTo: marker.Anchor)
              node._nodeLocalTransform = Transform(matrix: localTransform)
              print("   💾 First-time cache of local transform for \(node.Name): \(node._nodeLocalTransform!.translation)")
        }
         
        guard let localOffset = node._nodeWorldTransform else {
            print("⚠️ No cached world position for \(node.Name)")
            continue
        }
        // ✅ Get world position BEFORE removing from parent
        let worldPosition = localOffset.translation
        let worldOrientation = node._modelEntity.orientation(relativeTo: nil)
        let worldScale = node._modelEntity.scale(relativeTo: nil)
        
        print("   📍 Preserving world orientation: \(worldOrientation)")
        // ✅ Create anchor at the world position directly
        let tempAnchor = AnchorEntity(world: worldPosition)
        tempAnchor.name = "\(node.Name)_tempAnchor"
        node._tempWorldAnchor = tempAnchor
                
        print("⚠️ TEMP anchor transform.translation: \(tempAnchor.transform.translation)")
                
        // ✅ Add anchor to scene BEFORE parenting
        _arView.scene.addAnchor(tempAnchor)
        
        // ✅ Now remove and parent
        node._modelEntity.removeFromParent()
        node._modelEntity.setParent(tempAnchor, preservingWorldTransform: false)
        node._modelEntity.position = .zero
        
        let finalOrientation = node._modelEntity.orientation(relativeTo: nil)
        print("   📍 Final world orientation: \(finalOrientation)")
        print("⚠️ node local pos: \(node._modelEntity.position)")

          
      }
  }
  @inline(__always)
  private func distance(_ a: SIMD3<Float>, _ b: SIMD3<Float>) -> Float {
    simd_length(a - b)
  }

  func reattachNodesToMarker(_ marker: ImageMarker) {
      guard let markerAnchor = marker.Anchor else { return }

      let nodesToReattach = marker._attachedNodes.filter { $0._modelEntity.parent !== markerAnchor }
      
      if nodesToReattach.isEmpty {
          return
      }
      
      for node in marker._attachedNodes {
          guard let localOffset = node._nodeLocalTransform else {
              print("⚠️ No local transform for \(node.Name)")
              continue
          }

          node._tempWorldAnchor?.removeFromParent()
          node._tempWorldAnchor = nil
          
          node._modelEntity.setParent(markerAnchor, preservingWorldTransform: false)
          //node._modelEntity.setOrientation(worldOrientation, relativeTo: nil)
          node._modelEntity.position = localOffset.translation

          // CSB for now, leave orientation alone until we can solve for billboarding
          if marker._billboardNodes {
            /*node._modelEntity.orientation = localOffset.rotation
             
             print("   💾 Restored local orientation for \(node.Name): \(localOffset.rotation)")
             
             // ✅ THEN apply camera-facing orientation (which will preserve the restored orientation)
             if let cameraTransform = _arView.cameraTransform as Optional {
             node.applyCameraFacingOrientation(cameraPosition: cameraTransform.translation)
             }*/
          }
        
          
          
          print("reattaching: Set orientation for \(node.Name)")
      }
  }
  private func cleanupMarkerPivot(_ marker: ImageMarker) {
    marker._anchorEntity?.removeFromParent()
    marker._anchorEntity = nil
  }
  
  // MARK: ARSession Delegate Methods
  public func session(_ session: ARSession, didAdd anchors: [ARAnchor]) {
    let newMeshes = anchors.compactMap { $0 as? ARMeshAnchor }.count
    if newMeshes > 0 {
        meshAnchorCount += newMeshes
        if !didRefreshForFirstMesh {
            didRefreshForFirstMesh = true
            refreshWireframeAndSU()
        }
    }
    for anchor in anchors {
      if let planeAnchor = anchor as? ARPlaneAnchor {
        let detectedPlane = DetectedPlane(anchor: planeAnchor, container: self)
        _detectedPlanesDict[anchor] = detectedPlane
        PlaneDetected(detectedPlane)
      
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
              createInvisibleFloor(at: invisibleFloorLevel)
              _hasSetGroundLevel = true
            }
          } else {
            let invisibleFloorLevel = detectedRealFloorLevel + ARView3D.VERTICAL_OFFSET
            print("🏠 FIRST TIME: Setting ground level to detected floor: \(invisibleFloorLevel)")
            GROUND_LEVEL = invisibleFloorLevel
            createInvisibleFloor(at: invisibleFloorLevel)
            _hasSetGroundLevel = true
          }
        }
      } else if let imageAnchor = anchor as? ARImageAnchor {
        guard
          let name = imageAnchor.referenceImage.name,
          let marker = _imageMarkers[name] as ImageMarker?
        else { continue }
        
        print("➕ didAdd imageAnchor \(imageAnchor.transform)")
        
        // Create anchor entity if needed
        if marker.Anchor == nil {
          let imA = AnchorEntity(anchor: imageAnchor)
          _arView.scene.addAnchor(imA)
          marker.Anchor = imA
          marker._anchorEntity = imA
          marker._lastARAnchorId = imageAnchor.identifier
          let anchorPos = imA.position(relativeTo: nil)
          print("   🎯 Created marker anchor at: \(anchorPos)")
        }
        
        marker._isTracking = imageAnchor.isTracked
        
        // ✅ Only attach runtime-created nodes here (no queued offset)
        // LoadScene nodes (with queued offset) will be attached in didUpdate
        for node in marker._attachedNodes {
          if node._queuedMarkerOffset != nil {
            print("   ⏭️ Skipping \(node.Name) - has queued offset, will attach in didUpdate")
            continue
          }
          if node._followingMarker as? ImageMarker !== marker {
              print("   ⚠️ Node \(node.Name) doesn't belong to this marker (belongs to \(node._followingMarker?.Name ?? "none"))")
              continue
            }
          
          // remove previous node parent and set to marker
          if node._modelEntity.parent !== marker.Anchor {
            node._modelEntity.removeFromParent()
            marker.Anchor!.addChild(node._modelEntity)
            node._modelEntity.position = SIMD3<Float>(0, 0, 0)
            node._anchorEntity = marker.Anchor
                        
            if node._nodeLocalTransform == nil {
              node._nodeLocalTransform = node._modelEntity.transform //same as marker
            }
            print("   ✅ Attached runtime node \(node.Name)")
          }
        }
        
        marker.FirstDetected(imageAnchor)
            
      } else if let geoAnchor = anchor as? ARGeoAnchor {
          handleGeoAnchorAdded(geoAnchor)
      }
    }
  }

  // In ImageMarker class - add update loop
  private var _lastUpdateTime = Date()

  
  // store the node position when marker is tracking
  func updateNodeTracking(marker: ImageMarker) {
    guard marker._isTracking,
          let markerAnchor = marker.Anchor else { return }
    let now = Date()
    guard now.timeIntervalSince(_lastUpdateTime) > 0.2 else { return }
    _lastUpdateTime = now

    
    // node position in world coordinates
    for node in marker._attachedNodes where node._modelEntity.parent === markerAnchor {
      let worldM = node._modelEntity.transformMatrix(relativeTo: nil)
      node._nodeWorldTransform = Transform(matrix: worldM)
    }
  }

  func ensureCurrentAnchorEntity(for marker: ImageMarker, imageAnchor: ARImageAnchor) {
    // If we already have an AnchorEntity, make sure it's for THIS ARImageAnchor id
    if let ae = marker.Anchor {
      let sameId: Bool = {
        return marker._lastARAnchorId == imageAnchor.identifier
      }()

      if !sameId {
        // Old/stale AE → remove & replace
        ae.removeFromParent()
        let imA = AnchorEntity(anchor: imageAnchor)
        _arView.scene.addAnchor(imA)
        marker._anchorEntity = imA
        marker._lastARAnchorId = imageAnchor.identifier
        print("🔁 Replaced STALE AnchorEntity with new one for id=\(imageAnchor.identifier)")
        return
      }

      // Same id but not in scene? Re-add.
      if ae.scene == nil {
        _arView.scene.addAnchor(ae)
        print("ℹ️ AnchorEntity existed but scene was nil — re-added")
      }
      return
    }

    // No AnchorEntity yet → create and add, imageAnchor transform
    let imA = AnchorEntity(anchor: imageAnchor)
    _arView.scene.addAnchor(imA)
    marker.Anchor = imA
    marker._anchorEntity = imA
    marker._lastARAnchorId = imageAnchor.identifier
    let entityWorldPos = imA.position(relativeTo: nil)
    print("   RealityKit entity pos: (\(entityWorldPos.x), \(entityWorldPos.y), \(entityWorldPos.z))")
    print("➕ ensure AnchorEntity for id=\(imageAnchor.identifier) at \(imA.position)")
  }

  private var _consecutiveLostFrames = 0
  private let _lostFrameThreshold = 5
  public func session(_ session: ARSession, didUpdate anchors: [ARAnchor]) {
    for anchor in anchors {
      if let planeAnchor = anchor as? ARPlaneAnchor, let updatedPlane = _detectedPlanesDict[anchor] {
        updatedPlane.updateFor(anchor: planeAnchor)
        DetectedPlaneUpdated(updatedPlane)
      } else if let imageAnchor = anchor as? ARImageAnchor {
        guard
          let name = imageAnchor.referenceImage.name,
          let marker = _imageMarkers[name]
        else { continue }

        // Check if anchor will be replaced BEFORE calling ensureCurrentAnchorEntity
        let anchorWillBeReplaced = (marker._lastARAnchorId != nil &&
                                    marker._lastARAnchorId != imageAnchor.identifier)
        
        // Ensure we have current anchor entity
        ensureCurrentAnchorEntity(for: marker, imageAnchor: imageAnchor)
        
        // Capture tracking state
        let wasTracked = marker._isTracking
        let nowTracked = imageAnchor.isTracked
        
        // Update tracking state
        marker._isTracking = nowTracked
        
        // ✅ FIRST: Attach any LoadScene nodes with queued offsets
        if nowTracked {
          _consecutiveLostFrames = 0
          for node in marker._attachedNodes where node._queuedMarkerOffset != nil {
            print("   🔗 Attaching LoadScene node \(node.Name)")
            node._modelEntity.removeFromParent()
            marker.Anchor!.addChild(node._modelEntity)
            
            node._queuedMarkerOffset = nil
            node._anchorEntity = marker.Anchor
            node.EnablePhysics(false) //seems to bounce around
            
            //node._modelEntity.orientation = simd_quatf(angle: 0, axis: [0, 1, 0])  // no tipping
            
            if node._nodeLocalTransform == nil {
              node._nodeLocalTransform = node._modelEntity.transform
              print("     💾 Cached local transform (anchor pos): \(node._modelEntity.position)")
            }
          }
        }
        
        if anchorWillBeReplaced || (nowTracked && !wasTracked) {
          if anchorWillBeReplaced {
            //print("🔁 Anchor replaced for \(name) - about to reattach nodes")
          } else {
            //print("🔄 Marker \(name) reappeared - reattaching nodes")
          }
          marker.AppearedInView()
          reattachNodesToMarker(marker)
        }
        // ✅ THIRD: Handle detachment when marker is lost
        else if wasTracked && !nowTracked && !anchorWillBeReplaced {
          _consecutiveLostFrames += 1
          
          //csb - this throttling doesn't seem necessary atm if _consecutiveLostFrames >= 2 {
          print("⚠️ Marker \(name) lost - detaching to world space")
          marker.NoLongerInView()
          detachToWorldIfNeeded(for: marker)
          _consecutiveLostFrames = 0
          //}
        }
        
        // ✅ Update world transforms while tracked (for smooth fallback)
        if nowTracked {
          updateNodeTracking(marker: marker)
        }
          
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
      } else if let imageAnchor = anchor as? ARImageAnchor,
          let name = imageAnchor.referenceImage.name,
          let marker = _imageMarkers[name] {
            cleanupMarkerPivot(marker)
            marker.Reset()
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
  }
      
  public func sessionInterruptionEnded(_ session: ARSession) {
    print("⚠️ ===== AR SESSION INTERRUPTED ENDED =====")
      _sessionRunning = false
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
        self.requestRestart()
      }
  }
  
  func canUseGeo() -> Bool {
      guard let status = _arView.session.currentFrame?.geoTrackingStatus else { return false }
      return status.state == .localized && status.accuracy == .high
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
          let currentDistance = currentSessionStart.distance(from: currentSessionStart)
          
          if !canUseGeo() || currentDistance < 5.0 {
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
                  yMeters + halfHeight + ARView3D.VERTICAL_OFFSET,  // detected surface + radius + clearance
                  groundLevel + ARView3D.VERTICAL_OFFSET + halfHeight  // fallback floor position
              )
              
              //node.setPosition(x: xMeters, y: safeY, z: zMeters)
              node._modelEntity.setPosition(SIMD3<Float>(xMeters, safeY, zMeters), relativeTo: nil)
              print("create node at x \(xMeters) y  \(yMeters) z \(zMeters) and safe is \(safeY)")
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
      node.Model = modelObjString
      node.Initialize()
      
      let xMeters: Float = UnitHelper.centimetersToMeters(x)
      let yMeters: Float = UnitHelper.centimetersToMeters(y)
      let zMeters: Float = UnitHelper.centimetersToMeters(z)
      
      let groundLevel = GROUND_LEVEL
      let safeY = max(yMeters, groundLevel + ARView3D.VERTICAL_OFFSET) // At least 1cm above ground
      
      node.setPosition(x: xMeters, y: safeY, z: zMeters)
      
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
    
    @objc open func CreatePlaneNodeAtLocation(_ x: Float, _ y: Float, _ z: Float, _ lat: Double, _ lng: Double, _ altitude: Double,  _ hasGeoCoordinates: Bool, _ isANodeAtPoint: Bool) -> PlaneNode? {
      guard ARGeoTrackingConfiguration.isSupported else {
        _container?.form?.dispatchErrorOccurredEvent(self, "CreatePlaneNodeAtGeoAnchor", ErrorMessage.ERROR_GEOANCHOR_NOT_SUPPORTED.code)
        return nil
      }
      
      let node = PlaneNode(self)
      node.Name = "GeoPlaneNode"
      node.Initialize()
      
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
    
    @objc open func CreateImageMarker(_ imagePath: String) -> ImageMarker {
      let marker = ImageMarker(self)
      marker.Name = "RoomMarker" + UUID().uuidString
      marker.Image = imagePath
      return marker
    }
    
    @objc open func CreateImageMarkerFromSnapshot(_ name: String, _ width: Float) {
      self.TakePicture(name, width)
    }
    
    
    func captureSnapshot(_ onDone: @escaping (UIImage?) -> Void) {
      self._arView.snapshot(saveToHDR: false) { image in
        onDone(image)
      }
    }
    
    @objc open func TakePicture(_ dummyName: String, _ width: Float = 15.0) {
      captureSnapshot { image in
        guard let image else {
          print("❌ Failed to make image for marker")
          return
        }
        guard let imageUI = image as Optional else { return }
        do {
          let url = try self.savePNG(imageUI, dummyName)
          self._currentImageSnapshot = url
          
          let nsWidth: NSNumber = NSNumber(value: width)
          EventDispatcher.dispatchEvent(
              of: self,
              called: "AfterPicture",
              arguments: url as NSString, dummyName as NSString, width as NSNumber
          )
        }catch {
            print("❌ Failed to save snapshot for marker: \(error)")
        }
      }
    }

    @objc open func AfterPicture( _ ref: NSString, _ name: NSString, _ width: Float = 15.0) -> NSString?{
        return ref
    }
    
    func savePNG(_ image: UIImage, _ dummyName: String) throws -> String {
      let data = image.pngData()!
      let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        .first!
        .appendingPathComponent("\(dummyName).png")
      try data.write(to: url, options: .atomic)
      return url.absoluteString
    }
    
    func renameFile(urlString: String, to newName: String) throws -> String {
      guard let oldURL = URL(string: urlString) else {
        throw NSError(domain: "Invalid URL", code: -1, userInfo: nil)
      }
      
      let directory = oldURL.deletingLastPathComponent()
      let fileExtension = oldURL.pathExtension
      let newURL = directory.appendingPathComponent("\(newName).\(fileExtension)")
      
      try FileManager.default.moveItem(at: oldURL, to: newURL)
      
      return newURL.absoluteString
    }
    
    @objc open func ImageForMarkerCreated(_ urlString: String, _ newName: String, _ widthCm: Float = 15.0) -> ImageMarker {
      //marker.Name = "RoomMarker" + UUID().uuidString
      //marker.Image = imagePath
      let marker: ImageMarker = ImageMarker(self)
      marker.Name = newName
      
      var newU = urlString
      do {
        newU = try renameFile(urlString: urlString, to: newName)
      }catch {
        print("Error renaming file: \(error)")
        //return marker
      }

      marker.Image = newU
      marker.PhysicalWidthInCentimeters = widthCm // defaulting atm
      print("returning IM \(marker.Name) \(newU) \(widthCm)")
      return marker
    }
    
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
    
    public func loadWorldMapFromDisk() -> ARWorldMap? {
      guard FileManager.default.fileExists(atPath: worldMapURL.path) else {
          return nil
      }
      print("Load world map from disk")
      do {
        let data = try Data(contentsOf: worldMapURL)
        guard let worldMap = try NSKeyedUnarchiver.unarchivedObject(
            ofClass: ARWorldMap.self,
            from: data
        ) else {
          print("❌ Failed to unarchive world map")
            return nil
        }
        print("✅ world map!")
        return worldMap
      } catch {
        print("❌ Error loading world map: \(error)")
        return nil
      }
    }
      
    // MARK: - NodeSpec (lightweight blueprint built off-main)
    private struct NodeSpec {
        let type: String
        let dict: YailDictionary
    }

    @objc open func LoadScene(_ dictionaries: [AnyObject]) -> [AnyObject] {
      print("LOADING stored scene \(dictionaries)")
      
      var loadNode: ARNodeBase?
      var newNodes: [ARNode] = []
      
      var markersByName: [String: ImageMarker] = [:]
      var pendingFollows: [(node: ARNodeBase, marker: String, offCM: SIMD3<Float>)] = []
      
      guard !dictionaries.isEmpty else {
        return []
      }
      
      for any in dictionaries {
        guard let d = any as? YailDictionary else { continue }
        if let type = (d["type"] as? String)?.lowercased(), type == "imagemarker" {
          if let marker = CreateImageMarkerFromYail(d) {
            markersByName[marker.Name] = marker
          }
        }
      }
      print("⚠️ markers from yail '\(markersByName). should be added to ref");

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
            print("adding Node")
            addNode(node)
            newNodes.append(node)
            
            if let follow = nodeDict["follow"] as? YailDictionary,
               let name = follow["markerName"] as? String,
               let offM = follow["offsetM"] as? YailDictionary,
               // ✅ Handle both Float and Double
               let ox = (offM["x"] as? Optional) ?? Float(offM["x"] as? Double ?? 0.0),
               let oy = (offM["y"] as? Optional) ?? Float(offM["y"] as? Double ?? 0.0),
               let oz = (offM["z"] as? Optional) ?? Float(offM["z"] as? Double ?? 0.0) {
              print("node is following \(name)")
              node.EnablePhysics(false) //CSB for now
              pendingFollows.append((node, name, SIMD3<Float>(ox, oy, oz)))
            }
          }
        }
      }
      
      for (node, markerName, offM) in pendingFollows {  // storage in M
        if let marker = markersByName[markerName] {
          print("Reparenting from LOADSCENE!")
            node.reparentUnderMarker(marker, keepWorld: true, offsetM: offM)
        } else {
          print("⚠️ Missing marker '\(markerName)'; leaving node in world space.")
        }
      }
      print("loadscene new nodes are \(newNodes)")
      return newNodes
    }

    // Keep this main-actor because it touches session/currentFrame.
    @available(iOS 15.0, *)
    @MainActor
    private func waitForARKitReady() async {
        let maxWaitTime: TimeInterval = 3.0
        let checkInterval: TimeInterval = 0.1
        var elapsed: TimeInterval = 0

        while elapsed < maxWaitTime {
            if let status = _arView.session.currentFrame?.worldMappingStatus,
               status == .extending || status == .mapped {
                print("✅ ARKit ready (status: \(status))")
                return
            }
            try? await Task.sleep(nanoseconds: UInt64(checkInterval * 1_000_000_000))
            elapsed += checkInterval
        }
        print("⚠️ ARKit not fully ready after \(maxWaitTime)s, proceeding anyway")
    }

    @objc open func getWorldMappingStatus() -> String {
        switch _arView.session.currentFrame?.worldMappingStatus {
        case .notAvailable: return "Not Available"
        case .limited:      return "Limited"
        case .extending:    return "Extending"
        case .mapped:       return "Mapped"
        case .none:         return "Unknown"
        @unknown default:   return "Unknown"
        }
    }
    
    private func allImageMarkersForSave() -> [YailDictionary] {
      var out: [YailDictionary] = []
      for m in _imageMarkers {   // implement `allMarkers()`
        out.append(m.value.ImageMarkerToYail())
      }
      print("⚠️ markers to save: \(out)");
      return out
    }
    
    @objc open func SaveScene(_ newNodes: [AnyObject]) -> [YailDictionary] {
      var dictionaries: [YailDictionary] = []
 
      dictionaries.append(contentsOf: allImageMarkersForSave())
      
      for node in newNodes { // swift thinks newnodes is nsarray
        guard let arNode = node as? ARNode else { continue }
        
        let nodeDict = arNode.ARNodeToYail()
        dictionaries.append(nodeDict)
      }
      print("returning dictionaries: imageMarkers and nodes with count:\(dictionaries.count)")
      // Save world map
      _arView.session.getCurrentWorldMap { worldMap, error in
        guard let worldMap = worldMap else {
          print("❌ Error getting world map: \(error?.localizedDescription ?? "unknown")")
          return
        }
        
        do {
          // Encode the ARWorldMap using NSKeyedArchiver
          let data = try NSKeyedArchiver.archivedData(
              withRootObject: worldMap,
              requiringSecureCoding: true
          )
          // Save the encoded data to your file URL
          try data.write(to: self.worldMapURL, options: [.atomic])
          print("✅ World map saved successfully at \(self.worldMapURL.path)")
            
        } catch {
            print("❌ Error saving world map: \(error)")
        }
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

  // Turn a hit entity into your ARNodeBase using _nodeToAnchorDict
  private func ownerNode(for hit: Entity) -> ARNodeBase? {
      // You wrap each node around a single ModelEntity: node._modelEntity
      for (node, _) in _nodeToAnchorDict {
          if hit.isDescendant(of: node._modelEntity) {
              return node
          }
      }
      return nil
  }
  
  func findClosestNode(tapLocation: CGPoint) -> ARNodeBase? {
      // 1) Try RealityKit's entity hit-test first (topmost renderable under the finger)
      if let hitEntity = _arView.entity(at: tapLocation) {
        if let hitEntity = _arView.entity(at: tapLocation),
           let owner = ownerNode(for: hitEntity) {
            return owner
        }
      }

      // 2) Build a ray from the camera through the tap for robust fallback picking
      guard let cam = _arView.cameraTransform as? Transform else { return nil }
      let origin = cam.translation
      let screenRayDir: SIMD3<Float>
      if let ray = _arView.ray(through: tapLocation) {
          screenRayDir = simd_normalize(ray.direction)
      } else {
          // Last-resort: use ARKit plane raycast to get a direction
          if let rr = _arView.raycast(from: tapLocation, allowing: .estimatedPlane, alignment: .any).first {
              let hit = SIMD3<Float>(rr.worldTransform.columns.3.x,
                                     rr.worldTransform.columns.3.y,
                                     rr.worldTransform.columns.3.z)
              screenRayDir = simd_normalize(hit - origin)
          } else {
              return nil
          }
      }

      var best: (node: ARNodeBase, depth: Float, score: Float)?
      let maxDepth: Float = 10.0 // meters, ignore stuff behind far plane

      for (node, _) in _nodeToAnchorDict {

          // Skip invisible/no-geometry/no-name placeholders
          guard node.Visible,
                node._modelEntity.model != nil,
                !node.Name.isEmpty else { continue }

          // WORLD space position for everything
          let worldPos = node._modelEntity.position(relativeTo: nil)

          // Discard things behind the camera
          let toNode = worldPos - origin
          let depthAlongRay = simd_dot(toNode, screenRayDir)
          if depthAlongRay <= 0 || depthAlongRay > maxDepth { continue }

          // Adaptive screen gate using world->screen projection (WORLD point!)
          if let screenPt = _arView.project(worldPos) {
              // Threshold scales with distance so small far objects still pass
              let basePx: CGFloat = 36.0
              let adaptivePx = max(basePx, CGFloat(depthAlongRay) * 18.0)

              let dx = tapLocation.x - screenPt.x
              let dy = tapLocation.y - screenPt.y
              let screenDist = hypot(dx, dy)
              if screenDist > adaptivePx { continue }
          } else {
              // Can't project => skip
              continue
          }

          // Compute distance from the node to the camera ray (not to a plane hit)
          // dist(ray, point) = |(p0 - o) x d|  where o=origin, d=dir (unit)
          let crossVec = simd_cross(worldPos - origin, screenRayDir)
          let distToRay = simd_length(crossVec)

          // Use a size-aware radius based on visual bounds in WORLD space
          let vb = node._modelEntity.visualBounds(relativeTo: nil)
          let size = vb.max - vb.min
          let radius = max(max(size.x, size.y), size.z) * 0.5

          // Combine: favor small distToRay, then small depth (front-most)
          // Penalty disappears if the ray actually passes through the node's radius
          let missPenalty = max(0, distToRay - radius)
          let score = missPenalty * 4.0 + depthAlongRay * 0.25

          if best == nil || score < best!.score {
              best = (node, depthAlongRay, score)
          }
      }

      return best?.node
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
        isNodeAtPoint = true
        NodeLongClick(node)
        node.LongClick()
        
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
    
    guard marker._referenceImage?.name != nil else {
      _container?.form?.dispatchErrorOccurredEvent(self, "addMarker",
         ErrorMessage.ERROR_IMAGEMARKER_MISSING_NAME.code)
      return
    }

    _imageMarkers[marker.Name] = marker
    requestRestart([])
  
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
    // ⚙️ Delay full AR reconfiguration to ensure ARView stays valid after App Inventor reload
    
   
  }

  
  @objc public func onDestroy() {
    clearView()
  }
  
  private func clearView() {
      print("CLEARING view")
      DispatchQueue.main.async {
        self._sessionRunning = false
        self._arView.session.pause()

        // Remove your content (anchors/entities), but keep the ARView itself alive
        if let floor = self._floorAnchor { self._arView.scene.removeAnchor(floor) }
        self._floorAnchor = nil
        self._invisibleFloor = nil

        for (_, anchorEntity) in self._nodeToAnchorDict {
          self._arView.scene.removeAnchor(anchorEntity)
        }
        
        self.resetMeshFlags()
        
        self._nodeToAnchorDict.removeAll()
        self._lights.removeAll()
        self._imageMarkers.removeAll()
        self.removeInvisibleFloor()
        self._hasSetGroundLevel = false
        
        NotificationCenter.default.removeObserver(self)
        self._observersInstalled = false
        
        //CameraGuard.shared.release(.arkit)
      }
  }
}

// MARK: - Extensions for compatibility
// Helpers you can place in a utilities file
extension simd_float4x4 {
  var translation3: SIMD3<Float> { .init(columns.3.x, columns.3.y, columns.3.z) }
}


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
  
  @objc func getCameraLook() -> YailDictionary {
    let screenCenter = CGPoint(x: _arView.bounds.midX, y: _arView.bounds.midY)
    
    let raycastResults = _arView.raycast(
        from: screenCenter,
        allowing: .estimatedPlane,
        alignment: .horizontal
    )
    
    guard let result = raycastResults.first else {
      print("⚠️ No surface found where camera is looking")
      return [:]
    }
    
    let worldPosition = SIMD3<Float>(
        result.worldTransform.columns.3.x,
        result.worldTransform.columns.3.y,
        result.worldTransform.columns.3.z
    )
    
    
    print("📍 Placed object where camera was looking: \(worldPosition.x)")
   
      let d: YailDictionary = [:]
      d["x"] = worldPosition.x
      d["y"] = worldPosition.y
      d["z"] = worldPosition.z
    
    return d
  }
  
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
        
        
        let dragBoxBounds = node._modelEntity.visualBounds(relativeTo: nil)
        let dragBoxHalfHeight = (dragBoxBounds.max.y - dragBoxBounds.min.y) / 2
        
        // Position the new box's CENTER at: top of target + half-height of new box
        let stackPosition = SIMD3<Float>(
            hitPosition.x,
            topSurface.y,
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
   
