package edu.neu.acm.huskyhunters;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
	private static Boolean mIsLoaded;
	
	// long variable for storing last update time, saved in UTC millis format
	// Should be updated with database on every sync.
	private Time lastUpdateTime;
	
	private ClueDbAdapter mDbHelper;
	
	/**
	 * A cursor adapter to use with the ListActivity
	 * Technically, I don't even change anything, but the structure
	 * is now here if I ever want to.  And I didn't realize that I
	 * didn't need to change anything
	 * @author eric
	 */
	private static class CluesAdapter extends SimpleCursorAdapter {
		
		private static final String[] from = new String[] { 
			ClueDbAdapter.KEY_CLUEID,
			ClueDbAdapter.KEY_ANS,
			ClueDbAdapter.KEY_POINTS
		};
		private static final int[] to = new int[] { 
			R.id.cluenum, R.id.answer, R.id.points
		};
		private static final int layout = R.layout.clue_item;
		
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
		}
		
		@Override
		public void bindView(View row, Context context, Cursor cursor) {
			super.bindView(row, context, cursor);
		}
	}
	
	public CluesData(Context ctx) {
		this.mCtx = ctx;
		mDbHelper = new ClueDbAdapter(mCtx);
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
	 * Parse JSON representation of clues and add it to the database
	 * @param data JSON representation of the clues
	 * @author eric
	 */
	private void parseClues(String data) {
		try {
			JSONArray jsonArray = new JSONArray(data);
			Log.i(TAG, "Number of entries " + jsonArray.length());
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				
				String clueid = obj.getString("clueid");
				String answer = obj.getString("answer");
				String originalClue = obj.getString("clue");
				int points = obj.getInt("points");
				String solved = obj.getString("solved");
				JSONArray ll = obj.getJSONArray("location");
				Double[] latlng = { 0.0, 0.0 };
				if(ll.length() == 2) {
					latlng[0] = (Double) ll.get(0);
					latlng[1] = (Double) ll.get(1);
				}
				
				// Add to database
				mDbHelper.insertClue(clueid, answer, originalClue, points, 
						latlng, solved, null, null);
				
				Log.i(TAG, originalClue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		try {
			parseClues(requestClues(groupHash));
		} catch(Exception e) {
			e.printStackTrace();
			mIsLoaded = false;
			return false;
		}
		
		mIsLoaded = true;
		return true;
		
		//setTimeToNow(); // when sync is done, update last updated time
	}
	
	/**
	 * Called by Sync if firstTimeSync has already occurred
	 * @author eric
	 */
	private void updateSync() {
		throw new UnsupportedOperationException();
		//setTimeToNow(); // when sync is done, update last updated time
	}
	
	/**
	 * Syncs to server.  Calls appropriate sync method.
	 */
	public void sync(String hash) {
		throw new UnsupportedOperationException();
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
			String filter = mDbHelper.KEY_SOLVED + " != 'unsolved'";
		} else {
			String filter = mDbHelper.KEY_SOLVED + " = 'unsolved'";
		}
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns an adapter mapping a cursor to a row item
	 * @return A SimpleCursorAdapter
	 */
	public SimpleCursorAdapter getAdapter() {
		return new CluesAdapter(mCtx, fetchAllClues());
	}
	
	/**
	 * Simple clue database helper class.  Defines CRUD options for the database, 
	 * and gives ability to list all clues, list a specific clue, or filter 
	 * for a given clueId expression.
	 * Also includes a time table in which last update time is stored, 
	 * and getTime() and setTime() methods for updating.  Time is automatically 
	 * set to zero when the database is first created.
	 * @author francis
	 */
	private static class ClueDbAdapter implements Closeable {
		// Debugging TAG
		public static final String TAG = "ClueDbHelper";
		

		private static final String DB_NAME = "cluedata";
		private static final String CLUE_TABLE = "clueTable";
		private static final String TIME_TABLE = "timeTable";
		private static final Integer DB_VERSION = 1;

		// Define column names for the SQLite Clue DB
		public static final String KEY_CLUEID = "clueid";
		public static final String KEY_TEXT = "cluetext";
		public static final String KEY_ANS = "ans";
		public static final String KEY_SOLVED = "solved"; //boolean
		public static final String KEY_POINTS = "points";
		public static final String KEY_PHOTO_PATH = "photo_path";
		public static final String KEY_LOCATION_X = "locationX";
		public static final String KEY_LOCATION_Y = "locationY";
		public static final String KEY_UPLOADED = "uploaded"; //boolean
		public static final String KEY_ROWID = "_id";
		
		// Define column names for the SQLite Time DB
		public static final String TIME_TIME = "time";

		// photo path is a URI, but saved as a String
		// use Uri.parse(s) and myUri.toString()
		
		// time is saved in UTC milliseconds time format (long)
		// time value of 0 indicates database has never been synced

		// Database creation command
		/*private static final String DB_CREATE = 
				"create table clues (_id integer primary key autoincrement, " + 
				"num text not null, cluetext text not null, ans text not null, " +
				"locationX real not null, locationY real not null, " +
				"solved string not null, points integer not null, " + 
				"photo_path text, uploaded integer not null);";*/

		private static final String CLUEDB_CREATE = makeClueDatabaseCreator();
		private static final String TIMEDB_CREATE = makeTimeDatabaseCreator();
		private static final String SET_TIME_ZERO = 
				"INSERT INTO " + TIME_TABLE + " VALUES(0);";

		private static final String makeClueDatabaseCreator() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table ");
			sb.append(CLUE_TABLE);
			sb.append(" (");
			sb.append(KEY_ROWID);
			sb.append(" integer primary key autoincrement, ");
			sb.append(KEY_CLUEID);
			sb.append(" text not null, ");
			sb.append(KEY_ANS);
			sb.append(" text not null, ");
			sb.append(KEY_TEXT);
			sb.append(" text not null, ");
			sb.append(KEY_POINTS);
			sb.append(" integer not null, ");
			sb.append(KEY_LOCATION_X);
			sb.append(" integer not null, ");
			sb.append(KEY_LOCATION_Y);
			sb.append(" integer not null, ");
			sb.append(KEY_SOLVED);
			sb.append(" text not null, ");
			sb.append(KEY_PHOTO_PATH);
			sb.append(" text, ");
			sb.append(KEY_UPLOADED);
			sb.append(" integer not null);");
			return sb.toString();
		}
		
		private static final String makeTimeDatabaseCreator() {
			StringBuilder sb = new StringBuilder("create table ");
			sb.append(TIME_TABLE);
			sb.append(" (");
			sb.append(TIME_TIME);
			sb.append(" integer primary key);");
			return sb.toString();
		}

		private final Context mCtx;

		// Create references to a database and a database handler.
		private DbHelper mDbHelper;
		private SQLiteDatabase mDb;

		private static class DbHelper extends SQLiteOpenHelper {

			/**
			 * Constructor: calls the constructor of SQLiteOpenHelper with
			 * name DB_NAME, CursorFactory null, and version DB_VERSION.
			 * 
			 * @param ctx The context with which to work.
			 */
			DbHelper(Context ctx) {
				super(ctx, DB_NAME, null, DB_VERSION);
			}


			/**
			 * Creates the database and the clues and time tables.
			 * 
			 * @param db the database to create.
			 */
			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(CLUEDB_CREATE);
				db.execSQL(TIMEDB_CREATE);
				db.execSQL(SET_TIME_ZERO);
			}

			/**
			 * Handles database upgrades.  When upgrading, deletes all clue data.
			 * 
			 * @param db the database that is being upgraded.
			 * @param oldVersion the old database version number.
			 * @param newVersion the new database version number.
			 */
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				Log.w(TAG, "Upgrading database from version " + oldVersion + " to " 
						+ newVersion + ", which will destroy all data.");
				clear(db);
			}

			/**
			 * Re-initializes the database.
			 * @param db The database to be initialized.
			 */
			public void clear(SQLiteDatabase db) {
				db.execSQL("DROP TABLE IF EXISTS " + CLUE_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + TIME_TABLE);
				onCreate(db);
			}

		}

		/**
		 * Constructor - takes a context from which the database is opened/created.
		 * @param ctx The context within which to work.
		 */
		public ClueDbAdapter(Context ctx) {
			this.mCtx = ctx;
		}

		/**
		 * Open the clues database.  If it cannot be opened, try to create a 
		 * new instance of the database.  If that fails, throws an SQLException.
		 * @return this (self reference), to allow for chaining in initialization calls.
		 * @throws SQLException if the database could not be opened or created.
		 */
		public ClueDbAdapter open() throws SQLException {
			mDbHelper = new DbHelper(mCtx); // initialize the database helper
			mDb = mDbHelper.getWritableDatabase(); // initialize the database field
			return this;
		}
		/**
		 * Closes the database.
		 */
		public void close() {
			mDbHelper.close();
		}

		/**
		 * Inserts a clue into the database.  Returns the row ID if successful,
		 * or -1 if failed.
		 * @param clueId the identifier of the clue being added.
		 * @param answer the answer text of the clue.
		 * @param originalClue the original text of the clue.
		 * @param points the point value of the clue.
		 * @param location the location of the clue (?)
		 * @param solved whether the clue is solved or not
		 * @param photo_path the path to the photo as a serialized URI (may be null)
		 * @param uploaded a boolean indicating whether the photo has been uploaded
		 * @return the row ID of the new clue in the database, or -1 if there is an error.
		 */
		public long insertClue(String clueId, String answer, String originalClue,
				Integer points, Double[] location, String solved,
				String photo_path, Boolean uploaded) {
			ContentValues cv = new ContentValues();
			cv.put(KEY_CLUEID, clueId);
			cv.put(KEY_ANS, answer);
			cv.put(KEY_TEXT, originalClue);
			cv.put(KEY_POINTS, points);
			cv.put(KEY_LOCATION_X, location[0]);
			cv.put(KEY_LOCATION_Y, location[1]);
			cv.put(KEY_SOLVED, solved);
			cv.put(KEY_PHOTO_PATH, photo_path);
			cv.put(KEY_UPLOADED, uploaded);

			return mDb.insert(CLUE_TABLE, null, cv);
		}

		/**
		 * Deletes a clue from the database based on row ID.
		 * @param rowID the Row ID of the clue to be deleted.
		 * @return true if successful, false otherwise
		 */
		public boolean deleteClue(long rowID) {
			return mDb.delete(CLUE_TABLE, KEY_ROWID + "=" + rowID, null) > 0;
		}

		/**
		 * Deletes a clue from the database based on clue ID.
		 * @param clueId the clue ID of the clue to be deleted.
		 * @return true if successful, false otherwise
		 */
		public boolean deleteClue(String clueId) {
			return mDb.delete(CLUE_TABLE, KEY_CLUEID + "=" + clueId, null) > 0;
		}

		/**
		 * Initializes the database of players.
		 */
		public void clear() {
			mDbHelper.clear(mDb);
		}

		/**
		 * Updates the clue database according to the information provided.
		 * @param clueId the identifier of the clue being updated.
		 * @param answer the answer text of the clue.
		 * @param originalClue the original text of the clue.
		 * @param points the point value of the clue.
		 * @param location the location of the clue (Double[])
		 * @param solved whether the clue is solved or not
		 * @param photo_path the path to the photo as a serialized URI (may be null)
		 * @param uploaded a boolean indicating whether the photo has been uploaded
		 * @return true if the clue was successfully updated, false otherwise
		 */
		public boolean updateClue(String clueId, String answer, String originalClue,
				Integer points, Double[] location, String solved,
				String photo_path, Boolean uploaded) {
			ContentValues cv = new ContentValues();
			cv.put(KEY_ANS, answer);
			cv.put(KEY_TEXT, originalClue);
			cv.put(KEY_POINTS, points);
			cv.put(KEY_LOCATION_X, location[0]);
			cv.put(KEY_LOCATION_Y, location[1]);
			cv.put(KEY_SOLVED, solved);
			cv.put(KEY_PHOTO_PATH, photo_path);
			cv.put(KEY_UPLOADED, uploaded);

			return mDb.update(CLUE_TABLE, cv, KEY_CLUEID + "=" + clueId, null) > 0;
		}

		/**
		 * Fetches all clues in the database.
		 * @return a cursor over all clues in the database.
		 */
		public Cursor fetchAllClues() {
			return mDb.query(CLUE_TABLE,
					new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
					KEY_POINTS, KEY_LOCATION_X, KEY_LOCATION_Y, KEY_SOLVED, KEY_PHOTO_PATH, 
					KEY_UPLOADED },
					null, null, null, null, null);
		}

		/**
		 * Returns a Cursor positioned at the clue that matches the given rowId
		 * @param rowId id of clue to retrieve
		 * @return Cursor positioned at the matching clue, if found
		 * @throws SQLException if clue could not be found/retrieved
		 */
		public Cursor fetchClue(long rowId) throws SQLException {
			Cursor mCursor = mDb.query(true, CLUE_TABLE,
					new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
					KEY_POINTS, KEY_LOCATION_X, KEY_LOCATION_Y, KEY_SOLVED, KEY_PHOTO_PATH, 
					KEY_UPLOADED },
					KEY_ROWID + "=" + rowId, null, null, null, null, null);
			if (mCursor != null)
				mCursor.moveToFirst();
			return mCursor;
		}

		/**
		 * Returns a Cursor of all clues that match the given ClueID string.
		 * @param clueId id to pattern match clues to retrieve
		 * @return Cursor of all clues that match the given expression
		 * @throws SQLException if clue could not be found/retrieved
		 */
		public Cursor filterClues(String clueId) throws SQLException {
			Cursor mCursor = mDb.query(true, CLUE_TABLE,
					new String[] { KEY_ROWID, KEY_CLUEID, KEY_ANS, KEY_TEXT,
					KEY_POINTS, KEY_LOCATION_X, KEY_LOCATION_Y, KEY_SOLVED, KEY_PHOTO_PATH, 
					KEY_UPLOADED },
					KEY_CLUEID + " LIKE " + clueId + "%", null, null, null, null, null);
			return mCursor;
		}
		
		public void setTime(long t) {
			ContentValues cv = new ContentValues();
			cv.put(TIME_TIME, t);
			mDb.update(TIME_TABLE, cv, null, null);
		}
		
		public long getTime() {
			Cursor c = mDb.rawQuery("SELECT * FROM " + TIME_TABLE, null);
			c.moveToFirst();
			int t = c.getInt(c.getColumnIndex(TIME_TIME));
			c.close();
			return t;
		}

	}

}
