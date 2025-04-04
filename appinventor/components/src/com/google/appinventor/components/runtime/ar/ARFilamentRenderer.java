package com.google.appinventor.components.runtime;

import android.view.SurfaceView;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.Choreographer;
import android.opengl.GLES30;  // Import all GLES30 methods statically


import android.opengl.Matrix;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.Fence;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.LightManager;
import com.google.android.filament.Material;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.RenderTarget;
import com.google.android.filament.Scene;
import com.google.android.filament.Stream;
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

import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ARViewRender;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
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

    // Framebuffer for virtual scene
    private ARFrameBuffer virtualSceneFramebuffer;
    Texture filamentRenderTexture;
    RenderTarget filamentRenderTarget;
    private int displayTextureId = -1;
    private int framebufferId ;

    private ByteBuffer pixelBuffer = null;
    private int pixelBufferSize = 0;

    private ComponentContainer contextContainer = null;
    /**
     * Create a new ARFilamentRenderer
     */
    public ARFilamentRenderer(ComponentContainer container) throws IOException {
        this.contextContainer = container;
        formCopy = container.$form();
        Log.d(LOG_TAG, "ARFilamentRenderer constructor called");
    }




    /**
     * Initialize Filament components
     */
    public void initialize(ARViewRenderer arVR) {
        if (isInitialized) {
            Log.d(LOG_TAG, "Filament already initialized, skipping");
            return;
        }

        try {
            Log.d(LOG_TAG, "Initializing Filament");

            // Load Filament libraries
            initializeFilament();

            // Create Filament engine
            engine = Engine.create();
            Log.d(LOG_TAG, "Engine created successfully");

            renderer = engine.createRenderer();
            // In ARFilamentRenderer initialization
            engine = Engine.create(arVR.getEGLContext());

            // Create camera entity
            cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);

            // Create view
            view = engine.createView();
            view.setScene(scene);
            view.setCamera(camera);

            // Get dimensions from the surface
            // Note: We'll update these when surface changes
            viewportWidth = 1080;  // Default, will be updated
            viewportHeight = 1920; // Default, will be updated
            view.setViewport(new Viewport(0, 0, viewportWidth, viewportHeight));
            Log.d(LOG_TAG, "View created with viewport " + viewportWidth + "x" + viewportHeight);

            //swapChain = engine.createSwapChain(0, 0, SwapChain.CONFIG_READABLE);

            swapChain = engine.createSwapChain(surface);


            // Create asset loaders for models
            materialProvider = new MaterialProvider(engine);
            assetLoader = new AssetLoader(engine, materialProvider, EntityManager.get());
            resourceLoader = new ResourceLoader(engine);
            Log.d(LOG_TAG, "Asset loaders created successfully");

            // Set up lighting
            setupLighting();

            isInitialized = true;
            Log.d(LOG_TAG, "Filament initialized successfully");

            // Draw a test triangle to scene
            drawTestTriangleEntity();

            initializeRenderTargetStream();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing Filament: " + e.getMessage(), e);
        }
    }

    /**
     * Load Filament libraries - ensures proper loading of native libraries
     */
    private void initializeFilament() {
        try {
            System.loadLibrary("filament-jni");
            System.loadLibrary("gltfio-jni");
            Log.d(LOG_TAG, "Filament libraries loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(LOG_TAG, "Failed to load Filament libraries", e);
            throw e;
        }
    }

    /**
     * Set up basic lighting for the scene
     */
    private void setupLighting() {
        // Create a main directional light (sun)
        mainLightEntity = EntityManager.get().create();

        LightManager.Builder lightBuilder = new LightManager.Builder(LightManager.Type.DIRECTIONAL);
        lightBuilder.color(1.0f, 1.0f, 0.8f)    // Slightly warm sunlight color
                .intensity(100000.0f)           // Bright sunlight
                .direction(0.0f, -1.0f, -0.2f)  // Coming from above and slightly in front
                .castShadows(true);

        LightManager lightManager = engine.getLightManager();
        lightBuilder.build(engine, mainLightEntity);
        scene.addEntity(mainLightEntity);

        // Add an ambient light - use SUN type for version 1.9.11 compatibility
        int ambientLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.SUN)
                .color(0.8f, 0.8f, 1.0f)       // Slightly blue ambient light (sky color)
                .intensity(20000.0f)           // Moderate intensity for ambient
                .direction(0.0f, 1.0f, 0.0f)   // Coming from below (bounce light)
                .castShadows(false)
                .build(engine, ambientLightEntity);
        scene.addEntity(ambientLightEntity);

        Log.d(LOG_TAG, "Lighting setup complete with main and ambient lights");
    }

    /**
     * Create a simple test triangle to verify rendering works
     */
    public void drawTestTriangleEntity() {
        try {
            Log.d(LOG_TAG, "Creating test triangle entity");

            // Create a new entity
            if (testTriangleEntity == 0) {
                testTriangleEntity = EntityManager.get().create();

                Log.d(LOG_TAG, "Test entity created: " + testTriangleEntity);

                // Define vertices for a visible colored triangle
                float[] triangleVertices = {
                        0.0f, 0.5f, -1.5f,  // top
                        -0.5f, -0.5f, -1.5f,  // bottom left
                        0.5f, -0.5f, -1.5f   // bottom right
                };

                // Define bright colors for visibility
                float[] triangleColors = {
                        1.0f, 0.0f, 0.0f, 1.0f,  // bright red
                        0.0f, 1.0f, 0.0f, 1.0f,  // bright green
                        0.0f, 0.0f, 1.0f, 1.0f   // bright blue
                };

                // Create and setup vertex buffer
                FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(triangleVertices.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                vertexBuffer.put(triangleVertices).position(0);

                FloatBuffer colorBuffer = ByteBuffer.allocateDirect(triangleColors.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                colorBuffer.put(triangleColors).position(0);

                // Create Filament vertex buffer
                VertexBuffer.Builder vbBuilder = new VertexBuffer.Builder()
                        .vertexCount(3)
                        .bufferCount(2)
                        .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                                VertexBuffer.AttributeType.FLOAT3, 0, 0)
                        .attribute(VertexBuffer.VertexAttribute.COLOR, 1,
                                VertexBuffer.AttributeType.FLOAT4, 0, 0);

                VertexBuffer vertexBufferObj = vbBuilder.build(engine);
                vertexBufferObj.setBufferAt(engine, 0, vertexBuffer);
                vertexBufferObj.setBufferAt(engine, 1, colorBuffer);

                // Create index buffer
                short[] indices = {0, 1, 2};
                ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                        .order(ByteOrder.nativeOrder())
                        .asShortBuffer();
                indexBuffer.put(indices).position(0);

                IndexBuffer.Builder ibBuilder = new IndexBuffer.Builder()
                        .indexCount(3)
                        .bufferType(IndexBuffer.Builder.IndexType.USHORT);
                IndexBuffer indexBufferObj = ibBuilder.build(engine);
                indexBufferObj.setBuffer(engine, indexBuffer);

                // We'll attempt to create a simple material
                Material testMaterial = null;
                MaterialInstance materialInstance = null;

                try {
                    // Try to load a simple material from file
                    ByteBuffer materialBuffer = readAsset("basic.filamat");
                    if (materialBuffer != null) {
                        testMaterial = new Material.Builder()
                                .payload(materialBuffer, materialBuffer.capacity())
                                .build(engine);
                        materialInstance = testMaterial.createInstance();
                        Log.d(LOG_TAG, "Created material from file");
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to load material: " + e.getMessage());
                }

                // If loading from file failed, create a default material
                if (materialInstance == null) {
                    try {
                        // Use a default material builder
                        testMaterial = new Material.Builder().build(engine);
                        materialInstance = testMaterial.createInstance();

                        // Try to set a color parameter if it exists
                        if (testMaterial.hasParameter("baseColor")) {
                            materialInstance.setParameter("baseColor", 1.0f, 1.0f, 1.0f, 1.0f);
                        }

                        Log.d(LOG_TAG, "Created default material");
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Failed to create default material: " + e.getMessage());
                        return;
                    }
                }

                // Build renderable with larger bounding box for visibility
                RenderableManager.Builder builder = new RenderableManager.Builder(1);
                builder.boundingBox(new Box(-0.5f, -0.5f, -2.0f, 0.5f, 0.5f, -1.0f))
                        .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBufferObj, indexBufferObj)
                        .material(0, materialInstance)
                        .culling(false)       // Disable culling to ensure visibility
                        .receiveShadows(false)
                        .castShadows(false)
                        .priority(1000)        // High priority to ensure it's drawn
                        .build(engine, testTriangleEntity);

                // Add to scene
                scene.addEntity(testTriangleEntity);
                Log.d(LOG_TAG, "Added test triangle entity to scene");
            }

            // Position the triangle in front of the camera
            TransformManager transformManager = engine.getTransformManager();
            transformManager.create(testTriangleEntity);

            float[] modelMatrix = new float[16];
            Matrix.setIdentityM(modelMatrix, 0);

            // Place it directly in front of where the camera starts
            Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -0.5f);

            transformManager.setTransform(transformManager.getInstance(testTriangleEntity), modelMatrix);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in drawTestTriangleEntity: " + e.getMessage(), e);
        }
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
            // ARCore's viewMatrix is already a view matrix (camera pose inverse)
            // We need to invert it to get the camera's world position
            float[] cameraModelMatrix = new float[16];
            Matrix.invertM(cameraModelMatrix, 0, viewMatrix, 0);

            // Set camera transform
            TransformManager transformManager = engine.getTransformManager();
            int cameraInstance = transformManager.getInstance(cameraEntity);
            transformManager.setTransform(cameraInstance, cameraModelMatrix);

            // Extract FOV from ARCore's projection matrix
            double fovY = 2.0 * Math.toDegrees(Math.atan(1.0 / projectionMatrix[5]));

            // Set the camera projection
            camera.setProjection(
                    fovY,                   // vertical field of view
                    (double)viewportWidth / viewportHeight,  // aspect ratio
                    0.01f,                  // near plane (closer for AR)
                    100.0f,                 // far plane
                    Camera.Fov.VERTICAL
            );
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error updating camera: " + e.getMessage(), e);
        }
    }

    public void draw(List<ARNode> nodes, float[] viewMatrix, float[] projectionMatrix) {
        // Update camera matrices
        updateCameraFromARCore(viewMatrix, projectionMatrix);

        // Draw the test triangle
        //drawTestTriangleEntity();

        // Render with Filament directly to its swapchain
        if (renderer.beginFrame(swapChain, 0L)) {
            renderer.render(view);
            renderer.endFrame();
            Log.d(LOG_TAG, "Filament rendered frame successfully");
        } else {
            Log.e(LOG_TAG, "Filament beginFrame returned false");
        }
    }

    private void initializeRenderTargetStream() {
        try {
            // Create an external texture for display
            int[] textures = new int[1];
            GLES30.glGenTextures(1, textures, 0);
            displayTextureId = textures[0];

            // Set up the texture parameters
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, viewportWidth, viewportHeight,
                    0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            // Create a Stream to this texture
            Stream stream = new Stream.Builder()
                    .stream(displayTextureId)
                    .build(engine);

            // Create a Filament texture
            Texture streamTexture = new Texture.Builder()
                    .sampler(Texture.Sampler.SAMPLER_2D)
                    .format(Texture.InternalFormat.RGBA8)
                    .usage(Texture.Usage.COLOR_ATTACHMENT | Texture.Usage.SAMPLEABLE)
                    .width(viewportWidth)
                    .height(viewportHeight)
                    .build(engine);

            // Attach the stream
            streamTexture.setExternalStream(engine, stream);

            // Use this texture as your render target
            RenderTarget renderTarget = new RenderTarget.Builder()
                    .texture(RenderTarget.AttachmentPoint.COLOR, streamTexture)
                    .build(engine);

            // Set the render target on your view
            view.setRenderTarget(renderTarget);

            Log.d(LOG_TAG, "Render target initialized with texture ID " + displayTextureId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing render target: " + e.getMessage(), e);
        }
    }

    // Add a getter for the display texture
    public int getDisplayTextureId() {
        return displayTextureId;
    }
    /**
     * Read an asset into a ByteBuffer
     */
    private ByteBuffer readAsset(String assetName) throws IOException {
        try (java.io.InputStream is = MediaUtil.getAssetsIgnoreCaseInputStream(this.formCopy, assetName)) {
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
     * Creates a loader for 3D model assets
     */
    public void loadAsset(ARNode node, String assetPath) {
        try {
            // Read the asset file
            ByteBuffer buffer = readAsset(assetPath);

            // Use the appropriate loading method based on file extension
            FilamentAsset asset = null;

            if (assetPath.toLowerCase().endsWith(".glb") || assetPath.toLowerCase().endsWith(".gltf")) {
                // Load glTF/GLB model
                asset = assetLoader.createAssetFromBinary(buffer);
            } else {
                Log.e(LOG_TAG, "Unsupported asset type: " + assetPath);
                return;
            }

            if (asset == null) {
                Log.e(LOG_TAG, "Failed to load asset: " + assetPath);
                return;
            }

            // Initialize the resource loader and load resources
            resourceLoader.loadResources(asset);

            // Cache the asset for this node
            nodeAssetMap.put(node, asset);

            // Add all entities to the scene
            EntityManager entityManager = EntityManager.get();
            for (int entity : asset.getEntities()) {
                scene.addEntity(entity);
            }

            Log.d(LOG_TAG, "Loaded asset: " + assetPath);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load asset " + assetPath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Clean up resources
     */
    public void destroy() {
        if (engine != null) {
            Log.d(LOG_TAG, "Destroying Filament resources");

            // Clean up virtual scene framebuffer
            if (virtualSceneFramebuffer != null) {
                virtualSceneFramebuffer.release();
                virtualSceneFramebuffer = null;
            }

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