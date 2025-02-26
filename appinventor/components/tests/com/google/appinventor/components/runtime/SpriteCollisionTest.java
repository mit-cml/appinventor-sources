// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for collisions of sprites.
 *
 */
public class SpriteCollisionTest extends RobolectricTestBase {

  private Ball ball1;
  private Ball ball2;
  private ImageSprite imageSprite1;
  private ImageSprite imageSprite2;
  private ImageSprite imageSpriteRotated1;
  private ImageSprite imageSpriteRotated2;

  @Before
  public void setUp() {
    super.setUp();
    Canvas canvas = new Canvas(getForm());

    ball1 = new Ball(canvas);
    ball2 = new Ball(canvas);

    imageSprite1 = new ImageSprite(canvas);
    imageSprite2 = new ImageSprite(canvas);

    imageSpriteRotated1 = setRotatedImageSprite(canvas, 37);
    imageSpriteRotated2 = setRotatedImageSprite(canvas, -53);
  }

  private ImageSprite setRotatedImageSprite(Canvas canvas, double angle) {
    ImageSprite sprite = new ImageSprite(canvas);
    sprite.Rotates(true);
    sprite.Heading(angle);
    return sprite;
  }

  @Test
  public void testImageSpriteToImageSpriteTouching() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    imageSprite2.X(60);
    imageSprite2.Y(40);
    imageSprite2.U(0.0);
    imageSprite2.V(0.5);
    imageSprite2.Width(60);
    imageSprite2.Height(40);

    assertTrue(Sprite.colliding(imageSprite1, imageSprite2));
  }

  @Test
  public void testImageSpriteToImageSpriteColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(60);
    imageSprite1.Height(30);

    imageSprite2.X(60);
    imageSprite2.Y(40);
    imageSprite2.U(0.3);
    imageSprite2.V(0.5);
    imageSprite2.Width(60);
    imageSprite2.Height(40);

    assertTrue(Sprite.colliding(imageSprite1, imageSprite2));
  }

  @Test
  public void testImageSpriteToImageSpriteNotColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(10);
    imageSprite1.Height(30);

    imageSprite2.X(80);
    imageSprite2.Y(40);
    imageSprite2.U(0.5);
    imageSprite2.V(0.5);
    imageSprite2.Width(60);
    imageSprite2.Height(40);

    assertFalse(Sprite.colliding(imageSprite1, imageSprite2));
  }

  @Test
  public void testImageSpriteToRotatedImageSpriteTouching() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    // construct the image sprite such that it touches the rotated one
    int width = 40;
    int height = 50;
    imageSprite1.X(rightBottomCornerX + width * 0.3);
    imageSprite1.Y(rightBottomCornerY + height * 0.4);
    imageSprite1.U(0.3);
    imageSprite1.V(0.4);
    imageSprite1.Width(width);
    imageSprite1.Height(height);

    assertTrue(Sprite.colliding(imageSprite1, imageSpriteRotated1));
  }

  @Test
  public void testImageSpriteToRotatedImageSpriteColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    imageSpriteRotated1.X(55);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(0.3);
    imageSpriteRotated1.V(0.3);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(40);

    assertTrue(Sprite.colliding(imageSprite1, imageSpriteRotated1));
  }

  @Test
  public void testImageSpriteToRotatedImageSpriteNotColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(0.3);
    imageSpriteRotated1.V(0.3);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(40);

    assertFalse(Sprite.colliding(imageSprite1, imageSpriteRotated1));
  }

  @Test
  public void testImageSpriteToBallTouching() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    ball1.X(40);
    ball1.Y(40);
    ball1.Radius(40);
    ball1.OriginAtCenter(true);

    assertTrue(Sprite.colliding(imageSprite1, ball1));
  }

  @Test
  public void testImageSpriteToBallColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    ball1.X(40);
    ball1.Y(40);
    ball1.Radius(60);
    ball1.OriginAtCenter(true);

    assertTrue(Sprite.colliding(imageSprite1, ball1));
  }

  @Test
  public void testImageSpriteToBallNotColliding() {
    imageSprite1.X(40);
    imageSprite1.Y(40);
    imageSprite1.U(0.5);
    imageSprite1.V(0.5);
    imageSprite1.Width(40);
    imageSprite1.Height(30);

    ball1.X(80);
    ball1.Y(40);
    ball1.Radius(20);
    ball1.OriginAtCenter(true);

    assertTrue(Sprite.colliding(imageSprite1, ball1));
  }

  @Test
  public void testRotatedImageSpriteToRotatedImageSpriteTouching() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    // Let (0.5, 0) be the unit coordinates of the origin of sprite2. Make the top left corner of
    // the sprite 2 to touch the bottom right corner of sprite 1.

    int width = 30;
    int height = 45;

    // After rotation the origin would be (width * 0.5) * cos(theta) to the right of top left corner
    // as the origin is at the center of the top edge
    double dx = (width * 0.5) * (3.0 / 5);
    // After rotation the origin would be (width * 0.5) * sin(theta) down of the top left corner as
    // the origin is at the center of the top edge
    double dy = (width * 0.5) * (4.0 / 5);

    imageSpriteRotated2.X(rightBottomCornerX + dx);
    imageSpriteRotated2.Y(rightBottomCornerY + dy);
    imageSpriteRotated2.U(0.5);
    imageSpriteRotated2.V(0.0);
    imageSpriteRotated2.Width(width);
    imageSpriteRotated2.Height(height);

    assertTrue(Sprite.colliding(imageSpriteRotated2, imageSpriteRotated1));
  }

  @Test
  public void testRotatedImageSpriteToRotatedImageSpriteColliding() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    imageSpriteRotated2.X(rightBottomCornerX + 5);
    imageSpriteRotated2.Y(rightBottomCornerY + 5);
    imageSpriteRotated2.U(0.8);
    imageSpriteRotated2.V(0.8);
    imageSpriteRotated2.Width(40);
    imageSpriteRotated2.Height(50);

    assertTrue(Sprite.colliding(imageSpriteRotated2, imageSpriteRotated1));
  }

  @Test
  public void testRotatedImageSpriteToRotatedImageSpriteNotColliding() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    imageSpriteRotated2.X(rightBottomCornerX + 30);
    imageSpriteRotated2.Y(rightBottomCornerY + 30);
    imageSpriteRotated2.U(0.0);
    imageSpriteRotated2.V(0.0);
    imageSpriteRotated2.Width(20);
    imageSpriteRotated2.Height(20);

    assertFalse(Sprite.colliding(imageSpriteRotated2, imageSpriteRotated1));
  }

  @Test
  public void testRotatedImageSpriteToBallTouching() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    // construct the ball such that it touches the right bottom corner of image sprite
    int radius = 25;
    ball1.X(rightBottomCornerX + radius);
    ball1.Y(rightBottomCornerY);
    ball1.Radius(radius);
    ball1.OriginAtCenter(true);

    assertTrue(Sprite.colliding(imageSpriteRotated1, ball1));
  }

  @Test
  public void testRotatedImageSpriteToBallColliding() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;

    // construct the ball such that it collides with the image sprite
    ball1.X(rightBottomCornerX + 20);
    ball1.Y(rightBottomCornerY);
    ball1.Radius(30);
    ball1.OriginAtCenter(true);

    assertTrue(Sprite.colliding(imageSpriteRotated1, ball1));
  }


  @Test
  public void testRotatedImageSpriteToBallNotColliding() {
    imageSpriteRotated1.X(80);
    imageSpriteRotated1.Y(40);
    imageSpriteRotated1.U(1.0);
    imageSpriteRotated1.V(0.5);
    imageSpriteRotated1.Width(40);
    imageSpriteRotated1.Height(60);

    // After rotation the bottom right corner's x coordinate will be (height / 2) * sin(theta) to
    // right of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerX = 80 + (60 * 0.5) * (3.0 / 5);  // as sin 37 = 3 / 5;
    // After rotation the bottom right corner's y coordinate will be (height / 2) * cos(theta) down
    // of the origin. This is because the origin is in the middle of the right edge.
    double rightBottomCornerY = 40 + (60 * 0.5) * (4.0 / 5);  // as cos 37 = 4 / 5;


    // construct the ball such that it does not collide with the image sprite
    ball1.X(rightBottomCornerX + 30);
    ball1.Y(rightBottomCornerY + 30);
    ball1.Radius(20);
    ball1.OriginAtCenter(true);

    assertFalse(Sprite.colliding(imageSpriteRotated1, ball1));
  }

  @Test
  public void testBallToBallTouching() {
    ball1.X(80);
    ball1.Y(40);
    ball1.Radius(20);
    ball1.OriginAtCenter(true);

    ball2.X(40);
    ball2.Y(30);
    ball2.Radius(10);
    ball2.OriginAtCenter(false);

    assertTrue(Sprite.colliding(ball2, ball1));
  }

  @Test
  public void testBallToBallColliding() {
    ball1.X(65);
    ball1.Y(70);
    ball1.Radius(20);
    ball1.OriginAtCenter(true);

    ball2.X(35);
    ball2.Y(35);
    ball2.Radius(15);
    ball2.OriginAtCenter(false);

    assertTrue(Sprite.colliding(ball2, ball1));
  }

  @Test
  public void testBallToBallNotColliding() {
    ball1.X(80);
    ball1.Y(40);
    ball1.Radius(20);
    ball1.OriginAtCenter(true);

    ball2.X(10);
    ball1.Y(80);
    ball1.Radius(10);
    ball1.OriginAtCenter(false);

    assertFalse(Sprite.colliding(ball2, ball1));
  }
}
