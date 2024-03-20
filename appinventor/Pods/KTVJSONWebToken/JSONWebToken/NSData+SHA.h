//
//  NSData+SHA.h
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 19/11/15.
//

#import <Foundation/Foundation.h>

@interface NSData (SHA)
- (NSData * _Nonnull)jwt_shaDigestWithSize:(NSInteger)functionSize;
@end
