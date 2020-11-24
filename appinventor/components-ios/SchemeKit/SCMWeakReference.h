// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

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
