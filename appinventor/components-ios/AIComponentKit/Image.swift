//
//  Image.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class Image: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate let _view = UIImageView()
  fileprivate var _image: UIImage? = nil
  fileprivate var _picturePath = ""
  fileprivate var _rotationAngle = 0.0
  
  public override init(_ parent: ComponentContainer) {
    _view.isUserInteractionEnabled = true
    _view.translatesAutoresizingMaskIntoConstraints = false
    super.init(parent)
    parent.add(self)
  }

  open override var view: UIView {
    get {
      return _view
    }
  }
  
  open var Picture: String {
    get {
      return _picturePath
    }
    set(path) {
      _picturePath = path
      if path.isEmpty {
        updateImage(nil)
      } else if let image = UIImage(contentsOfFile: AssetManager.shared.pathForExistingFileAsset(path)) {
        updateImage(image)
      } else if let image = UIImage(named: path) {
        updateImage(image)
      } else if let image = UIImage(contentsOfFile: path) {
        updateImage(image)
      } else {
        do {
          if let url = URL(string: path), let image = try UIImage(data: Data(contentsOf: url)) {
            updateImage(image)
          } else {
            updateImage(nil)
          }
        } catch {
          updateImage(nil)
        }
      }
      _container.form.view.setNeedsLayout()
      NSLog("Image size: \(_view.frame)")
    }
  }

  open var RotationAngle: Double {
    get {
      return _rotationAngle
    }
    set(rotationAngle) {
      if (_rotationAngle == rotationAngle) {
        return  // Don't waste cycles
      }
    }
  }

  private func updateImage(_ image: UIImage?) {
    if let image = image {
      _image = image
      _view.image = image
      _view.frame.size = image.size
    } else {
      _image = nil
      _view.image = nil
      _view.frame.size = CGSize(width: 0, height: 0)
    }
    _view.invalidateIntrinsicContentSize()
    _view.setNeedsUpdateConstraints()
    _view.setNeedsLayout()
  }

}
