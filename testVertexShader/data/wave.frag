#ifdef GL_ES
precision mediump float;
#endif

varying float v_depth; 

void main() {
  float intensity = (v_depth + 75.0) / 150.0;
  gl_FragColor = vec4(intensity * 0.3, intensity, 1.0, 1.0);
}
