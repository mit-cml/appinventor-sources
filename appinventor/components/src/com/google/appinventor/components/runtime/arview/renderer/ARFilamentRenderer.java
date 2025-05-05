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
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;

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
public class ARFilamentRenderer {
    private static final String LOG_TAG = ARFilamentRenderer.class.getSimpleName();

    // Filament core components
    private Engine engine;
    private Renderer renderer;
    private Scene scene;
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
    private static final float Z_FAR = 100f;
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    // target for virtual scene
    Texture filamentRenderTexture;
    RenderTarget filamentRenderTarget;
    private int displayTextureId = -1;
    private int simpleFrameBuffer = -1;

    // Animation support
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

        int[] fbos = new int[1];
        GLES30.glGenFramebuffers(1, fbos, 0);
        simpleFrameBuffer = fbos[0];


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
            scene = engine.createScene();

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
        makeSimpleTextureBufferForRenderTarget();
        //createDummyTriangeWithMaterialEntity();
        createQuadTargetEntity();
        view.setScene(scene);

    }



    /**CSB best to bake in the lights for the filament renderer
     * Set up basic lighting for the scene
     */
    private void setupLighting() {
        // Create a main directional light (sun)
        mainLightEntity = EntityManager.get().create();

        LightManager.Builder lightBuilder = new LightManager.Builder(LightManager.Type.DIRECTIONAL);
        lightBuilder.color(1.0f, 1.0f, 0.8f)    // Slightly warm sunlight color
                .intensity(80000.0f)           // Bright sunlight
                .direction(0.0f, 5.0f, 0.0f)  // Coming from above and slightly in front
                .castShadows(true);

        LightManager lightManager = engine.getLightManager();
        lightBuilder.build(engine, mainLightEntity);
        scene.addEntity(mainLightEntity);

        // Add an ambient light - use SUN type for version 1.9.11 compatibility
        int ambientLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.SUN)
                .color(0.8f, 0.8f, 1.0f)       // Slightly blue ambient light (sky color)
                .intensity(80000.0f)           // Moderate intensity for ambient
                .direction(0.0f, 1.0f, 0.0f)   // Coming from below (bounce light)
                .castShadows(false)
                .build(engine, ambientLightEntity);
        scene.addEntity(ambientLightEntity);

        Log.d(LOG_TAG, "Lighting setup complete with main and ambient lights");
    }


    private MaterialInstance loadOrCreateMaterial() {
        Material testMaterial = null;
        MaterialInstance materialInstance = null;

        try {
            Log.d(LOG_TAG, "read from basic.filamat to get material");
            ByteBuffer materialBuffer = readAsset("basic.filamat", true);
            if (materialBuffer != null) {
                testMaterial = new Material.Builder()
                        .payload(materialBuffer, materialBuffer.capacity())
                        .build(engine);
                materialInstance = testMaterial.getDefaultInstance(); //createInstance();
                Log.d(LOG_TAG, "loadOrCreateMaterial, Created material from basic.filamat file");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "loadOrCreateMaterial Failed to load material: " + e.getMessage());
        }

        // If loading from file failed, create a default material
        if (materialInstance == null) {
            Log.d(LOG_TAG, "material instance is null");
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

    private int createQuadTargetEntity() {
        try {
            quadEntity = EntityManager.get().create();

            setupBuffers();

            // Basic material - simplest possible
            MaterialInstance materialInstance = loadOrCreateMaterial();
            // Build renderable
            materialInstance.setParameter("baseColor", 1.0f, 1.0f, 0.0f, 1.0f); // yellow
            RenderableManager.Builder builder = new RenderableManager.Builder(1);
            builder.boundingBox(new Box(-1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -0.4f))
                    .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBufferObj, indexBufferObj)
                    .material(0, materialInstance)
                    .culling(false)
                    .receiveShadows(false)
                    .priority(1001)
                    .castShadows(false)
                    .build(engine, quadEntity);


            Log.e(LOG_TAG, "created quad entity" );
            // Add to scene
            scene.addEntity(quadEntity);

            return quadEntity;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating simple entity: " + e.getMessage(), e);
            return 0;
        }
    }

    // Do this during initialization
    public void createDummyTriangeWithMaterialEntity() {
        try {
            // Create a simple entity
            EntityManager entityManager = EntityManager.get();
            simpleTriangleEntity = entityManager.create();

            MaterialInstance materialInstance = loadOrCreateMaterial();

            // Explicitly set a bright, contrasting color
            materialInstance.setParameter("baseColor", 0.0f, 1.0f, 0.0f, 1.0f); // Bright green
            Log.d(LOG_TAG, "Material color set to bright green");

            // Adjusted vertices to cover more of the screen vertically
            float[] vertices = {
                    0.0f, 1.0f, -0.5f,    // top (higher up)
                    -1.0f, -1.0f, -0.5f,  // bottom left (wider)
                    1.0f, -1.0f, -0.5f    // bottom right (wider)
            };


            short[] indices = { 0, 1, 2 };

            // Create vertex buffer
            VertexBuffer.Builder vbb = new VertexBuffer.Builder()
                    .vertexCount(3)
                    .bufferCount(1)
                    .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                            VertexBuffer.AttributeType.FLOAT3, 0, 12);

            vertexBufferObj = vbb.build(engine);

            // Fill the vertex buffer
            ByteBuffer vertexData = ByteBuffer.allocateDirect(vertices.length * 4)
                    .order(ByteOrder.nativeOrder());
            FloatBuffer floatBuffer = vertexData.asFloatBuffer();
            floatBuffer.put(vertices);
            floatBuffer.flip();

            vertexBufferObj.setBufferAt(engine, 0, vertexData);

            // Create index buffer
            IndexBuffer.Builder ibb = new IndexBuffer.Builder()
                    .indexCount(3)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT);
            indexBufferObj = ibb.build(engine);

            // Fill the index buffer
            ByteBuffer indexData = ByteBuffer.allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder());
            ShortBuffer shortBuffer = indexData.asShortBuffer();
            shortBuffer.put(indices);
            shortBuffer.flip();

            indexBufferObj.setBuffer(engine, indexData);

            // Add renderable component to the entity
            RenderableManager.Builder builder = new RenderableManager.Builder(1);
            builder
                    .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBufferObj, indexBufferObj)
                    .material(0, materialInstance)
                    .boundingBox(new Box(-1.0f, -1.0f, -3.0f, 1.0f, 1.0f, -1.0f))
                    .culling(false)       // Disable culling to ensure visibility
                    .receiveShadows(false)
                    .castShadows(false)
                    .priority(1000)        // High priority to ensure it's drawn
                    .build(engine, simpleTriangleEntity);

            Log.d(LOG_TAG, "Creating dummy triangle entity");
            Log.d(LOG_TAG, "Triangle vertices: " +
                    Arrays.toString(vertices));

            // Add entity to scene
            scene.addEntity(simpleTriangleEntity);

            Log.d(LOG_TAG, "Dummy triangle added to scene");
            Log.d(LOG_TAG, "Scene renderable count: " + scene.getRenderableCount());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating dummy triangle", e);
        }
    }

    public void makeSimpleTextureBufferForRenderTarget(){
        // Use actual viewport dimensions
        viewportWidth = 1020;
        viewportHeight = 1745;

        Texture.Builder textureBuilder = new Texture.Builder()
                .width(viewportWidth)      // Use actual width
                .height(viewportHeight)    // Use actual height
                .format(Texture.InternalFormat.RGBA8)
                .sampler(Texture.Sampler.SAMPLER_2D)
                .usage(Texture.Usage.COLOR_ATTACHMENT | Texture.Usage.SAMPLEABLE);

        Texture colorTexture = textureBuilder.build(engine);

        RenderTarget.Builder renderTargetBuilder = new RenderTarget.Builder()
                .texture(RenderTarget.AttachmentPoint.COLOR, colorTexture);

        filamentRenderTarget = renderTargetBuilder.build(engine);
        view.setRenderTarget(filamentRenderTarget);
    }

    private void initializeDisplayTexture() {
        try {
            // Create an external texture for display
            int[] textures = new int[1];
            GLES30.glGenTextures(1, textures, 0);
            displayTextureId = textures[0]; // a GL texture so ARView3d can fetch it..

            // Set up the texture parameters
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);


            Log.d(LOG_TAG, "Render target stream initialized with texture ID " + displayTextureId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing render target: " + e.getMessage(), e);
        }
    }


    // Add a getter for the display texture
    public int getDisplayTextureId() {
        return displayTextureId;
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
                    0.01f,                  // near plane (closer for AR)
                    100.0f,                 // far plane
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



    public void draw(List<ARNode> nodes, float[] viewMatrix, float[] projectionMatrix) {

        if (!isInitialized) {
            Log.d(LOG_TAG, "Skipping draw - Filament not yet initialized");
            return;
        }

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
            if (nodes.size() > 0){
               processNodes(nodes, viewMatrix, projectionMatrix);

            }

            view.setScene(scene);
            view.setRenderTarget(filamentRenderTarget);
            view.setBlendMode(View.BlendMode.TRANSLUCENT);

            Renderer.ClearOptions clearOptions = new Renderer.ClearOptions();
            clearOptions.clear = true;
            clearOptions.clearColor[0] = 1.0f; // Red
            clearOptions.clearColor[1] = 1.0f; // Green
            clearOptions.clearColor[2] = .0f; // Blue
            clearOptions.clearColor[3] = 1.0f; // Alpha
            renderer.setClearOptions(clearOptions);

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

            updateAnimations();
            // renderer renders into filamentRenderTarget
            // then we read those pixels and push them into displayTextureId
            try {
                // Render the view
                renderer.render(view);
                // Pixel buffer preparation with diagnostic logging
                handleRenderableBufferRead();


            } catch (Exception renderError) {
                Log.e(LOG_TAG, "Rendering process error", renderError);
            } finally {
                // Always end the frame
                renderer.endFrame();
            }

        } catch (Exception catastrophicError) {
            Log.e(LOG_TAG, "Catastrophic rendering error", catastrophicError);
            if (renderer != null) {
                renderer.endFrame();
            }
        }
    }


long lastSuccessfulFrameTime = 0;

private void handleRenderableBufferRead() {
    // Capture the current context before read pixels
    final EGLContext originalContext = EGL14.eglGetCurrentContext();
    Log.d(LOG_TAG, "Original context before readPixels: " + originalContext);

    int sampleSize = 4 * viewportWidth * viewportHeight;
    final ByteBuffer sampleBuffer = ByteBuffer.allocateDirect(sampleSize);
    sampleBuffer.order(ByteOrder.nativeOrder());

    Runnable callbackRunnable = new Runnable() {
        @Override
        public void run() {
            // Ensure we're on the GL thread with the correct context
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    EGLContext currentGLContext = EGL14.eglGetCurrentContext();
                    Log.d(LOG_TAG, "GL Thread Context for texture update: " + currentGLContext);

                    try {
                        // Process sample and ensure texture is updated in this context
                        processSmallSample(sampleBuffer, viewportWidth * viewportHeight);

                        // Explicitly recreate the texture in the current context
                        recreateDisplayTexture(sampleBuffer);

                        lastSuccessfulFrameTime = System.currentTimeMillis();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error updating texture", e);
                    }
                }

                private void recreateDisplayTexture(ByteBuffer pixelBuffer) {
                    // Delete existing texture if it exists
                    if (displayTextureId > 0) {
                        int[] textures = {displayTextureId};
                        GLES30.glDeleteTextures(1, textures, 0);
                    }

                    // Generate new texture
                    int[] textures = new int[1];
                    GLES30.glGenTextures(1, textures, 0);
                    displayTextureId = textures[0];

                    // Bind and configure the new texture
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);

                    // Set texture parameters
                    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                    // Upload pixel data
                    GLES30.glTexImage2D(
                            GLES30.GL_TEXTURE_2D,
                            0,
                            GLES30.GL_RGBA,
                            viewportWidth,
                            viewportHeight,
                            0,
                            GLES30.GL_RGBA,
                            GLES30.GL_UNSIGNED_BYTE,
                            pixelBuffer
                    );

                    // Check for OpenGL errors
                    int error = GLES30.glGetError();
                    if (error != GLES30.GL_NO_ERROR) {
                        Log.e(LOG_TAG, "OpenGL texture creation error: 0x" + Integer.toHexString(error));
                    }

                    Log.d(LOG_TAG, "Recreated display texture with ID: " + displayTextureId);
                }
            });
        }
    };

    // Rest of the readPixels configuration remains the same
    Texture.PixelBufferDescriptor descriptor = new Texture.PixelBufferDescriptor(
            sampleBuffer,
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
                filamentRenderTarget,
                0, 0,              // x, y start
                viewportWidth,
                viewportHeight,    // width, height
                descriptor
        );
    }

    private void processSmallSample(final ByteBuffer sampleBuffer, final int pixelCount) {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> processSmallSample(sampleBuffer, pixelCount));
            return;
        }
        // Now we should be on the UI thread
        Log.d(LOG_TAG, "Processing smallSample on UI thread");


        sampleBuffer.rewind();

        // Bind the texture and push data to the target displayTexture
        if (displayTextureId > 0) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);

            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);


            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA8,
                    viewportWidth,
                    viewportHeight,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    sampleBuffer
            );

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            // Check for errors
            int error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "OpenGL error: 0x" + Integer.toHexString(error));
            } else {
                Log.d(LOG_TAG, "Successfully updated texture ID: " + displayTextureId);
            }
        }
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
            scene.addEntity(rootEntityId);
            entityIds.add(rootEntityId);
            Log.d(LOG_TAG, "  root Entity ID: " + rootEntityId);
            for (int entityId : asset.getEntities()) {
                //Log.d(LOG_TAG, "  Entity ID: " + entityId);
                if (entityId != rootEntityId) {
                    scene.addEntity(entityId);
                    entityIds.add(entityId);
                }
            }

            setAnimationEnabled(true);

            resourceLoader.loadResources(asset);
            Log.d(LOG_TAG, "Resource loading completed successfully");

            // Only apply a default material if the model doesn't have its own
            if (asset.getMaterialInstances().length == 0) {
                Log.d(LOG_TAG, "model doesn't have materials ");
                MaterialInstance materialInstance = loadOrCreateMaterial();
                materialInstance.setParameter("baseColor", 0.0f, 1.0f, 1.0f, 0.5f);

                // Apply to all entities in the model that need materials
                for (int entityId : asset.getEntities()) {
                    if (renderableManager.hasComponent(entityId)) {
                        int instance = renderableManager.getInstance(entityId);
                        for (int i = 0; i < renderableManager.getPrimitiveCount(instance); i++) {
                            renderableManager.setMaterialInstanceAt(instance, i, materialInstance);
                        }
                        renderableManager.setCulling(instance, false);
                    }
                }
            }

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
                .vertexCount(3)  // Assuming 3 floats per position
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
                is = this.formCopy.openAsset(assetName);
            }
            if (is == null) {
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
                scene.remove(testTriangleEntity);
                EntityManager.get().destroy(testTriangleEntity);
                testTriangleEntity = 0;
            }

            if (mainLightEntity != 0) {
                scene.remove(mainLightEntity);
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
            engine.destroyScene(scene);
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