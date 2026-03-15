#version 300 es

uniform mat4 u_ModelView;
uniform mat4 u_ModelViewProjection;

in vec4 a_Position;
in vec3 a_Normal;
in vec2 a_TexCoord;

out vec3 v_ViewPosition;
out vec3 v_ViewNormal;
out vec2 v_TexCoord;
out vec3 v_ScreenSpacePosition;

void main() {
    v_ViewPosition = (u_ModelView * a_Position).xyz;
    v_ViewNormal = normalize((u_ModelView * vec4(a_Normal, 0.0)).xyz);
    v_TexCoord = a_TexCoord;
    gl_Position = u_ModelViewProjection * a_Position;
    v_ScreenSpacePosition = gl_Position.xyz / gl_Position.w;
}
