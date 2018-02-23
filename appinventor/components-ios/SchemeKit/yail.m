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
#include <stdlib.h>

static size_t BUFSIZE = 4096;

@class ProtocolWrapper;

/** Used to maintain references to objects GCed by Picrin */
static NSMutableDictionary<id, NSNumber *> *objects = nil;
static NSMutableDictionary<NSString *, ProtocolWrapper *> *protocols = nil;

@interface ProtocolWrapper: NSObject<NSCopying> {
  Protocol *protocol_;
}
- (instancetype)initWithProtocol:(Protocol *)protocol;
@property (readonly) Protocol *protocol;
@end

@implementation ProtocolWrapper

- (instancetype)initWithProtocol:(Protocol *)protocol {
  if (self = [super init]) {
    self->protocol_ = protocol;
  }
  return self;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

@synthesize protocol = protocol_;

@end


struct native_class {
  OBJECT_HEADER
  Class class_;
};

struct native_protocol {
  OBJECT_HEADER
  __unsafe_unretained Protocol *protocol_;
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
  NSNumber *wrappedValue = nil;
  if ((wrappedValue = [objects objectForKey:clazz])) {
    return (pic_value) wrappedValue.unsignedLongLongValue;
  }
  native_class *native = (native_class *)pic_obj_alloc(pic, offsetof(native_class, class_), YAIL_TYPE_CLASS);
  native->class_ = clazz;
  pic_value value = pic_obj_value(native);
  objects[clazz] = [NSNumber numberWithUnsignedLongLong:value];
  return value;
}

const char *
yail_native_class_name(pic_state *PIC_UNUSED(pic), struct native_class *data) {
  return [NSStringFromClass(data->class_) UTF8String];
}

void
yail_native_class_dtor(pic_state *pic, struct native_class *data) {
  [objects removeObjectForKey:data->class_];
}

#pragma mark Native Protocol values

pic_value
yail_make_native_protocol(pic_state *pic, Protocol *protocol) {
  NSNumber *wrappedValue = nil;
  ProtocolWrapper *wrapper = [protocols objectForKey:NSStringFromProtocol(protocol)];
  if (wrapper) {
    if ((wrappedValue = [objects objectForKey:wrapper])) {
      return (pic_value) wrappedValue.unsignedLongLongValue;
    }
  }
  wrapper = [[ProtocolWrapper alloc] initWithProtocol:protocol];
  [protocols setObject:wrapper forKey:NSStringFromProtocol(protocol)];
  native_protocol *native = (native_protocol *)pic_obj_alloc(pic, offsetof(native_protocol, protocol_), YAIL_TYPE_PROTOCOL);
  native->protocol_ = protocol;
  pic_value value = pic_obj_value(native);
  objects[wrapper] = [NSNumber numberWithUnsignedLongLong:value];
  return value;
}

const char *
yail_native_protocol_name(pic_state *PIC_UNUSED(pic), struct native_protocol *data) {
  return [NSStringFromProtocol(data->protocol_) UTF8String];
}

void
yail_native_protocol_dtor(pic_state *pic, struct native_protocol *data) {
  NSString *protocolName = NSStringFromProtocol(data->protocol_);
  ProtocolWrapper *wrapper = [protocols objectForKey:protocolName];
  [protocols removeObjectForKey:protocolName];
  [objects removeObjectForKey:wrapper];
}

#pragma mark Native Method values

pic_value
yail_make_native_method(pic_state *pic, SCMMethod *method) {
  NSNumber *wrappedValue = nil;
  if ((wrappedValue = [objects objectForKey:method])) {
    return (pic_value) wrappedValue.unsignedLongLongValue;
  }
  native_method *native = (native_method *)pic_obj_alloc(pic, offsetof(native_method, method_), YAIL_TYPE_METHOD);
  native->method_ = method;
  pic_value value = pic_obj_value(native);
  objects[method] = [NSNumber numberWithUnsignedLongLong:value];
  return pic_obj_value(native);
}

const char *
yail_native_method_name(pic_state *PIC_UNUSED(pic), struct native_method *obj) {
  return [obj->method_.yailName UTF8String];
}

void
yail_native_method_dtor(pic_state *pic, struct native_method *method) {
  [objects removeObjectForKey:method->method_];
}

#pragma mark Native Instance values

pic_value
yail_make_native_instance(pic_state *pic, id object) {
  NSNumber *wrappedValue = nil;
  if ((wrappedValue = [objects objectForKey:object])) {
    return (pic_value) wrappedValue.unsignedLongLongValue;
  }
  native_instance *native = (native_instance *)pic_obj_alloc(pic, offsetof(native_instance, object_), YAIL_TYPE_INSTANCE);
  native->object_ = object;
  pic_value value = pic_obj_value(native);
  objects[object] = [NSNumber numberWithUnsignedLongLong:value];
  return value;
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
  [objects removeObjectForKey:instance->object_];
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
    Protocol *proto = [SCMNameResolver protocolFromQualifiedName:[localSymbol UTF8String]];
    if (proto != nil) {
      pic_weak_set(pic, pic->globals, uid, yail_make_native_protocol(pic, proto));
      return 0;
    }
  }
  return 1;
}

void
yail_set_current_form(pic_state *pic, pic_value form) {
  pic_set(pic, "yail", "*this-form*", form);
}

pic_value
yail_make_instance(pic_state *pic) {
  pic_value native_class, *args;
  int argc;
  
  pic_get_args(pic, "o*", &native_class, &argc, &args);
  
  char *selector = (char *)malloc(5+argc);
  strcpy(selector, "init");
  for (int i = 0, j = 4; i < argc; ++i) {
    selector[j] = ':';
  }
  selector[4+argc] = '\0';

  Class clazz = yail_native_class_ptr(pic, native_class)->class_;
  SCMMethod *init = [SCMNameResolver naryInitializerForClass:clazz withName:"init" argCount:argc];
  if (init) {
    free(selector);
    NSInvocation *invocation = [init invocationForInstance:clazz];
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
        pic_error(pic, "incompatible yail type received", 1, pic_typename(pic, pic_type(pic, args[i])));
      }
    }
    id result = nil;
    @try {
      result = [clazz alloc];
      [invocation performSelectorOnMainThread:@selector(invokeWithTarget:) withObject:result waitUntilDone:YES];
      [invocation getReturnValue:&result];
    } @catch(NSException *e) {
      const char *msg = [[e description] UTF8String];
      pic_error(pic, "native exception", 1, pic_cstr_value(pic, msg));
    }
    if (result) {
      return yail_make_native_instance(pic, result);
    } else {
      return pic_undef_value(pic);
    }
  } else {
    pic_value str = pic_cstr_value(pic, selector);
    free(selector);
    pic_error(pic, "undefined initializer", 2, native_class, str);
  }
  
  return pic_undef_value(pic);
}

static pic_value object_to_yail(pic_state *, id);

static pic_value
nsstring_to_yail(pic_state *pic, NSString *string) {
  const char *data = [string UTF8String];
  return pic_cstr_value(pic, data);
}

static pic_value
nsnumber_to_yail(pic_state *pic, NSNumber *number) {
  const char *type = number.objCType;
  if (0 == strcmp(type, @encode(BOOL))) {
    return number.boolValue? pic_true_value(pic) : pic_false_value(pic);
  } else if (0 == strcmp(type, @encode(int))) {
    return pic_int_value(pic, number.intValue);
  } else if (0 == strcmp(type, @encode(NSInteger))) {
    //TODO: handle overflow
    return pic_int_value(pic, (int) number.integerValue);
  } else if (0 == strcmp(type, @encode(NSUInteger))) {
    //TODO: handle overflow
    return pic_int_value(pic, (int) number.unsignedIntegerValue);
  } else if (0 == strcmp(type, @encode(float))) {
    return pic_float_value(pic, number.floatValue);
  } else if (0 == strcmp(type, @encode(double))) {
    return pic_float_value(pic, number.doubleValue);
  } else {
    NSLog(@"Unknown YAIL type with Objective-C ID: %s", type);
    return pic_undef_value(pic);  // type not valid for yail
  }
}

static pic_value
nsarray_to_yail(pic_state *pic, NSArray *array) {
  pic_value *values = (pic_value *)malloc(array.count * sizeof(pic_value));
  int i = 0;
  for (id object in array) {
    values[i++] = object_to_yail(pic, object);
  }
  pic_value result = pic_make_list(pic, (int) i, values);
  free(values);
  return pic_cons(pic, pic_intern_cstr(pic, "*list*"), result);
}

static pic_value
object_to_yail(pic_state *pic, id object) {
  if ([object isKindOfClass:[NSArray class]]) {
    return nsarray_to_yail(pic, (NSArray *)object);
  } else if ([object isKindOfClass:[NSString class]]) {
    return nsstring_to_yail(pic, (NSString *)object);
  } else if ([object isKindOfClass:[NSNumber class]]) {
    return nsnumber_to_yail(pic, (NSNumber *)object);
  } else {
    return yail_make_native_instance(pic, object);
  }
}

static id
yail_to_objc(pic_state *pic, pic_value value, NSMutableDictionary *history) {
  if (pic_pair_p(pic, value)) {
    NSMutableArray *result = [NSMutableArray array];
    if (pic_nil_p(pic, value)) {
      return result;
    } else if (pic_sym_p(pic, pic_car(pic, value))) {
      // skip *list*
      value = pic_cdr(pic, value);
    }
    while (!pic_nil_p(pic, value)) {
      pic_value car = pic_car(pic, value);
      NSNumber *addr = [NSNumber numberWithUnsignedLongLong:car];
      if (history[addr] == nil) {
        id converted = yail_to_objc(pic, car, history);
        history[addr] = converted ? converted : [NSNull null];
      }
      [result addObject:history[addr]];
      value = pic_cdr(pic, value);
      if (!pic_pair_p(pic, value) && !pic_nil_p(pic, value)) {
        addr = [NSNumber numberWithUnsignedLongLong:value];
        if (history[addr] == nil) {
          id converted = yail_to_objc(pic, value, history);
          history[addr] = converted ? converted : [NSNull null];
        }
        [result addObject:history[addr]];
      }
    }
    return result;
  } else if (pic_str_p(pic, value)) {
    return [NSString stringWithCString:pic_str(pic, value) encoding:NSUTF8StringEncoding];
  } else if (pic_int_p(pic, value)) {
    return [NSNumber numberWithInt:pic_int(pic, value)];
  } else if (pic_float_p(pic, value)) {
    return [NSNumber numberWithDouble:pic_float_p(pic, value)];
  } else if (pic_true_p(pic, value)) {
    return [NSNumber numberWithBool:YES];
  } else if (pic_false_p(pic, value)) {
    return [NSNumber numberWithBool:NO];
  } else {
    NSLog(@"Unknown type to convert in yail_to_objc: %s", pic_typename(pic, pic_type(pic, value)));
    return nil;
  }
}

pic_value
yail_invoke(pic_state *pic) {
  pic_value native_object, native_method, *args;
  int argc;
  
  pic_get_args(pic, "oo*", &native_object, &native_method, &argc, &args);
  
  NSMutableArray *argTypes = [NSMutableArray arrayWithCapacity:argc];
  SCMMethod *method = nil;
  int isStatic = yail_native_class_p(pic, native_object) ? 1 : 0;
  if (yail_native_method_p(pic, native_method)) {
    method = yail_native_method_ptr(pic, native_method)->method_;
  } else if (pic_sym_p(pic, native_method)) {
    const char *name = pic_str(pic, pic_sym_name(pic, native_method));
    if (isStatic) {
      method = [SCMNameResolver methodForClass:yail_native_class_ptr(pic, native_object)->class_ withName:name argumentTypeList:argTypes];
    } else {
      method = [SCMNameResolver methodForClass:[yail_native_instance_ptr(pic, native_object)->object_ class] withName:name argumentTypeList:argTypes];
    }
  } else if (pic_id_p(pic, native_method)) {
    const char *name = pic_str(pic, pic_id_name(pic, native_method));
    if (isStatic) {
      method = [SCMNameResolver methodForClass:yail_native_class_ptr(pic, native_object)->class_ withName:name argumentTypeList:argTypes];
    } else {
      method = [SCMNameResolver methodForClass:[yail_native_instance_ptr(pic, native_object)->object_ class] withName:name argumentTypeList:argTypes];
    }
  }
  if (!method) {
    pic_error(pic, "unrecognized method", 1, native_method);
  }
  NSInvocation *invocation = nil;
  if (yail_native_class_p(pic, native_object)) {
    if (isStatic) {
      invocation = [method staticInvocation];
    } else {
      invocation = [method invocationForInstance:yail_native_class_ptr(pic, native_object)->class_];
    }
  } else {
    invocation = [method invocationForInstance:yail_native_instance_ptr(pic, native_object)->object_];
  }
  [invocation retainArguments];
  // first 2 args in Obj-C are reserved for target and selector
  for (int i = 0, j = 2; i < argc; ++i, ++j) {
    if (pic_float_p(pic, args[i])) {
      switch([invocation.methodSignature getArgumentTypeAtIndex:j][0]) {
        case '@': {
          NSString *value = [NSString stringWithFormat:@"%f", pic_float(pic, args[i])];
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'f': {
          float value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        default: {
          double value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
        }
      }
    } else if (pic_int_p(pic, args[i])) {
      switch ([invocation.methodSignature getArgumentTypeAtIndex:j][0]) {
        case 'f': {
          float value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'd': {
          double value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case '@': {
          NSString *value = [NSString stringWithFormat:@"%d", pic_int(pic, args[i])];
          [invocation setArgument:&value atIndex:j];
          break;
        }
        default: {
          int value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
        }
      }
    } else if (pic_str_p(pic, args[i])) {
      const char *str_value = pic_str(pic, args[i]);
      NSString *native_str = [NSString stringWithUTF8String:str_value];
      [invocation setArgument:&native_str atIndex:j];
    } else if (pic_true_p(pic, args[i])) {
      if ([invocation.methodSignature getArgumentTypeAtIndex:j][0] == '@') {
        NSString *value = @"#t";
        [invocation setArgument:&value atIndex:j];
      } else {
        BOOL value = YES;
        [invocation setArgument:&value atIndex:j];
      }
    } else if (pic_false_p(pic, args[i])) {
      if ([invocation.methodSignature getArgumentTypeAtIndex:j][0] == '@') {
        NSString *value = @"#f";
        [invocation setArgument:&value atIndex:j];
      } else {
        BOOL value = NO;
        [invocation setArgument:&value atIndex:j];
      }
    } else if (yail_native_instance_p(pic, args[i])) {
      id object = yail_native_instance_ptr(pic, args[i])->object_;
      [invocation setArgument:&object atIndex:j];
    } else if (pic_sym_p(pic, args[i])) {
      NSString *symname = [NSString stringWithUTF8String:pic_str(pic, pic_sym_name(pic, args[i]))];
      [invocation setArgument:&symname atIndex:j];
    } else if (pic_undef_p(pic, args[i]) || yail_null_p(pic, args[i])) {
      id value = nil;
      [invocation setArgument:&value atIndex:j];
    } else if (pic_pair_p(pic, args[i])) {
      id value = yail_to_objc(pic, args[i], [NSMutableDictionary dictionary]);
      [invocation setArgument:&value atIndex:j];
    } else if (pic_nil_p(pic, args[i])) {
      NSArray *value = [NSArray array];
      [invocation setArgument:&value atIndex:j];
    } else {
      NSLog(@"incompatible yail type received %s in call to %@ at index %d",
            pic_typename(pic, pic_type(pic, args[i])), method.yailName, i);
      pic_error(pic, "incompatible yail type received", 1, pic_cstr_value(pic, pic_typename(pic, pic_type(pic, args[i]))));
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
      __unsafe_unretained id unretainedValue = nil;
      [invocation getReturnValue:&unretainedValue];
      id value = unretainedValue;
      if (!value) {
        return pic_undef_value(pic);
      } else if ([value isKindOfClass:[NSString class]]) {
        const char *str = [(NSString *)value UTF8String];
        return pic_str_value(pic, str, (int)strlen(str));
      } else if ([value isKindOfClass:[YailList class]]) {
        return [(YailList *)value picrinList];
      } else if ([value isKindOfClass:[NSArray class]]) {
        return nsarray_to_yail(pic, (NSArray *)value);
      } else {
        return yail_make_native_instance(pic, value);
      }
    } else if (0 != strcmp(retType, @encode(void))) {
      pic_error(pic, "yail error: unknown return type for method", 1, native_method);
    }
  } @catch(NSException *e) {
    pic_error(pic, [e description].UTF8String, 0);
  }
  
  return pic_undef_value(pic);
}

id yail_to_native(pic_state *pic, pic_value result) {
  if (pic_str_p(pic, result)) {
    const char *str = pic_str(pic, result);
    return [NSString stringWithUTF8String:str];
  } else if (pic_int_p(pic, result)) {
    return [NSNumber numberWithInteger:pic_int(pic, result)];
  } else if (pic_float_p(pic, result)) {
    return [NSNumber numberWithDouble:pic_float(pic, result)];
  } else if (yail_native_instance_p(pic, result)) {
    __unsafe_unretained id unsafeId = yail_native_instance_ptr(pic, result)->object_;
    return unsafeId;
  }
  return nil;
}

pic_value
yail_isa(pic_state *pic) {
  pic_value native_object, native_class;
  
  pic_get_args(pic, "oo", &native_object, &native_class);
  if (!yail_native_instance_p(pic, native_object)) {
    return pic_false_value(pic);
  }
  id object = yail_native_instance_ptr(pic, native_object)->object_;
  if (yail_native_class_p(pic, native_class)) {
    Class clazz = yail_native_class_ptr(pic, native_class)->class_;
    if ([object isKindOfClass:clazz]) {
      return pic_true_value(pic);
    } else {
      return pic_false_value(pic);
    }
  } else if (yail_native_protocol_p(pic, native_class)) {
    Protocol *protocol = yail_native_protocol_ptr(pic, native_class)->protocol_;
    if ([object conformsToProtocol:protocol]) {
      return pic_true_value(pic);
    } else {
      return pic_false_value(pic);
    }
  } else {
    pic_error(pic, "Expected a native class or protocol", 1, native_class);
  }
  
}

pic_value
yail_format_inexact(pic_state *pic) {
  static const int BUFSIZE = 24;
  double value, absvalue;
  char buf[BUFSIZE];

  pic_get_args(pic, "f", &value);
  absvalue = fabs(value);

  if (absvalue > 1e6 || absvalue < 1e-6) {
    snprintf(&buf[0], BUFSIZE - 1, "%E", value);
  } else {
    snprintf(&buf[0], BUFSIZE - 1, "%F", value);
  }

  return pic_str_value(pic, buf, strlen(buf));
}

pic_value
yail_perform_on_main_thread(pic_state *pic) {
  pic_value thunk;

  pic_get_args(pic, "l", &thunk);
  if ([NSThread isMainThread]) {
    pic_call(pic, thunk, 0);
  } else {
    [NSOperationQueue.mainQueue addOperationWithBlock:^{
        pic_call(pic, thunk, 0);
      }];
  }

  return pic_undef_value(pic);
}

pic_value
yail_string_index_of(pic_state *pic) {
  pic_value str, needle;
  int slen, nlen, i, j;

  pic_get_args(pic, "ss", &str, &needle);

  slen = (int) pic_str_len(pic, str);
  nlen = (int) pic_str_len(pic, needle);

  for (i = 0; i <= slen - nlen; i++) {
    for (j = 0; j < nlen; j++) {
      if (pic_str_ref(pic, str, i + j) != pic_str_ref(pic, needle, j)) {
        break;
      }
    }
    if (j == nlen) {
      return pic_int_value(pic, i);
    }
  }

  return pic_int_value(pic, -1);
}

pic_value
yail_primitive_throw(pic_state *pic) {
  pic_value ex;

  pic_get_args(pic, "o", &ex);
  id exception = yail_native_instance_ptr(pic, ex)->object_;

  @throw exception;
}

pic_value
yail_set_seed(pic_state *pic) {
  double seed;

  pic_get_args(pic, "f", &seed);

  srand48((long) seed);

  return pic_undef_value(pic);
}

pic_value
yail_random_fraction(pic_state *pic) {
  return pic_float_value(pic, drand48());
}

pic_value
yail_random_int(pic_state *pic) {
  int bound;

  pic_get_args(pic, "i", &bound);

  return pic_int_value(pic, (int) (lrand48() / (double)UINT_MAX * bound));
}

pic_value
yail_bitwise_arithmetic_shift_left(pic_state *pic) {
  int value, bits, result;

  pic_get_args(pic, "ii", &value, &bits);

  result = value << bits;

  return pic_int_value(pic, result);
}

pic_value
yail_bitwise_arithmetic_shift_right(pic_state *pic) {
  int value, bits, result;

  pic_get_args(pic, "ii", &value, &bits);

  result = value >> bits;

  return pic_int_value(pic, result);
}

pic_value
yail_bitwise_and(pic_state *pic) {
  pic_value *args;
  int argc, result = -1;

  pic_get_args(pic, "*", &argc, &args);
  for (int i = 0; i < argc; i++) {
    result &= pic_int(pic, args[i]);
  }

  return pic_int_value(pic, result);
}

pic_value
yail_bitwise_ior(pic_state *pic) {
  pic_value *args;
  int argc, result = 0;

  pic_get_args(pic, "*", &argc, &args);
  for (int i = 0; i < argc; i++) {
    result |= pic_int(pic, args[i]);
  }

  return pic_int_value(pic, result);
}

pic_value
yail_bitwise_xor(pic_state *pic) {
  pic_value *args;
  int argc, result = 0;

  pic_get_args(pic, "i*", &result, &argc, &args);
  for (int i = 0; i < argc; i++) {
    result ^= pic_int(pic, args[i]);
  }

  return pic_int_value(pic, result);
}

pic_value
yail_format_places(pic_state *pic) {
  int places;
  double value;
  char buffer[18];
  char buffer2[6];

  pic_get_args(pic, "if", &places, &value);

  memset(buffer2, 0, 6);
  memset(buffer, 0, 18);

  snprintf(buffer2, 6, "%%.%df", places);
  snprintf(buffer, 18, buffer2, value);

  return pic_cstr_value(pic, buffer);
}

pic_value
yail_string_to_uppercase(pic_state *pic) {
  char *str;

  pic_get_args(pic, "z", &str);
  NSString *upper = [[NSString stringWithUTF8String:str] uppercaseString];
  const char *str2 = [upper cStringUsingEncoding:NSUTF8StringEncoding];

  return pic_str_value(pic, str2, strlen(str2));
}

pic_value
yail_string_to_lowercase(pic_state *pic) {
  char *str;

  pic_get_args(pic, "z", &str);
  NSString *lower = [[NSString stringWithUTF8String:str] lowercaseString];
  const char *str2 = [lower cStringUsingEncoding:NSUTF8StringEncoding];

  return pic_str_value(pic, str2, strlen(str2));
}

void
pic_init_yail(pic_state *pic)
{
  pic_deflibrary(pic, "yail");
  pic_define(pic, "yail", "*this-form*", pic_nil_value(pic));
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
  pic_defun(pic, "yail:isa", yail_isa);
  pic_defun(pic, "yail:format-inexact", yail_format_inexact);
  pic_defun(pic, "yail:perform-on-main-thread", yail_perform_on_main_thread);
  pic_defun(pic, "string-index-of", yail_string_index_of);
  pic_defun(pic, "string-to-upper-case", yail_string_to_uppercase);
  pic_defun(pic, "string-to-lower-case", yail_string_to_lowercase);
  pic_defun(pic, "primitive-throw", yail_primitive_throw);
  pic_defun(pic, "yail:set-seed", yail_set_seed);
  pic_defun(pic, "random-fraction", yail_random_fraction);
  pic_defun(pic, "yail:random-int", yail_random_int);
  pic_defun(pic, "bitwise-arithmetic-shift-left", yail_bitwise_arithmetic_shift_left);
  pic_defun(pic, "bitwise-arithmetic-shift-right", yail_bitwise_arithmetic_shift_right);
  pic_defun(pic, "bitwise-and", yail_bitwise_and);
  pic_defun(pic, "bitwise-ior", yail_bitwise_ior);
  pic_defun(pic, "bitwise-xor", yail_bitwise_xor);
  pic_defun(pic, "format-places", yail_format_places);
  objects = [NSMutableDictionary dictionary];
  protocols = [NSMutableDictionary dictionary];
}
