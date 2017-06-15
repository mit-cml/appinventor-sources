//
//  HVArrangement.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/30/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

private let kHorizontalCenterLeadingConstraint = "HCenterLeadingConstraint"
private let kHorizontalCenterTrailingConstraint = "HCenterTrailingConstraint"
private let kVerticalCenterLeadingConstraint = "VCenterLeadingConstraint"
private let kVerticalCenterTrailingConstraint = "VCenterTrailingConstraint"
private let kComponentKitConstraint = "AIComponentKitConstraint"

open class HVArrangement: ViewComponent, ComponentContainer, AbstractMethodsForViewComponent {
  fileprivate var _components: [ViewComponent] = [ViewComponent]()
//  fileprivate var _view: CSLinearLayoutView!
  fileprivate var _scrollView: UIScrollView!
  private var _contentView: UIView!
  fileprivate let _orientation: HVOrientation
  fileprivate var _horizontalAlign = HorizontalGravity.left
  fileprivate var _verticalAlign = VerticalGravity.top
  fileprivate var _backgroundColor = UIColor.white
  fileprivate var _imagePath = ""
  fileprivate var _csHorizontalAlign = CSLinearLayoutItemHorizontalAlignmentLeft
  fileprivate var _csVerticalAlign = CSLinearLayoutItemVerticalAlignmentTop
  fileprivate var _leading: UIView!
  fileprivate var _trailing: UIView!
  fileprivate var _lastConstraint: NSLayoutConstraint!
  
  public init(_ parent: ComponentContainer, orientation: HVOrientation, scrollable: Bool) {
    _orientation = orientation
//    _view = CSLinearLayoutView()
    _scrollView = UIScrollView()
    _scrollView.translatesAutoresizingMaskIntoConstraints = false
    _contentView = UIView()
    _contentView.accessibilityIdentifier = "Content view"
    _contentView.translatesAutoresizingMaskIntoConstraints = false
    _scrollView.addSubview(_contentView)
    _scrollView.addConstraint(_contentView.leadingAnchor.constraint(equalTo: _scrollView.leadingAnchor))
    _scrollView.addConstraint(_contentView.trailingAnchor.constraint(equalTo: _scrollView.trailingAnchor))
    _scrollView.addConstraint(_contentView.topAnchor.constraint(equalTo: _scrollView.topAnchor))
    _scrollView.addConstraint(_contentView.bottomAnchor.constraint(equalTo: _scrollView.bottomAnchor))
    _leading = UIView()
    _trailing = UIView()
    _leading.translatesAutoresizingMaskIntoConstraints = false
    _trailing.translatesAutoresizingMaskIntoConstraints = false
    _leading.accessibilityIdentifier = "Leading margin"
    _trailing.accessibilityIdentifier = "Trailing margin"
    _contentView.addSubview(_leading)
    _contentView.addSubview(_trailing)
    _scrollView.isScrollEnabled = scrollable
    _scrollView.isDirectionalLockEnabled = true
    if scrollable {
      switch orientation {
      case .horizontal:
        _scrollView.showsHorizontalScrollIndicator = true
        _contentView.addConstraint(_leading.heightAnchor.constraint(equalTo: _contentView.heightAnchor))
        _contentView.addConstraint(_trailing.heightAnchor.constraint(equalTo: _contentView.heightAnchor))
//        _view.orientation = CSLinearLayoutViewOrientationHorizontal
        break
      case .vertical:
        _scrollView.showsVerticalScrollIndicator = true
        _contentView.addConstraint(_leading.widthAnchor.constraint(equalTo: _contentView.widthAnchor))
        _contentView.addConstraint(_trailing.widthAnchor.constraint(equalTo: _contentView.widthAnchor))
//        _view.orientation = CSLinearLayoutViewOrientationVertical
        break
      }
    }
    super.init(parent)
    _scrollView.accessibilityIdentifier = String(describing: type(of: self))
    super.setDelegate(self)
    parent.add(self)
  }

  // MARK: AbstractMethodsForViewComponent protocol implementation
  open override var view: UIView {
    get {
      return _contentView
    }
  }

  // MARK: ComponentContainer protocol implementation
  open var form: Form? {
    get {
      return _container.form
    }
  }

  open func add(_ component: ViewComponent) {
    _components.append(component)
    let child = component.view
//    let layoutItem = CSLinearLayoutItem(for: child)
//    layoutItem?.horizontalAlignment = _csHorizontalAlign
//    layoutItem?.verticalAlignment = _csVerticalAlign
//    _view.addItem(layoutItem)
    _contentView.insertSubview(child, at: _contentView.subviews.index(of: _trailing)!)
    if _components.count > 1, let priorComponent = _components[_components.count - 2] as? ViewComponent {
      let startAttr: NSLayoutAttribute = _orientation == .horizontal ? .left : .top
      let endAttr: NSLayoutAttribute = _orientation == .horizontal ? .right : .bottom
      _contentView.addConstraint(NSLayoutConstraint(item: child, attribute: startAttr, relatedBy: .equal, toItem: priorComponent.view, attribute: endAttr, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
      if (_orientation == .horizontal && _horizontalAlign == .center) {
        _contentView.removeConstraint(_lastConstraint)
        _lastConstraint = _trailing.leadingAnchor.constraint(equalTo: child.trailingAnchor)
        _contentView.addConstraint(_lastConstraint)
      }
    } else {
      if _orientation == .vertical {
        _contentView.addConstraint(_contentView.topAnchor.constraint(equalTo: child.topAnchor))
      } else {
        if (AlignHorizontal == HorizontalGravity.left.rawValue) {
          _contentView.addConstraint(_contentView.leftAnchor.constraint(equalTo: child.leftAnchor))
        } else if (AlignHorizontal == HorizontalGravity.right.rawValue) {
          _contentView.addConstraint(_contentView.rightAnchor.constraint(equalTo: child.rightAnchor))
        } else if (AlignHorizontal == HorizontalGravity.center.rawValue) {
          _contentView.addConstraint(_leading.trailingAnchor.constraint(equalTo: child.leadingAnchor))
          _lastConstraint = _trailing.leadingAnchor.constraint(equalTo: child.trailingAnchor)
          _contentView.addConstraint(_lastConstraint)
        }
      }
    }
    if (_orientation == .horizontal) {
      if (AlignVertical == VerticalGravity.top.rawValue) {
        _contentView.addConstraint(child.topAnchor.constraint(equalTo: _contentView.topAnchor))
      } else if (AlignVertical == VerticalGravity.center.rawValue) {
        _contentView.addConstraint(child.centerYAnchor.constraint(equalTo: _contentView.centerYAnchor))
      } else if (AlignVertical == VerticalGravity.bottom.rawValue) {
        _contentView.addConstraint(child.bottomAnchor.constraint(equalTo: _contentView.bottomAnchor))
      }
//      _view.addConstraint(child.leadingAnchor.constraint(equalTo: view.leadingAnchor))
//      _view.addConstraint(child.trailingAnchor.constraint(equalTo: view.trailingAnchor))
    } else {
      if (AlignHorizontal == HorizontalGravity.left.rawValue) {
        _contentView.addConstraint(child.leftAnchor.constraint(equalTo: _contentView.leftAnchor))
      } else if (AlignHorizontal == HorizontalGravity.center.rawValue) {
        _contentView.addConstraint(child.centerXAnchor.constraint(equalTo: _contentView.centerXAnchor))
      } else if (AlignHorizontal == HorizontalGravity.right.rawValue) {
        _contentView.addConstraint(child.rightAnchor.constraint(equalTo: _contentView.rightAnchor))
      }
    }
    if (Width == kLengthPreferred) {
      _contentView.addConstraint(NSLayoutConstraint(item: _contentView, attribute: .right, relatedBy: .greaterThanOrEqual, toItem: child, attribute: .right, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    }
    if (Height == kLengthPreferred) {
      _contentView.addConstraint(NSLayoutConstraint(item: _contentView, attribute: .bottom, relatedBy: .greaterThanOrEqual, toItem: child, attribute: .bottom, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    }
  }

  open func setChildWidth(of component: ViewComponent, width: Int32) {
    let child = component.view
    if width >= 0 {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(width)))
    } else if width == kLengthPreferred {
      view.addConstraint(NSLayoutConstraint(item: view, attribute: .width, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .width, relatedBy: .equal, toItem: view, attribute: .width, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if width <= kLengthPercentTag {
      let width = -(width + 1000)
      let pWidth = CGFloat(width) / CGFloat(100.0)
      form?.view.addConstraint(child.widthAnchor.constraint(equalTo: (form?.view.widthAnchor)!, multiplier: pWidth))
    } else {
      NSLog("Unable to process width value \(width)")
    }
    var myWidth = CGFloat(0.0)
    for component in _components {
      let child = component.view
      myWidth += child.frame.size.width
    }
    NSLog("Setting width to \(myWidth)")
    var frame = _contentView.frame
    frame.size.width = myWidth
    _contentView.frame = frame
    _contentView.addConstraint(_contentView.widthAnchor.constraint(greaterThanOrEqualToConstant: myWidth));
  }

  open func setChildHeight(of component: ViewComponent, height: Int32) {
    let child = component.view
    if height >= 0 {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: CGFloat(0.0), constant: CGFloat(height)))
      child.frame.size.height = CGFloat(height)
    } else if height == kLengthPreferred {
      view.addConstraint(NSLayoutConstraint(item: view, attribute: .height, relatedBy: .greaterThanOrEqual, toItem: component.view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height == kLengthFillParent {
      view.addConstraint(NSLayoutConstraint(item: component.view, attribute: .height, relatedBy: .equal, toItem: view, attribute: .height, multiplier: CGFloat(1.0), constant: CGFloat(0.0)))
    } else if height <= kLengthPercentTag {
      let height = -(height + 1000)
      let pHeight = CGFloat(height) / CGFloat(100.0)
      form?.view.addConstraint(child.heightAnchor.constraint(equalTo: (form?.view.heightAnchor)!, multiplier: pHeight))
      child.frame.size.height = pHeight * (form?.view.frame.size.height)!
    } else {
      NSLog("Unable to process width value \(height)")
    }
    var myHeight = CGFloat(0.0)
    for component in _components {
      let child = component.view
      myHeight += child.frame.size.height
    }
    NSLog("Setting height to \(myHeight)")
    var frame = _contentView.frame
    frame.size.height = myHeight
    _contentView.frame = frame
    _contentView.addConstraint(_contentView.heightAnchor.constraint(greaterThanOrEqualToConstant: myHeight));
  }

  // MARK: HVArrangement Properties
  open var AlignHorizontal: Int32 {
    get {
      return _horizontalAlign.rawValue
    }
    set(align) {
      if let align = HorizontalGravity(rawValue: align) {
        _horizontalAlign = align
        switch(align) {
        case .left:
          _csHorizontalAlign = CSLinearLayoutItemHorizontalAlignmentLeft
          break
        case .center:
          _csHorizontalAlign = CSLinearLayoutItemHorizontalAlignmentCenter
          if (_orientation == .horizontal) {
            _contentView.addConstraint(_leading.widthAnchor.constraint(equalTo: _trailing.widthAnchor))
            _contentView.addConstraint(_leading.leadingAnchor.constraint(equalTo: _contentView.leadingAnchor))
            _contentView.addConstraint(_trailing.trailingAnchor.constraint(equalTo: _contentView.trailingAnchor))
          }
          break
        case .right:
          _csHorizontalAlign = CSLinearLayoutItemHorizontalAlignmentRight
          break
        }
//        for item in _view.items {
//          (item as! CSLinearLayoutItem).horizontalAlignment = _csHorizontalAlign
//        }
        _contentView.layoutSubviews()
      }
    }
  }

  open var AlignVertical: Int32 {
    get {
      return _verticalAlign.rawValue
    }
    set(align) {
      if let align = VerticalGravity(rawValue: align) {
        _verticalAlign = align
        switch(align) {
        case .top:
          _csVerticalAlign = CSLinearLayoutItemVerticalAlignmentTop
          break
        case .center:
          _csVerticalAlign = CSLinearLayoutItemVerticalAlignmentCenter
          break
        case .bottom:
          _csVerticalAlign = CSLinearLayoutItemVerticalAlignmentBottom
          break
        }
//        for item in _view.items {
//          (item as! CSLinearLayoutItem).verticalAlignment = _csVerticalAlign
//        }
        _contentView.layoutSubviews()
      }
    }
  }

  open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set(argb) {
      _backgroundColor = argbToColor(argb)
      if _imagePath != "" {
        _contentView.backgroundColor = _backgroundColor
      }
    }
  }

  open var Image: String {
    get {
      return _imagePath
    }
    set(path) {
      if path == _imagePath {
        // Already using this image
        return
      } else if path != "" {
        let image = AssetManager.shared.imageFromPath(path: path)
        if image != nil {
          _contentView.backgroundColor = UIColor(patternImage: image!)
          return
        }
      }
      _imagePath = ""
      _contentView.backgroundColor = _backgroundColor
    }
  }

  // MARK: Private implementation
  fileprivate func updateConstraints() {
    
  }

  fileprivate func updateConstraints(for view: UIView) {
  }
  
  fileprivate func updateHorizontalConstraints() {
    
  }
  
  fileprivate func updateVerticalConstraints() {
    
  }
  
  fileprivate func updateHorizontalConstraints(for view: UIView) {
    let constraints = _contentView.constraintsAffectingLayout(for: UILayoutConstraintAxis.horizontal)
    if _orientation == .horizontal {
      
    }
  }

  fileprivate func updateVerticalConstraints(for view: UIView) {
    let constraints = _contentView.constraintsAffectingLayout(for: UILayoutConstraintAxis.vertical)
    if _orientation == .vertical {
      
    }
  }
}
