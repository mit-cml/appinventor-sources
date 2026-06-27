// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

class AssetLoadError: Error {
}

@objc open class AssetFetcher: NSObject {
  fileprivate static let lock = DispatchSemaphore(value: 1)
  fileprivate static var inError: Bool = false
  
  @objc public static func fetchAssets(_ cookieValue: String, _ projectId: String, _ uri: String, _ asset: String) {
    DispatchQueue.main.async {
      let fileName = uri + "/ode/download/file/" + projectId + "/" + asset;
      getFile(fileName: fileName, cookieValue: cookieValue, asset: asset, depth: 0) { success, error in
        if success {
          RetValManager.shared().assetTransferred(asset)
        } else if let error = error {
          DispatchQueue.main.async {
            guard let window = UIApplication.shared.keyWindow else {
              return
            }
            let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
            window.makeToast("Unable to load \(asset) with error \(error)", point: center,
                             title: nil, image: nil, completion: nil)
          }
        }
      }
    }
  }
  
  @objc public static func fetchCachedProject(_ cookieValue: String, _ projectId: String, _ uri: String, _ projectName: String) {
    DispatchQueue.main.async {
      guard let window = UIApplication.shared.keyWindow else {
        return
      }
      guard let rootView = window.rootViewController else {
        return
      }
      let alert = UIAlertController(title: "Saving", message: "Downloading and saving your project...", preferredStyle: .alert)
      let spinner = UIActivityIndicatorView(style: .gray)
      spinner.translatesAutoresizingMaskIntoConstraints = false;
      alert.view.addSubview(spinner)
      NSLayoutConstraint.activate([
        spinner.bottomAnchor.constraint(equalTo: alert.view.bottomAnchor, constant: -10),
        spinner.centerXAnchor.constraint(equalTo: alert.view.centerXAnchor),
        alert.view.heightAnchor.constraint(equalToConstant: 100)
      ])
      rootView.present(alert, animated: true) {
        spinner.startAnimating()
      }
      let projectUrl = uri + "/ode/download/project-cached/" + projectId;
      getProject(projectUrl: projectUrl, cookieValue: cookieValue, projectName: projectName, depth: 0) { success, error in
        if success {
          DispatchQueue.main.async {
            let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
            spinner.stopAnimating()
            alert.dismiss(animated: true)
            window.makeToast("Head to your Library to open your downloaded app.", point: center,
                             title: "Project successfully downloaded!", image: nil, completion: nil)
          }
        } else if let error = error {
          DispatchQueue.main.async {
            let center = CGPoint(x: window.frame.size.width / 2.0, y: window.frame.size.height / 2.0)
            spinner.stopAnimating()
            alert.dismiss(animated: true)
            window.makeToast("Unable to download project with error \(error)", point: center,
                             title: nil, image: nil, completion: nil)
          }
        }
      }
    }
  }

  @objc public static func loadExtensions(_ extensionsJson: String) {
    // Loading extensions is not supported on iOS
    RetValManager.shared().extensionsLoaded()
  }
  
  private static func getFile(fileName: String, cookieValue: String, asset: String, depth: Int, completionHandler: @escaping (Bool, Error?) -> Void) {
    guard depth <= 1 else {
      lock.wait()
      if (!inError) {
        inError = true
        DispatchQueue.main.async {
          completionHandler(false, AssetLoadError())
        }
      }
      AssetFetcher.lock.signal()
      return
    }
    guard let url = URL(string: fileName) else {
      return
    }
    var request = URLRequest(url: url)
    request.setValue("AppInventor = " + cookieValue, forHTTPHeaderField: "Cookie")
    let task = URLSession.shared.dataTask(with: request) { (data, response, responseerror) in
      let filename = asset.chopPrefix(count: "assets/".count)
      let destination = URL(fileURLWithPath: AssetManager.shared.pathForPublicAsset(filename),
                            isDirectory: false)
      print("Saving asset to \(destination)")
      if let data = data {
        do {
          try FileManager.default.createDirectory(at: destination.deletingLastPathComponent(),
                                                  withIntermediateDirectories: true)
          try data.write(to: destination)
          DispatchQueue.main.async {
            completionHandler(true, nil)
          }
        } catch {
          getFile(fileName: fileName, cookieValue: cookieValue, asset: asset, depth: depth + 1,
                  completionHandler: completionHandler)
        }
      } else if let error = responseerror {
        completionHandler(false, error)
      }
    }
    task.priority = 1.0
    task.resume()
  }
  
  @objc static func getProject(projectUrl: String, cookieValue: String, projectName: String, depth: Int, completionHandler: @escaping (Bool, Error?) -> Void) {
    guard depth <= 1 else {
      lock.wait()
      if (!inError) {
        inError = true
        DispatchQueue.main.async {
          completionHandler(false, AssetLoadError())
        }
      }
      AssetFetcher.lock.signal()
      return
    }
    guard let url = URL(string: projectUrl) else {
      return
    }
    var request = URLRequest(url: url)
    request.setValue("AppInventor = " + cookieValue, forHTTPHeaderField: "Cookie")
    let task = URLSession.shared.dataTask(with: request) { (data, response, responseerror) in
      let samplesDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
          .appendingPathComponent("samples", isDirectory: true)
      try? FileManager.default.createDirectory(at: samplesDirectory, withIntermediateDirectories: true)
      let destination = samplesDirectory.appendingPathComponent(projectName + ".aia")
      if let data = data {
        do {
          try data.write(to: destination)
          DispatchQueue.main.async {
            completionHandler(true, nil)
          }
          // Unzips the .aia project into the /apps directory to allow for access to the app icon for the Library
          let newapp = BundledApp(aiaPath: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("samples/\(projectName).aia", isDirectory: false))
        } catch {
          getProject(projectUrl: projectUrl, cookieValue: cookieValue, projectName: projectName, depth: depth + 1, completionHandler: completionHandler)
        }
      } else if let error = responseerror {
        completionHandler(false, error)
      }
    }
    task.priority = 1.0
    task.resume()
  }
  
}

