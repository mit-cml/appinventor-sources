// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation


@objc open class Tab: UIViewController, Component, ComponentContainer {
  private var _children = [ViewComponent]()
  private var _container: ComponentContainer
  private var _icon: UIImage? = nil
  private var _iconPath = ""
  private var _scrollable = false
  private var _showIcon = true
  private var _showText = true
  private var _text = ""
  
  
  public init(_ parent: ComponentContainer) {
    _container = parent
    super.init(nibName: nil, bundle: nil)
    Icon = ""
    Scrollable = false
    ShowIcon = true
    ShowText = true
    Text = ""
  }
  
  public required init?(coder aDecoder: NSCoder) {
    guard let parent = aDecoder.decodeObject(forKey: "_container") as? ComponentContainer else {
      return nil
    }
    _container = parent
    super.init(coder: aDecoder)
  }
  
  // MARK: Component protocol implementation
  public var dispatchDelegate: HandlesEventDispatching? {
    return _container.form
  }
  
  // MARK: ComponentContainer protocol implementation
  public var form: Form? {
    return _container.form
  }
  
  public var container: ComponentContainer? {
    return _container
  }
  
  public func add(_ component: ViewComponent) {
    _children.append(component)
  }
  
  
  
  public func setChildWidth(of component: ViewComponent, to width: Int32) {
    component.Width = width
  }
  
  public func setChildHeight(of component: ViewComponent, to height: Int32) {
    component.Height = height
  }
  
  public func isVisible(component: ViewComponent) -> Bool {
    return true
  }
  
  public func setVisible(component: ViewComponent, to visibility: Bool) {
    // not needed
  }
  
  public var Width: Int32 {
    get {
      return Int32(tabBar.frame.width)
    }
    set {
      var frame = tabBar.frame
      frame.size.width = CGFloat(newValue)
      tabBar.frame = frame
    }
  }
  
  public var Height: Int32 {
    get {
      return Int32(tabBar.frame.height)
    }
    set {
      var frame = tabBar.frame
      frame.size.height = CGFloat(newValue)
      tabBar.frame = frame
    }
  }
  
  
  public func getChildren() -> [Component] {
    return _children as [Component]
  }
  
  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  // MARK: Tab Properties
  @objc open var Icon: String {
    get {
      return _iconPath
    }
    set {
      var image: UIImage? = nil
      if newValue == "" {
        image = nil
      } else {
        image = AssetManager.shared.imageFromPath(path: newValue)
      }
      _iconPath = newValue
      _icon = image
      updateAppearance()
    }
  }
  
  @objc open var Scrollable: Bool {
    get {
      return _scrollable
    }
    set {
      _scrollable = newValue
    }
  }
  
  @objc open var ShowIcon: Bool {
    get {
      return _showIcon
    }
    set {
      _showIcon = newValue
      updateAppearance()
    }
  }
  
  @objc open var ShowText: Bool {
    get {
      return _showText
    }
    set {
      _showText = newValue
      updateAppearance()
    }
  }
  
  @objc open var Text: String {
    get {
      return _text
    }
    set {
      _text = newValue
      updateAppearance()
    }
  }
  
  // MARK: Methods
  @objc open func Show() {
    (_container as? TabArrangement)?.show(tab: self)
  }
  
  // MARK: Events
  @objc open func Click() {
    EventDispatcher.dispatchEvent(of: self, called: "Click")
  }
  
  // MARK: Private implementation
  private func updateAppearance() {
    tabBarItem.title = _showText ? _text : nil
    tabBarItem.image = _showIcon ? _icon : nil
  }
  
  public var tabBar: UITabBar {
    return self.tabBarController!.tabBar
  }
  
}
