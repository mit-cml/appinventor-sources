// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

fileprivate let TRANSFER_MODEL_PREFIX = "appinventor://personal-audio-classifier/transfer/"
fileprivate let PERSONAL_MODEL_PREFIX = "appinventor://personal-audio-classifier/personal/"

@objc open class PersonalAudioClassifier: BaseClassifier{
  
  
  @objc public init(_ container: ComponentContainer) {
    super.init(container, "personal_audio_classifier", TRANSFER_MODEL_PREFIX, PERSONAL_MODEL_PREFIX)
  }
  
  
  
  //MARK: Methods

    func encodeFileToBase64(_ file: URL) -> String {
        do {
            let fileData = try Data(contentsOf: file)
            let base64File = fileData.base64EncodedString().replacingOccurrences(of: "\n", with: "")
            return base64File
        } catch {
            print("Exception while reading the file \(error)")
            return ""
        }
    }

    @objc public func ClassifySoundData(_ sound: String) {
        guard let webview = _webview else {
            print("WebView is not set")
            return
        }
        print("Entered Classify Sound Data")
        
        let soundFile = URL(fileURLWithPath: sound)
        print("soundFile: \(soundFile)")
        
        let encodedSound = encodeFileToBase64(soundFile)
        print("encodedSound: \(encodedSound)")
        
        let jsCommand = "getSpectrogram(\"\(encodedSound)\");"
        webview.evaluateJavaScript(jsCommand) { (result, error) in
            if let error = error {
                print("JavaScript evaluation error: \(error)")
            } else {
                print("encodedSound sent to Javascript!")
            }
        }
    }

    // MARK: Events

  /**
   * `onError` is called when an error ocurrs. By default, it extracts two arguments,
   *  which should be the numeric error code and the string error message.
   */
  
  open override func onError(_ args: String) {
    print("error")
    do {
      guard let result = try getYailObjectFromJson(args, true) as? YailList<AnyObject> else {
        print("Unable to parse error: \(args)")
        return
      }
      debugPrint(result)
      Error(result[0] as? Int32 ?? -999, result[1] as? String ?? "")
    } catch {
      print("Error parsing JSON from web view function error")
    }
  }

  @objc public func Error(_ errorCode: Int32, _ errorMsg: String) {
    debugPrint("ErrorFunction")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode as AnyObject, errorMsg as AnyObject)
    }
  }
}
