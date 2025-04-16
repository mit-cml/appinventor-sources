package com.google.appinventor.components.runtime;

import android.opengl.GLES30;
import android.util.Log;

import com.google.appinventor.components.runtime.Texture;

/**
 * A framebuffer object used for offscreen rendering.
 */
public class ARFrameBuffer {
    private static final String TAG = ARFrameBuffer.class.getSimpleName();

    private final int[] framebuffers = {0};
    int framebufferId = 0;
    private Texture colorTexture;
    private int colorTextureId;
    private Texture depthTexture;
    private int depthTextureId;
    private int width;
    private int height;
    private final ARViewRender render;

    /**
     * Constructs a Framebuffer with the given dimensions.
     *
     * @param render The ARViewRender instance
     * @param width The width of the framebuffer in pixels
     * @param height The height of the framebuffer in pixels
     */
    public ARFrameBuffer(ARViewRender render, int width, int height) {
        this.render = render;
        this.width = width;
        this.height = height;

        try {
            colorTexture =
                    new Texture(
                            render,
                            Texture.Target.TEXTURE_2D,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            /*useMipmaps=*/ false);
            depthTexture =
                    new Texture(
                            render,
                            Texture.Target.TEXTURE_2D,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            /*useMipmaps=*/ false);


            // Specify color texture storage
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTexture.getTextureId());
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA,
                    width,
                    height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    null);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

            // Specify depth textureT
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.getTextureId());
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_DEPTH_COMPONENT32F,
                    width,
                    height,
                    0,
                    GLES30.GL_DEPTH_COMPONENT,
                    GLES30.GL_FLOAT,
                    null);


            // Create framebuffer object and bind to the color and depth textures.
            GLES30.glGenFramebuffers(1, framebuffers, 0);
            GLError.maybeThrowGLException("Framebuffer creation failed", "glGenFramebuffers");
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, getFrameBufferId());
            GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D,
                    colorTexture.getTextureId(),
                    /*level=*/ 0);
            GLError.maybeThrowGLException(
                    "Failed to bind color texture to framebuffer", "glFramebufferTexture2D");
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_DEPTH_ATTACHMENT,
                    GLES30.GL_TEXTURE_2D,
                    depthTexture.getTextureId(),
                    /*level=*/ 0);
            GLError.maybeThrowGLException(
                    "Failed to bind depth texture to framebuffer", "glFramebufferTexture2D");

            int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("Framebuffer construction not complete: code " + status);
            }
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    public void close() {
        if (framebuffers[0] != 0) {
            GLES30.glDeleteFramebuffers(1, framebuffers, 0);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free framebuffer", "glDeleteFramebuffers");
            framebuffers[0] = 0;
        }
        colorTexture.close();
        depthTexture.close();
    }



    /**
     * Resizes the framebuffer to the given dimensions.
     */
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;

        // Color texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTexture.getTextureId());
        GLError.maybeThrowGLException("Failed to bind color texture", "glBindTexture");
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                /*level=*/ 0,
                GLES30.GL_RGBA,
                width,
                height,
                /*border=*/ 0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                /*pixels=*/ null);
        GLError.maybeThrowGLException("Failed to specify color texture format", "glTexImage2D");

        // Depth texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.getTextureId());
        GLError.maybeThrowGLException("Failed to bind depth texture", "glBindTexture");
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                /*level=*/ 0,
                GLES30.GL_DEPTH_COMPONENT32F,
                width,
                height,
                /*border=*/ 0,
                GLES30.GL_DEPTH_COMPONENT,
                GLES30.GL_FLOAT,
                /*pixels=*/ null);
        GLError.maybeThrowGLException("Failed to specify depth texture format", "glTexImage2D");
    }
    /**
     * Binds this framebuffer as the current render target.
     */
    public void bind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, getFrameBufferId());
        GLES30.glViewport(0, 0, width, height);
    }

    /**
     * Unbinds this framebuffer, returning to the default framebuffer.
     */
    public void unbind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Gets the color texture attachment of this framebuffer.
     *
     * @return The color texture
     */
    public Texture getColorTexture() {
        return colorTexture;
    }


    /**
     * Gets the depth texture attachment of this framebuffer.
     *
     * @return The depth texture
     */
    public Texture getDepthTexture() {
        return depthTexture;
    }
    /* package-private */
    public int getFrameBufferId() {

        return framebuffers[0];
    }

    public int[] getFrameBuffers() {

        return framebuffers;
    }
    /**
     * Gets the width of the framebuffer.
     *
     * @return The width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the framebuffer.
     *
     * @return The height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Releases the framebuffer and its attachments.
     */
    public void release() {
        if (framebuffers[0] != 0) {
            GLES30.glDeleteFramebuffers(1, framebuffers, 0);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free framebuffer", "glDeleteFramebuffers");
            framebuffers[0] = 0;
        }

        // Also close the textures if appropriate
        if (colorTexture != null) {
            colorTexture.close();
        }

        if (depthTexture != null) {
            depthTexture.close();
        }

        colorTexture = null;
        depthTexture = null;
    }

}