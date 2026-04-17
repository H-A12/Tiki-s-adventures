#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_textureSize; // Dimensiones totales del atlas
uniform vec4 u_outlineColor;
uniform float u_outlineWidth;
uniform vec4 u_texBounds; // x=minU, y=minV, z=maxU, w=maxV

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    
    if (color.a > 0.1) {
        gl_FragColor = v_color * color;
    } else {
        // Desplazamiento de 1 píxel normalizado según el tamaño del atlas
        float texelW = 1.0 / u_textureSize.x;
        float texelH = 1.0 / u_textureSize.y;
        
        float width = u_outlineWidth * texelW;
        float height = u_outlineWidth * texelH;
        
        float a = 0.0;
        
        // Muestreo dentro de los límites de la región (u_texBounds)
        vec2 coord1 = clamp(v_texCoords + vec2(width, 0.0), u_texBounds.xy, u_texBounds.zw);
        vec2 coord2 = clamp(v_texCoords + vec2(-width, 0.0), u_texBounds.xy, u_texBounds.zw);
        vec2 coord3 = clamp(v_texCoords + vec2(0.0, height), u_texBounds.xy, u_texBounds.zw);
        vec2 coord4 = clamp(v_texCoords + vec2(0.0, -height), u_texBounds.xy, u_texBounds.zw);
        
        a += texture2D(u_texture, coord1).a;
        a += texture2D(u_texture, coord2).a;
        a += texture2D(u_texture, coord3).a;
        a += texture2D(u_texture, coord4).a;
        
        if (a > 0.1) {
            gl_FragColor = v_color * u_outlineColor;
        } else {
            gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        }
    }
}
