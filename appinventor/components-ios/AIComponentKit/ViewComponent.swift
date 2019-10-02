// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

@objc public protocol AbstractMethodsForViewComponent: class {
  var view: UIView { get }
}

@objc open class ViewComponent: NSObject, VisibleComponent {
  fileprivate weak var _delegate: AbstractMethodsForViewComponent?
  @objc internal let _container: ComponentContainer

  fileprivate var _percentWidthHolder = kLengthUnknown
  fileprivate var _percentHeightHolder = kLengthUnknown
  internal var _lastSetWidth = kLengthPreferred
  internal var _lastSetHeight = kLengthPreferred

  fileprivate var _column = kDefaultRowColumn
  fileprivate var _row = kDefaultRowColumn

  // needs to be public for extensions
  @objc public init(_ parent: ComponentContainer) {
    self._container = parent
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  @objc internal func setDelegate(_ delegate: AbstractMethodsForViewComponent) {
    self._delegate = delegate
  }

  @objc open var view: UIView {
    get {
      return (_delegate?.view)!
    }
  }

  @objc open var Visible: Bool {
    get {
      return !(_delegate?.view.isHidden)!
    }
    set(visibility) {
      _delegate?.view.isHidden = !visibility
    }
  }

  @objc open var Width: Int32 {
    get {
      view.layoutIfNeeded()
      return Int32(view.bounds.width)
    }
    set(width) {
      _container.setChildWidth(of: self, to: width)
      _lastSetWidth = width
    }
  }

  @objc open func setWidthPercent(_ toPercent: Int32) {
    if toPercent > 100 {
      Width = -1100
    } else if toPercent < 1 {
      Width = -1001
    } else {
      Width = -(1000 + toPercent)
    }
  }

  @objc open var Height: Int32 {
    get {
      view.layoutIfNeeded()
      return Int32(view.bounds.height)
    }
    set(height) {
      _container.setChildHeight(of: self, to: height)
      _lastSetHeight = height
    }
  }

  @objc open func setHeightPercent(_ toPercent: Int32) {
    if toPercent > 100 {
      Height = -1100
    } else if toPercent < 1 {
      Height = -1001
    } else {
      Height = -(1000 + toPercent)
    }
  }

  @objc open func setNestedViewHeight(nestedView: UIView, height: Int32, shouldAddConstraints: Bool){
    resetNestedViewConstraints(for: nestedView, width: _lastSetWidth, height: height, shouldAddConstraint: shouldAddConstraints)
  }

  @objc open func setNestedViewWidth(nestedView: UIView, width: Int32, shouldAddConstraints: Bool){
    resetNestedViewConstraints(for: nestedView, width: width, height: _lastSetHeight, shouldAddConstraint: shouldAddConstraints)
  }

  @objc open func resetNestedViewConstraints(for nestedView: UIView, width: Int32, height: Int32, shouldAddConstraint: Bool) {
    let constraintsToRemove = _container.form.view.constraints.filter { constraint in
      if let vi = constraint.firstItem, type(of: vi) == type(of: view) {
        let tempView = vi as! UIView
        if tempView == nestedView || tempView == view {
          return true
        } else if let secondView = constraint.secondItem, type(of: secondView) == type(of: view) {
          let tempView2 = secondView as! UIView
          if tempView2 == nestedView || tempView2 == view{
            return true
          }
        }
      }
      return false
    }

    _container.form.view.removeConstraints(constraintsToRemove)
    _lastSetHeight = height
    _container.setChildHeight(of: self, to: height)
    _lastSetWidth = width
    _container.setChildWidth(of: self, to: width)
    if shouldAddConstraint {
      nestedView.frame = view.bounds
      let topAnchor = nestedView.topAnchor.constraint(equalTo: view.topAnchor)
      let bottomAnchor = nestedView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
      let leftAnchor = nestedView.leftAnchor.constraint(equalTo: view.leftAnchor)
      let rightAnchor = nestedView.rightAnchor.constraint(equalTo: view.rightAnchor)
      view.addConstraints([topAnchor, bottomAnchor, leftAnchor, rightAnchor])
    }
  }

  @objc open var Column: Int32 {
    get {
      return _column
    }
    set(column) {
      _column = column
    }
  }

  @objc open var Row: Int32 {
    get {
      return _row
    }
    set(row) {
      _row = row
    }
  }

  open var dispatchDelegate: HandlesEventDispatching {
    get {
      return _container.form.dispatchDelegate
    }
  }
}
