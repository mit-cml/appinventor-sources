// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.

import Foundation

private let kMaxAspectRatio = CGFloat(854.0)/CGFloat(480.0)
private let kDefaultNormalShortDimension = CGFloat(320)

func computeCompatibleScaling() -> CGFloat {
  let dims = UIScreen.main.nativeBounds
  print("nativeBounds = \(dims)")
  let density = UIScreen.main.nativeScale
  let (shortSize, longSize) = (dims.width, dims.height)
  let newShortSize = Int(kDefaultNormalShortDimension * density + 0.5)
  let aspect = min(kMaxAspectRatio, longSize/shortSize)
  let newLongSize = Int(CGFloat(newShortSize) * aspect + 0.5)
  let (newWidth, newHeight) = (newShortSize, newLongSize)
  let sw = dims.width/CGFloat(newWidth)
  let sh = dims.height/CGFloat(newHeight)
  let scale = min(sw, sh)
  return max(1.0, scale)
}

class ScaleFrameLayout: UIView {

  enum Mode {
    case Fixed
    case Responsive
  }

  private var _mode = Mode.Responsive
  private var _scale = computeCompatibleScaling()

  public override init(frame: CGRect) {
    super.init(frame: frame)
    clipsToBounds = true
    accessibilityIdentifier = "Root View"
  }

  required init?(coder aCoder: NSCoder) {
    super.init(coder: aCoder)
    clipsToBounds = true
    clearsContextBeforeDrawing = true
    accessibilityIdentifier = "Root View"
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
          let scale = _scale
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
        return _scale
      case .Responsive:
        return 1.0
      }
    }
  }
}
