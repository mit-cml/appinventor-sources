// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

func getFontTrait(font: UIFont?, trait: UIFontDescriptor.SymbolicTraits, shouldSet: Bool) -> UIFont? {
  var fontDescriptor: UIFontDescriptor?
  if let tempDescriptor = font?.fontDescriptor {
    var fontOps = tempDescriptor.symbolicTraits
    if shouldSet {
      fontOps.insert(trait)
    } else {
      fontOps.remove(trait)
    }
    fontDescriptor = tempDescriptor.withSymbolicTraits(fontOps)
  }
  if let size = font?.pointSize, let descriptor = fontDescriptor {
    return UIFont(descriptor: descriptor, size: size)
  } else {
    return font
  }
}

func getFontTypeface(font: UIFont?, typeFace: Typeface) -> UIFont? {
  if let descriptor = font?.fontDescriptor, let size = font?.pointSize {
    var tempFont: UIFont?
    switch typeFace {
    case .normal, .sansSerif:
      tempFont = UIFont.systemFont(ofSize: size)
    case .monospace:
      tempFont = UIFont(name: "Menlo", size: size)
    case .serif:
      tempFont = UIFont(name: "Cochin", size: size)
    }
    if descriptor.symbolicTraits.contains(.traitBold) {
      tempFont = getFontTrait(font: tempFont, trait: .traitBold, shouldSet: true)
    }
    if descriptor.symbolicTraits.contains(.traitItalic) {
      tempFont = getFontTrait(font: tempFont, trait: .traitItalic, shouldSet: true)
    }
    return tempFont ?? font
  } else {
    return font
  }
}

func getFontSize(font: UIFont?, size: Float32) -> UIFont? {
  if let descriptor = font?.fontDescriptor {
    return UIFont(descriptor: descriptor, size: CGFloat(size))
  } else {
    return font
  }
}

