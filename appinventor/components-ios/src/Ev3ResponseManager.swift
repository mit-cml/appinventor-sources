// -*- mode: swift; swift-mode:basic-offset: 2; -*-
//  https://github.com/andiikaa/ev3ios
//
//  Created by Andre on 22.04.16.
//  Copyright © 2016 Andre. All rights reserved.
//
import Foundation

class Ev3ResponseManager {
  private static var nextSequence: UInt16 = 0x0001
  private static var responses = [UInt16 : Ev3Response]()
  
  private static func getSequenceNumber() -> UInt16  {
    if nextSequence == UInt16.max{
      nextSequence = nextSequence &+ 1 //unsigned overflow
    }
    nextSequence += 1
    return nextSequence;
  }
  
  static func createResponse() -> Ev3Response {
    let sequence = getSequenceNumber();
    let r = Ev3Response(sequence: sequence);
    responses.updateValue(r, forKey: sequence)
    return r;
  }
  
  static func handleResponse(report: [UInt8]){
    if report.count < 3 {
      return
    }
    
    //let sequence: UInt16 = (ushort) (report[0] | (report[1] << 8));
    
    //TODO seems not that the seqence number is stored le
    let sequence: UInt16 = UInt16(report[1]) << 8 | UInt16(report[0])
    
    if sequence < 1 {
      return
    }
    
    print("received reply for sequence number \(sequence)")
    
    let replyType: UInt8 = report[2]
    
    guard let r = responses[sequence] else {
      print("no item for sequence number \(sequence)")
      return
    }
    
    if let rt = ReplyType(rawValue: replyType){
      r.replyType = rt
    }
    
    if(r.replyType != nil && ( r.replyType == .directReply || r.replyType == .directReplyError)) {
      let tmp = NSData(bytes: report, length: report.count)
      r.data = tmp.subdata(with: NSRange(location: 3, length: report.count - 3)) as NSData?
    }
    else if (r.replyType != nil && (r.replyType == .systemReply || r.replyType == .systemReplyError )){
      if let oc = SystemOpcode(rawValue: report[3]){
        r.systemCommand = oc
      }
      
      if let rs = SystemReplyStatus(rawValue: report[4]){
        r.systemReplyStatus = rs
      }
      
      let tmp = NSData(bytes: report, length: report.count)
      r.data = tmp.subdata(with: NSRange(location: 5, length: report.count - 5)) as NSData?
      
    }
    
    // informes the callback that a response for the command was received
    r.responseReceivedCallback?()
  }
}
