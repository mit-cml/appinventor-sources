// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import UIKit
import Toast_Swift

@objc open class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching, LifecycleDelegate {
  fileprivate static var _showListsAsJson = false
  fileprivate let TAG = "Form"
  fileprivate let RESULT_NAME = "APP_INVENTOR_RESULT"
  fileprivate let ARGUMENT_NAME = "APP_INVENTOR_START"
  @objc public let APPINVENTOR_URL_SCHEME = "appinventor"
  @objc open var application: Application?
  @objc weak static var activeForm: Form?
  fileprivate var deviceDensity: Float = 1.0
  fileprivate var compatScalingFactor: Float?
  fileprivate var applicationIsBeingClosed = false
  @objc var formName: String = ""
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
  fileprivate var _verticalAlignment = VerticalGravity.top.rawValue
  fileprivate var _backgroundImage = ""
  fileprivate var _screenInitialized = false
  fileprivate var _startText = ""
  fileprivate var _viewLayout = LinearLayout()
  fileprivate var _compatibilityMode = true
  @objc internal var _componentWithActiveEvent: Component?
  fileprivate var _statusBarHidden: Bool = true
  fileprivate var _linearView = LinearView()

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  @objc open var components: [Component] {
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
    _components.append(component)
    _linearView.addItem(LinearViewItem(component.view))
    view.setNeedsLayout()
    view.setNeedsUpdateConstraints()
  }

  @objc open func layoutSubviews() {
    view.layoutSubviews()
    NSLog("Horizontal frame size: \(view.frame)")
    NSLog("Vertical frame size: \(_linearView.frame)")
  }

  private var _dimensions = [Int:NSLayoutConstraint]()

  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    let hash = component.view.hash &* 2
    if let oldConstraint = _dimensions.removeValue(forKey: hash) {
      oldConstraint.isActive = false
    }
    form.view.setNeedsLayout()
    var constraint: NSLayoutConstraint!
    if width >= 0 {
      constraint = NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(width))
    } else if width == kLengthPreferred {
      constraint = NSLayoutConstraint(item: view, attribute: .width, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0))
    } else if width == kLengthFillParent {
      constraint = component.view.widthAnchor.constraint(equalTo: _scaleFrameLayout.widthAnchor)
    } else if width <= kLengthPercentTag {
      let percent = CGFloat(Double(-(width + 1000)) / 100.0)
      constraint = component.view.widthAnchor.constraint(equalTo: _scaleFrameLayout.widthAnchor, multiplier: percent)
    } else {
      NSLog("Unable to process width value \(width)")
      return
    }
    constraint.isActive = true
    _dimensions[hash] = constraint
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    let hash = component.view.hash &* 2 | 1
    if let oldConstraint = _dimensions.removeValue(forKey: hash) {
      oldConstraint.isActive = false
    }
    form.view.setNeedsLayout()
    var constraint: NSLayoutConstraint!
    if height >= 0 {
      constraint = NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(height))
    } else if height == kLengthPreferred {
      constraint = NSLayoutConstraint(item: view, attribute: .height, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0))
    } else if height == kLengthFillParent {
      constraint = component.view.heightAnchor.constraint(equalTo: _scaleFrameLayout.heightAnchor)
    } else if height <= kLengthPercentTag {
      let percent = CGFloat(Double(-(height + 1000)) / 100.0)
      constraint = component.view.heightAnchor.constraint(equalTo: _scaleFrameLayout.heightAnchor, multiplier: percent)
    } else {
      NSLog("Unable to process width value \(height)")
      return
    }
    constraint.isActive = true
    _dimensions[hash] = constraint
  }

  @objc open func clear() {
    let subviews = view.subviews
    for subview in subviews {
      subview.removeFromSuperview()
    }
    clearComponents()
    _linearView = LinearView(frame: view.frame)
    _linearView.accessibilityIdentifier = "Form root view"
    view.addSubview(_linearView)
    view.addConstraint(view.widthAnchor.constraint(equalTo: _linearView.widthAnchor))
    view.addConstraint(view.heightAnchor.constraint(equalTo: _linearView.heightAnchor))
    view.addConstraint(view.topAnchor.constraint(equalTo: _linearView.topAnchor))
    view.addConstraint(view.leadingAnchor.constraint(equalTo: _linearView.leadingAnchor))
    defaultPropertyValues()
  }

  @objc func clearComponents() {
    onDelete()
    _components = []
  }

  @objc public func onResume() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onResume()
      }
    }
    Notifier.notices.setPausedState(false)
  }

  @objc open func onPause() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onPause()
      }
    }
    Notifier.notices.setPausedState(true)
  }

  @objc open func onDelete() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onDelete()
      }
    }
    Notifier.notices.clearNotices()
  }

  @objc public func onDestroy() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onDestroy()
      }
    }
    Notifier.notices.clearNotices()
  }

  @objc open func callInitialize(_ component: Component) {
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
    ShowListsAsJson = false
  }
  
  // MARK: Form Properties
  @objc open var AboutScreen: String? {
    get {
      return _aboutScreen
    }
    set(aboutScreen) {
      _aboutScreen = aboutScreen
    }
  }

  @objc open var AccentColor: Int32 {
    get {
      return _accentColor
    }
    set(value) {
      if _accentColor != value {
        _accentColor = value
      }
    }
  }

  @objc open var ActionBar: Bool {
    get {
      return false
    }
    set(actionBar) {
      // Not supported on iOS
    }
  }

  @objc open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlignment
    }
    set(alignment) {
      if let halign = HorizontalGravity(rawValue: alignment) {
        _horizontalAlignment = alignment
        _linearView.horizontalAlignment = halign
        view.setNeedsLayout()
      }
    }
  }
  
  @objc open var AlignVertical: Int32 {
    get {
      return _verticalAlignment
    }
    set(alignment) {
      if let valign = VerticalGravity(rawValue: alignment) {
        _verticalAlignment = alignment
        _linearView.verticalAlignment = valign
        view.setNeedsLayout()
      }
    }
  }

  @objc open var AppName: String? {
    get {
      return _appName
    }
    set(aName) {
      _appName = aName
    }
  }

  @objc open var BackgroundColor: Int32 {
    get {
      return colorToArgb(self.view.backgroundColor!)
    }
    set(argb) {
      self.view.backgroundColor = argbToColor(argb)
    }
  }

  @objc open var BackgroundImage: String {
    get {
      return _backgroundImage
    }
    set(path) {
      if let image = UIImage(contentsOfFile: AssetManager.shared.pathForExistingFileAsset(path)) {
        _linearView.backgroundColor = UIColor(patternImage: image)
        _backgroundImage = path
      }
    }
  }

  @objc open var CloseScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }
  
  @objc open var DeviceDensity: Float {
    get {
      return deviceDensity
    }
    set(density) {
      deviceDensity = density
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

  @objc open var Icon: String {
    get {
      return ""
    }
    set(icon) {
      
    }
  }

  @objc open var OpenScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }

  @objc open var PrimaryColor: Int32 {
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

  @objc open var PrimaryColorDark: Int32 {
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

  @objc open var ScreenOrientation: String {
    get {
      return "portrait"
    }
    set(orientation) {
      
    }
  }

  @objc open var Scrollable: Bool {
    get {
      return _scrollable
    }
    set(scrollable) {
      _scrollable = scrollable
    }
  }

  @objc open var ShowListsAsJson: Bool {
    get {
      return Form._showListsAsJson
    }
    set(show) {
      Form._showListsAsJson = show
    }
  }

  @objc open var ShowStatusBar: Bool {
    get {
      return UIApplication.shared.isStatusBarHidden
    }
    set(show) {
      _statusBarHidden = !show
    }
  }
  
  open override var prefersStatusBarHidden: Bool {
    return _statusBarHidden
  }

  @objc open var Sizing: String {
    get {
      return _compatibilityMode ? "Fixed" : "Responsive"
    }
    set(value) {
      _compatibilityMode = (value == "Fixed")
    }
  }

  @objc open var Theme: String {
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
  @objc open func Title() -> String {
    return super.title!
  }

  @objc open var TitleVisible: Bool {
    get {
      return (self.navigationController?.isNavigationBarHidden)!
    }
    set(show) {
      self.navigationController?.setNavigationBarHidden(!show, animated: true)
    }
  }

  @objc open var TutorialURL: String {
    get {
      return ""  // not used in companion
    }
    set(url) {
      // not used in companion
    }
  }

  @objc open var VersionCode: Int32 {
    get {
      return 1  // not used in companion
    }
    set(version) {
      // not used in companion
    }
  }

  @objc open var VersionName: String {
    get {
      return ""  // not used in companion
    }
    set(version) {
      // not used in companion
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
  @objc open func HideKeyboard() {
    self.view.endEditing(true)
  }

  // MARK: Form Events
  open func dispatchErrorOccurredEvent(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    if let error = ErrorMessage(rawValue: Int(errorNumber)) {
      let formattedMessage = String(format: error.message, messageArgs)
      EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: component, functionName as NSString, NSNumber(value: errorNumber), formattedMessage as NSString)
    }
  }

  @objc open func dispatchErrorOccurredEventObjC(_ component: Component, _ functionNames: String, _ errorNumber: Int32, _ messageArgs: [AnyObject]) {
    // TODO: Implementation
  }

  open func dispatchErrorOccurredEventDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    // TODO: Implementation
  }

  @objc open func BackPressed() {
    EventDispatcher.dispatchEvent(of: self, called: "BackPressed")
  }

  @objc open func ErrorOccurred(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: functionName as NSString, NSNumber(value: errorNumber), message as NSString)
  }

  @objc open func ErrorOccurredDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String, _ title: String, _ buttonText: String) {
    // TODO: Implementation
  }

  @objc open func Initialize() {
    _screenInitialized = true
  }

  @objc open func OtherScreenClosed(_ otherScreenName: String, _ result: AnyObject) {
    EventDispatcher.dispatchEvent(of: self, called: "OtherScreenClosed", arguments: otherScreenName as NSString, result)
  }

  @objc open func ScreenOrientationChanged() {
    EventDispatcher.dispatchEvent(of: self, called: "ScreenOrientationChanged")
  }

  // Private implementation

  @objc public class func getActiveForm() -> Form? {
    return Form.activeForm
  }

  @objc open func runtimeFormErrorOccurredEvent(_ functionName: String, _ errorNumber: Int32, _ message: String) {
    dispatchErrorOccurredEvent(self, functionName, errorNumber, message as NSString)
  }

  @objc open class func switchForm(_ name: String) {
    activeForm?.doSwitchForm(to: name)
  }

  @objc open class func switchFormWithStartValue(_ name: String, _ value: AnyObject?) {
    activeForm?.doSwitchForm(to: name, startValue: value)
  }

  @objc open func doSwitchForm(to formName: String, startValue: AnyObject? = nil) {
    view.makeToast("Switching screens is not yet supported on iOS")
  }

  @objc open class func closeScreen() {
    activeForm?.doCloseScreen()
  }

  @objc func doCloseScreen(withValue value: AnyObject? = nil) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
      Form.activeForm?.OtherScreenClosed(self.formName, value ?? "" as NSString)
    }
  }

  @objc func doCloseScreen(withPlainText text: String) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
      Form.activeForm?.OtherScreenClosed(self.formName, text as NSString)
    }
  }

  @objc open class func closeApplication() {
    activeForm?.doCloseApplication()
  }

  @objc func doCloseApplication() {
    exit(0)
  }

  @objc open var startValue: AnyObject {
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

  @objc open class func getStartValue() -> AnyObject {
    if let form = Form.activeForm {
      return form.startValue
    } else {
      return "" as NSString
    }
  }

  @objc open class func closeScreenWithValue(_ value: AnyObject) {
    activeForm?.doCloseScreen(withValue: value)
  }

  @objc open var startText: String {
    get {
      return _startText
    }
    set(startText) {
      _startText = startText
    }
  }

  @objc open class func getStartText() -> String {
    if let form = Form.activeForm {
      return form.startText
    } else {
      return ""
    }
  }

  @objc open class func closeScreenWithPlainText(_ text: String) {
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

  @objc open func updateNavbar() {
    if let parent = navigationController {
      let navbar = parent.navigationBar
      navbar.tintColor = UIColor.white
      navbar.backgroundColor = argbToColor(_primaryColor)
      navbar.titleTextAttributes = convertToOptionalNSAttributedStringKeyDictionary([NSAttributedString.Key.foregroundColor.rawValue:UIColor.white])
      switch _theme {
      case .Classic, .DeviceDefault:
        navbar.barTintColor = argbToColor(_primaryColor)
        break
      case .BlackText:
        navbar.barTintColor = argbToColor(_primaryColor)
        navbar.tintColor = UIColor.black
        navbar.titleTextAttributes = convertToOptionalNSAttributedStringKeyDictionary([NSAttributedString.Key.foregroundColor.rawValue:UIColor.black])
        break
      case .Dark:
        navbar.barTintColor = argbToColor(_primaryColorDark)
        break
      }
      parent.setNeedsStatusBarAppearanceUpdate()
    }
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToOptionalNSAttributedStringKeyDictionary(_ input: [String: Any]?) -> [NSAttributedString.Key: Any]? {
	guard let input = input else { return nil }
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (NSAttributedString.Key(rawValue: key), value)})
}
