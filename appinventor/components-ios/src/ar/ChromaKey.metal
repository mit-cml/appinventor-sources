#include <metal_stdlib>
#include <RealityKit/RealityKit.h>

using namespace metal;

[[visible]]
void chromaKeyModifier(realitykit::surface_parameters params) {
    constexpr sampler videoSampler(coord::normalized,
                                    address::clamp_to_edge,
                                    filter::linear);

    auto surface = params.surface();
    auto geometry = params.geometry();
    auto tex = params.textures();

    float2 uv = geometry.uv0();
    half4 color = half4(tex.custom().sample(videoSampler, uv));

    half3 targetGreen = half3(0.0, 1.0, 0.0);
    float threshold = 0.4;
    float smoothing = 0.1;

    float dist = distance(float3(color.rgb), float3(targetGreen));
    float alpha = smoothstep(threshold, threshold + smoothing, dist);

    surface.set_base_color(color.rgb);
    surface.set_emissive_color(color.rgb);
    surface.set_opacity(alpha);

    // Suppress PBR lighting response so the video doesn't look glossy/lit.
    surface.set_roughness(1.0);
    surface.set_metallic(0.0);
    surface.set_specular(0.0);
}
