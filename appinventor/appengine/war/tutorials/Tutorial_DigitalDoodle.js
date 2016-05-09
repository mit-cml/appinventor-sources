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
        },
        {
            text: 'To allow the Canvas to fill the whole screen, verify that the Screen1 property "Scrollable" is unchecked (the box is near the bottom of the Properties pane).',
            validate: function (formName) {
                return true;
            },
            url: "",
            top: 1,
            left: 233
        },
        {
            text: "From the Drawing and Animation drawer in the palette on the left, drag out a Canvas component and drop it onto the viewer.",
            validate: function (formName) {
                return Tutorial.testForComponent("Canvas");
            },
            url: "",
        },
        {
            text: 'Make sure the Canvas component is selected in the Components list so that its properties show up in the Properties Pane.<br><br>Set the Height and Width properties to "Fill Parent".',
            validate: function (formName) {
                return true;
            },
            url: "",
        },
        {
            text: "Let's add some behavior to our app! Open the Blocks editor.",
            validate: function (formName) {
                return BlocklyPanel_InBlocksView();
            },
            url: '',
        },
        {
            text: "In the Blocks list on the left, click Canvas1 to open the Canvas1 blocks drawer. Pull out the <b>when Canvas1.Dragged</b> event.",
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
            text: "Open the Canvas1 drawer again, and pull out the purple <b>call Canvas1.DrawLine</b> block and add it to the <b>Canvas1.Dragged</b> event handler.",
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
        },
        {
            text: "The <b>Canvas1.Dragged</b> event will be called repeatedly very rapidly while the user drags a finger on the canvas. Each time it is called, we want to draw a small line between the previous location (<i>prevX</i>, <i>prevY</i>) of the finger to the new location (<i>currentX</i>, <i>currentY</i>).<br><br>Mouse over the parameters of the Canvas1.Dragged block to pull out and add the needed get blocks as the values of the <b>Canvas1.DrawLine</b> parameters.",
            validate: function (formName) {
                var dragVarToDrawParamMap = { 'prevX': 'x1', 'currentX': 'x2', 'prevY': 'y1', 'currentY': 'y2' };
                return Tutorial.testForBlock(formName, function(block) {
                    if (block.typeName === 'Canvas' && block.methodName === 'DrawLine') {
                        var correctParams = 0;
                        block.inputList.forEach(function (input) {
                            if (input.connection && input.connection.targetConnection && input.connection.targetConnection.sourceBlock_) {
                            console.log("input", input);
                              console.log("input title row", input.fieldRow);
                                connectedBlock = input.connection.targetConnection.sourceBlock_;
                                if (connectedBlock.category === 'Variables' && dragVarToDrawParamMap[connectedBlock.getVars()[0]] === input.fieldRow[0].text_) {
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
        },
        {
            text: "It's a good habit to test your apps while you build. App Inventor lets you live-test using the Companion app on your phone (or emulator). Connect and play with your app!<br><br>If you have never connected your phone (or emulator), <a target='_blank' href='http://appinventor.mit.edu/explore/ai2/setup.html'>follow these instructions</a> and then come back to this tutorial.",
            validate: function (formName) {
                return true;
            },
            url: "",
        },
        {
            text: 'Drag your finger around the screen. Do you see a line?',
            validate: function (formName) {
                return true;
            },
            url: '',
        },
        {
            text: 'Great work! Now try extending this app.<br><br>Here are some ideas for extending this app. You can probably think of many more! <ul><li>Change the color of the ink (and let the user pick from a selection of colors). See our <a target="_blank" href="http://appinventor.mit.edu/explore/ai2/paintpot-part1.html">Paint Pot tutorial.</a></li><li>Change the background to a photograph or picture.</li><li>Let the user draw dots as well as lines (hint: Use the <b>DrawCircle</b> block).</li><li>Add a button that turns on the camera and lets the user take a picture and then doodle on it.</li></ul>',
            validate: function (formName) {
                return true;
            },
            url: '',
        }
    ]
};