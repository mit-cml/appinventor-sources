// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

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

let UILayoutPriorityDefaultMedium = (Int(UILayoutPriority.defaultHigh.rawValue + UILayoutPriority.defaultLow.rawValue)) / 2

public class LinearView: UIView {
  fileprivate var _container = UIView()
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _orientation = HVOrientation.vertical
  fileprivate var _items = [LinearViewItem]()
  fileprivate var _hConstraints = [NSLayoutConstraint]()
  fileprivate var _vConstraints = [NSLayoutConstraint]()
  fileprivate var _verticalConstraint: NSLayoutConstraint!
  fileprivate var _horizontalConstraint: NSLayoutConstraint!
  fileprivate var _axisSizeConstraint: NSLayoutConstraint!

  override init(frame aRect: CGRect) {
    super.init(frame: aRect)
    setup()
  }
  
  required public init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setup()
  }

  private func setup() {
    translatesAutoresizingMaskIntoConstraints = false
    _container.translatesAutoresizingMaskIntoConstraints = false
//    _container.isScrollEnabled = false
//    _container.showsVerticalScrollIndicator = false
//    _container.showsHorizontalScrollIndicator = false
//    _container.isDirectionalLockEnabled = true
    addSubview(_container)
    let width = widthAnchor.constraint(equalToConstant: CGFloat(kEmptyHVArrangementWidth))
    width.priority = UILayoutPriority.defaultLow
    addConstraint(width)
    let height = heightAnchor.constraint(equalToConstant: CGFloat(kEmptyHVArrangementHeight))
    height.priority = UILayoutPriority.defaultLow
    addConstraint(height)
    _verticalConstraint = topAnchor.constraint(equalTo: _container.topAnchor)
    _horizontalConstraint = leadingAnchor.constraint(equalTo: _container.leadingAnchor)
    _axisSizeConstraint = widthAnchor.constraint(equalTo: _container.widthAnchor)
    addConstraint(_verticalConstraint)
    addConstraint(_horizontalConstraint)
    addConstraint(_axisSizeConstraint)
//    addConstraint(widthAnchor.constraint(lessThanOrEqualTo: _container.widthAnchor))
//    addConstraint(heightAnchor.constraint(lessThanOrEqualTo: _container.heightAnchor))
    addConstraint(_container.leadingAnchor.constraint(greaterThanOrEqualTo: leadingAnchor))
    addConstraint(_container.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor))
    addConstraint(_container.topAnchor.constraint(greaterThanOrEqualTo: topAnchor))
    addConstraint(_container.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor))
  }

  @objc open var isScrollEnabled: Bool {
    get {
      return false//_container.isScrollEnabled
    }
    set(scroll) {
//      _container.isScrollEnabled = scroll
//      _container.showsVerticalScrollIndicator = scroll && _orientation == .vertical
//      _container.showsHorizontalScrollIndicator = scroll && _orientation == .horizontal
    }
  }

  open var horizontalAlignment: HorizontalGravity {
    get {
      return _horizontalAlign
    }
    set(align) {
      _horizontalAlign = align
      updateHorizontalConstraints()
    }
  }

  open var verticalAlignment: VerticalGravity {
    get {
      return _verticalAlign
    }
    set(align) {
      _verticalAlign = align
      updateVerticalConstraints()
    }
  }

  open var orientation: HVOrientation {
    get {
      return _orientation
    }
    set(orientation) {
      _orientation = orientation
//      removeConstraint(_axisSizeConstraint)
//      if orientation == .horizontal {
//        _axisSizeConstraint = heightAnchor.constraint(equalTo: _container.heightAnchor)
//      } else {
//        _axisSizeConstraint = widthAnchor.constraint(equalTo: _container.widthAnchor)
//      }
//      addConstraint(_axisSizeConstraint)
      updateHorizontalConstraints()
      updateVerticalConstraints()
      setNeedsLayout()
      _container.setNeedsLayout()
    }
  }

  @objc open func addItem(_ item: LinearViewItem) {
    _container.addSubview(item.view)
    addHorizontalConstraint(for: item.view)
    addVerticalConstraint(for: item.view)
    _items.append(item)
  }

  open  override var backgroundColor: UIColor? {
    get {
      return super.backgroundColor
    }
    set(color) {
      super.backgroundColor = color
      _container.backgroundColor = color
    }
  }

  // MARK: Private implementation

  private func addHorizontalConstraint(for view: UIView) {
    if _orientation == .horizontal {
      if let trailingConstraint = _hConstraints.popLast() {
        // cleans up constraint between scroll view and previous "last" component
        _container.removeConstraint(trailingConstraint)
      }

      var constraint = view.leadingAnchor.constraint(equalTo: _items.count == 0 ? _container.leadingAnchor : (_items.last?.view.trailingAnchor)!)
      constraint.priority = UILayoutPriority.required
      _hConstraints.append(constraint)
      _container.addConstraint(constraint)

      // the following constraint must always be last
      constraint = _container.trailingAnchor.constraint(greaterThanOrEqualTo: view.trailingAnchor)
      constraint.priority = UILayoutPriority.required
      _hConstraints.append(constraint)
      _container.addConstraint(constraint)
    } else {
      var constraint: NSLayoutConstraint!
      switch _horizontalAlign {
      case .left:
        constraint = _container.leadingAnchor.constraint(equalTo: view.leadingAnchor)
        break;
      case .center:
        constraint = _container.centerXAnchor.constraint(equalTo: view.centerXAnchor)
        break;
      case .right:
        constraint = _container.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        break;
      }
      constraint.priority = UILayoutPriority.required
      _hConstraints.append(constraint)
      _container.addConstraint(constraint)
    }
  }

  private func addVerticalConstraint(for view: UIView) {
    if _orientation == .vertical {
      if let bottomConstraint = _vConstraints.popLast() {
        // cleans up constraint between scroll view and previous "last" component
        removeConstraint(bottomConstraint)
      }

      var constraint = view.topAnchor.constraint(equalTo: _items.count == 0 ? _container.topAnchor : (_items.last?.view.bottomAnchor)!)
      constraint.priority = UILayoutPriority.required
      _vConstraints.append(constraint)
      _container.addConstraint(constraint)

      constraint = _container.bottomAnchor.constraint(greaterThanOrEqualTo: view.bottomAnchor)
      constraint.priority = UILayoutPriority.required
      _vConstraints.append(constraint)
      _container.addConstraint(constraint)
    } else {
      var constraint: NSLayoutConstraint!
      switch _verticalAlign {
      case .top:
        constraint = _container.topAnchor.constraint(equalTo: view.topAnchor)
        break;
      case .center:
        constraint = _container.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        break;
      case .bottom:
        constraint = _container.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        break;
      }
      constraint.priority = UILayoutPriority.required
      _vConstraints.append(constraint)
      _container.addConstraint(constraint)
    }
  }

  private func updateHorizontalConstraints() {
    _container.removeConstraints(_hConstraints)
    _hConstraints.removeAll(keepingCapacity: true)
    removeConstraint(_horizontalConstraint)
    switch _horizontalAlign {
    case .left:
      _horizontalConstraint = leadingAnchor.constraint(equalTo: _container.leadingAnchor)
      break
    case .center:
      _horizontalConstraint = centerXAnchor.constraint(equalTo: _container.centerXAnchor)
      break
    case .right:
      _horizontalConstraint = trailingAnchor.constraint(equalTo: _container.trailingAnchor)
      break
    }
    addConstraint(_horizontalConstraint)
    if orientation == .horizontal {
      /* Theory of operation: We iterate over the child views creating constraints linking their
       * top/bottom edges. If we lack children, do nothing. We keep track of the last view processed
       * to serve two purposes: 1) it tracks whether we are processing the first view, and 2) it
       * allows us to create the constraints between current and last view.
       */
      var lastView: UIView! = nil
      for item in _items {
        if lastView == nil {
          let constraint = item.view.leadingAnchor.constraint(equalTo: _container.leadingAnchor)
          _hConstraints.append(constraint)
          _container.addConstraint(constraint)
        } else {
          let constraint = item.view.leadingAnchor.constraint(equalTo: lastView.trailingAnchor)
          _hConstraints.append(constraint)
          _container.addConstraint(constraint)
        }
        lastView = item.view
      }
      if lastView != nil {
        let constraint = _container.trailingAnchor.constraint(greaterThanOrEqualTo: lastView.trailingAnchor)
        _hConstraints.append(constraint)
        _container.addConstraint(constraint)
      }
    } else {
      for item in _items {
        var constraint: NSLayoutConstraint!
        switch _horizontalAlign {
        case .left:
          constraint = _container.leadingAnchor.constraint(equalTo: item.view.leadingAnchor)
          break
        case .center:
          constraint = _container.centerXAnchor.constraint(equalTo: item.view.centerXAnchor)
          break
        case .right:
          constraint = _container.trailingAnchor.constraint(equalTo: item.view.trailingAnchor)
          break
        }
        _container.addConstraint(constraint)
        _hConstraints.append(constraint)
      }
    }
  }

  private func updateVerticalConstraints() {
    _container.removeConstraints(_vConstraints)
    _vConstraints.removeAll(keepingCapacity: true)
    removeConstraint(_verticalConstraint)
    switch _verticalAlign {
    case .top:
      _verticalConstraint = topAnchor.constraint(equalTo: _container.topAnchor)
      break
    case .center:
      _verticalConstraint = centerYAnchor.constraint(equalTo: _container.centerYAnchor)
      break
    case .bottom:
      _verticalConstraint = bottomAnchor.constraint(equalTo: _container.bottomAnchor)
      break
    }
    addConstraint(_verticalConstraint)
    if orientation == .vertical {
      /* Theory of operation: We iterate over the child views creating constraints linking their
       * top/bottom edges. If we lack children, do nothing. We keep track of the last view processed
       * to serve two purposes: 1) it tracks whether we are processing the first view, and 2) it
       * allows us to create the constraints between current and last view.
       */
      var lastView: UIView! = nil
      for item in _items {
        if lastView == nil {
          let constraint = item.view.topAnchor.constraint(equalTo: _container.topAnchor)
          _vConstraints.append(constraint)
          _container.addConstraint(constraint)
        } else {
          let constraint = item.view.topAnchor.constraint(equalTo: lastView.bottomAnchor)
          _vConstraints.append(constraint)
          _container.addConstraint(constraint)
        }
        lastView = item.view
      }
      if lastView != nil {
        let constraint = _container.bottomAnchor.constraint(greaterThanOrEqualTo: lastView.bottomAnchor)
        _vConstraints.append(constraint)
        _container.addConstraint(constraint)
      }
    } else {
      for item in _items {
        var constraint: NSLayoutConstraint!
        switch _verticalAlign {
        case .top:
          constraint = _container.topAnchor.constraint(equalTo: item.view.topAnchor)
          break
        case .center:
          constraint = _container.centerYAnchor.constraint(equalTo: item.view.centerYAnchor)
          break
        case .bottom:
          constraint = _container.bottomAnchor.constraint(equalTo: item.view.bottomAnchor)
          break
        }
        _container.addConstraint(constraint)
        _vConstraints.append(constraint)
      }
    }
  }
}
