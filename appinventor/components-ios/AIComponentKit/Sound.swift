//
//  Sound.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import AVKit

public class Sound: NonvisibleComponent {
  private var sourcePath: String = ""
  private var minimumInterval: Int32 = 1
  private var audioPlayer: AVAudioPlayer?

  public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  public var Source: String {
    get {
      return sourcePath
    }
    set(path) {
      sourcePath = path
      if (path == "") {
        if (audioPlayer != nil) {
          audioPlayer?.stop()
        }
        audioPlayer = nil
      } else {
        let path = Bundle.main.path(forResource: path, ofType: nil)
        if (path == nil) {
          return;
        }
        let url = URL(fileURLWithPath: path!)
        do {
          audioPlayer = try AVAudioPlayer(contentsOf:url)
          audioPlayer?.prepareToPlay()
        } catch {
          NSLog("Error loading audio")
        }
      }
    }
  }
  
  public var MinimumInterval: Int32 {
    get {
      return minimumInterval
    }
    set {
      
    }
  }
  
  public func Play() {
    if (audioPlayer != nil) {
      audioPlayer?.play()
    }
  }
  
  public func Pause() {
    if (audioPlayer != nil) {
      audioPlayer?.pause()
    }
  }
  
  public func Stop() {
    if (audioPlayer != nil) {
      audioPlayer?.stop()
    }
  }
  
  public func Vibrate(duration: Int32) {
    
  }
  
  public func SoundError(message: String) {
    // deprecated
  }
}
