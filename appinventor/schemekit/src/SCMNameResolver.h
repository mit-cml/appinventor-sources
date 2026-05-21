// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

@class SCMMethod;

@interface SCMNameResolver : NSObject

/**
 *  Look up a Class in the Objective-C/Swift runtime given the qualified name.
 *
 *  @param name Qualified name of the class to reflect.
 *
 *  @return A Class if the class is available in the current runtime, otherwise nil.
 */
+ (Class)classFromQualifiedName:(const char *)name;

/**
 *  Look up a Protocol in the Objective-C/Swift runtime given the qualified name.
 *
 *  @param name Qualified name of the class to reflect.
 *
 *  @return A Protocol if the class is available in the current runtime, otherwise nil.
 */
+ (Protocol *)protocolFromQualifiedName:(const char *)name;

/**
 *  Find a no-arg constructor for the class with the given name.
 *
 *  @param clazz Class to search for the initializer.
 *  @param name Name of the initializer, typically @"init".
 *
 *  @return A {@link SCMMethod} for invoking the constructor of the class, or nil if no such constructor exists.
 */
+ (SCMMethod *)initializerForClass:(Class)clazz withName:(const char *)name;

/**
 *  Find a 1-arg constructor for the class with the given name.
 *
 *  @param clazz Class to search for the initializer.
 *  @param name Name of the initializer, typically @"init".
 *
 *  @return A {@link SCMMethod} for invoking the constructor of the class, or nil if no such constructor exists.
 */
+ (SCMMethod *)initializerWithArgForClass:(Class)clazz withName:(const char *)name;

/**
 *  Find an n-ary initializer for the class with the given name.
 *
 *  @param clazz Class to search for the initializer.
 *  @param name Name of the initializer, typically @"init".
 *  @param args The number of arguments the constructor accepts.
 *
 *  @return A {@link SCMMethod} for invoking the constructor of the class, or nil if no such constructor exists.
 */
+ (SCMMethod *)naryInitializerForClass:(Class)clazz withName:(const char *)name argCount:(NSInteger)args;

/**
 *  Find a method for the class with the given name and argument type list.
 *
 *  @param clazz Class to perform lookup against.
 *  @param name Name of the method to locate.
 *  @param args Argument list for the method. This will be an array of zero or more of the following strings:
 *    - boolean
 *    - number
 *    - string
 *    - list
 *    - component
 *    - any
 *  Due to the nature of the Objective-C runtime, string, list, component, and any are encoded exactly the same. The compiler must perform static type checking based on the signature information captured in simple_components.json.
 *
 *  @return A {@link SCMMethod} object if the method is found, otherwise nil.
 */
+ (SCMMethod *)methodForClass:(Class)clazz withName:(const char *)name argumentTypeList:(NSArray *)args;

/**
 *  Find a property setter for the class with the given name and type.
 *
 *  @param name Name of the property to retrieve a setter for.
 *  @param clazz Class definition containing the setter.
 *  @param type Yail type of the argument that will be passed to the setter.
 *
 *  @return A {@link SCMMethod} object if the property setter is found, otherwise nil.
 */
+ (SCMMethod *)setterForProperty:(const char *)name inClass:(Class)clazz withType:(NSString *)type;

/**
 * Register a protocol for a given name. This was introduced for iOS 13 where for some reason
 * NSProtocolFromString no longer returns protocols in production.
 */
+ (void)registerProtocol:(Protocol *)proto forName:(NSString *)name;

@end
