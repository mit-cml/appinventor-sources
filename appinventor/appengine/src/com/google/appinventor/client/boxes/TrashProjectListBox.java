// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for trash list.
 *
 */

public final class TrashProjectListBox extends Box {

    private static final TrashProjectListBox INSTANCE = new TrashProjectListBox();

    // Deleted Project list for young android
    private final ProjectList deletedList;

    /**
     * Returns the singleton deleted projects list box.
     *
     * @return  trash project list box
     */
    public static TrashProjectListBox getTrashProjectListBox() {
        return INSTANCE;
    }

    /**
     * Creates new project list box.
     */
    private TrashProjectListBox() {
        super(MESSAGES.trashprojectlistbox(),
                300,    // height
                false,  // minimizable
                false); // removable

        deletedList = new ProjectList();
        setContent(deletedList);
    }

    /**
     * Returns project list associated with projects explorer box.
     *
     * @return  project list
     */
    public ProjectList getTrashProjectList() {
        return deletedList;
    }
}
