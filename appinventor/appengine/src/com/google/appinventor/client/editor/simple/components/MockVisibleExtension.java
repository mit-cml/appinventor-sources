package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.ShadowRoot;
import com.google.appinventor.client.utils.jstypes.*;
import com.google.appinventor.client.utils.jstypes.Worker.MessageEvent;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.MockInfo;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.components.common.PropertyTypeConstants;

public class MockVisibleExtension extends MockVisibleComponent {
  private final String MOCK_RENDER_REQUEST = "render-request";

  private final long projectId;
  private final String packageName;
  private final MockInfo mockInfo;

  private Worker worker;
  private String workerUrl;

  private final ShadowRoot shadowRoot;

  public MockVisibleExtension(
      SimpleEditor editor,
      String typeName,
      Image iconImage,
      String packageName,
      MockInfo mockInfo) {
    super(editor, typeName, iconImage);
    Ode.CLog("MockVisibleExtension.constructor");

    this.projectId = editor.getProjectId();
    this.packageName = packageName;
    this.mockInfo = mockInfo;

    final FlowPanel shadowHost = new FlowPanel();
    shadowHost.setStylePrimaryName("ode-MockVisibleExtensionHost");
    shadowRoot = applyAttachShadow(shadowHost.getElement());

    initializeMock()
        .then0(
            () -> {
              refreshForm();
              return null;
            });
    initComponent(shadowHost);
  }

  private Promise<Object> initializeMock() {
    Ode.CLog("MockVisibleExtension.initWorker");
    final String assetsBasePath = "assets/external_comps/" + packageName + "/";

    final Promise<String> fetchScript = fetchFileContent(assetsBasePath + mockInfo.getScript());
    final Promise<String> fetchCss;
    if (mockInfo.getCss() != null) {
      fetchCss = fetchFileContent(assetsBasePath + mockInfo.getCss());
    } else {
      fetchCss = null;
    }
    return Promise.<String[]>allOf(fetchScript, fetchCss)
        .then(
            files -> {
              if (files[1] != null && !files[1].isEmpty()) {
                final CSSStyleSheet[] css = new CSSStyleSheet[] {new CSSStyleSheet()};
                css[0].replaceSync(files[1]);
                applyAdoptedStyleSheets(shadowRoot, css);
              }

              initializeWorker(files[0]);

              for (EditableProperty p : getProperties()) {
                onPropertyChange(p.getName(), p.getValue());
              }

              return null;
            });
  }

  private Promise<String> fetchFileContent(String path) {
    return Promise.<String>call(
            "Server error: Could not load file: " + path,
            cb -> Ode.getInstance().getProjectService().load(projectId, path, cb))
        .then(Promise::resolve);
  }

  private void initializeWorker(String mockScriptSrc) {
    final String[] workerSrc = buildWorkerSource(mockScriptSrc);
    final BlobOptions blobOpts = BlobOptions.create("text/javascript", "transparent");
    final Blob blob = new Blob(workerSrc, blobOpts);

    workerUrl = URL.createObjectURL(blob);
    final WorkerOptions workerOpts = WorkerOptions.create("module");
    worker = new Worker(workerUrl, workerOpts);

    worker.addEventListener("message", this::handleMessageEvent);
    worker.addEventListener("error", this::handleErrorEvent);
  }

  private String[] buildWorkerSource(String mockScriptSrc) {
    final String escapedScript = JsonUtils.escapeValue(mockScriptSrc);
    final String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();

    return new String[] {
      "import { parseHTML } from '" + baseUrl + "/static/linkedom/linkedom.min.js';\n",
      "const Mock = {\n",
      "  document: undefined,\n",
      "  template: () => undefined,\n",
      "  onPropertyChange: (property) => undefined,\n",
      "  onError: (error) => undefined,\n",
      "};\n",
      "const mockScript = new Function('Mock', " + escapedScript + ");\n",
      "mockScript(Mock);\n",
      "const mockHTML = Mock.template();\n",
      "postMessage({\n",
      "  type: '" + MOCK_RENDER_REQUEST + "',\n",
      "  data: mockHTML\n",
      "});\n",
      "Mock.document = parseHTML(mockHTML).document;\n",
      "onmessage = (msg) => {\n",
      "  Mock.onPropertyChange(JSON.parse(msg.data));\n",
      "  postMessage({\n",
      "    type: '" + MOCK_RENDER_REQUEST + "',\n",
      "    data: Mock.document.toString()\n",
      "  });\n",
      "};\n",
      "onerror = (err) => Mock.onError(err);\n",
    };
  }

  private void handleMessageEvent(MessageEvent<WorkerMessage> msg) {
    final WorkerMessage msgData = msg.getData();
    Ode.CLog("worker.message: type: " + msgData.getType());

    if (msgData.getType().equals(MOCK_RENDER_REQUEST)) {
      final String htmlStr = msgData.getData().toString();
      Ode.CLog("worker.data: dirty: " + htmlStr);
      final String sanitizedData = DOMPurify.sanitize(htmlStr);
      Ode.CLog("worker.data: clean: " + sanitizedData);

      final HTMLPanel htmlPanel = new HTMLPanel(sanitizedData);
      htmlPanel.setStylePrimaryName("ode-SimpleMockComponent");
      shadowRoot.removeAllChildren();
      shadowRoot.appendChild(htmlPanel.getElement());
    }
  }

  // TODO
  private native void handleErrorEvent(Object error) /*-{
    console.log(error)
  }-*/;

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (worker == null) return;
    final JSONObject msg = new JSONObject();
    msg.put("name", new JSONString(propertyName));
    msg.put("value", convertPropertyValue(propertyName, newValue));
    worker.postMessage(msg.toString());
  }

  private JSONValue convertPropertyValue(String name, String value) {
    final String type = getProperties().getProperty(name).getEditorType();
    switch (type) {
      case PropertyTypeConstants.PROPERTY_TYPE_INTEGER:
      case PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER:
        return new JSONNumber(Integer.parseInt(value));
      case PropertyTypeConstants.PROPERTY_TYPE_FLOAT:
      case PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT:
        return new JSONNumber(Float.parseFloat(value));
      case PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN:
      case PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY:
        return JSONBoolean.getInstance(Boolean.parseBoolean(value));
      case PropertyTypeConstants.PROPERTY_TYPE_COLOR:
        {
          final String alpha = value.substring(2, 4);
          final String baseHex = value.substring(4);
          return new JSONString("#" + baseHex + alpha);
        }
      case PropertyTypeConstants.PROPERTY_TYPE_ASSET:
        {
          final String url = MockComponentsUtil.convertAssetValueToUrl(editor, value);
          if (url == null) return null;
          return new JSONString(url);
        }
      case PropertyTypeConstants.PROPERTY_TYPE_LENGTH:
        {
          final int intVal = Integer.parseInt(value);
          if (intVal <= LENGTH_PERCENT_TAG) {
            return new JSONString(Math.abs(intVal + 1000) + "%");
          } else if (intVal == LENGTH_PREFERRED) {
            return new JSONString("auto");
          } else if (intVal == LENGTH_FILL_PARENT) {
            return new JSONString("100%");
          } else {
            return new JSONString(intVal + "px");
          }
        }
      case PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE:
        {
          try {
            final int intVal = Integer.parseInt(value);
            if (intVal <= 1) {
              return new JSONString("sans-serif");
            } else if (intVal == 2) {
              return new JSONString("serif");
            } else if (intVal == 3) {
              return new JSONString("monospace");
            }
          } catch (NumberFormatException e) {
            final String typeface = value.substring(0, value.lastIndexOf("."));
            if (Document.get().getElementById(typeface) == null) {
              final String url = MockComponentsUtil.convertAssetValueToUrl(editor, value);
              MockComponentsUtil.createFontResource(typeface, url, typeface);
            }
            return new JSONString(typeface);
          }
        }
      default:
        return new JSONString(value);
    }
  }

  @Override
  public void upgrade() {
    super.upgrade();
    Ode.CLog("MockVisibleExtension.upgrade");
    cleanUpWorker();
    initializeMock()
        .then0(
            () -> {
              refreshForm();
              upgradeComplete();
              return null;
            });
  }

  @Override
  public void delete() {
    cleanUpWorker();
    super.delete();
  }

  private void cleanUpWorker() {
    worker.terminate();
    URL.revokeObjectURL(workerUrl);
  }

  private static native ShadowRoot applyAttachShadow(Element element) /*-{
      return element.attachShadow({ mode: 'open' });
  }-*/;

  private static native void applyAdoptedStyleSheets(
      Element shadowRoot, CSSStyleSheet[] styleSheets) /*-{
      shadowRoot.adoptedStyleSheets = styleSheets;
  }-*/;

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
