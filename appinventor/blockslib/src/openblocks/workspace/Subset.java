// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace;

import java.awt.Color;

import openblocks.renderable.RenderableBlock;

/**
 * An Immuateble class identifying a subset's properties and blocks
 */
public class Subset {
	private String name;
	private Color color;
	private Iterable<RenderableBlock> blocks;
	public Subset(String name, Color color, Iterable<RenderableBlock> blocks){
		this.name = name;
		this.color=color;
		this.blocks = blocks;
	}
	public String getName() {
		return name;
	}
	public Color getColor() {
		return color;
	}
	public Iterable<RenderableBlock> getBlocks() {
		return blocks;
	}
}
