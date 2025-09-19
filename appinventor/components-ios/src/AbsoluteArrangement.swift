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
    viewLayout = RelativeLayout(preferredEmptyWidth: 0, preferredEmptyHeight: 0)
    
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
    
    if width <= kLengthPercentTag {
      let parentWidth = form.Width
      let childWidth = parentWidth * Int32(-(width - kLengthPercentTag)) / 100
      component._lastSetWidth = width
      
      NSLayoutConstraint.activate([
          component.view.widthAnchor.constraint(equalToConstant: CGFloat(childWidth))
      ])
    } else if width == kLengthPreferred {
      // Let view size itself
      component._lastSetWidth = width
      // Remove width constraints
      component.view.constraints.filter { $0.firstAttribute == .width }.forEach { $0.isActive = false }
    } else if width == kLengthFillParent {
      component._lastSetWidth = width
      NSLayoutConstraint.activate([
          component.view.widthAnchor.constraint(equalTo: _view.widthAnchor)
      ])
    } else {
      component._lastSetWidth = width
      NSLayoutConstraint.activate([
        component.view.widthAnchor.constraint(equalToConstant: CGFloat(width))
      ])
    }
  }
  
  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    guard let form = form else {
        return
    }
    
    if height <= kLengthPercentTag {
        let parentHeight = form.Height
        let childHeight = parentHeight * Int32(-(height - kLengthPercentTag)) / 100
        component._lastSetHeight = height
        
        NSLayoutConstraint.activate([
            component.view.heightAnchor.constraint(equalToConstant: CGFloat(childHeight))
        ])
    } else if height == kLengthPreferred {
        // Let view size itself
        component._lastSetHeight = height
        // Remove height constraints
        component.view.constraints.filter { $0.firstAttribute == .height }.forEach { $0.isActive = false }
    } else if height == kLengthFillParent {
        component._lastSetHeight = height
        NSLayoutConstraint.activate([
            component.view.heightAnchor.constraint(equalTo: _view.heightAnchor)
        ])
    } else {
        component._lastSetHeight = height
        NSLayoutConstraint.activate([
            component.view.heightAnchor.constraint(equalToConstant: CGFloat(height))
        ])
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
