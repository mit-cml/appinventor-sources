// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

public class CardView: ViewComponent, AbstractMethodsForViewComponent, ComponentContainer {
  public var container: ComponentContainer?
  fileprivate var _view = UIView()
  fileprivate var _strokeColor = UIColor.black
  fileprivate var _components: [ViewComponent] = [ViewComponent]()
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _backgroundColor = UIColor.white
  fileprivate var _strokeWidth : CGFloat = 4
  fileprivate var _elevation : CGFloat = 2
  fileprivate var _cornerRaduis : CGFloat = 10
  fileprivate var _fullClickable : Bool = true
  fileprivate var _touchColor = UIColor.white
  
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    setupCardView()
    super.setDelegate(self)
    parent.add(self)
  }
  
  fileprivate var _stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 2
    stackView.distribution = .equalSpacing
    return stackView
  }()
  
  public func setupCardView() {
    _view.layer.cornerRadius = 10
    _view.layer.shadowColor = UIColor.black.cgColor
    _view.layer.shadowOpacity = 0.5
    _view.layer.shadowOffset = CGSize(width: 0, height: 2)
    _view.layer.shadowRadius = 4
    
    
    _view.layer.borderColor = UIColor.white.cgColor
    _view.layer.borderWidth = 0
    
    _stackView.distribution = .equalSpacing
    
    _view.backgroundColor = .white
    _stackView.translatesAutoresizingMaskIntoConstraints = false
    _view.addSubview(_stackView)
    
    NSLayoutConstraint.activate([
      _stackView.leadingAnchor.constraint(equalTo: _view.leadingAnchor, constant: 10),
      _stackView.trailingAnchor.constraint(equalTo: _view.trailingAnchor, constant: -10),
      _stackView.topAnchor.constraint(equalTo: _view.topAnchor, constant: 10),
      _stackView.bottomAnchor.constraint(equalTo: _view.bottomAnchor, constant: -10)
    ])
  }
  
  public override var view: UIView {
    get {
      return _view
    }
  }
  
  @objc open var StrokeColor: Int32 {
    get {
      return _view.layer.borderColor as! Int32
    }
    set(argb) {
      _view.layer.borderColor = argbToColor(argb).cgColor
    }
  }
  
  @objc open var CornerRadius: CGFloat {
    get {
      return _cornerRaduis
    }
    set(argb) {
      _cornerRaduis = argb
      _view.layer.cornerRadius = _cornerRaduis
    }
  }
  
  @objc open var FullClickable: Bool {
    get {
      return _fullClickable
    }
    set(argb) {
      _fullClickable = argb
    }
  }
  
  @objc open var Elevation: CGFloat {
    get {
      return _elevation
    }
    set(argb) {
      _elevation = argb
      _view.layer.shadowOffset = CGSize(width: 0, height: _elevation)
    }
  }
  
  @objc open var StrokeWidth: CGFloat {
    get {
      return _strokeWidth
    }
    set(argb) {
      _strokeWidth = argb
      _view.layer.borderWidth = _strokeWidth
    }
  }
  
  @objc open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlign.rawValue
    }
    set(align) {
      if let align = HorizontalGravity(rawValue: align) {
        _horizontalAlign = align
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
      _view.backgroundColor = _backgroundColor
      
    }
  }
  
  @objc open var TouchColor: Int32 {
    get {
      return colorToArgb(_touchColor)
    }
    set(argb) {
      _touchColor = argbToColor(argb)
    }
  }
  
  // Implementations for ComponentContainer protocol
  open func add(_ component: ViewComponent) {
    _components.append(component)
    _stackView.addArrangedSubview(component.view)
  }
  
  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    
    _view.setNeedsLayout()
  }
  
  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    
    
    _view.setNeedsLayout()
  }
  
  open func isVisible(component: ViewComponent) -> Bool {
    return component.view.isDescendant(of: _view)
  }
  
  open func setVisible(component: ViewComponent, to visibility: Bool) {
    if visibility {
      if !isVisible(component: component) {
        _view.addSubview(component.view)
      }
    } else {
      if isVisible(component: component) {
        component.view.removeFromSuperview()
      }
    }
  }
  
  open func getChildren() -> [Component] {
    return _components
  }
}

