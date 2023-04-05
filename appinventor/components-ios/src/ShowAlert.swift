//
//  ShowAlert.swift
//  AIComponentKit
//
//  Created by Dhruv Shrivastava on 05/04/23.
//  Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

func showAlert(message: String?) {
  if let emessage = message {
    let alertController = UIAlertController(title: "Error", message: emessage, preferredStyle: .alert)
    alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
    UIApplication.shared.keyWindow?.rootViewController?.present(alertController, animated: true, completion: nil)
  } else {
    let alertController = UIAlertController(title: "Error", message: "init(coder:) has not been implemented", preferredStyle: .alert)
    alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
    UIApplication.shared.keyWindow?.rootViewController?.present(alertController, animated: true, completion: nil)
  }
}
