// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class Web: NonvisibleComponent {
  fileprivate var _url = ""
  fileprivate var _requestHeaders = YailList<YailList<NSString>>()
  fileprivate var _allowCookies = false
  fileprivate var _saveResponse = false
  fileprivate var _responseFileName = ""
  fileprivate var _cookieStorage = HTTPCookieStorage()
  private var _timeout: Int32 = 0

  fileprivate let stringToEncoding: [String: String.Encoding] =
    ["utf8": .utf8, "utf-8": .utf8, "utf16": .utf16, "utf-16": .utf16, "utf32": .utf32,
     "utf-32": .utf32, "iso2022jp": .iso2022JP, "iso-2022-jp": .iso2022JP, "iso-8859-1": .isoLatin1,
     "iso88591": .isoLatin1, "iso-8859-2": .isoLatin2, "iso88592": .isoLatin2, "ascii": .ascii]

  private func stringToEncodedData(_ encoding: String, _ text: String) -> Data? {
    guard let encodingType = stringToEncoding[encoding.lowercased()] else {
      self._form?.dispatchErrorOccurredEvent(self, "stringToEncodedData",
          ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.code, encoding)
      return nil
    }
    return text.data(using: encodingType)
  }

  // MARK: - Web Properties
  
  @objc open var Url: String {
    get {
      return _url
    }
    set(url) {
      _url = url.addingPercentEncoding(withAllowedCharacters: NSCharacterSet.urlQueryAllowed) ?? ""
    }
  }
  
  @objc open var RequestHeaders: YailList<YailList<NSString>> {
    get {
      return _requestHeaders
    }
    set(list) {
      if validateRequestHeaders(list) {
        _requestHeaders = list
      }
    }
  }

  @objc open var AllowCookies: Bool {
    get {
      return _allowCookies
    }
    set(allowCookies) {
      _allowCookies = allowCookies
    }
  }
  
  @objc open var SaveResponse: Bool {
    get {
      return _saveResponse
    }
    set(saveResponse) {
      _saveResponse = saveResponse
    }
  }
  
  @objc open var ResponseFileName: String {
    get {
      return _responseFileName
    }
    set(responseFileName) {
      _responseFileName = responseFileName
    }
  }

  @objc open var Timeout: Int32 {
    get {
      return _timeout
    }
    set (timeout) {
      _timeout = timeout
    }
  }

  // MARK: - Web Methods
  
  @objc open func ClearCookies() {
    _cookieStorage = HTTPCookieStorage.init()
  }

  fileprivate func validateRequestHeaders(_ list: YailList<YailList<NSString>>) -> Bool {
    do {
      _ = try processRequestHeaders(list)
      return true
    } catch (let error as InvalidHeadersError) {
      _form?.dispatchErrorOccurredEvent(self, "RequestHeaders", error.code, error.index)
    } catch (let error) {
      NSLog("Unexpected error occurred:", error.localizedDescription)
    }
    return false
  }
  
  @objc open func Get() {
    guard let webProps = capturePropertyValues("Get") else {
      return
    }
    performRequest(webProps, nil, nil, "GET")
  }

  @objc open func PostText(_ text: String) {
    requestTextImpl(text: text, encoding: "UTF-8", functionName: "PostText", httpVerb: "POST")
  }

  @objc open func PostTextWithEncoding(_ text: String, _ encoding: String?) {
    requestTextImpl(text: text, encoding: encoding, functionName: "PostTextWithEncoding", httpVerb: "POST")
  }

  @objc open func PostFile(_ path: String) {
    guard let webProps = capturePropertyValues("PostFile") else {
      return
    }
    performRequest(webProps, nil, path, "POST")
  }

  @objc open func PutText(_ text: String) {
    requestTextImpl(text: text, encoding: "UTF-8", functionName: "PutText", httpVerb: "PUT")
  }

  @objc open func PutTextWithEncoding(_ text: String, _ encoding: String?) {
    requestTextImpl(text: text, encoding: encoding, functionName: "PutTextWithEncoding", httpVerb: "PUT")
  }

  @objc open func PutFile(_ path: String) {
    guard let webProps = capturePropertyValues("PutFile") else {
      return
    }
    performRequest(webProps, nil, path, "PUT")
  }
  
  @objc open func Delete() {
    guard let webProps = capturePropertyValues("Delete") else {
      return
    }
    performRequest(webProps, nil, nil, "Delete")
  }

  fileprivate func requestTextImpl(text: String, encoding: String?, functionName: String, httpVerb: String) {
    guard let webProps: CapturedProperties = capturePropertyValues(functionName) else {
      return
    }
    var requestData: Data?

    if let encoding = encoding, !encoding.isEmpty {
      requestData = stringToEncodedData(encoding, text)
    } else {
      requestData = text.data(using: .utf8)
    }

    performRequest(webProps, requestData, nil, httpVerb)
  }

  fileprivate func performRequest(_ webProps: CapturedProperties, _ postData: Data?, _ postFile: String?, _ httpVerb: String) {
    let session = openSession(webProps, httpVerb)

    guard let url = URL(string: webProps.urlString) else {
      return
    }

    var request = URLRequest(url: url)
    if let postData = postData {
      request.httpMethod = httpVerb
      request.httpBody = postData
    } else if let postFile = postFile {
      request.httpMethod = httpVerb
      request.httpBody = Data(base64Encoded: postFile)
    }
    request.allHTTPHeaderFields = webProps.requestHeaders.mapValues({ (items) -> String in
      return items.joined(separator: ", ")
    })

    let task = session.dataTask(with: request, completionHandler: { (data, response, error) in
      if let response = response {
        let responseType = response.mimeType ?? ""
        let responseCode = (response as! HTTPURLResponse).statusCode
        var responseContent: NSString = ""

        if (self._saveResponse) {
          let path = self.saveResponseContent(response, webProps.responseFileName, responseType, data)

          DispatchQueue.main.async {
            self.GotFile(webProps.urlString as NSString, responseCode: responseCode as NSNumber, responseType: responseType as NSString, fileName: path as NSString)
          }
        } else {
          if let data = data {
            let encodingName = response.textEncodingName ?? "utf8"
            guard let encodingType = self.stringToEncoding[encodingName], let responseContentStr = String(data: data, encoding: encodingType) else {
              self._form?.dispatchErrorOccurredEvent(self, "performRequest",
                  ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.code, encodingName)
              return
            }
            responseContent = responseContentStr as NSString
          }
          DispatchQueue.main.async {
            self.GotText(webProps.urlString as NSString, responseCode: responseCode as NSNumber, responseType: responseType as NSString, responseContent: responseContent)
          }
        }
      } else if let error = error {
        NSLog("Got error during URL fetch: \(error.localizedDescription)")
        if (error as NSError).code == URLError.timedOut.rawValue {
          DispatchQueue.main.async {
            self.TimedOut(webProps.urlString as NSString)
            self._form?.dispatchErrorOccurredEvent(self, httpVerb,
                ErrorMessage.ERROR_WEB_REQUEST_TIMED_OUT.code, webProps.urlString)
          }
        }
      }
    })

    task.resume()
  }

  fileprivate func saveResponseContent(_ response: URLResponse, _ fileName: String, _ responseType: String, _ data: Data?) -> String {
    let filename = (fileName.isEmpty) ? response.suggestedFilename : fileName

    let fileManager = FileManager.default
    let path = NSTemporaryDirectory() + filename!
    fileManager.createFile(atPath: path, contents: data, attributes: nil)

    return path
  }

  fileprivate func openSession(_ webProps: CapturedProperties, _ httpVerb: String) -> URLSession {
    let urlSessionConfiguration = URLSessionConfiguration.default
    urlSessionConfiguration.httpCookieStorage = _cookieStorage
    urlSessionConfiguration.httpShouldSetCookies = _allowCookies
    if webProps.timeout > 0 {
      urlSessionConfiguration.timeoutIntervalForRequest = Double(webProps.timeout) / 1000.0
    }

    urlSessionConfiguration.httpAdditionalHeaders = webProps.requestHeaders

    let urlSession = URLSession.init(configuration: urlSessionConfiguration)

    return urlSession
  }

  fileprivate func saveResponseContent(_ response: URLResponse, _ responseFileName: String, _ responseType: String) {
    if !responseFileName.isEmpty {
      return
    }
  }

  @objc open func UriEncode(_ text: String) -> String {
    return text.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
  }

  fileprivate func processRequestHeaders(_ list: YailList<YailList<NSString>>) throws -> [String: [String]] {
    var requestHeadersDic = [String: [String]]()

    for (index, item) in list.enumerated() {
      if let sublist = item as? YailList<NSString> {
        if sublist.length == 2 {
          guard let fieldName = sublist[1] as? String else {
            continue
          }
          if let multipleFieldValues = sublist[2] as? YailList<NSString> {
            var values = [String]()
            for value in multipleFieldValues {
              values.append(String(describing: value))
            }
            requestHeadersDic[fieldName] = values
          } else if let singleField = sublist[2] as? String {
            //singular non-list item
            requestHeadersDic[fieldName] = [singleField]
          }


        } else {
          // sublist does not contain two elements
          throw InvalidHeadersError(ErrorMessage.ERROR_WEB_REQUEST_HEADER_NOT_TWO_ELEMENTS, index + 1)
        }
      } else {
        // the item isn't a sublist
        throw InvalidHeadersError(ErrorMessage.ERROR_WEB_REQUEST_HEADER_NOT_LIST, index + 1)
      }
    }

    return requestHeadersDic
  }

  @objc open func BuildRequestData(_ list: [AnyObject]) -> String{
    do {
      return try buildRequestData(list)
    } catch (let error as BuildRequestDataException) {
      _form?.dispatchErrorOccurredEvent(self, "RequestHeaders", error.code, error.index)
      return ""
    } catch (let error) {
      NSLog("Unexpected error occurred:", error.localizedDescription)
      return ""
    }
  }

  fileprivate func buildRequestData(_ list: [AnyObject]) throws -> String {
    var data = ""
    var separator = ""
    for (index, item) in list.enumerated() {
      if let sublist = item as? NSArray {
        if sublist.count == 2 {
          let name = (sublist.firstObject as AnyObject).description ?? ""
          let value = (sublist.lastObject as AnyObject).description ?? ""
          data += "\(separator)\(UriEncode(name))=\(UriEncode(value))"
        } else {
          throw BuildRequestDataException(ErrorMessage.ERROR_WEB_BUILD_REQUEST_DATA_NOT_TWO_ELEMENTS, index + 1)
        }
      } else  {
        throw BuildRequestDataException(ErrorMessage.ERROR_WEB_BUILD_REQUEST_DATA_NOT_LIST, index + 1)
      }
      separator = "&"
    }
    return data
  }

  @objc open func HtmlTextDecode(_ htmlText: String) -> String {
    if let text = HTMLEntities.decodeHTMLText(htmlText) {
      return text
    } else {
      _form?.dispatchErrorOccurredEvent(self, "HtmlTextDecode",
          ErrorMessage.ERROR_WEB_HTML_TEXT_DECODE_FAILED.code, htmlText)
      return ""
    }
  }

  @objc open func XMLTextDecode(_ xmlText: String) -> AnyObject {
    do {
      let xml = try XmlToJson.main.parseXML(xmlText)
      return JsonTextDecode(xml)
    } catch let error {
      _form?.dispatchErrorOccurredEvent(self, "XMLTextDecode",
          ErrorMessage.ERROR_WEB_JSON_TEXT_DECODE_FAILED.code, error.localizedDescription)
      return YailList<AnyObject>()
    }
  }

  @objc open func JsonTextDecode(_ jsonString: String) -> AnyObject {
    do {
      return try getYailObjectFromJson(jsonString, false)
    } catch let error {
      _form?.dispatchErrorOccurredEvent(self, "JsonTextDecode",
          ErrorMessage.ERROR_WEB_JSON_TEXT_DECODE_FAILED.code, error.localizedDescription)
      return "" as AnyObject
    }
  }

  fileprivate func capturePropertyValues(_ functionName: String) -> CapturedProperties? {
    do {
      return try CapturedProperties(web: self)
    } catch ErrorMessage.ERROR_WEB_MALFORMED_URL {
      self._form?.dispatchErrorOccurredEvent(self, functionName,
          ErrorMessage.ERROR_WEB_MALFORMED_URL.code, _url)
    } catch (let error as InvalidHeadersError) {
      self._form?.dispatchErrorOccurredEvent(self, functionName, error.code, error.index)
    } catch (let error) {
      NSLog("Unexpected error occurred: ", error.localizedDescription)
    }
    return nil
  }

  @objc open func JsonObjectEncode(_ jsonObject: AnyObject) -> String {
    do {
      return try getJsonRepresentation(jsonObject)
    } catch {
      _form?.dispatchErrorOccurredEvent(self, "JsonObjectEncode",
          ErrorMessage.ERROR_WEB_JSON_TEXT_ENCODE_FAILED)
      print(error)
      return ""
    }
  }

  @objc open func JsonTextDecodeWithDictionaries(_ jsonText: String) -> AnyObject {
    do {
      return try getYailObjectFromJson(jsonText, true)
    } catch {
      _form?.dispatchErrorOccurredEvent(self, "JsonTextDecodeWithDictionaries",
          ErrorMessage.ERROR_WEB_JSON_TEXT_DECODE_FAILED, jsonText)
      return "" as NSString
    }
  }

  @objc open func UriDecode(_ text: String) -> String {
    return text.removingPercentEncoding ?? text
  }

  @objc open func XMLTextDecodeAsDictionary(_ xmlText: String) -> YailDictionary {
    do {
      return try XmlToDictionaries.main.parseXML(xmlText) ?? YailDictionary()
    } catch {
      _form?.dispatchErrorOccurredEvent(self, "XMLTextDecodeAsDictionary",
          .ERROR_WEB_XML_TEXT_DECODE_FAILED, xmlText)
      return YailDictionary()
    }
  }

  // MARK: - Web Events

  @objc open func GotText(_ url: NSString, responseCode: NSNumber, responseType: NSString, responseContent: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "GotText", arguments: url, responseCode, responseType, responseContent)
  }

  @objc open func GotFile(_ url: NSString, responseCode: NSNumber, responseType: NSString, fileName: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "GotFile", arguments: url, responseCode, responseType, fileName)
  }

  @objc open func TimedOut(_ url: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "TimedOut", arguments: url)
  }

  class CapturedProperties {
    let urlString: String
    let url: URL
    let allowCookies: Bool
    let saveResponse: Bool
    let responseFileName: String
    var requestHeaders: [String: [String]] = [String: [String]]()
    let cookies: HTTPCookieStorage
    let timeout: Int32

    init?(web: Web) throws {
      urlString = web._url
      guard let url = URL.init(string: urlString) else {
        throw ErrorMessage.ERROR_WEB_MALFORMED_URL
      }
      self.url = url
      allowCookies = web._allowCookies
      saveResponse = web._saveResponse
      responseFileName = web._responseFileName
      cookies = web._cookieStorage
      requestHeaders = try web.processRequestHeaders(web._requestHeaders)
      timeout = web._timeout
    }
  }

  struct InvalidHeadersError: Error {
    var type: ErrorMessage { return _errorMessage }
    var message: String { return _errorMessage.message }
    var index: Int { return _index }
    var code: Int32 { return _errorMessage.code }

    private var _errorMessage: ErrorMessage
    private var _index: Int

    init(_ errorMessage: ErrorMessage, _ index: Int) {
      _errorMessage = errorMessage
      _index = index
    }
  }

  struct BuildRequestDataException: Error {
    var type: ErrorMessage { return _errorMessage }
    var message: String { return _errorMessage.message }
    var index: Int { return _index }
    var code: Int32 { return _errorMessage.code }

    private var _errorMessage: ErrorMessage
    private var _index: Int

    init(_ errorMessage: ErrorMessage, _ index: Int) {
      _errorMessage = errorMessage
      _index = index
    }
  }
}
