package edu.neu.acm.huskyhunters;

import java.util.HashMap;

import edu.neu.acm.huskyhunters.R;
import edu.neu.acm.huskyhunters.R.id;
import edu.neu.acm.huskyhunters.R.integer;
import edu.neu.acm.huskyhunters.R.layout;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class HuntersActivity extends ListActivity {
	private static final String TAG = "HuntersActivityDb";
	
	private static final String GROUP_HASH = "97580414";
	
	CluesData clues;
	
	class DownloadCluesTask extends AsyncTask<CluesData, Integer, CluesData>{

		@Override
		protected CluesData doInBackground(CluesData... params) {
	      CluesData clueDb = params[0];
		  //clues.load(GROUP_HASH);
	      clueDb.sync(GROUP_HASH);
		  return clueDb;
		}
		
	     protected void onPostExecute(CluesData result) {
	    	 clues = result;
	    	 // Set the list adapter
	         //setListAdapter(clues.getAdapter());
	     }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clues = CluesData.getInstance(this);
        new DownloadCluesTask().execute(clues);
        setContentView(R.layout.clue_list);
        fillData();
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> item = (HashMap<String, String>) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ClueDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("cluenum", Integer.parseInt(item.get("clueNum")));
		//bundle.putParcelableArrayList("clues", mCluesData.getArray());
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
		    			// clues.get(index).setSolved(picTaken);
		    			
		    			// Reset the list adapter
		    			
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
		    			/*
		    			ClueArray clues = (ClueArray) mCluesData.getArray();
		    			for(int i = 0; i < clues.size(); i++) {
		    				if(clues.get(i).clueNum() == cluenum) {
		    					clues.get(i).setSolved(solved);
		    					mCluesData.setData(clues);
		    					break;
		    				}
		    			}
		    			*/
		    			// Reset the list
		    	        setListAdapter(CluesData.getInstance(this
		    	        		.getApplicationContext()).getAdapter());
		    			
		    			// TODO Send data to server
		    			
		    			break;
		    		}
		    	}
	        	break;
	        default:
	        	throw new RuntimeException("Result");
	     } // end switch
	}
    
    /**
     * Gets all the clues from the database and populates the item list.
     */
    private void fillData() {
		Cursor c = clues.fetchAllClues();
		startManagingCursor(c);
		
		String[] from = new String[] { ClueDbAdapter.KEY_CLUEID, ClueDbAdapter.KEY_TEXT };
		int[] to = new int[] { R.id.clueid, R.id.cluedesc };
		
		SimpleCursorAdapter notes = 
				new SimpleCursorAdapter(this, R.layout.clue_row, c, from, to);
		setListAdapter(notes);
	}
}