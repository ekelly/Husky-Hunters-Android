package edu.neu.acm.huskyhunters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ClueDetail extends Activity {
	
	Clue clue;
	boolean solved;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clue_detail);
		
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		int cluenum = data.getInt("cluenum");
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
		
		// Set content in the view
		View status = findViewById(R.id.detail_status);
		if(clue.solved()) {
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
		Intent intent = new Intent(this, CameraActivity.class);
		Bundle data = new Bundle();
		data.putInt("cluenum", clue.clueNum());
		intent.putExtras(data);
		startActivityForResult(intent, R.integer.camera_result);
	}
	
	private void returnToParent() {
	   Bundle conData = new Bundle();
	   conData.putBoolean("solved", solved);
	   conData.putInt("cluenum", clue.clueNum());
	   Intent intent = new Intent();
	   intent.putExtras(conData);
	   setResult(RESULT_OK, intent);
	   finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(requestCode) {
	     	case R.integer.camera_result:
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
	        	break;
	        default:
	        	throw new RuntimeException("Result");
	     } // end switch
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