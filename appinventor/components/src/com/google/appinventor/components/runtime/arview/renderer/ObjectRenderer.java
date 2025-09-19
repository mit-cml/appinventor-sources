package com.google.appinventor.components.runtime.arview.renderer;

import android.opengl.Matrix;
import android.util.Log;
import android.opengl.GLES30;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.ar.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.Trackable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Renders an object loaded from an OBJ file in OpenGL. */
@UsesAssets(fileNames = "ar_object.vert, ar_object.frag," +
        "pawn_albedo.png, pawn.obj, cube.obj, sphere.obj, Palette.png")
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
  private static final String DEFAULT_MODEL_NAME = Form.ASSETS_PREFIX +"sphere.obj";
  private static final String DEFAULT_TEXTURE_NAME = Form.ASSETS_PREFIX +"Palette.png";

  // Shader names.
  private static final String VERTEX_SHADER_NAME = Form.ASSETS_PREFIX +"ar_object.vert";
  private static final String FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX +"ar_object.frag";

  // Cache for meshes and textures
  private final Map<String, Mesh> meshCache = new HashMap<>();
  private final Map<String, Texture> textureCache = new HashMap<>();

  private Shader shader;


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
  public float[] updateModelMatrix(float[] modelMatrix, float scaleFactor, float[] rotation) {

    float[] result = new float[16];
    Matrix.setIdentityM(result, 0);
    System.arraycopy(modelMatrix, 0, result, 0, 16);

    // Apply visual rotation if it exists
    //float[] rotationMatrix = new float[16];
    //Matrix.multiplyMM(result, 0, result, 0, rotationMatrix, 0);
    // Convert quaternion to full 4x4 rotation matrix
    float[] rotationMatrix = new float[16];
    quaternionToMatrix(rotation, rotationMatrix);
    Matrix.multiplyMM(result, 0, result, 0, rotationMatrix, 0);

    float[] scaleMatrix = new float[16];
    Matrix.setIdentityM(scaleMatrix, 0);
    scaleMatrix[0] = scaleFactor;
    scaleMatrix[5] = scaleFactor;
    scaleMatrix[10] = scaleFactor;
    Matrix.multiplyMM(result, 0, result, 0, scaleMatrix, 0);
    return result;
  }


  // Helper method to convert quaternion to 4x4 matrix
  private void quaternionToMatrix(float[] quaternion, float[] matrix) {
    float x = quaternion[0];
    float y = quaternion[1];
    float z = quaternion[2];
    float w = quaternion[3];

    float x2 = x + x;
    float y2 = y + y;
    float z2 = z + z;
    float xx = x * x2;
    float xy = x * y2;
    float xz = x * z2;
    float yy = y * y2;
    float yz = y * z2;
    float zz = z * z2;
    float wx = w * x2;
    float wy = w * y2;
    float wz = w * z2;

    matrix[0] = 1f - (yy + zz);
    matrix[1] = xy + wz;
    matrix[2] = xz - wy;
    matrix[3] = 0f;

    matrix[4] = xy - wz;
    matrix[5] = 1f - (xx + zz);
    matrix[6] = yz + wx;
    matrix[7] = 0f;

    matrix[8] = xz + wy;
    matrix[9] = yz - wx;
    matrix[10] = 1f - (xx + yy);
    matrix[11] = 0f;

    matrix[12] = 0f;
    matrix[13] = 0f;
    matrix[14] = 0f;
    matrix[15] = 1f;
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
      Log.d(TAG, "loading model: " + modelName );
      Mesh newMesh = Mesh.createFromAsset(render, modelName); // TODO CSB - provide for internal and external meshes..
      meshCache.put(modelName, newMesh);
      return newMesh;
    } catch (IOException e) {
      Log.e(TAG, "FAILED TO LOAD MODEL " + modelName + ", using default", e);
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

  private void renderSingleObject(ARViewRender render, ARNode arNode,
                                  float[] viewMatrix, float[] cameraProjection, Framebuffer virtualFrameBuffer) {
    try {
      Anchor anchor = arNode.Anchor();
      if (anchor == null || anchor.getTrackingState() != TrackingState.TRACKING) {
        return;
      }

      Mesh nodeMesh = createOrGetMesh(render, arNode.Model());
      Texture nodeTexture = createOrGetTexture(render, arNode.Texture());

      if (nodeMesh == null || nodeTexture == null) {
        return;
      }

      // Calculate matrices
      float[] anchorMatrix = new float[16];
      anchor.getPose().toMatrix(anchorMatrix, 0);
      float[] modelMatrix = updateModelMatrix(anchorMatrix, arNode.Scale(), arNode.Rotation());
      float[] localModelViewMatrix = new float[16];
      float[] localModelViewProjectionMatrix = new float[16];

      Matrix.multiplyMM(localModelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
      Matrix.multiplyMM(localModelViewProjectionMatrix, 0, cameraProjection, 0, localModelViewMatrix, 0);

      // Set shader uniforms
      shader.setTexture("u_Texture", nodeTexture);
     // shader.setVec4("u_ObjColor", defaultObjectColor);

      // CRITICAL: Set the transformation matrices
      //Log.d(TAG, "Setting matrices for " + arNode.Model());
      //shader.setMat4("u_ModelView", localModelViewMatrix);
      shader.setMat4("u_ModelViewProjection", localModelViewProjectionMatrix);
      //Log.d(TAG, "Matrices set successfully");

      render.draw(nodeMesh, shader, virtualFrameBuffer);

    } catch (Exception e) {
      Log.e(TAG, "Error rendering single object: " + e.toString(), e);
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
          /*Pose cameraPose,*/
          float [] viewMatrix,
          float[] cameraProjection,
          Framebuffer virtualFrameBuffer
  ) {
    if (allObjectNodes == null || allObjectNodes.isEmpty()) {
      return;
    }
    Log.i("Object renderer", "number of ar nodes is "+ allObjectNodes.size());

    //shader.setVec4("u_LightingParameters", defaultLightingParameters);
    //shader.setVec4("u_MaterialParameters", defaultMaterialParameters);
    //shader.setVec4("u_ColorCorrectionParameters", defaultColorCorrectionParameters);
    //shader.setVec4("u_ObjColor", defaultObjectColor);
    //shader.setDepthTest(true);
    //shader.setDepthWrite(true);

    //ARNode arNode = (ARNode) ((ArrayList) allObjectNodes).get(0);
    for (ARNode arNode : allObjectNodes) {
      try {
        renderSingleObject(render, arNode, viewMatrix, cameraProjection, virtualFrameBuffer);
      } catch (Exception e) {
        Log.e(TAG, "Error rendering object: " + e.toString(), e);
      }
   }
  }
}