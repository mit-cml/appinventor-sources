package com.google.appinventor.client.wizards;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;



public class ShareProjectUser extends Composite{
    interface ShareProjectUserUiBinder extends UiBinder<FlowPanel, ShareProjectUser> {}
    
    @UiField protected FlowPanel container;
    @UiField protected Label userEmailLabel;
    @UiField protected FocusPanel projectnameFocusPanel;
    @UiField protected ListBox permissionDropdown;

    HashMap<String, Integer> permissionMap = new HashMap<String, Integer>() {{
        put("1", 0);
        put("3", 1);
        put("5", 2);
    }};

    public ShareProjectUser(String userEmail, String permission, Runnable handlePermissionChange) {
        bindUI();
        userEmailLabel.setText(userEmail);
        permissionDropdown.setSelectedIndex(permissionMap.get(permission));
        permissionDropdown.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                // Call your function here
                handlePermissionChange.run();
            }
        });
    }

    public void bindUI() {
        ShareProjectUserUiBinder uibinder = GWT.create(ShareProjectUserUiBinder.class);
        initWidget(uibinder.createAndBindUi(this));
    }

    public String getEmail() {
        return userEmailLabel.getText();
    }

    public Integer getPermInteger() {
        return Integer.parseInt(permissionDropdown.getSelectedValue());
    }
}
