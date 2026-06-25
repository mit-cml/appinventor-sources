package com.google.appinventor.components.runtime.arview.renderer;
import com.google.appinventor.components.annotations.UsesAssets;
import android.opengl.Matrix;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.ar.*;

import java.io.IOException;

import com.google.ar.core.PointCloud;

/** Renders a point cloud. */
@UsesAssets(fileNames = "point_cloud.frag, point_cloud.vert" )
public class PointCloudRenderer {
    private static final String TAG = PointCloud.class.getSimpleName();
    private VertexBuffer pointCloudVertexBuffer;
    private Mesh mesh;
    private Shader shader;
    private long lastPointCloudTimestamp = 0;
    // Shader names.
    private static final String VERTEX_SHADER_NAME = Form.ASSETS_PREFIX + "point_cloud.vert";
    private static final String FRAGMENT_SHADER_NAME = Form.ASSETS_PREFIX + "point_cloud.frag";



    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];


    // Keep track of the last point cloud rendered to avoid updating the VBO if point cloud
    // was not changed.  Do this using the timestamp since we can't compare PointCloud objects.
    private long lastTimestamp = 0;



    public PointCloudRenderer(ARViewRender render) throws IOException {
        shader = Shader.createFromAssets(render,
                VERTEX_SHADER_NAME, FRAGMENT_SHADER_NAME,
                /*defines=*/ null).setVec4("u_Color", new float[]{31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f}).setFloat("u_PointSize", 5.0f);
        pointCloudVertexBuffer = new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 4, /*entries=*/ null);
        final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
        mesh = new Mesh(render, Mesh.PrimitiveMode.POINTS, /*indexBuffer=*/ null, pointCloudVertexBuffers);

    }



    /**
     * Renders the point cloud. ARCore point cloud is given in world space.
     *
     * @param cameraView the camera view matrix for this frame, typically from {@link
     *     com.google.ar.core.Camera#getViewMatrix(float[], int)}.
     * @param cameraPerspective the camera projection matrix for this frame, typically from {@link
     *     com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)}.
     */
    public void draw(ARViewRender render, PointCloud pc, float[] cameraView, float[] cameraPerspective) {
///frame.acquirePointCloud()
        try (PointCloud pointCloud = pc) {
            if (pointCloud.getTimestamp() > lastPointCloudTimestamp) {
                pointCloudVertexBuffer.set(pointCloud.getPoints());
                lastPointCloudTimestamp = pointCloud.getTimestamp();
            }
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraPerspective, 0, cameraView, 0);
            shader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
            render.draw(mesh, shader);

        }
    }
}