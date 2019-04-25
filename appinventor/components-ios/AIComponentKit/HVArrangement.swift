// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

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
  private var _dimensions = [Int:NSLayoutConstraint]()

  public init(_ parent: ComponentContainer, orientation: HVOrientation, scrollable: Bool) {
    _orientation = orientation
    super.init(parent)
    _view.translatesAutoresizingMaskIntoConstraints = false
    _view.scrollEnabled = scrollable
    _view.orientation = orientation
    _view.scrollEnabled = scrollable
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

  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    let hash = component.view.hash &* 2
    if let oldConstraint = _dimensions.removeValue(forKey: hash) {
      oldConstraint.isActive = false
    }
    form.view.setNeedsLayout()
    component.view.setContentHuggingPriority(FillParentHuggingPriority, for: .horizontal)
    var constraint: NSLayoutConstraint!
    if width >= 0 {
      constraint = NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(width))
    } else if width == kLengthPreferred {
      constraint = NSLayoutConstraint(item: view, attribute: .width, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0))
    } else if width == kLengthFillParent {
      component.view.setContentHuggingPriority(FillParentHuggingPriority, for: .horizontal)
      constraint = component.view.widthAnchor.constraint(greaterThanOrEqualTo: _view.widthAnchor)
      constraint.priority = UILayoutPriority(1)
    } else if width <= kLengthPercentTag {
      let percent = CGFloat(Double(-(width + 1000)) / 100.0)
      constraint = component.view.widthAnchor.constraint(equalTo: form.widthAnchor, multiplier: percent)
    } else {
      NSLog("Unable to process width value \(width)")
      return
    }
    constraint.isActive = true
    _dimensions[hash] = constraint
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    let hash = component.view.hash &* 2 | 1
    if let oldConstraint = _dimensions.removeValue(forKey: hash) {
      oldConstraint.isActive = false
    }
    form.view.setNeedsLayout()
    component.view.setContentHuggingPriority(.defaultLow, for: .vertical)
    var constraint: NSLayoutConstraint!
    if height >= 0 {
      constraint = NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(height))
    } else if height == kLengthPreferred {
      constraint = NSLayoutConstraint(item: view, attribute: .height, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0))
    } else if height == kLengthFillParent {
      component.view.setContentHuggingPriority(FillParentHuggingPriority, for: .vertical)
      constraint = component.view.heightAnchor.constraint(greaterThanOrEqualTo: _view.heightAnchor)
      constraint.priority = UILayoutPriority(1)
    } else if height <= kLengthPercentTag {
      let percent = CGFloat(Double(-(height + 1000)) / 100.0)
      constraint = component.view.heightAnchor.constraint(equalTo: form.heightAnchor, multiplier: percent)
    } else {
      NSLog("Unable to process width value \(height)")
      return
    }
    constraint.isActive = true
    _dimensions[hash] = constraint
  }

  // MARK: HVArrangement Properties
  @objc open var AlignHorizontal: Int32 {
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

  @objc open var AlignVertical: Int32 {
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

  @objc open var BackgroundColor: Int32 {
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

  @objc open var Image: String {
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
        constraint.priority = UILayoutPriority.required
        view.addConstraint(constraint)
      } else {
        let constraint = _components[_components.count - 2].view.trailingAnchor.constraint(equalTo: child.leadingAnchor)
        constraint.priority = UILayoutPriority.required
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
      constraint.priority = UILayoutPriority.defaultLow
      view.addConstraint(constraint)
      if _components.count == 1 {
        constraint = child.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor)
        constraint.priority = UILayoutPriority.required
        view.addConstraint(constraint)
      } else {
        constraint = _components[_components.count - 2].view.trailingAnchor.constraint(equalTo: child.leadingAnchor)
        constraint.priority = UILayoutPriority.required
        view.addConstraint(constraint)
      }
      _lastConstraint = _view.trailingAnchor.constraint(greaterThanOrEqualTo: child.trailingAnchor)
      view.addConstraint(_lastConstraint)
    } else {
      var constraint = _view.widthAnchor.constraint(greaterThanOrEqualTo: child.widthAnchor)
      constraint.priority = UILayoutPriority.defaultLow
      view.addConstraint(constraint)
      if _components.count == 0 {
      } else {
        constraint = _components[_components.count - 2].view.bottomAnchor.constraint(equalTo: child.topAnchor)
        constraint.priority = UILayoutPriority.required
        view.addConstraint(constraint)
      }
      _lastConstraint = _view.bottomAnchor.constraint(greaterThanOrEqualTo: child.bottomAnchor)
      view.addConstraint(_lastConstraint)
    }
  }
}
