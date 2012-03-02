package net.erickelly.huskyhunters;

import net.erickelly.huskyhunters.data.CluesData;
import net.erickelly.huskyhunters.services.DownloaderService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HuntersActivity extends FragmentActivity {
	private static final String TAG = "HuntersActivityDb";
	private final int CLUE_RESULT = 1;
	private final int CAMERA_RESULT = 0;
	
	CluesData clues;
	SimpleCursorAdapter cluesAdapter;
	Context context = this;
	EditText filterText = null;
	ListView list = null;
	TextView empty = null;
	
	private static String GROUP_HASH; // = "1480092";
	
	class DownloadCluesTask extends AsyncTask<String, Integer, CluesData> {
		
		@Override
		protected CluesData doInBackground(String... params) {
			String groupHash = params[0];
			clues.sync(groupHash);
			return clues;
		}
		
		@Override
		protected void onPostExecute(CluesData result) {
			clues = result;
			if (clues.fetchAllClues().getCount() < 1) {
				list.setVisibility(2);
				empty.setVisibility(0);
			} else {
				// Update the list
				list.setVisibility(0);
				empty.setVisibility(2);
				list.setAdapter(clues.getAdapter());
			}
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        GROUP_HASH = sp.getString("groupid", "42612523"); // TODO: make null when in production
        clues = CluesData.getInstance(this);
        
        setContentView(R.layout.clue_list);
        list = (ListView) findViewById(R.id.clue_list);
        filterText = (EditText) findViewById(R.id.search_box);
        empty = (TextView) findViewById(R.id.empty);
        
        fillData();
        new DownloadCluesTask().execute(GROUP_HASH);
        
        list.setTextFilterEnabled(true);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SQLiteCursor item = (SQLiteCursor) list.getAdapter().getItem(position);
				String clueid = item.getString(item.getColumnIndex("clueid"));
				Intent intent = new Intent(HuntersActivity.this, ClueDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("clueid", clueid);
				intent.putExtras(bundle);
				startActivityForResult(intent, CLUE_RESULT);
			}
        });
        filterText.addTextChangedListener(filterTextWatcher);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Intent intent = new Intent(HuntersActivity.this.getApplicationContext(),
                DownloaderService.class);
    	intent.putExtra("grouphash", GROUP_HASH);
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem refresh = menu.findItem(R.id.refresh);
        refresh.setIntent(intent);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
			case R.id.refresh:
				startService(item.getIntent());
				return true;
			case R.id.settings:
				startActivity(new Intent(this, Settings.class));
				return true;
    	}
    	return false;
    }
    
    
    
    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            cluesAdapter.getFilter().filter(s, new Filter.FilterListener() {
				
				@Override
				public void onFilterComplete(int count) {
					//HuntersActivity.this.findViewById(android.R.id.list).invalidate();
					list.setAdapter(cluesAdapter);
				}
			});
            Log.d(TAG, "test");
        }
    };
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(requestCode) {
	     	case CAMERA_RESULT:
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
	        case CLUE_RESULT:
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
		list.setAdapter(clues.getAdapter());
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	filterText.removeTextChangedListener(filterTextWatcher);
    }
}