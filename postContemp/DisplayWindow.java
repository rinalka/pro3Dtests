package postContemp;


import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class DisplayWindow {
	
	static int hold_displayW ;
	static int hold_displayH ;
	static int displayW;
	static int displayH;
	static int screen_locx;              // coords and size of the screen display
	static int screen_locy;

	PImage temp;     // temp storage for the part of the graphics buffer that will be displayed on the view panel
	static  float zoomPercent = (float) .05;
	static  float scrollPercent = (float) .05;
	
	DisplayWindow () {

		displayW = 1920; //(int) (GB.picWidth * GB.screensizeFactor);
		displayH = 1280; //(int) (GB.picHeight * GB.screensizeFactor);
		hold_displayW = displayW;
		hold_displayH = displayH;
		screen_locx = (GB.picWidth /2) - (displayW); //default the screen view window in the center of the picture
		screen_locy = (GB.picHeight /2) - (displayH);
	}
	
	 void update(PApplet p) {
	   // copy a chunk from myCanvas and display it on the screen
		 switch (CF.whichCanvas) {
		 case 0: //pic
			 temp = GB.thePicture.get(screen_locx,screen_locy,displayW,displayH);
			 break;
		 case 1: //alt pic
			 temp = GB.altPicture.get(screen_locx,screen_locy,displayW,displayH);
			 break;
		 case 2: //tempCanvas ---work
			 temp = GB.workCanvas.get(screen_locx,screen_locy,displayW,displayH);
			 break;
		 case 3: // contol --fmap
			 temp = GB.controlCanvas.get(screen_locx,screen_locy,displayW,displayH);
			 break;
		 case 4: //output
			 temp = GB.outputCanvas.get(screen_locx,screen_locy,displayW,displayH);
			 break;
		 case 5: // no canvas
			 break;
			 
		 }
	
	   if (CF.whichCanvas != 5) {// if it's any canvas but no canvas show it
		   p.image(temp,0,0,hold_displayW,hold_displayH);
		   p.removeCache(temp);
	   }
	}
	 
	 static void zoomPosition( int kc) {

		if (kc == 38) {screen_locy = (int) (screen_locy - (GB.picHeight*scrollPercent));}
	    if (kc == 40) {screen_locy = (int) (screen_locy + (GB.picHeight*scrollPercent));}
	    if (kc == 37) {screen_locx = (int) (screen_locx - (GB.picWidth*scrollPercent));}
	    if (kc == 39) {screen_locx = (int) (screen_locx + (GB.picWidth*scrollPercent));}
	    //just be sure we haven't scrolled off of the screen!
	    if (screen_locx < 0) {screen_locx = 0;}
	    if (screen_locy <0) {screen_locy = 0;}
	    if (screen_locx+displayW > GB.picWidth)  {screen_locx = GB.picWidth - displayW; }
	    if (screen_locy+displayH > GB.picHeight)  {screen_locy = GB.picHeight - displayH;}
	 }
	 
	 static void fullview() {
		 screen_locx = 0;
		 screen_locy = 0;
		 displayW = GB.picWidth;//view_panel_width;
		 displayH = GB.picHeight;//view_panel_height;	 
	 }
	 
	 static void halfview() {
		 screen_locx = ((GB.picWidth /4) - (hold_displayW/4)); //start the screen view window in the center of the picture
		 screen_locy = ((GB.picHeight /4)-  (hold_displayH/4));
		 displayW = GB.picWidth/2;
		 displayH = GB.picHeight/2;	 
	 }
	 
	 static void zoomview() {
		 screen_locx = ((GB.picWidth /2) - (hold_displayW/2)); //start the screen view window in the center of the picture
		 screen_locy = ((GB.picHeight /2)-  (hold_displayH/2));
		 displayW = hold_displayW;
		 displayH = hold_displayH;

	 }

	 static void zoomout() {
		 screen_locx = (int) (screen_locx - (GB.picWidth*zoomPercent));
		 if (screen_locx < 0 ) screen_locx = 0;
		 screen_locy = (int) (screen_locy - (GB.picHeight*zoomPercent));
		 if (screen_locy < 0 ) screen_locy = 0;
		 displayW = (int) (displayW + (GB.picWidth*zoomPercent));
		 if (displayW > GB.picWidth) displayW = GB.picWidth;
		 displayH = (int) (displayH + (GB.picHeight*zoomPercent));
		 if (displayH > GB.picHeight) displayH = GB.picHeight;
	 }
	 
	 static void zoomin() {
		 screen_locx = (int) (screen_locx + (GB.picWidth*zoomPercent));
//		 if (screen_locx > GB.picWidth/2 ) screen_locx = GB.picWidth/2;
		 screen_locy = (int) (screen_locy + (GB.picHeight*zoomPercent));
//		 if (screen_locy > GB.picHeight/2 ) screen_locy = GB.picWidth/2;
		 displayW = (int) (displayW - (GB.picWidth*zoomPercent));
//		 if (displayW < GB.picWidth) displayW = GB.picWidth;
		 displayH = (int) (displayH - (GB.picHeight*zoomPercent));
//		 if (displayH > GB.picHeight) displayH = GB.picHeight;
	 }
	 


	 static void canvasCopy(PApplet p) {
			CF2.runRB.activate(2);
			switch (CF.whichCopy) {
			case 0://pic to work
				GB.workCanvas.beginDraw();
					GB.workCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.workCanvas.image(GB.thePicture, 0, 0, GB.picWidth, GB.picHeight);
				GB.workCanvas.endDraw();
				p.removeCache(GB.thePicture);
				break;
			case 1://pic to output
				GB.outputCanvas.beginDraw();
					GB.outputCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.outputCanvas.image(GB.thePicture, 0, 0, GB.picWidth, GB.picHeight);  
				GB.outputCanvas.endDraw();
				p.removeCache(GB.thePicture);
				break;
			case 2://pic to control
				GB.controlCanvas.beginDraw();
					GB.controlCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.controlCanvas.image(GB.thePicture, 0, 0, GB.picWidth, GB.picHeight); 
				GB.controlCanvas.endDraw();
				p.removeCache(GB.thePicture);
				break;
			case 3://alt to control
				GB.controlCanvas.beginDraw();
					GB.controlCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.controlCanvas.image(GB.altPicture, 0, 0, GB.picWidth, GB.picHeight); 
				GB.controlCanvas.endDraw();
				p.removeCache(GB.altPicture);
				break;
			case 4://work to control
				GB.controlCanvas.beginDraw();
					GB.controlCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.controlCanvas.image(GB.workCanvas, 0, 0, GB.picWidth, GB.picHeight); 
				GB.controlCanvas.endDraw();
				p.removeCache(GB.workCanvas);
				break;
			case 5://work to output
				GB.outputCanvas.beginDraw();
					GB.outputCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.outputCanvas.image(GB.workCanvas, 0, 0, GB.picWidth, GB.picHeight); 
				GB.outputCanvas.endDraw();
				p.removeCache(GB.workCanvas);
				break;
			case 6://alt to work 
				GB.workCanvas.beginDraw();
					GB.workCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.workCanvas.image(GB.altPicture, 0, 0, GB.picWidth, GB.picHeight); 
				GB.workCanvas.endDraw();
				p.removeCache(GB.altPicture);
				break;
			case 7:
				GB.thePicture = p.loadImage(GB.thefile);
				break;
			case 8:
				GB.altPicture = p.loadImage(GB.thealtfile);
				break;
			case 9://control to output
				GB.outputCanvas.beginDraw();
					GB.outputCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.outputCanvas.image(GB.controlCanvas, 0, 0, GB.picWidth, GB.picHeight); 
				GB.outputCanvas.endDraw();
				p.removeCache(GB.controlCanvas);
				break;
			case 10://work to alt 
				GB.altPicture.loadPixels();
				for (int i=0; i<GB.altPicture.width*GB.altPicture.height; i++) {
						GB.altPicture.pixels[i] = GB.workCanvas.pixels[i]; 
				}
				GB.altPicture.updatePixels();
				break;
				
			case 11://pic to alt 
				GB.altPicture.loadPixels();
				for (int i=0; i<GB.altPicture.width*GB.altPicture.height; i++) {
						GB.altPicture.pixels[i] = GB.thePicture.pixels[i]; 
				}
				GB.altPicture.updatePixels();
				break;
			case 12://alt to output 
				GB.outputCanvas.beginDraw();
					GB.outputCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.outputCanvas.image(GB.altPicture, 0, 0, GB.picWidth, GB.picHeight); 
				GB.outputCanvas.endDraw();
				p.removeCache(GB.altPicture);
				break;
			case 13://control to work 
				GB.workCanvas.beginDraw();
					GB.workCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.workCanvas.image(GB.controlCanvas, 0, 0, GB.picWidth, GB.picHeight); 
				GB.workCanvas.endDraw();
				p.removeCache(GB.controlCanvas);
				break;
			case 14://output to work 
				GB.outputCanvas.beginDraw();
					GB.workCanvas.tint(CF.BtintR,CF.BtintG,CF.BtintB,CF.Balph);
					GB.workCanvas.image(GB.outputCanvas, 0, 0, GB.picWidth, GB.picHeight); 
				GB.outputCanvas.endDraw();
				p.removeCache(GB.outputCanvas);
				break;
			}
		}//end canvas Copy
	 
}
	 



