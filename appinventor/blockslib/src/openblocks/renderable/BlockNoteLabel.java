package openblocks.renderable;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;

/**
 * The BlockNoteLabel class controls the visibility of a BlockNote on a
 * RenderableBlock
 *
 */
public class BlockNoteLabel extends BlockControlLabel {

  private static final long serialVersionUID = 1L;
  public static final int BLOCK_NOTE_LABEL_SIZE = 14; // pixels, unzoomed

  private final BlockNote balloon;

  public BlockNoteLabel(long blockID, BlockNote balloon, String labelText) {
    super(blockID);
    setText(labelText);
    this.balloon = balloon;
    setBackground(Color.darkGray);
    setOpaque(true);
  }

  @Override
  public void setActive(boolean active) {
    super.setActive(active);
    balloon.setVisible(active);
  }

  /**
   * setup current visual state of button
   */
  @Override
  public void update() {
    RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());
    if (rb != null) {
      Point location = rb.getUnscaledBlockNoteLabelLocation(this);
      setLocation(rb.rescale(location.x), rb.rescale(location.y));
      setSize(rb.rescale(BLOCK_NOTE_LABEL_SIZE), rb.rescale(BLOCK_NOTE_LABEL_SIZE));
      setForeground(isActive() ? Color.WHITE : Color.LIGHT_GRAY);
    }
  }

  /**
   * Implement MouseListener interface
   * toggle collapse state of block if button pressed
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    toggle();
    RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());
    balloon.setVisible(isActive());
    Workspace.getInstance().notifyListeners(new WorkspaceEvent(balloon.getRenderableBlock().
        getParentWidget(), WorkspaceEvent.BLOCK_NOTE_VISIBILITY_CHANGE));
    update();
    rb.revalidate();
    rb.repaint();
    Workspace.getInstance().getMiniMap().repaint();
  }

  /**
   * highlight button state
   * Flash balloon
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    super.mouseEntered(e);
    balloon.setVisible(true);
    balloon.showOnTop();
  }

  /**
   * de-highlight button state
   * Hide inactive balloon
   */
  @Override
  public void mouseExited(MouseEvent e) {
    super.mouseExited(e);
    if (!isActive()) {
      balloon.setVisible(false);
    }
  }
}
