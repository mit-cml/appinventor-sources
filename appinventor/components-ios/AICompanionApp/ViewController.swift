// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2021 Massachusetts Institute of Technology, All rights reserved.

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
  @objc var barcodeScanner: BarcodeScanner?
  @objc var phoneStatus: PhoneStatus!
  private var onboardingScreen: OnboardViewController? = nil

  private static var _interpreterInitialized = false

  public override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    //barcodeScanner = BarcodeScanner(parent: self)
    //pushViewController(form, animated: false);
    ViewController.controller = self
    NotificationCenter.default.addObserver(self, selector: #selector(settingsChanged(_:)), name: UserDefaults.didChangeNotification, object: nil)
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
          fatalError("Unable to initialize runtime: \(interpreter.exception!)")
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
      phoneStatus = form.environment["PhoneStatus1"] as? PhoneStatus
      ipAddrLabel = form.view.viewWithTag(1) as! UILabel?
      versionNumber = form.view.viewWithTag(2) as! UILabel?
      connectCode = form.view.viewWithTag(3) as! UITextField?
      connectButton = form.view.viewWithTag(4) as! UIButton?
      barcodeButton = form.view.viewWithTag(5) as! UIButton?
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
    form.startHTTPD(false)
    form.application?.makeCurrent()
    let code = phoneStatus.setHmacSeedReturnCode((connectCode?.text)!)
    NSLog("Seed = \((connectCode?.text)!)")
    NSLog("Code = \(code)")
    let url = URL(string: "http://rendezvous.appinventor.mit.edu/rendezvous/");
    var request = URLRequest(url: url!)
    let values = [
      "key": code,
      "ipaddr": NetworkUtils.getIPAddress()!,
      "port": "9987",
      "webrtc": String(describing: phoneStatus.WebRTC),
      "version": phoneStatus.GetVersionName(),
      "api": phoneStatus.SdkLevel(),
      "installer": phoneStatus.GetInstaller(),
      "os": form.Platform,
      "aid": phoneStatus.InstallationId(),
      "r2": "true",
      "useproxy": String(describing: phoneStatus.UseProxy)
    ].map({ (key: String, value: String) -> String in
      return "\(key)=\(value)"
    }).joined(separator: "&")
    NSLog("Values = \(values)")
    request.httpMethod = "POST"
    request.httpBody = values.data(using: String.Encoding.utf8)
    URLSession.shared.dataTask(with: request, completionHandler: { (data, response, error) in
      var responseContent: NSString = ""
      if let data = data {
        guard let responseContentStr = String(data: data, encoding: .utf8) else {
          return
        }
        responseContent = responseContentStr as NSString
      }
      self.setPopup(popup: responseContent as String)
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

  fileprivate func reset() {
    form.stopHTTPD()
    form.clear()
    form.environment.removeAllObjects()
    form.initThunks.removeAllObjects()
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
      return
    }

    // Show onboarding
    let vc = storyboard?.instantiateViewController(withIdentifier: "onboard") as! OnboardViewController
    vc.modalPresentationStyle = .fullScreen
    present(vc, animated: true)
    onboardingScreen = vc
  }
}
