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

public class ViewComponent: NSObject, VisibleComponent {
  private weak var _delegate: AbstractMethodsForViewComponent?
  internal let _container: ComponentContainer

  private var _percentWidthHolder = kLengthUnknown
  private var _percentHeightHolder = kLengthUnknown
  private var _lastSetWidth = kLengthUnknown
  private var _lastSetHeight = kLengthUnknown

  private var _column = kDefaultRowColumn
  private var _row = kDefaultRowColumn

  // needs to be public for extensions
  public init(_ parent: ComponentContainer) {
    self._container = parent
  }
  
  internal func setDelegate(_ delegate: AbstractMethodsForViewComponent) {
    self._delegate = delegate
  }

  public var view: UIView {
    get {
      return (_delegate?.view)!
    }
  }

  public var Visible: Bool {
    get {
      return !(_delegate?.view.isHidden)!
    }
    set(visibility) {
      _delegate?.view.isHidden = !visibility
    }
  }

  public var Width: Int32 {
    get {
      return Int32((_delegate?.view.frame.width)!)
    }
    set(to) {
      var rect = (_delegate?.view.frame)!
      rect.size.width = CGFloat(to)
      _delegate?.view.frame = rect
    }
  }

  public func WidthPercent(toPercent: Int32) {
    //TODO: implementation
  }

  public var Height: Int32 {
    get {
      return Int32((_delegate?.view.frame.height)!)
    }
    set(to) {
      var rect = (_delegate?.view.frame)!
      rect.size.height = CGFloat(to)
      _delegate?.view.frame = rect
    }
  }

  public func HeightPercent(toPercent: Int32) {
    //TODO: implementation
  }

  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return (_container.form?.dispatchDelegate)!
    }
  }
}
