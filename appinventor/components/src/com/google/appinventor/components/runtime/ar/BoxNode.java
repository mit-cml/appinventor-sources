package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;

import android.util.Log;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;


@UsesAssets(fileNames = "cube.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a box in an ARView3D.  The box is positioned " +
        "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)
@SimpleObject public final class BoxNode extends ARNodeBase implements ARBox {

    private Anchor anchor = null;
    private Trackable trackable = null;
    private String texture = "";
    private String objectModel = Form.ASSETS_PREFIX + "cube.obj";
    private float scale = 1.0f;


    public BoxNode(final ARNodeContainer container) {
      super(container);
      //parentSession = session;
      container.addNode(this);
    }

    @Override // wht is the significance?
    public Anchor Anchor() { return this.anchor; }

    @Override
    public void Anchor(Anchor a) { this.anchor = a;}

    @Override
    public Trackable Trackable() { return this.trackable; }

    @Override
    public void Trackable(Trackable t) { this.trackable = t;}

    @Override
    public float Scale() { return this.scale; }

    @Override
    public void Scale(float t) { this.scale = t;}

    @Override
    @SimpleProperty(description = "The 3D model file to be loaded.",
        category = PropertyCategory.APPEARANCE)
    public String Model() { return this.objectModel; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Model(String model) {this.objectModel = model;}

    @Override
    @SimpleFunction(description = "move a capsule node properties at the " +
        "specified (x,y,z) position.")
    public void MoveTo(float x, float y, float z){}

    @Override
    @SimpleFunction(description = "move a capsule node properties to detectedplane.")
    public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {
      this.trackable = (Trackable) targetPlane.DetectedPlane();
      if (this.anchor != null) {
        this.anchor.detach();
      }
      Anchor(this.trackable.createAnchor((Pose) p));

    }


    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the x-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float WidthInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void WidthInCentimeters(float widthInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float HeightInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the z-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float LengthInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void LengthInCentimeters(float lengthInCentimeters) {}

    @Override
    @SimpleProperty(description = "This determines how rounded the boxes corners will be.  " +
      "A value of zero specifies no rounded corners, and a value of half the length, " +
      "height, or width of the BoxNode (whichever is greater) makes it fully rounded, with " +
      "no straight edges.  Values less than zero will be set to zero.")
    public float CornerRadius() { return 0f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void CornerRadius(float cornerRadius) {}


    @Override
    @SimpleProperty(description = "Gets the 3D texture",
        category = PropertyCategory.APPEARANCE)
    public String Texture()  {
      return this.texture; }

    @Override
    @SimpleProperty(description = "The 3D texturebe loaded.",
        category = PropertyCategory.APPEARANCE)
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    public void Texture(String texture) {
      this.texture = texture;}
  }
