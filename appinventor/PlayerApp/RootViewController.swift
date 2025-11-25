// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import AIComponentKit

fileprivate var runtimeInitialized = false

/**
 * Initializes the App Inventor runtime.
 */
func initializeRuntime() {
  guard !runtimeInitialized else {
    return
  }

  guard let runtimeUrl = Bundle(for: ReplForm.self).path(forResource: "runtime", ofType: "scm") else {
    // This should never happen and we cannot recover from it if it does.
    fatalError()
  }

  guard let runtimeScm = try? String(contentsOfFile: runtimeUrl) else {
    // This should never happen and we cannot recover from it if it does.
    fatalError()
  }

  SCMInterpreter.shared.evalForm(runtimeScm)

  runtimeInitialized = true
}


/**
 * The root view controller for apps. This serves as the screen stack and provides
 * backward navigation in the stack by default.
 */
class RootViewController: UINavigationController {
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  /**
   * Loads Screen1 from the packaged application data and initializes the stack of screens.
   */
  override func viewDidLoad() {
    super.viewDidLoad()
    initializeRuntime()
    SCMInterpreter.shared.setValue(self, forSymbol: "*the-root-view-controller*")
    let app = BundledApp(aiaPath: Bundle.main.url(forResource: "appdata", withExtension: "aia")!)
    app.makeCurrent()
    self.delegate = app
    let form = PlayerForm(application: app)
    form.edgesForExtendedLayout = .bottom
    form.loadScreen()
    app.theme = form.Theme

    self.setViewControllers([form], animated: false)
    navigationBar.barTintColor = argbToColor(form.PrimaryColor)
    navigationBar.isTranslucent = false
  }


  // We override this function to handle the Form's ScreenOrientation setting.
  open override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    guard let form = self.topViewController as? Form else {
      return .all
    }

    switch form.ScreenOrientation {
    case "portrait":
      return .portrait
    case "landscape":
      return .landscape
    default:
      return .all
    }
  }

  open override var shouldAutorotate: Bool {
    guard let form = self.topViewController as? Form else {
      return false
    }

    switch form.ScreenOrientation {
    case "portrait", "landscape":
      return false
    default:
      return true
    }
  }

  override public var childForStatusBarStyle: UIViewController? {
    return self.topViewController
  }

  @objc public func mark() {
    for vc in self.viewControllers {
      if let form = vc as? PlayerForm {
        form.mark()
      }
    }
  }
}

