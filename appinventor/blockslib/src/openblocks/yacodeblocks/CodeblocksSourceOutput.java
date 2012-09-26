// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 *
 *
 */
public class CodeblocksSourceOutput {

  private final String contents;
  private final String path;

  CodeblocksSourceOutput(String path, String contents) {
    this.path = path;
    this.contents = contents;
  }

  /**
   * Returns the path that this file ought to be written to.
   * @return the path
   */
  public String getPath(){
    return path;
  }

  /**
   * Returns the contents of this save file that should be persisted.
   * @return the file contents
   */
  public String getContents(){
    return contents;
  }
}
