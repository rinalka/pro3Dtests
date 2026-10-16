package postContemp;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import controlP5.CheckBox;
import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.ControllerInterface;
import controlP5.DropdownList;
import controlP5.FrameRate;
import controlP5.Label;
import controlP5.Radio;
import controlP5.RadioButton;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Textlabel;
import controlP5.Toggle;


public class CF extends PApplet {
	int w, h;
	
	
	static float BrushSize = 1;
	static float BrushSkew = 0;

	static int TintR = 255;
	static int TintG = 255;
	static int TintB = 255;
	static int Alph = 255;
	static float brushRotation = (float) 0.0;
	static int strokeAge = 100;
	static float smudgePercent = (float) 50;
	static int numOfStrokes = 50;
	static int orderedGridResX = 32;
	static int orderedGridResY = 32;
	static int oGridRanX = 5;
	static int oGridRanY = 5;
	public static float strokeAngleAdjust = 0;

	static int strokeType = 0;
	static int startPosType = 0;
	static int startPosResType = 0;
	static int velocityType =0;
	static int velocityKind =0;
	static int brushType =0;
	static int blendType =0;
	static int whichBrush=0;
	static int strokeWeight = 1;
	static int fanNumLines = 10;
	static int fanLineLength = 20;
	static int loadBrushFrom = 3;
	static int loadAlphaFrom =3;


	static boolean doBackground = false;
	static boolean doCanvasCopy = false;
	static boolean doMeshDisplay = false;
	static int copyTrifromSource = -1;
	static int whichMesh = 0;

	static boolean newBrush = false;
	static int colorBlendType = 0;
	static float saturation = 0;
	static boolean satChanged = false;
	static float HbristleVar,SbristleVar,BbristleVar;
	static float strokeAgeVar = 1;
	static int strokeVaryControl = 3;//off
	static boolean HSBinvert = false;
	static int hueShift = 0;
	static int satShift = 0;
	static int briShift = 0;
	static int alphaVal = 255;
	static float curveAmnt = (float) .05;
	static float randomFactor = (float) 1;
	static float HueRanLow,SatRanLow,BriRanLow ;
	static float HueRanHig,SatRanHig,BriRanHig ;
	static boolean hcran,scran,bcran = false;
	static int posResFrom =1;

	static int lineType = 0;
	static int blobType = 0;
	static int blobSize = 250;
	static int blobShape = 0;
	static int meshAlpha = 255;
	static int sizeageType = -1;
	static int sizehsbType = -1;

	static float ageMin = -.2f;
	static float ageMax = .2f;
	static float hsbMin = -.2f;
	static float hsbMax = .2f;

	
	static int bristleBrushType = 0;
	static int blobBrushType = 0;
	static int lineBrushType = 0;

	static int strokeSpeed = 50;
	static float accelerationControl = (float) 12;
	static int speedLimit = 2;

	static int BtintR = 255;
	static int BtintB= 255;
	static int BtintG= 255;
	static int Balph= 255;

	static int blobrSize = 20;
	static int vertSize = 10;
	static int spiroSegs = 4;

	static int numberOfPoints = 4;
	static float blobRadius = 50;
	static float controlBlobRadius = 50;
	
	static int huePosRestrictMin = 0,huePosRestrictMax = 255;
	static int satPosRestrictMin = 0,satPosRestrictMax = 255;
	static int briPosRestrictMin = 0,briPosRestrictMax = 255;
	static boolean huePosRestrict,satPosRestrict,briPosRestrict;

	static boolean sizeByHSBInvert = false;
	static boolean sizeBytri = false;
	
	static int whichCanvas = 4;
	static int whichCopy = 0;

	static int blurVal = 10;
	static int postVal = 4;
	static float threshVal = .5f;
	
	static int dropTo = 0;

	static int dropXdist,dropYdist = 20;
	static int dropSize = 10;
	static float dropAlpha = .2f;
	
	static float hueCenter = .5f;
	
	static int perLength = 200;
	static float spacing = .55f;
	static float Xnoiseyness = .25f;
	static float Ynoiseyness = .25f;
	static float Znoiseyness = .25f;
	static float curlyness = 10;
	static int perlinOctaves = 4;
	static float perlinFade = .5f;
	
	ControlP5 cp5;
	Object parent;
	ColorPicker cp1,cp2;

	Slider s1,s2,smudgePer,sat,gridResXS,gridResYS,saa,slv;
	Slider gridResXRanS,gridResYRanS;
	Slider Hbv,Sbv,Bbv;
	Slider curveAmtSL,curveRandomFacSL,brushSizeSL,brushSkewXSL,brushRotateSL;
	Slider speLim,conDir,strWeightS,strokeSpeedS,blobFixS,fanNumLinS,fanLineLngS;
	Slider numpntsS,blobRadS,controlblobRadS;
	Slider dropXS,dropYS,dropAS,dropSizeS,hueCenterSL;
	Slider perlinLengthS,perlinSpaceS,perlinNoiseXS,perlinNoiseYS,perlinNoiseZS,perlinCurlS,perlinOctS,perlinFadS;
	

	DropdownList brushShapeDD;
	DropdownList startPosTypeDD;
	DropdownList startPosResDD;
	DropdownList velocityTypeDD;
	DropdownList velocityKindDD;
	DropdownList dd7;
	DropdownList dd8;
	DropdownList svc;
	Textlabel l1,l2,l3,l4,l5,l6,l7,l8,cb,velocityKindL,lab,brushTypeL,brushShapeL,lineTypeL,blobTypeL,strokeTypeL,velocityTypeL;
	Textlabel blobBrushTypeL,lineBrushTypeL,bristleBrushTypeL,blobShapeL;
	FrameRate fr;
	Range startPosResRange,hueRan,satRan,briRan;
	Range ageMinMaxR,hsbMinMaxR;
	
	static Range huePosRange;
	static Range satPosRange;
	static Range briPosRange;
	static CheckBox StvCb,hcb,scb,bcb;
	CheckBox	hueRestrict,satRestrict,briRestrict;
	CheckBox  sizeageCB,sizehsbCB,sizetriCB;

	RadioButton drawToScreen;
	RadioButton fromRB,bfromRB,afromRB;
	RadioButton brushTypeRB;
	RadioButton strokeTypeRB;
	RadioButton lineTypeRB;
	RadioButton blobTypeRB;
	RadioButton blobBrushTypeRB, lineBrushTypeRB, bristleBrushTypeRB, blobShapeRB;
	RadioButton wCanvasRB,whichModRB;
	RadioButton dropRB;
	RadioButton sizeageRB,sizehsbRB;
	RadioButton  cts;
	
	int sBar = 0;
	int sOption = sBar+110;
	int prsBar = sBar+110;
	int bBar = 600 -120;
	int oBar = 1000-120;
	int cBar = 1300-120;
	int modBar = 1310-120;


	int col1 = 10;
	int col2 = col1+255+20;
	int col3 = col2+255+50;
	int col4 = col3+255+20+60;

	boolean firstt = true;



	public CF(Object theParent, int theWidth, int theHeight) {
		parent = theParent;
		w = theWidth;
		h = theHeight;
	}


	public void setup() {
		size(w, h);
		frameRate(15);
		cp5 = new ControlP5(this);
		PFont medfont = createFont("Arial",30,true); // use true/false for smooth/no-smooth
		PFont bigfont = createFont("Arial",50,true); 
		ControlFont font = new ControlFont(medfont,18);
		cp5.setFont(font) ;
		cp5.getFont() .sharp();



		// Stroke Bar   *****************************************

		//col1*******

		cp5.addSlider("numOfStrokes") .plugTo(parent,"numOfStrokes") .setRange(1, 500) .setValue(numOfStrokes) .setPosition(col1,sBar+35) .setSize(255,50);
		cp5.getController("numOfStrokes").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 		
		cp5.getController("numOfStrokes").getValueLabel() .setFont(bigfont);

		strokeSpeedS = cp5.addSlider("strokeSpeed") .plugTo(parent,"strokeSpeed") .setRange(1, 100) .setValue(strokeSpeed) .setPosition(col1,sBar+280) .setSize(255,20);
		cp5.getController("strokeSpeed").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		conDir = cp5.addSlider("accelerationControl").plugTo(parent,"accelerationControl").setRange((float).01,20)	.setValue(accelerationControl)	
				.setPosition(col1,sBar+330).setSize(255,20);
		cp5.getController("accelerationControl").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 

		speLim = cp5.addSlider("speedLimit").plugTo(parent,"speedLimit").setRange((float).1,20)	.setValue(speedLimit)	
				.setPosition(col1,sBar+380).setSize(255,20);
		cp5.getController("speedLimit").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 

		
		
		


		//col2************	
		cp5.addSlider("strokeAge")		.plugTo(parent,"strokeAge")	.setRange(1,400)	.setValue(strokeAge)	.setPosition(col2, sBar+15+20).setSize(255,30);
		cp5.getController("strokeAge").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 		
		cp5.getController("strokeAge").getValueLabel() .setFont(medfont);
		
		cp5.addSlider("strokeAgeVar")		.plugTo(parent,"strokeAgeVar")	.setRange(1,10)	.setValue(strokeAgeVar)	.setPosition(col2, sBar+15+80).setSize(255,20);
		cp5.getController("strokeAgeVar").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 		



		lab = cp5.addTextlabel("labelsv") .setText("Age ALT BY ")  .setPosition(col2,sBar+120) ;
		StvCb = cp5.addCheckBox("InvertHSB") .setPosition(col2+140,sBar+120)  .setColorForeground(color(120))  .setColorActive(color(255))  .setColorLabel(color(255))
				.setSize(20, 20)  .addItem("Inverted", 0);
		svc = cp5.addDropdownList("strokeVaryControl") .setPosition(col2, sBar+165) .setBackgroundColor(color(190)) .setItemHeight(20) .setBarHeight(20) .setWidth(255) .setHeight(90);
		svc.addItem("hue",0); 
		svc.addItem("sat",1); 
		svc.addItem("bri",2);	
		svc.addItem("red",3);
		svc.addItem("green",4);
		svc.addItem("blue",5);
		svc.addItem("OFF",6);	 

		curveAmtSL = cp5.addSlider("curveAmnt").plugTo(parent,"curveAmnt").setRange((float).01,(float)2)	.setValue(curveAmnt)	
				.setPosition(col2,sBar+280).setSize(255,20);
		cp5.getController("curveAmnt").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		curveRandomFacSL = cp5.addSlider("random factor") .plugTo(parent,"randomFactor") .setRange((float).01,(float)2) .setValue(randomFactor) 
				.setPosition(col2,sBar+330) .setSize(255,20) .setVisible(false);
		cp5.getController("random factor").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		
		
		gridResXS = cp5.addSlider("orderedGridResX") .plugTo(parent,"orderedGridResX") .setRange(1, 255) .setValue(orderedGridResX) 
				.setPosition(col2,sBar+395) 
				.setSize(125,20) .setVisible(false);
		cp5.getController("orderedGridResX").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		gridResYS = cp5.addSlider("orderedGridResY") .plugTo(parent,"orderedGridResY") .setRange(1, 255) .setValue(orderedGridResY) 
				.setPosition(col2+130,sBar+395) .setVisible(false)
				.setSize(125,20) .setVisible(false);
		cp5.getController("orderedGridResY").getCaptionLabel().setVisible(false);

		gridResXRanS = cp5.addSlider("oGridRanX") .plugTo(parent,"oGridRanX") .setRange(1,50) .setValue(oGridRanX) 
				.setPosition(col2,sBar+450) 
				.setSize(125,20) .setVisible(false);
		cp5.getController("oGridRanX").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		gridResYRanS = cp5.addSlider("oGridRanY") .plugTo(parent,"oGridRanY") .setRange(1,50) .setValue(oGridRanY) 
				.setPosition(col2+130,sBar+450) .setVisible(false)
				.setSize(125,20) .setVisible(false);
		cp5.getController("oGridRanY").getCaptionLabel().setVisible(false);
		

		strokeTypeL = cp5.addTextlabel("label2") .setText("STROKE TYPE")  .setPosition(col1,sBar+120) ;
		strokeTypeRB= cp5.addRadioButton("stype")
				.setPosition(col1,sBar+145) 
				.setSize(50,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(1) .setSpacingRow(10)
				.addItem("straight",0)
				.addItem("curved",1)
				.addItem("continuous flow",2);





		//col3***********************************

		
		fromRB =  cp5.addRadioButton("from")
				.setPosition(col3, prsBar+80) .setSize(20,20) .setColorForeground(color(80)) .setColorActive(color(0,255,0)) .setColorBackground(color(255,0,0))
		            .setColorLabel(color(255)).setItemsPerRow(3) .setSpacingColumn(90).setSpacingRow(10)
		            .addItem("Pic",0)
		            .addItem("Alt",1)
		            .addItem("Work",2)
		            .addItem("Control",3)
		            .addItem("Output",4);

		
		hueRestrict =   cp5.addCheckBox("hr") .setPosition(col3, prsBar+130+10)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0,0,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("jjj", 0); 
		hueRestrict.hideLabels();
		satRestrict =   cp5.addCheckBox("sr") .setPosition(col3, prsBar+130+70)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("kkk", 0);	
		satRestrict.hideLabels();
		briRestrict =   cp5.addCheckBox("br") .setPosition(col3, prsBar+130+130)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("lll", 0);	
		briRestrict.hideLabels();

		huePosRange = cp5.addRange("hue range") .setBroadcast(false)  
				.setPosition(col3+30, prsBar+130+30) .setSize(255,20) .setHandleSize(20) .setRange(0,255) .setRangeValues(huePosRestrictMin,huePosRestrictMax)
				.setBroadcast(true) .setColorForeground(color(255,200)).setColorBackground(color(255,40)) .setColorValueLabel(0) .setColorCaptionLabel(0) .setVisible(true);
		cp5.getController("hue range").getCaptionLabel() .align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 	

		satPosRange = cp5.addRange("sat Range") .setBroadcast(false)  
				.setPosition(col3+30, prsBar+130+90) .setSize(255,20) .setHandleSize(20) .setRange(0,255) .setRangeValues(satPosRestrictMin,satPosRestrictMax)
				.setBroadcast(true) .setColorForeground(color(255,200)).setColorBackground(color(255,40)) .setColorValueLabel(0).setColorCaptionLabel(0).setVisible(true);
		cp5.getController("sat Range").getCaptionLabel() .align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		briPosRange = cp5.addRange("bri Range") .setBroadcast(false)  
				.setPosition(col3+30, prsBar+130+150) .setSize(255,20) .setHandleSize(20) .setRange(0,255) .setRangeValues(briPosRestrictMin,briPosRestrictMax)
				.setBroadcast(true) .setColorForeground(color(255,200)).setColorBackground(color(255,40)).setColorValueLabel(0).setColorCaptionLabel(255) .setVisible(true);
		cp5.getController("bri Range").getCaptionLabel() .align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);


		cp5.addTextlabel("label3") .setText("START POSITION TYPE")  .setPosition(col3,sBar+15) ;
		startPosTypeDD = cp5.addDropdownList("Start Pos Type") .setPosition(col3, sBar+15+40) .setBackgroundColor(color(190)) .setItemHeight(20) .setBarHeight(20) .setWidth(255) .setHeight(180);
		startPosTypeDD.addItem("Random",0); 
		startPosTypeDD.addItem("Ordered",1); 
		startPosTypeDD.addItem("from Triangle",2);
		startPosTypeDD.addItem("fromMouse",3);
		startPosTypeDD.addItem("from Vertice", 4);
		startPosTypeDD.addItem("from List", 5);


		//col4*******************
		velocityTypeL = cp5.addTextlabel("label4") .setText("VELOCITY TYPE")  .setPosition(col4,sBar+15) ;
		velocityTypeDD = cp5.addDropdownList("Velocity Type") .setPosition(col4, sBar+15+40) .setBackgroundColor(color(190)) 
				.setItemHeight(20) .setBarHeight(20) .setWidth(255) .setHeight(180);
			velocityTypeDD.addItem("Random",0); 
			velocityTypeDD.addItem("Angle from Pic",1);
			velocityTypeDD.addItem("Angle from Alt",2);
			velocityTypeDD.addItem("Angle from Work",3);
			velocityTypeDD.addItem("Angle from Control",4);
			velocityTypeDD.addItem("Seeker from Pic",5);
			velocityTypeDD.addItem("Seeker from Alt",6);
			velocityTypeDD.addItem("Seeker from Work",7);
			velocityTypeDD.addItem("Seeker from Control",8);
			velocityTypeDD.addItem("to Mouse", 9);


		velocityKindL = cp5.addTextlabel("label5") .setText("VELOCITY KIND")  .setPosition(col4,sBar+240) .setVisible(false);
		velocityKindDD = cp5.addDropdownList("Velocity Kind") .setPosition(col4,sBar+290) .setVisible(false)
				.setBackgroundColor(color(190)) .setItemHeight(20) .setBarHeight(20) .setWidth(255) .setHeight(180);
				velocityKindDD.addItem("Red",0); 
				velocityKindDD.addItem("Green",1); 
				velocityKindDD.addItem("Blue",2); 
				velocityKindDD.addItem("Hue",3); 
				velocityKindDD.addItem("Saturation",4); 
				velocityKindDD.addItem("Brightness",5);


		saa =  cp5.addSlider("strokeAngleAdjust") .plugTo(parent,"strokeAngleAdjust") .setRange(0, TWO_PI) .setValue(strokeAngleAdjust) 
				.setVisible(false) .setPosition(col4,sOption+340) .setSize(255,20);
		cp5.getController("strokeAngleAdjust").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);







//brush Bar	****************************************************8         
		//col1***********

		brushTypeL =  cp5.addTextlabel("label6") .setText("BRUSH TYPE")  .setPosition(col1,bBar+15) ;
		brushTypeRB =   cp5.addRadioButton("btype")
				.setPosition(col1,bBar+15+40) .setSize(50,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(1) .setSpacingRow(10)
				.addItem("Bristle",0)
				.addItem("Line",1)
				.addItem("Blob",2);
					
		
		bristleBrushTypeL =  cp5.addTextlabel("labelbrb") .setText("Bristle Type")  .setPosition(col1,bBar+30+120) .setVisible(true);
		bristleBrushTypeRB =   cp5.addRadioButton("lbrtype")
				.setPosition(col1+40,bBar+15+40+120) .setSize(50,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(1) .setSpacingRow(10)
				.addItem("copy",0)
				.addItem("dipped",1)
				.addItem("smudge",2)
				.addItem("paint",3)
				.addItem("blend",4)
				.setVisible(true);
		
		blobBrushTypeL =  cp5.addTextlabel("labelbbtb") .setText("Blob Type")  .setPosition(col1,bBar+30+120) .setVisible(false);
		blobBrushTypeRB =   cp5.addRadioButton("lbbttype")
				.setPosition(col1+40,bBar+15+40+120) .setSize(30,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(2) .setSpacingRow(10) .setSpacingColumn(150)
				.addItem("bcopy",0)
				.addItem("bdipped",1)
				.addItem("bsmudge",2)
				.addItem("bpaint",3)
				.addItem("bblend",4)
				.addItem("brandom",5)
				.setVisible(false);

		lineBrushTypeL =  cp5.addTextlabel("labellb") .setText("Line Type")  .setPosition(col1,bBar+30+120) .setVisible(false);
		lineBrushTypeRB =   cp5.addRadioButton("lbtype")
				.setPosition(col1+40,bBar+15+40+120) .setSize(30,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(2) .setSpacingRow(10).setSpacingColumn(150)
				.addItem("single",0)
				.addItem("fan",1)
				.addItem("scattered",2)
				.addItem("parallel",3)
				.addItem("drop",4)	
				.addItem("perlin",5)
				.setVisible(false);
			
	
		cp5.addTextlabel("loadBfrom").setText("Load Brush From:") .setPosition (col1,bBar+330);
		bfromRB =  cp5.addRadioButton("bfrom")
				.setPosition(col1+150, bBar+330) .setSize(20,20) .setColorForeground(color(80)) .setColorActive(color(0,255,0)) .setColorBackground(color(255,0,0))
		            .setColorLabel(color(255)).setItemsPerRow(4) .setSpacingColumn(80).setSpacingRow(10)
		            .addItem(" Pic",0)
		            .addItem(" Alt",1)
		            .addItem(" Work",2)
		            .addItem(" Control",3)
		            ;
//		 			.activate(3);//start with pause button on
		cp5.addTextlabel("loadAfrom").setText("Load Alpha From:") .setPosition (col1,bBar+360);
		afromRB =  cp5.addRadioButton("afrom")
				.setPosition(col1+150, bBar+360) .setSize(20,20) .setColorForeground(color(80)) .setColorActive(color(0,255,0)) .setColorBackground(color(255,0,0))
		            .setColorLabel(color(255)).setItemsPerRow(4) .setSpacingColumn(80).setSpacingRow(10)
		            .addItem(".Pic",0)
		            .addItem(".Alt",1)
		            .addItem(".Work",2)
		            .addItem(".Control",3)
		            ;
//		 			.activate(3);//start with pause button on

		//col2***************
		smudgePer =  cp5.addSlider("smudgePercent")	.plugTo(parent,"smudgePercent")	.setRange(0,100)	.setValue(smudgePercent)	
				.setPosition(col2,bBar+85).setSize(255,20) .setVisible(false);
		cp5.getController("smudgePercent").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 


		Hbv =  cp5.addSlider("HbristleVar")	.plugTo(parent,"HbristleVar")	.setRange(0,20)	.setValue(HbristleVar)	
				.setPosition(col2,bBar+40).setSize(255,20) .setVisible(false);
		cp5.getController("HbristleVar").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 
		
		Sbv =  cp5.addSlider("SbristleVar")	.plugTo(parent,"SbristleVar")	.setRange(0,50)	.setValue(SbristleVar)	
				.setPosition(col2,bBar+90).setSize(255,20) .setVisible(false);
		cp5.getController("SbristleVar").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 
		
		Bbv =  cp5.addSlider("BbristleVar")	.plugTo(parent,"BbristleVar")	.setRange(0,50)	.setValue(BbristleVar)	
				.setPosition(col2,bBar+140).setSize(255,20) .setVisible(false);
		cp5.getController("BbristleVar").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 
		
		cb =   cp5.addTextlabel("label0") .setText("BLENDCOLOR")  .setPosition(col2,bBar+80) .setVisible(false);
		cp1 =   cp5.addColorPicker("picker1", 255,255,255,20) .setPosition(col2,bBar+80) .setSize(300,1) .setVisible(false);

		l8 =   cp5.addTextlabel("labelcbt") .setText(" ColorBLEND TYPE")  .setPosition(col2,bBar+180) ;
		l8.setVisible(false);
		dd8 =   cp5.addDropdownList("colorBlend Type") .setPosition(col2,bBar+200) .setBackgroundColor(color(190)) .setItemHeight(20) .setBarHeight(20) .setWidth(255) 
				.setHeight(120);
		dd8.addItem("Blend",0); dd8.addItem("Add",1); dd8.addItem("Subtract",2); dd8.addItem("Darkest",3); dd8.addItem("Lightest",4);
		dd8.addItem("Difference",5); dd8.addItem("Exclusion",6); dd8.addItem("Multiply",7); dd8.addItem("Screen", 8); dd8.addItem("Overlay",9); dd8.addItem("Hard_light",10);
		dd8.addItem("Soft_light",11); dd8.addItem("Dodge",12); dd8.addItem("Burn",13); 
		dd8.setVisible(false);




		//col3**********************************************
		brushShapeL =  cp5.addTextlabel("label1") .setText("BRUSH SHAPE")  .setPosition(col3,bBar+15) ;
		brushShapeDD =   cp5.addDropdownList("Brush Shape") .setPosition(col3,bBar+15+40) .setBackgroundColor(color(190)) .setItemHeight(20) .setBarHeight(20) .setWidth(255) .setHeight(380);
		brushShapeDD.addItem("whitedot32",0); 
		brushShapeDD.addItem("blob32",1); 
		brushShapeDD.addItem("blobcross32",2);
		brushShapeDD.addItem("circle32",3); 
		brushShapeDD.addItem("diags32",4); 
		brushShapeDD.addItem("fan32",5);
		brushShapeDD.addItem("grass32",6);
		brushShapeDD.addItem("hardstar32",7);
		brushShapeDD.addItem("kapow2",8);
		brushShapeDD.addItem("leaf32",9);
		brushShapeDD.addItem("lightnin32",10);
		brushShapeDD.addItem("puffball32",11);
		brushShapeDD.addItem("square32",12);
		


		blobShapeL =  cp5.addTextlabel("labelbbs") .setText("Blob Shape")  .setPosition(col3,bBar+15) .setVisible(false) ;
		blobShapeRB =   cp5.addRadioButton("bbtypes")
				.setPosition(col3,bBar+15+40) .setSize(50,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(1) .setSpacingRow(10)
				.addItem("ellipse",0)
				.addItem("rectangle",1)
				.addItem("triangle",2)
				.addItem("random",3)
				.addItem("beizierellipse",4)
				.setVisible(false);


		


		fanNumLinS =  cp5.addSlider("fanNumLines")	.plugTo(parent,"fanNumLines")	.setRange(5,50)	.setValue(fanNumLines)	
				.setPosition(col3,bBar+85).setSize(255,20) .setVisible(false);
		cp5.getController("fanNumLines").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 

		fanLineLngS =  cp5.addSlider("fanLineLength")	.plugTo(parent,"fanLineLength")	.setRange(10,150)	.setValue(fanLineLength)	
				.setPosition(col3,bBar+125).setSize(255,20) .setVisible(false);
		cp5.getController("fanLineLength").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 

		numpntsS = cp5.addSlider("numberOfPoints")		.plugTo(parent,"numberOfPoints")	.setRange(4,10)	.setValue(numberOfPoints)	
				.setPosition(col3,bBar+225).setSize(255,20).setVisible(false);
		cp5.getController("numberOfPoints").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		blobRadS = cp5.addSlider("blobRadius")		.plugTo(parent,"blobRadius")	.setRange(-100,150)	.setValue(blobRadius)	
				.setPosition(col3,bBar+265).setSize(255,20).setVisible(false);
		cp5.getController("blobRadius").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		controlblobRadS = cp5.addSlider("controlBlobRadius")		.plugTo(parent,"controlBlobRadius")	.setRange(-100,150)	.setValue(controlBlobRadius)	
				.setPosition(col3,bBar+305).setSize(255,20).setVisible(false);
		cp5.getController("controlBlobRadius").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		
		
		dropXS = cp5.addSlider("dropXdist")	.plugTo(parent,"dropXdist")	.setRange(-200,200)	.setValue(dropXdist)	
				.setPosition(col3,bBar+45).setSize(255,20) .setVisible(false);
		cp5.getController("dropXdist").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		dropYS = cp5.addSlider("dropYdist")	.plugTo(parent,"dropYdist")	.setRange(-200,200)	.setValue(dropYdist)	
				.setPosition(col3,bBar+85).setSize(255,20) .setVisible(false);
		cp5.getController("dropYdist").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		dropAS = cp5.addSlider("dropAlpha")	.plugTo(parent,"dropAlpha")	.setRange(.01f,.8f)	.setValue(dropAlpha)	
				.setPosition(col3,bBar+125).setSize(255,20) .setVisible(false);
		cp5.getController("dropAlpha").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		dropSizeS = cp5.addSlider("dropSize")	.plugTo(parent,"dropSize")	.setRange(1,20)	.setValue(dropSize)	
				.setPosition(col3,bBar+165).setSize(255,20) .setVisible(false);
		cp5.getController("dropSize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
	
		perlinLengthS = cp5.addSlider("perLength")	.plugTo(parent,"perLength")	.setRange(10,1000)	.setValue(perLength)	
				.setPosition(col3,bBar+35).setSize(255,20) .setVisible(false);
		cp5.getController("perLength").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinSpaceS = cp5.addSlider("spacing")	.plugTo(parent,"spacing")	.setRange(.1f,1)	.setValue(spacing)	
				.setPosition(col3,bBar+75).setSize(255,20) .setVisible(false);
		cp5.getController("spacing").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinNoiseXS = cp5.addSlider("Xnoiseyness")	.plugTo(parent,"Xnoiseyness")	.setRange(.1f,1f)	.setValue(Xnoiseyness)	
				.setPosition(col3,bBar+120).setSize(255,20) .setVisible(false);
		cp5.getController("Xnoiseyness").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinNoiseYS = cp5.addSlider("Ynoiseyness")	.plugTo(parent,"Ynoiseyness")	.setRange(.1f,1f)	.setValue(Ynoiseyness)	
				.setPosition(col3,bBar+165).setSize(255,20) .setVisible(false);
		cp5.getController("Ynoiseyness").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinNoiseZS = cp5.addSlider("Znoiseyness")	.plugTo(parent,"Znoiseyness")	.setRange(.1f,1f)	.setValue(Znoiseyness)	
				.setPosition(col3,bBar+210).setSize(255,20) .setVisible(false);
		cp5.getController("Znoiseyness").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinCurlS = cp5.addSlider("curlyness")	.plugTo(parent,"curlyness")	.setRange(.1f,20)	.setValue(curlyness)	
				.setPosition(col3,bBar+255).setSize(255,20) .setVisible(false);
		cp5.getController("curlyness").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		perlinOctS = cp5.addSlider("perlinOctaves")	.plugTo(parent,"perlinOctaves")	.setRange(1,8)	.setValue(perlinOctaves)	
				.setPosition(col3,bBar+300).setSize(255,20) .setVisible(false);
		cp5.getController("perlinOctaves").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		perlinFadS = cp5.addSlider("perlinFade")	.plugTo(parent,"perlinFade")	.setRange(.1f,.99f)	.setValue(perlinFade)	
				.setPosition(col3,bBar+340).setSize(255,20) .setVisible(false);
		cp5.getController("perlinFade").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);	

		//col4*************************
		
		strWeightS =  cp5.addSlider("strokeWeight")	.plugTo(parent,"strokeWeight")	.setRange(1,25)	.setValue(strokeWeight)	
				.setPosition(col4,bBar+30).setSize(255,20) .setVisible(false);
		cp5.getController("strokeWeight").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 
		
		brushSizeSL =  cp5.addSlider("BrushSize")	.plugTo(parent,"BrushSize")	.setRange( -10, 10)	.setValue(BrushSize)	.setPosition(col4,bBar+30).setSize(255,20);
		cp5.getController("BrushSize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 

		brushSkewXSL = cp5.addSlider("BrushSkew")	.plugTo(parent,"BrushSkew").setRange( -.9f, .9f)	.setValue(BrushSkew)	.setPosition(col4,bBar+75).setSize(255,20);
		cp5.getController("BrushSkew").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);


		sizehsbCB =   cp5.addCheckBox("sizebyHSBInvert") .setPosition(col4, bBar+120)  .setColorForeground(color(120)) .setColorActive(color(0,255,0))
				.setColorLabel(color(255,0,0,0)) .setSize(20, 20) .setSpacingColumn(30) .addItem("sizeByHSBInvert", 0); 
		
		
		sizehsbRB =   cp5.addRadioButton("shtype") .setPosition(col4, bBar+155)  .setSize(20,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(4).setSpacingColumn(64)
				.addItem("shu",0)
				.addItem("ssa",1)
				.addItem("sbr",2)
				.addItem("ssb",3)
//				.activate(0)
				.setVisible(true);
		
		
		  hsbMinMaxR = cp5.addRange("hsbMM")
		             // disable broadcasting since setRange and setRangeValues will trigger an event
		             .setBroadcast(false) 
		             .setPosition(col4-15, bBar+185)
		             .setSize(245,20)
		             .setHandleSize(20)
		             .setRange(-.9f, .9f)
		             .setRangeValues( -.2f, .2f )
		             // after the initialization we turn broadcast back on again
		             .setBroadcast(true)
		             .setColorForeground(color(150)).setColorActive(color(200)).setColorBackground(color(50,50,80))  
		             ;
		
			hueCenterSL = cp5.addSlider("hueCenter")	.plugTo(parent,"HueCenter").setRange( 0, 1)	.setValue(hueCenter)	.setPosition(col4-15,bBar+215).setSize(245,20);
			cp5.getController("BrushSkew").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		sizeageRB =   cp5.addRadioButton("satype") .setPosition(col4, bBar+255)  .setSize(20,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(4).setSpacingColumn(64)
				.addItem("sb",0)
				.addItem("bs",1)
				.addItem("sbs",2)
				.addItem("r",3)
//				.activate(0)
				.setVisible(true);
			
		  ageMinMaxR = cp5.addRange("ageMM")
		             // disable broadcasting since setRange and setRangeValues will trigger an event
		             .setBroadcast(false) 
		             .setPosition(col4, bBar+285)
		             .setSize(245,20)
		             .setHandleSize(20)
		             .setRange(-.9f, .9f)
		             .setRangeValues( -.2f, .2f )
		             // after the initialization we turn broadcast back on again
		             .setBroadcast(true)
		             .setColorForeground(color(150)).setColorActive(color(200)).setColorBackground(color(50,50,80))  
		             ;
		
		brushRotateSL = cp5.addSlider("brushRotation")		.plugTo(parent,"brushRotation")	.setRange(0,(float) .05)	.setValue(brushRotation)	.setPosition(col4,bBar+375).setSize(255,20);
		cp5.getController("brushRotation").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

		sizetriCB =   cp5.addCheckBox("sizeToTri") .setPosition(col4, bBar+335)  .setColorForeground(color(120)) .setColorActive(color(0,255,0))
				.setColorLabel(color(255,0,0,0)) .setSize(20, 20) .setSpacingColumn(30) .addItem("sizemxievmks",0);
		
//		sizeageCB =   cp5.addCheckBox("sizeBiAge") .setPosition(col4, bBar+220)  .setColorForeground(color(120)) .setColorActive(color(0,255,0))
//				.setColorLabel(color(255,0,0,0)) .setSize(20, 20) .setSpacingColumn(30) .addItem("sizeByAge", 0); 		
		blobFixS = cp5.addSlider("blobSize") .plugTo(parent,"blobSize") .setRange(10, 1024) .setValue(blobSize)
				.setPosition(col4,bBar+30) .setSize(255,20).setVisible(false);
		cp5.getController("blobSize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		
		

//oBar************************************


		hueRan =   cp5.addRange("hue randomize")
				.setBroadcast(false) .setPosition(col3-50+30, oBar+40)   .setSize(225,20)
				.setHandleSize(20)      .setRange((float)-.5,(float).5)   .setRangeValues(HueRanLow,HueRanHig)
				.setBroadcast(true) .setColorForeground(color(255,80))  .setColorBackground(color(255,80)) ;
		satRan =   cp5.addRange("sat randomize")
				.setBroadcast(false)    .setPosition(col3-50+30, oBar+100)   .setSize(225,20)
				.setHandleSize(20)      .setRange((float)-100,(float)100)   .setRangeValues(SatRanLow,SatRanHig)
				.setBroadcast(true) .setColorForeground(color(255,80))  .setColorBackground(color(255,80)) ;		   
		briRan =   cp5.addRange("bri randomize")
				.setBroadcast(false)    .setPosition(col3-50+30, oBar+160)   .setSize(225,20)
				.setHandleSize(20)      .setRange((float)-100,(float)100)   .setRangeValues(BriRanLow,BriRanHig)
				.setBroadcast(true) .setColorForeground(color(255,80))  .setColorBackground(color(255,80)) ;

		cp5.getController("hue randomize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 	
		cp5.getController("sat randomize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 	
		cp5.getController("bri randomize").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0); 	

		hcb =   cp5.addCheckBox("h") .setPosition(col3-50, oBar+20)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0,0,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("jj", 0); 
		hcb.hideLabels();
		scb =   cp5.addCheckBox("s") .setPosition(col3-50, oBar+80)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("kk", 0);	
		scb.hideLabels();
		bcb =   cp5.addCheckBox("b") .setPosition(col3-50, oBar+140)  .setColorForeground(color(120)) .setColorActive(color(0,155,0))
				.setColorLabel(color(255,0)) .setSize(20, 40) .setSpacingColumn(30) .addItem("ll", 0);	
		bcb.hideLabels();

		cp5.addSlider("hueShift") .plugTo(parent,"hueShift") .setRange(-128, 127) .setValue(hueShift) .setPosition(col4,oBar+40) .setSize(255,20);
		cp5.getController("hueShift").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		cp5.addSlider("satShift") .plugTo(parent,"satShift") .setRange(-100, 100) .setValue(satShift) .setPosition(col4,oBar+100) .setSize(255,20);
		cp5.getController("satShift").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		cp5.addSlider("briShift") .plugTo(parent,"briShift") .setRange(-100, 100) .setValue(briShift) .setPosition(col4,oBar+160) .setSize(255,20);
		cp5.getController("briShift").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
		cp5.addSlider("alphaVal") .plugTo(parent,"alphaVal") .setRange(0, 255)    .setValue(alphaVal) .setPosition(col4,oBar+220) .setSize(255,30);
		cp5.getController("alphaVal").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);



		//you should move these to the right place when you clean up this mess 
		startPosTypeDD.setValue(0);
		velocityTypeDD.setValue(0);
		velocityKindDD.setValue(0);

		
		
		
//final bar******************************************************************************

		cp5.addBang("reload pic").setPosition(col1, cBar+40) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER) ;
		cp5.addBang("reload alt").setPosition(col1, cBar+70) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER) ;

		dropRB =   cp5.addRadioButton("dropTo")
				.setPosition(col1, cBar+100) .setSize(20,20) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(2) .setSpacingColumn(60)
				.addItem("pic",0)
				.addItem("alt",1)
				.activate(0)
				.setVisible(true);

		wCanvasRB =   cp5.addRadioButton("wCanvas")
				.setPosition(col1, cBar+130) .setSize(30,30) 
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(1) .setSpacingRow(10)
				.addItem("pic 1 ",0)
				.addItem("alt pic2",1)
				.addItem("workC",2)
				.addItem("controlC",3)
				.addItem("outputC",4)
				.addItem(" no Canvas",5)
				.activate(4)
				.setVisible(true);
	
		int col1a = col2-100;
		cp5.addBang("pic to alt")  .setPosition(col1a, cBar+40) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("pic to work")  .setPosition(col1a, cBar+70) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER) ;
		cp5.addBang("pic to control")  .setPosition(col1a, cBar+100) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER) ;
		cp5.addBang("pic to output")  .setPosition(col1a, cBar+130) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("alt to work")  .setPosition(col1a, cBar+160) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("alt to control")  .setPosition(col1a, cBar+190) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("alt to output")  .setPosition(col1a, cBar+220) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("work to alt")  .setPosition(col1a, cBar+250) .setSize(20,20) .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addBang("work to control")  .setPosition(col1a, cBar+280) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER) ;
		cp5.addBang("work to output")  .setPosition(col1a, cBar+310) .setSize(20,20) .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addBang("control to work")  .setPosition(col1a, cBar+340) .setSize(20,20) .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addBang("control to output")  .setPosition(col1a, cBar+370) .setSize(20,20) .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addBang("output to work")  .setPosition(col1a, cBar+400) .setSize(20,20) .getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		
		cts =   cp5.addRadioButton("copyTri fromSource")  
				.setPosition(col1, cBar+440) .setSize(20,20)
				.setColorForeground(color(80)) .setColorActive(color(0,200,0)) .setColorBackground(color(50,50,80))
				.setColorLabel(color(255)).setItemsPerRow(3) .setSpacingColumn(180)
				.addItem("copy work tri from pic", 0)
				.addItem("copy work tri from alt", 1)
				.addItem("copy work tri from control", 2);
		 
		int plus = 120;		
	    int shift = 100;
	    int clroff = +180;
	    
		cp5.addBang("blobs")  .setPosition(col3,modBar+10) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addSlider("blobrSize") .plugTo(parent,"blobrSize") .setRange(10,300) .setValue(blobrSize) .setPosition(col3+plus,modBar+10) .setSize(255,20);
		cp5.addBang("verts")  .setPosition(col3,modBar+40) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addSlider("vertSize") .plugTo(parent,"vertSize") .setRange(10,200) .setValue(vertSize) .setPosition(col3+plus,modBar+40) .setSize(255,20);
		cp5.addBang("spiro")  .setPosition(col3,modBar+70) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addSlider("spiroSegs") .plugTo(parent,"spiroSegs") .setRange(1,25) .setValue(spiroSegs) .setPosition(col3+plus,modBar+70) .setSize(255,20);
		cp5.addBang("mesh").setPosition(col3,modBar+100) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		
		cp5.addSlider("meshAlpha") .plugTo(parent,"meshAlpha") .setRange(1,255) .setValue(meshAlpha) .setPosition(col3+plus,modBar+110) .setSize(255,30); 
	    
		cp2 =   cp5.addColorPicker("picker2",col3-110, modBar+clroff,200,20);
		
		cp5.addBang("color")  .setPosition(col3+shift,modBar+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER); 
	    cp5.addBang("erode")  .setPosition(col3+100+shift,modBar+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
	    cp5.addBang("dilate")  .setPosition(col3+200+shift,modBar+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
	    cp5.addBang("invert")  .setPosition(col3+300+shift,modBar+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
	    
		cp5.addBang("blur")  .setPosition(col3+shift,modBar+30+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER); 
		cp5.addSlider("blurVal").plugTo(parent,"blurVal") .setRange(1, 10) .setValue(blurVal) .setPosition(col3+plus+shift,modBar+30+clroff) .setSize(255,20);
	    cp5.addBang("poster")  .setPosition(col3+shift,modBar+60+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addSlider("postVal") .plugTo(parent,"postVal").setRange(2, 30) .setValue(postVal) .setPosition(col3+plus+shift,modBar+60+clroff) .setSize(255,20);
	    cp5.addBang("threshH")  .setPosition(col3+shift,modBar+90+clroff) .setSize(20,20).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE,  ControlP5.CENTER);
		cp5.addSlider("threshVal") .plugTo(parent,"threshVal") .setRange(0,1) .setValue(threshVal) .setPosition(col3+plus+shift,modBar+90+clroff) .setSize(255,20);
	    
		svc.setValue(6);
		strokeTypeRB.activate(0);
		fromRB.activate(0);
		blobShapeRB.activate(0);
		blobBrushTypeRB.activate(0);
		lineBrushTypeRB.activate(0);
		brushTypeRB.activate(0);
		bristleBrushTypeRB.activate(0);
		brushShapeDD.setValue(0);


}
	//*****************************************************************************************************************************
	//
	// end of setup
	//
	//*****************************************************************************************************************************

	public void draw() {

		fill (120,120,120);//erase all the left on buttons.....
		rect (0,0,w,sBar);

		fill (130,120,120);
		rect (0,sBar,w,bBar-sBar);

		fill (140,120,120);
		rect (0,bBar,w,oBar-bBar);

		if (GB.waitingforsomething == true)	{
			fill (255,0,0);
			rect (10,oBar+10, 400, 200);
		}else {
			fill (130,130,120);
			rect (0,oBar,w,cBar);
		}
		
		fill (120,130,140);
		rect (0,cBar,w,h);
		
		int poopy = GB.picWidth;  //***********************clean this mess up and learn how to cast
		float poop = (float) 210/poopy;
		float tempH =  GB.picHeight*poop;
		int tempHa = (int)tempH;
//		image(GB.picture, col1+15, bBar+570, 210, tempHa);

		if (GB.mousePressed ==true) {
			GB.mousePressed = false;
			onMouseSetPosRestrict();
		}
		fill (GB.mouseSelectedColor,255);
		rect (col3+30+255, prsBar+130+5,40,165);
		int r = ((GB.mouseSelectedColor >> 16) & 0xFF)  ;  //shift and and to get the rgb of the tempBrush pixel
		int g = ((GB.mouseSelectedColor >> 8) & 0xFF)  ;
		int b = GB.mouseSelectedColor  & 0xFF  ;
		float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1
		int h = (int) (hsb[0]*255);
		int s = (int) (hsb[1]*255);
		int l = (int) (hsb[2]*255);

		colorMode (HSB,255,255,255);
		for(int i = 0; i<256; i++) {
			stroke (i,255,255,255);
			line (col3+30+i,prsBar+130+10,col3+30+i,prsBar+130+10+20);
		}
		for(int i = 0; i<256; i++) {
			stroke (255,i,255,255);
			line (col3+30+i,prsBar+130+70,col3+30+i,prsBar+130+70+20);
		}
		for(int i = 0; i<256; i++) {
			stroke (255,255,i,255);
			line (col3+30+i,prsBar+130+130,col3+30+i,prsBar+130+130+20);
		}
		noStroke();
		colorMode (RGB,255,255,255);


	}//end draw***********************************************************************

	
	
	void onMouseSetPosRestrict () {

		int r = ((GB.mouseSelectedColor >> 16) & 0xFF)  ;  //shift and and to get the rgb of the tempBrush pixel
		int g = ((GB.mouseSelectedColor >> 8) & 0xFF)  ;
		int b = GB.mouseSelectedColor  & 0xFF  ;
		float[] hsb = Color.RGBtoHSB(r,g,b,null); // convert to hsb - now numbers between 0 and 1
		float h = (hsb[0]*255);
		float s = (hsb[1]*255);
		float l = (hsb[2]*255);
		//		println ("hsb are "+h+" "+s+" "+l);
		float minh = h-20;
		float maxh = h+20;
		float mins = s-20;
		float maxs = s+20;
		float minl = l-20;
		float maxl = l+20;
		if (minh <0) {
			minh = minh + 255;
//			PApplet.println("hue display is effed up less than 0");
		}
		if (maxh >255) {
			maxh = maxh - 255;
//			PApplet.println("hue display is effed up greater than 255");
		}
		if (mins <0) mins = 0;
		if (maxs >255) maxs = 255;
		if (minl <0) minl = 0;
		if (maxl >255) maxl = 255;
//		println(" after a mouse select.... sat and satmin satmax "+s+" "+mins+" "+maxs);
		CF.huePosRange.setRangeValues(minh,maxh);
		CF.satPosRange.setRangeValues(mins,maxs);
  		CF.briPosRange.setRangeValues(minl,maxl);
		//	   	println("pos restric values are now " + briPosRestrictMin +" "+ briPosRestrictMax);
	}




	public void controlEvent(ControlEvent c) {

		if (c.isGroup()) {//  if the Event was triggered from a ControlGroup
			
			if  (c.getGroup() == brushTypeRB) {
				brushType = (int) c.getValue();
				newBrush = true;
				switch (brushType) {
				case 0://bristle
					turnLineOff();  //the order off,off,on is important - as there might be shared options ...
					turnBlobOff();
					turnBristleOn();
					break;
				case 1://line
					turnBristleOff();
					turnBlobOff();
					turnLineOn();
					break;
				case 2://blob
					turnBristleOff();
					turnLineOff();
					turnBlobOn();
					break;
			}//end switch
		}//end brush type
			
			
			
			if (c.getGroup() == bristleBrushTypeRB) {
				bristleBrushType = (int)c.getValue();
				switch(bristleBrushType) {
				case 0://copy
					smudgePer.setVisible(false);
					turnColorBlendOff();
					turnhsbVarOff();
					break;
				case 1://dipped
					smudgePer.setVisible(false);
					turnColorBlendOff();
					turnhsbVarOff();
					break;
				case 2://smudge
					smudgePer.setVisible(true);
					turnColorBlendOff();
					turnhsbVarOff();
					break;
				case 3://paint
					smudgePer.setVisible(false);
					turnColorBlendOff();
					turnhsbVarOn();
					break;
				case 4://blend
					smudgePer.setVisible(false);
					turnColorBlendOn();
					turnhsbVarOff();
				}
			}
			
			if (c.getGroup() == blobBrushTypeRB) {
				blobBrushType = (int)c.getValue();
				switch(blobBrushType) {
				case 0://copy
					smudgePer.setVisible(false);
					turnhsbVarOff();
					turnColorBlendOff();
					break;
				case 1://dipped
					smudgePer.setVisible(false);
					turnhsbVarOff();
					turnColorBlendOff();
					break;
				case 2://smudge
					smudgePer.setVisible(true);
					turnhsbVarOff();
					turnColorBlendOff();
					break;
				case 3://paint
					smudgePer.setVisible(false);
					turnhsbVarOn();
					turnColorBlendOff();
					break;
				case 4://blend
					smudgePer.setVisible(false);
					turnhsbVarOff();
					turnColorBlendOn();
				}
			}
			
			
			if (c.getGroup() == blobShapeRB) {
				blobShape = (int)c.getValue();
				if(blobShape ==4) {
					turnBlobEllipseOptionsOn();
				}else {
					turnBlobEllipseOptionsOff();
				}
			}

			if (c.getGroup() == dropRB) {
				dropTo = (int)c.getValue();
			}
			if (c.getGroup() == fromRB) {
				posResFrom = (int)c.getValue();
			}
			if (c.getGroup() == bfromRB) {
				loadBrushFrom = (int)c.getValue();
			} 
			if (c.getGroup() == afromRB) {
				loadAlphaFrom = (int)c.getValue();
			} 
			if (c.getGroup() == lineBrushTypeRB) {
				lineBrushType = (int)c.getValue();

				if(lineBrushType == 1 || lineBrushType == 2) {//fan or scattered
					turnFanOn();
				}
				else {
					turnFanOff();	    		
				}
				
				if(lineBrushType == 4 ) {//drop
					turnDropShadowOn();
				}
				else {
					turnDropShadowOff();	    		
				}
				
				if(lineBrushType == 5 ) {//drop
					turnPerlinOn();
				}
				else {
					turnPerlinOff();
				}
			}


			
			

			if (c.getGroup() == sizeageRB) {
				sizeageType = (int) c.getValue();
			}
			if (c.getGroup() == sizehsbRB) {
				sizehsbType = (int) c.getValue();
			}
			if (c.getGroup() == blobTypeRB) {
				blobType = (int) c.getValue();
				switch (blobType ) {
					case 0:
						turnhsbVarOff();
						break;
					case 1:
						turnhsbVarOff();
						break;
					case 2:
						turnhsbVarOff();
						break;
					case 3:
						turnhsbVarOn();
						break;
					case 4:
						turnhsbVarOff();
						break;
				}
				newBrush = true;
			}
			

			if (c.getGroup() == lineTypeRB) {
				lineType = (int) c.getValue();
			}
			if  (c.getGroup() == velocityKindDD) {
				velocityKind = (int) c.getValue();
			}

			if  (c.getGroup() == velocityTypeDD) {
				velocityType = (int) c.getValue();
				if((velocityType == 1) || (velocityType == 2) || (velocityType == 3) || (velocityType == 4)){//any of the Angle from Color types
					if(velocityType ==1)	saa.setVisible(true);
					velocityKindDD.setVisible(true);
					velocityKindL.setVisible(true);}
				else {
					saa.setVisible(false);
					velocityKindDD.setVisible(false);
					velocityKindL.setVisible(false);
					//		    		strokeSpeedS.setVisible(true);
				}
			}

			
  			if  (c.getGroup() == startPosTypeDD) {
				startPosType = (int) c.getValue();
				if(startPosType == 1) { gridResXS.setVisible(true);gridResYS.setVisible(true);gridResXRanS.setVisible(true);gridResYRanS.setVisible(true);}
				else{gridResXS.setVisible(false);gridResYS.setVisible(false);gridResXRanS.setVisible(false);gridResYRanS.setVisible(false);}
			}



			if  (c.getGroup() == startPosResDD)  { 
				startPosResType = (int) c.getValue();
				if(startPosResType == 3) {startPosResRange.setVisible(false);}
				else {startPosResRange.setVisible(true);}
			}

			if  (c.getGroup() == strokeTypeRB) {
				strokeType = (int) c.getValue();
				if (strokeType == 1) { 	curveAmtSL.setVisible(true);curveRandomFacSL.setVisible(true);}
				else {curveAmtSL.setVisible(false);curveRandomFacSL.setVisible(false);}
			}
			
			if  (c.getGroup() == wCanvasRB) {
				whichCanvas = (int) c.getValue();
			}
		
			
			if  (c.getGroup() == svc) { 
				strokeVaryControl = (int) c.getValue(); 
			}


			if (c.getGroup()== brushShapeDD) {
				whichBrush = (int) c.getValue();
				switch (whichBrush) {
				case 0:
					GB.rawBrush = loadImage(GB.brush1);
					break;
				case 1:
					GB.rawBrush = loadImage(GB.brush2);
					break;
				case 2:
					GB.rawBrush = loadImage(GB.brush3);
					break;
				case 3:
					GB.rawBrush = loadImage(GB.brush4);
					break;
				case 4:
					GB.rawBrush = loadImage(GB.brush5);
					break;
				case 5:
					GB.rawBrush = loadImage(GB.brush6);
					break;
				case 6:
					GB.rawBrush = loadImage(GB.brush7);
					break;
				case 7:
					GB.rawBrush = loadImage(GB.brush8);
					break;
				case 8:
					GB.rawBrush = loadImage(GB.brush9);
					break;
				case 9:
					GB.rawBrush = loadImage(GB.brush10);
					break;
				case 10:
					GB.rawBrush = loadImage(GB.brush11);
					break;
				case 11:
					GB.rawBrush = loadImage(GB.brush12);
					break;
				case 12:
					GB.rawBrush = loadImage(GB.brush13);
					break;
				}
				GB.bW = GB.rawBrush.width;
				GB.bH = GB.rawBrush.height;
			} 
			

			if (c.getGroup() == StvCb) {
				HSBinvert = true;
				int n = (int)StvCb.getArrayValue()[0];
				if(n == 1) {HSBinvert = true; }
				else{HSBinvert = false;}
			}

			if (c.getGroup() == hueRestrict) {
				huePosRestrict = true;
				int n = (int)hueRestrict.getArrayValue()[0];
				if(n == 1) {huePosRestrict = true; }
				else{huePosRestrict = false;}
//				PApplet.println("huePosRestrict is "+huePosRestrict);
			}
			if (c.getGroup() == satRestrict) {
				satPosRestrict = true;
				int n = (int)satRestrict.getArrayValue()[0];
				if(n == 1) {satPosRestrict = true; }
				else{satPosRestrict = false;}
//				PApplet.println("satPosRestrict is "+satPosRestrict);
			}
			if (c.getGroup() == briRestrict) {
				briPosRestrict = true;
				int n = (int)briRestrict.getArrayValue()[0];
				if(n == 1) {briPosRestrict = true; }
				else{briPosRestrict = false;}
			}

			if (c.getGroup() == sizehsbCB) {
				int n = (int)sizehsbCB.getArrayValue()[0];
				if(n == 1) {sizeByHSBInvert = true;  }//sizehsbRB.setVisible(true); hsbMinMaxR.setVisible(true);
				else{sizeByHSBInvert = false; } //sizehsbRB.setVisible(false); hsbMinMaxR.setVisible(false);
			}
 
			if (c.getGroup() == sizetriCB) {
				int n = (int)sizetriCB.getArrayValue()[0];
				if(n == 1) {sizeBytri = true;  }
				else{sizeBytri = false; } 
			}
			
			if (c.getGroup() == hcb) {
				hcran = true;
				int n = (int)hcb.getArrayValue()[0];
				if(n == 1) {hcran = true; }
				else{hcran = false;}
			}
			if (c.getGroup() == scb) {
				scran = true;
				int n = (int)scb.getArrayValue()[0];
				if(n == 1) {scran = true;}
				else{scran = false;}
			}
			if (c.getGroup() == bcb) {
				bcran = true;
				int n = (int)bcb.getArrayValue()[0];
				if(n == 1) {bcran = true; }
				else{bcran = false;}
			}

			if (c.getGroup() == cts) {
				copyTrifromSource =  (int) c.getValue();
				println("copy tri from source is now "+copyTrifromSource);
			}
			
			if (c.getGroup() == cp1) {
				TintR = (int)(c.getArrayValue(0));
				TintG = (int)(c.getArrayValue(1));
				TintB = (int)(c.getArrayValue(2));
				Alph  = (int)(c.getArrayValue(3));
			}
			if (c.getGroup() == cp2) {
				BtintR = (int)(c.getArrayValue(0));
				BtintG = (int)(c.getArrayValue(1));
				BtintB = (int)(c.getArrayValue(2));
				Balph  = (int)(c.getArrayValue(3));
			}

			if  (c.getGroup() == dd8) {  	
				colorBlendType = (int) c.getValue();  
			}
		}//end if c.isGroup

		


		if (c.isController()) {// is c (the control event) a controller.....(as opposed to a group above)

			if(c.isFrom(huePosRange)) {
				huePosRestrictMin = (int)(c.getController().getArrayValue(0));
				huePosRestrictMax = (int)(c.getController().getArrayValue(1));
			}
			if(c.isFrom(satPosRange)) {
				satPosRestrictMin = (int)(c.getController().getArrayValue(0));
				satPosRestrictMax = (int)(c.getController().getArrayValue(1));
			}
			if(c.isFrom(briPosRange)) {
				briPosRestrictMin = (int)(c.getController().getArrayValue(0));
				briPosRestrictMax = (int)(c.getController().getArrayValue(1));
			}

			if(c.isFrom(hueRan)) {
				HueRanLow = c.getController().getArrayValue(0);
				HueRanHig = c.getController().getArrayValue(1);
			}
			if(c.isFrom(satRan)) {
				SatRanLow = c.getController().getArrayValue(0);
				SatRanHig = c.getController().getArrayValue(1);
			}
			if(c.isFrom(briRan)) {
				BriRanLow = c.getController().getArrayValue(0);
				BriRanHig = c.getController().getArrayValue(1);
			}
			if(c.isFrom(ageMinMaxR)) {
				ageMin = c.getController().getArrayValue(0);
				ageMax = c.getController().getArrayValue(1);
			}
			if(c.isFrom(hsbMinMaxR)) {
				hsbMin = c.getController().getArrayValue(0);
				hsbMax = c.getController().getArrayValue(1);
			}

			if (c.getController().getName().equals("reload pic")) {doCanvasCopy= true; whichCopy = 7;}
			if (c.getController().getName().equals("reload alt")) {doCanvasCopy= true; whichCopy = 8;}
			
			if (c.getController().getName().equals("pic to alt")) {doCanvasCopy = true; whichCopy = 11;}
			if (c.getController().getName().equals("pic to work")) {doCanvasCopy= true; whichCopy = 0;}
			if (c.getController().getName().equals("pic to control")) {doCanvasCopy = true; whichCopy = 2;}
			if (c.getController().getName().equals("pic to output")) {doCanvasCopy = true; whichCopy = 1;}
			
			if (c.getController().getName().equals("alt to work")) {doCanvasCopy = true; whichCopy = 6;}
			if (c.getController().getName().equals("alt to control")) {doCanvasCopy = true; whichCopy = 3;}
			if (c.getController().getName().equals("alt to output")) {doCanvasCopy = true; whichCopy = 12;}

			if (c.getController().getName().equals("work to alt")) {doCanvasCopy = true; whichCopy = 10;}
			if (c.getController().getName().equals("work to control")) {doCanvasCopy = true; whichCopy = 4;}
			if (c.getController().getName().equals("work to output")) {doCanvasCopy = true; whichCopy = 5;}
			
			if (c.getController().getName().equals("control to work")) {doCanvasCopy = true; whichCopy = 13;}
			if (c.getController().getName().equals("control to output")) {doCanvasCopy = true; whichCopy = 9;}
			
			if (c.getController().getName().equals("output to work")) {doCanvasCopy = true; whichCopy = 14;}
			
//			if (c.getController().getName().equals("copyTri fromSource")) {copyTrifromSource = true;}

			
			  if (c.getController().getName().equals("color")) { mods.solidColor(this); }
			  if (c.getController().getName().equals("blur")) {mods.blur(this);}
			  if (c.getController().getName().equals("poster")) { mods.posterize(this);}
			  if (c.getController().getName().equals("erode")) { mods.erode(this);}
			  if (c.getController().getName().equals("dilate")) { mods.dilate(this);}
			  if (c.getController().getName().equals("threshH")) { mods.threshH(this);}
			  if (c.getController().getName().equals("invert")) { mods.invert(this);}
			  
			  if (c.getController().getName().equals("blobs")) {doMeshDisplay = true; whichMesh = 1; }
			  if (c.getController().getName().equals("verts")) { doMeshDisplay = true; whichMesh = 2;}
			  if (c.getController().getName().equals("spiro")) {doMeshDisplay = true; whichMesh = 4;}
			  if (c.getController().getName().equals("mesh")) { doMeshDisplay = true; whichMesh = 0;}


			  
		}
	}//end event controller


///************functions for turning groups of controllers on and off
	
	void turnBristleOn() {
		println("turning bristle on");
		bristleBrushTypeRB.setVisible(true);
		bristleBrushTypeL.setVisible(true);
		brushRotateSL.setVisible(true);
		turnBrushSSSROn();
		turnSizeOn();
	}

	void turnBristleOff() {
		println("turnBristleOff");
		bristleBrushTypeRB.setVisible(false);
		bristleBrushTypeL.setVisible(false);
		turnBristleOptionsOff();
	}
	
	void turnBristleOptionsOff() {
		println("turnBristleOptionsOff");
		turnBrushSSSROff();
		smudgePer.setVisible(false);
		turnhsbVarOff();
		turnColorBlendOff();
		turnSizeOff();
		brushRotateSL.setVisible(false);
	}
	
	void turnLineOn() {
		lineBrushTypeRB.setVisible(true);
		lineBrushTypeL.setVisible(true);
		strWeightS.setVisible(true);
	}
	
	void turnLineOff() {
		lineBrushTypeRB.setVisible(false);
		lineBrushTypeL.setVisible(false);
		turnLineOptionsOff();
	}

	void turnLineOptionsOff() {
		turnPerlinOff();
		turnFanOff();
		turnDropShadowOff();
		strWeightS.setVisible(false);
	}
	
	void turnBlobOn() {
		blobBrushTypeRB.setVisible(true);
		blobBrushTypeL.setVisible(true);
		blobShapeRB.setVisible(true);
		blobShapeL.setVisible(true);
		brushRotateSL.setVisible(true);
		blobFixS.setVisible(true);
		turnSizeOn();
	}
	
	void turnBlobOff() {
		blobBrushTypeRB.setVisible(false);
		blobBrushTypeL.setVisible(false);
		blobShapeRB.setVisible(false);
		blobShapeL.setVisible(false);
		turnBlobOptionsOff();
//		turnBrushSSSROff();
	}
	
	void turnBlobOptionsOff() {
		turnBlobEllipseOptionsOff();
		brushRotateSL.setVisible(false);
		blobFixS.setVisible(false);
		turnSizeOff();
	}

	
	
	void turnBrushSSSROn() {
		brushShapeDD.setVisible(true);	brushShapeL.setVisible(true);	brushSizeSL.setVisible(true);	brushSkewXSL.setVisible(true);	brushRotateSL.setVisible(true);
	}
	void turnBrushSSSROff() {
		brushShapeDD.setVisible(false);	brushShapeL.setVisible(false);	brushSizeSL.setVisible(false);	brushSkewXSL.setVisible(false);	brushRotateSL.setVisible(false);
	}
	
	
	void turnPerlinOff() {
		perlinLengthS.setVisible(false);
		perlinSpaceS.setVisible(false);
		perlinNoiseXS.setVisible(false);
		perlinNoiseYS.setVisible(false);
		perlinNoiseZS.setVisible(false);
		perlinCurlS.setVisible(false);
		perlinOctS.setVisible(false);
		perlinFadS.setVisible(false);
	}
	
	void turnPerlinOn() {
		perlinLengthS.setVisible(true);
		perlinSpaceS.setVisible(true);
		perlinNoiseXS.setVisible(true);
		perlinNoiseYS.setVisible(true);
		perlinNoiseZS.setVisible(true);
		perlinCurlS.setVisible(true);
		perlinOctS.setVisible(true);
		perlinFadS.setVisible(true);
	}
	
	void turnFanOn() {
		fanNumLinS.setVisible(true);
		fanLineLngS.setVisible(true);
	}
	void turnFanOff() {
		fanNumLinS.setVisible(false);
		fanLineLngS.setVisible(false);
	}
	
	void turnSizeOn() {
		println("turnSizeOn");
		sizehsbCB.setVisible(true);
		sizehsbRB.setVisible(true);
		sizeageRB.setVisible(true);
		ageMinMaxR.setVisible(true);
		sizetriCB.setVisible(true);
		hsbMinMaxR.setVisible(true);
		hueCenterSL.setVisible(true);
	}
	void turnSizeOff() {
		println("turnSizeOff");
		sizehsbCB.setVisible(false);
		sizehsbRB.setVisible(false);
		sizeageRB.setVisible(false);
		ageMinMaxR.setVisible(false);
		sizetriCB.setVisible(false);
		hsbMinMaxR.setVisible(false);
		hueCenterSL.setVisible(false);
	}
	void turnhsbVarOn() {
		Hbv.setVisible(true);
		Sbv.setVisible(true);
		Bbv.setVisible(true);
	}
	void turnhsbVarOff() {
		Hbv.setVisible(false);
		Sbv.setVisible(false);
		Bbv.setVisible(false);
	}
	
	void turnColorBlendOff() {
		cb.setVisible(false); cp1.setVisible(false); l8.setVisible(false); dd8.setVisible(false);
	}
	void turnColorBlendOn() {
		cb.setVisible(true); cp1.setVisible(true); l8.setVisible(true); dd8.setVisible(true);
	}
	
	void turnDropShadowOn() {
		dropXS.setVisible(true); dropYS.setVisible(true); dropAS.setVisible(true); dropSizeS.setVisible(true);
	}
	void turnDropShadowOff() {
		dropXS.setVisible(false); dropYS.setVisible(false); dropAS.setVisible(false); dropSizeS.setVisible(false);
	}
	
	void turnBlobEllipseOptionsOff() {
		numpntsS.setVisible(false);	blobRadS.setVisible(false);	controlblobRadS.setVisible(false);
	}
	void turnBlobEllipseOptionsOn() {
		numpntsS.setVisible(true);	blobRadS.setVisible(true);	controlblobRadS.setVisible(true);
	}
	
	public void keyPressed() {  // used to move the screen display around the graphics buffer - or save a version to a file
		GB.externalInput(key,keyCode);
	}
}// end CF class
