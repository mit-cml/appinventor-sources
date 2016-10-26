//
//  aicompanion3Tests.swift
//  aicompanion3Tests
//
//  Created by Evan Patton on 9/23/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import XCTest
@testable import AICompanionApp

class AICompanionTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testSha1Hmac() {
      let seed = "lwearr"
      let code = seed.hmac(algorithm: CryptoAlgorithm.SHA1)
      NSLog("Seed = \(seed)")
      NSLog("Code = \(code)")
      XCTAssertEqual("cf81fecc42ff40eacc2e65413d5673ead3ec791b", code)
    }
    
    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }
    
}
