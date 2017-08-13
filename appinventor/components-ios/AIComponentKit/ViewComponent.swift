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

  open func WidthPercent(_ toPercent: Int32) {
    //TODO: implementation
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

  open func HeightPercent(_ toPercent: Int32) {
    //TODO: implementation
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
