package openblocks.workspace.typeblocking;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockLink;
import openblocks.codeblocks.BlockLinkChecker;

/**
 * Utilities class to find links between two blocks
 */
public class LinkFinderUtil {
	
	/**
	 * Handles the Connecting of blocks once they are dropped
	 * onto the canvas and are waiting to be linked if posible.
	 * @param child
	 * @param parent
	 */
	protected static BlockLink connectBlocks(Block child, Block parent){
		//child must exist for there to be a link (no links to null instances allowed)
		if(invalidBlock(child)) return null;
		//parent must exist for there to be a link (no links to null instances allowed)
		if(invalidBlock(parent)) return null;
		
		//for every socket in child, see if you can connect
		//the parent's plug into it:
		// C-socket <==== P-plug
		if(parent.hasPlug()){
			for(BlockConnector socket : child.getSockets()){
				//continue for invalid sockets
				if (invalidConnector(socket)) continue;
				//if valid, then get link
				BlockLink link = BlockLinkChecker.canLink(child, parent, socket, parent.getPlug());
				//if link is invalid, continue
				if(link == null) continue;
				//if link is valid, then rturn it
				return link;
			}
		}
		
		//for every socket in parent, see if it can connect
		//to the child's plug:
		// P-socket <====== C-plug
		if(child.hasPlug()){
			for(BlockConnector socket : parent.getSockets()){
				//continue if invalid socket
				//Since we want block insertion between blocks, we don't
				//want to do a total invaliCOnector check (which tests for hasBlock())
				//if (invalidConnector(socket)) continue;
				if(socket == null) continue;
				
				if(child.isInfix()){
				//TAKE SPECIAL CARE FOR INFIX BLOCKS
					//we want to recurse to the top level infix block
					if(parent.isInfix()) continue;
					//if valid, the get link
					BlockLink link = BlockLinkChecker.canLink(parent, child, socket, child.getPlug());
					//if link is invalid, continue
					if(link == null) continue;
					//if link is valid, then rturn it
					return link;
				}else{
				//Don't NEED TO RECURSE for non-infix blocks
					//if valid, the get link
					BlockLink link = BlockLinkChecker.canLink(parent, child, socket, child.getPlug());
					//if link is invalid, continue
					if(link == null) continue;
					//if link is valid, then rturn it
					return link;
				}
			}
		}
		
		//for every socket in parent, see if it can connect
		//to the child's before
		// P-socket <======= C-before
		if(child.hasBeforeConnector()){
			for(BlockConnector socket : parent.getSockets()){
				//continue if invalid socket
				if (invalidConnector(socket)) continue;
				//if valid, the get link
				BlockLink link = BlockLinkChecker.canLink(parent, child, socket, child.getBeforeConnector());
				//if link is invalid, continue
				if(link == null) continue;
				//if link is valid, then rturn it
				return link;
			}
		}
		
		
		//see if we can connect the child's before
		//to the parent's after
		// P-after <===== C-before
		if(child.hasBeforeConnector()){
			if(parent.hasAfterConnector()){
				//before and after connectors exists
				BlockLink link = BlockLinkChecker.canLink(parent, child, parent.getAfterConnector(), child.getBeforeConnector());
				//if link is invalid, continue
				if(link == null){
					//continue;
				}else{
					//if link is valid, then rturn it
					return link;
				}
			}
		}
		
		
		//see if we can connect the child's after to
		//the parent's before
		// C-after <====== P-before
		if(child.hasAfterConnector()){
			if(parent.hasBeforeConnector()){
				//before and after connectors exists
				BlockLink link = BlockLinkChecker.canLink(child, parent, child.getAfterConnector(), parent.getBeforeConnector());
				//if link is invalid, continue
				if(link == null){
					//continue;
				}else{
					//if link is valid, then rturn it
					return link;
				}
			}
		}
		
		//if the parent has no sockets, try its parent
		if(parent.hasPlug()){
			return LinkFinderUtil.connectBlocks(child, Block.getBlock(parent.getPlugBlockID()));
		}
		
		return null;
	}
	
	/**
	 * @param block
	 * @return true iff block is invalid (points to a null instance of Block)
	 */
	private static boolean invalidBlock(Block block){
		if(block == null) {
			return true;
		}else {
			return invalidBlock(block.getBlockID());
		}
	}
	/**
	 * @param block
	 * @return true iff block is invalid (points to a null instance of Block)
	 */
	private static boolean invalidBlock(Long blockID){
		if(blockID == null) return true;
		if(blockID == Block.NULL) return true;
		else return false;
	}
	/**
	 * An invalid connector is defined as one that
	 * is either
	 * 		(1) NULL,
	 * 		(2) belongs to an instance of a null block,
	 * 		(3) or already has a block connected to it. 
	 * If a connector is invalid, this returns true.
	 * Otherwise, return false.
	 * @param connector
	 * @return true if connector is invalid
	 */
	private static boolean invalidConnector(BlockConnector connector){
		if(connector == null) return true;
		if(connector.hasBlock()) return true;
		return false;
	}
}
