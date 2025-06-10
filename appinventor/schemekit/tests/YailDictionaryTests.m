// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright © 2020 Massachusetts Institute of Technology, All rights reserved.

#import <XCTest/XCTest.h>

#import <SchemeKit/SchemeKit.h>

@interface YailDictionaryTests : XCTestCase {
  SCMInterpreter *interpreter;
}

@end

@implementation YailDictionaryTests

- (void)setUp {
  interpreter = [[SCMInterpreter alloc] init];
  [SCMInterpreter setDefault:interpreter];
}

- (void)testEmptyDictionaryConstructor {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:interpreter];
  XCTAssertNotNil(dict);
  XCTAssertEqual(0, dict.count);
}

- (void)testDictionaryConstructor {
  YailDictionary *dict = [YailDictionary dictionaryInInterpreter:interpreter
                                                     withObjects:@[@"test", @5]
                                                         forKeys:@[@"first", @"second"]];
  XCTAssertNotNil(dict);
  XCTAssertEqual(2, dict.count);
  XCTAssertEqualObjects(@"test", dict[@"first"]);
  XCTAssertEqualObjects(@5, dict[@"second"]);
}

- (void)testNSDictionaryToYailDictionary {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5} yailDictionaryUsingInterpreter:interpreter];
  XCTAssertNotNil(dict);
  XCTAssertEqual(2, dict.count);
  XCTAssertEqualObjects(@"test", dict[@"first"]);
  XCTAssertEqualObjects(@5, dict[@"second"]);
}

- (void)testAssociativeListToDictionary {
  YailList *list = [YailList listInInterpreter:interpreter ofValues:[YailList listInInterpreter:interpreter ofValues:@"first", @"test", nil], [YailList listInInterpreter:interpreter ofValues:@"second", @5, nil], nil];
  NSError *error = nil;
  YailDictionary *dict = [list alistToDictWithError:&error];
  XCTAssertNil(error);
  XCTAssertNotNil(dict);
  XCTAssertEqual(2, dict.count);
  XCTAssertEqualObjects(@"test", dict[@"first"]);
  XCTAssertEqualObjects(@5, dict[@"second"]);
}

- (void)testDictionaryToAssociativeList {
}

- (void)testGetObjectAtKeyPath {

}

- (void)testWalkKeyPath {

}

- (void)testSetObjectForKeyPath {

}

- (void)testContainsObject {

}

- (void)testContainsKey {

}

- (void)testObjectAtIndex {

}

- (void)testCount {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:interpreter];
  XCTAssertEqual(0, dict.count);
  XCTAssertTrue(dict.empty);
  dict[@"first"] = @"test";
  XCTAssertEqual(1, dict.count);
  XCTAssertFalse(dict.empty);
}

- (void)testObjectForKey {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5} yailDictionaryUsingInterpreter:interpreter];
  XCTAssertEqualObjects(@"test", [dict objectForKey:@"first"]);
  XCTAssertEqualObjects(@5, [dict objectForKey:@"second"]);
}

- (void)testKeyEnumerator {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5}
                          yailDictionaryUsingInterpreter:interpreter];
  NSEnumerator *it = [dict keyEnumerator];
  id key = nil;
  int count = 0;
  while ((key = [it nextObject]) != nil) {
    if (count == 0) {
      XCTAssertEqualObjects(@"first", key);
      XCTAssertEqualObjects(@"test", dict[key]);
    } else {
      XCTAssertEqualObjects(@"second", key);
      XCTAssertEqualObjects(@5, dict[key]);
    }
    count++;
  }
  XCTAssertEqual(2, count);
}

- (void)testSetObjectForKey {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:interpreter];
  [dict setObject:@"test" forKey:@"first"];
  XCTAssertEqual(1, dict.count);
  XCTAssertEqual(@"test", dict[@"first"]);
}

- (void)testSCMValueImplementation {
  YailDictionary *dict = [YailDictionary emptyDictionaryIn:interpreter];
  XCTAssertFalse(dict.isBool);
  XCTAssertFalse(dict.isNumber);
  XCTAssertFalse(dict.isString);
  XCTAssertFalse(dict.isList);
  XCTAssertTrue(dict.isDictionary);
  XCTAssertFalse(dict.isComponent);
  XCTAssertFalse(dict.isNil);
  XCTAssertFalse(dict.isCons);
  XCTAssertFalse(dict.isSymbol);
  XCTAssertFalse(dict.isExact);
}

- (void)testIsPicEqual {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5}
                          yailDictionaryUsingInterpreter:interpreter];
  XCTAssertTrue([dict isPicEqual:dict.value]);
  XCTAssertFalse([dict isPicEqual:[interpreter makeSymbol:@"*list*"].value]);
}

- (void)testNSCopying {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5}
                          yailDictionaryUsingInterpreter:interpreter];
  YailDictionary *copy = [dict copy];
  XCTAssertNotEqual(copy, dict);
  XCTAssertEqualObjects(copy, dict);
  copy[@"third"] = @"foobar";
  XCTAssertNotEqualObjects(copy, dict);
  XCTAssertEqual(2, dict.count);
  XCTAssertEqual(3, copy.count);
}

- (void)testNSCoding {
  // We don't actually support NSCoding, but it's required to be implemented because YailDictionary
  // inherits from NSDictionary
  YailDictionary *dict = [@{@"first": @"test", @"second": @5}
                          yailDictionaryUsingInterpreter:interpreter];
  NSKeyedArchiver *coder = [[NSKeyedArchiver alloc] init];
  [dict encodeWithCoder:coder];
  [coder finishEncoding];
}

- (void)testFastEnumeration {
  YailDictionary *dict = [@{@"first": @"test", @"second": @5}
                          yailDictionaryUsingInterpreter:interpreter];
  BOOL first = YES;
  for (id key in dict) {
    if (first) {
      XCTAssertEqualObjects(@"first", key);
      XCTAssertEqualObjects(@"test", dict[key]);
    } else {
      XCTAssertEqualObjects(@"second", key);
      XCTAssertEqualObjects(@5, dict[key]);
    }
    first = NO;
  }
}

- (void)testDebugDescription {
  YailDictionary *dict;

  // Test empty dictionary
  dict = [YailDictionary emptyDictionaryIn:interpreter];
  XCTAssertEqualObjects(@"{}", [dict debugDescription]);

  // Test dictionary with content
  dict = [@{@"first": @"test", @"second": @5} yailDictionaryUsingInterpreter: interpreter];
  XCTAssertEqualObjects(@"{\n    first = test,\n    second = 5\n}", [dict debugDescription]);
}

/// MARK: - Tests for YailList+YailDictionary category

- (void)testAlistToDictWithError {
  YailList *list;
  YailDictionary *dict;
  NSError *error;

  // An empty list should convert to an empty dictionary
  list = [YailList emptyListIn:interpreter];
  dict = [list alistToDictWithError:&error];
  XCTAssertNil(error);
  XCTAssertNotNil(dict);
  XCTAssertTrue(dict.empty);

  // A valid alist should convert to the corresponding dictionary
  list = [YailList listInInterpreter:interpreter ofValues:
          [YailList listInInterpreter:interpreter ofValues:@"first", @"test", nil],
          [YailList listInInterpreter:interpreter ofValues:@"second", @5 , nil],
          nil];
  dict = [list alistToDictWithError:&error];
  XCTAssertNil(error);
  XCTAssertNotNil(dict);
  XCTAssertFalse(dict.empty);
  XCTAssertEqual(2, dict.count);
  XCTAssertEqualObjects(@"test", dict[@"first"]);
  XCTAssertEqualObjects(@5, dict[@"second"]);

  // Lists with arbitrary items should not convert
  list = [YailList listInInterpreter:interpreter ofValues:@"string", @5, nil];
  dict = [list alistToDictWithError:&error];
  XCTAssertNotNil(error);
  XCTAssertNil(dict);
  XCTAssertEqualObjects(@1, error.userInfo[kSCMBadIndex]);

  // Lists with invalid number of child entries are not alists
  list = [YailList listInInterpreter:interpreter ofValues:[YailList emptyListIn:interpreter], nil];
  dict = [list alistToDictWithError:&error];
  XCTAssertNotNil(error);
  XCTAssertNil(dict);
  XCTAssertEqualObjects(@1, error.userInfo[kSCMBadIndex]);

  // Error should work even if the error is nil
  dict = [list alistToDictWithError:nil];
  XCTAssertNil(dict);
}

- (void)testIsAlist {
  YailList *list;

  // An empty list is an alist
  list = [YailList emptyListIn:interpreter];
  XCTAssertTrue([list isAlist]);

  // Test a valid alist with multiple entries
  list = [YailList listInInterpreter:interpreter ofValues:
          [YailList listInInterpreter:interpreter ofValues:@"first", @"test", nil],
          [YailList listInInterpreter:interpreter ofValues:@"second", @5, nil],
          nil];
  XCTAssertTrue([list isAlist]);

  // Lists with arbitrary items are not alists
  list = [YailList listInInterpreter:interpreter ofValues:@"string", @5, nil];
  XCTAssertFalse([list isAlist]);

  // Lists with invalid number of child entries are not alists
  list = [YailList listInInterpreter:interpreter ofValues:[YailList emptyListIn:interpreter], nil];
  XCTAssertFalse([list isAlist]);

  // Lists with valid NSArrays (they are coerced when added) can be alists...
  list = [YailList emptyListIn:interpreter];
  [list addObject:[NSArray arrayWithObjects:@"first", @"test", nil]];
  XCTAssertTrue([list isAlist]);

  // ...but not if the nested arrays are of the wrong size
  list = [YailList emptyListIn:interpreter];
  [list addObject:[NSArray array]];
  XCTAssertFalse([list isAlist]);
}

@end
