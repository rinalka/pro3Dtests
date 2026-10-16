package postContemp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import jogamp.opengl.x11.glx.GLX;
import controlP5.*;
import sojamo.drop.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class PostContemp extends PApplet {
	// set up where most the globals are!  i know, i know.  
	//Notes: globals set on the control panels are in CF.java or CF2.java 
	// GB.java has all the other globals (like picture  and working buffers.... and sizes) 
	GB myglobals;
	CF cf1;
	CF2 cf2;
	static Frame f;
	static Frame f2;
	//Drag and drop objects and  file name
	SDrop drop;
	static String pictureUrl;
  	//the display is a screen view into GB.myCanvas, when GB.myCanvas is bigger than theDisplay, 
    //		controls for pan and zoom are keyboard driven (see GB.externalInput())
    //		the % of myCanvas shown in the Display is set with GB.screensizeFactor
	DisplayWindow theDisplay;  
	//used to control the speed of stroke creation - and allow killstrokes to do so gracefully
	int maxNewStrokes = 10;
	
	public void setup() {
		//setup the drag and drop for files
		drop = new SDrop(this);
		int time = millis()/1000;
		//setup all the GB global variables
		println ( "before globals " + ((millis()/1000) - time));
		myglobals = new GB(this);	
		println ( "after globals " + ((millis()/1000) - time));
		// set up the display window - which is probably smaller than the actual picture
		//start things off with a full view  - where the screen shows the full picture - at a lower res of course
		theDisplay = new DisplayWindow();
		theDisplay.fullview();
		//size of the sketch window is currently set to a standard monitor size 
//		size (1872,1248);
		
		// except for when it is in standard HDtv mode
		size (2920,1080);
		
	    colorMode (HSB,255,255,255);	
		//setup the control panels and initialize all the CF and CF2 variables 		//my dell res is 3200 * 1800;  //change for other display setups
	    cf1 = addControlFrame("Pastiche-O-Matic",1240 , 1700, 3200-1250, 0); // currently the control frame is too big for a standard monitor!
	    //try waiting a bit - setting up two panels right away seems to cause bugs - no idea why.
		    int soon = millis() +1000; // wait for a second
		    while (millis() < soon) { int four = 2+2;}
	    cf2 = addControlFrame2("P-O-M",1240,300, 3200-2500,1420);
	}//end setup

	
	
	public void draw() {
		
		//update the screen view - that is look into myCanvas in full half or zoom modes (keyboard below - to change)
		theDisplay.update(this); 
		
		if(CF2.killNewStrokes==true) {// 1. stop generating new strokes, 2. wait for all the strokes to die and then 3.turn drawPaused on 
			if (GB.theStrokes.size() == 0) {
				CF2.drawPaused=true;
				CF2.runRB.activate(1);//turn on the drawPaused button
				CF2.killNewStrokes=false;
			}
		}
		
		if(CF2.killAllStrokes == true) {// 1. remove all the existing strokes 2. pause
			for(int i=GB.theStrokes.size()-1; i > -1; i--) {
				GB.theStrokes.remove(i); 
			} 
			CF2.killAllStrokes = false;
			CF2.runRB.activate(1);//turn on the drawPaused button
			CF2.drawPaused = true;
		}
		
		if(CF.newBrush == true) {// 1. remove all the existing strokes 
			for(int i=GB.theStrokes.size()-1; i > -1; i--) {
				GB.theStrokes.remove(i); 
			} 
			CF.newBrush = false;
		}
		
		if(CF2.drawPaused == false) { 
			//create strokes if you need them BUT Never more than MaxNewStrokes at a time
			int newStrokesCreated = 0;
			maxNewStrokes = 20;
			while ((GB.theStrokes.size() < CF.numOfStrokes) && (newStrokesCreated < maxNewStrokes) && (CF2.killNewStrokes ==false)) {
				switch (CF.strokeType) {
					case 0:
						GB.theStrokes.add(new Stroke(this)); 
						break;
					case 1:
						GB.theStrokes.add(new curveStroke(this));
						break;
					case 2:
						GB.theStrokes.add(new contAngStroke(this));
						break;
				}
				newStrokesCreated++;
			}
			if (GB.newStrokeFailed == true) {//newStrokeFailed is set to true in Stroke IFF the conditions for a new stroke can't be meet!!!!
				GB.newStrokeFailed = false; //reset the flag
				if (CF2.killNewStrokes = false)
				CF2.killNewStrokes = true;
				CF2.runRB.activate(2);//turn on the drawKilled button
			}
			else {
				//and update all the strokes that are running
				for(int i=0; i<GB.theStrokes.size(); i++) {
					Stroke currentStroke = GB.theStrokes.get(i);
					if(currentStroke.strokeIsDead == true) {
						GB.theStrokes.remove(i);
					}else { 
						currentStroke.updateStroke(this);
					}
				}
			}
			if (frameCount%10000 == 0) {
				GB.outputCanvas.save(GB.saveFileName+frameCount/1000);
				CF.strokeAngleAdjust = (float) (CF.strokeAngleAdjust +.01);
//				if (CF.strokeAngleAdjust >= TWO_PI) {CF2.drawPaused = true;}
				
			}
		}//end if drawPaused
		
		if (GB.showFrameRate == true) {			//display the frame rate in the Console view - should put this in the control panel******
			if (frameCount % 100 == 0) {println("frameRate "+frameRate+"    "+frameCount+"  frameCount "+frameCount);}
			if (frameCount % 2000 == 0) {GB.outputCanvas.save(GB.saveFileName+frameCount);}
		}
		
		if(CF.doCanvasCopy == true) {
			CF.doCanvasCopy = false;
			DisplayWindow.canvasCopy(this);
		}

		if (CF.doMeshDisplay == true) {
			CF.doMeshDisplay = false;
			MeshMap.displaySomeMesh(this);
		}
		
		if (CF2.meshRecalc == true) {
			// be sure all strokes are dead - because if any stroke referes to the meshmap it will be effed up after a new mesh is created
			for(int i=GB.theStrokes.size()-1; i > -1; i--) {GB.theStrokes.remove(i); } 
			GB.triNum = 0;//start over with any display in order to avoid a out of bounds error
		    GB.theMeshMap = new MeshMap(this,CF2.meshMode,CF2.mThreshold,CF2.mResolution);
		    GB.theMeshMap.displaySolidMesh(this);  // draws the mesh onto the screen - does not effect myCanvas
		 }

	}//end draw

	
public void mousePressed() {
	//clicking on the screen will set the hsb start position restrictions on CF....
	//first remap the mouse as the pixels of the display are probably not the same as the actual picture
	int mValueX = (int) PApplet.map(mouseX, 0,1920, DisplayWindow.screen_locx, DisplayWindow.screen_locx+DisplayWindow.displayW);
	int mValueY = (int) PApplet.map(mouseY, 0,1280, DisplayWindow.screen_locy,DisplayWindow.screen_locy+DisplayWindow.displayH);
	//and just in case let's make sure we are still in bounds - print a message just so you know why it crashed.
	if ((mValueX > GB.picWidth) || (mValueY > GB.picHeight)) {
		println("something is wrong in mousePressed ****** you should not be able to get here *******************");
	}
	
//***************************************************************	TOTAL HACK - need to think this through - later *****************
	
	if (CF.copyTrifromSource != -1) {
		PVector mpoint = new PVector (mValueX,mValueY);
		int trinum = MeshMap.findTriNum(this , mpoint);
		println (trinum);
		if ( trinum == -1) {
			println("this should not happen - was this a tri that got added with the CF2 add boarder tri switch?");
		}else {
//			GB.workCanvas.beginDraw();
				MeshMap.fillaTri(this, trinum);
				MeshMap.copyTriChunk(this,trinum);
//			GB.workCanvas.endDraw();
		}
	}
//***************************************************	
	

	// now get the color under the mouse from the screen that is displayed
	switch(CF.posResFrom) {//mouseSelectedColor used to set min max values for hsb  in CF.onMouseSetPosRestrict ()
							// that are then used for restrictions in Stroke.isPosLegal
	case 0:
		GB.mouseSelectedColor = GB.thePicture.pixels[mValueY*GB.picWidth+mValueX];
		break;
	case 1:
		GB.mouseSelectedColor = GB.altPicture.pixels[mValueY*GB.picWidth+mValueX];
		break;
	case 2:
		GB.mouseSelectedColor = GB.workCanvas.pixels[mValueY*GB.picWidth+mValueX];
		break;
	case 3:
		GB.mouseSelectedColor = GB.controlCanvas.pixels[mValueY*GB.picWidth+mValueX];
		break;
	case 4:
		GB.mouseSelectedColor = GB.outputCanvas.pixels[mValueY*GB.picWidth+mValueX];
		break;
	}
	GB.mousePressed = true;
	
	// if we are in list mode (creating a list for StrokeStartPosition to follow) get and store all the info 
	if(CF2.createStartPositionList == true) {
		//if this is the first point don't do the "back fill"
		if (CF2.newlist == true) {
			CF2.newlist = false;
			if (GB.lineList.size() != 0) {
				for (int i=GB.lineList.size(); i==0; i-- ) {
					GB.lineList.remove(i);
				}
			}
		}
		
		if (GB.lineList.size()==0) {
			PVector aPoint = new PVector (mValueX,mValueY);
			GB.lineList.add(aPoint);
			GB.workCanvas.fill(0,0,255,255);
			GB.workCanvas.ellipse(aPoint.x,aPoint.y,10,10);
			println("created the first point in the list");
		}else {
			//on all other points figure out the number of previous points (based on CF.lineListInterval) and put them in the list
 			int index = GB.lineList.size()-1;
			PVector lastPoint = GB.lineList.get(index);
			PVector newPoint = new PVector (mValueX,mValueY);
			PVector dir = PVector.sub(newPoint, lastPoint);
			dir.div(CF2.lineListIntervalNum);
			for(int i = 0; i < CF2.lineListIntervalNum; i++ ) {
				PVector nextPoint = new PVector (lastPoint.x,lastPoint.y);
				lastPoint.add(dir);
				PVector wtf = new PVector(lastPoint.x,lastPoint.y);
				GB.lineList.add(wtf);
				GB.workCanvas.beginDraw();
				GB.workCanvas.fill(255,0,0,255);
				GB.workCanvas.ellipse(wtf.x,wtf.y,10,10);
				GB.workCanvas.endDraw();
				println("added a point to the list at "+lastPoint.x+" "+lastPoint.y+" now there should be "+GB.lineList.size());
			}
		}
		//do something about looping the list!
	}
}

public void keyPressed() { // look after any keyboard input
	GB.externalInput(key,keyCode);
}//end keypressed	



//Control Frame setup and stuff
	CF addControlFrame(String theName, int theWidth, int theHeight, int thex, int they) {
			f = new Frame(theName);
			  CF p = new CF(this, theWidth, theHeight);
			  f.add(p);
			  p.init();
			  f.setTitle(theName);
			  f.setSize(p.w, p.h);
			  f.setLocation(thex, they);
			  f.setResizable(false);
			  f.setVisible(true);
			  return p;
		}
	
	CF2 addControlFrame2(String theName, int theWidth, int theHeight, int thex, int they) {
		f2 = new Frame(theName);
		  CF2 p = new CF2(this, theWidth, theHeight);
		  f2.add(p);
		  p.init();
		  f2.setTitle(theName);
		  f2.setSize(p.w, p.h);
		  f2.setLocation(thex, they);
		  f2.setResizable(false);
		  f2.setVisible(true);
		  return p;
	}
  	
//functions to turn on and off the two control frames	
	static void killCFs() {
		f2.setVisible(false);
		f.setVisible(false);
	}
	static void showCFs() {
		f.setVisible(true);
		f2.setVisible(true);
	}


//function for drag and drop pictures into pic and alt buffers
	void dropEvent(DropEvent theDropEvent) {
		println("drop event");//just so i know it actually happend or at least something happened
		if (theDropEvent.isFile()) {
		    pictureUrl = theDropEvent.filePath();// get the file path
		    CF2.killNewStrokes = true; // when you get a new file kill all the current strokes
		    CF2.runRB.activate(2);
		    GB.setPic(this,pictureUrl);
		}
	}
	
}//end Pastiche-O-Matic
