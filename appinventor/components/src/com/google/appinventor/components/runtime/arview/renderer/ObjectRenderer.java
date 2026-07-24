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
@UsesAssets(fileNames = "ar_object.vert, ar_object.frag, ar_unlit_object.vert," +
    "ar_text.frag, ar_video.vert, ar_video.frag," +
    "pawn_albedo.png, pawn.obj, cube.obj, sphere.obj, plane.obj, quad.obj, Palette.png")
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
  private static final String DEFAULT_MODEL_NAME = Form.ASSETS_PREFIX + "sphere.obj";
  private static final String DEFAULT_TEXTURE_NAME = Form.ASSETS_PREFIX + "Palette.png";

  // Shader names.
  private static final String VERTEX_SHADER_NAME = Form.ASSETS_PREFIX + "ar_object.vert";
  private static final String FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX + "ar_object.frag";

  // Video (external OES) shader names — see ar_video.vert / ar_video.frag.
  private static final String VIDEO_VERTEX_SHADER_NAME = Form.ASSETS_PREFIX + "ar_video.vert";
  private static final String VIDEO_FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX + "ar_video.frag";

  // Text shader: ar_unlit_object.vert + ar_text.frag (unlit frag variant that
  // preserves the bitmap's alpha for antialiased glyph edges).
  private static final String TEXT_VERTEX_SHADER_NAME = Form.ASSETS_PREFIX + "ar_unlit_object.vert";
  private static final String TEXT_FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX + "ar_text.frag";

  // Cache for meshes and textures
  private final Map<String, Mesh> meshCache = new HashMap<>();
  private final Map<String, Texture> textureCache = new HashMap<>();

  // Per-node wrappers around dynamically generated GL textures (text bitmaps).
  // Keyed by node; invalidated when the node's GL texture id changes.
  private final Map<ARNode, Texture> dynamicTextureCache = new HashMap<>();
  private final Map<ARNode, Integer> dynamicTextureIds = new HashMap<>();

  private Shader shader;
  private Shader videoShader;          // lazily created; samples GL_TEXTURE_EXTERNAL_OES
  private Shader textShader;           // lazily created; unlit, alpha-preserving
  private ARViewRender render;         // kept for lazy shader creation
  private PlaneFinder planeFinder = null;

  public void setPlaneFinder(PlaneFinder finder) {
    this.planeFinder = finder;
  }

  // Default lighting and material parameters
  private final float[] defaultLightingParameters = {0.0f, 1.0f, 0.0f, 1.0f};
  private final float[] defaultMaterialParameters = {0.3f, 0.5f, 0.2f, 16.0f};
  private final float[] defaultColorCorrectionParameters = {1.0f, 1.0f, 1.0f, 1.0f};
  private final float[] defaultObjectColor = {1.0f, 1.0f, 1.0f, 1.0f}; // White

  // Depth occlusion
  private int depthTextureGlId = 0;
  private Texture depthTexture = null;
  private final float[] depthUvTransform = new float[16];
  private boolean occlusionEnabledByDepth = false;

  public ObjectRenderer(ARViewRender render) throws IOException {
    this.render = render;
    Texture defaultTexture = createOrGetTexture(render, DEFAULT_TEXTURE_NAME);
    shader = Shader.createFromAssets(
        render,
        VERTEX_SHADER_NAME,
        FRAGMENT_SHADER_NAME,
        null
    ).setTexture("u_Texture", defaultTexture);

    // Create depth texture — RG8 format matches ARCore depth packing
    int[] ids = new int[1];
    GLES30.glGenTextures(1, ids, 0);
    depthTextureGlId = ids[0];
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTextureGlId);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

    // Wrap in Texture object so Shader can manage the texture unit
    depthTexture = new Texture(render, Texture.Target.TEXTURE_2D,
        Texture.WrapMode.CLAMP_TO_EDGE, /*useMipmaps=*/ false, depthTextureGlId);

    // Register with shader — texture unit assigned automatically by Shader
    shader.setTexture("u_DepthTexture", depthTexture);

    // Identity UV transform until first depth frame arrives
    android.opengl.Matrix.setIdentityM(depthUvTransform, 0);
    shader.setMat4("u_UvTransform", depthUvTransform);
    shader.setFloat("u_OcclusionEnabled", 0.0f);

    try {
      shader.setVec4("u_PlaneEquation", new float[]{0f, 1f, 0f, 0f});
      shader.setFloat("u_PlaneOcclusionEnabled", 0.0f);
    } catch (IllegalArgumentException e) {
      Log.w(TAG, "Plane occlusion uniforms not found: " + e.getMessage());
    }
  }

  /** Lazily builds the external-OES shader used by VideoNode. */
  private Shader getOrCreateVideoShader() {
    if (videoShader == null) {
      try {
        videoShader = Shader.createFromAssets(
            render, VIDEO_VERTEX_SHADER_NAME, VIDEO_FRAGMENT_SHADER_NAME, null);
        videoShader.setBlend(Shader.BlendFactor.ONE, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA)
            .setDepthTest(true)
            .setDepthWrite(false);
      } catch (IOException e) {
        Log.e(TAG, "Failed to create video shader — VideoNodes will not render", e);
      }
    }
    return videoShader;
  }

  /** Lazily builds the unlit alpha-preserving shader used by TextNode. */
  private Shader getOrCreateTextShader() {
    if (textShader == null) {
      try {
        textShader = Shader.createFromAssets(
            render, TEXT_VERTEX_SHADER_NAME, TEXT_FRAGMENT_SHADER_NAME, null);
        textShader.setBlend(Shader.BlendFactor.ONE, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA)
            .setDepthTest(true)
            .setDepthWrite(false);
      } catch (IOException e) {
        Log.e(TAG, "Failed to create text shader — TextNodes will not render", e);
      }
    }
    return textShader;
  }

  /**
   * Updates the object model matrix and applies scaling.
   *
   * @param modelMatrix A 4x4 model-to-world transformation matrix, column-major.
   * @param scaleFactor Uniform scale applied before the {@code modelMatrix}.
   * @param rotation    Quaternion (x, y, z, w).
   */
  public float[] updateModelMatrix(float[] modelMatrix, float scaleFactor, float[] rotation) {
    return updateModelMatrix(modelMatrix, scaleFactor, rotation, null);
  }

  /**
   * As above, with an optional per-axis geometry scale (meters) multiplied in
   * after the uniform scale. Used by TextNode/VideoNode to size their unit
   * quad/box to measured world dimensions.
   */
  public float[] updateModelMatrix(float[] modelMatrix, float scaleFactor,
                                   float[] rotation, float[] geometryScale) {
    float[] result = new float[16];
    Matrix.setIdentityM(result, 0);
    System.arraycopy(modelMatrix, 0, result, 0, 16);

    // Rotation (quaternion → matrix)
    float[] rotationMatrix = new float[16];
    quaternionToMatrix(rotation, rotationMatrix);
    Matrix.multiplyMM(result, 0, result, 0, rotationMatrix, 0);

    // Uniform node scale
    float[] scaleMatrix = new float[16];
    Matrix.setIdentityM(scaleMatrix, 0);
    scaleMatrix[0] = scaleFactor;
    scaleMatrix[5] = scaleFactor;
    scaleMatrix[10] = scaleFactor;
    Matrix.multiplyMM(result, 0, result, 0, scaleMatrix, 0);

    // Optional per-axis geometry scale (text/video quads and boxes)
    if (geometryScale != null) {
      float[] geo = new float[16];
      Matrix.setIdentityM(geo, 0);
      geo[0]  = geometryScale[0];
      geo[5]  = geometryScale[1];
      geo[10] = geometryScale[2];
      Matrix.multiplyMM(result, 0, result, 0, geo, 0);
    }
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

  /** Creates or retrieves a cached mesh to prevent memory leaks. */
  private Mesh createOrGetMesh(ARViewRender render, String modelName) throws IOException {
    if (modelName == null || modelName.isEmpty()) {
      modelName = DEFAULT_MODEL_NAME;
    }

    Mesh cachedMesh = meshCache.get(modelName);
    if (cachedMesh != null) {
      return cachedMesh;
    }

    try {
      Log.d(TAG, "loading model: " + modelName);
      Mesh newMesh = Mesh.createFromAsset(render, modelName);
      meshCache.put(modelName, newMesh);
      return newMesh;
    } catch (IOException e) {
      Log.e(TAG, "FAILED TO LOAD MODEL " + modelName + ", using default", e);
      if (!modelName.equals(DEFAULT_MODEL_NAME)) {
        return createOrGetMesh(render, DEFAULT_MODEL_NAME);
      }
      throw e;
    }
  }

  /** Creates or retrieves a cached texture to prevent memory leaks. */
  private Texture createOrGetTexture(ARViewRender render, String textureName) throws IOException {
    if (textureName == null || textureName.isEmpty()) {
      textureName = DEFAULT_TEXTURE_NAME;
    }

    Texture cachedTexture = textureCache.get(textureName);
    if (cachedTexture != null) {
      return cachedTexture;
    }

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
      throw e;
    }
  }

  /**
   * Wraps a node's dynamically generated GL texture (e.g. TextNode's rasterized
   * text) in a Texture object the Shader can bind. Re-wraps if the node has
   * regenerated its texture under a new GL id.
   */
  private Texture getOrWrapDynamicTexture(ARNode node, int glTextureId) {
    Integer knownId = dynamicTextureIds.get(node);
    Texture wrapped = dynamicTextureCache.get(node);
    if (wrapped != null && knownId != null && knownId == glTextureId) {
      return wrapped;
    }
    // Id changed or first sight of this node — (re)wrap.
    // NOTE: do NOT close() the old wrapper here if Texture.close() deletes the
    // underlying GL id — the node owns its texture's lifetime.
    Texture t = new Texture(render, Texture.Target.TEXTURE_2D,
        Texture.WrapMode.CLAMP_TO_EDGE, /*useMipmaps=*/ true, glTextureId);
    dynamicTextureCache.put(node, t);
    dynamicTextureIds.put(node, glTextureId);
    return t;
  }

  /** Drop cached wrappers for a removed node. Call from node removal path. */
  public void evictNode(ARNode node) {
    dynamicTextureCache.remove(node);
    dynamicTextureIds.remove(node);
  }

  /** Cleans up resources when no longer needed. */
  public void cleanup() {
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

    dynamicTextureCache.clear();
    dynamicTextureIds.clear();

    if (shader != null) {
      try {
        shader.close();
      } catch (Exception e) {
        Log.e(TAG, "Error closing shader", e);
      }
      shader = null;
    }

    if (videoShader != null) {
      try {
        videoShader.close();
      } catch (Exception e) {
        Log.e(TAG, "Error closing video shader", e);
      }
      videoShader = null;
    }

    if (textShader != null) {
      try {
        textShader.close();
      } catch (Exception e) {
        Log.e(TAG, "Error closing text shader", e);
      }
      textShader = null;
    }

    if (depthTexture != null) {
      depthTexture.close();
      depthTexture = null;
      depthTextureGlId = 0;
    }
  }

  /**
   * Upload a new ARCore depth image to the depth texture.
   * Call this from onDrawFrame inside the acquireDepthImage16Bits() block,
   * BEFORE drawObjects().
   */
  public void updateDepthTexture(android.media.Image depthImage, float[] uvTransform) {
    Log.i("Object Renderer:", "Updating camera depth texture");
    if (depthTextureGlId == 0) return;

    System.arraycopy(uvTransform, 0, depthUvTransform, 0, 16);
    occlusionEnabledByDepth = true;

    android.media.Image.Plane plane = depthImage.getPlanes()[0];
    java.nio.ByteBuffer buffer = plane.getBuffer();

    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTextureGlId);
    GLES30.glTexImage2D(
        GLES30.GL_TEXTURE_2D, 0,
        GLES30.GL_RG8,
        depthImage.getWidth(),
        depthImage.getHeight(),
        0,
        GLES30.GL_RG,
        GLES30.GL_UNSIGNED_BYTE,
        buffer
    );
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
  }

  // ===========================================================================
  // Anchor / world matrix resolution — shared by all node types
  // ===========================================================================

  /** Returns the node's anchor/world matrix, or null if the node has no valid pose yet. */
  private float[] resolveAnchorMatrix(ARNode arNode) {
    ARNodeBase base = (ARNodeBase) arNode;
    float[] anchorMatrix = new float[16];
    if (base.currentWorldMatrix != null) {
      System.arraycopy(base.currentWorldMatrix, 0, anchorMatrix, 0, 16);
      return anchorMatrix;
    }
    Anchor anchor = arNode.Anchor();
    if (anchor == null || anchor.getTrackingState() != TrackingState.TRACKING) {
      return null;
    }
    anchor.getPose().toMatrix(anchorMatrix, 0);
    return anchorMatrix;
  }

  /** Computes MV and MVP and sets them (plus u_Model) on the given shader. */
  private void setTransformUniforms(Shader target, float[] modelMatrix,
                                    float[] viewMatrix, float[] cameraProjection) {
    float[] mv = new float[16];
    float[] mvp = new float[16];
    Matrix.multiplyMM(mv, 0, viewMatrix, 0, modelMatrix, 0);
    Matrix.multiplyMM(mvp, 0, cameraProjection, 0, mv, 0);
    target.setMat4("u_Model", modelMatrix);
    target.setMat4("u_ModelView", mv);
    target.setMat4("u_ModelViewProjection", mvp);
  }

  /**
   * For the unlit text/video shaders: sets only u_ModelViewProjection, which
   * is the sole uniform their vertex shader actually uses. u_ModelView is
   * declared in ar_unlit_object.vert but unused, so the GLSL compiler may
   * strip it — setting it unconditionally could throw. Attempt it, tolerate
   * absence.
   */
  private void setMinimalTransformUniforms(Shader target, float[] modelMatrix,
                                           float[] viewMatrix, float[] cameraProjection) {
    float[] mv = new float[16];
    float[] mvp = new float[16];
    Matrix.multiplyMM(mv, 0, viewMatrix, 0, modelMatrix, 0);
    Matrix.multiplyMM(mvp, 0, cameraProjection, 0, mv, 0);
    target.setMat4("u_ModelViewProjection", mvp);
    try {
      target.setMat4("u_ModelView", mv);
    } catch (IllegalArgumentException e) {
      // Uniform optimized out — fine.
    }
  }

  // ===========================================================================
  // TextNode branch
  // ===========================================================================

  private void renderTextNode(ARViewRender render, TextNode textNode,
                              float[] viewMatrix, float[] cameraProjection) {
    try {
      if (!textNode.isReadyToRender()) {
        return; // empty text, zero font size, or texture not yet uploaded
      }
      float[] anchorMatrix = resolveAnchorMatrix(textNode);
      if (anchorMatrix == null) return;

      // Mesh: cube.obj when DepthInCentimeters > 0, plane.obj when flat.
      // Uses the node's own accessor — independent of whether ARNodeBase.Model()
      // stores the string.
      Mesh mesh = createOrGetMesh(render, textNode.getObjectModel());
      if (mesh == null) return;

      // Texture: the node-owned rasterized text texture, wrapped for Shader.
      int glId = textNode.getTextTextureId();
      if (glId == 0) return;
      Texture textTexture = getOrWrapDynamicTexture(textNode, glId);

      Shader tShader = getOrCreateTextShader();
      if (tShader == null) return;

      float[] modelMatrix = updateModelMatrix(
          anchorMatrix, textNode.Scale(),
          ((ARNodeBase) textNode).getCurrentRotation(),
          textNode.getGeometryScale());

      tShader.setTexture("u_Texture", textTexture);
      setMinimalTransformUniforms(tShader, modelMatrix, viewMatrix, cameraProjection);

      // Text quads have alpha (transparent backgrounds, antialiased glyph
      // edges): blend on, depth TEST on so scene occludes text, depth WRITE
      // off so text doesn't punch holes for other transparent nodes.
      GLES30.glEnable(GLES30.GL_BLEND);
      GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
      GLES30.glEnable(GLES30.GL_DEPTH_TEST);
      GLES30.glDepthMask(false);

      render.draw(mesh, tShader);

      GLES30.glDepthMask(true);
    } catch (Exception e) {
      Log.e(TAG, "Error rendering TextNode: " + e, e);
    }
  }

  // ===========================================================================
  // VideoNode branch
  // ===========================================================================

  private void renderVideoNode(ARViewRender render, VideoNode videoNode,
                               float[] viewMatrix, float[] cameraProjection) {
    try {
      if (!videoNode.isReadyToRender()) {
        Log.e(TAG, "VideoNode not ready to render ");
        return; // GL not initialized or no decoded frame yet
      }
      float[] anchorMatrix = resolveAnchorMatrix(videoNode);
      if (anchorMatrix == null) return;

      Shader vShader = getOrCreateVideoShader();
      if (vShader == null) return;

      Mesh mesh = createOrGetMesh(render, videoNode.getObjectModel()); // plane.obj
      if (mesh == null) return;

      float[] modelMatrix = updateModelMatrix(
          anchorMatrix, videoNode.Scale(),
          ((ARNodeBase) videoNode).getCurrentRotation(),
          videoNode.getGeometryScale());

      setMinimalTransformUniforms(vShader, modelMatrix, viewMatrix, cameraProjection);

      // Bind the external OES texture manually on a known unit. If the Shader/
      // Texture classes support Target.TEXTURE_EXTERNAL_OES, prefer wrapping it
      // like the text texture instead of this manual bind.
      GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
      GLES30.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
          videoNode.getVideoTextureId());
      vShader.setInt("u_Texture", 0);

      // Opaque video: no blend, full depth participation.
      GLES30.glDisable(GLES30.GL_BLEND);
      GLES30.glEnable(GLES30.GL_DEPTH_TEST);
      GLES30.glDepthMask(true);
      Log.e(TAG, "drawing video node");
      render.draw(mesh, vShader);

      GLES30.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
      GLES30.glEnable(GLES30.GL_BLEND);
    } catch (Exception e) {
      Log.e(TAG, "Error rendering VideoNode: " + e, e);
    }
  }

  // ===========================================================================
  // Primitive branch (spheres, boxes, capsules) — original path, unchanged logic
  // ===========================================================================

  private void renderSingleObject(ARViewRender render, ARNode arNode,
                                  float[] viewMatrix, float[] cameraProjection) {
    try {
      float[] anchorMatrix = resolveAnchorMatrix(arNode);
      if (anchorMatrix == null) return;

      Mesh nodeMesh = createOrGetMesh(render, arNode.Model());
      Texture nodeTexture = createOrGetTexture(render, arNode.Texture());

      if (nodeMesh == null || nodeTexture == null) return;
      Log.i("ObjRenderer:", "rendering single object");

      float[] modelMatrix = updateModelMatrix(
          anchorMatrix, arNode.Scale(), ((ARNodeBase) arNode).getCurrentRotation());

      setTransformUniforms(shader, modelMatrix, viewMatrix, cameraProjection);

      // World and camera positions for occlusion checks
      float[] sphereWorldPos = {
          anchorMatrix[12], anchorMatrix[13], anchorMatrix[14]};
      float[] invView = new float[16];
      Matrix.invertM(invView, 0, viewMatrix, 0);
      float[] cameraWorldPos = {invView[12], invView[13], invView[14]};

      shader.setTexture("u_Texture", nodeTexture);
      shader.setMat4("u_UvTransform", depthUvTransform);
      shader.setFloat("u_OcclusionEnabled",
          occlusionEnabledByDepth ? 1.0f : 0.0f);

      // Plane-based occlusion — only active when depth is not available
      if (!occlusionEnabledByDepth && planeFinder != null) {
        Log.i("ObjRenderer", "Depth not supported, setting PlaneFinder");
        float sphereRadius = (arNode instanceof SphereNode)
            ? ((SphereNode) arNode).Scale()
            : 0.05f;
        com.google.ar.core.Plane occludingPlane =
            planeFinder.findOccludingPlane(sphereWorldPos, cameraWorldPos, sphereRadius);
        if (occludingPlane != null) {
          float[] poseMatrix = new float[16];
          occludingPlane.getCenterPose().toMatrix(poseMatrix, 0);
          float nx = poseMatrix[4];
          float ny = poseMatrix[5];
          float nz = poseMatrix[6];
          float[] center = occludingPlane.getCenterPose().getTranslation();
          float d = -(nx * center[0] + ny * center[1] + nz * center[2]);
          shader.setVec4("u_PlaneEquation", new float[]{nx, ny, nz, d});
          shader.setFloat("u_PlaneOcclusionEnabled", 1.0f);
        } else {
          shader.setFloat("u_PlaneOcclusionEnabled", 0.0f);
        }
      } else {
        shader.setFloat("u_PlaneOcclusionEnabled", 0.0f);
      }

      // Opaque primitives: full depth participation.
      GLES30.glEnable(GLES30.GL_DEPTH_TEST);
      GLES30.glDepthMask(true);

      render.draw(nodeMesh, shader);

    } catch (Exception e) {
      Log.e(TAG, "Error rendering single object: " + e.toString(), e);
    }
  }

  /**
   * Draws all nodes: opaque primitives and video first, transparent text last
   * (correct blending requires transparent geometry drawn after opaque).
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
      float[] cameraProjection
  ) {
    if (allObjectNodes == null || allObjectNodes.isEmpty()) {
      return;
    }
    Log.i("Object renderer", "number of ar nodes is " + allObjectNodes.size());

    ArrayList<TextNode> textNodes = new ArrayList<>();

    // Pass 1: opaque — primitives and video
    for (ARNode arNode : allObjectNodes) {
      try {
        if (arNode instanceof TextNode) {
          textNodes.add((TextNode) arNode);       // defer to pass 2
        } else if (arNode instanceof VideoNode) {
          renderVideoNode(render, (VideoNode) arNode, viewMatrix, cameraProjection);
        } else {
          renderSingleObject(render, arNode, viewMatrix, cameraProjection);
        }
      } catch (Exception e) {
        Log.e(TAG, "Error rendering object: " + e.toString(), e);
      }
    }

    // Pass 2: transparent — text quads/boxes over the opaque scene
    for (TextNode textNode : textNodes) {
      try {
        renderTextNode(render, textNode, viewMatrix, cameraProjection);
      } catch (Exception e) {
        Log.e(TAG, "Error rendering text node: " + e.toString(), e);
      }
    }
  }
}