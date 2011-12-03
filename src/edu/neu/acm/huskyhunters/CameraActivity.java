package edu.neu.acm.huskyhunters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.widget.FrameLayout;

public class CameraActivity extends Activity {

	final static String TAG = "CameraActivity";
	
	Camera camera;
	Preview preview;
	Integer cluenum;
	Boolean picTaken = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		cluenum = bundle.getInt("cluenum");
		
		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.picture_preview)).addView(preview);
	}
	
	public void takePicture(View v) {
		takePicture();
	}
	
	private void takePicture() {
		preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// TODO Do something when the shutter closes.
			Log.d(TAG, "onShutter fired");
		}
	};
 
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			// TODO Do something with the image RAW data.
			Log.d(TAG, "rawCallback fired");
		}
	};
 
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			// TODO Save image to SD card
			FileOutputStream outStream = null;
			try {
				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				File sdDir = Environment.getExternalStorageDirectory();
				if(sdDir.canWrite()) {
					String root = sdDir.getCanonicalPath();
					outStream = new FileOutputStream(String.format(root + "/%d.jpg", cluenum)); // System.currentTimeMillis()));
					outStream.write(_data);
					outStream.close();
					picTaken = true;
					Log.d(TAG, "onPictureTaken - wrote bytes: " + _data.length);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
			
			// TODO Send the image
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        returnToParent();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void returnToParent() {
	   Bundle conData = new Bundle();
	   conData.putBoolean("PictureTaken", picTaken);
	   Intent intent = new Intent();
	   intent.putExtras(conData);
	   setResult(RESULT_OK, intent);
	   finish();
	}
	
	public void onStop() {
		super.onStop();
		returnToParent();
	}
	
	public void onResume() {
		super.onResume();
	}
	
	public void onPause() {
		super.onPause();
	}
	
}
