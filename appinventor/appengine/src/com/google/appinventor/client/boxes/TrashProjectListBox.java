// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.TrashProjectList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for trash list.
 *
 */

public final class TrashProjectListBox extends Box {

    private static final TrashProjectListBox INSTANCE = new TrashProjectListBox();

    // Deleted Project list for young android
    private final TrashProjectList trashList;

    /**
     * Returns the singleton deleted projects list box.
     *
     * @return  trash project list box
     */
    public static TrashProjectListBox getTrashProjectListBox() {
        return INSTANCE;
    }

    /**
     * Creates new trash project list box.
     */
    private TrashProjectListBox() {
        super(MESSAGES.trashprojectlistbox(),
                300,    // height
                false,  // minimizable
                false); // removable

        trashList = new TrashProjectList();
        setContent(trashList);
    }

    /**
     * Returns trash project list associated with trash projects explorer box.
     *
     * @return trash project list
     */
    public TrashProjectList getTrashProjectList() {
        return trashList;
    }
}
