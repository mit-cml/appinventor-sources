//
//  MenuItem.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 21/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
//
//  MenuItem component with full get/set property support.
//  Properties: Text, Enabled, Visible, Icon, ShowOnActionBar
//  All property changes automatically refresh the parent menu.
//  Events: Click() - triggered when menu item is selected
//

import UIKit

@available(iOS 14.5, *)
@objc(MenuItem)
open class MenuItem: NonvisibleComponent {
    private weak var _container: ComponentContainer?

    @objc open var Text: String = "" {
        didSet {
            print("[MenuItem] Text changed to: \(Text)")
            notifyMenuUpdate()
        }
    }
    
    @objc open func getText() -> String {
        print("[MenuItem] Getting Text: \(Text)")
        return Text
    }
    
    @objc open func setTextDirect(_ text: String) {
        print("[MenuItem] setTextDirect called with: '\(text)'")
        print("[MenuItem] Current Text: '\(Text)'")
        
        // Direct property assignment
        self.Text = text
        
        print("[MenuItem] Text after direct set: '\(Text)'")
        
        // Force menu update
        notifyMenuUpdate()
        
        print("[MenuItem] setTextDirect completed")
    }
    
    @objc open var Enabled: Bool = true {
        didSet {
            print("[MenuItem] Enabled changed to: \(Enabled)")
            notifyMenuUpdate()
        }
    }
    
    @objc open func getEnabled() -> Bool {
        print("[MenuItem] Getting Enabled: \(Enabled)")
        return Enabled
    }
    
    // Setter method for App Inventor-style block: Enabled(value)
    @objc open func Enabled(_ value: Bool) {
        print("[MenuItem] Enabled(_:) called with: \(value)")
        self.Enabled = value
    }
    
    @objc open var Visible: Bool = true {
        didSet {
            print("[MenuItem] Visible changed to: \(Visible)")
            notifyMenuUpdate()
        }
    }
    
    @objc open func getVisible() -> Bool {
        print("[MenuItem] Getting Visible: \(Visible)")
        return Visible
    }
    
    @objc open var Icon: String = "" {
        didSet {
            print("[MenuItem] Icon changed to: \(Icon)")
            notifyMenuUpdate()
        }
    }
    
    @objc open func getIcon() -> String {
        print("[MenuItem] Getting Icon: \(Icon)")
        return Icon
    }
    
    @objc open var ShowOnActionBar: Bool = false {
        didSet {
            print("[MenuItem] ShowOnActionBar changed to: \(ShowOnActionBar)")
            notifyMenuUpdate()
        }
    }
    
    @objc open func getShowOnActionBar() -> Bool {
        print("[MenuItem] Getting ShowOnActionBar: \(ShowOnActionBar)")
        return ShowOnActionBar
    }

    @objc public override init(_ container: ComponentContainer) {
        self._container = container
        super.init(container)
        // Add this MenuItem to its parent Menu
        if let menu = container as? Menu {
            menu.addChildComponent(self)
        }
    }

    @objc open func Click() {
        print("[MenuItem] Click event triggered for: \(Text)")
        EventDispatcher.dispatchEvent(of: self, called: "Click")
    }
    
    @objc open func TestTextSetter(_ newText: String) {
        print("[MenuItem] TestTextSetter called with: '\(newText)'")
        print("[MenuItem] Current Text before set: '\(Text)'")
        
        // Test the setter
        Text = newText
        
        print("[MenuItem] Text after set: '\(Text)'")
        
        // Verify the getter works
        let retrievedText = getText()
        print("[MenuItem] Retrieved Text via getter: '\(retrievedText)'")
        
        // Test if the property was actually changed
        if Text == newText {
            print("[MenuItem] ✅ Text property setter is working correctly")
        } else {
            print("[MenuItem] ❌ Text property setter is NOT working")
        }
    }
    
    private func notifyMenuUpdate() {
        // Notify the parent menu to refresh when properties change
        if let menu = _container as? Menu {
            print("[MenuItem] Notifying menu of property change for: \(Text)")
            
            // Add a small delay to ensure property is fully updated
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                menu.ForceRefresh()
            }
        }
    }
}
