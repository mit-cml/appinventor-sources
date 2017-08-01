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
#import <GCDWebServer/GCDWebServerURLEncodedFormRequest.h>
#import <AIComponentKit/AIComponentKit-Swift.h>
#import <CoreFoundation/CoreFoundation.h>
#import <SchemeKit/SchemeKit.h>
#import "RetValManager.h"
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
static NSString *kMimeJson = @"application/json";

@implementation AppInvHTTPD

@synthesize interpreter = _interpreter;

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
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:[NSString stringWithFormat:@"An internal error occurred: %@ (%@)", _interpreter.exception.name, _interpreter.exception]];
      [_interpreter clearException];
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

- (GCDWebServerResponse *)setDefaultHeaders:(GCDWebServerResponse *)response {
  [response setValue:@"origin, content-type" forAdditionalHeader:@"Access-Control-Allow-Headers"];
  [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Access-Control-Allow-Methods"];
  [response setValue:@"*" forAdditionalHeader:@"Access-Control-Allow-Origin"];
  [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Allow"];
  return response;
}

- (GCDWebServerResponse *)getVersion:(GCDWebServerRequest *)request {
  UIDevice *device = [UIDevice currentDevice];
  NSDictionary *dict = @{
    @"fingerprint": [NSString stringWithFormat:@"%@/%@:%@", device.model, device.systemName, device.systemVersion],
    @"fqcn": @true,
    @"installer": @"unknown",
    @"package": @"edu.mit.appinventor.aicompanion3",
    @"version": @"2.43"
  };
  NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithData:data contentType:@"application/json"];
  return [self setDefaultHeaders:response];
}

- (GCDWebServerResponse *)values:(GCDWebServerRequest *)request {
  sleep(10);
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"{\"status\":\"OK\",\"values\":[]}"];
  response.contentType = @"application/json";
  response.statusCode = 200;
  return [self setDefaultHeaders:response];
}

- (GCDWebServerResponse *)newblocks:(GCDWebServerURLEncodedFormRequest *)request {
  if ([request hasBody]) {
    NSString *yail = request.arguments[@"code"];
    NSString *blockid = request.arguments[@"blockid"];
    if (!yail || yail.length == 0) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"No YAIL provided"];
      response.statusCode = 400;
      return [self setDefaultHeaders:response];
    }
    yail = [yail stringByReplacingOccurrencesOfString:@"\\u000a" withString:@"\n"];
    if ([blockid characterAtIndex:0] != '"' || [blockid characterAtIndex:blockid.length - 1] != '"') {
      blockid = [NSString stringWithFormat:@"\"%@\"", blockid];
    }
    yail = [NSString stringWithFormat:@"(process-repl-input %@ (begin %@))", blockid, yail];
    [NSOperationQueue.mainQueue addOperationWithBlock:^{
      NSLog(@"To Eval: %@", yail);
      [_interpreter evalForm:yail];
      if (_interpreter.exception) {
        [[RetValManager sharedManager] appendReturnValue:[NSString stringWithFormat:@"An internal error occurred: %@ (%@)", _interpreter.exception.name, _interpreter.exception]
                                                forBlock:blockid withStatus:@"BAD"];
        [_interpreter clearException];
      }
    }];
    NSData *result = [[[RetValManager sharedManager] fetch:NO]
                      dataUsingEncoding:NSUTF8StringEncoding];
    return [self setDefaultHeaders:[GCDWebServerDataResponse responseWithData:result
                                                                  contentType:kMimeJson]];
  } else {
    GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"No YAIL provided"];
    response.statusCode = 400;
    return [self setDefaultHeaders:response];
  }
}

- (GCDWebServerResponse *)handlePut:(GCDWebServerDataRequest *)request {
  BOOL error = false;

  if (!request.hasBody) {
    error = true;
  } else {
    NSString *filename = request.query[@"filename"];
    if (filename) {
      if ([filename hasPrefix:@".."] || [filename hasSuffix:@".."] || [filename containsString:@"../"]) {
        error = true;
      } else {
        NSString *targetPath = [[AssetManager shared] pathForPublicAsset:filename];
        NSLog(@"Saving asset to %@", targetPath);
        [request.data writeToFile:targetPath atomically:YES];
      }
    } else {
      error = true;
    }
  }

  GCDWebServerDataResponse *response = nil;
  if (error) {
    response = [GCDWebServerDataResponse responseWithText:@"NOTOK"];
  } else {
    response = [GCDWebServerDataResponse responseWithText:@"OK"];
  }
  return [self setDefaultHeaders:response];
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
    [self addHandlerForMethod:@"POST" path:@"/_values" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      return [httpd values:request];
    }];
    [self addHandlerForMethod:@"POST" path:@"/_newblocks" requestClass:[GCDWebServerURLEncodedFormRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerURLEncodedFormRequest *request) {
      return [httpd newblocks:request];
    }];
    [self addHandlerForMethod:@"POST" path:@"/_eval" requestClass:[GCDWebServerDataRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerDataRequest *request) {
      return [httpd eval:request];
    }];
    [self addDefaultHandlerForMethod:@"OPTIONS" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"OK"];
      response.statusCode = 200;
      [response setValue:@"*" forAdditionalHeader:@"Access-Control-Allow-Origin"];
      [response setValue:@"origin, content-type" forAdditionalHeader:@"Access-Control-Allow-Headers"];
      [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Access-Control-Allow-Methods"];
      [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Allow"];
      return response;
    }];
    [self addDefaultHandlerForMethod:@"PUT" requestClass:[GCDWebServerDataRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerDataRequest *request) {
      return [httpd handlePut:request];
    }];
    [self startWithPort:port
            bonjourName:[NSString stringWithFormat:@"AI2 Companion on %@",
                                                   [UIDevice currentDevice].name]];
  }
  return self;
}

@end
