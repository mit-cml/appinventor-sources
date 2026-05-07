// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import "SCMObject.h"
#import "picrin.h"
#import "YailList.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * @c YailDictionary is an implementation of a key-value store where most operations occur in
 * amortized O(1) time.
 */
@interface YailDictionary : NSMutableDictionary<NSObject *, NSObject *> <SCMObject>

/// ------------------------------------------
/// Creating and Initializing a YailDictionary
/// ------------------------------------------

/**
 * Creates an empty @c YailDictionary.
 *
 * @param interpreter The @c SCMInterpreter that will contain the @c YailDictionary.
 */
+ (nonnull instancetype)emptyDictionaryIn:(nonnull SCMInterpreter *)interpreter;

/**
 * Creates a new @c YailDictionary containing the given @c keys and @c objects in @c interpreter.
 *
 * It is an error if @c keys and @c objects are of different lengths.
 *
 * @param interpreter The @c SCMInterpreter that will contain the @c YailDictionary .
 * @param objects The values for the dictionary.
 * @param keys The keys for the dictionary.
 */
+ (nonnull instancetype)dictionaryInInterpreter:(nonnull SCMInterpreter *)interpreter
                                    withObjects:(NSArray<id> *)objects
                                        forKeys:(NSArray<id<NSCopying>> *)keys;

/**
 * Creates a new @c YailDictionary containing key-value mappings represented by the given @c pairs.
 * This uses the default interpreter.
 *
 * @param pairs A list of key-value pairs with which to populate the dictionary.
 */
+ (nonnull instancetype)dictionaryFromPairs:(pic_value)pairs;

/**
 * Constant value representing the <code>walk all at level</code> block.
 */
+ (nonnull id)ALL;


/// -------------------------
/// Querying a YailDictionary
/// -------------------------

/**
 * Gets the object at the corresponding @c path rooted at this dictionary.
 *
 * @return the value at the leaf of the @c path, or @c nil if the path terminates earlier.
 */
- (nullable id)getObjectAtKeyPath:(NSArray<id> *)path
                            error:(NSError ** _Nullable)error;

/**
 * Walks the object graph using the given @c keysOrIndices rooted at the receiver, returning an
 * array of values seen at the ends of each path.
 */
- (nullable NSArray<id> *)walkKeyPath:(NSArray<id> *)keysOrIndices
                                error:(NSError * * _Nullable)error;

/**
 * Tests whether the object exists in the dictionary (as a value).
 *
 * @param object the value to test for membership in the dictionary.
 * @return @c YES if the object is a value in the dictionary, @c NO otherwise.
 */
- (BOOL)containsObject:(id)object;

/**
 * Tests whether the given key exists in the dictionary.
 *
 * @param key the key to test for membership in the dictionary.
 * @return @c YES if the object is a key in the dictionary, @c NO otherwise.
 */
- (BOOL)containsKey:(id)key;

/**
 * Gets the object at the given @c index in the @c YailDictionary.
 *
 * @param index the index of the item to retrieve.
 * @return the object at @c index, or @c nil if the index was beyond the end of the dictionary.
 */
- (nullable id)objectAtIndex:(NSUInteger)index;

/**
 * Tests whether the dictionary is empty.
 *
 * @return @c YES if the dictionary is empty, @c NO otherwise.
 */
@property (readonly, atomic, getter=isEmpty) BOOL empty;

/// -------------------------
/// Updating a YailDictionary
/// -------------------------

/**
 * Sets or updates the entry at @c keyPath to the @c object.
 *
 * If the @c YailDictionary is successfully updated, @c YES will be returned. Otherwise, @c NO is
 * returned. If the @c keyPath is invalid and @c error is provided, it will be populated with error
 * information.
 *
 * @param object The object to be placed at the end of the @c keyPath .
 * @param keyPath The path of keys/indices to follow through the object graph.
 * @param error An optional pointer that will receive error information if an error occurs.
 * @return @c YES if the @c YailDictionary was updated successfully, otherwise @c NO is returned
 *         and @c error will contain more information if provided.
 */
- (BOOL)setObject:(nonnull id)object
       forKeyPath:(nonnull NSArray<id> *)keyPath
            error:(NSError **)error;

/// --------------------
/// YailList equivalence
/// --------------------

/**
 * Converts the @c YailDictionary into the equvialent associative list.
 *
 * @return a new @c YailList with pairs (as @c YailList) for each (key, value) entry from the dictionary
 */
- (nonnull YailList *)dictToAlist;

@end

/**
 * The @c YailDictionary extension to @c NSDictionary provides conversion support to convert a
 * @c NSDictionary into a @c YailDictionary.
 */
@interface NSDictionary (YailDictionary)

/**
 * Converts the receiver of the method invocation into a @c YailDictionary. The object graph rooted
 * at the receiver must be an acyclic object graph.
 *
 * @param interpreter The @c SCMInterpreter that will be used for encoding objects into Scheme.
 * @return A new @c YailDictionary containing a Scheme-compatible version of the key-value pairs
 *         in the receiver.
 */
- (nonnull YailDictionary *)yailDictionaryUsingInterpreter:(SCMInterpreter *)interpreter;

@end

/**
 * The @c YailDictionary extension to @c YailList provides conversion support to convert a
 * @c YailList into a @c YailDictionary.
 */
@interface YailList (YailDictionary)

/**
 * Converts the receiver into a @c YailDictionary.
 *
 * The receiver must be a well-formed associative list. If it is not, then @c nil will be returned
 * and if @c error is provided, it will be populated with error information.
 *
 * @param error An optional address to a pointer where error information will be provided if an
 *              error occurs.
 * @return a new @c YailDictionary representing the contents of the associative list, or @c nil if
 *         an error occurred.
 */
- (nullable YailDictionary *)alistToDictWithError:(NSError ** _Nullable)error;

/**
 * Checks whether the YailList represents a associative list.
 *
 * @return @c YES if the list is a valid associative list, otherwise @c NO.
 */
- (BOOL)isAlist;

@end

NS_ASSUME_NONNULL_END
