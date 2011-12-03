package edu.neu.acm.huskyhunters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
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
        
        // Popup "Loading..." indicator?
        
        /*
        JSONFetcher fetcher = new JSONFetcher();
        clues = fetcher.fetch();
        fetcher.finish();
        */
        
        // Remove loading indicator?
        
        clues = new ClueArray();
        clues.add(new Clue(1, "This is the Answer", "This is the Original Clue", 
        		70, "International Village", 3, false));
        clues.add(new Clue(2, "This is the Answer for clue 2", 
        		"This is the Original Clue", 50, "West Village C", 3, true));
        clues.add(new Clue(3, "This is the Answer for clue 3", 
        		"This is the Original Clue", 100, "West Village H", 3, false));
        
        ArrayList<? extends Map<String, ?>> cluemap = clues.mappify();
        SimpleAdapter ClueAdapter = new SimpleAdapter(this.getApplicationContext(), 
        		cluemap, R.layout.clue_item, 
        		new String[] { "clueNum", "answer", "points" }, 
        		new int[] { R.id.cluenum, R.id.answer, R.id.points } ); 
        setListAdapter(ClueAdapter);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> item = (HashMap<String, String>) getListAdapter().getItem(position);
		Toast.makeText(this, "Clue " + item.get("clueNum") + " selected", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, ClueDetail.class);
		Bundle bundle = new Bundle();
		bundle.putInt("cluenum", Integer.parseInt(item.get("clueNum")));
		bundle.putParcelableArrayList("clues", clues);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}