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
    DispatchQueue.global(qos: .background).async {
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
    URLSession.shared.dataTask(with: request) { (data, response, responseerror) in
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
    }.resume()
  }
}

