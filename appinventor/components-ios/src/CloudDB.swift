// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Alamofire
import Foundation
import PSSRedisClient
import CocoaAsyncSocket

fileprivate func readScript(_ filename: String) -> String {
  if let path = Bundle(for: CloudDB.self).path(forResource: filename, ofType: nil),
    let script = try? String(contentsOfFile: path) {
    return script
  } else {
    return ""
  }
}

internal func readCert(_ cert: String) -> SecCertificate? {
  if let path = Bundle(for: CloudDB.self).url(forResource: cert, withExtension: "der"), let data = try? Data(contentsOf: path) {
    return SecCertificateCreateWithData(nil, data as CFData)
  } else {
    return nil
  }
}

fileprivate func getFileExtension(_ abspath: String) -> String {
  let url = URL(fileURLWithPath: abspath)
  let filename = url.lastPathComponent
  if let dotIndex = filename.lastIndex(of: ".") {
    return String(filename[filename.index(dotIndex, offsetBy: 1)...])
  } else {
    return ""
  }
}

fileprivate class StoredValue {
  fileprivate var _tag: String
  fileprivate var _jsonArray: [String]

  init(tag: String, jsonArray: [String]) {
    _tag = tag
    _jsonArray = jsonArray
  }

  var tag: String {
    return _tag
  }

  var jsonArray: [String] {
    return _jsonArray
  }
}

public class CloudDB: NonvisibleComponent, RedisManagerDelegate {

  fileprivate var _defaultRedisServer: String = ""
  fileprivate var _useDefault: Bool = true

  fileprivate var _projectID: String = ""
  fileprivate var _redisPort: Int32 = 6381
  fileprivate var _redisServer: String = ""
  fileprivate var _token: String = ""
  fileprivate var _useSSL: Bool = true

  fileprivate var _redis: RedisClient?
  fileprivate var _subscriptionManager: RedisClient?
  fileprivate var _socket: GCDAsyncSocket? = nil
  fileprivate var _redisQueue: DispatchQueue =
    DispatchQueue(label: "edu.mit.appinventor.CloudDBRedisQueue")
  fileprivate var _storeQueue: DispatchQueue =
    DispatchQueue(label: "edu.mit.appinventor.CloudDBStoreQueue")
  fileprivate var _storeList: [StoredValue] = []

  /**
   * Lock used to synchronize access to {@link _storeList} between the UI thread
   * running the blocks code and the background thread sending data to CloudDB.
   */
  fileprivate var _storeQueueLock = NSLock()

  fileprivate let APPEND_SCRIPT: String
  fileprivate let APPEND_SCRIPT_SHA: String = "d6cc0f65b29878589f00564d52c8654967e9bcf8"
  fileprivate let POP_SCRIPT: String
  fileprivate let POP_SCRIPT_SHA: String = "ed4cb4717d157f447848fe03524da24e461028e1"
  fileprivate let SUB_SCRIPT: String
  fileprivate let SUB_SCRIPT_SHA: String = "765978e4c340012f50733280368a0ccc4a14dfb7"

  fileprivate let _comodoCert: SecCertificate
  fileprivate let _dstRootX3: SecCertificate
  fileprivate let _comodoUsrtrust: SecCertificate

  @objc public override init(_ container: ComponentContainer) {
    _comodoCert = readCert("comodo_root")!
    _dstRootX3 = readCert("dst_root_x3")!
    _comodoUsrtrust = readCert("comodo_usrtrust")!

    SUB_SCRIPT = readScript("sub_script")
    POP_SCRIPT = readScript("pop_script")
    APPEND_SCRIPT = readScript("append_script")
    super.init(container)
  }

  fileprivate func getRedis() {
    _redisQueue.sync {
      if _redis == nil {
        var sslConfig: [String: NSObject] = [:]
        if _useSSL {
          sslConfig = [GCDAsyncSocketManuallyEvaluateTrust as String: true as NSObject]
        }
        var jToken: String = _token
        if _token.hasPrefix("%") {
          jToken = String(_token.dropFirst())
        }
        
        _redis = RedisClient(delegate: self, queue: _redisQueue, sslConfig: sslConfig)
        _subscriptionManager = RedisClient(delegate: self, queue: _redisQueue, sslConfig: sslConfig)
        _redis?.connect(host: _useDefault ? _defaultRedisServer: _redisServer, port: Int(_redisPort), pwd: jToken)
        _subscriptionManager?.connect(host: _useDefault ? _defaultRedisServer: _redisServer, port: Int(_redisPort), pwd: jToken)
      }
    }
  }

  fileprivate func parseError(_ message: NSArray) -> String? {
    if (message.firstObject as? NSError != nil) {
      let error = message.firstObject as! NSError
      let userInfo = error.userInfo

      if let possibleMessage = userInfo["message"] {
        if let actualMessage = possibleMessage as? String {
          return actualMessage
        }
      }
    }
    return nil
  }

  @objc open var DefaultRedisServer: String {
    get {
      return _defaultRedisServer
    }
    set(newDefault) {
      _defaultRedisServer = newDefault
      if _useDefault {
        _redisServer = newDefault
      }
    }
  }

  // MARK: Variables
  @objc open var ProjectID: String {
    get {
      checkProjectIDNotBlank("ProjectID")
      return _projectID
    }
    set(newId) {
      if newId != _projectID {
        _projectID = newId
      }
      if newId.isEmpty {
        _form?.dispatchErrorOccurredEvent(self, "ProjectID",
            ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.code,
            ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.message, "ProjectID")
      }
    }
  }

  @objc open var RedisPort: Int32 {
    get {
      return _redisPort
    }
    set(newPort) {
      if newPort != _redisPort {
        _redisPort = newPort
        flushRedis()
      }
    }
  }

  @objc open var RedisServer: String {
    get {
      if _redisServer == _defaultRedisServer {
        return "DEFAULT"
      } else {
        return _redisServer
      }
    }
    set(newServer) {
      if newServer == "DEFAULT" {
        if !_useDefault {
          if _defaultRedisServer.isEmpty {
            // this shouldn't be happening
          } else {
            _redisServer = newServer
          }
        }
        flushRedis()
      } else {
        _useDefault = false
        if newServer != _redisServer {
          _redisServer = newServer
          flushRedis()
        }
      }
    }
  }

  @objc open var Token: String {
    get {
      checkProjectIDNotBlank("Token")
      return _token
    }
    set(newToken) {
      if newToken != _token {
        _token = newToken
      }
      if newToken.isEmpty {
        _form?.dispatchErrorOccurredEvent(self, "ProjectID",
            ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.code,
            ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.message, "Token")
      }
    }
  }

  @objc open var UseSSL: Bool {
    get {
      return _useSSL
    }
    set(shouldUseSSL) {
      if (shouldUseSSL != _useSSL) {
        _useSSL = shouldUseSSL
        flushRedis()
      }
    }
  }

  // MARK: Public methods
  @objc open func AppendValueToList(_ tag: String, _ itemToAdd: AnyObject?) {
    checkProjectIDNotBlank("AppendValueToList")
    var item: String = ""
    do {
      item = try getJsonRepresentation(itemToAdd)
    } catch (let error) {
      jsonError("AppendValueToList", error)
    }
    getRedis()
    evaluate(script: APPEND_SCRIPT, scriptsha1: APPEND_SCRIPT_SHA, argsCount: 1, args: tag, item, _projectID)
  }

  @objc open func ClearTag(_ tag: String) {
    checkProjectIDNotBlank("GetTagList")
    getRedis()
    _redis?.exec(args: ["del", "\(_projectID):\(tag)"]) { message in
      if let error = self.parseError(message) {
        print("error")
        self._form?.runOnUiThread {
          self.CloudDBError(error)
        }
        self.flushRedis()
      }
    }
  }

  @objc open func CloudConnected() -> Bool {
    return NetworkReachabilityManager()?.isReachable ?? false
  }

  @objc open func GetTagList() {
    checkProjectIDNotBlank("GetTagList")
    getRedis()
    if CloudConnected() {
      _redis?.exec(args: ["keys", "\(_projectID):*"]) { message in
        if let error = self.parseError(message) {
          self._form?.runOnUiThread {
            self.CloudDBError(error)
          }
          self.flushRedis()
        } else {
          var tagList: [String] = []
          for object in message {
            let item = String(describing: object)
            let range = item.index(item.startIndex, offsetBy: "\(self._projectID):".count)..<item.endIndex
            tagList.append(String(item[range]))
          }
          self._form?.runOnUiThread {
            self.TagList(tagList)
          }
        }
      }
    } else {
      CloudDBError("Not connected to the Internet, cannot list tags")
    }
  }

  @objc open func GetValue(_ tag: String, _ valueIfTagNotThere: AnyObject?) {
    if CloudConnected() {
      getRedis()
      _redis?.exec(args: ["get", "\(_projectID):\(tag)"]) { message in
        if let error = self.parseError(message) {
          print("GetValue error: \(error)")
          self._form?.runOnUiThread {
            self.CloudDBError(error)
          }
          self.flushRedis()
        } else {
          do {
            var value: AnyObject?
            if message.count > 0 {
              if message[0] is NSNull {
                value = try getJsonRepresentation(valueIfTagNotThere) as AnyObject
              } else {
                value = getJsonRepresentationIfValueFileName(message[0] as AnyObject) as AnyObject? ?? message[0] as AnyObject
              }
            } else {
              value = try getJsonRepresentation(valueIfTagNotThere) as AnyObject
            }
            self._form?.runOnUiThread {
              self.GotValue(tag, value)
            }
          } catch let error {
            self.jsonError("GetValue", error)
          }
        }
      }
    } else {
      CloudDBError("Cannot fetch variables while off-line.")
    }
  }

  @objc open func RemoveFirstFromList(_ tag: String) {
    checkProjectIDNotBlank("RemoveFirstFromList")
    getRedis()
    evaluate(script: POP_SCRIPT, scriptsha1: POP_SCRIPT_SHA, argsCount: 1, args: tag, _projectID) { array, error in
      if !error {
        if let value = array[0] as? String, value != "null" {
          self._form?.runOnUiThread {
            self.FirstRemoved(value as AnyObject)
          }
        } else {
          self._form?.runOnUiThread {
            self.FirstRemoved("" as AnyObject)
          }
        }
      }
    }
  }

  @objc open func StoreValue(_ tag: String, _ valueToStore: AnyObject?) {
    checkProjectIDNotBlank("StoreValue")
    let value: String
    do {
      if let valString = valueToStore as? String {
        if valString.starts(with: "file:///") ||      // Android
           valString.starts(with: "/storage") ||      // Android
           valString.starts(with: "/var/mobile/") ||  // iOS device
           valString.starts(with: "/Users/") {        // iOS simulator
          value = try getJsonRepresentation(readFile(valString) as AnyObject)
        } else {
          value = try getJsonRepresentation(valueToStore)
        }
      } else {
        value = try getJsonRepresentation(valueToStore)
      }
    } catch (let error) {
      jsonError("StoreValue", error)
      return
    }
    getRedis()
    if CloudConnected() {
      synchronized(_storeQueueLock) {
        _storeList.append(StoredValue(tag: tag, jsonArray: [value]))
        if _storeList.count == 1 {
          _storeQueue.async {
            self.processStore()
          }
        }
      }
    } else {
      CloudDBError("Cannot store values off-line.")
    }
  }

  fileprivate func processStore() {
    print("In processStore")
    var work: StoredValue! = nil
    var pendingTag: String! = nil
    var pendingValue: String! = nil
    var pendingValueList: Array<String>! = nil
    var i = 0
    var done = false
    while (!done) {
      synchronized(_storeQueueLock) {
        work = i < _storeList.count ? _storeList[i] : nil
        i += 1
      }
      if let currentWork = work {
        // There's work to do
        let tag = currentWork.tag
        let valueList = currentWork.jsonArray
        if pendingTag == nil {
          // First work object
          pendingTag = tag
          pendingValue = valueList[0]
          pendingValueList = valueList
        } else if pendingTag == tag {
          // New work object for same tag
          pendingValue = valueList[0]
          pendingValueList.append(pendingValue)
        } else {
          // New work object for new tag
          let jsonValueList =
            try! getJsonRepresentation(pendingValueList as AnyObject)
          evaluate(script: SUB_SCRIPT, scriptsha1: SUB_SCRIPT_SHA, argsCount: 1,
                   args: pendingTag, pendingValue, jsonValueList, _projectID)
          pendingTag = tag
          pendingValue = valueList[0]
          pendingValueList = valueList
        }
      } else {
        // No work remaining
        if let pendingTag = pendingTag {
          let jsonValueList =
            try! getJsonRepresentation(pendingValueList as AnyObject)
          evaluate(script: SUB_SCRIPT, scriptsha1: SUB_SCRIPT_SHA, argsCount: 1,
                   args: pendingTag, pendingValue, jsonValueList, _projectID)
        }
        // There was no more work earlier (work == nil), but we need to double
        // check before exiting.
        synchronized(_storeQueueLock) {
          if i >= _storeList.count {
            _storeList.removeAll(keepingCapacity: true)
            done = true
          }
        }
      }
    }
  }

  // MARK: Events
  @objc open func CloudDBError(_ message: String) {
    print("CloudDBError called \(message)")
    EventDispatcher.dispatchEvent(of: self, called: "CloudDBError", arguments: message as AnyObject)
  }

  @objc open func DataChanged(_ tag: String, _ value: AnyObject?) {
    var tagValue = "" as AnyObject?
    if let valAsString = value as? String {
      tagValue = try? getObjectFromJson(valAsString) ?? "" as AnyObject
    }
    EventDispatcher.dispatchEvent(of: self, called: "DataChanged", arguments: tag as AnyObject, tagValue ?? "" as AnyObject)
  }

  @objc open func FirstRemoved(_ value: AnyObject?) {
    checkProjectIDNotBlank("RemoveFirstFromList")
    do {
      var newValue = value
      if let sValue = value as? String {
        newValue = try getObjectFromJson(sValue) ?? "" as AnyObject
      }
      EventDispatcher.dispatchEvent(of: self, called: "FirstRemoved", arguments: newValue!)
    } catch let error {
      jsonError("RemoveFirstFromList", error)
    }
  }

  @objc open func GotValue(_ tag: String, _ value: AnyObject?) {
    checkProjectIDNotBlank("GetValue")
    do {
      if let value = value {
        var parsedValue: AnyObject = value
        if let valueAsString = value as? String {
          parsedValue = try getYailObjectFromJson(valueAsString, true)
        }
        EventDispatcher.dispatchEvent(of: self, called: "GotValue", arguments: tag as AnyObject, parsedValue)
      } else {
        CloudDBError("Trouble getting \(tag) from the server.")
      }
    } catch (let error) {
      jsonError("GetValue", error)
    }
  }

  @objc open func TagList(_ value: [String]) {
    checkProjectIDNotBlank("TagList")
    EventDispatcher.dispatchEvent(of: self, called: "TagList", arguments: value as AnyObject)
  }

  // MARK: Helper methods
  fileprivate func checkProjectIDNotBlank(_ cause: String) {
    if _projectID.isEmpty {
      _form?.dispatchErrorOccurredEvent(self, cause,
          ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.code,
          ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.message, "ProjectID")
    }
    if _token.isEmpty {
      _form?.dispatchErrorOccurredEvent(self, cause,
          ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.code,
          ErrorMessage.ERROR_EMPTY_CLOUDDB_PROPERTY.message, "ProjectID")
    }
  }

  fileprivate func evaluate(script: String, scriptsha1: String, argsCount: Int32, args: String..., completion: ((NSArray, Bool) -> ())? = nil) {
    _redis?.exec(args: ["evalsha", scriptsha1, String(argsCount)] + args) { message in
      if self.parseError(message) != nil {
        self._redis?.exec(args: ["eval", script, String(argsCount)] + args) { messages in
          if let secondError = self.parseError(messages) {
            self._form?.runOnUiThread {
              self.CloudDBError(secondError)
            }
            self.flushRedis()
            if let next = completion {
              next(message, true)
            }
          } else {
            if let next = completion {
              next(messages, false)
            }
          }
        }
      } else {
        if let next = completion {
          next(message, false)
        }
      }
    }
  }

  fileprivate func flushRedis() {
    if let redis = _redis {
      redis.close()
      _redis = nil
    }
    if let subscription = _subscriptionManager {
      subscription.close()
      _subscriptionManager = nil
    }
  }

  fileprivate func jsonError(_ method: String, _ error: Error) {
    print("a json error occured: \(error)")
    _form?.dispatchErrorOccurredEvent(self, method,
        ErrorMessage.ERROR_CLOUDDB_JSON_MALFORMED.code,
        ErrorMessage.ERROR_CLOUDDB_JSON_MALFORMED.message, error.localizedDescription)
  }

  private func readFile(_ originalFilename: String) throws -> [String] {
    var filename = originalFilename
    if originalFilename.starts(with: "file://") {
      filename = String(filename[filename.index(filename.startIndex, offsetBy: 7)...])
    }
    if !filename.starts(with: "/") {
      throw YailRuntimeError("Invalid fileName, was " + originalFilename, "ReadFrom")
    }
    let fileExt = getFileExtension(filename)
    let content = try Data(contentsOf: URL(fileURLWithPath: AssetManager.shared.pathForExistingFileAsset(filename)))
    let encodedContent = content.base64EncodedString()
    return [".\(fileExt)", encodedContent]
  }

  // MARK: RedisManagerDelegate implementation

  public func subscriptionMessageReceived(channel: String, message: String) {
    do {
      let json = try getObjectFromJson(message)
      if let arr = json as? NSArray,
         let tag = arr[0] as? String,
         let valArray = arr[1] as? Array<AnyObject> {
        for value in valArray {
          let retValue = getJsonRepresentationIfValueFileName(value)
          self._form?.runOnUiThread {
            self.DataChanged(tag, retValue as AnyObject? ?? value)
          }
        }
      } else {
        self._form?.runOnUiThread {
          self.CloudDBError("System Error: Unable to parse CloudDB packet")
        }
      }
    } catch let error {
      jsonError("DataChanged", error)
    }
  }

  public func socket(_ sock: GCDAsyncSocket, didReceive trust: SecTrust, completionHandler: @escaping (Bool) -> Void) {
    let certArray = [ _comodoCert, _comodoUsrtrust, _dstRootX3 ]
    let status = SecTrustSetAnchorCertificates(trust, certArray as NSArray)
    SecTrustSetAnchorCertificatesOnly(trust, false)  // also allow any trusted system root certificate
    if status == errSecSuccess {
      SecTrustEvaluateAsync(trust, DispatchQueue.global()) { (trust, result) in
        switch result {
        case .proceed, .unspecified:
          completionHandler(true)
        default:
          self._form?.runOnUiThread {
            self.CloudDBError("Unable to establish a secure connection with CloudDB")
          }
          completionHandler(false)
        }
      }
    } else {
      completionHandler(false)
    }
  }

  public func socketDidDisconnect(client: RedisClient, error: Error?) {
    self._form?.runOnUiThread {
      self.CloudDBError("Unable to connect to client: \(error?.localizedDescription ?? "unknown error")")
    }
    flushRedis()
  }

  public func socketDidConnect(client: RedisClient) {
    if _redis != nil, let manager = _subscriptionManager {
      manager.exec(args: ["subscribe", _projectID]) { res in
      }
    }
  }

}
