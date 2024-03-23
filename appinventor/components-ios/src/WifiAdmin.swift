// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import SystemConfiguration.CaptiveNetwork
import CoreLocation

@objc
open class WiFiAdmin: NonvisibleComponent {
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
  }
  // Enable Wi-Fi (Cannot be implemented)
  @objc func Enable() {
    print("Cannot be implemented")
    openSettings()
  }
  
  // Disable Wi-Fi (Cannot be implemented)
  @objc func Disable() {
    print("Cannot be implemented")
  }
  
  // Toggle Wi-Fi (Cannot be implemented)
  @objc func Toggle() {
    openSettings()
    print("Cannot be implemented")
  }
  
  // Check if Wi-Fi is enabled
  @objc func IsEnabled() {
    print("Cannot be implemented")
  }
  
  // Check if 5GHz is supported (Cannot be implemented)
  @objc func Is5GHzSupported() {
    print("Cannot be implemented")
  }
  
  // Check if Wi-Fi Direct is supported (Cannot be implemented)
  @objc func IsWiFiDirectSupported() {
    print("Cannot be implemented")
  }
  
  // Get IP Address
  @objc func LocalIP() -> String  {
    
    var address: String?
    var ifaddr: UnsafeMutablePointer<ifaddrs>? = nil
    if getifaddrs(&ifaddr) == 0 {
      var ptr = ifaddr
      while ptr != nil {
        defer { ptr = ptr?.pointee.ifa_next }
        
        guard let interface = ptr?.pointee else { return "" }
        let addrFamily = interface.ifa_addr.pointee.sa_family
        if addrFamily == UInt8(AF_INET) || addrFamily == UInt8(AF_INET6) {
          
          // wifi = ["en0"]
          // wired = ["en2", "en3", "en4"]
          // cellular = ["pdp_ip0","pdp_ip1","pdp_ip2","pdp_ip3"]
          
          let name: String = String(cString: (interface.ifa_name))
          if  name == "en0" || name == "en2" || name == "en3" || name == "en4" || name == "pdp_ip0" || name == "pdp_ip1" || name == "pdp_ip2" || name == "pdp_ip3" {
            var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
            getnameinfo(interface.ifa_addr, socklen_t((interface.ifa_addr.pointee.sa_len)), &hostname, socklen_t(hostname.count), nil, socklen_t(0), NI_NUMERICHOST)
            address = String(cString: hostname)
          }
        }
      }
      freeifaddrs(ifaddr)
    }
    return address ?? ""
  }
  
  // Get SSID (Cannot be implemented for all networks)
  @objc func SSID() -> String {
    if let interface = getWiFiInterface(), let ssid = getSSID(for: interface) {
      return ssid
    }
    return ""
  }
  
  // Get BSSID (Cannot be implemented for all networks)
  @objc func BSSID()  {
    print("Cannot be implemented")
  }
  
  // Get Signal Strength (Cannot be implemented for all networks)
  @objc func SignalStrength() -> Int {
    print("Cannot be implemented")
    return 0
  }
  
  // Get Link Speed (Cannot be implemented for all networks)
  @objc func LinkSpeed() -> Int {
    print("Cannot be implemented")
    return 0
  }
  
  // Get Mac Address (Cannot be implemented)
  @objc func MacAddress() {
    print("Cannot be implemented")
  }
  
  // Check if Wi-Fi is connected
  @objc func IsConnected() -> Bool {
    if let interface = getWiFiInterface(), let _ = getSSID(for: interface) {
      return true
    }
    return false
  }
  
  // Helper function to get the Wi-Fi interface
  private func getWiFiInterface() -> String? {
    if let interfaces = CNCopySupportedInterfaces() as? [String], let firstInterface = interfaces.first {
      return firstInterface
    }
    return nil
  }
  
  // Helper function to get SSID
  private func getSSID(for interface: String) -> String? {
    if let dict = CNCopyCurrentNetworkInfo(interface as CFString) as NSDictionary? {
      if let ssid = dict[kCNNetworkInfoKeySSID as String] as? String {
        return ssid
      }
    }
    return nil
  }
  private func openSettings() {
    
    if let url = URL(string: "App-Prefs:root=General") {
      if UIApplication.shared.canOpenURL(url) {
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
      }
    }
  }
}
