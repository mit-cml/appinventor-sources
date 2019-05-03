// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

#import "SCMInterpreter.h"
#include "picrin.h"
#include "picrin/extra.h"
#include "picrin/private/object.h"
#include <SchemeKit/SCMMethod.h>

// extern functions
pic_value yail_make_native_class(pic_state *, Class);
pic_value yail_make_native_method(pic_state *, SCMMethod *);
pic_value yail_make_native_instance(pic_state *, id);
id yail_to_native(pic_state *, pic_value);
void yail_set_time_zone(NSTimeZone *tz);

extern char *picrin_native_stack_start;

void
pic_init_picrin(pic_state *pic)
{
  void pic_init_contrib(pic_state *);
  void pic_load_piclib(pic_state *);
  
  pic_init_contrib(pic);
  pic_load_piclib(pic);
  pic_init_yail(pic);
  pic_in_library(pic, "yail");
  pic_import(pic, "scheme.base");
  pic_import(pic, "scheme.write");
  pic_import(pic, "picrin.base");
}

static NSException *
exception_from_pic_error(pic_state *pic, pic_value e) {
  const char *msg = pic_str(pic, pic_obj_value(pic_error_ptr(pic, e)->msg));
  pic_value e2, port, irrs = pic_error_ptr(pic, e)->irrs;
  const char *buffer;
  char *bufcopy;
  int buflen = 64;
  pic_try {
    port = pic_fmemopen(pic, NULL, buflen, "w");
    pic_fprintf(pic, port, "~a\0", irrs);
    pic_fgetbuf(pic, port, &buffer, &buflen);
    bufcopy = (char *)malloc(buflen + 1);
    strcpy(bufcopy, buffer);  // Picrin may GC, so make a clean copy before we exit this scope
  } pic_catch(e2) {
    NSLog(@"WTF");
  }
  NSException *result = [NSException exceptionWithName:@"RuntimeError" reason:[NSString stringWithFormat:@"%s. Irritants: %s", msg, bufcopy] userInfo:nil];
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

@interface SCMInterpreter () {
 @private
  pic_state *pic_;
  NSException *exception_;
}

@end

@implementation SCMInterpreter

@synthesize exception = exception_;

- (pic_value)picValueForObjCValue:(id)value {
  if (value == nil) {
    return pic_nil_value(self->pic_);
  } else if ([value isKindOfClass:[NSNumber class]]) {
    NSNumber *number = (NSNumber *)value;
    char c = number.objCType[0];
    switch(c) {
      case 'c':
        // Technically 'c' is a char, but NSNumber encodes booleans as chars.
        // Other integers will be stored as ints or longs, so we should be safe
        // to assume that if something is a char it's actually a boolean value.
        return number.boolValue ? pic_true_value(pic_) : pic_false_value(pic_);
      case 's':
      case 'i':
      case 'l':
        if (c != 'l' || sizeof(long) == sizeof(int)) {
          return pic_int_value(pic_, number.intValue);
        }
      case 'q':
        return pic_float_value(pic_, number.doubleValue);
      case 'C':
      case 'S':
      case 'I':
      case 'L':
        if (number.unsignedIntegerValue <= UINT32_MAX) {
          return pic_int_value(pic_, number.intValue);
        }
      case 'Q':
        return pic_float_value(pic_, number.doubleValue);
      case 'B':
        if (number.boolValue) {
          return pic_true_value(pic_);
        } else {
          return pic_false_value(pic_);
        }
        break;
      case 'f':
      case 'd':
        return pic_float_value(pic_, number.doubleValue);
      default:
        NSLog(@"Unknown NSNumber type: %c", c);
        break;
    }
  } else if ([value isKindOfClass:[NSString class]]) {
    const char *buf = [(NSString *)value UTF8String];
    return pic_str_value(pic_, buf, (int)strlen(buf));
  } else if ([value isKindOfClass:[NSArray class]]) {
    NSArray *items = (NSArray *)value;
    if (items.count == 0) {
      // special treatment of empty args list
      return pic_nil_value(pic_);
    }
    pic_value *values = (pic_value *) malloc(sizeof(pic_value) * items.count);
    for (NSUInteger i = 0 ; i < items.count ; ++i ) {
      values[i] = [self picValueForObjCValue:items[i]];
    }
    return pic_make_list(pic_, items.count, values);
  }
  return yail_make_native_instance(pic_, value);
}

- (instancetype)init {
  if (self = [super init]) {
    pic_state *pic = pic_ = pic_open(pic_default_allocf, NULL);
    pic_value e;
    exception_ = nil;
    pic_try {
      pic_init_picrin(pic);
    } pic_catch(e) {
      exception_ = exception_from_pic_error(pic, e);
    }
  }
  return self;
}

- (void)dealloc {
  pic_close(pic_);
}

- (nonnull NSString *)evalForm:(NSString *)form {
  char t;
  pic_state *pic = pic_;
  pic_value port, program, result, e, resultport;
  int buflen = 64;
  const char *yail = form.UTF8String;
  const char *buffer;
  NSString *response = nil;
  picrin_native_stack_start = &t;
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
  pic_state *pic = pic_;
  pic_value result, e;
  const char *name_cstr = name.UTF8String;
  picrin_native_stack_start = &t;
  id objcResult = nil;
  pic_try {
    size_t ai = pic_enter(pic);
    result = pic_apply(pic_, pic_ref(pic, "yail", name_cstr), (int)arguments.count, nativeargs);
    objcResult = yail_to_native(pic, result);
    pic_leave(pic, ai);
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic_, e);
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
  pic_state *pic = pic_;
  pic_value e;
  pic_try {
    yail_set_current_form(pic, yail_make_native_instance(pic, form));
    pic_eval(pic, pic_read_cstr(pic, "(add-to-current-form-environment 'Screen1 *this-form*)"), "yail");
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
}

- (void)setValue:(id)value forSymbol:(NSString *)symname {
  pic_state *pic = pic_;
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

@end
