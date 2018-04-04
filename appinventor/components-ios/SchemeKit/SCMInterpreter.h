// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>

@interface SCMInterpreter : NSObject

- (NSString * _Nonnull)evalForm:(NSString * _Nonnull)form;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name, ... NS_REQUIRES_NIL_TERMINATION;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name withArgs:(va_list)args;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name withArgArray:(NSArray * _Nonnull)args;
- (void)clearException;
- (void)setCurrentForm:(id _Nonnull)form;

@property (readonly) NSException * _Nullable exception;

@end
