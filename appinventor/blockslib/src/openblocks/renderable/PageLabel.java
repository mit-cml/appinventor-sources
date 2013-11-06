// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
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
