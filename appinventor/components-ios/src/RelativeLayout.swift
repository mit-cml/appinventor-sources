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
  
  public init(preferredEmptyWidth: Int?, preferredEmptyHeight: Int?) {
    if (preferredEmptyWidth == nil && preferredEmptyHeight != nil) ||
       (preferredEmptyWidth != nil && preferredEmptyHeight == nil) {
        fatalError("RelativeLayout - preferredEmptyWidth and preferredEmptyHeight must be either both nil or both not nil")
    }
    
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
    let x = component.left()
    let y = component.top()
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
    let x = component.left()
    let y = component.top()
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
  private let preferredEmptyWidth: Int?
  private let preferredEmptyHeight: Int?
  
  init(preferredEmptyWidth: Int?, preferredEmptyHeight: Int?) {
    self.preferredEmptyWidth = preferredEmptyWidth
    self.preferredEmptyHeight = preferredEmptyHeight
    super.init(frame: .zero)
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override var intrinsicContentSize: CGSize {
    // If there are no subviews, return preferred empty size
    if subviews.isEmpty && preferredEmptyWidth != nil && preferredEmptyHeight != nil {
        return CGSize(width: preferredEmptyWidth!, height: preferredEmptyHeight!)
    }
    
    // Otherwise return automatic size
    return UIView.layoutFittingExpandedSize
  }
  
  override func layoutSubviews() {
    super.layoutSubviews()

  }
}
