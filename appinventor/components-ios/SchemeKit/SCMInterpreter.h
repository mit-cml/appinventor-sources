//
//  SCMInterpreter.h
//  SchemeKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SCMInterpreter : NSObject

- (NSString * _Nonnull)evalForm:(NSString * _Nonnull)form;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name, ... NS_REQUIRES_NIL_TERMINATION;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name withArgs:(va_list _Null_unspecified)args;
- (id _Nullable)invokeMethod:(NSString * _Nonnull)name withArgArray:(NSArray * _Nonnull)args;
- (void)clearException;
- (void)setCurrentForm:(id _Nonnull)form;

@property (readonly) NSException * _Nullable exception;

@end
