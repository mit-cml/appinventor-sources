// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

open class Web: NonvisibleComponent {
  fileprivate var _url = ""
  fileprivate var _requestHeaders = YailList()
  fileprivate var _allowCookies = false
  fileprivate var _saveResponse = false
  fileprivate var _responseFileName = ""
  fileprivate var _cookieStorage = HTTPCookieStorage()
  
  fileprivate let stringToEncoding: [String: String.Encoding] =
    ["uft8": .utf8, "utf-8": .utf8, "utf16": .utf16, "utf-16": .utf16, "utf32": .utf32,
     "utf-32": .utf32, "iso2022jp": .iso2022JP, "iso-2022-jp": .iso2022JP, "iso-8859-1": .isoLatin1,
     "iso88591": .isoLatin1, "iso-8859-2": .isoLatin2, "iso-8859-2": .isoLatin2, "ascii": .ascii]
  
  private func stringToEncodedData(_ encoding: String, _ text: String) -> Data? {
    guard let encodingType = stringToEncoding[encoding.lowercased()] else {
      self._form.dispatchErrorOccurredEvent(self, "stringToEncodedData", ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.code, ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.message, encoding)
      return nil
    }
    return text.data(using: encodingType)
  }
  
  open var Url: String {
    get {
      return _url
    }
    set(url) {
      _url = url
    }
  }
  
  open var RequestHeaders: YailList {
    get {
      return _requestHeaders
    }
    set(list) {
      if validateRequestHeaders(list) {
        _requestHeaders = list
      }
    }
  }

  open var AllowCookies: Bool {
    get {
      return _allowCookies
    }
    set(allowCookies) {
      _allowCookies = allowCookies
    }
  }
  
  open var SaveResponse: Bool {
    get {
      return _saveResponse
    }
    set(saveResponse) {
      _saveResponse = saveResponse
    }
  }
  
  open var ResponseFileName: String {
    get {
      return _responseFileName
    }
    set(responseFileName) {
      _responseFileName = responseFileName
    }
  }
  
  open func ClearCookies() {
    _cookieStorage = HTTPCookieStorage.init()
  }
  
  fileprivate func validateRequestHeaders(_ list: YailList) -> Bool {
    do {
      _ = try processRequestHeaders(list)
      return true
    } catch (let error as InvalidHeadersError) {
      _form.dispatchErrorOccurredEvent(self, "RequestHeaders", error.code, error.message, error.index)
    } catch (let error) {
      NSLog("Unexpected error occurred:", error.localizedDescription)
    }
    return false
  }
  
  open func Get() {
    guard let webProps = capturePropertyValues("Get") else {
      return
    }
    performRequest(webProps, nil, nil, "GET")
  }
  
  open func PostTextWithEncoding(_ text: String, _ encoding: String?) {
    requestTextImpl(text: text, encoding: encoding, functionName: "PostTextWithEncoding", httpVerb: "POST")
  }
  
  open func PostFile(_ path: String) {
    guard let webProps = capturePropertyValues("PostFile") else {
      return
    }
    performRequest(webProps, nil, path, "POST")
  }
  
  open func PutText(_ text: String) {
    requestTextImpl(text: text, encoding: "UTF-8", functionName: "PutText", httpVerb: "PUT")
  }
  
  open func PutTextWithEncoding(_ text: String, _ encoding: String?) {
    requestTextImpl(text: text, encoding: encoding, functionName: "PutTextWithEncoding", httpVerb: "PUT")
  }
  
  open func PutFile(_ path: String) {
    guard let webProps = capturePropertyValues("PutFile") else {
      return
    }
    performRequest(webProps, nil, path, "PUT")
  }
  
  open func Delete() {
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
    
    let task = session.dataTask(with: request, completionHandler: { (data, response, error) in
      
      let responseType = response?.mimeType ?? ""
      let responseCode = (response as! HTTPURLResponse).statusCode
      var responseContent: NSString = ""
      
      if (self._saveResponse) {
        let path = self.saveResponseContent(response!, webProps.responseFileName, responseType, data)
        
        self.GotFile(webProps.urlString as NSString, responseCode: responseCode as NSNumber, responseType: responseType as NSString, fileName: path as NSString)
      } else {
        if let data = data {
          if let encodingName = response?.textEncodingName {
            guard let encodingType = self.stringToEncoding[encodingName], let responseContentStr = String(data: data, encoding: encodingType) else {
              self._form.dispatchErrorOccurredEvent(self, "performRequest", ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.code, ErrorMessage.ERROR_WEB_UNSUPPORTED_ENCODING.message, encodingName)
              return
            }
            responseContent = responseContentStr as NSString
          }
        }
        self.GotText(webProps.urlString as NSString, responseCode: responseCode as NSNumber, responseType: responseType as NSString, responseContent: responseContent)
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
    
    if (httpVerb == "PUT" || httpVerb == "DELETE") {
      urlSessionConfiguration.httpAdditionalHeaders = webProps.requestHeaders
    }
    
    let urlSession = URLSession.init(configuration: urlSessionConfiguration)
    
    return urlSession
  }
  
  fileprivate func saveResponseContent(_ response: URLResponse, _ responseFileName: String, _ responseType: String) {
    if !responseFileName.isEmpty {
      return
    }
  }
  
  fileprivate func processRequestHeaders(_ list: YailList) throws -> [String: [String]] {
    var requestHeadersDic = [String: [String]]()
    
    for (index, item) in list.enumerated() {
      if item is YailList {
        let sublist = item as! YailList
        if sublist.count == 2 {
          let fieldName = String(describing: sublist.firstObject)
          let fieldValues = sublist.lastObject
          
          var values = [String]()
          
          if fieldValues is YailList {
            let multipleFieldValues = fieldValues as! YailList
            for value in multipleFieldValues {
              values.append(String(describing: value))
            }
          } else {
            //singular non-list item
            let singleField = fieldValues
            values.append(String(describing: singleField))
          }
          
          requestHeadersDic[fieldName] = values
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
  
  fileprivate func capturePropertyValues(_ functionName: String) -> CapturedProperties? {
    do {
      return try CapturedProperties(web: self)
    } catch ErrorMessage.ERROR_WEB_MALFORMED_URL {
      self._form.dispatchErrorOccurredEvent(self, functionName, ErrorMessage.ERROR_WEB_MALFORMED_URL.code, ErrorMessage.ERROR_WEB_MALFORMED_URL.message, _url)
    } catch (let error as InvalidHeadersError) {
      self._form.dispatchErrorOccurredEvent(self, functionName, error.code, error.message, error.index)
    } catch (let error) {
      NSLog("Unexpected error occurred: ", error.localizedDescription)
    }
    return nil
  }
  
  open func GotText(_ url: NSString, responseCode: NSNumber, responseType: NSString, responseContent: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "GotText", arguments: url, responseCode, responseType, responseContent)
  }
  
  open func GotFile(_ url: NSString, responseCode: NSNumber, responseType: NSString, fileName: NSString) {
    EventDispatcher.dispatchEvent(of: self, called: "GotFile", arguments: url, responseCode, responseType, fileName)
  }
  
  class CapturedProperties {
    let urlString: String
    let url: URL
    let allowCookies: Bool
    let saveResponse: Bool
    let responseFileName: String
    var requestHeaders: [String: [String]] = [String: [String]]()
    let cookies: HTTPCookieStorage
    
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
}
