//
//  NSString_SHA1.h
//  AICompanionApp
//
//  Created by Evan Patton on 10/25/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (SHA1)

- (NSString * _Nonnull)sha1;

@property (readonly, nonatomic) NSString * _Nonnull sha1;

@end
