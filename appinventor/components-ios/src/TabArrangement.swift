// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class TabArrangement: ViewComponent, ComponentContainer, AbstractMethodsForViewComponent, UITabBarControllerDelegate {
  private var _tabs = [Tab]()
  private var _controller = UITabBarController()
  
  override public init(_ parent: ComponentContainer) {
    super.init(parent)
    setDelegate(self)
  }
  
  // MARK: AbstractMethodsForViewComponent protocol implementation
  public override var view: UIView {
    return _controller.view
  }
  
  // MARK: ComponentContainer protocol implementation
  public func add(_ component: ViewComponent) {
    print("Error: Cannot add a ViewComponent to a TabArrangement")
  }
  
  public func add(_ tab: Tab) {
    _tabs.append(tab)
    _controller.viewControllers?.append(tab)
  }
  
  public func setChildWidth(of component: ViewComponent, to width: Int32) {
    // Tab size is managed by the controller
  }
  
  public func setChildHeight(of component: ViewComponent, to height: Int32) {
    // Tab size is managed by the controller
  }
  
  public func isVisible(component: ViewComponent) -> Bool {
    return false
  }
  
  public func setVisible(component: ViewComponent, to visibility: Bool) {
    // Not needed
  }
  
  public func getChildren() -> [Component] {
    return _tabs as [Component]
  }
  
  public var container: ComponentContainer? {
    return _container
  }
  
  // MARK: UITabBarControllerDelegate protocol implementation
  // MARK: Properties
  private var _selectedTabIndicatorColor: UIColor?
  @objc var SelectedTabIndicatorColor: Int32 {
    get {
      return _selectedTabIndicatorColor?.toHex() ?? 0
    }
    set {
      _selectedTabIndicatorColor = UIColor(hex: newValue)
      _controller.tabBar.tintColor = _selectedTabIndicatorColor
    }
  }
  
  private var _tabBackgroundColor: UIColor?
  @objc var TabBackgroundColor: Int32 {
    get {
      return _tabBackgroundColor?.toHex() ?? 0
    }
    set {
      _tabBackgroundColor = UIColor(hex: newValue)
      _controller.tabBar.barTintColor = _tabBackgroundColor
    }
  }
  
  private var _tabTextColor: UIColor?
  @objc var TabTextColor: Int32 {
    get {
      return _tabTextColor?.toHex() ?? 0
    }
    set {
      _tabTextColor = UIColor(hex: newValue)
      _controller.tabBar.unselectedItemTintColor = _tabTextColor
    }
  }
  
  // MARK: Methods
  // MARK: Events
  @objc open func ShowTab(_ tab: Tab) {
    EventDispatcher.dispatchEvent(of: self, called: "ShowTab", arguments: tab as AnyObject)
  }
  
  // MARK: Private implementation
  func show(tab: Tab) {
    guard _controller.viewControllers?.contains(tab) == true else {
      return
    }
    _controller.selectedViewController = tab
  }
}

extension UIColor {
  convenience init(hex: Int32) {
    let red = CGFloat((hex & 0xFF0000) >> 16) / 255.0
    let green = CGFloat((hex & 0x00FF00) >> 8) / 255.0
    let blue = CGFloat(hex & 0x0000FF) / 255.0
    
    self.init(red: red, green: green, blue: blue, alpha: 1.0)
  }
  
  func toHex() -> Int32 {
    var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0
    self.getRed(&red, green: &green, blue: &blue, alpha: nil)
    
    let rgb = (Int32)(red * 255) << 16 | (Int32)(green * 255) << 8 | (Int32)(blue * 255) << 0
    return rgb
  }
}
