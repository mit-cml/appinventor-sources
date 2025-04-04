package com.google.appinventor.components.runtime;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Choreographer;
import android.util.Log;

// to manage surfaceview for arcore
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;


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
public class ARViewRender implements SurfaceHolder.Callback {
    private static final String LOG_TAG = ARView3D.class.getSimpleName();

    private final Form form;
    private final SurfaceView view;
    private final AssetManager assetManager;
    private final Choreographer.FrameCallback frameCallback;

    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private boolean surfaceReady = false;
    private boolean renderingActive = false;

    // Filament components
    private Engine engine;
    private final IRenderer irenderer;
    private Renderer filamentRenderer = null;
    private SwapChain swapChain;
    private View filamentView;
    private Scene scene;
    private Camera camera;

    // Add these as class members
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;



    // Add this method to clean up EGL
    private void releaseEGL() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            if (eglSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = EGL14.EGL_NO_SURFACE;
            }
            if (eglContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(eglDisplay, eglContext);
                eglContext = EGL14.EGL_NO_CONTEXT;
            }
            EGL14.eglTerminate(eglDisplay);
            eglDisplay = EGL14.EGL_NO_DISPLAY;
        }
    }
    /**
     * Constructs an ARViewRender object with SurfaceView.
     *
     * @param surfaceView SurfaceView for rendering
     * @param renderer Renderer implementation to receive callbacks
     * @param form Form context for asset access
     */
    public ARViewRender(SurfaceView surfaceView, final IRenderer irenderer, Form form) {
        this.form = form;
        this.assetManager = form.getAssets();
        this.view = surfaceView;
        this.irenderer = irenderer;

        // Set up the surface holder callback
        view.getHolder().addCallback(this);

        // Create a frame callback for rendering
        this.frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (renderingActive && surfaceReady) {
                    try {
                        // Make EGL context current
                        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);

                        // Try to clear with default framebuffer first
                       /* try {
                            // Clear the screen with default framebuffer
                            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            GLES30.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
                            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error clearing default framebuffer: " + e.getMessage());
                        }*/

                        // Call the renderer safely
                        try {
                            irenderer.onDrawFrame(ARViewRender.this);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error in onDrawFrame: " + e.getMessage());
                        }

                        // Swap buffers
                        EGL14.eglSwapBuffers(eglDisplay, eglSurface);

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error in doFrame: " + e.getMessage());
                    }

                    // Continue rendering loop
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
    }

    // In ARViewRender
    public EGLContext getEGLContext() {
        return eglContext;
    }

    // Add this method to initialize EGL
    private void initializeEGL(Surface surface) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("Unable to get EGL14 display");
        }

        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("Unable to initialize EGL14");
        }

        // Configure EGL for OpenGL ES 3.0
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)) {
            throw new RuntimeException("Unable to find a suitable EGLConfig");
        }

        // Create the context
        int[] contextAttribs = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE };
        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Unable to create EGL context");
        }

        // Create the surface
        int[] surfaceAttribs = { EGL14.EGL_NONE };
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttribs, 0);
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Unable to create EGL surface");
        }

        // Make the context current
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("Unable to make EGL context current");
        }
    }

    // SurfaceHolder.Callback implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface created");
        surfaceReady = true;

        initializeEGL(holder.getSurface());
        // Initialize Filament
        initializeFilament(holder.getSurface());

        // Notify the renderer
        irenderer.onSurfaceCreated(this);

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
        irenderer.onSurfaceChanged(this, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface destroyed");
        surfaceReady = false;

        // Stop rendering
        stopRendering();

        // Clean up Filament
        destroyFilament();

        // Clean up EGL
        releaseEGL();
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
        try {
            // Safely bind framebuffer (or default framebuffer if null)
            if (framebuffer == null) {
                // Just use default framebuffer (0)
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            } else {
                int fbId = framebuffer.getFrameBufferId();
                if (fbId > 0) {
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbId);
                } else {
                    // Invalid framebuffer ID, use default instead
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                    Log.w(LOG_TAG, "Invalid framebuffer ID " + fbId + ", using default");
                }
            }

            // Set viewport dimensions
            int width = (framebuffer != null) ? framebuffer.getWidth() : viewportWidth;
            int height = (framebuffer != null) ? framebuffer.getHeight() : viewportHeight;
            GLES30.glViewport(0, 0, width, height);

            // Clear the screen
            GLES30.glClearColor(r, g, b, a);
            GLES30.glDepthMask(true);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        } catch (Exception e) {
            // Log error but don't crash
            Log.e(LOG_TAG, "Error in clear(): " + e.getMessage());
        }
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
        try {
            int framebufferId;
            int viewportWidth;
            int viewportHeight;

            if (framebuffer == null) {
                framebufferId = 0;
                viewportWidth = this.viewportWidth;
                viewportHeight = this.viewportHeight;
            } else {
                framebufferId = framebuffer.getFrameBufferId();
                if (framebufferId <= 0) {
                    Log.w(LOG_TAG, "Invalid framebuffer ID: " + framebufferId + ", using default");
                    framebufferId = 0;
                }
                viewportWidth = framebuffer.getWidth();
                viewportHeight = framebuffer.getHeight();
            }

            Log.d(LOG_TAG, "viewport" + viewportWidth + " " + viewportHeight);

            // Try to bind framebuffer with error recovery
            try {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
                // Check error directly instead of throwing
                int error = GLES30.glGetError();
                if (error != GLES30.GL_NO_ERROR) {
                    Log.w(LOG_TAG, "glBindFramebuffer error: " + error + ", falling back to default framebuffer");
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception in glBindFramebuffer: " + e.getMessage());
                // Fall back to default framebuffer
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            }

            GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in useFramebuffer: " + e.getMessage());
        }
    }
}