// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

private let kMaxAspectRatio = CGFloat(854.0)/CGFloat(480.0)
private let kDefaultNormalShortDimension = CGFloat(320)

func computeCompatibleScaling() -> CGFloat {
  let dims = UIScreen.main.nativeBounds
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

public class ScaleFrameLayout: UIView {

  public enum Mode {
    case Fixed
    case Responsive
  }

  private var _mode = Mode.Responsive
  private var _scale = computeCompatibleScaling()

  public override init(frame: CGRect) {
    super.init(frame: frame)
    clipsToBounds = true
    translatesAutoresizingMaskIntoConstraints = false
    accessibilityIdentifier = "Root View"
  }

  required init?(coder aCoder: NSCoder) {
    super.init(coder: aCoder)
    clipsToBounds = true
    translatesAutoresizingMaskIntoConstraints = false
    clearsContextBeforeDrawing = true
    accessibilityIdentifier = "Root View"
  }

  public var mode: Mode {
    get {
      return _mode
    }
    set(mode) {
      if _mode != mode {
        _mode = mode
        switch _mode {
        case .Fixed:
          // iPhone 6 is 1334x750 with 326 dpi. This is 667x375 at 160dpi (almost).
          // Android MDPI is 480x320 at 160dpi.
          let scale = _scale
          layer.anchorPoint = CGPoint(x: 0.5 / scale, y: 0.5 / scale)
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
