// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import AVKit
import Combine

@available(iOS 14.0, *)
open class VideoNode: ARNodeBase, ARVideo {
  private var _player: AVPlayer
  private var _videoItem: AVPlayerItem? = nil
  private var _videoMaterial: VideoMaterial?
  private var cancellables = Set<AnyCancellable>()
  private var _videoWidth: Float = 0.5
  private var _videoHeight: Float = 0.375
  private var _isObservingStatus = false  // Track observer state
  
  @objc init(_ container: ARNodeContainer) {
    _player = AVPlayer()
    super.init(container: container)
    setupVideoNode()
    
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerItemDidPlayToEndTime),
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: nil
    )
    try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc private func setupVideoNode() {
    // Create a plane mesh for the video using the parent's _modelEntity
    let mesh = MeshResource.generatePlane(width: _videoWidth, height: _videoHeight)
    _modelEntity.model = ModelComponent(mesh: mesh, materials: [])
    
    // Rotate 180 degrees around Y axis to face the correct direction
    _modelEntity.transform.rotation = simd_quatf(angle: .pi, axis: [0, 1, 0])
  }
  
  // MARK: Properties
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_videoWidth)
    }
    set(width) {
      _videoWidth = UnitHelper.centimetersToMeters(abs(width))
      updateVideoPlaneSize()
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_videoHeight)
    }
    set(height) {
      _videoHeight = UnitHelper.centimetersToMeters(abs(height))
      updateVideoPlaneSize()
    }
  }
  
  private func updateVideoPlaneSize() {
    let mesh = MeshResource.generatePlane(width: _videoWidth, height: _videoHeight)
    _modelEntity.model?.mesh = mesh
    
    // Reapply the material if it exists
    if let material = _videoMaterial {
      _modelEntity.model?.materials = [material]
    }
  }
  
  @objc open var Source: String {
    get {
      return ""
    }
    set(path) {
      loadVideoSource(path: path)
    }
  }
  
  private func loadVideoSource(path: String) {
    
    // Remove observer from old video item if it exists
    if _isObservingStatus, let oldItem = _videoItem {
      oldItem.removeObserver(self, forKeyPath: "status")
      _isObservingStatus = false
    }
    
    // Check if this is a YouTube URL
    if isYouTubeURL(path) {
      print("📺 YouTube URL detected, this is a problem..")
      showFailYouTubeVideo(path)
      return
    }
    
    // Determine if this is a local file or remote URL
    let url: URL
    
    if path.hasPrefix("http://") || path.hasPrefix("https://") {
      // Remote URL
      guard let remoteURL = URL(string: path) else {
        print("❌ Invalid URL: \(path)")
        _container?.form?.dispatchErrorOccurredEvent(
          self,
          "Source",
          ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
          ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message
        )
        return
      }
      url = remoteURL
      
    } else {
      // Local file - get from asset manager
      let localPath = AssetManager.shared.pathForPublicAsset(path)
      url = URL(fileURLWithPath: localPath)
      
      // Check if file exists
      do {
        if try !url.checkResourceIsReachable() {
          print("❌ Video file not found: \(localPath)")
          _container?.form?.dispatchErrorOccurredEvent(
            self,
            "Source",
            ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
            ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message
          )
          return
        }
      } catch {
        print("❌ Error checking video file: \(error)")
        _container?.form?.dispatchErrorOccurredEvent(
          self,
          "Source",
          ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
          ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message
        )
        return
      }
    }
    
    // Create AVPlayerItem with proper configuration
    let asset = AVURLAsset(url: url)
    _videoItem = AVPlayerItem(asset: asset)
    
    // CRITICAL: Wait for player item to be ready before creating VideoMaterial
    _videoItem?.addObserver(
      self,
      forKeyPath: "status",
      options: [.new, .initial],
      context: nil
    )
    _isObservingStatus = true
    print("🔧 Added observer to new video item")
    
    // Replace current item
    _player.replaceCurrentItem(with: _videoItem)
    
    // Update notification observer
    NotificationCenter.default.removeObserver(
      self,
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: nil
    )
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerItemDidPlayToEndTime),
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: _videoItem
    )
  }
  
  private func isYouTubeURL(_ urlString: String) -> Bool {
    return urlString.contains("youtube.com") ||
           urlString.contains("youtu.be")
  }
  
  private func showFailYouTubeVideo(_ urlString: String) {
    _container?.form?.dispatchErrorOccurredEvent(
      self,
      "Source",
      ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
      "to show YouTube videos, you need to use the webViewNode instead."
    )
  }
  
  open override func observeValue(
    forKeyPath keyPath: String?,
    of object: Any?,
    change: [NSKeyValueChangeKey : Any]?,
    context: UnsafeMutableRawPointer?
  ) {
    if keyPath == "status" {
      if let playerItem = object as? AVPlayerItem {
        switch playerItem.status {
        case .readyToPlay:
          createVideoMaterial()
        case .failed:
          print("❌ Video failed to load: \(playerItem.error?.localizedDescription ?? "unknown error")")
          _container?.form?.dispatchErrorOccurredEvent(
            self,
            "Source",
            ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
            playerItem.error?.localizedDescription ?? "Failed to load video"
          )
        case .unknown:
          print("⏳ Video status unknown")
        @unknown default:
          break
        }
      }
    }
  }
  
  private func createVideoMaterial() {
    // Remove observer now that we're ready
    if _isObservingStatus, let item = _videoItem {
      item.removeObserver(self, forKeyPath: "status")
      _isObservingStatus = false
      print("🔧 Removed observer after video ready")
    }
    
    // Create VideoMaterial only when player is ready
    _videoMaterial = VideoMaterial(avPlayer: _player)
    _modelEntity.model?.materials = [_videoMaterial!]
    
    print("✅ VideoMaterial created and applied")
  }
  
  @objc open var IsPlaying: Bool {
    get {
      return _player.timeControlStatus == .playing
    }
  }
  
  @objc open var Volume: Int32 {
    get {
      return Int32(round(_player.volume * 100))
    }
    set(newVol) {
      _player.volume = min(max(Float(newVol) / 100.0, 0), 1)
    }
  }
  
  @objc open func Play() {
    print("▶️ Playing video")
    _player.play()
  }
  
  @objc open func Pause() {
    print("⏸ Pausing video")
    _player.pause()
  }
  
  @objc open func GetDuration() -> Int32 {
    if let item = _videoItem {
      let duration = CMTimeGetSeconds(item.duration)
      if duration.isNaN || duration.isInfinite {
        return 0
      }
      return Int32(duration)
    }
    return 0
  }
  
  @objc open func SeekTo(_ ms: Int32) {
    let time = CMTime(seconds: Double(ms) / 1000.0, preferredTimescale: 1000)
    _player.seek(to: time) { finished in
      if finished {
        print("⏩ Seek completed to \(ms)ms")
      }
    }
  }
  
  @objc open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }
  
  @objc func playerItemDidPlayToEndTime() {
    print("🏁 Video completed")
    Completed()
    _videoItem?.seek(to: .zero) { _ in }
  }
  
  deinit {
    if _isObservingStatus, let item = _videoItem {
      item.removeObserver(self, forKeyPath: "status")
      _isObservingStatus = false
      print("🔧 Removed observer in deinit")
    }
    
    // Remove notification observer
    NotificationCenter.default.removeObserver(self)
    
    // Clean up
    cancellables.removeAll()
  }
  
  override open func ScaleBy(_ scalar: Float) {
      let oldScale = Scale
      let hadPhysics = _modelEntity.physicsBody != nil
      
      let bounds = _modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.y - bounds.min.y) / 2.0  // Use Y for height
      let newScale = oldScale * abs(scalar)
      
      if hadPhysics {
          let previousSize = halfHeight * oldScale  // Use oldScale for clarity
          _modelEntity.position.y = _modelEntity.position.y - previousSize + (halfHeight * newScale)
      }
      
      Scale = newScale
  }

  override open func scaleByPinch(scalar: Float) {
    let oldScale = Scale
    let newScale = oldScale * abs(scalar)
    
    let hadPhysics = _modelEntity.physicsBody != nil
    // Use internal _height like SphereNode uses _radius
    let halfHeight = _videoHeight / 2.0
    let previousSize = halfHeight * oldScale
    if hadPhysics {
      let savedMass = Mass
      let savedFriction = StaticFriction
      let savedRestitution = Restitution
      
      _modelEntity.physicsBody = nil
      _modelEntity.collision = nil
        
      _modelEntity.position.y = _modelEntity.position.y - previousSize + (halfHeight * newScale)
      
      Scale = newScale
      Mass = savedMass
      StaticFriction = savedFriction
      Restitution = savedRestitution
      
      EnablePhysics(true)
    } else {
        Scale = newScale
    }
  }
}
