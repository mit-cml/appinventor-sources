package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.jstypes.Blob;
import com.google.appinventor.client.utils.jstypes.BlobOptions;
import com.google.appinventor.client.utils.jstypes.DOMPurify;
import com.google.appinventor.client.utils.jstypes.ComponentProperty;
import com.google.appinventor.client.utils.jstypes.URL;
import com.google.appinventor.client.utils.jstypes.Worker;
import com.google.appinventor.client.utils.jstypes.Worker.MessageEvent;
import com.google.appinventor.client.utils.jstypes.WorkerOptions;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;

public class MockVisibleExtension extends MockVisibleComponent {
  private final long projectId;
  private final String typeName;
  private final String packageName;

  private Worker worker = null;
  private String workerUrl = null;

  private final HorizontalPanel rootPanel;

  public MockVisibleExtension(
      SimpleEditor editor, String typeName, Image iconImage, String packageName) {
    super(editor, typeName, iconImage);

    this.projectId = editor.getProjectId();
    this.typeName = typeName;
    this.packageName = packageName;

    rootPanel = new HorizontalPanel();
    rootPanel.setStylePrimaryName("ode-MockVisibleExtensionLoading");

    iconImage.setWidth("24px");
    rootPanel.add(iconImage);

    InlineHTML label = new InlineHTML("Loading " + typeName + "...");
    rootPanel.add(label);

    Ode.CLog("MockVisibleExtension.constructor");
    initComponent(rootPanel);
  }

  @Override
  public void onCreateFromPalette() {
    super.onCreateFromPalette();
    Ode.CLog("MockVisibleExtension.createFromPalette");
    final String mockScriptPath =
        "assets/external_comps/" + packageName + "/mocks/" + typeName + ".mock.js";
    Promise.<ChecksumedLoadFile>call(
            "Server error: could not load mock script for component: " + typeName,
            cb -> Ode.getInstance().getProjectService().load2(projectId, mockScriptPath, cb))
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
    String[] workerSrc = getWorkerSource(mockScript);
    BlobOptions blobOpts = BlobOptions.create("text/javascript", "transparent");
    Blob blob = new Blob(workerSrc, blobOpts);

    WorkerOptions workerOpts = WorkerOptions.create("module");
    workerUrl = URL.createObjectURL(blob);
    worker = new Worker(workerUrl, workerOpts);

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

  private String[] getWorkerSource(String mockScript) {
    // Construct a JS object of the extension's properties and their values at
    // the time of initialization of the mock.
    final StringBuilder initPropsBuilder = new StringBuilder("{ ");
    for (EditableProperty p : getProperties()) {
      initPropsBuilder.append("'" + p.getName() + "': '" + p.getValue() + "', ");
    }
    initPropsBuilder.append("}");

    final String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();
    final String script = mockScript
        .replaceAll("`", "\\\\`")
        .replaceAll("\\$\\{", "\\\\\\${");

    return new String[] {
      "import { parseHTML } from '" + baseUrl + "/static/linkedom/linkedom.min.js';\n",
      "const Mock = {\n",
      "  document: undefined,\n",
      "  template: (initialProperties) => undefined,\n",
      "  onPropertyChange: (property) => undefined\n",
      "};\n",
      "const mockScript = new Function('Mock', `\n" + script + "\n`);\n",
      "mockScript(Mock);\n",
      "const mockHTML = Mock.template(" + initPropsBuilder + ");\n",
      "self.postMessage(mockHTML);\n",
      "Mock.document = parseHTML(mockHTML).document;\n",
      "onmessage = (msg) => {\n",
      "  Mock.onPropertyChange(msg.data);\n",
      "  postMessage(Mock.document.toString());\n",
      "};\n",
    };
  }

  @Override
  public void delete() {
    if (worker != null) {
      worker.terminate();
      URL.revokeObjectURL(workerUrl);
    }
    super.delete();
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (worker != null) {
      ComponentProperty msg = new ComponentProperty(propertyName, newValue);
      worker.postMessage(msg);
    }
  }

  //  @Override
  //  public int getPreferredWidth() {
  //    return 128;
  //  }
  //
  //  @Override
  //  public int getPreferredHeight() {
  //    return 48;
  //  }
}
