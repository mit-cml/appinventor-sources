package com.google.appinventor.components.runtime.arview.renderer;

import android.opengl.GLSurfaceView;
import android.opengl.GLES30;  // Import all GLES30 methods statically
import android.os.Looper;
import android.os.Handler;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.Matrix;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.LightManager;
import com.google.android.filament.Material;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.RenderTarget;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.Texture;
import com.google.android.filament.TextureSampler;
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;

import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;

import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.Form;


import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import java.lang.System;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * ARFilamentRenderer handles 3D rendering using Google Filament engine
 * integrated with ARCore for Augmented Reality applications.
 */
@UsesAssets(fileNames = "occlusion2.filamat")
public class ARFilamentRenderer {
    private static final String LOG_TAG = ARFilamentRenderer.class.getSimpleName();

    // Filament core components
    private Engine engine;
    private Renderer renderer;
    private Scene modelScene;    // For 3D models only
    private Scene compositeScene; // For final composite quad
    private View view;
    private Camera camera;
    private SwapChain swapChain;

    // For loading models
    private AssetLoader assetLoader;
    private ResourceLoader resourceLoader;
    private MaterialProvider materialProvider;

    // Testing
    private int testTriangleEntity = 0;

    // Cache for models
    private Map<ARNode, FilamentAsset> nodeAssetMap = new ConcurrentHashMap<>();
    private Map<FilamentAsset, List<Integer>> assetEntityMap = new ConcurrentHashMap<>();
    private Map<String, Material> materialCache = new HashMap<>();

    // Rendering state
    private int cameraEntity;
    private int mainLightEntity;
    private int viewportWidth = 1;
    private int viewportHeight = 1;

    private Form formCopy;
    private ARViewRender arViewRender;
    private boolean isInitialized = false;
    private boolean hasSetTextureNames = false;

    private static final float Z_NEAR = 0.085f;
    private static final float Z_FAR = 20f;
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];


    RenderTarget filamentRenderTarget;
    RenderTarget compositeRenderTarget;
    private int displayTextureId = -1;
    private Texture compositeColorTexture;
    private Texture virtualSceneColorTexture;
    private Texture virtualSceneDepthTexture;
    private Texture arDepthTexture;

    private Texture compositeDepthTexture;
    private int depthTextureId = -1;
    private MaterialInstance occlusionMaterialInstance;
    private MaterialInstance standardMaterialInstance;
    private float[] depthUvTransform = new float[16];

    boolean hasNonZeroData = false;
    // Add a flag to track when occlusion is ready
    private boolean occlusionTexturesReady = false;
    private boolean occlusionMaterialsApplied = false;

    // Animation supportin
    private float animationTime = 0.0f;
    private boolean animationEnabled = false;


    private ByteBuffer pixelBuffer = null;
    private int pixelBufferSize = 0;
    private int simpleTriangleEntity = 0;

    private int quadEntity = 0;


    VertexBuffer.Builder vbBuilder;
    VertexBuffer vertexBufferObj;
    VertexBuffer vertexBuffer;
    // Create index buffer
    short[] indices = {0, 1, 2, 2, 1, 3};
    ShortBuffer indexBuffer;

    IndexBuffer.Builder ibBuilder;
    IndexBuffer indexBufferObj;

    float[] quadVertices = {
            -1.0f, -1.0f, -1.0f,  // bottom-left
            1.0f, -1.0f, -1.0f,   // bottom-right
            -1.0f, 1.0f, -1.0f,   // top-left
            1.0f, 1.0f, -1.0f     // top-right
    };

    // Bright magenta color for all vertices
    float[] quadColors = {
            0.0f, 1.0f, 1.0f, 1.0f,  // magenta
            1.0f, 0.0f, 1.0f, 1.0f,  // magenta
            1.0f, 0.0f, 1.0f, 1.0f,  // magenta
            1.0f, 0.0f, 1.0f, 1.0f   // magenta
    };

// In your initialization
// ...

    private ComponentContainer contextContainer = null;
    private GLSurfaceView glSurfaceView= null;
    /**
     * Create a new ARFilamentRenderer
     */
    public ARFilamentRenderer(ComponentContainer container, GLSurfaceView glview) throws IOException {
        this.contextContainer = container;
        this.glSurfaceView = glview;
        formCopy = container.$form();
        Log.d(LOG_TAG, "ARFilamentRenderer constructor called " + this.glSurfaceView);

        //initialize();
    }


    /**
     * Initialize Filament components
     */
    public void initialize() {
        if (isInitialized) {
            Log.d(LOG_TAG, "Filament already initialized, skipping");
            return;
        }



        Log.d(LOG_TAG, "Initializing Filament " + this.glSurfaceView);

        if (this.glSurfaceView != null) {
                // Queue the initialization to run on the GL thread
                this.glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {

                        EGLContext context = EGL14.eglGetCurrentContext();
                        Log.d(LOG_TAG, "Initializing Filament in GL context: " + context);

                        initializeFilamentLibs();
                        // This code runs on the GL thread where the context is active
                        initializeFilament();
                    }
                });
            }

    }
        /**
         * Load Filament libraries - ensures proper loading of native libraries
         */
        private synchronized void initializeFilamentLibs() {
            try {
                System.loadLibrary("filament-jni");
                System.loadLibrary("gltfio-jni");
                Log.d(LOG_TAG, "Filament libraries loaded successfully");
            } catch (UnsatisfiedLinkError e) {
                Log.e(LOG_TAG, "Failed to load Filament libraries", e);
                throw e;
            }
        }

    private long getNativeContextHandle() {
        try {
            EGLContext context = EGL14.eglGetCurrentContext();
            if (context == EGL14.EGL_NO_CONTEXT) {
                return 0;
            }

            // Get the native handle using reflection
            java.lang.reflect.Method getNativeHandle = EGLContext.class.getMethod("getNativeHandle");
            Object result = getNativeHandle.invoke(context);
            if (result instanceof Long) {
                return (Long) result;
            }
            return 0;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get native context handle: " + e.getMessage(), e);
            return 0;
        }
    }
                /**
                 * Load Filament libraries - ensures proper loading of native libraries
                 */
    private void initializeFilament() {
        try {

            // Get current EGL context from the GL thread
            EGLContext currentContext = EGL14.eglGetCurrentContext();
            Log.d(LOG_TAG, "Creating Filament with shared context: " + currentContext);


            long nativeContextHandle = getNativeContextHandle();
            Log.d(LOG_TAG, "Creating Filament with native context handle: " + nativeContextHandle);

            // Create engine with shared context
            if (nativeContextHandle > 0) {
                engine = Engine.create(nativeContextHandle);
            } else {
                engine = Engine.create(currentContext);
            }


            Log.d(LOG_TAG, "Engine created successfully");

            renderer = engine.createRenderer();


            modelScene = engine.createScene();
            compositeScene = engine.createScene();

            // Create camera entity
            cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);


            // Create view
            view = engine.createView();

            // Explicitly position and orient the camera
            camera.lookAt(
                    0.0f, 0.0f, 2.0f,  // Eye position 2 units back
                    0.0f, 0.0f, 0.0f,  // Look at origin
                    0.0f, 1.0f, 0.0f   // Up vector
            );

            view.setCamera(camera);


            //colorEntity = createSimpleColorEntity();

            // Get dimensions from the surface
            // Note: We'll update these when surface changes
            viewportWidth = 1020;  // Default, will be updated
            viewportHeight = 1745; // Default, will be updated
            view.setViewport(new Viewport(0, 0, viewportWidth, viewportHeight));
            Log.d(LOG_TAG, "View created with viewport " + viewportWidth + "x" + viewportHeight);

            swapChain = engine.createSwapChain(0, 0, SwapChain.CONFIG_READABLE);


            // Create asset loaders for models
            materialProvider = new MaterialProvider(engine);
            assetLoader = new AssetLoader(engine, materialProvider, EntityManager.get());
            resourceLoader = new ResourceLoader(engine);
            Log.d(LOG_TAG, "Asset loaders created successfully");

            // Set up lighting
            setupLighting();




           initializeFilamentScene();
            isInitialized = true;
            Log.d(LOG_TAG, "Filament initialized successfully");


        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing Filament: " + e.getMessage(), e);
        }
    }
    private void initializeFilamentScene() {
        initializeDisplayTexture(); //display texture setup
        //drawTestTriangleWithMaterialEntity();  //entity added to scene
        //makeSimpleTextureBufferForRenderTarget();  wait
        //createDummyTriangeWithMaterialEntity();
        //createQuadTargetEntity();
        initializeFilamentMaterials();
        view.setScene(modelScene);

    }
    private void initializeFilamentMaterials() {
        occlusionMaterialInstance = loadOrCreateMaterial("occlusion2.filamat", true);

        // Create standard material for non-occluded objects
        standardMaterialInstance = loadOrCreateMaterial("basic.filamat", true);

    }



    /**CSB best to bake in the lights for the filament renderer
     * Set up basic lighting for the scene
     */
    private void setupLighting() {
        // Create main directional light (sun) - DISABLE SHADOWS
        mainLightEntity = EntityManager.get().create();

        LightManager.Builder lightBuilder = new LightManager.Builder(LightManager.Type.DIRECTIONAL);
        lightBuilder.color(1.0f, 1.0f, 0.8f)
            .intensity(180000.0f)
            .direction(1.0f, -1.5f, -1.0f)
            .castShadows(false); // DISABLE SHADOWS

        lightBuilder.build(engine, mainLightEntity);
        modelScene.addEntity(mainLightEntity);

        // Add fill light - ALSO DISABLE SHADOWS
       /* int ambientLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.6f, 0.6f, 0.8f)
            .intensity(80000.0f)
            .direction(-0.5f, 0.5f, 1.0f)
            .castShadows(false) // ALREADY DISABLED
            .build(engine, ambientLightEntity);*/

       // modelScene.addEntity(ambientLightEntity);

        Log.d(LOG_TAG, "Lighting setup complete - shadows disabled");
    }


    private MaterialInstance loadOrCreateMaterial(String assetName, boolean isInternal) {
        Material testMaterial = null;
        MaterialInstance materialInstance = null;

        try {
            Log.d(LOG_TAG, "read from, to get material" + assetName);
            ByteBuffer materialBuffer = readAsset(assetName, isInternal);
            if (materialBuffer != null) {
                testMaterial = new Material.Builder()
                        .payload(materialBuffer, materialBuffer.capacity())
                        .build(engine);
                materialInstance = testMaterial.getDefaultInstance(); //createInstance();
                Log.d(LOG_TAG, "loadOrCreateMaterial, Created material from " + assetName);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "loadOrCreateMaterial Failed to load material: " + e.getMessage());
        }

        // If loading from file failed, create a default material
        if (materialInstance == null) {
            Log.d(LOG_TAG, "material instance is null, creating default material");
            try {
                // Use a default material builder
                testMaterial = new Material.Builder().build(engine);
                materialInstance = testMaterial.createInstance();

                // Try to set a color parameter if it exists
                if (testMaterial.hasParameter("baseColor")) {
                    materialInstance.setParameter("baseColor", 1.0f, 0.0f, 1.0f, 1.0f); //magenta material
                }

                Log.d(LOG_TAG, "loadOrCreateMaterial test triangle, Created default material");
            } catch (Exception e) {
                Log.e(LOG_TAG, "loadOrCreateMaterial Failed to create default material: " + e.getMessage());
            }
        }
        return materialInstance;
    }



    private Texture getColorTexture(){
        Texture.Builder textureBuilder = new Texture.Builder()
            .width(viewportWidth)      // Use actual width
            .height(viewportHeight)    // Use actual height
            .format(Texture.InternalFormat.RGBA8)
            .sampler(Texture.Sampler.SAMPLER_2D)
            .usage(Texture.Usage.COLOR_ATTACHMENT | Texture.Usage.SAMPLEABLE);

        return textureBuilder.build(engine);
    }

    private Texture getDepthTexture(){
        // Depth texture for virtual scene for comparison with ARCore depth
        Texture.Builder depthTextureBuilder = new Texture.Builder()
            .width(viewportWidth)
            .height(viewportHeight)
            .format(Texture.InternalFormat.DEPTH24)  // or DEPTH32F
            .sampler(Texture.Sampler.SAMPLER_2D)
            .usage(Texture.Usage.DEPTH_ATTACHMENT | Texture.Usage.SAMPLEABLE);

        return depthTextureBuilder.build(engine);
    }

    /* renderTarget with color and depth set up */
    public RenderTarget makeSimpleTextureBufferForRenderTarget(RenderTarget renderTarget, Texture targetColorTexture, Texture targetDepthTexture){
        // Use actual viewport dimensions
        viewportWidth = 1020;
        viewportHeight = 1410;//1745;

        EGLContext context = EGL14.eglGetCurrentContext();
        if (context == EGL14.EGL_NO_CONTEXT) {
            Log.e(LOG_TAG, "No GL context when creating render target");
            return null;
        }

        // Build render target with BOTH color and depth
        RenderTarget.Builder renderTargetBuilder = new RenderTarget.Builder()
            .texture(RenderTarget.AttachmentPoint.COLOR, targetColorTexture)
            .texture(RenderTarget.AttachmentPoint.DEPTH, targetDepthTexture);



        renderTarget = renderTargetBuilder.build(engine);
        return renderTarget;
    }

    private void initializeDisplayTexture() {
        try {
            // Create an external texture for display
            int[] textures = new int[2];
            GLES30.glGenTextures(2, textures, 0);
            displayTextureId = textures[0] ; // a GL texture so ARView3d can fetch it..
            depthTextureId = textures[1] ;
            // Set up the texture parameters
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);


            // Allocate texture storage once (use glTexImage2D with null data)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA8,
                viewportWidth,
                viewportHeight,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null                 // Allocate storage without uploading data
            );


            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTextureId);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);


            // Allocate texture storage once (use glTexImage2D with null data)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_DEPTH_COMPONENT32F,
                viewportWidth,
                viewportHeight,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null                 // Allocate storage without uploading data
            );


            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            Log.d(LOG_TAG, "Render target  initialized with texture ID " + displayTextureId);
            Log.d(LOG_TAG, "Render target  initialized with depth texture ID " + depthTextureId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing render target: " + e.getMessage(), e);
        }
    }


    // Add a getter for the display texture
    public int getDisplayTextureId() {
        return displayTextureId;
    }

    public int getDepthTextureId() {
        return depthTextureId;
    }

    public void updateSurfaceDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.e(LOG_TAG, "Invalid surface dimensions: " + width + "x" + height);
            return;
        }

        viewportWidth = width;
        viewportHeight = height;

        if (view != null) {
            view.setViewport(new Viewport(0, 0, width, height));
            Log.d(LOG_TAG, "Updated viewport to " + width + "x" + height);
        }
        Log.d(LOG_TAG, "view is " + view);
    }

    public void updateCameraFromARCore(float[] arcoreViewMatrix, float[] arcoreProjectionMatrix) {
        if (camera == null) return;

        // Store matrices
        System.arraycopy(arcoreViewMatrix, 0, this.viewMatrix, 0, 16);
        System.arraycopy(arcoreProjectionMatrix, 0, this.projectionMatrix, 0, 16);

        // EXACT SAME CAMERA: Use ARCore's matrices directly
        float[] cameraWorldMatrix = new float[16];
        if (Matrix.invertM(cameraWorldMatrix, 0, arcoreViewMatrix, 0)) {

            // Extract ARCore camera position and orientation EXACTLY
            float camX = cameraWorldMatrix[12];
            float camY = cameraWorldMatrix[13];
            float camZ = cameraWorldMatrix[14];

            float[] right = {cameraWorldMatrix[0], cameraWorldMatrix[1], cameraWorldMatrix[2]};
            float[] up = {cameraWorldMatrix[4], cameraWorldMatrix[5], cameraWorldMatrix[6]};
            float[] forward = {-cameraWorldMatrix[8], -cameraWorldMatrix[9], -cameraWorldMatrix[10]};

            float targetX = camX + forward[0];
            float targetY = camY + forward[1];
            float targetZ = camZ + forward[2];

            camera.lookAt(
                camX, camY, camZ,
                targetX, targetY, targetZ,
                up[0], up[1], up[2]
            );

            // Use ARCore's EXACT projection
            double[] projectionMatrixDouble = new double[16];
            for (int i = 0; i < 16; i++) {
                projectionMatrixDouble[i] = (double) arcoreProjectionMatrix[i];
            }
            camera.setCustomProjection(projectionMatrixDouble, (double) Z_NEAR, (double) Z_FAR);

            Log.d(LOG_TAG, String.format("EXACT ARCore camera: pos[%.3f,%.3f,%.3f]", camX, camY, camZ));
        }
    }

    private void normalizeVector(float[] vec) {
        float length = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
        if (length > 0.0001f) {
            vec[0] /= length;
            vec[1] /= length;
            vec[2] /= length;
        }
    }

    private float[] calculateSceneCenter() {
        float centerX = 0, centerY = 0, centerZ = 0;
        int count = 0;

        for (ARNode node : nodeAssetMap.keySet()) {
            if (node.Anchor() != null && node.Anchor().getTrackingState() == TrackingState.TRACKING) {
                float[] pos = node.Anchor().getPose().getTranslation();
                centerX += pos[0];
                centerY += pos[1];
                centerZ += pos[2];
                count++;
            }
        }

        if (count > 0) {
            return new float[]{centerX / count, centerY / count, centerZ / count};
        } else {
            // Fallback: look 1 meter in front of camera
            float[] arCoreCameraMatrix = new float[16];
            Matrix.invertM(arCoreCameraMatrix, 0, this.viewMatrix, 0);
            float[] forward = {-arCoreCameraMatrix[8], -arCoreCameraMatrix[9], -arCoreCameraMatrix[10]};
            return new float[]{
                arCoreCameraMatrix[12] + forward[0],
                arCoreCameraMatrix[13] + forward[1],
                arCoreCameraMatrix[14] + forward[2]
            };
        }
    }
    // Add this field to store current object position
    private float[] currentObjectPos = {0.0f, 0.0f, -2.0f};


    private void applyNodeTransformation(ARNode node, FilamentAsset asset, float[] arcoreViewMatrix) {
        TransformManager transformManager = engine.getTransformManager();
        int rootEntityId = asset.getRoot();
        int rootInstance = transformManager.getInstance(rootEntityId);

        // Get anchor world position
        Pose anchorPose = node.Anchor().getPose();
        float[] anchorPos = anchorPose.getTranslation();

        // Update stored object position for camera lookAt
        currentObjectPos[0] = anchorPos[0];
        currentObjectPos[1] = anchorPos[1];
        currentObjectPos[2] = anchorPos[2];

        // Position object at anchor location
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);

        modelMatrix[12] = anchorPos[0];
        modelMatrix[13] = anchorPos[1];
        modelMatrix[14] = anchorPos[2];

        // Apply scale
        float scale = 0.2f;
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        transformManager.setTransform(rootInstance, modelMatrix);

        Log.d(LOG_TAG, String.format("Object positioned at anchor: [%.3f, %.3f, %.3f]",
            anchorPos[0], anchorPos[1], anchorPos[2]));
    }

    private void createARCoreDepthTexture() {
        Texture.Builder depthTextureBuilder = new Texture.Builder()
            .width(viewportWidth)
            .height(viewportHeight)
            .format(Texture.InternalFormat.RG8) // ARCore uses 16-bit depth in RG format
            .sampler(Texture.Sampler.SAMPLER_2D)
            .usage(Texture.Usage.SAMPLEABLE);

        arDepthTexture = depthTextureBuilder.build(engine);
    }

    private void createARCoreDepthTextureWithSize(int width, int height) {
        {
            Texture.Builder depthTextureBuilder = new Texture.Builder()
                .width(width)
                .height(height)
                .format(Texture.InternalFormat.RG8) // ARCore uses 16-bit depth in RG format
                .sampler(Texture.Sampler.SAMPLER_2D)
                .usage(Texture.Usage.SAMPLEABLE);

            arDepthTexture = depthTextureBuilder.build(engine);
        }
    }


    public void updateARCoreDepth(ByteBuffer depthData, float[] uvTransform, float nearPlane, float farPlane, int depthWidth, int depthHeight) {
        if (arDepthTexture != null && depthData != null) {
            // Update the depth texture with ARCore data
            Texture.PixelBufferDescriptor descriptor = new Texture.PixelBufferDescriptor(
                depthData,
                Texture.Format.RG,
                Texture.Type.UBYTE,
                4,                    // Alignment
                0,                    // Left padding
                0,                    // Top padding
                depthWidth,        // Stride
                null,  // Explicit main looper handler
                null
            );
            Log.d(LOG_TAG, "viewport width is : " + viewportWidth + " " + viewportHeight);
            Log.d(LOG_TAG, "depth width is : " + depthWidth + " " + depthHeight);
            arDepthTexture.setImage(engine, 0, descriptor);


            Log.d(LOG_TAG, "updateARCoreDepth called - buffer size: " + depthData.remaining() +
                ", dimensions: " + depthWidth + "x" + depthHeight);





            Log.d(LOG_TAG, "Depth buffer has non-zero data: " + hasNonZeroData);

            // Store UV transform for shader
            System.arraycopy(uvTransform, 0, depthUvTransform, 0, 16);


            //applyStandardMaterial();

            // Check if we have any non-zero depth values
            depthData.rewind();

            for (int i = 0; i < Math.min(100, depthData.remaining()); i++) {
                if (depthData.get(i) != 0) {
                  hasNonZeroData = true;
                  break;
                }
            }
            if (hasNonZeroData) {
              updateOcclusionMaterialNearFarParams(nearPlane, farPlane, uvTransform);
              occlusionTexturesReady = true; // Mark textures as ready
              Log.d(LOG_TAG, "Occlusion textures are now ready");
            }
        } else {
            // Create/recreate texture with correct size
            createARCoreDepthTextureWithSize(depthWidth, depthHeight);

        }
    }

    /* used w occlusion2.filamat */
    private void updateOcclusionMaterialNearFarParams(float nearPlane, float farPlane, float[] uvTransform) {
        if (occlusionMaterialInstance != null) {
            Log.d(LOG_TAG, "Updating occlusion material");
            try {

                //


                // Float parameters
                occlusionMaterialInstance.setParameter("nearPlane", nearPlane);
                occlusionMaterialInstance.setParameter("farPlane", farPlane);


                // CORRECT: Pass float4 as 4 individual float values
                occlusionMaterialInstance.setParameter("uvTransformRow0",
                    uvTransform[0], uvTransform[1], uvTransform[2], uvTransform[3]);
                occlusionMaterialInstance.setParameter("uvTransformRow1",
                    uvTransform[4], uvTransform[5], uvTransform[6], uvTransform[7]);
                occlusionMaterialInstance.setParameter("uvTransformRow2",
                    uvTransform[8], uvTransform[9], uvTransform[10], uvTransform[11]);
                occlusionMaterialInstance.setParameter("uvTransformRow3",
                    uvTransform[12], uvTransform[13], uvTransform[14], uvTransform[15]);

                occlusionMaterialInstance.setParameter("occlusionBias", 0.01f);



                Log.d(LOG_TAG, "Material near far set successfully");

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error setting material parameters: " + e.getMessage(), e);
            }
        }
    }
    private void updateOcclusionMaterialTextureParams() {
        if (occlusionMaterialInstance != null) {
            Log.d(LOG_TAG, "Updating occlusion material");
            try {

                // For texture parameters, you need to use TextureSampler
                TextureSampler sampler = new TextureSampler(
                    TextureSampler.MinFilter.LINEAR,
                    TextureSampler.MagFilter.LINEAR,
                    TextureSampler.WrapMode.CLAMP_TO_EDGE
                );

                // Set texture with sampler
                if (arDepthTexture != null) {
                    occlusionMaterialInstance.setParameter("depthTexture", arDepthTexture, sampler);
                    Log.d(LOG_TAG, "AR depth text parameter set successfully");
                }
                if (virtualSceneDepthTexture != null) {
                    //occlusionMaterialInstance.setParameter("virtualDepthTexture", virtualSceneDepthTexture, sampler);
                    Log.d(LOG_TAG, "DEPTH tex parameter set successfully");
                }
                if (virtualSceneColorTexture != null) {
                    occlusionMaterialInstance.setParameter("colorTexture", virtualSceneColorTexture, sampler);
                    Log.d(LOG_TAG, "COLOR  tex parameter set successfully");
                }

                occlusionMaterialInstance.setParameter("debugMode", 0);
                occlusionMaterialInstance.setParameter("baseColor", 0.0f, 0.0f, 1.0f, 1.0f);

                occlusionTexturesReady = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error setting material parameters: " + e.getMessage(), e);
            }
        }
    }

    private void robustBeginFrame(){
        // Robust frame beginning with multiple attempts
        boolean frameBegun = false;
        int maxAttempts = 3;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                frameBegun = renderer.beginFrame(swapChain, 0L);
                if (frameBegun) break;

                // Short sleep between attempts to prevent tight looping
                Thread.sleep(5);
            } catch (Exception beginFrameError) {
                Log.e(LOG_TAG, "Frame begin attempt " + (attempt + 1) + " failed", beginFrameError);
            }
        }

        if (!frameBegun) {
            Log.e(LOG_TAG, "Failed to begin frame after " + maxAttempts + " attempts");
            return;
        }
    }

   /* private void renderARCoreDepthOnly() {
        // OpenGL state - no materials/shaders needed for depth-only
        GLES30.glColorMask(false, false, false, false); // Disable color writes
        GLES30.glDepthMask(true);                       // Enable depth writes
        GLES30.glDepthFunc(GLES30.GL_LESS);            // Standard depth test

        // Render ARCore geometry (planes, meshes) to depth buffer only
        renderer.render(depthView, arScene, depthOnlyTarget);

        // Reset color mask for subsequent passes
        GLES30.glColorMask(true, true, true, true);
    }*/

    private void modelSceneRenderPass() {
        try {

            view.setScene(modelScene);       // Render composite quad
            view.setRenderTarget(filamentRenderTarget);  // Final output

            // how to confirm object stays in correct position
            //camera.lookAt(0, 0, 0, 0, 0, 2, 0, 1, 0);
            robustBeginFrame();
            // Render the view
            renderer.render(view);

            Log.d(LOG_TAG, "Rendered to modelScene");

        } catch (Exception renderError) {
            Log.e(LOG_TAG, "Rendering to model scene process error", renderError);
        } finally {
            // Always end the frame
            renderer.endFrame();
            GLES30.glColorMask(true, true, true, true); // Re-enable color
        }
    }

    // Apply occlusion material to an asset
    // this causes laggy shadows if the asset has many parts to it
    private void applyOcclusionMaterialToAllAssets() {
        try {
            occlusionMaterialsApplied = true;
            RenderableManager renderableManager = engine.getRenderableManager();
            for (FilamentAsset asset : nodeAssetMap.values()) {
                for (int entityId : asset.getEntities()) {
                    if (renderableManager.hasComponent(entityId)) {
                        int instance = renderableManager.getInstance(entityId);
                        for (int i = 0; i < renderableManager.getPrimitiveCount(instance); i++) {
                            renderableManager.setMaterialInstanceAt(instance, i, occlusionMaterialInstance);
                        }
                    }
                }
                Log.d(LOG_TAG, "updated  model occlusion material for asset " + asset.getRoot());
            }
            occlusionMaterialsApplied = true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error applying occlusion material: " + e.getMessage());
        }
    }

    private void compositeSceneRenderPass() {
        // PASS 2: Render composite quad with occlusion

        // renderer renders into compositerendertarget
        // then we read those pixels and push them into displayTextureId to use in ARView3D's filamentFramebuffer
        try {


            // Apply occlusion materials once when everything is ready
            if (occlusionTexturesReady && !occlusionMaterialsApplied) {
                updateOcclusionMaterialTextureParams(); // Set up all texture parameters
                occlusionMaterialsApplied = true;
                applyOcclusionMaterialToAllAssets();     // Apply materials ONLY ONCE

                Log.d(LOG_TAG, "Occlusion materials applied - ready for depth occlusion");
            }

            // Just update uniform parameters each frame (cheap)
            if (occlusionMaterialsApplied) {
                updateOcclusionMaterialTextureParams(); // Update texture uniforms only
            }

            view.setScene(modelScene);       // DOH
            view.setRenderTarget(compositeRenderTarget);  // Final output

            robustBeginFrame();

            // Render the view
            renderer.render(view);
            // Pixel buffer preparation with diagnostic logging


            Log.d(LOG_TAG, "Rendered to target with depth texture: " + virtualSceneDepthTexture);
            Log.d(LOG_TAG, "Render target has depth attachment: " + (filamentRenderTarget != null));

        } catch (Exception renderError) {
            Log.e(LOG_TAG, "Rendering composite scene process error", renderError);
        } finally {
            // Always end the frame
            renderer.endFrame();
            handleRenderableBufferRead();
        }
    }
    public void draw(List<ARNode> nodes, float[] viewMatrix, float[] projectionMatrix) {

        if (!isInitialized) {
            Log.d(LOG_TAG, "Skipping draw - Filament not yet initialized");
            return;
        }


        if (filamentRenderTarget == null) {
            virtualSceneColorTexture = getColorTexture();
            virtualSceneDepthTexture = getDepthTexture();

            filamentRenderTarget = makeSimpleTextureBufferForRenderTarget(filamentRenderTarget, virtualSceneColorTexture, virtualSceneDepthTexture);
            Log.d(LOG_TAG, "Filament render target with texture and depth " + virtualSceneColorTexture + " " + virtualSceneDepthTexture);
        }
        if (filamentRenderTarget != null && compositeRenderTarget == null) {
            compositeColorTexture = getColorTexture();
            compositeDepthTexture = getDepthTexture();

            compositeRenderTarget =makeSimpleTextureBufferForRenderTarget(compositeRenderTarget, compositeColorTexture, compositeDepthTexture);
            Log.d(LOG_TAG, "composide render target with texture and depth " + compositeColorTexture + " " + compositeDepthTexture);
        }
        // Create quad after render target exists AND we have ARCore depth
       /* if (quadEntity == 0 && arDepthTexture != null) {
            createQuadTargetEntity();
        }*/

        try {
            // Prevent rapid, repeated frame attempts
            long currentTime = System.currentTimeMillis();

            final long MINIMUM_FRAME_INTERVAL = 8; // milliseconds

            /*throttling */
            // Track last successful frame time
            if (lastSuccessfulFrameTime > 0 &&
                    (currentTime - lastSuccessfulFrameTime) < MINIMUM_FRAME_INTERVAL) {
                Log.d(LOG_TAG, "Skipping frame to maintain consistent rate");
                return;
            }


            EGLContext currentContext = EGL14.eglGetCurrentContext();
            Log.d("GLContext", "ARFilaentRenderer Draw:: Current context: " + currentContext);
            // Update camera and view
            updateCameraFromARCore(viewMatrix, projectionMatrix);


            Log.d(LOG_TAG, "about to draw quad with" + filamentRenderTarget);
            if (nodes.size() > 0) {
                processNodes(nodes, viewMatrix, projectionMatrix);


                view.setBlendMode(View.BlendMode.TRANSLUCENT);

                Renderer.ClearOptions clearOptions = new Renderer.ClearOptions();
                clearOptions.clear = true;
                clearOptions.clearColor[0] = 1.0f; // Red
                clearOptions.clearColor[1] = 1.0f; // Green
                clearOptions.clearColor[2] = .0f; // Blue
                clearOptions.clearColor[3] = 1.0f; // Alpha
                renderer.setClearOptions(clearOptions);

                updateAnimations();

                modelSceneRenderPass(); // this renders a pink chicken if combined with the handleRenderableBufferRead()
                GLES30.glFlush();
                GLES30.glFinish();
                //handleRenderableBufferRead();
                compositeSceneRenderPass(); // this second pass should provide filament depth info.. however it renders a black rectangle, so the occlusion still isn't right
                // Only update texture every other frame


            }


        } catch (Exception catastrophicError) {
            Log.e(LOG_TAG, "Catastrophic rendering error", catastrophicError);
            if (renderer != null) {
                renderer.endFrame();
            }
        }
    }


long lastSuccessfulFrameTime = 0;
    // Add these as class fields


    private void handleRenderableBufferRead() {
        // Reuse buffer allocation
        if (pixelBuffer == null || pixelBuffer.capacity() != pixelBufferSize) {
            int sampleSize = 4 * viewportWidth * viewportHeight;
            pixelBuffer = ByteBuffer.allocateDirect(sampleSize);
            pixelBuffer.order(ByteOrder.nativeOrder());
            pixelBufferSize = sampleSize;
            Log.d(LOG_TAG, "Allocated new pixel buffer of size: " + sampleSize);
        } else {
            pixelBuffer.rewind();
        }

        // Capture context for debugging
        final EGLContext originalContext = EGL14.eglGetCurrentContext();
        Log.d(LOG_TAG, "Original context before readPixels: " + originalContext);

        Runnable callbackRunnable = new Runnable() {
            @Override
            public void run() {
                // Ensure we're on the GL thread with the correct context
                if (glSurfaceView != null) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() { //has to be here to
                            EGLContext currentGLContext = EGL14.eglGetCurrentContext();
                            Log.d(LOG_TAG, "GL Thread Context for texture update: " + currentGLContext);

                            try {
                                updateDisplayTexture(pixelBuffer);
                                lastSuccessfulFrameTime = System.currentTimeMillis();
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Error updating texture", e);
                            }
                        }
                    });
                }
            }
        };

        Texture.PixelBufferDescriptor descriptor = new Texture.PixelBufferDescriptor(
            pixelBuffer,
            Texture.Format.RGBA,
            Texture.Type.UBYTE,
            4,                    // Alignment
            0,                    // Left padding
            0,                    // Top padding
            viewportWidth,        // Stride
            new Handler(Looper.getMainLooper()),  // Explicit main looper handler
            callbackRunnable
        );

        // Read pixels with error handling
        renderer.readPixels(
            compositeRenderTarget, // vs
            //filamentRenderTarget,
            0, 0,              // x, y start
            viewportWidth,
            viewportHeight,    // width, height
            descriptor
        );
    }

    /**
     * Unified texture update method that handles both recreation and updates efficiently
     */
    private void updateDisplayTexture(ByteBuffer pixelBuffer) {
        if (displayTextureId <= 0) {
            Log.e(LOG_TAG, "Invalid texture ID");
            return;
        }

        pixelBuffer.rewind();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);

        // FAST path - don't recreate texture unless necessary
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            recreateTexture();
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
        }

        // Use glTexSubImage2D for updates (faster than glTexImage2D)
        GLES30.glTexSubImage2D(
            GLES30.GL_TEXTURE_2D, 0, 0, 0,
            viewportWidth, viewportHeight,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
            pixelBuffer
        );

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        // Remove glFinish() - it forces CPU/GPU sync and causes lag
        // GLES30.glFinish(); // Comment this out
    }

    /**
     * Recreate texture only when necessary
     */
    private void recreateTexture() {
        Log.d(LOG_TAG, "Recreating display texture");

        // Delete existing texture if it exists
        if (displayTextureId > 0) {
            int[] textures = {displayTextureId};
            GLES30.glDeleteTextures(1, textures, 0);
        }

        // Generate new texture with same ID for clarity
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        displayTextureId = textures[0];

        Log.d(LOG_TAG, "Created new texture with ID: " + displayTextureId);
    }

    /**
     * Set texture parameters - extracted for reuse
     */
    private void setTextureParameters() {
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    }

    /**
     * Process all ARNodes for rendering
     *
     * @return true if any nodes are visible and being rendered
     */
    private boolean processNodes(Collection<ARNode> nodes,
                                 float[] viewMatrix, float[] projectionMatrix) {
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }

        boolean hasVisibleNodes = false;

        for (ARNode node : nodes) {
            // Skip nodes with no model specified
            if (node.Model() == null || node.Model().isEmpty() || !node.Visible()) {
                continue;
            }

            // Check if model needs to be loaded
            if (!nodeAssetMap.containsKey(node)) {
                try {
                    loadModelForNode(node);
                    Log.i(LOG_TAG, "loaded model " + node.Model() + " with Anchor " + node.Anchor());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to load model for node: " + node, e);
                    continue;
                }
            }

            FilamentAsset asset = nodeAssetMap.get(node);
            if (asset == null) continue;

            if (asset != null) {
                RenderableManager renderableManager = engine.getRenderableManager();
                for (int entityId : asset.getEntities()) {
                    int instance = renderableManager.getInstance(entityId);
                    if (instance != 0) {
                        // Make sure entity is visible
                        renderableManager.setLayerMask(instance, 0xFF, 0xFF);
                    }
                }
            }


            // Use anchor pose if available
            if (node.Anchor() != null && node.Anchor().getTrackingState() == TrackingState.TRACKING){
                //node.Anchor().getPose().toMatrix(poseMatrix, 0);
                hasVisibleNodes = true;

                Log.i(LOG_TAG, "process anchor with pose: " + node.Anchor().getPose() + " "+ node.Anchor().getPose().getTranslation());
            } else {
                // Use node position if no anchor or tracking lost
                    Log.i(LOG_TAG, "surprise, this shouldn't happen");

                hasVisibleNodes = true;
            }
            applyNodeTransformation(node, asset, viewMatrix);
        }
        return hasVisibleNodes;
    }


    public void setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
    }

    /**
     * Update animations for all assets
     */
    private void updateAnimations() {
       // boolean animationEnabled = false;
        if (!animationEnabled) return;

        Log.i(LOG_TAG, "animating  ");
        // Update global animation time
        animationTime += 1.0f / 60.0f; // Assuming 60fps

        // Apply animation to each asset that has animations
        for (FilamentAsset asset : nodeAssetMap.values()) {
            if (asset != null && asset.getAnimator().getAnimationCount() > 0) {
                Log.i(LOG_TAG, "node has animator " );
                asset.getAnimator().applyAnimation(0, animationTime);
                asset.getAnimator().updateBoneMatrices();
            }
        }
    }


    private void applyStandardMaterial() {
        try {

            if (standardMaterialInstance != null) {
                // Set basic PBR values
                standardMaterialInstance.setParameter("baseColor", 1.0f, 0f, 1.0f, 1.0f); // magenta
                Log.d(LOG_TAG, "Applying simple material");
                // Occlusion will be set up automatically when depth data arrives

                // this wipes texture from model, so don't do that
               // renderableManager.setMaterialInstance(asset);
               /* for (int entityId : asset.getEntities()) {
                    if (renderableManager.hasComponent(entityId)) {
                        int instance = renderableManager.getInstance(entityId);
                        for (int i = 0; i < renderableManager.getPrimitiveCount(instance); i++) {
                            renderableManager.setMaterialInstanceAt(instance, i, standardMaterialInstance);
                        }
                    }
                }*/

                Log.d(LOG_TAG, "loaded basic.filamat material");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to apply simple material: " + e.getMessage(), e);
        }
    }
    /**
     * Loads a model for a node
     */
    private void loadModelForNode(ARNode node) throws IOException {
        String modelFile = node.Model();
        if (modelFile == null || modelFile.isEmpty()) {
            throw new IOException("No model specified for node");
        }

        Log.d(LOG_TAG, "Loading model: " + modelFile + " for node: " + node);

        try {
            // Read the asset file
            ByteBuffer buffer = readAsset(modelFile, false);

            // Create the asset
            FilamentAsset asset = assetLoader.createAssetFromBinary(buffer);
            if (asset == null) {
                throw new IOException("Failed to create asset from model: " + modelFile);
            }

            // Track entity IDs for this asset
            List<Integer> entityIds = new ArrayList<>();

            RenderableManager renderableManager = engine.getRenderableManager();

            // Add root and all renderable entities to scene
            int rootEntityId = asset.getRoot();
            modelScene.addEntity(rootEntityId);
            entityIds.add(rootEntityId);
            Log.d(LOG_TAG, "  root Entity ID: " + rootEntityId);
            for (int entityId : asset.getEntities()) {
                //Log.d(LOG_TAG, "  Entity ID: " + entityId);
                if (entityId != rootEntityId) {
                    modelScene.addEntity(entityId);
                    entityIds.add(entityId);
                }
            }

            setAnimationEnabled(true);

            resourceLoader.loadResources(asset);
            Log.d(LOG_TAG, "Resource loading completed successfully");

            //if (asset.getMaterialInstances().length == 0) {
                // No materials - apply simple PBR with occlusion
            applyStandardMaterial();


            // Check material instances after loading
            MaterialInstance[] materials = asset.getMaterialInstances();
            Log.d(LOG_TAG, "Model has " + materials.length + " material instances");


            // Store mappings
            nodeAssetMap.put(node, asset);
            assetEntityMap.put(asset, entityIds);

            Log.d(LOG_TAG, "Successfully loaded model " + modelFile +
                    " with " + entityIds.size() + " entities");


        } catch (IOException e) {
            Log.e(LOG_TAG, "Error loading model: " + modelFile, e);
            throw e;
        }
    }


    public void createVertexBufferObjWithData() {

        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(quadVertices.length * 4)
                .order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = vertexBuffer.asFloatBuffer();
        floatBuffer.put(quadVertices).position(0);

        ByteBuffer colorBuffer = ByteBuffer.allocateDirect(quadColors.length * 4)
                .order(ByteOrder.nativeOrder());
        FloatBuffer cFloatBuffer =colorBuffer.asFloatBuffer();
        cFloatBuffer.put(quadColors).position(0);

        vertexBufferObj = new VertexBuffer.Builder()
                .vertexCount(4)  // Assuming 4 floats per position - with alpha I guess? csb
                .bufferCount(2)
                .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                        VertexBuffer.AttributeType.FLOAT3, 0, 0)
                .attribute(VertexBuffer.VertexAttribute.COLOR, 1,
                        VertexBuffer.AttributeType.FLOAT4, 0, 1)
                .build(engine);

        vertexBufferObj.setBufferAt(engine, 0, floatBuffer);
        vertexBufferObj.setBufferAt(engine, 1, cFloatBuffer);
    }

    private void setupBuffers(){

        createVertexBufferObjWithData();

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(indices).position(0);

        ibBuilder = new IndexBuffer.Builder()
                .indexCount(6)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT);
        indexBufferObj = ibBuilder.build(engine);
        indexBufferObj.setBuffer(engine, indexBuffer);
    }


    /**
     * Read an asset into a ByteBuffer
     */
    private ByteBuffer readAsset(String assetName, boolean isInternal) throws IOException {

        try {
            java.io.InputStream is = null;
            if (isInternal){
                is = MediaUtil.getAssetsIgnoreCaseInputStream(this.formCopy, assetName);
            } else{
                is = this.formCopy.openAsset(assetName); // adds on the /storage/emultated.. prefix for assets in project
            }
            if (is == null) {
                Log.e(LOG_TAG, "asset not found" + assetName);
                throw new IOException("Asset not found: " + assetName);
            }

            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
            byteBuffer.put(buffer);
            byteBuffer.rewind();
            return byteBuffer;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error reading asset " + assetName + ": " + e.getMessage());
            throw new IOException("Failed to read asset: " + assetName, e);
        }
    }


    /**
     * Clean up resources
     */
    public void destroy() {
        if (engine != null) {
            Log.d(LOG_TAG, "Destroying Filament resources");

            // Destroy assets
            for (FilamentAsset asset : nodeAssetMap.values()) {
                assetLoader.destroyAsset(asset);
            }
            nodeAssetMap.clear();

            // Release material cache
            for (Material material : materialCache.values()) {
                engine.destroyMaterial(material);
            }
            materialCache.clear();

            // Clean up entities
            if (testTriangleEntity != 0) {
                modelScene.remove(testTriangleEntity);
                EntityManager.get().destroy(testTriangleEntity);
                testTriangleEntity = 0;
            }

            if (mainLightEntity != 0) {
                modelScene.remove(mainLightEntity);
                EntityManager.get().destroy(mainLightEntity);
                mainLightEntity = 0;
            }

            if (cameraEntity != 0) {
                EntityManager.get().destroy(cameraEntity);
                cameraEntity = 0;
            }

            // Clean up Filament resources
            engine.destroyRenderer(renderer);
            engine.destroyView(view);
            engine.destroyScene(modelScene);
            engine.destroyScene(compositeScene);
            engine.destroyCamera(camera);

            if (swapChain != null) {
                engine.destroySwapChain(swapChain);
                swapChain = null;
            }

            // Destroy resource loader and asset loader
            if (resourceLoader != null) {
                resourceLoader.destroy();
                resourceLoader = null;
            }

            if (assetLoader != null) {
                assetLoader.destroy();
                assetLoader = null;
            }

            if (materialProvider != null) {
                materialProvider.destroyMaterials();
                materialProvider.destroy();
                materialProvider = null;
            }

            // Destroy engine last
            engine.destroy();
            engine = null;
            isInitialized = false;

            Log.d(LOG_TAG, "Filament resources destroyed");
        }
    }
}