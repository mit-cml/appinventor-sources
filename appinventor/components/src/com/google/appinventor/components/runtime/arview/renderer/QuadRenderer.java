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
import java.util.List;
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
                   // "    vec2 rotateUV = vec2(v_TexCoord.y, 1.0 - v_TexCoord.x);\n" +
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
                   // "    vec2 rotatedUV = vec2(1.0 - v_TexCoord.x, v_TexCoord.y);\n" +
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

    private float[] calculateSceneCenter(List<ARNode> modelNodes) {
        float centerX = 0, centerY = 0, centerZ = 0;
        int count = 0;

        for (ARNode node : modelNodes) {
            if (node.Anchor() != null && node.Anchor().getTrackingState() == TrackingState.TRACKING) {
                float[] pos = node.Anchor().getPose().getTranslation();
                centerX += pos[0];
                centerY += pos[1];
                centerZ += pos[2];
                count++;
            }
        }

        if (count > 0) {
            return new float[]{centerX / count, centerY / count, centerZ / count};
        } else {
            // Fallback: look 1 meter in front of camera
            float[] arCoreCameraMatrix = new float[16];
            Matrix.invertM(arCoreCameraMatrix, 0, this.viewMatrix, 0);
            float[] forward = {-arCoreCameraMatrix[8], -arCoreCameraMatrix[9], -arCoreCameraMatrix[10]};
            return new float[]{
                arCoreCameraMatrix[12] + forward[0],
                arCoreCameraMatrix[13] + forward[1],
                arCoreCameraMatrix[14] + forward[2]
            };
        }
    }

    public void draw(ARViewRender render, List<ARNode> modelNodes, Texture currentFilamentTexture,
                     Framebuffer target, Pose cameraPose, float[] cameraProjection) {

        if (currentFilamentTexture == null) return;

        try {
            // Get ARCore camera view matrix
            cameraPose.inverse().toMatrix(viewMatrix, 0);

            // GLASSES POSITIONING: Always in front of camera
            float[] cameraMatrix = new float[16];
            cameraPose.toMatrix(cameraMatrix, 0);

            // Extract camera position and forward direction
            float camX = cameraMatrix[12];
            float camY = cameraMatrix[13];
            float camZ = cameraMatrix[14];

            // Forward direction (where camera is looking)
            float forwardX = -cameraMatrix[8];
            float forwardY = -cameraMatrix[9];
            float forwardZ = -cameraMatrix[10];

            // Position quad just in front of camera (like glasses)
            float distanceFromCamera = .1f + 0.005f; // Just past near plane
            int viewportWidth = 1020;
            int viewportHeight = 1410;
            float textureAspect = (float) viewportWidth / viewportHeight; // Texture aspect ratio

            // Create identity matrix for quad
            Matrix.setIdentityM(modelMatrix, 0);

            // Position quad in front of camera
            modelMatrix[12] = camX + forwardX * distanceFromCamera;
            modelMatrix[13] = camY + forwardY * distanceFromCamera;
            modelMatrix[14] = camZ + forwardZ * distanceFromCamera;

            // Billboard: Make quad face camera (same as before)
            calculateBillboardMatrix(new float[]{modelMatrix[12], modelMatrix[13], modelMatrix[14]}, viewMatrix);

            // FULLSCREEN SCALING: Calculate size to fill screen at this distance
            // Use camera's field of view to determine required quad size
            double fovRadians = Math.toRadians(45.0); // Approximate FOV, you could extract from projection
            float quadHeight = (float)(2.0 * distanceFromCamera * Math.tan(fovRadians / 2.0));
            float screenAspect = (float) viewportWidth / viewportHeight;
            float quadWidth = quadHeight * screenAspect;

            // Scale quad to fill screen
            Matrix.scaleM(modelMatrix, 0, quadWidth, quadHeight, 1.3f);

            Log.d(TAG, String.format("Fullscreen quad: %.3f x %.3f at distance %.3f",
                quadWidth, quadHeight, distanceFromCamera));

            // Calculate MVP matrix
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraProjection, 0, modelViewMatrix, 0);

            shader.setTexture("u_Texture", currentFilamentTexture);
            shader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);


            render.draw(quadMesh, shader, target);

            Log.d(TAG, String.format("Quad with aspect %.2f, matrix scale X:%.3f Y:%.3f Z:%.3f",
                textureAspect,
                Math.sqrt(modelMatrix[0]*modelMatrix[0] + modelMatrix[1]*modelMatrix[1] + modelMatrix[2]*modelMatrix[2]),
                Math.sqrt(modelMatrix[4]*modelMatrix[4] + modelMatrix[5]*modelMatrix[5] + modelMatrix[6]*modelMatrix[6]),
                Math.sqrt(modelMatrix[8]*modelMatrix[8] + modelMatrix[9]*modelMatrix[9] + modelMatrix[10]*modelMatrix[10])
            ));

        } catch (Exception e) {
            Log.e(TAG, "Error during glasses quad rendering", e);
        }
    }

    private void calculateBillboardMatrix(float[] sceneCenter, float[] viewMatrix) {


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

        modelMatrix[12] = sceneCenter[0];
        modelMatrix[13] = sceneCenter[1];
        modelMatrix[14] = sceneCenter[2];

        // Scale appropriately (this is critical for making it look right)
        float scale = 0.5f; // Adjust based on your scene scale
        Matrix.scaleM(modelMatrix, 0, scale, scale, 1.0f);
    }   /* Clean up resources when no longer needed.
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