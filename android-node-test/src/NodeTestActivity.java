public class NodeTestActivity {

    public static void main(String[] args) {
        String[] nodeNames = {
            "CapsuleNode",
            "CubeNode",
            "SphereNode",
            "ModelNode",
            "AnimatedNode",
            "PlaneNode",
            "PyramidNode",
            "WebViewerNode",
            "VideoNode"
        };

        for (String nodeName : nodeNames) {
            NodeBase node;
            switch (nodeName) {
                case "CapsuleNode":
                    node = new CapsuleNode();
                    break;
                case "CubeNode":
                    node = new CubeNode();
                    break;
                case "SphereNode":
                    node = new SphereNode();
                    break;
                case "ModelNode":
                    node = new ModelNode();
                    break;
                case "AnimatedNode":
                    node = new AnimatedNode();
                    break;
                case "PlaneNode":
                    node = new PlaneNode();
                    break;
                case "PyramidNode":
                    node = new PyramidNode();
                    break;
                case "WebViewerNode":
                    node = new WebViewerNode();
                    break;
                case "VideoNode":
                    node = new VideoNode();
                    break;
                default:
                    node = new NodeBase();
                    break;
            }
            System.out.println("Adding node: " + nodeName);
            node.PoseFromPropertyPosition("1.0, 2.0, 3.0");
            float[] pos = node.PoseFromPropertyPosition();
            if (pos[0] == 1.0f && pos[1] == 2.0f && pos[2] == 3.0f &&
                node.objectModel != null && node.texture != null && node.scale == 1.0f) {
                System.out.println("Test passed for " + nodeName);
            } else {
                System.out.println("Test failed for " + nodeName);
            }
        }
    }
}
