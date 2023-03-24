// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMSymbol.h"
#import "SCMInterpreter-Private.h"


static NSString *kNameKey = @"name";


@interface SCMSymbol () {
  pic_value _symbol;
  SCMInterpreter *_interpreter;
  NSString *_name;
}

@end


@implementation SCMSymbol

@synthesize name = _name;

+ (instancetype)symbol:(pic_value)symbol inInterpreter:(SCMInterpreter *)interpreter {
  return [[self alloc] initWithSymbol:symbol inInterpreter:interpreter];
}

- (instancetype)initWithSymbol:(pic_value)symbol inInterpreter:(SCMInterpreter *)interpreter {
  if (self = [super init]) {
    _symbol = symbol;
    _interpreter = interpreter;
    _name = [NSString stringWithUTF8String:pic_str(_interpreter.state, pic_sym_name(_interpreter.state, symbol))];
  }
  return self;
}


- (NSString *)description {
  return [NSString stringWithFormat:@"'%@", _name];
}


/// MARK: SCMValue implementation

@synthesize value = _symbol;

- (BOOL)isBool {
  return NO;
}

- (BOOL)isNumber {
  return NO;
}

- (BOOL)isString {
  return NO;
}

- (BOOL)isList {
  return NO;
}

- (BOOL)isDictionary {
  return NO;
}

- (BOOL)isComponent {
  return NO;
}

- (BOOL)isNil {
  return NO;
}

- (BOOL)isSymbol {
  return YES;
}

- (BOOL)isCons {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (BOOL)isPicEqual:(pic_value)other {
  if (pic_sym_p(_interpreter.state, other)) {
    return other == _symbol;
  }
  return NO;
}

- (BOOL)isEqual:(id)object {
  if ([object isKindOfClass:[SCMSymbol class]]) {
    return [self isPicEqual:((SCMSymbol *) object)->_symbol];
  }
  return NO;
}

- (NSUInteger)hash {
  return [_name hash];
}

- (void)mark {
  [_interpreter mark:_symbol];
}

/// MARK: - SCMObject Implementation

@synthesize interpreter = _interpreter;

/// MARK: - NSCopying Implementation

- (id)copyWithZone:(NSZone *)zone {
  return self;  // Symbols are immutable
}

/// MARK: - NSCoding Implementation

- (instancetype)initWithCoder:(NSCoder *)coder {
  if (self = [super init]) {
    _name = [coder decodeObjectForKey:kNameKey];
  }
  return self;
}

- (id)awakeAfterUsingCoder:(NSCoder *)coder {
  return [SCMInterpreter.shared makeSymbol:_name];
}

- (void)encodeWithCoder:(NSCoder *)coder {
  [coder encodeObject:_name forKey:kNameKey];
}

@end
