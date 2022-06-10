// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>

@interface RetValManager : NSObject

+ (nonnull instancetype)sharedManager NS_SWIFT_NAME(shared());
- (void)appendReturnValue:(nonnull NSString *)item forBlock:(nonnull NSString *)blockId withStatus:(nonnull NSString *)status;
- (void)sendError:(nonnull NSString *)error;
- (void)pushScreen:(nonnull NSString *)screenName withValue:(nonnull NSObject *)value;
- (void)popScreen:(nonnull NSString *)value;
- (void)assetTransferred:(nonnull NSString *)name;
- (void)extensionLoaded;
- (nonnull NSString *)fetch:(BOOL)block;

@end
