// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MobileCoreServices
import AVFoundation

open class Camera: NonvisibleComponent, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
  fileprivate var _pictureRequest: Bool = false
  fileprivate let _dateFormatter: DateFormatter = DateFormatter()

  public override init(_ parent: ComponentContainer){
    _dateFormatter.dateFormat = "yyyyMMddHHmmss"
    super.init(parent)
  }

  @objc open func HasPermission() -> Bool {
    if let camPermission = PermissionHandler.HasPermission(for: .camera) {
      return camPermission
    } else {
      return false
    }
  }

  @objc open func RequestPermission() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camChanged {
        self.PermissionChange(camAllowed)
      }
    }
  }

  @objc open func PermissionChange(_ allowed: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "PermissionChange", arguments: allowed as AnyObject)
  }

  fileprivate func presentNotice(camStatus: Bool){
    if (camStatus) {
      let picker = UIImagePickerController()
      picker.delegate = self
      picker.sourceType = .camera
      picker.allowsEditing = false
      if UseFront {
        picker.cameraDevice = .front
      }
      picker.cameraCaptureMode = .photo
      _form?.present(picker, animated: true, completion: nil)
      _pictureRequest = true
    } else {
      let alert = UIAlertController(title: "Permission error", message: "", preferredStyle: .alert)
      alert.message = "AppInventor does not have access to the camera. As a result, the Camera will not display"
      alert.addAction(UIAlertAction(title: "OK", style: .default))
      _form?.present(alert, animated: true, completion: nil)
    }
  }

  @objc open func TakePicture() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camChanged {
        self.PermissionChange(camAllowed)
      }
      DispatchQueue.main.async {
        self.presentNotice(camStatus: camAllowed)
      }
    }
  }

  // Deprecated
  @objc open var UseFront: Bool = false

  public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
// Local variable inserted by Swift 4.2 migrator.
let info = convertFromUIImagePickerControllerInfoKeyDictionary(info)

    if let image = info[convertFromUIImagePickerControllerInfoKey(UIImagePickerController.InfoKey.originalImage)] as? UIImage {
      if let data = image.jpegData(compressionQuality: 0.8) {
        let filename = AssetManager.shared.pathForPublicAsset("AI_\(_dateFormatter.string(from: Date())).jpg")
        do {
          try data.write(to: URL(fileURLWithPath: filename))
          AfterPicture(filename)
        } catch {
        _form?.dispatchErrorOccurredEvent(self, "TakePicture",
            ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.code,
            ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.message)
        }
      } else {
        _form?.dispatchErrorOccurredEvent(self, "TakePicture",
            ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.code,
            ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.message)
      }
    } else {
      _form?.dispatchErrorOccurredEvent(self, "TakePicture",
          ErrorMessage.ERROR_CAMERA_NO_IMAGE_RETURNED.code,
          ErrorMessage.ERROR_CAMERA_NO_IMAGE_RETURNED.message)
    }
    if _pictureRequest {
      _form?.dismiss(animated: true, completion: nil)
      _pictureRequest = false
    }
  }

  @objc open func AfterPicture(_ image: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicture", arguments: image as AnyObject)
  }
}


// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromUIImagePickerControllerInfoKeyDictionary(_ input: [UIImagePickerController.InfoKey: Any]) -> [String: Any] {
	return Dictionary(uniqueKeysWithValues: input.map {key, value in (key.rawValue, value)})
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertFromUIImagePickerControllerInfoKey(_ input: UIImagePickerController.InfoKey) -> String {
	return input.rawValue
}
