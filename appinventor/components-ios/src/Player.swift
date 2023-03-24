// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/* -*- mode: swift; swift-mode:basic-offset: 2; -*- */
/* Copyright © 2017-2023 Massachusetts Institute of Technology, All rights reserved. */
/**
 * @file Player.swift Implementation of the MIT App Inventor Player
 * component for iOS.
 */

import Foundation
import AVKit

private let kMaxPlayDelayRetries: Int32 = 10
private let kPlayDelayLength = TimeInterval(0.050)

enum PlayerState {
  case Initial
  case Prepared
  case Playing
  case PausedByUser
  case PausedByEvent
  case Error
}

/**
 * Multimedia component that plays audio and optionally vibrate. It is built on top of AVKit.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
open class Player: NonvisibleComponent, LifecycleDelegate {
  fileprivate var _sourcePath: String = ""
  private var _player: AVPlayer? = nil
  fileprivate var _loop: Bool = false
  fileprivate var _playOnlyInForeground: Bool = false
  fileprivate var _wasPlaying = false
  private var state = PlayerState.Initial
  private var observer: NSObjectProtocol? = nil
  private var pendingPlay = false
  private var context = 0

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // MARK: Properties

  @objc open var Source: String {
    get {
      return _sourcePath
    }
    set(path) {
      _sourcePath = path
      state = .Initial
      if let player = _player, let observer = observer {
        NotificationCenter.default.removeObserver(observer, name: .AVPlayerItemDidPlayToEndTime,
            object: player.currentItem)
      }
      guard !path.isEmpty else {
        _player?.pause()
        _player = nil
        return
      }
      var resourceUrl: URL!
      if path.starts(with: "http:") || path.starts(with: "https:") {
        guard let url = URL(string: path) else {
          _form?.dispatchErrorOccurredEvent(self, "Source",
              ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA, path)
          return
        }
        resourceUrl = url
      } else if let path = Bundle.main.path(forResource: path, ofType: nil) {
        resourceUrl = URL(fileURLWithPath: path)
      } else {
        let path = AssetManager.shared.pathForExistingFileAsset(path)
        guard !path.isEmpty else {
          _form?.dispatchErrorOccurredEvent(self, "Source",
              ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
          return
        }
        resourceUrl = URL(fileURLWithPath: path)
      }
      let player = AVPlayer(url: resourceUrl)
      observer = NotificationCenter.default.addObserver(forName: .AVPlayerItemDidPlayToEndTime,
          object: player.currentItem, queue: .main) { [weak self] _ in
        self?.Completed()
        if self?._loop == true {
          self?._player?.seek(to: CMTime.zero)
          self?._player?.play()
        } else {
          self?._wasPlaying = false
        }
      }
      _player = player
      player.addObserver(self, forKeyPath: #keyPath(AVPlayer.status), options: [.new], context: &self.context)
    }
  }

  @objc open var Loop: Bool {
    get {
      return _loop
    }
    set(shouldLoop) {
      _loop = shouldLoop
    }
  }

  @objc open var IsPlaying: Bool {
    get {
      if #available(iOS 10.0, *) {
        return _player?.timeControlStatus == AVPlayer.TimeControlStatus.playing
      } else {
        // Fallback on earlier versions
        return _wasPlaying
      }
    }
  }

  @objc open var PlayOnlyInForeground: Bool {
    get {
      return _playOnlyInForeground
    }
    set(shouldForeground) {
      _playOnlyInForeground = shouldForeground
    }
  }

  @objc open var Volume: Int32 {
    get {
      if let fVolume = _player?.volume {
        return Int32(fVolume * 100.0)
      } else {
        return 0
      }
    }
    set(vol) {
      var vol = vol
      if (vol < 0) {
        vol = 0
      } else if (vol > 100) {
        vol = 100
      }
      _player?.volume = Float(vol) / 100.0
    }
  }

  // MARK: Methods

  @objc open func Start() {
    guard let player = _player else {
      return
    }
    guard state == .Prepared || state == .PausedByUser || state == .PausedByEvent else {
      if let error = player.error {
        state = .Error
        print("Unable to prepare to play audio: \(error)")
        _form?.dispatchErrorOccurredEvent(self, "Start", ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA,
            _sourcePath)
      } else {
        self.pendingPlay = true
      }
      return
    }
    if #available(iOS 10.0, *) {
      // We need to switch the audiosession to playback mode in case the phone is in silent mode
      // Otherwise, no sound will play.
      try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
    }
    pendingPlay = false
    _wasPlaying = true
    player.play()
    state = .Playing
  }
  
  @objc open func Pause() {
    guard state == .Playing || state == .PausedByUser || state == .PausedByEvent else {
      return
    }
    guard let player = _player else {
      return
    }
    player.pause()
    _wasPlaying = false
    state = .PausedByUser
  }
  
  @objc open func Stop() {
    guard state == .Playing || state == .PausedByUser || state == .PausedByEvent else {
      return
    }
    guard let player = _player else {
      return
    }
    player.pause()
    player.seek(to: CMTime.zero)
    _wasPlaying = false
    state = .PausedByUser
  }
  
  @objc open func Vibrate(_ duration: Int32) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
  }

  // MARK: Events
  
  @objc open func PlayerError(_ message: String) {
    // deprecated
  }

  @objc open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }

  @objc open func OtherPlayerStarted() {
    EventDispatcher.dispatchEvent(of: self, called: "OtherPlayerStarted")
  }

  // MARK: Key-Value Observation Implementation

  open override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
    guard context == &self.context else {
      super.observeValue(forKeyPath: keyPath, of: object, change: change, context: context)
      return
    }
    if keyPath == #keyPath(AVPlayer.status) {
      let status: AVPlayer.Status
      if let statusNumber = change?[.newKey] as? NSNumber {
        status = AVPlayer.Status(rawValue: statusNumber.intValue)!
      } else {
        status = .unknown
      }
      switch status {
      case .readyToPlay:
        self.state = .Prepared
        if self.pendingPlay {
          DispatchQueue.main.async {
            self.Start()
          }
        }
        break
      case .failed:
        self.state = .Error
        _form?.dispatchErrorOccurredEvent(self, "Source",
            ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA, _sourcePath)
        break
      default:
        self.state = .Initial
        break
      }
    }
  }

  // MARK: LifecycleDelegate Implementation

  @objc open func onResume() {
    if _wasPlaying && _playOnlyInForeground, let player = _player {
      player.play()
      state = .Playing
    }
  }

  @objc open func onPause() {
    if _playOnlyInForeground, let player = _player {
      player.pause()
      state = .PausedByEvent
    }
  }

  @objc open func onDestroy() {
  }

  @objc open func onDelete() {
    if let player = _player {
      player.pause()
      state = .PausedByEvent
    }
  }

  private func prepareToDie() {
    guard let player = _player else {
      return
    }
    player.pause()
    state = .PausedByEvent
    _player = nil
  }
}
