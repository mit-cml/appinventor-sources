// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

@interface NSString (SHA1)

- (NSString * _Nonnull)sha1;

@property (readonly, nonatomic) NSString * _Nonnull sha1;

@end
