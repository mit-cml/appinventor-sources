package openblocks.renderable;

import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

/**
 * CollapseLabel is a label that can be added to a renderable block that
 * will cause all blocks after this block to be hidden from view when
 * the isCollapsed parameter is true.
 *
 *
 */
class CollapseLabel extends BlockControlLabel {

  private static final long serialVersionUID = 1L;

  // we use small up/down triangles as collapse/expand
  // the offsets are hand-tweaked to match
  private static final String COLLAPSE_SYMBOL = "\u25B4";
  private static final String EXPAND_SYMBOL = "\u25BE";
  // offset of the hotspot -- the pointer is at center of hotspot
  private static final int LABEL_X_OFFSET = 0;
  private static final int LABEL_Y_OFFSET = 0;
  // size of the hotspot
  private static final int LABEL_WIDTH = 20;
  private static final int LABEL_HEIGHT = 40;

         CollapseLabel(long blockID) {
                 super(blockID);
        }

        /**
         * setup current visual state of button
         */
        @Override
        public void update() {
          RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());

          if (rb != null) {
            int x = 0;
            int y = 0;

            // The hotspot (label) is at the upper left of the block, as
            // specified by these offsets.

            y += LABEL_Y_OFFSET;
            x += LABEL_X_OFFSET;

            // Note: If we want to position the label at the bottom of the block, we
            // we could do it like this:
            // y += rb.getBlockHeight()/rb.getZoom() - 22 +
            // (isActive() ? BlockConnectorShape.CONTROL_PLUG_HEIGHT : 0);

            x=rb.rescale(x);
            y=rb.rescale(y);

            setLocation(x, y);
            setSize(rb.rescale(LABEL_WIDTH), rb.rescale(LABEL_HEIGHT));
            
            setVerticalAlignment(SwingConstants.CENTER);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (isActive()) {
              setText(EXPAND_SYMBOL);
            } else {
              setText(COLLAPSE_SYMBOL);
            }
          }
        }


    /**
     * Implement MouseListener interface
     * toggle collapse state of block if button pressed
     */
        @Override
        public void mouseClicked(MouseEvent e) {
        RenderableBlock.getRenderableBlock(getBlockID()).setCollapsed(!isActive());
        }
}
