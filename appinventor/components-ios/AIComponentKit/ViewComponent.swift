//
//  ViewComponent.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForViewComponent {
  var view: UIView { get }
}

public class ViewComponent: VisibleComponent {
  private final var _delegate: AbstractMethodsForViewComponent!
  internal let _container: ComponentContainer

  private var _percentWidthHolder = kLengthUnknown
  private var _percentHeightHolder = kLengthUnknown
  private var _lastSetWidth = kLengthUnknown
  private var _lastSetHeight = kLengthUnknown

  private var _column = kDefaultRowColumn
  private var _row = kDefaultRowColumn

  // needs to be public for extensions
  public init(parent: ComponentContainer, delegate: AbstractMethodsForViewComponent!) {
    self._container = parent
    self._delegate = delegate
  }
  
  public func Visible() -> Bool {
    return !_delegate.view.isHidden
  }
  
  public func Visible(visibility: Bool) {
    _delegate.view.isHidden = !visibility
  }
  
  public func Width() -> Int32 {
    return Int32(_delegate.view.frame.width)
  }
  
  public func Width(to: Int32) {
    
  }
  
  public func WidthPercent(toPercent: Int32) {
    
  }
  
  public func Height() -> Int32 {
    return Int32(_delegate.view.frame.height)
  }
  
  public func Height(to: Int32) {
    
  }
  
  public func HeightPercent(toPercent: Int32) {
    
  }
  
  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return _container.form.dispatchDelegate
    }
  }
}
