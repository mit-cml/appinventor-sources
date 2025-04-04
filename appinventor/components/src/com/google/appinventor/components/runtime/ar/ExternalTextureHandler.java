package com.google.appinventor.components.runtime;

import android.graphics.SurfaceTexture;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.google.android.filament.Stream;
import com.google.android.filament.Texture;
import com.google.android.filament.Engine;

/**
 * Creates a SurfaceTexture and Surface for displaying camera feed or external content in Filament.
 * Similar to Sceneform's ExternalTexture class but adapted for direct use with Filament 1.9.11.
 */
public class ExternalTextureHandler {
    private static final String TAG = ExternalTextureHandler.class.getSimpleName();

    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private Texture filamentTexture;
    private Stream filamentStream;
    private final Engine engine;
    private int textureId = 0;

    /**
     * Creates an ExternalTextureHandler with a new SurfaceTexture for the camera feed
     *
     * @param engine The Filament Engine
     * @param cameraTextureId The OpenGL texture ID for the camera feed, or 0 to create a new one
     */
    public ExternalTextureHandler(Engine engine, int cameraTextureId) {
        this.engine = engine;
        this.textureId = cameraTextureId;

        initialize();
    }

    /**
     * Initialize the texture, surface and stream
     */
    private void initialize() {
        try {
            Log.d(TAG, "Initializing ExternalTextureHandler with textureId: " + textureId);

            // Create the Android surface texture
            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.detachFromGLContext();

            // Create the Android surface
            surface = new Surface(surfaceTexture);

            // Create the filament stream
            Stream.Builder streamBuilder = new Stream.Builder();

            if (textureId != 0) {
                // If we have a specific texture ID (from ARCore), use it
                filamentStream = streamBuilder
                        .stream(textureId)
                        .build(engine);
                Log.d(TAG, "Created stream with textureId: " + textureId);
            } else {
                // Otherwise, use the SurfaceTexture
                filamentStream = streamBuilder
                        .stream(surfaceTexture)
                        .build(engine);
                Log.d(TAG, "Created stream with SurfaceTexture");
            }

            // Create the filament texture
            Texture.Sampler textureSampler = Texture.Sampler.SAMPLER_EXTERNAL;
            Texture.InternalFormat textureInternalFormat = Texture.InternalFormat.RGB8;

            filamentTexture = new Texture.Builder()
                    .sampler(textureSampler)
                    .format(textureInternalFormat)
                    .build(engine);

            // Connect the texture to the stream
            filamentTexture.setExternalStream(engine, filamentStream);

            Log.d(TAG, "ExternalTextureHandler initialization complete");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize ExternalTextureHandler", e);
            cleanup();
            throw e;
        }
    }

    /**
     * Get the SurfaceTexture created for this handler
     */
    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    /**
     * Get the Surface created for this handler
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * Get the Filament Texture
     */
    public Texture getFilamentTexture() {
        return filamentTexture;
    }

    /**
     * Get the Filament Stream
     */
    public Stream getFilamentStream() {
        return filamentStream;
    }

    /**
     * Update the texture size
     */
    public void setDefaultBufferSize(int width, int height) {
        if (surfaceTexture != null) {
            surfaceTexture.setDefaultBufferSize(width, height);
        }
    }

    /**
     * Call updateTexImage on the SurfaceTexture
     */
    public void updateTexImage() {
        if (surfaceTexture != null) {
            try {
                surfaceTexture.updateTexImage();
            } catch (Exception e) {
                Log.e(TAG, "Failed to update texture image", e);
            }
        }
    }

    /**
     * Get the current transformation matrix from the SurfaceTexture
     */
    public void getTransformMatrix(float[] matrix) {
        if (surfaceTexture != null) {
            surfaceTexture.getTransformMatrix(matrix);
        } else {
            Matrix.setIdentityM(matrix, 0);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (filamentTexture != null) {
            engine.destroyTexture(filamentTexture);
            filamentTexture = null;
        }

        if (filamentStream != null) {
            engine.destroyStream(filamentStream);
            filamentStream = null;
        }

        if (surface != null) {
            surface.release();
            surface = null;
        }

        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }
}