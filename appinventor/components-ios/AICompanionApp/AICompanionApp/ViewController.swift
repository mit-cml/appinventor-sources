//
//  ViewController.swift
//  AICompanionApp
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import UIKit
import AIComponentKit

class ViewController: UINavigationController, ComponentContainer {
  public var Height: Int32 = 0
  public var Width: Int32 = 0

  public func setChildHeight(of component: ViewComponent, height: Int32) {
    
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    
  }

  public var form: Form

  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var serverStatus: UILabel?
  @IBOutlet weak var button: UIButton?
  var httpd: AppInvHTTPD?
  var temp: TestObj?
  var barcodeScanner: BarcodeScanner!

  required init?(coder: NSCoder) {
    form = Form(coder: coder)!
    super.init(coder: coder)
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    let ipaddr: String! = NetworkUtils.getIPAddress()
    ipAddrLabel?.text = "IP Address: " + ipaddr
    httpd = AppInvHTTPD(port:8001, rootDirectory:"", secure:false, for:nil)
    serverStatus?.text = "Server status: running"
    barcodeScanner = BarcodeScanner(parent: self)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

  @IBAction func showBarcodeScanner(sender: UIButton) {
    barcodeScanner.DoScan()
  }

  func canDispatchEvent(of component: Component, called eventName: String) -> Bool {
    return true
  }

  func dispatchEvent(of component: Component, called componentName: String, with eventName: String, having args: [AnyObject]) -> Bool {
    return true
  }

  func add(component: ViewComponent) {
    
  }
}

