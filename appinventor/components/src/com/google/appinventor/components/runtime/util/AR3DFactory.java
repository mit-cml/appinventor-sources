// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;

import java.util.List;

import com.google.appinventor.components.runtime.util.YailList;


/**
 * Utilities used by the Augmented Reality components.
 *
 * @author niclarke@mit.edu (Nichole I. Clarke)
 */
public final class AR3DFactory {

  /**
   * ARNode is the root class of all nodes that are added to an AR 3D view.
   * It inherits from the Component interface since all ARNodes are components.
   * ARNode defines all of the general properties required by an AR node that should be implemented for App Inventor.
   *
   * @author niclarke@mit.edu (Nichole I. Clarke)
   */
   public interface ARNode extends Component, CanLook {

     // Properties
     int Height();
     void Height(int height);

     int Width();
     void Width(int width);

     String Type();

     boolean Visible();
     void Visible(boolean visible);

     boolean ShowShadow();
     void ShowShadow(boolean showShadow);

     int Opacity();
     void Opacity(int opacity);

     int FillColor();
     void FillColor(int color);

     int FillColorOpacity();
     void FillColorOpacity(int colorOpacity);

     String Texture();
     void Texture(String texture);

     int TextureOpacity();
     void TextureOpacity(int textureOpacity);

     boolean PinchToScale();
     void PinchToScale(boolean pinchToScale);

     boolean PanToMove();
     void PanToMove(boolean panToMove);

     boolean RotateWithGesture();
     void RotateWithGesture(boolean rotateWithGesture);

     float XPosition();
     void XPosition(float xPosition);

     float YPosition();
     void YPosition(float yPosition);

     float ZPosition();
     void ZPosition(float zPosition);

     float XRotation();
     void XRotation(float xRotation);

     float YRotation();
     void YRotation(float yRotation);

     float ZRotation();
     void ZRotation(float zRotation);

     float Scale();
     void Scale(float scalar);

     // NOTE: uncomment should we want to allow nonuniform scaling
     // float XScale();
     //
     // float YScale();
     //
     // float ZScale();

     // Methods
     void RotateXBy(float degrees);
     void RotateYBy(float degrees);
     void RotateZBy(float degrees);
     void ScaleBy(float scalar);
     void MoveBy(float x, float y, float z);
     void MoveTo(float x, float y, float z);
     float DistanceToNode(ARNode node);
     float DistanceToSpotlight(ARSpotlight light);
     float DistanceToPointLight(ARPointLight light);
     float DistanceToDetectedPlane(ARDetectedPlane detectedPlane);

     // Events
     void Click();
     void LongClick();
   }

   public interface FollowsMarker {
     boolean IsFollowingImageMarker();

     void Follow(ARImageMarker imageMarker);
     void FollowWithOffset(ARImageMarker imageMarker, float x, float y, float z);
     void StopFollowingImageMarker();

     void StoppedFollowingMarker();
   }

   /**
    * HasCornerRadius is a node interface for nodes that have a corner radius.
    *
    * @author niclarke@mit.edu (Nichole I. Clarke)
    */
   public interface HasCornerRadius {
     float CornerRadius();
     void CornerRadius(float cornerRadius);

   }

   /**
    * HasWidthInCentimeters is a node interface for nodes that have a width.
    *
    * @author niclarke@mit.edu (Nichole I. Clarke)
    */
   public interface HasWidthInCentimeters {
     float WidthInCentimeters();
     void WidthInCentimeters(float widthInCentimeters);
   }

   /**
    * HasHeightInCentimeters is a node interface for nodes that have a height.
    *
    * @author niclarke@mit.edu (Nichole I. Clarke)
    */
   public interface HasHeightInCentimeters {
     float HeightInCentimeters();
     void HeightInCentimeters(float heightInCentimeters);
   }

   public interface ARBox  extends ARNode, HasWidthInCentimeters, HasHeightInCentimeters, HasCornerRadius {
     float LengthInCentimeters();
     void LengthInCentimeters(float lengthInCentimeters);
   }

   /**
    * ARSphere is a node interface for spheres.
    *
    * @author niclarke@mit.edu (Nichole I. Clarke)
    */
   public interface ARSphere extends ARNode {
     float RadiusInCentimeters();
     void RadiusInCentimeters(float radiusInCentimeters);
   }

   /**
    * ARPlane is a node interface for planes.
    *
    * @author niclarke@mit.edu (Nichole I. Clarke)
    */
   public interface ARPlane extends ARNode, HasWidthInCentimeters, HasHeightInCentimeters, HasCornerRadius {}

   public interface ARCylinder  extends ARNode, HasHeightInCentimeters {
     float RadiusInCentimeters();
     void RadiusInCentimeters(float radiusInCentimeters);
   }

   public interface ARCone  extends ARNode, HasHeightInCentimeters {
     float TopRadiusInCentimeters();
     void TopRadiusInCentimeters(float topRadiusInCentimeters);

     float BottomRadiusInCentimeters();
     void BottomRadiusInCentimeters(float topRadiusInCentimeters);
   }

   public interface ARCapsule  extends ARNode, HasHeightInCentimeters {
     float CapRadiusInCentimeters();
     void CapRadiusInCentimeters(float capRadiusInCentimeters);
   }

   public interface ARTube  extends ARNode, HasHeightInCentimeters {
     float OuterRadiusInCentimeters();
     void OuterRadiusInCentimeters(float outerRadiusInCentimeters);

     float InnerRadiusInCentimeters();
     void InnerRadiusInCentimeters(float innerRadiusInCentimeters);
   }

   public interface ARTorus  extends ARNode {
     float RingRadiusInCentimeters();
     void RingRadiusInCentimeters(float ringRadiusInCentimeters);

     float PipeRadiusInCentimeters();
     void PipeRadiusInCentimeters(float pipeRadiusInCentimeters);
   }

   public interface ARPyramid  extends ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
     float LengthInCentimeters();
     void LengthInCentimeters(float lengthInCentimeters);
   }

   public interface ARText  extends ARNode {
     String Text();
     void Text(String text);

     float FontSizeInCentimeters();
     void FontSizeInCentimeters(float fontSizeInCentimeters);

     // These have no meaning unless a container frame is set.
     // boolean WrapText();
     // void WrapText(boolean wrapText);

     // int TextAlignment();
     // void TextAlignment(int textAlignment);

     // int Truncation();
     // void Truncation(int truncation);

     float DepthInCentimeters();
     void DepthInCentimeters(float depthInCentimeters);
   }

   public interface ARVideo  extends ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
     void Source(String source);

     boolean IsPlaying();

     void Volume(int volume);

     void Play();
     void Pause();
     int GetDuration();
     void SeekTo(int ms);
     void Completed();
   }

   public interface ARWebView extends ARNode, HasWidthInCentimeters, HasHeightInCentimeters {
     String HomeUrl();
     void HomeUrl(String url);

     boolean CanGoBack();
     boolean CanGoForward();
     void GoBack();
     void GoForward();
     void Reload();
     void GoToUrl(String url);
     void GoHome();
   }

   public interface ARModel extends ARNode {
     String Model();
     void Model(String model);

     String RootNodeName();
     void RootNodeName(String rootNodeName);

     List<YailList> BoundingBox();

     List<String> NamesOfNodes();

     // Functions
     void SetFillColorForAllNodes(int color, int opacity);
     void SetFillColorForNode(String name, int color, int opacity, boolean shouldColorChildNodes);
     void SetTextureForNode(String name, String texture, int opacity, boolean shouldTexturizeChildNodes);
     void SetTextureForAllNodes(String texture, int opacity);
     void SetShowShadowForNode(String name, boolean showShadow, boolean shouldShadowChildNodes);
     void SetShowShadowForAllNodes(boolean showShadow);
     void PlayAnimationsForAllNodes();
     void PlayAnimationsForNode(String name, boolean shouldPlayChildNodes);
     void StopAnimationsForAllNodes();
     void StopAnimationsForNode(String name, boolean shouldPlayChildNodes);
     void RenameNode(String oldName, String newName);

     void NodeNotFound(String name);
   }

   // Detected Items
   public interface ARImageMarker extends Component {
     String Image();
     void Image(String image);

     float PhysicalWidthInCentimeters();
     void PhysicalWidthInCentimeters(float width);

     float PhysicalHeightInCentimeters();

     List<ARNode> AttachedNodes();

     // Functions


     // EVENTS
     void FirstDetected();
     void PositionChanged(float x, float y, float z);
     void RotationChanged(float x, float y, float z);
     void NoLongerInView();
     void AppearedInView();
     void Reset();
   }

   public interface ARDetectedPlane extends Component {
     boolean IsHorizontal();

     int Opacity();
     void Opacity(int opacity);

     int FillColor();
     void FillColor(int color);

     int FillColorOpacity();
     void FillColorOpacity(int colorOpacity);

     String Texture();
     void Texture(String texture);

     int TextureOpacity();
     void TextureOpacity(int textureOpacity);
   }

   // Lights
   public interface ARLight extends Component {
     String Type();

     int Color();
     void Color(int color);

     float Temperature();
     void Temperature(float temperature);

     float Intensity();
     void Intensity(float intensity);

     boolean Visible();
     void Visible(boolean visible);
   }

   public interface HasPositionEffects {
     float XPosition();
     void XPosition(float xPosition);

     float YPosition();
     void YPosition(float yPosition);

     float ZPosition();
     void ZPosition(float zPosition);

     void MoveBy(float x, float y, float z);
     void MoveTo(float x, float y, float z);
     float DistanceToNode(ARNode node);
     float DistanceToSpotlight(ARSpotlight light);
     float DistanceToPointLight(ARPointLight light);
     float DistanceToDetectedPlane(ARDetectedPlane detectedPlane);
   }

   public interface HasDirectionEffects {
     float XRotation();
     void XRotation(float xRotation);

     float YRotation();
     void YRotation(float yRotation);

     float ZRotation();
     void ZRotation(float zRotation);

     void RotateXBy(float degrees);
     void RotateYBy(float degrees);
     void RotateZBy(float degrees);
   }

   public interface HasFalloff {
     float FalloffStartDistance();
     void FalloffStartDistance(float distance);

     float FalloffEndDistance();
     void FalloffEndDistance(float distance);

     int FalloffType();
     void FalloffType(int falloffType);
   }

   public interface CastsShadows {
     boolean CastsShadows();
     void CastsShadows(boolean castsShadows);

     int ShadowColor();
     void ShadowColor(int shadowColor);

     int ShadowOpacity();
     void ShadowOpacity(int shadowOpacity);
   }

   public interface CanLook {
     void LookAtNode(ARNode node);
     void LookAtDetectedPlane(ARDetectedPlane detectedPlane);
     void LookAtSpotlight(ARSpotlight light);
     void LookAtPointLight(ARPointLight light);
     void LookAtPosition(float x, float y, float z);
   }

   public interface ARAmbientLight extends ARLight {

   }

   public interface ARDirectionalLight extends ARLight, HasDirectionEffects, CastsShadows, CanLook {

   }

   public interface ARPointLight extends ARLight, HasFalloff, HasPositionEffects {

   }

   public interface ARSpotlight extends ARLight, HasFalloff, HasDirectionEffects, HasPositionEffects, CastsShadows, CanLook {
     float SpotInnerAngle();
     void SpotInnerAngle(float angle);

     float SpotOuterAngle();
     void SpotOuterAngle(float angle);

     float MaximumDistanceForShadows();
     void MaximumDistanceForShadows(float distance);

     float MinimumDistanceForShadows();
     void MinimumDistanceForShadows(float distance);
   }

   // Containers

   public interface ARNodeContainer extends ComponentContainer {
     List<ARNode> Nodes();

     boolean ShowWireframes();
     void ShowWireframes(boolean showWireframes);

     boolean ShowBoundingBoxes();
     void ShowBoundingBoxes(boolean showBoundingBoxes);

     // getARView()
     void NodeClick(ARNode node);
     void NodeLongClick(ARNode node);
     void TapAtPoint(float x, float y, float z, boolean isANodeAtPoint);
     void LongPressAtPoint(float x, float y, float z, boolean isANodeAtPoint);
   }

   public interface ARImageMarkerContainer extends ComponentContainer {
     List<ARImageMarker> ImageMarkers();
   }

   public interface ARDetectedPlaneContainer extends ComponentContainer {
     List<ARDetectedPlane> DetectedPlanes();

     // Events
     void ClickOnDetectedPlaneAt(ARDetectedPlane detectedPlane, float x, float y, float z, boolean isANodeAtPoint);
     void LongClickOnDetectedPlaneAt(ARDetectedPlane detectedPlane, float x, float y, float z, boolean isANodeAtPoint);
     void PlaneDetected(ARDetectedPlane detectedPlane);
     void DetectedPlaneUpdated(ARDetectedPlane detectedPlane);
     void DetectedPlaneRemoved(ARDetectedPlane detectedPlane);
   }

   public interface ARLightContainer extends ComponentContainer {
     List<ARLight> Lights();
     boolean LightingEstimation();
     void LightingEstimation(boolean lightingEstimation);

     boolean ShowLightLocations();
     void ShowLightLocations(boolean showLightLocations);

     boolean ShowLightAreas();
     void ShowLightAreas(boolean showLightAreas);

     void HideAllLights();

     // Events
     void LightingEstimateUpdated(float ambientIntensity, float ambientTemperature);
   }

}
