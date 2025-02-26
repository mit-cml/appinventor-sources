// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit
import Toast_Swift

let kMinimumToastWait = 10.0

@objc open class Form: UIKit.UIViewController, Component, ComponentContainer, HandlesEventDispatching, LifecycleDelegate, NeedsWeakReference {
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
  fileprivate var _defaultFileScope = FileScope.App
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
  fileprivate let _isPhone = UIDevice.current.userInterfaceIdiom == .phone
  // For screen switching
  var lastFormName = ""
  var formResult: AnyObject?
  private var _orientation: AIComponentKit.ScreenOrientation = .Unspecified
  private var _titleVisible = true
  private var _constraints = [NSLayoutConstraint]()
  private var _keyboardVisible = false
  private var _environment = YailDictionary()
  private var _initThunks = YailDictionary()
  private var _lastToastTime = 0.0
  private var _bigDefaultText = false
  private var _highContrast = false

  /**
   * Returns whether the current theme selected by the user is Dark or not.
   */
  public var isDarkTheme: Bool {
    return _theme == .Dark
  }

  public init(application: Application) {
    super.init(nibName: nil, bundle: nil)
    self.application = application
    defaultPropertyValues()
  }

  public override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    application = Application()
  }

  public required init?(coder: NSCoder) {
    super.init(coder: coder)
    application = Application()
  }

  open func copy(with zone: NSZone? = nil) -> Any {
    return self
  }
  
  @objc open var components: [Component] {
    get {
      return _components
    }
  }

  @objc open var environment: YailDictionary {
    return _environment
  }

  @objc open var initThunks: YailDictionary {
    return _initThunks
  }

  open override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    Form.activeForm = self
    NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(ScreenOrientationChanged), name: UIDevice.orientationDidChangeNotification, object: nil)
  }

  open override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
    NotificationCenter.default.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
    NotificationCenter.default.removeObserver(self, name: UIDevice.orientationDidChangeNotification, object: nil)
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
    if (canDispatch) {
      // Don't dispatch unless the current form is the dispatch delegate of the component
      return (component.dispatchDelegate as? Form) == self
    } else {
      NSLog("Attempted to dispatch event \(eventName) to \(component) but screen is not initialized");
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

  open var dispatchDelegate: HandlesEventDispatching? {
    get {
      return self
    }
  }

  open var form: Form? {
    get {
      return self
    }
  }

  public var container: ComponentContainer? {
    get {
      return self
    }
  }

  /**
   * Returns whether the Form is the REPL (companion) or not.
   */
  open var isRepl: Bool {
    return false
  }

  @objc(isInitialized) var initialized: Bool {
    return _screenInitialized
  }

  open override func viewDidLoad() {
    super.viewDidLoad()
    view.accessibilityIdentifier = String(describing: type(of: self))
  }

  open func add(_ component: NonvisibleComponent) {
    _components.append(component)
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
      _linearView.setWidth(of: component.view, to: Length(.Automatic))
    } else if width == kLengthFillParent {
      _linearView.setWidth(of: component.view, to: Length(.FillParent))
    } else {
      _linearView.setWidth(of: component.view, to: Length(pixels: width))
    }
    _linearView.setNeedsLayout()
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    if height <= kLengthPercentTag {
      _linearView.setHeight(of: component.view, to: Length(percent: height, of: _scaleFrameLayout))
    } else if height == kLengthPreferred {
      _linearView.setHeight(of: component.view, to: Length(.Automatic))
    } else if height == kLengthFillParent {
      _linearView.setHeight(of: component.view, to: Length(.FillParent))
    } else {
      _linearView.setHeight(of: component.view, to: Length(pixels: height))
    }
    _linearView.setNeedsLayout()
  }

  // MARK: Visual accessibility implementation

  public static func setBigDefaultTextRecursive(of container: ComponentContainer ,to enabled: Bool) {
    for child in container.getChildren() {
      if let child = child as? ComponentContainer {
        Form.setBigDefaultTextRecursive(of: child, to: enabled)
      } else if let child = child as? AccessibleComponent {
        child.LargeFont = enabled
      }
    }
  }

  public static func setHighContrastRecursive(of container: ComponentContainer, to enabled: Bool) {
    for child in container.getChildren() {
      if let child = child as? ComponentContainer {
        Form.setHighContrastRecursive(of: child, to: enabled)
      } else if let child = child as? AccessibleComponent {
        child.HighContrast = enabled
      }
    }
  }

  open func isVisible(component: ViewComponent) -> Bool {
    return _linearView.contains(component.view)
  }

  open func setVisible(component: ViewComponent, to visibility: Bool) {
    let visible = isVisible(component: component)
    if visible == visibility {
      return
    }
    if visibility {
      _linearView.setVisibility(of: component.view, to: true)
      // Replay width/height properties
      setChildHeight(of: component, to: component._lastSetHeight)
      setChildWidth(of: component, to: component._lastSetWidth)
    } else {
      _linearView.setVisibility(of: component.view, to: false)
    }
  }

  open var scaleFrameLayout: ScaleFrameLayout {
    return _scaleFrameLayout
  }

  @objc open func clear() {
    let subviews = view.subviews
    for subview in subviews {
      subview.removeFromSuperview()
    }
    _linearView.resetView()
    _linearView.removeAllItems()
    clearComponents()
    defaultPropertyValues()
    SCMInterpreter.shared.runGC()
  }

  private func recomputeLayout() {
    _linearView.removeFromSuperview()
    _scaleFrameLayout.removeFromSuperview()
    _linearView.accessibilityIdentifier = "Form root view"
    if _compatibilityMode {
      _scaleFrameLayout = ScaleFrameLayout(frame: CGRect(x: 0, y: 0, width: 320, height: view.frame.height / _scaleFrameLayout.scale))
    } else {
      _scaleFrameLayout = ScaleFrameLayout(frame: CGRect(origin: .zero, size: view.frame.size))
    }
    _scaleFrameLayout.mode = _compatibilityMode ? .Fixed : .Responsive
    _linearView.scrollEnabled = _scrollable
    _scaleFrameLayout.addSubview(_linearView)
    view.addSubview(_scaleFrameLayout)
    _linearView.horizontalAlignment = HorizontalGravity(rawValue: _horizontalAlignment)!
    _linearView.verticalAlignment = VerticalGravity(rawValue: _verticalAlignment)!
    resetConstraints()
  }

  private func resetConstraints() {
    _scaleFrameLayout.removeConstraints(_constraints)
    _constraints.removeAll()
    if ShowStatusBar && !TitleVisible {
      _constraints.append(_scaleFrameLayout.topAnchor.constraint(equalTo: view.topAnchor, constant: UIApplication.shared.statusBarFrame.height))
    } else {
      _constraints.append(_scaleFrameLayout.topAnchor.constraint(equalTo: view.topAnchor))
    }
    _constraints.append(_scaleFrameLayout.leadingAnchor.constraint(equalTo: view.leadingAnchor))
    _constraints.append(_scaleFrameLayout.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1.0/_scaleFrameLayout.scale))
    _constraints.append(_scaleFrameLayout.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 1.0/_scaleFrameLayout.scale))
    _constraints.append(_linearView.topAnchor.constraint(equalTo: _scaleFrameLayout.topAnchor))
    _constraints.append(_linearView.leadingAnchor.constraint(equalTo: _scaleFrameLayout.leadingAnchor))
    _constraints.append(_linearView.widthAnchor.constraint(equalTo: _scaleFrameLayout.widthAnchor))
    _constraints.append(_linearView.heightAnchor.constraint(equalTo: _scaleFrameLayout.heightAnchor))
    view.addConstraints(_constraints)
  }

  @objc func clearComponents() {
    onDelete()
    _components = []
  }

  @objc public func onResume() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onResume?()
      }
    }
    Notifier.notices.setPausedState(false)
  }

  @objc open func onPause() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onPause?()
      }
    }
    Notifier.notices.setPausedState(true)
  }

  @objc open func onDelete() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onDelete?()
      }
    }
    Notifier.notices.clearNotices()
  }

  @objc public func onDestroy() {
    for component in _components {
      if let delegate = component as? LifecycleDelegate {
        delegate.onDestroy?()
      }
    }
    Notifier.notices.clearNotices()
  }

  @objc open var toastAllowed: Bool {
    let now = CFAbsoluteTimeGetCurrent()
    if now > _lastToastTime + kMinimumToastWait {
      _lastToastTime = now
      return true
    }
    return false;
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
    BigDefaultText = false
    AlignHorizontal = HorizontalGravity.left.rawValue
    AlignVertical = VerticalGravity.top.rawValue
    HighContrast = false
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
      if path == _backgroundImage {
        // Already using this image
        return
      } else if path != "", let image = AssetManager.shared.imageFromPath(path: path) {
        _linearView.image = image
        _backgroundImage = path
      } else {
        _backgroundImage = ""
        _linearView.image = nil
        _linearView.backgroundColor = argbToColor(_backgroundColor)
      }
    }
  }

  @objc open var BigDefaultText: Bool {
    get {
      return _bigDefaultText
    }
    set(bigDefaultText) {
      _bigDefaultText = bigDefaultText
      Form.setBigDefaultTextRecursive(of: self ,to: _bigDefaultText)
      recomputeLayout()
    }
  }

  @objc open var BlocksToolkit: String {
    get {
      return ""
    }
    set {
    }
  }

  @objc open var CloseScreenAnimation: String {
    get {
      return "slide"
    }
    set(animation) {
      
    }
  }

  @objc open var DefaultFileScope: FileScope {
    get {
      return _defaultFileScope
    }
    set {
      _defaultFileScope = newValue
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

  @objc open var HighContrast: Bool {
    get {
      return _highContrast
    }
    set(highcontrast) {
      _highContrast = highcontrast
      Form.setHighContrastRecursive(of: self ,to: _highContrast)
      recomputeLayout()
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

  @objc open var Platform: String {
    return UIDevice.current.systemName
  }

  @objc open var PlatformVersion: String {
    return UIDevice.current.systemVersion
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
      return _orientation.toUnderlyingValue() as! String
    }
    set(orientation) {
      guard let orientation = AIComponentKit.ScreenOrientation.fromUnderlyingValue(orientation) else {
        return
      }
      _orientation = orientation
      if _orientation == .Portrait {
        if #available(iOS 16, *) {
          for scene in UIApplication.shared.connectedScenes {
            if let windowScene = scene as? UIWindowScene {
              windowScene.requestGeometryUpdate(UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: [.portrait, .portraitUpsideDown]))
            }
          }
        } else {
          UIDevice.current.setValue(UIDeviceOrientation.portrait.rawValue, forKey: "orientation")
        }
      } else if _orientation == .Landscape {
        if !_isPhone {
          let alert = UIAlertController(title: "Screen Orientation", message: "", preferredStyle: .alert)
          alert.message = "This app works best in landscape mode. Please rotate your device."
          alert.addAction(UIAlertAction(title: "OK", style: .default))
          present(alert, animated: true, completion: nil)
        }
        if #available(iOS 16, *) {
          for scene in UIApplication.shared.connectedScenes {
            if let windowScene = scene as? UIWindowScene {
              windowScene.requestGeometryUpdate(UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: .landscape))
            }
          }
        } else {
          UIDevice.current.setValue(UIDeviceOrientation.landscapeLeft.rawValue, forKey: "orientation")
        }
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
      return !UIApplication.shared.isStatusBarHidden
    }
    set(show) {
      _statusBarHidden = !show
      setNeedsStatusBarAppearanceUpdate()
      resetConstraints()
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
      return _titleVisible
    }
    set(show) {
      _titleVisible = show
      self.navigationController?.setNavigationBarHidden(!_titleVisible, animated: true)
      resetConstraints()
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
      return (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? ""
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

  @objc open func AskForPermission(_ permissionName: String) {
    var permission = permissionName
    if permissionName.contains(".") {
      let parts = permissionName.split(separator: ".")
      permission = String(parts[parts.count-1])
    }
    switch permission {
    case "RECORD_AUDIO", "MODIFY_AUDIO_SETTINGS":
      askForPermission(.microphone, codeNamed: permissionName)
      break
    case "ACCESS_COARSE_LOCATION", "ACCESS_FINE_LOCATION":
      askForPermission(.location, codeNamed: permissionName)
      break
    case "CAMERA":
      askForPermission(.camera, codeNamed: permissionName)
      break
    case "SPEECH_RECOGNIZER":
      askForPermission(.speech, codeNamed: permissionName)
      break
    default:
      PermissionGranted(permissionName)
    }
  }

  private func askForPermission(_ permission: Permission, codeNamed permissionName: String) {
    if let result = PermissionHandler.HasPermission(for: permission), result {
      PermissionGranted(permissionName)
    } else {
      PermissionHandler.RequestPermission(for: permission) { allowed, changed in
        if allowed {
          self.PermissionGranted(permissionName)
        } else {
          self.PermissionDenied(self, "AskForPermission", permissionName)
        }
      }
    }
  }

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

  open func dispatchErrorOccurredEvent(_ component: Component, _ functionName: String, _ error: ErrorMessage, _ messageArgs: Any...) {
    runOnUiThread {
      let formattedMessage = String(format: error.message, messageArgs: messageArgs)
      self.ErrorOccurred(component, functionName, error.code, formattedMessage)
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

  open func dispatchPermissionDeniedEvent(_ component: Component, _ functionName: String, _ exception: PermissionException) {
    runOnUiThread {
      self.PermissionDenied(component, functionName, exception.permissionNeeded)
    }
  }

  open func dispatchPermissionDeniedEvent(_ component: Component, _ functionName: String, _ permissionName: String) {
    runOnUiThread {
      self.PermissionDenied(component, functionName, permissionName)
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

  @objc open func PermissionDenied(_ component: Component, _ functionName: String, _ permissionName: String) {
    if (!EventDispatcher.dispatchEvent(of: self, called: "PermissionDenied", arguments: component as AnyObject, functionName as AnyObject, permissionName as AnyObject)) {
      dispatchErrorOccurredEvent(component, functionName, ErrorMessage.ERROR_PERMISSION_DENIED.code, permissionName)
    }
  }

  @objc open func PermissionGranted(_ permissionName: String) {
    EventDispatcher.dispatchEvent(of: self, called: "PermissionGranted", arguments: permissionName as AnyObject)
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
    EventDispatcher.removeDispatchDelegate(self)
  }

  @objc func doCloseScreen(withPlainText text: String) {
    if let nc = self.navigationController, nc.viewControllers.count > 1 {
      navigationController?.popViewController(animated: true)
    }
    if let vcs = navigationController?.viewControllers, let parentForm = vcs.last as? Form {
      parentForm.lastFormName = self.formName
      parentForm.formResult = text as AnyObject
    }
    EventDispatcher.removeDispatchDelegate(self)
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
    case .Dark:
      return .lightContent
    default:
      return .default
    }
  }

  @objc open func updateNavbar() {
    if let parent = navigationController {
      let navbar = parent.navigationBar
      navbar.tintColor = UIColor.white
      navbar.backgroundColor = argbToColor(_primaryColor)
      if #available(iOS 13.0, *) {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = argbToColor(_primaryColor)
        navbar.standardAppearance = appearance
        navbar.scrollEdgeAppearance = navbar.standardAppearance
      }
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

  // MARK: Keyboard handling
  @objc public func keyboardWillShow(_ notification: NSNotification) {
    guard !_keyboardVisible else {
      return
    }
    
    _keyboardVisible = true
    if let userInfo = notification.userInfo,
        let frame = (userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
      let height = frame.height
      view.frame = CGRect(x: view.frame.origin.x, y: view.frame.origin.y,
                          width: view.frame.size.width, height: view.frame.size.height - height)
    }
  }

  @objc public func keyboardWillHide(_ notification: NSNotification) {
    _keyboardVisible = false
    if let userInfo = notification.userInfo,
        let frame = (userInfo[UIResponder.keyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
      let height = frame.height
      view.frame = CGRect(x: view.frame.origin.x, y: view.frame.origin.y,
                          width: view.frame.size.width, height: view.frame.size.height + height)
    }
  }

  open func getChildren() -> [Component] {
    return _components
  }

  // MARK: Memory management

  private var marked = false

  @objc open func mark() {
    guard !marked else {
      return
    }
    defer {
      marked = false
    }
    marked = true
  #if MEMDEBUG
    NSLog("Form.mark")
  #endif
    environment.mark()
    initThunks.mark()
  }

#if MEMDEBUG
  deinit {
    NSLog("Deallocating \(self)")
  }
#endif
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToOptionalNSAttributedStringKeyDictionary(_ input: [String: Any]?) -> [NSAttributedString.Key: Any]? {
	guard let input = input else { return nil }
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (NSAttributedString.Key(rawValue: key), value)})
}
