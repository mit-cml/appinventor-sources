//
//  Sidebar.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 29/06/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//


import UIKit

@available(iOS 14.5, *)
@objc(Sidebar)
open class Sidebar: ViewComponent, AbstractMethodsForViewComponent {
    private var menuViewController: UIViewController
    private var _visible: Bool = false
    private var gestureRecognizers: [UIGestureRecognizer] = []
    
    // Gesture configuration properties
    @objc open var dismissOnSwipe: Bool = true
    @objc open var dismissOnTap: Bool = true
    @objc open var swipeDirection: UISwipeGestureRecognizer.Direction = .left
    @objc open var enableNativeGestures: Bool = true  // Enable native iOS gestures

    // MARK: - Init
    @objc public override init(_ parent: ComponentContainer) {
        self.menuViewController = Sidebar.defaultMenuController()
        super.init(parent)
        parent.add(self)
        setDelegate(self)
        
        // Disable native split view gestures and buttons from the start
        if let splitVC = splitViewController() {
            configureSplitViewControllerForCustomSidebar(splitVC)
        }
    }

    // MARK: - ViewComponent
    open override var view: UIView {
        return menuViewController.view
    }

    // MARK: - Public API
    @objc open var MenuViewController: UIViewController {
        get { menuViewController }
        set {
            configureMenuViewControllerBackground(newValue)
            menuViewController = newValue
        }
    }
    
    @objc open override var Visible: Bool {
        get { _visible }
        set { newValue ? showSidebar() : hideSidebar() }
    }

    @objc open func ShowSidebar() { showSidebar() }
    @objc open func HideSidebar() { hideSidebar() }
    @objc open func SetMenu(_ menu: UIViewController) {
        configureMenuViewControllerBackground(menu)
        menuViewController = menu
    }
    

    

    
    // MARK: - Gesture Configuration
    @objc open func enableSwipeToDismiss(_ enabled: Bool, direction: UISwipeGestureRecognizer.Direction = .left) {
        dismissOnSwipe = enabled
        swipeDirection = direction
        if _visible {
            setupGestures()
        }
    }
    
    @objc open func enableTapToDismiss(_ enabled: Bool) {
        dismissOnTap = enabled
        if _visible {
            setupGestures()
        }
    }
    
    @objc open func enableNativeIOSGestures(_ enabled: Bool) {
        enableNativeGestures = enabled
        if let splitVC = splitViewController() {
            splitVC.presentsWithGesture = enabled
        }
    }

    // MARK: - Private Helper Methods
    private func configureMenuViewControllerBackground(_ viewController: UIViewController) {
        viewController.loadViewIfNeeded()
        
        // Wrap menu view in a container with a button at the bottom (iPhone only)
        if UIDevice.current.userInterfaceIdiom == .phone {
            if let wrapped = wrapMenuWithReturnButton(viewController) {
                // Replace the view controller's view with the wrapped view
                viewController.view = wrapped
            }
        }
        
        viewController.view.backgroundColor = .white
        viewController.view.isOpaque = true
        viewController.view.alpha = 1.0
        if #available(iOS 13.0, *) {
            viewController.view.backgroundColor = .systemBackground
        }
        if let navController = viewController as? UINavigationController {
            configureNavigationControllerForSidebar(navController)
        }
    }

    // Helper to wrap menu view with a button at the bottom (iPhone only)
    private func wrapMenuWithReturnButton(_ menuVC: UIViewController) -> UIView? {
        // Avoid double-wrapping
        if menuVC.view.subviews.contains(where: { $0 is UIStackView }) {
            return menuVC.view
        }
        let stack = UIStackView()
        stack.axis = .vertical
        stack.alignment = .fill
        stack.distribution = .fill
        stack.translatesAutoresizingMaskIntoConstraints = false
        // Add the menu's main view
        let menuContent = menuVC.view
        menuContent?.translatesAutoresizingMaskIntoConstraints = false
        stack.addArrangedSubview(menuContent!)
        // Add spacer
        let spacer = UIView()
        spacer.translatesAutoresizingMaskIntoConstraints = false
        stack.addArrangedSubview(spacer)
        // Add the button
        let button = UIButton(type: .system)
        button.setTitle("Return to Screen", for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 18, weight: .medium)
        button.addTarget(self, action: #selector(returnToScreenButtonTapped), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        stack.addArrangedSubview(button)
        // Set button height
        button.heightAnchor.constraint(equalToConstant: 48).isActive = true
        // Add padding
        stack.layoutMargins = UIEdgeInsets(top: 0, left: 0, bottom: 24, right: 0)
        stack.isLayoutMarginsRelativeArrangement = true
        // Wrap in a container view
        let container = UIView()
        container.backgroundColor = menuContent?.backgroundColor
        container.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: container.topAnchor),
            stack.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: container.trailingAnchor),
            stack.bottomAnchor.constraint(equalTo: container.bottomAnchor)
        ])
        return container
    }

    @objc private func returnToScreenButtonTapped() {
        if let splitVC = splitViewController() {
            splitVC.show(.secondary)
        }
    }
    
    private func setupGestures() {
        guard let splitVC = splitViewController() else { return }
        
        removeGestures()
        
        if dismissOnSwipe {
            let swipeGesture = UISwipeGestureRecognizer(target: self, action: #selector(handleSwipeGesture(_:)))
            swipeGesture.direction = swipeDirection
            swipeGesture.delegate = self
            splitVC.view.addGestureRecognizer(swipeGesture)
            gestureRecognizers.append(swipeGesture)
        }
        
        if dismissOnTap {
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTapGesture(_:)))
            tapGesture.delegate = self
            splitVC.view.addGestureRecognizer(tapGesture)
            gestureRecognizers.append(tapGesture)
        }
    }
    
    private func removeGestures() {
        guard let splitVC = splitViewController() else { return }
        
        for gesture in gestureRecognizers {
            splitVC.view.removeGestureRecognizer(gesture)
        }
        gestureRecognizers.removeAll()
    }
    
    @objc private func handleSwipeGesture(_ gesture: UISwipeGestureRecognizer) {
        guard _visible else { return }
        print("[Sidebar] Swipe gesture detected - dismissing sidebar")
        hideSidebar()
    }
    
    @objc private func handleTapGesture(_ gesture: UITapGestureRecognizer) {
        guard _visible else { return }
        
        let location = gesture.location(in: gesture.view)
        
        if let splitVC = splitViewController(),
           let primaryView = splitVC.viewController(for: .primary)?.view,
           !primaryView.frame.contains(location) {
            print("[Sidebar] Tap outside sidebar detected - dismissing sidebar")
            hideSidebar()
        }
    }
    
    private func configureNavigationControllerForSidebar(_ navController: UINavigationController) {
        if #available(iOS 13.0, *) {
            let appearance = UINavigationBarAppearance()
            appearance.configureWithOpaqueBackground()
            appearance.backgroundColor = .systemBackground
            
            navController.navigationBar.standardAppearance = appearance
            navController.navigationBar.scrollEdgeAppearance = appearance
            navController.navigationBar.compactAppearance = appearance
        }
        
        navController.navigationBar.isTranslucent = false
        navController.view.backgroundColor = .systemBackground
    }
    
    private func hideNativeSplitViewButton(_ splitVC: UISplitViewController) {
        splitVC.presentsWithGesture = false
        
        if let secondaryNavController = splitVC.viewController(for: .secondary) as? UINavigationController {
            secondaryNavController.topViewController?.navigationItem.leftBarButtonItem = nil
            secondaryNavController.topViewController?.navigationItem.leftItemsSupplementBackButton = false
        }
        
        if let secondaryVC = splitVC.viewController(for: .secondary) {
            secondaryVC.navigationItem.leftBarButtonItem = nil
            secondaryVC.navigationItem.leftItemsSupplementBackButton = false
        }
        
        if #available(iOS 14.0, *) {
            splitVC.showsSecondaryOnlyButton = false
        }
    }
    
    private func restoreSecondaryViewNavigation(_ splitVC: UISplitViewController) {
        // Restore navigation items for secondary view if needed
        if let secondaryNavController = splitVC.viewController(for: .secondary) as? UINavigationController {
            secondaryNavController.topViewController?.navigationItem.leftItemsSupplementBackButton = true
        }
    }
    
    private func configureSplitViewControllerForCustomSidebar(_ splitVC: UISplitViewController) {
        splitVC.presentsWithGesture = enableNativeGestures
        
        if #available(iOS 14.0, *) {
            splitVC.showsSecondaryOnlyButton = false
            splitVC.preferredSplitBehavior = .tile
            splitVC.displayModeButtonVisibility = .never
        }
        
        // UISplitViewController setup for iPhone
        if UIDevice.current.userInterfaceIdiom == .phone {
            splitVC.preferredDisplayMode = .oneBesideSecondary
            splitVC.minimumPrimaryColumnWidth = 280
            splitVC.preferredPrimaryColumnWidthFraction = 0.4
            splitVC.maximumPrimaryColumnWidth = 320
        }
        
        DispatchQueue.main.async {
            self.hideNativeSplitViewButton(splitVC)
        }
    }

    // MARK: - Sidebar Logic
    private func splitViewController() -> UISplitViewController? {
        return UIApplication.shared.windows.first?.rootViewController as? UISplitViewController
    }

    private func showSidebar() {
        guard let splitVC = splitViewController() else { return }
        
        DispatchQueue.main.async {
            print("[Sidebar] ShowSidebar called: setting menu as primary and displayMode to .automatic")
            
            // Configure menu background
            self.configureMenuViewControllerBackground(self.menuViewController)
            
            // Set the menu as primary
            splitVC.setViewController(self.menuViewController, for: .primary)
            
            // Configure split view appearance
            splitVC.primaryBackgroundStyle = .sidebar
            
            if #available(iOS 14.0, *) {
                splitVC.preferredSplitBehavior = .tile
                splitVC.preferredDisplayMode = .oneBesideSecondary
                
                // DO NOT override trait collection - this is what breaks the secondary view layout
                // The secondary view should maintain its natural trait collection
            } else {
                splitVC.preferredDisplayMode = .allVisible
            }
            
            // iPhone specific configuration
            if UIDevice.current.userInterfaceIdiom == .phone {
                splitVC.minimumPrimaryColumnWidth = 280
                splitVC.preferredPrimaryColumnWidthFraction = 0.4
                splitVC.maximumPrimaryColumnWidth = 320
                
                if #available(iOS 14.0, *) {
                    splitVC.preferredSplitBehavior = .tile
                    splitVC.displayModeButtonVisibility = .never
                }
            }
            
            // Hide native split view button
            self.hideNativeSplitViewButton(splitVC)
            
            // Force layout update
            splitVC.view.setNeedsLayout()
            splitVC.view.layoutIfNeeded()
            
            print("[Sidebar] After setViewController:")
            print("  primary:", String(describing: splitVC.viewController(for: .primary)))
            print("  secondary:", String(describing: splitVC.viewController(for: .secondary)))
            print("  displayMode:", splitVC.displayMode.rawValue)
            print("  traitCollection:", splitVC.traitCollection)
            print("  iPhone detected:", UIDevice.current.userInterfaceIdiom == .phone)
            
            // Show primary
            splitVC.show(.primary)
            
            // Setup gestures
            self.setupGestures()
            
            self._visible = true
        }
    }

    private func hideSidebar() {
        guard _visible else { return }
        guard let splitVC = splitViewController() else { return }
        DispatchQueue.main.async {
            print("[Sidebar] HideSidebar called: setting displayMode to .secondaryOnly")
            self.removeGestures()
            splitVC.preferredDisplayMode = .secondaryOnly
            self._visible = false
        }
    }

    // MARK: - Defaults
    private static func defaultMenuController() -> UIViewController {
        let vc = UIViewController()
        vc.loadViewIfNeeded()
        vc.view.backgroundColor = .white
        vc.view.isOpaque = true
        vc.view.alpha = 1.0
        
        if #available(iOS 13.0, *) {
            vc.view.backgroundColor = .systemBackground
        }
        
        return vc
    }

    func setMenu(_ menu: UIViewController) {
        configureMenuViewControllerBackground(menu)
        menuViewController = menu
    }
}

// MARK: - UIGestureRecognizerDelegate
@available(iOS 14.5, *)
extension Sidebar: UIGestureRecognizerDelegate {
    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        guard _visible else { return false }
        
        if gestureRecognizer is UITapGestureRecognizer {
            guard let splitVC = splitViewController(),
                  let primaryView = splitVC.viewController(for: .primary)?.view else {
                return false
            }
            
            let location = touch.location(in: splitVC.view)
            return !primaryView.frame.contains(location)
        }
        
        return true
    }
    
    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return false
    }
}
