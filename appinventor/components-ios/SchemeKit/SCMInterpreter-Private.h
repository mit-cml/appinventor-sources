// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMInterpreter.h"
#import "picrin.h"

@protocol SCMObject;

@interface SCMInterpreter () {
 @private
  pic_state *_pic;
  NSException *exception_;
  NSMutableDictionary<NSString *, SCMSymbol *> *symbolTable_;
  NSMutableArray *protected_;
}

- (pic_value)wrapObject:(nullable id<SCMValue>)object;
- (pic_value)internObject:(nonnull id<SCMObject>)object;
- (nullable id<SCMValue>)unwrapValue:(pic_value)value;
- (pic_value)makeConsWithCar:(nullable id<SCMValue>)car cdr:(nullable id<SCMValue>)cdr;
- (void)mark;

/**
 * Mark a picrin value as in-use during the mark phase of a garbage collection cycle.
 */
- (void)mark:(pic_value)value;

@property (readonly) pic_state * _Nonnull state;

@end
