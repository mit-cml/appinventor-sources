// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;


/**
 * Command for previewing files.
 *
 */
public class PreviewFileCommand extends ChainableCommand {
    /**
     * Creates a new command for previewing a file.
     */
    public PreviewFileCommand() {
        super(null); // no next command
    }

    @Override
    public boolean willCallExecuteNextCommand() {
        return false;
    }

    @Override
    public void execute(final ProjectNode node) {
        Downloader.getInstance().download(
                StorageUtil.getFilePath(node.getProjectId(), node.getFileId()));
    }
}
