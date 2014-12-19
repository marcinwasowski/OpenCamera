/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
 */

package com.almalence.plugins.capture.standard;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.hardware.camera2.CaptureResult;


import com.almalence.opencam.CameraScreenActivity;
import com.almalence.opencam.PluginCapture;
import com.almalence.opencam.PluginManager;

import com.almalence.opencam.cameracontroller.CameraController;


/**
 * Implements standard capture plugin - capture single image and save it in
 * shared memory
 * *
 */

public class CapturePlugin extends PluginCapture {
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
            + "/DCIM/Camera/tmp_raw_img";

    public CapturePlugin() {
        super("com.almalence.plugins.capture", 0, 0, 0, null);
    }

    @Override
    public void onCreate() {}

    @Override
    public void onCameraParametersSetup() {}

    @Override
    public void onStart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CameraScreenActivity.getMainContext());
        captureRAW = (prefs.getBoolean(CameraScreenActivity.sCaptureRAWPref, false) && CameraController.isRAWCaptureSupported());
    }

    @Override
    public void onResume() {
        inCapture = false;
        aboutToTakePicture = false;
    }

    @Override
    public void onPause() {}

    @Override
    public void onGUICreate() {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    }

    @Override
    public void onStop() {}

    @Override
    public void onDefaultsSelect() {}

    @Override
    public void onShowPreferences() {}

    protected int framesCaptured = 0;
    protected int resultCompleted = 0;

    @Override
    public void takePicture() {
        framesCaptured = 0;
        resultCompleted = 0;

        if (captureRAW) {
            requestID = CameraController.captureImagesWithParams(1, CameraController.RAW, null, null, null, null, true);
        } else
            requestID = CameraController.captureImagesWithParams(1, CameraController.JPEG, null, null, null, null, true);
    }


    @Override
    public void onImageTaken(int frame, byte[] frameData, int frame_len, int format) {
        framesCaptured++;
        boolean isRAW = (format == CameraController.RAW);

        PluginManager.getInstance().addToSharedMem("frame" + framesCaptured + SessionID, String.valueOf(frame));
        PluginManager.getInstance().addToSharedMem("framelen" + framesCaptured + SessionID, String.valueOf(frame_len));

        PluginManager.getInstance().addToSharedMem("frameisraw" + framesCaptured + SessionID, String.valueOf(isRAW));


        PluginManager.getInstance().addToSharedMem("frameorientation" + framesCaptured + SessionID,
                String.valueOf(CameraScreenActivity.getGUIManager().getDisplayOrientation()));
        PluginManager.getInstance().addToSharedMem("framemirrored" + framesCaptured + SessionID,
                String.valueOf(CameraController.isFrontCamera()));

        PluginManager.getInstance().addToSharedMem("amountofcapturedframes" + SessionID, String.valueOf(framesCaptured));
        PluginManager.getInstance().addToSharedMem("amountofcapturedrawframes" + SessionID, isRAW ? "1" : "0");


        if ((captureRAW && framesCaptured == 2) || !captureRAW) {
            PluginManager.getInstance().sendMessage(PluginManager.MSG_CAPTURE_FINISHED, String.valueOf(SessionID));
            inCapture = false;
            framesCaptured = 0;
            resultCompleted = 0;
        }
    }

    @TargetApi(21)
    @Override
    public void onCaptureCompleted(CaptureResult result) {
        resultCompleted++;
        if (result.getSequenceId() == requestID) {
            PluginManager.getInstance().addToSharedMemExifTagsFromCaptureResult(result, SessionID, resultCompleted);
        }

        if (captureRAW) {
            Log.e("CapturePlugin", "onCaptureCompleted. resultCompleted = " + resultCompleted);
            PluginManager.getInstance().addRAWCaptureResultToSharedMem("captureResult" + resultCompleted + SessionID, result);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data) {}

    public boolean delayedCaptureSupported() { return true; }

    public boolean photoTimeLapseCaptureSupported() { return true; }
}
