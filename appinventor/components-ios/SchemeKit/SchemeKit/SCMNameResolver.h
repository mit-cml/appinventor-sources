//
//  SCMNameResolver.h
//  SchemeKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SCMNameResolver : NSObject

+ (Class)classFromQualifiedName:(const char *)name;
+ (SEL)selectorForClass:(Class)clazz withName:(const char *)name argumentTypeList:(NSArray *)args;

@end
