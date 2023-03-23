// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

class ByteTools{
  
  /**
   stolen from http://stackoverflow.com/questions/1305225/best-way-to-serialize-an-nsdata-into-a-hexadeximal-string
   */
  static func asHexString(data:NSData)->String{
    
    if data.length > 0 {
      let  hexChars = Array("0123456789abcdef".utf8) as [UInt8];
      let buf = UnsafeBufferPointer<UInt8>(start: data.bytes.bindMemory(to: UInt8.self, capacity: data.length), count: data.length);
      var output = [UInt8](repeating: 0, count: data.length*2 + 1);
      var ix:Int = 0;
      for b in buf {
        let hi  = Int((b & 0xf0) >> 4);
        let low = Int(b & 0x0f);
        output[ix] = hexChars[ hi];
        ix += 1
        output[ix] = hexChars[low];
        ix += 1
      }
      let result = String(cString: output)
      return result;
    }
    return "";
  }
  
  /**
   Takes a uint32 and converts it to a byte array
   */
  static func uint32ToUint8Array(value: UInt32) -> [UInt8]{
    var bigEndian = value.bigEndian
    let count = MemoryLayout<UInt32>.size
    let bytePtr = withUnsafePointer(to: &bigEndian) {
      $0.withMemoryRebound(to: UInt8.self, capacity: count) {
        UnsafeBufferPointer(start: $0, count: count)
      }
    }
    return Array(bytePtr)
  }
  
  /**
   Takes a int16 and converts it to a byte array
   */
  static func int16ToUint8Array(value: Int16) -> [UInt8]{
    var bigEndian = value.bigEndian
    let count = MemoryLayout<UInt32>.size
    let bytePtr = withUnsafePointer(to: &bigEndian) {
      $0.withMemoryRebound(to: UInt8.self, capacity: count) {
        UnsafeBufferPointer(start: $0, count: count)
      }
    }
    return Array(bytePtr)
  }
  
  /**
   Returns the first byte of a int16 value
   */
  static func firstByteOfInt16(value: Int16) -> UInt8{
    let all = int16ToUint8Array(value: value)
    return all[1]
  }
  
  static func convertToUInt8(data: NSData?, position: Int) -> UInt8{
    var out: UInt8 = 0
    guard let data = data else { return 0 }
    data.getBytes(&out, range: NSMakeRange(position, 1))
    return out
  }
  
}

extension NSMutableData{
  /// Appends UInt32 in BE format
  func appendUInt32(value : UInt32) {
    var val = value.bigEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  /// Appends UInt32 in BE format
  func appendUInt32LE(value : UInt32) {
    var val = value.littleEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  /// Appends UInt16 in LE format
  func appendUInt16LE(value: UInt16){
    var val = value.littleEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  /// Apppends Int16 in BE format
  func appendInt16(value: Int16){
    var val = value.bigEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  /// Apppends Int16 in LE format
  func appendInt16LE(value: Int16){
    var val = value.littleEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  /// Appends UInt16 in BE format
  func appendUInt16(value : UInt16) {
    var val = value.bigEndian
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
  
  func appendUInt8(value : UInt8) {
    var val = value
    self.append(&val, length: MemoryLayout.size(ofValue: val))
  }
}
