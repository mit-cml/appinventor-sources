package com.google.appinventor.components.runtime.arview.helper;

import android.opengl.GLES30;
import android.util.Log;

/**
 * Helper class for managing OpenGL textures and framebuffers for capturing
 * Filament renders to be used in the AR pipeline.
 */
public class GLTextureHelper {
    private static final String TAG = GLTextureHelper.class.getSimpleName();

    // OpenGL resources
    private int textureId = 0;
    private int framebufferId = 0;
    private int depthRenderbufferId = 0;

    // Texture dimensions
    private int width = 0;
    private int height = 0;

    /**
     * Creates a new GLTextureHelper
     */
    public GLTextureHelper() {
        // Textures will be created on demand
    }

    /**
     * Gets the current texture ID
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * Creates or resizes a texture and associated framebuffer
     *
     * @param width The width of the texture
     * @param height The height of the texture
     */
    public void createTexture(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid texture dimensions: " + width + "x" + height);
            return;
        }

        // Store dimensions
        this.width = width;
        this.height = height;

        // Clean up any existing resources
        destroyResources();

        // Create texture
        int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        // Allocate storage for the texture
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8,
                width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

        // Create framebuffer
        int[] framebufferIds = new int[1];
        GLES30.glGenFramebuffers(1, framebufferIds, 0);
        framebufferId = framebufferIds[0];

        // Create depth renderbuffer
        int[] renderbufferIds = new int[1];
        GLES30.glGenRenderbuffers(1, renderbufferIds, 0);
        depthRenderbufferId = renderbufferIds[0];

        // Set up depth renderbuffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthRenderbufferId);
        GLES30.glRenderbufferStorage(
                GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24,
                width, height);

        // Set up framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, textureId, 0);
        GLES30.glFramebufferRenderbuffer(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
                GLES30.GL_RENDERBUFFER, depthRenderbufferId);

        // Check framebuffer completeness
        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "Framebuffer incomplete: " + status);
        }

        // Unbind
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        Log.d(TAG, "Created texture " + textureId + " with dimensions " + width + "x" + height);
    }

    /**
     * Begins capturing to the framebuffer
     */
    public void beginCapture() {
        if (framebufferId == 0) {
            Log.w(TAG, "Cannot begin capture - framebuffer not created");
            return;
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Ends capturing to the framebuffer
     */
    public void endCapture() {
        // Unbind framebuffer to return to default
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Clean up OpenGL resources
     */
    public void destroyResources() {
        // Clean up framebuffer
        if (framebufferId != 0) {
            int[] ids = { framebufferId };
            GLES30.glDeleteFramebuffers(1, ids, 0);
            framebufferId = 0;
        }

        // Clean up renderbuffer
        if (depthRenderbufferId != 0) {
            int[] ids = { depthRenderbufferId };
            GLES30.glDeleteRenderbuffers(1, ids, 0);
            depthRenderbufferId = 0;
        }

        // Clean up texture
        if (textureId != 0) {
            int[] ids = { textureId };
            GLES30.glDeleteTextures(1, ids, 0);
            textureId = 0;
        }
    }

    /**
     * Destroys all resources
     */
    public void destroy() {
        destroyResources();
    }
}