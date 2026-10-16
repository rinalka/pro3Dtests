import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/*
  Delaunay triangulation, from PostContemp's CircumCircle.java:
  Florian Jenett's port of Paul Bourke's triangulate.c
  (http://paulbourke.net/papers/triangulate/), refactored by someone else, then by Rick.

  Changed from PostContemp:
  - sharedVertex compared t1.p1 with t2.p2 twice and never with t2.p1, so some triangles
    touching the bounding "super triangle" were left in.
  - the duplicate-edge search read each edge one step late, so it could miss shared edges
    and leave overlapping triangles.
  - only the ArrayList version of triangulate is kept.

  The triangles hold the same PVector objects that were passed in, so a point shared by
  several triangles is one object - handy for building a joined mesh.
*/

class Triangle {
  PVector p1, p2, p3;

  Triangle(PVector p1, PVector p2, PVector p3) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
  }

  Triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
    this(new PVector(x1, y1), new PVector(x2, y2), new PVector(x3, y3));
  }
}


class CircumCircle {
  /*
    Is point p inside the circumcircle of triangle t?
    The circumcircle centre is returned in circle.x, circle.y and its radius in circle.z.
    A point on the edge counts as inside.
  */
  static boolean circumCircle(PVector p, Triangle t, PVector circle) {
    float m1, m2, mx1, mx2, my1, my2;
    float dx, dy, rsqr, drsqr;

    // coincident points
    if (PApplet.abs(t.p1.y - t.p2.y) < PConstants.EPSILON && PApplet.abs(t.p2.y - t.p3.y) < PConstants.EPSILON) {
      return false;
    }

    if (PApplet.abs(t.p2.y - t.p1.y) < PConstants.EPSILON) {
      m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y);
      mx2 = (t.p2.x + t.p3.x) / 2f;
      my2 = (t.p2.y + t.p3.y) / 2f;
      circle.x = (t.p2.x + t.p1.x) / 2f;
      circle.y = m2 * (circle.x - mx2) + my2;
    } else if (PApplet.abs(t.p3.y - t.p2.y) < PConstants.EPSILON) {
      m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y);
      mx1 = (t.p1.x + t.p2.x) / 2f;
      my1 = (t.p1.y + t.p2.y) / 2f;
      circle.x = (t.p3.x + t.p2.x) / 2f;
      circle.y = m1 * (circle.x - mx1) + my1;
    } else {
      m1 = -(t.p2.x - t.p1.x) / (t.p2.y - t.p1.y);
      m2 = -(t.p3.x - t.p2.x) / (t.p3.y - t.p2.y);
      mx1 = (t.p1.x + t.p2.x) / 2f;
      mx2 = (t.p2.x + t.p3.x) / 2f;
      my1 = (t.p1.y + t.p2.y) / 2f;
      my2 = (t.p2.y + t.p3.y) / 2f;
      circle.x = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
      circle.y = m1 * (circle.x - mx1) + my1;
    }

    dx = t.p2.x - circle.x;
    dy = t.p2.y - circle.y;
    rsqr = dx * dx + dy * dy;
    circle.z = PApplet.sqrt(rsqr);

    dx = p.x - circle.x;
    dy = p.y - circle.y;
    drsqr = dx * dx + dy * dy;

    return drsqr <= rsqr;
  }
}


class Triangulator {

  static class Edge {
    PVector p1, p2;
    Edge(PVector p1, PVector p2) {
      this.p1 = p1;
      this.p2 = p2;
    }
  }

  static boolean sharedVertex(Triangle t1, Triangle t2) {
    return t1.p1 == t2.p1 || t1.p1 == t2.p2 || t1.p1 == t2.p3 ||
           t1.p2 == t2.p1 || t1.p2 == t2.p2 || t1.p2 == t2.p3 ||
           t1.p3 == t2.p1 || t1.p3 == t2.p2 || t1.p3 == t2.p3;
  }

  /*
    Triangulates the points in pxyz (which gets sorted by x) and adds the triangles to triangles.
    The points must not contain duplicates.
  */
  static void triangulate(ArrayList<PVector> pxyz, ArrayList<Triangle> triangles) {
    Collections.sort(pxyz, new Comparator<PVector>() {
      public int compare(PVector a, PVector b) {
        return Float.compare(a.x, b.x);
      }
    });

    // bounds, for the super triangle
    float xmin = pxyz.get(0).x, ymin = pxyz.get(0).y, xmax = xmin, ymax = ymin;
    for (PVector p : pxyz) {
      if (p.x < xmin) xmin = p.x;
      if (p.x > xmax) xmax = p.x;
      if (p.y < ymin) ymin = p.y;
      if (p.y > ymax) ymax = p.y;
    }
    float dx = xmax - xmin, dy = ymax - ymin;
    float dmax = dx > dy ? dx : dy;
    float two_dmax = dmax * 2;
    float xmid = (xmax + xmin) * .5f, ymid = (ymax + ymin) * .5f;

    HashSet<Triangle> complete = new HashSet<Triangle>();  // triangles no later point can affect

    // the super triangle contains every point; it is removed at the end
    Triangle superTriangle = new Triangle(xmid - two_dmax, ymid - dmax, xmid, ymid + two_dmax, xmid + two_dmax, ymid - dmax);
    triangles.add(superTriangle);

    ArrayList<Edge> edges = new ArrayList<Edge>();
    PVector circle = new PVector();

    // add the points one at a time
    for (PVector p : pxyz) {
      edges.clear();

      // every triangle whose circumcircle contains p is removed, keeping its edges
      for (int j = triangles.size() - 1; j >= 0; j--) {
        Triangle t = triangles.get(j);
        if (complete.contains(t)) continue;
        boolean inside = CircumCircle.circumCircle(p, t, circle);
        if (circle.x + circle.z < p.x) complete.add(t);
        if (inside) {
          edges.add(new Edge(t.p1, t.p2));
          edges.add(new Edge(t.p2, t.p3));
          edges.add(new Edge(t.p3, t.p1));
          triangles.remove(j);
        }
      }

      // edges shared by two removed triangles are inside the hole - drop them
      for (int j = 0; j < edges.size() - 1; j++) {
        Edge e1 = edges.get(j);
        for (int k = j + 1; k < edges.size(); k++) {
          Edge e2 = edges.get(k);
          if (e1.p1 != null && e1.p1 == e2.p2 && e1.p2 == e2.p1) {
            e1.p1 = e1.p2 = e2.p1 = e2.p2 = null;
          }
        }
      }

      // fill the hole with triangles from its outline to p
      for (Edge e : edges) {
        if (e.p1 == null || e.p2 == null) continue;
        triangles.add(new Triangle(e.p1, e.p2, p));
      }
    }

    // remove anything still attached to the super triangle
    for (int i = triangles.size() - 1; i >= 0; i--) {
      if (sharedVertex(triangles.get(i), superTriangle)) triangles.remove(i);
    }
  }
}
