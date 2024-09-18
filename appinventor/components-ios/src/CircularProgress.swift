// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class CircularProgress: ViewComponent, AbstractMethodsForViewComponent {
  private let _view: CircularProgressView
  
  public override init(_ parent: ComponentContainer) {
    _view = CircularProgressView(frame: CGRect(x: 0, y: 0, width: 50, height: 50))
    super.init(parent)
    super.setDelegate(self)
    parent.add(self)
    Width = 50
    Height = 50
  }
  
  public override var view: UIView {
    get {
      return _view
    }
  }
  
  @objc open var Color: Int32 {
    get {
      return _view.progressLayer.strokeColor as! Int32
    }
    set(argb) {
      _view.progressLayer.strokeColor = argbToColor(argb).cgColor
    }
  }
}

class CircularProgressView: UIView {
  let progressLayer = CAShapeLayer()
  let backgroundLayer = CAShapeLayer()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupViews()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setupViews()
  }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    updateViews()
  }
  
  func setupViews() {
    layer.addSublayer(backgroundLayer)
    layer.addSublayer(progressLayer)
    updateViews()
  }
  
  func updateViews() {
    let center = CGPoint(x: bounds.midX, y: bounds.midY)
    let radius = min(bounds.width, bounds.height) / 2
    let lineWidth: CGFloat = 10
    
    let startAngle: CGFloat = -.pi / 2
    
    let backgroundPath = UIBezierPath(arcCenter: center,
                                      radius: radius - lineWidth / 2,
                                      startAngle: startAngle,
                                      endAngle: startAngle + 2 * .pi,
                                      clockwise: true)
    
    backgroundLayer.path = backgroundPath.cgPath
    backgroundLayer.lineWidth = lineWidth
    backgroundLayer.strokeColor = UIColor.lightGray.cgColor
    backgroundLayer.fillColor = UIColor.clear.cgColor
    
    let progressPath = UIBezierPath(arcCenter: center,
                                    radius: radius - lineWidth / 2,
                                    startAngle: startAngle,
                                    endAngle: startAngle + 2 * .pi,
                                    clockwise: true)
    
    progressLayer.path = progressPath.cgPath
    progressLayer.lineWidth = lineWidth
    progressLayer.strokeColor = UIColor.blue.cgColor
    progressLayer.fillColor = UIColor.clear.cgColor
    progressLayer.strokeEnd = 0
    
    // Update animation
    let animation = CABasicAnimation(keyPath: "strokeEnd")
    animation.fromValue = 0
    animation.toValue = 1
    animation.duration = 1.5
    animation.repeatCount = .infinity
    progressLayer.add(animation, forKey: "progress")
    
    // Update sublayer frames
    backgroundLayer.frame = bounds
    progressLayer.frame = bounds
  }
}
