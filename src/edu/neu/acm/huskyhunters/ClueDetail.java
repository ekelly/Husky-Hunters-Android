package edu.neu.acm.huskyhunters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ClueDetail extends Activity {
	
	Clue clue;
	
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
		clueView.setText(clue.clueNum().toString());
		TextView pointsView = (TextView) findViewById(R.id.detail_points);
		pointsView.setText(clue.points().toString());
		TextView clueTextView = (TextView) findViewById(R.id.detail_clue);
		clueTextView.setText(clue.clue());
		TextView answerView = (TextView) findViewById(R.id.detail_answer);
		answerView.setText(clue.answer());
		
	}
	
	public void takePhoto(View v) {
		Log.i("ClueDetail","takePhoto");
	}
	
	public void getDirections(View v) {
		Log.i("ClueDetail","getDirections");
	}
	
}
