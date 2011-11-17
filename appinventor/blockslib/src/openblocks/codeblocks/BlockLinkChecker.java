package openblocks.codeblocks;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import openblocks.renderable.RenderableBlock;

import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceListener;

/**
 * <code>BlockLinkChecker</code> determines if two <code>Block</code> objects can connect.  In particular, 
 * <code>BlockLinkChecker</code> will report which sockets of the two <code>Block</code> objects can connect.
 * Interested <code>Block</code> objects may make a static call to canLink() to determine if it can link to another
 * <code>Block</code> object.
 * 
 * <code>BlockLinkChecker</code> uses a list of <code>LinkRule</code>s to check the <code>Connector</code>s of each
 * <code>Block</code>.  Rules may be added, inserted, and removed from the checker.  
 * 
 * There is only one instance of the <code>BlockLinkChecker</code>.
 */
public class BlockLinkChecker {
    
    private static ArrayList<LinkRule> rules = new ArrayList<LinkRule>();
    
    // TODO get a better value
    private static double MAX_LINK_DISTANCE = 20.0;
    
    /**
     * Clears all the rules within this.
     */
    public static void reset(){
        rules.clear();
    }
    
    /**
     * Adds a rule to the end of this checker's list of rules.
     * If the rule already exists in the rule list, the rule is removed in the original location and 
     * added to the end of the list.
     * @param rule the desired LinkRule to be added
     */
    public static void addRule(LinkRule rule){
        rules.add(rule);
        if (rule instanceof WorkspaceListener)
        		Workspace.getInstance().addWorkspaceListener((WorkspaceListener)rule);
    }
    
    /**
     * Insert rule at the specified index in this checker's list of rules.  The original rule at the 
     * specified index and rules after it are shifted down the list. If the index is greater 
     * or equal to the length of the rule list, then the rule is added to the end of the list.  
     * If the rule already exists in the rule list, the rule is moved to the specified index.
     * @param rule the desired rule to insert
     * @param index the index to insert the rule in
     */
    public static void insertRule(LinkRule rule, int index){
        rules.remove(rule);
        rules.add(index, rule);
    }
    
    /**
     * Removes the specified rule from the rule list
     * @param rule the desired LinkRule to remove
     */
    public static void removeRule(LinkRule rule){
        rules.remove(rule);
    }

    /**
     * Returns a BlockLink instance if the two specified blocks can connect at the specified 
     * block connectors at each block; null if no link is possible.  
     * @param block1 Block instance to compare 
     * @param block2 Block instance to compare
     * @param con1 the BlockConnector at block1 to compare against con2
     * @param con2 the BlockConnector at block2 to compare against con1
     */
    public static BlockLink canLink(Block block1, Block block2, BlockConnector con1, BlockConnector con2){
        if(checkRules(block1, block2, con1, con2))
            return BlockLink.getBlockLink(block1, block2, con1, con2);
        
        return null;
    }

    /**
     * Checks to see if a <code>RenderableBlock</code>s can connect to other
     *  <code>RenderableBlock</code>s. This would mean that they have
     *  <code>BlockConnector</code>s that satisfy at least one of the <code>LinkRule</code>s,
     *  and that these sockets are in close proximity.
     * @param rblock1 one of the blocks to check
     * @param otherBlocks the other blocks to check against
     * @return a <code>BlockLink</code> object that gives the two closest matching
     *  <code>BlockConnector</code>s in these blocks, or null if no such matching exists.\
     */
    public static BlockLink getLink(RenderableBlock rblock1, Iterable<RenderableBlock> otherBlocks) {
    		if (rblock1.isCollapsed()) {
    		  return null;      //  Collapsed blocks can't link.
    		}
    		Block block1 = Block.getBlock(rblock1.getBlockID());
    		BlockConnector closestSocket1 = null;
    		BlockConnector closestSocket2 = null;
    		Block closestBlock2 = null;
    		double closestDistance = MAX_LINK_DISTANCE;

    		for (RenderableBlock rblock2 : otherBlocks) {
        		BlockConnector currentPlug = getPlugEquivalent(block1);
	    		Block block2 = Block.getBlock(rblock2.getBlockID());	            
	    		if (block1.equals(block2) || !rblock1.isVisible() || !rblock2.isVisible() 
	    		    || rblock1.isCollapsed() || rblock2.isCollapsed())
	    			continue;
	    		Point2D currentPlugPoint = null;
	    		Point2D currentSocketPoint = null;
	    		if (currentPlug != null) {
	    			currentPlugPoint = getAbsoluteSocketPoint(rblock1, currentPlug);
		    		for (BlockConnector currentSocket : getSocketEquivalents(block2)) {
		    			currentSocketPoint = getAbsoluteSocketPoint(rblock2, currentSocket);
		    			double currentDistance = currentPlugPoint.distance(currentSocketPoint);
		    			if ((currentDistance < closestDistance) && checkRules(block1, block2, currentPlug, currentSocket)) {
		    				closestBlock2 = block2;
		    				closestSocket1 = currentPlug;
		    				closestSocket2 = currentSocket;
		    				closestDistance = currentDistance;
		    			}
		    		}
	    		}
	    		
	    		currentPlug = getPlugEquivalent(block2);
	    		if (currentPlug != null) {
	    			currentPlugPoint = getAbsoluteSocketPoint(rblock2, currentPlug);
		    		for (BlockConnector currentSocket : getSocketEquivalents(block1)) {
		    			currentSocketPoint = getAbsoluteSocketPoint(rblock1, currentSocket);
		    			double currentDistance = currentPlugPoint.distance(currentSocketPoint);
		    			if ((currentDistance < closestDistance) && checkRules(block1, block2, currentSocket, currentPlug)) {
		    				closestBlock2 = block2;
		    				closestSocket1 = currentSocket;
		    				closestSocket2 = currentPlug;
		    				closestDistance = currentDistance;
		    			}
		    		}
    			}
    		}
    		
    		if (closestSocket1 == null) {
    			return null;
    		}
	    		
    		return BlockLink.getBlockLink(block1, closestBlock2, closestSocket1, closestSocket2);
    }
    
    /**
     * NOTE: ALWAYS prefer BlockLinkChecker.getLink over this method.
     * 
     * Checks to see if a <code>RenderableBlock</code>s can connect
     * to other <code>RenderableBlock</code>s, implying that rblock1
     * has at least one <code>BlockConnector</code>s that satisfies at
     * least one of the <code>LinkRule</code>s.
     * 
     * Does not require close proximity.
     * 
     * @param rblock1 one of the blocks to check
     * @param otherBlocks the other blocks to check against
     * @return a <code>BlockLink</code> object that gives the two closest matching <code>BlockConnector</code>s in these blocks,
     * or null if no such matching exists.
     */
    public static BlockLink getWeakLink(RenderableBlock rblock1, Iterable<RenderableBlock> otherBlocks) {
    		Block block1 = Block.getBlock(rblock1.getBlockID());
    		BlockConnector closestSocket1 = null;
    		BlockConnector closestSocket2 = null;
    		Block closestBlock2 = null;
    		double closestDistance = Double.POSITIVE_INFINITY;
    		double currentDistance;

    		for (RenderableBlock rblock2 : otherBlocks) {
        		BlockConnector currentPlug = getPlugEquivalent(block1);
	    		Block block2 = Block.getBlock(rblock2.getBlockID());	            
	    		if (block1.equals(block2) || !rblock1.isVisible() || !rblock2.isVisible())
	    			continue;

	    		Point2D currentPlugPoint = null;
	    		Point2D currentSocketPoint = null;
	    		if (currentPlug != null) {
	    			currentPlugPoint = getAbsoluteSocketPoint(rblock1, currentPlug);
		    		for (BlockConnector currentSocket : getSocketEquivalents(block2)) {
		    			currentSocketPoint = getAbsoluteSocketPoint(rblock2, currentSocket);
		    			currentDistance = currentPlugPoint.distance(currentSocketPoint);
		    			if ((currentDistance < closestDistance) && checkRules(block1, block2, currentPlug, currentSocket)) {
		    				closestBlock2 = block2;
		    				closestSocket1 = currentPlug;
		    				closestSocket2 = currentSocket;
		    				closestDistance = currentDistance;
		    			}
		    		}
	    		}
	    		
	    		currentPlug = getPlugEquivalent(block2);
	    		if (currentPlug != null) {
	    			currentPlugPoint = getAbsoluteSocketPoint(rblock2, currentPlug);
		    		for (BlockConnector currentSocket : getSocketEquivalents(block1)) {
		    			currentSocketPoint = getAbsoluteSocketPoint(rblock1, currentSocket);
		    			currentDistance = currentPlugPoint.distance(currentSocketPoint);
		    			if ((currentDistance < closestDistance) && checkRules(block1, block2, currentSocket, currentPlug)) {
		    				closestBlock2 = block2;
		    				closestSocket1 = currentSocket;
		    				closestSocket2 = currentPlug;
		    				closestDistance = currentDistance;
		    			}
		    		}
    			}
    		}
    		
    		if (closestSocket1 == null) {
    			return null;
    		}
	    		
    		return BlockLink.getBlockLink(block1, closestBlock2, closestSocket1, closestSocket2);
    }
    
    /**
     * Checks if a potential link satisfies ANY of the rules loaded into the link checker
     * @param block1 one Block in the potential link
     * @param block2 the other Block
     * @param socket1 the BlockConnector from block1 in the potential link
     * @param socket2 the BlockConnector from block2
     * @return true if the pairing of block1 and block2 at socket1 and socket2 passes any rules, false otherwise
     */
    private static boolean checkRules(Block block1, Block block2, BlockConnector socket1, BlockConnector socket2) {
    		Iterator<LinkRule> rulesList = Collections.unmodifiableList(rules).iterator();
    		LinkRule currentRule = null;
    		boolean foundRule = false;
    		while(rulesList.hasNext()) {
    			currentRule = rulesList.next();
    			boolean canLink = currentRule.canLink(block1, block2, socket1, socket2);
    			if (!currentRule.isMandatory())
    				foundRule |= canLink;
    			else if (!canLink)
    				return false;
    		}
    		return foundRule;
    }
    
    /**
     * Gets the screen coordinate of the center of a socket.
     * @param block the RenderableBlock containting the socket
     * @param socket the desired socket
     * @return a Point2D that represents the center of the socket on the screen.
     */
    private static Point2D getAbsoluteSocketPoint(RenderableBlock block, BlockConnector socket) {
    		Point2D relativePoint = block.getSocketPixelPoint(socket);
    		Point2D blockPosition = block.getLocationOnScreen();
    		return new Point2D.Double(relativePoint.getX() + blockPosition.getX(), relativePoint.getY() + blockPosition.getY());
    }
    
    public static boolean hasPlugEquivalent(Block b) {
    		if (b == null)
    			return false;
    		boolean hasPlug = b.hasPlug();
    		boolean hasBefore = b.hasBeforeConnector();
    		// Should have at most one plug-type connector
    		assert(!(hasPlug & hasBefore));
    		return hasPlug | hasBefore;
    }
    
    public static BlockConnector getPlugEquivalent(Block b) {
    		if (!hasPlugEquivalent(b))
    			return null;
    		if (b.hasPlug())
    			return b.getPlug();
    		return b.getBeforeConnector();
    }
    
    public static Iterable<BlockConnector> getSocketEquivalents(Block b) {
    		if (b == null)
    			return new ArrayList<BlockConnector>();
    		if (!b.hasAfterConnector())
    			return b.getSockets();
    		ArrayList<BlockConnector> socketEquivalents = new ArrayList<BlockConnector>();
    		for (BlockConnector socket : b.getSockets()) {
    			socketEquivalents.add(socket);
    		}
    		socketEquivalents.add(b.getAfterConnector());
    		return Collections.unmodifiableList(socketEquivalents);
    }
    
    /**
     * Prints to the console all the rules this LinkChecker currently supports.
     */
    public static void printRules(){
        
    }
    
}
