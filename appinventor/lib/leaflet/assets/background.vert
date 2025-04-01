
// 3. Background camera feed shader - vertex shader (background.vert)
#version 450 core

// Vertex attributes
layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;

// Output to fragment shader
layout(location = 0) out vec2 vTexCoord;

void main() {
    // Pass position directly - fills screen
    gl_Position = position;

    // Pass texture coordinates to fragment shader
    vTexCoord = texCoord;
}
