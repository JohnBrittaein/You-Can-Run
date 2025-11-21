package com.youcanrun.ar;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.youcanrun.utils.Vector3;

import java.util.Objects;

/**
 *
 */
public class ARSessionManager {
    private static final String TAG = "ARSessionManager";
    private Context context;
    private ArSceneView Sceneview;
    private ArFragment arCam;

    private Vector3 exampleVector; // Just know Vector3 class exists for you! Should help.

    // TODO: Initialize ARCore session, manage anchors, render monsters

    public ARSessionManager(Context context) {
        this.context = context;
        //arSceneView = findViewById(R.id.surfaceview);
        arCam = (ArFragment)getSupportFragmentManager().findFr;
        FragmentManager.findFragment()
        //Fragment fragment = ArFragment.setupSession;
        Log.d(TAG, "ARSessionManager initialized");


    }

    // TODO: Add methods to manage and render the AR environment
}
