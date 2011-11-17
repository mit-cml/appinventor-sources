package openblocks.yacodeblocks;

import java.io.IOException;

/**
 * An ExternalController for codeblocks provides access to an external
 * environment for cases where codeblocks needs to communicate with
 * the outside world (e.g., to save its source code to an external system
 * via an http server).
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */

public interface ExternalController {

  /**
   * Cause the external controller to save codeblocks source code, represented
   * in String "contents" to a location identified by "path". The source code
   * is the .blk file which includes information about the blocks present on
   * the workspace and their layout.
   * @param path  location to save the source
   * @param contents  the contents of the source
   */
  public void writeCodeblocksSourceToServer(String path, String contents)
          throws IOException;

  /**
   * Gets the form properties for the current project.
   */
  public String getFormPropertiesForProject() throws IOException, NoProjectException;

  /**
   * Copy the contents named by "path" from the server and put it into a temp file.
   *
   * @param path the path of the contents to retrieve.
   * @throws java.io.IOException if an error occurs.
   */
  String downloadContentFromServer(String path) throws IOException;
}
