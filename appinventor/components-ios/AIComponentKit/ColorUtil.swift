// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public func argbToColor( _ argb: Int32) -> UIColor {
  var argb = argb
  var r: Float = 0.0, g: Float = 0.0, b: Float = 0.0, a: Float = 0.0
  b = Float(argb & 0xFF) / 255.0
  argb >>= 8
  g = Float(argb & 0xFF) / 255.0
  argb >>= 8
  r = Float(argb & 0xFF) / 255.0
  argb >>= 8
  a = Float(argb & 0xFF) / 255.0
  return UIColor(red: CGFloat(r), green: CGFloat(g), blue: CGFloat(b), alpha: CGFloat(a))
}

public func argbToColor(_ argb: UInt32) -> UIColor {
  return argbToColor(Int32(bitPattern: argb))
}

public func colorToArgb(_ color: UIColor) -> Int32 {
  var argb: Int32 = 0
  var r: CGFloat = 0.0, g: CGFloat = 0.0, b: CGFloat = 0.0, a: CGFloat = 0.0
  color.getRed(&r, green: &g, blue: &b, alpha: &a)
  argb |= ((Int32)(255 * a))
  argb <<= 8
  argb |= ((Int32)(255 * r))
  argb <<= 8
  argb |= ((Int32)(255 * g))
  argb <<= 8
  argb |= ((Int32)(255 * b))
  return argb
}

public func rgbArrayToInt32(_ rgb: [Int]) -> Int32 {
  let a = rgb.count == 4 ? rgb[0] : 0xFF
  let rgb = rgb.suffix(from: 1)
  let r = rgb[0], g = rgb[1], b = rgb[2]
  var argb: Int32 = 0
  argb |= ((Int32)(a & 0xFF))
  argb <<= 8
  argb |= ((Int32)(r & 0xFF))
  argb <<= 8
  argb |= ((Int32)(g & 0xFF))
  argb <<= 8
  argb |= ((Int32)(b & 0xFF))
  return argb
}

public func preferredBackgroundColor(_ form: Form?) -> UIColor {
  guard let form = form else {
    if #available(iOS 13.0, *) {
      return UIColor.systemBackground
    } else {
      return UIColor.white
    }
  }
  if #available(iOS 13.0, *), form.Theme == "DeviceDefault" {
    return UIColor.systemBackground
  } else if form.Theme == "Dark" {
    return UIColor.black
  } else {
    return UIColor.white
  }
}

public func preferredTextColor(_ form: Form?) -> UIColor {
  guard let form = form else {
    if #available(iOS 13.0, *) {
      return UIColor.label
    } else {
      return UIColor.darkText
    }
  }
  if #available(iOS 13.0, *), form.Theme == "DeviceDefault" {
    return UIColor.label
  } else if form.Theme == "Dark" {
    return UIColor.white
  } else {
    return UIColor.black
  }
}
