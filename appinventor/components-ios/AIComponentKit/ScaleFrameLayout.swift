// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

class ScaleFrameLayout: UIView {

  enum Mode {
    case Fixed
    case Responsive
  }

  private var _mode = Mode.Responsive

  public override init(frame: CGRect) {
    super.init(frame: frame)
  }

  required init?(coder aCoder: NSCoder) {
    super.init(coder: aCoder)
  }

  public var mode: Mode {
    get {
      return _mode
    }
    set(mode) {
      print("Screen native bounds = \(UIScreen.main.nativeBounds)")
      print("Screen bounds = \(UIScreen.main.bounds)")
      if _mode != mode {
        _mode = mode
        switch _mode {
        case .Fixed:
          // iPhone 6 is 1334x750 with 326 dpi. This is 667x375 at 160dpi (almost).
          // Android MDPI is 480x320 at 160dpi.
          print("anchorPoint = \(layer.anchorPoint)")
          print("center = \(center)")
          layer.anchorPoint = CGPoint(x: 0, y: 0)
          center = layer.anchorPoint
          let scale = CGFloat(UIScreen.main.bounds.width / 320.0)
          self.transform = CGAffineTransform(scaleX: scale, y: scale)
          break
        case .Responsive:
          layer.anchorPoint = CGPoint(x: 0.5, y: 0.5)
          self.transform = CGAffineTransform.init(scaleX: 1.0, y: 1.0)
          break
        }
        setNeedsLayout()
      }
    }
  }

  public var scale: CGFloat {
    get {
      switch _mode {
      case .Fixed:
        return 375.0/320.0
      case .Responsive:
        return 1.0
      }
    }
  }
}
