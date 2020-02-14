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
  fileprivate var _components: [Component] = []
  fileprivate var _aboutScreen: String?
  fileprivate var _appName: String?
  fileprivate var _accentColor: Int32 = Int32(bitPattern: 0xFFFF4081)
  fileprivate var _primaryColor: Int32 = Int32(bitPattern: 0xFF3F51B5)
  fileprivate var _primaryColorDark: Int32 = Int32(bitPattern: 0xFF303F9F)
  fileprivate var _scrollable = false
  fileprivate var _theme = AIComponentKit.Theme.Classic
  fileprivate var _title = "Screen1"
  fileprivate var _horizontalAlignment = HorizontalGravity.left.rawValue
  fileprivate var _verticalAlignment = VerticalGravity.top.rawValue
  private var _backgroundColor: Int32 = Color.default.int32
  fileprivate var _backgroundImage = ""
  fileprivate var _screenInitialized = false
  fileprivate var _startText = ""
  fileprivate var _compatibilityMode = true
  @objc internal var _componentWithActiveEvent: Component?
  fileprivate var _statusBarHidden: Bool = true
  fileprivate var _linearView = LinearView()
  fileprivate var _scaleFrameLayout = ScaleFrameLayout(frame: CGRect(x: 0, y: 0, width: 320, height: 480))
  // For screen switching
  var lastFormName = ""
  var formResult: AnyObject?
  private var _orientation = "unspecified"

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

  open override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    if isMovingFromParent {
      if let vcs = navigationController?.viewControllers, let parent = vcs.last as? Form {
        parent.lastFormName = formName
        if parent.formResult == nil {
          // No close value provided, so we will use the empty string
          parent.formResult = "" as AnyObject
        }
      }
    }
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
    // Will be overriden in compiled apps
    return false
  }

  open func dispatchGenericEvent(of component: Component, eventName: String, unhandled: Bool, arguments: [AnyObject]) {
    // Will be overriden in compiled apps
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
    if width <= kLengthPercentTag {
      _linearView.setWidth(of: component.view, to: Length(percent: width, of: _scaleFrameLayout))
    } else if width == kLengthPreferred {
      _linearView.setWidth(of: component.view, to: .Automatic)
    } else if width == kLengthFillParent {
      _linearView.setWidth(of: component.view, to: .FillParent)
    } else {
      _linearView.setWidth(of: component.view, to: Length(pixels: width))
    }
    _linearView.setNeedsLayout()
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    if height <= kLengthPercentTag {
      _linearView.setHeight(of: component.view, to: Length(percent: height, of: _scaleFrameLayout))
    } else if height == kLengthPreferred {
      _linearView.setHeight(of: component.view, to: .Automatic)
    } else if height == kLengthFillParent {
      _linearView.setHeight(of: component.view, to: .FillParent)
    } else {
      _linearView.setHeight(of: component.view, to: Length(pixels: height))
    }
    _linearView.setNeedsLayout()
  }

  open var scaleFrameLayout: ScaleFrameLayout {
    return _scaleFrameLayout
  }

  @objc open func clear() {
    let subviews = view.subviews
    for subview in subviews {
      subview.removeFromSuperview()
    }
    clearComponents()
    defaultPropertyValues()
  }

  private func recomputeLayout() {
    _linearView.removeFromSuperview()
    _scaleFrameLayout.removeFromSuperview()
    _linearView = LinearView(frame: view.frame)
    _linearView.accessibilityIdentifier = "Form root view"
    if _compatibilityMode {
      _scaleFrameLayout = ScaleFrameLayout(frame: CGRect(x: 0, y: 0, width: 320, height: view.frame.height / _scaleFrameLayout.scale))
    } else {
      _scaleFrameLayout = ScaleFrameLayout(frame: CGRect(origin: .zero, size: view.frame.size))
    }
    _scaleFrameLayout.mode = _compatibilityMode ? .Fixed : .Responsive
    _scaleFrameLayout.addSubview(_linearView)
    view.addSubview(_scaleFrameLayout)
    _scaleFrameLayout.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
    _scaleFrameLayout.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
    _scaleFrameLayout.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1.0/_scaleFrameLayout.scale).isActive = true
    _scaleFrameLayout.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 1.0/_scaleFrameLayout.scale).isActive = true
    _linearView.topAnchor.constraint(equalTo: _scaleFrameLayout.topAnchor).isActive = true
    _linearView.leadingAnchor.constraint(equalTo: _scaleFrameLayout.leadingAnchor).isActive = true
    _linearView.widthAnchor.constraint(equalTo: _scaleFrameLayout.widthAnchor).isActive = true
    _linearView.heightAnchor.constraint(equalTo: _scaleFrameLayout.heightAnchor).isActive = true
    _linearView.horizontalAlignment = HorizontalGravity(rawValue: _horizontalAlignment)!
    _linearView.verticalAlignment = VerticalGravity(rawValue: _verticalAlignment)!
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
    Sizing = "Responsive"
    BackgroundImage = ""
    AboutScreen = ""
    BackgroundColor = Int32(bitPattern: Color.default.rawValue)
    AlignHorizontal = HorizontalGravity.left.rawValue
    AlignVertical = VerticalGravity.top.rawValue
    self.title = ""
    ShowStatusBar = true
    TitleVisible = true
    ShowListsAsJson = true
    ScreenOrientation = "unspecified"
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
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      if argb == Color.default.int32 {
        view.backgroundColor = preferredBackgroundColor(self)
      } else {
        self.view.backgroundColor = argbToColor(argb)
      }
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

  @objc open var Name: String {
    get {
      return formName
    }
    set(name) {
      formName = name
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
      return _orientation
    }
    set(orientation) {
      _orientation = orientation
      if _orientation == "portrait" {
        UIDevice.current.setValue(UIDeviceOrientation.portrait.rawValue, forKey: "orientation")
      } else if _orientation == "landscape" {
        UIDevice.current.setValue(UIDeviceOrientation.landscapeLeft.rawValue, forKey: "orientation")
      } else {
        UIDevice.current.setValue(UIDeviceOrientation.unknown.rawValue, forKey: "orientation")
      }
      UINavigationController.attemptRotationToDeviceOrientation()
    }
  }

  @objc open var Scrollable: Bool {
    get {
      return _scrollable
    }
    set(scrollable) {
      _scrollable = scrollable
      recomputeLayout()
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
      setNeedsStatusBarAppearanceUpdate()
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
      _scaleFrameLayout.mode = _compatibilityMode ? .Fixed : .Responsive
      _scaleFrameLayout.setNeedsLayout()
      _linearView.setNeedsLayout()
      recomputeLayout()
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
      }
      if _backgroundColor == Color.default.int32 {
        view.backgroundColor = preferredBackgroundColor(self)
      }
      updateNavbar()
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
    runOnUiThread {
      if let error = ErrorMessage(rawValue: Int(errorNumber)) {
        let formattedMessage = String(format: error.message, messageArgs: messageArgs)
        self.ErrorOccurred(component, functionName, errorNumber, formattedMessage)
      }
    }
  }

  @objc open func dispatchErrorOccurredEventObjC(_ component: Component, _ functionNames: String, _ errorNumber: Int32, _ messageArgs: [AnyObject]) {
    runOnUiThread {
      if let error = ErrorMessage(rawValue: Int(errorNumber)) {
        let formattedMessage = String(format: error.message, messageArgs: messageArgs)
        self.ErrorOccurred(component, functionNames, errorNumber, formattedMessage)
      }
    }
  }

  open func dispatchErrorOccurredEventDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ messageArgs: Any...) {
    runOnUiThread {
      if let error = ErrorMessage(rawValue: Int(errorNumber)) {
        let formattedMessage = String(format: error.message, messageArgs: messageArgs)
        self.ErrorOccurredDialog(component, functionName, errorNumber, formattedMessage, "Error in \(functionName)", "Dismiss")
      }
    }
  }

  @objc open func BackPressed() {
    EventDispatcher.dispatchEvent(of: self, called: "BackPressed")
  }

  @objc open func ErrorOccurred(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String) {
    let handled = EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred", arguments: component as AnyObject, functionName as NSString, NSNumber(value: errorNumber), message as NSString)
    if !handled && _screenInitialized {
      Notifier(self).ShowAlert("Error \(errorNumber): \(message)")
    }
  }

  @objc open func ErrorOccurredDialog(_ component: Component, _ functionName: String, _ errorNumber: Int32, _ message: String, _ title: String, _ buttonText: String) {
    let handled = EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurredDialog", arguments: component as AnyObject, functionName as NSString, NSNumber(value: errorNumber), message as NSString, title as NSString, buttonText as NSString)
    if !handled && _screenInitialized {
      Notifier(self).ShowMessageDialog("Error \(errorNumber): \(message)", title, buttonText)
    }
  }

  @objc open func Initialize() {
    EventDispatcher.dispatchEvent(of: self, called: "Initialize")
    _screenInitialized = true
    if let previousFormValue = formResult {
      OtherScreenClosed(lastFormName, previousFormValue)
      // Clear these values for the next round through
      formResult = nil
      lastFormName = ""
    }
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
    }
    if let vcs = navigationController?.viewControllers, let parentForm = vcs.last as? Form {
      parentForm.lastFormName = self.formName
      parentForm.formResult = value ?? "" as AnyObject
    }
  }

  @objc func doCloseScreen(withPlainText text: String) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
    }
    if let vcs = navigationController?.viewControllers, let parentForm = vcs.last as? Form {
      parentForm.lastFormName = self.formName
      parentForm.formResult = text as AnyObject
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
        form.doCloseScreen(withPlainText: text)
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

  var widthAnchor: NSLayoutDimension {
    return _scaleFrameLayout.widthAnchor
  }

  var heightAnchor: NSLayoutDimension {
    return _scaleFrameLayout.heightAnchor
  }

  /**
   * Run the given closure on the UI thread.
   *
   * @param code The closure to execute on the main (UI) thread.
   */
  open func runOnUiThread(_ code: @escaping () -> ()) {
    DispatchQueue.main.async(execute: code)
  }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToOptionalNSAttributedStringKeyDictionary(_ input: [String: Any]?) -> [NSAttributedString.Key: Any]? {
	guard let input = input else { return nil }
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (NSAttributedString.Key(rawValue: key), value)})
}
