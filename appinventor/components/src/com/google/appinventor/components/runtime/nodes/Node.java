
package com.google.appinventor.components.runtime.nodes;

public class Node {
    private float[] fromPropertyPosition = {0f, 0f, 0f};
    // Anchor and Trackable removed due to missing dependencies
    // private Anchor anchor = null;
    // private Trackable trackable = null;

    private String objectModel = "default_mesh.obj";
    private String texture = "default_texture.png"; // default
    private float scale = 1.0f;

    public Node() {
    }

    public float[] PoseFromPropertyPosition() {
        return fromPropertyPosition;
    }

    public void PoseFromPropertyPosition(String positionFromProperty) {
        String[] positionArray = positionFromProperty.split(",");
        float[] position = {0f, 0f, 0f};

        for (int i = 0; i < positionArray.length && i < 3; i++) {
            position[i] = Float.parseFloat(positionArray[i].trim());
        }
        this.fromPropertyPosition = position;
        // Anchor creation removed due to missing dependencies
        // float[] rotation = {0f, 0f, 0f, 1f}; // no rotation rn TBD
        // if (this.trackable != null) {
        //     Anchor myAnchor = this.trackable.createAnchor(new Pose(position, rotation));
        //     setAnchor(myAnchor);
        // }
        // Log.i("Node", "Pose set with position: " + positionFromProperty);
    }

    // Anchor and Trackable getters/setters removed due to missing dependencies

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
