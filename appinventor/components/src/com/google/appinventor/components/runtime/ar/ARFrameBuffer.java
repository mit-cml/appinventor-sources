package com.google.appinventor.components.runtime;

import android.opengl.GLES30;
import android.util.Log;

import com.google.appinventor.components.runtime.Texture;

/**
 * A framebuffer object used for offscreen rendering.
 */
public class ARFrameBuffer {
    private static final String TAG = ARFrameBuffer.class.getSimpleName();

    private int framebufferId;
    private Texture colorTexture;
    private int colorTextureId;
    private Texture depthTexture;
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

        createFramebuffer(width, height);
    }

    public int getFrameBufferId(){
        return framebufferId;
    }


    private void createFramebuffer(int width, int height) {
        this.width = width;
        this.height = height;

        try {
            // Create texture
            int[] textures = new int[1];
            GLES30.glGenTextures(1, textures, 0);
            colorTextureId = textures[0];

            // Bind framebuffer
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);

            // Set up color attachment
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTexture.getTextureId());

            // Set texture parameters for color attachment
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            // Allocate storage for color texture
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8,
                    width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

            // Create framebuffer
            int[] framebuffers = new int[1];
            GLES30.glGenFramebuffers(1, framebuffers, 0);
            framebufferId = framebuffers[0];



            // Attach color texture to framebuffer
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D, colorTexture.getTextureId(), 0);


            // Set texture parameters for depth texture
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_NONE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            // Allocate storage for depth texture
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT24,
                    width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, null);

            // Attach depth texture to framebuffer
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
                    GLES30.GL_TEXTURE_2D, depthTexture.getTextureId(), 0);

            // Check status
            int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                String reason;
                switch (status) {
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                        reason = "INCOMPLETE_ATTACHMENT";
                        break;
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                        reason = "INCOMPLETE_DIMENSIONS";
                        break;
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                        reason = "INCOMPLETE_MISSING_ATTACHMENT";
                        break;
                    case GLES30.GL_FRAMEBUFFER_UNSUPPORTED:
                        reason = "UNSUPPORTED";
                        break;
                    default:
                        reason = "Unknown error: " + status;
                }
                Log.e(TAG, "Framebuffer creation failed: " + reason);
                throw new RuntimeException("Framebuffer is not complete: " + reason);
            }

            // Unbind
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        } catch (Exception e){
            Log.e(TAG, "Error creating framebuffer: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create framebuffer", e);
        }
    }


    /**
     * Resizes the framebuffer and its attachments.
     *
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid framebuffer dimensions: " + width + "x" + height);
            return;
        }

        if (this.width == width && this.height == height) {
            return; // No change needed
        }

        this.width = width;
        this.height = height;

        // Release existing resources
        release();

        // Create new framebuffer with updated dimensions
        createFramebuffer(width, height);
    }

    /**
     * Binds this framebuffer as the current render target.
     */
    public void bind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
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
        if (framebufferId != 0) {
            int[] framebuffers = {framebufferId};
            GLES30.glDeleteFramebuffers(1, framebuffers, 0);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free framebuffer", "glDeleteFramebuffers");
            framebufferId = 0;
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