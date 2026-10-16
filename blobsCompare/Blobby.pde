// A fine flat mesh over each piece, for the blobby 3D shapes (see World).
//
// The straight slabs only need corners along the outline. To bend a piece into a rounded,
// bumpy pebble it needs corners all over it, so each piece gets:
//   - its outlines (outside and holes) with extra points so no gap is longer than `spacing`
//   - an even scatter of points inside, `spacing` apart, nudged a little at random so they
//     don't sit on a perfect grid (which also keeps the triangulator happy)
//   - triangles joining them all (Delaunay, from Triangulate.java), keeping only the ones whose
//     middle is inside the piece
//   - for every point, how far it is from the nearest edge - the rounding and the bumps use that
//
// Points are relative to the piece's middle, like the slabs.

class Footprint {
  PVector[] pts;
  float[] edgeDist;     // distance from each point to the nearest outline
  int[][] tris;         // three point numbers per triangle
}

Footprint footprint(Piece pc, float spacing) {
  // the outlines, relative to the middle, with extra points along long sides
  ArrayList<PVector[]> loops = new ArrayList<PVector[]>();
  loops.add(densify(pc.outer, pc, spacing));
  for (PVector[] hole : pc.holes) loops.add(densify(hole, pc, spacing));

  ArrayList<PVector> pts = new ArrayList<PVector>();
  for (PVector[] loop : loops) for (PVector p : loop) pts.add(p);
  int rimCount = pts.size();

  // the points inside, on a nudged grid, not too close to an edge
  float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
  for (PVector p : loops.get(0)) {
    minX = min(minX, p.x); maxX = max(maxX, p.x);
    minY = min(minY, p.y); maxY = max(maxY, p.y);
  }
  java.util.Random rnd = new java.util.Random((long) (pc.cx * 1000 + pc.cy));
  for (float y = minY + spacing / 2; y < maxY; y += spacing) {
    for (float x = minX + spacing / 2; x < maxX; x += spacing) {
      float px = x + (rnd.nextFloat() - 0.5) * spacing * 0.4;
      float py = y + (rnd.nextFloat() - 0.5) * spacing * 0.4;
      if (!inside(px, py, loops)) continue;
      if (distToLoops(px, py, loops) < spacing * 0.5) continue;
      pts.add(new PVector(px, py));
    }
  }

  // triangulate. The triangulator sorts the list, so remember each point's number by identity
  java.util.IdentityHashMap<PVector, Integer> index = new java.util.IdentityHashMap<PVector, Integer>();
  for (int i = 0; i < pts.size(); i++) index.put(pts.get(i), i);
  ArrayList<PVector> sorted = new ArrayList<PVector>(pts);
  ArrayList<Triangle> tris = new ArrayList<Triangle>();
  Triangulator.triangulate(sorted, tris);

  ArrayList<int[]> keep = new ArrayList<int[]>();
  for (Triangle t : tris) {
    float mx = (t.p1.x + t.p2.x + t.p3.x) / 3, my = (t.p1.y + t.p2.y + t.p3.y) / 3;
    if (!inside(mx, my, loops)) continue;   // across a notch in the outline or over a hole
    keep.add(new int[] { index.get(t.p1), index.get(t.p2), index.get(t.p3) });
  }

  Footprint f = new Footprint();
  f.pts = pts.toArray(new PVector[0]);
  f.tris = keep.toArray(new int[0][]);
  f.edgeDist = new float[f.pts.length];
  for (int i = 0; i < f.pts.length; i++) {
    f.edgeDist[i] = i < rimCount ? 0 : distToLoops(f.pts[i].x, f.pts[i].y, loops);
  }
  return f;
}

// an outline relative to the piece's middle, with points added so no side is longer than spacing
PVector[] densify(PVector[] loop, Piece pc, float spacing) {
  ArrayList<PVector> out = new ArrayList<PVector>();
  for (int i = 0; i < loop.length; i++) {
    PVector a = loop[i], b = loop[(i + 1) % loop.length];
    int steps = max(1, ceil(dist(a.x, a.y, b.x, b.y) / spacing));
    for (int k = 0; k < steps; k++) {
      float t = k / (float) steps;
      out.add(new PVector(lerp(a.x, b.x, t) - pc.cx, lerp(a.y, b.y, t) - pc.cy));
    }
  }
  return out.toArray(new PVector[0]);
}

// inside the outside outline and not inside a hole (even-odd rule over all the outlines)
boolean inside(float x, float y, ArrayList<PVector[]> loops) {
  boolean in = false;
  for (PVector[] loop : loops) {
    for (int i = 0, j = loop.length - 1; i < loop.length; j = i++) {
      PVector a = loop[i], b = loop[j];
      if ((a.y > y) != (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) in = !in;
    }
  }
  return in;
}

// distance from a point to the nearest side of any outline
float distToLoops(float x, float y, ArrayList<PVector[]> loops) {
  float best = Float.MAX_VALUE;
  for (PVector[] loop : loops) {
    for (int i = 0; i < loop.length; i++) {
      PVector a = loop[i], b = loop[(i + 1) % loop.length];
      float dx = b.x - a.x, dy = b.y - a.y;
      float len2 = dx * dx + dy * dy;
      float t = len2 > 0 ? constrain(((x - a.x) * dx + (y - a.y) * dy) / len2, 0, 1) : 0;
      float ex = a.x + t * dx - x, ey = a.y + t * dy - y;
      best = min(best, ex * ex + ey * ey);
    }
  }
  return sqrt(best);
}
