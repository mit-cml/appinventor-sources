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
      double value = pic_float(pic, args[i]);
      [invocation setArgument:&value atIndex:j];
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
      BOOL value = YES;
      [invocation setArgument:&value atIndex:j];
    } else if (pic_false_p(pic, args[i])) {
      BOOL value = NO;
      [invocation setArgument:&value atIndex:j];
    } else if (yail_native_instance_p(pic, args[i])) {
      id object = yail_native_instance_ptr(pic, args[i])->object_;
      [invocation setArgument:&object atIndex:j];
    } else if (pic_sym_p(pic, args[i])) {
      NSString *symname = [NSString stringWithUTF8String:pic_str(pic, pic_sym_name(pic, args[i]))];
      [invocation setArgument:&symname atIndex:j];
    } else {
      NSLog(@"incompatible yail type received %s in call to %s at index %d",
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
  objects = [NSMutableDictionary dictionary];
  protocols = [NSMutableDictionary dictionary];
}
