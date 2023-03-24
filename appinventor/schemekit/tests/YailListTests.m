// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import <XCTest/XCTest.h>
#import <SchemeKit/SchemeKit.h>

@interface YailListTests : XCTestCase {
  SCMInterpreter *interpreter;
}

@end

@implementation YailListTests

- (void)setUp {
  interpreter = [[SCMInterpreter alloc] init];
  [SCMInterpreter setDefault:interpreter];
}

- (void)tearDown {
  interpreter = nil;
}

- (void)testEmptyListConstructor {
  YailList *list = [YailList emptyListIn:interpreter];
  XCTAssertNotNil(list);
  XCTAssertTrue(list.isEmpty);
  XCTAssertEqual(1, list.count);   // Actual length including *list* header
  XCTAssertEqual(0, list.length);  // Length as seen by App Inventor users
}

- (void)testListFromVarargs {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  XCTAssertNotNil(list);
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testInitFromCArray {
  id objects[] = {
    @"first",
    @"second"
  };
  YailList *list = [[YailList alloc] initWithObjects:objects count:2];
  XCTAssertNotNil(list);
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testInitFromNSArray {
  YailList *list = [[YailList alloc] initWithArray:@[@"first", @"second"] inInterpreter:interpreter];
  XCTAssertNotNil(list);
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testListFromPicValue {
  YailList *original = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  YailList *list = [YailList wrapList:original.value  fromInterpreter:interpreter];
  XCTAssertNotNil(list);
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(2, list.length);
}

- (void)testListFromNSArray {
  YailList *list = [@[@"first", @"second"] yailListUsingInterpreter:interpreter];
  XCTAssertNotNil(list);
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testNestedLists {
  YailList *child1 = [YailList emptyListIn:interpreter];
  YailList *child2 = [YailList listInInterpreter:interpreter ofValues:@"string", nil];
  YailList *parent = [YailList listInInterpreter:interpreter ofValues:child1, child2, nil];
  XCTAssertEqual(2, parent.length);
  XCTAssertTrue([[parent objectAtIndex:1] isKindOfClass:[YailList class]]);
  YailList *testChild1 = (YailList *)[parent objectAtIndex:1];
  XCTAssertEqual(0, testChild1.length);
  XCTAssertTrue([[parent objectAtIndex:1] isKindOfClass:[YailList class]]);
  YailList *testChild2 = (YailList *)[parent objectAtIndex:2];
  XCTAssertEqual(1, testChild2.length);
}

- (void)testLength {
  YailList *list = [YailList emptyListIn:interpreter];
  XCTAssertEqual(1, list.count);  // Objective-C length (*list* symbol header)
  XCTAssertEqual(0, list.length);   // YAIL list length
  XCTAssertEqualObjects([interpreter makeSymbol:@"*list*"], [list objectAtIndex:0]);
}

- (void)testCreation {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  XCTAssertNotNil(list);
  XCTAssertEqual(3, list.count);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", [list objectAtIndex:1]);
  XCTAssertEqualObjects(@"second", [list objectAtIndex:2]);
}

- (void)testAddElement {
  YailList *list = [YailList emptyListIn:interpreter];
  [list addObject:@"first"];
  XCTAssertFalse(list.isEmpty);
  XCTAssertEqual(1, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
}

- (void)testObjectAtIndex {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  XCTAssertNotNil(list);
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testFastIteration {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  BOOL first = YES;
  int count = 0;
  for (NSString *element in list) {
    if (first) {
      XCTAssertEqualObjects(@"first", element);
      first = NO;
    } else {
      XCTAssertEqualObjects(@"second", element);
    }
    count++;
  }
  XCTAssertEqual(list.length, count);
}

- (void)testInsertion {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"second", nil];
  [list insertObject:@"first" atIndex:1];
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testRemoveLastObject {
  YailList *list = [@[@"first", @"second"] yailListUsingInterpreter:interpreter];
  [list removeLastObject];
  XCTAssertEqual(1, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
}

- (void)testRemoveObjectAtIndex {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", @"third", nil];
  [list removeObjectAtIndex:2];
  XCTAssertEqual(2, list.length);
  XCTAssertEqualObjects(@"first", list[1]);
  XCTAssertEqualObjects(@"third", list[2]);
}

- (void)testReplaceObjectAtIndex {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"wrong", nil];
  [list replaceObjectAtIndex:2 withObject:@"second"];
  XCTAssertEqualObjects(@"second", list[2]);
}

- (void)testAsYailListIdempotent {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  XCTAssertTrue(list == [list yailListUsingInterpreter:interpreter]);
}

- (void)testSCMObjectProtocol {
  YailList *list = [YailList emptyListIn:interpreter];
  XCTAssertTrue([list isList]);
  XCTAssertTrue([list isCons]);
  XCTAssertFalse([list isBool]);
  XCTAssertFalse([list isNumber]);
  XCTAssertFalse([list isString]);
  XCTAssertFalse([list isDictionary]);
  XCTAssertFalse([list isNil]);
  XCTAssertFalse([list isSymbol]);
  XCTAssertFalse([list isExact]);
  XCTAssertFalse([list isComponent]);
}

- (void)testNSCopying {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:@"first", @"second", nil];
  YailList *copy = [list copy];
  XCTAssertEqualObjects(copy, list);
  [copy addObject:@"third"];
  XCTAssertEqual(2, list.length);
  XCTAssertEqual(3, copy.length);
  XCTAssertNotEqualObjects(copy, list);
}

- (void)testNSCoding {
  NSKeyedArchiver *archiver = [[NSKeyedArchiver alloc] init];
  YailList *list = [YailList emptyListIn:interpreter];
  [list encodeWithCoder:archiver];
  NSKeyedUnarchiver *unarchiver = [[NSKeyedUnarchiver alloc] initForReadingWithData:archiver.encodedData];
  YailList *copy = [[YailList alloc] initWithCoder:unarchiver];
  XCTAssertEqualObjects(copy, list);
}

- (void)testIsPicEquals {
  YailList *list = [YailList emptyListIn:interpreter];
  pic_value f = pic_false_value(nil);
  XCTAssertFalse([list isPicEqual:f]);
}

- (void)testInitWithCapacity {
  YailList *list = [[YailList alloc] initWithCapacity:100];
  XCTAssertTrue(list.isEmpty);
}

- (void)testHash {
  YailList *list1 = [YailList emptyListIn:interpreter];
  YailList *list2 = [YailList listInInterpreter:interpreter ofValues:@"item", nil];
  XCTAssertTrue([list1 hash] != [list2 hash]);
}

- (void)testInequality {
  YailList *list1 = [YailList listInInterpreter:interpreter ofValues:@1, @2, nil];
  YailList *list2 = [YailList listInInterpreter:interpreter ofValues:@1, @3, nil];
  XCTAssertFalse([list1 isEqual:list2]);
}

- (void)testInequalityOtherType {
  YailList *list = [YailList emptyListIn:interpreter];
  XCTAssertFalse([list isEqual:@1]);
}

- (void)testNSArrayEmpty {
  XCTAssertTrue([NSArray array].isEmpty);
}

@end
