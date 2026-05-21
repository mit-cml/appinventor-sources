// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

/**
 * BlocksCodeGenerationException is raised if {@link BlocklyPanel} fails to correctly generate
 * code for the given entity for editor's target platform.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class BlocksCodeGenerationException extends Exception {
  // The name of the entity being built when an error occurred
  private String entityName;

  BlocksCodeGenerationException() {
    entityName = "";
  }

  /**
   * Create a new BlocksCodeGenerationException for <code>entityName</code> with
   * <code>message</code>.
   *
   * @param message The error message stating the reason for failure
   * @param entityName The name of the entity that was being compiled/transpiled when the error
   *                   occurred
   */
  public BlocksCodeGenerationException(String message, String entityName) {
    super(message);
    this.entityName = entityName;
  }

  /**
   * Return the name of the entity code generation failed on.
   */
  public String getEntityName() {
    return entityName;
  }
}
