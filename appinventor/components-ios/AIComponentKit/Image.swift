//
//  Image.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class Image: ViewComponent, AbstractMethodsForViewComponent {
  private let _view = UIImageView()
  private var _image: UIImage? = nil
  private var _picturePath = ""
  private var _rotationAngle = 0.0
  
  public override init(_ parent: ComponentContainer) {
    _view.isUserInteractionEnabled = true
    super.init(parent)
  }

  public override var view: UIView {
    get {
      return _view
    }
  }
  
  public var Picture: String {
    get {
      return _picturePath
    }
    set(path) {
      _picturePath = path
      if let image = UIImage(named: path) {
        _image = image
        _view.image = image
        _view.setNeedsUpdateConstraints()
        _view.setNeedsLayout()
      }
    }
  }
  
  public var RotationAngle: Double {
    get {
      return _rotationAngle
    }
    set(rotationAngle) {
      if (_rotationAngle == rotationAngle) {
        return  // Don't waste cycles
      }
    }
  }
}
