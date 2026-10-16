// The 3D window. Each Piece becomes a solid slab - its outline extruded to `thickness` - placed
// where it was in the picture, and covered with the part of the photo it came from (the top and
// bottom show the photo; the walls show the photo's colours along the edge, stretched down). Click a piece and drag to move it; shift-drag moves it towards or
// away from you. Drag empty space to orbit, mouse wheel to zoom.
//   r  put every piece back where it started
//   e  scatter the pieces in depth
//   [ ]  thinner / thicker
//   t  photo / flat colour
//   p  which picture is on the pieces - the shapes stay cut from one picture, the surface can come
//      from another (each piece shows whatever was at the same place in that picture)
//   b  change the backdrop (each picture in turn, then none)
//   l  shading on / off - off shows every surface in the photo's own colours, evenly lit
//   k  blobby / straight. Blobby pieces are rounded pebbles: the top and bottom curve down to
//      meet at the outline, and Perlin noise gives them hills and valleys (see the Blobby tab)
//   - +  less / more bumpiness      , .  smaller / bigger bumps      n  new bumps
//   space  show / hide the controls panel
//
// The backdrop is one of the pictures drawn flat behind everything - it doesn't move with the
// camera and the pieces can't go behind it.
//
// Picking works by drawing the scene again off screen with every piece in its own flat colour,
// then reading the colour under the mouse. It uses a plain (untextured) copy of every piece so
// the photo can't get mixed into the picking colours.
//
// Closing the window only hides it; Make 3D in the main window shows it again with new pieces.

class World extends PApplet {
  float thickness = 30;             // how thick the pieces are, in picture pixels

  ArrayList<Piece> incoming;        // new pieces from the main window, built on this window's next frame
  float picW, picH;                 // the size of the picture the pieces came from
  PImage photo;                     // the full size picture on the pieces
  String[] picturePaths = new String[0];   // every picture in the main window's data folder
  int shapesPicture = 0;            // the picture the shapes were cut from
  int texturePicture = -1;          // the picture on the pieces, or -1 for the same as the shapes
  java.util.HashMap<Integer, PImage> loaded = new java.util.HashMap<Integer, PImage>();
  boolean textured = true;          // photo on the pieces, or their flat colour
  boolean shading = false;          // light and shade the pieces, or show every surface evenly
  boolean showControls = true;      // the controls panel at the top left (space hides it)
  boolean blobby = true;            // rounded, bumpy pebbles, or straight slabs
  float bumpiness = 1.0;            // how high the hills are, as a share of the thickness
  float bumpSize = 40;              // roughly how wide a hill is, in picture pixels
  float bumpSeed = 0;               // which noise - n picks another
  Footprint[] feet;                 // the fine flat mesh of each piece, worked out once per set of pieces
  ArrayList<Piece> pieces = new ArrayList<Piece>();
  PShape[] shapes = new PShape[0];  // one solid per piece, built around the piece's middle
  PShape[] plain = new PShape[0];   // the same solids without the photo, for picking
  PVector[] home = new PVector[0];  // where each piece started
  PVector[] pos = new PVector[0];   // where each piece is now
  int selected = -1;

  float rotX = 0, rotY = 0, zoom = 1.4;
  int dragMode = 0;                 // 0 nothing, 1 orbiting, 2 moving a piece
  PGraphics pick;
  boolean hidden = false;
  String[] backdropPaths = new String[0];   // the pictures that can be the backdrop
  int backdropIndex = -1;                   // which one, or -1 for none
  PImage backdrop;

  public void settings() {
    size(2400, 1600, P3D);
  }

  public void setup() {
    surface.setTitle("blobs in 3D");
    surface.setLocation(160, 160);
    pick = createGraphics(width, height, P3D);
    pick.noSmooth();   // smoothing would blend the picking colours at the edges
  }

  // called from the main window
  void show(ArrayList<Piece> p, float w, float h, String[] paths, int fromPicture) {
    picW = w;
    picH = h;
    picturePaths = paths;
    shapesPicture = fromPicture;
    incoming = p;
    if (hidden) {
      hidden = false;
      surface.setVisible(true);
      loop();
    }
  }

  // called from the main window when it first opens this one
  void setBackdrops(String[] paths, int start) {
    backdropPaths = paths;
    backdropIndex = start;
    backdrop = null;   // loaded on the next frame
  }

  // closing the window just hides it, instead of quitting everything
  public void exit() {
    hidden = true;
    surface.setVisible(false);
    noLoop();
  }

  public void draw() {
    if (incoming != null) {
      photo = picture(textureNow());
      build(incoming);
      incoming = null;
    }
    background(25);
    drawBackdrop();

    pushMatrix();
    placeCamera(g);
    useLighting();
    noStroke();
    for (int i = 0; i < shapes.length; i++) {
      pushMatrix();
      translate(pos[i].x, pos[i].y, pos[i].z);
      shape(shapes[i]);
      if (i == selected) outline(pieces.get(i));
      popMatrix();
    }
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

  // the backdrop picture, filling the window behind everything. It's drawn without the camera
  // and without writing depth, so it stays put and nothing can go behind it
  void drawBackdrop() {
    if (backdropIndex < 0 || backdropIndex >= backdropPaths.length) return;
    if (backdrop == null) backdrop = loadImage(backdropPaths[backdropIndex]);
    if (backdrop == null) return;
    hint(DISABLE_DEPTH_TEST);
    hint(DISABLE_DEPTH_MASK);
    noLights();
    // cover the window, cropping whatever doesn't fit
    float s = max(width / (float) backdrop.width, height / (float) backdrop.height);
    float w = backdrop.width * s, h = backdrop.height * s;
    image(backdrop, (width - w) / 2, (height - h) / 2, w, h);
    hint(ENABLE_DEPTH_MASK);
    hint(ENABLE_DEPTH_TEST);
  }

  // the picture on the pieces: the one picked with p, or the one the shapes came from
  int textureNow() {
    return texturePicture >= 0 ? texturePicture : shapesPicture;
  }

  // a full size picture, loaded the first time it's needed
  PImage picture(int i) {
    PImage img = loaded.get(i);
    if (img == null) {
      img = loadImage(picturePaths[i]);
      loaded.put(i, img);
    }
    return img;
  }

  // shaded, or every surface the same brightness (the photo's own colours)
  void useLighting() {
    if (shading) lights();
    else noLights();
  }

  // the controls, in a box at the top left
  void drawControls() {
    String back = backdropIndex < 0 ? "none" : new java.io.File(backdropPaths[backdropIndex]).getName();
    String[][] rows = {
      { "click + drag", "move a piece" },
      { "shift + drag", "move it towards / away" },
      { "drag empty", "orbit the camera" },
      { "wheel", "zoom" },
      { "r", "put every piece back" },
      { "e", "scatter the pieces in depth" },
      { "[  ]", "thinner / thicker  (" + nf(thickness, 0, 0) + ")" },
      { "t", "photo / flat colour  (" + (textured ? "photo" : "flat") + ")" },
      { "p", "picture on the pieces  (" + new java.io.File(picturePaths[textureNow()]).getName() + ")" },
      { "b", "change backdrop  (" + back + ")" },
      { "l", "shading on / off  (" + (shading ? "on" : "off") + ")" },
      { "k", "blobby / straight  (" + (blobby ? "blobby" : "straight") + ")" },
      { "-  +", "bumpiness  (" + nf(bumpiness, 0, 2) + ")" },
      { ",  .", "bump size  (" + nf(bumpSize, 0, 0) + ")" },
      { "n", "new bumps" },
      { "space", "hide this menu" },
    };
    float x = 24, y = 24, lineH = 38, w = 640, h = rows.length * lineH + 70;
    noStroke();
    fill(0, 150);
    rect(x, y, w, h, 12);
    fill(255, 220);
    textSize(28);
    textAlign(LEFT, TOP);
    text(shapes.length + " pieces", x + 20, y + 16);
    for (int i = 0; i < rows.length; i++) {
      float ry = y + 60 + i * lineH;
      textAlign(RIGHT, TOP);
      fill(120, 180, 240);
      text(rows[i][0], x + 230, ry);
      textAlign(LEFT, TOP);
      fill(255, 220);
      text(rows[i][1], x + 250, ry);
    }
  }

  // the same view for the screen and for the picking buffer
  void placeCamera(PGraphics pg) {
    pg.translate(width / 2, height / 2, 0);
    pg.rotateX(rotX);
    pg.rotateY(rotY);
    pg.scale(zoom);
  }


  // ------------------------------------------------------------ building the solids

  void build(ArrayList<Piece> p) {
    int t0 = millis();
    if (p != pieces) feet = null;   // new pieces need new footprints
    pieces = p;
    if (blobby && feet == null) {
      feet = new Footprint[p.size()];
      for (int i = 0; i < p.size(); i++) {
        Piece pc = p.get(i);
        // finer for small pieces, coarser for big ones, so no piece gets too many points
        float spacing = max(3, sqrt(pc.pixels / 1500.0));
        feet[i] = footprint(pc, spacing);
      }
      println("3D: fine meshes made in " + (millis() - t0) + " ms");
    }
    shapes = new PShape[p.size()];
    plain = new PShape[p.size()];
    home = new PVector[p.size()];
    pos = new PVector[p.size()];
    for (int i = 0; i < p.size(); i++) {
      Piece pc = p.get(i);
      plain[i] = blobby ? pebble(pc, feet[i], false) : solid(pc, false);
      if (textured && photo != null) shapes[i] = blobby ? pebble(pc, feet[i], true) : solid(pc, true);
      else shapes[i] = plain[i];
      home[i] = new PVector(pc.cx - picW / 2, pc.cy - picH / 2, 0);
      pos[i] = home[i].copy();
    }
    selected = -1;
    println("3D: " + shapes.length + " pieces built in " + (millis() - t0) + " ms");
  }

  // a piece's outline, extruded: a top, a bottom, and walls round the outside and round every hole.
  // Processing does the hard part - filling an outline with holes in it - and getTessellation
  // hands back the triangles
  PShape solid(Piece pc, boolean withPhoto) {
    PShape flat = createShape();
    flat.beginShape();
    flat.noStroke();
    for (PVector v : pc.outer) flat.vertex(v.x - pc.cx, v.y - pc.cy);
    for (PVector[] hole : pc.holes) {
      flat.beginContour();
      for (PVector v : hole) flat.vertex(v.x - pc.cx, v.y - pc.cy);
      flat.endContour();
    }
    flat.endShape(CLOSE);
    PShape tris = flat.getTessellation();

    float t = thickness / 2;
    PShape s = createShape();
    s.beginShape(TRIANGLES);
    s.noStroke();
    if (withPhoto) {
      s.textureMode(NORMAL);
      s.texture(photo);
      s.fill(255);   // so the photo shows in its own colours
    } else {
      s.fill(pc.colour);
    }
    // top (facing the camera) and bottom
    int n = tris.getVertexCount();
    s.normal(0, 0, 1);
    for (int k = 0; k + 2 < n; k += 3) {
      for (int j = 0; j < 3; j++) { PVector v = tris.getVertex(k + j); corner(s, pc, v.x, v.y, t, withPhoto); }
    }
    s.normal(0, 0, -1);
    for (int k = 0; k + 2 < n; k += 3) {
      for (int j = 2; j >= 0; j--) { PVector v = tris.getVertex(k + j); corner(s, pc, v.x, v.y, -t, withPhoto); }
    }
    // the walls
    wall(s, pc.outer, pc, t, withPhoto);
    for (PVector[] hole : pc.holes) wall(s, hole, pc, t, withPhoto);
    s.endShape();
    return s;
  }

  // one corner. x, y are relative to the piece's middle; the texture position is where that
  // point was in the picture, so each piece shows the photo that was under it
  void corner(PShape s, Piece pc, float x, float y, float z, boolean withPhoto) {
    if (withPhoto) s.vertex(x, y, z, (x + pc.cx) / picW, (y + pc.cy) / picH);
    else s.vertex(x, y, z);
  }

  // A blobby piece: the fine mesh bent into a pebble. The height rises from 0 at the outline along
  // a quarter circle, reaching half the thickness `thickness` pixels in from the edge, so the rim
  // is rounded and the top and bottom meet at the outline. Perlin noise then adds hills and
  // valleys - faded out towards the rim so the outline stays put. The noise is sampled at the
  // point's place in the picture, so neighbouring pieces get matching bumps where they meet.
  PShape pebble(Piece pc, Footprint f, boolean withPhoto) {
    int n = f.pts.length;
    float half = thickness / 2;
    float round = max(thickness, 2);
    float[] top = new float[n], bottom = new float[n];
    for (int i = 0; i < n; i++) {
      float s = min(f.edgeDist[i] / round, 1);
      float dome = half * sqrt(1 - (1 - s) * (1 - s));
      float fade = s * s * (3 - 2 * s);
      float nx = (f.pts[i].x + pc.cx) / bumpSize, ny = (f.pts[i].y + pc.cy) / bumpSize;
      float upTop = (noise(nx, ny, bumpSeed) - 0.5) * 2;
      float upBottom = (noise(nx, ny, bumpSeed + 17.3) - 0.5) * 2;
      top[i] = max(0, dome + bumpiness * thickness * upTop * fade);
      bottom[i] = -max(0, dome + bumpiness * thickness * upBottom * fade);
    }
    PVector[] nTop = smoothNormals(f, top, 1), nBottom = smoothNormals(f, bottom, -1);

    PShape s = createShape();
    s.beginShape(TRIANGLES);
    s.noStroke();
    if (withPhoto) {
      s.textureMode(NORMAL);
      s.texture(photo);
      s.fill(255);
    } else {
      s.fill(pc.colour);
    }
    for (int[] t : f.tris) {
      for (int k = 0; k < 3; k++) {
        int i = t[k];
        s.normal(nTop[i].x, nTop[i].y, nTop[i].z);
        corner(s, pc, f.pts[i].x, f.pts[i].y, top[i], withPhoto);
      }
      for (int k = 2; k >= 0; k--) {
        int i = t[k];
        s.normal(nBottom[i].x, nBottom[i].y, nBottom[i].z);
        corner(s, pc, f.pts[i].x, f.pts[i].y, bottom[i], withPhoto);
      }
    }
    s.endShape();
    return s;
  }

  // a normal at every point, averaged from the triangles round it, so the surface shades smoothly.
  // side is 1 for the top (normals face the camera) and -1 for the bottom
  PVector[] smoothNormals(Footprint f, float[] z, int side) {
    PVector[] nrm = new PVector[f.pts.length];
    for (int i = 0; i < nrm.length; i++) nrm[i] = new PVector();
    for (int[] t : f.tris) {
      PVector a = new PVector(f.pts[t[0]].x, f.pts[t[0]].y, z[t[0]]);
      PVector b = new PVector(f.pts[t[1]].x, f.pts[t[1]].y, z[t[1]]);
      PVector c = new PVector(f.pts[t[2]].x, f.pts[t[2]].y, z[t[2]]);
      PVector fn = PVector.sub(b, a).cross(PVector.sub(c, a));
      if (fn.z * side < 0) fn.mult(-1);
      for (int k = 0; k < 3; k++) nrm[t[k]].add(fn);
    }
    for (PVector v : nrm) {
      if (v.magSq() == 0) v.set(0, 0, side);
      v.normalize();
    }
    return nrm;
  }

  // a band of rectangles round one outline. The outlines go round so that (dy, -dx) points out of the piece
  void wall(PShape s, PVector[] loop, Piece pc, float t, boolean withPhoto) {
    for (int i = 0; i < loop.length; i++) {
      PVector a = loop[i], b = loop[(i + 1) % loop.length];
      float ax = a.x - pc.cx, ay = a.y - pc.cy, bx = b.x - pc.cx, by = b.y - pc.cy;
      float dx = bx - ax, dy = by - ay, len = sqrt(dx * dx + dy * dy);
      if (len == 0) continue;
      s.normal(dy / len, -dx / len, 0);
      corner(s, pc, ax, ay, t, withPhoto);  corner(s, pc, bx, by, t, withPhoto);  corner(s, pc, bx, by, -t, withPhoto);
      corner(s, pc, ax, ay, t, withPhoto);  corner(s, pc, bx, by, -t, withPhoto); corner(s, pc, ax, ay, -t, withPhoto);
    }
  }


  // ------------------------------------------------------------ picking and moving

  // which piece is under the mouse, or -1
  int pieceAt(int mx, int my) {
    pick.beginDraw();
    pick.background(0);
    pick.noLights();
    pick.noStroke();
    placeCamera(pick);
    for (int i = 0; i < shapes.length; i++) {
      pick.pushMatrix();
      pick.translate(pos[i].x, pos[i].y, pos[i].z);
      plain[i].disableStyle();
      pick.fill(0xFF000000 | (i + 1));   // each piece gets its own colour: its number + 1
      pick.shape(plain[i]);
      plain[i].enableStyle();
      pick.popMatrix();
    }
    pick.endDraw();
    int c = pick.get(mx, my) & 0xFFFFFF;
    return c - 1;
  }

  public void mousePressed() {
    int hit = pieceAt(mouseX, mouseY);
    if (hit >= 0 && hit < shapes.length) {
      selected = hit;
      dragMode = 2;
    } else {
      selected = -1;
      dragMode = 1;
    }
  }

  public void mouseDragged() {
    float dx = mouseX - pmouseX, dy = mouseY - pmouseY;
    if (dragMode == 1) {
      rotY += dx * 0.01;
      rotX -= dy * 0.01;
    } else if (dragMode == 2 && selected >= 0) {
      // turn the mouse movement on screen into a movement in the world, undoing the camera's turn
      boolean depth = keyPressed && keyCode == SHIFT;
      PVector screenMove = depth ? new PVector(0, 0, dy) : new PVector(dx, dy, 0);
      screenMove.div(zoom);
      PMatrix3D undo = new PMatrix3D();
      undo.rotateY(-rotY);
      undo.rotateX(-rotX);
      PVector worldMove = new PVector();
      undo.mult(screenMove, worldMove);
      pos[selected].add(worldMove);
    }
  }

  public void mouseReleased() {
    dragMode = 0;
  }

  public void mouseWheel(processing.event.MouseEvent e) {
    zoom *= pow(1.1, -e.getCount());
    zoom = constrain(zoom, 0.1, 20);
  }

  public void keyPressed() {
    if (key == 'r') for (int i = 0; i < pos.length; i++) pos[i] = home[i].copy();
    if (key == 'e') for (int i = 0; i < pos.length; i++) pos[i].z = home[i].z + random(-picW / 3, picW / 3);
    if (key == '[') { thickness = max(1, thickness / 1.5); rebuildKeepingPlaces(); }
    if (key == ']') { thickness = min(200, thickness * 1.5); rebuildKeepingPlaces(); }
    if (key == 't') { textured = !textured; rebuildKeepingPlaces(); }
    if (key == ' ') showControls = !showControls;
    if (key == 'l') shading = !shading;
    if (key == 'p' && picturePaths.length > 0) {
      texturePicture = (textureNow() + 1) % picturePaths.length;
      photo = picture(texturePicture);
      if (textured) rebuildKeepingPlaces();
    }
    if (key == 'k') { blobby = !blobby; rebuildKeepingPlaces(); }
    if (key == '-') { bumpiness = max(0, bumpiness - 0.1); if (blobby) rebuildKeepingPlaces(); }
    if (key == '+' || key == '=') { bumpiness = min(3, bumpiness + 0.1); if (blobby) rebuildKeepingPlaces(); }
    if (key == ',') { bumpSize = max(5, bumpSize / 1.3); if (blobby) rebuildKeepingPlaces(); }
    if (key == '.') { bumpSize = min(400, bumpSize * 1.3); if (blobby) rebuildKeepingPlaces(); }
    if (key == 'n') { bumpSeed = random(1000); if (blobby) rebuildKeepingPlaces(); }
    if (key == 'b') {
      // the next picture, then none, then round again
      backdropIndex++;
      if (backdropIndex >= backdropPaths.length) backdropIndex = -1;
      backdrop = null;
    }
  }

  // a white line round the top of the selected piece
  void outline(Piece pc) {
    pushStyle();
    noLights();
    noFill();
    stroke(255);
    strokeWeight(4);
    float z = blobby ? 0 : thickness / 2 + 0.5;   // pebbles: the rim, where top and bottom meet
    beginShape();
    for (PVector v : pc.outer) vertex(v.x - pc.cx, v.y - pc.cy, z);
    endShape(CLOSE);
    popStyle();
    useLighting();
  }

  // new thickness or texture: rebuild the solids but leave the pieces where they've been moved to
  void rebuildKeepingPlaces() {
    PVector[] keep = pos;
    int sel = selected;
    build(pieces);
    if (keep.length == pos.length) pos = keep;
    selected = sel;
  }
}
