// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

#import "SCMSymbol.h"

@interface SCMSymbol () {
  pic_value _symbol;
  pic_state *_state;
  NSString *_name;
}

@end


@implementation SCMSymbol

@synthesize name = _name;

+ (instancetype)symbol:(pic_value)symbol forState:(pic_state *)pic {
  return [[self alloc] initWithSymbol:symbol forState:pic];
}


- (instancetype)initWithSymbol:(pic_value)symbol forState:(pic_state *)pic {
  if (self = [super init]) {
    _symbol = symbol;
    _state = pic;
    _name = [NSString stringWithUTF8String:pic_str(pic, pic_sym_name(pic, symbol))];
  }
  return self;
}


- (NSString *)description {
  return _name;
}

@end
