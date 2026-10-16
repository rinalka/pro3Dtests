package postContemp;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;


public class Stroke {

	Brush thisStrokesBrush;
	
	PVector startStrokePos;
	PVector currentStrokePos;
	PVector tarketStrokePos;
	
	PVector strokeVelocity;
	PVector strokeAcceleration;

	int tarketStrokeAge;
	int strokeAge;
	Boolean strokeIsDead; 
	Boolean strokeTooOld;
	Boolean strokeIsaZombie;
	
	int strokeSpeed;
	int thisStrokesTriNum;
	int thisStrokesVertNum;
	int randomCount = 0;

	
	Stroke(PApplet p){
		strokeAge = 0;
		strokeIsDead = true;
		strokeIsaZombie = false;
		randomCount = 20;
		thisStrokesTriNum = 0;
		thisStrokesVertNum = 0;
		strokeTooOld = false;
	
		setStartPosition(p);   // there is a potential in set start position for an infinite loop caused by too tight color restrictions - this should be fixed some day
		setTarketStrokeAge(p);
		setVelocity(p);
		setAcceleration(p);
		
		setStrokesBrush(p, GB.rawBrush);
	}

//Select and set Brush type
	void setStrokesBrush(PApplet p, PImage thisStrokesBrushFile) {
		switch(CF.brushType) {
		case 0://bristle brush
			switch (CF.bristleBrushType) {
			case 0:
				thisStrokesBrush = new Brush(p,thisStrokesBrushFile,startStrokePos,tarketStrokeAge);
				break;
			case 1:
				thisStrokesBrush = new dippedBrush(p,thisStrokesBrushFile,startStrokePos,tarketStrokeAge);
				break;
			case 2:
				thisStrokesBrush = new smudgeBrush(p,thisStrokesBrushFile,startStrokePos,tarketStrokeAge);
				break;
			case 3:
				thisStrokesBrush = new paintBrush(p,thisStrokesBrushFile,startStrokePos,tarketStrokeAge);
				break;
			case 4:
				thisStrokesBrush = new colorBlendBrush(p,thisStrokesBrushFile,startStrokePos,tarketStrokeAge);
				break;
			}
			break;
		case 1://line brush
			thisStrokesBrush = new lineBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge);
			break;
		case 2://blob brush
			switch(CF.blobBrushType) {
			case 0:
			case 1:
				thisStrokesBrush = new blobBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge);
				break;
			case 2:
				thisStrokesBrush = new smudgeBlobBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge);
				break;
			case 3:
				thisStrokesBrush = new paintBlobBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge);
				break;
			case 4:
				thisStrokesBrush = new blendBlobBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge);
				break;
			case 5:
				thisStrokesBrush = new randomBlobBrush(p,startStrokePos,strokeVelocity,tarketStrokeAge,thisStrokesTriNum);
				break;
			}

		}
}//end setStrokesBrush
	

//Start position 
//sets a startStrokePos and currentStrokePos to a PVector - within the limits of "legal" start positions determined by the HSB CF setings
//and according to CF.startPosType 

	void setStartPosition(PApplet p) {
		//here lies a hack for a problem with blob brushes....
		//GB.bW and GB.bH are set in GB when the standard brush is loaded - however
		//a blob brush has it's size set by CF.blobSize, so it is possible to get here and GB.bW bH be wrong values and a bad startPosition being picked
		//so....
		if(CF.brushType == 2) {//then this is a blob brush
			GB.bW = CF.blobSize;
			GB.bH = CF.blobSize;
		}
		//turns out it is the same sort of story for line brushes only they lines (except for drop and that should be safe) should on ly be as wide as ....
		if(CF.brushType == 1) {//then this is a line brush
			GB.bW = CF.strokeWeight;
			GB.bH = CF.strokeWeight;
			if(CF.lineBrushType == 4) //a drop line needs some extra space for the dropped line
				GB.bW = CF.strokeWeight*CF.dropSize;
				GB.bH = GB.bW;
		}
		
		int count = 0;
		while(strokeIsDead == true) {  //**************this could be ugly need to fix**************if the conditions in Pos legal are too strict;
			count++;
			if (count == 20000) {
				p.println("stroke position restricts are too tight I have Kill Paused. Reset something.");
				strokeIsDead = false;
				GB.newStrokeFailed = true;
			}
		
			switch(CF.startPosType) {
			case 0://random
				int holdx = (int) p.random(0,GB.picWidth-GB.bW);
				int holdy = (int) p.random(0,GB.picHeight-GB.bH);
				startStrokePos = new PVector(holdx,holdy);
				currentStrokePos = new PVector(holdx,holdy);
				isPosLegal();
				break;
				
			case 1://ordered
				int tx = (int) GB.holdStrokePos.x + CF.orderedGridResX ;
				int ty = (int) GB.holdStrokePos.y ;
				if (tx > GB.picWidth-GB.bW) {
					tx = GB.bW;
					ty = (int) GB.holdStrokePos.y + CF.orderedGridResY ;
	
					if (ty > GB.picHeight-GB.bH) {
						GB.holdStrokePos.y = GB.bH;
						ty = GB.bW;
						//wait to start over...
						CF2.drawPaused = true;
						CF2.runRB.activate(2);//turn the Kill paused button on (kill lets the existing strokes finish)
					}
				}
				int holdtx = (int) (tx + p.random(-CF.oGridRanX,CF.oGridRanX));
				int holdty = (int) (ty + p.random(-CF.oGridRanY,CF.oGridRanY));
				if(holdtx < GB.bW) holdtx = GB.bW;
				if(holdty < GB.bH) holdty = GB.bH;
				if(holdtx > GB.picWidth-GB.bW) holdtx = GB.picWidth-GB.bW;
				if(holdty > GB.picHeight-GB.bH) holdty = GB.picHeight-GB.bH;
				GB.holdStrokePos = new PVector (tx,ty);
				startStrokePos = new PVector(holdtx,holdty);
				currentStrokePos = new PVector(holdtx,holdty);
				isPosLegal();
				break;
				
			case 2://triangle
				GB.triNum++;
				thisStrokesTriNum = GB.triNum;
				
				if(GB.triNum > GB.theMeshMap.triangles.size()-2) {
					GB.triNum = 0;
					CF2.drawPaused = true;
					CF2.runRB.activate(1);//turn the paused button on
				}

				startStrokePos = new PVector (0,0);
				startStrokePos =  GB.theMeshMap.triangles.get(GB.triNum).center();

				//check center is in range - shouldn't need to do this but just in case  ***********************
				if ( (startStrokePos.x > GB.picWidth -GB.bW) || (startStrokePos.y > GB.picHeight-GB.bH)){
					 p.println("center of the triangle was off screen  *************** should not be happening *************");
				}
				
				if((CF.brushType == 0) || (CF.brushType == 2)) {//if this is a brisle or blob brush worry about being able to load the brush.....
					if ( (startStrokePos.x > GB.picWidth-GB.bW) || (startStrokePos.y > GB.picHeight-GB.bH)) {
							startStrokePos = new PVector(0,0);//for now set to the top left corner and print a warning..... you really do need tofix this
							p.println("center of the triangle" +thisStrokesTriNum+" was out of range - it would have caused a crash - you need to fix this soon");
					  }
				}
				
				currentStrokePos = new PVector(startStrokePos.x,startStrokePos.y);
				isPosLegal();
				break;
				
			case 3: // start at the mouse location
				//worry about the brush size or else a bristle could be outside the screen area and cause an indexing error
				int mValueX = (int) (p.mouseX);// + p.random(-20,20));
				if (mValueX > p.width-GB.bW-1) mValueX = p.width-GB.bW-10;
				if (mValueX < 0+1) mValueX = 0+1;
				
				int mValueY = (int) (p.mouseY);// + p.random(-20,20));
				if (mValueY < 0+1) mValueY = 0+1;
				if (mValueY > p.height-GB.bH-1) mValueY = p.height-GB.bH-10;
				//remap the mouse to be appropriate for the size of the picture and what is being displayed
				mValueX = (int) PApplet.map(mValueX, 0,1920, DisplayWindow.screen_locx, DisplayWindow.screen_locx+DisplayWindow.displayW);
				mValueY = (int) PApplet.map(mValueY, 0,1280, DisplayWindow.screen_locy,DisplayWindow.screen_locy+DisplayWindow.displayH);
				//now set the start pos
				startStrokePos = new PVector (mValueX,mValueY);
				isPosLegal();
				currentStrokePos = startStrokePos;
				break;
				
			case 4: // from a vertice
				GB.verticeNum++;

				if(GB.verticeNum > GB.theMeshMap.vertices.size()-1) {
					GB.verticeNum = 0;
					CF2.drawPaused = true;
					CF2.runRB.activate(1);//turn the paused button on
				}
				thisStrokesVertNum = GB.verticeNum;
				startStrokePos = new PVector (0,0);
				startStrokePos =  GB.theMeshMap.vertices.get(GB.verticeNum);
				//******debug crap - should fix the out of range problem
				//check vertex is in range
				if ( (startStrokePos.x > GB.picWidth-GB.bW) || (startStrokePos.y > GB.picHeight-GB.bH) 
				  || (startStrokePos.x < GB.bW) || (startStrokePos.y < GB.bH)) {
				  startStrokePos = new PVector(32,32);
			      p.println("a vertice was out of range - it would have caused a crash - you need to fix this soon **************");
				  }
				holdx = (int) startStrokePos.x;
				holdy = (int) startStrokePos.y;
				currentStrokePos = new PVector(holdx,holdy);
				isPosLegal();
				break;
			case 5: // from list
				
				if(GB.listNum > GB.lineList.size()-1) {
					GB.listNum = 0;
					p.println("finished the list starting over");
				}
				PVector currentPnt = new PVector(0,0);
				currentPnt = 	GB.lineList.get(GB.listNum);
				int xpos = (int) currentPnt.x;
				int ypos = (int) currentPnt.y;
				startStrokePos = new PVector (xpos,ypos);
				currentStrokePos = startStrokePos;
				p.println("starting a new stroke at "+startStrokePos.x+" "+startStrokePos.y+ " index should be "+GB.listNum);
				isPosLegal();
				GB.listNum++;
				break;
			}//end switch 

		}// end while
	}//end setStartPosition
	
	
	void isPosLegal() {//the idea is a stroke remains dead ( isStrokeDead == true) until it gets a legal position and sets isStrokeDead to false
		//if non of the position restrict switches are on set isStrokeDead to false and leave....

		if ((CF.huePosRestrict == false) && (CF.satPosRestrict == false) && (CF.briPosRestrict == false) ) {//there are no restrictions
			strokeIsDead = false;
		}		
		else {
			boolean hueIsD  = true;
			boolean satIsD  = true;
			boolean briIsD = true;
			
			int index = (int) (startStrokePos.y*GB.picWidth+startStrokePos.x);
			int color = 0;

			switch(CF.posResFrom) {// which image will position restrictions operate from
			case 0: //pic
				color = GB.thePicture.pixels [(int) (startStrokePos.y*GB.picWidth+startStrokePos.x)];
				break;
			case 1: //alt
				color = GB.altPicture.pixels [(int) (startStrokePos.y*GB.picWidth+startStrokePos.x)];
				break;
			case 2: // work
				color = GB.workCanvas.pixels [(int) (startStrokePos.y*GB.picWidth+startStrokePos.x)];
				break;
			case 3: // control
				color = GB.controlCanvas.pixels [(int) (startStrokePos.y*GB.picWidth+startStrokePos.x)];
				break;
			case 4: // output
				color = GB.outputCanvas.pixels [(int) (startStrokePos.y*GB.picWidth+startStrokePos.x)];
				break;
			}

			int r = ((color >> 16) & 0xFF)  ;  //shift and and to get the rgb of the tempBrush pixel
			int g = ((color >> 8) & 0xFF)  ;
			int b = color  & 0xFF  ;
			float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1
			float hue = (hsb[0]*255);
			float sat = (hsb[1]*255);
			float bri = (hsb[2]*255);
			
			if (CF.huePosRestrict == true) {
				if (CF.huePosRestrictMin > CF.huePosRestrictMax) { //then the hue values have wrapped!!
					if ( ((hue >= 0) && (hue <= CF.huePosRestrictMax)) || ((hue >= CF.huePosRestrictMin) && (hue <= 255)) ) { hueIsD = false;}
				}else {
					if ((hue >= CF.huePosRestrictMin) && (hue <= CF.huePosRestrictMax)) {hueIsD = false;}
				}
			}
						
			if (CF.satPosRestrict == true) {
				if ((sat >= CF.satPosRestrictMin) && (sat <= CF.satPosRestrictMax)) {satIsD = false;}
			}
			
			if (CF.briPosRestrict == true) {
				if ((bri >= CF.briPosRestrictMin) && (bri <= CF.briPosRestrictMax)) {briIsD = false;}
			}

			// if each of h s b accepted the stroke position - accept the stroke.... WARNING this can put the new stroke while loop in a no win go infinitely state
			if ((!hueIsD || CF.huePosRestrict==false) && (!satIsD || CF.satPosRestrict==false) && (!briIsD || CF.briPosRestrict==false)){
				strokeIsDead = false;
				}
		}
	}
	

	
//setVelocity 	 ---- sets the direction for a given stroke ---
// ends with strokeVelocity set to a unit PVector
	void setVelocity(PApplet p){
		strokeVelocity = PVector.random2D(); //start with a random default Velocity
		switch(CF.velocityType) {
			case 0://random but straight
				strokeVelocity = PVector.random2D(); // a unit vector
				break;
			case 1://angle based on pic color
			  	float c = getAngleFromPic(p);
			  	c =  p.map(c, 0, 255, 0, p.TWO_PI);
			  	c = c + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
			  	strokeVelocity = PVector.fromAngle(c);
			  	break;
			case 2://angle based on  alt color
			  	float cc = getAngleFromAlt(p);
			  	cc =  p.map(cc, 0, 255, 0, p.TWO_PI);
			  	cc = cc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
			  	strokeVelocity = PVector.fromAngle(cc);
			  	break;
			case 3://angle based on  work color
			  	float ccc = getAngleFromWork(p);
			  	ccc =  p.map(ccc, 0, 255, 0, p.TWO_PI);
			  	ccc = ccc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
			  	strokeVelocity = PVector.fromAngle(ccc);
			  	break;	
			case 4://angle based control color
			  	float cccc = getAngleFromControl(p);
			  	cccc =  p.map(cccc, 0, 255, 0, p.TWO_PI);
			  	cccc = cccc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
			  	strokeVelocity = PVector.fromAngle(cccc);
			  	break;
			case 5: //seeker pic
			case 6: //seeker alt
			case 7: //seeker work
			case 8: //seeker control
			case 9: // go to the mouse
				strokeVelocity = PVector.random2D();
				break;
		}//end switch
	}//end setVelocity

//set acceleration
// sets strokeAcceleration according to CF.velocityType to a unit Vector
	void setAcceleration (PApplet p) {
		switch(CF.velocityType) {
		case 0://random but straight
			if(CF.strokeType == 2) {//that is to say if this is a continous stroke - we want a random walk
				randomCount++;//note random count needs to be initialized to the setting -1
				if(randomCount > 20) {
					strokeAcceleration = PVector.random2D();// get a new direction for our random walk --- this needs a slider for time range**********
					randomCount = 0;
				}
			}
			else {
			strokeAcceleration = strokeVelocity; 
			strokeAcceleration.normalize();
			}
			break;
		case 1://angle based pic color
		  	float c = getAngleFromPic(p);
		  	c =  p.map(c, 0, 255, 0, p.TWO_PI);
		  	c = c + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
		  	strokeAcceleration = PVector.fromAngle(c);
		  	break;
		case 2://angle based on alt color
		  	float cc = getAngleFromAlt(p);
		  	cc =  p.map(cc, 0, 255, 0, p.TWO_PI);
		  	cc = cc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
		  	strokeAcceleration = PVector.fromAngle(cc);
		  	break;
		case 3://angle based on alt color
		  	float ccc = getAngleFromWork(p);
		  	ccc =  p.map(ccc, 0, 255, 0, p.TWO_PI);
		  	ccc = ccc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
		  	strokeAcceleration = PVector.fromAngle(ccc);
		  	break;
		case 4://angle based on alt color
		  	float cccc = getAngleFromControl(p);
		  	cccc =  p.map(cccc, 0, 255, 0, p.TWO_PI);
		  	cccc = cccc + CF.strokeAngleAdjust;//which adds a number between 0 and TWO_PI as set on the control panel 
		  	strokeAcceleration = PVector.fromAngle(cccc);
		  	break;
		case 5: // seeker from pic - velocity based on angle around the current point
				strokeAcceleration = getDirectionFromPic(p);
				strokeAcceleration.normalize();
			break;
		case 6: // seeker from alt - velocity based on angle around the current point
			strokeAcceleration = getDirectionFromAlt(p);
			strokeAcceleration.normalize();
			break;
		case 7: // seeker from work - velocity based on angle around the current point
			strokeAcceleration = getDirectionFromWork(p);
			strokeAcceleration.normalize();
			break;
		case 8: // seeker from control - velocity based on angle around the current point
			strokeAcceleration = getDirectionFromControl(p);
			strokeAcceleration.normalize();
			break;
		case 9: // mouse
			int mValueX = (int) PApplet.map(p.mouseX, 0,1920, DisplayWindow.screen_locx, DisplayWindow.screen_locx+DisplayWindow.displayW);
			int mValueY = (int) PApplet.map(p.mouseY, 0,1280, DisplayWindow.screen_locy,DisplayWindow.screen_locy+DisplayWindow.displayH);
			PVector target = new PVector (mValueX,mValueY);
			target.sub(currentStrokePos);
			target.normalize();
			strokeAcceleration = target;
			break;
		}//end switch
	}
	  
	
	float getAngleFromPic(PApplet p) {
		int index = (int) currentStrokePos.y;
		int x = (int) currentStrokePos.x;
	  	index = index*GB.picWidth;
	  	index = (int) (index+x);
	  	float c=0;
	  	switch (CF.velocityKind) {
	  	case 0: 
	  		c =  p.red(GB.thePicture.pixels[index]);
	  		break;
	  	case 1:
	  		c =   p.green(GB.thePicture.pixels[index]);
	  		break;
	  	case 2:
	  		c =   p.blue(GB.thePicture.pixels[index]);
	  		break;
	  	case 3:
	  		 c =   p.hue(GB.thePicture.pixels[index]);
	  		break;
	  	case 4:
	  		 c =   p.saturation(GB.thePicture.pixels[index]);
	  		break;
	  	case 5:
	  		c =   p.brightness(GB.thePicture.pixels[index]);
	  		break;
	  	}
		return c;
	}
	float getAngleFromAlt(PApplet p) {
		int index = (int) currentStrokePos.y;
		int x = (int) currentStrokePos.x;
	  	index = index*GB.picWidth;
	  	index = (int) (index+x);
	  	float c=0;
	  	switch (CF.velocityKind) {
	  	case 0: 
	  		c =  p.red(GB.altPicture.pixels[index]);
	  		break;
	  	case 1:
	  		c =   p.green(GB.altPicture.pixels[index]);
	  		break;
	  	case 2:
	  		c =   p.blue(GB.altPicture.pixels[index]);
	  		break;
	  	case 3:
	  		 c =   p.hue(GB.altPicture.pixels[index]);
	  		break;
	  	case 4:
	  		 c =   p.saturation(GB.altPicture.pixels[index]);
	  		break;
	  	case 5:
	  		c =   p.brightness(GB.altPicture.pixels[index]);
	  		break;
	  	}
		return c;
	}
	float getAngleFromWork(PApplet p) {
		int index = (int) currentStrokePos.y;
		int x = (int) currentStrokePos.x;
	  	index = index*GB.picWidth;
	  	index = (int) (index+x);
	  	float c=0;
	  	switch (CF.velocityKind) {
	  	case 0: 
	  		c =  p.red(GB.workCanvas.pixels[index]);
	  		break;
	  	case 1:
	  		c =   p.green(GB.workCanvas.pixels[index]);
	  		break;
	  	case 2:
	  		c =   p.blue(GB.workCanvas.pixels[index]);
	  		break;
	  	case 3:
	  		 c =   p.hue(GB.workCanvas.pixels[index]);
	  		break;
	  	case 4:
	  		 c =   p.saturation(GB.workCanvas.pixels[index]);
	  		break;
	  	case 5:
	  		c =   p.brightness(GB.workCanvas.pixels[index]);
	  		break;
	  	}
		return c;
	}
	float getAngleFromControl(PApplet p) {
		int index = (int) currentStrokePos.y;
		int x = (int) currentStrokePos.x;
	  	index = index*GB.picWidth;
	  	index = (int) (index+x);
	  	float c=0;
	  	switch (CF.velocityKind) {
	  	case 0: 
	  		c =  p.red(GB.controlCanvas.pixels[index]);
	  		break;
	  	case 1:
	  		c =   p.green(GB.controlCanvas.pixels[index]);
	  		break;
	  	case 2:
	  		c =   p.blue(GB.controlCanvas.pixels[index]);
	  		break;
	  	case 3:
	  		 c =   p.hue(GB.controlCanvas.pixels[index]);
	  		break;
	  	case 4:
	  		 c =   p.saturation(GB.controlCanvas.pixels[index]);
	  		break;
	  	case 5:
	  		c =   p.brightness(GB.controlCanvas.pixels[index]);
	  		break;
	  	}
		return c;
	}
	
	PVector getDirectionFromPic(PApplet p) {
		PVector direction = new PVector(0,0);
		for (int i = -3; i<4; i++) { // loop through the area around the current stroke position vertically for x horizontally for y blah blah - kind of tricky
			int px = (int) (currentStrokePos.x +i);
			for (int j= -3; j<4; j++) {
				int py = (int) (currentStrokePos.y +j);
				if( (px >= 0) && (px < GB.picWidth) && (py >= 0) && (py < GB.picHeight)) {//if all points are in picture
					int index = px + py*GB.picWidth;
					if ( (index >= 0)  && ( index < GB.thePicture.pixels.length)) {
					  	switch (CF.velocityKind) {
					  	case 0: 
							direction.x += i*(255 - p.red( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.red( GB.thePicture.pixels[index]));
					  		break;
					  	case 1:
							direction.x += i*(255 - p.green( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.green( GB.thePicture.pixels[index]));
					  		break;
					  	case 2:
							direction.x += i*(255 - p.blue( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.blue( GB.thePicture.pixels[index]));
					  		break;
					  	case 3:
							direction.x += i*(255 - p.hue( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.hue( GB.thePicture.pixels[index]));
					  		break;
					  	case 4:
							direction.x += i*(255 - p.saturation( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.saturation( GB.thePicture.pixels[index]));
					  		break;
					  	case 5:
							direction.x += i*(255 - p.brightness( GB.thePicture.pixels[index]));
							direction.y += j*(255 - p.brightness( GB.thePicture.pixels[index]));
					  		break;
					  	}
					}else {p.println("ugh another pixel out of range.......Stroke GetDirection....");}
				}
				else { direction = PVector.random2D();
						p.print(".");// this is a bad solution - the stroke should be killed if a proper direction cant be found....
						}
			}//end inner for
		}//end out for
		return direction;
	}
	PVector getDirectionFromControl(PApplet p) {
		PVector direction = new PVector(0,0);
		for (int i = -3; i<4; i++) { // loop through the area around the current stroke position vertically for x horizontally for y blah blah - kind of tricky
			int px = (int) (currentStrokePos.x +i);
			for (int j= -3; j<4; j++) {
				int py = (int) (currentStrokePos.y +j);
				if( (px >= 0) && (px < GB.picWidth) && (py >= 0) && (py < GB.picHeight)) {//if all points are in picture
					int index = px + py*GB.picWidth;
					if ( (index >= 0)  && ( index < GB.controlCanvas.pixels.length)) {
					  	switch (CF.velocityKind) {
					  	case 0: 
							direction.x += i*(255 - p.red( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.red( GB.controlCanvas.pixels[index]));
					  		break;
					  	case 1:
							direction.x += i*(255 - p.green( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.green( GB.controlCanvas.pixels[index]));
					  		break;
					  	case 2:
							direction.x += i*(255 - p.blue( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.blue( GB.controlCanvas.pixels[index]));
					  		break;
					  	case 3:
							direction.x += i*(255 - p.hue( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.hue( GB.controlCanvas.pixels[index]));
					  		break;
					  	case 4:
							direction.x += i*(255 - p.saturation( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.saturation( GB.controlCanvas.pixels[index]));
					  		break;
					  	case 5:
							direction.x += i*(255 - p.brightness( GB.controlCanvas.pixels[index]));
							direction.y += j*(255 - p.brightness( GB.controlCanvas.pixels[index]));
					  		break;
					  	}
					}else {p.println("ugh another pixel out of range.......Stroke GetDirection....");}
				}
				else { direction = PVector.random2D();
						p.print(".");// this is a bad solution - the stroke should be killed if a proper direction cant be found....
						}
			}//end inner for
		}//end out for
		return direction;
	}
	PVector getDirectionFromAlt(PApplet p) {
		PVector direction = new PVector(0,0);
		for (int i = -3; i<4; i++) { // loop through the area around the current stroke position vertically for x horizontally for y blah blah - kind of tricky
			int px = (int) (currentStrokePos.x +i);
			for (int j= -3; j<4; j++) {
				int py = (int) (currentStrokePos.y +j);
				if( (px >= 0) && (px < GB.picWidth) && (py >= 0) && (py < GB.picHeight)) {//if all points are in picture
					int index = px + py*GB.picWidth;
					if ( (index >= 0)  && ( index < GB.altPicture.pixels.length)) {
					  	switch (CF.velocityKind) {
					  	case 0: 
							direction.x += i*(255 - p.red( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.red( GB.altPicture.pixels[index]));
					  		break;
					  	case 1:
							direction.x += i*(255 - p.green( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.green( GB.altPicture.pixels[index]));
					  		break;
					  	case 2:
							direction.x += i*(255 - p.blue( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.blue( GB.altPicture.pixels[index]));
					  		break;
					  	case 3:
							direction.x += i*(255 - p.hue( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.hue( GB.altPicture.pixels[index]));
					  		break;
					  	case 4:
							direction.x += i*(255 - p.saturation( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.saturation( GB.altPicture.pixels[index]));
					  		break;
					  	case 5:
							direction.x += i*(255 - p.brightness( GB.altPicture.pixels[index]));
							direction.y += j*(255 - p.brightness( GB.altPicture.pixels[index]));
					  		break;
					  	}
					}else {p.println("ugh another pixel out of range.......Stroke GetDirection....");}
				}
				else { direction = PVector.random2D();
						p.print(".");// this is a bad solution - the stroke should be killed if a proper direction cant be found....
						}
			}//end inner for
		}//end out for
		return direction;
	}
	
	
	PVector getDirectionFromWork(PApplet p) {
		PVector direction = new PVector(0,0);
		for (int i = -3; i<4; i++) { // loop through the area around the current stroke position vertically for x horizontally for y blah blah - kind of tricky
			int px = (int) (currentStrokePos.x +i);
			for (int j= -3; j<4; j++) {
				int py = (int) (currentStrokePos.y +j);
				if( (px >= 0) && (px < GB.picWidth) && (py >= 0) && (py < GB.picHeight)) {//if all points are in picture
					int index = px + py*GB.picWidth;
					if ( (index >= 0)  && ( index < GB.workCanvas.pixels.length)) {
					  	switch (CF.velocityKind) {
					  	case 0: 
							direction.x += i*(255 - p.red( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.red( GB.workCanvas.pixels[index]));
					  		break;
					  	case 1:
							direction.x += i*(255 - p.green( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.green( GB.workCanvas.pixels[index]));
					  		break;
					  	case 2:
							direction.x += i*(255 - p.blue( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.blue( GB.workCanvas.pixels[index]));
					  		break;
					  	case 3:
							direction.x += i*(255 - p.hue( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.hue( GB.workCanvas.pixels[index]));
					  		break;
					  	case 4:
							direction.x += i*(255 - p.saturation( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.saturation( GB.workCanvas.pixels[index]));
					  		break;
					  	case 5:
							direction.x += i*(255 - p.brightness( GB.workCanvas.pixels[index]));
							direction.y += j*(255 - p.brightness( GB.workCanvas.pixels[index]));
					  		break;
					  	}
					}else {p.println("ugh another pixel out of range.......Stroke GetDirection....");}
				}
				else { direction = PVector.random2D();
						p.print(".");// this is a bad solution - the stroke should be killed if a proper direction cant be found....
						}
			}//end inner for
		}//end out for
		return direction;
	}
	
//
	void setTarketStrokeAge (PApplet p) {
		float hold = CF.speedLimit/2;			// a really rough really really rought - way to adjust age to be related to speed! so fast is not too long
		if(hold < .25) {hold = (float).25;}
		float sAge = CF.strokeAge/hold;			// by dividing 1/2 of speed into the age set in CF
		tarketStrokeAge = (int) sAge;	
		
		if(CF.strokeVaryControl != 6) {//6 is strokeVaryControl off
			//adjust tarket age based on one of hue/sat/bri - depending on pixel at start location
			int color = GB.thePicture.pixels[(int) ((startStrokePos.y*GB.picWidth)+startStrokePos.x)]; //get color of the center pixel
			int h = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
			int s = (color >> 8) & 0xFF;
			int b = color  & 0xFF;
			
			int rgb = Color.HSBtoRGB(h,s,b);
			int r = (int) p.red(rgb);
			int g = (int) p.green(rgb);
			int bl = (int) p.blue(rgb);
			
			if(CF.HSBinvert == true) {
				h = 255 -h;
				s = 255 -s;
				b = 255 -b;
				r = 255 -r;
				g = 255 -g;
				bl = 255 -bl;
			}
			
			float adjust = 0;
			
			switch (CF.strokeVaryControl) {
				case 0://hue
					adjustAge(h,sAge);
					break;
				case 1://sat
					adjustAge(s,sAge);
					break;	
				case 2://bri
					adjustAge(b,sAge);
					break;	
				case 3: //red
					adjustAge(r,sAge);
					break;
				case 4: //green
					adjustAge(g,sAge);
					break;
				case 5://blue
					adjustAge(bl,sAge);
					break;
				case 6://off so not much point in even having this code here... but whatever
					tarketStrokeAge = (int) sAge;
					break;
			}
		}//end of if there is some sort of strokeVaryControl turned on
		if (tarketStrokeAge < 1) tarketStrokeAge = 1;//just in case....
	}

	void adjustAge(float adjustValue, float sAge) {
		if (adjustValue > 126) { adjustValue = PApplet.map(adjustValue, 127, 255, 1, 2) * CF.strokeAgeVar;}
		else { adjustValue = PApplet.map(adjustValue, 0, 126, (float).5, (float).9999) / CF.strokeAgeVar;}		
		sAge = (int) (sAge * adjustValue);
		tarketStrokeAge = (int) sAge;
	}
	
	
//Update
	void updateStroke(PApplet p){
		strokeAge++;
		endStroke();
		if(strokeIsDead == false) {
			thisStrokesBrush.processBrush(p,currentStrokePos,strokeVelocity,strokeAge,thisStrokesTriNum); //process the brush at the current location of the stroke
		}
		if(strokeIsaZombie == false) {
			moveStroke(); //move the stroke to it's next location
		}
	}

	
//Move
	void moveStroke() {
		//the strokeAcceleration * CF.accelerationControl is added to the strokeVelocity
		PVector tempAcc = new PVector(strokeAcceleration.x,strokeAcceleration.y);
		tempAcc.mult(CF.accelerationControl);
		strokeVelocity.add(tempAcc);
		//a unit version of strokeVelocity is calculated
		PVector strokeVelUV = new PVector(strokeVelocity.x,strokeVelocity.y);
		strokeVelUV.normalize();
		//that unit velocity is limited/or increased by CF.speedLimit 
		PVector moveAmount = new PVector(strokeVelUV.x,strokeVelUV.y);
		moveAmount.mult(CF.speedLimit);
		//and then added to the currentStrokePos
		currentStrokePos.add(moveAmount);
		//and then returned to the strokeVelocy * CF.strokeSpeed
		strokeVelocity = strokeVelUV;
		strokeVelocity.mult(CF.strokeSpeed);
	}
	
	
	
//endStroke		
	void endStroke () {
		strokeOnScreen();
		if(strokeIsDead == false)  strokeTooOld();
//		if(isStrokeDead == false)  strokeNotMoving();
	}

	void strokeOnScreen() {
		//check if stroke is still on the screen
		if  ((currentStrokePos.x > GB.picWidth-GB.bW)  || (currentStrokePos.x < 0)) {strokeIsDead = true;}
		if  ((currentStrokePos.y > GB.picHeight-GB.bH) || (currentStrokePos.y < 0)) {strokeIsDead = true;}
//		if (isStrokeDead == true) PApplet.println("killed off screen");
	}
	
	void strokeTooOld() {
//		PApplet.println("strokeAge and tarketStrokeAge are "+strokeAge+" and "+tarketStrokeAge);
		if (strokeAge > tarketStrokeAge) {
			strokeIsDead = true;
			//but wait maybe it will come back to life  if it is a perlin line brush AND it says its a zombie
			if ((CF.brushType == 1)  && (CF.lineBrushType == 5) && (thisStrokesBrush.amIaZombie(strokeAge) == true)   ) { 
				strokeIsaZombie = true;
				strokeIsDead = false; // come back to life
			}
		}
	}
	
	void strokeNotMoving() {
		//need to figure this one out - but it would be nice to kill strokes that are stuck in a small area
		if ((tarketStrokeAge > 100) && (strokeAge >120)){
			float d = PVector.dist(startStrokePos,currentStrokePos);
			if (d < 100) {
				strokeIsDead = true;
				}
			}
		}
	
}//end Stroke base class


//****************************************************************************************************************
//     Stroke classes																							//
//****************************************************************************************************************

class curveStroke extends Stroke { 
	PVector variation;
	  curveStroke(PApplet p) {
		  super(p);
	  }
	  
		void updateStroke(PApplet p){
			strokeAge++;
			endStroke();
			if(strokeIsDead == false) {
				updateAcceleration();
				thisStrokesBrush.processBrush(p,currentStrokePos,strokeVelocity,strokeAge,thisStrokesTriNum);
				moveStroke(); 
			}
		}
		
		void updateAcceleration() {
			strokeAcceleration.rotate(CF.curveAmnt);
		}
}
	  





//the direction of the stroke is calculated based on the color of the pixel at the strokes CURRENT location
class contAngStroke extends Stroke {

	contAngStroke (PApplet p) {
	    super(p);
	  }
	//differs from the standard updateStroke only in that setAcceleration is called every time through loop  
	void updateStroke(PApplet p){
		strokeAge++;
		endStroke();
		if(strokeIsDead == false) {
			setAcceleration(p);
			thisStrokesBrush.processBrush(p,currentStrokePos,strokeVelocity,strokeAge,thisStrokesTriNum);
			moveStroke(); 
		}
	}
}



