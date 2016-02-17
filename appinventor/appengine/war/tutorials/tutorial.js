var Tutorial = {
	currentStepIndex: 0,
	currentTutorial: "None",
	setTutorial: function(tutorial){
		Tutorial.currentTutorial=window[tutorial];
		Tutorial.currentStepIndex = 0;
		Tutorial.changeText(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].text);
	},

	changeText: function(message){
		document.getElementById('tutorialDialog').getElementsByClassName("Caption")[0].innerHTML=message;
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
		if (Tutorial.currentStepIndex !== Tutorial.currentTutorial.steps.length-1){
			var currentStep = Tutorial.currentTutorial.steps[Tutorial.currentStepIndex];
			if (!currentStep.validate || currentStep.validate(formName)) {
				document.getElementById("backButton").style.visibility = 'visible';
				nextStepErrorMsg.style.display = 'none';

				Tutorial.currentStepIndex += 1;
				var newStep = Tutorial.currentTutorial.steps[Tutorial.currentStepIndex]
				Tutorial.changeText(newStep.text);
				Tutorial.changePosition(newStep.top, newStep.left);
				//Tutorial.changeImage(Tutorial.currentTutorial.steps[Tutorial.currentStepIndex].url);
				if (Tutorial.currentStepIndex === Tutorial.currentTutorial.steps.length - 1) {
					//hide Next button if on last step
					document.getElementById("nextButton").style.visibility = 'hidden';
				}
			} else {
				//there is a next step, but the user has not finished this step yet.
				nextStepErrorMsg.style.display = 'block';
			}
		}
	},

	backStep: function(formName){
		if (Tutorial.currentStepIndex!=0){
			Tutorial.currentStepIndex=Tutorial.currentStepIndex-1;
			var newStep = Tutorial.currentTutorial.steps[Tutorial.currentStepIndex]
			Tutorial.changeText(newStep.text);
			Tutorial.changePosition(newStep.top, newStep.left);
			document.getElementById("nextButton").style.visibility = 'visible';
			document.getElementById("nextStepErrorMsg").style.display = 'none';
			if (Tutorial.currentStepIndex == 0) {
				document.getElementById("backButton").style.visibility = 'hidden';
			}
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
	},

	testForBlock: function(formName, validatingFunction) {
		return Tutorial.testForBlocks(formName, validatingFunction, 1);
	},

	testForBlocks: function(formName, validatingFunction, times) {
		var blocklies = Blocklies[formName];
		var count = 0;
		if (blocklies != null){
			var allBlocks = blocklies.mainWorkspace.getAllBlocks();
			for (var j = 0; j < allBlocks.length; j++) {
				if (allBlocks[j] != null &&
					validatingFunction(allBlocks[j])) {
					count++;
					if (count >= times) {
						return true;
					}
				}
			}
		}
		return false;
	},

	getTutorialMetaData: function() {
		var tutorialFileNames = Object.keys(window).filter(function(key) {
			return key.startsWith("Tutorial_");
		});
		return tutorialFileNames.map(function(fileName) {
			return {
				fileName: fileName,
				title: window[fileName].title,
				difficulty: window[fileName].difficulty
			};
		});
	}
};