//
//  SCMSymbol.m
//  SchemeKit
//
//  Created by Evan Patton on 10/29/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "SCMSymbol.h"

@interface SCMSymbol () {
  pic_value _symbol;
  pic_state *_state;
}

@end


@implementation SCMSymbol

+ (instancetype)symbol:(pic_value)symbol forState:(pic_state *)pic {
  return [[self alloc] initWithSymbol:symbol forState:pic];
}


- (instancetype)initWithSymbol:(pic_value)symbol forState:(pic_state *)pic {
  if (self = [super init]) {
    _symbol = symbol;
    _state = pic;
  }
  return self;
}


- (NSString *)description {
  return [NSString stringWithUTF8String:pic_str(_state, pic_sym_name(_state, _symbol))];
}

@end
