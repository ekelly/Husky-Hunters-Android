package edu.neu.acm.huskyhunters;

import android.app.Activity;
import android.os.Bundle;

public class HuntersActivity extends Activity {
	
	ClueArray clues;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        JSONFetcher fetcher = new JSONFetcher();
        clues = fetcher.fetch();
        fetcher.finish();
    }
}