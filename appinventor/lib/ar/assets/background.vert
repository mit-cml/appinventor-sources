#version 300 es
precision mediump float;

// Vertex attributes
layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

// Output to fragment shader
out vec4 vColor;

// Uniforms
uniform mat4 viewProj;
uniform mat4 model;

void main() {
    // Calculate position in clip space
    gl_Position = viewProj * model * position;
    
    // Pass color to fragment shader
    vColor = color;
}