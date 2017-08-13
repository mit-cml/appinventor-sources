//
//  CSLinearLayoutView.m
//  CSLinearLayoutView
//
//  Created by Charles Scalesse on 3/24/12.
//  Copyright (c) 2013 Charles Scalesse. All rights reserved.
//

#import "CSLinearLayoutView.h"

@interface CSLinearLayoutView()

- (void)setup;
- (void)adjustFrameSize;
- (void)adjustContentSize;

@end

@implementation CSLinearLayoutView

@synthesize items = _items;
@synthesize orientation = _orientation;
@synthesize autoAdjustFrameSize = _autoAdjustFrameSize;
@synthesize autoAdjustContentSize = _autoAdjustContentSize;

#pragma mark - Factories

- (id)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)setup {
    _items = [[NSMutableArray alloc] init];
    _orientation = CSLinearLayoutViewOrientationVertical;
    _autoAdjustFrameSize = NO;
    _autoAdjustContentSize = YES;
    self.autoresizesSubviews = NO;
}


#pragma mark - Lifecycle

- (void)dealloc {
    [_items release], _items = nil;
    [super dealloc];
}


#pragma mark - Layout

- (void)layoutSubviews {
    
    CGFloat relativePosition = 0.0;
    CGFloat absolutePosition = 0.0;
    
    for (CSLinearLayoutItem *item in _items) {
        
        CGFloat startPadding = 0.0;
        CGFloat endPadding = 0.0;

      if ([item.view isMemberOfClass:[CSLinearLayoutView class]]) {
        [item.view layoutSubviews];
      }
        
        if (self.orientation == CSLinearLayoutViewOrientationHorizontal) {
            
            startPadding = item.padding.left;
            endPadding = item.padding.right;
            
            if (item.verticalAlignment == CSLinearLayoutItemVerticalAlignmentTop || item.fillMode == CSLinearLayoutItemFillModeStretch) {
                absolutePosition = item.padding.top;
            } else if (item.verticalAlignment == CSLinearLayoutItemVerticalAlignmentBottom) {
                absolutePosition = self.frame.size.height - item.view.frame.size.height - item.padding.bottom;
            } else { // CSLinearLayoutItemVerticalCenter
                absolutePosition = (self.frame.size.height / 2) - ((item.view.frame.size.height + (item.padding.bottom - item.padding.top)) / 2);
            }
            
        } else {
            
            startPadding = item.padding.top;
            endPadding = item.padding.bottom;
            
            if (item.horizontalAlignment == CSLinearLayoutItemHorizontalAlignmentLeft || item.fillMode == CSLinearLayoutItemFillModeStretch) {
                absolutePosition = item.padding.left;
            } else if (item.horizontalAlignment == CSLinearLayoutItemHorizontalAlignmentRight) {
                absolutePosition = self.frame.size.width - item.view.frame.size.width - item.padding.right;
            } else { // CSLinearLayoutItemHorizontalCenter
                absolutePosition = (self.frame.size.width / 2) - ((item.view.frame.size.width + (item.padding.right - item.padding.left)) / 2);
            }
            
        }
        
        relativePosition += startPadding;
        
        CGFloat currentOffset = 0.0;
        if (self.orientation == CSLinearLayoutViewOrientationHorizontal) {
            
            CGFloat height = item.view.frame.size.height;
            if (item.fillMode == CSLinearLayoutItemFillModeStretch) {
                height = self.frame.size.height - (item.padding.top + item.padding.bottom);
            }
            
            item.view.frame = CGRectMake(relativePosition, absolutePosition, item.view.frame.size.width, height);
            currentOffset = item.view.frame.size.width;
            
        } else {
            
            CGFloat width = item.view.frame.size.width;
            if (item.fillMode == CSLinearLayoutItemFillModeStretch) {
                width = self.frame.size.width - (item.padding.left + item.padding.right);
            }
            
            item.view.frame = CGRectMake(absolutePosition, relativePosition, width, item.view.frame.size.height);
            currentOffset = item.view.frame.size.height;
            
        }
        
        relativePosition += currentOffset + endPadding;
        
    }
    
    if (_autoAdjustFrameSize == YES) {
        [self adjustFrameSize];
    }
    
    if (_autoAdjustContentSize == YES) {
        [self adjustContentSize];
    }
}

- (void)adjustFrameSize {
    if (self.orientation == CSLinearLayoutViewOrientationHorizontal) {
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.layoutOffset, self.frame.size.height);
    } else {
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, self.layoutOffset);
    }
}

- (void)adjustContentSize {
    if (self.orientation == CSLinearLayoutViewOrientationHorizontal) {
        CGFloat contentWidth = MAX(self.frame.size.width, self.layoutOffset);
        self.contentSize = CGSizeMake(contentWidth, self.frame.size.height);
    } else {
        CGFloat contentHeight = MAX(self.frame.size.height, self.layoutOffset);
        self.contentSize = CGSizeMake(self.frame.size.width, contentHeight);
    }
}

- (CGFloat)layoutWidth {
  if (self.orientation == CSLinearLayoutViewOrientationVertical) {
    CGFloat width = 0.0;
    for (CSLinearLayoutItem *item in _items) {
      width = MAX(width, item.view.frame.size.width);
    }
    return width;
  } else {
    return self.frame.size.width;
  }
}

- (CGFloat)layoutHeight {
  if (self.orientation == CSLinearLayoutViewOrientationHorizontal) {
    return self.frame.size.height;
  } else {
    CGFloat height = 0.0;
    for (CSLinearLayoutItem *item in _items) {
      height = MAX(height, item.view.frame.size.height);
    }
    return height;
  }
}

- (CGFloat)layoutOffset {
    CGFloat currentOffset = 0.0;
    
    for (CSLinearLayoutItem *item in _items) {
        if (_orientation == CSLinearLayoutViewOrientationHorizontal) {
            currentOffset += item.padding.left + item.view.frame.size.width + item.padding.right;
        } else {
            currentOffset += item.padding.top + item.view.frame.size.height + item.padding.bottom;
        }
    }
    
    return currentOffset;
}

- (void)setOrientation:(CSLinearLayoutViewOrientation)anOrientation {
    _orientation = anOrientation;
    [self setNeedsLayout];
}

- (void)addSubview:(UIView *)view {
    [super addSubview:view];
    
    if (_autoAdjustFrameSize == YES) {
        [self adjustFrameSize];
    }
    
    if (_autoAdjustContentSize == YES) {
        [self adjustContentSize];
    }
}


#pragma mark - Add, Remove, Insert, & Move

- (void)addItem:(CSLinearLayoutItem *)linearLayoutItem {
    if (linearLayoutItem == nil || [_items containsObject:linearLayoutItem] == YES || linearLayoutItem.view == nil) {
        return;
    }
    
    [_items addObject:linearLayoutItem];
    [self addSubview:linearLayoutItem.view];
}

- (void)removeItem:(CSLinearLayoutItem *)linearLayoutItem {
    if (linearLayoutItem == nil || [_items containsObject:linearLayoutItem] == NO) {
        return;
    }
    
    [linearLayoutItem retain];
    
    [_items removeObject:linearLayoutItem];
    [linearLayoutItem.view removeFromSuperview];
    
    [linearLayoutItem release];
}

- (void)removeAllItems {
    [_items removeAllObjects];
    for (UIView *subview in self.subviews) {
        [subview removeFromSuperview];
    }
}

- (void)insertItem:(CSLinearLayoutItem *)newItem beforeItem:(CSLinearLayoutItem *)existingItem {
    if (newItem == nil || [_items containsObject:newItem] == YES || existingItem == nil ||  [_items containsObject:existingItem] == NO) {
        return;
    }
    
    NSUInteger index = [_items indexOfObject:existingItem];
    [_items insertObject:newItem atIndex:index];
    [self addSubview:newItem.view];
}

- (void)insertItem:(CSLinearLayoutItem *)newItem afterItem:(CSLinearLayoutItem *)existingItem {
    if (newItem == nil || [_items containsObject:newItem] == YES || existingItem == nil || [_items containsObject:existingItem] == NO) {
        return;
    }
    
    if (existingItem == [_items lastObject]) {
        [_items addObject:newItem];
    } else {
        NSUInteger index = [_items indexOfObject:existingItem];
        [_items insertObject:newItem atIndex:++index];
    }
    
    [self addSubview:newItem.view];
}

- (void)insertItem:(CSLinearLayoutItem *)newItem atIndex:(NSUInteger)index {
    if (newItem == nil || [_items containsObject:newItem] == YES || index >= [_items count]) {
        return;
    }
    
    [_items insertObject:newItem atIndex:index];
    [self addSubview:newItem.view];
}

- (void)moveItem:(CSLinearLayoutItem *)movingItem beforeItem:(CSLinearLayoutItem *)existingItem {
    if (movingItem == nil || [_items containsObject:movingItem] == NO || existingItem == nil || [_items containsObject:existingItem] == NO || movingItem == existingItem) {
        return;
    }
    
    [movingItem retain];
    [_items removeObject:movingItem];
    
    NSUInteger existingItemIndex = [_items indexOfObject:existingItem];
    [_items insertObject:movingItem atIndex:existingItemIndex];
    [movingItem release];
    
    [self setNeedsLayout];
}

- (void)moveItem:(CSLinearLayoutItem *)movingItem afterItem:(CSLinearLayoutItem *)existingItem {
    if (movingItem == nil || [_items containsObject:movingItem] == NO || existingItem == nil || [_items containsObject:existingItem] == NO || movingItem == existingItem) {
        return;
    }
    
    [movingItem retain];
    [_items removeObject:movingItem];
    
    if (existingItem == [_items lastObject]) {
        [_items addObject:movingItem];
    } else {
        NSUInteger existingItemIndex = [_items indexOfObject:existingItem];
        [_items insertObject:movingItem atIndex:++existingItemIndex];
    }
    [movingItem release];
    
    [self setNeedsLayout];
}

- (void)moveItem:(CSLinearLayoutItem *)movingItem toIndex:(NSUInteger)index {
    if (movingItem == nil || [_items containsObject:movingItem] == NO || index >= [_items count] || [_items indexOfObject:movingItem] == index) {
        return;
    }
    
    [movingItem retain];
    [_items removeObject:movingItem];
    
    if (index == ([_items count] - 1)) {
        [_items addObject:movingItem];
    } else {
        [_items insertObject:movingItem atIndex:index];
    }
    [movingItem release];
    
    [self setNeedsLayout];
}

- (void)swapItem:(CSLinearLayoutItem *)firstItem withItem:(CSLinearLayoutItem *)secondItem {
    if (firstItem == nil || [_items containsObject:firstItem] == NO || secondItem == nil || [_items containsObject:secondItem] == NO || firstItem == secondItem) {
        return;
    }
    
    NSUInteger firstItemIndex = [_items indexOfObject:firstItem];
    NSUInteger secondItemIndex = [_items indexOfObject:secondItem];
    [_items exchangeObjectAtIndex:firstItemIndex withObjectAtIndex:secondItemIndex];
    
    [self setNeedsLayout];
}

@end

#pragma mark -

@implementation CSLinearLayoutItem

@synthesize view = _view;
@synthesize fillMode = _fillMode;
@synthesize horizontalAlignment = _horizontalAlignment;
@synthesize verticalAlignment = _verticalAlignment;
@synthesize padding = _padding;
@synthesize tag = _tag;
@synthesize userInfo = _userInfo;

#pragma mark - Factories

- (id)init {
    self = [super init];
    if (self) {
        self.horizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft;
        self.verticalAlignment = CSLinearLayoutItemVerticalAlignmentTop;
        self.fillMode = CSLinearLayoutItemFillModeNormal;
    }
    return self;
}

- (id)initWithView:(UIView *)aView {
    self = [super init];
    if (self) {
        self.view = aView;
        self.horizontalAlignment = CSLinearLayoutItemHorizontalAlignmentLeft;
        self.verticalAlignment = CSLinearLayoutItemVerticalAlignmentTop;
        self.fillMode = CSLinearLayoutItemFillModeNormal;
    }
    return self;
}

+ (CSLinearLayoutItem *)layoutItemForView:(UIView *)aView {
    CSLinearLayoutItem *item = [[[CSLinearLayoutItem alloc] initWithView:aView] autorelease];
    return item;
}

#pragma mark - Memory Management

- (void)dealloc {
    self.view = nil;
    self.userInfo = nil;
    
    [super dealloc];
}


#pragma mark - Helpers

CSLinearLayoutItemPadding CSLinearLayoutMakePadding(CGFloat top, CGFloat left, CGFloat bottom, CGFloat right) {
    CSLinearLayoutItemPadding padding;
    padding.top = top;
    padding.left = left;
    padding.bottom = bottom;
    padding.right = right;
    
    return padding;
}

@end
