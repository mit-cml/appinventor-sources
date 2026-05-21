// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "OAuth2CallbackHelper.h"

@implementation OAuth2CallbackHelper

+ (void)doCallbackWithDelegate:(id)delegate selector:(SEL)selector authorizer:(id)authorizer request:(NSMutableURLRequest *)request error:(NSError *)error {
  NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[delegate methodSignatureForSelector:selector]];
  invocation.target = delegate;
  invocation.selector = selector;
  [invocation setArgument:&authorizer atIndex:2];
  [invocation setArgument:&request atIndex:3];
  [invocation setArgument:&error atIndex:4];
  [invocation invoke];
}

@end
