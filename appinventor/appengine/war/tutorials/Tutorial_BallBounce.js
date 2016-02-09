var Tutorial_BallBounce = {
    title: "Ball Bounce",
    difficulty: "easy",
    steps: [
        {
            text: "In this tutorial, you will learn about animation in App Inventor by making a ball (a sprite) bounce around the screen (on a canvas).<br><br>",
            validate: function (formName) {
                return true;
            },
            top: 1,
            left: 233
        },
        {
            text: 'To allow the Canvas to fill the whole screen, verify that the Screen1 property "Scrollable" is unchecked (the box is near the bottom of the Properties pane).<br><br>',
            validate: function (formName) {
//                return Tutorial.testForScreenProperty({ Scrollable: false});
                return true;
            },
            url: "",
        },
        {
            text: "From the Drawing and Animation drawer in the palette on the left, drag out a Canvas component and drop it onto the viewer.<br><br>",
            validate: function (formName) {
                return Tutorial.testForComponent("Canvas");
            },
            url: "",
        },
        {
            text: 'Make sure the Canvas component is selected in the Components list so that its properties show up in the Properties Pane.<br><br>Set the Height and Width properties to "Fill Parent".<br><br>',
            validate: function (formName) {
//                return Tutorial.testForComponentProperty("Canvas", { Height: "Fill Parent"});
//                return Tutorial.testForComponentProperty("Canvas", { Width: "Fill Parent"});
                return true;
            },
            url: "",
        },
        {
            text: "Now that we have a Canvas on the screen, we can add a Ball. Drag a Ball component from the Drawing and Animation drawer and drop it onto the Canvas.<br><br>You can adjust the size of the ball by changing its Radius property in the Properties pane.<br><br>",
            validate: function (formName) {
                return Tutorial.testForComponent("Ball");
            },
            url: "",
        },
        {
            text: "Let's add some behavior to our app! Open the Blocks editor.<br><br>",
            validate: function (formName) {
                return BlocklyPanel_InBlocksView();
            },
            url: "",
        },
        {
            text: "In the Blocks list on the left, click Ball1 to open the Ball1 blocks drawer. Drag and drop the <b>when Ball1.Flung</b> block onto the workspace.<br><br>Flung refers to the user making a " + '"Fling gesture" with his/her finger to "fling" the ball. Fling is a finger flick gesture.<br><br>',
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    return block.eventName == "Flung" && block.typeName == "Ball";
                });
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: 'Open the Ball1 drawer again and scroll down in the list of blocks to get the green ' + "<b>set Ball1.Heading</b> and <b>set Ball1.Speed</b> blocks. Put these blocks into the <b>Flung</b> event handler.<br><br>",
            validate: function (formName) {
                return Tutorial.testForBlocks(formName, function (block) {
                    console.log("this", this);
                    console.log("heading", this.propertyName === "Heading");
                    console.log("both", this.propertyName === "Heading" || "Speed");
                    //this doesn't quite work. I was able to continue on to the next page with just 2 Ball set blocks.
                    if (block.typeName === 'Ball' && block.setOrGet === 'set' && (this.propertyName === "Heading" || this.propertyName === "Speed")) {
                        var surroundingParent = block.getSurroundParent();
                        return surroundingParent != null && surroundingParent.eventName === "Flung" && surroundingParent.typeName === "Ball";
                    } else {
                        return false;
                    }
                }, 2);
            },
            url: "",
        },
        {
            text: "Mouse over the <i>speed</i> parameter of the <b>Flung</b> event handler. The get and set blocks for the speed of the fling will pop up. Grab the <b>get speed</b> block and plug that into the <b>set Ball1.Speed</b> block.<br><br>",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    if (block.category === 'Variables' && block.getVars().length === 1 && block.getVars()[0] === 'speed') {
                        if (block.outputConnection && block.outputConnection.targetConnection && block.outputConnection.targetConnection.sourceBlock_) {
                            var outputTargetBlock = block.outputConnection.targetConnection.sourceBlock_;
                            return outputTargetBlock.typeName === 'Ball' && outputTargetBlock.setOrGet === 'set' && outputTargetBlock.propertyName === "Speed";
                        }
                    }
                    return false;
                });
            },
            url: "",
        },
        {
            text: "Do the same for the Ball's heading using the Flung block's <i>heading</i> parameter.<br><br>",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    if (block.category === 'Variables' && block.getVars().length === 1 && block.getVars()[0] === 'heading') {
                        if (block.outputConnection && block.outputConnection.targetConnection && block.outputConnection.targetConnection.sourceBlock_) {
                            var outputTargetBlock = block.outputConnection.targetConnection.sourceBlock_;
                            return outputTargetBlock.typeName === 'Ball' && outputTargetBlock.setOrGet === 'set' && outputTargetBlock.propertyName === "Heading";
                        }
                    }
                    return false;
                });
            },
            url: "",
        },
        {
            text: "It's a good habit to test your apps while you build. App Inventor lets you live-test using the Companion app on your phone (or emulator). Connect and play with your app!<br><br>If you have never connected your phone (or emulator), <a target='_blank' href='http://appinventor.mit.edu/explore/ai2/setup.html'>follow these instructions</a> and then come back to this tutorial.<br><br>",
            validate: function (formName) {
                return true;
            },
            url: "",
        },
        {
            text: 'Fling the ball! But wait, why does it get stuck on the side of the screen?!<br><br>The ball gets stuck because its heading has not changed. To make the ball "bounce" off the edge of the screen, add a new event handler, <b>when Ball1.EdgeReached</b>.<br><br>',
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    return block.eventName === "EdgeReached" && block.typeName === "Ball";
                });
            },
            url: "",
        },
        {
            text: "Next add a purple <b>call Ball1.Bounce</b> block to the <b>when Ball1.EdgeReached</b> block.<br><br>",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    if (block.typeName === 'Ball' && block.methodName === 'Bounce') {
                        var surroundingParent = block.getSurroundParent();
                        return surroundingParent != null && surroundingParent.eventName === "EdgeReached" && surroundingParent.typeName === "Ball";
                    } else {
                        return false;
                    }
                });
            },
            url: "",
        },
        {
            text: "Take the <b>get edge</b> block from <b>Ball1.EdgeReached</b>'s <i>edge</i> parameter and plug it into the <i>edge</i> parameter of <b>Ball1.Bounce</b>.<br><br>",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    if (block.category === 'Variables' && block.getVars().length === 1 && block.getVars()[0] === 'edge') {
                        if (block.outputConnection && block.outputConnection.targetConnection) {
                            var outputTargetBlock = block.outputConnection.targetConnection.sourceBlock_;
                            return outputTargetBlock && outputTargetBlock.typeName === 'Ball' && outputTargetBlock.methodName === 'Bounce';
                        }
                    }
                    return false;
                });
            },
            url: "",
        },
        {
            text: "Test your app again. When you fling the ball, it should bounce off the edges of the canvas. Great job!<br><br>",
            validate: function (formName) {
                return true;
            },
            url: "",
        },
        {
            text: "There are many ways to extend this app. The possibilities are endless, but here are some ideas: <ul><li>Change the color of the ball based on how fast it is moving or which edge it hits.</li><li>Scale the speed of the ball so that it slows down and stops after it gets flung.</li><li>Give the ball obstacles or targets to hit</li><li>Introduce a paddle for intercepting the ball, like a Pong game</li></ul>Visit the <a target='_blank' href='http://appinventor.mit.edu/explore'>App Inventor website</a> to find tutorials that help you extend this app, particularly the <a target='_blank' href='http://appinventor.mit.edu/explore/ai2/minigolf.html'>Mini Golf tutorial</a>.<br><br>Have fun with these extensions or others that you create!<br><br>",
            validate: function (formName) {
                return true;
            },
            url: "",
        }
    ]
};