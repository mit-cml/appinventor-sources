// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import ExternalAccessory

class EvConnection: NSObject, StreamDelegate {
  var _currentSession: EASession?
  var _manager = EAAccessoryManager.shared()
  var _writeBuffer = Array<NSData>()
  var _canWrite = true
  var _stopBeforeDisconnect = true
  var _accessory: EAAccessory?
  /// buffer for the input stream size, with size of 2
  var sizeBuffer = [UInt8](repeating: 0x00, count: 2)
  var _portsValue: UInt8?

  init(_ portsValue: UInt8){
    _portsValue = portsValue
  }
  
  override init(){}

  open var Ports: UInt8 {
    get {
      if let ports = _portsValue {
        return ports
      }
      return UInt8(0)
    }
    set(ports) {
      _portsValue = ports
    }
  }

  func getCurrentAccessory() -> EAAccessory? {
    let currentAccessories = EAAccessoryManager.shared().connectedAccessories
    for accessory in currentAccessories {
      if accessory.protocolStrings.contains(protocolString) {
        return accessory
      }
    }
    return nil
  }

  func start() {
    NotificationCenter.default.addObserver(self, selector: #selector(accessoryConnected), name: NSNotification.Name.EAAccessoryDidConnect, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(accessoryDisconnected), name: NSNotification.Name.EAAccessoryDidDisconnect, object: nil)
    _manager.registerForLocalNotifications()
    
    // If the user already connected to the EV3 before launching the app, the connection is established here. Otherwise, a picker will pop up to allow the user to select an accessory to connect to.
    if let connectedAccessory = getCurrentAccessory() {
      _accessory = connectedAccessory
      connect(connectedAccessory)
    } else {
      // allow user to choose a device to connect to
      _manager.showBluetoothAccessoryPicker(withNameFilter: nil, completion: {(error) in })
    }
  }

  func connect(_ accessory: EAAccessory) {
    _currentSession = EASession(accessory: accessory, forProtocol: protocolString)
    if let session = _currentSession {
      // write data on output stream and then hardware device can send data on input stream
      session.outputStream?.delegate = self
      session.outputStream?.schedule(in: RunLoop.main, forMode: .default)
      session.outputStream?.open()
      
      session.inputStream?.delegate = self
      session.inputStream?.schedule(in: RunLoop.main, forMode: .default)
      session.inputStream?.open()
    }
  }
  
  @objc private func accessoryConnected(notification: NSNotification) {
    let connectedAccessory = notification.userInfo![EAAccessoryKey] as! EAAccessory
    
    if connectedAccessory.protocolStrings.contains(protocolString) {
      _accessory = connectedAccessory
      _currentSession = EASession(accessory: connectedAccessory, forProtocol: protocolString)
      connect(connectedAccessory)
    }
  }
  
  @objc private func accessoryDisconnected(notification: NSNotification) {
    let connectedAccessory = notification.userInfo![EAAccessoryKey] as! EAAccessory
    
    // check if the device is a ev3
    if !connectedAccessory.protocolStrings.contains(protocolString) {
      return
    }
    
    if _stopBeforeDisconnect {
      let command = Ev3Command(commandType: CommandType.directNoReply, globalSize: 0, localSize: 0)
      if let ports = _portsValue {
        command.stopMotor(true, ports)
      }
      write(command)
    }
  }
  
  func clearBuffer() {
    if _writeBuffer.count > MAX_BUFFER_SIZE {
      for _ in 1...MAX_BUFFER_SIZE {
        _writeBuffer.remove(at: 1)
      }
    }
  }
  
  func writeToBuffer() {
    if _writeBuffer.count < 1 {
      return
    }
    
    _canWrite = false
    
    if let session = _currentSession {
      if let stream = session.outputStream {
        print("SESSION IS ALIVE ~ ~ ~ ~ ~ ~~ ~  ~ ~ ~ ~")
        if !stream.hasSpaceAvailable {
          print("error: stream no space available")
          return
        }
      }
    }
    
    let mData = _writeBuffer.remove(at: 0)
    print("Writing data: ")
    print(ByteTools.asHexString(data: mData))
    
    var bytes = mData.bytes.bindMemory(to: UInt8.self, capacity: mData.length)
    
    var bytesLeftToWrite: NSInteger = mData.length
    let bytesWritten = _currentSession?.outputStream?.write(bytes, maxLength: bytesLeftToWrite) ?? -1
    if bytesWritten == -1 {
      _canWrite = true
      print("error while writing data to bt output stream")
      return
    }
    
    bytesLeftToWrite -= bytesWritten
    bytes = bytes.advanced(by: bytesWritten)
    
    if bytesLeftToWrite > 0 {
      _writeBuffer.insert(NSData(bytes: &bytes, length: bytesLeftToWrite), at: 0)
    }
    
    Thread.sleep(forTimeInterval: connSleepTime)
  }
  
  func write(_ command: Ev3Command) {
    print("WRITING!!!!!!!!")
    let data = command.toBytes()
    DispatchQueue(label: "com.ev3ios.connection.queue").async {
      self.clearBuffer()
      self._writeBuffer.append(data)
      if self._canWrite {
        self.writeToBuffer()
      }
    }
  }
  
  func readInBackground(){
    
    let result = _currentSession?.inputStream?.read(&sizeBuffer, maxLength: sizeBuffer.count) ?? 0
    
    if result > 0 {
      // buffer contains result bytes of data to be handled
      let size: Int16 = Int16(sizeBuffer[1]) << 8 | Int16(sizeBuffer[0])
      
      if size > 0 {
        var buffer = [UInt8](repeating: 0x00, count: Int(size))
        let result = _currentSession?.inputStream?.read(&buffer, maxLength: buffer.count) ?? 0
        
        if result < 1 {
          print("error reading the input data with size: \(size)")
          return
        }
        
        print("read data:")
        let hexStr = ByteTools.asHexString(data: NSData(bytes: buffer, length: buffer.count))
        print(hexStr)
        
        reportReceived(report: buffer)
        
      }
      else{
        print("error on input stream: reply size < 1")
      }
      
    } else {
      print("error on input stream, while reading reply size")
    }
  }
  
  func reportReceived(report: [UInt8]){
    DispatchQueue.main.async {
      Ev3ResponseManager.handleResponse(report: report)
    }
  }
  
  public func stream(_ aStream: Stream, handle eventCode: Stream.Event) {
    switch eventCode {
      
    case Stream.Event.hasBytesAvailable:
      DispatchQueue(label: "com.ev3ios.connection.queue").async {
        self.readInBackground()
      }
      print("stream has bytes available")
      break
      
    case Stream.Event.hasSpaceAvailable:
      DispatchQueue(label: "com.ev3ios.connection.queue").async {
        self._canWrite = true
        self.writeToBuffer()
      }
      break
      
    case Stream.Event.openCompleted:
      print("stream opened")
      break
      
    case Stream.Event.errorOccurred:
      print("error on stream")
      break
      
    default:
      print("connection event: \(eventCode.rawValue)")
      break
    }
  }
}
