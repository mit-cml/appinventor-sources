//
//  SecKeyUtils.swift
//  JSONWebToken
//
//  A substantial portions of this code is from the Heimdall library
//  https://github.com/henrinormak/Heimdall
//
//  Heimdall - The gatekeeper of Bifrost, the road connecting the
//  world (Midgard) to Asgard, home of the Norse gods.
//
//  In iOS, Heimdall is the gatekeeper to the Keychain, offering
//  a nice wrapper for interacting with private-public RSA keys
//  and encrypting/decrypting/signing data.
//
//  Created by Henri Normak on 22/04/15.
//
//  The MIT License (MIT)
//
//  Copyright (c) 2015 Henri Normak
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//  SOFTWARE.
//



import Foundation
import Security



//these methods use a keychain api side effect to create public key from raw data
public extension RSAKey {
    
    enum KeyUtilError : Swift.Error {
        case notStringReadable
        case badPEMArmor
        case notBase64Readable
        case badKeyFormat
    }
    @discardableResult public static func registerOrUpdateKey(_ keyData : Data, tag : String) throws -> RSAKey {
        let key : SecKey? = try {
            if let existingData = try getKeyData(tag) {
                let newData = keyData.dataByStrippingX509Header()
                if existingData != newData {
                    try updateKey(tag, data: newData)
                }
                return try getKey(tag)
            } else {
                return try addKey(tag, data: keyData.dataByStrippingX509Header())
            }
        }()
        if let result = key {
            return RSAKey(secKey : result)
        } else {
            throw KeyUtilError.badKeyFormat
        }
    }
    @discardableResult public static func registerOrUpdateKey(modulus: Data, exponent : Data, tag : String) throws -> RSAKey {
        let combinedData = Data(modulus: modulus, exponent: exponent)
        return try RSAKey.registerOrUpdateKey(combinedData, tag : tag)
    }
    @discardableResult public static func registerOrUpdatePublicPEMKey(_ keyData : Data, tag : String) throws -> RSAKey {
        guard let stringValue = String(data: keyData, encoding: String.Encoding.utf8) else {
            throw KeyUtilError.notStringReadable
        }
        
        let base64Content : String = try {
            //remove ----BEGIN and ----END
            let scanner = Scanner(string: stringValue)
            scanner.charactersToBeSkipped = CharacterSet.whitespacesAndNewlines
            if scanner.scanString("-----BEGIN", into: nil) {
                scanner.scanUpTo("KEY-----", into: nil)
                guard scanner.scanString("KEY-----", into: nil) else {
                    throw KeyUtilError.badPEMArmor
                }
                
                var content : NSString? = nil
                scanner.scanUpTo("-----END", into: &content)
                guard scanner.scanString("-----END", into: nil) else {
                    throw KeyUtilError.badPEMArmor
                }
                return content?.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
            }
            return nil
        }() ?? stringValue
        
        guard let decodedKeyData = Data(base64Encoded: base64Content, options:[.ignoreUnknownCharacters]) else {
            throw KeyUtilError.notBase64Readable
        }
        return try RSAKey.registerOrUpdateKey(decodedKeyData, tag: tag)
    }
    static func registeredKeyWithTag(_ tag : String) -> RSAKey? {
        return ((try? getKey(tag)) ?? nil).map(RSAKey.init)
    }
    static func removeKeyWithTag(_ tag : String) {
        do {
            try deleteKey(tag)
        } catch {}
    }
}

private func getKey(_ tag: String) throws -> SecKey? {
    var keyRef: AnyObject?
    
    var query = matchQueryWithTag(tag)
    query[kSecReturnRef as String] = kCFBooleanTrue
    
    let status = SecItemCopyMatching(query as CFDictionary, &keyRef)
    
    switch status {
    case errSecSuccess:
        if keyRef != nil {
            return (keyRef as! SecKey)
        } else {
            return nil
        }
    case errSecItemNotFound:
        return nil
    default:
        throw RSAKey.Error.securityError(status)
    }
}
internal func getKeyData(_ tag: String) throws -> Data? {
    
    var query = matchQueryWithTag(tag)
    query[kSecReturnData as String] = kCFBooleanTrue
    
    var result: AnyObject? = nil
    let status = SecItemCopyMatching(query as CFDictionary, &result)

    switch status {
    case errSecSuccess:
        return (result as! Data)
    case errSecItemNotFound:
        return nil
    default:
        throw RSAKey.Error.securityError(status)
    }
}
private func updateKey(_ tag: String, data: Data) throws {
    let query = matchQueryWithTag(tag)
    let updateParam = [kSecValueData as String : data]
    let status = SecItemUpdate(query as CFDictionary, updateParam as CFDictionary)
    guard status == errSecSuccess else {
        throw RSAKey.Error.securityError(status)
    }
}

private func deleteKey(_ tag: String) throws {
    let query = matchQueryWithTag(tag)
    let status = SecItemDelete(query as CFDictionary)
    if status != errSecSuccess {
        throw RSAKey.Error.securityError(status)
    }
}
private func matchQueryWithTag(_ tag : String) -> Dictionary<String, Any> {
    return [
        kSecAttrKeyType as String : kSecAttrKeyTypeRSA,
        kSecClass as String : kSecClassKey,
        kSecAttrApplicationTag as String : tag,
    ]
}

private func addKey(_ tag: String, data: Data) throws -> SecKey? {
    var publicAttributes = Dictionary<String, Any>()
    publicAttributes[kSecAttrKeyType as String] = kSecAttrKeyTypeRSA
    publicAttributes[kSecClass as String] = kSecClassKey
    publicAttributes[kSecAttrApplicationTag as String] = tag as CFString
    publicAttributes[kSecValueData as String] = data as CFData
    publicAttributes[kSecReturnPersistentRef as String] = kCFBooleanTrue
    
    var persistentRef: CFTypeRef?
    let status = SecItemAdd(publicAttributes as CFDictionary, &persistentRef)
    if status == noErr || status == errSecDuplicateItem {
        return try getKey(tag)
    }
    throw RSAKey.Error.securityError(status)
}

///
/// Encoding/Decoding lengths as octets
///
private extension NSInteger {
    func encodedOctets() -> [CUnsignedChar] {
        // Short form
        if self < 128 {
            return [CUnsignedChar(self)];
        }
        
        // Long form
        let i = (self / 256) + 1
        var len = self
        var result: [CUnsignedChar] = [CUnsignedChar(i + 0x80)]
        
        for _ in 0..<i {
            result.insert(CUnsignedChar(len & 0xFF), at: 1)
            len = len >> 8
        }
        
        return result
    }
    
    init?(octetBytes: [CUnsignedChar], startIdx: inout NSInteger) {
        if octetBytes[startIdx] < 128 {
            // Short form
            self.init(octetBytes[startIdx])
            startIdx += 1
        } else {
            // Long form
            let octets = NSInteger(octetBytes[startIdx] - CUnsignedChar(128))
            
            if octets > octetBytes.count - startIdx {
                self.init(0)
                return nil
            }
            
            var result = UInt64(0)
            
            for j in 1...octets {
                result = (result << 8)
                result = result + UInt64(octetBytes[startIdx + j])
            }
            
            startIdx += 1 + octets
            self.init(result)
        }
    }
}

///
/// Manipulating data
///
private extension Data {
    init(modulus: Data, exponent: Data) {
        // Make sure neither the modulus nor the exponent start with a null byte
        var modulusBytes = [CUnsignedChar](UnsafeBufferPointer<CUnsignedChar>(start: (modulus as NSData).bytes.bindMemory(to: CUnsignedChar.self, capacity: modulus.count), count: modulus.count / MemoryLayout<CUnsignedChar>.size))
        let exponentBytes = [CUnsignedChar](UnsafeBufferPointer<CUnsignedChar>(start: (exponent as NSData).bytes.bindMemory(to: CUnsignedChar.self, capacity: exponent.count), count: exponent.count / MemoryLayout<CUnsignedChar>.size))
        
        // Make sure modulus starts with a 0x00
        if let prefix = modulusBytes.first , prefix != 0x00 {
            modulusBytes.insert(0x00, at: 0)
        }
        
        // Lengths
        let modulusLengthOctets = modulusBytes.count.encodedOctets()
        let exponentLengthOctets = exponentBytes.count.encodedOctets()
        
        // Total length is the sum of components + types
        let totalLengthOctets = (modulusLengthOctets.count + modulusBytes.count + exponentLengthOctets.count + exponentBytes.count + 2).encodedOctets()
        
        // Combine the two sets of data into a single container
        var builder: [CUnsignedChar] = []
        let data = NSMutableData()
        
        // Container type and size
        builder.append(0x30)
        builder.append(contentsOf: totalLengthOctets)
        data.append(builder, length: builder.count)
        builder.removeAll(keepingCapacity: false)
        
        // Modulus
        builder.append(0x02)
        builder.append(contentsOf: modulusLengthOctets)
        data.append(builder, length: builder.count)
        builder.removeAll(keepingCapacity: false)
        data.append(modulusBytes, length: modulusBytes.count)
        
        // Exponent
        builder.append(0x02)
        builder.append(contentsOf: exponentLengthOctets)
        data.append(builder, length: builder.count)
        data.append(exponentBytes, length: exponentBytes.count)
        
        self = Data(referencing: data)
    }
    
    
    func dataByStrippingX509Header() -> Data {
        
        var bytes = [CUnsignedChar](repeating: 0, count: self.count)
        (self as NSData).getBytes(&bytes, length:self.count)
        
        var range = NSRange(location: 0, length: self.count)
        var offset = 0
        
        // ASN.1 Sequence
        if bytes[offset] == 0x30 {
            offset += 1
            
            // Skip over length
            let _ = NSInteger(octetBytes: bytes, startIdx: &offset)
            
            let OID: [CUnsignedChar] = [0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86,
                                        0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00]
            let slice: [CUnsignedChar] = Array(bytes[offset..<(offset + OID.count)])
            
            if slice == OID {
                offset += OID.count
                
                // Type
                if bytes[offset] != 0x03 {
                    return self
                }
                
                offset += 1
                
                // Skip over the contents length field
                let _ = NSInteger(octetBytes: bytes, startIdx: &offset)
                
                // Contents should be separated by a null from the header
                if bytes[offset] != 0x00 {
                    return self
                }
                
                offset += 1
                range.location += offset
                range.length -= offset
            } else {
                return self
            }
        }
        
        return self.subdata(in : Range(range)!)
    }
}
