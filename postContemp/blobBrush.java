package postContemp;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

class blobBrush extends Brush {

	int thisBlobsTriNum,lastBlobsTriNum;
	float perx,pery,diff;
	
	PGraphics elBrush;
	
	//for the bezier ellipse shapes
	int pts = 4;
	float[] px, py, cx, cy, cx2, cy2;
	
	blobBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge) {
	    super(p, startStrokePos, startVel, tarketAge);

	    //blob brushes need a special kind of brush - elBrush and are 'drawn' by createBrushShape
	    elBrush = p.createGraphics(brushW, brushH);	
	    createBrushShape(p);
		rotateBrush();
		sizeBrush(p);
		tintBrush();
		moveBrush(p,startStrokePos);
		masterHsbBrush(p);
	}



	public void processBrush(PApplet p, PVector pos,PVector strokeVelocity, int _strokeAge, int triNum) {
		strokeAge = _strokeAge;
//		thisBlobsTriNum = triNum;
		
//		p.println("tempBrush "+tempBrush.pixels.length + "   elBrush "+elBrush.pixels.length +"   currentBrush "+currentBrush.pixels.length);

		rotateBrush();
		sizeBrush(p);
		tintBrush();
		moveBrush(p,pos); 

		if((CF.blobBrushType == 0) ||(CF.blobBrushType == 2) || (CF.blobBrushType == 4) ){//crap ---- but this in the subClasses
			loadBrush(p);//keep loading the thing
		}

		blendBrush(p);
		shapeBrush(p);
		drawBrush(p);

	}

	
	
	void loadBrush(PApplet p) {
    	int x = locX;
    	int y = locY;
		switch (CF.loadBrushFrom) {
		case 0:
			tempBrush = GB.thePicture.get(x,y,brushW,brushH);
			break;
		case 1:
			tempBrush = GB.altPicture.get(x,y,brushW,brushH);
			break;
		case 2:
			tempBrush = GB.workCanvas.get(x,y,brushW,brushH);
			break;
		case 3:
			tempBrush = GB.controlCanvas.get(x,y,brushW,brushH);
			break;
		}
    }



	
	void shapeBrush (PApplet p) {   // how is this different from the shapeBrush in Brush???**************
		currentBrush.loadPixels();
			for(int h=0; h < brushH; h++) { // for every pixel in the brush
				for( int w=0; w < brushW; w++) {
					int index = (h * brushW)+w;
					int mask = elBrush.pixels[index] <<24;  //shift over and use blue as alpha?
					int val = tempBrush.pixels[index] & 0x00ffffff;   // tempBrush has a chunk of graphic be sure no alpha
					currentBrush.pixels[index] = val | mask; // currentBrush gets the tempBrush color with the rawBrush alph
				}
			}
		currentBrush.updatePixels();
	}

	
	void blendBrush(PApplet p) {//unless it's a blendBlobBrush (which has it's own class below) do nothing
	}
	
	
	
	int setColor() {
		int c=0;
		switch (CF.blobShape) {
		case 0://single
			c= MeshMap.triColors[thisBlobsTriNum];
			break;
		case 1://age
		case 2://fixed
			c = GB.controlCanvas.pixels[(locY*GB.picWidth)+locX] ;
			break;
		case 3://image
			c = GB.controlCanvas.pixels[(locY*GB.picWidth)+locX] ;///*********************888temp
			break;
		}
		return c;
	}
	
	
	void sizeBrush(PApplet p) { 

		
		if(CF.sizeBytri == true) {
			//get the size of the tri beneath the current location
			PVector here = new PVector(locX,locY);
			int triNumber = MeshMap.findTriNum(p, here);
			if (triNumber == -1) {
				p.println("got a bad tri number " +triNumber +" at location "+locX+" "+locY);
				triNumber = 1;
			}
			int triArea = (int) MeshMap.triAreas[triNumber];
			int size = (int) p.sqrt((triArea/p.PI)*2);
			sizeX = (int) (size * (CF.blobSize * .01));

			if (sizeX > CF.blobSize) { sizeX = CF.blobSize;}
			sizeY = sizeX;

		}else {
			sizeX = CF.blobSize;
			sizeY = CF.blobSize;
		}
		
		// now size by HSB
		if(CF.sizehsbType != -1) {//-1 means nothing is set so don't size by type at all
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

	void createBrushShape(PApplet p) {
		int backc = p.color(0,0);
		int forc = p.color(255,255);
		elBrush.noStroke();				//could get some neat effects with stroke on or off**************
		elBrush.beginDraw();
		switch (CF.blobShape) {
			case 0://ellipse
	//			elBrush.loadPixels();
				elBrush.fill(backc);
				elBrush.rect(0,0,brushW, brushH);
				elBrush.fill(forc);
				elBrush.ellipse(brushW/2,brushH/2,brushW,brushH);
				break;
			case 1://rectangle
				elBrush.fill(forc);
				elBrush.rect(0,0,brushW,brushH);
				break;
			case 2://triangle
				elBrush.fill(backc);
				elBrush.rect(0,0,brushW, brushH);
				elBrush.fill(forc);
				elBrush.beginShape();
					elBrush.vertex(0,p.random(0,brushH));
					elBrush.vertex(brushW, p.random(0,brushH));
					elBrush.vertex(p.random(0,brushW), p.random(0,brushH));
				elBrush.endShape();
				break;

			case 3://random shape
				elBrush.fill(backc);
				elBrush.rect(0,0,brushW, brushH);
				
					float[] tid = new float[12];
					float angle = p.PI / 4;
					int numVertices = 8;
					int part = 3;

					  p.noStroke();
					  for(int i = 0; i < numVertices; i++)
					  {
					    tid[i] = p.random(0, 1);
					  }
	
					 // elBrush.fill( (int)(p.noise(tid[1]) * 360), 255, 200);
					  elBrush.fill(forc);
					  elBrush.beginShape();
					  for(int i = 0; i < numVertices; i++) 
					  {
					    float xpos = brushW/part + brushW/part * p.cos((angle * i)) + p.noise(tid[i]) * brushW/part;
					    float ypos = brushH/part + brushW/part * p.sin((angle * i)) + p.noise(tid[(i + numVertices - 1) % numVertices]) * brushW/part;
					    elBrush.curveVertex(xpos, ypos);
					  }
					  for(int i = 0; i < 3; i++) 
					  {
					    float xpos = brushW/part + brushW/part * p.cos((angle * i)) + p.noise(tid[i]) * brushW/part;
					    float ypos = brushW/part + brushW/part * p.sin((angle * i)) + p.noise(tid[(i + numVertices - 1) % numVertices]) * brushW/part;
					    elBrush.curveVertex(xpos, ypos);
					  }  
					  elBrush.endShape(p.CLOSE);
					  for(int i = 0; i < numVertices; i++) 
					  {
					    tid[i] += p.random(0, (float) 0.01);
					  }
				break;
			case 4://bezier ellipse
				pts = CF.numberOfPoints;
				float blobRadius = CF.blobRadius;
				float controlRadius = CF.controlBlobRadius;
				setEllipse(p,pts,blobRadius,controlRadius);
	//				elBrush.stroke(255,0,0,255);
	//				elBrush.rect(0,0,brushW,brushH);
				 elBrush.fill(forc);
				 elBrush.beginShape();
				    for (int i=0; i<pts; i++){
				        if (i==pts-1) {
				          elBrush.bezier(px[i], py[i], cx[i], cy[i], cx2[i], cy2[i],  px[0], py[0]);
				        }
				        else{
				          elBrush.bezier(px[i], py[i], cx[i], cy[i], cx2[i], cy2[i],  px[i+1], py[i+1]);
				        }
				      }
				    elBrush.endShape();
				break;
		}
		elBrush.endDraw();		
	}//end createBrush
		
		void setEllipse(PApplet p, int points, float radius, float controlRadius){
			  int pts = points;
			  
			  px = new float[points];
			  py = new float[points];
			  cx = new float[points];
			  cy = new float[points];
			  cx2 = new float[points];
			  cy2 = new float[points];
			  float angle = (float) (360.0/points);
			  float controlAngle1 = (float) (angle/3.0);
			  float controlAngle2 = (float) (controlAngle1*2.0);
			  for ( int i=0; i<points; i++){
			    px[i] = brushW/2+p.cos(p.radians(angle))*radius;
			    py[i] = brushH/2+p.sin(p.radians(angle))*radius;
			    cx[i] = brushW/2+p.cos(p.radians(angle+controlAngle1))* 
			      controlRadius/p.cos(p.radians(controlAngle1));
			    cy[i] = brushH/2+p.sin(p.radians(angle+controlAngle1))* 
			      controlRadius/p.cos(p.radians(controlAngle1));
			    cx2[i] = brushW/2+p.cos(p.radians(angle+controlAngle2))* 
			      controlRadius/p.cos(p.radians(controlAngle1));
			    cy2[i] = brushH/2+p.sin(p.radians(angle+controlAngle2))* 
			      controlRadius/p.cos(p.radians(controlAngle1));
			    // Increment angle so trig functions keep chugging along
			    angle+=360.0/points;
			  }
		}
}//end blobBrush


class smudgeBlobBrush extends blobBrush{
	
	smudgeBlobBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge) {
	    super(p, startStrokePos, startVel, tarketAge);
		    //percentage of smudge calculation - make slider be a percent - then account for the number of times brush will be used
			perx = CF.smudgePercent/100/CF.strokeAge;
			pery = 1 - perx;  
			diff = perx;
			for(int h=0; h < brushH; h++) { // for every pixel in the brush
				for( int w=0; w < brushW; w++) {
					int index = (h * brushW)+w;
					int mask = elBrush.pixels[index] <<24;
					int val = tempBrush.pixels[index] & 0x00ffffff;
					currentBrush.pixels[index] = tempBrush.pixels[index] ; // currentBrush gets the tempBrush (which was loaded in the super constructor
					currentBrush.pixels[index] = val |mask;
				}
			}
			GB.outputCanvas.image(currentBrush,locX,locY,brushW,brushH); //**********************why do i need this - without it all the brushes start black????
	  }
	
	void shapeBrush(PApplet p) {
		currentBrush.loadPixels();
		perx = perx + diff;
		pery = pery - diff;
		for(int h=0; h< brushH; h++) { // for every pixel in the brush
			for( int w=0; w< brushW; w++) {
				int index = (h*brushW)+w;
				int mask = elBrush.pixels[index] <<24; // use the blue value of elBrush pixel as the alpha channel
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
		currentBrush.updatePixels();
	}
}



class paintBlobBrush extends blobBrush {

	paintBlobBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge) {
	    super(p, startStrokePos, startVel, tarketAge);
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

		int index = (brushH/2*brushW)+brushW/2;
		if (index > tempBrush.pixels.length  || index <0 ) { index = 1;}
		int color = tempBrush.pixels[index]; //get color of the center pixel
		int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
		int g = (color >> 8) & 0xFF;
		int b = color  & 0xFF;
		float[] hsb = Color.RGBtoHSB(r,g,b,null);
		
		float hrangemax = CF.HbristleVar/100;
		float hrangemin = -hrangemax;
		float huedelta = (float).000005;
		
		float srangemax = CF.SbristleVar/100;
		float srangemin = -srangemax;
		float suedelta = (float).000005;
		
		float brangemax = CF.BbristleVar/100;
		float brangemin = -brangemax;
		float buedelta = (float).000005;
		
		for(int h=0; h < tempBrush.pixels.length ; h++) { // for every pixel in the brush set to that color
				huedelta = huedelta + (p.random((float)-.0000005,(float).0000005));
				hueshift = hueshift + huedelta; 
				if (hueshift > hrangemax || hueshift < hrangemin) huedelta = huedelta*-1;
				float hue = hsb[0]+ hueshift ;
				if (hue <0) hue = hue+1;
				if (hue >1) hue = hue-1;
				
				suedelta = suedelta + (p.random((float)-.0000005,(float).0000005));
				satshift = satshift + suedelta;
				if (satshift > srangemax || satshift < srangemin) suedelta = suedelta*-1;
				float sat = hsb[1]+ satshift;
				sat = p.constrain(sat,0,1);
				
				buedelta = buedelta + (p.random((float)-.0000005,(float).0000005));
				brishift = brishift + buedelta;
				if (brishift > brangemax || brishift < brangemin) buedelta = buedelta*-1;	
				float bri = hsb[2]+brishift;
				bri = p.constrain(bri,0,1);
				
				color = Color.HSBtoRGB(hue, sat, bri);
				tempBrush.pixels[h] = color; //color;
		}
		tempBrush.updatePixels();

	}
}//end paintBlobBrush



class blendBlobBrush extends blobBrush {

	blendBlobBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge) {
	    super(p, startStrokePos, startVel, tarketAge);
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

class randomBlobBrush extends blobBrush {
	int tempcolor,tempr,tempg,tempb;
	
	randomBlobBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge, int strokesTriNum) {
		
		
	    super(p, startStrokePos, startVel, tarketAge);//strokesTriNum);

		if ( strokesTriNum > MeshMap.triAreas.length-1) p.println("index "+strokesTriNum+" is going to crash in randomBlobBrush "+ MeshMap.triAreas.length);
		
		int size = (int) p.sqrt((MeshMap.triAreas[strokesTriNum]/p.PI)*2);
	    brushW = ( size + CF.blobSize)/2;
	    brushH = brushW;
	    
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
	    
	    elBrush = p.createGraphics(brushW, brushH);	
	    createBrushShape(p);
		rotateBrush();
		sizeBrush(p);
		tintBrush();
		moveBrush(p,startStrokePos);
		masterHsbBrush(p);
	}
	
	public void processBrush(PApplet p, PVector pos,PVector strokeVelocity, int _strokeAge, int triNum) {
		strokeAge = _strokeAge;

		rotateBrush();
		sizeBrush(p);
		tintBrush();
		moveBrush(p,pos); 
		shapeBrush(p);
		drawBrush(p);

	}
	
	void drawBrush (PApplet p){
		GB.outputCanvas.imageMode(p.CENTER);
		GB.outputCanvas.pushMatrix();
			GB.outputCanvas.translate(locX+(brushW/2),locY+(brushH/2));
			GB.outputCanvas.rotate (brushAngle);
			GB.outputCanvas.tint(tempr,tempg,tempb,CF.alphaVal);
			GB.outputCanvas.image(currentBrush,0,0,sizeX,sizeY);//
			GB.outputCanvas.removeCache(currentBrush); //****** delete from memory manually or CRASH because of Java heap
		GB.outputCanvas.popMatrix();
		GB.outputCanvas.imageMode(p.CORNER);//so all the other places that draw to myCanvas aren't all off....
	}
	
	void loadBrush(PApplet p) {
		
		int ranx = (int) p.random(0+brushW,GB.picWidth-brushW);
		int rany = (int) p.random(0+brushH,GB.picHeight-brushH);
		switch ((int)p.random(0,3)) {
		case 0:
			tempBrush = GB.thePicture.get(ranx,rany,brushW,brushH);
			break;
		case 1:
			tempBrush = GB.altPicture.get(ranx,rany,brushW,brushH);
			break;
		case 2:
		case 3:
			tempBrush = GB.controlCanvas.get(ranx,rany,brushW,brushH);
			break;
		}
		tempcolor = tempBrush.pixels[(brushH/2*brushW)+brushW/2]; //get color of the center pixel
		tempr = (tempcolor >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
		tempg = (tempcolor >> 8) & 0xFF;
		tempb = tempcolor  & 0xFF;
	}

}//end randomBlobBrush
