// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Base58Swift

// URL where we can reach the ChatBot
let CHATBOT_HOST: URL! = URL(string:  "https://chatbot.appinventor.mit.edu/")
let kRequestError: Int32 = -1

open class ChatBot: ProxiedComponent<ChatBot_token, ChatBot_request, ChatBot_response> {
  private static let SERVICE_URL = CHATBOT_HOST.appendingPathComponent("chat/v1")
  private var _uuid = ""

  @objc public init(_ container: ComponentContainer) {
    super.init(container, ChatBot.SERVICE_URL)
  }

  // MARK: Properties

  @objc open var Apikey: String {
    get {
      return ApiKey
    }
    set {
      ApiKey = newValue
    }
  }
  @objc open var Model: String = ""
  @objc open var Provider: String = "chatgpt"
  @objc open var System: String = ""

  // MARK: Methods

  @objc open func ResetConversation() {
    _uuid = ""
  }

  @objc open func Converse(_ message: String) {
    do {
      try doRequest(configuration: {
        $0.uuid = _uuid
        $0.question = message
        if _uuid.isEmpty && !System.isEmpty {
          $0.system = System
        }
        $0.provider = Provider
        if !Model.isEmpty {
          $0.model = Model
        }
      }) {
        if let error = $2 {
          self.ErrorOccurred($0, (error as? ProxyError)?.message ?? error.localizedDescription)
        } else if let response = $1 {
          self._uuid = response.uuid
          self.GotResponse(response.answer)
        }
      }
    } catch {
      self.ErrorOccurred(kRequestError, "\(error)")
    }
  }

  @objc open func ConverseWithImage(_ message: String, _ source: AnyObject) {
    guard let sourceData = loadImageData(source, resize: 1024) else {
      ErrorOccurred(kUnableToLoadSourceError, "Unable to load source")
      return
    }
    print("Image size: \(sourceData.count)")
    do {
      try doRequest(configuration: {
        $0.uuid = _uuid
        $0.question = message
        if _uuid.isEmpty && !System.isEmpty {
          $0.system = System
        }
        $0.provider = Provider
        if !Model.isEmpty {
          $0.model = Model
        }
        $0.inputimage = sourceData
      }) {
        if let error = $2 {
          self.ErrorOccurred($0, (error as? ProxyError)?.message ?? error.localizedDescription)
        } else if let response = $1 {
          self._uuid = response.uuid
          self.GotResponse(response.answer)
        }
      }
    } catch {
      self.ErrorOccurred(kRequestError, "\(error)")
    }
  }

  // MARK: Events

  @objc open func GotResponse(_ responseText: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "GotResponse",
                                    arguments: responseText as NSString)
    }
  }

  @objc open func ErrorOccurred(_ responseCode: Int32, _ responseText: String) {
    DispatchQueue.main.async {
      if !EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred",
                                        arguments: responseCode as AnyObject,
                                        responseText as AnyObject) {
        self._form?.dispatchErrorOccurredEvent(self, "ErrorOccurred",
            ErrorMessage.ERROR_CHATBOT_ERROR, responseCode as AnyObject, responseText as AnyObject)
      }
    }
  }
}

extension ChatBot_request: HasToken {
  public typealias T = ChatBot_token
}

extension ChatBot_response: HasResponse {
  public typealias T = String

  public var response: String {
    return self.answer
  }
}
