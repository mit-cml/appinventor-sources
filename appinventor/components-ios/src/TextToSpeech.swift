// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import AVKit

/**
 * Component for converting text to speech using the AVKit framework.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
open class TextToSpeech: NonvisibleComponent, AVSpeechSynthesizerDelegate {
  fileprivate static let LOG_TAG = "TextToSpeech"
  fileprivate static var LANGUAGES: [String] = []
  fileprivate static var COUNTRIES: [String] = []
  fileprivate static var ISO_LANG_2_TO_3 = [String: String]()
  fileprivate static var ISO_LANG_3_TO_2 = [String: String]()
  fileprivate static var ISO_COUNTRY_2_TO_3 = [String: String]()
  fileprivate static var ISO_COUNTRY_3_TO_2 = [String: String]()
  fileprivate var _pitch: Float = 1.0
  fileprivate var _speechRate: Float = 1.0
  fileprivate var _voice: AVSpeechSynthesisVoice!
  fileprivate var _result: Bool = false
  fileprivate var _language: String = AVSpeechSynthesisVoice.currentLanguageCode()
  fileprivate var _countryCode: String
  fileprivate var _countryCode2: String
  fileprivate var _tts = AVSpeechSynthesizer()

  @discardableResult fileprivate class func getLanguageCodes() -> [String] {
    if (LANGUAGES.count > 0) {
      return LANGUAGES
    }
    let voices = AVSpeechSynthesisVoice.speechVoices()
    for voice in voices {
      let langWithCountry = voice.language
      let parts = langWithCountry.split("-")
      // insertion sort, but we'll only do this once per companion session.
      if (!LANGUAGES.contains(parts[0])) {
        LANGUAGES.append(parts[0])
      }
      if (!COUNTRIES.contains(parts[1])) {
        COUNTRIES.append(parts[1])
      }
    }
    LANGUAGES.sort()
    COUNTRIES.sort()
    if let plistPath = Bundle(for: TextToSpeech.self).url(forResource: "language-codes", withExtension: "plist") {
      if let languageMapping = NSDictionary(contentsOf: plistPath) {
        for (lang3, lang2) in languageMapping as! [NSString: NSString] {
          ISO_LANG_3_TO_2[lang3 as String] = lang2 as String
          ISO_LANG_2_TO_3[lang2 as String] = lang3 as String
        }
      }
    }
    if let plistPath = Bundle(for: TextToSpeech.self).url(forResource: "iso3166_1_2_to_iso3166_1_3", withExtension: "plist") {
      if let countryMapping = NSDictionary(contentsOf: plistPath) {
        for (country2, country3) in countryMapping as! [NSString: NSString] {
          ISO_COUNTRY_3_TO_2[country3 as String] = country2 as String
          ISO_COUNTRY_2_TO_3[country2 as String] = country3 as String
        }
      }
    }
    return LANGUAGES
  }

  public override init(_ parent: ComponentContainer) {
    TextToSpeech.getLanguageCodes()
    _voice = AVSpeechSynthesisVoice(language: _language)
    let parts = _language.split("-")
    _language = parts[0]
    _countryCode2 = parts[1]
    _countryCode = TextToSpeech.ISO_COUNTRY_2_TO_3[_countryCode2] ?? "USA"
    super.init(parent)
    _tts.delegate = self
    SpeechRate = 1.0
  }

  // MARK: Properties

  @objc open var Pitch: Float32 {
    get {
      return _pitch
    }
    set(pitch) {
      var pitch = pitch
      if (pitch < 0.5) {
        pitch = 0.5
      } else if (pitch > 2.0) {
        pitch = 2.0
      }
      _pitch = pitch
    }
  }

  // Note: Transformed SpeechRate. The below function maps an input of [0, 2] to [.19, .68],
  // half-speed to double-speed (roughly). Speeds < .2 are virtually the same,
  // and speeds > .62 are difficult to understand.
  @objc open var SpeechRate: Float32 {
    get {
      return _speechRate
    }
    set(speechRate) {
      let speechRate = speechRate > 2 ? 2: speechRate
      if speechRate <= 0 {
        _speechRate = 0.19
      } else if speechRate < 0.8 {
        _speechRate = (0.635 * speechRate + 0.7092) / 2.8076
      } else {
        _speechRate = (0.85 * speechRate + 1.4645) / 4.7226
      }
    }
  }

  @objc open var Result: Bool {
    get {
      return _result
    }
  }

  @objc open var Language: String {
    get {
      return _language
    }
    set(language) {
      let language = language.lowercased()
      if (TextToSpeech.LANGUAGES.contains(language)) {
        _language = language
        updateLanguage()
      }
    }
  }

  @objc open var Country: String {
    get {
      return _countryCode
    }
    set(country) {
      let countryUpper = country.uppercased()
      if let country = TextToSpeech.ISO_COUNTRY_3_TO_2[countryUpper] {
        if TextToSpeech.COUNTRIES.contains(country) {
          _countryCode2 = country
          _countryCode = countryUpper
          updateLanguage()
        }
      }
    }
  }

  @objc open var AvailableLanguages: [String] {
    get {
      return TextToSpeech.LANGUAGES
    }
  }

  @objc open var AvailableCountries: [String] {
    get {
      return TextToSpeech.COUNTRIES
    }
  }

  // MARK: Methods

  @objc open func Speak(_ message: String) throws {
    try AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playback)
    try AVAudioSession.sharedInstance().setActive(true)
    DispatchQueue.main.async {
      self.BeforeSpeaking()
      let utterance = AVSpeechUtterance(string: message)
      utterance.pitchMultiplier = self._pitch
      utterance.rate = self._speechRate
      utterance.voice = self._voice
      self._tts.stopSpeaking(at: AVSpeechBoundary.immediate)
      self._tts.speak(utterance)
    }
  }

  @objc open func Stop() {
    _tts.stopSpeaking(at: .immediate)
  }

  // MARK: Events

  @objc open func BeforeSpeaking() {
    EventDispatcher.dispatchEvent(of: self, called: "BeforeSpeaking")
  }

  @objc open func AfterSpeaking(_ result: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterSpeaking", arguments: result as AnyObject)
  }

  // MARK: AVSpeechSynthesizerDelegate implementation

  public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
    _result = false
    AfterSpeaking(_result)
  }

  public func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
    _result = true
    AfterSpeaking(_result)
  }

  // MARK: Private implementation

  // VisibleForTesting
  var voice: AVSpeechSynthesisVoice {
    return _voice
  }

  fileprivate func updateLanguage() {
    let language = _language + "-" + _countryCode2
    if let voice = AVSpeechSynthesisVoice(language: language) {
      _voice = voice
    }
  }
}

extension TextToSpeech: LifecycleDelegate {
  public func onResume() {
    if _tts.isPaused {
      _tts.continueSpeaking()
    }
  }

  public func onPause() {
    _tts.pauseSpeaking(at: .immediate)
  }

  public func onDestroy() {
    Stop()
  }

  public func onDelete() {
    Stop()
  }
}
