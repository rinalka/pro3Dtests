// Turning the regions into pieces for the 3D window.
//
// Each connected region becomes a Piece: its outline (and the outlines of any holes in it, where
// another region sits inside), its average colour, and where its middle is in the picture.
//
// Outlines are traced along the pixel edges, so they start as staircases; they're then simplified
// (Douglas-Peucker) so the stairs become clean diagonal lines, to within `tolerance` pixels.

class Piece {
  PVector[] outer;                                  // the outline, in picture pixels
  ArrayList<PVector[]> holes = new ArrayList<PVector[]>();
  int colour;
  float cx, cy;                                     // the middle of the piece in the picture
  int pixels;
}

ArrayList<Piece> makePieces(int[] label, PImage img, float tolerance, int minPixels) {
  int w = img.width, h = img.height, n = w * h;

  // 1. split regions into connected pieces (a region can be in several pieces after smoothing)
  int[] piece = new int[n];
  java.util.Arrays.fill(piece, -1);
  int[] queue = new int[n];
  int count = 0;
  for (int start = 0; start < n; start++) {
    if (piece[start] >= 0) continue;
    int head = 0, tail = 0;
    queue[tail++] = start;
    piece[start] = count;
    while (head < tail) {
      int i = queue[head++];
      int x = i % w, y = i / w;
      if (x > 0     && piece[i - 1] < 0 && label[i - 1] == label[start]) { piece[i - 1] = count; queue[tail++] = i - 1; }
      if (x + 1 < w && piece[i + 1] < 0 && label[i + 1] == label[start]) { piece[i + 1] = count; queue[tail++] = i + 1; }
      if (y > 0     && piece[i - w] < 0 && label[i - w] == label[start]) { piece[i - w] = count; queue[tail++] = i - w; }
      if (y + 1 < h && piece[i + w] < 0 && label[i + w] == label[start]) { piece[i + w] = count; queue[tail++] = i + w; }
    }
    count++;
  }

  // colour, size and middle of each piece
  long[][] sum = new long[count][6];   // r, g, b, x, y, pixels
  for (int i = 0; i < n; i++) {
    int c = img.pixels[i];
    long[] s = sum[piece[i]];
    s[0] += (c >> 16) & 0xFF; s[1] += (c >> 8) & 0xFF; s[2] += c & 0xFF;
    s[3] += i % w; s[4] += i / w; s[5]++;
  }
  Piece[] pieces = new Piece[count];
  for (int p = 0; p < count; p++) {
    Piece pc = new Piece();
    long cnt = sum[p][5];
    pc.pixels = (int) cnt;
    pc.colour = color(sum[p][0] / cnt, sum[p][1] / cnt, sum[p][2] / cnt);
    pc.cx = sum[p][3] / (float) cnt + 0.5;
    pc.cy = sum[p][4] / (float) cnt + 0.5;
    pieces[p] = pc;
  }

  // 2. the boundary edges, along pixel sides, going round each piece clockwise on screen.
  // Corners of pixels are numbered y * (w+1) + x
  int V = (w + 1) * (h + 1);
  IntList eFrom = new IntList(), eTo = new IntList(), ePiece = new IntList();
  for (int y = 0; y < h; y++) {
    for (int x = 0; x < w; x++) {
      int i = y * w + x, c = piece[i];
      int tl = y * (w + 1) + x, tr = tl + 1, bl = tl + (w + 1), br = bl + 1;
      if (y == 0     || piece[i - w] != c) { eFrom.append(tl); eTo.append(tr); ePiece.append(c); }  // top
      if (x == w - 1 || piece[i + 1] != c) { eFrom.append(tr); eTo.append(br); ePiece.append(c); }  // right
      if (y == h - 1 || piece[i + w] != c) { eFrom.append(br); eTo.append(bl); ePiece.append(c); }  // bottom
      if (x == 0     || piece[i - 1] != c) { eFrom.append(bl); eTo.append(tl); ePiece.append(c); }  // left
    }
  }
  int E = eFrom.size();
  // for each (piece, corner), the edges leaving it - at most two (where two pixels touch diagonally)
  java.util.HashMap<Long, int[]> leaving = new java.util.HashMap<Long, int[]>();
  for (int e = 0; e < E; e++) {
    long key = (long) ePiece.get(e) * V + eFrom.get(e);
    int[] slot = leaving.get(key);
    if (slot == null) leaving.put(key, new int[] { e, -1 });
    else slot[1] = e;
  }

  // 3. follow the edges round into closed loops
  boolean[] used = new boolean[E];
  for (int e0 = 0; e0 < E; e0++) {
    if (used[e0]) continue;
    int p = ePiece.get(e0);
    IntList loop = new IntList();
    int e = e0;
    while (true) {
      used[e] = true;
      loop.append(eFrom.get(e));
      int v = eTo.get(e);
      int[] slot = leaving.get((long) p * V + v);
      int next = pickNext(e, slot, eFrom, eTo, used, e0, w);
      if (next < 0 || next == e0) break;
      e = next;
    }
    if (loop.size() < 3) continue;

    PVector[] pts = simplifyLoop(loop, w, tolerance);
    if (pts == null) continue;
    if (signedArea(pts) > 0) pieces[p].outer = pts;   // clockwise on screen = the outside
    else pieces[p].holes.add(pts);
  }

  ArrayList<Piece> out = new ArrayList<Piece>();
  for (Piece pc : pieces) if (pc.outer != null && pc.pixels >= minPixels) out.add(pc);
  return out;
}

// Which edge to follow next out of a corner. Where two pixels of the piece touch only at a corner
// there are two ways out; turning right keeps hugging the same pixel, which keeps loops simple.
int pickNext(int e, int[] slot, IntList eFrom, IntList eTo, boolean[] used, int e0, int w) {
  if (slot == null) return -1;
  int dx = dirX(eFrom.get(e), eTo.get(e), w), dy = dirY(eFrom.get(e), eTo.get(e), w);
  int best = -1, bestRank = 9;
  for (int k = 0; k < 2; k++) {
    int c = slot[k];
    if (c < 0 || (used[c] && c != e0)) continue;
    int cx = dirX(eFrom.get(c), eTo.get(c), w), cy = dirY(eFrom.get(c), eTo.get(c), w);
    int rank;
    if (cx == -dy && cy == dx) rank = 0;        // right turn
    else if (cx == dx && cy == dy) rank = 1;    // straight on
    else rank = 2;                              // left turn
    if (rank < bestRank) { bestRank = rank; best = c; }
  }
  return best;
}
int dirX(int a, int b, int w) { return (b % (w + 1)) - (a % (w + 1)); }
int dirY(int a, int b, int w) { return (b / (w + 1)) - (a / (w + 1)); }

// corners of a traced loop -> simplified outline. Returns null if it shrinks to nothing
PVector[] simplifyLoop(IntList loop, int w, float tolerance) {
  int m = loop.size();
  float[] xs = new float[m], ys = new float[m];
  for (int i = 0; i < m; i++) {
    xs[i] = loop.get(i) % (w + 1);
    ys[i] = loop.get(i) / (w + 1);
  }
  // split the loop at its first point and the point farthest from it, and simplify each half
  int far = 0;
  float farD = -1;
  for (int i = 1; i < m; i++) {
    float d = sq(xs[i] - xs[0]) + sq(ys[i] - ys[0]);
    if (d > farD) { farD = d; far = i; }
  }
  boolean[] keep = new boolean[m + 1];
  float[] X = new float[m + 1], Y = new float[m + 1];
  for (int i = 0; i < m; i++) { X[i] = xs[i]; Y[i] = ys[i]; }
  X[m] = xs[0]; Y[m] = ys[0];   // the loop closed back to its start
  keep[0] = keep[far] = keep[m] = true;
  douglasPeucker(X, Y, 0, far, tolerance, keep);
  douglasPeucker(X, Y, far, m, tolerance, keep);

  ArrayList<PVector> pts = new ArrayList<PVector>();
  for (int i = 0; i < m; i++) if (keep[i]) pts.add(new PVector(X[i], Y[i]));
  if (pts.size() < 3) return null;
  return pts.toArray(new PVector[0]);
}

// keep the points between a and b that are more than tol from the straight line a-b
void douglasPeucker(float[] X, float[] Y, int a, int b, float tol, boolean[] keep) {
  if (b <= a + 1) return;
  float ax = X[a], ay = Y[a], bx = X[b], by = Y[b];
  float len = dist(ax, ay, bx, by);
  int worst = -1;
  float worstD = tol;
  for (int i = a + 1; i < b; i++) {
    float d = len > 0 ? abs((bx - ax) * (ay - Y[i]) - (ax - X[i]) * (by - ay)) / len : dist(X[i], Y[i], ax, ay);
    if (d > worstD) { worstD = d; worst = i; }
  }
  if (worst < 0) return;
  keep[worst] = true;
  douglasPeucker(X, Y, a, worst, tol, keep);
  douglasPeucker(X, Y, worst, b, tol, keep);
}

// positive for loops that go clockwise on screen (y pointing down)
float signedArea(PVector[] pts) {
  float s = 0;
  for (int i = 0; i < pts.length; i++) {
    PVector p = pts[i], q = pts[(i + 1) % pts.length];
    s += p.x * q.y - q.x * p.y;
  }
  return s / 2;
}
