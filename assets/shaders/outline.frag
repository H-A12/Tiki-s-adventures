#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_sceneTexture; // Unidad 0
uniform sampler2D u_maskTexture;  // Unidad 1
uniform vec2 u_texelSize;         // 1.0 / resolucion
uniform vec4 u_outlineColor;      // Color del Tier
uniform float u_outlineThreshold; // Grosor

void main() {
    // 1. Color de la escena original y de la máscara blanca
    vec4 sceneColor = texture2D(u_sceneTexture, v_texCoord);
    vec4 maskColor = texture2D(u_maskTexture, v_texCoord);

    // 2. Si el píxel en la máscara es blanco (> 0.5), es el arma real.
    // Dibujamos la escena tal cual (el arma ya está ahí).
    if (maskColor.r > 0.5) {
        gl_FragColor = sceneColor;
        return;
    }

    // 3. Si es negro, buscamos vecinos blancos para crear el contorno
    float a = 0.0;

    // Muestreo en cruz (Arriba, Abajo, Izquierda, Derecha)
    a += texture2D(u_maskTexture, v_texCoord + vec2(u_texelSize.x * u_outlineThreshold, 0.0)).r;
    a += texture2D(u_maskTexture, v_texCoord - vec2(u_texelSize.x * u_outlineThreshold, 0.0)).r;
    a += texture2D(u_maskTexture, v_texCoord + vec2(0.0, u_texelSize.y * u_outlineThreshold)).r;
    a += texture2D(u_maskTexture, v_texCoord - vec2(0.0, u_texelSize.y * u_outlineThreshold)).r;

    // 4. Si encontramos blanco cerca, pintamos el color del Tier
    if (a > 0.1) {
        gl_FragColor = u_outlineColor;
    } else {
        // Si no hay nada, fondo de la escena normal
        gl_FragColor = sceneColor;
    }
}
