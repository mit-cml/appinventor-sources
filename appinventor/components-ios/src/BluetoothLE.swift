// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022-2024 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import CoreBluetooth

/**
 * The error code to report when a given BLE operation is not supported.
 */
fileprivate let ERROR_OPERATION_NOT_SUPPORTED = 9012

/**
 * The error code to report when an error occurs reading a BLE characteristic.
 */
fileprivate let ERROR_BLE_READ_ERROR = 9013

/**
 * The `BluetoothConnectionListener` protocol defines a means by which other components can
 * receive information about a `BluetoothLE` instance's connection status.
 */
@objc public protocol BluetoothConnectionListener {
  /**
   * The `component` has successfully connected to a peripheral.
   */
  @objc optional func onConnected(_ component: BluetoothLE)

  /**
   * The `component` has disconnected from the peripheral.
   */
  @objc optional func onDisconnected(_ component: BluetoothLE)
}

extension Data {
  /**
   * Converts the `Data` into an `Array` of the `Desired` integer type. The stride of the data
   * is determined via the `Intermediate` type.
   *
   * - Parameters:
   *    - type: The intermediate type for interpreting the bytes contained in the `Data`
   *
   * - Returns: An `Array` of the `Desired` type.
   */
  func interpret<
    Intermediate: FixedWidthInteger,
    Desired: FixedWidthInteger
  >(as type: Intermediate.Type) -> [Desired] {
    return self.withUnsafeBytes {
      Array($0.bindMemory(to: Intermediate.self)).map(Intermediate.init(littleEndian:))
    }.map { Desired($0) }
  }
}

extension Array where Element == any FixedWidthInteger {
  /**
   * Converts an `Array` of `FixedWidthInteger` into a `Data` using a little-endian encoding.
   *
   * - Returns: A new `Data` containing bytes for the contents of this `Array`.
   */
  func data() -> Data {
    var result = Data()
    for element in self {
      result += Swift.withUnsafeBytes(of: element.littleEndian) { Data($0) }
    }
    return result
  }
}

/**
 * `StringEncodingError` can be thrown when an attempt to encode a `String` using `data(using:)`
 * fails. This can be more informative than a method returning `nil`.
 */
struct StringEncodingError: Error {
  /**
   * The string for which an error was encountered during encoding.
   */
  let string: String

  /**
   * The target encoding that caused the error.
   */
  let targetEncoding: String.Encoding
}

extension Array where Element == String {
  /**
   * Converts an `Array<String>` into a `Data` using the given `encoding` and optionally
   * terminating each `String` with `terminator`.
   *
   * - Parameters:
   *    - encoding: The desired encoding for the `String`s in the `Array`.
   *    - terminator: Optional terminating string, by default the empty string.
   * - Throws: `StringEncodingError` if any `String` or the `terminator` cannot be encoded using
   *           `encoding`.
   * - Returns: A `Data` containing the encoded contents of the `Array`.
   */
  func data(using encoding: String.Encoding, terminator: String = "") throws -> Data {
    guard let sepdata = terminator.data(using: encoding) else {
      throw StringEncodingError(string: terminator, targetEncoding: encoding)
    }
    var result = Data()
    for element in self {
      guard let encoded = element.data(using: encoding) else {
        throw StringEncodingError(string: element, targetEncoding: encoding)
      }
      result += encoded
      result += sepdata
    }
    return result
  }
}

extension Array where Element == AnyObject {
  func data() throws -> Data {
    var result = Data()
    for element in self {
      if let element = element as? NSNumber {
        result.append(contentsOf: [element.uint8Value])
      } else if let element = element as? String {
        guard let utf8 = element.data(using: .utf8) else {
          throw StringEncodingError(string: element, targetEncoding: .utf8)
        }
        result += utf8
      }
    }
    return result
  }
}

/**
 * The `BleDeviceInfo` class captures information about detected Blueooth low energy peripherals.
 */
class BleDeviceInfo {
  /**
   *
   */
  let peripheral: CBPeripheral
  let advertisingData: [String: Any]
  let rssi: NSNumber

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

class BleOperation {
  unowned let owner: BluetoothLE
  let caller: String
  let characteristic: CBCharacteristic

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic) {
    self.caller = caller
    self.owner = owner
    self.characteristic = characteristic
  }

  /**
   * Run the operation.
   *
   * - Returns: `true` if the operation has completed. If `false`, the operation MUST call the
   * `runNextOperation` method on the BluetoothLE `owner` when it has finished.
   */
  func run() -> Bool {
    owner._form?.view.window?.makeToast("Object \(self) did not implement the run method.")
    return true
  }
}

class BleReadOperation: BleOperation {
  let notify: Bool

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ notify: Bool) {
    self.notify = notify
    super.init(owner, caller, characteristic)
  }

  override func run() -> Bool {
    guard let peripheral = owner.peripheral else {
      return true
    }
    peripheral.readValue(for: characteristic)
    return false  // Do not run the next operation until the read finishes.
  }

  func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic,
      error: Error?) {
    defer {
      owner.runNextOperation()
    }
    if let error = error {
      // TODO: Report the error
      owner._form?.dispatchErrorOccurredEvent(owner, caller, .ERROR_EXTENSION_ERROR, ERROR_BLE_READ_ERROR, owner, error.localizedDescription)
      return
    }
    if characteristic.isNotifying == notify {
      handle(characteristic.value ?? Data())
    }
  }

  func handle(_ data: Data) {
    // Handled by subclasses
  }
}

class BleReadBytesOperation: BleReadOperation {
  let signed: Bool
  let handler: ([Int32]) -> ()

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ signed: Bool, _ notify: Bool = false, _ handler: @escaping ([Int32]) -> ()) {
    self.signed = signed
    self.handler = handler
    super.init(owner, caller, characteristic, notify)
  }

  override func handle(_ data: Data) {
    self.handler(data.map { Int32($0) - (self.signed ? 128 : 0) })
  }
}

class BleReadShortsOperation: BleReadOperation {
  let signed: Bool
  let handler: ([Int32]) -> ()

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ signed: Bool, _ notify: Bool = false, _ handler: @escaping ([Int32]) -> ()) {
    self.signed = signed
    self.handler = handler
    super.init(owner, caller, characteristic, notify)
  }

  override func handle(_ data: Data) {
    handler(signed ? data.interpret(as: Int16.self) : data.interpret(as: UInt16.self))
  }
}

class BleReadIntegersOperation: BleReadOperation {
  let signed: Bool
  let handler: ([Int]) -> ()

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ signed: Bool, _ notify: Bool = false, _ handler: @escaping ([Int]) -> ()) {
    self.signed = signed
    self.handler = handler
    super.init(owner, caller, characteristic, notify)
  }

  override func handle(_ data: Data) {
    handler(signed ? data.interpret(as: Int32.self) : data.interpret(as: UInt32.self))
  }
}

class BleReadStringsOperation: BleReadOperation {
  let encoding: String.Encoding
  let handler: ([String]) -> ()

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ utf16: Bool, _ notify: Bool = false,
      _ handler: @escaping ([String]) -> ()) {
    encoding = utf16 ? .utf16LittleEndian : .utf8
    self.handler = handler
    super.init(owner, caller, characteristic, notify)
  }

  override func handle(_ data: Data) {
    guard let result = String(data: data, encoding: encoding) else {
      return
    }
    handler([result])
  }
}

class BleWriteOperation: BleOperation {
  let type: CBCharacteristicWriteType
  let data: Data

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ data: Data, _ withResponse: Bool) {
    type = withResponse ? .withResponse : .withoutResponse
    self.data = data
    super.init(owner, caller, characteristic)
  }

  override func run() -> Bool {
    guard let peripheral = owner.peripheral else {
      // TODO: Error
      return true
    }
    peripheral.writeValue(data, for: characteristic, type: type)
    return type == .withoutResponse
  }
}

class BleWriteStringsOperation: BleWriteOperation {
  let strings: [String]
  let withResponse: Bool

  public init(_ owner: BluetoothLE, _ caller: String, _ characteristic: CBCharacteristic, _ utf16: Bool,
              _ values: [String], _ withResponse: Bool, _ nullTerminate: Bool) {
    self.strings = values
    self.withResponse = withResponse
    let encoding: String.Encoding = utf16 ? .utf16LittleEndian : .utf8
    var data = Data()
    for value in values {
      guard let encoded = value.data(using: encoding) else {
        break
      }
      data += encoded
      if nullTerminate {
        data.append(contentsOf: [0])
      }
    }
    super.init(owner, caller, characteristic, data, withResponse)
  }
}

func bleStringToUuid(_ uuidString: String) -> CBUUID? {
  if uuidString.count == 32 {
    let split1 = uuidString.index(uuidString.startIndex, offsetBy: 8)
    let split2 = uuidString.index(uuidString.startIndex, offsetBy: 12)
    let split3 = uuidString.index(uuidString.startIndex, offsetBy: 16)
    let split4 = uuidString.index(uuidString.startIndex, offsetBy: 20)
    let partA = uuidString[..<split1]
    let partB = uuidString[split1..<split2]
    let partC = uuidString[split2..<split3]
    let partD = uuidString[split3..<split4]
    let partE = uuidString[split4...]
    let composedUuid = "\(partA)-\(partB)-\(partC)-\(partD)-\(partE)"
    return CBUUID(string: composedUuid)
  } else if uuidString.count == 4 || uuidString.count == 8 || uuidString.count == 36 {
    return CBUUID(string: uuidString)
  } else {
    return nil
  }
}


/**
 * The `BluetoothLE` component allows users to interact with Bluetooth low energy devices.
 *
 * Theory of operation:
 *
 * The various read/register/write/unregister methods schedule events on the `pendingOperations`
 * queue. If the operation in question is meant to pause, it sets the `operationWaiting` flag and
 * then waits on the `operationPending` semaphore. When a read/write confirmation is reported
 * via the corresponding `CBCentralManagerDelegate` method, it MUST be the case that
 * `operationWaiting` is `true`, in which case the `operationPending` semaphore is signalled to
 * wake the sleeping thread.
 */
@objc public class BluetoothLE : NonvisibleComponent, CBCentralManagerDelegate, CBPeripheralDelegate {
  private var manager: CBCentralManager!
  public private(set) var peripheral: CBPeripheral? = nil
  private var connectingPeripheral: CBPeripheral? = nil
  private var deviceList = [BleDeviceInfo]()
  private var deviceSet = Set<UUID>()
  private var services = [CBUUID: CBService]()
  private var characteristics = [CBUUID: CBCharacteristic]()
  private var descriptors = [CBUUID: CBDescriptor]()
  private var listeners = NSMutableSet()
  private var pendingOperations = DispatchQueue(label: "edu.mit.appinventor.ble.BluetoothLE", qos: .userInitiated)
  private var pendingOperationsByUuid: [CBUUID: [BleOperation]] = [:]
  private var registeredHandlers: [CBUUID: [ObjectIdentifier:(Data) -> ()]] = [:]
  private var lastReadRssi: Int32 = -100
  private var handlers = [CBUUID: NSMutableArray]()
  private var connectionTimer: Timer? = nil
  private var pendingOperationsMutex = DispatchSemaphore(value: 1)
  private var operationWaiting = false
  private var operationPending = DispatchSemaphore(value: 0)

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
    return peripheral?.name ?? ""
  }

  @objc public var ConnectedDeviceRssi: Int32 {
    return lastReadRssi
  }

  @objc public var ConnectionTimeout: Int32 = 0

  @objc public var DeviceCharacteristics: YailList<AnyObject> {
    guard peripheral != nil else {
      return YailList<AnyObject>()
    }

    return YailList<AnyObject>(array: characteristics.values.map({ $0 }))
  }

  @objc public var DeviceList: String {
    return deviceList.map({ $0.description }).joined(separator: ",")
  }

  @objc public var DeviceServices: YailList<AnyObject> {
    guard peripheral != nil else {
      return YailList<AnyObject>()
    }

    return YailList<AnyObject>(array: services.values.map({ $0 }))
  }

  @objc public var IsDeviceAdvertising: Bool {
    return false
  }

  @objc public var IsDeviceConnected: Bool {
    return self.peripheral != nil
  }

  @objc public var NullTerminatedStrings: Bool = false

  @objc public var Scanning: Bool {
    return manager.isScanning
  }

  @objc public var TxPower: Double {
    return 0.0
  }

  // MARK: Methods

  @objc public func Connect(_ index: Int32) {
    guard index >= 1 && index <= deviceList.count else {
      // TODO(ewpatton): Raise an error?
      return
    }
    let deviceInfo = deviceList[Int(index - 1)]
    var options: [String:Any]? = nil
    if AutoReconnect {
      if #available(iOS 17.0, *) {
        options = [
          CBConnectPeripheralOptionEnableAutoReconnect: true
        ]
      } else {
        // Fallback on earlier versions
      }
    }
    manager.connect(deviceInfo.peripheral, options: options)
    if ConnectionTimeout > 0 {
      connectionTimer?.invalidate()
      connectingPeripheral = deviceInfo.peripheral
      connectionTimer = Timer.scheduledTimer(timeInterval: Double(ConnectionTimeout) / 1000.0, target: self, selector: #selector(cancelConnection(_:)), userInfo: nil, repeats: false)
    }
  }

  @objc func cancelConnection(_ sender: Timer) {
    guard let peripheral = connectingPeripheral else {
      return
    }
    manager.cancelPeripheralConnection(peripheral)
    self.connectingPeripheral = nil
  }

  @objc public func Disconnect() {
    guard let peripheral = self.peripheral else {
      // Not connected so nothing to do.
      return
    }
    manager.cancelPeripheralConnection(peripheral)
  }

  @objc public func StartScanning() {
    guard manager.state == .poweredOn else {
      // TODO(ewpatton): Report error to client
      print("Current manager state: \(manager.state)")
      return
    }

    deviceList.removeAll(keepingCapacity: true)
    deviceSet.removeAll(keepingCapacity: true)
    manager.scanForPeripherals(withServices: nil, options: nil)
  }

  @objc public func StopScanning() {
    guard self.manager.isScanning else {
      // TODO(ewpatton): Report error to client
      return
    }

    manager.stopScan()
  }

  @objc public func ReadBytes(_ serviceUuid: String, _ characteristicUuid: String, _ signed: Bool) {
    guard let characteristicUuid = bleStringToUuid(characteristicUuid) else {
      // TODO: Badly formatted UUID
      return
    }
    guard let characteristic = characteristics[characteristicUuid] else {
      // TODO: Characteristic not published
      return
    }
    let op = BleReadBytesOperation(self, "ReadBytes", characteristic, signed) { result in
      DispatchQueue.main.async { [self] in
        self.BytesReceived(serviceUuid, characteristicUuid.uuidString, result)
      }
    }
  }

  @objc public func ReadStrings(_ serviceUuid: String, _ characteristicUuid: String,
      _ utf16: Bool) {
    guard let peripheral = peripheral else {
      // TODO: Disconnected error
      return
    }
    guard let characteristicUuid = bleStringToUuid(characteristicUuid) else {
      // TODO: Badly formatted UUID
      return
    }
    guard let characteristic = characteristics[characteristicUuid] else {
      // TODO: Characteristic not published
      return
    }
    peripheral.readValue(for: characteristic)
  }

  @objc public func RegisterForStrings(_ serviceUuid: String, _ characteristicUuid: String,
                                            _ utf16: Bool) {
    registerForStrings(serviceUuid, characteristicUuid, utf16)
  }

  @objc public func WriteBytes(_ serviceUuid: String, _ characteristicUuid: String,
                               _ signed: Bool, _ values: [AnyObject]) {
    guard let peripheral = peripheral else {
      // TODO: Raise an error
      return
    }
    guard let characteristic = findCharacteristic(serviceUuid, characteristicUuid) else {
      return
    }
    var data = Data()
    for value in values {
      if let number = value as? NSNumber {
        if signed {
          data.append(contentsOf: [UInt8(bitPattern: number.int8Value)])
        } else {
          data.append(contentsOf: [number.uint8Value])
        }
      } else if let string = value as? String, let strdata = string.data(using: .utf8) {
        data.append(strdata)
      }
    }
    schedulePendingOperation(BleWriteOperation(self, "WriteBytes", characteristic, data, false))
  }

  @objc public func WriteBytesWithResponse(_ serviceUuid: String, _ characteristicUuid: String,
                                           _ signed: Bool, _ values: [AnyObject]) {
    guard let peripheral = peripheral else {
      // TODO: Raise an error
      return
    }
    guard let characteristic = findCharacteristic(serviceUuid, characteristicUuid) else {
      return
    }
    var data = Data()
    for value in values {
      if let number = value as? NSNumber {
        if signed {
          data.append(contentsOf: [UInt8(bitPattern: number.int8Value)])
        } else {
          data.append(contentsOf: [number.uint8Value])
        }
      } else if let string = value as? String, let strdata = string.data(using: .utf8) {
        data.append(strdata)
      }
    }
    schedulePendingOperation(BleWriteOperation(self, "WriteBytesWithResponse", characteristic, data, true))
  }

  @objc public func WriteStrings(_ serviceUuid: String, _ characteristicUuid: String,
                                 _ utf16: Bool, _ values: [AnyObject]) {
    guard let peripheral = peripheral else {
      // TODO: Raise an error
      return
    }
    guard let uuid = bleStringToUuid(characteristicUuid) else {
      return
    }
    guard let characteristic = characteristics[uuid] else {
      return
    }

    var data = Data()
    for value in values.toStringArray() {
      if let strdata = value.data(using: utf16 ? .utf16 : .utf8) {
        data.append(strdata)
        if NullTerminatedStrings {
          data.append(contentsOf: [0])
        }
      }
    }

    peripheral.writeValue(data, for: characteristic, type: .withoutResponse)
  }

  private func registerForStrings(_ serviceUuid: String, _ characteristicUuid: String,
                                  _ utf16: Bool, _ handler: (([String])->())? = nil) {
    guard let characteristic = findCharacteristic(serviceUuid, characteristicUuid) else {
      return
    }
    guard let peripheral = peripheral else {
      return
    }
    peripheral.setNotifyValue(true, for: characteristic)
//    schedulePendingOperation(BleReadStringOperation(characteristic, utf16, handler, true))
  }

  private func findCharacteristic(_ serviceUuid: String, _ characteristicUuid: String) -> CBCharacteristic? {
    guard let serviceUuuid = bleStringToUuid(serviceUuid),
          let characteristicUuid = bleStringToUuid(characteristicUuid) else {
      return nil
    }
    return characteristics[characteristicUuid]
  }

  private func schedulePendingOperation(_ operation: BleOperation) {
    pendingOperationsMutex.wait()
    defer {
      pendingOperationsMutex.signal()
    }
    var operationQueue = pendingOperationsByUuid[operation.characteristic.uuid, default: []]
    operationQueue.append(operation)
    pendingOperationsByUuid[operation.characteristic.uuid] = operationQueue
    pendingOperations.async { [self] in
      pendingOperationsMutex.wait()
      let runNext = operation.run()
      if runNext {

      }
      pendingOperationsMutex.signal()
    }
  }


  // MARK: Events

  @objc public func Connected() {
    EventDispatcher.dispatchEvent(of: self, called: "Connected")
    for listener in listeners {
      if let listener = listener as? BluetoothConnectionListener {
        listener.onConnected?(self)
      }
    }
  }

  @objc public func ConnectionFailed(_ reason: String) {
    EventDispatcher.dispatchEvent(of: self, called: "ConnectionFailed", arguments: reason as AnyObject)
  }

  @objc public func DeviceFound() {
    EventDispatcher.dispatchEvent(of: self, called: "DeviceFound")
  }

  @objc public func Disconnected() {
    defer {
      EventDispatcher.dispatchEvent(of: self, called: "Disconnected")
    }
    for listener in listeners {
      if let listener = listener as? BluetoothConnectionListener {
        listener.onDisconnected?(self)
      }
    }
  }

  @objc public func MTUChanged(_ bytes: Int32) {
    EventDispatcher.dispatchEvent(of: self, called: "MTUChanged", arguments: bytes as AnyObject)
  }

  @objc public func RssiChanged(_ rssi: Int32) {
    EventDispatcher.dispatchEvent(of: self, called: "RssiChanged", arguments: rssi as AnyObject)
  }

  @objc public func BytesReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                  _ values: [Int32]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "BytesReceived", arguments: values as AnyObject)
    }
  }

  @objc public func ShortsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                   _ values: [Int32]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "ShortsReceived", arguments: values as AnyObject)
    }
  }

  @objc public func IntegersReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                     _ values: [Int]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "IntegersReceived", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func FloatsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                   _ values: [Float]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FloatsReceived", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func StringsReceived(_ serviceUuid: String, _ characteristicUuid: String,
                                    _ values: [String]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "StringsReceived", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func BytesWritten(_ serviceUuid: String, _ characteristicUuid: String,
                                 _ values: [Int]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "BytesWritten", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func ShortsWritten(_ serviceUuid: String, _ characteristicUuid: String,
                                  _ values: [Int]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "ShortsWritten", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func IntegersWritten(_ serviceUuid: String, _ characteristicUuid: String, _ values: [Int]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "IntegersWritten", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func FloatsWritten(_ serviceUuid: String, _ characteristicUuid: String, _ values: [Float]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "FloatsWritten", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  @objc public func StringsWritten(_ serviceUuid: String, _ characteristicUuid: String, _ values: [String]) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "StringsWritten", arguments: serviceUuid as AnyObject, characteristicUuid as AnyObject, values as NSArray)
    }
  }

  // MARK: Extension interface

  @objc public func addConnectionListener(_ listener: BluetoothConnectionListener) {
    listeners.add(listener)
  }

  @objc public func removeConnectionListener(_ listener: BluetoothConnectionListener) {
    listeners.remove(listener)
  }

  @objc public func ExRegisterForStringValues(_ service: String, _ characteristic: String,
      _ utf16: Bool, _ component: Component, _ handler: @escaping (String, String, [String]) -> Void) {
    guard let characteristic = findCharacteristic(service, characteristic) else {
      // TODO: Report an error
      return
    }
    var entries = registeredHandlers[characteristic.uuid] ?? [:]
    entries[ObjectIdentifier(component)] = { data in
      print("Handling strings from peripheral")
      guard let str = String(data: data, encoding: utf16 ? .utf16LittleEndian : .utf8) else {
        return
      }
      handler(service, characteristic.uuid.uuidString, [str])
    }
    registeredHandlers[characteristic.uuid] = entries
    if entries.count == 1 {
      print("Registered for string values \(characteristic.uuid)")
      peripheral?.setNotifyValue(true, for: characteristic)
    }
  }

  @objc public func ExUnregisterForValues(_ service: String, _ characteristic: String,
      _ component: Component, _ handler: (String, String, [String]) -> Void) {
    // TODO: Implementation
    guard let characteristic = findCharacteristic(service, characteristic) else {
      return
    }
    
  }

  @objc public func ExWriteStringValuesWithResponse(_ service: String, _ characteristic: String,
      _ utf16: Bool, _ message: String, _ handler: (String, String, [String]) -> Void) {
    guard let characteristic = findCharacteristic(service, characteristic) else {
      // TODO: Report the error here
      return
    }
    guard let maxLen = peripheral?.maximumWriteValueLength(for: .withResponse),
          let maxLen2 = peripheral?.maximumWriteValueLength(for: .withoutResponse) else {
      return
    }
    print("max write: \(maxLen)")
    print("max write 2: \(maxLen2)")
    guard let data = message.data(using: utf16 ? .utf16LittleEndian : .utf8) else {
      return
    }
    print("data length: \(data.count)")
    var chunks: [Data] = []
    var start = 0
    for _ in 0..<data.count / maxLen {
      let chunk = data.subdata(in: start..<min(start + maxLen, data.count))
      chunks.append(chunk)
      start += maxLen
    }
    if maxLen > 0 && data.count <= maxLen {
      print("Writing whole data: \(message)")
      peripheral?.writeValue(data, for: characteristic, type: .withResponse)
      return
    }
    for chunk in chunks {
      schedulePendingOperation(BleWriteOperation(self, "ExWriteStringValuesWithResponse", characteristic, chunk, true))
    }
  }

  // MARK: CBCentralManagerDelegate implementation

  public func centralManagerDidUpdateState(_ central: CBCentralManager) {
  }

  public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
    connectionTimer?.invalidate()
    connectionTimer = nil
    self.peripheral = peripheral
    peripheral.delegate = self
    peripheral.discoverServices(nil)
  }

  public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
    self.peripheral = nil
    Disconnected()
  }

  public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
    if !deviceSet.contains(peripheral.identifier) {
      deviceSet.insert(peripheral.identifier)
      deviceList.append(BleDeviceInfo(peripheral, advertisementData, RSSI))
      DeviceFound()
    }
  }

  // MARK: CBPeripheralDelegate implementation

  private var servicesToDiscover: Int = 0

  public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
    if let error = error {
      // TODO(ewpatton): Report error to user
      return
    }
    guard let services = peripheral.services else {
      return
    }
    servicesToDiscover = services.count
    print("Services to discover: \(servicesToDiscover)")
    for service in services {
      peripheral.discoverCharacteristics(nil, for: service)
      self.services[service.uuid] = service
    }
  }

  public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    defer {
      servicesToDiscover -= 1
      print("Services remaining: \(servicesToDiscover)")
      if servicesToDiscover == 0 {
        Connected()
      }
    }
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

  public func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: (any Error)?) {
    guard let error = error else {
      // Success! Nothing to do
      return
    }
    _form?.dispatchErrorOccurredEvent(self, "RegisterForValues", .ERROR_EXTENSION_ERROR, ERROR_OPERATION_NOT_SUPPORTED, error.localizedDescription)
  }

  public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
    print("isNotifying? \(characteristic.isNotifying)")
    if characteristic.isNotifying {
      let serviceUuid = characteristic.service?.uuid.uuidString ?? ""
      let characteristicUuid = characteristic.uuid.uuidString
      guard let data = characteristic.value else {
        print("No data provided")
        return
      }
      guard let str = String(data: data, encoding: .utf8) else {
        print("Unable to decode message")
        return
      }
      StringsReceived(serviceUuid, characteristicUuid, [str])
      guard let callbacks = registeredHandlers[characteristic.uuid] else {
        return
      }
      callbacks.values.forEach { handler in
        DispatchQueue.main.async {
          handler(data)
        }
      }
    } else {
      pendingOperationsMutex.wait()

      // MARK: CRITICAL SECTION
      defer {
        pendingOperationsMutex.signal()
      }

      var operationQueue = pendingOperationsByUuid[characteristic.uuid, default: []]
      guard let pendingOperation = operationQueue.first as? BleReadOperation else {
        return
      }
      operationQueue.removeFirst()
      pendingOperationsByUuid[characteristic.uuid] = operationQueue
      pendingOperation.peripheral(peripheral, didUpdateValueFor: characteristic, error: error)

      // MARK: END CRITICAL SECTION
    }
  }

  public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
    print("Did write characteristic \(characteristic)")

    pendingOperationsMutex.wait()

    // MARK: CRITICAL SECTION
    defer {
      pendingOperationsMutex.signal()
    }

    var operationQueue = pendingOperationsByUuid[characteristic.uuid, default: []]
    guard let pendingOperation = operationQueue.first as? BleWriteOperation else {
      return
    }
    operationQueue.removeFirst()
    pendingOperationsByUuid[characteristic.uuid] = operationQueue
    if !operationQueue.isEmpty {
      DispatchQueue.main.async {
        self.runNextOperation()
      }
    }

    // MARK: END CRITICAL SECTION
  }

  func runNextOperation() {

  }
}
