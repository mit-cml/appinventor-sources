// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

/**
 * The `ComponentContainer` protocol is implemented by any `Component` that is intended to have
 * children.
 */
@objc public protocol ComponentContainer {
  /**
   * Gets the `Form` that is at the root of the container hierarchy, if any.
   */
  var form: Form? { get }

  /**
   * Gets the parent `ComponentContainer` for this container. This may be `nil` if the container
   * is not attached to the component hierarchy.
   */
  var container: ComponentContainer? { get }

  /**
   * Adds a child to the receiving container.
   */
  func add(_ component: ViewComponent)

  /**
   * Sets the width of the specified child to the given value.
   *
   * - Parameter component: The child component to resize
   * - Parameter width: The desired width of `component`
   */
  func setChildWidth(of component: ViewComponent, to width: Int32)

  /**
   * Sets the height of the specified child to the given value.
   *
   * - Parameter component: The child component to resize
   * - Parameter width: The desired height of `component`
   */
  func setChildHeight(of component: ViewComponent, to height: Int32)

  /**
   * Returns whether `component` is marked as visible or not. Even if this is `true`, it is not
   * guaranteed that the view is attached to the view hierarchy.
   *
   * - Parameter component: The component to test for visibility
   */
  func isVisible(component: ViewComponent) -> Bool

  /**
   * Sets the visibility of `component` to the given `visibility`.
   *
   * - Parameter component:The component to change the visibility of
   * - Parameter visibility: Boolean indicating whether the component should be visible
   */
  func setVisible(component: ViewComponent, to visibility: Bool)

  /**
   * The component's width, in pixels.
   */
  var Width: Int32 { get }

  /**
   * The component's height, in pixels.
   */
  var Height: Int32 { get }

  /**
   * Gets the list of direct descendants of the receiving `ComponentContainer`.
   *
   * - Returns:
   */
  func getChildren() -> [Component]
}
