// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import KTVJSONWebToken
import Alamofire

open class FusiontablesControl: NonvisibleComponent {
  fileprivate var _apiKey = ""
  fileprivate var _query = "show tables"
  fileprivate var _keyPath = ""
  fileprivate var _isServiceAuth = false
  fileprivate var _serviceEmail = ""
  fileprivate var _expirationDate = Date()
  fileprivate var _authToken = ""

  fileprivate let tablesURL = "https://www.googleapis.com/fusiontables/v2/query"
  fileprivate let createURL = "https://www.googleapis.com/fusiontables/v2/tables"

  @objc open var ApiKey: String {
    get {
      return _apiKey
    }
    set (newKey) {
      _apiKey = newKey
    }
  }

  @objc open var KeyFile: String {
    get {
      return _keyPath
    }
    set (newPath) {
      _keyPath = newPath
    }
  }

  @objc open var Query: String {
    get {
      return _query
    }
    set (newQuery) {
      _query = newQuery
    }
  }

  @objc open var ServiceAccountEmail: String {
    get {
      return _serviceEmail
    }
    set (newEmail) {
      _serviceEmail = newEmail
    }
  }

  @objc open var UseServiceAuthentication: Bool {
    get {
      return _isServiceAuth
    }
    set (shouldUse) {
      _isServiceAuth = shouldUse
    }
  }

  @objc open func DoQuery() {
    SendQuery()
  }

  @objc open func ForgetLogin() {
    _authToken = ""
  }

  @objc open func GetRows(_ tableId: String, _ columns: String) {
    _query = "SELECT \(columns) FROM \(tableId)"
    SendQuery()
  }

  @objc open func GetRowsWithConditions(_ tableId: String, _ columns: String, _ conditions: String) {
    _query = "SELECT \(columns) FROM \(tableId) WHERE \(conditions)"
    SendQuery()
  }

  @objc open func InsertRow(_ tableId: String, _ columns: String, values: String) {
    _query = "INSERT INTO \(tableId) (\(columns)) VALUES (\(values))"
    SendQuery()
  }

  @objc open func SendQuery() {
    if _isServiceAuth {
      if Date() > _expirationDate || _authToken == "" {
        let parameters = [
          "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
          "assertion": getJWT() ?? ""
        ]
        _expirationDate = Date()
        let tempQuery = _query
        Alamofire.request("https://www.googleapis.com/oauth2/v4/token", method: .post, parameters: parameters).responseJSON { response in
          if let code = response.response?.statusCode, let result = response.result.value as? NSDictionary {
            switch code {
            case 200..<300:
              self._authToken = result["access_token"] as? String ?? ""
              self._expirationDate = self._expirationDate + (TimeInterval(result["expires_in"] as? Int32 ?? 0))
              self.doSendQuery(tempQuery)
            default:
              self.sendAuthError("Missing or incorrect key file")
            }
          } else {
            self.sendQueryError(tempQuery, "Empty response received")
          }
        }
      } else {
        doSendQuery(_query)
      }
    } else {
      doSendQuery(_query)
    }
  }

  @objc open func GotResult(_ result: String) {
    EventDispatcher.dispatchEvent(of: self, called: "GotResult", arguments: result as NSString)
  }

  fileprivate func getJWT() -> String? {
    do {
      var payload = JSONWebToken.Payload()
      payload.issuer = _serviceEmail
      payload.audience = ["https://www.googleapis.com/oauth2/v4/token"]
      payload.expiration = Date().addingTimeInterval(3600)
      payload.issuedAt = Date()
      payload["scope"] = "https://www.googleapis.com/auth/fusiontables"
      let path = URL(fileURLWithPath: AssetManager.shared.pathForPublicAsset(_keyPath))
      if try path.checkResourceIsReachable() {
        let data = try Data(contentsOf: path)
        let identity : (publicKey : RSAKey, privateKey : RSAKey) = try RSAKey.keysFromPkcs12Identity(data, passphrase : "notasecret")
        let signer = RSAPKCS1Signer(hashFunction: .sha256, key: identity.privateKey)
        let jwt = try JSONWebToken(payload: payload, signer: signer)
        return jwt.rawString
      } else {
        sendAuthError("key file is missing or cannot be reached")
      }
    } catch let error {
      sendAuthError(error.localizedDescription)
    }
    return nil
  }

  fileprivate func doSendQuery(_ query: String) {
    var parameters: Parameters = ["sql": query, "alt": "csv"]
    var headers: HTTPHeaders?
    if _isServiceAuth {
      headers = [ "Authorization": "Bearer \(_authToken)" ]
    } else {
      parameters["key"] = _apiKey
    }

    if _query.lowercased().contains("create table") {
      processCreateTableSQL(query)
    } else {
      Alamofire.request(tablesURL, method: .post, parameters: parameters,encoding: URLEncoding.queryString, headers: headers).responseString { response in
        if let value = response.result.value {
          if let resp = response.response, 200..<300 ~= resp.statusCode {
            self.GotResult(self.toCSV(value))
          } else {
            print("query error 2: \(query)")
            self._form?.dispatchErrorOccurredEvent(self, "SendQuery",
                ErrorMessage.FUSION_TABLES_QUERY_ERROR.code,
                ErrorMessage.FUSION_TABLES_QUERY_ERROR.message, query, value)
          }
        } else {
          print("query error: \(query)")
          self.sendQueryError(query, "Empty response received")
        }
      }
    }
  }

  fileprivate func processCreateTableSQL(_ query: String) {
    var columnNames = [[String:String]]()
    let header: HTTPHeaders
    let params: [String: String]

    if _isServiceAuth {
      header = ["Authorization": "Bearer \(_authToken)", "Content-Type": "application/json"]
      params = [:]
    } else {
      header = [:]
      params = ["key": _apiKey]
    }

    var urlComponent = URLComponents(string: createURL)!
    urlComponent.queryItems = params.map({URLQueryItem(name: $0.key, value: $0.value)})
    var request = URLRequest(url: urlComponent.url!)
    request.httpMethod = "POST"
    
    if let start = query.firstIndex(of: "("), let end = query.firstIndex(of: ")"), start < end {
      let columns = String(query[query.index(after: start)..<end]).split(", ")
      for i in 0..<columns.count {
        let item = columns[i].split(":")
        if item.count > 1 {
          columnNames.append(["name": item[0], "type": item[1].trimmingCharacters(in: .whitespaces)])
        }
      }
      let parameters: Parameters = [
        "name": query[query.index(query.startIndex, offsetBy: "CREATE TABLE".count + 2)..<(query.firstIndex(of: "(") ?? query.endIndex)].trimmingCharacters(in: .whitespaces),
        "isExportable": true,
        "columns": columnNames
      ]
      do {
        request.httpBody = try JSONSerialization.data(withJSONObject: parameters)
        request.allHTTPHeaderFields = header

        Alamofire.request(request).responseJSON { response in
          if let value = response.result.value, let json = value as? NSDictionary {
            if let resp = response.response, 200..<300 ~= resp.statusCode {
              self.GotResult("\"tableId\",\"\(json["tableId"] ?? "")\"")
            } else {
              if let error = json["error"] as? NSDictionary, let message = error["message"] as? String {
                self.sendQueryError(query, message)
              } else {
                self.sendQueryError(query, "An error occured when attempting to create a table.")
              }
            }
          } else {
            self.sendQueryError(query, "Empty response returned")
          }
        }
      } catch let error {
        self.sendQueryError(query, error.localizedDescription)
      }
    } else {
      self.sendQueryError(query, "Error processing query: incorrect syntax for create table.")
    }
  }

  fileprivate func toCSV(_ value: String) -> String {
    var string = "\""
    var inString = false
    value.forEach({ character in
      switch character {
      case "\n", ",":
        if !inString {
          string.append("\"\(character)\"")
        }
      case "\"":
        inString = !inString
      default:
        string.append(character)
      }
    })
    return string
  }

  fileprivate func sendAuthError(_ message: String) {
    _form?.dispatchErrorOccurredEvent(self, "SendQuery",
        ErrorMessage.FUSION_TABLES_AUTH_ERROR.code,
        ErrorMessage.FUSION_TABLES_AUTH_ERROR.message, message)
  }

  fileprivate func sendQueryError(_ query: String, _ message: String) {
    _form?.dispatchErrorOccurredEvent(self, "SendQuery",
        ErrorMessage.FUSION_TABLES_QUERY_ERROR.code,
        ErrorMessage.FUSION_TABLES_QUERY_ERROR.message, query, message)
  }
}
