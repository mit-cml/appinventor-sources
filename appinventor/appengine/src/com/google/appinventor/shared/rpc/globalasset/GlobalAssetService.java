package com.google.appinventor.shared.rpc.globalasset;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("globalasset")
public interface GlobalAssetService extends RemoteService {
  /**
   * Retrieves a list of global asset paths for the current user.
   * Paths are relative to the user's global asset root (i.e., "_global_/" prefix is stripped).
   * For example, if a global asset is stored as "_global_/my_icons/image.png",
   * this method would return "my_icons/image.png".
   *
   * @return A list of relative global asset paths.
   * @throws OdeAuthException if the user is not authenticated.
   */
  List<String> getGlobalAssetPaths();
}
