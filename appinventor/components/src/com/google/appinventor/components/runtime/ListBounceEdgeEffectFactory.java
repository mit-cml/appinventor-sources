// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Canvas;
import android.widget.EdgeEffect;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ListBounceEdgeEffectFactory extends RecyclerView.EdgeEffectFactory {

  private static final float OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f;
  private static final float FLING_TRANSLATION_MAGNITUDE = 0.5f;

  @Override
  public EdgeEffect createEdgeEffect(RecyclerView recyclerView, int direction) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    boolean isHorizontal = (layoutManager != null && layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL);
    return new BounceEdgeEffect(recyclerView, direction, isHorizontal);
  }

  private static class BounceEdgeEffect extends EdgeEffect {
    private SpringAnimation translationAnim;
    private final RecyclerView recyclerView;
    private final int direction;
    private final boolean isHorizontal;

    public BounceEdgeEffect(RecyclerView recyclerView, int direction, boolean isHorizontal) {
      super(recyclerView.getContext());
      this.recyclerView = recyclerView;
      this.direction = direction;
      this.isHorizontal = isHorizontal;
    }

    @Override
    public void onPull(float deltaDistance) {
      super.onPull(deltaDistance);
      handlePull(deltaDistance);
    }

    @Override
    public void onPull(float deltaDistance, float displacement) {
      super.onPull(deltaDistance, displacement);
      handlePull(deltaDistance);
    }

    private void handlePull(float deltaDistance) {
      int sign = (direction == DIRECTION_BOTTOM || (isHorizontal && direction == DIRECTION_RIGHT)) ? -1 : 1;
      float translationDelta = sign * recyclerView.getWidth() * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE;
      translateRecyclerView(translationDelta);
      if (translationAnim != null) {
        translationAnim.cancel();
      }
    }

    @Override
    public void onRelease() {
      super.onRelease();
      if (getTranslation() != 0f) {
      translationAnim = createAnim();
        if (translationAnim != null) {
          translationAnim.start();
        }
      }
    }

    @Override
    public void onAbsorb(int velocity) {
      super.onAbsorb(velocity);
      int sign = (direction == DIRECTION_BOTTOM || (isHorizontal && direction == DIRECTION_RIGHT)) ? -1 : 1;
      float translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE;
      if (translationAnim != null) {
        translationAnim.cancel();
      }
      translationAnim = createAnim();
      if (translationAnim != null) {
        translationAnim.setStartVelocity(translationVelocity);
        translationAnim.start();
      }
    }

    @Override
    public boolean draw(Canvas canvas) {
      return false;
    }

    @Override
    public boolean isFinished() {
      return (translationAnim == null || !translationAnim.isRunning());
    }

    private SpringAnimation createAnim() {
      return new SpringAnimation(recyclerView, (isHorizontal ? SpringAnimation.TRANSLATION_X : SpringAnimation.TRANSLATION_Y))
                  .setSpring(new SpringForce()
                  .setFinalPosition(0f)
                  .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                  .setStiffness(SpringForce.STIFFNESS_LOW)
      );
    }

    private float getTranslation() {
      return isHorizontal ? recyclerView.getTranslationX() : recyclerView.getTranslationY();
    }

    private void translateRecyclerView(float translationDelta) {
      if (isHorizontal) {
        recyclerView.setTranslationX(getTranslation() + translationDelta);
      } else {
        recyclerView.setTranslationY(getTranslation() + translationDelta);
      }
    }
  }
}
