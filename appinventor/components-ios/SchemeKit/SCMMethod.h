//
//  SCMMethod.h
//  SchemeKit
//
//  Created by Evan Patton on 10/9/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <objc/runtime.h>

@interface SCMMethod : NSObject

- (instancetype)initWithMethod:(Method)method forClass:(Class)clazz isStatic:(BOOL)isStatic;
- (NSInvocation *)unboundInvocation;
- (NSInvocation *)invocationForInstance:(id)target;
- (NSInvocation *)staticInvocation;

@property (nonatomic, readonly) NSString *yailName;

@end
