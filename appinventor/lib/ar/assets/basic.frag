#version 300 es
precision mediump float;

in vec2 v_texCoord;
out vec4 o_fragColor;

uniform sampler2D u_texture;
uniform int u_isDebugMode;
uniform vec4 u_debugColor;

void main() {
    if (u_isDebugMode == 1) {
        o_fragColor = u_debugColor;
    } else {
        o_fragColor = texture(u_texture, v_texCoord);
    }
}