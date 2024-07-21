package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.jstypes.Blob;
import com.google.appinventor.client.utils.jstypes.BlobOptions;
import com.google.appinventor.client.utils.jstypes.DOMPurify;
import com.google.appinventor.client.utils.jstypes.PropertyChangeMessage;
import com.google.appinventor.client.utils.jstypes.URL;
import com.google.appinventor.client.utils.jstypes.Worker;
import com.google.appinventor.client.utils.jstypes.Worker.MessageEvent;
import com.google.appinventor.client.utils.jstypes.WorkerOptions;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;

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

    Ode.CLog("MockVisibleExtension.constructor");
    initComponent(rootPanel);
  }

  @Override
  public void onCreateFromPalette() {
    super.onCreateFromPalette();
    Ode.CLog("MockVisibleExtension.createFromPalette");

    Promise.<ChecksumedLoadFile>call(
            "Server error: could not load mock script for component: " + getVisibleTypeName(),
            cb -> Ode.getInstance().getProjectService().load2(projectId, mockFileId, cb))
        .then(
            result -> {
              String mockScript;
              try {
                mockScript = result.getContent();
              } catch (ChecksumedFileException e) {
                return Promise.reject(e.getMessage());
              }
              initWorker(mockScript);
              return Promise.resolve(result);
            });
  }

  private void initWorker(String mockScript) {
    String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();
    String[] parts =
        new String[] {
          "import { parseHTML } from '" + baseUrl + "/static/linkedom/linkedom.min.js';\n",
          "self.Mock = {\n",
          "  document: undefined,\n",
          "  template: () => undefined,\n",
          "  onPropertyChange: (property) => undefined\n",
          "};\n",
          mockScript + "\n",
          "const ____mockhtml = Mock.template();\n",
          "self.postMessage(____mockhtml);\n",
          "Mock.document = parseHTML(____mockhtml).document;\n",
          "onmessage = (msg) => {\n",
          "  Mock.onPropertyChange(msg.data);\n",
          "  postMessage(Mock.document.toString());\n",
          "};\n",
        };

    BlobOptions blobOpts = BlobOptions.create("text/javascript", "transparent");
    Blob blob = new Blob(parts, blobOpts);

    WorkerOptions workerOpts = WorkerOptions.create("module");
    worker = new Worker(URL.createObjectURL(blob), workerOpts);
    worker.addEventListener(
        "message",
        (MessageEvent event) -> {
          Ode.CLog("worker.message: dirty: " + event.getData().toString());
          String sanitizedData = DOMPurify.sanitize(event.getData().toString());
          Ode.CLog("worker.message: clean: " + sanitizedData);
          HTMLPanel html = new HTMLPanel(sanitizedData);
          rootPanel.clear();
          rootPanel.setStylePrimaryName("ode-SimpleMockComponent");
          rootPanel.add(html);
        });

    worker.addEventListener(
        "error",
        (Worker.ErrorEvent event) -> {
          Ode.CLog("worker.error: " + event.getMessage());
          Ode.CLog(String.valueOf(event.getError()));
        });
  }

  @Override
  public void delete() {
    if (worker != null) {
      worker.terminate();
    }
    super.delete();
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (worker != null) {
      PropertyChangeMessage msg = new PropertyChangeMessage(propertyName, newValue);
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
