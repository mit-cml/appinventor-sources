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

class ViewController: UINavigationController {
  public var Height: Int32 = 0
  public var Width: Int32 = 0

  public func setChildHeight(of component: ViewComponent, height: Int32) {
    
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    
  }

  @IBOutlet public var form: Form?

  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var serverStatus: UILabel?
  @IBOutlet weak var button: UIButton?
  var httpd: AppInvHTTPD?
  var temp: TestObj?

  override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    //barcodeScanner = BarcodeScanner(parent: self)
    //pushViewController(form, animated: false);
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    if (form == nil) {
      form = self.viewControllers[self.viewControllers.count - 1] as! ReplForm;
      ipAddrLabel = form?.view.viewWithTag(1) as! UILabel?
      serverStatus = form?.view.viewWithTag(2) as! UILabel?
      let ipaddr: String! = NetworkUtils.getIPAddress()
      ipAddrLabel?.text = "IP Address: " + ipaddr
      serverStatus?.text = "Server status: running"
      httpd = AppInvHTTPD(port:8001, rootDirectory:"", secure:false, for:form as! ReplForm)
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

  override func viewDidAppear(_ animated: Bool) {
//    self.playSound(self)
  }

  override func didReceiveMemoryWarning() {
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
}

