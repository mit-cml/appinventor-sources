// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

#import <Foundation/Foundation.h>
#import <SchemeKit/picrin.h>

@interface SCMSymbol : NSObject

+ (instancetype)symbol:(pic_value)symbol forState:(pic_state *)pic;
- (instancetype)initWithSymbol:(pic_value)symbol forState:(pic_state *)pic;

@property (readonly, copy) NSString *name;

@end
