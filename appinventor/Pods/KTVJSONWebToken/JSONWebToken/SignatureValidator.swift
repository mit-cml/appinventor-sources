//
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 18/11/15.
//

import Foundation

public protocol SignatureValidator : JSONWebTokenValidatorType {
    func canVerifyWithSignatureAlgorithm(_ alg : SignatureAlgorithm) -> Bool
    func verify(_ input : Data, signature : Data) -> Bool
}
public enum SignatureValidatorError : Error {
    case algorithmMismatch
    case badInputData
    case signatureMismatch
}
extension SignatureValidator {
    public func validateToken(_ token : JSONWebToken) -> ValidationResult {
        guard self.canVerifyWithSignatureAlgorithm(token.signatureAlgorithm) else {
            return .failure(SignatureValidatorError.algorithmMismatch)
        }
        guard let input = (token.base64Parts.header+"."+token.base64Parts.payload).data(using: String.Encoding.utf8) else {
            return .failure(SignatureValidatorError.badInputData)
        }
        guard self.verify(input, signature: token.decodedDataForPart(.signature))  else {
            return .failure(SignatureValidatorError.signatureMismatch)
        }
        return .success
    }
}
