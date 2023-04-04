// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Referencing: https://github.com/andiikaa/ev3ios

import UIKit

let DELAY_MILLISECONDS = 50
let DEFAULT_MOTOR_PORTS = "ABC"
let DEFAULT_WHEEL_DIAMETER = 4.32
let MAX_BUFFER_SIZE = 2
let protocolString = "COM.LEGO.MINDSTORMS.EV3"
let connSleepTime = 0.125
let stringToOutputPort = ["A": OutputPort.A, "B": OutputPort.B, "C": OutputPort.C, "D": OutputPort.D]

class Ev3Motors: NonvisibleComponent {
  var _bluetoothClient: BluetoothClient?
  var _connection : EvConnection
  var _motorPorts = DEFAULT_MOTOR_PORTS
  var _portsValue = OutputPort.All.rawValue
  var _wheelDiameter = DEFAULT_WHEEL_DIAMETER
  var _directionReversed = false
  var _regulationEnabled = true
  var _stopBeforeDisconnect = true
  var _tachoCount: Int32 = 0
  var _tachoCountChangedEventEnabled = false

  public override init(_ parent: ComponentContainer) {
    _connection = EvConnection(_portsValue)
    super.init(parent)
    _connection.start()
  }
  
  //MARK: Properties
  
  /* BluetoothClient is unnecessary for Ev3Motors in iOS. The user just needs to connect
     the phone's bluetooth to the EV3 in settings before launching the app.
   */
  @objc open var BluetoothClient: BluetoothClient {
    get {
      return _bluetoothClient!
    }
    set(client) {
      _bluetoothClient = client
    }
  }
  
  @objc open var MotorPorts: String {
    get {
      return _motorPorts
    }
    set(ports) {
      _motorPorts = ports
      var newValue = UInt8(0)
      for char in _motorPorts {
        let str = String(char)
        newValue += (stringToOutputPort[str]?.rawValue)!
      }
      _portsValue = newValue
      _connection.Ports = newValue
    }
  }
  
  @objc open var WheelDiameter: Double {
    get {
      return _wheelDiameter
    }
    set(diameter) {
      _wheelDiameter = diameter
    }
  }
  
  @objc open var ReverseDirection: Bool {
    get {
      return _directionReversed
    }
    set(reversed) {
      _directionReversed = reversed
    }
  }
  
  @objc open var EnableSpeedRegulation: Bool {
    get {
      return _regulationEnabled
    }
    set(enabled) {
      _regulationEnabled = enabled
    }
  }
  
  @objc open var StopBeforeDisconnect: Bool {
    get {
      return _stopBeforeDisconnect
    }
    set(stop) {
      _stopBeforeDisconnect = stop
    }
  }
  
  @objc open var TachoCountChangedEventEnabled: Bool {
    get {
      return _tachoCountChangedEventEnabled
    }
    set(enabled) {
      _tachoCountChangedEventEnabled = enabled
    }
  }
  
  //MARK: Methods

  /*   Start to rotate the motors.
   */
  @objc open func RotateIndefinitely (_ power: Int32) {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
    if _regulationEnabled {
      command.setOutputPower(power, _portsValue)
    } else {
      command.setOutputSpeed(power, _portsValue)
    }
    command.startMotor(_portsValue)
    _connection.write(command)
  }
  
  /* Rotate the motors in a number of tacho counts.
   */
  @objc open func RotateInTachoCounts (_ power: Int32, _ tachoCounts: Int32, _ useBrake: Bool) {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
    if _regulationEnabled {
      command.setOutputStepSpeed(power, 0, tachoCounts, 0, useBrake, _portsValue)
    } else {
      command.setOutputStepPower(power, 0, tachoCounts, 0, useBrake, _portsValue)
    }
    command.startMotor(_portsValue)
    _connection.write(command)
  }
 
  /*   Rotate the motors in a period of time.
   */
  @objc open func RotateInDuration (_ power: Int32, _ milliseconds: Int32, _ useBrake: Bool) {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
//    command.rotateInDuration(power, milliseconds, useBrake, _portsValue)
    if _regulationEnabled {
      command.setOutputTimeSpeed(power, 0, milliseconds, 0, useBrake, _portsValue)
    } else {
      command.setOutputTimePower(power, 0, milliseconds, 0, useBrake, _portsValue)
    }
    command.startMotor(_portsValue)
    _connection.write(command)
  }

  /*   Rotate the motors in a distance.
   */
  @objc open func RotateInDistance (_ power: Int32, _ distance: Int32, _ useBrake: Bool) {
    let tachoCounts = Int32(Double(distance) * 360.0 / _wheelDiameter / Double.pi)
    RotateInTachoCounts(power, tachoCounts, useBrake)
  }
  
  /*   Start to rotate the motors at the same speed.
   */
  @objc open func RotateSyncIndefinitely (_ power: Int32, _ turnRatio: Int32) {
    if _portsValue != 0 {
      if isOneShotInteger(_portsValue) {
        RotateIndefinitely(power)
      } else {
        let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
        command.setOutputStepSync(power, turnRatio, 0, true, _portsValue)
        command.startMotor(_portsValue)
        _connection.write(command)
      }
    }
  }
  
  func isOneShotInteger(_ value: UInt8) -> Bool {
      return (value != 0) && ((value & ~(value ^ (value - 1))) == 0)
  }
  
  /*    Rotate the motors at the same speed for a distance in cm.
   */
  @objc open func RotateSyncInDistance (_ power: Int32, _ distance: Int32, _ turnRatio: Int32, _ useBrake: Bool) {
    let tachoCounts = Int32(Double(distance) * 360.0 / _wheelDiameter / Double.pi)
    if _portsValue != 0 {
      if isOneShotInteger(_portsValue) {
        RotateInTachoCounts(power, tachoCounts, useBrake)
      } else {
        let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
        command.setOutputStepSync(power, turnRatio, tachoCounts, useBrake, _portsValue)
        command.startMotor(_portsValue)
        _connection.write(command)
      }
    }
  }
  
  /*   Rotate the motors at the same speed in a period of time.
   */
  @objc open func RotateSyncInDuration (_ power: Int32, _ milliseconds: Int32, _ turnRatio: Int32, _ useBrake: Bool) {
    if _portsValue != 0 {
      if isOneShotInteger(_portsValue) {
        RotateInDuration(power, milliseconds, useBrake)
      } else {
        let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
        command.setOutputTimeSync(power, turnRatio, milliseconds, useBrake, _portsValue)
        command.startMotor(_portsValue)
        _connection.write(command)
      }
    }
  }
  
  /*   Rotate the motors at the same speed in a number of tacho counts.
   */
  @objc open func RotateSyncInTachoCounts (_ power: Int32, _ tachoCounts: Int32, _ turnRatio: Int32, _ useBrake: Bool) {
    if _portsValue != 0 {
      if isOneShotInteger(_portsValue) {
        RotateInTachoCounts(power, tachoCounts, useBrake)
      } else {
        let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
        command.setOutputStepSync(power, turnRatio, tachoCounts, useBrake, _portsValue)
        command.startMotor(_portsValue)
        _connection.write(command)
      }
    }
  }
  
  /*   Stop the motors of the robot.
   */
  @objc open func Stop (_ useBrake: Bool) {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
    command.stopMotor(useBrake, _portsValue)
    _connection.write(command)
  }
  
  /*   Toggle the direction of motors.
   */
  @objc open func ToggleDirection () {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
    command.toggleDirection(_portsValue)
    _connection.write(command)
  }
  
  /*  Set the current tacho count to zero.
   */
  @objc open func ResetTachoCount () {
    let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
    command.resetTachoCount(_portsValue)
    _connection.write(command)
  }

  func getTacho(_ receivedTacho: @escaping (Data?) -> Void) {
    let c = Ev3Command(commandType: .directReply, globalSize: 4, localSize: 0)
    let minChar = _motorPorts.sorted().first
    if let min = minChar {
      let minPortValue = stringToOutputPort[String(min)]?.rawValue
      if let port = minPortValue {
        c.getTachoCount(maxLength: 0x10, index: 0, nos: port)
        c.response?.responseReceivedCallback = {
          if let data = c.response?.data {
            receivedTacho(data as Data)
          } else {
            receivedTacho(nil)
          }
        }
        _connection.write(c)
      }
    }
  }

  /*  Get the current tacho count.
   */
  // TODO: Fix because this always returns 0
  @objc open func GetTachoCount () -> Int32 {
    getTacho({ (count: Data?) in
      if let data = count {
        let res = data.scanValue(at: 0, endianess: .LittleEndian) as UInt32
        self._tachoCount = Int32(res)
      } else {
        self._tachoCount = 0
      }
    })
    return _tachoCount
  }
}

extension Data {
  enum Endianness {
    case BigEndian
    case LittleEndian
  }
  func scanValue<T: FixedWidthInteger>(at index: Data.Index, endianess: Endianness) -> T {
    let number: T = self.subdata(in: index..<index + MemoryLayout<T>.size).withUnsafeBytes {
      $0.load(as: T.self)
    }
    switch endianess {
    case .BigEndian:
      return number.bigEndian
    case .LittleEndian:
      return number.littleEndian
    }
  }
}
