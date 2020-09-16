// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>
#include "picrin.h"

@protocol SCMValue;
@class SCMSymbol;

@interface SCMInterpreter : NSObject

+ (void)setDefault:(nonnull SCMInterpreter *)defaultInterpreter;
+ (nullable instancetype)default;
- (nonnull NSString *)evalForm:(nonnull NSString *)form;
- (nullable id)invokeMethod:(nonnull NSString *)name, ... NS_REQUIRES_NIL_TERMINATION;
- (nullable id)invokeMethod:(nonnull NSString *)name withArgs:(va_list)args;
- (nullable id)invokeMethod:(nonnull NSString *)name withArgArray:(nonnull NSArray *)args;
- (void)clearException;
- (void)setCurrentForm:(nonnull id)form;
- (void)setValue:(nullable id)value forSymbol:(nonnull NSString *)symname;
- (void)setTimeZone:(nonnull NSTimeZone *)tz;
- (nonnull SCMSymbol *)makeSymbol:(nonnull NSString *)name;
- (nonnull id<SCMValue>)valueForObject:(nullable id)object;

@property (readonly) NSException * _Nullable exception;

@end
