#version 300 es
/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
precision mediump float;

uniform sampler2D u_Texture;
uniform vec4 u_LightingParameters;
uniform vec4 u_MaterialParameters;
uniform vec4 u_ColorCorrectionParameters;
uniform vec4 u_ObjColor;

// Depth occlusion uniforms
uniform sampler2D u_DepthTexture;
uniform mat4 u_UvTransform;
uniform float u_OcclusionEnabled;

in vec3 v_ViewPosition;
in vec3 v_ViewNormal;
in vec2 v_TexCoord;
in vec3 v_ScreenSpacePosition;

layout(location = 0) out vec4 o_FragColor;

// Unpack depth from RG channels — millimeters
// Matches background_show_depth_color_visualization.frag packing
float getDepthMillimeters(sampler2D depthTexture, vec2 uv) {
    vec3 packed = texture(depthTexture, uv).xyz;
    return dot(packed.xy, vec2(255.0, 256.0 * 255.0));
}

void main() {
    // Flip y to address texture from top-left
    vec4 objectColor = texture(u_Texture,
        vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));

    vec3 color = objectColor.rgb;

    // Depth occlusion
    if (u_OcclusionEnabled > 0.5) {
        // Convert screen-space position to normalized device coords
        // v_ScreenSpacePosition is in [-1,1] range
        vec2 screenUv = v_ScreenSpacePosition.xy * 0.5 + 0.5;

        // Apply ARCore UV transform to match depth texture orientation
        vec2 depthUv = (u_UvTransform * vec4(screenUv, 0.0, 1.0)).xy;

        // Clamp to valid range
        depthUv = clamp(depthUv, 0.0, 1.0);

        // Get real-world depth at this screen position
        float depthMm = getDepthMillimeters(u_DepthTexture, depthUv);

        // Get virtual object depth — v_ViewPosition.z is negative in view space
        float objectDepthMm = -v_ViewPosition.z * 1000.0;

        // Discard if real world is closer than virtual object
        // Small bias (15mm) prevents z-fighting at surface contact
        if (depthMm > 0.0 && objectDepthMm > depthMm + 15.0) {
            discard;
        }
    }

    o_FragColor = vec4(color, objectColor.a > 0.1 ? 1.0 : 0.0);
}
