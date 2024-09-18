// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class LinearProgress: ViewComponent, AbstractMethodsForViewComponent {
  private var _view: UIProgressView
  private var _progress: Int32 = 0
  private var _isAnimating: Bool = false
  private var _animationTimer: Timer?
  private var _maximum: Int32 = 100
  private var _minimum: Int32 = 0
  public override init(_ parent: ComponentContainer) {
    _view = UIProgressView(progressViewStyle: .bar)
    super.init(parent)
    super.setDelegate(self)
    setupProgressView()
    parent.add(self)
    Width = kLengthFillParent
    Height = 6
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

  // MARK: Properties

  @objc open var Indeterminate: Bool = true {
    didSet {
      IsAnimating = Indeterminate
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
  
  @objc open var Maximum: Int32 {
    get {
      return _maximum
    }
    set(maximum) {
      _maximum = maximum
    }
  }
  
  @objc open var Minimum: Int32 {
    get {
      return _minimum
    }
    set(minimum) {
      _minimum = minimum
    }
  }
  
  @objc open var Progress: Int32 {
    get {
      return _progress
    }
    set(progress) {
      if progress < _minimum {
        _progress = _minimum
      } else if progress > _maximum {
        _progress = _maximum
      } else {
        _progress = progress
        print(_progress)
      }
      
      setProgressValue()
      ProgressChanged(_progress)
    }
  }

  // MARK: Methods

  @objc open func IncrementProgressBy(_ value: Int32) {
    Progress += value
  }

  // MARK: Events

  @objc open func ProgressChanged(_ value: Int32) {
    EventDispatcher.dispatchEvent(of: self, called: "ProgressChanged", arguments: value as NSNumber)
  }

  // MARK: Private implementation

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
    guard _isAnimating else {
      return
    }

    _isAnimating = false
    _animationTimer?.invalidate()
    _animationTimer = nil
  }

  @objc private func updateProgress() {
    _progress += 1

    if _progress >= _maximum {
      _progress = _minimum
    }
    
    setProgressValue()
  }

  private func setProgressValue() {
    _view.setProgress(Float(_progress - _minimum) / Float(_maximum - _minimum), animated: true)
  }
}
