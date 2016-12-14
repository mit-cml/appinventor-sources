//
//  ViewController.swift
//  AICompanionApp
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import UIKit
import AIComponentKit
import AVKit

public class ViewController: UINavigationController {
  public var Height: Int32 = 0
  public var Width: Int32 = 0
  private static var controller: ViewController?

  public func setChildHeight(of component: ViewComponent, height: Int32) {
    
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    
  }

  @IBOutlet public var form: Form?

  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var versionNumber: UILabel?
  @IBOutlet weak var connectCode: UITextField?
  @IBOutlet weak var connectButton: UIButton?
  @IBOutlet weak var barcodeButton: UIButton?
  var barcodeScanner: BarcodeScanner?
//  var httpd: AppInvHTTPD?

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
      form = self.viewControllers[self.viewControllers.count - 1] as! ReplForm;
      form?.Initialize()
      let repl = form! as! ReplForm
      repl.startHTTPD(false)
      repl.interpreter?.evalForm("(add-component Screen1 AIComponentKit.BarcodeScanner BarcodeScanner1)")
      if ((repl.interpreter?.exception) != nil) {
        NSLog("Exception: \((repl.interpreter?.exception?.name)!) (\(repl.interpreter?.exception)!)")
      }
      repl.interpreter?.evalForm("(define-event BarcodeScanner1 AfterScan(result) (yail:invoke AICompanionApp.ViewController 'gotText result))")
      if ((repl.interpreter?.exception) != nil) {
        NSLog("Exception: \((repl.interpreter?.exception?.name)!) (\(repl.interpreter?.exception)!)")
      }
      if let mooning = UIImage(named: "mooning.png") {
        form?.view.backgroundColor = UIColor(patternImage: mooning)
      }
      ipAddrLabel = form?.view.viewWithTag(1) as! UILabel?
      versionNumber = form?.view.viewWithTag(2) as! UILabel?
      connectCode = form?.view.viewWithTag(3) as! UITextField?
      connectButton = form?.view.viewWithTag(4) as! UIButton?
      barcodeButton = form?.view.viewWithTag(5) as! UIButton?
      let ipaddr: String! = NetworkUtils.getIPAddress()
      ipAddrLabel?.text = "IP Address: \(ipaddr!)"
      versionNumber?.text = "Version: \((Bundle.main.infoDictionary?["CFBundleShortVersionString"])!)"
      connectButton?.addTarget(self, action: #selector(connect(_:)), for: UIControlEvents.primaryActionTriggered)
      barcodeButton?.addTarget(self, action: #selector(showBarcodeScanner(_:)), for: UIControlEvents.primaryActionTriggered)
//      httpd = AppInvHTTPD(port:8001, rootDirectory:"", secure:false, for:form as! ReplForm)
//      let temp = UIButton(type: UIButtonType.system)
//      let image = UIImage(named: "kitty.png")
//      temp.setBackgroundImage(image, for: UIControlState.normal)
//      temp.frame = CGRect(x: 0, y: 0, width: 294, height: 270)
//      temp.addTarget(self, action: #selector(playSound(_:)), for: UIControlEvents.primaryActionTriggered)
//      temp.titleLabel?.font = UIFont(name: "System", size: 15.0)
//      temp.titleLabel?.textColor = UIColor(
//      temp.setTitle("Hello world", for: UIControlState.normal)
//      form?.view.addSubview(temp)
    }
  }

  var audioPlayer: AVAudioPlayer? = nil

  func playSound(_ sender: AnyObject?) {
    if audioPlayer == nil {
      let meow = URL(fileURLWithPath: Bundle.main.path(forResource: "meow.mp3", ofType: nil)!)
      NSLog("Meow: \(meow)")
      do {
        try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback)
        try AVAudioSession.sharedInstance().setActive(true)
        audioPlayer = try AVAudioPlayer(contentsOf: meow)
        audioPlayer?.prepareToPlay()
      } catch {
        NSLog("Error")
        return
      }
    }
    audioPlayer?.play()
  }

  public override func viewDidAppear(_ animated: Bool) {
//    self.playSound(self)
  }

  public override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

  func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    return true
  }

  func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    return true
  }

  func add(_ component: ViewComponent) {
    
  }
  
  func connect(_ sender: UIButton?) {
    let code = (connectCode?.text?.sha1)!
    NSLog("Seed = \((connectCode?.text)!)")
    NSLog("Code = \(code)")
    let url = URL(string: "http://rendezvous.appinventor.mit.edu/rendezvous/");
    var request = URLRequest(url: url!)
    let values = "key=\(code)&ipaddr=\((NetworkUtils.getIPAddress())!)&port=9987"
    NSLog("Values = \(values)")
    request.httpMethod = "POST"
    request.httpBody = values.data(using: String.Encoding.utf8)
    URLSession.shared.dataTask(with: request).resume()
  }
  
  func showBarcodeScanner(_ sender: UIButton?) {
    let repl = (form! as! ReplForm)
    repl.interpreter?.evalForm("(yail:invoke (lookup-in-form-environment 'BarcodeScanner1) 'DoScan)")
    if ((repl.interpreter?.exception) != nil) {
      NSLog("Exception: \((repl.interpreter?.exception?.name)!) (\(repl.interpreter?.exception)!)")
    }
  }
  
  public class func gotText(_ text: String) {
    ViewController.controller?.connectCode?.text = text
    if (text != "") {
      ViewController.controller?.connect(nil)
    }
  }
}

