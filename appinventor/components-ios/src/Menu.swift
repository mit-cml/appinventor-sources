//
//  Menu.swift
//  AIComponentKit
//
//  Created by Kashish Sharma on 9/07/25.
//  Copyright © 2025 Massachusetts Institute of Technology. All rights reserved.
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

    
    public func addChildComponent(_ component: NonvisibleComponent) {
        if let menuItem = component as? MenuItem {
            menuItems.append(menuItem)
            print("[Menu] Added MenuItem: \(menuItem.Text), total items: \(menuItems.count)")
            
            updateMenuOnly()
        }
    }

    
    @objc open func ShowFrom(_ anchor: UIView) {
        let filteredItems = menuItems.filter { $0.Visible }

        let actions = filteredItems.map { item in
            UIAction(title: item.Text,
                     image: item.Icon.isEmpty ? nil : UIImage(systemName: item.Icon),
                     state: .off) { [weak self] _ in
                item.Click()
                self?.AfterSelecting(item.Text)
            }
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
        if let button = anchorButton {
            attachTo(button: button)
        }
    }


    @objc open func createUIMenu(title: String = "", image: UIImage? = nil, options: UIMenu.Options = []) -> UIMenu {
        let actions = menuItems.filter { $0.Visible }.map { item in
            let iconImage = item.Icon.isEmpty ? nil : UIImage(systemName: item.Icon)
            let action = UIAction(title: item.Text, image: iconImage) { [weak self] _ in
                item.Click()
                self?.AfterSelecting(item.Text)
            }
            action.attributes = item.Enabled ? [] : [.disabled]
            return action
        }

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
        
        
        guard Menu.buttonCreated, let customButton = Menu.globalCustomButton else {
            print("[Menu] Global button not created yet, skipping menu update")
            return
        }
        
        
        let actions = menuItems.filter { $0.Visible }.map { item in
            UIAction(title: item.Text,
                     image: item.Icon.isEmpty ? nil : UIImage(systemName: item.Icon),
                     state: .off) { [weak self] _ in
                item.Click()
                self?.ItemSelected(self?.menuItems.firstIndex(of: item) ?? 0, item)
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
        
        
        if !Menu.buttonCreated {
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
                
                
                existingButtons.removeAll { $0 === Menu.globalMenuButton }
                
                
                if let menuButton = Menu.globalMenuButton {
                    existingButtons.insert(menuButton, at: 0)
                }
                
                
                navigationController.navigationBar.topItem?.rightBarButtonItems = existingButtons
                print("[Menu] Added global button before bookmarks button, total buttons: \(existingButtons.count)")
            }
            
            Menu.buttonCreated = true
        }
        
        
        if #available(iOS 14.0, *), let customButton = Menu.globalCustomButton {
            let actions = menuItems.filter { $0.Visible }.map { item in
                UIAction(title: item.Text,
                         image: item.Icon.isEmpty ? nil : UIImage(systemName: item.Icon),
                         state: .off) { [weak self] _ in
                    item.Click()
                    self?.ItemSelected(self?.menuItems.firstIndex(of: item) ?? 0, item)
                }
            }
            
            let menu = UIMenu(title: "", children: actions)
            customButton.menu = menu
            print("[Menu] Updated global menu with \(actions.count) actions")
        }
    }
    
    @objc private func menuButtonTapped() {
        
        print("Menu button tapped - native iOS menu should appear")
    }
    
    
    @objc open func ItemSelected(_ itemIndex: Int, _ item: MenuItem) {
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
