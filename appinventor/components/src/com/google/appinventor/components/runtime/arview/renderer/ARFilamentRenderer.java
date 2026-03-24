package com.google.appinventor.components.runtime.arview.renderer;

import android.os.Handler;
import android.os.HandlerThread;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
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
import com.google.android.filament.View;
import com.google.android.filament.Viewport;

import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;

import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ar.ARNodeBase;
import com.google.appinventor.components.runtime.ar.ModelNode;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ARFilamentRenderer — renders animated glTF models using Filament 1.9.11.
 *
 * THREADING MODEL:
 *
 *   FilamentRenderThread — a dedicated HandlerThread that owns all Filament
 *   objects and the render loop.
 *   sync with frame by calling renderFrame
 *
 *   GL thread (GLSurfaceView) — calls updateFrame() each frame to hand off
 *   fresh ARCore matrices and the current node list. This is a non-blocking
 *   write under a lock. The GL thread never calls any Filament API.
 *
 *   Main thread — calls initializeEngine(), initializeSwapChain(),
 *   destroySwapChain(), and destroy(). These post work to FilamentRenderThread
 *   where needed and return immediately.
 *
 * WHY THIS FIXES THE FREEZE:
 *   renderer.beginFrame() can block waiting for Filament's backend to finish
 *   the previous frame. On the GL thread that stalls camera feed presentation.
 *   On the main thread it freezes the entire UI. On FilamentRenderThread it
 *   blocks nothing that matters — camera and UI run freely at full frame rate.
 *
 */
public class ARFilamentRenderer {

    private static final String LOG_TAG = "ARFilamentRenderer";

    // -------------------------------------------------------------------------
    // Dedicated render thread
    // -------------------------------------------------------------------------

    private HandlerThread filamentThread;
    private Handler       filamentHandler;

    // -------------------------------------------------------------------------
    // Filament core — only touched on FilamentRenderThread
    // -------------------------------------------------------------------------

    private Engine    engine;
    private Renderer  renderer;
    private Scene     scene;
    private View      view;
    private Camera    camera;
    private SwapChain swapChain;
    private int       cameraEntity;
    private int       mainLightEntity;

    // -------------------------------------------------------------------------
    // glTF loading — only touched on FilamentRenderThread
    // -------------------------------------------------------------------------

    private AssetLoader      assetLoader;
    private ResourceLoader   resourceLoader;
    private MaterialProvider materialProvider;

    private final Map<ARNode, FilamentAsset> nodeAssetMap = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Depth occlusion — only touched on FilamentRenderThread
    // -------------------------------------------------------------------------

    private Texture          arDepthTexture;
    private MaterialInstance occlusionMaterialInstance;
    private boolean depthDataReceived           = false;
    private boolean occlusionAppliedToAllAssets = false;

    private float[] lastCameraWorldPos = {0, 0, 0};
    private PlaneFinder planeFinder = null; // when depth isn't avail via device

    List<Plane> trackingPlanes = new ArrayList<>();
    public void setPlaneFinder(PlaneFinder finder) {
        this.planeFinder = finder;
    }
    // -------------------------------------------------------------------------
    // Shared state — written by GL thread, read by FilamentRenderThread
    // Guarded by matrixLock
    // -------------------------------------------------------------------------

    private final Object  matrixLock    = new Object();
    private final float[] sharedView    = new float[16];
    private final float[] sharedProj    = new float[16];
    private List<ARNode>  sharedNodes   = new ArrayList<>();
    private boolean       matricesReady = false;

    // Depth update — written by GL thread, consumed by FilamentRenderThread
    private static final class DepthUpdate {
        final ByteBuffer data;
        final float[]    uvTransform;
        final float      near, far;
        final int        width, height;

        DepthUpdate(ByteBuffer data, float[] uvTransform,
                    float near, float far, int width, int height) {
            this.data        = data;
            this.uvTransform = uvTransform;
            this.near        = near;
            this.far         = far;
            this.width       = width;
            this.height      = height;
        }
    }

    private final Object      depthLock        = new Object();
    private       DepthUpdate pendingDepthUpdate = null;

    // -------------------------------------------------------------------------
    // State flags — volatile, read across threads
    // -------------------------------------------------------------------------

    private volatile boolean engineReady    = false;
    private volatile boolean swapChainReady = false;
    private volatile boolean destroyed      = false;
    private volatile boolean animationEnabled = true;

    private float animationTime = 0f; // only on FilamentRenderThread

    private int viewportWidth  = 1;
    private int viewportHeight = 1;

    private static final float Z_NEAR = 0.085f;
    private static final float Z_FAR  = 20f;

    private final Form form;

    public void renderSynchronous(List<ARNode> nodes,
                                  float[] viewMatrix,
                                  float[] projMatrix,
                                  List<Plane> planes) {
        if (!engineReady || !swapChainReady || destroyed) return;
        if (filamentHandler == null) return;

        // Copy matrices before posting — GL thread arrays may be reused
        final float[] viewCopy = new float[16];
        final float[] projCopy = new float[16];
        System.arraycopy(viewMatrix, 0, viewCopy, 0, 16);
        System.arraycopy(projMatrix, 0, projCopy, 0, 16);
        final List<ARNode> nodesCopy = new ArrayList<>(nodes);
        final List<Plane> planesCopy = new ArrayList<>(planes);

        // Use CountDownLatch to block GL thread until Filament finishes
        java.util.concurrent.CountDownLatch latch =
            new java.util.concurrent.CountDownLatch(1);

        filamentHandler.post(() -> {
            try {
                synchronized (matrixLock) {
                    trackingPlanes = planesCopy;
                    sharedNodes = nodesCopy;
                }
                updateCameraFromARCore(viewCopy, projCopy);
                loadAndPositionNodes(nodesCopy);
                updateAnimations();

                long timestamp = System.nanoTime();
                if (renderer.beginFrame(swapChain, timestamp)) {
                    renderer.render(view);
                    renderer.endFrame();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Render error: " + e.getMessage(), e);
                safeEndFrame();
            } finally {
                latch.countDown();
            }
        });

        try {
            // Wait max 16ms — one frame budget
            latch.await(16, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ARFilamentRenderer(ComponentContainer container) {
        this.form = container.$form();
    }

    // =========================================================================
    // PUBLIC API — safe to call from any thread
    // =========================================================================

    /**
     * Phase 1. Starts FilamentRenderThread and initializes the Engine.
     * Call once — from onSurfaceCreated or onCreate.
     * Returns immediately; initialization runs on FilamentRenderThread.
     */
    public void initializeEngine() {
        if (engineReady || filamentThread != null) return;

        filamentThread = new HandlerThread("FilamentRenderThread");
        filamentThread.start();
        filamentHandler = new Handler(filamentThread.getLooper());

        filamentHandler.post(() -> {
            try {
                loadNativeLibraries();

                // Engine.create() with no args — Filament manages its own
                // EGL context internally on its backend thread
                engine = Engine.create();
                Log.d(LOG_TAG, "Engine created on FilamentRenderThread");

                renderer = engine.createRenderer();
                scene    = engine.createScene();
                view     = engine.createView();

                cameraEntity = EntityManager.get().create();
                camera = engine.createCamera(cameraEntity);
                view.setCamera(camera);
                view.setScene(scene);
                view.setViewport(new Viewport(0, 0, viewportWidth, viewportHeight));

                // Transparent clear — GLSurfaceView camera feed shows through
                Renderer.ClearOptions opts = new Renderer.ClearOptions();
                opts.clear           = true;
                opts.clearColor[0]   = 0f;
                opts.clearColor[1]   = 0f;
                opts.clearColor[2]   = 0f;
                opts.clearColor[3]   = 0f;
                renderer.setClearOptions(opts);

                materialProvider = new MaterialProvider(engine);
                assetLoader      = new AssetLoader(engine, materialProvider,
                    EntityManager.get());
                resourceLoader   = new ResourceLoader(engine);

                setupLighting();
                loadOcclusionMaterial();

                engineReady = true;
                Log.d(LOG_TAG, "Engine initialization complete");


            } catch (Exception e) {
                Log.e(LOG_TAG, "Engine init failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Phase 2. Creates the SwapChain from the filamentSurfaceView's Surface.
     * Call from SurfaceHolder.Callback.surfaceChanged.
     * Safe to call before engineReady — will wait for engine then proceed.
     */
    public void initializeSwapChain(final Surface surface,
                                    final int width, final int height) {
        if (filamentHandler == null) {
            Log.w(LOG_TAG, "initializeSwapChain called before thread started");
            return;
        }
        filamentHandler.post(() -> {
            if (!engineReady) {
                Log.w(LOG_TAG, "Engine not ready when SwapChain requested");
                return;
            }
            if (swapChain != null) {
                engine.destroySwapChain(swapChain);
                swapChain = null;
                swapChainReady = false;
            }
            swapChain = engine.createSwapChain(
                surface, SwapChain.CONFIG_TRANSPARENT);
            viewportWidth  = width;
            viewportHeight = height;
            view.setViewport(new Viewport(0, 0, width, height));
            swapChainReady = true;
            Log.d(LOG_TAG, "SwapChain ready: " + width + "x" + height);
        });
    }

    /**
     * Called when filamentSurfaceView's surface is destroyed.
     * Destroys SwapChain but keeps engine alive for recreation.
     */
    public void destroySwapChain() {
        if (filamentHandler == null) return;
        filamentHandler.post(() -> {
            if (swapChain != null && engine != null) {
                engine.destroySwapChain(swapChain);
                swapChain = null;
            }
            swapChainReady = false;
            Log.d(LOG_TAG, "SwapChain destroyed");
        });
    }

    /**
     * Called every frame from ARView3D.onDrawFrame (GL thread).
     * Non-blocking — writes shared state under lock and returns immediately.
     * Never calls any Filament API.
     */
    public void updateFrame(List<ARNode> modelNodes,
                            float[] viewMatrix,
                            float[] projMatrix,
                            List<Plane> tPlanes) {
        synchronized (matrixLock) {
            System.arraycopy(viewMatrix, 0, sharedView, 0, 16);
            System.arraycopy(projMatrix, 0, sharedProj, 0, 16);
            sharedNodes   = new ArrayList<>(modelNodes);
            matricesReady = true;
            trackingPlanes = tPlanes;
        }
    }

    /**
     * Called from ARView3D when new ARCore depth data is available.
     * Non-blocking — stores pending update for FilamentRenderThread to consume.
     */
    public void updateARCoreDepth(ByteBuffer depthData, float[] uvTransform,
                                  float near, float far,
                                  int depthWidth, int depthHeight) {
        // Copy the buffer — ARCore may reclaim it after this call returns
        ByteBuffer copy = ByteBuffer.allocateDirect(depthData.remaining());
        copy.put(depthData);
        copy.rewind();

        synchronized (depthLock) {
            pendingDepthUpdate = new DepthUpdate(
                copy, uvTransform, near, far, depthWidth, depthHeight);
        }
    }

    /**
     * Viewport update — called when surface dimensions change.
     */
    public void updateSurfaceDimensions(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewportWidth  = width;
        viewportHeight = height;
        if (filamentHandler != null) {
            filamentHandler.post(() -> {
                if (view != null) {
                    view.setViewport(new Viewport(0, 0, width, height));
                }
            });
        }
    }

    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
    }

    /**
     * Remove a node from the scene.
     * Posts to FilamentRenderThread — safe to call from any thread.
     */
    public void removeNode(ARNode node) {
        if (filamentHandler == null) return;
        filamentHandler.post(() -> {
            FilamentAsset asset = nodeAssetMap.remove(node);
            if (asset == null) return;
            scene.remove(asset.getRoot());
            for (int e : asset.getEntities()) scene.remove(e);
            assetLoader.destroyAsset(asset);
            Log.d(LOG_TAG, "Removed: " + node.NodeType());
        });
    }

    /**
     * Full cleanup. Posts all Filament destruction to FilamentRenderThread,
     * then shuts the thread down cleanly.
     */
    public void destroy() {
        if (destroyed) return;
        destroyed = true;

        if (filamentHandler == null) return;

        filamentHandler.post(() -> {

            if (engine == null) return;

            for (FilamentAsset asset : nodeAssetMap.values()) {
                scene.remove(asset.getRoot());
                for (int e : asset.getEntities()) scene.remove(e);
                assetLoader.destroyAsset(asset);
            }
            nodeAssetMap.clear();

            if (mainLightEntity != 0) {
                scene.remove(mainLightEntity);
                engine.destroyEntity(mainLightEntity);
            }
            if (arDepthTexture != null)  engine.destroyTexture(arDepthTexture);
            if (resourceLoader  != null) resourceLoader.destroy();
            if (assetLoader     != null) assetLoader.destroy();
            if (materialProvider != null) {
                materialProvider.destroyMaterials();
                materialProvider.destroy();
            }
            if (cameraEntity != 0) {
                engine.destroyCamera(camera);
                engine.destroyEntity(cameraEntity);
            }
            engine.destroyRenderer(renderer);
            engine.destroyView(view);
            engine.destroyScene(scene);
            if (swapChain != null) engine.destroySwapChain(swapChain);
            engine.destroy();
            engine = null;

            engineReady    = false;
            swapChainReady = false;
            Log.d(LOG_TAG, "Filament destroyed");
        });

        // Give the destruction post a moment to execute, then stop thread
        filamentThread.quitSafely();
        filamentThread = null;
        filamentHandler = null;
    }

    // =========================================================================
    // PRIVATE — all methods below run exclusively on FilamentRenderThread
    // =========================================================================

    private void loadNativeLibraries() {
        System.loadLibrary("filament-jni");
        System.loadLibrary("gltfio-jni");
        Log.d(LOG_TAG, "Native libraries loaded");
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
            Log.w(LOG_TAG, "Occlusion material unavailable: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Depth — called on FilamentRenderThread from renderCallback
    // -------------------------------------------------------------------------

    private void applyDepthUpdate(DepthUpdate depth) {
        try {
            if (arDepthTexture == null
                || arDepthTexture.getWidth(0)  != depth.width
                || arDepthTexture.getHeight(0) != depth.height) {

                if (arDepthTexture != null) engine.destroyTexture(arDepthTexture);

                arDepthTexture = new Texture.Builder()
                    .width(depth.width).height(depth.height).levels(1)
                    .format(Texture.InternalFormat.RG8)
                    .sampler(Texture.Sampler.SAMPLER_2D)
                    .usage(Texture.Usage.SAMPLEABLE)
                    .build(engine);

                occlusionAppliedToAllAssets = false;
                Log.d(LOG_TAG, "Depth texture: "
                    + depth.width + "x" + depth.height);
            }

            depth.data.rewind();
            arDepthTexture.setImage(engine, 0,
                new Texture.PixelBufferDescriptor(
                    depth.data,
                    Texture.Format.RG, Texture.Type.UBYTE,
                    4, 0, 0, depth.width, null, null));

            if (!depthDataReceived) {
                depthDataReceived = true;
                Log.d(LOG_TAG, "First depth frame");
            }

            updateOcclusionParameters(depth.near, depth.far, depth.uvTransform);

            if (!occlusionAppliedToAllAssets
                && occlusionMaterialInstance != null
                && !nodeAssetMap.isEmpty()) {
                applyOcclusionToAllAssets();
                occlusionAppliedToAllAssets = true;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Depth apply failed: " + e.getMessage(), e);
        }
    }

    private void updateOcclusionParameters(float near, float far, float[] t) {
        if (occlusionMaterialInstance == null || arDepthTexture == null) return;
        try {
            occlusionMaterialInstance.setParameter("nearPlane", near);
            occlusionMaterialInstance.setParameter("farPlane",  far);
            occlusionMaterialInstance.setParameter("occlusionBias", 0.01f);
            occlusionMaterialInstance.setParameter(
                "uvTransformRow0", t[0],  t[1],  t[2],  t[3]);
            occlusionMaterialInstance.setParameter(
                "uvTransformRow1", t[4],  t[5],  t[6],  t[7]);
            occlusionMaterialInstance.setParameter(
                "uvTransformRow2", t[8],  t[9],  t[10], t[11]);
            occlusionMaterialInstance.setParameter(
                "uvTransformRow3", t[12], t[13], t[14], t[15]);

            TextureSampler sampler = new TextureSampler(
                TextureSampler.MinFilter.LINEAR,
                TextureSampler.MagFilter.LINEAR,
                TextureSampler.WrapMode.CLAMP_TO_EDGE);
            occlusionMaterialInstance.setParameter(
                "depthTexture", arDepthTexture, sampler);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Occlusion params: " + e.getMessage());
        }
    }

    private void applyOcclusionToAllAssets() {
        RenderableManager rm = engine.getRenderableManager();
        for (FilamentAsset asset : nodeAssetMap.values()) {
            applyOcclusionToAsset(asset, rm);
        }
        Log.d(LOG_TAG, "Occlusion applied to all assets");
    }

    private void applyOcclusionToAsset(FilamentAsset asset, RenderableManager rm) {
        for (int entity : asset.getEntities()) {
            if (!rm.hasComponent(entity)) continue;
            int instance = rm.getInstance(entity);
            for (int i = 0; i < rm.getPrimitiveCount(instance); i++) {
                rm.setMaterialInstanceAt(instance, i, occlusionMaterialInstance);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Camera
    // -------------------------------------------------------------------------

    private void updateCameraFromARCore(float[] viewMatrix, float[] projMatrix) {
        if (camera == null) return;

        // Invert view matrix to get camera world transform
        float[] invView = new float[16];
        if (!Matrix.invertM(invView, 0, viewMatrix, 0)) return;

        lastCameraWorldPos = new float[]{invView[12], invView[13], invView[14]};

        // Set camera world transform directly — no vector extraction,
        // no lookAt reconstruction, no floating point round-trip
        TransformManager tm = engine.getTransformManager();
        int cameraInstance = tm.getInstance(cameraEntity);
        tm.setTransform(cameraInstance, invView);

        // Projection — extract near/far from ARCore matrix
        float near = projMatrix[14] / (projMatrix[10] - 1f);
        float far  = projMatrix[14] / (projMatrix[10] + 1f);

        double[] proj64 = new double[16];
        for (int i = 0; i < 16; i++) proj64[i] = projMatrix[i];
        camera.setCustomProjection(proj64, near, far);
    }

    public Collection<ARNode> getFilamentNodes(){
        return sharedNodes;
    }

    // -------------------------------------------------------------------------
    // Node processing
    // -------------------------------------------------------------------------

    private void loadAndPositionNodes(Collection<ARNode> nodes) {
        for (ARNode node : nodes) {

            if (!shouldRender(node)) continue;
            if (!nodeAssetMap.containsKey(node)) {
                // Only attempt load if node has a valid matrix
                if (((ARNodeBase) node).currentWorldMatrix == null) continue;
                try {
                    Log.e(LOG_TAG, "Trying to loading node " + node.Model());
                    loadModelForNode(node);
                } catch (IOException e) {
                    continue;
                }
            }
            FilamentAsset asset = nodeAssetMap.get(node);
            if (asset != null) {
                ARNodeBase base = (ARNodeBase) node;
                // Render if we have a valid world matrix — don't require anchor tracking
                if (base.currentWorldMatrix != null) {
                    applyNodeTransform(node, asset);
                }
            }
        }
    }

    private boolean shouldRender(ARNode node) {
        return node != null
            && node.Visible()
            && node.Model() != null
            && !node.Model().isEmpty();
    }

    /* update the model w/r to scale (first) and rotation */
    private void applyNodeTransform(ARNode node, FilamentAsset asset) {
        TransformManager tm = engine.getTransformManager();
        int rootInstance = tm.getInstance(asset.getRoot());
        if (rootInstance == 0) return;

        ARNodeBase base = (ARNodeBase) node;
        float[] t = base.getCurrentPosition();

        // planeFinder is null if occlusion isn't enabled for either depth or plane-based occlusion
        if (!depthDataReceived && planeFinder != null && !base.isBeingDragged) {
            float sphereRadius = ((ARNodeBase) node).getCollisionRadius();
            com.google.ar.core.Plane occludingPlane =
                planeFinder.findOccludingPlane(t, lastCameraWorldPos, sphereRadius);
            if (occludingPlane != null) {
                float[] hideMatrix = new float[16];
                Matrix.setIdentityM(hideMatrix, 0);
                hideMatrix[13] = -9999f;
                tm.setTransform(rootInstance, hideMatrix);
                return;
            }
        }

        float s = node.Scale();

        // Scale matrix
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, s, s, s);

        // Rotation matrix
        float[] rotMatrix = new float[16];
        Matrix.setIdentityM(rotMatrix, 0);
        quaternionToMatrix(base.getCurrentRotation(), rotMatrix);

        // Combine rotation * scale
        float[] modelMatrix = new float[16];
        Matrix.multiplyMM(modelMatrix, 0, rotMatrix, 0, scaleMatrix, 0);

        // Translation last — world space, unaffected by rotation/scale
        modelMatrix[12] = t[0];
        modelMatrix[13] = t[1];
        modelMatrix[14] = t[2];

        tm.setTransform(rootInstance, modelMatrix);

        Log.d(LOG_TAG, String.format(
            "applyNodeTransform: pos=[%.3f, %.3f, %.3f] scale=%.3f dragging=%b",
            t[0], t[1], t[2], s, base.isBeingDragged));
    }
    // -------------------------------------------------------------------------
    // Model loading
    // -------------------------------------------------------------------------

    private void loadModelForNode(ARNode node) throws IOException {
        String path = node.Model();
        Log.d(LOG_TAG, "Loading: " + path);

        ByteBuffer buffer = readAsset(path, false);
        if (buffer == null) throw new IOException("Not found: " + path);

        FilamentAsset asset = assetLoader.createAssetFromBinary(buffer);
        if (asset == null) throw new IOException("AssetLoader failed: " + path);

        scene.addEntity(asset.getRoot());
        for (int e : asset.getEntities()) {
            if (e != asset.getRoot()) scene.addEntity(e);
        }
        resourceLoader.loadResources(asset);

        try {
            // Reset transform to identity before reading bounds
            TransformManager tm = engine.getTransformManager();
            int rootInstance = tm.getInstance(asset.getRoot());
            float[] identity = new float[16];
            android.opengl.Matrix.setIdentityM(identity, 0);
            tm.setTransform(rootInstance, identity);

            com.google.android.filament.Box aabb = asset.getBoundingBox();
            float[] halfExtent = aabb.getHalfExtent();
            float radius = (float) Math.sqrt(
                halfExtent[0] * halfExtent[0] +
                    halfExtent[1] * halfExtent[1] +
                    halfExtent[2] * halfExtent[2]);

            ((ARNodeBase) node).setCollisionRadius(radius);
            ((ARNodeBase) node).updateCollisionShape();

            ARNodeBase base = (ARNodeBase) node;
            float visualRadius = radius * base.Scale();
            float[] pos = base.getCurrentPosition();
            if (pos[1] <= base.GROUND_LEVEL + 0.01f) {
                // only adjust if still at ground level — not already offset
                pos[1] = base.GROUND_LEVEL + visualRadius;
                base.setCurrentPosition(pos);
            }

        } catch (Exception e) {
            Log.w(LOG_TAG, "Could not compute mesh bounds: " + e.getMessage());
        }

        nodeAssetMap.put(node, asset);
        // rest of method continues...
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

    // -------------------------------------------------------------------------
    // Asset reading
    // -------------------------------------------------------------------------

    private ByteBuffer readAsset(String name, boolean isInternal)
        throws IOException {
        InputStream is = isInternal
            ? MediaUtil.getAssetsIgnoreCaseInputStream(form, name)
            : form.openAsset(name);
        if (is == null) throw new IOException("Not found: " + name);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        is.close();
        ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length);
        buf.put(bytes);
        buf.rewind();
        return buf;
    }

    // -------------------------------------------------------------------------
    // Math
    // -------------------------------------------------------------------------

    private void quaternionToMatrix(float[] q, float[] m) {
        float x=q[0], y=q[1], z=q[2], w=q[3];
        float x2=x+x, y2=y+y, z2=z+z;
        float xx=x*x2, xy=x*y2, xz=x*z2;
        float yy=y*y2, yz=y*z2, zz=z*z2;
        float wx=w*x2, wy=w*y2, wz=w*z2;
        m[0]=1-(yy+zz); m[1]=xy+wz;     m[2]=xz-wy;     m[3]=0;
        m[4]=xy-wz;     m[5]=1-(xx+zz); m[6]=yz+wx;     m[7]=0;
        m[8]=xz+wy;     m[9]=yz-wx;     m[10]=1-(xx+yy); m[11]=0;
        m[12]=0;        m[13]=0;        m[14]=0;          m[15]=1;
    }

    // -------------------------------------------------------------------------
    // Safety
    // -------------------------------------------------------------------------

    private void safeEndFrame() {
        try { if (renderer != null) renderer.endFrame(); }
        catch (Exception ignored) { }
    }
}