// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

let MB_SERVICE_UUID = "BB37A001-B922-4018-8E74-E14824B3A638"
let MB_UUID_RX = "BB37A002-B922-4018-8E74-E14824B3A638"
let MB_UUID_TX = "BB37A003-B922-4018-8E74-E14824B3A638"

@objc public class MicroBlocks: NonvisibleComponent, BluetoothConnectionListener {
  private var bleDevice: BluetoothLE? = nil

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  @objc public func Connect(_ bleExtension: BluetoothLE?, _ name: String) {
    bleDevice?.removeConnectionListener(self)
    bleDevice = bleExtension
    bleDevice?.addConnectionListener(self)
    bleDevice?.ConnectToDeviceWithServiceAndName(MB_SERVICE_UUID, name)
  }

  @objc public var IsDeviceConnected: Bool {
    guard let device = bleDevice else {
      return false
    }
    return device.IsDeviceConnected
  }

  @objc public func Disconnect() {
    if IsDeviceConnected {
      bleDevice?.Disconnect()
    }
  }

  @objc public func ConnectionChanged(_ isConnected: Bool) {
    if isConnected {
      bleDevice?.RequestMTU(256)
    }
    EventDispatcher.dispatchEvent(of: self, called: "ConnectionChanged", arguments: isConnected as NSNumber)
  }

  @objc public func SendMessage(_ message: String) {
    let body = message.bytes
    let len = Int32(body.count + 1)

    var msgBytes = [Int32]()
    msgBytes.append(0xFB)
    msgBytes.append(0x1B)
    msgBytes.append(0)
    msgBytes.append(len & 255)
    msgBytes.append((len >> 8) & 255)
    for b in body {
      msgBytes.append(Int32(b))
    }
    msgBytes.append(0xFE)

    bleDevice?.ExWriteByteValues(MB_SERVICE_UUID, MB_UUID_RX, false, msgBytes)
  }

  @objc public func MicroBlocksMessageReceived(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "MicroBlocksMessageReceived", arguments: message as NSString)
  }

  func receiveBytes(_ serviceUuid: String, _ characteristicUuid: String, _ values: [Int32]) {
    let LONG_MSG_START: Int32 = 251
    let BROADCAST_MSG: Int32 = 27

    var start = 0
    while start < values.count {
      // Skip to the start of the next long message
      if values[start] != LONG_MSG_START {
        start += 1
        continue
      }
      guard start + 5 <= values.count else {
        // Not found or incomplete
        break
      }
      let messageLen = Int(values[start + 4] << 8) + Int(values[start + 3])
      guard start + 5 + messageLen <= values.count else {
        // Not found or incomplete
        break
      }
      // Process the message
      if values[start + 1] == BROADCAST_MSG {
        let msgBytes = Array(values[(start + 5)..<(start + 5 + messageLen)])
        let msgData = Data(msgBytes.map { UInt8($0) })
        let message = String(data: msgData, encoding: .utf8)
        if let message = message {
          self.MicroBlocksMessageReceived(message)
        }
      }
      start += 5 + messageLen
    }
  }

  // MARK: BluetoothConnectionListener implementation

  public func onConnected(_ component: BluetoothLE) {
    component.ExRegisterForByteValues(MB_SERVICE_UUID, MB_UUID_TX, false, self, self.receiveBytes(_:_:_:))
    ConnectionChanged(true)
  }

  public func onDisconnected(_ component: BluetoothLE) {
    ConnectionChanged(false)
  }
}
