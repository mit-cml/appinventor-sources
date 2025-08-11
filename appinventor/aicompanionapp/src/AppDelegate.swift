// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import AIComponentKit

@available(iOS 14.0, *)
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?

  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    UserDefaults.standard.register(defaults: ["isNewUser": true])
    AppInventorRuntime.initialize()

    window = UIWindow(frame: UIScreen.main.bounds)
    let splitVC = UISplitViewController(style: .doubleColumn)
    splitVC.preferredDisplayMode = .secondaryOnly


    let storyboard = UIStoryboard(name: "Main", bundle: nil)
    let navController = storyboard.instantiateInitialViewController() as! ViewController
    splitVC.setViewController(navController, for: .secondary)


    let emptySidebar = UIViewController()
    emptySidebar.view.backgroundColor = .white
    let headerLabel = UILabel()
    headerLabel.text = "Sidebar"
    headerLabel.font = UIFont.boldSystemFont(ofSize: 24)
    headerLabel.textAlignment = .center
    headerLabel.textColor = .systemGray
    headerLabel.translatesAutoresizingMaskIntoConstraints = false
    emptySidebar.view.addSubview(headerLabel)
    NSLayoutConstraint.activate([
        headerLabel.centerXAnchor.constraint(equalTo: emptySidebar.view.centerXAnchor),
        headerLabel.topAnchor.constraint(equalTo: emptySidebar.view.safeAreaLayoutGuide.topAnchor, constant: 24)
    ])
    
    // Add Return to Screen button below the header
    let returnButton = UIButton(type: .system)
    returnButton.setTitle("Return to Screen", for: .normal)
    returnButton.titleLabel?.font = UIFont.systemFont(ofSize: 18, weight: .medium)
    returnButton.translatesAutoresizingMaskIntoConstraints = false
    emptySidebar.view.addSubview(returnButton)
    NSLayoutConstraint.activate([
        returnButton.centerXAnchor.constraint(equalTo: emptySidebar.view.centerXAnchor),
        returnButton.topAnchor.constraint(equalTo: headerLabel.bottomAnchor, constant: 24)
    ])
    returnButton.addAction(UIAction { _ in
        splitVC.show(.secondary)
    }, for: .touchUpInside)
    
    splitVC.setViewController(emptySidebar, for: .primary)

    window?.rootViewController = splitVC
    window?.makeKeyAndVisible()

    return true
  }

  func applicationWillResignActive(_ application: UIApplication) {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
  }

  func applicationDidEnterBackground(_ application: UIApplication) {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
  }

  func applicationWillEnterForeground(_ application: UIApplication) {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
  }

  func applicationDidBecomeActive(_ application: UIApplication) {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
  }

  func applicationWillTerminate(_ application: UIApplication) {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
  }
}


