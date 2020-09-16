// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>
#import "SCMObject.h"
#import "SCMInterpreter.h"

NS_ASSUME_NONNULL_BEGIN

@interface SCMObjectWrapper : NSObject<SCMObject> {
  @private
  SCMInterpreter *interpreter_;
  pic_value value_;
}

+ (instancetype)object:(pic_value)value inInterpreter:(SCMInterpreter *)interpreter;

@end

NS_ASSUME_NONNULL_END
