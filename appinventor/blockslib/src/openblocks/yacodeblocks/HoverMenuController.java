// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import openblocks.renderable.FactoryRenderableBlock;
import openblocks.workspace.Workspace;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

/**
 * A HoverController can be created and then added as a MouseListener to
 * one or more target JComponents.  When the user clicks on a target, the
 * HoverMenu is placed over the click point. When the mouse wanders outside the menu's
 * vicinity the menu is taken down.
 * 
 */
public class HoverMenuController {

  private HoverMenu menu;  // the HoverMenu being controlled
  private Rectangle lastRegion = new Rectangle(0, 0, 0, 0);  // The block and menu area


  /**
   * Creates a HoverMenuController for the given HoverMenu.
   * @param menu the HoverMenu to be moved as user attention changes
   */

  public HoverMenuController(HoverMenu menu) {
    this.menu = menu;
    menu.setVisible(false);
    menu.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        // Prevent refocusing the popup while user is in it.
      }
    });
  }

  /**
   * Makes this controller listen to the given target.
   * This method should be called for each target that can trigger the
   * HoverMenu. For performance reasons, it should be called at most once
   * per target. We don't check that this is the case, however.
   * 
   * @param target a context in which the HoverMenu is triggered
   */
  public void addTarget(JComponent target) {
    if (!(target instanceof FactoryRenderableBlock)) { 
      target.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
            return;
          }
          showMenu((JComponent) e.getSource(), e.getPoint());
        }
      }
      );
    }
  }

  /**
   * Sets the context of the HoverMenu and moves it to a nearby location.
   */
  private void showMenu(final JComponent target, final Point pointInTarget) {
    JLayeredPane place = (JLayeredPane)
        SwingUtilities.getAncestorOfClass(JLayeredPane.class, target);
    if (place != null) {
      menu.setContext(target, pointInTarget);  // update the menu per context
      // Choose to make the center of the menu come up just above the mouse click
      // point unless the mouse is too close to a boundary.
      Point menuLoc = target.getLocation();
      JComponent canvas = Workspace.getInstance().getBlockCanvas().getCanvas();
      menuLoc.translate(pointInTarget.x, pointInTarget.y);
      menuLoc.translate(-menu.getWidth() / 2, -menu.getHeight() - 2);
      menuLoc.translate(Math.max(0, -menuLoc.x), Math.max(0, -menuLoc.y));
      menuLoc.translate(Math.min(0, canvas.getWidth() - menuLoc.x),
                        Math.min(0, canvas.getHeight() - menuLoc.y));      
      if (menu.getParent() != null) {
        // workaround to ensure that the popup menu is put on top
        menu.getParent().remove(menu);
      }
      place.add(menu, JLayeredPane.POPUP_LAYER);
      place.moveToFront(menu);
      menu.setLocation(menuLoc);
      menu.setVisible(true);
      // Keep menu up as long as mouse is in rectangle defined by the menu itself
      // and region below it three menu heights deep.
      lastRegion = new Rectangle(menuLoc.x, menuLoc.y, menu.getWidth(),
          menu.getHeight() * 4);
      lastRegion = SwingUtilities.convertRectangle(
          menu.getParent(),
           lastRegion,
           Workspace.getInstance().getBlockCanvas().getCanvas());
    }
  }

  /**
   * Allows the HoverMenu to change based on Workspace events
   */
  public void mouseMovedOnCanvas(MouseEvent event) {
    if (!lastRegion.contains(event.getPoint())) {
      menu.setVisible(false);
    }
  }

}