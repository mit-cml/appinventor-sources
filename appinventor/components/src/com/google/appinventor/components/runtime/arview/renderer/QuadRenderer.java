package com.google.appinventor.components.runtime.arview.renderer;

import android.opengl.GLES30;
import android.util.Log;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import android.opengl.Matrix;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.Trackable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 * StableQuadRenderer renders textured quads with stable performance.
 * Following the same architecture as PlaneRenderer and ObjectRenderer.
 */
public class QuadRenderer {
    private static final String TAG = QuadRenderer.class.getSimpleName();

    // Shader names - you would need to create these shader files
    private static final String VERTEX_SHADER_NAME = "quad.vert";
    private static final String FRAGMENT_SHADER_NAME = "quad.frag";

    // Quad geometry constants
    private static final int VERTICES_PER_QUAD = 4;
    private static final int COORDS_PER_VERTEX = 2;
    private static final float[] QUAD_COORDS = {
            -1.0f, -1.0f,  // bottom-left
            1.0f, -1.0f,   // bottom-right
            -1.0f, 1.0f,   // top-left
            1.0f, 1.0f     // top-right
    };
    private static final float[] TEXTURE_COORDS = {
            0.0f, 1.0f,  // bottom-left
            1.0f, 1.0f,  // bottom-right
            0.0f, 0.0f,  // top-left
            1.0f, 0.0f   // top-right
    };

    private final Mesh quadMesh;
    private final Shader shader;

    // Cache for textures to avoid recreating wrapper objects
    private final Map<Integer, Texture> textureCache = new HashMap<>();


    // Temporary matrices allocated here to reduce allocations for each frame.
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] arNodeAngleUvMatrix = new float[4]; // 2x2 rotation matrix for uv coords

    /**
     * Create a StableQuadRenderer with persistent OpenGL resources.
     */
    public QuadRenderer(ARViewRender render) throws IOException {
        // Create vertex buffers for position and texture coordinates
        FloatBuffer quadCoordsBuffer = ByteBuffer.allocateDirect(QUAD_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(QUAD_COORDS);
        quadCoordsBuffer.position(0);

        FloatBuffer texCoordsBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE_COORDS);
        texCoordsBuffer.position(0);

        // Create VertexBuffer objects
        VertexBuffer quadCoordsVertexBuffer =
                new VertexBuffer(render, COORDS_PER_VERTEX, quadCoordsBuffer);
        VertexBuffer texCoordsVertexBuffer =
                new VertexBuffer(render, COORDS_PER_VERTEX, texCoordsBuffer);

        // Create mesh from vertex buffers
        VertexBuffer[] vertexBuffers = {
                quadCoordsVertexBuffer, texCoordsVertexBuffer
        };
        quadMesh = new Mesh(render, Mesh.PrimitiveMode.TRIANGLE_STRIP, /*indexBuffer=*/null, vertexBuffers);

        // Create shader or load from assets
        try {
            shader = createShader(render);
            // Set default blend mode for proper alpha handling
            shader.setBlend(
                    Shader.BlendFactor.SRC_ALPHA,
                    Shader.BlendFactor.ONE_MINUS_SRC_ALPHA);
            // Typically no depth testing needed for quads
            shader.setDepthTest(false);
            shader.setDepthWrite(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create quad shader", e);
            throw e;
        }
    }

    /**
     * Create a shader for rendering the quad.
     */
    private Shader createShader(ARViewRender render) throws IOException {
        try {
            // First try to load from asset files
            return Shader.createFromAssets(
                    render,
                    VERTEX_SHADER_NAME,
                    FRAGMENT_SHADER_NAME,
                    /*defines=*/ null);
        } catch (IOException e) {
            // Fall back to embedded shader code if assets aren't available
            Log.w(TAG, "Shader assets not found, using embedded shaders");

            String vertexShaderCode =
                    "#version 300 es\n" +
                            "uniform mat4 u_ModelViewProjection;\n" +
                            "layout(location = 0) in vec2 a_Position; // Your quad coordinates\n" +
                            "layout(location = 1) in vec2 a_TexCoord; // Your texture coordinates\n" +
                            "out vec2 v_TexCoord;\n" +
                            "\n" +
                            "void main() {\n" +
                            "    v_TexCoord = a_TexCoord;\n" +
                            "    gl_Position = u_ModelViewProjection * vec4(a_Position, 0.0, 1.0);\n" +
                            "}\n";

            String fragmentShaderCode =
                    "#version 300 es\n" +
                            "precision mediump float;\n" +
                            "uniform sampler2D u_Texture;\n" +
                            "in vec2 v_TexCoord;\n" +
                            "layout(location = 0) out vec4 o_FragColor;\n" +
                            "\n" +
                            "void main() {\n" +
                            "    o_FragColor = texture(u_Texture, v_TexCoord);\n" +
                            "}\n";

            return new Shader(render, vertexShaderCode, fragmentShaderCode, /*defines=*/ null);
        }
    }

    public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactor;
        scaleMatrix[5] = scaleFactor;
        scaleMatrix[10] = scaleFactor;
        Matrix.multiplyMM(this.modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
    }

    public void updateModelMatrix(float[] modelMatrix, float scaleFactorX, float scaleFactorY, float scaleFactorZ) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactorX;
        scaleMatrix[5] = scaleFactorY;
        scaleMatrix[10] = scaleFactorZ;
        Matrix.multiplyMM(this.modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
    }

    public void draw(ARViewRender render, ARNode filamentSceneNode, Texture currentFilamentTexture,
                     Framebuffer target, Pose cameraPose, float[] cameraProjection) {

        int texId = currentFilamentTexture.getTextureId();
        if (texId <= 0) {
            Log.w(TAG, "Invalid texture ID: " + texId);
            return;
        }

        try {
            // Bind framebuffer handling code remains the same

            Anchor anchor = filamentSceneNode.Anchor();
            if (anchor == null || anchor.getTrackingState() != TrackingState.TRACKING) {
                return;
            }

            // Get the world position of the anchor
            float[] anchorMatrix = new float[16];
            anchor.getPose().toMatrix(anchorMatrix, 0);

            // Get camera view matrix
            cameraPose.inverse().toMatrix(viewMatrix, 0);

            // Calculate the billboard matrix (always facing camera)
            calculateBillboardMatrix(anchorMatrix, viewMatrix);

            // Apply scale
            updateModelMatrix(modelMatrix, 1.0f);

            // Calculate MVP matrix

            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraProjection, 0, modelViewMatrix, 0);


            // Set shader uniforms - much simpler now!
            shader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

            // Draw the quad
            render.draw(quadMesh, shader, target);

        } catch (Exception e) {
            Log.e(TAG, "Error during quad rendering", e);
        }
    }

    /**
     * Calculate a billboard matrix that will make the quad face the camera
     */
    private void calculateBillboardMatrix(float[] anchorMatrix, float[] viewMatrix) {
        // Start with the anchor's position
        System.arraycopy(anchorMatrix, 0, modelMatrix, 0, 16);

        // Clear the rotation part of the model matrix, keeping only translation
        modelMatrix[0] = 1.0f;
        modelMatrix[1] = 0.0f;
        modelMatrix[2] = 0.0f;

        modelMatrix[4] = 0.0f;
        modelMatrix[5] = 1.0f;
        modelMatrix[6] = 0.0f;

        modelMatrix[8] = 0.0f;
        modelMatrix[9] = 0.0f;
        modelMatrix[10] = 1.0f;

        // For perfect camera facing, you can extract rotation from the view matrix
        // and apply its inverse, but in most AR cases, just clearing rotation works well
    }
     /* Clean up resources when no longer needed.
     */
    public void cleanup() {
        // Clean up shader
        if (shader != null) {
            shader.close();
        }

        // Clean up texture cache
        for (Texture texture : textureCache.values()) {
            if (texture != null) {
                texture.close();
            }
        }
        textureCache.clear();
    }
}