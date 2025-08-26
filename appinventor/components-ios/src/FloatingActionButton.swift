//
//  FloatingActionButton.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 07/07/25.
//  Updated to properly link ContextMenu in MIT App Inventor
//


import UIKit
import ObjectiveC

@available(iOS 14.5, *)
@objc(FloatingActionButton)
open class FloatingActionButton: ViewComponent, AbstractMethodsForViewComponent {
    private let _view = UIView()
    private let button = UIButton(type: .custom)
    public var onTap: (() -> Void)?
    private var heightConstraint: NSLayoutConstraint?
    private var widthConstraint: NSLayoutConstraint?
    private var _icon: String = ""
    private var _contextMenuSelector: String = ""
    private var _linkedContextMenu: Any?
    private var contextMenuInteraction: UIContextMenuInteraction?

    // MARK: Init
    @objc public override init(_ parent: ComponentContainer) {
        super.init(parent)
        super.setDelegate(self)
        parent.add(self)
        setupButton()
        setupContextMenu()
        
        if let parentView = parent.form?.view {
            _view.translatesAutoresizingMaskIntoConstraints = false
            parentView.addSubview(_view)
            let trailing = _view.trailingAnchor.constraint(equalTo: parentView.trailingAnchor, constant: -24)
            let bottom = _view.bottomAnchor.constraint(equalTo: parentView.bottomAnchor, constant: -24)
            widthConstraint = _view.widthAnchor.constraint(equalToConstant: 56)
            heightConstraint = _view.heightAnchor.constraint(equalToConstant: 56)
            NSLayoutConstraint.activate([trailing, bottom, widthConstraint!, heightConstraint!])
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
        
        // Prevent visual feedback during context menu interaction
        button.layer.borderWidth = 0
        button.layer.borderColor = UIColor.clear.cgColor

        if #available(iOS 13.0, *) {
            button.setImage(UIImage(systemName: "plus"), for: .normal)
        } else {
            button.setTitle("+", for: .normal)
        }
        
        // Disable default button highlighting and visual feedback
        button.adjustsImageWhenHighlighted = false
        button.adjustsImageWhenDisabled = false

        button.addTarget(self, action: #selector(tapAction), for: .touchUpInside)
        _view.addSubview(button)
        NSLayoutConstraint.activate([
            button.leadingAnchor.constraint(equalTo: _view.leadingAnchor),
            button.trailingAnchor.constraint(equalTo: _view.trailingAnchor),
            button.topAnchor.constraint(equalTo: _view.topAnchor),
            button.bottomAnchor.constraint(equalTo: _view.bottomAnchor)
        ])
        
        // Set up context menu interaction
        setupContextMenu()
    }

    private func setupContextMenu() {
        if #available(iOS 13.0, *) {
            print("[FAB] Setting up context menu interaction")
            contextMenuInteraction = UIContextMenuInteraction(delegate: self)
            _view.addInteraction(contextMenuInteraction!)
            print("[FAB] ✅ Context menu interaction set up successfully")
        }
    }

    @objc private func tapAction() {
        Click()
        onTap?()
    }

    open override var view: UIView { _view }

    // MARK: - Override to remove unwanted properties
    // Remove Height property - FloatingActionButton has fixed size
    @objc open override var Height: Int32 {
        get { 
            print("[FAB] Height property is not available for FloatingActionButton")
            return 56 // Return default size
        }
        set {
            print("[FAB] Height property is not available for FloatingActionButton")
            // Do nothing - ignore attempts to set height
        }
    }

    // Remove Width property - FloatingActionButton has fixed size
    @objc open override var Width: Int32 {
        get { 
            print("[FAB] Width property is not available for FloatingActionButton")
            return 56 // Return default size
        }
        set {
            print("[FAB] Width property is not available for FloatingActionButton")
            // Do nothing - ignore attempts to set width
        }
    }

    // Remove HeightPercent functionality
    @objc open override func setHeightPercent(_ toPercent: Int32) {
        print("[FAB] HeightPercent is not available for FloatingActionButton")
        // Do nothing - ignore attempts to set height percent
    }

    // Remove WidthPercent functionality
    @objc open override func setWidthPercent(_ toPercent: Int32) {
        print("[FAB] WidthPercent is not available for FloatingActionButton")
        // Do nothing - ignore attempts to set width percent
    }

    // Remove Visible property - FloatingActionButton is always visible
    @objc open override var Visible: Bool {
        get { 
            print("[FAB] Visible property is not available for FloatingActionButton")
            return true // Always return true
        }
        set {
            print("[FAB] Visible property is not available for FloatingActionButton")
            // Do nothing - ignore attempts to set visibility
        }
    }

    // MARK: - Internal size management (for corner radius updates)
    private func updateCornerRadius() {
        button.layer.cornerRadius = min(_view.bounds.width, _view.bounds.height) / 2.0
    }

    // MARK: - Icon
    @objc open var Icon: String {
        get { _icon }
        set {
            _icon = newValue
            if _icon.isEmpty {
                button.setImage(nil, for: .normal)
                button.setTitle("+", for: .normal)
                return
            }
            if let img = AssetManager.shared.imageFromPath(path: _icon) {
                button.setImage(img, for: .normal); return
            }
            if #available(iOS 13.0, *), let sys = UIImage(systemName: _icon) {
                button.setImage(sys, for: .normal); return
            }
            if let img = UIImage(named: _icon) {
                button.setImage(img, for: .normal); return
            }
            button.setImage(nil, for: .normal)
            button.setTitle(_icon, for: .normal)
        }
    }

    // MARK: - ContextMenu Selector - UPDATED FOR COMPATIBILITY
    @objc open var ContextMenuSelector: String {
        get {
            print("[FAB] ContextMenuSelector getter called, returning: '\(_contextMenuSelector)'")
            return _contextMenuSelector
        }
        set {
            print("[FAB] ContextMenuSelector setter called with: '\(newValue)'")
            print("[FAB] ContextMenuSelector setter - newValue type: \(String(describing: type(of: newValue)))")
            _contextMenuSelector = newValue
            findAndLinkContextMenu()
        }
    }

    // UPDATED: Better handling for direct component references
    @objc open func ContextMenuSelector(_ value: Any) {
        print("[FAB] ContextMenuSelector(_:) called with value: \(String(describing: value))")
        print("[FAB] Value type: \(String(describing: type(of: value)))")
        
        // PRIORITY: Handle direct ContextMenu component objects
        if let component = value as? NSObject,
           String(describing: type(of: component)).contains("ContextMenu") {
            print("[FAB] ✅ Direct ContextMenu component received!")
            _linkedContextMenu = component
            
            // Try to get the component name for reference
            if let name = getComponentName(component) {
                _contextMenuSelector = name
                print("[FAB] ✅ Stored ContextMenu with name: '\(name)'")
            } else {
                // Fallback to type name
                _contextMenuSelector = String(describing: type(of: component))
                print("[FAB] ✅ Stored ContextMenu with type name: '\(_contextMenuSelector)'")
            }
            
            // Ensure context menu interaction is set up
            if contextMenuInteraction == nil {
                print("[FAB] Setting up context menu interaction for received ContextMenu")
                setupContextMenu()
            }
            
            return
        }
        
        // Handle string-based lookup
        let stringValue: String
        if let str = value as? String {
            stringValue = str
            print("[FAB] Value is String: '\(str)'")
        }
        else if let bool = value as? Bool {
            stringValue = bool ? "true" : "false"
            print("[FAB] Value is Bool: \(bool)")
        }
        else if let num = value as? NSNumber {
            stringValue = num.stringValue
            print("[FAB] Value is NSNumber: \(num)")
        }
        else {
            stringValue = String(describing: value)
            print("[FAB] Value is unknown type, using description: '\(stringValue)'")
        }
        
        print("[FAB] Final stringValue: '\(stringValue)'")
        _contextMenuSelector = stringValue
        findAndLinkContextMenu()
    }

    @objc open func SetContextMenuSelector(_ value: Any) {
        print("[FAB] SetContextMenuSelector(_:) called - delegating to ContextMenuSelector(_:)")
        ContextMenuSelector(value)
    }

    private func findAndLinkContextMenu() {
        guard !_contextMenuSelector.isEmpty else {
            _linkedContextMenu = nil
            print("[FAB] ContextMenuSelector is empty, clearing linked menu")
            return
        }
        
        // If we already have a direct component reference, validate it
        if let existingMenu = _linkedContextMenu,
           String(describing: type(of: existingMenu)).contains("ContextMenu") {
            print("[FAB] ✅ Using existing direct ContextMenu reference")
            return
        }
        
        guard let form = _container?.form else {
            print("[FAB] ❌ No form available for search")
            return
        }
 
        print("[FAB] 🔍 Searching for ContextMenu: '\(_contextMenuSelector)'")
        
        // Search strategy 1: Look in non-visible components (where ContextMenu should be)
        let formMirror = Mirror(reflecting: form)
        for child in formMirror.children {
            if let label = child.label,
               (label.contains("nonvisible") || label.contains("Nonvisible") ||
                label.contains("NonVisible") || label.contains("nonVisibleComponents")) {
                print("[FAB] 🔍 Found non-visible components collection: \(label)")
                
                if let nonVisibleComponents = child.value as? [Any] {
                    print("[FAB] 📦 Non-visible components count: \(nonVisibleComponents.count)")
                    
                    for (index, component) in nonVisibleComponents.enumerated() {
                        let componentType = String(describing: type(of: component))
                        print("[FAB] 🔍 Checking component \(index): \(componentType)")
                        
                        if componentType.contains("ContextMenu") {
                            print("[FAB] 🎯 Found ContextMenu component!")
                            
                            if let name = getComponentName(component), name == _contextMenuSelector {
                                print("[FAB] ✅ Name matches! Linking ContextMenu: '\(name)'")
                                _linkedContextMenu = component
                                
                                // Ensure context menu interaction is set up
                                if contextMenuInteraction == nil {
                                    print("[FAB] Setting up context menu interaction")
                                    setupContextMenu()
                                }
                                
                                return
                            } else {
                                let foundName = getComponentName(component) ?? "unknown"
                                print("[FAB] ❌ Name mismatch. Found: '\(foundName)', Looking for: '\(_contextMenuSelector)'")
                            }
                        }
                    }
                } else {
                    print("[FAB] ❌ Could not cast non-visible components to array")
                }
            }
        }
        
        // Search strategy 2: Check visible components as fallback
        print("[FAB] 🔍 Fallback: Checking visible components...")
        let visibleComponents = form.getChildren()
        print("[FAB] 📦 Visible components count: \(visibleComponents.count)")
        
        for (index, child) in visibleComponents.enumerated() {
            let componentType = String(describing: type(of: child))
            print("[FAB] 🔍 Visible component \(index): \(componentType)")
            
            if componentType.contains("ContextMenu") {
                print("[FAB] 🎯 Found ContextMenu in visible components!")
                
                if let name = getComponentName(child), name == _contextMenuSelector {
                    print("[FAB] ✅ Name matches! Linking ContextMenu: '\(name)'")
                    _linkedContextMenu = child
                    
                    // Ensure context menu interaction is set up
                    if contextMenuInteraction == nil {
                        print("[FAB] Setting up context menu interaction")
                        setupContextMenu()
                    }
                    
                    return
                } else {
                    let foundName = getComponentName(child) ?? "unknown"
                    print("[FAB] ❌ Name mismatch. Found: '\(foundName)', Looking for: '\(_contextMenuSelector)'")
                }
            }
        }
        
        print("[FAB] ❌ ContextMenu '\(_contextMenuSelector)' not found in any component collection")
        _linkedContextMenu = nil
    }

    private func getComponentName(_ component: Any) -> String? {
        // Use safer reflection approach to avoid YAIL string conversion issues
        let mirror = Mirror(reflecting: component)
        
        // Try common name properties
        for child in mirror.children {
            if let label = child.label,
               (label == "_name" || label == "name" || label == "_componentName" || label == "componentName") {
                
                // Handle various string types safely
                if let stringValue = child.value as? String {
                    print("[FAB] Found component name via \(label): '\(stringValue)'")
                    return stringValue
                }
                
                if let nsString = child.value as? NSString {
                    let stringValue = nsString as String
                    print("[FAB] Found component name via \(label) (NSString): '\(stringValue)'")
                    return stringValue
                }
            }
        }
        
        // Try calling getName methods if they exist
        if let nsObject = component as? NSObject {
            let getNameMethods = ["GetName", "getName", "GetNameAsString", "ToString"]
            
            for methodName in getNameMethods {
                let selector = NSSelectorFromString(methodName)
                if nsObject.responds(to: selector) {
                    print("[FAB] Trying to get name via \(methodName)")
                    
                    if let result = nsObject.perform(selector)?.takeUnretainedValue() as? String {
                        print("[FAB] Got name via \(methodName): '\(result)'")
                        return result
                    }
                }
            }
        }
        
        print("[FAB] ❌ Could not extract component name")
        return nil
    }

    // MARK: Events
    @objc open func Click() {
        EventDispatcher.dispatchEvent(of: self, called: "Click")
    }
}

// MARK: - ContextMenu Delegate - UPDATED FOR COMPATIBILITY
@available(iOS 14.5, *)
extension FloatingActionButton: UIContextMenuInteractionDelegate {
    public func contextMenuInteraction(
        _ interaction: UIContextMenuInteraction,
        configurationForMenuAtLocation location: CGPoint
    ) -> UIContextMenuConfiguration? {
        
        // Customize the configuration to remove visual feedback
        let configuration = UIContextMenuConfiguration(identifier: nil, previewProvider: nil) { _ in
            guard let contextMenu = self._linkedContextMenu else {
                print("[FAB] ❌ No linked ContextMenu available")
                return self.createErrorMenu("No ContextMenu linked")
            }
            
            print("[FAB] ✅ Found linked ContextMenu")
            print("[FAB] ContextMenu type: \(String(describing: type(of: contextMenu)))")
            
            // Try to use the ContextMenu's createUIMenu method
            if let contextMenuObj = contextMenu as? NSObject {
                print("[FAB] ✅ ContextMenu is NSObject, attempting to create UI menu")
                
                // Try the updated method signatures from the fixed ContextMenu
                let methodsToTry = [
                    "createUIMenu",                    // Parameterless version
                    "createUIMenuWithTitle:image:options:",  // Full parameters
                    "createUIMenuWithTitle:",          // Title only
                    "buildUIMenu",                     // Alternative name
                    "getUIMenu"                        // Alternative name
                ]
                
                for methodName in methodsToTry {
                    let selector = NSSelectorFromString(methodName)
                    if contextMenuObj.responds(to: selector) {
                        print("[FAB] ✅ ContextMenu responds to \(methodName)")
                        
                        var result: Unmanaged<AnyObject>?
                        
                        // Call appropriate method based on signature
                        if methodName == "createUIMenu" {
                            result = contextMenuObj.perform(selector)
                        } else if methodName.contains("Title:") && !methodName.contains("image:") {
                            result = contextMenuObj.perform(selector, with: "")
                        } else if methodName.contains(":") {
                            // For methods with multiple parameters, call with minimal args
                            result = contextMenuObj.perform(selector, with: "", with: nil)
                        } else {
                            result = contextMenuObj.perform(selector)
                        }
                        
                        if let menu = result?.takeUnretainedValue() as? UIMenu {
                            print("[FAB] ✅ Successfully created UIMenu with \(menu.children.count) items")
                            return menu
                        } else {
                            print("[FAB] ⚠️ Method \(methodName) returned: \(String(describing: result))")
                        }
                    }
                }
                
                print("[FAB] ❌ ContextMenu does not respond to any known menu creation methods")
                
                // Debug: List all available methods
                var methodCount: UInt32 = 0
                if let methods = class_copyMethodList(type(of: contextMenuObj), &methodCount) {
                    print("[FAB] Available methods on ContextMenu:")
                    for i in 0..<Int(methodCount) {
                        let method = methods[i]
                        let selector = method_getName(method)
                        let methodName = NSStringFromSelector(selector)
                        if methodName.lowercased().contains("menu") || methodName.lowercased().contains("create") {
                            print("[FAB]   - \(methodName)")
                        }
                    }
                    free(methods)
                }
            } else {
                print("[FAB] ❌ ContextMenu is not an NSObject")
            }
            
            print("[FAB] ❌ Falling back to error menu")
            return self.createErrorMenu("Failed to create menu from ContextMenu '\(self._contextMenuSelector)'")
        }
        
        // Customize the configuration to remove visual feedback
        // Note: Using only iOS 14.5+ compatible properties
        
        return configuration
    }
    
    // MARK: - Visual State Handling
    // Note: These methods are available in iOS 13.0+, which is compatible with our iOS 14.5+ target
    
    public func contextMenuInteraction(_ interaction: UIContextMenuInteraction, willDisplayMenuFor configuration: UIContextMenuConfiguration, animator: UIContextMenuInteractionAnimating?) {
        // Remove any visual feedback when menu appears
        button.layer.borderWidth = 0
        button.layer.borderColor = UIColor.clear.cgColor
        button.layer.shadowOpacity = 0.1  // Reduce shadow during interaction
        
        // Ensure no highlighting or selection state
        button.isHighlighted = false
        button.isSelected = false
    }
    
    public func contextMenuInteraction(_ interaction: UIContextMenuInteraction, didEndFor configuration: UIContextMenuConfiguration, animator: UIContextMenuInteractionAnimating?) {
        // Restore normal visual state when menu disappears
        button.layer.borderWidth = 0
        button.layer.borderColor = UIColor.clear.cgColor
        button.layer.shadowOpacity = 0.3  // Restore normal shadow
        
        // Ensure normal button state
        button.isHighlighted = false
        button.isSelected = false
    }

    private func createErrorMenu(_ message: String) -> UIMenu {
        let errorAction: UIAction
        
        if #available(iOS 13.0, *) {
            errorAction = UIAction(
                title: message,
                image: UIImage(systemName: "exclamationmark.triangle")
            ) { _ in
                print("[FAB] Error menu item tapped: \(message)")
            }
        } else {
            errorAction = UIAction(title: message) { _ in
                print("[FAB] Error menu item tapped: \(message)")
            }
        }
        
        errorAction.attributes = [.disabled]
        return UIMenu(title: "", children: [errorAction])
    }
}

// MARK: - Utility Extension
@available(iOS 14.5, *)
extension FloatingActionButton {
    private func getIconForMenuItem(icon: String) -> UIImage? {
        guard !icon.isEmpty else { return nil }
        if let custom = AssetManager.shared.imageFromPath(path: icon) { return custom }
        if #available(iOS 13.0, *), let sys = UIImage(systemName: icon) { return sys }
        if let img = UIImage(named: icon) { return img }
        return nil
    }
    
    // Debug method to check ContextMenu status
    @objc open func DebugContextMenuStatus() -> String {
        var status = "FloatingActionButton ContextMenu Status:\n"
        status += "- ContextMenuSelector: '\(_contextMenuSelector)'\n"
        status += "- Has LinkedContextMenu: \(_linkedContextMenu != nil)\n"
        status += "- LinkedContextMenu Type: \(String(describing: type(of: _linkedContextMenu)))\n"
        status += "- Has ContextMenuInteraction: \(contextMenuInteraction != nil)\n"
        
        if let contextMenu = _linkedContextMenu as? NSObject {
            status += "- ContextMenu responds to createUIMenu: \(contextMenu.responds(to: NSSelectorFromString("createUIMenu")))\n"
            
            // Try to get debug info from ContextMenu if available
            if contextMenu.responds(to: NSSelectorFromString("DebugInfo")) {
                if let debugInfo = contextMenu.perform(NSSelectorFromString("DebugInfo"))?.takeUnretainedValue() as? String {
                    status += "- ContextMenu Debug Info:\n\(debugInfo)\n"
                }
            }
        }
        
        print("[FAB] Debug Status: \(status)")
        return status
    }
}
