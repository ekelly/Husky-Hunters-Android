package edu.neu.acm.huskyhunters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HuntersActivity extends ListActivity {
	
	ClueArray clues;
	ArrayList<? extends Map<String, ?>> cluemap;
	ClueAdapter clueAdapter;
	
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
        clues.add(new Clue(2, "This is the Answer for clue 2, which is really really long", 
        		"This is the Original Clue", 50, "West Village C", 3, 
        		true));
        clues.add(new Clue(3, "This is the Answer for clue 3", 
        		"This is the Original Clue", 100, "West Village H", 3, 
        		false));
        
        cluemap = clues.mappify();
        clueAdapter = new ClueAdapter(this.getApplicationContext(), 
        		cluemap, R.layout.clue_item, 
        		new String[] { "clueNum", "answer", "points" }, 
        		new int[] { R.id.cluenum, R.id.answer, R.id.points } ); 
        setListAdapter(clueAdapter);
    }
    
    private static class ClueAdapter extends SimpleAdapter {
    	
		public ClueAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		  LinearLayout view = (LinearLayout) super.getView(position, convertView, parent);
		  @SuppressWarnings("unchecked")
		HashMap<String, String> c = (HashMap<String, String>) getItem(position);
		  if(Boolean.parseBoolean(c.get("solved"))) {
			  view.setBackgroundColor(Color.GREEN);
		  }
		  return view;
		}
    	
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> item = (HashMap<String, String>) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ClueDetail.class);
		Bundle bundle = new Bundle();
		bundle.putInt("cluenum", Integer.parseInt(item.get("clueNum")));
		bundle.putParcelableArrayList("clues", clues);
		intent.putExtras(bundle);
		startActivityForResult(intent, R.integer.clue_result);
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(requestCode) {
	     	case R.integer.camera_result:
		    	if (resultCode == RESULT_OK) {
		    		Bundle res = data.getExtras();
		    		Boolean picTaken = res.getBoolean("PictureTaken");
		    		Integer index = res.getInt("cluenum");
		    		if(picTaken) {
		    			// Sets clue to boolean and returns itself
		    			clues.get(index).setSolved(picTaken);
		    			// Refresh the list view
		    			cluemap = clues.mappify();
		    			clueAdapter = new ClueAdapter(this.getApplicationContext(), 
		    	        		cluemap, R.layout.clue_item, 
		    	        		new String[] { "clueNum", "answer", "points" }, 
		    	        		new int[] { R.id.cluenum, R.id.answer, R.id.points } ); 
		    	        setListAdapter(clueAdapter);
		    			
		    			// TODO Send data to server
		    			
		    			Toast.makeText(this, "Picture taken!", Toast.LENGTH_SHORT).show();
		    			break;
		    		}
		    	}
		    	Toast.makeText(this, "Picture not taken.", Toast.LENGTH_SHORT).show();
	        	break;
	        case R.integer.clue_result:
	        	if (resultCode == RESULT_OK) {
		    		Bundle res = data.getExtras();
		    		Boolean solved = res.getBoolean("solved");
		    		Integer cluenum = res.getInt("cluenum");
		    		if(solved) {
		    			// Sets clue to boolean and returns itself
		    			for(int i = 0; i < clues.size(); i++) {
		    				if(clues.get(i).clueNum() == cluenum) {
		    					clues.get(i).setSolved(solved);
		    					break;
		    				}
		    			}
		    			// Refresh the list view
		    			cluemap = clues.mappify();
		    			clueAdapter = new ClueAdapter(this.getApplicationContext(), 
		    	        		cluemap, R.layout.clue_item, 
		    	        		new String[] { "clueNum", "answer", "points" }, 
		    	        		new int[] { R.id.cluenum, R.id.answer, R.id.points } ); 
		    	        setListAdapter(clueAdapter);
		    			
		    			// TODO Send data to server
		    			
		    			break;
		    		}
		    	}
	        	break;
	        default:
	        	throw new RuntimeException("Result");
	     } // end switch
	}
}