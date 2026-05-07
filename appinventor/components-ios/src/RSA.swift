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
import BigInt

struct RSAKey {
  var N : BigUInt
  var E : BigUInt
  var D : BigUInt
  /*
   var Primes : [BigUInt]
   var Dp: BigUInt
   var Dq: BigUInt
   var Qinv : BigUInt
   */
  
  init?(privateKey:String) {
    var pem = ""
    for line in privateKey.components(separatedBy:"\n") {
      if line.range(of:"PRIVATE KEY") == nil {
        pem += line
      }
    }
    if let der = Data(base64Encoded: pem, options: []),
      let asn1Array = ASN1Decoder.decode(der: der) {
      N = BigUInt(asn1Array[2].children?[1].data ?? Data())
      E = BigUInt(asn1Array[2].children?[2].data ?? Data())
      D = BigUInt(asn1Array[2].children?[3].data ?? Data())
    } else {
      return nil
    }
  }
  
  func sign(hash:[UInt8]) -> [UInt8] {
    let prefix : [UInt8] = [0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20]
    var dataToEncode = [UInt8](repeating: 0xFF, count: 256)
    dataToEncode[0] = 0x00
    dataToEncode[1] = 0x01
    let offset1 = 256 - hash.count - prefix.count
    dataToEncode[offset1 - 1] = 0x00
    for i in 0..<prefix.count {
      dataToEncode[offset1+i] = prefix[i]
    }
    let offset2 = 256 - hash.count
    for i in 0..<hash.count {
      dataToEncode[offset2+i] = hash[i]
    }
    let message = BigUInt(Data(dataToEncode))
    let signature = message.power(self.D, modulus: self.N)
    //let verify = signature.power(self.E, modulus: self.N)
    return Array(signature.serialize())
  }
}
