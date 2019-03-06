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
    
    if let component = _form._componentWithActiveEvent, let vc = component as? ViewComponent {
      view = vc.view
    }
    DispatchQueue.main.async {
      activityVC.popoverPresentationController?.sourceView = view  // For iPad popover
      self._form.present(activityVC, animated: true, completion: nil)
    }
  }

  // MARK: Sharing Methods
  @objc open func ShareMessage(_ message: String) {
    showActivityPicker([message])
  }


  @objc open func ShareFile(_ file: String) {
    if let image = AssetManager.shared.imageFromPath(path: file) {
      showActivityPicker([image])
    } else {
      showActivityPicker([URL(fileURLWithPath: file)])
    }
  }

  @objc open func ShareFileWithMessage(_ file: String, _ message: String) {
    if let image = AssetManager.shared.imageFromPath(path: file) {
      showActivityPicker([message, image])
    } else {
      showActivityPicker([message, URL(fileURLWithPath: file)])
    }
  }
}
