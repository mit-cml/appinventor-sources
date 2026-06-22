//
//  RelativeLayout.swift
//  AIComponentKit
//
//  Created by Cindy Bishop on 2/21/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import UIKit

open class RelativeLayout: Layout {

  private var layoutManager: UIView

  private let NOT_VALID: Int = -1

  public init(preferredEmptyWidth: Int, preferredEmptyHeight: Int) {
    // Create a custom UIView that will act as our layout manager
    layoutManager = CustomRelativeLayoutView(preferredEmptyWidth: preferredEmptyWidth,
                                            preferredEmptyHeight: preferredEmptyHeight)
  }

  /**
   * Returns the width.
   *
   * @return width
   */
  open func getWidth() -> Double {
    return Double(layoutManager.frame.width)
  }

  /**
   * Returns the height.
   *
   * @return height
   */
  open func getHeight() -> Double {
    return Double(layoutManager.frame.height)
  }

  /**
   * Returns the layout manager view
   */
  open func getLayoutManager() -> UIView {
    return layoutManager
  }

  /**
   * Add a component to the layout. The view is inserted synchronously so that
   * subsequent Width/Height setters (called while attachedToWindow is true) can
   * apply constraints immediately without a race against an async dispatch.
   */
  open func add(_ component: ViewComponent) {
    let view = component.view
    view.translatesAutoresizingMaskIntoConstraints = false
    if view.superview !== layoutManager {
      layoutManager.addSubview(view)
    }
    if component.Left != NOT_VALID && component.Top != NOT_VALID {
      updateComponentPosition(component: component)
    }
  }

  /**
   * Update the position of the component within the layout.
   */
  open func updateComponentPosition(component: ViewComponent) {
    let x = component.Left
    let y = component.Top
    let view = component.view

    if x == NOT_VALID || y == NOT_VALID {
      return  // Position not yet set
    }

    // Resolve percent-encoded coordinates to pixel constants.
    let leftConstant: CGFloat
    if x <= Int(kLengthPercentTag) {
      let parentWidth = layoutManager.bounds.width
      if parentWidth == 0 {
        // Parent not yet measured; retry after the next layout pass.
        DispatchQueue.main.async { [weak self] in
          self?.updateComponentPosition(component: component)
        }
        return
      }
      let percent = -(x - Int(kLengthPercentTag))
      leftConstant = parentWidth * CGFloat(percent) / 100.0
    } else {
      leftConstant = CGFloat(x)
    }

    let topConstant: CGFloat
    if y <= Int(kLengthPercentTag) {
      let parentHeight = layoutManager.bounds.height
      if parentHeight == 0 {
        DispatchQueue.main.async { [weak self] in
          self?.updateComponentPosition(component: component)
        }
        return
      }
      let percent = -(y - Int(kLengthPercentTag))
      topConstant = parentHeight * CGFloat(percent) / 100.0
    } else {
      topConstant = CGFloat(y)
    }

    // Ensure view is a child of layoutManager before setting constraints
    if view.superview !== layoutManager {
      layoutManager.addSubview(view)
      view.translatesAutoresizingMaskIntoConstraints = false
    }

    // Remove any existing position constraints for this view
    layoutManager.removeConstraints(layoutManager.constraints.filter {
        ($0.firstItem as? UIView) == view || ($0.secondItem as? UIView) == view
    })
    // Create new constraints
    let leftConstraint = view.leftAnchor.constraint(equalTo: layoutManager.leftAnchor, constant: leftConstant)
    let topConstraint = view.topAnchor.constraint(equalTo: layoutManager.topAnchor, constant: topConstant)

    // Activate them
    NSLayoutConstraint.activate([leftConstraint, topConstraint])

    // Force layout update
    layoutManager.setNeedsLayout()
    layoutManager.layoutIfNeeded()

    view.setNeedsLayout()
  }

}

/**
* Custom UIView subclass that mimics Android's RelativeLayout behavior with
* respect to preferred empty width/height
*/
  private class CustomRelativeLayoutView: UIView {
    private let preferredEmptyWidth: Int
    private let preferredEmptyHeight: Int

    init(preferredEmptyWidth: Int = kEmptyHVArrangementWidth, preferredEmptyHeight: Int = kEmptyHVArrangementHeight) {
      self.preferredEmptyWidth = preferredEmptyWidth
      self.preferredEmptyHeight = preferredEmptyHeight
      super.init(frame: .zero)
    }

    required init?(coder: NSCoder) {
      fatalError("init(coder:) has not been implemented")
    }

    override var intrinsicContentSize: CGSize {
      // If there are no subviews, return preferred empty size
      if subviews.isEmpty {
        return CGSize(width: Int(preferredEmptyWidth), height: Int(preferredEmptyHeight))
      }
      return UIView.layoutFittingExpandedSize
    }
    override func layoutSubviews() {
      super.layoutSubviews()
    }

}
