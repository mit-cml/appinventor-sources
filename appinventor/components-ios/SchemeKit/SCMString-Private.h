// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import "SCMString.h"

@interface SCMString () {
  SCMInterpreter *interpreter_;
  pic_value value_;
}

- (instancetype)initWithPicString:(pic_value)str fromInterpreter:(SCMInterpreter *)interpreter;

@end
