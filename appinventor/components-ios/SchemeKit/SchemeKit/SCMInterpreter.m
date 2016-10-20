//
//  SCMInterpreter.m
//  SchemeKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "SCMInterpreter.h"
#include "picrin.h"
#include "picrin/extra.h"
#include "picrin/private/object.h"
#include <SchemeKit/SCMMethod.h>

// extern functions
pic_value yail_make_native_class(pic_state *, Class);
pic_value yail_make_native_method(pic_state *, SCMMethod *);
pic_value yail_make_native_instance(pic_state *, id);

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
}

static NSException *
exception_from_pic_error(pic_state *pic, pic_value e) {
  const char *msg = pic_str(pic, pic_obj_value(pic_error_ptr(pic, e)->msg));
  const char *irr = pic_str(pic, pic_error_ptr(pic, e)->irrs);
  return [NSException exceptionWithName:[NSString stringWithUTF8String:msg] reason:[NSString stringWithUTF8String:irr] userInfo:nil];
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
      pic_fprintf(pic, resultport, "~s", result);
      pic_fgetbuf(pic, resultport, &buffer, &buflen);
      if (response) {
        response = [NSString stringWithFormat:@"%@\n%@", response, [NSString stringFromBuffer:buffer withLength:buflen]];
      } else {
        response = [NSString stringFromBuffer:buffer withLength:buflen];
      }
      pic_fclose(pic, resultport);
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
  return response != nil ? response : @"";
}

- (void)clearException {
  exception_ = nil;
}

- (void)setCurrentForm:(id)form {
  pic_state *pic = pic_;
  pic_value e;
  pic_try {
    yail_set_current_form(pic, yail_make_native_instance(pic, form));
  } pic_catch(e) {
    exception_ = exception_from_pic_error(pic, e);
  }
}

@end
