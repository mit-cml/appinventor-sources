//
//  ViewController.swift
//  AICompanionApp
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import UIKit
import AIComponentKit

class ViewController: UINavigationController {
  @IBOutlet weak var ipAddrLabel: UILabel?
  @IBOutlet weak var serverStatus: UILabel?
  var httpd: AppInvHTTPD?
  var temp: TestObj?

  override func viewDidLoad() {
    super.viewDidLoad()
    // Do any additional setup after loading the view, typically from a nib.
    let ipaddr: String! = NetworkUtils.getIPAddress()
    ipAddrLabel?.text = "IP Address: " + ipaddr
    httpd = AppInvHTTPD(port:8001, rootDirectory:"", secure:false, for:nil)
    serverStatus?.text = "Server status: running"
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }


}

