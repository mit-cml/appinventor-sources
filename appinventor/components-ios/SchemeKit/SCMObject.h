// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>

#import "SCMInterpreter.h"
#import "SCMValue.h"

NS_ASSUME_NONNULL_BEGIN

@protocol SCMObject<SCMValue>

@property (nonnull, readonly) SCMInterpreter *interpreter;

@end

NS_ASSUME_NONNULL_END
