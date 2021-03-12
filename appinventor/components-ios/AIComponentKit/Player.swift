/* -*- mode: swift; swift-mode:basic-offset: 2; -*- */
/* Copyright © 2017-2021 Massachusetts Institute of Technology, All rights reserved. */
/**
 * @file Player.swift Implementation of the MIT App Inventor Player
 * component for iOS.
 */

import Foundation
import AVKit

private let kMaxPlayDelayRetries: Int32 = 10
private let kPlayDelayLength = TimeInterval(0.050)

/**
 * Multimedia component that plays audio and optionally vibrate. It is built on top of AVKit.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
open class Player: NonvisibleComponent, AVAudioPlayerDelegate, LifecycleDelegate {
  fileprivate var _sourcePath: String = ""
  fileprivate var _audioPlayer: AVAudioPlayer?
  fileprivate var _loop: Bool = false
  fileprivate var _playOnlyInForeground: Bool = false
  fileprivate var _wasPlaying = false
  
  public override init(_ container: ComponentContainer) {
    super.init(container)
    if #available(iOS 10.0, *) {
      // We need to switch the audiosession to playback mode in case the phone is in silent mode
      // Otherwise, no sound will play.
      try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [])
    }
  }
  
  @objc open var Source: String {
    get {
      return _sourcePath
    }
    set(path) {
      _sourcePath = path
      if (path.isEmpty) {
        _audioPlayer?.stop()
        _audioPlayer = nil
      } else {
        if let path = Bundle.main.path(forResource: path, ofType: nil) {
          let url = URL(fileURLWithPath: path)
          do {
            _audioPlayer = try AVAudioPlayer(contentsOf:url)
            _audioPlayer?.prepareToPlay()
          } catch {
            _audioPlayer = nil
            _form?.dispatchErrorOccurredEvent(self, "Source",
                ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
          }
        } else {
          let path = AssetManager.shared.pathForExistingFileAsset(path)
          if !path.isEmpty {
            do {
              let url = URL(fileURLWithPath: path)
              _audioPlayer = try AVAudioPlayer(contentsOf:url)
              _audioPlayer?.prepareToPlay()
            } catch {
              _audioPlayer = nil
              _form?.dispatchErrorOccurredEvent(self, "Source",
                  ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
            }
          } else {
            _form?.dispatchErrorOccurredEvent(self, "Source",
                ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
          }
        }
      }
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
      return _audioPlayer?.isPlaying ?? false
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
      if let fVolume = _audioPlayer?.volume {
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
      _audioPlayer?.volume = Float(vol) / 100.0
    }
  }

  @objc open func Start() {
    _audioPlayer?.numberOfLoops = _loop ? -1 : 0
    _audioPlayer?.delegate = self
    _wasPlaying = true
    _audioPlayer?.play()
  }
  
  @objc open func Pause() {
    _audioPlayer?.pause()
  }
  
  @objc open func Stop() {
    _audioPlayer?.stop()
  }
  
  @objc open func Vibrate(_ duration: Int32) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
  }
  
  @objc open func PlayerError(_ message: String) {
    // deprecated
  }

  @objc open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }

  @objc open func OtherPlayerStarted() {
    EventDispatcher.dispatchEvent(of: self, called: "OtherPlayerStarted")
  }

  public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    _wasPlaying = false
    Completed()
  }

  public func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
    _form?.dispatchErrorOccurredEvent(self, "Source",
        ErrorMessage.ERROR_UNABLE_TO_PREPARE_MEDIA.code, _sourcePath)
  }

  @objc open func onResume() {
    if _wasPlaying && _playOnlyInForeground {
      _audioPlayer?.play()
    }
  }

  @objc open func onPause() {
    if _playOnlyInForeground {
      _audioPlayer?.pause()
    }
  }

  @objc open func onDestroy() {
  }

  @objc open func onDelete() {
  }

  private func prepareToDie() {
    _audioPlayer?.stop()
    _audioPlayer = nil
  }
}
