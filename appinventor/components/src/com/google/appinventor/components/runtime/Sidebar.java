// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

/**
 * Container for sidebarItems.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
@DesignerComponent(version = YaVersion.SIDEBAR_COMPONENT_VERSION,
        category = ComponentCategory.LAYOUT,
        showOnPalette = false)
@SimpleObject
public class Sidebar extends AndroidViewComponent implements ComponentContainer{

    private Menu menu;

    /**
     * Creates a new AndroidViewComponent.
     *
     * @param container container, component will be placed in
     */
    public Sidebar(ComponentContainer container) {
        super(container);
        menu = $form().navigationView.getMenu();
        menu.clear();
        $form().navigationView.removeHeaderView($form().navigationView.getHeaderView(0));
    }

    public int additem(SidebarItem item) {
        menu.add("");
        $form().navigationView.invalidate();
        return (menu.size()-1);
    }

    public void addHeader(SidebarHeader sidebarHeader) {
        if(sidebarHeader.viewLayout.getLayoutManager().getParent()!=null) {
            ((ViewGroup)sidebarHeader.viewLayout.getLayoutManager().getParent()).removeView(sidebarHeader.viewLayout.getLayoutManager());
        }
        $form().navigationView.addHeaderView(sidebarHeader.viewLayout.getLayoutManager());
    }

    @Override
    public View getView() {
        return $form().navigationView;
    }

    @Override
    public Activity $context() {
        return container.$context();
    }

    @Override
    public Form $form() {
        return container.$form();
    }

    @Override
    public void $add(AndroidViewComponent<? extends View> component) {
    }

    @Override
    public void setChildWidth(AndroidViewComponent<? extends View> component, int width) {

    }

    @Override
    public void setChildHeight(AndroidViewComponent<? extends View> component, int height) {

    }
}
