// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockNode {
	private Map<Integer, BlockNode> socketIndexToChildren = new HashMap<Integer, BlockNode>();
	private BlockNode afterNode = null;
	private String genusName = null;
	private String parentGenus = null;
	private String label = null;
	/**
	 * genusName != null
	 * @param genusName
	 * @param parentGenus
	 * @param label
	 */
	BlockNode (String genusName, String parentGenus, String label){
		if(genusName == null ) throw new RuntimeException("Requirement Clause violdated: genus name and label may not be null");
		this.genusName = genusName;
		this.parentGenus = parentGenus;
		this.label = label;
	}
	void addChild(BlockNode child, int socketIndex){
		if(child == null) return;
		socketIndexToChildren.put(socketIndex, child);
	}
	void setAfter(BlockNode after){
		afterNode = after;
	}
	BlockNode getAfterNode() {
		return afterNode;
	}
	Map<Integer, BlockNode> getChildren() {
		return socketIndexToChildren;
	}
	String getGenusName() {
		return genusName;
	}
	String getParentGenusName() {
		return parentGenus;
	}
	String getLabel() {
		return label;
	}
}