//
//  NSData+HMAC.m
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 18/11/15.
//  Copyright © 2015 Antoine Palazzolo. All rights reserved.
//

#import "NSData+HMAC.h"
#import <CommonCrypto/CommonHMAC.h>
@implementation NSData (HMAC)
- (NSData *)jwt_hmacSignatureWithSHAHashFuctionSize:(NSInteger)functionSize secret:(NSData *)secret {
    
    NSUInteger outputSize;
    CCHmacAlgorithm alg;
    switch (functionSize) {
        case 256:
            alg = kCCHmacAlgSHA256;
            outputSize = CC_SHA256_DIGEST_LENGTH;
            break;
        case 384:
            alg = kCCHmacAlgSHA384;
            outputSize = CC_SHA384_DIGEST_LENGTH;
            break;
        case 512:
            alg = kCCHmacAlgSHA512;
            outputSize = CC_SHA512_DIGEST_LENGTH;
            break;
        default:
            @throw [NSException exceptionWithName:@"HMAC" reason:@"invalidate sha function size" userInfo:nil];
    }
    NSMutableData * result = [[NSMutableData alloc] initWithLength:outputSize];
    CCHmac(alg, secret.bytes, secret.length, self.bytes, self.length, result.mutableBytes);
    return result;
}
@end
