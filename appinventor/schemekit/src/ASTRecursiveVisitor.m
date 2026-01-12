// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#include "picrin.h"
#import "ASTRecursiveVisitor.h"

@interface ASTRecursiveVisitor () {
 @private
  pic_state *_state;
  id<ASTRecursiveVisitorDelegate> __unsafe_unretained _delegate;
}

@end

@implementation ASTRecursiveVisitor

- (instancetype)initWithPicState:(pic_state *)pic {
  if (self = [super init]) {
    _state = pic;
  }
  return self;
}

- (BOOL)visit:(pic_value)form {
  int type = pic_type(_state, form);
  const char *typename = pic_typename(_state, type);
  NSLog(@"Read object of type %d (%s)", type, typename);
  pic_printf(_state, "~a~%", form);
  return YES;
}

@synthesize delegate = _delegate;

@end
