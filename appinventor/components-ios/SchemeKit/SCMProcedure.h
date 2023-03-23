// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <SchemeKit/picrin.h>
#import "SCMObject.h"

@class SCMInterpreter;

NS_ASSUME_NONNULL_BEGIN

@interface SCMProcedure : NSObject<SCMObject>

- (instancetype)initWithProcedure:(pic_value)procedure interpreter:(SCMInterpreter *)interpreter;

@end

NS_ASSUME_NONNULL_END
