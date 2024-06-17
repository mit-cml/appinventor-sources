package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.DedicatedWorker;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

import java.util.logging.Logger;

public class MockVisibleExtension extends MockComponent {
    private final String mockScript;

    private DedicatedWorker worker;
    private Button button;

    public MockVisibleExtension(SimpleEditor editor, String type, Image iconImage, String mockScript) {
        super(editor, type, iconImage);
        this.mockScript = mockScript;

        button = new Button();
        button.setText("waiting for something...");
        button.setStylePrimaryName("ode-SimpleMockComponent");
        button.setStyleName("ode-SimpleMockButton");
    }

    @Override
    public boolean isVisibleComponent() {
        return true;
    }

    @Override
    public void onCreateFromPalette() {
        worker = new DedicatedWorker(mockScript);
        worker.postMessage("ode: hello worker!");
        worker.addEventListener("message", event -> {
            button.setText(event.getData().toString());
            Logger.getGlobal().fine(event.getData().toString());
        });
    }
}
