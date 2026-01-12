// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMString.h"

@interface SCMString () {
  SCMInterpreter *interpreter_;
  pic_value value_;
}

- (instancetype)initWithPicString:(pic_value)str fromInterpreter:(SCMInterpreter *)interpreter;

@end
