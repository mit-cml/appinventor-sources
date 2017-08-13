//
//  TableArrangement.swift
//  AIComponentKit
//
//  Created by Evan Patton on 12/14/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class TableArrangement: ViewComponent, AbstractMethodsForViewComponent, ComponentContainer {

  private var _view: UICollectionView!

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
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
    _view.addSubview(component.view)
  }

  public func setChildWidth(of component: ViewComponent, width: Int32) {

  }

  public func setChildHeight(of component: ViewComponent, height: Int32) {

  }
}
