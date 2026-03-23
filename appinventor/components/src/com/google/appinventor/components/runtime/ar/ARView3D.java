package com.google.appinventor.components.runtime.ar;

import android.view.*;
import com.google.appinventor.components.runtime.*;
import static android.Manifest.permission.CAMERA;
import static com.google.appinventor.components.runtime.ar.SphereNode.SPHERE_OBJ_RADIUS;

import android.app.Activity;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import android.content.Context;
import android.graphics.PointF;

import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;


import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.ar.core.*;
import com.google.ar.core.Camera;
import org.json.JSONException;


import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.arview.helper.TrackingStateHelper;
import com.google.appinventor.components.runtime.arview.helper.TapHelper;
import com.google.appinventor.components.runtime.arview.helper.DisplayRotationHelper;
import com.google.appinventor.components.runtime.arview.renderer.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlane;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARDetectedPlaneContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarker;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARImageMarkerContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLight;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARLightContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNodeContainer;
import com.google.appinventor.components.runtime.util.CameraVectors;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.exceptions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import android.util.Log;
import org.osmdroid.views.overlay.gestures.RotationGestureDetector;

import java.util.stream.Collectors;

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
@UsesLibraries({"ar-core.jar", "ar-core.aar", "obj-0.3.0.jar",
        "filament-v1.9.11-android.jar",
        "gltfio-v1.9.11-android.jar"})
@UsesNativeLibraries(
        v7aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so, libfilament-jni.so, libgltfio-jni.so",
        v8aLibraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so, libfilament-jni.so, libgltfio-jni.so",
        x86_64Libraries = "libarcore_sdk_c.so,libarcore_sdk_jni.so, libfilament-jni.so, libgltfio-jni.so"
)
@UsesPermissions({CAMERA})
@UsesApplicationMetadata(metaDataElements = {
        @MetaDataElement(name = "com.google.ar.core", value = "required"),
        @MetaDataElement(name = "com.google.ar.core.min_apk_version", value = "221930000")
})
@UsesQueries(packageNames = {"com.google.ar.core"})
@UsesActivities(activities = {
        @ActivityElement(
                name = "com.google.ar.core.InstallActivity",
                configChanges = "keyboardHidden|orientation|screenSize",
                excludeFromRecents = "true",
                exported = "false",
                launchMode = "singleTop",
                theme = "@android:style/Theme.Material.Light.Dialog.Alert"
        )
})
@UsesAssets(fileNames = "ar_object.vert, ar_unlit_object.frag, ar_unlit_object.vert, ar_object.frag, plane.vert, plane.frag, point_cloud.vert, point_cloud.frag," +
        "cube.obj, plane.obj, sphere.obj, torus.obj, trigrid.png," +
        "background_show_camera.frag, background_show_camera.vert," +
        "basic.filamat, material_background.filamat, material_basic.filamat, occlusion2.filamat," +
        "background.frag, background.vert," +
        "basic.frag, basic.vert," +
        "background_show_depth_color_visualization.frag, background_show_depth_color_visualization.vert," +
        "occlusion.frag, occlusion.vert, depth_color_palette.png"
)
public class ARView3D extends AndroidViewComponent implements Component, ARNodeContainer,
        ARImageMarkerContainer, ARDetectedPlaneContainer, ARLightContainer,
        OnPauseListener, OnResumeListener, OnClearListener, OnDestroyListener, ARViewRender.IRenderer {

    private static final String LOG_TAG = ARView3D.class.getSimpleName();
    private static final String SEARCHING_PLANE_MESSAGE = "Searching for Surfaces.";
    private static final String WAITING_FOR_TAP_MESSAGE = "Tap on the Surface.";


    private static final float Z_NEAR = 0.085f;
    private static final float Z_FAR = 20f;


    // ARCore components
    private boolean installRequested;
    private Session session;
    private final TapHelper tapHelper;
    private final DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper;
    private boolean hasSetTextureNames = false;
    private final DepthSettings depthSettings = new DepthSettings();
    private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
    private boolean enableOcclusion = false;
    private boolean enableBoundingBoxes = false;
    private boolean enableWireframes = false;

    private boolean useSimulatedDepth = false;
    boolean isDepthSupported = false;
    private Config.DepthMode depthMode = Config.DepthMode.RAW_DEPTH_ONLY;
    // Rendering components
    private ARFilamentRenderer arFilamentRenderer;
    private BackgroundRenderer backgroundRenderer;
    private ObjectRenderer objRenderer;
    private PlaneRenderer planeRenderer;
    private PointCloudRenderer pointCloudRenderer;

    private final GLSurfaceView glSurfaceView;       // camera feed + sphere/box/capsule
    private final SurfaceView   filamentSurfaceView; // glTF models (Filament-owned)
    private final FrameLayout   frameLayout;         // returned by getView()
    private ARViewRender arViewRender;

    // Matrices and math components
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    private int currentViewportWidth = 0;
    private int currentViewportHeight = 0;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private float previousTwoFingerAngle = 0f;
    private enum ActiveGesture { NONE, SCALE, ROTATE }
    private ActiveGesture activeGesture = ActiveGesture.NONE;

    // Track dragging state
    private ARNode currentlyDraggedNode = null;

    private Frame lastFrame = null;
    private Camera lastCamera = null;
    private final float[] lastViewMatrix = new float[16];
    private final float[] lastProjMatrix = new float[16];

    private float GROUND_LEVEL = -1.0f;
    private float invisibleFloor = -1.0f;
    private boolean groundDetected = false;
    private int detectedPlaneType = 1;
    private float lastPhysicsUpdateTime = 0.0f;

    private Surface pendingFilamentSurface = null;
    private int pendingFilamentWidth  = 0;
    private int pendingFilamentHeight = 0;

    private List<ARNode> arNodes = Nodes();

    public ARView3D(final ComponentContainer container) {
        super(container);
        this.trackingStateHelper   = new TrackingStateHelper(container.$context());
        this.displayRotationHelper = new DisplayRotationHelper(container.$context());
        this.tapHelper             = new TapHelper(container.$context());

        // --- Bottom layer: GLSurfaceView for camera feed + ObjectRenderer ---
        glSurfaceView = new GLSurfaceView(container.$form());
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // --- Top layer: plain SurfaceView for Filament glTF rendering ---
        filamentSurfaceView = new SurfaceView(container.$form());
        // TRANSLUCENT format tells SurfaceFlinger this surface has an alpha
        // channel — pixels with alpha=0 reveal the GLSurfaceView below.
        filamentSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // Place above the GLSurfaceView in the compositor stack.
        filamentSurfaceView.setZOrderMediaOverlay(true);

        filamentSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Filament surface created");
                // Engine may not be ready yet — initializeSwapChain handles that guard
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(LOG_TAG, "Filament surfaceChanged: " + width + "x" + height);
                if (arFilamentRenderer != null) {
                    arFilamentRenderer.initializeSwapChain(holder.getSurface(), width, height);
                } else {
                    // arFilamentRenderer not created yet — store for when it is
                    pendingFilamentSurface = holder.getSurface();
                    pendingFilamentWidth   = width;
                    pendingFilamentHeight  = height;
                    Log.d(LOG_TAG, "Stored pending surface for deferred SwapChain init");
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Filament surface destroyed");
                if (arFilamentRenderer != null) {
                    arFilamentRenderer.destroySwapChain();
                }
            }
        });

        // --- FrameLayout wraps both — this is what App Inventor sees ---
        frameLayout = new FrameLayout(container.$context());
        FrameLayout.LayoutParams fullscreen = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        frameLayout.addView(glSurfaceView,       fullscreen);
        frameLayout.addView(filamentSurfaceView, fullscreen);

        // Touch events hit filamentSurfaceView first (it's on top).
        // We wire our gesture detectors to it.
        setupGestureDetectors(container.$context());

        // ARViewRender drives the GLSurfaceView render loop (onSurfaceCreated,
        // onSurfaceChanged, onDrawFrame). This is unchanged.
        arViewRender = new ARViewRender(glSurfaceView, this, container.$form());

        installRequested = false;
        depthSettings.onCreate(container.$context());
        instantPlacementSettings.onCreate(container.$context());
        depthSettings.setUseDepthForOcclusion(true);

        container.$add(this);
        container.$form().registerForOnClear(this);
        container.$form().registerForOnResume(this);
        container.$form().registerForOnPause(this);
        container.$form().registerForOnDestroy(this);
    }

    public void Initialize() {
        onResume();
    }

    /**
     * Initialize physics system for the AR view
     */

    /**
     * Get current camera vectors for physics calculations
     */
    public CameraVectors getCurrentCameraVectors() {
        if (lastCamera == null) {
            Log.w("ARView3D", "No AR camera available, using default vectors");
            return null;
        }

        // Get camera's view matrix
        float[] viewMatrix = new float[16];
        lastCamera.getViewMatrix(viewMatrix, 0);

        return new CameraVectors(viewMatrix);
    }

    private void setupGestureDetectors(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return handleTap(e.getX(), e.getY());
            }

            @Override
            public boolean onDown(MotionEvent e) {
                // Check if we're starting a potential drag
                ARNode targetNode = findClosestNode(e.getX(), e.getY());
                Log.i(LOG_TAG, "on Down " + targetNode);
                if (targetNode != null && targetNode.PanToMove()) {
                    currentlyDraggedNode = targetNode;
                    // Start drag immediately
                    if (currentlyDraggedNode instanceof ARNodeBase) {
                        ((ARNodeBase) currentlyDraggedNode).handleAdvancedGestureUpdate(
                            new PointF(e.getX(), e.getY()),
                            new PointF(0, 0),
                            new PointF(0, 0),
                            null,
                            null,
                            "began"
                        );
                    }
                }
                return true; // required
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.i(LOG_TAG, "=== GestureDetector.onScroll() CALLED ===");
                Log.i(LOG_TAG, "currentlyDraggedNode: " + (currentlyDraggedNode != null ? currentlyDraggedNode.NodeType() : "null"));

                if (currentlyDraggedNode != null) {
                    Log.i(LOG_TAG, "Processing scroll for drag movement");
                    PointF fingerLocation = new PointF(e2.getX(), e2.getY());
                    PointF fingerMovement = new PointF(-distanceX, -distanceY);
                    float[] groundProjection = getProjectionForNode(
                        currentlyDraggedNode,
                        fingerLocation,
                        fingerMovement,
                        "changed"
                    );

                    if (currentlyDraggedNode instanceof ARNodeBase) {
                        Log.i(LOG_TAG, "Calling handleAdvancedGestureUpdate with 'changed'");
                        ((ARNodeBase) currentlyDraggedNode).handleAdvancedGestureUpdate(
                            fingerLocation,
                            new PointF(0, 0),
                            new PointF(0, 0),
                            groundProjection,
                            null,
                            "changed"
                        );
                    }
                    return true;
                }
                Log.i(LOG_TAG, "onScroll returning false - no drag node");
                return false;
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return handlePinch(detector);

            }

        });


        // In ScaleGestureDetector.onScaleBegin:


// In RotationGestureDetector.OnRotationGestureListener.onRotation:

        this.filamentSurfaceView.setOnTouchListener((v, event) -> {
            boolean handled = false;
            handled |= scaleGestureDetector.onTouchEvent(event);

            // Extract rotation from two-finger events directly
            if (event.getPointerCount() == 2) {
                float dx = event.getX(1) - event.getX(0);
                float dy = event.getY(1) - event.getY(0);
                float angle = (float) Math.atan2(dy, dx);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        previousTwoFingerAngle = angle;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (previousTwoFingerAngle != 0) {
                            float delta = angle - previousTwoFingerAngle;
                            // Normalize to [-π, π]
                            if (delta > Math.PI) delta -= 2 * Math.PI;
                            if (delta < -Math.PI) delta += 2 * Math.PI;

                            if (activeGesture == ActiveGesture.NONE
                                && Math.abs(delta) > 0.02f) {
                                activeGesture = ActiveGesture.ROTATE;
                            }
                            if (activeGesture == ActiveGesture.ROTATE
                                && currentlyDraggedNode != null
                                && currentlyDraggedNode.RotateWithGesture()) {
                                handleRotation(delta);
                            }
                        }
                        previousTwoFingerAngle = angle;
                        break;
                }
            }

            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
                previousTwoFingerAngle = 0f;
                if (event.getPointerCount() <= 2) {
                    activeGesture = ActiveGesture.NONE;
                }
            }

            if (!scaleGestureDetector.isInProgress()) {
                handled |= gestureDetector.onTouchEvent(event);
            }
            return handled || tapHelper.onTouch(v, event);
        });
    }



    // Shader compilation utility
    private int compileShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        // Check for compilation errors
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(LOG_TAG, "Shader compilation error: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    @Override
    public void onSurfaceCreated(ARViewRender render) {
        try {
            Log.d(LOG_TAG, "onSurfaceCreated called");
            initializeFilamentAndRenderers();
            Log.d(LOG_TAG, "ARView3D onSurfaceCreated successfully");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to create renderers", e);
        }
    }

    @Override
    public void onSurfaceChanged(ARViewRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        if (width <= 0 || height <= 0) return;

        currentViewportWidth  = width;
        currentViewportHeight = height;

        // Filament viewport is updated via filamentSurfaceView's surfaceChanged
        // callback — no Framebuffer to create here.

        Log.d(LOG_TAG, "Surface changed: " + width + "x" + height);
    }

    // Filter ARNodes by type and ensure they have anchors
    public List<ARNode> sort(List<ARNode> nodes, String[] nodeType) {
        List<ARNode> filteredNodes = nodes.stream()
            .filter(n -> {
                // Check if any of the types match
                boolean match = false;
                for (String type : nodeType) {
                    if (n.NodeType().contains(type)) {
                        match = true;
                        break;  // Exit the loop once we find a match
                    }
                }
                return match;  // Return the match result to the filter

            })
            .collect(Collectors.toList());
        System.out.println("Filtered : " + filteredNodes);
        return filteredNodes;
    }


    private void setDefaultPositions(List<ARNode> nodes) {
        for (ARNode node : nodes) {
            if (node == null) continue;
            if (node.Anchor() != null) continue;

            ARNodeBase base = (ARNodeBase) node;
            float[] pos = base.fromPropertyPosition;
            if (pos[0] == 0 && pos[1] == 0 && pos[2] == 0) continue;

            float[] quaternion = base.eulerAnglesToQuaternion(base.fromPropertyRotation);
            Pose pose = new Pose(pos, quaternion);
            try {
                node.Anchor(session.createAnchor(pose));
            } catch (Exception e) {
                Log.w(LOG_TAG, "setDefaultPositions anchor failed: " + e.getMessage());
            }
        }
    }




    public void emitPlaneDetectedEvent() {
        Collection<Plane> planes = session.getAllTrackables(Plane.class);
        Log.d(LOG_TAG, "Number of planes detected: " + planes.size());

        for (Plane plane : planes) {
            ARDetectedPlane arplane = new DetectedPlane(plane);
            PlaneDetected(arplane); //dispatch
        }
    }

    public void drawPlanesAndPoints(ARViewRender render, Camera camera, Frame frame, float[] viewMatrix, float[] projectionMatrix) {
        if (ShowFeaturePoints()) {
            pointCloudRenderer.draw(arViewRender, frame.acquirePointCloud(), viewMatrix, projectionMatrix);
        }

        // Draw planes and feature points
        if (ShowWireframes() && PlaneDetectionType() != 0) {
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

            Log.d(LOG_TAG, " has tracking planes? ");
            planeRenderer.drawPlanes(arViewRender, session.getAllTrackables(Plane.class),
                camera.getDisplayOrientedPose(), projectionMatrix);
        }

        emitPlaneDetectedEvent();

        if (!groundDetected) {
            updateGroundLevelFromPlanes();
        }
    }

    public void drawObjects(ARViewRender render, List<ARNode> objectNodes,
                            float[] viewMatrix, float[] projectionMatrix) {


        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);
        GLES30.glDepthMask(true);
        GLES30.glDisable(GLES30.GL_BLEND);
        objRenderer.draw(render, objectNodes, viewMatrix, projectionMatrix);
    }



    private ByteBuffer convertDepthImageToBuffer(android.media.Image depthImage) {
        // ARCore depth is 16-bit, stored as DEPTH16 format
        android.media.Image.Plane plane = depthImage.getPlanes()[0];
        ByteBuffer depthBuffer = plane.getBuffer().asReadOnlyBuffer();

        // Create a copy since we need to modify it
        ByteBuffer result = ByteBuffer.allocateDirect(depthBuffer.remaining());
        result.put(depthBuffer);
        result.rewind();

        return result;
    }

    private void updateFilamentWithARCoreDepth(Frame frame,
                                               android.media.Image depthImage) {
        try {
            int depthWidth  = depthImage.getWidth();
            int depthHeight = depthImage.getHeight();

            ByteBuffer depthBuffer = convertDepthImageToBuffer(depthImage);

            float[] ndcQuad = { -1f,-1f, 1f,-1f, -1f,1f, 1f,1f };
            float[] textureCoords = new float[8];
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, ndcQuad,
                Coordinates2d.TEXTURE_NORMALIZED, textureCoords);

            float[] depthUvTransform = calculateTransformMatrix(textureCoords);

            if (arFilamentRenderer != null) {
                // Non-blocking — stores for FilamentRenderThread to consume
                arFilamentRenderer.updateARCoreDepth(
                    depthBuffer, depthUvTransform,
                    Z_NEAR, Z_FAR, depthWidth, depthHeight);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "updateFilamentWithARCoreDepth: " + e.getMessage(), e);
        }
    }
    /**
     * Calculate transformation matrix from 4 corner texture coordinates
     */
    private float[] calculateTransformMatrix(float[] textureCoords) {
        // For now, create a simple UV scale/offset matrix from the corner points
        float[] transform = new float[16];

        // Extract UV bounds from the transformed coordinates
        float minU = Math.min(Math.min(textureCoords[0], textureCoords[2]),
            Math.min(textureCoords[4], textureCoords[6]));
        float maxU = Math.max(Math.max(textureCoords[0], textureCoords[2]),
            Math.max(textureCoords[4], textureCoords[6]));
        float minV = Math.min(Math.min(textureCoords[1], textureCoords[3]),
            Math.min(textureCoords[5], textureCoords[7]));
        float maxV = Math.max(Math.max(textureCoords[1], textureCoords[3]),
            Math.max(textureCoords[5], textureCoords[7]));

        // Create scale and offset matrix
        Matrix.setIdentityM(transform, 0);

        // Scale
        float scaleU = maxU - minU;
        float scaleV = maxV - minV;

        // Offset
        float offsetU = minU;
        float offsetV = minV;

        // Apply scale and offset
        transform[0] = scaleU;    // U scale
        transform[5] = scaleV;    // V scale
        transform[12] = offsetU;  // U offset
        transform[13] = offsetV;  // V offset

        return transform;
    }


    public void updatePhysics() {
        float currentTime = System.currentTimeMillis();
        if (lastPhysicsUpdateTime == 0) {
            lastPhysicsUpdateTime = currentTime; // initialize on first frame
            return;
        }
        float deltaTime = (currentTime - lastPhysicsUpdateTime) / 1000.0f;
        deltaTime = Math.min(deltaTime, 1.0f / 30.0f); // cap at 30fps worth — not floor
        lastPhysicsUpdateTime = currentTime;

        for (ARNode node : arNodes) {
            if (((ARNodeBase)node).EnablePhysics()) {
                ((ARNodeBase)node).updateSimplePhysics(deltaTime);
            }
        }
    }

    public Plane getNearestPlane(float posX, float posZ) {
        Collection<Plane> planes = session.getAllTrackables(Plane.class);
        Plane nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (Plane plane : planes) {
            if (plane.getTrackingState() != TrackingState.TRACKING) continue;
            float[] center = plane.getCenterPose().getTranslation();
            float dx = center[0] - posX;
            float dz = center[2] - posZ;
            float dist = dx*dx + dz*dz;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = plane;
            }
        }
        return nearest;
    }

    private void updateGroundLevelFromPlanes() {
        if (groundDetected) return; // only need to do this once

        float lowestY = Float.MAX_VALUE;
        Plane bestPlane = null;

        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() != TrackingState.TRACKING) continue;
            if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) continue;
            // Minimum size filter — ignore tiny noise planes
            if (plane.getExtentX() * plane.getExtentZ() < 0.25f) continue;

            float planeY = plane.getCenterPose().ty();
            if (planeY < lowestY) {
                lowestY = planeY;
                bestPlane = plane;
            }
        }

        if (bestPlane != null) {
            GROUND_LEVEL = lowestY;
            groundDetected = true;
            Log.d(LOG_TAG, "Ground level confirmed at: " + GROUND_LEVEL);

            for (ARNode node : arNodes) {
                ARNodeBase base = (ARNodeBase) node;
                base.setGroundLevel(GROUND_LEVEL);
            }
        }
    }

    private android.media.Image getDepthBits(Frame lastFrame) throws NotYetAvailableException {
        if (depthMode == Config.DepthMode.AUTOMATIC) {
            return lastFrame.acquireDepthImage16Bits();
        } else {
            return lastFrame.acquireRawDepthImage16Bits();
        }
    }

    @Override
    public void onDrawFrame(ARViewRender render) {
        if (session == null) return;

        try {
            // Register camera texture (unchanged)
            if (!hasSetTextureNames) {
                if (backgroundRenderer != null
                    && backgroundRenderer.getCameraColorTexture() != null) {
                    int textureId =
                        backgroundRenderer.getCameraColorTexture().getTextureId();
                    if (textureId != 0) {
                        session.setCameraTextureNames(new int[]{textureId});
                        hasSetTextureNames = true;
                    }
                }
                if (!hasSetTextureNames) return;
            }

            // Update ARCore frame (unchanged)
            try {
                lastFrame  = session.update();
                lastCamera = lastFrame.getCamera();
                lastCamera.getViewMatrix(lastViewMatrix, 0);
                lastCamera.getProjectionMatrix(lastProjMatrix, 0, Z_NEAR, Z_FAR);
            } catch (CameraNotAvailableException e) {
                Log.e(LOG_TAG, "Camera not available: " + e.getMessage());
                return;
            } catch (com.google.ar.core.exceptions.SessionPausedException e) {
                // Session not yet resumed — skip this frame
                return;
            }

            // OpenGL draws — camera feed, planes, sphere/box/capsule
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthMask(false);
            GLES30.glEnable(GLES30.GL_BLEND);

            displayRotationHelper.updateSessionIfNeeded(session);
            if (backgroundRenderer != null) {
                backgroundRenderer.updateDisplayGeometry(lastFrame);
                try {
                    backgroundRenderer.setUseDepthVisualization(render, false);
                    backgroundRenderer.setUseOcclusion(render,
                        depthSettings.useDepthForOcclusion());
                    Log.d("ARView3d", "bgRenderer useDepthForOcclusion: " + depthSettings.useDepthForOcclusion());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "BackgroundRenderer setup: " + e.getMessage());
                    return;
                }
                if (lastFrame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                    // Don't update matrices — keep last known good pose
                    // Don't call updateFrame() — Filament holds its last position
                    backgroundRenderer.drawBackground(render, 0,
                        currentViewportWidth, currentViewportHeight);// still draw camera
                    return;
                }
                if (lastFrame.getTimestamp() != 0) {
                    backgroundRenderer.drawBackground(render, 0,
                        currentViewportWidth, currentViewportHeight);
                }
            }

            if (lastCamera.getTrackingState() == TrackingState.PAUSED) return;

            // Depth (unchanged)
            if (isDepthSupported
                && lastCamera.getTrackingState() == TrackingState.TRACKING
                && (depthSettings.useDepthForOcclusion()
                || depthSettings.depthColorVisualizationEnabled())) {
                Log.d("ARView3d", "last camera tracking, useDepthForOcclusion: " + depthSettings.useDepthForOcclusion());


                try (android.media.Image depthImage =
                         getDepthBits(lastFrame)) {
                    Log.d("ARView3d", "got depthBits");
                    useSimulatedDepth = false;
                    backgroundRenderer.updateCameraDepthTexture(depthImage);
                    if (arFilamentRenderer != null) {
                        updateFilamentWithARCoreDepth(lastFrame, depthImage);
                    }
                    if (objRenderer != null) {
                        float[] ndcQuad = { -1f,-1f, 1f,-1f, -1f,1f, 1f,1f };
                        float[] texCoords = new float[8];
                        lastFrame.transformCoordinates2d(
                            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, ndcQuad,
                            Coordinates2d.TEXTURE_NORMALIZED, texCoords);
                        objRenderer.updateDepthTexture(depthImage,
                            calculateTransformMatrix(texCoords));
                        Log.i(LOG_TAG, "objRenderer updateDepthTexture");


                    }
                } catch (NotYetAvailableException e) {
                    Log.d(LOG_TAG, "Depth not yet available");
                } catch (IllegalStateException e) {
                    useSimulatedDepth = true;
                }
            }

            lastCamera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
            lastCamera.getViewMatrix(viewMatrix, 0);

            setDefaultPositions(arNodes);
            drawPlanesAndPoints(render, lastCamera, lastFrame,
                viewMatrix, projectionMatrix);
            updatePhysics();
            updateCollisions();


            // Let pending InitialPosition nodes and scaled acquire anchors
            for (ARNode node : arNodes) {
                node.tryCreateAnchorIfNeeded(this::getNearestPlane);
            }

            // Sphere/box/capsule via ObjectRenderer (unchanged)
            List<ARNode> objectNodes = sort(arNodes,
                new String[]{"CapsuleNode", "SphereNode", "BoxNode", "WebViewNode"});
            if (objRenderer != null && !objectNodes.isEmpty()) {
                drawObjects(render, objectNodes, viewMatrix, projectionMatrix);
            }

            GLES30.glFinish();

            // ---------------------------------------------------------------
            // Hand off to Filament — NON-BLOCKING.
            // updateFrame() does a lock+copy of matrices and node list.
            // Returns immediately. FilamentRenderThread picks this up on its
            // next vsync via Choreographer. The GL thread is never blocked
            // by anything Filament does.
            // ---------------------------------------------------------------
            if (arFilamentRenderer != null) {
                List<ARNode> modelNodes = sort(arNodes,
                    new String[]{"ModelNode"});

                if (!modelNodes.isEmpty()) {
                    // In ARView3D.onDrawFrame, collect planes on GL thread and pass to Filament:
                    List<Plane> trackingPlanes = new ArrayList<>(
                        session.getAllTrackables(Plane.class));
                    arFilamentRenderer.updateFrame(modelNodes, viewMatrix, projectionMatrix, trackingPlanes);
                }
                for (ARNode n : modelNodes) {
                    float[] pos = n.Anchor() != null ?
                        n.Anchor().getPose().getTranslation() : null;
                    Log.d(LOG_TAG, "ModelNode: anchor=" + (n.Anchor() != null)
                        + " tracking=" + (n.Anchor() != null ?
                        n.Anchor().getTrackingState() : "none")
                        + " pos=" + (pos != null ?
                        pos[0]+","+pos[1]+","+pos[2] : "null")
                        + " inFilament " + arFilamentRenderer.getFilamentNodes());
                }
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "onDrawFrame error: " + e.getMessage(), e);
        }
    }

    private void initializeFilamentAndRenderers() {
        try {
            backgroundRenderer  = new BackgroundRenderer(arViewRender);
            planeRenderer       = new PlaneRenderer(arViewRender);
            pointCloudRenderer  = new PointCloudRenderer(arViewRender);
            objRenderer         = new ObjectRenderer(arViewRender);


            arFilamentRenderer = new ARFilamentRenderer(this.container);
            arFilamentRenderer.initializeEngine(); // Phase 1

            if (!isDepthSupported && enableOcclusion) {
                Log.i(LOG_TAG, "Depth not supported, setting PlaneFinder");
                objRenderer.setPlaneFinder(this::findOccludingPlane);
                arFilamentRenderer.setPlaneFinder(this::findOccludingPlane);
            }
// Surface may have already fired before renderer was created
            if (pendingFilamentSurface != null) {
                Log.d(LOG_TAG, "Applying pending surface to SwapChain");
                arFilamentRenderer.initializeSwapChain(
                    pendingFilamentSurface, pendingFilamentWidth, pendingFilamentHeight);
                pendingFilamentSurface = null;
            }

            Log.d(LOG_TAG, "Renderers initialized");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to initialize renderers: " + e.getMessage(), e);
        }
    }


    // Create a default anchor for placing objects
    public Anchor CreateDefaultAnchor(float[] defaultOrFromProperty) {
        float[] position = defaultOrFromProperty;
        float[] rotation = {0, 0, 0, 1};
        Anchor defaultAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i(LOG_TAG, "default anchor with pose: from" + defaultOrFromProperty + " " + defaultAnchor.getPose());

        return defaultAnchor;
    }

    private boolean handleTap(float screenX, float screenY) {
        if (session == null) return false;

        try {
            Frame frame = lastFrame;
            Camera camera = lastCamera;

            if (camera.getTrackingState() != TrackingState.TRACKING) {
                Log.i(LOG_TAG, "get tracking isn't tracking ");
                return false;
            }

            ARNode hitNode = findClosestNode(screenX, screenY);
            if (hitNode != null) {
                NodeClick(hitNode);
                return true;
            }

            List<HitResult> hitResultList = frame.hitTest(screenX, screenY);
            for (HitResult hit : hitResultList) {

                Trackable mostRecentTrackable = hit.getTrackable();
                Anchor a = hit.createAnchor();


                /*Pose worldPose = hit.getHitPose();
                float[] worldPosition = worldPose.getTranslation();

                // Convert to geospatial coordinates
               Earth earth = session.getEarth();
                GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
                //earth.getPose()
                Log.i("tap is, pose is, trackable is ", tap.toString() + " " + a.getPose() + " " + mostRecentTrackable);
                if (mostRecentTrackable instanceof Plane) {

                */

                Log.i("hit is, pose is, trackable is ", hit.toString() + " " + a.getPose() + " " + mostRecentTrackable);
                if (mostRecentTrackable instanceof Plane) { //most reliably tracked

                    Log.i("detectedplane hit", a.getPose().getTranslation()[0] + " " + a.getPose().getTranslation()[1] + " " + a.getPose().getTranslation()[2]);
                    ARDetectedPlane arplane = new DetectedPlane((Plane) mostRecentTrackable);
                    ClickOnDetectedPlaneAt(arplane, a.getPose(), false, true);
                } else if ((mostRecentTrackable instanceof Point && ((Point) mostRecentTrackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    Log.i("point hit", a.getPose().getTranslation()[0] + " " + a.getPose().getTranslation()[1] + " " + a.getPose().getTranslation()[2]);
                    TapAtPoint(a.getPose().getTranslation()[0], a.getPose().getTranslation()[1], a.getPose().getTranslation()[2], true);
                } else if (mostRecentTrackable instanceof Point && useSimulatedDepth) {
                    Log.i("point hit", a.getPose().getTranslation()[0] + " " + a.getPose().getTranslation()[1] + " " + a.getPose().getTranslation()[2]);
                    TapAtPoint(a.getPose().getTranslation()[0], a.getPose().getTranslation()[1], a.getPose().getTranslation()[2], true);
                } else if ((mostRecentTrackable instanceof InstantPlacementPoint)
                    || (mostRecentTrackable instanceof DepthPoint)) {

                    Log.i("instantplacement","instantplacement");
                    //ARDetectedPlane arplane = new DetectedPlane((Plane) mostRecentTrackable);
                    //ClickOnDetectedPlaneAt(arplane, a.getPose(), true);
                } /*else if ((mostRecentTrackable instanceof Point && ((Point) mostRecentTrackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    //send everything, will sort out if we have it or not
                    // TBD update isGeo
                    //earth or camera, get coord?
                    Anchor geoA = earth.createAnchor(a.getPose());
                    TapAtLocation(geoA.getPose().getTranslation()[0], geoA.getPose().getTranslation()[1], geoA.getPose().getTranslation()[2], 0, 0, 0, false, true);
                } else if ((mostRecentTrackable instanceof InstantPlacementPoint)
                    || (mostRecentTrackable instanceof DepthPoint)) {
                    // are there hooks for this?
                }*/
            }
            return true;
        } catch (Exception e) {
            Log.i(LOG_TAG, "catching exception " + e);
           // throw new RuntimeException(e);
        }
        return false;

    }


    private ARNode findClosestNode(float screenX, float screenY) {
        Log.i(LOG_TAG, "=== findClosestNode START ===");
        Log.i(LOG_TAG, "Touch coordinates: (" + screenX + ", " + screenY + ")");

        if (lastCamera == null || arNodes.isEmpty()) {
            Log.i(LOG_TAG, "Returning null - no camera or nodes");
            return null;
        }

        ARNode closestNode = null;
        float closestDistance = Float.MAX_VALUE;

        // Combine view-projection matrix
        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, lastProjMatrix, 0, lastViewMatrix, 0);
        Log.i(LOG_TAG, "VP matrix calculated");

        for (ARNode node : arNodes) {
            Log.i(LOG_TAG, "--- Checking node: " + node.NodeType() + " ---");


            if (node.Anchor() == null) {
                Log.i(LOG_TAG, "Skipping - no anchor");
                continue;
            }

            // Project node center to screen space
            float[] worldPos = ((ARNodeBase) node).getCurrentPosition();
            Log.i(LOG_TAG, "World position: (" + worldPos[0] + ", " + worldPos[1] + ", " + worldPos[2] + ")");

            float[] screenPos = worldToScreen(worldPos, vpMatrix);

            if (screenPos != null) {
                Log.i(LOG_TAG, "Screen position: (" + screenPos[0] + ", " + screenPos[1] + ")");

                // Calculate screen distance
                float dx = screenX - screenPos[0];
                float dy = screenY - screenPos[1];
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                Log.i(LOG_TAG, "Delta: dx=" + dx + ", dy=" + dy);

                // Check against node's screen radius
                float nodeRadius = getNodeScreenRadius(node, worldPos);
                Log.i(LOG_TAG, "Distance: " + distance + " pixels");
                Log.i(LOG_TAG, "Node radius: " + nodeRadius + " pixels");

                float touchTolerance = 50f;
                float totalRadius = nodeRadius + touchTolerance;
                Log.i(LOG_TAG, "Total radius (including tolerance): " + totalRadius + " pixels");

                boolean withinRadius = distance <= totalRadius;
                boolean closerThanPrevious = distance < closestDistance;
                boolean canMove = node.PanToMove();

                if (withinRadius && closerThanPrevious && canMove) {
                    Log.i(LOG_TAG, "*** FOUND CLOSER DRAGGABLE NODE: " + node.NodeType() + " ***");
                    closestDistance = distance;
                    closestNode = node;
                } else {
                    if (!withinRadius) {
                        Log.i(LOG_TAG, "FAILED: Not within radius - need distance <= " + totalRadius + ", got " + distance);
                    }
                    Log.i(LOG_TAG, "Node rejected - not within criteria");
                }
            } else {
                Log.i(LOG_TAG, "worldToScreen returned null - node behind camera or projection failed");
            }
        }

        Log.i(LOG_TAG, "=== findClosestNode RESULT ===");
        Log.i(LOG_TAG, "RETURNING: " + (closestNode != null ? closestNode.NodeType() : "null"));
        Log.i(LOG_TAG, "Final closest distance: " + (closestNode != null ? closestDistance : "N/A"));
        return closestNode;
    }

    // Enhanced worldToScreen logging
    private float[] worldToScreen(float[] worldPos, float[] vpMatrix) {
        Log.i(LOG_TAG, "worldToScreen input: (" + worldPos[0] + ", " + worldPos[1] + ", " + worldPos[2] + ")");

        float[] clipPos = new float[4];
        float[] worldPos4 = {worldPos[0], worldPos[1], worldPos[2], 1.0f};

        Matrix.multiplyMV(clipPos, 0, vpMatrix, 0, worldPos4, 0);
        Log.i(LOG_TAG, "Clip coordinates: (" + clipPos[0] + ", " + clipPos[1] + ", " + clipPos[2] + ", " + clipPos[3] + ")");

        if (clipPos[3] <= 0.01f) {
            Log.i(LOG_TAG, "worldToScreen returning null - behind camera (w=" + clipPos[3] + ")");
            return null;
        }

        // Perspective divide
        float ndcX = clipPos[0] / clipPos[3];
        float ndcY = clipPos[1] / clipPos[3];
        Log.i(LOG_TAG, "NDC coordinates: (" + ndcX + ", " + ndcY + ")");

        // Convert to screen coordinates
        float screenX = (ndcX + 1.0f) * 0.5f * currentViewportWidth;
        float screenY = (1.0f - ndcY) * 0.5f * currentViewportHeight;
        Log.i(LOG_TAG, "Final screen coordinates: (" + screenX + ", " + screenY + ")");

        return new float[]{screenX, screenY};
    }
    private float getNodeScreenRadius(ARNode node, float[] worldPos) {
        float[] invView = new float[16];
        android.opengl.Matrix.invertM(invView, 0, lastViewMatrix, 0);
        float[] cameraPos = {invView[12], invView[13], invView[14]};
        float distToCamera = ((ARNodeBase) node).vectorDistance(worldPos, cameraPos);

        if (distToCamera < 0.001f) return 100f;

        float visualRadiusMeters;

        if (node instanceof SphereNode) {
            // sphere.obj has local radius 0.5
            visualRadiusMeters = SPHERE_OBJ_RADIUS * node.Scale();

        } else if (node instanceof ModelNode) {
            // use collision radius as best available size approximation
            visualRadiusMeters = ((ModelNode) node).collisionRadius * node.Scale();

        } else {
            // BoxNode and others — use visual bounds bounding sphere
            float[] bounds = node.getVisualBounds();
            float w = bounds[0] * 0.5f;
            float h = bounds[1] * 0.5f;
            float d = bounds[2] * 0.5f;
            visualRadiusMeters = (float) Math.sqrt(w*w + h*h + d*d);
        }

        float fovScale = lastProjMatrix[5];
        float screenRadius = (visualRadiusMeters / distToCamera)
            * fovScale
            * (currentViewportHeight * 0.5f);

        Log.d(LOG_TAG, "getNodeScreenRadius: type=" + node.NodeType()
            + " visualRadius=" + visualRadiusMeters
            + "m dist=" + distToCamera
            + "m screenRadius=" + screenRadius + "px");

        return screenRadius;
    }

    protected float vectorDistance(float[] a, float[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        float dz = a[2] - b[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private float[] getProjectionForNode(ARNode node, PointF fingerLocation,
                                         PointF fingerMovement, String gesturePhase) {
        try {

            // ALWAYS use incremental movement during drag - avoid hit testing chaos
            if ("changed".equals(gesturePhase) && node.Anchor() != null) {
                Log.i(LOG_TAG, "Using stable incremental movement during drag");
                return projectFingerIncrementally(
                    ((ARNodeBase) node).getCurrentPosition(),  // ← reads matrix, updates each frame
                    fingerMovement
                );
            }


            List<HitResult> hitResults = lastFrame.hitTest(fingerLocation.x, fingerLocation.y);
            Log.i(LOG_TAG, "Hit test found " + hitResults.size() + " results");
            for (HitResult hit : hitResults) {
                if (hit.getTrackable() instanceof Plane &&
                    ((Plane) hit.getTrackable()).isPoseInPolygon(hit.getHitPose())) {

                    float[] position = hit.getHitPose().getTranslation();
                    Log.i(LOG_TAG, "Using PLANE hit: (" + position[0] + ", " + position[1] + ", " + position[2] + ")");
                    return adjustPositionForNodeType(node, position);
                }
            }

            // Fallback: Instant placement with closer distance
            List<HitResult> instantHits = lastFrame.hitTestInstantPlacement(
                fingerLocation.x, fingerLocation.y, 0.5f  // Try closer distance
            );

            if (!instantHits.isEmpty()) {
                float[] position = instantHits.get(0).getHitPose().getTranslation();
                Log.i(LOG_TAG, "Using INSTANT placement: (" + position[0] + ", " + position[1] + ", " + position[2] + ")");
                return adjustPositionForNodeType(node, position);
            }



        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in projection", e);
        }

        // Keep current position as fallback
        if (node.Anchor() != null) {
            float[] currentPos =  ((ARNodeBase) node).getCurrentPosition();
            Log.i(LOG_TAG, "Using CURRENT position: (" + currentPos[0] + ", " + currentPos[1] + ", " + currentPos[2] + ")");
            return currentPos;
        }

        return null;
    }

    private float[] projectFingerIncrementally(float[] currentPos, PointF fingerMovement) {
        Log.d(LOG_TAG, "projectFingerIncrementally: fingerMovement=("
            + fingerMovement.x + ", " + fingerMovement.y + ")");

        if (lastCamera == null) {
            Log.w(LOG_TAG, "lastCamera is null");
            return currentPos;
        }

            // Extract camera yaw from pose matrix
            float[] cameraMatrix = new float[16];
            lastCamera.getPose().toMatrix(cameraMatrix, 0);
            float[] cameraForward = {
                -cameraMatrix[8], 0, -cameraMatrix[10]
            };
            float cameraYaw = (float) Math.atan2(cameraForward[0], cameraForward[2]);

            // Scale movement based on distance
            float[] cameraPos = lastCamera.getPose().getTranslation();
            float distance = vectorDistance(currentPos, cameraPos);
            float scale = 0.004f * Math.max(distance * 0.5f, 0.5f);

            Log.d(LOG_TAG, "Camera distance: " + distance + ", scale: " + scale + ", cameraYaw: " + cameraYaw);

            // Apply camera rotation to movement
            float fingerX = -fingerMovement.x * scale;
            float fingerZ = -fingerMovement.y * scale;

            float rotatedX = fingerX * (float) Math.cos(-cameraYaw) - fingerZ * (float) Math.sin(-cameraYaw);
            float rotatedZ = fingerX * (float) Math.sin(-cameraYaw) + fingerZ * (float) Math.cos(-cameraYaw);

            Log.d(LOG_TAG, "Calculated movement: (" + rotatedX + ", 0, " + rotatedZ + ")");

            float newX = currentPos[0] + rotatedX;
            float newZ = currentPos[2] + rotatedZ;

            return new float[]{newX, currentPos[1], newZ};
    }

    private float[] adjustPositionForNodeType(ARNode node, float[] position) {
        if (node instanceof SphereNode) {
            return new float[]{position[0], Math.max(position[1], 0.0f), position[2]};
        } /*else {
            float constrainedY = Math.max(position[1], GROUND_LEVEL + 0.005f);
            return new float[]{position[0], constrainedY, position[2]};
        }*/
        return new float[]{position[0],position[1], position[2]};
    }

    private float getCollisionRadius(ARNodeBase node) {
        return node.getCollisionRadius(); // scale already applied

    }private int collisionLogThrottle = 0;

    private void updateCollisions() {
        int totalChecks = 0;
        int actualCollisions = 0;
        int skipped = 0;

        for (int i = 0; i < arNodes.size(); i++) {
            for (int j = i + 1; j < arNodes.size(); j++) {
                ARNodeBase a = (ARNodeBase) arNodes.get(i);
                ARNodeBase b = (ARNodeBase) arNodes.get(j);

                // log why pairs are skipped
                if (!a.EnablePhysics() && !b.EnablePhysics()) {
                    skipped++;
                    continue;
                }

                totalChecks++;
                float dist = a.collisionDistance(b);
                float minDist = a.getCollisionVolume().getEffectiveRadius()
                    + b.getCollisionVolume().getEffectiveRadius();

                if (dist < minDist) {
                    actualCollisions++;
                    // log every actual collision
                    Log.d(LOG_TAG, "COLLISION: " + a.NodeType() + " vs " + b.NodeType()
                        + " dist=" + dist + " minDist=" + minDist
                        + " aPhysics=" + a.EnablePhysics()
                        + " bPhysics=" + b.EnablePhysics()
                        + " aDragged=" + a.isBeingDragged
                        + " bDragged=" + b.isBeingDragged
                        + " aVel=(" + a.currentVelocity[0] + "," + a.currentVelocity[2] + ")"
                        + " bVel=(" + b.currentVelocity[0] + "," + b.currentVelocity[2] + ")");
                }

                a.separateFrom(b);
            }
        }

        // log summary every 60 frames — not every frame
        collisionLogThrottle++;
        if (collisionLogThrottle >= 60) {
            collisionLogThrottle = 0;
            Log.i(LOG_TAG, "Collision summary: nodes=" + arNodes.size()
                + " totalChecks=" + totalChecks
                + " actualCollisions=" + actualCollisions
                + " skippedNoPhysics=" + skipped
                + " checksPerFrame=" + totalChecks);
        }
    }

    private void handleCollision(ARNode nodeA, ARNode nodeB) {
        // Separate objects first
        ((ARNodeBase) nodeA).separateFrom((ARNodeBase) nodeB);

        // Delayed notification to avoid physics interference
         nodeA.ObjectCollidedWithObject(nodeB);
         nodeB.ObjectCollidedWithObject(nodeA);
    }

    private boolean checkCollision(ARNode nodeA, ARNode nodeB) {
        ARNodeBase a = (ARNodeBase) nodeA;
        ARNodeBase b = (ARNodeBase) nodeB;
        return a.getCollisionVolume().intersects(
            b.getCollisionVolume(),
            a.getCurrentPosition(),
            b.getCurrentPosition());
    }

    // Configure ARCore session
    private void configureSession() {

        Config config = session.getConfig();
        //config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        config.setLightEstimationMode(Config.LightEstimationMode.AMBIENT_INTENSITY);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        // This is the critical part for plane detection
        if (PlaneDetectionType() == 1) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
        } else if (PlaneDetectionType() == 2) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.VERTICAL);
        } else if (PlaneDetectionType() == 3) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
        } else {
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        }

        boolean isGeospatialSupported =
            session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED);
        Log.i(LOG_TAG, "ARCore: geospatial supported ? " + isGeospatialSupported);


        config.setDepthMode(Config.DepthMode.DISABLED);
        session.configure(config);

        configureDepth(config);

    }
    private void configureDepth(Config config) {

        Log.i(LOG_TAG, "configureDepth: lightMode=" + config.getLightEstimationMode()
            + " planeFinding=" + config.getPlaneFindingMode()
            + " updateMode=" + config.getUpdateMode());

        if (EnableOcclusion()) { //TODO: CSB, I'm not sure this is the correct place to evaluate. Depth is more than occlusion
            for (Config.DepthMode mode : new Config.DepthMode[]{
                Config.DepthMode.AUTOMATIC, Config.DepthMode.RAW_DEPTH_ONLY}) {
                config.setDepthMode(mode);
                try {
                    session.configure(config);
                    isDepthSupported = true;
                    depthMode = mode;
                    Log.i(LOG_TAG, "Depth enabled: " + mode);
                    return;
                } catch (com.google.ar.core.exceptions.UnsupportedConfigurationException e) {
                    Log.i(LOG_TAG, mode + " not supported, trying next");
                }
            }
            //TODO: CSB we should inform the user depth is not supported if they have turned on Occlusion
        }
        config.setDepthMode(Config.DepthMode.DISABLED);
        session.configure(config);
        isDepthSupported = false;
        depthMode = Config.DepthMode.DISABLED;
        Log.i(LOG_TAG, "Depth not supported");
    }

    private Plane findOccludingPlane(float[] nodeWorldPos, float[] cameraWorldPos, float sphereRadius) {
        Collection<Plane> planes = session.getAllTrackables(Plane.class);

        // Ray from camera through sphere
        float[] rayDir = {
            nodeWorldPos[0] - cameraWorldPos[0],
            nodeWorldPos[1] - cameraWorldPos[1],
            nodeWorldPos[2] - cameraWorldPos[2]
        };
        float distanceToSphere = (float) Math.sqrt(
            rayDir[0]*rayDir[0] + rayDir[1]*rayDir[1] + rayDir[2]*rayDir[2]);
        // Normalize ray direction
        rayDir[0] /= distanceToSphere;
        rayDir[1] /= distanceToSphere;
        rayDir[2] /= distanceToSphere;

        for (Plane plane : planes) {
            if (plane.getTrackingState() != TrackingState.TRACKING) continue;

            if (plane.getExtentX() < 0.3f || plane.getExtentZ() < 0.3f) continue;

            // Get plane normal from pose matrix column 1 (local Y axis)
            float[] poseMatrix = new float[16];
            plane.getCenterPose().toMatrix(poseMatrix, 0);
            float nx = poseMatrix[4], ny = poseMatrix[5], nz = poseMatrix[6];

            // Plane equation: dot(normal, point) + d = 0
            float[] center = plane.getCenterPose().getTranslation();
            float d = -(nx * center[0] + ny * center[1] + nz * center[2]);

            // Ray-plane intersection: t = -(dot(normal, rayOrigin) + d) / dot(normal, rayDir)
            float denom = nx * rayDir[0] + ny * rayDir[1] + nz * rayDir[2];

            // Ray parallel to plane — no intersection
            if (Math.abs(denom) < 1e-6f) continue;

            float t = -(nx * cameraWorldPos[0] + ny * cameraWorldPos[1]
                + nz * cameraWorldPos[2] + d) / denom;

            // Intersection must be between camera and sphere
            if (t < 0.05f || t > distanceToSphere - 0.05f) continue;
            if (t < 0.05f || t > distanceToSphere - sphereRadius) continue;
            // Intersection point in world space
            float[] hitPoint = {
                cameraWorldPos[0] + t * rayDir[0],
                cameraWorldPos[1] + t * rayDir[1],
                cameraWorldPos[2] + t * rayDir[2]
            };

            // Transform hit point into plane local space to check extent
            float[] invPlaneMatrix = new float[16];
            android.opengl.Matrix.invertM(invPlaneMatrix, 0, poseMatrix, 0);
            float[] localHit = new float[4];
            android.opengl.Matrix.multiplyMV(localHit, 0, invPlaneMatrix, 0,
                new float[]{hitPoint[0], hitPoint[1], hitPoint[2], 1f}, 0);

            float halfX = plane.getExtentX() / 2f;
            float halfZ = plane.getExtentZ() / 2f;
            if (Math.abs(localHit[0]) > halfX || Math.abs(localHit[2]) > halfZ) continue;

            Log.i(LOG_TAG, "Found occluding plane! type=" + plane.getType()
                + " t=" + t + " distToSphere=" + distanceToSphere
                + " hitLocalX=" + localHit[0] + " hitLocalZ=" + localHit[2]);
            return plane;
        }
        return null;
    }

    // Update lighting estimation from ARCore
   /* private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
        if (virtualObjectShader == null) {
            return;
        }

        if (lightEstimate.getState() != LightEstimate.State.VALID) {
            virtualObjectShader.setBool("u_LightEstimateIsValid", false);
            return;
        }

        virtualObjectShader.setBool("u_LightEstimateIsValid", true);
        Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
        virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);

        // Update main light
        float[] direction = lightEstimate.getEnvironmentalHdrMainLightDirection();
        float[] intensity = lightEstimate.getEnvironmentalHdrMainLightIntensity();

        worldLightDirection[0] = direction[0];
        worldLightDirection[1] = direction[1];
        worldLightDirection[2] = direction[2];
        Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
        virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
        virtualObjectShader.setVec3("u_LightIntensity", intensity);

        // Update spherical harmonics
        updateSphericalHarmonicsCoefficients(lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());

        // Update cube map
        cubeMapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
    }*/



    // LIFECYCLE METHODS

    // Resume AR session
    @Override
    public void onResume() {
        if (session == null) {

            try {
                // Request ARCore installation if needed
                switch (ArCoreApk.getInstance().requestInstall($form(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // Check camera permission
                if ($form().isDeniedPermission(CAMERA)) {
                    $form().askPermission(CAMERA, new PermissionResultHandler() {
                        @Override
                        public void HandlePermissionResponse(String permission, boolean granted) {
                            if (!granted) {
                                // Handle permission denied
                                onResume();
                            }
                        }
                    });
                    return;
                }

                // Create and configure ARCore session
                session = new Session($context());
                configureSession();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to create AR session", e);
                return;
            }
        }
        try {
            session.resume();
            Log.d(LOG_TAG, "resume called");
            // Initialize renderers with ARCore session
            //initializeFilamentAndRenderers();
            depthSettings.setUseDepthForOcclusion(EnableOcclusion());
            // Resume display rotation helper
            displayRotationHelper.onResume();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to create AR session", e);
        }
    }


    @Override
    public View getView() {
        return frameLayout; // FrameLayout containing both surfaces
    }

    @Override
    public List<Component> getChildren() {
        return new ArrayList<>();
    }

    //@Override
    public Activity $context() {
        return container.$context();
    }

    //@Override
    public Form $form() {
        return container.$form();
    }

    //@Override
    public void $add(AndroidViewComponent component) {
    }

    //@Override
    public void setChildWidth(AndroidViewComponent component, int width) {
    }

    //@Override
    public void setChildHeight(AndroidViewComponent component, int height) {
    }


    @Override
    public void onPause() {

        if (session == null) {
            return;
        }

        displayRotationHelper.onPause();
        session.pause();
    }


    @Override
    public void onClear() {
        onPause();

        for (ARNode node : arNodes) {
            ((ARNodeBase)node).Anchor(null);
            node = null;
            node = null;
        }

        if (session != null) {
            session.close();
            session = null;
        }

    }

    @Override
    public void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }
        if (arFilamentRenderer != null) {
            arFilamentRenderer.destroy();
            arFilamentRenderer = null;
        }
    }

    @SimpleFunction(description = "Delete")
    public void onDelete() {
    }

    @SimpleFunction(description = "Load scene from storage")
    public List<ARNode> LoadScene(List<YailDictionary> dictionaries) {
        Log.i(LOG_TAG, "loading stored scene " + dictionaries);
        ARNode addNode = null;
        List<ARNode> newNodes = new ArrayList<>();
        for (Object obj : dictionaries) {
            Log.i(LOG_TAG, "loadscene obj is " + obj);
            if (obj == null || obj instanceof gnu.mapping.Symbol) {
                Log.i(LOG_TAG, "loadscene null or list " + obj);
                continue;
            }

            YailDictionary nodeDict = (YailDictionary) obj;
            String type = (String) nodeDict.get("type");
            Log.i(LOG_TAG, "loadscene TYPE is " + type);
            switch (type.toLowerCase()) {
                case "capsule":
                    addNode = this.CreateCapsuleNodeFromYail(nodeDict);
                    break;
                case "box":
                    addNode = this.CreateBoxNodeFromYail(nodeDict);
                    break;
                case "sphere":
                    addNode = this.CreateSphereNodeFromYail(nodeDict);
                    break;
                case "text":
                    addNode = this.CreateTextNodeFromYail(nodeDict);
                    break;
                case "video":
                    addNode = this.CreateVideoNodeFromYail(nodeDict);
                    break;
                case "webview":
                    addNode = this.CreateWebViewNodeFromYail(nodeDict);
                    break;
                default:
                    // currently not storing or handling modelNode..
                    break;
            }

            if (addNode != null) {
                addNode(addNode);
                newNodes.add(addNode);
                Log.i(LOG_TAG, "loaded " + addNode);
            }

        }
        Log.i(LOG_TAG, "loadscene new nodes are " + newNodes);
        return newNodes;

    }


    @SimpleFunction(description = "Get scene from storage")
    public List<YailDictionary> SaveScene(List<ARNode> newNodes) {

        List<YailDictionary> dictionaries = new ArrayList<>();
        for (Object node : newNodes) {
            if (node == null || node instanceof gnu.mapping.Symbol) {
                Log.i(LOG_TAG, "savescene null or list " + node);
                continue;
            }
            Log.i(LOG_TAG, "savescene node" + node);
            ARNode arnode = (ARNode) node;
            String type = arnode.NodeType();
            dictionaries.add(arnode.ARNodeToYail());
            Log.i(LOG_TAG, "saving node " + type);
        }
        return dictionaries;

    }

    @SimpleFunction(description = "Sets Visible to false for all Lights.")
    public void HideAllLights() {
    }


    //@Override
    @SimpleEvent(description = "The user tapped on a node in the ARView3D.")
    public void addNode(ARNode node) {
        Log.i("ADDING ARNODE", "");
        arNodes.add(node);
        ((ARNodeBase) node).Session(session);
        ((ARNodeBase) node).setPlaneFinder(this::getNearestPlane);
        ((ARNodeBase) node).setGroundLevel(GROUND_LEVEL);
        Log.i("ADDED ARNODE", node.NodeType() + " and session is null? " + (session == null));
    }

    @SimpleFunction(description = "Remove a node ")
    @SimpleEvent(description = "Remove a node ")
    public void removeNode(ARNode node) {
        Log.i("Removing ARNODE", "");
        arNodes.remove(node);
    }

    public boolean handlePinch(ScaleGestureDetector detector) {
        // Get the focus point of the pinch gesture
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        ARNode nodeEntity = findClosestNode(focusX, focusY);
        if (nodeEntity != null && nodeEntity.PinchToScale()) {
            Log.d(LOG_TAG, "Found closest node for scaling: " + nodeEntity.NodeType());

            float scaleFactor = detector.getScaleFactor();
            // Clamp scale factor to prevent extreme values
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));

            nodeEntity.ScaleBy(scaleFactor);
            return true; // We handled the pinch
        } else {
            Log.d(LOG_TAG, "No scalable nodes close to pinch location");
            return false; // We didn't handle it
        }
    }

    private void handleRotation(float rotationDelta) {
        if (currentlyDraggedNode == null) return;
        if (!(currentlyDraggedNode instanceof ARNodeBase)) return;
        ARNodeBase base = (ARNodeBase) currentlyDraggedNode;
        if (!base.RotateWithGesture()) return;

        float[] currentQ = base.getCurrentRotation();

        // Delta quaternion for Y-axis rotation only
        float halfAngle = rotationDelta * 0.5f;
        float[] deltaQ = {
            0f,
            (float) Math.sin(halfAngle),
            0f,
            (float) Math.cos(halfAngle)
        };

        // Apply delta to current rotation
        base.setCurrentRotation(multiplyQuaternions(deltaQ, currentQ));
    }

    // Quaternion multiply — in ARView3D or a shared math utility
    private float[] multiplyQuaternions(float[] a, float[] b) {
        return new float[]{
            a[3]*b[0] + a[0]*b[3] + a[1]*b[2] - a[2]*b[1],
            a[3]*b[1] - a[0]*b[2] + a[1]*b[3] + a[2]*b[0],
            a[3]*b[2] + a[0]*b[1] - a[1]*b[0] + a[2]*b[3],
            a[3]*b[3] - a[0]*b[0] - a[1]*b[1] - a[2]*b[2]
        };
    }

    // @Override
    @SimpleEvent(description = "The user tapped on a node in the ARView3D.")
    public void NodeClick(ARNode node) {
        EventDispatcher.dispatchEvent(this, "Click");
    }

    // @Override
    @SimpleEvent(description = "The user long-pressed a node in the ARView3D.")
    public void NodeLongClick(ARNode node) {
    }



    @SimpleEvent(description = " Collision detected")
    public void CollisionDetected() {
        EventDispatcher.dispatchEvent(this, "CollisionDetected");

    }

    @SimpleEvent(description = " Object collided with Scene detected")
    public void ObjectCollidedWithScene(ARNode node){
        EventDispatcher.dispatchEvent(this, "ObjectCollidedWithScene", node);

    }

    @SimpleEvent(description = " Object collided with Object detected")
    public void ObjectCollidedWithObject(ARNode node, ARNode node2) {
        EventDispatcher.dispatchEvent( this, "ObjectCollidedWithObject", node, node2);

    }

    @SimpleFunction(description = "Get where camera is currently looking")
    public YailDictionary getCameraLook() {
        float[] cameraMatrix = new float[16];
        lastCamera.getPose().toMatrix(cameraMatrix, 0);

        float[] cameraPosition = {
            cameraMatrix[12], cameraMatrix[13], cameraMatrix[14]
        };

        float[] cameraForward = {
            -cameraMatrix[8], -cameraMatrix[9], -cameraMatrix[10]
        };

        /* todo csb List<HitResult> hits = lastFrame.hitTest(
            lastCamera.getViewMatrix(),
            cameraForward[0], cameraForward[1], cameraForward[2]
        );

        if (!hits.isEmpty()) {
            HitResult hit = hits.get(0);
            Pose hitPose = hit.getHitPose();
            float[] hitPosition = hitPose.getTranslation();

            // Now hitPosition is equivalent to Swift's worldPosition
            Log.d("Camera", "Looking at: " + Arrays.toString(hitPosition));
            EventDispatcher.dispatchEvent(this, "CameraIsLookingAt", hitPosition);
            return;
        }*/
        YailDictionary yDict = new YailDictionary();
        yDict.put("x", cameraForward[0]);
        yDict.put("y", cameraForward[1]);
        yDict.put("z", cameraForward[2]);
        return yDict;
    }

    /*@SimpleEvent(description = "Position camera is currently looking")
    public void gotCameraLook(float x, float y, float z) {

    }*/

    //@Override
    @SimpleEvent(description = "The user tapped on a point on the ARView3D.  (x,y,z) is " +
        "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
        "at that point and false otherwise.")
    public void TapAtPoint(float x, float y, float z, boolean isANodeAtPoint) {
        Log.i("TAPPED at ARVIEW3D point", "");
        container.$form().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(ARView3D.this, "TapAtPoint", x, y, z, isANodeAtPoint);
            }
        });
    }

    @SimpleEvent(description = "The user tapped on a point on the ARView3D.  (x,y,z) is \" +\n" +
        "        \"the real-world coordinate of the point. Use this event if you want geoCoordinates to also be determined if available. " +
        "       \" isANoteAtPoint is true if a node is already at that point and false otherwise.")
    public void TapAtLocation(float x, float y, float z,
                              double lat, double lng, double alt,
                              boolean hasGeoCoordinates, boolean isANodeAtPoint) {

        EventDispatcher.dispatchEvent(this, "TapAtLocation", x, y, z, lat, lng, alt, hasGeoCoordinates, isANodeAtPoint);
    }

    //@Override
    @SimpleEvent(description = "The user long-pressed on a point on the ARView3D.  (x,y,z) is " +
        "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
        "at that point and false otherwise.")
    public void LongPressAtPoint(float x, float y, float z, boolean isANodeAtPoint) {
    }


    @Override
    @SimpleEvent(description = "A real-world plane was detected.  The detectedPlane is the " +
        "component added at the location of the real-world plane.  This event will only trigger if " +
        "PlaneDetection is not None, and the TrackingType is WorldTracking.  Note that the default " +
        "FillColor of a DetectedPlane is None, so it is shown visually by default.")
    public void PlaneDetected(ARDetectedPlane detectedPlane) {
        container.$form().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(ARView3D.this, "PlaneDetected", detectedPlane);
                Log.i("dispatching detected Plane", " plane is " + detectedPlane);
            }
        });

    }

    @Override
    @SimpleEvent(description = "A DetectedPlane updated its properties, either its rotation, " +
        "position, or size.  This event will only trigger if PlaneDetection is not None, and the " +
        "TrackingType is WorldTracking.")
    public void DetectedPlaneUpdated(ARDetectedPlane detectedPlane) {
        EventDispatcher.dispatchEvent(this, "DetectedPlaneUpdated", detectedPlane);
        Log.i("dispatching updated Plane", "");
    }

    @Override
    @SimpleEvent(description = "The user long-pressed on a DetectedPlane, detectedPlane.  (x,y,z) is " +
        "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
        "at that point and false otherwise.  This event will only trigger if PlaneDetection is not " +
        "None, and the TrackingType is WorldTracking.")
    public void LongClickOnDetectedPlaneAt(ARDetectedPlane detectedPlane, float x, float y, float z, boolean isANodeAtPoint) {
    }

    @Override
    @SimpleEvent(description = "The user tapped on a DetectedPlane, detectedPlane.  (x,y,z) is " +
        "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
        "at that point and false otherwise.  This event will only trigger if PlaneDetection is not " +
        "None, and the TrackingType is WorldTracking.")
    public void ClickOnDetectedPlaneAt(ARDetectedPlane targetPlane, Object p, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        container.$form().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(ARView3D.this, "ClickOnDetectedPlaneAt", targetPlane, p, hasGeoCoordinates, isANodeAtPoint);
                Log.i("dispatching Click On Detected Plane", "");
            }
        });

    }

    @Override
    @SimpleEvent(description = "A DetectedPlane was removed from the ARView3D.  This happens " +
        "when two DetectedPlanes are combined to form one or the detected items were reset.  " +
        "This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.")
    public void DetectedPlaneRemoved(ARDetectedPlane detectedPlane) {
    }

    @Override
    @SimpleEvent(description = "The lighting estimate has been updated.  This provides an " +
        "estimate for the real-world ambient lighting.  This event will only trigger if " +
        "LightingEstimation is true.")
    public void LightingEstimateUpdated(float ambientIntensity, float ambientTemperature) {
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
    public void EnableOcclusion(boolean occlusion) {
        enableOcclusion = occlusion;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "get invisible floor at y in centimeters, if present")
    public float locationOfInvisibleFloorInCentimeters() {
        return invisibleFloor;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether enable occlusion in world understanding. If true, objects will disappear behind walls, furniture and sometime floor")
    public boolean EnableOcclusion() {
        return enableOcclusion;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowAnchorGeometry(boolean showGeometry) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether geometry shows in world understanding. ")
    public boolean ShowAnchorGeometry() {
        return false;
    }
    
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowPhysics(boolean showP) {
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether physics show in world understanding. ")
    public boolean ShowPhysics() {
        return false;
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

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to visualize features points as yellow points.  Feature points are " + "points for which their world coordinates are known.")
    public boolean ShowFeaturePoints() {
        return true;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowWireframes(boolean showWireframes) {
        enableWireframes = showWireframes;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to show the wireframe of nodes' geometries on top of their FillColor of Texture.")
    public boolean ShowWireframes() {
        return enableWireframes;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty
    public void ShowBoundingBoxes(boolean showBoundingBoxes) {
        enableBoundingBoxes = showBoundingBoxes;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether to visualize the bounding box surrounding a node.")
    public boolean ShowBoundingBoxes() {
        return enableBoundingBoxes;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_PLANE_DETECTION_TYPE, defaultValue = "1")
    public void PlaneDetectionType(int planeType) {
        detectedPlaneType = planeType;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "<p>Determines whether plane detection is enabled.  If this property is set to None, " + "then planes in the real world will not be detected.  Setting it to Horizontal detects horizontal " + "planes in the real world.  Setting it to Vertical detects vertical planes in the real world, and " + "setting it to both detects both horizontal and vertical planes.  When a plane is detected, a " + "DetectedPlane component will placed at that location.  This works when the TrackingType is WorldTracking.</p>" + "<p>Valid values are: 0 (None), 1 (Horizontal), 2 (Vertical), 3 (Both).</p>")
    public int PlaneDetectionType() {
        return detectedPlaneType;
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

    @Override
    public void setChildNeedsLayout(AndroidViewComponent component) {
        throw new UnsupportedOperationException("Canvas.setChildNeedsLayout() called");
    }

    @SimpleFunction(description = "Starts the live camera feed and begins tracking.")
    public void StartTracking() {
    }

    @SimpleFunction(description = "Pauses the live camera feed and pauses tracking.")
    public void PauseTracking() {
    }

    @SimpleFunction(description = "Resets the tracking, resetting all items including DetectedPlanes and ImageMarkers. " +
        "If this is called while tracking is not paused, then this resets and restarts tracking.  If tracking is paused " +
        "and this is called, this will reset the ARView3D once StartTracking is called again.")
    public void ResetTracking() {
        onClear();
    }

    @SimpleFunction(description = "Removed DetectedPlanes and resets detection for ImageMarkers.")
    public void ResetDetectedItems() {
    }

    @SimpleFunction(description = "Sets Visible to false for all Nodes.")
    public void HideAllNodes() {
    }

    @SimpleFunction(description = "Create a new BoxNode with default properties at the " +
        "specified (x,y,z) position.")
    public BoxNode CreateBoxNode(float x, float y, float z) {

        Log.i("creating box node", "with x, y, z " + x + " " + y + " " + z);
        BoxNode boxNode = new BoxNode(this);

        float[] position = {x, y, z};
        float[] rotation = {0, 0, 0, 1};
        Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i("creating Capsule node, anchor is", myAnchor.toString());
        boxNode.Anchor(myAnchor);
        return boxNode;
    }

    @SimpleFunction(description = "Create a new BoxNode with default properties at the plane position.")
    public BoxNode CreateBoxNodeAtPlane(ARDetectedPlane targetPlane, Object p) {
        Log.i("creating Capsule node", "with detected plane and pose");
        Pose pose = (Pose) p;
        BoxNode bNode = new BoxNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        bNode.Anchor(trackable.createAnchor(pose));
        bNode.Trackable(trackable);

        Log.i("creating Box node, anchor is", bNode.Anchor().toString());
        return bNode;
    }

    @SimpleFunction(description = "Create a new boxNode with default properties with a pose.")
    public BoxNode CreateBoxNodeFromYail(YailDictionary yailNodeObj) {

        if (session != null) {
            try {
                Log.i(LOG_TAG, " creating block node from " + yailNodeObj);

                BoxNode boxNode = new BoxNode(this);
                ARUtils.parseYailToNode(boxNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created boxNode node from json, anchor is" + boxNode.Anchor().toString());
                return boxNode;
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create boxNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode ", " since there is no session");
        return null;
    }
    @SimpleFunction(description = "Create a new CapsuleNode with geo coords")
    public BoxNode CreateBoxNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Sphere node", "with geo location");

        BoxNode node = new BoxNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        node.Anchor(geoAnchor);

        Log.i("created Box node, geo anchor is", node.Anchor().toString());
        return node;
    }

    @SimpleFunction(description = "Create an ImageMarker with an url string")
    public ImageMarker CreateImageMarker(String image) {
        ImageMarker imageMarker = new ImageMarker(this);
        imageMarker.imageUrl = image;
        return imageMarker;
    }


    @SimpleFunction(description = "take picture and invoke listener for a successful capture")
    public void TakePicture(String name, float width) {
        //capture screenshot
            //AfterPicture(image, name: name)
    }

    @SimpleEvent(description = "handle after picture")
    public String AfterPicture(String url, String name, float width) {
        //let url = try self.savePNG(imageUI, name: "Marker")
        //          self._currentImageSnapshot = image
        EventDispatcher.dispatchEvent(this, "AfterPicture", url, name, width);
        return url;
    }

    @SimpleFunction(description = "returnImageMarker after snapshot url is made")
    public ImageMarker ImageForMarkerCreated(String url, String name, float width){
        ImageMarker iM = new ImageMarker(this); //url
        iM.imageUrl = url;
        iM.name = name;
        iM.PhysicalWidthInCentimeters(width);
        return iM;
    }

    @SimpleFunction(description = "Create an ImageMarker with from a snapshot with width and url string")
    public void CreateImageMarkerFromSnapshot(String name, float width) {
        // TODO CSB how to do snapshot from frame in ARCore?
        Log.i("Create ImageMarker from snapshot", "");
        String url = "dummyimagestring";
        TakePicture(name, width);
    }

    @SimpleFunction(description = "Create a new ImageMarker from yail")
    public ImageMarker CreateImageMarkerFromYail(YailDictionary yailObj) {

        if (session != null) {
          Log.i(LOG_TAG, " creating block node from " + yailObj);

          YailDictionary keyvalue = (YailDictionary)yailObj;

                /*String  type = (String) keyvalue.get("type");
                      String name = (String) keyvalue.get("name");
                      String imgPath = (String) keyvalue.get("image");
                      Float wCM = d["physicalWidthCM"]
                       Float wCM = keyvalue.get("physicalWidthCM");
                      Bool vis =  keyvalue.get("visible");

                 */
          ImageMarker imageMarker = new ImageMarker(this);

          Log.i(LOG_TAG, "SUCCESS created image marker from json, anchor is" + imageMarker.toString());
          return imageMarker;

        }
        Log.i("cannot create imageMarker from yail ", " since there is no session");
        return null;
    }

    @SimpleFunction(description = "Create a new PlaneNode with geo coords")
    public PlaneNode CreatePlaneNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Plane node", "with geo location");

        PlaneNode node = new PlaneNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        node.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", node.Anchor().toString());
        return node;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with geo coords, if available")
    public SphereNode CreateSphereNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Sphere node", "with geo location");

        SphereNode node = new SphereNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        node.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", node.Anchor().toString());
        return node;
    }


    @SimpleFunction(description = "Create a new SphereNode with default properties at the detected plane position.")
    public SphereNode CreateSphereNodeAtPlane(ARDetectedPlane targetPlane, Object p, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Sphere node", "with detected plane and pose");
        Pose pose = (Pose) p;
        SphereNode sphereNode = new SphereNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        sphereNode.Anchor(trackable.createAnchor(pose));
        sphereNode.Trackable(trackable);
        Log.i("creating sphere node, anchor is", sphereNode.Anchor().toString());
        return sphereNode;
    }

    @SimpleFunction(description = "Create a new boxNode with default properties with a pose.")
    public SphereNode CreateSphereNodeFromYail(YailDictionary yailNodeObj) {
        if (session != null) {
            try {
                Log.i(LOG_TAG, " triggered via block, sphere node is " + yailNodeObj);

                SphereNode sphereNode = new SphereNode(this);
                ARUtils.parseYailToNode(sphereNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created SphereNode node from json, anchor is" + sphereNode.Anchor().toString());
                return sphereNode;
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create SphereNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode ", " since there is no session");
        return null;
    }

    @SimpleFunction(description = "Create a new SphereNode with default properties at the specified (x,y,z) position.")
    public SphereNode CreateSphereNode(float x, float y, float z) {
        Log.i("creating Sphere node", "with x, y, z " + x + " " + y + " " + z);
        SphereNode sphereNode = new SphereNode(this);
        sphereNode.XPosition(x);
        sphereNode.YPosition(y);
        sphereNode.ZPosition(z);

        float[] position = {x, y, z};
        float[] rotation = {0, 0, 0, 1};
        Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i("creating sphere node, anchor is", myAnchor.toString());
        sphereNode.Anchor(myAnchor);

        return sphereNode;
    }

    @SimpleFunction(description = "Create a new PlaneNode with default properties at the specified (x,y,z) position.")
    public PlaneNode CreatePlaneNode(float x, float y, float z) {
        return new PlaneNode(this);
    }

    @SimpleFunction(description = "Create a new PlaneNode with default properties at the detected plane position.")
    public PlaneNode CreatePlaneAtPlane(ARDetectedPlane targetPlane, Object p) {
        Log.i("creating plane node", "with detected plane and pose");
        Pose pose = (Pose) p;
        PlaneNode vNode = new PlaneNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        vNode.Anchor(trackable.createAnchor(pose));
        vNode.Trackable(trackable);
        Log.i("creating video node, anchor is", vNode.Anchor().toString());
        return vNode;
    }


    @SimpleFunction(description = "Create a new CylinderNode with default properties at the specified (x,y,z) position.")
    public CylinderNode CreateCylinderNode(float x, float y, float z) {
        return new CylinderNode(this);
    }

    @SimpleFunction(description = "Create a new ConeNode with default properties at the specified (x,y,z) position.")
    public ConeNode CreateConeNode(float x, float y, float z) {
        return new ConeNode(this);
    }

    @SimpleFunction(description = "Create a new CapsuleNode with default properties at the plane position.")
    public CapsuleNode CreateCapsuleNodeAtPlane(ARDetectedPlane targetPlane, Object p) {
        Log.i("creating Capsule node", "with detected plane and pose");
        Pose pose = (Pose) p;
        CapsuleNode capNode = new CapsuleNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        capNode.Anchor(trackable.createAnchor(pose));
        capNode.Trackable(trackable);

        Log.i("creating Capsule node, anchor is", capNode.Anchor().toString());
        return capNode;
    }

    @SimpleFunction(description = "Create a new CapsuleNode with geo coords, if available")
    public CapsuleNode CreateCapsuleNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Capsule node", "with geo location");

        CapsuleNode capNode = new CapsuleNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);
        capNode.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", capNode.Anchor().toString());
        return capNode;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with default properties with a pose.")
    public CapsuleNode CreateCapsuleNodeWithPose(String p) {

        if (session != null) {
            CapsuleNode capNode = new CapsuleNode(this);

            Pose pose = ARUtils.parsePoseObject(p);

            float[] position = {pose.tx(), pose.ty(), pose.tz()};
            float[] rotation = {pose.qx(), pose.qy(), pose.qz(), 1};
            Log.i(LOG_TAG, position + " " + rotation);
            Anchor myAnchor = session.createAnchor(new Pose(new float[]{0f, 0f, -1f}, (new float[]{0f, 0f, 0f, 1f})));

            capNode.Anchor(myAnchor);

            Log.i("created Capsule node, anchor is", capNode.Anchor().toString());
            return capNode;
        }
        Log.i("cannot create Capsule node", " since there is no session");
        return null;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with default properties with a pose.")
    public CapsuleNode CreateCapsuleNodeFromYail(YailDictionary yailNodeObj) {

        if (session != null) {
            try {
                Log.i(LOG_TAG, " triggered via block, capnode is  " + yailNodeObj);

                CapsuleNode capNode = new CapsuleNode(this);
                ARUtils.parseYailToNode(capNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created Capsule node from json, anchor is" + capNode.Anchor().toString());
                return capNode;
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create capsure node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create Capsule node", " since there is no session");
        return null;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with default properties at the specified (x,y,z) position.")
    public CapsuleNode CreateCapsuleNode(float x, float y, float z) {
        Log.i("creating Capsule node", "with x, y, z " + x + " " + y + " " + z);
        CapsuleNode capNode = new CapsuleNode(this);

        float[] position = {x, y, z};
        float[] rotation = {0, 0, 0, 1};
        Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i("creating Capsule node, anchor is", myAnchor.toString());
        capNode.Anchor(myAnchor);

        return capNode;
    }


    @SimpleFunction(description = "Create a new ModelNode with default properties at the specified (x,y,z) position.")
    public ModelNode CreateModelNode(float x, float y, float z, String modelObjectString) {
        if (modelObjectString == null) throw new RuntimeException("You must specify a model asset that has been uploaded to your project");

        ModelNode modelNode = new ModelNode(this);
        modelNode.Model(modelObjectString);

        float[] position = {x, y, z};
        float[] rotation = {0, 0, 0, 1};
        Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i("creating Model node, anchor is", myAnchor.toString());
        modelNode.Anchor(myAnchor);

        return modelNode;
    }

    @SimpleFunction(description = "Create a new ModelNode with geo coords, if available")
    public ModelNode CreateModelNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint, String modelObjectString) {
        if (modelObjectString == null) throw new RuntimeException("You must specify a model asset that has been uploaded to your project");
        ModelNode modelNode = new ModelNode(this);
        modelNode.Model(modelObjectString);
        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);
        modelNode.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", modelNode.Anchor().toString());
        return modelNode;
    }

    @SimpleFunction(description = "Create a new ModelNode with default properties at the plane position.")
    public ModelNode CreateModelNodeAtPlane(ARDetectedPlane targetPlane, Object p, String modelObjectString) {
        Log.i("creating Capsule node", "with detected plane and pose");
        Pose pose = (Pose) p;
        ModelNode mNode = new ModelNode(this);
        mNode.Model(modelObjectString);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        mNode.Anchor(trackable.createAnchor(pose));
        mNode.Trackable(trackable);

        Log.i("creating Capsule node, anchor is", mNode.Anchor().toString());
        return mNode;
    }

    @SimpleFunction(description = "Create a new TubeNode with default properties at the specified (x,y,z) position.")
    public TubeNode CreateTubeNode(float x, float y, float z) {
        return new TubeNode(this);
    }

    @SimpleFunction(description = "Create a new TorusNode with default properties at the specified (x,y,z) position.")
    public TorusNode CreateTorusNode(float x, float y, float z) {
        return new TorusNode(this);
    }

    @SimpleFunction(description = "Create a new PyramidNode with default properties at the specified (x,y,z) position.")
    public PyramidNode CreatePyramidNode(float x, float y, float z) {
        return new PyramidNode(this);
    }



    @SimpleFunction(description = "Create a new TextNode with geo coords, if available")
    public TextNode CreateTextNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating text node", "with geo location");

        TextNode textNode = new TextNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        textNode.Anchor(geoAnchor);

        Log.i("created text node, geo anchor is", textNode.Anchor().toString());
        return textNode;
    }


    @SimpleFunction(description = "Create a new TextNode with default properties at the detected plane position.")
    public TextNode CreateTextNodeAtPlane(ARDetectedPlane targetPlane, Object p, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating text node", "with detected plane and pose");
        Pose pose = (Pose) p;
        TextNode textNode = new TextNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        textNode.Anchor(trackable.createAnchor(pose));
        textNode.Trackable(trackable);
        Log.i("creating text node, anchor is", textNode.Anchor().toString());
        return textNode;
    }

    @SimpleFunction(description = "Create a new boxNode with default properties with a pose.")
    public TextNode CreateTextNodeFromYail(YailDictionary yailNodeObj) {
        if (session != null) {
            try {
                Log.i(LOG_TAG, " triggered via block, text node is " + yailNodeObj);

                TextNode textNode = new TextNode(this);
                ARUtils.parseYailToNode(textNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created textNode node from json, anchor is" + textNode.Anchor().toString());
                return textNode;
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create textnode from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode ", " since there is no session");
        return null;
    }



    @SimpleFunction(description = "Create a new TextNode with default properties at the specified (x,y,z) position.")
    public TextNode CreateTextNode(float x, float y, float z) {

        TextNode textNode = new TextNode(this);

        float[] position = {x, y, z};
        float[] rotation = {0, 0, 0, 1};
        Anchor myAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i("creating textNode, anchor is", myAnchor.toString());
        textNode.Anchor(myAnchor);

        return textNode;
    }
    @SimpleFunction(description = "Create a new VideoNode with default properties at the specified (x,y,z) position.")
    public VideoNode CreateVideoNode(float x, float y, float z) {
        return new VideoNode(this);
    }


    @SimpleFunction(description = "Create a new VideoNode with default properties at the detected plane position.")
    public VideoNode CreateVideoNodeAtPlane(ARDetectedPlane targetPlane, Object p) {
        Log.i("creating video node", "with detected plane and pose");
        Pose pose = (Pose) p;
        VideoNode vNode = new VideoNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        vNode.Anchor(trackable.createAnchor(pose));
        vNode.Trackable(trackable);
        Log.i("creating video node, anchor is", vNode.Anchor().toString());
        return vNode;
    }

    @SimpleFunction(description = "Create a new boxNode with default properties with a pose.")
    public VideoNode CreateVideoNodeFromYail(YailDictionary yailNodeObj) {
        if (session != null) {
            try {
                Log.i(LOG_TAG, " from blocks yailDict is " + yailNodeObj);

                VideoNode videoNode = new VideoNode(this);
                ARUtils.parseYailToNode(videoNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created videoNode node from json, anchor is" + videoNode.Anchor().toString());
                return videoNode;
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create videoNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode ", " since there is no session");
        return null;
    }

    @SimpleFunction(description = "Create a new VideoNode with geo coords, if available")
    public VideoNode CreateVideoNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating text node", "with geo location");

        VideoNode vNode = new VideoNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        vNode.Anchor(geoAnchor);

        Log.i("created text node, geo anchor is", vNode.Anchor().toString());
        return vNode;
    }

    @SimpleFunction(description = "Create a new WebViewNode with default properties at the specified (x,y,z) position.")
    public WebViewNode CreateWebViewNode(float x, float y, float z) {
        return new WebViewNode(this);
    }


    public Anchor setupLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates){
        double[] position = { x, y, z};
        double[] geoPosition = { lat, lng };

        float[] rotation = {0,0,0, 1};
        Log.i(LOG_TAG, position + " " + rotation + " geo is " + geoPosition);
        Earth earth = session.getEarth();
        Anchor geoAnchor  = earth.createAnchor(lat,lng, altitude, rotation);
        // CSB to do
        return geoAnchor;
    }


    @SimpleFunction(description = "Create a new webViewNode with geo coords")
    public WebViewNode CreateWebViewNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating WebView node", "with geo location");

        WebViewNode webViewNode = new WebViewNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        webViewNode.Anchor(geoAnchor);

        Log.i("created webViewNode node, geo anchor is", webViewNode.Anchor().toString());
        return webViewNode;
    }

    @SimpleFunction(description = "Create a new WebViewNode with default properties at the detected plane position.")
    public WebViewNode CreateWebViewNodeAtPlane(ARDetectedPlane targetPlane, Object p) {
        Log.i("creating web node", "with detected plane and pose");
        Pose pose = (Pose) p;
        WebViewNode webNode = new WebViewNode(this);

        Trackable trackable = (Trackable) targetPlane.DetectedPlane();
        webNode.Anchor(trackable.createAnchor(pose));
        webNode.Trackable(trackable);
        Log.i("creating web node, anchor is", webNode.Anchor().toString());
        return webNode;
    }

    @SimpleFunction(description = "Create a new webviewer with default properties with a pose.")
    public WebViewNode CreateWebViewNodeFromYail(YailDictionary yailNodeObj) {
        if (session != null) {
            try {
                WebViewNode webNode = new WebViewNode(this);
                ARUtils.parseYailToNode(webNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created webNode node from json, anchor is" +  webNode.Anchor().toString());
                return webNode;
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create webNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode "," since there is no session");
        return null;
    }

}