// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

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
