// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "YailList.h"

#include "picrin.h"
#include "yail.h"

#import "SCMTypes.h"
#import "SCMValue-Private.h"
#import "SCMInterpreter-Private.h"

@interface YailList () {
 @private
  SCMInterpreter *_interpreter;
  pic_value _head;
  pic_value _tail;
  NSUInteger _mutationGuard;
  NSMutableArray *_retainBuffer;
}

@end

/**
 * Finds and returns the cons prior to @c before in the list starting at @c start.
 *
 * @param pic The picrin session that contains the objects (used for exception handling).
 * @param start The starting cons in a list.
 * @param before The target cons in a list.
 * @return The cons prior to @c before or @c pic_nil if @c before is not reachable from @c start.
 */
static pic_value cons_before(pic_state *pic, pic_value start, pic_value before) {
  pic_value temp = start;
  pic_value look = pic_cdr(pic, start);
  while (!pic_nil_p(pic, look) && look != before) {
    temp = look;
    look = pic_cdr(pic, temp);
  }
  return pic_nil_p(pic, look) ? look : temp;
}

/**
 * Returns the cons at the given @c index from @c start.
 *
 * The @c car of the cons will be the @e index-th element in the list.
 *
 * @param pic The picrin session that contains the objects (used for exception handling).
 * @param start The starting cons in a list.
 * @param index The desired index of the element in the list.
 * @return The cons @c index steps from @c start or @c pic_nil if @c index is greater than the
 *         length of the list.
 */
static pic_value cons_at(pic_state *pic, pic_value start, NSUInteger index) {
  pic_value temp = start;
  pic_value temp_cdr = pic_cdr(pic, start);
  while (index > 0 && !pic_nil_p(pic, temp_cdr)) {
    temp = temp_cdr;
    temp_cdr = pic_cdr(pic, temp_cdr);
    index--;
  }
  return temp;
}

@implementation YailList

@synthesize value = _head;
@synthesize interpreter = _interpreter;

+ (nonnull instancetype)emptyListIn:(nonnull SCMInterpreter *)interpreter {
  pic_value value = [interpreter makeConsWithCar:[interpreter makeSymbol:@"*list*"] cdr:nil];
  return [[self alloc] initWithList:value fromInterpreter:interpreter];
}

+ (nonnull instancetype)wrapList:(pic_value)list fromInterpreter:(SCMInterpreter * _Nonnull)interpreter {
  return [[self alloc] initWithList:list fromInterpreter:interpreter];
}

+ (nonnull instancetype)listInInterpreter:(nonnull SCMInterpreter *)interpreter ofValues:(nonnull id)value, ... NS_REQUIRES_NIL_TERMINATION {
  va_list args;
  id item;
  va_start(args, value);
  pic_value list = [interpreter makeConsWithCar:[interpreter makeSymbol:@"*list*"] cdr:nil];
  pic_value insertion = list;
  if (value != nil) {
    pic_set_cdr(interpreter.state, insertion, [interpreter makeConsWithCar:[interpreter valueForObject:value] cdr:nil]);
    insertion = pic_cdr(interpreter.state, insertion);
    while ((item = va_arg(args, id)) != nil) {
      pic_set_cdr(interpreter.state, insertion, [interpreter makeConsWithCar:[interpreter valueForObject:item] cdr:nil]);
      insertion = pic_cdr(interpreter.state, insertion);
    }
  }
  va_end(args);
  return [[self alloc] initWithList:list fromInterpreter:interpreter];
}

- (nonnull instancetype)initWithArray:(nonnull NSArray *)array
                        inInterpreter:(nonnull SCMInterpreter *)interpreter {
  if (self = [super init]) {
    self->_interpreter = interpreter;
    self->_mutationGuard = 0;
    self->_retainBuffer = nil;
    pic_value insert = [interpreter makeConsWithCar:[interpreter makeSymbol:@"*list*"] cdr:nil];
    self->_head = insert;
    self->_tail = insert;
    for (id object in array) {
      pic_value item = [interpreter wrapObject:[interpreter valueForObject:object]];
      insert = pic_cons(interpreter.state, item, pic_nil_value(interpreter.state));
      pic_set_cdr(interpreter.state, _tail, insert);
      _tail = insert;
    }
  }
  return self;
}

- (nonnull instancetype)initWithList:(pic_value)list fromInterpreter:(SCMInterpreter *)interpreter {
  if (self = [super init]) {
    self->_head = list;
    self->_interpreter = interpreter;
    self->_mutationGuard = 0;
    self->_tail = _head;
    pic_value temp = pic_cdr(_interpreter.state, self->_head);
    while (!pic_nil_p(_interpreter.state, temp)) {
      self->_tail = temp;
      temp = pic_cdr(_interpreter.state, temp);
    }
    self->_retainBuffer = nil;
  }
  return self;
}

- (NSInteger)length {
  return self.count - 1;
}

- (BOOL)isEmpty {
  return pic_nil_p(nil, pic_cdr(nil, _head));
}

/* Note that we use the head as a unique has so that when we return YailLists across the
 * Scheme/Objective-C barrier we get the same object.
 */
- (NSUInteger)hash {
  return (NSUInteger)_head;
}

- (BOOL)isEqual:(id)object {
  if ([object isKindOfClass:[YailList class]]) {
    YailList *otherAsList = (YailList *) object;
    NSUInteger len = self.length;
    if (len != otherAsList.length) {
      return NO;
    }
    for (NSUInteger i = 1; i <= len; i++) {
      if (![self[i] isEqual:otherAsList[i]]) {
        return NO;
      }
    }
    return YES;
  }
  return NO;
}

/// MARK: NSMutableArray initializers

- (instancetype)init {
  SCMInterpreter *interpreter = SCMInterpreter.shared;
  pic_value head = [interpreter makeConsWithCar:[interpreter makeSymbol:@"*list*"] cdr:nil];
  return [self initWithList:head fromInterpreter:interpreter];
}

- (instancetype)initWithCapacity:(NSUInteger)numItems {
  return [self init];
}

- (instancetype)initWithCoder:(NSCoder *)coder {
  return [self init];
}

- (instancetype)initWithObjects:(id  _Nonnull const [])objects count:(NSUInteger)cnt {
  if (self = [self init]) {
    for (NSUInteger i = 0; i < cnt; i++) {
      pic_value insertion = [_interpreter makeConsWithCar:[_interpreter valueForObject:objects[i]] cdr:nil];
      pic_set_cdr(_interpreter.state, _tail, insertion);
      _tail = insertion;
    }
  }
  return self;
}

/// MARK: NSMutableArray required implementation

- (void)addObject:(id)anObject {
  pic_value new_tail = [_interpreter makeConsWithCar:[_interpreter valueForObject:anObject] cdr:[SCMValue nilValue]];
  pic_set_cdr(_interpreter.state, _tail, new_tail);
  _tail = new_tail;
  _mutationGuard++;
}

- (void)insertObject:(id)anObject atIndex:(NSUInteger)index {
  pic_value before = cons_at(_interpreter.state, _head, index - 1);
  pic_value insertion = [_interpreter makeConsWithCar:[_interpreter valueForObject:anObject] cdr:nil];
  pic_set_cdr(_interpreter.state, insertion, pic_cdr(_interpreter.state, before));
  pic_set_cdr(_interpreter.state, before, insertion);
  _mutationGuard++;
}

- (void)removeLastObject {
  if (_head != _tail) {
    pic_value temp = cons_before(_interpreter.state, _head, _tail);
    pic_set_cdr(_interpreter.state, temp, pic_nil_value(_interpreter.state));
    _tail = temp;
    _mutationGuard++;
  }
}

- (void)removeObjectAtIndex:(NSUInteger)index {
  if (index > 0) {
    pic_value before = cons_at(_interpreter.state, _head, index - 1);
    pic_value after = pic_cdr(_interpreter.state, pic_cdr(_interpreter.state, before));
    pic_set_cdr(_interpreter.state, before, after);
    _mutationGuard++;
  }
}

- (void)replaceObjectAtIndex:(NSUInteger)index withObject:(id)anObject {
  if (index > 0) {
    pic_value temp = cons_at(_interpreter.state, _head, index);
    pic_set_car(_interpreter.state, temp, [_interpreter valueForObject:anObject].value);
    _mutationGuard++;
  }
}

/// MARK: NSArray required implementation

- (NSUInteger)count {
  NSUInteger length = 0;
  pic_value ptr = self->_head;
  while (!pic_nil_p(_interpreter.state, ptr)) {
    length++;
    ptr = pic_cdr(_interpreter.state, ptr);
  }
  return length;
}

- (id)objectAtIndex:(NSUInteger)index {
  pic_value x = pic_list_ref(_interpreter.state, self->_head, (int) index);
  return [_interpreter unwrapValue:x];
}

/// MARK: NSFastEnumeration Implementation

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id _Nullable * _Nonnull)buffer
                                    count:(NSUInteger)len {
  pic_value it;
  if (state->state == 0) {
    it = pic_cdr(_interpreter.state, _head);
    _retainBuffer = [[NSMutableArray alloc] initWithCapacity:len];
  } else {
    it = (pic_value) state->state;
    [_retainBuffer removeAllObjects];
  }
  NSUInteger counter = 0;
  while (!pic_nil_p(_interpreter.state, it) && counter < len) {
    id item = [_interpreter unwrapValue:pic_car(_interpreter.state, it)];
    [_retainBuffer addObject:item];
    buffer[counter] = item;
    it = pic_cdr(_interpreter.state, it);
    counter++;
  }
  state->state = it;
  state->itemsPtr = buffer;
  state->mutationsPtr = &self->_mutationGuard;
  if (counter == 0 && _retainBuffer) {
    // Done iterating so we can clean up the retain buffer
    [_retainBuffer removeAllObjects];
    _retainBuffer = nil;
  }
  return counter;
}

/// MARK: NSCopying Implementation

- (instancetype)copyWithZone:(NSZone *)zone {
  // All instances of YailList are mutable
  return [self mutableCopyWithZone:zone];
}

- (instancetype)mutableCopyWithZone:(NSZone *)zone {
  pic_state *pic = _interpreter.state;
  YailList *copy = [[self class] emptyListIn:_interpreter];
  pic_value reader = pic_cdr(pic, _head);
  pic_value writer = copy->_head;
  while (!pic_nil_p(pic, reader)) {
    pic_value next = pic_cons(pic, pic_car(pic, reader), pic_nil_value(pic));
    pic_set_cdr(pic, writer, next);
    writer = next;
    reader = pic_cdr(pic, reader);
  }
  copy->_tail = writer;
  copy->_retainBuffer = nil;
  return copy;
}

/// MARK: SCMValue Implementation

- (BOOL)isBool {
  return NO;
}

- (BOOL)isNumber {
  return NO;
}

- (BOOL)isString {
  return NO;
}

- (BOOL)isList {
  return YES;
}

- (BOOL)isDictionary {
  return NO;
}

- (BOOL)isComponent {
  return NO;
}

- (BOOL)isNil {
  return pic_nil_p(nil, self->_head);
}

- (BOOL)isCons {
  return YES;
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (BOOL)isPicEqual:(pic_value)value {
  return NO;
}

- (void)mark {
  gc_mark(_interpreter.state, _head);
}

/// MARK: NSCoding Implementation

- (void)encodeWithCoder:(nonnull NSCoder *)coder {
  [super encodeWithCoder:coder];
}

@end

@implementation NSArray (YailList)

- (nonnull YailList *)yailListUsingInterpreter:(nonnull SCMInterpreter *)interpreter {
  if ([self isKindOfClass:[YailList class]]) {
    return (YailList *)self;  // Already a yail list
  } else {
    pic_value list = [interpreter makeConsWithCar:[interpreter makeSymbol:@"*list*"] cdr:nil];
    pic_value insertion = list;
    for (NSUInteger i = 0; i < self.count; i++) {
      pic_value next = [interpreter makeConsWithCar:[interpreter valueForObject:self[i]] cdr:nil];
      pic_set_cdr(interpreter.state, insertion, next);
      insertion = next;
    }
    return [YailList wrapList:list fromInterpreter:interpreter];
  }
}

- (BOOL)isEmpty {
  return self.count == 0;
}

@end
