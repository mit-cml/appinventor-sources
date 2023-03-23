// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

// Custom priorities for TableCellCollection
let DefaultTableSizingPriority = UILayoutPriority(500)
let CellSizingPriority = UILayoutPriority(501)

/**
 * A struct used to hold a weak reference to an object
 * Used for TableCellCollection to stora an array of references
 */
struct WeakRef<T: AnyObject> {
  weak var value: T?
  init(_ value: T) {
    self.value = value
  }
}

/**
 * An enum that defines the possible constraint updates for a TableCell
 */
fileprivate enum ConstraintUpdate {
  // called when we want to change the height only
  case height
  // called when initializing a TableCell
  case initialize
  // called when we want to change the width only
  case width
}

/**
 * A view used to represent a cell in TableArrangement
 * Handles adding and removing constraints for a view
 */
@objc public class TableCell: UIView {
  /**
   * The width and height constraints for the component's view
   * We store these so that they can be removed and added as necessary without breaking constraints
   */
  private var _heightConstraint: NSLayoutConstraint? = nil
  private var _widthConstraint: NSLayoutConstraint? = nil

  init() {
    super.init(frame: .zero)
    translatesAutoresizingMaskIntoConstraints = false
    setContentHuggingPriority(.defaultHigh, for: .horizontal)
    setContentHuggingPriority(.defaultHigh, for: .vertical)
  }

  required public init?(coder aDecoder: NSCoder) {
    fatalError()
  }

  /**
   * The component that should be displayed for this cell
   * It is left aligned to ensure that the component takes up the whole width
   */
  fileprivate weak var component: ViewComponent? = nil {
    didSet {
      if let view = component {
        addSubview(view.view)
        view.view.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
      }
    }
  }

  /**
   * Handles updating height/width constraints
   * Height and width constraints are only added if the cell has a a percent- or pixel-based width
   * Percent-based width sales off of the screen's size
   * If the change is for height or width, the previous constraint (if any) is first removed
   */
  fileprivate func updateComponentConstraints(for change: ConstraintUpdate) {
    if change == .height, let constraint = _heightConstraint {
      component?.view.removeConstraint(constraint)
      component?.form?.view.removeConstraint(constraint)
    }
    if change == .width, let constraint = _widthConstraint {
      component?.view.removeConstraint(constraint)
      component?.form?.view.removeConstraint(constraint)
    }
    if change == .height || change == .initialize {
      addWidthConstraint()
    }
    if change == .width || change == .initialize {
      addHeightConstraint()
    }
  }

  // MARK: helper methods for constraints
  private func addHeightConstraint() {
    _heightConstraint = nil
    if let child = component {
      if child._lastSetHeight >= 0 {
        _heightConstraint = child.view.heightAnchor.constraint(equalToConstant: CGFloat(child._lastSetHeight))
        child.view.addConstraint(_heightConstraint!)
      } else if child._lastSetHeight <= kLengthPercentTag {
        if let formView = child.form?.view, child.attachedToWindow {
          let height = -(child._lastSetHeight + 1000)
          let pHeight = CGFloat(height) / 100
          _heightConstraint = child.view.heightAnchor.constraint(equalTo: formView.heightAnchor, multiplier: pHeight)
          formView.addConstraint(_heightConstraint!)
        }
      }
    }
  }

  private func addWidthConstraint() {
    _widthConstraint = nil
    if let child = component {
      if child._lastSetWidth >= 0 {
        _widthConstraint = child.view.widthAnchor.constraint(equalToConstant: CGFloat(child._lastSetWidth))
        child.view.addConstraint(_widthConstraint!)
      } else if child._lastSetWidth <= kLengthPercentTag {
        if let formView = child.form?.view, child.attachedToWindow {
          let width = -(child._lastSetWidth + 1000)
          let pWidth = CGFloat(width) / 100
          _widthConstraint = child.view.widthAnchor.constraint(equalTo: formView.widthAnchor, multiplier: pWidth)
          formView.addConstraint(_widthConstraint!)
        }
      }
    }
  }
}

/**
 * This class is used to manage all cells for a TableArrangement
 */
@objc public class TableCellCollection: UIView {
  // stores all the cells for the TableArrangement. Mapped from components
  private var _cells = [[TableCell]]()
  // stores all the components for the TableArrangement, including ones that are not visible
  private var _components = [WeakRef<ViewComponent>]()

  // constraints for when the height and/or width are automatic, to force the parent view to take up space
  private var _emptyConstraints: [NSLayoutConstraint]!
  private var _bottomConstraints = [NSLayoutConstraint]()
  private var _rightConstraints = [NSLayoutConstraint]()

  // The parent container
  private weak var _container: TableArrangement?

  // MARK: wrapper for parent values
  private var rows: Int32 {
    get {
      return _container?.Rows ?? 2
    }
  }

  private var columns: Int32 {
    get {
      return _container?.Columns ?? 2
    }
  }

  init(_ container: TableArrangement) {
    _container = container
    super.init(frame: .zero)
    _emptyConstraints = [heightAnchor.constraint(equalToConstant: 100), widthAnchor.constraint(equalToConstant: 100)]
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  /**
   * Initializes the cells in the table with the corresponding views (if any)
   * Constraints are added such that each cells in a column have the same width, and cells in a row have the same height
   */
  func initialize() {
    _cells = [[TableCell]]()

    // We create all the cells and add them as subviews
    for _ in 0..<rows {
      var row = [TableCell]()
      for _ in 0..<columns {
        let cell = TableCell()
        row.append(cell)
        addSubview(cell)
      }
      _cells.append(row)
    }

    /**
     * We need to add the components to all the cells.
     * This is done in reverse order to give priority to later components if multiple components resize in the same cell
     * The cell is constrained to being equal to or larger than the view it encloses
     * Then, the component's constraints are applied to the cell. Only percent-based and pixel-based heights are included
     */
    for ref in _components.reversed() {
      if let component = ref.value {
        guard component.Row < rows && component.Column < columns else {
          continue
        }
        let cell = _cells[Int(component.Row)][Int(component.Column)]
        if cell.subviews.count == 0 {
          cell.component = component
          constrain(cell.widthAnchor, to: component.view.widthAnchor, with: CellSizingPriority, for: cell)
          constrain(cell.heightAnchor, to: component.view.heightAnchor, with: CellSizingPriority, for: cell)
          self.addConstraint(component.view.centerYAnchor.constraint(equalTo: cell.centerYAnchor))
          cell.updateComponentConstraints(for: .initialize)
        }
      }
    }

    /**
     * Finally, we need to make sure that all cells in a row have the same height and all cells in a column have the same width
     * These constraints have a lower priority than the cell sizing constraints
     * Constraints are also made for automatic height and width to force the view to take up the entire space
     */
    for (rowIdx, row) in _cells.enumerated() {
      for (colIdx, cell) in row.enumerated() {
        addConstraint(cell.leftAnchor.constraint(equalTo: (colIdx == 0 ? leftAnchor: row[colIdx - 1].rightAnchor)))
        addConstraint(cell.topAnchor.constraint(equalTo: (rowIdx == 0 ? topAnchor: _cells[rowIdx - 1][colIdx].bottomAnchor)))
        for (idx, otherCell) in row.enumerated() {
          if idx != colIdx {
            constrain(cell.heightAnchor, to: otherCell.heightAnchor, with: DefaultTableSizingPriority, for: self)
          }
        }
        for (idx, otherRow) in _cells.enumerated() {
          if idx != rowIdx {
            constrain(cell.widthAnchor, to: otherRow[colIdx].widthAnchor, with: DefaultTableSizingPriority, for: self)
          }
        }
        if colIdx == columns - 1 {
          _rightConstraints.append(rightAnchor.constraint(equalTo: cell.rightAnchor))
        }
        if rowIdx == rows - 1 {
          _bottomConstraints.append(bottomAnchor.constraint(equalTo: cell.bottomAnchor))
        }
      }
    }

    _initialized = true
    if _automaticWidth {
      addConstraints(_rightConstraints)
    }
    if _automaticHeight {
      addConstraints(_bottomConstraints)
    }
  }

  private var _initialized = false
  private var _automaticWidth = false
  private var _automaticHeight = false

  var automaticWidth: Bool {
    get {
      return _automaticWidth
    }
    set(newValue) {
      _automaticWidth = newValue
      guard _initialized else { return }
      if newValue {
        addConstraints(_rightConstraints)
      } else {
        removeConstraints(_rightConstraints)
      }
    }
  }

  var automaticHeight: Bool {
    get {
      return _automaticHeight
    }
    set(newValue) {
      _automaticHeight = newValue
      guard _initialized else { return }
      if newValue {
        addConstraints(_bottomConstraints)
      } else {
        removeConstraints(_bottomConstraints)
      }
    }
  }

  // adds a component to the view
  fileprivate func add(component: ViewComponent) {
    _components.append(WeakRef<ViewComponent>(component))
  }

  // called when we want to change the width or height of a component
  fileprivate func update(column: Int32, row: Int32, for update: ConstraintUpdate) {
    if 0..<columns ~= column, 0..<rows ~= row {
      _cells[Int(row)][Int(column)].updateComponentConstraints(for: update)
    }
  }

  // a helper for adding a constraint with a custom priority
  private func constrain(_ anchor: NSLayoutDimension, to otherAnchor: NSLayoutDimension, with priority: UILayoutPriority, for parent: UIView) {
    if priority == CellSizingPriority {
      let constraint = anchor.constraint(greaterThanOrEqualTo: otherAnchor)
      constraint.priority = priority
      parent.addConstraint(constraint)
    }
    let constraint = anchor.constraint(equalTo: otherAnchor)
    constraint.priority = priority
    parent.addConstraint(constraint)
  }

  @objc override public var intrinsicContentSize: CGSize {
    get {
      return CGSize(width: 100, height: 100)
    }
  }
}

open class TableArrangement: ViewComponent, AbstractMethodsForViewComponent, ComponentContainer {
  private var _view: TableCellCollection!
  private var _initialized = false

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    _view = TableCellCollection(self)
    _view.translatesAutoresizingMaskIntoConstraints = false
    super.setDelegate(self)
    parent.add(self)
    self.Height = kLengthPreferred
    self.Width = kLengthPreferred
  }

  @objc open func Initialize() {
    _view.initialize()
    _initialized = true
  }

  @objc open var Columns: Int32 = 2

  @objc open override var Height: Int32 {
    get {
      return super.Height
    }
    set(newHeight) {
      super.Height = newHeight
      _view.automaticHeight = newHeight == kLengthPreferred
    }
  }

  @objc open var Rows: Int32 = 2

  @objc open override var Width: Int32 {
    get {
      return super.Width
    }
    set(newWidth) {
      super.Width = newWidth
      _view.automaticWidth = newWidth == kLengthPreferred
    }
  }

  @objc open override var view: UIView {
    get {
      return _view
    }
  }

  // MARK: ComponentContainer implementation

  open var container: ComponentContainer? {
    get {
      return _container
    }
  }

  public func add(_ component: ViewComponent) {
    _view.add(component: component)
  }

  public func setChildWidth(of component: ViewComponent, to width: Int32) {
    component._lastSetWidth = width
    if _initialized {
      _view.update(column: component.Column, row: component.Row, for: .width)
    }
  }

  public func setChildHeight(of component: ViewComponent, to height: Int32) {
    component._lastSetHeight = height
    if _initialized  {
      _view.update(column: component.Column, row: component.Row, for: .height)
    }
  }

  public func isVisible(component: ViewComponent) -> Bool {
    return !component.view.isHidden
  }

  public func setVisible(component: ViewComponent, to visibility: Bool) {
    component.view.isHidden = !visibility
  }

  open func getChildren() -> [Component] {
    return []
  }
}
