//
//  SCMInterpreter.h
//  SchemeKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SCMInterpreter : NSObject

- (NSString *)evalForm:(NSString *)form;
- (void)clearException;
- (void)setCurrentForm:(id)form;

@property (readonly) NSException *exception;

@end
