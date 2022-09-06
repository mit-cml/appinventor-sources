package com.google.appinventor.components.runtime;


import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A SampleRender context.
 */
public class ARViewRender {
    private static final String TAG = ARViewRender.class.getSimpleName();

    private final AssetManager assetManager;

    private int viewportWidth = 1;
    private int viewportHeight = 1;

    /**
     * Constructs a SampleRender object and instantiates GLSurfaceView parameters.
     *
     * @param glSurfaceView Android GLSurfaceView
     * @param renderer      Renderer implementation to receive callbacks
     * @param assetManager  AssetManager for loading Android resources
     */
    public ARViewRender(GLSurfaceView glSurfaceView, final Renderer renderer, AssetManager assetManager) {
        this.assetManager = assetManager;
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setRenderer(
                new GLSurfaceView.Renderer() {
                    @Override
                    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                        GLES30.glEnable(GLES30.GL_BLEND);
                        GLError.maybeThrowGLException("Failed to enable blending", "glEnable");
                        renderer.onSurfaceCreated(ARViewRender.this);
                    }

                    @Override
                    public void onSurfaceChanged(GL10 gl, int w, int h) {
                        viewportWidth = w;
                        viewportHeight = h;
                        renderer.onSurfaceChanged(ARViewRender.this, w, h);
                    }

                    @Override
                    public void onDrawFrame(GL10 gl) {
                        clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f);
                        renderer.onDrawFrame(ARViewRender.this);
                    }
                });
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setWillNotDraw(false);
    }


    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }


    public void draw(Mesh mesh, Shader shader, Framebuffer framebuffer) {
        useFramebuffer(framebuffer);
        shader.lowLevelUse();
        mesh.lowLevelDraw();
    }

    public void clear(Framebuffer framebuffer, float r, float g, float b, float a) {
        useFramebuffer(framebuffer);
        GLES30.glClearColor(r, g, b, a);
        GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
        GLES30.glDepthMask(true);
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
    }

    public static interface Renderer {

        public void onSurfaceCreated(ARViewRender render);

        public void onSurfaceChanged(ARViewRender render, int width, int height);

        public void onDrawFrame(ARViewRender render);
    }

    /* package-private */
    AssetManager getAssets() {
        return assetManager;
    }

    private void useFramebuffer(Framebuffer framebuffer) {
        int framebufferId;
        int viewportWidth;
        int viewportHeight;
        if (framebuffer == null) {
            framebufferId = 0;
            viewportWidth = this.viewportWidth;
            viewportHeight = this.viewportHeight;
        } else {
            framebufferId = framebuffer.getFramebufferId();
            viewportWidth = framebuffer.getWidth();
            viewportHeight = framebuffer.getHeight();
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
    }
}
