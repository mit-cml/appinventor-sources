// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

/**
 * A SoundRecorder class.  Multimedia component that records audio.
 * @author Nichole Clarke
 */
open class SoundRecorder: NonvisibleComponent, AVAudioRecorderDelegate {
  private let _TAG: String = "SoundRecorder"
  private var _savedRecordingPath: String = ""
  private var _recordingSession: AVAudioSession!
  private var _recorder: AVAudioRecorder? = nil
  private var _fileURL: URL?
  private let DEFAULT_SOUND_EXTENSION: String = "m4a"
  
  public override init(_ container: ComponentContainer) {
    super.init(container)
    
    _recordingSession = AVAudioSession.sharedInstance()
    do {
      setCategory(.playAndRecord, for: _recordingSession)
      try _recordingSession.setActive(true)
      _recordingSession.requestRecordPermission() { [unowned self] allowed in
        DispatchQueue.main.async {
          if !allowed {
            self._form.dispatchErrorOccurredEvent(self, "init", ErrorMessage.ERROR_SOUND_RECORDER_PERMISSION_DENIED.code, ErrorMessage.ERROR_SOUND_RECORDER_PERMISSION_DENIED.message)
          }
        }
      }
    } catch {
      _form.dispatchErrorOccurredEvent(self, "init", ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
    }
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
    if let _recorder = _recorder {
      NSLog("Start() called, but already recording to " + _recorder.url.absoluteString)
      return
    }
    let audioSession = AVAudioSession.sharedInstance()
    
    do {
      let filePath = _savedRecordingPath.isEmpty ? try FileUtil.getRecordingFile(DEFAULT_SOUND_EXTENSION) : try FileUtil.getRecordingFileFromAndroidPath(_savedRecordingPath)
      _fileURL = URL(fileURLWithPath: filePath)
      guard let _fileURL = _fileURL else {
        _form.dispatchErrorOccurredEvent(self, "Start", ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE.code, ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE.message, filePath as NSString)
        return
      }
      _recorder = try AVAudioRecorder(url: _fileURL, settings: getSettingsForFileType(fileExtension: _fileURL.pathExtension))
      _recorder?.delegate = self
      _recorder?.prepareToRecord()
      try audioSession.setActive(true)
      _recorder?.record()
      StartedRecording()
    } catch (let error as FileError) {
      finishedRecording(success: false)
      _form.dispatchErrorOccurredEvent(self, "Start", error.code, error.message, error.filePath)
    } catch {
      finishedRecording(success: false)
      _form.dispatchErrorOccurredEvent(self, "Start", ErrorMessage.ERROR_SOUND_RECORDER_CANNOT_CREATE.code, ErrorMessage.ERROR_SOUND_RECORDER_CANNOT_CREATE.message, _fileURL?.absoluteString ?? "Unknown filepath")
    }
  }
  
  @objc open func Stop() {
    if _recorder == nil {
      NSLog("Stop() called, but already stopped")
      return
    }
    _recorder?.stop()
    do {
      let audioSession = AVAudioSession.sharedInstance()
      try audioSession.setActive(false)
    } catch {
      _form.dispatchErrorOccurredEvent(self, "Stop", ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
    }
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
        _form.dispatchErrorOccurredEvent(self, "finishedRecording", ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
        return
      }
      let sound = _fileURL.path
      AfterSoundRecorded(sound)
    } else {
      _form.dispatchErrorOccurredEvent(self, "finishedRecording", ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
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
    _form.dispatchErrorOccurredEvent(self, "audioRecorderEncodeErrorDidOccur", ErrorMessage.ERROR_SOUND_RECORDER.code, ErrorMessage.ERROR_SOUND_RECORDER.message)
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
