package com.google.appinventor.components.runtime.arview.renderer;

import android.media.Image;
import android.opengl.GLES30;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.ar.*;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
/**
 * This class both renders the AR camera background and composes the a scene foreground. The camera
 * background can be rendered as either camera image data or camera depth data. The virtual scene
 * can be composited with or without depth occlusion.
 */

@UsesAssets(fileNames =
        "background_show_camera.frag, background_show_camera.vert," +
        "background.frag, background.vert," +
        "background_show_depth_color_visualization.frag, background_show_depth_color_visualization.vert," +
        "occlusion.frag, occlusion.vert, depth_color_palette.png"
)
public class BackgroundRenderer {
    private static final String LOG_TAG = BackgroundRenderer.class.getSimpleName();

    private static final int VERTICES_PER_QUAD = 4;
    private static final int COMPONENTS_PER_VERTEX = 2;
    private static final int COORDS_BUFFER_SIZE = VERTICES_PER_QUAD * COMPONENTS_PER_VERTEX * Float.BYTES;


    private static final FloatBuffer NDC_QUAD_COORDS_BUFFER =
            ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private static final FloatBuffer VIRTUAL_SCENE_TEX_COORDS_BUFFER =
            ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    static {
        NDC_QUAD_COORDS_BUFFER.put(
                new float[]{
                        /*0:*/ -1f, -1f, /*1:*/ +1f, -1f, /*2:*/ -1f, +1f, /*3:*/ +1f, +1f,
                });
        VIRTUAL_SCENE_TEX_COORDS_BUFFER.put(
                new float[]{
                        /*0:*/ 0f, 0f, /*1:*/ 1f, 0f, /*2:*/ 0f, 1f, /*3:*/ 1f, 1f,
                });


    }

    private final FloatBuffer cameraTexCoords =
            ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private final Mesh mesh;
    private final VertexBuffer cameraTexCoordsVertexBuffer;
    private Shader backgroundShader;
    private Shader occlusionShader;
    private final Texture cameraDepthTexture;
    private final Texture cameraColorTexture;
    private Texture placeHolderTexture;
    private Texture depthColorPaletteTexture;

    private boolean useDepthVisualization;
    private boolean useOcclusion;
    private float aspectRatio;

    // Shader names - you would need to create these shader files
    private static final String VERTEX_SHADER_NAME = Form.ASSETS_PREFIX +"background_show_camera.vert";
    private static final String FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX +"background_show_camera.frag";

    public BackgroundRenderer(ARViewRender render) throws IOException{
        placeHolderTexture =
                new Texture(
                        render,
                        Texture.Target.TEXTURE_2D,
                        Texture.WrapMode.CLAMP_TO_EDGE,
                        /*useMipmaps=*/ false);

        cameraColorTexture =
                new Texture(
                        render,
                        Texture.Target.TEXTURE_EXTERNAL_OES,
                        Texture.WrapMode.CLAMP_TO_EDGE,
                        /*useMipmaps=*/ false);
        cameraDepthTexture =
                new Texture(
                        render,
                        Texture.Target.TEXTURE_2D,
                        Texture.WrapMode.CLAMP_TO_EDGE,
                        /*useMipmaps=*/ false);

        // In initialization
        FloatBuffer defaultTexCoords = ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        defaultTexCoords.put(new float[] {
                0.0f, 1.0f,  // bottom-left
                1.0f, 1.0f,  // bottom-right
                0.0f, 0.0f,  // top-left
                1.0f, 0.0f   // top-right
        });
        defaultTexCoords.position(0);


        // Create a Mesh with three vertex buffers: one for the screen coordinates (normalized device
        // coordinates), one for the camera texture coordinates (to be populated with proper data later
        // before drawing), and one for the virtual scene texture coordinates (unit texture quad)
        VertexBuffer screenCoordsVertexBuffer =
                new VertexBuffer(render, /* numberOfEntriesPerVertex=*/ 2, NDC_QUAD_COORDS_BUFFER);
        cameraTexCoordsVertexBuffer =
                new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 2, /*entries=*/ defaultTexCoords);
        VertexBuffer virtualSceneTexCoordsVertexBuffer =
                new VertexBuffer(render, /* numberOfEntriesPerVertex=*/ 2, VIRTUAL_SCENE_TEX_COORDS_BUFFER);
        VertexBuffer[] vertexBuffers = {
                screenCoordsVertexBuffer, cameraTexCoordsVertexBuffer, virtualSceneTexCoordsVertexBuffer,
        };
        mesh =
                new Mesh(render, Mesh.PrimitiveMode.TRIANGLE_STRIP, /*indexBuffer=*/ null, vertexBuffers);


        backgroundShader = Shader.createFromAssets(
                        render,
                        VERTEX_SHADER_NAME,
                        FRAGMENT_SHADER_NAME,
                        null)
                .setTexture("u_CameraColorTexture", cameraColorTexture)
               // .setTexture("u_Texture", placeHolderTexture)
                .setDepthTest(false)
                .setDepthWrite(false);


        // Initialize occlusion shader with default settings
        HashMap<String, String> defines = new HashMap<>();
        defines.put("USE_OCCLUSION", "0");  // Start with occlusion disabled
        occlusionShader = Shader.createFromAssets(
                        render,
                Form.ASSETS_PREFIX +"occlusion.vert",
                Form.ASSETS_PREFIX +"occlusion.frag",
                        defines)
                .setTexture("u_CameraColorTexture", cameraColorTexture)
                .setDepthTest(false)
                .setDepthWrite(false)
                .setBlend(Shader.BlendFactor.SRC_ALPHA, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA);

        // Set default aspect ratio
        aspectRatio = 1.0f;
    }

    /**
     * Sets whether the background camera image should be replaced with a depth visualization instead.
     * This reloads the corresponding shader code, and must be called on the GL thread.
     */
    public void setUseDepthVisualization(ARViewRender render, boolean useDepthVisualization)
            throws IOException {
        if (backgroundShader != null) {
            if (this.useDepthVisualization == useDepthVisualization) {
                return;
            }
            backgroundShader.close();
            backgroundShader = null;
            this.useDepthVisualization = useDepthVisualization;
        }
        if (useDepthVisualization) {
            depthColorPaletteTexture =
                    Texture.createFromAsset(
                            render,
                            "depth_color_palette.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.LINEAR);
            backgroundShader =
                    Shader.createFromAssets(
                                    render,
                                    "background_show_depth_color_visualization.vert",
                                    "background_show_depth_color_visualization.frag",
                                    /*defines=*/ null)
                            .setTexture("u_CameraDepthTexture", cameraDepthTexture)
                            .setTexture("u_ColorMap", depthColorPaletteTexture)
                            .setDepthTest(false)
                            .setDepthWrite(false);
        } else {
            backgroundShader =
                    Shader.createFromAssets(
                                    render,
                                    VERTEX_SHADER_NAME,
                                    FRAGMENT_SHADER_NAME,
                                    /*defines=*/ null)
                            .setTexture("u_CameraColorTexture", cameraColorTexture)
                           // .setTexture("u_Texture", placeHolderTexture)
                            .setDepthTest(false)
                            .setDepthWrite(false);
        }
    }

    /**
     * Sets whether to use depth for occlusion. This reloads the shader code with new {@code
     * #define}s, and must be called on the GL thread.
     */
    public void setUseOcclusion(ARViewRender render, boolean useOcclusion) throws IOException {
        if (occlusionShader != null) {
            if (this.useOcclusion == useOcclusion) {
                return;
            }
            occlusionShader.close();
            occlusionShader = null;
            this.useOcclusion = useOcclusion;
        }
        HashMap<String, String> defines = new HashMap<>();
        defines.put("USE_OCCLUSION", useOcclusion ? "1" : "0");
        occlusionShader =
                Shader.createFromAssets(render, "occlusion.vert", "occlusion.frag", defines)
                        .setDepthTest(false)
                        .setDepthWrite(false)
                        .setBlend(Shader.BlendFactor.SRC_ALPHA, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA);
        if (useOcclusion) {
            occlusionShader
                    .setTexture("u_CameraDepthTexture", cameraDepthTexture)
                    .setFloat("u_DepthAspectRatio", aspectRatio);
        }
    }

    /**
     * Updates the display geometry. This must be called every frame before calling either of
     * BackgroundRenderer's draw methods.
     */
    public void updateDisplayGeometry(Frame frame) {
        if (frame == null) {
            Log.e(LOG_TAG, "Cannot update display geometry with null frame");
            return;
        }

        // Check frame timestamp to ensure it's valid
        if (frame.getTimestamp() == 0) {
            Log.e(LOG_TAG, "Frame has invalid timestamp");
            return;
        }

        // Reset buffers properly
        cameraTexCoords.clear();
        NDC_QUAD_COORDS_BUFFER.position(0);

        try {

            frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    NDC_QUAD_COORDS_BUFFER,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    cameraTexCoords);

            // Rewind buffer for reading
            cameraTexCoords.position(0);

            // Check if the buffer has data
            if (cameraTexCoords.limit() == 0) {
                Log.e(LOG_TAG, "Transformation produced empty buffer");
                return;
            }

            // Update the vertex buffer with the new coordinates
            cameraTexCoordsVertexBuffer.set(cameraTexCoords);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in updateDisplayGeometry", e);
        }
    }

    /**
     * Update depth texture with Image contents.
     */
    public void updateCameraDepthTexture(Image image) {
        // SampleRender abstraction leaks here
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, cameraDepthTexture.getTextureId());
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RG8,
                image.getWidth(),
                image.getHeight(),
                0,
                GLES30.GL_RG,
                GLES30.GL_UNSIGNED_BYTE,
                image.getPlanes()[0].getBuffer());
        if (useOcclusion) {
            aspectRatio = (float) image.getWidth() / (float) image.getHeight();
            occlusionShader.setFloat("u_DepthAspectRatio", aspectRatio);
        }
    }

    /**
     * Draws the AR background image. The image will be drawn such that virtual content rendered with
     * the matrices provided by {@link com.google.ar.core.Camera#getViewMatrix(float[], int)} and
     * {@link com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)} will
     * accurately follow static physical objects.
     */

    public void drawBackground(ARViewRender render, int textureId, int width, int height) {
        try {
            //backgroundShader.setBlend(Shader.BlendFactor.SRC_ALPHA, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA);
            render.draw(mesh, backgroundShader, null);
        } catch (java.lang.Exception e) {
            Log.d(LOG_TAG, "Error trying to creat texture" + e);
        }
    }

    /**
     * Draws the virtual scene. Any objects rendered in the given {@link Framebuffer} will be drawn
     * given the previously specified.
     *
     * <p>Virtual content should be rendered using the matrices provided by {@link
     * com.google.ar.core.Camera#getViewMatrix(float[], int)} and {@link
     * com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)}.
     */
    public void drawVirtualScene(
            ARViewRender render, Framebuffer framebuffer,
            float zNear, float zFar) {

        if (occlusionShader == null) {
            Log.e(LOG_TAG, "Cannot draw virtual scene: backgroundShader is null");
            return;
        }
        Shader tempShader = occlusionShader;


        try {
// filament texture that was written to framebuffer by quadrenderer
            tempShader.setTexture("u_VirtualSceneColorTexture", framebuffer.getColorTexture());
            //tempShader.setTexture("u_CameraColorTexture", cameraColorTexture);

            // check framebuffer.getDepthTexture();
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }

        // Explicitly bind to default framebuffer
       // GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        Log.d(LOG_TAG, "Drawing virtual scene with fb and color TEXTURE id " + framebuffer.getFramebufferId() + " " + framebuffer.getColorTexture().getTextureId());
        // Draw to default framebuffer
        render.draw(mesh, tempShader, null);

    }


    /**
     * Return the camera color texture generated by this object.
     */
    public Texture getCameraColorTexture() {
        return cameraColorTexture;
    }

    /**
     * Return the camera depth texture generated by this object.
     */
    public Texture getCameraDepthTexture() {
        return cameraDepthTexture;
    }
}

