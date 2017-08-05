//
//  SCMSymbol.h
//  SchemeKit
//
//  Created by Evan Patton on 10/29/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SchemeKit/picrin.h>

@interface SCMSymbol : NSObject

+ (instancetype)symbol:(pic_value)symbol forState:(pic_state *)pic;
- (instancetype)initWithSymbol:(pic_value)symbol forState:(pic_state *)pic;

@end
