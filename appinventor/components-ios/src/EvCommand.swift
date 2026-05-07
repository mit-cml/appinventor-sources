// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Referencing: https://github.com/andiikaa/ev3ios

class Ev3Command {
  var commandType: CommandType
  var buffer: NSMutableData = NSMutableData()
  var globalSize: Int
  var localSize: Int
  var response: Ev3Response?

  init(commandType: CommandType, globalSize: Int, localSize: Int){
    // initialize
    self.commandType = commandType
    self.globalSize = globalSize
    self.localSize = localSize
    
    let response = Ev3ResponseManager.createResponse()
    self.response = response
    
    // header
    buffer.appendUInt16(value: 0xffff)
    buffer.appendUInt16LE(value: response.sequence)
    
    // command
    buffer.appendUInt8(value: commandType.rawValue)
    buffer.appendUInt8(value: UInt8(globalSize & 0xff))
    buffer.appendUInt8(value: UInt8(Int((globalSize >> 8) & 0x03) | localSize << 2))
  }
  
  func getTachoCount(maxLength: UInt8, index: UInt32, nos: UInt8) {
    addOpcode(Opcode.outputGetCount)
    addParameter(UInt8(0x00)) //layer
    addParameter(UInt8(0x02)) // port number
    addParameter(UInt32(0x00)) // supposedly tacho count
    addGlobalIndex(UInt8(index))   // index where buffer begins
  }

  func setOutputPower(_ power: Int32, _ motorsValue: UInt8) {
    // turn motor at power
    addOpcode(Opcode.outputPower)
    addParameter(UInt8(0x00))       // layer
    addParameter(motorsValue)  // ports
    let pwr = ByteTools.firstByteOfInt16(value: Int16(power))
    addParameter(pwr)      // power
  }
  
  func setOutputSpeed(_ speed: Int32, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputSpeed)
    addParameter(UInt8(0x00))       // layer
    addParameter(motorsValue)  // ports
    let speed = ByteTools.firstByteOfInt16(value: Int16(speed))
    addParameter(speed)      // power
  }
  
  func setOutputStepSpeed(_ speed: Int32, _ step1: Int32, _ step2: Int32, _ step3: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputStepSpeed) //16
    addParameter(UInt8(0x00)) //8
    addParameter(motorsValue) //8
    let speed = ByteTools.firstByteOfInt16(value: Int16(speed)) //8
    addParameter(speed)
    addParameter(UInt32(step1)) //32
    addParameter(UInt32(step2)) //32
    addParameter(UInt32(step3)) //32
    addParameter(UInt8(useBrake ? 0x01 : 0x00)) //8
  }
  
  func setOutputStepPower(_ power: Int32, _ step1: Int32, _ step2: Int32, _ step3: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputStepPower)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    let pwr = ByteTools.firstByteOfInt16(value: Int16(power))
    addParameter(pwr)
    addParameter(UInt32(step1))
    addParameter(UInt32(step2))
    addParameter(UInt32(step3))
    addParameter(UInt8(useBrake ? 0x01 : 0x00))
  }

  func setOutputTimeSpeed(_ speed: Int32, _ step1: Int32, _ step2: Int32, _ step3: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputTimeSpeed)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    let sp = ByteTools.firstByteOfInt16(value: Int16(speed))
    addParameter(sp)
    addParameter(UInt32(step1))
    addParameter(UInt32(step2))
    addParameter(UInt32(step3))
    addParameter(UInt8(useBrake ? 0x01 : 0x00))
  }
  
  func addGlobalIndex(_ index: UInt8) {
    // 0xe1 = global index, long format, 1 byte
    buffer.appendUInt8(value: 0xe1);
    buffer.appendUInt8(value: index);
  }
  
   func getFirmwareVersion(maxLength: UInt8, index: UInt32) {
    addOpcode(Opcode.uiRead_GetFirmware)
    addParameter(maxLength)    // global buffer size
    addGlobalIndex(UInt8(index))   // index where buffer begins
  }

  func setOutputTimePower(_ power: Int32, _ step1: Int32, _ step2: Int32, _ step3: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputTimePower)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    let pwr = ByteTools.firstByteOfInt16(value: Int16(power))
    addParameter(pwr)
    addParameter(UInt32(step1))
    addParameter(UInt32(step2))
    addParameter(UInt32(step3))
    addParameter(UInt8(useBrake ? 0x01 : 0x00))
  }
  
  func setOutputStepSync(_ speed: Int32, _ turnRatio: Int32, _ step: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputStepSync)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    let sp = ByteTools.firstByteOfInt16(value: Int16(speed))
    addParameter(sp)
    addParameter(Int16(turnRatio))
    addParameter(UInt32(step))
    addParameter(UInt8(useBrake ? 0x01 : 0x00))
  }
  
  func setOutputTimeSync(_ speed: Int32, _ turnRatio: Int32, _ milliseconds: Int32, _ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputTimeSync)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    let sp = ByteTools.firstByteOfInt16(value: Int16(speed))
    addParameter(sp)
    addParameter(UInt16(turnRatio))
    addParameter(UInt32(milliseconds))
    addParameter(UInt8(useBrake ? 0x01 : 0x00))
  }
  
  func toggleDirection(_ motorsValue: UInt8){
    addOpcode(Opcode.outputPolarity)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
    addParameter(UInt8(0))
  }
  
  func resetTachoCount(_ motorsValue: UInt8) {
    addOpcode(Opcode.outputReset)
    addParameter(UInt8(0x00))
    addParameter(motorsValue)
  }

  func startMotor(_ motorsValue: UInt8) {
    addOpcode(Opcode.outputStart)
    addParameter(UInt8(0x00))             // layer
    addParameter(motorsValue)   // ports
  }
  
  func stopMotor(_ useBrake: Bool, _ motorsValue: UInt8) {
    addOpcode(Opcode.outputStop)
    addParameter(UInt8(0x00))                 // layer
    addParameter(motorsValue)       // ports
    addParameter(UInt8(useBrake ? 0x01 : 0x00))  // brake (0 = coast, 1 = brake)
  }
  
  func addOpcode(_ opcode: Opcode) {
    // 1 or 2 bytes (opcode + subcmd, if applicable)
    // I combined opcode + sub into ushort where applicable, so we need to pull them back apart here
    
    if opcode.rawValue > Opcode.tst.rawValue {
      buffer.appendUInt8(value: UInt8(opcode.rawValue >> 8))
    }
    buffer.appendUInt8(value: UInt8(opcode.rawValue & 0x00ff))
  }
  
  func addParameter(_ parameter: UInt8) {
    buffer.appendUInt8(value: ArgumentSize.byte.rawValue)
    buffer.appendUInt8(value: parameter)
  }

  func addParameter(_ parameter: Int16) {
    buffer.appendUInt8(value: ArgumentSize.short.rawValue)
    buffer.appendInt16LE(value: parameter)
  }
  
  func addParameter(_ parameter: UInt16) {
    buffer.appendUInt8(value: ArgumentSize.short.rawValue)
    buffer.appendUInt16LE(value: parameter)
  }
  
  func addParameter(_ parameter: UInt32) {
    buffer.appendUInt8(value: ArgumentSize.int.rawValue)
    buffer.appendUInt32LE(value: parameter)
  }
  
  func addParameter(_ s: String){
    // 0x84 = long format, null terminated string
    buffer.appendUInt8(value: ArgumentSize.string.rawValue)
    let bytes: [UInt8] = [UInt8](s.utf8)
    buffer.append(bytes, length: bytes.count)
    buffer.appendUInt8(value: 0x00)
  }
  
  func toBytes() -> NSData {
    // size of data, not including the 2 size bytes
    let size = UInt32(buffer.length - 2)
    
    let byteArray = ByteTools.uint32ToUint8Array(value: size)
    
    var msb = UInt8(byteArray[2])
    var lsb = UInt8(byteArray[3])
    
    // little-endian
    buffer.replaceBytes(in: NSRange(location: 0, length: 1), withBytes: &lsb)
    buffer.replaceBytes(in: NSRange(location: 1, length: 1), withBytes: &msb)
    
    return buffer
  }
}

class Ev3Response {
  var replyType: ReplyType?
  var sequence: UInt16
  
  var data: NSData?
  var systemCommand: SystemOpcode?
  var systemReplyStatus: SystemReplyStatus?
  
  var responseReceivedCallback: (() -> Void)?
  
  init(sequence: UInt16){
    self.sequence = sequence
  }
}
