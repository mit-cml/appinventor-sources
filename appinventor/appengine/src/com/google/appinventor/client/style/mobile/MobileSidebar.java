// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

/**
 * A mobile-friendly sidebar component that can slide in and out from the side of the screen.
 * This component implements both Composite (for GWT widget composition) and HasWidgets
 * (to allow adding child widgets to the sidebar content area).
 *
 * The sidebar consists of:
 * - An overlay that covers the screen when open (clicking it closes the sidebar)
 * - The actual sidebar panel that slides in from the side
 * - A content area within the sidebar where widgets can be added
 *
 * The open/close state is managed via CSS classes and boolean state tracking.
 */

public class MobileSidebar extends Composite implements HasWidgets {

    interface MobileSidebarUiBinder extends UiBinder<FlowPanel, MobileSidebar> {}
    private static MobileSidebarUiBinder uiBinder = GWT.create(MobileSidebarUiBinder.class);

    @UiField FocusPanel overlay;
    @UiField SimplePanel sidebar;
    @UiField SimplePanel content;

    private boolean isOpen = false;

    public MobileSidebar() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void add(Widget w) {
        if (w != null) {
            content.add(w);
        }
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return content.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return content.remove(w);
    }

    public void open() {
        if (!isOpen) {
            isOpen = true;
            getWidget().addStyleName("open");
        }
    }

    public void close() {
        if (isOpen) {
            isOpen = false;
            getWidget().removeStyleName("open");
        }
    }

    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }


    @UiHandler("overlay")
    void onOverlayClick(ClickEvent event) {
        close();
    }

}