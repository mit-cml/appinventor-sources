// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * A SoundRecorder class.  Multimedia component that records audio.
 * @author Nichole Clarke
 */
open class SoundRecorder: NonvisibleComponent, AVAudioRecorderDelegate {
  private let _TAG: String = "SoundRecorder"
  private var _savedRecordingPath: String = ""
  private var _recorder: AVAudioRecorder? = nil
  private var _fileURL: URL?
  private let DEFAULT_SOUND_EXTENSION: String = "m4a"
  
  public override init(_ container: ComponentContainer) {
    super.init(container)
  }
  
  // MARK: Properties
  @objc open var SavedRecording: String {
    get {
      return _savedRecordingPath
    }
    set(savedRecording) {
      _savedRecordingPath = savedRecording
    }
  }
  
  // MARK: Events
  @objc open func Start() {
    guard PermissionHandler.HasPermission(for: .microphone) ?? false else {
      PermissionHandler.RequestPermission(for: .microphone) { (allowed, changed) in
        if allowed {
          self.Start()
        } else {
          self._form?.dispatchPermissionDeniedEvent(self, "Start", "RECORD_AUDIO")
        }
      }
      return
    }

    guard _recorder == nil else {
      NSLog("Start() called, but already recording to " + (_recorder?.url.absoluteString ?? ""))
      return
    }

    let audioSession = AVAudioSession.sharedInstance()
    setCategory(.playAndRecord, for: audioSession)
    
    do {
      try audioSession.setActive(true)
      let filePath = _savedRecordingPath.isEmpty ? try FileUtil.getRecordingFile(DEFAULT_SOUND_EXTENSION) : try FileUtil.getRecordingFileFromAndroidPath(_savedRecordingPath)
      _fileURL = URL(fileURLWithPath: filePath)
      guard let _fileURL = _fileURL else {
        _form?.dispatchErrorOccurredEvent(self, "Start",
            ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE.code,
            ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE.message, filePath as NSString)
        return
      }
      let recorder = try AVAudioRecorder(url: _fileURL, settings: getSettingsForFileType(fileExtension: _fileURL.pathExtension))
      recorder.delegate = self
      recorder.prepareToRecord()
      try audioSession.setActive(true)
      recorder.record()
      _recorder = recorder
      StartedRecording()
    } catch (let error as FileError) {
      finishedRecording(success: false)
      _form?.dispatchErrorOccurredEvent(self, "Start", error.code, error.message, error.filePath)
    } catch {
      finishedRecording(success: false)
      _form?.dispatchErrorOccurredEvent(self, "Start",
          ErrorMessage.ERROR_SOUND_RECORDER_CANNOT_CREATE.code,
          ErrorMessage.ERROR_SOUND_RECORDER_CANNOT_CREATE.message,
          _fileURL?.absoluteString ?? "Unknown filepath")
    }
  }
  
  @objc open func Stop() {
    guard let recorder = _recorder else {
      NSLog("Stop() called, but already stopped")
      return
    }
    _recorder = nil
    recorder.stop()
  }
  
  @objc open func AfterSoundRecorded(_ sound: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterSoundRecorded", arguments: sound as NSString)
  }
  
  @objc open func StartedRecording() {
    EventDispatcher.dispatchEvent(of: self, called: "StartedRecording")
  }
  
  @objc open func StoppedRecording() {
    EventDispatcher.dispatchEvent(of: self, called: "StoppedRecording")
  }
  
  // MARK: Delegate and Helper Functions
  private func finishedRecording(success: Bool) {
    if success {
      guard let _fileURL = _fileURL else {
        _form?.dispatchErrorOccurredEvent(self, "finishedRecording",
            ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
        return
      }
      let sound = _fileURL.path
      AfterSoundRecorded(sound)
    } else {
      _form?.dispatchErrorOccurredEvent(self, "finishedRecording",
          ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
    }
    StoppedRecording()
    _recorder = nil
    _fileURL = nil
  }
  
  open func audioRecorderDidFinishRecording(_ recorder: AVAudioRecorder, successfully flag: Bool) {
    finishedRecording(success: flag)
  }
  
  open func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder, error: Error?) {
    if _recorder == nil || recorder != _recorder {
      NSLog("ErrorOccurred with wrong recorder.  Ignoring")
      return
    }
    _form?.dispatchErrorOccurredEvent(self, "audioRecorderEncodeErrorDidOccur",
        ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
    _recorder?.stop()
    _recorder = nil
    _fileURL = nil
    StoppedRecording()
  }
  
  private func getSettingsForFileType(fileExtension: String) -> [String: Int] {
    switch fileExtension.lowercased() {
    case "m4a":
      return [ AVFormatIDKey: Int(kAudioFormatMPEG4AAC), AVSampleRateKey: 12000,
        AVNumberOfChannelsKey: 1, AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue]
    default:
      // currently default returns for ".aac"
      return [ AVFormatIDKey: Int(kAudioFormatMPEG4AAC), AVSampleRateKey: 12000,
               AVNumberOfChannelsKey: 1, AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue]
    }
  }
}
