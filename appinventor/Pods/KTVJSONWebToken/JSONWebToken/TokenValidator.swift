//
//  TokenValidator.swift
//
//  Created by Antoine Palazzolo on 23/11/15.
//

import Foundation

public enum ValidationResult {
    case success
    case failure(Error)
    
    public var isValid : Bool {
        if case .success = self {
            return true
        }
        return false
    }
}
public protocol JSONWebTokenValidatorType {
    func validateToken(_ token : JSONWebToken) -> ValidationResult
}
public struct JSONWebTokenValidator : JSONWebTokenValidatorType  {
    fileprivate let validator : (_ token : JSONWebToken) -> ValidationResult
    
    public func validateToken(_ token : JSONWebToken) -> ValidationResult {
        return self.validator(token)
    }
}

public struct CombinedValidatorError : Error {
    public let errors : [Error]
}

public func &(lhs : JSONWebTokenValidatorType, rhs : JSONWebTokenValidatorType) -> JSONWebTokenValidatorType {
    let and = { (token : JSONWebToken) -> ValidationResult in
        let errors = [lhs,rhs].map{ $0.validateToken(token) }.map { validation -> Error? in
            if case ValidationResult.failure(let error) = validation {
                return Optional.some(error)
            } else {
                return nil
            }
            }.compactMap {$0}
        return errors.count > 0 ? .failure(CombinedValidatorError(errors: errors)) : .success
    }
    return JSONWebTokenValidator(validator: and)
    
}

public func |(lhs : JSONWebTokenValidatorType, rhs : JSONWebTokenValidatorType) -> JSONWebTokenValidatorType {
    let or = { (token : JSONWebToken) -> ValidationResult in
        var errors = [Error]()
        for validator in [lhs,rhs] {
            switch validator.validateToken(token) {
            case .success:
                return .success
            case .failure(let error):
                errors.append(error)
            }
        }
        return .failure(CombinedValidatorError(errors: errors))
        
    }
    return JSONWebTokenValidator(validator: or)
}
