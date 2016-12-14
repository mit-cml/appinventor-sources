//
//  HVArrangement.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/30/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

private let kHorizontalCenterLeadingConstraint = "HCenterLeadingConstraint"
private let kHorizontalCenterTrailingConstraint = "HCenterTrailingConstraint"
private let kVerticalCenterLeadingConstraint = "VCenterLeadingConstraint"
private let kVerticalCenterTrailingConstraint = "VCenterTrailingConstraint"
private let kComponentKitConstraint = "AIComponentKitConstraint"

open class HVArrangement: ViewComponent, ComponentContainer, AbstractMethodsForViewComponent {
  fileprivate var _components: [Component] = [Component]()
  fileprivate var _view: UIScrollView
  fileprivate let _orientation: HVOrientation
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _backgroundColor = UIColor.white
  fileprivate var _imagePath = ""
  
  public init(_ parent: ComponentContainer, orientation: HVOrientation, scrollable: Bool) {
    _orientation = orientation
    _view = UIScrollView()
    _view.isScrollEnabled = scrollable
    _view.isDirectionalLockEnabled = true
    if scrollable {
      switch orientation {
      case .horizontal:
        _view.showsHorizontalScrollIndicator = true
        break
      case .vertical:
        _view.showsVerticalScrollIndicator = true
        break
      }
    }
    super.init(parent)
    super.setDelegate(self)
    parent.add(self)
  }

  // MARK: AbstractMethodsForViewComponent protocol implementation
  open override var view: UIView {
    get {
      return _view
    }
  }

  // MARK: ComponentContainer protocol implementation
  open var form: Form? {
    get {
      return _container.form
    }
  }

  open func add(_ component: ViewComponent) {
    _components.append(component)
  }

  open func setChildWidth(of component: ViewComponent, width: Int32) {
    if width > 0 {
      
    } else if width == 0 {
      
    } else if width == kLengthPreferred {

    } else if width == kLengthFillParent {

    } else if width <= kLengthPercentTag {
      
    }
  }

  open func setChildHeight(of component: ViewComponent, height: Int32) {
    if height > 0 {
      
    } else if height == 0 {
      
    } else if height == kLengthPreferred {
      
    } else if height == kLengthFillParent {
      
    } else if height <= kLengthPercentTag {
      
    }
  }

  // MARK: HVArrangement Properties
  open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlign.rawValue
    }
    set(align) {
      updateConstraints()
    }
  }

  open var AlignVertical: Int32 {
    get {
      return _verticalAlign.rawValue
    }
    set(align) {
      updateConstraints()
    }
  }

  open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set(argb) {
      _backgroundColor = argbToColor(argb)
      if _imagePath != "" {
        _view.backgroundColor = _backgroundColor
      }
    }
  }

  open var Image: String {
    get {
      return _imagePath
    }
    set(path) {
      if path == _imagePath {
        // Already using this image
        return
      } else if path != "" {
        let image = AssetManager.shared.imageFromPath(path: path)
        if image != nil {
          _view.backgroundColor = UIColor(patternImage: image!)
          return
        }
      }
      _imagePath = ""
      _view.backgroundColor = _backgroundColor
    }
  }

  // MARK: Private implementation
  fileprivate func updateConstraints() {
    
  }

  fileprivate func updateConstraints(for view: UIView) {
  }
  
  fileprivate func updateHorizontalConstraints() {
    
  }
  
  fileprivate func updateVerticalConstraints() {
    
  }
  
  fileprivate func updateHorizontalConstraints(for view: UIView) {
    let constraints = _view.constraintsAffectingLayout(for: UILayoutConstraintAxis.horizontal)
    if _orientation == .horizontal {
      
    }
  }

  fileprivate func updateVerticalConstraints(for view: UIView) {
    let constraints = _view.constraintsAffectingLayout(for: UILayoutConstraintAxis.vertical)
    if _orientation == .vertical {
      
    }
  }
}
