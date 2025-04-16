#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

uniform samplerExternalOES u_CameraColorTexture;
uniform sampler2D u_texture;

in vec2 v_CameraTexCoord;
in vec2 v_FilamentTexCoord;

layout(location = 0) out vec4 o_FragColor;

void main() {
    // Ensure the texture is actually used in the shader
    vec4 texColor = texture(u_texture, v_FilamentTexCoord);
    vec4 cameraColor = texture(u_CameraColorTexture, v_CameraTexCoord);

    // Check if the alpha channel is meaningful (not 0 or 1)
    float alpha = texColor.a;

    // If alpha is very small, just use camera color
    if (alpha < 0.01) {
        o_FragColor = cameraColor;
    } else {
        // Composite colors using the proper alpha
        vec3 blendedColor = texColor.rgb * alpha + cameraColor.rgb * (1.0 - alpha);
        o_FragColor = vec4(blendedColor, 1.0);
    }

}