var Tutorial = {
	currentStepIndex: 0,
	currentTutorial: "None",
	setTutorial: function(tutorial){
		Tutorial.currentTutorial=tutorial;
	},

	changeText: function(message){
		document.getElementsByClassName("Caption")[0].innerHTML=message;
	},
	changePosition: function(top, left){
		var div=document.getElementById("tutorialDialog");
		div.style.left=left+"px";
		div.style.top=top+"px";
	},
	/**Use to switch between steps **/
	nextStep: function(formName){
		if (Tutorial.currentStepIndex==Tutorial.currentTutorial.steps.length){
		}
		else if (Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].validate(formName)){
			Tutorial.currentStepIndex+=1;
			Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
		}
	},
	backStep: function(formName){
		if (Tutorial.currentStepIndex!=0){
			Tutorial.currentStepIndex=Tutorial.currentStepIndex-1;
			Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
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




