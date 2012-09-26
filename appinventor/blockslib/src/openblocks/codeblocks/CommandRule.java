// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks;

import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;

public class CommandRule implements LinkRule, WorkspaceListener {
	
	public CommandRule() {
	}

	public boolean canLink(Block block1, Block block2, BlockConnector socket1, BlockConnector socket2) {
		if (!BlockConnectorShape.isCommandConnector(socket1) || !BlockConnectorShape.isCommandConnector(socket2))
			return false;
		// We want exactly one before connector
		if (socket1 == block1.getBeforeConnector())
			return !socket1.hasBlock();
		else if (socket2 == block2.getBeforeConnector())
			return !socket2.hasBlock();
		return false;
	}
	
	public boolean isMandatory() {
		return false;
	}

	public void workspaceEventOccurred(WorkspaceEvent e) {
		// TODO Auto-generated method stub
		if (e.getEventType() == WorkspaceEvent.BLOCKS_CONNECTED) {
			BlockLink link = e.getSourceLink();
			if (link.getLastBlockID() != null && link.getLastBlockID() != Block.NULL &&
				BlockConnectorShape.isCommandConnector(link.getPlug()) && BlockConnectorShape.isCommandConnector(link.getSocket())) {
				Block top = Block.getBlock(link.getPlugBlockID());
				while (top.hasAfterConnector() && top.getAfterConnector().hasBlock())
					top = Block.getBlock(top.getAfterBlockID());
				Block bottom = Block.getBlock(link.getLastBlockID());
				
				// For safety: if either the top stack is terminated, or
				// the bottom stack is not a starter, don't try to force a link
				if (!top.hasAfterConnector() || !bottom.hasBeforeConnector())
				    return;
				
				link = BlockLink.getBlockLink(top, bottom, top.getAfterConnector(), bottom.getBeforeConnector());
				link.connect();
			}
		}
	}

}
