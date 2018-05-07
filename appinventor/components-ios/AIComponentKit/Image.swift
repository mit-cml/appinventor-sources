// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class Image: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate let _view = UIImageView()
  fileprivate var _image: UIImage? = nil
  fileprivate var _picturePath = ""
  fileprivate var _rotationAngle = 0.0
  fileprivate var _scaleToFit = false
  
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
      } else if path.starts(with: "file://") {
        if let image = UIImage(contentsOfFile: path.chopPrefix(count: 7).removingPercentEncoding ?? "") {
          updateImage(image)
        } else {
          updateImage(nil)
        }
      } else if (path.starts(with: "http://") || path.starts(with: "https://")), let url = URL(string: path) {
        URLSession.shared.dataTask(with: url) { data, response, error in
          DispatchQueue.main.async {
            guard let data = data, error == nil else {
              self.updateImage(nil)
              return
            }
            self.updateImage(UIImage(data: data))
            self._container.form.view.setNeedsLayout()
          }
        }.resume()
      } else {
        updateImage(nil)
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

  open var ScalePictureToFit: Bool {
    get {
      return _scaleToFit
    }
    set(scale) {
      if _scaleToFit != scale {
        _scaleToFit = scale
        _view.contentMode = _scaleToFit ? .scaleAspectFit : .topLeft
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
