package postContemp;

import java.util.ArrayList;

import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;


public class MeshMap {
	
	static int[] triColors;
	static float[] triAreas;
	static PVector[] triCenters;
	static ArrayList<Triangle> triangles;
	static Triangle[] zztriangles;
	static ArrayList<PVector> vertices;

	int bob =1;
	int move = 10;
	static float minarea = 1000;
	static float maxarea = 0;
	
	int threshold,res,mode;
	
	
	MeshMap (PApplet p, int mode, int threshold ,int res) {  // so simple!!!!
		makeverts(mode,threshold,res);
	}	
	
	public void makeverts(int mode, int threshold, int res) {
		GB.waitingforsomething = true;
		vertices = new ArrayList<PVector>();
	    EdgeDetector.extractPoints(vertices, GB.thePicture, mode, threshold, res);

	    if(CF2.addMeshBoarder == true) {//Add some points in the border of the canvas to complete all space
	    	int h = 0;
	    	int v = 0;
		    for (float i = 0; i<=1 ; i+=.05) {
		    	h = (int) (GB.picWidth*i);
		    	if (h > GB.picWidth) h = GB.picWidth;
		    	v = (int) (GB.picHeight*i);
		    	if (v > GB.picHeight) h = GB.picHeight;
		        vertices.add(new PVector(h, 0));
		        vertices.add(new PVector(h, GB.picHeight));
		        vertices.add(new PVector(0, v));
		        vertices.add(new PVector(GB.picWidth, v));
		    }
	    }
	    
	    //Get the triangles using qhull algorithm.
	    //The algorithm is a custom refactoring of Triangulate library by Florian Jennet (a port of Paul Bourke... not surprisingly... :D)
	    triangles = new ArrayList<Triangle>();
	    for (int i=0; i < triangles.size(); i++) {
        	triangles.remove(i);
	    }
	    
	    new Triangulator().triangulate(vertices, triangles);
	    PApplet.println("and i made "+triangles.size()+" triangles");
	    //Prune triangles with vertices outside of the canvas.
	    Triangle t = new Triangle();
	
	    for (int i=0; i < triangles.size(); i++) {
	        t = triangles.get(i);
	        if (vertexOutside(t.p1) || vertexOutside(t.p2) || vertexOutside(t.p3)) {
	        	PApplet.println ("taking out triangle "+i );
	        	triangles.remove(i);
	             
	        }
	    }
	   
	    //Get colors and centers for triangles
	    int tSize = triangles.size();
	    triColors = new int[tSize*3];
	    triCenters = new PVector[tSize];
	    PVector c = new PVector();
	    for (int i = 0; i < tSize; i++) {
	        c = triangles.get(i).center();
	        triCenters[i] = c;
	        triColors[i] = GB.thePicture.get((int)(c.x), (int)(c.y));
	    }
	    
//	    int tSize = triangles. size();
	    float triarea = 0;

	    //Calculate the area of each triangle
	    triAreas = new float[tSize];
	    for (int i = 0; i <tSize; i++) { 
	    	Triangle tr = triangles.get(i);
	    	PVector at = new PVector (tr.p1.x,tr.p1.y);
	    	PVector bt = new PVector (tr.p2.x,tr.p2.y);
	    	PVector ct = new PVector (tr.p3.x,tr.p3.y);
	    	
	    	triarea = area(at,bt,ct);
	    	triAreas[i] = triarea;
			if (triarea < minarea) {minarea = triarea;}
			if (triarea > maxarea) {maxarea = triarea;}

	    }
	    
	    //create a matching list of triangles that have a center of 0,0
	    zztriangles = new Triangle[tSize];
	    for (int i = 0; i <tSize; i++) {
	    	Triangle tr = triangles.get(i);
	    	PVector cent = triCenters[i];
	    	PVector a = new PVector(cent.x,cent.y);
	    	PVector b = new PVector(cent.x,cent.y);
	    	PVector d = new PVector(cent.x,cent.y);
	    	a.sub(tr.p1);
	    	b.sub(tr.p2);
	    	d.sub(tr.p3);
	    	zztriangles[i] = new Triangle(a.x,a.y,b.x,b.y,d.x,d.y);
	    }
 
	    GB.waitingforsomething = false;
	}// end makeverts

	public static void displaySomeMesh(PApplet p) {//writes the mesh to workCanvas using alpha values from CF.alphaVal of the master hsba
		switch (CF.whichMesh) {
		case 0:
			displaySolidMesh(p);
			break;
		case 1:
			displayBlobs(p);
			break;
		case 2:
			displayVerts(p);
			break;
		case 3:
		case 4:
			displaySpiro(p);
			break;
		}
	}


	public static void fillaTri(PApplet p,int triNumber) {
	    Triangle t = new Triangle();
	    GB.workCanvas.beginDraw();
	    int color;
	        t = triangles.get(triNumber);
	        color = triColors[triNumber];
	        GB.workCanvas.fill(255);
	        GB.workCanvas.noStroke();
	        GB.workCanvas.beginShape(p.TRIANGLES);
		        GB.workCanvas.vertex(t.p1.x, t.p1.y);
		        GB.workCanvas.vertex(t.p2.x, t.p2.y);
		        GB.workCanvas.vertex(t.p3.x, t.p3.y);
	        GB.workCanvas.endShape();
	    GB.workCanvas.endDraw();
		}
	
	public static void copyTriChunk(PApplet p,int triNumber) {
	    Triangle t = new Triangle();
	    int[] mmX = new int[2];
	    int[] mmY = new int[2];
	    t = triangles.get(triNumber);
	    mmX = minmax((int)t.p1.x,(int)t.p2.x,(int)t.p3.x);
	    mmY = minmax((int)t.p1.y,(int)t.p2.y,(int)t.p3.y);
	    
	    PImage mask = GB.workCanvas.get( mmX[0], mmY[0],  mmX[1]-mmX[0],  mmY[1]-mmY[0]);
	    PImage picC = null;
	    switch (CF.copyTrifromSource) {
	    case 0:
	    	picC = GB.thePicture.get( mmX[0], mmY[0],  mmX[1]-mmX[0],  mmY[1]-mmY[0]);
	    	break;
	    case 1:
	    	picC = GB.altPicture.get( mmX[0], mmY[0],  mmX[1]-mmX[0],  mmY[1]-mmY[0]);
	    	break;
	    case 2:
	    	picC = GB.controlCanvas.get( mmX[0], mmY[0],  mmX[1]-mmX[0],  mmY[1]-mmY[0]);
	    	break;
	    }
	    
	    PGraphics outblob = p.createGraphics(mask.width, mask.height);
	    outblob.beginDraw();
	    outblob.image(picC,0,0,mask.width,mask.height);
	    outblob.endDraw();
	    for (int h=0; h<mask.height; h++  ) {
	    	for ( int w=0; w<mask.width; w++) {
	    		int i = (h * mask.width) + w;
				int m = mask.pixels[i];
				int o = outblob.pixels[i];
//				p.println(" i "+i+" m "+m+" o "+ o);
				if (m != -1) {//then i don't want it
					outblob.pixels[i] = 0x00000000;
				}
//				int val = picC.pixels[i] & 0x00ffffff;   // tempBrush has a chunk of graphic be sure no alpha
//				picC.pixels[i] = val | m; // currentBrush gets the tempBrush color with the rawBrush alph
			}
		}
	    GB.outputCanvas.image(outblob,mmX[0],mmY[0],outblob.width,outblob.height);
	    GB.outputCanvas.removeCache(outblob);
 	}

	
	
	public static int findTriNum(PApplet p, PVector point) {//find the tri number the point is inside of
	    Triangle t = new Triangle();
	    int bob = -1; //going to return -1 if the point is not inside any triangle - this should be impossible, but i'm too lazy to figure it out
	    for (int i = 0; i < triangles.size(); i++)  //look at each triangle
	    {
	    	t = triangles.get(i);
	    	boolean well = pointInTri (t, point); //is the point inside the triangle?
	    	if (well == true) {bob = i;}          //yes well then bobs your uncle
	    }
		return bob;
	}
	
	public static void displaySolidMesh (PApplet p)//
	{
		
		 switch (CF.whichCanvas){
		 case 0: //pic
			 p.println("Can't writh mesh over the Picture");
			 break;
		 case 1: //alt
			 p.println("Can't writh mesh over the alt Picture");
			 break;
		 case 2: //work
			 GB.workCanvas.beginDraw();
			    Triangle t = new Triangle();
			    for (int i = 0; i < triangles.size(); i++)
			    {
			        t = triangles.get(i);
			        GB.workCanvas.fill(triColors[i],CF.meshAlpha);
			        GB.workCanvas.noStroke();
			        GB.workCanvas.beginShape(p.TRIANGLES);
				        GB.workCanvas.vertex(t.p1.x, t.p1.y);
				        GB.workCanvas.vertex(t.p2.x, t.p2.y);
				        GB.workCanvas.vertex(t.p3.x, t.p3.y);
			        GB.workCanvas.endShape();
			    }
		     GB.workCanvas.endDraw();
			 break;
		 case 3: //control
			 GB.controlCanvas.beginDraw();
			    t = new Triangle();
			    for (int i = 0; i < triangles.size(); i++) {
			        t = triangles.get(i);
			        GB.controlCanvas.fill(triColors[i],CF.meshAlpha);
			        GB.controlCanvas.noStroke();
			        GB.controlCanvas.beginShape(p.TRIANGLES);
				        GB.controlCanvas.vertex(t.p1.x, t.p1.y);
				        GB.controlCanvas.vertex(t.p2.x, t.p2.y);
				        GB.controlCanvas.vertex(t.p3.x, t.p3.y);
			        GB.controlCanvas.endShape();
			    }
		     GB.controlCanvas.endDraw(); 
			 break;
		 case 4: //output
			 GB.outputCanvas.beginDraw();
			    t = new Triangle();
			    for (int i = 0; i < triangles.size(); i++) {
			        t = triangles.get(i);
			        GB.outputCanvas.fill(triColors[i],CF.meshAlpha);
			        GB.outputCanvas.noStroke();
			        GB.outputCanvas.beginShape(p.TRIANGLES);
				        GB.outputCanvas.vertex(t.p1.x, t.p1.y);
				        GB.outputCanvas.vertex(t.p2.x, t.p2.y);
				        GB.outputCanvas.vertex(t.p3.x, t.p3.y);
			        GB.outputCanvas.endShape();
			    }
		     GB.outputCanvas.endDraw();
			 break;
		 }
	}
	
	
	public static void displayVerts(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 p.println("Cant write Verts over the Picture");
			 break;
		 case 1: //alt
			 p.println("Cant write Verts over the alt Picture");
			 break;
		 case 2: //work
				GB.workCanvas.beginDraw();
				for ( int i=0; i < vertices.size(); i++ ) {
					PVector vert = vertices.get(i);
					GB.workCanvas.noStroke();
					int size = (int) (CF2.mResolution * CF.vertSize *.01);
					int index = (int) getIndex(vert);
					if (index < GB.workCanvas.pixels.length) {
						int color = getMeAColor(index);
						GB.workCanvas.fill(color,CF.meshAlpha);
						GB.workCanvas.ellipse(vert.x, vert.y, size, size);
					}
				}
				GB.workCanvas.endDraw();
			 break;
		 case 3: //control
				GB.controlCanvas.beginDraw();
				for ( int i=0; i < vertices.size(); i++ ) {
					PVector vert = vertices.get(i);
					GB.controlCanvas.noStroke();
					int size = (int) (CF2.mResolution * CF.vertSize *.01);
					int index = (int) getIndex(vert);
					if (index < GB.controlCanvas.pixels.length) {
						int color = getMeAColor(index);
						GB.controlCanvas.fill(color,CF.meshAlpha);
						GB.controlCanvas.ellipse(vert.x, vert.y, size, size);
					}
				}
				GB.workCanvas.endDraw(); 
			 break;
		 case 4: //output
			 GB.outputCanvas.beginDraw();
				for ( int i=0; i < vertices.size(); i++ ) {
					PVector vert = vertices.get(i);
					GB.outputCanvas.noStroke();
					int size = (int) (CF2.mResolution * CF.vertSize *.01);
					int index = (int) getIndex(vert);
					if (index < GB.outputCanvas.pixels.length) {
						int color = getMeAColor(index);
						GB.outputCanvas.fill(color,CF.meshAlpha);
						GB.outputCanvas.ellipse(vert.x, vert.y, size, size);
					}
				}
				GB.outputCanvas.endDraw(); 
			 break;
		 }

	}
	


	public static void displayBlobs(PApplet p) {
		 switch (CF.whichCanvas){
		 case 0: //pic
			 p.println("can't write blobs over the picture"); 
			 break;
		 case 1: //alt
			 p.println("can't write blobs over the picture"); 
			 break;
		 case 2: //work
			 PVector cent = new PVector (0,0);
				GB.workCanvas.beginDraw();
			    for (int i = 1; i < triangles.size(); i++)   	
			    {
					float area = triAreas[i];
					int size = (int) p.sqrt((area/p.PI)*2);
					size = (int) (size * (CF.blobrSize * .01));
					GB.workCanvas.fill(triColors[i],CF.meshAlpha); 
					GB.workCanvas.noStroke();
						cent =  triangles.get(i).center();
						GB.workCanvas.ellipse (cent.x,cent.y,size,size);
						GB.workCanvas.noFill();
				}
			    GB.workCanvas.endDraw();
			 break;
		 case 3: //control
			 cent = new PVector (0,0);
				GB.controlCanvas.beginDraw();
			    for (int i = 1; i < triangles.size(); i++)   	
			    {
					float area = triAreas[i];
					int size = (int) p.sqrt((area/p.PI)*2);
					size = (int) (size * (CF.blobrSize * .01));
					GB.controlCanvas.fill(triColors[i],CF.meshAlpha); 
					GB.controlCanvas.noStroke();
						cent =  triangles.get(i).center();
						GB.controlCanvas.ellipse (cent.x,cent.y,size,size);
						GB.controlCanvas.noFill();
				}
			    GB.controlCanvas.endDraw();
			 break;
		 case 4: //output
			 cent = new PVector (0,0);
				GB.outputCanvas.beginDraw();
			    for (int i = 1; i < triangles.size(); i++)   	
			    {
					float area = triAreas[i];
					int size = (int) p.sqrt((area/p.PI)*2);
					size = (int) (size * (CF.blobrSize * .01));
					GB.outputCanvas.fill(triColors[i],CF.meshAlpha); 
					GB.outputCanvas.noStroke();
						cent =  triangles.get(i).center();
						GB.outputCanvas.ellipse (cent.x,cent.y,size,size);
						GB.outputCanvas.noFill();
				}
			    GB.outputCanvas.endDraw(); 
			 break;
		 }
		
		
	 }
	

	
	public static void displaySpiro(PApplet p) {
	    int ttSize = MeshMap.triangles.size();
	    int numSegments = CF.spiroSegs;
	    float increment = (float) 1/numSegments;
	    
	    switch (CF.whichCanvas){
		 case 0: //pic
			 p.println("can't write sprio's over the picture");
			 break;
		 case 1: //alt
			 p.println("can't write sprio's over the  alt picture"); 
			 break;
		 case 2: //work
			    GB.workCanvas.beginDraw();
			    for (int i = 1; i <ttSize; i++) {
			    	Triangle tr = MeshMap.triangles.get(i);
			    	PVector at = new PVector (tr.p1.x,tr.p1.y);
			    	PVector bt = new PVector (tr.p2.x,tr.p2.y);
			    	PVector ct = new PVector (tr.p3.x,tr.p3.y);
			    	
			    	PVector dist = new PVector (at.x,at.y);
			    	dist.sub(bt);
			    	dist.mult((float) increment);
			    	PVector point = new PVector (at.x,at.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {//*********************colorloaded
			    			int color = getMeAColor(index);
							GB.workCanvas.stroke(color,CF.meshAlpha);
					    	GB.workCanvas.line(ct.x, ct.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}
					
			    	dist = new PVector (bt.x,bt.y);
			    	dist.sub(ct);
			    	dist.mult((float)increment);
			    	point = new PVector (bt.x,bt.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length) {
			    			int color = getMeAColor(index);
							GB.workCanvas.stroke(color,CF.meshAlpha);
					    	GB.workCanvas.line(at.x, at.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}
			    	
			    	dist = new PVector (ct.x,ct.y);
			    	dist.sub(at);
			    	dist.mult((float)increment);
			    	point = new PVector (ct.x,ct.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {
			    			int color = getMeAColor(index);
				    		GB.workCanvas.stroke(color,CF.meshAlpha);
				    		GB.workCanvas.line(bt.x, bt.y, point.x-dist.x, point.y-dist.y);
				    		point.sub(dist);
			    		}
			    	}
			    	
			    }
			    GB.workCanvas.endDraw();
			 break;
		 case 3: //control
			    GB.controlCanvas.beginDraw();
			    for (int i = 1; i <ttSize; i++) {
			    	Triangle tr = MeshMap.triangles.get(i);
			    	PVector at = new PVector (tr.p1.x,tr.p1.y);
			    	PVector bt = new PVector (tr.p2.x,tr.p2.y);
			    	PVector ct = new PVector (tr.p3.x,tr.p3.y);
			    	
			    	PVector dist = new PVector (at.x,at.y);
			    	dist.sub(bt);
			    	dist.mult((float) increment);
			    	PVector point = new PVector (at.x,at.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {//*********************colorloaded
			    			int color = getMeAColor(index);
							GB.controlCanvas.stroke(color,CF.meshAlpha);
					    	GB.controlCanvas.line(ct.x, ct.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}					
			    	dist = new PVector (bt.x,bt.y);
			    	dist.sub(ct);
			    	dist.mult((float)increment);
			    	point = new PVector (bt.x,bt.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length) {
			    			int color = getMeAColor(index);
							GB.controlCanvas.stroke(color,CF.meshAlpha);
					    	GB.controlCanvas.line(at.x, at.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}
			    	dist = new PVector (ct.x,ct.y);
			    	dist.sub(at);
			    	dist.mult((float)increment);
			    	point = new PVector (ct.x,ct.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {
			    			int color = getMeAColor(index);
				    		GB.controlCanvas.stroke(color,CF.meshAlpha);
				    		GB.controlCanvas.line(bt.x, bt.y, point.x-dist.x, point.y-dist.y);
				    		point.sub(dist);
			    		}
			    	}
			    }
			    GB.controlCanvas.endDraw(); 
			 break;
		 case 4: //output
			    GB.outputCanvas.beginDraw();
			    for (int i = 1; i <ttSize; i++) {
			    	Triangle tr = MeshMap.triangles.get(i);
			    	PVector at = new PVector (tr.p1.x,tr.p1.y);
			    	PVector bt = new PVector (tr.p2.x,tr.p2.y);
			    	PVector ct = new PVector (tr.p3.x,tr.p3.y);
			    	
			    	PVector dist = new PVector (at.x,at.y);
			    	dist.sub(bt);
			    	dist.mult((float) increment);
			    	PVector point = new PVector (at.x,at.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {//*********************colorloaded
			    			int color = getMeAColor(index);
							GB.outputCanvas.stroke(color,CF.meshAlpha);
					    	GB.outputCanvas.line(ct.x, ct.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}
					
			    	dist = new PVector (bt.x,bt.y);
			    	dist.sub(ct);
			    	dist.mult((float)increment);
			    	point = new PVector (bt.x,bt.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length) {
			    			int color = getMeAColor(index);
							GB.outputCanvas.stroke(color,CF.meshAlpha);
					    	GB.outputCanvas.line(at.x, at.y, point.x-dist.x, point.y-dist.y);
					    	point.sub(dist);
			    		}
			    	}
			    	
			    	dist = new PVector (ct.x,ct.y);
			    	dist.sub(at);
			    	dist.mult((float)increment);
			    	point = new PVector (ct.x,ct.y);
			    	for(int j=0; j<numSegments; j++) {
			    		int index = (int) getIndex(point);
			    		if(index <GB.controlCanvas.pixels.length && index > 0) {
			    			int color = getMeAColor(index);
				    		GB.outputCanvas.stroke(color,CF.meshAlpha);
				    		GB.outputCanvas.line(bt.x, bt.y, point.x-dist.x, point.y-dist.y);
				    		point.sub(dist);
			    		}
			    	}
			    }
			    GB.outputCanvas.endDraw(); 
			 break;
		 }
 	}
	
	
static int getMeAColor (int index) {
	int color = 0;
	switch (CF.loadBrushFrom) {
	case 0:
		color = GB.thePicture.pixels [index];
		break;
	case 1:
		color = GB.altPicture.pixels [index];
		break;
	case 2:
		color = GB.workCanvas.pixels [index];
		break;
	case 3:
		color = GB.controlCanvas.pixels [index];
		break;
	}
	return color;
}


//Utility function to prune triangles with vertices out of bounds 
boolean vertexOutside(PVector v) { 
	return v.x < 00 || v.x > (GB.picWidth) || v.y < 0 || v.y > (GB.picHeight); 
} 

// function to calculate area of a triangle
static float area (PVector a, PVector b, PVector c) {
	float area = 0;
	float chunk1 = a.x * (b.y-c.y);
	float chunk2 = b.x * (c.y-a.y);
	float chunk3 = c.x * (a.y-b.y);
	area = PApplet.abs(   (chunk1 + chunk2 + chunk3)  /2   );
	return area;
}

static boolean pointInTri (Triangle t, PVector p) {
	if ( (area(t.p1,t.p2,p)+ area(t.p2,t.p3,p) + area(t.p1,t.p3,p)) == area(t.p1,t.p2,t.p3) ) {
		return true;
	}else {
		return false;
	}
}

static int[] minmax (int a, int b, int c) {
	int[] mima; 
	mima= new int[2];
	int min, max;
	
	if( a > b ){
		 if( a > c ){
			  max = a;
			  if( b > c ){  min = c;}
			  else{  		min = b;}
		 }else{
			  if( b > c ){  max = b;  min = c;}
			  else{   		max = c;  min = b;}
		 }
	}else{
		 if( b > c ){
			  max = b;
			  if( a > c ){  min = c;}
			  else{ 	    min = a;}
		 }else{
			  max = c;	  
			  min = a;
		 }
	}
	mima[0] = min;
	mima[1] = max;
	return mima;
}



static float getIndex (PVector a) {
	return (int)(a.y*GB.picWidth+a.x);

}


}
