// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import AVKit
import AVFoundation

fileprivate extension AVPlayerViewController {
  func goFullScreen(_ shouldGoFullScreen: Bool) {
    let selector: String
    let choice = shouldGoFullScreen ? "To": "From"
    if #available(iOS 11.0, *) {
      selector = "_transition\(choice)FullScreenAnimated:completionHandler:"
    } else {
      selector = "_transition\(choice)FullScreenViewControllerAnimated:completionHandler:"
    }

    if self.responds(to: NSSelectorFromString(selector)) {
      self.perform(NSSelectorFromString(selector), with: true, with: nil)
    }
  }
}

open class VideoPlayer: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate let _controller: AVPlayerViewController
  fileprivate let _view = UIView()
  fileprivate var _added = false
  fileprivate let DefaultWidth: Int32 = 175
  fileprivate let DefaultHeight: Int32 = 150

  public override init(_ parent: ComponentContainer) {
    _controller = AVPlayerViewController()
    _controller.showsPlaybackControls = true
    _view.translatesAutoresizingMaskIntoConstraints = false
    _controller.view.translatesAutoresizingMaskIntoConstraints = false
    super.init(parent)
    super.setDelegate(self)
    parent.add(self)
    super.Height = DefaultHeight
    super.Width = DefaultWidth
    _controller.view.frame = _view.bounds
  }

  @objc open var FullScreen: Bool {
    get {
      return isFullScreen()
    }
    set(shouldGoFullScreen) {
      if shouldGoFullScreen != isFullScreen(){
        _controller.goFullScreen(shouldGoFullScreen)
      }
    }
  }

  @objc open override var Height: Int32 {
    get {
      return super.Height
    }
    set(newHeight) {
      setNestedViewHeight(nestedView: _controller.view, height: newHeight, shouldAddConstraints: _added)
    }
  }

  @objc open override var Width: Int32 {
    get {
      return super.Width
    }
    set(newWidth) {
      setNestedViewWidth(nestedView: _controller.view, width: newWidth, shouldAddConstraints: _added)
    }
  }

  fileprivate func isFullScreen() -> Bool {
    return _controller.videoBounds.width != _view.bounds.width && _controller.videoBounds.height != _view.bounds.height
  }

  @objc open var Source: String {
    get {
      return ""
    }
    set(path) {
      let url = URL(fileURLWithPath: AssetManager.shared.pathForPublicAsset(path))
      _controller.player = AVPlayer(url: url)
      if !_added {
        _view.addSubview(_controller.view)
        _controller.view.topAnchor.constraint(equalTo: _view.topAnchor).isActive = true
        _controller.view.leadingAnchor.constraint(equalTo: _view.leadingAnchor).isActive = true
        _controller.view.widthAnchor.constraint(equalTo: _view.widthAnchor).isActive = true
        _controller.view.heightAnchor.constraint(equalTo: _view.heightAnchor).isActive = true
        _added = true
      }
      NotificationCenter.default.addObserver(self, selector: #selector(Completed), name: .AVPlayerItemDidPlayToEndTime, object: _controller.player?.currentItem)
    }
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  @objc open var Volume: Int32 {
    get {
      if let player = _controller.player {
        return Int32(player.volume * 100)
      } else {
        return 0
      }
    }
    set(newVol) {
      if let player = _controller.player {
        var volume = Float(newVol) / 100.0
        if (volume > 1){
          volume = 1
        } else if (volume < 0) {
          volume = 0
        }
        player.volume = volume
      }
    }
  }

  @objc open func GetDuration() -> Int32 {
    if let player = _controller.player, let item = player.currentItem {
      return Int32(CMTimeGetSeconds(item.duration) * 1000.0)
    } else {
      return 0
    }
  }

  @objc open func SeekTo(_ ms: Int32) {
    if let player = _controller.player {
      player.seek(to: CMTime(seconds: Double(ms) / 1000.0, preferredTimescale: 1))
      player.pause()
    }
  }

  @objc open func Start() {
    if let player = _controller.player {
      player.play()
    }
  }

  @objc open func Pause() {
    if let player = _controller.player {
      player.pause()
    }
  }

  @objc open func Stop() {
    if let player = _controller.player {
      player.pause()
      player.seek(to: CMTime(seconds: 0, preferredTimescale: 1))
    }
  }

  @objc open func Completed(){
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }

  // Deprecated
  @objc open func VideoPlayerError(_ message: String) {}
}

