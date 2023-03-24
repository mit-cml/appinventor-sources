// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import Speech

fileprivate let kSpeechRecognizerPermission = "ios.permission.SPEECH_RECOGNIZER"

@objc open class SpeechRecognizer : NonvisibleComponent, SFSpeechRecognitionTaskDelegate {
  private var _result: String = ""
  private var _useLegacy = true
  private var audioEngine: AVAudioEngine? = nil
  private var recognitionTask: NSObject? = nil

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
  }

  @objc public func Initialize() {
    guard #available(iOS 10.0, *) else {
      _form?.dispatchErrorOccurredEvent(self, "SpeechRecognizer",
          ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.code)
      return
    }
  }

  // MARK: - SpeechRecognizer Properties

  @objc open var UseLegacy: Bool {
    get {
      return _useLegacy
    }
    set(legacy) {
      _useLegacy = legacy
    }
  }

  @objc open var Result: String {
    return _result
  }

  // MARK: - SpeechRecognizer Methods

  @objc open func GetText() {
    if #available(iOS 10.0, *) {
      doRequestPermission() { allowed, changed in
        DispatchQueue.main.async {
          if changed {
            if allowed {
              self._form?.PermissionGranted(kSpeechRecognizerPermission)
            } else {
              self._form?.dispatchPermissionDeniedEvent(self, "GetText", kSpeechRecognizerPermission)
            }
          }
          if allowed {
            self.startSpeechRecognition()
          }
        }
      }
    } else {
      self._form?.dispatchErrorOccurredEventDialog(self, "GetText",
          ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.code)
    }
  }

  @objc open func Stop() {
    if !_useLegacy {
      audioEngine?.stop()
      if #available(iOS 10.0, *) {
        (recognitionTask as? SFSpeechRecognitionTask)?.finish()
      } else {
        // Speech recognition isn't available before iOS 10
      }
      audioEngine = nil
      recognitionTask = nil
    }
  }

  // MARK: - SpeechRecognizer Events

  @objc open func BeforeGettingText() {
    EventDispatcher.dispatchEvent(of: self, called: "BeforeGettingText")
  }

  @objc open func AfterGettingText(_ text: String, _ partial: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterGettingText",
        arguments: text as AnyObject, partial as AnyObject)
  }

  // MARK: - SFSpeechRecognitionTaskDelegate implementation

  @available(iOS 10.0, *)
  open func speechRecognitionTask(_ task: SFSpeechRecognitionTask,
      didHypothesizeTranscription transcription: SFTranscription) {
    _result = transcription.formattedString
    if !_useLegacy {
      AfterGettingText(_result, true)
    }
  }

  @available(iOS 10.0, *)
  open func speechRecognitionTask(_ task: SFSpeechRecognitionTask,
      didFinishSuccessfully successfully: Bool) {
    if (successfully) {
      AfterGettingText(_result, false)
    }
  }

  // MARK: - Private implementation

  private func doRequestPermission(completionHandler: ((Bool, Bool) -> ())? = nil) {
    PermissionHandler.RequestPermission(for: .speech) { speechAllowed, speechChanged in
      if let handler = completionHandler {
        handler(speechAllowed, speechChanged)
      }
    }
  }

  @available(iOS 10.0, *)
  private func startSpeechRecognition() {
    // get speech recognizer
    guard let recognizer = SFSpeechRecognizer(), recognizer.isAvailable else {
      self._form?.dispatchErrorOccurredEvent(self, "GetText",
          ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNAVAILABLE.code)
      return
    }

    // prepare recording for speech recognition
    let audioEngine = AVAudioEngine()
    self.audioEngine = audioEngine
    let request = SFSpeechAudioBufferRecognitionRequest()
    request.shouldReportPartialResults = true

    let node = audioEngine.inputNode

    let recordingFormat = node.inputFormat(forBus: 0)
    node.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
      request.append(buffer)
    }

    let recognitionTask = recognizer.recognitionTask(with: request, delegate: self)
    audioEngine.prepare()
    self.recognitionTask = recognitionTask

    if _useLegacy {
      startSpeechRecognitionLegacy(audioEngine: audioEngine, request: request,
          recognitionTask: recognitionTask)
    } else {
      try? audioEngine.start()
      self.BeforeGettingText()
    }
  }

  @available(iOS 10.0, *)
  private func startSpeechRecognitionLegacy(audioEngine: AVAudioEngine,
      request: SFSpeechAudioBufferRecognitionRequest, recognitionTask: SFSpeechRecognitionTask) {
    // create a custom alert to notify user about speech recognition
    let alert = UIAlertController(title: "Speech recognition request",
        message: "This app is attempting to use speech recognition. To begin recording audio," +
            " press start. If you do not wish to record, press cancel (you can cancel while " +
            "recording)",
        preferredStyle: .alert)

    // create "Cancel", "Finish", "Start" actions for alert
    let cancel = UIAlertAction(title: "Cancel", style: .cancel, handler: { alert in
      request.endAudio()
      audioEngine.stop()
      recognitionTask.cancel()
    })

    let stop = UIAlertAction(title: "Finish recording", style: .default, handler: { alert in
      request.endAudio()
      audioEngine.stop()
      recognitionTask.finish()
    })

    let start = UIAlertAction(title: "Start recording", style: .default) { item in
      do {
        self._form?.present(alert, animated: true) {
          // we want to terminate before the one minute maximum
          DispatchQueue.main.asyncAfter(deadline: .now() + 59.0, execute: {
            audioEngine.stop()
            recognitionTask.finish()
            alert.dismiss(animated: false)
          })
        }

        // we want to change the alert status after starting recording
        alert.title = "Recording in progress"
        alert.message = "Currently recording audio for speech recognition. Press finish when " +
            "done speaking (for a maximum time of one minute), or cancel to prevent recording"
        try audioEngine.start()
        self.BeforeGettingText()
        item.isEnabled = false
        stop.isEnabled = true
      } catch {
        self._form?.dispatchErrorOccurredEvent(self, "GetText",
            ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_PROCESSING_ERROR.code)
      }
    }
    alert.addAction(start)
    alert.addAction(stop)
    alert.addAction(cancel)
    stop.isEnabled = false
    self._form?.present(alert, animated: false)
  }
}

