import org.junit.Test;
import static org.junit.Assert.*;

public class NodeTestActivityTest {

    @Test
    public void testPoseFromPropertyPositionParsing() {
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
            node.PoseFromPropertyPosition("1.0, 2.0, 3.0");
            float[] pos = node.PoseFromPropertyPosition();
            assertEquals(1.0f, pos[0], 0.0001);
            assertEquals(2.0f, pos[1], 0.0001);
            assertEquals(3.0f, pos[2], 0.0001);
            assertNotNull(node.objectModel);
            assertNotNull(node.texture);
            assertEquals(1.0f, node.scale, 0.0001);
        }
    }
}
