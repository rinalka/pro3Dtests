PShader meshShader;
float zoomLevel = 1.0;

void setup() {
  size(800, 600, P3D);
  
  // Load BOTH the vertex (.vert) and fragment (.frag) shaders
  meshShader = loadShader("wave.frag", "wave.vert");
}

void draw() {
  background(15, 15, 25);
  
  float time = millis() / 1000.0;
  meshShader.set("u_time", time);
  
  // Mouse X controls wave speed/frequency, Mouse Y controls wave height
  float mouseXNorm = (float)mouseX / width;
  float mouseYNorm = (float)mouseY / height;
  meshShader.set("u_mouse", mouseXNorm, mouseYNorm);
  
  shader(meshShader);
  
  translate(width/2, height/2, -100);
  scale(zoomLevel);
  
  // Rotate slightly just so we can see the 3D depth of the displacement
  rotateX(0.8); 
  rotateZ(frameCount * 0.003);
  
  // Generate a dense mesh grid (100x100 rows/columns)
  int gridDetail = 100;
  float size = 500;
  float steps = size / gridDetail;
  
  stroke(0, 195, 255, 150); // Neon blue mesh lines
  noFill();
  
  // Draw the grid using triangle strips
  for (int y = 0; y < gridDetail; y++) {
    beginShape(TRIANGLE_STRIP);
    for (int x = 0; x <= gridDetail; x++) {
      float vx1 = -size/2 + (x * steps);
      float vy1 = -size/2 + (y * steps);
      float vy2 = -size/2 + ((y + 1) * steps);
      
      // We pass the vertices completely flat (Z = 0). 
      // The vertex shader will pull them upward into 3D waves.
      vertex(vx1, vy1, 0);
      vertex(vx1, vy2, 0);
    }
    endShape();
  }
}

void keyPressed() {
  if (key == '+' || key == '=') zoomLevel += 0.1;
  if (key == '-' || key == '_') zoomLevel -= 0.1;
  zoomLevel = constrain(zoomLevel, 0.2, 3.0);
}
