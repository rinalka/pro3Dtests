// The landscape window. The whole picture is one surface - a fine grid of points textured with a
// photo - and every region from the main window can be pushed out or pulled in. The steps between
// regions are rounded off, and Perlin noise adds hills and valleys, so it becomes a landscape whose
// shapes still come from the blobs.
//
// How the height of each point is worked out:
//   - every region has a height (all 0 to start; e gives them random ones; dragging changes one)
//   - each point looks at the regions within `rounding` pixels of it, with nearer pixels counting
//     more, and takes that blend of their heights. So inside a region the surface is flat at the
//     region's height, and at the edges it slopes smoothly into the next region
//   - Perlin noise is added on top
//
// Mouse: left-drag on the landscape pushes / pulls the region under the mouse (up = towards you).
// Orbit with right-drag, shift + drag, a left-drag off the landscape, or the arrow keys: sideways
// spins the landscape like a turntable, up / down changes how steeply you look down on it.
// Wheel zooms.
// The surface is cut into tiles so that changing one region only rebuilds the tiles it touches.
//
// Closing the window only hides it; Make landscape in the main window shows it again.

class Scape extends PApplet {
  // ---- settings ----
  float spacing = 4;        // grid step, in picture pixels (smaller = finer, slower)
  int tileQuads = 24;       // grid squares along each side of a tile
  float rounding = 16;      // how far the step between regions is smoothed, in pixels (0 = sharp)
  float depth = 60;         // how far e pushes and pulls regions, in pixels
  float bumpHeight = 10;    // how high the Perlin hills are, in pixels
  float bumpSize = 60;      // roughly how wide a hill is, in pixels
  float bumpSeed = 0;
  boolean shading = true;   // light and shade (shows the relief), or every surface evenly lit
  boolean textured = true;  // the photo on the landscape, or each region in its own average colour
  boolean showControls = true;

  // ---- from the main window ----
  int[] incomingLabels;
  int[] incomingColours;
  int incomingW, incomingH;
  boolean pending = false;
  String[] picturePaths = new String[0];
  int shapesPicture = 0;

  // ---- the regions and the grid ----
  int[] labels;                 // the region of every picture pixel
  int[] regionColour;           // the average colour of each region
  int[] pointColour;            // each grid point's colour: its regions' colours, blended like the heights
  int W, H;                     // picture size (the working copy's)
  int regions;
  float[] regionHeight = new float[0];
  int cols, rows;               // grid points across and down
  int[][] wReg;                 // for every grid point, the regions near it ...
  float[][] wVal;               // ... and how much each one counts (they add up to 1)
  float[] bump;                 // the Perlin noise at every grid point
  float[] z;                    // the height of every grid point
  int tilesX, tilesY;
  PShape[] tiles = new PShape[0];
  boolean[] dirty = new boolean[0];
  boolean[][] regionTiles;      // which tiles each region touches

  // ---- pictures ----
  PImage photo;
  int texturePicture = -1;      // -1 = the picture the regions came from
  java.util.HashMap<Integer, PImage> loaded = new java.util.HashMap<Integer, PImage>();
  String[] backdropPaths = new String[0];
  int backdropIndex = -1;
  PImage backdrop;

  // ---- camera and mouse ----
  float tilt = 0.8;          // 0 = looking straight down on the picture, bigger = closer to the horizon
  float spin = 0;            // turntable turn round the landscape's up direction
  float zoom = 1.3;
  int dragMode = 0;             // 0 nothing, 1 orbiting, 2 pushing a region
  int grabbed = -1;
  boolean hidden = false;

  public void settings() {
    size(2400, 1600, P3D);
  }

  public void setup() {
    surface.setTitle("blob landscape");
    surface.setLocation(160, 160);
  }

  // called from the main window
  void show(int[] lab, int[] colours, int w, int h, String[] paths, int fromPicture) {
    incomingColours = colours;
    picturePaths = paths;
    shapesPicture = fromPicture;
    incomingW = w;
    incomingH = h;
    incomingLabels = lab;
    pending = true;
    if (hidden) {
      hidden = false;
      surface.setVisible(true);
      loop();
    }
  }

  void setBackdrops(String[] paths, int start) {
    backdropPaths = paths;
    backdropIndex = start;
    backdrop = null;
  }

  // closing the window just hides it, instead of quitting everything
  public void exit() {
    hidden = true;
    surface.setVisible(false);
    noLoop();
  }

  public void draw() {
    if (pending) {
      pending = false;
      regionColour = incomingColours;
      newRegions(incomingLabels, incomingW, incomingH);
    }
    rebuildDirtyTiles();

    background(25);
    drawBackdrop();

    pushMatrix();
    translate(width / 2, height / 2, 0);
    rotateX(tilt);
    rotateZ(spin);
    scale(zoom);
    if (shading) lights(); else noLights();
    for (PShape t : tiles) if (t != null) shape(t);
    popMatrix();

    noLights();
    hint(DISABLE_DEPTH_TEST);
    if (showControls) {
      drawControls();
    } else {
      fill(255, 90);
      textSize(22);
      textAlign(LEFT, TOP);
      text("space: menu", 24, 24);
    }
    hint(ENABLE_DEPTH_TEST);
  }


  // ------------------------------------------------------------ building the landscape

  // new regions from the main window: everything starts flat
  void newRegions(int[] lab, int w, int h) {
    int t0 = millis();
    labels = lab;
    W = w;
    H = h;
    regions = 0;
    for (int l : labels) regions = max(regions, l + 1);
    regionHeight = new float[regions];
    cols = floor((W - 1) / spacing) + 1;
    rows = floor((H - 1) / spacing) + 1;
    tilesX = ceil((cols - 1) / (float) tileQuads);
    tilesY = ceil((rows - 1) / (float) tileQuads);
    tiles = new PShape[tilesX * tilesY];
    dirty = new boolean[tiles.length];
    photo = picture(textureNow());
    computeWeights();
    computeBumps();
    computeHeights();
    java.util.Arrays.fill(dirty, true);
    println("landscape: " + regions + " regions, " + cols + " x " + rows + " points, " + tiles.length
      + " tiles, set up in " + (millis() - t0) + " ms");
  }

  // where grid point (i, j) is in the picture
  float gx(int i) { return i * (W - 1) / (float) (cols - 1); }
  float gy(int j) { return j * (H - 1) / (float) (rows - 1); }

  // for every grid point, which regions are within `rounding` pixels and how much each counts
  void computeWeights() {
    int t0 = millis();
    int n = cols * rows;
    wReg = new int[n][];
    wVal = new float[n][];
    pointColour = new int[n];
    regionTiles = new boolean[regions][tiles.length];
    float R = rounding;
    int stepPx = max(1, round(R / 6));
    float sigma = max(R / 2, 0.5);
    int[] candR = new int[512];
    float[] candW = new float[512];

    for (int j = 0; j < rows; j++) {
      for (int i = 0; i < cols; i++) {
        int k = j * cols + i;
        float x = gx(i), y = gy(j);
        int cands = 0;
        if (R < 1) {
          candR[0] = labels[round(y) * W + round(x)];
          candW[0] = 1;
          cands = 1;
        } else {
          int r = ceil(R);
          float total = 0;
          for (int dy = -r; dy <= r; dy += stepPx) {
            for (int dx = -r; dx <= r; dx += stepPx) {
              if (dx * dx + dy * dy > R * R) continue;
              int px = constrain(round(x + dx), 0, W - 1), py = constrain(round(y + dy), 0, H - 1);
              int l = labels[py * W + px];
              float wt = exp(-(dx * dx + dy * dy) / (2 * sigma * sigma));
              int c = 0;
              while (c < cands && candR[c] != l) c++;
              if (c == cands) { if (cands == candR.length) continue; candR[c] = l; candW[c] = 0; cands++; }
              candW[c] += wt;
              total += wt;
            }
          }
          for (int c = 0; c < cands; c++) candW[c] /= total;
        }
        wReg[k] = java.util.Arrays.copyOf(candR, cands);
        wVal[k] = java.util.Arrays.copyOf(candW, cands);
        // the colour, blended the same way as the height
        float cr = 0, cg = 0, cb = 0;
        for (int c = 0; c < cands; c++) {
          int col = regionColour[candR[c]];
          cr += candW[c] * ((col >> 16) & 0xFF); cg += candW[c] * ((col >> 8) & 0xFF); cb += candW[c] * (col & 0xFF);
        }
        pointColour[k] = color(cr, cg, cb);
        // the tiles this point is in (a point on a tile edge is in the tiles either side)
        for (int c = 0; c < cands; c++) markTilesOfPoint(regionTiles[candR[c]], i, j);
      }
    }
    println("landscape: rounding worked out in " + (millis() - t0) + " ms");
  }

  // the tiles that use grid point (i, j), and their neighbours (whose shading looks at this point)
  void markTilesOfPoint(boolean[] mark, int i, int j) {
    int tx = min(i / tileQuads, tilesX - 1), ty = min(j / tileQuads, tilesY - 1);
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        int x = tx + dx, y = ty + dy;
        if (x >= 0 && y >= 0 && x < tilesX && y < tilesY) mark[y * tilesX + x] = true;
      }
    }
  }

  void computeBumps() {
    bump = new float[cols * rows];
    for (int j = 0; j < rows; j++) {
      for (int i = 0; i < cols; i++) {
        bump[j * cols + i] = (noise(gx(i) / bumpSize, gy(j) / bumpSize, bumpSeed) - 0.5) * 2 * bumpHeight;
      }
    }
  }

  // every point's height: the blend of its regions' heights, plus the noise
  void computeHeights() {
    int n = cols * rows;
    if (z == null || z.length != n) z = new float[n];
    for (int k = 0; k < n; k++) {
      float h = bump[k];
      int[] r = wReg[k];
      float[] w = wVal[k];
      for (int c = 0; c < r.length; c++) h += w[c] * regionHeight[r[c]];
      z[k] = h;
    }
  }

  void markRegionDirty(int r) {
    for (int t = 0; t < tiles.length; t++) if (regionTiles[r][t]) dirty[t] = true;
  }

  void allDirty() {
    java.util.Arrays.fill(dirty, true);
  }

  void rebuildDirtyTiles() {
    for (int t = 0; t < tiles.length; t++) {
      if (dirty[t]) {
        tiles[t] = buildTile(t % tilesX, t / tilesX);
        dirty[t] = false;
      }
    }
  }

  // one tile of the surface: two triangles per grid square, with the photo on it
  PShape buildTile(int tx, int ty) {
    int i0 = tx * tileQuads, j0 = ty * tileQuads;
    int i1 = min(i0 + tileQuads, cols - 1), j1 = min(j0 + tileQuads, rows - 1);
    PShape s = createShape();
    s.beginShape(TRIANGLES);
    s.noStroke();
    if (textured && photo != null) {
      s.textureMode(NORMAL);
      s.texture(photo);
      s.fill(255);
    }
    for (int j = j0; j < j1; j++) {
      for (int i = i0; i < i1; i++) {
        corner(s, i, j);     corner(s, i + 1, j);     corner(s, i + 1, j + 1);
        corner(s, i, j);     corner(s, i + 1, j + 1); corner(s, i, j + 1);
      }
    }
    s.endShape();
    return s;
  }

  // one corner: its place, a normal from the slope round it (for the shading), and where it is in the photo
  void corner(PShape s, int i, int j) {
    int k = j * cols + i;
    int il = max(i - 1, 0), ir = min(i + 1, cols - 1), ju = max(j - 1, 0), jd = min(j + 1, rows - 1);
    float dzdx = (z[j * cols + ir] - z[j * cols + il]) / (gx(ir) - gx(il));
    float dzdy = (z[jd * cols + i] - z[ju * cols + i]) / (gy(jd) - gy(ju));
    s.normal(-dzdx, -dzdy, 1);
    float x = gx(i), y = gy(j);
    if (textured && photo != null) {
      s.vertex(x - W / 2.0, y - H / 2.0, z[k], x / W, y / H);
    } else {
      s.fill(pointColour[k]);
      s.vertex(x - W / 2.0, y - H / 2.0, z[k]);
    }
  }


  // ------------------------------------------------------------ finding the region under the mouse

  // follow a line from the eye through the mouse into the scene until it goes under the surface
  int regionAt(int mx, int my) {
    if (labels == null) return -1;
    float camZ = (height / 2.0) / tan(PI / 6);   // where Processing's default camera sits
    PVector eye = new PVector(0, 0, camZ);          // relative to the middle of the window
    PVector dir = new PVector(mx - width / 2.0, my - height / 2.0, -camZ);
    // undo the view's turn and zoom, so the line is in the landscape's own coordinates
    PMatrix3D undo = new PMatrix3D();
    undo.scale(1 / zoom);
    undo.rotateZ(-spin);
    undo.rotateX(-tilt);
    PVector o = undo.mult(eye, null), d = undo.mult(dir, null);
    d.normalize();

    float step = spacing / 2;
    float maxT = o.mag() + 2 * max(W, H) + 1000;
    for (float t = 0; t < maxT; t += step) {
      float x = o.x + d.x * t + W / 2.0, y = o.y + d.y * t + H / 2.0, pz = o.z + d.z * t;
      if (x < 0 || y < 0 || x > W - 1 || y > H - 1) continue;
      if (pz <= heightAt(x, y)) return labels[round(y) * W + round(x)];
    }
    return -1;
  }

  // the surface height at a picture position, between grid points
  float heightAt(float x, float y) {
    float fi = x / (W - 1) * (cols - 1), fj = y / (H - 1) * (rows - 1);
    int i = constrain(floor(fi), 0, cols - 2), j = constrain(floor(fj), 0, rows - 2);
    float u = fi - i, v = fj - j;
    float a = z[j * cols + i], b = z[j * cols + i + 1], c = z[(j + 1) * cols + i], e = z[(j + 1) * cols + i + 1];
    return lerp(lerp(a, b, u), lerp(c, e, u), v);
  }


  // ------------------------------------------------------------ pictures and backdrop

  int textureNow() {
    return texturePicture >= 0 ? texturePicture : shapesPicture;
  }

  PImage picture(int i) {
    if (i < 0 || i >= picturePaths.length) return null;
    PImage img = loaded.get(i);
    if (img == null) {
      img = loadImage(picturePaths[i]);
      loaded.put(i, img);
    }
    return img;
  }

  void drawBackdrop() {
    if (backdropIndex < 0 || backdropIndex >= backdropPaths.length) return;
    if (backdrop == null) backdrop = loadImage(backdropPaths[backdropIndex]);
    if (backdrop == null) return;
    hint(DISABLE_DEPTH_TEST);
    hint(DISABLE_DEPTH_MASK);
    noLights();
    float s = max(width / (float) backdrop.width, height / (float) backdrop.height);
    float w = backdrop.width * s, h = backdrop.height * s;
    image(backdrop, (width - w) / 2, (height - h) / 2, w, h);
    hint(ENABLE_DEPTH_MASK);
    hint(ENABLE_DEPTH_TEST);
  }

  void drawControls() {
    String back = backdropIndex < 0 ? "none" : new java.io.File(backdropPaths[backdropIndex]).getName();
    String pic = picturePaths.length == 0 ? "" : new java.io.File(picturePaths[textureNow()]).getName();
    String[][] rows = {
      { "drag", "push / pull a region (up = towards you)" },
      { "right drag", "orbit  (or shift + drag, or the arrow keys)" },
      { "wheel", "zoom" },
      { "e", "push and pull every region at random" },
      { "r", "flatten" },
      { "z  x", "less / more depth  (" + nf(depth, 0, 0) + ")" },
      { "[  ]", "sharper / rounder edges  (" + nf(rounding, 0, 1) + ")" },
      { "-  +", "lower / higher hills  (" + nf(bumpHeight, 0, 0) + ")" },
      { ",  .", "smaller / bigger hills  (" + nf(bumpSize, 0, 0) + ")" },
      { "n", "new hills" },
      { "t", "photo / region colours  (" + (textured ? "photo" : "colours") + ")" },
      { "p", "picture on the landscape  (" + pic + ")" },
      { "b", "change backdrop  (" + back + ")" },
      { "l", "shading on / off  (" + (shading ? "on" : "off") + ")" },
      { "space", "hide this menu" },
    };
    float x = 24, y = 24, lineH = 38, w = 760, h = rows.length * lineH + 70;
    noStroke();
    fill(0, 150);
    rect(x, y, w, h, 12);
    fill(255, 220);
    textSize(28);
    textAlign(LEFT, TOP);
    text(regions + " regions" + (grabbed >= 0 ? "     region " + grabbed + " at " + nf(regionHeight[grabbed], 0, 0) : ""), x + 20, y + 16);
    for (int i = 0; i < rows.length; i++) {
      float ry = y + 60 + i * lineH;
      textAlign(RIGHT, TOP);
      fill(120, 180, 240);
      text(rows[i][0], x + 200, ry);
      textAlign(LEFT, TOP);
      fill(255, 220);
      text(rows[i][1], x + 220, ry);
    }
  }


  // ------------------------------------------------------------ mouse and keys

  public void mousePressed() {
    boolean shift = keyPressed && keyCode == SHIFT;
    if (mouseButton == LEFT && !shift) {
      grabbed = regionAt(mouseX, mouseY);
      dragMode = grabbed >= 0 ? 2 : 1;
    } else {
      dragMode = 1;
    }
  }

  public void mouseDragged() {
    float dx = mouseX - pmouseX, dy = mouseY - pmouseY;
    if (dragMode == 1) {
      orbit(dx * 0.01, -dy * 0.01);
    } else if (dragMode == 2 && grabbed >= 0) {
      regionHeight[grabbed] -= dy / zoom;
      computeHeights();
      markRegionDirty(grabbed);
    }
  }

  public void mouseReleased() {
    dragMode = 0;
  }

  // turn the view: spin round the landscape's up direction, and tilt between looking straight
  // down and looking along the horizon (kept within that range so it can't flip over)
  void orbit(float dSpin, float dTilt) {
    spin += dSpin;
    tilt = constrain(tilt + dTilt, -0.3, 1.5);
  }

  public void mouseWheel(processing.event.MouseEvent e) {
    zoom *= pow(1.1, -e.getCount());
    zoom = constrain(zoom, 0.1, 20);
  }

  public void keyPressed() {
    if (key == CODED) {
      if (keyCode == LEFT) orbit(-0.05, 0);
      if (keyCode == RIGHT) orbit(0.05, 0);
      if (keyCode == UP) orbit(0, -0.05);
      if (keyCode == DOWN) orbit(0, 0.05);
      return;
    }
    if (labels == null) return;
    if (key == 'e') {
      for (int r = 0; r < regions; r++) regionHeight[r] = random(-depth, depth);
      computeHeights(); allDirty();
    }
    if (key == 'r') {
      java.util.Arrays.fill(regionHeight, 0);
      computeHeights(); allDirty();
    }
    if (key == 'z' || key == 'x') {
      float f = key == 'z' ? 1 / 1.3 : 1.3;
      depth *= f;
      for (int r = 0; r < regions; r++) regionHeight[r] *= f;   // the current landscape follows
      computeHeights(); allDirty();
    }
    if (key == '[' || key == ']') {
      rounding = key == '[' ? (rounding < 1.5 ? 0 : rounding / 1.4) : max(1, rounding * 1.4);
      rounding = min(rounding, 120);
      computeWeights(); computeHeights(); allDirty();
    }
    if (key == '-') { bumpHeight = max(0, bumpHeight - 3); computeBumps(); computeHeights(); allDirty(); }
    if (key == '+' || key == '=') { bumpHeight += 3; computeBumps(); computeHeights(); allDirty(); }
    if (key == ',') { bumpSize = max(5, bumpSize / 1.3); computeBumps(); computeHeights(); allDirty(); }
    if (key == '.') { bumpSize = min(800, bumpSize * 1.3); computeBumps(); computeHeights(); allDirty(); }
    if (key == 'n') { bumpSeed = random(1000); computeBumps(); computeHeights(); allDirty(); }
    if (key == 'p' && picturePaths.length > 0) {
      texturePicture = (textureNow() + 1) % picturePaths.length;
      photo = picture(texturePicture);
      allDirty();
    }
    if (key == 'b') {
      backdropIndex++;
      if (backdropIndex >= backdropPaths.length) backdropIndex = -1;
      backdrop = null;
    }
    if (key == 'l') shading = !shading;
    if (key == 't') { textured = !textured; allDirty(); }
    if (key == ' ') showControls = !showControls;
  }
}
