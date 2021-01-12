// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2021 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import UIKit

extension UIFont {
  /**
   * Determines the largest possible font size no larger than the receiver that fits `string` to
   * the given `width`. If the string fits using the current font, returns self.
   *
   * - Parameter string: The string to size
   * - Parameter width: The maximum desired width of the string
   * - Returns: A smaller font sized for the text, or self if the receiver is sufficient
   */
  func fitting(string: String, width: CGFloat) -> UIFont {
    let string = string as NSString
    var targetPoints = self.pointSize
    var targetFont = self
    var size = string.size(withAttributes: [.font: self])
    while (size.width > width) {
      targetPoints -= 2;
      targetFont = self.withSize(targetPoints)
      size = string.size(withAttributes: [.font: targetFont])
    }
    return targetFont
  }
}
