# How to implement AR in your projects



This code is tested and known to work with Java 17, RealityKit on ios 15 or higher (ideally 18) and ARCore v 18.0.



## Setup Instructions (Manual)

To run this version of appinventor, follow the steps at the README.md
Additionally, to run AR, you will need an extended download size of at least 10MB


If you want to create an ar testserver, you will need to build and push via GCP. you can find those instructions <a href="https://docs.google.com/document/d/1mEFRzg1d97CGMGtYhVHfbDb4XTg9XGcU0ck0DZa0NkQ/edit?usp=sharing">here</a>



Nodes supported: 
BoxNode
CapsuleNode
ImageNode
ModelNode
PlaneNode
SphereNode
TextNode
WebViewNode
VideoNode

ImageMarkers are supported and nodes can be added to ImageMarkers by `FollowsMarker` and `FollowsMarkerWithOffset`. See example projects.

GeoMarkers are supported on iOS and Android, but not fully tested at this time.

Key features:
InitialPosition
InitialRotation
InitialGeoCoordinates

Scale is different between iOS and ARCore

SphereNode can roll or bounce or float.

VideoNode has a ChromaKey that allows for transparent greenscreen (or other colors)

TextNode is quite different between iOS and Android. In iOS, it has true geometery, in ARCore, it is just a skinny block.

all nodes can detect collisions. You must enable EnablePhysics for them to respond to collisions. 
ModelNodes have "play" methods, for Android you must use a GLB 3d model. for iOS, you must use a USDZ 3d model

