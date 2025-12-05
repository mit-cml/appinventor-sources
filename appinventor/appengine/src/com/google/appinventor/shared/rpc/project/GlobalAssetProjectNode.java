
package com.google.appinventor.shared.rpc.project;

public class GlobalAssetProjectNode extends ProjectNode {
    private static final long serialVersionUID = -1213141414L;

    public GlobalAssetProjectNode(String name, String fileId) {
        super(name, fileId);
    }

    @Override
    public long getProjectId() {
        return 0;
    }

    @Override
    public ProjectRootNode getProjectRoot() {
        return null;
    }
}
