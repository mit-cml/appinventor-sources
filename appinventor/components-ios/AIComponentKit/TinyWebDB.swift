// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Alamofire

open class TinyWebDB: NonvisibleComponent {
    
    fileprivate let _storeValueCommand: String = "storeavalue"
    fileprivate let _getValueCommand: String = "getvalue"
    fileprivate let _tagParameter: String = "tag"
    fileprivate let _valueParameter: String = "value"
    fileprivate var _serviceURL: String = "http://tinywebdb.appinventor.mit.edu/"
    
    // MARK: TinyWebDB Properties
    open var ServiceURL: String {
        get {
            return _serviceURL
        }
        set(url) {
            _serviceURL = url
        }
    }
    
    // MARK: TinyWebDB Methods
    open func StoreValue(_ tag: String, _ valueToStore: AnyObject) {
        // post a store value command
        do {
            let parameters: Parameters = [_tagParameter: tag, _valueParameter: try getJsonRepresentation(valueToStore)]
            Alamofire.request(_serviceURL + _storeValueCommand, method:.post, parameters: parameters, encoding: URLEncoding.default).validate().responseJSON { response in
                switch response.result {
                case .success:
                    self.ValueStored()
                case .failure(let error):
                    self.WebServiceError(error.localizedDescription)
                }
            }
        } catch {
            NSLog("Unable to serialize valueToStore")
        }
    }
    
    open func GetValue(_ tag: String) {
        // get value for a specific tag
        let parameters: Parameters = [_tagParameter: tag]
        Alamofire.request(_serviceURL + _getValueCommand, method: .post, parameters: parameters, encoding: URLEncoding.default).validate().responseJSON { response in
            switch response.result {
            case .success(let responseValue):
                do {
                    if let responseArray = responseValue as? [String] {
                        let tag = responseArray[1]
                        if let value = try getObjectFromJson("\"" + responseArray[2] + "\"") {
                            self.GotValue(tag, value)
                        }
                    }
                } catch {
                    NSLog("Unable to serialize JSON value to object")
                }
            case .failure(let error):
                print(error)
                self.WebServiceError(error.localizedDescription)
            }
        }
    }
    
    // MARK: TinyWebDB Events
    open func ValueStored() {
        EventDispatcher.dispatchEvent(of: self, called: "ValueStored")
    }
    
    open func GotValue(_ tagFromWebDB: String, _ valueFromWebDB: AnyObject) {
        EventDispatcher.dispatchEvent(of: self, called: "GotValue", arguments: tagFromWebDB as NSString, valueFromWebDB)
    }
    
    open func WebServiceError(_ message: String) {
        EventDispatcher.dispatchEvent(of: self, called: "WebServiceError", arguments: message as NSString)
    }
}
