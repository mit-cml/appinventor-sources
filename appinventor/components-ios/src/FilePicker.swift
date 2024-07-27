//
//  FilePicker.swift
//  AIComponentKit
//
//  Created by Jonathan Tjandra on 4/11/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import UIKit
import MobileCoreServices

@available(iOS 14.0, *)
open class FilePicker: Picker, AbstractMethodsForPicker, UIDocumentPickerDelegate{
  
    public func open() {
      documentPicker.delegate = self
      documentPicker.modalPresentationStyle = .formSheet
      documentPicker.allowsMultipleSelection = false
      _container?.form?.present(documentPicker, animated: true)
    }
  
    fileprivate let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.image, .pdf, .plainText, .video])
    fileprivate var action = FileAction.PickExistingFile
    fileprivate var selection = ""
    fileprivate var mimeType = "*/*"
  
    public override init (_ parent: ComponentContainer) {
      super.init(parent)
      super.setDelegate(self)
      _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
    }
  
    @objc open var Action: FileAction {
      get {
        return action
      }
  
      set (action) {
        self.action = action
      }
    }
  
    @objc open var MimeType: String{
      get {
        return mimeType
      }
      set (mimeType){
       self.mimeType = mimeType
      }
    }
  
    @objc open var Selection: String{
        get {
          return selection
        }
      }

    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
      
      guard let fileURL = urls.first else { return }
      selection = fileURL.absoluteString
      AfterPicking()
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
      AfterPicking()
    }
}

