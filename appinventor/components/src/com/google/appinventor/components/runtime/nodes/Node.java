package com.google.appinventor.components.runtime.nodes;

import com.google.appinventor.components.runtime.nodes.Anchor;
import com.google.appinventor.components.runtime.nodes.Trackable;
import com.google.appinventor.components.runtime.nodes.Pose;

public class Node {
    private float[] fromPropertyPosition = {0f, 0f, 0f};
    private Anchor anchor = null;
    private Trackable trackable = null;

    private String objectModel = "default_mesh.obj";
    private String texture = "default_texture.png"; // default
    private float scale = 1.0f;

    public Node() {
    }

    public float[] PoseFromPropertyPosition() {
        return fromPropertyPosition;
    }

    public void PoseFromPropertyPosition(String positionFromProperty) {
        if (positionFromProperty == null || positionFromProperty.trim().isEmpty()) {
            // Log.i("Node", "Empty or null position string received.");
            return;
        }
        String[] positionArray = positionFromProperty.split(",");
        float[] position = {0f, 0f, 0f};

        for (int i = 0; i < 3; i++) {
            if (i < positionArray.length) {
                try {
                    position[i] = Float.parseFloat(positionArray[i].trim());
                } catch (NumberFormatException e) {
                    // Log.e("Node", "Invalid float value in position string: " + positionArray[i], e);
                    position[i] = 0f;
                }
            } else {
                position[i] = 0f;
            }
        }
        this.fromPropertyPosition = position;
        float[] rotation = {0f, 0f, 0f, 1f}; // no rotation rn TBD
        if (this.trackable != null) {
            Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
            setAnchor(myAnchor);
        }
        // Log.i("Node", "Pose set with position: " + positionFromProperty);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public Trackable getTrackable() {
        return trackable;
    }

    public void setTrackable(Trackable trackable) {
        this.trackable = trackable;
    }

    public String getObjectModel() {
        return objectModel;
    }

    public void setObjectModel(String objectModel) {
        this.objectModel = objectModel;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
