//
//  AssetFetcher.swift
//  AIComponentKit
//
//  Created by Li Li on 3/10/22.
//  Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation

open class AssetFetcher: NSObject {
  fileprivate static let lock = DispatchSemaphore(value: 1)
  fileprivate static var inError: Bool = false
  
  public static func fetchAssets(cookieValue: String, projectId: String, uri: String, asset: String) {
    DispatchQueue.global(qos: .background).async {
      let fileName = uri + "/ode/download/file/" + projectId + "/" + asset;
      if (!getFile(fileName: fileName, cookieValue: cookieValue, asset: asset, depth: 0)) {
        RetValManager.shared().assetTransferred(asset);
      }
    }
  }
  
  private static func getFile(fileName: String, cookieValue: String, asset: String, depth: Int) -> Bool {
    if (depth > 1) {
      lock.wait()
      if (!inError) {
        inError = true
        DispatchQueue.main.async {
           fatalError("Unable to load file " + fileName)
        }
      }
      AssetFetcher.lock.signal()
      return false
    }
    var getFileError = false
    guard let url = URL(string: fileName) else { return false}
    var request = URLRequest(url: url)
    request.httpMethod = "GET"
    request.setValue("cookie", forHTTPHeaderField: "AppInventor = " + cookieValue)
    URLSession.shared.dataTask(with: request, completionHandler: { (data, response, responseerror) in
      let destination = URL(string: AssetManager.shared.pathForPrivateAsset(asset))
      if let data = data {
        do {
          try data.write(to: destination!)
        } catch {
         getFileError =  getFile(fileName: fileName, cookieValue: cookieValue, asset: asset, depth: depth + 1)
        }
      }
    }
    ).resume()
    if (getFileError) {
      return getFile(fileName: fileName, cookieValue: cookieValue, asset: asset, depth: depth + 1)
    }
   return true
  }
}

