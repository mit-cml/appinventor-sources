//
//  SCMNameResolver.m
//  SchemeKit
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import "SCMNameResolver.h"
#import <objc/runtime.h>

@implementation SCMNameResolver

+ (Class)classFromQualifiedName:(const char *)name {
  return NSClassFromString([NSString stringWithUTF8String:name]);
}

+ (NSDictionary *)getMethodsForClass:(Class)clazz {
  NSMutableDictionary *result = [NSMutableDictionary dictionary];
  unsigned int count = 0;
  Method *methods = class_copyMethodList(clazz, &count);
  
  return [result copy];
}

+ (SEL)selectorForClass:(Class)clazz withName:(const char *)name argumentTypeList:(NSArray *)args {
  return nil;
}

@end
