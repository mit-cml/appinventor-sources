//
//  Signature.swift
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 18/11/15.
//  Copyright © 2015 Antoine Palazzolo. All rights reserved.
//

import Foundation


public enum SignatureAlgorithm {
    public enum HashFunction : Int {
        case sha256 = 256
        case sha384 = 384
        case sha512 = 512
        
        fileprivate var jwtIdentifierSuffix : String {
            switch self {
            case .sha256:
                return "256"
            case .sha384:
                return "384"
            case .sha512:
                return "512"
            }
        }
    }
    
    case none
    case hmac(HashFunction) // HMAC -> HSXXX
    case rsassa_PKCS1(HashFunction) // RSASSA-PKCS1-v1_5 -> RSXXX
    case ecdsa(HashFunction) // ECDSA -> ESXXX
    case rsassa_PSS(HashFunction) //RSASSA-PSS -> PSXXX
    
    public init(name : String) throws {
        guard name.count > 0 else {throw JSONWebToken.Error.invalidSignatureAlgorithm(name)}
        guard name.lowercased() != "none" else { self = .none; return }
        
        let prefixIndex = name.index(name.startIndex, offsetBy: 2)
        let prefix = String(name[..<prefixIndex])
        let suffix = String(name[prefixIndex...])
        
        let hashFunction : HashFunction = try {
            switch suffix {
            case HashFunction.sha256.jwtIdentifierSuffix:
                return .sha256
            case HashFunction.sha384.jwtIdentifierSuffix:
                return .sha384
            case HashFunction.sha512.jwtIdentifierSuffix:
                return .sha512
            default:
                throw JSONWebToken.Error.invalidSignatureAlgorithm(name)
            }
            }()
        switch prefix {
        case "HS" : self = .hmac(hashFunction)
        case "RS" : self = .rsassa_PKCS1(hashFunction)
        case "ES" : self = .ecdsa(hashFunction)
        case "PS" : self = .rsassa_PSS(hashFunction)
        default : throw JSONWebToken.Error.invalidSignatureAlgorithm(name)
        }
    }
    var jwtIdentifier : String {
        switch self {
        case .none:
            return "none"
        case .hmac(let f):
            return "HS"+f.jwtIdentifierSuffix
        case .rsassa_PKCS1(let f):
            return "RS"+f.jwtIdentifierSuffix
        case .rsassa_PSS(let f):
            return "PS"+f.jwtIdentifierSuffix
        case .ecdsa(let f):
            return "ES"+f.jwtIdentifierSuffix
        }
    }
}
