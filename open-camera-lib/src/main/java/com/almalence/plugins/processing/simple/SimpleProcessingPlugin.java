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

package com.almalence.plugins.processing.simple;

import com.almalence.opencam.CameraScreenActivity;
import com.almalence.opencam.PluginManager;
import com.almalence.opencam.PluginProcessing;
import com.almalence.opencam.R;
import com.almalence.opencam.cameracontroller.CameraController;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/***
 * Implements simple processing plugin - just translate shared memory values
 * from captured to result.
 ***/

public class SimpleProcessingPlugin extends PluginProcessing
{
	private long			sessionID				= 0;


	private int				modePrefDro = 1;
	
	public SimpleProcessingPlugin()
	{
		super("com.almalence.plugins.simpleprocessing", R.xml.preferences_capture_dro, 0, 0, null);
	}

	@Override
	public void onStartProcessing(long SessionID)
	{
		sessionID = SessionID;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CameraScreenActivity.getMainContext());
		modePrefDro = Integer.parseInt(prefs.getString("modePrefDro", "1"));
		
		PluginManager.getInstance().addToSharedMem("modeSaveName" + sessionID,
				PluginManager.getInstance().getActiveMode().modeSaveName);

		CameraController.Size imageSize = CameraController.getCameraImageSize();
		int mImageWidth = imageSize.getWidth();
		int mImageHeight = imageSize.getHeight();

		String num = PluginManager.getInstance().getFromSharedMem("amountofcapturedframes" + sessionID);
		
		if (num == null)
			return;
		int imagesAmount = Integer.parseInt(num);
		
		String numRAW = PluginManager.getInstance().getFromSharedMem("amountofcapturedrawframes" + sessionID);
		
		int imagesAmountRAW = 0;
		if (numRAW != null)
			imagesAmountRAW = Integer.parseInt(numRAW);
		
		int frameNumRAW = 0;
		int frameNum = 0;

		for (int i = 1; i <= imagesAmount; i++)
		{
			int orientation = Integer.parseInt(PluginManager.getInstance().getFromSharedMem(
					"frameorientation" + i + sessionID));



				int frame = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("frame" + i + sessionID));
				int len = Integer.parseInt(PluginManager.getInstance().getFromSharedMem("framelen" + i + sessionID));
				
				boolean isRAW = Boolean.parseBoolean(PluginManager.getInstance().getFromSharedMem("frameisraw" + i + sessionID));
				
				int frameIndex = isRAW? (++frameNumRAW) : (imagesAmountRAW + (++frameNum));
				
				PluginManager.getInstance().addToSharedMem("resultframeformat" + frameIndex + sessionID, isRAW? "dng" : "jpeg");
				PluginManager.getInstance().addToSharedMem("resultframe" + frameIndex + sessionID, String.valueOf(frame));
				PluginManager.getInstance().addToSharedMem("resultframelen" + frameIndex + sessionID, String.valueOf(len));

				PluginManager.getInstance().addToSharedMem("saveImageWidth" + sessionID,
						String.valueOf(mImageWidth));
				PluginManager.getInstance().addToSharedMem("saveImageHeight" + sessionID,
						String.valueOf(mImageHeight));


			boolean cameraMirrored = Boolean.parseBoolean(PluginManager.getInstance().getFromSharedMem(
					"framemirrored" + i + sessionID));
			PluginManager.getInstance().addToSharedMem("resultframeorientation" + i + sessionID,
					String.valueOf(orientation));
			PluginManager.getInstance().addToSharedMem("resultframemirrored" + i + sessionID,
					String.valueOf(cameraMirrored));
		}

		PluginManager.getInstance().addToSharedMem("amountofresultframes" + sessionID, String.valueOf(imagesAmount));
	}

	@Override
	public boolean isPostProcessingNeeded()
	{
		return false;
	}

	@Override
	public void onStartPostProcessing()
	{
	}
}
