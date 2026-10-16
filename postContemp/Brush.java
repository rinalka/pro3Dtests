package postContemp;

import java.awt.Color;
import java.awt.Component;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Brush {

	PImage thisRawBrush;		//the original brush loaded in setup GB - unaltered selected in CF
	PGraphics currentBrush;    // the current state of the brush - depending on.....
	PImage tempBrush;
	PImage tempBrush2;

	PVector startLoc;
	PVector strokeVel;
	PVector strokesOldPos;
	
	int brushW;
	int brushH;
	static int elBrushW; //used just for the brush width and height for blobBrush/Image type  hack h********* should be setable in CF******
	static int elBrushH;
	
	int locX;
	int locY;
	float sizeY;
	float sizeX;
	int toggle ;
	int strokeAge;
	int tarketAge;
	int randomAgeCounter = 0;
	
	float brushAngle;
	int redTint,greTint,bluTint,alpTint;
	int r2,g2,b2; //the color (rgb) at the origin point of the brush
	
	float hueshift,satshift,brishift;
	float hueRanFactor,satRanFactor,briRanFactor;
	
	//constructor for bristle brushes
	Brush (PApplet p, PImage _rawBrush, PVector _startLoc, int _tarketAge) {
		startLoc = _startLoc;
		tarketAge = _tarketAge;
		strokesOldPos = startLoc;
		
		thisRawBrush = _rawBrush;
	 	brushW = thisRawBrush.width;
	 	brushH = thisRawBrush.height;

		currentBrush = p.createGraphics(brushW, brushH);
		currentBrush.beginDraw();
			currentBrush.fill(125);
			currentBrush.rect(0,0,brushW,brushH);
		currentBrush.endDraw();

		moveBrush(p,startLoc);	//move brush for the first time
	 	loadBrush(p);
	 	setHsbRanFactor(p);
	 	masterHsbBrush(p);
	 	blendBrush(p);
	  	shapeBrush(p);
	}

	//constructor for line and blob brushes
	public Brush(PApplet p, PVector _startLoc, PVector _startVel, int _tarketAge) {
		startLoc = _startLoc;
		strokeVel = _startVel;
		tarketAge = _tarketAge;
		strokesOldPos = startLoc;

		if(CF.brushType == 1) {// a line brush
			brushW = 4;
			brushH = 4;
		}else {//a blob brush
			brushW = CF.blobSize;		
			brushH = CF.blobSize;
		}
//		thisRawBrush = p.createImage(brushW, brushH, p.RGB);
		GB.bW = brushW;
		GB.bH = brushH;

		currentBrush = p.createGraphics(brushW, brushH);
		currentBrush.beginDraw();
			currentBrush.fill(125,0);
			currentBrush.rect(0,0,brushW,brushH);
		currentBrush.endDraw();
		
		moveBrush(p,startLoc);	//move brush for the first time
	 	loadBrush(p);
	 	setHsbRanFactor(p);
	 	masterHsbBrush(p);
	 	blendBrush(p);
	}

	void processBrush(PApplet p, PVector pos, PVector strokeVelocity, int _strokeAge, int triNum) {
		strokeAge = _strokeAge;
		strokeVel = strokeVelocity; //some brush types (like lineBrush) need to know the strokes current Velocity
		rotateBrush();  	// set the the brushAngle that is to be used in drawBrush
		sizeBrush(p);		// set the scale(w and h) that is to be used in drawBrush
		tintBrush();		// set the tint parameters that are used in drawBrush
		moveBrush (p,pos);  // move the brush to it's new position (based on the stroke position 'pos'
		loadBrush(p);       // load the brush from the fmap
		blendBrush(p);		// like it says
		masterHsbBrush(p);		// adjust the Brush with hsb values
		shapeBrush(p);		// map to the rawBrush shape and Alpha
		drawBrush (p);		// draw the brush into myCanvas
		strokesOldPos = pos; // save the current stroke position in old position 
	}
	
	void rotateBrush() {
		brushAngle = (float) (brushAngle+CF.brushRotation);
	}
	
	void sizeBrush(PApplet p) { 
		//adjust for CF.brushSize  ****************************depends on the limits of brush size being -10, 10
		if(CF.BrushSize >= 1) {	
			sizeX = CF.BrushSize * brushW; 	
			sizeY = CF.BrushSize * brushH; 
			}
		else {
			if(CF.BrushSize < 0) {
				if(CF.BrushSize == -10) CF.BrushSize = -9.99f;//if -10 you would get a divide by 0 error....
				sizeX = ((10 + CF.BrushSize ) / 10) * brushW; 	
				sizeY = ((10 + CF.BrushSize ) / 10) * brushH; }
			else {sizeX = sizeY = 1;}// for all those cases from 0 to 1 (just call it 1)
		}
		
		// now factor in the skew slider (which goes between -.9 and .9) so slider is percent 1 side or the other....
		sizeX = sizeX * (1+ CF.BrushSkew);
		sizeY = sizeY * (1- CF.BrushSkew);
		if (sizeX < 1)sizeX =1;
		if (sizeY < 1)sizeY =1;
		
		// now size by HSB
		if(CF.sizehsbType != -1) {
			int color = tempBrush.pixels[((brushH/2)*brushW)+(brushW/2)]; //get color of the  pixel
			int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
			int g = (color >> 8) & 0xFF;
			int b = color  & 0xFF;
			float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1
			float hue = hsb[0];
			float sat = hsb[1];
			float bri = hsb[2];
			float scale = 5;
			float minFactor = scale * CF.hsbMin;
			float maxFactor = scale * CF.hsbMax;
			
			switch(CF.sizehsbType) {
			case 0: //hue
				if (CF.sizeByHSBInvert == true) hue = 1-hue;
				hue = hue + CF.hueCenter;
				if (hue > 1) hue = hue -1;
				if (hue < 0) hue = hue +1;
				float hueFactor = p.map(hue, 0, 1, minFactor, maxFactor);
				sizeX = sizeX + (sizeX * hueFactor);
				sizeY = sizeY + (sizeY * hueFactor);
				break;
			case 1: //saturation
				if (CF.sizeByHSBInvert == true) sat = 1-sat;
				float satFactor = p.map(sat, 0, 1, minFactor, maxFactor);
				sizeX = sizeX + (sizeX * satFactor);
				sizeY = sizeY + (sizeY * satFactor);
				break;
			case 2: //brightnessif 
				if (CF.sizeByHSBInvert == true) bri = 1-bri;
				float briFactor = p.map(bri, 0, 1, minFactor, maxFactor);
				sizeX = sizeX * briFactor;
				sizeY = sizeY * briFactor;
				break;
			}
		}
			
		//finally size by age
		if(CF.sizeageType != -1) { // -1 is telling me that there is no type turned on.
			// CF.ageMax, CF.ageMin are between -.9 and .9 
			float scale = 5;  //should probably make this a CF slider or something
			float minFactor = scale * CF.ageMin;
			float maxFactor = scale * CF.ageMax;
			float sizeByAgeInc =  (maxFactor - minFactor) / (tarketAge-1);
			float sizeByAgeMod =  sizeByAgeInc;

			switch(CF.sizeageType) {
			case 0: //small to big
				sizeByAgeMod =  sizeByAgeInc * strokeAge;
				sizeX = sizeX * sizeByAgeMod; 
				sizeY = sizeY * sizeByAgeMod;
				break;
			case 1://big to small
				sizeByAgeMod =  sizeByAgeInc * (tarketAge - strokeAge);
				sizeX = sizeX * sizeByAgeMod; 
				sizeY = sizeY * sizeByAgeMod;
				break;
			case 2://small to big to small
				if (strokeAge < (tarketAge/2)) {
					sizeByAgeMod =  sizeByAgeInc * strokeAge;
					sizeX = sizeX * sizeByAgeMod; 
					sizeY = sizeY * sizeByAgeMod;
				}else {
					sizeByAgeMod =  sizeByAgeInc * (tarketAge - strokeAge);
					sizeX = sizeX * sizeByAgeMod; 
					sizeY = sizeY * sizeByAgeMod;
				}
				break;
			case 3://this is bogus it doesn't really work ********************fix it or remove it
				randomAgeCounter--;
				if ( randomAgeCounter <= 0) {randomAgeCounter = (int) p.random(tarketAge/5,tarketAge);}
				sizeByAgeMod =  sizeByAgeInc * strokeAge;
				sizeX = sizeX * sizeByAgeMod; 
				sizeY = sizeY * sizeByAgeMod;
				break;
			}
		}

		//just in case some sort of round off crap
		if(sizeX < 1) sizeX = 1;
		if(sizeY < 1) sizeY = 1;
	}
	
	
	void tintBrush() {///******************************************** redTint ...  not really used anymore only use alpTint???????????
		redTint = (int) CF.TintR;
		greTint = (int) CF.TintG;
		bluTint = (int) CF.TintB;
		alpTint = (int) CF.Alph;
	}
	
	void moveBrush(PApplet p, PVector pos) {
		locX = (int) pos.x; 
		locY = (int) pos.y; 
	}
	
	void loadBrush(PApplet p) {
		switch (CF.loadBrushFrom) {
		case 0:
			tempBrush = GB.thePicture.get(locX,locY,brushW,brushH);
			break;
		case 1:
			tempBrush = GB.altPicture.get(locX,locY,brushW,brushH);
			break;
		case 2:
			tempBrush = GB.workCanvas.get(locX,locY,brushW,brushH);
			break;
		case 3:
			tempBrush = GB.controlCanvas.get(locX,locY,brushW,brushH);
			break;
		}
	}


	void blendBrush(PApplet p) {
		//all gone bye bye - because now there is a blendBrush class 
	}

	void shapeBrush(PApplet p) {
		currentBrush.loadPixels();
		for(int h=0; h < brushH; h++) { // for every pixel in the brush
			for( int w=0; w < brushW; w++) {
				int index = (h * brushW)+w;
				int mask = thisRawBrush.pixels[index] <<24;  //shift over and use blue as alpha?
				int val = tempBrush.pixels[index] & 0x00ffffff;   // tempBrush has a chunk of graphic be sure no alpha
				currentBrush.pixels[index] = val | mask; // currentBrush gets the tempBrush color with the rawBrush alph
			}
		}
		currentBrush.updatePixels();
	}
	
	void setHsbRanFactor (PApplet p) {// called only when the brush is constructed - so only for the stroke...
		hueRanFactor = p.random(CF.HueRanLow,CF.HueRanHig) ;
		satRanFactor = (float) (p.random(CF.SatRanLow,CF.SatRanHig) *.01); //convert to %
		briRanFactor = (float) (p.random(CF.BriRanLow,CF.BriRanHig) *.01);
	}
	

	
	void masterHsbBrush (PApplet p) {//called only when brush constructed
		hueshift = p.map(CF.hueShift,-127,127,(float)-.5,(float).5);
		satshift = (float) (CF.satShift *.01); // convert satShift to a percent    
		brishift = (float) (CF.briShift * .01); 

//		p.println(tempBrush.pixels.length+" "+(brushW*brushH));
		for(int h=0; h< brushH; h++) { // for every pixel in the brush adjust hue sat and bri
			for( int w=0; w< brushW; w++) {
				int index = (h*brushW)+w;
				if(index > tempBrush.pixels.length -1) {
					p.println("index in masterHsbBrush out of range...."); 
					index = 1;
				}
				int color = tempBrush.pixels[index]; //get color of the  pixel
				int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
				int g = (color >> 8) & 0xFF;
				int b = color  & 0xFF;
				
				float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1
				
				//add in the brush randomization factor...
				if(CF.hcran == true) {
					 hsb[0] = hsb[0] +hueRanFactor;		
				}
				if(CF.scran == true) {
					hsb[1] = hsb[1] + hsb[1]*satRanFactor;
				}
				if(CF.bcran == true) {
					 hsb[2] = hsb[2] +hsb[2]*briRanFactor;
				}
				 
				//and then the master hsb shifts
				float hue1 = hsb[0]+hueshift;
				if (hue1 > 1) hue1=hue1-1;
				if (hue1 < 0) hue1=hue1+1;
				float sat1 = p.constrain( hsb[1]+(hsb[1]*satshift), 0, 1);
				float bri1 = p.constrain( hsb[2]+(hsb[2]*brishift), 0, 1);
				color = Color.HSBtoRGB(hue1, sat1, bri1);// which should be back to numbers between 0 and 255
				
				tempBrush.pixels[index] = color;
			}
		}
	}
	
	
	void drawBrush (PApplet p){
		GB.outputCanvas.imageMode(p.CENTER);
		GB.outputCanvas.pushMatrix();
			GB.outputCanvas.translate(locX+(brushW/2),locY+(brushH/2));
			GB.outputCanvas.rotate (brushAngle);
			GB.outputCanvas.tint(255,CF.alphaVal);
			GB.outputCanvas.image(currentBrush,0,0,sizeX,sizeY);//
			GB.outputCanvas.removeCache(currentBrush); //****** delete from memory manually or CRASH because of Java heap
		GB.outputCanvas.popMatrix();
		GB.outputCanvas.imageMode(p.CORNER);//so all the other places that draw to myCanvas aren't all off....
	}

	public Boolean amIaZombie(int age) {
		return null;
	}

	
}//end of the basic Brush class


//dippedBrush   ***************can be removed - is just smudge brush at one extreme - but it does run faster
//**************
//gets its colorS from where it is first dipped into the original picture (which is the default for load brush(at the moment)
class dippedBrush extends Brush {

	dippedBrush (PApplet p, PImage _rawBrush, PVector startLoc, int tarketAge) {
	    super(p, _rawBrush, startLoc, tarketAge);
//	    masterHsbBrush(p);		// adjust the Brush with hsb values
	}
	//just don't load the brush after the first call to loadBrush in the constructor!!!!!!!!!!!!!
	void processBrush(PApplet p, PVector pos,PVector vel,int _strokeAge, int triNum) {
		strokeAge = _strokeAge;
		rotateBrush();  	// set the the brushAngle that is to be used in drawBrush
		sizeBrush(p);		// set the scale(w and h) that is to be used in drawBrush
		tintBrush();		// set the tint paramaters that are used in drawBrush
		moveBrush (p,pos);  // move the brush to it's new position (based on the stroke position 'pos'
//		loadBrush(p);       // load the brush from the fmap
//	 	blendBrush(p);
//	 	hsbVarBrush(p);
		
		shapeBrush(p);		// do what needs to be done to the current brush color including mapping to the rawBrush shape
		drawBrush (p);		// draw the brush into myCanvas
	}
}//end dippedBrush


//paintBrush
//**********
//paints a color onto the canvas - color current takeng from the pixel at the center of the start location of the brush
class paintBrush extends Brush {

	paintBrush (PApplet p, PImage _rawBrush, PVector startLoc, int tarketAge) {
	    super(p, _rawBrush, startLoc, tarketAge);
//	    masterHsbBrush(p);		// adjust the Brush with hsb values
	}
	//don't load the brush after the first call to loadBrush in the constructor!!!!!!!!!!!!!
	void processBrush(PApplet p, PVector pos, PVector vel, int _strokeAge, int triNum) {
		strokeAge = _strokeAge;
		rotateBrush();  	// set the the brushAngle that is to be used in drawBrush
		sizeBrush(p);		// set the scale(w and h) that is to be used in drawBrush
		tintBrush();		// set the tint paramaters that are used in drawBrush
		moveBrush (p,pos);  // move the brush to it's new position (based on the stroke position 'pos'
//		loadBrush(p);       // load the brush from the fmap
//	 	blendBrush(p);
//	 	hsbVarBrush(p);
		
		shapeBrush(p);		// do what needs to be done to the current brush color including mapping to the rawBrush shape
		drawBrush (p);		// draw the brush into myCanvas
	}
	void loadBrush(PApplet p) {
		switch (CF.loadBrushFrom) {
		case 0:
			tempBrush = GB.thePicture.get(locX,locY,brushW,brushH);
			break;
		case 1:
			tempBrush = GB.altPicture.get(locX,locY,brushW,brushH);
			break;
		case 2:
			tempBrush = GB.workCanvas.get(locX,locY,brushW,brushH);
			break;
		case 3:
			tempBrush = GB.controlCanvas.get(locX,locY,brushW,brushH);
			break;
		}
		int color = tempBrush.pixels[(brushH/2*brushW)+brushW/2]; //get color of the center pixel
		int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
		int g = (color >> 8) & 0xFF;
		int b = color  & 0xFF;
		float[] hsb = Color.RGBtoHSB(r,g,b,null);
		float range = CF.HbristleVar/10000;
		
		if (CF.HbristleVar != 0) {
			for(int h=0; h< brushH; h++) { // for every pixel in the brush set to that color
				for( int w=0; w< brushW; w++) {
					int index = (h*brushW)+w;
					float hueshift = p.random(-range,range);
					float satshift = p.random(-range,range);
					float brishift = p.random(-range,range);
					hsb[0] = p.constrain( hsb[0]+hueshift  	, -1, 2);
					hsb[1] = p.constrain( hsb[1]+satshift  	, 0, 1);
					hsb[2] = p.constrain( hsb[2]+brishift  	, 0, 1);
					color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
					tempBrush.pixels[index] = color;
				}
			}
			tempBrush.updatePixels();
		}
	}
}//end paintBrush2


// smudgeBrush mixes the current color of the  brush with the color under the brush now

class smudgeBrush extends Brush {
	float perx,pery,diff;
	
	  smudgeBrush (PApplet p, PImage _rawBrush, PVector startLoc, int tarketAge) {
	    super(p, _rawBrush, startLoc, tarketAge);
	    
	    //percentage of smudge calculation - make slider be a percent - then account for the number of times brush will be used
		perx = CF.smudgePercent/100/CF.strokeAge;
		pery = 1 - perx;  
		diff = perx;
		
		for(int h=0; h < brushH; h++) { // need to set the currentBrush to be a copy of the tempBrush (which was set in loadBrush)
			for( int w=0; w < brushW; w++) {
				int index = (h *brushW)+w;
				currentBrush.pixels[index] = tempBrush.pixels[index];
			}
		}
	  }
	  
		void shapeBrush(PApplet p) {
			perx = perx + diff;
			pery = pery - diff;

			for(int h=0; h< brushH; h++) { // for every pixel in the brush
				for( int w=0; w< brushW; w++) {
					int index = (h*brushW)+w;
					int mask = thisRawBrush.pixels[index] <<24; // use the blue value of rawBrush pixel as the alpha channel
					//*************can't figure how to mix in HSB so switch val to RGB
					int HSBval1 = tempBrush.pixels[index];   // tempBrush has a chunk of graphic be sure no alpha
					int hue1 = (HSBval1 >> 16) & 0xFF;  //shift and and to get the HSB values isolated
					int sat1 = (HSBval1 >> 8) & 0xFF;
					int bri1 = HSBval1  & 0xFF;
	
					int hsbVal2 = currentBrush.pixels[index]; // get the colors currently in the brush
					int hue2 = (hsbVal2 >> 16) & 0xFF;  //shift and and to get the HSB values isolated
					int sat2 = (hsbVal2 >> 8) & 0xFF;
					int bri2 = hsbVal2  & 0xFF;
					
				    int hue3 = (int) ((hue1*perx)+(hue2*pery));
				    int sat3 = (int) ((sat1*perx)+(sat2*pery));
				    int bri3 = (int) ((bri1*perx)+(bri2*pery));
					
					hue3 = hue3	<< 16;// & 0x00ff0000
					sat3 = sat3 <<  8;// & 0x0000ff00;
					bri3 = bri3	;
					int color = hue3+sat3+bri3;
					currentBrush.pixels[index] = color | mask;
				}
			}
		}
}//end smudgeBrush


// color**********************************************************needs work
class colorBlendBrush extends Brush {

	colorBlendBrush (PApplet p, PImage _rawBrush, PVector startLoc, int tarketAge) {
	    super(p, _rawBrush, startLoc, tarketAge);
	}
	
	void blendBrush(PApplet p) {
//*****************************************************************need options for getting colors
		int color = p.color(CF.TintR,CF.TintG,CF.TintB);
		for (int i=0; i<tempBrush.pixels.length; i++ ) {
			switch(CF.colorBlendType) {
				case 0:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.BLEND);
					break;
				case 1:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.ADD);
					break;
				case 2:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.SUBTRACT);
					break;
				case 3:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.DARKEST);
					break;
				case 4:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.LIGHTEST);
					break;
				case 5:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.DIFFERENCE);
					break;
				case 6:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.EXCLUSION);
					break;
				case 7:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.MULTIPLY);
					break;
				case 8:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.SCREEN);
					break;
				case 9:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.OVERLAY);
					break;
				case 10:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.HARD_LIGHT);
					break;
				case 11:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.SOFT_LIGHT);
					break;
				case 12:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.DODGE);
					break;
				case 13:
					tempBrush.pixels[i] = p.blendColor(tempBrush.pixels[i],color, p.BURN);
					break;
				}
			}
		}
}//end colorBlendBrushBrush


