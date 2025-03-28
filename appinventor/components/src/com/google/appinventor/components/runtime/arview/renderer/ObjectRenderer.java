package com.google.appinventor.components.runtime.arview.renderer;
import com.google.appinventor.components.annotations.UsesAssets;
import android.opengl.Matrix;
import android.util.Log;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.Trackable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Renders an object loaded from an OBJ file in OpenGL. */
@UsesAssets(fileNames = "ar_object.frag, ar_object.vert, ar_unlit_object.frag, ar_unlit_object.vert, pawn.obj, pawn_abledo.png")
public class ObjectRenderer {
  private static final String TAG = ObjectRenderer.class.getSimpleName();

  /**
   * Blend mode.
   *
   * @see #setBlendMode(BlendMode)
   */
  public enum BlendMode {
    /** Multiplies the destination color by the source alpha, without z-buffer writing. */
    Shadow,
    /** Normal alpha blending with z-buffer writing. */
    AlphaBlending
  }

  // Default model and texture if none specified
  private static final String DEFAULT_MODEL_NAME = "chick_baby_chicken_bird.glb";
  private static final String DEFAULT_TEXTURE_NAME = "Palette.png";

  // Shader names.
  private static final String VERTEX_SHADER_NAME = "ar_object.vert";
  private static final String FRAGMENT_SHADER_NAME = "ar_object.frag";

  // Cache for meshes and textures
  private final Map<String, Mesh> meshCache = new HashMap<>();
  private final Map<String, Texture> textureCache = new HashMap<>();

  private Shader shader;

  // Temporary matrices allocated here to reduce allocations for each frame.
  private final float[] viewMatrix = new float[16];
  private final float[] modelMatrix = new float[16];
  private final float[] modelViewMatrix = new float[16];
  private final float[] modelViewProjectionMatrix = new float[16];
  private final float[] arNodeAngleUvMatrix = new float[4]; // 2x2 rotation matrix for uv coords

  // Default lighting and material parameters
  private final float[] defaultLightingParameters = {0.0f, 1.0f, 0.0f, 1.0f};
  private final float[] defaultMaterialParameters = {0.3f, 0.5f, 0.2f, 16.0f};
  private final float[] defaultColorCorrectionParameters = {1.0f, 1.0f, 1.0f, 1.0f};
  private final float[] defaultObjectColor = {1.0f, 1.0f, 1.0f, 1.0f}; // White

  public ObjectRenderer(ARViewRender render) throws IOException {
    // Create the shader once during initialization
    Texture defaultTexture = createOrGetTexture(render, DEFAULT_TEXTURE_NAME);
    shader = Shader.createFromAssets(
            render,
            VERTEX_SHADER_NAME,
            FRAGMENT_SHADER_NAME,
            /*defines=*/ null
    ).setTexture("u_Texture", defaultTexture);
  }

  /**
   * Updates the object model matrix and applies scaling.
   *
   * @param modelMatrix A 4x4 model-to-world transformation matrix, stored in column-major order.
   * @param scaleFactor A separate scaling factor to apply before the {@code modelMatrix}.
   * @see android.opengl.Matrix
   */
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

  /**
   * Creates or retrieves a cached mesh to prevent memory leaks
   */
  private Mesh createOrGetMesh(ARViewRender render, String modelName) throws IOException {
    if (modelName == null || modelName.isEmpty()) {
      modelName = DEFAULT_MODEL_NAME;
    }

    // Check if mesh is already cached
    Mesh cachedMesh = meshCache.get(modelName);
    if (cachedMesh != null) {
      return cachedMesh;
    }

    // Create new mesh and cache it
    try {


      Mesh newMesh = Mesh.createFromAsset(render, modelName);


      meshCache.put(modelName, newMesh);
      return newMesh;
    } catch (IOException e) {
      Log.e(TAG, "Failed to load model: " + modelName + ", using default", e);
      // Try to load default model instead
      if (!modelName.equals(DEFAULT_MODEL_NAME)) {
        return createOrGetMesh(render, DEFAULT_MODEL_NAME);
      }
      throw e; // If default model also fails, propagate the exception
    }
  }

  /**
   * Creates or retrieves a cached texture to prevent memory leaks
   */
  private Texture createOrGetTexture(ARViewRender render, String textureName) throws IOException {
    if (textureName == null || textureName.isEmpty()) {
      textureName = DEFAULT_TEXTURE_NAME;
    }

    // Check if texture is already cached
    Texture cachedTexture = textureCache.get(textureName);
    if (cachedTexture != null) {
      return cachedTexture;
    }

    // Create new texture and cache it
    try {
      Log.i("Object renderer", "creating texture ");
      Texture newTexture = Texture.createFromAsset(
              render,
              textureName,
              Texture.WrapMode.REPEAT,
              Texture.ColorFormat.LINEAR
      );
      textureCache.put(textureName, newTexture);
      return newTexture;
    } catch (IOException e) {
      Log.e(TAG, "Failed to load texture: " + textureName + ", using default", e);
      // Try to load default texture instead
      if (!textureName.equals(DEFAULT_TEXTURE_NAME)) {
        return createOrGetTexture(render, DEFAULT_TEXTURE_NAME);
      }
      throw e; // If default texture also fails, propagate the exception
    }
  }

  /**
   * Cleans up resources when no longer needed
   */
  public void cleanup() {
    // Clean up all cached meshes
    for (Mesh mesh : meshCache.values()) {
      if (mesh != null) {
        try {
          mesh.close();
        } catch (Exception e) {
          Log.e(TAG, "Error closing mesh", e);
        }
      }
    }
    meshCache.clear();

    // Clean up all cached textures
    for (Texture texture : textureCache.values()) {
      if (texture != null) {
        try {
          texture.close();
        } catch (Exception e) {
          Log.e(TAG, "Error closing texture", e);
        }
      }
    }
    textureCache.clear();

    // Clean up shader
    if (shader != null) {
      try {
        shader.close();
      } catch (Exception e) {
        Log.e(TAG, "Error closing shader", e);
      }
      shader = null;
    }
  }

  /**
   * Draws the model.
   *
   * @param render The ARViewRender to use for drawing
   * @param allObjectNodes Collection of ARNodes to render
   * @param viewMatrix A 4x4 view matrix, in column-major order
   * @param cameraProjection A 4x4 projection matrix, in column-major order
   */
  public void draw(
          ARViewRender render,
          Collection<ARNode> allObjectNodes,
          float[] viewMatrix,
          float[] cameraProjection,
          Framebuffer virtualSceneFramebuffer
  ) {
    if (allObjectNodes == null || allObjectNodes.isEmpty()) {
      return;
    }
    Log.i("Object renderer", "number of ar nodes is "+ allObjectNodes.size());
    for (ARNode arNode : allObjectNodes) {
      try {
        Anchor anchor = arNode.Anchor();
        Trackable trackable = arNode.Trackable();

        if (anchor == null || trackable == null) {
          continue;
        }

        // Skip nodes that aren't being tracked
        if (anchor.getTrackingState() != TrackingState.TRACKING) {
          continue;
        }

        Log.i(TAG, "Rendering node: " + arNode.toString() + " " + arNode.Model());

        Mesh nodeMesh = createOrGetMesh(render, arNode.Model());
        String textureToUse = arNode.Texture();
        Texture nodeTexture = createOrGetTexture(render, textureToUse);

        // Set the texture for this render pass
        shader.setTexture("u_Texture", nodeTexture);

        // Get the world position of the anchor
        float[] anchorMatrix = new float[16];
        anchor.getPose().toMatrix(anchorMatrix, 0);

        // Apply transformations
        System.arraycopy(anchorMatrix, 0, modelMatrix, 0, 16);
        updateModelMatrix(modelMatrix, 1.0f);
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraProjection, 0, modelViewMatrix, 0);

        // Populate the shader uniforms for this frame
        shader.setMat4("u_ModelView", modelViewMatrix);
        shader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
        shader.setVec4("u_LightingParameters", defaultLightingParameters);
        shader.setVec4("u_MaterialParameters", defaultMaterialParameters);
        shader.setVec4("u_ColorCorrectionParameters", defaultColorCorrectionParameters);
        shader.setVec4("u_ObjColor", defaultObjectColor);

        // Draw the mesh with this shader
        render.draw(nodeMesh, shader, virtualSceneFramebuffer);
      } catch (Exception e) {
        Log.e(TAG, "Error rendering object: " + e.toString(), e);
      }
    }
  }
}