// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
  [interpreter evalForm:@"(define *this-form* #!null)"];
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
  form.formName = @"Screen1";
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
  [interpreter evalForm:@"(reset-current-form-environment)"];
  [interpreter evalForm:@"(add-component Screen1 com.google.appinventor.components.runtime.Label Label1)"];
  [interpreter evalForm:@"(set-and-coerce-property! 'Label1 'FontSize 30 'number)"];
  Label *label = (Label *) form.components[0];
  XCTAssertEqual(30.0, ((UILabel *) label.view).font.pointSize);
}

- (void)testDeinit {
  SCMInterpreter *interpreter = SCMInterpreter.shared;
  __weak YailDictionary *environment = nil;
  __weak Form *weakForm = nil;
  @autoreleasepool {
    NSURL *runtimeUrl = [[NSBundle bundleForClass:[ReplForm class]] URLForResource:@"runtime" withExtension:@"scm"];
    if (runtimeUrl != nil) {
      if (interpreter.exception != nil) {
        // FIXME: Happens due to SCMInterpreter not finding runtime.scm when testing
        [interpreter clearException];
      }
      NSString *text = [NSString stringWithContentsOfURL:runtimeUrl encoding:NSUTF8StringEncoding error:nil];
      [interpreter evalForm:text];
      if (interpreter.exception != nil) {
        NSLog(@"Exception loading runtime: %@ (%@)", interpreter.exception.name, interpreter.exception);
        XCTFail(@"Exception occurred loading runtime");
        return;
      }
    }
    Form *form = [[Form alloc] init];
    form.formName = @"Screen1";
    weakForm = form;
    [interpreter setCurrentForm:form];
    [interpreter evalForm:@"(reset-current-form-environment)"];
    [interpreter evalForm:@"(add-component Screen1 com.google.appinventor.components.runtime.Label Label1)"];
    environment = form.environment;
    form = nil;
  }
  NSLog(@"Clearing *this-form*");
  [interpreter evalForm:@"(set! *this-form* #!null)"];
  NSLog(@"Running GC");
  [interpreter runGC];
  NSLog(@"Testing environment");
#ifdef MEMDBEUG
  id strongForm = weakForm;
  if (strongForm != nil) {
    [interpreter printGCRoots:strongForm];
  }
#endif
  XCTAssertNil(environment);
}

- (void)testDeinitMap {
  SCMInterpreter *interpreter = SCMInterpreter.shared;
  __weak Form *weakForm = nil;
  __weak Map *weakMap = nil;
  @autoreleasepool {
    NSURL *runtimeUrl = [[NSBundle bundleForClass:[ReplForm class]] URLForResource:@"runtime" withExtension:@"scm"];
    if (runtimeUrl != nil) {
      if (interpreter.exception != nil) {
        // FIXME: Happens due to SCMInterpreter not finding runtime.scm when testing
        [interpreter clearException];
      }
      NSString *text = [NSString stringWithContentsOfURL:runtimeUrl encoding:NSUTF8StringEncoding error:nil];
      [interpreter evalForm:text];
      if (interpreter.exception != nil) {
        NSLog(@"Exception loading runtime: %@ (%@)", interpreter.exception.name, interpreter.exception);
        XCTFail(@"Exception occurred loading runtime");
        return;
      }
    }
    Form *form = [[Form alloc] init];
    form.formName = @"Screen1";
    weakForm = form;
    [interpreter setCurrentForm:form];
    [interpreter evalForm:@"(reset-current-form-environment)"];
    [interpreter evalForm:@"(add-component Screen1 com.google.appinventor.components.runtime.Map Map1)"];
    [interpreter evalForm:@"(add-component Map1 com.google.appinventor.components.runtime.Marker Marker1)"];
    [interpreter evalForm:@"(add-component Map1 com.google.appinventor.components.runtime.LineString LineString1)"];
    [interpreter evalForm:@"(add-component Map1 com.google.appinventor.components.runtime.Polygon Polygon1)"];
    [interpreter evalForm:@"(add-component Map1 com.google.appinventor.components.runtime.Rectangle Rectangle1)"];
    [interpreter evalForm:@"(add-component Map1 com.google.appinventor.components.runtime.Circle Circle1)"];
    weakMap = (Map *) form.environment[@"Map1"];
    form = nil;
  }
  [interpreter evalForm:@"(set! *this-form* #!null)"];
  [interpreter runGC];
#ifdef MEMDEBUG
  id strongForm = weakForm;
  if (strongForm) {
    [interpreter printGCRoots:strongForm];
  }
#endif
  XCTAssertNil(weakForm);
#ifdef MEMDEBUG
  id strongMap = weakMap;
  if (strongMap) {
    [interpreter printGCRoots:strongMap];
  }
#endif
  XCTAssertNil(weakMap);
}

- (void)testDeinitNoScheme {
  __weak Form *weakForm = nil;
  @autoreleasepool {
    Form *form = [[Form alloc] init];
    form.formName = @"Screen1";
    Map *map = [[Map alloc] init:form];
    weakForm = form;
    form = nil;
  }
  Form *strongForm = weakForm;
  XCTAssertNil(strongForm);
}

@end
