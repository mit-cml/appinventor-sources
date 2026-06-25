#version 300 es
precision mediump float;

uniform sampler2D u_Texture;
uniform vec4 u_LightingParameters;
uniform vec4 u_MaterialParameters;
uniform vec4 u_ColorCorrectionParameters;
uniform vec4 u_ObjColor;

// Depth-based occlusion
uniform sampler2D u_DepthTexture;
uniform mat4 u_UvTransform;
uniform float u_OcclusionEnabled;

// Plane-based occlusion
uniform vec4 u_PlaneEquation;
uniform float u_PlaneOcclusionEnabled;

// World position from vertex shader
in vec3 v_WorldPosition;
in vec3 v_ViewPosition;
in vec3 v_ViewNormal;
in vec2 v_TexCoord;
in vec3 v_ScreenSpacePosition;

layout(location = 0) out vec4 o_FragColor;

float getDepthMillimeters(sampler2D depthTexture, vec2 uv) {
    vec3 packed = texture(depthTexture, uv).xyz;
    return dot(packed.xy, vec2(255.0, 256.0 * 255.0));
}

void main() {
    vec4 objectColor = texture(u_Texture,
        vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));

    // Depth-based occlusion — per fragment, smooth
    if (u_OcclusionEnabled > 0.5) {
        vec2 screenUv = v_ScreenSpacePosition.xy * 0.5 + 0.5;
        vec2 depthUv = (u_UvTransform * vec4(screenUv, 0.0, 1.0)).xy;
        depthUv = clamp(depthUv, 0.0, 1.0);
        float depthMm = getDepthMillimeters(u_DepthTexture, depthUv);
        float objectDepthMm = -v_ViewPosition.z * 1000.0;
        if (depthMm > 0.0 && objectDepthMm > depthMm + 15.0) {
            discard;
        }
    }

    // Plane-based occlusion — fallback when depth not supported
    if (u_PlaneOcclusionEnabled > 0.5) {
        float side = dot(u_PlaneEquation.xyz, v_WorldPosition)
                   + u_PlaneEquation.w;
        if (side < 0.0) {
            discard;
        }
    }

    o_FragColor = vec4(objectColor.rgb, objectColor.a > 0.1 ? 1.0 : 0.0);
}
