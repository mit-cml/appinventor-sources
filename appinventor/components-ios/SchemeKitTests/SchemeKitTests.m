//
//  SchemeKitTests.m
//  SchemeKitTests
//
//  Created by Evan Patton on 9/22/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#import <XCTest/XCTest.h>
#import <SchemeKit/SchemeKit.h>
#import "SchemeKitTests-Swift.h"

extern char *picrin_native_stack_start;

void
pic_init_picrin(pic_state *pic)
{
  void pic_init_contrib(pic_state *);
  void pic_load_piclib(pic_state *);
  
  pic_init_contrib(pic);
  pic_load_piclib(pic);
  pic_init_yail(pic);
}

@interface SchemeKitTests : XCTestCase

@end

@implementation SchemeKitTests

- (void)testClassFromQualifiedName {
    MockContainer *container = [[MockContainer alloc] init];
    Class clazz = [MockComponent class];
    const char *className = object_getClassName(clazz);
    NSLog(@"Hello world!");
    NSLog(@"Class: %s", className);
    Class clazz2 = [SCMNameResolver classFromQualifiedName:"SchemeKitTests.MockComponent"];
    XCTAssertTrue(!!clazz2);
    id object = [[clazz2 alloc] init:container];
    XCTAssertNotNil(object);
    NSLog(@"Object: %@", object);
}

- (void)testSelectorForMethod {
  Class clazz = [MockComponent class];
  unsigned int count = 0;
  Method *methods = class_copyMethodList(clazz, &count);
  for (unsigned int i = 0; i < count; ++i) {
    char *returnType = method_copyReturnType(methods[i]);
    NSLog(@"Method Name: %s", sel_getName(method_getName(methods[i])));
    NSLog(@"Method Returns: %s", returnType);
    free(returnType);
    NSLog(@"Method Types: %s", method_getTypeEncoding(methods[i]));
    unsigned int argCount = method_getNumberOfArguments(methods[i]);
    NSMethodSignature *sig = [clazz instanceMethodSignatureForSelector:method_getName(methods[i])];
    NSLog(@"Method Returns: %s", sig.methodReturnType);
    for (unsigned int j = 0; j < argCount; ++j) {
      char *argType = method_copyArgumentType(methods[i], j);
      if (argType) NSLog(@"Method Type[%d]: %s", j, argType);
      NSLog(@"Method signature type[%d]: %s", j, [sig getArgumentTypeAtIndex:j]);
      free(argType);
    }
  }
  NSLog(@"Void: %s", @encode(void));
  NSLog(@"BOOL: %s", @encode(BOOL));
  NSLog(@"int: %s", @encode(int));
  NSLog(@"long: %s", @encode(long));
  NSLog(@"float: %s", @encode(float));
  NSLog(@"double: %s", @encode(double));
  NSLog(@"NSInteger: %s", @encode(NSInteger));
  NSLog(@"NSString: %s", @encode(NSString *));
  NSLog(@"NSObject: %s", @encode(NSObject *));
  NSLog(@"id: %s", @encode(id));
  free(methods);
}

- (void)testStaticMethodCall {
  SCMMethod *method = [SCMNameResolver methodForClass:[MockComponent class] withName:"makeMockComponent" argumentTypeList:@[@"component"]];
  XCTAssertNotNil(method);
  NSInvocation *invocation = [method staticInvocation];
  [invocation invoke];
  __unsafe_unretained id result = nil;
  [invocation getReturnValue:&result];
  XCTAssertNotNil(result);
  XCTAssertTrue([result isKindOfClass:[MockComponent class]]);
}

- (void)testZeroArgInitializerCall {
  MockContainer *container = [MockContainer alloc];
  SCMMethod *initializer = [SCMNameResolver initializerForClass:[MockComponent class] withName:"init"];
  XCTAssertNotNil(initializer);
  NSInvocation *invocation = [initializer invocationForInstance:container];
  [invocation invoke];
  __unsafe_unretained id result = nil;
  [invocation getReturnValue:&result];
  XCTAssertNotNil(result);
  XCTAssertEqual(container, result);
}

- (void)testOneArgInitializerCall {
  MockContainer *container = [[MockContainer alloc] init];
  MockComponent *instance = [MockComponent alloc];
  SCMMethod *initializer = [SCMNameResolver initializerWithArgForClass:[MockComponent class] withName:"init"];
  XCTAssertNotNil(initializer);
  NSInvocation *invocation = [initializer invocationForInstance:instance];
  [invocation setArgument:&container atIndex:2];
  [invocation invoke];
  __unsafe_unretained id result = nil;
  [invocation getReturnValue:&result];
  XCTAssertNotNil(result);
  XCTAssertEqual(instance, result);
}

- (void)testTwoArgInitializerCall {
  NSString *message = @"An error occurred";
  NSString *errorType = @"Test Error";
  YailRuntimeError *instance = [YailRuntimeError alloc];
  SCMMethod *initializer = [SCMNameResolver naryInitializerForClass:[YailRuntimeError class] withName:"init" argCount:2];
  XCTAssertNotNil(initializer);
  NSInvocation *invocation = [initializer invocationForInstance:instance];
  [invocation setArgument:&message atIndex:2];
  [invocation setArgument:&errorType atIndex:3];
  [invocation invoke];
  __unsafe_unretained id result = nil;
  [invocation getReturnValue:&result];
  XCTAssertNotNil(result);
  XCTAssertEqual(instance, result);
}

- (void)testMethodCall {
  SCMMethod *method = [SCMNameResolver methodForClass:[MockComponent class] withName:"DoStuff" argumentTypeList:@[@"component", @"number"]];
  XCTAssertNotNil(method);
  MockContainer *container = [[MockContainer alloc] init];
  MockComponent *component = [[MockComponent alloc] init:container];
  NSInvocation *invocation = [method invocationForInstance:component];
  NSInteger times = 5;
  [invocation setArgument:&component atIndex:2];
  [invocation setArgument:&times atIndex:3];
  [invocation invoke];
}

- (void)testPropertySetGet {
  MockContainer *container = [[MockContainer alloc] init];
  MockComponent *component = [[MockComponent alloc] init:container];
  SCMMethod *setter = [SCMNameResolver setterForProperty:"PropertyC" inClass:[MockComponent class] withType:@"number"];
  SCMMethod *getter = [SCMNameResolver methodForClass:[MockComponent class] withName:"PropertyC" argumentTypeList:@[]];
  XCTAssertNotNil(setter);
  XCTAssertNotNil(getter);
  double value = 1.25;
  NSInvocation *invocation;
  invocation = [getter invocationForInstance:component];
  [invocation invoke];
  [invocation getReturnValue:&value];
  XCTAssertEqual(0.0, value);
  value = 1.25;
  invocation = [setter invocationForInstance:component];
  [invocation setArgument:&value atIndex:2];
  [invocation invoke];
  value = 0.0;
  invocation = [getter invocationForInstance:component];
  [invocation invoke];
  [invocation getReturnValue:&value];
  XCTAssertEqual(1.25, value);
}

- (void)testParsingHelloPurr {
  char t;
  NSError *err = nil;
  NSString *hellopurr = [NSString stringWithContentsOfURL:[[NSBundle bundleForClass:[SchemeKitTests class]] URLForResource:@"HelloPurr" withExtension:@"yail"] encoding:NSUTF8StringEncoding error:&err];
  XCTAssertNil(err);
  pic_state *pic;
  pic_value port, form, e;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);
  
  pic_try {
    pic_init_picrin(pic);
    ASTRecursiveVisitor *visitor = [[ASTRecursiveVisitor alloc] initWithPicState:pic];
    const char *content = [hellopurr UTF8String];
    port = pic_fmemopen(pic, content, hellopurr.length, "r");
    size_t ai = pic_enter(pic);
    
    while (! pic_eof_p(pic, form = pic_read(pic, port))) {
      [visitor visit:form];
      
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }
  
  pic_close(pic);
}

- (void)testParsingCompanion {
  char t;
  NSError *err = nil;
  NSString *companion = [NSString stringWithContentsOfURL:[[NSBundle bundleForClass:[SchemeKitTests class]] URLForResource:@"Screen1" withExtension:@"yail"] encoding:NSUTF8StringEncoding error:&err];
  XCTAssertNil(err);
  pic_state *pic;
  pic_value port, form, e;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);
  
  pic_try {
    pic_init_picrin(pic);
    ASTRecursiveVisitor *visitor = [[ASTRecursiveVisitor alloc] initWithPicState:pic];
    const char *content = [companion UTF8String];
    port = pic_fmemopen(pic, content, companion.length, "r");
    size_t ai = pic_enter(pic);
    
    while (! pic_eof_p(pic, form = pic_read(pic, port))) {
      [visitor visit:form];
      
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }
  
  pic_close(pic);
}

- (void)testParsingRuntime {
  char t;
  NSError *err = nil;
  NSString *runtime = [NSString stringWithContentsOfURL:[[NSBundle bundleForClass:[SchemeKitTests class]] URLForResource:@"android-runtime" withExtension:@"scm"] encoding:NSUTF8StringEncoding error:&err];
  XCTAssertNil(err);
  pic_state *pic;
  pic_value port, form, e;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);
  
  pic_try {
    pic_init_picrin(pic);
    ASTRecursiveVisitor *visitor = [[ASTRecursiveVisitor alloc] initWithPicState:pic];
    const char *content = [runtime UTF8String];
    port = pic_fmemopen(pic, content, runtime.length, "r");
    size_t ai = pic_enter(pic);
    
    while (! pic_eof_p(pic, form = pic_read(pic, port))) {
      [visitor visit:form];
      
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }
  
  pic_close(pic);
}

- (void)testNativeMethods {
  char t;
  const char *yail = "(begin (import (scheme base) (yail))(define x (yail:make-instance NSMutableArray))(define y (yail:make-instance NSString))(yail:invoke x 'addObject y)(yail:invoke x 'count))";
  pic_state *pic;
  pic_value port, form, e;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);

  pic_try {
    pic_init_picrin(pic);
    port = pic_fmemopen(pic, yail, strlen(yail), "r");
    size_t ai = pic_enter(pic);

    while (! pic_eof_p(pic, form = pic_read(pic, port))) {
      pic_value result = pic_eval(pic, form, "scheme.base");
      XCTAssertTrue(pic_int_p(pic, result));
      XCTAssertEqual(1, pic_int(pic, result));

      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }

  pic_close(pic);
}

- (void)testParsingIOSRuntime {
  char t;
  NSError *err = nil;
  NSString *platform = [NSString stringWithContentsOfURL:[[NSBundle bundleForClass:[SchemeKitTests class]] URLForResource:@"runtime" withExtension:@"scm"] encoding:NSUTF8StringEncoding error:&err];
  XCTAssertNil(err);
  pic_state *pic;
  pic_value port, form, e;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);
  
  pic_try {
    pic_init_picrin(pic);
    ASTRecursiveVisitor *visitor = [[ASTRecursiveVisitor alloc] initWithPicState:pic];
    const char *content = [platform UTF8String];
    port = pic_fmemopen(pic, content, platform.length, "r");
    size_t ai = pic_enter(pic);
    
    while (! pic_eof_p(pic, form = pic_read(pic, port))) {
      [visitor visit:form];
      
      pic_leave(pic, ai);
    }
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }

  pic_close(pic);
}

- (void)testInterpreter {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"(define x (yail:make-instance NSObject)) (define y (yail:make-instance NSMutableArray)) (yail:invoke y 'addObject x) (yail:invoke y 'count)"];
  NSLog(@"Result: %@", result);
}

- (void)testCurrentForm {
  MockContainer *container = [[MockContainer alloc] init];
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  [interpreter setCurrentForm:container];
  NSLog(@"*this-form* = %@", [interpreter evalForm:@"*this-form*"]);
  XCTAssertTrue([[interpreter evalForm:@"*this-form*"] containsString:@"MockContainer"]);
}

- (void)testNativeInvocation {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  [interpreter evalForm:@"(define (add5 x) (+ x 5))"];
  id result = [interpreter invokeMethod:@"add5", [NSNumber numberWithInt:5], nil];
  XCTAssertNotNil(result);
  XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
  XCTAssertEqual(10, ((NSNumber *)result).intValue);
}

- (void)testNSArrayToPicrin {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSArray *inputs = @[@1, @2, @3, @4, @5];
  id result = [interpreter invokeMethod:@"length", inputs, nil];
  XCTAssertNotNil(result);
  XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
  XCTAssertEqual(5, [(NSNumber *)result intValue]);
}

- (void)testDoubleValues {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSArray *inputs = @[@1.5, @2.25];
  id result = [interpreter invokeMethod:@"+" withArgArray:inputs];
  XCTAssertNotNil(result);
  XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
  XCTAssertEqual(3.75, [(NSNumber *)result doubleValue]);
}

@end
