//
//  yail.m
//  SchemeKit
//
//  Created by Evan Patton on 10/9/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#import "SCMNameResolver.h"
#import "SCMMethod.h"
#import "YailList.h"
#include "picrin.h"
#include "picrin/extra.h"
#include "picrin/private/object.h"
#include "picrin/private/state.h"

/** Used to maintain references to objects GCed by Picrin */
static NSMutableSet *objects = nil;


struct native_class {
  OBJECT_HEADER
  Class class_;
};

struct native_method {
  OBJECT_HEADER
  __unsafe_unretained SCMMethod *method_;
};

struct native_instance {
  OBJECT_HEADER
  __unsafe_unretained id object_;
};

static pic_value
pic_call_native_method(pic_state *pic) {
  //TODO: implementation
  // rough sketch:
  // 1. get target native object from pic
  // 2. get target native selector from pic
  // 3. get list of
  return pic_invalid_value(pic);
}

static pic_value
pic_yail_is_type(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_set_property(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_get_property(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_find_class(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_make_component(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_make_random(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_make_runtime_error(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_make_yail_list(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_call_instance_method(pic_state *pic) {
  return 0;
}

static pic_value
pic_yail_call_static_method(pic_state *pic) {
  return 0;
}

int
yail_print_ast(pic_state *pic, pic_value form) {
  return 0;
}

pic_value
yail_make_instance(pic_state *pic);

#pragma mark Native Class values

pic_value
yail_make_native_class(pic_state *pic, Class clazz) {
  native_class *native = (native_class *)pic_obj_alloc(pic, offsetof(native_class, class_), YAIL_TYPE_CLASS);
  native->class_ = clazz;
  return pic_obj_value(native);
}

const char *
yail_native_class_name(pic_state *PIC_UNUSED(pic), struct native_class *data) {
  return [NSStringFromClass(data->class_) UTF8String];
}

void
yail_native_class_dtor(pic_state *pic, struct native_class *data) {
  // no-op
}

#pragma mark Native Method values

pic_value
yail_make_native_method(pic_state *pic, SCMMethod *method) {
  native_method *native = (native_method *)pic_obj_alloc(pic, offsetof(native_method, method_), YAIL_TYPE_METHOD);
  native->method_ = method;
  [objects addObject:method];
  return pic_obj_value(native);
}

const char *
yail_native_method_name(pic_state *PIC_UNUSED(pic), struct native_method *obj) {
  return [obj->method_.yailName UTF8String];
}

void
yail_native_method_dtor(pic_state *pic, struct native_method *method) {
  [objects removeObject:method->method_];
}

#pragma mark Native Instance values

pic_value
yail_make_native_instance(pic_state *pic, id object) {
  native_instance *native = (native_instance *)pic_obj_alloc(pic, offsetof(native_instance, object_), YAIL_TYPE_INSTANCE);
  native->object_ = object;
  [objects addObject:object];
  return pic_obj_value(native);
}

const char *
yail_native_instance_typename(pic_state *PIC_UNUSED(pic), struct native_instance *obj) {
  id object = obj->object_;
  Class clazz = [object class];
  NSString *className = NSStringFromClass(clazz);
  return [className UTF8String];
}

void
yail_native_instance_dtor(pic_state *pic, struct native_instance *instance) {
  [objects removeObject:instance->object_];
}

int
yail_resolve_native_symbol(pic_state *pic, pic_value uid) {
  //TODO: implementation
  NSString *nssymbol = [NSString stringWithUTF8String:pic_str(pic, pic_sym_name(pic, uid))];
  NSArray<NSString *> *parts = [nssymbol componentsSeparatedByString:@"/"];
  NSString *localSymbol = parts[parts.count - 1];
  if ([localSymbol containsString:@":"]) {
    // method
    NSRange range = [localSymbol rangeOfString:@":"];
    NSString *targetName = [localSymbol substringToIndex:range.location];
    NSString *methodName = [localSymbol substringFromIndex:range.location+1];
    // find variable value in locals
    // find class name
    Class clazz = [SCMNameResolver classFromQualifiedName:[targetName UTF8String]];
    if (clazz == nil) {
    } else {
      SCMMethod *method = [SCMNameResolver methodForClass:clazz withName:[methodName UTF8String] argumentTypeList:@[]];
    }
  } else {
    // class / instance
    Class clazz = [SCMNameResolver classFromQualifiedName:[localSymbol UTF8String]];
    if (clazz != nil) {
      pic_weak_set(pic, pic->globals, uid, yail_make_native_class(pic, clazz));
      return 0;
    }
  }
  return 1;
}

void
yail_set_current_form(pic_state *pic, pic_value form) {
  pic_set(pic, "yail", "current-form", form);
}

pic_value
yail_make_instance(pic_state *pic) {
  //TODO: implementation
  pic_value native_class, *args;
  int argc;
  
  pic_get_args(pic, "o*", &native_class, &argc, &args);
  
  char *selector = (char *)malloc(5+argc);
  strcpy(selector, "init");
  for (int i = 0, j = 4; i < argc; ++i) {
    selector[j] = ':';
  }
  selector[4+argc] = '\0';
  
  SCMMethod *init = [SCMNameResolver naryInitializerForClass:yail_native_class_ptr(pic, native_class)->class_ withName:"init" argCount:argc];
  if (init) {
    free(selector);
    NSInvocation *invocation = [init invocationForInstance:yail_native_class_ptr(pic, native_class)->class_];
    [invocation retainArguments];
    for (int i = 0, j = 2; i < argc; ++i, ++j) {
      if (pic_float_p(pic, args[i])) {
        double value = pic_float(pic, args[i]);
        [invocation setArgument:&value atIndex:j];
      } else if (pic_int_p(pic, args[i])) {
        int value = pic_int(pic, args[i]);
        [invocation setArgument:&value atIndex:j];
      } else if (pic_str_p(pic, args[i])) {
        const char *str_value = pic_str(pic, args[i]);
        NSString *native_str = [NSString stringWithUTF8String:str_value];
        [invocation setArgument:&native_str atIndex:j];
      } else if (pic_true_p(pic, args[i])) {
        BOOL value = YES;
        [invocation setArgument:&value atIndex:j];
      } else if (pic_false_p(pic, args[i])) {
        BOOL value = NO;
        [invocation setArgument:&value atIndex:j];
      } else if (yail_native_instance_p(pic, args[i])) {
        id object = yail_native_instance_ptr(pic, args[i])->object_;
        [invocation setArgument:&object atIndex:j];
      } else {
        pic_error(pic, "incompatible yail type received %s", 1, pic_typename(pic, pic_type(pic, args[i])));
      }
    }
    id result = nil;
    @try {
      result = [yail_native_class_ptr(pic, native_class)->class_ alloc];
      [invocation performSelectorOnMainThread:@selector(invokeWithTarget:) withObject:result waitUntilDone:YES];
      [invocation getReturnValue:&result];
    } @catch(NSException *e) {
      pic_error(pic, "native exception %s", 1, [e debugDescription]);
    }
    if (result) {
      return yail_make_native_instance(pic, result);
    } else {
      return pic_undef_value(pic);
    }
  } else {
    pic_value str = pic_str_value(pic, selector, (int) strlen(selector));
    free(selector);
    pic_error(pic, "undefined initializer %s", 1, str);
    //TODO: handle leaking selector
  }
  
  return pic_undef_value(pic);
}

pic_value
yail_invoke(pic_state *pic) {
  pic_value native_object, native_method, *args;
  int argc;
  
  pic_get_args(pic, "oo*", &native_object, &native_method, &argc, &args);
  
  NSMutableArray *argTypes = [NSMutableArray arrayWithCapacity:argc];
  SCMMethod *method = nil;
  if (yail_native_method_p(pic, native_method)) {
    method = yail_native_method_ptr(pic, native_method)->method_;
  } else if (pic_sym_p(pic, native_method)) {
    const char *name = pic_str(pic, pic_sym_name(pic, native_method));
    method = [SCMNameResolver methodForClass:[yail_native_instance_ptr(pic, native_object)->object_ class] withName:name argumentTypeList:argTypes];
  }
  if (!method) {
    pic_error(pic, "unrecognized method", 1, native_method);
  }
  NSInvocation *invocation = nil;
  if (yail_native_class_p(pic, native_object)) {
    invocation = [method invocationForInstance:yail_native_class_ptr(pic, native_object)->class_];
  } else {
    invocation = [method invocationForInstance:yail_native_instance_ptr(pic, native_object)->object_];
  }
  [invocation retainArguments];
  // first 2 args in Obj-C are reserved for target and selector
  for (int i = 0, j = 2; i < argc; ++i, ++j) {
    if (pic_float_p(pic, args[i])) {
      double value = pic_float(pic, args[i]);
      [invocation setArgument:&value atIndex:j];
    } else if (pic_int_p(pic, args[i])) {
      int value = pic_int(pic, args[i]);
      [invocation setArgument:&value atIndex:j];
    } else if (pic_str_p(pic, args[i])) {
      const char *str_value = pic_str(pic, args[i]);
      NSString *native_str = [NSString stringWithUTF8String:str_value];
      [invocation setArgument:&native_str atIndex:j];
    } else if (pic_true_p(pic, args[i])) {
      BOOL value = YES;
      [invocation setArgument:&value atIndex:j];
    } else if (pic_false_p(pic, args[i])) {
      BOOL value = NO;
      [invocation setArgument:&value atIndex:j];
    } else if (yail_native_instance_p(pic, args[i])) {
      id object = yail_native_instance_ptr(pic, args[i])->object_;
      [invocation setArgument:&object atIndex:j];
    } else {
      pic_error(pic, "incompatible yail type received %s", 1, pic_typename(pic, pic_type(pic, args[i])));
    }
  }
  
  // Invoke based on return type
  @try {
    [invocation performSelectorOnMainThread:@selector(invoke) withObject:nil waitUntilDone:YES];
    const char *retType = invocation.methodSignature.methodReturnType;
    if (0 == strcmp(retType, @encode(BOOL))) {
      BOOL value = NO;
      [invocation getReturnValue:&value];
      return value? pic_true_value(pic) : pic_false_value(pic);
    } else if (0 == strcmp(retType, @encode(int))) {
      int value = 0;
      [invocation getReturnValue:&value];
      return pic_int_value(pic, value);
    } else if (0 == strcmp(retType, @encode(NSInteger))) {
      NSInteger value = 0;
      [invocation getReturnValue:&value];
      //TODO: handle overflow
      return pic_int_value(pic, (int) value);
    } else if (0 == strcmp(retType, @encode(NSUInteger))) {
      NSUInteger value = 0;
      [invocation getReturnValue:&value];
      //TODO: handle overflow
      return pic_int_value(pic, (int) value);
    } else if (0 == strcmp(retType, @encode(float))) {
      float value = 0;
      [invocation getReturnValue:&value];
      return pic_float_value(pic, value);
    } else if (0 == strcmp(retType, @encode(double))) {
      double value = 0;
      [invocation getReturnValue:&value];
      return pic_float_value(pic, value);
    } else if (0 == strcmp(retType, @encode(id))) {
      id value = nil;
      [invocation getReturnValue:&value];
      if (!value) {
        return pic_undef_value(pic);
      } else if ([value isKindOfClass:[NSString class]]) {
        const char *str = [(NSString *)value UTF8String];
        return pic_str_value(pic, str, (int)strlen(str));
      } else if ([value isKindOfClass:[YailList class]]) {
        return [(YailList *)value picrinList];
      } else {
        return yail_make_native_instance(pic, value);
      }
    } else if (0 != strcmp(retType, @encode(void))) {
      pic_error(pic, "yail error: unknown return type for method", 1, native_method);
    }
  } @catch(NSException *e) {
    pic_error(pic, [e debugDescription].UTF8String, 0);
  }
  
  return pic_undef_value(pic);
}

void
pic_init_yail(pic_state *pic)
{
  pic_deflibrary(pic, "yail");
  pic_define(pic, "yail", "current-form", pic_nil_value(pic));
  pic_defun(pic, "yail:find-class", pic_yail_find_class);
  pic_defun(pic, "yail:type?", pic_yail_is_type);
  pic_defun(pic, "yail:set-property!", pic_yail_set_property);
  pic_defun(pic, "yail:get-property", pic_yail_get_property);
  pic_defun(pic, "yail:make-component", pic_yail_make_component);
  pic_defun(pic, "yail:make-random", pic_yail_make_random);
  pic_defun(pic, "yail:make-runtime-error", pic_yail_make_runtime_error);
  pic_defun(pic, "yail:make-yail-list", pic_yail_make_yail_list);
  pic_defun(pic, "yail:call-instance-method", pic_yail_call_instance_method);
  pic_defun(pic, "yail:call-static-method", pic_yail_call_static_method);
  pic_defun(pic, "yail:make-instance", yail_make_instance);
  pic_defun(pic, "yail:invoke", yail_invoke);
  objects = [NSMutableSet set];
}
