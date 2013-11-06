// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;


/**
 * See documentation for Explorer.
 *
 * The TabbedExplorer functions much like any JTAbbedPane.
 * But its set of tabs are fitted into a wheel such that
 * the buttons scroll from left to right or right to left.
 * A pop-up menu is also provided to search through all
 * the possible tabs.
 */
public class TabbedExplorer extends JPanel implements Explorer, MouseListener, ActionListener{
  private static final long serialVersionUID = 328149080307L;
  /** the total VISIBLE height of each tab */
  private static final int BUTTON_HEIGHT = 28;
  /** The set of drawers that wraps each canvas */
  private final List<TabCard> drawerCards;
  /** Teh canvas portion */
  private final JComponent canvasPane;
  /** The wheel of buttons */
  private final CWheeler wheeler;
  /** The PopupMenu that displays all canvas names */
  private final JPopupMenu menu;
  /** For scrolling the wheel of buttons left */
  private final CButton left;
  /** For scrolling the wheel of buttons right */
  private final CButton right;
  /** For displaying the popup menu */
  private final CButton down;
  /** The timer that scrolls through the wheel of buttons */
  private final Timer timer;
  /** true if the timer should scroll left when active */
  private boolean directedToLeft = true;
  /** true if the canvas is should fit in a scroll pane.  false if it must fit within the bounds of the canvaspane */
  private final boolean scrollable;
  /** The index of the selected Canvas */
  private int selectedIndex = 0;
  /**
   * Constructs new TabbedExplorer with scrollable set to true
   */
  public TabbedExplorer(){
    this(true);
  }
  /**
   * Constructs new TabbedExplorer that is scrollable if specified as true.
   */
  public TabbedExplorer(boolean scrollable){
    super();
    this.scrollable = scrollable;
    this.drawerCards = new ArrayList<TabCard>();
    this.canvasPane = new JPanel(new BorderLayout());
    canvasPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5, CGraphite.darkgreen));

    this.setBackground(Color.black);
    this.timer = new Timer(300, this);
    menu = new JPopupMenu();
    wheeler = new CWheeler(new ArrayList<JComponent>(), false, Color.black);

    left = new CTabButton("<<");
    left.addMouseListener(this);
    right = new CTabButton(">>");
    right.addMouseListener(this);
    down = new CTabButton("V");
    down.addMouseListener(this);

    double[][] constraints = {{BUTTON_HEIGHT,TableLayoutConstants.FILL,BUTTON_HEIGHT,BUTTON_HEIGHT},
                              {BUTTON_HEIGHT, TableLayoutConstants.FILL}};
    this.setLayout(new TableLayout(constraints));
    this.add(left, "0, 0");
    this.add(wheeler, "1, 0");
    this.add(right, "2, 0");
    this.add(down, "3, 0");
    this.add(canvasPane, "0, 1, 3, 1");
  }

  public boolean anyCanvasSelected(){
    return false;
  }

  public int getSelectedCanvasWidth(){
    return this.canvasPane.getWidth();
  }

  public void addListener(ExplorerListener gel){}

  public void removeListener(ExplorerListener gel){}


  /**
   * Reassigns the set of canvases that this explorer controls.
   * Though the collection of canvas may be empty, it may not be null.
   * @param items
   *
   * @requires items != null &&
   *           for each element in item, element!= null &&
   *     the Collection is backed by a type that gives a consistent iteration order
   */
  public void setDrawersCard(Collection<? extends Canvas> items){
    drawerCards.clear();
    menu.removeAll();
    List<JComponent> buttons  = new ArrayList<JComponent>();
    int i = 0;
    for(Canvas canvas : items){
      TabCard card = new TabCard(i, canvas, this, scrollable);
      drawerCards.add(card);
      buttons.add(card.getButton());
      card.getButton().setPreferredSize(new Dimension(125,BUTTON_HEIGHT));

      menu.add(card.getMenuItem());
      i++;
    }
    wheeler.setElements(buttons);
    this.selectCanvas(0);
  }
  public void addDrawersCard(Canvas item, int index) {
    // not implemented!
    throw new RuntimeException("method not implemented");
  }
  /**
   * Selects the canvas at the specified index.  If the index is
   * out of bounds, perform no action.
   * @param index - the index of the canvas to be viewed.
   *
   * @requires none (INDEX DOES NOT HAVE TO BE WITHIN BOUNDS)
   */
  public void selectCanvas(int index){
    if(index >=0 && index < drawerCards.size()){
      TabCard card = drawerCards.get(index);
      canvasPane.removeAll();
      canvasPane.add(card.getScroll());
      canvasPane.revalidate();
      canvasPane.repaint();
      for(TabCard otherCard :  drawerCards){
        otherCard.getButton().toggleSelected(false);
      }
      card.getButton().toggleSelected(true);
      this.selectedIndex = index;
      this.revalidate();
    }
  }
  public int getSelectedIndex(){
    return this.selectedIndex;
  }
  /**
   * Scroll to a particular draw at the specified index
   * @param index
   */
  void scrollToWheelItem(int index){
    if(index >=0 && index < drawerCards.size()){
      wheeler.scrollToWheelItem(drawerCards.get(index).getButton());
    }
  }
  /**
   * Reforms this explorer based on the new size or location of this explorer.
   * For some explorers whose implementation does not depend on the size of itself,
   * this method may trigger no action.
   */
  public void reformView(){}
  /**
   * @return a JCOmponent representation of this explorer
   */
  public JComponent getJComponent(){
    return this;
  }
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){
    timer.stop();
  }
  /**
   * Start the timer when the user presses the mouse.
   */
  public void mousePressed(MouseEvent e){
    if(e.getSource().equals(left)){
      directedToLeft = true;
      wheeler.scrollLeft();
      timer.start();
    }else if(e.getSource().equals(right)){
      directedToLeft = false;
      wheeler.scrollRight();
      timer.start();
    }else if(e.getSource().equals(down)){
      menu.show(down, 5, 5);
    }
  }
  /**
   * Stop the timer when the releases the mouse
   */
  public void mouseReleased(MouseEvent e){
    timer.stop();
  }
  /**
   * Continue to scroll to the left or right
   * for every time step
   */
  public void actionPerformed(ActionEvent e){
    if (directedToLeft){
      wheeler.scrollLeft();
    }else{
      wheeler.scrollRight();
    }
  }
}
