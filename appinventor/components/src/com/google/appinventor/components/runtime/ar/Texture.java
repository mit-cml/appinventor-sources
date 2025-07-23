package com.google.appinventor.components.runtime.ar;
import com.google.appinventor.components.runtime.arview.renderer.ARViewRender;
import com.google.appinventor.components.runtime.util.MediaUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A GPU-side texture.
 */
public class Texture implements Closeable {
    private static final String TAG = Texture.class.getSimpleName();

    private int[] textureId = {0};
    private final Target target;

    /**
     * Describes the way the texture's edges are rendered.
     *
     * @see <a
     * href="https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glTexParameter.xhtml">GL_TEXTURE_WRAP_S</a>.
     */
    public enum WrapMode {
        CLAMP_TO_EDGE(GLES30.GL_CLAMP_TO_EDGE),
        MIRRORED_REPEAT(GLES30.GL_MIRRORED_REPEAT),
        REPEAT(GLES30.GL_REPEAT);

        /* package-private */
        final int glesEnum;

        private WrapMode(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    /**
     * Describes the target this texture is bound to.
     *
     * @see <a
     * href="https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glBindTexture.xhtml">glBindTexture</a>.
     */
    public enum Target {
        TEXTURE_2D(GLES30.GL_TEXTURE_2D),
        TEXTURE_EXTERNAL_OES(GLES11Ext.GL_TEXTURE_EXTERNAL_OES),
        TEXTURE_CUBE_MAP(GLES30.GL_TEXTURE_CUBE_MAP);

        final int glesEnum;

        private Target(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    /**
     * Describes the color format of the texture.
     *
     * @see <a
     * href="https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glTexImage2D.xhtml">glTexImage2d</a>.
     */
    public enum ColorFormat {
        LINEAR(GLES30.GL_RGBA8),
        SRGB(GLES30.GL_SRGB8_ALPHA8);

        final int glesEnum;

        private ColorFormat(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    /**
     * Construct an empty {@link Texture}.
     *
     * <p>Since {@link Texture}s created in this way are not populated with data, this method is
     * mostly only useful for creating textures. See {@link
     * #createFromAsset} if you want a texture with data.
     */
    public Texture(ARViewRender render, Target target, WrapMode wrapMode) {
        this(render, target, wrapMode, /*useMipmaps=*/ true, 0);
    }

    public Texture(ARViewRender render, Target target, WrapMode wrapMode, int textureId) {
        this(render, target, wrapMode, /*useMipmaps=*/ true, textureId);
    }

    public Texture(ARViewRender render, Target target, WrapMode wrapMode, boolean useMipmaps){
        this(render, target, wrapMode, useMipmaps, 0);
    }

    public Texture(ARViewRender render, Target target, WrapMode wrapMode, boolean useMipmaps, int altTextureId) {
        this.target = target;


        if (altTextureId > 0){
            Log.d("Texture", "trying to create texture with id " + altTextureId);
            textureId[0] =altTextureId;
        } else {
            GLES30.glGenTextures(1, textureId, 0);
        }

        GLError.maybeThrowGLException("Texture creation failed", "either using pre-existing textureid or glGenTextures");

        int minFilter = useMipmaps ? GLES30.GL_LINEAR_MIPMAP_LINEAR : GLES30.GL_LINEAR;

        try {
            GLES30.glBindTexture(target.glesEnum, textureId[0]);
            GLError.maybeThrowGLException("Failed to bind texture", "glBindTexture");
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_MIN_FILTER, minFilter);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");

            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_WRAP_S, wrapMode.glesEnum);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_WRAP_T, wrapMode.glesEnum);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    /*
     * Create a texture from the given asset file name. Could be an external or internal asset
     */
    public static Texture createFromAsset(
            ARViewRender render, String assetFileName, WrapMode wrapMode, ColorFormat colorFormat)
            throws IOException {
        Texture texture = new Texture(render, Target.TEXTURE_2D, wrapMode);
        Bitmap bitmap = null;

        java.io.InputStream inputStream = null;
        try {
            inputStream = MediaUtil.openMedia(render.getForm(), assetFileName);
            Log.i(TAG, "texture asset found: " + assetFileName);

            // The following lines up to glTexImage2D could technically be replaced with
            // GLUtils.texImage2d, but this method does not allow for loading sRGB images.

            // Load and convert the bitmap and copy its contents to a direct ByteBuffer. Despite its name,
            // the ARGB_8888 config is actually stored in RGBA order.
            bitmap =
                    convertBitmapToConfig(
                            BitmapFactory.decodeStream(inputStream),
                            Bitmap.Config.ARGB_8888);

            ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(buffer);
            buffer.rewind();

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.getTextureId());
            GLError.maybeThrowGLException("Failed to bind texture", "glBindTexture");
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    /*level=*/ 0,
                    colorFormat.glesEnum,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    /*border=*/ 0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    buffer);
            GLError.maybeThrowGLException("Failed to populate texture data", "glTexImage2D");
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLError.maybeThrowGLException("Failed to generate mipmaps", "glGenerateMipmap");
        } catch (Throwable t) {
            texture.close();
            throw t;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        return texture;
    }

    /**
     * Create a texture from the given asset file name.
     */
    public static Texture createFromId(
            ARViewRender render, int textureId)
            throws IOException {

        // Create a texture object that references the existing ID
        Texture texture = new Texture(render, Target.TEXTURE_2D, WrapMode.REPEAT, textureId);


        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        Log.d("Texture", "Created wrapper for existing texture with ID " + textureId + " and new texture id is " + texture.getTextureId());
        return texture;
    }

    @Override
    public void close() {
        if (textureId[0] != 0) {
            GLES30.glDeleteTextures(1, textureId, 0);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free texture", "glDeleteTextures");
            textureId[0] = 0;
        }
    }

    /**
     * Retrieve the native texture ID.
     */
    public int getTextureId() {
        return textureId[0];
    }

    /* package-private */
    Target getTarget() {
        return target;
    }

    private static Bitmap convertBitmapToConfig(Bitmap bitmap, Bitmap.Config config) {
        // We use this method instead of BitmapFactory.Options.outConfig to support a minimum of Android
        // API level 24.
        if (bitmap.getConfig() == config) {
            return bitmap;
        }
        Bitmap result = bitmap.copy(config, /*isMutable=*/ false);
        bitmap.recycle();
        return result;
    }
}

