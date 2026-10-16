// blobCurtains
// The curtains idea with blobs instead of triangles. A photo is divided into blobs of solid colour
// (about as many as meshTube's triangles, see the Blobs tab), then five copies are spread front to
// back so together they roughly fill a cube. The back one is complete; each of the four in front
// has a random 25% of its blobs cut out, so you see through the holes to the layers behind.
//
// Each layer is a flat picture with the missing blobs made transparent, so the holes are exactly
// blob shaped. See-through layers have to be drawn back to front, so they're sorted by distance
// from the camera every frame.
//
// Buttons on screen, or keys:
//   New gaps (r)   pick new missing blobs
//   More / Less    more / less missing (+ / -)
//   Photo (p)      fill the blobs with the photo instead of their flat colour
//   Outline (o)    outlines round the blobs
//   one button per picture in the data folder - drop more .jpg / .png files in there to add them
// Mouse drag orbits the camera, mouse wheel zooms, s saves a frame (blobCurtains-####.png).

// ---- settings ----
float ui = 2;             // size of the buttons and text (1 = small)
int detectWidth = 1000;   // the blobs are worked out on a copy this wide
int resolution = 12;      // meshTube's mesh settings - used to count its triangles
int threshold = 100;
int edgeSteps = 60;
float compactness = 10;   // low = blobs follow edges and wiggle, high = rounder and more even
int iterations = 10;
int layers = 5;           // the back one plus the curtains in front of it
float missing = 0.25;     // fraction of each curtain's blobs cut out

// ---- pictures ----
String[] imageFiles;
String imageFile;

// ---- blobs ----
PImage photo;             // the detection-sized copy of the photo
int[] labels;             // which blob each pixel belongs to
int blobCount;            // set by joinUp in the Blobs tab
PImage flat, flatOutlined;   // every blob in its average colour, without and with outlines
boolean usePhoto = false;
boolean outlines = false;
boolean[][] shown;        // for each layer, which blobs are there (the back layer has them all)
PImage[] layerImages;     // each layer's picture, with its missing blobs transparent

// ---- camera ----
float rotX = -0.2, rotY = -0.5;
float zoom = 1.3;
boolean orbiting = false; // true while a drag that started off the buttons is rotating the view

// ---- buttons ----
ArrayList<Button> buttons = new ArrayList<Button>();


void setup() {
  size(3200, 2000, P3D);
  // Processing can't see the screen size here, so it puts the window off the top left - put it
  // near the top left instead
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

  // the cube is as deep as the picture is wide; the layers are evenly spread through it
  float W = photo.width, H = photo.height;
  float[] z = new float[layers];
  for (int l = 0; l < layers; l++) z[l] = -W/2 + W * l / (layers - 1);

  // draw the farthest layer first. screenZ is bigger the farther away a point is
  Integer[] order = new Integer[layers];
  for (int l = 0; l < layers; l++) order[l] = l;
  final float[] depth = new float[layers];
  for (int l = 0; l < layers; l++) depth[l] = screenZ(0, 0, z[l]);
  java.util.Arrays.sort(order, (a, b) -> Float.compare(depth[b], depth[a]));

  // transparent pixels mustn't block the layers drawn after them, so don't write depth
  hint(DISABLE_DEPTH_MASK);
  noStroke();
  for (int l : order) {
    pushMatrix();
    translate(0, 0, z[l]);
    image(layerImages[l], -W/2, -H/2, W, H);
    popMatrix();
  }
  hint(ENABLE_DEPTH_MASK);
  popMatrix();

  // flat things drawn over the 3D
  hint(DISABLE_DEPTH_TEST);
  for (Button b : buttons) b.draw();
  fill(255, 160);
  textSize(14 * ui);
  textAlign(LEFT, BASELINE);
  text(imageFile + "   " + blobCount + " blobs per layer   " + round(missing * 100) + "% missing"
    + "   drag to orbit, wheel to zoom, s to save", 16 * ui, height - 16 * ui);
  hint(ENABLE_DEPTH_TEST);
}


// ---------------------------------------------------------------- pictures and blobs

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

// load a picture, divide it into blobs, pick the gaps and make the layers
void loadPicture(String file) {
  int t0 = millis();
  imageFile = file;
  photo = loadImage(file);
  photo.resize(detectWidth, 0);
  photo.loadPixels();
  int wanted = countTriangles(photo);
  labels = slic(photo, wanted, compactness, iterations);
  flat = fillBlobs(photo, labels, false);
  flatOutlined = fillBlobs(photo, labels, true);
  pickGaps();
  makeLayers();
  println(file + ": " + blobCount + " blobs in " + (millis() - t0) + " ms");
}

// for every layer but the back one, cut out a random `missing` share of the blobs.
// Each layer gets its own random set, so the holes don't line up
void pickGaps() {
  shown = new boolean[layers][blobCount];
  java.util.Arrays.fill(shown[0], true);   // the back layer is complete
  for (int layer = 1; layer < layers; layer++) {
    int[] order = new int[blobCount];
    for (int i = 0; i < blobCount; i++) order[i] = i;
    for (int i = blobCount - 1; i > 0; i--) {
      int j = int(random(i + 1));
      int t = order[i]; order[i] = order[j]; order[j] = t;
    }
    java.util.Arrays.fill(shown[layer], true);
    int drop = round(blobCount * missing);
    for (int i = 0; i < drop; i++) shown[layer][order[i]] = false;
  }
}

// each layer's picture: the blobs it keeps, in flat colour or the photo, and see-through where a blob is cut out
void makeLayers() {
  layerImages = new PImage[layers];
  int w = photo.width, h = photo.height;
  boolean edgeLines = outlines;
  for (int layer = 0; layer < layers; layer++) {
    PImage img = createImage(w, h, ARGB);
    img.loadPixels();
    for (int i = 0; i < w * h; i++) {
      int c;
      if (edgeLines && flatOutlined.pixels[i] == 0xFF000000) c = 0xFF000000;
      else if (usePhoto) c = photo.pixels[i];
      else c = flat.pixels[i];
      // a cut out blob keeps its colour but is fully see-through - if it were black, the smoothing
      // the graphics card does at the edge of each hole would leave a dark fringe
      int alpha = shown[layer][labels[i]] ? 0xFF000000 : 0;
      img.pixels[i] = alpha | (c & 0xFFFFFF);
    }
    img.updatePixels();
    layerImages[layer] = img;
  }
}


// ---------------------------------------------------------------- actions (buttons and keys)

void newGaps()        { pickGaps(); makeLayers(); }
void moreMissing()    { missing = min(missing + 0.05, 0.95); pickGaps(); makeLayers(); }
void lessMissing()    { missing = max(missing - 0.05, 0);    pickGaps(); makeLayers(); }
void togglePhoto()    { usePhoto = !usePhoto; makeLayers(); }
void toggleOutlines() { outlines = !outlines; makeLayers(); }


// ---------------------------------------------------------------- buttons

class Button {
  String label;
  float x, y, w, h;
  Runnable action;
  int lights = 0;         // 1 = lit while photo fill is on, 2 = lit while outlines are on
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
    return (lights == 1 && usePhoto) || (lights == 2 && outlines)
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
  float x = 16 * ui, y = 16 * ui, w = 130 * ui, h = 32 * ui, gap = 8 * ui;

  buttons.add(new Button("New gaps (r)", x, y, w, h, () -> newGaps()));     y += h + gap;
  buttons.add(new Button("More (+)",     x, y, w, h, () -> moreMissing())); y += h + gap;
  buttons.add(new Button("Less (-)",     x, y, w, h, () -> lessMissing())); y += h + gap;
  Button ph = new Button("Photo (p)", x, y, w, h, () -> togglePhoto());
  ph.lights = 1;
  buttons.add(ph);                                                           y += h + gap;
  Button ol = new Button("Outline (o)", x, y, w, h, () -> toggleOutlines());
  ol.lights = 2;
  buttons.add(ol);                                                           y += h + gap * 4;

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
  if (key == 'p') togglePhoto();
  if (key == 'o') toggleOutlines();
  if (key == 's') saveFrame("blobCurtains-####.png");
}
