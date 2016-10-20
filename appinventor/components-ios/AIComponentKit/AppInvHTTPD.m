//
//  AppInvHTTPD.m
//  AIComponentKit
//
//  Created by Evan Patton on 9/17/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "AppInvHTTPD.h"
#import <GCDWebServer/GCDWebServerDataResponse.h>
#import <GCDWebServer/GCDWebServerDataRequest.h>
#import <AIComponentKit/AIComponentKit-Swift.h>
#import <CoreFoundation/CoreFoundation.h>
#import <SchemeKit/SchemeKit.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <ifaddrs.h>
#include <arpa/inet.h>

@interface AppInvHTTPD() {
 @private
  NSString *_wwwroot;
  BOOL _secure;
  ReplForm *_form;
  SCMInterpreter *_interpreter;
}

@end

static NSString *_hmacKey = nil;
static int _hmacSeq = 1;

@implementation AppInvHTTPD

+ (void)setHmacKey:(NSString *)key {
  _hmacKey = [key copy];
  _hmacSeq = 1;
}

+ (void)resetSeq {
  _hmacSeq = 1;
}

- (GCDWebServerResponse *)eval:(GCDWebServerDataRequest *)request {
  if ([request hasBody]) {
    NSString *yail = request.text;
    if (!yail) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"No YAIL provided"];
      response.statusCode = 400;
      return response;
    }
    NSString *result = [_interpreter evalForm:yail];
    if (_interpreter.exception) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"An internal error occurred"];
      response.statusCode = 500;
      return response;
    } else {
      return [GCDWebServerDataResponse responseWithText:result];
    }
  } else {
    GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"No YAIL provided"];
    response.statusCode = 400;
    return response;
  }
}

- (GCDWebServerResponse *)getVersion:(GCDWebServerRequest *)request {
  UIDevice *device = [UIDevice currentDevice];
  NSDictionary *dict = @{
    @"fingerprint": [NSString stringWithFormat:@"%@/%@:%@", device.model, device.systemName, device.systemVersion],
    @"fqcn": @true,
    @"installer": @"unknown",
    @"package": @"edu.mit.appinventor.aicompanion3",
    @"version": @"2.38"
  };
  NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithData:data contentType:@"application/json"];
  [response setValue:@"origin, content-type" forAdditionalHeader:@"Access-Control-Allow-Headers"];
  [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Access-Control-Allow-Methods"];
  [response setValue:@"*" forAdditionalHeader:@"Access-Control-Allow-Origin"];
  [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Allow"];
  return response;
}

- (GCDWebServerResponse *)values:(GCDWebServerRequest *)request {
  return nil;
}

- (GCDWebServerResponse *)newblocks:(GCDWebServerRequest *)request {
  return nil;
}

- (instancetype)initWithPort:(NSUInteger)port rootDirectory:(NSString *)wwwroot secure:(BOOL)secure
       forReplForm:(ReplForm *)form {
  if (self = [super init]) {
    _wwwroot = [wwwroot copy];
    _secure = secure;
    _form = form;
    _interpreter = [[SCMInterpreter alloc] init];
    if (_form) {
      [_interpreter setCurrentForm:_form];
    }
    __weak AppInvHTTPD *httpd = self;
    // AppInvHTTPD paths:
    // * /_newblocks
    // * /_values
    // * /_getversion
    // * /_update or /_install
    // * /_package
    // * method: OPTIONS on any
    // * method: PUT on any
    // * method: GET on any
    [self addHandlerForMethod:@"GET" path:@"/_getversion" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return [httpd getVersion:request];
    }];
    [self addHandlerForMethod:@"GET" path:@"/_values" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return [httpd values:request];
    }];
    [self addHandlerForMethod:@"POST" path:@"/_newblocks" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return [httpd newblocks:request];
    }];
    [self addHandlerForMethod:@"POST" path:@"/_eval" requestClass:[GCDWebServerDataRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerDataRequest *request) {
      return [httpd eval:request];
    }];
    [self addDefaultHandlerForMethod:@"OPTIONS" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return nil;
    }];
    [self addDefaultHandlerForMethod:@"PUT" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return nil;
    }];
    [self startWithPort:port
            bonjourName:[NSString stringWithFormat:@"AI2 Companion on %@",
                                                   [UIDevice currentDevice].name]];
  }
  return self;
}

@end
