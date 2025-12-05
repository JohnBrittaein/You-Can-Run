package com.youcanrun.ar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

/**
 * CameraFragment - A simple AR camera fragment using ARCore directly.
 * Renders camera feed using GLSurfaceView.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    private static final int PERMISSION_CODE = 100;

    private Session arSession;
    private GLSurfaceView glSurfaceView;
    private ARRenderer arRenderer;
    private boolean userRequestedInstall = false;
    private String errorMessage = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "========== CameraFragment onCreateView START ==========");

        // Create GLSurfaceView for AR rendering
        glSurfaceView = new GLSurfaceView(requireContext());
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        Log.d(TAG, "GLSurfaceView created");

        // Create renderer
        arRenderer = new ARRenderer(this);
        glSurfaceView.setRenderer(arRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        Log.d(TAG, "ARRenderer set");

        // Request camera permission if needed
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Camera permission NOT granted, requesting...");
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CODE);
        } else {
            Log.i(TAG, "Camera permission already granted");
            initializeARCore();
        }

        Log.i(TAG, "========== CameraFragment onCreateView END ==========");
        return glSurfaceView;
    }

    private void initializeARCore() {
        if (arSession != null) {
            return; // Already initialized
        }

        try {
            // Check ARCore availability
            ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(requireContext());
            Log.d(TAG, "ARCore availability: " + availability);

            if (availability.isTransient()) {
                // ARCore is checking availability, retry in a moment
                Log.i(TAG, "ARCore availability is transient, will check again");
                return;
            }

            if (!availability.isSupported()) {
                errorMessage = "This device does not support ARCore";
                Log.e(TAG, errorMessage);
                showError(errorMessage);
                return;
            }

            // Request ARCore installation if needed
            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(
                    requireActivity(), !userRequestedInstall);
            Log.d(TAG, "ARCore install status: " + installStatus);

            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                userRequestedInstall = true;
                Log.i(TAG, "ARCore installation requested");
                return;
            }

            // Create AR session
            arSession = new Session(requireContext());

            // Configure session
            Config config = new Config(arSession);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            config.setFocusMode(Config.FocusMode.AUTO);
            arSession.configure(config);

            Log.i(TAG, "ARCore session created and configured successfully!");
            Toast.makeText(requireContext(), "ARCore initialized successfully", Toast.LENGTH_SHORT).show();

            // Set session for renderer
            if (arRenderer != null) {
                arRenderer.setSession(arSession);
            }

        } catch (UnavailableArcoreNotInstalledException e) {
            errorMessage = "ARCore is not installed. Please install it from the Play Store.";
            Log.e(TAG, errorMessage, e);
            showErrorWithPlayStoreLink(errorMessage);
        } catch (UnavailableUserDeclinedInstallationException e) {
            errorMessage = "ARCore installation was declined. AR features require ARCore.";
            Log.e(TAG, errorMessage, e);
            showError(errorMessage);
        } catch (UnavailableApkTooOldException e) {
            errorMessage = "ARCore is too old. Please update it from the Play Store.";
            Log.e(TAG, errorMessage, e);
            showErrorWithPlayStoreLink(errorMessage);
        } catch (UnavailableSdkTooOldException e) {
            errorMessage = "App SDK is too old. Please update the app.";
            Log.e(TAG, errorMessage, e);
            showError(errorMessage);
        } catch (UnavailableDeviceNotCompatibleException e) {
            errorMessage = "This device is not compatible with ARCore.";
            Log.e(TAG, errorMessage, e);
            showError(errorMessage);
        } catch (Exception e) {
            errorMessage = "Failed to initialize ARCore: " + e.getMessage();
            Log.e(TAG, errorMessage, e);
            showError(errorMessage);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showErrorWithPlayStoreLink(String message) {
        Toast.makeText(requireContext(), message + "\n\nOpening Play Store...", Toast.LENGTH_LONG).show();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Play Store", e);
        }
    }

    /**
     * Get the AR renderer for controlling filters
     */
    public ARRenderer getRenderer() {
        return arRenderer;
    }

    /**
     * Set camera filter mode
     */
    public void setFilter(CameraFilter filter) {
        if (arRenderer != null) {
            arRenderer.setFilter(filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Try to initialize ARCore if not already done (handles install flow)
        if (arSession == null) {
            initializeARCore();
        }

        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }

        if (arSession != null) {
            try {
                arSession.resume();
                Log.d(TAG, "ARCore session resumed");
            } catch (Exception e) {
                Log.e(TAG, "Error resuming ARCore session: " + e.getMessage(), e);
                errorMessage = "Failed to resume AR session: " + e.getMessage();
                showError(errorMessage);
            }
        }
    }

    @Override
    public void onPause() {
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
        if (arSession != null) {
            arSession.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (arRenderer != null) {
            arRenderer.cleanup();
        }
        if (arSession != null) {
            arSession.close();
            arSession = null;
        }
        super.onDestroy();
    }
}
