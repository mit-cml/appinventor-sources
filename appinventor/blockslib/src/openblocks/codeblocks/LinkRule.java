package openblocks.codeblocks;

/**
 * <code>LinkRule</code> specifies a rule for <code>Block</code> linking.
 */
public interface LinkRule {

    /**
     * Returns true if the two sockets of the two blocks can link; false if not
     * @param block1 the associated <code>Block</code> of socket1
     * @param block2 the associated <code>Block</code> of socket2
     * @param socket1 a <code>Socket</code> of block1
     * @param socket2 a <code>Socket</code> of block2
     * @return true if the two sockets of the two blocks can link; false if not
     */
    public boolean canLink(Block block1, Block block2, BlockConnector socket1, BlockConnector socket2);
    
    public boolean isMandatory();
    
}
