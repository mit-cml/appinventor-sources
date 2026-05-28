// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit



open class AbsoluteArrangement: ViewComponent, ComponentContainer, AbstractMethodsForViewComponent {

  
  public func setChildNeedsLayout(component: ViewComponent) {
    viewLayout.updateComponentPosition(component: component);
  }
  
  // MARK: - Properties
  public var container: (any ComponentContainer)?
  
  private var _components: [ViewComponent] = []
  private var _view: AbsoluteView
  private var _backgroundColor = Color.default.int32
  private var _imagePath = ""

  // Layout
  private var viewLayout: RelativeLayout
  private var _widthConstraints = [ObjectIdentifier: NSLayoutConstraint]()
  private var _heightConstraints = [ObjectIdentifier: NSLayoutConstraint]()
  
  // MARK: - Constants
  private let NOT_VALID: Int = -1
  
  // MARK: - Custom View for Absolute Layout
  private class AbsoluteView: UIView {
    weak var contentView: UIView?

    override init(frame: CGRect) {
      super.init(frame: frame)
      translatesAutoresizingMaskIntoConstraints = false
      backgroundColor = .white
      clipsToBounds = true
    }
    
    required init?(coder: NSCoder) {
      fatalError("init(coder:) has not been implemented")
    }
    
    func setVisibility(of view: UIView, to visibility: Bool) {
      view.isHidden = !visibility
    }
    
    override var intrinsicContentSize: CGSize {
      return contentView?.intrinsicContentSize ?? CGSize(width: CGFloat(kEmptyHVArrangementWidth),
                                                         height: CGFloat(kEmptyHVArrangementHeight))
    }
  }
  
  // MARK: - Initialization
  public override init(_ parent: ComponentContainer) {
    _view = AbsoluteView(frame: .zero)
    viewLayout = RelativeLayout(preferredEmptyWidth: kEmptyHVArrangementWidth,
                                preferredEmptyHeight: kEmptyHVArrangementHeight)
    
    super.init(parent)
    super.setDelegate(self)
    
  
    // Add the layout view to our view
    _view.addSubview(viewLayout.getLayoutManager())
    _view.contentView = viewLayout.getLayoutManager()
    viewLayout.getLayoutManager().translatesAutoresizingMaskIntoConstraints = false
    
    // Make the layout view fill our view
    NSLayoutConstraint.activate([
      viewLayout.getLayoutManager().topAnchor.constraint(equalTo: _view.topAnchor),
      viewLayout.getLayoutManager().leftAnchor.constraint(equalTo: _view.leftAnchor),
      viewLayout.getLayoutManager().rightAnchor.constraint(equalTo: _view.rightAnchor),
      viewLayout.getLayoutManager().bottomAnchor.constraint(equalTo: _view.bottomAnchor)
    ])
    
    parent.add(self)
    BackgroundColor = Color.default.int32
  }
  

  // MARK: - AbstractMethodsForViewComponent
  open override var view: UIView {
    return _view
  }
  
  open func getView() -> UIView {
    return viewLayout.getLayoutManager();
  }

  // MARK: - ComponentContainer Implementation
  open func add(_ component: ViewComponent) {
    _components.append(component)
    viewLayout.add(component)
    
    // Update position immediately if values are set
    updatePosition(component: component)
  }
  
  open func isVisible(component: ViewComponent) -> Bool {
    return !component.view.isHidden && component.view.superview != nil
  }
  
  open func setVisible(component: ViewComponent, to visibility: Bool) {
    let visible = isVisible(component: component)
    if visibility == visible {
      return
    }
    
    if visibility {
      _view.setVisibility(of: component.view, to: true)
      // Replay width/height properties
      setChildHeight(of: component, to: component._lastSetHeight)
      setChildWidth(of: component, to: component._lastSetWidth)
    } else {
      _view.setVisibility(of: component.view, to: false)
    }
  }
  
  open func getChildren() -> [Component] {
    return _components as [Component]
  }
  
  // MARK: - Position Management
  private func updatePosition(component: ViewComponent) {
    let x = component.Left
    let y = component.Top
    
    if x != NOT_VALID && y != NOT_VALID {
      viewLayout.updateComponentPosition(component: component)
      setChildWidth(of: component, to: component._lastSetWidth)
      setChildHeight(of: component, to: component._lastSetHeight)
    }
  }
  
  // MARK: - Child Sizing Methods
  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    guard let form = form else {
      return
    }

    replaceConstraint(&_widthConstraints, for: component, with: nil)
    let constraint: NSLayoutConstraint?
    if width <= kLengthPercentTag {
      let percent = CGFloat(-(width - kLengthPercentTag)) / 100.0
      constraint = component.view.widthAnchor.constraint(equalTo: form.scaleFrameLayout.widthAnchor,
                                                         multiplier: percent)
    } else if width == kLengthPreferred {
      constraint = nil
    } else if width == kLengthFillParent {
      constraint = component.view.widthAnchor.constraint(equalTo: _view.widthAnchor)
    } else {
      constraint = component.view.widthAnchor.constraint(equalToConstant: CGFloat(width))
    }
    component._lastSetWidth = width
    replaceConstraint(&_widthConstraints, for: component, with: constraint)
    _view.invalidateIntrinsicContentSize()
  }
  
  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    guard let form = form else {
      return
    }

    replaceConstraint(&_heightConstraints, for: component, with: nil)
    let constraint: NSLayoutConstraint?
    if height <= kLengthPercentTag {
      let percent = CGFloat(-(height - kLengthPercentTag)) / 100.0
      constraint = component.view.heightAnchor.constraint(equalTo: form.scaleFrameLayout.heightAnchor,
                                                          multiplier: percent)
    } else if height == kLengthPreferred {
      constraint = nil
    } else if height == kLengthFillParent {
      constraint = component.view.heightAnchor.constraint(equalTo: _view.heightAnchor)
    } else {
      constraint = component.view.heightAnchor.constraint(equalToConstant: CGFloat(height))
    }
    component._lastSetHeight = height
    replaceConstraint(&_heightConstraints, for: component, with: constraint)
    _view.invalidateIntrinsicContentSize()
  }

  private func replaceConstraint(_ constraints: inout [ObjectIdentifier: NSLayoutConstraint],
                                 for component: ViewComponent,
                                 with constraint: NSLayoutConstraint?) {
    let key = ObjectIdentifier(component)
    constraints[key]?.isActive = false
    constraints[key] = constraint
    if let constraint = constraint, canActivate(constraint) {
      constraint.isActive = true
    }
  }

  private func canActivate(_ constraint: NSLayoutConstraint) -> Bool {
    guard let first = constraint.firstItem as? UIView,
          let second = constraint.secondItem as? UIView else {
      return true
    }
    return sharesViewHierarchy(first, second)
  }

  private func sharesViewHierarchy(_ first: UIView, _ second: UIView) -> Bool {
    var ancestors = Set<ObjectIdentifier>()
    var current: UIView? = first
    while let view = current {
      ancestors.insert(ObjectIdentifier(view))
      current = view.superview
    }
    current = second
    while let view = current {
      if ancestors.contains(ObjectIdentifier(view)) {
        return true
      }
      current = view.superview
    }
    return false
  }
  
  // MARK: - Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      if _imagePath.isEmpty {
        _view.backgroundColor = defaultedBackgroundColor(argb)
      }
    }
  }
  
  @objc open var Image: String {
    get {
      return _imagePath
    }
    set(path) {
      if path == _imagePath {
        return
      } else if !path.isEmpty {
        if let image = AssetManager.shared.imageFromPath(path: path) {
          _view.backgroundColor = UIColor(patternImage: image)
          _imagePath = path
          return
        }
      }
      _imagePath = ""
      _view.backgroundColor = defaultedBackgroundColor(_backgroundColor)
    }
  }

  private func defaultedBackgroundColor(_ argb: Int32) -> UIColor {
    if argb == Color.default.int32 {
      return form?.isDarkTheme == true ? Color.black.uiColor : Color.white.uiColor
    }
    return argbToColor(argb)
  }
}
