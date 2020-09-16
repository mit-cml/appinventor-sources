// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

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
