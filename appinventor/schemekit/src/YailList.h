// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

#import "SCMObject.h"

/**
 * # Overview
 *
 * This is the overview.
 *
 * ## Subsection
 *
 * This is a subsection
 *
 * ### Subsubsection
 */
@interface YailList<ObjectType> : NSMutableArray<ObjectType> <SCMObject>

/// ------------------------------------
/// Creating and Initializing a YailList
/// ------------------------------------

/**
 * Creates an empty YailList.
 *
 * @param interpreter The SCMInterpreter that will contain the YailList
 */
+ (nonnull instancetype)emptyListIn:(nonnull SCMInterpreter *)interpreter;

/**
 * Wraps an existing @c list from YAIL as a @c YailList.
 *
 * @param list First cons of a list in YAIL
 * @param interpreter The @c SCMInterpreter that contains the YailList
 */
+ (nonnull instancetype)wrapList:(pic_value)list
                 fromInterpreter:(nonnull SCMInterpreter *)interpreter;
/**
 * Creates a new @c YailList from a nil-terminated list of objects.
 *
 * @param interpreter The @c SCMInterpreter that will contain the @c YailList
 * @param value A variable argument list of values to populate the @c YailList
 */
+ (nonnull instancetype)listInInterpreter:(nonnull SCMInterpreter *)interpreter
                                 ofValues:(nonnull ObjectType)value, ... NS_REQUIRES_NIL_TERMINATION;

/**
 * Initializes a @c YailList with the contents of @c array, allocated in the memory space of the
 * @c interpreter.
 *
 * @param array The @c NSArray from which to build an equivalent @c YailList
 * @param interpreter The @c SCMInterpreter that contains the YailList
 */
- (nonnull instancetype)initWithArray:(nonnull NSArray<ObjectType> *)array
                        inInterpreter:(nonnull SCMInterpreter *)interpreter
    NS_DESIGNATED_INITIALIZER;

/**
 * Initializes a @c YailList representing the @c list from @c interpreter.
 *
 * @param list First cons of the list containing the @c *list* symbol
 * @param interpreter The @c SCMInterpreter that contains the @c list
 */
- (nonnull instancetype)initWithList:(pic_value)list
                     fromInterpreter:(nonnull SCMInterpreter *)interpreter
    NS_DESIGNATED_INITIALIZER;

/// -------------------
/// Querying a YailList
/// -------------------

/**
 * The length of the YailList as it appears in the blocks language.
 */
@property (atomic, readonly) NSInteger length;

/**
 * A Boolean value that indicates if the YailList is empty.
 *
 * This is more efficient than testing whether length is 0 because length requires walking the linked list to count its elements.
 *
 * @returns YES if the YailList is empty, otherwise NO.
 */
@property (atomic, readonly, getter=isEmpty) BOOL empty;

@end

@interface NSArray (YailList)

/**
 * Converts the receiver from a @c NSArray into a @c YailList instance.
 *
 * If the object is a @c YailList then it will return itself rather than making a copy.
 *
 * @param interpreter The interpreter to use for interning values
 */
- (nonnull YailList<id> *)yailListUsingInterpreter:(nonnull SCMInterpreter *)interpreter;

/**
 * Tests whether the collection contains any items. For linked list implementations of @c NSArray
 * this is faster than checking @c count for a zero value.
 */
@property (atomic, readonly, getter=isEmpty) BOOL empty;

@end
