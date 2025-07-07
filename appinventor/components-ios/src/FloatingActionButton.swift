//
//  FloatingActionButton.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 07/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//

import UIKit

@objc(FloatingActionButton)
open class FloatingActionButton: ViewComponent, AbstractMethodsForViewComponent {
    private let _view = UIView()
    private let button = UIButton(type: .custom)
    public var onTap: (() -> Void)?
    private var heightConstraint: NSLayoutConstraint?
    private var widthConstraint: NSLayoutConstraint?
    private var _icon: String = ""

  @objc public override init(_ parent: ComponentContainer) {
        super.init(parent)
        super.setDelegate(self)
        parent.add(self)
        setupButton()
        // Anchor to bottom right of parent view
        if let parentView = parent.form?.view {
            _view.translatesAutoresizingMaskIntoConstraints = false
            parentView.addSubview(_view)
            NSLayoutConstraint.activate([
                _view.trailingAnchor.constraint(equalTo: parentView.trailingAnchor, constant: -24),
                _view.bottomAnchor.constraint(equalTo: parentView.bottomAnchor, constant: -24),
                _view.widthAnchor.constraint(equalToConstant: 56),
                _view.heightAnchor.constraint(equalToConstant: 56)
            ])
        }
    }

    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupButton() {
        button.translatesAutoresizingMaskIntoConstraints = false
        button.backgroundColor = .systemBlue
        button.tintColor = .white
        button.layer.cornerRadius = 28
        button.layer.shadowColor = UIColor.black.cgColor
        button.layer.shadowOpacity = 0.3
        button.layer.shadowOffset = CGSize(width: 0, height: 2)
        button.layer.shadowRadius = 4

        if #available(iOS 13.0, *) {
            let plus = UIImage(systemName: "plus")
            button.setImage(plus, for: .normal)
        } else {
            button.setTitle("+", for: .normal)
        }

        button.addTarget(self, action: #selector(tapAction), for: .touchUpInside)
        _view.addSubview(button)
        NSLayoutConstraint.activate([
            button.widthAnchor.constraint(equalToConstant: 56),
            button.heightAnchor.constraint(equalToConstant: 56),
            button.centerXAnchor.constraint(equalTo: _view.centerXAnchor),
            button.centerYAnchor.constraint(equalTo: _view.centerYAnchor)
        ])
    }

    @objc private func tapAction() {
        onTap?()
    }

    open override var view: UIView {
        return _view
    }

    @objc open override var Height: Int32 {
        get {
            return Int32(_view.frame.height)
        }
        set(newHeight) {
            let h = CGFloat(newHeight)
            if let constraint = heightConstraint {
                constraint.constant = h
            } else {
                heightConstraint = _view.heightAnchor.constraint(equalToConstant: h)
                heightConstraint?.isActive = true
            }
            // Keep width square by default
            if let wConstraint = widthConstraint {
                wConstraint.constant = h
            } else {
                widthConstraint = _view.widthAnchor.constraint(equalToConstant: h)
                widthConstraint?.isActive = true
            }
            _view.setNeedsLayout()
            _view.layoutIfNeeded()
        }
    }

    @objc open var Icon: String {
        get { _icon }
        set {
            _icon = newValue
            print("[FAB] Setting Icon to: \(_icon)")
            if _icon.isEmpty {
                button.setImage(nil, for: .normal)
                button.setTitle("+", for: .normal) // fallback
                return
            }
            // Try AssetManager first (matches Image, Button, etc.)
            if let image = AssetManager.shared.imageFromPath(path: _icon) {
                print("[FAB] Loaded image from AssetManager: \(_icon)")
                button.setImage(image, for: .normal)
                button.setTitle(nil, for: .normal)
                return
            }
            // Fallback to system or asset images
            if #available(iOS 13.0, *) {
                if let image = UIImage(systemName: _icon) {
                    print("[FAB] Found system image: \(_icon)")
                    button.setImage(image, for: .normal)
                    button.setTitle(nil, for: .normal)
                    return
                }
            }
            if let image = UIImage(named: _icon) {
                print("[FAB] Found asset image: \(_icon)")
                button.setImage(image, for: .normal)
                button.setTitle(nil, for: .normal)
                return
            }
            // Fallback: show text
            button.setImage(nil, for: .normal)
            button.setTitle(_icon, for: .normal)
        }
    }
}
