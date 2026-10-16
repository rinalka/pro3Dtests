PShader meshShader;
PImage img;
float zoomLevel = 1.0;

float noiseScale = 2.0;    
float noiseHeight = 150.0; 
float noiseOffset = 0.0; // MANUALLY CONTROLS THE NOISE SCROLLING

void setup() {
  size(1600, 1200, P3D);
  
  // Force the window to open at the top-left corner of your monitor
  windowMove(100, 100);
  
  // Create a high-contrast checkered gradient texture
  img = createImage(512, 512, RGB);
  img.loadPixels();
  for (int y = 0; y < 512; y++) {
    for (int x = 0; x < 512; x++) {
      int idx = x + y * 512;
      boolean check = ((x / 32) % 2 == 0) ^ ((y / 32) % 2 == 0);
      if (check) {
        img.pixels[idx] = color(x / 2, y / 2, 255);
      } else {
        img.pixels[idx] = color(255, x / 2, y / 2);
      }
    }
  }
  img.updatePixels();
  
  meshShader = loadShader("wave.frag", "wave.vert");
}

void draw() {
  background(15, 15, 25);
  
  // Pass your manual controls cleanly to the shader
  meshShader.set("u_noiseScale", (float)noiseScale);
  meshShader.set("u_noiseHeight", (float)noiseHeight);
  meshShader.set("u_noiseOffset", (float)noiseOffset); // Replaces u_time for control
  
  shader(meshShader);
  
  translate(width/2, height/2, -100);
  scale(zoomLevel);
  
  rotateX(0.8); 
  rotateZ(0.4);
  
  int gridDetail = 60; 
  float size = 1200; // Scaled up to match the larger canvas size nicely
  float steps = size / gridDetail;
  
  noStroke(); 
  fill(255);  
  textureMode(NORMAL); 
  
  for (int y = 0; y < gridDetail; y++) {
    beginShape(TRIANGLE_STRIP);
    texture(img); 
    
    for (int x = 0; x <= gridDetail; x++) {
      float vx1 = -size/2 + (x * steps);
      float vy1 = -size/2 + (y * steps);
      float vy2 = -size/2 + ((y + 1) * steps);
      
      float u = (float)x / gridDetail;
      float v1 = (float)y / gridDetail;
      float v2 = (float)(y + 1) / gridDetail;
      
      vertex(vx1, vy1, 0, u, v1);
      vertex(vx1, vy2, 0, u, v2);
    }
    endShape();
  }
}

void keyPressed() {
  if (key == '+' || key == '=') zoomLevel += 0.1;
  if (key == '-' || key == '_') zoomLevel -= 0.1;
  zoomLevel = constrain(zoomLevel, 0.2, 3.0);
  
  // MANUAL NOISE SHIFT CONTROLS
  if (key == 'w' || key == 'W') {
    noiseOffset += 0.05; // Shift noise terrain forward
  }
  if (key == 's' || key == 'S') {
    noiseOffset -= 0.05; // Shift noise terrain backward
  }
  
  if (key == CODED) {
    if (keyCode == LEFT)  noiseScale -= 0.2;
    if (keyCode == RIGHT) noiseScale += 0.2;
    if (keyCode == UP)    noiseHeight += 15.0;
    if (keyCode == DOWN)  noiseHeight -= 15.0;
    
    noiseScale = constrain(noiseScale, 0.2, 8.0);
    noiseHeight = constrain(noiseHeight, 0.0, 800.0);
    println("Scale (Left/Right): " + noiseScale + " | Height (Up/Down): " + noiseHeight);
  }
}
