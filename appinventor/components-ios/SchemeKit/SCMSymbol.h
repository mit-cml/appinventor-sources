// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.

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
