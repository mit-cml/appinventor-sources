/* -*- mode: swift; swift-mode:basic-offset: 2; -*- */
/**
 * @copyright Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
 */
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
  
  public override init(_ container: ComponentContainer) {
    super.init(container)
  }
  
  open var Source: String {
    get {
      return _sourcePath
    }
    set(path) {
      _sourcePath = path
      if (path == "") {
        if (_audioPlayer != nil) {
          _audioPlayer?.stop()
        }
        _audioPlayer = nil
      } else {
        if let path = Bundle.main.path(forResource: path, ofType: nil) {
          let url = URL(fileURLWithPath: path)
          do {
            _audioPlayer = try AVAudioPlayer(contentsOf:url)
            _audioPlayer?.prepareToPlay()
          } catch {
            _audioPlayer = nil
            _form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
          }
        } else {
          let path = AssetManager.shared.pathForExistingFileAsset(path)
          if path != "" {
            do {
              let url = URL(fileURLWithPath: path)
              _audioPlayer = try AVAudioPlayer(contentsOf:url)
              _audioPlayer?.prepareToPlay()
            } catch {
              _audioPlayer = nil
              _form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
            }
          } else {
            _form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA.code, path)
          }
        }
      }
    }
  }

  open var Loop: Bool {
    get {
      return _loop
    }
    set(shouldLoop) {
      _loop = shouldLoop
    }
  }

  open var IsPlaying: Bool {
    get {
      if let result = _audioPlayer?.isPlaying {
        return result
      } else {
        return false
      }
    }
  }

  open var PlayOnlyInForeground: Bool {
    get {
      return _playOnlyInForeground
    }
    set(shouldForeground) {
      _playOnlyInForeground = shouldForeground
    }
  }

  open var Volume: Int32 {
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
      if let player = _audioPlayer {
        player.volume = Float(vol) / 100.0
      }
    }
  }

  open func Start() {
    if let player = _audioPlayer {
      player.numberOfLoops = _loop ? -1 : 0
      player.play()
    }
  }
  
  open func Pause() {
    if let player = _audioPlayer {
      player.pause()
    }
  }
  
  open func Stop() {
    if let player = _audioPlayer {
      player.stop()
    }
  }
  
  open func Vibrate(_ duration: Int32) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
  }
  
  open func PlayerError(_ message: String) {
    // deprecated
  }

  open func Completed() {
    EventDispatcher.dispatchEvent(of: self, called: "Completed")
  }

  open func OtherPlayerStarted() {
    EventDispatcher.dispatchEvent(of: self, called: "OtherPlayerStarted")
  }

  public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    Completed()
  }

  public func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
    _form?.dispatchErrorOccurredEvent(self, "Source", ErrorMessages.ERROR_UNABLE_TO_PREPARE_MEDIA.code, _sourcePath)
  }

  open func onResume() {
  }

  open func onPause() {
  }

  open func onDestroy() {
  }

  open func onDelete() {
  }

  private func prepareToDie() {
    if let player = _audioPlayer {
      player.stop()
      _audioPlayer = nil
    }
  }
}
