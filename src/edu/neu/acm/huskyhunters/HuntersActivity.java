package edu.neu.acm.huskyhunters;

import java.util.HashMap;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HuntersActivity extends ListActivity {
	
	ClueArray clues;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        /*
        JSONFetcher fetcher = new JSONFetcher();
        clues = fetcher.fetch();
        fetcher.finish();
        */
        clues = new ClueArray();
        clues.add(new Clue(1, "This is the Answer", "This is the Original Clue", 
        		70, "International Village", 3, false));
        SimpleAdapter ClueAdapter = new SimpleAdapter(this.getApplicationContext(), clues.mappify(), 
        		R.layout.clue_item, 
        		new String[] { "clueNum", "answer", "points" }, 
        		new int[] { R.id.cluenum, R.id.answer, R.id.points } ); 
        setListAdapter(ClueAdapter);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, String> item = (HashMap<String, String>) getListAdapter().getItem(position);
		Toast.makeText(this, "Clue " + item.get("clueNum") + " selected", Toast.LENGTH_SHORT).show();
	}
}