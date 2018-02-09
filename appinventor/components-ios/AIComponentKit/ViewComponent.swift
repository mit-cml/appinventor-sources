//
//  ViewComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

@objc public protocol AbstractMethodsForViewComponent: class {
  var view: UIView { get }
}

open class ViewComponent: NSObject, VisibleComponent {
  fileprivate weak var _delegate: AbstractMethodsForViewComponent?
  internal let _container: ComponentContainer

  fileprivate var _percentWidthHolder = kLengthUnknown
  fileprivate var _percentHeightHolder = kLengthUnknown
  fileprivate var _lastSetWidth = kLengthPreferred
  fileprivate var _lastSetHeight = kLengthPreferred

  fileprivate var _column = kDefaultRowColumn
  fileprivate var _row = kDefaultRowColumn

  // needs to be public for extensions
  public init(_ parent: ComponentContainer) {
    self._container = parent
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  internal func setDelegate(_ delegate: AbstractMethodsForViewComponent) {
    self._delegate = delegate
  }

  open var view: UIView {
    get {
      return (_delegate?.view)!
    }
  }

  open var Visible: Bool {
    get {
      return !(_delegate?.view.isHidden)!
    }
    set(visibility) {
      _delegate?.view.isHidden = !visibility
    }
  }

  open var Width: Int32 {
    get {
      return _lastSetWidth
    }
    set(width) {
      _lastSetWidth = width
      _container.setChildWidth(of: self, width: width)
    }
  }

  open func setWidthPercent(_ toPercent: Int32) {
    if toPercent > 100 {
      Width = -1100
    } else if toPercent < 1 {
      Width = -1001
    } else {
      Width = -(1000 + toPercent)
    }
  }

  open var Height: Int32 {
    get {
      return _lastSetHeight
    }
    set(height) {
      _lastSetHeight = height
      _container.setChildHeight(of: self, height: height)
    }
  }

  open func setHeightPercent(_ toPercent: Int32) {
    if toPercent > 100 {
      Height = -1100
    } else if toPercent < 1 {
      Height = -1001
    } else {
      Height = -(1000 + toPercent)
    }
  }

  open func setNestedViewHeight(nestedView: UIView, height: Int32, shouldAddConstraints: Bool){
    resetNestedViewConstraints(for: nestedView, width: Width, height: height, shouldAddConstraint: shouldAddConstraints)
  }

  open func setNestedViewWidth(nestedView: UIView, width: Int32, shouldAddConstraints: Bool){
    resetNestedViewConstraints(for: nestedView, width: width, height: Height, shouldAddConstraint: shouldAddConstraints)
  }

  open func resetNestedViewConstraints(for nestedView: UIView, width: Int32, height: Int32, shouldAddConstraint: Bool) {
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
    _container.setChildHeight(of: self, height: height)
    _lastSetWidth = width
    _container.setChildWidth(of: self, width: width)
    if shouldAddConstraint {
      nestedView.frame = view.bounds
      let topAnchor = nestedView.topAnchor.constraint(equalTo: view.topAnchor)
      let bottomAnchor = nestedView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
      let leftAnchor = nestedView.leftAnchor.constraint(equalTo: view.leftAnchor)
      let rightAnchor = nestedView.rightAnchor.constraint(equalTo: view.rightAnchor)
      view.addConstraints([topAnchor, bottomAnchor, leftAnchor, rightAnchor])
    }
  }

  open var Column: Int32 {
    get {
      return _column
    }
    set(column) {
      _column = column
    }
  }

  open var Row: Int32 {
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
