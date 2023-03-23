// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMObjectWrapper.h"
#import "SCMInterpreter-Private.h"

@implementation SCMObjectWrapper

@synthesize value = value_;
@synthesize interpreter = interpreter_;

+ (instancetype)object:(pic_value)value inInterpreter:(SCMInterpreter *)interpreter {
  SCMObjectWrapper *wrapper = [[SCMObjectWrapper alloc] init];
  wrapper->interpreter_ = interpreter;
  wrapper->value_ = value;
  return wrapper;
}

- (BOOL)isBool {
  return NO;
}

- (BOOL)isNumber {
  return NO;
}

- (BOOL)isString {
  return NO;
}

- (BOOL)isList {
  return NO;
}

- (BOOL)isDictionary {
  return NO;
}

- (BOOL)isNil {
  return NO;
}

- (BOOL)isCons {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isComponent {
  return NO;
}

- (BOOL)isPicEqual:(pic_value)other {
  return value_ == other;
}

- (void)mark {
  [interpreter_ mark:value_];
}

// MARK: NSCopying implementation

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

@end
