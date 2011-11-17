package openblocks.workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;

import openblocks.renderable.BlockUtilities;
import openblocks.renderable.RenderableBlock;


public class TrashCan extends JComponent implements MouseListener, WorkspaceWidget, ComponentListener {
	private static final long serialVersionUID = 328149080275L;
	private Image tcImage;
	private Image openedTcImage;
	private Image currentImage;
	private Color currentColor = Color.BLACK;

	public TrashCan (Image trashCanImage, Image openedTrashCanImage) {
		this.tcImage = trashCanImage;
		this.openedTcImage = openedTrashCanImage;
		currentImage = tcImage;
		int width=0, height=0;

		if(tcImage.getWidth(null) > openedTcImage.getWidth(null))
			width = tcImage.getWidth(null);
		else
			width = openedTcImage.getWidth(null);

		if(tcImage.getHeight(null) > openedTcImage.getHeight(null))
			height = tcImage.getHeight(null);
		else
			height = openedTcImage.getHeight(null);
		if(width >0 && height > 0){
			setSize(width,height);
			setPreferredSize(new Dimension(width,height));
		}else{
			setSize(150,200);
			setPreferredSize(new Dimension(150,200));
		}
		this.setLayout(null);
		//Set the widget's location.
		this.setLocation(500, 400);
		
		addMouseListener(this);
		Workspace.getInstance().addComponentListener(this);
		
		//to make draggable, uncomment
		//JComponentDragHandler dragHandler = new JComponentDragHandler(this);
		//addMouseListener(dragHandler);
		//addMouseMotionListener(dragHandler);
	}


	public void paint(Graphics g) {
		if(currentImage ==  null){
			g.setColor(currentColor);
			((Graphics2D)g).fillRect(0,0,150,200); 
		}else
			g.drawImage(currentImage, 0, 0, null);
	}

        private void redrawTrashCanAfterDrop() {
		currentColor = Color.BLACK;
		currentImage = tcImage;
		this.revalidate();
		this.repaint();
        }

	public void blockDropped(RenderableBlock block) {
		// remove the block from the land of the living
                // note that BlockUtilities.deleteBlockUserConfirm was called earlier
		BlockUtilities.deleteBlock(block, false);
                redrawTrashCanAfterDrop();
	}

	public void blockDroppedNoDelete() {
                // redraw the trash can without actually doing the deletion
                redrawTrashCanAfterDrop();
	}

	public void addBlock(RenderableBlock block) {}
	public void addBlocks(Collection<RenderableBlock> block) {}
	public void removeBlock(RenderableBlock block) {}
	public void blockDragged(RenderableBlock block) {}

	/**
	 * Called when a RenderableBlock is being dragged and goes from being
	 * outside this Widget to being inside the Widget.
	 * @param block the RenderableBlock being dragged
	 */
	public void blockEntered(RenderableBlock block) {
		currentColor = Color.RED;
		currentImage = openedTcImage;
		repaint();
	}

	/**
	 * Called when a RenderableBlock that was being dragged over this Widget
	 * goes from being inside this Widget to being outside the Widget.
	 * @param block the RenderableBlock being dragged
	 */
	public void blockExited(RenderableBlock block) {
		currentColor = Color.BLACK;
		currentImage = tcImage;
		repaint();
	}

	public JComponent getJComponent() {
		return this;
	}

	public Collection<RenderableBlock> getBlocks(){
		return new ArrayList<RenderableBlock>();
	}

	/**Component Listeners**/
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentResized(ComponentEvent e){
		Point location = new Point(
				Workspace.getInstance().getWidth(),
				Workspace.getInstance().getHeight());
		location.translate(-this.getWidth()-50, -this.getHeight()-50);
		this.setLocation(location);
	}
	public void componentShown(ComponentEvent e){}
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e){}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
