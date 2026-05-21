// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

@objc open class BaseClassifier: BaseAiComponent {
  private var _labels = [String]()

  @objc public override init(_ container: ComponentContainer, _ basePath: String, _ baseModel: String?, _ personalModel: String?) {
    super.init(container, basePath, baseModel, personalModel)
  }

  // MARK: Properties

  @objc public var ModelLabels: [String] {
    return _labels
  }

  // MARK: Private Implementation

  open override func onModelReady(_ args: String) {
    do {
      guard let result = try getYailObjectFromJson(args, true) as? YailList<AnyObject> else {
        print("Unable to parse result")
        return
      }
      print(result)
      var labelList: [String] = []
      for el in result {
        if let label = el as? String {
          labelList.append(label)
        }
      }
      _labels = labelList
      super.onModelReady(args)
    } catch {
      print("Error parsing JSON from web view function ready")
    }
  }

  open override func ModelReady() {
    ClassifierReady()
  }

  @objc public func ClassifierReady() {
    debugPrint("classifierReady")
    DispatchQueue.main.async { [self] in
      EventDispatcher.dispatchEvent(of: self, called: "ClassifierReady")
    }
  }

  open override func GotResult(_ result: AnyObject) {
    GotClassification(result)
  }

  @objc public func GotClassification(_ result: AnyObject) {
    debugPrint("GotClassification")
    DispatchQueue.main.async { [self] in
      EventDispatcher.dispatchEvent(of: self, called: "GotClassification", arguments: result)
    }
  }

  @objc override public func Error(_ errorCode: Int32) {
    debugPrint("ErrorFunction")
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "Error", arguments: errorCode as AnyObject)
    }
  }
}
