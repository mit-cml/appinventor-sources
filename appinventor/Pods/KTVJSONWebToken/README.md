# JSONWebToken
Swift library for decoding, validating, signing and verifying JWT

# Features

- Verify and sign :
	- HMAC `HS256` `HS384` `HS512`
	- RSASSA-PKCS1-v1_5 `RS256` `RS384` `RS384`
- Validate (optionally) all [registered claims](https://tools.ietf.org/html/rfc7519#section-4.1) :
	- Issuer `iss`
	- Subject `sub`
	- Audience `aud`
	- Expiration Time `exp`
	- Not Before `nbf`
	- Issued At `iat`
	- JWT ID `jti`
- No external dependencies : **CommonCrypto** and **Security** framework are used for signing and verifying 
- Extensible : add your own claim validator and sign operations

[![Carthage compatible](https://img.shields.io/badge/Carthage-compatible-4BC51D.svg?style=flat)](https://github.com/Carthage/Carthage)

# Usage

## Decode & Validation

```swift
import JSONWebToken

let rawJWT : String
let jwt : JSONWebToken = try JSONWebToken(string : rawJWT)

//create the validator by combining other validators with the & or | operator
let validator = RegisteredClaimValidator.expiration & 
				RegisteredClaimValidator.notBefore.optional &
				HMACSignature(secret: "secret".dataUsingEncoding(NSUTF8StringEncoding)!, hashFunction: .SHA256)
/*
- not expired
- can be used now (optional : a jwt without nbf will be valid)
- signed with HS256 and the key "secret"
*/
let validationResult = validator.validateToken(jwt)
guard case ValidationResult.Success = validationResult else { return }

//use the token and access the payload
let issuer : String? = jwt.payload.issuer
let customClaim = jwt.payload["customClaim"] as? String
```
## Sign

```swift
import JSONWebToken

//build the payload
var payload = JSONWebToken.Payload()
payload.issuer = "http://kreactive.com"
payload.subject = "antoine"            
payload.audience = ["com.kreactive.app"]
payload.expiration = NSDate().dateByAddingTimeInterval(300)
payload.notBefore = NSDate()
payload.issuedAt = NSDate()
payload.jwtIdentifier = NSUUID().UUIDString
payload["customClaim"] = "customClaim"

//use HS256 to sign the token
let signer = HMACSignature(secret: "secret".dataUsingEncoding(NSUTF8StringEncoding)!, hashFunction: .SHA256) 

//build the token, signer is optional
let jwt = try JSONWebToken(payload : payload, signer : signer)
let rawJWT : String = jwt.rawString
```

## RSASSA-PKCS1-v1_5 Signature

#### Keys
Keys are represented by the `RSAKey` struct, wrapping a `SecKeyRef`.
The preferred way of importing **public keys** is to use a `DER-encoded X.509` certificate (.cer), and for **private keys** a `PKCS#12` (.p12) identity. 
It's also possible to import raw representation of keys (X509, public pem, modulus/exponent ...) by using a keychain item import side effect. 
```swift
let certificateData : NSData = //DER-encoded X.509 certificate
let publicKey : RSAKey = try RSAKey(certificateData : certificateData)
```

```swift
let p12Data : NSData //PKCS #12–formatted identity data
let identity : (publicKey : RSAKey, privateKey : RSAKey) = try RSAKey.keysFromPkcs12Identity(p12Data, passphrase : "pass")
```

```swift
let keyData : NSData
//import key into the keychain
let key : RSAKey = try RSAKey.registerOrUpdateKey(keyData, tag : "keyTag")
```

```swift
let modulusData : NSData
let exponentData : NSData
//import key into the keychain
let key : RSAKey = try RSAKey.registerOrUpdateKey(modulus : modulusData, exponent : exponentData, tag : "keyTag")
```

Retrieve or delete key from the keychain :
```swift
//get registered key
let key : RSAKey? = RSAKey.registeredKeyWithTag("keyTag")
//remove
RSAKey.removeKeyWithTag("keyTag")
```

A large part of the raw key import code is copied from the [Heimdall](https://github.com/henrinormak/Heimdall) library.
#### Verify
Use `RSAPKCS1Verifier` as validator to verify token signature :

```swift
let jwt : JSONWebToken
let publicKey : RSAKey
let validator = RegisteredClaimValidator.expiration & 
				RegisteredClaimValidator.notBefore.optional &
				RSAPKCS1Verifier(key : publicKey, hashFunction: .SHA256)
				
let validationResult = validator.validateToken(jwt)
...
```
#### Sign
Use `RSAPKCS1Signer` to generate signed token:

```swift
let payload : JSONWebToken.Payload
let privateKey : RSAKey

let signer = RSAPKCS1Signer(hashFunction: .SHA256, key: privateKey)	
let jwt = try JSONWebToken(payload : payload, signer : signer)
let rawJWT : String = jwt.rawString
...
```
# Validation
Validators (signature and claims) implement the protocol `JSONWebTokenValidatorType`
```swift
public protocol JSONWebTokenValidatorType {
    func validateToken(token : JSONWebToken) -> ValidationResult
}
```

Implementing this protocol on any `class` or `struct` allows it to be combined with other validator using the `|` or `&` operator.
The validation method returns a `ValidationResult` :

```swift
public enum ValidationResult {
    case Success
    case Failure(ErrorType)
    
    public var isValid : Bool
}
```

## Claim validation
You can implement a claim validator with the `ClaimValidator` struct :
```swift
public struct ClaimValidator<T> : JSONWebTokenValidatorType {
}
```

```swift
let validator : ClaimValidator<Int> = ClaimValidator(key: "customClaim", transform: { (jsonValue : AnyObject) throws -> Int in
	guard let numberValue = jsonValue as? NSNumber else {
		throw ClaimValidatorError(message: "customClaim value \(jsonValue) is not the expected Number type")
	}
	return numberValue.integerValue
}).withValidator { 1..<4 ~= $0 }
```

All registered claims validators are implemented :

- `RegisteredClaimValidator.issuer` : `iss` claim is defined and is a `String`
- `RegisteredClaimValidator.subject` : `sub` claim is defined and is a `String` 
- `RegisteredClaimValidator.audience` : `aud` claim is defined and is a `String` or `[String]`
- `RegisteredClaimValidator.expiration` : `exp` claim is defined, is an `Integer` transformable to `NSDate`, and is after current date 
- `RegisteredClaimValidator.notBefore` : `nbf` claim is defined, is an `Integer` transformable to `NSDate`, and is before current date 
- `RegisteredClaimValidator.issuedAt` : `iat` claim is defined, is an `Integer` transformable to `NSDate`
- `RegisteredClaimValidator.jwtIdentifier` : `jti` claim is defined and is a `String` 

And it can be extended : 
```swift
let myIssuerValidator = RegisteredClaimValidator.issuer.withValidator { $0 == "kreactive" }
```
# Unsupported signature

## Verify
Implement the `SignatureValidator` protocol on any `class` or `struct` to add and unsupported signature algorithm validator.
 
```swift
public protocol SignatureValidator : JSONWebTokenValidatorType {
    func canVerifyWithSignatureAlgorithm(alg : SignatureAlgorithm) -> Bool
    func verify(input : NSData, signature : NSData) -> Bool
}
```

## Sign
Implement the `TokenSigner` protocol on any `class` or `struct` to sign token with unsupported signature algorithm.

```swift
public protocol TokenSigner {
    var signatureAlgorithm : SignatureAlgorithm {get}
    func sign(input : NSData) throws -> NSData
}
```

# Test

Test [samples](https://github.com/kreactive/JSONWebToken/tree/master/JSONWebTokenTests/Samples) are [generated](https://github.com/kreactive/JSONWebToken/blob/master/JSONWebTokenTests/Samples/GenerateSample.py) using [pyjwt](https://github.com/jpadilla/pyjwt) Python library
