// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A CircularBuffer struct.  A buffer of fixed size.
 */
public struct CircularBuffer<T> {
  fileprivate var _insertIndex: Int = 0
  fileprivate var _array: [T]
  fileprivate let _maxSize: Int
  
  init(_ maxSize: Int, _ defaultVal: T) {
    _maxSize = maxSize
    _array = [T](repeating: defaultVal, count: maxSize)
  }
  
  public var buffer: [T] {
    get {
      return _array
    }
  }
  
  public var size: Int {
    get {
      return _maxSize
    }
  }
  
  mutating func write(_ value: T) {
    _array[_insertIndex] = value
    _insertIndex += 1
    _insertIndex = _insertIndex >= _maxSize ? 0 : _insertIndex
  }
}
