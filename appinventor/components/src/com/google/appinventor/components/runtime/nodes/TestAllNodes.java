package com.google.appinventor.components.runtime.nodes;

public class TestAllNodes {
    public static void main(String[] args) {
        Node[] nodes = new Node[] {
            new CapsuleNode(),
            new CubeNode(),
            new SphereNode(),
            new ModelNode(),
            new AnimatedNode(),
            new PlaneNode(),
            new PyramidNode(),
            new WebViewerNode(),
            new VideoNode()
        };

        for (Node node : nodes) {
            System.out.println("Testing " + node.getClass().getSimpleName());

            // Test default values
            float[] pos = node.PoseFromPropertyPosition();
            System.out.print("Default fromPropertyPosition: ");
            for (float f : pos) {
                System.out.print(f + " ");
            }
            System.out.println();

            System.out.println("Default objectModel: " + node.getObjectModel());
            System.out.println("Default texture: " + node.getTexture());
            System.out.println("Default scale: " + node.getScale());

            // Test setting pose from string
            node.PoseFromPropertyPosition("1.0,2.0,3.0");
            pos = node.PoseFromPropertyPosition();
            System.out.print("Updated fromPropertyPosition: ");
            for (float f : pos) {
                System.out.print(f + " ");
            }
            System.out.println();

            // Test setting objectModel, texture, scale
            node.setObjectModel("new_mesh.obj");
            node.setTexture("new_texture.png");
            node.setScale(2.5f);

            System.out.println("Updated objectModel: " + node.getObjectModel());
            System.out.println("Updated texture: " + node.getTexture());
            System.out.println("Updated scale: " + node.getScale());

            System.out.println();
        }
    }
}
