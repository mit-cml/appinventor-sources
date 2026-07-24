#version 300 es
// Fragment shader for TextNode — one-line diff from ar_unlit_object.frag:
// outputs the texture's real alpha instead of hardcoding 1.0, so
// antialiased glyph edges blend smoothly and transparent backgrounds
// stay transparent. Pair with ar_unlit_object.vert.
precision mediump float;

uniform sampler2D u_Texture;

in vec2 v_TexCoord;

layout(location = 0) out vec4 o_FragColor;

void main() {
  // Mirror texture coordinates over the X axis (same convention as
  // ar_object.frag / ar_unlit_object.frag — compensates bitmap row order)
  vec2 texCoord = vec2(v_TexCoord.x, 1.0 - v_TexCoord.y);

  o_FragColor = texture(u_Texture, texCoord);
}
