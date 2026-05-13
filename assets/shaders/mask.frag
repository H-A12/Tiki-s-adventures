varying vec2 v_texCoord;
uniform sampler2D u_texture;

void main() {
    float alpha = texture2D(u_texture, v_texCoord).a;
    if (alpha < 0.1) {
        discard;
    }
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
