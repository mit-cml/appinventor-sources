// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlane;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlaneContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarker;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarkerContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLight;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLightContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNodeContainer;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import java.util.ArrayList;
import java.util.List;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "<p> A two dimensional container that renders an augmented reality view " +
      "and allows for multiple node, light, and ImageMarker components to be added to the view.  The rendered view " +
      "displays the live camera feed as well as any three dimensional virtual content added by the user." +
      "Before tracking has started, the ARView3D will be a black view.  The origin of the world, or the " +
      "<code>(0,0,0)</code> coordinate, is placed where the phone is when tracking is first started.</p>" +
      "<p>Although the ARView3D displays the live camera feed when tracking has started, it " +
      "does not need to take the entire size of the view.  It can be resized to cover as much " +
      "screenspace as desired.</p>",
    category = ComponentCategory.AR,
    androidMinSdk = 14)

@SimpleObject
@UsesLibraries({"ar-core.jar", "ar-core.aar"})
@UsesNativeLibraries(
    v7aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so",
    v8aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so",
    x86_64Libraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so"
)
public final class ARView3D extends AndroidViewComponent implements Component, ARNodeContainer,
    ARImageMarkerContainer, ARDetectedPlaneContainer, ARLightContainer,
    OnPauseListener, OnResumeListener, OnClearListener {

  private static final String LOG_TAG = ARView3D.class.getSimpleName();
  private final List<? extends Component> children = new ArrayList<>();
  private final GLSurfaceView view;

  private Session session;

  private boolean installRequested = false;

    public ARView3D(final ComponentContainer container) {
        super(container);
        view = new GLSurfaceView(container.$form());
        view.setPreserveEGLContextOnPause(true);
        container.$add(this);

        //Default Property Values
    }

    @Override
    public View getView() {
        return view;
    }

  @Override
  public List<? extends Component> getChildren() {
    return children;
  }

    // OnResumeListener implementation

    @Override
    public void onResume() {
        if (session != null) {
            return;
        }
        Exception exception = null;
        String message = null;
        try {
            switch (ArCoreApk.getInstance().requestInstall($form(), !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    return;
                case INSTALLED:
                    break;
            }

            // ARCore requires camera permissions to operate. If we did not yet obtain runtime
            // permission on Android M and above, now is a good time to ask the user for it.
            if ($form().isDeniedPermission(CAMERA)) {
                $form().askPermission(CAMERA, new PermissionResultHandler() {
                    @Override
                    public void HandlePermissionResponse(String permission, boolean granted) {
                        if (!granted) {
                            // TODO: Handle error
                        }
                    }
                });
                return;
            }

            // Create the session.
            session = new Session(/* context= */ $context());
            configureSession();
            session.resume();
            view.onResume();
        } catch (UnavailableArcoreNotInstalledException
                 | UnavailableUserDeclinedInstallationException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
            message = "This device does not support AR";
            exception = e;
        } catch (Exception e) {
            message = "Failed to create AR session";
            exception = e;
        }
        Toast.makeText($context(), message, Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, message, exception);
    }

    // OnPauseListener implementation

    @Override
    public void onPause() {
        if (session == null) {
            return;
        }
        view.onPause();
        session.pause();
    }

    // OnClearListener implementation

    @Override
    public void onClear() {
        onPause();
        session.close();
        session = null;
    }

    // PROPERTIES

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void ShowStatistics(boolean showStatistics) {  }

    @SimpleProperty(description = "Determines whether to show statistics such as frames per seconds " +
      "under the ARView3D.")
    public boolean ShowStatistics() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_TRACKING_TYPE, defaultValue = "1")
    public void TrackingType(int trackingType) {}

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "<p>The tracking type for the AR session.  WorldTracking allows for plane detection, " +
      "image detection with ImageMarkers, and the placement of nodes in the world.  The devices's position " +
      "and orientation are used to track the placemet of the world.  Nodes will remain where you place them " +
      "in the world, even if you move.  OrientationTracking allows for placing a nodes but using the devices's " +
      "orientation to determine location.  If you move, the items will move too.  They do not stay in place. " +
      "ImageTracking allows for using ImageMarkers and placing items relative to the images." +
      "Valid values are: 1 (WorldTracking), 2 (OrientationTracking), 3 (ImageTracking)")
    public int TrackingType() { return 1; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowWorldOrigin(boolean showWorldOrigin) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Determines whether to place a coordinate plane at the world origin (0,0,0) which " +
            "displays the x, y, and z axes.")
    public boolean ShowWorldOrigin() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowFeaturePoints(boolean showFeaturePoints) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "Determines whether to visualize features points as yellow points.  Feature points are " +
              "points for which their world coordinates are known.")
    public boolean ShowFeaturePoints() { return true; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowWireframes(boolean showWireframes) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
        description = "Determines whether to show the wireframe of nodes' geometries on top of their FillColor of Texture.")
    public boolean ShowWireframes() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowBoundingBoxes(boolean showBoundingBoxes) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
        description = "Determines whether to visualize the bounding box surrounding a node.")
    public boolean ShowBoundingBoxes() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_PLANE_DETECTION_TYPE, defaultValue = "1")
    public void PlaneDetectionType(int detectsPlanes) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
        description = "<p>Determines whether plane detection is enabled.  If this property is set to None, " +
          "then planes in the real world will not be detected.  Setting it to Horizontal detects horizontal " +
          "planes in the real world.  Setting it to Vertical detects vertical planes in the real world, and " +
          "setting it to both detects both horizontal and vertical planes.  When a plane is detected, a " +
          "DetectedPlane component will placed at that location.  This works when the TrackingType is WorldTracking.</p>" +
          "<p>Valid values are: 0 (None), 1 (Horizontal), 2 (Vertical), 3 (Both).</p>")
    public int PlaneDetectionType() { return 1; }

    @SimpleProperty(description = "The list of Nodes added to the ARView3D.")
    public List<ARNode> Nodes() { return new ArrayList<ARNode> (); }

    @SimpleProperty(description = "The list of DetectedPlanes added to the ARView3D.")
    public List<ARDetectedPlane> DetectedPlanes() { return new ArrayList<ARDetectedPlane> (); }

    @SimpleProperty(description = "The list of ImageMarkers added to the ARView3D.")
    public List<ARImageMarker> ImageMarkers() { return new ArrayList<ARImageMarker> (); }

    @SimpleProperty(description = "The list of Lights added to the ARView3D.")
    public List<ARLight> Lights() { return new ArrayList<ARLight> (); }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void LightingEstimation(boolean lightingEstimation) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Determines whether the real-world ambient light intensity and temperature should be estimated. " +
        "If this property is set to true, lighting estimates are provided.  Otherwise, not lighting estimates are " +
        "are provided.")
    public boolean LightingEstimation() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowLightAreas(boolean showLightAreas) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
        description = "If this property is set to true, this visualizes the areas in space that are lit by Lights " +
          "as well as the locations of the lights. " +
          "Otherwise, areas light by the light are not visualized.")
    public boolean ShowLightAreas() { return false; }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowLightLocations(boolean showLightLocations) {  }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
        description = "If this property is set to true, this visualizes the locations of lights.  " +
          "Otherwise, locations of lights are not visualized.")
    public boolean ShowLightLocations() { return false; }


    // FUNCTIONS

    @SimpleFunction(description = "Starts the live camera feed and begins tracking.")
    public void StartTracking() {}

    @SimpleFunction(description = "Pauses the live camera feed and pauses tracking.")
    public void PauseTracking() {}

    @SimpleFunction(description = "Resets the tracking, resetting all items including DetectedPlanes and ImageMarkers. " +
      "If this is called while tracking is not paused, then this resets and restarts tracking.  If tracking is paused " +
      "and this is called, this will reset the ARView3D once StartTracking is called again.")
    public void ResetTracking() {}

    @SimpleFunction(description = "Removed DetectedPlanes and resets detection for ImageMarkers.")
    public void ResetDetectedItems() {}

    @SimpleFunction(description = "Sets Visible to false for all Nodes.")
    public void HideAllNodes() {}

    @SimpleFunction(description = "Create a new BoxNode with default properties at the " +
      "specified (x,y,z) position.")
    public BoxNode CreateBoxNode(float x, float y, float z) {
      return new BoxNode(this);
    }

    @SimpleFunction(description = "Create a new SphereNode with default properties at the " +
      "specified (x,y,z) position.")
    public SphereNode CreateSphereNode(float x, float y, float z) {
      return new SphereNode(this);
    }

    @SimpleFunction(description = "Create a new PlaneNode with default properties at the " +
      "specified (x,y,z) position.")
    public PlaneNode CreatePlaneNode(float x, float y, float z) {
      return new PlaneNode(this);
    }

    @SimpleFunction(description = "Create a new CylinderNode with default properties at the " +
      "specified (x,y,z) position.")
    public CylinderNode CreateCylinderNode(float x, float y, float z) {
      return new CylinderNode(this);
    }

    @SimpleFunction(description = "Create a new ConeNode with default properties at the " +
      "specified (x,y,z) position.")
    public ConeNode CreateConeNode(float x, float y, float z) {
      return new ConeNode(this);
    }

    @SimpleFunction(description = "Create a new CapsuleNode with default properties at the " +
      "specified (x,y,z) position.")
    public CapsuleNode CreateCapsuleNode(float x, float y, float z) {
      return new CapsuleNode(this);
    }

    @SimpleFunction(description = "Create a new TubeNode with default properties at the " +
      "specified (x,y,z) position.")
    public TubeNode CreateTubeNode(float x, float y, float z) {
      return new TubeNode(this);
    }

    @SimpleFunction(description = "Create a new TorusNode with default properties at the " +
      "specified (x,y,z) position.")
    public TorusNode CreateTorusNode(float x, float y, float z) {
      return new TorusNode(this);
    }

    @SimpleFunction(description = "Create a new PyramidNode with default properties at the " +
      "specified (x,y,z) position.")
    public PyramidNode CreatePyramidNode(float x, float y, float z) {
      return new PyramidNode(this);
    }

    @SimpleFunction(description = "Create a new TextNode with default properties at the " +
      "specified (x,y,z) position.")
    public TextNode CreateTextNode(float x, float y, float z) {
      return new TextNode(this);
    }

    @SimpleFunction(description = "Create a new VideoNode with default properties at the " +
      "specified (x,y,z) position.")
    public VideoNode CreateVideoNode(float x, float y, float z) {
      return new VideoNode(this);
    }

    @SimpleFunction(description = "Create a new WebViewNode with default properties at the " +
      "specified (x,y,z) position.")
    public WebViewNode CreateWebViewNode(float x, float y, float z) {
      return new WebViewNode(this);
    }

    @SimpleFunction(description = "Sets Visible to false for all Lights.")
    public void HideAllLights() {}

    @Override
    public Activity $context() {
      return container.$context();
    }

    @Override
    public Form $form() {
      return container.$form();
    }

    @Override
    public void $add(AndroidViewComponent component) {}

    @Override
    public void setChildWidth(AndroidViewComponent component, int width) {}

    @Override
    public void setChildHeight(AndroidViewComponent component, int height) {}

    // EVENTS

    @Override
    @SimpleEvent(description = "The user tapped on a node in the ARView3D.")
    public void NodeClick(ARNode node) {}

    @Override
    @SimpleEvent(description = "The user long-pressed a node in the ARView3D.")
    public void NodeLongClick(ARNode node) {}

    @Override
    @SimpleEvent(description = "The user tapped on a point on the ARView3D.  (x,y,z) is " +
      "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
      "at that point and false otherwise.")
    public void TapAtPoint(float x, float y, float z, boolean isANodeAtPoint) {}

    @Override
    @SimpleEvent(description = "The user long-pressed on a point on the ARView3D.  (x,y,z) is " +
      "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
      "at that point and false otherwise.")
    public void LongPressAtPoint(float x, float y, float z, boolean isANodeAtPoint) {}

    @Override
    @SimpleEvent(description = "A real-world plane was detected.  The detectedPlane is the " +
      "component added at the location of the real-world plane.  This event will only trigger if " +
      "PlaneDetection is not None, and the TrackingType is WorldTracking.  Note that the default " +
      "FillColor of a DetectedPlane is None, so it is shown visually by default.")
    public void PlaneDetected(ARDetectedPlane detectedPlane) {}

    @Override
    @SimpleEvent(description = "A DetectedPlane updated its properties, either its rotation, " +
      "position, or size.  This event will only trigger if PlaneDetection is not None, and the " +
      "TrackingType is WorldTracking.")
    public void DetectedPlaneUpdated(ARDetectedPlane detectedPlane) {}

    @Override
    @SimpleEvent(description = "The user long-pressed on a DetectedPlane, detectedPlane.  (x,y,z) is " +
      "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
      "at that point and false otherwise.  This event will only trigger if PlaneDetection is not " +
      "None, and the TrackingType is WorldTracking.")
    public void LongClickOnDetectedPlaneAt(ARDetectedPlane detectedPlane, float x, float y, float z, boolean isANodeAtPoint) {}

    @Override
    @SimpleEvent(description = "The user tapped on a DetectedPlane, detectedPlane.  (x,y,z) is " +
      "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
      "at that point and false otherwise.  This event will only trigger if PlaneDetection is not " +
      "None, and the TrackingType is WorldTracking.")
    public void ClickOnDetectedPlaneAt(ARDetectedPlane detectedPlane, float x, float y, float z, boolean isANodeAtPoint) {}

    @Override
    @SimpleEvent(description = "A DetectedPlane was removed from the ARView3D.  This happens " +
    "when two DetectedPlanes are combined to form one or the detected items were reset.  " +
    "This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.")
    public void DetectedPlaneRemoved(ARDetectedPlane detectedPlane) {}

    @Override
    @SimpleEvent(description = "The lighting estimate has been updated.  This provides an " +
      "estimate for the real-world ambient lighting.  This event will only trigger if " +
      "LightingEstimation is true.")
    public void LightingEstimateUpdated(float ambientIntensity, float ambientTemperature) {}

    // Private implementation

    /** Configures the session with feature settings. */
    private void configureSession() {
        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
        session.configure(config);
    }
}
