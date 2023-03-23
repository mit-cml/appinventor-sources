// -* mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Zip
import UIKit

public class BundledApp : Application, UINavigationControllerDelegate {
  let path: String
  var theme: String = ""

  @objc public convenience init(aiaPath: URL) {
    var name = ""
    var path = ""
    do {
      Zip.addCustomFileExtension("aia")
      var docUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
      docUrl.appendPathComponent("apps", isDirectory: true)
      let dirname = aiaPath.lastPathComponent.replace(target: ".\(aiaPath.pathExtension)", withString: "")
      docUrl.appendPathComponent(dirname, isDirectory: true)
      path = docUrl.absoluteString.replace(target: "file://", withString: "")
      if FileManager.default.fileExists(atPath: path) {
        try FileManager.default.removeItem(at: docUrl)
      }
      try FileManager.default.createDirectory(at: docUrl, withIntermediateDirectories: true)
      try Zip.unzipFile(aiaPath, destination: docUrl, overwrite: true, password: nil)
      name = aiaPath.lastPathComponent
      if name.hasSuffix(".aia"), let dot = name.firstIndex(of: ".") {
        name = String(name[..<dot])
      }
    } catch {
      print(error)
    }
    self.init(named: name, at: path)
  }

  @objc public init(named name: String, at path: String) {
    self.path = path
    super.init(named: name)
  }

  @objc open override func pushScreen(named name: String, with startValue: NSObject? = nil) {
    guard let navigationController = ReplForm.activeForm?.navigationController else {
      // Technically this should always be true, but stranger things have happened.
      print("No navigation stack available for pushing forms")
      return
    }
    guard let screen = locateScreen(named: name) else {
      print("Unable to find screen \(name) in project")
      return
    }
    guard let yail = try? String(contentsOfFile: screen)  else {
      print("Unable to load YAIL for screen \(name)")
      return
    }
    DispatchQueue.main.async {
      let newForm = ReplForm(application: self)
      newForm.formName = name
      newForm.Theme = self.theme
      if let startValue = startValue {
        newForm.startValue = startValue
      }
      let interpreter = SCMInterpreter.shared
      interpreter.setCurrentForm(newForm)
      interpreter.evalForm(yail)
      navigationController.pushViewController(newForm, animated: true)
    }
  }

  @objc open override func popScreen(with closeValue: String) {
    guard let navigationController = ReplForm.activeForm?.navigationController else {
      // Technically this should always be true, but stranger things have happened.
      print("No navigation stack available for pushing forms")
      return
    }
    guard let oldForm = navigationController.viewControllers.last as? ReplForm else {
      return
    }
    DispatchQueue.main.async {
      SCMInterpreter.shared.setCurrentForm(oldForm)
      oldForm.OtherScreenClosed(oldForm.lastFormName, oldForm.formResult ?? ("" as AnyObject))
    }
  }

  @objc open func loadScreen1(_ form: ReplForm) {
    guard let screen = locateScreen(named: "Screen1") else {
      print("Unable to find screen Screen1 in project")
      return
    }
    guard let yail = try? String(contentsOfFile: screen) else {
      print("Unable to load YAIL for screen Screen1")
      return
    }
    form.navigationController?.delegate = self
    form.application = self
    self.makeCurrent()
    form.formName = "Screen1"
    form.clear()
    let interpreter = SCMInterpreter.shared
    interpreter.setCurrentForm(form)
    interpreter.evalForm(yail)
    if let exception = interpreter.exception {
      print("\(exception)")
    }
    theme = form.Theme
  }

  private func locateScreen(named name: String) -> String? {
    if FileManager.default.fileExists(atPath: "\(path)/src") {
      do {
        var paths = try FileManager.default.contentsOfDirectory(atPath: "\(path)/src/appinventor")
        let userdir = paths[0]  // paths[0] will be the ai_username directory
        paths = try FileManager.default.contentsOfDirectory(atPath: "\(path)/src/appinventor/\(userdir)")
        let projectdir = paths[0]  // paths[0] will be the project name
        let finalPath = "\(path)/src/appinventor/\(userdir)/\(projectdir)/\(name).yail"
        if FileManager.default.fileExists(atPath: finalPath) {
          return finalPath
        }
        return nil
      } catch {
        return nil
      }
    } else {
      return Bundle.main.path(forResource: name, ofType: "yail", inDirectory: path)
    }
  }

  open func navigationController(_ navigationController: UINavigationController, didShow viewController: UIViewController, animated: Bool) {
    if let form = viewController as? ReplForm, !form.initialized {
      form.Initialize()
    }
  }
}
