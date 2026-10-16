// curtains
// Turns a photo into a triangle mesh (like PostContemp's MeshMap) and textures each triangle with
// the part of the photo under it - the same way as meshTube - but keeps it flat. Five copies are
// spread front to back so together they roughly fill a cube. The back one is the whole picture;
// each of the four in front has a random 25% of its triangles missing, like curtains with holes.
//
// Buttons on screen, or keys:
//   New gaps (r)   pick new missing triangles
//   More / Less    more / less missing (+ / -)
//   Wire (w)       wireframe on / off
//   one button per picture in the data folder - drop more .jpg / .png files in there to add them
// Mouse drag orbits the camera, mouse wheel zooms, s saves a frame (curtains-####.png).
//
// EdgeDetector.java and Triangulate.java are the same as meshTube's - PostContemp's edge point
// finder and Delaunay triangulator, with bugs fixed.

// ---- settings ----
float ui = 2;             // size of the buttons and text (1 = small)
int detectWidth = 1000;   // points are found on a copy this wide - keeps the triangle count sensible
int resolution = 12;      // grid step for edge points, in detection pixels - smaller = more triangles
int threshold = 100;      // how strong an edge has to be to get a point - smaller = more triangles
int kernel = EdgeDetector.SOBEL;  // SOBEL, SCHARR or RICK
int edgeSteps = 60;       // points along each edge of the picture
int layers = 5;           // the back one plus the curtains in front of it
float missing = 0.25;     // fraction of each curtain's triangles taken out

// ---- pictures ----
String[] imageFiles;      // every .jpg / .png in the data folder
String imageFile;         // the one showing

// ---- the mesh ----
PImage img;               // full size photo, used as the texture
float W, H;               // size of the detection copy
ArrayList<PVector> points = new ArrayList<PVector>();    // 2D points (detection pixels)
ArrayList<Triangle> triangles = new ArrayList<Triangle>();
boolean[][] shown;        // for each layer, which triangles are there (the back layer has them all)
PShape curtains;
boolean wireframe = false;

// ---- camera ----
float rotX = -0.2, rotY = -0.5;
float zoom = 1.3;
boolean orbiting = false; // true while a drag that started off the buttons is rotating the view

// ---- buttons ----
ArrayList<Button> buttons = new ArrayList<Button>();


void setup() {
  size(3200, 2000, P3D);
  // Processing can't see the screen size here (it says displayWidth/displayHeight will be 0),
  // so it centres the window wrongly and puts it off the top left. Put it near the top left instead,
  // low enough that the title bar is on screen
  surface.setLocation(40, 80);

  imageFiles = findImages();
  if (imageFiles.length == 0) {
    println("no .jpg or .png files in " + dataPath(""));
    exit();
    return;
  }
  loadPicture(imageFiles[0]);
  makeButtons();
}

void draw() {
  background(20);

  pushMatrix();
  translate(width/2, height/2, 0);
  rotateX(rotX);
  rotateY(rotY);
  scale(zoom);
  shape(curtains);
  popMatrix();

  // flat things drawn over the 3D
  hint(DISABLE_DEPTH_TEST);
  for (Button b : buttons) b.draw();
  fill(255, 160);
  textSize(14 * ui);
  textAlign(LEFT, BASELINE);
  text(imageFile + "   " + triangles.size() + " triangles per layer   " + round(missing * 100) + "% missing"
    + "   drag to orbit, wheel to zoom, s to save", 16 * ui, height - 16 * ui);
  hint(ENABLE_DEPTH_TEST);
}


// ---------------------------------------------------------------- pictures

// the pictures in the data folder, sorted by name
String[] findImages() {
  ArrayList<String> found = new ArrayList<String>();
  java.io.File[] files = new java.io.File(dataPath("")).listFiles();
  if (files != null) {
    for (java.io.File f : files) {
      String n = f.getName().toLowerCase();
      if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")) found.add(f.getName());
    }
  }
  java.util.Collections.sort(found);
  return found.toArray(new String[0]);
}

// load a picture and build everything from it - the mesh, new gaps, and the shape
void loadPicture(String file) {
  imageFile = file;
  img = loadImage(file);
  PImage small = img.copy();
  small.resize(detectWidth, 0);
  W = small.width;
  H = small.height;

  points.clear();
  triangles.clear();
  buildMesh(small);
  pickGaps();
  buildShape();
}


// ---------------------------------------------------------------- building the mesh

// find edge points, add points round the border so the mesh covers the whole picture, triangulate
void buildMesh(PImage small) {
  int t0 = millis();
  EdgeDetector.extractPoints(points, small, kernel, threshold, resolution);

  for (int i = 0; i <= edgeSteps; i++) {
    float y = H * i / edgeSteps;
    points.add(new PVector(0, y));
    points.add(new PVector(W, y));
  }
  int across = round(edgeSteps * W / H);
  for (int i = 1; i < across; i++) {
    float x = W * i / across;
    points.add(new PVector(x, 0));
    points.add(new PVector(x, H));
  }

  Triangulator.triangulate(points, triangles);
  println(imageFile + ": " + points.size() + " points, " + triangles.size() + " triangles in " + (millis() - t0) + " ms");
}


// for every layer but the back one, take out a random `missing` share of the triangles.
// Each layer gets its own random set, so the holes don't line up
void pickGaps() {
  int n = triangles.size();
  shown = new boolean[layers][n];
  java.util.Arrays.fill(shown[0], true);   // the back layer is complete

  for (int layer = 1; layer < layers; layer++) {
    // shuffle the triangle numbers and drop the first `missing` share of them
    int[] order = new int[n];
    for (int i = 0; i < n; i++) order[i] = i;
    for (int i = n - 1; i > 0; i--) {
      int j = int(random(i + 1));
      int t = order[i]; order[i] = order[j]; order[j] = t;
    }
    int drop = round(n * missing);
    java.util.Arrays.fill(shown[layer], true);
    for (int i = 0; i < drop; i++) shown[layer][order[i]] = false;
  }
}


// all the layers in one shape. The back layer is at the back of a cube as deep as the picture is
// wide, the front one at the front, and the others evenly between
void buildShape() {
  float depth = W;
  curtains = createShape();
  curtains.beginShape(TRIANGLES);
  curtains.textureMode(NORMAL);
  if (wireframe) {
    curtains.noFill();
    curtains.stroke(255, 120);
    curtains.strokeWeight(1);
  } else {
    curtains.noStroke();
    curtains.texture(img);
  }
  for (int layer = 0; layer < layers; layer++) {
    float z = -depth/2 + depth * layer / (layers - 1);
    for (int t = 0; t < triangles.size(); t++) {
      if (!shown[layer][t]) continue;
      Triangle tri = triangles.get(t);
      corner(tri.p1, z);
      corner(tri.p2, z);
      corner(tri.p3, z);
    }
  }
  curtains.endShape();
}

// one corner of a triangle: the picture is centred on the middle of the cube, and the
// texture coordinates come from the flat point so each triangle shows its part of the photo
void corner(PVector p, float z) {
  curtains.vertex(p.x - W/2, p.y - H/2, z, p.x / W, p.y / H);
}


// ---------------------------------------------------------------- actions (buttons and keys)

void newGaps()      { pickGaps(); buildShape(); }
void moreMissing()  { missing = min(missing + 0.05, 0.95); pickGaps(); buildShape(); }
void lessMissing()  { missing = max(missing - 0.05, 0);    pickGaps(); buildShape(); }
void toggleWire()   { wireframe = !wireframe; buildShape(); }


// ---------------------------------------------------------------- buttons

class Button {
  String label;
  float x, y, w, h;
  Runnable action;
  boolean isWire = false; // lit while wireframe is on
  String picture = null;  // for picture buttons - lit when that picture is showing

  Button(String label, float x, float y, float w, float h, Runnable action) {
    this.label = label;
    this.x = x; this.y = y; this.w = w; this.h = h;
    this.action = action;
  }

  boolean over(float mx, float my) {
    return mx >= x && mx <= x + w && my >= y && my <= y + h;
  }

  boolean lit() {
    return (isWire && wireframe) || (picture != null && picture.equals(imageFile));
  }

  void draw() {
    stroke(255, 90);
    if (lit()) fill(70, 130, 200, 230);
    else if (over(mouseX, mouseY)) fill(80, 80, 80, 230);
    else fill(40, 40, 40, 200);
    rect(x, y, w, h, 6 * ui);
    fill(255);
    textSize(15 * ui);
    textAlign(CENTER, CENTER);
    text(label, x + w/2, y + h/2 - 1);
  }
}

void makeButtons() {
  float x = 16 * ui, y = 16 * ui, w = 130 * ui, h = 32 * ui, gap = 8 * ui;

  buttons.add(new Button("New gaps (r)", x, y, w, h, () -> newGaps()));     y += h + gap;
  buttons.add(new Button("More (+)",     x, y, w, h, () -> moreMissing())); y += h + gap;
  buttons.add(new Button("Less (-)",     x, y, w, h, () -> lessMissing())); y += h + gap;
  Button wire = new Button("Wire (w)", x, y, w, h, () -> toggleWire());
  wire.isWire = true;
  buttons.add(wire);                                                         y += h + gap * 4;

  // one button per picture in the data folder
  for (String f : imageFiles) {
    final String file = f;
    String name = f.substring(0, f.lastIndexOf('.'));
    Button b = new Button(name, x, y, w, h, () -> loadPicture(file));
    b.picture = f;
    buttons.add(b);
    y += h + gap;
  }
}


// ---------------------------------------------------------------- mouse and keys

void mousePressed() {
  orbiting = true;
  for (Button b : buttons) {
    if (b.over(mouseX, mouseY)) {
      b.action.run();
      orbiting = false;   // a click on a button doesn't turn the view
      return;
    }
  }
}

void mouseDragged() {
  if (!orbiting) return;
  rotY += (mouseX - pmouseX) * 0.01;
  rotX -= (mouseY - pmouseY) * 0.01;
}

void mouseReleased() {
  orbiting = false;
}

void mouseWheel(MouseEvent e) {
  zoom *= pow(1.1, -e.getCount());
  zoom = constrain(zoom, 0.1, 20);
}

void keyPressed() {
  if (key == 'r') newGaps();
  if (key == '+' || key == '=') moreMissing();
  if (key == '-') lessMissing();
  if (key == 'w') toggleWire();
  if (key == 's') saveFrame("curtains-####.png");
}
