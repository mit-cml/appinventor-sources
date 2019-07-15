//
//  ToggleBase.swift
//  AIComponentKit
//
//  Created by Susan Rati Lane on 5/10/19.
//  Copyright © 2019 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public protocol AbstractMethodsForToggle: AbstractMethodsForViewComponent {
  func changeSwitch(gesture: UITapGestureRecognizer)
}
