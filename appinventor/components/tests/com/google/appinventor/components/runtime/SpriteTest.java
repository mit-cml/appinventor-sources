// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Handler;
import android.view.View;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests Sprite.java.
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Canvas.class, Handler.class, Form.class, View.class })
public class SpriteTest {
  static class TestSprite extends Sprite {
    private int height;
    private int width;
    protected int canvasHeight = 320;
    protected int canvasWidth = 320;

    private TestSprite(Canvas canvas, Handler handler) {
      super(canvas, handler);
    }

    public static TestSprite createTestSprite(Canvas canvas, Handler handler) {
      return new TestSprite(canvas, handler);
    }

    public static TestSprite createTestSprite(Canvas canvas, Handler handler,
                                              int h, int w) {
      TestSprite sprite = createTestSprite(canvas, handler);
      sprite.height = h;
      sprite.width = w;
      return sprite;
    }

    // Create trivial implementations of abstract methods.
    @Override
    public int Width() {
      return width;
    }

    @Override
    public void Width(int width) {
      this.width = width;
    }

    @Override
    public int Height() {
      return height;
    }

    @Override
    public void Height(int height) {
      this.height = height;
    }

    @Override
    public void onDraw(android.graphics.Canvas canvas) {
    }

    // Override hitEdge() and MoveIntoBounds() to keep them from calling
    // unavailable and unnecessary methods.
    @Override
    protected int hitEdge() {
      return hitEdge(canvasWidth, canvasHeight);
    }

    @Override
    public void MoveIntoBounds() {
      moveIntoBounds(canvasWidth, canvasHeight);
    }

    // Override low-level methods.
    // TODO(user): Test that the following methods are called appropriately.
    @Override
    protected void postEvent(Sprite sprite, String eventName, Object... args) {
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
      throw new UnsupportedOperationException();
    }

    public void WidthPercent(int wPercent) {
    }

    public void HeightPercent(int hPercent) {
    }

}

  private static final double DELTA = .0001;  // for floating-point comparisons

  private final Form formMock = PowerMock.createNiceMock(Form.class);
  private final View canvasViewMock = PowerMock.createNiceMock(View.class);
  private final Canvas canvasMock = PowerMock.createNiceMock(Canvas.class);
  private final Handler handlerMock = PowerMock.createNiceMock(Handler.class);

  @Before
  public void setUp() throws Exception {
    EasyMock.expect(canvasMock.getView()).andReturn(canvasViewMock).anyTimes();
    EasyMock.expect(canvasMock.$form()).andReturn(formMock).anyTimes();
    EasyMock.replay(canvasViewMock, canvasMock, handlerMock);
  }

  protected double normalizeRadians(double r) {
    // This isn't the most efficient general solution, but it's good enough
    // for the range we're in.
    while (r < 0) {
      r += 2 * Math.PI;
    }
    while (r >= 2 * Math.PI) {
      r -= 2 * Math.PI;
    }
    return r;
  }

  @Test
  public void testHeading() throws Exception {
    Sprite sprite = TestSprite.createTestSprite(canvasMock, handlerMock);

    final int degrees[] = { 0, 45, 90, 180, 270 };
    final double radians[] = { 0, Math.PI / 4, Math.PI / 2, Math.PI, Math.PI * 1.5 };
    final double cosine[] = { 1, 1 / Math.sqrt(2), 0, -1, 0 };
    final double sine[] = { 0, 1 / Math.sqrt(2), 1, 0, -1 };

    for (int i = 0; i < degrees.length; i++) {
      // Do a reality check on my data.
      assertEquals(degrees[i], (int) Math.toDegrees(radians[i]));
      assertEquals(cosine[i], Math.cos(radians[i]), DELTA);
      assertEquals(sine[i], Math.sin(radians[i]), DELTA);

      // Test Sprite
      sprite.Heading(-degrees[i]);  // Flip, because y increases in the downward direction
      assertEquals(-degrees[i], (int) sprite.userHeading);
      assertEquals(degrees[i], (int) sprite.heading);
      assertEquals(radians[i], normalizeRadians(sprite.headingRadians), DELTA);
      assertEquals(cosine[i], sprite.headingCos, DELTA);
      assertEquals(sine[i], sprite.headingSin, DELTA);
    }
  }

  @Test
  public void testSpriteBiggerThanCanvas() throws Exception {
    // Declare cat
    final int BIG_CAT_WIDTH = 150;
    final int BIG_CAT_HEIGHT = 150;
    TestSprite bigCat = TestSprite.createTestSprite(
        canvasMock, handlerMock, BIG_CAT_HEIGHT, BIG_CAT_WIDTH);
    bigCat.canvasWidth = BIG_CAT_WIDTH - 10;
    bigCat.canvasHeight = BIG_CAT_HEIGHT - 10;
    bigCat.MoveIntoBounds();
    // The sprite should be moved to the upper left corner of the canvas - x and y should both be 0.
    assertEquals(0.0, bigCat.X(), DELTA);
    assertEquals(0.0, bigCat.Y(), DELTA);
  }

  @Test
  public void testMoveTo() {
    // Declare sprite
    final int SPRITE_HEIGHT = 20;
    final int SPRITE_WIDTH = 30;
    TestSprite sprite = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE_HEIGHT, SPRITE_WIDTH);

    // Origin at top - left corner
    sprite.MoveTo(50, 50);
    assertEquals(50, sprite.xOrigin, DELTA);
    assertEquals(50, sprite.yOrigin, DELTA);
    assertEquals(50, sprite.xLeft, DELTA);
    assertEquals(50, sprite.yTop, DELTA);

    // Origin at top - right corner
    sprite.U(1.0);
    sprite.V(0.0);
    sprite.MoveTo(40, 40);
    assertEquals(40, sprite.xOrigin, DELTA);
    assertEquals(40, sprite.yOrigin, DELTA);
    assertEquals(10, sprite.xLeft, DELTA);
    assertEquals(40, sprite.yTop, DELTA);

    // Origin at bottom - right corner
    sprite.U(1.0);
    sprite.V(1.0);
    sprite.MoveTo(50, 50);
    assertEquals(50, sprite.xOrigin, DELTA);
    assertEquals(50, sprite.yOrigin, DELTA);
    assertEquals(20, sprite.xLeft, DELTA);
    assertEquals(30, sprite.yTop, DELTA);

    // Origin at bottom - left corner
    sprite.U(0.0);
    sprite.V(1.0);
    sprite.MoveTo(40, 40);
    assertEquals(40, sprite.xOrigin, DELTA);
    assertEquals(40, sprite.yOrigin, DELTA);
    assertEquals(40, sprite.xLeft, DELTA);
    assertEquals(20, sprite.yTop, DELTA);

    // Origin at center
    sprite.U(0.5);
    sprite.V(0.5);
    sprite.MoveTo(50, 50);
    assertEquals(50, sprite.xOrigin, DELTA);
    assertEquals(50, sprite.yOrigin, DELTA);
    assertEquals(35, sprite.xLeft, DELTA);
    assertEquals(40, sprite.yTop, DELTA);
  }

  @Test
  public void testPointTowards() {
    // Declare Sprite1 at (30, 30) with origin at (0.3, 0.7)
    final int SPRITE1_HEIGHT = 40;
    final int SPRITE1_WIDTH = 30;
    TestSprite sprite1 = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE1_HEIGHT, SPRITE1_WIDTH);
    final int SPRITE1_X = 30;
    final int SPRITE1_Y = 30;
    sprite1.MoveTo(SPRITE1_X, SPRITE1_Y);
    sprite1.U(0.3);
    sprite1.V(0.7);

    // Declare Sprite2 at (60, 60) with origin at (0.6, 0.6)
    final int SPRITE2_HEIGHT = 50;
    final int SPRITE2_WIDTH = 60;
    TestSprite sprite2 = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE2_HEIGHT, SPRITE2_WIDTH);
    final int SPRITE2_X = 60;
    final int SPRITE2_Y = 60;
    sprite2.U(0.6);
    sprite2.V(0.6);
    sprite2.MoveTo(SPRITE2_X, SPRITE2_Y);

    // Make sprite1 to point towards sprite2
    sprite1.PointTowards(sprite2);
    assertEquals(45, sprite1.heading, DELTA);
  }

  @Test
  public void testPointInDirection() {
    // Declare Sprite at (45, 45) with origin at (0.5, 0.75)
    final int SPRITE_HEIGHT = 40;
    final int SPRITE_WIDTH = 60;
    TestSprite sprite = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE_HEIGHT, SPRITE_WIDTH);
    final int SPRITE_X = 45;
    final int SPRITE_Y = 45;
    sprite.MoveTo(SPRITE_X, SPRITE_Y);
    sprite.U(0.5);
    sprite.V(0.75);

    sprite.PointInDirection(65, 65);
    assertEquals(45, sprite.heading, DELTA);
  }

  @Test
  public void testOriginAtCenterTrue() {
    // Declare sprite at (40, 40) with default origin position
    // Sprite represents a ball of radius 25
    final int SPRITE_HEIGHT = 50;
    final int SPRITE_WIDTH = 50;
    TestSprite sprite = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE_HEIGHT, SPRITE_WIDTH);
    final int SPRITE_X = 40;
    final int SPRITE_Y = 40;
    sprite.MoveTo(SPRITE_X, SPRITE_Y);

    // Check originAtCenter
    sprite.OriginAtCenter(true);
    // Assert u, v, xLeft, yTop, xOrigin and yOrigin values
    assertEquals(0.5, sprite.u, DELTA);
    assertEquals(0.5, sprite.v, DELTA);
    assertEquals(15, sprite.xLeft, DELTA);
    assertEquals(15, sprite.yTop, DELTA);
    assertEquals(40, sprite.xOrigin, DELTA);
    assertEquals(40, sprite.yOrigin, DELTA);
  }

  @Test
  public void testOriginAtCenterFalse() {
    // Declare sprite at (40, 40) with origin at center
    // Sprite represents a ball of radius 25
    final int SPRITE_HEIGHT = 50;
    final int SPRITE_WIDTH = 50;
    TestSprite sprite = TestSprite.createTestSprite(
            canvasMock, handlerMock, SPRITE_HEIGHT, SPRITE_WIDTH);
    final int SPRITE_X = 40;
    final int SPRITE_Y = 40;
    sprite.MoveTo(SPRITE_X, SPRITE_Y);
    sprite.U(0.5);
    sprite.V(0.5);

    // Uncheck originAtCenter
    sprite.OriginAtCenter(false);
    // Assert u, v, xLeft, yTop, xOrigin and yOrigin values
    assertEquals(0.0, sprite.u, DELTA);
    assertEquals(0.0, sprite.v, DELTA);
    assertEquals(40, sprite.xLeft, DELTA);
    assertEquals(40, sprite.yTop, DELTA);
    assertEquals(40, sprite.xOrigin, DELTA);
    assertEquals(40, sprite.yOrigin, DELTA);
  }
}
