// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import MobileCoreServices

open class ImagePicker: Picker, AbstractMethodsForPicker, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIPopoverPresentationControllerDelegate {
  
  fileprivate var _selectedImage = ""
  fileprivate let _isPad = UIDevice.current.userInterfaceIdiom == .pad
  
  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
  }
  
  //MARK: ImagePicker Properties
  @objc open var Selection: String {
    get {
      return _selectedImage
    }
  }

  //MARK: UIImagePickerControllerDelegate
  public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
    // Dismiss the picker if the user canceled.
    form?.dismiss(animated: true, completion: nil)
    AfterPicking()
  }
  
  public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
    // Local variable inserted by Swift 4.2 migrator.
    let info = convertFromUIImagePickerControllerInfoKeyDictionary(info)

    // The asset manager encodes the image URL's original path name to a new URL. The image data is then written to the new URL.
    let url = info[convertFromUIImagePickerControllerInfoKey(UIImagePickerController.InfoKey.referenceURL)] as! NSURL
    let urlPath = url.path?.chopPrefix(count: 1)
    guard let assetmgr = form?.application?.assetManager else {
      return
    }
    _selectedImage = assetmgr.pathForPrivateAsset(urlPath?.replace(target: ".HEIC", withString: ".jpg") ?? "")
    let selectedImageURL = URL(string: "file://" + _selectedImage.addingPercentEncoding(withAllowedCharacters: NSCharacterSet.urlPathAllowed)!)
    let image = info[convertFromUIImagePickerControllerInfoKey(UIImagePickerController.InfoKey.originalImage)] as! UIImage
    var data:Data?
    let pathExtension = url.pathExtension?.lowercased()
    if pathExtension == "jpg" || pathExtension == "jpeg" {
      data = image.jpegData(compressionQuality: 0.8)
    } else if pathExtension == "png" {
      data = image.pngData()
    } else if pathExtension == "heic" {
      // Camera picture, so use JPG
      data = image.jpegData(compressionQuality: 0.8)
    }
    do {
      try data?.write(to: selectedImageURL!)
    } catch {
      form?.dispatchErrorOccurredEvent(self, "ImagePicker",
          ErrorMessage.ERROR_CANNOT_COPY_MEDIA.code,
          ErrorMessage.ERROR_CANNOT_COPY_MEDIA.message)
      _selectedImage = ""
    }
    form?.dismiss(animated: true, completion: nil)
    AfterPicking()
  }
    
  //MARK: AbstractMethodsForPicker Implementation
  @objc public func open() {
    let picker = UIImagePickerController()
    if UIImagePickerController.isSourceTypeAvailable(UIImagePickerController.SourceType.photoLibrary) {
      picker.sourceType = .photoLibrary
      picker.mediaTypes = [kUTTypeImage as String]
      picker.delegate = self as UIImagePickerControllerDelegate & UINavigationControllerDelegate
      if !_isPad {
      form?.present(picker, animated: true, completion: nil)
      } else {
        picker.modalPresentationStyle = UIModalPresentationStyle.popover
        let popover = picker.popoverPresentationController
        popover?.delegate = self
        popover?.sourceView = _view
        popover?.sourceRect = _view.frame
        form?.present(picker, animated: true, completion: nil)
      }
    }
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
