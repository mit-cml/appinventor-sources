// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "AppInvHTTPD.h"
#import <GCDWebServer/GCDWebServerDataResponse.h>
#import <GCDWebServer/GCDWebServerDataRequest.h>
#import <GCDWebServer/GCDWebServerURLEncodedFormRequest.h>
#import <AIComponentKit/AIComponentKit-Swift.h>
#import <CommonCrypto/CommonCrypto.h>
#import <CoreFoundation/CoreFoundation.h>
#import <SchemeKit/SchemeKit.h>
#import "RetValManager.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <ifaddrs.h>
#include <arpa/inet.h>

NSString *getInstaller(void);

NSString *getInstaller() {
#if DEBUG
  return @"XCode";
#else
  NSURL *receiptUrl = [NSBundle mainBundle].appStoreReceiptURL;
  if (receiptUrl) {
    return [receiptUrl.path containsString:@"sandboxReceipt"] ? @"TestFlight" : @"App Store";
  } else {
    return @"Not Known";
  }
#endif
}

static NSString *stringFromResult(unsigned char *result, int length) {
  NSMutableString *hash = [[NSMutableString alloc] initWithCapacity:2*length];
  for (int i = 0; i < length; i++) {
    [hash appendFormat:@"%02x", result[i]];
  }
  return [hash copy];
}

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
static NSString *_popup = @"<!DOCTYPE html><html><body><h1>No Page Provided</h1></body></html>";

@implementation AppInvHTTPD

@synthesize interpreter = _interpreter;

+ (void)setHmacKey:(NSString *)key {
  _hmacKey = [key copy];
  _hmacSeq = 1;
}

+ (void)setPopup:(NSString *)popup {
  _popup = [popup copy];
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
  [response setValue:@"true" forAdditionalHeader:@"Access-Control-Allow-Private-Network"];
  return response;
}

- (GCDWebServerResponse *)getVersion:(GCDWebServerRequest *)request {
  UIDevice *device = [UIDevice currentDevice];
  NSDictionary *dict = @{
    @"fingerprint": [NSString stringWithFormat:@"%@/%@:%@", device.model, device.systemName, device.systemVersion],
    @"fqcn": @true,
    @"installer": getInstaller(),
    @"package": @"edu.mit.appinventor.aicompanion3",
    @"version": [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"]
  };
  NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithData:data contentType:@"application/json"];
  return [self setDefaultHeaders:response];
}

- (GCDWebServerResponse *)values:(GCDWebServerRequest *)request {
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:[[RetValManager sharedManager] fetch:YES]];
  response.contentType = @"application/json";
  response.statusCode = 200;
  return [self setDefaultHeaders:response];
}

- (GCDWebServerResponse *)proxy:(GCDWebServerRequest *)request {
  GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithHTML:_popup];
  response.statusCode = 200;
  return [self setDefaultHeaders:response];
}

- (GCDWebServerResponse *)newblocks:(GCDWebServerURLEncodedFormRequest *)request {
  if ([request hasBody]) {
    int iseq = request.arguments[@"seq"] ? ((NSString *)request.arguments[@"seq"]).intValue : 0;
    NSString *yail = request.arguments[@"code"];
    NSString *blockid = request.arguments[@"blockid"];
    NSString *inMac = request.arguments[@"mac"] ? request.arguments[@"mac"] : @"no key provided";
    if (_hmacKey) {
      // Compute the updated HMAC
      CCHmacContext context;
      unsigned char *buf = malloc(CC_SHA1_DIGEST_LENGTH);
      CCHmacInit(&context, kCCHmacAlgSHA1, [_hmacKey cStringUsingEncoding:NSUTF8StringEncoding], _hmacKey.length);
      NSString *intermediate = [NSString stringWithFormat:@"%@%d%@", yail, iseq, blockid];
      const char *cstr = [intermediate cStringUsingEncoding:NSUTF8StringEncoding];
      CCHmacUpdate(&context, cstr, strlen(cstr));
      CCHmacFinal(&context, buf);
      NSString *compMac = stringFromResult(buf, CC_SHA1_DIGEST_LENGTH);
      free(buf);

      // Logging
      NSLog(@"Incoming Mac = %@", inMac);
      NSLog(@"Computed Mac = %@", compMac);
      NSLog(@"Incoming Seq = %d", iseq);
      NSLog(@"Computed Seq = %d", _hmacSeq);
      NSLog(@"blockid = %@", blockid);

      // Does our HMAC match what the browser sends?
      if (![inMac isEqualToString:compMac]) {
        NSLog(@"HMAC does not match");
        [_form dispatchErrorOccurredEventObjC:_form :@"AppInvHTTPD"
                                             :ErrorMessageERROR_REPL_SECURITY_ERROR
                                             :@[@"Invalid HMAC"]];
        return [self error:@"Security Error: Invalid MAC"];
      }

      // Does the sequence match?
      if ((_hmacSeq != iseq) && (_hmacSeq != iseq+1)) {
        NSLog(@"Seq does not match");
        [_form dispatchErrorOccurredEventObjC:_form :@"AppInvHTTPD"
                                             :ErrorMessageERROR_REPL_SECURITY_ERROR
                                             :@[@"Invalid Seq"]];
        return [self error:@"Security Error: Invalid Seq"];
      }
      // Seq Fixup: Sometimes the Companion doesn't increment it's seq if it is in the middle of a
      // project switch so we tolerate an off-by-one here.
      if (_hmacSeq == (iseq+1))
        NSLog(@"Seq Fixup Invoked");
      _hmacSeq = iseq + 1;
    } else {
      NSLog(@"No HMAC key");
      [_form dispatchErrorOccurredEventObjC:_form :@"AppInvHTTPD"
                                           :ErrorMessageERROR_REPL_SECURITY_ERROR
                                           :@[@"No HMAC Key"]];
      return [self error:@"Security Error: No HMAC Key"];
    }
    if (!yail || yail.length == 0) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"No YAIL provided"];
      response.statusCode = 400;
      return [self setDefaultHeaders:response];
    }
    yail = [yail stringByReplacingOccurrencesOfString:@"\\u000a" withString:@"\n"];
    if ([blockid characterAtIndex:0] != '"' || [blockid characterAtIndex:blockid.length - 1] != '"') {
      blockid = [NSString stringWithFormat:@"\"%@\"", blockid];
    }
    if ([yail isEqualToString:@"#f"]) {
      NSLog(@"Skipping evaluation of #f");
    } else {
      yail = [NSString stringWithFormat:@"(process-repl-input %@ (begin %@))", blockid, yail];
      NSOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        NSLog(@"To Eval: %@", yail);
        [self->_interpreter evalForm:yail];
        if (self->_interpreter.exception) {
          [[RetValManager sharedManager] appendReturnValue:[NSString stringWithFormat:@"An internal error occurred: %@ (%@)", self->_interpreter.exception.name, self->_interpreter.exception]
                                                  forBlock:blockid withStatus:@"BAD"];
          [self->_interpreter clearException];
        }
      }];
      // Blocks the web server, but allows us to immediately return a REPL result for "Do It"
      [NSOperationQueue.mainQueue addOperations:@[op] waitUntilFinished:YES];
    }
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
    _interpreter = SCMInterpreter.shared;
    if (_form) {
      [_interpreter setCurrentForm:_form];
    }
    // Quiet the web server logs to INFO, WARN, ERROR
    [GCDWebServer setLogLevel:2];
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
    [self addHandlerForMethod:@"GET" path:@"/_proxy" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest
        *request) {
      return [httpd proxy:request];
    }];
    [self addDefaultHandlerForMethod:@"OPTIONS" requestClass:[GCDWebServerRequest class] processBlock:^GCDWebServerResponse *(__kindof GCDWebServerRequest *request) {
      GCDWebServerDataResponse *response = [GCDWebServerDataResponse responseWithText:@"OK"];
      response.statusCode = 200;
      [response setValue:@"*" forAdditionalHeader:@"Access-Control-Allow-Origin"];
      [response setValue:@"origin, content-type" forAdditionalHeader:@"Access-Control-Allow-Headers"];
      [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Access-Control-Allow-Methods"];
      [response setValue:@"POST,OPTIONS,GET,HEAD,PUT" forAdditionalHeader:@"Allow"];
      [response setValue:@"true" forAdditionalHeader:@"Access-Control-Allow-Private-Network"];
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

- (GCDWebServerResponse *)error:(NSString *)msg {
  return [self setDefaultHeaders:[GCDWebServerDataResponse responseWithJSONObject:@{
      @"status": @"BAD",
      @"message": msg
    }]];
}

@end
