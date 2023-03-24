// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMMethod.h"

@interface SCMMethod() {
 @private
  Class _class;
  SEL _selector;
  NSString *_fullName;
  NSString *_yailName;
  NSMethodSignature *_signature;
  BOOL _static;
}

@end

@implementation SCMMethod

- (instancetype)initWithMethod:(Method)method forClass:(Class)clazz isStatic:(BOOL)isStatic {
  if (self = [super init]) {
    _static = isStatic;
    _class = clazz;
    _selector = method_getName(method);
    
    // Get Yail method name
    NSString *name = [NSString stringWithUTF8String:sel_getName(_selector)];
    NSArray<NSString *> *parts = [name componentsSeparatedByString:@":"];
    _fullName = name;
    _yailName = parts[0];
    if ([_yailName isEqualToString:@"init"]) {
      if (parts.count == 1) {
        _yailName = name;
      } else {
        NSMutableString *tempName = [NSMutableString stringWithString:parts[0]];
        for (NSUInteger i = 0; i < parts.count - 1; ++i) {
          [tempName appendString:@":"];
        }
        _yailName = [tempName copy];
      }
    }
    
    // Get method signature
    if (isStatic) {
      _signature = [clazz methodSignatureForSelector:_selector];
    } else {
      _signature = [clazz instanceMethodSignatureForSelector:_selector];
    }
  }
  return self;
}

- (NSInvocation *)unboundInvocation {
  NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:_signature];
  invocation.selector = _selector;
  return invocation;
}

- (NSInvocation *)invocationForInstance:(id)target {
  NSInvocation *invocation = nil;
  if (!_static) {
    invocation = [NSInvocation invocationWithMethodSignature:_signature];
    invocation.target = target;
    invocation.selector = _selector;
  }
  return invocation;
}

- (NSInvocation *)staticInvocation {
  NSInvocation *invocation = nil;
  if (_static) {
    invocation = [NSInvocation invocationWithMethodSignature:_signature];
    invocation.target = _class;
    invocation.selector = _selector;
  }
  return invocation;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

@synthesize yailName = _yailName;
@synthesize fullName = _fullName;

- (NSString *)debugDescription {
  return [NSString stringWithFormat:@"SCMMethod %@.%@", NSStringFromClass(_class), _yailName];
}

@end
