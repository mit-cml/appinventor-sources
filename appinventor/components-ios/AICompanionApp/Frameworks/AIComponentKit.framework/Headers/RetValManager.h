//
//  RetValManager.h
//  AIComponentKit
//
//  Created by Evan Patton on 9/27/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RetValManager : NSObject

+ (instancetype)sharedManager;
- (void)appendReturnValue:(NSString *)item forBlock:(NSString *)blockId withStatus:(NSString *)status;
- (void)sendError:(NSString *)error;
- (void)pushScreen:(NSString *)screenName withValue:(NSObject *)value;
- (void)popScreen:(NSString *)value;
- (NSString *)fetch:(BOOL)block;

@end
