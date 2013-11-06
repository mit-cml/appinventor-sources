// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks;

/**
 * <code>SocketRule</code> checks if the two sockets being matched can connect simply by checking if the socket/plug
 * match in kind.
 *
 */
public class SocketRule implements LinkRule {
	
    /**
     * Returns true if the two sockets of the two blocks can link by matching their socket kind; false if not.
     * Both sockets must be empty to return true.
     * @param block1 the associated <code>Block</code> of socket1
     * @param block2 the associated <code>Block</code> of socket2
     * @param socket1 a <code>Socket</code> or plug of block1
     * @param socket2 a <code>Socket</code> or plug of block2
     * @return true if the two sockets of the two blocks can link; false if not
     */
    public boolean canLink(Block block1, Block block2, BlockConnector socket1, BlockConnector socket2) {
    		// Make sure that none of the sockets are connected,
    		// and that exactly one of the sockets is a plug.
    		if (socket1.hasBlock() || socket2.hasBlock() ||
    			!((block1.hasPlug() && block1.getPlug() == socket1) ^
    			  (block2.hasPlug() && block2.getPlug() == socket2)))
    			return false;
    		
    		// If they both have the same kind, then they can connect
    		if (socket1.getKind().equals(socket2.getKind()))
    			return true;
    		
    		return false;
    }
    
    public boolean isMandatory() {
    		return false;
    }
}
