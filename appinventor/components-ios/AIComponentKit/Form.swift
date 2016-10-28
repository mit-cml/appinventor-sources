//
//  Form.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import UIKit
import Toast_Swift

public class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching {
  private let TAG = "Form"
  private let RESULT_NAME = "APP_INVENTOR_RESULT"
  private let ARGUMENT_NAME = "APP_INVENTOR_START"
  public let APPINVENTOR_URL_SCHEME = "appinventor"
  weak static var activeForm: Form?
  private var deviceDensity: Float?
  private var compatScalingFactor: Float?
  private var applicationIsBeingClosed = false
  var formName: String?
  private var screenInitialized = false
  private var _components: [Component] = []
  private var _aboutScreen: String?
  private var _appName: String?
  private var _scrollable = false
  private var _title = "Screen1"
  private var _horizontalAlignment = HorizontalGravity.left.rawValue
  private var _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft
  private var _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentTop
  private var _verticalAlignment = VerticalGravity.top.rawValue
  private var _backgroundImage = ""
  private var _screenInitialized = false
  private var _viewLayout = LinearLayout()
  private var _compatibilityMode = true
  private var _verticalLayout = CSLinearLayoutView()
  private var _verticalItem: CSLinearLayoutItem!
  
  public func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  public var components: [Component] {
    get {
      return _components
    }
  }
  
  public func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    let canDispatch = _screenInitialized || (self.isEqual(component) && eventName == "Initialize")
    if (!_screenInitialized) {
      NSLog("Attempted to dispatch event \(eventName) to \(component) but screen is not initialized");
    }
    if (canDispatch) {
      Form.activeForm = self
    }
    return canDispatch
  }
  
  public func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    // TODO(ewpatton): Implementation
    return false
  }

  public var dispatchDelegate: HandlesEventDispatching {
    get {
      return self
    }
  }
  
  public var form: Form? {
    get {
      return self
    }
  }

  public func add(_ component: ViewComponent) {
    // TODO(ewpatton): Implementation
    _components.append(component)
    if view is CSLinearLayoutView {
      component.view.sizeToFit()
      let item = CSLinearLayoutItem(for: component.view)
      item?.verticalAlignment = _csVerticalAlignment
      item?.horizontalAlignment = _csHorizontalAlignment
      _verticalLayout.addItem(item!)
      _verticalLayout.layoutSubviews()
      view.layoutSubviews()
      NSLog("Horizontal frame size: \(view.frame)")
      NSLog("Vertical frame size: \(_verticalLayout.frame)")
    } else {
      view.addSubview(component.view)
    }
//    if components.count > 0 && components[components.count-1] is ViewComponent {
//      let lastComponent = components[components.count-1] as! ViewComponent
//      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: lastComponent.view, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 0.0))
//    }
//    view.sizeToFit()
    view.setNeedsLayout()
    view.setNeedsUpdateConstraints()
  }
  
  public func layoutSubviews() {
    _verticalLayout.layoutSubviews()
    view.layoutSubviews()
    NSLog("Vertical frame size: \(_verticalLayout.frame)")
    NSLog("Horizontal frame size: \(view.frame)")
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    // TODO(ewpatton): Implementation
  }
  
  public func setChildHeight(of component: ViewComponent, height: Int32) {
    // TODO(ewpatton): Implementation
  }
  
  public class func switchForm(_ name: String) {
    activeForm?.view.makeToast("Switching screens is not yet supported on iOS")
  }
  
  public class func switchFormWithStartValue(_ name: String, _ value: AnyObject?) {
    activeForm?.view.makeToast("Switching screens is not yet supported on iOS")
  }

  public func clear() {
    let subviews = view.subviews
    for subview in subviews {
      NSLog("Removing subview \(subview)")
      subview.removeFromSuperview()
    }
    _components = []
    let horizontal = CSLinearLayoutView(frame: view.frame)  // vertical alignment
    horizontal.orientation = CSLinearLayoutViewOrientationHorizontal
    view = horizontal
    _verticalLayout = CSLinearLayoutView()
    _verticalLayout.orientation = CSLinearLayoutViewOrientationVertical
    _verticalLayout.autoAdjustFrameSize = true
    _verticalLayout.autoAdjustContentSize = true
    _verticalLayout.frame.size.width = horizontal.frame.size.width
    _verticalItem = CSLinearLayoutItem(for: _verticalLayout)
    horizontal.addItem(_verticalItem)
    defaultPropertyValues()
  }
  
  public func callInitialize(_ component: Component) {
    //TODO: implementation
  }
  
  private func defaultPropertyValues() {
    Scrollable = false
    Sizing = "Fixed"
    BackgroundImage = ""
    AboutScreen = ""
    BackgroundColor = Int32(bitPattern: Color.white.rawValue)
    AlignHorizontal = HorizontalGravity.left.rawValue
    AlignVertical = VerticalGravity.top.rawValue
    self.setTitle(title: "")
    ShowStatusBar = true
    TitleVisible = true
  }
  
  // MARK: Form Properties
  public var AboutScreen: String? {
    get {
      return _aboutScreen
    }
    set(aboutScreen) {
      _aboutScreen = aboutScreen
    }
  }
  
  public var AlignHorizontal: Int32 {
    get {
      return _horizontalAlignment
    }
    set(alignment) {
      if view is CSLinearLayoutView, let halign = HorizontalGravity(rawValue: alignment) {
        _horizontalAlignment = alignment
        switch(halign) {
          case .left:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft
            break
          case .center:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentCenter
            break
          case .right:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentRight
            break
        }
        let items = _verticalLayout.items
        for item in items! {
          let viewitem = item as! CSLinearLayoutItem
          viewitem.horizontalAlignment = _csHorizontalAlignment
        }
        view.layoutSubviews()
      }
    }
  }
  
  public var AlignVertical: Int32 {
    get {
      return _verticalAlignment
    }
    set(alignment) {
      if view is CSLinearLayoutView, let valign = VerticalGravity(rawValue: alignment) {
        _verticalAlignment = alignment
        switch(valign) {
        case .top:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentTop
          break
        case .center:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentCenter
          break
        case .bottom:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentBottom
          break
        }
        _verticalItem.verticalAlignment = _csVerticalAlignment
        view.layoutSubviews()
      }
    }
  }

  public var AppName: String? {
    get {
      return _appName
    }
    set(aName) {
      _appName = aName
    }
  }
  
  public var BackgroundColor: Int32 {
    get {
      return colorToArgb(self.view.backgroundColor!)
    }
    set(argb) {
      self.view.backgroundColor = argbToColor(argb)
    }
  }
  
  public var BackgroundImage: String {
    get {
      return _backgroundImage
    }
    set(path) {
      if let image = UIImage(named: path) {
        self.view.backgroundColor = UIColor(patternImage: image)
        _backgroundImage = path
      }
    }
  }
  
  public var CloseScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }
  
  public var Height: Int32 {
    get {
      // TODO(ewpatton): Implementation
      return 0
    }
    set {
      // TODO(ewpatton): Implementation
    }
  }
  
  public var Icon: String {
    get {
      return ""
    }
    set(icon) {
      
    }
  }
  
  public var OpenScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }
  
  public var ScreenOrientation: String {
    get {
      return "portrait"
    }
    set(orientation) {
      
    }
  }
  
  public var Scrollable: Bool {
    get {
      return _scrollable
    }
    set(scrollable) {
      _scrollable = scrollable
    }
  }
  
  public var ShowStatusBar: Bool {
    get {
      return UIApplication.shared.isStatusBarHidden
    }
    set(show) {
      UIApplication.shared.isStatusBarHidden = !show
    }
  }
  
  public var Sizing: String {
    get {
      return _compatibilityMode ? "Fixed" : "Responsive"
    }
    set(value) {
      if (value == "Fixed") {
        _compatibilityMode = true
      } else {
        _compatibilityMode = false
      }
    }
  }
  
  // Title has a different approach due to UIViewController having a setTitle method.
  public func Title() -> String {
    return super.title!
  }
  
  public func setTitle(title: String) {
    _title = title
    super.title = title
  }
  
  public var TitleVisible: Bool {
    get {
      return (self.navigationController?.isNavigationBarHidden)!
    }
    set(show) {
      self.navigationController?.setNavigationBarHidden(!show, animated: true)
    }
  }
  
  public var Width: Int32 {
    get {
      // TODO(ewpatton): Implementation
      return 0
    }
    set {
      // TODO(ewpatton): Implementation
    }
  }

  // MARK: Form Methods
  public func Initialize() {
    _screenInitialized = true
  }
  
}
