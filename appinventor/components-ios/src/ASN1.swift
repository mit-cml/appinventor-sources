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
import BigInt
import SwiftyBase64

private extension Data {
  var firstByte: UInt8 {
    var byte: UInt8 = 0
    copyBytes(to: &byte, count: MemoryLayout<UInt8>.size)
    return byte
  }
}

private class ASN1Scanner {
  let data: Data
  
  private(set) var position = 0
  
  init(data: Data) {
    self.data = data
  }
  
  var isComplete: Bool {
    return position >= data.count
  }
  
  var remaining: Int {
    return data.count - position
  }
  
  func scan(distance: Int) -> Data? {
    guard distance > 0 else { return nil }
    guard position <= (data.count - distance) else { return nil }
    defer {
      position = position + distance
    }
    return data.subdata(in: data.startIndex.advanced(by: position)..<data.startIndex.advanced(by: position + distance))
  }
  
  func scanLength() -> Int {
    var length = 0
    if let lengthByte = self.scan(distance: 1)?.firstByte {
      if lengthByte & 0x80 != 0x00 {
        // long form
        let lengthLength = Int(lengthByte & 0x7F)
        for _ in 0..<lengthLength {
          if let nextByte = self.scan(distance: 1)?.firstByte {
            length = length * 256 + Int(nextByte)
          }
        }
      } else {
        // short form
        length = Int(lengthByte)
      }
    }
    return length
  }
}

struct ASN1Object {
  let type: ASN1Decoder.DERCode
  let data: Data
  var children: [ASN1Object]?
}

struct ASN1Decoder {
  enum DERCode: UInt8 {
    case Boolean = 0x01
    case Integer = 0x02
    case OctetString = 0x04
    case Null = 0x05
    case ObjectIdentifier = 0x06
    case IA5String = 0x16
    case Sequence = 0x30
    static func allTypes() -> [DERCode] {
      return [
        .Boolean,
        .Integer,
        .OctetString,
        .Null,
        .ObjectIdentifier,
        .IA5String,
        .Sequence,
      ]
    }
  }
  static func decode(der: Data) -> [ASN1Object]? {
    let scanner =  ASN1Scanner(data: der)
    //Verify that this is actually a DER sequence
    guard scanner.scan(distance: 1)?.firstByte == DERCode.Sequence.rawValue else {
      print("not a DER sequence")
      return nil
    }
    let length = scanner.scanLength()
    // The length should be length of the data, minus itself and the sequence type
    guard length == scanner.remaining else {
      print("invalid length \(length)")
      return nil
    }
    return self.decodeSequence(scanner: scanner)
  }
  fileprivate static func decodeSequence(scanner:  ASN1Scanner) -> [ASN1Object]? {
    var output: [ASN1Object] = []
    while !scanner.isComplete {
      let scannedType = scanner.scan(distance: 1)?.firstByte
      var dataType: DERCode?
      for type in DERCode.allTypes() {
        if scannedType == type.rawValue {
          dataType = type
          break
        }
      }
      guard let type = dataType else {
        let unsupported = scannedType
        print("unsupported type \(unsupported!)")
        return nil
      }
      let fieldLength = scanner.scanLength()
      if fieldLength > 0 {
        guard let actualData = scanner.scan(distance: fieldLength) else {
          print("too few bytes")
          return nil
        }
        var object = ASN1Object(type: type, data: actualData, children:nil)
        if type == .Sequence {
          object.children = decodeSequence(scanner: ASN1Scanner(data: actualData))
        }
        if type == .OctetString {
          object.children = decode(der:actualData)
        }
        output.append(object)
      } else {
        output.append(ASN1Object(type: type, data: Data(), children:nil))
      }
    }
    return output
  }
}


