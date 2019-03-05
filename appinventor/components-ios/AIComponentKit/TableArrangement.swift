// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2016-2018 Massachusetts Institute of Technology, All rights reserved.

import Foundation

// Custom priorities for TableCellCollection
let DefaultTableSizingPriority = UILayoutPriority(8)
let CellSizingPriority = UILayoutPriority(9)

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
fileprivate class TableCell: UIView {
  /**
   * The width and height constraints for the component's view
   * We store these so that they can be removed and added as necessary without breaking constraints
   */
  private var _heightConstraint: NSLayoutConstraint? = nil
  private var _widthConstraint: NSLayoutConstraint? = nil

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
      component?._container.form.view.removeConstraint(constraint)
    }
    if change == .width, let constraint = _widthConstraint {
      component?.view.removeConstraint(constraint)
      component?._container.form.view.removeConstraint(constraint)
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
        let height = -(child._lastSetHeight + 1000)
        let pHeight = CGFloat(height) / 100
        _heightConstraint = child.view.heightAnchor.constraint(equalTo: child._container.form.view.heightAnchor, multiplier: pHeight)
        child._container.form.view.addConstraint(_heightConstraint!)
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
        let width = -(child._lastSetWidth + 1000)
        let pWidth = CGFloat(width) / 100
        _widthConstraint = child.view.widthAnchor.constraint(equalTo: child._container.form.view.widthAnchor, multiplier: pWidth)
        child._container.form.view.addConstraint(_widthConstraint!)
      }
    }
  }
}

/**
 * This class is used to manage all cells for a TableArrangement
 */
fileprivate class TableCellCollection: UIView {
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
        cell.translatesAutoresizingMaskIntoConstraints = false
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
  }

  /**
   * These functions are used to update the height and width respectively
   * Should only be called after the view has been initialized
   * If there are no components, the automatic width and height should be 100
   */
  fileprivate func updateHeight(from oldVal: Int32, to newVal: Int32) {
    if oldVal != newVal {
      let constraints = _components.count == 0 ? [_emptyConstraints[0]]: _bottomConstraints
      if oldVal == kLengthPreferred {
        removeConstraints(constraints)
      } else if newVal == kLengthPreferred {
        addConstraints(constraints)
      }
    }
  }

  fileprivate func updateWidth(from oldVal: Int32, to newVal: Int32) {
    if oldVal != newVal {
      let constraints = _components.count == 0 ? [_emptyConstraints[1]]: _rightConstraints
      if oldVal == kLengthPreferred {
        removeConstraints(constraints)
      } else if newVal == kLengthPreferred {
        addConstraints(constraints)
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
    let constraint = priority == CellSizingPriority ? anchor.constraint(greaterThanOrEqualTo: otherAnchor): anchor.constraint(equalTo: otherAnchor)
    constraint.priority = priority
    parent.addConstraint(constraint)
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
  }

  open func Initialize() {
    _view.initialize()
    if Height == kLengthPreferred {
      _view.updateHeight(from: 0, to: kLengthPreferred)
    }
    if Width == kLengthPreferred {
      _view.updateWidth(from: 0, to: kLengthPreferred)
    }
    _initialized = true
  }

  open var Columns: Int32 = 2

  open override var Height: Int32 {
    didSet(height) {
      _view.updateHeight(from: height, to: Height)
    }
  }

  open var Rows: Int32 = 2

  open override var Width: Int32 {
    didSet(width) {
      _view.updateWidth(from: width, to: Width)
    }
  }

  open override var view: UIView {
    get {
      return _view
    }
  }

  open var form: Form {
    get {
      return _container.form
    }
  }

  public func add(_ component: ViewComponent) {
    _view.add(component: component)
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {
    component._lastSetWidth = width
    if _initialized {
      _view.update(column: component.Column, row: component.Row, for: .width)
    }
  }

  public func setChildHeight(of component: ViewComponent, height: Int32) {
    component._lastSetHeight = height
    if _initialized  {
      _view.update(column: component.Column, row: component.Row, for: .height)
    }
  }
}
