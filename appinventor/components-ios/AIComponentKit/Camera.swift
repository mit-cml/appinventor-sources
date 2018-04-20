// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

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

  open func HasPermission() -> Bool {
    if let camPermission = PermissionHandler.HasPermission(for: .camera) {
      return camPermission
    } else {
      return false
    }
  }

  open func RequestPermission() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camChanged {
        self.PermissionChange(camAllowed)
      }
    }
  }

  open func PermissionChange(_ allowed: Bool) {
    EventDispatcher.dispatchEvent(of: self, called: "PermissionChange", arguments: allowed as AnyObject)
  }

  fileprivate func presentNotice(camStatus: Bool){
    if (camStatus) {
      let picker = UIImagePickerController()
      picker.delegate = self
      picker.sourceType = .camera
      picker.allowsEditing = false
      picker.cameraCaptureMode = .photo
      _form.present(picker, animated: true, completion: nil)
      _pictureRequest = true
    } else {
      let alert = UIAlertController(title: "Permission error", message: "", preferredStyle: .alert)
      alert.message = "AppInventor does not have access to the camera. As a result, the Camera will not display"
      alert.addAction(UIAlertAction(title: "OK", style: .default))
      _form.present(alert, animated: true, completion: nil)
    }
  }

  open func TakePicture() {
    PermissionHandler.RequestPermission(for: .camera) { camAllowed, camChanged in
      if camChanged {
        self.PermissionChange(camAllowed)
      }
      DispatchQueue.main.async {
        self.presentNotice(camStatus: camAllowed)
      }
    }
  }

  public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
    if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
      if let data = UIImageJPEGRepresentation(image, 0.8) {
        let filename = AssetManager.shared.pathForPublicAsset("AI_\(_dateFormatter.string(from: Date())).jpg")
        do {
          try data.write(to: URL(fileURLWithPath: filename))
          AfterPicture(filename)
        } catch {
        _form.dispatchErrorOccurredEvent(self, "TakePicture", ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.code, ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.message)
        }
      } else {
        _form.dispatchErrorOccurredEvent(self, "TakePicture", ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.code, ErrorMessage.ERROR_CAMERA_CANNOT_SAVE_FILE.message)
      }
    } else {
      _form.dispatchErrorOccurredEvent(self, "TakePicture", ErrorMessage.ERROR_CAMERA_NO_IMAGE_RETURNED.code, ErrorMessage.ERROR_CAMERA_NO_IMAGE_RETURNED.message)
    }
    if _pictureRequest {
      _form.dismiss(animated: true, completion: nil)
      _pictureRequest = false
    }
  }

  open func AfterPicture(_ image: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterPicture", arguments: image as AnyObject)
  }
}

