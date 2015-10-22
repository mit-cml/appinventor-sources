var Tutorial = {
	currentStepIndex: 0,
	currentTutorial: "None",
	setTutorial: function(tutorial){

		Tutorial.currentTutorial=window[tutorial];
		Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
	},

	changeText: function(message){
		document.getElementsByClassName("Caption")[0].innerHTML=message;
	},
	changePosition: function(top, left){
		var div=document.getElementById("tutorialDialog");
		div.style.left=left+"px";
		div.style.top=top+"px";
	},
	changeImage: function(img_src){
		var e=document.getElementById("Tutorial_frame");
		e.src=img_src;
	},
	/**Use to switch between steps **/
	nextStep: function(formName){
		var nextStepErrorMsg = document.getElementById("nextStepErrorMsg");
		if (Tutorial.currentStepIndex==Tutorial.currentTutorial.steps.length-1){
			nextStepErrorMsg.style.display = 'none';
			Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
		}
		else if (Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].validate(formName)){
			nextStepErrorMsg.style.display = 'none';
			Tutorial.currentStepIndex+=1;
			Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
			Tutorial.changePosition(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].top,Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].left);
			//Tutorial.changeImage(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].url);
			if (Tutorial.currentStepIndex === Tutorial.currentTutorial.steps.length-1) {
				document.getElementById("nextButton").style.visibility = 'hidden';
			}
		} else {
			//there is a next step, but the user has not finished this step yet.
			nextStepErrorMsg.style.display = 'block';
		}
	},
	backStep: function(formName){
		if (Tutorial.currentStepIndex!=0){
			Tutorial.currentStepIndex=Tutorial.currentStepIndex-1;
			Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
			document.getElementById("nextButton").style.visibility = 'visible';
			nextStepErrorMsg.style.display = 'none';
		}
	},
	testForComponent: function(component_name){
		var componentsArrayList=BlocklyPanel_GetComponentNames();
		var components = componentsArrayList.array;
		for (var i=0; i<components.length; i++){
			if (components[i]==component_name){
				return true;
			}
		}
		return false;
	}

}




