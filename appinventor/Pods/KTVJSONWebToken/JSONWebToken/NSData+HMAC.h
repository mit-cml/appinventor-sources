//
//  NSData+HMAC.h
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 18/11/15.
//  Copyright © 2015 Antoine Palazzolo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSData (HMAC)
- (NSData * _Nonnull)jwt_hmacSignatureWithSHAHashFuctionSize:(NSInteger)functionSize secret:(NSData * _Nonnull)secret;
@end
