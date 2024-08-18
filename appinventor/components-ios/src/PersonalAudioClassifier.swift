// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit

@objc open class PersonalAudioClassifier: BaseAiComponent{
  
  
  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
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

    @objc func classifySoundData(_ sound: String) {
        guard let _webView = _webView else {
            print("WebView is not set")
            return
        }
        print("Entered Classify Sound Data")
        
        let soundFile = URL(fileURLWithPath: sound)
        print("soundFile: \(soundFile)")
        
        let encodedSound = encodeFileToBase64(soundFile)
        print("encodedSound: \(encodedSound)")
        
        let jsCommand = "getSpectrogram(\"\(encodedSound)\");"
        _webView.evaluateJavaScript(jsCommand) { (result, error) in
            if let error = error {
                print("JavaScript evaluation error: \(error)")
            } else {
                print("encodedSound sent to Javascript!")
            }
        }
    }



    // MARK: Events

    @objc override public func ClassifierReady() {
      print("classifierReady")
        DispatchQueue.main.async { [self] in
        EventDispatcher.dispatchEvent(of: self, called: "ClassifierReady")
        }
    }

    @objc override public func GotClassification(_ result: AnyObject) {
        print("GotClassification")
        DispatchQueue.main.async { [self] in
        EventDispatcher.dispatchEvent(of: self, called: "GotClassification", arguments: result)
        }
    }

    @objc override public func Error(_ errorCode: Int32) {
        print("ErrorFunction")
        DispatchQueue.main.async {
        EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode as AnyObject)
        }
    }
}