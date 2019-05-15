// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SpriteKit
import AVKit
import SceneKit

@available(iOS 11.3, *)
open class VideoNode: ARNodeBase, ARVideo {
  private var _videoNode: SKVideoNode
  private var _player: AVPlayer
  private var _videoItem: AVPlayerItem? = nil
  private var _videoPlaneNode: SCNNode
  private var _videoPlaneGeometry = SCNPlane(width: 0.5, height: 0.375)
  private var videoScene: SKScene
  
  @objc init(_ container: ARNodeContainer) {
    _player = AVPlayer(playerItem: _videoItem)
    _videoNode = SKVideoNode(avPlayer: _player)
    _videoPlaneNode = SCNNode(geometry: _videoPlaneGeometry)
    videoScene = SKScene(size: CGSize(width: 640, height: 480))
    super.init(container: container, node: _videoPlaneNode)
    setupVideoNode()
    
    NotificationCenter.default.addObserver(self, selector: #selector(playerItemDidPlayToEndTime), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: _videoItem)
    try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
  }
  
  required public init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  @objc private func setupVideoNode() {
    videoScene.scaleMode = .aspectFit
    videoScene.addChild(_videoNode)
    
    _videoNode.position = CGPoint(x: videoScene.size.width/2, y: videoScene.size.height/2)
    _videoNode.size = videoScene.size
    
    _videoPlaneGeometry.firstMaterial?.diffuse.contents = videoScene
    _videoPlaneGeometry.firstMaterial?.isDoubleSided = true
    
    _videoPlaneNode.eulerAngles = SCNVector3(Double.pi, 0, 0)
    
  }
  
  // MARK: Properties
  @objc open var WidthInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_videoPlaneGeometry.width)
    }
    set(width) {
      _videoPlaneGeometry.width = UnitHelper.centimetersToMeters(abs(width))
    }
  }
  
  @objc open var HeightInCentimeters: Float {
    get {
      return UnitHelper.metersToCentimeters(_videoPlaneGeometry.height)
    }
    set(height) {
      _videoPlaneGeometry.height = UnitHelper.centimetersToMeters(abs(height))
    }
  }
  
  @objc open var Source: String {
    get {
      return ""
    }
    set(path) {
      let url = URL(fileURLWithPath: AssetManager.shared.pathForPublicAsset(path))
      do {
        if try url.checkResourceIsReachable() {
          _videoItem = AVPlayerItem(url: url)
          _player.replaceCurrentItem(with: _videoItem)
        } else {
          _container?.form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code, ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message)
        }
      } catch {
        _container?.form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.code, ErrorMessage.ERROR_UNABLE_TO_LOAD_MEDIA.message)
      }
    }
  }
  
  @objc open var IsPlaying: Bool {
    get {
      /// Note: the minimum SDK for the functino is iOS10.  However, the minimum for ARKit is 11+
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
  
  // FillColor is not user accessible
  @objc open override var FillColor: Int32 {
    get {
      return 0
    }
    set(color) {}
  }
  
  // FillColorOpacity is not user accessible
  @objc open override var FillColorOpacity: Int32 {
    get {
      return 1
    }
    set(color) {}
  }
  
  // Texture is not user accessible
  @objc open override var Texture: String {
    get {
      return ""
    }
    set(path) {}
  }
  
  // TextureOpacity is not user accessible
  @objc open override var TextureOpacity: Int32 {
    get {
      return 1
    }
    set(opacity) {}
  }
  
  @objc open func Play() {
    _videoNode.play()
  }
  
  @objc open func Pause() {
    _videoNode.pause()
  }
  
  @objc open func GetDuration() -> Int32 {
    if let item = _videoItem {
      return Int32(CMTimeGetSeconds(item.duration))
    }
    return 0
  }
  
  @objc open func SeekTo(_ ms: Int32) {
    _player.seek(to: CMTime(seconds: Double(ms), preferredTimescale: 1))
    _player.pause()
  }
  
  @objc open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }
  
  @objc func playerItemDidPlayToEndTime() {
    Completed()
    _videoItem?.seek(to: .zero)
  }
}
