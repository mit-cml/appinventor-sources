#version 300 es
// Vertex shader for VideoNode — verbatim copy of ar_unlit_object.vert so
// attribute locations are guaranteed to match what Mesh binds.

uniform mat4 u_ModelView;
uniform mat4 u_ModelViewProjection;

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_TexCoord;
layout(location = 2) in vec3 a_Normal;

out vec2 v_TexCoord;

void main() {
  v_TexCoord = a_TexCoord;
  gl_Position = u_ModelViewProjection * a_Position;
}
