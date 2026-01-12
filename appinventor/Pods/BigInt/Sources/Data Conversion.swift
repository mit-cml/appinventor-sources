//
//  Data Conversion.swift
//  BigInt
//
//  Created by Károly Lőrentey on 2016-01-04.
//  Copyright © 2016-2017 Károly Lőrentey.
//

import Foundation

extension BigUInt {
    //MARK: NSData Conversion

    public init(_ buffer: UnsafeRawBufferPointer) {
        // This assumes Word is binary.
        precondition(Word.bitWidth % 8 == 0)

        self.init()

        let length = buffer.count
        guard length > 0 else { return }
        let bytesPerDigit = Word.bitWidth / 8
        var index = length / bytesPerDigit
        var c = bytesPerDigit - length % bytesPerDigit
        if c == bytesPerDigit {
            c = 0
            index -= 1
        }

        var word: Word = 0
        for byte in buffer {
            word <<= 8
            word += Word(byte)
            c += 1
            if c == bytesPerDigit {
                self[index] = word
                index -= 1
                c = 0
                word = 0
            }
        }
        assert(c == 0 && word == 0 && index == -1)
    }


    /// Initializes an integer from the bits stored inside a piece of `Data`.
    /// The data is assumed to be in network (big-endian) byte order.
    public init(_ data: Data) {
        // This assumes Word is binary.
        precondition(Word.bitWidth % 8 == 0)

        self.init()

        let length = data.count
        guard length > 0 else { return }
        let bytesPerDigit = Word.bitWidth / 8
        var index = length / bytesPerDigit
        var c = bytesPerDigit - length % bytesPerDigit
        if c == bytesPerDigit {
            c = 0
            index -= 1
        }
        let word: Word = data.withUnsafeBytes { buffPtr in
            var word: Word = 0
            let p = buffPtr.bindMemory(to: UInt8.self)
            for byte in p {
                word <<= 8
                word += Word(byte)
                c += 1
                if c == bytesPerDigit {
                    self[index] = word
                    index -= 1
                    c = 0
                    word = 0
                }
            }
            return word
        }
        assert(c == 0 && word == 0 && index == -1)
    }

    /// Return a `Data` value that contains the base-256 representation of this integer, in network (big-endian) byte order.
    public func serialize() -> Data {
        // This assumes Digit is binary.
        precondition(Word.bitWidth % 8 == 0)

        let byteCount = (self.bitWidth + 7) / 8

        guard byteCount > 0 else { return Data() }

        var data = Data(count: byteCount)
        data.withUnsafeMutableBytes { buffPtr in
            let p = buffPtr.bindMemory(to: UInt8.self)
            var i = byteCount - 1
            for var word in self.words {
                for _ in 0 ..< Word.bitWidth / 8 {
                    p[i] = UInt8(word & 0xFF)
                    word >>= 8
                    if i == 0 {
                        assert(word == 0)
                        break
                    }
                    i -= 1
                }
            }
        }
        return data
    }
}

