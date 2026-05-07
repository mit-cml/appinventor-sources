// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface OAuth2CallbackHelper : NSObject

+ (void)doCallbackWithDelegate:(id)delegate
                      selector:(SEL)selector
                    authorizer:(id)authorizer
                       request:(NSMutableURLRequest * _Nullable)request
                         error:(NSError * _Nullable)error;

@end

NS_ASSUME_NONNULL_END
