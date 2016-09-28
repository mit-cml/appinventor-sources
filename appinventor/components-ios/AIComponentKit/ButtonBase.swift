//
//  ButtonBase.swift
//  AIComponentKit
//
//  Created by Evan Patton on 9/21/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForButton: AbstractMethodsForViewComponent {
  
}

public class ButtonBase: ViewComponent {
  private static let kRoundedCornersRadius: Float = 10.0
  private static let kRoundedCornersArray = [kRoundedCornersRadius, kRoundedCornersRadius]
  private static let kShapedDefaultBackgroundColor = Color.lightGray
  private final var _view: UIButton
  private final var _delegate: AbstractMethodsForButton
  private var _textAlignment = Alignment.center
  private var _backgroundColor = Color.DEFAULT
  private var _fontTypeface = Typeface.normal
  private var _bold = false
  private var _showFeedback = true
  private var _italic = false
  private var _textColor = Color.DEFAULT
  private var _shape = ButtonShape.normal
  private var _image: UIImage!

  public init(parent: ComponentContainer, delegate: AbstractMethodsForButton) {
    self._view = UIButton(type: UIButtonType.custom)
    self._delegate = delegate
    super.init(parent: parent, delegate: delegate)
  }
}
