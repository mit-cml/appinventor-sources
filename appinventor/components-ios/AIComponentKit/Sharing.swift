//
//  Sharing.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class Sharing: NonvisibleComponent {

  fileprivate func showActivityPicker(_ activityItems: [Any]) {
    let activityVC = UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    var view = _form.view
    if let component = _form._componentWithActiveEvent {
      if component is ViewComponent {
        let vc = component as! ViewComponent
        view = vc.view
      }
    }
    activityVC.popoverPresentationController?.sourceView = view  // For iPad popover
    _form.present(activityVC, animated: true, completion: nil)
  }

  // MARK: Sharing Methods
  open func ShareMessage(_ message: String) {
    showActivityPicker([message])
  }

  open func ShareFile(_ file: String) {
    let fileUrl = URL(fileURLWithPath: file)
    showActivityPicker([fileUrl])
  }

  open func ShareFileWithMessage(_ file: String, _ message: String) {
    let fileUrl = URL(fileURLWithPath: file)
    showActivityPicker([message, fileUrl])
  }
}
