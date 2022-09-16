// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.media.Image;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
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
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.arview.helper.TrackingStateHelper;
import com.google.appinventor.components.runtime.arview.helper.TapHelper;
import com.google.appinventor.components.runtime.arview.helper.DisplayRotationHelper;
import com.google.appinventor.components.runtime.arview.renderer.BackgroundRenderer;
import com.google.appinventor.components.runtime.arview.renderer.PlaneRenderer;
import com.google.appinventor.components.runtime.arview.renderer.SpecularCubeMapFilter;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlane;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlaneContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarker;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarkerContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLight;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLightContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNodeContainer;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.DepthPoint;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
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
@UsesLibraries({"ar-core.jar", "ar-core.aar", "obj-0.3.0.jar"})
@UsesNativeLibraries(
    v7aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so",
    v8aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so",
    x86_64Libraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so"
)
@UsesPermissions({CAMERA})
public final class ARView3D extends AndroidViewComponent implements Component, ARNodeContainer, ARImageMarkerContainer,
   ARDetectedPlaneContainer, ARLightContainer, OnPauseListener, OnResumeListener, OnClearListener, OnDestroyListener,
   ARViewRender.Renderer {

    private static final String LOG_TAG = ARView3D.class.getSimpleName();
    private static final String SEARCHING_PLANE_MESSAGE = "Searching for Surfaces.";
    private static final String WAITING_FOR_TAP_MESSAGE = "Tap on the Surface.";

    private static final float[] sphericalHarmonicFactors = {
      0.282095f,
      -0.325735f,
      0.325735f,
      -0.325735f,
      0.273137f,
      -0.273137f,
      0.078848f,
      -0.273137f,
      0.136569f,
    };
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;
    private static final int CUBE_MAP_RESOLUTION = 16;
    private static final int CUBE_MAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;
    private final GLSurfaceView view;
    private boolean installRequested;
    private Session session;
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper;
    private TapHelper tapHelper;
    private ARViewRender arViewRender;
    private PlaneRenderer planeRenderer;
    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;
    private final DepthSettings depthSettings = new DepthSettings();
    private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
    private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;
    private VertexBuffer pointCloudVertexBuffer;
    private Mesh pointCloudMesh;
    private Shader pointCloudShader;
    private long lastPointCloudTimestamp = 0;
    private Mesh virtualObjectMesh;
    private Shader virtualObjectShader;
    private Texture virtualObjectAlbedoTexture;
    private Texture virtualObjectAlbedoInstantPlacementTexture;
    private final List<WrappedAnchor> wrappedAnchors = new ArrayList<>();
    private Texture dfgTexture;
    private SpecularCubeMapFilter cubeMapFilter;
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4];

    public ARView3D(final ComponentContainer container) {
        super(container);
        this.trackingStateHelper = new TrackingStateHelper(container.$context());
        this.displayRotationHelper = new DisplayRotationHelper(container.$context());
        this.tapHelper = new TapHelper(container.$context());
        this.view = new GLSurfaceView(container.$form());
        this.view.setPreserveEGLContextOnPause(true);
        this.view.setOnTouchListener(tapHelper);
        arViewRender = new ARViewRender(this.view, this, container.$context().getAssets());
        installRequested = false;
        depthSettings.onCreate(container.$context());
        instantPlacementSettings.onCreate(container.$context());
        container.$add(this);
    }

    // OnDestroyListener implementation
    @Override
    public void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }
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
            session = new Session($context());
            configureSession();
            session.resume();
            view.onResume();
            displayRotationHelper.onResume();
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

    @Override
    public View getView() {
        return view;
    }

    @Override
    public List<Component> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public void onPause() {
        if (session == null) {
            return;
        }
        displayRotationHelper.onPause();
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

    @Override
    public void onSurfaceCreated(ARViewRender render) {
        try {
            planeRenderer = new PlaneRenderer(render);
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, 1, 1);
            cubeMapFilter = new SpecularCubeMapFilter(render, CUBE_MAP_RESOLUTION, CUBE_MAP_NUMBER_OF_IMPORTANCE_SAMPLES);
            dfgTexture = new Texture(render, Texture.Target.TEXTURE_2D, Texture.WrapMode.CLAMP_TO_EDGE,false);
            final int dfgResolution = 64;
            final int dfgChannels = 2;
            final int halfFloatSize = 2;
            ByteBuffer buffer = ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize);
            try (InputStream is = $context().getAssets().open("dfg.raw")) {
                is.read(buffer.array());
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.getTextureId());
            GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture");
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D,0, GLES30.GL_RG16F, dfgResolution, dfgResolution,
                     0, GLES30.GL_RG, GLES30.GL_HALF_FLOAT, buffer);
            GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D");
            pointCloudShader = Shader.createFromAssets(render, "point_cloud.vert", "point_cloud.frag", /*defines=*/ null).setVec4("u_Color", new float[]{31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f}).setFloat("u_PointSize", 5.0f);
            pointCloudVertexBuffer = new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 4, /*entries=*/ null);
            final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
            pointCloudMesh = new Mesh(render, Mesh.PrimitiveMode.POINTS, /*indexBuffer=*/ null, pointCloudVertexBuffers);
            virtualObjectAlbedoTexture = Texture.createFromAsset(render, "pawn_albedo.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);
            virtualObjectAlbedoInstantPlacementTexture = Texture.createFromAsset(render, "pawn_albedo_instant_placement.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);
            Texture virtualObjectPbrTexture = Texture.createFromAsset(render, "pawn_roughness_metallic_ao.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.LINEAR);
            virtualObjectMesh = Mesh.createFromAsset(render, "pawn.obj");
            virtualObjectShader = Shader.createFromAssets(render, "environmental_hdr.vert", "environmental_hdr.frag",
                    /*defines=*/ new HashMap<String, String>() {
                        {
                            put("NUMBER_OF_MIPMAP_LEVELS", Integer.toString(cubeMapFilter.getNumberOfMipmapLevels()));
                        }
                    }).setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture).setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture).setTexture("u_Cubemap", cubeMapFilter.getFilteredCubemapTexture()).setTexture("u_DfgTexture", dfgTexture);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onSurfaceChanged(ARViewRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(ARViewRender render) {
        if (session == null) {
            return;
        }
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }
        displayRotationHelper.updateSessionIfNeeded(session);
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return;
        }
        Camera camera = frame.getCamera();
        try {
            backgroundRenderer.setUseDepthVisualization(render, depthSettings.depthColorVisualizationEnabled());
            backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return;
        }
        backgroundRenderer.updateDisplayGeometry(frame);
        if (camera.getTrackingState() == TrackingState.TRACKING && (depthSettings.useDepthForOcclusion() || depthSettings.depthColorVisualizationEnabled())) {
            try (Image depthImage = frame.acquireDepthImage16Bits()) {
                backgroundRenderer.updateCameraDepthTexture(depthImage);
            } catch (NotYetAvailableException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
        handleTap(frame, camera);
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());
        String message = null;
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
                message = SEARCHING_PLANE_MESSAGE;
            } else {
                message = TrackingStateHelper.getTrackingFailureReasonString(camera);
            }
        } else if (hasTrackingPlane()) {
            if (wrappedAnchors.isEmpty()) {
                message = WAITING_FOR_TAP_MESSAGE;
            }
        } else {
            message = SEARCHING_PLANE_MESSAGE;
        }
        if (message == null) {
            //messageSnackbarHelper.hide(this);
        } else {
            Toast.makeText($context(), message, Toast.LENGTH_SHORT).show();
        }
        if (frame.getTimestamp() != 0) {
            backgroundRenderer.drawBackground(render);
        }
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
        camera.getViewMatrix(viewMatrix, 0);
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
            if (pointCloud.getTimestamp() > lastPointCloudTimestamp) {
                pointCloudVertexBuffer.set(pointCloud.getPoints());
                lastPointCloudTimestamp = pointCloud.getTimestamp();
            }
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            pointCloudShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            render.draw(pointCloudMesh, pointCloudShader);
        }
        planeRenderer.drawPlanes(render, session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projectionMatrix);
        updateLightEstimation(frame.getLightEstimate(), viewMatrix);
        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
        for (WrappedAnchor wrappedAnchor : wrappedAnchors) {
            Anchor anchor = wrappedAnchor.getAnchor();
            Trackable trackable = wrappedAnchor.getTrackable();
            if (anchor.getTrackingState() != TrackingState.TRACKING) {
                continue;
            }
            anchor.getPose().toMatrix(modelMatrix, 0);
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
            virtualObjectShader.setMat4("u_ModelView", modelViewMatrix);
            virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            if (trackable instanceof InstantPlacementPoint && ((InstantPlacementPoint) trackable).getTrackingMethod() == InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE) {
                virtualObjectShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoInstantPlacementTexture);
            } else {
                virtualObjectShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture);
            }
            render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
        }
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
    }

    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            List<HitResult> hitResultList;
            if (instantPlacementSettings.isInstantPlacementEnabled()) {
                hitResultList =
                        frame.hitTestInstantPlacement(tap.getX(), tap.getY(), APPROXIMATE_DISTANCE_METERS);
            } else {
                hitResultList = frame.hitTest(tap);
            }
            for (HitResult hit : hitResultList) {
                Trackable trackable = hit.getTrackable();
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)
                        || (trackable instanceof InstantPlacementPoint)
                        || (trackable instanceof DepthPoint)) {
                    if (wrappedAnchors.size() >= 20) {
                        wrappedAnchors.get(0).getAnchor().detach();
                        wrappedAnchors.remove(0);
                    }
                    wrappedAnchors.add(new WrappedAnchor(hit.createAnchor(), trackable));
                    break;
                }
            }
        }
    }

    // PROPERTIES

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void ShowStatistics(boolean showStatistics) {
    }

    @SimpleProperty(description = "Determines whether to show statistics such as frames per seconds " + "under the ARView3D.")
    public boolean ShowStatistics() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_TRACKING_TYPE, defaultValue = "1")
    public void TrackingType(int trackingType) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "<p>The tracking type for the AR session.  WorldTracking allows for plane detection, " + "image detection with ImageMarkers, and the placement of nodes in the world.  The devices's position " + "and orientation are used to track the placemet of the world.  Nodes will remain where you place them " + "in the world, even if you move.  OrientationTracking allows for placing a nodes but using the devices's " + "orientation to determine location.  If you move, the items will move too.  They do not stay in place. " + "ImageTracking allows for using ImageMarkers and placing items relative to the images." + "Valid values are: 1 (WorldTracking), 2 (OrientationTracking), 3 (ImageTracking)")
    public int TrackingType() {
        return 1;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowWorldOrigin(boolean showWorldOrigin) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to place a coordinate plane at the world origin (0,0,0) which " + "displays the x, y, and z axes.")
    public boolean ShowWorldOrigin() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowFeaturePoints(boolean showFeaturePoints) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to visualize features points as yellow points.  Feature points are " + "points for which their world coordinates are known.")
    public boolean ShowFeaturePoints() {
        return true;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowWireframes(boolean showWireframes) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to show the wireframe of nodes' geometries on top of their FillColor of Texture.")
    public boolean ShowWireframes() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowBoundingBoxes(boolean showBoundingBoxes) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to visualize the bounding box surrounding a node.")
    public boolean ShowBoundingBoxes() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_PLANE_DETECTION_TYPE, defaultValue = "1")
    public void PlaneDetectionType(int detectsPlanes) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "<p>Determines whether plane detection is enabled.  If this property is set to None, " + "then planes in the real world will not be detected.  Setting it to Horizontal detects horizontal " + "planes in the real world.  Setting it to Vertical detects vertical planes in the real world, and " + "setting it to both detects both horizontal and vertical planes.  When a plane is detected, a " + "DetectedPlane component will placed at that location.  This works when the TrackingType is WorldTracking.</p>" + "<p>Valid values are: 0 (None), 1 (Horizontal), 2 (Vertical), 3 (Both).</p>")
    public int PlaneDetectionType() {
        return 1;
    }

    @SimpleProperty(description = "The list of Nodes added to the ARView3D.")
    public List<ARNode> Nodes() {
        return new ArrayList<ARNode>();
    }

    @SimpleProperty(description = "The list of DetectedPlanes added to the ARView3D.")
    public List<ARDetectedPlane> DetectedPlanes() {
        return new ArrayList<ARDetectedPlane>();
    }

    @SimpleProperty(description = "The list of ImageMarkers added to the ARView3D.")
    public List<ARImageMarker> ImageMarkers() {
        return new ArrayList<ARImageMarker>();
    }

    @SimpleProperty(description = "The list of Lights added to the ARView3D.")
    public List<ARLight> Lights() {
        return new ArrayList<ARLight>();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void LightingEstimation(boolean lightingEstimation) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether the real-world ambient light intensity and temperature should be estimated. " + "If this property is set to true, lighting estimates are provided.  Otherwise, not lighting estimates are " + "are provided.")
    public boolean LightingEstimation() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowLightAreas(boolean showLightAreas) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "If this property is set to true, this visualizes the areas in space that are lit by Lights " + "as well as the locations of the lights. " + "Otherwise, areas light by the light are not visualized.")
    public boolean ShowLightAreas() {
        return false;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowLightLocations(boolean showLightLocations) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "If this property is set to true, this visualizes the locations of lights.  " + "Otherwise, locations of lights are not visualized.")
    public boolean ShowLightLocations() {
        return false;
    }


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

    @SimpleFunction(description = "Create a new TubeNode with default properties at the " + "specified (x,y,z) position.")
    public TubeNode CreateTubeNode(float x, float y, float z) {
        return new TubeNode(this);
    }

    @SimpleFunction(description = "Create a new TorusNode with default properties at the " + "specified (x,y,z) position.")
    public TorusNode CreateTorusNode(float x, float y, float z) {
        return new TorusNode(this);
    }

    @SimpleFunction(description = "Create a new PyramidNode with default properties at the " + "specified (x,y,z) position.")
    public PyramidNode CreatePyramidNode(float x, float y, float z) {
        return new PyramidNode(this);
    }

    @SimpleFunction(description = "Create a new TextNode with default properties at the " + "specified (x,y,z) position.")
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

    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
        if (lightEstimate.getState() != LightEstimate.State.VALID) {
            virtualObjectShader.setBool("u_LightEstimateIsValid", false);
            return;
        }
        virtualObjectShader.setBool("u_LightEstimateIsValid", true);
        Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
        virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);
        updateMainLight(lightEstimate.getEnvironmentalHdrMainLightDirection(), lightEstimate.getEnvironmentalHdrMainLightIntensity(), viewMatrix);
        updateSphericalHarmonicsCoefficients(lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());
        cubeMapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
    }

    private void updateMainLight(float[] direction, float[] intensity, float[] viewMatrix) {
        worldLightDirection[0] = direction[0];
        worldLightDirection[1] = direction[1];
        worldLightDirection[2] = direction[2];
        Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
        virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectShader.setVec3("u_LightIntensity", intensity);
    }

    private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
        if (coefficients.length != 9 * 3) {
            throw new IllegalArgumentException("The given coefficients array must be of length 27 (3 components per 9 coefficients");
        }
        for (int i = 0; i < 9 * 3; ++i) {
            sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
        }
        virtualObjectShader.setVec3Array("u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
    }

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

class WrappedAnchor {
    private Anchor anchor;
    private Trackable trackable;

    public WrappedAnchor(Anchor anchor, Trackable trackable) {
        this.anchor = anchor;
        this.trackable = trackable;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public Trackable getTrackable() {
        return trackable;
    }
}
