// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <objc/runtime.h>
#import "SCMNameResolver.h"
#import "SCMMethod.h"

static NSMutableDictionary<NSString *, NSMutableDictionary<NSString *, SCMMethod *> *> *methodLookupDict = nil;
static NSString *JAVA_PACKAGE = @"com.google.appinventor.components.runtime";
static NSString *JAVA_UTIL_PACKAGE =
    @"com.google.appinventor.components.runtime.util";
static NSString *JAVA_COMMON_PACKAGE = @"com.google.appinventor.components.common";
static NSString *JAVA_EXTENSION_PACKAGE = @"edu.mit.appinventor.";
static NSString *SWIFT_PACKAGE = @"AIComponentKit";
static NSMutableDictionary<NSString *, Protocol *> *preregisteredProtocols = nil;

@implementation SCMNameResolver

/**
 *  Looks up the methods for a Class and populates them into methodLookupDict.
 *
 *  @param clazz The Class object to reflect methods for.
 *
 *  @return A dictionary mapping method names to {@link SCMMethod} instances.
 */
+ (NSDictionary<NSString *, SCMMethod *> *)lookupMethodsForClass:(Class)clazz {
  if (clazz == nil) {
    // Prevent infinite recursion because the class of nil is nil
    return [NSDictionary dictionary];
  }
  if (!methodLookupDict) {
    methodLookupDict = [[NSMutableDictionary alloc] init];
  }
  NSString *className = [NSString stringWithUTF8String:class_getName(clazz)];
  NSMutableDictionary<NSString *, SCMMethod *> *result = [methodLookupDict objectForKey:className];
  if (result) {
    return result;
  }
  if (clazz != [NSObject class]) {
    result = [[NSMutableDictionary alloc] initWithDictionary:[SCMNameResolver lookupMethodsForClass:class_getSuperclass(clazz)]];
  } else {
    result = [[NSMutableDictionary alloc] init];
  }
  methodLookupDict[className] = result;

  unsigned int count = 0;
  unsigned int registeredMethods = 0;
  Method *methods;
  
  // Static (class) methods
  methods = class_copyMethodList(object_getClass(clazz), &count);
  if (count > 0) {
    // Add new static method names
    for (unsigned int i = 0; i < count; ++i) {
      @try {
        SCMMethod *method = [[SCMMethod alloc] initWithMethod:methods[i] forClass:clazz isStatic:YES];
        result[method.fullName] = method;
        result[method.yailName] = method;
        ++registeredMethods;
      } @catch (NSException *e) {
        // invalid method signature
      }
    }
    free(methods);
  }

  methods = class_copyMethodList(clazz, &count);
  if (count > 0) {
    // Add new instance method names
    for (unsigned int i = 0; i < count; ++i) {
      @try {
        SCMMethod *method = [[SCMMethod alloc] initWithMethod:methods[i] forClass:clazz isStatic:NO];
        result[method.fullName] = method;
        result[method.yailName] = method;
        ++registeredMethods;
      } @catch (NSException *e) {
        // invalid method signature
      }
    }
    free(methods);
  }
  return result;
}


+ (Class)classFromQualifiedName:(const char *)name {
  NSString *localName = [NSString stringWithUTF8String:name];
  if ([localName hasPrefix:JAVA_UTIL_PACKAGE]) {
    if ([localName hasSuffix:@".YailDictionary"]) {
      localName = @"YailDictionary";
    } else if ([localName hasSuffix:@".YailList"]) {
      localName = @"YailList";
    } else {
      localName = [localName
                   stringByReplacingOccurrencesOfString:JAVA_UTIL_PACKAGE
                   withString:SWIFT_PACKAGE];
    }
  } else if ([localName hasPrefix:JAVA_PACKAGE]) {
    localName = [localName stringByReplacingOccurrencesOfString:JAVA_PACKAGE withString:SWIFT_PACKAGE];
  } else if ([localName hasPrefix:JAVA_COMMON_PACKAGE]) {
    localName = [localName stringByReplacingOccurrencesOfString:JAVA_COMMON_PACKAGE withString:SWIFT_PACKAGE];
  } else if ([localName hasPrefix:JAVA_EXTENSION_PACKAGE]) {
    localName = [NSString stringWithFormat:@"AIComponentKit.%@", [localName componentsSeparatedByString:@"."].lastObject];
  }
  return NSClassFromString(localName);
}


+ (Protocol *)protocolFromQualifiedName:(const char *)name {
  NSString *localName = [NSString stringWithUTF8String:name];
  if ([localName hasPrefix:JAVA_PACKAGE]) {
    localName = [localName stringByReplacingOccurrencesOfString:JAVA_PACKAGE withString:SWIFT_PACKAGE];
  }
  Protocol *result = NSProtocolFromString(localName);
  if (preregisteredProtocols && !result) {
    result = preregisteredProtocols[[NSString stringWithCString:name encoding:NSUTF8StringEncoding]];
  }
  return result;
}


+ (NSDictionary<NSString *, SCMMethod *> *)methodsForClass:(Class)clazz {
  return [SCMNameResolver lookupMethodsForClass:clazz];
}


+ (SCMMethod *)initializerForClass:(Class)clazz withName:(const char *)name {
  NSString *initializerName = [NSString stringWithUTF8String:name];
  NSDictionary<NSString *, SCMMethod *> *methods = [SCMNameResolver methodsForClass:clazz];
  return methods[initializerName];
}


+ (SCMMethod *)initializerWithArgForClass:(Class)clazz withName:(const char *)name {
  NSString *initializerName = [NSString stringWithFormat:@"%s:", name];
  NSDictionary<NSString *, SCMMethod *> *methods = [SCMNameResolver methodsForClass:clazz];
  return methods[initializerName];
}


+ (SCMMethod *)naryInitializerForClass:(Class)clazz withName:(const char *)name argCount:(NSInteger)args {
  NSMutableString *initializerName = [NSMutableString stringWithUTF8String:name];
  for (NSInteger i = 0; i < args; ++i) {
    [initializerName appendString:@":"];
  }
  NSDictionary<NSString *, SCMMethod *> *methods = [SCMNameResolver methodsForClass:clazz];
  return methods[initializerName];
}


+ (SCMMethod *)methodForClass:(Class)clazz withName:(const char *)name argumentTypeList:(NSArray *)args {
  NSDictionary<NSString *, SCMMethod *> *methods = [SCMNameResolver methodsForClass:clazz];
  NSString *methodName = [NSString stringWithUTF8String:name];
  SCMMethod *method = methods[methodName];
  // TODO(ewpatton): Implement logic to check argument lists
  return method;
}


+ (SCMMethod *)setterForProperty:(const char *)name inClass:(Class)clazz withType:(NSString *)type {
  NSString *setterName = [NSString stringWithFormat:@"set%s", name];
  NSDictionary<NSString *, SCMMethod *> *methods = [SCMNameResolver methodsForClass:clazz];
  SCMMethod *method = methods[setterName];
  // TODO(ewpatton): Implement logic to check argument type
  return method;
}

+ (void)registerProtocol:(Protocol *)proto forName:(NSString *)name {
  if (!preregisteredProtocols) {
    preregisteredProtocols = [[NSMutableDictionary alloc] init];
  }
  preregisteredProtocols[name] = proto;
}

@end
