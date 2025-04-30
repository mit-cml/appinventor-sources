// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import WebRTC

public let kDefaultRendezvousServer = "rendezvous.appinventor.mit.edu"

@objc public protocol WebRTCConnectionDelegate {
  func webRTCDidGetLocalOffer()
  func webRTCDidGetRemoteOffer()
  func webRTCDidGenerateICECandidate()
  func webRTCDataChannelOpened()
}

struct Offer: Codable {
  var sdp: String
  var type: String

  var offerType: RTCSdpType {
    switch type {
    case "offer":
      return .offer
    case "answer":
      return .answer
    case "pr-answer":
      return .prAnswer
    default:
      return .offer
    }
  }

  var description: RTCSessionDescription {
    return RTCSessionDescription(from: self)
  }

  init(from description: RTCSessionDescription) {
    self.sdp = description.sdp
    switch description.type {
    case .offer:
      self.type = "offer"
      break
    case .answer:
      self.type = "answer"
      break
    case .prAnswer:
      self.type = "pr-answer"
      break
    @unknown default:
      self.type = "unknown"
      break
    }
  }
}

extension RTCSessionDescription {
  convenience init(from offer: Offer) {
    self.init(type: offer.offerType, sdp: offer.sdp)
  }
}

struct IceCandidate: Codable {
  var candidate: String
  var sdpMLineIndex: Int32
  var sdpMid: String?

  init(from iceCandidate: RTCIceCandidate) {
    self.sdpMLineIndex = iceCandidate.sdpMLineIndex
    self.sdpMid = iceCandidate.sdpMid
    self.candidate = iceCandidate.sdp
  }
}

extension RTCIceCandidate {
  convenience init(from iceCandidate: IceCandidate) {
    self.init(sdp: iceCandidate.candidate, sdpMLineIndex: iceCandidate.sdpMLineIndex, sdpMid: iceCandidate.sdpMid)
  }
}

struct WebRTCMessage: Codable {
  var offer: Offer?
  var candidate: IceCandidate?
  var nonce: Int?
  var webrtc: Bool
  var key: String
}

@objc class WebRTCNativeManager: NSObject, RTCPeerConnectionDelegate, RTCDataChannelDelegate {
  static var queue = DispatchQueue(label: "WebRTC")
  private let encoder = JSONEncoder()
  private let rendezvousServer: String
  private var rendezvousServer2: String! = nil
  private var iceServers = [RTCIceServer]()
  private var timer: Timer!
  private var keepPolling = true
  private var dataChannel: RTCDataChannel? = nil
  private var seenNonces = Set<Int>()
  private var first = false
  private var rCode = ""
  private unowned var form: ReplForm!
  private var peerConnection: RTCPeerConnection!
  private var random = SystemRandomNumberGenerator()
  private var haveOffer = false
  private var haveLocalDescription = false
  private let constraints: RTCMediaConstraints
  public weak var delegate: WebRTCConnectionDelegate? = nil

  public init(_ rendezvousServer: String, _ rendezvousResult: String) {
    RTCInitializeSSL()
    self.rendezvousServer = rendezvousServer
    constraints = RTCMediaConstraints(mandatoryConstraints: nil, optionalConstraints: nil)
    var rendezvousData = rendezvousResult
    if rendezvousResult.isEmpty || rendezvousResult.starts(with: "OK") {
      rendezvousData = "{\"rendezvous2\" : \"" + kDefaultRendezvousServer + "\"," +
        "\"iceservers\" : " +
        "[{ \"server\" : \"stun:stun.l.google.com:19302\" }," +
         "{ \"server\" : \"turn:turn.appinventor.mit.edu:3478\"," +
           "\"username\" : \"oh\"," +
           "\"password\" : \"boy\"}]}"
    }
    do {
      let resultJson = try JSONSerialization.jsonObject(with: rendezvousData.data(using: .utf8)!) as! [String:Any]
      let server = resultJson["rendezvous2"] as? String
      self.rendezvousServer2 = server
      for item in (resultJson["iceservers"] as! [[String:Any]]) {
        let urls = [item["server"] as! String]
        if let username = item["username"] as? String {
          if let password = item["password"] as? String {
            iceServers.append(RTCIceServer(urlStrings: urls, username: username, credential: password))
          } else {
            iceServers.append(RTCIceServer(urlStrings: urls, username: username, credential: nil))
          }
        } else {
          iceServers.append(RTCIceServer(urlStrings: urls))
        }
      }
    } catch {
      print("Error in WebRTC initialization: \(error)")
    }
    let config = RTCConfiguration()
    config.iceServers = iceServers
    config.sdpSemantics = .unifiedPlan
    config.continualGatheringPolicy = .gatherContinually
    let factory = RTCPeerConnectionFactory()
    peerConnection = factory.peerConnection(with: config, constraints: constraints, delegate: nil)
    super.init()
    peerConnection.delegate = self
  }

  public func initiate(_ form: ReplForm, _ code: String) {
    self.form = form
    form.setWebRTCManager(self)
    rCode = code
    keepPolling = true
    timer = Timer(timeInterval: 1.0, target: self, selector: #selector(poller), userInfo: nil, repeats: true)
    RunLoop.main.add(self.timer, forMode: .default)
  }

  public func stop() {
    peerConnection.close()
  }

  // MARK: RTCPeerConnectionDelegate Implementation

  func peerConnection(_ peerConnection: RTCPeerConnection, didChange stateChanged: RTCSignalingState) {
    print("onSignalingChange: signalingState = \(stateChanged)")
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didAdd stream: RTCMediaStream) {
    // pass
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didRemove stream: RTCMediaStream) {
    // pass
  }

  func peerConnectionShouldNegotiate(_ peerConnection: RTCPeerConnection) {
    // pass
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceConnectionState) {
    // pass
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceGatheringState) {
    print("onIceGatheringChange: iceGatheringState = \(newState)")
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didGenerate candidate: RTCIceCandidate) {
    let response = [
      "nonce": random.next(upperBound: UInt32(100000)),
      "candidate": [
        "candidate": candidate.sdp,
        "sdpMLineIndex": candidate.sdpMLineIndex,
        "sdpMid": candidate.sdpMid
      ] as [String: Any?]
    ] as [String : Any]
    print("Sending ice candidate = \(response)")
    sendRendezvous(data: response) {
      DispatchQueue.main.async {
        self.delegate?.webRTCDidGenerateICECandidate()
      }
    }
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didRemove candidates: [RTCIceCandidate]) {
    // pass
  }

  func peerConnection(_ peerConnection: RTCPeerConnection, didOpen dataChannel: RTCDataChannel) {
    print("opened data channel")
    dataChannel.delegate = self
    self.dataChannel = dataChannel
    DispatchQueue.main.async {
      self.delegate?.webRTCDataChannelOpened()
      self.delegate = nil
    }
    keepPolling = false
    timer.invalidate()
    timer = nil
    seenNonces.removeAll()
  }

  // MARK: RTCDataChannelDelegate Implementation

  func dataChannelDidChangeState(_ dataChannel: RTCDataChannel) {
    // pass
  }

  func dataChannel(_ dataChannel: RTCDataChannel, didReceiveMessageWith buffer: RTCDataBuffer) {
    print("dataChannel: received data channel message")
    if let input = String(data: buffer.data, encoding: .utf8) {
      DispatchQueue.main.async {
        (Form.activeForm as? ReplForm)?.evalScheme(input)
      }
    }
  }

  // MARK: Private Implementation

  @objc func poller() {
    guard keepPolling else {
      timer.invalidate()
      timer = nil
      return
    }
    guard let rendezvousServer2 = rendezvousServer2 else {
      return
    }
    guard let url = URL(string: "https://\(rendezvousServer2)/rendezvous2/\(rCode)-s") else {
      return
    }
    URLSession.shared.dataTask(with: url) { data, response, error in
      guard let data = data, self.keepPolling else {
        return
      }
      let jsonArray = (try? JSONDecoder().decode([WebRTCMessage].self, from: data)) ?? []
      if !self.haveOffer {
        guard let offer = jsonArray.first(where: { $0.offer != nil })?.offer else {
          print("No offer yet...")
          return
        }
        self.haveOffer = true
        DispatchQueue.main.async {
          self.delegate?.webRTCDidGetRemoteOffer()
          print("Have remote offer: \(offer.sdp)")
          self.peerConnection.setRemoteDescription(offer.description) { error in
            if let error = error {
              print("Error setting remote offer: \(error)")
            }
          }
          self.peerConnection.answer(for: self.constraints) { description, error in
            DispatchQueue.main.async {
              if let description = description {
                print("Have local description: \(description.sdp)")
                self.peerConnection.setLocalDescription(description) { error in
                  if let error = error {
                    print("Error setting local answer: \(error)")
                  }
                }
                self.sendAnswer(description)
                self.processCandidates(candidates: jsonArray)
              } else if let error = error {
                print("Error creating answer: \(error)")
              }
            }
          }
        }
      } else if self.haveLocalDescription {
//        self.processCandidates(candidates: jsonArray)
      }
    }.resume()
  }

  private func sendAnswer(_ description: RTCSessionDescription) {
    haveLocalDescription = true
    sendRendezvous(data: ["offer": ["type": "answer", "sdp": description.sdp]]) {
      DispatchQueue.main.async {
        self.delegate?.webRTCDidGetLocalOffer()
      }
    }
  }

  private func processCandidates(candidates: [WebRTCMessage]) {
    candidates.filter({ $0.nonce != nil }).forEach { message in
      guard let nonce = message.nonce, let candidate = message.candidate else {
        return
      }
      guard !seenNonces.contains(nonce) else {
        return
      }
      seenNonces.insert(nonce)
      self.peerConnection.add(RTCIceCandidate(from: candidate)) { error in
        // pass
      }
    }
  }

  open func send(_ output: String) {
    guard let dataChannel = dataChannel else {
      print("No Data Channel in Send")
      return
    }
    if let data = output.data(using: .utf8) {
      dataChannel.sendData(RTCDataBuffer(data: data, isBinary: false))
    }
  }

  open func sendData(_ data: Data, isBinary: Bool = true) {
    guard let dataChannel = dataChannel else {
      print("No Data Channel in Send")
      return
    }
    dataChannel.sendData(RTCDataBuffer(data: data, isBinary: isBinary))
  }

  private func sendRendezvous(data: [String:Any], completion: (() -> ())? = nil) {
    guard let rendezvousServer2 = rendezvousServer2 else {
      print("rendezvousServer2 is nil")
      return
    }
    WebRTCNativeManager.queue.async {
      var dataCopy = data
      dataCopy["first"] = self.first
      dataCopy["webrtc"] = true
      dataCopy["key"] = self.rCode + "-r"
      if self.first {
        self.first = false
        dataCopy["apiversion"] = UIDevice.current.systemVersion
      }
      do {
        var request = try URLRequest(url: "https://\(rendezvousServer2)/rendezvous2/", method: .post)
        request.httpBody = try JSONSerialization.data(withJSONObject: dataCopy)
        URLSession.shared.dataTask(with: request) { data, response, error in
          // pass
          if let data = data {
            print("sendRendezvous response: \(data)")
            completion?()
          } else if let error = error {
            print("sendRendezvous error: \(error)")
          }
        }.resume()
      } catch {
        print("sendRendezvous error: \(error)")
      }
    }
  }
}
