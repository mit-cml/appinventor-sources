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
  private var componentsToAdd: [ViewComponent] = []
  
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
     * Add a component to the layout
     */
  open func add(_ component: ViewComponent) {
    // Configure the component's view with default constraints
    let view = component.view
    view.translatesAutoresizingMaskIntoConstraints = false
    
    // Add to pending components list
    addComponentLater(component: component)
  }
    
    /**
     * Update the position of the component within the layout.
     */
  open func updateComponentPosition(component: ViewComponent) {
    let x = component.Left
    let y = component.Top
    let view = component.view

      
    if x <= NOT_VALID || y <= NOT_VALID {
      return  // Position not yet set
    }
  
    // Ensure view is a child of layoutManager before setting constraints
    if view.superview !== layoutManager {
      layoutManager.addSubview(view)
      view.translatesAutoresizingMaskIntoConstraints = false
    }
  
    
    // Remove any existing constraints
    layoutManager.removeConstraints(layoutManager.constraints.filter {
        ($0.firstItem as? UIView) == view || ($0.secondItem as? UIView) == view
    })
    // Create new constraints
    let leftConstraint = view.leftAnchor.constraint(equalTo: layoutManager.leftAnchor, constant: CGFloat(x))
    let topConstraint = view.topAnchor.constraint(equalTo: layoutManager.topAnchor, constant: CGFloat(y))
  
    // Activate them
    NSLayoutConstraint.activate([leftConstraint, topConstraint])
  
    // Force layout update
    layoutManager.setNeedsLayout()
    layoutManager.layoutIfNeeded()
    
    view.setNeedsLayout()
  }
    
  /**
   * Causes addComponent to be called later on the main thread.
   */
  private func addComponentLater(component: ViewComponent) {
    DispatchQueue.main.async { [weak self] in
      self?.addComponent(component: component)
    }
  }
    
  private func addComponent(component: ViewComponent) {
    let x = component.Left
    let y = component.Top
    let view = component.view
  
    // Always add the view to the hierarchy first
    if view.superview != layoutManager {
        layoutManager.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
    }
    
    if x == NOT_VALID || y == NOT_VALID {
        // Position not yet set, add it later
        addComponentLater(component: component)
    } else {
        // Set up constraints for position
        updateComponentPosition(component: component)
    }
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
      var maxX = CGFloat(0)
      var maxY = CGFloat(0)
      var hasPositionedSubview = false
      for subview in subviews where !subview.isHidden {
        guard let origin = originForSubview(subview) else {
          continue
        }
        let size = preferredSize(of: subview)
        maxX = max(maxX, origin.x + size.width)
        maxY = max(maxY, origin.y + size.height)
        hasPositionedSubview = true
      }
      if !hasPositionedSubview {
        return CGSize(width: CGFloat(preferredEmptyWidth), height: CGFloat(preferredEmptyHeight))
      }
      return CGSize(width: maxX, height: maxY)
    }

    private func originForSubview(_ subview: UIView) -> CGPoint? {
      var x: CGFloat?
      var y: CGFloat?
      for constraint in constraints {
        if (constraint.firstItem as? UIView) == subview && (constraint.secondItem as? UIView) == self {
          if constraint.firstAttribute == .left && constraint.secondAttribute == .left {
            x = constraint.constant
          } else if constraint.firstAttribute == .top && constraint.secondAttribute == .top {
            y = constraint.constant
          }
        } else if (constraint.firstItem as? UIView) == self && (constraint.secondItem as? UIView) == subview {
          if constraint.firstAttribute == .left && constraint.secondAttribute == .left {
            x = -constraint.constant
          } else if constraint.firstAttribute == .top && constraint.secondAttribute == .top {
            y = -constraint.constant
          }
        }
      }
      if let x = x, let y = y {
        return CGPoint(x: x, y: y)
      }
      return nil
    }

    private func preferredSize(of view: UIView) -> CGSize {
      let fitting = view.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize)
      let intrinsic = view.intrinsicContentSize
      return CGSize(width: preferredDimension(fitting.width, intrinsic.width, view.bounds.width),
                    height: preferredDimension(fitting.height, intrinsic.height, view.bounds.height))
    }

    private func preferredDimension(_ values: CGFloat...) -> CGFloat {
      return values.reduce(CGFloat(0)) { result, value in
        if value.isFinite && value >= 0 && value < CGFloat.greatestFiniteMagnitude {
          return max(result, value)
        }
        return result
      }
    }

    override func layoutSubviews() {
      super.layoutSubviews()
      invalidateIntrinsicContentSize()
    }
  
}
