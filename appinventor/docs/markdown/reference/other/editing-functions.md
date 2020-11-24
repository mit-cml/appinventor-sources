---
title: Editing Functions in App Inventor
layout: documentation
---

Beginning with version nb184, App Inventor has included the capability to cut, copy, and paste parts of your app design. This document explains this functionality.

## Manipulating Components

Your app design is made up of one or more components in a tree structure that starts at the Screen. You can add components by dragging them in from the Palette panel to the left of the designer. You can customize components by changing their properties in the Properties panel to the right of the designer.

### Selecting Components

You select components by clicking on them. Starting with release nb182, you can select multiple components by holding a platform specific key (Ctrl on Windows/Linux, Command on macOS) and clicking on additional components. This feature is sometimes referred to in App Inventor as multiselect. When you select multiple components, the properties panel will update to only show common properties among the selected components. Changing a property in this mode will change it for all of the selected components.

### Copying Components

Once you have selected one or more elements, use the standard keyboard shortcut for your platform to copy them (Ctrl+C on Windows/Linux, ⌘C on macOS). This places the contents of the selection onto your system's clipboard.

You can also use the Cut shortcut (Ctrl+X on Windows/Linux, ⌘X on macOS) to copy elements to the clipboard and immediately delete them, so that they can be pasted elsewhere.

### Pasting Components

Once you have copied components to the clipboard, you can press the paste shortcut key (Ctrl+V on Windows/Linux, ⌘V on macOS) to paste them. When copying and pasting components, the default behavior is to copy both the design as well as the behavior (i.e., the blocks). If you would prefer to only paste the component without copying its behavior, hold the shift key while pressing the paste key combination. App Inventor will skip pasting the blocks if the shift key is held.

When pasting, App Inventor will rename the pasted components if there is a collision with existing components in the project. It does this by computing a fresh name for each colliding name. The renaming algorithm works by either adding a numeric suffix to the name, or by incrementing the numeric suffix of the name until the collision is resolved. For example, if you copy a component named `ResetButton`, the first copy will be called `ResetButton1`, the second will be called `ResetButton2`, and so forth. These new names are also subsituted into the copied blocks code, if any.

## Manipulating Blocks

The blocks editor is where you provide the behavior for your app. Like the designer, it supports copying and pasting blocks. You can also use the [backpack](backpack.html) to transfer blocks between projects, or [download blocks as images](download-pngs.html) that you can share with others.

### Copying Blocks

To copy a block, first select the block. You can copy the block by either pressing the copy shortcut for your platform (Ctrl+C for Windows/Linux, ⌘C for macOS). The copy action is also available on the context menu by right clicking (Ctrl+clicking on macOS) the block.

### Paste Blocks

To paste a block, press the paste shortcut for your platofmr (Ctrl+V for Windows/Linux, ⌘V on macOS). The paste action is also available on the context menu by right clicking (Ctrl+clicking on macOS) the workspace.

## Copying Screens

App Inventor allows you to copy and paste the content of a screen, effectively allowing you to copy screens.

To copy a screen, select the screen either by clicking its background or selecting it in the structure tree. Press the copy shortcut key to copy it. Next, create a new screen. Press the paste shortcut key in this new screen to paste the contents copied from the previous screen.

## Sharing Designs

Because the copy functionality puts the content of your selection onto the clipboard, it is possible to share the content in a text form although the format is somewhat complex. For example, you can copy a component and paste its textual representation into a text document or email. Someone can select the text, copy it, and then paste it into App Inventor to recreate the component's representation. This can help you build a collection of design elements for your apps.
