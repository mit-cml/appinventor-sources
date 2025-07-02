package appinventor.components.tests.com.google.appinventor.components.runtime.ar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.appinventor.components.runtime.ar.ARNodeBase;

public class NodeTest {

    @Test
    public void testPoseFromPropertyPosition_ValidInput() {
        ARNodeBase node = new ARNodeBase();
        String validInput = "1.0,2.0,3.0,0.1,0.2,0.3,0.4";
        float[] result = node.PoseFromPropertyPosition(validInput);
        assertNotNull(result);
        assertEquals(7, result.length);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f, 0.1f, 0.2f, 0.3f, 0.4f}, result, 0.0001f);
    }

    @Test
    public void testPoseFromPropertyPosition_EmptyInput() {
        ARNodeBase node = new ARNodeBase();
        String emptyInput = "";
        float[] result = node.PoseFromPropertyPosition(emptyInput);
        assertNotNull(result);
        assertEquals(7, result.length);
        for (float v : result) {
            assertEquals(0f, v, 0.0001f);
        }
    }

    @Test
    public void testPoseFromPropertyPosition_NullInput() {
        ARNodeBase node = new ARNodeBase();
        String nullInput = null;
        float[] result = node.PoseFromPropertyPosition(nullInput);
        assertNotNull(result);
        assertEquals(7, result.length);
        for (float v : result) {
            assertEquals(0f, v, 0.0001f);
        }
    }

    @Test
    public void testPoseFromPropertyPosition_PartialInput() {
        ARNodeBase node = new ARNodeBase();
        String partialInput = "1.0,2.0";
        float[] result = node.PoseFromPropertyPosition(partialInput);
        assertNotNull(result);
        assertEquals(7, result.length);
        assertEquals(1.0f, result[0], 0.0001f);
        assertEquals(2.0f, result[1], 0.0001f);
        for (int i = 2; i < 7; i++) {
            assertEquals(0f, result[i], 0.0001f);
        }
    }

    @Test
    public void testPoseFromPropertyPosition_InvalidInput() {
        ARNodeBase node = new ARNodeBase();
        String invalidInput = "a,b,c,d,e,f,g";
        float[] result = node.PoseFromPropertyPosition(invalidInput);
        assertNotNull(result);
        assertEquals(7, result.length);
        for (float v : result) {
            assertEquals(0f, v, 0.0001f);
        }
    }
}
