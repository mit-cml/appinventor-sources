//
//  JSONWebToken.swift
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 17/11/15.
//

import Foundation

public struct JSONWebToken {
    
    public indirect enum Error : Swift.Error {
        case badTokenStructure
        case cannotDecodeBase64Part(JSONWebToken.Part,String)
        case invalidJSON(JSONWebToken.Part,Swift.Error)
        case invalidJSONStructure(JSONWebToken.Part)
        case typeIsNotAJSONWebToken
        case invalidSignatureAlgorithm(String)
        case missingSignatureAlgorithm

    }
    public enum Part {
        case header
        case payload
        case signature
    }
    
    public struct Payload {
        public enum RegisteredClaim : String {
            case Issuer = "iss"
            case Subject = "sub"
            case Audience = "aud"
            case ExpirationTime = "exp"
            case NotBefore = "nbf"
            case IssuedAt = "iat"
            case JWTIdentifier = "jti"
        }
        
        var jsonPayload : [String : Any]
        fileprivate init(jsonPayload : [String : Any]) {
            self.jsonPayload = jsonPayload
        }
        public init() {
            jsonPayload = Dictionary()
        }
        public subscript(key : String) -> Any? {
            get {
                let result = jsonPayload[key]
                switch result {
                case .some(let value) where value is NSNull:
                    return nil
                case .some(_):
                    return result
                case .none:
                    return nil
                }
            }
            set {
                if newValue == nil || newValue is NSNull {
                    jsonPayload.removeValue(forKey: key)
                } else {
                    jsonPayload[key] = newValue
                }
            }
        }
        fileprivate subscript(registeredClaim : RegisteredClaim) -> Any? {
            get {
                return self[registeredClaim.rawValue]
            }
            set {
                return self[registeredClaim.rawValue] = newValue
            }
        }
        public var issuer : String? {
            get {
                return (try? self[.Issuer].map(RegisteredClaimValidator.issuer.transform)) ?? nil
            }
            set {
                self[.Issuer] = newValue
            }
        }
        public var subject : String? {
            get {
                return (try? self[.Subject].map(RegisteredClaimValidator.subject.transform)) ?? nil
            }
            set {
                self[.Subject] = newValue
            }
        }
        public var audience : [String] {
            get {
                return (try? self[.Audience].map(RegisteredClaimValidator.audience.transform) ?? []) ?? []
            }
            set {
                switch newValue.count {
                case 0:
                    self[.Audience] = nil
                case 1:
                    self[.Audience] = newValue[0]
                default:
                    self[.Audience] = newValue
                }
            }
        }
        fileprivate static func jsonClaimValueFromDate(_ date : Date?) -> NSNumber? {
            return date.map { NSNumber(value: Int64($0.timeIntervalSince1970)) }
        }
        public var expiration : Date? {
            get {
                return (try? self[.ExpirationTime].map(RegisteredClaimValidator.expiration.transform)) ?? nil
            }
            set {
                self[.ExpirationTime] = Payload.jsonClaimValueFromDate(newValue)
            }
        }
        
        public var notBefore : Date? {
            get {
                return (try? self[.NotBefore].map(RegisteredClaimValidator.notBefore.transform)) ?? nil
            }
            set {
                self[.NotBefore] = Payload.jsonClaimValueFromDate(newValue)
            }
        }
        
        public var issuedAt : Date? {
            get {
                return (try? self[.IssuedAt].map(RegisteredClaimValidator.issuedAt.transform)) ?? nil
            }
            set {
                self[.IssuedAt] = Payload.jsonClaimValueFromDate(newValue)
            }
        }
        public var jwtIdentifier : String? {
            get {
                return (try? self[.JWTIdentifier].map(RegisteredClaimValidator.jwtIdentifier.transform)) ?? nil
            }
            set {
                self[.JWTIdentifier] = newValue
            }
        }
    }
    
    
    public let signatureAlgorithm : SignatureAlgorithm
    public let payload : Payload
    let base64Parts : (header : String,payload : String, signature : String)
    
    public init(string input: String) throws {

        let parts = input.components(separatedBy: ".")
        guard parts.count == 3 else { throw Error.badTokenStructure }
        
        self.base64Parts = (parts[0],parts[1],parts[2])
        
        guard let headerData = Data(jwt_base64URLEncodedString: base64Parts.header, options: []) else {
            throw Error.cannotDecodeBase64Part(.header,base64Parts.header)
        }
        guard let payloadData = Data(jwt_base64URLEncodedString: base64Parts.payload, options: []) else {
            throw Error.cannotDecodeBase64Part(.payload,base64Parts.payload)
        }
        guard Data(jwt_base64URLEncodedString: base64Parts.signature, options: []) != nil else {
            throw Error.cannotDecodeBase64Part(.signature,base64Parts.signature)
        }
        
        let jsonHeader = try JSONWebToken.jwtJSONFromData(headerData,part: .header)
        
        guard (jsonHeader["typ"] as? String).map({$0.uppercased() == "JWT"}) ?? true else {
            throw Error.typeIsNotAJSONWebToken
        }
        guard let signatureAlgorithm = try (jsonHeader["alg"] as? String).map(SignatureAlgorithm.init) else {
            throw Error.missingSignatureAlgorithm
        }
        self.signatureAlgorithm = signatureAlgorithm
        
        let jsonPayload = try JSONWebToken.jwtJSONFromData(payloadData,part: .payload)

        self.payload = Payload(jsonPayload: jsonPayload)
    }
    fileprivate static func jwtJSONFromData(_ data : Data, part : JSONWebToken.Part) throws -> [String : Any] {
        let json : Any
        do {
            json = try JSONSerialization.jsonObject(with: data, options: [])
        } catch {
            throw Error.invalidJSON(part,error)
        }
        guard let result = json as? [String : Any] else {
            throw Error.invalidJSONStructure(part)
        }
        return result
    }
    
    public init(payload : Payload, signer : TokenSigner? = nil) throws {
        self.signatureAlgorithm = signer?.signatureAlgorithm ?? SignatureAlgorithm.none
        self.payload = payload
        
        let header = ["alg" : self.signatureAlgorithm.jwtIdentifier , "typ" : "JWT"]
        let headerBase64 = try JSONSerialization.data(withJSONObject: header, options: []).jwt_base64URLEncodedStringWithOptions([])
        let payloadBase64 = try JSONSerialization.data(withJSONObject: payload.jsonPayload, options: []).jwt_base64URLEncodedStringWithOptions([])
        
        let signatureInput = headerBase64 + "." + payloadBase64
        
        let signature = try signer.map {
            try $0.sign(signatureInput.data(using: String.Encoding.utf8)!)
        } ?? Data()
        
        let signatureBase64 = signature.jwt_base64URLEncodedStringWithOptions([])
        
        self.base64Parts = (headerBase64,payloadBase64,signatureBase64)
    }
    
    
    public func decodedDataForPart(_ part : Part) -> Data {
        switch part {
        case .header:
            return Data(jwt_base64URLEncodedString: base64Parts.header, options: [])!
        case .payload:
            return Data(jwt_base64URLEncodedString: base64Parts.payload, options: [])!
        case .signature:
            return Data(jwt_base64URLEncodedString: base64Parts.signature, options: [])!
        }
    }
    
    public var rawString : String {
        return "\(base64Parts.header).\(base64Parts.payload).\(base64Parts.signature)"
    }
    public var rawData : Data {
        return self.rawString.data(using: String.Encoding.utf8)!
    }
}
