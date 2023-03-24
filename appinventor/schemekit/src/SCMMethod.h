// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <objc/runtime.h>

/**
 * The @c SCMMethod class encapsulates information about an
 * Objective-C method. It's primary use is to store mappings of
 * potential methods invokable from the Scheme code via the @c invoke
 * function.
 */
@interface SCMMethod : NSObject<NSCopying>

/**
 * Initializes a new @c SCMMethod object that represents the given @c
 * method of @c clazz and whether that method should be invokved statically.
 *
 * @param method A @c Method object from the Objective-C runtime
 * @param clazz The @c Class object declaring the method
 * @param isStatic @c YES if the method is defined on the class, @c NO if
 *                 it should be called on an instance of the class.
 */
- (instancetype)initWithMethod:(Method)method forClass:(Class)clazz isStatic:(BOOL)isStatic;

/**
 * Returns an unbound invocation object for use at a later time.
 */
- (NSInvocation *)unboundInvocation;

/**
 * Returns an invocation of the underlying Objective-C method to be
 * sent to the given @c target .
 *
 * @param target An object of type @c clazz that will perform the method
 */
- (NSInvocation *)invocationForInstance:(id)target;

/**
 * Returns a static invocation of the underlying Objective-C method.
 */
- (NSInvocation *)staticInvocation;

/**
 * The short name invokable from YAIL.
 */
@property (nonatomic, readonly) NSString *yailName;

/**
 * The full method name. For example, @c initWithMethod:forClass:isStatic:
 */
@property (nonatomic, readonly) NSString *fullName;

@end
