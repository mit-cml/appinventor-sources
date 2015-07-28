package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.FolderNode;


public interface HasComponentsFolder<T extends FolderNode> {
    T getComponentsFolder();
}