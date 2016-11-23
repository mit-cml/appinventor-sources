//
//  YailList.h
//  SchemeKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#include <SchemeKit/picrin.h>

@interface YailList : NSMutableArray

+ (instancetype)makeList:(pic_value)list :(pic_state *)state;
- (instancetype)init:(pic_value)list :(pic_state *)state;

@property (readonly) pic_value picrinList;

@end
