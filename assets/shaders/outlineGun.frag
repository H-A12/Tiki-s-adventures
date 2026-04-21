varying vec2 v_texCoord;
uniform sampler2D u_sceneTexture; // La escena del juego
uniform sampler2D u_maskTexture;  // La máscara blanca/negra
uniform vec2 u_texelSize;        // Tamaño de un téxel
uniform vec4 u_outlineColor;     // Color del Tier
uniform float u_outlineThreshold; // Grosor del contorno

void main() {
    vec4 sceneColor = texture2D(u_sceneTexture, v_texCoord);
    vec4 maskColor = texture2D(u_maskTexture, v_texCoord);

    if (maskColor.r > 0.5) {
        gl_FragColor = sceneColor;
        return;
    }

    float neighborSum = 0.0;


    neighborSum += texture2D(u_maskTexture, v_texCoord + vec2(0.0, u_texelSize.y) * u_outlineThreshold).r;

    neighborSum += texture2D(u_maskTexture, v_texCoord - vec2(0.0, u_texelSize.y) * u_outlineThreshold).r;

    neighborSum += texture2D(u_maskTexture, v_texCoord + vec2(u_texelSize.x, 0.0) * u_outlineThreshold).r;

    neighborSum += texture2D(u_maskTexture, v_texCoord - vec2(u_texelSize.x, 0.0) * u_outlineThreshold).r;

    if (neighborSum > 0.1) {
        gl_FragColor = u_outlineColor; // O combinar con sceneColor: vec4(mix(u_outlineColor.rgb, sceneColor.rgb, u_outlineColor.a), 1.0);
    } else {
        // No hay contorno, pintamos el color original de la escena
        gl_FragColor = sceneColor;
    }
}
