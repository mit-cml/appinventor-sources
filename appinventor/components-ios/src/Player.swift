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
import AVFoundation

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
open class Player: NonvisibleComponent, LifecycleDelegate, AVAudioPlayerDelegate {
  fileprivate var _sourcePath: String = ""
  private var _player: AVAudioPlayer? = nil
  fileprivate var _loop: Bool = false
  fileprivate var _playOnlyInForeground: Bool = false
  fileprivate var _wasPlaying = false
  private var state = PlayerState.Initial
  private var pendingPlay = false
  private var context = 0
  private var _userVolume: Int32 = 50

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
      if let player = _player {
        player.delegate = nil
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
      do {
        let player = try AVAudioPlayer(contentsOf: resourceUrl)
        _player = player
        player.numberOfLoops = Loop ? -1 : 0
        player.volume = Float(_userVolume) / 100.0
        player.delegate = self
        if player.prepareToPlay() {
          state = .Prepared
        }
      } catch {
        print("Foo: \(error)")
      }
    }
  }

  @objc open var Loop: Bool {
    get {
      return _loop
    }
    set(shouldLoop) {
      _loop = shouldLoop
      if let player = _player {
        player.numberOfLoops = shouldLoop ? -1 : 0
      }
    }
  }

  @objc open var IsPlaying: Bool {
    get {
      return _player?.isPlaying ?? false
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
        return _userVolume
      }
    }
    set(vol) {
      var vol = vol
      if (vol < 0) {
        vol = 0
      } else if (vol > 100) {
        vol = 100
      }
      _userVolume = vol
      _player?.volume = Float(vol) / 100.0
    }
  }

  // MARK: Methods

  @objc open func Start() {
    guard let player = _player else {
      return
    }
    guard state == .Prepared || state == .PausedByUser || state == .PausedByEvent else {
      if state == .Error {
        state = .Error
        print("Unable to prepare to play audio")
        _form?.dispatchErrorOccurredEvent(self, "Start", ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA,
            _sourcePath)
      } else {
        self.pendingPlay = true
      }
      return
    }
    // We need to switch the audiosession to playback mode in case the phone is in silent mode
    // Otherwise, no sound will play.
    try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
    try? AVAudioSession.sharedInstance().setActive(true)
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
    player.currentTime = 0
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

  // MARK: AVAudioPlayerDelegate

  public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    state = .Prepared
    DispatchQueue.main.async {
      self.Completed()
    }
  }

  public func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
    guard let error = error else {
      return
    }
    self.state = .Error
    DispatchQueue.main.async {
      self.PlayerError("\(error)")
    }
  }
}
