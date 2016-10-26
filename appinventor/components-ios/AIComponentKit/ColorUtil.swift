//
//  ColorUtil.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/20/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

func argbToColor( _ argb: Int32) -> UIColor {
  var argb = argb
  var r: Float = 0.0, g: Float = 0.0, b: Float = 0.0, a: Float = 0.0
  b = Float(argb & 0xFF) / 255.0
  argb >>= 8
  g = Float(argb & 0xFF) / 255.0
  argb >>= 8
  r = Float(argb & 0xFF) / 255.0
  argb >>= 8
  a = Float(argb & 0xFF) / 255.0
  return UIColor(colorLiteralRed: r, green: g, blue: b, alpha: a)
}

func colorToArgb(_ color: UIColor) -> Int32 {
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
