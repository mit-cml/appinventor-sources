package com.google.appinventor.components.runtime;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Choreographer;
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
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;

/**
 * A render context that uses SurfaceView instead of GLSurfaceView.
 */
public class ARViewRender implements SurfaceHolder.Callback, Renderer {
    private static final String LOG_TAG = ARView3D.class.getSimpleName();

    private final Form form;
    private final SurfaceView view;
    private final AssetManager assetManager;
    private final Renderer renderer;
    private final Choreographer.FrameCallback frameCallback;

    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private boolean surfaceReady = false;
    private boolean renderingActive = false;

    // Filament components
    private Engine engine;
    private Renderer filamentRenderer;
    private SwapChain swapChain;
    private View filamentView;
    private Scene scene;
    private Camera camera;

    /**
     * Constructs an ARViewRender object with SurfaceView.
     *
     * @param surfaceView SurfaceView for rendering
     * @param renderer Renderer implementation to receive callbacks
     * @param form Form context for asset access
     */
    public ARViewRender(SurfaceView surfaceView, final Renderer renderer, Form form) {
        this.form = form;
        this.assetManager = form.getAssets();
        this.view = surfaceView;
        this.renderer = renderer;

        // Set up the surface holder callback
        view.getHolder().addCallback(this);

        // Create a frame callback for rendering
        this.frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (renderingActive && surfaceReady) {
                    // Clear the screen
                    clear(null, 0f, 0f, 0f, 1f);

                    // Call the renderer
                    renderer.onDrawFrame(ARViewRender.this);

                    // Schedule the next frame
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
    }

    // SurfaceHolder.Callback implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface created");
        surfaceReady = true;

        // Initialize Filament
        initializeFilament(holder.getSurface());

        // Notify the renderer
        renderer.onSurfaceCreated(this);

        // Start the rendering loop
        startRendering();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOG_TAG, "Surface changed: " + width + "x" + height);
        viewportWidth = width;
        viewportHeight = height;

        // Update Filament view
        if (filamentView != null) {
            filamentView.setViewport(
                    new com.google.android.filament.Viewport(0, 0, width, height));
        }

        // Notify the renderer
        renderer.onSurfaceChanged(this, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface destroyed");
        surfaceReady = false;

        // Stop rendering
        stopRendering();

        // Clean up Filament
        destroyFilament();
    }

    /**
     * Initialize Filament components
     */
    private void initializeFilament(Surface surface) {

        initializeFilamentLibs();
        try {
            // Create the engine
            engine = Engine.create();

            // Create renderer, scene, and camera
            filamentRenderer = engine.createRenderer();
            scene = engine.createScene();

            // Create camera entity
            int cameraEntity = com.google.android.filament.EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);

            // Create view
            filamentView = engine.createView();
            filamentView.setScene(scene);
            filamentView.setCamera(camera);
            filamentView.setViewport(
                    new com.google.android.filament.Viewport(0, 0, viewportWidth, viewportHeight));

            // Create swap chain
            swapChain = engine.createSwapChain(surface);

            Log.d(LOG_TAG, "Filament initialized successfully");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to initialize Filament", e);
        }
    }

    private void initializeFilamentLibs() {
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
     * Clean up Filament resources
     */
    private void destroyFilament() {
        if (engine != null) {
            if (swapChain != null) {
                engine.destroySwapChain(swapChain);
                swapChain = null;
            }

            if (filamentView != null) {
                engine.destroyView(filamentView);
                filamentView = null;
            }

            if (camera != null) {
                engine.destroyCamera(camera);
                camera = null;
            }

            if (scene != null) {
                engine.destroyScene(scene);
                scene = null;
            }

            if (filamentRenderer != null) {
                engine.destroyRenderer(filamentRenderer);
                filamentRenderer = null;
            }

            engine.destroy();
            engine = null;

            Log.d(LOG_TAG, "Filament resources destroyed");
        }
    }

    /**
     * Start the rendering loop
     */
    public void startRendering() {
        if (!renderingActive) {
            renderingActive = true;
            Choreographer.getInstance().postFrameCallback(frameCallback);
            Log.d(LOG_TAG, "Rendering started");
        }
    }

    /**
     * Stop the rendering loop
     */
    public void stopRendering() {
        if (renderingActive) {
            renderingActive = false;
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            Log.d(LOG_TAG, "Rendering stopped");
        }
    }

    /**
     * Draw a mesh with a shader
     */
    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }

    /**
     * Draw a mesh with a shader to a framebuffer
     */
    public void draw(Mesh mesh, Shader shader, ARFrameBuffer framebuffer) {
        useFramebuffer(framebuffer);
        shader.lowLevelUse();
        mesh.lowLevelDraw();
    }

    /**
     * Clear the screen or framebuffer
     */
    public void clear(ARFrameBuffer framebuffer, float r, float g, float b, float a) {
        useFramebuffer(framebuffer);
        GLES30.glClearColor(r, g, b, a);
        GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
        GLES30.glDepthMask(true);
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
    }

    /**
     * Begin a Filament frame
     */
    public boolean beginFrame() {
        if (engine != null && filamentRenderer != null && swapChain != null) {
            return filamentRenderer.beginFrame(swapChain, 0);
        }
        return false;
    }

    /**
     * Render the Filament view
     */
    public void renderView() {
        if (filamentRenderer != null && filamentView != null) {
            filamentRenderer.render(filamentView);
        }
    }

    /**
     * End a Filament frame
     */
    public void endFrame() {
        if (filamentRenderer != null) {
            filamentRenderer.endFrame();
        }
    }

    /**
     * Get the Filament engine
     */
    public com.google.android.filament.Engine getFilamentEngine() {
        return engine;
    }

    /**
     * Get the Filament scene
     */
    public com.google.android.filament.Scene getFilamentScene() {
        return scene;
    }

    /**
     * Get the Filament camera
     */
    public com.google.android.filament.Camera getFilamentCamera() {
        return camera;
    }

    /**
     * Renderer interface
     */
    public interface IRenderer {
        void onSurfaceCreated(ARViewRender render);
        void onSurfaceChanged(ARViewRender render, int width, int height);
        void onDrawFrame(ARViewRender render);
    }

    /* package-private */
    AssetManager getAssets() {
        return assetManager;
    }

    public Form getForm() {
        return form;
    }

    public SurfaceView getSurfaceView() {
        return view;
    }

    private void useFramebuffer(ARFrameBuffer framebuffer) {
        int framebufferId;
        int viewportWidth;
        int viewportHeight;
        if (framebuffer == null) {
            framebufferId = 0;
            viewportWidth = this.viewportWidth;
            viewportHeight = this.viewportHeight;
        } else {
            framebufferId = framebuffer.getFrameBufferId();
            viewportWidth = framebuffer.getWidth();
            viewportHeight = framebuffer.getHeight();
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
    }
}