// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation
import SwiftSVG

public class CheckBox: ViewComponent, AbstractMethodsForViewComponent {
  fileprivate var _view = UIView()
  fileprivate var _button = UIButton(frame: CGRect(x: 0, y: 0, width: 30, height: 30))
  fileprivate var _text = UILabel()

  fileprivate var _backgroundColor = Int32(bitPattern: Color.default.rawValue)
  fileprivate var _bold = false
  fileprivate var _fontTypeface = Typeface.normal
  fileprivate var _italic = false
  fileprivate var _textColor = Int32(bitPattern: Color.default.rawValue)

  public override init(_ parent: ComponentContainer) {
    Checked = false
    super.init(parent)
    super.setDelegate(self)
    _button.addTarget(self, action: #selector(Changed), for: .touchUpInside)
    _view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(changeSwitch)))
    parent.add(self)
    
    setupViews()
    Height = 32
  }
  
  private func setupViews() {
    _view.translatesAutoresizingMaskIntoConstraints = false
    _button.translatesAutoresizingMaskIntoConstraints = false
    _text.translatesAutoresizingMaskIntoConstraints = false
    _text.numberOfLines = 0
    
    _view.addSubview(_button)
    _view.addSubview(_text)
    
    _button.centerYAnchor.constraint(equalTo: _view.centerYAnchor).isActive = true
    _button.leftAnchor.constraint(equalTo: _view.leftAnchor, constant: 15).isActive = true
    _button.widthAnchor.constraint(lessThanOrEqualToConstant: 15).isActive = true
    _button.heightAnchor.constraint(lessThanOrEqualToConstant: 15).isActive = true
    
    _text.centerYAnchor.constraint(equalTo: _view.centerYAnchor).isActive = true
    _text.leftAnchor.constraint(equalTo: _button.rightAnchor, constant: 10).isActive = true
    _text.rightAnchor.constraint(equalTo: _view.rightAnchor).isActive = true
    _text.topAnchor.constraint(greaterThanOrEqualTo: _view.topAnchor).isActive = true
    _text.bottomAnchor.constraint(lessThanOrEqualTo: _view.bottomAnchor).isActive = true
    _text.heightAnchor.constraint(greaterThanOrEqualTo: _button.heightAnchor).isActive = true
    FontSize = 14.0
    _view.clipsToBounds = true
    renderCheck()
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
    didSet {
      if oldValue != Checked {
        renderCheck()
        Changed()
      }
    }
  }

  @objc open var Enabled: Bool {
    get {
      return _button.isEnabled
    }
    set(enabled) {
      if _button.isEnabled != enabled {
        _button.isEnabled = enabled
        renderCheck()
      }
    }
  }

  fileprivate func renderCheck() {
    _button.removeAllViews()
    if let boxPath = Bundle(for: CheckBox.self).url(forResource: (Checked ? "checked": "unchecked"), withExtension: "svg") {
      let checkBox = UIView(SVGURL: boxPath) { (layer) in
        if !self.Enabled {
          layer.fillColor = UIColor(red: 0, green: 0, blue: 0, alpha: 36.0 / 255.0).cgColor
        }
        else if self.Checked {
          layer.fillColor = UIColor(red: 0, green: 150.0 / 255.0, blue: 136.0 / 255.0, alpha: 1.0).cgColor
        } else {
          layer.fillColor = UIColor(red: 0, green: 0, blue: 0, alpha: 138.0 /
            255.0).cgColor
        }
      }
      _button.addSubview(checkBox)
    }
  }

  @objc open var FontBold: Bool {
    get {
      return _bold
    }
    set(shouldBold) {
      _bold = shouldBold
      _text.font = getFontTrait(font: _text.font, trait: .traitBold, shouldSet: shouldBold)
    }
  }

  @objc open var FontItalic: Bool {
    get {
      return _italic
    }
    set(shouldItalic) {
      _italic = shouldItalic
      _text.font = getFontTrait(font: _text.font, trait: .traitItalic, shouldSet: shouldItalic)
    }
  }

  @objc open var FontSize: Float32 {
    get {
      return Float32(_text.font.pointSize)
    }
    set(size) {
      _text.font = UIFont(descriptor: _text.font.fontDescriptor, size: CGFloat(size))
    }
  }

  @objc open var FontTypeface: Int32 {
    get {
      return _fontTypeface.rawValue
    }
    set(newTypeface) {

    }
  }

  @objc open var Text: String {
    get {
      return _text.text ?? ""
    }
    set(newText) {
      _text.text = newText
    }
  }

  @objc open var TextColor: Int32 {
    get {
      return colorToArgb(_text.textColor)
    }
    set(color) {
      _text.textColor = argbToColor(color)
    }
  }

  @objc fileprivate func changeSwitch(gesture: UITapGestureRecognizer) {
    if _button.isEnabled {
      Checked = !Checked
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
