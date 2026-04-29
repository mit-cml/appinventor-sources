// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Event;
import com.google.gwt.aria.client.Roles;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * This explorer is used to outline the structure of a source file. Note that
 * this explorer is shared by all its clients. That means that clients (most
 * likely editors) need to update its content upon activation.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class SourceStructureExplorer extends Composite {
  // UI elements
  private final EventCaptureTree tree;
  private ScrollPanel scrollPanel;
  private final TextButton renameButton;
  private final TextButton deleteButton;

  // Drag-and-drop state
  /** Maps component name → SourceStructureExplorerItem; rebuilt on every tree update. */
  private final Map<String, SourceStructureExplorerItem> nameToItem = new HashMap<>();
  /** Name of the component currently being dragged, or null if no drag is in progress. */
  private String draggingName = null;
  /** Name of the component currently hovered as a drop target, or null if none. */
  private String hoverName = null;
  /** Current drop position relative to the hover target: -1=before, 0=into, 1=after. */
  private int hoverPosition = 0;
  /** Absolutely-positioned div injected into the scroll panel as the drop indicator. */
  private Element indicatorDiv;

  /** Names of components that the user has explicitly collapsed in the tree. */
  private final Set<String> collapsedNames = new HashSet<>();
  /** Names of components whose initial expansion state has already been determined. */
  private final Set<String> seenNames = new HashSet<>();
  /** True when the indicator is currently showing as a box (drop-into); false for a line. */
  private boolean indicatorIsBox = false;

  /**
   * This is a hack to work around the fact that for multiselect we need to have
   * access to the state of the meta/ctrl key but the SelectionHandler doesn't
   * provide access to the original event that caused the selection. We capture
   * the most recent event before the selection event is triggered and then
   * reset once the selection has been updated.
   */
  static class EventCaptureTree extends Tree {

    Event lastEvent = null;

    public EventCaptureTree(Resources resources) {
      super(resources);
    }

    @Override
    public void onBrowserEvent(Event event) {
      lastEvent = event;
      super.onBrowserEvent(event);
    }
  }

  /**
   * Creates a new source structure explorer.
   */
  public SourceStructureExplorer() {
    // Initialize UI elements
    tree = new EventCaptureTree(Ode.getImageBundle());
    tree.setAnimationEnabled(true);
    tree.setScrollOnSelectEnabled(false);
    Roles.getTreeRole().set(tree.getElement());
    tree.addCloseHandler(new CloseHandler<TreeItem>() {
      @Override
      public void onClose(CloseEvent<TreeItem> event) {
        TreeItem treeItem = event.getTarget();
        if (treeItem != null) {
          String name = treeItem.getElement().getAttribute("data-name");
          if (name != null && !name.isEmpty()) {
            collapsedNames.add(name);
          }
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            ((SourceStructureExplorerItem) userObject).onStateChange(false);
          }
        }
      }
    });
    tree.addOpenHandler(new OpenHandler<TreeItem>() {
      @Override
      public void onOpen(OpenEvent<TreeItem> event) {
        TreeItem treeItem = event.getTarget();
        if (treeItem != null) {
          String name = treeItem.getElement().getAttribute("data-name");
          if (name != null && !name.isEmpty()) {
            collapsedNames.remove(name);
          }
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            ((SourceStructureExplorerItem) userObject).onStateChange(true);
          }
        }
      }
    });
    tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem treeItem = event.getSelectedItem();
        if (treeItem != null) {
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
            enableButtons(item);
            //showBlocks(item);
            item.onSelected(tree.lastEvent);
          } else {
            disableButtons();
            //hideComponent();
          }
        } else {
          disableButtons();
        }
        tree.lastEvent = null;
      }
    });
    tree.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_DELETE || keyCode == KeyCodes.KEY_BACKSPACE) {
          event.preventDefault();
          deleteItemFromTree();
        } else if (event.isAltKeyDown() && keyCode == KeyCodes.KEY_N) {
          event.preventDefault();
          renameItem();
        }
      }
    });
    tree.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        tree.getParent().addStyleName("gwt-Tree-focused");
      }
    });
    tree.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        tree.getParent().removeStyleName("gwt-Tree-focused");
      }
    });
    tree.addMouseUpHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        tree.setFocus(true);
      }
    });

    // drag-and-drop for component tree reordering
    tree.addBitlessDomHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        Element target = event.getNativeEvent().getEventTarget().cast();
        String name = findDataName(target);
        if (name == null) {
          event.getNativeEvent().preventDefault();
          return;
        }
        SourceStructureExplorerItem item = nameToItem.get(name);
        if (item == null || !item.canDrag()) {
          event.getNativeEvent().preventDefault();
          return;
        }
        draggingName = name;
        initDragTransfer(event.getNativeEvent(), name);
      }
    }, DragStartEvent.getType());

    tree.addBitlessDomHandler(new DragOverHandler() {
      @Override
      public void onDragOver(DragOverEvent event) {
        if (draggingName == null) {
          return;
        }
        event.getNativeEvent().preventDefault(); // required to allow drop
        setDropEffect(event.getNativeEvent(), "move");

        Element target = event.getNativeEvent().getEventTarget().cast();
        Element nameEl = findDataNameElement(target);
        if (nameEl == null) {
          hideIndicator();
          hoverName = null;
          return;
        }

        String targetName = nameEl.getAttribute("data-name");
        if (targetName.equals(draggingName)) {
          hideIndicator();
          hoverName = null;
          return;
        }

        SourceStructureExplorerItem targetItem = nameToItem.get(targetName);
        if (targetItem == null) {
          hideIndicator();
          hoverName = null;
          return;
        }

        // Compute drop position from mouse Y relative to the item row
        int mouseY = event.getNativeEvent().getClientY();
        int elTop = nameEl.getAbsoluteTop();
        int elHeight = nameEl.getOffsetHeight();
        if (elHeight == 0) {
          elHeight = 20; // fallback
        }
        int relY = mouseY - elTop;

        int position;
        if (targetItem.isContainer()) {
          // Three zones: top third = before, middle = into, bottom third = after
          if (relY < elHeight / 3) {
            position = -1;
          } else if (relY > 2 * elHeight / 3) {
            position = 1;
          } else {
            position = 0;
          }
        } else {
          // Two zones: top half = before, bottom half = after
          position = (relY < elHeight / 2) ? -1 : 1;
        }

        // Update visual indicator
        hoverName = targetName;
        hoverPosition = position;
        showIndicator(nameEl, position);
      }
    }, DragOverEvent.getType());

    tree.addBitlessDomHandler(new DragLeaveHandler() {
      @Override
      public void onDragLeave(DragLeaveEvent event) {
        // Only clear the highlight when the pointer leaves the tree entirely
        Element relatedTarget = getRelatedTarget(event.getNativeEvent());
        if (relatedTarget == null || !tree.getElement().isOrHasChild(relatedTarget)) {
          hideIndicator();
          hoverName = null;
        }
      }
    }, DragLeaveEvent.getType());

    tree.addBitlessDomHandler(new DropHandler() {
      @Override
      public void onDrop(DropEvent event) {
        event.getNativeEvent().preventDefault();
        event.getNativeEvent().stopPropagation();
        if (draggingName != null && hoverName != null) {
          SourceStructureExplorerItem source = nameToItem.get(draggingName);
          SourceStructureExplorerItem target = nameToItem.get(hoverName);
          if (source != null && target != null) {
            source.moveTo(target, hoverPosition);
          }
        }
        clearDragState();
      }
    }, DropEvent.getType());

    tree.addBitlessDomHandler(new DragEndHandler() {
      @Override
      public void onDragEnd(DragEndEvent event) {
        clearDragState();
      }
    }, DragEndEvent.getType());

    // Put a ScrollPanel around the tree.
    scrollPanel = new ScrollPanel(tree);
    scrollPanel.setStyleName("ode-SourceScrollPanel");
    // The scroll panel must be position:relative so the absolute indicator div is contained within it.
    scrollPanel.getElement().getStyle().setProperty("position", "relative");

    // Create a dedicated drop indicator div, injected into the scroll panel.
    // Initialised in line mode (matches indicatorIsBox = false).
    indicatorDiv = Document.get().createDivElement();
    indicatorDiv.getStyle().setProperty("position",     "absolute");
    indicatorDiv.getStyle().setProperty("pointerEvents","none");
    indicatorDiv.getStyle().setProperty("display",      "none");
    indicatorDiv.getStyle().setProperty("zIndex",       "1000");
    indicatorDiv.getStyle().setProperty("left",         "0");
    indicatorDiv.getStyle().setProperty("width",        "100%");
    indicatorDiv.getStyle().setProperty("height",       "2px");
    indicatorDiv.getStyle().setProperty("background",   "#4285F4");
    indicatorDiv.getStyle().setProperty("border",       "none");
    scrollPanel.getElement().appendChild(indicatorDiv);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setStyleName("ode-PanelButtons");
    buttonPanel.setSpacing(4);

    renameButton = new TextButton(MESSAGES.renameButton());
    renameButton.setEnabled(false);
    renameButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        renameItem();
      }
    });
    buttonPanel.add(renameButton);
    buttonPanel.setCellHorizontalAlignment(renameButton, HorizontalPanel.ALIGN_RIGHT);

    deleteButton = new TextButton(MESSAGES.deleteButton());
    deleteButton.setEnabled(false);
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        deleteItemFromTree();
      }
    });
    buttonPanel.add(deleteButton);
    buttonPanel.setCellHorizontalAlignment(deleteButton, HorizontalPanel.ALIGN_LEFT);

    VerticalPanel panel = new VerticalPanel();
    panel.add(scrollPanel);
    panel.add(new Label());
    panel.add(buttonPanel);
    panel.setCellHorizontalAlignment(buttonPanel, HorizontalPanel.ALIGN_CENTER);
    initWidget(panel);
  }

  public void setStyleName (String styleName){
    setStylePrimaryName(styleName);
  }

  private void deleteItemFromTree() {
    TreeItem treeItem = tree.getSelectedItem();
    if (treeItem != null) {
      Object userObject = treeItem.getUserObject();
      if (userObject instanceof SourceStructureExplorerItem) {
        SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
        item.delete();
      }
    }
  }

  private void renameItem() {
    TreeItem treeItem = tree.getSelectedItem();
    if (treeItem != null) {
      Object userObject = treeItem.getUserObject();
      if (userObject instanceof SourceStructureExplorerItem) {
        SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
        item.rename();
      }
    }
  }

  private void enableButtons(SourceStructureExplorerItem item) {
    renameButton.setEnabled(item.canRename());
    deleteButton.setEnabled(item.canDelete());
  }

  private void disableButtons() {
    renameButton.setEnabled(false);
    deleteButton.setEnabled(false);
  }

  
  /* move this logic to declarations of SourceStructureExplorerItem subtypes
  private void showBlocks(SourceStructureExplorerItem item) {
    // are we showing the blocks editor?
    if (Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor) {
      YaBlocksEditor editor = 
          (YaBlocksEditor) Ode.getInstance().getCurrentFileEditor();
      OdeLog.log("Showing item " + item.getItemName());
      if (item.isComponent()) {
        editor.showComponentBlocks(item.getItemName());
      } else {
        editor.showBuiltinBlocks(item.getItemName());
      }
    }
  }

  private void hideComponent() {
    if (Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor) {
      YaBlocksEditor editor =
          (YaBlocksEditor) Ode.getInstance().getCurrentFileEditor();
      OdeLog.log("Hiding selected item");
      editor.hideComponentBlocks();
    }  
  }
   */  

  /**
   * Clears the tree.
   */
  public void clearTree() {
    tree.clear();
    disableButtons();
  }

  /**
   * Updates the tree
   *
   * @param root the new root TreeItem
   * @param itemToSelect item to select, or null for no selected item
   */
  public void updateTree(TreeItem root, SourceStructureExplorerItem itemToSelect) {
    TreeItem items[] = new TreeItem[1];
    items[0] = root;
    updateTree(items, itemToSelect);
  }

  
  /**
   * Updates the tree
   *
   * @param roots An array of root items (all top level)
   * @param itemToSelect item to select, or null for no selected item
   */
  public void updateTree(TreeItem[] roots, SourceStructureExplorerItem itemToSelect) {
    tree.clear();
    nameToItem.clear();
    for (TreeItem root : roots) {
      applyExpansionState(root);  // before addItem so setState doesn't trigger animation
      tree.addItem(root);
      collectNameToItem(root);
    }
    if (itemToSelect != null) {
      selectItem(itemToSelect, true);
    } else {
      disableButtons();
    }
  }

  /**
   * Select or unselect an item in the tree
   *
   * @param item to select or unselect
   * @param select true to select, false to unselect
   */
  private void selectItem(SourceStructureExplorerItem item, boolean select) {
    Iterator<TreeItem> iter = tree.treeItemIterator();
    while (iter.hasNext()) {
      TreeItem treeItem = iter.next();
      if (item.equals(treeItem.getUserObject())) {
        // NOTE(lizlooney) - It turns out that calling TreeItem.setSelected(true) doesn't actually
        // select the item in the tree. It looks selected, but Tree.getSelectedItem() will return
        // null. Instead, we have to call Tree.setSelectedItem.
        if (select) {
          tree.setSelectedItem(treeItem, false); // false means don't trigger a SelectionEvent
          enableButtons(item);
          //showBlocks(item);
        } else {
          tree.setSelectedItem(null, false); // false means don't trigger a SelectionEvent
          disableButtons();
          //hideComponent();
        }
        break;
      }
    }
  }

  /**
   * Select an item in the tree
   *
   * @param item item to select
   */
  public void selectItem(SourceStructureExplorerItem item) {
    selectItem(item, true);
  }

  /**
   * Select an item in the tree
   *
   * @param item item to unselect
   */
  public void unselectItem(SourceStructureExplorerItem item) {
    selectItem(item, false);
  }

  public Tree getTree() {
    return tree;
  }

  /** Recursively walks a TreeItem hierarchy and populates {@link #nameToItem}. */
  private void collectNameToItem(TreeItem item) {
    if (item == null) {
      return;
    }
    String name = item.getElement().getAttribute("data-name");
    if (name != null && !name.isEmpty()) {
      Object userObject = item.getUserObject();
      if (userObject instanceof SourceStructureExplorerItem) {
        nameToItem.put(name, (SourceStructureExplorerItem) userObject);
      }
    }
    for (int i = 0; i < item.getChildCount(); i++) {
      collectNameToItem(item.getChild(i));
    }
  }

  /**
   * Recursively applies expansion state to freshly built tree items.
   * Items whose data-name is in {@link #collapsedNames} are closed; all others are opened.
   */
  private void applyExpansionState(TreeItem item) {
    String name = item.getElement().getAttribute("data-name");
    if (name != null && !name.isEmpty() && !seenNames.contains(name)) {
      seenNames.add(name);
      Object userObj = item.getUserObject();
      if (userObj instanceof SourceStructureExplorerItem
          && !((SourceStructureExplorerItem) userObj).isInitiallyExpanded()) {
        collapsedNames.add(name);
      }
    }
    item.setState(name == null || name.isEmpty() || !collapsedNames.contains(name));
    for (int i = 0; i < item.getChildCount(); i++) {
      applyExpansionState(item.getChild(i));
    }
  }

  /** Hides the drop indicator div. */
  private void hideIndicator() {
    indicatorDiv.getStyle().setProperty("display", "none");
  }

  /**
   * Positions and shows the drop indicator div relative to {@code target}.
   * For position 0 (drop-into): draws a dashed outline box around the element.
   * For position -1/1 (before/after): draws a 2px horizontal line above or below the element.
   */
  private void showIndicator(Element target, int position) {
    int elAbsTop     = target.getAbsoluteTop();
    int panelAbsTop  = scrollPanel.getElement().getAbsoluteTop();
    int scrollTop    = scrollPanel.getElement().getScrollTop();

    if (position == 0) {
      // Box mode: outline around the target element
      if (!indicatorIsBox) {
        indicatorDiv.getStyle().setProperty("height",     "");
        indicatorDiv.getStyle().setProperty("left",       "");
        indicatorDiv.getStyle().setProperty("background", "transparent");
        indicatorDiv.getStyle().setProperty("border",     "2px dashed #4285F4");
        indicatorIsBox = true;
      }
      int panelAbsLeft = scrollPanel.getElement().getAbsoluteLeft();
      indicatorDiv.getStyle().setProperty("top",    (elAbsTop - panelAbsTop + scrollTop) + "px");
      indicatorDiv.getStyle().setProperty("left",   (target.getAbsoluteLeft() - panelAbsLeft) + "px");
      indicatorDiv.getStyle().setProperty("width",  target.getOffsetWidth()  + "px");
      indicatorDiv.getStyle().setProperty("height", target.getOffsetHeight() + "px");
    } else {
      // Line mode: 2px horizontal bar above or below the target element
      if (indicatorIsBox) {
        indicatorDiv.getStyle().setProperty("left",       "0");
        indicatorDiv.getStyle().setProperty("width",      "100%");
        indicatorDiv.getStyle().setProperty("height",     "2px");
        indicatorDiv.getStyle().setProperty("background", "#4285F4");
        indicatorDiv.getStyle().setProperty("border",     "none");
        indicatorIsBox = false;
      }
      int lineY = elAbsTop - panelAbsTop + scrollTop + (position > 0 ? target.getOffsetHeight() : 0);
      indicatorDiv.getStyle().setProperty("top", lineY + "px");
    }
    indicatorDiv.getStyle().setProperty("display", "block");
  }

  /** Clears all drag state (called on drop or dragend). */
  private void clearDragState() {
    draggingName = null;
    hoverName = null;
    hoverPosition = 0;
    hideIndicator();
  }

  /**
   * Walks up the DOM from {@code el} and returns the first element that has a
   * {@code data-name} attribute, or {@code null} if none is found before body.
   */
  private static native String findDataName(Element el) /*-{
    var current = el;
    while (current && current.tagName && current.tagName.toLowerCase() !== 'body') {
      var n = current.getAttribute ? current.getAttribute('data-name') : null;
      if (n) return n;
      current = current.parentElement;
    }
    return null;
  }-*/;

  /**
   * Walks up the DOM from {@code el} and returns the first element that has a
   * {@code data-name} attribute, or {@code null} if none is found before body.
   */
  private static native Element findDataNameElement(Element el) /*-{
    var current = el;
    while (current && current.tagName && current.tagName.toLowerCase() !== 'body') {
      var n = current.getAttribute ? current.getAttribute('data-name') : null;
      if (n) return current;
      current = current.parentElement;
    }
    return null;
  }-*/;

  /** Returns the {@code relatedTarget} of a drag/mouse event, or {@code null}. */
  private static native Element getRelatedTarget(NativeEvent event) /*-{
    return event.relatedTarget || null;
  }-*/;

  /** Sets {@code dataTransfer.dropEffect} on a native drag event. */
  private static native void setDropEffect(NativeEvent event, String effect) /*-{
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = effect;
    }
  }-*/;

  /** Sets the drag transfer data so the browser shows a "move" cursor. */
  private static native void initDragTransfer(NativeEvent event, String data) /*-{
    if (event.dataTransfer) {
      try {
        event.dataTransfer.setData('text/plain', data);
        event.dataTransfer.effectAllowed = 'move';
      } catch (e) { }
    }
  }-*/;
}
