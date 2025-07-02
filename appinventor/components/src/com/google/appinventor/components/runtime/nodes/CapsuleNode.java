package com.google.appinventor.components.runtime.nodes;

public class CapsuleNode extends Node {
    public CapsuleNode() {
        super();
        // Set default mesh object and texture for CapsuleNode
        setObjectModel("capsule_mesh.obj");
        setTexture("capsule_texture.png");
    }
}
