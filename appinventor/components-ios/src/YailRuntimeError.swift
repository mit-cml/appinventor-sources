// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

//@objc open class YailRuntimeError : NSException, Error, @unchecked Sendable {
//  @objc public init(_ message: String, _ errorType: String) {
//    super.init(name: errorType as NSString as NSExceptionName, reason: message, userInfo: nil)
//  }
//
//  public required init?(coder aDecoder: NSCoder) {
//    fatalError("init(coder:) has not been implemented")
//  }
//}
@objc open class YailRuntimeError : NSException, Error, @unchecked Sendable {
  @objc public init(_ message: String, _ errorType: String) {
    super.init(name: errorType as NSString as NSExceptionName, reason: message, userInfo: nil)
  }
  
  public required init?(coder aDecoder: NSCoder) {
    let alert = UIAlertController(title: "Error", message: "YailRuntimeError occurred. \(aDecoder)", preferredStyle: .alert)
    let okAction = UIAlertAction(title: "OK", style: .default) { _ in
      // Handle OK action if needed
    }
    alert.addAction(okAction)
    
    if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
      rootViewController.present(alert, animated: true, completion: nil)
    }
    
    return nil // Return nil to indicate failure
  }
}
