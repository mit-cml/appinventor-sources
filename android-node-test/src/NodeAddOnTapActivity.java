import java.util.Scanner;

public class NodeAddOnTapActivity {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Tap the screen by typing a node name to add (e.g., CapsuleNode, CubeNode, SphereNode). Type 'exit' to quit.");

        while (true) {
            System.out.print("Enter node to add: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting.");
                break;
            }

            NodeBase node;
            switch (input) {
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
                    System.out.println("Unknown node type: " + input);
                    continue;
            }

            node.PoseFromPropertyPosition("0.0, 0.0, 0.0");
            System.out.println("Added node: " + input);
            System.out.println("Pose: " + node.PoseFromPropertyPosition()[0] + ", " + node.PoseFromPropertyPosition()[1] + ", " + node.PoseFromPropertyPosition()[2]);
            System.out.println("objectModel: " + node.objectModel);
            System.out.println("texture: " + node.texture);
            System.out.println("scale: " + node.scale);
        }

        scanner.close();
    }
}
