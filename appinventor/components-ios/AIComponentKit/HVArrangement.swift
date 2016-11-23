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

public class HVArrangement: ViewComponent, ComponentContainer, AbstractMethodsForViewComponent {
  private var _components: [Component] = [Component]()
  private var _view: UIScrollView
  private let _orientation: HVOrientation
  private var _horizontalAlign = HorizontalGravity.left
  private var _verticalAlign = VerticalGravity.top
  private var _backgroundColor = UIColor.white
  private var _imagePath = ""
  
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
  public override var view: UIView {
    get {
      return _view
    }
  }

  // MARK: ComponentContainer protocol implementation
  public var form: Form? {
    get {
      return _container.form
    }
  }

  public func add(_ component: ViewComponent) {
    _components.append(component)
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    if width > 0 {
      
    } else if width == 0 {
      
    } else if width == kLengthPreferred {

    } else if width == kLengthFillParent {

    } else if width <= kLengthPercentTag {
      
    }
  }

  public func setChildHeight(of component: ViewComponent, height: Int32) {
    if height > 0 {
      
    } else if height == 0 {
      
    } else if height == kLengthPreferred {
      
    } else if height == kLengthFillParent {
      
    } else if height <= kLengthPercentTag {
      
    }
  }

  // MARK: HVArrangement Properties
  public var AlignHorizontal: Int32 {
    get {
      return _horizontalAlign.rawValue
    }
    set(align) {
      updateConstraints()
    }
  }

  public var AlignVertical: Int32 {
    get {
      return _verticalAlign.rawValue
    }
    set(align) {
      updateConstraints()
    }
  }

  public var BackgroundColor: Int32 {
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

  public var Image: String {
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
  private func updateConstraints() {
    
  }

  private func updateConstraints(for view: UIView) {
  }
  
  private func updateHorizontalConstraints() {
    
  }
  
  private func updateVerticalConstraints() {
    
  }
  
  private func updateHorizontalConstraints(for view: UIView) {
    let constraints = _view.constraintsAffectingLayout(for: UILayoutConstraintAxis.horizontal)
    if _orientation == .horizontal {
      
    }
  }

  private func updateVerticalConstraints(for view: UIView) {
    let constraints = _view.constraintsAffectingLayout(for: UILayoutConstraintAxis.vertical)
    if _orientation == .vertical {
      
    }
  }
}
