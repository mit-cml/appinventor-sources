package com.google.appinventor.components.runtime;

import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLU;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Module for handling OpenGL errors. */
public class GLError {
    /** Throws a {@link GLException} if a GL error occurred. */
    public static void maybeThrowGLException(String reason, String api) {
        List<Integer> errorCodes = getGlErrors();
        if (errorCodes != null) {
            throw new GLException(errorCodes.get(0), formatErrorMessage(reason, api, errorCodes));
        }
    }

    /** Logs a message with the given logcat priority if a GL error occurred. */
    public static void maybeLogGLError(int priority, String tag, String reason, String api) {
        List<Integer> errorCodes = getGlErrors();
        if (errorCodes != null) {
            Log.println(priority, tag, formatErrorMessage(reason, api, errorCodes));
        }
    }

    private static String formatErrorMessage(String reason, String api, List<Integer> errorCodes) {
        StringBuilder builder = new StringBuilder(String.format("%s: %s: ", reason, api));
        Iterator<Integer> iterator = errorCodes.iterator();
        while (iterator.hasNext()) {
            int errorCode = iterator.next();
            builder.append(String.format("%s (%d)", GLU.gluErrorString(errorCode), errorCode));
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private static List<Integer> getGlErrors() {
        int errorCode = GLES30.glGetError();
        // Shortcut for no errors
        if (errorCode == GLES30.GL_NO_ERROR) {
            return null;
        }
        List<Integer> errorCodes = new ArrayList<>();
        errorCodes.add(errorCode);
        while (true) {
            errorCode = GLES30.glGetError();
            if (errorCode == GLES30.GL_NO_ERROR) {
                break;
            }
            errorCodes.add(errorCode);
        }
        return errorCodes;
    }

    private GLError() {}
}
