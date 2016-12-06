//
//  PasswordTextBox.swift
//  AIComponentKit
//
//  Created by Evan Patton on 12/1/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class PasswordTextBox: TextBoxBase, AbstractMethodsForTextBox {
  private let _field = UITextField(frame: CGRect.zero)

  public override init(_ parent: ComponentContainer) {
    super.init(parent)
    super.setDelegate(self)
    _field.isSecureTextEntry = true
  }
}
