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
#import <objc/runtime.h>
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
  [interpreter evalForm:@"(define x (yail:make-instance AIComponentKit.Button *this-form*))"];
  XCTAssertEqual(1, form.components.count);
  XCTAssertTrue([(NSObject *)form.components[0] isMemberOfClass:[Button class]]);
  [interpreter evalForm:@"(yail:invoke x 'setText \"Hello\")"];
  NSString *result = [interpreter evalForm:@"(yail:invoke x 'Text)"];
  NSLog(@"%@", result);
  XCTAssertEqualObjects(@"Hello", result);
}

- (void)testProtocols {
  Protocol *protocol = NSProtocolFromString(@"AIComponentKit.Component");
  Form *form = [[Form alloc] init];
  Button *button = [[Button alloc] init:form];
  XCTAssertNotNil(protocol);
  XCTAssertTrue([button conformsToProtocol:protocol]);
}

- (void)testFontSize {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  Form *form = [[Form alloc] init];
  NSURL *runtimeUrl = [[NSBundle bundleForClass:[ReplForm class]] URLForResource:@"runtime" withExtension:@"scm"];
  if (runtimeUrl != nil) {
    if (interpreter.exception != nil) {
      // FIXME: Happens due to SCMInterpreter not finding runtime.scm when testing
      [interpreter clearException];
    }
    NSString *text = [NSString stringWithContentsOfURL:runtimeUrl encoding:NSUTF8StringEncoding error:nil];
    [interpreter evalForm:text];
    [interpreter setCurrentForm:form];
    if (interpreter.exception != nil) {
      NSLog(@"Exception loading runtime: %@ (%@)", interpreter.exception.name, interpreter.exception);
      XCTFail(@"Exception occurred loading runtime");
      return;
    }
  }
  [interpreter evalForm:@"(add-component Screen1 com.google.appinventor.components.runtime.Label Label1)"];
  [interpreter evalForm:@"(set-and-coerce-property! 'Label1 'FontSize 30 'number)"];
  Label *label = form.components[0];
  XCTAssertEqual(30.0, ((UILabel *) label.view).font.pointSize);
}

@end
