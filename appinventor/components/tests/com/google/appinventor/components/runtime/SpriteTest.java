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
  public void testCollidingFalse() {
    // Declare sprite1 at (50, 75)
    final int SPRITE1_HEIGHT = 10;
    final int SPRITE1_WIDTH = 15;
    TestSprite sprite1 = TestSprite.createTestSprite(
        canvasMock, handlerMock,
        SPRITE1_HEIGHT, SPRITE1_WIDTH);
    final int SPRITE1_X = 50;
    final int SPRITE1_Y = 75;
    sprite1.MoveTo(SPRITE1_X, SPRITE1_Y);

    // Declare sprite2
    final int SPRITE2_HEIGHT = 20;
    final int SPRITE2_WIDTH = 25;
    TestSprite sprite2 = TestSprite.createTestSprite(
        canvasMock, handlerMock,
        SPRITE2_HEIGHT, SPRITE2_WIDTH);

    // Sprite2 entirely E of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH, SPRITE1_Y);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely SE of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH, SPRITE1_Y + SPRITE1_HEIGHT);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely S of Sprite1
    sprite2.MoveTo(SPRITE1_X, SPRITE1_Y + SPRITE1_HEIGHT);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely SW of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH, SPRITE1_Y + SPRITE1_HEIGHT);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely W of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH, SPRITE1_Y);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely NW of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH, SPRITE1_Y - SPRITE2_HEIGHT);
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely N of Sprite1
    sprite2.MoveTo(SPRITE1_X, SPRITE1_Y - SPRITE2_HEIGHT - 5);  // fudge factor
    assertFalse(Sprite.colliding(sprite1, sprite2));

    // Sprite2 entirely NE of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH, SPRITE1_Y - SPRITE2_HEIGHT);
    assertFalse(Sprite.colliding(sprite1, sprite2));
  }

  @Test
  public void testCollidingTrue() {
    // Declare sprite1 at (50, 75)
    final int SPRITE1_HEIGHT = 10;
    final int SPRITE1_WIDTH = 15;
    TestSprite sprite1 = TestSprite.createTestSprite(
        canvasMock, handlerMock,
        SPRITE1_HEIGHT, SPRITE1_WIDTH);
    final int SPRITE1_X = 50;
    final int SPRITE1_Y = 75;
    sprite1.MoveTo(SPRITE1_X, SPRITE1_Y);

    // Declare sprite2
    final int SPRITE2_HEIGHT = 20;
    final int SPRITE2_WIDTH = 25;
    TestSprite sprite2 = TestSprite.createTestSprite(
        canvasMock, handlerMock,
        SPRITE2_HEIGHT, SPRITE2_WIDTH);

    // Sprite2 almost entirely E of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH - 1, SPRITE1_Y);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely SE of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH - 1, SPRITE1_Y + SPRITE1_HEIGHT - 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely S of Sprite1
    sprite2.MoveTo(SPRITE1_X, SPRITE1_Y + SPRITE1_HEIGHT - 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely SW of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH + 1, SPRITE1_Y + SPRITE1_HEIGHT - 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely W of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH + 1, SPRITE1_Y);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely NW of Sprite1
    sprite2.MoveTo(SPRITE1_X - SPRITE2_WIDTH + 1, SPRITE1_Y - SPRITE2_HEIGHT + 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely N of Sprite1
    sprite2.MoveTo(SPRITE1_X, SPRITE1_Y - SPRITE2_HEIGHT + 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));

    // Sprite2 almost entirely NE of Sprite1
    sprite2.MoveTo(SPRITE1_X + SPRITE1_WIDTH - 1, SPRITE1_Y - SPRITE2_HEIGHT + 1);
    assertTrue(Sprite.colliding(sprite1, sprite2));
  }

  @Test
  public void testCollidingCatMouse() {
    // Declare cat
    final int CAT_HEIGHT = 49;
    final int CAT_WIDTH = 43;
    TestSprite cat = TestSprite.createTestSprite(
        canvasMock, handlerMock, CAT_HEIGHT, CAT_WIDTH);
    cat.MoveTo(95, 83);

    // Declare mouse
    final int MOUSE_HEIGHT = 31;
    final int MOUSE_WIDTH = 64;
    TestSprite mouse = TestSprite.createTestSprite(
        canvasMock, handlerMock, MOUSE_HEIGHT, MOUSE_WIDTH);
    mouse.MoveTo(98, 86);

    assertTrue(Sprite.colliding(cat, mouse));
    assertTrue(Sprite.colliding(mouse, cat));
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
}
