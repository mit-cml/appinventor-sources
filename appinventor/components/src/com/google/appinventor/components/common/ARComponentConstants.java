// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

/**
 * Contains constants related to the persistent format of Simple components.
 *
 */
public class ARComponentConstants {
    private ARComponentConstants() {
      // nothing
    }

    public static class PhysicsSettingsObject {
      public float mass;
      public float staticFriction;
      public float dynamicFriction;
      public float restitution;
      public float gravityScale;
      public float dragSensitivity;

      public PhysicsSettingsObject(float mass, float staticFriction, float dynamicFriction, float restitution, float gravityScale, float dragSensitivity) {
        this.mass = mass;
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
        this.restitution = restitution;
        this.gravityScale = gravityScale;
        this.dragSensitivity = dragSensitivity;

      }
    }

    public static PhysicsSettingsObject getSphereDefaultSettings(String behavior) {

      PhysicsSettingsObject settings = new PhysicsSettingsObject(1.0f, 0.5f, 0.6f, 1.0f, 1.0f, 1.0f);

      // ✅ Apply behavior-specific defaults (last behavior wins if multiple)
      switch(behavior) {
        case "1":
          settings.mass = 1.0f; // Heavy default
          settings.dragSensitivity = 0.2f;  // Harder to drag
          break;
        case "2":  //light) {
          settings.mass = 0.04f;  // Light default
          settings.dragSensitivity = .8f;  // Easier to drag
          break;
        case "3": //bouncy) {
          settings.restitution = 0.9f;  // Very bouncy default
          settings.staticFriction = 0.2f;  // Low friction for bouncing
          settings.dynamicFriction = 0.12f;
          break;
        case "4": //wet) {
          // Wet ball defaults - high friction, low bounce
          settings.staticFriction = 0.8f;
          settings.dynamicFriction = 0.65f;
          settings.restitution = 0.15f;
          settings.dragSensitivity = 0.7f; // Harder to drag when wet
          break;
        case "5": //.sticky) {
          // Sticky ball defaults - extreme friction, no bounce
          settings.staticFriction = 0.95f;
          settings.dynamicFriction = 0.85f;
          settings.restitution = 0.05f;  // Almost no bounce
          settings.dragSensitivity = 0.4f;  // Very hard to drag
          break;

        case "6": //slippery
          // Slippery ball defaults - minimal friction, bouncy
          settings.staticFriction = 0.05f;
          settings.dynamicFriction = 0.02f;
          settings.restitution = 0.8f;  // Bounces well
          settings.dragSensitivity = .8f; // Easy to drag
          break;

        case "7": //floating
          // Floating defaults - light with reduced gravity
          settings.mass = 0.02f; // Very light
          settings.staticFriction = 0.1f;  // Low friction for floating
          settings. dynamicFriction = 0.06f;
          settings.dragSensitivity = .8f; // Very easy to move
          break;
        default:
          break;
      }

      return settings;
    }

  }
