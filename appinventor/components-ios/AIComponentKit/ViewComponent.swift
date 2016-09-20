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
  final var delegate: AbstractMethodsForViewComponent!
  internal let container: ComponentContainer

  private var percentWidthHolder = kLengthUnknown
  private var percentHeightHolder = kLengthUnknown
  private var lastSetWidth = kLengthUnknown
  private var lastSetHeight = kLengthUnknown

  private var column = kDefaultRowColumn
  private var row = kDefaultRowColumn

  // needs to be public for extensions
  public init(parent: ComponentContainer, delegate: AbstractMethodsForViewComponent!) {
    self.container = parent
    self.delegate = delegate
  }
  
  public func Visible() -> Bool {
    return !delegate.view.isHidden
  }
  
  public func Visible(visibility: Bool) {
    delegate.view.isHidden = !visibility
  }
  
  public func Width() -> Int32 {
    return Int32(delegate.view.frame.width)
  }
  
  public func Width(to: Int32) {
    
  }
  
  public func WidthPercent(toPercent: Int32) {
    
  }
  
  public func Height() -> Int32 {
    return Int32(delegate.view.frame.height)
  }
  
  public func Height(to: Int32) {
    
  }
  
  public func HeightPercent(toPercent: Int32) {
    
  }
  
  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return container.form
    }
  }
}
