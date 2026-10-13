#version 150

// Separable depth-of-field blur, run once horizontally and once vertically.
// Each sample contributes if its blur disc reaches this pixel; samples behind the
// centre are capped by the centre's own blur so sharp subjects don't smear outward.

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec2 BlurDir;
uniform float Focus;
uniform float Strength;
uniform float Near;
uniform float Far;
uniform float Ortho;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

const int TAPS = 12;
const float MAX_RADIUS = 18.0; // pixels at full blur

float linearDepth(vec2 uv) {
    float d = texture(DepthSampler, uv).r;
    if (Ortho > 0.5) {
        return Near + d * (Far - Near);
    }
    float z = d * 2.0 - 1.0;
    return 2.0 * Near * Far / (Far + Near - z * (Far - Near));
}

// Circle of confusion radius in pixels.
float coc(float depth) {
    float c;
    if (Ortho > 0.5) {
        c = abs(depth - Focus) / 24.0;
    } else {
        c = abs(depth - Focus) / max(depth, 0.001);
    }
    return clamp(c * Strength, 0.0, 1.0) * MAX_RADIUS;
}

void main() {
    float centreDepth = linearDepth(texCoord);
    float centreCoc = coc(centreDepth);

    vec3 sum = vec3(0.0);
    float total = 0.0;
    float spacing = MAX_RADIUS / float(TAPS);

    for (int i = -TAPS; i <= TAPS; i++) {
        float dist = abs(float(i)) * spacing;
        vec2 uv = texCoord + BlurDir * oneTexel * float(i) * spacing;
        float depth = linearDepth(uv);
        float sampleCoc = coc(depth);
        float reach = depth < centreDepth ? sampleCoc : min(sampleCoc, centreCoc);
        float w = clamp(reach - dist + 1.0, 0.0, 1.0);
        sum += texture(DiffuseSampler, uv).rgb * w;
        total += w;
    }

    fragColor = vec4(sum / max(total, 1e-4), 1.0);
}
