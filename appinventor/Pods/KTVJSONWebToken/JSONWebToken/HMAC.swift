//
//  HMAC.swift
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 18/11/15.
//

import Foundation


public struct HMACSignature : SignatureValidator,TokenSigner {
    let secret : Data
    let hashFunction : SignatureAlgorithm.HashFunction
    public init(secret : Data,hashFunction : SignatureAlgorithm.HashFunction) {
        self.secret = secret
        self.hashFunction = hashFunction
    }
    public func canVerifyWithSignatureAlgorithm(_ alg : SignatureAlgorithm) -> Bool {
        if case SignatureAlgorithm.hmac(self.hashFunction) = alg {
            return true
        }
        return false
    }
    public func verify(_ input : Data, signature : Data) -> Bool {
        return (input as NSData).jwt_hmacSignature(withSHAHashFuctionSize: self.hashFunction.rawValue, secret: secret) == signature
    }
    
    public var signatureAlgorithm : SignatureAlgorithm {
        return .hmac(self.hashFunction)
    }
    public func sign(_ input : Data) throws -> Data {
        return (input as NSData).jwt_hmacSignature(withSHAHashFuctionSize: self.hashFunction.rawValue, secret: secret)
    }
}
