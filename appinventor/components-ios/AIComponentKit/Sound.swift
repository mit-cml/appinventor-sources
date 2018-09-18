//
//  Sound.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import AVKit

private let kMaxPlayDelayRetries: Int32 = 10
private let kPlayDelayLength = TimeInterval(0.050)

open class Sound: NonvisibleComponent, AVAudioPlayerDelegate {
  fileprivate var _sourcePath: String = ""
  fileprivate var _minimumInterval: Int32 = 500
  fileprivate var _timeLastPlayed: Double = 0.0
  fileprivate var _audioPlayer: AVAudioPlayer?
  fileprivate var _delayRetries: Int32 = 0
  fileprivate var _started: Bool = false

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  open var Source: String {
    get {
      return _sourcePath
    }
    set(path) {
      _sourcePath = path
      if (path.isEmpty) {
        _audioPlayer?.stop()
        _audioPlayer = nil
      } else {
        let soundPath = Bundle.main.path(forResource: path, ofType: nil) ??
          AssetManager.shared.pathForExistingFileAsset(path)
        let url = URL(fileURLWithPath: soundPath)
        do {
          _audioPlayer = try AVAudioPlayer(contentsOf:url)
          _audioPlayer?.prepareToPlay()
          _audioPlayer?.delegate = self
        } catch {
          NSLog("Error loading audio")
        }
      }
    }
  }
  
  open var MinimumInterval: Int32 {
    get {
      return _minimumInterval
    }
    set(interval) {
      _minimumInterval = interval
    }
  }
  
  open func Play() {
    let currentTime = Date().timeIntervalSince1970
    if (_timeLastPlayed == 0.0 || currentTime >= _timeLastPlayed + Double(_minimumInterval)/1000.0) {
      _timeLastPlayed = currentTime
      _delayRetries = kMaxPlayDelayRetries
      playWhenLoadComplete()
    } else {
      NSLog("Unable to play because MinimumInterval has not elapsed since last play.")
    }
  }

  fileprivate func playWhenLoadComplete() {
    _started = true
    _audioPlayer?.play()
  }

  open func Pause() {
    _audioPlayer?.pause()
  }

  public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    if flag {
      _started = false
    }
  }

  open func Resume() {
    if let audio = _audioPlayer, !audio.isPlaying,
      audio.currentTime != audio.duration, _started {
      audio.play()
    }
  }
  
  open func Stop() {
    _started = false
    _audioPlayer?.stop()
  }
  
  open func Vibrate(_ duration: Int32) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
  }
  
  open func SoundError(_ message: String) {
    // deprecated
  }
}
