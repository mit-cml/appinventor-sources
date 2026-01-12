// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>
#import "SCMObject.h"

NS_ASSUME_NONNULL_BEGIN

@protocol NeedsWeakReference
@end

@interface SCMWeakReference<ObjectType> : NSObject<SCMObject>

+ (instancetype)referenceForObject:(_Nonnull ObjectType)object inInterpreter:(SCMInterpreter *)interpreter;
- (instancetype)initWithObject:(_Nonnull ObjectType)object inInterpreter:(SCMInterpreter *)interpreter;
- (void)clear;

@property (readonly, nonatomic) _Nullable ObjectType object;
@property (readonly) BOOL cleared;

@end

NS_ASSUME_NONNULL_END
