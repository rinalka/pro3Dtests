#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D texture;
varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform float u_time;
uniform vec2 u_mouse; // x and y mouse coordinates mapped to 0.0 - 1.0

void main() {
  vec2 uv = vertTexCoord.st;
  
  // 1. CREATING THE BLOCKS
  // By multiplying by a number and flooring it, we turn smooth gradients into hard steps.
  // u_mouse.y controls how many blocky steps there are vertically.
  float blockResolution = 10.0 + (u_mouse.y * 50.0); 
  float steppedY = floor(uv.y * blockResolution) / blockResolution;
  
  // 2. MOUSE CONTROLLED DISTORTION
  // u_mouse.x scales how violently the blocks tear to the side.
  float glitchStrength = u_mouse.x * 0.25;
  
  // Calculate horizontal shift using a jagged, fast-moving sine wave
  float horizontalShift = sin(steppedY * 40.0 + u_time * 15.0) * glitchStrength;
  
  // 3. COLOR SPLITTING (RGB CHROMATIC ABERRATION)
  // We sample the image texture three separate times with slightly different offsets.
  // This rips the Red, Green, and Blue channels apart for that vibrant glitch look.
  float r = texture2D(texture, vec2(uv.x + horizontalShift, uv.y)).r;
  float g = texture2D(texture, vec2(uv.x + horizontalShift * 0.5, uv.y)).g;
  float b = texture2D(texture, vec2(uv.x - horizontalShift * 0.2, uv.y)).b;
  
  // Combine the separated channels back into a single color string
  vec4 finalColor = vec4(r, g, b, 1.0);
  
  gl_FragColor = finalColor * vertColor;
}

