// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

func getFontTrait(font: UIFont?, trait: UIFontDescriptorSymbolicTraits, shouldSet: Bool) -> UIFont? {
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
