var currentStepIndex=0;
var proj_number;
var BlocklyPanel_tutorial;
// var steps=[];

setTutorial=function(tutorial){
	BlocklyPanel_tutorial=tutorial;
}

changeText = function(message){

	document.getElementsByClassName("Caption")[0].innerHTML=message;
	
}

changePosition=function(top, left){
	var div=document.getElementById("tutorialDialog");
	div.style.left=left+"px";
	div.style.top=top+"px";
}

/* Javascript functions for testing truthness of steps */

isButtonClickThere=function(){
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

isTextToSpeechThere=function(){
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

isTextBlockThere=function(){
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

doesTextBlockHaveText=function(){
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
nextStep=function(proj_number){
	if (BlocklyPanel_tutorial.steps[currentStepIndex].validate(proj_number)){
		currentStepIndex+=1;
		changeText(BlocklyPanel_tutorial.steps[currentStepIndex].text);
	}
}


