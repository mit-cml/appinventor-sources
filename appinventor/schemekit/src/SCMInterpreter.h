// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#include "picrin.h"

@protocol SCMValue;
@class SCMSymbol;

@interface SCMInterpreter : NSObject

+ (void)setDefault:(nonnull SCMInterpreter *)defaultInterpreter;
@property (class, readonly) SCMInterpreter * _Nonnull shared;

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
- (void)protect:(nonnull id)object;
- (void)unprotect:(nonnull id)object;

/**
 * Run a garbage collection cycle of the Scheme memory.
 */
- (void)runGC;

#ifdef MEMDEBUG

/**
 * Run the garbage collector and idenitfy at least one strong path from the GC roots to the object.
 *
 * @param object the object of interest
 */
- (void)printGCRoots:(nonnull id)object;

#endif

@property (readonly) NSException * _Nullable exception;

@end
