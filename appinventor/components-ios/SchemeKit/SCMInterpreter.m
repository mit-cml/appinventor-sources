// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "SCMInterpreter-Private.h"
#include "picrin/extra.h"
#include "picrin/private/object.h"
#import "SCMMethod.h"
#import "SCMValue-Private.h"
#import "SCMTypes.h"
#import "SCMObjectWrapper.h"
#import "yail.h"

// extern functions
pic_value yail_make_native_class(pic_state *, Class);
pic_value yail_make_native_method(pic_state *, SCMMethod *);
pic_value yail_make_native_instance(pic_state *, id);
id yail_to_native(pic_state *, pic_value);
void yail_set_time_zone(NSTimeZone *tz);

NSString *SCMErrorDomain = @"SchemeKit";
NSString *kSCMBadIndex = @"index";
NSString *kSCMBadValue = @"value";

extern char *picrin_native_stack_start;

void
pic_init_picrin(pic_state *pic)
{
  void pic_init_contrib(pic_state *);
  void pic_load_piclib(pic_state *);
  
  pic_init_contrib(pic);
  pic_load_piclib(pic);
  pic_init_yail(pic);
}

static NSException *
exception_from_pic_error(pic_state *pic, pic_value e) {
  const char *msg, *buffer;
  char *msgcopy = NULL, *bufcopy = NULL;
  pic_value e2, port, irrs = pic_error_ptr(pic, e)->irrs;
  int buflen = 1024;
  pic_try {
    msg = pic_str(pic, pic_obj_value(pic_error_ptr(pic, e)->msg));
    msgcopy = (char *)malloc(strlen(msg) + 1);
    strcpy(msgcopy, msg);
    port = pic_fmemopen(pic, NULL, buflen, "w");
    pic_fprintf(pic, port, "~a\0", irrs);
    pic_fgetbuf(pic, port, &buffer, &buflen);
    bufcopy = (char *)malloc(buflen + 1);
    strncpy(bufcopy, buffer, buflen);  // Picrin may GC, so make a clean copy before we exit this scope
    bufcopy[buflen] = '\0';
  } pic_catch(e2) {
    NSLog(@"WTF");
  }
  NSException *result = [NSException exceptionWithName:@"RuntimeError" reason:[NSString stringWithFormat:@"%s. Irritants: %s", msgcopy, bufcopy] userInfo:nil];
  free(msgcopy);
  free(bufcopy);
  return result;
}

@interface NSString (NSStringFromBuffer)

+ (nonnull instancetype)stringFromBuffer:(const char *)buffer withLength:(size_t)length;

@end

@implementation NSString (NSStringFromBuffer)

+ (nonnull instancetype)stringFromBuffer:(const char *)buffer withLength:(size_t)length {
  char *cbuffer = (char *) malloc(length + 1);
  memcpy(cbuffer, buffer, length);
  cbuffer[length] = 0;
  id result = [self stringWithUTF8String:cbuffer];
  free(cbuffer);
  return result;
}

@end

@interface SCMInterpreter ()
@end

static SCMInterpreter *_defaultInterpreter = nil;

@implementation SCMInterpreter

@synthesize exception = exception_;

+ (void)setDefault:(SCMInterpreter *)defaultInterpreter {
  _defaultInterpreter = defaultInterpreter;
}

+ (instancetype)shared {
  if (!_defaultInterpreter) {
    _defaultInterpreter = [[SCMInterpreter alloc] init];
  }
  return _defaultInterpreter;
}

- (pic_value)picValueForObjCValue:(id)value {
  if (value == nil) {
    return pic_nil_value(self->_pic);
  } else if ([[value class] conformsToProtocol:@protocol(SCMValue)]) {
    return [(id<SCMValue>)value value];
  } else if ([value isKindOfClass:[NSNumber class]]) {
    NSNumber *number = (NSNumber *)value;
    char c = number.objCType[0];
    switch(c) {
      case 'c':
        // Technically 'c' is a char, but NSNumber encodes booleans as chars.
        // Other integers will be stored as ints or longs, so we should be safe
        // to assume that if something is a char it's actually a boolean value.
        return number.boolValue ? pic_true_value(_pic) : pic_false_value(_pic);
      case 's':
      case 'i':
      case 'l':
        if (c != 'l' || sizeof(long) == sizeof(int)) {
          return pic_int_value(_pic, number.intValue);
        }
      case 'q':
        return pic_float_value(_pic, number.doubleValue);
      case 'C':
      case 'S':
      case 'I':
      case 'L':
        if (number.unsignedIntegerValue <= UINT32_MAX) {
          return pic_int_value(_pic, number.intValue);
        }
      case 'Q':
        return pic_float_value(_pic, number.doubleValue);
      case 'B':
        if (number.boolValue) {
          return pic_true_value(_pic);
        } else {
          return pic_false_value(_pic);
        }
        break;
      case 'f':
      case 'd':
        return pic_float_value(_pic, number.doubleValue);
      default:
        NSLog(@"Unknown NSNumber type: %c", c);
        break;
    }
  } else if ([value isKindOfClass:[NSString class]]) {
    const char *buf = [(NSString *)value UTF8String];
    return pic_str_value(_pic, buf, (int)strlen(buf));
  } else if ([value isKindOfClass:[NSArray class]]) {
    NSArray *items = (NSArray *)value;
    if (items.count == 0) {
      // special treatment of empty args list
      return pic_nil_value(_pic);
    }
    pic_value *values = (pic_value *) malloc(sizeof(pic_value) * items.count);
    for (NSUInteger i = 0 ; i < items.count ; ++i ) {
      values[i] = [self picValueForObjCValue:items[i]];
    }
    pic_value list = pic_make_list(_pic, (int) items.count, values);
    free(values);
    return list;
  }
  return yail_make_native_instance(_pic, value);
}

- (instancetype)init {
  if (self = [super init]) {
    pic_state *pic = _pic = pic_open(pic_default_allocf, NULL);
    pic_value e;
    exception_ = nil;
    symbolTable_ = [[NSMutableDictionary alloc] init];
    protected_ = [[NSMutableArray alloc] init];
    pic_try {
      pic_init_picrin(pic);
      SCMSymbol *listHeader = [[SCMSymbol alloc] initWithSymbol:pic_intern(_pic, pic_cstr_value(_pic, "*list*")) inInterpreter:self];
      [symbolTable_ setValue:listHeader forKey:@"*list*"];
      pic_value selfref = yail_make_native_instance(pic, self);
      pic_protect(pic, selfref);  // Protect the interpreter's state from GC
    } pic_catch(e) {
      exception_ = exception_from_pic_error(pic, e);
    }
  }
  return self;
}

- (void)dealloc {
  pic_close(_pic);
}

- (nonnull NSString *)evalForm:(NSString *)form {
  char t;
  pic_state *pic = _pic;
  pic_value port, program, result, e, resultport;
  int buflen = 64;
  const char *yail = form.UTF8String;
  const char *buffer;
  NSString *response = nil;
  picrin_native_stack_start = &t;
  _defaultInterpreter = self;
  pic_try {
    port = pic_fmemopen(pic, yail, strlen(yail), "r");
    size_t ai = pic_enter(pic);
    while (! pic_eof_p(pic, (program = pic_read(pic, port)))) {
      result = pic_eval(pic, program, "yail");
      buflen = 64;
      resultport = pic_fmemopen(pic, NULL, buflen, "w");
      pic_fprintf(pic, resultport, "~a\0", result);
      pic_fgetbuf(pic, resultport, &buffer, &buflen);
      if (response) {
        response = [NSString stringWithFormat:@"%@\n%@", response, [NSString stringWithUTF8String:buffer]];
      } else {
        response = [NSString stringWithUTF8String:buffer];
      }
      pic_fclose(pic, resultport);
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
  return response != nil ? response : @"";
}

- (id)invokeMethod:(NSString *)name withPicArgs:(NSArray<NSNumber *> *)arguments {
  char t;
  pic_value *nativeargs = (pic_value *)malloc(sizeof(pic_value) * arguments.count);
  int argcount = 0;
  for (NSNumber *v in arguments) {
    nativeargs[argcount++] = (pic_value)v.unsignedLongLongValue;
  }
  pic_state *pic = _pic;
  pic_value result, e;
  const char *name_cstr = name.UTF8String;
  picrin_native_stack_start = &t;
  id objcResult = nil;
  _defaultInterpreter = self;
  pic_try {
    size_t ai = pic_enter(pic);
    result = pic_apply(_pic, pic_ref(pic, "yail", name_cstr), (int)arguments.count, nativeargs);
    objcResult = yail_to_native(pic, result);
    pic_leave(pic, ai);
  } pic_catch(e) {
    exception_ = exception_from_pic_error(_pic, e);
  }
  return objcResult;
}

- (id)invokeMethod:(NSString *)name withArgs:(va_list)args {
  NSMutableArray *pic_args = [NSMutableArray array];
  for (id<NSObject> arg = va_arg(args, id); arg != nil; arg = va_arg(args, id)) {
    [pic_args addObject:[NSNumber numberWithUnsignedLongLong:[self picValueForObjCValue:arg]]];
  }
  return [self invokeMethod:name withPicArgs:pic_args];
}

- (id)invokeMethod:(NSString *)name, ... {
  va_list args;
  va_start(args, name);
  id result = [self invokeMethod:name withArgs:args];
  va_end(args);
  return result;
}

- (id)invokeMethod:(NSString *)name withArgArray:(NSArray *)args {
  NSMutableArray *pic_args = [NSMutableArray arrayWithCapacity:args.count];
  for (id arg in args) {
    [pic_args addObject:[NSNumber numberWithUnsignedLongLong:[self picValueForObjCValue:arg]]];
  }
  return [self invokeMethod:name withPicArgs:pic_args];
}

- (void)clearException {
  exception_ = nil;
}

- (void)setCurrentForm:(id)form {
  pic_state *pic = _pic;
  pic_value e;
  _defaultInterpreter = self;
  size_t ai = pic_enter(pic);
  pic_try {
    yail_set_current_form(pic, yail_make_native_instance(pic, form));
    pic_eval(pic, pic_read_cstr(pic, "(add-to-current-form-environment (string->symbol (yail:invoke *this-form* 'formName)) *this-form*)"), "yail");
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
  pic_leave(pic, ai);
}

- (void)setValue:(id)value forSymbol:(NSString *)symname {
  pic_state *pic = _pic;
  pic_value e;
  const char *c_symname = [symname cStringUsingEncoding:NSUTF8StringEncoding];
  pic_try {
    pic_define(pic, "yail", c_symname, [self picValueForObjCValue:value]);
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
}

- (void)setTimeZone:(NSTimeZone *)tz {
  yail_set_time_zone(tz);
}

- (pic_value)wrapObject:(id<SCMValue>)object {
  if (!object) {
    return yail_null_value(_pic);
  }
  return object.value;
}

- (pic_value)internObject:(id<SCMObject>)object {
  return yail_make_native_instance(_pic, object);
}

- (nonnull SCMSymbol *)makeSymbol:(nonnull NSString *)name {
  // Check if we've seen interned this symbol
  SCMSymbol *symbol = [symbolTable_ objectForKey:name];
  if (symbol) {
    return symbol;
  }

  // Intern the new symbol
  symbol = [SCMSymbol symbol:pic_intern(_pic, pic_cstr_value(_pic, [name cStringUsingEncoding:NSUTF8StringEncoding])) inInterpreter:self];
  [symbolTable_ setValue:symbol forKey:name];
  return symbol;
}

- (pic_value)makeConsWithCar:(nullable id<SCMValue>)car cdr:(nullable id<SCMValue>)cdr {
  return pic_cons(_pic, car == nil ? pic_nil_value(_pic) : car.value,
      cdr == nil ? pic_nil_value(_pic) : cdr.value);
}

- (nonnull id<SCMValue>)valueForObject:(nullable id)object {
  if (object == nil) {
    return [SCMValue nilValue];
  } else if ([object conformsToProtocol:@protocol(SCMValue)]) {
    return (id<SCMValue>)object;
  } else if ([object isKindOfClass:[SCMValue class]]) {
    return (SCMValue *)object;
  } else if ([object isKindOfClass:[YailList class]]) {
    return (YailList *)object;
  } else if ([object isKindOfClass:[YailDictionary class]]) {
    return (YailDictionary *)object;
  } else if ([object isKindOfClass:[NSNumber class]]) {
    char type = [((NSNumber *) object) objCType][0];
    if (type == 'd' || type == 'f') {
      return [SCMValue doubleValue:((NSNumber *) object).doubleValue];
    } else {
      return [SCMValue intValue:((NSNumber *) object).intValue];
    }
  } else if ([object isKindOfClass:[NSString class]]) {
    return [SCMString stringWithString:object inInterpreter:self];
  } else if ([object isKindOfClass:[NSArray class]]) {
    return [((NSArray *) object) yailListUsingInterpreter:self];
  } else if ([object isKindOfClass:[NSDictionary class]]) {
    return [((NSDictionary *) object) yailDictionaryUsingInterpreter:self];
  } else {
    return [SCMObjectWrapper object:yail_make_native_instance(_pic, object) inInterpreter:self];
  }
}

- (id<SCMValue>)unwrapValue:(pic_value)value {
  if (yail_list_p(_pic, value)) {
    return yail_list_objc(_pic, value);
  } else if (pic_list_p(_pic, value)) {
    if (pic_nil_p(_pic, value)) {
      return [YailList emptyListIn:self];
    } else if (pic_car(_pic, value) == symbolTable_[@"*list*"].value) {
      return [YailList wrapList:value fromInterpreter:self];
    } else {
      return yail_to_native(_pic, value);
    }
  } else if (yail_dictionary_p(_pic, value)) {
    return yail_dict_objc(_pic, value);
  } else if (yail_scmvalue_p(_pic, value)) {
    return yail_scmvalue_objc(_pic, value);
  } else if (yail_native_instance_p(_pic, value)) {
    return yail_native_instance_objc(_pic, value);
  } else if (pic_true_p(_pic, value)) {
    return [SCMValue trueValue];
  } else if (pic_false_p(_pic, value)) {
    return [SCMValue falseValue];
  } else if (pic_int_p(_pic, value)) {
    return [SCMValue intValue:pic_int(_pic, value)];
  } else if (pic_float_p(_pic, value)) {
    return [SCMValue doubleValue:pic_float(_pic, value)];
  } else if (pic_sym_p(_pic, value)) {
    NSString *objcName = [NSString stringWithUTF8String:pic_str(_pic, pic_sym_name(_pic, value))];
    SCMSymbol *result = symbolTable_[objcName];
    if (result) {
      return result;
    }
    result = [[SCMSymbol alloc] initWithSymbol:value inInterpreter:self];
    symbolTable_[objcName] = result;
    return result;
  } else if (pic_str_p(_pic, value)) {
    return [SCMString stringFromPicString:value inInterpreter:self];
  } else {
    return yail_to_native(_pic, value);
  }
}

- (void)mark:(pic_value)value {
  gc_mark(_pic, value);
}

- (void)runGC {
  pic_gc(_pic);
}

- (void)protect:(id)object {
  [protected_ addObject:object];
}

- (void)unprotect:(id)object {
  [protected_ removeObject:object];
}

- (void)mark {
  for (id item in self->protected_) {
    if ([item respondsToSelector:@selector(mark)]) {
      [item mark];
    }
  }
}

#ifdef MEMDEBUG

- (void)printGCRoots:(id)object {
  NSLog(@"Looking for strong references to %@", object);
  pic_value e;
  pic_state *pic = _pic;
  _defaultInterpreter = self;
  size_t ai = pic_enter(pic);
  pic_try {
    yail_print_strong_refs(pic, yail_make_native_instance(pic, object));
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
  pic_leave(pic, ai);
}

#endif

@synthesize state = _pic;

@end
