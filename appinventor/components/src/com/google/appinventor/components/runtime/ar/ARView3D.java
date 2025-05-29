package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.opengl.GLES30;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.google.appinventor.components.runtime.util.YailDictionary;
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


import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.JsonUtil;
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
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.*;

import java.io.IOException;
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
        "cube.obj, sphere.obj, torus.obj, trigrid.png," +
        "background_show_camera.frag, background_show_camera.vert," +
        "basic.filamat, material_background.filamat, material_basic.filamat," +
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
    private static final float TINT_INTENSITY = 0.1f;
    private static final float TINT_ALPHA = 1.0f;
    private static final int[] TINT_COLORS_HEX = {
            0x000000, 0xF44336, 0xE91E63, 0x9C27B0, 0x673AB7, 0x3F51B5, 0x2196F3, 0x03A9F4, 0x00BCD4,
            0x009688, 0x4CAF50, 0x8BC34A, 0xCDDC39, 0xFFEB3B, 0xFFC107, 0xFF9800,
    };
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 100f;
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

    // AR content
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
        this.glview.setOnTouchListener(tapHelper);

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

        //arViewRender = new ARViewRender(this.view, this, container.$form());
        arViewRender = new ARViewRender(this.glview, this, container.$form());


        installRequested = false;
        depthSettings.onCreate(container.$context());
        instantPlacementSettings.onCreate(container.$context());

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
            virtualSceneFramebuffer.resize(width, height);
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
            filamentFramebuffer.resize(width, height);
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
                        if (n.Type().contains(type)) {
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
        for (ARNode node: nodes){
            if (node != null && node.Anchor() == null) {
                Log.d(LOG_TAG, "Creating default anchor for " + node);
                // TBD handle if anchor is loaded from a db
                node.Anchor(CreateDefaultAnchor()); // assign an anchor if there isn't one
            }
        }
    }

public void drawPlanesAndPoints(ARViewRender render,Camera camera, Frame frame, float[] viewMatrix, float[] projectionMatrix){
    if (ShowFeaturePoints()) {
        pointCloudRenderer.draw(arViewRender, frame.acquirePointCloud(), viewMatrix, projectionMatrix);
    }

    // Draw planes and feature points
    if (PlaneDetectionType() != 0) {
        Log.d(LOG_TAG, " has tracking planes? " + hasTrackingPlane());
        planeRenderer.drawPlanes(arViewRender, session.getAllTrackables(Plane.class),
            camera.getDisplayOrientedPose(), projectionMatrix);
    }

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

    public void drawObjects(ARViewRender render, List<ARNode>objectNodes, float[] viewMatrix, float[] projectionMatrix) {

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, virtualSceneFramebuffer.getFramebufferId());
        GLES30.glViewport(0, 0, virtualSceneFramebuffer.getWidth(), virtualSceneFramebuffer.getHeight());

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glDisable(GLES30.GL_BLEND);        // No blending between objects
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);    // Enable depth testing
        GLES30.glDepthFunc(GLES30.GL_LESS);       // Closer objects win
        GLES30.glDepthMask(true);

        // Draw the Filament texture to the framebuffer
        Log.d(LOG_TAG, "Drawing virtualSceneFramebuffer texture " + virtualSceneFramebuffer.getColorTexture().getTextureId() +
            " to framebuffer " + virtualSceneFramebuffer.getFramebufferId());


        objRenderer.draw(render, objectNodes, viewMatrix, projectionMatrix, virtualSceneFramebuffer);
    }


    public void drawAnimatedObjects(ARViewRender render, List<ARNode>modelNodes, float[] viewMatrix, float[] projectionMatrix) {
        arFilamentRenderer.draw(modelNodes, viewMatrix, projectionMatrix);
        filamentTextureId = arFilamentRenderer.getDisplayTextureId();

        try{
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
        // Draw the Filament texture to the framebuffer
        Log.d(LOG_TAG, "Drawing Filament texture " + filamentTextureId +
            " to framebuffer " + filamentFramebuffer.getFramebufferId());


        //use model node as SCENE nodes to which pose/translation the quad is attached?
        ARNode filamentSceneNode = modelNodes.get(0);

        //quadRenderer.draw(render, filamentSceneNode, currentFilamentTexture, filamentFramebuffer, camera.getDisplayOrientedPose(), projectionMatrix);
    }


    @Override
    public void onDrawFrame(ARViewRender render) {
        if (session == null) {
            return;
        }

        try {
            Frame frame;
            Camera camera;

            try{

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
                frame = session.update();
                camera = frame.getCamera();
            } catch (CameraNotAvailableException e) {
                Log.e(LOG_TAG, "onDrawframe Camera not available: " + e.getMessage(), e);
                return;
            }



            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthFunc(GLES30.GL_LESS);

            // Update display rotation and geometry for rendering
            displayRotationHelper.updateSessionIfNeeded(session);
            if (backgroundRenderer != null){
                backgroundRenderer.updateDisplayGeometry(frame);
                if (frame.getTimestamp() != 0) {
                    backgroundRenderer.drawBackground(render, 0, currentViewportWidth, currentViewportHeight);
                }
            }

            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            int error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error after [draw bakcground]: 0x" + Integer.toHexString(error));
            }
            // Get camera matrices for 3D rendering
            camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);
            camera.getViewMatrix(viewMatrix, 0);

            setDefaultPositions(arNodes);

            String[] modelNodeType = new String[]{"ModelNode"};
            List<ARNode> modelNodes = sort(arNodes, modelNodeType);




            //Framebuffer A
            if (arFilamentRenderer != null && modelNodes.size() > 0) {
                //drawAnimatedObjects(render, modelNodes, viewMatrix, projectionMatrix);
            }


            error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error before [draw arfilament]: 0x" + Integer.toHexString(error));
            }
            GLES30.glFinish();


            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

            drawPlanesAndPoints(render, camera, frame, viewMatrix, projectionMatrix);

            

            String[] genObjectTypes = new String[]{"CapsuleNode", "SphereNode", "BoxNode", "WebViewNode"};
            List<ARNode> objectNodes = sort(arNodes, genObjectTypes);

            //Framebuffer B
            if (virtualSceneFramebuffer != null) {
                if (objRenderer != null && objectNodes.size() > 0){
                    Log.d(LOG_TAG, "objects " + objectNodes);
                    drawObjects(render, objectNodes, viewMatrix, projectionMatrix);
                }
            }
            GLES30.glFinish();

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
            GLES30.glViewport(0, 0, currentViewportWidth, currentViewportHeight);
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

            //backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
            backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);

            drawPlanesAndPoints(render, camera, frame, viewMatrix, projectionMatrix);
            error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error afger [draw objects]: 0x" + Integer.toHexString(error));
            }


            handleTap(frame, camera);





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
    public Anchor CreateDefaultAnchor() {
        float[] position = {0f, 0f, 1};
        float[] rotation = {0, 2, -2, 1};
        Anchor defaultAnchor = session.createAnchor(new Pose(position, rotation));
        Log.i(LOG_TAG, "default anchor with pose: " + defaultAnchor.getPose() + " "+ defaultAnchor.getPose().getTranslation());

        return defaultAnchor;
    }

    private void handleTap(Frame frame, Camera camera) {

        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            Log.i(LOG_TAG, "inside handle tap2 ");
            List<HitResult> hitResultList;
            /*if (instantPlacementSettings.isInstantPlacementEnabled()) {
                hitResultList =
                        frame.hitTestInstantPlacement(tap.getX(), tap.getY(), APPROXIMATE_DISTANCE_METERS);
            } else {
                hitResultList = frame.hitTest(tap);
            }*/

            //TBD node tapped?

            hitResultList = frame.hitTest(tap);
            Log.i(LOG_TAG, "how many hits? " + hitResultList.size());

            for (HitResult hit : hitResultList) {

                Trackable mostRecentTrackable = hit.getTrackable();
                Anchor a = hit.createAnchor();
                Log.i("tap is, pose is, trackable is ", tap.toString() + " " + a.getPose() + " " + mostRecentTrackable);
                if (mostRecentTrackable instanceof Plane){
                    ARDetectedPlane arplane = new DetectedPlane((Plane)mostRecentTrackable);

                    ClickOnDetectedPlaneAt(arplane, a.getPose(), true);

                }
                else if ((mostRecentTrackable instanceof Point && ((Point) mostRecentTrackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)){
                    TapAtPoint(a.getPose().getTranslation()[0], a.getPose().getTranslation()[1], a.getPose().getTranslation()[2], true);
                }
                else if ((mostRecentTrackable instanceof InstantPlacementPoint)
                        || (mostRecentTrackable instanceof DepthPoint)){
                    // are there hooks for this?
                }
            }
        }
    }

    // Check if there are any tracking planes
    private boolean hasTrackingPlane() {
        if (session == null) return false;

        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }

        return false;
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

            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC);
            } else {
                config.setDepthMode(Config.DepthMode.DISABLED);
            }

            config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
            session.configure(config);
        }

    // Update lighting estimation from ARCore
    private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
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
    }

    // Update spherical harmonics coefficients for lighting
    private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
        if (coefficients.length != 9 * 3) {
            throw new IllegalArgumentException("The given coefficients array must be of length 27 (3 components per 9 coefficients)");
        }

        for (int i = 0; i < 9 * 3; ++i) {
            sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
        }

        virtualObjectShader.setVec3Array("u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
    }

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
    public void $add(AndroidViewComponent component) {}

    //@Override
    public void setChildWidth(AndroidViewComponent component, int width) {}

    //@Override
    public void setChildHeight(AndroidViewComponent component, int height) {}


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

    @SimpleFunction(description = "Load scene from storage")
    public List<ARNode> LoadScene(List<YailDictionary> dictionaries) {
        Log.i(LOG_TAG, "loading stored scene " + dictionaries);
        ARNode addNode = null;
        List<ARNode> newNodes = new ArrayList<>();
        for (Object obj : dictionaries) {
            Log.i(LOG_TAG, "loadscene obj is " + obj);
            if (obj == null || obj instanceof gnu.mapping.Symbol){
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
            if (node == null || node instanceof gnu.mapping.Symbol){
                Log.i(LOG_TAG, "savescene null or list " + node);
                continue;
            }
            Log.i(LOG_TAG, "savescene node" + node);
            ARNode arnode = (ARNode) node;
            String type = arnode.Type();
            dictionaries.add(arnode.ARNodeToYail());
            Log.i(LOG_TAG, "saving node " + type);
        }
        return dictionaries;

    }

    @SimpleFunction(description = "Sets Visible to false for all Lights.")
    public void HideAllLights() {}


    //@Override
    @SimpleEvent(description = "The user tapped on a node in the ARView3D.")
    public void addNode(ARNode node){
        Log.i("ADDING ARNODE", "");
        arNodes.add(node);
        Log.i("ADDED ARNODE", node.Type());
        // or would this dispatch a create node event?
    }

    // @Override
    @SimpleEvent(description = "The user tapped on a node in the ARView3D.")
    public void NodeClick(ARNode node) {}

    // @Override
    @SimpleEvent(description = "The user long-pressed a node in the ARView3D.")
    public void NodeLongClick(ARNode node) {}

    //@Override
    @SimpleEvent(description = "The user tapped on a point on the ARView3D.  (x,y,z) is " +
            "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
            "at that point and false otherwise.")
    public void TapAtPoint(float x, float y, float z, boolean isANodeAtPoint) {
        Log.i("TAPPED at ARVIEW3D point", "");
        EventDispatcher.dispatchEvent(this, "TapAtPoint", x, y, z);
    }

    //@Override
    @SimpleEvent(description = "The user long-pressed on a point on the ARView3D.  (x,y,z) is " +
            "the real-world coordinate of the point.  isANoteAtPoint is true if a node is already " +
            "at that point and false otherwise.")
    public void LongPressAtPoint(float x, float y, float z, boolean isANodeAtPoint) {}


    @Override
    @SimpleEvent(description = "A real-world plane was detected.  The detectedPlane is the " +
            "component added at the location of the real-world plane.  This event will only trigger if " +
            "PlaneDetection is not None, and the TrackingType is WorldTracking.  Note that the default " +
            "FillColor of a DetectedPlane is None, so it is shown visually by default.")
    public void PlaneDetected(ARDetectedPlane detectedPlane) {
        container.$form().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(ARView3D.this, "PlaneDetected",detectedPlane);
                Log.i("dispatching detected Plane", " plane is " + detectedPlane);
            }
        });

    }

    @Override
    @SimpleEvent(description = "A DetectedPlane updated its properties, either its rotation, " +
            "position, or size.  This event will only trigger if PlaneDetection is not None, and the " +
            "TrackingType is WorldTracking.")
    public void DetectedPlaneUpdated(ARDetectedPlane detectedPlane) {
        EventDispatcher.dispatchEvent(this, "DetectedPlaneUpdated",detectedPlane);
        Log.i("dispatching updated Plane", "");
    }

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
    public void ClickOnDetectedPlaneAt(ARDetectedPlane targetPlane, Object p, boolean isANodeAtPoint) {
            container.$form().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventDispatcher.dispatchEvent(ARView3D.this, "ClickOnDetectedPlaneAt",targetPlane, p, isANodeAtPoint);
                    Log.i("dispatching Click On Detected Plane", "");
                }
            });

    }

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
                Log.i(LOG_TAG," creating block node from " + yailNodeObj);

                BoxNode boxNode = new BoxNode(this);
                ARUtils.parseYailToNode(boxNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created boxNode node from json, anchor is" +  boxNode.Anchor().toString());
                return boxNode;
            } catch(JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create boxNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode "," since there is no session");
        return null;
    }


    @SimpleFunction(description = "Create a new SphereNode with default properties at the detected plane position.")
    public SphereNode CreateSphereNodeAtPlane(ARDetectedPlane targetPlane, Object p) {
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
                Log.i(LOG_TAG," triggered via block, sphere node is " + yailNodeObj);

                SphereNode sphereNode = new SphereNode(this);
                ARUtils.parseYailToNode(sphereNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created SphereNode node from json, anchor is" +  sphereNode.Anchor().toString());
                return sphereNode;
            } catch(JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create SphereNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode "," since there is no session");
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
        Log.i("cannot create Capsule node"," since there is no session");
        return null;
    }


    @SimpleFunction(description = "Create a new CapsuleNode with default properties with a pose.")
    public CapsuleNode CreateCapsuleNodeFromYail(YailDictionary yailNodeObj) {

        if (session != null) {
            try {
                Log.i(LOG_TAG," triggered via block, capnode is  " + yailNodeObj);

                CapsuleNode capNode = new CapsuleNode(this);
                ARUtils.parseYailToNode(capNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created Capsule node from json, anchor is" +  capNode.Anchor().toString());
                return capNode;
            } catch(JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create capsure node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create Capsule node"," since there is no session");
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

    @SimpleFunction(description = "Create a new TextNode with default properties at the specified (x,y,z) position.")
    public TextNode CreateTextNode(float x, float y, float z) {
        return new TextNode(this);
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
                Log.i(LOG_TAG," from blocks yailDict is " + yailNodeObj);

                VideoNode videoNode = new VideoNode(this);
                ARUtils.parseYailToNode(videoNode, yailNodeObj, session);

                Log.i(LOG_TAG, "SUCCESS created videoNode node from json, anchor is" +  videoNode.Anchor().toString());
                return videoNode;
            } catch(JSONException e) {
                $form().dispatchErrorOccurredEvent(this, "getfromJSON",
                    ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
            } catch (Exception e){
                Log.e(LOG_TAG, "tried to create videoNode node from db string which is yail list" + e);
                throw e;
            }

        }
        Log.i("cannot create boxNode "," since there is no session");
        return null;
    }

    @SimpleFunction(description = "Create a new WebViewNode with default properties at the specified (x,y,z) position.")
    public WebViewNode CreateWebViewNode(float x, float y, float z) {
        return new WebViewNode(this);
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