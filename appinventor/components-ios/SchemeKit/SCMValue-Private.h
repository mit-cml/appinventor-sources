// -*- mode: objective-c; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import "SCMValue.h"

@interface SCMValue () {
  pic_value _value;
}

- (instancetype)initWithValue:(pic_value)value;

@end
