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
  private var _backgroundColor = UIColor.white
  private var _imagePath = ""
  private var _childWidthConstraints: [ObjectIdentifier: NSLayoutConstraint] = [:]
  private var _childHeightConstraints: [ObjectIdentifier: NSLayoutConstraint] = [:]

  // Layout
  private var viewLayout: RelativeLayout
  
  // MARK: - Constants
  private let NOT_VALID: Int = -1
  
  // MARK: - Custom View for Absolute Layout
  private class AbsoluteView: UIView {
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
      // Let parent layout determine size
      return UIView.layoutFittingExpandedSize
    }
  }
  
  // MARK: - Initialization
  public override init(_ parent: ComponentContainer) {
    _view = AbsoluteView(frame: .zero)
    viewLayout = RelativeLayout(preferredEmptyWidth: kEmptyHVArrangementWidth,preferredEmptyHeight: kEmptyHVArrangementWidth)
    
    super.init(parent)
    super.setDelegate(self)
    
  
    // Add the layout view to our view
    _view.addSubview(viewLayout.getLayoutManager())
    viewLayout.getLayoutManager().translatesAutoresizingMaskIntoConstraints = false
    
    // Make the layout view fill our view
    NSLayoutConstraint.activate([
      viewLayout.getLayoutManager().topAnchor.constraint(equalTo: _view.topAnchor),
      viewLayout.getLayoutManager().leftAnchor.constraint(equalTo: _view.leftAnchor),
      viewLayout.getLayoutManager().rightAnchor.constraint(equalTo: _view.rightAnchor),
      viewLayout.getLayoutManager().bottomAnchor.constraint(equalTo: _view.bottomAnchor)
    ])
    
    parent.add(self)
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
    }
  }
  
  // MARK: - Child Sizing Methods
  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    guard let form = form else {
      return
    }
    let key = ObjectIdentifier(component)
    _childWidthConstraints[key]?.isActive = false
    _childWidthConstraints.removeValue(forKey: key)
    if width <= kLengthPercentTag {
      let parentWidth = form.Width
      let childWidth = parentWidth * Int32(-(width - kLengthPercentTag)) / 100
      component._lastSetWidth = width
      let c = component.view.widthAnchor.constraint(equalToConstant: CGFloat(childWidth))
      NSLayoutConstraint.activate([c])
      _childWidthConstraints[key] = c
    } else if width == kLengthPreferred {
      component._lastSetWidth = width
      // No explicit constraint — let Auto Layout use intrinsic content size
    } else if width == kLengthFillParent {
      component._lastSetWidth = width
      let c = component.view.widthAnchor.constraint(equalTo: _view.widthAnchor)
      NSLayoutConstraint.activate([c])
      _childWidthConstraints[key] = c
    } else {
      component._lastSetWidth = width
      let c = component.view.widthAnchor.constraint(equalToConstant: CGFloat(width))
      NSLayoutConstraint.activate([c])
      _childWidthConstraints[key] = c
    }
  }
  
  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    guard let form = form else {
      return
    }
    let key = ObjectIdentifier(component)
    _childHeightConstraints[key]?.isActive = false
    _childHeightConstraints.removeValue(forKey: key)
    if height <= kLengthPercentTag {
      let parentHeight = form.Height
      let childHeight = parentHeight * Int32(-(height - kLengthPercentTag)) / 100
      component._lastSetHeight = height
      let c = component.view.heightAnchor.constraint(equalToConstant: CGFloat(childHeight))
      NSLayoutConstraint.activate([c])
      _childHeightConstraints[key] = c
    } else if height == kLengthPreferred {
      component._lastSetHeight = height
      // No explicit constraint — let Auto Layout use intrinsic content size
    } else if height == kLengthFillParent {
      component._lastSetHeight = height
      let c = component.view.heightAnchor.constraint(equalTo: _view.heightAnchor)
      NSLayoutConstraint.activate([c])
      _childHeightConstraints[key] = c
    } else {
      component._lastSetHeight = height
      let c = component.view.heightAnchor.constraint(equalToConstant: CGFloat(height))
      NSLayoutConstraint.activate([c])
      _childHeightConstraints[key] = c
    }
  }
  
  // MARK: - Properties
  @objc open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set(argb) {
      _backgroundColor = argbToColor(argb)
      if _imagePath.isEmpty {
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
        return
      } else if !path.isEmpty {
        if let image = AssetManager.shared.imageFromPath(path: path) {
          _view.backgroundColor = UIColor(patternImage: image)
          _imagePath = path
          return
        }
      }
      _imagePath = ""
      _view.backgroundColor = _backgroundColor
    }
  }
}
