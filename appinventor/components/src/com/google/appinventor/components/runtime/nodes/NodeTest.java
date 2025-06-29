package com.google.appinventor.components.runtime.nodes;

import com.google.appinventor.components.runtime.nodes.Node;

public class NodeTest {

    public static void main(String[] args) {
        Node node = new Node();

        // Test 1: Valid position string
        String validPosition = "1.0, 2.0, 3.0";
        node.PoseFromPropertyPosition(validPosition);
        float[] pos = node.PoseFromPropertyPosition();
        System.out.println("Test 1 - Position: " + pos[0] + ", " + pos[1] + ", " + pos[2]);

        // Test 2: Invalid position string (non-numeric)
        String invalidPosition = "a, b, c";
        node.PoseFromPropertyPosition(invalidPosition);
        pos = node.PoseFromPropertyPosition();
        System.out.println("Test 2 - Position: " + pos[0] + ", " + pos[1] + ", " + pos[2]);

        // Test 3: Partial position string
        String partialPosition = "4.5";
        node.PoseFromPropertyPosition(partialPosition);
        pos = node.PoseFromPropertyPosition();
        System.out.println("Test 3 - Position: " + pos[0] + ", " + pos[1] + ", " + pos[2]);

        // Test 4: Empty position string
        String emptyPosition = "";
        node.PoseFromPropertyPosition(emptyPosition);
        pos = node.PoseFromPropertyPosition();
        System.out.println("Test 4 - Position: " + pos[0] + ", " + pos[1] + ", " + pos[2]);

        // Test 5: Null position string
        node.PoseFromPropertyPosition(null);
        pos = node.PoseFromPropertyPosition();
        System.out.println("Test 5 - Position: " + pos[0] + ", " + pos[1] + ", " + pos[2]);
    }
}
