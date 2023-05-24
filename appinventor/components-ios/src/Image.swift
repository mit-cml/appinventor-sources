// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class Image: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate let _view = UIImageView()
  fileprivate var _image: UIImage? = nil
  fileprivate var _picturePath = ""
  fileprivate var _rotationAngle = 0.0
  fileprivate var _scaleToFit = true
  fileprivate var _stoppedPosition: CGFloat? = nil
  fileprivate var _clickable: Bool = false
  
  public override init(_ parent: ComponentContainer) {
    _view.translatesAutoresizingMaskIntoConstraints = false
    super.init(parent)
    setDelegate(self)
    parent.add(self)
    ScalePictureToFit = false
    let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(click))
    _view.isUserInteractionEnabled = true
    _view.addGestureRecognizer(tapGestureRecognizer)
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  /**
   * Applies or cancels a horizontal scrolling animation
   */
  @objc open var Animation: String {
    get {
      return ""
    }
    set(animation) {
      switch animation {
      case "ScrollRightSlow":
        ApplyScrollAnimation(false, 8)
      case "ScrollRight":
        ApplyScrollAnimation(false, 4)
      case "ScrollRightFast":
        ApplyScrollAnimation(false, 1)
      case "ScrollLeftSlow":
        ApplyScrollAnimation(true, 8)
      case "ScrollLeft":
        ApplyScrollAnimation(true, 4)
      case "ScrollLeftFast":
        ApplyScrollAnimation(true, 1)
      case "Stop":
        // we want to save the current position
        _stoppedPosition = _view.frame.origin.x - (_view.layer.presentation()?.frame.origin.x ?? _view.frame.origin.x)
        _view.layer.removeAllAnimations()
      default:
        NSLog("Invalid animation provided: \(animation)")
      }
    }
  }

  // applies an appropriate animation
  fileprivate func ApplyScrollAnimation(_ left: Bool, _ duration: TimeInterval) {
    _stoppedPosition = nil
    let sign: CGFloat = left ? -1: 1
    let dx = sign * (_view.superview?.frame.width ?? 1) * 0.75
    let angle = CGFloat(self._rotationAngle) * .pi / 180
    transform(-dx, angle)
    UIView.animate(withDuration: duration, delay: 0, options: .curveEaseInOut, animations: {
      self.transform(2 * dx, angle)
    }, completion: { completed in
      // the rotation angle might have changed mid animation
      let finalAngle = CGFloat(self._rotationAngle) * .pi / 180
      if completed {
        self.transform(-dx, finalAngle)
      } else if let position = self._stoppedPosition {
        self.transform(-position, finalAngle)
      }
    })
  }

  /**
   * Translates a rotated view horizontally
   * We have to disable the transform before translating and restore it after
   */
  fileprivate func transform(_ dx: CGFloat, _ angle: CGFloat) {
    _view.transform = CGAffineTransform.identity.rotated(by: 0)
    _view.frame.origin.x += dx
    _view.transform = CGAffineTransform.identity.rotated(by: angle)
  }
  
  @objc public func click() {
    Click()
  }

  @objc open var Picture: String {
    get {
      return _picturePath
    }
    set(path) {
      _picturePath = path
      if let image = AssetManager.shared.imageFromPath(path: path) {
        updateImage(image)
      } else if (path.starts(with: "http://") || path.starts(with: "https://")), let url = URL(string: path) {
        URLSession.shared.dataTask(with: url) { data, response, error in
          DispatchQueue.main.async {
            guard let data = data, error == nil else {
              self.updateImage(nil)
              return
            }
            self.updateImage(UIImage(data: data))
            self._container?.form?.view.setNeedsLayout()
          }
        }.resume()
      } else {
        updateImage(nil)
      }
      _container?.form?.view.setNeedsLayout()
      NSLog("Image size: \(_view.frame)")
    }
  }

  @objc open var RotationAngle: Double {
    get {
      return _rotationAngle
    }
    set(rotationAngle) {
      if (_rotationAngle == rotationAngle) {
        return  // Don't waste cycles
      }
      _rotationAngle = rotationAngle
      _view.transform = CGAffineTransform.identity.rotated(by: CGFloat(RotationAngle) * .pi / 180)
    }
  }

  @objc open var ScalePictureToFit: Bool {
    get {
      return _scaleToFit
    }
    set(scale) {
      if _scaleToFit != scale {
        _scaleToFit = scale
        _view.contentMode = _scaleToFit ? .scaleToFill : .scaleAspectFit
      }
    }
  }
  
  /**
   * Whether the image is clickable.
   */
  @objc open var Clickable: Bool {
    get {
      return _clickable
    }
    set(clickable) {
      _clickable = clickable
    }
  }
  
  @objc public func Click() {
    if _clickable {
      EventDispatcher.dispatchEvent(of: self, called: "Click")
    }
  }
  // Deprecated
  @objc open var Scaling: Int32 = 0

  private func updateImage(_ image: UIImage?) {
    if let image = image {
      _image = image
      _view.image = image
      _view.frame.size = image.size
    } else {
      _image = nil
      _view.image = nil
      _view.frame.size = .zero
    }
    _view.invalidateIntrinsicContentSize()
    _view.setNeedsUpdateConstraints()
    _view.setNeedsLayout()
  }
}
