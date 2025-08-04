// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import AIComponentKit
import AVKit

/**
 * Menu for the iPad REPL.
 */
class MenuViewController: UITableViewController {

  weak var delegate: ViewController?

  public override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return 1
  }

  public override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = UITableViewCell()
    cell.textLabel?.text = "Close Project"
    cell.textLabel?.textColor = UIColor.red
    return cell
  }

  public override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    delegate?.reset()
  }
}

/**
 * Root view controller for the MIT AI Companion for iOS. Eventually this will go away
 * once we have the capability to build apps from YAIL files, in which case we will be
 * able to build the app from the aiplayapp sources.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ViewController: UINavigationController, UITextFieldDelegate {
  @objc public var Height: Int32 = 0
  @objc public var Width: Int32 = 0
  private static var controller: ViewController?
  private var connectProgressDialog: UIAlertController?
  private var connectProgressView: UIProgressView?

  @objc public func setChildHeight(of component: ViewComponent, height: Int32) {
    
  }

  @objc public func setChildWidth(of component: ViewComponent, width: Int32) {
    
  }

  @IBOutlet public var form: ReplForm!

  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var versionNumber: UILabel?
  @IBOutlet weak var connectCode: UITextField?
  @IBOutlet weak var connectButton: UIButton?
  @IBOutlet weak var barcodeButton: UIButton?
  @IBOutlet weak var legacyCheckbox: CheckBoxView!
  @objc var barcodeScanner: BarcodeScanner?
  @objc var phoneStatus: PhoneStatus!
  @objc var notifier1: Notifier!
  private var onboardingScreen: OnboardViewController? = nil
  private var didWifiCheck = false

  private static var _interpreterInitialized = false

  public override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    SCMInterpreter.shared.protect(self)
    ViewController.controller = self
    NotificationCenter.default.addObserver(self, selector: #selector(settingsChanged(_:)), name: UserDefaults.didChangeNotification, object: nil)
    self.delegate = self
  }

  @objc func settingsChanged(_ sender: AnyObject?) {
    maybeShowOnboardingScreen()
  }

  public override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    maybeShowOnboardingScreen()
  }

  // We override this function to handle the Form's ScreenOrientation setting.
  open override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    guard form != nil else {
      return .all
    }
    switch form.ScreenOrientation {
    case "portrait":
      return .portrait
    case "landscape":
      return .landscape
    default:
      return .all
    }
  }

  open override var shouldAutorotate: Bool {
    return true
  }

  private func initializeInterpreter() -> SCMInterpreter {
    guard !ViewController._interpreterInitialized else {
      return SCMInterpreter.shared
    }
    let interpreter = SCMInterpreter.shared
    do {
      if let url = Bundle(for: ReplForm.self).path(forResource: "runtime", ofType: "scm") {
        let runtime = try! String(contentsOfFile: url)
        interpreter.evalForm(runtime)
        if interpreter.exception != nil {
          let title = "Error"
          let message = "Unable to initialize runtime: \(interpreter.exception!)"
          let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
          let okAction = UIAlertAction(title: "OK", style: .default, handler: nil)
          alertController.addAction(okAction)
          if let topController = UIApplication.shared.keyWindow?.rootViewController {
            topController.present(alertController, animated: true, completion: nil)
          }
          return interpreter
        }
        ViewController._interpreterInitialized = true
      }
    }
    return interpreter
  }

  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    if let menuButton = viewControllers.last?.navigationItem.rightBarButtonItem {
      menuButton.action = #selector(openMenu(caller:))
      menuButton.target = self
    }
    if (form == nil) {
      let interpreter = initializeInterpreter()
      form = self.viewControllers[self.viewControllers.count - 1] as? ReplForm
      form.makeTopForm()
      interpreter.setCurrentForm(form!)
      form.AccentColor = Int32(bitPattern: 0xFF128BA8)
      if let mooning = UIImage(named: "Mooning") {
        form.view.backgroundColor = UIColor(patternImage: mooning)
      }
      form.PrimaryColor = Int32(bitPattern: 0xFFA5CF47)
      form.PrimaryColorDark = Int32(bitPattern: 0xFF516623)
      form.title = "MIT App Inventor 2"
      interpreter.evalForm("(add-component Screen1 AIComponentKit.BarcodeScanner BarcodeScanner1)")
      interpreter.evalForm("(define-event BarcodeScanner1 AfterScan(result) (yail:invoke AICompanionApp.ViewController 'gotText result))")
      if let exception = interpreter.exception {
        NSLog("Exception: \(exception.name) (\(exception))")
      }
      interpreter.evalForm("(add-component Screen1 AIComponentKit.PhoneStatus PhoneStatus1)")
      interpreter.evalForm("(add-component Screen1 AIComponentKit.Notifier Notifier1)")
      interpreter.evalForm("""
        (define-event Notifier1 AfterChoosing($choice)(set-this-form)
          (if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value $choice) "Exit") '(any any) "=") (begin   (call-component-method 'PhoneStatus1 'shutdown (*list-for-runtime*) '()))))
        """)
      phoneStatus = form.environment["PhoneStatus1"] as? PhoneStatus
      notifier1 = form.environment["Notifier1"] as? Notifier
      ipAddrLabel = form.view.viewWithTag(1) as! UILabel?
      versionNumber = form.view.viewWithTag(2) as! UILabel?
      connectCode = form.view.viewWithTag(3) as! UITextField?
      connectButton = form.view.viewWithTag(4) as! UIButton?
      barcodeButton = form.view.viewWithTag(5) as! UIButton?
      legacyCheckbox = form.view.viewWithTag(6) as? CheckBoxView
      legacyCheckbox.Text = "Use Legacy Connection"
      let ipaddr: String! = NetworkUtils.getIPAddress()
      let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? "unknown"
      let build = Bundle.main.infoDictionary?["CFBundleVersion"] ?? "?"
      ipAddrLabel?.text = "IP Address: \(ipaddr!)"
      versionNumber?.text = "Version: \(version) (build \(build))"
      connectCode?.delegate = self
      connectButton?.addTarget(self, action: #selector(connect(_:)), for: UIControl.Event.primaryActionTriggered)
      barcodeButton?.addTarget(self, action: #selector(showBarcodeScanner(_:)), for: UIControl.Event.primaryActionTriggered)
      navigationBar.barTintColor = argbToColor(form.PrimaryColor)
      navigationBar.isTranslucent = false
      form.updateNavbar()
      form.Initialize()
    }
  }

  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
    SCMInterpreter.shared.runGC()
  }

  @objc func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    return true
  }

  @objc func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    return true
  }

  @objc func add(_ component: ViewComponent) {
    
  }
  
  @objc func connect(_ sender: UIButton?) {
    guard let text = connectCode?.text else {
      return
    }
    if text.hasPrefix("https:") {
      ViewController.gotText(text)
      return
    }
    guard text.count == 6 else {
      notifier1.ShowAlert("Invalid code: Code must be 6 characters")
      return
    }
    phoneStatus.WebRTC = !(legacyCheckbox?.Checked ?? true)
    RetValManager.shared().usingWebRTC = phoneStatus.WebRTC
    form.startHTTPD(false)
    form.application?.makeCurrent()
    let alertView = UIAlertController(title: "Connecting", message: nil, preferredStyle: .alert)
    alertView.addAction(UIAlertAction(title: "Cancel", style: .cancel))
    present(alertView, animated: true) {
      let rect = CGRect(x: 8.0, y: 72.0, width: alertView.view.frame.width - 16.0, height: 2.0)
      let progressView = UIProgressView(frame: rect)
      self.connectProgressView = progressView
      progressView.progress = 0.05
      progressView.tintColor = self.view.tintColor
      alertView.view.addSubview(progressView)
      alertView.message = "Initial Rendezvous"
    }
    self.connectProgressDialog = alertView
    let code = phoneStatus.setHmacSeedReturnCode(text)
    NSLog("Seed = \(text)")
    NSLog("Code = \(code)")
    let url = URL(string: "https://\(kDefaultRendezvousServer)/rendezvous/");
    var request = URLRequest(url: url!)
    let values = [
      "key": code,
      "ipaddr": NetworkUtils.getIPAddress(),
      "port": "9987",
      "webrtc": phoneStatus.WebRTC ? "true" : "false",
      "version": phoneStatus.GetVersionName(),
      "api": phoneStatus.SdkLevel(),
      "installer": phoneStatus.GetInstaller(),
      "os": form.Platform,
      "aid": phoneStatus.InstallationId(),
      "r2": "true",
      "extensions": """
      [
      \"edu.mit.appinventor.ble\",
      \"com.bbc.microbit.profile\",
      \"edu.mit.appinventor.ai.personalimageclassifier\",
      \"edu.mit.appinventor.ai.personalaudioclassifier\",
      \"edu.mit.appinventor.ai.posenet\",
      \"edu.mit.appinventor.ai.facemesh\",
      \"edu.mit.appinventor.ai.teachablemachine\",
      \"fun.microblocks.microblocks\"
      ]
      """,
      "useproxy": phoneStatus.UseProxy ? "true" : "false"
    ].map({ (key: String, value: String) -> String in
      return "\(key)=\(value)"
    }).joined(separator: "&")
    NSLog("Values = \(values)")
    request.httpMethod = "POST"
    request.httpBody = values.data(using: String.Encoding.utf8)
    URLSession.shared.dataTask(with: request, completionHandler: { (data, response, error) in
      if self.phoneStatus.WebRTC {
        guard let data = data, let responseContent = String(data: data, encoding: .utf8) else {
          return
        }
        DispatchQueue.main.async {
          self.connectProgressDialog?.message = "Waiting for remote..."
          self.connectProgressView?.progress = 0.15
          self.phoneStatus.startWebRTC(kDefaultRendezvousServer, responseContent, self)
        }
      } else {
        self.connectProgressDialog?.dismiss(animated: true)
        var responseContent = ""
        if let data = data {
          guard let responseContentStr = String(data: data, encoding: .utf8) else {
            return
          }
          responseContent = responseContentStr
        }
        self.setPopup(popup: responseContent)
      }
    }
    ).resume()
  }
  
  @objc func showBarcodeScanner(_ sender: UIButton?) {
    form.interpreter.evalForm("(call-component-method 'BarcodeScanner1 'DoScan (*list-for-runtime*) '())")
    if let exception = form.interpreter.exception {
      NSLog("Exception: \(exception.name) (\(exception))")
    }
  }
  
  @objc public class func gotText(_ text: String) {
    ViewController.controller?.connectCode?.text = text
    if !text.isEmpty {
      ViewController.controller?.connect(nil)
    }
  }
  
  override public var childForStatusBarStyle: UIViewController? {
    return form
  }

  @objc func openMenu(caller: UIBarButtonItem) {
    if UIDevice.current.userInterfaceIdiom == .phone {
      let controller = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
      controller.addAction(UIAlertAction(title: "Close Project", style: .destructive) { [weak self] (UIAlertAction) in
        self?.reset()
        controller.dismiss(animated: false)
      })
      controller.addAction(UIAlertAction(title: "Cancel", style: .cancel) { (UIAlertAction) in
        controller.dismiss(animated: true)
      })
      present(controller, animated: true)
    } else {
      let menu = MenuViewController()
      menu.modalPresentationStyle = .popover
      menu.delegate = self
      menu.preferredContentSize = UITableViewCell().frame.size
      menu.popoverPresentationController?.barButtonItem = caller
      self.present(menu, animated: true)
    }
  }

  open func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    if textField.text?.count == 6 {
      DispatchQueue.main.async {
        self.connect(nil)
      }
      textField.resignFirstResponder()
      return false
    }
    return true
  }

  @objc func mark() {
    for vc in self.viewControllers {
      if let form = vc as? Form {
        form.mark()
      }
    }
  }

  @objc public func reset() {
    form.stopHTTPD()
    form.clear()
    form.environment.removeAllObjects()
    form.initThunks.removeAllObjects()
    form.stopWebRTC()
    EventDispatcher.removeDispatchDelegate(form)
    form = nil
    self.viewControllers = []
    let storyboard = UIStoryboard(name: "Main", bundle: nil)
    if let newRoot = storyboard.instantiateInitialViewController() {
      UIApplication.shared.delegate?.window??.rootViewController = newRoot
    }
  }

  private func setPopup(popup: String) {
    phoneStatus.setPopup(popup)
  }

  /**
   * Show the onboarding screen when certain conditions are met.
   *
   * The following two conditions must be true.
   *
   * 1. The new user flag is present
   * 2. The onboarding screen isn't already shown
   *
   * If the onboarding screen is already visible but the flag is false, this will hide the
   * onboarding screen as this indicates the user toggled the Show Welcome Screen switch
   * in the Settings app.
   */
  private func maybeShowOnboardingScreen() {
    guard SystemVariables.newUser && onboardingScreen == nil else {
      if !SystemVariables.newUser {
        // Hide the welcome screen if visible
        onboardingScreen?.dismiss(animated: true, completion: {
          self.onboardingScreen = nil
        })
      }
      if !self.didWifiCheck {
        DispatchQueue.main.async {
          self.checkWifi()
        }
      }
      return
    }

    // Show onboarding
    let vc = storyboard?.instantiateViewController(withIdentifier: "onboard") as! OnboardViewController
    vc.modalPresentationStyle = .fullScreen
    present(vc, animated: true)
    onboardingScreen = vc
    vc.onCompletionHandler = {
      if !self.didWifiCheck {
        self.checkWifi()
      }
    }
  }

  // Implemented in Swift based on aiplayapp/src/edu/mit/appinventor/aicompanion3/Screen1.yail
  private func checkWifi() {
    guard !didWifiCheck else {
      return
    }
    didWifiCheck = true
    if PhoneStatus.GetWifiIpAddress().hasPrefix("Error") {
      notifier1.ShowChooseDialog("Your Device does not appear to have a Wifi Connection",
                                 "No WiFi", "Continue without WiFi", "Exit", false)
    }
  }
}

extension ViewController: UINavigationControllerDelegate {
  public func navigationController(_ navigationController: UINavigationController,
      animationControllerFor operation: UINavigationController.Operation,
      from fromVC: UIViewController,
      to toVC: UIViewController) -> UIViewControllerAnimatedTransitioning? {
    guard let oldForm = fromVC as? Form, let newForm = toVC as? Form else {
      return nil
    }
    oldForm.onPause()
    if operation == .pop {
      oldForm.onDestroy()
    }
    newForm.onResume()
    return nil
  }
}

extension ViewController: WebRTCConnectionDelegate {
  public func webRTCDidGetLocalOffer() {
    connectProgressView?.progress = 0.4
    connectProgressDialog?.message = "Generating routes..."
  }

  public func webRTCDidGetRemoteOffer() {
    connectProgressView?.progress = 0.3
    connectProgressDialog?.message = "Sending local answer..."
  }

  public func webRTCDidGenerateICECandidate() {
    guard let progressView = connectProgressView else {
      return
    }
    progressView.progress = min(0.01 + progressView.progress, 0.9)
  }

  public func webRTCDataChannelOpened() {
    connectProgressDialog?.dismiss(animated: true)
    connectProgressView = nil
    connectProgressDialog = nil
  }
}
