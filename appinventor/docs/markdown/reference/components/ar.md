---
layout: documentation
title: Augmented Reality
---

[&laquo; Back to index](index.html)
# Augmented Reality

Table of Contents:

* [ARView3D](#ARView3D)
* [AmbientLight](#AmbientLight)
* [BoxNode](#BoxNode)
* [CapsuleNode](#CapsuleNode)
* [ConeNode](#ConeNode)
* [CylinderNode](#CylinderNode)
* [DirectionalLight](#DirectionalLight)
* [ImageMarker](#ImageMarker)
* [ModelNode](#ModelNode)
* [PlaneNode](#PlaneNode)
* [PointLight](#PointLight)
* [PyramidNode](#PyramidNode)
* [SphereNode](#SphereNode)
* [Spotlight](#Spotlight)
* [TextNode](#TextNode)
* [TorusNode](#TorusNode)
* [TubeNode](#TubeNode)
* [VideoNode](#VideoNode)
* [WebViewNode](#WebViewNode)

## ARView3D  {#ARView3D}

Component for ARView3D



### Properties  {#ARView3D-Properties}

{:.properties}

{:id="ARView3D.DetectedPlanes" .list .ro .bo} *DetectedPlanes*
: The list of DetectedPlanes added to the ARView3D.

{:id="ARView3D.Height" .number .bo} *Height*
: Specifies the `ARView3D`'s vertical height, measured in pixels.

{:id="ARView3D.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `ARView3D`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="ARView3D.ImageMarkers" .list .ro .bo} *ImageMarkers*
: The list of ImageMarkers added to the ARView3D.

{:id="ARView3D.LightingEstimation" .boolean .ro} *LightingEstimation*
: Determines whether the real-world ambient light intensity and temperature should be estimated. If this property is set to true, lighting estimates are provided.  Otherwise, not lighting estimates are are provided.

{:id="ARView3D.Lights" .list .ro .bo} *Lights*
: The list of Lights added to the ARView3D.

{:id="ARView3D.Nodes" .list .ro .bo} *Nodes*
: The list of Nodes added to the ARView3D.

{:id="ARView3D.PlaneDetectionType" .number .ro} *PlaneDetectionType*
: <p>Determines whether plane detection is enabled.  If this property is set to None, then planes in the real world will not be detected.  Setting it to Horizontal detects horizontal planes in the real world.  Setting it to Vertical detects vertical planes in the real world, and setting it to both detects both horizontal and vertical planes.  When a plane is detected, a DetectedPlane component will placed at that location.  This works when the TrackingType is WorldTracking.</p><p>Valid values are: 0 (None), 1 (Horizontal), 2 (Vertical), 3 (Both).</p>

{:id="ARView3D.ShowBoundingBoxes" .boolean} *ShowBoundingBoxes*
: Determines whether to visualize the bounding box surrounding a node.

{:id="ARView3D.ShowFeaturePoints" .boolean} *ShowFeaturePoints*
: Determines whether to visualize features points as yellow points.  Feature points are points for which their world coordinates are known.

{:id="ARView3D.ShowLightAreas" .boolean} *ShowLightAreas*
: If this property is set to true, this visualizes the areas in space that are lit by Lights as well as the locations of the lights. Otherwise, areas light by the light are not visualized.

{:id="ARView3D.ShowLightLocations" .boolean} *ShowLightLocations*
: If this property is set to true, this visualizes the locations of lights.  Otherwise, locations of lights are not visualized.

{:id="ARView3D.ShowStatistics" .boolean} *ShowStatistics*
: Determines whether to show statistics such as frames per seconds under the ARView3D.

{:id="ARView3D.ShowWireframes" .boolean} *ShowWireframes*
: Determines whether to show the wireframe of nodes' geometries on top of their FillColor of Texture.

{:id="ARView3D.ShowWorldOrigin" .boolean} *ShowWorldOrigin*
: Determines whether to place a coordinate plane at the world origin (0,0,0) which displays the x, y, and z axes.

{:id="ARView3D.TrackingType" .number .ro} *TrackingType*
: <p>The tracking type for the AR session.  WorldTracking allows for plane detection, image detection with ImageMarkers, and the placement of nodes in the world.  The devices's position and orientation are used to track the placemet of the world.  Nodes will remain where you place them in the world, even if you move.  OrientationTracking allows for placing a nodes but using the devices's orientation to determine location.  If you move, the items will move too.  They do not stay in place. ImageTracking allows for using ImageMarkers and placing items relative to the images.Valid values are: 1 (WorldTracking), 2 (OrientationTracking), 3 (ImageTracking)

{:id="ARView3D.Visible" .boolean} *Visible*
: Specifies whether the `ARView3D` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `ARView3D` is showing and `false`{:.logic.block} if hidden.

{:id="ARView3D.Width" .number .bo} *Width*
: Specifies the horizontal width of the `ARView3D`, measured in pixels.

{:id="ARView3D.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `ARView3D` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#ARView3D-Events}

{:.events}

{:id="ARView3D.ClickOnDetectedPlaneAt"} ClickOnDetectedPlaneAt(*detectedPlane*{:.component},*x*{:.number},*y*{:.number},*z*{:.number},*isANodeAtPoint*{:.boolean})
: The user tapped on a DetectedPlane, detectedPlane.  (x,y,z) is the real-world coordinate of the point.  isANoteAtPoint is true if a node is already at that point and false otherwise.  This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.

{:id="ARView3D.DetectedPlaneRemoved"} DetectedPlaneRemoved(*detectedPlane*{:.component})
: A DetectedPlane was removed from the ARView3D.  This happens when two DetectedPlanes are combined to form one or the detected items were reset.  This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.

{:id="ARView3D.DetectedPlaneUpdated"} DetectedPlaneUpdated(*detectedPlane*{:.component})
: A DetectedPlane updated its properties, either its rotation, position, or size.  This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.

{:id="ARView3D.LightingEstimateUpdated"} LightingEstimateUpdated(*ambientIntensity*{:.number},*ambientTemperature*{:.number})
: The lighting estimate has been updated.  This provides an estimate for the real-world ambient lighting.  This event will only trigger if LightingEstimation is true.

{:id="ARView3D.LongClickOnDetectedPlaneAt"} LongClickOnDetectedPlaneAt(*detectedPlane*{:.component},*x*{:.number},*y*{:.number},*z*{:.number},*isANodeAtPoint*{:.boolean})
: The user long-pressed on a DetectedPlane, detectedPlane.  (x,y,z) is the real-world coordinate of the point.  isANoteAtPoint is true if a node is already at that point and false otherwise.  This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.

{:id="ARView3D.LongPressAtPoint"} LongPressAtPoint(*x*{:.number},*y*{:.number},*z*{:.number},*isANodeAtPoint*{:.boolean})
: The user long-pressed on a point on the ARView3D.  (x,y,z) is the real-world coordinate of the point.  isANoteAtPoint is true if a node is already at that point and false otherwise.

{:id="ARView3D.NodeClick"} NodeClick(*node*{:.component})
: The user tapped on a node in the ARView3D.

{:id="ARView3D.NodeLongClick"} NodeLongClick(*node*{:.component})
: The user long-pressed a node in the ARView3D.

{:id="ARView3D.PlaneDetected"} PlaneDetected(*detectedPlane*{:.component})
: A real-world plane was detected.  The detectedPlane is the component added at the location of the real-world plane.  This event will only trigger if PlaneDetection is not None, and the TrackingType is WorldTracking.  Note that the default FillColor of a DetectedPlane is None, so it is shown visually by default.

{:id="ARView3D.TapAtPoint"} TapAtPoint(*x*{:.number},*y*{:.number},*z*{:.number},*isANodeAtPoint*{:.boolean})
: The user tapped on a point on the ARView3D.  (x,y,z) is the real-world coordinate of the point.  isANoteAtPoint is true if a node is already at that point and false otherwise.

### Methods  {#ARView3D-Methods}

{:.methods}

{:id="ARView3D.CreateBoxNode" class="method returns component"} <i/> CreateBoxNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new BoxNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateCapsuleNode" class="method returns component"} <i/> CreateCapsuleNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new CapsuleNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateConeNode" class="method returns component"} <i/> CreateConeNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new ConeNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateCylinderNode" class="method returns component"} <i/> CreateCylinderNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new CylinderNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreatePlaneNode" class="method returns component"} <i/> CreatePlaneNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new PlaneNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreatePyramidNode" class="method returns component"} <i/> CreatePyramidNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new PyramidNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateSphereNode" class="method returns component"} <i/> CreateSphereNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new SphereNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateTextNode" class="method returns component"} <i/> CreateTextNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new TextNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateTorusNode" class="method returns component"} <i/> CreateTorusNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new TorusNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateTubeNode" class="method returns component"} <i/> CreateTubeNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new TubeNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateVideoNode" class="method returns component"} <i/> CreateVideoNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new VideoNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.CreateWebViewNode" class="method returns component"} <i/> CreateWebViewNode(*x*{:.number},*y*{:.number},*z*{:.number})
: Create a new WebViewNode with default properties at the specified (x,y,z) position.

{:id="ARView3D.HideAllLights" class="method"} <i/> HideAllLights()
: Sets Visible to false for all Lights.

{:id="ARView3D.HideAllNodes" class="method"} <i/> HideAllNodes()
: Sets Visible to false for all Nodes.

{:id="ARView3D.PauseTracking" class="method"} <i/> PauseTracking()
: Pauses the live camera feed and pauses tracking.

{:id="ARView3D.ResetDetectedItems" class="method"} <i/> ResetDetectedItems()
: Removed DetectedPlanes and resets detection for ImageMarkers.

{:id="ARView3D.ResetTracking" class="method"} <i/> ResetTracking()
: Resets the tracking, resetting all items including DetectedPlanes and ImageMarkers. If this is called while tracking is not paused, then this resets and restarts tracking.  If tracking is paused and this is called, this will reset the ARView3D once StartTracking is called again.

{:id="ARView3D.StartTracking" class="method"} <i/> StartTracking()
: Starts the live camera feed and begins tracking.

## AmbientLight  {#AmbientLight}

Component for AmbientLight



### Properties  {#AmbientLight-Properties}

{:.properties}

{:id="AmbientLight.Color" .color} *Color*
: The color of the light's rays.

{:id="AmbientLight.Intensity" .number} *Intensity*
: The brightness of the light.  The default value is 1000. Lower values darken the light, and its color, whereas higher values brighten it.

{:id="AmbientLight.Temperature" .number} *Temperature*
: <p>The temperature of the light, in degrees Kelvin. This, paired with the Color, determine the color of the light rays.  The default value, 6,500 represents white light.  Lower values add a warmer, or yellow, effect to the light, and greater values as a cooler, or blue, effect to the light.</p>Values less than zero will be treated as zero, and values greater than 40,000 will be treated as 40,000.

{:id="AmbientLight.Type" .text .ro .bo} *Type*
: Returns the type of light as a String.

{:id="AmbientLight.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

### Events  {#AmbientLight-Events}

{:.events}
None


### Methods  {#AmbientLight-Methods}

{:.methods}
None


## BoxNode  {#BoxNode}

Component for BoxNode



### Properties  {#BoxNode-Properties}

{:.properties}

{:id="BoxNode.CornerRadius" .number} *CornerRadius*
: This determines how rounded the boxes corners will be.  A value of zero specifies no rounded corners, and a value of half the length, height, or width of the BoxNode (whichever is greater) makes it fully rounded, with no straight edges.  Values less than zero will be set to zero.

{:id="BoxNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="BoxNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="BoxNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the BoxNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode will not appear.

{:id="BoxNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="BoxNode.LengthInCentimeters" .number} *LengthInCentimeters*
: How far, in centimeters, the BoxNode extends along the z-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode will not appear.

{:id="BoxNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="BoxNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="BoxNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="BoxNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="BoxNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="BoxNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="BoxNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="BoxNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="BoxNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="BoxNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="BoxNode.WidthInCentimeters" .number} *WidthInCentimeters*
: How far, in centimeters, the BoxNode extends along the x-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode will not appear.

{:id="BoxNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="BoxNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="BoxNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="BoxNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="BoxNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="BoxNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#BoxNode-Events}

{:.events}

{:id="BoxNode.Click"} Click()
: The user clicked on the node.

{:id="BoxNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="BoxNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#BoxNode-Methods}

{:.methods}

{:id="BoxNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="BoxNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="BoxNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="BoxNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="BoxNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="BoxNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="BoxNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="BoxNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="BoxNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="BoxNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="BoxNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="BoxNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="BoxNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="BoxNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="BoxNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="BoxNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="BoxNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="BoxNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## CapsuleNode  {#CapsuleNode}

Component for CapsuleNode



### Properties  {#CapsuleNode-Properties}

{:.properties}

{:id="CapsuleNode.CapRadiusInCentimeters" .number} *CapRadiusInCentimeters*
: The radius, in centimeters, of two hemispheres or caps at the ends of a CapsuleNode.  Values less than zero will be treated as their absolute values.  When set to zero or when set to greater than half of the Height, the CapsuleNode will not appear.

{:id="CapsuleNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="CapsuleNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CapsuleNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the CapsuleNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero or when set to less than double the CapRadius, the CapsuleNode will not appear.

{:id="CapsuleNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="CapsuleNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CapsuleNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="CapsuleNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="CapsuleNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="CapsuleNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="CapsuleNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="CapsuleNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="CapsuleNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CapsuleNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="CapsuleNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="CapsuleNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="CapsuleNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="CapsuleNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="CapsuleNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="CapsuleNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="CapsuleNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#CapsuleNode-Events}

{:.events}

{:id="CapsuleNode.Click"} Click()
: The user clicked on the node.

{:id="CapsuleNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="CapsuleNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#CapsuleNode-Methods}

{:.methods}

{:id="CapsuleNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="CapsuleNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="CapsuleNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="CapsuleNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="CapsuleNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="CapsuleNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="CapsuleNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="CapsuleNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="CapsuleNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="CapsuleNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="CapsuleNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="CapsuleNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="CapsuleNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="CapsuleNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="CapsuleNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="CapsuleNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="CapsuleNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="CapsuleNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## ConeNode  {#ConeNode}

Component for ConeNode



### Properties  {#ConeNode-Properties}

{:.properties}

{:id="ConeNode.BottomRadiusInCentimeters" .number} *BottomRadiusInCentimeters*
: This defines the radius of the bottom of the ConeNode.  A value of zero causes the cone to meet at a point at the bottom.  Values less than zero will be treated as their absolute value.  If the BottomRadius is set to zero and this is set to zero, the ConeNode will not be shown.

{:id="ConeNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="ConeNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ConeNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the ConeNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the ConeNode will not be shown.

{:id="ConeNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="ConeNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ConeNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="ConeNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="ConeNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="ConeNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="ConeNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="ConeNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="ConeNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ConeNode.TopRadiusInCentimeters" .number} *TopRadiusInCentimeters*
: This defines the radius of the top of the ConeNode.  A value of zero causes the cone to meet at a point at the top.  Values less than zero will be treated as their absolute value.  If the BottomRadius is set to zero and this is set to zero, the ConeNode will not be shown.

{:id="ConeNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="ConeNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="ConeNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="ConeNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="ConeNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="ConeNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="ConeNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="ConeNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#ConeNode-Events}

{:.events}

{:id="ConeNode.Click"} Click()
: The user clicked on the node.

{:id="ConeNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="ConeNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#ConeNode-Methods}

{:.methods}

{:id="ConeNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="ConeNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="ConeNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="ConeNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="ConeNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="ConeNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="ConeNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="ConeNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="ConeNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="ConeNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="ConeNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="ConeNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="ConeNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="ConeNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="ConeNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="ConeNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="ConeNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="ConeNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## CylinderNode  {#CylinderNode}

Component for CylinderNode



### Properties  {#CylinderNode-Properties}

{:.properties}

{:id="CylinderNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="CylinderNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CylinderNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the CylinderNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the CylinderNode will not be shown.

{:id="CylinderNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="CylinderNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CylinderNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="CylinderNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="CylinderNode.RadiusInCentimeters" .number} *RadiusInCentimeters*
: The radius of the CylinderNode determines the size of the cicular base and top.  Values less than zero will be treated as their absolute value.  When set to zero, the CylinderNode will not be shown.

{:id="CylinderNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="CylinderNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="CylinderNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="CylinderNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="CylinderNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="CylinderNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="CylinderNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="CylinderNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="CylinderNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="CylinderNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="CylinderNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="CylinderNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="CylinderNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#CylinderNode-Events}

{:.events}

{:id="CylinderNode.Click"} Click()
: The user clicked on the node.

{:id="CylinderNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="CylinderNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#CylinderNode-Methods}

{:.methods}

{:id="CylinderNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="CylinderNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="CylinderNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="CylinderNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="CylinderNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="CylinderNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="CylinderNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="CylinderNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="CylinderNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="CylinderNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="CylinderNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="CylinderNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="CylinderNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="CylinderNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="CylinderNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="CylinderNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="CylinderNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="CylinderNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## DirectionalLight  {#DirectionalLight}

Component for DirectionalLight



### Properties  {#DirectionalLight-Properties}

{:.properties}

{:id="DirectionalLight.CastsShadows" .boolean} *CastsShadows*
: If this property is set to true, then nodes illuminated by the DirectionalLight will cast shadows, if ShowShadow for the node is true.  Otherwise, the nodes it illuminates will not cast shadows.

{:id="DirectionalLight.Color" .color} *Color*
: The color of the light's rays.

{:id="DirectionalLight.Intensity" .number} *Intensity*
: The brightness of the light.  The default value is 1000. Lower values darken the light, and its color, whereas higher values brighten it.

{:id="DirectionalLight.ShadowColor" .color} *ShadowColor*
: The color of the shadows that nodes illuminated by the DirectionalLight will cast, if CastsShadows is true.

{:id="DirectionalLight.ShadowOpacity" .number} *ShadowOpacity*
: The opacity of the shadows that nodes illuminated by the DirectionalLight will cast, if CastsShadows is true.  This determines how intense the shadows are.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="DirectionalLight.Temperature" .number} *Temperature*
: <p>The temperature of the light, in degrees Kelvin. This, paired with the Color, determine the color of the light rays.  The default value, 6,500 represents white light.  Lower values add a warmer, or yellow, effect to the light, and greater values as a cooler, or blue, effect to the light.</p>Values less than zero will be treated as zero, and values greater than 40,000 will be treated as 40,000.

{:id="DirectionalLight.Type" .text .ro .bo} *Type*
: Returns the type of light as a String.

{:id="DirectionalLight.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="DirectionalLight.XRotation" .number .bo} *XRotation*
: The x rotation of the DirectionalLight in degrees.

{:id="DirectionalLight.YRotation" .number .bo} *YRotation*
: The y rotation of the DirectionalLight in degrees.

{:id="DirectionalLight.ZRotation" .number .bo} *ZRotation*
: The z rotation of the DirectionalLight in degrees.

### Events  {#DirectionalLight-Events}

{:.events}
None


### Methods  {#DirectionalLight-Methods}

{:.methods}

{:id="DirectionalLight.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Change its rotation to shine light in the direction of the DetectedPlane.

{:id="DirectionalLight.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Change its rotation to shine light in the direction of the node.

{:id="DirectionalLight.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Change its rotation to shine light in the direction of the Pointlight.

{:id="DirectionalLight.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Change its rotation to shine light in the direction of the (x,y,z) position.

{:id="DirectionalLight.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Change its rotation to shine light in the direction of the Spotlight.

{:id="DirectionalLight.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the DirectionalLight's x rotation by the given degrees.

{:id="DirectionalLight.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the DirectionalLight's y rotation by the given degrees.

{:id="DirectionalLight.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the DirectionalLight's z rotation by the given degrees.

## ImageMarker  {#ImageMarker}

Component for ImageMarker



### Properties  {#ImageMarker-Properties}

{:.properties}

{:id="ImageMarker.AttachedNodes" .list .ro .bo} *AttachedNodes*
: The nodes that are following the ImageMarker.

{:id="ImageMarker.Image" .text .do} *Image*
: The image file asset to be detected.

{:id="ImageMarker.PhysicalHeightInCentimeters" .number .ro .bo} *PhysicalHeightInCentimeters*
: The height of the image, in centimeters, in the real-world.

{:id="ImageMarker.PhysicalWidthInCentimeters" .number .ro} *PhysicalWidthInCentimeters*
: The width of the image, in centimeters, in the real-world.  This is used to determine how far the image is from the camera when it is detected.  This property must be greater than zero for the ImageMarker to be detected.  Values less than zero will be treated as their absolute value.

### Events  {#ImageMarker-Events}

{:.events}

{:id="ImageMarker.AppearedInView"} AppearedInView()
: The ImageMarker's Image has been detected in the view of the camera, after having not been detected in the view.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

{:id="ImageMarker.FirstDetected"} FirstDetected()
: The ImageMarker's image has been detected in the real-world for the first time.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

{:id="ImageMarker.NoLongerInView"} NoLongerInView()
: The ImageMarker's Image is no longer detected in the view of the camera, after having been in view.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

{:id="ImageMarker.PositionChanged"} PositionChanged(*x*{:.number},*y*{:.number},*z*{:.number})
: The position of the detected real-world image has changed to (x,y,z).  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

{:id="ImageMarker.Reset"} Reset()
: The ImageMarker's detection has been reset.  This is triggered by calling ResetDetectedItems on the ARView3D or if the detection information is no longer saved by the AR engine.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

{:id="ImageMarker.RotationChanged"} RotationChanged(*x*{:.number},*y*{:.number},*z*{:.number})
: The rotation of the detected real-world image has updated.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.

### Methods  {#ImageMarker-Methods}

{:.methods}
None


## ModelNode  {#ModelNode}

Component for ModelNode



### Properties  {#ModelNode-Properties}

{:.properties}

{:id="ModelNode.BoundingBox" .list .ro .bo} *BoundingBox*
: The minimum and maximum coordinates of the ModelNode.  The minimum and maximum are lists of the x-, y-, z-coordinates, and this returns a list of <code>[min, max]</code> where mine and max are <code>[x, y, z]</code>.

{:id="ModelNode.FillColor" .color .do} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="ModelNode.FillColorOpacity" .number .do} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ModelNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="ModelNode.Model" .text .ro} *Model*
: The 3D model file to be loaded.

{:id="ModelNode.NamesOfNodes" .list .ro .bo} *NamesOfNodes*
: Returns a list of the names of all nodes in the model.  If the model did not name a node, then the node will be named by the component Name and number, such as ModelNode1-1.

{:id="ModelNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ModelNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="ModelNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="ModelNode.RootNodeName" .text .ro} *RootNodeName*
: The name of the root node to add to the scene.  If this is "" or a node with the given name does not exist, then the model's default root node will be added.

{:id="ModelNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="ModelNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="ModelNode.ShowShadow" .boolean .do} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="ModelNode.Texture" .text .do} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="ModelNode.TextureOpacity" .number .do} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="ModelNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="ModelNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="ModelNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="ModelNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="ModelNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="ModelNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="ModelNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="ModelNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#ModelNode-Events}

{:.events}

{:id="ModelNode.Click"} Click()
: The user clicked on the node.

{:id="ModelNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="ModelNode.NodeNotFound"} NodeNotFound(*name*{:.text})
: This event is triggered when the user tries to access a node named "name", but a node with that "name" does not exist.

{:id="ModelNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#ModelNode-Methods}

{:.methods}

{:id="ModelNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="ModelNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="ModelNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="ModelNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="ModelNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="ModelNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="ModelNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="ModelNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="ModelNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="ModelNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="ModelNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="ModelNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="ModelNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="ModelNode.PlayAnimationsForAllNodes" class="method"} <i/> PlayAnimationsForAllNodes()
: Plays all animations in the model, if it has animations.

{:id="ModelNode.PlayAnimationsForNode" class="method"} <i/> PlayAnimationsForNode(*name*{:.text},*shouldPlayChildNodes*{:.boolean})
: Plays animations attached to a node named "name".  If a node named "name" does not exist, then the <code>NodeNotFound</code> event will be triggered.  <code>shouldPlayChildNodes</code> specifies if all nodes below below the named node in the node tree should also have their animations played.

{:id="ModelNode.RenameNode" class="method"} <i/> RenameNode(*oldName*{:.text},*newName*{:.text})
: Renames a node named "oldName" to "newName".  If no node exists with name "oldName", then the <code>NodeNotFound</code> event will be triggered.

{:id="ModelNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="ModelNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="ModelNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="ModelNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="ModelNode.SetFillColorForAllNodes" class="method"} <i/> SetFillColorForAllNodes(*color*{:.number},*opacity*{:.number})
: Sets the color of all nodes with the given opacity.  Opacity vales less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="ModelNode.SetFillColorForNode" class="method"} <i/> SetFillColorForNode(*name*{:.text},*color*{:.number},*opacity*{:.number},*shouldColorChildNodes*{:.boolean})
: <p>Sets the color of a node named "name" with the given opacity.  If a node named "name" does not exist, then the <code>NodeNotFound</code> event will be triggered.  <code>shouldColorChildNodes</code> specifies if all nodes below below the named node in the node tree should also have their color set.  If <code>shouldColorChildNodes</code> is false and the named node cannot be colored, an error will occur.  Otherwise, no error will occur, and the child nodes will attempt to be colored.</p>Opacity vales less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="ModelNode.SetShowShadowForAllNodes" class="method"} <i/> SetShowShadowForAllNodes(*showShadow*{:.boolean})
: Sets if all nodes show a shadow.

{:id="ModelNode.SetShowShadowForNode" class="method"} <i/> SetShowShadowForNode(*name*{:.text},*showShadow*{:.boolean},*shouldShadowChildNodes*{:.boolean})
: <p>Sets whether the shadow is shown for a node named "name".  If a node named "name" does not exist, then the <code>NodeNotFound</code> event will be triggered.  <code>shouldShadowChildNodes</code> specifies if all nodes below below the named node in the node tree should also have their shadow set.

{:id="ModelNode.SetTextureForAllNodes" class="method"} <i/> SetTextureForAllNodes(*texture*{:.text},*opacity*{:.number})
: Sets the texture of all nodes with the given opacity.Opacity vales less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="ModelNode.SetTextureForNode" class="method"} <i/> SetTextureForNode(*name*{:.text},*texture*{:.text},*opacity*{:.number},*shouldTexturizeChildNodes*{:.boolean})
: <p>Sets the texture of a node named "name" with the given opacity.  If a node named "name" does not exist, then the <code>NodeNotFound</code> event will be triggered.  <code>shouldTexturizeChildNodes</code> specifies if all nodes below below the named node in the node tree should also have their color set.  If <code>shouldTexturizeChildNodes</code> is false and the named node cannot be textured, an error will occur.  Otherwise, no error will occur, and the child nodes will attempt to be textured.</p>Opacity vales less than 0 will be treated as 0, and values greater than 100 will be treated as 100.

{:id="ModelNode.StopAnimationsForAllNodes" class="method"} <i/> StopAnimationsForAllNodes()
: Stops all animations in the model, if it has animations.

{:id="ModelNode.StopAnimationsForNode" class="method"} <i/> StopAnimationsForNode(*name*{:.text},*shouldStopChildNodes*{:.boolean})
: Stops animations attached to a node named "name".  If a node named "name" does not exist, then the <code>NodeNotFound</code> event will be triggered.  <code>shouldStopChildNodes</code> specifies if all nodes below below the named node in the node tree should also have their animations stopped.

{:id="ModelNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## PlaneNode  {#PlaneNode}

Component for PlaneNode



### Properties  {#PlaneNode-Properties}

{:.properties}

{:id="PlaneNode.CornerRadius" .number} *CornerRadius*
: This determines how rounded the boxes corners will be.  A value of zero specifies no rounded corners, and a value of half the height or width of the PlaneNode (whichever is greater) makes it fully rounded, with no straight edges.  Values less than zero will be treated as zero.

{:id="PlaneNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="PlaneNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PlaneNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the PlaneNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode will not be shown.

{:id="PlaneNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="PlaneNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PlaneNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="PlaneNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="PlaneNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="PlaneNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="PlaneNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="PlaneNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="PlaneNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PlaneNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="PlaneNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="PlaneNode.WidthInCentimeters" .number} *WidthInCentimeters*
: How far, in centimeters, the PlaneNode extends along the x-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode will not be shown.

{:id="PlaneNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="PlaneNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="PlaneNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="PlaneNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="PlaneNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="PlaneNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#PlaneNode-Events}

{:.events}

{:id="PlaneNode.Click"} Click()
: The user clicked on the node.

{:id="PlaneNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="PlaneNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#PlaneNode-Methods}

{:.methods}

{:id="PlaneNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="PlaneNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="PlaneNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="PlaneNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="PlaneNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="PlaneNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="PlaneNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="PlaneNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="PlaneNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="PlaneNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="PlaneNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="PlaneNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="PlaneNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="PlaneNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="PlaneNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="PlaneNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="PlaneNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="PlaneNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## PointLight  {#PointLight}

Component for PointLight



### Properties  {#PointLight-Properties}

{:.properties}

{:id="PointLight.Color" .color} *Color*
: The color of the light's rays.

{:id="PointLight.FalloffEndDistance" .number} *FalloffEndDistance*
: The distance, in centimeters, at which the light's intensity goes to zero.  Points past this distance are not lit by the light.  Values less than zero will be treated as zero.  When set to zero, the light's intensity does not falloff.

{:id="PointLight.FalloffStartDistance" .number} *FalloffStartDistance*
: The distance, in centimeters, at which the light's intensity starts to falloff or diminish.  A value of 0 specifies no falloff.  Values less than zero will be treated as zero.

{:id="PointLight.FalloffType" .number} *FalloffType*
: <p>This specifies the speed at which the light falloffs.  None means that the light never falls off to zero.  Linear means that it decreases evenly throughout, and Quadratic means it decreases faster the further away from the light.</p>Valid values are: 0 (None), 1 (Linear), 2 (Quadratic)

{:id="PointLight.Intensity" .number} *Intensity*
: The brightness of the light.  The default value is 1000. Lower values darken the light, and its color, whereas higher values brighten it.

{:id="PointLight.Temperature" .number} *Temperature*
: <p>The temperature of the light, in degrees Kelvin. This, paired with the Color, determine the color of the light rays.  The default value, 6,500 represents white light.  Lower values add a warmer, or yellow, effect to the light, and greater values as a cooler, or blue, effect to the light.</p>Values less than zero will be treated as zero, and values greater than 40,000 will be treated as 40,000.

{:id="PointLight.Type" .text .ro .bo} *Type*
: Returns the type of light as a String.

{:id="PointLight.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="PointLight.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the PointLight.

{:id="PointLight.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the PointLight.

{:id="PointLight.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the PointLight.

### Events  {#PointLight-Events}

{:.events}
None


### Methods  {#PointLight-Methods}

{:.methods}

{:id="PointLight.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between the PointLight and a detectedPlane.

{:id="PointLight.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between the PointLight and a node.

{:id="PointLight.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between two PointLights.

{:id="PointLight.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between the PointLight and a Spotlight.

{:id="PointLight.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the PointLight's position by (x,y,z).

{:id="PointLight.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the PointLight's position to (x,y,z).

## PyramidNode  {#PyramidNode}

Component for PyramidNode



### Properties  {#PyramidNode-Properties}

{:.properties}

{:id="PyramidNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="PyramidNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PyramidNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the PyramidNode extends along the y-axis.  Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode will not be shown.

{:id="PyramidNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="PyramidNode.LengthInCentimeters" .number} *LengthInCentimeters*
: How far, in centimeters, the PyramidNode extends along the z-axis.  Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode will not be shown.

{:id="PyramidNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PyramidNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="PyramidNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="PyramidNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="PyramidNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="PyramidNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="PyramidNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="PyramidNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="PyramidNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="PyramidNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="PyramidNode.WidthInCentimeters" .number} *WidthInCentimeters*
: How far, in centimeters, the PyramidNode extends along the x-axis.  Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode will not be shown.

{:id="PyramidNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="PyramidNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="PyramidNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="PyramidNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="PyramidNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="PyramidNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#PyramidNode-Events}

{:.events}

{:id="PyramidNode.Click"} Click()
: The user clicked on the node.

{:id="PyramidNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="PyramidNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#PyramidNode-Methods}

{:.methods}

{:id="PyramidNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="PyramidNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="PyramidNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="PyramidNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="PyramidNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="PyramidNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="PyramidNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="PyramidNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="PyramidNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="PyramidNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="PyramidNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="PyramidNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="PyramidNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="PyramidNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="PyramidNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="PyramidNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="PyramidNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="PyramidNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## SphereNode  {#SphereNode}

Component for SphereNode



### Properties  {#SphereNode-Properties}

{:.properties}

{:id="SphereNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="SphereNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="SphereNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="SphereNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="SphereNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="SphereNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="SphereNode.RadiusInCentimeters" .number} *RadiusInCentimeters*
: The radius of the sphere in centimeters.

{:id="SphereNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="SphereNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="SphereNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="SphereNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="SphereNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="SphereNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="SphereNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="SphereNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="SphereNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="SphereNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="SphereNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="SphereNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="SphereNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#SphereNode-Events}

{:.events}

{:id="SphereNode.Click"} Click()
: The user clicked on the node.

{:id="SphereNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="SphereNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#SphereNode-Methods}

{:.methods}

{:id="SphereNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="SphereNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="SphereNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="SphereNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="SphereNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="SphereNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="SphereNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="SphereNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="SphereNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="SphereNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="SphereNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="SphereNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="SphereNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="SphereNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="SphereNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="SphereNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="SphereNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="SphereNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## Spotlight  {#Spotlight}

Component for Spotlight



### Properties  {#Spotlight-Properties}

{:.properties}

{:id="Spotlight.CastsShadows" .boolean} *CastsShadows*
: If this property is set to true, then nodes illuminated by the Spotlight will cast shadows, if ShowShadow for the node is true.  Otherwise, the nodes it illuminates will not cast shadows.

{:id="Spotlight.Color" .color} *Color*
: The color of the light's rays.

{:id="Spotlight.FalloffEndDistance" .number} *FalloffEndDistance*
: The distance, in centimeters, at which the light's intensity goes to zero.  Points past this distance are not lit by the light.  Values less than zero will be treated as zero.  When set to zero, the light's intensity does not falloff.

{:id="Spotlight.FalloffStartDistance" .number} *FalloffStartDistance*
: The distance, in centimeters, at which the light's intensity starts to falloff or diminish.  Values less than zero will be treated as zero.

{:id="Spotlight.FalloffType" .number} *FalloffType*
: <p>This specifies the speed at which the light falloffs.  None means that the light never falls off to zero.  Linear means that it decreases evenly throughout, and Quadratic means it decreases faster the further away from the light.</p>Valid values are: 0 (None), 1 (Linear), 2 (Quadratic)

{:id="Spotlight.Intensity" .number} *Intensity*
: The brightness of the light.  The default value is 1000. Lower values darken the light, and its color, whereas higher values brighten it.

{:id="Spotlight.MaximumDistanceForShadows" .number} *MaximumDistanceForShadows*
: This specifies the furthest distance away an object can be in order for the Spotlight to make it cast shadows.  Objects further than this will not cast shadows due to this Spotlight.  Values less than zero will be treated as zero.

{:id="Spotlight.MinimumDistanceForShadows" .number} *MinimumDistanceForShadows*
: This specifies the closest distance away an object can be in order for the Spotlight to make it cast shadows.  Objects closer than this will not cast shadows due to this Spotlight. Values less than zero will be treated as zero.

{:id="Spotlight.ShadowColor" .color} *ShadowColor*
: The color of the shadows that nodes illuminated by the Spotlight will cast, if CastsShadows is true.

{:id="Spotlight.ShadowOpacity" .number} *ShadowOpacity*
: The opacity of the shadows that nodes illuminated by the Spotlight will cast, if CastsShadows is true.  This determines how intense the shadows are.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="Spotlight.SpotInnerAngle" .number} *SpotInnerAngle*
: This specifies the area at which the Spotlight's intensity is at full strength.  This area is defined by an angle in degrees.  From the SpotInnerAngle to the SpotOuterAngle, the light's intensity transitions to zero.  Values less than zero will be treated as zero, and values greater than 180 will be treated as 180.

{:id="Spotlight.SpotOuterAngle" .number} *SpotOuterAngle*
: This specifies the area at which the Spotlight's intensity is at non-zero strength.  This area is defined by an angle in degrees.  From the SpotInnerAngle to the SpotOuterAngle, the light's intensity transitions to zero.  Outside the SpotOuterAngle, the intensity is zero.  Values less than zero will be treated as zero, and values greater than 180 will be treated as 180.

{:id="Spotlight.Temperature" .number} *Temperature*
: <p>The temperature of the light, in degrees Kelvin. This, paired with the Color, determine the color of the light rays.  The default value, 6,500 represents white light.  Lower values add a warmer, or yellow, effect to the light, and greater values as a cooler, or blue, effect to the light.</p>Values less than zero will be treated as zero, and values greater than 40,000 will be treated as 40,000.

{:id="Spotlight.Type" .text .ro .bo} *Type*
: Returns the type of light as a String.

{:id="Spotlight.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="Spotlight.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the Spotlight.

{:id="Spotlight.XRotation" .number .bo} *XRotation*
: The x rotation of the Spotlight in degrees.

{:id="Spotlight.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the Spotlight.

{:id="Spotlight.YRotation" .number .bo} *YRotation*
: The y rotation of the Spotlight in degrees.

{:id="Spotlight.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the Spotlight.

{:id="Spotlight.ZRotation" .number .bo} *ZRotation*
: The z rotation of the Spotlight in degrees.

### Events  {#Spotlight-Events}

{:.events}
None


### Methods  {#Spotlight-Methods}

{:.methods}

{:id="Spotlight.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between the Spotlight and a DetectedPlane.

{:id="Spotlight.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between the Spotlight and a node.

{:id="Spotlight.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between the Spotlight and a PointLight.

{:id="Spotlight.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between two Spotlights.

{:id="Spotlight.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Change its rotation to shine light in the direction of the DetectedPlane.

{:id="Spotlight.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Change its rotation to shine light in the direction of the node.

{:id="Spotlight.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Change its rotation to shine light in the direction of the PointLight.

{:id="Spotlight.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Change its rotation to shine light in the direction of the (x,y,z) position.

{:id="Spotlight.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Change its rotation to shine light in the direction of the Spotlight.

{:id="Spotlight.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the PointLight's position by (x,y,z).

{:id="Spotlight.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the PointLight's position to (x,y,z).

{:id="Spotlight.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the Spotlight's x rotation by the given degrees.

{:id="Spotlight.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the Spotlight's y rotation by the given degrees.

{:id="Spotlight.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the Spotlight's z rotation by the given degrees.

## TextNode  {#TextNode}

Component for TextNode



### Properties  {#TextNode-Properties}

{:.properties}

{:id="TextNode.DepthInCentimeters" .number} *DepthInCentimeters*
: How far, in centimeters, the TextNode extends along the z-axis.  Values less than zero will be treated as zero.

{:id="TextNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="TextNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TextNode.FontSizeInCentimeters" .number} *FontSizeInCentimeters*
: The font size in centimeters.  Values less than zero will be treated as their absolute value.  When set to zero, the TextNode will not be shown.

{:id="TextNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="TextNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TextNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="TextNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="TextNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="TextNode.Scale" .number .do} *Scale*
: Functions from ARNodeBase that should not be user facing.

{:id="TextNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="TextNode.Text" .text} *Text*
: Text to display by the TextNode.  If this is set to "", the TextNode will not be shown.

{:id="TextNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="TextNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TextNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="TextNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="TextNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="TextNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="TextNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="TextNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="TextNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="TextNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#TextNode-Events}

{:.events}

{:id="TextNode.Click"} Click()
: The user clicked on the node.

{:id="TextNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="TextNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#TextNode-Methods}

{:.methods}

{:id="TextNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="TextNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="TextNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="TextNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="TextNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="TextNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="TextNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="TextNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="TextNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="TextNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="TextNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="TextNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="TextNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="TextNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="TextNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="TextNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="TextNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="TextNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## TorusNode  {#TorusNode}

Component for TorusNode



### Properties  {#TorusNode-Properties}

{:.properties}

{:id="TorusNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="TorusNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TorusNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="TorusNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TorusNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="TorusNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="TorusNode.PipeRadiusInCentimeters" .number} *PipeRadiusInCentimeters*
: The pipe radius defines the size of the surface that encircles the ring, or the pipe radius.  This can be thought of as the band of the ring.  Values less than zero will be treated as their absolute value.  When set to zero, the TorusNode will not be shown.

{:id="TorusNode.RingRadiusInCentimeters" .number} *RingRadiusInCentimeters*
: The ring radius defines the size of the overall torus (or major radius) in centimeters.  Values less than zero will be treated as their absolute value.  When set to zero, the TorusNode will not be shown.

{:id="TorusNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="TorusNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="TorusNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="TorusNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="TorusNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TorusNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="TorusNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="TorusNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="TorusNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="TorusNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="TorusNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="TorusNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="TorusNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#TorusNode-Events}

{:.events}

{:id="TorusNode.Click"} Click()
: The user clicked on the node.

{:id="TorusNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="TorusNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#TorusNode-Methods}

{:.methods}

{:id="TorusNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="TorusNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="TorusNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="TorusNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="TorusNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="TorusNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="TorusNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="TorusNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="TorusNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="TorusNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="TorusNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="TorusNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="TorusNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="TorusNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="TorusNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="TorusNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="TorusNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="TorusNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## TubeNode  {#TubeNode}

Component for TubeNode



### Properties  {#TubeNode-Properties}

{:.properties}

{:id="TubeNode.FillColor" .color} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="TubeNode.FillColorOpacity" .number} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TubeNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the TubeNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the TubeNode will not be shown.

{:id="TubeNode.InnerRadiusInCentimeters" .number} *InnerRadiusInCentimeters*
: The inner radius of the TubeNode determines the size of the cicular cutout from the middle of the TubeNode.  Values less than zero will be treated as ther absolute value.  When set to zero or when set to equal to or greater than the OuterRadius, the TubeNode will not be shown.

{:id="TubeNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="TubeNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TubeNode.OuterRadiusInCentimeters" .number} *OuterRadiusInCentimeters*
: The outer radius of the TubeNode determines the overall size of the cicular base and top.  Values less than zero will be treated as ther absolute value.  When set to zero or when set to equal to or less than the InnerRadius, the TubeNode will not be shown.

{:id="TubeNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="TubeNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="TubeNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="TubeNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="TubeNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="TubeNode.Texture" .text} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="TubeNode.TextureOpacity" .number} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="TubeNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="TubeNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="TubeNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="TubeNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="TubeNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="TubeNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="TubeNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="TubeNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#TubeNode-Events}

{:.events}

{:id="TubeNode.Click"} Click()
: The user clicked on the node.

{:id="TubeNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="TubeNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#TubeNode-Methods}

{:.methods}

{:id="TubeNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="TubeNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="TubeNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="TubeNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="TubeNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="TubeNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="TubeNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="TubeNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="TubeNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="TubeNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="TubeNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="TubeNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="TubeNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="TubeNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="TubeNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="TubeNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="TubeNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="TubeNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## VideoNode  {#VideoNode}

Component for VideoNode



### Properties  {#VideoNode-Properties}

{:.properties}

{:id="VideoNode.FillColor" .color .do} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="VideoNode.FillColorOpacity" .number .do} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="VideoNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the VideoNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode will not be shown.

{:id="VideoNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="VideoNode.IsPlaying" .boolean .ro .bo} *IsPlaying*
: Returns true if the video is currently playing false otherwise.

{:id="VideoNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="VideoNode.PanToMove" .boolean} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="VideoNode.PinchToScale" .boolean} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="VideoNode.RotateWithGesture" .boolean} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="VideoNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="VideoNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="VideoNode.Source" .text .wo} *Source*
: The "path" to the video.  Usually, this will be the name of the video file, which should be added in the Designer.

{:id="VideoNode.Texture" .text .do} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="VideoNode.TextureOpacity" .number .do} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="VideoNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="VideoNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="VideoNode.Volume" .number .wo} *Volume*
: Sets the volume property to a number between 0 and 100.

{:id="VideoNode.WidthInCentimeters" .number} *WidthInCentimeters*
: How far, in centimeters, the VideoNode extends along the x-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the VideoNode will not be shown.

{:id="VideoNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="VideoNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="VideoNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="VideoNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="VideoNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="VideoNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#VideoNode-Events}

{:.events}

{:id="VideoNode.Click"} Click()
: The user clicked on the node.

{:id="VideoNode.Completed"} Completed()
: Indicated that the video has reached the end.

{:id="VideoNode.LongClick"} LongClick()
: The user long-pressed on the node.

{:id="VideoNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#VideoNode-Methods}

{:.methods}

{:id="VideoNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="VideoNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="VideoNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="VideoNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="VideoNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="VideoNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="VideoNode.GetDuration" class="method returns number"} <i/> GetDuration()
: Returns duration of the video in milliseconds.

{:id="VideoNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="VideoNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="VideoNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="VideoNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="VideoNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="VideoNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="VideoNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="VideoNode.Pause" class="method"} <i/> Pause()
: Pauses playback of the video.

{:id="VideoNode.Play" class="method"} <i/> Play()
: Starts playback of the video.

{:id="VideoNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="VideoNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="VideoNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="VideoNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="VideoNode.SeekTo" class="method"} <i/> SeekTo(*ms*{:.number})
: Seeks to the requested time (specified in milliseconds) in the video. If the video is paused, the frame shown will not be updated by the seek. The player can jump only to key frames in the video, so seeking to times that differ by short intervals may not actually move to different frames.

{:id="VideoNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.

## WebViewNode  {#WebViewNode}

Component for WebViewNode



### Properties  {#WebViewNode-Properties}

{:.properties}

{:id="WebViewNode.FillColor" .color .do} *FillColor*
: The color of the node.  If the Texture is set, the color is not shown.

{:id="WebViewNode.FillColorOpacity" .number .do} *FillColorOpacity*
: The opacity of the node's FillColor.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="WebViewNode.HeightInCentimeters" .number} *HeightInCentimeters*
: How far, in centimeters, the WebViewNode extends along the y-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the WebViewNode will not be shown.

{:id="WebViewNode.HomeUrl" .text} *HomeUrl*
: URL of the page the WebViewNode should initially open to.  Setting this will load the page.

{:id="WebViewNode.IsFollowingImageMarker" .boolean .ro .bo} *IsFollowingImageMarker*
: Specifies whether a node is following an ImageMarker.  Returns true if it is and false otherwise.

{:id="WebViewNode.Opacity" .number} *Opacity*
: Sets the opacity of the node.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="WebViewNode.PanToMove" .boolean .do} *PanToMove*
: If the property is set to true, then the node can be moved using a one finger pan gesture.  Otherwise, a node's x,y-position cannot be changed by the pan gesture.

{:id="WebViewNode.PinchToScale" .boolean .do} *PinchToScale*
: If the property is set to true, then the node can be scaled using the pinch gesture.  Otherwise, a node's Scale cannot be changed by the pinch gesture.

{:id="WebViewNode.RotateWithGesture" .boolean .do} *RotateWithGesture*
: If the property is set to true, the the node can be rotated around its y-axis using a two finger rotation gesture.  Clockwise increases the angle, and counter clockwise decreases the angle.  Otherwise, the node's rotation cannot be changed with a rotation gesutre.

{:id="WebViewNode.Scale" .number} *Scale*
: The scale of the node.  This is used to multiply its sizing properties.  Values less than zero will be treated as their absolute value.

{:id="WebViewNode.ShowShadow" .boolean} *ShowShadow*
: Specifies whether the node should show a shadow when it is lit by Lights.

{:id="WebViewNode.Texture" .text .do} *Texture*
: The image used to texture the node.  If set, the FillColor is not shown.

{:id="WebViewNode.TextureOpacity" .number .do} *TextureOpacity*
: The opacity of the node's Texture.  Values less than zero will be treated as zero, and values greater than 100 will be treated as 100.

{:id="WebViewNode.Type" .text .ro .bo} *Type*
: Returns the type of node as a String.

{:id="WebViewNode.Visible" .boolean} *Visible*
: Specifies whether the component should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="WebViewNode.WidthInCentimeters" .number} *WidthInCentimeters*
: How far, in centimeters, the WebViewNode extends along the x-axis.  Values less than zero will be treated as their absolute value.  When set to zero, the WebViewNode will not be shown.

{:id="WebViewNode.XPosition" .number .bo} *XPosition*
: The x position in centimeters of the node.

{:id="WebViewNode.XRotation" .number .bo} *XRotation*
: The x rotation of the node in degrees.

{:id="WebViewNode.YPosition" .number .bo} *YPosition*
: The y position in centimeters of the node.

{:id="WebViewNode.YRotation" .number .bo} *YRotation*
: The y rotation of the node in degrees.

{:id="WebViewNode.ZPosition" .number .bo} *ZPosition*
: The z position in centimeters of the node.

{:id="WebViewNode.ZRotation" .number .bo} *ZRotation*
: The z rotation of the node in degrees.

### Events  {#WebViewNode-Events}

{:.events}

{:id="WebViewNode.StoppedFollowingMarker"} StoppedFollowingMarker()
: The node stopped following an ImageMarker.  This event will trigger after the StopFollowingImageMarker block is called.

### Methods  {#WebViewNode-Methods}

{:.methods}

{:id="WebViewNode.CanGoBack" class="method returns boolean"} <i/> CanGoBack()
: Returns true if the WebViewNode can go backward in the history list.

{:id="WebViewNode.CanGoForward" class="method returns boolean"} <i/> CanGoForward()
: Returns true if the WebViewNode can go forward in the history list.

{:id="WebViewNode.DistanceToDetectedPlane" class="method returns number"} <i/> DistanceToDetectedPlane(*detectedPlane*{:.component})
: Caluates the distance, in centimeters, between a node and a DetectedPlane.

{:id="WebViewNode.DistanceToNode" class="method returns number"} <i/> DistanceToNode(*node*{:.component})
: Caluates the distance, in centimeters, between two nodes.

{:id="WebViewNode.DistanceToPointLight" class="method returns number"} <i/> DistanceToPointLight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Pointlight.

{:id="WebViewNode.DistanceToSpotlight" class="method returns number"} <i/> DistanceToSpotlight(*light*{:.component})
: Caluates the distance, in centimeters, between a node and a Spotlight.

{:id="WebViewNode.Follow" class="method"} <i/> Follow(*imageMarker*{:.component})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image.

{:id="WebViewNode.FollowWithOffset" class="method"} <i/> FollowWithOffset(*imageMarker*{:.component},*x*{:.number},*y*{:.number},*z*{:.number})
: Makes the node follow an ImageMarker and sets its position to be the center of the detected image with an offset of (x,y,z).

{:id="WebViewNode.GoBack" class="method"} <i/> GoBack()
: Go back to the previous page in the history list.  Does nothing if there is no previous page.

{:id="WebViewNode.GoForward" class="method"} <i/> GoForward()
: Go forward to the next page in the history list.   Does nothing if there is no next page.

{:id="WebViewNode.GoHome" class="method"} <i/> GoHome()
: Loads the home URL page.  This happens automatically when the home URL is changed.

{:id="WebViewNode.GoToUrl" class="method"} <i/> GoToUrl(*url*{:.text})
: Load the page at the given URL.

{:id="WebViewNode.LookAtDetectedPlane" class="method"} <i/> LookAtDetectedPlane(*detectedPlane*{:.component})
: Rotates the node to look at the DetectedPlane.

{:id="WebViewNode.LookAtNode" class="method"} <i/> LookAtNode(*node*{:.component})
: Rotates the node to look at the given node.

{:id="WebViewNode.LookAtPointLight" class="method"} <i/> LookAtPointLight(*light*{:.component})
: Rotates the node to look at the PointLight.

{:id="WebViewNode.LookAtPosition" class="method"} <i/> LookAtPosition(*x*{:.number},*y*{:.number},*z*{:.number})
: Rotates the node to look at the (x,y,z) position.

{:id="WebViewNode.LookAtSpotlight" class="method"} <i/> LookAtSpotlight(*light*{:.component})
: Rotates the node to look at the Spotlight.

{:id="WebViewNode.MoveBy" class="method"} <i/> MoveBy(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position by (x,y,z).

{:id="WebViewNode.MoveTo" class="method"} <i/> MoveTo(*x*{:.number},*y*{:.number},*z*{:.number})
: Changes the node's position to (x,y,z).

{:id="WebViewNode.Reload" class="method"} <i/> Reload()
: Reloads the current webpage.

{:id="WebViewNode.RotateXBy" class="method"} <i/> RotateXBy(*degrees*{:.number})
: Changes the node's x rotation by the given degrees.

{:id="WebViewNode.RotateYBy" class="method"} <i/> RotateYBy(*degrees*{:.number})
: Changes the node's y rotation by the given degrees.

{:id="WebViewNode.RotateZBy" class="method"} <i/> RotateZBy(*degrees*{:.number})
: Changes the node's z rotation by the given degrees.

{:id="WebViewNode.ScaleBy" class="method"} <i/> ScaleBy(*scalar*{:.number})
: Changes the node's scale by the given scalar.

{:id="WebViewNode.StopFollowingImageMarker" class="method"} <i/> StopFollowingImageMarker()
: Makes the node stop following the ImageMarker and sets its position to its current position when this block is called.
