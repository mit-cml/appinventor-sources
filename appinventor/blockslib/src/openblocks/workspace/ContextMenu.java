package openblocks.workspace;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * ContextMenu handles right-click menu for BlockCanvas.
 * TODO Move this code to BlockCanvas.
 *
 */
public class ContextMenu extends PopupMenu implements ActionListener{
  private static final long serialVersionUID = 328149080421L;

  //context menu for canvas plus
  //menu items for canvas context menu
  private static ContextMenu canvasMenu = new ContextMenu();
  private static MenuItem arrangeAllBlocks;
  private static MenuItem collapseAllBlocks;
  private static MenuItem expandAllBlocks;
  private static MenuItem resizeWorkspace;
  private final static String ARRANGE_ALL_BLOCKS = "ARRANGE_ALL_BLOCKS";
  private final static String COLLAPSE_ALL_BLOCKS = "COLLAPSE_ALL_BLOCKS";
  private final static String EXPAND_ALL_BLOCKS = "EXPAND_ALL_BLOCKS";
  private final static String RESIZE_WORKSPACE = "RESIZE_WORKSPACE";
  private static boolean canvasMenuInit = false;

  /** The JComponent that launched the context menu in the first place */
  private static Object activeComponent = null;

  //privatize the constructor
  private ContextMenu(){
  }

  /**
   * Initializes the context menu for the BlockCanvas
   *
   */
  private static void initCanvasMenu(){
    arrangeAllBlocks = new MenuItem("Organize all blocks");
    arrangeAllBlocks.setActionCommand(ARRANGE_ALL_BLOCKS);
    arrangeAllBlocks.addActionListener(canvasMenu);
    collapseAllBlocks = new MenuItem("Collapse all blocks");
    collapseAllBlocks.setActionCommand(COLLAPSE_ALL_BLOCKS);
    collapseAllBlocks.addActionListener(canvasMenu);
    expandAllBlocks = new MenuItem("Expand all blocks");
    expandAllBlocks.setActionCommand(EXPAND_ALL_BLOCKS);
    expandAllBlocks.addActionListener(canvasMenu);
    resizeWorkspace = new MenuItem("Resize workspace");
    resizeWorkspace.setActionCommand(RESIZE_WORKSPACE);
    resizeWorkspace.addActionListener(canvasMenu);
    canvasMenu.add(arrangeAllBlocks);
    canvasMenu.add(collapseAllBlocks);
    canvasMenu.add(expandAllBlocks);
    canvasMenu.add(resizeWorkspace);
    canvasMenuInit = true;
  }

  /**
   * Returns the right click context menu for the specified JComponent.  If there is
   * none, returns null.
   * @param o JComponent object seeking context menu
   * @return the right click context menu for the specified JComponent.  If there is
   * none, returns null.
   */
  public static PopupMenu getContextMenuFor(Object o){
    if(o instanceof BlockCanvas){
      if(!canvasMenuInit)
        initCanvasMenu();
      activeComponent = o;
      return canvasMenu;
    }
    return null;
  }

  public void actionPerformed(ActionEvent a) {
    if (a.getActionCommand() == ARRANGE_ALL_BLOCKS) {
      // notify the component that launched the context menu in the first place
      if(activeComponent != null && activeComponent instanceof BlockCanvas){
        ((BlockCanvas)activeComponent).arrangeAllBlocks();
      }
    } else if(a.getActionCommand() == COLLAPSE_ALL_BLOCKS) {
      // notify the component that launched the context menu in the first place
      if(activeComponent != null && activeComponent instanceof BlockCanvas){
        ((BlockCanvas)activeComponent).collapseAllBlocks();
      }
    } else if(a.getActionCommand() == EXPAND_ALL_BLOCKS) {
      // notify the component that launched the context menu in the first place
      if(activeComponent != null && activeComponent instanceof BlockCanvas){
        ((BlockCanvas)activeComponent).expandAllBlocks();
      }
    } else if(a.getActionCommand() == RESIZE_WORKSPACE) {
      // notify the component that launched the context menu in the first place
      if(activeComponent != null && activeComponent instanceof BlockCanvas){
        ((BlockCanvas)activeComponent).resizePage();
      }
    }
  }
}
