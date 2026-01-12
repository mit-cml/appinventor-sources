// -*- mode: swift; swift-mode:basic-offset: 2; -*-
//  https://github.com/andiikaa/ev3ios
//
//  Created by Andre on 22.04.16.
//  Copyright © 2016 Andre. All rights reserved.
//

import Foundation


enum ArgumentSize : UInt8
{
  case byte = 0x81  // 1 byte
  case short = 0x82  // 2 bytes
  case int = 0x83    // 4 bytes
  case string = 0x84  // null-terminated string
}

public enum ReplyType : UInt8
{
  case directReply = 0x02
  case systemReply = 0x03
  case directReplyError = 0x04
  case systemReplyError = 0x05
}

enum Opcode : UInt16
{
  case programStart = 0x03
  case programStop = 0x02
  
  case uiRead_GetFirmware = 0x810a
  case uiRead_GetVBatt    = 0x8101
  case uiRead_GetLBatt    = 0x8112
  
  case uiWrite_LED = 0x821b
  
  case uiButton_Pressed = 0x8309
  
  case uiDraw_Update = 0x8400
  case uiDraw_Clean = 0x8401
  case uiDraw_Pixel = 0x8402
  case uiDraw_Line = 0x8403
  case uiDraw_Circle = 0x8404
  case uiDraw_Text = 0x8405
  case uiDraw_FillRect = 0x8409
  case uiDraw_Rect = 0x840a
  case uiDraw_InverseRect = 0x8410
  case uiDraw_SelectFont = 0x8411
  case uiDraw_Topline = 0x8412
  case uiDraw_FillWindow = 0x8413
  case uiDraw_DotLine = 0x8415
  case uiDraw_FillCircle = 0x8418
  case uiDraw_BmpFile = 0x841c
  
  case sound_Break = 0x9400
  case sound_Tone = 0x9401
  case sound_Play = 0x9402
  case sound_Repeat = 0x9403
  case sound_Service = 0x9404
  case sound_Ready = 0x96
  
  case inputDevice_GetTypeMode = 0x9905
  case inputDevice_GetDeviceName = 0x9915
  case inputDevice_GetModeName = 0x9916
  case inputDevice_ReadyPct = 0x991b
  case inputDevice_ReadyRaw = 0x991c
  case inputDevice_ReadySI = 0x991d
  case inputDevice_ClearAll = 0x990a
  case inputDevice_ClearChanges = 0x991a
  
  case inputRead = 0x9a
  case inputReadExt = 0x9e
  case inputReadSI = 0x9d
  
  case outputSetType = 0xa1
  case outputReset = 0xa2
  case outputStop = 0xa3
  case outputPower = 0xa4
  case outputSpeed = 0xa5
  case outputStart = 0xa6
  case outputPolarity = 0xa7
  case outputReady = 0xaa
  case outputStepPower = 0xac
  case outputTimePower = 0xad
  case outputStepSpeed = 0xae
  case outputTimeSpeed = 0xaf
  case outputStepSync = 0xb0
  case outputTimeSync = 0xb1
  case outputClearCount = 0xb2
  case outputGetCount = 0xb3
  
  case file_LoadImage = 0xC008
  
  case timerWait = 0x85
  
  case tst = 0xff
}

enum SystemOpcode : UInt8
{
  case beginDownload = 0x92
  case continueDownload = 0x93
  case closeFileHandle = 0x98
  case createDirectory = 0x9b
  case deleteFile = 0x9c
}

enum SystemReplyStatus : UInt8
{
  case success = 0x00
  case unknownHandle
  case handleNotReady
  case corruptFile
  case noHandlesAvailable
  case noPermission
  case illegalPath
  case fileExists
  case endOfFile
  case sizeError
  case unknownError
  case illegalFilename
  case illegalConnection
}

// The type of command being sent to the brick
public enum CommandType : UInt8
{
  // Direct command with a reply expected
  case directReply = 0x00
  
  // Direct command with no reply
  case directNoReply = 0x80
  
  // System command with a reply expected
  case systemReply = 0x01
  
  // System command with no reply
  case systemNoReply = 0x81
}

// Format for sensor data.
enum Format : UInt8
{
  // Percentage
  case percent = 0x10
  
  // Raw
  case raw = 0x11
  
  /// International System of Units
  case si = 0x12
}

// Polarity/direction to turn the motor
enum Polarity : Int
{
  // Turn backward
  case backward = -1
  
  // Turn in the opposite direction
  case opposite = 0
  
  // Turn forward
  case forward = 1
}

/// Ports which can receive input data
public enum InputPort : UInt8
{
  /// Port 1
  case one = 0x00
  
  /// Port 2
  case two = 0x01
  
  /// Port 3
  case three = 0x02
  
  /// Port 4
  case four = 0x03
  
  /// Port A
  case a  = 0x10
  
  /// Port B
  case b  = 0x11
  
  /// Port C
  case c  = 0x12
  
  /// Port D
  case d  = 0x13
  
  /// makes it possible to loop over this enum as swift provides no functionality for this yet
  static let allValues = [one, two, three, four, a, b, c, d]
}

// Ports which can send output
public struct OutputPort : OptionSet {
  public let rawValue: UInt8
  
  public init(rawValue:UInt8){ self.rawValue = rawValue }
  
  /// Port A
  public static let A = OutputPort(rawValue: 0x01)
  
  /// Port B
  static let B = OutputPort(rawValue: 0x02)
  
  /// Port C
  static let C = OutputPort(rawValue: 0x04)
  
  /// Port D
  static let D = OutputPort(rawValue: 0x08)
  
  /// Ports A,B,C and D simultaneously
  static let All = OutputPort(rawValue: 0x0f)
}

/// List of devices which can be recognized as input or output devices
public enum DeviceType : UInt8
{
  // 2 motors
  /// Large motor
  case lMotor = 7
  
  /// Medium motor
  case mMotor = 8
  
  // Ev3 devices
  /// EV3 Touch sensor
  case touch = 16
  
  /// EV3 Color sensor
  case color = 29
  
  /// EV3 Ultrasonic sensor
  case ultrasonic = 30
  
  /// EV3 Gyroscope sensor
  case gyroscope = 32
  
  /// EV3 IR sensor
  case infrared = 33
  
  // other
  /// Sensor is initializing
  case initializing = 0x7d
  
  /// Port is empty
  case empty = 0x7e
  
  /// Sensor is plugged into a motor port, or vice-versa
  case wrongPort = 0x7f
  
  /// Unknown sensor/status
  case unknown = 0xff
}


/// Buttons on the face of the EV3 brick
public enum BrickButton: UInt8
{
  /// No button
  case none = 0
  
  /// Up button
  case up = 1
  
  /// Enter button
  case enter = 2
  
  /// Down button
  case down = 3
  
  /// Right button
  case right = 4
  
  /// Left button
  case left = 5
  
  /// Back button
  case back = 6
  
  /// Any button
  case anyKey = 7
}

/// Pattern to light up the EV3 brick's LED
public enum LedPattern : UInt8
{
  /// LED off
  case black = 0
  
  /// Solid green
  case green = 1
  
  /// Solid red
  case red = 2
  
  /// Solid orange
  case orange = 3
  
  /// Flashing green
  case greenFlash = 4
  
  /// Flashing red
  case redFlash = 5
  
  /// Flashing orange
  case orangeFlash = 6
  
  /// Pulsing green
  case greenPulse = 7
  
  /// Pulsing red
  case redPulse = 8
  
  /// Pulsing orange
  case orangePulse = 9
}

/// NXT and EV3 Touch Sensor mode
public enum TouchMode : UInt8
{
  /// On when pressed, off when released
  case touch = 0
  
  /// Running counter of number of presses
  case bumps = 1
}

/// NXT Light Sensor mode
public enum NxtLightMode : UInt8
{
  /// Amount of reflected light
  case reflect = 0
  
  /// Amoutn of ambient light
  case ambient = 1
}

/// NXT Sound Sensor mode
public enum NxtSoundMode : UInt8
{
  /// Decibels
  case decibels = 0
  
  /// Adjusted Decibels
  case adjustedDecibels = 1
}

/// NXT Ultrasonic Sensor mode
public enum NxtUltrasonicMode : UInt8
{
  /// Values in centimeter units
  case centimeters = 0
  
  /// Values in inch units
  case inches = 1
}

/// NXT Temperature Sensor mode
public enum NxtTemperatureMode : UInt8
{
  /// Values in Celsius units
  case celsius = 0
  
  /// Values in Fahrenheit units
  case fahrenheit = 1
}

/// Motor mode
public enum MotorMode : UInt8
{
  
  /// Values in degrees
  case degrees = 0
  
  /// Values in rotations
  case rotations = 1
  
  /// Values in percentage
  case percent = 2
}


/// EV3 Color Sensor mode
public enum ColorMode : UInt8
{
  /// Reflected color
  case reflective = 0
  
  /// Ambient color
  case ambient = 1
  
  /// Specific color
  case color = 2
  
  /// Reflected color raw value
  case reflectiveRaw = 3
  
  /// Reflected color RGB value
  case reflectiveRgb = 4
  
  /// Calibration
  case calibration = 5 // TODO: ??
}


/// EV3 Ultrasonic Sensor mode
public enum UltrasonicMode : UInt8
{
  /// Values in centimeter units
  case centimeters = 0
  
  /// Values in inch units
  case inches = 1
  
  /// Listen mode
  case listen = 2
  
  /// Unknown
  case siCentimeters = 3
  
  /// Unknown
  case siInches = 4
  
  /// Unknown
  case dcCentimeters = 5  // TODO: DC?
  /// Unknown
  case dcInches = 6    // TODO: DC?
}

/// EV3 Gyroscope Sensor mode
public enum GyroscopeMode : UInt8
{
  /// Angle
  case angle = 0
  
  /// Rate of movement
  case rate = 1
  
  /// Unknown
  case fas = 2    // TOOD: ??
  /// Unknown
  case gandA = 3  // TODO: ??
  /// Calibrate
  case calibrate = 4
}

/// EV3 Infrared Sensor mode
public enum InfraredMode : UInt8
{
  
  /// Proximity
  case proximity = 0
  
  /// Seek
  case seek = 1
  
  /// EV3 remote control
  case remote = 2
  
  /// Unknown
  case remoteA = 3  // TODO: ??
  /// Unknown
  case salt = 4    // TODO: ??
  ///  Calibrate
  case calibrate = 5
}

/// Values returned by the color sensor
public enum ColorSensorColor
{
  /// Transparent
  case transparent
  
  /// Black
  case black
  
  /// Blue
  case blue
  
  /// Green
  case green
  
  /// Yellow
  case yellow
  
  /// Red
  case red
  
  /// White
  case white
  
  /// Brown
  case brown
}
