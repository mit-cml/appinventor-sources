// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "NetworkUtils.h"
#include <ifaddrs.h>
#include <arpa/inet.h>

@implementation NetworkUtils

+ (NSString *)getIPAddress {
  
  NSString *address = @"Error: No Wifi Connection";
  struct ifaddrs *interfaces = NULL;
  struct ifaddrs *temp_addr = NULL;
  int success = 0;
  // retrieve the current interfaces - returns 0 on success
  success = getifaddrs(&interfaces);
  if (success == 0) {
    // Loop through linked list of interfaces
    temp_addr = interfaces;
    while(temp_addr != NULL) {
      if(temp_addr->ifa_addr->sa_family == AF_INET) {
        // Check if interface is an ethernet interface
        if([[NSString stringWithUTF8String:temp_addr->ifa_name] hasPrefix:@"en"]) {
          // Get IP address on interface as NSString (from C string)
          address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)];

          // Only accept a reasonable look IP address
          if (![address isEqualToString:@""] && ![address isEqualToString:@"0.0.0.0"]) {
            break;
          }
          
        }
        
      }
      
      temp_addr = temp_addr->ifa_next;
    }
  }
  // Free memory
  freeifaddrs(interfaces);
  return address;
  
}

@end
