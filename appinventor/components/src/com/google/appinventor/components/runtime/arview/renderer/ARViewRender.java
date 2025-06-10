package com.google.appinventor.components.runtime.arview.renderer;

import com.google.appinventor.components.runtime.*;
import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Choreographer;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import com.google.appinventor.components.runtime.ar.Framebuffer;
import com.google.appinventor.components.runtime.ar.Mesh;
import com.google.appinventor.components.runtime.ar.Shader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
//import com.google.appinventor.components.annotations.UsesAssets;
/**
 * A render context that uses GLSurfaceView for ARCore compatibility,
 * with optional Choreographer support for additional frame timing control.
 */

public class ARViewRender {
    private static final String LOG_TAG = ARViewRender.class.getSimpleName();

    private final Form form;
    private final GLSurfaceView glView;
    private final AssetManager assetManager;
    private final Choreographer.FrameCallback frameCallback;

    private int viewportWidth = 1;
    private int viewportHeight = 1;
    private boolean surfaceReady = false;
    private boolean useChoreographer = false;

    /**
     * Constructs an ARViewRender object with GLSurfaceView.
     *
     * @param glSurfaceView GLSurfaceView for rendering
     * @param renderer Renderer implementation to receive callbacks
     * @param form Form context for asset access
     */
    public ARViewRender(GLSurfaceView glSurfaceView, final IRenderer renderer, Form form) {
        this.form = form;
        this.assetManager = form.getAssets();
        this.glView = glSurfaceView;

        // Configure the GLSurfaceView - these settings are crucial for ARCore
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // Set up the GLSurfaceView renderer
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                Log.d(LOG_TAG, "GLSurfaceView.onSurfaceCreated called");
                surfaceReady = true;

                // Set up default OpenGL state
                GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES30.glEnable(GLES30.GL_BLEND);
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

                // Notify the renderer
                renderer.onSurfaceCreated(ARViewRender.this);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                Log.d(LOG_TAG, "GLSurfaceView.onSurfaceChanged: " + width + "x" + height);
                viewportWidth = width;
                viewportHeight = height;

                // Notify the renderer
                renderer.onSurfaceChanged(ARViewRender.this, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if (surfaceReady) {
                    try {
                        // Clear with full transparency to start fresh
                        clear(null, 0.0f, 1.0f, 0.0f, 0.0f);

                        // Call the renderer
                        renderer.onDrawFrame(ARViewRender.this);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error in onDrawFrame: " + e.getMessage(), e);
                    }
                }
            }
        });

        // Set the render mode to continuously render
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setWillNotDraw(false);

        // Create a frame callback for optional Choreographer timing
        this.frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (surfaceReady && useChoreographer) {
                    try {
                        // Request a render from GLSurfaceView
                        glSurfaceView.requestRender();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error in choreographer callback: " + e.getMessage(), e);
                    }

                    // Continue the choreographer loop if active
                    if (useChoreographer) {
                        Choreographer.getInstance().postFrameCallback(this);
                    }
                }
            }
        };
    }

    /**
     * Start using Choreographer for frame timing
     */
   /* public void startChoreographer() {
        useChoreographer = true;
        Choreographer.getInstance().postFrameCallback(frameCallback);
        Log.d(LOG_TAG, "Choreographer started");
    }
*/
    /**
     * Stop using Choreographer for frame timing
     */
   /* public void stopChoreographer() {
        useChoreographer = false;
        Log.d(LOG_TAG, "Choreographer stopped");
    }
*/
    /**
     * Draw a mesh with a shader
     */
    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }

    /**
     * Draw a mesh with a shader to a framebuffer
     */
    public void draw(Mesh mesh, Shader shader, Framebuffer arframebuffer) {
        if (mesh == null) {
            Log.e(LOG_TAG, "Cannot draw null mesh");
            return;
        }

        if (shader == null) {
            Log.e(LOG_TAG, "Cannot draw with null shader");
            return;
        }


        EGLContext currentGLContext = EGL14.eglGetCurrentContext();
        Log.d(LOG_TAG, "GL Thread Context for drawing: " + currentGLContext);
        useFramebuffer(arframebuffer);
        shader.lowLevelUse();
        mesh.lowLevelDraw();
    }

    /**
     * Clear the screen or framebuffer
     */
    public void clear(Framebuffer arFrameBuffer, float r, float g, float b, float a) {
        try {
            useFramebuffer(arFrameBuffer);

            // Clear the screen
            GLES30.glClearColor(r, g, b, a);
            GLES30.glDepthMask(true);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

            int error = GLES30.glGetError();
            if (error != GLES30.GL_NO_ERROR) {
                Log.e(LOG_TAG, "GL error in clear: 0x" + Integer.toHexString(error));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in clear: " + e.getMessage(), e);
        }
    }

    /**
     * Renderer interface
     */
    public interface IRenderer {
        void onSurfaceCreated(ARViewRender render);
        void onSurfaceChanged(ARViewRender render, int width, int height);
        void onDrawFrame(ARViewRender render);
    }

    /**
     * Get the assets manager
     */
    AssetManager getAssets() {
        Log.d(LOG_TAG, "assets are " + assetManager);
        return assetManager;
    }

    /**
     * Get the form
     */
    public Form getForm() {
        return form;
    }

    /**
     * Get the GLSurfaceView
     */
    public GLSurfaceView getGLSurfaceView() {
        return glView;
    }

    /**
     * Use a framebuffer
     */
    private void useFramebuffer(Framebuffer arframebuffer) {
        try {
            int framebufferId;
            int viewportWidth;
            int viewportHeight;

            if (arframebuffer == null) {
                framebufferId = 0;
                viewportWidth = this.viewportWidth;
                viewportHeight = this.viewportHeight;
            } else {
                framebufferId = arframebuffer.getFramebufferId();
                viewportWidth = arframebuffer.getWidth();
                viewportHeight = arframebuffer.getHeight();
            }

            // Bind framebuffer and set viewport
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
            GLES30.glViewport(0, 0, viewportWidth, viewportHeight);

            Log.d(LOG_TAG, "Binding to framebuffer: " + framebufferId +
                    ", viewport: " + viewportWidth + "x" + viewportHeight);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in useFramebuffer: " + e.getMessage(), e);
        }
    }
}