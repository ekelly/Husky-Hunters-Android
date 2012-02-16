package edu.neu.acm.huskyhunters;

import edu.neu.acm.huskyhunters.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class HuntersActivity extends ListActivity {
	//private static final String TAG = "HuntersActivityDb";
	
	CluesData clues;
	SimpleCursorAdapter cluesAdapter;
	Context context = this;
	EditText filterText = null;
	
	private static final String GROUP_HASH = "1480092";
	
	class DownloadCluesTask extends AsyncTask<String, Integer, CluesData>{
		
		ProgressDialog loading;

		@Override
		protected CluesData doInBackground(String... params) {
			String groupHash = params[0];
			clues.sync(groupHash);
			return clues;
		}
		
		@Override
		protected void onPreExecute() {
			loading = new ProgressDialog(context);
			loading.setMessage("Downloading clues");
			loading.show();
		}
		
		@Override
		protected void onPostExecute(CluesData result) {
			clues = result;
			// Update the list
			setListAdapter(clues.getAdapter());
			if(loading.isShowing()) {
				loading.dismiss();
			}
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clues = CluesData.getInstance(this);
        fillData();
        new DownloadCluesTask().execute(GROUP_HASH);
        setContentView(R.layout.clue_list);
        this.getListView().setTextFilterEnabled(true);
        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);
    }
    
    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            cluesAdapter.getFilter().filter(s);
        }
    };
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SQLiteCursor item = (SQLiteCursor) getListAdapter().getItem(position);
		String clueid = item.getString(item.getColumnIndex("clueid"));
		Intent intent = new Intent(this, ClueDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("cluenum", Integer.parseInt(clueid));
		intent.putExtras(bundle);
		startActivityForResult(intent, R.integer.clue_result);
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(requestCode) {
	     	case R.integer.camera_result:
		    	if (resultCode == RESULT_OK) {
		    		Bundle res = data.getExtras();
		    		Boolean picTaken = res.getBoolean("PictureTaken");
		    		//Integer index = res.getInt("cluenum");
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
		    		//Integer cluenum = res.getInt("cluenum");
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
		cluesAdapter = clues.getAdapter();
		setListAdapter(cluesAdapter);
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	filterText.removeTextChangedListener(filterTextWatcher);
    }
}