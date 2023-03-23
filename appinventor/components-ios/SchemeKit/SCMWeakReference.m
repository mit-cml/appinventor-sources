// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMWeakReference.h"

@interface SCMWeakReference<ObjectType> () {
@private
  __weak ObjectType ref_;
  SCMInterpreter *interpreter_;
}

@end

@implementation SCMWeakReference

+ (instancetype)referenceForObject:(id)object inInterpreter:(SCMInterpreter *)interpreter {
  return [[SCMWeakReference alloc] initWithObject:object inInterpreter:interpreter];
}

- (instancetype)initWithObject:(id)object inInterpreter:(SCMInterpreter *)interpreter {
  if (self = [super init]) {
    ref_ = object;
    interpreter_ = interpreter;
  }
  return self;
}

- (void)clear {
  ref_ = nil;
}

- (id)object {
  return ref_;
}

- (BOOL)isCleared {
  return ref_ == nil;
}

// MARK: SCMValue implementation

- (BOOL)isPicEqual:(pic_value)other {
  return NO;
}

- (pic_value)value {
  return [SCMValue nilValue].value;
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

- (BOOL)isComponent {
  return NO;
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (BOOL)isNil {
  return NO;
}

- (BOOL)isCons {
  return NO;
}

- (void)mark {
  // Nothing to do
}

// MARK: SCMObject implementation

@synthesize interpreter = interpreter_;

// MARK: NSCopying implementation

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

// MARK: Debugging facilities

- (NSString *)debugDescription {
  id ref = ref_;
  if (ref) {
    return [NSString stringWithFormat:@"<Weak reference for %@>", [ref debugDescription]];
  } else {
    return @"<Expired weak reference>";
  }
}

@end
