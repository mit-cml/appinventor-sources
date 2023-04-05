// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import "SCMValue-Private.h"
#import "picrin.h"
#import <math.h>

static SCMValue *valueNil = nil;
static NSNumber *valueInts[257];
static NSNumber *valueDouble[12];
static NSString *kValueKey = @"value";

@implementation SCMValue

- (instancetype)initWithValue:(pic_value)value {
  if (self = [super init]) {
    self->_value = value;
  }
  return self;
}

- (instancetype)initWithCoder:(NSCoder *)coder {
  if (self = [super init]) {
    self->_value = [coder decodeInt64ForKey:kValueKey];
  }
  return self;
}

@synthesize value = _value;

+ (NSNumber *)trueValue {
  return [NSNumber numberWithBool:TRUE];
}

+ (NSNumber *)falseValue {
  return [NSNumber numberWithBool:FALSE];
}

+ (SCMValue *)nilValue {
  if (valueNil == nil) {
    valueNil = [[self alloc] initWithValue:pic_nil_value(nil)];
  }
  return valueNil;
}

+ (NSNumber *)intValue:(int)i {
  if (i >= 0 && i <= 255) {
    if (!valueInts[i]) {
      valueInts[i] = [NSNumber numberWithInt:i];
    }
    return valueInts[i];
  } else if (i == -1) {
    if (!valueInts[256]) {
      valueInts[256] = [NSNumber numberWithInt:-1];
    }
    return valueInts[256];
  } else {
    return [NSNumber numberWithInt:i];
  }
}

+ (NSNumber *)doubleValue:(double)value {
  if (value == 0.0) {
    if (!valueDouble[0]) {
      valueDouble[0] = [NSNumber numberWithDouble:0.0];
    }
    return valueDouble[0];
  } else if (value == 1.0) {
    if (!valueDouble[1]) {
      valueDouble[1] = [NSNumber numberWithDouble:1.0];
    }
    return valueDouble[1];
  } else if (value == 2.0) {
    if (!valueDouble[2]) {
      valueDouble[2] = [NSNumber numberWithDouble:2.0];
    }
    return valueDouble[2];
  } else if (value == 0.5) {
    if (!valueDouble[3]) {
      valueDouble[3] = [NSNumber numberWithDouble:0.5];
    }
    return valueDouble[3];
  } else if (value == -0.5) {
    if (!valueDouble[4]) {
      valueDouble[4] = [NSNumber numberWithDouble:-0.5];
    }
    return valueDouble[4];
  } else if (value == -2.0) {
    if (!valueDouble[5]) {
      valueDouble[5] = [NSNumber numberWithDouble:-2.0];
    }
    return valueDouble[5];
  } else if (value == -1.0) {
    if (!valueDouble[6]) {
      valueDouble[6] = [NSNumber numberWithDouble:-1.0];
    }
    return valueDouble[6];
  } else if (value == 10.0) {
    if (!valueDouble[7]) {
      valueDouble[7] = [NSNumber numberWithDouble:10.0];
    }
    return valueDouble[7];
  } else if (value == M_PI) {
    if (!valueDouble[8]) {
      valueDouble[8] = [NSNumber numberWithDouble:M_PI];
    }
    return valueDouble[8];
  } else if (value == M_E) {
    if (!valueDouble[9]) {
      valueDouble[9] = [NSNumber numberWithDouble:M_E];
    }
    return valueDouble[9];
  } else if (value == INFINITY) {
    if (!valueDouble[10]) {
      valueDouble[10] = [NSNumber numberWithDouble:INFINITY];
    }
    return valueDouble[10];
  } else if (value == -INFINITY) {
    if (!valueDouble[11]) {
      valueDouble[11] = [NSNumber numberWithDouble:-INFINITY];
    }
    return valueDouble[11];
  } else {
    return [NSNumber numberWithDouble:value];
  }
}

/// MARK: SCMValue Protocol Implementation

- (void)mark {
  // nothing to do for built-in types
}

- (BOOL)isBool {
  return pic_true_p(nil, self->_value) ||
      pic_false_p(nil, self->_value);
}

- (BOOL)isNumber {
  return pic_int_p(nil, self->_value) ||
      pic_float_p(nil, self->_value);
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
  return pic_nil_p(nil, self->_value);
}

- (BOOL)isCons {
  return pic_pair_p(nil, self->_value);
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isExact {
  return pic_int_p(nil, self->_value);
}

- (pic_value)value {
  return self->_value;
}

- (BOOL)isPicEqual:(pic_value)other {
  // TODO: Implementation
  return NO;
}

/// MARK: - NSCoding Implementation

- (void)encodeWithCoder:(NSCoder *)coder {
  [coder encodeInt64:_value forKey:kValueKey];
}

- (id)awakeAfterUsingCoder:(NSCoder *)coder {
  if (pic_true_p(nil, _value)) {
    return [NSNumber numberWithBool:TRUE];
  } else if (pic_false_p(nil, _value)) {
    return [NSNumber numberWithBool:FALSE];
  } else if (pic_nil_p(nil, _value)) {
    return valueNil ? valueNil : (valueNil = self);
  } else if (pic_int_p(nil, _value)) {
    int ival = pic_int(nil, _value);
    if (ival >= 0 && ival <= 255) {
      return valueInts[ival] ? valueInts[ival] : (valueInts[ival] = self);
    } else if (ival == -1) {
      return valueInts[256] ? valueInts[256] : (valueInts[256] = self);
    }
  } else if (pic_float_p(nil, _value)) {
    double dval = pic_float_value(nil, _value);
    if (dval == 0.0) {
      return valueDouble[0] ? valueDouble[0] : (valueDouble[0] = self);
    } else if (dval == 1.0) {
      return valueDouble[1] ? valueDouble[1] : (valueDouble[1] = self);
    } else if (dval == 2.0) {
      return valueDouble[2] ? valueDouble[2] : (valueDouble[2] = self);
    } else if (dval == 0.5) {
      return valueDouble[3] ? valueDouble[3] : (valueDouble[3] = self);
    } else if (dval == -0.5) {
      return valueDouble[4] ? valueDouble[4] : (valueDouble[4] = self);
    } else if (dval == -2.0) {
      return valueDouble[5] ? valueDouble[5] : (valueDouble[5] = self);
    } else if (dval == -1.0) {
      return valueDouble[6] ? valueDouble[6] : (valueDouble[6] = self);
    } else if (dval == 10.0) {
      return valueDouble[7] ? valueDouble[7] : (valueDouble[7] = self);
    } else if (dval == M_PI) {
      return valueDouble[8] ? valueDouble[8] : (valueDouble[8] = self);
    } else if (dval == M_E) {
      return valueDouble[9] ? valueDouble[9] : (valueDouble[9] = self);
    } else if (dval == INFINITY) {
      return valueDouble[10] ? valueDouble[10] : (valueDouble[10] = self);
    } else if (dval == -INFINITY) {
      return valueDouble[11] ? valueDouble[11] : (valueDouble[11] = self);
    }
  }
  return self;
}

/// MARK: - NSCopying Implementation

- (id)copyWithZone:(NSZone *)zone {
  return self;  // SCMValues are immutable
}

/// MARK: - NSNumber Implementation

- (const char *)objCType {
  if (pic_int_p(nil, _value)) {
    return "i";
  } else if (pic_float_p(nil, _value)) {
    return "d";
  } else if (pic_true_p(nil, _value) || pic_false_p(nil, _value)) {
    return [NSNumber numberWithBool:TRUE].objCType;
  } else if (pic_nil_p(nil, _value)) {
    return "v";
  }
  return "d";
}

- (BOOL)boolValue {
  return pic_true_p(nil, _value) ? YES : NO;
}

- (char)charValue {
  if (pic_false_p(nil, _value) || pic_nil_p(nil, _value)) {
    return 0;
  } else if (pic_true_p(nil, _value)) {
    return 1;
  } else if (pic_int_p(nil, _value)) {
    return (char) pic_int(nil, _value);
  } else{
    return (char) pic_float(nil, _value);
  }
}

- (int)intValue {
  return pic_int(nil, _value);
}

- (double)doubleValue {
  return pic_float(nil, _value);
}

- (NSString *)debugDescription {
  if (pic_true_p(nil, _value)) {
    return @"true";
  } else if (pic_false_p(nil, _value)) {
    return @"false";
  } else if (pic_nil_p(nil, _value)) {
    return @"nil";
  } else if (pic_int_p(nil, _value)) {
    return [NSString stringWithFormat:@"%d", pic_int(nil, _value)];
  } else if (pic_float_p(nil, _value)) {
    return [NSString stringWithFormat:@"%f", pic_float(nil, _value)];
  } else {
    return [super debugDescription];
  }
}

@end

@implementation NSNumber (SCMValueProtocol)

- (BOOL)isPicEqual:(pic_value)other {
  return self.value == other;
}

- (void)mark {
  // nothing to do for built-in types
}

- (BOOL)isBool {
  return CFGetTypeID((__bridge CFTypeRef)(self)) == CFBooleanGetTypeID();
}

- (BOOL)isNumber {
  return CFGetTypeID((__bridge CFTypeRef)(self)) != CFBooleanGetTypeID();
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
  return NO;
}

- (BOOL)isCons {
  return NO;
}

- (BOOL)isExact {
  if (self.isNumber) {
    return self.objCType[0] == 'f' || self.objCType[0] == 'd';
  }
  return NO;
}

- (pic_value)value {
  if (self.isBool) {
    return self.boolValue ? pic_true_value(nil) : pic_false_value(nil);
  } else if (self.objCType[0] == 'f' || self.objCType[0] == 'd') {
    return pic_float_value(nil, self.doubleValue);
  } else {
    long long val = self.longLongValue;
    if (val < INT_MIN || val >= INT_MAX) {
      return pic_float_value(nil, val);
    } else {
      return pic_int_value(nil, (int) val);
    }
  }
}

@end
