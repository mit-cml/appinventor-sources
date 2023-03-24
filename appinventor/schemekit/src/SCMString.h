// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import "SCMObject.h"
#import "SCMInterpreter.h"

NS_ASSUME_NONNULL_BEGIN

@interface SCMString : NSString<SCMObject>

+ (instancetype)stringFromPicString:(pic_value)string inInterpreter:(SCMInterpreter *)interpreter;
+ (instancetype)stringWithString:(NSString *)string inInterpreter:(SCMInterpreter *)interpreter;
- (instancetype)initWithString:(NSString *)string inInterpreter:(SCMInterpreter *)interpreter;

@end

NS_ASSUME_NONNULL_END
