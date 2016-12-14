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
  fileprivate var _lastSetWidth = kLengthUnknown
  fileprivate var _lastSetHeight = kLengthUnknown

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
      return Int32((_delegate?.view.frame.width)!)
    }
    set(to) {
      var rect = (_delegate?.view.frame)!
      rect.size.width = CGFloat(to)
      _delegate?.view.frame = rect
    }
  }

  open func WidthPercent(_ toPercent: Int32) {
    //TODO: implementation
  }

  open var Height: Int32 {
    get {
      return Int32((_delegate?.view.frame.height)!)
    }
    set(to) {
      var rect = (_delegate?.view.frame)!
      rect.size.height = CGFloat(to)
      _delegate?.view.frame = rect
    }
  }

  open func HeightPercent(_ toPercent: Int32) {
    //TODO: implementation
  }

  open var dispatchDelegate: HandlesEventDispatching {
    get {
      return (_container.form?.dispatchDelegate)!
    }
  }
}
