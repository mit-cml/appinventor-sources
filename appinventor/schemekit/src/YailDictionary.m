// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import "YailDictionary.h"

#import "SCMErrors.h"
#import "SCMSymbol.h"
#import "YailList.h"
#import "SCMInterpreter-Private.h"
#import "SCMWeakReference.h"


static NSString *kSizeKey = @"size";


/**
 * The @c AllConstant class instance is used in walking operations to indicate
 * steps where every item should be visited rather than taking a single path.
 */
@interface AllConstant : NSObject<NSCopying, NSMutableCopying>

@end

@implementation AllConstant

- (NSString *)description {
  return @"ALL_ITEMS";
}

- (NSString *)debugDescription {
  return @"ALL_ITEMS";
}

- (BOOL)isEqual:(id)object {
  return object == self;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

- (id)mutableCopyWithZone:(NSZone *)zone {
  return self;
}

@end

/**
 * The singleton instance of @c AllConstant.
 */
static AllConstant *_ALL = nil;

/**
 * Gets the item from the @c target list indicated by @c currentKey.
 *
 * Because App Inventor allows for duck typing of strings as numeric values, we have to consider
 * the many types that @c currentKey may take.
 *
 * If an error occurs and @c error is provided, it will contain an error with the domain
 * @c SCMErrorDomain and one of the following error codes:
 *
 * - @c SCMERrorIndexOutOfBounds
 * - @c SCMErrorInvalidIndex if @c currentKey was not valid, for example if the key is a string
 *
 * @param target the array from which to retrieve the value
 * @param currentKey the current key being evaluated
 * @param step the current step in the path
 * @param error an optional pointer to a location to place an NSError with more error information
 * @return an object if @c currentKey is valid in @c target. Otherwise, @c nil is returned and
 *     if @c error is provided, it will contain more information about the error.
 */
static id getFromList(NSArray *target, id currentKey, NSUInteger step, NSError **error) {
  NSUInteger index = NSUIntegerMax;
  if ([currentKey isKindOfClass:[NSString class]]) {
    index = (NSUInteger) [(NSString *)currentKey integerValue];
  } else if ([currentKey isKindOfClass:[NSNumber class]]) {
    index = [(NSNumber *)currentKey unsignedIntegerValue];
  } else if ([currentKey isKindOfClass:[SCMValue class]]) {
    SCMValue *temp = (SCMValue *)currentKey;
    if ([temp isNumber] && pic_int_p(nil, temp.value)) {
      index = pic_int(nil, temp.value);
    }
  }
  if (index != NSUIntegerMax) {
    if (index <= target.count) {
      return target[index];
    } else if (error) {
      *error = [NSError errorWithDomain:SCMErrorDomain
                                   code:SCMErrorIndexOutOfBounds
                               userInfo:@{kSCMBadIndex: [NSNumber numberWithUnsignedInteger:index],
                                          kSCMBadValue: target}];
      return nil;
    }
  }
  if (error) {
    *error = [NSError errorWithDomain:SCMErrorDomain
                                 code:SCMErrorInvalidIndex
                             userInfo:@{kSCMBadIndex: [NSNumber numberWithUnsignedInteger:step],
                                        kSCMBadValue: currentKey}];
  }
  return nil;
}

NSMutableArray<id> *walkKeyPath(id root, NSArray<id> *keysOrIndices, NSMutableArray<id> *result) {
  if (keysOrIndices.empty) {
    if (root != nil) {
      [result addObject:root];
    }
    return result;
  } else if (root == nil) {
    return result;
  }

  NSMutableArray *currentNodes = nil;
  NSMutableArray *nodesToVisit = [NSMutableArray arrayWithObject:root];
  for (id currentKey in keysOrIndices) {
    currentNodes = nodesToVisit;
    nodesToVisit = [[NSMutableArray alloc] init];
    for (id node in currentNodes) {
      if (currentKey == _ALL) {
        if ([node isKindOfClass:[NSArray class]]) {
          [nodesToVisit addObjectsFromArray:(NSArray *)node];
        } else if ([node isKindOfClass:[NSDictionary class]]) {
          [nodesToVisit addObjectsFromArray:[(NSDictionary *) node allValues]];
        } else {
          // pass
        }
      } else if ([currentKey isKindOfClass:[NSString class]]) {
        if ([node isKindOfClass:[NSArray class]]) {
          [nodesToVisit addObject:[(NSArray *)node objectAtIndex:[(NSString *) currentKey integerValue]]];
        } else if ([node isKindOfClass:[NSDictionary class]]) {
          [nodesToVisit addObject:[(NSDictionary *)node objectForKey:currentKey]];
        } else {
          // pass
        }
      } else if ([currentKey isKindOfClass:[NSNumber class]]) {
        if ([node isKindOfClass:[NSArray class]]) {
          [nodesToVisit addObject:[(NSArray *)node objectAtIndex:[(NSNumber *) currentKey unsignedIntegerValue]]];
        } else if ([currentKey isKindOfClass:[NSDictionary class]]) {
          [nodesToVisit addObject:[(NSDictionary *)node objectForKey:currentKey]];
        } else {
          // pass
        }
      } else if ([currentKey conformsToProtocol:@protocol(SCMValue)]) {
        if ([(SCMValue *) currentKey isString]) {
        } else if ([(SCMValue *) currentKey isNumber]) {
        }
      }
    }
  }
  [result addObjectsFromArray:nodesToVisit];
  return result;
}

@interface YailDictionary () {
  SCMInterpreter *_interpreter;
  NSMutableDictionary<id, id> *_backend;
  NSMutableArray *_keys;
  NSUInteger _mutations;
  pic_value _value;
  NSMutableArray *_retainArray;
}

@end

@implementation YailDictionary

+ (nonnull instancetype)emptyDictionaryIn:(SCMInterpreter *)interpreter {
  return [[self alloc] initWithInterpreter:interpreter];
}

+ (nonnull instancetype)dictionaryInInterpreter:(SCMInterpreter *)interpreter
                                    withObjects:(NSArray<id> *)objects
                                        forKeys:(NSArray<id<NSCopying>> *)keys {
  return [[self alloc] initWithInterpreter:interpreter withObjects:objects forKeys:keys];
}

+ (nonnull instancetype)dictionaryFromPairs:(pic_value)pairs {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:SCMInterpreter.shared];
  pic_state *pic = dict->_interpreter.state;
  while (!pic_nil_p(pic, pairs)) {
    @try {
      pic_value pair = pic_cdr(pic, pic_car(pic, pairs));
      pic_value key = pic_car(pic, pair);
      pic_value value = pic_cadr(pic, pair);
      id<SCMValue> objcKey = [dict->_interpreter unwrapValue:key];
      id<SCMValue> objcValue = [dict->_interpreter unwrapValue:value];
      if (objcKey && objcValue) {
        dict[objcKey] = objcValue;
      }
    } @catch (NSException *exception) {
      NSLog(@"Exception when creating dictionary: %@", exception);
    }
    pairs = pic_cdr(pic, pairs);
  }
  return dict;
}

- (nonnull instancetype)initWithInterpreter:(nonnull SCMInterpreter *)interpreter
                                withObjects:(NSArray<id> *)objects
                                    forKeys:(NSArray<id<NSCopying>> *)keys {
  if (self = [super init]) {
    _interpreter = interpreter;
    _backend = [NSMutableDictionary dictionaryWithObjects:objects forKeys:keys];
    _keys = [keys mutableCopy];
    _mutations = 0;
    _value = [interpreter internObject:self];
    assert(pic_type(interpreter.state, _value) == YAIL_TYPE_DICT);
    _retainArray = nil;
  }
  return self;
}

- (nonnull instancetype)initWithInterpreter:(nonnull SCMInterpreter *)interpreter {
  if (self = [super init]) {
    _interpreter = interpreter;
    _backend = [[NSMutableDictionary alloc] init];
    _keys = [[NSMutableArray alloc] init];
    _value = [interpreter internObject:self];
    assert(pic_type(interpreter.state, _value) == YAIL_TYPE_DICT);
    _mutations = 0;
    _retainArray = nil;
  }
  return self;
}

- (nullable instancetype)initWithCoder:(NSCoder *)coder {
  if (self = [self initWithInterpreter:SCMInterpreter.shared]) {
    NSInteger itemsToDecode = [coder decodeIntegerForKey:kSizeKey];
    for (NSInteger i = 0; i < itemsToDecode; i++) {
      id key = [coder decodeObject];
      id value = [coder decodeObject];
      [_keys addObject:key];
      _backend[key] = value;
    }
  }
  return self;
}

- (nonnull instancetype)init {
  if (self = [super init]) {
    _interpreter = SCMInterpreter.shared;
    _backend = [NSMutableDictionary dictionary];
    _keys = [NSMutableArray array];
    _mutations = 0;
    _value = [_interpreter internObject:self];
    assert(pic_type(_interpreter.state, _value) == YAIL_TYPE_DICT);
    _retainArray = nil;
  }
  return self;
}

+ (nonnull id)ALL {
  if (!_ALL) {
    _ALL = [[AllConstant alloc] init];
  }
  return _ALL;
}

- (nonnull YailList *)dictToAlist {
  NSMutableArray *copy = [[NSMutableArray alloc] initWithCapacity:self.count];
  for (id key in self) {
    [copy addObject:[YailList listInInterpreter:_interpreter ofValues:key, _backend[key], nil]];
  }
  return [copy yailListUsingInterpreter:_interpreter];
}

- (nullable id)getObjectAtKeyPath:(NSArray<id> *)path error:(NSError ** _Nullable)error {
  id target = self;
  NSUInteger step = 1;
  for (id currentKey in path) {
    if ([target isKindOfClass:[YailDictionary class]]) {
      target = [(YailDictionary *)target objectForKey:currentKey];
    } else if ([target isKindOfClass:[NSDictionary class]]) {
      target = [(NSDictionary *)target objectForKey:currentKey];
    } else if ([target isKindOfClass:[YailList class]] && [target isAlist]) {
      target = [[(YailList *)target alistToDictWithError:nil] objectForKey:currentKey];
    } else if ([target isKindOfClass:[NSArray class]]) {
      target = getFromList(target, currentKey, step, error);
    } else {
      return nil;
    }
    step++;
  }
  return target;
}

- (nullable NSArray<id> *)walkKeyPath:(NSArray<id> *)keysOrIndices
                                error:(NSError * * _Nullable)error {
  return walkKeyPath(self, keysOrIndices, [[NSMutableArray alloc] init]);
}

- (BOOL)setObject:(nonnull id)object
       forKeyPath:(nonnull NSArray<id> *)keyPath
            error:(NSError **)error {
  /*
   Object target = this;
   Iterator<?> it = keys.iterator();

   // Updating with an empty key path is a no-op as there isn't a path to update.
   if (keys.isEmpty()) {
     return;
   }

   while (it.hasNext()) {
     Object key = it.next();
     if (it.hasNext()) {
       // More keys to go
       target = lookupTargetForKey(target, key);
     } else {
       if (target instanceof YailDictionary) {
         ((YailDictionary) target).put(key, value);
       } else if (target instanceof YailList) {
         LList l = (LList) target;
         l.getIterator(keyToIndex((List<?>) target, key)).set(value);
       } else if (target instanceof List) {
         //noinspection unchecked
         ((List) target).set(keyToIndex((List<?>) target, key), value);
       } else {
         throw new DispatchableError(ErrorMessages.ERROR_INVALID_VALUE_IN_PATH);
       }
     }
   }
   */
  id last = self;
  id next = self;

  // Updating with an empty key path is a no-op as there isn't a path to update.
  if (keyPath.empty) {
    return NO;
  }

  id lastKey = nil;
  for (id key in keyPath) {
    lastKey = key;
    last = next;
    /**
     if (target instanceof YailDictionary) {
       return ((YailDictionary) target).get(key);
     } else if (target instanceof List) {
       return ((List<?>) target).get(keyToIndex((List<?>) target, key));
     } else {
       throw new DispatchableError(ErrorMessages.ERROR_INVALID_VALUE_IN_PATH,
           target == null ? "null" : target.getClass().getSimpleName());
     }
     */
    if ([key isKindOfClass:[SCMSymbol class]]) {
      continue;
    }
    if ([last isKindOfClass:[NSDictionary class]]) {
      next = last[lastKey];
    } else if ([last isKindOfClass:[NSArray class]]) {
      if ([key isKindOfClass:[NSNumber class]]) {
        next = [(NSArray *)last objectAtIndex:[(NSNumber *)key unsignedIntegerValue]];
      } else if ([key isKindOfClass:[NSString class]]) {
        next = [(NSArray *)last objectAtIndex:[(NSString *)key integerValue]];
      }
    } else {
      return NO;
    }
  }
  last[lastKey] = object;
  return YES;
}

- (BOOL)containsObject:(id)object {
  NSEnumerator<id> *it = [_backend objectEnumerator];
  id value;
  while ((value = [it nextObject])) {
    if ([value isEqual:object]) {
      return YES;
    }
  }
  return NO;
}

- (BOOL)containsKey:(id)key {
  return _backend[key] != nil;
}

- (nullable id)objectAtIndex:(NSUInteger)index {
  if ([_keys count] >= index) {
    return nil;
  }
  return _backend[_keys[index]];
}

- (BOOL)isEmpty {
  return _keys.empty;
}

- (NSUInteger)hash {
  return (NSUInteger) self;
}

- (BOOL)isEqual:(id)object {
  if ([object isKindOfClass:[YailDictionary class]]) {
    return [self isDictionaryEqual:(YailDictionary *)object];
  }
  return NO;
}

- (BOOL)isDictionaryEqual:(YailDictionary *)other {
  if (self.count != other.count) {
    return false;
  }
  for (id key in _keys) {
    id ourValue = self[key];
    id otherValue = other[key];
    if ([ourValue isKindOfClass:[YailDictionary class]]) {
      if ([otherValue isKindOfClass:[YailDictionary class]]) {
        if (![(YailDictionary *)ourValue isDictionaryEqual:otherValue]) {
          return false;
        }
      } else {
        return false;
      }
    } else if ([otherValue isKindOfClass:[YailDictionary class]]) {
      return false;
    } else if (![ourValue isEqual:otherValue]) {
      return false;
    }
  }
  return true;
}

- (NSArray<NSObject *> *)allKeys {
  return [_keys copy];
}

- (NSArray<NSObject *> *)allValues {
  NSMutableArray<NSObject *> *result = [NSMutableArray arrayWithCapacity:[_keys count]];
  for (id key in _keys) {
    [result addObject:self[key]];
  }
  return result;
}

@synthesize interpreter = _interpreter;
@synthesize value = _value;

/// MARK: NSDictionary required methods

- (instancetype)initWithObjects:(id  _Nonnull const [_Nullable])objects forKeys:(id<NSCopying>  _Nonnull const [_Nullable])keys count:(NSUInteger)count {
  if (self = [super init]) {
    _backend = [[NSMutableDictionary alloc] initWithObjects:objects forKeys:keys count:count];
    _keys = [[NSMutableArray alloc] initWithObjects:keys count:count];
    _interpreter = SCMInterpreter.shared;
    _value = [_interpreter internObject:self];
    assert(pic_type(_interpreter.state, _value) == YAIL_TYPE_DICT);
  }
  return self;
}

- (instancetype)initWithDictionary:(NSDictionary *)otherDictionary {
  if (self = [super init]) {
    _backend = [[NSMutableDictionary alloc] initWithDictionary:otherDictionary];
    _keys = [[otherDictionary allKeys] mutableCopy];
    _interpreter = SCMInterpreter.shared;
    _value = [_interpreter internObject:self];
    assert(pic_type(_interpreter.state, _value) == YAIL_TYPE_DICT);
}
  return self;
}

- (NSUInteger)count {
  return [_backend count];
}

- (id)objectForKey:(id)aKey {
  id result = [_backend objectForKey:aKey];
  if (result != nil && [result isKindOfClass:[SCMWeakReference class]]) {
    return ((SCMWeakReference *) result).object;
  }
  return result;
}

- (id)objectForKeyedSubscript:(id)aKey {
  id result = [_backend objectForKeyedSubscript:aKey];
  if (result != nil && [result isKindOfClass:[SCMWeakReference class]]) {
    return ((SCMWeakReference *) result).object;
  }
  return result;
}

- (NSEnumerator<id> *)keyEnumerator {
  return [_keys objectEnumerator];
}

- (void)putAll:(YailDictionary *)other {
  for (id key in other->_keys) {
    self[key] = other[key];
  }
}

/// MARK: NSMutableDictionary required methods

- (void)setObject:(id)anObject forKey:(id<NSCopying>)aKey {
  if ([[anObject class] conformsToProtocol:@protocol(NeedsWeakReference)]) {
    anObject = [SCMWeakReference referenceForObject:anObject inInterpreter:_interpreter];
  }
  if (![_backend objectForKey:aKey]) {
    [_keys addObject:aKey];
  }
  [_backend setObject:anObject forKey:aKey];
}

- (void)removeObjectForKey:(id)aKey {
  [_keys removeObject:aKey];
  [_backend removeObjectForKey:aKey];
}

- (void)removeAllObjects {
  [_keys removeAllObjects];
  [_backend removeAllObjects];
}

/// MARK: SCMValue implementation

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
  return NO;
}

- (BOOL)isDictionary {
  return YES;
}

- (BOOL)isComponent {
  return NO;
}

- (BOOL)isNil {
  return NO;
}

- (BOOL)isCons {
  return NO;
}

- (BOOL)isSymbol {
  return NO;
}

- (BOOL)isExact {
  return NO;
}

- (BOOL)isPicEqual:(pic_value)other {
  if (other == _value) {
    return YES;
  }
  // TODO: Implement deep equals for dictionaries
  return NO;
}

- (void)mark {
#ifdef MEMDEBUG
  NSLog(@"YailDictionary.mark");
#endif
  assert(pic_type(_interpreter.state, _value) == YAIL_TYPE_DICT);
  [_interpreter mark:_value];
  for (id key in _keys) {
    if ([key respondsToSelector:@selector(mark)]) {
      [key mark];
    }
    id value = _backend[key];
    if ([value respondsToSelector:@selector(mark)]) {
      [value mark];
    }
  }
}


/// MARK: NSCopying implementation

- (nonnull id)copyWithZone:(nullable NSZone *)zone {
  return [self mutableCopyWithZone:zone];
}

- (nonnull id)mutableCopyWithZone:(nullable NSZone *)zone {
  YailDictionary *copy = [[YailDictionary alloc] init];
  copy->_interpreter = _interpreter;
  copy->_backend = [_backend mutableCopyWithZone:zone];
  copy->_keys = [_keys mutableCopyWithZone:zone];
  copy->_value = [_interpreter internObject:copy];
  assert(pic_type(_interpreter.state, _value) == YAIL_TYPE_DICT);
  return copy;
}

/// MARK: NSCoding implementation

- (void)encodeWithCoder:(nonnull NSCoder *)coder {
  [coder encodeInteger:self.count forKey:kSizeKey];
  for (id key in _keys) {
    [coder encodeObject:key];
    [coder encodeByrefObject:_backend[key]];
  }
}

/// MARK: NSFastEnumeration implementation

- (NSUInteger)countByEnumeratingWithState:(nonnull NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id  _Nullable * _Nonnull)buffer
                                    count:(NSUInteger)len {
  NSUInteger start = 0;
  if (state->state == 0) {
    _retainArray = [[NSMutableArray alloc] init];
  } else {
    start = state->state;
    [_retainArray removeAllObjects];
  }
  NSUInteger i = 0;
  while (i < len && (start + i) < _keys.count) {
    id key = _keys[start + i];
    buffer[i++] = key;
  }
  state->state = start + i;
  state->itemsPtr = buffer;
  state->mutationsPtr = &self->_mutations;
  if (i == 0) {
    _retainArray = nil;
  }
  return i;
}

/// MARK: - Debugging Information

- (NSString *)debugDescription {
  NSMutableString *result = [NSMutableString stringWithString:@"{"];
  NSString *sep = @"\n";
  BOOL needsNL = NO;
  for (id key in _keys) {
    [result appendFormat:@"%@    %@ = %@", sep, [key debugDescription], [_backend[key] debugDescription]];
    sep = @",\n";
    needsNL = YES;
  }
  [result appendString:(needsNL ? @"\n}" : @"}")];
  return [result copy];
}

- (NSString *)description {
  return [self debugDescription];
}

#ifdef MEMDEBUG
- (void)dealloc {
  NSLog(@"Deallocating YailDictionary");
}
#endif

@end

@implementation NSDictionary (YailDictionary)

- (nonnull YailDictionary *)yailDictionaryUsingInterpreter:(SCMInterpreter *)interpreter {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:interpreter];
  for (id key in self) {
    id value = self[key];
    if ([value conformsToProtocol:@protocol(SCMValue)]) {
      dict[key] = value;
    } else if ([value isKindOfClass:[NSArray class]]) {
      dict[key] = [(NSArray *)value yailListUsingInterpreter:interpreter];
    } else if ([value isKindOfClass:[NSDictionary class]]) {
      dict[key] = [(NSDictionary *)value yailDictionaryUsingInterpreter:interpreter];
    } else {
      dict[key] = value;
    }
  }
  return dict;
}

@end

@implementation YailList (YailDictionary)

- (nullable YailDictionary *)alistToDictWithError:(NSError *__autoreleasing  _Nullable *)error {
  YailDictionary *result = [YailDictionary emptyDictionaryIn:self.interpreter];
  NSUInteger index = 1;
  for (id element in self) {
    if ([element isKindOfClass:[YailList class]]) {
      YailList *list = (YailList *) element;
      if (list.length != 2) {
        if (error) {
          *error = [NSError errorWithDomain:SCMErrorDomain
                                       code:SCMErrorMalformedAlist
                                   userInfo:@{
                                     kSCMBadIndex: [NSNumber numberWithUnsignedInteger:index]
                                   }];
        }
        return nil;
      }
      [result setObject:list[2] forKey:list[1]];
    } else {
      if (error) {
        *error = [NSError errorWithDomain:SCMErrorDomain
                                     code:SCMErrorMalformedAlist
                                 userInfo:@{
                                   kSCMBadIndex: [NSNumber numberWithUnsignedInteger:index]
                                 }];
      }
      return nil;
    }
    index++;
  }
  return result;
}

- (BOOL)isAlist {
  for (id element in self) {
    if ([element isKindOfClass:[YailList class]]) {
      if (((YailList *) element).length != 2) {
        return NO;
      }
    } else {
      return NO;
    }
  }
  return YES;
}

@end
