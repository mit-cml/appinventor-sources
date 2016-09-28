//
//  AppInvHTTPD.h
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <GCDWebServer/GCDWebServer.h>

@class ReplForm;

@interface AppInvHTTPD : GCDWebServer

+ (void)setHmacKey:(NSString *)key;
+ (void)resetSeq;
- (instancetype)initWithPort:(NSUInteger)port rootDirectory:(NSString *)wwwroot secure:(BOOL)secure
  forReplForm:(ReplForm *)form;

@end
