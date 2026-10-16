package postContemp;

import java.awt.Color;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class lineBrush extends Brush{

	ArrayList <perlinParticles> perlin_points = new ArrayList<perlinParticles> ();
	float elapsedFrames = 0f;
	boolean thisLineisaZombie;
	PVector vel;
	int spx,spy;
	
		lineBrush (PApplet p, PVector startStrokePos, PVector startVel, int tarketAge) {
		    super(p, startStrokePos, startVel, tarketAge);
		    spx = (int) startStrokePos.x;
		    spy = (int) startStrokePos.y;
		    brushW = 8;
		    brushH = 8;
		    thisLineisaZombie = false;
		}

		public Boolean amIaZombie(int age) {
			if((perlin_points.size()!= 0) && (age != 0) ) {
				thisLineisaZombie = true;
			}else {
				thisLineisaZombie = false;
			}
			return thisLineisaZombie;
		}

		public void processBrush(PApplet p, PVector pos, PVector strokeVelocity, int _strokeAge, int triNum) {
			strokeVel = strokeVelocity;
//			int px = (int) pos.x;
			int px = spx;
			int py = spy;
//			int py = (int) pos.y;
			int color =0;
			int sourceAlpha = 0;
			
			switch (CF.loadAlphaFrom) {//get the brightnes of the pixel from the appropriate canvas or picture  // it will be 
			case 0:
				color = GB.thePicture.pixels[(py*GB.picWidth)+px] ;
				int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
				int g = (color >> 8) & 0xFF;
				int b = color  & 0xFF;
				float[] hsb = Color.RGBtoHSB(r,g,b,null);
				sourceAlpha = (int) (hsb[2]*255);
				break;
			case 1:
				color = GB.altPicture.pixels[(py*GB.picWidth)+px] ;
				r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
				g = (color >> 8) & 0xFF;
				b = color  & 0xFF;
				hsb = Color.RGBtoHSB(r,g,b,null);
				sourceAlpha = (int) (hsb[2]*255);
				break;
			case 2:
				color = GB.workCanvas.pixels[(py*GB.picWidth)+px] ;
				r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
				g = (color >> 8) & 0xFF;
				b = color  & 0xFF;
				hsb = Color.RGBtoHSB(r,g,b,null);
				sourceAlpha = (int) (hsb[2]*255);
				break;
			case 3:
				color = GB.controlCanvas.pixels[(py*GB.picWidth)+px] ;
				r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
				g = (color >> 8) & 0xFF;
				b = color  & 0xFF;
				hsb = Color.RGBtoHSB(r,g,b,null);
				sourceAlpha = (int) (hsb[2]*255);
				break;
			}
			sourceAlpha = 255 - sourceAlpha;
			
			
			switch (CF.loadBrushFrom) {
			case 0:
				color = GB.thePicture.pixels[(py*GB.picWidth)+px] ;
				break;
			case 1:
				color = GB.altPicture.pixels[(py*GB.picWidth)+px] ;
				break;
			case 2:
				color = GB.workCanvas.pixels[(py*GB.picWidth)+px] ;
				break;
			case 3:
				color = GB.controlCanvas.pixels[(py*GB.picWidth)+px] ;
				break;
			}

			color = varyColor(p,color);
			drawLine(p,color,pos,strokeVel,sourceAlpha);
			strokesOldPos = new PVector (pos.x,pos.y) ; // save the current stroke position in old position 
			
		}///end processBrush
		

		
		//******************************************************************shares too much with masterHSB - should break off into functions....
		protected static int varyColor(PApplet p, int color) {
			int r = (color >> 16) & 0xFF;  //shift and and to get the rgb of the tempBrush pixel
			int g = (color >> 8) & 0xFF;
			int b = color  & 0xFF;
			float hueshift = p.map(CF.hueShift,-127,127,(float)-.5,(float).5);
			float satshift = (float) (CF.satShift *.01); // convert satShift to a percent     was p.map(CF.satShift,-127,127,(float)-.5,(float).5);
			float brishift = (float) (CF.briShift * .01); //p.map(CF.briShift,-127,127,(float)-.5,(float).5);
		
//			float hueshift = p.map(CF.hueShift,-127,127,(float)-.5,(float).5);
//			float satshift = p.map(CF.satShift,-127,127,(float)-.5,(float).5);
//			float brishift = p.map(CF.briShift,-127,127,(float)-.5,(float).5);
			
			float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1

			//add in the brush randomization factor...
			if(CF.hcran == true) {
				float hrf = p.random(CF.HueRanLow,CF.HueRanHig);
				 hsb[0] = hsb[0] +hrf;		
				 if (hsb[0] >1) hsb[0]=hsb[0]-1;
				 if (hsb[0] <0) hsb[0]=hsb[0]+1;
			}
			if(CF.scran == true) {
				float srf = p.random(CF.SatRanLow,CF.SatRanHig)/2; //divided by two because they can be between -1 and 1
				hsb[1] = hsb[1] +srf;
			}
			if(CF.bcran == true) {
				float brf = p.random(CF.BriRanLow,CF.BriRanHig)/2;
				 hsb[2] = hsb[2] +brf;
			}

			float hue1 = hsb[0]+hueshift;
			if (hue1 > 1) hue1=hue1-1;
			if (hue1 < 0) hue1=hue1+1;
			float sat1 = p.constrain( hsb[1]+satshift, 0, 1);
			float bri1 = p.constrain( hsb[2]+brishift, 0, 1);

			color = Color.HSBtoRGB(hue1, sat1, bri1);// which should be back to numbers between 0 and 255
		
		   return(color);
		}//end varyColor

		


		private void drawLine (PApplet p, int color,PVector pos,PVector vel,int sourceAlpha) {
			
			GB.outputCanvas.strokeWeight(CF.strokeWeight);
			GB.outputCanvas.strokeCap(p.ROUND);
			GB.outputCanvas.strokeJoin(p.MITER);
			
			int alpha = (CF.alphaVal + sourceAlpha) /2;
			GB.outputCanvas.stroke(color,alpha);
//			GB.outputCanvas.stroke(color,CF.alphaVal);
			
			switch (CF.lineBrushType) {
			case 0://single
				GB.outputCanvas.line(pos.x, pos.y, strokesOldPos.x, strokesOldPos.y);
				break;
			case 1://fan
				PVector f = new PVector (pos.x,pos.y);
				f.sub(strokesOldPos);
				PVector g = new PVector (pos.x,pos.y);
				for (int i=0; i<CF.fanNumLines; i++) {
					f.rotate(i*(p.TWO_PI/CF.fanNumLines));
					f.normalize();
					f.mult(CF.fanLineLength);
					g = new PVector (pos.x,pos.y);
					g.add(f);
					GB.outputCanvas.line(pos.x, pos.y, g.x, g.y);
				}
				break;
			case 2://scatter
				PVector s = new PVector (pos.x,pos.y);
				s.sub(strokesOldPos);
				PVector t = new PVector (pos.x,pos.y);
				for (int i=0; i<CF.fanNumLines; i++) {
					s.rotate(i*(p.TWO_PI/p.random(p.TWO_PI)));
					s.normalize();
					s.mult(CF.fanLineLength);
					g = new PVector (p.random(-CF.fanLineLength,CF.fanLineLength) ,p.random(-CF.fanLineLength,CF.fanLineLength));
//					g.normalize();
//					g.mult(CF.fanLineLength);
					GB.outputCanvas.line(pos.x+g.x  ,pos.y+g.y , pos.x+s.x, pos.y+s.y);
				}
				break;
			case 3://parallel
	//			GB.outputCanvas.line(pos.x, pos.y-10, strokesOldPos.x, strokesOldPos.y-10);
				GB.outputCanvas.line(pos.x, pos.y, strokesOldPos.x, strokesOldPos.y);
				int greyscale = color  & 0xFF;
				GB.outputCanvas.stroke(greyscale,alpha);
				GB.outputCanvas.line(pos.x+100, pos.y+100, strokesOldPos.x+100, strokesOldPos.y+100);
				break;

			case 4://drop
				GB.outputCanvas.line(pos.x, pos.y, strokesOldPos.x, strokesOldPos.y);
				
				GB.outputCanvas.strokeWeight(CF.strokeWeight*CF.dropSize);
				GB.outputCanvas.stroke(color,alpha*CF.dropAlpha);
				GB.outputCanvas.line(pos.x+CF.dropXdist, pos.y+CF.dropYdist, strokesOldPos.x+CF.dropXdist, strokesOldPos.y+CF.dropYdist);
				break;
			case 5:// perlin
				//debug so i can see where the base line is
				//GB.outputCanvas.stroke(color,255);
				//GB.outputCanvas.strokeWeight(5);
				//GB.outputCanvas.line(pos.x, pos.y, strokesOldPos.x, strokesOldPos.y);

				 //make a new particle - if the stroke is still happening - otherwise just update
				if (thisLineisaZombie == false ) {
				    PVector vel1 = new PVector(0,0);
				    PVector cPos = new PVector (pos.x,pos.y);
				    perlinParticles pp = new perlinParticles(p, cPos, vel1,CF.perLength);
				    perlin_points.add(pp);
				}
			 //kill old particles   
			    for(int i = 0; i < perlin_points.size(); i++){
			    	perlinParticles localPoint = perlin_points.get(i);
			       if(localPoint.isDead == true){   perlin_points.remove(i);}
			   //update the particles and draw them     
			       else {localPoint.update(p);
			       		localPoint.fpdrw(p,color,alpha);}
			    }

			  // count time  
			    elapsedFrames++;
				break;
			}

		
		}

		
		class perlinParticles {
		    PVector particlePos;
		    PVector vel;
		    PVector noiseVec;
			float noiseFloat;
			float lifeTime;
			float age;
			boolean isDead;
			
			perlinParticles (PApplet p, PVector _pos, PVector _vel, float _lifeTime) {
				particlePos = _pos;
				vel = _vel;
				lifeTime = _lifeTime;
				age = 0;
				isDead = false;
				noiseVec = new PVector();
				
				p.noiseDetail(CF.perlinOctaves, CF.perlinFade);
			}

			//****************************************************************************************fix this up
			//noise - is perli noise********need to get control over the various multipliers
			 void update(PApplet p){
			    noiseFloat = p.noise(particlePos.x * CF.Xnoiseyness/100, particlePos.y * CF.Ynoiseyness/100, elapsedFrames * CF.Znoiseyness/100);//0.001f);
				noiseVec.x = p.cos((float) (((noiseFloat - .3f) * p.TWO_PI) * CF.curlyness));
			    noiseVec.y = p.sin((float) (((noiseFloat - .3f) * p.TWO_PI) * CF.curlyness));
				   
			    vel.add(noiseVec);
			    vel.mult(CF.spacing);
			    particlePos.add(vel);
				    
			    if (lifeTime-age == 0) {//(1.0-(age/lifeTime) == 0)	{
			    	isDead = true;
			    	}
			    if(particlePos.x < 0 || particlePos.x > GB.picWidth || particlePos.y < 0 || particlePos.y > GB.picHeight) {
			    	isDead = true;
			    	}
			    age++;   
			  }
				   
			 void fpdrw(PApplet p, int color,int alpha){   
//				float spot = 0;
//				int h = (int) (spot/GB.picWidth*255);
//			    int s = (int) 255 - p.abs (	(int) (p.map	(particlePos.x ,0 , GB.picHeight,-255,255))	);
//			    int b = (int) p.abs((p.map(particlePos.y,0,p.height,-255,255)));
//			    p.fill(h,s,b,30);;
//			    p.fill(h,s,255);
				 int index = (int) ((particlePos.y*GB.picWidth)+particlePos.x);
				 if (index > 0  && index < GB.picWidth*GB.picHeight) { //then we are in bounds otherwise just ignore
					 //color = GB.thePicture.pixels[index] ;
					 float temp = 1 - (age/lifeTime);
					 float tempalpha = (float) (255 * temp);
					 GB.outputCanvas.fill(color,10);
					 GB.outputCanvas.noStroke();
					 GB.outputCanvas.ellipse(particlePos.x, particlePos.y, 1, 1);
				 }
			 }
		}

	}//end lineBrush