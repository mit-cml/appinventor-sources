// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <XCTest/XCTest.h>
#import <SchemeKit/SchemeKit.h>

@interface SCMProcedureTests : XCTestCase {
  SCMInterpreter *interpreter;
  YailDictionary *env;
}

@end

@implementation SCMProcedureTests

- (void)setUp {
  interpreter = [[SCMInterpreter alloc] init];
  [SCMInterpreter setDefault:interpreter];
  env = [YailDictionary emptyDictionaryIn:interpreter];
  [interpreter setValue:env forSymbol:@"*test-environment*"];
}

- (void)testApply {
  [interpreter evalForm:@"(yail:invoke *test-environment* 'setObject:forKey: (lambda () #t) \"proc1\")"];
  XCTAssertNil(interpreter.exception);
  SCMProcedure *proc = (SCMProcedure *) env[@"proc1"];
  XCTAssertNotNil(proc);
  XCTAssertEqual([NSNumber numberWithBool:YES], [proc invoke]);
}

- (void)testApplyWithArgs {
  [interpreter evalForm:@"(yail:invoke *test-environment* 'setObject:forKey: (lambda (x y) (+ x y)) \"proc2\")"];
  XCTAssertNil(interpreter.exception);
  SCMProcedure *proc = (SCMProcedure *) env[@"proc2"];
  XCTAssertNotNil(proc);
  NSArray *args = @[@2, @2];
  NSNumber *result = (NSNumber *)[proc invokeWithArguments:args];
  XCTAssertEqual(4, result.intValue);
}

@end
