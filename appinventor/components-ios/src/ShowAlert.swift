// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

func showAlert(message: String) {
  let alertController = UIAlertController(title: "Error", message: message, preferredStyle: .alert)
  alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
  UIApplication.shared.keyWindow?.rootViewController?.present(alertController, animated: true, completion: nil)
}
