// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.workspace;

import java.awt.Component;

/**
 * RBParents have methods for adding any Component to either the BlockLayer
 * or HighlightLayer.  The HighlightLayer must be rendered behind the BlockLayer
 * such that all Components on the HighlightLayer are rendered behind ALL
 * Components on the BlockLayer.
 *
 */
public interface RBParent {

  /**
   * Add this Component to the BlockNoteLayer, which is understood to be above
   * or at the BlockArrowLayer, although no guarantee is made about its order
   * relative to any other layers this RBParent may have.
   * @param c the Component to add
   */
  public void addToBlockNoteLayer(Component c);

  /**
   * Add this Component to the BlockArrowLayer, which is understood to be above
   *  the  BlockLayer, although no guarantee is made about its order relative
   * to any other layers this RBParent may have.
   * @param c the Component to add
   */
  public void addToBlockArrowLayer(Component c);

  /**
   * Add this Component to the BlockLayer, which is understood to be above the
   * HighlightLayer, although no guarantee is made about its order relative
   * to any other layers this RBParent may have.
   * @param c the Component to add
   */
  public void addToBlockLayer(Component c);

  /**
   * Add this Component to the HighlightLayer, which is understood to be
   * directly and completely beneath the BlockLayer, such that all Components
   * on the HighlightLayer are rendered behind ALL Components on the BlockLayer.
   * @param c the Component to add
   */
  public void addToHighlightLayer(Component c);

}
