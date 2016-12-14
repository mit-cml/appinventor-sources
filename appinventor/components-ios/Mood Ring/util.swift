//
//  util.swift
//  Mood Ring
//
//  Created by Evan Patton on 12/13/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation
import UIKit

func openAnotherScreen(named screenName: String) {
  // TODO(ewpatton): Address kludge
  if screenName == "Screen3" {
    UIApplication.shared.keyWindow?.rootViewController = Screen3()
  } else if screenName == "Screen4" {
    UIApplication.shared.keyWindow?.rootViewController = Screen4()
  } else if screenName == "Screen5" {
    UIApplication.shared.keyWindow?.rootViewController = Screen5()
  } else if screenName == "Screen6" {
    UIApplication.shared.keyWindow?.rootViewController = Screen6()
  } else {
    NSLog("Unknown screen \(screenName)")
  }
}
