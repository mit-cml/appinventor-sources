// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
  
  @objc open var MinimumInterval: Int32 {
    get {
      return _minimumInterval
    }
    set(interval) {
      _minimumInterval = interval
    }
  }
  
  @objc open func Play() {
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

  @objc open func Pause() {
    _audioPlayer?.pause()
  }

  public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    if flag {
      _started = false
    }
  }

  @objc open func Resume() {
    if let audio = _audioPlayer, !audio.isPlaying,
      audio.currentTime != audio.duration, _started {
      audio.play()
    }
  }
  
  @objc open func Stop() {
    _started = false
    _audioPlayer?.stop()
  }
  
  @objc open func Vibrate(_ duration: Int32) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
  }
  
  @objc open func SoundError(_ message: String) {
    // deprecated
  }
}
