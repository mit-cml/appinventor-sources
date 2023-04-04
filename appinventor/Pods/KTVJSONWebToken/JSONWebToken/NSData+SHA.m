//
//  NSData+SHA.m
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 19/11/15.
//  Copyright © 2015 Antoine Palazzolo. All rights reserved.
//

#import "NSData+SHA.h"
#import <CommonCrypto/CommonDigest.h>

@implementation NSData (SHA)
- (NSData * _Nonnull)jwt_shaDigestWithSize:(NSInteger)functionSize {
    NSMutableData *result = nil;
    switch (functionSize) {
        case 1:
            result = [[NSMutableData alloc] initWithLength:CC_SHA1_DIGEST_LENGTH];
            CC_SHA1(self.bytes, (CC_LONG)self.length, result.mutableBytes);
        case 256:
            result = [[NSMutableData alloc] initWithLength:CC_SHA256_DIGEST_LENGTH];
            CC_SHA256(self.bytes, (CC_LONG)self.length, result.mutableBytes);
            break;
        case 384:
            result = [[NSMutableData alloc] initWithLength:CC_SHA384_DIGEST_LENGTH];
            CC_SHA384(self.bytes, (CC_LONG)self.length, result.mutableBytes);
            break;
        case 512:
            result = [[NSMutableData alloc] initWithLength:CC_SHA512_DIGEST_LENGTH];
            CC_SHA512(self.bytes, (CC_LONG)self.length, result.mutableBytes);
            break;
        default:
            @throw [NSException exceptionWithName:@"SHA" reason:@"invalidate SHA function size" userInfo:nil];
    }
    return result;
}
@end
