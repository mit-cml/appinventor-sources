//
//  Button.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public final class Button: ButtonBase, AbstractMethodsForButton {
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.titleLabel?.text = "Button1"
    _view.addTarget(self, action: #selector(click), for: UIControlEvents.primaryActionTriggered)
    let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(Button.longPress(_:)))
    _view.addGestureRecognizer(longPressGesture)
    parent.add(self)
  }

  public func click() {
    Click()
  }

  public func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  public func longPress(_ gesture: UILongPressGestureRecognizer) {
    if ( gesture.state == UIGestureRecognizerState.ended) {
      LongClick()
    }
  }
  
  public func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
}
