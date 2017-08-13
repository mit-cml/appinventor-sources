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

open class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching {
  fileprivate let TAG = "Form"
  fileprivate let RESULT_NAME = "APP_INVENTOR_RESULT"
  fileprivate let ARGUMENT_NAME = "APP_INVENTOR_START"
  open let APPINVENTOR_URL_SCHEME = "appinventor"
  open var application: Application?
  weak static var activeForm: Form?
  fileprivate var deviceDensity: Float?
  fileprivate var compatScalingFactor: Float?
  fileprivate var applicationIsBeingClosed = false
  var formName: String?
  fileprivate var screenInitialized = false
  fileprivate var _components: [Component] = []
  fileprivate var _aboutScreen: String?
  fileprivate var _appName: String?
  fileprivate var _scrollable = false
  fileprivate var _title = "Screen1"
  fileprivate var _horizontalAlignment = HorizontalGravity.left.rawValue
  fileprivate var _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft
  fileprivate var _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentTop
  fileprivate var _verticalAlignment = VerticalGravity.top.rawValue
  fileprivate var _backgroundImage = ""
  fileprivate var _screenInitialized = false
  fileprivate var _viewLayout = LinearLayout()
  fileprivate var _compatibilityMode = true
  fileprivate var _verticalLayout = CSLinearLayoutView()
  fileprivate var _verticalItem: CSLinearLayoutItem!
  internal var _componentWithActiveEvent: Component?
  
  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  open var components: [Component] {
    get {
      return _components
    }
  }

  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    Form.activeForm = self
  }
  
  open func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    let canDispatch = _screenInitialized || (self.isEqual(component) && eventName == "Initialize")
    if (!_screenInitialized) {
      NSLog("Attempted to dispatch event \(eventName) to \(component) but screen is not initialized");
    }
    if (canDispatch) {
      Form.activeForm = self
    }
    return canDispatch
  }
  
  open func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    // TODO(ewpatton): Implementation
    return false
  }

  open var dispatchDelegate: HandlesEventDispatching {
    get {
      return self
    }
  }

  open var form: Form {
    get {
      return self
    }
  }

  open override func viewDidLoad() {
    super.viewDidLoad()
    view.accessibilityIdentifier = String(describing: type(of: self))
  }

  open func add(_ component: ViewComponent) {
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
      if _components.count > 1, let priorComponent = _components[_components.count - 2] as? ViewComponent {
        view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .top, relatedBy: .equal, toItem: priorComponent.view, attribute: .bottom, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
      }
      if AlignHorizontal == HorizontalGravity.center.rawValue {
        view.addConstraint(component.view.centerXAnchor.constraint(equalTo: view.centerXAnchor))
      }
    }
//    if components.count > 0 && components[components.count-1] is ViewComponent {
//      let lastComponent = components[components.count-1] as! ViewComponent
//      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: lastComponent.view, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 0.0))
//    }
//    view.sizeToFit()
    view.setNeedsLayout()
    view.setNeedsUpdateConstraints()
  }
  
  open func layoutSubviews() {
    _verticalLayout.layoutSubviews()
    view.layoutSubviews()
    NSLog("Vertical frame size: \(_verticalLayout.frame)")
    NSLog("Horizontal frame size: \(view.frame)")
  }

  open func setChildWidth(of component: ViewComponent, width: Int32) {
    if width >= 0 {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(width)))
    } else if width == kLengthPreferred {
      view.addConstraint(NSLayoutConstraint(item: view, attribute: .width, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width <= kLengthPercentTag {
      let width = -(width + 1000)
      let pWidth = CGFloat(width) / CGFloat(100.0)
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: view, attribute: .width, multiplier: pWidth, constant: CGFloat(0.0)))
    } else {
      NSLog("Unable to process width value \(width)")
    }
  }
  
  open func setChildHeight(of component: ViewComponent, height: Int32) {
    if height >= 0 {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(height)))
    } else if height == kLengthPreferred {
      let contentSize = component.view.sizeThatFits(view.frame.size)
      component.view.addConstraint(component.view.heightAnchor.constraint(greaterThanOrEqualToConstant: contentSize.height))
      view.addConstraint(NSLayoutConstraint(item: view, attribute: .height, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height <= kLengthPercentTag {
      let height = -(height + 1000)
      let pHeight = CGFloat(height) / CGFloat(100.0)
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: view, attribute: .height, multiplier: pHeight, constant: CGFloat(0.0)))
    } else {
      NSLog("Unable to process width value \(height)")
    }
  }
  
  open class func switchForm(_ name: String) {
    activeForm?.view.makeToast("Switching screens is not yet supported on iOS")
  }
  
  open class func switchFormWithStartValue(_ name: String, _ value: AnyObject?) {
    activeForm?.view.makeToast("Switching screens is not yet supported on iOS")
  }

  open func clear() {
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
  
  open func callInitialize(_ component: Component) {
    //TODO: implementation
  }
  
  fileprivate func defaultPropertyValues() {
    Scrollable = false
    Sizing = "Fixed"
    BackgroundImage = ""
    AboutScreen = ""
    BackgroundColor = Int32(bitPattern: Color.white.rawValue)
    AlignHorizontal = HorizontalGravity.left.rawValue
    AlignVertical = VerticalGravity.top.rawValue
    self.title = ""
    ShowStatusBar = true
    TitleVisible = true
  }
  
  // MARK: Form Properties
  open var AboutScreen: String? {
    get {
      return _aboutScreen
    }
    set(aboutScreen) {
      _aboutScreen = aboutScreen
    }
  }
  
  open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlignment
    }
    set(alignment) {
      if let halign = HorizontalGravity(rawValue: alignment) {
        _horizontalAlignment = alignment
        switch(halign) {
          case .left:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft
          case .center:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentCenter
          case .right:
            _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentRight
        }
        if view is CSLinearLayoutView {
          let items = _verticalLayout.items
          for item in items! {
            let viewitem = item as! CSLinearLayoutItem
            viewitem.horizontalAlignment = _csHorizontalAlignment
          }
        } else {
          // TODO(ewpatton): Update existing constraints
        }
        view.layoutSubviews()
      }
    }
  }
  
  open var AlignVertical: Int32 {
    get {
      return _verticalAlignment
    }
    set(alignment) {
      if let valign = VerticalGravity(rawValue: alignment) {
        _verticalAlignment = alignment
        switch(valign) {
        case .top:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentTop
        case .center:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentCenter
        case .bottom:
          _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentBottom
        }
        if view is CSLinearLayoutView {
          _verticalItem.verticalAlignment = _csVerticalAlignment
        }
        view.layoutSubviews()
      }
    }
  }

  open var AppName: String? {
    get {
      return _appName
    }
    set(aName) {
      _appName = aName
    }
  }
  
  open var BackgroundColor: Int32 {
    get {
      return colorToArgb(self.view.backgroundColor!)
    }
    set(argb) {
      self.view.backgroundColor = argbToColor(argb)
    }
  }
  
  open var BackgroundImage: String {
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
  
  open var CloseScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }
  
  open var Height: Int32 {
    get {
      if let height = view.window?.frame.size.height {
        return Int32(height)
      } else {
        return 0
      }
    }
    set {
      NSLog("Cannot set Height of Form")
    }
  }
  
  open var Icon: String {
    get {
      return ""
    }
    set(icon) {
      
    }
  }
  
  open var OpenScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }
  
  open var ScreenOrientation: String {
    get {
      return "portrait"
    }
    set(orientation) {
      
    }
  }
  
  open var Scrollable: Bool {
    get {
      return _scrollable
    }
    set(scrollable) {
      _scrollable = scrollable
    }
  }
  
  open var ShowStatusBar: Bool {
    get {
      return UIApplication.shared.isStatusBarHidden
    }
    set(show) {
      UIApplication.shared.isStatusBarHidden = !show
    }
  }
  
  open var Sizing: String {
    get {
      return _compatibilityMode ? "Fixed" : "Responsive"
    }
    set(value) {
      _compatibilityMode = (value == "Fixed")
    }
  }
  
  // Title has a different approach due to UIViewController having a setTitle method.
  open override var title: String? {
    get {
      return super.title
    }
    set(title) {
      _title = title!
      super.title = title
    }
  }

  // For App Inventor naming compatability
  open func Title() -> String {
    return super.title!
  }

  open var TitleVisible: Bool {
    get {
      return (self.navigationController?.isNavigationBarHidden)!
    }
    set(show) {
      self.navigationController?.setNavigationBarHidden(!show, animated: true)
    }
  }
  
  open var Width: Int32 {
    get {
      if let width =  view.window?.frame.size.width {
        return Int32(width)
      } else {
        return 0
      }
    }
    set {
      NSLog("Cannot set Width of Form")
    }
  }

  // MARK: Form Methods
  
  // MARK: Form Events
  open func dispatchErrorOccurredEvent(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    // TODO: Implementation
  }
  
  open func dispatchErrorOccurredEventDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    // TODO: Implementation
  }
  
  open func ErrorOccurred(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String) {
    // TODO: Implementation
  }
  
  open func ErrorOccurredDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String, _ title: String, _ buttonText: String) {
    // TODO: Implementation
  }
  
  open func Initialize() {
    _screenInitialized = true
  }

  open func ScreenOrientationChanged() {
    EventDispatcher.dispatchEvent(of: self, called: "ScreenOrientationChanged")
  }
}
