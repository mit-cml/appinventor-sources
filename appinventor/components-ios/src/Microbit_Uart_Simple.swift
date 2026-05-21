// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022-2024 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import CoreBluetooth

fileprivate let kMicrobitUartService = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
fileprivate let kMicrobitTxCharacteristic = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
fileprivate let kMicrobitRxCharacteristic = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

/**
 * The `Microbit_Uart_Simple` component implments the UART protocol for the BBC micro:bit.
 */
@objc public class Microbit_Uart_Simple : NonvisibleComponent, BluetoothConnectionListener, BleDevice {

  private unowned var bleConnection: BluetoothLE? = nil

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  // MARK: Properties

  @objc public var BluetoothDevice: BluetoothLE? {
    get {
      return bleConnection
    }
    set {
      bleConnection?.removeConnectionListener(self)
      bleConnection = newValue
      bleConnection?.addConnectionListener(self)
    }
  }

  // MARK: Methods

  @objc public func SendMessage(_ message: String) {
    guard let conn = self.bleConnection else {
      reportNullConnection("SendMessage")
      return
    }
    conn.ExWriteStringValuesWithResponse(kMicrobitUartService, kMicrobitRxCharacteristic,
        false, message, self.rxCharacteristicWriteHandler)
  }

  // MARK: Events

  @objc public func MessageReceived(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "MessageReceived",
        arguments: message as AnyObject)
  }

  @objc public func MessageSent(_ message: String) {
    EventDispatcher.dispatchEvent(of: self, called: "MessageSent", arguments: message as AnyObject)
  }

  // MARK: BluetoothConnectionListener implementation

  @objc public func onConnected(_ conn: BluetoothLE) {
    conn.ExRegisterForStringValues(kMicrobitUartService, kMicrobitTxCharacteristic, false,
        self, self.txCharacteristicHandler)
  }

  @objc public func onDisconnected(_ conn: BluetoothLE) {
    conn.ExUnregisterForValues(kMicrobitUartService, kMicrobitTxCharacteristic,
        self, self.txCharacteristicHandler)
  }

  // MARK: BleDevice implementation

  @objc public var broadcastUuid: CBUUID? = nil

  @objc public var deviceCallback: ((CBPeripheral) -> Bool)? {
    return { peripheral in
      guard let name = peripheral.name else {
        return false
      }
      return name.contains("BBC micro:bit")
    }
  }

  // MARK: Private implementation

  private func rxCharacteristicWriteHandler(_ service: String, _ characteristic: String,
      _ values: [String]) {
    guard !values.isEmpty else {
      return
    }
    MessageSent(values.first ?? "")
  }

  private func txCharacteristicHandler(_ service: String, _ characteristic: String,
      _ values: [String]) {
    guard !values.isEmpty else {
      return
    }
    MessageReceived(values.first ?? "")
  }

  private func reportNullConnection(_ method: String) {
    self._form?.dispatchErrorOccurredEvent(self, method, ErrorMessage.ERROR_EXTENSION_ERROR,
        1 as AnyObject, "Microbit_Uart_Simple", "BluetoothDevice is not set!")
  }
}
