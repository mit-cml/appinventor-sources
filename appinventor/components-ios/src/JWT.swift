// Copyright 2019 Google LLC. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import Foundation
import CryptoSwift
import SwiftyBase64

struct JWTHeader : Codable {
  let Algorithm : String
  let Format : String
  enum CodingKeys: String, CodingKey {
    case Algorithm = "alg"
    case Format = "typ"
  }
}

struct JWTClaimSet : Codable {
  let Issuer : String
  let Audience : String
  let Scope : String
  let IssuedAt : Int
  let Expiration : Int
  enum CodingKeys: String, CodingKey {
    case Issuer = "iss"
    case Audience = "aud"
    case Scope = "scope"
    case IssuedAt = "iat"
    case Expiration = "exp"
  }
}

struct JWT {
  static func encodeWithRS256(jwtHeader: JWTHeader,
                              jwtClaimSet: JWTClaimSet,
                              rsaKey: RSAKey) throws -> String {
    let encoder = JSONEncoder()
    let headerData = try encoder.encode(jwtHeader)
    let claimsData = try encoder.encode(jwtClaimSet)
    let header = SwiftyBase64.EncodeString(Array(headerData), alphabet:.URLAndFilenameSafe)
    let claims = SwiftyBase64.EncodeString(Array(claimsData), alphabet:.URLAndFilenameSafe)
    let body = header + "." + claims
    let bodyData = body.data(using: String.Encoding.utf8)!
    let sha2 = SHA2(variant: SHA2.Variant(rawValue:256)!)
    let hash = sha2.calculate(for:Array(bodyData))
    let signature = rsaKey.sign(hash:hash)
    let signatureString = SwiftyBase64.EncodeString(signature, alphabet:.URLAndFilenameSafe)
    return body + "." + signatureString
  }
}
