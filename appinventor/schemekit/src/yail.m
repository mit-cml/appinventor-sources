// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#import "SCMNameResolver.h"
#import "SCMMethod.h"
#import "SCMTypes.h"
#include "picrin.h"
#include "picrin/extra.h"
#include "picrin/private/object.h"
#include "picrin/private/state.h"
#include <stdlib.h>

static pic_value
yail_invoke_internal(pic_state *pic, NSInvocation *method, int argc, pic_value args[]);

static NSTimeZone *timeZone = nil;
static NSMutableDictionary<NSString *, id> *aliasMap = nil;

@class ProtocolWrapper;
@class ValueHolder;

/** Used to maintain references to objects GCed by Picrin */
static NSMutableDictionary<id, ValueHolder *> *objects = nil;
static NSMutableDictionary<NSString *, ProtocolWrapper *> *protocols = nil;

@interface ClassWrapper: NSObject<NSCopying> {
  Class class_;
  NSString *name_;
}
- (instancetype)initWithClass:(Class)aClass;
@property (readonly) Class class;
@end

@implementation ClassWrapper

- (instancetype)initWithClass:(Class)aClass {
  if (self = [super init]) {
    self->class_ = aClass;
    self->name_ = NSStringFromClass(aClass);
  }
  return self;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

- (NSUInteger)hash {
  return [name_ hash];
}

- (BOOL)isEqual:(id)object {
  if (object == nil) {
    return NO;
  } else if ([object isKindOfClass:[ClassWrapper class]]) {
    return class_ == ((ClassWrapper *) object)->class_;
  }
  return NO;
}

- (NSString *)description {
  return name_;
}

@synthesize class = class_;

@end

@interface ProtocolWrapper: NSObject<NSCopying> {
  Protocol *protocol_;
  NSString *name_;
}
- (instancetype)initWithProtocol:(Protocol *)protocol;
@property (readonly) Protocol *protocol;
@end

@implementation ProtocolWrapper

- (instancetype)initWithProtocol:(Protocol *)protocol {
  if (self = [super init]) {
    self->protocol_ = protocol;
    self->name_ = NSStringFromProtocol(protocol);
  }
  return self;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

- (NSUInteger)hash {
  return [name_ hash];
}

- (BOOL)isEqual:(id)object {
  if (object == nil) {
    return NO;
  } else if ([object isKindOfClass:[ProtocolWrapper class]]) {
    return protocol_ == ((ProtocolWrapper *) object)->protocol_;
  }
  return NO;
}

@synthesize protocol = protocol_;

@end

@interface CopyableReference : NSObject<NSCopying> {
@public
  id ref;
}

+ (instancetype)referenceWithObject:(id)object;

@end

@implementation CopyableReference

- (instancetype)initWithObject:(id)object {
  if (self = [super init]) {
    self->ref = object;
  }
  return self;
}

+ (instancetype)referenceWithObject:(id)object {
  if (object == nil) {
    return nil;
  }
  return [[CopyableReference alloc] initWithObject:object];
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

- (NSUInteger)hash {
  if (self->ref == nil) {
    return 0;
  }
  return [self->ref hash];
}

- (BOOL)isEqual:(id)object {
  if (object == nil) {
    return NO;
  }
  if ([object isKindOfClass:[CopyableReference class]]) {
    object = ((CopyableReference *)object)->ref;
  }
  if (object == self->ref) {
    return YES;
  } else if (![[object class] conformsToProtocol:@protocol(SCMObject)]) {
    return [object isEqual:self->ref];
  } else {
    return NO;
  }
}

@end

/**
 * @c ValueHolder wraps an Objective-C/Swift object in the form of a Picrin value and manages
 * whether it is garbage collected using a reference counter.
 */
@interface ValueHolder : NSObject {
@private
  pic_value value_;
}

/**
 * Allocate a new reference counter for the given value.
 *
 * @param value the opaque scheme value wrapping the reference-counted Objective-C object
 */
+ (instancetype)holderForValue:(pic_value)value;

@property (readonly) pic_value value;

@end

@implementation ValueHolder

+ (instancetype)holderForValue:(pic_value)value {
  return [[ValueHolder alloc] initWithValue:value];
}

- (instancetype)initWithValue:(pic_value)value {
  if (self = [super init]) {
    value_ = value;
  }
  return self;
}

@synthesize value = value_;

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
  __weak id object_;
};

struct native_scmvalue {
  OBJECT_HEADER
  __weak SCMValue *object_;
};

struct native_yaillist {
  OBJECT_HEADER
  __weak YailList *object_;
};

struct native_yaildict {
  OBJECT_HEADER
  __weak YailDictionary *object_;
};

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

bool
pic_pair_p(pic_state *pic, pic_value v) {
  return (pic_type(pic, v) == PIC_TYPE_PAIR) || yail_list_p(pic, v);
}

pic_value
yail_list_value(pic_state *pic, pic_value v) {
  if (!yail_list_p(pic, v)) {
    return pic_nil_value(pic);
  }
  id object = yail_native_instance_ptr(pic, v)->object_;
  if (object == nil || ![object isKindOfClass:[YailList class]]) {
    return pic_nil_value(pic);
  }
  return ((YailList *) object).value;
}

struct pair *
pic_pair_ptr(pic_state *pic, pic_value v) {
  assert(pic_pair_p(pic, v));
  if (yail_list_p(pic, v)) {
    return pic_pair_ptr(pic, yail_list_value(pic, v));
  } else {
    return (struct pair *)pic_obj_ptr(v);
  }
}

pic_value
yail_make_instance(pic_state *pic);

#pragma mark Native Class values

pic_value
yail_make_native_class(pic_state *pic, Class clazz) {
  ValueHolder *wrappedValue = nil;
  ClassWrapper *wrapper = [[ClassWrapper alloc] initWithClass:clazz];
  if ((wrappedValue = [objects objectForKey:wrapper])) {
    return wrappedValue.value;
  }
  native_class *native = (native_class *)pic_obj_alloc(pic, sizeof(native_class), YAIL_TYPE_CLASS);
  native->class_ = clazz;
  pic_value value = pic_obj_value(native);
  objects[wrapper] = [ValueHolder holderForValue:value];
  return value;
}

const char *
yail_native_class_name(pic_state *PIC_UNUSED(pic), struct native_class *data) {
  return [NSStringFromClass(data->class_) UTF8String];
}

void
yail_native_class_dtor(pic_state *pic, struct native_class *data) {
  ClassWrapper *wrapper = [[ClassWrapper alloc] initWithClass:data->class_];
  ValueHolder *holder = objects[wrapper];
  if (holder) {
#ifdef MEMDEBUG
    NSLog(@"Deallocating class %@", [wrapper description]);
#endif
    [objects removeObjectForKey:wrapper];
  }
}

#pragma mark Native Protocol values

pic_value
yail_make_native_protocol(pic_state *pic, Protocol *protocol) {
  ValueHolder *wrappedValue = nil;
  ProtocolWrapper *wrapper = [protocols objectForKey:NSStringFromProtocol(protocol)];
  if (!wrapper) {
    wrapper = [[ProtocolWrapper alloc] initWithProtocol:protocol];
  }
  if (wrapper) {
    if ((wrappedValue = [objects objectForKey:wrapper])) {
      return wrappedValue.value;
    }
    [protocols setObject:wrapper forKey:NSStringFromProtocol(protocol)];
    native_protocol *native = (native_protocol *)pic_obj_alloc(pic, sizeof(native_protocol), YAIL_TYPE_PROTOCOL);
    native->protocol_ = protocol;
    pic_value value = pic_obj_value(native);
    objects[wrapper] = [ValueHolder holderForValue:value];
    return value;
  } else {
    return pic_undef_value(pic);
  }
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
  ValueHolder *wrappedValue = nil;
  if ((wrappedValue = [objects objectForKey:method])) {
    return wrappedValue.value;
  }
  native_method *native = (native_method *)pic_obj_alloc(pic, sizeof(native_method), YAIL_TYPE_METHOD);
  native->method_ = method;
  pic_value value = pic_obj_value(native);
  objects[method] = [ValueHolder holderForValue:value];
  return value;
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

static pic_value
yail_make_native_instance_internal(pic_state *pic, id object, int type) {
  ValueHolder *wrappedValue = nil;
  CopyableReference *ref = [CopyableReference referenceWithObject:object];
  if ((wrappedValue = [objects objectForKey:ref])) {
    return wrappedValue.value;
  }
#ifdef MEMDEBUG
  NSLog(@"Allocating picrin object for <%@ %p> %@",
        NSStringFromClass([object class]), object, [object debugDescription]);
#endif
  size_t ai = pic_enter(pic);  // prevent this from turning into a strong reference
  native_instance *native = (native_instance *)pic_obj_alloc(pic,
      sizeof(native_instance), type);
  pic_leave(pic, ai);
  native->object_ = object;
  pic_value value = pic_obj_value(native);
  objects[ref] = [ValueHolder holderForValue:value];
  return value;
}

const char *
yail_format_native_instance(pic_state *pic, pic_value o) {
  id object = yail_native_instance_ptr(pic, o)->object_;
  const char *buffer = [[object debugDescription] UTF8String];
  return buffer;
}

pic_value
yail_make_native_instance(pic_state *state, id object) {
  int type = YAIL_TYPE_INSTANCE;
  if ([object isKindOfClass:[YailList class]]) {
    type = YAIL_TYPE_LIST;
  } else if ([object isKindOfClass:[YailDictionary class]]) {
    type = YAIL_TYPE_DICT;
  }
  return yail_make_native_instance_internal(state, object, type);
}

pic_value
yail_make_native_yaillist(pic_state *state, YailList *list) {
  return yail_make_native_instance_internal(state, list, YAIL_TYPE_LIST);
}

pic_value
yail_make_native_yaildictionary(pic_state *state, YailDictionary *dict) {
  return yail_make_native_instance_internal(state, dict, YAIL_TYPE_DICT);
}

static pic_value
pic_yail_make_yail_list(pic_state *state) {
  pic_value head;

  pic_get_args(state, "o", &head);

  head = pic_cons(state, pic_intern_cstr(state, "*list*"), head);

  return yail_make_native_yaillist(state, [YailList wrapList:head fromInterpreter:SCMInterpreter.shared]);
}

const char *
yail_native_instance_typename(pic_state *PIC_UNUSED(pic), struct native_instance *obj) {
  id object = obj->object_;
  Class clazz = [object class];
  NSString *className = NSStringFromClass(clazz);
  return [className UTF8String];
}

id
yail_native_instance_objc(pic_state *PIC_UNUSED(pic), pic_value value) {
  assert(pic_type(pic, value) == YAIL_TYPE_INSTANCE);
  return ((struct native_instance *)pic_obj_ptr(value))->object_;
}

void
yail_native_instance_dtor(pic_state *pic, struct native_instance *instance) {
  CopyableReference *ref = [CopyableReference referenceWithObject:instance->object_];
  ValueHolder *value = objects[ref];
  if (value) {
#ifdef MEMDEBUG
    NSLog(@"Deallocating picrin object for <%@ %p> %@",
          NSStringFromClass([instance->object_ class]), (void *)instance,
          [instance->object_ debugDescription]);
#endif
    [objects removeObjectForKey:ref];
    instance->object_ = nil;
  } else {
#ifdef MEMDEBUG
    NSLog(@"No reference counter for deallocated object.");
#endif
    instance->object_ = nil;
  }
}

/// MARK: Other YAIL Types

SCMValue *
yail_scmvalue_objc(pic_state *PIC_UNUSED(pic), pic_value value) {
  assert(pic_type(pic, value) == YAIL_TYPE_VALUE);
  return ((struct native_scmvalue *) pic_obj_ptr(value))->object_;
}

YailList *
yail_list_objc(pic_state *PIC_UNUSED(pic), pic_value value) {
  assert(pic_type(pic, value) == YAIL_TYPE_LIST);
  return ((struct native_yaillist *) pic_obj_ptr(value))->object_;
}

YailDictionary *
yail_dict_objc(pic_state *PIC_UNUSED(pic), pic_value value) {
  assert(pic_type(pic, value) == YAIL_TYPE_DICT);
  return ((struct native_yaildict *) pic_obj_ptr(value))->object_;
}

static pic_value
yail_invoke_native(pic_state *pic) {
  pic_value method;
  pic_value *args;
  int n;
  method = pic_closure_ref(pic, 0);
  pic_get_args(pic, "*", &n, &args);
  SCMMethod *scmMethod = yail_native_method_ptr(pic, method)->method_;
  return yail_invoke_internal(pic, [scmMethod staticInvocation], n, args);
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
    Class clazz = aliasMap[targetName];
    if (clazz == nil) {
      clazz = [SCMNameResolver classFromQualifiedName:[targetName UTF8String]];
    }
    if (clazz != nil) {
      SCMMethod *method = [SCMNameResolver methodForClass:clazz withName:[methodName UTF8String] argumentTypeList:@[]];
      if (method != nil) {
        pic_value val = pic_lambda(pic, yail_invoke_native, 1, yail_make_native_method(pic, method));
        pic_weak_set(pic, pic->globals, uid, val);
        return 0;
      }
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
    if (number.doubleValue > INT_MAX || number.doubleValue < INT_MIN) {
      return pic_float_value(pic, number.doubleValue);
    }
    return pic_int_value(pic, (int) number.integerValue);
  } else if (0 == strcmp(type, @encode(NSUInteger))) {
    //TODO: handle overflow
    if (number.doubleValue > LONG_MAX || number.doubleValue < LONG_MIN) {
      return pic_float_value(pic, number.doubleValue);
    }
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
    // Check if the first element is *list* and add it if missing before making the YailList
    if (!pic_sym_p(pic, pic_car(pic, value))) {
      value = pic_cons(pic, pic_intern_cstr(pic, "*list*"), value);
    }
    return [YailList wrapList:value fromInterpreter:SCMInterpreter.shared];
  } else if (pic_str_p(pic, value)) {
    return [NSString stringWithCString:pic_str(pic, value) encoding:NSUTF8StringEncoding];
  } else if (pic_int_p(pic, value)) {
    return [NSNumber numberWithInt:pic_int(pic, value)];
  } else if (pic_float_p(pic, value)) {
    return [NSNumber numberWithDouble:pic_float(pic, value)];
  } else if (pic_true_p(pic, value)) {
    return [NSNumber numberWithBool:YES];
  } else if (pic_false_p(pic, value)) {
    return [NSNumber numberWithBool:NO];
  } else if (yail_native_instance_p(pic, value)) {
    return yail_native_instance_ptr(pic, value)->object_;
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
  } else if (!yail_native_class_p(pic, native_object) && !yail_native_instance_p(pic, native_object)) {
    const char *buffer;
    int len;
    pic_value port = pic_fmemopen(pic, NULL, 256, "w");
    pic_fprintf(pic, port, "invoke: unable to invoke method `~a` in object of type ~a",
        native_method ,pic_cstr_value(pic, pic_typename(pic, pic_type(pic, native_object))));
    pic_fgetbuf(pic, port, &buffer, &len);
    pic_value error = pic_make_error(pic, "", buffer, pic_nil_value(pic));
    pic_fclose(pic, port);
    pic_raise(pic, error);
    return pic_undef_value(pic);
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
  return yail_invoke_internal(pic, invocation, argc, args);
}

static pic_value
yail_invoke_internal(pic_state *pic, NSInvocation *invocation, int argc, pic_value args[]) {
  [invocation retainArguments];
  // first 2 args in Obj-C are reserved for target and selector
  for (int i = 0, j = 2; i < argc; ++i, ++j) {
    if (pic_float_p(pic, args[i])) {
      switch([invocation.methodSignature getArgumentTypeAtIndex:j][0]) {
        case '@': {
          NSNumber *value = [NSNumber numberWithDouble:pic_float(pic, args[i])];
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'f': {
          float value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'q': {
          long long value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'Q': {
          unsigned long long value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'l': {
          long value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'L': {
          unsigned long value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'i': {
          int value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'I': {
          unsigned int value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 's': {
          short value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'S': {
          unsigned short value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'c': {
          char value = pic_float(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'C': {
          unsigned char value = pic_float(pic, args[i]);
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
          NSNumber *value = [NSNumber numberWithInteger:pic_int(pic, args[i])];
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'q': {
          long long value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'Q': {
          unsigned long long value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 's': {
          short value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'S': {
          unsigned short value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'c': {
          char value = pic_int(pic, args[i]);
          [invocation setArgument:&value atIndex:j];
          break;
        }
        case 'C': {
          unsigned char value = pic_int(pic, args[i]);
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
        NSNumber *value = [NSNumber numberWithBool:YES];
        [invocation setArgument:&value atIndex:j];
      } else {
        BOOL value = YES;
        [invocation setArgument:&value atIndex:j];
      }
    } else if (pic_false_p(pic, args[i])) {
      if ([invocation.methodSignature getArgumentTypeAtIndex:j][0] == '@') {
        NSNumber *value = [NSNumber numberWithBool:NO];
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
    } else if (pic_proc_p(pic, args[i])) {
      id value = [[SCMProcedure alloc] initWithProcedure:args[i] interpreter:SCMInterpreter.shared];
      [invocation setArgument:&value atIndex:j];
    } else {
      NSString *methodName = NSStringFromSelector(invocation.selector);
      NSLog(@"incompatible yail type received %s in call to %@ at index %d",
            pic_typename(pic, pic_type(pic, args[i])), methodName, i);
      pic_error(pic, "incompatible yail type received", 1,
                pic_cstr_value(pic, pic_typename(pic, pic_type(pic, args[i]))));
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
      if (sizeof(NSInteger) == sizeof(unsigned int) ||
          (INT_MIN <= (long) value && (long) value <= INT_MAX)) {
        return pic_int_value(pic, (int) value);
      } else {
        return pic_float_value(pic, (double) value);
      }
    } else if (0 == strcmp(retType, @encode(NSUInteger))) {
      NSUInteger value = 0;
      [invocation getReturnValue:&value];
      //TODO: handle overflow
      if (sizeof(NSUInteger) == sizeof(unsigned int) ||
          (unsigned long) value <= INT_MAX) {
        return pic_int_value(pic, (int) value);
      } else {
        return pic_float_value(pic, (double) value);
      }
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
        return yail_null_value(pic);
      } else if ([value isKindOfClass:[YailDictionary class]]) {
        return yail_make_native_instance(pic, value);
      } else if ([value isKindOfClass:[YailList class]]) {
        return yail_make_native_instance(pic, value);
      } else if ([[value class] conformsToProtocol:@protocol(SCMValue)]) {
        return [(id<SCMValue>)value value];
      } else if ([value isKindOfClass:[NSString class]]) {
        const char *str = [(NSString *)value UTF8String];
        return pic_str_value(pic, str, (int)strlen(str));
      } else if ([value isKindOfClass:[NSArray class]]) {
        return [[[YailList alloc] initWithArray:value inInterpreter:SCMInterpreter.shared] value];
      } else if ([value isKindOfClass:[NSDictionary class]]) {
        return yail_make_native_instance(pic, [[YailDictionary alloc] initWithDictionary:value]);
      } else if ([value isKindOfClass:[NSNumber class]]) {
        NSNumber *num = (NSNumber *)value;
        if (0==strcmp(num.objCType, @encode(BOOL)) || 0==strcmp(num.objCType, "c")) {
          return num.boolValue ? pic_true_value(pic) : pic_false_value(pic);
        } else if (0==strcmp(num.objCType, @encode(double)) || 0==strcmp(num.objCType, @encode(float))) {
          return pic_float_value(pic, num.doubleValue);
        } else {
          return pic_int_value(pic, num.intValue);
        }
      } else {
        return yail_make_native_instance(pic, value);
      }
    } else if (0 != strcmp(retType, @encode(void))) {
      pic_error(pic, "yail error: unknown return type for method", 0);
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
  } else if (pic_true_p(pic, result)) {
    return [NSNumber numberWithBool:YES];
  } else if (pic_false_p(pic, result)) {
    return [NSNumber numberWithBool:NO];
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
  } else if (pic_sym_p(pic, native_class)) {
    // We have a symbol representing a FQCN
    NSString *name = [NSString stringWithUTF8String:pic_str(pic, pic_sym_name(pic, native_class))];
    if ([name hasPrefix:@"com.google.appinventor.components.runtime."]) {
      name = [name stringByReplacingOccurrencesOfString:@"com.google.appinventor.components.runtime." withString:@"AIComponentKit."];
    } else if ([name hasPrefix:@"com.google.appinventor.components.common."]) {
      name = [name stringByReplacingOccurrencesOfString:@"com.google.appinventor.components.common." withString:@"AIComponentKit."];
    } else if ([name hasPrefix:@"edu.mit.appinventor."]) {
      NSArray *parts = [name componentsSeparatedByString:@"."];
      name = [NSString stringWithFormat:@"AIComponentKit.%@", parts.lastObject];
    }
    Class clazz = [SCMNameResolver classFromQualifiedName:name.UTF8String];
    if (clazz == nil) {
      pic_error(pic, "Expected a native class or protocol", 1, native_class);
    } else {
      return yail_make_native_class(pic, clazz);
    }
  } else {
    pic_error(pic, "Expected a native class or protocol", 1, native_class);
  }
  
}

pic_value
yail_format_integer(pic_state *pic) {
  static const int BUFSIZE=24;
  double f;
  long i;
  char buf[BUFSIZE];

  pic_get_args(pic, "f", &f);
  i = f;

  snprintf(&buf[0], BUFSIZE, "%ld", i);

  return pic_cstr_value(pic, buf);
}

pic_value
yail_format_inexact(pic_state *pic) {
  static const int BUFSIZE = 24;
  double value, absvalue;
  char buf[BUFSIZE];

  pic_get_args(pic, "f", &value);
  absvalue = fabs(value);

  if (absvalue > 1e6 || absvalue < 1e-6) {
    char work[BUFSIZE];
    snprintf(&work[0], BUFSIZE, "%.5G", value);
    size_t i = 0;
    size_t j = 0;
    BOOL in_exponent = NO;
    BOOL in_prefix = NO;
    while (work[i]) {
      if (work[i] == 'E') {
        in_exponent = YES;
        in_prefix = YES;
        buf[j++] = work[i++];
      } else if (in_exponent && in_prefix) {
        if (work[i] == '-') {
          in_prefix = NO;
          buf[j++] = work[i++];
        } else if (work[i] >= '1' && work[i] <= '9') {
          in_prefix = NO;
          buf[j++] = work[i++];
        } else {
          i++;  // skip this character (either + or 0).
        }
      } else {
        buf[j++] = work[i++];
      }
    }
    buf[j] = 0;  // ensure NULL-terminated string
  } else {
    snprintf(&buf[0], BUFSIZE, "%G", value);
  }

  return pic_cstr_value(pic, buf);
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

  return pic_int_value(pic, (int) (lrand48() / (double)INT_MAX * bound));
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

  return pic_cstr_value(pic, str2);
}

pic_value
yail_string_to_lowercase(pic_state *pic) {
  char *str;

  pic_get_args(pic, "z", &str);
  NSString *lower = [[NSString stringWithUTF8String:str] lowercaseString];
  const char *str2 = [lower cStringUsingEncoding:NSUTF8StringEncoding];

  return pic_cstr_value(pic, str2);
}

pic_value
yail_format_date(pic_state *pic) {
  pic_value picdate;

  pic_get_args(pic, "o", &picdate);
  NSDate *date = (NSDate *) yail_native_instance_ptr(pic, picdate)->object_;
  if (date) {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
    formatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";  // ISO8601 format
    formatter.timeZone = timeZone;
    NSString *datestr = [formatter stringFromDate:date];
    const char *datecstr = [datestr cStringUsingEncoding:NSUTF8StringEncoding];
    return pic_cstr_value(pic, datecstr);
  } else {
    return pic_false_value(pic);
  }
}

void yail_set_time_zone(NSTimeZone *tz) {
  timeZone = tz;
}

pic_value yail_get_class(pic_state *pic) {
  pic_value native_object;

  pic_get_args(pic, "o", &native_object);
  if (yail_native_instance_p(pic, native_object)) {
    id object = yail_native_instance_ptr(pic, native_object)->object_;
    Class clazz = [object class];
    return yail_make_native_class(pic, clazz);
  } else {
    pic_error(pic, "Expected a native object", 1, native_object);
    return pic_undef_value(pic);
  }
}

pic_value yail_get_simple_name(pic_state *pic) {
  pic_value native_class;

  pic_get_args(pic, "o", &native_class);

  if (yail_native_class_p(pic, native_class)) {
    const char *name = yail_native_class_name(pic, yail_native_class_ptr(pic, native_class));
    size_t lastDot = 0;
    for (size_t i = 0; name[i] != 0; i++) {
      if (name[i] == '.') {
        lastDot = i + 1;
      }
    }
    return pic_cstr_value(pic, name + lastDot);
  } else {
    pic_error(pic, "Expected a native class", 1, native_class);
    return pic_undef_value(pic);
  }
}

pic_value
yail_make_dictionary(pic_state *pic) {
  pic_value pairs;

  pic_get_args(pic, "o", &pairs);

  return yail_make_native_yaildictionary(pic, [YailDictionary dictionaryFromPairs:pairs]);
}

pic_value
yail_dictionary_alist_to_dict(pic_state *pic) {
  pic_value entries;

  pic_get_args(pic, "o", &entries);

  if (!yail_list_p(pic, entries)) {
    pic_error(pic, "YailDictionary:alistToDict: YailList required", 1, entries);
  }
  YailList *listOfEntries = yail_list_objc(pic, entries);
  YailDictionary *dict = [YailDictionary dictionaryFromPairs:pic_cdr(pic, listOfEntries.value)];
  return yail_make_native_yaildictionary(pic, dict);
}

static pic_value
yail_define_alias(pic_state *pic) {
  pic_value name, symbol;

  pic_get_args(pic, "oo", &name, &symbol);
  NSString *objcName = [NSString stringWithCString:pic_str(pic, pic_sym_name(pic, name)) encoding:NSUTF8StringEncoding];
  NSString *objcSymbol = [NSString stringWithCString:pic_str(pic, pic_sym_name(pic, symbol)) encoding:NSUTF8StringEncoding];
  objcSymbol = [objcSymbol substringWithRange:NSMakeRange(1, objcSymbol.length - 2)];

  Class clazz = [SCMNameResolver classFromQualifiedName:[objcSymbol UTF8String]];
  if (clazz != nil) {
    aliasMap[objcName] = clazz;
  } else {
    pic_error(pic, "Unrecognized symbol", 1, symbol);
  }

  return pic_undef_value(pic);
}

static pic_value
yail_static_field(pic_state *pic) {
  pic_value class_name, field_name;

  pic_get_args(pic, "oo", &class_name, &field_name);

  Class clazz = yail_native_class_ptr(pic, class_name)->class_;
  const char *field_name_cstr;

  if (pic_sym_p(pic, field_name)) {
    // The `walk all` block uses a symbol rather than a string
    field_name_cstr = pic_str(pic, pic_sym_name(pic, field_name));
  } else if (pic_str_p(pic, field_name)) {
    // Helper blocks use strings to look up fields
    field_name_cstr = pic_str(pic, field_name);
  } else {
    pic_error(pic, "Unknown field name in static-field", 1, field_name);
  }

  SCMMethod *method = [SCMNameResolver methodForClass:clazz
                                             withName:field_name_cstr
                                     argumentTypeList:@[]];
  if (method == nil) {
    return pic_undef_value(pic);
  }

  NSInvocation *invocation = [method staticInvocation];
  [invocation invoke];
  id result = nil;
  [invocation getReturnValue:&result];
  return yail_make_native_instance(pic, result);
}

/// MARK: Memory Management

void
yail_gc_mark(pic_state *pic, pic_value v) {
  id value = yail_native_instance_ptr(pic, v)->object_;
#ifdef MEMDEBUG
  if (looking_for_gcroots) {
    NSString *str = NSStringFromClass([value class]);
    NSLog(@"Type %@", str);
    if ([str containsString:@"Form"]) {
      NSLog(@"Encountered form in GC");
    }
  }
#endif
  if ([value respondsToSelector:@selector(mark)]) {
    [value mark];
  }
}

/**
 * Mark all of the objects strongly referenced by the YAIL boundary to prevent garbage collection.
 *
 * @param pic the picrin state
 */
static void
yail_mark_shared(pic_state *pic) {
  for (id item in objects) {
    if ([item isKindOfClass:[CopyableReference class]]) {
      id ref = ((CopyableReference *) item)->ref;
      if ([ref respondsToSelector:@selector(mark)]) {
        [ref mark];
      }
    }
  }
}

pic_value
yail_get_native_instance(pic_state *pic, id object) {
  CopyableReference *ref = [CopyableReference referenceWithObject:object];
  ValueHolder *holder = objects[ref];
  if (holder) {
    return holder.value;
  }
  return pic_nil_value(pic);
}

pic_value
yail_format(pic_state *pic) {
  pic_value *args;
  int argc;

  pic_get_args(pic, "*", &argc, &args);

  if (argc < 2) {
    pic_error(pic, "format expected at least 2 arguments", 0);
  }

  pic_value dest = args[0];
  const char *format_str = pic_str(pic, args[1]);
  size_t len = strlen(format_str);
  BOOL is_str = NO;
  if (pic_false_p(pic, dest)) {
    dest = pic_fmemopen(pic, NULL, len + 1, "w");
    is_str = YES;
  } else if (pic_true_p(pic, dest)) {
    dest = pic_stdout(pic);
  } else if (!pic_port_p(pic, dest)) {
    pic_error(pic, "Expected argument 1 of format to be #t, #f, or a port", 1, dest);
  }

  int i = 2;
  while (*format_str) {
    if (*format_str == '~') {
      format_str++;
      if (!*format_str) {
        break;
      }
      switch (*format_str) {
        case 'a':
        case 'A':
          if (i >= argc) {
            pic_error(pic, "Too few arguments to format", 0);
          }
          pic_fprintf(pic, dest, "~a", args[i++]);
          break;

        case 's':
        case 'S':
          if (i >= argc) {
            pic_error(pic, "Too few arguments to format", 0);
          }
          pic_fprintf(pic, dest, "~s", args[i++]);
          break;

        case '~':
          pic_fputc(pic, '~', dest);
          break;

        case '%':
          pic_fputc(pic, '\n', dest);
          break;

        default:
          pic_fputc(pic, '~', dest);
          pic_fputc(pic, *format_str, dest);
          break;
      }
    } else {
      pic_fputc(pic, *format_str, dest);
    }
    format_str++;
  }
  if (is_str) {
    const char *result;
    int len;
    pic_fputc(pic, '\0', dest);
    pic_fgetbuf(pic, dest, &result, &len);
    return pic_cstr_value(pic, result);
  } else {
    pic_fflush(pic, dest);
    return pic_undef_value(pic);
  }
}

pic_value
yail_print_type(pic_state *pic) {
  pic_value v;

  pic_get_args(pic, "o", &v);

  NSLog(@"Type: %s", pic_typename(pic, pic_type(pic, v)));

  return pic_undef_value(pic);
}

pic_value
yail_modulo(pic_state *pic) {
  double n, d, result;

  pic_get_args(pic, "ff", &n, &d);

  double remainder = fmod(n, d);

  return pic_float_value(pic, remainder);
}

/// MARK: Initialization

static void
initialize_rand() {
  srand48((long) [[NSDate alloc] init].timeIntervalSince1970 * 1000.0);
}

void
pic_init_yail(pic_state *pic)
{
  pic_value e;
  pic_deflibrary(pic, "yail");
  pic_import(pic, "scheme.base");
  pic_import(pic, "scheme.write");
  pic_import(pic, "picrin.base");
  pic_defun(pic, "yail:find-class", pic_yail_find_class);
  pic_defun(pic, "yail:type?", pic_yail_is_type);
  pic_defun(pic, "yail:set-property!", pic_yail_set_property);
  pic_defun(pic, "yail:get-property", pic_yail_get_property);
  pic_defun(pic, "yail:make-component", pic_yail_make_component);
  pic_defun(pic, "yail:make-random", pic_yail_make_random);
  pic_defun(pic, "yail:make-runtime-error", pic_yail_make_runtime_error);
  pic_defun(pic, "YailList:makeList", pic_yail_make_yail_list);
  pic_defun(pic, "yail:call-instance-method", pic_yail_call_instance_method);
  pic_defun(pic, "yail:call-static-method", pic_yail_call_static_method);
  pic_defun(pic, "yail:make-instance", yail_make_instance);
  pic_defun(pic, "yail:invoke", yail_invoke);
  pic_defun(pic, "invoke", yail_invoke);
  pic_defun(pic, "yail:isa", yail_isa);
  pic_defun(pic, "yail:format-inexact", yail_format_inexact);
  pic_defun(pic, "yail:format-exact", yail_format_integer);
  pic_defun(pic, "yail:modulo", yail_modulo);
  pic_defun(pic, "yail:perform-on-main-thread", yail_perform_on_main_thread);
  pic_defun(pic, "*:getClass", yail_get_class);
  pic_defun(pic, "*:getSimpleName", yail_get_simple_name);
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
  pic_defun(pic, "yail:format-date", yail_format_date);
  pic_defun(pic, "YailDictionary:makeDictionary", yail_make_dictionary);
  pic_defun(pic, "YailDictionary:alistToDict", yail_dictionary_alist_to_dict);
  pic_defun(pic, "yail:define-alias", yail_define_alias);
  pic_defun(pic, "format", yail_format);
  pic_defun(pic, "yail:print-type", yail_print_type);
  pic_defun(pic, "static-field", yail_static_field);
  pic_load_cstr(pic, "(define-syntax define-alias (syntax-rules () ((_ alias name) "
      "(yail:define-alias 'alias 'name))))");
  objects = [NSMutableDictionary dictionary];
  protocols = [NSMutableDictionary dictionary];
  timeZone = [NSTimeZone localTimeZone];
  aliasMap = [NSMutableDictionary dictionary];
  initialize_rand();
}
