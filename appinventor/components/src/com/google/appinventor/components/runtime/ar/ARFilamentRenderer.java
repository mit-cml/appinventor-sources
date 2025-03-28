package com.google.appinventor.components.runtime;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.Entity;
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
import com.google.android.filament.Skybox;
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

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.arview.helper.GLTextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced ARFilamentRenderer - A complete Filament-based renderer for ARCore
 *
 * This renderer integrates ARCore with Filament for high-quality 3D rendering in AR applications.
 * It manages the complete rendering pipeline including:
 * - Loading and managing 3D models (glTF/glb assets)
 * - Configuring materials and lighting
 * - Handling AR anchors and tracking
 * - Adapting to AR environment through ARCore's lighting estimation
 */
public class ARFilamentRenderer {
    private static final String TAG = ARFilamentRenderer.class.getSimpleName();

    // Filament engine core components
    private Engine engine;
    private Renderer renderer;
    private Scene scene;
    private View view;
    private Camera camera;
    private SwapChain swapChain;
    private Skybox skybox;

    // Asset loading components
    private AssetLoader assetLoader;
    private ResourceLoader resourceLoader;
    private MaterialProvider materialProvider;

    // Cache for loaded assets
    private Map<ARNode, FilamentAsset> nodeAssetMap = new ConcurrentHashMap<>();
    private Map<FilamentAsset, List<Integer>> assetEntityMap = new ConcurrentHashMap<>();

    // Rendering state
    private int cameraEntity;
    private int mainLightEntity;
    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private boolean hasInitialized = false;

    // Animation support
    private float animationTime = 0.0f;
    private boolean animationEnabled = true;

    // AR environment integration
    private float[] lightIntensity = {1.0f, 1.0f, 1.0f};
    private float[] lightDirection = {0.0f, -1.0f, 0.0f};
    private float lightTemperature = 6500.0f;

    // Texture capture and export
    private GLTextureHelper textureHelper;
    private Texture filamentCapturedTexture;

    // Constants
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final String DEFAULT_MODEL = "chick_baby_chicken_bird.glb";

    /**
     * Creates a new ARFilamentRenderer
     *
     * @param render The ARViewRender that manages the OpenGL context
     * @throws IOException If asset loading fails
     */
    public ARFilamentRenderer(ARViewRender render) throws IOException {
        textureHelper = new GLTextureHelper();
    }

    /**
     * Initializes the Filament engine and core components
     *
     * @param arView The GLSurfaceView used for AR rendering
     */
    public void onSurfaceCreated(GLSurfaceView arView) {
        try {
            // Initialize Filament
            initializeFilament();

            // Create the core Filament components
            engine = Engine.create();
            renderer = engine.createRenderer();
            scene = engine.createScene();

            // Create camera entity
            cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);
            camera.setExposure(16.0f, 1.0f/125.0f, 100.0f);

            // Set up the view
            view = engine.createView();
            view.setScene(scene);
            view.setCamera(camera);
            view.setShadowingEnabled(true);
            view.setPostProcessingEnabled(true);

            // Create a skybox with subtle ambient color
            skybox = new Skybox.Builder()
                    .color(0.1f, 0.125f, 0.25f, 1.0f)
                    .build(engine);
            scene.setSkybox(skybox);

            // Set up asset loaders for glTF models
            materialProvider = new MaterialProvider(engine);
            assetLoader = new AssetLoader(engine, materialProvider, EntityManager.get());
            resourceLoader = new ResourceLoader(engine);

            // Set up lighting
            setupLighting();

            // Create an offscreen SwapChain
            swapChain = engine.createSwapChain(0, 0, SwapChain.CONFIG_READABLE);

            hasInitialized = true;
            Log.d(TAG, "Filament engine initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Filament engine", e);
        }
    }

    /**
     * Initialize Filament native library loading
     */
    private void initializeFilament() {
        try {
            System.loadLibrary("filament-jni");
            System.loadLibrary("gltfio-jni");
            Log.d(TAG, "Filament libraries loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load Filament libraries", e);
            throw e;
        }
    }

    /**
     * Sets up the lighting environment for Filament
     */
    private void setupLighting() {
        // Create a main directional light (sun)
        mainLightEntity = EntityManager.get().create();

        LightManager.Builder lightBuilder = new LightManager.Builder(LightManager.Type.DIRECTIONAL);
        lightBuilder.color(lightIntensity[0], lightIntensity[1], lightIntensity[2])
                .intensity(100000.0f)
                .direction(lightDirection[0], lightDirection[1], lightDirection[2])
                .castShadows(true);

        LightManager lightManager = engine.getLightManager();
        lightBuilder.build(engine, mainLightEntity);
        scene.addEntity(mainLightEntity);

        // Add additional ambient light
        int ambientLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.SUN)
                .color(0.8f, 0.8f, 1.0f)
                .intensity(0.5f)
                .build(engine, ambientLightEntity);
        scene.addEntity(ambientLightEntity);
    }

    /**
     * Updates the view when surface dimensions change
     *
     * @param width The new surface width
     * @param height The new surface height
     */
    public void onSurfaceChanged(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid surface dimensions: " + width + "x" + height);
            return;
        }

        this.viewportWidth = width;
        this.viewportHeight = height;

        // Update the viewport dimensions
        view.setViewport(new Viewport(0, 0, width, height));

        // Create our texture for output capture
        textureHelper.createTexture(width, height);

        // Recreate SwapChain
        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
        }
        swapChain = engine.createSwapChain(width, height, SwapChain.CONFIG_READABLE);

        Log.d(TAG, "Surface changed: " + width + "x" + height);
    }

    /**
     * Draws the AR scene using Filament
     *
     * @param render The ARViewRender context
     * @param allNodes Collection of ARNodes to render
     * @param viewMatrix The AR camera view matrix
     * @param projectionMatrix The AR camera projection matrix
     */
    public void draw(ARViewRender render, Collection<ARNode> allNodes,
                     float[] viewMatrix, float[] projectionMatrix) {
        if (!hasInitialized || engine == null) {
            Log.w(TAG, "Skipping draw - renderer not initialized");
            return;
        }

        try {
            // Update camera transform from ARCore
            updateCameraFromARCore(viewMatrix, projectionMatrix);

            // Begin capturing to texture
            textureHelper.beginCapture();

            // Process each node
            boolean hasVisibleNodes = processNodes(render, allNodes);

            // If no visible nodes, ensure default model
            if (!hasVisibleNodes && nodeAssetMap.isEmpty()) {
                ensureDefaultModel(render);
            }

            // Update animations
            updateAnimations();

            // Render the scene
            if (renderer.beginFrame(swapChain, 0L)) {
                renderer.render(view);
                renderer.endFrame();
            }

            // Finish capturing to texture
            textureHelper.endCapture();

        } catch (Exception e) {
            Log.e(TAG, "Error during rendering: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the Filament camera using ARCore camera matrices
     */
    private void updateCameraFromARCore(float[] viewMatrix, float[] projectionMatrix) {
        // Invert the AR view matrix to get camera pose in world space
        float[] cameraModelMatrix = new float[16];
        Matrix.invertM(cameraModelMatrix, 0, viewMatrix, 0);

        // Update camera transform
        TransformManager transformManager = engine.getTransformManager();
        int cameraInstance = transformManager.getInstance(cameraEntity);
        transformManager.setTransform(cameraInstance, cameraModelMatrix);

        double aspectRatio = (double) viewportWidth / viewportHeight;
        camera.setProjection(
                45.0,                // fov in degrees
                aspectRatio,         // aspect ratio
                Z_NEAR,              // near plane
                Z_FAR,               // far plane
                Camera.Fov.VERTICAL  // field of view direction
        );
    }

    /**
     * Process all ARNodes for rendering
     *
     * @return true if any nodes are visible and being rendered
     */
    private boolean processNodes(ARViewRender render, Collection<ARNode> nodes) {
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
                    loadModelForNode(render, node);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load model for node: " + node, e);
                    continue;
                }
            }

            FilamentAsset asset = nodeAssetMap.get(node);
            if (asset == null) continue;

            // Get node pose
            float[] poseMatrix = new float[16];
            Matrix.setIdentityM(poseMatrix, 0);

            // Use anchor pose if available
            if (node.Anchor() != null && node.Anchor().getTrackingState() == TrackingState.TRACKING) {
                node.Anchor().getPose().toMatrix(poseMatrix, 0);
                hasVisibleNodes = true;
            } else {
                // Use node position if no anchor or tracking lost
                Pose defaultPose = new Pose(
                        new float[] {node.XPosition(), node.YPosition(), node.ZPosition()},
                        new float[] {0, 0, 0, 1} // Default quaternion
                );
                defaultPose.toMatrix(poseMatrix, 0);
                hasVisibleNodes = true;
            }

            // Apply node transformations (scale, rotation)
            applyNodeTransformation(node, asset, poseMatrix);
        }

        return hasVisibleNodes;
    }

    /**
     * Apply node-specific transformations
     */
    private void applyNodeTransformation(ARNode node, FilamentAsset asset, float[] poseMatrix) {
        TransformManager transformManager = engine.getTransformManager();
        int rootEntityId = asset.getRoot();
        int rootInstance = transformManager.getInstance(rootEntityId);

        // Apply scale
        float scale = node.Scale();
        if (scale != 1.0f) {
            float[] scaleMatrix = new float[16];
            Matrix.setIdentityM(scaleMatrix, 0);
            scaleMatrix[0] = scale;
            scaleMatrix[5] = scale;
            scaleMatrix[10] = scale;

            float[] finalMatrix = new float[16];
            Matrix.multiplyMM(finalMatrix, 0, poseMatrix, 0, scaleMatrix, 0);
            transformManager.setTransform(rootInstance, finalMatrix);
        } else {
            transformManager.setTransform(rootInstance, poseMatrix);
        }
    }

    /**
     * Update animations for all assets
     */
    private void updateAnimations() {
        if (!animationEnabled) return;

        // Update global animation time
        animationTime += 1.0f / 60.0f; // Assuming 60fps

        // Apply animation to each asset that has animations
        for (FilamentAsset asset : nodeAssetMap.values()) {
            if (asset != null && asset.getAnimator().getAnimationCount() > 0) {
                asset.getAnimator().applyAnimation(0, animationTime);
                asset.getAnimator().updateBoneMatrices();
            }
        }
    }

    /**
     * Loads a model for a node
     */
    private void loadModelForNode(ARViewRender render, ARNode node) throws IOException {
        String modelFile = node.Model();
        if (modelFile == null || modelFile.isEmpty()) {
            throw new IOException("No model specified for node");
        }

        Log.d(TAG, "Loading model: " + modelFile + " for node: " + node);

        try {
            // Read the asset file
            ByteBuffer buffer = readAsset(render, modelFile);

            // Create the asset
            FilamentAsset asset = assetLoader.createAssetFromBinary(buffer);
            if (asset == null) {
                throw new IOException("Failed to create asset from model: " + modelFile);
            }

            // Track entity IDs for this asset
            List<Integer> entityIds = new ArrayList<>();

            // Add root and all renderable entities to scene
            int rootEntityId = asset.getRoot();
            scene.addEntity(rootEntityId);
            entityIds.add(rootEntityId);

            for (int entityId : asset.getEntities()) {
                if (entityId != rootEntityId) {
                    scene.addEntity(entityId);
                    entityIds.add(entityId);
                }
            }

            // Load all resources for the asset
            resourceLoader.loadResources(asset);

            // Store mappings
            nodeAssetMap.put(node, asset);
            assetEntityMap.put(asset, entityIds);

            Log.d(TAG, "Successfully loaded model " + modelFile +
                    " with " + entityIds.size() + " entities");

        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + modelFile, e);
            throw e;
        }
    }

    /**
     * Ensures that a default model is loaded if no other models are present
     */
    public void loadDefaultModel(ARViewRender render) {
        try {
            // Create a mock node for the default model
            ARNode defaultNode = new ModelNode(null);
            defaultNode.Model(DEFAULT_MODEL);
            defaultNode.XPosition(0);
            defaultNode.YPosition(0);
            defaultNode.ZPosition(-1.5f);
            defaultNode.Scale(0.5f);

            // Load model for this node
            loadModelForNode(render, defaultNode);

            Log.d(TAG, "Default model loaded");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load default model", e);
        }
    }

    /**
     * Ensures a default model is loaded if needed
     */
    private void ensureDefaultModel(ARViewRender render) {
        // Check if we already have any models loaded
        if (nodeAssetMap.isEmpty()) {
            loadDefaultModel(render);
        }
    }

    /**
     * Reads an asset file into a ByteBuffer
     */
    private ByteBuffer readAsset(ARViewRender render, String assetName) throws IOException {
        try (java.io.InputStream is = render.getForm().openAsset(assetName)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
            byteBuffer.put(buffer);
            byteBuffer.rewind();
            return byteBuffer;
        }
    }

    /**
     * Updates lighting based on ARCore light estimation
     */
    public void updateLightEstimation(LightEstimate lightEstimate) {
        if (lightEstimate == null || lightEstimate.getState() != LightEstimate.State.VALID) {
            return;
        }

        // Get light parameters from ARCore
        float[] colorCorrection = new float[4];
        lightEstimate.getColorCorrection(colorCorrection, 0);

        // Update directional light
        LightManager lightManager = engine.getLightManager();
        int lightInstance = lightManager.getInstance(mainLightEntity);

        // Update intensity based on color correction (RGB)
        float intensity = (colorCorrection[0] + colorCorrection[1] + colorCorrection[2]) / 3.0f;
        intensity = Math.max(intensity * 100000.0f, 5000.0f); // Keep a minimum level

        lightManager.setColor(lightInstance,
                colorCorrection[0], colorCorrection[1], colorCorrection[2]);
        lightManager.setIntensity(lightInstance, intensity);

        // If using environmental HDR light estimation (ARCore 1.18+)
        if (lightEstimate.getEnvironmentalHdrMainLightIntensity() != null) {
            float[] direction = lightEstimate.getEnvironmentalHdrMainLightDirection();
            float[] envLightIntensity = lightEstimate.getEnvironmentalHdrMainLightIntensity();

            lightManager.setDirection(lightInstance,
                    direction[0], direction[1], direction[2]);
            lightManager.setColor(lightInstance,
                    envLightIntensity[0], envLightIntensity[1], envLightIntensity[2]);
        }
    }

    /**
     * Gets the rendered texture ID for external use
     */
    public int getRenderedTextureId() {
        return textureHelper.getTextureId();
    }

    /**
     * Removes a node from the scene
     */
    public void removeNode(ARNode node) {
        FilamentAsset asset = nodeAssetMap.get(node);
        if (asset != null) {
            List<Integer> entities = assetEntityMap.get(asset);
            if (entities != null) {
                for (int entityId : entities) {
                    scene.remove(entityId);
                }
            }

            assetLoader.destroyAsset(asset);
            assetEntityMap.remove(asset);
            nodeAssetMap.remove(node);
        }
    }

    /**
     * Enables or disables animation
     */
    public void setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
    }

    /**
     * Cleanup all resources when done
     */
    public void destroy() {
        if (engine != null) {
            // Clean up all loaded assets
            for (Map.Entry<ARNode, FilamentAsset> entry : nodeAssetMap.entrySet()) {
                FilamentAsset asset = entry.getValue();
                List<Integer> entities = assetEntityMap.get(asset);

                if (entities != null) {
                    for (int entityId : entities) {
                        scene.remove(entityId);
                        EntityManager.get().destroy(entityId);
                    }
                }

                assetLoader.destroyAsset(asset);
            }

            nodeAssetMap.clear();
            assetEntityMap.clear();

            // Clean up Filament resources
            engine.destroyRenderer(renderer);
            engine.destroyView(view);
            engine.destroyScene(scene);
            engine.destroyCamera(camera);

            if (skybox != null) {
                engine.destroySkybox(skybox);
            }

            if (swapChain != null) {
                engine.destroySwapChain(swapChain);
            }

            // Destroy engine last
            engine.destroy();
            engine = null;
        }

        // Clean up texture helper
        if (textureHelper != null) {
            textureHelper.destroy();
        }
    }
}