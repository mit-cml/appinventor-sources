// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

extern NSString *SCMErrorDomain;
extern NSString *kSCMBadIndex;
extern NSString *kSCMBadValue;

typedef NS_ENUM(NSInteger, SCMErrorCode) {
  SCMErrorCodeUnknown = 1,
  SCMErrorCodeNotImplemented,
  SCMErrorInvalidKey,
  SCMErrorInvalidIndex,
  SCMErrorKeyNotFound,
  SCMErrorIndexOutOfBounds,
  SCMErrorMalformedAlist
};
