// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;

// TODO: update the component version
@UsesAssets(fileNames = "cube.obj, quad.obj")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays text in an ARView3D. The text is rasterized " +
        "onto the front face of a box whose depth is controlled by DepthInCentimeters; " +
        "the box's sides and back use the node's FillColor. When depth is zero the text " +
        "renders as a flat quad.",
    category = ComponentCategory.AR, iconName = "images/textNode.png")
@SimpleObject
public final class TextNode extends ARNodeBase implements ARText {

  private static final String LOG_TAG = "TextNode";

  // ---------------------------------------------------------------------------
  // Texture rasterization constants
  // ---------------------------------------------------------------------------

  /** Pixel height at which glyphs are rasterized into the texture. This is
   *  texture *resolution*, not world size — world size comes from
   *  fontSizeInCentimeters. 128px is crisp at typical AR label distances. */
  private static final float TEXT_RASTER_SIZE_PX = 128f;

  /** Padding (px) around the text inside the bitmap, so linear filtering and
   *  mipmapping never bleed the glyph edges into the border. */
  private static final int TEXT_PADDING_PX = 16;

  /** GL requires power-of-two textures for GL_REPEAT wrap modes; we use
   *  clamp-to-edge so NPOT is fine on ES3, but we still cap texture size. */
  private static final int MAX_TEXTURE_DIM = 2048;

  // ---------------------------------------------------------------------------
  // Text & appearance state
  // ---------------------------------------------------------------------------

  private String text = "";
  private float fontSizeInCentimeters = 6.0f;
  private float depthInCentimeters = 1.0f;
  private String font = "";
  private int textColor = Color.WHITE;
  private int backgroundColor = Color.TRANSPARENT;

  // ---------------------------------------------------------------------------
  // Measured geometry (world meters, at Scale() == 1)
  //
  // Unlike the old hardcoded width/height fields, these are derived from the
  // rasterized bitmap's aspect ratio each time the text/font/size changes, so
  // the box (and its collision volume) always hugs the rendered text.
  // ---------------------------------------------------------------------------

  private float worldWidth  = 0f;   // meters, before Scale()
  private float worldHeight = 0f;   // meters, before Scale()

  // ---------------------------------------------------------------------------
  // Render resources — owned by the GL thread
  // ---------------------------------------------------------------------------

  /** Set on the UI thread whenever text/font/size/color changes; consumed on
   *  the GL thread by updateTextTextureIfNeeded(). Volatile is sufficient:
   *  single writer semantics per field, and the GL thread only ever clears it. */
  private volatile boolean textureNeedsUpdate = false;

  /** CPU-side bitmap staged for upload. Built on whichever thread calls the
   *  setter (cheap), uploaded and recycled on the GL thread. */
  private volatile Bitmap pendingTextBitmap = null;

  /** GL texture name holding the rasterized text; 0 = none yet. Only touched
   *  on the GL thread. */
  private int textTextureId = 0;

  /** True once the node has geometry + texture and may be drawn. This is the
   *  flag ObjectRenderer's filter should consult (see isReadyToRender()). */
  private volatile boolean renderable = false;

  public TextNode(final ARNodeContainer container) {
    super(container);
    // Geometry is chosen per-frame in getObjectModel(): cube.obj when depth > 0,
    // plane.obj when depth == 0. We register the cube by default.
    Model(Form.ASSETS_PREFIX + "cube.obj");
    container.addNode(this);
    // Note: no Texture("") call — an empty texture assignment is what left the
    // old version materialless. The text texture is generated, not asset-loaded.
  }

  // ===========================================================================
  // Text & Font Properties
  // ===========================================================================

  @Override
  @SimpleProperty(description = "Text to display by the TextNode. If this is " +
      "set to \"\", the TextNode will not be shown.")
  public String Text() { return text; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Text(String txt) {
    if (txt == null) {
      txt = "";
    }
    if (txt.equals(text)) {
      return;
    }
    text = txt;
    requestTextureRebuild();
    Log.i(LOG_TAG, "Text set to: " + txt);
  }

  @SimpleProperty(description = "Font family (e.g. \"sans-serif\", \"serif\", " +
      "\"monospace\", or a specific family name). Empty uses the default.")
  public String Font() { return font; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Font(String f) {
    if (f == null) {
      f = "";
    }
    if (f.equals(font)) {
      return;
    }
    font = f;
    requestTextureRebuild();
    Log.i(LOG_TAG, "Font set to: " + f);
  }

  @Override
  @SimpleProperty(description = "The font size in centimeters. Values less than " +
      "zero will be treated as their absolute value. When set to zero, the TextNode " +
      "will not be shown.")
  public float FontSizeInCentimeters() { return fontSizeInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6.0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FontSizeInCentimeters(float size) {
    float newSize = Math.abs(size);
    if (newSize == fontSizeInCentimeters) {
      return;
    }
    fontSizeInCentimeters = newSize;
    // Font size changes world dimensions but not the raster (we always
    // rasterize at TEXT_RASTER_SIZE_PX); still need to remeasure + recollide.
    recomputeWorldDimensions();
    updateCollisionShape();
    Log.i(LOG_TAG, "FontSizeInCentimeters set to: " + fontSizeInCentimeters);
  }

  @SimpleProperty(description = "The color of the text glyphs.")
  public int TextColor() { return textColor; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TextColor(int color) {
    if (color == textColor) {
      return;
    }
    textColor = color;
    requestTextureRebuild();
  }

  @SimpleProperty(description = "The background color behind the text on the " +
      "front face. Use a transparent color for floating text (only sensible " +
      "when DepthInCentimeters is 0).")
  public int BackgroundColor() { return backgroundColor; }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void BackgroundColor(int color) {
    if (color == backgroundColor) {
      return;
    }
    backgroundColor = color;
    requestTextureRebuild();
  }

  // ===========================================================================
  // Geometry Properties
  // ===========================================================================

  @Override
  @SimpleProperty(description = "How far, in centimeters, the TextNode extends along the z-axis. " +
      "Values less than zero will be treated as zero. When zero, the text renders " +
      "as a flat quad; when positive, as a solid box with FillColor sides.")
  public float DepthInCentimeters() { return depthInCentimeters; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1.0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void DepthInCentimeters(float depth) {
    float newDepth = Math.max(0f, depth);
    if (newDepth == depthInCentimeters) {
      return;
    }
    boolean geometryClassChanged = (depthInCentimeters == 0f) != (newDepth == 0f);
    depthInCentimeters = newDepth;
    if (geometryClassChanged) {
      // Switch between quad and box geometry.
      Model(getObjectModel());
    }
    updateCollisionShape();
    Log.i(LOG_TAG, "DepthInCentimeters set to: " + depthInCentimeters);
  }

  /** The asset backing this node's mesh: a box when it has depth, an upright
   *  unit quad when flat. (plane.obj is floor-oriented in XZ — unusable here.) */
  public String getObjectModel() {
    return Form.ASSETS_PREFIX + (depthInCentimeters > 0f ? "cube.obj" : "quad.obj");
  }

  // ===========================================================================
  // Rasterization pipeline
  // ===========================================================================

  /** Called from property setters (any thread). Builds the bitmap eagerly
   *  (it's cheap and thread-safe) and flags the GL thread to upload it. */
  private void requestTextureRebuild() {
    if (text.isEmpty() || fontSizeInCentimeters == 0f) {
      // Invisible by contract — drop render resources' relevance.
      renderable = false;
      pendingTextBitmap = null;
      textureNeedsUpdate = true;   // GL thread will release the old texture
      worldWidth = 0f;
      worldHeight = 0f;
      updateCollisionShape();
      return;
    }
    Bitmap bmp = createTextBitmap(text, TEXT_RASTER_SIZE_PX, textColor, backgroundColor, font);
    pendingTextBitmap = bmp;
    recomputeWorldDimensionsFrom(bmp.getWidth(), bmp.getHeight());
    updateCollisionShape();
    textureNeedsUpdate = true;
  }

  /** Rasterizes the string into an ARGB bitmap sized to fit it. */
  private static Bitmap createTextBitmap(String text, float textSizePx,
                                         int textColor, int bgColor, String fontFamily) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextSize(textSizePx);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.LEFT);
    if (fontFamily != null && !fontFamily.isEmpty()) {
      paint.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
    }

    Paint.FontMetrics fm = paint.getFontMetrics();
    int w = (int) Math.ceil(paint.measureText(text)) + 2 * TEXT_PADDING_PX;
    int h = (int) Math.ceil(fm.bottom - fm.top)      + 2 * TEXT_PADDING_PX;
    w = Math.max(1, Math.min(w, MAX_TEXTURE_DIM));
    h = Math.max(1, Math.min(h, MAX_TEXTURE_DIM));

    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(bgColor);
    canvas.drawText(text, TEXT_PADDING_PX, TEXT_PADDING_PX - fm.top, paint);
    return bitmap;
  }

  /** Maps bitmap aspect + fontSizeInCentimeters to world meters. */
  private void recomputeWorldDimensionsFrom(int bitmapW, int bitmapH) {
    worldHeight = fontSizeInCentimeters / 100f;                    // meters
    worldWidth  = worldHeight * (bitmapW / (float) bitmapH);       // keep aspect
  }

  /** Re-derive world dims from the staged/last-known bitmap aspect. */
  private void recomputeWorldDimensions() {
    Bitmap bmp = pendingTextBitmap;
    if (bmp != null) {
      recomputeWorldDimensionsFrom(bmp.getWidth(), bmp.getHeight());
    } else if (worldHeight > 0f) {
      float aspect = worldWidth / worldHeight;
      worldHeight = fontSizeInCentimeters / 100f;
      worldWidth  = worldHeight * aspect;
    }
  }

  // ===========================================================================
  // GL-thread hooks — call these from ObjectRenderer / ARViewRender
  // ===========================================================================

  /**
   * MUST be called on the GL thread (e.g. at the top of the per-frame draw for
   * this node, or in ObjectRenderer's update pass). Uploads any pending text
   * bitmap into a GL texture and marks the node renderable.
   */
  public void updateTextTextureIfNeeded() {
    if (!textureNeedsUpdate) {
      return;
    }
    textureNeedsUpdate = false;

    Bitmap bmp = pendingTextBitmap;
    pendingTextBitmap = null;

    if (bmp == null) {
      // Text was cleared — release GPU resources.
      deleteTextureIfPresent();
      renderable = false;
      return;
    }

    if (textTextureId == 0) {
      int[] ids = new int[1];
      GLES30.glGenTextures(1, ids, 0);
      textTextureId = ids[0];
    }
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textTextureId);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0);
    GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    bmp.recycle();

    renderable = true;
    Log.i(LOG_TAG, "Text texture uploaded (id=" + textTextureId + ")");
  }

  /** GL texture holding the rasterized text; 0 if none. Bind this on the front
   *  face when drawing (and, mirrored, on the back face if desired). Side faces
   *  should be drawn with FillColor via the untextured/unlit path. */
  public int getTextTextureId() { return textTextureId; }

  /**
   * The predicate ObjectRenderer's node filter should use for TextNodes:
   * geometry exists, texture is uploaded, and the node isn't contractually
   * invisible (empty text / zero font size).
   */
  public boolean isReadyToRender() {
    return renderable && !text.isEmpty() && fontSizeInCentimeters > 0f;
  }

  /**
   * Per-axis scale (meters) to apply to the unit cube/quad so it matches the
   * measured text dimensions. Multiply into the model matrix before the
   * node's own Scale()/rotation/translation.
   */
  public float[] getGeometryScale() {
    return new float[]{
        worldWidth,
        worldHeight,
        depthInCentimeters > 0f ? depthInCentimeters / 100f : 1f  // quad ignores z
    };
  }

  /** GL-thread cleanup. Call when the node is removed or the surface is destroyed. */
  public void releaseGlResources() {
    deleteTextureIfPresent();
    renderable = false;
  }

  private void deleteTextureIfPresent() {
    if (textTextureId != 0) {
      GLES30.glDeleteTextures(1, new int[]{textTextureId}, 0);
      textTextureId = 0;
    }
  }

  // ===========================================================================
  // Collision / Bounds — now derived from measured text dimensions
  // ===========================================================================

  @Override
  public void updateCollisionShape() {
    float scaledWidth  = worldWidth  * Scale();
    float scaledHeight = worldHeight * Scale();
    float scaledDepth  = Math.max(depthInCentimeters / 100f, 0.001f); // never a zero-thickness volume
    collisionVolume = new BoxVolume(scaledWidth, scaledHeight, scaledDepth);
    Log.i(LOG_TAG, "Collision shape updated — " + scaledWidth + " x " + scaledHeight
        + " x " + scaledDepth + " m");
  }

  @Override
  public float[] getModelBounds() {
    return new float[]{
        worldWidth  * Scale(),
        worldHeight * Scale(),
        depthInCentimeters / 100f
    };
  }

  // ===========================================================================
  // Scaling
  // ===========================================================================

  @Override
  @SimpleFunction(description = "Changes the text's scale by the given scalar, " +
      "maintaining bottom position if physics is enabled.")
  public void ScaleBy(float scalar) {
    Log.i(LOG_TAG, "Scaling text " + name + " by " + scalar);

    float oldScale = Scale();
    float newScale = oldScale * Math.abs(scalar);

    if (EnablePhysics()) {
      // Adjust Y so the bottom of the node stays grounded.
      float previousHalfHeight = (worldHeight * oldScale) / 2f;
      float newHalfHeight      = (worldHeight * newScale) / 2f;
      float[] currentPos = getCurrentPosition();
      currentPos[1] = currentPos[1] - previousHalfHeight + newHalfHeight;
      setCurrentPosition(currentPos);
    }

    Scale(newScale);
    updateCollisionShape();
    Log.i(LOG_TAG, "Scale complete: " + oldScale + " → " + newScale);
  }

  // ===========================================================================
  // Debug Helpers
  // ===========================================================================

  @SimpleFunction(description = "Logs the current collision shape dimensions for debugging.")
  public void DebugCollisionShape() {
    Log.i(LOG_TAG, "=== COLLISION SHAPE DEBUG ===");
    Log.i(LOG_TAG, "World width:       " + worldWidth + "m");
    Log.i(LOG_TAG, "World height:      " + worldHeight + "m");
    Log.i(LOG_TAG, "Scale:             " + Scale());
    Log.i(LOG_TAG, "Depth:             " + depthInCentimeters + "cm");
    Log.i(LOG_TAG, "Geometry:          " + getObjectModel());
    Log.i(LOG_TAG, "Renderable:        " + isReadyToRender());
    Log.i(LOG_TAG, "Texture id:        " + textTextureId);
    Log.i(LOG_TAG, "Has physics:       " + EnablePhysics());
    Log.i(LOG_TAG, "=============================");
  }

  @SimpleFunction(description = "Logs the current physics state for debugging.")
  public void DebugPhysicsState() {
    float[] pos = getCurrentPosition();
    Log.i(LOG_TAG, "=== PHYSICS STATE DEBUG ===");
    Log.i(LOG_TAG, "Position:          " + arrayToString(pos));
    Log.i(LOG_TAG, "Has physics:       " + EnablePhysics());
    Log.i(LOG_TAG, "Mass:              " + Mass());
    Log.i(LOG_TAG, "Scale:             " + Scale());
    Log.i(LOG_TAG, "Static Friction:   " + StaticFriction());
    Log.i(LOG_TAG, "Dynamic Friction:  " + DynamicFriction());
    Log.i(LOG_TAG, "Restitution:       " + Restitution());
    Log.i(LOG_TAG, "Drag Sensitivity:  " + DragSensitivity());
    Log.i(LOG_TAG, "Font size (cm):    " + fontSizeInCentimeters);
    Log.i(LOG_TAG, "Depth (cm):        " + depthInCentimeters);
    Log.i(LOG_TAG, "===========================");
  }

  // ---------------------------------------------------------------------------
  // Deferred / future properties (not yet implemented)
  //
  // TODO: WrapText       — controls whether text wraps within the node width
  // TODO: TextAlignment  — ALIGNMENT_NORMAL / CENTER / OPPOSITE
  // TODO: Truncation     — how overflowing text is truncated
  // TODO: CornerRadius   — rounded corners on the backing box
  // TODO: Extrusion      — true per-glyph extruded geometry (SCNText-style)
  // ---------------------------------------------------------------------------
}