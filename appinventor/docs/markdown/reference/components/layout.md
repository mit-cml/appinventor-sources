---
layout: documentation
title: Layout
---

[&laquo; Back to index](index.html)
# Layout

Table of Contents:

* [AbsoluteArrangement](#AbsoluteArrangement)
* [HorizontalArrangement](#HorizontalArrangement)
* [HorizontalScrollArrangement](#HorizontalScrollArrangement)
* [Tab](#Tab)
* [TabArrangement](#TabArrangement)
* [TableArrangement](#TableArrangement)
* [VerticalArrangement](#VerticalArrangement)
* [VerticalScrollArrangement](#VerticalScrollArrangement)

## AbsoluteArrangement  {#AbsoluteArrangement}

Container for components that arranges them absolutely.



### Properties  {#AbsoluteArrangement-Properties}

{:.properties}

{:id="AbsoluteArrangement.BackgroundColor" .color} *BackgroundColor*
: Specifies the button's background color as an alpha-red-green-blue integer.
 If the parameter is [`Component`'s `COLOR`](#Component.COLOR), the original beveling is
 restored. If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="AbsoluteArrangement.Height" .number .bo} *Height*
: Specifies the `AbsoluteArrangement`'s vertical height, measured in pixels.

{:id="AbsoluteArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `AbsoluteArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="AbsoluteArrangement.Image" .text} *Image*
: Specifies the path of the button's image.

{:id="AbsoluteArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="AbsoluteArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="AbsoluteArrangement.Visible" .boolean} *Visible*
: Specifies whether the `AbsoluteArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `AbsoluteArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="AbsoluteArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `AbsoluteArrangement`, measured in pixels.

{:id="AbsoluteArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `AbsoluteArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#AbsoluteArrangement-Events}

{:.events}
None


### Methods  {#AbsoluteArrangement-Methods}

{:.methods}
None


## HorizontalArrangement  {#HorizontalArrangement}

![HorizontalArrangement icon](images/horizontalarrangement.png)

 Use a horizontal arrangement component to display a group of components laid out from left to
 right.

 This component is a formatting element in which you place components that should be displayed
 from left to right. If you want to have components displayed one over another, use
 [`VerticalArrangement`](#VerticalArrangement) instead.

 In a `HorizontalArrangement`, components are arranged along the horizontal axis, vertically
 center-aligned.

 If a `HorizontalArrangement`'s [`Height`](#HorizontalArrangement.Height) property is set to `Automatic`, the actual
 height of the arrangement is determined by the tallest component in the arrangement whose
 [`Height`](#HorizontalArrangement.Height) property is not set to `Fill Parent`. If a `HorizontalArrangment`'s
 [`Height`](#HorizontalArrangement.Height) property is set to `Automatic` and it contains only components whose `Height`
 properties are set to `Fill Parent`, the actual height of the arrangement is calculated using
 the automatic heights of the components. If a `HorizontalArrangement`'s [`Height`](#HorizontalArrangement.Height)
 property is set to `Automatic` and it is empty, the [`Height`](#HorizontalArrangement.Height) will be 100.

 If a `HorizontalArrangement`'s [`Width`](#HorizontalArrangement.Width) property is set to `Automatic`, the actual width
 of the arrangement is determined by the sum of the widths of the components. **If a
 `HorizontalArrangement`'s [`Width`](#HorizontalArrangement.Width) property is set to `Automatic`, any components whose
 [`Width`](#HorizontalArrangement.Width) properties are set to `Fill Parent` will behave as if they were set to
 `Automatic`.**

 If a `HorizontalArrangement`'s [`Width`](#HorizontalArrangement.Width) properties are set to `Fill Parent` will equally
 take up the width not occupied by other components.



### Properties  {#HorizontalArrangement-Properties}

{:.properties}

{:id="HorizontalArrangement.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the `HorizontalArrangement` are aligned horizontally. The choices
 are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
 effect if the `HorizontalArrangement`'s [`Width`](#HorizontalArrangement.Width) is `Automatic`.

{:id="HorizontalArrangement.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the `HorizontalArrangement` are aligned vertically. The choices
 are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
 Alignment has no effect if the `HorizontalArrangement`'s [`Height`](#HorizontalArrangement.Height) is `Automatic`.

{:id="HorizontalArrangement.BackgroundColor" .color} *BackgroundColor*
: Specifies the background color of the HorizontalArrangement as an alpha-red-green-blue
 integer.  If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="HorizontalArrangement.Height" .number .bo} *Height*
: Specifies the `HorizontalArrangement`'s vertical height, measured in pixels.

{:id="HorizontalArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `HorizontalArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="HorizontalArrangement.Image" .text} *Image*
: Specifies the path of the background image of the `HorizontalArrangement`.

{:id="HorizontalArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="HorizontalArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="HorizontalArrangement.Visible" .boolean} *Visible*
: Specifies whether the `HorizontalArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `HorizontalArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="HorizontalArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `HorizontalArrangement`, measured in pixels.

{:id="HorizontalArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `HorizontalArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#HorizontalArrangement-Events}

{:.events}
None


### Methods  {#HorizontalArrangement-Methods}

{:.methods}
None


## HorizontalScrollArrangement  {#HorizontalScrollArrangement}

A formatting element in which to place components that should be displayed from left to right.
 If you wish to have components displayed one over another, use [`VerticalScrollArrangement`](#VerticalScrollArrangement)
 instead.

 This version is scrollable.



### Properties  {#HorizontalScrollArrangement-Properties}

{:.properties}

{:id="HorizontalScrollArrangement.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the `HorizontalScrollArrangement` are aligned horizontally. The choices
 are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
 effect if the `HorizontalScrollArrangement`'s [`Width`](#HorizontalScrollArrangement.Width) is `Automatic`.

{:id="HorizontalScrollArrangement.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the `HorizontalScrollArrangement` are aligned vertically. The choices
 are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
 Alignment has no effect if the `HorizontalScrollArrangement`'s [`Height`](#HorizontalScrollArrangement.Height) is `Automatic`.

{:id="HorizontalScrollArrangement.BackgroundColor" .color} *BackgroundColor*
: Specifies the background color of the HorizontalScrollArrangement as an alpha-red-green-blue
 integer.  If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="HorizontalScrollArrangement.Height" .number .bo} *Height*
: Specifies the `HorizontalScrollArrangement`'s vertical height, measured in pixels.

{:id="HorizontalScrollArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `HorizontalScrollArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="HorizontalScrollArrangement.Image" .text} *Image*
: Specifies the path of the background image of the `HorizontalScrollArrangement`.

{:id="HorizontalScrollArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="HorizontalScrollArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="HorizontalScrollArrangement.Visible" .boolean} *Visible*
: Specifies whether the `HorizontalScrollArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `HorizontalScrollArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="HorizontalScrollArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `HorizontalScrollArrangement`, measured in pixels.

{:id="HorizontalScrollArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `HorizontalScrollArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#HorizontalScrollArrangement-Events}

{:.events}
None


### Methods  {#HorizontalScrollArrangement-Methods}

{:.methods}
None


## Tab  {#Tab}

Use a tab component to add a new tab in a tab arrangement.

 This component extends `HVArrangement`, within which the components of this particular tab can be placed.
 The components are vertically aligned within a tab.

 This component can be placed only in a `TabArrangement` component.



### Properties  {#Tab-Properties}

{:.properties}

{:id="Tab.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the `Tab` are aligned horizontally. The choices
 are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
 effect if the `Tab`'s [`Width`](#Tab.Width) is `Automatic`.

{:id="Tab.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the `Tab` are aligned vertically. The choices
 are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
 Alignment has no effect if the `Tab`'s [`Height`](#Tab.Height) is `Automatic`.

{:id="Tab.BackgroundColor" .color} *BackgroundColor*
: Specifies the background color of the Tab as an alpha-red-green-blue
 integer.  If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="Tab.Height" .number .bo} *Height*
: Specifies the `Tab`'s vertical height, measured in pixels.

{:id="Tab.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `Tab`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="Tab.Icon" .text} *Icon*
: Returns the icon displayed in the tab.

{:id="Tab.Image" .text} *Image*
: Specifies the path of the background image of the `Tab`.

{:id="Tab.Scrollable" .boolean} *Scrollable*
: Scrollable property getter method.

{:id="Tab.ShowIcon" .boolean} *ShowIcon*
: Returns the visibility of the tab icon.

{:id="Tab.ShowText" .boolean} *ShowText*
: Returns the visibility of the tab label.

{:id="Tab.Text" .text} *Text*
: Returns the text displayed by the tab label.

{:id="Tab.Visible" .boolean} *Visible*
: Specifies whether the `Tab` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Tab` is showing and `false`{:.logic.block} if hidden.

{:id="Tab.Width" .number .bo} *Width*
: Specifies the horizontal width of the `Tab`, measured in pixels.

{:id="Tab.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `Tab` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#Tab-Events}

{:.events}

{:id="Tab.Click"} Click()
: Indicates that the user selected the tab.

### Methods  {#Tab-Methods}

{:.methods}

{:id="Tab.Show" class="method"} <i/> Show()
: Shows the tab contents if not currently active.

## TabArrangement  {#TabArrangement}

Use a tab arrangement component to arrange the group of components in tab format.

 This component is a formatting element in which to place components that should be displayed
 in the tab format.

 This component uses `com.google.android.material.tabs.TabLayout` and `androidx.viewpager2.widget.ViewPager2`
 to arrange the group of components in tab format. `TabLayout` provides a horizontal layout to display tabs.
 ViewPager2 enables the paging of the tab content and provides a built-in swipe gestures to transition through
 pages (or tabs) and displays screen slide animations during the transition.

 `TabArrangement` component accepts only the `Tab` component. Within each tab, components are
 vertically aligned.

 If a `TabArrangement`'s [`Height`](#TabArrangement.Height) property is set to `Automatic`, the actual height
 of the arrangement is set to `Fill Parent`.

 If a `TabArrangement`'s [`Width`](#TabArrangement.Width) property is set to `Automatic`, the actual width
 of the arrangement is set to `Fill Parent`.



### Properties  {#TabArrangement-Properties}

{:.properties}

{:id="TabArrangement.Height" .number .bo} *Height*
: Specifies the `TabArrangement`'s vertical height, measured in pixels.

{:id="TabArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `TabArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="TabArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="TabArrangement.SelectedTabIndicatorColor" .color} *SelectedTabIndicatorColor*
: Specifies the indicator color of the selected tab as an alpha-red-green-blue
 integer.

{:id="TabArrangement.SelectedTabTextColor" .color} *SelectedTabTextColor*
: Specifies the text color of the label of selected tab as an alpha-red-green-blue
 integer.

{:id="TabArrangement.TabBackgroundColor" .color} *TabBackgroundColor*
: Specifies the background color of the tab bar as an alpha-red-green-blue
 integer.

{:id="TabArrangement.TabBarPosition" .number} *TabBarPosition*
: Specifies a number that encodes the position of the tab bar.
 The choices are: 1 = default, 2 = top, 3 = bottom.

{:id="TabArrangement.TabBarVisible" .boolean} *TabBarVisible*
: Specifies whether the tab bar is visible.

{:id="TabArrangement.TabTextColor" .color} *TabTextColor*
: Specifies the text color of the tab label as an alpha-red-green-blue
 integer.

{:id="TabArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="TabArrangement.Visible" .boolean} *Visible*
: Specifies whether the `TabArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `TabArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="TabArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `TabArrangement`, measured in pixels.

{:id="TabArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `TabArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#TabArrangement-Events}

{:.events}

{:id="TabArrangement.ShowTab"} ShowTab(*tab*{:.component})
: Indicates when the selected tab has changed in the TabArrangement.

### Methods  {#TabArrangement-Methods}

{:.methods}
None


## TableArrangement  {#TableArrangement}

Use a table arrangement component to display a group of components in a tabular fashion.

 This component is a formatting element in which you place components that should be displayed
 in tabular form.

 In a `TableArrangement`, components are arranged in a grid of rows and columns, with not more
 than one component visible in each cell. **If multiple components occupy the same cell, only the
 last one will be visible.**

 Within each row, components are vertically center-aligned.

 The width of a column is determined by the widest component in that column. When calculating
 column width, the automatic width is used for components whose [`Width`](#TableArrangement.Width) property is set
 to `Fill Parent`. **However, each component will always fill the full width of the column that it
 occupies.**

 The height of a row is determined by the tallest component in that row whose [`Height`](#TableArrangement.Height)
 property is not set to `Fill Parent`. If a row contains only components whose [`Height`](#TableArrangement.Height)
 properties are set to `Fill Parent`, the height of the row is calculated using the automatic
 heights of the components.



### Properties  {#TableArrangement-Properties}

{:.properties}

{:id="TableArrangement.Columns" .number .do} *Columns*
: Determines the number of columns in the table.

{:id="TableArrangement.Height" .number .bo} *Height*
: Specifies the `TableArrangement`'s vertical height, measured in pixels.

{:id="TableArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `TableArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="TableArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="TableArrangement.Rows" .number .do} *Rows*
: Determines the number of rows in the table.

{:id="TableArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="TableArrangement.Visible" .boolean} *Visible*
: Specifies whether the `TableArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `TableArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="TableArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `TableArrangement`, measured in pixels.

{:id="TableArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `TableArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#TableArrangement-Events}

{:.events}
None


### Methods  {#TableArrangement-Methods}

{:.methods}
None


## VerticalArrangement  {#VerticalArrangement}

![VerticalArrangement icon](images/verticalarrangement.png)

 Use a `VerticalArrangement` component to display a group of components laid out from top to
 bottom, left-aligned.

 This component is a formatting element in which you place components that should be displayed
 one below another. The first child component is stored on top, the second beneath it, and so on.
 If you want to have components displayed next to one another, use [`HorizontalArrangement`](#HorizontalArrangement)
 instead.

 In a `VerticalArrangement`, components are arranged along the vertical axis, left-aligned.

 If a `VerticalArrangement`'s [`Width`](#VerticalArrangement.Width) property is set to `Automatic`, the actual width
 of the arrangement is determined by the widest component in the arrangement whose
 [`Width`](#VerticalArrangement.Width) property is not set to `Fill Parent`. If a `VerticalArrangement`'s
 [`Width`](#VerticalArrangement.Width) property is set to `Automatic` and it contains only components whose
 [`Width`](#VerticalArrangement.Width) properties are set to `Fill Parent`, the actual width of the arrangement is
 calculated using the automatic widths of the components. If a `VerticalArrangement`'s
 [`Width`](#VerticalArrangement.Width) property is set to `Automatic` and it is empty, the width will be 100.

 If a `VerticalArrangement`'s [`Height`](#VerticalArrangement.Height) property is set to `Automatic`, the actual height
 of the arrangement is determined by the sum of the heights of the components. **If a
 `VerticalArrangement`'s [`Height`](#VerticalArrangement.Height) property is set to `Automatic`, any components whose
 `Height` properties are set to `Fill Parent` will behave as if they were set to `Automatic`.**

 If a `VerticalArrangement`'s [`Height`](#VerticalArrangement.Height) property is set to `Fill Parent` or specified in
 pixels, any components whose Height properties are set to `Fill Parent` will equally take up the
 height not occupied by other components.



### Properties  {#VerticalArrangement-Properties}

{:.properties}

{:id="VerticalArrangement.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the `VerticalArrangement` are aligned horizontally. The choices
 are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
 effect if the `VerticalArrangement`'s [`Width`](#VerticalArrangement.Width) is `Automatic`.

{:id="VerticalArrangement.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the `VerticalArrangement` are aligned vertically. The choices
 are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
 Alignment has no effect if the `VerticalArrangement`'s [`Height`](#VerticalArrangement.Height) is `Automatic`.

{:id="VerticalArrangement.BackgroundColor" .color} *BackgroundColor*
: Specifies the background color of the VerticalArrangement as an alpha-red-green-blue
 integer.  If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="VerticalArrangement.Height" .number .bo} *Height*
: Specifies the `VerticalArrangement`'s vertical height, measured in pixels.

{:id="VerticalArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `VerticalArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="VerticalArrangement.Image" .text} *Image*
: Specifies the path of the background image of the `VerticalArrangement`.

{:id="VerticalArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="VerticalArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="VerticalArrangement.Visible" .boolean} *Visible*
: Specifies whether the `VerticalArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `VerticalArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="VerticalArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `VerticalArrangement`, measured in pixels.

{:id="VerticalArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `VerticalArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#VerticalArrangement-Events}

{:.events}
None


### Methods  {#VerticalArrangement-Methods}

{:.methods}
None


## VerticalScrollArrangement  {#VerticalScrollArrangement}

A formatting element in which to place components that should be displayed one below another.
 (The first child component is stored on top, the second beneath it, etc.) If you wish to have
 components displayed next to one another, use [`HorizontalScrollArrangement`](#HorizontalScrollArrangement) instead.

 This version is scrollable.



### Properties  {#VerticalScrollArrangement-Properties}

{:.properties}

{:id="VerticalScrollArrangement.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the `VerticalScrollArrangement` are aligned horizontally. The choices
 are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
 effect if the `VerticalScrollArrangement`'s [`Width`](#VerticalScrollArrangement.Width) is `Automatic`.

{:id="VerticalScrollArrangement.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the `VerticalScrollArrangement` are aligned vertically. The choices
 are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
 Alignment has no effect if the `VerticalScrollArrangement`'s [`Height`](#VerticalScrollArrangement.Height) is `Automatic`.

{:id="VerticalScrollArrangement.BackgroundColor" .color} *BackgroundColor*
: Specifies the background color of the VerticalScrollArrangement as an alpha-red-green-blue
 integer.  If an Image has been set, the color change will not be visible
 until the Image is removed.

{:id="VerticalScrollArrangement.Height" .number .bo} *Height*
: Specifies the `VerticalScrollArrangement`'s vertical height, measured in pixels.

{:id="VerticalScrollArrangement.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `VerticalScrollArrangement`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="VerticalScrollArrangement.Image" .text} *Image*
: Specifies the path of the background image of the `VerticalScrollArrangement`.

{:id="VerticalScrollArrangement.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="VerticalScrollArrangement.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="VerticalScrollArrangement.Visible" .boolean} *Visible*
: Specifies whether the `VerticalScrollArrangement` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `VerticalScrollArrangement` is showing and `false`{:.logic.block} if hidden.

{:id="VerticalScrollArrangement.Width" .number .bo} *Width*
: Specifies the horizontal width of the `VerticalScrollArrangement`, measured in pixels.

{:id="VerticalScrollArrangement.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `VerticalScrollArrangement` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#VerticalScrollArrangement-Events}

{:.events}
None


### Methods  {#VerticalScrollArrangement-Methods}

{:.methods}
None

