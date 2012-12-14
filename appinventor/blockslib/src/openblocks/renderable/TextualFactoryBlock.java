// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

/**
 * This private class acts as an IMMUTABLE ID tag for
 * factoryrenderableBlock.  But it does more.  It has a string
 * tag displayed through the toString method.  This allows
 * us to add TextualFactoryBlock rather than String objects
 * into menu.JList.  By overriding equals, we ensure that
 * AutoCompletePanel.menu when never have two equal items.
 */
public class TextualFactoryBlock {
  private final FactoryRenderableBlock block;
  private final String stringRepresentation;
  private final String ID;

  /**
   * Creates a TextualFactoryBlock with the given block and string representation
   * @param block the FactoryRenderableBlock tagged/represented by this TextualFactoryBlock
   * @param rep the String "representation" to use for this TFB.  Usually this would be the
   * block's label.  To auto-generate a detailed rep, use the disambiguousStringRep() method.
   */
  public TextualFactoryBlock(FactoryRenderableBlock block, String rep){
    this.block = block;
    this.stringRepresentation = rep;
    this.ID = BlockUtilities.disambiguousStringRep(block);
  }
  /** @return FactoryBlock contained in this TextualFactoryBlock */
  public FactoryRenderableBlock getfactoryBlock(){
    return this.block;
  }
  /** @return hashCode of this */
  public int hashCode(){
    return this.ID.hashCode();
  }
  public boolean equals(Object obj){
    if(obj instanceof TextualFactoryBlock){
      TextualFactoryBlock objBlock = (TextualFactoryBlock)obj;
      return this.ID.equals(objBlock.ID);
    }
    return false;
  }
  public int compareTo(TextualFactoryBlock b2){
    return this.ID.toLowerCase().compareTo(b2.ID.toLowerCase());
  }
  /** @return the string representation of this TextualFactoryBlock */
  public String toString(){
    return stringRepresentation;
  }
}
