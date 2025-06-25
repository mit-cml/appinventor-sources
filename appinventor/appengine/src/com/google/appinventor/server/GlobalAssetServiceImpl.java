package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
// import com.google.appinventor.shared.rpc.OdeAuthException;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class GlobalAssetServiceImpl extends OdeRemoteServiceServlet implements GlobalAssetService {

  private static final Logger LOG = Logger.getLogger(GlobalAssetServiceImpl.class.getName());
  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private static final String GLOBAL_ASSET_PREFIX = "_global_/";

  @Override
  public List<String> getGlobalAssetPaths() {
    String userId = userInfoProvider.getUserId();
    List<String> fullPaths = storageIo.getUserFiles(userId, GLOBAL_ASSET_PREFIX);
    List<String> relativePaths = new ArrayList<String>();
    for (String path : fullPaths) {
      if (path.startsWith(GLOBAL_ASSET_PREFIX)) {
        relativePaths.add(path.substring(GLOBAL_ASSET_PREFIX.length()));
      } else {
        // This case should ideally not happen if prefix filtering works correctly
        LOG.warning("UserFile for user " + userId + " expected to have prefix " +
                    GLOBAL_ASSET_PREFIX + " but found: " + path);
      }
    }
    return relativePaths;
  }
}
