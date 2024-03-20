// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <GCDWebServer/GCDWebServer.h>

@class ReplForm;
@class SCMInterpreter;
@class PhoneStatus;

@protocol AppInvHTTPDDelegate

- (NSString *)processYAIL:(NSString *)yail;

@end

@interface AppInvHTTPD : GCDWebServer

+ (void)setHmacKey:(NSString *)key;
+ (void)setPopup:(NSString *)popup;
+ (void)resetSeq;
- (instancetype)initWithPort:(NSUInteger)port rootDirectory:(NSString *)wwwroot secure:(BOOL)secure
  forReplForm:(ReplForm *)form;

@property (nonatomic, readonly) SCMInterpreter *interpreter;

@end
