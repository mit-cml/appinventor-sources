//
//  Sound.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Sound: NonvisibleComponent {
  private var sourcePath: String = ""
  private var minimumInterval: Int32 = 1

  public init(container: ComponentContainer) {
    super.init(dispatcher: container.form.dispatchDelegate)
  }

  public var Source: String {
    get {
      return sourcePath
    }
    set(path) {
      sourcePath = path
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
    
  }
  
  public func Pause() {
    
  }
  
  public func Stop() {
    
  }
  
  public func Vibrate(duration: Int32) {
    
  }
  
  public func SoundError(message: String) {
    // deprecated
  }
}
