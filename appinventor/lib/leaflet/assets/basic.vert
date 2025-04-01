#version 450 core

// Vertex attributes
layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

// Output to fragment shader
layout(location = 0) out vec4 vColor;

// Uniforms
layout(set = 0, binding = 0) uniform Camera {
    mat4 viewProj;
};

layout(set = 1, binding = 0) uniform Transform {
    mat4 model;
};

void main() {
    // Calculate position in clip space
    gl_Position = viewProj * model * position;

    // Pass color to fragment shader
    vColor = color;
}

