import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/*
  From PostContemp's EdgeDetector.java.
  Uses a Sobel / Scharr / "Rick" operator to find the significant points of a picture:
  every res pixels it measures how sharply the brightness changes, and keeps the point
  if that is more than threshold.

  Changed from PostContemp: the 3x3 loop now reads the pixel it is weighting (the original read
  each pixel one step late, so the first weight always got 0 and the rest were shifted by one).
*/
public class EdgeDetector {

  static final int[][][] OPERATOR = new int[][][] {
    { {2,  2,   0},
      {2,  0,  -2},
      {0, -2,  -2} },   // Sobel kernel

    { {6,  10,   0},
      {10,  0, -10},
      {0, -10,  -6} },  // Scharr kernel

    { {5,  3,   0},
      {3,  0,  -3},
      {0, -3,  -5} }    // Rick kernel
  };
  static final int SOBEL = 0, SCHARR = 1, RICK = 2;  // indexes of the kernels above

  // adds the significant points of img to vertices
  static void extractPoints(ArrayList<PVector> vertices, PImage img, int op, int threshold, int res) {
    img.loadPixels();
    int W = img.width - 1, H = img.height - 1;

    // every res pixels, except the borders
    for (int Y = 1; Y < H; Y += res) {
      for (int X = 1; X < W; X += res) {
        int colSum = 0;
        for (int y = -1; y <= 1; y++) {
          for (int x = -1; x <= 1; x++) {
            int col = img.pixels[(Y + y) * img.width + (X + x)];
            colSum += OPERATOR[op][x + 1][y + 1] * ((col >> 16 & 0xFF) + (col >> 8 & 0xFF) + (col & 0xFF));
          }
        }
        if (PApplet.abs(colSum) > threshold) {
          vertices.add(new PVector(X, Y));
        }
      }
    }
  }
}
