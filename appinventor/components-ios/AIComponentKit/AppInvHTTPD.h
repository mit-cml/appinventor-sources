//
//  AppInvHTTPD.h
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GCDWebServer.h"
#import <AIComponentKit/AIComponentKit-Swift.h>

@interface AppInvHTTPD : GCDWebServer

+ (void)setHmacKey:(NSString *)key;
+ (void)resetSeq;

@end
