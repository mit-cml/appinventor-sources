package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.Blob;
import com.google.appinventor.client.utils.Blob.BlobOptions;
import com.google.appinventor.client.utils.Worker;
import com.google.appinventor.client.utils.JsURL;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Worker.MessageEvent;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.gwt.user.client.ui.*;

public class MockVisibleExtension extends MockVisibleComponent {
  private final long projectId;
  private final String mockFileId;
  private final HorizontalPanel rootPanel;

  private Worker worker = null;

  public MockVisibleExtension(
      SimpleEditor editor, String type, Image iconImage, String mockFileId) {
    super(editor, type, iconImage);

    this.mockFileId = mockFileId;
    this.projectId = editor.getProjectId();

    rootPanel = new HorizontalPanel();
    rootPanel.setStylePrimaryName("ode-MockVisibleExtensionLoading");

    iconImage.setWidth("24px");
    rootPanel.add(iconImage);

    InlineHTML label = new InlineHTML("Loading " + type + "...");
    rootPanel.add(label);

    Ode.CLog("constructor...");
    initComponent(rootPanel);
  }

  @Override
  public void onCreateFromPalette() {
    super.onCreateFromPalette();
    Ode.CLog("initing...");
    Promise.<ChecksumedLoadFile>call(
                "Server error: could not mock script for component: " + getVisibleTypeName(),
                cb -> Ode.getInstance().getProjectService().load2(projectId, mockFileId, cb))
            .then(
                result -> {
                  Blob blob;
                  try {
                    String[] parts = new String[] {result.getContent()};
                    BlobOptions options = new BlobOptions("text/javascript", "transparent");
                    blob = new Blob(parts, options);
                  } catch (ChecksumedFileException e) {
                    return Promise.reject(e.getMessage());
                  }

                  worker = new Worker(JsURL.createObjectURL(blob));
                  worker.postMessage("ping");
                  worker.addEventListener(
                      "message",
                      (MessageEvent event) -> {
                        HTMLPanel html = new HTMLPanel(event.getData().toString());
                        rootPanel.clear();
                        rootPanel.setStylePrimaryName("ode-SimpleMockComponent");
                        rootPanel.add(html);
                      });
                  return Promise.resolve(result);
                });
  }

  @Override
  public void delete() {
    super.delete();
    if (worker != null) {
      worker.terminate();
    }
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (worker != null) {
      String msg = propertyName + ": " + newValue;
      worker.postMessage(msg);
    }
  }

  @Override
  public int getPreferredWidth() {
    return 128;
  }

  @Override
  public int getPreferredHeight() {
    return 48;
  }
}
