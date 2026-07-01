// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import RealityKit
import AVKit
import Combine

@available(iOS 14.0, *)
open class VideoNode: ARNodeBase, ARVideo {

  public var IsBillboarding: Bool {
    get { return _isBillboarding }
    set { _isBillboarding = newValue }
  }

  private var _player: AVPlayer
  private var _videoItem: AVPlayerItem? = nil

  // Boxed so the stored property itself doesn't require iOS 15.
  private var _customMaterialBox: Any? = nil
  @available(iOS 15.0, *)
  private var _customMaterial: CustomMaterial? {
    get { _customMaterialBox as? CustomMaterial }
    set { _customMaterialBox = newValue }
  }

  private var cancellables = Set<AnyCancellable>()
  private var _videoWidth: Float = 0.5
  private var _videoHeight: Float = 0.375
  private var _isObservingStatus = false  // Track observer state

  private var _isBillboarding = false
  private var _loop = false

  // Pixel pump plumbing (iOS 15+ chroma-key path only)
  private var _videoOutput: AVPlayerItemVideoOutput?
  private var _displayLink: CADisplayLink?
  private var _textureCache: CVMetalTextureCache?
  private var _metalDevice: MTLDevice?
  
  private var _sourceRetryCount = 0
  private let _maxSourceRetries = 20  // 10 seconds total

  // Boxed so these can also stay outside the class's iOS 14 stored-property minimum.
  private var _drawableQueueBox: Any? = nil
  @available(iOS 15.0, *)
  private var _drawableQueue: TextureResource.DrawableQueue? {
    get { _drawableQueueBox as? TextureResource.DrawableQueue }
    set { _drawableQueueBox = newValue }
  }

  private var _videoTextureResourceBox: Any? = nil
  @available(iOS 15.0, *)
  private var _videoTextureResource: TextureResource? {
    get { _videoTextureResourceBox as? TextureResource }
    set { _videoTextureResourceBox = newValue }
  }

  @objc init(_ container: ARNodeContainer) {
    _player = AVPlayer()
    super.init(container: container)
    setupVideoNode()
    self.Name = "video"
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerItemDidPlayToEndTime(notification:)),
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: _videoItem
    )
    try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override open func defaultCameraFacingOrientation() -> simd_quatf {
    return simd_quatf(angle: .pi, axis: [1, 0, 0])  // Pitch -90°
  }

  @objc private func setupVideoNode() {
    let mesh = MeshResource.generatePlane(width: _videoWidth, height: _videoHeight)
    _modelEntity.model = ModelComponent(mesh: mesh, materials: [])
  }
  
  override open func orientationForMarkerAttachment() -> simd_quatf {
      let standUpright = simd_quatf(angle: +.pi/2, axis: [1, 0, 0])
      let cancelYFlip = simd_quatf(angle: .pi, axis: [0, 1, 0])
      return standUpright * cancelYFlip
  }
  
  override open var needsCameraFacingOrientationOnPlacement: Bool { return true }

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
  


  private func retryLoadingSource(path: String, delay: TimeInterval = 0.5) {
      guard _sourceRetryCount < _maxSourceRetries else {
          print("❌ Gave up waiting for file: \(path)")
          _sourceRetryCount = 0
          _container?.form?.dispatchErrorOccurredEvent(
              self,
              "Source",
              ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
              ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message
          )
          return
      }
      _sourceRetryCount += 1
      print("⏳ Retry \(_sourceRetryCount)/\(_maxSourceRetries) for: \(path)")
      DispatchQueue.main.asyncAfter(deadline: .now() + delay) { [weak self] in
          self?.loadVideoSource(path: path)
      }
  }

  private func updateVideoPlaneSize() {
    let mesh = MeshResource.generatePlane(width: _videoWidth, height: _videoHeight)
    // Materials persist across mesh swaps automatically; no need to reassign them here.
    _modelEntity.model?.mesh = mesh
  }

  @objc open var Source: String {
    get {
      return ""
    }
    set(path) {
      print("🎬 Source set called with path: \(path)")
      _sourceRetryCount = 0
      loadVideoSource(path: path)
    }
  }

  @objc private func playerItemDidPlayToEndTime(notification: NSNotification) {
    print("🏁 Video completed")
    Completed()
    if _loop {
      _player.seek(to: .zero)
      _player.play()
    } else {
      _videoItem?.seek(to: .zero) { _ in }
    }
  }

  private func loadVideoSource(path: String) {
    
    print("🔍 loadVideoSource — isHTTP: \(path.hasPrefix("http")), path: \(path)")
    if _isObservingStatus, let oldItem = _videoItem {
      oldItem.removeObserver(self, forKeyPath: "status")
      _isObservingStatus = false
    }

    teardownVideoOutput()

    if isYouTubeURL(path) {
      print("📺 YouTube URL detected, this is a problem..")
      showFailYouTubeVideo(path)
      return
    }

    let url: URL
    if path.hasPrefix("http://") || path.hasPrefix("https://") {
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
      let localPath = AssetManager.shared.pathForPublicAsset(path)
      url = URL(fileURLWithPath: localPath)

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
        print("⏳ Video file not ready yet, will retry: \(path)")
        retryLoadingSource(path: path)
        return
      }
    }

    let asset = AVURLAsset(url: url)
    _videoItem = AVPlayerItem(asset: asset)

    _videoItem?.addObserver(
      self,
      forKeyPath: "status",
      options: [.new, .initial],
      context: nil
    )
    _isObservingStatus = true
    print("🔧 Added observer to new video item")

    _player.replaceCurrentItem(with: _videoItem)

    NotificationCenter.default.removeObserver(
      self,
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: nil
    )
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(playerItemDidPlayToEndTime(notification:)),
      name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
      object: _videoItem
    )
  }

  // MARK: - Setting up the video output + texture cache (iOS 15+ chroma-key path)

  @available(iOS 15.0, *)
  private func setupVideoOutput(for item: AVPlayerItem) {
    // BGRA is the simplest format to map straight into a Metal texture,
    // no YCbCr plane conversion needed.
    let attrs: [String: Any] = [
      kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA
    ]
    let output = AVPlayerItemVideoOutput(pixelBufferAttributes: attrs)
    item.add(output)
    _videoOutput = output

    if _textureCache == nil {
      guard let device = _metalDevice ?? MTLCreateSystemDefaultDevice() else {
        print("❌ No Metal device available for texture cache")
        return
      }
      _metalDevice = device
      var cache: CVMetalTextureCache?
      CVMetalTextureCacheCreate(kCFAllocatorDefault, nil, device, nil, &cache)
      _textureCache = cache
    }

    startDisplayLink()
  }

  @available(iOS 15.0, *)
  private func setupDrawableQueue(width: Int, height: Int) {
    print("🎬 setupDrawableQueue called — _customMaterial is \(_customMaterialBox == nil ? "NIL ❌" : "set ✅")")
        
    let descriptor = TextureResource.DrawableQueue.Descriptor(
      pixelFormat: .bgra8Unorm,
      width: width,
      height: height,
      usage: [.shaderRead, .renderTarget],
      mipmapsMode: .none
    )
    do {
      let queue = try TextureResource.DrawableQueue(descriptor)
      _drawableQueue = queue

      let placeholder = makePlaceholderImage(width: width, height: height)
      if #available(iOS 18.0, *) {
        let resource = try TextureResource(
          image: placeholder,
          withName: "videoFrame",
          options: .init(semantic: .color)
        )
        resource.replace(withDrawables: queue)
        _videoTextureResource = resource

        if var mat = _customMaterial {
          mat.custom.texture = CustomMaterial.Texture(resource)
          _customMaterial = mat
          _modelEntity.model?.materials = [mat]
        }
      } else {
        print("❌ Isn't ios18, fallback")
        
      }
     
    } catch {
      print("❌ Failed to create drawable queue: \(error)")
    }
  }

  private func makePlaceholderImage(width: Int, height: Int) -> CGImage {
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    let context = CGContext(
      data: nil, width: width, height: height,
      bitsPerComponent: 8, bytesPerRow: width * 4,
      space: colorSpace,
      bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    )!
    return context.makeImage()!
  }

  @available(iOS 15.0, *)
  private func startDisplayLink() {
    stopDisplayLink()
    let link = CADisplayLink(target: self, selector: #selector(displayLinkFired))
    link.add(to: .main, forMode: .common)
    _displayLink = link
  }

  private func stopDisplayLink() {
    _displayLink?.invalidate()
    _displayLink = nil
  }

  @available(iOS 15.0, *)
  @objc private func displayLinkFired(_ sender: CADisplayLink) {
    guard let output = _videoOutput,
          let cache = _textureCache else { return }

    let itemTime = output.itemTime(forHostTime: CACurrentMediaTime())
    guard output.hasNewPixelBuffer(forItemTime: itemTime),
          let pixelBuffer = output.copyPixelBuffer(forItemTime: itemTime, itemTimeForDisplay: nil)
    else { return }

    guard let mtlTexture = makeTexture(from: pixelBuffer, cache: cache) else { return }

    // Lazily create the drawable queue once we know the real frame dimensions.
    if _drawableQueue == nil {
      setupDrawableQueue(width: mtlTexture.width, height: mtlTexture.height)
    }

    guard let queue = _drawableQueue else { return }

    do {
      let drawable = try queue.nextDrawable()
      let destTexture = drawable.texture

      guard let device = _metalDevice,
            let commandQueue = device.makeCommandQueue(),
            let commandBuffer = commandQueue.makeCommandBuffer(),
            let blitEncoder = commandBuffer.makeBlitCommandEncoder() else {
        return
      }

      blitEncoder.copy(from: mtlTexture, to: destTexture)
      blitEncoder.endEncoding()
      commandBuffer.addCompletedHandler { _ in
        drawable.present()
      }
      commandBuffer.commit()

    } catch {
      // nextDrawable() can legitimately fail/timeout under load; not fatal.
    }
  }

  private func isYouTubeURL(_ urlString: String) -> Bool {
    return urlString.contains("youtube.com") || urlString.contains("youtu.be")
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
          DispatchQueue.main.async { [weak self] in
            self?.createVideoMaterial()
          }
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

  @available(iOS 15.0, *)
  private func makeTexture(from pixelBuffer: CVPixelBuffer, cache: CVMetalTextureCache) -> MTLTexture? {
    let width = CVPixelBufferGetWidth(pixelBuffer)
    let height = CVPixelBufferGetHeight(pixelBuffer)

    var cvTextureOut: CVMetalTexture?
    let status = CVMetalTextureCacheCreateTextureFromImage(
      kCFAllocatorDefault,
      cache,
      pixelBuffer,
      nil,
      .bgra8Unorm,
      width,
      height,
      0,
      &cvTextureOut
    )

    guard status == kCVReturnSuccess,
          let cvTexture = cvTextureOut,
          let mtlTexture = CVMetalTextureGetTexture(cvTexture)
    else {
      print("❌ CVMetalTextureCacheCreateTextureFromImage failed: \(status)")
      return nil
    }
    return mtlTexture
  }

  @objc private func createVideoMaterial() {
    if _isObservingStatus, let item = _videoItem {
      item.removeObserver(self, forKeyPath: "status")
      _isObservingStatus = false
    }

    if #available(iOS 15.0, *) {
      guard let device = MTLCreateSystemDefaultDevice() else {
        print("❌ MTLCreateSystemDefaultDevice() returned nil")
        return
      }
      guard let library = try? device.makeDefaultLibrary(bundle: Bundle(for: VideoNode.self)) else {
        print("❌ makeDefaultLibrary(bundle:) returned nil")
        return
      }

      do {
        let surfaceShader = CustomMaterial.SurfaceShader(named: "chromaKeyModifier", in: library)
        var customMat = try CustomMaterial(
          surfaceShader: surfaceShader,
          geometryModifier: nil,
          lightingModel: .lit
        )
        customMat.blending = .transparent(opacity: 1.0)

        _customMaterial = customMat
        _modelEntity.model?.materials = [customMat]

        if let item = _videoItem {
          setupVideoOutput(for: item)
        }

        print("✅ CustomMaterial created, waiting on first decoded frame")
      } catch {
        print("❌ Error configuring custom material: \(error)")
        _container?.form?.dispatchErrorOccurredEvent(
          self,
          "Source",
          ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code,
          "Failed to generate alpha-keyed material layout: \(error.localizedDescription)"
        )
      }
    } else {
      // iOS 14 fallback: opaque video, no chroma key.
      let videoMaterial = VideoMaterial(avPlayer: _player)
      _modelEntity.model?.materials = [videoMaterial]
      print("⚠️ Running on iOS 14: chroma-key transparency unavailable, showing opaque video instead.")
    }
  }

  // Stop the display link / detach the video output when the source changes or node is torn down
  private func teardownVideoOutput() {
    stopDisplayLink()
    if #available(iOS 15.0, *) {
      if let output = _videoOutput, let item = _videoItem {
        item.remove(output)
      }
    }
    _videoOutput = nil
  }

  @objc open var IsPlaying: Bool {
    get { return _player.timeControlStatus == .playing }
  }

  @objc open var Volume: Int32 {
    get { return Int32(round(_player.volume * 100)) }
    set(newVol) { _player.volume = min(max(Float(newVol) / 100.0, 0), 1) }
  }

  @objc open var Loop: Bool {
    get { return _loop }
    set(shouldLoop) { _loop = shouldLoop }
  }

  @objc open func Play() {
    print("▶️ Playing video")
    _player.play()
  }

  @objc open func Pause() {
    print("⏸ Pausing video")
    _player.pause()
  }

  @objc open func SeekTo(_ positionInSeconds: Int32) {
    let targetTime = CMTime(seconds: Double(positionInSeconds), preferredTimescale: 1000)
    _player.seek(to: targetTime, toleranceBefore: .zero, toleranceAfter: .zero)
  }

  @objc open func GetDuration() -> Int32 {
    if let item = _videoItem {
      let duration = CMTimeGetSeconds(item.duration)
      if duration.isNaN || duration.isInfinite { return 0 }
      return Int32(duration)
    }
    return 0
  }

  @objc open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
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

      EnablePhysics(_enablePhysics)
    } else {
      Scale = newScale
    }
  }

}
