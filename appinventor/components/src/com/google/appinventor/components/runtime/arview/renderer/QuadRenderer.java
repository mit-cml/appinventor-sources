package com.google.appinventor.components.runtime.arview.renderer;
import com.google.appinventor.components.annotations.UsesAssets;
import android.media.Image;
import android.opengl.GLES30;
import com.google.appinventor.components.runtime.*;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.util.Log;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This class renders a composite quad.
 */
@UsesAssets(fileNames = "background_show_camera.frag, background_show_camera.vert," +
        "plane.frag, plane.vert")


public class QuadRenderer {
    private static final String LOG_TAG = QuadRenderer.class.getSimpleName();

    private int quadShaderProgram;
    /**
     * Allocates and initializes OpenGL resources needed by the background renderer. Must be called
     * during a {@link ARViewRender} callback, typically in {@link
     */
    public QuadRenderer()  {
        // Create shader for the quad if needed
        if (quadShaderProgram == 0) {
            quadShaderProgram = createQuadShader();
        }

    }


    public void drawTexturedQuad(int textureId) {
                if (textureId <= 0) return;
                GLES30.glFinish();
                Log.e(LOG_TAG, "drawing textured quad");


                // Use the shader
                GLES30.glUseProgram(quadShaderProgram);


                // Set up vertices for a fullscreen quad (using normalized device coordinates)
                float[] quadVertices = {
                        -1.0f, -1.0f,  // bottom-left
                        1.0f, -1.0f,   // bottom-right
                        -1.0f, 1.0f,   // top-left
                        1.0f, 1.0f     // top-right
                };


                // Texture coordinates - try this specific orientation
                float[] texCoords = {
                        0.0f, 1.0f,  // bottom-left
                        1.0f, 1.0f,  // bottom-right
                        0.0f, 0.0f,  // top-left
                        1.0f, 0.0f   // top-right
                };
                // Set up the vertex arrays
                int posHandle = GLES30.glGetAttribLocation(quadShaderProgram, "a_position");
                int texHandle = GLES30.glGetAttribLocation(quadShaderProgram, "a_texCoord");

                // Load vertices
                FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(quadVertices.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                vertexBuffer.put(quadVertices).position(0);

                FloatBuffer texBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                texBuffer.put(texCoords).position(0);

                // Set up the texture
                int texUniform = GLES30.glGetUniformLocation(quadShaderProgram, "u_texture");


        // Check texture binding and state
        int[] boundTexture = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_TEXTURE_BINDING_2D, boundTexture, 0);
        Log.d(LOG_TAG, "Currently bound texture: " + boundTexture[0]);

        // Check for OpenGL errors before rendering
        int errorBefore = GLES30.glGetError();
        if (errorBefore != GLES30.GL_NO_ERROR) {
            Log.e(LOG_TAG, "OpenGL error before rendering: 0x" +
                    Integer.toHexString(errorBefore));
        }


                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
                GLES30.glUniform1i(texUniform, 0);
// In your drawTexturedQuad method, before any drawing

                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
                // Set up blending for transparency
                GLES30.glEnable(GLES30.GL_BLEND);
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

                // Draw the quad
                GLES30.glEnableVertexAttribArray(posHandle);
                GLES30.glVertexAttribPointer(posHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer);

                GLES30.glEnableVertexAttribArray(texHandle);
                GLES30.glVertexAttribPointer(texHandle, 2, GLES30.GL_FLOAT, false, 0, texBuffer);

                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);


        // Check for OpenGL errors after rendering
        int errorAfter = GLES30.glGetError();
        if (errorAfter != GLES30.GL_NO_ERROR) {
            Log.e(LOG_TAG, "OpenGL error after rendering: 0x" +
                    Integer.toHexString(errorAfter));
        }

                // Clean up
                GLES30.glDisableVertexAttribArray(posHandle);
                GLES30.glDisableVertexAttribArray(texHandle);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
                GLES30.glDisable(GLES30.GL_BLEND);
                GLES30.glUseProgram(0);
            }

            // Create a simple shader for the quad
            private int createQuadShader() {
                // Simple vertex shader
                String vertexShaderCode =
                        "attribute vec2 a_position;\n" +
                                "attribute vec2 a_texCoord;\n" +
                                "varying vec2 v_texCoord;\n" +
                                "void main() {\n" +
                                "  gl_Position = vec4(a_position, 0.0, 1.0);\n" +
                                "  v_texCoord = a_texCoord;\n" +
                                "}";
                //blend over
                String fragmentShaderCode =
                        "precision highp float;\n" +
                                "varying vec2 v_texCoord;\n" +
                                "uniform sampler2D u_texture;\n" +
                                "uniform float u_opacity;\n" +
                                "void main() {\n" +
                                "  // Read texture and expand the sampling area slightly\n" +
                                "  vec4 color = texture2D(u_texture, v_texCoord);\n" +
                                "  color.a = .5;\n" +
                                "  // Ensure we're properly handling the SRGB data\n" +
                                "  gl_FragColor = color;\n" +
                                "}";
                // Simple colored fragment shader
 /*       String fragmentShaderCode =
                "precision mediump float;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" + // Solid red
                        "}";

  */
                // Compile shaders
                int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
                int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

                // Create program
                int program = GLES30.glCreateProgram();
                GLES30.glAttachShader(program, vertexShader);
                GLES30.glAttachShader(program, fragmentShader);
                GLES30.glLinkProgram(program);
                Log.e(LOG_TAG, "SUCCeSS drawing textured quad");
                return program;
            }

        // Shader compilation utility
        private int compileShader(int type, String shaderCode) {
            int shader = GLES30.glCreateShader(type);
            GLES30.glShaderSource(shader, shaderCode);
            GLES30.glCompileShader(shader);

            // Check for compilation errors
            int[] compiled = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(LOG_TAG, "Shader compilation error: " + GLES30.glGetShaderInfoLog(shader));
                GLES30.glDeleteShader(shader);
                return 0;
            }
            return shader;
        }




}

