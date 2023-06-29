// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import <SchemeKit/picrin.h>
#import "SCMObject.h"

@class SCMInterpreter;

/**
 * The @c SCMSymbol class provides an Objective-C view on a Scheme
 * symbol. A textual representation of the symbol can be accessed
 * using the @c name property.
 */
@interface SCMSymbol : NSObject<SCMObject, NSCoding, NSCopying>

/**
 * Creates a new @c SCMSymbol to represent the given @c symbol from
 * the given @c interpreter.
 *
 * @param symbol the opaque pointer to the symbol in the scheme interpreter
 * @param interpreter the interpreter object containing the symbol
 */
+ (instancetype)symbol:(pic_value)symbol inInterpreter:(SCMInterpreter *)interpreter;

/**
 * Initializes a new @c SCMSymbol to represent the given @c symbol
 * from the given @c interpreter.
 *
 * @param symbol the opaque pointer to the symbol in the scheme interpreter
 * @param interpreter the interpreter object containing the symbol
 */
- (instancetype)initWithSymbol:(pic_value)symbol inInterpreter:(SCMInterpreter *)interpreter;

/**
 * Returns the @c SCMSymbol as a @c NSString instance.
 */
@property (readonly, copy) NSString *name;

@end
