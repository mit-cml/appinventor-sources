//
//  YailList.h
//  SchemeKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#include "picrin.h"

@interface YailList : NSMutableArray

- (pic_value)picrinList;

@end
