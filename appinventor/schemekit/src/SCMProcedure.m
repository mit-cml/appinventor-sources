// mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMProcedure.h"
#import "picrin.h"
#include "picrin/private/object.h"
#include "picrin/private/vm.h"
#import "SCMInterpreter-Private.h"

@interface SCMProcedure () {
@private
  pic_value value_;
  SCMInterpreter *interpreter_;
}

@end

@implementation SCMProcedure

@synthesize interpreter = interpreter_;
@synthesize value = value_;

- (instancetype)initWithProcedure:(pic_value)procedure interpreter:(SCMInterpreter *)interpreter {
  if (self = [super init]) {
    value_ = procedure;
    interpreter_ = interpreter;
  }
  return self;
}

- (BOOL)isPicEqual:(pic_value)other {
  return value_ == other;
}

- (void)mark {
  if (interpreter_) {
    gc_mark(interpreter_.state, value_);
  }
}

// Built-in YAIL types
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

- (BOOL)isNil {
  return NO;
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isCons {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (nonnull id)copyWithZone:(nullable NSZone *)zone {
  return self;
}

- (id<SCMValue>)invoke {
  return [interpreter_ apply:self.value];
}

- (id<SCMValue>)invokeWithArguments:(NSArray<id> *)arguments {
  return [interpreter_ apply:self.value to:arguments];
}

- (NSInteger)numberOfArguments {
  if (pic_type(interpreter_.state, value_) == PIC_TYPE_IREP) {
    struct proc *proc = pic_proc_ptr(interpreter_.state, value_);
    return (NSInteger) proc->u.i.irep->argc - 1;
  }
  return 0;
}

- (BOOL)variadic {
  if (pic_type(interpreter_.state, value_) == PIC_TYPE_IREP) {
    struct proc *proc = pic_proc_ptr(interpreter_.state, value_);
    return proc->u.i.irep->varg;
  }
  return NO;
}

@end
