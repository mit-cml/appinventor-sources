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

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 40f;
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
        // Create main directional light (sun)
        mainLightEntity = EntityManager.get().create();

        LightManager.Builder lightBuilder = new LightManager.Builder(LightManager.Type.DIRECTIONAL);
        lightBuilder.color(1.0f, 1.0f, 0.8f)
            .intensity(180000.0f)
            .direction(1.0f, -1.5f, -1.0f);  // ✅ More angled from above-front-right

        lightBuilder.build(engine, mainLightEntity);
        modelScene.addEntity(mainLightEntity);

        // Add fill light
        int ambientLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.6f, 0.6f, 0.8f)       // Softer blue fill
            .intensity(80000.0f)            // ✅ Lower intensity for fill
            .direction(-0.5f, 0.5f, 1.0f)  // ✅ From below-back-left (opposite side)
            .castShadows(false)
            .build(engine, ambientLightEntity);

        modelScene.addEntity(ambientLightEntity);

        Log.d(LOG_TAG, "Lighting setup complete with main and ambient lights");
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

    /**
     * Updates the camera position and projection based on ARCore camera data
     */
    public void updateCameraFromARCore(float[] viewMatrix, float[] projectionMatrix) {
        if (camera == null) {
            Log.e(LOG_TAG, "Camera not initialized in updateCameraFromARCore");
            return;
        }
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            Log.e(LOG_TAG, "Invalid viewport dimensions: " + viewportWidth + "x" + viewportHeight);
            return;
        }

        try {
            //invert view matrix to get the camera's world position
            float[] cameraModelMatrix = new float[16];
            Matrix.invertM(cameraModelMatrix, 0, viewMatrix, 0);

            // Set camera transform
            TransformManager transformManager = engine.getTransformManager();
            int cameraInstance = transformManager.getInstance(cameraEntity);
            transformManager.setTransform(cameraInstance, cameraModelMatrix);

            double fovY = 2.0 * Math.toDegrees(Math.atan(1.0 / projectionMatrix[5]));

            camera.setProjection(
                    (double) 120.0,                   // vertical field of view
                    (double) viewportWidth / viewportHeight,  // aspect ratio
                    Z_NEAR,              // near plane (closer for AR)
                    Z_FAR,                // far plane SHOULD MATCH ARView3d!!
                    Camera.Fov.VERTICAL
            );

            // Log camera position and orientation
            float[] position = new float[3];
            camera.getPosition(position);
            Log.d(LOG_TAG, "Camera position: " + Arrays.toString(position));

            // Log camera projection details
            double[] projMatrix = new double[16];
            camera.getProjectionMatrix(projMatrix);
            Log.d(LOG_TAG, "Projection matrix: " + Arrays.toString(projMatrix));

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error updating camera: " + e.getMessage(), e);
        }
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

            // Check if we have any non-zero depth values
            depthData.rewind();
            boolean hasNonZeroData = false;
            for (int i = 0; i < Math.min(100, depthData.remaining()); i++) {
                if (depthData.get(i) != 0) {
                    hasNonZeroData = true;
                    break;
                }
            }
            depthData.rewind();

            Log.d(LOG_TAG, "Depth buffer has non-zero data: " + hasNonZeroData);

            // Store UV transform for shader
            System.arraycopy(uvTransform, 0, depthUvTransform, 0, 16);

            // Update material parameters if occlusion material is loaded
            if (hasNonZeroData){
                updateOcclusionMaterialNearFarParams(nearPlane, farPlane, uvTransform);
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

                occlusionMaterialInstance.setParameter("debugMode", 26);
                occlusionMaterialInstance.setParameter("baseColor", 0.0f, 0.0f, 1.0f, 1.0f);


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
    private void applyOcclusionMaterialToAllAssets() {
        try {


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

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error applying occlusion material: " + e.getMessage());
        }
    }


    private void mergeARCoreDepthUsingObjects() {

        try {
            // Apply depth comparison material to all objects
            applyOcclusionMaterialToAllAssets();

            // Configure depth testing for merging
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthFunc(GLES30.GL_ALWAYS);  // Always run fragment shader
            GLES30.glDepthMask(true);              // Allow depth writes
            GLES30.glColorMask(false, false, false, false); // No color output

            robustBeginFrame();
            // Render the view
            renderer.render(view);

            Log.d(LOG_TAG, "Rendered to depth scene");

        } catch (Exception renderError) {
            Log.e(LOG_TAG, "Rendering to model scene process error", renderError);
        } finally {
            // Always end the frame
            renderer.endFrame();
            GLES30.glColorMask(true, true, true, true); // Re-enable color
        }
    }
    private void compositeSceneRenderPass() {
        // PASS 2: Render composite quad with occlusion

        // renderer renders into compositerendertarget
        // then we read those pixels and push them into displayTextureId to use in ARView3D's filamentFramebuffer
        try {


            updateOcclusionMaterialTextureParams();
            applyOcclusionMaterialToAllAssets(); // apparently this is necessary?

            view.setScene(modelScene);       // DOH
            view.setRenderTarget(compositeRenderTarget);  // Final output

           /* GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthFunc(GLES30.GL_LESS);
            GLES30.glDepthMask(true); // Write depth for objects that pass occlusion test
*/
            robustBeginFrame();

            // Render the view
            renderer.render(view);
            // Pixel buffer preparation with diagnostic logging
            handleRenderableBufferRead();

            Log.d(LOG_TAG, "Rendered to target with depth texture: " + virtualSceneDepthTexture);
            Log.d(LOG_TAG, "Render target has depth attachment: " + (filamentRenderTarget != null));

        } catch (Exception renderError) {
            Log.e(LOG_TAG, "Rendering composite scene process error", renderError);
        } finally {
            // Always end the frame
            renderer.endFrame();
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

            final long MINIMUM_FRAME_INTERVAL = 16; // milliseconds

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
                        public void run() {
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

        // Check if we need to recreate the texture (only when necessary)
        boolean needsRecreation = false;

        // Test if texture is still valid by trying to bind it
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.d(LOG_TAG, "Texture needs recreation due to GL error: 0x" + Integer.toHexString(error));
            needsRecreation = true;
        }

        if (needsRecreation) {
            recreateTexture();
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
        }

        setTextureParameters();

        // Upload pixel data
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            viewportWidth,
            viewportHeight,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            pixelBuffer
        );

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glFlush();  // Ensure commands complete
        GLES30.glFinish(); // Wait for completion
        // Check for errors
        error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.e(LOG_TAG, "OpenGL texture update error: 0x" + Integer.toHexString(error));
        } else {
            Log.d(LOG_TAG, "Successfully updated texture ID: " + displayTextureId);
        }
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
    /**
     * Apply node-specific transformations
     */
    private void applyNodeTransformation(ARNode node, FilamentAsset asset, float[] viewMatrix) {
        TransformManager transformManager = engine.getTransformManager();
        int rootEntityId = asset.getRoot();
        int rootInstance = transformManager.getInstance(rootEntityId);

        // Create a model matrix with identity (no rotation)
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);

        // Apply a fixed rotation if needed to orient the model correctly
        // Uncomment and adjust this if needed:
        // Matrix.rotateM(modelMatrix, 0, 180, 0, 1, 0);
        Matrix.rotateM(modelMatrix, 0, 0, 0, 1, 0);  // No rotation (identity)
        // Get the anchor's position in world space - this stays fixed
        Pose anchorPose = node.Anchor().getPose();
        float[] position = anchorPose.getTranslation();

        // Set the world position from the anchor
        modelMatrix[12] = position[0];
        modelMatrix[13] = position[1];
        modelMatrix[14] = position[2];



        // Apply scale
        float scale = .2f; // node.Scale();
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

        transformManager.setTransform(rootInstance, modelMatrix);
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


    private void applyStandardMaterial(FilamentAsset asset) {
        try {

            if (standardMaterialInstance != null) {
                // Set basic PBR values
                standardMaterialInstance.setParameter("baseColor", 1.0f, 0f, 1.0f, 1.0f); // magenta
                Log.d(LOG_TAG, "Applying simple material");
                // Occlusion will be set up automatically when depth data arrives

                // Apply to all parts of the model
               // RenderableManager renderableManager = engine.getRenderableManager();
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
            applyStandardMaterial(asset);


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