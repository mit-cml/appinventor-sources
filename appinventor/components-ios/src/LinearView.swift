// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

public class LinearViewItem: NSObject {

  @objc public let view: UIView

  @objc public init(_ view: UIView) {
    self.view = view
  }

}

extension UIView {
  @objc func isRTL() -> Bool {
    return UIView.userInterfaceLayoutDirection(for: self.semanticContentAttribute) == UIUserInterfaceLayoutDirection.rightToLeft
  }
}

private class HelperView: UIView {
  open override var intrinsicContentSize: CGSize {
    get {
      return CGSize.zero
    }
  }
}

public class Length: Equatable {
  static let Automatic = Length(-1)
  static let FillParent = Length(-2)

  private let rawValue: Int32
  fileprivate let view: UIView!
  fileprivate var constraint: NSLayoutConstraint? = nil

  public init(_ other: Length) {
    rawValue = other.rawValue
    view = other.view
  }

  private init(_ fixed: Int32) {
    rawValue = fixed
    self.view = nil
  }

  public init(pixels: Int32) {
    rawValue = pixels
    self.view = nil
  }

  public init(percent: Int32, of view: UIView) {
    rawValue = -1000 - percent
    self.view = view
  }

  public static func == (lhs: Length, rhs: Length) -> Bool {
    return lhs.rawValue == rhs.rawValue && lhs.view == rhs.view
  }

  public var isPercent: Bool {
    return view != nil
  }

  public var cgFloat: CGFloat {
    if isPercent {
      return CGFloat(rawValue) / 100.0
    } else {
      return CGFloat(rawValue)
    }
  }
}

let UILayoutPriorityDefaultMedium = (Int(UILayoutPriority.defaultHigh.rawValue + UILayoutPriority.defaultLow.rawValue)) / 2
let TightSizingPriority = UILayoutPriority(10)
let ConstraintPriority = UILayoutPriority(8)
let HuggingPriority = UILayoutPriority(7)
let DefaultSizingPriority = UILayoutPriority(6)
let FillParentHuggingPriority = UILayoutPriority(5)
let AutomaticHuggingPriority = UILayoutPriority.defaultLow  //UILayoutPriority(26)  // Needs to be greater than 25 otherwise intrinsic content size overrides

public class LinearView: UIView {
  fileprivate var _outer = UIStackView()
  fileprivate var _inner = UIStackView()
  fileprivate var _scrollview = UIScrollView()
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _orientation = HVOrientation.vertical
  fileprivate var _scrollable = false
  fileprivate var _items = [LinearViewItem]()
  fileprivate var _head = HelperView()
  fileprivate var _tail = HelperView()
  fileprivate var _innerHead = HelperView()
  fileprivate var _innerTail = HelperView()
  fileprivate var _innerHeadZero: NSLayoutConstraint!
  fileprivate var _innerTailZero: NSLayoutConstraint!
  fileprivate var _outerEqualConstraint: NSLayoutConstraint!
  fileprivate var _innerEqualConstraint: NSLayoutConstraint!
  fileprivate var _equalConstraint: NSLayoutConstraint!
  fileprivate var _backgroundView = UIView()  // View for background color
  fileprivate var _imageView = UIImageView()  // View for background image
  fileprivate var _fillParentView = UIView()
  private var widthConstraints = [UIView:Length]()
  private var widthFillParent = 0
  private var heightConstraints = [UIView:Length]()
  private var heightFillParent = 0
  private var widthFillParentConstraint: NSLayoutConstraint?
  private var heightFillParentConstraint: NSLayoutConstraint?

  override init(frame aRect: CGRect) {
    super.init(frame: aRect)
    setup()
  }

  required public init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setup()
  }

  private func setup() {
    clipsToBounds = true
    translatesAutoresizingMaskIntoConstraints = false
    _outer.translatesAutoresizingMaskIntoConstraints = false
    _inner.translatesAutoresizingMaskIntoConstraints = false
    _scrollview.translatesAutoresizingMaskIntoConstraints = false
    _backgroundView.translatesAutoresizingMaskIntoConstraints = false
    _imageView.translatesAutoresizingMaskIntoConstraints = false
    _fillParentView.translatesAutoresizingMaskIntoConstraints = false
    addSubview(_backgroundView)
    addSubview(_imageView)
    addSubview(_fillParentView)
    addSubview(_outer)
    _backgroundView.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
    _backgroundView.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
    _backgroundView.topAnchor.constraint(equalTo: topAnchor).isActive = true
    _backgroundView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
    _imageView.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
    _imageView.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
    _imageView.topAnchor.constraint(equalTo: topAnchor).isActive = true
    _imageView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
    _imageView.setContentHuggingPriority(FillParentHuggingPriority, for: .horizontal)
    _imageView.setContentHuggingPriority(FillParentHuggingPriority, for: .vertical)
    _imageView.setContentCompressionResistancePriority(FillParentHuggingPriority, for: .horizontal)
    _imageView.setContentCompressionResistancePriority(FillParentHuggingPriority, for: .vertical)
    _outer.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
    _outer.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
    _outer.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
    _outer.topAnchor.constraint(equalTo: topAnchor).isActive = true
    _outer.spacing = 0
    _outer.alignment = .top
    _outer.distribution = .fill
    _outer.axis = .horizontal
    _inner.spacing = 0
    _inner.alignment = .leading
    _inner.distribution = .fill
    _inner.axis = .vertical
    _outer.addArrangedSubview(_head)
    _outer.addArrangedSubview(_inner)
    _outer.addArrangedSubview(_tail)
    _inner.addArrangedSubview(_innerHead)
    _inner.addArrangedSubview(_innerTail)
    updatePositioningConstraints()
    updatePriorities()
  }

  @objc open var scrollEnabled: Bool {
    @objc(isScrollEnabled)
    get {
      return _scrollable
    }
    set(scroll) {
      if _scrollable != scroll {
        _scrollable = scroll
        if _scrollable {
          _scrollview.isScrollEnabled = true
          _outer.removeFromSuperview()
          _scrollview.addSubview(_outer)
          addSubview(_scrollview)
          if _orientation == .horizontal {
            _scrollview.showsHorizontalScrollIndicator = true
            _scrollview.showsVerticalScrollIndicator = false
            _outer.heightAnchor.constraint(equalTo: _scrollview.heightAnchor).isActive = true
            _scrollview.trailingAnchor.constraint(equalTo: _outer.trailingAnchor).isActive = true
            _scrollview.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
          } else if _orientation == .vertical {
            _scrollview.showsVerticalScrollIndicator = true
            _scrollview.showsHorizontalScrollIndicator = false
            _outer.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
            _outer.trailingAnchor.constraint(equalTo: _scrollview.trailingAnchor).isActive = true
            _scrollview.bottomAnchor.constraint(equalTo: _outer.bottomAnchor).isActive = true
            _scrollview.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
          }
          _scrollview.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
          _scrollview.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
          _scrollview.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
          _scrollview.topAnchor.constraint(equalTo: topAnchor).isActive = true
          _outer.leadingAnchor.constraint(equalTo: _scrollview.leadingAnchor).isActive = true
          _outer.topAnchor.constraint(equalTo: _scrollview.topAnchor).isActive = true
          updatePositioningConstraints()
          updatePriorities()
        } else {
          _scrollview.removeFromSuperview()
          _outer.removeFromSuperview()
          addSubview(_outer)
        }
      }
    }
  }

  open var horizontalAlignment: HorizontalGravity {
    get {
      return _horizontalAlign
    }
    set(align) {
      if _horizontalAlign != align {
        _horizontalAlign = align
        updatePriorities()
        updateHorizontalAlignment()
      }
    }
  }

  open var verticalAlignment: VerticalGravity {
    get {
      return _verticalAlign
    }
    set(align) {
      if _verticalAlign != align {
        _verticalAlign = align
        updatePriorities()
        updateVerticalAlignment()
      }
    }
  }

  open var orientation: HVOrientation {
    get {
      return _orientation
    }
    set(orientation) {
      if _orientation != orientation {
        _orientation = orientation
        removeConstraint(_outerEqualConstraint)
        _inner.removeConstraint(_innerEqualConstraint)
        if _orientation == .horizontal {
          _inner.axis = .horizontal
          _outer.axis = .vertical
        } else {
          _inner.axis = .vertical
          _outer.axis = .horizontal
        }
        updatePositioningConstraints()
        updatePriorities()
        updateHorizontalAlignment()
        updateVerticalAlignment()
      }
    }
  }

  @objc open func addItem(_ item: LinearViewItem) {
    _inner.insertSubview(item.view, at: _inner.subviews.count - 1)
    _inner.insertArrangedSubview(item.view, at: _inner.arrangedSubviews.count - 1)
    _items.append(item)
  }

  open func removeItem(_ view: UIView) {
    _inner.removeArrangedSubview(view)
    view.removeFromSuperview()
    _items.removeAll { (item) -> Bool in
      item.view == view
    }
  }

  open func resetView() {
    /* Resets the all the width*/
    widthFillParent = 0
    widthFillParentConstraint?.isActive = false
    widthFillParentConstraint = nil

    /* Resets the all the height*/
    heightFillParent = 0
    heightFillParentConstraint?.isActive = false
    heightFillParentConstraint = nil

    _innerEqualConstraint.isActive = true
    _innerHeadZero.isActive = false
    _innerTailZero.isActive = false

    updatePositioningConstraints()
    updatePriorities()
  }

  open func removeAllItems() {
    for item in _items {
      _inner.removeArrangedSubview(item.view)
      item.view.removeFromSuperview()
    }
    _items.removeAll()
  }

  open func setVisibility(of view: UIView, to visible: Bool) {
    if visible && view.superview == nil {
      var visibleCount = 1  // accounts for _innerHead
      for item in _items {
        if item.view == view {
          NSLog("Inserting view %@ at position %d", view, visibleCount)
          _inner.insertSubview(view, at: visibleCount)
          _inner.insertArrangedSubview(view, at: visibleCount)
          break
        } else if item.view.superview != nil {
          visibleCount += 1
        }
      }
      setNeedsLayout()
      setNeedsUpdateConstraints()
    } else if !visible && view.superview != nil {
      view.removeFromSuperview()
      setNeedsLayout()
      setNeedsUpdateConstraints()
    }
  }

  open var arrangedSubviews: [UIView] {
    return Array<UIView>(_inner.arrangedSubviews[1..<_inner.arrangedSubviews.count-1])
  }

  open func contains(_ item: UIView) -> Bool {
    return _inner.arrangedSubviews.contains(item)
  }

  open override var backgroundColor: UIColor? {
    get {
      return super.backgroundColor
    }
    set(color) {
      _backgroundView.backgroundColor = color
    }
  }

  open var image: UIImage? {
    get {
      return _imageView.image
    }
    set(img) {
      _imageView.image = img
    }
  }

  /**
   * Sets the width of an arranged child of the LinearView.
   *
   * This method is not threadsafe. It should only be called from the UI thread.
   */
  open func setWidth(of view: UIView, to length: Length) {
    // Remove old constraint
    if let oldLength = widthConstraints[view] {
      oldLength.constraint?.isActive = false
      if oldLength == .FillParent {
        widthFillParent -= 1
      }
      if widthFillParent == 0 && orientation == .horizontal {
        widthFillParentConstraint?.isActive = false
        widthFillParentConstraint = nil
        _innerEqualConstraint.isActive = true
        _innerHeadZero.isActive = false
        _innerTailZero.isActive = false
        setNeedsUpdateConstraints()
      }
    }

    if length == .FillParent {
      view.setContentHuggingPriority(FillParentHuggingPriority, for: .horizontal)
    } else {
      view.setContentHuggingPriority(AutomaticHuggingPriority, for: .horizontal)
    }

    // Add new constraint
    if length == .FillParent {
      widthFillParent += 1
      if orientation == .horizontal {
        if widthFillParent == 1 {
          widthFillParentConstraint = _inner.widthAnchor.constraint(equalTo: widthAnchor)
          widthFillParentConstraint?.isActive = true
          _innerEqualConstraint.isActive = false
          _innerHeadZero.isActive = true
          _innerTailZero.isActive = true
          setNeedsUpdateConstraints()
        }
        length.constraint = _fillParentView.widthAnchor.constraint(equalTo: view.widthAnchor)
        length.constraint?.priority = UILayoutPriority.defaultHigh
      } else {
        length.constraint = view.widthAnchor.constraint(equalTo: self.widthAnchor)
      }
    } else if length == .Automatic {
      length.constraint = _inner.widthAnchor.constraint(greaterThanOrEqualTo: view.widthAnchor)
    } else if length.isPercent {
      length.constraint = view.widthAnchor.constraint(equalTo: length.view.widthAnchor, multiplier: length.cgFloat)
    } else {
      length.constraint = view.widthAnchor.constraint(equalToConstant: length.cgFloat)
    }
    length.constraint?.isActive = true
    widthConstraints[view] = length
    invalidateIntrinsicContentSize()
  }

  /**
   * Sets the height of an arranged child of the LinearView.
   *
   * This method is not threadsafe. It should only be called from the UI thread.
   */
  open func setHeight(of view: UIView, to length: Length) {
    // Remove old constraint
    if let oldLength = heightConstraints[view] {
      oldLength.constraint?.isActive = false
      if oldLength == .FillParent {
        heightFillParent -= 1
      }
      if heightFillParent == 0 && orientation == .vertical {
        heightFillParentConstraint?.isActive = false
        heightFillParentConstraint = nil
        _innerEqualConstraint.isActive = true
        _innerHeadZero.isActive = false
        _innerTailZero.isActive = false
        setNeedsUpdateConstraints()
      }
    }

    if length == .FillParent {
      view.setContentHuggingPriority(FillParentHuggingPriority, for: .vertical)
    } else {
      view.setContentHuggingPriority(AutomaticHuggingPriority, for: .vertical)
    }

    // Add new constraint
    if length == .FillParent {
      heightFillParent += 1
      if orientation == .vertical {
        if heightFillParent == 1 {
          heightFillParentConstraint = _inner.heightAnchor.constraint(equalTo: heightAnchor)
          heightFillParentConstraint?.isActive = true
          _innerEqualConstraint.isActive = false
          _innerHeadZero.isActive = true
          _innerTailZero.isActive = true
          setNeedsUpdateConstraints()
        }
        length.constraint = _fillParentView.heightAnchor.constraint(equalTo: view.heightAnchor)
        length.constraint?.priority = UILayoutPriority.defaultHigh
      } else {
        length.constraint = view.heightAnchor.constraint(equalTo: self.heightAnchor)
      }
    } else if length == .Automatic {
      length.constraint = _inner.heightAnchor.constraint(greaterThanOrEqualTo: view.heightAnchor)
    } else if length.isPercent {
      length.constraint = view.heightAnchor.constraint(equalTo: length.view.heightAnchor, multiplier: length.cgFloat)
    } else {
      length.constraint = view.heightAnchor.constraint(equalToConstant: length.cgFloat)
    }
    length.constraint?.isActive = true
    heightConstraints[view] = length
    invalidateIntrinsicContentSize()
  }

  open override func updateConstraints() {
    super.updateConstraints()
    updatePriorities()
  }

  open override var intrinsicContentSize: CGSize {
    if _items.count == 0 {
      return CGSize(width: 100, height: 100)
    } else {
      var max = CGFloat(0.0), sum = CGFloat(0.0)
      if orientation == .horizontal {
        _items.forEach { item in
          if item.view.intrinsicContentSize.height > max {
            max = item.view.intrinsicContentSize.height
          }
          sum += item.view.intrinsicContentSize.width
        }
        return CGSize(width: sum, height: max)
      } else {
        _items.forEach { item in
          if item.view.intrinsicContentSize.width > max {
            max = item.view.intrinsicContentSize.width
          }
          sum += item.view.intrinsicContentSize.height
        }
        return CGSize(width: max, height: sum)
      }
    }
  }

  // MARK: Private Implementation

  private func updateHorizontalAlignment() {
    if _orientation == .vertical {
      switch _horizontalAlign {
      case .left:
        _inner.alignment = .leading
      case .center:
        _inner.alignment = .center
      case .right:
        _inner.alignment = .trailing
      }
    }
  }

  private func updateVerticalAlignment() {
    if _orientation == .horizontal {
      switch _verticalAlign {
      case .top:
        _inner.alignment = .top
      case .center:
        _inner.alignment = .center
      case .bottom:
        _inner.alignment = .bottom
      }
    }
  }

  private var horizontalHead: UIView {
    get {
      return _orientation == .horizontal ? _innerHead : _head
    }
  }

  private var horizontalTail: UIView {
    get {
      return _orientation == .horizontal ? _innerTail : _tail
    }
  }

  private var verticalHead: UIView {
    get {
      return _orientation == .vertical ? _innerHead : _head
    }
  }

  private var verticalTail: UIView {
    get {
      return _orientation == .vertical ? _innerTail : _tail
    }
  }

  private func updatePositioningConstraints() {
    if _outerEqualConstraint != nil {
      removeConstraint(_outerEqualConstraint)
      _inner.removeConstraint(_innerEqualConstraint)
    }
    if _orientation == .horizontal {
      _outerEqualConstraint = _head.heightAnchor.constraint(equalTo: _tail.heightAnchor)
      _innerEqualConstraint = _innerHead.widthAnchor.constraint(equalTo: _innerTail.widthAnchor)
      _equalConstraint = widthAnchor.constraint(equalTo: _inner.widthAnchor)
      _innerHeadZero = _innerHead.widthAnchor.constraint(equalToConstant: 0.0)
      _innerTailZero = _innerTail.widthAnchor.constraint(equalToConstant: 0.0)
    } else {
      _outerEqualConstraint = _head.widthAnchor.constraint(equalTo: _tail.widthAnchor)
      _innerEqualConstraint = _innerHead.heightAnchor.constraint(equalTo: _innerTail.heightAnchor)
      _equalConstraint = heightAnchor.constraint(equalTo: _inner.heightAnchor)
      _innerHeadZero = _innerHead.heightAnchor.constraint(equalToConstant: 0.0)
      _innerTailZero = _innerTail.heightAnchor.constraint(equalToConstant: 0.0)
    }
    _outerEqualConstraint.priority = ConstraintPriority
    _outerEqualConstraint.identifier = "Outer equality constraint"
    _innerEqualConstraint.priority = ConstraintPriority
    _innerEqualConstraint.identifier = "Inner equality constraint"
    _innerHeadZero.identifier = "Inner head zero"
    _innerTailZero.identifier = "Inner tail zero"
    if _scrollable == false {
      addConstraint(_equalConstraint)
    }
    addConstraint(_outerEqualConstraint)
    _inner.addConstraint(_innerEqualConstraint)
  }

  private func updatePriorities() {
    // Horizontal Head has the following fixed properties
    horizontalHead.setContentCompressionResistancePriority(DefaultSizingPriority, for: .horizontal)
    horizontalHead.setContentCompressionResistancePriority(DefaultSizingPriority, for: .vertical)
    horizontalHead.setContentHuggingPriority(DefaultSizingPriority, for: .vertical)

    // Horizontal Tail has the following fixed properties
    horizontalTail.setContentCompressionResistancePriority(DefaultSizingPriority, for: .horizontal)
    horizontalTail.setContentCompressionResistancePriority(DefaultSizingPriority, for: .vertical)
    horizontalTail.setContentHuggingPriority(DefaultSizingPriority, for: .vertical)

    // Vertical Head has the following fixed properties
    verticalHead.setContentCompressionResistancePriority(DefaultSizingPriority, for: .vertical)
    verticalHead.setContentCompressionResistancePriority(DefaultSizingPriority, for: .horizontal)
    verticalHead.setContentHuggingPriority(DefaultSizingPriority, for: .horizontal)

    // Vertical Tail has the following fixed properties
    verticalTail.setContentCompressionResistancePriority(DefaultSizingPriority, for: .vertical)
    verticalTail.setContentCompressionResistancePriority(DefaultSizingPriority, for: .horizontal)
    verticalTail.setContentHuggingPriority(DefaultSizingPriority, for: .horizontal)

    // Dynamic horizontal control
    horizontalHead.setContentHuggingPriority(DefaultSizingPriority, for: .horizontal)
    horizontalTail.setContentHuggingPriority(DefaultSizingPriority, for: .horizontal)
    if widthFillParent == 0 {
      switch _horizontalAlign {
      case .left:
        horizontalHead.setContentHuggingPriority(TightSizingPriority, for: .horizontal)
      case .center:
        break
      case .right:
        horizontalTail.setContentHuggingPriority(TightSizingPriority, for: .horizontal)
      }
    }

    // Dynamic vertical control
    verticalHead.setContentHuggingPriority(DefaultSizingPriority, for: .vertical)
    verticalTail.setContentHuggingPriority(DefaultSizingPriority, for: .vertical)
    if heightFillParent == 0 {
      switch _verticalAlign {
      case .top:
        verticalHead.setContentHuggingPriority(TightSizingPriority, for: .vertical)
      case .center:
        break
      case .bottom:
        verticalTail.setContentHuggingPriority(TightSizingPriority, for: .vertical)
      }
    }
  }
}
