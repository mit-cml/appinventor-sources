// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

import UIKit
import AIComponentKit
import AVKit

/**
 * Root view controller for the MIT AI Companion for iOS. Eventually this will go away
 * once we have the capability to build apps from YAIL files, in which case we will be
 * able to build the app from the aiplayapp sources.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ViewController: UINavigationController {
  @objc public var Height: Int32 = 0
  @objc public var Width: Int32 = 0
  private static var controller: ViewController?

  @objc public func setChildHeight(of component: ViewComponent, height: Int32) {
    
  }

  @objc public func setChildWidth(of component: ViewComponent, width: Int32) {
    
  }

  @IBOutlet public var form: Form!

  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var versionNumber: UILabel?
  @IBOutlet weak var connectCode: UITextField?
  @IBOutlet weak var connectButton: UIButton?
  @IBOutlet weak var barcodeButton: UIButton?
  @objc var barcodeScanner: BarcodeScanner?
  @objc var phoneStatus: PhoneStatus!

  public override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    //barcodeScanner = BarcodeScanner(parent: self)
    //pushViewController(form, animated: false);
    ViewController.controller = self
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    if (form == nil) {
      form = self.viewControllers[self.viewControllers.count - 1] as! ReplForm
      form.Initialize()
      form.AccentColor = Int32(bitPattern: 0xFF128BA8)
      form.PrimaryColor = Int32(bitPattern: 0xFFA5CF47)
      form.PrimaryColorDark = Int32(bitPattern: 0xFF516623)
      form.title = "MIT App Inventor 2"
      let repl = form as! ReplForm
      repl.startHTTPD(false)
      repl.interpreter?.evalForm("(add-component Screen1 AIComponentKit.BarcodeScanner BarcodeScanner1)")
      phoneStatus = PhoneStatus(repl)
      if let exception = repl.interpreter?.exception {
        NSLog("Exception: \(exception.name) (\(exception))")
      }
      repl.interpreter?.evalForm("(define-event BarcodeScanner1 AfterScan(result) (yail:invoke AICompanionApp.ViewController 'gotText result))")
      if let exception = repl.interpreter?.exception {
        NSLog("Exception: \(exception.name) (\(exception))")
      }
      if let mooning = UIImage(named: "mooning.png") {
        form.view.backgroundColor = UIColor(patternImage: mooning)
      }
      ipAddrLabel = form.view.viewWithTag(1) as! UILabel?
      versionNumber = form.view.viewWithTag(2) as! UILabel?
      connectCode = form.view.viewWithTag(3) as! UITextField?
      connectButton = form.view.viewWithTag(4) as! UIButton?
      barcodeButton = form.view.viewWithTag(5) as! UIButton?
      let ipaddr: String! = NetworkUtils.getIPAddress()
      ipAddrLabel?.text = "IP Address: \(ipaddr!)"
      versionNumber?.text = "Version: \((Bundle.main.infoDictionary?["CFBundleShortVersionString"])!)"
      connectButton?.addTarget(self, action: #selector(connect(_:)), for: UIControl.Event.primaryActionTriggered)
      barcodeButton?.addTarget(self, action: #selector(showBarcodeScanner(_:)), for: UIControl.Event.primaryActionTriggered)
      navigationBar.barTintColor = argbToColor(form.PrimaryColor)
      navigationBar.isTranslucent = false
      form.updateNavbar()
    }
  }

  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
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
    let code = phoneStatus.setHmacSeedReturnCode((connectCode?.text)!)
    NSLog("Seed = \((connectCode?.text)!)")
    NSLog("Code = \(code)")
    let url = URL(string: "http://rendezvous.appinventor.mit.edu/rendezvous/");
    var request = URLRequest(url: url!)
    let values = "key=\(code)&ipaddr=\((NetworkUtils.getIPAddress())!)&port=9987&webrtc=false&version=2.51&api=12.0&os=ios&installer=unknown"
    NSLog("Values = \(values)")
    request.httpMethod = "POST"
    request.httpBody = values.data(using: String.Encoding.utf8)
    URLSession.shared.dataTask(with: request).resume()
  }
  
  @objc func showBarcodeScanner(_ sender: UIButton?) {
    let repl = (form! as! ReplForm)
    repl.interpreter?.evalForm("(yail:invoke (lookup-in-form-environment 'BarcodeScanner1) 'DoScan)")
    if let exception = repl.interpreter?.exception {
      NSLog("Exception: \(exception.name) (\(exception))")
    }
  }
  
  @objc public class func gotText(_ text: String) {
    ViewController.controller?.connectCode?.text = text
    if (text != "") {
      ViewController.controller?.connect(nil)
    }
  }

  override public var childForStatusBarStyle: UIViewController? {
    return form
  }
}
