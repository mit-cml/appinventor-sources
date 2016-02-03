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
				text: "Welcome to App Inventor! To get started, we'll be guiding you through the process of building an app that allows your phone to talk to you with the press of a button.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3 
			},
			{
				text: "This entire screen is called the <em>Design tab</em>. It contains the Palette, Viewer and list of Components. This is where you design what your app looks like. ",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3 
			},
			{
				text: "This part of the screen is called the <em>Viewer</em>. You can drag components from the Palette to the Viewer to see what your app will look like.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3 
			},
			{
				text: "This is the <em>Palette</em>. The Palette is made of several drawers: User Interface, Media, Layout, Drawing and Animation, Sensors, and more. You can find components and drag them to the Viewer to add them to your app.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: 75 
			},
			{
				text: "Click and hold a Button component. Drag it from the Palette to the Viewer. Note that Buttons are found in the User Interface drawer. Notice how your connected device should display a Button on the screen.",
				validate: function(formName){
					return Tutorial.testForComponent("Button");
				},
				url: "",
				top: 50,
				left: window.innerWidth*2/3 
			},
			{
				text: 	"These are the properties of a Button. Change the Text property to display the words Talk To Me on the button.",

				validate: function(formName){
					return true;
				},
				url: "",
				top: 50,
				left: window.innerWidth*2/3-175 
			},
			{
				text: "Open the Media drawer in the Palette. Click and hold a TextToSpeech. Drag it from the Palette to the Viewer. This component will show up in the Non-Visible components area below the phone screen.",
				validate: function(formName){
					return Tutorial.testForComponent("TextToSpeech");
				},
				url: "",
				top: 50,
				left: window.innerWidth*2/3-175
			},
			{
				text: "Now let's program our app! Click the Blocks button to go to the Blocks Tab.",
				validate: function(formName){
					var truth= BlocklyPanel_InBlocksView();
					return truth;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3 
			},
			{
				text: "This is the Workspace. You can drag blocks from the drawers to the Workspace to build relationships and behavior.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3 
			},
			{
				text: "These are Built-In Blocks. You can find blocks for general behaviors you may want to add to your app. ",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/6,
				left: window.innerWidth/6 
			},
			{
				text: "These are Component Blocks. You can find blocks for behaviors for specific components. ",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/2,
				left: window.innerWidth/6 
			},
			{
				text: "Find the block that says When Button1 Click from the Button Drawer. Click and hold this block. Drag it into your workspace. ",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						return block.eventName=="Click" & block.typeName=="Button";
					});
				},
				url: "",
				top: 50,
				left: window.innerWidth*2/3
			},
			{
				text: "Find the block that says TextToSpeech1.Speak from the TextToSpeech Drawer. Click and hold this block. Drag it inside the Button Click block so that they fit together. ",
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
				text: "Almost done! Now you just need to tell the TextToSpeech.Speak block what to say. To do that, click on the Text drawer under Built-In. Drag out a text block and plug it into the socket labeled 'message'. ",
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
				text: "Clicking on the text block will allow you to type a message. Type the message: Congratulations! You've built your first app!",
				validate: function(formName){
					return Tutorial.testForBlock(formName, function(block) {
						return block.type=="text" && block.getTitleValue("TEXT").length>0;
					});
				},
				url: ""
			},
			{
				text: "Go to your connected device and click the button. Make sure your volume is up! You should hear the phone speak the phrase out loud. (This works even with the emulator.) ",
				validate: function(formName){
					return true;
				},
				url: ""
			},
			{
				text: "Congratulations! You made your first app! If you'd like to make a new project, click on Projects --> Start new project or click <a href='http://appinventor.mit.edu/explore/ai2/tutorials.html'>here</a> to view some of our web tutorials.",
				validate: function(formName){
					return true;
				},
				url: ""
			},
		]


};