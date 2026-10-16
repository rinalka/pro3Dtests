package postContemp;

import controlP5.Bang;
import controlP5.CheckBox;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Radio;
import controlP5.RadioButton;
import controlP5.Slider;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class CF2 extends PApplet {
	  int w, h;
	  ControlP5 cp5a;
   	  Object parent;
   	  
   	  static RadioButton runRB;
   	  static RadioButton meshRB;
   	  static Bang fullpic,fullzoom,zoomin,zoomout,meshRecB;
   	  static Slider meshThreshRB,mesResolRB;
   	  static Slider blurS,postS;
   	  static Slider edgeSl1,edgeSl2,edgeSl3,edgeSl4;
   	  static Slider interval;
   	  
   	  static CheckBox meshAddBoarderCB;
   	  static Bang doEdgeNow;
  	  static int mResolution = 40;
  	  static int mThreshold = 100;
  	  static int meshMode = 0;
  	  static boolean meshRecalc = false;
  	  static boolean addMeshBoarder = false;
  	  static boolean createStartPositionList = false;
  	  static boolean newlist = false;
  	  static int lineListIntervalNum = 1;
  	  
  	  static float lowThresh = 2;
  	  static float highThresh = 16;
  	  static float gKRadius =2;
  	  static int gKWidth = 16;
  	  
	  static boolean drawPaused = true;
	  static boolean killNewStrokes = false;
	  static boolean killAllStrokes = false;
	  
   	  
   	  int col1 = 10;
   	  int col2 = col1+100;
   	  int col3 = col2+100;
   	  int col4 = col3+100;
   	  int col5 = col4+100;
   	  int col6 = col5+100;
   	  
   	  int row1 = 10;
   	  int row2 = row1+60;
   	  
   	  int optionCol = 770;
   	  
   	  
    public CF2(Object theParent, int theWidth, int theHeight) {
	    parent = theParent;
	    w = theWidth;
	    h = theHeight;
  }
 	
  
  

public void setup() {
	  size(w, h);
	  frameRate(15);
	  
	  cp5a = new ControlP5(this);
	  PFont medfont = createFont("Arial",30,true); // use true/false for smooth/no-smooth
	  PFont bigfont = createFont("Arial",50,true); 
	  ControlFont font = new ControlFont(medfont,20);
	  cp5a.setFont(font) ;
	  cp5a.getFont() .sharp();
	  
	
	   runRB =  cp5a.addRadioButton("run")
	            .setPosition(col1,row1) .setSize(40,40) .setColorForeground(color(80)) .setColorActive(color(0,255,0)) .setColorBackground(color(255,0,0))
	            .setColorLabel(color(255)).setItemsPerRow(5) .setSpacingColumn(120).setSpacingRow(20)
	            .addItem("Running",1)
	            .addItem("Pause",2)
	            .addItem("Stop New",3)
	            .addItem("Kill All", 4)
	 			.activate(1);//start with pause button on
	   
	   cp5a.addBang("Mesh ReCalc").setPosition(optionCol,row1) .setSize(30,30) ;  
	   cp5a.addBang("Edge Calc").setPosition(optionCol+180,row1) .setSize(30,30) ; 
	   cp5a.addBang("Make List").setPosition(optionCol+310,row1) .setSize(30,30);
	   doEdgeNow = cp5a.addBang("edgeNow").setPosition(optionCol+310,row1) .setSize(30,30).setColorBackground(color(255,0,0)) .setVisible(false) ; 
	   
	   meshRB = cp5a.addRadioButton("meshrecalc")
	            .setPosition(optionCol,row1+80) .setSize(20,20) .setColorForeground(color(80)) .setColorActive(color(0,255,0)) .setColorBackground(color(255,0,0))
	            .setColorLabel(color(255)).setItemsPerRow(3) .setSpacingColumn(90)
	            .addItem("SSOBEL",1)
	            .addItem("SCHARR",2)
	            .addItem("RickS",3)
	            .activate(1)
	            .setVisible(false);
	   
	   meshThreshRB= cp5a.addSlider("mThreshold") .plugTo(parent,"mThreshold").setRange(1, 450) .setValue(mThreshold) .setPosition(optionCol,row1+60+60) .setSize(255,20).setVisible(false);
	   mesResolRB= cp5a.addSlider("mResolution")  .setRange(8, 100) .setValue(mResolution) .setPosition(optionCol,row1+90+60) .setSize(255,20).setVisible(false);
	   meshAddBoarderCB = cp5a.addCheckBox("Add Mesh Borders?") .setPosition(optionCol,row1+120+60)  .setColorForeground(color(120))  
			   				.setColorActive(color(255))  .setColorLabel(color(255)).setSize(20, 20)  .addItem("Add Mesh Boarder", 0).setVisible(false);

	   edgeSl1 = cp5a.addSlider("lowThresh") .setRange(.5f, 10) .setValue(lowThresh) .setPosition(optionCol,row1+80) .setSize(255,20).setVisible(false);
	   edgeSl2 = cp5a.addSlider("highThresh") .setRange(2f, 20) .setValue(highThresh) .setPosition(optionCol,row1+110) .setSize(255,20).setVisible(false);
	   edgeSl3 = cp5a.addSlider("gKRadius") .setRange(1f, 6) .setValue(gKRadius) .setPosition(optionCol,row1+140) .setSize(255,20).setVisible(false);
	   edgeSl4 = cp5a.addSlider("gKWidth") .setRange(4, 24) .setValue(gKWidth) .setPosition(optionCol,row1+170) .setSize(255,20).setVisible(false);
	   
	   interval = cp5a.addSlider("lineListIntervalNum").setRange(1, 10) .setValue(lineListIntervalNum) 
			   .setPosition(optionCol,row1+80) .setSize(255,20).setVisible(false);
}

public void draw() {
	background(128);
	fill (150,140,140);
	noStroke();
	if (GB.waitingforsomething == true)	{

		fill (255,0,0);
	}
	rect (optionCol -15,0,width,height);
	if (killNewStrokes == true ) runRB.activate(2);
}

public void controlEvent(ControlEvent c) {
//println("a control event in CF2 " + c);
	if (c.isGroup()) {//  if the Event was triggered from a ControlGroup
	      if (c.getGroup() == runRB) {
  	    	  switch((int)c.getValue()) {
	    	  case 1:  //running
	    		  drawPaused = false;
	    		  killNewStrokes = false;
	    		  break;
	    	  case 2: // paused
	    		  drawPaused = true;
	    		  killNewStrokes = false;
	    		  break;
	    	  case 3://kill paused
	    		  killNewStrokes = true;
	    		  drawPaused = false;
	    		  break;
	    	  case 4: //kill all
	    		  killAllStrokes = true;
	    		  killNewStrokes = false;
	    		  drawPaused = false;
	    		  break;
	    	  }
		  }//end if group is runRB

		  if (c.getGroup() == meshAddBoarderCB) {
	    		addMeshBoarder = true;
		  }
		    
	      if (c.getGroup() == meshRB) {
	    	  switch((int)c.getValue()) {
	    	  case 1:
	    		  meshMode = 0;
	    		  break;
	    	  case 2:
	    		  meshMode = 1;
	    		  break;
	    	  case 3:
	    		  meshMode = 2;
	    		  break;
	    	  }
	      }
	  }//end if group

	  else {// not a group then must have been a controller

		  if (c.getController().getName().equals("Mesh ReCalc")) {
			  if(meshRecalc == true){
				  meshRecalc = false;
				  turnMeshCalcOff();
				  
			  }else {
				  pauseThings();
	    		  meshRecalc = true;
	    		  turnMeshCalcOn();
	    		  turnEdgesOff();
			  }
    	  }
		  
		  if (c.getController().getName().equals("Edge Calc")) {
    		  runRB.activate(1);
    		  turnEdgesOn();
			  turnMeshCalcOff();
    	  }
		  if (c.getController().getName().equals("Make List")) {
			  interval.setVisible(true);
			  if (createStartPositionList == true) {
				  createStartPositionList = false;
				  turnListOff();
			  }else {// turn on the switch and start collecting points - until the button pushed again
				  createStartPositionList = true;
				  newlist = true;
				  pauseThings();
				  interval.setVisible(true);
				  println("you should see the dame thing");
				  turnEdgesOff();
				  turnMeshCalcOff();				  
			  }
    	  }
		  
		  if (c.getController().getName().equals("edgeNow")) { 
			  pauseThings();
			  GB.waitingforsomething = true;
			  doEdgeNow.setVisible(false);
			  CannyED.createEdges(this);
 		  	  turnEdgesOff();
 		  	GB.waitingforsomething = false;
			  }
	  }//end else - that is  control event not a group
	
	}//end control event

public void pauseThings() {
	runRB.activate(1);
}
public void turnMeshCalcOff() {
	  meshRB.setVisible(false);
	  mesResolRB.setVisible(false);
	  meshThreshRB.setVisible(false);
	  meshAddBoarderCB.setVisible(false);
//	  interval.setVisible(false);
}
public void turnMeshCalcOn() {	
	  GB.waitingforsomething = true;
	  meshRB.setVisible(true);
	  mesResolRB.setVisible(true);
	  meshThreshRB.setVisible(true);
	  meshAddBoarderCB.setVisible(true);
	  GB.waitingforsomething = false;
}
public void turnEdgesOff() {
	edgeSl1.setVisible(false);
	edgeSl2.setVisible(false);
	edgeSl3.setVisible(false);
	edgeSl4.setVisible(false);
	doEdgeNow.setVisible(false);
//	interval.setVisible(false);
}
public void turnEdgesOn() {
	doEdgeNow.setVisible(true);
	edgeSl1.setVisible(true);
	edgeSl2.setVisible(true);
	edgeSl3.setVisible(true);
	edgeSl4.setVisible(true);
}
public void turnListOff() {
	interval.setVisible(false);
}

public void keyPressed() {  // used to move the screen display around the graphics buffer - or save a version to a file
		GB.externalInput(key,keyCode);
}
}//end cf2
