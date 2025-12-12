#version 300 es
precision mediump float;

// Input from vertex shader
in vec4 vColor;

// Output color
out vec4 fragColor;

void main() {
    // Output the vertex color
    fragColor = vColor;
}