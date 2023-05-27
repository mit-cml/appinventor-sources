// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class File: NonvisibleComponent {
  @objc let NO_ASSETS = "No_Assets"
  private var _isRepl: Bool = false
  private let LOG_TAG: String = "FileComponent"

  public override init(_ container: ComponentContainer) {
    super.init(container)
    _isRepl = _form is ReplForm
  }

  /// MARK: File Properties

  /**
   * Whether the app has enabled LegacyMode logic for the File component. Note that on iOS all
   * apps are sandboxed, so every app works as if LegacyMode == false. Apps can change this but it
   * won't affect the behavior of the app.
   */
  @objc open var LegacyMode = false

  /// MARK: File Methods
  
  @objc open func SaveFile(_ text: String, _ fileName: String) {
    write(fileName, text, false)
  }
  
  @objc open func AppendToFile(_ text: String, _ fileName: String) {
    write(fileName, text, true)
  }
  
  @objc open func ReadFrom(_ fileName: String) {
    DispatchQueue.global(qos: .background).async {
      do {
        let filePath: String = FileUtil.absoluteFileName(fileName, self._isRepl)
        if filePath.isEmpty || !FileManager().fileExists(atPath: filePath) {
          throw FileError(ErrorMessage.ERROR_CANNOT_FIND_FILE, fileName)
        } else {
          let url = URL(fileURLWithPath: filePath)
          let text = try String(contentsOf: url , encoding: .utf8)
          let normalizedText = self.normalizeNewLineChars(text: text)
          DispatchQueue.main.async {
            self.GotText(normalizedText)
          }
        }
      } catch {
        DispatchQueue.main.async {
          self._form?.dispatchErrorOccurredEvent(self, "ReadFrom",
              ErrorMessage.ERROR_CANNOT_READ_FILE.code,
              ErrorMessage.ERROR_CANNOT_READ_FILE.message, fileName)
        }
      }
    }
  }
  
  @objc open func Delete(_ fileName: String) {
    if fileName.starts(with: "//") {
      _form?.dispatchErrorOccurredEvent(self, "DeleteFile",
          ErrorMessage.ERROR_CANNOT_DELETE_ASSET.code,
          ErrorMessage.ERROR_CANNOT_DELETE_ASSET.message, fileName)
      return
    }
    
    let filePath = FileUtil.absoluteFileName(fileName, _isRepl)
    let fileManager = FileManager()
    if filePath.isEmpty || !fileManager.fileExists(atPath: filePath) {
      _form?.dispatchErrorOccurredEvent(self, "DeleteFile",
          ErrorMessage.ERROR_CANNOT_FIND_FILE.code,
          ErrorMessage.ERROR_CANNOT_FIND_FILE.message, fileName)
    } else {
      do {
        try fileManager.removeItem(atPath: filePath)
      } catch {
        NSLog("An unexpected error occurred when attempting to delete file: \(fileName)")
      }
    }
  }
  
  private func write(_ fileName: String, _ text: String, _ append: Bool) {
    if fileName.starts(with: "//") {
      if append {
        _form?.dispatchErrorOccurredEvent(self, "AppendTo",
            ErrorMessage.ERROR_CANNOT_WRITE_ASSET.code,
            ErrorMessage.ERROR_CANNOT_WRITE_ASSET.message, fileName)
      } else {
        _form?.dispatchErrorOccurredEvent(self, "SaveFile",
            ErrorMessage.ERROR_CANNOT_WRITE_ASSET.code,
            ErrorMessage.ERROR_CANNOT_WRITE_ASSET.message, fileName)
      }
      return
    }
    DispatchQueue.global(qos: .background).async {
      do {
        let filePath = FileUtil.absoluteFileName(fileName, self._isRepl)
        try FileUtil.createFullFilePath(filePath, isAppend: append)
        if append, let fileHandle = FileHandle(forWritingAtPath: filePath) {
          defer {
            fileHandle.closeFile()
          }
          fileHandle.seekToEndOfFile()
          guard let textData = text.data(using: .utf8) else {
            DispatchQueue.main.async {
              self._form?.dispatchErrorOccurredEvent(self, "AppendTo",
                  ErrorMessage.ERROR_CANNOT_ENCODE_TEXT_AS_UTF8.code,
                  ErrorMessage.ERROR_CANNOT_ENCODE_TEXT_AS_UTF8.message, fileName)
            }
            return
          }
          fileHandle.write(textData)
        } else {
          try text.write(toFile: filePath, atomically: true, encoding: .utf8)
        }
        DispatchQueue.main.async {
          self.AfterFileSaved(fileName)
        }
      } catch {
        DispatchQueue.main.async {
          if append {
            self._form?.dispatchErrorOccurredEvent(self, "AppendTo",
                ErrorMessage.ERROR_CANNOT_CREATE_FILE.code,
                ErrorMessage.ERROR_CANNOT_CREATE_FILE.message, fileName)
          } else {
            self._form?.dispatchErrorOccurredEvent(self, "SaveFile",
                ErrorMessage.ERROR_CANNOT_CREATE_FILE.code,
                ErrorMessage.ERROR_CANNOT_CREATE_FILE.message, fileName)
          }
        }
      }
    }
  }
  
  // Copy the contents from the first file to the second file.
  func copyFile(fromPath: String, toPath: String) -> Bool {
      let fileManager = FileManager.default
      
      do {
          // Check if source file exists
          guard fileManager.fileExists(atPath: fromPath) else {
              print("Source file does not exist")
              return false
          }

          // Check if destination file already exists
          guard !fileManager.fileExists(atPath: toPath) else {
              print("Destination file already exists")
              return false
          }
          
          // Create intermediate directories if necessary
          let toDirectory = URL(fileURLWithPath: toPath).deletingLastPathComponent().path
          try fileManager.createDirectory(atPath: toDirectory, withIntermediateDirectories: true, attributes: nil)

          // Copy the file
          try fileManager.copyItem(atPath: fromPath, toPath: toPath)
          
          print("File copied successfully")
          return true
      } catch let error as NSError {
          print("Error copying file: \(error.localizedDescription)")
          return false
      }
  }
  
  // Move a file from one location to another.
  func moveFile(fromPath: String, toPath: String) -> Bool {
      let fileManager = FileManager.default
      
      do {
          // Check if source file exists
          guard fileManager.fileExists(atPath: fromPath) else {
              print("Source file does not exist")
              return false
          }
          
          // Check if destination file already exists
          guard !fileManager.fileExists(atPath: toPath) else {
              print("Destination file already exists")
              return false
          }

          // Create intermediate directories if necessary
          let toDirectory = URL(fileURLWithPath: toPath).deletingLastPathComponent().path
          try fileManager.createDirectory(atPath: toDirectory, withIntermediateDirectories: true, attributes: nil)

          // Move the file
          try fileManager.moveItem(atPath: fromPath, toPath: toPath)
          
          print("File moved successfully")
          return true
      } catch let error as NSError {
          print("Error moving file: \(error.localizedDescription)")
          return false
      }
  }
  
  // Create a new directory for storing files.
  func makeDirectory(directoryName: String, completion: @escaping (Bool) -> Void) {
      let fileManager = FileManager.default
      let documentsDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
      let directoryURL = documentsDirectory.appendingPathComponent(directoryName)
      
      if fileManager.fileExists(atPath: directoryURL.path) {
          if fileManager.isDirectory(url: directoryURL) {
              completion(true)
          } else {
              // cannot make a directory if there's a regular file there
              completion(false)
          }
      } else {
          do {
              try fileManager.createDirectory(at: directoryURL, withIntermediateDirectories: true, attributes: nil)
              completion(true)
          } catch {
              // cannot make directory, probably because an ancestor is read-only
              completion(false)
          }
      }
  }
  
  // Remove a directory from the file system.
  func removeDirectory(directoryName: String, completion: @escaping (Bool) -> Void) {
      let fileManager = FileManager.default
      let documentsDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
      let directoryURL = documentsDirectory.appendingPathComponent(directoryName)
      
      do {
          try fileManager.removeItem(at: directoryURL)
          completion(true)
      } catch {
          print("Error removing directory: \(error)")
          completion(false)
      }
  }

  // Get a list of files and directories in the given directory.
  func listDirectories(path: String) -> [String]? {
      let fileManager = FileManager.default
      let url = URL(fileURLWithPath: path)
      
      do {
          let contents = try fileManager.contentsOfDirectory(at: url, includingPropertiesForKeys: nil, options: [.skipsHiddenFiles, .skipsSubdirectoryDescendants])
          var dirs: [String] = []
          
          for item in contents {
              var isDir: ObjCBool = false
              if fileManager.fileExists(atPath: item.path, isDirectory: &isDir), isDir.boolValue {
                  dirs.append(item.lastPathComponent)
              }
          }
          
          return dirs
      } catch {
          print("Error listing directories: \(error)")
          return nil
      }
  }
  
  
  
  private func normalizeNewLineChars(text: String) -> String {
    return text.replace(target: "\r\n", withString: "\n")
  }
  
  @objc open func GotText(_ text: String) {
    EventDispatcher.dispatchEvent(of: self, called: "GotText", arguments: text as NSString)
  }
  
  @objc open func AfterFileSaved(_ fileName: String) {
    EventDispatcher.dispatchEvent(of: self, called: "AfterFileSaved",
        arguments: fileName as NSString)
  }
}


extension FileManager {
    func isDirectory(url: URL) -> Bool {
        var isDirectory: ObjCBool = false
        guard fileExists(atPath: url.path, isDirectory: &isDirectory) else { return false }
        return isDirectory.boolValue
    }
}

