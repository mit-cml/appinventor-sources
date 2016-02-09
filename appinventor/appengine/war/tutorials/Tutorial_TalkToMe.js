/* Step Object */
// step = function(text, validate, url){
// 	this.text=text;
// 	this.validate=validate;
// 	this.url=url;
// }


/* Tutorial Object */

/*Every tutorial will be harcoded into a tutorial object which can easily be run */

var Tutorial_TalkToMe = {
	title: "Talk To Me",
	difficulty: "easy",
	noCheck: function(){
		return true;
	},
	steps:
		[
			{
				text: "Welcome to App Inventor! To get started, we'll be guiding you through the process of building an app that allows your phone to talk to you with the press of a button.<br><br>",
				validate: function(formName){
					return true;
				},
				url: "",
			},
			{
				text: "Drag a button from the palette to the viewer.<br><br>",
				validate: function(formName){
					return Tutorial.testForComponent("Button");
				},
				url: "",
				top: 1,
        left: 233
			},
			{
				text:"Change the button's <i>Text</i> property to display the words " + '"Talk To Me".',
				validate: function(formName){
					return true;
				},
				url: "",
			},
		  {
      	text: "Connect your phone or the emulator to App Inventor. You should see your Talk To Me button on the screen.<br><br>If you have never connected your phone (or emulator), <a target='_blank' href='http://appinventor.mit.edu/explore/ai2/setup.html'>follow these instructions</a> and then come back to this tutorial.<br><br>",
        validate: function (formName) {
        	return true;
        },
        url: "",
      },
			{
				text: "Drag a <b>TextToSpeech</b> component from the Media drawer to the viewer. This component will show up in the Non-Visible components area below the phone screen.<br><br>",
				validate: function(formName){
					return Tutorial.testForComponent("TextToSpeech");
				},
				url: "",
			},
			{
				text: "Now let's program our app! Click the Blocks button to go to the Blocks Tab.<br><br>",
				validate: function(formName){
					var truth= BlocklyPanel_InBlocksView();
					return truth;
				},
				url: "",
			},
			{
				text: "Add the <b>when Button1.Click</b> block from the Button1 drawer to the workspace.<br><br>",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						return block.eventName=="Click" & block.typeName=="Button";
					});
				},
				url: "",
				top: 1,
				left: 521
			},
			{
				text: "From the TextToSpeech drawer, drag the <b>TextToSpeech1.Speak</b> inside the <b>Button1.Click</b> block so that they fit together.<br><br>",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						if (block.methodName=="Speak" & block.typeName=="TextToSpeech"){
							var target=block.previousConnection.targetConnection;
							if (target!=null){
								var sourceblock=target.sourceBlock_;
								return sourceblock.eventName=="Click" & sourceblock.typeName=="Button";
							}
						}
						return false;
					});
				},
				url: ""
			},
			{
				text: "Almost done! Now you just need to tell the <b>TextToSpeech.Speak</b> block what to say.<br>Click on the Text drawer under Built-In. Drag out a <b>text</b> block and plug it into the socket labeled <i>message</i>.<br><br>",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						if (block.methodName=="Speak" & block.typeName=="TextToSpeech"){
							var arg=block.getInput("ARG0").connection.targetConnection;
							if (arg!=null){
								var source=arg.sourceBlock_;
								return source.type=="text";
							}
						}
						return false;
					});
				},
				url: ""
			},
			{
				text: "Click on the text block to type a message. Type the message: Congratulations! You've built your first app!<br><br>",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						return block.type=="text" && block.getTitleValue("TEXT").length>0;
					});
				},
				url: ""
			},
			{
				text: "Go to your connected device and click the button. Make sure your volume is up! You should hear the phone speak the phrase out loud. (This works even with the emulator.)<br><br>",
				validate: function(formName){
					return true;
				},
				url: ""
			},
			{
				text: "Congratulations! You've finished this app! If you'd like to make a new project, click on Projects --> Start new project or click <a target='_blank' href='http://appinventor.mit.edu/explore/ai2/tutorials.html'>here</a> to view some of our web tutorials.",
				validate: function(formName){
					return true;
				},
				url: ""
			},
		]


};