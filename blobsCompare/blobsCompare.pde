// blobsCompare
// Divides a photo into blobs of solid colour - about as many as the triangles meshTube makes from
// the same photo, so they're about the same size - and shows them next to the original.
//
// The blobs are "superpixels" (the SLIC method): seeds are spread over the picture on a grid,
// and each grows into the pixels around it that are close in colour, stopping at strong edges.
// Each blob is then filled with its average colour.
//
// Neighbouring blobs that are close in colour can then be joined into bigger regions - the slider
// under the blob picture sets how close is close enough (see the Join tab). A second slider
// smooths the lines between regions (see the Smooth tab).
//
// Make 3D turns every region into its own solid piece - its outline extruded into a slab - and
// shows them in a second window where they can be clicked and moved (see the Pieces and World tabs).
//
// Keys:
//   1 2 3 4 ...  pick a picture (every .jpg / .png in the data folder, in name order)
//   o            outlines on / off
//   [ / ]        rounder / wigglier blobs (compactness)
//   - / +        bigger / smaller blobs (fewer / more of them)
//   , / .        join less / more (or drag the join slider under the blob picture)
//   ; / '        smooth the edges less / more (or drag the smooth slider)
//   m            make 3D pieces (or click Make 3D)
//   s            save the blob picture (blobsCompare-####.png in the sketch folder)
//
// EdgeDetector.java and Triangulate.java are the same as meshTube's - they are only used here to
// count how many triangles the mesh would have.

// ---- settings ----
float ui = 2;             // size of the text (1 = small)
int detectWidth = 1000;   // the blobs are worked out on a copy this wide
int resolution = 12;      // the mesh settings from meshTube - used to count its triangles
int threshold = 100;
int edgeSteps = 60;
float blobScale = 1;      // 1 = as many blobs as triangles; 2 = twice as many (smaller), 0.5 = half
float compactness = 10;   // low = blobs follow edges closely and wiggle, high = rounder and more even
int iterations = 10;      // how many times the blobs are refined
float joinBelow = 0;      // join touching blobs whose colours differ by less than this (0 = no joining)
float joinMax = 40;       // the slider's top end
float smoothRadius = 0;   // how far round each boundary pixel the smoothing looks, in pixels (0 = off)
float smoothMax = 16;     // the slider's top end

// ---- state ----
String[] imageFiles;
int current = 0;
PImage original;          // the detection-sized copy of the photo
PImage blobbed;           // the result
int[] labels;             // which blob each pixel of the copy belongs to
int[] joined;             // which region each pixel is in after joining
int[] smoothed;           // ... and after smoothing the edges
int regionsLeft;          // how many regions there are after smoothing
int processMs;            // how long the last join + smooth took
int blobCount, triangleCount;
boolean outlines = false;
int buildMs;
float sliderX, sliderW, joinY, smoothY;   // where the sliders are drawn (set in draw)
int dragging = 0;         // 0 = no slider, 1 = join, 2 = smooth
boolean pending = false;  // a slider moved but the update was too slow to do while dragging
float buttonX, buttonY, buttonW, buttonH;   // the Make 3D button (set in draw)
World world;              // the 3D window, once it's been opened


void setup() {
  size(3200, 2000, P2D);
  // Processing can't see the screen size here, so it puts the window off the top left - put it
  // near the top left instead
  surface.setLocation(40, 80);

  imageFiles = findImages();
  if (imageFiles.length == 0) {
    println("no .jpg or .png files in " + dataPath(""));
    exit();
    return;
  }
  loadPicture(0);
}

void draw() {
  background(20);
  float panel = min(width / 2 - 60 * ui, height - 120 * ui);   // each picture is drawn this big
  float top = 50 * ui;
  float leftX = width / 4 - panel / 2;
  float rightX = 3 * width / 4 - panel / 2;
  float ph = panel * original.height / original.width;
  image(original, leftX, top, panel, ph);
  image(blobbed, rightX, top, panel, ph);

  fill(255, 200);
  textSize(16 * ui);
  textAlign(CENTER, BASELINE);
  text("original", leftX + panel / 2, top - 12 * ui);
  String count = blobCount + " blobs";
  if (regionsLeft < blobCount) count += " joined into " + regionsLeft + " regions";
  text(count + "  (the mesh would have " + triangleCount + " triangles)", rightX + panel / 2, top - 12 * ui);

  // the sliders, under the blob picture
  sliderX = rightX;
  sliderW = panel;
  joinY = top + ph + 34 * ui;
  smoothY = joinY + 70 * ui;
  drawSlider(joinY, sqrt(joinBelow / joinMax), "join neighbours closer in colour than " + nf(joinBelow, 0, 2)
    + "     (0 = off,  ~2 barely different,  10 clearly different,  25+ another colour)");
  // the Make 3D button, under the original
  buttonW = 220 * ui;
  buttonH = 40 * ui;
  buttonX = leftX + panel / 2 - buttonW / 2;
  buttonY = joinY - buttonH / 2;
  boolean overButton = mouseX >= buttonX && mouseX <= buttonX + buttonW && mouseY >= buttonY && mouseY <= buttonY + buttonH;
  stroke(255, 90);
  strokeWeight(1);
  fill(overButton ? color(90, 150, 220) : color(70, 130, 200));
  rect(buttonX, buttonY, buttonW, buttonH, 6 * ui);
  fill(255);
  textSize(16 * ui);
  textAlign(CENTER, CENTER);
  text("Make 3D (m)", buttonX + buttonW / 2, buttonY + buttonH / 2 - 1);

  drawSlider(smoothY, smoothRadius / smoothMax, "smooth edges over " + nf(smoothRadius, 0, 1) + " pixels     (0 = off)"
    + (pending ? "     - updates when you let go" : ""));

  // the controls, under the original
  String[][] controls = {
    { "1 - " + imageFiles.length, "pick a picture" },
    { "o", "outlines on / off" },
    { "[  ]", "wigglier / rounder blobs" },
    { "-  +", "bigger / smaller blobs" },
    { "s", "save the blob picture" },
    { ",  .", "join less / more" },
    { ";  '", "smooth less / more" },
    { "m", "Make 3D" },
  };
  drawControls(controls, leftX, buttonY + buttonH + 24 * ui, panel);

  fill(255, 130);
  textSize(13 * ui);
  textAlign(LEFT, BASELINE);
  text(imageFiles[current] + "     compactness " + nf(compactness, 0, 1) + "     blob scale " + nf(blobScale, 0, 2)
    + "     blobs built in " + buildMs + " ms     sliders: drag them - with smoothing on they update when you let go",
    16 * ui, height - 12 * ui);
}

// a list of key / what-it-does pairs in two columns
void drawControls(String[][] rows, float x, float y, float w) {
  int perColumn = (rows.length + 1) / 2;
  float lineH = 26 * ui;
  textSize(15 * ui);
  for (int i = 0; i < rows.length; i++) {
    float cx = x + (i / perColumn) * w / 2;
    float cy = y + (i % perColumn) * lineH;
    textAlign(RIGHT, TOP);
    fill(120, 180, 240);
    text(rows[i][0], cx + 70 * ui, cy);
    textAlign(LEFT, TOP);
    fill(255, 210);
    text(rows[i][1], cx + 90 * ui, cy);
  }
}


// ---------------------------------------------------------------- pictures

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

void loadPicture(int i) {
  current = i;
  original = loadImage(imageFiles[i]);
  original.resize(detectWidth, 0);
  original.loadPixels();
  triangleCount = countTriangles(original);
  rebuild();
}

// work out the blobs again for the current picture and settings
void rebuild() {
  int t0 = millis();
  int wanted = max(1, round(triangleCount * blobScale));
  labels = slic(original, wanted, compactness, iterations);
  blobStats(original, labels, blobCount);
  applyJoin();
  buildMs = millis() - t0;
  println(imageFiles[current] + ": " + blobCount + " blobs (asked for " + wanted + ") in " + buildMs + " ms");
}

// join neighbours for the current setting and repaint
void applyJoin() {
  int t0 = millis();
  joined = joinBlobs(labels, blobCount, joinBelow);
  smoothed = smoothLabels(joined, original.width, original.height, smoothRadius);
  regionsLeft = countRegions(smoothed);
  blobbed = fillBlobs(original, smoothed, outlines);
  processMs = millis() - t0;
}

// a slider: a line, a knob at position f (0..1) along it, and a label underneath
void drawSlider(float y, float f, String label) {
  stroke(255, 90);
  strokeWeight(2 * ui);
  line(sliderX, y, sliderX + sliderW, y);
  noStroke();
  fill(70, 130, 200);
  circle(sliderX + sliderW * f, y, 18 * ui);
  fill(255, 200);
  textSize(14 * ui);
  textAlign(CENTER, TOP);
  text(label, sliderX + sliderW / 2, y + 16 * ui);
}

// the number of triangles meshTube's mesh would have for this picture (without its fold lines)
int countTriangles(PImage small) {
  ArrayList<PVector> pts = new ArrayList<PVector>();
  ArrayList<Triangle> tris = new ArrayList<Triangle>();
  float W = small.width, H = small.height;
  EdgeDetector.extractPoints(pts, small, EdgeDetector.SOBEL, threshold, resolution);
  for (int i = 0; i <= edgeSteps; i++) {
    pts.add(new PVector(0, H * i / edgeSteps));
    pts.add(new PVector(W, H * i / edgeSteps));
  }
  int across = round(edgeSteps * W / H);
  for (int i = 1; i < across; i++) {
    pts.add(new PVector(W * i / across, 0));
    pts.add(new PVector(W * i / across, H));
  }
  Triangulator.triangulate(pts, tris);
  return tris.size();
}


// ---------------------------------------------------------------- SLIC superpixels

// Returns a blob number for every pixel of img. About `wanted` blobs; compactness trades
// following the edges (low) against round, even blobs (high).
int[] slic(PImage img, int wanted, float compactness, int iterations) {
  int w = img.width, h = img.height, n = w * h;

  // colours in Lab, where distance matches how different colours look
  float[] L = new float[n], A = new float[n], B = new float[n];
  float[] lab = new float[3];
  for (int i = 0; i < n; i++) {
    toLab(img.pixels[i], lab);
    L[i] = lab[0]; A[i] = lab[1]; B[i] = lab[2];
  }

  // seeds on a grid S pixels apart, each nudged to the smoothest spot nearby so it doesn't start on an edge
  int S = max(2, round(sqrt((float) n / wanted)));
  ArrayList<float[]> seeds = new ArrayList<float[]>();   // l, a, b, x, y
  for (int y = S / 2; y < h; y += S) {
    for (int x = S / 2; x < w; x += S) {
      int bx = x, by = y;
      float best = Float.MAX_VALUE;
      for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
          int xx = x + dx, yy = y + dy;
          if (xx < 1 || yy < 1 || xx >= w - 1 || yy >= h - 1) continue;
          float g = sq(L[yy * w + xx + 1] - L[yy * w + xx - 1]) + sq(L[(yy + 1) * w + xx] - L[(yy - 1) * w + xx]);
          if (g < best) { best = g; bx = xx; by = yy; }
        }
      }
      int i = by * w + bx;
      seeds.add(new float[] { L[i], A[i], B[i], bx, by });
    }
  }
  int k = seeds.size();

  int[] label = new int[n];
  float[] dist = new float[n];
  float spatial = sq(compactness / S);   // how much a pixel of distance counts against a unit of colour difference

  for (int it = 0; it < iterations; it++) {
    java.util.Arrays.fill(dist, Float.MAX_VALUE);
    // each seed claims the pixels within 2S x 2S of it that are closer to it than to any other seed
    for (int c = 0; c < k; c++) {
      float[] s = seeds.get(c);
      int x0 = max(0, (int) (s[3] - S)), x1 = min(w, (int) (s[3] + S) + 1);
      int y0 = max(0, (int) (s[4] - S)), y1 = min(h, (int) (s[4] + S) + 1);
      for (int y = y0; y < y1; y++) {
        for (int x = x0; x < x1; x++) {
          int i = y * w + x;
          float d = sq(L[i] - s[0]) + sq(A[i] - s[1]) + sq(B[i] - s[2])
                  + (sq(x - s[3]) + sq(y - s[4])) * spatial;
          if (d < dist[i]) { dist[i] = d; label[i] = c; }
        }
      }
    }
    // move each seed to the middle (and average colour) of what it claimed
    float[][] sum = new float[k][6];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int i = y * w + x;
        float[] t = sum[label[i]];
        t[0] += L[i]; t[1] += A[i]; t[2] += B[i]; t[3] += x; t[4] += y; t[5]++;
      }
    }
    for (int c = 0; c < k; c++) {
      float cnt = sum[c][5];
      if (cnt == 0) continue;
      float[] s = seeds.get(c);
      for (int j = 0; j < 5; j++) s[j] = sum[c][j] / cnt;
    }
  }

  return joinUp(label, w, h, S * S / 4);
}

// A blob can end up in several unconnected pieces. Give each connected piece its own number, and
// fold pieces smaller than minSize into the blob next to them.
int[] joinUp(int[] label, int w, int h, int minSize) {
  int n = w * h;
  int[] out = new int[n];
  java.util.Arrays.fill(out, -1);
  int[] queue = new int[n];
  int[] dx = { -1, 1, 0, 0 }, dy = { 0, 0, -1, 1 };
  int next = 0;

  for (int start = 0; start < n; start++) {
    if (out[start] >= 0) continue;
    // a neighbour that's already numbered - small pieces join it
    int adjacent = -1;
    int sx = start % w, sy = start / w;
    for (int d = 0; d < 4; d++) {
      int xx = sx + dx[d], yy = sy + dy[d];
      if (xx >= 0 && yy >= 0 && xx < w && yy < h && out[yy * w + xx] >= 0) adjacent = out[yy * w + xx];
    }
    // flood fill the piece
    int head = 0, tail = 0;
    queue[tail++] = start;
    out[start] = next;
    while (head < tail) {
      int i = queue[head++];
      int x = i % w, y = i / w;
      for (int d = 0; d < 4; d++) {
        int xx = x + dx[d], yy = y + dy[d];
        if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
        int j = yy * w + xx;
        if (out[j] < 0 && label[j] == label[start]) { out[j] = next; queue[tail++] = j; }
      }
    }
    if (tail < minSize && adjacent >= 0) {
      for (int q = 0; q < tail; q++) out[queue[q]] = adjacent;
    } else {
      next++;
    }
  }
  blobCount = next;
  return out;
}

// paint every blob its average colour, with black outlines where neighbouring blobs meet if asked
PImage fillBlobs(PImage img, int[] label, boolean withOutlines) {
  int w = img.width, h = img.height, n = w * h;
  int count = 0;                       // labels run 0 .. count-1
  for (int i = 0; i < n; i++) count = max(count, label[i] + 1);
  long[][] sum = new long[count][4];
  for (int i = 0; i < n; i++) {
    int c = img.pixels[i];
    long[] s = sum[label[i]];
    s[0] += (c >> 16) & 0xFF; s[1] += (c >> 8) & 0xFF; s[2] += c & 0xFF; s[3]++;
  }
  int[] colour = new int[count];
  for (int b = 0; b < count; b++) {
    long cnt = Math.max(1L, sum[b][3]);
    colour[b] = color(sum[b][0] / cnt, sum[b][1] / cnt, sum[b][2] / cnt);
  }

  PImage out = createImage(w, h, RGB);
  out.loadPixels();
  for (int y = 0; y < h; y++) {
    for (int x = 0; x < w; x++) {
      int i = y * w + x;
      boolean edge = withOutlines && ((x + 1 < w && label[i + 1] != label[i]) || (y + 1 < h && label[i + w] != label[i]));
      out.pixels[i] = edge ? color(0) : colour[label[i]];
    }
  }
  out.updatePixels();
  return out;
}

// sRGB colour to CIE Lab (D65)
void toLab(int c, float[] lab) {
  float r = lin(((c >> 16) & 0xFF) / 255.0), g = lin(((c >> 8) & 0xFF) / 255.0), b = lin((c & 0xFF) / 255.0);
  float x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047;
  float y = (r * 0.2126 + g * 0.7152 + b * 0.0722);
  float z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883;
  x = labF(x); y = labF(y); z = labF(z);
  lab[0] = 116 * y - 16;
  lab[1] = 500 * (x - y);
  lab[2] = 200 * (y - z);
}
float lin(float v)  { return v <= 0.04045 ? v / 12.92 : pow((v + 0.055) / 1.055, 2.4); }
float labF(float t) { return t > 0.008856 ? pow(t, 1.0 / 3) : 7.787 * t + 16.0 / 116; }


// ---------------------------------------------------------------- keys

void keyPressed() {
  if (key >= '1' && key <= '9' && key - '1' < imageFiles.length) loadPicture(key - '1');
  if (key == 'o') { outlines = !outlines; blobbed = fillBlobs(original, smoothed, outlines); }
  if (key == ',') { joinBelow = max(0, joinBelow - 0.5); applyJoin(); }
  if (key == '.') { joinBelow = min(joinMax, joinBelow + 0.5); applyJoin(); }
  if (key == 'm') make3D();
  if (key == ';') { smoothRadius = max(0, smoothRadius - 1); applyJoin(); }
  if (key == '\'') { smoothRadius = min(smoothMax, smoothRadius + 1); applyJoin(); }
  if (key == '[') { compactness = max(1, compactness / 1.5); rebuild(); }
  if (key == ']') { compactness = min(80, compactness * 1.5); rebuild(); }
  if (key == '-') { blobScale /= 1.5; rebuild(); }
  if (key == '+' || key == '=') { blobScale *= 1.5; rebuild(); }
  if (key == 's') blobbed.save(sketchPath("blobsCompare-" + nf(frameCount, 4) + ".png"));
}


// ---------------------------------------------------------------- the sliders

boolean onSlider(float y) {
  return abs(mouseY - y) < 20 * ui && mouseX > sliderX - 20 * ui && mouseX < sliderX + sliderW + 20 * ui;
}

void mousePressed() {
  if (mouseX >= buttonX && mouseX <= buttonX + buttonW && mouseY >= buttonY && mouseY <= buttonY + buttonH) {
    make3D();
    return;
  }
  if (onSlider(joinY)) dragging = 1;
  else if (onSlider(smoothY)) dragging = 2;
  if (dragging > 0) slideTo(mouseX, false);
}

void mouseDragged() {
  if (dragging > 0) slideTo(mouseX, false);
}

void mouseReleased() {
  if (dragging > 0 && pending) applyJoin();
  pending = false;
  dragging = 0;
}

// move the slider being dragged. If the last update took too long to keep up with the mouse,
// just move the knob and update when the mouse is let go
void slideTo(float mx, boolean force) {
  float f = constrain((mx - sliderX) / sliderW, 0, 1);
  boolean changed = false;
  if (dragging == 1) {
    // squared, so the low end - where small changes matter most - gets more of the slider
    float v = round(f * f * joinMax * 4) / 4.0;   // steps of 0.25
    changed = v != joinBelow;
    joinBelow = v;
  } else if (dragging == 2) {
    float v = round(f * smoothMax * 2) / 2.0;      // steps of 0.5
    changed = v != smoothRadius;
    smoothRadius = v;
  }
  if (!changed) return;
  if (processMs > 150 && !force) pending = true;
  else applyJoin();
}


// ---------------------------------------------------------------- 3D

// turn the regions as they are now into pieces and show them in the 3D window
void make3D() {
  int t0 = millis();
  ArrayList<Piece> pieces = makePieces(smoothed, original, 0.8, 6);
  println("3D: traced " + pieces.size() + " pieces in " + (millis() - t0) + " ms");
  // every picture can go on the pieces or be the backdrop in the 3D window
  String[] paths = new String[imageFiles.length];
  for (int i = 0; i < imageFiles.length; i++) paths[i] = dataPath(imageFiles[i]);
  if (world == null) {
    world = new World();
    world.show(pieces, original.width, original.height, paths, current);
    world.setBackdrops(paths, (current + 1) % imageFiles.length);   // start on the picture after this one
    PApplet.runSketch(new String[] { "blobs in 3D" }, world);
  } else {
    world.show(pieces, original.width, original.height, paths, current);
  }
}
