/* Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This is a simple class to create or parse a MIME document.

// To create a MIME document, allocate a new GTMMIMEDocument and start adding parts.
// When you are done adding parts, call generateInputStream or generateDispatchData.
//
// A good reference for MIME is http://en.wikipedia.org/wiki/MIME

#import <Foundation/Foundation.h>

// GTMMIMEDocumentPart represents a part of a MIME document.
//
// +[GTMMIMEDocument MIMEPartsWithBoundary:data:] returns an array of these.
@interface GTMMIMEDocumentPart : NSObject

@property(nonatomic, readonly, nullable) NSDictionary<NSString *, NSString *> *headers;
@property(nonatomic, readonly, nonnull) NSData *headerData;
@property(nonatomic, readonly, nonnull) NSData *body;
@property(nonatomic, readonly) NSUInteger length;

+ (nonnull instancetype)partWithHeaders:(nullable NSDictionary *)headers
                                   body:(nonnull NSData *)body;

@end

@interface GTMMIMEDocument : NSObject

// Get or set the unique boundary for the parts that have been added.
//
// When creating a MIME document from parts, this is typically calculated
// automatically after all parts have been added.
@property(nonatomic, copy, null_resettable) NSString *boundary;

#pragma mark - Methods for Creating a MIME Document

+ (nonnull instancetype)MIMEDocument;

// Adds a new part to this mime document with the given headers and body.
// The headers keys and values should be NSStrings.
// Adding a part may cause the boundary string to change.
- (void)addPartWithHeaders:(nonnull NSDictionary<NSString *, NSString *> *)headers
                      body:(nonnull NSData *)body;

// An inputstream that can be used to efficiently read the contents of the MIME document.
//
// Any parameter may be null if the result is not wanted.
- (void)generateInputStream:(NSInputStream *_Nullable *_Nullable)outStream
                     length:(unsigned long long *_Nullable)outLength
                   boundary:(NSString *_Nullable *_Nullable)outBoundary;

// A dispatch_data_t with the contents of the MIME document.
//
// Note: dispatch_data_t is one-way toll-free bridged so the result
// may be cast directly to NSData *.
//
// Any parameter may be null if the result is not wanted.
- (void)generateDispatchData:(dispatch_data_t _Nullable *_Nullable)outDispatchData
                      length:(unsigned long long *_Nullable)outLength
                    boundary:(NSString *_Nullable *_Nullable)outBoundary;

// Utility method for making a header section, including trailing newlines.
+ (nonnull NSData *)dataWithHeaders:(nullable NSDictionary<NSString *, NSString *> *)headers;

#pragma mark - Methods for Parsing a MIME Document

// Method for parsing out an array of MIME parts from a MIME document.
//
// Returns an array of GTMMIMEDocumentParts.  Returns nil if no part can
// be found.
//
// NOTE: if MIME parts in the data are malformed, the resulting array may
// still contain GTMMIMEDocumentParts in the position where the malformed
// parts appeared; these parts will have an empty NSData body and nil
// headers.
+ (nullable NSArray<GTMMIMEDocumentPart *> *)MIMEPartsWithBoundary:(nonnull NSString *)boundary
                                                              data:(nonnull NSData *)
                                                                       fullDocumentData;

// Utility method for efficiently searching possibly discontiguous NSData
// for occurrences of target byte. This method does not "flatten" an NSData
// that is composed of discontiguous blocks.
//
// The byte offsets of non-overlapping occurrences of the target are returned as
// NSNumbers in the array.
+ (void)searchData:(nonnull NSData *)data
       targetBytes:(const void *_Nonnull)targetBytes
      targetLength:(NSUInteger)targetLength
      foundOffsets:(NSArray<NSNumber *> *_Nullable *_Nonnull)outFoundOffsets;

// Utility method to parse header bytes into an NSDictionary.
+ (nullable NSDictionary<NSString *, NSString *> *)headersWithData:(nonnull NSData *)data;

// ------ UNIT TESTING ONLY BELOW ------

// Internal methods, exposed for unit testing only.
- (void)seedRandomWith:(u_int32_t)seed;

+ (NSUInteger)findBytesWithNeedle:(const unsigned char *_Nonnull)needle
                     needleLength:(NSUInteger)needleLength
                         haystack:(const unsigned char *_Nonnull)haystack
                   haystackLength:(NSUInteger)haystackLength
                      foundOffset:(NSUInteger *_Nonnull)foundOffset;

+ (void)searchData:(nonnull NSData *)data
          targetBytes:(const void *_Nonnull)targetBytes
         targetLength:(NSUInteger)targetLength
         foundOffsets:(NSArray<NSNumber *> *_Nullable *_Nonnull)outFoundOffsets
    foundBlockNumbers:(NSArray<NSNumber *> *_Nullable *_Nonnull)outFoundBlockNumbers;

@end
