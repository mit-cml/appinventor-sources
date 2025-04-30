// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import UIKit
import MobileCoreServices

@available(iOS 14.0, *)
@objc open class FilePicker: Picker, AbstractMethodsForPicker, UIDocumentPickerDelegate, LifecycleDelegate {

  private var docTypes: [UTType]
  private var documentPicker: UIDocumentPickerViewController
  private var action = FileAction.PickExistingFile
  private var selection = ""
  private var mimeType = "*/*"

  public override init(_ parent: ComponentContainer) {
    let allTypes: [UTType] = [.image, .pdf, .text, .video, .audio]
    self.docTypes = allTypes
    self.documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: allTypes)
    super.init(parent)
    super.setDelegate(self)
    _view.addTarget(self, action: #selector(click), for: UIControl.Event.primaryActionTriggered)
  }

  // MARK: Properties

  @objc open var Action: FileAction {
    get {
      return action
    }

    set (action) {
      self.action = action
      switch action {
      case .PickExistingFile:
        documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: docTypes)
      case .PickNewFile:
        // TODO: This implementation is a placeholder. The actual behavior on iOS is more complex
        // so we will need to expand the component's functionality
        documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: docTypes)
        break
      case .PickDirectory:
        documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.folder])
      default:
        // TODO: Raise an error
        break
      }
    }
  }

  @objc open var MimeType: String {
    get {
      return mimeType
    }
    set (mimeType){
      self.mimeType = mimeType
      let mimeLower = mimeType.lowercased()
      if mimeLower.starts(with: "image/") {
        docTypes = [.image]
      } else if mimeLower.starts(with: "video/") {
        docTypes = [.video]
      } else if mimeLower.starts(with: "audio/") {
        docTypes = [.audio]
      } else if mimeLower.starts(with: "text/") {
        docTypes = [.text]
      } else if mimeLower == "application/pdf" {
        docTypes = [.pdf]
      } else {
        // Fall back to all possible types.
        docTypes = [.image, .pdf, .text, .video, .audio]
      }
      Action = action
    }
  }

  @objc open var Selection: String {
    get {
      return selection
    }
  }

  // MARK: AbstractMethodsForPicker Implementation

  public func open() {
    documentPicker.delegate = self
    documentPicker.modalPresentationStyle = .formSheet
    documentPicker.allowsMultipleSelection = false
    _container?.form?.present(documentPicker, animated: true)
  }

  // MARK: UIDocumentPickerDelegate Implementation

  public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
    guard let fileURL = urls.first else { return }
    closeResource()  // No-op if we haven't picked a document yet, but just in case
    selection = fileURL.absoluteString.replace(target: "file://", withString: "extfile://")
    guard fileURL.startAccessingSecurityScopedResource() else {
      // TODO: Report an error
      return
    }
    AfterPicking()
  }

  public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
    AfterPicking()
  }

  // MARK: LifecycleDelegate Implementation

  public func onDelete() {
    closeResource()
  }

  public func onClear() {
    closeResource()
  }

  // MARK: Private Implementation

  private func closeResource() {
    guard !selection.isEmpty, let url = URL(string: selection.replace(target: "extfile://", withString: "file://")) else { return }
    selection = ""
    url.stopAccessingSecurityScopedResource()
  }
}

