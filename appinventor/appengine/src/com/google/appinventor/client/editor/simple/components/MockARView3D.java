
package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.DOM;

import com.google.appinventor.components.common.ComponentConstants;
import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.Map;

/**
* Mock ARView3D component.
*
*/
public final class MockARView3D extends MockContainer {

  /**
  * Component type name.
  */
  public static final String TYPE = "ARView3D";

  // UI components
  private final AbsolutePanel arViewWidget;

  public MockARView3D(SimpleEditor editor) {
    super(editor, TYPE, images.arView3D(), new MockARView3DLayout());

    rootPanel.setHeight("100%");

    arViewWidget = new AbsolutePanel();
    arViewWidget.setStylePrimaryName("ode-SimpleMockContainer");
    arViewWidget.add(rootPanel);

    initComponent(arViewWidget);

    // We set the background image of the arViewWidget so it displays the image.
    // We need to override the background-size property so that is properly sized.
    String url = "images/arView3DBig.png";
    MockComponentsUtil.setWidgetBackgroundImage(this, arViewWidget, url);
    DOM.setStyleAttribute(arViewWidget.getElement(), "backgroundSize", "");
  }

  @Override
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    if (component instanceof MockARNode || component instanceof MockImageMarker || component instanceof MockARLight) {
      return true;
    }
    return false;
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_VIEW_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_VIEW_PREFERRED_HEIGHT;
  }

  @Override
  public void collectTypesAndIcons(Map<String, String> typesAndIcons) {
    super.collectTypesAndIcons(typesAndIcons);
    typesAndIcons.put("DetectedPlane", new Image(images.detectedPlane()).getElement().getString());
    typesAndIcons.put("CapsuleNode", new Image(images.capsuleNode()).getElement().getString());
    typesAndIcons.put("ConeNode", new Image(images.coneNode()).getElement().getString());
    typesAndIcons.put("BoxNode", new Image(images.boxNode()).getElement().getString());
    typesAndIcons.put("CylinderNode", new Image(images.cylinderNode()).getElement().getString());
    typesAndIcons.put("PlaneNode", new Image(images.planeNode()).getElement().getString());
    typesAndIcons.put("PyramidNode", new Image(images.pyramidNode()).getElement().getString());
    typesAndIcons.put("SphereNode", new Image(images.sphereNode()).getElement().getString());
    typesAndIcons.put("TextNode", new Image(images.textNode()).getElement().getString());
    typesAndIcons.put("TorusNode", new Image(images.torusNode()).getElement().getString());
    typesAndIcons.put("TubeNode", new Image(images.tubeNode()).getElement().getString());
    typesAndIcons.put("VideoNode", new Image(images.videoNode()).getElement().getString());
    typesAndIcons.put("WebViewNode", new Image(images.webViewNode()).getElement().getString());
  }
}
