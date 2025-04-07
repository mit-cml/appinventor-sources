package com.google.appinventor.components.runtime;

import android.view.SurfaceView;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.Choreographer;
import android.opengl.GLES30;  // Import all GLES30 methods statically
import android.os.Looper;
import android.os.Handler;

import android.opengl.Matrix;
import android.util.Log;

import com.google.android.filament.Box;
import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.Fence;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.Texture.PixelBufferDescriptor;
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
import java.util.Arrays;
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
    private int simpleFrameBuffer = -1;

    private ByteBuffer pixelBuffer = null;
    private int pixelBufferSize = 0;
    private int colorEntity = 0;


// In your initialization
// ...

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
    public void initialize() {
        if (isInitialized) {
            Log.d(LOG_TAG, "Filament already initialized, skipping");
            return;
        }

        int[] fbos = new int[1];
        GLES30.glGenFramebuffers(1, fbos, 0);
        simpleFrameBuffer = fbos[0];

        try {
            Log.d(LOG_TAG, "Initializing Filament");

            // Load Filament libraries
            initializeFilament();

            // Create Filament engine
            engine = Engine.create();
            Log.d(LOG_TAG, "Engine created successfully");

            renderer = engine.createRenderer();
            scene = engine.createScene();

            // Create camera entity
            cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);

            // Create view
            view = engine.createView();

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
            //setupLighting();

            isInitialized = true;
            Log.d(LOG_TAG, "Filament initialized successfully");

            Log.d(LOG_TAG, "setup render target stream");

            initializeDisplayTextureRenderTargetStream(); //display texture set to target via stream
            //drawTestTriangleWithMaterialEntity();  //entity added to scene
            makeSimpleTextureBufferForRenderTarget();
            createDummyTriangeWithMaterialEntity();
            view.setScene(scene);

            // Draw a test triangle to scene
            // Verify triangle configuration
// In your diagnostic code
            if (testTriangleEntity != 0) {
                RenderableManager renderableManager = engine.getRenderableManager();
                int renderableInstance = renderableManager.getInstance(testTriangleEntity);

                if (renderableInstance != 0) {
                    Log.d(LOG_TAG, "Renderable Instance Valid: true");
                    Log.d(LOG_TAG, "Renderable Primitive Count: " + renderableManager.getPrimitiveCount(renderableInstance));

                    try {
                        MaterialInstance materialInstance = renderableManager.getMaterialInstanceAt(renderableInstance, 0);
                        if (materialInstance != null) {
                            Log.d(LOG_TAG, "Material Instance Valid: true");

                            Material material = materialInstance.getMaterial();
                            if (material != null) {
                                Log.d(LOG_TAG, "Material Name: " + material.getName());

                                // Safely log parameter names instead of using toString
                                List<Material.Parameter> parameters = material.getParameters();
                                if (parameters != null) {

                                    Log.d(LOG_TAG, parameters.toString());
                                }
                            }
                        } else {
                            Log.e(LOG_TAG, "Material Instance is NULL");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error accessing material instance", e);
                    }
                } else {
                    Log.e(LOG_TAG, "Renderable Instance is INVALID");
                }
            }
           // Renderer.ClearOptions co = new Renderer.ClearOptions();
          ///  float[] fl =  {0.0f, 0.0f, 1.0f, 0.0f };
          //  co.clearColor = fl;
          //  renderer.setClearOptions(co);
            //Log.d(LOG_TAG, "draw triangle");
            //drawTestTriangleEntity();


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


    private MaterialInstance loadOrCreateMaterial() {
        Material testMaterial = null;
        MaterialInstance materialInstance = null;

        try {
            Log.d(LOG_TAG, "read from basic.filamat to get material");
            ByteBuffer materialBuffer = readAsset("basic.filamat");
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



    private int createSimpleColorEntity() {
        try {
            int entity = EntityManager.get().create();

            // Very simple quad vertices
            float[] quadVertices = {
                    -1.0f, -1.0f, -1.0f,  // bottom-left
                    1.0f, -1.0f, -1.0f,  // bottom-right
                    -1.0f, 1.0f, -1.0f,  // top-left
                    1.0f, 1.0f, -1.0f   // top-right
            };

            // Bright magenta color for all vertices
            float[] quadColors = {
                    1.0f, 0.0f, 1.0f, 1.0f,  // magenta
                    1.0f, 0.0f, 1.0f, 1.0f,  // magenta
                    1.0f, 0.0f, 1.0f, 1.0f,  // magenta
                    1.0f, 0.0f, 1.0f, 1.0f   // magenta
            };
 /*           ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(TEXTURE_WIDTH * TEXTURE_HEIGHT * 4)
                    .order(ByteOrder.NATIVE_ORDER);

  */
            // Create buffers
            FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(quadVertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexBuffer.put(quadVertices).position(0);

            FloatBuffer colorBuffer = ByteBuffer.allocateDirect(quadColors.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            colorBuffer.put(quadColors).position(0);

            // Create vertex buffer
            VertexBuffer.Builder vbBuilder = new VertexBuffer.Builder()
                    .vertexCount(4)
                    .bufferCount(2)
                    .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                            VertexBuffer.AttributeType.FLOAT3, 0, 0)
                    .attribute(VertexBuffer.VertexAttribute.COLOR, 1,
                            VertexBuffer.AttributeType.FLOAT4, 0, 0);

            VertexBuffer vertexBufferObj = vbBuilder.build(engine);
            //this.vb.setBufferAt(engine, 0, TRIANGLE_POSITIONS);
            //this.vb.setBufferAt(engine, 1, TRIANGLE_COLORS);
            vertexBufferObj.setBufferAt(engine, 0, vertexBuffer);
            vertexBufferObj.setBufferAt(engine, 1, colorBuffer);

            // Create index buffer
            short[] indices = {0, 1, 2, 2, 1, 3};
            ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            indexBuffer.put(indices).position(0);

            IndexBuffer.Builder ibBuilder = new IndexBuffer.Builder()
                    .indexCount(6)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT);
            IndexBuffer indexBufferObj = ibBuilder.build(engine);
            indexBufferObj.setBuffer(engine, indexBuffer);

            // Basic material - simplest possible
            MaterialInstance materialInstance = loadOrCreateMaterial();
            // Build renderable
            RenderableManager.Builder builder = new RenderableManager.Builder(1);
            builder.boundingBox(new Box(-1.0f, -1.0f, -0.6f, 1.0f, 1.0f, -0.4f))
                    .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBufferObj, indexBufferObj)
                    .material(0, materialInstance)
                    .culling(false)
                    .receiveShadows(false)
                    .castShadows(false)
                    .build(engine, entity);


            // Add to scene
            scene.addEntity(entity);

            return entity;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating simple entity: " + e.getMessage(), e);
            return 0;
        }
    }

    private void drawSimpleOpenGLTriangle() {
        // Simple shader for a red triangle
        String vertexShaderSource =
                "attribute vec3 aPosition;\n" +
                        "void main() {\n" +
                        "  gl_Position = vec4(aPosition, 1.0);\n" +
                        "}";

        String fragmentShaderSource =
                "precision mediump float;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
                        "}";

        // Compile vertex shader
        int vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertexShader, vertexShaderSource);
        GLES30.glCompileShader(vertexShader);

        // Compile fragment shader
        int fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES30.glCompileShader(fragmentShader);

        // Create program
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        GLES30.glUseProgram(program);

        // Triangle vertex data
        float[] triangleVertices = {
                0.0f, 0.5f, 0.0f,  // top
                -0.5f, -0.5f, 0.0f,  // bottom left
                0.5f, -0.5f, 0.0f   // bottom right
        };

        // Load vertex data
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(triangleVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(triangleVertices).position(0);

        // Get position attribute and enable it
        int positionHandle = GLES30.glGetAttribLocation(program, "aPosition");
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        // Draw triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        // Clean up
        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glUseProgram(0);
    }

    // Do this during initialization
    public void createDummyTriangeWithMaterialEntity() {
        try {
            // Create a simple entity
            EntityManager entityManager = EntityManager.get();
            int entity = entityManager.create();

            MaterialInstance materialInstance = loadOrCreateMaterial();

            // Explicitly set a bright, contrasting color
            materialInstance.setParameter("baseColor", 0.0f, 1.0f, 0.0f, 1.0f); // Bright green
            Log.d(LOG_TAG, "Material color set to bright green");

            float[] vertices = {
                    0.0f,  0.0f, -0.1f,   // Top vertex (very close to camera)
                    -0.5f, -0.5f, -0.1f,  // Bottom left (very close to camera)
                    0.5f, -0.5f, -0.1f    // Bottom right (very close to camera)
            };

            short[] indices = { 0, 1, 2 };

            // Create vertex buffer
            VertexBuffer.Builder vbb = new VertexBuffer.Builder()
                    .vertexCount(3)
                    .bufferCount(1)
                    .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                            VertexBuffer.AttributeType.FLOAT3, 0, 12);
            VertexBuffer vertexBuffer = vbb.build(engine);

            // Fill the vertex buffer
            ByteBuffer vertexData = ByteBuffer.allocateDirect(vertices.length * 4)
                    .order(ByteOrder.nativeOrder());
            FloatBuffer floatBuffer = vertexData.asFloatBuffer();
            floatBuffer.put(vertices);
            floatBuffer.flip();
            vertexBuffer.setBufferAt(engine, 0, vertexData);

            // Create index buffer
            IndexBuffer.Builder ibb = new IndexBuffer.Builder()
                    .indexCount(3)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT);
            IndexBuffer indexBuffer = ibb.build(engine);

            // Fill the index buffer
            ByteBuffer indexData = ByteBuffer.allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder());
            ShortBuffer shortBuffer = indexData.asShortBuffer();
            shortBuffer.put(indices);
            shortBuffer.flip();
            indexBuffer.setBuffer(engine, indexData);

            // Add renderable component to the entity
            RenderableManager.Builder builder = new RenderableManager.Builder(1);
            builder
                    .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer)
                    .material(0, materialInstance)
                    .boundingBox(new Box(-1.0f, -1.0f, -3.0f, 1.0f, 1.0f, -1.0f))
                    .culling(false)       // Disable culling to ensure visibility
                    .receiveShadows(false)
                    .castShadows(false)
                    .priority(1000)        // High priority to ensure it's drawn
                    .build(engine, entity);

            Log.d(LOG_TAG, "Creating dummy triangle entity");
            Log.d(LOG_TAG, "Triangle vertices: " +
                    Arrays.toString(vertices));

            // Add entity to scene
            scene.addEntity(entity);

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

    private void initializeDisplayTextureRenderTargetStream() {
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

            // Create texture with SRGB format like Sceneform does for visual content

            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_SRGB8_ALPHA8,  // Try SRGB format like Sceneform
                    viewportWidth, viewportHeight,
                    0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);


//Error initializing render target: Invalid texture sampler: When used with a stream, a texture must use a SAMPLER_EXTERNAL
//java.lang.IllegalStateException: Invalid texture sampler: When used with a stream, a texture must use a SAMPLER_EXTERNAL

            // Create a Stream with explicit dimensions to the display texture
       /*     Stream stream = new Stream.Builder()
                    .stream(displayTextureId)
                    .width(viewportWidth)
                    .height(viewportHeight)
                    .build(engine);

            // Create a Filament texture with SRGB format
            Texture filamentTextureTarget = new Texture.Builder()
                    .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
                    .format(Texture.InternalFormat.SRGB8_A8)  // Use SRGB for color
                    .usage(Texture.Usage.COLOR_ATTACHMENT | Texture.Usage.SAMPLEABLE)
                    .width(viewportWidth)
                    .height(viewportHeight)
                    .build(engine);

            // Connect the display stream to the texture
            filamentTextureTarget.setExternalStream(engine, stream);

            // Create a render target for scene to render INTO filamentRenderTarget
            // and then to be streamed to displayTextureId
            filamentRenderTarget= new RenderTarget.Builder()
                    .texture(RenderTarget.AttachmentPoint.COLOR, filamentTextureTarget)
                    .build(engine);

            // Set the render target on your view
            view.setRenderTarget(filamentRenderTarget);
*/
            Log.d(LOG_TAG, "Render target stream initialized with texture ID " + displayTextureId);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing render target: " + e.getMessage(), e);
        }
    }


    // Add a getter for the display texture
    public int getDisplayTextureId() {
        return displayTextureId;
    }
    public void drawTestTriangleWithMaterialEntity() {
        try {
            // Remove any existing triangle
            if (testTriangleEntity != 0) {
                scene.remove(testTriangleEntity);
                EntityManager.get().destroy(testTriangleEntity);
                testTriangleEntity = 0;
            }

            // Create a single, precise triangle
            testTriangleEntity = EntityManager.get().create();

            // Exactly defined vertices
            float[] triangleVertices = {
                    0.0f, 0.5f, -0.5f,    // top (precisely centered)
                    -0.5f, -0.5f, -0.5f,  // bottom left
                    0.5f, -0.5f, -0.5f    // bottom right
            };

            // Single color for all vertices
            float[] triangleColors = {
                    1.0f, 0.0f, 0.0f, 1.0f,  // solid red
                    1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 0.0f, 0.0f, 1.0f
            };

            // Vertex Buffer with explicit, minimal configuration
            VertexBuffer.Builder vbBuilder = new VertexBuffer.Builder()
                    .vertexCount(3)
                    .bufferCount(2)
                    .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                            VertexBuffer.AttributeType.FLOAT3, 0, 12)  // Explicit stride
                    .attribute(VertexBuffer.VertexAttribute.COLOR, 1,
                            VertexBuffer.AttributeType.FLOAT4, 0, 16); // Explicit stride

            VertexBuffer vertexBufferObj = vbBuilder.build(engine);

            // Create and fill buffers
            FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(triangleVertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexBuffer.put(triangleVertices).position(0);

            FloatBuffer colorBuffer = ByteBuffer.allocateDirect(triangleColors.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            colorBuffer.put(triangleColors).position(0);

            vertexBufferObj.setBufferAt(engine, 0, vertexBuffer);
            vertexBufferObj.setBufferAt(engine, 1, colorBuffer);

            // Index Buffer
            short[] indices = {0, 1, 2};
            IndexBuffer.Builder ibBuilder = new IndexBuffer.Builder()
                    .indexCount(3)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT);
            IndexBuffer indexBufferObj = ibBuilder.build(engine);

            ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            indexBuffer.put(indices).position(0);
            indexBufferObj.setBuffer(engine, indexBuffer);

            // Material with explicit, non-transparent settings
            MaterialInstance materialInstance = loadOrCreateMaterial();
            materialInstance.setParameter("baseColor", 1.0f, 0.0f, 0.0f, 1.0f); // Solid, opaque red

            // Renderable with precise configuration
            RenderableManager.Builder builder = new RenderableManager.Builder(1);
            builder
                    .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBufferObj, indexBufferObj)
                    .material(0, materialInstance)
                    .culling(false)
                    .receiveShadows(false)
                    .castShadows(false)
                    .priority(1000)
                    .build(engine, testTriangleEntity);

            // Add to scene
            scene.addEntity(testTriangleEntity);

            // Precise transformation
            TransformManager transformManager = engine.getTransformManager();
            float[] modelMatrix = new float[16];
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -0.5f);
            transformManager.create(testTriangleEntity);
            transformManager.setTransform(transformManager.getInstance(testTriangleEntity), modelMatrix);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Triangle creation error", e);
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

           /* camera.setProjection(Camera.Projection.ORTHOGRAPHIC,
                    -1.0f, 1.0f,   // left, right
                    -1.0f, 1.0f,   // bottom, top
                    0.1f, 10.0f);  // near, far
*/
// Position the camera to see objects at the origin


           // view.setCamera(camera);
            // Set the camera projection
            camera.setProjection(
                    (double) 120.0,                   // vertical field of view
                    (double) viewportWidth / viewportHeight,  // aspect ratio
                    0.01f,                  // near plane (closer for AR)
                    100.0f,                 // far plane
                    Camera.Fov.VERTICAL
            );


                // Explicitly position and orient the camera
                camera.lookAt(
                        0.0f, 0.0f, 0.0f,  // Eye position at origin
                        0.0f, 0.0f, -1.0f, // Look directly down negative Z
                        0.0f, 1.0f, 0.0f   // Up vector
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
        try {
            // Prevent rapid, repeated frame attempts
            long currentTime = SystemClock.elapsedRealtime();

            // Introduce a minimum frame interval (e.g., 16ms for ~60 FPS)
            final long MINIMUM_FRAME_INTERVAL = 16; // milliseconds

            // Track last successful frame time
            if (lastSuccessfulFrameTime > 0 &&
                    (currentTime - lastSuccessfulFrameTime) < MINIMUM_FRAME_INTERVAL) {
                Log.d(LOG_TAG, "Skipping frame to maintain consistent rate");
                return;
            }

            // Update camera and view
            updateCameraFromARCore(viewMatrix, projectionMatrix);

            // Explicit render target and view configuration
            view.setScene(scene);
            view.setRenderTarget(filamentRenderTarget);

            // Minimize clear operations
            Renderer.ClearOptions clearOptions = new Renderer.ClearOptions();
            clearOptions.clear = false;
            clearOptions.discard = false;
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

            try {
                // Render the view
                renderer.render(view);

                // Pixel buffer preparation with diagnostic logging
                int sampleSize = 4 * viewportWidth * viewportHeight;
                ByteBuffer sampleBuffer = ByteBuffer.allocateDirect(sampleSize);
                sampleBuffer.order(ByteOrder.nativeOrder());

                final Runnable callbackRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processSmallSample(sampleBuffer, viewportWidth * viewportHeight);

                            // Update last successful frame time
                            lastSuccessfulFrameTime = SystemClock.elapsedRealtime();
                        } catch (Exception callbackError) {
                            Log.e(LOG_TAG, "Pixel buffer callback error", callbackError);
                        }
                    }
                };

                // Pixel buffer descriptor configuration
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

    // Add this field to the class
    private long lastSuccessfulFrameTime = 0;

    private void processSmallSample(final ByteBuffer sampleBuffer, final int pixelCount) {
        // Check if we're on the UI thread
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Log.d(LOG_TAG, "Not on UI thread - posting to UI thread");
            // Post to UI thread to ensure OpenGL context
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    processSmallSample(sampleBuffer, pixelCount);
                }
            });
            return;
        }

        // Now we should be on the UI thread
        Log.d(LOG_TAG, "Processing sample on UI thread");

        // Reset buffer position
        sampleBuffer.rewind();


        // Bind the texture and upload data
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, displayTextureId);
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