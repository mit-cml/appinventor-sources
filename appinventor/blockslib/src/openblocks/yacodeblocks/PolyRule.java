// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockLink;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblocks.LinkRule;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;

import java.util.ArrayList;

// Note: this was copied originally from
// third_party/java/openblocks/slcodeblocks/v20090424/PolyRule.java.

/*** About poly rule **********************************************************
 *
 * PolyRule checks linking ability of poly connectors and handles
 * setting/reverting them to/from different types.
 * There are two types of poly connectors, regular "poly" (POLY SINGLE) and
 * "poly-list (POLY LIST). POLY SINGLE can attach to anything, unless it is
 * operating on a list (in which case we prevent it from accepting any list
 * types) so that we avoid multi-level nested lists. POLY LIST can attach to
 * any type of list.
 *
 * ** General note:
 *      The majority of the complications in dealing with poly connectors has
 *      to do with differentiating between POLY SINGLE and POLY LIST, since
 *      POLY SINGLE is more general and can be set to POLY LIST type.
 *      The other complication occurs with multi-level nested lists not being
 *      allowed, but this may not always be the case, so we try to abstract this
 *      check, in case we want to change it in the future (should we want
 *      to allow multi-level nested lists, change "canLinkList", "setPolyKind",
 *      and "revertPolyKind").
 *
*** Vocabulary for the code description of this class is defined here with examples.
 *
 *  Connector types:
 *  - POLY:         (polymorphous, i.e. can change types)
 *  - POLY SINGLE:  conn type = "poly":          single hook (such as in "say")
 *  - POLY LIST:    conn type = "poly-list": double hook (such as in "length" of list)
 *  - TYPED:        (not poly, i.e. cannot change types)
 *  - TYPED LIST:   conn type does not contain poly and contains "list":
 *                   (three cases: "boolean-list", "number-list", "string-list")
 *  - TYPED SINGLE: conn type does not not poly and does not contain "list":
 *                   (all other cases, ex. "boolean", "number", "string")
 *
 *  Block types:
 *  - LIST-RELATED: block type = contains a list socket (includes "list" block
 *    and all list operators, like "add-items-to-list")
 *    special because: LIST-RELATED blocks are an exception case during POLY
 *    connector linking.
 *  - PROC-RELATED: block type = either a procedure declaration or procedure call block
 *      *special because: PROC-RELATED blocks are an exception case during POLY
 *      connector setting/reverting.
 *
 *  Setting/reverting terminology:
 *  - POLY CONN:    a connector of POLY type that needs to be set/reverted.
 *  - SETTER CONN:  a connector of any type that tells a POLY CONN what to
 *    set/revert to.
 *
 ******************************************************************************
 */

public class PolyRule implements LinkRule, WorkspaceListener {

    /**
     * Connector is initially a poly shape
     */
    private static boolean isInitPoly(BlockConnector socket) {
        return socket.initKind().contains("poly");
    }

    /**
     * Connector is currently a poly shape.
     */
    private static boolean isCurrentlyPoly(BlockConnector socket) {
        return socket.getKind().contains("poly");
    }

    /**
     * Block is PROC-RELATED (either a procedure declaration or a procedure call
     * block)
     * [changed for young android - sharon]
     */
    private static boolean isProcRelated(Long blockID) {
        Block block = Block.getBlock(blockID);
        String genusName = block.getGenusName();
        return (block.isProcedureDeclBlock()
                || genusName.equals("callerprocedure")
                || ProcedureBlockManager.isProcDeclBlock(block));
    }


/*** Rules for linking: *******************************************************
 *
 *  1) POLY SINGLE can attach to anything usually (LIST or SINGLE).  *
 *  The exception is if the POLY SINGLE connector is in a LIST-RELATED
 *  block, in which case it can only attach to a SINGLE. The reason
 *  for this is to prevent multi-level nested lists (this
 *  functionality may be changed later).  (Ex. "add-items-to-list" has both
 *  POLY SINGLE and POLY LIST connectors, but we don't want to add a
 *  list to a list, so we only take SINGLE plugs into the POLY SINGLE
 *  socket)
 *
 *  2)  POLY LIST can attach to any list (POLY LIST or TYPED LIST).
 *
 ****************************************************************************** */

    /**
     * Boolean function that returns if the two sockets being
     * connected follow the poly rules for linking.
     *
     * (See "Rules for linking" for explanation of rules.)
     *
     * @param block1 is one of the blocks being checked for linking
     * capability
     * @param block2 is the other block
     * @param socket1 is the socket in block1 being checked for
     * linking capability
     * @param socket2 is the socket in block2
     */
    public boolean canLink(Block block1, Block block2, BlockConnector socket1,
        BlockConnector socket2) {

        // Make sure that none of the sockets are connected, and that exactly
        // one of the sockets is a plug.
        boolean isPlug1 = block1.hasPlug() && block1.getPlug() == socket1;
        boolean isPlug2 = block2.hasPlug() && block2.getPlug() == socket2;
        if (socket1.hasBlock() || socket2.hasBlock() || !(isPlug1 ^ isPlug2))
            return false;

        // Boolean conditions to help test list linking
        boolean isPoly1 = socket1.getKind().contains("poly");
        boolean isPoly2 = socket2.getKind().contains("poly");
        boolean isList1 = socket1.getKind().contains("list");
        boolean isList2 = socket2.getKind().contains("list");
        boolean inListBlock1 = block1.isListRelated();
        boolean inListBlock2 = block2.isListRelated();

        // Linking rules:
        // At least one is a LIST -> true if list-linking true
        // Neither is a LIST -> true if one is POLY SINGLE
        if (isList1 || isList2) { // if at least one conn is LIST
            return canLinkList(isPoly1,isPoly2,isList1,isList2,inListBlock1,
                inListBlock2);
        }
        else // if neither conn is LIST and neither are cmds
            return ((isPoly1 || isPoly2) && !((socket1.getKind().contains("cmd"))
                     || socket2.getKind().contains("cmd")));
    }

    /**
     * We write this as a separate function that it will be easier to
     * go back and change, should we want to edit list linking
     * capability. Returns true if the two sockets being connected
     * follow the poly rules for linking, GIVEN that one of them is a
     * LIST (POLY or TYPED).
     *
     * (See "Rules for linking" for explanation of rules.)
     *
     * Order of first and second socket for the params does not matter.
     * @param isPoly1 tells if the first socket is POLY SINGLE or POLY LIST
     * @param isPoly2 tells if the second socket is
     * @param isList1 tells if the first socket is POLY LIST or TYPED LIST
     * @param isList2 tells if the second socket is
     * @param inListBlock1 tells if the first block is LIST-RELATED
     * @param inListBlock2 tells if the second block is LIST-RELATED
     */
    private boolean canLinkList(boolean isPoly1, boolean isPoly2,
            boolean isList1, boolean isList2,
            boolean inListBlock1,boolean inListBlock2) {
        // Linking rules:
        // Given one connector is a LIST, the other connector must either be:
        //  1) a LIST (POLY or same type)
        //  2) a POLY SINGLE not LIST-RELATED.
        return ((isList1 && isList2 && (isPoly1 ^ isPoly2)) ||
                (!isList1 && isPoly1 && !inListBlock1) ||
                (!isList2 && isPoly2 && !inListBlock2));
    }

    public boolean isMandatory() {
        return false;
    }


/*** Setting/reverting POLY connector types: **********************************
 *
 *--Basic idea:----------------------------------------------------------------
 *
 *      Connectors must be consistent a) across a link and b) within a block.
 *
 *      Therefore, the basic idea of POLY connector setting/reverting
 *      is that the initial connector that was part of the
 *      connected/disconnected link changes type, and it propagates
 *      the new type information to the other POLY connectors in the
 *      same block, which each propagate the new type information to
 *      the POLY connectors they are connected to, which each
 *      propagate the new type information to the other POLY
 *      connectors in their blocks, and so on. The process repeats
 *      until there are no POLY connectors left to convert. The
 *      exception is if the POLY connector is in a PROC-RELATED block,
 *      in which case the conversion does not propagate to the other
 *      POLY connectors in the block because procedures can take many
 *      types.
 *
 *      --> Technical note: The recursion always proceeds from one
 *      initial connector outwards in a single direction, meaning that
 *      if the connector may receive information across a link and
 *      propagate it to the rest of the block, or the other way
 *      around, but not both. We keep track of the ID of the last
 *      block checked so that the recursion doesn't travel backwards
 *      onto blocks we've already checked, preventing infinite loop.
 *
 *******************************************************************************
 */

    /**
     * Handles setting/reverting POLY connectors when a
     * BLOCKS_CONNECTED/ BLOCKS_DISCONNECTED event occurs.
     * Connecting: checks which connector is the POLY CONN and which
     * is the SETTER CONN, calls connectPoly with the SETTER
     * CONN. (See "Setting type specifics" for details on how to
     * determine this and why.)  Disconnecting: calls revertPoly on
     * both connectors. (See "Reverting type specifics" for details on
     * why.)  Finally, calls Procedure Output Manager to handle
     * changes in output block types (which are also POLY).
     */
    public void workspaceEventOccurred(WorkspaceEvent e) {
        BlockLink link = e.getSourceLink();
        // CONNECTING
        if (e.getEventType() == WorkspaceEvent.BLOCKS_CONNECTED) {
            boolean isPolyPlug = link.getPlug().getKind().contains("poly");
            boolean isPolySocket = link.getSocket().getKind().contains("poly");
            boolean isListPlug = link.getPlug().getKind().contains("list");
            boolean isListSocket = link.getSocket().getKind().contains("list");
            // one POLY SINGLE
            if ((isPolyPlug && !isListPlug)
                && !(isPolySocket && !isListSocket))
                connectPoly(link.getSocketBlockID(), link.getSocket());
            else if (isPolySocket && !isListSocket)
                connectPoly(link.getPlugBlockID(), link.getPlug());
            // no POLY SINGLE, one POLY LIST
            else if ((isPolyPlug && isListPlug)
                     && !(isPolySocket && isListSocket))
                connectPoly(link.getSocketBlockID(), link.getSocket());
            else if (isPolySocket && isListSocket)
                connectPoly(link.getPlugBlockID(), link.getPlug());
            // expandable sockets (only happens when dealing with LIST entries)
            else if (link.getSocket().isExpandable()) {
                setPolyConnectors(link.getPlug());
            }
        }
        // DISCONNECTING
        else if (e.getEventType() == WorkspaceEvent.BLOCKS_DISCONNECTED) {
            // call for each side of the broken connection.
            revertPoly(Block.getBlock(link.getPlugBlockID()), link.getPlug());
            revertPoly(Block.getBlock(link.getSocketBlockID()),
                       link.getSocket());
        }
    }

    /**
     * Helper function: given a block, we return all POLY connectors
     * of the block.
     * @param b is the block
     */
    private static Iterable<BlockConnector> getPolyConnectors(Block b) {
        ArrayList<BlockConnector> polySockets = new ArrayList<BlockConnector>();
        if (b.hasPlug() && isInitPoly(b.getPlug())) // add poly plugs
            polySockets.add(b.getPlug());
        for (BlockConnector socket : b.getSockets()) // add poly sockets
            if (isInitPoly(socket))
                polySockets.add(socket);
        return polySockets;
    }


/***Setting type specifics (case BLOCK_CONNECTED): ****************************
 *
 *--Initiation:----------------------------------------------------------------
 *  -   When two blocks are connected, we must first determine which, if any, is
 *      the connector that needs to be set.
 *          To do this, we order the "degree of poly-ness" for the
 *          connectors, and only when the "degree of poly-ness"
 *          differs between the connectors does the one with the lower
 *          degree (SETTER CONN) pass type information to the one with
 *          the higher degree (POLY CONN).  "Degree of poly-ness": 1)
 *          POLY SINGLE, 2) POLY LIST, 3) TYPED
 *
 *          The consequence of this is the following behavior: a) both
 *          connectors are the same type -> no information passed, do
 *          nothing b) one connector is POLY SINGLE and other is not
 *          -> other passes type to POLY SINGLE c) one connector is
 *          POLY LIST, and other is TYPED LIST -> other passes type to
 *          POLY LIST This allows POLY SINGLE to be set to POLY LIST,
 *          and not the other way around, though both are POLY.
 *
 *--The recursion:-------------------------------------------------------------
 *  - Once we determine the connector that needs to be set, we begin
 *  the recursive setting. The original lower degree connector is
 *  passed in as the SETTER CONN, and after it changes the type of the
 *  POLY CONN, the POLY CONN becomes the new SETTER CONN and the other
 *  POLY connectors in the block are queued to become the next POLY
 *  CONN.
 *
 *--How to set a type:---------------------------------------------------------
 *      Vocabulary:
 *          NEW TYPE:           type of the SETTER CONN
 *          SINGLE VERSION:     prefix of the NEW TYPE
 *          LIST VERSION:       prefix of the NEW TYPE + "-list"
 *
 *          Ex.  ( NEW TYPE = boolean -> SINGLE VERSION = boolean ->
 *          LIST VERSION = boolean-list NEW TYPE = boolean-list ->
 *          SINGLE VERSION = boolean -> LIST VERSION = boolean-list )
 *
 *  -   Rules:
 *      1)  If the POLY CONN is a POLY SINGLE
 *          a) If the SETTER CONN is SINGLE --> set to NEW TYPE.
 *          b) If the SETTER CONN is LIST,
 *              i) If the POLY CONN is not in a LIST-RELATED block -->
 *                 set to NEW TYPE.
 *              i) Else --> set to SINGLE VERSION. (i.e. when there is a mix
 *                 of LIST and SINGLE in the block, stay SINGLE)
 *      2)  If the POLY CONN is a POLY LIST --> set to LIST VERSION.
 *
 ******************************************************************************
 */

    /**
     * The initiation step. Determining the SETTER CONN is done when the WorkspaceEvent occurred, so this
     * only performs a double check that there is a POLY CONN to set. Then call recursion.
     *
     * @param setterBlockID is the ID of the block containing the SETTER CONN
     * @param setterConn is the SETTER CONN
     */
    private static void connectPoly(Long setterBlockID,
                                    BlockConnector setterConn) {
        // should always have block when called, but let's check anyway
        if (setterConn.hasBlock())
            setPolyConnectors(setterConn,setterBlockID);
    }

    /**
     * This method is specific to when the block expands new POLY sockets.
     * @param setterConn is the SETTER CONN
     */
    private static void setPolyConnectors(BlockConnector setterConn) {
        // should always have block when called, but let's check anyway
        if (setterConn.hasBlock()) {
            Block otherBlock = Block.getBlock(setterConn.getBlockID());
            for (BlockConnector polyConn : getPolyConnectors(otherBlock)) {
                if (isCurrentlyPoly(polyConn))
                    setPolyKind(otherBlock, polyConn, setterConn.getKind());
            }
            otherBlock.notifyRenderable();
        }
    }

    /**
     * The recursion step. The SETTER CONN sets all of the POLY
     * connectors in the block that it's connected to, unless the
     * block is PROC-RELATED, in which case we only set the directly
     * connected POLY connector. The POLY connectors that are set
     * become new SETTER CONNs and recurse.
     *
     * @param setterBlockID is the ID of the block containing the SETTER CONN
     * @param setterConn is the SETTER CONN
     */
    private static void setPolyConnectors(BlockConnector setterConn,
                                          Long setterBlockID) {
        Block otherBlock = Block.getBlock(setterConn.getBlockID());
        // Set only the directly connected POLY connector if block
        // is PROC-RELATED. Also, notify any stubs that parent connectors
        // changed.
        if (isProcRelated(otherBlock.getBlockID())) {
            BlockConnector polyConn = otherBlock.getConnectorTo(setterBlockID);
            setPolyKind(otherBlock, polyConn, setterConn.getKind());
            otherBlock.notifyRenderable();
            if (otherBlock.hasStubs() && ProcedureBlockManager.isArgSocket(polyConn)) {
                // Update stubs
                BlockStub.parentConnectorsChanged(otherBlock.getBlockID());
            }
        }
        // Otherwise set all POLY connectors in the block and recurse
        else {
            for (BlockConnector polyConn : getPolyConnectors(otherBlock)) {
                if (isCurrentlyPoly(polyConn)) {
                    setPolyKind(otherBlock, polyConn, setterConn.getKind());
                    // Make sure we don't recurse on blocks we've checked
                    // already
                    if (polyConn.hasBlock()
                        && !polyConn.getBlockID().equals(setterBlockID))
                        setPolyConnectors(polyConn, otherBlock.getBlockID());
                }
            }
            otherBlock.notifyRenderable();
        }
    }

    /**
     * Changes POLY connector to a new type.
     *
     * (See "Setting type specifics: how to set a type" for the rules and why.)
     *
     * @param polyBlock is the block containing the POLY CONN
     * @param polyConn is the POLY CONN
     * @param newType is the NEW TYPE (type of the SETTER CONN)
     */
    private static void setPolyKind(Block polyBlock, BlockConnector polyConn,
                                    String newType) {
        // Get the PREFIX of the NEW TYPE
        String prefix = newType;
        int index = prefix.indexOf("-");
        // "-inv" shapes contain "-" but we are looking for "-list"
        if (!prefix.contains("inv") && index >= 0) {
            prefix = prefix.substring(0, index);
        }
        // POLY CONN is POLY SINGLE
        if (polyConn.getKind().equals("poly")) {
            // SETTER CONN is SINGLE or LIST, and POLY CONN is not in a
            // LIST-RELATED block
            if (!polyBlock.isListRelated())
                polyConn.setKind(newType);
            // SETTER CONN is LIST, and POLY CONN is in a LIST-RELATED block
            else
                polyConn.setKind(prefix); //case 1a
        }
        // POLY CONN is POLY LIST
        else if (polyConn.getKind().startsWith("poly-")) {
            // set to LIST VERSION of PREFIX (prefix + "-list")
            prefix += polyConn.getKind().substring(4);
            polyConn.setKind(prefix); //case 2
        }
    }


/***Reverting type specifics (case BLOCK_DISCONNECTED): ***********************
 *
 *--Initiation:----------------------------------------------------------------
 *      - When two blocks are disconnected, both connectors need to be
 *      set back to their original types, if possible.  We check that
 *      it is possible to revert a connector by checking that it is
 *      POLY type and that there are no blocks still connected to it
 *      dictating the type. The latter is determined by recursively
 *      checking connected blocks for POLY connectors that cannot
 *      revert.
 *
 *--The recursion:-------------------------------------------------------------
 *      - Once we determine if the connector can be reverted, we begin
 *      the recursive reverting. The process is similar to the setting
 *      type recursion, except for the first round. During the first
 *      iteration, we use the initial disconnected connector to revert
 *      the types of the OTHER connectors within its OWN block. In
 *      future iterations, a reverted connector reverts the types of
 *      ALL connectors of the NEXT connected block. The reverted
 *      connector becomes the SETTER CONN and connectors of the next
 *      block are the POLY CONN. The reason for this initial round is
 *      that when a block disconnects, there is no connector connected
 *      to the initial block to be the SETTER CONN.
 *
 *          --> Technical note: We only have to check that we can
 *          revert for the initial connector and not during the
 *          recursion because the initial check was already recursive
 *          and had checked the rest along the way.
 *
 *--How to revert a type:------------------------------------------------------
 *  - When we revert a type, we know the initial type of the SETTER
 *  CONN and the current type of the SETTER CONN.  Vocabulary: INITIAL
 *  TYPE: either POLY SINGLE or POLY LIST (since we've already checked
 *  the connector is POLY) CURRENT TYPE: anything (since the connector
 *  may or may not have been reverted) POLY VERSION: suffix of the
 *  TYPE
 *
 *          Ex.     (   TYPE = boolean  ->  POLY VERSION = poly
 *                      TYPE = boolean-list ->  POLY VERSION = poly-list    )
 *
 *  -   Rules:
 *      1)  If the POLY CONN and SETTER CONN have the same INITIAL TYPE (both
 *          POLY or both POLY-LIST)
 *              --> set POLY CONN CURRENT TYPE to SETTER CONN CURRENT TYPE.
 *      2)  Else (one POLY and other POLY-LIST)
 *              --> set POLY CONN CURRENT TYPE to POLY VERSION of POLY
 *                  CONN CURRENT TYPE.
 *              (Ex. if we have "say"-"list"-"1", and we remove the "1", the
 *               "list" socket reverts from number
 *               to its original poly, the "list" plug reverts from number-list
 *               to its original poly-list,
 *               BUT the "say" socket reverts from number-list to poly-list
 *               and NOT its original poly.)
 *
 ******************************************************************************
 */

    /**
     * The initial recursive check to determine if the POLY connectors
     * of a block can revert.  We check that all the POLY connectors
     * of the block are connected to POLY connectors that can revert,
     * determining those recursively. If the block is PROC-RELATED, it
     * automatically returns true, since PROC-RELATED blocks don't
     * require POLY connector consistency. If it reaches the end of
     * the recursion with no conflicts, it also returns true. However,
     * if the POLY connector is connected to another connector that is
     * not initially POLY, then it returns false.
     *
     * @param b is the current block being checked
     * @param prevBlock is the last blocked checked, to prevent infinite loop
     * @return false iff at any point one of the POLY connectors
     * checked is connected to a TYPED connector
     */
    private static boolean canRevertPolyConnectors(Block b, Long prevBlock) {
        // Proc-related block is a dead-end (no need to check further)
        if (isProcRelated(b.getBlockID()))
            return true;
        Block otherBlock;
        // Propagate to connected blocks
        for (BlockConnector polyConn : getPolyConnectors(b)) {
            long prevIDval = polyConn.getBlockID().longValue();
            long prevBlockval = prevBlock.longValue();
            if (polyConn.hasBlock() && prevIDval != prevBlockval) {
                otherBlock = Block.getBlock(polyConn.getBlockID());
                if (!(isInitPoly(otherBlock.getConnectorTo(b.getBlockID())) &&
                        canRevertPolyConnectors(otherBlock, b.getBlockID())))
                    return false;
            }
        }
        return true;
    }

    /**
     * The initiation step. We first check that the initial connector
     * (the connector that was disconnected) is POLY and can be
     * reverted. If we pass the check, we set the initial connector to
     * its INITIAL TYPE.  If the block is PROC-RELATED, we stop the
     * recursion. Otherwise, we use the initial connector as the
     * SETTER CONN to revert the other POLY connectors. Then we call
     * recursion on connected blocks, with the newly reverted POLY
     * CONNs as SETTER CONNs.
     *
     * (See "Reverting type specifics: " for details on why we need an
     * initial round before recursion.)
     *
     * @param polyBlock is the block containing the initial connector
     * @param polyConn is the initial connector
     */
    private static void revertPoly(Block polyBlock, BlockConnector polyConn) {
        // Check that we can revert the initial connector.
        if (!(isInitPoly(polyConn)
              && canRevertPolyConnectors(polyBlock, Block.NULL)))
            return;
        // Revert the initial to its INITIAL TYPE
        polyConn.setKind(polyConn.initKind());
        // Stop recursion if block is PROC-RELATED
        if (isProcRelated(polyBlock.getBlockID())) {
            polyBlock.notifyRenderable();
            if(polyBlock.hasStubs() && ProcedureBlockManager.isArgSocket(polyConn)) {
                BlockStub.parentConnectorsChanged(polyBlock.getBlockID());
            }
        }
        // Otherwise set all POLY connectors in the block and begin recursion
        else {
            for (BlockConnector nextConn : getPolyConnectors(polyBlock)) {
                revertPolyKind(nextConn,polyConn);
                // Excludes initial connector, which doesn't have a
                // connected block
                if (nextConn.hasBlock())
                    revertPolyConnectors(nextConn,polyBlock.getBlockID());
            }
            polyBlock.notifyRenderable();
        }
    }

    /**
     * The recursion step. The SETTER CONN reverts all of the POLY
     * connectors in the block that it's connected to, unless the
     * block is PROC-RELATED, in which case we only revert the
     * directly connected POLY connector. The POLY connectors that are
     * revert become new SETTER CONNs and recurse.
     *
     * @param setterBlockID is the ID of the block containing the SETTER CONN
     * @param setterConn is the SETTER CONN
     */
    private static void revertPolyConnectors(BlockConnector setterConn,
                                             Long setterBlockID) {
        Block otherBlock = Block.getBlock(setterConn.getBlockID());
        // Revert only the directly connected POLY connector if block is
        // PROC-RELATED
        if (isProcRelated(otherBlock.getBlockID())) {
            BlockConnector polyConn = otherBlock.getConnectorTo(setterBlockID);
            revertPolyKind(polyConn,setterConn);
            otherBlock.notifyRenderable();
            if(otherBlock.hasStubs())
                BlockStub.parentConnectorsChanged(otherBlock.getBlockID());
        }
        // Otherwise revert all POLY connectors in the block and recurse
        else {
            for (BlockConnector polyConn : getPolyConnectors(otherBlock)) {
                revertPolyKind(polyConn,setterConn);
                // Make sure we don't recurse on blocks we've checked already
                if (polyConn.hasBlock()
                    && !polyConn.getBlockID().equals(setterBlockID))
                    revertPolyConnectors(polyConn,otherBlock.getBlockID());
            }
        }
        otherBlock.notifyRenderable();
    }

    /**
     * Reverts POLY connector to a POLY type.
     *
     * (See "Reverting type specifics: how to revert a type" for the
     * rules and why.)
     *
     * @param polyConn is the POLY CONN
     * @param setterConn is the SETTER CONN
     */
    private static void revertPolyKind(BlockConnector polyConn,
                                       BlockConnector setterConn) {
        // POLY CONN is not initially POLY (can't revert)
        if (!isInitPoly(polyConn))
            return;
        // POLY CONN and SETTER CONN have the same INITIAL TYPE
        // set to SETTER CONN CURRENT TYPE
        if (polyConn.initKind().equals(setterConn.initKind()))
            polyConn.setKind(setterConn.getKind());
        // POLY CONN and SETTER CONN have different INITIAL TYPEs
        // (one POLY and other POLY-LIST) set to POLY VERSION of POLY CONN
        // CURRENT TYPE
        else if (polyConn.getKind().contains("list"))
            polyConn.setKind("poly-list");
        else
            polyConn.setKind("poly");
    }

}
