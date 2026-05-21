//
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 20/11/15.
//

import Foundation


public struct ClaimValidatorError : Error {
    public let message : String
    public init(message : String) {
        self.message = message
    }
}
public func ClaimTransformString(_ value : Any) throws -> String {
    if let result = value as? String {
        return result
    } else {
        throw ClaimValidatorError(message: "\(value) is not a String type value")
    }
}
public func ClaimTransformDate(_ value : Any) throws -> Date {
    return try Date(timeIntervalSince1970: ClaimTransformNumber(value).doubleValue)
}
public func ClaimTransformNumber(_ value : Any) throws -> NSNumber {
    if let numberValue = value as? NSNumber {
        return numberValue
    } else {
        throw ClaimValidatorError(message: "\(value) is not a Number type value")
    }
}
public func ClaimTransformArray<U>(_ elementTransform : (Any) throws -> U, value : Any) throws -> [U] {
    if let array = value as? NSArray {
        return try array.map(elementTransform)
    } else {
        throw ClaimValidatorError(message: "\(value) is not an Array type value")
    }
}
public struct ClaimValidator<T> : JSONWebTokenValidatorType {
    fileprivate var isOptional : Bool = false
    fileprivate var validator : (T) -> ValidationResult = {_ in return .success}

    public let key : String
    public let transform : (Any) throws -> T
    
    public init(key : String, transform : @escaping (Any) throws -> T) {
        self.key = key
        self.transform = transform
    }

    public init(claim : JSONWebToken.Payload.RegisteredClaim, transform : @escaping (Any) throws -> T) {
        self.init(key : claim.rawValue,transform : transform)
    }
    
    public func withValidator(_ validator : @escaping (T) -> ValidationResult) -> ClaimValidator<T> {
        var result = self
        result.validator = { input in
            let validationResult = self.validator(input)
            guard case ValidationResult.success = validationResult else {
                return validationResult
            }
            return validator(input)
        }
        return result
    }
    public func withValidator(_ validator : @escaping (T) -> Bool) -> ClaimValidator<T> {
        return self.withValidator {
            return validator($0) ? .success : .failure(ClaimValidatorError(message: "custom validation failed for key \(self.key)"))
        }
    }
    
    
    public var optional : ClaimValidator<T> {
        var result = self
        result.isOptional = true
        return result
    }
    
    
    public func validateToken(_ token : JSONWebToken) -> ValidationResult {
        guard let initialValue = token.payload[self.key] else {
            if self.isOptional {
                return .success
            } else {
                return .failure(ClaimValidatorError(message: "missing value for claim with key \(self.key)"))
            }
        }
        do {
            return try self.validator(self.transform(initialValue))
        } catch {
            return .failure(error)
        }
    }
}
public struct RegisteredClaimValidator {
 
    public static let issuer = ClaimValidator(claim: .Issuer, transform: ClaimTransformString)
    public static let subject = ClaimValidator(claim: .Subject, transform:  ClaimTransformString)
    public static let audience = ClaimValidator(claim: .Audience, transform: { value throws -> [String] in
        if let singleAudience = try? ClaimTransformString(value) {
            return [singleAudience]
        } else if let multiple = try? ClaimTransformArray(ClaimTransformString,value : value) {
            return multiple
        } else {
            throw ClaimValidatorError(message: "audience value \(value) is not an array or string value")
        }
    })
    
    public static let expiration = ClaimValidator(claim: .ExpirationTime, transform:  ClaimTransformDate).withValidator { date -> ValidationResult in
        if date.timeIntervalSinceNow >= 0.0 {
            return .success
        } else {
            return .failure(ClaimValidatorError(message: "token is expired"))
        }
    }
    public static let notBefore = ClaimValidator(claim: .NotBefore, transform: ClaimTransformDate).withValidator { date -> ValidationResult in
        if date.timeIntervalSinceNow <= 0.0 {
            return .success
        } else {
            return .failure(ClaimValidatorError(message: "token cannot be used before \(date)"))
        }
    }
    public static let issuedAt = ClaimValidator(claim: .IssuedAt, transform: ClaimTransformDate)
    public static let jwtIdentifier = ClaimValidator(claim: .JWTIdentifier, transform: ClaimTransformString)
    
}



