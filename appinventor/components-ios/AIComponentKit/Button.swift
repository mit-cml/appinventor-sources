// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

import Foundation

public final class Button: ButtonBase, AbstractMethodsForButton {
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.titleLabel?.text = "Button1"
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
    let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(Button.longPress(_:)))
    longPressGesture.minimumPressDuration = 0.5
    _view.addGestureRecognizer(longPressGesture)
  }

  @objc public func click() {
    Click()
  }

  @objc public func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  @objc public func longPress(_ gesture: UILongPressGestureRecognizer) {
    if ( gesture.state == UIGestureRecognizer.State.began) {
      LongClick()
    }
  }
  
  @objc public func LongClick() {
    EventDispatcher.dispatchEvent(of: self, called: "LongClick")
  }
}
