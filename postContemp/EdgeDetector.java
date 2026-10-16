package postContemp;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class EdgeDetector {
	/*
	  An algorithm that uses a custom implementation of a Sobel/Scharr operator to get the significant points of a picture.
	*/
	
	    static final int [][][] OPERATOR  = new int[][][]  {
	        { {2,  2,   0}, 
	          {2,  0,  -2}, 
	          {0, -2,  -2}},  //Sobel kernel
	          
	        { {6,  10,   0}, 
	          {10,  0, -10}, 
	          {0, -10,  -6}},   //Scharr kernel
	          
	        { {5,  3,   0}, 
		      {3,  0, -3}, 
		      {0, -3,  -5}}   //Rick kernel

	    };
	    static final int SOBEL = 0, SCHARR = 1, RICK = 2;  //Indexes of the kernels in the previous array
	     
	    //This method add significant points of the given picture to a given list
	    static void extractPoints(ArrayList <PVector> vertices, PImage img, int op, int treshold, int res)
	    {    
	          int col = 0, colSum = 0, W = img.width-1, H = img.height-1;
	             
	          //For any pixel in the image excepting borders           
	          for (int Y = 1; Y < H; Y += res) {//outer1
	        	  for (int X = 1; X < W; X += res, colSum = 0) {//outer2
		              //Convolute surrounding pixels with desired operator      
		              for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++, col = img.get((X+x), (Y+y)))
		                  colSum += OPERATOR[op][x+1][y+1] * ((col>>16 & 0xFF)+(col>>8 & 0xFF)+(col & 0xFF));              
		              //And if the resulting sum is over the threshold add pixel position to the list
		              if (PApplet.abs(colSum) > treshold) {
		            	  vertices.add(new PVector(X, Y));    
		              }
		          }//outer2 
	          }//outer1
	          
	     } //end extractPoints  
	}// EdgeDetector
