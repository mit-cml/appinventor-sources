
// 4. Background camera feed shader - fragment shader (background.frag)
#version 450 core

// Input from vertex shader
layout(location = 0) in vec2 vTexCoord;

// Output color
layout(location = 0) out vec4 fragColor;

// External camera texture
layout(set = 1, binding = 0) uniform samplerExternalOES cameraTexture;

void main() {
    // Sample from camera texture
    fragColor = texture(cameraTexture, vTexCoord);
}
