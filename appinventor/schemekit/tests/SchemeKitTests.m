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

id yail_to_native(pic_state *pic, pic_value result);

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

typedef union {
  double dVal;
  float fVal;
  unsigned long long ullVal;
  long long llVal;
  unsigned long ulVal;
  long lVal;
  unsigned int uiVal;
  int iVal;
  unsigned short usVal;
  short sVal;
  unsigned char ucVal;
  char cVal;
} primitive_types;

@interface CoercionTestHelper: NSObject<NSCopying> {
  @public
  primitive_types result;
  NSString *strResult;
}

@end

@implementation CoercionTestHelper

+ (instancetype)helper {
  return [[CoercionTestHelper alloc] init];
}

- (void)setDouble:(double)result {
  self->result.dVal = result;
}

- (void)setFloat:(float)result {
  self->result.fVal = result;
}

- (void)setUnsignedLongLong:(unsigned long long)result {
  self->result.ullVal = result;
}

- (void)setLongLong:(long long)result {
  self->result.llVal = result;
}

- (void)setUnsignedLong:(unsigned long)result {
  self->result.ulVal = result;
}

- (void)setLong:(long)result {
  self->result.lVal = result;
}

- (void)setUnsignedInt:(unsigned int)result {
  self->result.uiVal = result;
}

- (void)setInt:(int)result {
  self->result.iVal = result;
}

- (void)setUnsignedShort:(unsigned short)result {
  self->result.usVal = result;
}

- (void)setShort:(short)result {
  self->result.sVal = result;
}

- (void)setUnsignedChar:(unsigned char)result {
  self->result.ucVal = result;
}

- (void)setChar:(char)result {
  self->result.cVal = result;
}

- (void)setString:(NSString *)result {
  self->strResult = result;
}

- (void)setNumber:(NSNumber *)result {
  self->strResult = [result stringValue];
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

- (id)copy {
  return self;
}

@end

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
  [interpreter evalForm:@"(define *this-form* #!null)"];
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

- (void)testYailDoubleToObjCDoubleConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setDouble 0.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqualWithAccuracy(0.25, helper->result.dVal, 1e-7);
}

- (void)testYailDoubleToObjCFloatConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setFloat 0.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqualWithAccuracy(0.25, helper->result.fVal, 1e-7);
}

- (void)testYailDoubleToObjCUnsignedLongLongConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setUnsignedLongLong 42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(42, helper->result.ullVal);
}

- (void)testYailDoubleToObjCLongLongConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setLongLong -42.45)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(-42, helper->result.llVal);
}

- (void)testYailDoubleToObjCUnsignedLongConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setUnsignedLong 42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(42, helper->result.ulVal);
}

- (void)testYailDoubleToObjCLongConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setLong -42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(-42, helper->result.lVal);
}

- (void)testYailDoubleToObjCUnsignedIntConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setUnsignedInt 42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(42, helper->result.uiVal);
}

- (void)testYailDoubleToObjCIntConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setInt -42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(-42, helper->result.iVal);
}

- (void)testYailDoubleToObjCUnsignedShortConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setUnsignedShort 42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(42, helper->result.usVal);
}

- (void)testYailDoubleToObjCShortConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setShort -42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(-42, helper->result.sVal);
}

- (void)testYailDoubleToObjCUnsignedCharConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setUnsignedChar 42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(42, helper->result.ucVal);
}

- (void)testYailDoubleToObjCCharConversion {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setChar -42.25)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertEqual(-42, helper->result.cVal);
}

- (void)testYailDoubleToObjCString {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  CoercionTestHelper *helper = [CoercionTestHelper helper];
  [interpreter setValue:helper forSymbol:@"test-helper"];
  [interpreter evalForm:@"(yail:invoke test-helper 'setNumber 42.25)"];
  XCTAssertNil(interpreter.exception);
  NSLog(@"Result: %@", helper->strResult);
  XCTAssertTrue([@"42.25" isEqualToString:helper->strResult]);
}

- (void)testUTF8Support {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"(string-append \"Hello \" \"🌎\")"];
  XCTAssertNil(interpreter.exception);
  NSString *result2 = [interpreter evalForm:@"\"你好\""];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertNotNil(result2);
  XCTAssertEqualObjects(@"Hello 🌎", result);
  XCTAssertEqualObjects(@"你好", result2);
}

- (void)testMultipointUTFParsing {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"\"\\ud83d\\udc4b\""];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertEqualObjects(@"👋", result);
}

- (void)testSinglePointUTF16Parsing {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"\"\\u4f60\\u597d\""];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertEqualObjects(@"你好", result);
}

- (void)testSinglePointUTF16CharacterParsing {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"#\\xD800"];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertEqualObjects(@"55296", result);
}

- (void)testFormatLong {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"(yail:format-exact 4028354713.0)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertEqualObjects(@"4028354713", result);
}

- (void)testFormatNegative {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@"(yail:format-exact -1)"];
  XCTAssertNil(interpreter.exception);
  XCTAssertNotNil(result);
  XCTAssertEqualObjects(@"-1", result);
}

- (void)testYailToNative {
  char t;
  pic_state *pic;
  pic_value e;
  id result;
  picrin_native_stack_start = &t;
  pic = pic_open(pic_default_allocf, NULL);

  pic_try {
    pic_init_picrin(pic);
    size_t ai = pic_enter(pic);

    result = yail_to_native(pic, pic_true_value(pic));
    XCTAssertNotNil(result);
    XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
    XCTAssertTrue([(NSNumber *)result boolValue]);

    result = yail_to_native(pic, pic_false_value(pic));
    XCTAssertNotNil(result);
    XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
    XCTAssertFalse([(NSNumber *)result boolValue]);

    result = yail_to_native(pic, pic_cstr_value(pic, "test"));
    XCTAssertNotNil(result);
    XCTAssertTrue([result isKindOfClass:[NSString class]]);
    XCTAssertEqualObjects(@"test", result);

    result = yail_to_native(pic, pic_int_value(pic, 42));
    XCTAssertNotNil(result);
    XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
    XCTAssertEqual(42, [(NSNumber *)result integerValue]);

    result = yail_to_native(pic, pic_float_value(pic, 12.5));
    XCTAssertNotNil(result);
    XCTAssertTrue([result isKindOfClass:[NSNumber class]]);
    XCTAssertEqualWithAccuracy(12.5, [(NSNumber *)result doubleValue], 0.0000001);

    pic_leave(pic, ai);
  } pic_catch(e) {
    pic_print_error(pic, pic_stderr(pic), e);
    XCTFail("Picrin exception was thrown");
  }

  pic_close(pic);
}

- (void)testParseFloat {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];
  NSString *result = [interpreter evalForm:@".5"];
  XCTAssertEqualObjects(@"0.5", result);

  result = [interpreter evalForm:@"(+ .5)"];
  XCTAssertEqualObjects(@"0.5", result);
}

- (void)testFormat {
  SCMInterpreter *interpreter = [[SCMInterpreter alloc] init];

  // Working example
  NSString *result = [interpreter evalForm:@"(format #f \"text ~S ~A ~~ ~%\" \"abc\" \"def\")"];
  XCTAssertEqualObjects(@"text \"abc\" def ~ \n", result);

  // Simple example
  result = [interpreter evalForm:@"(format #f \"test\")"];
  XCTAssertEqualObjects(@"test", result);

  // Test too few parameters
  [interpreter evalForm:@"(format)"];
  XCTAssertNotNil(interpreter.exception);
  [interpreter clearException];

  // Test bad first argument
  [interpreter evalForm:@"(format 'foobar \"test\")"];
  XCTAssertNotNil(interpreter.exception);
  [interpreter clearException];

  // Test too few replacements in ~a
  [interpreter evalForm:@"(format #f \"~a\")"];
  XCTAssertNotNil(interpreter.exception);
  [interpreter clearException];

  // Test too few replacements in ~s
  [interpreter evalForm:@"(format #f \"~s\")"];
  XCTAssertNotNil(interpreter.exception);
  [interpreter clearException];

  // Test unrecognized substitutions
  result = [interpreter evalForm:@"(format #f \"~n\")"];
  XCTAssertEqualObjects(@"~n", result);

  // Test tilde at end of input
  result = [interpreter evalForm:@"(format #f \"~\")"];
  XCTAssertEqualObjects(@"", result);
  XCTAssertNil(interpreter.exception);

  // Print to stdout
  result = [interpreter evalForm:@"(format #t \"hello world~%\")"];
  XCTAssertEqualObjects(@"#undefined", result);
  XCTAssertNil(interpreter.exception);
}

@end
