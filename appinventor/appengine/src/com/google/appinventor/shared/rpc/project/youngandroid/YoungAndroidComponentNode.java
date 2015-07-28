package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.FileNode;


public class YoungAndroidComponentNode extends FileNode {

    // For serialization
    //private static final long serialVersionUID = L; // TODO assign UID

    /**
     * Default constructor (for serialization only).
     */
    public YoungAndroidComponentNode() {
    }

    /**
     * Creates a new asset file project node.
     *
     * @param name  asset file name
     * @param fileId  file ID
     */
    public YoungAndroidComponentNode(String name, String fileId) {
        super(name, fileId);
    }
}
