package com.google.appinventor.components.runtime;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.util.Log;
import android.view.Choreographer;

import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityManager;
import com.google.android.filament.Renderer;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;

// Add these imports
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

/**
 * A render context that uses GLSurfaceView.
 */
public class ARViewRender implements SurfaceHolder.Callback {
    private static final String LOG_TAG = ARView3D.class.getSimpleName();

    private final Form form;
    private final GLSurfaceView glView;
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

    /**
     * Constructs an ARViewRender object with GLSurfaceView.
     *
     * @param glSurfaceView GLSurfaceView for rendering
     * @param renderer Renderer implementation to receive callbacks
     * @param form Form context for asset access
     */
    public ARViewRender(GLSurfaceView glSurfaceView, final IRenderer irenderer, Form form) {
        this.form = form;
        this.assetManager = form.getAssets();
        this.glView = glSurfaceView;
        this.irenderer = irenderer;

        // Configure the GLSurfaceView
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // Set up the GLSurfaceView renderer
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                surfaceReady = true;
                // Initialize Filament with the surface
                initializeFilament(glSurfaceView.getHolder().getSurface());
                // Notify the renderer
                irenderer.onSurfaceCreated(ARViewRender.this);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                viewportWidth = width;
                viewportHeight = height;

                // Update Filament view
                if (filamentView != null) {
                    filamentView.setViewport(
                            new com.google.android.filament.Viewport(0, 0, width, height));
                }

                // Notify the renderer
                irenderer.onSurfaceChanged(ARViewRender.this, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if (surfaceReady) {
                    try {
                        // Call the renderer
                        irenderer.onDrawFrame(ARViewRender.this);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error in onDrawFrame: " + e.getMessage());
                    }
                }
            }
        });

        // Set the render mode to continuously
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Create a frame callback - will only be used if needed
        this.frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                // This is optional now as GLSurfaceView handles the render loop
                if (renderingActive && surfaceReady) {
                    glSurfaceView.requestRender();
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
    }

    // SurfaceHolder.Callback implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface created");
        // GLSurfaceView handles this now
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOG_TAG, "Surface changed: " + width + "x" + height);
        // GLSurfaceView handles this now
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "Surface destroyed");
        surfaceReady = false;

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
            int cameraEntity = EntityManager.get().create();
            camera = engine.createCamera(cameraEntity);

            // Create view
            filamentView = engine.createView();
            filamentView.setScene(scene);
            filamentView.setCamera(camera);
            filamentView.setViewport(
                    new Viewport(0, 0, viewportWidth, viewportHeight));

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
            // GLSurfaceView handles rendering loop now
            Log.d(LOG_TAG, "Rendering started");
        }
    }

    /**
     * Stop the rendering loop
     */
    public void stopRendering() {
        if (renderingActive) {
            renderingActive = false;
            Log.d(LOG_TAG, "Rendering stopped");
        }
    }

    /**
     * Draw a mesh with a shader
     */
    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }

    public void draw(Mesh mesh, ARFrameBuffer frameBuffer) {
        draw(mesh, /* shader */ null, frameBuffer);
    }

    /**
     * Draw a mesh with a shader to a framebuffer
     */
    public void draw(Mesh mesh, Shader shader, ARFrameBuffer framebuffer) {
        useFramebuffer(framebuffer);
        if (shader != null) {
            shader.lowLevelUse();
        }
        mesh.lowLevelDraw();
    }

    /**
     * Clear the screen or framebuffer
     */
    public void clear(ARFrameBuffer framebuffer, float r, float g, float b, float a) {
        try {
            useFramebuffer(framebuffer);

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
    public Engine getFilamentEngine() {
        return engine;
    }

    /**
     * Get the Filament scene
     */
    public Scene getFilamentScene() {
        return scene;
    }

    /**
     * Get the Filament camera
     */
    public Camera getFilamentCamera() {
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

    public GLSurfaceView getGLSurfaceView() {
        return glView;
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

            Log.d(LOG_TAG, "binding to framebuffer, viewport " + framebufferId + " " + viewportWidth + " " + viewportHeight);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
            GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception in useFramebuffer: " + e.getMessage());
        }
    }
}