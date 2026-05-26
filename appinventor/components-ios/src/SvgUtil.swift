// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import UIKit
import SwiftSVG

/**
 * Utility methods for loading and rasterizing SVG images on iOS.
 *
 * SVG files cannot be loaded via `UIImage(contentsOfFile:)` on iOS; they must be
 * parsed and rasterized explicitly. This helper uses the **SwiftSVG** library
 * (already a CocoaPod dependency) to parse SVG data and render it into a
 * `UIImage` that can be used anywhere a `UIImage` is expected.
 *
 * The rasterization is performed **synchronously** on the current thread using
 * `NSXMLSVGParser.startParsing()`. SwiftSVG's `DispatchQueue.safeAsync`
 * extension executes the completion block inline when called from the main
 * thread, so no semaphore/deadlock concerns apply as long as this is called
 * from the main thread (which is the normal App Inventor component path).
 */
enum SvgUtil {

  /// Default raster size (points) when the SVG has no intrinsic dimensions.
  private static let defaultSize: CGFloat = 100

  /**
   * Returns `true` if the given path refers to an SVG file (by extension).
   *
   * - Parameter path: A filesystem path, asset name, or URL string.
   */
  static func isSvg(_ path: String) -> Bool {
    guard !path.isEmpty else { return false }
    // Strip any query string before checking the extension
    let base = path.components(separatedBy: "?").first ?? path
    return base.lowercased().hasSuffix(".svg")
  }

  /**
   * Synchronously loads and rasterizes an SVG file from the given filesystem
   * path into a `UIImage`.
   *
   * - Parameters:
   *   - filePath: The absolute filesystem path to the `.svg` file.
   *   - desiredWidth:  Target raster width in points; ≤ 0 means "use intrinsic".
   *   - desiredHeight: Target raster height in points; ≤ 0 means "use intrinsic".
   * - Returns: A rasterized `UIImage`, or `nil` if the file cannot be read or
   *   the SVG cannot be parsed.
   */
  static func imageFromSvgFile(
    _ filePath: String,
    desiredWidth: CGFloat = 0,
    desiredHeight: CGFloat = 0
  ) -> UIImage? {
    guard let data = try? Data(contentsOf: URL(fileURLWithPath: filePath)) else {
      NSLog("SvgUtil: cannot read SVG file at \(filePath)")
      return nil
    }
    return imageFromSvgData(data, desiredWidth: desiredWidth, desiredHeight: desiredHeight)
  }

  static func imageFromSvgFileAsync(
    _ filePath: String,
    desiredWidth: CGFloat = 0,
    desiredHeight: CGFloat = 0,
    completion: @escaping (UIImage?) -> Void
  ) {
    DispatchQueue.global(qos: .userInitiated).async {
      guard let data = try? Data(contentsOf: URL(fileURLWithPath: filePath)) else {
        NSLog("SvgUtil: cannot read SVG file at \(filePath)")
        DispatchQueue.main.async {
          completion(nil)
        }
        return
      }
      imageFromSvgDataAsync(data, desiredWidth: desiredWidth, desiredHeight: desiredHeight,
                            completion: completion)
    }
  }

  /**
   * Synchronously rasterizes SVG `Data` into a `UIImage`.
   *
   * This is the core rasterization function. It uses `NSXMLSVGParser` from
   * SwiftSVG, which calls its completion block synchronously when invoked from
   * the main thread (via `DispatchQueue.safeAsync`).
   *
   * - Parameters:
   *   - data: The raw SVG file contents.
   *   - desiredWidth:  Target raster width in points; ≤ 0 means "use intrinsic".
   *   - desiredHeight: Target raster height in points; ≤ 0 means "use intrinsic".
   * - Returns: A rasterized `UIImage`, or `nil` if parsing fails.
   */
  static func imageFromSvgData(
    _ data: Data,
    desiredWidth: CGFloat = 0,
    desiredHeight: CGFloat = 0
  ) -> UIImage? {
    var resultLayer: SVGLayer? = nil

    // NSXMLSVGParser's completion fires synchronously when called from the main
    // thread (SwiftSVG's safeAsync calls the block directly in that case).
    let parser = NSXMLSVGParser(SVGData: data) { layer in
      resultLayer = layer
    }
    parser.startParsing()

    guard let svgLayer = resultLayer else {
      NSLog("SvgUtil: SVG parsing produced no layer")
      return nil
    }

    return rasterize(svgLayer, desiredWidth: desiredWidth, desiredHeight: desiredHeight)
  }

  static func imageFromSvgDataAsync(
    _ data: Data,
    desiredWidth: CGFloat = 0,
    desiredHeight: CGFloat = 0,
    completion: @escaping (UIImage?) -> Void
  ) {
    DispatchQueue.global(qos: .userInitiated).async {
      let parser = NSXMLSVGParser(SVGData: data) { layer in
        completion(rasterize(layer, desiredWidth: desiredWidth, desiredHeight: desiredHeight))
      }
      parser.startParsing()
    }
  }

  private static func rasterize(
    _ svgLayer: SVGLayer,
    desiredWidth: CGFloat,
    desiredHeight: CGFloat
  ) -> UIImage? {
    // Determine raster size: prefer explicit desired size, then SVG bounding box, then default.
    let intrinsicW = svgLayer.boundingBox.width > 0 ? svgLayer.boundingBox.width : defaultSize
    let intrinsicH = svgLayer.boundingBox.height > 0 ? svgLayer.boundingBox.height : defaultSize
    let rasterW = desiredWidth > 0 ? desiredWidth : intrinsicW
    let rasterH = desiredHeight > 0 ? desiredHeight : intrinsicH

    // Scale the SVG layer to fill the target rect.
    let scaleX = rasterW / intrinsicW
    let scaleY = rasterH / intrinsicH
    svgLayer.setAffineTransform(CGAffineTransform(scaleX: scaleX, y: scaleY))
    svgLayer.frame = CGRect(x: 0, y: 0, width: rasterW, height: rasterH)

    // Render the layer into a UIImage using UIGraphicsImageRenderer.
    let renderer = UIGraphicsImageRenderer(size: CGSize(width: rasterW, height: rasterH))
    let image = renderer.image { ctx in
      svgLayer.render(in: ctx.cgContext)
    }
    return image
  }
}
