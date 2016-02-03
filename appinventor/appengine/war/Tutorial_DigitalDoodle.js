var Tutorial_DigitalDoodle = {
    title: "Digital Doodle",
    difficulty: "easy",
    steps: [
        {
            text: 'This tutorial will show you how to draw a line on the screen as the user drags a finger around.',
            validate: function (formName) {
                return true;
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'The default setting for App Inventor is that the screen of your app will be "scrollable", which means that the user interface can go beyond the limit of the screen and the user can scroll down by swiping their finger (like scrolling on a web page).<br><br>When you are using a Canvas, you have to turn off the "Scrollable" setting (UNCHECK THE BOX) so that the screen does not scroll. This will allow you to make the Canvas to fill up the whole screen.',
            validate: function (formName) {
                // when Component property testing is made feasible
                //return Tutorial.testForComponentProperty("Canvas", { Scrollable: false});
                return true;
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'From the Drawing and Animation drawer of the Palette, drag out a Canvas component and drop it in the Viewer.',
            validate: function (formName) {
                return Tutorial.testForComponent("Canvas");
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'Make sure the Canvas component is selected in the Components list so that its properties show up in the Properties Pane. Down at the bottom of the Properties Pane, set the Height property to "Fill Parent". Do the same with the Width property.',
            validate: function (formName) {
                // when Component property testing is made feasible
                //return Tutorial.testForComponentProperty("Canvas", { Height: "Fill Parent"});
                //return Tutorial.testForComponentProperty("Canvas", { Width: "Fill Parent"});
                return true;
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'Believe it or not, for now this app only needs a Canvas. Go into the Blocks Editor to program the app.',
            validate: function (formName) {
                return BlocklyPanel_InBlocksView();
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: "From the list of block categories on the left, click on Canvas1 to open the Canvas1 blocks drawer. From the Canvas1 drawer, pull out the 'when Canvas1.Dragged' event.",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function(block) {
                    return block.eventName === "Dragged" && block.typeName === "Canvas";
                });
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: "Open the Canvas1 drawer again, and this time, pull out the 'when Canvas1.DrawLine' method block and add it to the Canvas1.Dragged event handler.",
            validate: function (formName) {
                return Tutorial.testForBlock(formName, function(block) {
                    if (block.typeName === 'Canvas' && block.methodName === 'DrawLine') {
                        var surroundingParent = block.getSurroundParent();
                        return surroundingParent != null && surroundingParent.eventName === "Dragged" && surroundingParent.typeName === "Canvas";
                    } else {
                        return false;
                    }
                });
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: "The Canvas Dragged event will happen over and over again very rapidly while the user drags a finger on the screen. Each time that Dragged event block is called, we want to draw a small line between the previous location (prevX, prevY) of the finger to the new location (currentX, currentY).<br><br>Mouse over the parameters of the Canvas1.Dragged block to pull out the get blocks that you need. (<i>Mouse over the parameter names, but don't click until your mouse is on the get block.</i>) Add the get blocks as the values of the draw line parameters.",
            validate: function (formName) {
                var dragVarToDrawParamMap = { 'prevX': 'x1', 'currentX': 'x2', 'prevY': 'y1', 'currentY': 'y2' };
                return Tutorial.testForBlock(formName, function(block) {
                    if (block.typeName === 'Canvas' && block.methodName === 'DrawLine') {
                        var correctParams = 0;
                        block.inputList.forEach(function (input) {
                            if (input.connection && input.connection.targetConnection && input.connection.targetConnection.sourceBlock_) {
                                var connectedBlock = input.connection.targetConnection.sourceBlock_;
                                if (connectedBlock.category === 'Variables' && dragVarToDrawParamMap[connectedBlock.getVars()[0]] === input.titleRow[0].text_) {
                                    correctParams++;
                                }
                            }
                        });
                        return correctParams === 4;
                    } else {
                        return false;
                    }
                });
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'Go to your connected device and drag your finger around the screen. Do you see a line?',
            validate: function (formName) {
                return true;
            },
            url: '',
            top: 1,
            left: 521
        },
        {
            text: 'Great work! Now try extending this app.<br><br>Here are some ideas for extending this app. You can probably think of many more! <br><br>  - Change the color of the ink (and let the user pick from a selection of colors). See <a href="http://appinventor.mit.edu/explore/ai2/paintpot-part1.html">Paint Pot tutorial.</a><br>  - Change the background to a photograph or picture.<br>  - Let the user draw dots as well as lines (hint: Use DrawCircle block).<br>  - Add a button that turns on the camera and lets the user take a picture and then doodle on it.',
            validate: function (formName) {
                return true;
            },
            url: '',
            top: 1,
            left: 521
        }
    ]
};