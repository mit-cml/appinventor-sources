

public class NodeBase {
    protected float[] fromPropertyPosition = {0f, 0f, 0f};
    protected Object anchor = null;
    protected Object trackable = null;

    protected String objectModel = "Form.ASSETS_PREFIX + default_model";
    protected String texture = "Form.ASSETS_PREFIX + default_texture";
    protected float scale = 1.0f;

    public float[] PoseFromPropertyPosition() {
        return fromPropertyPosition;
    }

    public void PoseFromPropertyPosition(String positionFromProperty) {
        String[] positionArray = positionFromProperty.split(",");
        float[] position = {0f, 0f, 0f};
        for (int i = 0; i < positionArray.length && i < 3; i++) {
            try {
                position[i] = Float.parseFloat(positionArray[i].trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid float in positionFromProperty: " + positionArray[i]);
            }
        }
        this.fromPropertyPosition = position;
        // Stub: no rotation or anchor creation
        System.out.println("PoseFromPropertyPosition set to " + positionFromProperty);
    }
}
