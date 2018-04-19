// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import UIKit
import Toast_Swift

open class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching {
  fileprivate static var _showListsAsJson = false
  fileprivate let TAG = "Form"
  fileprivate let RESULT_NAME = "APP_INVENTOR_RESULT"
  fileprivate let ARGUMENT_NAME = "APP_INVENTOR_START"
  open let APPINVENTOR_URL_SCHEME = "appinventor"
  open var application: Application?
  weak static var activeForm: Form?
  fileprivate var deviceDensity: Float?
  fileprivate var compatScalingFactor: Float?
  fileprivate var applicationIsBeingClosed = false
  var formName: String = ""
  fileprivate var screenInitialized = false
  fileprivate var _components: [Component] = []
  fileprivate var _aboutScreen: String?
  fileprivate var _appName: String?
  fileprivate var _accentColor: Int32 = Int32(bitPattern: 0xFFFF4081)
  fileprivate var _primaryColor: Int32 = Int32(bitPattern: 0xFF3F51B5)
  fileprivate var _primaryColorDark: Int32 = Int32(bitPattern: 0xFF303F9F)
  fileprivate var _scrollable = false
  fileprivate var _theme = AIComponentKit.Theme.DeviceDefault
  fileprivate var _title = "Screen1"
  fileprivate var _horizontalAlignment = HorizontalGravity.left.rawValue
  fileprivate var _csHorizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft
  fileprivate var _csVerticalAlignment = CSLinearLayoutItemVerticalAlignmentTop
  fileprivate var _verticalAlignment = VerticalGravity.top.rawValue
  fileprivate var _backgroundImage = ""
  fileprivate var _screenInitialized = false
  fileprivate var _startText = ""
  fileprivate var _viewLayout = LinearLayout()
  fileprivate var _compatibilityMode = true
  fileprivate var _verticalLayout = CSLinearLayoutView()
  fileprivate var _verticalItem: CSLinearLayoutItem!
  internal var _componentWithActiveEvent: Component?
  fileprivate var _linearView: LinearView!

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
      _verticalLayout.setNeedsLayout()
      _verticalLayout.setNeedsUpdateConstraints()
      NSLog("Horizontal frame size: \(view.frame)")
      NSLog("Vertical frame size: \(_verticalLayout.frame)")
    } else if let _linearView = _linearView {
      _linearView.addItem(LinearViewItem(component.view))
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
    view.layoutSubviews()
    NSLog("Horizontal frame size: \(view.frame)")
    NSLog("Vertical frame size: \(_verticalLayout.frame)")
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
    form.view.setNeedsLayout()
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
    form.view.setNeedsLayout()
  }

  open func clear() {
    let subviews = view.subviews
    for subview in subviews {
      subview.removeFromSuperview()
    }
    _components = []
    _linearView = LinearView(frame: view.frame)
    _linearView.accessibilityIdentifier = "Form root view"
    view.addSubview(_linearView)
    view.addConstraint(view.widthAnchor.constraint(equalTo: _linearView.widthAnchor, multiplier: 1.0))
    view.addConstraint(view.heightAnchor.constraint(equalTo: _linearView.heightAnchor, multiplier: 1.0))
    view.addConstraint(view.topAnchor.constraint(equalTo: _linearView.topAnchor))
    view.addConstraint(view.leadingAnchor.constraint(equalTo: _linearView.leadingAnchor))
//    let horizontal = CSLinearLayoutView(frame: view.frame)  // vertical alignment
//    horizontal.orientation = CSLinearLayoutViewOrientationHorizontal
//    view = horizontal
//    _verticalLayout = CSLinearLayoutView()
//    _verticalLayout.orientation = CSLinearLayoutViewOrientationVertical
//    _verticalLayout.autoAdjustFrameSize = true
//    _verticalLayout.autoAdjustContentSize = true
//    _verticalLayout.frame.size.width = horizontal.frame.size.width
//    _verticalItem = CSLinearLayoutItem(for: _verticalLayout)
//    horizontal.addItem(_verticalItem)
    defaultPropertyValues()
  }

  open func callInitialize(_ component: Component) {
    if let obj = component as? NSObject {
      if obj.responds(to: #selector(Initialize)) {
        obj.perform(#selector(Initialize))
      }
    }
  }

  fileprivate func defaultPropertyValues() {
    AccentColor = Int32(bitPattern: 0xFFFF4081)
    PrimaryColor = Int32(bitPattern: 0xFF3F51B5)
    PrimaryColorDark = Int32(bitPattern: 0xFF303F9F)
    Theme = "Classic"
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

  open var AccentColor: Int32 {
    get {
      return _accentColor
    }
    set(value) {
      if _accentColor != value {
        _accentColor = value
      }
    }
  }

  open var ActionBar: Bool {
    get {
      return false
    }
    set(actionBar) {
      // Not supported on iOS
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

  open var PrimaryColor: Int32 {
    get {
      return _primaryColor
    }
    set(value) {
      if _primaryColor != value {
        _primaryColor = value
        updateNavbar()
      }
    }
  }

  open var PrimaryColorDark: Int32 {
    get {
      return _primaryColorDark
    }
    set(value) {
      if _primaryColorDark != value {
        _primaryColorDark = value
        updateNavbar()
      }
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

  open var ShowListsAsJson: Bool {
    get {
      return Form._showListsAsJson
    }
    set(show) {
      Form._showListsAsJson = show
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

  open var Theme: String {
    get {
      return _theme.rawValue
    }
    set(value) {
      let newTheme = AIComponentKit.Theme.fromString(value)
      if _theme != newTheme {
        _theme = newTheme
        updateNavbar()
      }
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
    if let error = ErrorMessage(rawValue: Int(errorNumber)) {
      let formattedMessage = String(format: error.message, messageArgs)
      EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: component, functionName as NSString, NSNumber(value: errorNumber), formattedMessage as NSString)
    }
  }

  open func dispatchErrorOccurredEventObjC(_ component: Component, _ functionNames: String, _ errorNumber: Int32, _ messageArgs: [AnyObject]) {
    // TODO: Implementation
  }

  open func dispatchErrorOccurredEventDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    // TODO: Implementation
  }

  open func BackPressed() {
    EventDispatcher.dispatchEvent(of: self, called: "BackPressed")
  }

  open func ErrorOccurred(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: functionName as NSString, NSNumber(value: errorNumber), message as NSString)
  }

  open func ErrorOccurredDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String, _ title: String, _ buttonText: String) {
    // TODO: Implementation
  }

  open func Initialize() {
    _screenInitialized = true
  }

  open func OtherScreenClosed(_ otherScreenName: String, _ result: AnyObject) {
    EventDispatcher.dispatchEvent(of: self, called: "OtherScreenClosed", arguments: otherScreenName as NSString, result)
  }

  open func ScreenOrientationChanged() {
    EventDispatcher.dispatchEvent(of: self, called: "ScreenOrientationChanged")
  }

  // Private implementation

  open func runtimeFormErrorOccurredEvent(_ functionName: String, _ errorNumber: Int32, _ message: String) {
    dispatchErrorOccurredEvent(self, functionName, errorNumber, message as NSString)
  }

  open class func switchForm(_ name: String) {
    activeForm?.doSwitchForm(to: name)
  }

  open class func switchFormWithStartValue(_ name: String, _ value: AnyObject?) {
    activeForm?.doSwitchForm(to: name, startValue: value)
  }

  open func doSwitchForm(to formName: String, startValue: AnyObject? = nil) {
    view.makeToast("Switching screens is not yet supported on iOS")
  }

  open class func closeScreen() {
    activeForm?.doCloseScreen()
  }

  func doCloseScreen(withValue value: AnyObject? = nil) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
      Form.activeForm?.OtherScreenClosed(self.formName, value ?? "" as NSString)
    }
  }

  func doCloseScreen(withPlainText text: String) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
      Form.activeForm?.OtherScreenClosed(self.formName, text as NSString)
    }
  }

  open class func closeApplication() {
    activeForm?.doCloseApplication()
  }

  func doCloseApplication() {
    exit(0)
  }

  open var startValue: AnyObject {
    get {
      do {
        return try getObjectFromJson(_startText) ?? _startText as NSString
      } catch {
        return _startText as NSString
      }
    }
    set(startValue) {
      do {
        _startText = try getJsonRepresentation(startValue)
      } catch {
        _startText = ""
      }
    }
  }

  open class func getStartValue() -> AnyObject {
    if let form = Form.activeForm {
      return form.startValue
    } else {
      return "" as NSString
    }
  }

  open class func closeScreenWithValue(_ value: AnyObject) {
    activeForm?.doCloseScreen(withValue: value)
  }

  open var startText: String {
    get {
      return _startText
    }
    set(startText) {
      _startText = startText
    }
  }

  open class func getStartText() -> String {
    if let form = Form.activeForm {
      return form.startText
    } else {
      return ""
    }
  }

  open class func closeScreenWithPlainText(_ text: String) {
    if let form = activeForm, let vcs = activeForm?.navigationController?.viewControllers {
      if vcs.count > 1 {
        if let parentForm = vcs[vcs.count - 2] as? Form {
          form.doCloseScreen()
          parentForm.OtherScreenClosed(form.formName, text as NSString)
        }
      }
    }
  }

  override open var preferredStatusBarStyle: UIStatusBarStyle {
    switch _theme {
    case .BlackText:
      return .default
    default:
      return .lightContent
    }
  }

  open func updateNavbar() {
    if let parent = navigationController {
      let navbar = parent.navigationBar
      navbar.tintColor = UIColor.white
      navbar.backgroundColor = argbToColor(_primaryColor)
      navbar.titleTextAttributes = [NSForegroundColorAttributeName:UIColor.white]
      switch _theme {
      case .Classic, .DeviceDefault:
        navbar.barTintColor = argbToColor(_primaryColor)
        break
      case .BlackText:
        navbar.barTintColor = argbToColor(_primaryColor)
        navbar.tintColor = UIColor.black
        navbar.titleTextAttributes = [NSForegroundColorAttributeName:UIColor.black]
        break
      case .Dark:
        navbar.barTintColor = argbToColor(_primaryColorDark)
        break
      }
      parent.setNeedsStatusBarAppearanceUpdate()
    }
  }
}
