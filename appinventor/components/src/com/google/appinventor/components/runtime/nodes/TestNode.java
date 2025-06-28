package com.google.appinventor.components.runtime.nodes;

import com.google.appinventor.components.runtime.nodes.CapsuleNode;

public class TestNode {
    public static void main(String[] args) {
        CapsuleNode capsuleNode = new CapsuleNode();

        // Test default values
        System.out.println("Default fromPropertyPosition: ");
        float[] pos = capsuleNode.PoseFromPropertyPosition();
        for (float f : pos) {
            System.out.print(f + " ");
        }
        System.out.println();

        System.out.println("Default objectModel: " + capsuleNode.getObjectModel());
        System.out.println("Default texture: " + capsuleNode.getTexture());
        System.out.println("Default scale: " + capsuleNode.getScale());

        // Test setting pose from string
        capsuleNode.PoseFromPropertyPosition("1.0,2.0,3.0");
        pos = capsuleNode.PoseFromPropertyPosition();
        System.out.println("Updated fromPropertyPosition: ");
        for (float f : pos) {
            System.out.print(f + " ");
        }
        System.out.println();

        // Test setting objectModel, texture, scale
        capsuleNode.setObjectModel("new_mesh.obj");
        capsuleNode.setTexture("new_texture.png");
        capsuleNode.setScale(2.5f);

        System.out.println("Updated objectModel: " + capsuleNode.getObjectModel());
        System.out.println("Updated texture: " + capsuleNode.getTexture());
        System.out.println("Updated scale: " + capsuleNode.getScale());
    }
}
