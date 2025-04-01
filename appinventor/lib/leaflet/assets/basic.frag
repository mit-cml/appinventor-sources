#version 450 core

// Input from vertex shader
layout(location = 0) in vec4 vColor;

// Output color
layout(location = 0) out vec4 fragColor;

// Material parameters
layout(set = 2, binding = 0) uniform Material {
    vec4 baseColor;
};

void main() {
    // Combine vertex color with material base color
    fragColor = vColor * baseColor;
}