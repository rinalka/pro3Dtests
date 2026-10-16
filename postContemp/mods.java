package postContemp;

import processing.core.PApplet;
import processing.core.PImage;

public class mods {

	mods () {
	}
	 static void solidColor(PApplet p) {//pause the strokes, copy rgba as set in CF with the color picer -  over the appropriate screen,
			CF2.runRB.activate(2);
			switch (CF.whichCanvas) {
			 case 0: //pic*****************************************************************nice effect - but not proper color mixing
				 	GB.thePicture.loadPixels();
				 	for (int x = 0; x < GB.thePicture.width; x++) {
				 		  for (int y = 0; y < GB.thePicture.height; y++ ) {
				 		    // Calculate the 1D pixel location
				 		    int loc = x + y*GB.thePicture.width;
				 		    // Get the R,G,B values from image
				 		    float r = p.red   (GB.thePicture.pixels[loc]);
				 		    float g = p.green (GB.thePicture.pixels[loc]);
				 		    float b = p.blue  (GB.thePicture.pixels[loc]);
				 		    r = r + (CF.BtintR*CF.Balph/255);
				 		    g = g + (CF.BtintG*CF.Balph/255);
				 		    b = b + (CF.BtintB*CF.Balph/255);
				 		    // Constrain RGB to between 0-255
				 		    r = p.constrain(r,0,255);
				 		    g = p.constrain(g,0,255);
				 		    b = p.constrain(b,0,255);
				 		    // Make a new color and set pixel in the window
				 		    int  c = p.color(r,g,b);
				 		    GB.thePicture.pixels[loc] = c;
				 		  }
				 		}
				 	GB.thePicture.updatePixels();
				 break;
			 case 1: //alt

				 break;
			 case 2: //work
					GB.workCanvas.fill(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
		  		  	GB.workCanvas.rect( 0, 0, GB.picWidth, GB.picHeight);  
				 break;
			 case 3: //control
					GB.controlCanvas.fill(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
		  		  	GB.controlCanvas.rect( 0, 0, GB.picWidth, GB.picHeight); 
				 break;
			 case 4: //output
					GB.outputCanvas.fill(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
		  		  	GB.outputCanvas.rect( 0, 0, GB.picWidth, GB.picHeight);  //or a color
				 break;
			}
			GB.workCanvas.noFill();
		}//end solidColor

	
	 static void threshH(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.THRESHOLD,CF.threshVal); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.THRESHOLD,CF.threshVal); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.THRESHOLD,CF.threshVal); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.THRESHOLD,CF.threshVal); 
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.THRESHOLD,CF.threshVal); 
			 break;
		 }
	 }

	 static void erode(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.ERODE); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.ERODE); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.ERODE); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.ERODE);  
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.ERODE);  
			 break;
		 }
	 }
	 
	 static void dilate(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.DILATE); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.DILATE); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.DILATE); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.DILATE);  
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.DILATE);  
			 break;
		 }
	 }
	 
		 
	 static void blur(PApplet p) {
		 GB.waitingforsomething = true;
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.BLUR, CF.blurVal); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.BLUR, CF.blurVal); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.BLUR, CF.blurVal); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.BLUR, CF.blurVal);  
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.BLUR, CF.blurVal);  
			 break;
		 }
		 GB.waitingforsomething = false;
	 }
	 
	 
	 static void posterize(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.POSTERIZE, CF.postVal); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.POSTERIZE, CF.postVal); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.POSTERIZE, CF.postVal); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.POSTERIZE, CF.postVal);  
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.POSTERIZE, CF.postVal);  
			 break;
		 }
	 }
	
	 static void invert(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 GB.thePicture.filter(p.INVERT); 
			 break;
		 case 1: //alt
			 GB.altPicture.filter(p.INVERT); 
			 break;
		 case 2: //work
			 GB.workCanvas.filter(p.INVERT); 
			 break;
		 case 3: //control
			 GB.controlCanvas.filter(p.INVERT);  
			 break;
		 case 4: //output
			 GB.outputCanvas.filter(p.INVERT);  
			 break;
		 }
	 }
	
}
