// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>
#import <SchemeKit/picrin.h>
#import "SCMObject.h"

@class SCMInterpreter;

NS_ASSUME_NONNULL_BEGIN

@interface SCMProcedure : NSObject<SCMObject>

- (instancetype)initWithProcedure:(pic_value)procedure interpreter:(SCMInterpreter *)interpreter;

@end

NS_ASSUME_NONNULL_END
