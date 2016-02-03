var Tutorial_BallBounce = {
    title: "Ball Bounce",
    difficulty: "easy",
    steps: [
        {
            text: "In this tutorial, you will learn about animation in App Inventor by making a Ball (a sprite) bounce around on the screen (on a Canvas).",
            validate: function (formName) {
                return true;
            },
            top: window.innerHeight / 3,
            left: window.innerWidth / 3
        },
        {
            text: "Take a look at the Palette on the left of the screen. From the Drawing and Animation drawer, drag out a Canvas component and drop it onto the viewer.",
            validate: function (formName) {
                return Tutorial.testForComponent("Canvas");
            },
            url: "",
            top: 50,
            left: window.innerWidth * 2 / 3
        },
        {
            text: 'The default setting for App Inventor is that the screen of your app will be "scrollable", which means that the user interface can go beyond the limit of the screen and the user can scroll down by swiping their finger (like scrolling on a web page).<br><br>When you are using a Canvas, you have to turn off the "Scrollable" setting (UNCHECK THE BOX) so that the screen does not scroll. This will allow you to make the Canvas to fill up the whole screen.',
            validate: function (formName) {
                //return Tutorial.testForScreenProperty({ Scrollable: false});
                return true;
            },
            url: "",
            top: 50,
            left: window.innerWidth * 2 / 3 - 17
        },
        {
            text: 'To the right of the Viewer is our list of current components. Make sure the Canvas component is selected in the Components list so that its properties show up in the Properties Pane.<br><br>Down at the bottom, set the Height property to "Fill Parent". Do the same with the Width property.',
            validate: function (formName) {
                //return Tutorial.testForComponentProperty("Canvas", { Height: "Fill Parent"});
                //return Tutorial.testForComponentProperty("Canvas", { Width: "Fill Parent"});
                return true;
            },
            url: "",
            top: 50,
            left: window.innerWidth * 2 / 3 - 17
        },
        {
            text: "Now that we have a Canvas in place, we can add a Ball Sprite. This can also be found in the Drawing and Animation drawer. Drag out a Ball component and drop it onto the Canvas.<br><br>If you'd like the ball to show up better, you can change its Radius property in the Properties pane.",
            validate: function (formName) {
                return Tutorial.testForComponent("Ball");
            },
            url: "",
            top: 50,
            left: window.innerWidth * 2 / 3 - 17
        },
        {
            text: "Now let's add some behavior to our app. Open the Blocks pane.",
            validate: function (formName) {
                return BlocklyPanel_InBlocksView();
            },
            url: "",
            top: window.innerHeight / 3,
            left: window.innerWidth * 2 / 3
        },
        {
            text: "In the Blocks list on the left, click on Ball1 to open the Ball1 Drawer and view the Ball's blocks. Choose the block 'when Ball1.Flung' and drag-and-drop it onto the workspace.<br><br>Flung refers to the user making a " + '"Fling gesture" with his/her finger to "fling" the ball. Fling is a gesture like what a golf club does, not like how you launch Angry Birds! In App Inventor, the event handler for that type of gesture is called ' + "'when Flung.'",
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
            text: 'Open the Ball drawer again and scroll down in the list of blocks to get the ' + "'set Ball1.Heading' and 'set Ball1.Speed' blocks. Plug the 'set Ball1.Speed' and 'set Ball1.Heading' blocks into the Fling event handler.",
            validate: function (formName) {
                return Tutorial.testForBlocks(formName, function (block) {
                    if (block.typeName === 'Ball' && block.setOrGet === 'set' && (this.propertyName === "Heading" || "Speed")) {
                        var surroundingParent = block.getSurroundParent();
                        return surroundingParent != null && surroundingParent.eventName === "Flung" && surroundingParent.typeName === "Ball";
                    } else {
                        return false;
                    }
                }, 2);
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: "Mouse over the 'speed' parameter of the 'when Ball1.Flung' event handler. The get and set blocks for the speed of the fling will pop up. Grab the 'get speed' block and plug that into the 'set Ball1.Speed' block.",
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
            top: 1,
            left: 521
        },
        {
            text: "Do the same for the Ball's heading. Mouse over the 'heading' parameter and you'll see the 'get heading' block appear. Grab that block, and click it into the 'set Ball1.Heading' block.",
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
            top: 1,
            left: 521
        },
        {
            text: "A good habit while building apps is to test while you build. App Inventor lets you do this easily because you can have a live connection between your phone (or emulator) and the App Inventor development environment.<br><br>If you don't have a phone (or emulator) connected, go to the connection instructions and then come back to this tutorial. (Connection instructions are in Tutorial #1 or on the website under " + '"Getting Started".)',
            validate: function (formName) {
                return true;
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: 'Why does the Ball get stuck on the side of the screen?! After flinging your ball across the screen, you probably noticed that it got stuck on the side.<br>This is because the ' + "ball's heading has not changed even though it hit the side of the canvas. To make the ball " + '"bounce" of the edge of the screen, we can program in a new event handler called '+"'when Edge Reached'.",
            validate: function (formName) {
                return true;
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: "Go into the Ball1 drawer and pull out a 'when Ball1.EdgeReached' do event.",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function (block) {
                    return block.eventName === "EdgeReached" && block.typeName === "Ball";
                });
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: "Go back into the Ball1 drawer and pull out a Ball.Bounce block. Add it to the EdgeReached event handler.",
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
            top: 1,
            left: 521
        },
        {
            text: "The Ball.Bounce method needs an edge argument. Notice that the Ball1.EdgeReached event has an edge as a parameter. We can take the 'get edge' block from that argument and plug it into the 'call Ball1.Bounce' method.<br><br>Grab the 'get edge' block by mousing over (hover your mouse pointer over) the 'edge' parameter on the 'when Ball1.EdgeReached' block. Drop the 'get edge' block in as the 'edge' parameter to the Ball.Bounce method.",
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
            top: 1,
            left: 521
        },
        {
            text: "Now, when you fling the ball, it should bounce off the edges of the canvas. Great job!",
            validate: function (formName) {
                return true;
            },
            url: "",
            top: 1,
            left: 521
        },
        {
            text: "There are many ways to extend this app.<br>Here are some ideas... but the possibilities are endless!<br>  - Change the color of the ball based on how fast it is moving or which edge it reaches.<br>  - Scale the speed of the ball so that it slows down and stops after it gets flung.<br>  - Give the ball obstacles or targets to hit<br>  - Introduce a paddle for intercepting the ball, like a Pong game<br><br>Visit the <a href='http://appinventor.mit.edu/explore'>App Inventor website</a> to find tutorials that help you extend this app, particularly the <a href='http://appinventor.mit.edu/explore/ai2/minigolf.html'>Mini Golf tutorial</a>.<br><br>Have fun with these extensions, or others that you think up!",
            validate: function (formName) {
                return true;
            },
            url: "",
            top: 1,
            left: 521
        }
    ]
};