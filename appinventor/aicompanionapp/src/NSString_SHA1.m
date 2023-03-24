// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonCrypto.h>

@implementation NSString (SHA1)

- (NSString * _Nonnull)sha1 {
  uint8_t hmacBytes[CC_SHA1_DIGEST_LENGTH];
  const char *bytes = self.UTF8String;
  size_t len = strlen(bytes);
  CC_SHA1_CTX context;
  CC_SHA1_Init(&context);
  CC_SHA1_Update(&context, bytes, (CC_LONG)len);
  CC_SHA1_Final(hmacBytes, &context);
  CC_SHA1_Init(&context);
  NSMutableString *sha1 = [NSMutableString stringWithCapacity:2*CC_SHA1_DIGEST_LENGTH];
  for (size_t i = 0; i < CC_SHA1_DIGEST_LENGTH; ++i) {
    [sha1 appendFormat:@"%02x", hmacBytes[i]];
  }
  return sha1;
}

@end
