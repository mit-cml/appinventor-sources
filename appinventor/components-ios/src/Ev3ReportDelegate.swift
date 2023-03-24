// -*- mode: swift; swift-mode:basic-offset: 2; -*-
//  https://github.com/andiikaa/ev3ios
//
//  Created by Andre on 22.04.16.
//  Copyright © 2016 Andre. All rights reserved.
//
import Foundation

protocol Ev3ReportDelegate {
  
  func reportReceived(report: [UInt8])
}
