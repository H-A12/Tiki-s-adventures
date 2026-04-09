#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_flashIntensity;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    
    // Si la intensidad es 1, el objeto se vuelve blanco (mantenemos el alfa original)
    vec4 flashColor = vec4(1.0, 1.0, 1.0, texColor.a);
    
    // Mezclamos el color original con el blanco según la intensidad
    gl_FragColor = mix(texColor, flashColor, u_flashIntensity) * v_color;
}