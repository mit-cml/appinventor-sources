package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.appinventor.components.runtime.ARView3D;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

public class ARFilamentActivity extends Activity {
    private Session arSession;
    private GLSurfaceView surfaceView;
    //private ARView3D renderer;
    private boolean installRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the GL surface view for rendering
        surfaceView = new GLSurfaceView(this);
        setContentView(surfaceView);

        // Set up the renderer
       // renderer = new ARView3D();
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(3);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //surfaceView.setRenderer(renderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ARCore requires camera permission
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }

        // Make sure ARCore is installed and up to date
        try {
            if (arSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // Create ARCore session
                arSession = new Session(this);

                // Configure the session
                Config config = new Config(arSession);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);

                // Configure light estimation if needed
                config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);

                arSession.configure(config);

                // Set the session in the renderer
                //renderer.setSession(arSession);
            }

            // Resume ARCore session
            arSession.resume();

        } catch (UnavailableArcoreNotInstalledException |
                 UnavailableApkTooOldException |
                 UnavailableSdkTooOldException |
                 UnavailableUserDeclinedInstallationException |
                 UnavailableDeviceNotCompatibleException e) {
            Toast.makeText(this, "ARCore not available: " + e, Toast.LENGTH_LONG).show();
            finish();
            return;
        } catch (CameraNotAvailableException e) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        surfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (arSession != null) {
            surfaceView.onPause();
            arSession.pause();
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        if (renderer != null) {
//            renderer.destroy();
//        }
//
//        if (arSession != null) {
//            arSession.close();
//            arSession = null;
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (arSession == null) {
            return false;
        }

//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            try {
//                Frame frame = arSession.update();
//
//                // Perform hit test against detected planes
//                for (HitResult hit : frame.hitTest(event.getX(), event.getY())) {
//                    if (hit.getTrackable() instanceof Plane &&
//                            ((Plane) hit.getTrackable()).isPoseInPolygon(hit.getHitPose())) {
//                        // Place the model at the hit location
//                        renderer.placeModel(hit.getHitPose());
//                        return true;
//                    }
//                }
//
//            } catch (CameraNotAvailableException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }

        return super.onTouchEvent(event);
    }

    // Helper class for camera permissions
    private static class CameraPermissionHelper {
        private static final int CAMERA_PERMISSION_CODE = 0;
        private static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;

        public static boolean hasCameraPermission(Activity activity) {
            return activity.checkSelfPermission(CAMERA_PERMISSION) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED;
        }

        public static void requestCameraPermission(Activity activity) {
            activity.requestPermissions(new String[] {CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
        }

        public static boolean shouldShowRequestPermissionRationale(Activity activity) {
            return activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION);
        }

        public static void launchPermissionSettings(Activity activity) {
            android.content.Intent intent = new android.content.Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivity(intent);
        }
    }
}