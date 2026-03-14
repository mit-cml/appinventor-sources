package com.google.appinventor.components.runtime.arview.renderer;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
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

import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ARFilamentRenderer — renders animated glTF models using Filament 1.9.11.
 *
 * Architecture:
 *   Pass 1 — BackgroundRenderer (OpenGL, handled by ARView3D) draws camera feed.
 *   Pass 2 — Filament renders 3D nodes transparently on top via a SwapChain
 *             created directly from the GLSurfaceView's Surface.
 *
 * No pixel readback. No intermediate framebuffers. No CPU/GPU sync points.
 */
public class ARFilamentRenderer {

    private static final String LOG_TAG = "ARFilamentRenderer";

    // -------------------------------------------------------------------------
    // Filament core
    // -------------------------------------------------------------------------

    private Engine engine;
    private Renderer renderer;
    private Scene scene;
    private View view;
    private Camera camera;
    private SwapChain swapChain;

    private int cameraEntity;
    private int mainLightEntity;

    // -------------------------------------------------------------------------
    // Model loading
    // -------------------------------------------------------------------------

    private AssetLoader assetLoader;
    private ResourceLoader resourceLoader;
    private MaterialProvider materialProvider;

    // Maps ARNode → its loaded glTF asset
    private final Map<ARNode, FilamentAsset> nodeAssetMap = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Depth occlusion
    // -------------------------------------------------------------------------

    private Texture arDepthTexture;
    private MaterialInstance occlusionMaterialInstance;

    // Only apply occlusion materials once per asset load
    private boolean occlusionMaterialApplied = false;
    private boolean depthDataReceived = false;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private boolean isInitialized = false;
    private boolean animationEnabled = true;
    private float animationTime = 0f;

    // Frame rate throttle — avoid submitting faster than display vsync
    private long lastFrameTimeNs = 0;
    private static final long MIN_FRAME_INTERVAL_NS = 8_000_000L; // ~8ms

    private final GLSurfaceView glSurfaceView;
    private final Form formCopy;

    private static final float Z_NEAR = 0.085f;
    private static final float Z_FAR = 20f;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ARFilamentRenderer(ComponentContainer container,
                              GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.formCopy = container.$form();
        Log.d(LOG_TAG, "ARFilamentRenderer created");
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    public void initialize() {
        if (isInitialized) return;
        // Queue onto the GL thread where the EGL context is current
        glSurfaceView.queueEvent(() -> {
            loadNativeLibraries();
            initializeFilament();
        });
    }

    private synchronized void loadNativeLibraries() {
        try {
            System.loadLibrary("filament-jni");
            System.loadLibrary("gltfio-jni");
            Log.d(LOG_TAG, "Filament native libraries loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.e(LOG_TAG, "Failed to load Filament libraries", e);
            throw e;
        }
    }

    private void initializeFilament() {
        try {
            // Must be called on the GL thread so EGL context is current
            EGLContext eglContext = EGL14.eglGetCurrentContext();
            Log.d(LOG_TAG, "Initializing Filament with EGL context: " + eglContext);

            // Engine.create(Object) accepts an EGLContext directly in 1.9.11
            engine = Engine.create(eglContext);
            Log.d(LOG_TAG, "Filament Engine created");

            renderer = engine.createRenderer();
            scene    = engine.createScene();
            view     = engine.createView();

            cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);

            view.setCamera(camera);
            view.setScene(scene);
            view.setViewport(new Viewport(0, 0, viewportWidth, viewportHeight));

            // Transparent clear so BackgroundRenderer's camera feed shows through.
            // SwapChain.CONFIG_TRANSPARENT tells Filament the window surface has
            // an alpha channel and compositing is expected.
            Renderer.ClearOptions clearOptions = new Renderer.ClearOptions();
            clearOptions.clear = true;
            clearOptions.clearColor[0] = 0f;
            clearOptions.clearColor[1] = 0f;
            clearOptions.clearColor[2] = 0f;
            clearOptions.clearColor[3] = 0f; // fully transparent
            renderer.setClearOptions(clearOptions);

            // createSwapChain(Object, long) confirmed in Engine.java for 1.9.11.
            // Passing the real Surface means Filament renders directly to screen —
            // no readback ever needed.
            android.view.Surface surface = glSurfaceView.getHolder().getSurface();
            swapChain = engine.createSwapChain(surface, SwapChain.CONFIG_TRANSPARENT);
            Log.d(LOG_TAG, "SwapChain created on real Surface with CONFIG_TRANSPARENT");

            // Asset loading infrastructure
            materialProvider = new MaterialProvider(engine);
            assetLoader = new AssetLoader(engine, materialProvider,
                EntityManager.get());
            resourceLoader = new ResourceLoader(engine);

            setupLighting();
            loadOcclusionMaterial();

            isInitialized = true;
            Log.d(LOG_TAG, "Filament initialized successfully — direct surface rendering");

        } catch (Exception e) {
            Log.e(LOG_TAG, "Filament initialization failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Lighting
    // -------------------------------------------------------------------------

    private void setupLighting() {
        mainLightEntity = EntityManager.get().create();
        new LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 0.9f)
            .intensity(180_000f)
            .direction(0.5f, -1.0f, -0.8f)
            .castShadows(false)
            .build(engine, mainLightEntity);
        scene.addEntity(mainLightEntity);
        Log.d(LOG_TAG, "Directional light added");
    }

    // -------------------------------------------------------------------------
    // Occlusion material
    // -------------------------------------------------------------------------

    private void loadOcclusionMaterial() {
        try {
            ByteBuffer buf = readAsset("occlusion2.filamat", true);
            if (buf != null) {
                Material mat = new Material.Builder()
                    .payload(buf, buf.capacity())
                    .build(engine);
                occlusionMaterialInstance = mat.getDefaultInstance();
                Log.d(LOG_TAG, "Occlusion material loaded");
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "Could not load occlusion material: " + e.getMessage());
            // Non-fatal — nodes will render without depth occlusion
        }
    }

    // -------------------------------------------------------------------------
    // ARCore depth — called from ARView3D each frame when depth is available
    // -------------------------------------------------------------------------

    public void updateARCoreDepth(ByteBuffer depthData, float[] uvTransform,
                                  float near, float far,
                                  int depthWidth, int depthHeight) {
        if (!isInitialized) return;

        glSurfaceView.queueEvent(() -> {
            try {
                // Create or recreate depth texture if dimensions changed
                if (arDepthTexture == null
                    || arDepthTexture.getWidth(0) != depthWidth
                    || arDepthTexture.getHeight(0) != depthHeight) {

                    if (arDepthTexture != null) {
                        engine.destroyTexture(arDepthTexture);
                    }
                    arDepthTexture = new Texture.Builder()
                        .width(depthWidth)
                        .height(depthHeight)
                        .levels(1)
                        .format(Texture.InternalFormat.RG8)
                        .sampler(Texture.Sampler.SAMPLER_2D)
                        .usage(Texture.Usage.SAMPLEABLE)
                        .build(engine);

                    Log.d(LOG_TAG, "ARCore depth texture created: "
                        + depthWidth + "x" + depthHeight);
                    // New texture means we need to re-apply to materials
                    occlusionMaterialApplied = false;
                }

                // Upload depth data
                depthData.rewind();
                Texture.PixelBufferDescriptor descriptor =
                    new Texture.PixelBufferDescriptor(
                        depthData,
                        Texture.Format.RG,
                        Texture.Type.UBYTE,
                        4,           // alignment
                        0,           // left padding
                        0,           // top padding
                        depthWidth,  // stride
                        null,
                        null
                    );
                arDepthTexture.setImage(engine, 0, descriptor);

                // Wire occlusion material parameters once depth arrives
                if (!depthDataReceived) {
                    depthDataReceived = true;
                    Log.d(LOG_TAG, "First depth data received — enabling occlusion");
                }

                updateOcclusionParameters(near, far, uvTransform);

                // Apply occlusion material to any already-loaded assets
                if (!occlusionMaterialApplied && occlusionMaterialInstance != null
                    && !nodeAssetMap.isEmpty()) {
                    applyOcclusionToAllAssets();
                    occlusionMaterialApplied = true;
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Depth update error: " + e.getMessage(), e);
            }
        });
    }

    private void updateOcclusionParameters(float near, float far,
                                           float[] uvTransform) {
        if (occlusionMaterialInstance == null) return;
        try {
            occlusionMaterialInstance.setParameter("nearPlane", near);
            occlusionMaterialInstance.setParameter("farPlane", far);
            occlusionMaterialInstance.setParameter("occlusionBias", 0.01f);

            // Pass as rows — confirmed safe for Filament 1.9.11
            occlusionMaterialInstance.setParameter("uvTransformRow0",
                uvTransform[0], uvTransform[1], uvTransform[2], uvTransform[3]);
            occlusionMaterialInstance.setParameter("uvTransformRow1",
                uvTransform[4], uvTransform[5], uvTransform[6], uvTransform[7]);
            occlusionMaterialInstance.setParameter("uvTransformRow2",
                uvTransform[8], uvTransform[9], uvTransform[10], uvTransform[11]);
            occlusionMaterialInstance.setParameter("uvTransformRow3",
                uvTransform[12], uvTransform[13], uvTransform[14], uvTransform[15]);

            if (arDepthTexture != null) {
                TextureSampler sampler = new TextureSampler(
                    TextureSampler.MinFilter.LINEAR,
                    TextureSampler.MagFilter.LINEAR,
                    TextureSampler.WrapMode.CLAMP_TO_EDGE
                );
                occlusionMaterialInstance.setParameter(
                    "depthTexture", arDepthTexture, sampler);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Occlusion parameter update failed: " + e.getMessage());
        }
    }

    private void applyOcclusionToAllAssets() {
        if (occlusionMaterialInstance == null) return;
        RenderableManager rm = engine.getRenderableManager();
        for (FilamentAsset asset : nodeAssetMap.values()) {
            applyOcclusionToAsset(asset, rm);
        }
        Log.d(LOG_TAG, "Occlusion material applied to all loaded assets");
    }

    private void applyOcclusionToAsset(FilamentAsset asset,
                                       RenderableManager rm) {
        for (int entityId : asset.getEntities()) {
            if (rm.hasComponent(entityId)) {
                int instance = rm.getInstance(entityId);
                int count = rm.getPrimitiveCount(instance);
                for (int i = 0; i < count; i++) {
                    rm.setMaterialInstanceAt(
                        instance, i, occlusionMaterialInstance);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Surface / viewport changes — called from ARView3D.onSurfaceChanged
    // -------------------------------------------------------------------------

    public void updateSurfaceDimensions(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewportWidth  = width;
        viewportHeight = height;
        if (view != null) {
            view.setViewport(new Viewport(0, 0, width, height));
            Log.d(LOG_TAG, "Viewport updated: " + width + "x" + height);
        }
    }

    // -------------------------------------------------------------------------
    // Camera — mirrors ARCore camera exactly
    // -------------------------------------------------------------------------

    private void updateCameraFromARCore(float[] arcoreViewMatrix,
                                        float[] arcoreProjectionMatrix) {
        if (camera == null) return;

        System.arraycopy(arcoreViewMatrix,     0, viewMatrix,      0, 16);
        System.arraycopy(arcoreProjectionMatrix, 0, projectionMatrix, 0, 16);

        float[] invView = new float[16];
        if (!Matrix.invertM(invView, 0, arcoreViewMatrix, 0)) return;

        float camX = invView[12];
        float camY = invView[13];
        float camZ = invView[14];

        // Forward = -Z column of inverted view
        float fwdX = -invView[8];
        float fwdY = -invView[9];
        float fwdZ = -invView[10];

        // Up = +Y column of inverted view
        float upX = invView[4];
        float upY = invView[5];
        float upZ = invView[6];

        camera.lookAt(
            camX, camY, camZ,
            camX + fwdX, camY + fwdY, camZ + fwdZ,
            upX, upY, upZ
        );

        // Use ARCore's exact projection matrix
        double[] proj64 = new double[16];
        for (int i = 0; i < 16; i++) proj64[i] = arcoreProjectionMatrix[i];
        camera.setCustomProjection(proj64, Z_NEAR, Z_FAR);
    }

    // -------------------------------------------------------------------------
    // Main draw — called every frame from ARView3D.onDrawFrame
    //
    // BackgroundRenderer has already drawn the camera feed to the screen.
    // Filament draws 3D nodes on top via its transparent SwapChain.
    // -------------------------------------------------------------------------

    public void draw(List<ARNode> nodes, float[] arcoreViewMatrix,
                     float[] arcoreProjectionMatrix) {

        if (!isInitialized) {
            Log.d(LOG_TAG, "Skipping draw — not yet initialized");
            return;
        }

        // Frame rate throttle
        long now = System.nanoTime();
        if (lastFrameTimeNs > 0
            && (now - lastFrameTimeNs) < MIN_FRAME_INTERVAL_NS) {
            return;
        }

        updateCameraFromARCore(arcoreViewMatrix, arcoreProjectionMatrix);

        if (nodes != null && !nodes.isEmpty()) {
            loadAndPositionNodes(nodes);
            updateAnimations();
        }

        // Single render pass — directly to the screen surface
        try {
            if (renderer.beginFrame(swapChain, now)) {
                renderer.render(view);
                renderer.endFrame();
                lastFrameTimeNs = now;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Render error: " + e.getMessage(), e);
            safeEndFrame();
        }
    }

    private void safeEndFrame() {
        try {
            if (renderer != null) renderer.endFrame();
        } catch (Exception ignored) { }
    }

    // -------------------------------------------------------------------------
    // Node processing — load models and position them from ARCore anchors
    // -------------------------------------------------------------------------

    private void loadAndPositionNodes(Collection<ARNode> nodes) {
        for (ARNode node : nodes) {
            if (!shouldRender(node)) continue;

            // Load model if not yet loaded
            if (!nodeAssetMap.containsKey(node)) {
                try {
                    loadModelForNode(node);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to load model for "
                        + node.NodeType() + ": " + e.getMessage());
                    continue;
                }
            }

            FilamentAsset asset = nodeAssetMap.get(node);
            if (asset == null) continue;

            // Position the model at its ARCore anchor
            if (node.Anchor() != null
                && node.Anchor().getTrackingState() == TrackingState.TRACKING) {
                applyNodeTransform(node, asset);
            }
        }
    }

    private boolean shouldRender(ARNode node) {
        if (node == null)                          return false;
        if (!node.Visible())                       return false;
        if (node.Model() == null)                  return false;
        if (node.Model().isEmpty())                return false;
        return true;
    }

    private void applyNodeTransform(ARNode node, FilamentAsset asset) {
        TransformManager tm = engine.getTransformManager();
        int rootInstance = tm.getInstance(asset.getRoot());
        if (rootInstance == 0) return;

        Pose pose = node.Anchor().getPose();
        float[] anchorPos = pose.getTranslation();

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);

        // Translation
        modelMatrix[12] = anchorPos[0];
        modelMatrix[13] = anchorPos[1];
        modelMatrix[14] = anchorPos[2];

        // Rotation from ARCore pose quaternion
        float[] rotMatrix = new float[16];
        quaternionToMatrix(pose.getRotationQuaternion(), rotMatrix);
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0);

        // Scale
        float s = node.Scale();
        Matrix.scaleM(modelMatrix, 0, s, s, s);

        tm.setTransform(rootInstance, modelMatrix);
    }

    // -------------------------------------------------------------------------
    // Model loading
    // -------------------------------------------------------------------------

    private void loadModelForNode(ARNode node) throws IOException {
        String modelPath = node.Model();
        Log.d(LOG_TAG, "Loading model: " + modelPath);

        ByteBuffer buffer = readAsset(modelPath, false);
        if (buffer == null) {
            throw new IOException("Could not read asset: " + modelPath);
        }

        FilamentAsset asset = assetLoader.createAssetFromBinary(buffer);
        if (asset == null) {
            throw new IOException("AssetLoader failed for: " + modelPath);
        }

        // Add all entities to scene
        scene.addEntity(asset.getRoot());
        for (int entityId : asset.getEntities()) {
            if (entityId != asset.getRoot()) {
                scene.addEntity(entityId);
            }
        }

        // Load textures and buffers
        resourceLoader.loadResources(asset);

        nodeAssetMap.put(node, asset);
        Log.d(LOG_TAG, "Loaded model: " + modelPath
            + " (" + asset.getEntities().length + " entities)");

        // Apply occlusion if depth data already arrived
        if (depthDataReceived && occlusionMaterialInstance != null) {
            applyOcclusionToAsset(asset, engine.getRenderableManager());
            Log.d(LOG_TAG, "Occlusion applied to freshly loaded: " + modelPath);
        }
    }

    // -------------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------------

    private void updateAnimations() {
        if (!animationEnabled) return;
        animationTime += 1f / 60f;
        for (FilamentAsset asset : nodeAssetMap.values()) {
            if (asset.getAnimator().getAnimationCount() > 0) {
                asset.getAnimator().applyAnimation(0, animationTime);
                asset.getAnimator().updateBoneMatrices();
            }
        }
    }

    public void setAnimationEnabled(boolean enabled) {
        this.animationEnabled = enabled;
    }

    // -------------------------------------------------------------------------
    // Node removal — called when ARView3D removes a node
    // -------------------------------------------------------------------------

    public void removeNode(ARNode node) {
        FilamentAsset asset = nodeAssetMap.remove(node);
        if (asset == null) return;

        // Remove entities from scene before destroying
        scene.remove(asset.getRoot());
        for (int entityId : asset.getEntities()) {
            scene.remove(entityId);
        }
        assetLoader.destroyAsset(asset);
        Log.d(LOG_TAG, "Removed node: " + node.NodeType());
    }

    // -------------------------------------------------------------------------
    // Asset reading
    // -------------------------------------------------------------------------

    /**
     * @param assetName  file name
     * @param isInternal true → read from app's internal assets (e.g. shaders),
     *                   false → read from App Inventor project assets
     */
    private ByteBuffer readAsset(String assetName,
                                 boolean isInternal) throws IOException {
        try {
            java.io.InputStream is = isInternal
                ? MediaUtil.getAssetsIgnoreCaseInputStream(formCopy, assetName)
                : formCopy.openAsset(assetName);

            if (is == null) {
                throw new IOException("Asset not found: " + assetName);
            }

            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();

            ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length);
            buf.put(bytes);
            buf.rewind();
            return buf;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to read asset: " + assetName, e);
        }
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    /** Converts a quaternion [x, y, z, w] to a column-major 4x4 rotation matrix. */
    private void quaternionToMatrix(float[] q, float[] m) {
        float x = q[0], y = q[1], z = q[2], w = q[3];
        float x2 = x + x, y2 = y + y, z2 = z + z;
        float xx = x * x2, xy = x * y2, xz = x * z2;
        float yy = y * y2, yz = y * z2, zz = z * z2;
        float wx = w * x2, wy = w * y2, wz = w * z2;

        m[0]  = 1f - (yy + zz); m[1]  = xy + wz;       m[2]  = xz - wy;       m[3]  = 0f;
        m[4]  = xy - wz;        m[5]  = 1f - (xx + zz); m[6]  = yz + wx;       m[7]  = 0f;
        m[8]  = xz + wy;        m[9]  = yz - wx;        m[10] = 1f - (xx + yy); m[11] = 0f;
        m[12] = 0f;             m[13] = 0f;             m[14] = 0f;            m[15] = 1f;
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    public void destroy() {
        if (engine == null) return;

        Log.d(LOG_TAG, "Destroying Filament resources");

        // Destroy all loaded assets
        for (FilamentAsset asset : nodeAssetMap.values()) {
            scene.remove(asset.getRoot());
            for (int e : asset.getEntities()) scene.remove(e);
            assetLoader.destroyAsset(asset);
        }
        nodeAssetMap.clear();

        // Destroy lights
        if (mainLightEntity != 0) {
            scene.remove(mainLightEntity);
            engine.destroyEntity(mainLightEntity);
        }

        // Destroy depth texture
        if (arDepthTexture != null) {
            engine.destroyTexture(arDepthTexture);
        }

        // Destroy asset loading infrastructure
        if (resourceLoader != null)  resourceLoader.destroy();
        if (assetLoader != null)     assetLoader.destroy();
        if (materialProvider != null) {
            materialProvider.destroyMaterials();
            materialProvider.destroy();
        }

        // Destroy camera entity
        if (cameraEntity != 0) {
            engine.destroyCamera(camera);
            engine.destroyEntity(cameraEntity);
        }

        // Destroy core Filament objects
        engine.destroyRenderer(renderer);
        engine.destroyView(view);
        engine.destroyScene(scene);

        if (swapChain != null) {
            engine.destroySwapChain(swapChain);
        }

        engine.destroy();
        engine = null;
        isInitialized = false;

        Log.d(LOG_TAG, "Filament resources destroyed");
    }
}