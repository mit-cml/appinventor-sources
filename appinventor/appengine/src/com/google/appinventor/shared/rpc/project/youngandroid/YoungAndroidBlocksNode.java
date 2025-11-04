package com.google.appinventor.shared.rpc.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.CODEBLOCKS_SOURCE_EXTENSION;

import com.google.appinventor.shared.storage.StorageUtil;


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
        + CODEBLOCKS_SOURCE_EXTENSION;
  }

  public static String getBlocklyFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + BLOCKLY_SOURCE_EXTENSION;
  }
}
