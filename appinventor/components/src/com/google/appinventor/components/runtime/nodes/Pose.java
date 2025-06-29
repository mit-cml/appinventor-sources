package com.google.appinventor.components.runtime.nodes;

public class Pose {
    private float[] position;
    private float[] rotation;

    public Pose(float[] position, float[] rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public float[] getPosition() {
        return position;
    }

    public float[] getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "Pose{position=[" + position[0] + ", " + position[1] + ", " + position[2] + "], rotation=[" +
                rotation[0] + ", " + rotation[1] + ", " + rotation[2] + ", " + rotation[3] + "]}";
    }
}
