/***************************************************************************
  
CSLinearLayoutView.h
CSLinearLayoutView
Version 1.0

Copyright (c) 2013 Charles Scalesse.
 
Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
***************************************************************************/


#import <UIKit/UIKit.h>

@class CSLinearLayoutItem;

typedef enum {
    CSLinearLayoutViewOrientationVertical,
    CSLinearLayoutViewOrientationHorizontal
} CSLinearLayoutViewOrientation;

@interface CSLinearLayoutView : UIScrollView

@property (nonatomic, readonly) NSMutableArray *items;
@property (nonatomic, assign) CSLinearLayoutViewOrientation orientation;
@property (nonatomic, readonly) CGFloat layoutOffset;       // Iterates through the existing layout items and returns the current offset.
@property (nonatomic, readonly) CGFloat layoutWidth;
@property (nonatomic, readonly) CGFloat layoutHeight;
@property (nonatomic, assign) BOOL autoAdjustFrameSize;     // Updates the frame size as items are added/removed. Default is NO.
@property (nonatomic, assign) BOOL autoAdjustContentSize;   // Updates the contentView as items are added/removed. Default is YES.

- (void)addItem:(CSLinearLayoutItem *)linearLayoutItem;
- (void)removeItem:(CSLinearLayoutItem *)linearLayoutItem;
- (void)removeAllItems;

- (void)insertItem:(CSLinearLayoutItem *)newItem beforeItem:(CSLinearLayoutItem *)existingItem;
- (void)insertItem:(CSLinearLayoutItem *)newItem afterItem:(CSLinearLayoutItem *)existingItem;
- (void)insertItem:(CSLinearLayoutItem *)newItem atIndex:(NSUInteger)index;

- (void)moveItem:(CSLinearLayoutItem *)movingItem beforeItem:(CSLinearLayoutItem *)existingItem;
- (void)moveItem:(CSLinearLayoutItem *)movingItem afterItem:(CSLinearLayoutItem *)existingItem;
- (void)moveItem:(CSLinearLayoutItem *)movingItem toIndex:(NSUInteger)index;

- (void)swapItem:(CSLinearLayoutItem *)firstItem withItem:(CSLinearLayoutItem *)secondItem;

@end


typedef enum {
    CSLinearLayoutItemFillModeNormal,   // Respects the view's frame size
    CSLinearLayoutItemFillModeStretch   // Adjusts the frame to fill the linear layout view
} CSLinearLayoutItemFillMode;

typedef enum {
    CSLinearLayoutItemHorizontalAlignmentLeft,
    CSLinearLayoutItemHorizontalAlignmentRight,
    CSLinearLayoutItemHorizontalAlignmentCenter
} CSLinearLayoutItemHorizontalAlignment;

typedef enum {
    CSLinearLayoutItemVerticalAlignmentTop,
    CSLinearLayoutItemVerticalAlignmentBottom,
    CSLinearLayoutItemVerticalAlignmentCenter
} CSLinearLayoutItemVerticalAlignment;      

typedef struct {
    CGFloat top;
    CGFloat left;
    CGFloat bottom;
    CGFloat right;
} CSLinearLayoutItemPadding;

@interface CSLinearLayoutItem : NSObject

@property (nonatomic, retain) UIView *view;
@property (nonatomic, assign) CSLinearLayoutItemFillMode fillMode;
@property (nonatomic, assign) CSLinearLayoutItemHorizontalAlignment horizontalAlignment;    // Use horizontalAlignment when the layout view is set to VERTICAL orientation
@property (nonatomic, assign) CSLinearLayoutItemVerticalAlignment verticalAlignment;        // Use verticalAlignment when the layout view is set to HORIZONTAL orientation
@property (nonatomic, assign) CSLinearLayoutItemPadding padding;
@property (nonatomic, assign) NSDictionary *userInfo;
@property (nonatomic, assign) NSInteger tag;

- (id)initWithView:(UIView *)aView;
+ (CSLinearLayoutItem *)layoutItemForView:(UIView *)aView;

CSLinearLayoutItemPadding CSLinearLayoutMakePadding(CGFloat top, CGFloat left, CGFloat bottom, CGFloat right);

@end
