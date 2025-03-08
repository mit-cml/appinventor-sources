// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

let kSaveFileError: Int32 = -2
let kUnableToLoadSourceError: Int32 = -3
let kNoResponseError: Int32 = -4

open class ImageBot: ProxiedComponent<ImageBot_token, ImageBot_request, ImageBot_response> {
  private static let SERVICE_URL = CHATBOT_HOST.appendingPathComponent("image/v1")
  public static let sRGB: CGColorSpace! = CGColorSpace(name: CGColorSpace.sRGB)
  public static let context = CIContext(options: [CIContextOption.outputColorSpace : NSNull()])

  @objc public init(_ parent: ComponentContainer) {
    super.init(parent, ImageBot.SERVICE_URL)
  }

  // MARK: Properties

  @objc open var InvertMask: Bool = true
  @objc open var Size: Int32 = 256

  // MARK: Methods

  @objc open func CreateImage(_ description: String) {
    do {
      try doRequest(configuration: {
        $0.operation = ImageBot_request.OperationType.create
        $0.prompt = description
        $0.size = "\(Size)x\(Size)"
      }) {
        if let error = $2 {
          self.ErrorOccurred($0, (error as? ProxyError)?.message ?? error.localizedDescription)
          return
        }
        guard let response = $1 else {
          return
        }
        do {
          self.ImageCreated(try self.saveImage(response.image))
        } catch {
          self.ErrorOccurred(kSaveFileError, "\(error)")
        }
      }
    } catch {
      self.ErrorOccurred(kRequestError, "\(error)")
    }
  }

  @objc open func EditImage(_ source: AnyObject, _ description: String) {
    guard let sourceData = loadImageData(source) else {
      self.ErrorOccurred(kUnableToLoadSourceError, "Unable to load source")
      return
    }
    do {
      try doRequest(configuration: {
        $0.operation = ImageBot_request.OperationType.edit
        $0.prompt = description
        $0.source = sourceData
        $0.size = "\(Size)x\(Size)"
      }, editHandler(_:_:_:))
    } catch {
      self.ErrorOccurred(kRequestError, "\(error)")
    }
  }

  @objc open func EditImageWithMask(_ imageSource: AnyObject, _ maskSource: AnyObject, _ prompt: String) {
    guard let sourceData = loadImageData(imageSource) else {
      ErrorOccurred(kUnableToLoadSourceError, "Unable to load source")
      return
    }
    guard let maskData = loadMaskData(maskSource) else {
      ErrorOccurred(kUnableToLoadSourceError, "Unable to load mask")
      return
    }
    do {
      try doRequest(configuration: {
        $0.operation = ImageBot_request.OperationType.edit
        $0.prompt = prompt
        $0.source = sourceData
        $0.mask = maskData
        $0.size = "\(Size)x\(Size)"
      }, editHandler(_:_:_:))
    } catch {
      ErrorOccurred(kRequestError, "\(error)")
    }
  }

  // MARK: Events

  @objc open func ErrorOccurred(_ responseCode: Int32, _ responseText: String) {
    DispatchQueue.main.async {
      if !EventDispatcher.dispatchEvent(of: self, called: "ErrorOccurred",
                                        arguments: responseCode as AnyObject,
                                        responseText as AnyObject) {
        self._form?.dispatchErrorOccurredEvent(self, "ErrorOccurred",
            ErrorMessage.ERROR_IMAGEBOT_ERROR, responseCode as AnyObject, responseText as AnyObject)
      }
    }
  }

  @objc open func ImageCreated(_ fileName: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "ImageCreated", arguments: fileName as AnyObject)
    }
  }

  @objc open func ImageEdited(_ fileName: String) {
    DispatchQueue.main.async {
      EventDispatcher.dispatchEvent(of: self, called: "ImageEdited", arguments: fileName as AnyObject)
    }
  }

  // MARK: Private implementation

  func loadMaskData(_ source: AnyObject) -> Data? {
    guard let bitmap = loadImage(source) else {
      return nil
    }
    if InvertMask {
      let filter = CIFilter(name: "CIColorMatrix", parameters: [
        "inputImage": bitmap,
        "inputRVector": CIVector(x: 1.0, y: 0.0, z: 0.0, w: 0.0),
        "inputGVector": CIVector(x: 0.0, y: 1.0, z: 0.0, w: 0.0),
        "inputBVector": CIVector(x: 0.0, y: 0.0, z: 1.0, w: 0.0),
        "inputAVector": CIVector(x: 0.0, y: 0.0, z: 0.0, w: -1.0),
        "inputBiasVector": CIVector(x: 0.0, y: 0.0, z: 0.0, w: 1.0)
      ])
      guard let output = filter?.outputImage?.cropped(to: bitmap.extent) else {
        return nil
      }
      let image = UIImage(ciImage: output)
      let format = UIGraphicsImageRendererFormat()
      format.scale = 1.0
      let size = CGSize(width: CGFloat(Size), height: CGFloat(Size))
      let result = UIGraphicsImageRenderer(size: size, format: format).pngData { context in
        image.draw(in: CGRect(origin: .zero, size: size), blendMode: .copy, alpha: 1.0)
      }
      return result
    }
    return ImageBot.context.pngRepresentation(of: bitmap, format: .ARGB8, colorSpace: ImageBot.sRGB)
  }

  func saveImage(_ content: Data) throws -> String {
    let destination = try FileUtil.getPictureFile("png")
    let url = URL(fileURLWithPath: destination)
    try content.write(to: url)
    return destination
  }

  private func editHandler(_ statusCode: Int32, _ response: ImageBot_response?, _ error: Error?) {
    if let error = error {
      self.ErrorOccurred(statusCode, (error as? ProxyError)?.message ?? error.localizedDescription)
    } else if let response = response {
      do {
        self.ImageEdited(try self.saveImage(response.image))
      } catch {
        self.ErrorOccurred(kSaveFileError, "\(error)")
      }
    }
  }
}

extension ImageBot_request: HasToken {
  public typealias T = ImageBot_token
}

extension ImageBot_response: HasResponse {
  public typealias T = Data

  public var response: Data {
    return self.image
  }
}

extension Canvas {
  var ciImage: CIImage? {
    let uiImage = UIGraphicsImageRenderer(size: self.canvasView.bounds.size).image { context in
      self.canvasView.layer.render(in: context.cgContext)
    }
    return uiImage.ciImage ?? CIImage(image: uiImage)
  }
}

#if DEBUG
extension CIImage {
  func base64png(_ size: CGFloat) -> String {
    let rectSize = CGSize(width: size, height: size)
    let format = UIGraphicsImageRendererFormat()
    format.scale = 1.0
    let result = UIGraphicsImageRenderer(size: rectSize, format: format).pngData { context in
      UIImage(ciImage: self).draw(in: CGRect(origin: .zero, size: rectSize))
    }
    return result.base64EncodedString()
  }
}
#endif


func loadImageData(_ source: AnyObject, resize: Int? = nil) -> Data? {
  guard let bitmap = loadImage(source) else {
    return nil
  }
  if let resize = resize {
    let filter = CIFilter(name: "CILanczosScaleTransform")!
    let targetSize = CGSize(width: CGFloat(resize), height: CGFloat(resize))
    let scale = targetSize.height / bitmap.extent.height
    let aspectRatio = targetSize.width / (bitmap.extent.width * scale)
    filter.setValue(bitmap, forKey: kCIInputImageKey)
    filter.setValue(scale, forKey: kCIInputScaleKey)
    filter.setValue(aspectRatio, forKey: kCIInputAspectRatioKey)
    if let resizedImage = filter.outputImage {
      return ImageBot.context.pngRepresentation(of: resizedImage, format: .ARGB8, colorSpace: ImageBot.sRGB)
    }
  }
  return ImageBot.context.pngRepresentation(of: bitmap, format: .ARGB8, colorSpace: ImageBot.sRGB)
}

func loadImage(_ source: AnyObject) -> CIImage? {
  if let canvas = source as? Canvas {
    return canvas.ciImage
  } else if let image = source as? Image {
    guard let uiImage = (image.view as? UIImageView)?.image else {
      return nil
    }
    return CIImage(image: uiImage)
  } else if let path = source as? String {
    let resolvedPath = AssetManager.shared.pathForExistingFileAsset(path)
    if !resolvedPath.isEmpty, let image = AssetManager.shared.imageFromPath(path: resolvedPath) {
      return CIImage(image: image)
    }
  }
  return nil
}
