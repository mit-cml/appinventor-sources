// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import XCTest
@testable import AIComponentKit

class TextToSpeechTests: AppInventorTestCase {
  var TextToSpeech1: TextToSpeech!

  override func setUp() {
    super.setUp()
    TextToSpeech1 = TextToSpeech(form)
  }

  func testLanguage() {
    TextToSpeech1.Language = "es"
    let language = TextToSpeech1.voice.language.split("-").first
    XCTAssertEqual("es", TextToSpeech1.Language)
    XCTAssertEqual("es", language)
  }

  func testCountry() {
    TextToSpeech1.Country = "IRL"
    let country = TextToSpeech1.voice.language.split("-").last
    XCTAssertEqual("IRL", TextToSpeech1.Country)
    XCTAssertEqual("IE", country)
  }

  func testPitchBounds() {
    XCTAssertEqual(1.0, TextToSpeech1.Pitch)

    // Test that we clamp to 2
    TextToSpeech1.Pitch = 5.0
    XCTAssertEqual(2.0, TextToSpeech1.Pitch)

    // Text that we clamp to 0.5
    TextToSpeech1.Pitch = 0.0
    XCTAssertEqual(0.5, TextToSpeech1.Pitch)
  }

  func testSpeedBounds() {
    XCTAssertEqual(0.49, TextToSpeech1.SpeechRate, accuracy: 0.0001)

    TextToSpeech1.SpeechRate = 0.0
    XCTAssertEqual(0.19, TextToSpeech1.SpeechRate)

    TextToSpeech1.SpeechRate = 5.0
    XCTAssertEqual(0.67, TextToSpeech1.SpeechRate, accuracy: 0.0001)
  }

  func testAvailableCountries() {
    XCTAssertGreaterThan(TextToSpeech1.AvailableCountries.count, 0)
  }

  func testAvailableLanguages() {
    XCTAssertGreaterThan(TextToSpeech1.AvailableLanguages.count, 0)
  }

  func testSpeak() {
    expectToReceiveEvent(on: TextToSpeech1, named: "BeforeSpeaking")
    expectToReceiveEvent(on: TextToSpeech1, named: "AfterSpeaking")
    try! TextToSpeech1.Speak("unit test")
    verify(timeout: 4.0)
    XCTAssertTrue(TextToSpeech1.Result)
  }
}
