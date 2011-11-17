package openblocks.renderable;

import java.awt.Color;


class PageLabel extends BlockLabel{
	PageLabel(String initLabelText, BlockLabel.Type labelType, boolean isEditable, long blockID){
		super(initLabelText, labelType, isEditable, blockID, false, Color.yellow);
	}
	void update(){
		int x = 5;
        int y = 5;

		RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());
        if (rb != null) x += descale(rb.getControlLabelsWidth());
        
    	this.setPixelLocation( rescale(x), rescale(y));
	}
}
