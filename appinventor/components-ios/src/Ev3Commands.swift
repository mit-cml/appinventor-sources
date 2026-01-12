// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit

class Ev3Commands: NonvisibleComponent {
  var _bluetoothClient: BluetoothClient?
  var _connection : EvConnection
  var _firmwareVersion = ""
  
  public override init(_ parent: ComponentContainer) {
    _connection = EvConnection()
    super.init(parent)
    _connection.start()
  }
  
  //MARK: Properties
  /* BluetoothClient is unnecessary for Ev3Motors in iOS. The user just needs to connect
   the phone's bluetooth to the EV3 in settings before launching the app.
   */
  open var BluetoothClient: BluetoothClient {
    get {
      return _bluetoothClient!
    }
    set(client) {
      _bluetoothClient = client
    }
  }
  
  //MARK: Methods
  /* Keep the EV3 brick from shutdown for a period of time.
   */
  open func KeepAlive (_ minutes: Int32) {
    
  }
  
  /* Get the battery voltage.
   */
  open func GetBatteryVoltage () {
    
  }
  
  /* Get the battery current.
   */

  open func GetBatteryCurrent () {
    
  }
  
  /* Get the OS version on EV3.
   */
  open func GetOSVersion () {
    
  }
  
 /* Get the OS build on EV3.
  */
  open func GetOSBuild () {
    
  }
  
  /* Get the firmware version on EV3.
   */
  open func GetFirmwareVersion () -> String {
    getFirmwareVersion({ (version: String?) in
      if let firmware = version {
        print("firmware is: ", firmware)
        self._firmwareVersion = firmware
      }
    })
    // TODO: Currently, an empty string is always returned because the update to _firmwareVersion happens after the value has already been returned.
    return self._firmwareVersion
  }
  
  /* Get the firmware build on EV3.
   */
  open func GetFirmwareBuild () {
    
  }
  
  /* Get the hardware version of EV3.
   */
  open func GetHardwareVersion () {
    
  }
  
  public func getFirmwareVersion(_ receivedFirmware: @escaping (String?) -> Void) {
    let c = Ev3Command(commandType: .directReply, globalSize: 0x10, localSize: 0)
    c.getFirmwareVersion(maxLength: 0x10, index: 0)
    c.response?.responseReceivedCallback = {
      if let data = c.response?.data, let str = String(data: data as Data, encoding: String.Encoding.utf8) {
        receivedFirmware(str)
      } else {
        receivedFirmware(nil)
      }
    }
    _connection.write(c)
  }

}
