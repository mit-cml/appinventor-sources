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

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.FilamentInstance;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;

import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ar.ARNodeBase;
import com.google.appinventor.components.runtime.ar.ModelNode;
import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.ar.core.Plane;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ARFilamentRenderer — renders animated glTF models using Filament 1.9.20.
 *
 * =========================================================================
 * THREADING MODEL
 * =========================================================================
 *
 *   FilamentRenderThread (HandlerThread)
 *     Owns all Filament objects. Executes all Filament API calls.
 *     Drives the render loop via renderSynchronous().
 *
 *   loadExecutor (2-thread pool)
 *     Performs blocking file I/O only. Never calls any Filament API.
 *     On completion posts GPU work back to FilamentRenderThread.
 *
 *   GL thread (GLSurfaceView.Renderer)
 *     Calls renderSynchronous() each frame with ARCore matrices.
 *     Returns immediately — never blocks, never calls Filament.
 *
 *   Main thread
 *     Calls initializeEngine(), initializeSwapChain(), destroySwapChain(),
 *     destroy(). All post to FilamentRenderThread and return immediately.
 *
 * =========================================================================
 * INSTANCING MODEL (Filament 1.9.20)
 * =========================================================================
 *
 *   modelBufferCache : path → ByteBuffer
 *     One disk read per unique model path. Shared via duplicate() — zero copy.
 *
 *   assetCache : path → FilamentAsset  (the PRIMARY asset)
 *     createAssetFromBinary + loadResources called exactly once per path.
 *     Mesh geometry and textures live on GPU once regardless of instance count.
 *
 *   nodeRootEntity : ARNode → int
 *     The root entity to apply world transforms to for each node.
 *     Primary node  → asset.getRoot()
 *     Instance node → FilamentInstance.getRoot()
 *     This is the ONLY map needed for per-frame transform updates.
 *
 *   nodeInstanceMap : ARNode → FilamentInstance
 *     Populated only for non-primary nodes (2nd..Nth instances of a model).
 *     Used exclusively for cleanup in removeNode/destroy.
 *
 *   nodeAssetMap : ARNode → FilamentAsset
 *     Reverse lookup: which primary asset does this node reference.
 *     Used in removeNode to determine if the last reference is gone.
 *
 * =========================================================================
 * WHY setParent IS NOT USED
 * =========================================================================
 *
 *   setParent(assetRoot, transformEntity) would make ALL instances of a
 *   model inherit from one transform — they would all move together.
 *   Instead, each node's root entity (asset.getRoot() or instance.getRoot())
 *   is independent in the scene graph. applyNodeTransform writes directly
 *   to that root, affecting only that node.
 *
 * =========================================================================
 * FRAME PACING
 * =========================================================================
 *
 *   renderInFlight flag ensures at most one render task is queued at a time.
 *   If the previous frame hasn't finished when the GL thread posts the next
 *   one, the new frame is dropped cleanly — no queue backup, no ANR.
 *
 *   beginFrame() can block on a GPU fence from the previous frame. On
 *   FilamentRenderThread that block stalls nothing that matters — the
 *   camera feed and UI run freely at full frame rate.
 */
public class ARFilamentRenderer {

    private static final String LOG_TAG = "ARFilamentRenderer";

    // -------------------------------------------------------------------------
    // Dedicated render thread — owns all Filament objects
    // -------------------------------------------------------------------------

    private HandlerThread filamentThread;
    private Handler       filamentHandler;

    // -------------------------------------------------------------------------
    // Background I/O — file reads only, never Filament API
    // -------------------------------------------------------------------------

    private final ExecutorService loadExecutor = Executors.newFixedThreadPool(2);

    // -------------------------------------------------------------------------
    // Filament core — only on FilamentRenderThread
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
    // Filament glTF subsystems — only on FilamentRenderThread
    // -------------------------------------------------------------------------

    private AssetLoader      assetLoader;
    private ResourceLoader   resourceLoader;
    private MaterialProvider materialProvider;

    // -------------------------------------------------------------------------
    // Asset caches — see class javadoc for ownership and threading rules
    // -------------------------------------------------------------------------

    /** Level 1: disk cache — path → raw GLB bytes. Written by loadExecutor. */
    private final Map<String, ByteBuffer>    modelBufferCache = new ConcurrentHashMap<>();

    /** Level 2: GPU cache — path → primary FilamentAsset. FilamentRenderThread only. */
    private final Map<String, FilamentAsset> assetCache       = new ConcurrentHashMap<>();

    /**
     * Per-node root entity for transform updates. FilamentRenderThread only.
     * Primary node  → asset.getRoot()
     * Instance node → FilamentInstance.getRoot()
     */
    private final Map<ARNode, Integer>          nodeRootEntity  = new ConcurrentHashMap<>();

    /**
     * Non-primary instances only — for cleanup. FilamentRenderThread only.
     * Not populated for the first/primary node of each model path.
     */
    private final Map<ARNode, FilamentInstance> nodeInstanceMap = new ConcurrentHashMap<>();

    /**
     * Every node → its primary FilamentAsset. FilamentRenderThread only.
     * Used in removeNode to check if the last reference is gone.
     */
    private final Map<ARNode, FilamentAsset>    nodeAssetMap    = new ConcurrentHashMap<>();

    private final Map<String, ArrayDeque<FilamentInstance>> instancePoolCache
        = new ConcurrentHashMap<>();

    // How many instances to pre-allocate per unique model path.
// Tune this to your max expected count per model.
    private static final int MAX_INSTANCES_PER_MODEL = 20;

    /** Guards against launching duplicate loads for the same node. */
    private final Set<ARNode> loadingNodes =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    // -------------------------------------------------------------------------
    // Depth occlusion — only on FilamentRenderThread
    // -------------------------------------------------------------------------

    private Texture          arDepthTexture;
    private MaterialInstance occlusionMaterialInstance;
    private boolean          depthDataReceived           = false;
    private boolean          occlusionAppliedToAllAssets = false;

    private float[] lastCameraWorldPos = {0f, 0f, 0f};

    private PlaneFinder planeFinder = null;

    public void setPlaneFinder(PlaneFinder finder) {
        this.planeFinder = finder;
    }

    List<Plane> trackingPlanes = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Shared frame state — written by GL thread, read on FilamentRenderThread
    // Guarded by matrixLock
    // -------------------------------------------------------------------------

    private final Object  matrixLock  = new Object();
    private final float[] sharedView  = new float[16];
    private final float[] sharedProj  = new float[16];
    private List<ARNode>  sharedNodes = new ArrayList<>();
    private boolean       matricesReady = false;

    // -------------------------------------------------------------------------
    // Pending depth update — GL thread writes, FilamentRenderThread consumes
    // Guarded by depthLock
    // -------------------------------------------------------------------------

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

    private final Object      depthLock         = new Object();
    private       DepthUpdate pendingDepthUpdate = null;

    // -------------------------------------------------------------------------
    // State flags
    // -------------------------------------------------------------------------

    private volatile boolean engineReady      = false;
    private volatile boolean swapChainReady   = false;
    private volatile boolean destroyed        = false;
    private volatile boolean animationEnabled = true;

    /** At most one render task in flight — prevents queue backup and ANR. */
    private volatile boolean renderInFlight = false;

    /** Kept in sync with initializeSwapChain — guards against resize mismatches. */
    private volatile int swapChainWidth  = 1;
    private volatile int swapChainHeight = 1;

    private float animationTime = 0f;  // only on FilamentRenderThread

    private int viewportWidth  = 1;
    private int viewportHeight = 1;

    private final Form form;

    // =========================================================================
    // Constructor
    // =========================================================================

    public ARFilamentRenderer(ComponentContainer container) {
        this.form = container.$form();
    }

    // =========================================================================
    // PUBLIC API — safe to call from any thread
    // =========================================================================

    /**
     * Called every frame from ARView3D.onDrawFrame (GL thread).
     *
     * Non-blocking. If the previous render task hasn't finished this frame
     * is dropped cleanly. The GL thread never waits on Filament.
     */
    public void renderSynchronous(List<ARNode> nodes,
                                  float[] viewMatrix,
                                  float[] projMatrix,
                                  List<Plane> planes) {
        if (!engineReady || !swapChainReady || destroyed) return;
        if (filamentHandler == null) return;
        if (renderInFlight) return;

        renderInFlight = true;

        final float[] viewCopy  = new float[16];
        final float[] projCopy  = new float[16];
        System.arraycopy(viewMatrix, 0, viewCopy, 0, 16);
        System.arraycopy(projMatrix, 0, projCopy, 0, 16);
        final List<ARNode> nodesCopy  = new ArrayList<>(nodes);
        final List<Plane>  planesCopy = new ArrayList<>(planes);

        filamentHandler.post(() -> {
            try {
                synchronized (matrixLock) {
                    trackingPlanes = planesCopy;
                    sharedNodes    = nodesCopy;
                }

                updateCameraFromARCore(viewCopy, projCopy);
                loadAndPositionNodes(nodesCopy);
                updateAnimations();

                DepthUpdate depth;
                synchronized (depthLock) {
                    depth = pendingDepthUpdate;
                    pendingDepthUpdate = null;
                }
                if (depth != null) applyDepthUpdate(depth);

                // Sync viewport to SwapChain dimensions — guards resize mismatches
                view.setViewport(new Viewport(0, 0, swapChainWidth, swapChainHeight));

                long timestamp = System.nanoTime();
                if (renderer.beginFrame(swapChain, timestamp)) {
                    renderer.render(view);
                    renderer.endFrame();
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Render error: " + e.getMessage(), e);
                safeEndFrame();
            } finally {
                renderInFlight = false;
            }
        });
    }

    /**
     * Phase 1: Start FilamentRenderThread and initialize the Engine.
     * Call once from onSurfaceCreated. Returns immediately.
     */
    public void initializeEngine() {
        if (engineReady || filamentThread != null) return;

        filamentThread = new HandlerThread("FilamentRenderThread");
        filamentThread.start();
        filamentHandler = new Handler(filamentThread.getLooper());

        filamentHandler.post(() -> {
            try {
                loadNativeLibraries();

                engine   = Engine.create();
                renderer = engine.createRenderer();
                scene    = engine.createScene();
                view     = engine.createView();

                cameraEntity = EntityManager.get().create();
                camera = engine.createCamera(cameraEntity);
                view.setCamera(camera);
                view.setScene(scene);
                view.setViewport(new Viewport(0, 0, viewportWidth, viewportHeight));

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
                Log.d(LOG_TAG, "Engine ready");

            } catch (Exception e) {
                Log.e(LOG_TAG, "Engine init failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Phase 2: Create SwapChain from the Filament SurfaceView's Surface.
     * Call from SurfaceHolder.Callback.surfaceChanged. Returns immediately.
     */
    public void initializeSwapChain(final Surface surface,
                                    final int width, final int height) {
        if (filamentHandler == null) {
            Log.w(LOG_TAG, "initializeSwapChain: thread not started");
            return;
        }
        swapChainWidth  = width;
        swapChainHeight = height;

        filamentHandler.post(() -> {
            if (!engineReady) {
                Log.w(LOG_TAG, "Engine not ready when SwapChain requested");
                return;
            }
            if (swapChain != null) {
                engine.destroySwapChain(swapChain);
                swapChain      = null;
                swapChainReady = false;
            }
            swapChain = engine.createSwapChain(surface, SwapChain.CONFIG_TRANSPARENT);
            viewportWidth  = width;
            viewportHeight = height;
            view.setViewport(new Viewport(0, 0, width, height));
            swapChainReady = true;
            Log.d(LOG_TAG, "SwapChain ready: " + width + "x" + height);
        });
    }

    /** Tear down the SwapChain on surface loss. Engine stays alive. */
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

    /** Non-blocking frame data update for use with a separate render loop. */
    public void updateFrame(List<ARNode> modelNodes,
                            float[] viewMatrix,
                            float[] projMatrix,
                            List<Plane> tPlanes) {
        synchronized (matrixLock) {
            System.arraycopy(viewMatrix, 0, sharedView, 0, 16);
            System.arraycopy(projMatrix, 0, sharedProj, 0, 16);
            sharedNodes    = new ArrayList<>(modelNodes);
            trackingPlanes = tPlanes;
            matricesReady  = true;
        }
    }

    /** Called from ARView3D when new ARCore depth data is available. */
    public void updateARCoreDepth(ByteBuffer depthData, float[] uvTransform,
                                  float near, float far,
                                  int depthWidth, int depthHeight) {
        ByteBuffer copy = ByteBuffer.allocateDirect(depthData.remaining());
        copy.put(depthData);
        copy.rewind();
        synchronized (depthLock) {
            pendingDepthUpdate = new DepthUpdate(
                copy, uvTransform, near, far, depthWidth, depthHeight);
        }
    }

    /** Called when surface dimensions change (rotation, keyboard). */
    public void updateSurfaceDimensions(int width, int height) {
        if (width <= 0 || height <= 0) return;
        swapChainWidth  = width;
        swapChainHeight = height;
        viewportWidth   = width;
        viewportHeight  = height;
        if (filamentHandler != null) {
            filamentHandler.post(() -> {
                if (view != null) view.setViewport(new Viewport(0, 0, width, height));
            });
        }
    }

    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
    }

    public Collection<ARNode> getFilamentNodes() {
        return sharedNodes;
    }

    /**
     * Remove a single node from the scene.
     *
     * Instance node: destroys the FilamentInstance (shared GPU data untouched).
     * Primary node:  destroys the FilamentAsset only when the last reference
     *                is gone (i.e. no other nodes share this model path).
     */
    public void removeNode(ARNode node) {
        if (filamentHandler == null) return;
        filamentHandler.post(() -> {
            nodeRootEntity.remove(node);

            FilamentInstance instance = nodeInstanceMap.remove(node);
            FilamentAsset    primary  = nodeAssetMap.remove(node);

            if (instance != null) {
                // Remove from scene — return to pool for reuse
                for (int e : instance.getEntities()) scene.remove(e);
                ArrayDeque<FilamentInstance> pool =
                    instancePoolCache.get(node.Model());
                if (pool != null) {
                    pool.add(instance);  // recycle — ready for next node
                    Log.d(LOG_TAG, "Instance returned to pool: " + node.Model()
                        + " poolSize=" + pool.size());
                }

            } else if (primary != null) {
                // Check if this is a fallback asset (not in assetCache)
                boolean isFallback = !assetCache.containsValue(primary);
                if (isFallback) {
                    scene.remove(primary.getRoot());
                    for (int e : primary.getEntities()) scene.remove(e);
                    assetLoader.destroyAsset(primary);
                    Log.d(LOG_TAG, "Fallback asset freed: " + node.Model());
                    return;
                }

                // Primary node — only destroy if last reference
                boolean stillInUse = nodeAssetMap.containsValue(primary);
                if (!stillInUse) {
                    instancePoolCache.remove(node.Model());
                    assetCache.values().remove(primary);
                    scene.remove(primary.getRoot());
                    for (int e : primary.getEntities()) scene.remove(e);
                    assetLoader.destroyAsset(primary);
                    Log.d(LOG_TAG, "Primary asset freed: " + node.Model());
                }
            }
        });
    }
    /** Full teardown. Posts Filament destruction then stops the thread. */
    public void destroy() {
        if (destroyed) return;
        destroyed = true;

        loadExecutor.shutdownNow();
        modelBufferCache.clear();
        loadingNodes.clear();

        if (filamentHandler == null) return;

        filamentHandler.post(() -> {
            if (engine == null) return;

            // Destroy non-primary instances first

            nodeInstanceMap.clear();
            nodeRootEntity.clear();
            nodeAssetMap.clear();

// Return all instances to scene removal — destroyAsset handles memory
            for (Map.Entry<ARNode, FilamentInstance> entry : nodeInstanceMap.entrySet()) {
                FilamentInstance inst = entry.getValue();
                for (int e : inst.getEntities()) scene.remove(e);
            }
            nodeInstanceMap.clear();
            instancePoolCache.clear();
            nodeRootEntity.clear();
            nodeAssetMap.clear();

// Then the existing loop:
            for (FilamentAsset asset : assetCache.values()) {
                scene.remove(asset.getRoot());
                for (int e : asset.getEntities()) scene.remove(e);
                assetLoader.destroyAsset(asset);  // frees all pre-allocated instances too
            }
            assetCache.clear();
            // Destroy primary assets — one per unique model path
            for (FilamentAsset asset : assetCache.values()) {
                scene.remove(asset.getRoot());
                for (int e : asset.getEntities()) scene.remove(e);
                assetLoader.destroyAsset(asset);
            }
            assetCache.clear();

            if (mainLightEntity != 0) {
                scene.remove(mainLightEntity);
                engine.destroyEntity(mainLightEntity);
            }
            if (arDepthTexture  != null)  engine.destroyTexture(arDepthTexture);
            if (resourceLoader  != null)  resourceLoader.destroy();
            if (assetLoader     != null)  assetLoader.destroy();
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

        filamentThread.quitSafely();
        filamentThread  = null;
        filamentHandler = null;
    }

    // =========================================================================
    // PRIVATE — all methods run on FilamentRenderThread unless noted
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
    // Depth
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
                Log.d(LOG_TAG, "Depth texture: " + depth.width + "x" + depth.height);
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
                && !assetCache.isEmpty()) {
                applyOcclusionToAllPrimaryAssets();
                occlusionAppliedToAllAssets = true;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Depth apply failed: " + e.getMessage(), e);
        }
    }

    private void updateOcclusionParameters(float near, float far, float[] t) {
        if (occlusionMaterialInstance == null || arDepthTexture == null) return;
        try {
            occlusionMaterialInstance.setParameter("nearPlane",     near);
            occlusionMaterialInstance.setParameter("farPlane",      far);
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
            Log.e(LOG_TAG, "Occlusion params failed: " + e.getMessage());
        }
    }

    /**
     * Apply occlusion to primary assets only.
     * Instances share the same material instances as their primary asset —
     * applying once to the primary covers all instances automatically.
     */
    private void applyOcclusionToAllPrimaryAssets() {
        RenderableManager rm = engine.getRenderableManager();
        for (FilamentAsset asset : assetCache.values()) {
            applyOcclusionToEntities(asset.getEntities(), rm);
        }
        Log.d(LOG_TAG, "Occlusion applied to all primary assets");
    }

    private void applyOcclusionToEntities(int[] entities, RenderableManager rm) {
        for (int entity : entities) {
            if (!rm.hasComponent(entity)) continue;
            int inst = rm.getInstance(entity);
            for (int i = 0; i < rm.getPrimitiveCount(inst); i++) {
                rm.setMaterialInstanceAt(inst, i, occlusionMaterialInstance);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Camera
    // -------------------------------------------------------------------------

    private void updateCameraFromARCore(float[] viewMatrix, float[] projMatrix) {
        if (camera == null) return;

        float[] invView = new float[16];
        if (!Matrix.invertM(invView, 0, viewMatrix, 0)) return;

        lastCameraWorldPos = new float[]{ invView[12], invView[13], invView[14] };

        TransformManager tm = engine.getTransformManager();
        tm.setTransform(tm.getInstance(cameraEntity), invView);

        float near = projMatrix[14] / (projMatrix[10] - 1f);
        float far  = projMatrix[14] / (projMatrix[10] + 1f);

        double[] proj64 = new double[16];
        for (int i = 0; i < 16; i++) proj64[i] = projMatrix[i];
        camera.setCustomProjection(proj64, near, far);
    }

    // -------------------------------------------------------------------------
    // Node processing
    // -------------------------------------------------------------------------

    private void loadAndPositionNodes(Collection<ARNode> nodes) {
        for (ARNode node : nodes) {
            if (!shouldRender(node)) continue;

            if (!nodeRootEntity.containsKey(node) && !loadingNodes.contains(node)) {
                if (((ARNodeBase) node).currentWorldMatrix == null) continue;
                loadingNodes.add(node);
                loadModelAsync(node);
            }

            if (nodeRootEntity.containsKey(node)
                && ((ARNodeBase) node).currentWorldMatrix != null) {
                applyNodeTransform(node);
            }
        }
    }

    private boolean shouldRender(ARNode node) {
        return node != null
            && node.Visible()
            && node.Model() != null
            && !node.Model().isEmpty();
    }

    /**
     * Writes the node's world transform to its root entity.
     *
     * nodeRootEntity holds either asset.getRoot() (primary) or
     * FilamentInstance.getRoot() (instance). Both are independent entities
     * in the scene graph — writing here moves only this node.
     */
    private void applyNodeTransform(ARNode node) {
        Integer rootEntity = nodeRootEntity.get(node);
        if (rootEntity == null) return;

        TransformManager tm = engine.getTransformManager();
        int tmInst = tm.getInstance(rootEntity);
        if (tmInst == 0) return;

        ARNodeBase base = (ARNodeBase) node;
        float[] t = base.getCurrentPosition();

        if (!depthDataReceived && planeFinder != null && !base.isBeingDragged) {
            float radius = base.getCollisionRadius();
            com.google.ar.core.Plane plane =
                planeFinder.findOccludingPlane(t, lastCameraWorldPos, radius);
            if (plane != null) {
                float[] hide = new float[16];
                Matrix.setIdentityM(hide, 0);
                hide[13] = -9999f;
                tm.setTransform(tmInst, hide);
                return;
            }
        }

        float s = node.Scale();

        float[] scale = new float[16];
        Matrix.setIdentityM(scale, 0);
        Matrix.scaleM(scale, 0, s, s, s);

        float[] rot = new float[16];
        Matrix.setIdentityM(rot, 0);
        quaternionToMatrix(base.getCurrentRotation(), rot);

        float[] model = new float[16];
        Matrix.multiplyMM(model, 0, rot, 0, scale, 0);
        model[12] = t[0];
        model[13] = t[1];
        model[14] = t[2];

        tm.setTransform(tmInst, model);
    }

    // -------------------------------------------------------------------------
    // Model loading — two phases
    // -------------------------------------------------------------------------

    /**
     * Phase 1 — loadExecutor (file I/O, never touches Filament).
     *
     * Returns cached ByteBuffer or reads from disk. Calls duplicate() to
     * share the backing memory with zero copy, then posts Phase 2.
     */
    private void loadModelAsync(ARNode node) {
        final String path = node.Model();

        loadExecutor.submit(() -> {
            try {
                ByteBuffer buffer = modelBufferCache.get(path);
                if (buffer == null) {
                    buffer = readAsset(path, false);
                    if (buffer == null) {
                        Log.w(LOG_TAG, "Asset not found: " + path);
                        loadingNodes.remove(node);
                        return;
                    }
                    modelBufferCache.put(path, buffer);
                    Log.d(LOG_TAG, "Disk read: " + path
                        + " (" + buffer.capacity() + " bytes)");
                } else {
                    Log.d(LOG_TAG, "Buffer cache hit: " + path);
                }

                final ByteBuffer nodeBuf = buffer.duplicate();
                nodeBuf.rewind();

                filamentHandler.post(() -> {
                    try {
                        createNodeOnRenderThread(node, nodeBuf, path);
                    } finally {
                        loadingNodes.remove(node);
                    }
                });

            } catch (Exception e) {
                Log.w(LOG_TAG, "Load failed: " + path + " — " + e.getMessage());
                loadingNodes.remove(node);
            }
        });
    }

    /**
     * Phase 2 — FilamentRenderThread (GPU work only).
     *
     * FIRST NODE for a model path:
     *   createAssetFromBinary → full GPU upload (mesh + textures)
     *   loadResources         → uploads embedded images
     *   Cached in assetCache. Root entity = asset.getRoot().
     *
     * SUBSEQUENT NODES for the same path:
     *   createInstance(primaryAsset) → zero GPU upload
     *   Shares all GPU buffers with the primary asset.
     *   Has its own independent root entity, transform, and animator.
     *   Root entity = instance.getRoot().
     *
     *   CRITICAL: do NOT call loadResources on an instance — resources
     *   are owned by the primary asset and shared automatically.
     */
    private void createNodeOnRenderThread(ARNode node, ByteBuffer buffer, String path) {
        try {
            FilamentAsset primary = assetCache.get(path);
            int rootEntity;

            if (primary == null) {
                // ── First node for this path — full GPU upload + pre-allocate pool ──

                // Allocate the instance array upfront — size determines max instances
                FilamentInstance[] instances = new FilamentInstance[MAX_INSTANCES_PER_MODEL];

                buffer.rewind();
                primary = assetLoader.createInstancedAsset(buffer, instances);
                if (primary == null) {
                    Log.e(LOG_TAG, "createInstancedAsset returned null: " + path);
                    return;
                }

                // Add primary asset entities to scene
                scene.addEntity(primary.getRoot());
                for (int e : primary.getEntities()) {
                    if (e != primary.getRoot()) scene.addEntity(e);
                }

                // loadResources uploads textures — called once, shared by all instances
                resourceLoader.loadResources(primary);

                assetCache.put(path, primary);

                // Build the pool from the pre-allocated instances.
                // instances[0] is typically the primary's own instance — skip it
                // and use primary.getRoot() for the first node directly.
                ArrayDeque<FilamentInstance> pool = new ArrayDeque<>();
                for (FilamentInstance inst : instances) {
                    if (inst != null) {
                        pool.add(inst);
                    }
                }
                instancePoolCache.put(path, pool);

                // First node uses the primary asset root directly
                rootEntity = primary.getRoot();

                computeBoundsAndAdjustY(node, primary, primary.getRoot());

                if (depthDataReceived && occlusionMaterialInstance != null) {
                    applyOcclusionToEntities(primary.getEntities(),
                        engine.getRenderableManager());
                }

                Log.d(LOG_TAG, "GPU upload: " + path
                    + " poolSize=" + pool.size()
                    + " entities=" + primary.getEntities().length);

            } else {
                // ── Subsequent node — pull from pre-allocated pool ────────────────

                ArrayDeque<FilamentInstance> pool = instancePoolCache.get(path);
                FilamentInstance instance = (pool != null) ? pool.poll() : null;

                if (instance == null) {
                    // Pool exhausted — fall back to a fresh asset load
                    Log.w(LOG_TAG, "Instance pool exhausted for: " + path
                        + " — falling back to createAssetFromBinary");
                    buffer.rewind();
                    FilamentAsset fallback = assetLoader.createAssetFromBinary(buffer);
                    if (fallback == null) {
                        Log.e(LOG_TAG, "Fallback createAssetFromBinary failed: " + path);
                        return;
                    }
                    scene.addEntity(fallback.getRoot());
                    for (int e : fallback.getEntities()) {
                        if (e != fallback.getRoot()) scene.addEntity(e);
                    }
                    resourceLoader.loadResources(fallback);
                    rootEntity = fallback.getRoot();
                    // Track separately — this fallback asset needs its own destroy
                    nodeAssetMap.put(node, fallback);
                    computeBoundsAndAdjustY(node, fallback, fallback.getRoot());
                    nodeRootEntity.put(node, rootEntity);
                    return;
                }

                // Add this instance's entities to the scene
                scene.addEntity(instance.getRoot());
                for (int e : instance.getEntities()) {
                    if (e != instance.getRoot()) scene.addEntity(e);
                }

                nodeInstanceMap.put(node, instance);
                rootEntity = instance.getRoot();

                computeBoundsAndAdjustY(node, primary, instance.getRoot());

                Log.d(LOG_TAG, "Instance from pool: " + path
                    + " remaining=" + pool.size());
            }

            nodeRootEntity.put(node, rootEntity);
            nodeAssetMap.put(node, primary);

        } catch (Exception e) {
            Log.e(LOG_TAG, "createNodeOnRenderThread failed: " + path
                + " — " + e.getMessage(), e);
        }
    }
    /**
     * Computes bounding sphere radius and adjusts Y above ground level.
     *
     * @param isFirstInstance when true, resets the asset transform to identity
     *   before measuring so any previous transform doesn't contaminate bounds.
     *   For subsequent instances the primary asset transform may already be in
     *   use by another node, so the reset is skipped.
     */
    private void computeBoundsAndAdjustY(ARNode node, FilamentAsset asset,
                                         int rootEntityForMeasurement) {
        try {
            TransformManager tm = engine.getTransformManager();
            float[] identity = new float[16];
            Matrix.setIdentityM(identity, 0);
            tm.setTransform(tm.getInstance(rootEntityForMeasurement), identity);

            com.google.android.filament.Box aabb = asset.getBoundingBox();
            float[] half = aabb.getHalfExtent();
            float radius = (float) Math.sqrt(
                half[0] * half[0] + half[1] * half[1] + half[2] * half[2]);

            ARNodeBase base = (ARNodeBase) node;
            base.setCollisionRadius(radius);
            base.updateCollisionShape();

            float[] pos = base.getCurrentPosition();
            if (pos[1] <= base.GROUND_LEVEL + 0.01f) {
                pos[1] = base.GROUND_LEVEL + (radius * base.Scale());
                base.setCurrentPosition(pos);
            }

        } catch (Exception e) {
            Log.w(LOG_TAG, "Bounds failed: " + e.getMessage());
        }
    }

    private int countInstances(String path) {
        int n = 0;
        for (Map.Entry<ARNode, FilamentAsset> e : nodeAssetMap.entrySet()) {
            if (e.getKey().Model().equals(path)) n++;
        }
        return n;
    }

    // -------------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------------

    /**
     * Animate primary assets and all live instances.
     *
     * In Filament 1.9.20 FilamentInstance has its own Animator, allowing
     * each instance to run independently. Here all share the same clock
     * for synchronized behavior (e.g. all bowling pins animate together).
     *
     * Calling applyAnimation on the primary asset's Animator drives the
     * primary node. Each non-primary instance must be driven separately
     * via its own Animator.
     */
    private void updateAnimations() {
        if (!animationEnabled) return;
        animationTime += 1f / 60f;

        // Animate primary assets
        for (FilamentAsset asset : assetCache.values()) {
            Animator anim = asset.getAnimator();
            if (anim != null && anim.getAnimationCount() > 0) {
                anim.applyAnimation(0, animationTime);
                anim.updateBoneMatrices();
            }
        }

        // Animate in-use instances (those in nodeInstanceMap, not the pool)
        for (FilamentInstance instance : nodeInstanceMap.values()) {
            Animator anim = instance.getAnimator();
            if (anim != null && anim.getAnimationCount() > 0) {
                anim.applyAnimation(0, animationTime);
                anim.updateBoneMatrices();
            }
        }
    }
    // -------------------------------------------------------------------------
    // Asset reading — runs on loadExecutor, never on FilamentRenderThread
    // -------------------------------------------------------------------------

    /**
     * Reads a complete file into a direct ByteBuffer using a chunked loop.
     *
     * InputStream.available() is unreliable for compressed Android assets
     * and readAllBytes() requires API 33. This loop reads until EOF.
     */
    private ByteBuffer readAsset(String name, boolean isInternal) throws IOException {
        if (name.startsWith("//")) name = name.substring(2);

        InputStream is = isInternal
            ? MediaUtil.getAssetsIgnoreCaseInputStream(form, name)
            : form.openAsset(name);
        if (is == null) throw new IOException("Asset not found: " + name);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = is.read(chunk)) != -1) {
                baos.write(chunk, 0, n);
            }
            byte[] bytes = baos.toByteArray();
            ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length);
            buf.put(bytes);
            buf.rewind();
            Log.d(LOG_TAG, "Read " + bytes.length + " bytes: " + name);
            return buf;
        } finally {
            is.close();
        }
    }

    // -------------------------------------------------------------------------
    // Math
    // -------------------------------------------------------------------------

    private void quaternionToMatrix(float[] q, float[] m) {
        float x = q[0], y = q[1], z = q[2], w = q[3];
        float x2 = x+x, y2 = y+y, z2 = z+z;
        float xx = x*x2, xy = x*y2, xz = x*z2;
        float yy = y*y2, yz = y*z2, zz = z*z2;
        float wx = w*x2, wy = w*y2, wz = w*z2;
        m[0]  = 1-(yy+zz);  m[1]  = xy+wz;      m[2]  = xz-wy;      m[3]  = 0;
        m[4]  = xy-wz;      m[5]  = 1-(xx+zz);  m[6]  = yz+wx;      m[7]  = 0;
        m[8]  = xz+wy;      m[9]  = yz-wx;      m[10] = 1-(xx+yy);  m[11] = 0;
        m[12] = 0;          m[13] = 0;          m[14] = 0;           m[15] = 1;
    }

    // -------------------------------------------------------------------------
    // Safety
    // -------------------------------------------------------------------------

    private void safeEndFrame() {
        try { if (renderer != null) renderer.endFrame(); }
        catch (Exception ignored) { }
    }
}