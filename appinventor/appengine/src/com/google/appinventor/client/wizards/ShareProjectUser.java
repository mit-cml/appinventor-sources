package com.google.appinventor.client.wizards;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.core.client.GWT;



public class ShareProjectUser extends Composite{
    interface ShareProjectUserUiBinder extends UiBinder<FlowPanel, ShareProjectUser> {}
    
    @UiField protected FlowPanel container;
    @UiField protected Label userEmailLabel;
    @UiField protected FocusPanel projectnameFocusPanel;
    @UiField protected ListBox permissionDropdown;

    public ShareProjectUser(String userEmail, Integer permission) {
        bindUI();
        userEmailLabel.setText(userEmail);
        permissionDropdown.setSelectedIndex(permission);
    }

    public void bindUI() {
        ShareProjectUserUiBinder uibinder = GWT.create(ShareProjectUserUiBinder.class);
        initWidget(uibinder.createAndBindUi(this));
    }

    public String getEmail() {
        return userEmailLabel.getText();
    }

    public Integer getPermInteger() {
        return permissionDropdown.getSelectedIndex();
    }
}
