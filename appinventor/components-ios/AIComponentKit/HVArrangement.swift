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
  fileprivate var _components: [ViewComponent] = [ViewComponent]()
  fileprivate var _view = LinearView()
  fileprivate var _orientation = HVOrientation.vertical
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _backgroundColor = UIColor.white
  fileprivate var _imagePath = ""
  fileprivate var _lastConstraint: NSLayoutConstraint! = nil

  public init(_ parent: ComponentContainer, orientation: HVOrientation, scrollable: Bool) {
    _orientation = orientation
    super.init(parent)
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.isScrollEnabled = scrollable
    _view.orientation = orientation
    _view.isScrollEnabled = scrollable
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
  open var form: Form {
    get {
      return _container.form
    }
  }

  open func add(_ component: ViewComponent) {
    _components.append(component)
    _view.addItem(LinearViewItem(component.view))
  }

  open func setChildWidth(of component: ViewComponent, width: Int32) {
    let child = component.view
    if width >= 0 {
      let constraint = child.widthAnchor.constraint(equalToConstant: CGFloat(width))
      constraint.priority = UILayoutPriorityRequired
      view.addConstraint(constraint)
    } else if width == kLengthPreferred {

      view.addConstraint(NSLayoutConstraint(item: view, attribute: .width, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width <= kLengthPercentTag {
      let width = -(width + 1000)
      let pWidth = CGFloat(width) / CGFloat(100.0)
      form.view.addConstraint(child.widthAnchor.constraint(equalTo: form.view.widthAnchor, multiplier: pWidth))
    } else {
      NSLog("Unable to process width value \(width)")
    }
    form.view.setNeedsLayout()
  }

  open func setChildHeight(of component: ViewComponent, height: Int32) {
    let child = component.view
    if height >= 0 {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(height)))
      child.frame.size.height = CGFloat(height)
    } else if height == kLengthPreferred {
      view.addConstraint(NSLayoutConstraint(item: view, attribute: .height, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height <= kLengthPercentTag {
      let height = -(height + 1000)
      let pHeight = CGFloat(height) / CGFloat(100.0)
      form.view.addConstraint(child.heightAnchor.constraint(equalTo: form.view.heightAnchor, multiplier: pHeight))
    } else {
      NSLog("Unable to process width value \(height)")
    }
    form.view.setNeedsLayout()
  }

  // MARK: HVArrangement Properties
  open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlign.rawValue
    }
    set(align) {
      if let align = HorizontalGravity(rawValue: align) {
        _horizontalAlign = align
        _view.horizontalAlignment = align
        _view.setNeedsUpdateConstraints()
        _view.setNeedsLayout()
      }
    }
  }

  open var AlignVertical: Int32 {
    get {
      return _verticalAlign.rawValue
    }
    set(align) {
      if let align = VerticalGravity(rawValue: align) {
        _verticalAlign = align
        _view.verticalAlignment = align
        _view.setNeedsUpdateConstraints()
        _view.setNeedsLayout()
      }
    }
  }

  open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set(argb) {
      _backgroundColor = argbToColor(argb)
      if _imagePath == "" {
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
        if let image = AssetManager.shared.imageFromPath(path: path) {
          _view.backgroundColor = UIColor(patternImage: image)
          return
        }
      }
      _imagePath = ""
      _view.backgroundColor = _backgroundColor
    }
  }

  // MARK: Private implementation
  private func updateHorizontalConstraints(_ child: UIView) {
    if _orientation == .horizontal {
      if _components.count == 1 {
        let constraint = child.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor)
        constraint.priority = UILayoutPriorityRequired
        view.addConstraint(constraint)
      } else {
        let constraint = _components[_components.count - 2].view.trailingAnchor.constraint(equalTo: child.leadingAnchor)
        constraint.priority = UILayoutPriorityRequired
        view.addConstraint(constraint)
      }
      _lastConstraint = _view.trailingAnchor.constraint(greaterThanOrEqualTo: child.trailingAnchor)
      view.addConstraint(_lastConstraint)
    } else {
    }
  }

  private func updateVerticalConstraints(_ child: UIView) {
    if _orientation == .horizontal {
      var constraint = _view.heightAnchor.constraint(greaterThanOrEqualTo: child.heightAnchor)
      constraint.priority = UILayoutPriorityDefaultLow
      view.addConstraint(constraint)
      if _components.count == 1 {
        constraint = child.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor)
        constraint.priority = UILayoutPriorityRequired
        view.addConstraint(constraint)
      } else {
        constraint = _components[_components.count - 2].view.trailingAnchor.constraint(equalTo: child.leadingAnchor)
        constraint.priority = UILayoutPriorityRequired
        view.addConstraint(constraint)
      }
      _lastConstraint = _view.trailingAnchor.constraint(greaterThanOrEqualTo: child.trailingAnchor)
      view.addConstraint(_lastConstraint)
    } else {
      var constraint = _view.widthAnchor.constraint(greaterThanOrEqualTo: child.widthAnchor)
      constraint.priority = UILayoutPriorityDefaultLow
      view.addConstraint(constraint)
      if _components.count == 0 {
      } else {
        constraint = _components[_components.count - 2].view.bottomAnchor.constraint(equalTo: child.topAnchor)
        constraint.priority = UILayoutPriorityRequired
        view.addConstraint(constraint)
      }
      _lastConstraint = _view.bottomAnchor.constraint(greaterThanOrEqualTo: child.bottomAnchor)
      view.addConstraint(_lastConstraint)
    }
  }
}
