//
//  Menu.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 9/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//
//  Menu component with integrated click events for MenuItems.
//  Each MenuItem has a Click() event that is triggered when the user selects it.
//  The Menu component also has an ItemSelected() event that provides the index and item reference.
//  Menu automatically refreshes when MenuItem properties change to reflect updates in the UI.
//

import UIKit

@available(iOS 14.5, *)
@objc(Menu)
open class Menu: NonvisibleComponent, ComponentContainer {
    private var menuItems: [MenuItem] = []
    private weak var anchorButton: UIButton? // Track anchor to reattach menus if needed
    private static var globalMenuButton: UIBarButtonItem? // Static to ensure only one button ever exists
    private static var globalCustomButton: UIButton? // Static custom button
    private static var buttonCreated: Bool = false // Static flag
    private static var actionBarButtons: [UIBarButtonItem] = [] // Icons shown directly on the action bar
    private static var actionBarMenuItems: [MenuItem] = [] // Backing items for action bar buttons (same order)
    private static let maxActionBarItems: Int = 3 // Basic cap; overflow will contain the rest

    
    public func addChildComponent(_ component: NonvisibleComponent) {
        if let menuItem = component as? MenuItem {
            menuItems.append(menuItem)
            print("[Menu] Added MenuItem: \(menuItem.Text), total items: \(menuItems.count)")
            
            updateMenuOnly()
        }
    }
    
    public func removeChildComponent(_ component: NonvisibleComponent) {
        if let menuItem = component as? MenuItem {
            if let index = menuItems.firstIndex(where: { $0 === menuItem }) {
                menuItems.remove(at: index)
                print("[Menu] Removed MenuItem: \(menuItem.Text), total items: \(menuItems.count)")
                
                // Update the menu and check if we need to hide the button
                updateMenuOnly()
            }
        }
    }

    
    @objc open func ShowFrom(_ anchor: UIView) {
        let filteredItems = menuItems.filter { $0.Visible }

        let actions = filteredItems.map { item in
            let action = UIAction(title: item.Text,
                     image: getIconForMenuItem(item),
                     state: .off) { [weak self] _ in
                // Trigger the click event
                item.Click()
                self?.AfterSelecting(item.Text)
                
                // Refresh the menu to show updated properties
                DispatchQueue.main.async {
                    self?.Refresh()
                }
            }
            action.attributes = item.Enabled ? [] : [.disabled]
            return action
        }

        let menu = UIMenu(title: "", children: actions)

        if let button = anchor as? UIButton {
            button.menu = menu
            button.showsMenuAsPrimaryAction = true
            anchorButton = button
        } else {
            let interaction = UIContextMenuInteraction(delegate: self)
            anchor.addInteraction(interaction)
        }
    }
    
    
    @objc open func Show() {
        
        if let form = _form {
            for child in form.getChildren() {
                if let button = child as? ViewComponent, button.view is UIButton {
                    ShowFrom(button.view)
                    return
                }
            }
        }
    }

    
    @objc open func AfterSelecting(_ item: String) {
        EventDispatcher.dispatchEvent(of: self, called: "AfterSelecting", arguments: item as AnyObject)
    }

    
    @objc open func Refresh() {
        print("[Menu] Refresh called - updating all menus")
        
        // Update anchor button menu if it exists
        if let button = anchorButton {
            attachTo(button: button)
        }
        
        // Update navigation bar menu
        updateNavigationBarMenu()
        
        // Update any other menu instances
        updateMenuOnly()
        
        // Force UI update
        DispatchQueue.main.async {
            if let form = self._form {
                form.view.setNeedsLayout()
                form.view.layoutIfNeeded()
            }
        }
    }
    
    @objc open func ForceRefresh() {
        print("[Menu] ForceRefresh called - recreating all menus")
        
        // Clear existing menus completely
        if let button = anchorButton {
            button.menu = nil
            print("[Menu] Cleared anchor button menu")
        }
        
        // Clear global menu
        if let customButton = Menu.globalCustomButton {
            customButton.menu = nil
            print("[Menu] Cleared global menu")
        }
        
        // Check if we need to remove the global button due to no visible items
        let hasVisibleItems = menuItems.contains { $0.Visible }
        if !hasVisibleItems && Menu.buttonCreated, let form = _form, let navigationController = form.navigationController {
            print("[Menu] No visible menu items - removing global button during ForceRefresh")
            
            // Remove the global button from navigation bar
            var existingButtons = navigationController.navigationBar.topItem?.rightBarButtonItems ?? []
            existingButtons.removeAll { $0 === Menu.globalMenuButton }
            navigationController.navigationBar.topItem?.rightBarButtonItems = existingButtons
            
            // Clear the button references
            Menu.globalMenuButton = nil
            Menu.globalCustomButton = nil
            Menu.buttonCreated = false
            Menu.actionBarButtons.removeAll()
            Menu.actionBarMenuItems.removeAll()
            
            print("[Menu] Global button removed during ForceRefresh, total right items: \(existingButtons.count)")
            return
        }
        
        // Force immediate recreation
        DispatchQueue.main.async { [weak self] in
            self?.recreateAllMenus()
        }
    }
    
    private func recreateAllMenus() {
        print("[Menu] recreateAllMenus called")
        
        // Recreate anchor button menu
        if let button = anchorButton {
            attachTo(button: button)
            print("[Menu] Recreated anchor button menu")
        }
        
        // Recreate navigation bar menu
        updateNavigationBarMenu()
        
        // Force UI update
        if let form = _form {
            form.view.setNeedsLayout()
            form.view.layoutIfNeeded()
            print("[Menu] Forced UI layout update")
        }
    }

    // MARK: - Helpers for splitting action bar vs overflow items
    private func splitActionBarAndOverflowItems() -> (actionBarItems: [MenuItem], overflowItems: [MenuItem]) {
        // Visible items that requested action bar placement
        let requestedActionBar = menuItems.filter { $0.Visible && $0.ShowOnActionBar }
        // Cap how many can be shown directly
        let actionBar = Array(requestedActionBar.prefix(Menu.maxActionBarItems))
        // Everything else (including items that requested action bar but are over the cap) goes to overflow
        let overflow = menuItems.filter { candidate in
            candidate.Visible && !actionBar.contains { ab in ab === candidate }
        }
        return (actionBarItems: actionBar, overflowItems: overflow)
    }

    @objc private func actionBarItemTapped(_ sender: UIBarButtonItem) {
        // Find which item was tapped based on our stored arrays
        guard let index = Menu.actionBarButtons.firstIndex(where: { $0 === sender }),
              index < Menu.actionBarMenuItems.count else {
            return
        }
        let item = Menu.actionBarMenuItems[index]
        guard item.Enabled else { return }
        // Trigger events consistent with overflow actions
        item.Click()
        ItemSelected(menuItems.firstIndex(where: { $0 === item }) ?? index, item)
        AfterSelecting(item.Text)
        DispatchQueue.main.async { [weak self] in
            self?.Refresh()
        }
    }


    @objc open func createUIMenu(title: String = "", image: UIImage? = nil, options: UIMenu.Options = []) -> UIMenu {
        print("[Menu] createUIMenu called with \(menuItems.count) items")
        
        let actions = menuItems.filter { $0.Visible }.map { item in
            print("[Menu] Creating action for item: '\(item.Text)' (Enabled: \(item.Enabled), Visible: \(item.Visible))")
            
            let action = UIAction(title: item.Text, image: getIconForMenuItem(item)) { [weak self] _ in
                // Trigger the click event
                item.Click()
                self?.AfterSelecting(item.Text)
                
                // Refresh the menu to show updated properties
                DispatchQueue.main.async {
                    self?.ForceRefresh()
                }
            }
            action.attributes = item.Enabled ? [] : [.disabled]
            return action
        }

        print("[Menu] Created menu with \(actions.count) actions")
        return UIMenu(title: title, image: image, options: options, children: actions)
    }

    
    @objc open func attachTo(button: UIButton, showsMenuAsPrimaryAction: Bool = true) {
        button.menu = createUIMenu()
        button.showsMenuAsPrimaryAction = showsMenuAsPrimaryAction
        anchorButton = button
    }

    @objc open func attachTo(barButtonItem: UIBarButtonItem) {
        barButtonItem.menu = createUIMenu()
    }

    
    @objc open var Items: String {
        get {
            return menuItems.map { $0.Text }.joined(separator: ",")
        }
        set {
            
            let itemTexts = newValue.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }
            menuItems.removeAll()
            for text in itemTexts where !text.isEmpty {
                let menuItem = MenuItem(self)
                menuItem.Text = text
                menuItems.append(menuItem)
            }
            print("[Menu] Items property set, total items: \(menuItems.count)")
            updateNavigationBarMenu()
        }
    }
    
    @objc open var ShowAbout: String = ""
    @objc open var ShowStop: String = ""
    
    
    @objc open func Initialize() {
        print("[Menu] Initialize called")
        updateNavigationBarMenu()
        EventDispatcher.dispatchEvent(of: self, called: "Initialize")
    }

    
    private func updateMenuOnly() {
        print("[Menu] updateMenuOnly called, global buttonCreated: \(Menu.buttonCreated)")
        
        // Check if we have any visible menu items
        let hasVisibleItems = menuItems.contains { $0.Visible }
        
        // If no visible items, remove the button
        if !hasVisibleItems {
            if Menu.buttonCreated, let form = _form, let navigationController = form.navigationController {
                print("[Menu] No visible menu items - removing global button")
                
                // Remove the global button from navigation bar
                var existingButtons = navigationController.navigationBar.topItem?.rightBarButtonItems ?? []
                existingButtons.removeAll { $0 === Menu.globalMenuButton }
                navigationController.navigationBar.topItem?.rightBarButtonItems = existingButtons
                
                // Clear the button references
                Menu.globalMenuButton = nil
                Menu.globalCustomButton = nil
                Menu.buttonCreated = false
                Menu.actionBarButtons.removeAll()
                Menu.actionBarMenuItems.removeAll()
                
                print("[Menu] Global button removed from updateMenuOnly, total right items: \(existingButtons.count)")
                return
            }
        }
        
        guard Menu.buttonCreated, let customButton = Menu.globalCustomButton else {
            print("[Menu] Global button not created yet, skipping menu update")
            return
        }
        
        // Use overflow items for the global menu button
        let (_, overflowItems) = splitActionBarAndOverflowItems()
        let actions = overflowItems.map { item in
            UIAction(title: item.Text,
                     image: getIconForMenuItem(item),
                     state: .off) { [weak self] _ in
                // Trigger the click event
                item.Click()
                self?.ItemSelected(self?.menuItems.firstIndex(where: { $0 === item }) ?? 0, item)
                
                // Refresh the menu to show updated properties
                DispatchQueue.main.async {
                    self?.Refresh()
                }
            }
        }
        
        let menu = UIMenu(title: "", children: actions)
        
        if #available(iOS 14.0, *) {
            customButton.menu = menu
            print("[Menu] Updated global menu with \(actions.count) actions")
        }
    }
    
    
    private func updateNavigationBarMenu() {
        guard let form = _form else { 
            print("[Menu] No form available")
            return 
        }
        
        print("[Menu] updateNavigationBarMenu called, global buttonCreated: \(Menu.buttonCreated)")
        
        // Check if we have any visible menu items
        let hasVisibleItems = menuItems.contains { $0.Visible }
        print("[Menu] Has visible menu items: \(hasVisibleItems)")
        
        // If no visible items, remove the button if it exists
        if !hasVisibleItems {
            if Menu.buttonCreated, let navigationController = form.navigationController {
                print("[Menu] No visible menu items - removing global button")
                
                // Remove the global button from navigation bar
                var existingButtons = navigationController.navigationBar.topItem?.rightBarButtonItems ?? []
                existingButtons.removeAll { $0 === Menu.globalMenuButton }
                navigationController.navigationBar.topItem?.rightBarButtonItems = existingButtons
                
                // Clear the button references
                Menu.globalMenuButton = nil
                Menu.globalCustomButton = nil
                Menu.buttonCreated = false
                Menu.actionBarButtons.removeAll()
                Menu.actionBarMenuItems.removeAll()
                
                print("[Menu] Global button removed, total right items: \(existingButtons.count)")
                return
            }
        }
        
        // Create global button only if we don't have one and we have visible items
        if !Menu.buttonCreated && hasVisibleItems {
            print("[Menu] Creating global button - FIRST TIME ONLY")
            
            Menu.globalCustomButton = UIButton(type: .system)
            Menu.globalCustomButton?.setImage(UIImage(systemName: "ellipsis.circle"), for: .normal)
            Menu.globalCustomButton?.tintColor = .systemBlue
            
            if #available(iOS 14.0, *) {
                Menu.globalCustomButton?.showsMenuAsPrimaryAction = true
            }
            
            Menu.globalMenuButton = UIBarButtonItem(customView: Menu.globalCustomButton!)
            
            if let navigationController = form.navigationController {
                var existingButtons = navigationController.navigationBar.topItem?.rightBarButtonItems ?? []
                // Ensure we don't duplicate if re-entering
                existingButtons.removeAll { $0 === Menu.globalMenuButton }
                if let menuButton = Menu.globalMenuButton {
                    existingButtons.insert(menuButton, at: 0)
                }
                navigationController.navigationBar.topItem?.rightBarButtonItems = existingButtons
                print("[Menu] Added global button before other right bar buttons, total buttons: \(existingButtons.count)")
            }
            
            Menu.buttonCreated = true
        }
        
        // Only proceed with menu updates if we have a button and visible items
        guard Menu.buttonCreated && hasVisibleItems else {
            print("[Menu] Skipping menu updates - no button or no visible items")
            return
        }
        
        // Build action bar items (icons) and overflow items
        let (actionBarItems, overflowItems) = splitActionBarAndOverflowItems()

        // 1) Update overflow menu on the global button
        if #available(iOS 14.0, *), let customButton = Menu.globalCustomButton {
            let actions = overflowItems.map { item in
                let action = UIAction(title: item.Text,
                         image: getIconForMenuItem(item),
                         state: .off) { [weak self] _ in
                    // Trigger the click event
                    item.Click()
                    self?.ItemSelected(self?.menuItems.firstIndex(where: { $0 === item }) ?? 0, item)
                    // Refresh the menu to show updated properties
                    DispatchQueue.main.async {
                        self?.Refresh()
                    }
                }
                action.attributes = item.Enabled ? [] : [.disabled]
                return action
            }
            let menu = UIMenu(title: "", children: actions)
            customButton.menu = menu
            print("[Menu] Updated global menu (overflow) with \(actions.count) actions")
        }

        // 2) Create UIBarButtonItems for action bar items
        let previousActionButtons = Menu.actionBarButtons
        var newActionButtons: [UIBarButtonItem] = []
        for item in actionBarItems {
            if let icon = getIconForMenuItem(item) {
                let b = UIBarButtonItem(image: icon, style: .plain, target: self, action: #selector(actionBarItemTapped(_:)))
                b.isEnabled = item.Enabled
                newActionButtons.append(b)
            } else {
                // Fallback to text if no icon available
                let b = UIBarButtonItem(title: item.Text, style: .plain, target: self, action: #selector(actionBarItemTapped(_:)))
                b.isEnabled = item.Enabled
                newActionButtons.append(b)
            }
        }
        Menu.actionBarButtons = newActionButtons
        Menu.actionBarMenuItems = actionBarItems

        // 3) Install buttons on the navigation bar (overflow button first, then icons)
        if let navigationController = form.navigationController {
            var existing = navigationController.navigationBar.topItem?.rightBarButtonItems ?? []
            // Remove old items added by Menu
            existing.removeAll { barBtn in
                (Menu.globalMenuButton != nil && barBtn === Menu.globalMenuButton) || previousActionButtons.contains { pb in pb === barBtn }
            }
            var ours: [UIBarButtonItem] = []
            if let menuButton = Menu.globalMenuButton {
                ours.append(menuButton) // keep overflow at the far right
            }
            ours.append(contentsOf: Menu.actionBarButtons)
            navigationController.navigationBar.topItem?.rightBarButtonItems = ours + existing
            print("[Menu] Installed \(Menu.actionBarButtons.count) action bar item(s) + overflow button. Total right items: \((ours + existing).count)")
        }
    }
    
    @objc private func menuButtonTapped() {
        
        print("Menu button tapped - native iOS menu should appear")
    }
    
    
    @objc open func ItemSelected(_ itemIndex: Int, _ item: MenuItem) {
        print("[Menu] ItemSelected event triggered - Index: \(itemIndex), Item: \(item.Text)")
        EventDispatcher.dispatchEvent(of: self, called: "ItemSelected", arguments: itemIndex as AnyObject, item as AnyObject)
    }
    
    
  
    
    @objc open func ShowMenu() {
        Show()
    }
    
    
    public var form: Form? { return _form }
    public var container: ComponentContainer? { return _form }
    public func add(_ component: ViewComponent) {}
    public func setChildWidth(of component: ViewComponent, to width: Int32) {}
    public func setChildHeight(of component: ViewComponent, to height: Int32) {}
    public func isVisible(component: ViewComponent) -> Bool { return false }
    public func setVisible(component: ViewComponent, to visibility: Bool) {}
    public func getChildren() -> [Component] { return menuItems }
    @objc open var Width: Int32 { return 0 }
    @objc open var Height: Int32 { return 0 }

    
    private func getIconForMenuItem(_ item: MenuItem) -> UIImage? {
        guard !item.Icon.isEmpty else { return nil }
        
        var iconImage: UIImage?
        
        // First try to load as a custom image using AssetManager
        if let customImage = AssetManager.shared.imageFromPath(path: item.Icon) {
            print("[Menu] Loaded custom image for menu item: \(item.Icon)")
            iconImage = customImage
        }
        // Fallback to SF Symbols
        else if #available(iOS 13.0, *) {
            if let systemImage = UIImage(systemName: item.Icon) {
                print("[Menu] Using SF Symbol for menu item: \(item.Icon)")
                iconImage = systemImage
            }
        }
        // Try as asset name
        else if let assetImage = UIImage(named: item.Icon) {
            print("[Menu] Using asset image for menu item: \(item.Icon)")
            iconImage = assetImage
        }
        
        if iconImage == nil {
            print("[Menu] Could not load icon for menu item: \(item.Icon)")
            return nil
        }
        
        // Configure icon for proper left alignment in menu
        // Ensure icon is properly sized for menu display
        let menuIconSize: CGFloat = 20.0
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: menuIconSize, height: menuIconSize))
        
        let resizedIcon = renderer.image { context in
            iconImage?.draw(in: CGRect(x: 0, y: 0, width: menuIconSize, height: menuIconSize))
        }
        
        // Configure for left alignment
        if #available(iOS 13.0, *) {
            resizedIcon.withRenderingMode(.alwaysTemplate)
        }
        
        return resizedIcon
    }

    @objc open func TestMenuUpdate() {
        print("[Menu] TestMenuUpdate called")
        print("[Menu] Current menu items:")
        for (index, item) in menuItems.enumerated() {
            print("  [\(index)] Text: '\(item.Text)', Enabled: \(item.Enabled), Visible: \(item.Visible), Icon: '\(item.Icon)'")
        }
        
        // Force a complete menu rebuild
        ForceRefresh()
    }

    @objc open func ShowUpdatedMenu() {
        print("[Menu] ShowUpdatedMenu called")
        
        // Force refresh first
        ForceRefresh()
        
        // Then show the menu again
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak self] in
            self?.Show()
        }
    }

    @objc open func TestMenuItemTextSetter() {
        print("[Menu] TestMenuItemTextSetter called")
        print("[Menu] Total menu items: \(menuItems.count)")
        
        for (index, item) in menuItems.enumerated() {
            print("[Menu] Testing MenuItem[\(index)]:")
            let originalText = item.Text
            let testText = "Test Text \(index + 1)"
            
            print("  Original Text: '\(originalText)'")
            print("  Setting Text to: '\(testText)'")
            
            // Test the setter using direct property assignment
            item.Text = testText
            
            print("  Text after set: '\(item.Text)'")
            print("  Getter result: '\(item.getText())'")
            
            // Verify it worked
            if item.Text == testText {
                print("  ✅ Text setter working for MenuItem[\(index)]")
            } else {
                print("  ❌ Text setter NOT working for MenuItem[\(index)]")
            }
            
            // Restore original text
            item.Text = originalText
            print("  Restored Text: '\(item.Text)'")
        }
    }
    
    @objc open func UpdateButtonVisibility() {
        print("[Menu] UpdateButtonVisibility called")
        print("[Menu] Total menu items: \(menuItems.count)")
        
        let visibleItems = menuItems.filter { $0.Visible }
        print("[Menu] Visible menu items: \(visibleItems.count)")
        
        for (index, item) in visibleItems.enumerated() {
            print("[Menu] Visible item[\(index)]: '\(item.Text)'")
        }
        
        // Force update the navigation bar menu
        updateNavigationBarMenu()
        
        print("[Menu] Button visibility updated")
    }
    
    @objc open func ClearAllMenuItems() {
        print("[Menu] ClearAllMenuItems called")
        print("[Menu] Clearing \(menuItems.count) menu items")
        
        menuItems.removeAll()
        
        // Force update to remove the button
        updateNavigationBarMenu()
        
        print("[Menu] All menu items cleared")
    }
}


@available(iOS 14.5, *)
extension Menu: UIContextMenuInteractionDelegate {
    public func contextMenuInteraction(_ interaction: UIContextMenuInteraction,
                                       configurationForMenuAtLocation location: CGPoint) -> UIContextMenuConfiguration? {
        return UIContextMenuConfiguration(identifier: nil, previewProvider: nil) { _ in
            return self.createUIMenu()
        }
    }
}
