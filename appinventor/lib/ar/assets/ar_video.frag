#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
// Fragment shader for VideoNode — diff from ar_unlit_object.frag:
// sampler2D -> samplerExternalOES (required to read MediaPlayer /
// SurfaceTexture frames; see background_show_camera.frag for the same
// mechanism used by the camera feed). Pair with ar_video.vert.
precision mediump float;

uniform samplerExternalOES u_Texture;

in vec2 v_TexCoord;

layout(location = 0) out vec4 o_FragColor;

void main() {
  // Same V-flip convention as the other object shaders. SurfaceTexture
  // frames are top-left origin like bitmaps, so the single flip here is
  // correct with quad.obj's plain UVs.
  vec2 texCoord = vec2(v_TexCoord.x, 1.0 - v_TexCoord.y);

  o_FragColor = vec4(texture(u_Texture, texCoord).rgb, 1.0);
}
