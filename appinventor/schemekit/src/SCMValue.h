// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

#include "picrin.h"

@protocol SCMValue<NSObject, NSCopying>

- (BOOL)isPicEqual:(pic_value)other;
- (void)mark;

// Built-in YAIL types
@property (readonly) BOOL isBool;
@property (readonly) BOOL isNumber;
@property (readonly) BOOL isString;
@property (readonly) BOOL isList;
@property (readonly) BOOL isDictionary;
@property (readonly) BOOL isComponent;

@property (readonly) BOOL isNil;
@property (readonly) BOOL isSymbol;
@property (readonly) BOOL isCons;
@property (readonly) BOOL isExact;

@property (readonly) pic_value value;

@end

@interface SCMValue : NSNumber<SCMValue>

@property (class, readonly, nonnull) NSNumber *trueValue;
@property (class, readonly, nonnull) NSNumber *falseValue;
@property (class, readonly, nonnull) SCMValue *nilValue;
+ (nonnull NSNumber *)intValue:(int)value;
+ (nonnull NSNumber *)doubleValue:(double)value;

@end

@interface NSNumber (SCMValueProtocol) <SCMValue>

@end
