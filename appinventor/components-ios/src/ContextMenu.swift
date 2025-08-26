
//
//  ContextMenu.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 9/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//
//  ContextMenu component that holds MenuItems and can be attached to other components.
//  Context menu appears on long press of the registered view and offers actions that affect the selected content.
//  Each MenuItem has a Click() event that is triggered when the user selects it.
//  ContextMenu automatically refreshes when MenuItem properties change to reflect updates in the UI.
//

import UIKit

@available(iOS 14.5, *)
@objc(ContextMenu)
open class ContextMenu: NonvisibleComponent, ComponentContainer {
    private var menuItems: [MenuItem] = []
    private var _name: String = ""
    private var _nameStorage: NSString = ""  // NSString backing for YAIL compatibility
    
    @objc public override init(_ container: ComponentContainer) {
        super.init(container)
        print("[ContextMenu] Initialize called for container: \(String(describing: type(of: container)))")
        print("[ContextMenu] Self type: \(String(describing: type(of: self)))")
        print("[ContextMenu] Container type: \(String(describing: type(of: container)))")
        print("[ContextMenu] Container form: \(String(describing: container.form))")
        print("[ContextMenu] 🔍 Checking if this is a duplicate component...")
        
        // Check if there are other ContextMenu components
        if let form = container.form {
            let allComponents = form.getChildren()
            let contextMenus = allComponents.filter { String(describing: type(of: $0)).contains("ContextMenu") }
            let menus = allComponents.filter { String(describing: type(of: $0)).contains("Menu") && !String(describing: type(of: $0)).contains("ContextMenu") }
            
            print("[ContextMenu] Found \(contextMenus.count) ContextMenu components in form")
            print("[ContextMenu] Found \(menus.count) Menu components in form")
            
            if contextMenus.count > 1 {
                print("[ContextMenu] ⚠️ WARNING: Multiple ContextMenu components detected!")
                for (index, comp) in contextMenus.enumerated() {
                    print("[ContextMenu]   [\(index)] Type: \(String(describing: type(of: comp)))")
                }
            }
            
            if menus.count > 0 {
                print("[ContextMenu] ⚠️ WARNING: Menu components detected alongside ContextMenu!")
                for (index, comp) in menus.enumerated() {
                    print("[ContextMenu]   [\(index)] Type: \(String(describing: type(of: comp)))")
                }
            }
            
            // Print all component types for debugging
            print("[ContextMenu] All component types in form:")
            for (index, comp) in allComponents.enumerated() {
                let compType = String(describing: type(of: comp))
                print("[ContextMenu]   [\(index)] \(compType)")
            }
            
            // CRITICAL: If we detect other Menu components, this ContextMenu should not be created
            if menus.count > 0 {
                print("[ContextMenu] 🚨 CRITICAL: Menu components detected! This suggests a component creation issue.")
                print("[ContextMenu] 🚨 The web palette is creating both Menu and ContextMenu components.")
                print("[ContextMenu] 🚨 This ContextMenu should not exist alongside Menu components.")
            }
        }
        
        // Set a default name if none is provided
        if _name.isEmpty {
            let defaultName = "ContextMenu\(UUID().uuidString.prefix(8))"
            _name = defaultName
            _nameStorage = NSString(string: defaultName)  // Keep NSString in sync
            print("[ContextMenu] 🚨 CRITICAL: No name provided! Setting default name: '\(defaultName)'")
            print("[ContextMenu] 🚨 This suggests the web palette is not setting component names properly.")
        }
    }
    
    public func addChildComponent(_ component: NonvisibleComponent) {
        print("[ContextMenu] addChildComponent called with: \(String(describing: type(of: component)))")
        if let menuItem = component as? MenuItem {
            menuItems.append(menuItem)
            print("[ContextMenu] ✅ Successfully added MenuItem: '\(menuItem.Text)', total items: \(menuItems.count)")
            print("[ContextMenu] MenuItem details - Enabled: \(menuItem.Enabled), Visible: \(menuItem.Visible), Icon: '\(menuItem.Icon)'")
        } else {
            print("[ContextMenu] ❌ Component is not a MenuItem: \(String(describing: type(of: component)))")
        }
    }
    
    @objc open var Name: String {
        get {
            print("[ContextMenu] Name getter called, returning: '\(_name)'")
            
            // Get the call stack to see where this is being called from
            let callStack = Thread.callStackSymbols
            print("[ContextMenu] Name getter call stack:")
            for (index, symbol) in callStack.prefix(5).enumerated() {
                print("[ContextMenu]   [\(index)] \(symbol)")
            }
            
            return _name
        }
        set {
            print("[ContextMenu] Name setter called, setting to: '\(newValue)'")
            
            // Get the call stack to see where this is being called from
            let callStack = Thread.callStackSymbols
            print("[ContextMenu] Name setter call stack:")
            for (index, symbol) in callStack.prefix(5).enumerated() {
                print("[ContextMenu]   [\(index)] \(symbol)")
            }
            
            _name = newValue
            _nameStorage = NSString(string: newValue)  // Keep NSString in sync
            print("[ContextMenu] Name setter completed, _name is now: '\(_name)'")
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
        
        // Create a context menu interaction for the anchor view
        let interaction = UIContextMenuInteraction(delegate: ContextMenuDelegate(menu: menu))
        anchor.addInteraction(interaction)
        
        print("[ContextMenu] Context menu attached to view with \(actions.count) items")
    }
    
        @objc open func Show() {
        print("[ContextMenu] Show() called - but this method is disabled")
        print("[ContextMenu] ContextMenu only works with long-press gestures")
        print("[ContextMenu] Use long-press on the FloatingActionButton to show the context menu")
    }
    
    @objc open func ShowFromComponent(_ componentName: String) {
        print("[ContextMenu] ShowFromComponent('\(componentName)') called - but this method is disabled")
        print("[ContextMenu] ContextMenu only works with long-press gestures")
        print("[ContextMenu] Use long-press on the FloatingActionButton to show the context menu")
    }
    
    @objc open func Refresh() {
        print("[ContextMenu] Refresh called - updating context menu")
        print("[ContextMenu] Current menu items count: \(menuItems.count)")
        for (index, item) in menuItems.enumerated() {
            print("[ContextMenu] MenuItem \(index): Text='\(item.Text)', Enabled=\(item.Enabled), Visible=\(item.Visible)")
        }
        // Context menus are recreated each time they're shown, so no persistent update needed
    }
    
    @objc open func createUIMenu() -> UIMenu {
        return createUIMenu(title: "", image: nil, options: [])
    }
    
    @objc open func createUIMenu(title: String = "", image: UIImage? = nil, options: UIMenu.Options = []) -> UIMenu {
        print("[ContextMenu] createUIMenu called with \(menuItems.count) items")
        
        if menuItems.isEmpty {
            print("[ContextMenu] ⚠️ Warning: No menu items available!")
            return UIMenu(title: "No Items", children: [])
        }
        
        // Debug: Print all menu items
        for (index, item) in menuItems.enumerated() {
            print("[ContextMenu] MenuItem \(index): Text='\(item.Text)', Enabled=\(item.Enabled), Visible=\(item.Visible)")
        }
        
        let actions = menuItems.filter { $0.Visible }.map { item in
            // Safety check for MenuItem properties
            let itemText = item.Text.isEmpty ? "Menu Item" : item.Text
            print("[ContextMenu] Creating action for item: '\(itemText)' (Enabled: \(item.Enabled), Visible: \(item.Visible))")
            
            let action = UIAction(title: itemText, image: getIconForMenuItem(item)) { [weak self] _ in
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

        print("[ContextMenu] Created menu with \(actions.count) actions")
        return UIMenu(title: title, image: image, options: options, children: actions)
    }

    @objc open func ItemSelected(_ itemIndex: Int, _ item: MenuItem) {
        print("[ContextMenu] ItemSelected event triggered - Index: \(itemIndex), Item: \(item.Text)")
        EventDispatcher.dispatchEvent(of: self, called: "ItemSelected", arguments: itemIndex as AnyObject, item as AnyObject)
    }
    
    private func getIconForMenuItem(_ item: MenuItem) -> UIImage? {
        guard !item.Icon.isEmpty else { return nil }
        
        var iconImage: UIImage?
        
        // First try to load as a custom image using AssetManager
        if let customImage = AssetManager.shared.imageFromPath(path: item.Icon) {
            print("[ContextMenu] Loaded custom image for menu item: \(item.Icon)")
            iconImage = customImage
        }
        // Fallback to SF Symbols
        else if #available(iOS 13.0, *) {
            if let systemImage = UIImage(systemName: item.Icon) {
                print("[ContextMenu] Using SF Symbol for menu item: \(item.Icon)")
                iconImage = systemImage
            }
        }
        // Try as asset name
        else if let assetImage = UIImage(named: item.Icon) {
            print("[ContextMenu] Using asset image for menu item: \(item.Icon)")
            iconImage = assetImage
        }
        
        if iconImage == nil {
            print("[ContextMenu] Could not load icon for menu item: \(item.Icon)")
            return nil
        }
        
        // Configure icon for proper left alignment in menu
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
    
    // MARK: - ComponentContainer implementation
    public var form: Form? {
        print("[ContextMenu] form getter called, returning: \(String(describing: _form))")
        return _form
    }
    public var container: ComponentContainer? {
        print("[ContextMenu] container getter called, returning: \(String(describing: _form))")
        return _form
    }
    public func add(_ component: ViewComponent) {
        print("[ContextMenu] add called with ViewComponent: \(String(describing: type(of: component)))")
    }
    public func setChildWidth(of component: ViewComponent, to width: Int32) {
        print("[ContextMenu] setChildWidth called")
    }
    public func setChildHeight(of component: ViewComponent, to height: Int32) {
        print("[ContextMenu] setChildHeight called")
    }
    public func isVisible(component: ViewComponent) -> Bool {
        print("[ContextMenu] isVisible called")
        return false
    }
    public func setVisible(component: ViewComponent, to visibility: Bool) {
        print("[ContextMenu] setVisible called with: \(visibility)")
    }
    public func getChildren() -> [Component] {
        print("[ContextMenu] getChildren called, returning \(menuItems.count) items")
        return menuItems
    }
    @objc open var Width: Int32 { return 0 }
    @objc open var Height: Int32 { return 0 }
    
    // MARK: - Debug methods
    @objc open func DebugInfo() -> String {
        let info = """
        [ContextMenu Debug Info]
        - Name: \(_name)
        - Menu Items Count: \(menuItems.count)
        - Form: \(String(describing: _form))
        - Self Type: \(String(describing: type(of: self)))
        - Menu Items:
        """
        
        var itemsInfo = ""
        for (index, item) in menuItems.enumerated() {
            itemsInfo += "\n  [\(index)] Text: '\(item.Text)', Enabled: \(item.Enabled), Visible: \(item.Visible)"
        }
        
        return info + itemsInfo
    }
    
    @objc open func TestMenuItemAddition() -> String {
        print("[ContextMenu] TestMenuItemAddition called")
        print("[ContextMenu] Current menu items count: \(menuItems.count)")
        
        for (index, item) in menuItems.enumerated() {
            print("[ContextMenu] Test - MenuItem \(index): Text='\(item.Text)', Enabled=\(item.Enabled), Visible=\(item.Visible)")
        }
        
        return "ContextMenu has \(menuItems.count) menu items"
    }
    
    @objc open func GetComponentStatus() -> String {
        print("[ContextMenu] GetComponentStatus called")
        
        var status = "ContextMenu Status:\n"
        status += "- Name: '\(_name)'\n"
        status += "- Menu Items: \(menuItems.count)\n"
        status += "- Form: \(String(describing: _form))\n"
        
        if let form = _form {
            let allComponents = form.getChildren()
            let contextMenus = allComponents.filter { String(describing: type(of: $0)).contains("ContextMenu") }
            let menus = allComponents.filter { String(describing: type(of: $0)).contains("Menu") && !String(describing: type(of: $0)).contains("ContextMenu") }
            
            status += "- Total ContextMenu components: \(contextMenus.count)\n"
            status += "- Total Menu components: \(menus.count)\n"
            status += "- Total components in form: \(allComponents.count)\n"
            
            if menus.count > 0 {
                status += "- ⚠️ WARNING: Menu components detected alongside ContextMenu!\n"
                status += "- This suggests a component creation issue in the web palette.\n"
            }
        }
        
        print("[ContextMenu] Component status: \(status)")
        return status
    }
    
    @objc open func GetName() -> String {
        print("[ContextMenu] GetName() called, returning: '\(_name)'")
        return _name
    }
    
    @objc open func GetNameAsString() -> String {
        print("[ContextMenu] GetNameAsString() called, returning: '\(_name)'")
        return _name
    }
    
    @objc open func ToString() -> String {
        print("[ContextMenu] ToString() called, returning: '\(_name)'")
        return _name
    }
    
    // MARK: - YAIL String Bridge Compatibility Methods
    // These methods handle YAIL's attempts to treat this object as a string
    
    @objc open var length: Int {
        print("[ContextMenu] ⚠️ WARNING: length property accessed on ContextMenu!")
        print("[ContextMenu] This suggests the system is treating ContextMenu as a string")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack:")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        print("[ContextMenu] Current _name: '\(_name)'")
        print("[ContextMenu] Returning _name.count: \(_name.count)")
        return _name.count
    }
    
    @objc open func count() -> Int {
        print("[ContextMenu] ⚠️ WARNING: count() method called on ContextMenu!")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for count():")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        print("[ContextMenu] Current _name: '\(_name)'")
        print("[ContextMenu] Returning _name.count: \(_name.count)")
        return _name.count
    }
    
    @objc open func isEmpty() -> Bool {
        print("[ContextMenu] ⚠️ WARNING: isEmpty() method called on ContextMenu!")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for isEmpty():")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        print("[ContextMenu] Current _name: '\(_name)'")
        print("[ContextMenu] Returning _name.isEmpty: \(_name.isEmpty)")
        return _name.isEmpty
    }
    
    // CRITICAL FIX: The method YAIL is actually calling
    @objc open func _fastCStringContents(_ encoding: UInt) -> UnsafePointer<Int8>? {
        print("[ContextMenu] ⚠️ CRITICAL: _fastCStringContents(_:) method called on ContextMenu!")
        print("[ContextMenu] This is the exact method YAIL was failing to find!")
        print("[ContextMenu] Encoding parameter: \(encoding)")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for _fastCStringContents(_:):")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        print("[ContextMenu] Current _name: '\(_name)'")
        
        // Return the C string representation of the name
        // We need to ensure this pointer stays valid, so we use the NSString storage
        return _nameStorage.utf8String
    }
    
    // Fallback method without parameter (in case both are called)
    @objc open func _fastCStringContents() -> UnsafePointer<Int8>? {
        print("[ContextMenu] ⚠️ WARNING: _fastCStringContents() method called on ContextMenu!")
        print("[ContextMenu] This suggests the system is trying to bridge ContextMenu to NSString")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for _fastCStringContents():")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        print("[ContextMenu] Current _name: '\(_name)'")
        
        // Return the C string representation of the name using NSString storage
        return _nameStorage.utf8String
    }
    
    @objc open func UTF8String() -> UnsafePointer<Int8>? {
        print("[ContextMenu] ⚠️ WARNING: UTF8String() method called on ContextMenu!")
        print("[ContextMenu] This suggests the system is trying to bridge ContextMenu to NSString")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for UTF8String():")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        // Return the UTF8 string representation of the name using NSString storage
        return _nameStorage.utf8String
    }
    
    @objc open func cString(using encoding: UInt) -> UnsafePointer<Int8>? {
        print("[ContextMenu] ⚠️ WARNING: cString(using:) method called on ContextMenu!")
        print("[ContextMenu] This suggests the system is trying to bridge ContextMenu to NSString")
        print("[ContextMenu] Encoding: \(encoding)")
        
        // Get the call stack to see where this is being called from
        let callStack = Thread.callStackSymbols
        print("[ContextMenu] Call stack for cString(using:):")
        for (index, symbol) in callStack.prefix(10).enumerated() {
            print("[ContextMenu]   [\(index)] \(symbol)")
        }
        
        // Return the C string representation of the name using NSString storage
        return _nameStorage.utf8String
    }
    
    // Additional NSString compatibility methods
    @objc open func characterAtIndex(_ index: Int) -> unichar {
        print("[ContextMenu] ⚠️ WARNING: characterAtIndex(\(index)) called on ContextMenu!")
        return _nameStorage.character(at: index)
    }
    
    @objc open func substringFromIndex(_ from: Int) -> String {
        print("[ContextMenu] ⚠️ WARNING: substringFromIndex(\(from)) called on ContextMenu!")
        return _nameStorage.substring(from: from)
    }
    
    @objc open func substringToIndex(_ to: Int) -> String {
        print("[ContextMenu] ⚠️ WARNING: substringToIndex(\(to)) called on ContextMenu!")
        return _nameStorage.substring(to: to)
    }
    
    @objc open func substringWithRange(_ range: NSRange) -> String {
        print("[ContextMenu] ⚠️ WARNING: substringWithRange(\(range)) called on ContextMenu!")
        return _nameStorage.substring(with: range)
    }
    
    // Override description to return the name
    open override var description: String {
        return _name
    }
    
    open override var debugDescription: String {
        return "ContextMenu(name: '\(_name)', menuItems: \(menuItems.count))"
    }
    
    // Helper method to get component names
    private func getComponentName(_ component: Any) -> String? {
        let mirror = Mirror(reflecting: component)
        
        // Try common name properties
        for child in mirror.children {
            if let label = child.label,
               (label == "_name" || label == "name" || label == "_componentName" || label == "componentName") {
                
                if let stringValue = child.value as? String {
                    return stringValue
                }
                
                if let nsString = child.value as? NSString {
                    return nsString as String
                }
            }
        }
        
        // Try calling getName methods if they exist
        if let nsObject = component as? NSObject {
            let getNameMethods = ["GetName", "getName", "GetNameAsString", "ToString"]
            
            for methodName in getNameMethods {
                let selector = NSSelectorFromString(methodName)
                if nsObject.responds(to: selector) {
                    if let result = nsObject.perform(selector)?.takeUnretainedValue() as? String {
                        return result
                    }
                }
            }
        }
        
        return nil
    }
    

    

    

}

// MARK: - UIContextMenuInteractionDelegate wrapper
@available(iOS 14.5, *)
private class ContextMenuDelegate: NSObject, UIContextMenuInteractionDelegate {
    private let menu: UIMenu
    
    init(menu: UIMenu) {
        self.menu = menu
        super.init()
    }
    
    public func contextMenuInteraction(_ interaction: UIContextMenuInteraction,
                                      configurationForMenuAtLocation location: CGPoint) -> UIContextMenuConfiguration? {
        return UIContextMenuConfiguration(identifier: nil, previewProvider: nil) { _ in
            return self.menu
        }
    }
}
