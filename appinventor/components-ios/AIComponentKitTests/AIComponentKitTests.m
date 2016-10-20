//
//  AIComponentKitTests.m
//  AIComponentKit
//
//  Created by Evan Patton on 10/16/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <XCTest/XCTest.h>
#import <Foundation/Foundation.h>
#import <SchemeKit/SchemeKit.h>
@import AIComponentKit;

@interface SchemeKitTests : XCTestCase

@end

@implementation SchemeKitTests

- (void)testSwiftStringType {
  Form *form = [[Form alloc] init];
  Button *button = [[Button alloc] init:form];
  id result = [button Text];
  NSLog(@"%@", [result class]);
  NSLog(@"Is kind of NSString? %d", [result isKindOfClass:[NSString class]]);
  NSString *str = (NSString *)result;
  NSLog(@"Contents: %s", str.UTF8String);
}

- (void)testButton {
  NSLog(@"Button class name: %s", class_getName([Button class]));
  Form *form = [[Form alloc] init];
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  [interpreter setCurrentForm:form];
  [interpreter evalForm:@"(define x (yail:make-instance AIComponentKit.Button current-form))"];
  XCTAssertEqual(1, form.components.count);
  XCTAssertTrue([(NSObject *)form.components[0] isMemberOfClass:[Button class]]);
  [interpreter evalForm:@"(yail:invoke x 'setText \"Hello\")"];
  NSString *result = [interpreter evalForm:@"(yail:invoke x 'Text)"];
  NSLog(@"%@", result);
  XCTAssertTrue([result isEqualToString:@"Hello"]);
}

@end
