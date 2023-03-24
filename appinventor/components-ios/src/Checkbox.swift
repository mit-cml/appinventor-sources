// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import SwiftSVG

private let CHECKBOX_HORIZONTAL_MARGIN: CGFloat = 17 * 30.0 / 55.0
private let CHECKBOX_VERTICAL_MARGIN: CGFloat = 19.0 * 30.0 / 55.0
private let CHECKBOX_SIZE: CGFloat = 30.0
private let CHECKBOX_TEXT_MARGIN_BOTTOM: CGFloat = 5 * 30.0 / 55.0
private let CHECKBOX_CHECKED_COLOR = UIColor(red: 0, green: 150.0 / 255.0, blue: 136.0 / 255.0, alpha: 1.0).cgColor

private var checked: SVGLayer!
private var unchecked: SVGLayer!
private var waiting = [(SVGLayer, SVGLayer) -> Void]()

/**
 * loadCheckbox(completion:) loads the image assets for the CheckBox. SwiftSVG
 * caches the resulting layer internally, so if we want to reuse these for more
 * than one checkbox we need to copy them. When the designer first loads, it
 * is possible that many checkboxes will need access to these layers. This
 * function manages multiple requests for the content and calls completion
 * handlers once the data is available.
 */
func loadCheckBox(completion: @escaping (SVGLayer, SVGLayer) -> Void) {
  if checked != nil && unchecked != nil {
    completion(checked.svgLayerCopy!, unchecked.svgLayerCopy!)
  } else {
    waiting.append(completion)
    if waiting.count == 1 {
      if let boxPath = Bundle(for: CheckBox.self).url(forResource: "checked", withExtension: "svg"),
         let unboxPath = Bundle(for: CheckBox.self).url(forResource: "unchecked", withExtension: "svg") {
        CALayer(SVGURL: boxPath, completion: { (layer) in
          checked = layer
          if unchecked != nil {
            DispatchQueue.main.async {
              for completion in waiting {
                completion(checked.svgLayerCopy!, unchecked.svgLayerCopy!)
              }
              waiting.removeAll()
            }
          }
        })
        CALayer(SVGURL: unboxPath, completion: { (layer) in
          unchecked = layer
          if checked != nil {
            DispatchQueue.main.async {
              for completion in waiting {
                completion(checked.svgLayerCopy!, unchecked.svgLayerCopy!)
              }
              waiting.removeAll()
            }
          }
        })
      }
    }
  }
}

/**
 * CheckBoxView is a custom UIView to handle the logic of the CheckBox. It is
 * responsible for updating the state of the button and its appearance as a
 * function of user or block interaction with the component.
 */
public class CheckBoxView: UIView {
  fileprivate var _button = UIButton(frame: .zero)
  fileprivate var _text = UILabel()
  fileprivate var _checked: CAShapeLayer!
  fileprivate var _unchecked: CAShapeLayer!
  fileprivate var _layersLoaded = false

  public init() {
    super.init(frame: .zero)
    setupCheckbox()
  }

  func setupCheckbox() {
    loadCheckBox { (checked, unchecked) in
      self._checked = checked
      self._unchecked = unchecked
      // TODO(ewpatton): Figure out why these constants work...
      self._checked.position = CGPoint(x: 9.5, y: 10.5)
      self._unchecked.position = CGPoint(x: 9.5, y: 10.5)
      self._button.layer.addSublayer(self.Checked ? self._checked : self._unchecked)
      self.updateColor()
      self._layersLoaded = true
    }

    clipsToBounds = true
    translatesAutoresizingMaskIntoConstraints = false
    _button.translatesAutoresizingMaskIntoConstraints = false
    _text.translatesAutoresizingMaskIntoConstraints = false
    _text.numberOfLines = 0
    _text.setContentHuggingPriority(.defaultHigh, for: .horizontal)

    addSubview(_button)
    addSubview(_text)

    // Configure checkbox button constraints
    _button.widthAnchor.constraint(equalToConstant: CHECKBOX_SIZE).isActive = true
    _button.heightAnchor.constraint(equalToConstant: CHECKBOX_SIZE).isActive = true
    _button.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
    _button.topAnchor.constraint(greaterThanOrEqualTo: topAnchor,
        constant: CHECKBOX_VERTICAL_MARGIN).isActive = true
    bottomAnchor.constraint(greaterThanOrEqualTo: _button.bottomAnchor,
        constant: CHECKBOX_VERTICAL_MARGIN).isActive = true
    _button.leadingAnchor.constraint(equalTo: leadingAnchor,
        constant: CHECKBOX_HORIZONTAL_MARGIN).isActive = true

    // Configure text constraints
    _text.leadingAnchor.constraint(equalTo: _button.trailingAnchor,
        constant: CHECKBOX_HORIZONTAL_MARGIN).isActive = true
    _text.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
    _text.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
    _text.topAnchor.constraint(greaterThanOrEqualTo: topAnchor,
        constant: CHECKBOX_TEXT_MARGIN_BOTTOM).isActive = true
    bottomAnchor.constraint(greaterThanOrEqualTo: _text.bottomAnchor,
        constant: CHECKBOX_TEXT_MARGIN_BOTTOM).isActive = true
    _button.addTarget(self, action: #selector(changeSwitch), for: .touchUpInside)
    addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(changeSwitch)))
  }

  public var Checked: Bool = false {
    didSet {
      guard _layersLoaded else {
        return
      }
      if oldValue != Checked {
        if oldValue {
          _button.layer.addSublayer(_unchecked)
          _checked.removeFromSuperlayer()
        } else {
          _button.layer.addSublayer(_checked)
          _unchecked.removeFromSuperlayer()
        }
        updateColor()
      }
    }
  }

  public var Enabled: Bool = true {
    didSet {
      _button.isEnabled = Enabled
      if oldValue != Enabled {
        updateColor()
      }
    }
  }

  private func updateColor() {
    if let layer = Checked ? _checked : _unchecked {
      if !Enabled {
        if #available(iOS 11.0, *) {
          layer.fillColor = UIColor(named: "CheckBoxDisabled", in: Bundle(for: CheckBox.self),
                                    compatibleWith: _button.traitCollection)?.cgColor
        } else {
          // Fallback on earlier versions
          layer.fillColor = UIColor.black.withAlphaComponent(38.0/255.0).cgColor
        }
      } else if Checked {
        layer.fillColor = CHECKBOX_CHECKED_COLOR
      } else {
        if #available(iOS 11.0, *) {
          layer.fillColor = UIColor(named: "CheckBoxEnabled", in: Bundle(for: CheckBox.self),
                                    compatibleWith: _button.traitCollection)?.cgColor
        } else {
          // Fallback on earlier versions
          layer.fillColor = UIColor.black.withAlphaComponent(138.0/255.0).cgColor
        }
      }
      setNeedsDisplay()
    }
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setupCheckbox()
  }

  // We need to override intrinsicContentSize to allow for automatic and
  // fill parent sizing
  open override var intrinsicContentSize: CGSize {
    let textHeight = _text.intrinsicContentSize.height + 2.0 * CHECKBOX_TEXT_MARGIN_BOTTOM
    let checkboxHeight = CHECKBOX_SIZE + 2.0 * CHECKBOX_VERTICAL_MARGIN
    let width = _text.intrinsicContentSize.width + 2.0 * (CHECKBOX_HORIZONTAL_MARGIN + CHECKBOX_SIZE)
    return CGSize(width: width, height: max(textHeight, checkboxHeight))
  }

  class public override var requiresConstraintBasedLayout: Bool {
    return true
  }

  // Called when dark mode is enabled/disabled (could be time-of-day dependent)
  override open func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
    updateColor()
  }

  public var Text: String {
    get {
      return _text.text ?? ""
    }
    set {
      _text.text = newValue
    }
  }

  @objc private func changeSwitch() {
    if Enabled {
      Checked = !Checked
    }
  }
}

public class CheckBox: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate var _view = CheckBoxView()
  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _bold = false
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _italic = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _view._button.addTarget(self, action: #selector(changeSwitch), for: .touchUpInside)
    _view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(changeSwitch)))
    _view._text.textColor = preferredTextColor(parent.form)
    parent.add(self)
    Checked = false
    Enabled = true
    FontSize = 14.0
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  @objc open var BackgroundColor: Int32 {
    get {
      return _backgroundColor
    }
    set(argb) {
      _backgroundColor = argb
      _view.backgroundColor = argbToColor(argb)
    }
  }

  @objc open var Checked: Bool {
    get {
      return _view.Checked
    }
    set(checked) {
      _view.Checked = checked
    }
  }

  @objc open var Enabled: Bool {
    get {
      return _view.Enabled
    }
    set(enabled) {
      _view.Enabled = enabled
    }
  }

  @objc open var FontBold: Bool {
    get {
      return _bold
    }
    set(shouldBold) {
      _bold = shouldBold
      _view._text.font = getFontTrait(font: _view._text.font, trait: .traitBold, shouldSet: shouldBold)
    }
  }

  @objc open var FontItalic: Bool {
    get {
      return _italic
    }
    set(shouldItalic) {
      _italic = shouldItalic
      _view._text.font = getFontTrait(font: _view._text.font, trait: .traitItalic, shouldSet: shouldItalic)
    }
  }

  @objc open var FontSize: Float32 {
    get {
      return Float32(_view._text.font.pointSize)
    }
    set(size) {
      _view._text.font = UIFont(descriptor: _view._text.font.fontDescriptor, size: CGFloat(size))
    }
  }

  @objc open var FontTypeface: Int32 {
    get {
      return _fontTypeface.rawValue
    }
    set(newTypeFace) {
      if newTypeFace != _fontTypeface.rawValue {
        if let type = Typeface(rawValue: Int32(newTypeFace)) {
          _fontTypeface = type
          _view._text.font = getFontTypeface(font: _view._text.font, typeFace: type)
        }
      }
    }
  }

  @objc open var Text: String {
    get {
      return _view._text.text ?? ""
    }
    set(newText) {
      _view._text.text = newText
    }
  }

  @objc open var TextColor: Int32 {
    get {
      return colorToArgb(_view._text.textColor)
    }
    set(color) {
      _view._text.textColor = argbToColor(color)
    }
  }

  @objc fileprivate func changeSwitch(gesture: UITapGestureRecognizer) {
    if _view.Enabled {
      Changed()
    }
  }

  @objc open func Changed() {
    EventDispatcher.dispatchEvent(of: self, called: "Changed")
  }

  @objc open func GotFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "GotFocus")
  }
  
  @objc open func LostFocus() {
    EventDispatcher.dispatchEvent(of: self, called: "LostFocus")
  }
}
