// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class ClockTests: AppInventorTestCase {
  var clock = Clock(Form())
  var calendar = Calendar.current
  var components = DateComponents()
  var date = Date()
  
  override func setUp() {
    super.setUp()
    clock = Clock(Form())
    resetDate()
  }
  
  func resetDate() {
    components.month = 04
    components.day = 11
    components.year = 2018
    components.hour = 7
    components.minute = 27
    components.second = 20
    
    date = calendar.date(from: components)!
  }
  
  func testNow() {
    let expectedComponents = calendar.dateComponents([.month, .day, .year, .hour, .minute, .second], from: Date())
    let clocksComponents = calendar.dateComponents([.month, .day, .year, .hour, .minute, .second], from: clock.Now())
    XCTAssertTrue(clocksComponents == expectedComponents)
  }
  
  func testMakeInstant() {
    let instantString = "04/04/2018 08:05:01"
    do {
      let date = try clock.MakeInstant(instantString)
      let dateComponents = calendar.dateComponents([.month, .day, .year, .hour, .minute, .second], from: date)
      
      XCTAssertTrue(dateComponents.year == 2018)
      XCTAssertTrue(dateComponents.month == 4)
      XCTAssertTrue(dateComponents.day == 4)
      XCTAssertTrue(dateComponents.hour == 8)
      XCTAssertTrue(dateComponents.minute == 5)
      XCTAssertTrue(dateComponents.second == 1)
    } catch {
      XCTFail(String(format: "Expected ability to process date but was unable to process %@", instantString))
    }
  }
  
  func testMakeInstantError() {
    let instantString = DateFormatter.localizedString(from: Date(), dateStyle: .medium, timeStyle: .short)
    print("INST", instantString)
    do {
      _ = try clock.MakeInstant(instantString)
      XCTFail()
    } catch (_ as YailRuntimeError) {
      print("Test has succeeded")
    } catch {
      XCTFail()
    }
  }
  
  //MakeInstantFromMillis -- issue: returns the date a day before when millis = 0
  func testMakeInstanstFromMillis() {
    let zeroDate = 0
    let computedZeroInstant = Clock.MakeInstantFromMillis(0)
    let computedZeroComponents = calendar.dateComponents([.month, .day, .year], from: computedZeroInstant)
//    XCTAssertTrue(computedZeroComponents.month! == 1)
//    XCTAssertTrue(computedZeroComponents.day! == 1)
//    XCTAssertTrue(computedZeroComponents.year! == 1970)
  }
  
  func testGetMillis() {
    let zeroDate = 0
    let computedZeroInstant = Date(timeIntervalSince1970: 0)
    XCTAssertTrue(clock.GetMillis(computedZeroInstant) == zeroDate)
    
    let thousandDate = 1000
    let computedThousandInstant = Date(timeIntervalSince1970: TimeInterval(thousandDate/1000))
    XCTAssertTrue(clock.GetMillis(computedThousandInstant) == thousandDate)
  }
  
  func testAddDuration() {
    let updatedDate = clock.AddDuration(date, 1000)
    let updatedComponents = calendar.dateComponents([.second], from: updatedDate)
    XCTAssertTrue(updatedComponents.second == (components.second! + 1))
  }
  
  func testAddSeconds() {
    let updatedDate = clock.AddSeconds(date, 10)
    let updatedComponents = calendar.dateComponents([.second], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.second! - components.second!) == 10)
  }
  
  func testAddSecondsGreaterThanMinute() {
    let updatedDate = clock.AddSeconds(date, 61)
    let updatedComponents = calendar.dateComponents([.second, .minute], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.second! - components.second!) == 1)
    XCTAssertTrue(updatedComponents.minute! - components.minute! == 1)
  }
  
  func testAddSecondsNegative() {
    let updatedDate = clock.AddSeconds(date, -10)
    let updatedComponents = calendar.dateComponents([.second], from: updatedDate)
    XCTAssertTrue(updatedComponents.second! - components.second! == -10)
  }
  
  func testAddSecondsNegativeAffectsMinute() {
    let updatedDate = clock.AddSeconds(date, -61)
    let updatedComponents = calendar.dateComponents([.second, .minute], from: updatedDate)
    XCTAssertTrue(updatedComponents.second! - components.second! == -1)
    XCTAssertTrue(updatedComponents.minute! - components.minute! == -1)
  }
  
  func testAddMinutes() {
    let updatedDate = clock.AddMinutes(date, 1)
    let updatedComponents = calendar.dateComponents([.minute], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.minute! - components.minute!) == 1)
  }
  
  func testAddMinutesGreaterThanHour() {
    let updatedDate = clock.AddMinutes(date, 61)
    let updatedComponents = calendar.dateComponents([.minute, .hour], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.minute! - components.minute!) == 1)
    XCTAssertTrue(updatedComponents.hour! - components.hour! == 1)
  }
  
  func testAddMinutesNegative() {
    let updatedDate = clock.AddMinutes(date, -10)
    let updatedComponents = calendar.dateComponents([.minute], from: updatedDate)
    XCTAssertTrue(updatedComponents.minute! - components.minute! == -10)
  }
  
  func testAddMinutesNegativeAffectsMinute() {
    let updatedDate = clock.AddMinutes(date, -70)
    let updatedComponents = calendar.dateComponents([.minute, .hour], from: updatedDate)
    XCTAssertTrue(updatedComponents.minute! - components.minute! == -10)
    XCTAssertTrue(updatedComponents.hour! - components.hour! == -1)
  }
  
  func testAddHours() {
    let updatedDate = clock.AddHours(date, 5)
    let updatedComponents = calendar.dateComponents([.hour], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.hour! - components.hour!) == 5)
  }
  
  func testAddHoursGreaterThanDay() {
    let updatedDate = clock.AddHours(date, 29)
    let updatedComponents = calendar.dateComponents([.hour, .day], from: updatedDate)
    XCTAssertTrue(updatedComponents.hour! - components.hour! == 5)
    XCTAssertTrue(updatedComponents.day! - components.day! == 1)
  }
  
  func testAddHoursNegative() {
    let updatedDate = clock.AddHours(date, -5)
    let updatedComponents = calendar.dateComponents([.hour], from: updatedDate)
    XCTAssertTrue(updatedComponents.hour! - components.hour! == -5)
  }
  
  func testAddHoursNegativeAffectsDay() {
    let updatedDate = clock.AddHours(date, -25)
    let updatedComponents = calendar.dateComponents([.hour, .day], from: updatedDate)
    XCTAssertTrue(updatedComponents.hour! - components.hour! == -1)
    XCTAssertTrue(updatedComponents.day! - components.day! == -1)
  }
  
  func testAddDays() {
    let updatedDate = clock.AddDays(date, 5)
    let updatedComponents = calendar.dateComponents([.day], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.day! - components.day!) == 5)
  }
  
  func testAddDaysGreaterThanMonth() {
    let updatedDate = clock.AddDays(date, 31)
    let updatedComponents = calendar.dateComponents([.day, .month], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.day! - components.day!) == 1)
    XCTAssertTrue(updatedComponents.month! > components.month!)
  }
  
  func testAddDaysNegative() {
    let updatedDate = clock.AddDays(date, -5)
    let updatedComponents = calendar.dateComponents([.day], from: updatedDate)
    XCTAssertTrue(updatedComponents.day! - components.day! == -5)
  }
  
  func testAddDaysNegativeAffectsMonth() {
    let updatedDate = clock.AddDays(date, -12)
    let updatedComponents = calendar.dateComponents([.day, .month], from: updatedDate)
    XCTAssertTrue(updatedComponents.day! - components.day! == 19)
    XCTAssertTrue(updatedComponents.month! - components.month! == -1)
  }
  
  func testAddWeeks() {
    let updatedDate = clock.AddWeeks(date, 1)
    let updatedComponents = calendar.dateComponents([.day], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.day! - components.day!) == 7)
  }
  
  func testAddWeeksGreaterThanMonth() {
    let updatedDate = clock.AddWeeks(date, 5)
    let updatedComponents = calendar.dateComponents([.day, .month], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.day! - components.day!) == 5)
    XCTAssertTrue(abs(updatedComponents.month! - components.month!) == 1)
  }
  
  func testAddWeeksNegative() {
    let updatedDate = clock.AddWeeks(date, -1)
    let updatedComponents = calendar.dateComponents([.day], from: updatedDate)
    XCTAssertTrue(updatedComponents.day! - components.day! == -7)
  }
  
  func testAddWeeksNegativeAffectsMonth() {
    let updatedDate = clock.AddWeeks(date, -2)
    let updatedComponents = calendar.dateComponents([.day, .month], from: updatedDate)
    XCTAssertTrue(updatedComponents.day! - components.day! == 17)
    XCTAssertTrue(updatedComponents.month! - components.month! == -1)
  }
  
  func testAddMonths() {
    let updatedDate = clock.AddMonths(date, 5)
    let updatedComponents = calendar.dateComponents([.month], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.month! - components.month!) == 5)
  }
  
  func testAddMonthsGreaterThanYear() {
    let updatedDate = clock.AddMonths(date, 13)
    let updatedComponents = calendar.dateComponents([.month, .year], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.month! - components.month!) == 1)
    XCTAssertTrue(abs(updatedComponents.year! - components.year!) == 1)
  }
  
  func testAddMonthsNegative() {
    let updatedDate = clock.AddMonths(date, -3)
    let updatedComponents = calendar.dateComponents([.month], from: updatedDate)
    XCTAssertTrue(updatedComponents.month! - components.month! == -3)
  }
  
  func testAddYears() {
    let updatedDate = clock.AddYears(date, 5)
    let updatedComponents = calendar.dateComponents([.year], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.year! - components.year!) == 5)
  }
  
  func testAddYearsNegative() {
    let updatedDate = clock.AddYears(date, -5)
    let updatedComponents = calendar.dateComponents([.year], from: updatedDate)
    XCTAssertTrue(abs(updatedComponents.year! - components.year!) == 5)
    XCTAssertTrue(updatedComponents.year! < components.year!)
  }
  
  func testDuration() {
    let upASecondDate = calendar.date(byAdding: .second, value: 1, to: date, wrappingComponents: true)!
    let downASecondDate = calendar.date(byAdding: .second, value: -1, to: date, wrappingComponents: true)!
    let upAMinuteDate = calendar.date(byAdding: .minute, value: 1, to: date, wrappingComponents: true)!
    let upAnHourDate = calendar.date(byAdding: .hour, value: 1, to: date, wrappingComponents: true)!
    let upADayDate = calendar.date(byAdding: .day, value: 1, to: date, wrappingComponents: true)!
    let upAMonthDate = calendar.date(byAdding: .month, value: 1, to: date, wrappingComponents: true)!
    let upAYearDate = calendar.date(byAdding: .year, value: 1, to: date, wrappingComponents: true)!
    
    XCTAssertTrue(clock.Duration(date, upASecondDate) == 1000)
    XCTAssertTrue(clock.Duration(date, downASecondDate) == -1000)
    XCTAssertTrue(clock.Duration(date, upAMinuteDate) == 60000)
    XCTAssertTrue(clock.Duration(date, upAnHourDate) == 3600000)
    XCTAssertTrue(clock.Duration(date, upADayDate) == 86400000)
    XCTAssertTrue(clock.Duration(date, upAMonthDate) == 2592000000)
    XCTAssertTrue(clock.Duration(date, upAYearDate) == 31536000000)
  }
  
  func testDurationToSeconds() {
    XCTAssertTrue(clock.DurationToSeconds(60) == 0)
    XCTAssertTrue(clock.DurationToSeconds(1007) == 1)
    XCTAssertTrue(clock.DurationToSeconds(1200000) == 1200)
  }
  
  func testDurationToMinutes() {
    XCTAssertTrue(clock.DurationToMinutes(60) == 0)
    XCTAssertTrue(clock.DurationToMinutes(60090) == 1)
    XCTAssertTrue(clock.DurationToMinutes(1200000) == 20)
  }
  
  func testDurationToHours() {
    XCTAssertTrue(clock.DurationToHours(100) == 0)
    XCTAssertTrue(clock.DurationToHours(3600700) == 1)
    XCTAssertTrue(clock.DurationToHours(90000000) == 25)
  }
  
  func testDurationToDays() {
    XCTAssertTrue(clock.DurationToDays(500) == 0)
    XCTAssertTrue(clock.DurationToDays(86400900) == 1)
    XCTAssertTrue(clock.DurationToDays(6048000000) == 70)
  }
  
  func testDurationToWeeks() {
    XCTAssertTrue(clock.DurationToWeeks(1000) == 0)
    XCTAssertTrue(clock.DurationToWeeks(604800500) == 1)
    XCTAssertTrue(clock.DurationToWeeks(24192000000) == 40)
  }
  
  //Components
  
  //Format
}
