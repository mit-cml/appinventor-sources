// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class Sharing: NonvisibleComponent {

  fileprivate func showActivityPicker(_ activityItems: [Any]) {
    let activityVC = UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    guard var view = _form?.view else {
      return
    }
    
    if let component = _form?._componentWithActiveEvent, let vc = component as? ViewComponent {
      view = vc.view
    }
    DispatchQueue.main.async {
      activityVC.popoverPresentationController?.sourceView = view  // For iPad popover
      self._form?.present(activityVC, animated: true, completion: nil)
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
