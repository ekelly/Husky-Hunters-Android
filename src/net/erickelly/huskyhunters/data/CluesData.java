package net.erickelly.huskyhunters.data;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

import net.erickelly.huskyhunters.R;
import net.erickelly.huskyhunters.data.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;

/**
 * A database system for loading and accessing clues.  Includes methods for 
 * retrieving clues from server, storing them in database, and 
 * @author francis
 *
 */
public class CluesData implements Closeable {
	private static final String TAG = "Clues";
	
	// Context from which to work
	private final Context mCtx;
	
	// Singleton instance of the class
	private static CluesData sInstance = null;
	
	// Switch for data being loaded
	//private static Boolean mIsLoaded;
	
	// long variable for storing last update time, saved in UTC millis format
	// Should be updated with database on every sync.
	private Time lastUpdateTime;
	
	private DatabaseWrapper mDbHelper;
	
	/**
	 * A cursor adapter to use with the ListActivity
	 * Technically, I don't even change anything, but the structure
	 * is now here if I ever want to.  And I didn't realize that I
	 * didn't need to change anything
	 * @author eric
	 */
	private static class CluesAdapter extends SimpleCursorAdapter implements Filterable {
		
		private static final String[] from = new String[] { 
			Constants.KEY_CLUEID,
			Constants.KEY_ANS,
			Constants.KEY_POINTS
		};
		private static final int[] to = new int[] { 
			R.id.cluenum, R.id.answer, R.id.points
		};
		private static final int layout = R.layout.clue_item;
		
		Context context;
		
		/**
		 * Constructor for CluesAdapter
		 * @param context The context where the ListView associated with this SimpleListItemFactory is running
		 * @param layout resource identifier of a layout file that defines the views for this list item. The layout file should include at least those named views defined in "to"
		 * @param c The database cursor. Can be null if the cursor is not available yet.
		 * @param from A list of column names representing the data to bind to the UI. Can be null if the cursor is not available yet.
		 * @param to The views that should display column in the "from" parameter. These should all be TextViews. The first N views in this list are given the values of the first N columns in the from parameter. Can be null if the cursor is not available yet.
		 * @param flags Flags used to determine the behavior of the adapter, as per CursorAdapter(Context, Cursor, int).
		 */
		public CluesAdapter(Context context, Cursor c) {
			super(context, layout, c, from, to);
			this.context = context;
		}
		
		@Override
		public void bindView(View row, Context context, Cursor cursor) {
			super.bindView(row, context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        final LayoutInflater inflater = LayoutInflater.from(context);
	        View v = inflater.inflate(layout, parent, false);
	        String solved = cursor.getString(cursor.getColumnIndex(Constants.KEY_SOLVED));
	        if(solved == "solved") {
	        	v.setBackgroundResource(R.drawable.list_item_solved);
	        	v.invalidate();
	        }
	        
	        return v;
		}
		
		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			if(constraint == null || constraint.equals("")) {
				return getInstance(context).fetchAllClues();
			}
			String filter = (String) constraint;
	        Cursor c = getInstance(context).filterClues(filter);
	        Cursor oldCursor = getCursor();
	        if(oldCursor != null) {
	        	oldCursor.close();
	        }
	        return c;
		}
	}
	
	public CluesData(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new DatabaseWrapper(mCtx);
		try {
			mDbHelper.open();
			lastUpdateTime = new Time();
			lastUpdateTime.set(mDbHelper.getTime());
		}
		catch(SQLException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}
	
	public static CluesData getInstance(Context ctx) {
		if(sInstance == null) {
			sInstance = new CluesData(ctx);
		}
		return sInstance;
	}
	
	/**
	 * Opens the clues database for writing.  If it cannot be opened, attempt 
	 * to create a new instance.  If this is unsuccessful, throws an
	 * exception.
	 * @throws SQLException if database cannot be opened/created.
	 */
	public void open() throws SQLException {
		try {
			mDbHelper.open();
			lastUpdateTime = new Time();
			lastUpdateTime.set(mDbHelper.getTime());
		}
		catch(SQLException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}
	
	/**
	 * Closes the database.
	 */
	@Override
	public void close() {
		mDbHelper.close();
	}
	/**
	 * Updates the time store in the database to indicate the current time.
	 * Should be used when a sync is completed.
	 */
	public void setTimeToNow() {
		lastUpdateTime.setToNow();
		mDbHelper.setTime(lastUpdateTime.toMillis(false));
	}
	
	/**
	 * Parse JSON representation of clues and return an ArrayList of clues
	 * @param data JSON representation of the clues
	 * @return ArrayList<Clue>
	 * @author eric
	 */
	private ArrayList<Clue> parseClues(String data) {
		ArrayList<Clue> clueList = new ArrayList<Clue>();
		try {
			JSONArray jsonArray = new JSONArray(data);
			Log.i(TAG, "Number of entries " + jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				
				/*String clueid = obj.getString("clueid");*/
				String clueid = ((Integer) obj.getInt("number")).toString();
				String answer = obj.getString("answer");
				/*String originalClue = obj.getString("clue");*/
				String originalClue = obj.getString("hint");
				Integer points = obj.getInt("points");
				/*String solved = obj.getString("solved");*/
				JSONArray ll = obj.getJSONArray("latlng");
				Double[] latlng = { 0.0, 0.0 };
				if(ll.length() == 2) {
					latlng[0] = (Double) ll.get(0);
					latlng[1] = (Double) ll.get(1);
				}
				
				String solved = "unsolved";
				
				clueList.add(new Clue(clueid, answer, originalClue, 
						points, "", solved, latlng, new LinkedList<String>()));
				
				Log.i(TAG, originalClue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return clueList;
	}
	
	/**
	 * Get clues from the server
	 * @param groupHash The hash indicating which group's clues to download
	 * @return JSON representation of the data in the form of a string
	 * @author eric
	 */
	private String requestClues(String groupHash) {
		StringBuilder builder = new StringBuilder();
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(getCluesUrl(groupHash));
		
		try {
			HttpResponse response = client.execute(request);

			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	/**
	 * Returns the constructed url of the api server
	 * @param groupHash The hash indicating which group's clues to download
	 * @return the url of the API server
	 * @author eric
	 */
	private String getCluesUrl(String groupHash) {
		return "http://huskyhunter.roderic.us/api/teams/" + groupHash + "/clues";
	}
	
	/**
	 * Download all clues
	 * @param groupHash The hash indicating which group's clues to download
	 * @return Whether or not the clues successfully loaded
	 * @author eric
	 */
	private Boolean firstTimeSync(String groupHash) {
		mDbHelper.clear();
		try {
			ArrayList<Clue> clueList = parseClues(requestClues(groupHash));
			for(Clue c : clueList) {
				// Add to database
				mDbHelper.insertClue(c);
			}
			setTimeToNow();
		} catch(Exception e) {
			e.printStackTrace();
			//mIsLoaded = false;
			return false;
		}
		//mIsLoaded = true;
		return true;
	}
	
	/**
	 * Called by Sync if firstTimeSync has already occurred
	 * @author eric
	 */
	private void updateSync(String hash) {
		//throw new UnsupportedOperationException();
		firstTimeSync(hash);
		setTimeToNow();
	}
	
	/**
	 * Syncs to server.  Calls appropriate sync method.
	 */
	public void sync(String hash) {
		// if time > 24 hours old
		Time now = new Time();
		now.setToNow();
		long diff = now.toMillis(false) - lastUpdateTime.toMillis(false);
		long seconds = diff / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		if(hours > 24) {
			firstTimeSync(hash);
		} else {
			updateSync(hash);
		}
	}
	
	/**
	 * Fetches all clues in the database.
	 * @return A cursor over all clues in the database.
	 */
	public Cursor fetchAllClues() {
		return mDbHelper.fetchAllClues();
	}
	
	/**
	 * Fetches a given clue by database row id.
	 * @param rowId id of clue to retrieve
	 * @return A cursor positioned at the clue, if found
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor fetchClue(long rowId) throws SQLException {
		return mDbHelper.fetchClue(rowId);
	}
	
	public Cursor fetchCluePhotos(String clueid) {
		return mDbHelper.fetchCluePhotos(clueid);
	}
	
	/**
	 * Returns a Cursor of all clues whose Clue ID begins with the given String
	 * @param clueId Clue ID to pattern match clues to retrieve
	 * @return A Cursor over all matching clues
	 * @throws SQLException if clue could not be found/retrieved
	 */
	public Cursor filterClues(String clueId) throws SQLException {
		return mDbHelper.filterClues(clueId);
	}
	
	/**
	 * Returns a Cursor of all clues which are either solved or unsolved
	 * @param solved Boolean - true if you only want solved clues, false otherwise
	 * @return Cursor
	 * @throws SQLException
	 * @author eric
	 */
	public Cursor filterClues(Boolean solved) throws SQLException {
		if(solved) {
			//String filter = Constants.KEY_SOLVED + " != 'unsolved'";
		} else {
			//String filter = Constants.KEY_SOLVED + " = 'unsolved'";
		}
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns an adapter mapping a cursor to a row item
	 * @return A SimpleCursorAdapter
	 */
	public SimpleCursorAdapter getAdapter() {
		CluesAdapter adapter = new CluesAdapter(mCtx, fetchAllClues());
		Integer clueidCol = adapter.getCursor().getColumnIndexOrThrow("clueid");
		adapter.setStringConversionColumn(clueidCol);
		return adapter;
	}
}
