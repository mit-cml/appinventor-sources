// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import Speech

open class SpeechRecognizer : NonvisibleComponent, SFSpeechRecognitionTaskDelegate {
  fileprivate var _result: String = ""
  fileprivate var _useLegacy = true

  @objc public override init(_ container: ComponentContainer) {
    super.init(container)
    guard #available(iOS 10.0, *) else {
      _form.dispatchErrorOccurredEvent(self, "SpeechRecognizer", ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.code, ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.message)
      return
    }
  }

  @objc open var UseLegacy: Bool {
    get {
      return _useLegacy
    }
    set(legacy) {
      _useLegacy = legacy
    }
  }

  @objc open func Result() -> String {
    return _result
  }

  @available(iOS 10.0, *)
  open func speechRecognitionTask(_ task: SFSpeechRecognitionTask, didHypothesizeTranscription transcription: SFTranscription){
    _result = transcription.formattedString
    if !_useLegacy {
      AfterGettingText(_result, true)
    }
  }

  @available(iOS 10.0, *)
  open func speechRecognitionTask(_ task: SFSpeechRecognitionTask, didFinishSuccessfully successfully: Bool) {
    if (successfully) {
      AfterGettingText(_result, false)
    }
  }

  @objc open func HasPermission() -> Bool {
    return PermissionHandler.HasPermission(for: .speech) ?? false
  }

  @objc open func RequestPermission() {
    if #available(iOS 10.0, *) {
      doRequestPermission() { allowed, changed in
        if changed {
          self.PermissionChange(allowed)
        }
      }
    } else {
      self._form.dispatchErrorOccurredEventDialog(self, "GetText", ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.code, ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.message)
    }
  }

  fileprivate func doRequestPermission(completionHandler: ((Bool, Bool) -> ())? = nil) {
    PermissionHandler.RequestPermission(for: .speech) { speechAllowed, speechChanged in
      if let handler = completionHandler {
        handler(speechAllowed, speechChanged)
      }
    }
  }

  @objc open func PermissionChange(_ allowed: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "PermissionChange", arguments: allowed as AnyObject)
  }

  @objc open func GetText() {
    if #available(iOS 10.0, *) {
      doRequestPermission() { allowed, changed in
        DispatchQueue.main.async {
          self.onPermissionResult(allowed, changed)
        }
      }
    } else {
      self._form.dispatchErrorOccurredEventDialog(self, "GetText", ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.code, ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNSUPPORTED.message)
    }
  }

  @available(iOS 10.0, *)
  private func onPermissionResult(_ allowed: Bool, _ changed: Bool) {
    if allowed {
      // get speech recognizer
      guard let recognizer = SFSpeechRecognizer(), recognizer.isAvailable else {
        self._form.dispatchErrorOccurredEvent(self, "GetText", ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNAVAILABLE.code, ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_UNAVAILABLE.message)
        return
      }

      // prepare recording for speech recognition
      let audioEngine = AVAudioEngine()
      let request = SFSpeechAudioBufferRecognitionRequest()
      request.shouldReportPartialResults = true

      let node = audioEngine.inputNode

      let recordingFormat = node.inputFormat(forBus: 0)
      node.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
        request.append(buffer)
      }

      let recognitionTask = recognizer.recognitionTask(with: request, delegate: self)
      audioEngine.prepare()

      // create a custom alert to notify user about speech recognition
      let alert = UIAlertController(title: "Speech recognition request", message: "This app is attempting to use speech recognition. To begin recording audio, press start. If you do not wish to record, press cancel (you can cancel while recording)", preferredStyle: .alert)

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
          self._form.present(alert, animated: true) {
            // we want to terminate before the one minute maximum
            DispatchQueue.main.asyncAfter(deadline: .now() + 59.0, execute: {
              audioEngine.stop()
              recognitionTask.finish()
              alert.dismiss(animated: false)
            })
          }

          // we want to change the alert status after starting recording
          alert.title = "Recording in progress"
          alert.message = "Currently recording audio for speech recognition. Press finish when done speaking (for a maximum time of one minute), or cancel to prevent recording"
          try audioEngine.start()
          self.BeforeGettingText()
          item.isEnabled = false
          stop.isEnabled = true
        } catch {
          self._form.dispatchErrorOccurredEvent(self, "GetText", ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_PROCESSING_ERROR.code, ErrorMessage.ERROR_IOS_SPEECH_RECOGNITION_PROCESSING_ERROR.message)
        }
      }
      alert.addAction(start)
      alert.addAction(stop)
      alert.addAction(cancel)
      stop.isEnabled = false
      self._form.present(alert, animated: false)
    } else {
      let notice = UIAlertController(title: "Permissions denied", message: "Permissions to microphone and/or speech recognition were denied. As a result, SpeechRecognizer will not function", preferredStyle: .alert)
      notice.addAction(UIAlertAction(title: "OK", style: .default))
      self._form.present(notice, animated: true)
    }
    if changed {
      self.PermissionChange(allowed)
    }
  }

  @objc open func BeforeGettingText(){
    EventDispatcher.dispatchEvent(of: self, called: "BeforeGettingText")
  }

  @objc open func AfterGettingText(_ text: String, _ partial: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterGettingText", arguments: text as AnyObject, partial as AnyObject)
  }
}

