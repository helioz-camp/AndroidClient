package com.helioz.androiddrumclient;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by john on 8/25/16.
 */
public class DrumGL {
    static int mProgram;
    private static final String TAG = DrumGL.class.getSimpleName();

    private static final float TIME_TO_SCALE_FACTOR = 0.01f;

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
        }
    }

    private final static String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform float scale;" +
                    "uniform vec2 chosenPoint;" +
                    "uniform vec2 resolution;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "vec2 uv = gl_FragCoord.xy / resolution.xx - vec2(.5,0);" +
                    "vec2 displacement = chosenPoint - gl_FragCoord.xy;" +
    "  gl_FragColor = mix(vec4(0.), vColor, cos(pow(length(displacement)/length(resolution)*scale,1.414214)));" +
                    "}";

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkGLError("glShaderSource");
        GLES20.glCompileShader(shader);
        checkGLError("glCompileShader");

        return shader;
    }

    static void setup() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        checkGLError("glAttachShader vertex");

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        checkGLError("glAttachShader fragment");

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
        checkGLError("glLinkProgram");

        setupTriangle();

    }
    private static int mPositionHandle;
    private static int mColorHandle;
    private static int chosenPointHandle;
    private static int resolutionHandle;
    private static int scaleHandle;
    private static FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float coords[] = {   // in counterclockwise order:
            -1, -1, 0,
            1,-1, 0,
            -1, 1, 0,

            -1, 1, 0,
            1, -1, 0,
            1, 1, 0
    };

    // Set color with red, green, blue and alpha (opacity) values
    static float color[] = { 0.98f, 0.61f, 0.78f, 1.0f };

    static private void setupTriangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                coords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(coords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    private final static int vertexCount = coords.length / COORDS_PER_VERTEX;
    private final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public static void draw(final DrumView view) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        checkGLError("glUseProgram");

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        chosenPointHandle = GLES20.glGetUniformLocation(mProgram, "chosenPoint");
        resolutionHandle = GLES20.glGetUniformLocation(mProgram, "resolution");
        scaleHandle = GLES20.glGetUniformLocation(mProgram, "scale");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);



        GLES20.glUniform2fv(chosenPointHandle, 1, new float[] {view.touchX,view.getHeight()*1.f-view.touchY}, 0);
        GLES20.glUniform2fv(resolutionHandle, 1, new float[] {view.getWidth()*1.f,view.getHeight()*1.f}, 0);
        GLES20.glUniform1f(scaleHandle, (System.currentTimeMillis() - view.touchStartMillis)* TIME_TO_SCALE_FACTOR);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
