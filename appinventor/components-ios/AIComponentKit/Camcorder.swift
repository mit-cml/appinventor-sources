// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import MobileCoreServices
import AVFoundation

open class Camcorder: NonvisibleComponent, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
  fileprivate var recordRequest = false

  open func HasPermission() -> Bool {
    if let camPermission = PermissionHandler.HasPermission(for: .camera), let micPermission = PermissionHandler.HasPermission(for: .microphone) {
      return camPermission && micPermission
    } else {
      return false
    }
  }

  open func RequestPermission() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camAllowed {
        PermissionHandler.RequestPermission(for: .microphone) { micAllowed, micChanged in
          if camChanged || micChanged {
            self.PermissionChange(micAllowed)
          }
        }
      } else if camChanged {
        self.PermissionChange(camAllowed)
      }
    }
  }

  open func PermissionChange(_ allowed: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "PermissionChange", arguments: allowed as AnyObject)
  }

  open func presentNotice(camStatus: Bool, micStatus: Bool){
    let picker = UIImagePickerController()
    picker.delegate = self
    picker.sourceType = .camera
    picker.allowsEditing = false
    picker.mediaTypes = [kUTTypeMovie as String, "public.movie"]
    picker.cameraCaptureMode = .video
    if (camStatus && micStatus) {
      _form.present(picker, animated: true, completion: nil)
      recordRequest = true
    } else {
      var message = "AppInventor does not have access to the "
      let alert = UIAlertController(title: "Permission error", message: "", preferredStyle: .alert)
      if (camStatus){
        message += "microphone. As a result, the recording will not have audio. Do you still wish to proceed?"
      } else {
        message += "camera. As a result, the Camcorder will not display."
      }
      alert.message = message
      if (camStatus){
        let accept = UIAlertAction(title: "Yes", style: .default) { action in
          self._form.present(picker, animated: true, completion: nil)
          self.recordRequest = true
        }
        let reject = UIAlertAction(title: "No", style: .cancel)
        alert.addAction(accept)
        alert.addAction(reject)
      } else {
        alert.addAction(UIAlertAction(title: "OK", style: .default))
      }
      _form.present(alert, animated: true, completion: nil)
    }
  }

  open func RecordVideo() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camAllowed {
        PermissionHandler.RequestPermission(for: .microphone) { micAllowed, micChanged in
          if camChanged || micChanged {
            self.PermissionChange(camAllowed && micAllowed)
          }
          DispatchQueue.main.async {
            self.presentNotice(camStatus: camAllowed, micStatus: micAllowed)
          }
        }
      } else {
        if camChanged {
          self.PermissionChange(camAllowed)
        }
        DispatchQueue.main.async {
          self.presentNotice(camStatus: false, micStatus: false)
        }
      }
    }
  }

  open func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
    if let moviePath = info[UIImagePickerControllerMediaURL] as? URL {
      do {
        let newPath =  URL(string: "file://\(AssetManager.shared.pathForPublicAsset(moviePath.lastPathComponent))")
        try FileManager.default.moveItem(at: moviePath, to: newPath!)
        AfterRecording(newPath!.lastPathComponent)
      } catch {
        _form.dispatchErrorOccurredEvent(self, "RecordVideo", ErrorMessage.ERROR_CAMCORDER_CANNOT_SAVE_FILE.code, ErrorMessage.ERROR_CAMCORDER_CANNOT_SAVE_FILE.message)
      }
    } else {
      _form.dispatchErrorOccurredEvent(self, "RecordVideo", ErrorMessage.ERROR_CAMCORDER_NO_CLIP_RETURNED.code, ErrorMessage.ERROR_CAMCORDER_NO_CLIP_RETURNED.message)
    }
    if recordRequest {
      _form.dismiss(animated: true, completion: nil)
      recordRequest = false
    }
  }

  open func AfterRecording(_ clip: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterRecording", arguments: clip as AnyObject)
  }
}
