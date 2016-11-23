CSLinearLayoutView
=============

CSLinearLayoutView is designed to simplify relative layouts on iOS. 

We've all been there before— the content in your app is dynamic and you need to display different sized views in neat succession. You spend the next hours pushing pixels, estimating text sizes, and tracking deltas. Lame.

Enter CSLinearLayoutView. Create your layout view, create layout items with the views you want to display, and then add the items to your layout. Much simpler.

Example Usage
---------
	// create the linear layout view
	CSLinearLayoutView *linearLayoutView = [[[CSLinearLayoutView alloc] initWithFrame:self.view.bounds] autorelease];
    linearLayoutView.orientation = CSLinearLayoutViewOrientationVertical;
    [self.view addSubview:linearLayoutView];
    
    // create a layout item for the view you want to display
    CSLinearLayoutItem *item = [CSLinearLayoutItem layoutItemForView:someView];
    item.padding = CSLinearLayoutMakePadding(5.0, 10.0, 5.0, 10.0);
    item.horizontalAlignment = CSLinearLayoutItemHorizontalAlignmentCenter;
    item.fillMode = CSLinearLayoutItemFillModeNormal;
    
    // add the layout item to the linear layout view
    [linearLayoutView addItem:item];

Checkout the demo project for additional tests and examples.

MIT License
-----------
    Copyright (c) 2013 Charles Scalesse.

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:

    The above copyright notice and this permission notice shall be included
    in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
    OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
    CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
    TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
    SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.