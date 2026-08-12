#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
// Fragment shader for VideoNode — samples MediaPlayer / SurfaceTexture frames
// via samplerExternalOES.
//
// u_Opacity:      uniform overall transparency, 0.0..1.0.
// u_KeyEnabled:   1.0 to enable chroma keying, 0.0 to disable.
// u_KeyColor:     RGB (0..1) of the key color (e.g. green screen).
// u_KeyThreshold: chroma distance below which pixels are fully transparent.
// u_KeySmooth:    extra distance over which alpha ramps 0 -> 1 (soft edge).
//
// Keying is done in CbCr (chroma) space rather than RGB: this keys on HUE
// while ignoring brightness, so shadows and highlights on the green screen
// key out together instead of leaving dark-green fringes.
precision mediump float;

uniform samplerExternalOES u_Texture;
uniform float u_Opacity;
uniform float u_KeyEnabled;
uniform vec3  u_KeyColor;
uniform float u_KeyThreshold;
uniform float u_KeySmooth;

in vec2 v_TexCoord;

layout(location = 0) out vec4 o_FragColor;

// RGB -> CbCr (BT.601), ignoring luma
vec2 rgbToCbCr(vec3 rgb) {
  return vec2(
    -0.169 * rgb.r - 0.331 * rgb.g + 0.500 * rgb.b,
     0.500 * rgb.r - 0.419 * rgb.g - 0.081 * rgb.b
  );
}

void main() {
  vec2 texCoord = vec2(v_TexCoord.x, 1.0 - v_TexCoord.y);
  vec3 rgb = texture(u_Texture, texCoord).rgb;

  float alpha = u_Opacity;

  if (u_KeyEnabled > 0.5) {
    float chromaDist = distance(rgbToCbCr(rgb), rgbToCbCr(u_KeyColor));
    // 0 inside threshold, ramp to 1 across the smooth band
    float keyAlpha = smoothstep(u_KeyThreshold, u_KeyThreshold + u_KeySmooth, chromaDist);
    alpha *= keyAlpha;

    // Despill: pull the key hue out of semi-transparent edge pixels so
    // hair/edges don't glow green against the AR background.
    float spill = 1.0 - keyAlpha;
    float keyMax = max(max(u_KeyColor.r, u_KeyColor.g), u_KeyColor.b);
    if (keyMax > 0.0) {
      vec3 keyDir = u_KeyColor / keyMax;
      float amount = dot(rgb, keyDir) / dot(keyDir, keyDir);
      rgb = mix(rgb, rgb - keyDir * amount * 0.5, spill * 0.7);
    }
  }

  o_FragColor = vec4(rgb, alpha);
}
