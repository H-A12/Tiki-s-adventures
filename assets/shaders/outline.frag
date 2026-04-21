#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform vec2 u_texelSize;     // Tamaño de 1 pixel (1.0 / ancho, 1.0 / alto)
uniform vec4 u_outlineColor;  // El color del contorno según el Tier
uniform float u_outlineWidth; // Grosor del contorno

void main() {
    vec4 color = texture2D(u_texture, v_texCoord);

    // Si el píxel ya tiene color (es parte del arma), lo dibujamos normal
    if (color.a > 0.5) {
        gl_FragColor = color * v_color;
        return;
    }

    // Si el píxel es transparente, comprobamos a sus vecinos (Arriba, Abajo, Izq, Der y Diagonales)
    float a = 0.0;
    a += texture2D(u_texture, v_texCoord + vec2(u_texelSize.x * u_outlineWidth, 0.0)).a;
    a += texture2D(u_texture, v_texCoord - vec2(u_texelSize.x * u_outlineWidth, 0.0)).a;
    a += texture2D(u_texture, v_texCoord + vec2(0.0, u_texelSize.y * u_outlineWidth)).a;
    a += texture2D(u_texture, v_texCoord - vec2(0.0, u_texelSize.y * u_outlineWidth)).a;

    // Diagonales para un contorno más suave
    a += texture2D(u_texture, v_texCoord + vec2(u_texelSize.x * u_outlineWidth, u_texelSize.y * u_outlineWidth)).a;
    a += texture2D(u_texture, v_texCoord - vec2(u_texelSize.x * u_outlineWidth, u_texelSize.y * u_outlineWidth)).a;
    a += texture2D(u_texture, v_texCoord + vec2(u_texelSize.x * u_outlineWidth, -u_texelSize.y * u_outlineWidth)).a;
    a += texture2D(u_texture, v_texCoord - vec2(u_texelSize.x * u_outlineWidth, -u_texelSize.y * u_outlineWidth)).a;

    // Si este pixel es transparente pero tiene vecinos con color, lo pintamos del color del Tier
    if (color.a <= 0.5 && a > 0.0) {
        gl_FragColor = u_outlineColor;
    } else {
        gl_FragColor = color * v_color;
    }
}
