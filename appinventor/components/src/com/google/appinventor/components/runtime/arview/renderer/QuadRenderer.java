package com.google.appinventor.components.runtime.arview.renderer;

import android.util.Log;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.ar.*;
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

/**
 * StableQuadRenderer renders textured quads with stable performance.
 * Following the same architecture as PlaneRenderer and ObjectRenderer.
 */
public class QuadRenderer {
    private static final String TAG = QuadRenderer.class.getSimpleName();

    // Shader names - you would need to create these shader files
    private static final String VERTEX_SHADER_NAME = Form.ASSETS_PREFIX +"quad.vert";
    private static final String FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX +"quad.frag";

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

    public void draw(ARViewRender render, ARNode filamentSceneNode, Texture currentFilamentTexture,
                     Framebuffer target, Pose cameraPose, float[] cameraProjection) {

        if (currentFilamentTexture == null ) {
            Log.w(TAG, "Invalid texture  " + currentFilamentTexture);
            return;
        }
        int texId = currentFilamentTexture.getTextureId();
        if ( texId <= 0) {
            Log.w(TAG, "Invalid texture ID: " + texId);
            return;
        }

        try {
            Anchor anchor = filamentSceneNode.Anchor();
            if (anchor == null || anchor.getTrackingState() != TrackingState.TRACKING) {
                return;
            }

            // Get ARCore camera view matrix
            cameraPose.inverse().toMatrix(viewMatrix, 0);

            // Use the EXACT same transformation as ARFilamentRenderer
            Pose anchorPose = anchor.getPose();
            float[] anchorMatrix = new float[16];
            anchorPose.toMatrix(anchorMatrix, 0);

            // Apply the SAME scale that ARFilamentRenderer uses (0.1f)
            float scale = filamentSceneNode.Scale();
            Matrix.scaleM(anchorMatrix, 0, scale, scale, scale);

            // Position the quad exactly where the 3D model is
            calculateBillboardMatrix(anchorMatrix, viewMatrix);

            // Apply additional quad scale for visibility (this is separate from model scale)
            updateModelMatrix(modelMatrix, 1.0f); // Make quad bigger since model is 0.1 scale

            // Calculate MVP matrix
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraProjection, 0, modelViewMatrix, 0);

            shader.setTexture("u_Texture", currentFilamentTexture);
            shader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

            // Draw the quad
            render.draw(quadMesh, shader, target);

            float[] anchorPos = anchorPose.getTranslation();
            Log.d(TAG, String.format("Quad positioned exactly like 3D model: [%.3f, %.3f, %.3f] * %.3f scale",
                anchorPos[0], anchorPos[1], anchorPos[2], scale));

        } catch (Exception e) {
            Log.e(TAG, "Error during quad rendering", e);
        }
    }

    private void calculateBillboardMatrix(float[] anchorMatrix, float[] viewMatrix) {
        // Extract anchor position
        float anchorX = anchorMatrix[12];
        float anchorY = anchorMatrix[13];
        float anchorZ = anchorMatrix[14];

        // Create a clean model matrix starting with identity
        Matrix.setIdentityM(modelMatrix, 0);

        // There are two approaches for billboarding:

        // APPROACH 1: FULL BILLBOARDING (always fully faces camera)
        // Extract the rotation from the view matrix (excluding translation)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Take transpose of the rotation component of the view matrix
                // (transpose of rotation matrix = inverse rotation)
                modelMatrix[i*4+j] = viewMatrix[j*4+i];
            }
        }

        // APPROACH 2: Y-AXIS CONSTRAINED BILLBOARDING (only rotates around Y, stays upright)
        // This is your current approach which works well for signs, trees, etc.
    /*
    // Extract camera position (using inverse of view matrix)
    float[] cameraMatrix = new float[16];
    Matrix.invertM(cameraMatrix, 0, viewMatrix, 0);
    float cameraX = cameraMatrix[12];
    float cameraY = cameraMatrix[13];
    float cameraZ = cameraMatrix[14];

    // Vector from anchor to camera (in XZ plane only for Y-up constraint)
    float dirX = cameraX - anchorX;
    float dirZ = cameraZ - anchorZ;

    // Calculate rotation around Y axis to face camera
    float angle = (float) Math.toDegrees(Math.atan2(dirX, dirZ));

    // Apply rotation around Y axis to face camera in XZ plane
    Matrix.rotateM(modelMatrix, 0, angle, 0, 1, 0);
    */

        // Set position from anchor
        modelMatrix[12] = anchorX;
        modelMatrix[13] = anchorY;
        modelMatrix[14] = anchorZ;
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