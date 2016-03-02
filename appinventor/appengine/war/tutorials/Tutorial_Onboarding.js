/* Step Object */
// step = function(text, validate, url){
// 	this.text=text;
// 	this.validate=validate;
// 	this.url=url;
// }

var Tutorial_Onboarding = {
	title: "App Inventor Tour",
	difficulty: "easy",
	noCheck: function(){
		return true;
	},
	steps:
		[
			{
				text: "Welcome to App Inventor! Before you get started, we'd like to take you on a short tour.<br><a target='_blank' href='http://appinventor.mit.edu/explore/designer-blocks.html'>See diagrams instead.</a>",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 1,
				left: 233
			},
			{
				text: "This view is called the <b>Designer</b>. This is where you will design the look and feel of your app.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3
			},
			{
				text: "This is the <b>Palette</b>. It stores all of the components you can use in your apps in these labeled drawers.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 175,
				left: 14
			},
			{
				text: "This is the <b>Viewer</b>. It shows what the app will look like on a phone screen. Drag and drop components from the Palette here to add them to your app.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 145,
				left: 235
			},
			{
				text: "This is the list of <b>Components</b>. All of the components in the viewer will appear in a nested list here. You can select, rename and delete components from this column.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 170,
				left: window.innerWidth-450
			},
			{
				text: "This is a list of the <b>Properties</b> specific to the selected component in the app. You can change the component's size, text, color, etc.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 170,
				left: window.innerWidth-400
			},
			{
				text: "These buttons toggle between the Designer and Blocks views. In the Blocks view, you can program the behavior of your app. Click the Blocks button.",
				validate: function(formName){
					var truth = BlocklyPanel_InBlocksView();
					return truth;
				},
				url: "",
				top: 103,
				left: window.innerWidth-400
			},
			{
				text: "This is the <b>Workspace</b>. You drag and drop blocks from the drawers on the left to this workspace and snap them together to program your app's behavior.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3
			},
			{
				text: "These are the built-in blocks. They are the general behaviors for your apps and are always available.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 175,
				left: 145
			},
			{
				text: "Component blocks will be here, nested under Screen1. These blocks are specific to the components you added to your app in the <b>Viewer</b>.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 395,
				left: 145
			},
			{
				text: "Any Component blocks are more advanced blocks that operate on groups of similar components and are often used inside of loops, such as changing the positions of a group of sprites.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 420,
				left: 145
			},
			{
				text: "The Backpack lets you save copies of your blocks so that you can carry them around and use them in other projects and on other screens. <br><a target='_blank' href='http://ai2.appinventor.mit.edu/reference/other/backpack.html'>More information...</a>",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 235,
				left: window.innerWidth-400
			},
			{
				text: "Up here at the top, you can find options to live-test your app, generate an installable .apk, get help, share your app in the Gallery, and more.",
				validate: function(formName){
					return true;
				},
				url: "",
				top: 55,
				left: 250
			},
			{
				text: "Have fun inventing!",
				validate: function(formName){
					return true;
				},
				url: "",
				top: window.innerHeight/3,
				left: window.innerWidth/3
			}
		]
};