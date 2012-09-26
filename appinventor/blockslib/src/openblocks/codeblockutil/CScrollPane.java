// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;


import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JLayeredPane;


/**
 * The CScrollPane is a swing-compatible widget that
 * allows clients of this CScrollPane to control the
 * width of the thumb, the color of the thumb, and
 * and the color of the track.  Like all swing-compatible
 * scroll panes, the CScrollPane wraps a viewport and must
 * change the viewing space (also known as the visible rectangle)
 * of the scroll pane when users attempts to scroll
 * with the mouse, wheel, or key board.
 */
public abstract class CScrollPane extends JLayeredPane implements MouseWheelListener, KeyListener{
  public enum ScrollPolicy{
    HORIZONTAL_BAR_ALWAYS,
        HORIZONTAL_BAR_NEVER,
        HORIZONTAL_BAR_AS_NEEDED,
        VERTICAL_BAR_ALWAYS,
        VERTICAL_BAR_NEVER,
        VERTICAL_BAR_AS_NEEDED};
  CScrollPane(){
    super();
  }

  /**
   * @return vertical scroll bar bounding range model.  May be null
   */
  abstract public BoundedRangeModel getVerticalModel();

  /**
   * @return horizontal scroll bar bounding range model.  May be null
   */
  abstract public BoundedRangeModel getHorizontalModel();

  /**
   * Scrolls the view so that Rectangle  within the view becomes visible.
   * This attempts to validate the view before scrolling if the view is
   * currently not valid - isValid returns false. To avoid excessive
   * validation when the containment hierarchy is being created this
   * will not validate if one of the ancestors does not have a peer,
   * or there is no validate root ancestor, or one of the ancestors
   * is not a Window or Applet.
   *
   * Note that this method will not scroll outside of the valid viewport;
   * for example, if contentRect is larger than the viewport, scrolling
   * will be confined to the viewport's bounds.
   *
   * @param contentRect - the Rectangle to display
   */
  abstract public void scrollRectToVisible(Rectangle contentRect);

  /**
   * Set the amount by which the mouse wheel scrolls
   * @requires INTEGER_MIN<x<INTEGER_MAX
   * @modifies this.SCROLLINGUNIT
   * @effects set this.scrollingunit to x
   */
  abstract public void setScrollingUnit(int x);


  /**
   * MouseWheelListener: Should move the viewport by same amount of wheel scroll
   */
  abstract public void mouseWheelMoved(MouseWheelEvent e);
}
