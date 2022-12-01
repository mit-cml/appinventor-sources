// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import CoreBluetooth

@objc public protocol BluetoothConnectionListener {
  @objc optional func onConnected(_ conn: BluetoothLE)
  @objc optional func onDisconnected(_ conn: BluetoothLE)
}

class BleDeviceInfo {
  var peripheral: CBPeripheral
  var advertisingData: [String: Any]
  var rssi: NSNumber

  init(_ peripheral: CBPeripheral, _ advertisingData: [String: Any], _ rssi: NSNumber) {
    self.peripheral = peripheral
    self.advertisingData = advertisingData
    self.rssi = rssi
  }

  var description: String {
    var result = self.peripheral.identifier.uuidString + " " + (self.peripheral.name ?? "") + " "
    result += self.rssi.stringValue
    return result
  }
}

@objc public class BluetoothLE : NonvisibleComponent, CBCentralManagerDelegate, CBPeripheralDelegate {

  private var manager: CBCentralManager!
  private var peripheral: CBPeripheral? = nil
  private var deviceList = [BleDeviceInfo]()
  private var services = [CBUUID: CBService]()
  private var characteristics = [CBUUID: CBCharacteristic]()
  private var descriptors = [CBUUID: CBDescriptor]()
  private var listeners = NSMutableSet()

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
    self.manager = CBCentralManager(delegate: self, queue: nil)
  }

  // MARK: Properties

  @objc public var AdvertisementScanPeriod: Int32 = 0

  @objc public var AdvertiserAddresses: String {
    return ""
  }

  @objc public var AdvertiserNames: String {
    return ""
  }

  @objc public var AutoReconnect: Bool = false

  @objc public var BatteryValue: Int32 {
    return 100
  }

  @objc public var ConnectedDeviceName: String {
    return ""
  }

  @objc public var ConnectedDeviceRssi: Int32 {
    return 0
  }

  @objc public var ConnectionTimeout: Int32 = 0

  @objc public var DeviceCharacteristics: YailList<AnyObject> {
    return YailList<AnyObject>()
  }

  @objc public var DeviceList: String {
    var infos = ""
    for device in deviceList {
      infos += device.description
    }
    return infos
  }

  @objc public var DeviceServices: YailList<AnyObject> {
    return YailList<AnyObject>()
  }

  @objc public var IsDeviceAdvertising: Bool {
    return false
  }

  @objc public var IsDeviceConnected: Bool {
    return self.peripheral != nil
  }

  @objc public var NullTerminatedStrings: Bool = false

  @objc public var Scanning: Bool {
    return self.manager.isScanning
  }

  @objc public var TxPower: Double {
    return 0.0
  }

  // MARK: Methods

  @objc public func Connect(_ index: Int32) {
    guard index >= 1 && index <= self.deviceList.count else {
      // TODO(ewpatton): Raise an error?
      return
    }
    let deviceInfo = self.deviceList[Int(index - 1)]
    self.manager.connect(deviceInfo.peripheral)
  }

  @objc public func Disconnect() {
    guard let peripheral = self.peripheral else {
      // Not connected so nothing to do.
      return
    }
    self.manager.cancelPeripheralConnection(peripheral)
  }

  @objc public func StartScanning() {
    guard self.manager.state == .poweredOn else {
      // TODO(ewpatton): Report error to client
      return
    }

    manager.scanForPeripherals(withServices: nil, options: nil)
  }

  @objc public func StopScanning() {
    guard self.manager.isScanning else {
      // TODO(ewpatton): Report error to client
      return
    }

    manager.stopScan()
  }


  // MARK: Events

  @objc public func Connected() {
    EventDispatcher.dispatchEvent(of: self, called: "Connected")
  }

  @objc public func ConnectionFailed(_ reason: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ConnectionFailed", arguments: reason as AnyObject)
  }

  @objc public func DeviceFound() {
    EventDispatcher.dispatchEvent(of: self, called: "DeviceFound")
  }

  @objc public func Disconnected() {
    EventDispatcher.dispatchEvent(of: self, called: "Disconnected")
  }

  @objc public func BytesReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                  _ values: [Int]) {

  }

  @objc public func ShortsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                   _ values: [Int]) {

  }

  @objc public func IntegersReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                     _ values: [Int]) {

  }

  @objc public func FloatsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                   _ values: [Float]) {

  }

  @objc public func StringsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                    _ values: [String]) {

  }

  // MARK: Extension interface

  @objc public func addConnectionListener(_ listener: BluetoothConnectionListener) {
    listeners.add(listener)
  }

  @objc public func removeConnectionListener(_ listener: BluetoothConnectionListener) {
    listeners.remove(listener)
  }

  @objc public func ExRegisterForStringValues(_ service: String, _ characteristic: String,
      _ utf16: Bool, _ handler: (String, String, [String]) -> Void) {

  }

  @objc public func ExUnregisterForValues(_ service: String, _ characteristic: String,
      _ handler: (String, String, [String]) -> Void) {

  }

  @objc public func ExWriteStringValuesWithResponse(_ service: String, _ characteristic: String,
      _ utf16: Bool, _ message: String, _ handler: (String, String, [String]) -> Void) {

  }

  // MARK: CBCentralManagerDelegate implementation

  public func centralManagerDidUpdateState(_ central: CBCentralManager) {
  }

  public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
    self.peripheral = peripheral
    peripheral.delegate = self
    peripheral.discoverServices(nil)
  }

  public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
    self.peripheral = nil
    Disconnected()
  }

  public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
    deviceList.append(BleDeviceInfo(peripheral, advertisementData, RSSI))
    DeviceFound()
  }

  // MARK: CBPeripheralDelegate implementation

  public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
    if let error = error {
      // TODO(ewpatton): Report error to user
      return
    }
    guard let services = peripheral.services else {
      return
    }
    for service in services {
      peripheral.discoverCharacteristics(nil, for: service)
      self.services[service.uuid] = service
    }
    Connected()
  }

  public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    if let error = error {
      // TODO(ewpatton): Report error to user
      return
    }
    guard let characteristics = service.characteristics else {
      return
    }
    for characteristic in characteristics {
      self.characteristics[characteristic.uuid] = characteristic
    }
  }

  public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
    if characteristic.isNotifying {

    } else {

    }
  }

  public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
    // pass
  }
}
