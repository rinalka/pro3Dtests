// meshTube
// Turns a photo into a triangle mesh (like PostContemp's MeshMap), textures each triangle
// with the part of the photo under it, wraps the mesh around a tube, sphere, box or pyramid
// (joined down the back), and pushes every vertex a random amount.
//
// The photo's width goes round the shape and its height goes along it. The box is closed like a
// folded cardboard box: the middle band of the photo goes round its four sides, and the squares
// of photo above and below the front face fold over to make the lid and the bottom.
//
// Buttons on screen, or keys:
//   New (r)        new random distortion
//   More / Less    more / less distortion (+ / -)
//   None (0)       no distortion
//   Wire (w)       wireframe on / off
//   Tube / Sphere / Box / Pyramid   (1 2 3 4)
//   one button per picture in the data folder - drop more .jpg / .png files in there to add them
// Mouse drag orbits the camera, mouse wheel zooms, s saves a frame (meshTube-####.png).
//
// EdgeDetector.java and Triangulate.java are PostContemp's edge point finder and Delaunay
// triangulator, with bugs fixed (see the notes at the top of each).

// ---- settings ----
float ui = 2;             // size of the buttons and text (1 = small)
int detectWidth = 1000;   // points are found on a copy this wide - keeps the triangle count sensible
int resolution = 12;      // grid step for edge points, in detection pixels - smaller = more triangles
int threshold = 100;      // how strong an edge has to be to get a point - smaller = more triangles
int kernel = EdgeDetector.SOBEL;  // SOBEL, SCHARR or RICK
int edgeSteps = 60;       // points down each side edge and each fold line
float jitter = 8;         // biggest random move of a vertex, in detection pixels

// ---- shapes ----
final int TUBE = 0, SPHERE = 1, BOX = 2, PYRAMID = 3;
String[] shapeNames = { "Tube", "Sphere", "Box", "Pyramid" };
int shapeType = TUBE;

// ---- pictures ----
String[] imageFiles;      // every .jpg / .png in the data folder
String imageFile;         // the one showing

// ---- the mesh ----
PImage img;               // full size photo, used as the texture
float W, H;               // size of the detection copy - how far round and how far along the shape
ArrayList<PVector> points = new ArrayList<PVector>();    // 2D points (detection pixels)
ArrayList<Triangle> triangles = new ArrayList<Triangle>();
int[][] corners;          // for each triangle, the index of its three points
int[] boxPart;            // for each triangle, which part of the closed box it is (SIDES, LID, BASE, UNUSED)
final int SIDES = 0, LID = 1, BASE = 2, UNUSED = 3;
float capFold;            // how far the lid and bottom folds are from the top and bottom of the photo
int[] joined;             // for each point, the point whose 3D position it uses (right edge -> left edge)
PVector[] base;           // 3D position of each point on the current shape
PVector[] offset;         // random move of each point (a direction up to length 1, scaled by jitter)
PShape mesh3d;
boolean wireframe = false;

// ---- camera ----
float rotX = -0.3, rotY = 0;
float zoom = 1.6;
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

// load a picture and build everything from it - the mesh, the shape and a new distortion
void loadPicture(String file) {
  imageFile = file;
  img = loadImage(file);
  PImage small = img.copy();
  small.resize(detectWidth, 0);
  W = small.width;
  H = small.height;
  // the box's lid and bottom are squares one face wide - unless the photo is too short for that
  capFold = min(W / 4, H / 3);

  points.clear();
  triangles.clear();
  buildMesh(small);
  findJoins();
  wrap();
  randomise();
  buildShape();
}

void draw() {
  background(20);

  pushMatrix();
  translate(width/2, height/2, 0);
  rotateX(rotX);
  rotateY(rotY);
  scale(zoom);
  shape(mesh3d);
  popMatrix();

  // flat things drawn over the 3D
  hint(DISABLE_DEPTH_TEST);
  for (Button b : buttons) b.draw();
  fill(255, 160);
  textSize(14 * ui);
  textAlign(LEFT, BASELINE);
  text(imageFile + "   " + triangles.size() + " triangles   distortion " + nf(jitter, 0, 1)
    + "   drag to orbit, wheel to zoom, s to save", 16 * ui, height - 16 * ui);
  hint(ENABLE_DEPTH_TEST);
}


// ---------------------------------------------------------------- building the mesh

// find edge points, add the border and fold lines, triangulate, and index every triangle's corners
void buildMesh(PImage small) {
  int t0 = millis();
  EdgeDetector.extractPoints(points, small, kernel, threshold, resolution);

  // the top and bottom edges
  int across = round(edgeSteps * W / H);
  for (int i = 1; i < across; i++) {
    float x = W * i / across;
    points.add(new PVector(x, 0));
    points.add(new PVector(x, H));
  }

  // drop any point within 2 pixels of a fold line, so the lines added next don't get tiny slivers
  float quarter = W / 4;
  for (int i = points.size() - 1; i >= 0; i--) {
    PVector p = points.get(i);
    if (abs(p.x - round(p.x / quarter) * quarter) < 2) points.remove(i);
    else if (abs(p.y - capFold) < 2 || abs(p.y - (H - capFold)) < 2) points.remove(i);
  }

  // horizontal fold lines where the box's lid and bottom fold over (see onBox)
  for (int i = 1; i < across; i++) {
    float x = W * i / across;
    if (abs(x - round(x / quarter) * quarter) < 2) continue;   // the vertical fold lines cover these
    points.add(new PVector(x, capFold));
    points.add(new PVector(x, H - capFold));
  }

  // vertical lines of points at the left edge, the right edge and the three quarter marks.
  // The edges become the seam (so they get the same y values, and every right edge point has a
  // left edge partner); the quarter marks are where the box and pyramid fold, so triangles
  // bend along the corners instead of cutting across them.
  for (int k = 0; k <= 4; k++) {
    float x = (k == 4) ? W : quarter * k;
    for (int i = 0; i <= edgeSteps; i++) {
      float y = H * i / edgeSteps;
      if (abs(y - capFold) < 2 || abs(y - (H - capFold)) < 2) continue;  // added exactly below
      points.add(new PVector(x, y));
    }
    // exactly where the lid and bottom fold lines cross, so the box's corners meet
    points.add(new PVector(x, capFold));
    points.add(new PVector(x, H - capFold));
  }

  Triangulator.triangulate(points, triangles);

  // the triangulator sorts points and its triangles hold the same PVector objects,
  // so look up each corner's index by identity
  java.util.IdentityHashMap<PVector, Integer> index = new java.util.IdentityHashMap<PVector, Integer>();
  for (int i = 0; i < points.size(); i++) index.put(points.get(i), i);
  corners = new int[triangles.size()][3];
  boxPart = new int[triangles.size()];
  for (int i = 0; i < triangles.size(); i++) {
    Triangle t = triangles.get(i);
    corners[i][0] = index.get(t.p1);
    corners[i][1] = index.get(t.p2);
    corners[i][2] = index.get(t.p3);

    // which part of the closed box this triangle belongs to, by where its middle is.
    // The lid is the square of photo above the front face, the base the square below it;
    // the rest of the top and bottom strips aren't used by the box
    float cx = (t.p1.x + t.p2.x + t.p3.x) / 3;
    float cy = (t.p1.y + t.p2.y + t.p3.y) / 3;
    if (cy < capFold)          boxPart[i] = (cx < W / 4) ? LID : UNUSED;
    else if (cy > H - capFold) boxPart[i] = (cx < W / 4) ? BASE : UNUSED;
    else                       boxPart[i] = SIDES;
  }
  println(points.size() + " points, " + triangles.size() + " triangles in " + (millis() - t0) + " ms");
}


// the right edge (x = W) is the same place as the left edge (x = 0) on every shape,
// so those points share one 3D position - that's the join down the back
void findJoins() {
  java.util.HashMap<Float, Integer> leftEdge = new java.util.HashMap<Float, Integer>();
  for (int i = 0; i < points.size(); i++) {
    if (points.get(i).x == 0) leftEdge.put(points.get(i).y, i);
  }
  joined = new int[points.size()];
  for (int i = 0; i < points.size(); i++) {
    PVector p = points.get(i);
    joined[i] = i;
    if (p.x == W && leftEdge.containsKey(p.y)) joined[i] = leftEdge.get(p.y);
  }
}


// ---------------------------------------------------------------- wrapping onto shapes

// put every point on the current shape. u (0..1) is how far round, v (0..1) is how far along
void wrap() {
  base = new PVector[points.size()];
  for (int i = 0; i < points.size(); i++) {
    PVector p = points.get(i);
    float u = p.x / W;
    float v = p.y / H;
    switch (shapeType) {
    case TUBE:    base[i] = onTube(u, v);    break;
    case SPHERE:  base[i] = onSphere(u, v);  break;
    case BOX:     base[i] = onBox(p);        break;
    case PYRAMID: base[i] = onPyramid(u, v); break;
    }
  }
}

// circumference = picture width, so the photo isn't stretched round the tube
PVector onTube(float u, float v) {
  float r = W / TWO_PI;
  float a = u * TWO_PI;
  return new PVector(r * sin(a), v * H - H/2, r * cos(a));
}

// u round the equator, v from the north pole (top of the photo) to the south pole
PVector onSphere(float u, float v) {
  float r = W / TWO_PI;
  float a = u * TWO_PI;
  float lat = v * PI;
  return new PVector(r * sin(lat) * sin(a), -r * cos(lat), r * sin(lat) * cos(a));
}

// a point on the outline of a square of side `side`, going round it as u goes 0..1
// (one quarter of the photo per side, starting at the front left corner)
PVector roundSquare(float u, float side) {
  float h = side / 2;
  float t = (u * 4) % 4;
  int face = min(int(t), 3);
  float f = t - face;
  switch (face) {
  case 0:  return new PVector(lerp(-h, h, f), 0, h);    // front, left to right
  case 1:  return new PVector(h, 0, lerp(h, -h, f));    // right side, front to back
  case 2:  return new PVector(lerp(h, -h, f), 0, -h);   // back, right to left
  default: return new PVector(-h, 0, lerp(-h, h, f));   // left side, back to front
  }
}

// a closed box, folded like a cardboard box net. The middle band of the photo goes round the
// four sides; the square of photo above the front face folds over to make the lid, and the
// square below it folds under to make the bottom. p is a point in the flat photo (detection pixels).
// Each point gets one position, so neighbouring triangles always meet - one that crosses a fold
// just bends over the edge.
PVector onBox(PVector p) {
  float side = W / 4;
  float h = side / 2;
  float top = -(H/2 - capFold);   // y of the box's top and bottom faces
  float bottom = H/2 - capFold;
  float x = lerp(-h, h, p.x / side);   // across the front face, same as roundSquare's face 0

  int part = SIDES;
  if (p.x <= side && p.y < capFold) part = LID;
  if (p.x <= side && p.y > H - capFold) part = BASE;

  switch (part) {
  case LID:
    // the fold is at photo y = capFold, along the front top edge; further up the photo goes further back
    return new PVector(x, top, h - (capFold - p.y) * side / capFold);
  case BASE:
    // the fold is at photo y = H - capFold, along the front bottom edge; further down goes further back
    return new PVector(x, bottom, h - (p.y - (H - capFold)) * side / capFold);
  default:
    PVector s = roundSquare(p.x / W, side);
    s.y = p.y - H/2;
    return s;
  }
}

// a square pyramid, open base. The top of the photo is the tip, the bottom is the base edge
PVector onPyramid(float u, float v) {
  PVector p = roundSquare(u, W / 4);
  p.mult(v);   // squeeze in towards the tip
  p.y = v * H - H/2;
  return p;
}


// ---------------------------------------------------------------- distortion and the shape

// a new random move for every point (joined points share one, so the seam stays closed)
void randomise() {
  offset = new PVector[points.size()];
  for (int i = 0; i < points.size(); i++) {
    offset[i] = PVector.random3D().mult(random(1));
  }
}

// build the shape from base + offset * jitter. Texture coordinates come from the flat 2D points,
// so each triangle shows the part of the photo it was made from
void buildShape() {
  mesh3d = createShape();
  mesh3d.beginShape(TRIANGLES);
  mesh3d.textureMode(NORMAL);
  if (wireframe) {
    mesh3d.noFill();
    mesh3d.stroke(255, 120);
    mesh3d.strokeWeight(1);
  } else {
    mesh3d.noStroke();
    mesh3d.texture(img);
  }
  for (int t = 0; t < corners.length; t++) {
    if (shapeType == BOX && boxPart[t] == UNUSED) continue;
    for (int k = 0; k < 3; k++) {
      int i = corners[t][k];
      int j = joined[i];
      PVector p2 = points.get(i);
      float x = base[j].x + offset[j].x * jitter;
      float y = base[j].y + offset[j].y * jitter;
      float z = base[j].z + offset[j].z * jitter;
      mesh3d.vertex(x, y, z, p2.x / W, p2.y / H);
    }
  }
  mesh3d.endShape();
}


// ---------------------------------------------------------------- actions (buttons and keys)

void newDistortion()  { randomise(); buildShape(); }
void moreDistortion() { jitter = max(jitter * 1.25, 1); buildShape(); }
void lessDistortion() { jitter /= 1.25; buildShape(); }
void noDistortion()   { jitter = 0; buildShape(); }
void toggleWire()     { wireframe = !wireframe; buildShape(); }
void setShape(int s)  { shapeType = s; wrap(); buildShape(); }


// ---------------------------------------------------------------- buttons

class Button {
  String label;
  float x, y, w, h;
  Runnable action;
  int shapeIndex = -1;    // for shape buttons - lit when that shape is showing
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
    return (shapeIndex >= 0 && shapeIndex == shapeType) || (isWire && wireframe)
      || (picture != null && picture.equals(imageFile));
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
  float x = 16 * ui, y = 16 * ui, w = 110 * ui, h = 32 * ui, gap = 8 * ui;

  buttons.add(new Button("New (r)",  x, y, w, h, () -> newDistortion()));   y += h + gap;
  buttons.add(new Button("More (+)", x, y, w, h, () -> moreDistortion()));  y += h + gap;
  buttons.add(new Button("Less (-)", x, y, w, h, () -> lessDistortion()));  y += h + gap;
  buttons.add(new Button("None (0)", x, y, w, h, () -> noDistortion()));    y += h + gap;
  Button wire = new Button("Wire (w)", x, y, w, h, () -> toggleWire());
  wire.isWire = true;
  buttons.add(wire);                                                         y += h + gap * 4;

  for (int s = 0; s < shapeNames.length; s++) {
    final int shape = s;
    Button b = new Button(shapeNames[s] + " (" + (s + 1) + ")", x, y, w, h, () -> setShape(shape));
    b.shapeIndex = s;
    buttons.add(b);
    y += h + gap;
  }
  y += gap * 3;

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
  if (key == 'r') newDistortion();
  if (key == '+' || key == '=') moreDistortion();
  if (key == '-') lessDistortion();
  if (key == '0') noDistortion();
  if (key == 'w') toggleWire();
  if (key >= '1' && key <= '4') setShape(key - '1');
  if (key == 's') saveFrame("meshTube-####.png");
}
