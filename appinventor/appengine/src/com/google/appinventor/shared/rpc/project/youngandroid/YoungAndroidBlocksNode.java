package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;


/**
 * Young Android blocks source file node in the project tree.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YoungAndroidBlocksNode extends YoungAndroidSourceNode {

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidBlocksNode() {
  }

  /**
   * Creates a new Young Android blocks source file project node.
   *
   * @param fileId  file id
   */
  public YoungAndroidBlocksNode(String fileId) {
    super(StorageUtil.basename(fileId), fileId);
  }

  public static String getCodeblocksFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + YoungAndroidSourceAnalyzer.CODEBLOCKS_SOURCE_EXTENSION;
  }

  public static String getBlocklyFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + YoungAndroidSourceAnalyzer.BLOCKLY_SOURCE_EXTENSION;
  }
}
