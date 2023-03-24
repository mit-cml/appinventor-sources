//
//  Random.swift
//  BigInt
//
//  Created by Károly Lőrentey on 2016-01-04.
//  Copyright © 2016-2017 Károly Lőrentey.
//

import Foundation
#if os(Linux) || os(FreeBSD)
  import Glibc
#endif


extension BigUInt {
    //MARK: Random Integers

    /// Create a big integer consisting of `width` uniformly distributed random bits.
    ///
    /// - Returns: A big integer less than `1 << width`.
    /// - Note: This function uses `arc4random_buf` to generate random bits.
    public static func randomInteger(withMaximumWidth width: Int) -> BigUInt {
        guard width > 0 else { return 0 }

        let byteCount = (width + 7) / 8
        assert(byteCount > 0)

        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: byteCount)
      #if os(Linux) || os(FreeBSD)
        let fd = open("/dev/urandom", O_RDONLY)
        defer {
            close(fd)
        }
        let _ = read(fd, buffer, MemoryLayout<UInt8>.size * byteCount)
      #else
        arc4random_buf(buffer, byteCount)
      #endif
        if width % 8 != 0 {
            buffer[0] &= UInt8(1 << (width % 8) - 1)
        }
        defer {
            buffer.deinitialize(count: byteCount)
            buffer.deallocate()
        }
        return BigUInt(Data(bytesNoCopy: buffer, count: byteCount, deallocator: .none))
    }

    /// Create a big integer consisting of `width-1` uniformly distributed random bits followed by a one bit.
    ///
    /// - Returns: A random big integer whose width is `width`.
    /// - Note: This function uses `arc4random_buf` to generate random bits.
    public static func randomInteger(withExactWidth width: Int) -> BigUInt {
        guard width > 1 else { return BigUInt(width) }
        var result = randomInteger(withMaximumWidth: width - 1)
        result[(width - 1) / Word.bitWidth] |= 1 << Word((width - 1) % Word.bitWidth)
        return result
    }

    /// Create a uniformly distributed random integer that's less than the specified limit.
    ///
    /// - Returns: A random big integer that is less than `limit`.
    /// - Note: This function uses `arc4random_buf` to generate random bits.
    public static func randomInteger(lessThan limit: BigUInt) -> BigUInt {
        let width = limit.bitWidth
        var random = randomInteger(withMaximumWidth: width)
        while random >= limit {
            random = randomInteger(withMaximumWidth: width)
        }
        return random
    }
}
