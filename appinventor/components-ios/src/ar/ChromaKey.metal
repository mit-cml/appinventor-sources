#include <metal_stdlib>
#include <RealityKit/RealityKit.h>

using namespace metal;

// Chroma-key surface shader for VideoNode.
//
// Keying algorithm intentionally matches the Android ar_video.frag so the
// same footage, key color, and sensitivity produce the same result on both
// platforms:
//   - distance measured in CbCr (chroma) space, not RGB: keys on HUE while
//     ignoring brightness, so shadows and hotspots on the green screen key
//     out together instead of leaving dark-green fringes
//   - smoothstep over a fixed 0.08 band for soft antialiased edges
//   - despill: pulls the key hue out of semi-transparent edge pixels so
//     hair/edges don't glow green against the AR background
//
// Inputs (set from VideoNode.pushChromaKeyUniforms):
//   custom_parameter().xyz = key color RGB, 0..1
//   custom_parameter().w   = chroma distance threshold:
//                              > 0  -> keying enabled (sensitivity / 250)
//                              < 0  -> keying DISABLED, video shown opaque
//                              == 0 -> uniforms never pushed; use defaults
//   custom texture         = current decoded video frame

// RGB -> CbCr (BT.601), ignoring luma — identical matrix to the GLSL side.
static inline float2 rgbToCbCr(float3 rgb) {
    return float2(
        -0.169 * rgb.r - 0.331 * rgb.g + 0.500 * rgb.b,
         0.500 * rgb.r - 0.419 * rgb.g - 0.081 * rgb.b
    );
}

[[visible]]
void chromaKeyModifier(realitykit::surface_parameters params) {
    constexpr sampler videoSampler(coord::normalized,
                                   address::clamp_to_edge,
                                   filter::linear);

    auto surface  = params.surface();
    auto geometry = params.geometry();
    auto tex      = params.textures();

    float2 uv = geometry.uv0();
    uv.y = 1.0 - uv.y;  // flip vertically
    float3 rgb = float3(tex.custom().sample(videoSampler, uv).rgb);

    float4 keyParams = params.uniforms().custom_parameter();
    float3 keyColor  = keyParams.xyz;
    float  threshold = keyParams.w;

    // Negative threshold is the "chroma key disabled" sentinel set by
    // pushChromaKeyUniforms when ChromaKeyColor is None: show the video
    // opaque and skip keying/despill entirely.
    if (threshold < 0.0) {
        surface.set_base_color(half3(rgb));
        surface.set_emissive_color(half3(rgb));
        surface.set_opacity(half(1.0));
        surface.set_roughness(1.0);
        surface.set_metallic(0.0);
        surface.set_specular(0.0);
        return;
    }

    // Fallbacks for unset uniforms (material created before the first
    // pushChromaKeyUniforms ran). Exactly zero means "never pushed", not
    // "disabled". A zero-chroma key would remove all neutral pixels — the
    // "keys black instead of green" failure — so default to canonical
    // green and the sensitivity-65 threshold.
    if (all(keyColor == float3(0.0))) {
        keyColor = float3(0.0, 1.0, 0.0);
    }
    if (threshold == 0.0) {
        threshold = 0.26;                   // sensitivity 65 / 250
    }

    // Same smoothing band as the Android shader (u_KeySmooth = 0.08).
    // Edges always differ by exactly kSmoothing, so smoothstep can never
    // degenerate (equal edges -> divide by zero -> NaN alpha).
    const float kSmoothing = 0.08;

    float chromaDist = distance(rgbToCbCr(rgb), rgbToCbCr(keyColor));
    float alpha = smoothstep(threshold, threshold + kSmoothing, chromaDist);

    // Despill — mirror of the GLSL: pull the key hue out of edge pixels.
    float spill = 1.0 - alpha;
    float keyMax = max(max(keyColor.r, keyColor.g), keyColor.b);
    if (keyMax > 0.0) {
        float3 keyDir = keyColor / keyMax;
        float amount = dot(rgb, keyDir) / dot(keyDir, keyDir);
        rgb = mix(rgb, rgb - keyDir * amount * 0.5, spill * 0.7);
    }

    surface.set_base_color(half3(rgb));
    surface.set_emissive_color(half3(rgb));
    surface.set_opacity(half(alpha));

    // Suppress PBR response so the video doesn't look glossy/lit under
    // the .lit lighting model.
    surface.set_roughness(1.0);
    surface.set_metallic(0.0);
    surface.set_specular(0.0);
}
