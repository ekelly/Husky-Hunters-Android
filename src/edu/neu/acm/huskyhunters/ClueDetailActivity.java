package edu.neu.acm.huskyhunters;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ClueDetailActivity extends Activity {
	
	Clue clue;
	boolean solved;
	Uri imageUri;
	CluesData clues;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clue_detail);
		
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		Integer cluenum = data.getInt("cluenum");
		clues = CluesData.getInstance(getApplicationContext());
		Cursor clueCursor = clues.filterClues(cluenum.toString());
		startManagingCursor(clueCursor);
		clue = new Clue(clueCursor);
		
		/*
		ArrayList<Clue> clues = data.getParcelableArrayList("clues");
		for(int i = 0; i < clues.size(); i++) {
			if(clues.get(i).clueNum() == cluenum) {
				clue = clues.get(i);
				break;
			}
		}
		if(clue == null) {
			throw new RuntimeException("Selected clue wasn't in data set");
		}
		*/
		
		// Set content in the view
		View status = findViewById(R.id.detail_status);
		if(clue.solved() == "solved") {
			status.setBackgroundColor(android.graphics.Color.GREEN);
		}
		TextView clueView = (TextView) findViewById(R.id.detail_cluenum);
		clueView.setText("#" + clue.clueNum().toString());
		TextView pointsView = (TextView) findViewById(R.id.detail_points);
		pointsView.setText(clue.points().toString() + "pts");
		TextView clueTextView = (TextView) findViewById(R.id.detail_clue);
		clueTextView.setText(clue.clue());
		TextView answerView = (TextView) findViewById(R.id.detail_answer);
		answerView.setText(clue.answer());
		
	}
	
	public void takePhoto(View v) {
		Log.i("ClueDetail","takePhoto");
		/*
		Intent intent = new Intent(this, CameraActivity.class);
		Bundle data = new Bundle();
		data.putInt("cluenum", clue.clueNum());
		intent.putExtras(data);
		startActivityForResult(intent, R.integer.camera_result);
		*/
		//define the file-name to save photo taken by Camera activity
		String fileName = clue.clueNum().toString() + ".jpg";
		//create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION,"Image for clue " + clue.clueNum().toString());
		//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		//create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, R.integer.camera_result);
	}
	
	private void returnToParent() {
	   Bundle conData = new Bundle();
	   if(solved) {
		   conData.putParcelable("photo", imageUri);
	   }
	   conData.putBoolean("solved", solved);
	   conData.putString("cluenum", clue.clueNum());
	   Intent intent = new Intent();
	   intent.putExtras(conData);
	   setResult(RESULT_OK, intent);
	   finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(requestCode) {
	     	case R.integer.camera_result:
	     		/*
		    	if (resultCode == RESULT_OK) {
		    		Bundle res = data.getExtras();
		    		Boolean picTaken = res.getBoolean("PictureTaken");
		    		if(picTaken) {
		    			solved = clue.setSolved(picTaken); // Sets clue to boolean and returns itself
		    			View status = findViewById(R.id.detail_status);
		    			status.setBackgroundColor(Color.GREEN);
		    			Toast.makeText(this, "Picture taken!", Toast.LENGTH_SHORT).show();
		    			break;
		    		}
		    	}
		    	Toast.makeText(this, "Picture not taken.", Toast.LENGTH_SHORT).show();
		    	*/
	     		if (resultCode == RESULT_OK) {
	     	        // use imageUri to get image result
	     			clue.setSolved("solved");
	     			View status = findViewById(R.id.detail_status);
	     			status.setBackgroundColor(Color.GREEN);
	     			Toast.makeText(this, "Picture taken!", Toast.LENGTH_SHORT).show();
	     	    } else if (resultCode == RESULT_CANCELED) {
	     	        Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
	     	    } else {
	     	        Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
	     	    }
	        	break;
	        default:
	        	throw new RuntimeException("Result");
	     } // end switch
	}
	
	public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
		Cursor cursor = null;
		try {
		    String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
		    cursor = activity.managedQuery( imageUri,
		            proj, // Which columns to return
		            null,       // WHERE clause; which rows to return (all rows)
		            null,       // WHERE clause selection arguments (none)
		            null); // Order-by clause (ascending by name)
		    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    // int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
		    if (cursor.moveToFirst()) {
		        //String orientation =  cursor.getString(orientation_ColumnIndex);
		        return new File(cursor.getString(file_ColumnIndex));
		    }
		    return null;
		} finally {
		    if (cursor != null) {
		        cursor.close();
		    }
		}
	}
	
	public void getDirections(View v) {
		Log.i("ClueDetail","getDirections");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        returnToParent();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void onClose() {
		returnToParent();
	}
	
}
