/*This file is for useful functions needed for the tutorial*/

//var proj_number=$wnd.GetCurrentView();
var currentStepIndex=0;
// var steps=[];


changeText = function(message){

	document.getElementsByClassName("Caption")[0].innerHTML=message;
	
}

changePosition=function(top, left){
	var div=document.getElementById("tutorialDialog");
	div.style.left=left+"px";
	div.style.top=top+"px";
}

/* Javascript functions for testing truthness of steps */

isButtonClickThere=function(proj_number){
	var blocklies=Blocklies[proj_number+"_Screen1"];
	var allblocks=blocklies.mainWorkspace.getAllBlocks();
	var truth=false;
	for (var i=0; i<allblocks.length; i++ ){
		var block=allblocks[i];
		if (block.eventName=="Click" & block.typeName=="Button"){
			truth=true;
		}
	}
	return truth;
}

isTextToSpeechThere=function(proj_number){
	var blocklies=Blocklies[proj_number+"_Screen1"];
	var truth=false;
	if (blocklies!=null){

		var allblocks=blocklies.mainWorkspace.getAllBlocks();
		for (var i=0; i<allblocks.length; i++ ){
			var block=allblocks[i];
			if (block.methodName=="Speak" & block.typeName=="TextToSpeech"){
				var target=block.previousConnection.targetConnection;
				if (target!=null){
					var sourceblock=target.sourceBlock_;
					if (sourceblock.eventName=="Click" & sourceblock.typeName=="Button"){
						truth=true;
					}
				}
			}
		}
	}
	return truth;
}

isTextBlockThere=function(proj_number){
	var blocklies=Blocklies[proj_number+"_Screen1"];
	var truth=false;
	if (blocklies!=null){

		var allblocks=blocklies.mainWorkspace.getAllBlocks();
		for (var i=0; i<allblocks.length; i++ ){
			var block=allblocks[i];
			if (block.methodName=="Speak" & block.typeName=="TextToSpeech"){
				var arg=block.getInput("ARG0");
				if (arg!=null){
					var source=arg.connection.targetConnection.sourceBlock_;
					if (source.type=="text"){
						truth=true;
					}
				}
			}
		}
	}
	return truth;
}

doesTextBlockHaveText=function(proj_number){
	var blocklies=Blocklies[proj_number+"_Screen1"];
	var allblocks=blocklies.mainWorkspace.getAllBlocks();
	var truth=false;
	for (var i=0; i<allblocks.length; i++ ){
		var block=allblocks[i];
		if (block.type=="text"){
			if (block.getTitleValue("TEXT").length>0){
				truth=true;
			}
		}
	}
	return truth;
}



/**Use for testing truth at each step**/


/**Use to switch between tests **/
nextStep=function(){
	currentStepIndex+=1;
	window["step_"+currentStepIndex]();
}


/*JS functions for each step of the tutorial*/
step_1=function(){
	changeText("This screen is called the <b>Design tab</b>. This is where you design what your app looks like. ");
}

step_2=function(){
	changeText("This is the <em>Viewer</em>. Drag components from the Palette to the Viewer to see what your app will look like.");
	// var elements=document.getElementsByClassName("ode-Box-header-caption");
	// for (var i=0; i<elements.length;i++){
	// 	if (elements[i].innerHTML=="Viewer"){
	// 		el=elements[i];
	// 	}
	// }
	// var top=el.top+15;
	// var left=el.left+15;
	// changePosition(top,left);
}

step_3=function(){
	changeText("This is the <em>Palette</em>. The Palette is made of several drawers: User Interface, Media, Layout, Drawing and Animation, Sensors, and more. Find components and drag them to the Viewer to add them to your app.");
}

step_4=function(){
	changeText("Click and hold a Button component. Drag it from the Palette to the Viewer. Note that Buttons are found in the User Interface drawer. Notice how your connected device should display a Button on the screen.");
}

step_5=function(){
	changeText("These are the properties of a Button. Change the Text property to display the words Talk To Me on the button.");
}

step_6=function(){
	changeText("Click and hold a TextToSpeech. Drag it from the Palette to the Viewer.. This component will show up in the Non-Visible components area below the phone screen. Note that this component is found in the Media drawer.");
}

step_7=function(){
	changeText("Now let’s program our app! Click the Blocks button to go to the Blocks Tab.");
}
step_8=function(){
	//TODO call/test truth of inBlocksView
	changeText("This is the Workspace. Drag blocks from the drawers to the Workspace to build relationships and behavior.");
}
step_9=function(){
	changeText("These are Built-In Blocks. Find blocks for general behaviors you may want to add to your app. ");
}
step_10=function(){
	changeText("These are Component Blocks. Find blocks for behaviors for specific components. ");
}
step_11=function(){
	changeText("Find the block that says When Button1 Click from the Button Drawer. Click and hold this block. Drag it into your workspace. ");
}
step_12=function(){
	if (isButtonClickThere()){
		changeText("Find the block that says TextToSpeech1.Speak from the TextToSpeech Drawer. Click and hold this block. Drag it inside the Button Click block so that they fit together. ");
	}
	else{
		currentStepIndex-=1;
	}
}
step_13=function(){
	if (isTextToSpeechThere()){
		changeText("Almost done! Now you just need to tell the TextToSpeech.Speak block what to say. To do that, click on the Text drawer under Built-In. Drag out a text block and plug it into the socket labeled 'message'. ");
	}
	else{
		currentStepIndex-=1;
	}
}
step_14=function(){
	if (isTextBlockThere()){
		changeText("Clicking on the text block will allow you to type a message. Type the message: Congratulations! You’ve made your first app!");
	}
	else{
		currentStepIndex-=1;
	}
}
step_15=function(){
	if (doesTextBlockHaveText()){
		changeText("Go to your connected device and click the button. Make sure your volume is up! You should hear the phone speak the phrase out loud. (This works even with the emulator.) ");
	}
	else{
		currentStepIndex-=1;
	}
}
step_16=function(){
	changeText("Congratulations! You made your first app! If you’d like to make a new project, click here or click here to view some of our web tutorials.");
}