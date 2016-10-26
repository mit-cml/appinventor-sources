//
//  ASTRecursiveVisitor.m
//  SchemeKit
//
//  Created by Evan Patton on 10/9/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

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
