package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.jstypes.Blob;
import com.google.appinventor.client.utils.jstypes.BlobOptions;
import com.google.appinventor.client.utils.jstypes.ComponentProperty;
import com.google.appinventor.client.utils.jstypes.DOMPurify;
import com.google.appinventor.client.utils.ShadowRoot;
import com.google.appinventor.client.utils.jstypes.URL;
import com.google.appinventor.client.utils.jstypes.Worker;
import com.google.appinventor.client.utils.jstypes.Worker.MessageEvent;
import com.google.appinventor.client.utils.jstypes.WorkerOptions;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.appinventor.components.common.PropertyTypeConstants;

public class MockVisibleExtension extends MockVisibleComponent {
  private final long projectId;
  private final String typeName;
  private final String packageName;

  private Worker worker = null;
  private String workerUrl = null;

  private final ShadowRoot shadowRoot;

  public MockVisibleExtension(
      SimpleEditor editor, String typeName, Image iconImage, String packageName) {
    super(editor, typeName, iconImage);

    this.projectId = editor.getProjectId();
    this.typeName = typeName;
    this.packageName = packageName;

    FlowPanel shadowHost = new FlowPanel();
    shadowHost.setStylePrimaryName("ode-MockVisibleExtensionHost");

    shadowRoot = attachShadow(shadowHost.getElement());

    HorizontalPanel loadingPanel = new HorizontalPanel();
    loadingPanel.setStylePrimaryName(".ode-MockVisibleExtensionLoading");
    iconImage.setWidth("24px");
    loadingPanel.add(iconImage);
    InlineHTML label = new InlineHTML("Loading " + typeName + "...");
    loadingPanel.add(label);

    shadowRoot.appendChild(loadingPanel.getElement());

    Ode.CLog("MockVisibleExtension.constructor");
    initComponent(shadowHost);
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
              final String mockScript;
              try {
                mockScript = result.getContent();
              } catch (ChecksumedFileException e) {
                return Promise.reject(e.getMessage());
              }

              String[] workerSrc = getWorkerSource(mockScript);
              BlobOptions blobOpts = BlobOptions.create("text/javascript", "transparent");
              Blob blob = new Blob(workerSrc, blobOpts);

              workerUrl = URL.createObjectURL(blob);
              WorkerOptions workerOpts = WorkerOptions.create("module");
              worker = new Worker(workerUrl, workerOpts);

              worker.addEventListener("message", this::handleMessageEvent);
              worker.addEventListener("error", this::handleErrorEvent);

              return Promise.resolve(result);
            });
  }

  private String[] getWorkerSource(String mockScript) {
    // Construct a JS object of the extension's properties and their values at
    // the time of initialization of the mock.
    JSONObject initialProps = new JSONObject();
    for (EditableProperty p : getProperties()) {
      initialProps.put(p.getName(), new JSONString(p.getValue()));
    }
    final String propsBuilder = initialProps.toString();

    final String escapedScript = new JSONString(mockScript).toString();
    final String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();

    return new String[] {
      "import { parseHTML } from '" + baseUrl + "/static/linkedom/linkedom.min.js';\n",
      "const Mock = {\n",
      "  document: undefined,\n",
      "  template: (initialProperties) => undefined,\n",
      "  onPropertyChange: (property) => undefined\n",
      "};\n",
      "const mockScript = new Function('Mock', " + escapedScript + ");\n",
      "mockScript(Mock);\n",
      "const mockHTML = Mock.template(" + propsBuilder + ");\n",
      "self.postMessage(mockHTML);\n",
      "Mock.document = parseHTML(mockHTML).document;\n",
      "onmessage = (msg) => {\n",
      "  Mock.onPropertyChange(msg.data);\n",
      "  postMessage(Mock.document.toString());\n",
      "};\n",
    };
  }

  private void handleMessageEvent(MessageEvent event) {
    Ode.CLog("worker.message: dirty: " + event.getData().toString());
    String sanitizedData = DOMPurify.sanitize(event.getData().toString());
    Ode.CLog("worker.message: clean: " + sanitizedData);
    HTMLPanel html = new HTMLPanel(sanitizedData);
    html.setStylePrimaryName(".ode-SimpleMockComponent");
    shadowRoot.removeAllChildren();
    shadowRoot.appendChild(html.getElement());
  }

  private void handleErrorEvent(Worker.ErrorEvent error) {
    Ode.CLog("worker.error: " + error.getMessage());
    Ode.CLog(String.valueOf(error.getError()));
  }

  private static native ShadowRoot attachShadow(Element element) /*-{
      return element.attachShadow({ mode: 'open' });
  }-*/;

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
      Object value = typeAndSanitizeProperty(propertyName, newValue);
      ComponentProperty msg = new ComponentProperty(propertyName, value);
      worker.postMessage(msg);
    }
  }

  private Object typeAndSanitizeProperty(String name, String value) {
    final String type = getProperties().getProperty(name).getEditorType();
    switch (type) {
      case PropertyTypeConstants.PROPERTY_TYPE_INTEGER:
      case PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER:
        return Integer.parseInt(value);
      case PropertyTypeConstants.PROPERTY_TYPE_FLOAT:
      case PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT:
        return Float.parseFloat(value);
      case PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN:
      case PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY:
        return Boolean.parseBoolean(value);
      case PropertyTypeConstants.PROPERTY_TYPE_COLOR:
        {
          String alpha = value.substring(2, 4);
          String baseHex = value.substring(4);
          return "#" + baseHex + alpha;
        }
      case PropertyTypeConstants.PROPERTY_TYPE_ASSET:
        {
          String url = MockComponentsUtil.convertAssetValueToUrl(editor, value);
          return URL.parse(url).toString();
        }
      case PropertyTypeConstants.PROPERTY_TYPE_LENGTH:
        {
          int intVal = Integer.parseInt(value);
          if (intVal <= LENGTH_PERCENT_TAG) {
            return Math.abs(intVal + 1000) + "%";
          } else if (intVal == LENGTH_PREFERRED) {
            return "auto";
          } else if (intVal == LENGTH_FILL_PARENT) {
            return "100%";
          } else {
            return intVal + "px";
          }
        }
      case "typeface":
        {
          try {
            int intVal = Integer.parseInt(value);
            if (intVal <= 1) {
              return "sans-serif";
            } else if (intVal == 2) {
              return "serif";
            } else if (intVal == 3) {
              return "monospace";
            }
          } catch (NumberFormatException e) {
            String typeface = value.substring(0, value.lastIndexOf("."));
            if (Document.get().getElementById(typeface) == null) {
              String url = MockComponentsUtil.convertAssetValueToUrl(editor, value);
              MockComponentsUtil.createFontResource(typeface, url, typeface);
            }
            return typeface;
          }
        }
      default:
        return value;
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
