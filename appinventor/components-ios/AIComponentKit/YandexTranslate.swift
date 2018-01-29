// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class YandexTranslate: NonvisibleComponent {
  fileprivate let baseURL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";
  fileprivate var yandexKey: String

  public override init(_ container: ComponentContainer) {
    yandexKey = ""
    super.init(container)
  }

  open func RequestTranslation(_ languageToTranslateTo: String, _ textToTranslate: String){
    if (yandexKey == ""){
      _form.dispatchErrorOccurredEvent(self, "RequestTranslation", ErrorMessage.ERROR_TRANSLATE_NO_KEY_FOUND.code, ErrorMessage.ERROR_TRANSLATE_NO_KEY_FOUND.message)
      return;
    }

    let url = URL(string: "\(baseURL)\(yandexKey)")
    var request = URLRequest(url: url!)
    request.httpMethod = "POST"
    request.setValue("application/x-www-form-urlencoded; charset=utf-8", forHTTPHeaderField: "Content-Type")
    request.httpBody = "lang=\(languageToTranslateTo)&text=\(textToTranslate)".data(using: .utf8)

    let task = URLSession.shared.dataTask(with: request) { data, response, error in
      DispatchQueue.main.async {
        guard let data = data, error == nil else {
          self._form.dispatchErrorOccurredEvent(self, "RequestTranslation", ErrorMessage.ERROR_TRANSLATE_SERVICE_NOT_AVAILABLE.code, ErrorMessage.ERROR_TRANSLATE_SERVICE_NOT_AVAILABLE.message)
          return
        }

        var responseCode = 0
        if let httpStatus = response as? HTTPURLResponse {
          responseCode = httpStatus.statusCode
        }

        var translation = ""
        do {
          let parsedData = try JSONSerialization.jsonObject(with: data) as! [String:Any]
          if let text = parsedData["text"] as? NSArray {
            let swiftArray: [String] = NSMutableArray(array: text).flatMap({ $0 as? String })
            translation = swiftArray[0]
          }
        } catch {
          self._form.dispatchErrorOccurredEvent(self, "RequestTranslation", ErrorMessage.ERROR_TRANSLATE_JSON_RESPONSE.code, ErrorMessage.ERROR_TRANSLATE_JSON_RESPONSE.message)
          return
        }
        self.GotTranslation(responseCode: responseCode as NSNumber, translation: translation as NSString)
      }
    }
    task.resume()
  }

  open func GotTranslation(responseCode: NSNumber, translation: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "GotTranslation", arguments: responseCode, translation)

  }
}

