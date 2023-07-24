// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class LinearProgress: ViewComponent, AbstractMethodsForViewComponent {
  private var _view: UIProgressView
  private var _progress: Float = 0.0
  private var _isAnimating: Bool = false
  private var _animationTimer: Timer?
  private var _maximum: Int = 100
  private var _minimum: Int = 0
  public override init(_ parent: ComponentContainer) {
    _view = UIProgressView(progressViewStyle: .bar)
    super.init(parent)
    super.setDelegate(self)
    setupProgressView()
    parent.add(self)
  }
  
  private func setupProgressView() {
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.progressTintColor = UIColor.blue
    _view.trackTintColor = UIColor.lightGray
    _view.setProgress(0.0, animated: false)
    _isAnimating = true
  }
  
  public override var view: UIView {
    get {
      return _view
    }
  }
  
  @objc open var IndeterminateColor: Int32 {
    get {
      return _view.progressTintColor?.cgColor as! Int32
    }
    set(argb) {
      
      _view.progressTintColor = argbToColor(argb)
    }
  }
  
  @objc open var ProgressColor: Int32 {
    get {
      return _view.progressTintColor?.cgColor as! Int32
    }
    set(argb) {
      
      _view.progressTintColor = argbToColor(argb)
    }
  }
  
  @objc open var Maximum: Int {
    get {
      return _maximum
    }
    set(maximum) {
      _maximum = maximum
    }
  }
  
  @objc open var Minimum: Int {
    get {
      return _minimum
    }
    set(minimum) {
      _minimum = minimum
    }
  }
  
  @objc open var Progress: Int {
    get {
      return 0
    }
    set(progress) {
      if progress < _minimum {
        _progress = 0.0
      } else if progress > _maximum {
        _progress = 1.0
      } else {
        _progress = Float(progress) / Float(_maximum + _minimum)
        print(_progress)
      }
      
      setProgressValue()
    }
  }
  
  public var IsAnimating: Bool {
    get {
      return _isAnimating
    }
    set(isAnimating) {
      if isAnimating == true {
        startAnimating()
      } else {
        stopAnimating()
      }
    }
  }
  
  private func startAnimating() {
    if _isAnimating == true {
      return
    }
    
    _isAnimating = true
    _animationTimer = Timer.scheduledTimer(timeInterval: 0.05, target: self, selector: #selector(updateProgress), userInfo: nil, repeats: true)
  }
  
  private func stopAnimating() {
    if _isAnimating == false {
      return
    }
    
    _isAnimating = false
    _animationTimer?.invalidate()
    _animationTimer = nil
  }
  
  @objc private func updateProgress() {
    _progress += 0.01
    
    if _progress >= 1.0 {
      _progress = 1.0
      stopAnimating()
    }
    
    setProgressValue()
  }
  
  private func setProgressValue() {
    _view.setProgress(_progress, animated: true)
  }
}
