package edu.neu.acm.huskyhunters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class HuntersActivity extends ListActivity {
	private static final String TAG = "HuntersActivity";
	
	private static final String GROUP_HASH = "97580414";
	
	ClueArray clues;
	ArrayList<? extends Map<String, ?>> cluemap;	
	CluesData mCluesData;
	
	
	class DownloadCluesTask extends AsyncTask<CluesData, Integer, CluesData>{

		@Override
		protected CluesData doInBackground(CluesData... params) {
	      CluesData clue = params[0];
		  clue.load(GROUP_HASH);
		  return clue;
		}
		
	     protected void onPostExecute(CluesData result) {
	    	 mCluesData = result;
	         setListAdapter(mCluesData.getAdapter());
	     } 
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mCluesData = CluesData.getInstance(getApplicationContext());
        if ( !mCluesData.isLoaded() ) {
        	new DownloadCluesTask().execute(mCluesData);
        }
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> item = (HashMap<String, String>) getListAdapter().getItem(position);
		Intent intent = new Intent(this, ClueDetail.class);
		Bundle bundle = new Bundle();
		bundle.putInt("cluenum", Integer.parseInt(item.get("clueNum")));
		bundle.putParcelableArrayList("clues", mCluesData.getArray());
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
		    			
		    			// Reset the list adapter
		    			CluesData.getInstance(getApplicationContext());
		    	        setListAdapter(CluesData.getInstance(this
		    	        		.getApplicationContext()).getAdapter());
		    			
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
		    			ClueArray clues = (ClueArray) mCluesData.getArray();
		    			for(int i = 0; i < clues.size(); i++) {
		    				if(clues.get(i).clueNum() == cluenum) {
		    					clues.get(i).setSolved(solved);
		    					mCluesData.setData(clues);
		    					break;
		    				}
		    			}
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
}