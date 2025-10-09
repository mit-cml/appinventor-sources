package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import static android.Manifest.permission.CAMERA;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;

import android.app.Activity;
import android.opengl.GLES30;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
//import android.view.RotationGestureDetector;
import android.content.Context;
import android.graphics.PointF;



import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.ar.core.*;
import com.google.ar.core.Camera;
import kawa.standard.let;
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
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.exceptions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import android.util.Log;
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

    private static final float[] sphericalHarmonicFactors = {
        0.282095f, -0.325735f, 0.325735f, -0.325735f, 0.273137f,
        -0.273137f, 0.078848f, -0.273137f, 0.136569f,
    };
    private static final float TINT_INTENSITY = 0.05f;
    private static final float TINT_ALPHA = 1.0f;
    private static final int[] TINT_COLORS_HEX = {
        0x000000, 0xF44336, 0xE91E63, 0x9C27B0, 0x673AB7, 0x3F51B5, 0x2196F3, 0x03A9F4, 0x00BCD4,
        0x009688, 0x4CAF50, 0x8BC34A, 0xCDDC39, 0xFFEB3B, 0xFFC107, 0xFF9800,
    };
    private static final float Z_NEAR = 0.085f;
    private static final float Z_FAR = 20f;
    private static final int CUBE_MAP_RESOLUTION = 16;
    private static final int CUBE_MAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;
    private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;

    private final GLSurfaceView glview;
    private final SurfaceView view;
    private ARViewRender arViewRender;

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

    private boolean useSimulatedDepth = false;

    // Rendering components
    private ARFilamentRenderer arFilamentRenderer;
    private QuadRenderer quadRenderer;
    private BackgroundRenderer backgroundRenderer;
    private ObjectRenderer objRenderer;
    private PlaneRenderer planeRenderer;
    private PointCloudRenderer pointCloudRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private Framebuffer filamentFramebuffer;
    private int quadShader = 0;
    private Shader virtualObjectShader;

    private Texture dfgTexture;
    private SpecularCubeMapFilter cubeMapFilter;
    int filamentTextureId = 0;
    Texture currentFilamentTexture = null;

    // Matrices and math components
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
    private final float[] viewInverseMatrix = new float[16];
    private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] viewLightDirection = new float[4];

    private int currentViewportWidth = 0;
    private int currentViewportHeight = 0;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    //private RotationGestureDetector rotationGestureDetector;

    // Track dragging state
    private ARNode currentlyDraggedNode = null;
    private Frame lastFrame = null;
    private Camera lastCamera = null;
    private final float[] lastViewMatrix = new float[16];
    private final float[] lastProjMatrix = new float[16];

    private float GROUND_LEVEL = 1.0f;
    private boolean groundDetected = false;
    private float lastPhysicsUpdateTime = 0.0f;

    private List<ARNode> arNodes = Nodes();

    public ARView3D(final ComponentContainer container) {
        super(container);
        this.trackingStateHelper = new TrackingStateHelper(container.$context());
        this.displayRotationHelper = new DisplayRotationHelper(container.$context());
        this.tapHelper = new TapHelper(container.$context());


        this.glview = new GLSurfaceView(container.$form());
        this.glview.setPreserveEGLContextOnPause(true);
        this.glview.setEGLContextClientVersion(3);
        this.glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // glSurfaceView.setEGLContextClientVersion(3);
        // glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Create SurfaceView instead of GLSurfaceView
        this.view = new SurfaceView(container.$form());


        // Add a callback to handle Surface creation and changes
        view.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Surface created");
                // Don't initialize Filament here - wait until ARCore session is ready
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(LOG_TAG, "Surface changed: " + width + "x" + height);
                if (arFilamentRenderer != null) {
                    arFilamentRenderer.updateSurfaceDimensions(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Surface destroyed");
                if (arFilamentRenderer != null) {
                    arFilamentRenderer.destroy();
                }
            }
        });


        setupGestureDetectors(container.$context());

        //arViewRender = new ARViewRender(this.view, this, container.$form());
        arViewRender = new ARViewRender(this.glview, this, container.$form());


        installRequested = false;
        depthSettings.onCreate(container.$context());
        instantPlacementSettings.onCreate(container.$context());

        depthSettings.setUseDepthForOcclusion(true);

        // Register with container and form lifecycle events
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

        this.glview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handled = false;
                Log.i(LOG_TAG, "on touch");
                // Process in priority order
                handled |= scaleGestureDetector.onTouchEvent(event);
                if (!scaleGestureDetector.isInProgress()) {
                    Log.i(LOG_TAG, "pass to gestureDetector");
                    handled |= gestureDetector.onTouchEvent(event);
                }

                // Handle drag end
                if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {

                    if (currentlyDraggedNode != null) {
                        Log.i(LOG_TAG, "stopping DRAG");
                        if (currentlyDraggedNode instanceof ARNodeBase) {
                            ((ARNodeBase) currentlyDraggedNode).handleAdvancedGestureUpdate(
                                new PointF(event.getX(), event.getY()),
                                new PointF(0, 0),
                                new PointF(0, 0),
                                null,
                                getCurrentCameraVectors(),
                                "ended"
                            );
                        }
                        currentlyDraggedNode = null;
                        handled = true;
                    }
                }

                return handled || tapHelper.onTouch(v, event);
            }
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
        Log.d(LOG_TAG, "ARView3D.onSurfaceChanged: " + width + "x" + height);

        if (width <= 0 || height <= 0) {
            Log.w(LOG_TAG, "Invalid surface dimensions: " + width + "x" + height);
            return;
        }

        // Create or resize framebuffer
        if (virtualSceneFramebuffer != null) {
            Log.d(LOG_TAG, "resize virtualSceneFramebuffer: " + virtualSceneFramebuffer);
            //filamentFramebuffer.resize(width, height);
        } else {
            // Create the virtual scene framebuffer
            try {
                virtualSceneFramebuffer = new Framebuffer(render, width, height);
                Log.d(LOG_TAG, "Created virtualSceneFramebuffer: " +
                    virtualSceneFramebuffer.getFramebufferId() + " " + width + "x" + height);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to create framebuffer: " + e.getMessage());
            }
        }


        // Create or resize framebuffer
        if (filamentFramebuffer != null) {
            Log.d(LOG_TAG, "resize virtualSceneFramebuffer: " + virtualSceneFramebuffer);
            //filamentFramebuffer.resize(width, height);
        } else {
            // Create the virtual scene framebuffer
            try {
                filamentFramebuffer = new Framebuffer(render, width, height);
                Log.d(LOG_TAG, "Created filamentFramebuffer: " +
                    filamentFramebuffer.getFramebufferId() + " " + width + "x" + height);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to create filamentFramebuffer: " + e.getMessage());
            }
        }

        currentViewportWidth = width;
        currentViewportHeight = height;

        // Update ARFilamentRenderer dimensions if available
        if (arFilamentRenderer != null) {
            Log.d(LOG_TAG, "Loading onsurface changed for  arfilamentrenderer");
            arFilamentRenderer.updateSurfaceDimensions(width, height);
        }

        Log.d(LOG_TAG, "Surface changed processin" +
            "g complete");
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


    // Filter ARNodes by type and ensure they have anchors
    public void setDefaultPositions(List<ARNode> nodes) {
        for (ARNode node : nodes) {
            if (node != null && node.Anchor() == null) {
                Log.d(LOG_TAG, "Creating default anchor for " + Arrays.toString(node.PoseFromPropertyPosition()));
                // TBD handle if anchor is loaded from a db
                node.Session(session);
                node.Anchor(CreateDefaultAnchor(node.PoseFromPropertyPosition())); // assign an anchor if there isn't one
            }
        }
    }

    public void emitPlaneDetectedEvent() {
        Collection<Plane> planes = session.getAllTrackables(Plane.class);
        Log.d(LOG_TAG, "Number of planes detected: " + planes.size());

        for (Plane plane : planes) {
            ARDetectedPlane arplane = new DetectedPlane(plane);
            Log.i("has tracking arplane", arplane.toString());
            PlaneDetected(arplane); //dispatch
            Log.d(LOG_TAG, "Plane tracking state: " + plane.getTrackingState() +
                ", type: " + plane.getType() +
                ", extent: " + plane.getExtentX() + "x" + plane.getExtentZ());
        }
    }

    public void drawPlanesAndPoints(ARViewRender render, Camera camera, Frame frame, float[] viewMatrix, float[] projectionMatrix) {
        if (ShowFeaturePoints()) {
            pointCloudRenderer.draw(arViewRender, frame.acquirePointCloud(), viewMatrix, projectionMatrix);
        }

        // Draw planes and feature points
        /*if (PlaneDetectionType() != 0) {
            Log.d(LOG_TAG, " has tracking planes? " + hasTrackingPlane());
            planeRenderer.drawPlanes(arViewRender, session.getAllTrackables(Plane.class),
                camera.getDisplayOrientedPose(), projectionMatrix);
        }*/

        emitPlaneDetectedEvent();

    }

    public void drawObjects(ARViewRender render, List<ARNode> objectNodes, float[] viewMatrix, float[] projectionMatrix) {

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, virtualSceneFramebuffer.getFramebufferId());
        GLES30.glViewport(0, 0, virtualSceneFramebuffer.getWidth(), virtualSceneFramebuffer.getHeight());

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glDisable(GLES30.GL_BLEND);        // No blending between objects
        //GLES30.glEnable(GLES30.GL_DEPTH_TEST);    // Enable depth testing
        // GLES30.glDepthFunc(GLES30.GL_LESS);       // Closer objects win
        // GLES30.glDepthMask(true);

        // Draw the Filament texture to the framebuffer
        Log.d(LOG_TAG, "Drawing virtualSceneFramebuffer texture " + virtualSceneFramebuffer.getColorTexture().getTextureId() +
            " to framebuffer " + virtualSceneFramebuffer.getFramebufferId());

       // updatePhysics();

        objRenderer.draw(render, objectNodes, viewMatrix, projectionMatrix, null); //virtualSceneFramebuffer);
    }


    public void drawAnimatedObjects(ARViewRender render, Camera camera, List<ARNode> modelNodes, float[] viewMatrix, float[] projectionMatrix) {
        arFilamentRenderer.draw(modelNodes, viewMatrix, projectionMatrix);
        filamentTextureId = arFilamentRenderer.getDisplayTextureId();

        try {
            if (filamentTextureId > 0 && currentFilamentTexture == null) {
                currentFilamentTexture = Texture.createFromId(render, filamentTextureId);
                Log.d(LOG_TAG, "create texture from filament texture id  " + filamentTextureId + " and should match:" + currentFilamentTexture.getTextureId());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in createFromId in onDrawFrame: " + e.getMessage(), e);
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, filamentFramebuffer.getFramebufferId());
        GLES30.glViewport(0, 0, filamentFramebuffer.getWidth(), filamentFramebuffer.getHeight());

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);

        GLES30.glDisable(GLES30.GL_BLEND);        // No blending between objects
        // Draw the Filament texture to the framebuffer
        Log.d(LOG_TAG, "Drawing Filament texture " + filamentTextureId +
            " to framebuffer " + filamentFramebuffer.getFramebufferId());


        quadRenderer.draw(render, modelNodes, currentFilamentTexture, filamentFramebuffer, camera.getDisplayOrientedPose(), projectionMatrix);
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

    private void updateFilamentWithARCoreDepth(Frame frame, android.media.Image depthImage) {
        try {

            // Get actual depth image dimensions
            int depthWidth = depthImage.getWidth();
            int depthHeight = depthImage.getHeight();

            Log.d(LOG_TAG, "ARCore depth image size: " + depthWidth + "x" + depthHeight);
            // Convert ARCore depth image to ByteBuffer
            ByteBuffer depthBuffer = convertDepthImageToBuffer(depthImage);

            // Get depth UV transform from ARCore
            // Define 4 corner points in NDC coordinates (8 floats = 4 coordinate pairs)
            float[] ndcQuad = {
                -1f, -1f,  // bottom-left
                1f, -1f,  // bottom-right
                -1f, 1f,  // top-left
                1f, 1f   // top-right
            };
            float[] textureCoords = new float[8];


            // Transform coordinates from NDC to texture space - also see background renderer which does the same thing
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                ndcQuad,           // Input: 8 floats (4 coordinate pairs)
                Coordinates2d.TEXTURE_NORMALIZED,
                textureCoords      // Output: 8 floats (4 coordinate pairs)
            );

            // Convert the 4 corner points to a transformation matrix
            float[] depthUvTransform = calculateTransformMatrix(textureCoords);


            // Pass to Filament renderer
            if (arFilamentRenderer != null) {
                arFilamentRenderer.updateARCoreDepth(
                    depthBuffer,
                    depthUvTransform,
                    Z_NEAR,
                    Z_FAR,
                    depthWidth,
                    depthHeight
                );
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error updating Filament with ARCore depth", e);
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
        float deltaTime = (currentTime - lastPhysicsUpdateTime) / 1000.0f;
        deltaTime = Math.max(deltaTime, 1.0f / 60.0f); //
        lastPhysicsUpdateTime = currentTime;

        // Update each node's physics
        for (ARNode node : arNodes) {
            if (((ARNodeBase)node).enablePhysics) {
                ((ARNodeBase)node).updateSimplePhysics(deltaTime);
            }
        }
    }

    @Override
    public void onDrawFrame(ARViewRender render) {
        if (session == null) {
            return;
        }

        try {


            try {

                // as soon as this is set up, it seems it grabs the surface from the filament renderer
                // may need to explicitly bind some how
                if (!hasSetTextureNames) {

                    if (backgroundRenderer != null && backgroundRenderer.getCameraColorTexture() != null) {
                        int textureId = backgroundRenderer.getCameraColorTexture().getTextureId();
                        if (textureId != 0) {
                            Log.d(LOG_TAG, "onDrawFrame  has CAMERA texture " + textureId);
                            session.setCameraTextureNames(new int[]{textureId});
                            hasSetTextureNames = true;
                        }
                    }

                    if (!hasSetTextureNames) {
                        Log.d(LOG_TAG, "no texture");
                        return;
                    }
                }

                // Update ARCore frame and camera

                lastFrame = session.update();
                lastCamera = lastFrame.getCamera();

                lastCamera.getViewMatrix(lastViewMatrix, 0);
                lastCamera.getProjectionMatrix(lastProjMatrix, 0, Z_NEAR, Z_FAR);

            } catch (CameraNotAvailableException e) {
                Log.e(LOG_TAG, "onDrawframe Camera not available: " + e.getMessage(), e);
                return;
            }


            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            // DISABLE depth for camera background
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthMask(false); // Don't write to screen depth buffer    // DISABLE depth for camera background
            GLES30.glEnable(GLES30.GL_BLEND);

            // Update display rotation and geometry for rendering
            displayRotationHelper.updateSessionIfNeeded(session);
            if (backgroundRenderer != null) {
                backgroundRenderer.updateDisplayGeometry(lastFrame);
                try {
                    backgroundRenderer.setUseDepthVisualization(render, false);
                    backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to read a required asset file", e);
                    return;
                }
                if (lastFrame.getTimestamp() != 0) {
                    backgroundRenderer.drawBackground(render, 0, currentViewportWidth, currentViewportHeight);
                }
            }

            if (lastCamera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            if (lastCamera.getTrackingState() == TrackingState.TRACKING
                && (depthSettings.useDepthForOcclusion()
                || depthSettings.depthColorVisualizationEnabled())) {
                try (android.media.Image depthImage = lastFrame.acquireDepthImage16Bits()) {
                    useSimulatedDepth = false;
                    backgroundRenderer.updateCameraDepthTexture(depthImage);
                    if (arFilamentRenderer != null) {
                        updateFilamentWithARCoreDepth(lastFrame, depthImage);
                    }
                } catch (NotYetAvailableException e) {
                    // This normally means that depth data is not available yet. This is normal so we will not
                    // spam the logcat with this.
                } catch (IllegalStateException ie) {
                    // if we can't acquire depth data, that is ok
                    useSimulatedDepth = true;
                    Log.i(LOG_TAG," no depth camera");
                }
            }

            int error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error after [draw bakcground]: 0x" + Integer.toHexString(error));
            }
            // Get camera matrices for 3D rendering
            lastCamera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
            lastCamera.getViewMatrix(viewMatrix, 0);

            setDefaultPositions(arNodes);

            String[] modelNodeType = new String[]{"ModelNode"};
            List<ARNode> modelNodes = sort(arNodes, modelNodeType);


            //Framebuffer A
            if (arFilamentRenderer != null && modelNodes.size() > 0) {
                drawAnimatedObjects(render, lastCamera, modelNodes, viewMatrix, projectionMatrix);
            }


            error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error before [draw arfilament]: 0x" + Integer.toHexString(error));
            }
            GLES30.glFinish();


            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            // DISABLE depth for camera background
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthMask(false); // Don't write to screen depth buffer
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

            drawPlanesAndPoints(render, lastCamera, lastFrame, viewMatrix, projectionMatrix);
            //emitPlaneDetectedEvent();


            updateCollisions();
            String[] genObjectTypes = new String[]{"CapsuleNode", "SphereNode", "BoxNode", "WebViewNode"};
            List<ARNode> objectNodes = sort(arNodes, genObjectTypes);

            //Framebuffer B
           // if (virtualSceneFramebuffer != null) {
                if (objRenderer != null && objectNodes.size() > 0) {
                    Log.d(LOG_TAG, "objects " + objectNodes);
                    drawObjects(render, objectNodes, viewMatrix, projectionMatrix);
                }
           // }
            GLES30.glFinish();

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);

            //backgroundRenderer.drawVirtualSceneComposite(render, filamentFramebuffer, virtualSceneFramebuffer, Z_NEAR, Z_FAR);

            error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error afger [draw objects]: 0x" + Integer.toHexString(error));
            }


            GLES30.glDisable(GLES30.GL_BLEND);
            GLES30.glFinish();

            // Note: we don't need to extract texture or draw composed scene manually
            // Filament renders directly to the swapchain
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in onDrawFrame: " + e.getMessage(), e);
        }
    }

    // Initialize renderers
    private void initializeFilamentAndRenderers() {
        try {

            // Initialize background renderer
            backgroundRenderer = new BackgroundRenderer(arViewRender);

            planeRenderer = new PlaneRenderer(arViewRender);

            // Initialize point cloud renderer for showing feature points
            pointCloudRenderer = new PointCloudRenderer(arViewRender);

            objRenderer = new ObjectRenderer(arViewRender);

            quadRenderer = new QuadRenderer(arViewRender);
            // Create and initialize ARFilamentRenderer
            Log.d(LOG_TAG, "instantiating new arfilamentrenderer" + this.glview);
            arFilamentRenderer = new ARFilamentRenderer(this.container, this.glview);
            arFilamentRenderer.initialize();

            Log.d(LOG_TAG, "ARFilamentRenderer initialized successfully");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to initialize Filament and renderers", e);
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
                if (mostRecentTrackable instanceof Plane) {
                    ARDetectedPlane arplane = new DetectedPlane((Plane) mostRecentTrackable);

                    ClickOnDetectedPlaneAt(arplane, a.getPose(), false, true);


                } else if ((mostRecentTrackable instanceof Point && ((Point) mostRecentTrackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    TapAtPoint(a.getPose().getTranslation()[0], a.getPose().getTranslation()[1], a.getPose().getTranslation()[2], true);
                } else if (mostRecentTrackable instanceof Point && useSimulatedDepth) {
                    Log.i("point hit", a.getPose().getTranslation()[0] + " " + a.getPose().getTranslation()[1] + " " + a.getPose().getTranslation()[2]);
                    TapAtPoint(a.getPose().getTranslation()[0], a.getPose().getTranslation()[1], a.getPose().getTranslation()[2], true);
                } else if ((mostRecentTrackable instanceof InstantPlacementPoint)
                    || (mostRecentTrackable instanceof DepthPoint)) {


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
            float[] worldPos = node.Anchor().getPose().getTranslation();
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

    // Enhanced getNodeScreenRadius logging
    private float getNodeScreenRadius(ARNode node, float[] worldPos) {
        float baseRadius = 60f;
        Log.i(LOG_TAG, "Base radius: " + baseRadius);

        if (node instanceof SphereNode) {
            SphereNode sphere = (SphereNode) node;
            float worldRadius = sphere.RadiusInCentimeters() * sphere.Scale();
            Log.i(LOG_TAG, "Sphere world radius: " + worldRadius + " cm");

            float approximateScreenRadius = worldRadius * 0.01f;
            Log.i(LOG_TAG, "Approximate screen radius: " + approximateScreenRadius);

            baseRadius = Math.max(baseRadius, approximateScreenRadius);
            Log.i(LOG_TAG, "Max of base and sphere radius: " + baseRadius);
        }

        Log.i(LOG_TAG, "Final node radius: " + baseRadius + " pixels");
        return baseRadius;
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
                    node.Anchor().getPose().getTranslation(),
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
            float[] currentPos = node.Anchor().getPose().getTranslation();
            Log.i(LOG_TAG, "Using CURRENT position: (" + currentPos[0] + ", " + currentPos[1] + ", " + currentPos[2] + ")");
            return currentPos;
        }

        return null;
    }

    private float[] projectFingerIncrementally(float[] currentPos, PointF fingerMovement) {
        Log.d(LOG_TAG, "projectFingerIncrementally: fingerMovement=(" + fingerMovement.x + ", " + fingerMovement.y + ")");

        if (lastCamera == null) {
            Log.w(LOG_TAG, "lastCamera is null - using basic fallback");
            float scale = 0.0002f;
            return new float[]{
                currentPos[0] + fingerMovement.x * scale,
                currentPos[1],
                currentPos[2]
            };
        }

        try {
            // Get camera transform
            float[] cameraMatrix = new float[16];
            lastCamera.getPose().toMatrix(cameraMatrix, 0);

            // Extract camera forward vector
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

            float newZ = currentPos[2] + rotatedZ;
            newZ = Math.max(-4.0f, Math.min(-1.0f, newZ));

            float[] result = new float[]{
                currentPos[0] + rotatedX,
                currentPos[1],
                newZ
            };

            Log.d(LOG_TAG, "Calculated movement: (" + rotatedX + ", 0, " + rotatedZ + ")");
            Log.d(LOG_TAG, "New position: (" + result[0] + ", " + result[1] + ", " + result[2] + ")");

            return result;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in incremental projection", e);
            return currentPos;
        }
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



    // In ARView3D update loop
    private void updateCollisions() {
        List<ARNode> nodes = arNodes;

        for (int i = 0; i < arNodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                ARNode nodeA = nodes.get(i);
                ARNode nodeB = nodes.get(j);

                if (checkCollision(nodeA, nodeB)) {
                    handleCollision(nodeA, nodeB);
                }
            }
        }
    }
    // Add this debug logging to your collision detection when objects should be colliding
    private boolean checkCollision(ARNode nodeA, ARNode nodeB) {
        float[] posA = ((ARNodeBase)nodeA).getCurrentPosition();
        float[] posB = ((ARNodeBase)nodeB).getCurrentPosition();

        float distance = vectorDistance(posA, posB);

        // Get the bounding box dimensions in meters
        float[] boundsA = nodeA.getVisualBounds();
        float[] boundsB = nodeB.getVisualBounds();

        // Add detailed debugging for close objects
        if (distance < 0.3f) { // Within 30cm - should show collision details
            Log.i(LOG_TAG, "=== CLOSE OBJECTS DEBUG ===");
            if (nodeA instanceof SphereNode) {
                SphereNode sphereA = (SphereNode) nodeA;
                Log.i(LOG_TAG, "NodeA - RadiusCM: " + sphereA.RadiusInCentimeters() + ", Scale: " + sphereA.Scale());
                Log.i(LOG_TAG, "NodeA - Expected visual radius: " + (sphereA.RadiusInCentimeters() * 0.01f * sphereA.Scale()) + "m");
            }
            if (nodeB instanceof SphereNode) {
                SphereNode sphereB = (SphereNode) nodeB;
                Log.i(LOG_TAG, "NodeB - RadiusCM: " + sphereB.RadiusInCentimeters() + ", Scale: " + sphereB.Scale());
                Log.i(LOG_TAG, "NodeB - Expected visual radius: " + (sphereB.RadiusInCentimeters() * 0.01f * sphereB.Scale()) + "m");
            }
        }

        // Calculate half-extents
        float[] halfExtentsA = {boundsA[0] / 2, boundsA[1] / 2, boundsA[2] / 2};
        float[] halfExtentsB = {boundsB[0] / 2, boundsB[1] / 2, boundsB[2] / 2};

        boolean xOverlap = Math.abs(posA[0] - posB[0]) < (halfExtentsA[0] + halfExtentsB[0]);
        boolean yOverlap = Math.abs(posA[1] - posB[1]) < (halfExtentsA[1] + halfExtentsB[1]);
        boolean zOverlap = Math.abs(posA[2] - posB[2]) < (halfExtentsA[2] + halfExtentsB[2]);

        boolean collision = xOverlap && yOverlap && zOverlap;

        // Log collision analysis for close objects
        if (distance < 0.3f) {
            Log.i(LOG_TAG, "Distance: " + distance + "m, Collision: " + collision);
            Log.i(LOG_TAG, "Visual bounds A: (" + boundsA[0] + ", " + boundsA[1] + ", " + boundsA[2] + ")");
            Log.i(LOG_TAG, "Visual bounds B: (" + boundsB[0] + ", " + boundsB[1] + ", " + boundsB[2] + ")");
            Log.i(LOG_TAG, "Sum of radii approx: " + (halfExtentsA[0] + halfExtentsB[0]) + "m");

            if (distance < (halfExtentsA[0] + halfExtentsB[0]) && !collision) {
                Log.w(LOG_TAG, "*** SPHERES SHOULD BE COLLIDING BUT AABB FAILED ***");
                Log.w(LOG_TAG, "Consider using sphere collision for SphereNodes");
            }
        }

        return collision;
    }

    private void handleCollision(ARNode nodeA, ARNode nodeB) {
        // Separate objects first
        separateCollidingNodes(nodeA, nodeB);

        // Delayed notification to avoid physics interference
         nodeA.ObjectCollidedWithObject(nodeB);
         nodeB.ObjectCollidedWithObject(nodeA);

           // dispatchCollisionEvent(nodeA, nodeB);
    }

    private void separateCollidingNodes(ARNode nodeA, ARNode nodeB) {
        float[] posA = ((ARNodeBase)nodeA).getCurrentPosition();
        float[] posB = ((ARNodeBase)nodeB).getCurrentPosition();

        // Get the bounding box dimensions for both nodes
        float[] boundsA = nodeA.getVisualBounds();
        float[] boundsB = nodeB.getVisualBounds();

        // Calculate overlap in each axis
        float[] overlap = new float[3];
        overlap[0] = (boundsA[0] + boundsB[0]) / 2 - Math.abs(posA[0] - posB[0]);
        overlap[1] = (boundsA[1] + boundsB[1]) / 2 - Math.abs(posA[1] - posB[1]);
        overlap[2] = (boundsA[2] + boundsB[2]) / 2 - Math.abs(posA[2] - posB[2]);

        // Find the axis with minimum overlap (shortest separation distance)
        int minAxis = 0;
        if (overlap[1] < overlap[minAxis]) minAxis = 1;
        if (overlap[2] < overlap[minAxis]) minAxis = 2;

        // Calculate separation distance - need to separate by FULL overlap plus a small gap
        float separationDistance = overlap[minAxis] + 0.01f; // Add 1cm gap to prevent immediate re-collision

        Log.i(LOG_TAG, "Overlap on axis " + minAxis + ": " + overlap[minAxis] + "m");
        Log.i(LOG_TAG, "Total separation distance: " + separationDistance + "m");

        // Calculate separation vector - each object moves half the total distance
        float[] separation = new float[3];
        float halfSeparation = separationDistance * 0.5f;

        // Determine separation direction based on relative positions
        if (posA[minAxis] < posB[minAxis]) {
            separation[minAxis] = -halfSeparation;  // Move A in negative direction
        } else {
            separation[minAxis] = halfSeparation;   // Move A in positive direction
        }

        // Apply separation (move objects apart equally)
        float[] newPosA = ((ARNodeBase)nodeA).vectorAdd(posA, separation);
        float[] newPosB = ((ARNodeBase)nodeB).vectorSubtract(posB, separation);

        ((ARNodeBase)nodeA).setCurrentPosition(newPosA);
        ((ARNodeBase)nodeB).setCurrentPosition(newPosB);

        Log.i(LOG_TAG, "Separated nodes on axis " + minAxis + " by total distance " + separationDistance + "m");
        Log.i(LOG_TAG, "Each node moved " + halfSeparation + "m");

        // Verify separation worked
        float newDistance = Math.abs(newPosA[minAxis] - newPosB[minAxis]);
        float requiredDistance = (boundsA[minAxis] + boundsB[minAxis]) / 2;
        Log.i(LOG_TAG, "New distance on axis " + minAxis + ": " + newDistance + "m (required: " + requiredDistance + "m)");
    }



    // Configure ARCore session
    private void configureSession() {

        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        // This is the critical part for plane detection
        if (PlaneDetectionType() == 1 || PlaneDetectionType() == 3) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
        } else if (PlaneDetectionType() == 2) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.VERTICAL);
        } else if (PlaneDetectionType() == 3) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
        } else {
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        }




// Check whether the user's device supports the Depth API.
        boolean isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC);
        boolean isGeospatialSupported =
            session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED);
        Log.i(LOG_TAG, "ARCore: geospatial supported ? " + isGeospatialSupported);

        if (isDepthSupported) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {

            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        /*if (isDepthSupported && isGeospatialSupported) {
            // These three settings are needed to use Geospatial Depth.
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
            config.setGeospatialMode(Config.GeospatialMode.ENABLED);
            Log.i(LOG_TAG, "setting geospatial mode " + isGeospatialSupported);
           // config.setStreetscapeGeometryMode(Config.StreetscapeGeometryMode.ENABLED);
        }*/

        config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
        session.configure(config);
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
        if (session != null) {
            return;
        }

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
            session.resume();
            Log.d(LOG_TAG, "resume called");
            // Initialize renderers with ARCore session
            //initializeFilamentAndRenderers();

            // Resume display rotation helper
            displayRotationHelper.onResume();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to create AR session", e);
        }
    }


    @Override
    public View getView() {

        Log.d(LOG_TAG, "get glview not surface view");
        return glview;
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
        Log.i("ADDED ARNODE", node.NodeType() + " and session is null? " + (session == null));
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

    //@Override
    @SimpleEvent(description = "The user tapped on a point on the ARView3D.  (x,y,z) is " +
        "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
        "at that point and false otherwise.")
    public void TapAtPoint(float x, float y, float z, boolean isANodeAtPoint) {
        Log.i("TAPPED at ARVIEW3D point", "");
        EventDispatcher.dispatchEvent(this, "TapAtPoint", x, y, z, isANodeAtPoint);
    }


    @SimpleEvent(description = "all world and geo coords if avail")
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

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Determines whether enable occlusion in world understanding. If true, objects will disappear behind walls, furniture and sometime floor")
    public boolean EnableOcclusion() {
        return enableOcclusion;
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
            } catch (JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
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

        BoxNode boxNode = new BoxNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        boxNode.Anchor(geoAnchor);

        Log.i("created Box node, geo anchor is", boxNode.Anchor().toString());
        return boxNode;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with geo coords")
    public SphereNode CreateSphereNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint) {
        Log.i("creating Sphere node", "with geo location");

        SphereNode sphereNode = new SphereNode(this);

        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);

        sphereNode.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", sphereNode.Anchor().toString());
        return sphereNode;
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
            } catch (JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
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

    @SimpleFunction(description = "Create a new CapsuleNode with geo coords")
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
            } catch (JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
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

    @SimpleFunction(description = "Create a new ModelNode with geo coords")
    public ModelNode CreateModelNodeAtLocation(float x, float y, float z, double lat, double lng, double altitude, boolean hasGeoCoordinates, boolean isANodeAtPoint, String modelObjectString) {
        Log.i("creating ModelNode ", "with geo location");
        if (modelObjectString == null) throw new RuntimeException("You must specify a model asset that has been uploaded to your project");
        ModelNode modelNode = new ModelNode(this);
        modelNode.Model(modelObjectString);
        Anchor geoAnchor = setupLocation(x, y, z, lat, lng, altitude, hasGeoCoordinates);
        modelNode.Anchor(geoAnchor);

        Log.i("created Capsule node, geo anchor is", modelNode.Anchor().toString());
        return modelNode;
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



    @SimpleFunction(description = "Create a new TextNode with geo coords")
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
            } catch (JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
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
            } catch (JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e) {
                Log.e(LOG_TAG, "tried to create videoNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode ", " since there is no session");
        return null;
    }

    @SimpleFunction(description = "Create a new VideoNode with geo coords")
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
                Log.i(LOG_TAG," from blocks yailDict is " + yailNodeObj);

                WebViewNode webNode = new WebViewNode(this);
                ARUtils.parseYailToNode(webNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created webNode node from json, anchor is" +  webNode.Anchor().toString());
                return webNode;
            } catch(JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create webNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode "," since there is no session");
        return null;
    }

}