// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

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

+ (instancetype)trueValue;
+ (instancetype)falseValue;
+ (instancetype)nilValue;
+ (instancetype)intValue:(int)value;
+ (instancetype)doubleValue:(double)value;

@end
